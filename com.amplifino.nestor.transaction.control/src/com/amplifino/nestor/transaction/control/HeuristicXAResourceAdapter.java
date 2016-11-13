package com.amplifino.nestor.transaction.control;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.osgi.service.transaction.control.LocalResource;

import com.amplifino.nestor.transaction.provider.xa.spi.XAResourceKind;

public class HeuristicXAResourceAdapter extends XAResourceAdapter {
	
	private boolean committed = false;
	private final XAResourceKind.Kind kind;
	
	public HeuristicXAResourceAdapter(LocalResource resource, XAResourceKind.Kind kind) {
		super(resource);
		this.kind = kind;
	}

	@Override
	public int prepare(Xid arg0) throws XAException {
		resource.commit();
		committed = true;
		return XAResource.XA_OK;
	}

	@Override
	public Kind kind() {
		return kind;
	}

	@Override
	void commitPhaseTwo() throws XAException {
		// no operation as we committed on prepare;
		if (!committed) {
			throw new IllegalStateException();
		}
	}
	
	@Override
	public void rollback(Xid xid) throws XAException {
		if (committed) {
			throw new XAException(XAException.XA_HEURCOM);
		} else {
			super.rollback(xid);
		}
	}

}
