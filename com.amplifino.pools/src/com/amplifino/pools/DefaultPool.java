package com.amplifino.pools;

import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;

final class DefaultPool<T> implements Pool<T> {
	
	private Deque<DefaultPoolEntry<T>> idles;
	private Semaphore semaphore;
	private final AtomicInteger poolSize = new AtomicInteger(0);
	private final AtomicBoolean closed = new AtomicBoolean();
	private final CountDownLatch closeComplete = new CountDownLatch(1);
	
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
	
	private long cycleTime = 0;
	private TimeUnit cycleUnit;
	
	private Logger logger = Logger.getLogger("com.amplifino.pools");
	private Counters<Stats> counters = Counters.of(Stats.class);
	private String name = "No name";
	
	private ScheduledExecutorService cycleExecutorService;
	private ScheduledFuture<?> scheduledFuture;

	private DefaultPool(Supplier<T> supplier) {
		this.supplier = supplier;
	}
	
	private void init() {
		if (cycleTime > 0) {
			cycleExecutorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Cycle thread for pool " + name));
		}
		init(cycleExecutorService);
	}
	
	private void init(ScheduledExecutorService executorService) {
		if (initialSize > maxIdle) {
			throw new IllegalStateException("Initial size " + initialSize + " exceeds max idle size " + maxIdle);
		}
		if (initialSize > maxSize) {
			throw new IllegalStateException("Initial size " + initialSize + " exceeds max pool size " + maxSize);
		}
		idles = new LinkedBlockingDeque<>(maxIdle);
		semaphore = new Semaphore(maxSize, true);
		try {
			for (int i = 0 ; i < initialSize ; i++) {
				doRelease(allocate());
			}
		} catch (Throwable e) {
			logger.log(Level.WARNING, logMessage("initial size allocation failed"), e);
		}
		if (cycleTime > 0 ) {
			scheduledFuture = executorService.scheduleAtFixedRate(this::cycle, cycleTime, cycleTime, cycleUnit);
		}
	}
	
