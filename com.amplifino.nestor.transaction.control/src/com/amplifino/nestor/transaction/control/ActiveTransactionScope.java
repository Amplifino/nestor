package com.amplifino.nestor.transaction.control;

abstract class ActiveTransactionScope extends AbstractTransactionScope {
	
	ActiveTransactionScope(TransactionScope parent) {
		super(parent);
	}

	@Override
	public final boolean isActive() {
		return true;
	}

	@Override
	public final boolean getRollbackOnly() {
		return getContext().getRollbackOnly();
	}

	@Override
	public final void setRollbackOnly() {
		getContext().setRollbackOnly();
	}

}
