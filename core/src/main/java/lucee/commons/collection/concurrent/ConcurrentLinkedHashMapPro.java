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
package lucee.commons.collection.concurrent;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import lucee.commons.collection.AbstractMapPro;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;

/**
 * <p>
 * Concurrent hash map and linked list implementation of the <tt>ConcurrentMap</tt> interface, with
 * predictable iteration order. This implementation differs from <tt>ConcurrentHashMap</tt> in that
 * it maintains a doubly-linked list running through all of its entries. This linked list defines
 * the iteration ordering, which is normally the order in which keys were inserted into the map
 * (<i>insertion-order</i>). Note that insertion order is not affected if a key is
 * <i>re-inserted</i> into the map. (A key <tt>k</tt> is reinserted into a map <tt>m</tt> if
 * <tt>m.put(k, v)</tt> is invoked when <tt>m.containsKey(k)</tt> would return <tt>true</tt>
 * immediately prior to the invocation.)
 *
 * <p>
 * This implementation spares its clients from the unspecified, generally chaotic ordering provided
 * by {@link ConcurrentHashMap} (and {@link Hashtable}), without incurring the increased cost
 * associated with {@link TreeMap}. It can be used to produce a copy of a map that has the same
 * order as the original, regardless of the original map's implementation:
 * 
 * <pre>
 *     void foo(Map m) {
 *         Map copy = new ConcurrentLinkedHashMap(m);
 *         ...
 *     }
 * </pre>
 * 
 * This technique is particularly useful if a module takes a map on input, copies it, and later
 * returns results whose order is determined by that of the copy. (Clients generally appreciate
 * having things returned in the same order they were presented.)
 *
 * <p>
 * A special {@link #ConcurrentLinkedHashMap(int,float,int, int,{@link EvictionPolicy}) constructor}
 * is provided to create a concurrent linked hash map whose order of iteration is the order
 * designated by the relevant eviction policy class. Invoking the <tt>put</tt> or <tt>get</tt>
 * method results in an access to the corresponding entry (assuming it exists after the invocation
 * completes). The <tt>putAll</tt> method generates one entry access for each mapping in the
 * specified map, in the order that key-value mappings are provided by the specified map's entry set
 * iterator. <i>No other methods generate entry accesses.</i> In particular, operations on
 * collection-views do <i>not</i> affect the order of iteration of the backing map.
 *
 * <p>
 * The {@link #removeEldestEntry(Map.Entry)} method may be overridden to impose a policy for
 * removing stale mappings automatically when new mappings are added to the map.
 *
 * Performance is likely to be just slightly below that of <tt>ComcurrentHashMap</tt>, due to the
 * added expense of maintaining the linked list, with one exception: Iteration over the
 * collection-views of a <tt>ConcurrentLinkedHashMap</tt> requires time proportional to the
 * <i>size</i> of the map, regardless of its capacity. Iteration over a <tt>ConcurrentHashMap</tt>
 * is likely to be more expensive, requiring time proportional to its <i>capacity</i>.
 *
 *
 * @author Justin Cater - Original code by Doug Lea
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 */
public class ConcurrentLinkedHashMapPro<K, V> extends AbstractMapPro<K, V> implements ConcurrentMap<K, V>, Serializable {

    private static final long serialVersionUID = -6894959298396386516L;

    /*
     * The basic strategy is to subdivide the table among Segments, each of which itself is a
     * concurrently readable hash table.
     */

    /* ---------------- Constants -------------- */

    /**
     * The default initial capacity for this table, used when not otherwise specified in a constructor.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The default load factor for this table, used when not otherwise specified in a constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The default concurrency level for this table, used when not otherwise specified in a constructor.
     */
    static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the
     * constructors with arguments. MUST be a power of two <= 1<<30 to ensure that entries are indexable
     * using ints.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The maximum number of segments to allow; used to bound constructor arguments.
     */
    static final int MAX_SEGMENTS = 1 << 16; // slightly conservative

    /**
     * Number of unsynchronized retries in size and containsValue methods before resorting to locking.
     * This is used to avoid unbounded retries if tables undergo continuous modification which would
     * make it impossible to obtain an accurate result.
     */
    static final int RETRIES_BEFORE_LOCK = 2;

    /**
     * The maxSize attribute defines the maximum number of name/value pairs the map will hold. The
     * Integer.MAX_VALUE mark disables this upper bound limit.
     */
    static final int UNLIMITED_SIZE = Integer.MAX_VALUE;

    /* ---------------- Fields -------------- */

    /**
     * Mask value for indexing into segments. The upper bits of a key's hash code are used to choose the
     * segment.
     */
    final int segmentMask;

    /**
     * Shift value for indexing within segments.
     */
    final int segmentShift;

    /**
     * The segments, each of which is a specialized hash table
     */
    final Segment<K, V>[] segments;

    /**
     * The eviction policy to be used
     */
    final EvictionPolicy evictionPolicy;

    /**
     * The maxSize attribute defines the maximum number of name/value pairs the map will hold. The
     * UNLIMITED_SIZE mark disables this upper bound limit.
     */
    final int maxSize;

    /**
     * The head of the doubly linked list.
     */
    transient HashEntry<K, V> header;

    /**
     * The lock for atomic access to the doubly linked list.
     */
    transient ReentrantLock modifyListLock;

    transient Set<K> keySet;
    transient Set<Map.Entry<K, V>> entrySet;
    transient Collection<V> values;

    /* ---------------- Small Utilities -------------- */

    /**
     * Applies a supplemental hash function to a given hashCode, which defends against poor quality hash
     * functions. This is critical because ConcurrentHashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ in lower or upper bits.
     */
    private static int hash(Object k) {
	if (k instanceof KeyImpl) return ((KeyImpl) k).wangJenkinsHash();
	// Spread bits to regularize both segment and index locations,
	// using variant of single-word Wang/Jenkins hash.
	int h = k.hashCode();

	h += (h << 15) ^ 0xffffcd7d;
	h ^= (h >>> 10);
	h += (h << 3);
	h ^= (h >>> 6);
	h += (h << 2) + (h << 14);
	return h ^ (h >>> 16);
    }

