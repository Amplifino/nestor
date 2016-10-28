package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.TransactionContext;

interface TransactionScope {

	TransactionScope parent();
	TransactionScope notSupported();
	TransactionScope required();
	TransactionScope requiresNew();
	TransactionScope supports();
	void resume();
	<T> T execute(Callable<T> callable) throws Exception;
	
	boolean isActive();
	boolean isTransaction();
	TransactionContext getContext();
	boolean getRollbackOnly();
	void ignoreException(Throwable throwable);
	void setRollbackOnly();
	
	TransactionControlImpl getTransactionControl();
}
