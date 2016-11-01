package com.amplifino.nestor.transaction.provider;

import javax.transaction.xa.Xid;
import javax.xml.bind.DatatypeConverter;

import com.amplifino.nestor.transaction.provider.spi.GlobalTransaction;

class XidImpl implements Xid {
	
	private final int format;
	private final GlobalTransaction globalTransaction;
	private final byte[] branchQualifier;
	
	XidImpl(int format, GlobalTransaction globalTransaction, byte[] branchQualifier) {
		if (globalTransaction.id().length > Xid.MAXGTRIDSIZE) {
			throw new IllegalArgumentException();
		}
		if (branchQualifier.length > Xid.MAXBQUALSIZE) {
			throw new IllegalArgumentException();
		}
		this.format = format;
		this.globalTransaction = globalTransaction;
		this.branchQualifier = branchQualifier;
	}

	@Override
	public byte[] getBranchQualifier() {
		return copy(branchQualifier);
	}

	@Override
	public int getFormatId() {
		return format;
	}

	@Override
	public byte[] getGlobalTransactionId() {
		return copy(globalTransaction.id());
	}
	
	@Override
	public String toString() {
		return String.format("Xid: format %d, branch %s in %s", 
			format, DatatypeConverter.printHexBinary(branchQualifier), globalTransaction);
	}
	
	private byte[] copy(byte[] in ) {
		byte[] result = new byte[in.length];
		System.arraycopy(in,  0 , result, 0, in.length);
		return result;
	}
}