    /**
     * Returns the segment that should be used for key with given hash
     * 
     * @param hash the hash code for the key
     * @return the segment
     */
    final Segment<K, V> segmentFor(int hash) {
	return segments[(hash >>> segmentShift) & segmentMask];
    }

    /* ---------------- Inner Classes -------------- */

    /**
     * ConcurrentHashMap list entry. Note that this is never exported out as a user-visible Map.Entry.
     *
     * Because the value field is volatile, not final, it is legal wrt the Java Memory Model for an
     * unsynchronized reader to see null instead of initial value when read via a data race. Although a
     * reordering leading to this is not likely to ever actually occur, the Segment.readValueUnderLock
     * method is used as a backup in case a null (pre-initialized) value is ever seen in an
     * unsynchronized access method.
     */
    static final class HashEntry<K, V> implements Entry<K, V> {
	final K key;
	final int hash;
	volatile V value;
	HashEntry<K, V> next;
	HashEntry<K, V> after;
	HashEntry<K, V> before;
	long accessCount;
	final long creationTime;
	long lastAccessedTime;
	ReentrantLock modifyListLock;
	AtomicInteger cloneAllFlag;

	HashEntry(K key, int hash, HashEntry<K, V> next, V value, long accessCount, long creationTime, long lastAccessedTime) {
	    this.key = key;
	    this.hash = hash;
	    this.next = next;
	    this.value = value;
	    this.accessCount = accessCount;
	    this.creationTime = creationTime;
	    this.lastAccessedTime = lastAccessedTime;
	}

	@SuppressWarnings("unchecked")
	static final <K, V> HashEntry<K, V>[] newArray(int i) {
	    return new HashEntry[i];
	}

	/**
	 * Removes this entry from the linked list.
	 */
	public void remove() {
	    before.after = after;
	    after.before = before;
	}

	/**
	 * Inserts this entry before the specified existing entry in the list.
	 */
	public void addBefore(HashEntry<K, V> existingEntry) {
	    after = existingEntry;
	    before = existingEntry.before;
	    before.after = this;
	    after.before = this;
	}

	/**
	 * This method is invoked by the superclass whenever the value of a pre-existing entry is read by
	 * Map.get or modified by Map.set. If the enclosing Map is access-ordered, it moves the entry to the
	 * end of the list; otherwise, it does nothing.
	 */
	void recordAccess(HashEntry<K, V> header, EvictionPolicy evictionPolicy) {
	    waitForModifyPermition(header);
	    remove();
	    addBefore((HashEntry<K, V>) evictionPolicy.recordAccess(header, this));
	    accessCount++;
	    lastAccessedTime = System.currentTimeMillis();
	    grandModifyAndCloneAllPermition(header);
	}

	/**
	 * This method is invoked by the superclass whenever a new entry is inserted by Map.put
	 */
	void recordInsertion(HashEntry<K, V> header, EvictionPolicy evictionPolicy) {
	    waitForModifyPermition(header);
	    addBefore((HashEntry<K, V>) evictionPolicy.recordInsertion(header, this));
	    grandModifyAndCloneAllPermition(header);
	}

	void recordRemoval(HashEntry<K, V> header) {
	    waitForModifyPermition(header);
	    remove();
	    grandModifyAndCloneAllPermition(header);
	}

	public HashEntry<K, V> clone(HashEntry<K, V> next, HashEntry<K, V> header) {
	    waitForModifyPermition(header);
	    HashEntry<K, V> nextEntry = after;
	    remove();
	    HashEntry<K, V> theClone = new HashEntry<K, V>(key, hash, next, value, accessCount, creationTime, lastAccessedTime);
	    theClone.addBefore(nextEntry);
	    grandModifyAndCloneAllPermition(header);
	    return theClone;
	}

	public HashEntry<K, V> cloneAll(HashEntry<K, V> header) {
	    waitForCloneAllPermition(header);
	    HashEntry<K, V> rootClone = new HashEntry<K, V>(key, hash, next, value, accessCount, creationTime, lastAccessedTime);
	    rootClone.before = rootClone.after = rootClone;

	    HashEntry<K, V> pointer = after;
	    while (pointer != header) {
		HashEntry<K, V> nextClone = new HashEntry<K, V>(pointer.key, pointer.hash, pointer.next, pointer.value, pointer.accessCount, pointer.creationTime,
			pointer.lastAccessedTime);
		nextClone.addBefore(rootClone);
		pointer = pointer.after;
	    }
	    grandModifyPermition(header);
	    return rootClone;
	}

	private void waitForModifyPermition(HashEntry<K, V> header) {
	    while (!checkForModifyPermition(header)) {
		try {
		    Thread.sleep(0, 1);
		}
		catch (InterruptedException e) {}
	    }
	}

	private boolean checkForModifyPermition(HashEntry<K, V> header) {
	    if (header.cloneAllFlag.getAndDecrement() <= 0) {
		header.modifyListLock.lock();
		return true;
	    }
	    header.cloneAllFlag.getAndIncrement();
	    return false;
	}

	private void grandModifyAndCloneAllPermition(HashEntry<K, V> header) {
	    header.modifyListLock.unlock();
	    header.cloneAllFlag.getAndIncrement();
	}

	private void waitForCloneAllPermition(HashEntry<K, V> header) {
	    while (!checkForCloneAllPermition(header)) {
		try {
		    Thread.sleep(0, 1);
		}
		catch (InterruptedException e) {}
	    }
	}

	private boolean checkForCloneAllPermition(HashEntry<K, V> header) {
	    if (header.cloneAllFlag.getAndIncrement() >= 0) return true;
	    grandModifyPermition(header);
	    return false;
	}

