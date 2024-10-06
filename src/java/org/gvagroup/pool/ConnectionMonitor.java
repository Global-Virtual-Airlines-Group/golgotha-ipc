// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2015, 2016, 2020, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import org.gvagroup.tomcat.SharedTask;

/**
 * A daemon to monitor connection pools.
 * @author Luke
 * @version 3.00
 * @param <T> the connection type
 * @since 1.0
 */

class ConnectionMonitor<T extends AutoCloseable> implements SharedTask {

	private static final long serialVersionUID = -5370602877805586773L;
	
	private transient final ConnectionPool<T> _pool;
	private final String _name;
	private final int _sleepTime;
	
	private long _poolCheckCount;
	private boolean _isStopped = false;

	/**
	 * Creates a new Connection Monitor.
	 * @param name the monitor name
	 * @param interval the sleep time <i>in seconds</i>
	 * @param pool the ConnectionPool to monitor
	 */
	ConnectionMonitor(String name, int interval, ConnectionPool<T> pool) {
		super();
		_name = name;
		_pool = pool;
		_sleepTime = Math.min(3600, Math.max(10, interval)) * 1000; // Convert seconds into ms
	}
	
	@Override
	public void stop() {
		_isStopped = true;
	}
	
	@Override
	public boolean isStopped() {
		return _isStopped;
	}

	/**
	 * Returns the number of times the connection pool has been validated.
	 * @return the number of validation runs
	 */
	public long getCheckCount() {
		return _poolCheckCount;
	}
	
	@Override
	public int getInterval() {
		return _sleepTime;
	}

	@Override
	public synchronized void execute() {
		_poolCheckCount++;
		_pool.validate();
	}

	@Override
	public String toString() {
		return String.format("%s %s Connection Monitor", _name, _pool.getType());
	}
}