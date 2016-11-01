package com.amplifino.nestor.transaction.provider.recovery;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amplifino.nestor.transaction.provider.spi.GlobalTransaction;
import com.amplifino.nestor.transaction.provider.spi.RecoveryAgent;
import com.amplifino.nestor.transaction.provider.spi.TransactionLog;
import com.amplifino.nestor.transaction.provider.spi.TransactionLog.GlobalTransactionState;

@Component
public class RecoveryAgentImpl implements RecoveryAgent {

	@Reference
	private TransactionLog log;
	
	@Override
	public void recover(XAResource xaResource) {
		try {
			doRecover(xaResource);
		} catch (XAException e) {
			throw new RuntimeException(e);
		}		
	}

	private void doRecover(XAResource xaResource) throws XAException {
		Xid[] xids = xaResource.recover(XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN);
		for (Xid xid : xids) {
			TransactionLog.GlobalTransactionState state = log.state(GlobalTransaction.of(xid));
			if (state == GlobalTransactionState.INDOUBT_COMMIT) {
				xaResource.commit(xid, false);				
				log.forget(xid);
			}
			if (state == GlobalTransactionState.INDOUBT_ROLLBACK) {
				xaResource.rollback(xid);
				log.forget(xid);
			}
		}
	}
	
}
