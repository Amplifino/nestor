package com.amplifino.nestor.jms.impl;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferenceScope;

import com.amplifino.nestor.jms.SessionAwareMessageListener;

@Component
public class TopicSubscriberWhiteboard {

	@Reference(target="(application=whiteboard.topic)")
	private Connection connection;
	
	private final Map<MessageListener, Map.Entry<Session, TopicSubscriber>> subscriptions = new ConcurrentHashMap<>();
	private final Logger logger = Logger.getLogger("com.amplifino.nestor.jms");
	
	@Reference(name="zListener", cardinality=ReferenceCardinality.MULTIPLE, scope=ReferenceScope.PROTOTYPE, 
			policy=ReferencePolicy.DYNAMIC, target="(&(topic=*)(subscriberName=*))")
	public void addListener(MessageListener listener, Map<String, Object> properties) throws JMSException {
		doAddListener(listener, properties, false);
	}
	
	@Reference(name="zSessionAware", cardinality=ReferenceCardinality.MULTIPLE, scope=ReferenceScope.PROTOTYPE,
			policy=ReferencePolicy.DYNAMIC, target="(&(topic=*)(subscriberName=*))")
	public void addSessionAwareListener(SessionAwareMessageListener listener, Map<String, Object> properties) throws JMSException {
		doAddListener(listener, properties, true);
	}
	
	private void doAddListener(MessageListener listener, Map<String, Object> properties, boolean sessionAware) throws JMSException {
		Session session = connection.createSession(false, acknowledgeMode(properties));
		if (sessionAware) {
			((SessionAwareMessageListener) listener).init(session);
		}
		String topicName = topicName(properties);
		Topic topic = session.createTopic(topicName);
		String subscriberName = subscriberName(properties);
		String messageSelector = messageSelector(properties);
		TopicSubscriber subscriber = session.createDurableSubscriber(topic, subscriberName, messageSelector, false);
		subscriber.setMessageListener(listener);
		subscriptions.put(listener, new AbstractMap.SimpleImmutableEntry<>(session, subscriber));
		String logMsg = "Activated topic subscriber " + subscriberName + " on topic " + topicName;
		if (messageSelector != null) {
			logMsg = logMsg + " with message selector " + messageSelector;
		}
		logger.info(logMsg);
	}
	
	public void removeListener(MessageListener listener, Map<String, Object> properties) {
		doRemoveListener(listener, properties);
	}
	
	public void removeSessionAwareListener(MessageListener listener, Map<String, Object> properties) {
		doRemoveListener(listener, properties);
	}
	
	public void doRemoveListener(MessageListener listener, Map<String, Object> properties) {
		String topicName = topicName(properties);
		String subscriberName = subscriberName(properties);
		Map.Entry<Session, TopicSubscriber> subscription = subscriptions.remove(listener);
		if (subscription == null) {
			logger.info("No match found for subscriber " + subscriberName + " on topic " + topicName);
		} else {
			logger.info("About to deactivate subscriber " + subscriberName + " on topic " + topicName);
			close(subscription.getKey(), subscription.getValue());
		}
	}
	
	private void close(Session session, TopicSubscriber subscriber) {
		try {
			try {
				subscriber.close();
			} finally {
				session.close();
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error releasing subscription: " + e, e);
		}
	}
	
	private String topicName(Map<String, Object> properties) {
		return (String) properties.get("topic");
	}
	
	private String subscriberName(Map<String, Object> properties) {
		return (String) properties.get("subscriberName");
	}
	
	private String messageSelector(Map<String, Object> properties) {
		return (String) properties.get("messageSelector");
	}
	
	private int acknowledgeMode(Map<String, Object> properties) {
		return Optional.ofNullable((Integer) properties.get("acknowledgeMode")).orElse(Session.AUTO_ACKNOWLEDGE);
	}
}
