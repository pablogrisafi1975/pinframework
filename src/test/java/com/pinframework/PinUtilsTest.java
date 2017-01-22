package com.pinframework;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PinUtilsTest {

	@Test
	public void testFullyRead() throws Exception {
	}

	@Test
	public void testCopy() throws Exception {
	}

	@Test
	public void testAsString() throws Exception {
	}

	@Test
	public void testPut() throws Exception {
	}

	@Test
	public void testUrlEncode() throws Exception {
	}

	@Test
	public void testUrlDecode() throws Exception {
	}

	@Test
	public void testGetFirst() throws Exception {
		Map<String, List<String>> map = new HashMap<>();
		map.put("key", Arrays.asList("first", "second"));
		Assert.assertEquals(PinUtils.getFirst(map, "key"), "first");
	}

}
