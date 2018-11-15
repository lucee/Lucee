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

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections4.map.ReferenceMap;

import lucee.commons.collection.HashMapPro;
import lucee.commons.collection.LinkedHashMapPro;
import lucee.commons.collection.MapFactory;
import lucee.commons.collection.MapPro;
import lucee.commons.collection.MapProWrapper;
import lucee.commons.collection.SyncMap;
import lucee.commons.collection.WeakHashMapPro;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SerializableObject;
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
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

/**
 * CFML data type struct
 */
public class StructImpl extends StructSupport {
    private static final long serialVersionUID = 1421746759512286393L;
    private static final int TYPE_LINKED_NOT_SYNC = 100;

    private MapPro<Collection.Key, Object> map;

    /**
     * default constructor
     */
    public StructImpl() {
	this(StructImpl.TYPE_UNDEFINED, HashMapPro.DEFAULT_INITIAL_CAPACITY);// asx
    }

    /**
     * This implementation spares its clients from the unspecified, generally chaotic ordering provided
     * by normally Struct , without incurring the increased cost associated with TreeMap. It can be used
     * to produce a copy of a map that has the same order as the original
     * 
     * @param type
     */
    public StructImpl(int type) {
	this(type, HashMapPro.DEFAULT_INITIAL_CAPACITY);
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
	if (type == TYPE_WEAKED) map = new SyncMap<Collection.Key, Object>(new WeakHashMapPro<Collection.Key, Object>(initialCapacity));
	else if (type == TYPE_SOFT) map = new SyncMap<Collection.Key, Object>(
		new MapProWrapper<Collection.Key, Object>(new ReferenceMap<Collection.Key, Object>(HARD, SOFT, initialCapacity, 0.75f), new SerializableObject()));
	else if (type == TYPE_LINKED) map = new SyncMap<Collection.Key, Object>(new LinkedHashMapPro<Collection.Key, Object>(initialCapacity));
	else if (type == TYPE_LINKED_NOT_SYNC) map = new LinkedHashMapPro<Collection.Key, Object>(initialCapacity);
	else map = MapFactory.getConcurrentMap(initialCapacity);
    }

    @Override
    public int getType() {
	return StructUtil.getType(map);
    }

    @Override
    public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
	int type = getType();
	String subject = type == Struct.TYPE_REGULAR || type == Struct.TYPE_SYNC || type == Struct.TYPE_UNDEFINED ? "Struct" : "Struct (" + StructUtil.toType(type, "") + ")";

	return StructUtil.toDumpTable(this, subject, pageContext, maxlevel, properties);
    }

    @Override
    public Object get(Collection.Key key, Object defaultValue) {
	Object val = map.g(key, CollectionUtil.NULL);
	if (val == CollectionUtil.NULL) return defaultValue;
	if (val == null && !NullSupportHelper.full()) return defaultValue;
	return val;
    }

    public Object g(Collection.Key key, Object defaultValue) {
	return map.g(key, defaultValue);
    }

    public Object g(Collection.Key key) throws PageException {
	return map.g(key);
    }

    private static int count2 = 0;

    @Override
    public Object get(Collection.Key key) throws PageException {
	Object val = map.g(key);
	if (val != null) return val;
	if (NullSupportHelper.full()) return val;
	throw StructSupport.invalidKey(null, this, key, null);
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
	    MapPro<Key, Object> old = map;
	    try {
		map = new lucee.commons.collection.SyncMap(map);
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

	Object val = map.r(key);
	if (val != null || NullSupportHelper.full()) return val;

	throw new ExpressionException("can't remove key [" + key + "] from struct, key value is NULL what is equal do not existing in case full null support is not enabled");
    }

    @Override
    public Object removeEL(Collection.Key key) {
	return map.remove(key);
    }

    @Override
    public Object remove(Collection.Key key, Object defaultValue) {
	Object val = map.r(key, CollectionUtil.NULL);
	if (val == CollectionUtil.NULL) return defaultValue;
	if (val == null && !NullSupportHelper.full()) return defaultValue;
	return val;
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
    public boolean containsValue(Object value) {
	return map.containsValue(value);
    }

    @Override
    public java.util.Collection<Object> values() {
	return map.values();
    }

    @Override
    public int hashCode() {
	return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	return map.equals(obj);
    }
}