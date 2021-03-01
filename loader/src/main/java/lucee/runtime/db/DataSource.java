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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

import org.osgi.framework.BundleException;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ClassException;
import lucee.runtime.config.Config;
import lucee.runtime.type.Struct;

/**
 * interface for a datasource
 */
public interface DataSource extends Cloneable {

	/**
	 * Field <code>ALLOW_SELECT</code>
	 */
	public static final int ALLOW_SELECT = 1;

	/**
	 * Field <code>ALLOW_DELETE</code>
	 */
	public static final int ALLOW_DELETE = 2;

	/**
	 * Field <code>ALLOW_UPDATE</code>
	 */
	public static final int ALLOW_UPDATE = 4;

	/**
	 * Field <code>ALLOW_INSERT</code>
	 */
	public static final int ALLOW_INSERT = 8;

	/**
	 * Field <code>ALLOW_CREATE</code>
	 */
	public static final int ALLOW_CREATE = 16;

	/**
	 * Field <code>ALLOW_GRANT</code>
	 */
	public static final int ALLOW_GRANT = 32;

	/**
	 * Field <code>ALLOW_REVOKE</code>
	 */
	public static final int ALLOW_REVOKE = 64;

	/**
	 * Field <code>ALLOW_DROP</code>
	 */
	public static final int ALLOW_DROP = 128;

	/**
	 * Field <code>ALLOW_ALTER</code>
	 */
	public static final int ALLOW_ALTER = 256;

	/**
	 * Field <code>ALLOW_ALL</code>
	 */
	public static final int ALLOW_ALL = ALLOW_SELECT + ALLOW_DELETE + ALLOW_UPDATE + ALLOW_INSERT + ALLOW_CREATE + ALLOW_GRANT + ALLOW_REVOKE + ALLOW_DROP + ALLOW_ALTER;

	/**
	 * @deprecated use instead <code>getConnectionString()</code>
	 */
	@Deprecated
	public abstract String getDsnOriginal();

	/**
	 * @deprecated use instead <code>getConnectionStringTranslated()</code>
	 */
	@Deprecated
	public abstract String getDsnTranslated();

	/**
	 * @return Returns the connection string with NOT replaced placeholders.
	 */
	public String getConnectionString();

	/**
	 * @return unique id of the DataSource
	 */
	public String id();

	/**
	 * @return Returns the connection string with replaced placeholders.
	 */
	public abstract String getConnectionStringTranslated();

	public abstract Connection getConnection(Config config, String user, String pass) throws ClassException, BundleException, SQLException;

	/**
	 * @return Returns the password.
	 */
	public abstract String getPassword();

	/**
	 * @return Returns the username.
	 */
	public abstract String getUsername();

	/**
	 * @return Returns the readOnly.
	 */
	public abstract boolean isReadOnly();

	/**
	 * @param allow
	 * @return returns if given allow exists
	 */
	public abstract boolean hasAllow(int allow);

	/**
	 * @return Returns the clazz.
	 */
	@SuppressWarnings("rawtypes")
	public abstract ClassDefinition getClassDefinition();

	/**
	 * @return Returns the database.
	 */
	public abstract String getDatabase();

	/**
	 * @return Returns the port.
	 */
	public abstract int getPort();

	/**
	 * @return Returns the host.
	 */
	public abstract String getHost();

	/**
	 * @return cloned Object
	 */
	public abstract Object clone();

	/**
	 * @return clone the DataSource as ReadOnly
	 */
	public abstract DataSource cloneReadOnly();

	/**
	 * @return Returns the blob.
	 */
	public abstract boolean isBlob();

	/**
	 * @return Returns the clob.
	 */
	public abstract boolean isClob();

	/**
	 * @return Returns the connectionLimit.
	 */
	public abstract int getConnectionLimit();

	/**
	 * @return Returns the connection idle timeout.
	 */
	// FUTURE @Deprecated
	public abstract int getConnectionTimeout();

	// FUTURE public abstract int getIdleTimeout();
	// FUTURE public abstract int getLiveTimeout();

	/**
	 * network timeout in seconds
	 * 
	 * @return
	 */
	public abstract int getNetworkTimeout();

	public long getMetaCacheTimeout();

	public TimeZone getTimeZone();

	/**
	 * @param key
	 * @return Returns matching custom value or null if not exist.
	 */
	public abstract String getCustomValue(String key);

	/**
	 * @return returns all custom names
	 */
	public abstract String[] getCustomNames();

	/**
	 * @return returns custom
	 */
	public abstract Struct getCustoms();

	/**
	 * @return returns if database has a SQL restriction
	 */
	public abstract boolean hasSQLRestriction();

	/**
	 * @return Returns the name.
	 */
	public abstract String getName();

	public abstract boolean isStorage();

	public abstract boolean validate();

	public abstract Log getLog();
}