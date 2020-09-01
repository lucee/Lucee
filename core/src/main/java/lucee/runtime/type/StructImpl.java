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

import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.SOFT;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.collections4.map.ReferenceMap;

import lucee.commons.collection.MapFactory;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

/**
 * CFML data type struct
 */
public class StructImpl extends StructSupport {
	private static final long serialVersionUID = 1421746759512286393L;
	private static final int TYPE_LINKED_NOT_SYNC = 100;
	private static final int DEFAULT_INITIAL_CAPACITY = 32;
	public static final Object NULL = new Object();

	private Map<Collection.Key, Object> map;
	private final int type;

	/**
	 * default constructor
	 */
	public StructImpl() {
		this(StructImpl.TYPE_UNDEFINED, DEFAULT_INITIAL_CAPACITY);// asx
	}

	/**
	 * This implementation spares its clients from the unspecified, generally chaotic ordering provided
	 * by normally Struct , without incurring the increased cost associated with TreeMap. It can be used
	 * to produce a copy of a map that has the same order as the original
	 * 
	 * @param type
	 */
	public StructImpl(int type) {
		this(type, DEFAULT_INITIAL_CAPACITY);
	}

	/**
	 * This implementation spares its clients from the unspecified, generally chaotic ordering provided
	 * by normally Struct , without incurring the increased cost associated with TreeMap. It can be used
	 * to produce a copy of a map that has the same order as the original
	 * 
	 * @param type
	 * @param initialCapacity initial capacity - MUST be a power of two.
	 */
	public StructImpl(int type, int initialCapacity) {
		if (type == TYPE_WEAKED) map = Collections.synchronizedMap(new WeakHashMap<Collection.Key, Object>(initialCapacity));
		else if (type == TYPE_SOFT) map = Collections.synchronizedMap(new ReferenceMap<Collection.Key, Object>(HARD, SOFT, initialCapacity, 0.75f));
		else if (type == TYPE_LINKED) map = Collections.synchronizedMap(new LinkedHashMap<Collection.Key, Object>(initialCapacity));
		else if (type == TYPE_LINKED_NOT_SYNC) map = new LinkedHashMap<Collection.Key, Object>(initialCapacity);
		else map = MapFactory.getConcurrentMap(initialCapacity);
		this.type = type;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		int type = getType();
		String subject = type == Struct.TYPE_REGULAR || type == Struct.TYPE_SYNC || type == Struct.TYPE_UNDEFINED ? "Struct" : "Struct (" + StructUtil.toType(type, "") + ")";

		return StructUtil.toDumpTable(this, subject, pageContext, maxlevel, properties);
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		Object val = map.getOrDefault(key, CollectionUtil.NULL);
		if (val == CollectionUtil.NULL) return defaultValue;
		if (val == null && !NullSupportHelper.full()) return defaultValue;
		return val;
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		Object val = map.getOrDefault(key, CollectionUtil.NULL);
		if (val == CollectionUtil.NULL) return defaultValue;
		if (val == null && !NullSupportHelper.full(pc)) return defaultValue;
		return val;
	}

	public Object g(Collection.Key key, Object defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}

	public Object g(Collection.Key key) throws PageException {
		Object res = map.getOrDefault(key, NULL);
		if (res != NULL) return res;
		throw StructSupport.invalidKey(null, this, key, null);
	}

	@Override
	public Object get(Collection.Key key) throws PageException {
		Object res = map.getOrDefault(key, NULL);
		if (res == NULL) throw StructSupport.invalidKey(null, this, key, null);
		if (res == null && !NullSupportHelper.full()) {
			throw StructSupport.invalidKey(null, this, key, null);
		}
		return res;
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		Object res = map.getOrDefault(key, NULL);
		if (res == NULL) throw StructSupport.invalidKey(null, this, key, null);
		if (res == null && !NullSupportHelper.full(pc)) {
			throw StructSupport.invalidKey(null, this, key, null);
		}
		return res;
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		map.put(key, value);
		return value;
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		map.put(key, value);
		return value;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection.Key[] keys() {
		try {
			return map.keySet().toArray(new Key[map.size()]);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			Map<Key, Object> old = map;
			try {
				map = Collections.synchronizedMap(map);
				Set<Key> set = map.keySet();
				Collection.Key[] keys = new Collection.Key[set.size()];
				Iterator<Key> it = set.iterator();
				int count = 0;
				while (it.hasNext() && keys.length > count) {
					keys[count++] = KeyImpl.toKey(it.next(), null);
				}
				return keys;
			}
			finally {
				map = old;
			}
		}
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		if (!map.containsKey(key)) throw new ExpressionException("can't remove key [" + key + "] from struct, key does not exist");

		Object res = map.remove(key);
		if (res != null || NullSupportHelper.full()) return res;
		throw new ExpressionException("can't remove key [" + key + "] from struct, key value is [null] what is equal do not existing in case full null support is not enabled");

	}

	@Override
	public Object removeEL(Collection.Key key) {
		return map.remove(key);
	}

	@Override
	public Object remove(Collection.Key key, Object defaultValue) {
		if (!map.containsKey(key)) return defaultValue;

		Object res = map.remove(key);
		if (res != null || NullSupportHelper.full()) return res;
		return defaultValue;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		Struct sct = new StructImpl(getType());
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
		return map.keySet().iterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new StringIterator(keys());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return this.map.entrySet().iterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return map.values().iterator();
	}

	@Override
	public boolean containsKey(Collection.Key key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsKey(PageContext pc, Collection.Key key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public java.util.Collection<Object> values() {
		return map.values();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StructImpl)) return false;
		StructImpl other = (StructImpl) obj;
		if (other.size() != size()) return false;

		Key[] a;
		Key[] b;
		Arrays.sort(a = other.keys());
		Arrays.sort(b = keys());
		if (!ListUtil.arrayToList(a, ",").equalsIgnoreCase(ListUtil.arrayToList(b, ","))) return false;
		for (Key k: a) {
			if (!other.get(k, "").equals(get(k, ""))) return false;
		}

		return true;
	}
}