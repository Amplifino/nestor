package com.amplifino.nestor.associations;

final class SimpleIntAssociation<V> implements IntAssociation<V> {

	private final int key;
	private final V value;
	
	SimpleIntAssociation(int key, V value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public int key() {
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
