package com.amplifino.jetty;

import java.io.IOException;
import java.nio.channels.Selector;

import org.junit.Test;

public class SelectorTest {

	@Test
	public void test() throws IOException {
		Selector selector = Selector.open();
		selector.select();
	}
}
