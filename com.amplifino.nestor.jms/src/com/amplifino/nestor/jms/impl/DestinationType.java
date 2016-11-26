package com.amplifino.nestor.jms.impl;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

public enum DestinationType {
	
	NONE {
		@Override
		Destination destination(Session session, String name) throws JMSException {
			return null;
		}
	},
	QUEUE {
		@Override
		Destination destination(Session session, String name) throws JMSException {
			return session.createQueue(name);
		}
	},
	TOPIC {
		@Override
		Destination destination(Session session, String name) throws JMSException {
			return session.createTopic(name);
		}
	};

	final Destination destination(Connection connection, String name) throws JMSException {
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		try {
			return destination(session, name);
		} finally {
			session.close();
		}
	}
	
	abstract Destination destination(Session session, String name) throws JMSException;
}
