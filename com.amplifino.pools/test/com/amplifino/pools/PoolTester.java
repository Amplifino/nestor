package com.amplifino.pools;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import com.amplifino.counters.Counts;

public class PoolTester {
	
	@Test
	public void test() {
		Pool<Object> pool = Pool.builder(Object::new).build();
		Object object = pool.borrow();
		pool.release(object);
		Counts counts = pool.counts();
		Assert.assertEquals(1, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(1, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(1, counts.get(Pool.Stats.RELEASES));
		Assert.assertEquals(1, counts.get(Pool.Stats.MAXSIZE));
		pool.close();
	}

	@Test
	public void testInitialSize() {
		final int initialSize = 10;
		Pool<Object> pool = Pool.builder(Object::new).initialSize(initialSize).build();
		Object object = pool.borrow();
		pool.release(object);
		Counts counts = pool.counts();
		Assert.assertEquals(initialSize, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(1, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(1, counts.get(Pool.Stats.RELEASES));
		Assert.assertEquals(initialSize, counts.get(Pool.Stats.MAXSIZE));
		List<Object> objects = IntStream.range(0,  initialSize + 1)
			.mapToObj(i -> pool.borrow())
			.collect(Collectors.toList());
		objects.forEach(pool::release);
		counts = pool.counts();
		Assert.assertEquals(initialSize + 1, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(initialSize + 2, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(initialSize + 2, counts.get(Pool.Stats.RELEASES));
		Assert.assertEquals(initialSize + 1, counts.get(Pool.Stats.MAXSIZE));		
		pool.close();
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testMaxSize() {
		final int maxSize = 10;
		Pool<Object> pool = Pool.builder(Object::new).maxSize(maxSize).maxWait(1, TimeUnit.MILLISECONDS).build();
		try { 
			IntStream.range(0, maxSize  + 1).forEach( i -> pool.borrow());
		} catch (NoSuchElementException e) {
			Counts counts = pool.counts();
			Assert.assertEquals(maxSize, counts.get(Pool.Stats.ALLOCATIONS));
			Assert.assertEquals(maxSize, counts.get(Pool.Stats.BORROWS));
			Assert.assertEquals(1, counts.get(Pool.Stats.TIMEOUTS));
			Assert.assertEquals(1, counts.get(Pool.Stats.FAILURES));
			throw e;
		}
		Assert.fail();
	}
	
	@Test
	public void testSuspend() throws InterruptedException {
		Pool<Object> pool = Pool.builder(Object::new).maxSize(1).build();
		Object lease = pool.borrow();
		Thread thread = new Thread( () -> sleepAndRelease(pool, lease));
		thread.start();
		pool.borrow();
		Counts counts = pool.counts();
		Assert.assertEquals(1, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(2, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(1, counts.get(Pool.Stats.RELEASES));
		Assert.assertEquals(1, counts.get(Pool.Stats.MAXSIZE));
		Assert.assertEquals(1, counts.get(Pool.Stats.SUSPENDS));
	}
	
	private void sleepAndRelease(Pool<Object> pool, Object lease) {
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {			
		}
		pool.release(lease);
	}
	
	@Test
	public void testMaxIdle() {
		final int maxIdle = 5;
		final int borrows = 7;
		final AtomicInteger destroys = new AtomicInteger(0);
		Pool<Object> pool = Pool.builder(Object::new)
				.destroy( object -> destroys.incrementAndGet())
				.maxIdle(5).build();
		List<Object> objects = IntStream.range(0,  borrows)
				.mapToObj(i -> pool.borrow())
				.collect(Collectors.toList());
		objects.forEach(pool::release);
		Counts counts = pool.counts();
		Assert.assertEquals(borrows, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(borrows, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(borrows, counts.get(Pool.Stats.RELEASES));
		Assert.assertEquals(borrows, counts.get(Pool.Stats.MAXSIZE));
		Assert.assertEquals(borrows - maxIdle, counts.get(Pool.Stats.DESTROYS));
		Assert.assertEquals(borrows - maxIdle, destroys.get());
	}
	
	@Test
	public void testWait() throws InterruptedException {
		Pool<Object> pool = Pool.builder(Object::new).destroy( o -> {throw new IllegalArgumentException();}).build();
		Object lease = pool.borrow();
		pool.close();
		new Thread(() -> sleepAndRelease(pool, lease)).start();
		pool.await();
	}
	
	@Test
	public void testWaitWithBorrow() throws InterruptedException {
		Pool<Object> pool = Pool.builder(Object::new).build();
		Object borrow = pool.borrow();
		pool.close();
		Assert.assertFalse(pool.await(1, TimeUnit.MILLISECONDS));
		pool.release(borrow);
		Assert.assertTrue(pool.await(1, TimeUnit.MILLISECONDS));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testClose()  {
		Pool<Object> pool = Pool.builder(Object::new).build();
		List<Object> leases = IntStream.range(0, 10)
			.mapToObj(i -> pool.borrow())
			.collect(Collectors.toList());
		leases.forEach( lease -> new Thread(() -> sleepAndRelease(pool, lease)).start());
		pool.close();
		pool.borrow();		
	}
	
	@Test
	public void testOnBorrow() {
		Iterator<Integer> iterator = IntStream.range(0, Integer.MAX_VALUE).iterator();
		Pool<Integer> pool = Pool.builder(iterator::next)
			.onBorrow(i -> ((i.intValue() % 3) != 0))
			.build();
		Assert.assertTrue(IntStream.range(0, 10)
				.mapToObj( i -> pool.borrow())
				.allMatch(i -> ((i.intValue() % 3) != 0)));
		Counts counts = pool.counts();
		Assert.assertEquals(15, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(10, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(5, counts.get(Pool.Stats.DESTROYS));
		Assert.assertEquals(5, counts.get(Pool.Stats.INVALIDONBORROW));				
	}
	
	@Test
	public void testMinIdleTime() {
		Iterator<Integer> iterator = IntStream.range(0, Integer.MAX_VALUE).iterator();
		Pool<Integer> pool = Pool.builder(iterator::next)
			.minIdleTime(1L, TimeUnit.SECONDS)
			.onBorrow(i -> false)
			.build();
		IntStream.range(0, 10)
			.mapToObj( i -> pool.borrow())
			.collect(Collectors.toList());
		Counts counts = pool.counts();
		Assert.assertEquals(10, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(10, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(0, counts.get(Pool.Stats.DESTROYS));
		Assert.assertEquals(0, counts.get(Pool.Stats.INVALIDONBORROW));				
	}
	
	@Test
	public void testOnRelease() {
		Pool<Object> pool = Pool.builder(Object::new)
			.onRelease( obj -> false)
			.build();
		Object lease = pool.borrow();
		pool.release(lease);
		lease = pool.borrow();
		Counts counts = pool.counts();
		Assert.assertEquals(2, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(2, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(1, counts.get(Pool.Stats.RELEASES));
		Assert.assertEquals(1, counts.get(Pool.Stats.DESTROYS));
		Assert.assertEquals(1, counts.get(Pool.Stats.INVALIDONRELEASE));				
	}
	
	@Test
	public void testFifo() {
		Iterator<Integer> iterator = IntStream.range(0, Integer.MAX_VALUE).iterator();
		Pool<Integer> pool = Pool.builder(iterator::next)
			.fifo()
			.build();
		List<Integer> leases = IntStream.range(0, 10)
				.mapToObj(i -> pool.borrow())
				.collect(Collectors.toList());
		leases.forEach(pool::release);
		Integer lease = pool.borrow();
		Assert.assertEquals(0, lease.intValue());
	}
	
	@Test
	public void testLifo() {
		Iterator<Integer> iterator = IntStream.range(0, Integer.MAX_VALUE).iterator();
		Pool<Integer> pool = Pool.builder(iterator::next)
			.lifo()
			.build();
		List<Integer> leases = IntStream.range(0, 10)
				.mapToObj(i -> pool.borrow())
				.collect(Collectors.toList());
		leases.forEach(pool::release);
		Integer lease = pool.borrow();
		Assert.assertEquals(9, lease.intValue());
	}
	
	@Test
	public void testMaxIdleTime() throws InterruptedException {	
		Pool<Object> pool = Pool.builder(Object::new).maxIdleTime(10, TimeUnit.MILLISECONDS).build();
		Object object = pool.borrow();
		pool.release(object);
		Thread.sleep(1000L);
		object = pool.borrow();
		pool.release(object);
		Counts counts = pool.counts();
		Assert.assertEquals(2, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(2, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(2, counts.get(Pool.Stats.RELEASES));
		Assert.assertEquals(1, counts.get(Pool.Stats.MAXSIZE));
		Assert.assertEquals(1, counts.get(Pool.Stats.IDLETIMEEXCEEDED));
	}
	
	@Test
	public void testEviction() {
		Pool<Object> pool = Pool.builder(Object::new).build();
		Object object = pool.borrow();
		pool.evict(object);
		Counts counts = pool.counts();
		Assert.assertEquals(1, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(1, counts.get(Pool.Stats.DESTROYS));
		Assert.assertEquals(1, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(1, counts.get(Pool.Stats.RELEASES));
		Assert.assertEquals(1, counts.get(Pool.Stats.EVICTIONS));
	}
	
	@Test
	public void testFailureEviction() {
		Pool<Object> pool = Pool.builder(() -> { throw new RuntimeException(); }).build();
		try {
			pool.borrow();
			Assert.fail();
		} catch (RuntimeException e) {
		}
		Counts counts = pool.counts();
		Assert.assertEquals(1, counts.get(Pool.Stats.FAILURES));
		Assert.assertEquals(0, counts.get(Pool.Stats.ALLOCATIONS));
		Assert.assertEquals(0, counts.get(Pool.Stats.BORROWS));
		Assert.assertEquals(0, counts.get(Pool.Stats.RELEASES));
	}
	
}
