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
package lucee.runtime.op;

import java.util.IdentityHashMap;
import java.util.Map;

import lucee.commons.lang.types.RefBoolean;

public final class ThreadLocalDuplication {

	// Java 25: ScopedValue replaces ThreadLocal for automatic cleanup and tracking of duplication cycles
	public static final ScopedValue<Map<Object, Object>> DUPLICATION_MAP = ScopedValue.newInstance();

	public static boolean set(Object o, Object c) {
		if (!DUPLICATION_MAP.isBound()) {
			throw new IllegalStateException("Duplication map not bound - must establish scope first via ScopedValue.where()");
		}
		DUPLICATION_MAP.get().put(o, c);
		return true; // Always inside when scope is bound
	}

	public static Object get(Object object, RefBoolean before) {
		if (!DUPLICATION_MAP.isBound()) {
			before.setValue(false);
			return null;
		}
		before.setValue(true);
		return DUPLICATION_MAP.get().get(object);
	}

	/**
	 * @deprecated With ScopedValue this is a no-op - scope cleanup is automatic
	 */
	@Deprecated
	public static void reset() {
		// No-op: ScopedValue automatically cleans up when scope exits
	}

}