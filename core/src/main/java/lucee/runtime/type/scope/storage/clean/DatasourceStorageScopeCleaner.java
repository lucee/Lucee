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
package lucee.runtime.type.scope.storage.clean;

import java.sql.SQLException;

import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceConnectionPool;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.scope.storage.StorageScopeEngine;
import lucee.runtime.type.scope.storage.StorageScopeListener;
import lucee.runtime.type.scope.storage.db.SQLExecutionFactory;
import lucee.runtime.type.scope.storage.db.SQLExecutor;

public class DatasourceStorageScopeCleaner extends StorageScopeCleanerSupport {

	// private String strType;

	public DatasourceStorageScopeCleaner(int type, StorageScopeListener listener) {
		super(type, listener, INTERVALL_HOUR);
		// this.strType=VariableInterpreter.scopeInt2String(type);
	}

	@Override
	public void init(StorageScopeEngine engine) {
		super.init(engine);
	}

	@Override
	protected void _clean() {
		ConfigWeb config = engine.getFactory().getConfig();
		DataSource[] datasources = config.getDataSources();
		for (int i = 0; i < datasources.length; i++) {
			if (datasources[i].isStorage()) {
				try {
					clean(config, datasources[i]);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					error(t);
				}
			}
		}
	}

	private void clean(ConfigWeb config, DataSource dataSource) throws PageException, SQLException {
		ConfigWebPro cwi = (ConfigWebPro) config;
		DatasourceConnection dc = null;

		DatasourceConnectionPool pool = cwi.getDatasourceConnectionPool();
		try {
			dc = pool.getDatasourceConnection(null, dataSource, null, null);
			Log log = config.getLog("scope");
			SQLExecutor executor = SQLExecutionFactory.getInstance(dc);
			executor.clean(config, dc, type, engine, this, listener, log);
		}
		finally {
			if (dc != null) pool.releaseDatasourceConnection(dc);
		}
	}
}