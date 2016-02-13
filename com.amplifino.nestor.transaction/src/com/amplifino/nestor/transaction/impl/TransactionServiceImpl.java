package com.amplifino.nestor.transaction.impl;

import java.util.function.Supplier;

import javax.transaction.UserTransaction;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amplifino.nestor.transaction.TransactionContext;
import com.amplifino.nestor.transaction.TransactionService;

@Component
public class TransactionServiceImpl implements TransactionService {

	@Reference
	private UserTransaction userTransaction;

	@Override
	public TransactionContext context() {
		return new TransactionContextImpl(userTransaction);
	}

	@Override
	public <T> T execute(Supplier<T> supplier) {
		try (TransactionContext context = context()) {
			T result = supplier.get();
			context.commit();
			return result;
		}
	}

	@Override
	public void execute(Runnable runnable) {
		try (TransactionContext context = context()) {
			runnable.run();
			context.commit();
		} 		
	}
	
	
	
}
