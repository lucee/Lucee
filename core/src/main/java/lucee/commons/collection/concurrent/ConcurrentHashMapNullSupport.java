/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
 * Copyright (c) 2024, Lucee Association Switzerland
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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A wrapper around Java 8+ ConcurrentHashMap that supports null values.
 *
 * This is a modern replacement for the legacy Java 7-style segmented ConcurrentHashMap.
 * Benefits:
 * - Lock-free reads using CAS operations
 * - Fine-grained bucket locking (not segment locking)
 * - Better memory efficiency
 * - Better scalability under high contention
 * - Automatic benefits from JVM improvements
 *
 * Null support is implemented using sentinel values (NULL_VALUE objects)
 * that are transparently wrapped/unwrapped.
 */
public final class ConcurrentHashMapNullSupport<K, V> implements Map<K, V>, Serializable {
	private static final long serialVersionUID = 7249069246763182398L;

	// Sentinel value for null value handling (keys cannot be null, matching legacy behavior)
	private static final Object NULL_VALUE = new NullValue();

	// Sentinel for getOrDefault — distinct from NULL_VALUE, never stored in the map
	private static final Object ABSENT = new Object();

	private final ConcurrentHashMap<K, Object> delegate;

	/**
	 * Default initial capacity (same as legacy implementation for compatibility)
	 */
	public static final int DEFAULT_INITIAL_CAPACITY = 32;

