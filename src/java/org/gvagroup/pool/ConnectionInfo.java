// Copyright 2005, 2007, 2008, 2009, 2010, 2016, 2017, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import java.time.Instant;

/**
 * A bean to store information about a JDBC connection pool entry.
 * @author Luke
 * @version 3.02
 * @since 1.0
 */

public class ConnectionInfo implements java.io.Serializable, Comparable<ConnectionInfo> {
   
	private static final long serialVersionUID = -2296573022993532611L;
	
	private final int _id;
	private final long _lastThreadID;
	private final String _type;
	private final boolean _isDynamic;
	private final boolean _isConnected;
	private final boolean _inUse;
	private final int _connectCount;
	private final int _checkCount;
	private final long _useCount;
	private final long _sessionUseCount;
	private final long _totalUse;
	private final long _currentUse;
	private final long _maxUse;
	private final Instant _lastUsed;
	private final Instant _lastChecked;
	private final Throwable _trace;

   /**
    * Creates a new ConnectionInfo object from a Connection Pool entry.
    * @param entry the Connection Pool entry.
    */
   ConnectionInfo(ConnectionPoolEntry<?> entry) {
      super();
      _id = entry.getID();
      _lastThreadID = entry.getLastThreadID();
      _type = entry.getType();
      _isDynamic = entry.isDynamic();
      _isConnected = entry.isConnected();
      _inUse = entry.inUse();
      _useCount = entry.getUseCount();
      _checkCount = entry.getCheckCount();
      _connectCount = entry.getConnectCount();
      _sessionUseCount = entry.getSessionUseCount();
      _totalUse = entry.getTotalUseTime();
      _currentUse = entry.getUseTime();
      _maxUse = entry.getMaxUseTime();
      _trace = entry.getStackInfo();
      _lastUsed = (entry.getLastUseTime() > 0) ? Instant.ofEpochMilli(entry.getLastUseTime()) : null;
      _lastChecked = (entry.getLastCheckTime() > 0) ? Instant.ofEpochMilli(entry.getLastCheckTime()) : null;
   }
   
   /**
    * Returns the Connection's ID.
    * @return the connection ID
    */
   public int getID() {
      return _id;
   }
   
   /**
    * Returns the connection type.
    * @return the type name
    */
   public String getType() {
	   return _type;
   }
   
   /**
    * Returns the Connection's stack trace
    * @return a Throwable with the stack trace
    * @see Throwable#getStackTrace()
    */
   public Throwable getStackInfo() {
	   return _trace;
   }
   
   /**
    * Returns the ID of the last Thread to reserve this Connection.
    * @return the Thread ID
    */
   public long getLastThreadID() {
	   return _lastThreadID;
   }
   
   /**
    * Returns if the Connection is currently in use.
    * @return TRUE if the Connection has been reserved, otherwise FALSE
    */
   public boolean getInUse() {
      return _inUse;
   }
   
   /**
    * Returns if the Connection is currently active.
    * @return TRUE if connected, otherwise FALSE
    */
   public boolean getConnected() {
	   return _isConnected;
   }
   
   /**
    * Returns if the Connection is a Dynamic connection.
    * @return TRUE if dynamic, otherwise FALSE
    */
   public boolean getDynamic() {
	   return _isDynamic;
   }
   
   /**
    * Returns the number of times this Connection slot has reconnected to the data source.
    * @return the number of reconnections
    */
   public int getConnectCount() {
	   return _connectCount;
   }
   
   /**
    * Returns the number of times the Connection has been used. 
    * @return the number of times the Connection was reserved
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
    * Returns the number of times the Connection has been validated. 
    * @return the number of times the Connection was checked
    */
   public int getCheckCount() {
	   return _checkCount;
   }
   
   /**
    * Returns the total usage time for the Connection.
    * @return the total usage time in milliseconds
    */
   public long getTotalUse() {
      return _totalUse;
   }
   
   /**
    * Returns the usage time of the last, or current, reservation of this Connection.
    * @return the usage time in milliseconds
    */
   public long getCurrentUse() {
      return _currentUse;
   }
   
   /**
    * Returns the maximum usage time of this Connection.
    * @return the usage time in milliseconds
    */
   public long getMaxUse() {
	   return _maxUse;
   }
   
   /**
    * Returns the last time this connection was reserved.
    * @return the last use date/time, or null if never
    */
   public Instant getLastUsed() {
	   return _lastUsed;
   }
   
   /**
    * Returns the last time this connection was checked.
    * @return the last check date/time, or null if never
    */
   public Instant getLastChecked() {
	   return _lastChecked;
   }
   
   /**
    * Returns the connection type for rendering in a JSP.
    * @return the connection type
    */
   public String getTypeName() {
	   return _isDynamic ? "Dynamic" : "Persistent";
   }

   /**
    * Compares two ConnectionInfo objects by comparing their IDs and usage counts.
    */
   @Override
   public int compareTo(ConnectionInfo ci2) {
      int tmpResult = Integer.compare(_id, ci2._id);
      return (tmpResult == 0) ? Long.compare(_useCount, ci2._useCount) : tmpResult;
   }
   
   @Override
   public int hashCode() {
	   return _id;
   }
}