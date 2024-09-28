// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import redis.clients.jedis.Jedis;

/**
 * A connection wrapper class for Jedis connections.
 * @author Luke
 * @version 3.00
 * @since 3.00
 */

public class JedisConnectionWrapper extends ConnectionWrapper<Jedis> {

	/**
	 * Creates the wrapper.
	 * @param j the Jedis connection
	 * @param cpe the connection pool entry
	 */
	protected JedisConnectionWrapper(Jedis j, ConnectionPoolEntry<Jedis> cpe) {
		super(j, cpe);
	}

	@Override
	public int compareTo(ConnectionWrapper<Jedis> cw2) {
		return Integer.compare(getID(), cw2.getID());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return "JedisWrapper-" + String.valueOf(getID());
	}
}