	private void grandModifyPermition(HashEntry<K, V> header) {
	    header.cloneAllFlag.getAndDecrement();
	}

	@Override
	public K getKey() {
	    return key;
	}

	@Override
	public V getValue() {
	    return value;
	}

	@Override
	public V setValue(V value) {
	    V oldValue = this.value;
	    this.value = value;
	    return oldValue;
	}

	@Override
	public Entry<K, V> getAfter() {
	    return after;
	}

	@Override
	public Entry<K, V> getBefore() {
	    return before;
	}

	@Override
	public long getAccessCount() {
	    return accessCount;
	}

	@Override
	public long getCreationTime() {
	    return creationTime;
	}

	@Override
	public long getLastAccessTime() {
	    return lastAccessedTime;
	}

    }

    /**
     * Segments are specialized versions of hash tables. This subclasses from ReentrantLock
     * opportunistically, just to simplify some locking and avoid separate construction.
     */
    static final class Segment<K, V> extends ReentrantLock implements Serializable {
	/*
	 * Segments maintain a table of entry lists that are ALWAYS kept in a consistent state, so can be
	 * read without locking. Next fields of nodes are immutable (final). All list additions are
	 * performed at the front of each bin. This makes it easy to check changes, and also fast to
	 * traverse. When nodes would otherwise be changed, new nodes are created to replace them. This
	 * works well for hash tables since the bin lists tend to be short. (The average length is less than
	 * two for the default load factor threshold.)
	 *
	 * Read operations can thus proceed without locking, but rely on selected uses of volatiles to
	 * ensure that completed write operations performed by other threads are noticed. For most purposes,
	 * the "count" field, tracking the number of elements, serves as that volatile variable ensuring
	 * visibility. This is convenient because this field needs to be read in many read operations
	 * anyway:
	 *
	 * - All (unsynchronized) read operations must first read the "count" field, and should not look at
	 * table entries if it is 0.
	 *
	 * - All (synchronized) write operations should write to the "count" field after structurally
	 * changing any bin. The operations must not take any action that could even momentarily cause a
	 * concurrent read operation to see inconsistent data. This is made easier by the nature of the read
	 * operations in Map. For example, no operation can reveal that the table has grown but the
	 * threshold has not yet been updated, so there are no atomicity requirements for this with respect
	 * to reads.
	 *
	 * As a guide, all critical volatile reads and writes to the count field are marked in code
	 * comments.
	 */

	private static final long serialVersionUID = 2249069246763182397L;

	/**
	 * The number of elements in this segment's region.
	 */
	transient volatile int count;

	/**
	 * Number of updates that alter the size of the table. This is used during bulk-read methods to make
	 * sure they see a consistent snapshot: If modCounts change during a traversal of segments computing
	 * size or checking containsValue, then we might have an inconsistent view of state so (usually)
	 * must retry.
	 */
	transient int modCount;

	/**
	 * The table is rehashed when its size exceeds this threshold. (The value of this field is always
	 * <tt>(int)(capacity *
	 * loadFactor)</tt>.)
	 */
	transient int threshold;

	/**
	 * The per-segment table.
	 */
	transient volatile HashEntry<K, V>[] table;

	/**
	 * The load factor for the hash table. Even though this value is same for all segments, it is
	 * replicated to avoid needing links to outer object.
	 * 
	 * @serial
	 */
	final float loadFactor;

	/**
	 * The eviction policy for this linked hash map. Even though this value is same for all segments, it
	 * is replicated to avoid needing links to outer object.
	 *
	 * @serial
	 */
	final EvictionPolicy evictionPolicy;

	Segment(int initialCapacity, float lf, EvictionPolicy ep) {
	    loadFactor = lf;
	    evictionPolicy = ep;
	    setTable(HashEntry.<K, V>newArray(initialCapacity));
	}

	@SuppressWarnings("unchecked")
	static final <K, V> Segment<K, V>[] newArray(int i) {
	    return new Segment[i];
	}

	/**
	 * Sets table to new HashEntry array. Call only while holding lock or in constructor.
	 */
	void setTable(HashEntry<K, V>[] newTable) {
	    threshold = (int) (newTable.length * loadFactor);
	    table = newTable;
	}

	/**
	 * Returns properly casted first entry of bin for given hash.
	 */
	HashEntry<K, V> getFirst(int hash) {
	    HashEntry<K, V>[] tab = table;
	    return tab[hash & (tab.length - 1)];
	}

	/**
	 * Reads value field of an entry under lock. Called if value field ever appears to be null. This is
	 * possible only if a compiler happens to reorder a HashEntry initialization with its table
	 * assignment, which is legal under memory model but is not known to ever occur.
	 */
	V readValueUnderLock(HashEntry<K, V> e) {
	    lock();
	    try {
		return e.value;
	    }
	    finally {
		unlock();
	    }
	}

	/* Specialized implementations of map methods */

	V get(Object key, int hash, HashEntry<K, V> header, V defaultValue) {
	    if (count != 0) { // read-volatile
		HashEntry<K, V> e = getFirst(hash);
		while (e != null) {
		    if (e.hash == hash && key.equals(e.key)) {
			V v = e.value;
			if (v != null) {
			    if (evictionPolicy.accessOrder()) e.recordAccess(header, evictionPolicy);
			    return v;
			}
			return readValueUnderLock(e); // recheck
		    }
		    e = e.next;
		}
	    }
	    return defaultValue;
	}

	boolean containsKey(Object key, int hash) {
	    if (count != 0) { // read-volatile
		HashEntry<K, V> e = getFirst(hash);
		while (e != null) {
		    if (e.hash == hash && key.equals(e.key)) return true;
		    e = e.next;
		}
	    }
	    return false;
	}

