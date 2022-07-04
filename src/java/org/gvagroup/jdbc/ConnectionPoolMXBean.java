// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

import javax.management.MXBean;

/**
 * 
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

@MXBean
public interface ConnectionPoolMXBean {

	/**
	 * Returns a collection of connection information.
	 * @return a Collection of ConnectionMBeans
	 */
	public ConnectionMBean[] getPoolInfo();
	
	/**
	 * Updates connection pool statistics when called by the JMX client.
	 */
	public void update();
}
