package com.amplifino.counters;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Counts represents a snapshot of a Counters instance.
 *
 */
@ProviderType
public interface Counts {
	
	/**
	 * @return a map 
	 */
	Map<Enum<?>, Long> asMap();
	/**
	 * returns the current count for the given key
	 * @param key the given key
	 * @return the count
	 */
	long get(Enum<?> key);
	/**
	 * return the time snapshot was taken (since counting started).
	 * @param unit time unit
	 * @return the elapsed time
	 */
	long elapsed(TimeUnit unit);
	/**
	 * takes the delta with a previous snapshot 
	 * @param snapshot previous snapshot
	 * @return the delta
	 */
	Counts delta(Counts snapshot);
	/**
	 * takes the delta with a previous snapshot and process it by the second argument. Returns this
	 * Typical use case is to process the delta and keep the new snapshot as baseline:
	 *  At startup:
	 *  <pre>snapshot = conutssupplier.counts();</pre>
	 *  After each step or iteration: 
	 *  <pre>snapshot = countssupplier.counts().delta(snapshot, Counts::print);</pre>
	 * @param snapshot the previous delta
	 * @param consumer to process delta
	 * @return this
	 */
	Counts delta(Counts snapshot, Consumer<Counts> consumer);
	/**
	 * print counters to System.out 
	 */
	void print();
	/**
	 * print counters
	 * @param stream the output stream
	 */
	void print(PrintStream stream);
	/**
	 * print counters to System.out using header
	 * @param header the leading header
	 */
	void print(String header);
	/**
	 * print counters using the header
	 * @param header the leading header 
	 * @param stream the output stream
	 */
	void print(String header, PrintStream stream);

}
