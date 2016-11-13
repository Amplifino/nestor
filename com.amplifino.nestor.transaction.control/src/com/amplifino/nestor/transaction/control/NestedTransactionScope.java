package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.TransactionContext;

class NestedTransactionScope extends RealTransactionScope {
	
	private TransactionContext context;
	
	NestedTransactionScope(TransactionScope parent) {
		super(parent);
		TransactionControlImpl transactionControl = parent.getTransactionControl();
		Object contextKey = transactionControl.contextKey();
		this.context = (TransactionContext) transactionControl.synchronizationRegistry().getResource(contextKey);
		if (context == null) {
			// interference of code interacting directly with TransactionManager
			context = new RealTransactionContext(this);
			transactionControl.synchronizationRegistry().putResource(contextKey, context);
		}
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
