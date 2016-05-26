package com.amplifino.nestor.associations;

import java.util.Objects;
import java.util.function.IntFunction;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface IntAssociation<V> {

	int key();
	V value();
	
	static <V> IntAssociation<V> of(int key, V value) {
		return new SimpleIntAssociation<>(key, Objects.requireNonNull(value));
	}
	
	static <V> IntAssociation<V> of(int key, IntFunction<V> fetcher) {
		return new LazyIntAssociation<>(key, Objects.requireNonNull(fetcher));
	}
}
