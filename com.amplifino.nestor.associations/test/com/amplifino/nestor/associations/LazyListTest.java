package com.amplifino.nestor.associations;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class LazyListTest {

	@Test
	public void test() {
		List <String> baseList = Arrays.asList("one", "two" , "three");
		List<String> lazyList = LazyList.supplier(() -> baseList);
		Assert.assertEquals(lazyList.size(), baseList.size());
		Assert.assertTrue(lazyList.toString().contains("three"));
		Assert.assertTrue(baseList.equals(lazyList));
		Assert.assertTrue(lazyList.equals(baseList));
		Assert.assertEquals(baseList.hashCode(), lazyList.hashCode());		
	}
}
