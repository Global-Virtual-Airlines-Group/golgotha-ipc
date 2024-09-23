// Copyright 2007, 2009, 2011, 2013, 2014, 2016, 2017, 2020, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

/**
 * A wrapper around connections returned from the Connection Pool. This prevents certain sensitive methods (such as {@link AutoCloseable#close()}) from being called by
 * command code. 
 * @author Luke
 * @version 3.00
 * @param <T> the connection type
 * @since 1.0
 */

public abstract class ConnectionWrapper<T extends AutoCloseable> implements AutoCloseable, Comparable<ConnectionWrapper<T>> {
	
	private final int _id;
	private long _lastUse;
	
	/**
	 * The connection.
	 */
	protected transient final T _c;
	
	/**
	 * The connection pool entry.
	 */
	protected transient final ConnectionPoolEntry<T> _entry;

	/**
	 * Creates the wrapper.
	 * @param c the connection
	 * @param cpe the ConnectionPoolEntry to wrap
	 */
	protected ConnectionWrapper(T c, ConnectionPoolEntry<T> cpe) {
		super();
		_c = c;
		_id = cpe.getID();
		_entry = cpe;
	}
	
	/**
	 * Returns the Connection Pool ID for this Connection.
	 * @return the ID
	 * @see ConnectionPoolEntry#getID()
	 */
	public int getID() {
		return _id;
	}
	
	/**
	 * Returns the last time the underlying JDBC connection was accessed.
	 * @return the last use date/time
	 */
	long getLastUse() {
		return _lastUse;
	}
	
	/**
	 * Helper method to record last connection use.
	 */
	protected void recordLastUse() {
		_lastUse = System.currentTimeMillis();
	}

	@Override
	public void close() {
		recordLastUse();
		_entry.free();
	}
	
	/**
	 * Forces a close of the underlying connection.
	 * @throws Exception if an error occurs
	 */
	void forceClose() throws Exception {
		recordLastUse();
		_c.close();
	}
	
	/**
	 * Returns the unwrapped connection.
	 * @return the connection
	 */
	T getConnection() {
		return _c;
	}
}