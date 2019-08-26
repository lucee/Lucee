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
package lucee.runtime.type;

import java.util.Map;

import lucee.runtime.db.SQL;
import lucee.runtime.exp.PageException;

/**
 * interface for resultset (query) object
 */
public interface Query extends Collection, Iterator, com.allaire.cfx.Query {

	/**
	 * Constant <code>ORDER_ASC</code>, used for method sort
	 */
	public static final int ORDER_ASC = 1;

	/**
	 * Constant <code>ORDER_DESC</code>, used for method sort
	 */
	public static final int ORDER_DESC = 2;

	/**
	 * @return return how many lines are affected by an update/insert
	 */
	public int getUpdateCount();

	/**
	 * return a value of the resultset by specified column and row
	 * 
	 * @param key column to get
	 * @param row row to get from (1-recordcount)
	 * @return value at the called position
	 * @throws PageException if invalid position definition
	 * @deprecated use instead <code>{@link #getAt(lucee.runtime.type.Collection.Key, int)}</code>
	 */
	@Deprecated
	public Object getAt(String key, int row) throws PageException;

	/**
	 * return a value of the resultset by specified column and row
	 * 
	 * @param key column to get
	 * @param row row to get from (1-recordcount)
	 * @return value at the called position
	 * @throws PageException if invalid position definition
	 */
	public Object getAt(Collection.Key key, int row) throws PageException;

	/**
	 * return a value of the resultset by specified column and row, otherwise to getAt this method throw
	 * no exception if value dont exist (return null)
	 * 
	 * @param key column to get
	 * @param row row to get from (1-recordcount)
	 * @param defaultValue default value returned in case there is no value
	 * @return value at the called position
	 * @deprecated use instead
	 *             <code>{@link #getAt(lucee.runtime.type.Collection.Key, int, Object)}</code>
	 */
	@Deprecated
	public Object getAt(String key, int row, Object defaultValue);

	/**
	 * return a value of the resultset by specified column and row, otherwise return defaultValue
	 * 
	 * @param key column to get
	 * @param row row to get from (1-recordcount)
	 * @param defaultValue value returned in case row or column does not exist
	 * @return value at the called position
	 */
	public Object getAt(Collection.Key key, int row, Object defaultValue);

	/**
	 * set a value at the defined position
	 * 
	 * @param key column to set
	 * @param row row to set
	 * @param value value to fill
	 * @return filled value
	 * @throws PageException thrown when fails to set the value
	 * @deprecated use instead
	 *             <code>{@link #setAtEL(lucee.runtime.type.Collection.Key, int, Object)}</code>
	 */
	@Deprecated
	public Object setAt(String key, int row, Object value) throws PageException;

	/**
	 * set a value at the defined position
	 * 
	 * @param key column to set
	 * @param row row to set
	 * @param value value to fill
	 * @return filled value
	 * @throws PageException thrown when fails to set the value
	 */
	public Object setAt(Collection.Key key, int row, Object value) throws PageException;

	/**
	 * set a value at the defined position
	 * 
	 * @param key column to set
	 * @param row row to set
	 * @param value value to fill
	 * @return filled value
	 * @deprecated use instead
	 *             <code>{@link #setAtEL(lucee.runtime.type.Collection.Key, int, Object)}</code>
	 */
	@Deprecated
	public Object setAtEL(String key, int row, Object value);

	/**
	 * set a value at the defined position
	 * 
	 * @param key column to set
	 * @param row row to set
	 * @param value value to fill
	 * @return filled value
	 */
	public Object setAtEL(Collection.Key key, int row, Object value);

	/**
	 * adds a new row to the resultset
	 * 
	 * @param count count of rows to add
	 * @return return if row is addded or nod (always true)
	 */
	public boolean addRow(int count);

	/**
	 * remove row from query
	 * 
	 * @param row row number to remove
	 * @return return new rowcount
	 * @throws PageException exception thrown when it fails to remove the row
	 */
	public int removeRow(int row) throws PageException;

	/**
	 * remove row from query
	 * 
	 * @param row row number to remove
	 * @return return new rowcount
	 */
	public int removeRowEL(int row);

	/**
	 * adds a new column to the resultset
	 * 
	 * @param columnName name of the new column
	 * @param content content of the new column inside an array (must have same size like query has
	 *            records)
	 * @return if column is added return true otherwise false (always true, throw error when false)
	 * @throws PageException exception thrown when not able to add the column
	 * @deprecated use instead <code>{@link #addColumn(lucee.runtime.type.Collection.Key, Array)}</code>
	 */
	@Deprecated
	public boolean addColumn(String columnName, Array content) throws PageException;

	/**
	 * adds a new column to the resultset
	 * 
	 * @param columnName name of the new column
	 * @param content content of the new column inside an array (must have same size like query has
	 *            records)
	 * @return if column is added return true otherwise false (always true, throw error when false)
	 * @throws PageException exception thrown when not able to add the column
	 */
	public boolean addColumn(Collection.Key columnName, Array content) throws PageException;

	/**
	 * adds a new column to the resultset
	 * 
	 * @param columnName name of the new column
	 * @param content content of the new column inside an array (must have same size like query has
	 *            records)
	 * @param type data type from (java.sql.Types)
	 * @return if column is added return true otherwise false (always true, throw error when false)
	 * @throws PageException exception thrown when not able to add the column
	 * @deprecated use instead
	 *             <code>{@link #addColumn(lucee.runtime.type.Collection.Key, Array, int)}</code>
	 */
	@Deprecated
	public boolean addColumn(String columnName, Array content, int type) throws PageException;