	/**
	 * Default load factor (same as legacy implementation for compatibility)
	 */
	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * Creates a new, empty map with default initial capacity (32) and load factor (0.75).
	 */
	public ConcurrentHashMapNullSupport() {
		this.delegate = new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new, empty map with the specified initial capacity and default load factor (0.75).
	 *
	 * @param initialCapacity the initial capacity. The implementation performs internal sizing to
	 *            accommodate this many elements.
	 * @throws IllegalArgumentException if the initial capacity is negative.
	 */
	public ConcurrentHashMapNullSupport(int initialCapacity) {
		this.delegate = new ConcurrentHashMap<>(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new, empty map with the specified initial capacity and load factor.
	 *
	 * @param initialCapacity the initial capacity
	 * @param loadFactor the load factor threshold, used to control resizing
	 * @throws IllegalArgumentException if the initial capacity is negative or the load factor is nonpositive
	 */
	public ConcurrentHashMapNullSupport(int initialCapacity, float loadFactor) {
		this.delegate = new ConcurrentHashMap<>(initialCapacity, loadFactor);
	}

	/**
	 * Creates a new, empty map with the specified initial capacity, load factor and concurrency level.
	 * Note: In Java 8+, concurrencyLevel is no longer used (ConcurrentHashMap uses a different algorithm),
	 * but this constructor is kept for backward compatibility.
	 *
	 * @param initialCapacity the initial capacity
	 * @param loadFactor the load factor threshold, used to control resizing
	 * @param concurrencyLevel ignored (kept for compatibility with legacy code)
	 * @throws IllegalArgumentException if the initial capacity is negative or the load factor is nonpositive
	 */
	public ConcurrentHashMapNullSupport(int initialCapacity, float loadFactor, int concurrencyLevel) {
		// In Java 8+, concurrencyLevel is ignored - the new algorithm doesn't use segments
		this.delegate = new ConcurrentHashMap<>(initialCapacity, loadFactor);
	}

	/**
	 * Creates a new map with the same mappings as the given map.
	 * The map is created with a capacity of 1.5 times the number of mappings in the given map
	 * or 32 (whichever is greater), and default load factor (0.75).
	 *
	 * @param m the map
	 */
	public ConcurrentHashMapNullSupport(Map<? extends K, ? extends V> m) {
		// Match legacy behavior: capacity = max(m.size() / loadFactor + 1, DEFAULT_INITIAL_CAPACITY)
		int capacity = Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY);
		this.delegate = new ConcurrentHashMap<>(capacity, DEFAULT_LOAD_FACTOR);
		putAll(m);
	}

	// Sentinel marker class for null value handling
	private static final class NullValue implements Serializable {
		private static final long serialVersionUID = 1L;
		@Override public int hashCode() { return 0; }
		@Override public boolean equals(Object obj) { return obj instanceof NullValue; }
		@Override public String toString() { return "NULL_VALUE"; }
	}

	/**
	 * Wraps a value for storage (null -> NULL_VALUE sentinel)
	 */
	private Object wrapValue(Object value) {
		return value == null ? NULL_VALUE : value;
	}

	/**
	 * Unwraps a value from storage (NULL_VALUE sentinel -> null)
	 */
	@SuppressWarnings("unchecked")
	private V unwrapValue(Object value) {
		return value == NULL_VALUE ? null : (V) value;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(wrapValue(value));
	}

	@Override
	public V get(Object key) {
		return unwrapValue(delegate.get(key));
	}

	@Override
	public V put(K key, V value) {
		return unwrapValue(delegate.put(key, wrapValue(value)));
	}

	@Override
	public V remove(Object key) {
		return unwrapValue(delegate.remove(key));
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public Set<K> keySet() {
		// Return a view that unwraps keys
		return new KeySetView();
	}

	@Override
	public Collection<V> values() {
		// Return a view that unwraps values
		return new ValuesView();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		// Return a view that unwraps both keys and values
		return new EntrySetView();
	}

	// View classes for unwrapping values in collections (keys pass through directly)
	private class KeySetView implements Set<K> {
		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return delegate.containsKey(o);
		}

		@Override
		public java.util.Iterator<K> iterator() {
			return delegate.keySet().iterator();
		}

		@Override
		public Object[] toArray() {
			return delegate.keySet().toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return delegate.keySet().toArray(a);
		}

		@Override
		public boolean add(K e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			return delegate.remove(o) != null;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for (Object e : c) {
				if (!contains(e)) return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends K> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean modified = false;
			for (Object e : c) {
				if (remove(e)) modified = true;
			}
			return modified;
		}

		@Override
		public void clear() {
			delegate.clear();
		}
	}

	private class ValuesView implements Collection<V> {
		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return delegate.containsValue(wrapValue(o));
		}

		@Override
		public java.util.Iterator<V> iterator() {
			return new java.util.Iterator<V>() {
				private final java.util.Iterator<Object> delegateIterator = delegate.values().iterator();

				@Override
				public boolean hasNext() {
					return delegateIterator.hasNext();
				}

				@Override
				public V next() {
					return unwrapValue(delegateIterator.next());
				}

				@Override
				public void remove() {
					delegateIterator.remove();
				}
			};
		}

		@Override
		public Object[] toArray() {
			Object[] arr = delegate.values().toArray();
			for (int i = 0; i < arr.length; i++) {
				arr[i] = unwrapValue(arr[i]);
			}
			return arr;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			Object[] arr = delegate.values().toArray();
			if (a.length < arr.length) {
				a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), arr.length);
			}
			for (int i = 0; i < arr.length; i++) {
				a[i] = (T) unwrapValue(arr[i]);
			}
			if (a.length > arr.length) {
				a[arr.length] = null;
			}
			return a;
		}

		@Override
		public boolean add(V e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for (Object e : c) {
				if (!contains(e)) return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			delegate.clear();
		}
	}

	private class EntrySetView implements Set<Entry<K, V>> {
		@Override
		public int size() {
			return delegate.size();
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Entry)) return false;
			Entry<?, ?> e = (Entry<?, ?>) o;
			Object v = delegate.get(e.getKey());
			return v != null && v.equals(wrapValue(e.getValue()));
		}

		@Override
		public java.util.Iterator<Entry<K, V>> iterator() {
			return new java.util.Iterator<Entry<K, V>>() {
				private final java.util.Iterator<Entry<K, Object>> delegateIterator = delegate.entrySet().iterator();

				@Override
				public boolean hasNext() {
					return delegateIterator.hasNext();
				}

				@Override
				public Entry<K, V> next() {
					Entry<K, Object> e = delegateIterator.next();
					return new Entry<K, V>() {
						@Override
						public K getKey() {
							return e.getKey();
						}

						@Override
						public V getValue() {
							return unwrapValue(e.getValue());
						}

						@Override
						public V setValue(V value) {
							return unwrapValue(e.setValue(wrapValue(value)));
						}

						@Override
						public boolean equals(Object o) {
							if (!(o instanceof Entry)) return false;
							Entry<?, ?> other = (Entry<?, ?>) o;
							K k = getKey();
							V v = getValue();
							return (k == null ? other.getKey() == null : k.equals(other.getKey()))
									&& (v == null ? other.getValue() == null : v.equals(other.getValue()));
						}

						@Override
						public int hashCode() {
							K k = getKey();
							V v = getValue();
							return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
						}
					};
				}

				@Override
				public void remove() {
					delegateIterator.remove();
				}
			};
		}

