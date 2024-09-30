package org.gvagroup.pool;

import java.util.Properties;

import junit.framework.TestCase;

public class TestPoolEntryComparator extends TestCase {

	private final PoolEntryComparator _cmp = new PoolEntryComparator();
	
	public void testPersistent() {
		
		JDBCPoolEntry e1 = new JDBCPoolEntry(1, null, new Properties());
		JDBCPoolEntry e2 = new JDBCPoolEntry(2, null, new Properties());
		
		assertFalse(e1.isDynamic());
		assertFalse(e2.isDynamic());
		assertEquals(-1, _cmp.compare(e1, e2));
		assertEquals(1, _cmp.compare(e2, e1));
	}
	
	public void testDynamic() {
		
		JDBCPoolEntry e1 = new JDBCPoolEntry(1, null, new Properties());
		e1.setDynamic(true);
		JDBCPoolEntry e2 = new JDBCPoolEntry(2, null, new Properties());
		e2.setDynamic(true);
		
		assertTrue(e1.isDynamic());
		assertTrue(e2.isDynamic());
		assertEquals(-1, _cmp.compare(e1, e2));
		assertEquals(1, _cmp.compare(e2, e1));
	}

	public void testPersistentDynamic() {
		
		JDBCPoolEntry e1 = new JDBCPoolEntry(1, null, new Properties());
		e1.setDynamic(true);
		JDBCPoolEntry e2 = new JDBCPoolEntry(2, null, new Properties());
		e2.setDynamic(false);

		assertTrue(e1.isDynamic());
		assertFalse(e2.isDynamic());
		assertEquals(1, _cmp.compare(e1, e2));
		assertEquals(-1, _cmp.compare(e2, e1));
	}
}