// Copyright 2013, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

/**
 * A utility class to capture stack traces.
 * @author Luke
 * @version 3.00
 * @since 1.7
 */

class StackUtils {
	
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
			while (ste.getClassName().contains(".pool.") && (idx < stk.length))
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