package com.amplifino.nestor.apps.events;

import java.util.logging.Logger;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component
public class LogNoise {

	private Thread thread;
	private Logger logger = Logger.getLogger("com.amplifino.nestor.apps.events");
	
	@Activate 
	public void activate() {
		thread = new Thread(this::run);
		thread.start();
	}
	
	@Deactivate
	public void deactivate() {
		thread.interrupt();
	}
	
	private void run() {
		int i = 1;
		while(!Thread.interrupted()) {
			logger.info("Message " + i++);
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
}
