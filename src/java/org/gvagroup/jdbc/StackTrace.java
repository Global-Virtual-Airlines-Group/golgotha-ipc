// Copyright 2007, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

/**
 * A class used to track thread stack status when requesting JDBC Connections from a Connection Pool.
 * @author Luke
 * @version 1.41
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
}