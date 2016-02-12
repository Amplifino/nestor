package com.amplifino.nestor.logging.test;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;

import com.amplifino.nestor.logging.LogBridge;

public class LogForwardingTest {

	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
	private final CountDownLatch latch = new CountDownLatch(1);
	private final Logger logger = Logger.getLogger(getClass().getPackage().getName());
	private LogBridge bridge;
	
	@Before
	public void setup() throws InterruptedException {
		// register log listener
		getService(LogReaderService.class).addLogListener(this::logged);
		// wait for log bridge initialization
		bridge = getService(LogBridge.class);
	}
	
	@Test
	public void testInfo() throws InterruptedException {
		logger.info("A log message");
		Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
	}
	
	@Test
	public void testFiner() throws InterruptedException {
		logger.finer("A finer log message");
		Assert.assertFalse(latch.await(1, TimeUnit.SECONDS));
		bridge.setLevel(Level.FINER);
		logger.setLevel(Level.FINER);
		logger.finer("A finer log message");
		Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
	}
	
	@Test
	public void testConfiguration() throws InterruptedException, IOException {
		ConfigurationAdmin configurationAdmin = getService(ConfigurationAdmin.class);
		Configuration configuration = configurationAdmin.getConfiguration("com.amplifino.nestor.logging", "?");
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("level", Level.WARNING.getName());
		configuration.update(props);
		// wait for logging bridge component to restart, as this happens asynchronous
		Thread.sleep(1000L);
		logger.info("An info message");
		Assert.assertFalse(latch.await(1, TimeUnit.SECONDS));
		logger.warning("A warning message");
		Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));		
	}
	
	public void logged(LogEntry entry) {
		if (entry.getMessage().contains(getClass().getPackage().getName())) {
			latch.countDown();
		}
	}
	
	private <T> T getService(Class<T> clazz) {
		ServiceTracker<T, T> tracker = new ServiceTracker<>(context, clazz, null);
		tracker.open();
		try {
			return Objects.requireNonNull(tracker.waitForService(1000L));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
