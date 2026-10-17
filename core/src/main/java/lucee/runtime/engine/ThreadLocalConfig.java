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
import lucee.runtime.config.ConfigPro;

/**
 * class to handle thread local PageContext, do use pagecontext in classes that have no method
 * argument pagecontext
 */
public final class ThreadLocalConfig {

	private static InheritableThreadLocal<Config> cThreadLocal = new InheritableThreadLocal<Config>();
	// CCL_UNSET sentinel distinguishes "never saved" from "saved a null CCL"; some boot/gateway threads legitimately have a null context classloader.
	private static final ClassLoader CCL_UNSET = new ClassLoader(null) {};
	private static ThreadLocal<ClassLoader> prevCCL = ThreadLocal.withInitial(() -> CCL_UNSET);

	/**
	 * register a Config for he current thread
	 *
	 * @param config Config to register
	 */
	public static void register(Config config) {
		cThreadLocal.set(config);
		if (config == null) {
			return;
		}
		Thread t = Thread.currentThread();
		ClassLoader target = ((ConfigPro) config).getClassLoaderEnv();
		ClassLoader current = t.getContextClassLoader();
		// fast path: re-entrant register where the CCL already matches (e.g. Controler.run's 4x register cycle)
		if (current == target) return;
		// first register on this thread captures the pre-Lucee CCL; nested register with a different target keeps the original save
		if (prevCCL.get() == CCL_UNSET) {
			prevCCL.set(current);
		}
		t.setContextClassLoader(target);
	}

	/**
	 * returns Config registered for the current thread
	 *
	 * @return Config for the current thread or null
	 */
	static Config get() {
		return cThreadLocal.get();
	}

	/**
	 * release the pagecontext for the current thread
	 */
	public static void release() {
		cThreadLocal.set(null);
		ClassLoader prev = prevCCL.get();
		if (prev != CCL_UNSET) {
			Thread.currentThread().setContextClassLoader(prev);
			prevCCL.remove();
		}
	}
}
