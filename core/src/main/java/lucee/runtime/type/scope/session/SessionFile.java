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
import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.storage.StorageScopeFile;

public class SessionFile extends StorageScopeFile implements Session {

	private static final long serialVersionUID = 3896214476118229640L;

	/**
	 * Constructor of the class
	 * 
	 * @param pc
	 * @param name
	 * @param sct
	 */
	private SessionFile(PageContext pc, Resource res, Struct sct) {
		super(pc, res, "session", SCOPE_SESSION, sct);
	}

	/**
	 * Constructor of the class, clone existing
	 * 
	 * @param other
	 */
	private SessionFile(SessionFile other, boolean deepCopy) {
		super(other, deepCopy);
	}

	/**
	 * load new instance of the class
	 * 
	 * @param name
	 * @param pc
	 * @param checkExpires
	 * @return
	 */
	public static Session getInstance(String name, PageContext pc, Log log) {

		Resource res = _loadResource(pc.getConfig(), SCOPE_SESSION, name, pc.getCFID());
		Struct data = _loadData(pc, res, log);
		return new SessionFile(pc, res, data);
	}

	public static boolean hasInstance(String name, PageContext pc) {
		Resource res = _loadResource(pc.getConfig(), SCOPE_SESSION, name, pc.getCFID());
		Struct data = _loadData(pc, res, null);
		return data != null;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new SessionFile(this, deepCopy);
	}
}