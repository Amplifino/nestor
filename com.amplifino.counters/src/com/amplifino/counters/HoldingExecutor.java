package com.amplifino.counters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class HoldingExecutor implements Executor {
	private List<Runnable> runnables = new ArrayList<>();
	
	public synchronized void execute(Runnable runnable) {
		if (runnables == null) {
			runnable.run();
		} else {
			runnables.add(runnable);
		}
	}
	
	public synchronized void start() {
		runnables.forEach(Runnable::run);
		runnables = null;
	}
	
}   