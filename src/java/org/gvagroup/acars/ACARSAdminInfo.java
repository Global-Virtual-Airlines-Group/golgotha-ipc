// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.acars;

import java.util.Collection;

import org.gvagroup.ipc.IPCInfo;

/**
 * An interface to allow ACARS implementations to return Connection Pool diagnostics and map entries.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public interface ACARSAdminInfo<RouteEntry> extends IPCInfo {

	/**
	 * Returns all current Connection data.
	 * @param showHidden TRUE if stealth connections should be displayed, otherwise FALSE
	 * @return a Collection of ACARSConnection beans
	 */
	public Collection<byte[]> getPoolInfo(boolean showHidden);

	/**
	 * Returns the positions of all ACARS flights.
	 * @return a Collection of RouteEntry beans
	 */
	public Collection<RouteEntry> getMapEntries();
	
	/**
	 * Returns the positions of all ACARS flights in a serialized fashion, suitable for transfer between virtual
	 * machines and class loaders. Each element is a byte array which can be fed into an ObjectInputStream
	 * for deserialization.
	 * @return a Collection of byte arrays
	 */
	public Collection<byte[]> getSerializedInfo();

	/**
	 * Returns the curent ACARS Flight IDs.
	 * @return a Collection of Integer flight IDs
	 */
	public Collection<Integer> getFlightIDs();

	/**
	 * Returns connection statisitcs.
	 * @return a Collection of ConnectionStats beans
	 */
	public Collection getStatistics();

	/**
	 * Returns the number of active ACARS Connections.
	 * @return the number of connections
	 */
	public int size();
	
	/**
	 * Returns if there are any dispatch Connections.
	 * @return TRUE if there is at least one dispatch connection, otherwise FALSE
	 */
	public boolean isDispatchOnline();
}