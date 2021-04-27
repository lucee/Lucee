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
package lucee.runtime.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.ArraySupport;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListIteratorImpl;

/**
 * CFML array object
 */
public class ArrayClassic extends ArraySupport {

	private static final long serialVersionUID = -6187994169003839005L;

	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	private Object[] arr;
	private int dimension = 1;
	private final int cap = 32;
	private int size = 0;
	private int offset = 0;
	private int offCount = 0;

	/**
	 * constructor with definiton of the dimension
	 * 
	 * @param dimension dimension goes from one to 3
	 * @throws ExpressionException
	 */
	public ArrayClassic(int dimension) throws ExpressionException {
		if (dimension > 3 || dimension < 1) throw new ExpressionException("Array Dimension must be between 1 and 3");
		this.dimension = dimension;
		arr = new Object[offset + cap];
	}

	/**
	 * constructor with default dimesnion (1)
	 */
	public ArrayClassic() {
		arr = new Object[offset + cap];
	}

	/**
	 * constructor with to data to fill
	 * 
	 * @param objects Objects array data to fill
	 */
	public ArrayClassic(Object[] objects) {
		size = objects.length;

		arr = new Object[Math.max(size, cap)];

		if (size > 0) arr = ArrayUtil.mergeNativeArrays(arr, objects, 0, false);

		offset = 0;
	}

	/**
	 * return dimension of the array
	 * 
	 * @return dimension of the array
	 */
	@Override
	public int getDimension() {
		return dimension;
	}

	@Override
	public Object get(String key) throws ExpressionException {
		return getE(Caster.toIntValue(key));
	}

	@Override
	public final Object get(Collection.Key key) throws ExpressionException {
		return getE(Caster.toIntValue(key.getString()));
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key) throws ExpressionException {
		return getE(pc, Caster.toIntValue(key.getString()));
	}

