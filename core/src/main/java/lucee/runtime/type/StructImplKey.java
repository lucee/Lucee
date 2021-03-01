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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import lucee.commons.collection.MapFactory;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

/**
 * CFML data type struct
 */
public final class StructImplKey extends StructSupport implements Struct {

	public static final int TYPE_WEAKED = 0;
	public static final int TYPE_LINKED = 1;
	public static final int TYPE_SYNC = 2;
	public static final int TYPE_REGULAR = 3;

	private Map<Collection.Key, Object> _map;
	// private static int scount=0;
	// private static int kcount=0;

	/**
	 * default constructor
	 */
	public StructImplKey() {
		_map = new HashMap<Collection.Key, Object>();
	}

	/**
	 * This implementation spares its clients from the unspecified, generally chaotic ordering provided
	 * by normally Struct , without incurring the increased cost associated with TreeMap. It can be used
	 * to produce a copy of a map that has the same order as the original
	 * 
	 * @param doubleLinked
	 */
	public StructImplKey(int type) {
		if (type == TYPE_LINKED) _map = new LinkedHashMap<Collection.Key, Object>();
		else if (type == TYPE_WEAKED) _map = new java.util.WeakHashMap<Collection.Key, Object>();
		else if (type == TYPE_SYNC) _map = MapFactory.<Collection.Key, Object>getConcurrentMap();
		else _map = new HashMap<Collection.Key, Object>();
	}

	@Override
	public final Object get(Collection.Key key, Object defaultValue) {
		Object rtn = _map.get(key);
		if (rtn != null) return rtn;
		return defaultValue;
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		Object rtn = _map.get(key);
		if (rtn != null) return rtn;
		return defaultValue;
	}

	@Override
	public final Object get(Collection.Key key) throws PageException {// print.out("k:"+(kcount++));
		Object rtn = _map.get(key);
		if (rtn != null) return rtn;
		throw invalidKey(key.getString());
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key) throws PageException {// print.out("k:"+(kcount++));
		Object rtn = _map.get(key);
		if (rtn != null) return rtn;
		throw invalidKey(key.getString());
	}

