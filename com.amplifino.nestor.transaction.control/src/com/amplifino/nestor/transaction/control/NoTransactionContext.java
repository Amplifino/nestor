package com.amplifino.nestor.transaction.control;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.transaction.xa.XAResource;

import org.osgi.service.transaction.control.LocalResource;
import org.osgi.service.transaction.control.TransactionStatus;

class NoTransactionContext extends ActiveTransactionContext {
	
	private final RootNoTransactionScope scope;
	private final Map<Object, Object> scopedObjects = new HashMap<>();
	
	public NoTransactionContext(RootNoTransactionScope scope) {
		this.scope = scope;
	}
	
	@Override
	public final Object getScopedValue(Object key) {
		return scopedObjects.get(key);
	}

	@Override
	public final void putScopedValue(Object key, Object value) {
		scopedObjects.put(key, value);
	}
	
	@Override
	public boolean getRollbackOnly() throws IllegalStateException {
		throw new IllegalStateException();
	}

	@Override
	public Object getTransactionKey() {
		return null;
	}

	@Override
	public TransactionStatus getTransactionStatus() {
		return TransactionStatus.NO_TRANSACTION;
	}

	@Override
	public void postCompletion(Consumer<TransactionStatus> consumer) throws IllegalStateException {
		scope.postCompletion(consumer);
	}

	@Override
	public void preCompletion(Runnable runnable) throws IllegalStateException {
		scope.preCompletion(runnable);
	}

	@Override
	public void registerLocalResource(LocalResource resource) throws IllegalStateException {
		throw new IllegalStateException();

	}

	@Override
	public void registerXAResource(XAResource resource, String resourceId) throws IllegalStateException {
		throw new IllegalStateException();
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException {
		throw new IllegalStateException();
	}

	@Override
	public boolean supportsLocal() {
		return false;
	}

	@Override
	public boolean supportsXA() {
		return false;
	}

}