	@Override
	public Object get(String key, Object defaultValue) {
		double index = Caster.toIntValue(key, Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return defaultValue;
		return get((int) index, defaultValue);
	}

	@Override
	public final Object get(Collection.Key key, Object defaultValue) {
		double index = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return defaultValue;
		return get((int) index, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		double index = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return defaultValue;
		return get(pc, (int) index, defaultValue);
	}

	@Override
	public final synchronized Object get(int key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	public final synchronized Object get(PageContext pc, int key, Object defaultValue) {
		if (key > size || key < 1) {
			if (dimension > 1) {
				ArrayClassic ai = new ArrayClassic();
				ai.dimension = dimension - 1;
				return setEL(key, ai);
			}
			return defaultValue;
		}
		Object o = arr[(offset + key) - 1];

		if (o == null) {
			if (dimension > 1) {
				ArrayClassic ai = new ArrayClassic();
				ai.dimension = dimension - 1;
				return setEL(key, ai);
			}
			if (!NullSupportHelper.full(pc)) return defaultValue;
		}
		return o;
	}

	@Override
	public synchronized Object getE(int key) throws ExpressionException {
		return getE(null, key);
	}

	public synchronized Object getE(PageContext pc, int key) throws ExpressionException {
		if (key < 1) {
			throw invalidPosition(key);
		}
		else if (key > size) {
			if (dimension > 1) return setE(key, new ArrayClassic(dimension - 1));
			throw invalidPosition(key);
		}

		Object o = arr[(offset + key) - 1];

		if (NullSupportHelper.full(pc)) {
			if (o == null && dimension > 1) return setE(key, new ArrayClassic(dimension - 1));
			return o;
		}

		if (o == null) {
			if (dimension > 1) return setE(key, new ArrayClassic(dimension - 1));
			throw invalidPosition(key);
		}
		return o;
	}

	/**
	 * Exception method if key doesn't exist at given position
	 * 
	 * @param pos
	 * @return exception
	 */
	private ExpressionException invalidPosition(int pos) {
		return new ExpressionException("Element at position [" + pos + "] doesn't exist in array");
	}

	@Override
	public Object setEL(String key, Object value) {
		try {
			return setEL(Caster.toIntValue(key), value);
		}
		catch (ExpressionException e) {
			return null;
		}
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		try {
			return setEL(Caster.toIntValue(key.getString()), value);
		}
		catch (ExpressionException e) {
			return null;
		}
	}

	@Override
	public Object set(String key, Object value) throws ExpressionException {
		return setE(Caster.toIntValue(key), value);
	}

	@Override
	public Object set(Collection.Key key, Object value) throws ExpressionException {
		return setE(Caster.toIntValue(key.getString()), value);
	}

	@Override
	public synchronized Object setEL(int key, Object value) {
		if (offset + key > arr.length) enlargeCapacity(key);
		if (key > size) size = key;
		arr[(offset + key) - 1] = checkValueEL(value);
		return value;
	}

	/**
	 * set value at defined position
	 * 
	 * @param key
	 * @param value
	 * @return defined value
	 * @throws ExpressionException
	 */
	@Override
	public synchronized Object setE(int key, Object value) throws ExpressionException {
		if (key < 1) throw new ExpressionException("Invalid index [" + key + "] for array. Index must be a positive integer (1, 2, 3, ...)");
		if (offset + key > arr.length) enlargeCapacity(key);
		if (key > size) size = key;
		arr[(offset + key) - 1] = checkValue(value);
		return value;
	}

	public synchronized int ensureCapacity(int cap) {
		if (cap > arr.length) enlargeCapacity(cap);
		return arr.length;
	}

	/**
	 * !!! all methods that use this method must be sync enlarge the inner array to given size
	 * 
	 * @param key min size of the array
	 */
	private void enlargeCapacity(int key) {
		int diff = offCount - offset;
		int minCapacity = Math.max(arr.length, key + offset + diff + 1);

		if (minCapacity > arr.length) {
			int oldCapacity = arr.length;
			int newCapacity = oldCapacity + (oldCapacity >> 1);
			if (newCapacity - minCapacity < 0) newCapacity = minCapacity;
			if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity);
			// minCapacity is usually close to size, so this is a win:
			arr = Arrays.copyOf(arr, newCapacity);
		}
	}

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) // overflow
			throw new OutOfMemoryError();
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

	/**
	 * !!! all methods that use this method must be sync enlarge the offset if 0
	 */
	private void enlargeOffset() {
		if (offset == 0) {
			offCount = offCount == 0 ? 1 : offCount * 2;
			offset = offCount;
			Object[] narr = new Object[arr.length + offset];
			for (int i = 0; i < size; i++) {
				narr[offset + i] = arr[i];
			}
			arr = narr;
		}
	}

	/**
	 * !!! all methods that use this method must be sync check if value is valid to insert to array (to
	 * a multidimesnional array only array with one smaller dimension can be inserted)
	 * 
	 * @param value value to check
	 * @return checked value
	 * @throws ExpressionException
	 */
	private Object checkValue(Object value) throws ExpressionException {
		// is a 1 > Array
		if (dimension > 1) {
			if (value instanceof Array) {
				if (((Array) value).getDimension() != dimension - 1) throw new ExpressionException("You can only Append an Array with " + (dimension - 1) + " Dimension",
						"array has wrong dimension, now is " + (((Array) value).getDimension()) + " but it must be " + (dimension - 1));
			}
			else throw new ExpressionException("You can only Append an Array with " + (dimension - 1) + " Dimension", "now is an object of type " + Caster.toClassName(value));
		}
		return value;
	}

	/**
	 * !!! all methods that use this method must be sync check if value is valid to insert to array (to
	 * a multidimesnional array only array with one smaller dimension can be inserted), if value is
	 * invalid return null;
	 * 
	 * @param value value to check
	 * @return checked value
	 */
	private Object checkValueEL(Object value) {
		// is a 1 > Array
		if (dimension > 1) {
			if (value instanceof Array) {
				if (((Array) value).getDimension() != dimension - 1) return null;
			}
			else return null;
		}
		return value;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public synchronized Collection.Key[] keys() {

		ArrayList<Collection.Key> lst = new ArrayList<Collection.Key>();
		int count = 0;
		for (int i = offset; i < offset + size; i++) {
			Object o = arr[i];
			count++;
			if (o != null) lst.add(KeyImpl.getInstance(count + ""));
		}
		return lst.toArray(new Collection.Key[lst.size()]);
	}

	@Override
	public synchronized int[] intKeys() {
		ArrayList<Integer> lst = new ArrayList<Integer>();
		int count = 0;
		for (int i = offset; i < offset + size; i++) {
			Object o = arr[i];
			count++;
			if (o != null) lst.add(Integer.valueOf(count));
		}

		int[] ints = new int[lst.size()];

		for (int i = 0; i < ints.length; i++) {
			ints[i] = lst.get(i).intValue();
		}
		return ints;
	}

	@Override
	public Object remove(Collection.Key key) throws ExpressionException {
		return removeE(Caster.toIntValue(key.getString()));
	}

	@Override
	public Object removeEL(Collection.Key key) {
		return removeEL(Caster.toIntValue(key.getString(), -1));
	}

	@Override
	public Object remove(Collection.Key key, Object defaultValue) {
		try {
			return removeE(Caster.toIntValue(key.getString(), -1));
		}
		catch (ExpressionException e) {
			return defaultValue;
		}
	}

	@Override
	public synchronized Object removeE(int key) throws ExpressionException {
		if (key > size || key < 1) throw invalidPosition(key);
		Object obj = get(key, null);
		for (int i = (offset + key) - 1; i < (offset + size) - 1; i++) {
			arr[i] = arr[i + 1];
		}
		size--;
		return obj;
	}

	@Override
	public synchronized Object pop() throws ExpressionException {
		int key = size();
		if (key == 0) throw new ExpressionException("cannot pop an element from array, the array is empty");
		Object obj = get(key, null);
		for (int i = (offset + key) - 1; i < (offset + size) - 1; i++) {
			arr[i] = arr[i + 1];
		}
		size--;
		return obj;
	}

	@Override
	public synchronized Object pop(Object defaultValue) {
		int key = size();
		if (key == 0) return defaultValue;
		Object obj = get(key, null);
		for (int i = (offset + key) - 1; i < (offset + size) - 1; i++) {
			arr[i] = arr[i + 1];
		}
		size--;
		return obj;
	}

	@Override
	public synchronized Object shift() throws ExpressionException {
		if (size() == 0) throw new ExpressionException("cannot pop an element from array, the array is empty");
		int key = 1;
		Object obj = get(key, null);
		for (int i = (offset + key) - 1; i < (offset + size) - 1; i++) {
			arr[i] = arr[i + 1];
		}
		size--;
		return obj;
	}

	@Override
	public synchronized Object shift(Object defaultValue) {
		if (size() == 0) return defaultValue;
		int key = 1;
		Object obj = get(key, null);
		for (int i = (offset + key) - 1; i < (offset + size) - 1; i++) {
			arr[i] = arr[i + 1];
		}
		size--;
		return obj;
	}

	@Override
	public synchronized Object removeEL(int key) {
		if (key > size || key < 1) return null;
		Object obj = get(key, null);

		for (int i = (offset + key) - 1; i < (offset + size) - 1; i++) {
			arr[i] = arr[i + 1];
		}
		size--;
		return obj;
	}

	@Override
	public synchronized void clear() {
		if (size() > 0) {
			arr = new Object[cap];
			size = 0;
			offCount = 1;
			offset = 0;
		}
	}

	@Override
	public synchronized boolean insert(int key, Object value) throws ExpressionException {
		if (key < 1 || key > size + 1) {
			throw new ExpressionException("can't insert value to array at position " + key + ", array goes from 1 to " + size());
		}
		// Left
		if ((size / 2) >= key) {
			enlargeOffset();
			for (int i = offset; i < (offset + key) - 1; i++) {
				arr[i - 1] = arr[i];
			}
			offset--;
			arr[(offset + key) - 1] = checkValue(value);
			size++;
		}
		// Right
		else {
			if ((offset + key) > arr.length || size + offset >= arr.length) enlargeCapacity(arr.length + 2);
			for (int i = size + offset; i >= key + offset; i--) {
				arr[i] = arr[i - 1];
			}
			arr[(offset + key) - 1] = checkValue(value);
			size++;

		}
		return true;
	}

	@Override
	public synchronized Object append(Object o) throws ExpressionException {
		if (offset + size + 1 > arr.length) enlargeCapacity(size + 1);
		arr[offset + size] = checkValue(o);
		size++;
		return o;
	}

	/**
	 * append a new value to the end of the array
	 * 
	 * @param o value to insert
	 * @return inserted value
	 */
	@Override
	public synchronized Object appendEL(Object o) {

		if (offset + size + 1 > arr.length) enlargeCapacity(size + 1);
		arr[offset + size] = o;
		size++;
		return o;
	}

	/**
	 * append a new value to the end of the array
	 * 
	 * @param str value to insert
	 * @return inserted value
	 */
	public synchronized String _append(String str) {
		if (offset + size + 1 > arr.length) enlargeCapacity(size + 1);
		arr[offset + size] = str;
		size++;
		return str;
	}

	/**
	 * add a new value to the begin of the array
	 * 
	 * @param o value to insert
	 * @return inserted value
	 * @throws ExpressionException
	 */
	@Override
	public Object prepend(Object o) throws ExpressionException {
		insert(1, o);
		return o;
	}

	/**
	 * resize array to defined size
	 * 
	 * @param to new minimum size of the array
	 */
	@Override
	public synchronized void resize(int to) {
		if (to > size) {
			enlargeCapacity(to);
			size = to;
		}
	}

	@Override
	public synchronized void sortIt(Comparator comp) {
		if (getDimension() > 1) throw new PageRuntimeException("only 1 dimensional arrays can be sorted");
		Arrays.sort(arr, offset, offset + size, comp);
	}

	/**
	 * @return return arra as native (Java) Object Array
	 */
	@Override
	public synchronized Object[] toArray() {
		Object[] rtn = new Object[size];
		int count = 0;
		for (int i = offset; i < offset + size; i++) {
			rtn[count++] = arr[i];
		}

		return rtn;
	}

	/**
	 * @return return array as ArrayList
	 */
	/*
	 * public synchronized ArrayList toArrayList() { ArrayList al=new ArrayList(); for(int
	 * i=offset;i<offset+size;i++) { al.add(arr[i]); } return al; }
	 */

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = new DumpTable("array", "#99cc33", "#ccff33", "#000000");
		table.setTitle("Array");

		int top = dp.getMaxlevel();

		if (size() > top) table.setComment("Rows: " + size() + " (showing top " + top + ")");
		else if (size() > 10 && dp.getMetainfo()) table.setComment("Rows: " + size());

		int length = size();

		for (int i = 1; i <= length; i++) {
			Object o = null;
			try {
				o = getE(i);
			}
			catch (Exception e) {}

			table.appendRow(1, new SimpleDumpData(i), DumpUtil.toDumpData(o, pageContext, maxlevel, dp));

			if (i == top) break;
		}

		return table;
	}

	/**
	 * return code print of the array as plain text
	 * 
	 * @return content as string
	 */
	public synchronized String toPlain() {

		StringBuffer sb = new StringBuffer();
		int length = size();
		for (int i = 1; i <= length; i++) {
			sb.append(i);
			sb.append(": ");
			sb.append(get(i - 1, null));
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public synchronized Collection duplicate(boolean deepCopy) {
		return duplicate(new ArrayClassic(), deepCopy);
	}

	protected Collection duplicate(ArrayClassic arr, boolean deepCopy) {
		arr.dimension = dimension;
		Iterator<Entry<Key, Object>> it = entryIterator();
		boolean inside = deepCopy ? ThreadLocalDuplication.set(this, arr) : true;
		Entry<Key, Object> e;
		try {
			while (it.hasNext()) {
				e = it.next();
				if (deepCopy) arr.set(e.getKey(), Duplicator.duplicate(e.getValue(), deepCopy));
				else arr.set(e.getKey(), e.getValue());
			}
		}
		catch (ExpressionException ee) {}
		finally {
			if (!inside) ThreadLocalDuplication.reset();
		}

		return arr;
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

	@Override
	public Iterator iterator() {
		return new ListIteratorImpl(this, 0);
	}
}
