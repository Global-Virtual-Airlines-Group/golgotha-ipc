// Copyright 2024, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import redis.clients.jedis.*;

/**
 * A connection wrapper class for Jedis connections.
 * @author Luke
 * @version 3.16
 * @since 3.00
 */

public class JedisWrapper extends Jedis implements ConnectionWrapper<Jedis> {
	
	private final int _id;
	private long _lastUse;
	
	private transient final ConnectionPoolEntry<Jedis> _entry;
	
	/**
	 * Creates the wrapper.
	 * @param ep the Jedis server address
	 * @param cfg a JedisClientConfig
	 * @param cpe the connection pool entry
	 */
	JedisWrapper(HostAndPort ep, JedisClientConfig cfg, ConnectionPoolEntry<Jedis> cpe) {
		super(ep, cfg);
		_id = cpe.getID();
		_entry = cpe;
	}
	
	/**
	 * Creates the wrapper with a custom SocketFactory (usually for domain socket connections).
	 * @param sf a SocketFactory
	 * @param cfg a JedisClientConfig
	 * @param cpe the connection pool entry
	 */
	JedisWrapper(JedisSocketFactory sf, JedisClientConfig cfg, ConnectionPoolEntry<Jedis> cpe) {
		super(sf, cfg);
		_id = cpe.getID();
		_entry = cpe;
	}
	
	@Override
	public int getID() {
		return _id;
	}
	
	@Override
	public long getLastUse() {
		return _lastUse;
	}
	
	@Override
	public Jedis get() {
		return this;
	}

	private void recordLastUse() {
		_lastUse = System.currentTimeMillis();
	}

	@Override
	public void close() {
		recordLastUse();
		_entry.recycle();
	}

	@Override
	public void forceClose() throws Exception {
		recordLastUse();
		super.close();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("JedisWrapper-");
		return buf.append(_id).toString();
	}
}