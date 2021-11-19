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
import lucee.runtime.CFMLFactoryImpl;

public class StorageScopeEngine {

	private StorageScopeCleaner[] cleaners;

	private CFMLFactoryImpl factory;

	private Log log;

	public StorageScopeEngine(CFMLFactoryImpl factory, Log log, StorageScopeCleaner[] cleaners) {
		this.cleaners = cleaners;
		this.factory = factory;
		this.log = log;

		for (int i = 0; i < cleaners.length; i++) {
			cleaners[i].init(this);
		}
	}

	public void clean() {
		for (int i = 0; i < cleaners.length; i++) {
			cleaners[i].clean();
		}
	}

	/**
	 * @return the factory
	 */
	public CFMLFactoryImpl getFactory() {
		return factory;
	}

	/**
	 * @return the log
	 */
	public Log _getLog() {
		return log;
	}

	public void remove(int type, String appName, String cfid) {

		getFactory().getScopeContext().remove(type, appName, cfid);
	}
}