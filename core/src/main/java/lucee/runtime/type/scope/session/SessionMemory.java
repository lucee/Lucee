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
import lucee.runtime.PageSource;
import lucee.runtime.type.Collection;
import lucee.runtime.type.scope.Session;
import lucee.runtime.type.scope.storage.MemoryScope;
import lucee.runtime.type.scope.storage.StorageScopeMemory;

public final class SessionMemory extends StorageScopeMemory implements Session, MemoryScope {

	private static final long serialVersionUID = 7703261878730061485L;
	private transient PageSource applicationPageSource;
	private String applicationPath;

	/**
	 * Constructor of the class
	 * 
	 * @param pc
	 * @param log
	 */
	protected SessionMemory(PageContext pc, Log log) {
		super(pc, "session", SCOPE_SESSION, log);
	}

	/**
	 * Constructor of the class, clone existing
	 * 
	 * @param other
	 * @param deepCopy
	 */
	protected SessionMemory(StorageScopeMemory other, boolean deepCopy) {
		super(other, deepCopy);
	}

	/**
	 * load a new instance of the class
	 * 
	 * @param pc
	 * @param isNew
	 * @param log
	 * @return
	 */
	public static Session getInstance(PageContext pc, Log log) {
		return new SessionMemory(pc, log);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new SessionMemory(this, deepCopy);
	}

	public void setApplicationPageSource(PageSource applicationPageSource) {
		this.applicationPageSource = applicationPageSource;
		this.applicationPath = applicationPageSource == null ? null : applicationPageSource.getRealpathWithVirtual();
	}

	public PageSource getApplicationPageSource() {
		return applicationPageSource;
	}

	public String getApplicationPath() {
		return applicationPath;
	}
}