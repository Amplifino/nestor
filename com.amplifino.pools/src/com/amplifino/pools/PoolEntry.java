package com.amplifino.pools;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface PoolEntry<T> {

	T get();
	long age();
	boolean older(long ms);
}
