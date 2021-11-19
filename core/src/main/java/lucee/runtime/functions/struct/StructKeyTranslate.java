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
package lucee.runtime.functions.struct;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class StructKeyTranslate extends BIF {

	private static final long serialVersionUID = -7978129950865681102L;

	public static double call(PageContext pc, Struct sct) throws PageException {
		return call(pc, sct, false, false);
	}

	public static double call(PageContext pc, Struct sct, boolean deepTranslation) throws PageException {
		return call(pc, sct, deepTranslation, false);
	}

	public static double call(PageContext pc, Struct sct, boolean deepTranslation, boolean leaveOriginalKey) throws PageException {
		return translate(sct, deepTranslation, leaveOriginalKey);
	}

	private static int translate(Collection coll, boolean deep, boolean leaveOrg) throws PageException {
		Key[] keys = coll.keys(); // we do not entry to avoid ConcurrentModificationException

		boolean isStruct = coll instanceof Struct;
		String key;
		Object value;
		int index;
		int count = 0;
		for (Key k: keys) {
			key = k.getString();
			value = coll.get(k);
			if (deep) count += translate(value, leaveOrg);
			if (isStruct && (index = key.indexOf('.')) != -1) {
				count++;
				translate(index, k, key, coll, leaveOrg);
			}
		}
		return count;
	}

	private static int translate(Object value, boolean leaveOrg) throws PageException {
		if (value instanceof Collection) return translate((Collection) value, true, leaveOrg);
		if (value instanceof List) return translate((List<?>) value, leaveOrg);
		if (value instanceof Map) return translate((Map<?, ?>) value, leaveOrg);
		if (Decision.isArray(value)) return translate(Caster.toNativeArray(value), leaveOrg);
		return 0;
	}

	private static int translate(List<?> list, boolean leaveOrg) throws PageException {
		Iterator<?> it = list.iterator();
		int count = 0;
		while (it.hasNext()) {
			count += translate(it.next(), leaveOrg);
		}
		return count;
	}

	private static int translate(Map<?, ?> map, boolean leaveOrg) throws PageException {
		Iterator<?> it = map.entrySet().iterator();
		int count = 0;
		while (it.hasNext()) {
			count += translate(((Map.Entry<?, ?>) it.next()).getValue(), leaveOrg);
		}
		return count;
	}

	private static int translate(Object[] arr, boolean leaveOrg) throws PageException {
		int count = 0;
		for (int i = 0; i < arr.length; i++) {
			count += translate(arr[i], leaveOrg);
		}
		return count;
	}

	private static void translate(int index, Key key, String strKey, Collection coll, boolean leaveOrg) throws PageException {
		String left;
		Object value = leaveOrg ? coll.get(key) : coll.remove(key);
		do {
			left = strKey.substring(0, index);
			strKey = strKey.substring(index + 1);
			coll = touch(coll, KeyImpl.init(left));

		}
		while ((index = strKey.indexOf('.')) != -1);
		coll.set(KeyImpl.init(strKey), value);
	}

	private static Collection touch(Collection coll, Key key) throws PageException {
		Object obj = coll.get(key, null);
		if (obj instanceof Collection) return (Collection) obj;
		if (Decision.isCastableToStruct(obj)) return Caster.toStruct(obj);
		coll.set(key, coll = new StructImpl());
		return coll;

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toBooleanValue(args[1]), Caster.toBooleanValue(args[2]));
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toBooleanValue(args[1]));
		if (args.length == 1) return call(pc, Caster.toStruct(args[0]));
		throw new FunctionException(pc, "StructKeyTranslate", 1, 3, args.length);
	}

}