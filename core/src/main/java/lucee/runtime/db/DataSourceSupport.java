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
import java.lang.ref.SoftReference;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;



import lucee.commons.io.log.Log;
import lucee.commons.lang.ClassException;
import lucee.runtime.config.Config;

import org.apache.commons.collections4.map.ReferenceMap;
import org.osgi.framework.BundleException;

public abstract class DataSourceSupport implements DataSource, Cloneable {

	private static final int NETWORK_TIMEOUT_IN_SECONDS = 10;
	private ClassDefinition cd;
	private final boolean blob;
	private final boolean clob;
	private final int connectionLimit;
	private final int connectionTimeout;
	private final long metaCacheTimeout;
	private final TimeZone timezone;
	private final String name;
	private final boolean storage;
	protected final int allow;
	private final boolean readOnly;
	private final String username;
	private final String password;

	private Map<String, SoftReference<ProcMetaCollection>> procedureColumnCache;
	private Driver driver;

	private final Log log;

	public DataSourceSupport(Config config, String name,
			ClassDefinition cd, String username, String password, boolean blob,
			boolean clob, int connectionLimit, int connectionTimeout,
			long metaCacheTimeout, TimeZone timezone, int allow,
			boolean storage, boolean readOnly, Log log) {
		this.name = name;
		this.cd = cd;//_initializeCD(null, cd, config);
		this.blob = blob;
		this.clob = clob;
		this.connectionLimit = connectionLimit;
		this.connectionTimeout = connectionTimeout;
		this.metaCacheTimeout = metaCacheTimeout;
		this.timezone = timezone;
		this.allow = allow;
		this.storage = storage;
		this.readOnly = readOnly;
		this.username = username;
		this.password = password;
		this.log = log;
	}
	
	@Override
	public Connection getConnection(Config config, String user, String pass)
			throws ClassException, BundleException, SQLException {
		try {
			if (user == null)
				user = username;
			if (pass == null)
				pass = password;
			return _getConnection(config, initialize(config), SQLUtil.connectionStringTranslatedPatch(config, getConnectionStringTranslated()), user, pass);

		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Connection _getConnection(Config config, Driver driver,
			String connStrTrans, String user, String pass) throws SQLException {
		java.util.Properties props = new java.util.Properties();
		if (user != null)
			props.put("user", user);
		if (pass != null)
			props.put("password", pass);

		return driver.connect(connStrTrans, props);
	}

	private Driver initialize(Config config) throws BundleException, InstantiationException, IllegalAccessException, IOException {
		if (driver == null) {
			return driver = _initializeDriver(cd, config);
		}
		return driver;
	}

	/*private static ClassDefinition _initializeCD(JDBCDriver jdbc, ClassDefinition cd, Config config) {
		// try to link the class defintion with a jdbc driver defintion
		if (!cd.isBundle()) {
			if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cd
					.getClassName())) {
				cd = new ClassDefinitionImpl(
						"com.microsoft.sqlserver.jdbc.SQLServerDriver",
						cd.getName(), cd.getVersionAsString(), null);
			}

			ConfigImpl ci = ((ConfigImpl) ThreadLocalPageContext.getConfig(config));
			JDBCDriver tmp = jdbc != null ? ci.getJDBCDriverByCD(jdbc.cd, null) : null;
			if (tmp == null) tmp = ((ConfigImpl) config).getJDBCDriverByClassName(cd.getClassName(), null);
			
			// we have a matching jdbc driver found
			if (tmp != null) {
				cd = tmp.cd;
			}
		}
		return cd;
	}*/

	private static Driver _initializeDriver(ClassDefinition cd, Config config)
			throws ClassException, BundleException, InstantiationException,
			IllegalAccessException {
		// load the class
		Class clazz = cd.getClazz();
		return (Driver) clazz.newInstance();
	}

	public static void verify(Config config, ClassDefinition cd, String connStrTranslated, String user, String pass) throws ClassException, BundleException, SQLException {
		try {
			//Driver driver = _initializeDriver(_initializeCD(jdbc, cd, config),config);
			Driver driver = _initializeDriver(cd,config);
			_getConnection(config, driver, connStrTranslated, user, pass);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
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
		return connectionTimeout;
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
	public Log getLog() {
		return log;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DataSource))
			return false;
		DataSource ds = (DataSource) obj;
		return id().equals(ds.id());
	}

	@Override
	public int hashCode() {
		return id().hashCode();
	}

	@Override
	public String id() {

		return new StringBuilder(getConnectionStringTranslated())
				.append(':')
				.append(getConnectionLimit())
				.append(':')
				.append(getConnectionTimeout())
				.append(':')
				.append(getMetaCacheTimeout())
				.append(':')
				.append(getName().toLowerCase())
				.append(':')
				.append(getUsername())
				.append(':')
				.append(getPassword())
				.append(':')
				.append(cd.toString())
				.append(':')
				.append((getTimeZone() == null ? "null" : getTimeZone().getID()))
				.append(':').append(isBlob()).append(':').append(isClob())
				.append(':').append(isReadOnly()).append(':')
				.append(isStorage()).toString();

	}

	@Override
	public String toString() {
		return id();
	}
}