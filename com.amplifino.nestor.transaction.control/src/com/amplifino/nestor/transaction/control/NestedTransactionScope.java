package com.amplifino.nestor.transaction.control;

import java.util.Objects;
import java.util.concurrent.Callable;

import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

class NestedTransactionScope implements TransactionScope {
	
	private final NormalTransactionScope  parent;
	
	public NestedTransactionScope(NormalTransactionScope parent, TransactionManager transactionManager) {
		this.parent = Objects.requireNonNull(parent);
	}

	@Override
	public void suspend() throws SystemException {
		parent.suspend();
	}

	@Override
	public void resume() throws SystemException, InvalidTransactionException {
		parent.resume();
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
			if (!commit) {
				parent.setRollbackOnly();
			}
		}
	} 

}
