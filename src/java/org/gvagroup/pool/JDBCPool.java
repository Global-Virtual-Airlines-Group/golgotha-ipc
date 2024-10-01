// Copyright 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2013, 2014, 2015, 2016, 2017, 2020, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import java.sql.*;
import java.io.File;
import java.lang.reflect.*;

/**
 * A user-configurable JDBC Connection Pool.
 * @author Luke
 * @version 3.00
 * @since 1.0
 */

public class JDBCPool extends ConnectionPool<Connection> {

	private static final long serialVersionUID = 4958035033059527516L;

	private transient boolean _isMySQL;
	private boolean _autoCommit = true;

	@Override
	int getStaleTime() {
		return 145_000;
	}
	
	/**
	 * Creates a new JDBC connection pool.
	 * @param maxSize the maximum size of the connection pool
	 * @param name the Connection pool size
	 */
	public JDBCPool(int maxSize, String name) {
		super(maxSize, name, 30, JDBCPool.class);
		DriverManager.setLoginTimeout(2);
	}
	
	@Override
	public String getType() {
		return "JDBC";
	}

	/**
	 * Updates the database auto-commit setting.
	 * @param commit TRUE if statements are committed automatically, otherwise FALSE
	 */
	public void setAutoCommit(boolean commit) {
		_autoCommit = commit;
	}
	
	/**
	 * Sets the data source URL to use.
	 * @param url the JDBC URL
	 */
	public void setURL(String url) {
		_props.put("url", url);
	}
	
	/**
	 * Sets a domain socket to connect to.
	 * @param socketFile the path to the Unix domain socket
	 */
	public void setSocket(String socketFile) {
		if (socketFile == null) return;
		File f = new File(socketFile);
		if (f.exists() && _isMySQL) {
			_props.put("socketFactory", "org.newsclub.net.mysql.AFUNIXDatabaseSocketFactoryCJ");
			_props.put("junixsocket.file", f.getAbsolutePath());
		} else {
			_props.remove("socketFactory");
			_props.remove("junixsocket.file");
		}
	}

	/**
	 * Sets the JDBC Driver class name.
	 * @param driverClassName the fully-qualified class name of the JDBC driver
	 * @throws ClassNotFoundException if the class cannot be loaded or is not a JDBC driver
	 */
	public void setDriver(String driverClassName) throws ClassNotFoundException {
		Class<?> c = Class.forName(driverClassName);
		_isMySQL = driverClassName.startsWith("com.mysql.cj.jdbc.");
		if (_isMySQL)
			log.info("MySQL JDBC Driver detected");
		
		for (int x = 0; x < c.getInterfaces().length; x++) {
			if (c.getInterfaces()[x].getName().equals("java.sql.Driver"))
				return;
		}

		throw new ClassCastException(String.format("%s does not implement java.sql.Driver", c.getName()));
	}

	@Override
	protected ConnectionPoolEntry<Connection> createConnection(int id) throws SQLException {
		String url = _props.getProperty("junixsocket.file", _props.getProperty("url"));
		log.info("{} connecting to {} as user {} ID #{}", getName(), url, _props.getProperty("user"), Integer.valueOf(id));
		JDBCPoolEntry entry = new JDBCPoolEntry(id, this, _props);
		entry.setAutoCommit(_autoCommit);
		entry.connect();
		return entry;
	}

	@Override
	public void close() {
		super.close();
		
		// MySQL thread shutdown
		if (_isMySQL) {
			log.info("Shutting down MySQL abandoned connection thread");
			try {
				Class<?> c = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
				Method m = c.getMethod("uncheckedShutdown", new Class<?>[] {});
				m.invoke(null, new Object[] {});
				Thread.sleep(250);
				
				// Wait for thread to die
				Field f = c.getDeclaredField("threadRef"); 
				boolean oldAccess = f.canAccess(null); f.setAccessible(true);
				Object o = f.get(null); f.setAccessible(oldAccess);
				if (o != null) {
					Thread t = (Thread) o; int totalTime = 0;
					log.info("Found thread {} - {}", t.getName(), t.isAlive() ? "Running" : "Terminated");
					while (t.isAlive() && (totalTime < 250)) {
						Thread.sleep(50);
						totalTime += 50;
					}
					
					if (t.isAlive())
						log.warn("{} still running", t.getName());
				}
			} catch (ClassNotFoundException cnfe) {
				log.warn("Cannot load class com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
			} catch (Exception e) {
				log.error("{} shutting down thread - {}", e.getClass().getSimpleName(), e.getMessage());
			}
		}
	}
}