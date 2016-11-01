package com.amplifino.nestor.transaction.spi;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.transaction.xa.Xid;

import org.osgi.service.component.annotations.Component;

import com.amplifino.nestor.transaction.provider.spi.AbortException;
import com.amplifino.nestor.transaction.provider.spi.GlobalTransaction;
import com.amplifino.nestor.transaction.provider.spi.TransactionLog;

@Component
public class TransactionLogImpl implements TransactionLog {
	
	private final Logger logger = Logger.getLogger("com.amplifino.nestor.transaction.spi");
	private final Map<GlobalTransaction, GlobalTransactionState> transactions = new ConcurrentHashMap<>();
	
	@Override
	public int getFormatId() {
		return 0x416d706c;
	}
	
	@Override
	public void preparing(GlobalTransaction globalTransaction,  Stream<Xid> xids) {
		logger.info(String.format("Preparing %s with %d participants", globalTransaction, xids.count()));
		transactions.put(globalTransaction, GlobalTransactionState.PREPARING);
	}
	
	@Override
	public void committing(GlobalTransaction globalTransaction, Stream<Xid> xids) throws AbortException {
		logger.info(String.format("Commiting %s with %d participants", globalTransaction, xids.count()));
		transactions.put(globalTransaction, GlobalTransactionState.COMITTING);
	}
	
	@Override
	public void rollingback(GlobalTransaction globalTransaction, Stream<Xid> xids) {
		logger.info(String.format("Rolling back %s with %d participants", globalTransaction, xids.count()));
		transactions.put(globalTransaction, GlobalTransactionState.ROLLBACKING);
	}
	
	@Override
	public void commitInComplete(GlobalTransaction globalTransaction, Stream<Xid> xids) {
		logger.info(String.format("Commit incomplete for %s with %d in doubt participants", globalTransaction, xids.count()));
		transactions.put(globalTransaction, GlobalTransactionState.INDOUBT_COMMIT);
	}
	
	@Override
	public void rollbackInComplete(GlobalTransaction globalTransaction, Stream<Xid> xids) {
		logger.info(String.format("Rollback incomplete for %s with %d in doubt participants", globalTransaction, xids.count()));
		transactions.put(globalTransaction, GlobalTransactionState.INDOUBT_ROLLBACK);
	}
	
	@Override
	public void commitComplete(GlobalTransaction globalTransaction) {
		logger.info(String.format("Commit complete for %s", globalTransaction));
		transactions.remove(globalTransaction);
	}
	
	@Override
	public void rollbackComplete(GlobalTransaction globalTransaction) {
		logger.info(String.format("Rollback complete for %s", globalTransaction));
		transactions.remove(globalTransaction);
	}
	
	@Override
	public void prepared(Xid xid) {
	}
	
	@Override
	public void committed(Xid xid) {
	}
	
	@Override
	public void rollbacked(Xid xid) {
	}
	
	@Override
	public void forget(Xid xid) {
	}
	
	@Override
	public GlobalTransactionState state(GlobalTransaction globalTransaction) {
		return transactions.getOrDefault(globalTransaction, GlobalTransactionState.UNKNOWN);
 	}

	@Override
	public Stream<Entry<GlobalTransaction, GlobalTransactionState>> activeTransactions() {
		return transactions.entrySet().stream();
	}
	
}
