// Copyright 2007, 2008, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * A class to hold cross-application event data.
 * @author Luke
 * @version 1.50
 * @since 1.0
 */

public class SystemEvent implements java.io.Serializable, Comparable<SystemEvent> {
	
	private static final long serialVersionUID = -6449972707076384490L;
	
	public enum Type {
		AIRPORT_RELOAD, AIRLINE_RELOAD, USER_SUSPEND, USER_INVALIDATE, MVS_RELOAD;
	}
	
	private Type _code;

	/**
	 * Creates a System Event.
	 * @param code the event code
	 */
	public SystemEvent(Type code) {
		super();
		_code = code;
	}

	/**
	 * Returns the event code.
	 * @return the code
	 */
	public Type getCode() {
		return _code;
	}
	
	/**
	 * Compares two events by comparing their codes.
	 */
	public int compareTo(SystemEvent ev2) {
		return _code.compareTo(ev2._code);
	}
	
	public int hashCode() {
		return _code.hashCode();
	}
}