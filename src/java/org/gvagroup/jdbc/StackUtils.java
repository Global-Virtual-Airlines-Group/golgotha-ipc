// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

import java.util.*;

/**
 * A utility class to capture stack traces.
 * @author Luke
 * @version 1.7
 * @since 1.7
 */

class StackUtils {
	
	private static final String PACKAGE = StackUtils.class.getPackage().getName();

	// singleton
	private StackUtils() {
		super();
	}

	/**
	 * Generates a stack trace with the current package removed.
	 * @return a StackTrace
	 */
	static StackTrace generate() {
		return generate(true);
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
			List<StackTraceElement> el = new ArrayList<StackTraceElement>(Arrays.asList(st.getStackTrace()));
			StackTraceElement ste = el.get(0);
			while (ste.getClassName().startsWith(PACKAGE) && (el.size() > 1)) {
				el.remove(0);
				ste = el.get(0);
			}

			if (el.size() > 1)
				st.setStackTrace(el.toArray(new StackTraceElement[0]));
		}
		
		return st;
	}
}