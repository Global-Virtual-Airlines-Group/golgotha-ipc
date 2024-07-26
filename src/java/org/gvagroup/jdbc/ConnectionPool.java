// Copyright 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2015, 2016, 2017, 2020, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

import java.sql.*;
import java.util.*;
import java.io.File;
import java.lang.reflect.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.logging.log4j.*;

import org.gvagroup.tomcat.SharedWorker;

/**
 * A user-configurable JDBC Connection Pool.
 * @author Luke
 * @version 2.66
 * @since 1.0
 * @see ConnectionPoolEntry
 * @see ConnectionMonitor
 */

public class ConnectionPool implements java.io.Serializable, java.io.Closeable {

	private static final long serialVersionUID = 5092908907485396942L;

	private static transient final Logger log = LogManager.getLogger(ConnectionPool.class);

	/**
	 * The maximum amount of time a connection can be reserved before we consider it to be stale and return it anyways.
	 */
	static final int MAX_USE_TIME = 145_000;
	
	private final String _name;

	private int _poolMaxSize = 1;
	private int _maxRequests;
	private final LongAdder _totalRequests = new LongAdder();
	private final LongAdder _expandCount = new LongAdder();
	private final LongAdder _waitCount = new LongAdder();
	private final LongAdder _fullCount = new LongAdder();
	private boolean _logStack;
	private transient boolean _isMySQL;
	private long _lastPoolFullTime;
	
	private long _maxWaitTime;
	private long _maxBorrowTime;

	private final ConnectionMonitor _monitor;
	private final SortedMap<Integer, ConnectionPoolEntry> _cons = new TreeMap<Integer, ConnectionPoolEntry>();
	private transient final BlockingQueue<ConnectionPoolEntry> _idleCons = new PriorityBlockingQueue<ConnectionPoolEntry>();

	private transient final Properties _props = new Properties();
	private boolean _autoCommit = true;
	
	/**
	 * Connection Pool full exception.
	 */
	public static class ConnectionPoolFullException extends ConnectionPoolException {
		private static final long serialVersionUID = -1618858703712722475L;

		ConnectionPoolFullException() {
			super("Connection Pool Full");
		}
	}

	/**
	 * Creates a new JDBC connection pool.
	 * @param maxSize the maximum size of the connection pool
	 * @param name the Connection pool size
	 */
	public ConnectionPool(int maxSize, String name) {
		super();
		DriverManager.setLoginTimeout(2);
		_name = name;
		_poolMaxSize = maxSize;
		_monitor = new ConnectionMonitor(_name, 30, this);
		SharedWorker.register(_monitor);
	}

	/*
	 * Get first available connection ID.
	 */
	private int getNextID() {
		return _cons.isEmpty() ? 1 : _cons.lastKey().intValue() + 1;
	}

	/**
	 * Returns the current size of the connection pool.
	 * @return the number of open connections, or -1 if not connected
	 */
	public int getSize() {
		return _cons.size();
	}

	/**
	 * Returns the maximum size of the connection pool.
	 * @return the maximum number of connections that can be opened
	 */
	public int getMaxSize() {
		return _poolMaxSize;
	}

	/**
	 * Returns the number of times the connection pool has been validated.
	 * @return the number of validation runs
	 * @see ConnectionMonitor#getCheckCount()
	 */
	public long getValidations() {
		return _monitor.getCheckCount();
	}

	/**
	 * Returns the last time the connection pool was validated.
	 * @return the date/time of the last validation run
	 * @see ConnectionMonitor#getLastCheck()
	 */
	public java.time.Instant getLastValidation() {
		return _monitor.getLastCheck();
	}

	/**
	 * Updates the database auto-commit setting.
	 * @param commit TRUE if statements are committed automatically, otherwise FALSE
	 */
	public void setAutoCommit(boolean commit) {
		_autoCommit = commit;
	}

	/**
	 * Sets the credentials used to connect to the JDBC data source.
	 * @param user the User ID
	 * @param pwd the password
	 */
	public void setCredentials(String user, String pwd) {
		_props.setProperty("user", user);
		_props.setProperty("password", pwd);
	}
	
	/**
	 * Sets a domain socket to connect to.
	 * @param socketFile the path to the Unix domain socket
	 */
	public void setSocket(String socketFile) {
		if (socketFile == null) return;
		File f = new File(socketFile);
		if (f.exists() && _isMySQL) {
			_props.put("socketFactory", "org.newsclub.net.mysql.AFUNIXDatabaseSocketFactoryCJ");
			_props.put("junixsocket.file", f.getAbsolutePath());
		} else {
			_props.remove("socketFactory");
			_props.remove("junixsocket.file");
		}
	}

