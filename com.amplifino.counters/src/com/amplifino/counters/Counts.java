package com.amplifino.counters;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface Counts {
	
	Map<Enum<?>, Long> asMap();
	long get(Enum<?> key);
	long elapsed(TimeUnit unit);
	Counts delta(Counts snapshot);
	Counts delta(Counts snapshot, Consumer<Counts> consumer);
	void print();
	void print(PrintStream stream);
	void print(String header);
	void print(String header, PrintStream stream);

}
