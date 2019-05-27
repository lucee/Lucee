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

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lucee.commons.lang.CFTypes;
import lucee.runtime.PageContext;
import lucee.runtime.converter.LazyConverter;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.ArrayPro;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryArrayIterator;

public abstract class ArraySupport extends AbstractList implements ArrayPro, List, Objects {

	public static final short TYPE_OBJECT = 0;
	public static final short TYPE_BOOLEAN = 1;
	public static final short TYPE_BYTE = 2;
	public static final short TYPE_SHORT = 3;
	public static final short TYPE_INT = 4;
	public static final short TYPE_LONG = 5;
	public static final short TYPE_FLOAT = 6;
	public static final short TYPE_DOUBLE = 7;
	public static final short TYPE_CHARACTER = 8;
	public static final short TYPE_STRING = 9;

	@Override
	public final void add(int index, Object element) {
		try {
			insert(index + 1, element);
		}
		catch (PageException e) {
			throw new IndexOutOfBoundsException("can't insert value to List at position " + index + ", " + "valid values are from 0 to " + (size() - 1) + ", size is " + size());
		}
	}

	@Override
	public final boolean addAll(java.util.Collection c) {
		Iterator it = c.iterator();
		while (it.hasNext()) {
			add(it.next());
		}
		return true;
	}

	@Override
	public final boolean remove(Object o) {
		int index = indexOf(o);
		if (index == -1) return false;

		try {
			removeE(index + 1);
		}
		catch (PageException e) {
			return false;
		}
		return true;
	}

	@Override
	public final boolean removeAll(java.util.Collection c) {
		Iterator it = c.iterator();
		boolean rtn = false;
		while (it.hasNext()) {
			if (remove(it.next())) rtn = true;
		}
		return rtn;
	}

	@Override
	public final boolean retainAll(java.util.Collection c) {
		boolean modified = false;
		Key[] keys = CollectionUtil.keys(this);
		Key k;
		for (int i = keys.length - 1; i >= 0; i--) {
			k = keys[i];
			if (!c.contains(get(k, null))) {
				removeEL(k);
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public final Object[] toArray(Object[] a) {
		if (a == null) return toArray();

		Class trgClass = a.getClass().getComponentType();
		short type = TYPE_OBJECT;
		if (trgClass == Boolean.class) type = TYPE_BOOLEAN;
		else if (trgClass == Byte.class) type = TYPE_BYTE;
		else if (trgClass == Short.class) type = TYPE_SHORT;
		else if (trgClass == Integer.class) type = TYPE_INT;
		else if (trgClass == Long.class) type = TYPE_LONG;
		else if (trgClass == Float.class) type = TYPE_FLOAT;
		else if (trgClass == Double.class) type = TYPE_DOUBLE;
		else if (trgClass == Character.class) type = TYPE_CHARACTER;
		else if (trgClass == String.class) type = TYPE_STRING;

		Iterator it = iterator();
		int i = 0;
		Object o;
		try {
			while (it.hasNext()) {
				o = it.next();
				switch (type) {
				case TYPE_BOOLEAN:
					o = Caster.toBoolean(o);
					break;
				case TYPE_BYTE:
					o = Caster.toByte(o);
					break;
				case TYPE_CHARACTER:
					o = Caster.toCharacter(o);
					break;
				case TYPE_DOUBLE:
					o = Caster.toDouble(o);
					break;
				case TYPE_FLOAT:
					o = Caster.toFloat(o);
					break;
				case TYPE_INT:
					o = Caster.toInteger(o);
					break;
				case TYPE_LONG:
					o = Caster.toLong(o);
					break;
				case TYPE_SHORT:
					o = Caster.toShort(o);
					break;
				case TYPE_STRING:
					o = Caster.toString(o);
					break;
				}
				a[i++] = o;
			}
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
		return a;
	}

	@Override
	public final Object get(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("invalid index definition [" + index + "], " + "index should be a number between [0 - " + (size() - 1) + "], size is " + size());
		if (index >= size())
			throw new IndexOutOfBoundsException("invalid index [" + index + "] definition, " + "index should be a number between [0 - " + (size() - 1) + "], size is " + size());
		return get(index + 1, null);
	}

	@Override
	public final Object remove(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("invalid index definition [" + index + "], " + "index should be a number between [0 - " + (size() - 1) + "], size is " + size());
		if (index >= size())
			throw new IndexOutOfBoundsException("invalid index [" + index + "] definition, " + "index should be a number between [0 - " + (size() - 1) + "], size is " + size());

		return removeEL(index + 1);
	}

	@Override
	public Object remove(Collection.Key key, Object defaultValue) {
		try {
			return remove(key);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public final Object set(int index, Object element) {
		Object o = get(index);
		setEL(index + 1, element);
		return o;
	}

	@Override
	public boolean containsKey(String key) {
		return get(KeyImpl.init(key), null) != null;
	}

	@Override
	public boolean containsKey(Collection.Key key) {
		return get(key, null) != null;
	}

	@Override
	public boolean containsKey(int key) {
		return get(key, null) != null;
	}

	@Override
	public String toString() {
		return LazyConverter.serialize(this);
	}

	@Override
	public Object clone() {
		return duplicate(true);
	}

	@Override
	public String castToString() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type Array to String", "Use Built-In-Function \"serialize(Array):String\" to create a String from Array");
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type Array to a boolean value");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type Array to a number value");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type Array to a Date");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Array with a boolean value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Array with a DateTime Object");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Array with a numeric value");
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Array with a String");
	}

	@Override
	public List toList() {
		return this;
	}

	@Override
	public Iterator<Object> valueIterator() {
		return iterator();
	}

	/*
	 * @Override public Object get(PageContext pc, Key key, Object defaultValue) { return get(key,
	 * defaultValue); }
	 * 
	 * @Override public Object get(PageContext pc, Key key) throws PageException { return get(key); }
	 */

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		return setEL(propertyName, value);
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] args) throws PageException {
		return MemberUtil.call(pc, this, methodName, args, new short[] { CFTypes.TYPE_ARRAY }, new String[] { "array" });
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		return MemberUtil.callWithNamedValues(pc, this, methodName, args, CFTypes.TYPE_ARRAY, "array");
	}

	@Override
	public java.util.Iterator<Object> getIterator() {
		return valueIterator();
	}

	@Override
	public void sort(String sortType, String sortOrder) throws PageException {
		if (getDimension() > 1) throw new ExpressionException("only 1 dimensional arrays can be sorted");
		sortIt(ArrayUtil.toComparator(null, sortType, sortOrder, false));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Collection)) return false;
		return CollectionUtil.equals(this, (Collection) obj);
	}

	/*
	 * @Override public int hashCode() { return CollectionUtil.hashCode(this); }
	 */

	@Override
	public Iterator<Entry<Integer, Object>> entryArrayIterator() {
		return new EntryArrayIterator(this, intKeys());
	}
}