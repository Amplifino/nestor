package com.amplifino.nestor.transaction.impl;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.amplifino.nestor.transaction.TransactionContext;

class TransactionContextImpl implements TransactionContext {
	
	private final UserTransaction userTransaction;
	private boolean finished = false;
	
	TransactionContextImpl(UserTransaction userTransaction) {
		this.userTransaction = userTransaction;
		try {
			userTransaction.begin();
		} catch (SystemException | NotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void commit() {
		try {
			userTransaction.commit();
			finished = true;
		} catch (RollbackException | HeuristicRollbackException | HeuristicMixedException e) {
			finished = true;
			throw new RuntimeException(e);
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		if (!finished) {
			try {
				userTransaction.rollback();
			} catch (SystemException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
