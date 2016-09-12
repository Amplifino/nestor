package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

public class NoTransactionScope implements TransactionScope {
	
	private final TransactionScope parent;

	public NoTransactionScope(TransactionScope parent) {
		this.parent = parent;
	}
	
	@Override
	public void suspend() {
	}

	@Override
	public void resume() throws Exception {		
	}

	@Override
	public TransactionScope parent() {
		return parent;
	}

	@Override
	public <T> T execute(Callable<T> callable) throws Exception {
		return callable.call();
	}

}
