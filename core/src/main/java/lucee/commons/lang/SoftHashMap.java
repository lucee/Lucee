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
package lucee.commons.lang;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class SoftHashMap extends AbstractMap {
  /** The internal HashMap that will hold the SoftReference. */
  private final Map hash = new HashMap();
  /** The number of "hard" references to hold internally. */
  private final int HARD_SIZE;
  /** The FIFO list of hard references, order of last access. */
  private final LinkedList hardCache = new LinkedList();
  /** Reference queue for cleared SoftReference objects. */
  private final ReferenceQueue queue = new ReferenceQueue();

  public SoftHashMap() { 
    HARD_SIZE = -1;
  }
  public SoftHashMap(int hardSize) { 
    HARD_SIZE = hardSize; 
  }

  @Override
public Object get(Object key) {
    Object result = null;
    // We get the SoftReference represented by that key
    SoftReference soft_ref = (SoftReference)hash.get(key);
    if (soft_ref != null) {
      result = soft_ref.get();
      if (result == null) {
        hash.remove(key);
      } else {
        hardCache.addFirst(result);
        // Remove the last entry if list longer than HARD_SIZE
        if (HARD_SIZE > 0 && hardCache.size() > HARD_SIZE) {
          hardCache.removeLast();
        }
      }
    }
    return result;
  }

  private void processQueue() {
    SoftValue sv;
    while ((sv = (SoftValue)queue.poll()) != null) {
      hash.remove(sv.key); 
    }
  }

  @Override
public Object put(Object key, Object value) {
    processQueue(); 
    return hash.put(key, new SoftValue(value, queue, key));
  }

  @Override
public Object remove(Object key) {
    // throw out garbage collected values first
    processQueue(); 
    return hash.remove(key);
  }

  @Override
public void clear() {
    hardCache.clear();
    processQueue(); 
    hash.clear();
  }

  @Override
public int size() {
    processQueue(); 
    return hash.size();
  }

  @Override
public Set entrySet() {
    // no, no, you may NOT do that!!! GRRR
    throw new UnsupportedOperationException();
  }
  
  private static class SoftValue extends SoftReference {
    private final Object key; // always make data member final
    private SoftValue(Object k, ReferenceQueue q, Object key) {
      super(k, q);
      this.key = key;
    }
  }
}