/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.TimeZone;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.sql.SQLUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.tag.listener.TagListener;
import lucee.transformer.library.ClassDefinitionImpl;

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

	private transient volatile Driver driver;
	private transient Log log;
	private final TagListener listener;
	private final boolean requestExclusive;
	private final boolean literalTimestampWithTSOffset;
	private final boolean alwaysResetConnections;
	private final int minIdle;
	private final int maxIdle;
	private final int maxTotal;
	private Boolean mssql;
	private String id;

	public DataSourceSupport(String name, ClassDefinition cd, String username, String password, TagListener listener, boolean blob, boolean clob, int connectionLimit,
			int idleTimeout, int liveTimeout, int minIdle, int maxIdle, int maxTotal, long metaCacheTimeout, TimeZone timezone, int allow, boolean storage, boolean readOnly,
			boolean validate, boolean requestExclusive, boolean alwaysResetConnections, boolean literalTimestampWithTSOffset, Log log) {
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
	public Connection getConnection(Config config, String user, String pass) throws ClassException, SQLException {
		try {
			if (user == null) user = username;
			if (pass == null) pass = password;
			return _getConnection(config, initialize(config), SQLUtil.connectionStringTranslatedPatch(config, getConnectionStringTranslated()), user, pass);

		}
		catch (Exception e) {
			throw Caster.toPageRuntimeException(e);
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
		catch (Exception e) {}
		return defaultValue;
	}

	@Override
	public int getDefaultTransactionIsolation() {
		return defaultTransactionIsolation;
	}

	private Driver initialize(Config config) throws Exception {
		Driver d = driver;
		if (d != null && !isFromUninstalledBundle(d.getClass())) return d;
		synchronized (this) {
			d = driver;
			if (d != null && !isFromUninstalledBundle(d.getClass())) return d;
			// the cached driver (if any) comes from a bundle that was uninstalled, drop it and resolve again
			driver = null;
			return driver = _initializeDriver(name, cd, config);
		}
	}

	/**
	 * drops the cached JDBC driver instance, so the next connection resolves the driver again
	 */
	public void resetDriver() {
		synchronized (this) {
			driver = null;
		}
	}

	private static Driver _initializeDriver(String dsName, ClassDefinition cd, Config config) throws Exception {
		// LDEV-5801 without a class name there is nothing to load, tell what is missing instead of failing with [null]
		if (cd == null || !cd.hasClass()) throw noDriverClass(dsName, config);

		// load the class
		Class<?> clazz = cd.getClazz();
		if (isFromUninstalledBundle(clazz)) {
			// the class definition still holds a class from an uninstalled bundle, force a fresh lookup
			Bundle uninstalled = getBundle(clazz);
			try {
				clazz = (cd instanceof ClassDefinitionImpl) ? ((ClassDefinitionImpl) cd).getClazz(true) : cd.getClazz();
			}
			catch (Exception e) {
				ClassException ce = driverNotAvailable(cd, uninstalled);
				ExceptionUtil.initCauseEL(ce, e);
				throw ce;
			}
			if (isFromUninstalledBundle(clazz)) throw driverNotAvailable(cd, uninstalled);
		}
		return (Driver) ClassUtil.newInstance(clazz);
	}

	/**
	 * LDEV-5801 the datasource has no JDBC driver class, because none of the settings that define the driver
	 * ([class], [id], [type], [dbdriver]) did result in a driver.
	 */
	private static ApplicationException noDriverClass(String dsName, Config config) {
		StringBuilder sb = new StringBuilder();
		sb.append("No JDBC driver defined for the datasource").append(StringUtil.isEmpty(dsName, true) ? "" : " [" + dsName + "]").append(". ");
		sb.append("The driver is defined by [class] (the JDBC driver class, together with [bundleName] and [bundleVersion] or [maven]), ");
		sb.append("or by [type] / [dbdriver] / [id] matching the id of an installed JDBC driver extension");
		String ids = getInstalledDriverIds(config);
		if (ids.length() > 0) sb.append(" (installed: ").append(ids).append(")");
		sb.append(".");
		return new ApplicationException(sb.toString());
	}

	private static String getInstalledDriverIds(Config config) {
		try {
			if (!(config instanceof ConfigPro)) return "";
			JDBCDriver[] drivers = ((ConfigPro) config).getJDBCDrivers();
			if (drivers == null) return "";
			StringBuilder sb = new StringBuilder();
			for (JDBCDriver d: drivers) {
				if (d == null || StringUtil.isEmpty(d.id, true)) continue;
				if (sb.length() > 0) sb.append(", ");
				sb.append(d.id);
			}
			return sb.toString();
		}
		catch (Exception e) {
			return "";
		}
	}

	private static ClassException driverNotAvailable(ClassDefinition cd, Bundle bundle) {
		String bundleInfo = bundle != null ? bundle.getSymbolicName() + ":" + bundle.getVersion() : cd.getName() + ":" + cd.getVersionAsString();
		return new ClassException("The JDBC driver [" + cd.getClassName() + "] is no longer available, the OSGi bundle [" + bundleInfo
				+ "] it was loaded from has been uninstalled (for example because the extension providing the driver was removed or updated). "
				+ "Install the extension that provides this JDBC driver again.");
	}

	private static Bundle getBundle(Class<?> clazz) {
		if (clazz == null) return null;
		ClassLoader cl = clazz.getClassLoader();
		if (cl instanceof BundleReference) return ((BundleReference) cl).getBundle();
		return null;
	}

	private static boolean isFromUninstalledBundle(Class<?> clazz) {
		Bundle b = getBundle(clazz);
		return b != null && b.getState() == Bundle.UNINSTALLED;
	}

	public static void verify(Config config, ClassDefinition cd, String connStrTranslated, String user, String pass) throws PageException {
		try {
			// Driver driver = _initializeDriver(_initializeCD(jdbc, cd, config),config);
			Driver driver = _initializeDriver(null, cd, config);
			_getConnection(config, driver, connStrTranslated, user, pass);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object clone() {
		return cloneReadOnly();
	}

	@Override
	public final boolean isBlob() {
		return blob;
	}

	public final boolean getBlob() {
		return blob;
	}

	@Override
	public final boolean isClob() {
		return clob;
	}

	public final boolean getClob() {
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

	public final boolean getStorage() {
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

	public boolean getRequestExclusive() {
		return requestExclusive;
	}

	@Override
	public boolean isAlwaysResetConnections() {
		return alwaysResetConnections;
	}

	public boolean getAlwaysResetConnections() {
		return alwaysResetConnections;
	}

	@Override
	public final boolean getLiteralTimestampWithTSOffset() {
		return literalTimestampWithTSOffset;
	}

	@Override
	public final Boolean isMSSQL() {
		return mssql;
	}

	@Override
	public final void setMSSQL(boolean isMSSQL) {
		mssql = isMSSQL ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public Log getLog() {
		// can be null if deserialized
		if (log == null) log = ThreadLocalPageContext.getLog("application");
		return log;
	}

	@Override
	public TagListener getListener() {
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
		if (id == null) {
			id = new StringBuilder().append(getConnectionStringTranslated()).append(':').append(getConnectionLimit()).append(':').append(getConnectionTimeout()).append(':')
					.append(getLiveTimeout()).append(':').append(getMetaCacheTimeout()).append(':').append((getName() + "").toLowerCase()).append(':').append(getUsername())
					.append(':').append(getPassword()).append(':').append(validate()).append(':').append(cd == null ? "null" : cd.toString()).append(':')
					.append((getTimeZone() == null ? "null" : getTimeZone().getID())).append(':').append(isBlob()).append(':').append(isClob()).append(':').append(isReadOnly())
					.append(':').append(isStorage()).append(':').append(isRequestExclusive()).append(':').append(isAlwaysResetConnections()).toString();
		}
		return id;
	}

	@Override
	public String toString() {
		return id();
	}
}