package com.amplifino.pools;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.counters.CountsSupplier;

/**
 * Generic Object Pool
 *
 * @param <T> Pooled Object type
 */
@ProviderType
public interface Pool<T> extends CountsSupplier {
	
	/**
	 * borrow a pool member
	 * @return the borrowed pool member
	 */
	T borrow();
	/**
	 * borrow a pool member entry.
	 * To be used for pool users that need to know the time the entry remained idle in the pool
	 * @return the borrowed pool entry
	 */
	PoolEntry<T> borrowEntry();
	
	/**
	 * return a previously borrowed member to the pool 
	 * @param borrowed the borrowed member
	 */
	void release(T borrowed);
	/**
	 * return a previously borrowed member, instructing the pool to evict it from the pool
	 * @param borrowed the borrowed pool member
	 */
	void evict(T borrowed);
	/**
	 * closes the pool. 
	 * The call returns immediately, but the pool will wait untill all leases are returned 
	 */
	void close();
	/**
	 * wait for termination of the pool
	 * @throws InterruptedException if thread was interrupted
	 */
	void await() throws InterruptedException;
	/**
	 * wait for termination of the pool
	 * @param amount wait time amount 
	 * @param timeUnit wait time unit
	 * @return true if pool was terminated before argument elapsed.
	 * @throws InterruptedException if thread was interrupted
	 */
	boolean await(long amount, TimeUnit timeUnit) throws InterruptedException;
	/**
	 * @return the pool name
	 */
	String name();
	
	/**
	 * perform a pool maintenance cycle 
	 */
	void cycle();
	
	/**
	 * returns the current pool size. For monitoring purpose only.
	 * 
	 * @return the pool size
	 */
	int size();
	/**
	 * return a new pool builder
	 * @param supplier for creating pool members
	 * @param <S> the pool member type
	 * @return the new builder
	 */
	static <S> Builder<S> builder(Supplier<S> supplier) {
		return new DefaultPool.DefaultBuilder<>(supplier);
	}
	
	/**
	 * Pool builder
	 *
	 * @param <T> pool member type
	 */	
	@ProviderType
	interface Builder<T> {
		/**
		 * configures the pool's member destroy logic
		 * @param consumer will be called when a member is removed from the pool 
		 * @return this
		 */
		Builder<T> destroy(Consumer<T> consumer);
		/**
		 * configures the pool's onRelease logic 
		 * @param predicate will be called when a member is returned to the pool. If the filter returns false the member is removed from the pool.
		 * @return this
		 */
		Builder<T> onRelease(Predicate<T> predicate);
		/**
		 * configures the pool's onBorrow logic
		 * @param predicate will be called when a member is leased. If the filter returns false the member is removed from the pool and a new member is tried.
		 * @return this
		 */
		Builder<T> onBorrow(Predicate<T> predicate);
		/**
		 * sets the pool initial size
		 * @param initialSize the pool's initial size
		 * @return this
 		 */
		Builder<T> initialSize(int initialSize);
		/**
		 * sets the pool maximum size.
		 * @param maxSize max size count
		 * @return this
		 */
		Builder<T> maxSize(int maxSize);
		/**
		 * sets the maximum number of idle members in the pool
		 * @param maxIdle max idle count
		 * @return this
		 */
		Builder<T> maxIdle(int maxIdle);
		/**
		 * sets the maximum amount of time a caller will wait for a free pool member 
		 * @param amount max idle time amount
		 * @param timeUnit max idle time unit
		 * @return this
		 */
		Builder<T> maxWait(long amount, TimeUnit timeUnit);
		/**
		 * sets the maximum amount a member can remain idle in the pool
		 * @param amount max wait time amount
		 * @param timeUnit max wait time unit
		 * @return this
		 */
		Builder<T> maxIdleTime(long amount, TimeUnit timeUnit);
		/**
		 * sets the member allocation strategy to FIFO (first in, first out)
		 * @return this
		 */
		Builder<T> fifo();
		/**
		 * sets the member allocation strategy to LIFO (last in, first out)
		 * @return this
		 */
		Builder<T> lifo();
		/**
		 * Overrides the default logger instance
		 * @param logger the pool logger
		 * @return this
		 */
		Builder<T> logger(Logger logger);
		/**
		 * sets the pool name. Pool name is used in log messages.
		 * @param name the pool name
		 * @return this
		 */
		Builder<T> name(String name);
		/**
		 * sets the minimum idle time in order to test the pool member with onBorrow
		 * @param amount idle time amount
		 * @param timeUnit idle time unit
		 * @return this
		 */
		Builder<T> minIdleTime(long amount, TimeUnit timeUnit);
		/**
		 * sets the cycle time (interval between scans for idle pool members that are older than maxIdle)
		 * @param amount delay between cycles		
		 * @param timeUnit delay time unit
		 * @return this
		 */
		Builder<T> propertyCycle(long amount, TimeUnit timeUnit);
		/**
		 * sets the executor service to use for the cycle task
		 * @param executorService used to perform cycle tasks
		 * @return this
		 */
		Builder<T> scheduleExecutorService(ScheduledExecutorService executorService);
		/**
		 * build and start the pool
		 * @return the pool
		 */
		Pool<T> build();
	}
	
	/**
	 * Enumeration used for Pool Statistics Counts
	 *
	 */
	enum Stats {
		ALLOCATIONS,
		DESTROYS,
		BORROWS,
		RELEASES,
		SUSPENDS,
		MAXSIZE,
		INVALIDONBORROW,
		INVALIDONRELEASE,
		IDLETIMEEXCEEDED,
		EVICTIONS,
		TIMEOUTS,
		FAILURES;
	}

}
