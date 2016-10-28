package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.TransactionContext;

class NoTransactionScope extends ActiveTransactionScope {
	
	private final TransactionContext context;
	
	NoTransactionScope(TransactionScope parent) {
		super(parent);
		context = new NoTransactionContext();
	}

	@Override
	public TransactionContext getContext() {
		return context;
	}

	@Override
	public TransactionScope required() {
		return new RootTransactionScope(this);
	}

	@Override
	public TransactionScope supports() {
		return new NoTransactionScope(this);
	}

	@Override
	public <T> T execute(Callable<T> callable) throws Exception {
		return callable.call();
	}

	@Override
	public boolean isTransaction() {
		return false;
	}

	@Override
	public void ignoreException(Throwable throwable) {
		throw new IllegalStateException();
	}
		
}
