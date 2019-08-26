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
package lucee.commons.io.cache;

import java.io.Serializable;

/**
 * Ac CacheEventListener is registered to a cache implementing the interface CacheEvent, a
 * CacheEventListener can listen to certain event happening in a cache
 */
public interface CacheEventListener extends Serializable {

	/**
	 * this method is invoked before a Cache Entry is removed from Cache
	 * 
	 * @param entry entry that will be removed from Cache
	 */
	public void onRemove(CacheEntry entry);

	/**
	 * this method is invoked before a new Entry is putted to a cache (update and insert)
	 */
	public void onPut(CacheEntry entry);

	/**
	 * this method is invoked before an entry expires (lifetime and idletime)
	 */
	public void onExpires(CacheEntry entry);

	public CacheEventListener duplicate();
}