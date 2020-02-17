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
package lucee.commons.io.cache;

import java.io.IOException;
import java.util.List;

import lucee.runtime.config.Config;
import lucee.runtime.type.Struct;

public interface Cache {

	/**
	 * initialize the cache
	 * 
	 * @param config Lucee configuration
	 * @param cacheName name of the cache
	 * @param arguments configuration arguments
	 * @throws IOException thrown when fail to execute
	 */
	public void init(Config config, String cacheName, Struct arguments) throws IOException;

	// FUTURE public void release() throws IOException;

	/**
	 * return cache entry that match the key, throws a CacheException when entry does not exist or is
	 * stale
	 * 
	 * @param key key name to get an entry for
	 * @return matching cache entry
	 * @throws IOException thrown when fail to execute
	 */
	public CacheEntry getCacheEntry(String key) throws IOException;

	/**
	 * return value that match the key, throws a CacheException when entry does not exist or is stale
	 * 
	 */
	public Object getValue(String key) throws IOException;

	/**
	 * return cache entry that match the key or the defaultValue when entry does not exist
	 * 
	 * @param key key of the cache entry to get
	 * @param defaultValue returned in case there is no entry or the cache fails to reach it
	 * @return cache entry
	 */
	public CacheEntry getCacheEntry(String key, CacheEntry defaultValue);

	/**
	 * return value that match the key or the defaultValue when entry does not exist
	 * 
	 * @param key key of the value to get
	 * @param defaultValue default value returned in case no value exist
	 * @return value
	 */
	public Object getValue(String key, Object defaultValue);

	/**
	 * puts a cache entry to the cache, overwrite existing entries that already exists inside the cache
	 * with the same key
	 */
	public void put(String key, Object value, Long idleTime, Long until) throws IOException;

	/**
	 * check if there is an entry inside the cache that match the given key
	 * 
	 * @param key
	 * @return contains a value that match this key
	 */
	public boolean contains(String key) throws IOException;

	/**
	 * remove entry that match this key
	 * 
	 * @param key
	 * @return returns if there was a removal
	 */
	public boolean remove(String key) throws IOException;

	/**
	 * remove all entries that match the given filter
	 * 
	 * @param filter
	 * @return returns the count of the removal or -1 if this information is not available
	 */
	public int remove(CacheKeyFilter filter) throws IOException;

	/**
	 * remove all entries that match the given filter
	 * 
	 * @param filter
	 * @return returns the count of the removal or -1 if this information is not available
	 */
	public int remove(CacheEntryFilter filter) throws IOException;

	/**
	 * 
	 * Returns a List of the keys contained in this cache. The set is NOT backed by the cache, so
	 * changes to the cache are NOT reflected in the set, and vice-versa.
	 * 
	 * @return a set of the keys contained in this cache.
	 */
	public List<String> keys() throws IOException;

	/**
	 * 
	 * Returns a List of the keys contained in this cache that match the given filter. The set is NOT
	 * backed by the cache, so changes to the cache are NOT reflected in the set, and vice-versa.
	 * 
	 * @param filter
	 * @return a set of the keys contained in this cache.
	 */
	public List<String> keys(CacheKeyFilter filter) throws IOException;

	/**
	 * 
	 * Returns a List of the keys contained in this cache that match the given filter. The set is NOT
	 * backed by the cache, so changes to the cache are NOT reflected in the set, and vice-versa.
	 * 
	 * @param filter
	 * @return a set of the keys contained in this cache.
	 */
	public List<String> keys(CacheEntryFilter filter) throws IOException;

	/**
	 * Returns a List of values containing in this cache. The set is NOT backed by the cache, so changes
	 * to the cache are NOT reflected in the set, and vice-versa.
	 * 
	 * @return a set of the entries contained in this cache.
	 */
	public List<Object> values() throws IOException;

	/**
	 * Returns a list of values containing in this cache that match the given filter. The set is NOT
	 * backed by the cache, so changes to the cache are NOT reflected in the set, and vice-versa.
	 * 
	 * @return a set of the entries contained in this cache.
	 */
	public List<Object> values(CacheKeyFilter filter) throws IOException;

	/**
	 * Returns a list of values containing in this cache that match the given filter. The set is NOT
	 * backed by the cache, so changes to the cache are NOT reflected in the set, and vice-versa.
	 * 
	 * @return a set of the entries contained in this cache.
	 */
	public List<Object> values(CacheEntryFilter filter) throws IOException;

	/**
	 * Returns a List of entries containing in this cache Each element in the returned list is a
	 * CacheEntry. The set is NOT backed by the cache, so changes to the cache are NOT reflected in the
	 * set, and vice-versa.
	 * 
	 * @return a set of the entries contained in this cache.
	 */
	public List<CacheEntry> entries() throws IOException;

	/**
	 * Returns a list of entries containing in this cache that match the given filter. Each element in
	 * the returned set is a CacheEntry. The set is NOT backed by the cache, so changes to the cache are
	 * NOT reflected in the set, and vice-versa.
	 * 
	 * @return a set of the entries contained in this cache.
	 */
	public List<CacheEntry> entries(CacheKeyFilter filter) throws IOException;

	/**
	 * Returns a list of entries containing in this cache that match the given filter. Each element in
	 * the returned set is a CacheEntry. The set is NOT backed by the cache, so changes to the cache are
	 * NOT reflected in the set, and vice-versa.
	 * 
	 * @return a set of the entries contained in this cache.
	 */
	public List<CacheEntry> entries(CacheEntryFilter filter) throws IOException;

	/**
	 * how many time was the cache accessed? this information is optional and depends on the
	 * implementation, when information is not available -1 is returned
	 * 
	 * @return access count
	 */
	public long hitCount() throws IOException;

	/**
	 * how many time was the cache accessed for a record that does not exist? this information is
	 * optional and depends on the implementation, when information is not available -1 is returned
	 * 
	 * @return access count
	 */
	public long missCount() throws IOException;

	/**
	 * get all information data available for this cache
	 */
	public Struct getCustomInfo() throws IOException;

}