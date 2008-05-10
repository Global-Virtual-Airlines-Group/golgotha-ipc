// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.common;

/**
 * A cross-application event to track events linked to a specific User.
 * @author Luke
 * @version 1.2
 * @snce 1.2
 */

public class UserEvent extends SystemEvent {

	public static final int USER_SUSPEND = 3;
	public static final int USER_INVALIDATE = 4;
	
	private int _userID;

	/**
	 * Creates an event to suspend a user.
	 * @param userID the user's database ID
	 * @return a UserEvent
	 */
	public static final UserEvent UserSuspend(int userID) {
		return new UserEvent(USER_SUSPEND, userID);
	}
	
	/**
	 * Creates an event to invalidate a cached copy of a user.
	 * @param userID the user's database ID.
	 * @return a UserEvent
	 */
	public static final UserEvent UserInvalidate(int userID) {
		return new UserEvent(USER_INVALIDATE, userID);
	}
	
	/**
	 * Creates the Event.
	 * @param code the Event code
	 * @param userID the user's database ID
	 */
	protected UserEvent(int code, int userID) {
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