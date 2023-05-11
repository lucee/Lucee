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
package lucee.runtime.type.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;

public class CollectionUtil {

	public static final Object NULL = new Object();

	public static boolean equals(Collection left, Collection right) {
		if (left.size() != right.size()) return false;
		Iterator<Key> it = left.keyIterator();
		Key k;
		Object l, r;
		while (it.hasNext()) {
			k = it.next();
			r = right.get(k, NULL);
			if (r == NULL) return false;
			l = left.get(k, NULL);
			if (!OpUtil.equalsEL(ThreadLocalPageContext.get(), r, l, false, true)) return false;
		}
		return true;
	}

	/*
	 * public static String[] toStringArray(Key[] keys) { if(keys==null) return null; String[] arr=new
	 * String[keys.length]; for(int i=0;i<keys.length;i++){ arr[i]=keys[i].getString(); } return arr; }
	 */

	public static String getKeyList(Iterator<Key> it, String delimiter) {
		StringBuilder sb = new StringBuilder(it.next().getString());
		if (delimiter.length() == 1) {
			char c = delimiter.charAt(0);
			while (it.hasNext()) {
				sb.append(c);
				sb.append(it.next().getString());
			}
		}
		else {
			while (it.hasNext()) {
				sb.append(delimiter);
				sb.append(it.next().getString());
			}
		}
		return sb.toString();
	}

	public static String getKeyList(Collection coll, String delimiter) {
		if (coll.size() == 0) return "";
		return getKeyList(coll.keyIterator(), delimiter);
	}

	public static Key[] keys(Collection coll) {
		if (coll == null) return new Key[0];
		Iterator<Key> it = coll.keyIterator();
		List<Key> rtn = new ArrayList<Key>();
		if (it != null) while (it.hasNext()) {
			rtn.add(it.next());
		}
		return rtn.toArray(new Key[rtn.size()]);
	}

	public static String[] keysAsString(Collection coll) {
		if (coll == null) return new String[0];
		Iterator<Key> it = coll.keyIterator();
		List<String> rtn = new ArrayList<String>();
		if (it != null) while (it.hasNext()) {
			rtn.add(it.next().getString());
		}
		return rtn.toArray(new String[rtn.size()]);
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.size() == 0;
	}

	/*
	 * public static int hashCode(Collection coll) { produce infiniti loop when there is a refrerence to
	 * itself or an anchestor
	 * 
	 * int hashCode = 1; Iterator<Entry<Key, Object>> it = coll.entryIterator(); Entry<Key, Object> e;
	 * while(it.hasNext()) { e = it.next(); hashCode = 31*hashCode+
	 * 
	 * ( (e.getKey()==null?0:e.getKey().hashCode()) ^ (e.getValue()==null ? 0 : e.getValue().hashCode())
	 * ); } return hashCode; }
	 */

	public static Collection.Key[] toKeys(String[] strArr, boolean trim) {
		Collection.Key[] keyArr = new Collection.Key[strArr.length];
		for (int i = 0; i < keyArr.length; i++) {
			keyArr[i] = KeyImpl.init(trim ? strArr[i].trim() : strArr[i]);
		}
		return keyArr;
	}

	public static Collection.Key[] toKeys(Set<String> set) {
		Collection.Key[] keyArr = new Collection.Key[set.size()];
		Iterator<String> it = set.iterator();
		int index = 0;
		while (it.hasNext()) {
			keyArr[index++] = KeyImpl.init(it.next());
		}
		return keyArr;
	}

	public static String[] toString(Collection.Key[] keys, boolean trim) {
		if (keys == null) return null;

		String[] data = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			data[i] = trim ? keys[i].getString().trim() : keys[i].getString();
		}
		return data;
	}

	public static <T> T remove(List<T> list, int index, T defaultValue) {
		try {
			return list.remove(index);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}
}