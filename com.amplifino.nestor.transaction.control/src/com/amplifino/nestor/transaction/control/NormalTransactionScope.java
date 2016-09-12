package com.amplifino.nestor.transaction.control;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.Synchronization;

import org.osgi.service.transaction.control.LocalResource;
import org.osgi.service.transaction.control.TransactionStatus;

import javax.transaction.Status;

import javax.transaction.Transaction;

class NormalTransactionScope implements TransactionScope {
	
	private final TransactionManager transactionManager;
	private final Transaction transaction;
	private final TransactionScope parent;
	private final Map<Object, Object> scopedObjects = new HashMap<>();
	
	public NormalTransactionScope(TransactionScope parent, TransactionManager transactionManager) throws SystemException, NotSupportedException {
		this.parent = parent;
		this.transactionManager = transactionManager;
		transactionManager.begin();
		transaction = transactionManager.getTransaction();
	}

	@Override
	public void suspend() throws SystemException {
		transactionManager.suspend();
	}

	@Override
	public void resume() throws SystemException, InvalidTransactionException {
		transactionManager.resume(transaction);
	}

	@Override
	public TransactionScope parent() {
		return parent;
	}

	@Override
	public <T> T execute(Callable<T> callable) throws Exception {
		boolean commit = false;
		try {
			return callable.call();
		} finally {
			if (commit) {
				transaction.commit();
			} else {
				transaction.rollback();
			}
		}
	}

	@Override
	public void setRollbackOnly()  {
		try {
			transaction.setRollbackOnly();
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getRollbackOnly()  {
		try {
			return transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK;
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object getScopedValue(Object key) {
		return scopedObjects.get(key);
	}

	@Override
	public Object getTransactionKey() {
		return transaction;
	}

	@Override
	public TransactionStatus getTransactionStatus() {
		
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public void postCompletion(Consumer<TransactionStatus> consumer) {
		try {
			transaction.registerSynchronization(new PostAction(consumer));
		} catch (RollbackException e) {
			throw new IllegalStateException(e);
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void preCompletion(Runnable runnable) throws IllegalStateException {
		try {
			transaction.registerSynchronization(new PreAction(runnable));
		} catch (RollbackException e) {
			throw new IllegalStateException(e);
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void putScopedValue(Object key, Object value) {
		scopedObjects.put(key, value);
	}

	@Override
	public void registerLocalResource(LocalResource resoyrce) throws IllegalStateException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void registerXAResource(XAResource resource) throws IllegalStateException {
		try {
			transaction.enlistResource(resource);
		} catch (RollbackException e) {
			throw new IllegalStateException(e);
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean supportsLocal() {
		return false;
	}

	@Override
	public boolean supportsXA() {
		return true;
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
			consumer.accept(NormalTransactionScope.toStatus(status));
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
