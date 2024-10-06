// Copyright 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2015, 2016, 2017, 2020, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import java.io.*;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.*;

import org.gvagroup.tomcat.SharedWorker;

/**
 * A user-configurable Connection Pool.
 * @author Luke
 * @version 3.00
 * @param <T> the Connection type.
 * @since 1.0
 * @see ConnectionPoolEntry
 * @see ConnectionMonitor
 */

public abstract class ConnectionPool<T extends AutoCloseable> implements Serializable, AutoCloseable, org.gvagroup.pool.Recycler<T> {

	private static final long serialVersionUID = 8550734573930973176L;
	
	private transient final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(false); // unfair scheduling to give writer priority
	private final Lock _r = _lock.readLock();
	private final Lock _w = _lock.writeLock();

	/**
	 * Pool logger.
	 */
	protected transient final Logger log;
	
	private final String _name;

	private int _poolMaxSize = 1;
	private int _maxRequests;
	private final LongAdder _totalRequests = new LongAdder();
	private final LongAdder _expandCount = new LongAdder();
	private final LongAdder _waitCount = new LongAdder();
	private final LongAdder _fullCount = new LongAdder();
	private final LongAdder _errorCount = new LongAdder();
	private boolean _logStack;
	private long _lastPoolFullTime;
	private long _lastValidationTime;
	
	private long _maxWaitTime;
	private long _maxBorrowTime;

	private final ConnectionMonitor<T> _monitor;
	private final SortedMap<Integer, ConnectionPoolEntry<T>> _cons = new TreeMap<Integer, ConnectionPoolEntry<T>>();
	private transient final BlockingQueue<ConnectionPoolEntry<T>> _idleCons = new PriorityBlockingQueue<ConnectionPoolEntry<T>>(4, new PoolEntryComparator());

	protected transient final Properties _props = new Properties();
	
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
	 * Creates a new connection pool.
	 * @param maxSize the maximum size of the connection pool
	 * @param name the connection pool name
	 * @param monitorInterval the connection monitor interval in seconds
	 * @param logClass the logging class to use
	 */
	protected ConnectionPool(int maxSize, String name, int monitorInterval, Class<?> logClass) {
		super();
		log = LogManager.getLogger(logClass);
		_name = name;
		_poolMaxSize = maxSize;
		_monitor = new ConnectionMonitor<T>(_name, Math.max(1, monitorInterval), this);
		SharedWorker.register(_monitor);
	}
	
	/**
	 * Returns the connection pool name.
	 * @return the pool name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the connection pool type.
	 * @return the pool type
	 */
	public abstract String getType();
	
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
	 * Returns the amount of time after which a connection is considered stale.
	 * @return the time in milliseconds
	 */
	abstract int getStaleTime();

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
	 * Returns whether stack logging is enabled when a connection is borrowed from the pool.
	 * @return TRUE if stack logging enabled, otherwise FALSE
	 */
	public boolean getLogStack() {
		return _logStack;
	}

