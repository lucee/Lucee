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
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;

public class CacheHandlerCollections {

	public final CacheHandlerCollectionImpl query;
	public final CacheHandlerCollectionImpl function;
	public final CacheHandlerCollectionImpl include;
	public final CacheHandlerCollectionImpl resource;
	public final CacheHandlerCollectionImpl http;
	public final CacheHandlerCollectionImpl file;
	public final CacheHandlerCollectionImpl webservice;

	public CacheHandlerCollections(ConfigWeb cw) {
		query = new CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_QUERY);
		function = new CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_FUNCTION);
		include = new CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_INCLUDE);
		resource = new CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_RESOURCE);
		http = new CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_HTTP);
		file = new CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_FILE);
		webservice = new CacheHandlerCollectionImpl(cw, ConfigPro.CACHE_TYPE_WEBSERVICE);
	}

	public void releaseCacheHandlers(PageContext pc) {
		try {
			query.release(pc);
		}
		catch (PageException e) {}
		try {
			function.release(pc);
		}
		catch (PageException e) {}
		try {
			include.release(pc);
		}
		catch (PageException e) {}
		try {
			resource.release(pc);
		}
		catch (PageException e) {}
		try {
			http.release(pc);
		}
		catch (PageException e) {}
		try {
			file.release(pc);
		}
		catch (PageException e) {}
		try {
			webservice.release(pc);
		}
		catch (PageException e) {}
	}
}