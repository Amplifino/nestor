package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.TransactionContext;

interface TransactionScope extends TransactionContext{

	void suspend() throws Exception;
	void resume() throws Exception;
	TransactionScope parent();
	<T> T execute(Callable<T> callable) throws Exception;
	
}
