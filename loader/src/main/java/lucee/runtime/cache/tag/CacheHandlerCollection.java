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

import java.util.List;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public interface CacheHandlerCollection {

	// public static final int TYPE_TIMESPAN=1;
	// public static final int TYPE_REQUEST=2;
	// public static final int TYPE_SMART=4;

	/**
	 * based on the cachedWithin Object we choose the right Cachehandler and return it
	 * 
	 * @return
	 */
	public CacheHandler getInstanceMatchingObject(Object cachedWithin, CacheHandler defaultValue);

	/**
	 * 
	 * @param cacheHandlerId id returned by CacheHandler.id() can be for example (request,timespan,...)
	 * @return
	 */
	public CacheHandler getInstance(String cacheHandlerId, CacheHandler defaultValue);

	// public SmartCacheHandler getSmartCacheHandler();

	public int size(PageContext pc) throws PageException;

	public void clear(PageContext pc) throws PageException;

	public void clear(PageContext pc, CacheHandlerFilter filter) throws PageException;

	public void clean(PageContext pc) throws PageException;

	public void remove(PageContext pageContext, String id) throws PageException;

	public void release(PageContext pc) throws PageException;

	public List<String> getPatterns();
}