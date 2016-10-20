// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2013, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.acars;

import java.util.*;

import org.gvagroup.ipc.IPCInfo;

/**
 * An interface to allow ACARS implementations to return Connection Pool
 * diagnostics and map entries.
 * @author Luke
 * @version 2.10
 * @since 1.0
 */

@SuppressWarnings({ "rawtypes", "javadoc" })
public interface ACARSAdminInfo<RouteEntry> extends IPCInfo {

	/**
	 * Returns all current Connection data.
	 * @param showHidden TRUE if stealth connections should be displayed, otherwise FALSE
	 * @return a Collection of ACARSConnection beans
	 */
	public Collection<byte[]> getPoolInfo(boolean showHidden);

	/**
	 * Returns the positions of all ACARS flights in a serialized fashion,
	 * suitable for transfer between virtual machines and class loaders. Each
	 * element is a byte array which can be fed into an ObjectInputStream for
	 * deserialization.
	 * @return a Collection of byte arrays
	 */
	@Override
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
	public Collection<?> getStatistics();

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
	
	/**
	 * Returns the number of select operations performed on the pool's selector.
	 * @return the number of selects
	 */
	public int getSelectCount();
}