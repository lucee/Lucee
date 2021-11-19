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
package lucee.runtime.dump;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalDump {

	private static ThreadLocal<Map<Object, String>> local = new ThreadLocal<Map<Object, String>>();

	public static void set(Object o, String c) {

		touch().put(o, c);
	}

	public static Map<Object, String> getMap() {
		return touch();
	}

	public static void remove(Object o) {
		touch().remove(o);
	}

	public static String get(Object obj) {
		Map<Object, String> list = touch();
		return list.get(obj);
	}

	private static Map<Object, String> touch() {
		Map<Object, String> set = local.get();
		if (set == null) {
			set = new HashMap<Object, String>();
			local.set(set);
		}
		return set;
	}

}