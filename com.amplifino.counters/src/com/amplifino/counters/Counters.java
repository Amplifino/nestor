package com.amplifino.counters;

public interface Counters<T extends Enum<T>> {
	
	Counters<T> increment(T key);
	Counters<T> add(T key, long increment);
	Counters<T> max(T key, long challenge);
	Counts counts();
	
	static <T extends Enum<T>> Counters<T> of (Class<T> enumClass) {
		return DefaultCounters.of(enumClass);
	}

	static <T extends Enum<T>> Counters<T> empty(Class<T> enumClass) {
		return NoCounters.of(enumClass);
	}
}
