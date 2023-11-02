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

/**
 * class to handle thread local PageContext, do use pagecontext in classes that have no method
 * argument pagecontext
 */
public final class ThreadLocalWDDXResult {

	private static ThreadLocal resultThreadLocal = new ThreadLocal();

	/**
	 * register a pagecontext for he current thread
	 * 
	 * @param pc PageContext to register
	 */
	public static void set(Object result) {
		resultThreadLocal.set(result);
	}

	public static Object get() {
		return resultThreadLocal.get();
	}

	public static void release() {
		resultThreadLocal.set(null);
	}

}