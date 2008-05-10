// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * A class to hold cross-application event data.
 * @author Luke
 * @version 1.2
 * @since 1.0
 */

public class SystemEvent implements java.io.Serializable, Comparable<SystemEvent> {
	
	public static final int AIRPORT_RELOAD = 1;
	public static final int AIRLINE_RELOAD = 2;
	
	private int _code;

	/**
	 * Creates an event to reload all Airports.
	 * @return a SystemEvent
	 */
	public static final SystemEvent AirportReload() {
		return new SystemEvent(AIRPORT_RELOAD);
	}
	
	/**
	 * Creates an event to reload all Airlines.
	 * @return a SystemEvent
	 */
	public static final SystemEvent AirlineReload() {
		return new SystemEvent(AIRLINE_RELOAD);
	}
	
	/**
	 * Creates a System Event.
	 * @param code the event code
	 */
	protected SystemEvent(int code) {
		super();
		_code = code;
	}

	/**
	 * Returns the event code.
	 * @return the code
	 */
	public int getCode() {
		return _code;
	}
	
	/**
	 * Compares two events by comparing their codes.
	 */
	public int compareTo(SystemEvent ev2) {
		return Integer.valueOf(_code).compareTo(Integer.valueOf(ev2._code));
	}
	
	public int hashCode() {
		return _code;
	}
}