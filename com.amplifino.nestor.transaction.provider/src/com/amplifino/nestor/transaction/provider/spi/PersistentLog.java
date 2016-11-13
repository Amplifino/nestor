package com.amplifino.nestor.transaction.provider.spi;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Helper Interface for Transaction log implementations.
 * This interface only contains the operations 
 * that need to interface with persistent storage that survives VM crashes.
 * 
 */
@ProviderType
public interface PersistentLog {

	
	/**
	 * Signals that the TransactionLog is processing a committing request and needs the global transaction id
	 * saved on persistent storage
	 * After this call, a call to recalls should return true, even after a VM crash
	 * @param globalTransaction
	 * @throws AbortException should only be thrown if the implementor is in doubt whether
	 * the commit record got logged or not, in other words whether recall for the same global transaction
	 * would return true or false after a VM crash. 
	 * If the Transaction log helper has an exceptional situation where it knows that it failed to write the 
	 * commit record (in other words recalls() will return false. 
	 * it should throw a RuntimeException, and the TM will start the rollback process
	 *  
	 */
	public void remember(GlobalTransaction globalTransaction) throws AbortException;
	
	/**
	 * return true if remember was called with the same argument, but no forget was received.
	 * otherwise return false.
	 * Should Only be used during recovery
	 * @param globalTransaction
	 * @return
	 */
	public boolean recalls(GlobalTransaction globalTransaction);
	
	/**
	 * forget the argument. After this call the result of recalls(globalTransaction) is undefined.
	 * @param globalTransaction
	 */
	public void forget(GlobalTransaction globalTransaction);

}
