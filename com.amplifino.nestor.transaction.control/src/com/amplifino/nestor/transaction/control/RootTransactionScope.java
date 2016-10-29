package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import javax.transaction.TransactionManager;

import org.osgi.service.transaction.control.TransactionContext;

class RootTransactionScope extends RealTransactionScope {
	
	private final TransactionContext context;
	
	public RootTransactionScope(TransactionScope parent) {
		super(parent);
		this.context = new RealTransactionContext(this);
	}

	@Override
	public <T> T execute(Callable<T> callable) throws Exception {
		TransactionManager txManager = getTransactionControl().transactionManager();
		txManager.begin();
		T result = null;
		try {
			result = callable.call();
		} catch (Throwable e) {
			if (ignore(e)) {
				txManager.commit();
			} else {
				txManager.rollback();
			}
			throw e;
		}
		txManager.commit();
		return result;		
	}
	
	@Override
	public TransactionContext getContext() {
		return context;
	}

	
}
