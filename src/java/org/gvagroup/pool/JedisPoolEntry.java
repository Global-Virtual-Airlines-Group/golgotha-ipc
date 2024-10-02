// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import java.util.Properties;

import redis.clients.jedis.*;

/**
 * A class to store Jedis connections in a connection pool and track usage.
 * @author Luke
 * @version 3.00
 * @since 3.00
 */

public class JedisPoolEntry extends ConnectionPoolEntry<Jedis> {

	private static final long serialVersionUID = -8922331133236192374L;
	
	private final Properties _props = new Properties();
	
	private static class DefaultJedisConfig implements JedisClientConfig {
		private DefaultJedisConfig() {
			super();
		}
	}

	/**
	 * Creates the pool entry.
	 * @param id the Connection ID
	 * @param src the data source
	 * @param props the connection properties
	 */
	public JedisPoolEntry(int id, Recycler<Jedis> src, Properties props) {
		super(id, src, JedisPoolEntry.class);
		_props.putAll(props);
	}
	
	@Override
	public String getType() {
		return "Jedis";
	}

	@Override
	void connect() throws Exception {
		
		// Check for domain socket
		String host = _props.getProperty("addr", "locahost");
		if (host.startsWith("/")) {
			log.info("Using Unix socket {}", host);
			JedisSocketFactory sf = new JedisDomainSocketFactory(host);
			setWrapper(new JedisWrapper(sf, new DefaultJedisConfig(), this));
		} else {
			int port = Integer.parseInt(_props.getProperty("port", "6379"));
			setWrapper(new JedisWrapper(host, port, this));
		}

		Jedis j = get();
		j.select(Integer.parseInt(_props.getProperty("db", "0")));
		j.clientSetname(String.format("%s-%d", _props.getProperty("poolName", "jedis"), Integer.valueOf(getID())));
		markConnected();
	}
	
	@Override
	Jedis reserve(boolean logStack) {
		checkState();
		if (logStack)
			generateStackTrace();
		
		// Mark the connection as in use, and return the Jedis connection
		markUsed();
		return getWrapper().get();
	}

	@Override
	void free() {
		if (checkFree())
			return;
		
		markFree();		
	}

	@Override
	boolean checkConnection() {
		markUsed();
		try {
			Jedis j = get(); // Don't autoclose as the pool will do this
			String result = j.ping();
			return "PONG".equals(result);
		} catch (Exception e) {
			log.error("Error checking {}-{} - {}", getType(), Integer.valueOf(getID()), e.getMessage());
			return false;
		} finally {
			markFree();
		}
	}
	
	@Override
	void cleanup() {
		Jedis j = get(); // Don't autoclose as the pool will do this
		j.resetState();
	}
}