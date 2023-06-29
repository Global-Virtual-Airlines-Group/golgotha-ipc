// Copyright 2008, 2015, 2017, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * A cross-application event to track events linked to a specific User.
 * @author Luke
 * @version 2.61
 * @snce 1.2
 */

public class UserEvent extends SystemEvent {

	private static final long serialVersionUID = -4485607718271551135L;
	
	private final int _userID;

	/**
	 * Creates the Event.
	 * @param code the EventType
	 * @param userID the user's database ID
	 */
	public UserEvent(EventType code, int userID) {
		super(code);
		_userID = Math.max(0, userID);
	}

	/**
	 * Returns the user's database ID.
	 * @return the database ID
	 */
	public int getUserID() {
		return _userID;
	}
	
	@Override
	public int compareTo(SystemEvent se2) {
		if (se2 instanceof UserEvent ue2) {
			int tmpResult = Integer.compare(_userID, ue2._userID);
			if (tmpResult != 0)
				return tmpResult;
		}
		
		return super.compareTo(se2);
	}
}	