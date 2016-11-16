package com.amplifino.nestor.transaction.provider.recovery;

import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amplifino.nestor.transaction.provider.spi.GlobalTransaction;
import com.amplifino.nestor.transaction.provider.spi.RecoveryService;
import com.amplifino.nestor.transaction.provider.spi.TransactionLog;
import com.amplifino.nestor.transaction.provider.spi.TransactionLog.GlobalTransactionState;

@Component
public class RecoveryServiceImpl implements RecoveryService {

	private Logger logger = Logger.getLogger("com.amplifino.nestor.transaction.provider");
	
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
			if (xid.getFormatId() == log.getFormatId()) {
				// We created the XID
				TransactionLog.GlobalTransactionState state = log.state(GlobalTransaction.of(xid));
				if (state == GlobalTransactionState.INDOUBT_COMMIT) {
					xaResource.commit(xid, false);				
					log.forget(xid);
				} else if (state == GlobalTransactionState.INDOUBT_ROLLBACK) {
					xaResource.rollback(xid);
					log.forget(xid);
				} else {
					logger.info("No action in recovery of Xid " + xid + ", state: " + state);
				}
			}
		}
	}
	
}
