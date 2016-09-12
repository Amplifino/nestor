package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.ScopedWorkException;
import org.osgi.service.transaction.control.TransactionBuilder;
import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.TransactionRolledBackException;

public class TransactionControlImpl implements TransactionControl {

	@Override
	public <T> T notSupported(Callable<T> callable) throws TransactionException, ScopedWorkException {
		return null;
	}

	@Override
	public <T> T required(Callable<T> callable) throws TransactionException, TransactionRolledBackException, ScopedWorkException {
		return null;
	}

	@Override
	public <T> T requiresNew(Callable<T> callable) throws TransactionException, TransactionRolledBackException, ScopedWorkException {
		return null;
	}

	@Override
	public <T> T supports(Callable<T> callable) throws TransactionException, ScopedWorkException {
		return null;
	}

	@Override
	public boolean activeScope() {
		return false;
	}

	@Override
	public boolean activeTransaction() {
		return false;
	}

	@Override
	public TransactionBuilder build() {
		return null;
	}

	@Override
	public TransactionContext getCurrentContext() {
		return null;
	}

	@Override
	public boolean getRollbackOnly() throws IllegalStateException {
		return false;
	}

	@Override
	public void ignoreException(Throwable throwable) throws IllegalStateException {
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException {
	}

}
