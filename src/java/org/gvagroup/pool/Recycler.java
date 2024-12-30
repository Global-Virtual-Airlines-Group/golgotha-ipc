// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import java.time.Duration;

/**
 * An interface to mark objects that can cycle pooled connections.
 * @author Luke
 * @version 3.10
 * @param <T> the Connection class
 * @since 2.71
 */

interface Recycler<T> {

	/**
	 * Returns a connection to the pool.
	 * @param c the Connection
	 * @return the number of milliseconds the connection was used for
	 */
	public Duration release(T c);
}