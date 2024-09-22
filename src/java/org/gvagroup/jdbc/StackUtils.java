// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

/**
 * A utility class to capture stack traces.
 * @author Luke
 * @version 2.71
 * @since 1.7
 */

class StackUtils {
	
	private static final String PACKAGE = StackUtils.class.getPackage().getName();

	// singleton
	private StackUtils() {
		super();
	}

	/**
	 * Generates a stack trace.
	 * @param removeCurrentPackage TRUE if stack frames from the current package should be removed, otherwise FALSE
	 * @return a StackTrace
	 */
	static StackTrace generate(boolean removeCurrentPackage) {
		
		StackTrace st = new StackTrace();
		st.fillInStackTrace();
		if (removeCurrentPackage) {
			StackTraceElement[] stk = st.getStackTrace();
			StackTraceElement ste = stk[0]; int idx = 0;
			while (ste.getClassName().startsWith(PACKAGE) && (idx < stk.length))
				ste = stk[++idx];

			if (idx < stk.length) {
				StackTraceElement[] nstk = new StackTraceElement[stk.length - idx];
				System.arraycopy(stk, idx, nstk, 0, stk.length - idx);
				st.setStackTrace(nstk);
			}
		}
		
		return st;
	}
}