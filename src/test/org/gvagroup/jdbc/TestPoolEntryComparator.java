package org.gvagroup.jdbc;

import java.util.Properties;

import junit.framework.TestCase;

public class TestPoolEntryComparator extends TestCase {

	private final PoolEntryComparator _cmp = new PoolEntryComparator();
	
	public void testPersistent() {
		
		ConnectionPoolEntry e1 = new ConnectionPoolEntry(1, new Properties());
		ConnectionPoolEntry e2 = new ConnectionPoolEntry(2, new Properties());
		
		assertFalse(e1.isDynamic());
		assertFalse(e2.isDynamic());
		assertEquals(-1, _cmp.compare(e1, e2));
		assertEquals(1, _cmp.compare(e2, e1));
	}
	
	public void testDynamic() {
		
		ConnectionPoolEntry e1 = new ConnectionPoolEntry(1, new Properties());
		e1.setDynamic(true);
		ConnectionPoolEntry e2 = new ConnectionPoolEntry(2, new Properties());
		e2.setDynamic(true);
		
		assertTrue(e1.isDynamic());
		assertTrue(e2.isDynamic());
		assertEquals(-1, _cmp.compare(e1, e2));
		assertEquals(1, _cmp.compare(e2, e1));
	}

	public void testPersistentDynamic() {
		
		ConnectionPoolEntry e1 = new ConnectionPoolEntry(1, new Properties());
		e1.setDynamic(true);
		ConnectionPoolEntry e2 = new ConnectionPoolEntry(2, new Properties());
		e2.setDynamic(false);

		assertTrue(e1.isDynamic());
		assertFalse(e2.isDynamic());
		assertEquals(1, _cmp.compare(e1, e2));
		assertEquals(-1, _cmp.compare(e2, e1));
	}
}