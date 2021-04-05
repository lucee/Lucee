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
package lucee.runtime.util;

import lucee.runtime.PageContext;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;

/**
 * Query Stack
 */
public interface QueryStack {

	/**
	 * adds a Query to the Stack
	 * 
	 * @param query Query
	 */
	public abstract void addQuery(Query query);

	/**
	 * removes a Query from Stack
	 */
	public abstract void removeQuery();

	/**
	 * @return returns if stack is empty or not
	 */
	public abstract boolean isEmpty();

	/**
	 * loop over all Queries and return value at first occurrence
	 * 
	 * @param pc Page Context
	 * @param key column name of the value to get
	 * @param defaultValue default value
	 * @return value
	 * 
	 */
	public Object getDataFromACollection(PageContext pc, Key key, Object defaultValue);

	/**
	 * loop over all Queries and return value as QueryColumn at first occurrence
	 * 
	 * @param key column name of the value to get
	 * @return value
	 */
	public abstract QueryColumn getColumnFromACollection(Collection.Key key);

	/**
	 * clear the collection stack
	 */
	public abstract void clear();

	/**
	 * @return returns all queries in the stack
	 */
	public Query[] getQueries();

	public QueryStack duplicate(boolean deepCopy);

}