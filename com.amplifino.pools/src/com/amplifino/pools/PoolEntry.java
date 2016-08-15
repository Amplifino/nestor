package com.amplifino.pools;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author kha
 *
 * @param <T>
 */
@ProviderType
public interface PoolEntry<T> {

	/**
	 * return the pooled member
	 * @return
	 */
	T get();
	/**
	 * return the age in milliseconds that the entry was idle in the pool
	 * @return
	 */
	long age();
	
	/**
	 * test if the entry has been idle for more milliseconds than the argument.
	 * @param ms
	 * @return
	 */
	boolean older(long ms);
}
