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

public interface ConnectionWrapper<T extends AutoCloseable> extends Comparable<ConnectionWrapper<T>> {
	
	/**
	 * Returns the Connection Pool ID for this Connection.
	 * @return the ID
	 * @see ConnectionPoolEntry#getID()
	 */
	public int getID();
	
	/**
	 * Returns the last time the underlying JDBC connection was accessed.
	 * @return the last use date/time
	 */
	long getLastUse();
	
	/**
	 * Returns the unwrapped connection.
	 * @return the connection
	 */
	T get();
	
	/**
	 * Overrides {@link AutoCloseable#close()} to return the connection to the pool.
	 */
	void close();
	
	/**
	 * Forcibly closes the connection.
	 * @throws Exception if an error occurs
	 */
	void forceClose() throws Exception;
	
	/**
	 * Compares two connection wrappers by comparing their ID and last use times.
	 */
	@Override
	default int compareTo(ConnectionWrapper<T> cw) {
		int tmpResult = Integer.compare(getID(), cw.getID());
		return (tmpResult == 0) ? Long.compare(getLastUse(), cw.getLastUse()) : tmpResult;
	}
}