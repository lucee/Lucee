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
package lucee.runtime.type.wrap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

/**
 * 
 */
public class MapAsStruct extends StructSupport implements Struct {

	Map map;
	private boolean caseSensitive;

	/**
	 * constructor of the class
	 * 
	 * @param map
	 * @param caseSensitive
	 */
	private MapAsStruct(Map map, boolean caseSensitive) {
		this.map = map;
		this.caseSensitive = caseSensitive;
	}

	public static Struct toStruct(Map map) {
		return toStruct(map, false);
	}

	public static Struct toStruct(Map map, boolean caseSensitive) {
		if (map instanceof Struct) return ((Struct) map);
		return new MapAsStruct(map, caseSensitive);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection.Key[] keys() {
		Set set = map.keySet();
		Iterator it = set.iterator();
		Collection.Key[] k = new Collection.Key[set.size()];
		int count = 0;
		while (it.hasNext()) {
			k[count++] = KeyImpl.init(StringUtil.toStringNative(it.next(), ""));
		}
		return k;
	}

	public static String getCaseSensitiveKey(Map map, String key) {
		Iterator it = map.keySet().iterator();
		String strKey;
		while (it.hasNext()) {
			strKey = Caster.toString(it.next(), "");
			if (strKey.equalsIgnoreCase(key)) return strKey;
		}
		return null;
	}

	@Override
	public Object remove(Collection.Key key) throws ExpressionException {
		Object obj = map.remove(key.getString());
		if (obj == null) {
			if (map.containsKey(key.getString())) return null;
			if (!caseSensitive) {
				String csKey = getCaseSensitiveKey(map, key.getString());
				if (csKey != null) obj = map.remove(csKey);
				if (obj != null) return obj;
			}
			throw new ExpressionException("can't remove key [" + key.getString() + "] from map, key doesn't exist");
		}
		return obj;
	}

	@Override
	public Object removeEL(Collection.Key key) {
		Object obj = map.remove(key.getString());
		if (!caseSensitive && obj == null) {
			String csKey = getCaseSensitiveKey(map, key.getString());
			if (csKey != null) obj = map.remove(csKey);
		}
		return obj;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public final Object get(Collection.Key key) throws ExpressionException {
		return get((PageContext) null, key);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key) throws ExpressionException {
		Object o = map.get(key.getString());
		if (o == null) {
			if (map.containsKey(key.getString())) return null;
			if (!caseSensitive) {
				String csKey = getCaseSensitiveKey(map, key.getString());
				if (csKey != null) o = map.get(csKey);
				if (o != null || map.containsKey(csKey)) return o;
			}
			throw new ExpressionException("key " + key.getString() + " doesn't exist in " + Caster.toClassName(map));
		}
		return o;
	}

	@Override
	public final Object get(Collection.Key key, Object defaultValue) {
		return get((PageContext) null, key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		Object obj = map.get(key.getString());
		if (obj == null) {
			if (map.containsKey(key.getString())) return null;
			if (!caseSensitive) {
				String csKey = getCaseSensitiveKey(map, key.getString());
				if (csKey != null) obj = map.get(csKey);
				if (obj != null || map.containsKey(csKey)) return obj;
			}
			return defaultValue;
		}
		return obj;
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		return map.put(key.getString(), value);
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		return map.put(key.getString(), value);
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
		return new ValueIterator(this, keys());
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return DumpUtil.toDumpData(map, pageContext, maxlevel, dp);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new MapAsStruct(Duplicator.duplicateMap(map, deepCopy), caseSensitive);
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return containsKey(null, key);
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		boolean contains = map.containsKey(key.getString());
		if (contains) return true;
		if (!caseSensitive) return map.containsKey(getCaseSensitiveKey(map, key.getString()));
		return false;
	}

	@Override
	public String castToString() throws ExpressionException {
		throw new ExpressionException("Can't cast Complex Object Type Struct [" + getClass().getName() + "] to String",
				"Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct");
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		throw new ExpressionException("Can't cast Complex Object Type Struct [" + getClass().getName() + "] to a boolean value");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws ExpressionException {
		throw new ExpressionException("Can't cast Complex Object Type Struct [" + getClass().getName() + "] to a number value");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws ExpressionException {
		throw new ExpressionException("Can't cast Complex Object Type Struct [" + getClass().getName() + "] to a Date");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public int compareTo(boolean b) throws ExpressionException {
		throw new ExpressionException("can't compare Complex Object Type Struct [" + getClass().getName() + "] with a boolean value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Struct [" + getClass().getName() + "] with a DateTime Object");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Struct [" + getClass().getName() + "] with a numeric value");
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Struct [" + getClass().getName() + "] with a String");
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public java.util.Collection values() {
		return map.values();
	}

	@Override
	public int getType() {
		return StructUtil.getType(map);
	}
}