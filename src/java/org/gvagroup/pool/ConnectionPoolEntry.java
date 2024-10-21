// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2013, 2014, 2015, 2017, 2020, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import org.apache.logging.log4j.*;

/**
 * A class to store connection data in a connection pool and track usage.
 * @author Luke
 * @version 3.02
 * @param <T> the connection type
 * @since 1.0
 */

public abstract class ConnectionPoolEntry<T extends AutoCloseable> implements java.io.Serializable, Comparable<ConnectionPoolEntry<T>> {

	private static final long serialVersionUID = 680599574118396796L;

	/**
	 * Logger.
	 */
	protected transient final Logger log;
	
	private transient ConnectionWrapper<T> _c;
	private transient Recycler<T> _src;
	private StackTrace _stackInfo;
	private final int _id;

	private boolean _inUse = false;
	private boolean _dynamic = false;
	private boolean _connected = false;
	private long _lastThreadID;

	private long _totalTime;
	private long _useTime;
	private long _maxUseTime;
	private long _startTime;
	private long _lastUsed;
	private long _lastChecked;
	
	private int _connectCount;
	private int _checkCount;
	private long _useCount;
	private long _sessionUseCount;

	/**
	 * Create a new Connection Pool entry.
	 * @param id the connection pool entry ID
	 * @param src the connection data source
	 * @param logClass the Logger class
	 */
	protected ConnectionPoolEntry(int id, Recycler<T> src, Class<? extends ConnectionPoolEntry<T>> logClass) {
		super();
		_id = id;
		_src = src;
		log = LogManager.getLogger(logClass);
	}
	
	/**
	 * Returns the connection type.
	 * @return the connection class name
	 */
	public abstract String getType();

	/**
	 * Check if the connection is in use.
	 * @return TRUE if this connection is in use, otherwise FALSE
	 */
	public boolean inUse() {
		return _inUse;
	}

	/**
	 * Returns whether the Connection is active. Inactivte entries are retained for statistics purposes.
	 * @return TRUE if there is a connection, otherwise FALSE
	 */
	boolean isActive() {
		return (_c != null);
	}

	/**
	 * Returns if this connection can be reconnected by a connection monitor, or freed after use.
	 * @return TRUE if the connection can be freed after use, otherwise FALSE
	 */
	public boolean isDynamic() {
		return _dynamic;
	}

	/**
	 * Returns if the entry is connected to the database. Ordinarily one could check if there was a Connection object in
	 * this class, but since Connections are not {@link java.io.Serializable}, this method would not be valid across web
	 * applications.
	 * @return TRUE if connected, otherwise FALSE
	 * @see ConnectionPoolEntry#connect()
	 * @see ConnectionPoolEntry#close()
	 */
	public boolean isConnected() {
		return ((_c != null) || _connected);
	}

	/**
	 * Connects this entry to the data source.
	 * @throws Exception if a JDBC error occurs
	 * @throws IllegalStateException if the entry is already connected
	 */
	abstract void connect() throws Exception;
	
	/**
	 * Marks the entry as free.
	 */
	abstract void free();

	/**
	 * Forcibly Closes the connection, swallowing any errors.
	 */
	void close() {
		try {
			_c.forceClose();
		} catch (Exception e) {
			// empty
		} finally {
			_connected = false;
			_inUse = false;
			_c = null;
		}
	}
	
	/**
	 * Helper method to check for already free connection.
	 * @return if the connection was already freed 
	 */
	protected boolean checkFree() {
		boolean wasFree = !inUse();
		if (wasFree)
			log.warn("Attempting to re-free Connection {}", Integer.valueOf(_id));
		
		return wasFree;
	}
	
	/**
	 * Adjusts connection metrics.
	 */
	protected void markConnected() {
		_connectCount++;
	}

	/**
	 * Adds the usage time to the total for this connection, and marks this entry as free.
	 */
	protected void markFree() {
		_useTime = getUseTime();
		_totalTime += _useTime;
		_maxUseTime = Math.max(_maxUseTime, _useTime);
		_inUse = false;
	}
	
	/**
	 * Initializes usage counters, and marks this entry as busy. 
	 */
	protected void markUsed() {
		_startTime = System.currentTimeMillis();
		_lastUsed = _startTime;
		_inUse = true;
		_useCount++;
		_sessionUseCount++;
		_lastThreadID = Thread.currentThread().threadId();
	}
	
	/**
	 * Tracks the last connection check time.
	 */
	protected void markChecked() {
		_checkCount++;
		_lastChecked = System.currentTimeMillis();
	}

	/**
	 * Validates the connection.
	 * @return TRUE if connected, FALSE if not connected
	 */
	abstract boolean checkConnection();

	/**
	 * Cleans up lingering connection state.
	 * @throws Exception if an error occurs
	 */
	abstract void cleanup() throws Exception;
	
	/**
	 * Returns this entry's connection to its original source.
	 */
	void recycle() {
		_src.release(_c.get());
	}

