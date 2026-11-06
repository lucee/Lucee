/**
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.ArraySupport;
import lucee.runtime.type.util.ListIteratorImpl;

/**
 * Read-through view over a native Java array. Mutations copy into an {@link ArrayImpl} first so
 * CFML still sees an independent array (same observable behaviour as the previous
 * {@code new ArrayImpl(...)} cast path).
 */
public abstract class NativeArrayAsArray extends ArraySupport implements Array, List {

	private static final long serialVersionUID = 6806202511036806006L;

	private ArrayImpl mutable;

	protected abstract int nativeSize();

	protected abstract Object nativeGet(int zeroBasedIndex);

	protected abstract Object[] nativeToArray();

	protected abstract void releaseNative();

	protected final boolean isMutable() {
		return mutable != null;
	}

	protected final ArrayImpl ensureMutable() {
		if (mutable == null) {
			mutable = new ArrayImpl(nativeToArray());
			releaseNative();
		}
		return mutable;
	}

	/**
	 * Used by serialization so a view never leaves a JVM with a live native-array reference.
	 */
	protected Object writeReplace() {
		if (mutable != null) return mutable;
		return new ArrayImpl(nativeToArray());
	}

	@Override
	public final int getDimension() {
		return 1;
	}

	@Override
	public final int size() {
		if (mutable != null) return mutable.size();
		return nativeSize();
	}

