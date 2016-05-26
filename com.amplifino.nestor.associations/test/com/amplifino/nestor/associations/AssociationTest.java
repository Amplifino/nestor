package com.amplifino.nestor.associations;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class AssociationTest {

	@Test
	public void testSimple() {
		String key = "key";
		String value = "value";
		Association<String, String> association = Association.of(key, value);
		Assert.assertEquals(key, association.key());
		Assert.assertEquals(value, association.value());
	}
	
	@Test
	public void testLazy() {
		String key = "key";
		String value = "value";
		AtomicInteger invocationCount = new AtomicInteger(0);
		Association<String, String> association = Association.of(key, k -> { invocationCount.incrementAndGet() ; return value; });
		Assert.assertFalse(association.toString().contains(value));
		Assert.assertEquals(key, association.key());
		Assert.assertEquals(value, association.value());
		Assert.assertTrue(association.toString().contains(value));
		Assert.assertEquals(1, invocationCount.intValue());
		association.value();
		Assert.assertEquals(1, invocationCount.intValue());
	}
	
	@Test
	public void testIntSimple() {
		int key = 1;
		String value = "value";
		IntAssociation<String> association = IntAssociation.of(key, value);
		Assert.assertEquals(key, association.key());
		Assert.assertEquals(value, association.value());
	}
	
	@Test
	public void testIntLazy() {
		int key = Integer.MAX_VALUE;
		String value = "value";
		AtomicInteger invocationCount = new AtomicInteger(0);
		IntAssociation<String> association = IntAssociation.of(key, k -> { invocationCount.incrementAndGet() ; return value; });
		Assert.assertFalse(association.toString().contains(value));
		Assert.assertEquals(key, association.key());
		Assert.assertEquals(value, association.value());
		Assert.assertTrue(association.toString().contains(value));
		Assert.assertEquals(1, invocationCount.intValue());
		association.value();
		Assert.assertEquals(1, invocationCount.intValue());
	}
	
	
	@Test
	public void testLongSimple() {
		long key = Long.MAX_VALUE;
		String value = "value";
		LongAssociation<String> association = LongAssociation.of(key, value);
		Assert.assertEquals(key, association.key());
		Assert.assertEquals(value, association.value());
	}
	
	@Test
	public void testLongLazy() {
		long key = Long.MAX_VALUE;
		String value = "value";
		AtomicInteger invocationCount = new AtomicInteger(0);
		LongAssociation<String> association = LongAssociation.of(key, k -> { invocationCount.incrementAndGet(); return value;});
		Assert.assertFalse(association.toString().contains(value));
		Assert.assertEquals(key, association.key());
		Assert.assertEquals(value, association.value());
		Assert.assertTrue(association.toString().contains(value));
		Assert.assertEquals(1, invocationCount.intValue());
		association.value();
		Assert.assertEquals(1, invocationCount.intValue());
	}
	
}
