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
	// SavedCCL holder distinguishes "saved null" from "never saved" so the re-entrancy guard works on threads with a null CCL.
	private static ThreadLocal<SavedCCL> prevCCL = new ThreadLocal<>();

	private static final class SavedCCL {
		final ClassLoader value;
		SavedCCL(ClassLoader value) { this.value = value; }
	}

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
		// capture the original CCL on the first register so release() can restore
		// it. The null check is a re-entrancy guard: Controler.run calls
		// register(config) 4x before its single release(); without the guard, the
		// second register would clobber the original save with the Lucee CCL.
		if (prevCCL.get() == null) {
			prevCCL.set(new SavedCCL(t.getContextClassLoader()));
		}
		t.setContextClassLoader(((ConfigPro) config).getClassLoaderEnv());
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
		SavedCCL prev = prevCCL.get();
		if (prev != null) {
			Thread.currentThread().setContextClassLoader(prev.value);
			prevCCL.remove();
		}
	}
}
