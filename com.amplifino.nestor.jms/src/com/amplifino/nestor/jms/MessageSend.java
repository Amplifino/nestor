package com.amplifino.nestor.jms;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.osgi.annotation.versioning.ConsumerType;

@FunctionalInterface
@ConsumerType
public interface MessageSend {
	
	void send(Session session, MessageProducer producer) throws JMSException;

}
