package com.amplifino.nestor.transaction.provider;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class TransactionSynchronizationRegistryImpl implements TransactionSynchronizationRegistry {
	
	@Reference
	private TransactionManager transactionManager;
	
	private TransactionImpl getTransaction() {
		try {
			return (TransactionImpl) transactionManager.getTransaction();
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Object getResource(Object key) {
		TransactionImpl transaction = getTransaction();
		if (transaction == null) {
			throw new IllegalStateException("Not in transaction");
		}
		return transaction.getResource(key);		
	}

	@Override
	public boolean getRollbackOnly() {
		int status = getTransactionStatus();
		if (status == Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException();
		}
		return status == Status.STATUS_MARKED_ROLLBACK;
	}

	@Override
	public Object getTransactionKey() {
		return getTransaction();
	}

	@Override
	public int getTransactionStatus() {
		try {
			return transactionManager.getStatus();
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void putResource(Object key, Object value) {
		TransactionImpl transaction = getTransaction();
		if (transaction == null) {
			throw new IllegalStateException("Not in transaction");
		}
		transaction.putResource(key, value);
	}
	
	@Override
	public void registerInterposedSynchronization(Synchronization synchronizer) {
		Transaction transaction = getTransaction();
		if (transaction == null) {
			throw new IllegalStateException("Not in transaction");
		}
		((TransactionImpl) transaction).registerInterposedSynchronization(synchronizer);
	}

	@Override
	public void setRollbackOnly() {
		try {
			transactionManager.setRollbackOnly();
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}
	
}
