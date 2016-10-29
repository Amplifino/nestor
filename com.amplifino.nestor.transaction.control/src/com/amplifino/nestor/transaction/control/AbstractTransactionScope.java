package com.amplifino.nestor.transaction.control;

import java.util.Optional;

import javax.transaction.InvalidTransactionException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.osgi.service.transaction.control.TransactionException;

abstract class AbstractTransactionScope implements TransactionScope {
	
	private final Optional<TransactionScope> parent;
	private Transaction suspendedTransaction = null;

	public AbstractTransactionScope() {
		this.parent = Optional.empty();
	}
	
	AbstractTransactionScope(TransactionScope parent) {
		this.parent = Optional.of(parent);
	}

	@Override
	public final TransactionScope parent() {
		return parent.orElseThrow(() -> new IllegalStateException("Initial scope has no parent"));
	}

	@Override
	public TransactionControlImpl getTransactionControl() {
		return parent().getTransactionControl();
	}
		
	@Override
	public final TransactionScope notSupported() {
		suspend();
		return new NoTransactionScope(this);
	}
	
	@Override
	public final TransactionScope requiresNew() {
		suspend();
		return new RootTransactionScope(this);
	}
	
	@Override
	public final TransactionScope required() {
		return inTransaction() ? new NestedTransactionScope(this) : new RootTransactionScope(this);
	}
	
	@Override
	public final TransactionScope supports() {
		return inTransaction() ? new NestedTransactionScope(this) : new NoTransactionScope(this);
	}
	
	void suspend() {
		try {
			suspendedTransaction = getTransactionControl().transactionManager().suspend();
		} catch (SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}
	
	@Override
	public final void resume() {
		if (suspendedTransaction != null) {
			try {
				getTransactionControl().transactionManager().resume(suspendedTransaction);
			} catch (InvalidTransactionException | SystemException e) {
				throw new TransactionException(e.toString(), e);
			} finally {
				suspendedTransaction = null;
			}
		}
	}
	
	boolean inTransaction() {
		try {
			return getTransactionControl().transactionManager().getStatus() != Status.STATUS_NO_TRANSACTION;
		} catch (SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}
}
