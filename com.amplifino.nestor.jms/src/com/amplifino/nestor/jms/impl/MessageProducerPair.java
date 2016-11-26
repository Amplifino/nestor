package com.amplifino.nestor.jms.impl;

import java.util.Optional;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import com.amplifino.nestor.jms.UncheckedJMSException;

class MessageProducerPair {

	private final Session session;
	private final MessageProducer producer;
	
	MessageProducerPair(Connection connection, Optional<Destination> destination) {
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = initProducer(destination);
		} catch (JMSException e) {
			throw new UncheckedJMSException(e);
		} 
	}
	
	private MessageProducer initProducer(Optional<Destination> destination) throws JMSException {
		MessageProducer result = null;
		try {
			result = session.createProducer(destination.orElse(null));
			return result;
		} finally {
			if (result == null) {
				session.close();
			}
		}
	}
	
	Session session() {
		return session;
	}
	
	MessageProducer producer() {
		return producer;
	}
	
	void close() {
		try {
			try {
				producer.close();
			} finally {
				session.close();
			}
		} catch (JMSException e) {
			throw new UncheckedJMSException(e);
		}
	}

	
}
