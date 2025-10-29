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
 * class to handle thread local Config, do use config in classes that have no method
 * argument config
 */
public final class ThreadLocalConfig {

	// Java 25: ScopedValue replaces InheritableThreadLocal for automatic cleanup
	public static final ScopedValue<Config> CURRENT = ScopedValue.newInstance();

	/**
	 * register a Config for the current thread
	 *
	 * @param config Config to register
	 * @deprecated With ScopedValue this doesn't actually register - scope must be established via ScopedValue.where()
	 * This method is kept for backwards compatibility in background threads that don't have a proper scope
	 */
	@Deprecated
	public static void register(Config config) {
		// No-op: With ScopedValue, scope must be established via ScopedValue.where()
		// This method exists for backwards compatibility but does nothing
		if (config == null) {
			return;
		}
		Thread t = Thread.currentThread();
		t.setContextClassLoader(((ConfigPro) config).getClassLoaderEnv());
	}

	/**
	 * returns Config registered for the current thread
	 *
	 * @return Config for the current thread or null
	 */
	static Config get() {
		return CURRENT.isBound() ? CURRENT.get() : null;
	}

	/**
	 * release the config for the current thread
	 * @deprecated With ScopedValue this is a no-op - scope is automatically cleaned up
	 */
	@Deprecated
	public static void release() {
		// No-op: ScopedValue automatically cleans up when scope exits
	}
}