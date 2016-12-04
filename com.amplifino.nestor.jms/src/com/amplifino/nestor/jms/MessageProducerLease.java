package com.amplifino.nestor.jms;

import javax.jms.MessageProducer;
import javax.jms.Session;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface MessageProducerLease extends AutoCloseable {
	
	Session session();
	MessageProducer producer();
	@Override
	void close();

}
