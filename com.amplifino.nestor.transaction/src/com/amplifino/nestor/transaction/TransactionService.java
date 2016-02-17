package com.amplifino.nestor.transaction;

import java.util.function.Supplier;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Safe API for UserTransaction
 *
 */
@ProviderType
public interface TransactionService {	
	/**
	 * starts a JTA Transaction that will terminated when close is called on the returned context
	 * @return
	 */
	TransactionContext context();
	/**
	 * executes the argument in a JTA transaction.
	 * If an exception occurs the transaction is rolled back, otherwise committed
	 * @param supplier
	 * @return the value returned by supplier.get()
	 */
	<T> T execute(Supplier<T> supplier);
	/**
	 * runs the argument in a JTA transaction
	 * If an exception occurs the transaction is rolled back, otherwise committed
	 * @param run
	 */
	void execute(Runnable run);
}
