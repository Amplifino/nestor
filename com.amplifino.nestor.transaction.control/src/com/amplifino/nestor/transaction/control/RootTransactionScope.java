package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionException;


class RootTransactionScope extends RealTransactionScope {
	
	private final TransactionContext context;
	
	public RootTransactionScope(TransactionScope parent) {
		super(parent);
		this.context = new RealTransactionContext(this);
	}

	@Override
	public <T> Try<T> execute(Callable<T> callable) {
		begin();
		return Try.of(callable).handle(this::handle);		
	}
	
	private <T> void handle(T t, Throwable e) {
		if (e == null || ignore(e)) {
			commit();
		} else {
			rollback();
		}		
	}
	
	private void begin() {
		try {
			getTransactionControl().transactionManager().begin();
		} catch (SystemException | NotSupportedException e) {
			throw new TransactionException(e.toString(), e);
		}
	}
	
	private void commit() {
		try {
			getTransactionControl().transactionManager().commit();
		} catch (SystemException | RollbackException | HeuristicRollbackException | HeuristicMixedException e) {
			throw new TransactionException(e.toString(), e);
		}
	}
	
	private void rollback() {
		try {
			getTransactionControl().transactionManager().rollback();
		} catch (SystemException e) {
			throw new TransactionException(e.toString(), e);
		}
	}
	@Override
	public TransactionContext getContext() {
		return context;
	}

	
}
