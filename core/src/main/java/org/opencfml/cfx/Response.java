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
package org.opencfml.cfx;

/**
 * Alternative Implementation of Jeremy Allaire's Response Interface
 */
public interface Response {

	/**
	 * adds a query to response
	 * 
	 * @param name name of the new Query
	 * @param column columns of the new Query
	 * @return created query
	 */
	public Query addQuery(String name, String[] column);

	/**
	 * sets a variable to response
	 * 
	 * @param key key of the variable
	 * @param value value of the variable
	 */
	public void setVariable(String key, String value);

	/**
	 * write out a String to response
	 * 
	 * @param str String to write
	 */
	public void write(String str);

	/**
	 * write out if debug is enabled
	 * 
	 * @param str String to write
	 */
	public void writeDebug(String str);

}