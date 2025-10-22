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
package lucee.runtime.type.scope;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.CFTypes;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.ArrayPro;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Null;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.it.EntryArrayIterator;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.MemberUtil;
import lucee.runtime.type.wrap.ArrayAsList;

/**
 * implementation of the argument scope
 */
public final class ArgumentImpl extends ScopeSupport implements Argument, ArrayPro {

	private static final long serialVersionUID = 4346997451403177136L;

	private boolean bind;
	private Set functionArgumentNames;
	private static int argumentInitialCapacity;
	private static final boolean USE_ARRAY_OPTIMIZATION;

	// Lightweight storage for common case - simple Object[] array
	// Format: [key1, value1, key2, value2, ...]
	// Even indices = keys, odd indices = values
	private boolean useHashMap = false;
	private Object[] argArray;
	private int argCount = 0; // number of key-value pairs (not array length)

	static {
		argumentInitialCapacity = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.scope.arguments.capacity", "16"), 16);
		USE_ARRAY_OPTIMIZATION = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.scope.arguments.array", "true"), true);
	}
	// private boolean supportFunctionArguments;

	/**
	 * constructor of the class
	 */
	public ArgumentImpl() {
		// super("arguments", SCOPE_ARGUMENTS, StructImpl.TYPE_LINKED, 4);
		super("arguments", SCOPE_ARGUMENTS, StructImpl.TYPE_LINKED_NOT_SYNC, argumentInitialCapacity);
		// Start with space for argumentInitialCapacity key-value pairs
		if ( USE_ARRAY_OPTIMIZATION ) {
			argArray = new Object[argumentInitialCapacity * 2];
		}
		else {
			useHashMap = true;
		}
	}

	/**
	 * Ensures array has capacity for at least minCapacity key-value pairs
	 */
	private void ensureCapacity( int minCapacity ) {
		int currentCapacity = argArray.length / 2;
		if ( minCapacity > currentCapacity ) {
			int newCapacity = Math.max( minCapacity, currentCapacity * 2 );
			Object[] newArray = new Object[newCapacity * 2];
			System.arraycopy( argArray, 0, newArray, 0, argCount * 2 );
			argArray = newArray;
		}
	}

	/**
	 * Converts from lightweight array storage to HashMap storage
	 */
	private void convertToHashMap() {
		if ( useHashMap ) return;

		// Migrate data from array to parent HashMap
		for ( int i = 0; i < argCount; i++ ) {
			super.setEL( (Key) argArray[i * 2], argArray[i * 2 + 1] );
		}

		// Clear array and switch mode
		argArray = null;
		argCount = 0;
		useHashMap = true;
	}

	@Override
	public void release(PageContext pc) {
		functionArgumentNames = null;

		// Clear our array storage
		if ( !useHashMap ) {
			argCount = 0;
			// Null out array for GC
			if ( argArray != null ) {
				for ( int i = 0; i < argArray.length; i++ ) {
					argArray[i] = null;
				}
			}
		}
		// Reset to array mode for next use
		if ( USE_ARRAY_OPTIMIZATION ) {
			useHashMap = false;
			if ( argArray == null ) {
				argArray = new Object[argumentInitialCapacity * 2];
			}
		}
		// Let parent handle isInit and HashMap clearing
		super.release(ThreadLocalPageContext.get(pc));
	}

	@Override
	public void setBind(boolean bind) {
		if (bind) {
			convertToHashMap();
			makeSynchronized();
		}
		this.bind = bind;
	}

	@Override
	public boolean isBind() {
		return this.bind;
	}

	@Override
	public Object getFunctionArgument(String key, Object defaultValue) {
		return getFunctionArgument(KeyImpl.init(key), defaultValue);
	}

	@Override
	public Object getFunctionArgument(Collection.Key key, Object defaultValue) {
		if ( !useHashMap ) {
			return get( key, defaultValue );
		}
		return super.get(key, defaultValue);
	}

