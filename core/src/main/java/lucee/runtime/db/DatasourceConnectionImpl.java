/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.db;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.spooler.Task;

/**
 * wrap for datasorce and connection from it
 */
public final class DatasourceConnectionImpl implements DatasourceConnectionPro, Task {

	// private static final int MAX_PS = 100;
	private Connection connection;
	private DataSourcePro datasource;
	private long time;
	private final long start;
	private String username;
	private String password;
	private int transactionIsolationLevel = -1;
	private int requestId = -1;
	private Boolean supportsGetGeneratedKeys;

	/**
	 * @param connection
	 * @param datasource
	 * @param pass
	 * @param user
	 */
	public DatasourceConnectionImpl(Connection connection, DataSourcePro datasource, String username, String password) {
		this.connection = connection;
		this.datasource = datasource;
		this.time = this.start = System.currentTimeMillis();
		this.username = username;
		this.password = password;

		if (username == null) {
			this.username = datasource.getUsername();
			this.password = datasource.getPassword();
		}
		if (this.password == null) this.password = "";
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public DataSource getDatasource() {
		return datasource;
	}

	@Override
	public boolean isTimeout() {
		int timeout = datasource.getConnectionTimeout();
		if (timeout <= 0) return false;
		timeout *= 60000;
		return (time + timeout) < System.currentTimeMillis();
	}

	@Override
	public boolean isLifecycleTimeout() {
		int timeout = datasource.getConnectionTimeout() * 5;// fo3 the moment simply 5 times the idle timeout
		if (timeout <= 0) return false;
		timeout *= 60000;
		return (start + timeout) < System.currentTimeMillis();
	}

	public DatasourceConnection using() throws PageException {
		time = System.currentTimeMillis();
		if (datasource.isAlwaysResetConnections()) {
			try {
				connection.setAutoCommit(true);
				connection.setTransactionIsolation(getDefaultTransactionIsolation());
			}
			catch (SQLException sqle) {
				throw Caster.toPageException(sqle);
			}
		}
		return this;
	}

	/**
	 * @return the password
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * @return the username
	 */
	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return equals(this, (DatasourceConnection) obj);
	}

	public static boolean equals(DatasourceConnection left, DatasourceConnection right) {
		if (!left.getDatasource().equals(right.getDatasource())) return false;
		return StringUtil.emptyIfNull(left.getUsername()).equals(StringUtil.emptyIfNull(right.getUsername()))
				&& StringUtil.emptyIfNull(left.getPassword()).equals(StringUtil.emptyIfNull(right.getPassword()));
	}

	public static boolean equals(DatasourceConnection dc, DataSource ds, String user, String pass) {
		if (StringUtil.isEmpty(user)) {
			user = ds.getUsername();
			pass = ds.getPassword();
		}
		if (!dc.getDatasource().equals(ds)) return false;
		return StringUtil.emptyIfNull(dc.getUsername()).equals(StringUtil.emptyIfNull(user)) && StringUtil.emptyIfNull(dc.getPassword()).equals(StringUtil.emptyIfNull(pass));
	}

