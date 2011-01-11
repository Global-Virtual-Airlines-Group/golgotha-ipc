// Copyright 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

import java.sql.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * A user-configurable JDBC Connection Pool.
 * @author Luke
 * @version 1.43
 * @since 1.0
 * @see ConnectionPoolEntry
 * @see ConnectionMonitor
 */

public class ConnectionPool implements java.io.Serializable, Thread.UncaughtExceptionHandler {

	private static final long serialVersionUID = 5092908907485396942L;

	private static transient final Logger log = Logger.getLogger(ConnectionPool.class.getName());

	// The maximum amount of time a connection can be reserved before we consider
	// it to be stale and return it anyways
	static final int MAX_USE_TIME = 110 * 1000;

	private int _poolMaxSize = 1;
	private int _maxRequests;
	private final AtomicLong _totalRequests = new AtomicLong();
	private final AtomicLong _expandCount = new AtomicLong();
	private final AtomicLong _waitCount = new AtomicLong();
	private final AtomicLong _fullCount = new AtomicLong();
	private boolean _logStack;

	private ConnectionMonitor _monitor;
	private final SortedMap<Integer, ConnectionPoolEntry> _cons = new TreeMap<Integer, ConnectionPoolEntry>();
	private transient final BlockingQueue<ConnectionPoolEntry> _idleCons = new PriorityBlockingQueue<ConnectionPoolEntry>(16);
	private transient Thread _monitorThread;

