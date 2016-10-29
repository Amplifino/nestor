package com.amplifino.nestor.transaction.control;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionStatus;

class NoTransactionScope extends ActiveTransactionScope {
	
	private final TransactionContext context;
	private final List<Runnable> preActions = new ArrayList<>();
	private final List<Consumer<TransactionStatus>> postActions = new ArrayList<>();
	
	NoTransactionScope(TransactionScope parent) {
		super(parent);
		context = new NoTransactionContext(this);
	}

	@Override
	public TransactionContext getContext() {
		return context;
	}

	@Override
	public <T> T execute(Callable<T> callable) throws Exception {
		try {
			T result = callable.call();
			preActions();
			return result;
		} finally {
			postActions();
		}
	}

	private void preActions() {
		preActions.forEach(this::preAction);
	}
	
	private void preAction(Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable e) {			
		}
	}
	
	private void postActions() {
		postActions.forEach(this::postAction);
	}
	
	private void postAction(Consumer<TransactionStatus> consumer) {
		try {
			consumer.accept(TransactionStatus.NO_TRANSACTION);
		} catch (Throwable e) {			
		}
	}
	
	@Override
	public boolean isTransaction() {
		return false;
	}

	@Override
	public void ignoreException(Throwable throwable) {
		throw new IllegalStateException();
	}
	
	void postCompletion(Consumer<TransactionStatus> consumer) {
		postActions.add(consumer);
	}

	void preCompletion(Runnable runnable) throws IllegalStateException {
		preActions.add(runnable);
	}
		
}
