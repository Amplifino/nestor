package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.transaction.control.LocalResource;
import org.osgi.service.transaction.control.ScopedWorkException;
import org.osgi.service.transaction.control.TransactionBuilder;
import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionControl;

@Component(property={"osgi.xa.enabled:Boolean=true"})
@Designate(ocd=TransactionControlConfiguration.class)
public class TransactionControlImpl implements TransactionControl {
	
	private final ThreadLocal<TransactionScope> scopeHolder = ThreadLocal.withInitial(this::initialScope);
	@Reference
	private TransactionManager transactionManager;
	@Reference
	private TransactionSynchronizationRegistry synchronizationRegistry;
	private TransactionControlConfiguration config;
	private final Object contextKey = new Object(); 
	
	@Activate
	public void activate(TransactionControlConfiguration config) {
		this.config = config;
	}
	
	private TransactionScope getScope() {
		return scopeHolder.get();
	}
	
	private TransactionScope pushScope(TransactionScope scope) {
		scopeHolder.set(scope);
		return scope;
	}

	private void popScope() {
		scopeHolder.set(getScope().parent());
		getScope().resume();
	}
	
	@Override
	public <T> T notSupported(Callable<T> callable) {
		return execute(getScope().notSupported(), callable);			
	}

	@Override
	public <T> T required(Callable<T> callable) {
		return execute(getScope().required(), callable);
	}

	@Override
	public <T> T requiresNew(Callable<T> callable) {
		return execute(getScope().requiresNew(), callable);
	}

	@Override
	public <T> T supports(Callable<T> callable)  {
		return execute(getScope().supports(), callable);
	}

	private  <T> T execute(TransactionScope scope, Callable<T> callable) {
		try {
			return pushScope(scope).execute(callable).orElseThrow(this::wrap);
		} finally {
			popScope();
		}
	}
	
	private ScopedWorkException wrap(Throwable e) {
		if (e instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
		if (e instanceof ScopedWorkException) {
			Throwable cause = e.getCause();
			ScopedWorkException result = new ScopedWorkException(e.getMessage(), cause, getScope().getContext());
			result.addSuppressed(e);
			return result;
		}
		return new ScopedWorkException(e.toString(), e, getScope().getContext());
	}
	
	@Override
	public boolean activeScope() {
		return getScope().isActive();
	}

	@Override
	public boolean activeTransaction() {
		return getScope().isTransaction();
	}

	@Override
	public TransactionBuilder build() {
		return new TransactionBuilderImpl(this);
	}

	@Override
	public TransactionContext getCurrentContext() {
		return getScope().getContext();
	}

	@Override
	public boolean getRollbackOnly() {
		return getScope().getRollbackOnly();
	}

	@Override
	public void ignoreException(Throwable throwable) {
		getScope().ignoreException(throwable);
	}

	@Override
	public void setRollbackOnly() {
		getScope().setRollbackOnly();
	}

	private TransactionScope initialScope() {
		return new NoScope(this);
	}
	
	TransactionManager transactionManager() {
		return transactionManager;
	}
	
	TransactionSynchronizationRegistry synchronizationRegistry() {
		return synchronizationRegistry;
	}

	XAResource wrapResource(LocalResource resource) {
		return config.compliance().wrap(resource);
	}
	
	Object contextKey() {
		return contextKey;
	}
}
