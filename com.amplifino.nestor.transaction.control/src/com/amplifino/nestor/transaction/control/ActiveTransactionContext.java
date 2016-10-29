package com.amplifino.nestor.transaction.control;

import org.osgi.service.transaction.control.TransactionContext;

abstract class ActiveTransactionContext implements TransactionContext {

	@Override
	public final boolean isReadOnly() {
		return false;
	}

}