	boolean containsValue(Object value) {
	    if (count != 0) { // read-volatile
		HashEntry<K, V>[] tab = table;
		int len = tab.length;
		for (int i = 0; i < len; i++) {
		    for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
			V v = e.value;
			if (v == null) // recheck
			    v = readValueUnderLock(e);
			if (value.equals(v)) return true;
		    }
		}
	    }
	    return false;
	}

	boolean replace(K key, int hash, V oldValue, V newValue) {
	    lock();
	    try {
		HashEntry<K, V> e = getFirst(hash);
		while (e != null && (e.hash != hash || !key.equals(e.key)))
		    e = e.next;

		boolean replaced = false;
		if (e != null && oldValue.equals(e.value)) {
		    replaced = true;
		    e.value = newValue;
		}
		return replaced;
	    }
	    finally {
		unlock();
	    }
	}

	V replace(K key, int hash, V newValue) {
	    lock();
	    try {
		HashEntry<K, V> e = getFirst(hash);
		while (e != null && (e.hash != hash || !key.equals(e.key)))
		    e = e.next;

		V oldValue = null;
		if (e != null) {
		    oldValue = e.value;
		    e.value = newValue;
		}
		return oldValue;
	    }
	    finally {
		unlock();
	    }
	}

	V put(K key, int hash, V value, boolean onlyIfAbsent, HashEntry<K, V> header) {
	    lock();
	    try {

		int c = count;
		if (c++ > threshold) // ensure capacity
		    rehash(header);
		HashEntry<K, V>[] tab = table;
		int index = hash & (tab.length - 1);
		HashEntry<K, V> first = tab[index];
		HashEntry<K, V> e = first;
		while (e != null && (e.hash != hash || !key.equals(e.key)))
		    e = e.next;

		V oldValue;
		if (e != null) {
		    oldValue = e.value;
		    if (!onlyIfAbsent) e.value = value;
		}
		else {
		    oldValue = null;
		    ++modCount;
		    long now = System.currentTimeMillis();
		    e = new HashEntry<K, V>(key, hash, first, value, 1, now, now);
		    if (evictionPolicy.insertionOrder()) e.recordInsertion(header, evictionPolicy);
		    else e.addBefore(header);
		    tab[index] = e;
		    count = c; // write-volatile
		}
		return oldValue;
	    }
	    finally {
		unlock();
	    }
	}

	void rehash(HashEntry<K, V> header) {
	    HashEntry<K, V>[] oldTable = table;
	    int oldCapacity = oldTable.length;
	    if (oldCapacity >= MAXIMUM_CAPACITY) return;

	    /*
	     * Reclassify nodes in each list to new Map. Because we are using power-of-two expansion, the
	     * elements from each bin must either stay at same index, or move with a power of two offset. We
	     * eliminate unnecessary node creation by catching cases where old nodes can be reused because their
	     * next fields won't change. Statistically, at the default threshold, only about one-sixth of them
	     * need cloning when a table doubles. The nodes they replace will be garbage collectable as soon as
	     * they are no longer referenced by any reader thread that may be in the midst of traversing table
	     * right now.
	     */

	    HashEntry<K, V>[] newTable = HashEntry.newArray(oldCapacity << 1);
	    threshold = (int) (newTable.length * loadFactor);
	    int sizeMask = newTable.length - 1;
	    for (int i = 0; i < oldCapacity; i++) {
		// We need to guarantee that any existing reads of old Map can
		// proceed. So we cannot yet null out each bin.
		HashEntry<K, V> e = oldTable[i];

		if (e != null) {
		    HashEntry<K, V> next = e.next;
		    int idx = e.hash & sizeMask;

		    // Single node on list
		    if (next == null) newTable[idx] = e;

		    else {
			// Reuse trailing consecutive sequence at same slot
			HashEntry<K, V> lastRun = e;
			int lastIdx = idx;
			for (HashEntry<K, V> last = next; last != null; last = last.next) {
			    int k = last.hash & sizeMask;
			    if (k != lastIdx) {
				lastIdx = k;
				lastRun = last;
			    }
			}
			newTable[lastIdx] = lastRun;

			// Clone all remaining nodes
			for (HashEntry<K, V> p = e; p != lastRun; p = p.next) {
			    int k = p.hash & sizeMask;
			    HashEntry<K, V> n = newTable[k];
			    newTable[k] = p.clone(n, header);
			}
		    }
		}
	    }
	    table = newTable;
	}

	/**
	 * Remove; match on key only if value null, else match both.
	 */
	V remove(Object key, int hash, Object value, HashEntry<K, V> header, V defaultValue) {
	    lock();
	    try {
		int c = count - 1;
		HashEntry<K, V>[] tab = table;
		int index = hash & (tab.length - 1);
		HashEntry<K, V> first = tab[index];
		HashEntry<K, V> e = first;
		while (e != null && (e.hash != hash || !key.equals(e.key)))
		    e = e.next;

		V old = defaultValue;
		if (e != null) {
		    if (value == null || value.equals(e.value)) {
			old = e.value;
			e.recordRemoval(header);
			// All entries following removed node can stay
			// in list, but all preceding ones need to be
			// cloned.
			++modCount;
			HashEntry<K, V> newFirst = e.next;
			for (HashEntry<K, V> p = first; p != e; p = p.next)
			    newFirst = p.clone(newFirst, header);
			tab[index] = newFirst;
			count = c; // write-volatile
		    }
		}
		return old;
	    }
	    finally {
		unlock();
	    }
	}

