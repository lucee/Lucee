/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

import lucee.commons.io.cache.exp.CacheException;

public interface CachePro extends Cache {

	/**
	 * clears the complete Cache
	 * 
	 * @throws IOException
	 */
	public int clear() throws IOException;

	/**
	 * verifies the cache, throws an exception if something is wrong with the cache
	 * 
	 * @throws CacheException
	 */
	public void verify() throws CacheException;

	/**
	 * if the cache does not necessary decouple values, this method should make sure of it.
	 */
	public CachePro decouple();
}