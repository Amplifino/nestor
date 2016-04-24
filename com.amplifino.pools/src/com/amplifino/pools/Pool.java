package com.amplifino.pools;

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
	 * @return 
	 */
	T borrow();
	/**
	 * return a previously borrowed member to the pool 
	 * @param borrowed
	 */
	void release(T borrowed);
	/**
	 * return a previously borrowed member, instructing the pool to evict it from the pool
	 * @param borrowed
	 */
	void evict(T borrowed);
	/**
	 * closes the pool. 
	 * The call returns immediately, but the pool will wait untill all leases are returned 
	 */
	void close();
	/**
	 * wait for termination of the pool
	 * @throws InterruptedException
	 */
	void await() throws InterruptedException;
	/**
	 * wait for termination of the pool
	 * @param amount
	 * @param timeUnit
	 * @return
	 * @throws InterruptedException
	 */
	boolean await(long amount, TimeUnit timeUnit) throws InterruptedException;
	/**
	 * @return the pool name
	 */
	String name();
	
	/**
	 * return a new pool builder
	 * @param supplier for creating pool members
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
	/**
	 * @author kha
	 *
	 * @param <T>
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
		 * @param initialSize
		 * @return this
 		 */
		Builder<T> initialSize(int initialSize);
		/**
		 * sets the pool maximum size.
		 * @param maxSize
		 * @return this
		 */
		Builder<T> maxSize(int maxSize);
		/**
		 * sets the maximum number of idle members in the pool
		 * @param maxIdle
		 * @return this
		 */
		Builder<T> maxIdle(int maxIdle);
		/**
		 * sets the maximum amount of time a caller will wait for a free pool member 
		 * @param amount
		 * @param timeUnit
		 * @return this
		 */
		Builder<T> maxWait(long amount, TimeUnit timeUnit);
		/**
		 * sets the maximum amount a member can remain idle in the pool
		 * @param amount
		 * @param timeUnit
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
		 * @param logger
		 * @return this
		 */
		Builder<T> logger(Logger logger);
		/**
		 * sets the pool name. Pool name is used in log messages.
		 * @param name
		 * @return this
		 */
		Builder<T> name(String name);
		/**
		 * sets the pool minimum idle time for executing onBorrow filter
		 * @return this
		 */
		Builder<T> minIdleTime(long amount, TimeUnit timeUnit);
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
		IDLETIMEEXCEEDED;
	}

}
