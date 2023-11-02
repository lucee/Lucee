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
package lucee.runtime.type.scope.client;

import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Client;
import lucee.runtime.type.scope.storage.StorageScopeCookie;

public final class ClientCookie extends StorageScopeCookie implements Client {

	private static final long serialVersionUID = 4203695198240254464L;
	private static final String TYPE = "CLIENT";

	private ClientCookie(PageContext pc, String cookieName, Struct sct) {
		super(pc, cookieName, "client", SCOPE_CLIENT, sct);
	}

	/**
	 * Constructor of the class, clone existing
	 * 
	 * @param other
	 */
	private ClientCookie(ClientCookie other, boolean deepCopy) {
		super(other, deepCopy);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new ClientCookie(this, deepCopy);
	}

	/**
	 * load new instance of the class
	 * 
	 * @param name
	 * @param pc
	 * @param log
	 * @return
	 */
	public static Client getInstance(String name, PageContext pc, Log log) {
		if (!StringUtil.isEmpty(name)) name = StringUtil.toUpperCase(StringUtil.toVariableName(name));
		String cookieName = "CF_" + TYPE + "_" + name;
		return new ClientCookie(pc, cookieName, _loadData(pc, cookieName, SCOPE_CLIENT, "client", log));
	}
}