	private transient final Properties _props = new Properties();
	private boolean _autoCommit = true;

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
		DriverManager.setLoginTimeout(5);
		_poolMaxSize = maxSize;
		_monitor = new ConnectionMonitor(name, 30, this);
	}

	/**
	 * Start the Connection Monitor thread.
	 */
	private void startMonitor() {
		_monitorThread = new Thread(_monitor, _monitor.toString());
		_monitorThread.setPriority(Thread.currentThread().getPriority());
		_monitorThread.setDaemon(true);
		_monitorThread.setUncaughtExceptionHandler(this);
		_monitorThread.start();
	}

	/**
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
	public java.util.Date getLastValidation() {
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
	 * Sets a JDBC connection property.
	 * @param propertyName the property name
	 * @param propertyValue the property value
	 */
	public void setProperty(String propertyName, String propertyValue) {
		_props.setProperty(propertyName, propertyValue);
	}

	/**
	 * Sets the maximum number of reservations of a JDBC Connection. After the maximum number of reservations have been
	 * made, the Connection is closed and another one opened in its place.
	 * @param maxReqs the maximum number of reuqests, or 0 to disable
	 */
	public void setMaxRequests(int maxReqs) {
		_maxRequests = maxReqs;
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
	 * Sets the JDBC Driver class name.
	 * @param driverClassName the fully-qualified class name of the JDBC driver
	 * @throws ClassNotFoundException if the class cannot be loaded or is not a JDBC driver
	 */
	public void setDriver(String driverClassName) throws ClassNotFoundException {
		Class<?> c = Class.forName(driverClassName);
		for (int x = 0; x < c.getInterfaces().length; x++) {
			if (c.getInterfaces()[x].getName().equals("java.sql.Driver"))
				return;
		}

		throw new ClassCastException(c.getName() + " does not implement java.sql.Driver");
	}

	/**
	 * Adds a new connection to the connection pool.
	 * @return the new connection pool entry
	 * @param id the Connection ID
	 * @throws SQLException if a JDBC error occurs connecting to the data source.
	 */
	protected ConnectionPoolEntry createConnection(int id) throws SQLException {
		log.info("Connecting to " + _props.getProperty("url") + " as user " + _props.getProperty("user") + " ID #" + id);
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
			if (log.isLoggable(Level.FINER))
				log.finer("Reserving JDBC Connection " + cpe);
			if (!cpe.isActive())
				_expandCount.incrementAndGet();
			
			_totalRequests.incrementAndGet();
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
					_totalRequests.incrementAndGet();
					_expandCount.incrementAndGet();
					return c;
				} catch (SQLException se) {
					throw new ConnectionPoolException(se);
				}
			}
		}

		// Wait for a new connection to become available
		try {
			cpe = _idleCons.poll(1250, TimeUnit.MILLISECONDS);
			if (cpe != null) {
				_waitCount.incrementAndGet();		
				return cpe.reserve(_logStack);
			}
		} catch (InterruptedException ie) {
			log.warning("Interrupted waiting for Connection");
		}

		_fullCount.incrementAndGet();
		throw new ConnectionPoolFullException();
	}

	/**
	 * Returns a JDBC connection to the connection pool. <i>Since the connection may have been returned back to the pool
	 * in the middle of a failed transaction, all pending writes will be rolled back and the autoCommit property of the
	 * JDBC connection reset.</i>
	 * @param c the JDBC connection to return
	 * @return the number of milliseconds the connection was used for
	 */
	public long release(Connection c) {
		if (c == null)
			return 0;

		// Since this connection may have been given to us with pending writes, ROLL THEM BACK
		try {
			c.rollback();
			log.info("Rolling back transactions");
		} catch (SQLException se) {
			log.warning("Error rolling back transaction - " + se.getMessage());
			_monitor.execute();
		}

		// Check that we got a connection wrapper
		if (!(c instanceof ConnectionWrapper)) {
			log.warning("Invalid JDBC Connection returned");
			return 0;
		}

		// Find the connection pool entry and free it
		ConnectionWrapper cw = (ConnectionWrapper) c;
		ConnectionPoolEntry cpe = _cons.get(cw.getID());
		if (cpe == null) {
			log.warning("Invalid JDBC Connection returned - " + cw.getID());
			return 0;
		}

		// Free the connection
		cpe.free();
		long useTime = cpe.getUseTime();

		// If this is a stale dynamic connection, such it down
		if (cpe.isDynamic() && (cpe.getUseCount() > MAX_USE_TIME)) {
			log.logp(Level.SEVERE, ConnectionPool.class.getName(), "release", "Closed stale dynamic JDBC Connection " + cpe + " after " + cpe.getUseTime() + "ms", 
					cpe.getStackInfo());
			cpe.close();
		} else if (!cpe.isDynamic()) {
			if (log.isLoggable(Level.FINER))
				log.finer("Released JDBC Connection " + cpe + " after " + cpe.getUseTime() + "ms");

			// Check if we need to restart
			if ((_maxRequests > 0) && (cpe.getSessionUseCount() > _maxRequests)) {
				log.warning("Restarting JDBC Connection " + cpe + " after " + cpe.getUseCount() + " reservations");
				cpe.close();
				try {
					cpe.connect();
				} catch (SQLException se) {
					log.logp(Level.SEVERE, ConnectionMonitor.class.getName(), "CheckPool", "Cannot reconnect Connection " + cpe, se);
				}
			}
		}

		// Return connection back to the pool
		_idleCons.add(cpe);

		// Return usage time
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
			throw new IllegalArgumentException("Invalid pool size - " + initialSize);

		// Create connections
		try {
			for (int x = 1; x <= initialSize; x++) {
				ConnectionPoolEntry cpe = createConnection(x);
				_cons.put(Integer.valueOf(cpe.getID()), cpe);
				_idleCons.add(cpe);
			}
		} catch (SQLException se) {
			throw new ConnectionPoolException(se);
		}

		startMonitor();
	}

	/**
	 * Disconnects the Connection pool from the JDBC data source.
	 */
	public void close() {
		_monitorThread.interrupt();
		try {
			_monitorThread.join(500);
		} catch (InterruptedException ie) {
			// empty
		}

		// Disconnect the regular connections
		for (Iterator<ConnectionPoolEntry> i = _cons.values().iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = i.next();
			if (cpe.inUse())
				log.warning("Forcibly closing JDBC Connection " + cpe);

			cpe.close();
			i.remove();
		}
	}

	/**
	 * Returns information about the connection pool.
	 * @return a Collection of ConnectionInfo entries
	 */
	public Collection<ConnectionInfo> getPoolInfo() {
		Collection<ConnectionInfo> results = new ArrayList<ConnectionInfo>(_cons.size());
		for (Iterator<ConnectionPoolEntry> i = getEntries().iterator(); i.hasNext();) {
			ConnectionPoolEntry cpe = i.next();
			results.add(new ConnectionInfo(cpe));
		}

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
	 * Connection Monitor uncaught exception handler.
	 */
	public void uncaughtException(Thread t, Throwable e) {
		if (t == _monitorThread)
			startMonitor();
	}
}