package com.amplifino.nestor.transaction.provider.spi;

import java.util.Arrays;
import java.util.UUID;

import javax.transaction.xa.Xid;
import javax.xml.bind.DatatypeConverter;

public final class GlobalTransaction {
	
	private final byte[] id;
	
	private GlobalTransaction(byte[] id) {
		this.id = id;
	}
	
	public byte[] id() {
		return id;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other instanceof GlobalTransaction) {
			return Arrays.equals(id, ((GlobalTransaction) other).id());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(id);
	}
	
	@Override
	public String toString() {
		return "GlobalTransaction: " + DatatypeConverter.printHexBinary(id);
	}
	
	public static GlobalTransaction of(byte[] id) {
		return new GlobalTransaction(id);
	}
	
	public static GlobalTransaction of(Xid xid) {
		return new GlobalTransaction(xid.getGlobalTransactionId());
	}
	
	public static GlobalTransaction random() {
		return GlobalTransaction.of(UUID.randomUUID().toString().getBytes());
	}
}
