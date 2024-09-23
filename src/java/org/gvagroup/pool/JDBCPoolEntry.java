// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2013, 2014, 2015, 2017, 2020, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.pool;

import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.*;

/**
 * A class to store JDBC connections in a connection pool and track usage.
 * @author Luke
 * @version 3.00
 * @since 3.0
 */

class JDBCPoolEntry extends ConnectionPoolEntry<Connection> {

	private static final long serialVersionUID = 986869655819214021L;
	
	private static transient final Logger log = LogManager.getLogger(JDBCPoolEntry.class);

	// Default connection serialization
	private static transient final int DEFAULT_SERIALIZATION = Connection.TRANSACTION_READ_COMMITTED;
	
	private Properties _props = new Properties();
	private transient String _validationQuery = "SELECT 1";
	private boolean _autoCommit = true;

	/**
	 * Create a new Connection Pool entry.
	 * @param id the connection pool entry ID
	 * @param props JDBC connection properties
	 */
	JDBCPoolEntry(int id, Properties props) {
		super(id);
		if (props.containsKey("validationQuery")) {
			_validationQuery = props.getProperty("validationQuery");
			props.remove("validationQuery");
		}
		
		_props.putAll(props);
	}
	
	@Override
	public String getType() {
		return "JDBC";
	}

	/**
	 * Connects this entry to the JDBC data source.
	 * @throws SQLException if a JDBC error occurs
	 * @throws IllegalStateException if the entry is already connected
	 */
	@Override
	void connect() throws SQLException {
		Connection c = isActive() ? getConnection() : null;
		if ((c != null) && !c.isClosed())
			throw new IllegalStateException(String.format("Connection %s already Connected", toString()));

		// Create the connection
		Connection con = DriverManager.getConnection(_props.getProperty("url"), _props);
		con.setTransactionIsolation(DEFAULT_SERIALIZATION);
		JDBCConnectionWrapper cw = new JDBCConnectionWrapper(con, this);
		cw.setAutoCommit(_autoCommit);
		setWrapper(cw);
	}
	
	@Override
	void free() {
		if (checkFree())
			return;

		// Reset auto-commit property
		try {
			Connection c = getConnection();
			if ((c != null) && (c.getAutoCommit() != _autoCommit)) {
				log.info("Resetting autoCommit to {}", Boolean.valueOf(_autoCommit));
				c.setAutoCommit(_autoCommit);
				c.setTransactionIsolation(DEFAULT_SERIALIZATION);
			}
		} catch (Exception e) {
			log.error("Error resetting autoCommit/isolation on {} - {}", Integer.valueOf(getID()), e.getMessage());
		}

		// Add the usage time to the total for this connection
		markFree();
	}
	
	@Override
	boolean checkConnection() {
		Connection c = getConnection();
		try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(_validationQuery)) {
			return rs.next();
		} catch (SQLException se) {
			return false;
		}
	}

	/**
	 * Sets the automatic commit setting for this connection. When set, all transactions will be committed to the JDBC
	 * data source immediately. Data Access Objects may change the autoCommit property of the underlying JDBC
	 * connection, but when the connection is returned to the pool its autoCommit property will be reset back to this
	 * value.
	 * @param commit TRUE if connections should autoCommit by default, otherwise FALSE
	 * @see Connection#setAutoCommit(boolean)
	 */
	void setAutoCommit(boolean commit) {
		_autoCommit = commit;
	}
}