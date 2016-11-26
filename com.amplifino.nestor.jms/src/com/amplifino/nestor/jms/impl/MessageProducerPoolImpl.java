package com.amplifino.nestor.jms.impl;

import java.util.Optional;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import com.amplifino.nestor.jms.MessageProducerLease;
import com.amplifino.nestor.jms.MessageProducerPool;
import com.amplifino.nestor.jms.MessageSend;
import com.amplifino.nestor.jms.UncheckedJMSException;
import com.amplifino.pools.Pool;

@Component
@Designate(ocd=MessageProducerPoolConfiguration.class, factory=true)
public class MessageProducerPoolImpl implements MessageProducerPool {
	
	@Reference
	private Connection connection;
	private Pool<MessageProducerPair> pool;
	private Optional<Destination> destination;
	
	@Activate
	public void activate(MessageProducerPoolConfiguration config) throws JMSException {
		if (config.destinationType() == DestinationType.NONE) {
			destination = Optional.empty();
		} else {
			destination = Optional.of(config.destinationType().destination(connection,  config.destination()));
		}		
		Pool.Builder<MessageProducerPair> builder = Pool.builder(this::allocate)
				.destroy(MessageProducerPair::close)
				.name("Message Producer Pool");
		if (config.maxIdle() > 0) {
			builder.maxIdle(config.maxIdle());
		}
		if (config.maxSize() > 0) {
			builder.maxSize(config.maxSize()); 
		}
		pool = builder.build();
	}
	
	@Deactivate
	public void deactivate() {
		pool.close();
	}

	@Override
	public MessageProducerLease lease() {
		return new MessageProducerLeaseImpl(pool, pool.borrow());
	}

	@Override
	public void send(MessageSend consumer) {
		try (MessageProducerLease lease = lease()) {
			consumer.send(lease.session(), lease.producer());
		} catch (JMSException e) {
			throw new UncheckedJMSException(e);
		}
	}
	
	private MessageProducerPair allocate() {
		return new MessageProducerPair(connection, destination);
	}
	

}