		@Override
		public Object[] toArray() {
			Object[] arr = new Object[delegate.size()];
			int i = 0;
			for (Entry<K, V> e : this) {
				arr[i++] = e;
			}
			return arr;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			int size = delegate.size();
			if (a.length < size) {
				a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
			}
			int i = 0;
			for (Entry<K, V> e : this) {
				a[i++] = (T) e;
			}
			if (a.length > size) {
				a[size] = null;
			}
			return a;
		}

		@Override
		public boolean add(Entry<K, V> e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			if (!(o instanceof Entry)) return false;
			Entry<?, ?> e = (Entry<?, ?>) o;
			return delegate.remove(e.getKey(), wrapValue(e.getValue()));
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for (Object e : c) {
				if (!contains(e)) return false;
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends Entry<K, V>> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			boolean modified = false;
			for (Object e : c) {
				if (remove(e)) modified = true;
			}
			return modified;
		}

		@Override
		public void clear() {
			delegate.clear();
		}
	}

	// Java 8+ Map methods with null-aware wrapping

	@Override
	public V getOrDefault(Object key, V defaultValue) {
		Object raw = delegate.getOrDefault(key, ABSENT);
		return raw == ABSENT ? defaultValue : unwrapValue(raw);
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		delegate.forEach((k, v) -> action.accept(k, unwrapValue(v)));
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		delegate.replaceAll((k, v) -> wrapValue(function.apply(k, unwrapValue(v))));
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return unwrapValue(delegate.putIfAbsent(key, wrapValue(value)));
	}

	@Override
	public boolean remove(Object key, Object value) {
		return delegate.remove(key, wrapValue(value));
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return delegate.replace(key, wrapValue(oldValue), wrapValue(newValue));
	}

	@Override
	public V replace(K key, V value) {
		return unwrapValue(delegate.replace(key, wrapValue(value)));
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return unwrapValue(delegate.computeIfAbsent(key, k -> wrapValue(mappingFunction.apply(k))));
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return unwrapValue(delegate.computeIfPresent(key, (k, v) -> wrapValue(remappingFunction.apply(k, unwrapValue(v)))));
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return unwrapValue(delegate.compute(key, (k, v) -> wrapValue(remappingFunction.apply(k, unwrapValue(v)))));
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return unwrapValue(delegate.merge(key, wrapValue(value), (v1, v2) -> wrapValue(remappingFunction.apply(unwrapValue(v1), unwrapValue(v2)))));
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Map)) return false;
		Map<?, ?> m = (Map<?, ?>) o;
		if (m.size() != size()) return false;
		try {
			for (Entry<K, V> e : entrySet()) {
				K key = e.getKey();
				V value = e.getValue();
				if (value == null) {
					if (!(m.get(key) == null && m.containsKey(key))) return false;
				}
				else {
					if (!value.equals(m.get(key))) return false;
				}
			}
		}
		catch (ClassCastException | NullPointerException unused) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int h = 0;
		for (Entry<K, V> e : entrySet()) {
			h += e.hashCode();
		}
		return h;
	}

	@Override
	public String toString() {
		java.util.Iterator<Entry<K, V>> i = entrySet().iterator();
		if (!i.hasNext()) return "{}";

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (;;) {
			Entry<K, V> e = i.next();
			K key = e.getKey();
			V value = e.getValue();
			sb.append(key == this ? "(this Map)" : key);
			sb.append('=');
			sb.append(value == this ? "(this Map)" : value);
			if (!i.hasNext()) return sb.append('}').toString();
			sb.append(',').append(' ');
		}
	}
}
