package com.amplifino.nestor.transaction.provider;

import java.util.UUID;

import javax.transaction.xa.Xid;

class XidImpl implements Xid {
	
	private static final int FORMAT = 0x416d706c;
	private final byte[] globalTransactionId;
	private final byte[] branchQualifier;
	
	XidImpl(byte[] globalTransactionId, byte[] branchQualifier) {
		if (globalTransactionId.length > Xid.MAXGTRIDSIZE) {
			throw new IllegalArgumentException();
		}
		if (branchQualifier.length > Xid.MAXBQUALSIZE) {
			throw new IllegalArgumentException();
		}
		this.globalTransactionId = globalTransactionId;
		this.branchQualifier = branchQualifier;
	}

	@Override
	public byte[] getBranchQualifier() {
		return copy(branchQualifier);
	}

	@Override
	public int getFormatId() {
		return XidImpl.FORMAT;
	}

	@Override
	public byte[] getGlobalTransactionId() {
		return copy(globalTransactionId);
	}
	
	private byte[] copy(byte[] in ) {
		byte[] result = new byte[in.length];
		System.arraycopy(in,  0 , result, 0, in.length);
		return result;
	}

	static byte[] newGlobalTransactionId() {
		return UUID.randomUUID().toString().getBytes();
	}
}
