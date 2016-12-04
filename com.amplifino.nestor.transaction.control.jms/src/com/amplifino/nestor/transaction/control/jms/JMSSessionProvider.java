package com.amplifino.nestor.transaction.control.jms;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XASession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.transaction.control.ResourceProvider;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;

import com.amplifino.pools.Pool;

@Component
public class JMSSessionProvider implements ResourceProvider<Session> {

	@Reference
	private XAConnection connection;
	
	@Override
	public Session getResource(TransactionControl transactionControl) throws TransactionException {
		return new XASessionProxy(transactionControl, createPool());
	}
	
	private Pool<XASession> createPool() {
		return Pool.builder(this::allocate)
			.destroy(this::destroy)
			.build();
	}
	
	private XASession allocate() {
		try {
			return connection.createXASession();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void destroy(XASession xaSession) {
		try {
			xaSession.close();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

}
