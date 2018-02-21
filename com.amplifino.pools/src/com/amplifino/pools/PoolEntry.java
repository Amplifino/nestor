package com.amplifino.pools;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Interface for advanced pool users 
 *
 * @param <T> the pooled type
 */
@ProviderType
public interface PoolEntry<T> {

	/**
	 * return the pooled member
	 * @return the member
	 */
	T get();
	/**
	 * return the age in milliseconds that the entry was idle in the pool
	 * @return the entry's age
	 */
	long age();
	
	/**
	 * test if the entry has been idle for more milliseconds than the argument.
	 * @param ms age in milliseconds
	 * @return true if entry has been idle longer than the argument
	 */
	boolean older(long ms);
	
	/**
	 * @return true if this is a newly allocated pool member, false if it comes from the idle pool
	 */
	boolean isFresh();
}
