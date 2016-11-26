package com.amplifino.nestor.jms;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface SessionAwareMessageListener extends MessageListener {

	void init(Session session) throws JMSException;
}
