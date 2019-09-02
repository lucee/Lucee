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
package lucee.runtime.query;

import java.io.IOException;
import java.util.Date;

import lucee.runtime.PageContext;
import lucee.runtime.db.SQL;
import lucee.runtime.type.Query;

/**
 * interface for a query cache
 */
public interface QueryCache {

	/**
	 * clear expired queries from cache
	 * 
	 * @throws IOException
	 */
	public abstract void clearUnused(PageContext pc) throws IOException;

	/**
	 * returns a Query from Query Cache or null if no match found
	 * 
	 * @param sql
	 * @param datasource
	 * @param username
	 * @param password
	 * @param cacheAfter
	 * @return Query
	 */
	public abstract Query getQuery(PageContext pc, SQL sql, String datasource, String username, String password, Date cacheAfter);

	/**
	 * sets a Query to Cache
	 * 
	 * @param sql
	 * @param datasource
	 * @param username
	 * @param password
	 * @param value
	 * @param cacheBefore
	 */
	public abstract void set(PageContext pc, SQL sql, String datasource, String username, String password, Object value, Date cacheBefore);

	/**
	 * clear the cache
	 * 
	 * @param pc
	 */
	public abstract void clear(PageContext pc);

	/**
	 * clear the cache
	 * 
	 * @param filter
	 */
	public abstract void clear(PageContext pc, QueryCacheFilter filter);

	/**
	 * removes query from cache
	 * 
	 * @param pc
	 * @param sql
	 * @param datasource
	 * @param username
	 * @param password
	 */
	public abstract void remove(PageContext pc, SQL sql, String datasource, String username, String password);

	public abstract Object get(PageContext pc, SQL sql, String datasource, String username, String password, Date cachedafter);

	public abstract int size(PageContext pc);
}