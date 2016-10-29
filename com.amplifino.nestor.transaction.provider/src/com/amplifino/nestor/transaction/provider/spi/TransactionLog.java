package com.amplifino.nestor.transaction.provider.spi;

import javax.transaction.xa.Xid;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface TransactionLog {

	public void remember(byte[] globalTransactionId);
	public boolean recognizes(Xid xid);
	public void forget(byte[] globalTransactionId);
	
	public void prepared(Xid xid);
	public void committed(Xid xid);
}
