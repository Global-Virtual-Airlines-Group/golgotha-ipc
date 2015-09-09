// Copyright 2008, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * A cross-application event to track events linked to a specific User.
 * @author Luke
 * @version 1.95
 * @snce 1.2
 */

public class UserEvent extends SystemEvent {

	private static final long serialVersionUID = -4485607718271551135L;
	
	private final int _userID;

	/**
	 * Creates the Event.
	 * @param code the Event code
	 * @param userID the user's database ID
	 */
	public UserEvent(Type code, int userID) {
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
	
	/**
	 * Compares two events by comparing their user and event codes.
	 */
	@Override
	public int compareTo(SystemEvent se2) {
		if (se2 instanceof UserEvent) {
			UserEvent ue2 = (UserEvent) se2;
			int tmpResult = Integer.valueOf(_userID).compareTo(Integer.valueOf(ue2._userID));
			if (tmpResult != 0)
				return tmpResult;
		}
		
		return super.compareTo(se2);
	}
}	