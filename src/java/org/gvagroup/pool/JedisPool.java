// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import redis.clients.jedis.Jedis;

/**
 * A connection pool for Jedis connections.
 * @author Luke
 * @version 3.00
 * @since 3.00
 */

public class JedisPool extends ConnectionPool<Jedis> {

	private static final long serialVersionUID = 479597434632122740L;

	/**
	 * Creates the pool.
	 * @param maxSize the maximum pool size
	 * @param name the pool name
	 */
	public JedisPool(int maxSize, String name) {
		super(maxSize, name, JedisPool.class);
	}

	@Override
	int getStaleTime() {
		return 5000;
	}

	@Override
	protected ConnectionPoolEntry<Jedis> createConnection(int id) throws Exception {
		String url = _props.getProperty("socketFile", _props.getProperty("addr"));
		log.info("{} connecting to {} ID #{}", getName(), url, Integer.valueOf(id));
		JedisPoolEntry entry = new JedisPoolEntry(id, this, _props);
		entry.connect();
		return entry;
	}

	@Override
	protected void cleanup(Jedis j) throws Exception {
		j.resetState();
	}
}