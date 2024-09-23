package org.gvagroup.pool;

import java.lang.reflect.*;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestReflection extends TestCase {

	public void testStaticMethods() throws Exception {
		
		Class<?> c = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
		assertNotNull(c);
		
		Method m = c.getMethod("shutdown", new Class<?>[] {});
		assertNotNull(m);
		m.invoke(null, new Object[] {});
		
		// Wait for thread to die (up to 500ms)
		Field f = c.getDeclaredField("threadRef");
		assertNotNull(f);
		assertFalse(f.canAccess(null));
		f.setAccessible(true);
		Thread t = (Thread) f.get(null);
		assertNull(t);
	}
}