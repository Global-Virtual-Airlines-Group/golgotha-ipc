// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

/**
 * A comparator to prioritize persistent connections over dynamic, then prioritize by ID. 
 * @author Luke
 * @version 3.00
 * @since 2.71
 */

class PoolEntryComparator implements java.util.Comparator<ConnectionPoolEntry<?>> {
	
	@Override
	public int compare(ConnectionPoolEntry<?> cpe1, ConnectionPoolEntry<?> cpe2) {
		
		int tmpResult = Boolean.compare(cpe1.isDynamic(), cpe2.isDynamic());
		if ((tmpResult == 0) && cpe1.isDynamic())
			return Integer.compare(cpe1.getID(), cpe2.getID());
		if (tmpResult == 0)
			tmpResult = Long.compare(cpe1.getLastUseTime(), cpe2.getLastUseTime());
		
		return (tmpResult == 0) ? Integer.compare(cpe1.getID(), cpe2.getID()) : tmpResult;
	}
}