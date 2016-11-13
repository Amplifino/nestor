package com.amplifino.nestor.transaction.control;

import java.util.function.Consumer;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.osgi.service.transaction.control.LocalResource;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.TransactionStatus;

class RealTransactionContext extends ActiveTransactionContext {
	
	private final RealTransactionScope scope;
	
	RealTransactionContext(RealTransactionScope scope) {
		this.scope = scope;
	}

	@Override
	public final Object getScopedValue(Object key) {
		return scope.getTransactionControl().synchronizationRegistry().getResource(key);
	}

	@Override
	public final void putScopedValue(Object key, Object value) {
		scope.getTransactionControl().synchronizationRegistry().putResource(key, value);		
	}
	
	@Override
	public boolean getRollbackOnly() throws IllegalStateException {
		return getTransactionStatus() == TransactionStatus.MARKED_ROLLBACK;
	}

	@Override
	public Object getTransactionKey() {
		return scope.getTransactionControl().synchronizationRegistry().getTransactionKey();
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
	public void postCompletion(Consumer<TransactionStatus> consumer)  {
		try {
			transaction().registerSynchronization(new PostAction(consumer));
		} catch (RollbackException | SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}

	@Override
	public void preCompletion(Runnable runnable) {
		try {
			transaction().registerSynchronization(new PreAction(runnable));
		} catch (RollbackException | SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}

	@Override
	public void registerLocalResource(LocalResource resource)  {
		registerXAResource(scope.getTransactionControl().wrapResource(resource), null);
	}

	@Override
	public void registerXAResource(XAResource resource, String resourceId)  {
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

	
	
}
