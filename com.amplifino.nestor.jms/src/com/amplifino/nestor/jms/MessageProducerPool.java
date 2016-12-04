package com.amplifino.nestor.jms;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface MessageProducerPool {

	MessageProducerLease lease();
	void send(MessageSend messageSend);
}
