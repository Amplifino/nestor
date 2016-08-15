package com.amplifino.pools;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ScheduledTaskTester {

	private volatile boolean stopped = false;
	
	@Test
	public void test() {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		ScheduledFuture<?> future = service.scheduleAtFixedRate(this::run, 1, 1 , TimeUnit.SECONDS);
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
		}
		future.cancel(false);
		stopped = true;
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
		}
	}
	
	private void run() {
		System.out.println("Stopped: " + stopped);
	}
}