	/**
	 * @return the transactionIsolationLevel
	 */
	public int getTransactionIsolationLevel() {
		return transactionIsolationLevel;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	@Override
	public boolean supportsGetGeneratedKeys() {
		if (supportsGetGeneratedKeys == null) {
			try {
				supportsGetGeneratedKeys = Caster.toBoolean(getConnection().getMetaData().supportsGetGeneratedKeys());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				return false;
			}
		}
		return supportsGetGeneratedKeys.booleanValue();
	}

	// private Map<String,PreparedStatement> preparedStatements=new HashMap<String,
	// PreparedStatement>();

	@Override
	public PreparedStatement getPreparedStatement(SQL sql, boolean createGeneratedKeys, boolean allowCaching) throws SQLException {
		if (createGeneratedKeys) return getConnection().prepareStatement(sql.getSQLString(), Statement.RETURN_GENERATED_KEYS);
		return getConnection().prepareStatement(sql.getSQLString());
	}

	/*
	 * public PreparedStatement getPreparedStatement(SQL sql, boolean createGeneratedKeys,boolean
	 * allowCaching) throws SQLException { // create key String strSQL=sql.getSQLString(); String
	 * key=strSQL.trim()+":"+createGeneratedKeys; try { key = MD5.getDigestAsString(key); } catch
	 * (IOException e) {} PreparedStatement ps = allowCaching?preparedStatements.get(key):null;
	 * if(ps!=null) { if(DataSourceUtil.isClosed(ps,true)) preparedStatements.remove(key); else return
	 * ps; }
	 * 
	 * 
	 * if(createGeneratedKeys) ps=
	 * getConnection().prepareStatement(strSQL,Statement.RETURN_GENERATED_KEYS); else
	 * ps=getConnection().prepareStatement(strSQL); if(preparedStatements.size()>MAX_PS)
	 * closePreparedStatements((preparedStatements.size()-MAX_PS)+1);
	 * if(allowCaching)preparedStatements.put(key,ps); return ps; }
	 */

	@Override
	public PreparedStatement getPreparedStatement(SQL sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return getConnection().prepareStatement(sql.getSQLString(), resultSetType, resultSetConcurrency);
	}

	/*
	 * 
	 * public PreparedStatement getPreparedStatement(SQL sql, int resultSetType,int
	 * resultSetConcurrency) throws SQLException { boolean allowCaching=false; // create key String
	 * strSQL=sql.getSQLString(); String key=strSQL.trim()+":"+resultSetType+":"+resultSetConcurrency;
	 * try { key = MD5.getDigestAsString(key); } catch (IOException e) {} PreparedStatement ps =
	 * allowCaching?preparedStatements.get(key):null; if(ps!=null) {
	 * if(DataSourceUtil.isClosed(ps,true)) preparedStatements.remove(key); else return ps; }
	 * 
	 * ps=getConnection().prepareStatement(strSQL,resultSetType,resultSetConcurrency);
	 * if(preparedStatements.size()>MAX_PS)
	 * closePreparedStatements((preparedStatements.size()-MAX_PS)+1);
	 * if(allowCaching)preparedStatements.put(key,ps); return ps; }
	 */

	@Override
	public Object execute(Config config) throws PageException {
		((ConfigImpl) config).getDatasourceConnectionPool().releaseDatasourceConnection(this);
		return null;
	}

	@Override
	public Statement createStatement() throws SQLException {
		return connection.createStatement();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return connection.prepareCall(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return connection.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return connection.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return connection.prepareStatement(sql, columnNames);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return connection.isWrapperFor(iface);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return connection.unwrap(iface);
	}

	@Override
	public void clearWarnings() throws SQLException {
		connection.clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		connection.close();
	}

	@Override
	public void commit() throws SQLException {
		connection.commit();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return connection.createArrayOf(typeName, elements);
	}

	@Override
	public Blob createBlob() throws SQLException {
		return connection.createBlob();
	}

	@Override
	public Clob createClob() throws SQLException {
		return connection.createClob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return connection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return connection.createSQLXML();
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return connection.createStruct(typeName, attributes);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	@Override
	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return connection.getClientInfo();
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return connection.getClientInfo(name);
	}

	@Override
	public int getHoldability() throws SQLException {
		return connection.getHoldability();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return connection.isValid(timeout);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return connection.nativeSQL(sql);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	@Override
	public void rollback() throws SQLException {
		connection.rollback();
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		connection.setClientInfo(properties);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		connection.setClientInfo(name, value);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		connection.setHoldability(holdability);
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return connection.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connection.setTypeMap(map);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		connection.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		return connection.getSchema();
	}

	// used only with java 7, do not set @Override
	@Override
	public void abort(Executor executor) throws SQLException {
		connection.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		connection.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return connection.getNetworkTimeout();
	}

	@Override
	public boolean isAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	@Override
	public int getDefaultTransactionIsolation() {
		return datasource.getDefaultTransactionIsolation();
	}

}