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
package lucee.runtime.engine;

import lucee.runtime.config.Config;

/**
 * class to handle thread local PageContext, do use pagecontext in classes that have no method
 * argument pagecontext
 */
public final class ThreadLocalConfig {

	private static ThreadLocal cThreadLocal = new ThreadLocal();

	/**
	 * register a Config for he current thread
	 * 
	 * @param config Config to register
	 */
	public static void register(Config config) {// DO NOT CHANGE, used in Ortus extension via reflection
		cThreadLocal.set(config);
	}

	/**
	 * returns Config registered for the current thread
	 * 
	 * @return Config for the current thread or null
	 */
	static Config get() {
		return (Config) cThreadLocal.get();
	}

	/**
	 * release the pagecontext for the current thread
	 */
	public static void release() {// DO NOT CHANGE, used in Ortus extension via reflection
		cThreadLocal.set(null);
	}
}