	/**
	 * Sets the maximum number of reservations of a JDBC Connection. After the maximum number of reservations have been
	 * made, the Connection is closed and another one opened in its place.
	 * @param maxReqs the maximum number of reuqests, or 0 to disable
	 */
	public void setMaxRequests(int maxReqs) {
		_maxRequests = Math.max(0, maxReqs);
	}

	/**
	 * Sets whether each the thread stack of each thread requesting a connection should be logged for debugging purposes.
	 * This requires that a dummy exception be thrown on each connection reservation, which may have an adverse effect
	 * upon system performance.
	 * @param doLog TRUE if thread state should be logged, otherwise FALSE
	 */
	public void setLogStack(boolean doLog) {
		_logStack = doLog;
	}

	/**
	 * Sets multiple JDBC connection properties at once.
	 * @param props the properties to set
	 */
	public void setProperties(Map<?, ?> props) {
		_props.putAll(props);
	}
	
	/**
	 * Sets the JDBC URL to use.
	 * @param url the JDBC URL
	 */
	public void setURL(String url) {
		_props.put("url", url);
	}

	/**
	 * Sets the JDBC Driver class name.
	 * @param driverClassName the fully-qualified class name of the JDBC driver
	 * @throws ClassNotFoundException if the class cannot be loaded or is not a JDBC driver
	 */
	public void setDriver(String driverClassName) throws ClassNotFoundException {
		Class<?> c = Class.forName(driverClassName);
		_isMySQL = driverClassName.startsWith("com.mysql.cj.jdbc.");
		if (_isMySQL)
			log.info("MySQL JDBC Driver detected");
		
		for (int x = 0; x < c.getInterfaces().length; x++) {
			if (c.getInterfaces()[x].getName().equals("java.sql.Driver"))
				return;
		}

		throw new ClassCastException(String.format("%s does not implement java.sql.Driver", c.getName()));
	}

	/**
	 * Adds a new connection to the connection pool.
	 * @return the new connection pool entry
	 * @param id the Connection ID
	 * @throws SQLException if a JDBC error occurs connecting to the data source.
	 */
	protected ConnectionPoolEntry createConnection(int id) throws SQLException {
		String url = _props.getProperty("junixsocket.file", _props.getProperty("url"));
		log.info("Connecting to {} as user {} ID #{}", url, _props.getProperty("user"), Integer.valueOf(id));
		ConnectionPoolEntry entry = new ConnectionPoolEntry(id, _props);
		entry.setAutoCommit(_autoCommit);
		entry.connect();
		return entry;
	}

	/**
	 * Gets a JDBC connection from the connection pool. The size of the connection pool will be increased if the pool is
	 * full but maxSize has not been reached.
	 * @return the JDBC connection
	 * @throws IllegalStateException if the connection pool is not connected to the JDBC data source
	 * @throws ConnectionPoolException if the connection pool is entirely in use
	 */
	public Connection getConnection() throws ConnectionPoolException {

		// Try and get a connection from the pool
		ConnectionPoolEntry cpe = _idleCons.poll();
		if (cpe != null) {
			Connection c = cpe.reserve(_logStack);
			log.debug("Reserving JDBC Connection {}", cpe);
			if (!cpe.isActive())
				_expandCount.increment();
			
			_totalRequests.increment();
			return c;
		}

		// Is the pool at its max size? If not, then create a new connection and add it to the pool
		synchronized (_cons) {
			if (_cons.size() < _poolMaxSize) {
				try {
					cpe = createConnection(getNextID());
					cpe.setDynamic(true);
					Connection c = cpe.reserve(_logStack);
					_cons.put(Integer.valueOf(cpe.getID()), cpe);

					// Return back the new connection
					_totalRequests.increment();
					_expandCount.increment();
					return c;
				} catch (SQLException se) {
					throw new ConnectionPoolException(se);
				}
			}
		}

		// Wait for a new connection to become available
		long waitTime = System.nanoTime();
		try {
			cpe = _idleCons.poll(950, TimeUnit.MILLISECONDS);
			if (cpe != null) {
				_waitCount.increment();		
				return cpe.reserve(_logStack);
			}
		} catch (InterruptedException ie) {
			log.warn("Interrupted waiting for Connection");
		} finally {
			waitTime = System.nanoTime() - waitTime;
		}

		_maxWaitTime = Math.max(_maxWaitTime, TimeUnit.MILLISECONDS.convert(waitTime, TimeUnit.NANOSECONDS));
		_fullCount.increment();
		
		// Dump stack if this is our first error in a while
		long now = System.currentTimeMillis();
		if ((now - _lastPoolFullTime) > 90_000) {
			log.error("Pool Full, idleCons = {}", _idleCons);
			synchronized (_cons) {
				for (Map.Entry<Integer, ConnectionPoolEntry> me : _cons.entrySet()) {
					cpe = me.getValue();
					log.atError().withThrowable(cpe.getStackInfo()).log("Connection {} connected = {}, active = {}", me.getKey(), Boolean.valueOf(cpe.isConnected()), Boolean.valueOf(cpe.isActive()));
				}
			}
		}
		
		_lastPoolFullTime = now;
		throw new ConnectionPoolFullException();
	}
	
