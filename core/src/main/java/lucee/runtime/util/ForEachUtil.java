/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.util;

import java.util.Enumeration;
import java.util.Iterator;

import lucee.commons.lang.ClassUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.java.JavaObject;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection;
import lucee.runtime.type.ForEachIteratorable;
import lucee.runtime.type.Iteratorable;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Resetable;
import lucee.runtime.type.it.EnumAsIt;
import lucee.runtime.type.util.ListUtil;

public class ForEachUtil {

	public static Iterator loopCollection(Object o) throws PageException {
		// only components are handled with ForEachIteratorable, because of he magic functions
		if (Decision.isComponent(o)) return Caster.toComponent(o).getIterator();

		Iterator it = _toIterator(o);
		if (it != null) return it;

		if (o instanceof ObjectWrap) return loopCollection(((ObjectWrap) o).getEmbededObject());
		return loopCollection(Caster.toCollection(o));
	}

	public static Iterator forEach(Object o) throws PageException {
		if (o instanceof ForEachIteratorable) return ((ForEachIteratorable) o).getIterator();

		// every are is handled with ForEachIteratorable
		if (Decision.isArray(o)) return Caster.toArray(o).getIterator();

		Iterator it = _toIterator(o);
		if (it != null) return it;

		if (Decision.isWrapped(o)) return forEach(Caster.unwrap(o));

		return forEach(Caster.toCollection(o));
	}

	private static Iterator _toIterator(Object o) {
		if (o instanceof Iteratorable) {
			return ((Iteratorable) o).keysAsStringIterator();
		}
		if (o instanceof Iterator) {
			return (Iterator) o;
		}
		if (o instanceof Enumeration) {
			return new EnumAsIt((Enumeration) o);
		}
		if (o instanceof JavaObject) {
			Collection coll = Caster.toCollection(((JavaObject) o).getEmbededObject(null), null);
			if (coll != null) return coll.getIterator();

			String[] names = ClassUtil.getFieldNames(((JavaObject) o).getClazz());
			return new ArrayIterator(names);
		}
		else if (o instanceof CharSequence) {
			return ListUtil.listToArray(o.toString(), ',').getIterator();
		}
		else if (Decision.isSimpleValueLimited(o)) {
			String str = Caster.toString(o, null);
			if (str == null) return null; // should never happen
			return ListUtil.listToArray(str, ',').getIterator();
		}

		return null;
	}

	public static void reset(Iterator it) throws PageException {

		if (it instanceof Resetable) {
			((Resetable) it).reset();
		}
	}

}