package com.amplifino.nestor.transaction;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface TransactionContext extends AutoCloseable {

	void commit();
	@Override
	void close();
}
