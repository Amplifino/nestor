package com.amplifino.nestor.associations;

import java.util.Objects;
import java.util.function.LongFunction;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface LongAssociation<V> {

	long key();
	V value();
	
	static <V> LongAssociation<V> of(long key, V value) {
		return new SimpleLongAssociation<>(key, Objects.requireNonNull(value));
	}
	
	static <V> LongAssociation<V> of(long key, LongFunction<V> fetcher) {
		return new LazyLongAssociation<>(key, Objects.requireNonNull(fetcher));
	}
}
