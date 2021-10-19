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
package lucee.runtime.type.scope;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	// private boolean supportFunctionArguments;

	/**
	 * constructor of the class
	 */
	public ArgumentImpl() {
		super("arguments", SCOPE_ARGUMENTS, Struct.TYPE_LINKED);
		// this(true);
	}

	@Override
	public void release(PageContext pc) {
		functionArgumentNames = null;
		super.release(ThreadLocalPageContext.get(pc));
	}

	@Override
	public void setBind(boolean bind) {
		this.bind = bind;
	}

	@Override
	public boolean isBind() {
		return this.bind;
	}

	@Override
	public Object getFunctionArgument(String key, Object defaultValue) {
		return getFunctionArgument(KeyImpl.getInstance(key), defaultValue);
	}

	@Override
	public Object getFunctionArgument(Collection.Key key, Object defaultValue) {
		return super.get(key, defaultValue);
	}

	@Override
	public boolean containsFunctionArgumentKey(Key key) {
		return super.containsKey(key);// functionArgumentNames!=null && functionArgumentNames.contains(key);
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		/*
		 * if(NullSupportHelper.full()) { Object o=super.get(key,NullSupportHelper.NULL());
		 * if(o!=NullSupportHelper.NULL())return o;
		 * 
		 * o=get(Caster.toIntValue(key.getString(),-1),NullSupportHelper.NULL());
		 * if(o!=NullSupportHelper.NULL())return o; return defaultValue; }
		 */

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
		Object o = super.g(key, Null.NULL);
		if (o != Null.NULL) return o;

		if (key.length() > 0) {
			char c = key.charAt(0);
			if ((c >= '0' && c <= '9') || c == '+') {
				o = get(Caster.toIntValue(key.getString(), -1), Null.NULL);
				if (o != Null.NULL) return o;
			}
		}

		throw new ExpressionException("key [" + key.getString() + "] doesn't exist in argument scope. existing keys are ["
				+ lucee.runtime.type.util.ListUtil.arrayToList(CollectionUtil.keys(this), ", ") + "]");
	}

	@Override
	public Object get(int intKey, Object defaultValue) {
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
		Iterator it = valueIterator();// getMap().keySet().iterator();
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
		DumpTable htmlBox = new DumpTable("struct", "#9999ff", "#ccccff", "#000000");
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
		for (int i = size(); i > 0; i--) {
			setE(i + 1, getE(i));
		}
		setE(1, o);
		return o;
	}

	@Override
	public void resize(int to) throws PageException {
		for (int i = size(); i < to; i++) {
			append(null);
		}
		// throw new ExpressionException("can't resize this array");
	}

	@Override
	public void sort(String sortType, String sortOrder) throws ExpressionException {
		// TODO Impl.
		throw new ExpressionException("can't sort [" + sortType + "-" + sortOrder + "] Argument Scope", "not Implemnted Yet");
	}

	@Override
	public void sortIt(Comparator com) {
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

	public ArrayList toArrayList() {
		ArrayList list = new ArrayList();
		Object[] arr = toArray();
		for (int i = 0; i < arr.length; i++) {
			list.add(arr[i]);
		}
		return list;
	}

	@Override
	public Object removeE(int intKey) throws PageException {
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
		Object val = super.g(key, CollectionUtil.NULL);
		if (val == CollectionUtil.NULL) return false;
		if (val == null && !NullSupportHelper.full()) return false;
		return true;
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
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
	public void setFunctionArgumentNames(Set functionArgumentNames) {// future add to interface
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
		Struct trg = new StructImpl();
		StructImpl.copy(arg, trg, false);
		return trg;
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