	private boolean acquire() {
		if (semaphore.tryAcquire()) {
			return true;
		} else {
			counters.increment(Stats.SUSPENDS);
			try {
				if (maxWaitAmount == -1) {
					semaphore.acquire();
					return true;
				} else {
					return semaphore.tryAcquire(maxWaitAmount, maxWaitUnit);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalThreadStateException("Thread interrupted");
			}
		}
	}
	
	@Override
	public PoolEntry<T> borrowEntry() {		
		if (closed.get()) {
			throw new IllegalStateException("Pool closed");
		}
		if (acquire()) {
			try {
				return takeEntry();
			} catch(Throwable e) {
				counters.increment(Stats.FAILURES);
				semaphore.release();
				throw e;
			}		
		} else {
			counters.increment(Stats.TIMEOUTS).increment(Stats.FAILURES);
			throw new NoSuchElementException("Time out while waiting on pool");
		}
	}
	
	private DefaultPoolEntry<T> takeEntry() {
		while(!closed.get()) {
			DefaultPoolEntry<T> candidate = doBorrow();
			if (activate(candidate)) {
				counters.increment(Stats.BORROWS);
				return candidate;
			} else {
				destroy(candidate.get());
			}
		}		
		tryClose();
		throw new IllegalStateException("Pool closed");
	}
	
	@Override
	public T borrow() {
		return borrowEntry().get();
	}

	@Override
	public void release(T borrowed) {
		counters.increment(Stats.RELEASES);
		doRelease(borrowed);
		semaphore.release();
		if (closed.get()) {
			tryClose();
		}
	}
	
	@Override
	public void evict(T borrowed) {
		counters.increment(Stats.RELEASES).increment(Stats.EVICTIONS);
		destroy(borrowed);
		semaphore.release();
		if (closed.get()) {
			tryClose();
		}
	}
	
	@Override
	public void close() {
		if (!closed.compareAndSet(false, true)) {
			throw new IllegalStateException("Already closed");
		}
		if (scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}
		if (cycleExecutorService != null) {
			cycleExecutorService.shutdown();
		}
		logger.info(logMessage("Close requested"));
		tryClose();
	}
	
	@Override
	public String name() {
		return name;
	}
	
	private synchronized void tryClose() {
		if (idles.size() == poolSize.get()) {
			for ( DefaultPoolEntry<T> entry = idles.pollLast() ; entry != null; entry = idles.pollLast()) {
				destroy(entry.get());				
			}
			// possible a pool entry could be "stolen" by a thread that obtained a permit before the close,
			// and borrowed after the close. extra test on poolSize == 0.
			if (poolSize.get() == 0) {
				logger.info(logMessage("Close complete"));
				closeComplete.countDown();
			}
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
		} catch (Throwable e) {
			logger.log(Level.WARNING, logMessage("Error when closing "),e);
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
	
	private DefaultPoolEntry<T> doBorrow() {
		return Optional.ofNullable(idles.pollLast()).orElseGet(() -> new DefaultPoolEntry<>(this.allocate()));
	}
	
	private void doRelease(T borrowed) {
		if (!passivate(borrowed) || !strategy.offer(idles, new DefaultPoolEntry<>(borrowed))) {
			destroy(borrowed);
		}
	}
	
	private boolean activate(DefaultPoolEntry<T> entry) {
		if (entry.older(maxIdleTime)) {
			counters.increment(Stats.IDLETIMEEXCEEDED);
			return false;
		}
		try {
			boolean result = !entry.older(minIdleTime) || onBorrow.test(entry.get());
			if (!result) {
				counters.increment(Stats.INVALIDONBORROW);
			}
			return result && !closed.get();
		} catch (Throwable e) {
			logger.log(Level.WARNING, logMessage("Pool Error when borrowing " + entry.get()), e);
			counters.increment(Stats.INVALIDONBORROW);
			return false;
		}
	}
	
	private String logHeader() {
		return "Pool " + name + ": ";
	}
	
	private String logMessage(String text) {
		return logHeader().concat(text);
	}
	
	private boolean passivate(T borrowed) {
		try {
			boolean result = onRelease.test(borrowed);
			if (!result) {
				counters.increment(Stats.INVALIDONRELEASE);
			}
			return result;
		} catch (Throwable e) {
			logger.log(Level.WARNING, logMessage("Error when releasing " + borrowed), e);
			counters.increment(Stats.INVALIDONRELEASE);
			return false;
		}
	}
	
	private T allocate() {
		T t = Objects.requireNonNull(supplier.get());
		int newSize = poolSize.incrementAndGet();
		logger.info(logMessage("Pool size increased to " + newSize));
		counters.increment(Stats.ALLOCATIONS);
		counters.max(Stats.MAXSIZE, newSize);
		return t;		
	}
	
	private void destroy(T t) {
		int currentSize = poolSize.decrementAndGet();
		logger.info(logMessage("Pool size decreased to " + currentSize));
		counters.increment(Stats.DESTROYS);
		try {
			destroyer.accept(t);
		} catch (Throwable e) {
			logger.log(Level.WARNING, "Exception when destroying element " + t, e);
		}
	}
	
	private boolean cycleOne() {
		DefaultPoolEntry<T> oldestEntry = strategy.peekOldest(idles);
		if (oldestEntry == null || !oldestEntry.older(maxIdleTime)) {
			return false;
		} else {
			oldestEntry = strategy.pollOldest(idles);
			if (oldestEntry == null) {
				return false;
			} else {
				boolean expired = oldestEntry.older(maxIdleTime);
				if (expired || !strategy.offerOld(idles,  oldestEntry)) {
					destroy(oldestEntry.get());
				}
				return expired;
			} 
		}
	}
	
	@Override
	public void cycle() {
		if (closed.get()) {
			return;
		}
		while (cycleOne());
		try {
			while (poolSize.get() < initialSize) {
				doRelease(allocate());
			}
		} catch (Throwable e) {
			logger.log(Level.WARNING, logMessage("exception while restoring pool size to " + initialSize), e);
		}
	}
	
	@Override
	public int size() {
		return poolSize.get();
	}
	
	static final class DefaultBuilder<T> implements Pool.Builder<T> {
		
		private DefaultPool<T> pool; 
		private ScheduledExecutorService executorService;
		
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
			pool.maxIdleTime = timeUnit.toMillis(amount);
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
			pool.minIdleTime = timeUnit.toMillis(amount);
			return this;
		}
		
		@Override
		public Builder<T> propertyCycle(long amount, TimeUnit timeUnit) {
			if (amount <= 0) {
				throw new IllegalArgumentException();
			}
			pool.cycleTime = amount;
			pool.cycleUnit = Objects.requireNonNull(timeUnit);
			return this;
		}
		
		@Override
		public Builder<T> scheduleExecutorService(ScheduledExecutorService executorService) {
			this.executorService = Objects.requireNonNull(executorService);
			return this;
		}
		
		@Override
		public Pool<T> build() {
			if (executorService == null) {
				pool.init();
			} else {
				pool.init(executorService);
			}
			// set pool field to null to avoid further modification of pool through this builder
			Pool<T> result = this.pool;
			this.pool = null;
			return result;
		}
		
	}

	private static enum Strategy {
		FIFO {
			@Override
			<T> boolean offer(Deque<T> deque, T element) {
				return deque.offerFirst(element);
			}
			
			@Override
			<T> T peekOldest(Deque<T> deque) {
				return deque.peekFirst();
			}
			
			@Override
			<T> T pollOldest(Deque<T> deque) {
				return deque.pollFirst();
			}
			
			@Override
			<T> boolean offerOld(Deque<T> deque, T element) {
				return deque.offerLast(element);
			}
		},
		LIFO {
			@Override
			<T> boolean offer(Deque<T> deque, T element) {
				return deque.offerLast(element);
			}
			
			@Override
			<T> T peekOldest(Deque<T> deque) {
				return deque.peekLast();
			}
			
			@Override
			<T> T pollOldest(Deque<T> deque) {
				return deque.pollFirst();
			}
			
			@Override
			<T> boolean offerOld(Deque<T> deque, T element) {
				return deque.offerFirst(element);
			}
		};

		abstract <T> boolean offer(Deque<T> deque , T  element);
		abstract <T> T peekOldest(Deque<T> deque);
		abstract <T> T pollOldest(Deque<T> deque);
		abstract <T> boolean offerOld(Deque<T> deque, T element);
	}
}
