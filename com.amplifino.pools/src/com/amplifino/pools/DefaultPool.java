package com.amplifino.pools;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;

class DefaultPool<T> implements Pool<T> {
	
	private BlockingDeque<PoolEntry<T>> idles;
	private final AtomicInteger poolSize = new AtomicInteger(0);
	private volatile boolean closed = false;
	private CountDownLatch closeComplete = new CountDownLatch(1);
	
	private final Supplier<T> supplier;
	private Consumer<T> destroyer = this::close;
	private Predicate<T> onBorrow =  t -> true;
	private Predicate<T> onRelease = t -> true;
	private Strategy strategy = Strategy.LIFO;
	
	private int initialSize = 0;
	private int maxSize = Integer.MAX_VALUE;
	private int maxIdle = Integer.MAX_VALUE;
	private long maxWaitAmount = -1;
	private TimeUnit maxWaitUnit;
	
	private long maxIdleTime = Long.MAX_VALUE;
	private long minIdleTime = -1L;
	
	private Logger logger = Logger.getLogger("com.amplifino.pools");
	private Counters<Stats> counters = Counters.of(Stats.class);
	private String name = "No name";

	private DefaultPool(Supplier<T> supplier) {
		this.supplier = supplier;
	}
	
	private void init() {
		idles = new LinkedBlockingDeque<>(maxIdle);
		for (int i = 0 ; i < initialSize ; i++) {
			doRelease(allocate());
		}
	}
	
	@Override
	public T borrow() {
		boolean success = false;
		try {
			while(!closed) {
				PoolEntry<T> candidate = doBorrow();
				if (activate(candidate)) {
					success = true;
					return candidate.get();
				} else {
					destroy(candidate.get());
				}
			} 
		} finally {
			counters.increment(success ? Stats.BORROWS : Stats.FAILURES);
		}	
		throw new NoSuchElementException("Pool closed");
	}

	@Override
	public void release(T borrowed) {
		counters.increment(Stats.RELEASES);
		doRelease(borrowed);
		if (closed) {
			tryClose();	
		}
	}
	
	@Override
	public void evict(T borrowed) {
		counters.increment(Stats.RELEASES).increment(Stats.EVICTIONS);
		destroy(borrowed);
		if (closed) {
			tryClose();
		}
	}
	
	@Override
	public void close() {
		if (closed) {
			throw new IllegalStateException("Already closed");
		}
		closed = true;
		tryClose();
	}
	
	@Override
	public String name() {
		return name;
	}
	
	private synchronized void tryClose() {
		if (idles.size() == poolSize.get()) {
			for ( PoolEntry<T> entry = idles.pollLast() ; entry != null; entry = idles.pollLast()) {
				destroy(entry.get());
			}
			closeComplete.countDown();
		}
	}
	
	private void close(T lease) {
		Optional.of(lease)
			.filter(AutoCloseable.class::isInstance)
			.map(AutoCloseable.class::cast)
			.ifPresent(this::doClose);
	}
	
