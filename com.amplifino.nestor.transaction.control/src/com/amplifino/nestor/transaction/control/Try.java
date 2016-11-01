package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Try<T> {

	<U extends Exception> T orElseThrow(Function<Throwable, U> exceptionWrapper) throws U;
	Try<T> handle(BiConsumer<T, Throwable> handler);
	
	static <T> Try<T> of(Callable<T> callable) {	
		try {
			return new Success<>(callable.call());
		} catch (Throwable e) {
			return new Failure<>(e);
		}
	}
	
	class Success<T> implements Try<T> {
		
		private final T value;
		
		private Success(T value) {
			this.value = value;
		}

		@Override
		public <U extends Exception> T orElseThrow(Function<Throwable, U> exceptionWrapper) throws U {
			return value;
		}
		
		@Override
		public Success<T> handle(BiConsumer<T, Throwable> handler) {
			handler.accept(value, null);
			return this;
		}
	}
	
	class Failure<T> implements Try<T> {
		
		private final Throwable e;
		
		private Failure(Throwable e) {
			this.e = e;
		}

		@Override
		public <U extends Exception> T orElseThrow(Function<Throwable, U> exceptionWrapper) throws U {
			throw exceptionWrapper.apply(e);
		}
		
		@Override
		public Failure<T> handle(BiConsumer<T, Throwable> handler) {
			handler.accept(null, e);
			return this;
		}
	}
}
