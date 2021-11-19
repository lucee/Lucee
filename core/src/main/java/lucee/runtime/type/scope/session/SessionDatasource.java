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
package lucee.runtime.type.scope.session;

import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.storage.StorageScopeDatasource;

public class SessionDatasource extends StorageScopeDatasource implements Session {

	private SessionDatasource(PageContext pc, String datasourceName, Struct sct) {
		super(pc, datasourceName, "session", SCOPE_SESSION, sct);
	}

	/**
	 * Constructor of the class, clone existing
	 * 
	 * @param other
	 */
	private SessionDatasource(StorageScopeDatasource other, boolean deepCopy) {
		super(other, deepCopy);
	}

	/**
	 * load an new instance of the client datasource scope
	 * 
	 * @param datasourceName
	 * @param appName
	 * @param pc
	 * @param checkExpires
	 * @return client datasource scope
	 * @throws PageException
	 */
	public static Session getInstance(String datasourceName, PageContext pc, Log log) throws PageException {

		Struct _sct = _loadData(pc, datasourceName, "session", SCOPE_SESSION, log, false);
		if (_sct == null) _sct = new StructImpl();

		return new SessionDatasource(pc, datasourceName, _sct);
	}

	public static Session getInstance(String datasourceName, PageContext pc, Log log, Session defaultValue) {
		try {
			return getInstance(datasourceName, pc, log);
		}
		catch (PageException e) {
		}
		return defaultValue;
	}

	public static boolean hasInstance(String datasourceName, PageContext pc) {
		try {
			return _loadData(pc, datasourceName, "session", SCOPE_SESSION, null, false) != null;
		}
		catch (PageException e) {
			return false;
		}
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new SessionDatasource(this, deepCopy);
	}
}