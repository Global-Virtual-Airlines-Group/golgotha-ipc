// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

import java.util.*;
import java.sql.*;
import java.util.logging.*;

/**
 * A daemon to monitor JDBC connections.
 * @author Luke
 * @version 1.45
 * @since 1.0
 */

class ConnectionMonitor implements java.io.Serializable, Runnable {

	private static final long serialVersionUID = 730339303146022570L;
	
	private static transient final Logger log = Logger.getLogger(ConnectionMonitor.class.getName());
	private static final Collection<String> _sqlStatus = Arrays.asList("08003", "08S01");

	private transient ConnectionPool _pool;
	private String _name = "";
	private long _sleepTime;
	
	private long _poolCheckCount;
	private long _lastPoolCheck;

	/**
	 * Creates a new Connection Monitor.
	 * @param the monitor name
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
	public java.util.Date getLastCheck() {
		return (_lastPoolCheck == 0) ? null : new java.util.Date(_lastPoolCheck);
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
		if (log.isLoggable(Level.FINER))
			log.finer("Checking Connection Pool");

		// Loop through the entries
		for (Iterator<ConnectionPoolEntry> i = _pool.getEntries().iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = i.next();
			boolean isStale = (cpe.getUseTime() > ConnectionPool.MAX_USE_TIME);

			// Check if the entry has timed out
			if (!cpe.isActive()) {
				if (cpe.inUse()) {
					log.warning("Inactive connection " + cpe + " in use!");
					cpe.free();
				} else if (log.isLoggable(Level.FINER))
					log.finer("Skipping inactive connection " + cpe);
			} else if (cpe.inUse() && isStale) {
				log.logp(Level.SEVERE, ConnectionMonitor.class.getName(), "CheckPool", "Releasing stale Connection " + cpe, cpe.getStackInfo());
				_pool.release(cpe.getWrapper());
			} else if (cpe.isDynamic() && !cpe.inUse()) {
				if (isStale)
					log.logp(Level.SEVERE, ConnectionMonitor.class.getName(), "CheckPool", "Releasing stale dynamic Connection " + cpe, cpe.getStackInfo());
				else
					log.info("Releasing dynamic Connection " + cpe);
				
				cpe.close();
				_pool.addIdle(cpe);
			} else if (cpe.inUse())
				log.info("Connection " + cpe + " in use");
			else if (!cpe.inUse() && !cpe.checkConnection()) {
				log.warning("Reconnecting Connection " + cpe);
				cpe.close();

				try {
					cpe.connect();
					_pool.addIdle(cpe);
				} catch (SQLException se) {
					if (_sqlStatus.contains(se.getSQLState()))
						log.warning("Transient SQL Error - " + se.getSQLState());
					else
						log.warning("Unknown SQL Error code - " + se.getSQLState());
				} catch (Exception e) {
					log.logp(Level.SEVERE, ConnectionMonitor.class.getName(), "CheckPool", "Error reconnecting " + cpe, e);
				}
			} else {
				boolean needsAdded = _pool.addIdle(cpe);
				if (needsAdded)
					log.info("Returning idle Connection " + cpe + " to queue");
			}
		}
	}

	/**
	 * Returns the thread name.
	 */
	public String toString() {
		return _name + " JDBC Connection Monitor";
	}

	/**
	 * Executes the Thread.
	 */
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