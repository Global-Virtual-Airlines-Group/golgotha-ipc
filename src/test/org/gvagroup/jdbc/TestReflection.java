package org.gvagroup.jdbc;

import java.lang.reflect.*;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestReflection extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Class<?> c = Class.forName("com.mysql.jdbc.AbandonedConnectionCleanupThread");
		assertNotNull(c);
	}

	public void testStaticMethods() throws Exception {
		
		Class<?> c = Class.forName("com.mysql.jdbc.AbandonedConnectionCleanupThread");
		assertNotNull(c);
		
		Method m = c.getMethod("shutdown", new Class<?>[] {});
		assertNotNull(m);
		m.invoke(null, new Object[] {});
		
		// Wait for thread to die (up to 500ms)
		Field f = c.getDeclaredField("threadRef");
		assertNotNull(f);
		assertFalse(f.isAccessible());
		f.setAccessible(true);
		Thread t = (Thread) f.get(null);
		assertNull(t);
	}
}