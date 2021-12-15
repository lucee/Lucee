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
/**
 * Implements the CFML Function structfindkey
 */
package lucee.runtime.functions.struct;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.wrap.ListAsArray;
import lucee.runtime.type.wrap.MapAsStruct;

public final class StructFindKey extends BIF {

	private static final long serialVersionUID = 598706098288773975L;

	public static Array call(PageContext pc, lucee.runtime.type.Struct struct, String value) throws PageException {
		return _call(pc, struct, value, false);
	}

	public static Array call(PageContext pc, Struct struct, String value, String scope) throws PageException {
		// Scope
		boolean all = false;
		if (scope.equalsIgnoreCase("one")) all = false;
		else if (scope.equalsIgnoreCase("all")) all = true;
		else throw new FunctionException(pc, "structFindValue", 3, "scope", "invalid scope definition [" + scope + "], valid scopes are [one, all]");
		return _call(pc, struct, value, all);
	}

	private static Array _call(PageContext pc, Struct struct, String value, boolean all) throws PageException {
		Array array = new ArrayImpl();
		getValues(array, struct, value, all, "");
		return array;
	}

	/**
	 * @param coll
	 * @param value
	 * @param all
	 * @param buffer
	 * @return
	 * @throws PageException
	 */
	private static boolean getValues(Array array, Collection coll, String value, boolean all, String path) throws PageException {
		// Collection.Key[] keys=coll.keys();
		Iterator<Entry<Key, Object>> it = coll.entryIterator();
		Entry<Key, Object> e;
		boolean abort = false;
		Collection.Key key;

		while (it.hasNext()) {
			e = it.next();
			if (abort) break;
			key = e.getKey();
			Object o = e.getValue();

			// matching value (this function search first for base)
			if (key.getString().equalsIgnoreCase(value)) {
				Struct sct = new StructImpl();

				sct.setEL(KeyConstants._value, o);
				sct.setEL(KeyConstants._path, createKey(coll, path, key));
				sct.setEL(KeyConstants._owner, coll);
				array.append(sct);
				if (!all) abort = true;
			}

			// Collection
			if (!abort) {
				if (o instanceof Collection) {
					abort = getValues(array, ((Collection) o), value, all, createKey(coll, path, key));
				}
				else if (o instanceof List) {
					abort = getValues(array, ListAsArray.toArray((List<?>) o), value, all, createKey(coll, path, key));
				}
				else if (o instanceof Map) {
					abort = getValues(array, MapAsStruct.toStruct((Map<?, ?>) o), value, all, createKey(coll, path, key));
				}
			}
		}

		return abort;
	}

	static String createKey(Collection coll, String path, Collection.Key key) {
		StringBuilder p = new StringBuilder(path.toString());
		if (isArray(coll)) {
			p.append('[').append(key.getString()).append(']');
		}
		else {
			p.append('.').append(key.getString());
		}
		return p.toString();
	}

	static boolean isArray(Collection coll) {
		return coll instanceof Array && !(coll instanceof Argument);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]));
		throw new FunctionException(pc, "StructFindKey", 2, 3, args.length);
	}
}