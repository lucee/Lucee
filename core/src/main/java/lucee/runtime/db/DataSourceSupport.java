/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.db;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleException;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.sql.SQLUtil;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.tag.listener.TagListener;

public abstract class DataSourceSupport implements DataSourcePro, Cloneable, Serializable {

	private static final long serialVersionUID = -9111025519905149021L;
	private static final int NETWORK_TIMEOUT_IN_SECONDS = 10;
	private static int defaultTransactionIsolation = -1;

	private final boolean blob;
	private final boolean clob;
	private final int connectionLimit;
	private final int idleTimeout;
	private final int liveTimeout;
	private final long metaCacheTimeout;
	private final TimeZone timezone;
	private final String name;
	private final boolean storage;
	private final boolean validate;
	protected final int allow;
	private final boolean readOnly;
	private final String username;
	private final String password;
	private final ClassDefinition cd;

	private transient Map<String, SoftReference<ProcMetaCollection>> procedureColumnCache;
	private transient Driver driver;
	private transient Log log;
	private final TagListener listener;
	private final boolean requestExclusive;
	private final boolean literalTimestampWithTSOffset;
	private final boolean alwaysResetConnections;
	private final int minIdle;
	private final int maxIdle;
	private final int maxTotal;

	public DataSourceSupport(Config config, String name, ClassDefinition cd, String username, String password, TagListener listener, boolean blob, boolean clob,
			int connectionLimit, int idleTimeout, int liveTimeout, int minIdle, int maxIdle, int maxTotal, long metaCacheTimeout, TimeZone timezone, int allow, boolean storage,
			boolean readOnly, boolean validate, boolean requestExclusive, boolean alwaysResetConnections, boolean literalTimestampWithTSOffset, Log log) {
		this.name = name;
		this.cd = cd;// _initializeCD(null, cd, config);
		this.blob = blob;
		this.clob = clob;
		this.connectionLimit = connectionLimit;
		this.idleTimeout = idleTimeout;
		this.liveTimeout = liveTimeout;
		this.metaCacheTimeout = metaCacheTimeout;
		this.timezone = timezone;
		this.allow = allow;
		this.storage = storage;
		this.readOnly = readOnly;
		this.username = username;
		this.password = password;
		this.listener = listener;
		this.validate = validate;
		this.requestExclusive = requestExclusive;
		this.alwaysResetConnections = alwaysResetConnections;
		this.log = log;
		this.literalTimestampWithTSOffset = literalTimestampWithTSOffset;
		this.minIdle = minIdle;
		this.maxIdle = maxIdle;
		this.maxTotal = maxTotal;
	}

	@Override
	public Connection getConnection(Config config, String user, String pass) throws ClassException, BundleException, SQLException {
		try {
			if (user == null) user = username;
			if (pass == null) pass = password;
			return _getConnection(config, initialize(config), SQLUtil.connectionStringTranslatedPatch(config, getConnectionStringTranslated()), user, pass);

		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public static Connection _getConnection(Config config, Driver driver, String connStrTrans, String user, String pass) throws SQLException {
		java.util.Properties props = new java.util.Properties();
		if (user != null) props.put("user", user);
		if (pass != null) props.put("password", pass);

		if (defaultTransactionIsolation == -1) {
			Connection c = driver.connect(connStrTrans, props);
			defaultTransactionIsolation = getValidTransactionIsolation(c, Connection.TRANSACTION_READ_COMMITTED);
			return c;
		}
		return driver.connect(connStrTrans, props);
	}

	private static int getValidTransactionIsolation(Connection conn, int defaultValue) {
		try {
			int transactionIsolation = conn.getTransactionIsolation();
			if (transactionIsolation == Connection.TRANSACTION_READ_COMMITTED) return Connection.TRANSACTION_READ_COMMITTED;
			if (transactionIsolation == Connection.TRANSACTION_SERIALIZABLE) return Connection.TRANSACTION_SERIALIZABLE;
			if (SQLUtil.isOracle(conn)) return defaultValue;
			if (transactionIsolation == Connection.TRANSACTION_READ_UNCOMMITTED) return Connection.TRANSACTION_READ_UNCOMMITTED;
			if (transactionIsolation == Connection.TRANSACTION_REPEATABLE_READ) return Connection.TRANSACTION_REPEATABLE_READ;
		}
		catch (Exception e) {
		}
		return defaultValue;
	}

	@Override
	public int getDefaultTransactionIsolation() {
		return defaultTransactionIsolation;
	}

	private Driver initialize(Config config) throws BundleException, InstantiationException, IllegalAccessException, IOException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		if (driver == null) {
			return driver = _initializeDriver(cd, config);
		}
		return driver;
	}

	private static Driver _initializeDriver(ClassDefinition cd, Config config) throws ClassException, BundleException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// load the class
		Driver d = (Driver) ClassUtil.newInstance(cd.getClazz());
		return d;
	}

