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
/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package lucee.commons.collection;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;

public class HashMapPro<K, V> extends AbstractMapPro<K, V> implements Map<K, V>, MapPro<K, V>, Cloneable, Serializable {
    private static final long serialVersionUID = 362498820763181265L;

    /**
     * The default initial capacity - MUST be a power of two.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 32;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the
     * constructors with arguments. MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    transient Entry<K, V>[] table;

    /**
     * The number of key-value mappings contained in this map.
     */
    transient int size;

    /**
     * The next size value at which to resize (capacity * load factor).
     * 
     * @serial
     */
    int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    final float loadFactor;

    /**
     * The number of times this HashMap has been structurally modified Structural modifications are
     * those that change the number of mappings in the HashMap or otherwise modify its internal
     * structure (e.g., rehash). This field is used to make iterators on Collection-views of the HashMap
     * fail-fast. (See ConcurrentModificationException).
     */
    transient int modCount;

    /**
     * The default threshold of map capacity above which alternative hashing is used for String keys.
     * Alternative hashing reduces the incidence of collisions due to weak hash code calculation for
     * String keys.
     * <p/>
     * This value may be overridden by defining the system property
     * {@code jdk.map.althashing.threshold}. A property value of {@code 1} forces alternative hashing to
     * be used at all times whereas {@code -1} value ensures that alternative hashing is never used.
     */
    static final int ALTERNATIVE_HASHING_THRESHOLD_DEFAULT = Integer.MAX_VALUE;

    /**
     * holds values which can't be initialized until after VM is booted.
     */
    private static class Holder {

	// Unsafe mechanics
	/**
	 * Unsafe utilities
	 */
	static final sun.misc.Unsafe UNSAFE;

	/**
	 * Offset of "final" hashSeed field we must set in readObject() method.
	 */
	static final long HASHSEED_OFFSET;

	/**
	 * Table capacity above which to switch to use alternative hashing.
	 */
	static final int ALTERNATIVE_HASHING_THRESHOLD;

	static {
	    String altThreshold = java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("jdk.map.althashing.threshold"));

	    int threshold;
	    try {
		threshold = (null != altThreshold) ? Integer.parseInt(altThreshold) : ALTERNATIVE_HASHING_THRESHOLD_DEFAULT;

		// disable alternative hashing if -1
		if (threshold == -1) {
		    threshold = Integer.MAX_VALUE;
		}

		if (threshold < 0) {
		    throw new IllegalArgumentException("value must be positive integer.");
		}
	    }
	    catch (IllegalArgumentException failed) {
		throw new Error("Illegal value for 'jdk.map.althashing.threshold'", failed);
	    }
	    ALTERNATIVE_HASHING_THRESHOLD = threshold;

