package com.amplifino.nestor.associations;

final class SimpleLongAssociation<V> implements LongAssociation<V> {

	private final long key;
	private final V value;
	
	SimpleLongAssociation(long key, V value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public long key() {
		return key;
	}

	@Override
	public V value() {
		return value;
	}
	
	@Override
	public String toString() {
		return key + " -> "  + value;
	}

}
