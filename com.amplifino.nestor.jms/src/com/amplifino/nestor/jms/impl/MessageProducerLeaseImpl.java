package com.amplifino.nestor.jms.impl;

import javax.jms.MessageProducer;
import javax.jms.Session;

import com.amplifino.nestor.jms.MessageProducerLease;
import com.amplifino.pools.Pool;

public class MessageProducerLeaseImpl implements MessageProducerLease {

	private final Pool<MessageProducerPair> pool;
	private final MessageProducerPair pair;
	
	public MessageProducerLeaseImpl(Pool<MessageProducerPair> pool, MessageProducerPair pair) {
		this.pool = pool;
		this.pair = pair;
	}
	
	@Override
	public Session session() {
		return pair.session();
	}
	@Override
	public MessageProducer producer() {
		return pair.producer();
	}
	
	@Override
	public void close() {
		pool.release(pair);		
	}
}
