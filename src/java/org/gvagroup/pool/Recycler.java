// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

/**
 * An interface to mark objects that can cycle pooled connections.
 * @author Luke
 * @version 2.71
 * @param <T> the Connection class
 * @since 2.71
 */

interface Recycler<T> {

	/**
	 * Returns a connection to the pool.
	 * @param c the Connection
	 * @return the number of milliseconds the connection was used for
	 */
	public long release(T c);
}