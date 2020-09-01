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
import java.util.AbstractMap;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import lucee.commons.collection.AbstractCollection;
import lucee.commons.collection.AbstractSet;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Null;
import lucee.runtime.type.util.StructSupport;

/**
 * A hash table supporting full concurrency of retrievals and adjustable expected concurrency for
 * updates. This class obeys the same functional specification as {@link java.util.Hashtable}, and
 * includes versions of methods corresponding to each method of <tt>Hashtable</tt>. However, even
 * though all operations are thread-safe, retrieval operations do <em>not</em> entail locking, and
 * there is <em>not</em> any support for locking the entire table in a way that prevents all access.
 * This class is fully interoperable with <tt>Hashtable</tt> in programs that rely on its thread
 * safety but not on its synchronization details.
 *
 * <p>
 * Retrieval operations (including <tt>get</tt>) generally do not block, so may overlap with update
 * operations (including <tt>put</tt> and <tt>remove</tt>). Retrievals reflect the results of the
 * most recently <em>completed</em> update operations holding upon their onset. For aggregate
 * operations such as <tt>putAll</tt> and <tt>clear</tt>, concurrent retrievals may reflect
 * insertion or removal of only some entries. Similarly, Iterators and Enumerations return elements
 * reflecting the state of the hash table at some point at or since the creation of the
 * iterator/enumeration. They do <em>not</em> throw {@link ConcurrentModificationException}.
 * However, iterators are designed to be used by only one thread at a time.
 *
 * <p>
 * The allowed concurrency among update operations is guided by the optional
 * <tt>concurrencyLevel</tt> constructor argument (default <tt>16</tt>), which is used as a hint for
 * internal sizing. The table is internally partitioned to try to permit the indicated number of
 * concurrent updates without contention. Because placement in hash tables is essentially random,
 * the actual concurrency will vary. Ideally, you should choose a value to accommodate as many
 * threads as will ever concurrently modify the table. Using a significantly higher value than you
 * need can waste space and time, and a significantly lower value can lead to thread contention. But
 * overestimates and underestimates within an order of magnitude do not usually have much noticeable
 * impact. A value of one is appropriate when it is known that only one thread will modify and all
 * others will only read. Also, resizing this or any other kind of hash table is a relatively slow
 * operation, so, when possible, it is a good idea to provide estimates of expected table sizes in
 * constructors.
 *
 * <p>
 * This class and its views and iterators implement all of the <em>optional</em> methods of the
 * {@link Map} and {@link Iterator} interfaces.
 *
 * <p>
 * Like {@link Hashtable} but unlike {@link HashMap}, this class does <em>not</em> allow
 * <tt>null</tt> to be used as a key or value.
 *
 * <p>
 * This class is a member of the <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentHashMapNullSupport<K, V> extends AbstractMap<K, V> implements Serializable {
	private static final long serialVersionUID = 7249069246763182397L;

	/*
	 * The basic strategy is to subdivide the table among Segments, each of which itself is a
	 * concurrently readable hash table.
	 */

	/* ---------------- Constants -------------- */

	/**
	 * The default initial capacity for this table, used when not otherwise specified in a constructor.
	 */
	public static final int DEFAULT_INITIAL_CAPACITY = 32;

	/**
	 * The default load factor for this table, used when not otherwise specified in a constructor.
	 */
	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The default concurrency level for this table, used when not otherwise specified in a constructor.
	 */
	public static final int DEFAULT_CONCURRENCY_LEVEL = 16;

	/**
	 * The maximum capacity, used if a higher value is implicitly specified by either of the
	 * constructors with arguments. MUST be a power of two <= 1<<30 to ensure that entries are indexable
	 * using ints.
	 */
	static final int MAXIMUM_CAPACITY = 1 << 30;

	/**
	 * The maximum number of segments to allow; used to bound constructor arguments.
	 */
	static final int MAX_SEGMENTS = 1 << 32; // slightly conservative

	/**
	 * Number of unsynchronized retries in size and containsValue methods before resorting to locking.
	 * This is used to avoid unbounded retries if tables undergo continuous modification which would
	 * make it impossible to obtain an accurate result.
	 */
	static final int RETRIES_BEFORE_LOCK = 2;

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

	transient Set<K> keySet;
	transient Set<Map.Entry<K, V>> entrySet;
	transient Collection<V> values;

	/* ---------------- Small Utilities -------------- */

	private static int hash(Object o) {
		if (o instanceof KeyImpl) {
			return ((KeyImpl) o).wangJenkinsHash();
		}
		if (o == null) return 0;
		int h = o.hashCode();
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
	 * ConcurrentHashMap list entry. Note that this is never exported out as an user-visible Map.Entry.
	 *
	 * Because the value field is volatile, not final, it is legal wrt the Java Memory Model for an
	 * unsynchronized reader to see null instead of initial value when read via a data race. Although a
	 * reordering leading to this is not likely to ever actually occur, the Segment.readValueUnderLock
	 * method is used as a backup in case a null (pre-initialized) value is ever seen in an
	 * unsynchronized access method.
	 */
	static final class HashEntry<K, V> {
		final K key;
		final int hash;
		volatile V value;
		final HashEntry<K, V> next;

		HashEntry(K key, int hash, HashEntry<K, V> next, V value) {
			this.key = key;
			this.hash = hash;
			this.next = next;
			this.value = value;
		}

		@SuppressWarnings("unchecked")
		static final <K, V> HashEntry<K, V>[] newArray(int i) {
			return new HashEntry[i];
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

		Segment(int initialCapacity, float lf) {
			loadFactor = lf;
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
			// return e.value;
			try {
				return e.value;
			}
			finally {
				unlock();
			}
		}

		/* Specialized implementations of map methods */

		V get(Object key, int hash) {
			return get(key, hash, null);
		}

		V get(Object key, int hash, V defaultValue) {
			if (count != 0) { // read-volatile
				HashEntry<K, V> e = getFirst(hash);
				while (e != null) {
					if (e.hash == hash && (key == e.key || key.equals(e.key))) {
						V v = e.value;
						if (v != null) return v;
						return readValueUnderLock(e); // recheck; possible unnecessary double check then value can be null
					}
					e = e.next;
				}
			}
			return defaultValue;
		}

		V getE(Map<K, V> map, Object key, int hash) throws PageException {
			if (count != 0) { // read-volatile
				HashEntry<K, V> e = getFirst(hash);
				while (e != null) {
					if (e.hash == hash && key.equals(e.key)) {
						V v = e.value;
						if (v != null) return v;
						return readValueUnderLock(e); // recheck; possible unnecessary double check then value can be null
					}
					e = e.next;
				}
			}
			throw StructSupport.invalidKey(map, key, false);
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
						if (v == null) // recheck ; possible unnecessary double check then value can be null
							v = readValueUnderLock(e);
						if (value == null) {
							if (v == null) return true;
						}
						else if (value.equals(v)) return true;
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

		V repl(K key, int hash, V newValue, RefBoolean replaced) {
			lock();
			try {
				HashEntry<K, V> e = getFirst(hash);
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				if (e == null) return null;
				replaced.setValue(true);
				V oldValue = e.value;
				e.value = newValue;
				return oldValue;
			}
			finally {
				unlock();
			}
		}

		V put(K key, int hash, V value, boolean onlyIfAbsent) {
			lock();
			try {
				int c = count;
				if (c++ > threshold) // ensure capacity
					rehash();
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
					tab[index] = new HashEntry<K, V>(key, hash, first, value);
					count = c; // write-volatile
				}
				return oldValue;
			}
			finally {
				unlock();
			}
		}

		void rehash() {
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
							newTable[k] = new HashEntry<K, V>(p.key, p.hash, n, p.value);
						}
					}
				}
			}
			table = newTable;
		}

		/**
		 * Remove; match on key only if value null, else match both.
		 */
		Object _remove(Object key, int hash, Object value, Object defaultValue) {
			lock();
			try {
				int c = count - 1;
				HashEntry<K, V>[] tab = table;
				int index = hash & (tab.length - 1);
				HashEntry<K, V> first = tab[index];
				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				if (e == null) return defaultValue;

				V v = e.value;
				if ((value == null && v == null) || value.equals(v)) {
					++modCount;
					HashEntry<K, V> newFirst = e.next;
					for (HashEntry<K, V> p = first; p != e; p = p.next)
						newFirst = new HashEntry<K, V>(p.key, p.hash, newFirst, p.value);
					tab[index] = newFirst;
					count = c; // write-volatile
					return v;
				}
				return defaultValue;
			}
			finally {
				unlock();
			}
		}

		V r(Object key, int hash, V defaultValue) {
			lock();
			try {
				int c = count - 1;
				HashEntry<K, V>[] tab = table;
				int index = hash & (tab.length - 1);
				HashEntry<K, V> first = tab[index];
				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				if (e == null) return defaultValue;
				V v = e.value;
				V oldValue = v;
				++modCount;
				HashEntry<K, V> newFirst = e.next;
				for (HashEntry<K, V> p = first; p != e; p = p.next)
					newFirst = new HashEntry<K, V>(p.key, p.hash, newFirst, p.value);
				tab[index] = newFirst;
				count = c; // write-volatile
				return oldValue;
			}
			finally {
				unlock();
			}
		}

		V r(Map map, Object key, int hash) throws PageException {
			lock();
			try {
				int c = count - 1;
				HashEntry<K, V>[] tab = table;
				int index = hash & (tab.length - 1);
				HashEntry<K, V> first = tab[index];
				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				if (e == null) throw StructSupport.invalidKey(map, key, false);
				V v = e.value;
				V oldValue = v;
				++modCount;
				HashEntry<K, V> newFirst = e.next;
				for (HashEntry<K, V> p = first; p != e; p = p.next)
					newFirst = new HashEntry<K, V>(p.key, p.hash, newFirst, p.value);
				tab[index] = newFirst;
				count = c; // write-volatile
				return oldValue;
			}
			finally {
				unlock();
			}
		}

		void clear() {
			if (count != 0) {
				lock();
				try {
					HashEntry<K, V>[] tab = table;
					for (int i = 0; i < tab.length; i++)
						tab[i] = null;
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
	 * Creates a new, empty map with the specified initial capacity, load factor and concurrency level.
	 *
	 * @param initialCapacity the initial capacity. The implementation performs internal sizing to
	 *            accommodate this many elements.
	 * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed
	 *            when the average number of elements per bin exceeds this threshold.
	 * @param concurrencyLevel the estimated number of concurrently updating threads. The implementation
	 *            performs internal sizing to try to accommodate this many threads.
	 * @throws IllegalArgumentException if the initial capacity is negative or the load factor or
	 *             concurrencyLevel are nonpositive.
	 */
	public ConcurrentHashMapNullSupport(int initialCapacity, float loadFactor, int concurrencyLevel) {
		if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0) throw new IllegalArgumentException();

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
			this.segments[i] = new Segment<K, V>(cap, loadFactor);
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
	public ConcurrentHashMapNullSupport(int initialCapacity, float loadFactor) {
		this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
	}

	/**
	 * Creates a new, empty map with the specified initial capacity, and with default load factor (0.75)
	 * and concurrencyLevel (16).
	 *
	 * @param initialCapacity the initial capacity. The implementation performs internal sizing to
	 *            accommodate this many elements.
	 * @throws IllegalArgumentException if the initial capacity of elements is negative.
	 */
	public ConcurrentHashMapNullSupport(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
	}

	/**
	 * Creates a new, empty map with a default initial capacity (16), load factor (0.75) and
	 * concurrencyLevel (16).
	 */
	public ConcurrentHashMapNullSupport() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
	}

	/**
	 * Creates a new map with the same mappings as the given map. The map is created with a capacity of
	 * 1.5 times the number of mappings in the given map or 16 (whichever is greater), and a default
	 * load factor (0.75) and concurrencyLevel (16).
	 *
	 * @param m the map
	 */
	public ConcurrentHashMapNullSupport(Map<? extends K, ? extends V> m) {
		this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
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
		return segmentFor(hash).get(key, hash);
	}

	private V ge(Object key) throws PageException {
		int hash = hash(key);
		return segmentFor(hash).getE(this, key, hash);
	}

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		int hash = hash(key);
		// int hash = hash(key.hashCode());
		return segmentFor(hash).get(key, hash, defaultValue);
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

		final Segment<K, V>[] segments = this.segments;
		int[] mc = new int[segments.length];

		// Try a few times without locking
		for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
			int sum = 0;
			int mcsum = 0;
			for (int i = 0; i < segments.length; ++i) {
				int c = segments[i].count;
				mcsum += mc[i] = segments[i].modCount;
				if (segments[i].containsValue(value)) return true;
			}
			boolean cleanSweep = true;
			if (mcsum != 0) {
				for (int i = 0; i < segments.length; ++i) {
					int c = segments[i].count;
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
		int hash = hash(key);
		return segmentFor(hash).put(key, hash, value, false);
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
		return segmentFor(hash).r(key, hash, null);
	}

	public V rem(Object key) throws PageException {
		int hash = hash(key);
		return segmentFor(hash).r(this, key, hash);
	}

	private boolean remove(Map.Entry e) {
		Object k = e.getKey();
		Object v = e.getValue();
		int hash = hash(k);
		return segmentFor(hash)._remove(k, hash, v, Null.NULL) != Null.NULL;
	}

	/**
	 * Removes all of the mappings from this map.
	 */
	@Override
	public void clear() {
		for (int i = 0; i < segments.length; ++i)
			segments[i].clear();
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
		int nextSegmentIndex;
		int nextTableIndex;
		HashEntry<K, V>[] currentTable;
		HashEntry<K, V> nextEntry;
		HashEntry<K, V> lastReturned;

		HashIterator() {
			nextSegmentIndex = segments.length - 1;
			nextTableIndex = -1;
			advance();
		}

		public boolean hasMoreElements() {
			return hasNext();
		}

		final void advance() {
			if (nextEntry != null && (nextEntry = nextEntry.next) != null) return;

			while (nextTableIndex >= 0) {
				if ((nextEntry = currentTable[nextTableIndex--]) != null) return;
			}

			while (nextSegmentIndex >= 0) {
				Segment<K, V> seg = segments[nextSegmentIndex--];
				if (seg.count != 0) {
					currentTable = seg.table;
					for (int j = currentTable.length - 1; j >= 0; --j) {
						if ((nextEntry = currentTable[j]) != null) {
							nextTableIndex = j - 1;
							return;
						}
					}
				}
			}
		}

		public boolean hasNext() {
			return nextEntry != null;
		}

		HashEntry<K, V> nextEntry() {
			if (nextEntry == null) throw new NoSuchElementException();
			lastReturned = nextEntry;
			advance();
			return lastReturned;
		}

		public void remove() {
			if (lastReturned == null) throw new IllegalStateException();
			ConcurrentHashMapNullSupport.this.remove(lastReturned.key);
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
	final class WriteThroughEntry extends AbstractMap.SimpleEntry<K, V> {
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
			V v = super.setValue(value);
			ConcurrentHashMapNullSupport.this.put(getKey(), value);
			return v;
		}
	}

	final class EntryIterator extends HashIterator implements Iterator<Entry<K, V>> {
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
			return ConcurrentHashMapNullSupport.this.size();
		}

		@Override
		public boolean isEmpty() {
			return ConcurrentHashMapNullSupport.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return ConcurrentHashMapNullSupport.this.containsKey(o);
		}

		@Override
		public boolean remove(Object o) {
			try {
				ConcurrentHashMapNullSupport.this.rem(o);
				return true;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				return false;
			}
		}

		@Override
		public void clear() {
			ConcurrentHashMapNullSupport.this.clear();
		}
	}

	final class Values extends AbstractCollection<V> {
		@Override
		public Iterator<V> iterator() {
			return new ValueIterator();
		}

		@Override
		public int size() {
			return ConcurrentHashMapNullSupport.this.size();
		}

		@Override
		public boolean isEmpty() {
			return ConcurrentHashMapNullSupport.this.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return ConcurrentHashMapNullSupport.this.containsValue(o);
		}

		@Override
		public void clear() {
			ConcurrentHashMapNullSupport.this.clear();
		}
	}

	final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			try {
				V v = ge(e.getKey());
				if (v == null) {
					return e.getValue() == null;
				}
				return v.equals(e.getValue());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				return false;
			}
		}

		@Override
		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			return ConcurrentHashMapNullSupport.this.remove(e);
		}

		@Override
		public int size() {
			return ConcurrentHashMapNullSupport.this.size();
		}

		@Override
		public boolean isEmpty() {
			return ConcurrentHashMapNullSupport.this.isEmpty();
		}

		@Override
		public void clear() {
			ConcurrentHashMapNullSupport.this.clear();
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
	 * Reconstitute the <tt>ConcurrentHashMap</tt> instance from a stream (i.e., deserialize it).
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
}