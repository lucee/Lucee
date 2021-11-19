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

import java.util.TimeZone;

import lucee.commons.io.log.Log;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.Struct;

public class ApplicationDataSource extends DataSourceSupport {

	private String connStr;

	private ApplicationDataSource(Config config, String name, ClassDefinition cd, String connStr, String username, String password, TagListener listener, boolean blob,
			boolean clob, int connectionLimit, int idleTimeout, int liveTimeout, long metaCacheTimeout, TimeZone timezone, int allow, boolean storage, boolean readOnly,
			boolean validate, boolean requestExclusive, boolean alwaysResetConnections, boolean literalTimestampWithTSOffset, Log log) {
		super(config, name, cd, username, ConfigWebUtil.decrypt(password), listener, blob, clob, connectionLimit, idleTimeout, liveTimeout, metaCacheTimeout, timezone,
				allow < 0 ? ALLOW_ALL : allow, storage, readOnly, validate, requestExclusive, alwaysResetConnections, literalTimestampWithTSOffset, log);

		this.connStr = connStr;
	}

	public static DataSource getInstance(Config config, String name, ClassDefinition cd, String connStr, String username, String password, TagListener listener, boolean blob,
			boolean clob, int connectionLimit, int idleTimeout, int liveTimeout, long metaCacheTimeout, TimeZone timezone, int allow, boolean storage, boolean readOnly,
			boolean validate, boolean requestExclusive, boolean alwaysResetConnections, boolean literalTimestampWithTSOffset, Log log) {

		return new ApplicationDataSource(config, name, cd, connStr, username, password, listener, blob, clob, connectionLimit, idleTimeout, liveTimeout, metaCacheTimeout, timezone,
				allow, storage, readOnly, validate, requestExclusive, alwaysResetConnections, literalTimestampWithTSOffset, log);
	}

	@Override
	public String getDsnOriginal() {
		throw exp();
	}

	@Override
	public String getConnectionString() {
		throw exp();
	}

	@Override
	public String getDsnTranslated() {
		return getConnectionStringTranslated();
	}

	@Override
	public String getConnectionStringTranslated() {
		return connStr;
	}

	@Override
	public String getDatabase() {
		throw new PageRuntimeException(new ApplicationException("Datasource defined in the application event handler has no name."));
	}

	@Override
	public int getPort() {
		throw exp();
	}

	@Override
	public String getHost() {
		throw exp();
	}

	@Override
	public DataSource cloneReadOnly() {
		try {
			return new ApplicationDataSource(ThreadLocalPageContext.getConfig(), getName(), getClassDefinition(), connStr, getUsername(), getPassword(), getListener(), isBlob(),
					isClob(), getConnectionLimit(), getIdleTimeout(), getLiveTimeout(), getMetaCacheTimeout(), getTimeZone(), allow, isStorage(), isReadOnly(), validate(),
					isRequestExclusive(), isAlwaysResetConnections(), getLiteralTimestampWithTSOffset(), getLog());
		}
		catch (Exception e) {
			throw new RuntimeException(e);// this should never happens, because the class was already loaded in this object
		}
	}

	@Override
	public String getCustomValue(String key) {
		throw exp();
	}

	@Override
	public String[] getCustomNames() {
		throw exp();
	}

	@Override
	public Struct getCustoms() {
		throw exp();
	}

	private PageRuntimeException exp() {
		// return new MethodNotSupportedException();
		throw new PageRuntimeException(new ApplicationException("method not supported"));
	}
}