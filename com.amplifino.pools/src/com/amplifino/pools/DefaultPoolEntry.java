package com.amplifino.pools;

final class DefaultPoolEntry<T> implements PoolEntry<T>{
	
	private final T pooled;
	private final long poolTime;
	private final boolean fresh;

	DefaultPoolEntry(T pooled, boolean fresh) {
		this.pooled = pooled;
		this.poolTime = System.currentTimeMillis();
		this.fresh = fresh;
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
	
	@Override
	public boolean isFresh() {
		return fresh;
	}
	
}
