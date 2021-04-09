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

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;
import lucee.runtime.type.ref.Reference;

/**
 * represent a Single column of a query object
 */
public interface QueryColumn extends Collection, Reference, Castable {

	/**
	 * removes the value but dont the index
	 * 
	 * @param row row number
	 * @return removed Object
	 * @throws PageException Page Exception
	 */
	public Object remove(int row) throws PageException;

	/**
	 * remove a row from query
	 * 
	 * @param row row number
	 * @return removed value
	 * @throws PageException Page Exception
	 */
	public Object removeRow(int row) throws PageException;

	/**
	 * removes method with int as key
	 * 
	 * @param row row number
	 * @return removed Object
	 */
	public Object removeEL(int row);

	/**
	 * get method with an int as key, return empty default value for invalid row
	 * 
	 * @param row row to get value
	 * @return row value
	 * @throws PageException Page Exceptiontion
	 * @deprecated use instead <code>get(int row, Object defaultValue)</code>
	 */
	@Deprecated
	public Object get(int row) throws PageException;

	/**
	 * return the value in this row (can be null), when row number is invalid the default value is
	 * returned
	 * 
	 * @param row row to get value
	 * @param emptyValue value returned when row does not exists or the rows value is null
	 * @return row value
	 */
	public Object get(int row, Object emptyValue);

	/**
	 * set method with an int as key
	 * 
	 * @param row row to set
	 * @param value value to set
	 * @return setted value
	 * @throws PageException Page Exceptionn
	 */
	public Object set(int row, Object value) throws PageException;

	/**
	 * adds a value to the column
	 * 
	 * @param value value to add
	 */
	public void add(Object value);

	/**
	 * setExpressionLess method with an int as key
	 * 
	 * @param row row to set
	 * @param value value to set
	 * @return setted value
	 */
	public Object setEL(int row, Object value);

	/**
	 * @param count adds count row to the column
	 */
	public void addRow(int count);

	/**
	 * @return returns the type of the Column (java.sql.Types.XYZ)
	 */
	public int getType();

	/**
	 * @return returns the type of the Column as String
	 */
	public String getTypeAsString();

	/**
	 * cuts row to defined size
	 * 
	 * @param maxrows max rows
	 */
	public void cutRowsTo(int maxrows);

}