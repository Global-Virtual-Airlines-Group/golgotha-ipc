// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.ipc;

/**
 * An enumeration of worker thread states.
 * @author Luke
 * @version 2.12
 * @since 2.12
 */

public enum WorkerState {
	UNKNOWN("Unknown"), SHUTDOWN("Shut Down"), ERROR("Error"), RUNNING("Started"), INIT("Initializing");
	
	private final String _name;
	
	WorkerState(String name) {
		_name = name;
	}
	
	public String getName() {
		return _name;
	}
}