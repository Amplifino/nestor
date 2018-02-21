package com.amplifino.counters;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Thread safe counters used for instrumenting infrastructure components
 *
 * @param <T> enum type
 */
@ProviderType
public interface Accumulators<T extends Enum<T>> {
	
	/**
	 * perform the default operation for this key
	 * @param key counter identification
	 * @param value value to accumulate
	 * @return this
	 */
	Accumulators<T> accumulate(T key, long value);
	
	/**
	 * increment. Behavior depends on long operator. Equivalent to accumulate(key, 1);
	 * @param key counter identification
	 * @return this
	 */
	Accumulators<T> increment(T key);
	/**
	 * @return the current value of the counters
	 */
	Counts counts();
	/**
	 * return a new set of counters for the given enum type
	 * @param enumClass the enum type
	 * @param <T> the enum type
	 * @return the new Accumulators instance
	 */
	static <T extends Enum<T>> Accumulators<T> of (Class<T> enumClass) {
		return DefaultAccumulators.of(enumClass);
	}
	
}
