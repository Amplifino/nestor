package com.amplifino.nestor.transaction.provider;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class UserTransactionImpl implements UserTransaction {
	
	@Reference
	private TransactionManager transactionManager;

	@Override
	public void begin() throws NotSupportedException, SystemException {
		transactionManager.begin();
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
		transactionManager.commit();		
	}

	@Override
	public int getStatus() throws SystemException {
		return transactionManager.getStatus();
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		transactionManager.rollback();
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		transactionManager.setRollbackOnly();
	}

	@Override
	public void setTransactionTimeout(int timeOut) throws SystemException {
		transactionManager.setTransactionTimeout(timeOut);
	}

}
