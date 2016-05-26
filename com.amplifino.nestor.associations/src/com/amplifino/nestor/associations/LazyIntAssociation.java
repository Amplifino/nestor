package com.amplifino.nestor.associations;

import java.util.Objects;
import java.util.function.IntFunction;

final class LazyIntAssociation<V> implements IntAssociation<V> {

	private final int key;
	private final IntFunction<V> fetcher;
	private V value;
	
	LazyIntAssociation(int key, IntFunction<V> fetcher) {
		this.key = key;
		this.fetcher = fetcher;
	}
	
	@Override
	public int key() {
		return key;
	}

	@Override
	public V value() {
		if (value == null) {
			value = Objects.requireNonNull(fetcher.apply(key));
		}
		return value;
	}

	@Override
	public String toString() {
		return key + " -> "  + (value == null ? "<lazy> " : value);
	}
}
