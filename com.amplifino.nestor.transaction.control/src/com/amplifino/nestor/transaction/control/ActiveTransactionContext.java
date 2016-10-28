package com.amplifino.nestor.transaction.control;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.transaction.control.TransactionContext;

abstract class ActiveTransactionContext implements TransactionContext {

	private final Map<Object, Object> scopedObjects = new HashMap<>();
	
	@Override
	public final Object getScopedValue(Object key) {
		return scopedObjects.get(key);
	}

	@Override
	public final boolean isReadOnly() {
		return false;
	}

	@Override
	public final void putScopedValue(Object key, Object value) {
		scopedObjects.put(key, value);
	}


}
