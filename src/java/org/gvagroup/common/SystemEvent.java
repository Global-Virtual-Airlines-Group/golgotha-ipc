// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * An enumeration of valid cross-application events.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public enum SystemEvent {
	
	AIRPORT_RELOAD(1),
	AIRLINE_RELOAD(2),
	USER_SUSPEND(3);
	
	private int _code;

	/**
	 * Creates a System Event.
	 * @param code the event code
	 */
	SystemEvent(int code) {
		_code = code;
	}

	/**
	 * Returns the event code.
	 * @return the code
	 */
	public int code() {
		return _code;
	}
}