	private void doClose(AutoCloseable lease) {
		try {
			lease.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, logHeader() + "Error when closing ",e);
		} 
	}
	
	@Override
	public void await() throws InterruptedException {
		closeComplete.await();
	}

	@Override
	public boolean await(long amount, TimeUnit timeUnit) throws InterruptedException {
		return closeComplete.await(amount, timeUnit);
	}

	
	@Override
	public Counts counts() {
		return counters.counts();
	}
	
	private PoolEntry<T> doBorrow() {
		return Optional.ofNullable(idles.pollLast()).orElseGet(this::allocateOrWait);
	}
	
	private void doRelease(T borrowed) {
		if (!passivate(borrowed) || !strategy.offer(idles, new PoolEntry<>(borrowed))) {
			destroy(borrowed);
		}
	}
	
	private boolean activate(PoolEntry<T> entry) {
		if (entry.older(maxIdleTime)) {
			counters.increment(Stats.IDLETIMEEXCEEDED);
			return false;
		}
		try {
			boolean result = !entry.older(minIdleTime) || onBorrow.test(entry.get());
			if (!result) {
				counters.increment(Stats.INVALIDONBORROW);
			}
			return result;
		} catch (Exception e) {
			logger.log(Level.WARNING, logHeader() + "Pool Error when borrowing " + entry.get(), e);
			counters.increment(Stats.INVALIDONBORROW);
			return false;
		}
	}
	
	private String logHeader() {
		return "Pool " + name + ": ";
	}
	
	private boolean passivate(T borrowed) {
		try {
			boolean result = onRelease.test(borrowed);
			if (!result) {
				counters.increment(Stats.INVALIDONRELEASE);
			}
			return result;
		} catch (Exception e) {
			logger.log(Level.WARNING, logHeader() + "Error when releasing " + borrowed, e);
			counters.increment(Stats.INVALIDONRELEASE);
			return false;
		}
	}
	
	private PoolEntry<T> allocateOrWait() {
		if ( poolSize.get() >= maxSize) {
			return waitForRelease();
		} else {
			return new PoolEntry<>(allocate());
		}
	}
	
	private PoolEntry<T> waitForRelease() {
		counters.increment(Pool.Stats.SUSPENDS);
		try {
			return doWaitForRelease();
		} catch (InterruptedException e) {
			throw new NoSuchElementException(e.toString());
		}
	}
	
	private PoolEntry<T> doWaitForRelease() throws InterruptedException {
		if (maxWaitAmount == -1) {
			return idles.takeLast();			
		} else {
			return Optional.ofNullable(idles.pollLast(maxWaitAmount, maxWaitUnit))					
					.orElseThrow(this::timeOutException);
		}
	}
	
	private NoSuchElementException timeOutException() {
		counters.increment(Stats.TIMEOUTS);
		return new NoSuchElementException("Time out while waiting on pool");
	}
	
	private T allocate() {
		T t = supplier.get();
		int currentSize = poolSize.incrementAndGet();
		logger.info(logHeader() + "Pool size increased to " + currentSize);
		counters.increment(Stats.ALLOCATIONS);
		counters.max(Stats.MAXSIZE, currentSize);
		return t;
	}
	
	private void destroy(T t) {
		int currentSize = poolSize.decrementAndGet();
		logger.info(logHeader() + "Pool size decreased to " + currentSize);
		counters.increment(Stats.DESTROYS);
		destroyer.accept(t);
	}
	
	static class DefaultBuilder<T> implements Pool.Builder<T> {
		
		private final DefaultPool<T> pool; 
		
		DefaultBuilder(Supplier<T> supplier) {
			pool = new DefaultPool<>(supplier);
		}

		@Override
		public Builder<T> destroy(Consumer<T> consumer) {
			pool.destroyer = Objects.requireNonNull(consumer);
			return this;
		}

		@Override
		public Builder<T> onRelease(Predicate<T> predicate) {
			pool.onRelease = Objects.requireNonNull(predicate);
			return this;
		}

		@Override
		public Builder<T> onBorrow(Predicate<T> predicate) {
			pool.onBorrow = Objects.requireNonNull(predicate);
			return this;
		}
		
		@Override
		public Builder<T> initialSize(int initialSize) {
			if (initialSize < 0) {
				throw new IllegalArgumentException();
			}
			pool.initialSize = initialSize;
			return this;
		}

		@Override
		public Builder<T> maxSize(int maxSize) {
			if (maxSize <= 0) {
				throw new IllegalArgumentException();
			}
			pool.maxSize = maxSize;
			return this;
		}

		@Override
		public Builder<T> maxIdle(int maxIdle) {
			if (maxIdle <= 0) {
				throw new IllegalArgumentException();
			}
			pool.maxIdle = maxIdle;
			return this;
		}

		@Override
		public Builder<T> maxWait(long amount, TimeUnit timeUnit) {
			if (amount < 0) {
				throw new IllegalArgumentException();
			}
			pool.maxWaitAmount = amount;
			pool.maxWaitUnit = Objects.requireNonNull(timeUnit);
			return this;
		}
		
		@Override
		public Builder<T> maxIdleTime(long amount, TimeUnit timeUnit) {
			pool.maxIdleTime = TimeUnit.MILLISECONDS.convert(amount,  timeUnit);
			return this;
		}
		
		@Override
		public Builder<T> fifo() {
			pool.strategy = Strategy.FIFO;
			return this;
		}
		
		@Override
		public Builder<T> lifo() {
			pool.strategy = Strategy.LIFO;
			return this;
		}
		
		@Override
		public Builder<T> logger(Logger logger) {
			pool.logger = Objects.requireNonNull(logger);
			return this;
		}
		
		@Override
		public Builder<T> name(String name) {
			pool.name = name;
			return this;
		}
		
		@Override
		public Builder<T> minIdleTime(long amount, TimeUnit timeUnit) {
			pool.minIdleTime = TimeUnit.MILLISECONDS.convert(amount,  timeUnit);
			return this;
		}
		
		@Override
		public Pool<T> build() {
			pool.init();
			return pool;
		}
		
	}

	private static enum Strategy {
		FIFO {
			@Override
			<T> boolean offer(BlockingDeque<T> deque, T element) {
				return deque.offerFirst(element);
			}
		},
		LIFO {
			@Override
			<T> boolean offer(BlockingDeque<T> deque, T element) {
				return deque.offerLast(element);
			}
		};

		abstract <T> boolean offer(BlockingDeque<T> deque , T  element);
	}
}
