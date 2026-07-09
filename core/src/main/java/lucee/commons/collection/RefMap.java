package lucee.commons.collection;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RefMap<K, V> extends AbstractMap<K, V> {

	public enum ReferenceType {
		SOFT, WEAK
	}

	private final Map<K, Reference<V>> delegate;
	private final ReferenceType type;

	/**
	 * Creates a RefMap with default HashMap delegate
	 */
	public RefMap(ReferenceType type) {
		this(type, 16, 0.75f);
	}

	/**
	 * Creates a RefMap with default HashMap delegate and initial capacity
	 */
	public RefMap(ReferenceType type, int initialCapacity) {
		this(type, initialCapacity, 0.75f);
	}

	/**
	 * Creates a RefMap with default HashMap delegate
	 */
	public RefMap(ReferenceType type, int initialCapacity, float loadFactor) {
		this(type, new HashMap<K, Reference<V>>(initialCapacity, loadFactor));
	}

	/**
	 * Creates a RefMap with a custom delegate Map Useful for thread-safe access: pass ConcurrentHashMap
	 * or Collections.synchronizedMap()
	 */
	public RefMap(ReferenceType type, Map<K, Reference<V>> delegateMap) {
		this.type = type;
		this.delegate = delegateMap;
	}

	@Override
	public V get(Object key) {
		Reference<V> ref = delegate.get(key);
		if (ref == null) return null;

		V value = ref.get();
		if (value == null) {
			delegate.remove(key);
		}
		return value;
	}

	@Override
	public V put(K key, V value) {
		if (value == null) {
			return remove(key);
		}

		Reference<V> oldRef = delegate.put(key, createReference(value));
		return oldRef == null ? null : oldRef.get();
	}

	@Override
	public V remove(Object key) {
		Reference<V> ref = delegate.remove(key);
		return ref == null ? null : ref.get();
	}

	@Override
	public boolean containsKey(Object key) {
		Reference<V> ref = delegate.get(key);
		if (ref == null) return false;

		V value = ref.get();
		if (value == null) {
			delegate.remove(key);
			return false;
		}
		return true;
	}

	@Override
	public boolean containsValue(Object value) {
		for (Reference<V> ref: delegate.values()) {
			V v = ref.get();
			if (v == null) continue;
			if (v.equals(value)) return true;
		}
		return false;
	}

	@Override
	public int size() {
		cleanupDeadEntries();
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		cleanupDeadEntries();
		return delegate.isEmpty();
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		cleanupDeadEntries();
		return new AbstractSet<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				final Iterator<Entry<K, Reference<V>>> delegateIt = delegate.entrySet().iterator();
				return new Iterator<Entry<K, V>>() {
					private Entry<K, V> nextEntry;
					private Iterator<Entry<K, Reference<V>>> iter = delegateIt;

					@Override
					public boolean hasNext() {
						while (iter.hasNext()) {
							Entry<K, Reference<V>> entry = iter.next();
							V value = entry.getValue().get();
							if (value != null) {
								nextEntry = new AbstractMap.SimpleEntry<>(entry.getKey(), value);
								return true;
							}
							else {
								delegate.remove(entry.getKey());
							}
						}
						return false;
					}

					@Override
					public Entry<K, V> next() {
						return nextEntry;
					}
				};
			}

			@Override
			public int size() {
				return RefMap.this.size();
			}
		};
	}

	private void cleanupDeadEntries() {
		Iterator<Entry<K, Reference<V>>> it = delegate.entrySet().iterator();
		while (it.hasNext()) {
			Entry<K, Reference<V>> entry = it.next();
			if (entry.getValue().get() == null) {
				it.remove();
			}
		}
	}

	private Reference<V> createReference(V value) {
		switch (type) {
		case SOFT:
			return new SoftReference<>(value);
		case WEAK:
			return new WeakReference<>(value);
		default:
			throw new IllegalStateException("Unknown reference type: " + type);
		}
	}

	@Override
	public String toString() {
		return "RefMap(" + type + ")[size=" + size() + "]";
	}
}
