package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.TransactionContext;

class NestedTransactionScope extends RealTransactionScope {
	
	private TransactionContext context;
	
	NestedTransactionScope(TransactionScope parent) {
		super(parent);
		this.context = new RealTransactionContext(this);
	}
	
	
	@Override
	public <T> Try<T> execute(Callable<T> callable) {
		return Try.of(callable).handle(this::handle);
	}

	private <T> void handle(T t, Throwable e) {
		if (e != null && !ignore(e)) {
			getContext().setRollbackOnly();
		}
	}

	@Override
	public TransactionContext getContext() {
		return context;
	}

}
