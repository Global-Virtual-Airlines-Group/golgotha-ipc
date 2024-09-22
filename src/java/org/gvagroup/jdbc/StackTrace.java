// Copyright 2007, 2010, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

/**
 * A class used to track thread stack status when requesting JDBC Connections from a Connection Pool.
 * @author Luke
 * @version 2.71
 * @since 1.0
 */

class StackTrace extends Throwable {
	
	private static final long serialVersionUID = 4608952212833798308L;

	/**
	 * Instantiates the stack trace for the current thread.
	 */
	StackTrace() {
		super(Thread.currentThread().getName());
	}
	
	/**
	 * Retrieves the first element of the stack trace.
	 * @return the first element
	 */
	public String getCaller() {
		StackTraceElement[] stk = getStackTrace();
		if (stk.length == 0) return null;
		
		StackTraceElement st = stk[0];
		return String.format("%s#%s (%s:%d)", st.getClassName(), st.getMethodName(), st.getFileName(), Integer.valueOf(st.getLineNumber()));
	}
}