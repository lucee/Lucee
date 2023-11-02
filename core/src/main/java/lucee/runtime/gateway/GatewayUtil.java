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
package lucee.runtime.gateway;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

public class GatewayUtil {

	public static Object toCFML(Object obj) {
		if (obj instanceof Map) return toCFML((Map) obj);
		if (obj instanceof List) return toCFML((List) obj);
		return obj;
	}

	public static Map toCFML(Map map) {
		Iterator it = map.entrySet().iterator();
		Map.Entry entry;
		while (it.hasNext()) {
			entry = (Entry) it.next();
			entry.setValue(toCFML(entry.getValue()));
		}
		return map;
	}

	public static Object toCFML(List list) {
		ListIterator it = list.listIterator();
		int index;
		while (it.hasNext()) {
			index = it.nextIndex();
			list.set(index, toCFML(it.next()));

		}
		return list;
	}

	public static int getState(GatewayEntry ge) { // this method only exists to make sure the Gateway interface must not be used outsite the gateway
		// package
		return ge.getGateway().getState();
	}

}