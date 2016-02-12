package com.amplifino.pools;

class PoolEntry<T> {
	
	private final T pooled;
	private long poolTime;

	PoolEntry(T pooled) {
		this.pooled = pooled;
		this.poolTime = System.currentTimeMillis();
	}
	
	T get() {
		return pooled;
	}
	
	long age() {
		return System.currentTimeMillis() - poolTime;
	}
	
	boolean older(long age) {
		return age() > age;
	}
	
	
}
