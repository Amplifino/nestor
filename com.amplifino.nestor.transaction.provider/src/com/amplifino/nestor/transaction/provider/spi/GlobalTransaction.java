package com.amplifino.nestor.transaction.provider.spi;

import java.util.Arrays;
import java.util.UUID;

import javax.transaction.xa.Xid;
import javax.xml.bind.DatatypeConverter;

/**
 * A wrapper around a global transaction id byte array 
 * that supports equals() and hashCode()
 * This class is only meant as a communication between the Transaction Manager and a Transaction log
 *
 */
public final class GlobalTransaction {
	
	private final byte[] id;
	
	private GlobalTransaction(byte[] id) {
		this.id = id;
	}
	
	/**
	 * returns the transaction id
	 * For performance reasons, no defensive copy is made.
	 * Users of this method must not modify the returned byte array in any way.
	 * @return
	 */
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
