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
package lucee.runtime.orm.naming;

public interface NamingStrategy {

	/**
	 * Defines the table name to be used for a specified table name. The specified table name is either
	 * the table name specified in the mapping or chosen using the entity name.
	 */
	public String convertTableName(String tableName);

	/**
	 * Defines the column name to be used for a specified column name. The specified column name is
	 * either the column name specified in the mapping or chosen using the property name.
	 */
	public String convertColumnName(String columnName);

	public String getType();

}