	/**
	 * @see lucee.runtime.type.Collection#set(lucee.runtime.type.Collection.Key, java.lang.Object)
	 */
	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		_map.put(key, value);
		return value;
	}

	/**
	 * @see lucee.runtime.type.Collection#setEL(lucee.runtime.type.Collection.Key, java.lang.Object)
	 */
	@Override
	public Object setEL(Collection.Key key, Object value) {
		_map.put(key, value);
		return value;
	}

	/**
	 * @see lucee.runtime.type.Collection#size()
	 */
	@Override
	public int size() {
		return _map.size();
	}

	@Override
	public Collection.Key[] keys() {// print.out("keys");
		Iterator<Key> it = keyIterator();
		Collection.Key[] keys = new Collection.Key[size()];
		int count = 0;
		while (it.hasNext()) {
			keys[count++] = it.next();
		}
		return keys;
	}

	/**
	 * @see lucee.runtime.type.Collection#remove(lucee.runtime.type.Collection.Key)
	 */
	@Override
	public Object remove(Collection.Key key) throws PageException {
		Object obj = _map.remove(key);
		if (obj == null) throw new ExpressionException("Cannot remove key [" + key.getString() + "] from struct, the key doesn't exist");
		return obj;
	}

	@Override
	public Object removeEL(Collection.Key key) {
		return _map.remove(key);
	}

	/**
	 * @see lucee.runtime.type.Collection#clear()
	 */
	@Override
	public void clear() {
		_map.clear();
	}

	/**
	 *
	 * @see lucee.runtime.dump.Dumpable#toDumpData(lucee.runtime.PageContext, int)
	 */
	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		Iterator it = _map.keySet().iterator();

		DumpTable table = new DumpTable("struct", "#9999ff", "#ccccff", "#000000");
		table.setTitle("Struct");
		maxlevel--;
		int maxkeys = dp.getMaxKeys();
		int index = 0;
		while (it.hasNext()) {
			Object key = it.next();
			if (DumpUtil.keyValid(dp, maxlevel, key.toString())) {
				if (maxkeys <= index++) break;
				table.appendRow(1, new SimpleDumpData(key.toString()), DumpUtil.toDumpData(_map.get(key), pageContext, maxlevel, dp));
			}
		}
		return table;
	}

	/**
	 * throw exception for invalid key
	 * 
	 * @param key Invalid key
	 * @return returns an invalid key Exception
	 */
	protected ExpressionException invalidKey(String key) {
		return new ExpressionException("Key [" + key + "] doesn't exist in struct");
	}

	/**
	 * @see lucee.runtime.type.Collection#duplicate(boolean)
	 */
	@Override
	public Collection duplicate(boolean deepCopy) {
		Struct sct = new StructImplKey();
		copy(this, sct, deepCopy);
		return sct;
	}

	public static void copy(Struct src, Struct trg, boolean deepCopy) {
		boolean inside = ThreadLocalDuplication.set(src, trg);
		try {
			Iterator<Entry<Key, Object>> it = src.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				if (!deepCopy) trg.setEL(e.getKey(), e.getValue());
				else trg.setEL(e.getKey(), Duplicator.duplicate(e.getValue(), deepCopy));
			}
		}
		finally {
			if (!inside) ThreadLocalDuplication.reset();
		}
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return _map.keySet().iterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new StringIterator(keys());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	/**
	 * @see lucee.runtime.type.Iteratorable#iterator()
	 */
	@Override
	public Iterator valueIterator() {
		return _map.values().iterator();
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return _map.containsKey(key);
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		return _map.containsKey(key);
	}

	/**
	 * @see lucee.runtime.op.Castable#castToString()
	 */
	@Override
	public String castToString() throws ExpressionException {
		throw new ExpressionException("Cannot cast [Struct] to String", "Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct");
	}

	/**
	 * @see lucee.runtime.type.util.StructSupport#castToString(java.lang.String)
	 */
	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	/**
	 * @see lucee.runtime.op.Castable#castToBooleanValue()
	 */
	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		throw new ExpressionException("Cannot cast [Struct] to a boolean value");
	}

	/**
	 * @see lucee.runtime.op.Castable#castToBoolean(java.lang.Boolean)
	 */
	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	/**
	 * @see lucee.runtime.op.Castable#castToDoubleValue()
	 */
	@Override
	public double castToDoubleValue() throws ExpressionException {
		throw new ExpressionException("Cannot cast [Struct] to a numeric value");
	}

	/**
	 * @see lucee.runtime.op.Castable#castToDoubleValue(double)
	 */
	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	/**
	 * @see lucee.runtime.op.Castable#castToDateTime()
	 */
	@Override
	public DateTime castToDateTime() throws ExpressionException {
		throw new ExpressionException("Cannot cast [Struct] to a Date");
	}

	/**
	 * @see lucee.runtime.op.Castable#castToDateTime(lucee.runtime.type.dt.DateTime)
	 */
	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	/**
	 * @see lucee.runtime.op.Castable#compare(boolean)
	 */
	@Override
	public int compareTo(boolean b) throws ExpressionException {
		throw new ExpressionException("Cannot compare a [Struct] with a boolean value");
	}

	/**
	 * @see lucee.runtime.op.Castable#compareTo(lucee.runtime.type.dt.DateTime)
	 */
	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("Cannot compare a [Struct] with a DateTime Object");
	}

	/**
	 * @see lucee.runtime.op.Castable#compareTo(double)
	 */
	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("Cannot compare a [Struct] with a numeric value");
	}

	/**
	 * @see lucee.runtime.op.Castable#compareTo(java.lang.String)
	 */
	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("Cannot compare a [Struct] with a String");
	}

	@Override
	public boolean containsValue(Object value) {
		return _map.containsValue(value);
	}

	@Override
	public java.util.Collection values() {
		return _map.values();
	}

	@Override
	public int getType() {
		return StructUtil.getType(_map);
	}

}
