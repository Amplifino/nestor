package com.amplifino.counters;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Counts represents a snapshot of a Counters instance.
 *
 */
/**
 * @author kha
 *
 */
/**
 * @author kha
 *
 */
public interface Counts {
	
	/**
	 * @return a map 
	 */
	Map<Enum<?>, Long> asMap();
	/**
	 * get the count for the given key
	 * @param key
	 * @return
	 */
	long get(Enum<?> key);
	/**
	 * return the time snapshot was taken (since counting started).
	 * @param unit 
	 * @return
	 */
	long elapsed(TimeUnit unit);
	/**
	 * takes the delta with a previous snapshot 
	 * @param snapshot
	 * @return
	 */
	Counts delta(Counts snapshot);
	/**
	 * takes the delta with a previous snapshot and process it by the second argument. Returns this
	 * Typical use case is to process the delta and keep the new snapshot as baseline:
	 *  At startup:
	 *  <pre>snapshot = conutssupplier.counts();</pre>
	 *  After each step or iteration: 
	 *  <pre>snapshot = countssupplier.counts().delta(snapshot, Counts::print);</pre>
	 * @param snapshot
	 * @param consumer
	 * @return this
	 */
	Counts delta(Counts snapshot, Consumer<Counts> consumer);
	/**
	 * print counters to System.out 
	 */
	void print();
	/**
	 * print counters
	 * @param stream
	 */
	void print(PrintStream stream);
	/**
	 * print counters to System.out using header
	 * @param header
	 */
	void print(String header);
	/**
	 * print counters using the header
	 * @param header
	 * @param stream
	 */
	void print(String header, PrintStream stream);

}
