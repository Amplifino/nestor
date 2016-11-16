package com.amplifino.nestor.transaction.spi;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.transaction.xa.Xid;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import com.amplifino.nestor.transaction.provider.spi.AbortException;
import com.amplifino.nestor.transaction.provider.spi.GlobalTransaction;
import com.amplifino.nestor.transaction.provider.spi.TransactionLog;
import com.amplifino.nestor.transaction.provider.spi.PersistentLog;

@Component
@Designate(ocd=TransactionLogConfiguration.class)
public class TransactionLogImpl implements TransactionLog {
	
	private final Logger logger = Logger.getLogger("com.amplifino.nestor.transaction.spi");
	private final Map<GlobalTransaction, GlobalTransactionState> transactions = new ConcurrentHashMap<>();
	private final AtomicReference<PersistentLog> persistentLogHolder = new AtomicReference<>();
	private int formatId;
	
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL, policy=ReferencePolicy.DYNAMIC, policyOption=ReferencePolicyOption.GREEDY) 
	public void setTransactionLogHelper(PersistentLog logHelper) {
		persistentLogHolder.set(logHelper);
	}
	
	@Activate
	public void activate(TransactionLogConfiguration config) {
		this.formatId = config.formatId();
	}
	public void unsetTransactionLogHelper(PersistentLog logHelper) {
		persistentLogHolder.compareAndSet(logHelper, null);
	}
	
	@Override
	public int getFormatId() {
		return formatId;
	}
	
	@Override
	public void preparing(GlobalTransaction globalTransaction,  Stream<Xid> xids) {
		transactions.put(globalTransaction, GlobalTransactionState.PREPARING);
	}
	
	@Override
	public void committing(GlobalTransaction globalTransaction, Stream<Xid> xids) throws AbortException {
		transactions.put(globalTransaction, GlobalTransactionState.COMITTING);
		PersistentLog persistentLog = persistentLogHolder.get();
		if (persistentLog != null) {
			persistentLog.remember(globalTransaction);
		}
	}
	
	@Override
	public void rollingback(GlobalTransaction globalTransaction, Stream<Xid> xids) {
		transactions.put(globalTransaction, GlobalTransactionState.ROLLBACKING);
	}
	
	@Override
	public void commitInComplete(GlobalTransaction globalTransaction, Stream<Xid> xids) {
		logger.warning(String.format("Commit incomplete for %s with %d in doubt participants", globalTransaction, xids.count()));
		transactions.put(globalTransaction, GlobalTransactionState.INDOUBT_COMMIT);
	}
	
	@Override
	public void rollbackInComplete(GlobalTransaction globalTransaction, Stream<Xid> xids) {
		logger.warning(String.format("Rollback incomplete for %s with %d in doubt participants", globalTransaction, xids.count()));
		transactions.put(globalTransaction, GlobalTransactionState.INDOUBT_ROLLBACK);
	}
	
	@Override
	public void commitComplete(GlobalTransaction globalTransaction) {
		transactions.remove(globalTransaction);
		PersistentLog persistentLog = persistentLogHolder.get();
		if (persistentLog != null) {
			persistentLog.forget(globalTransaction);
		}
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
		GlobalTransactionState state = transactions.get(globalTransaction);
		if (state == null) {
			// transaction not in memory, consult persistent log
			PersistentLog persistentLog = persistentLogHolder.get();
			if (persistentLog == null) {
				// since we do not have a persistent log we do not know the outcome of the tx after restart
				return GlobalTransactionState.UNKNOWN;
			} else {
				if (persistentLog.recalls(globalTransaction)) {
					return GlobalTransactionState.INDOUBT_COMMIT;
				} else {
					// the xid is ours, and we do not have a commit record, so there was no commit decision, hence rollback
					return GlobalTransactionState.INDOUBT_ROLLBACK;
				}
			}
		} else {
			return state;
		}		
 	}

	@Override
	public Stream<Entry<GlobalTransaction, GlobalTransactionState>> activeTransactions() {
		return transactions.entrySet().stream();
	}
	
}