	/**
	 * Returns the last time the connection pool was validated.
	 * @return the date/time of the last validation run
	 */
	public java.time.Instant getLastValidation() {
		return (_lastValidationTime == 0) ? null : Instant.ofEpochMilli(_lastValidationTime);
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
	 * Returns the number of times the Connection Pool has detected a state error.
	 * @return the number of state errors
	 */
	public long getErrorCount() {
		return _errorCount.longValue();
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
	 * @return the maximum time a connection was borrowed in milliseconds
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

	/**
	 * Sets the credentials used to connect to the data source.
	 * @param user the User ID
	 * @param pwd the password
	 */
	public void setCredentials(String user, String pwd) {
		_props.setProperty("user", user);
		_props.setProperty("password", pwd);
	}
	
	/**
	 * Sets the maximum number of reservations of a Connection. After the maximum number of reservations have been
	 * made, the Connection is closed and another one opened in its place.
	 * @param maxReqs the maximum number of reuqests, or 0 to disable
	 */
	public void setMaxRequests(int maxReqs) {
		_maxRequests = Math.max(0, maxReqs);
	}

	/**
	 * Sets whether each the thread stack of each thread requesting a connection should be logged for debugging purposes. This requires that a dummy exception be 
	 * thrown on each connection reservation, which may have an adverse effect upon system performance.
	 * @param doLog TRUE if thread state should be logged, otherwise FALSE
	 */
	public void setLogStack(boolean doLog) {
		_logStack = doLog;
	}

	/**
	 * Sets multiple connection properties at once.
	 * @param props the properties to set
	 */
	public void setProperties(Map<?, ?> props) {
		_props.putAll(props);
	}
	
	/**
	 * Adds a new connection to the connection pool.
	 * @return the new connection pool entry
	 * @param id the Connection ID
	 * @throws Exception if an error occurs connecting to the data source
	 */
	protected abstract ConnectionPoolEntry<T> createConnection(int id) throws Exception;
	
	/**
	 * Gets a connection from the connection pool. The size of the connection pool will be increased if the pool is
	 * full but maxSize has not been reached.
	 * @return the connection
	 * @throws ConnectionPoolException if the connection pool is entirely in use
	 */
	public T getConnection() throws ConnectionPoolException {

		// Try and get an idle connection from the pool
		ConnectionPoolEntry<T> cpe = null;
		try {
			_r.lock();
			cpe = _idleCons.poll();
			if (cpe != null) {
				if (cpe.isActive()) {
					T c = cpe.reserve(_logStack);
					log.debug("{} reserve {} - {}", _name, cpe, _idleCons);
					if (!cpe.isActive())
						_expandCount.increment();
				
					_totalRequests.increment();
					return c;
				}
				
				log.warn("{} retrieved idle inactive Connection {}", _name, cpe);
				_errorCount.increment();
				cpe = null;
			}
		} finally {
			_r.unlock();
		}

		// Is the pool at its max size? If not, then create a new connection and add it to the pool
		try {
			_w.lock();
			Optional<ConnectionPoolEntry<T>> nextAvailable = _cons.values().stream().filter(pe -> !pe.inUse()).findFirst();
			if (!nextAvailable.isPresent() && (_cons.size() < _poolMaxSize)) {
				try {
					cpe = createConnection(getNextID());
					cpe.setDynamic(true);
					_cons.put(Integer.valueOf(cpe.getID()), cpe);
				} catch (Exception e) {
					throw new ConnectionPoolException(e);	
				}
			} else if (nextAvailable.isPresent()) {
				cpe = nextAvailable.get();
				if (!cpe.isActive()) {
					try {
						log.info("{} reconnecting Connection {}", _name, cpe);
						cpe.connect();
					} catch (Exception e) {
						throw new ConnectionPoolException(e);
					}
				} else {
					// This may have been returned to the pool between lines 211 and 232
					long useDelta = System.currentTimeMillis() - cpe.getLastUseTime();
					if (useDelta > 10) {
						log.warn("{} active ({} for {}ms) Connection {} not in idle list - {}", _name, Boolean.valueOf(cpe.inUse()), Long.valueOf(useDelta), cpe, cpe.getStackInfo().getCaller());
						_errorCount.increment();
					}
				}
			}
			
			// Return back the connection
			if (cpe != null) {
				T c = cpe.reserve(_logStack);
				_totalRequests.increment();
				_expandCount.increment();
				return c;
			}
		} finally {
			_w.unlock();
		}

		// Wait for a new connection to become available, since we cannot expand
		long waitTime = System.nanoTime();
		try {
			_r.lock();
			cpe = _idleCons.poll(250, TimeUnit.MILLISECONDS);
			if (cpe != null) {
				_waitCount.increment();		
				return cpe.reserve(_logStack);
			}
		} catch (InterruptedException ie) {
			log.warn("Interrupted waiting for Connection");
		} finally {
			_r.unlock();
			waitTime = System.nanoTime() - waitTime;
			long ms = TimeUnit.MILLISECONDS.convert(waitTime, TimeUnit.NANOSECONDS);
			_maxWaitTime = Math.max(_maxWaitTime, ms);
			if (ms > 75)
				log.warn("{} waited {}ms for Connection", _name, Long.valueOf(ms));
		}
		
		// Dump stack if this is our first error in a while
		long now = System.currentTimeMillis();
		if ((now - _lastPoolFullTime) > 5_000) {
			try {
				_r.lock();
				log.error("Pool Full, idleCons = {}", _idleCons);
				for (Map.Entry<Integer, ConnectionPoolEntry<T>> me : _cons.entrySet()) {
					cpe = me.getValue();
					long activeTime = now - cpe.getLastUseTime();
					log.atError().withThrowable(cpe.getStackInfo()).log("Connection {} connected = {}, active = {} ({}ms)", me.getKey(), Boolean.valueOf(cpe.isConnected()), Boolean.valueOf(cpe.isActive()), Long.valueOf(activeTime));
				}
			} finally {
				_r.unlock();
			}
		}
		
		_lastPoolFullTime = now;
		_fullCount.increment();
		throw new ConnectionPoolFullException();
	}
	
	@Override
	public long release(T c) {
		return release(c, false);
	}

	/**
	 * Returns a connection to the connection pool. <i>Since the connection may have been returned back to the pool in the middle of a failed transaction, all pending writes will be 
	 * rolled back and the autoCommit property of the JDBC connection reset.</i>
	 * @param c the connection to return
	 * @param isForced TRUE if a forced close by the connection monitor, otherwise FALSE
	 * @return the number of milliseconds the connection was used for
	 */
	long release(T c, boolean isForced) {
		if (c == null) return 0;

		// Check that we got a connection wrapper
		if (!(c instanceof ConnectionWrapper cw)) {
			log.warn("Invalid Connection returned - {}", c.getClass().getName());
			_errorCount.increment();
			return 0;
		}

		// Find the connection pool entry and free it
		ConnectionPoolEntry<T> cpe = _cons.get(Integer.valueOf(cw.getID()));
		if (cpe == null) {
			log.warn("Invalid Connection returned - {}", Integer.valueOf(cw.getID()));
			_errorCount.increment();
			return 0;
		}
		
		// Do any cleanup
		try {
			cpe.cleanup();
		} catch (Exception e) {
			log.warn("{} error cleaning up {}  - {}", _name, c, e.getMessage());
			_errorCount.increment();
			_monitor.execute();
		}

		// Get use time
		long useTime = cpe.getUseTime();
		_maxBorrowTime = Math.max(_maxBorrowTime, useTime);
		if (isForced)
			log.error("{} forced connection close - Connection {}", _name, cpe);

		// If this is a stale dynamic connection, such it down
		boolean isStale = (useTime > getStaleTime());
		if (cpe.isDynamic() && (isForced || isStale)) {
			log.atError().withThrowable(cpe.getStackInfo()).log("Closed stale dynamic Connection {} after {} ms", cpe, Long.valueOf(useTime));
			cpe.free();
			cpe.close();
			_errorCount.increment();
			return useTime;
		} else if (!cpe.isDynamic()) {
			// Check if we need to restart
			if (isForced || isStale || ((_maxRequests > 0) && (cpe.getSessionUseCount() > _maxRequests))) {
				log.warn("{} restarting Connection {} after {} (total {}) reservations", _name, cpe, Long.valueOf(cpe.getSessionUseCount()), Long.valueOf(cpe.getUseCount()));
				cpe.close();
				try {
					cpe.connect();
				} catch (Exception se) {
					log.atError().withThrowable(se).log("{} cannot reconnect Connection {}", _name, cpe);
					_errorCount.increment();
				}
			}
			
			if (cpe.isConnected())
				addIdle(cpe);
		} else if (cpe.isConnected())
			addIdle(cpe);

		// Return usage time
		log.debug("{} free {} - {} ({}ms)", _name, cpe, _idleCons, Long.valueOf(useTime));
		return useTime;
	}

	/**
	 * Connects the pool to the data source.
	 * @param initialSize the initial number of connections to establish
	 * @throws IllegalArgumentException if initialSize is negative or greater than getMaxSize()
	 * @throws ConnectionPoolException if an error occurs
	 */
	public void connect(int initialSize) throws ConnectionPoolException {
		if ((initialSize < 0) || (initialSize > _poolMaxSize))
			throw new IllegalArgumentException(String.format("Invalid pool size - %d", Integer.valueOf(initialSize)));
		
		// Create connections
		log.info("Opening {} (size={})", _name, Integer.valueOf(initialSize));
		resetMaxTimes();
		try {
			for (int x = 1; x <= initialSize; x++) {
				ConnectionPoolEntry<T> cpe = createConnection(x);
				_cons.put(Integer.valueOf(cpe.getID()), cpe);
				_idleCons.add(cpe);
			}
		} catch (Exception e) {
			throw new ConnectionPoolException(e);
		}
	}

	@Override
	public void close() {
		log.info("Shutting down pool {}", _name);
		_monitor.stop();

		// Disconnect the connections
		try {
			_w.lock();
			for (Iterator<ConnectionPoolEntry<T>> i = _cons.values().iterator(); i.hasNext();) {
				ConnectionPoolEntry<T> cpe = i.next();
				if (cpe.inUse()) {
					try {
						log.warn("Connection {} in use, waiting", cpe);
						Thread.sleep(50);
					} catch (InterruptedException ie) { /* empty */ }
				}
			
				log.log(cpe.inUse() ? Level.WARN : Level.INFO, "Closing {} Connection {}", _name, cpe);
				cpe.close();
				i.remove();
			}
		} finally {
			_w.unlock();
			log.info("Shut down {}", _name);	
		}
	}
	
	/**
	 * Frees a connection entry and returns it back to the list of idle connections. We do the free() here to ensure this occurs when we have the write lock.
	 * @param cpe the ConnectionPoolEntry
	 * @return TRUE if the connection was already present, otherwise FALSE
	 */
	boolean addIdle(ConnectionPoolEntry<T> cpe) {
		try {
			_w.lock();
			if (cpe.inUse()) cpe.free();
			boolean hasCon = _idleCons.remove(cpe);
			_idleCons.add(cpe);
			return hasCon;
		} finally {
			_w.unlock();
		}
	}
	
	/**
	 * Removes a connection entry from to the list of idle connections.
	 * @param cpe the ConnectionPoolEntry
	 * @return TRUE if the connection was not present, otherwise FALSE
	 */
	boolean removeIdle(ConnectionPoolEntry<T> cpe) {
		try {
			_w.lock();
			return !_idleCons.remove(cpe);
		} finally {
			_w.unlock();
		}
	}
	
	/**
	 * Returns information about the connection pool.
	 * @return a Collection of ConnectionInfo entries
	 */
	public Collection<ConnectionInfo> getPoolInfo() {
		try {
			_r.lock();
			return  _cons.values().stream().map(ConnectionInfo::new).collect(Collectors.toList());
		} finally {
			_r.unlock();
		}
	}
	
	/**
	 * Validates the connection pool. This is called by {@link ConnectionMonitor}.
	 */
	void validate() {
		try {
			_lastValidationTime = System.currentTimeMillis();
			_w.lock();
			
			// Check entries and idle entries. Number of free should match idle
			Collection<ConnectionPoolEntry<T>> entries = _cons.values();
			Collection<ConnectionPoolEntry<T>> freeEntries = entries.stream().filter(cpe -> cpe.isActive() && !cpe.inUse()).collect(Collectors.toList());
			long idleCount = _idleCons.stream().filter(ConnectionPoolEntry::isActive).count();
			if ((freeEntries.size() != _idleCons.size()) || (idleCount != _idleCons.size()))
				log.warn("{} Free = {} / {}, IdleCount = {}, Idle = {} / {}", _name, Long.valueOf(freeEntries.size()), freeEntries, Long.valueOf(idleCount), Integer.valueOf(_idleCons.size()), _idleCons);
			
			// Loop through the entries
			for (ConnectionPoolEntry<T> cpe : entries) {
				boolean isStale = (cpe.getUseTime() > getStaleTime());
				if (isStale && cpe.isActive()) {
					long useTime = cpe.getUseTime();
					long lastActiveInterval = _lastValidationTime - cpe.getWrapper().getLastUse();
					if ((useTime - lastActiveInterval) > 15_000)
						log.warn("Connection reserved for {}ms, last activity {}ms ago", Long.valueOf(cpe.getUseTime()), Long.valueOf(lastActiveInterval));
					
					isStale = (lastActiveInterval > getStaleTime());
				}

				// Check if the entry has timed out
				if (!cpe.isActive()) {
					if (cpe.inUse()) {
						log.warn("Inactive connection {} in use", cpe);
						cpe.close(); // Resets last use
					} else
						log.debug("Skipping inactive connection {}", cpe);
				} else if (cpe.inUse() && isStale) {
					@SuppressWarnings("unchecked")
					long useTime = release((T) cpe.getWrapper(), true);
					log.atError().withThrowable(cpe.getStackInfo()).log("{} releasing stale Connection {} after {}ms", _name, cpe, Long.valueOf(useTime));
				} else if (cpe.isDynamic() && !cpe.inUse()) {
					if (isStale)
						log.atError().withThrowable(cpe.getStackInfo()).log("{} releasing stale dynamic Connection {}", _name, cpe);
					else
						log.info("{} releasing dynamic Connection {}", _name, cpe);
					
					cpe.close();
					boolean wasNotIdle = !_idleCons.remove(cpe);
					if (wasNotIdle)
						log.warn("{} attempted to remove non-idle connection {}", _name, cpe);
				} else if (cpe.inUse())
					log.info("Connection {} in use", cpe);
				else if (!cpe.inUse() && !cpe.checkConnection()) {
					log.warn("Reconnecting Connection {}", cpe);
					cpe.close();
					removeIdle(cpe);

					try {
						cpe.connect();
						boolean wasIdle = addIdle(cpe);
						if (wasIdle)
							log.warn("{} returned already idle connection {}", cpe);
					} catch (SQLException se) {
						log.warn("Unknown SQL Error code - {}", se.getSQLState());
					} catch (Exception e) {
						log.atError().withThrowable(e).log("Error reconnecting {}", cpe);
					}
				}
			}
		} finally {
			_w.unlock();
		}
	}
}