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
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_SELECT = 1;

	/**
	 * Field <code>ALLOW_DELETE</code>
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_DELETE = 2;

	/**
	 * Field <code>ALLOW_UPDATE</code>
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_UPDATE = 4;

	/**
	 * Field <code>ALLOW_INSERT</code>
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_INSERT = 8;

	/**
	 * Field <code>ALLOW_CREATE</code>
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_CREATE = 16;

	/**
	 * Field <code>ALLOW_GRANT</code>
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_GRANT = 32;

	/**
	 * Field <code>ALLOW_REVOKE</code>
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_REVOKE = 64;

	/**
	 * Field <code>ALLOW_DROP</code>
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_DROP = 128;

	/**
	 * Field <code>ALLOW_ALTER</code>
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_ALTER = 256;

	/**
	 * Field <code>ALLOW_ALL</code>
	 * 
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9, this value is ignored
	 */
	@Deprecated
	public static final int ALLOW_ALL = ALLOW_SELECT + ALLOW_DELETE + ALLOW_UPDATE + ALLOW_INSERT + ALLOW_CREATE + ALLOW_GRANT + ALLOW_REVOKE + ALLOW_DROP + ALLOW_ALTER;

	/**
	 * @deprecated use instead <code>getConnectionString()</code>
	 * @return original DSN
	 */
	@Deprecated
	public abstract String getDsnOriginal();

	/**
	 * @deprecated use instead <code>getConnectionStringTranslated()</code>
	 * @return DSN Translated
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

	public abstract Connection getConnection(Config config, String user, String pass) throws ClassException, SQLException;

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
	 * @param allow allow
	 * @return always returns true, per-datasource SQL restrictions were removed in Lucee 9
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9
	 */
	@Deprecated
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
	@Deprecated
	public abstract int getConnectionTimeout();

	public abstract int getIdleTimeout();

	public abstract int getLiveTimeout();

	/**
	 * network timeout in seconds
	 * 
	 * @return Returns the network timeout.
	 */
	public abstract int getNetworkTimeout();

	public long getMetaCacheTimeout();

	public TimeZone getTimeZone();

	/**
	 * @param key key
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
	 * @return always returns false, per-datasource SQL restrictions were removed in Lucee 9
	 * @deprecated per-datasource SQL restrictions were removed in Lucee 9
	 */
	@Deprecated
	public abstract boolean hasSQLRestriction();

	/**
	 * @return Returns the name.
	 */
	public abstract String getName();

	public abstract boolean isStorage();

	public abstract boolean validate();

	public abstract Log getLog();

	public abstract ParamSyntax getParamSyntax();

	public abstract boolean getAlwaysSetTimeout();

	/**
	 * should connections produced from this datasource be exclusive to a request or not?
	 * 
	 * @return
	 */
	public boolean isRequestExclusive();

	public boolean isAlwaysResetConnections();

	public boolean getLiteralTimestampWithTSOffset();

	public int getDefaultTransactionIsolation();

	public int getMinIdle();

	public int getMaxIdle();

	public int getMaxTotal();

	public Boolean isMSSQL();

	public void setMSSQL(boolean ismssql);
}