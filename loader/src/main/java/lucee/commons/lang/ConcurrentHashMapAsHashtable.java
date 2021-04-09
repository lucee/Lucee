package lucee.commons.lang;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConcurrentHashMapAsHashtable<K, V> extends Hashtable<K, V> {

	private ConcurrentHashMap<K, V> map;

	public ConcurrentHashMapAsHashtable() {
		this.map = new ConcurrentHashMap<>();
	}

	@Override
	public synchronized int size() {
		return map.size();
	}

	@Override
	public synchronized boolean isEmpty() {

		return map.isEmpty();
	}

	@Override
	public synchronized Enumeration<K> keys() {

		return map.keys();
	}

	@Override
	public synchronized Enumeration<V> elements() {

		return map.elements();
	}

	@Override
	public synchronized boolean contains(Object value) {

		return map.contains(value);
	}

	@Override
	public boolean containsValue(Object value) {

		return map.containsValue(value);
	}

	@Override
	public synchronized boolean containsKey(Object key) {

		return map.containsKey(key);
	}

	@Override
	public synchronized V get(Object key) {

		return map.get(key);
	}

	@Override
	protected void rehash() {
		// do nothing
	}

	@Override
	public synchronized V put(K key, V value) {
		if (size() > 200) clear();// TODO do a soft version instead
		return map.put(key, value);
	}

	@Override
	public synchronized V remove(Object key) {

		return map.remove(key);
	}

	@Override
	public synchronized void putAll(Map<? extends K, ? extends V> t) {

		map.putAll(t);
	}

	@Override
	public synchronized void clear() {

		map.clear();
	}

	@Override
	public synchronized Object clone() {
		ConcurrentHashMapAsHashtable<K, V> newMap = new ConcurrentHashMapAsHashtable<>();

		for (java.util.Map.Entry<K, V> e: map.entrySet()) {
			newMap.put(e.getKey(), e.getValue());
		}
		return newMap;
	}

	@Override
	public synchronized String toString() {

		return map.toString();
	}

	@Override
	public Set<K> keySet() {

		return map.keySet();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {

		return map.entrySet();
	}

	@Override
	public Collection<V> values() {

		return map.values();
	}

	@Override
	public synchronized boolean equals(Object o) {

		return this == o || map.equals(o);
	}

	@Override
	public synchronized int hashCode() {

		return map.hashCode();
	}

	@Override
	public synchronized V getOrDefault(Object key, V defaultValue) {

		return map.getOrDefault(key, defaultValue);
	}

	@Override
	public synchronized void forEach(BiConsumer<? super K, ? super V> action) {

		map.forEach(action);
	}

	@Override
	public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {

		map.replaceAll(function);
	}

	@Override
	public synchronized V putIfAbsent(K key, V value) {

		return map.putIfAbsent(key, value);
	}

	@Override
	public synchronized boolean remove(Object key, Object value) {

		return map.remove(key, value);
	}

	@Override
	public synchronized boolean replace(K key, V oldValue, V newValue) {

		return map.replace(key, oldValue, newValue);
	}

	@Override
	public synchronized V replace(K key, V value) {

		return map.replace(key, value);
	}

	@Override
	public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {

		return map.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {

		return map.computeIfPresent(key, remappingFunction);
	}

	@Override
	public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {

		return map.compute(key, remappingFunction);
	}

	@Override
	public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {

		return map.merge(key, value, remappingFunction);
	}
}