package com.amplifino.nestor.transaction.spi;

import javax.transaction.xa.Xid;
import javax.xml.bind.DatatypeConverter;

import org.osgi.service.component.annotations.Component;

import com.amplifino.nestor.transaction.provider.spi.TransactionLog;

@Component
public class TransactionLogImpl implements TransactionLog {

	@Override
	public void remember(byte[] globalTransactionId) {
		System.out.println("Commit decision for " + format(globalTransactionId));
	}

	@Override
	public boolean recognizes(Xid xid) {
		return false;
	}

	@Override
	public void forget(byte[] globalTransactionId) {
		System.out.println("Commit complete for " + format(globalTransactionId));
	}

	@Override
	public void prepared(Xid xid) {	
		System.out.println("Prepared branch " + format(xid.getBranchQualifier()) + " in tx " + format(xid.getGlobalTransactionId()));
	}

	@Override
	public void committed(Xid xid) {
		System.out.println("Committed branch " + format(xid.getBranchQualifier()) + " in tx " + format(xid.getGlobalTransactionId()));
	}
	
	String format(byte[] bytes) {
		return DatatypeConverter.printHexBinary(bytes);
	}

}
