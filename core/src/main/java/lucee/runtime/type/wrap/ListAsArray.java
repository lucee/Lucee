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
package lucee.runtime.type.wrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.ArraySupport;
import lucee.runtime.type.util.ListIteratorImpl;

/**
 * 
 */
public class ListAsArray extends ArraySupport implements Array, List {

	protected List list;

	protected ListAsArray(List list) {
		this.list = list;
	}

	public static Array toArray(List list) {
		if (list instanceof ArrayAsList) return ((ArrayAsList) list).array;
		if (list instanceof Array) return (Array) list;
		return new ListAsArray(list);
	}

	@Override
	public Object append(Object o) throws PageException {
		list.add(o);
		return o;
	}

	@Override
	public Object appendEL(Object o) {
		list.add(o);
		return o;
	}

	/*---@Override
	public boolean containsKey(int index) {
		super.containsKey(index);
		return get(index-1,null)!=null;
	}*/

	@Override
	public final Object get(int key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	public final Object get(PageContext pc, int key, Object defaultValue) {
		if (key <= 0) return defaultValue;
		if (key > list.size()) return defaultValue;

		try {
			Object rtn = list.get(key - 1);
			if (rtn == null) {
				if (NullSupportHelper.full(pc)) {
					return null;
				}
				return defaultValue;
			}
			return rtn;
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public final Object getE(int key) throws PageException {
		return getE(null, key);
	}

	public final Object getE(PageContext pc, int key) throws PageException {
		if (key <= 0) {
			Integer idx = list.size() + key <= 0 ? 0 : list.size() + key;
			if (idx == 0 || key == 0) {
				throw new ExpressionException("Array index [" + key + "] out of range, array size is [" + list.size() + "]");
			}
			Object rtn = list.get(idx);
			return rtn;
		}
		if (key > list.size()) throw new ExpressionException("Array index [" + key + "] out of range, array size is [" + list.size() + "]");
		Object rtn = list.get(key - 1);
		if (rtn == null) {
			if (NullSupportHelper.full(pc)) {
				return null;
			}
			throw new ExpressionException("Element at position [" + key + "] does not exist in list");
		}
		return rtn;
	}

	@Override
	public int getDimension() {
		return 1;
	}

	@Override
	public boolean insert(int key, Object value) throws PageException {
		try {
			list.add(key - 1, value);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new ExpressionException("can't insert value to array at position " + key + ", array goes from 1 to " + size());
		}
		return true;
	}

	@Override
	public int[] intKeys() {
		ListIterator lit = list.listIterator();
		ArrayList keys = new ArrayList();
		int index = 0;
		Object v;
		while (lit.hasNext()) {
			index = lit.nextIndex() + 1;
			v = lit.next();
			if (v != null) keys.add(Integer.valueOf(index));
		}
		int[] intKeys = new int[keys.size()];
		Iterator it = keys.iterator();
		index = 0;
		while (it.hasNext()) {
			intKeys[index++] = ((Integer) it.next()).intValue();
		}

		return intKeys;
	}

	@Override
	public Object prepend(Object o) throws PageException {
		list.add(0, o);
		return o;
	}

	@Override
	public Object removeE(int key) throws PageException {
		try {
			return list.remove(key - 1);
		}
		catch (Exception e) {
			ExpressionException ee = new ExpressionException("can not remove Element at position [" + key + "]", e.getMessage());
			ee.setStackTrace(e.getStackTrace());
			throw ee;
		}
	}

	@Override
	public Object removeEL(int key) {
		try {
			return removeE(key);
		}
		catch (PageException e) {
			return null;
		}
	}

	public Object remove(int key, Object defaultValue) {
		try {
			return removeE(key);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public Object pop() throws PageException {
		if (size() == 0) throw new ExpressionException("can not pop Element from array, array is empty");
		try {
			return list.remove(size() - 1);
		}
		catch (Exception e) {
			ExpressionException ee = new ExpressionException("can not pop Element from array", e.getMessage());
			ee.setStackTrace(e.getStackTrace());
			throw ee;
		}
	}

	@Override
	public Object pop(Object defaultValue) {
		if (size() == 0) return defaultValue;
		try {
			return list.remove(size() - 1);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public Object shift() throws PageException {
		if (size() == 0) throw new ExpressionException("can not pop Element from array, array is empty");
		try {
			return list.remove(0);
		}
		catch (Exception e) {
			ExpressionException ee = new ExpressionException("can not pop Element from array", e.getMessage());
			ee.setStackTrace(e.getStackTrace());
			throw ee;
		}
	}

	@Override
	public Object shift(Object defaultValue) {
		if (size() == 0) return defaultValue;
		try {
			return list.remove(0);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public void resize(int to) throws PageException {
		while (size() < to)
			list.add(null);
	}

	@Override
	public Object setE(int key, Object value) throws PageException {
		if (key <= size()) {
			try {
				list.set(key - 1, value);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw new ExpressionException("can not set Element at position [" + key + "]", t.getMessage());
			}

		}
		else {
			while (size() < key - 1)
				list.add(null);
			list.add(value);
		}
		return value;
	}

	@Override
	public Object setEL(int key, Object value) {
		if (key <= size()) {
			try {
				list.set(key - 1, value);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				return value;
			}

		}
		else {
			while (size() < key - 1)
				list.add(null);
			list.add(value);
		}
		return value;
	}

	/*---@Override
	public void sort(String sortType, String sortOrder) throws PageException {
		sortIt(ArrayUtil.toComparator(null, sortType, sortOrder, false));
	}*/

	@Override
	public void sortIt(Comparator comp) {
		if (getDimension() > 1) throw new PageRuntimeException("only 1 dimensional arrays can be sorted");
		Collections.sort(list, comp);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	public ArrayList toArrayList() {
		return new ArrayList(list);
	}

	@Override
	public void clear() {
		list.clear();
	}

	/*---@Override
	public boolean containsKey(String key) {
		return get(key,null)!=null;
	}
	
	@Override
	public boolean containsKey(Key key) {
		super.containsKey(key)
		return get(key,null)!=null;
	}*/

	@Override
	public Collection duplicate(boolean deepCopy) {
		new ArrayImpl().duplicate(deepCopy);
		return new ListAsArray((List) Duplicator.duplicate(list, deepCopy));
	}

	@Override
	public final Object get(String key) throws PageException {
		return getE(Caster.toIntValue(key));
	}

	public final Object get(PageContext pc, String key) throws PageException {
		return getE(pc, Caster.toIntValue(key));
	}

	@Override
	public final Object get(Key key) throws PageException {
		return get(key.getString());
	}

	@Override
	public final Object get(PageContext pc, Key key) throws PageException {
		return get(pc, key.getString());
	}

	@Override
	public final Object get(String key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	public final Object get(PageContext pc, String key, Object defaultValue) {
		double index = Caster.toIntValue(key, Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return defaultValue;
		return get((int) index, defaultValue);
	}

	@Override
	public final Object get(Key key, Object defaultValue) {
		return get(key.getString(), defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Key key, Object defaultValue) {
		return get(pc, key.getString(), defaultValue);
	}

	@Override
	public Key[] keys() {
		int[] intKeys = intKeys();
		Collection.Key[] keys = new Collection.Key[intKeys.length];
		for (int i = 0; i < intKeys.length; i++) {
			keys[i] = KeyImpl.init(Caster.toString(intKeys[i]));
		}
		return keys;
	}

	@Override
	public Object remove(Key key) throws PageException {
		return removeE(Caster.toIntValue(key.getString()));
	}

	@Override
	public Object removeEL(Key key) {
		double index = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return null;
		return removeEL((int) index);
	}

	@Override
	public Object remove(Key key, Object defaultValue) {
		double index = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return defaultValue;
		return remove((int) index, defaultValue);
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		return setE(Caster.toIntValue(key), value);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return set(key.getString(), value);
	}

	@Override
	public Object setEL(String key, Object value) {
		double index = Caster.toIntValue(key, Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return value;
		return setEL((int) index, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return setEL(key.getString(), value);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return DumpUtil.toDumpData(list, pageContext, maxlevel, dp);
	}

	@Override
	public Iterator iterator() {
		return list.iterator();
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new KeyIterator(keys());
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new StringIterator(keys());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	/*---@Override
	public String castToString() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to String",
	  "Use Built-In-Function \"serialize(Array):String\" to create a String from Array");
	}
	
	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}
	
	
	@Override
	public boolean castToBooleanValue() throws PageException {
	throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to a boolean value");
	}
	
	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
	return defaultValue;
	}
	
	
	@Override
	public double castToDoubleValue() throws PageException {
	throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to a number value");
	}
	
	@Override
	public double castToDoubleValue(double defaultValue) {
	return defaultValue;
	}
	
	
	@Override
	public DateTime castToDateTime() throws PageException {
	throw new ExpressionException("Can't cast Complex Object Type "+Caster.toClassName(list)+" to a Date");
	}
	
	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
	return defaultValue;
	}
	
	@Override
	public int compareTo(boolean b) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a boolean value");
	}
	
	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a DateTime Object");
	}
	
	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a numeric value");
	}
	
	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type "+Caster.toClassName(list)+" with a String");
	}*/

	/*---@Override
	public String toString() {
		return LazyConverter.serialize(this);
	}*/

	/*---@Override
	public Object clone() {
		super.clone()
		return duplicate(true);
	}*/

	@Override
	public boolean add(Object o) {
		return list.add(o);
	}

	/*---@Override
	public void add(int index, Object element) {
		list.add(index, element);
	}
	
	@Override
	public boolean addAll(java.util.Collection c) {
		return list.addAll(c);
	}*/

	@Override
	public boolean addAll(int index, java.util.Collection c) {
		return list.addAll(index, c);
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public boolean containsAll(java.util.Collection c) {
		return list.containsAll(c);
	}

	/*---@Override
	public Object get(int index) {
		return list.get(index);
	}*/

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		return list.listIterator(index);
	}

	/*
	 * @Override public boolean remove(Object o) { return list.remove(o); }
	 * 
	 * @Override public Object remove(int index) { return list.remove(index); }
	 * 
	 * @Override public boolean removeAll(java.util.Collection c) { return list.removeAll(c); }
	 * 
	 * @Override public boolean retainAll(java.util.Collection c) { return list.retainAll(c); }
	 * 
	 * @Override public Object set(int index, Object element) { return list.set(index, element); }
	 */

	@Override
	public List subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	/*---@Override not sure to remove it
	public Object[] toArray(Object[] a) {
		super.toArray(a)
		return list.toArray(a);
	}*/

	@Override
	public List toList() {
		return this;
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new ListIteratorImpl(list, 0);
		// return list.iterator();
	}

	@Override
	public Iterator<Object> getIterator() {
		return new ListIteratorImpl(list, 0);
		// return list.iterator();
	}

	/*---@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return get(key, defaultValue);
	}
	
	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		return get(key);
	}
	
	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}
	
	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		return setEL(propertyName, value);
	}*/

	/*
	 * @Override public Object call(PageContext pc, Key methodName, Object[] args) throws PageException
	 * { return MemberUtil.call(pc, this, methodName, args, CFTypes.TYPE_ARRAY, "array"); }
	 * 
	 * @Override public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws
	 * PageException { return MemberUtil.callWithNamedValues(pc,this,methodName,args,
	 * CFTypes.TYPE_ARRAY, "array"); }
	 */

	/*---@Override
	public java.util.Iterator<Object> getIterator() {
		return valueIterator();
	}*/
}