package com.amplifino.nestor.associations;

final class SimpleAssociation<K,V> implements Association<K,V> {

	private final K key;
	private final V value;
	
	SimpleAssociation(K key, V  value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K key() {
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
