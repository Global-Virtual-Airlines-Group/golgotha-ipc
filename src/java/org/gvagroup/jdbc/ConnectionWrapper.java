// Copyright 2007, 2009, 2011, 2013, 2014, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.gvagroup.jdbc;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * A wrapper around JDBC connections returned from the Connection Pool. This prevents
 * certain sensitive methods (such as {@link Connection#close()}) from being called by
 * command code. 
 * @author Luke
 * @version 2.21
 * @since 1.0
 */

public class ConnectionWrapper implements Connection, Comparable<ConnectionWrapper> {
	
	private final int _id;
	private transient final Connection _c;
	private transient final ConnectionPoolEntry _entry;
	
	private long _lastUse;
	private boolean _autoCommit;

	/**
	 * Creates the wrapper.
	 * @param c the JDBC connection
	 * @param cpe the ConnectionPoolEntry to wrap
	 */
	ConnectionWrapper(Connection c, ConnectionPoolEntry cpe) {
		super();
		_c = c;
		_id = cpe.getID();
		_entry = cpe;
	}
	
	/**
	 * Returns the Connection Pool ID for this Connection.
	 * @return the ID
	 * @see ConnectionPoolEntry#getID()
	 */
	public int getID() {
		return _id;
	}
	
	/**
	 * Returns the last time the underlying JDBC connection was accessed.
	 * @return the last use date/time
	 */
	long getLastUse() {
		return _lastUse;
	}
	
	private void recordLastUse() {
		_lastUse = System.currentTimeMillis();
	}

	@Override
	public void clearWarnings() throws SQLException {
		recordLastUse();
		_c.clearWarnings();
	}

	@Override
	public void close() {
		recordLastUse();
		_entry.free();
	}
	
	/**
	 * Forces a close of the underlying JDBC connection.
	 * @throws SQLException
	 */
	void forceClose() throws SQLException {
		recordLastUse();
		_c.close();
	}
	
	Connection getConnection() {
		return _c;
	}

	@Override
	public void commit() throws SQLException {
		recordLastUse();
		_c.commit();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		recordLastUse();
		return _c.createArrayOf(typeName, elements);
	}

	@Override
	public Blob createBlob() throws SQLException {
		recordLastUse();
		return _c.createBlob();
	}

	@Override
	public Clob createClob() throws SQLException {
		recordLastUse();
		return _c.createClob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		recordLastUse();
		return _c.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		recordLastUse();
		return _c.createSQLXML();
	}

	@Override
	public Statement createStatement() throws SQLException {
		recordLastUse();
		return _c.createStatement();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		recordLastUse();
		return _c.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		recordLastUse();
		return _c.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		recordLastUse();
		return _c.createStruct(typeName, attributes);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return _autoCommit;
	}

	@Override
	public String getCatalog() throws SQLException {
		recordLastUse();
		return _c.getCatalog();
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		recordLastUse();
		return _c.getClientInfo();
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		recordLastUse();
		return _c.getClientInfo(name);
	}

	@Override
	public int getHoldability() throws SQLException {
		recordLastUse();
		return _c.getHoldability();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		recordLastUse();
		return _c.getMetaData();
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		recordLastUse();
		return _c.getTransactionIsolation();
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		recordLastUse();
		return _c.getTypeMap();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		recordLastUse();
		return _c.getWarnings();
	}

	@Override
	public boolean isClosed() throws SQLException {
		recordLastUse();
		return _c.isClosed();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		recordLastUse();
		return _c.isValid(timeout);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		recordLastUse();
		return _c.nativeSQL(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		recordLastUse();
		return _c.prepareCall(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		recordLastUse();
		return _c.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		recordLastUse();
		return _c.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		recordLastUse();
		PreparedStatement ps = _c.prepareStatement(sql);
		return new PreparedStatementWrapper(ps);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		recordLastUse();
		PreparedStatement ps = _c.prepareStatement(sql, autoGeneratedKeys);
		return new PreparedStatementWrapper(ps);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		recordLastUse();
		PreparedStatement ps = _c.prepareStatement(sql, columnIndexes);
		return new PreparedStatementWrapper(ps);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		recordLastUse();
		PreparedStatement ps = _c.prepareStatement(sql, columnNames);
		return new PreparedStatementWrapper(ps);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		recordLastUse();
		PreparedStatement ps = _c.prepareStatement(sql, resultSetType, resultSetConcurrency);
		return new PreparedStatementWrapper(ps);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		recordLastUse();
		PreparedStatement ps = _c.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		return new PreparedStatementWrapper(ps);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		recordLastUse();
		_c.releaseSavepoint(savepoint);
	}

	@Override
	public void rollback() throws SQLException {
		recordLastUse();
		_c.rollback();
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		recordLastUse();
		_c.rollback(savepoint);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		_autoCommit = autoCommit;
		recordLastUse();
		_c.setAutoCommit(_autoCommit);
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		recordLastUse();
		_c.setCatalog(catalog);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		recordLastUse();
		_c.setClientInfo(properties);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		recordLastUse();
		_c.setClientInfo(name, value);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		recordLastUse();
		_c.setHoldability(holdability);
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		recordLastUse();
		// empty
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		recordLastUse();
		return _c.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		recordLastUse();
		return _c.setSavepoint(name);
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		recordLastUse();
		// nothing
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> m) throws SQLException {
		recordLastUse();
		_c.setTypeMap(m);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		recordLastUse();
		return _c.isWrapperFor(iface);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		recordLastUse();
		return _c.unwrap(iface);
	}
	
	@Override
	public int compareTo(ConnectionWrapper cw2) {
		return Integer.compare(_id, cw2._id);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return "ConnectionWrapper-" + String.valueOf(_id);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		recordLastUse();
		_c.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		recordLastUse();
		return _c.getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		recordLastUse();
		_c.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		recordLastUse();
		_c.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		recordLastUse();
		return _c.getNetworkTimeout();
	}
}