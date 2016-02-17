package com.amplifino.nestor.transaction;

import org.osgi.annotation.versioning.ProviderType;

/**
 * An AutoClosable that wraps a UserTransaction
 *
 */
@ProviderType
public interface TransactionContext extends AutoCloseable {

	/**
	 * commits the transaction 
	 */
	void commit();
	/**
	 * close() will rollback the transaction if commit has not been called
	 */
	@Override
	void close();
}
