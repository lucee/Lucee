/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.ArraySupport;

/**
 * The class name stays in the serialization hierarchy of ArrayImpl: streams carry the elements in this class's
 * {@code list} slot. Every method that touches {@code list} is abstract, so a subclass that doesn't use it can't
 * inherit a body that does.
 */
public abstract class ListAsArray extends ArraySupport implements Array, List {

	// LDEV-6475 pinned to the value the JVM computed on 6.2/7.0/7.1, keeps arrays serialized by other versions readable
	private static final long serialVersionUID = -2946571425825745801L;

	protected List list;

	protected ListAsArray(List list) {
		this.list = list;
	}

	public static Array toArray(List list) {
		if (list instanceof ArrayAsList) return ((ArrayAsList) list).array;
		if (list instanceof Array) return (Array) list;
		return new ListBackedArray(list);
	}

	// the list written to the stream in the list slot, same layout as defaultWriteObject
	protected List streamList() {
		return list;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		ObjectOutputStream.PutField fields = out.putFields();
		fields.put("list", streamList());
		out.writeFields();
	}

	@Override
	public abstract Object append(Object o) throws PageException;

	@Override
	public abstract Object appendEL(Object o);

	/*---@Override
	public boolean containsKey(int index) {
		super.containsKey(index);
		return get(index-1,null)!=null;
	}*/

	@Override
	public final Object get(int key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	@Override
	public abstract Object get(PageContext pc, int key, Object defaultValue);

	@Override
	public final Object getE(int key) throws PageException {
		return getE(null, key);
	}

	@Override
	public abstract Object getE(PageContext pc, int key) throws PageException;

	@Override
	public int getDimension() {
		return 1;
	}

	@Override
	public abstract boolean insert(int key, Object value) throws PageException;

	@Override
	public abstract int[] intKeys();

	@Override
	public abstract Object prepend(Object o) throws PageException;

	@Override
	public abstract Object removeE(int key) throws PageException;

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
	public abstract Object pop() throws PageException;

	@Override
	public abstract Object pop(Object defaultValue);

	@Override
	public abstract Object shift() throws PageException;

	@Override
	public abstract Object shift(Object defaultValue);

	@Override
	public abstract void resize(int to) throws PageException;

	@Override
	public Object setE(int key, Object value) throws PageException {
		return setE(null, key, value);
	}

	@Override
	public abstract Object setE(PageContext pc, int key, Object value) throws PageException;

	@Override
	public abstract Object setEL(int key, Object value);

	/*---@Override
	public void sort(String sortType, String sortOrder) throws PageException {
		sortIt(ArrayUtil.toComparator(null, sortType, sortOrder, false));
	}*/

	@Override
	public abstract void sortIt(Comparator comp);

	@Override
	public abstract Object[] toArray();

	public abstract ArrayList toArrayList();

	@Override
	public abstract void clear();

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
	public abstract Collection duplicate(boolean deepCopy);

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
		return get(pc, (int) index, defaultValue);
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
	public abstract int size();

	@Override
	public abstract DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp);

	@Override
	public abstract Iterator iterator();

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
	public abstract boolean add(Object o);

	/*---@Override
	public void add(int index, Object element) {
		list.add(index, element);
	}
	
	@Override
	public boolean addAll(java.util.Collection c) {
		return list.addAll(c);
	}*/

	@Override
	public abstract boolean addAll(int index, java.util.Collection c);

	@Override
	public abstract boolean contains(Object o);

	@Override
	public abstract boolean containsAll(java.util.Collection c);

	/*---@Override
	public Object get(int index) {
		return list.get(index);
	}*/

	@Override
	public abstract int indexOf(Object o);

	@Override
	public abstract boolean isEmpty();

	@Override
	public abstract int lastIndexOf(Object o);

	@Override
	public abstract ListIterator listIterator();

	@Override
	public abstract ListIterator listIterator(int index);

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
	public abstract List subList(int fromIndex, int toIndex);

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
	public abstract Iterator<Object> valueIterator();

	@Override
	public abstract Iterator<Object> getIterator();

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