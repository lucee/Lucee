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
package lucee.commons.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;

public class MapProWrapper<K, V> implements MapPro<K, V>,Serializable {

	private final V NULL;
	private Map<K, V> map;

	public MapProWrapper(Map<K, V> map,V NULL){
		this.map=map;
		this.NULL=NULL;
	}
	
	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		if(value==null)value=NULL;
		return map.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<Entry<K, V>> src = map.entrySet();
		Iterator<Entry<K, V>> it = src.iterator();
		Entry<K, V> e;
		while(it.hasNext()){
			e = it.next();
			if(e.getValue()==NULL) e.setValue(null);
		}
		return src;
	}

	@Override
	public V get(Object key) {
		V v = map.get(key);
		if(v==NULL) return null;
		return v;
	}

	@Override
	public V g(K key, V defaultValue) {
		V v = map.get(key);
		if(v==NULL) return null;
		if(v==null) return defaultValue;
		return v;
	}

	@Override
	public V g(K key) throws PageException {
		V v = map.get(key);
		if(v==NULL) return null;
		if(v==null) throw invalidKey(this,key,false);
		return v;
	}

	@Override
	public V r(K key, V defaultValue) {
		V v = map.remove(key);
		if(v==NULL) return null;
		if(v==null) return defaultValue;
		return v;
	}

	@Override
	public V r(K key) throws PageException {
		V v = map.get(key);
		if(v==NULL) return null;
		if(v==null) throw invalidKey(this,key,true);
		return v;
	}
	
	private ExpressionException invalidKey(Map<K,V> map,K key, boolean remove) {

		StringBuilder sb=new StringBuilder();
		Iterator<K> it = map.keySet().iterator();
		K k;
		while(it.hasNext()){
			k = it.next();
			if(sb.length()>0)sb.append(',');
			sb.append(k.toString());
		}

		return new ExpressionException(
				(remove?
						"cannot remove key ["+key+"] from struct, key doesn't exist":
						"key [" + key + "] doesn't exist") +
				" (existing keys:" + sb.toString() + ")" );
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public V put(K key, V value) {
		V old;
		if(value==null) old=map.put(key, NULL);
		else old=map.put(key, value);
		
		if(old==NULL) return null;
		return old;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		return map.remove(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<V> values() {
		Collection<V> src = map.values();
		Set<V> trg = new HashSet<V>();
		Iterator<V> it = src.iterator();
		V v;
		while(it.hasNext()){
			v = it.next();
			if(v==NULL) trg.add(null);
			else trg.add(v);
		}
		return trg;
	}

	@Override
	public boolean equals(Object arg0) {
		return map.equals(arg0);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public String toString() {
		return map.toString();
	}
	
	public Map<K, V> getMap(){
		return map;
	}
}