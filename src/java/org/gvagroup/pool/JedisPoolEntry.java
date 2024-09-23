// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import java.util.Properties;

import org.apache.logging.log4j.*;

import redis.clients.jedis.*;

/**
 * A class to store Jedis connections in a connection pool and track usage.
 * @author Luke
 * @version 3.00
 * @since 3.00
 */

public class JedisPoolEntry extends ConnectionPoolEntry<Jedis> {

	private static final long serialVersionUID = -8922331133236192374L;
	
	private static transient final Logger log = LogManager.getLogger(JedisPoolEntry.class);
	
	private final Properties _props = new Properties();
	
	private static class DefaultJedisConfig implements JedisClientConfig {
		private DefaultJedisConfig() {
			super();
		}
	}

	/**
	 * Creates the pool entry.
	 * @param id the Connection ID
	 * @param props the connection properties
	 */
	public JedisPoolEntry(int id, Properties props) {
		super(id);
		_props.putAll(props);
	}
	
	@Override
	public String getType() {
		return "Jedis";
	}

	@Override
	void connect() throws Exception {
		
		// Check for domain socket
		String host = _props.contains("host") ? "localhost" : _props.getProperty("host");
		if (host.startsWith("/") && _props.contains("socketFactory")) {
			log.info("Using Unix socket {}", host);
			Class<?> c = Class.forName(_props.getProperty("socketFactory"));
			JedisSocketFactory sf = (JedisSocketFactory) c.getDeclaredConstructor().newInstance();
			Jedis j = new Jedis(sf, new DefaultJedisConfig());
		} else {
			int port = Integer.parseInt(_props.getProperty("port", "6379"));
			Jedis j = new Jedis(host, port);
		}
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
			Jedis j = getConnection();
			String result = j.ping();
			return "PONG".equals(result);
		} finally {
			markFree();
		}
	}
}