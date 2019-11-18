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
package lucee.runtime.type.scope.storage;

import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.ScopeContext;

/**
 * client scope that not store it's data
 */
public abstract class StorageScopeMemory extends StorageScopeImpl implements MemoryScope {

	private static final long serialVersionUID = -6917303245683342065L;

	/**
	 * Constructor of the class
	 *
	 * @param pc
	 * @param log
	 * @param name
	 */
	protected StorageScopeMemory(PageContext pc, String strType, int type, Log log) {
		super(new StructImpl(), new DateTimeImpl(pc.getConfig()), null, -1, 1, strType, type);
		ScopeContext.debug(log, "create new memory based " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID());

	}

	/**
	 * Constructor of the class, clone existing
	 *
	 * @param other
	 */
	protected StorageScopeMemory(StorageScopeMemory other, boolean deepCopy) {
		super(other, deepCopy);
	}

	@Override
	public String getStorageType() {
		return "Memory";
	}
}