	@Override
	public final Object get(int key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	public final Object get(PageContext pc, int key, Object defaultValue) {
		if (mutable != null) return mutable.get(pc, key, defaultValue);
		if (key <= 0) return defaultValue;
		int len = nativeSize();
		if (key > len) return defaultValue;
		Object rtn = nativeGet(key - 1);
		if (rtn == null) {
			if (NullSupportHelper.full(pc)) return null;
			return defaultValue;
		}
		return rtn;
	}

	@Override
	public final Object getE(int key) throws PageException {
		return getE(null, key);
	}

	public final Object getE(PageContext pc, int key) throws PageException {
		if (mutable != null) return mutable.getE(pc, key);
		int len = nativeSize();
		if (key <= 0) {
			int idx = len + key < 0 ? -1 : len + key;
			if (idx < 0 || key == 0) {
				throw new ExpressionException("Array index [" + key + "] out of range, array size is [" + len + "]");
			}
			Object rtn = nativeGet(idx);
			if (rtn == null && !NullSupportHelper.full(pc)) {
				throw new ExpressionException("Element at position [" + key + "] does not exist in array");
			}
			return rtn;
		}
		if (key > len) throw new ExpressionException("Array index [" + key + "] out of range, array size is [" + len + "]");
		Object rtn = nativeGet(key - 1);
		if (rtn == null && !NullSupportHelper.full(pc)) {
			throw new ExpressionException("Element at position [" + key + "] does not exist in array");
		}
		return rtn;
	}

	@Override
	public final Object[] toArray() {
		if (mutable != null) return mutable.toArray();
		return nativeToArray();
	}

	@Override
	public Object[] toNativeArray() {
		ArrayImpl m = mutable;
		if (m != null) return m.toNativeArray();
		return nativeToArray();
	}

	@Override
	public final int[] intKeys() {
		if (mutable != null) return mutable.intKeys();
		int len = nativeSize();
		ArrayList keys = new ArrayList(len);
		for (int i = 0; i < len; i++) {
			if (nativeGet(i) != null) keys.add(Integer.valueOf(i + 1));
		}
		int[] intKeys = new int[keys.size()];
		for (int i = 0; i < intKeys.length; i++) {
			intKeys[i] = ((Integer) keys.get(i)).intValue();
		}
		return intKeys;
	}

	@Override
	public final Collection duplicate(boolean deepCopy) {
		// Independent ArrayImpl copy — never another view of the same native array.
		ArrayImpl copy = new ArrayImpl(size());
		int len = size();
		for (int i = 1; i <= len; i++) {
			Object v = get(i, null);
			if (deepCopy) copy.setEL(i, Duplicator.duplicate(v, true));
			else copy.setEL(i, v);
		}
		if (copy.size() < len) {
			try {
				copy.resize(len);
			}
			catch (PageException pe) {
				throw new PageRuntimeException(pe);
			}
		}
		return copy;
	}

	@Override
	public final Object append(Object o) throws PageException {
		return ensureMutable().append(o);
	}

	@Override
	public final Object appendEL(Object o) {
		return ensureMutable().appendEL(o);
	}

	@Override
	public final Object prepend(Object o) throws PageException {
		return ensureMutable().prepend(o);
	}

	@Override
	public final boolean insert(int key, Object value) throws PageException {
		return ensureMutable().insert(key, value);
	}

	@Override
	public final Object setE(int key, Object value) throws PageException {
		return ensureMutable().setE(key, value);
	}

	@Override
	public final Object setEL(int key, Object value) {
		return ensureMutable().setEL(key, value);
	}

	@Override
	public final Object removeE(int key) throws PageException {
		return ensureMutable().removeE(key);
	}

	@Override
	public final Object removeEL(int key) {
		return ensureMutable().removeEL(key);
	}

	public final Object remove(int key, Object defaultValue) {
		return ensureMutable().remove(key, defaultValue);
	}

	@Override
	public final Object pop() throws PageException {
		return ensureMutable().pop();
	}

	@Override
	public final Object pop(Object defaultValue) {
		return ensureMutable().pop(defaultValue);
	}

	@Override
	public final Object shift() throws PageException {
		return ensureMutable().shift();
	}

	@Override
	public final Object shift(Object defaultValue) {
		return ensureMutable().shift(defaultValue);
	}

	@Override
	public final void resize(int to) throws PageException {
		ensureMutable().resize(to);
	}

	@Override
	public final void clear() {
		ensureMutable().clear();
	}

	@Override
	public final void sortIt(Comparator comp) {
		if (getDimension() > 1) throw new PageRuntimeException("only 1 dimensional arrays can be sorted");
		ensureMutable().sortIt(comp);
	}

	@Override
	public final boolean add(Object o) {
		return ensureMutable().add(o);
	}

	@Override
	public final boolean addAll(int index, java.util.Collection c) {
		return ensureMutable().addAll(index, c);
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
	public final Key[] keys() {
		int[] intKeys = intKeys();
		Collection.Key[] keys = new Collection.Key[intKeys.length];
		for (int i = 0; i < intKeys.length; i++) {
			keys[i] = KeyImpl.init(Caster.toString(intKeys[i]));
		}
		return keys;
	}

	@Override
	public final Object remove(Key key) throws PageException {
		return removeE(Caster.toIntValue(key.getString()));
	}

	@Override
	public final Object removeEL(Key key) {
		double index = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return null;
		return removeEL((int) index);
	}

	@Override
	public final Object remove(Key key, Object defaultValue) {
		double index = Caster.toIntValue(key.getString(), Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return defaultValue;
		return remove((int) index, defaultValue);
	}

	@Override
	public final Object set(String key, Object value) throws PageException {
		return setE(Caster.toIntValue(key), value);
	}

	@Override
	public final Object set(Key key, Object value) throws PageException {
		return set(key.getString(), value);
	}

	@Override
	public final Object setEL(String key, Object value) {
		double index = Caster.toIntValue(key, Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return value;
		return setEL((int) index, value);
	}

	@Override
	public final Object setEL(Key key, Object value) {
		return setEL(key.getString(), value);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		if (mutable != null) return mutable.toDumpData(pageContext, maxlevel, dp);
		DumpTable table = new DumpTable("array", "#52b788", "#b7e4c7", "#000000");
		table.setTitle("Array");
		int top = dp.getMaxlevel();
		int length = size();
		if (length > top) table.setComment("Rows: " + length + " (showing top " + top + ")");
		else if (length > 10 && dp.getMetainfo()) table.setComment("Rows: " + length);
		for (int i = 1; i <= length; i++) {
			table.appendRow(1, new SimpleDumpData(i), DumpUtil.toDumpData(get(i, null), pageContext, maxlevel, dp));
			if (i == top) break;
		}
		return table;
	}

	@Override
	public Iterator iterator() {
		return valueIterator();
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
	public Iterator<Object> valueIterator() {
		if (mutable != null) return mutable.valueIterator();
		return new ListIteratorImpl(this, 0);
	}

	@Override
	public Iterator<Object> getIterator() {
		return valueIterator();
	}

	@Override
	public boolean contains(Object o) {
		if (mutable != null) return mutable.contains(o);
		int len = nativeSize();
		for (int i = 0; i < len; i++) {
			Object v = nativeGet(i);
			if (o == null ? v == null : o.equals(v)) return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(java.util.Collection c) {
		if (mutable != null) return mutable.containsAll(c);
		Iterator it = c.iterator();
		while (it.hasNext()) {
			if (!contains(it.next())) return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public int indexOf(Object o) {
		if (mutable != null) return mutable.indexOf(o);
		int len = nativeSize();
		for (int i = 0; i < len; i++) {
			Object v = nativeGet(i);
			if (o == null ? v == null : o.equals(v)) return i;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		if (mutable != null) return mutable.lastIndexOf(o);
		for (int i = nativeSize() - 1; i >= 0; i--) {
			Object v = nativeGet(i);
			if (o == null ? v == null : o.equals(v)) return i;
		}
		return -1;
	}

	@Override
	public ListIterator listIterator() {
		if (mutable != null) return mutable.listIterator();
		return new ListIteratorImpl(this, 0);
	}

	@Override
	public ListIterator listIterator(int index) {
		if (mutable != null) return mutable.listIterator(index);
		return new ListIteratorImpl(this, index);
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return ensureMutable().subList(fromIndex, toIndex);
	}

	@Override
	public List toList() {
		return this;
	}
}