	/**
	 * Returns a connection to the pool.
	 * @param c the Connection
	 * @return the number of milliseconds the connection was used for
	 */
	public long release(Connection c) {
		return release(c, false);
	}

	/**
	 * Returns a JDBC connection to the connection pool. <i>Since the connection may have been returned back to the pool
	 * in the middle of a failed transaction, all pending writes will be rolled back and the autoCommit property of the
	 * JDBC connection reset.</i>
	 * @param c the JDBC connection to return
	 * @param isForced TRUE if a forced close by the connection monitor, otherwise FALSE
	 * @return the number of milliseconds the connection was used for
	 */
	long release(Connection c, boolean isForced) {
		if (c == null) return 0;

		// Since this connection may have been given to us with pending writes, ROLL THEM BACK
		try {
			if (!c.getAutoCommit()) {
				c.rollback();
				log.info("Rolling back transactions");
			}
		} catch (Exception e) {
			log.warn("Error rolling back transaction - {}", e.getMessage());
			_monitor.execute();
		}

		// Check that we got a connection wrapper
		if (!(c instanceof ConnectionWrapper)) {
			log.warn("Invalid JDBC Connection returned - {}", c.getClass().getName());
			return 0;
		}

		// Find the connection pool entry and free it
		ConnectionWrapper cw = (ConnectionWrapper) c;
		ConnectionPoolEntry cpe = _cons.get(Integer.valueOf(cw.getID()));
		if (cpe == null) {
			log.warn("Invalid JDBC Connection returned - {}", Integer.valueOf(cw.getID()));
			return 0;
		}

		// Free the connection and reset last use
		cw.close();
		long useTime = cpe.getUseTime();
		_maxBorrowTime = Math.max(_maxBorrowTime, useTime);
		if (isForced)
			log.error("Forced connection close - JDBC Connection {}", cpe);

		// If this is a stale dynamic connection, such it down
		if (cpe.isDynamic() && (useTime > MAX_USE_TIME)) {
			log.atError().withThrowable(cpe.getStackInfo()).log("Closed stale dynamic JDBC Connection {} after {} ms", cpe, Long.valueOf(useTime));
			cpe.close();
		} else if (!cpe.isDynamic()) {
			log.debug("Released JDBC Connection {} after {}ms", cpe, Long.valueOf(useTime));

			// Check if we need to restart
			if (isForced || ((_maxRequests > 0) && (cpe.getSessionUseCount() > _maxRequests))) {
				log.warn("Restarting JDBC Connection {} after {} (total {}) reservations", cpe, Long.valueOf(cpe.getSessionUseCount()), Long.valueOf(cpe.getUseCount()));
				cpe.close();
				try {
					cpe.connect();
				} catch (SQLException se) {
					log.atError().withThrowable(se).log("Cannot reconnect Connection {}", cpe);
				}
			}
		}

		// Return connection back to the pool and return usage time
		_idleCons.add(cpe);
		return useTime;
	}

	/**
	 * Connects the pool to the JDBC data source.
	 * @param initialSize the initial number of connections to establish
	 * @throws IllegalArgumentException if initialSize is negative or greater than getMaxSize()
	 * @throws ConnectionPoolException if a JDBC error occurs
	 */
	public void connect(int initialSize) throws ConnectionPoolException {
		if ((initialSize < 0) || (initialSize > _poolMaxSize))
			throw new IllegalArgumentException(String.format("Invalid pool size - %d", Integer.valueOf(initialSize)));
		
		// Create connections
		log.info("Opening {} (size={})", _name, Integer.valueOf(initialSize));
		resetMaxTimes();
		try {
			for (int x = 1; x <= initialSize; x++) {
				ConnectionPoolEntry cpe = createConnection(x);
				_cons.put(Integer.valueOf(cpe.getID()), cpe);
				_idleCons.add(cpe);
			}
		} catch (SQLException se) {
			throw new ConnectionPoolException(se);
		}
	}

