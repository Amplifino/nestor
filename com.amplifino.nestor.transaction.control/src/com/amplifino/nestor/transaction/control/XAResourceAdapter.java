package com.amplifino.nestor.transaction.control;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.osgi.service.transaction.control.LocalResource;
import org.osgi.service.transaction.control.TransactionException;

import com.amplifino.nestor.transaction.provider.xa.spi.XAResourceKind;

abstract class XAResourceAdapter implements XAResource, XAResourceKind {

	final LocalResource resource;
	
	XAResourceAdapter(LocalResource resource) {
		this.resource = resource;
	}

	abstract void commitPhaseTwo() throws XAException;
	
	@Override
	public final void commit(Xid xid, boolean onePhase) throws XAException {
		if (onePhase) {
			resource.commit();
		} else {
			commitPhaseTwo();
		}			
	}

	@Override
	public void end(Xid xid, int flags) throws XAException {			
	}

	@Override
	public void forget(Xid xid) throws XAException {			
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	@Override
	public boolean isSameRM(XAResource xares) throws XAException {
		return false;
	}

	@Override
	public Xid[] recover(int flag) throws XAException {
		throw new TransactionException("Recover not supported for local resource " + resource);
	}

	@Override
	public void rollback(Xid xid) throws XAException {
		resource.rollback();
	}

	@Override
	public boolean setTransactionTimeout(int seconds) throws XAException {
		return false;
	}

	@Override
	public void start(Xid xid, int flags) throws XAException {			
	}	
	
}
