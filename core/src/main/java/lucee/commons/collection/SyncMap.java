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
package lucee.commons.collection;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lucee.commons.lang.SerializableObject;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.util.StructUtil;


public class SyncMap<K,V>
        implements MapPro<K,V>, Serializable {
        private static final long serialVersionUID = 1978198479659022715L;

        private final MapPro<K,V> m;     // Backing Map
        final Serializable      mutex;        // Object on which to synchronize

        public SyncMap() {
            this(null);
        }
        
        public SyncMap(MapPro<K,V> m) {
            if (m==null) this.m = new HashMapPro<K, V>();
            else this.m = m;
            mutex = new SerializableObject();
        }

        SyncMap(MapPro<K,V> m, Serializable mutex) {
            this.m = m;
            this.mutex = mutex;
        }
        

        @Override
		public int size() {
            synchronized (mutex) {return m.size();}
        }
        @Override
		public boolean isEmpty() {
            synchronized (mutex) {return m.isEmpty();}
        }
        @Override
		public boolean containsKey(Object key) {
            synchronized (mutex) {return m.containsKey(key);}
        }
        @Override
		public boolean containsValue(Object value) {
            synchronized (mutex) {return m.containsValue(value);}
        }
        @Override
		public V get(Object key) {
            synchronized (mutex) {return m.get(key);}
        }


		@Override
		public V g(K key) throws PageException {
			synchronized (mutex) {return m.g(key);}
		}

		@Override
		public V g(K key, V defaultValue) {
			synchronized (mutex) {return m.g(key,defaultValue);}
		}

		@Override
		public V r(K key) throws PageException {
			synchronized (mutex) {return m.r(key);}
		}

		@Override
		public V r(K key, V defaultValue) {
			synchronized (mutex) {return m.r(key,defaultValue);}
		}
        
        

        @Override
		public V put(K key, V value) {
            synchronized (mutex) {return m.put(key, value);}
        }
        @Override
		public V remove(Object key) {
            synchronized (mutex) {return m.remove(key);}
        }
        
        @Override
		public void putAll(Map<? extends K, ? extends V> map) {
            synchronized (mutex) {m.putAll(map);}
        }
        @Override
		public void clear() {
            synchronized (mutex) {m.clear();}
        }

        private transient Set<K> keySet = null;
        private transient Set<MapPro.Entry<K,V>> entrySet = null;
        private transient Collection<V> values = null;

        @Override
		public Set<K> keySet() {
            synchronized (mutex) {
                if (keySet==null)
                    keySet = new SyncSet<K>(m.keySet(), mutex);
                return keySet;
            }
        }

        @Override
		public Set<Map.Entry<K,V>> entrySet() {
            synchronized (mutex) {
                if (entrySet==null)
                    entrySet = new SyncSet<Map.Entry<K,V>>(m.entrySet(), mutex);
                return entrySet;
            }
        }

        @Override
		public Collection<V> values() {
            synchronized (mutex) {
                if (values==null)
                    values = new SyncCollection<V>(m.values(), mutex);
                return values;
            }
        }

        @Override
		public boolean equals(Object o) {
            if (this == o)
                return true;
            synchronized (mutex) {return m.equals(o);}
        }
        @Override
		public int hashCode() {
            synchronized (mutex) {return m.hashCode();}
        }
        @Override
		public String toString() {
            synchronized (mutex) {return m.toString();}
        }
        private void writeObject(ObjectOutputStream s) throws IOException {
            synchronized (mutex) {s.defaultWriteObject();}
        }

		public int getType() {
			return StructUtil.getType(m);
		}
    }