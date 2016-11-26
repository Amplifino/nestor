package com.amplifino.nestor.jms;

import javax.jms.MessageProducer;
import javax.jms.Session;

public interface MessageProducerLease extends AutoCloseable {
	
	Session session();
	MessageProducer producer();
	@Override
	void close();

}
