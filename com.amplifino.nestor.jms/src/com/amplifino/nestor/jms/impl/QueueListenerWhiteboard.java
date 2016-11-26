package com.amplifino.nestor.jms.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;

import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.amplifino.nestor.jms.SessionAwareMessageListener;

@Component
public class QueueListenerWhiteboard {

	@Reference(target="(application=whiteboard.queue)")
	private Connection connection;
	
	private final Map<ComponentServiceObjects<? extends MessageListener>, List<Map.Entry<Session, MessageConsumer>>> consumers = new ConcurrentHashMap<>();
	private final Logger logger = Logger.getLogger("com.amplifino.nestor.jms");
	
	@Reference(name="zListener", cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC, target="(queue=*)")
	public void addListener(ComponentServiceObjects<MessageListener> listenerFactory) throws JMSException {
		doAddListener(listenerFactory, false);	
	}
	
	@Reference(name="zSessionAware", cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC, target="(queue=*)")
	public void addSessionAwareListener(ComponentServiceObjects<SessionAwareMessageListener> listenerFactory) throws JMSException {
		doAddListener(listenerFactory, true);
	}
	
	private void doAddListener(ComponentServiceObjects<? extends MessageListener> listenerFactory, boolean sessionAware) throws JMSException {
		String queueName = queueName(listenerFactory);
		String messageSelector = messageSelector(listenerFactory);
		int threadCount = threadCount(listenerFactory);
		List<Map.Entry<Session, MessageConsumer>> listeners = new ArrayList<>(threadCount);
		for (int i = 0 ; i < threadCount; i++) {	
			Session session = connection.createSession(false, acknowledgeMode(listenerFactory));
			MessageListener listener = listenerFactory.getService();
			if (sessionAware) {
				((SessionAwareMessageListener) listener).init(session);
			}
			Queue queue = session.createQueue(queueName);
			MessageConsumer consumer = session.createConsumer(queue, messageSelector);
			consumer.setMessageListener(listener);
			listeners.add(new AbstractMap.SimpleImmutableEntry<>(session, consumer));
		}
		consumers.put(listenerFactory, listeners);
		String logMsg = "Activated " + threadCount + " message listener(s) on queue " + queueName;
		if (messageSelector != null) {
			logMsg = logMsg + " with message selector " + messageSelector;
		}
		logger.info(logMsg);
	}
	
	public void removeListener(ComponentServiceObjects<MessageListener> listenerFactory) {
		doRemoveListener(listenerFactory);
	}
	
	public void removeSessionAwareListener(ComponentServiceObjects<SessionAwareMessageListener> listenerFactory) {
		doRemoveListener(listenerFactory);
	}
	
	public void doRemoveListener(ComponentServiceObjects<? extends MessageListener> listenerFactory) {
		String queueName = queueName(listenerFactory);
		List<Map.Entry<Session, MessageConsumer>> listeners = consumers.remove(listenerFactory);
		if (listeners == null) {
			logger.info("No match found for queue listener on " + queueName);
		} else {
			logger.info("About to deactivate " + listeners.size() + " listener(s) on queue " +  queueName);
			listeners.forEach( entry -> close(entry.getKey(), entry.getValue()));
		}
	}
	
	private void close(Session session, MessageConsumer consumer) {
		try {
			try {
				consumer.close();
			} finally {
				session.close();
			}
		} catch (Exception e) {
			Logger.getLogger("eu.vandevelde.activemq").log(Level.WARNING, "Error releasing consumer: " + e, e);
		}
	}
	
	private String queueName(ComponentServiceObjects<?> listenerFactory) {
		return (String) listenerFactory.getServiceReference().getProperty("queue");
	}
	
	private String messageSelector(ComponentServiceObjects<?> listenerFactory) {
		return (String) listenerFactory.getServiceReference().getProperty("messageSelector");
	}
	
	private int threadCount(ComponentServiceObjects<?> listenerFactory) {
		return Optional.ofNullable((Integer) listenerFactory.getServiceReference().getProperty("threadCount")).orElse(1); 
	}
	
	private int acknowledgeMode(ComponentServiceObjects<?> listenerFactory) {
		return Optional.ofNullable((Integer) listenerFactory.getServiceReference().getProperty("acknowledgeMode")).orElse(Session.AUTO_ACKNOWLEDGE);
	}

}
