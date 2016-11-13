package com.amplifino.nestor.transaction.control;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.osgi.service.transaction.control.LocalResource;
import org.osgi.service.transaction.control.TransactionException;

public class AcidXAResourceAdapter extends XAResourceAdapter {
	
	public AcidXAResourceAdapter(LocalResource resource) {
		super(resource);
	}

	@Override
	public int prepare(Xid arg0) throws XAException {
		throw new TransactionException("Prepare not supported for local resource " + resource);
	}

	@Override
	public Kind kind() {
		return XAResourceAdapter.Kind.ONEPHASE;
	}

	@Override
	void commitPhaseTwo() throws XAException {
		throw new TransactionException("Two phase commit not supported for local resource " + resource);
	}

}
