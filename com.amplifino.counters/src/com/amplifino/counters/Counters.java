package com.amplifino.counters;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Thread safe counters used for instrumenting infrastructure components
 *
 * @param <T> enum type
 */
@ProviderType
public interface Counters<T extends Enum<T>> {
	
	/**
	 * increment counter
	 * @param key counter identification
	 * @return this
	 */
	Counters<T> increment(T key);
	/**
	 * adds the second argument to the current value of the counter identified by the first argument
	 * @param key counter identification
	 * @param increment value to add
	 * @return this
	 */
	Counters<T> add(T key, long increment);
	/**
	 * replaces the counter identified by the first argument with the second value if that is larger than the current value
	 * @param key counter identification
	 * @param challenge potential new maximum value
	 * @return this
	 */
	Counters<T> max(T key, long challenge);
	/**
	 * @return the current value of the counters
	 */
	Counts counts();
	
	/**
	 * return a new set of counters for the given enum type
	 * @param enumClass the enum type
	 * @return
	 */
	static <T extends Enum<T>> Counters<T> of (Class<T> enumClass) {
		return DefaultCounters.of(enumClass);
	}

	/**
	 * return a null implementation when performance is more imporant than statistics
	 * @param enumClass
	 * @return
	 */
	static <T extends Enum<T>> Counters<T> empty(Class<T> enumClass) {
		return NoCounters.of(enumClass);
	}
}
