// Copyright 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * A System Event that can accept an ID.
 * @author Luke
 * @version 2.33
 * @since 1.95
 */

public class IDEvent extends SystemEvent {

	private static final long serialVersionUID = 4567315156127930486L;
	
	private final String _id;
	private final String _data;

	/**
	 * Creates the Event.
	 * @param code the EventType
	 * @param id the ID
	 */
	public IDEvent(EventType code, String id) {
		this(code, id, null);
	}
	
	/**
	 * Creates the Event.
	 * @param code the EventType
	 * @param id the ID
	 * @param data a payload or null if none
	 */
	public IDEvent(EventType code, String id, String data) {
		super(code);
		_id = id;
		_data = data;
	}


	/**
	 * Returns the ID associated with this event.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns the optional payload for this event.
	 * @return the payload or null if none
	 */
	public String getData() {
		return _data;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(super.toString());
		buf.append('-').append(_id);
		if (_data != null)
			buf.append('-').append(_data);
		
		return buf.toString();
	}
}