	@Override
	public boolean containsFunctionArgumentKey(Key key) {
		if ( !useHashMap ) {
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					return true;
				}
			}
			return false;
		}
		return super.containsKey(key);// functionArgumentNames!=null && functionArgumentNames.contains(key);
	}

	@Override
	public int size() {
		if ( !useHashMap ) {
			return argCount;
		}
		return super.size();
	}

	@Override
	public Collection.Key[] keys() {
		if ( !useHashMap ) {
			Key[] result = new Key[argCount];
			for ( int i = 0; i < argCount; i++ ) {
				result[i] = (Key) argArray[i * 2];
			}
			return result;
		}
		return super.keys();
	}

	@Override
	public Iterator<Key> keyIterator() {
		if ( !useHashMap ) {
			List<Key> keyList = new ArrayList<Key>( argCount );
			for ( int i = 0; i < argCount; i++ ) {
				keyList.add( (Key) argArray[i * 2] );
			}
			return keyList.iterator();
		}
		return super.keyIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		if ( !useHashMap ) {
			List<Object> valueList = new ArrayList<Object>( argCount );
			for ( int i = 0; i < argCount; i++ ) {
				valueList.add( argArray[i * 2 + 1] );
			}
			return valueList.iterator();
		}
		return super.valueIterator();
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		if ( !useHashMap ) {
			List<Entry<Key, Object>> entryList = new ArrayList<Entry<Key, Object>>( argCount );
			for ( int i = 0; i < argCount; i++ ) {
				final Key k = (Key) argArray[i * 2];
				final Object v = argArray[i * 2 + 1];
				entryList.add( new Entry<Key, Object>() {
					@Override
					public Key getKey() {
						return k;
					}

					@Override
					public Object getValue() {
						return v;
					}

					@Override
					public Object setValue( Object value ) {
						throw new UnsupportedOperationException();
					}
				} );
			}
			return entryList.iterator();
		}
		return super.entryIterator();
	}

	@Override
	public Object g(Collection.Key key, Object defaultValue) {
		if ( !useHashMap ) {
			// Linear search through array
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					return argArray[i * 2 + 1];
				}
			}
			return defaultValue;
		}
		return super.g( key, defaultValue );
	}

	@Override
	public Object g(Collection.Key key) throws PageException {
		if ( !useHashMap ) {
			// Linear search through array
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					return argArray[i * 2 + 1];
				}
			}
			throw new ExpressionException("The key [" + key.getString() + "] doesn't exist in the arguments scope.");
		}
		return super.g( key );
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		if ( !useHashMap ) {
			// Linear search through array (fast for small n)
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					return argArray[i * 2 + 1];
				}
			}

			// Try numeric access if key looks like a number
			if ( key.length() > 0 ) {
				char c = key.charAt( 0 );
				if ( (c >= '0' && c <= '9') || c == '+' ) {
					int intKey = Caster.toIntValue( key.getString(), -1 );
					if ( intKey > 0 && intKey <= argCount ) {
						return argArray[(intKey - 1) * 2 + 1];
					}
				}
			}
			return defaultValue;
		}

		Object o = super.g(key, Null.NULL);
		if (o != Null.NULL) return o;

		if (key.length() > 0) {
			char c = key.charAt(0);
			if ((c >= '0' && c <= '9') || c == '+') {
				o = get(Caster.toIntValue(key.getString(), -1), Null.NULL);
				if (o != Null.NULL) return o;
			}
		}
		return defaultValue;
	}

	@Override
	public Object get(Collection.Key key) throws ExpressionException {
		// null is supported as returned value with argument scope
		if ( !useHashMap ) {
			// Linear search through array
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					return argArray[i * 2 + 1];
				}
			}

			// Try numeric access if key looks like a number
			if ( key.length() > 0 ) {
				char c = key.charAt( 0 );
				if ( (c >= '0' && c <= '9') || c == '+' ) {
					int intKey = Caster.toIntValue( key.getString(), -1 );
					if ( intKey > 0 && intKey <= argCount ) {
						return argArray[(intKey - 1) * 2 + 1];
					}
				}
			}

			throw new ExpressionException("The key [" + key.getString() + "] doesn't exist in the arguments scope. The existing keys are ["
					+ lucee.runtime.type.util.ListUtil.arrayToList(keys(), ", ") + "]");
		}

		Object o = super.g(key, Null.NULL);
		if (o != Null.NULL) return o;

		if (key.length() > 0) {
			char c = key.charAt(0);
			if ((c >= '0' && c <= '9') || c == '+') {
				o = get(Caster.toIntValue(key.getString(), -1), Null.NULL);
				if (o != Null.NULL) return o;
			}
		}

		throw new ExpressionException("The key [" + key.getString() + "] doesn't exist in the arguments scope. The existing keys are ["
				+ lucee.runtime.type.util.ListUtil.arrayToList(CollectionUtil.keys(this), ", ") + "]");
	}

	@Override
	public Object get(int intKey, Object defaultValue) {
		if ( !useHashMap ) {
			// Direct array access (1-based indexing)
			if ( intKey > 0 && intKey <= argCount ) {
				return argArray[(intKey - 1) * 2 + 1];
			}
			return defaultValue;
		}

		Iterator<Object> it = valueIterator(); // keyIterator();//getMap().keySet().iterator();
		int count = 0;
		Object o;
		while (it.hasNext()) {
			o = it.next();
			if ((++count) == intKey) {
				return o;// super.get(o.toString(),defaultValue);
			}
		}
		return defaultValue;
	}

	/**
	 * return a value matching to key
	 *
	 * @param intKey
	 * @return value matching key
	 * @throws PageException
	 */
	@Override
	public Object getE(int intKey) throws PageException {
		if ( !useHashMap ) {
			// Direct array access (1-based indexing)
			if ( intKey > 0 && intKey <= argCount ) {
				return argArray[(intKey - 1) * 2 + 1];
			}
			throw new ExpressionException("invalid index [" + intKey + "] for argument scope");
		}

		Iterator<Object> it = valueIterator();// getMap().keySet().iterator();
		int count = 0;
		Object o;
		while (it.hasNext()) {
			o = it.next();
			if ((++count) == intKey) {
				return o;// super.get(o.toString());
			}
		}
		throw new ExpressionException("invalid index [" + intKey + "] for argument scope");
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable htmlBox = new DumpTable("struct", "#468faf", "#89c2d9", "#000000");
		htmlBox.setTitle("Scope Arguments");
		if (size() > 10 && dp.getMetainfo()) htmlBox.setComment("Entries:" + size());

		maxlevel--;
		// Map mapx=getMap();
		Iterator<Key> it = keyIterator();// mapx.keySet().iterator();
		int count = 0;
		Collection.Key key;
		int maxkeys = dp.getMaxKeys();
		int index = 0;
		while (it.hasNext()) {
			key = it.next();// it.next();

			if (DumpUtil.keyValid(dp, maxlevel, key)) {
				if (maxkeys <= index++) break;
				htmlBox.appendRow(3, new SimpleDumpData(key.getString()), new SimpleDumpData(++count), DumpUtil.toDumpData(get(key, null), pageContext, maxlevel, dp));
			}
		}
		return htmlBox;
	}

	@Override
	public int getDimension() {
		return 1;
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		if ( !useHashMap ) {
			// Check if key exists, update it
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					argArray[i * 2 + 1] = value;
					return value;
				}
			}
			// Key doesn't exist, add it
			ensureCapacity( argCount + 1 );
			argArray[argCount * 2] = key;
			argArray[argCount * 2 + 1] = value;
			argCount++;
			return value;
		}
		return super.set( key, value );
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		if ( !useHashMap ) {
			// Check if key exists, update it
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					argArray[i * 2 + 1] = value;
					return value;
				}
			}
			// Key doesn't exist, add it
			ensureCapacity( argCount + 1 );
			argArray[argCount * 2] = key;
			argArray[argCount * 2 + 1] = value;
			argCount++;
			return value;
		}
		return super.setEL( key, value );
	}

	@Override
	public Object setEL(int intKey, Object value) {
		int count = 0;

		if (intKey > size()) {
			return setEL(Caster.toString(intKey), value);
		}
		// Iterator it = keyIterator();
		Key[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			if ((++count) == intKey) {
				return super.setEL(keys[i], value);
			}
		}
		return value;
	}

	@Override
	public Object setE(int intKey, Object value) throws PageException {

		if (intKey > size()) {
			return set(Caster.toString(intKey), value);
		}
		// Iterator it = keyIterator();
		Key[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			if ((i + 1) == intKey) {
				return super.set(keys[i], value);
			}
		}
		throw new ExpressionException("invalid index [" + intKey + "] for argument scope");
	}

	@Override
	public int[] intKeys() {
		int[] ints = new int[size()];
		for (int i = 0; i < ints.length; i++)
			ints[i] = i + 1;
		return ints;
	}

	@Override
	public boolean insert(int index, Object value) throws ExpressionException {
		return insert(index, "" + index, value);
	}

	@Override
	public boolean insert(int index, String key, Object value) throws ExpressionException {
		// Rare operation - convert to HashMap
		convertToHashMap();

		int len = size();
		if (index < 1 || index > len) throw new ExpressionException("invalid index to insert a value to argument scope",
				len == 0 ? "can't insert in an empty argument scope" : "valid index goes from 1 to " + (len - 1));

		// remove all upper
		LinkedHashMap lhm = new LinkedHashMap();
		Collection.Key[] keys = keys();

		Collection.Key k;
		for (int i = 1; i <= keys.length; i++) {
			if (i < index) continue;
			k = keys[i - 1];
			lhm.put(k.getString(), get(k, null));
			removeEL(k);
		}

		// set new value
		setEL(key, value);

		// reset upper values
		Iterator it = lhm.entrySet().iterator();
		Map.Entry entry;
		while (it.hasNext()) {
			entry = (Entry) it.next();
			setEL(KeyImpl.toKey(entry.getKey()), entry.getValue());
		}
		return true;
	}

	@Override
	public Object append(Object o) throws PageException {
		return set(Caster.toString(size() + 1), o);
	}

	@Override
	public Object appendEL(Object o) {
		try {
			return append(o);
		}
		catch (PageException e) {
			return null;
		}
	}

	@Override
	public Object prepend(Object o) throws PageException {
		// Rare operation - convert to HashMap
		convertToHashMap();

		for (int i = size(); i > 0; i--) {
			setE(i + 1, getE(i));
		}
		setE(1, o);
		return o;
	}

	@Override
	public void resize(int to) throws PageException {
		// Rare operation - convert to HashMap
		convertToHashMap();

		for (int i = size(); i < to; i++) {
			append(null);
		}
		// throw new ExpressionException("can't resize this array");
	}

	@Override
	public void sort(String sortType, String sortOrder) throws ExpressionException {
		// Rare operation - convert to HashMap
		convertToHashMap();

		// TODO Impl.
		throw new ExpressionException("can't sort [" + sortType + "-" + sortOrder + "] Argument Scope", "not Implemnted Yet");
	}

	@Override
	public void sortIt(Comparator com) {
		// Rare operation - convert to HashMap
		convertToHashMap();

		// TODO Impl.
		throw new PageRuntimeException("can't sort Argument Scope", "not Implemnted Yet");
	}

	@Override
	public Object[] toArray() {
		Iterator it = keyIterator();// getMap().keySet().iterator();
		Object[] arr = new Object[size()];
		int count = 0;

		while (it.hasNext()) {
			arr[count++] = it.next();
		}
		return arr;
	}

	@Override
	public Object setArgument(Object obj) throws PageException {
		if (obj == this) return obj;

		if (Decision.isStruct(obj)) {
			clear(); // TODO bessere impl. anstelle vererbung wrao auf struct
			Struct sct = Caster.toStruct(obj);
			Iterator<Key> it = sct.keyIterator();
			Key key;
			while (it.hasNext()) {
				key = it.next();
				setEL(key, sct.get(key, null));
			}
			return obj;
		}
		throw new ExpressionException("can not overwrite arguments scope");
	}

	public ArrayList<Object> toArrayList() {
		ArrayList<Object> list = new ArrayList<Object>();
		Object[] arr = toArray();
		for (int i = 0; i < arr.length; i++) {
			list.add(arr[i]);
		}
		return list;
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		if ( !useHashMap ) {
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					Object removed = argArray[i * 2 + 1];
					// Shift remaining elements left
					System.arraycopy( argArray, (i + 1) * 2, argArray, i * 2, (argCount - i - 1) * 2 );
					argCount--;
					// Clear last elements
					argArray[argCount * 2] = null;
					argArray[argCount * 2 + 1] = null;
					return removed;
				}
			}
			throw new ExpressionException("can't remove key [" + key.getString() + "], key doesn't exist");
		}
		return super.remove( key );
	}

	@Override
	public Object remove(Collection.Key key, Object defaultValue) {
		if ( !useHashMap ) {
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					Object removed = argArray[i * 2 + 1];
					// Shift remaining elements left
					System.arraycopy( argArray, (i + 1) * 2, argArray, i * 2, (argCount - i - 1) * 2 );
					argCount--;
					// Clear last elements
					argArray[argCount * 2] = null;
					argArray[argCount * 2 + 1] = null;
					return removed;
				}
			}
			return defaultValue;
		}
		return super.remove( key, defaultValue );
	}

	@Override
	public Object removeEL(Collection.Key key) {
		if ( !useHashMap ) {
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					Object removed = argArray[i * 2 + 1];
					// Shift remaining elements left
					System.arraycopy( argArray, (i + 1) * 2, argArray, i * 2, (argCount - i - 1) * 2 );
					argCount--;
					// Clear last elements
					argArray[argCount * 2] = null;
					argArray[argCount * 2 + 1] = null;
					return removed;
				}
			}
			return null;
		}
		return super.removeEL( key );
	}

	@Override
	public void clear() {
		if ( !useHashMap ) {
			argCount = 0;
			// Help GC by nulling out references
			for ( int i = 0; i < argArray.length; i++ ) {
				argArray[i] = null;
			}
			return;
		}
		super.clear();
	}

	@Override
	public Object removeE(int intKey) throws PageException {
		// Rare operation - convert to HashMap
		convertToHashMap();

		Key[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			if ((i + 1) == intKey) {
				return super.remove(keys[i]);
			}
		}
		throw new ExpressionException("can't remove argument number [" + intKey + "], argument doesn't exist");
	}

	@Override
	public Object removeEL(int intKey) {
		return remove(intKey, null);
	}

	public Object remove(int intKey, Object defaultValue) {
		// Rare operation - convert to HashMap
		convertToHashMap();

		Key[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			if ((i + 1) == intKey) {
				return super.removeEL(keys[i]);
			}
		}
		return defaultValue;
	}

	@Override
	public Object pop() throws PageException {
		return removeE(size());
	}

	@Override
	public synchronized Object pop(Object defaultValue) {
		return remove(size(), defaultValue);
	}

	@Override
	public Object shift() throws PageException {
		return removeE(1);
	}

	@Override
	public synchronized Object shift(Object defaultValue) {
		return remove(1, defaultValue);
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		if ( !useHashMap ) {
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					Object val = argArray[i * 2 + 1];
					if ( val == null && !NullSupportHelper.full() ) return false;
					return true;
				}
			}
			return false;
		}

		Object val = super.g(key, CollectionUtil.NULL);
		if (val == CollectionUtil.NULL) return false;
		if (val == null && !NullSupportHelper.full()) return false;
		return true;
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		if ( !useHashMap ) {
			for ( int i = 0; i < argCount; i++ ) {
				if ( argArray[i * 2].equals( key ) ) {
					Object val = argArray[i * 2 + 1];
					if ( val == null && !NullSupportHelper.full( pc ) ) return false;
					return true;
				}
			}
			return false;
		}

		Object val = super.g(key, CollectionUtil.NULL);
		if (val == CollectionUtil.NULL) return false;
		if (val == null && !NullSupportHelper.full(pc)) return false;
		return true;
	}
	/*
	 * public boolean containsKey(Collection.Key key) { return get(key,null)!=null &&
	 * super.containsKey(key); }
	 */

	@Override
	public boolean containsKey(int key) {
		return key > 0 && key <= size();
	}

	@Override
	public List toList() {
		return ArrayAsList.toList(this);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		ArgumentImpl trg = new ArgumentImpl();
		trg.bind = false;
		trg.functionArgumentNames = functionArgumentNames;
		// trg.supportFunctionArguments=supportFunctionArguments;
		copy(this, trg, deepCopy);
		return trg;
	}

	@Override
	public void setFunctionArgumentNames(Set functionArgumentNames) {
		this.functionArgumentNames = functionArgumentNames;
	}
	/*
	 * public void setNamedArguments(boolean namedArguments) { this.namedArguments=namedArguments; }
	 * public boolean isNamedArguments() { return namedArguments; }
	 */

	/**
	 * converts an argument scope to a regular struct
	 * 
	 * @param arg argument scope to convert
	 * @return resulting struct
	 */
	public static Struct toStruct(Argument arg) {
		return StructImpl.copy(arg, false);
	}

	/**
	 * converts an argument scope to a regular array
	 * 
	 * @param arg argument scope to convert
	 * @return resulting array
	 */
	public static Array toArray(Argument arg) {
		ArrayImpl trg = new ArrayImpl();
		int[] keys = arg.intKeys();
		for (int i = 0; i < keys.length; i++) {
			trg.setEL(keys[i], arg.get(keys[i], null));
		}
		return trg;
	}

	@Override
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
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] args) throws PageException {
		Object obj = get(methodName, null);
		if (obj instanceof UDF) {
			return ((UDF) obj).call(pc, methodName, args, false);
		}
		return MemberUtil.call(pc, this, methodName, args, new short[] { CFTypes.TYPE_STRUCT }, new String[] { "struct" });
		// return MemberUtil.call(pc, this, methodName, args, CFTypes.TYPE_ARRAY, "array");
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		Object obj = get(methodName, null);
		if (obj instanceof UDF) {
			return ((UDF) obj).callWithNamedValues(pc, methodName, args, false);
		}
		return MemberUtil.callWithNamedValues(pc, this, methodName, args, CFTypes.TYPE_STRUCT, "struct");
		// return MemberUtil.callWithNamedValues(pc,this,methodName,args, CFTypes.TYPE_ARRAY, "array");
	}

	@Override
	public Iterator<Entry<Integer, Object>> entryArrayIterator() {
		return new EntryArrayIterator(this, intKeys());
	}
}