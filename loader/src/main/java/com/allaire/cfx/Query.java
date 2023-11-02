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
package com.allaire.cfx;

import java.sql.ResultSet;

import lucee.runtime.type.Collection;

/**
 * Alternative Implementation of Jeremy Allaire's Query Interface
 */
public interface Query extends ResultSet {

	/**
	 * @return adds a row to resultset
	 */
	public int addRow();

	/**
	 * returns index of a columnName
	 * 
	 * @param coulmnName column name to get index for
	 * @return index of a columnName
	 */
	public int getColumnIndex(String coulmnName);

	/**
	 * @return All column Names of resultset as string
	 * @deprecated use instead <code>getColumnNamesAsString()</code>
	 */
	@Deprecated
	public String[] getColumns();

	public String[] getColumnNamesAsString();

	public Collection.Key[] getColumnNames();

	/**
	 * returns one field of a Query as String
	 * 
	 * @param row row number to get
	 * @param col column number to get
	 * @return data from query object
	 * @throws IndexOutOfBoundsException thrown when col or/and row are invalid
	 */
	public String getData(int row, int col) throws IndexOutOfBoundsException;

	/**
	 * @return returns name of the query
	 */
	public String getName();

	/**
	 * @return returns row count
	 */
	public int getRowCount();

	/**
	 * sets value at a defined position in Query
	 * 
	 * @param row row number to set
	 * @param col column number to set
	 * @param value value to set
	 * @throws IndexOutOfBoundsException thrown when col or/and row are invalid
	 */
	public void setData(int row, int col, String value) throws IndexOutOfBoundsException;

}