package com.amplifino.nestor.transaction.datasources;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

class NotSameRMXAResourceWrapper implements XAResource {

	private final XAResource resource;
	
	public NotSameRMXAResourceWrapper(XAResource resource) {
		this.resource = resource;
	}
	
	@Override
	public void commit(Xid xid, boolean onePhase) throws XAException {
		resource.commit(xid, onePhase);
	}

	@Override
	public void end(Xid xid, int flags) throws XAException {
		resource.end(xid, flags);

	}

	@Override
	public void forget(Xid xid) throws XAException {
		resource.forget(xid);
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return resource.getTransactionTimeout();
	}

	@Override
	public boolean isSameRM(XAResource other) throws XAException {
		return other == this || other == resource;
	}

	@Override
	public int prepare(Xid xid) throws XAException {
		return resource.prepare(xid);
	}

	@Override
	public Xid[] recover(int flags) throws XAException {
		return resource.recover(flags);
	}

	@Override
	public void rollback(Xid xid) throws XAException {
		resource.rollback(xid);
	}

	@Override
	public boolean setTransactionTimeout(int seconds) throws XAException {
		return resource.setTransactionTimeout(seconds);
	}

	@Override
	public void start(Xid xid, int flags) throws XAException {
		resource.start(xid, flags);
	}

}
