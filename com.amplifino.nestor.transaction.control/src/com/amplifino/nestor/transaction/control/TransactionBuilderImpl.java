package com.amplifino.nestor.transaction.control;

import java.util.List;
import java.util.concurrent.Callable;

import org.osgi.service.transaction.control.TransactionBuilder;

public class TransactionBuilderImpl extends TransactionBuilder {
	
	private final TransactionControlImpl transactionControl;
	
	TransactionBuilderImpl(TransactionControlImpl transactionControl) {
		this.transactionControl = transactionControl;
	}

	@Override
	public <T> T notSupported(Callable<T> callable)  {
		return transactionControl.notSupported(wrap(callable));
	}

	@Override
	public <T> T required(Callable<T> callable) {
		return transactionControl.required(wrap(callable));
	}

	@Override
	public <T> T requiresNew(Callable<T> callable) {
		return transactionControl.required(wrap(callable));
	}

	@Override
	public <T> T supports(Callable<T> callable) {
		return transactionControl.supports(wrap(callable));
	}

	@Override
	public TransactionBuilder readOnly() {
		return this;
	}
		
	private <T> Callable<T> wrap(Callable<T> callable) {
		return () -> {
			try {
				return callable.call(); 
			} catch (Throwable e) {
				if (ignore(e)) {
					transactionControl.ignoreException(e);
				}
				throw e;
			}
		};
	}
	
	private boolean ignore(Throwable e) {
		return score(e, noRollbackFor) < score(e, rollbackFor);
	}
	
	private int score(Throwable e, List<Class<? extends Throwable>> types) {
		return types.stream()
			.filter(t -> t.isInstance(e))
			.mapToInt(t -> this.distance(e, t))
			.min()
			.orElse(Integer.MAX_VALUE);
	}

	private int distance(Object instance, Class<?> clazz) {
		int i = 0;
		Class<?> current = instance.getClass();
		do {
			if (current == clazz) {
				return i;
			} else {
				i++;
			}
			current = current.getSuperclass();	
		} while (current != null);
		throw new IllegalArgumentException();
	}

}
