package com.amplifino.nestor.associations;

import java.util.Objects;
import java.util.function.LongFunction;

final class LazyLongAssociation<V> implements LongAssociation<V> {

	private final long key;
	private final LongFunction<V> fetcher;
	private V value;
	
	LazyLongAssociation(long key, LongFunction<V> fetcher) {
		this.key = key;
		this.fetcher = fetcher;
	}
	
	@Override
	public long key() {
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
