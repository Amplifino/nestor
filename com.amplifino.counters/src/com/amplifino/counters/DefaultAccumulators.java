package com.amplifino.counters;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.Stream;

final class DefaultAccumulators<T extends Enum<T>> implements Accumulators<T> {
	
	private final Class<T> enumClass;
	private final LongAccumulator[] values;
	private final long epoch = System.nanoTime();

	private DefaultAccumulators(Class<T> enumClass) {
		this.enumClass = enumClass;
		this.values = allocate();
	}
	
	private LongAccumulator[] allocate() {
		return enums()
			.map(this::createAccumulator)
			.toArray(LongAccumulator[]::new);
	}
	
	private LongAccumulator createAccumulator (T enumConstant) {
		if (enumConstant.name().toUpperCase().contains("MAX")) {
			return new LongAccumulator(Long::max, 0);
		} else {
			return new LongAccumulator(Long::sum, 0);
		}
	}
	
	static <T extends Enum<T>> DefaultAccumulators<T> of (Class<T> enumClass ) {
		return new DefaultAccumulators<>(enumClass);
	}
	
	private Map<T, Long> asMap() {
		Map<T, Long> result = new EnumMap<>(enumClass);
		enums().forEach( t -> result.put(t, values[t.ordinal()].get()));
		return result;
	}
	
	@Override
	public DefaultAccumulators<T> accumulate(T key, long value) {
		values[key.ordinal()].accumulate(value);
		return this;
	}
	
	@Override
	public DefaultAccumulators<T> increment(T key) {
		return accumulate(key, 1);
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
