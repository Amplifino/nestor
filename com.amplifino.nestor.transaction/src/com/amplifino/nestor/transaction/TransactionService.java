package com.amplifino.nestor.transaction;

import java.util.function.Supplier;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface TransactionService {	
	TransactionContext context();
	<T> T execute(Supplier<T> supplier);
	void execute(Runnable run);
}
