package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.TransactionContext;

class NestedNoTransactionScope extends NoTransactionScope {
	
	NestedNoTransactionScope(TransactionScope parent) {
		super(parent);
	}

	@Override
	public TransactionContext getContext() {
		return parent().getContext();
	}

	@Override
	public <T> Try<T> execute(Callable<T> callable) {
		return Try.of(callable); 
	}
	
		
}
