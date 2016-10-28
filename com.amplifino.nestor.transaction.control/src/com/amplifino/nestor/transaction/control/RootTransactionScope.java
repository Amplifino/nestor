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
		boolean inJtaTransaction = txManager.getTransaction() != null;
		if (!inJtaTransaction) {
			txManager.begin();
		}
		try {
			T result = callable.call();
			if (!inJtaTransaction) {
				txManager.commit();
			}
			return result;
		} catch (Throwable e) {
			if (ignore(e)) {
				if (!inJtaTransaction) {
					txManager.commit();
				}
			} else {
				if (inJtaTransaction) {
					txManager.setRollbackOnly();
				} else {
					txManager.rollback();
				}
			}
			throw e;
		}
	}

	@Override
	public TransactionContext getContext() {
		return context;
	}

	
}
