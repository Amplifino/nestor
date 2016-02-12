package com.amplifino.pools;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.counters.CountsSupplier;

@ProviderType
public interface Pool<T> extends CountsSupplier {
	
	T borrow();
	void release(T borrowed);
	void evict(T borrowed);
	void close();
	void await() throws InterruptedException;
	boolean await(long amount, TimeUnit timeUnit) throws InterruptedException;
	String name();
	
	static <S> Builder<S> builder(Supplier<S> supplier) {
		return new DefaultPool.DefaultBuilder<>(supplier);
	}
	
	interface Builder<T> {
		Builder<T> destroy(Consumer<T> consumer);
		Builder<T> onRelease(Predicate<T> predicate);
		Builder<T> onBorrow(Predicate<T> predicate);
		Builder<T> initialSize(int initialSize);
		Builder<T> maxSize(int maxSize);
		Builder<T> maxIdle(int maxIdle);
		Builder<T> maxWait(long amount, TimeUnit timeUnit);
		Builder<T> maxIdleTime(long amount, TimeUnit timeUnit);
		Builder<T> fifo();
		Builder<T> lifo();
		Builder<T> logger(Logger logger);
		Builder<T> name(String name);
		Pool<T> build();
	}
	
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
