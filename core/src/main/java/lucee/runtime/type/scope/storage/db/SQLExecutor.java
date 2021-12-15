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
package lucee.runtime.type.scope.storage.db;

import java.sql.SQLException;

import lucee.commons.io.log.Log;
import lucee.runtime.config.Config;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Query;
import lucee.runtime.type.scope.storage.StorageScopeEngine;
import lucee.runtime.type.scope.storage.StorageScopeListener;
import lucee.runtime.type.scope.storage.clean.DatasourceStorageScopeCleaner;

public interface SQLExecutor {

	/**
	 * does a select statement on the datasource to get data
	 * 
	 * @param config Config of the current context
	 * @param cfid CFID of the current user
	 * @param applicationName name of the current application context
	 * @param dc Datasource Connection to use
	 * @param type storage type (Scope.SCOPE_CLIENT,Scope.SCOPE_SESSION)
	 * @param log
	 * @param createTableIfNotExist do create the table if not existing
	 * @return data matching criteria
	 * @throws PageException
	 * @throws SQLException
	 */
	public Query select(Config config, String cfid, String applicationName, DatasourceConnection dc, int type, Log log, boolean createTableIfNotExist)
			throws PageException, SQLException;

	/**
	 * updates the data in the datasource for a specific user (CFID), if the data not exist, a new
	 * record is created
	 * 
	 * @param config Config of the current context
	 * @param cfid CFID of the current user
	 * @param applicationName name of the current application context
	 * @param dc Datasource Connection to use
	 * @param type storage type (Scope.SCOPE_CLIENT,Scope.SCOPE_SESSION)
	 * @param data data to store
	 * @param timeSpan timespan in millis
	 * @param log
	 * @throws PageException
	 * @throws SQLException
	 */
	public void update(Config config, String cfid, String applicationName, DatasourceConnection dc, int type, Object data, long timeSpan, Log log)
			throws PageException, SQLException;

	/**
	 * deletes the data in the datasource for a specific user (CFID), if there is no data for this user
	 * nothing is happeing
	 * 
	 * @param config Config of the current context
	 * @param cfid CFID of the current user
	 * @param applicationName name of the current application context
	 * @param dc Datasource Connection to use
	 * @param type storage type (Scope.SCOPE_CLIENT,Scope.SCOPE_SESSION)
	 * @param log
	 * @throws PageException
	 * @throws SQLException
	 */
	public void delete(Config config, String cfid, String applicationName, DatasourceConnection dc, int type, Log log) throws PageException, SQLException;

	public void clean(Config config, DatasourceConnection dc, int type, StorageScopeEngine engine, DatasourceStorageScopeCleaner cleaner, StorageScopeListener listener, Log log)
			throws PageException, SQLException;

}