	@Override
	public void close() {
		log.info("Closing {}", _name);
		_monitor.stop();

		// Disconnect the connections
		for (Iterator<ConnectionPoolEntry> i = _cons.values().iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = i.next();
			if (cpe.inUse()) {
				try {
					log.warn("JDNC Connection {} in use, waiting", cpe);
					Thread.sleep(50);
				} catch (InterruptedException ie) { /* empty */ }
			}
			
			log.log(cpe.inUse() ? Level.WARN : Level.INFO, "Closing {} JDBC Connection {}", _name, cpe);
			cpe.close();
			i.remove();
		}
		
		// MySQL thread shutdown
		if (_isMySQL) {
			log.info("Shutting down MySQL abandoned connection thread");
			try {
				Class<?> c = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
				Method m = c.getMethod("checkedShutdown", new Class<?>[] {});
				m.invoke(null, new Object[] {});
				
				// Wait for thread to die
				Field f = c.getDeclaredField("threadRef"); 
				boolean oldAccess = f.canAccess(null); f.setAccessible(true);
				Object o = f.get(null); f.setAccessible(oldAccess);
				if (o != null) {
					Thread t = (Thread) o; int totalTime = 0;
					log.info("Found thread {} - {}", t.getName(), t.isAlive() ? "Running" : "Terminated");
					while (t.isAlive() && (totalTime < 250)) {
						Thread.sleep(50);
						totalTime += 50;
					}
					
					if (t.isAlive())
						log.warn("{} still running", t.getName());
				}
			} catch (ClassNotFoundException cnfe) {
				log.warn("Cannot load class com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
			} catch (Exception e) {
				log.error("{} shutting down thread - {}", e.getClass().getSimpleName(), e.getMessage());
			}
		}
		
		log.info("Shut down {}", _name);
	}
	
	/**
	 * Adds a connection entry back to the list of idle connections.
	 * @param cpe the ConnectionPoolEntry
	 * @return TRUE if the connection was not present, otherwise FALSE
	 */
	boolean addIdle(ConnectionPoolEntry cpe) {
		synchronized (_cons) {
			boolean hasCon = _idleCons.remove(cpe);
			_idleCons.add(cpe);
			return hasCon;
		}
	}
	
	/**
	 * Returns information about the connection pool.
	 * @return a Collection of ConnectionInfo entries
	 */
	public Collection<ConnectionInfo> getPoolInfo() {
		Collection<ConnectionInfo> results = new ArrayList<ConnectionInfo>(_cons.size() + 2);
		_cons.values().stream().map(ConnectionInfo::new).forEach(results::add);
		return results;
	}

	/**
	 * Returns all connection pool entries, for use by the {@link ConnectionMonitor}.
	 * @return a Collection of ConnectionPoolEntry beans
	 * @see ConnectionMonitor
	 */
	Collection<ConnectionPoolEntry> getEntries() {
		return new ArrayList<ConnectionPoolEntry>(_cons.values());
	}

	/**
	 * Returns the total number of connections handed out by the Connection Pool.
	 * @return the number of connection reservations
	 */
	public long getTotalRequests() {
		return _totalRequests.longValue();
	}

	/**
	 * Returns the number of times the Connection Pool has been full and a request failed.
	 * @return the number of ConnectionPoolFullExceptions thrown
	 */
	public long getFullCount() {
		return _fullCount.longValue();
	}

	/**
	 * Returns the number of times the Connection Pool has been expanded and a dynamic connection returned.
	 * @return the number of times the Connection Pool was expanded
	 */
	public long getExpandCount() {
		return _expandCount.longValue();
	}

	/**
	 * Returns the number of times that a thread has waited for a connection to become available.
	 * @return the number of times a thread has waited
	 */
	public long getWaitCount() {
		return _waitCount.longValue();
	}
	
	/**
	 * Returns the maximum wait time for a connection.
	 * @return the maximum time in milliseconds
	 */
	public long getMaxWaitTime() {
		return _maxWaitTime;
	}
	
	/**
	 * Returns the maximum borrow time for a connection.
	 * @return the maximum time a connection was borrowwed in milliseconds
	 */
	public long getMaxBorrowTime() {
		return _maxBorrowTime;
	}
	
	/**
	 * Resets the maximum borrow and wait times.
	 */
	public void resetMaxTimes() {
		_maxWaitTime = 0;
		_maxBorrowTime = 0;
	}
}