package com.amplifino.counters;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

class DefaultCounts<T extends Enum<T>> implements Counts {
	
	private final Class<T> enumClass;
	private final Map<T, Long> map;
	private long elapsed;
	
	DefaultCounts (Class<T> enumClass, Map<T, Long> map, long elapsed) {
		this.enumClass = enumClass;
		this.map = map;
		this.elapsed = elapsed;
	}
	
	private final Stream<T> enums() {
		return Arrays.stream(enumClass.getEnumConstants());
	}
	
	@Override
	public long get(Enum<?> key) {
		// avoid NPE with autoboxing
		return Optional.ofNullable(map.get(key)).orElse(0L);
	}

	@Override
	public long elapsed(TimeUnit unit) {
		return unit.convert(elapsed, TimeUnit.NANOSECONDS);
	}

	@Override
	public Counts delta(Counts snapshot) {
		Map<T, Long> deltas = new EnumMap<>(enumClass);
		enums().forEach(t -> deltas.put(t, get(t) - snapshot.get(t)));
		return new DefaultCounts<>(enumClass, deltas, elapsed - snapshot.elapsed(TimeUnit.NANOSECONDS));
	}
	
	@Override
	public Counts delta(Counts snapshot, Consumer<Counts> consumer) {
		consumer.accept(delta(snapshot));
		return this;
	}
	
	@Override
	public void print() {
		print(System.out);
	}
	
	@Override
	public void print(String header) {
		print(header, System.out);
	}
	
	@Override
	public void print(PrintStream stream) {
		print(LocalDateTime.now().toString(), stream);
	}
	
	@Override
	public void print(String header, PrintStream stream) {
		stream.print(header + " elapsed: " + elapsedString(elapsed));
		enums().forEach(t -> print(stream,t));
		stream.println();
	}
	
	@Override
	public Map<Enum<?>, Long> asMap() {
		return Collections.unmodifiableMap(map);
	}
	
	private void print(PrintStream stream, T t) {
		stream.print(" " + t + ": " + format(get(t)));
	}
	
	/* copied from Stopwatch */
	
	private static String elapsedString(long nanos) {
	    TimeUnit unit = chooseUnit(nanos);
	    double value = (double) nanos / NANOSECONDS.convert(1, unit);
	    return String.format("%.4g %s", value, abbreviate(unit));
	  }

	private static TimeUnit chooseUnit(long nanos) {
	    if (DAYS.convert(nanos, NANOSECONDS) > 0) {
	      return DAYS;
	    }
	    if (HOURS.convert(nanos, NANOSECONDS) > 0) {
	      return HOURS;
	    }
	    if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
	      return MINUTES;
	    }
	    if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
	      return SECONDS;
	    }
	    if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
	      return MILLISECONDS;
	    }
	    if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
	      return MICROSECONDS;
	    }
	    return NANOSECONDS;
	  }

	 private static String abbreviate(TimeUnit unit) {
	    switch (unit) {
	      case NANOSECONDS:
	        return "ns";
	      case MICROSECONDS:
	        return "\u03bcs"; 
	      case MILLISECONDS:
	        return "ms";
	      case SECONDS:
	        return "s";
	      case MINUTES:
	        return "min";
	      case HOURS:
	        return "h";
	      case DAYS:
	        return "d";
	      default:
	        throw new AssertionError();
	    }
	  }

	 static String format(long counter) {
		 if (counter >= (1L << 50 )) {
			return "" + (counter >> 40) + "T";
		 } else if (counter >= (1L << 40)) {
			 return "" + (counter >> 30) + "G";	
		 } else if (counter >= (1L << 30)) {
			 return "" + (counter >> 20) + "M";
		 } else if (counter >= (1L << 20)) {
			 return "" + (counter >> 10) + "K";
		 } else {
			 return "" + counter;
		 }
	 }
		 
}