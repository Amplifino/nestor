package com.amplifino.nestor.transaction.control;

import java.util.ArrayList;
import java.util.List;

abstract class RealTransactionScope extends ActiveTransactionScope {
	
	private final List<Throwable> ignoredExceptions = new ArrayList<>();
	
	RealTransactionScope(TransactionScope parent) {
		super(parent);
	}

	@Override
	public final boolean isTransaction() {
		return true;
	}

	@Override
	public void ignoreException(Throwable e) {
		ignoredExceptions.add(e);
	}

	final boolean ignore(Throwable throwable) {
		// paranoia. Maybe someone coded a Throwable with a buggy equals.
		try {
			return ignoredExceptions.contains(throwable);
		} catch (Throwable e) {
			return false;
		}
	}
	
	@Override
	final TransactionScope supportNoTransactionScope() {
		return new RootNoTransactionScope(this);
	}
}
