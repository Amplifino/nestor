package com.amplifino.pools;

final class DefaultPoolEntry<T> implements PoolEntry<T>{
	
	private final T pooled;
	private long poolTime;

	DefaultPoolEntry(T pooled) {
		this.pooled = pooled;
		this.poolTime = System.currentTimeMillis();
	}
	
	@Override
	public T get() {
		return pooled;
	}
	
	@Override
	public long age() {
		return System.currentTimeMillis() - poolTime;
	}
	
	@Override
	public boolean older(long age) {
		return age() > age;
	}
	
	
}