	/**
	 * Returns the connection object behind this ConnectionPoolEntry. This is package protected since it should only be accessed by the equals() method or the connection pool itself.
	 * @return the Connection
	 * @see ConnectionWrapper#get()
	 */
	T get() {
		return _c.get();
	}
	
	/**
	 * Returns the connection wrapper behind this ConnectionPoolEntry. This is package protected since it should only be
	 * accessed by the conection monitor.
	 * @return the ConnectionWrapper
	 * @see ConnectionMonitor#execute()
	 */
	ConnectionWrapper<T> getWrapper() {
		return _c;
	}
	
	/**
	 * Updates the connection wrapper, marks the entry as connected and clears session usage totals.
	 * @param cw a ConnectionWrapper
	 */
	protected void setWrapper(ConnectionWrapper<T> cw) {
		_lastUsed = System.currentTimeMillis();
		_connected = true;
		_sessionUseCount = 0;
		_c = cw;
	}

	/**
	 * Returns the Conenction Pool Entry id.
	 * @return the entry id
	 */
	public int getID() {
		return _id;
	}

	/**
	 * Marks this connection as dynamic.
	 * @param dynamic TRUE if the connection is dynamic, otherwise FALSE
	 * @see ConnectionPoolEntry#isDynamic()
	 */
	void setDynamic(boolean dynamic) {
		_dynamic = dynamic;
	}

	/**
	 * Checks connection state prior to reservation.
	 */
	protected void checkState() {
		if (inUse())
			throw new IllegalStateException(String.format("Connection %s already in use", toString()));
		if (!isActive())
			throw new IllegalStateException(String.format("Connection %s inactive", toString()));
	}

	/**
	 * Generates the stack trace data.
	 */
	protected void generateStackTrace() {
		try {
			_stackInfo = StackUtils.generate(true);
		} catch (Exception e) {
			log.warn("Cannot fetch stack trace - {}", e.getMessage());
		}
	}
	
	/**
	 * Reserve this Connection pool entry, and get the underlyig connection. This method is package private since
	 * it only should be called by the ConnectionPool object.
	 * @param logStack whether the current thread's stack state should be preserved
	 * @return the Connection object
	 * @throws IllegalStateException if the connection is already reserved
	 */
	abstract T reserve(boolean logStack);

	/**
	 * Returns how long this connection was used the last time.
	 * @return the time this Connection Entry was reserved, in milliseconds
	 */
	public long getUseTime() {
		return inUse() ? (System.currentTimeMillis() - _startTime) : _useTime;
	}
	
	/**
	 * Returns the maximum usage time of this connection entry.
	 * @return the maximum time this Connection Entry was reserved, in milliseconds
	 */
	public long getMaxUseTime() {
		return _maxUseTime;
	}
	
	/**
    * Returns the number of times this Connection slot has reconnected to the data source.
    * @return the number of reconnections
    */
	public int getConnectCount() {
		return _connectCount;
	}
	
	/**
	 * Returns the number of times this Connection has been validated.
	 * @return the number of checks
	 */
	public int getCheckCount() {
		return _checkCount;
	}

	/**
	 * Returns the number of times this connection has been reserved.
	 * @return the number of times reserved
	 */
	public long getUseCount() {
		return _useCount;
	}
	
	/**
	 * Returns the number of times this connection has been reserved since last connected.
	 * @return the number of times reserved
	 */
	public long getSessionUseCount() {
		return _sessionUseCount;	
	}

	/**
	 * Returns the timestamp of this Connection's last validation.
	 * @return the connection's last validation timestamp
	 */
	public long getLastCheckTime() {
		return _lastChecked;
	}
	
	/**
	 * Returns the timestamp of this Connection's last use.
	 * @return the connection's last use timestamp
	 */
	public long getLastUseTime() {
		return _lastUsed;
	}

	/**
	 * Returns how long this connection was used since the Connection pool was started.
	 * @return the total time this Connection Entry was reserved, in milliseconds
	 */
	public long getTotalUseTime() {
		return _totalTime;
	}
	
	/**
	 * Returns the ID of the last thread to use this connection. 
	 * @return the Thread ID
	 */
	public long getLastThreadID() {
		return _lastThreadID;
	}

	/**
	 * Returns this connection's stack trace data, from the last thread to reserve the Connection.
	 * @return a Throwable whose StackTrace is the thread data
	 * @see Throwable#getStackTrace()
	 */
	public StackTrace getStackInfo() {
		return _stackInfo;
	}

	/**
	 * This overrides equals behavior by comparing the underlying connection object. This allows us to get a
	 * ConnectionPoolEntry from the pool when all we get back is the SQL Connection.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o2) {
		return (o2 instanceof ConnectionPoolEntry cpe2) && (compareTo(cpe2) == 0); 
	}

	@Override
	public int compareTo(ConnectionPoolEntry<T> e2) {
		return Integer.compare(_id, e2._id);
	}

	@Override
	public int hashCode() {
		return _id;
	}

	@Override
	public final String toString() {
		StringBuilder buf = new StringBuilder("#").append(_id);
		return buf.toString();
	}
}