package com.amplifino.nestor.transaction.control;

public abstract class NoTransactionScope extends ActiveTransactionScope {

	NoTransactionScope(TransactionScope parent) {
		super(parent);
	}
	
	@Override
	public final boolean isTransaction() {
		return false;
	}
	
	@Override
	public final void ignoreException(Throwable throwable) {
		throw new IllegalStateException();
	}
	
	@Override
	final TransactionScope supportNoTransactionScope() {
		return new NestedNoTransactionScope(this);
	}
}
