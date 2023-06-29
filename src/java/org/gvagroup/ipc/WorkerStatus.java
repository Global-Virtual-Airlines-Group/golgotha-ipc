// Copyright 2004, 2005, 2006, 2007, 2008, 2010, 2016, 2017, 2023 Global Virtual Airline Group. All Rights Reserved.
package org.gvagroup.ipc;

/**
 * A bean to return worker thread information.
 * @author Luke
 * @version 2.61
 * @since 1.4
 */

public class WorkerStatus implements Comparable<WorkerStatus> {
	
	private long _execStartTime;
	private long _execStopTime;
	
	private final String _name;
	private String _msg;
	private WorkerState _status;
	private final int _sortOrder;
	private long _execCount;
	private boolean _isRunning;
	
	/**
	 * Initializes the bean.
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
	
	public WorkerState getStatus() {
		return _status;
	}
	
	public boolean getAlive() {
		return _isRunning;
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
	
	public synchronized void setStatus(WorkerState newStatus) {
		_status = newStatus;
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
		if (_execStartTime == 0) return 0;
		return ((_execStopTime == 0) ? System.currentTimeMillis() : _execStopTime) - _execStartTime;
	}
	
	/**
	 * Returns the sort order value.
	 * @return the sort order
	 */
	public int getSortOrder() {
		return _sortOrder;
	}
	
	@Override
	public int compareTo(WorkerStatus ws2) {
		int tmpResult = Integer.compare(_sortOrder, ws2._sortOrder);
		return (tmpResult == 0) ? _name.compareTo(ws2._name) : tmpResult; 
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof WorkerStatus ws2) && (compareTo(ws2) == 0);
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