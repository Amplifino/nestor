package com.amplifino.nestor.useradmin;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.service.useradmin.UserAdminEvent;
import org.osgi.service.useradmin.UserAdminListener;
 
class EventPublisher {
	private final static Logger LOGGER = Logger.getLogger("com.amplifino.useradmin");
	private final ThreadGroup threadGroup;
	private final Thread thread;
	private final BlockingQueue<EventAction> queue;
	
	EventPublisher(int capacity) {
		queue = new ArrayBlockingQueue<>(100);
		threadGroup = new ThreadGroup("User Admin");
		thread = new Thread(threadGroup, new Runner(), "User Admin Event Publisher");
		thread.start();
	}
	
	void publish(UserAdminEvent event, Iterator<UserAdminListener> iterator) {
		try {
			queue.put(new EventAction(event, iterator));
		} catch (InterruptedException ex) {
			// restore interrupt flag			
			LOGGER.severe("Thread interrupted while trying to queue user admin event for role " + event.getRole());
			Thread.currentThread().interrupt();
		}
	}
	
	void deactivate() {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException ex) {
			LOGGER.severe("Declarative Service Thread interrupted in deactivate call");
			return;
		}
		threadGroup.destroy();
	}
	
	private class Runner implements Runnable {

		@Override
		public void run() {
			while(!Thread.interrupted()) {
				try {
					EventAction action = queue.take();
					action.action();
				} catch (InterruptedException ex) {
					// restore interrupt flag
					Thread.currentThread().interrupt();
				}
			}		
		}
		
	}
	
	private static class EventAction {
		private final UserAdminEvent event;
		private final Iterator<UserAdminListener> iterator;
		
		EventAction(UserAdminEvent event, Iterator<UserAdminListener> iterator) {
			this.event = event;
			this.iterator = iterator;
		}
		
		void action() {
			while (iterator.hasNext()) {
				UserAdminListener listener = iterator.next();
				try { 
					listener.roleChanged(event);
				} catch (Throwable ex) {
					LOGGER.log(Level.SEVERE, "Exception in User Admin Event Listener " + listener + " : " + ex.getMessage(), ex);
				}
			}
		}
	}
}
