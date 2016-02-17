package com.amplifino.nestor.transaction.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

class TransactionImpl implements Transaction {
	
	private final byte[] globalTransactionId;
	private int lastBranch = 0;
	private int status;
	private final List<TransactionBranch> branches = new ArrayList<>(); 
	private final List<Synchronization> synchronizers = new ArrayList<>();
	private final List<Synchronization> interposedSynchronizers = new ArrayList<>();
	private final Map<Object, Object> resources = new HashMap<>();
	
	TransactionImpl() {
		globalTransactionId = XidImpl.newGlobalTransactionId();
		status = Status.STATUS_ACTIVE;
	}
	
	@Override
	public void commit() throws RollbackException {
		checkActiveOrMarked();
		if (status == Status.STATUS_MARKED_ROLLBACK) {
			rollback();
			throw new RollbackException("Transaction was marked for rollback");
		}
		beforeCompletion();
		status = Status.STATUS_COMMITTING;		
		if (branches.isEmpty()) {
			status = Status.STATUS_COMMITTED;
			afterCompletion();
			return;
		}
		if (branches.size() == 1) {
			try {
				branches.get(0).commitOnePhase();
			} catch (Throwable e) {
				report(e);
				status = Status.STATUS_ROLLEDBACK;
				afterCompletion();
				throw (RollbackException) new RollbackException(e.toString()).initCause(e);
			}
			status = Status.STATUS_COMMITTED;
			afterCompletion();
			return;
		}
		status = Status.STATUS_PREPARING;
		try {
			for (TransactionBranch branch : branches) {
				branch.prepare();									
			}
			status = Status.STATUS_COMMITTING;
		} catch (Throwable e) {
			report(e);
			status = Status.STATUS_ROLLING_BACK;
		}
		if (status == Status.STATUS_COMMITTING) {
			logCommitDecision();
		}
		for (TransactionBranch branch : branches) {
			try {
				if (status == Status.STATUS_COMMITTING) {
					branch.commitTwoPhase();
				} else {
					branch.rollback();
				}
			} catch (Throwable e) {				
				report(e);
			}
		}
		status = (status == Status.STATUS_COMMITTING) ? Status.STATUS_COMMITTED : Status.STATUS_ROLLEDBACK;
		boolean rollback = status == Status.STATUS_ROLLEDBACK;
		afterCompletion();
		if (rollback) {
			throw new RollbackException("Received rollback vote or exception in prepare step");
		}				
	}

	private void logCommitDecision() {
		// TODO
		// log commit decision and flush log
		// when recovering from failure we need log record to decide if we
		// need to commit or rollback in doubt transactions
		//
	}
	
	@Override
	public boolean delistResource(XAResource resource, int flags) throws SystemException {
		checkActiveOrMarked();
		Optional<TransactionBranch> branch = branch(resource);
		if (!branch.isPresent()) {
			return false;
		}
		try {
			return branch.get().end(flags);
		} catch (XAException e) {
			throw (SystemException) new SystemException(e.toString()).initCause(e);
		}		
	}


	private boolean isParticipant(XAResource resource) {
		return branch(resource).isPresent();
	}
	
	private Optional<TransactionBranch> branch(XAResource resource) {
		return branches.stream()
			.filter(branch -> branch.resource()
			.equals(resource)).findAny();
	}
	
	@Override
	public boolean enlistResource(XAResource resource) throws RollbackException, IllegalStateException, SystemException {
		checkActive();		
		if (!isParticipant(resource)) {
			TransactionBranch branch = new TransactionBranch(resource, branch());
			try {
				branch.start();
			} catch (XAException e) {
				throw (SystemException) new SystemException(e.getMessage()).initCause(e);
			}
			branches.add(branch);			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getStatus() throws SystemException {
		return status;
	}

	@Override
	public void registerSynchronization(Synchronization synchronizer) throws RollbackException, IllegalStateException, SystemException {
		checkActive();		
		synchronizers.add(synchronizer);
	}

	void registerInterposedSynchronization(Synchronization synchronizer) {
		interposedSynchronizers.add(synchronizer);
	}
	
	@Override
	public void rollback()  {
		checkActiveOrMarked();
		status = Status.STATUS_ROLLING_BACK;
		for( TransactionBranch branch : branches) {
			try {
				branch.rollback();
			} catch (Throwable e) {			
				report(e);
			}
		}
		status = Status.STATUS_ROLLEDBACK;
		afterCompletion();
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		checkActiveOrMarked();
		status = Status.STATUS_MARKED_ROLLBACK;		
	}

	private Xid branch() {
		lastBranch++;
		return new XidImpl(globalTransactionId, ("" + lastBranch).getBytes());
	}
	
	private void beforeCompletion() {
		for (Synchronization synchronization : synchronizers) {
			try {
				synchronization.beforeCompletion();
			} catch (Throwable e) {				
				e.printStackTrace();
			}
		}
		for (Synchronization synchronization : interposedSynchronizers) {
			try {
				synchronization.beforeCompletion();
			} catch (Throwable e) {				
				e.printStackTrace();
			}
		}
	}
	
	private void afterCompletion() {		
		for (Synchronization synchronization : interposedSynchronizers) {
			try {
				synchronization.afterCompletion(status);
			} catch (Throwable e) {
				report(e);
			}
		}
		for (Synchronization synchronization : synchronizers) {
			try {
				synchronization.afterCompletion(status);
			} catch (Throwable e) {			
				report(e);
			}
		}
		status = Status.STATUS_NO_TRANSACTION;
	}
	
	private void report(Throwable e) {
		Logger.getLogger("com.amplifino.tx")
			.log(Level.SEVERE, "Exception in state " + status + ": " + e.getMessage() , e);   
	}
	
	Object getResource(Object key) {
		return resources.get(key);
	}
	
	void putResource(Object key, Object value) {
		resources.put(key, value);
	}
	
	private void checkActive() throws RollbackException  {
		if (status == Status.STATUS_MARKED_ROLLBACK) {
			throw new RollbackException("Transaction marked for rollback");
		}
		if (status != Status.STATUS_ACTIVE) {
			throw new IllegalStateException("Transaction not active, but in state: " + status);
		}
	}
	
	private void checkActiveOrMarked() {
		if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
			throw new IllegalStateException("Transaction not active, but in state: " + status);
		}
	}
}