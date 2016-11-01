package com.amplifino.transaction.test;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public abstract class XaResourceWrapper implements XAResource {

	private final XAResource resource;
	
	public XaResourceWrapper(XAResource resource) {
		this.resource = resource;
	}
	
	@Override
	public void commit(Xid arg0, boolean arg1) throws XAException {
		resource.commit(arg0, arg1);
	}

	@Override
	public void end(Xid arg0, int arg1) throws XAException {
		resource.end(arg0,  arg1);
		
	}

	@Override
	public void forget(Xid arg0) throws XAException {
		resource.forget(arg0);
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return resource.getTransactionTimeout();
	}

	@Override
	public boolean isSameRM(XAResource arg0) throws XAException {
		return resource.isSameRM(arg0);
	}

	@Override
	public int prepare(Xid arg0) throws XAException {
		return resource.prepare(arg0);
	}

	@Override
	public Xid[] recover(int arg0) throws XAException {
		return resource.recover(arg0);
	}

	@Override
	public void rollback(Xid arg0) throws XAException {
		resource.rollback(arg0);
	}

	@Override
	public boolean setTransactionTimeout(int arg0) throws XAException {
		return resource.setTransactionTimeout(arg0);
	}

	@Override
	public void start(Xid arg0, int arg1) throws XAException {
		resource.start(arg0,  arg1);
	}

	
}
