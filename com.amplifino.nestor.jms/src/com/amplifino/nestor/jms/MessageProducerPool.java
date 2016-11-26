package com.amplifino.nestor.jms;

public interface MessageProducerPool {

	MessageProducerLease lease();
	void send(MessageSend messageSend);
}
