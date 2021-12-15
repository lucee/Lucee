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
import lucee.commons.lang.types.RefBooleanImpl;

public class ThreadLocalDuplication {

	private static ThreadLocal<Map<Object, Object>> local = new ThreadLocal<Map<Object, Object>>();
	private static ThreadLocal<RefBoolean> inside = new ThreadLocal<RefBoolean>();

	public static boolean set(Object o, Object c) {
		touch(true).put(o, c);
		return isInside();
	}

	/*
	 * public static Map<Object, Object> getMap() { return touch(); }
	 * 
	 * public static void removex(Object o) { touch().remove(o); }
	 */

	/*
	 * private static Object get(Object obj) { Map<Object,Object> list = touch(); return list.get(obj);
	 * }
	 */

	public static Object get(Object object, RefBoolean before) {
		if (!isInside()) {
			reset();
			setIsInside(true);
			before.setValue(false);
		}
		else before.setValue(true);

		Map<Object, Object> list = touch(false);
		return list == null ? null : list.get(object);
	}

	private static Map<Object, Object> touch(boolean createIfNecessary) {
		Map<Object, Object> set = local.get();
		if (set == null) {
			if (!createIfNecessary) return null;

			set = new IdentityHashMap<Object, Object>();// it is importend to have a reference comparsion here
			local.set(set);
		}
		return set;
	}

	public static void reset() {
		Map<Object, Object> set = local.get();
		if (set != null) set.clear();
		setIsInside(false);
	}

	private static boolean isInside() {
		RefBoolean b = inside.get();
		return b != null && b.toBooleanValue();
	}

	private static void setIsInside(boolean isInside) {
		RefBoolean b = inside.get();
		if (b == null) inside.set(new RefBooleanImpl(isInside));
		else b.setValue(isInside);
	}

}