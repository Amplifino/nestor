package com.amplifino.nestor.transaction.provider.spi;

import java.util.Map;
import java.util.stream.Stream;

import javax.transaction.xa.Xid;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Interface for the Transaction Manager to store recovery information on durable storage.
 * All methods called by the TM during normal tx processing are highly performance sensitive.
 * Implementors should be thread safe.
 * It is optional to process information about transaction branches.
 * A minimal implementation only needs to manage globak transaction ids.
 * 
 */
@ProviderType
public interface TransactionLog {

	/**
	 * Implementation should return a unique value that identifies the TM
	 * This helps in recovery to match the in doubt transaction with the correct TM.
	 * @return the format identifier to be used for XID's created by the transaction manager using this log.
	 */
	public int getFormatId();
	
	/**
	 * Signals that the TM will start a two phase commit.
	 * After this call, a call to state with the same globalTransaction
	 * should return PREPARING, and when recovering after a VM crash INDOUBT_ROLLBACK 
	 * @param globalTransaction
	 * @param xids that will be prepared
	 */
	public void preparing(GlobalTransaction globalTransaction, Stream<Xid> xids);
	
	/**
	 * Signals that the TM has decided to commit and enters phase 2
	 * This method will only be called if there are at least two resources left to commit
	 * After this call, a call to state with the same globalTransaction
	 * should return COMITTING, and when recovering after a VM crash INDOUBT_COMMIT
	 * @param globalTransaction
	 * @param xids that will be committed. This is a subset of the xids on the corresponding
	 * preparing call as some resources may have responded with read_only
	 * @throws AbortException should only be thrown if the implementor is in doubt whether
	 * the commit record got logged or not, in other words whether state() for the same global transaction
	 * would return INDOUBT_COMMIT or INDOUBT_ROLLBACK after a VM crash. In this case the TM manager will abort the 2PC 
	 * process leaving all resources in doubt about the outcome of the transaction.
	 * If the Transaction log has an exceptional situation where it knows that it failed to write the 
	 * commit record (in other words state() will return INDOUBT_ROLLBACK), 
	 * it should throw a RuntimeException, and the TM will start the rollback process
	 *  
	 */
	public void committing(GlobalTransaction globalTransaction, Stream<Xid> xids) throws AbortException;
	
	/**
	 * Signals that the TM has decided to rollback the tx.
	 * After this call, a call to state with the same globalTransaction
	 * should return ROLLBACKING, and when recovering after a VM crash INDOUBT_ROLLBACK
	 * @param globalTransaction
	 * @param xids@ that will be rollbacked. This is a subset of the xids on the corresponding
	 * preparing call as some resources may have responded with read_only
	 */
	public void rollingback(GlobalTransaction globalTransaction, Stream<Xid> xids);
	
	/**
	 * Signals that the TM failed to complete commit for some transaction branches
	 * After this call, a call to state with the same globalTransaction
	 * should return INDOUBT_COMMIT, and when recovering after a VM crash INDOUBT_COMMIT
	 * @param globalTransaction
	 * @param xids that threw an exception on commit
	 */
	public void commitInComplete(GlobalTransaction globalTransaction, Stream<Xid> xids);
	
	/**
	 * Signals that the TM failed to complete rollback for some tranaction branches
	 * After this call, a call to state with the same globalTransaction
	 * should return INDOUBT_ROLLBACK, and when recovering after a VM crash INDOUBT_ROLLBACK
	 * @param globalTransaction
	 * @param xids that threw an exception on rollback
	 */
	public void rollbackInComplete(GlobalTransaction globalTransaction, Stream<Xid> xids);
	
	/**
	 * Signals that the TM has successfully completed the 2PC transaction
	 * After this call the return value a call to state with the same globalTransaction is undefined.
	 * 
	 * @param globalTransaction
	 */
	public void commitComplete(GlobalTransaction globalTransaction);
	
	/**
	 * Signals that the TM has completed the rollback the 2PC transaction
	 * After this call the return value a call to state with the same globalTransaction is undefined.
	 * @param globalTransaction
	 */
	public void rollbackComplete(GlobalTransaction globalTransaction);
	
	/**
	 * Notification that the resource for this xid has return ok on prepare 
	 * @param xid
	 */
	public void prepared(Xid xid);
	
	/**
	 * Notification that the resource for this xid has been committed
	 * @param xid
	 */
	public void committed(Xid xid);
	
	/**
	 * Notification that the resource for this xid has been rollbacked
	 * @param xid
	 */
	public void rollbacked(Xid xid);
	
	/**
	 * return the transaction state for this global transaction.
	 * used during the recovery proces.
	 * @param globalTransaction
	 * @return
	 */
	public GlobalTransactionState state(GlobalTransaction globalTransaction);
	
	/**
	 * notification that the resource for this xid has been successfully recovered
	 * @param xid
	 */
	public void forget(Xid xid);
	
	public Stream<Map.Entry<GlobalTransaction, GlobalTransactionState>> activeTransactions();
	
	enum GlobalTransactionState {
		UNKNOWN,
		PREPARING,
		COMITTING,
		ROLLBACKING,
		INDOUBT_COMMIT,
		INDOUBT_ROLLBACK
	}
}