	    try {
		UNSAFE = sun.misc.Unsafe.getUnsafe();
		HASHSEED_OFFSET = UNSAFE.objectFieldOffset(HashMapPro.class.getDeclaredField("hashSeed"));
	    }
	    catch (NoSuchFieldException e) {
		throw new Error("Failed to record hashSeed offset", e);
	    }
	    catch (SecurityException e) {
		throw new Error("Failed to record hashSeed offset", e);
	    }
	}
    }

    /**
     * A randomizing value associated with this instance that is applied to hash code of keys to make
     * hash collisions harder to find.
     */
    transient final int hashSeed = Hashing.randomHashSeed(this);

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and load factor.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor the load factor
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor is
     *             nonpositive
     */
    public HashMapPro(int initialCapacity, float loadFactor) {
	if (initialCapacity < 0) throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
	if (initialCapacity > MAXIMUM_CAPACITY) initialCapacity = MAXIMUM_CAPACITY;
	if (loadFactor <= 0 || Float.isNaN(loadFactor)) throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

	// Find a power of 2 >= initialCapacity
	int capacity = 1;
	while (capacity < initialCapacity)
	    capacity <<= 1;

	this.loadFactor = loadFactor;
	threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
	table = new Entry[capacity];
	// useAltHashing = sun.misc.VM.isBooted() && (capacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);
	init();
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and the default load
     * factor (0.75).
     *
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public HashMapPro(int initialCapacity) {
	this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default load
     * factor (0.75).
     */
    public HashMapPro() {
	this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new <tt>HashMap</tt> with the same mappings as the specified <tt>Map</tt>. The
     * <tt>HashMap</tt> is created with default load factor (0.75) and an initial capacity sufficient to
     * hold the mappings in the specified <tt>Map</tt>.
     *
     * @param m the map whose mappings are to be placed in this map
     * @throws NullPointerException if the specified map is null
     */
    public HashMapPro(Map<? extends K, ? extends V> m) {
	this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
	putAllForCreate(m);
    }

    // internal utilities

    /**
     * Initialization hook for subclasses. This method is called in all constructors and
     * pseudo-constructors (clone, readObject) after HashMap has been initialized but before any entries
     * have been inserted. (In the absence of this method, readObject would require explicit knowledge
     * of subclasses.)
     */
    void init() {}

    /**
     * Retrieve object hash code and applies a supplemental hash function to the result hash, which
     * defends against poor quality hash functions. This is critical because HashMap uses power-of-two
     * length hash tables, that otherwise encounter collisions for hashCodes that do not differ in lower
     * bits. Note: Null keys always map to hash 0, thus index 0.
     */
    final static int hash(Object k) {
	if (k instanceof KeyImpl) return ((KeyImpl) k).slotForMap();
	int h = 0;
	/*
	 * if (useAltHashing) { if (k instanceof String) { return Hashing.stringHash32((String) k); } h =
	 * hashSeed; }
	 */

	h ^= k.hashCode();

	// This function ensures that hashCodes that differ only by
	// constant multiples at each bit position have a bounded
	// number of collisions (approximately 8 at default load factor).
	h ^= (h >>> 20) ^ (h >>> 12);
	return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Returns index for hash code h.
     */
    static int indexFor(int h, int length) {
	return h & (length - 1);
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
	return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
	return size == 0;
    }

    @Override
    public V get(Object key) {
	if (key == null) return getForNullKey();
	Entry<K, V> entry = getEntry(key);

	return null == entry ? null : entry.getValue();
    }

    @Override
    public V g(K key, V defaultValue) {
	if (key == null) return getForNullKey();

	int hash = hash(key);
	for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
	    if (e.hash == hash && ((e.key) == key || key.equals(e.key))) return e.getValue();
	}

	return defaultValue;
    }

    /*
     * public static void main(String[] args) {
     * 
     * //HashMapPro<Key, Object> map=new HashMapPro<Key, Object>(); long
     * startx=System.currentTimeMillis(); for(int i=0;i<10000000;i++){ KeyImpl.init("K"+i); }
     * aprint.e("init.key:"+(System.currentTimeMillis()-startx));
     * 
     * 
     * 
     * HashMapPro<Key,Object> map=new HashMapPro<Key,Object>(); Map<Key,Object> sm =
     * Collections.synchronizedMap(map); ConcurrentHashMapPro<Key,Object> map2=new
     * ConcurrentHashMapPro<Key,Object>(); HashMap<Key,Object> hm=new HashMap<Key,Object>();
     * 
     * Key[] keys=new Key[100]; for(int i=0;i<100;i++){ keys[i]=KeyImpl.init("K"+i); map.put(keys[i],
     * ""+i); map2.put(keys[i], ""+i); hm.put(keys[i], ""+i); }
     * 
     * for(int i=0;i<100;i++){ keys[i]=KeyImpl.init("K"+i); }
     * 
     * long start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map.g(keys[37],null);
     * map.get(keys[37]); //k.hashCode(); } aprint.e("HM.get:"+(System.currentTimeMillis()-start));
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ map.g(keys[37],null);
     * //map.get(keys[37]); //k.hashCode(); } aprint.e("HM.g:"+(System.currentTimeMillis()-start));
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map.g(keys[37],null);
     * map.get(keys[37]); //k.hashCode(); } aprint.e("HM.get:"+(System.currentTimeMillis()-start));
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map.g(keys[37],null);
     * map.put(keys[37], ""); //map.get(keys[37]); //k.hashCode(); }
     * aprint.e("HM.put:"+(System.currentTimeMillis()-start));
     * 
     * 
     * /////////////////////////////////////////
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map.g(keys[37],null);
     * map2.get(keys[37]); //k.hashCode(); } aprint.e("CHM.get:"+(System.currentTimeMillis()-start));
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ map2.g(keys[37],null);
     * //map.get(keys[37]); //k.hashCode(); } aprint.e("CHM.g:"+(System.currentTimeMillis()-start));
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map.g(keys[37],null);
     * map2.get(keys[37]); //k.hashCode(); } aprint.e("CHM.get:"+(System.currentTimeMillis()-start));
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map2.g(keys[37],null);
     * map2.put(keys[37], ""); //map.get(keys[37]); //k.hashCode(); }
     * aprint.e("CHM.put:"+(System.currentTimeMillis()-start));
     * 
     * 
     * /////////////////////////////////////////
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map.g(keys[37],null);
     * sm.get(keys[37]); //k.hashCode(); } aprint.e("SM.get:"+(System.currentTimeMillis()-start));
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map2.g(keys[37],null);
     * sm.put(keys[37], ""); //map.get(keys[37]); //k.hashCode(); }
     * aprint.e("SM.put:"+(System.currentTimeMillis()-start));
     * 
     * 
     * /////////////////////////////////////////
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map.g(keys[37],null);
     * hm.get(keys[37]); //k.hashCode(); } aprint.e("SM.get:"+(System.currentTimeMillis()-start));
     * 
     * 
     * start=System.currentTimeMillis(); for(int i=0;i<100000000;i++){ //map2.g(keys[37],null);
     * hm.put(keys[37], ""); //map.get(keys[37]); //k.hashCode(); }
     * aprint.e("SM.put:"+(System.currentTimeMillis()-start)); }
     */

    @Override
    public V g(K key) throws PageException {
	if (key == null) return getForNullKey();

	int hash = hash(key);
	for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
	    Object k;
	    if (e.hash == hash && ((k = e.key) == key || key.equals(k))) return e.getValue();
	}
	throw invalidKey(this, key, false);
    }

    /**
     * Offloaded version of get() to look up null keys. Null keys map to index 0. This null case is
     * split out into separate methods for the sake of performance in the two most commonly used
     * operations (get and put), but incorporated with conditionals in others.
     */
    private V getForNullKey() {
	for (Entry<K, V> e = table[0]; e != null; e = e.next) {
	    if (e.key == null) return e.value;
	}
	return null;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     *
     * @param key The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     */
    @Override
    public boolean containsKey(Object key) {
	return getEntry(key) != null;
    }

    /**
     * Returns the entry associated with the specified key in the HashMap. Returns null if the HashMap
     * contains no mapping for the key.
     */
    final Entry<K, V> getEntry(Object key) {
	int hash = (key == null) ? 0 : hash(key);
	for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
	    if (e.hash == hash && ((e.key) == key || (key != null && key.equals(e.key)))) return e;
	}
	return null;
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously
     * contained a mapping for the key, the old value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping
     *         for <tt>key</tt>. (A <tt>null</tt> return can also indicate that the map previously
     *         associated <tt>null</tt> with <tt>key</tt>.)
     */
    @Override
    public V put(K key, V value) {
	if (key == null) return putForNullKey(value);
	int hash = hash(key);
	int i = indexFor(hash, table.length);
	for (Entry<K, V> e = table[i]; e != null; e = e.next) {
	    Object k;
	    if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
		V oldValue = e.value;
		e.value = value;
		e.recordAccess(this);
		return oldValue;
	    }
	}

	modCount++;
	addEntry(hash, key, value, i);
	return null;
    }

    /**
     * Offloaded version of put for null keys
     */
    private V putForNullKey(V value) {
	for (Entry<K, V> e = table[0]; e != null; e = e.next) {
	    if (e.key == null) {
		V oldValue = e.value;
		e.value = value;
		e.recordAccess(this);
		return oldValue;
	    }
	}
	modCount++;
	addEntry(0, null, value, 0);
	return null;
    }

    /**
     * This method is used instead of put by constructors and pseudoconstructors (clone, readObject). It
     * does not resize the table, check for comodification, etc. It calls createEntry rather than
     * addEntry.
     */
    private void putForCreate(K key, V value) {
	int hash = null == key ? 0 : hash(key);
	int i = indexFor(hash, table.length);

	/**
	 * Look for preexisting entry for key. This will never happen for clone or deserialize. It will only
	 * happen for construction if the input Map is a sorted map whose ordering is inconsistent w/
	 * equals.
	 */
	for (Entry<K, V> e = table[i]; e != null; e = e.next) {
	    Object k;
	    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
		e.value = value;
		return;
	    }
	}

	createEntry(hash, key, value, i);
    }

    private void putAllForCreate(Map<? extends K, ? extends V> m) {
	for (Map.Entry<? extends K, ? extends V> e: m.entrySet())
	    putForCreate(e.getKey(), e.getValue());
    }

    /**
     * Rehashes the contents of this map into a new array with a larger capacity. This method is called
     * automatically when the number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets threshold
     * to Integer.MAX_VALUE. This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two; must be greater than current
     *            capacity unless current capacity is MAXIMUM_CAPACITY (in which case value is
     *            irrelevant).
     */
    void resize(int newCapacity) {
	Entry<K, V>[] oldTable = table;
	int oldCapacity = oldTable.length;
	if (oldCapacity == MAXIMUM_CAPACITY) {
	    threshold = Integer.MAX_VALUE;
	    return;
	}

	Entry<K, V>[] newTable = new Entry[newCapacity];
	// boolean oldAltHashing = useAltHashing;
	// useAltHashing |= sun.misc.VM.isBooted() && (newCapacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);
	// boolean rehash = oldAltHashing ^ useAltHashing;
	transfer(newTable);
	table = newTable;
	threshold = (int) Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    void transfer(Entry<K, V>[] newTable) {
	int newCapacity = newTable.length;
	for (Entry<K, V> e: table) {
	    while (null != e) {
		Entry<K, V> next = e.next;
		/*
		 * if (rehash) { e.hash = null == e.key ? 0 : hash(e.key); }
		 */
		int i = indexFor(e.hash, newCapacity);
		e.next = newTable[i];
		newTable[i] = e;
		e = next;
	    }
	}
    }

    /**
     * Copies all of the mappings from the specified map to this map. These mappings will replace any
     * mappings that this map had for any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
	int numKeysToBeAdded = m.size();
	if (numKeysToBeAdded == 0) return;

	/*
	 * Expand the map if the map if the number of mappings to be added is greater than or equal to
	 * threshold. This is conservative; the obvious condition is (m.size() + size) >= threshold, but
	 * this condition could result in a map with twice the appropriate capacity, if the keys to be added
	 * overlap with the keys already in this map. By using the conservative calculation, we subject
	 * ourself to at most one extra resize.
	 */
	if (numKeysToBeAdded > threshold) {
	    int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
	    if (targetCapacity > MAXIMUM_CAPACITY) targetCapacity = MAXIMUM_CAPACITY;
	    int newCapacity = table.length;
	    while (newCapacity < targetCapacity)
		newCapacity <<= 1;
	    if (newCapacity > table.length) resize(newCapacity);
	}

	for (Map.Entry<? extends K, ? extends V> e: m.entrySet())
	    put(e.getKey(), e.getValue());
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping
     *         for <tt>key</tt>. (A <tt>null</tt> return can also indicate that the map previously
     *         associated <tt>null</tt> with <tt>key</tt>.)
     */
    @Override
    public V remove(Object key) {
	Entry<K, V> e = removeEntryForKey(key);
	return (e == null ? null : e.value);
    }

    @Override
    public V r(K key, V defaultValue) {
	Entry<K, V> e = removeEntryForKey(key);
	return (e == null ? defaultValue : e.value);
    }

    @Override
    public V r(K key) throws PageException {
	Entry<K, V> e = removeEntryForKey(key);
	if (e == null) throw invalidKey(this, key, true);
	return e.value;
    }

    /**
     * Removes and returns the entry associated with the specified key in the HashMap. Returns null if
     * the HashMap contains no mapping for this key.
     */
    final Entry<K, V> removeEntryForKey(Object key) {
	int hash = (key == null) ? 0 : hash(key);
	int i = indexFor(hash, table.length);
	Entry<K, V> prev = table[i];
	Entry<K, V> e = prev;

	while (e != null) {
	    Entry<K, V> next = e.next;
	    Object k;
	    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
		modCount++;
		size--;
		if (prev == e) table[i] = next;
		else prev.next = next;
		e.recordRemoval(this);
		return e;
	    }
	    prev = e;
	    e = next;
	}

	return e;
    }

    /**
     * Special version of remove for EntrySet using {@code Map.Entry.equals()} for matching.
     */
    final Entry<K, V> removeMapping(Object o) {
	if (!(o instanceof Map.Entry)) return null;

	Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
	Object key = entry.getKey();
	int hash = (key == null) ? 0 : hash(key);
	int i = indexFor(hash, table.length);
	Entry<K, V> prev = table[i];
	Entry<K, V> e = prev;

	while (e != null) {
	    Entry<K, V> next = e.next;
	    if (e.hash == hash && e.equals(entry)) {
		modCount++;
		size--;
		if (prev == e) table[i] = next;
		else prev.next = next;
		e.recordRemoval(this);
		return e;
	    }
	    prev = e;
	    e = next;
	}

	return e;
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    @Override
    public void clear() {
	modCount++;
	Entry[] tab = table;
	for (int i = 0; i < tab.length; i++)
	    tab[i] = null;
	size = 0;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the specified value
     */
    @Override
    public boolean containsValue(Object value) {
	if (value == null) return containsNullValue();

	Entry[] tab = table;
	for (int i = 0; i < tab.length; i++)
	    for (Entry e = tab[i]; e != null; e = e.next)
		if (value.equals(e.value)) return true;
	return false;
    }

    /**
     * Special-case code for containsValue with null argument
     */
    private boolean containsNullValue() {
	Entry[] tab = table;
	for (int i = 0; i < tab.length; i++)
	    for (Entry e = tab[i]; e != null; e = e.next)
		if (e.value == null) return true;
	return false;
    }

    /**
     * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and values themselves are not
     * cloned.
     *
     * @return a shallow copy of this map
     */
    @Override
    public Object clone() {
	HashMapPro<K, V> result = null;
	try {
	    result = (HashMapPro<K, V>) super.clone();
	}
	catch (CloneNotSupportedException e) {
	    // assert false;
	}
	result.table = new Entry[table.length];
	result.entrySet = null;
	result.modCount = 0;
	result.size = 0;
	result.init();
	result.putAllForCreate(this);

	return result;
    }

    static class Entry<K, V> implements Map.Entry<K, V> {
	final K key;
	V value;
	Entry<K, V> next;
	int hash;

	/**
	 * Creates new entry.
	 */
	Entry(int h, K k, V v, Entry<K, V> n) {
	    value = v;
	    next = n;
	    key = k;
	    hash = h;
	}

	@Override
	public final K getKey() {
	    return key;
	}

	@Override
	public final V getValue() {
	    return value;
	}

	@Override
	public final V setValue(V newValue) {
	    V oldValue = value;
	    value = newValue;
	    return oldValue;
	}

	@Override
	public final boolean equals(Object o) {
	    if (!(o instanceof Map.Entry)) return false;
	    Map.Entry e = (Map.Entry) o;
	    Object k1 = getKey();
	    Object k2 = e.getKey();
	    if (k1 == k2 || (k1 != null && k1.equals(k2))) {
		Object v1 = getValue();
		Object v2 = e.getValue();
		if (v1 == v2 || (v1 != null && v1.equals(v2))) return true;
	    }
	    return false;
	}

	@Override
	public final int hashCode() {
	    return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
	}

	@Override
	public final String toString() {
	    return getKey() + "=" + getValue();
	}

	/**
	 * This method is invoked whenever the value in an entry is overwritten by an invocation of put(k,v)
	 * for a key k that's already in the HashMap.
	 */
	void recordAccess(HashMapPro<K, V> m) {}

	/**
	 * This method is invoked whenever the entry is removed from the table.
	 */
	void recordRemoval(HashMapPro<K, V> m) {}
    }

    /**
     * Adds a new entry with the specified key, value and hash code to the specified bucket. It is the
     * responsibility of this method to resize the table if appropriate.
     *
     * Subclass overrides this to alter the behavior of put method.
     */
    void addEntry(int hash, K key, V value, int bucketIndex) {
	if ((size >= threshold) && (null != table[bucketIndex])) {
	    resize(2 * table.length);
	    hash = (null != key) ? hash(key) : 0;
	    bucketIndex = indexFor(hash, table.length);
	}

	createEntry(hash, key, value, bucketIndex);
    }

    /**
     * Like addEntry except that this version is used when creating entries as part of Map construction
     * or "pseudo-construction" (cloning, deserialization). This version needn't worry about resizing
     * the table.
     *
     * Subclass overrides this to alter the behavior of HashMap(Map), clone, and readObject.
     */
    void createEntry(int hash, K key, V value, int bucketIndex) {
	Entry<K, V> e = table[bucketIndex];
	table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
	size++;
    }

    private abstract class HashIterator<E> implements Iterator<E> {
	Entry<K, V> next; // next entry to return
	int expectedModCount; // For fast-fail
	int index; // current slot
	Entry<K, V> current; // current entry

	HashIterator() {
	    expectedModCount = modCount;
	    if (size > 0) { // advance to first entry
		Entry[] t = table;
		while (index < t.length && (next = t[index++]) == null)
		    ;
	    }
	}

	@Override
	public final boolean hasNext() {
	    return next != null;
	}

	final Entry<K, V> nextEntry() {
	    if (modCount != expectedModCount) throw new ConcurrentModificationException();
	    Entry<K, V> e = next;
	    if (e == null) throw new NoSuchElementException();

	    if ((next = e.next) == null) {
		Entry[] t = table;
		while (index < t.length && (next = t[index++]) == null)
		    ;
	    }
	    current = e;
	    return e;
	}

	@Override
	public void remove() {
	    if (current == null) throw new IllegalStateException();
	    if (modCount != expectedModCount) throw new ConcurrentModificationException();
	    Object k = current.key;
	    current = null;
	    HashMapPro.this.removeEntryForKey(k);
	    expectedModCount = modCount;
	}
    }

    private final class ValueIterator extends HashIterator<V> {
	@Override
	public V next() {
	    return nextEntry().value;
	}
    }

    private final class KeyIterator extends HashIterator<K> {
	@Override
	public K next() {
	    return nextEntry().getKey();
	}
    }

    private final class EntryIterator extends HashIterator<Map.Entry<K, V>> {
	@Override
	public Map.Entry<K, V> next() {
	    return nextEntry();
	}
    }

    // Subclass overrides these to alter behavior of views' iterator() method
    Iterator<K> newKeyIterator() {
	return new KeyIterator();
    }

    Iterator<V> newValueIterator() {
	return new ValueIterator();
    }

    Iterator<Map.Entry<K, V>> newEntryIterator() {
	return new EntryIterator();
    }

    // Views

    private transient Set<Map.Entry<K, V>> entrySet = null;

    /**
     * Returns a {@link Set} view of the keys contained in this map. The set is backed by the map, so
     * changes to the map are reflected in the set, and vice-versa. If the map is modified while an
     * iteration over the set is in progress (except through the iterator's own <tt>remove</tt>
     * operation), the results of the iteration are undefined. The set supports element removal, which
     * removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It
     * does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     */
    @Override
    public Set<K> keySet() {
	Set<K> ks = keySet;
	return (ks != null ? ks : (keySet = new KeySet()));
    }

    private final class KeySet extends AbstractSet<K> {
	@Override
	public Iterator<K> iterator() {
	    return newKeyIterator();
	}

	@Override
	public int size() {
	    return size;
	}

	@Override
	public boolean contains(Object o) {
	    return containsKey(o);
	}

	@Override
	public boolean remove(Object o) {
	    return HashMapPro.this.removeEntryForKey(o) != null;
	}

	@Override
	public void clear() {
	    HashMapPro.this.clear();
	}
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map. The collection is backed
     * by the map, so changes to the map are reflected in the collection, and vice-versa. If the map is
     * modified while an iteration over the collection is in progress (except through the iterator's own
     * <tt>remove</tt> operation), the results of the iteration are undefined. The collection supports
     * element removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     */
    @Override
    public Collection<V> values() {
	Collection<V> vs = values;
	return (vs != null ? vs : (values = new Values()));
    }

    private final class Values extends AbstractCollection<V> {
	@Override
	public Iterator<V> iterator() {
	    return newValueIterator();
	}

	@Override
	public int size() {
	    return size;
	}

	@Override
	public boolean contains(Object o) {
	    return containsValue(o);
	}

	@Override
	public void clear() {
	    HashMapPro.this.clear();
	}
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set is backed by the map,
     * so changes to the map are reflected in the set, and vice-versa. If the map is modified while an
     * iteration over the set is in progress (except through the iterator's own <tt>remove</tt>
     * operation, or through the <tt>setValue</tt> operation on a map entry returned by the iterator)
     * the results of the iteration are undefined. The set supports element removal, which removes the
     * corresponding mapping from the map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
	return entrySet0();
    }

    private Set<Map.Entry<K, V>> entrySet0() {
	Set<Map.Entry<K, V>> es = entrySet;
	return es != null ? es : (entrySet = new EntrySet());
    }

    private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
	    return newEntryIterator();
	}

	@Override
	public boolean contains(Object o) {
	    if (!(o instanceof Map.Entry)) return false;
	    Map.Entry<K, V> e = (Map.Entry<K, V>) o;
	    Entry<K, V> candidate = getEntry(e.getKey());
	    return candidate != null && candidate.equals(e);
	}

	@Override
	public boolean remove(Object o) {
	    return removeMapping(o) != null;
	}

	@Override
	public int size() {
	    return size;
	}

	@Override
	public void clear() {
	    HashMapPro.this.clear();
	}
    }

    /**
     * Save the state of the <tt>HashMap</tt> instance to a stream (i.e., serialize it).
     *
     * @serialData The <i>capacity</i> of the HashMap (the length of the bucket array) is emitted (int),
     *             followed by the <i>size</i> (an int, the number of key-value mappings), followed by
     *             the key (Object) and value (Object) for each key-value mapping. The key-value
     *             mappings are emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
	Iterator<Map.Entry<K, V>> i = (size > 0) ? entrySet0().iterator() : null;

	// Write out the threshold, loadfactor, and any hidden stuff
	s.defaultWriteObject();

	// Write out number of buckets
	s.writeInt(table.length);

	// Write out size (number of Mappings)
	s.writeInt(size);

	// Write out keys and values (alternating)
	if (size > 0) {
	    for (Map.Entry<K, V> e: entrySet0()) {
		s.writeObject(e.getKey());
		s.writeObject(e.getValue());
	    }
	}
    }

    /**
     * Reconstitute the {@code HashMap} instance from a stream (i.e., deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
	// Read in the threshold (ignored), loadfactor, and any hidden stuff
	s.defaultReadObject();
	if (loadFactor <= 0 || Float.isNaN(loadFactor)) throw new InvalidObjectException("Illegal load factor: " + loadFactor);

	// set hashSeed (can only happen after VM boot)
	Holder.UNSAFE.putIntVolatile(this, Holder.HASHSEED_OFFSET, Hashing.randomHashSeed(this));

	// Read in number of buckets and allocate the bucket array;
	s.readInt(); // ignored

	// Read number of mappings
	int mappings = s.readInt();
	if (mappings < 0) throw new InvalidObjectException("Illegal mappings count: " + mappings);

	int initialCapacity = (int) Math.min(
		// capacity chosen by number of mappings
		// and desired load (if >= 0.25)
		mappings * Math.min(1 / loadFactor, 4.0f),
		// we have limits...
		HashMapPro.MAXIMUM_CAPACITY);
	int capacity = 1;
	// find smallest power of two which holds all mappings
	while (capacity < initialCapacity) {
	    capacity <<= 1;
	}

	table = new Entry[capacity];
	threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
	// useAltHashing = sun.misc.VM.isBooted() && (capacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);

	init(); // Give subclass a chance to do its thing.

	// Read the keys and values, and put the mappings in the HashMap
	for (int i = 0; i < mappings; i++) {
	    K key = (K) s.readObject();
	    V value = (V) s.readObject();
	    putForCreate(key, value);
	}
    }

    // These methods are used when serializing HashSets
    int capacity() {
	return table.length;
    }

    float loadFactor() {
	return loadFactor;
    }

}