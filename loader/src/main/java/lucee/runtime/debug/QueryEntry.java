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
package lucee.runtime.debug;

import java.io.Serializable;

import lucee.runtime.db.SQL;
import lucee.runtime.type.Query;

/**
 * a single query entry
 */
public interface QueryEntry extends Serializable {

	/**
	 * @return return the query execution time in mili seconds
	 * @deprecated use instead <code>getExecutionTime()</code>
	 */
	@Deprecated
	public abstract int getExe();

	/**
	 * @return return the query execution time in nanoseconds
	 */
	public long getExecutionTime();

	/**
	 * @return Returns the query.
	 */
	public abstract SQL getSQL();

	/**
	 * return the query of this entry (can be null, if the query has not produced a resultset)
	 * 
	 * @return
	 */
	public Query getQry();

	/**
	 * @return Returns the src.
	 */
	public abstract String getSrc();

	/**
	 * @return Returns the name.
	 */
	public abstract String getName();

	/**
	 * @return Returns the recordcount.
	 */
	public abstract int getRecordcount();

	/**
	 * @return Returns the datasource.
	 */
	public abstract String getDatasource();

	public long getStartTime();

	public String getCacheType();

}