package com.amplifino.nestor.transaction.provider;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CompositeException<T> {

	private final List<Map.Entry<T, Throwable>> exceptions = new ArrayList<>();
	private final ThrowingConsumer<T> consumer;
	
	private CompositeException(ThrowingConsumer<T> consumer) {
		this.consumer = consumer;
	}
	
	void add(T t) {
		try {
			consumer.accept(t);
		} catch (Throwable e) {
			exceptions.add(new AbstractMap.SimpleImmutableEntry<>(t,e));
		}
	}
	
	void addAll(CompositeException<T> other) {
		exceptions.addAll(other.exceptions);
	}
	
	boolean isEmpty() {
		return exceptions.isEmpty();
	}
	
	Stream<Map.Entry<T, Throwable>> failed() {
		return exceptions.stream();
	}
	
	<U extends Exception> void ifNotEmptyThrow(Supplier<U> supplier) throws U {
		if (!exceptions.isEmpty()) {
			U e = supplier.get();
			e.initCause(exceptions.get(0).getValue());
			exceptions.stream().skip(1).map(Map.Entry::getValue).forEach(e::addSuppressed);
			throw e;
		}
	}

	Stream<Throwable> exceptions() {
		return failed().map(Map.Entry::getValue);
	}
	
	static <T> CompositeException<T> of (ThrowingConsumer<T> consumer) {
		return new CompositeException<>(consumer);
	}
	
	@FunctionalInterface
	static interface ThrowingConsumer<T> {
		void accept(T t) throws Exception;
	}
	
}
