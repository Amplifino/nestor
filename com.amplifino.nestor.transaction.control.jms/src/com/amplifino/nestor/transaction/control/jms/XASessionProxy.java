package com.amplifino.nestor.transaction.control.jms;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XASession;

import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;

import com.amplifino.pools.Pool;

class XASessionProxy extends SessionProxy {
	
	private final Pool<XASession> pool;
	private final TransactionControl transactionControl;
	
	public XASessionProxy(TransactionControl transactionControl, Pool<XASession> pool) {
		this.transactionControl = transactionControl;
		this.pool = pool;
		// TODO Auto-generated constructor stub
	}
	

	@Override
	Session session() throws JMSException {
		  TransactionContext context = transactionControl.getCurrentContext();
		  if (context == null) {
    		throw new TransactionException("No active scope");
		  }
		  Session session = (Session) context.getScopedValue(this);
		  return session == null ? newSession() : session;
	}
		    
	Session newSession() throws JMSException {
		XASession xaSession = pool.borrow();
    	transactionControl.getCurrentContext().postCompletion(status -> this.close(xaSession));
    	if (transactionControl.activeTransaction()) {
    		transactionControl.getCurrentContext().registerXAResource(xaSession.getXAResource(), null);
    	}
    	return xaSession;
    }
	
	private void close(XASession xaSession) {
    	pool.release(xaSession);
    }
	
}	

