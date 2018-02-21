package com.amplifino.nestor.transaction.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.amplifino.nestor.transaction.provider.spi.TransactionLog;

class TransactionBranch {
	
	private final List<ManagedResource> resources = new ArrayList<>();
	private final Xid xid;
	private PrepareResult prepareResult;
	
	TransactionBranch(XAResource xaResource, Xid xid) {
		resources.add(new ManagedResource(xaResource));
		this.xid = xid;
		this.prepareResult = PrepareResult.NOTPREPARED;
	}
	
	Xid xid() {
		return xid;
	}
	
	XAResource resource() {
		return resources.get(0).resource();
	}
	
	Optional<ManagedResource> resource(XAResource xaResource) {
		return resources.stream().filter( r -> r.resource() == xaResource).findFirst();
	}
		
	boolean adopt(XAResource candidate) throws XAException {
		if (resource(candidate).isPresent()) {
			return true;
		}
		if (resource().isSameRM(candidate)) {
			ManagedResource managedResource = new ManagedResource(candidate);
			managedResource.start(xid, XAResource.TMJOIN);
			resources.add(managedResource);
			return true;
		}
		return false;
	}
	
	void start() throws XAException {
		for (ManagedResource each: resources) {
			each.start(xid, XAResource.TMNOFLAGS);
		}
	}
	
	void end() throws XAException {		
		end(XAResource.TMSUCCESS);
	}
	
	void end(int flags) throws XAException {
		for (ManagedResource each: resources) {
			each.end(xid, flags);
		}
	}
	
	boolean end(XAResource resource, int flags) throws XAException {
		Optional<ManagedResource> managedResource = resource(resource);
		if (managedResource.isPresent()) {
			return managedResource.get().end(xid, flags);
		} else {
			return false;
		}
	}
	
	void prepare(TransactionLog log) throws XAException {
		end();
		prepareResult = PrepareResult.of(resource().prepare(xid));
		if (prepareResult == PrepareResult.OK) {
			log.prepared(xid);
		}
	}
	
	void commitOnePhase() throws XAException {
		if (prepareResult != PrepareResult.NOTPREPARED) {
			throw new IllegalStateException();
		}
		end();
		resource().commit(xid, true);
	}
	
	void commitTwoPhase(TransactionLog log) throws XAException {
		switch (prepareResult) {
			case NOTPREPARED:
				throw new IllegalStateException();
			case OK:
				resource().commit(xid, false);
				log.committed(xid);
				break;
			case READONLY:				
		}
	}
	
	void rollback(TransactionLog log) throws XAException {
		if (!isReadOnly()) {
			end();
		}
		resource().rollback(xid);
		if (prepareResult == PrepareResult.OK) {
			log.rollbacked(xid);
		}
	}
	
	boolean isReadOnly() {
		return prepareResult == PrepareResult.READONLY;
	}
	
	private static enum PrepareResult {
		NOTPREPARED,
		OK,
		READONLY;
		
		static PrepareResult of(int in) {
			switch(in) {
				case XAResource.XA_OK:
					return OK;
				case XAResource.XA_RDONLY:
					return READONLY;
				default:
					throw new IllegalArgumentException();
			}
		}
	}
}
