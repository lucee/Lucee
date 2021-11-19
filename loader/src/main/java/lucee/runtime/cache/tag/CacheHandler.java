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
package lucee.runtime.cache.tag;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;

public interface CacheHandler {

	public void init(ConfigWeb cw, String id, int cacheType) throws PageException;

	public String id();

	public CacheItem get(PageContext pc, String id) throws PageException;

	public boolean remove(PageContext pc, String id) throws PageException;

	public void set(PageContext pc, String id, Object cachedwithin, CacheItem value) throws PageException;

	public void clear(PageContext pc) throws PageException;

	public void clear(PageContext pc, CacheHandlerFilter filter) throws PageException;

	public void clean(PageContext pc) throws PageException;

	public int size(PageContext pc) throws PageException;

	public void release(PageContext pc) throws PageException;

	public boolean acceptCachedWithin(Object cachedWithin);

	/**
	 * return a pattern for that handler, for example "request" or "{time-span}"
	 * 
	 * @return
	 */
	public String pattern();
}