	/**
	 * adds a new column to the resultset
	 * 
	 * @param columnName name of the new column
	 * @param content content of the new column inside an array (must have same size like query has
	 *            records)
	 * @param type data type from (java.sql.Types)
	 * @return if column is added return true otherwise false (always true, throw error when false)
	 * @throws PageException exception thrown when not able to add the column
	 */
	public boolean addColumn(Collection.Key columnName, Array content, int type) throws PageException;

	/**
	 * @return Coloned Object
	 */
	@Override
	public Object clone();

	/**
	 * @return return all types
	 */
	public int[] getTypes();

	/**
	 * @return returns all types as Map (key==column)
	 */
	public Map<Collection.Key, String> getTypesAsMap();

	/**
	 * return the query column matching to key
	 * 
	 * @param key key to get
	 * @return QueryColumn object
	 * @throws PageException exception thrown in case there is no column with that name
	 * @deprecated use instead <code>{@link #getColumn(lucee.runtime.type.Collection.Key)}</code>
	 */
	@Deprecated
	public QueryColumn getColumn(String key) throws PageException;

	/**
	 * return the query column matching to key
	 * 
	 * @param key key to get
	 * @return QueryColumn object
	 * @throws PageException exception thrown in case there is no column with that name
	 */
	public QueryColumn getColumn(Collection.Key key) throws PageException;

	/**
	 * return the query column matching to key, if key not exist return null
	 * 
	 * @param key key to get
	 * @param column default value returned in case there is no matching column
	 * @return QueryColumn object
	 * @deprecated use instead
	 *             <code>{@link #getColumn(lucee.runtime.type.Collection.Key, QueryColumn)}</code>
	 */
	@Deprecated
	public QueryColumn getColumn(String key, QueryColumn column);

	/**
	 * return the query column matching to key, if key not exist return null
	 * 
	 * @param key key to get
	 * @param column default value returned in case there is no matching column
	 * @return QueryColumn object
	 */
	public QueryColumn getColumn(Collection.Key key, QueryColumn column);

	/**
	 * remove column matching to key
	 * 
	 * @param key key to remove
	 * @return QueryColumn object removed
	 * @throws PageException thrown when fail to remove column
	 * @deprecated use instead <code>{@link #removeColumn(lucee.runtime.type.Collection.Key)}</code>
	 */
	@Deprecated
	public QueryColumn removeColumn(String key) throws PageException;

	/**
	 * remove column matching to key
	 * 
	 * @param key key to remove
	 * @return QueryColumn object removed
	 * @throws PageException thrown when fail to remove column
	 */
	public QueryColumn removeColumn(Collection.Key key) throws PageException;

	/**
	 * remove column matching to key
	 * 
	 * @param key key to remove
	 * @return QueryColumn object removed or null if column not exist
	 * @deprecated use instead <code>{@link #removeColumnEL(lucee.runtime.type.Collection.Key)}</code>
	 */
	@Deprecated
	public QueryColumn removeColumnEL(String key);

	/**
	 * remove column matching to key
	 * 
	 * @param key key to remove
	 * @return QueryColumn object removed or null if column not exist
	 */
	public QueryColumn removeColumnEL(Collection.Key key);

	/**
	 * sets the execution Time of the query
	 * 
	 * @param l execution time
	 */
	public void setExecutionTime(long l);

	/**
	 * sorts a query by a column, direction is asc
	 * 
	 * @param column column to sort
	 * @throws PageException if fails to sort
	 * @deprecated use instead <code>{@link #sort(lucee.runtime.type.Collection.Key)}</code>
	 */
	@Deprecated
	public void sort(String column) throws PageException;

	/**
	 * sorts a query by a column, direction is asc
	 * 
	 * @param column column to sort
	 * @throws PageException if fails to sort
	 */
	public void sort(Collection.Key column) throws PageException;

	/**
	 * sorts a query by a column
	 * 
	 * @param strColumn column to sort
	 * @param order sort type (Query.ORDER_ASC or Query.ORDER_DESC)
	 * @throws PageException if fails to sort
	 * @deprecated use instead <code>{@link #sort(lucee.runtime.type.Collection.Key, int)}</code>
	 */
	@Deprecated
	public void sort(String strColumn, int order) throws PageException;

	/**
	 * sorts a query by a column
	 * 
	 * @param strColumn column to sort
	 * @param order sort type (Query.ORDER_ASC or Query.ORDER_DESC)
	 * @throws PageException if fails to sort
	 */
	public void sort(Collection.Key strColumn, int order) throws PageException;

	public String getCacheType();

	public void setCacheType(String cacheType);

	/**
	 * sets if query is form cache or not
	 * 
	 * @param isCached is cached or not
	 */
	public void setCached(boolean isCached);

	/**
	 * is query from cache or not
	 * 
	 * @return is cached or not
	 */
	public boolean isCached();

	/**
	 * @return returns struct with meta data to the query
	 */
	// public Struct getMetaData();

	/**
	 * @return returns array with meta data to the query (only column names and type)
	 */
	public Array getMetaDataSimple();

	public void rename(Collection.Key columnName, Collection.Key newColumnName) throws PageException;

	@Override
	public Collection.Key[] getColumnNames();

	@Override
	public String[] getColumnNamesAsString();

	public int getColumnCount();

	public Query getGeneratedKeys();

	public SQL getSql();

	public String getTemplate();

	/**
	 * @return return the query execution time in nanoseconds
	 */
	public long getExecutionTime();

	/**
	 * @return returns the execution time
	 * @deprecated use <code>getExecutionTime()</code> instead
	 */
	@Deprecated
	public int executionTime();

	public void enableShowQueryUsage();

}