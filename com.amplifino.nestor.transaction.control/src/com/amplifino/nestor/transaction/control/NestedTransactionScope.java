package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.TransactionContext;

class NestedTransactionScope extends RealTransactionScope {
	
	NestedTransactionScope(TransactionScope parent) {
		super(parent);
	}
	
	
	@Override
	public <T> T execute(Callable<T> callable) throws Exception {
		try {
			return callable.call();
		} catch (Throwable e) {
			if (!ignore(e)) {
				getContext().setRollbackOnly();
			}
			throw e;
		}
	}

	@Override
	public TransactionContext getContext() {
		return parent().getContext();
	}

}
