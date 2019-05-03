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
package lucee.commons.collection.concurrent;

import lucee.commons.collection.concurrent.ConcurrentLinkedHashMapPro.Entry;

public interface EvictionPolicy {

	/**
	 * Defines if element access can alter the ordering of elements in the underlying cache. This method
	 * will be invoked by the underlying cache implementation when an entry access is generated.
	 * 
	 * <p>
	 * Invoking the <tt>get</tt> cache method results in an access to the corresponding entry (assuming
	 * it exists after the invocation completes). <i>No other methods generate entry accesses.</i> In
	 * particular, operations on collection-views do <i>not</i> affect the order of iteration of the
	 * backing cache.
	 * 
	 * @return true for access based ordering, false otherwise
	 */
	public boolean accessOrder();

	/**
	 * Defines if element insertion can alter the ordering of elements in the underlying cache. This
	 * method will be invoked by the underlying cache implementation when an entry insertion is
	 * generated.
	 * 
	 * <p>
	 * Invoking the <tt>put</tt> cache method results in an insertion of the corresponding entry. The
	 * <tt>putAll</tt> method generates one entry insertion for each mapping in the specified map, in
	 * the order that key-value mappings are provided by the specified map's entry set iterator. <i>No
	 * other methods generate entry insertions.</i>
	 * 
	 * @return true for insertion based ordering, false otherwise
	 */
	public boolean insertionOrder();

	/**
	 * This method will be invoked by the underlying cache implementation when a entry insertion is
	 * generated. For every entry insertion an entry eviction can take place if a cache size threshold
	 * has been defined and exceeded and this method's implementation returns a <i>not NULL<i> element.
	 * Invoking the <tt>put</tt> cache method results in an insertion of the corresponding entry. The
	 * <tt>putAll</tt> method generates one entry insertion for each mapping in the specified map, in
	 * the order that key-value mappings are provided by the specified map's entry set iterator. <i>No
	 * other methods generate entry insertions.</i> In particular, operations on collection-views do
	 * <i>not</i> trigger element eviction for the backing cache.
	 * 
	 * @param head the head of the double linked list of all elements in cache
	 * @return the element that should be evicted or null if no eviction should happen
	 */
	public Entry<?, ?> evictElement(Entry<?, ?> head);

	/**
	 * This method will be invoked by the underlying cache implementation when a entry insertion is
	 * generated. Invoking the <tt>put</tt> cache method results in an insertion of the corresponding
	 * entry. The <tt>putAll</tt> method generates one entry insertion for each mapping in the specified
	 * map, in the order that key-value mappings are provided by the specified map's entry set iterator.
	 * <i>No other methods generate entry insertions.</i>
	 * 
	 * <p>
	 * This method has no effect if {@link #insertionOrder()} method is implemented to return false,
	 * whereas the newly inserted element will be placed at the end (just before the head) of the
	 * cache's double linked element list.
	 * 
	 * @param head the head of the double linked list of all elements in cache
	 * @param insertedEntry the cache entry that is inserted
	 * @return the element that will be preceding the newly inserted element
	 */
	public Entry<?, ?> recordInsertion(Entry<?, ?> head, Entry<?, ?> insertedEntry);

	/**
	 * This method will be invoked by the underlying cache implementation when a entry access is
	 * generated. Invoking the <tt>get</tt> cache method results in an access to the corresponding entry
	 * (assuming it exists after the invocation completes).<i>No other methods generate entry
	 * accesses.</i>
	 * 
	 * <p>
	 * This method has no effect if {@link #accessOrder()} method is implemented to return false.
	 * 
	 * @param head the head of the double linked list of all elements in cache
	 * @param accessEntry the cache entry that is accessed
	 * @return the element that will be preceding the newly accessed element
	 */
	public Entry<?, ?> recordAccess(Entry<?, ?> head, Entry<?, ?> accessedEntry);

}