	public static void verify(Config config, ClassDefinition cd, String connStrTranslated, String user, String pass) throws ClassException, BundleException, SQLException {
		try {
			// Driver driver = _initializeDriver(_initializeCD(jdbc, cd, config),config);
			Driver driver = _initializeDriver(cd, config);
			_getConnection(config, driver, connStrTranslated, user, pass);
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e.getTargetException());
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object clone() {
		return cloneReadOnly();
	}

	public Map<String, SoftReference<ProcMetaCollection>> getProcedureColumnCache() {
		if (procedureColumnCache == null) procedureColumnCache = new ConcurrentHashMap<String, SoftReference<ProcMetaCollection>>();
		return procedureColumnCache;
	}

	@Override
	public final boolean isBlob() {
		return blob;
	}

	@Override
	public final boolean isClob() {
		return clob;
	}

	@Override
	public final int getConnectionLimit() {
		return connectionLimit;
	}

	@Override
	public final int getConnectionTimeout() {
		return idleTimeout;
	}

	@Override
	public final int getIdleTimeout() {
		return idleTimeout;
	}

	@Override
	public final int getLiveTimeout() {
		return liveTimeout;
	}

	@Override
	public final long getMetaCacheTimeout() {
		return metaCacheTimeout;
	}

	@Override
	public final TimeZone getTimeZone() {
		return timezone;
	}

	@Override
	public final ClassDefinition getClassDefinition() {
		return cd;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final boolean isStorage() {
		return storage;
	}

	@Override
	public final boolean hasAllow(int allow) {
		return (this.allow & allow) > 0;
	}

	@Override
	public final boolean hasSQLRestriction() {
		return this.allow != DataSource.ALLOW_ALL;
	}

	@Override
	public final boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public int getNetworkTimeout() {
		return NETWORK_TIMEOUT_IN_SECONDS;
	}

	@Override
	public int getMinIdle() {
		return minIdle;
	}

	@Override
	public int getMaxIdle() {
		return maxIdle;
	}

	@Override
	public int getMaxTotal() {
		return maxTotal;
	}

	@Override
	public final boolean validate() {
		return validate;
	}

	@Override
	public boolean isRequestExclusive() {
		return requestExclusive;
	}

	@Override
	public boolean isAlwaysResetConnections() {
		return alwaysResetConnections;
	}

	// FUTURE add to interface
	public final boolean getLiteralTimestampWithTSOffset() {
		return literalTimestampWithTSOffset;
	}

	@Override
	public Log getLog() {
		// can be null if deserialized
		if (log == null) log = ThreadLocalPageContext.getConfig().getLog("application");
		return log;
	}

	@Override
	public TagListener getListener() { // FUTURE may add to interface
		return listener;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DataSource)) return false;
		DataSource ds = (DataSource) obj;
		return id().equals(ds.id());
	}

	@Override
	public int hashCode() {
		return id().hashCode();
	}

	@Override
	public String id() {

		return new StringBuilder(getConnectionStringTranslated()).append(':').append(getConnectionLimit()).append(':').append(getConnectionTimeout()).append(':')
				.append(getLiveTimeout()).append(':').append(getMetaCacheTimeout()).append(':').append(getName().toLowerCase()).append(':').append(getUsername()).append(':')
				.append(getPassword()).append(':').append(validate()).append(':').append(cd.toString()).append(':').append((getTimeZone() == null ? "null" : getTimeZone().getID()))
				.append(':').append(isBlob()).append(':').append(isClob()).append(':').append(isReadOnly()).append(':').append(isStorage()).append(':').append(isRequestExclusive())
				.append(':').append(isAlwaysResetConnections()).toString();
	}

	@Override
	public String toString() {
		return id();
	}
}