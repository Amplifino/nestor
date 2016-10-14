package com.amplifino.jetty;

import java.io.IOException;
import java.nio.channels.Selector;
import java.time.Instant;

public class SelectTester {

	private void go() throws IOException {
		Selector  selector = Selector.open();
		System.out.println("Started select at " + Instant.now());
		int count = selector.select();
		System.out.println("Selected " + count + " at " + Instant.now());
		count = selector.select();
		System.out.println("Selected " + count + " at " + Instant.now());
		count = selector.select();
		System.out.println("Selected " + count + " at " + Instant.now());
	}
	public static void main(String[] args) {
		try {
			new SelectTester().go();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
