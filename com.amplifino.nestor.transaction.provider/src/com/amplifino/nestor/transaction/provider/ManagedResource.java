package com.amplifino.nestor.transaction.provider;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class ManagedResource {

	private final XAResource resource;
	private boolean started;
	
	public ManagedResource(XAResource resource) {
		this.resource = resource;
		started = false;
	}
	
	XAResource resource() {
		return resource;
	}
	
	void start(Xid xid, int flags) throws XAException {
		if (!started) {
			resource.start(xid, flags);
			started = true;
		}
	}
	
	boolean end(Xid xid, int flags) throws XAException {
		if (started) {
			resource.end(xid, flags);
			started = false;
			return true;
		} else {
			return false;
		}
	}
	
}

