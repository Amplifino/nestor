package com.amplifino.nestor.transaction.control;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionStatus;

class RootNoTransactionScope extends NoTransactionScope {
	
	private final Logger logger = Logger.getLogger("com.amplifino.nestor.transaction.control");
	private final TransactionContext context;
	private final List<Runnable> preActions = new ArrayList<>();
	private final List<Consumer<TransactionStatus>> postActions = new ArrayList<>();
	
	RootNoTransactionScope(TransactionScope parent) {
		super(parent);
		context = new NoTransactionContext(this);
	}

	@Override
	public TransactionContext getContext() {
		return context;
	}

	@Override
	public <T> Try<T> execute(Callable<T> callable) {
		return Try.of(callable).handle(this::handle); 
	}

	private <T> void handle(T t, Throwable e) {
		if (e == null) {
			preActions();
		}
		postActions();
	}
	
	private void preActions() {
		preActions.forEach(this::preAction);
	}
	
	private void preAction(Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable e) {			
			logger.log(Level.WARNING, "Exception in precommit action ", e);
		}
	}
	
	private void postActions() {
		postActions.forEach(this::postAction);
	}
	
	private void postAction(Consumer<TransactionStatus> consumer) {
		try {
			consumer.accept(TransactionStatus.NO_TRANSACTION);
		} catch (Throwable e) {			
			logger.log(Level.WARNING, "Exception in postcommit action ", e);
		}
	}
	
	void postCompletion(Consumer<TransactionStatus> consumer) {
		postActions.add(consumer);
	}

	void preCompletion(Runnable runnable) {
		preActions.add(runnable);
	}
		
}
