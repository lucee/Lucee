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
 * Implements the CFML Function structfindvalue
 */
package lucee.runtime.functions.struct;

import java.util.Iterator;
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
import lucee.runtime.type.util.KeyConstants;

public final class StructFindValue extends BIF {

	private static final long serialVersionUID = 1499023912262918840L;

	public static Array call(PageContext pc, lucee.runtime.type.Struct struct, String value) throws PageException {
		return call(pc, struct, value, "one");
	}

	public static Array call(PageContext pc, Struct struct, String value, String scope) throws PageException {
		// Scope
		boolean all = false;
		if (scope.equalsIgnoreCase("one")) all = false;
		else if (scope.equalsIgnoreCase("all")) all = true;
		else throw new FunctionException(pc, "structFindValue", 3, "scope", "invalid scope definition [" + scope + "], valid scopes are [one, all]");

		Array array = new ArrayImpl();
		getValues(pc, array, struct, value, all, "");
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
	private static boolean getValues(PageContext pc, Array array, Collection coll, String value, boolean all, String path) throws PageException {
		// Key[] keys = coll.keys();
		boolean abort = false;
		Key key;
		Iterator<Entry<Key, Object>> it = coll.entryIterator();
		Entry<Key, Object> e;
		loop: while (it.hasNext()) {
			e = it.next();
			if (abort) break loop;
			key = e.getKey();
			Object o = e.getValue();

			// Collection (this function search first for sub)
			if (o instanceof Collection) {
				abort = getValues(pc, array, ((Collection) o), value, all, StructFindKey.createKey(coll, path, key));

			}
			// matching value
			if (!abort && !StructFindKey.isArray(coll)) {
				String target = Caster.toString(o, null);
				if ((target != null && target.equalsIgnoreCase(value)) /* || (o instanceof Array && checkSub(array,((Array)o),value,all,path,abort)) */) {
					Struct sct = new StructImpl();
					sct.setEL(KeyConstants._key, key.getString());
					sct.setEL(KeyConstants._path, StructFindKey.createKey(coll, path, key));
					sct.setEL(KeyConstants._owner, coll);
					array.append(sct);
					if (!all) abort = true;
				}
			}
		}

		return abort;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		if (args.length == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]));
		throw new FunctionException(pc, "StructFindValue", 2, 3, args.length);
	}
}