package org.gvagroup.jdbc;

import junit.framework.TestCase;

public class TestStackTrace extends TestCase {

	@SuppressWarnings("static-method")
	public void testCaller() {
		
		StackTrace st = StackUtils.generate(false);
		assertNotNull(st);
		String caller = st.getCaller();
		assertTrue(caller.startsWith("org.gvagroup.jdbc.StackUtils#generate"));
	}
}