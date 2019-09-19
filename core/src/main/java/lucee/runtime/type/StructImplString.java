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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.StructUtil;

/**
 * CFML data type struct
 */
public final class StructImplString extends StructImpl implements Struct {

	public static final int TYPE_WEAKED = 0;
	public static final int TYPE_LINKED = 1;
	public static final int TYPE_SYNC = 2;
	public static final int TYPE_REGULAR = 3;

	private Map<Collection.Key, Object> map;
	// private static int scount=0;
	// private static int kcount=0;

	/**
	 * default constructor
	 */
	public StructImplString() {
		map = new HashMap<Collection.Key, Object>();
	}

	/**
	 * This implementation spares its clients from the unspecified, generally chaotic ordering provided
	 * by normally Struct , without incurring the increased cost associated with TreeMap. It can be used
	 * to produce a copy of a map that has the same order as the original
	 * 
	 * @param doubleLinked
	 */
	public StructImplString(int type) {
		if (type == TYPE_LINKED) map = new LinkedHashMap<Collection.Key, Object>();
		else if (type == TYPE_WEAKED) map = new java.util.WeakHashMap<Collection.Key, Object>();
		else if (type == TYPE_SYNC) map = MapFactory.<Collection.Key, Object>getConcurrentMap();
		else map = new HashMap<Collection.Key, Object>();
	}

	/**
	 * @see lucee.runtime.type.Collection#get(lucee.runtime.type.Collection.Key, java.lang.Object)
	 */
	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		Object rtn = map.get(key.getLowerString());
		if (rtn != null) return rtn;
		return defaultValue;
	}

	@Override
	public Object get(Collection.Key key) throws PageException {
		Object rtn = map.get(key.getLowerString());
		if (rtn != null) return rtn;
		throw invalidKey(key.getString());
	}

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		Object rtn = map.get(key.getLowerString());
		if (rtn != null) return rtn;
		throw invalidKey(key.getString());
	}

	/**
	 * @see lucee.runtime.type.Collection#set(lucee.runtime.type.Collection.Key, java.lang.Object)
	 */
	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		map.put(key, value);
		return value;
	}

	/**
	 * @see lucee.runtime.type.Collection#setEL(lucee.runtime.type.Collection.Key, java.lang.Object)
	 */
	@Override
	public Object setEL(Collection.Key key, Object value) {
		map.put(key, value);
		return value;
	}

	/**
	 * @see lucee.runtime.type.Collection#size()
	 */
	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection.Key[] keys() {
		Iterator<Key> it = map.keySet().iterator();
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
		Object obj = map.remove(key.getLowerString());
		if (obj == null) throw new ExpressionException("can't remove key [" + key + "] from struct, key doesn't exist");
		return obj;
	}

	/**
	 *
	 * @see lucee.runtime.type.Collection#removeEL(lucee.runtime.type.Collection.Key)
	 */
	@Override
	public Object removeEL(Collection.Key key) {
		return map.remove(key.getLowerString());
	}

	/**
	 * @see lucee.runtime.type.Collection#clear()
	 */
	@Override
	public void clear() {
		map.clear();
	}

	/**
	 *
	 * @see lucee.runtime.dump.Dumpable#toDumpData(lucee.runtime.PageContext, int)
	 */
	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return StructUtil.toDumpTable(this, "struct", pageContext, maxlevel, dp);
		/*
		 * Iterator it=map.keySet().iterator();
		 * 
		 * DumpTable table = new DumpTable("struct","#9999ff","#ccccff","#000000");
		 * table.setTitle("Struct"); maxlevel--; while(it.hasNext()) { Object key=it.next();
		 * if(DumpUtil.keyValid(dp, maxlevel,key.toString())) table.appendRow(1,new
		 * SimpleDumpData(key.toString()),DumpUtil.toDumpData(map.get(key), pageContext,maxlevel,dp)); }
		 * return table;
		 */
	}

	/**
	 * throw exception for invalid key
	 * 
	 * @param key Invalid key
	 * @return returns an invalid key Exception
	 */
	protected ExpressionException invalidKey(String key) {
		return new ExpressionException("key [" + key + "] doesn't exist in struct");
	}

	/**
	 * @see lucee.runtime.type.Collection#duplicate(boolean)
	 */
	@Override
	public Collection duplicate(boolean deepCopy) {
		Struct sct = new StructImplString();
		copy(this, sct, deepCopy);
		return sct;
	}

	public static void copy(Struct src, Struct trg, boolean deepCopy) {
		Iterator<Entry<Key, Object>> it = src.entryIterator();
		Entry<Key, Object> e;
		boolean inside = ThreadLocalDuplication.set(src, trg);
		try {
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

	/**
	 * @see lucee.runtime.type.Collection#keyIterator()
	 */
	@Override
	public Iterator<Collection.Key> keyIterator() {
		return map.keySet().iterator();
	}

	/**
	 * @see lucee.runtime.type.Iteratorable#iterator()
	 */
	@Override
	public Iterator valueIterator() {
		return map.values().iterator();
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return map.containsKey(key.getLowerString());
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		return map.containsKey(key.getLowerString());
	}

	/**
	 * @see lucee.runtime.op.Castable#castToString()
	 */
	@Override
	public String castToString() throws ExpressionException {
		throw new ExpressionException("Can't cast Complex Object Type Struct to String", "Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct");
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
		throw new ExpressionException("can't cast Complex Object Type Struct to a boolean value");
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
		throw new ExpressionException("can't cast Complex Object Type Struct to a number value");
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
		throw new ExpressionException("can't cast Complex Object Type Struct to a Date");
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
		throw new ExpressionException("can't compare Complex Object Type Struct with a boolean value");
	}

	/**
	 * @see lucee.runtime.op.Castable#compareTo(lucee.runtime.type.dt.DateTime)
	 */
	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Struct with a DateTime Object");
	}

	/**
	 * @see lucee.runtime.op.Castable#compareTo(double)
	 */
	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Struct with a numeric value");
	}

	/**
	 * @see lucee.runtime.op.Castable#compareTo(java.lang.String)
	 */
	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Struct with a String");
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public java.util.Collection values() {
		return map.values();
	}

}