package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.TransactionContext;

class NoScope extends AbstractTransactionScope {
	
	private final TransactionControlImpl transactionControl;
	
	NoScope(TransactionControlImpl transactionControl) {
		super();
		this.transactionControl = transactionControl;
	}

	@Override
	public <T> T execute(Callable<T> callable) throws Exception {
		throw new IllegalStateException();
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean isTransaction() {
		return false;
	}

	@Override
	public TransactionContext getContext() {
		return null;
	}

	@Override
	public boolean getRollbackOnly() {
		throw new IllegalStateException();
	}

	@Override
	public void ignoreException(Throwable throwable) {
		throw new IllegalStateException();
	}

	@Override
	public void setRollbackOnly() {
		throw new IllegalStateException();		
	}
	
	@Override
	public TransactionControlImpl getTransactionControl() {
		return transactionControl;
	}
	
}
