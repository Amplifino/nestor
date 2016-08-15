package com.amplifino.counters;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

final class DefaultCounters<T extends Enum<T>> implements Counters<T> {
	
	private final Class<T> enumClass;
	private final AtomicLong[] values;
	private final long epoch = System.nanoTime();

	private DefaultCounters(Class<T> enumClass) {
		this.enumClass = enumClass;
		this.values = allocate(enumClass.getEnumConstants().length);
	}
	
	private static AtomicLong[] allocate(int length) {
		return Stream.generate(AtomicLong::new)
			.limit(length)
			.toArray(AtomicLong[]::new);
	}
	
	static <T extends Enum<T>> DefaultCounters<T> of (Class<T> enumClass ) {
		return new DefaultCounters<>(enumClass);
	}
	
	private Map<T, Long> asMap() {
		Map<T, Long> result = new EnumMap<>(enumClass);
		enums().forEach( t -> result.put(t, values[t.ordinal()].get()));
		return result;
	}
	
	@Override
	public DefaultCounters<T>  increment(T key) {
		return add(key, 1);
	}

	@Override
	public DefaultCounters<T> add(T key, long delta) {
		values[key.ordinal()].addAndGet(delta);
		return this;
	}
	
	@Override
	public DefaultCounters<T> max(T key, long challenge) {
		values[key.ordinal()].accumulateAndGet(challenge, Long::max);
		return this;
	}
	
	@Override
	public Counts counts() {
		long nanos = System.nanoTime() - epoch;
		return new DefaultCounts<> (enumClass, asMap(), nanos);
	}
	
	private Stream<T> enums() {
		return Arrays.stream(enumClass.getEnumConstants());
	}
	
}
