// Copyright 2007, 2008, 2010, 2011, 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * A class to hold cross-application event data.
 * @author Luke
 * @version 2.33
 * @since 1.0
 */

public class SystemEvent implements java.io.Serializable, Comparable<SystemEvent> {
	
	private static final long serialVersionUID = -4971005909500879396L;

	private final EventType _code;

	/**
	 * Creates a System Event.
	 * @param code the event code
	 */
	public SystemEvent(EventType code) {
		_code = code;
	}

	/**
	 * Returns the event code.
	 * @return the code
	 */
	public EventType getCode() {
		return _code;
	}
	
	@Override
	public int compareTo(SystemEvent ev2) {
		return _code.compareTo(ev2._code);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return _code.toString();
	}
}