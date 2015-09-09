// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * A System Event that can accept an ID.
 * @author Luke
 * @version 1.95
 * @since 1.95
 */

public class IDEvent extends SystemEvent {

	private static final long serialVersionUID = 4567315156127930486L;
	
	private final String _id;

	/**
	 * Creates the Event
	 * @param code
	 * @param id
	 */
	public IDEvent(Type code, String id) {
		super(code);
		_id = id;
	}

	/**
	 * Returns the ID associated with this event.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(super.toString());
		buf.append('-').append(_id);
		return buf.toString();
	}
}