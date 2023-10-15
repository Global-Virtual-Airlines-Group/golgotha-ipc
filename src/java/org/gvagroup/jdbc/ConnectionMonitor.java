// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2015, 2016, 2020, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

import java.util.*;
import java.sql.*;
import java.time.Instant;

import org.apache.logging.log4j.*;

import org.gvagroup.tomcat.SharedTask;

/**
 * A daemon to monitor JDBC connections.
 * @author Luke
 * @version 2.63
 * @since 1.0
 */

class ConnectionMonitor implements SharedTask {

	private static final long serialVersionUID = -5370602877805586773L;
	
	private static transient final Logger log = LogManager.getLogger(ConnectionMonitor.class);
	private static final Collection<String> _sqlStatus = List.of("08003", "08S01");

	private transient final ConnectionPool _pool;
	private final String _name;
	private final int _sleepTime;
	
	private long _poolCheckCount;
	private long _lastPoolCheck;
	private boolean _isStopped = false;

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
	
	@Override
	public void stop() {
		_isStopped = true;
	}
	
	@Override
	public boolean isStopped() {
		return _isStopped;
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
	
	@Override
	public int getInterval() {
		return _sleepTime;
	}

	@Override
	public synchronized void execute() {
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
					log.warn("Connection reserved for {}ms, last activity {}ms ago", Long.valueOf(cpe.getUseTime()), Long.valueOf(lastActiveInterval));
				
				isStale = (lastActiveInterval > 10_000);
			}

			// Check if the entry has timed out
			if (!cpe.isActive()) {
				if (cpe.inUse()) {
					log.warn("Inactive connection {} in use", cpe);
					cpe.close(); // Resets last use
				} else
					log.debug("Skipping inactive connection {}", cpe);
			} else if (cpe.inUse() && isStale) {
				log.atError().withThrowable(cpe.getStackInfo()).log("Releasing stale Connection {}", cpe);
				_pool.release(cpe.getWrapper(), true);
			} else if (cpe.isDynamic() && !cpe.inUse()) {
				if (isStale)
					log.atError().withThrowable(cpe.getStackInfo()).log("Releasing stale dynamic Connection {}", cpe);
				else
					log.info("Releasing dynamic Connection {}", cpe);
				
				cpe.close();
				_pool.addIdle(cpe);
			} else if (cpe.inUse())
				log.info("Connection {} in use", cpe);
			else if (!cpe.inUse() && !cpe.checkConnection()) {
				log.warn("Reconnecting Connection {}", cpe);
				cpe.close();

				try {
					cpe.connect();
					_pool.addIdle(cpe);
				} catch (SQLException se) {
					if (_sqlStatus.contains(se.getSQLState()))
						log.warn("Transient SQL Error - {}", se.getSQLState());
					else
						log.warn("Unknown SQL Error code - {}", se.getSQLState());
				} catch (Exception e) {
					log.atError().withThrowable(e).log("Error reconnecting {}", cpe);
				}
			}
		}
	}

	@Override
	public String toString() {
		return _name + " JDBC Connection Monitor";
	}
}