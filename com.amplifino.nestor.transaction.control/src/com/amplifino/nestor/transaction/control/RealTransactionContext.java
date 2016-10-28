package com.amplifino.nestor.transaction.control;

import java.util.function.Consumer;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.osgi.service.transaction.control.LocalResource;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.TransactionStatus;

class RealTransactionContext extends ActiveTransactionContext {
	
	private final RootTransactionScope scope;
	
	RealTransactionContext(RootTransactionScope scope) {
		this.scope = scope;
	}

	@Override
	public boolean getRollbackOnly() throws IllegalStateException {
		return getTransactionStatus() == TransactionStatus.MARKED_ROLLBACK;
	}

	@Override
	public Object getTransactionKey() {
		return transaction();
	}

	@Override
	public TransactionStatus getTransactionStatus() {
		try {
			return toStatus(transactionManager().getStatus());
		} catch (SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}

	@Override
	public void postCompletion(Consumer<TransactionStatus> consumer) throws IllegalStateException {
		try {
			transaction().registerSynchronization(new PostAction(consumer));
		} catch (RollbackException | SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}

	@Override
	public void preCompletion(Runnable runnable) throws IllegalStateException {
		try {
			transaction().registerSynchronization(new PreAction(runnable));
		} catch (RollbackException | SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}

	@Override
	public void registerLocalResource(LocalResource resource) throws IllegalStateException {
		registerXAResource(new XAResourceAdapter(resource));
	}

	@Override
	public void registerXAResource(XAResource resource) throws IllegalStateException {
		try {
			transaction().enlistResource(resource);
		} catch (RollbackException | SystemException e) {	
			throw new TransactionException(e.toString(), e);
		}
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException {
		try {
			transactionManager().setRollbackOnly();
		} catch (SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}

	@Override
	public boolean supportsLocal() {
		return true;
	}

	@Override
	public boolean supportsXA() {
		return true;
	}
	
	private TransactionManager transactionManager() {
		return scope.getTransactionControl().transactionManager();
	}
	
	private Transaction transaction() {
		try {
			return transactionManager().getTransaction();
		} catch (SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}
	
	static TransactionStatus toStatus(int status) {
		switch(status) {
			case Status.STATUS_ACTIVE:
				return TransactionStatus.ACTIVE;
			case Status.STATUS_COMMITTED:
				return TransactionStatus.COMMITTED;
			case Status.STATUS_COMMITTING:
				return TransactionStatus.COMMITTING;
			case Status.STATUS_MARKED_ROLLBACK:
				return TransactionStatus.MARKED_ROLLBACK;
			case Status.STATUS_NO_TRANSACTION:
				return TransactionStatus.NO_TRANSACTION;
			case Status.STATUS_PREPARED:
				return TransactionStatus.PREPARED;
			case Status.STATUS_PREPARING:
				return TransactionStatus.PREPARING;
			case Status.STATUS_ROLLEDBACK:
				return TransactionStatus.ROLLED_BACK;
			case Status.STATUS_ROLLING_BACK:
				return TransactionStatus.ROLLING_BACK;
			case Status.STATUS_UNKNOWN:
			default:
				throw new IllegalStateException("Illegal transaction status: " + status);
		}
	}
	
	private static class PostAction implements Synchronization {

		private final Consumer<TransactionStatus> consumer; 
		
		PostAction(Consumer<TransactionStatus> consumer) {
			this.consumer = consumer;
		}
		
		@Override
		public void afterCompletion(int status) {
			consumer.accept(RealTransactionContext.toStatus(status));
		}

		@Override
		public void beforeCompletion() {
		}
		
	}
	
	private static class PreAction implements Synchronization {

		private final Runnable runnable; 
		
		PreAction(Runnable runnable) {
			this.runnable = runnable;
		}
		
		@Override
		public void afterCompletion(int status) {
		}

		@Override
		public void beforeCompletion() {
			runnable.run();
		}
		
	}
	
	private static class XAResourceAdapter implements XAResource {
		
		private final LocalResource resource;
		
		XAResourceAdapter(LocalResource resource) {
			this.resource = resource;
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			if (onePhase) {
				resource.commit();
			} else {
				throw new TransactionException("Two phase commit not supported for local resource " + resource);
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
		public int prepare(Xid xid) throws XAException {
			throw new TransactionException("Prepare not supported for local resource " + resource);
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
	
}
