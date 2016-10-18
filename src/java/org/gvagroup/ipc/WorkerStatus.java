// Copyright 2004, 2005, 2006, 2007, 2008, 2010, 2016 Global Virtual Airline Group. All Rights Reserved.
package org.gvagroup.ipc;

/**
 * A bean to return worker thread information.
 * @author Luke
 * @version 2.10
 * @since 1.4
 */

public class WorkerStatus implements Comparable<WorkerStatus> {
	
	public static final int STATUS_UNKNOWN = 0;
	public static final int STATUS_SHUTDOWN = 1;
	public static final int STATUS_ERROR = 3;
	public static final int STATUS_START = 4;
	public static final int STATUS_INIT = 5;
	
	public static final String[] STATUS_NAME = {"Unknown", "Shutdown", "?", "Error", "Started", "Initializing" };
	
	private long _execStartTime;
	private long _execStopTime;
	
	private String _name;
	private String _msg;
	private int _status;
	private int _sortOrder;
	private long _execCount;
	private boolean _isRunning;
	
	/**
	 * Initializes the bean
	 * @param name the worker name
	 * @param sortOrder the sorting order
	 */
	public WorkerStatus(String name, int sortOrder) {
		super();
		_name = name;
		_sortOrder = Math.max(0, sortOrder);
	}

	public synchronized String getMessage() {
		return _msg;
	}
	
	public synchronized int getStatus() {
		return _status;
	}
	
	public boolean getAlive() {
		return _isRunning;
	}
	
	public String getStatusName() {
		return STATUS_NAME[getStatus()];
	}
	
	public long getExecutionCount() {
		return _execCount;
	}
	
	public synchronized void setMessage(String msg) {
		_msg = msg;
	}
	
	public void setAlive(boolean isAlive) {
		_isRunning = isAlive;
	}
	
	public synchronized void setStatus(int newStatus) {
		if ((newStatus >= 0) && (newStatus < STATUS_NAME.length)) {
			_status = newStatus;
		} else {
			_status = STATUS_UNKNOWN;
		}
	}
	
	public synchronized void execute() {
		_execStartTime = System.currentTimeMillis();
		_execStopTime = 0;
	}
	
	public synchronized void complete() {
		_execStopTime = System.currentTimeMillis();
		_execCount++;
	}
	
	public synchronized long getExecutionTime() {
		if (_execStartTime == 0)
			return 0;
		
		return ((_execStopTime == 0) ? System.currentTimeMillis() : _execStopTime) - _execStartTime;
	}
	
	/**
	 * Returns the sort order value.
	 * @return the sort order
	 */
	public int getSortOrder() {
		return _sortOrder;
	}
	
	/**
	 * Compares two workers by comparing their sort ordering and names.
	 */
	@Override
	public int compareTo(WorkerStatus ws2) {
		int tmpResult = Integer.valueOf(_sortOrder).compareTo(Integer.valueOf(ws2._sortOrder));
		return (tmpResult == 0) ? _name.compareTo(ws2._name) : tmpResult; 
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof WorkerStatus) && (compareTo((WorkerStatus) o) == 0);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return _name;
	}
}