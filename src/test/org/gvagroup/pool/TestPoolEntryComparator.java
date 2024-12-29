package org.gvagroup.pool;

import java.util.Properties;
import java.util.concurrent.*;

import junit.framework.TestCase;

public class TestPoolEntryComparator extends TestCase {

	private final PoolEntryComparator _cmp = new PoolEntryComparator();
	
	@SuppressWarnings("serial")
	private static class MockJDBCPoolEntry extends JDBCPoolEntry {
		
		MockJDBCPoolEntry(int id, int useCount) {
			super(id, null, new Properties());
			for (int x = 0; x < useCount; x++)
				markUsed();
			
			markFree();
		}
	}
	
	public void testPersistent() {
		
		JDBCPoolEntry e1 = new MockJDBCPoolEntry(1, 0);
		JDBCPoolEntry e2 = new MockJDBCPoolEntry(2, 0);
		
		assertFalse(e1.isDynamic());
		assertFalse(e2.isDynamic());
		assertEquals(-1, _cmp.compare(e1, e2));
		assertEquals(1, _cmp.compare(e2, e1));
	}
	
	public void testDynamic() {
		
		JDBCPoolEntry e1 = new MockJDBCPoolEntry(1, 0);
		e1.setDynamic(true);
		JDBCPoolEntry e2 = new MockJDBCPoolEntry(2, 0);
		e2.setDynamic(true);
		
		assertTrue(e1.isDynamic());
		assertTrue(e2.isDynamic());
		assertEquals(-1, _cmp.compare(e1, e2));
		assertEquals(1, _cmp.compare(e2, e1));
	}

	public void testPersistentDynamic() {
		
		JDBCPoolEntry e1 = new MockJDBCPoolEntry(1, 0);
		e1.setDynamic(true);
		JDBCPoolEntry e2 = new MockJDBCPoolEntry(2, 0);
		e2.setDynamic(false);

		assertTrue(e1.isDynamic());
		assertFalse(e2.isDynamic());
		assertEquals(1, _cmp.compare(e1, e2));
		assertEquals(-1, _cmp.compare(e2, e1));
	}
	
	public void testUsageCount() {
		
		JDBCPoolEntry e1 = new MockJDBCPoolEntry(1, 20);
		JDBCPoolEntry e2 = new MockJDBCPoolEntry(2, 10);
		
		assertEquals(20, e1.getUseCount());
		assertEquals(10, e2.getUseCount());
		assertFalse(e1.isDynamic());
		assertFalse(e2.isDynamic());
		assertTrue(e2.getUseCount() < e1.getUseCount());
		assertEquals(-1, e1.compareTo(e2));
		assertEquals(1, _cmp.compare(e1, e2));
	}
	
	public void testQueue() {
		
		BlockingQueue<ConnectionPoolEntry<?>> _idleCons = new PriorityBlockingQueue<ConnectionPoolEntry<?>>(4, new PoolEntryComparator());
		
		JDBCPoolEntry e1 = new MockJDBCPoolEntry(1, 20);
		JDBCPoolEntry e2 = new MockJDBCPoolEntry(2, 10);
		
		assertEquals(20, e1.getUseCount());
		assertEquals(10, e2.getUseCount());
		assertFalse(e1.isDynamic());
		assertFalse(e2.isDynamic());
		assertTrue(e2.getUseCount() < e1.getUseCount());
		assertEquals(1, _cmp.compare(e1, e2));

		_idleCons.offer(e1);
		_idleCons.offer(e2);
		assertEquals(2, _idleCons.size());
		
		ConnectionPoolEntry<?> e3 = _idleCons.poll();
		assertNotNull(e3);
		assertEquals(2, e3.getID());
	}
}