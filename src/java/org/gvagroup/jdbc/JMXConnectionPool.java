// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

import java.util.*;

/**
 * A JMX bean to export JDBC Connection Pool statistics.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class JMXConnectionPool implements ConnectionPoolMXBean {
	
	private final ConnectionPool _pool;
	
	private final Collection<? super ConnectionMBean> _info = new ArrayList<ConnectionMBean>();
	
	/**
	 * Initializes the bean.
	 * @param pool the JDBC connection pool
	 */
	public JMXConnectionPool(ConnectionPool pool) {
		super();
		_pool = pool;
	}

	@Override
	public ConnectionMBean[] getPoolInfo() {
		return _info.toArray(new ConnectionMBean[0]);
	}
	
	@Override
	public synchronized void update() {
		_info.clear();
		Collection<ConnectionInfo> info = _pool.getPoolInfo();
		info.stream().map(ConnectionMBeanImpl::new).forEach(_info::add);
	}
}