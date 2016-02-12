package com.amplifino.counters;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

class NoCounters<T extends Enum<T>> implements Counters<T> {
	
	private final Class<T> enumClass;
	
	private NoCounters(Class<T> enumClass) {
		this.enumClass = enumClass;
	}
	
	static <T extends Enum<T>> NoCounters<T> of (Class<T> enumClass ) {
		return new NoCounters<>(enumClass);
	}
	
	private Map<T, Long> asMap() {
		Map<T, Long> result = new EnumMap<>(enumClass);
		enums().forEach( t -> result.put(t, 0L));
		return result;
	}
	
	@Override
	public NoCounters<T>  increment(T key) {
		return this;
	}

	@Override
	public NoCounters<T> add(T key, long delta) {
		return this;
	}
	
	@Override
	public NoCounters<T> max(T key, long challenge) {
		return this;
	}
	
	@Override
	public Counts counts() {
		return new DefaultCounts<> (enumClass, asMap(), 0L);
	}
	
	private Stream<T> enums() {
		return Arrays.stream(enumClass.getEnumConstants());
	}
	
}