	/**
	 * Remove; match on key only if value null, else match both.
	 * 
	 * @throws PageException
	 */
	V removeE(Map<K, V> m, Object key, int hash, Object value, HashEntry<K, V> header) throws PageException {
	    lock();
	    try {
		int c = count - 1;
		HashEntry<K, V>[] tab = table;
		int index = hash & (tab.length - 1);
		HashEntry<K, V> first = tab[index];
		HashEntry<K, V> e = first;
		while (e != null && (e.hash != hash || !key.equals(e.key)))
		    e = e.next;

		if (e != null) {
		    if (value == null || value.equals(e.value)) {
			e.recordRemoval(header);
			// All entries following removed node can stay
			// in list, but all preceding ones need to be
			// cloned.
			++modCount;
			HashEntry<K, V> newFirst = e.next;
			for (HashEntry<K, V> p = first; p != e; p = p.next)
			    newFirst = p.clone(newFirst, header);
			tab[index] = newFirst;
			count = c; // write-volatile
			return e.value;
		    }
		}
		throw AbstractMapPro.invalidKey(m, key, true);
	    }
	    finally {
		unlock();
	    }
	}

	void clear(HashEntry<K, V> header) {
	    if (count != 0) {
		lock();
		try {
		    HashEntry<K, V>[] tab = table;
		    for (int i = 0; i < tab.length; i++) {
			if (tab[i] != null) {
			    tab[i].recordRemoval(header);
			    tab[i] = null;
			}
		    }
		    ++modCount;
		    count = 0; // write-volatile
		}
		finally {
		    unlock();
		}
	    }
	}
    }

    /* ---------------- Public operations -------------- */

    /**
     * Creates a new, empty map with the specified initial capacity, load factor, concurrency level, max
     * size and eviction policy.
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to
     *            accommodate this many elements.
     * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed
     *            when the average number of elements per bin exceeds this threshold.
     * @param concurrencyLevel the estimated number of concurrently updating threads. The implementation
     *            performs internal sizing to try to accommodate this many threads.
     * @param maxSize the maximum number of name/value pairs this map will hold.
     * @param evictionPolicy the eviction policy to be used
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor or
     *             concurrencyLevel are nonpositive.
     */
    public ConcurrentLinkedHashMapPro(int initialCapacity, float loadFactor, int concurrencyLevel, int maxSize, EvictionPolicy evictionPolicy) {
	if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0) throw new IllegalArgumentException();

	this.maxSize = maxSize;
	this.evictionPolicy = evictionPolicy;

	if (concurrencyLevel > MAX_SEGMENTS) concurrencyLevel = MAX_SEGMENTS;

	// Find power-of-two sizes best matching arguments
	int sshift = 0;
	int ssize = 1;
	while (ssize < concurrencyLevel) {
	    ++sshift;
	    ssize <<= 1;
	}
	segmentShift = 32 - sshift;
	segmentMask = ssize - 1;
	this.segments = Segment.newArray(ssize);

	if (initialCapacity > MAXIMUM_CAPACITY) initialCapacity = MAXIMUM_CAPACITY;
	int c = initialCapacity / ssize;
	if (c * ssize < initialCapacity) ++c;
	int cap = 1;
	while (cap < c)
	    cap <<= 1;

	for (int i = 0; i < this.segments.length; ++i)
	    this.segments[i] = new Segment<K, V>(cap, loadFactor, evictionPolicy);

	header = new HashEntry<K, V>(null, -1, null, null, -1, -1, -1);
	header.before = header.after = header;
	header.modifyListLock = new ReentrantLock();
	header.cloneAllFlag = new AtomicInteger();
    }

    /**
     * Creates a new, empty map with the specified initial capacity and load factor and with the default
     * concurrencyLevel (16).
     *
     * @param initialCapacity The implementation performs internal sizing to accommodate this many
     *            elements.
     * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed
     *            when the average number of elements per bin exceeds this threshold.
     * @throws IllegalArgumentException if the initial capacity of elements is negative or the load
     *             factor is nonpositive
     *
     * @since 1.6
     */
    public ConcurrentLinkedHashMapPro(int initialCapacity, float loadFactor) {
	this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL, UNLIMITED_SIZE, new FIFOPolicy());
    }

    /**
     * Creates a new, empty map with the specified initial capacity, and with default load factor (0.75)
     * and concurrencyLevel (16).
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to
     *            accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity of elements is negative.
     */
    public ConcurrentLinkedHashMapPro(int initialCapacity) {
	this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, UNLIMITED_SIZE, new FIFOPolicy());
    }

    /**
     * Creates a new, empty map with a default initial capacity (16), load factor (0.75) and
     * concurrencyLevel (16).
     */
    public ConcurrentLinkedHashMapPro() {
	this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, UNLIMITED_SIZE, new FIFOPolicy());
    }

    /**
     * Creates a new map with the same mappings as the given map. The map is created with a capacity of
     * 1.5 times the number of mappings in the given map or 16 (whichever is greater), and a default
     * load factor (0.75) and concurrencyLevel (16).
     *
     * @param m the map
     */
    public ConcurrentLinkedHashMapPro(Map<? extends K, ? extends V> m) {
	this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, UNLIMITED_SIZE, new FIFOPolicy());
	putAll(m);
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
	final Segment<K, V>[] segments = this.segments;
	/*
	 * We keep track of per-segment modCounts to avoid ABA problems in which an element in one segment
	 * was added and in another removed during traversal, in which case the table was never actually
	 * empty at any point. Note the similar use of modCounts in the size() and containsValue() methods,
	 * which are the only other methods also susceptible to ABA problems.
	 */
	int[] mc = new int[segments.length];
	int mcsum = 0;
	for (int i = 0; i < segments.length; ++i) {
	    if (segments[i].count != 0) return false;
	    mcsum += mc[i] = segments[i].modCount;
	}
	// If mcsum happens to be zero, then we know we got a snapshot
	// before any modifications at all were made. This is
	// probably common enough to bother tracking.
	if (mcsum != 0) {
	    for (int i = 0; i < segments.length; ++i) {
		if (segments[i].count != 0 || mc[i] != segments[i].modCount) return false;
	    }
	}
	return true;
    }

    /**
     * Returns the number of key-value mappings in this map. If the map contains more than
     * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
	final Segment<K, V>[] segments = this.segments;
	long sum = 0;
	long check = 0;
	int[] mc = new int[segments.length];
	// Try a few times to get accurate count. On failure due to
	// continuous async changes in table, resort to locking.
	for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
	    check = 0;
	    sum = 0;
	    int mcsum = 0;
	    for (int i = 0; i < segments.length; ++i) {
		sum += segments[i].count;
		mcsum += mc[i] = segments[i].modCount;
	    }
	    if (mcsum != 0) {
		for (int i = 0; i < segments.length; ++i) {
		    check += segments[i].count;
		    if (mc[i] != segments[i].modCount) {
			check = -1; // force retry
			break;
		    }
		}
	    }
	    if (check == sum) break;
	}
	if (check != sum) { // Resort to locking all segments
	    sum = 0;
	    for (int i = 0; i < segments.length; ++i)
		segments[i].lock();
	    for (int i = 0; i < segments.length; ++i)
		sum += segments[i].count;
	    for (int i = 0; i < segments.length; ++i)
		segments[i].unlock();
	}
	if (sum > Integer.MAX_VALUE) return Integer.MAX_VALUE;
	return (int) sum;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no
     * mapping for the key.
     *
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such that
     * {@code key.equals(k)}, then this method returns {@code v}; otherwise it returns {@code null}.
     * (There can be at most one such mapping.)
     *
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V get(Object key) {
	int hash = hash(key);
	return segmentFor(hash).get(key, hash, header, null);
    }

    @Override
    public V g(K key) throws PageException {
	int hash = hash(key);
	Segment<K, V> seg = segmentFor(hash);

	if (seg.count != 0) { // read-volatile
	    HashEntry<K, V> e = seg.getFirst(hash);
	    while (e != null) {
		if (e.hash == hash && key.equals(e.key)) {
		    V v = e.value;
		    if (v != null) {
			if (evictionPolicy.accessOrder()) e.recordAccess(header, evictionPolicy);
			return v;
		    }
		    return seg.readValueUnderLock(e); // recheck
		}
		e = e.next;
	    }
	}
	throw AbstractMapPro.invalidKey(this, key, false);
    }

    @Override
    public V g(K key, V defaultValue) {
	int hash = hash(key);
	return segmentFor(hash).get(key, hash, header, defaultValue);
    }

    /**
     * Tests if the specified object is a key in this table.
     *
     * @param key possible key
     * @return <tt>true</tt> if and only if the specified object is a key in this table, as determined
     *         by the <tt>equals</tt> method; <tt>false</tt> otherwise.
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean containsKey(Object key) {
	int hash = hash(key);
	return segmentFor(hash).containsKey(key, hash);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value. Note: This method
     * requires a full internal traversal of the hash table, and so is much slower than method
     * <tt>containsKey</tt>.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the specified value
     * @throws NullPointerException if the specified value is null
     */
    @Override
    public boolean containsValue(Object value) {
	if (value == null) throw new NullPointerException();

	// See explanation of modCount use above

	final Segment<K, V>[] segments = this.segments;
	int[] mc = new int[segments.length];

	// Try a few times without locking
	for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
	    int mcsum = 0;
	    for (int i = 0; i < segments.length; ++i) {
		mcsum += mc[i] = segments[i].modCount;
		if (segments[i].containsValue(value)) return true;
	    }
	    boolean cleanSweep = true;
	    if (mcsum != 0) {
		for (int i = 0; i < segments.length; ++i) {
		    if (mc[i] != segments[i].modCount) {
			cleanSweep = false;
			break;
		    }
		}
	    }
	    if (cleanSweep) return false;
	}
	// Resort to locking all segments
	for (int i = 0; i < segments.length; ++i)
	    segments[i].lock();
	boolean found = false;
	try {
	    for (int i = 0; i < segments.length; ++i) {
		if (segments[i].containsValue(value)) {
		    found = true;
		    break;
		}
	    }
	}
	finally {
	    for (int i = 0; i < segments.length; ++i)
		segments[i].unlock();
	}
	return found;
    }

    /**
     * Legacy method testing if some key maps into the specified value in this table. This method is
     * identical in functionality to {@link #containsValue}, and exists solely to ensure full
     * compatibility with class {@link java.util.Hashtable}, which supported this method prior to
     * introduction of the Java Collections framework.
     * 
     * @param value a value to search for
     * @return <tt>true</tt> if and only if some key maps to the <tt>value</tt> argument in this table
     *         as determined by the <tt>equals</tt> method; <tt>false</tt> otherwise
     * @throws NullPointerException if the specified value is null
     */
    public boolean contains(Object value) {
	return containsValue(value);
    }

    /**
     * Maps the specified key to the specified value in this table. Neither the key nor the value can be
     * null.
     *
     * <p>
     * The value can be retrieved by calling the <tt>get</tt> method with a key that is equal to the
     * original key.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping
     *         for <tt>key</tt>
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public V put(K key, V value) {

	HashEntry<K, V> evictEntry = (HashEntry<K, V>) evictionPolicy.evictElement(header);
	if (evictEntry != null && size() >= maxSize) segmentFor(evictEntry.hash).remove(evictEntry.key, evictEntry.hash, evictEntry.value, header, null);

	int hash = hash(key);
	return segmentFor(hash).put(key, hash, value, false, header);
    }

    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key, or <tt>null</tt> if there was no
     *         mapping for the key
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public V putIfAbsent(K key, V value) {
	if (value == null) throw new NullPointerException();

	HashEntry<K, V> evictEntry = (HashEntry<K, V>) evictionPolicy.evictElement(header);
	if (evictEntry != null && size() >= maxSize) segmentFor(evictEntry.hash).remove(evictEntry.key, evictEntry.hash, evictEntry.value, header, null);

	int hash = hash(key);
	return segmentFor(hash).put(key, hash, value, true, header);
    }

    /**
     * Copies all of the mappings from the specified map to this one. These mappings replace any
     * mappings that this map had for any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
	for (Map.Entry<? extends K, ? extends V> e: m.entrySet())
	    put(e.getKey(), e.getValue());
    }

    /**
     * Removes the key (and its corresponding value) from this map. This method does nothing if the key
     * is not in the map.
     *
     * @param key the key that needs to be removed
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping
     *         for <tt>key</tt>
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V remove(Object key) {
	int hash = hash(key);
	return segmentFor(hash).remove(key, hash, null, header, null);
    }

    @Override
    public V r(K key) throws PageException {
	int hash = hash(key);
	return segmentFor(hash).removeE(this, key, hash, null, header);
    }

    @Override
    public V r(K key, V defaultValue) {
	int hash = hash(key);
	return segmentFor(hash).remove(key, hash, null, header, defaultValue);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public boolean remove(Object key, Object value) {
	int hash = hash(key);
	if (value == null) return false;
	return segmentFor(hash).remove(key, hash, value, header, null) != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if any of the arguments are null
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
	if (oldValue == null || newValue == null) throw new NullPointerException();
	int hash = hash(key);
	return segmentFor(hash).replace(key, hash, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key, or <tt>null</tt> if there was no
     *         mapping for the key
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public V replace(K key, V value) {
	if (value == null) throw new NullPointerException();
	int hash = hash(key);
	return segmentFor(hash).replace(key, hash, value);
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
	for (int i = 0; i < segments.length; ++i)
	    segments[i].clear(header);
	header.before = header.after = header;
    }

    /**
     * Returns <tt>true</tt> if this map should remove its eldest entry. This method is invoked by
     * <tt>put</tt> and <tt>putAll</tt> after inserting a new entry into the map. It provides the
     * implementor with the opportunity to remove the eldest entry each time a new one is added. This is
     * useful if the map represents a cache: it allows the map to reduce memory consumption by deleting
     * stale entries.
     *
     * <p>
     * Sample use: this override will allow the map to grow up to 100 entries and then delete the eldest
     * entry each time a new entry is added, maintaining a steady state of 100 entries.
     * 
     * <pre>
     * private static final int MAX_ENTRIES = 100;
     *
     * protected boolean removeEldestEntry(Map.Entry eldest) {
     *     return size() > MAX_ENTRIES;
     * }
     * </pre>
     *
     * <p>
     * This method typically does not modify the map in any way, instead allowing the map to modify
     * itself as directed by its return value. It <i>is</i> permitted for this method to modify the map
     * directly, but if it does so, it <i>must</i> return <tt>false</tt> (indicating that the map should
     * not attempt any further modification). The effects of returning <tt>true</tt> after modifying the
     * map from within this method are unspecified.
     *
     * <p>
     * This implementation merely returns <tt>false</tt> (so that this map acts like a normal map - the
     * eldest element is never removed).
     *
     * @param eldest The least recently inserted entry in the map, or if this is an access-ordered map,
     *            the least recently accessed entry. This is the entry that will be removed it this
     *            method returns <tt>true</tt>. If the map was empty prior to the <tt>put</tt> or
     *            <tt>putAll</tt> invocation resulting in this invocation, this will be the entry that
     *            was just inserted; in other words, if the map contains a single entry, the eldest
     *            entry is also the newest.
     * @return <tt>true</tt> if the eldest entry should be removed from the map; <tt>false</tt> if it
     *         should be retained.
     */
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	return false;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map. The set is backed by the map, so
     * changes to the map are reflected in the set, and vice-versa. The set supports element removal,
     * which removes the corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It
     * does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * <p>
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw
     * {@link ConcurrentModificationException}, and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to) reflect any modifications
     * subsequent to construction.
     */
    @Override
    public Set<K> keySet() {
	Set<K> ks = keySet;
	return (ks != null) ? ks : (keySet = new KeySet());
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map. The collection is backed
     * by the map, so changes to the map are reflected in the collection, and vice-versa. The collection
     * supports element removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * <p>
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw
     * {@link ConcurrentModificationException}, and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to) reflect any modifications
     * subsequent to construction.
     */
    @Override
    public Collection<V> values() {
	Collection<V> vs = values;
	return (vs != null) ? vs : (values = new Values());
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set is backed by the map,
     * so changes to the map are reflected in the set, and vice-versa. The set supports element removal,
     * which removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It
     * does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * <p>
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw
     * {@link ConcurrentModificationException}, and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to) reflect any modifications
     * subsequent to construction.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
	Set<Map.Entry<K, V>> es = entrySet;
	return (es != null) ? es : (entrySet = new EntrySet());
    }

    /**
     * Returns an enumeration of the keys in this table.
     *
     * @return an enumeration of the keys in this table
     * @see #keySet()
     */
    public Enumeration<K> keys() {
	return new KeyIterator();
    }

    /**
     * Returns an enumeration of the values in this table.
     *
     * @return an enumeration of the values in this table
     * @see #values()
     */
    public Enumeration<V> elements() {
	return new ValueIterator();
    }

    /* ---------------- Iterator Support -------------- */

    abstract class HashIterator {
	HashEntry<K, V> nextEntry = null;
	HashEntry<K, V> lastReturned = null;

	HashEntry<K, V> snapshotHeader = null;

	HashIterator() {
	    if (evictionPolicy.recordAccess(header, header) != null) snapshotHeader = header.cloneAll(header);
	    else snapshotHeader = header;

	    nextEntry = snapshotHeader.after;
	}

	public boolean hasMoreElements() {
	    return hasNext();
	}

	public boolean hasNext() {
	    return nextEntry != snapshotHeader;
	}

	HashEntry<K, V> nextEntry() {
	    if (nextEntry == snapshotHeader) throw new NoSuchElementException();
	    HashEntry<K, V> e = lastReturned = nextEntry;
	    nextEntry = e.after;

	    return e;
	}

	public void remove() {
	    if (lastReturned == null) throw new IllegalStateException();
	    ConcurrentLinkedHashMapPro.this.remove(lastReturned.key);
	    lastReturned = null;
	}

    }

    final class KeyIterator extends HashIterator implements Iterator<K>, Enumeration<K> {
	@Override
	public K next() {
	    return super.nextEntry().key;
	}

	@Override
	public K nextElement() {
	    return super.nextEntry().key;
	}
    }

    final class ValueIterator extends HashIterator implements Iterator<V>, Enumeration<V> {
	@Override
	public V next() {
	    return super.nextEntry().value;
	}

	@Override
	public V nextElement() {
	    return super.nextEntry().value;
	}
    }

    /**
     * Custom Entry class used by EntryIterator.next(), that relays setValue changes to the underlying
     * map.
     */
    final class WriteThroughEntry extends AbstractMap.SimpleEntry<K, V> implements Map.Entry<K, V> {

	private static final long serialVersionUID = 1573332674915851631L;

	WriteThroughEntry(K k, V v) {
	    super(k, v);
	}

	/**
	 * Set our entry's value and write through to the map. The value to return is somewhat arbitrary
	 * here. Since a WriteThroughEntry does not necessarily track asynchronous changes, the most recent
	 * "previous" value could be different from what we return (or could even have been removed in which
	 * case the put will re-establish). We do not and cannot guarantee more.
	 */
	@Override
	public V setValue(V value) {
	    if (value == null) throw new NullPointerException();
	    V v = super.setValue(value);
	    ConcurrentLinkedHashMapPro.this.put(getKey(), value);
	    return v;
	}
    }

    final class EntryIterator extends HashIterator implements Iterator<Map.Entry<K, V>> {
	@Override
	public Map.Entry<K, V> next() {
	    HashEntry<K, V> e = super.nextEntry();
	    return new WriteThroughEntry(e.key, e.value);
	}
    }

    final class KeySet extends AbstractSet<K> {
	@Override
	public Iterator<K> iterator() {
	    return new KeyIterator();
	}

	@Override
	public int size() {
	    return ConcurrentLinkedHashMapPro.this.size();
	}

	@Override
	public boolean isEmpty() {
	    return ConcurrentLinkedHashMapPro.this.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
	    return ConcurrentLinkedHashMapPro.this.containsKey(o);
	}

	@Override
	public boolean remove(Object o) {
	    return ConcurrentLinkedHashMapPro.this.remove(o) != null;
	}

	@Override
	public void clear() {
	    ConcurrentLinkedHashMapPro.this.clear();
	}
    }

    final class Values extends AbstractCollection<V> {
	@Override
	public Iterator<V> iterator() {
	    return new ValueIterator();
	}

	@Override
	public int size() {
	    return ConcurrentLinkedHashMapPro.this.size();
	}

	@Override
	public boolean isEmpty() {
	    return ConcurrentLinkedHashMapPro.this.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
	    return ConcurrentLinkedHashMapPro.this.containsValue(o);
	}

	@Override
	public void clear() {
	    ConcurrentLinkedHashMapPro.this.clear();
	}
    }

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
	    return new EntryIterator();
	}

	@Override
	public boolean contains(Object o) {
	    if (!(o instanceof Map.Entry<?, ?>)) return false;
	    Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
	    V v = ConcurrentLinkedHashMapPro.this.get(e.getKey());
	    return v != null && v.equals(e.getValue());
	}

	@Override
	public boolean remove(Object o) {
	    if (!(o instanceof Map.Entry<?, ?>)) return false;
	    Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
	    return ConcurrentLinkedHashMapPro.this.remove(e.getKey(), e.getValue());
	}

	@Override
	public int size() {
	    return ConcurrentLinkedHashMapPro.this.size();
	}

	@Override
	public boolean isEmpty() {
	    return ConcurrentLinkedHashMapPro.this.isEmpty();
	}

	@Override
	public void clear() {
	    ConcurrentLinkedHashMapPro.this.clear();
	}
    }

    /* ---------------- Serialization Support -------------- */

    /**
     * Save the state of the <tt>ConcurrentHashMap</tt> instance to a stream (i.e., serialize it).
     * 
     * @param s the stream
     * @serialData the key (Object) and value (Object) for each key-value mapping, followed by a null
     *             pair. The key-value mappings are emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
	s.defaultWriteObject();

	for (int k = 0; k < segments.length; ++k) {
	    Segment<K, V> seg = segments[k];
	    seg.lock();
	    try {
		HashEntry<K, V>[] tab = seg.table;
		for (int i = 0; i < tab.length; ++i) {
		    for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
			s.writeObject(e.key);
			s.writeObject(e.value);
		    }
		}
	    }
	    finally {
		seg.unlock();
	    }
	}
	s.writeObject(null);
	s.writeObject(null);
    }

    /**
     * Reconstitute the <tt>ConcurrentLinkedHashMap</tt> instance from a stream (i.e., deserialize it).
     * 
     * @param s the stream
     */
    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
	s.defaultReadObject();

	// Initialize each segment to be minimally sized, and let grow.
	for (int i = 0; i < segments.length; ++i) {
	    segments[i].setTable(new HashEntry[1]);
	}

	// Read the keys and values, and put the mappings in the table
	for (;;) {
	    K key = (K) s.readObject();
	    V value = (V) s.readObject();
	    if (key == null) break;
	    put(key, value);
	}
    }

    public interface Entry<K, V> extends Map.Entry<K, V> {

	/**
	 * Returns the entry before this entry in the entry list.
	 */
	Entry<K, V> getBefore();

	/**
	 * Returns the entry after this entry in the entry list.
	 */
	Entry<K, V> getAfter();

	/**
	 * Returns the entry's access count.
	 */
	long getAccessCount();

	/**
	 * Returns the entry's creation time in milliseconds.
	 */
	long getCreationTime();

	/**
	 * Returns the entry's last access time in milliseconds.
	 */
	long getLastAccessTime();

    }
}