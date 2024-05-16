package lucee.commons.lang;

import java.util.Hashtable;

public class WeakConcurrentHashMapAsHashtable<K, V> extends Hashtable<K, V> {
	/*
	 * private static final Reference DEFAULT_VALUE = new WeakReference("");
	 * 
	 * private ConcurrentHashMap<K, Reference<V>> map;
	 * 
	 * public WeakConcurrentHashMapAsHashtable() { this.map = new ConcurrentHashMap<>(); }
	 * 
	 * @Override public int size() { int size = 0; for (Reference<V> ref: map.values()) { if (ref !=
	 * null && ref.get() != null) size++; }
	 * 
	 * return size; }
	 * 
	 * @Override public boolean isEmpty() { for (Reference<V> ref: map.values()) { if (ref != null &&
	 * ref.get() != null) return false; } return true; }
	 * 
	 * @Override public Enumeration<K> keys() { return entries().keys(); }
	 * 
	 * @Override public Enumeration<V> elements() { return entries().elements(); }
	 * 
	 * @Override public boolean contains(Object value) { return map.containsValue(value); }
	 * 
	 * @Override public boolean containsValue(Object value) { if (value == null) throw new
	 * NullPointerException();
	 * 
	 * for (Reference<V> ref: map.values()) { if (ref != null && ref.get() != null && (ref.get() ==
	 * value || ref.get().equals(value))) return true; } return false; }
	 * 
	 * @Override public boolean containsKey(Object key) { if (key == null) throw new
	 * NullPointerException(); Reference<V> ref = map.get(key); return ref != null && ref.get() != null;
	 * }
	 * 
	 * @Override public V get(Object key) { Reference<V> ref = map.get(key); return ref != null ?
	 * ref.get() : null; }
	 * 
	 * @Override protected void rehash() { // do nothing }
	 * 
	 * @Override public V put(K key, V value) { Reference<V> ref = map.put(key, new
	 * WeakReference<V>(value)); return ref != null ? ref.get() : null; }
	 * 
	 * private void putIgnoreNull(K key, V value) { if (value != null) map.put(key, new
	 * WeakReference<V>(value)); }
	 * 
	 * @Override public V remove(Object key) { Reference<V> ref = map.remove(key); return ref != null ?
	 * ref.get() : null; }
	 * 
	 * @Override public void putAll(Map<? extends K, ? extends V> m) { for (Map.Entry<? extends K, ?
	 * extends V> e: m.entrySet()) { put(e.getKey(), e.getValue()); } }
	 * 
	 * @Override public void clear() { map.clear(); }
	 * 
	 * @Override public Object clone() { ConcurrentHashMapAsHashtable<K, V> newMap = new
	 * ConcurrentHashMapAsHashtable<>(); Reference<V> ref; for (java.util.Map.Entry<K, Reference<V>> e:
	 * map.entrySet()) { ref = e.getValue(); if (ref != null) newMap.putIgnoreNull(e.getKey(),
	 * ref.get()); } return newMap; }
	 * 
	 * @Override public String toString() {
	 * 
	 * return map.toString(); }
	 * 
	 * @Override public Set<K> keySet() {
	 * 
	 * return map.keySet(); }
	 * 
	 * @Override public Set<Entry<K, V>> entrySet() { return entries().entrySet(); }
	 * 
	 * @Override public Collection<V> values() { return entries().values(); }
	 * 
	 * @Override public boolean equals(Object o) {
	 * 
	 * return map.equals(o); }
	 * 
	 * @Override public int hashCode() {
	 * 
	 * return map.hashCode(); }
	 * 
	 * @Override public V getOrDefault(Object key, V defaultValue) { Reference ref =
	 * map.getOrDefault(key, DEFAULT_VALUE); return ref == DEFAULT_VALUE ? defaultValue : (V) ref.get();
	 * }
	 * 
	 * @Override public void forEach(BiConsumer<? super K, ? super V> action) {
	 * entries().forEach(action); }
	 * 
	 * @Override public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
	 * 
	 * map.replaceAll(function); }
	 * 
	 * @Override public V putIfAbsent(K key, V value) {
	 * 
	 * return map.putIfAbsent(key, value); }
	 * 
	 * @Override public boolean remove(Object key, Object value) {
	 * 
	 * return map.remove(key, value); }
	 * 
	 * @Override public boolean replace(K key, V oldValue, V newValue) {
	 * 
	 * return map.replace(key, oldValue, newValue); }
	 * 
	 * @Override public V replace(K key, V value) {
	 * 
	 * return map.replace(key, value); }
	 * 
	 * @Override public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
	 * 
	 * return map.computeIfAbsent(key, mappingFunction); }
	 * 
	 * @Override public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V>
	 * remappingFunction) {
	 * 
	 * return map.computeIfPresent(key, remappingFunction); }
	 * 
	 * @Override public V compute(K key, BiFunction<? super K, ? super V, ? extends V>
	 * remappingFunction) {
	 * 
	 * return map.compute(key, remappingFunction); }
	 * 
	 * @Override public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V>
	 * remappingFunction) {
	 * 
	 * return map.merge(key, value, remappingFunction); }
	 * 
	 * private ConcurrentHashMap<K, V> entries() { ConcurrentHashMap<K, V> entries = new
	 * ConcurrentHashMap<>(); V v; for (Entry<K, Reference<V>> e: map.entrySet()) { if (e.getValue() !=
	 * null) { v = e.getValue().get(); if (v != null) entries.put(e.getKey(), e.getValue().get()); } }
	 * return entries; }
	 */
}
