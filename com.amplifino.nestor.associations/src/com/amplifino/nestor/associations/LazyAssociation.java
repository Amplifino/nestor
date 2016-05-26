package com.amplifino.nestor.associations;

import java.util.Objects;
import java.util.function.Function;

final class LazyAssociation<K, V> implements Association<K, V> {

	private final K key;
	private final Function<K,V> fetcher;
	private V value;
	
	LazyAssociation(K key, Function<K,V> fetcher) {
		this.key = key;
		this.fetcher = fetcher;
	}
	
	@Override
	public K key() {
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
