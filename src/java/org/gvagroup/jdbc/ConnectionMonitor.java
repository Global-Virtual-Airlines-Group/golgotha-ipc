// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2015, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

import java.util.*;
import java.sql.*;
import java.time.Instant;

import org.apache.log4j.Logger;

/**
 * A daemon to monitor JDBC connections.
 * @author Luke
 * @version 2.26
 * @since 1.0
 */

class ConnectionMonitor implements java.io.Serializable, Runnable {

	private static final long serialVersionUID = -5370602877805586773L;
	
	private static transient final Logger log = Logger.getLogger(ConnectionMonitor.class);
	private static final Collection<String> _sqlStatus = List.of("08003", "08S01");

	private transient final ConnectionPool _pool;
	private final String _name;
	private final long _sleepTime;
	
	private long _poolCheckCount;
	private long _lastPoolCheck;

	/**
	 * Creates a new Connection Monitor.
	 * @param name the monitor name
	 * @param interval the sleep time <i>in seconds</i>
	 * @param pool the ConnectionPool to monitor
	 */
	ConnectionMonitor(String name, int interval, ConnectionPool pool) {
		super();
		_name = name;
		_pool = pool;
		_sleepTime = Math.min(3600, Math.max(10, interval)) * 1000; // Convert seconds into ms
	}

	/**
	 * Returns the size of the connection pool being monitored.
	 * @return the size of the pool
	 */
	public int size() {
		return _pool.getSize();
	}
	
	/**
	 * Returns the number of times the connection pool has been validated.
	 * @return the number of validation runs
	 */
	public long getCheckCount() {
		return _poolCheckCount;
	}
	
	/**
	 * Returns the last time the connection pool was validated.
	 * @return the date/time of the last validation run, or null if never
	 */
	public java.time.Instant getLastCheck() {
		return (_lastPoolCheck == 0) ? null : Instant.ofEpochMilli(_lastPoolCheck);
	}

	/**
	 * Alerts the thread to immediately check the connection pool. 
	 */
	synchronized void execute() {
		notify();
	}

	/**
	 * Manually check the connection pool.
	 */
	protected synchronized void checkPool() {
		_poolCheckCount++;
		_lastPoolCheck = System.currentTimeMillis();
		if (log.isDebugEnabled())
			log.debug("Checking Connection Pool");

		// Loop through the entries
		for (ConnectionPoolEntry cpe : _pool.getEntries()) {
			boolean isStale = (cpe.getUseTime() > ConnectionPool.MAX_USE_TIME);
			if (isStale && cpe.isActive()) {
				long useTime = cpe.getUseTime();
				long lastActiveInterval = _lastPoolCheck - cpe.getWrapper().getLastUse();
				if ((useTime - lastActiveInterval) > 1500)
					log.warn("Connection reserved for " + cpe.getUseTime() + "ms, last activity " + lastActiveInterval + "ms ago");
				
				isStale = (lastActiveInterval > 10_000);
			}

			// Check if the entry has timed out
			if (!cpe.isActive()) {
				if (cpe.inUse()) {
					log.warn("Inactive connection " + cpe + " in use!");
					cpe.close(); // Resets last use
				} else if (log.isDebugEnabled())
					log.debug("Skipping inactive connection " + cpe);
			} else if (cpe.inUse() && isStale) {
				log.error("Releasing stale Connection " + cpe, cpe.getStackInfo());
				_pool.release(cpe.getWrapper(), true);
			} else if (cpe.isDynamic() && !cpe.inUse()) {
				if (isStale)
					log.error("Releasing stale dynamic Connection " + cpe, cpe.getStackInfo());
				else
					log.info("Releasing dynamic Connection " + cpe);
				
				cpe.close();
				_pool.addIdle(cpe);
			} else if (cpe.inUse())
				log.info("Connection " + cpe + " in use");
			else if (!cpe.inUse() && !cpe.checkConnection()) {
				log.warn("Reconnecting Connection " + cpe);
				cpe.close();

				try {
					cpe.connect();
					_pool.addIdle(cpe);
				} catch (SQLException se) {
					if (_sqlStatus.contains(se.getSQLState()))
						log.warn("Transient SQL Error - " + se.getSQLState());
					else
						log.warn("Unknown SQL Error code - " + se.getSQLState());
				} catch (Exception e) {
					log.error("Error reconnecting " + cpe, e);
				}
			}
		}
	}

	@Override
	public String toString() {
		return _name + " JDBC Connection Monitor";
	}

	@Override
	public void run() {
		log.info("Starting");
		while (!Thread.currentThread().isInterrupted()) {
			checkPool();
			synchronized (this) {
				try {
					wait(_sleepTime);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		}

		log.info("Stopping");
	}
}