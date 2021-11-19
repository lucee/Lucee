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
package lucee.runtime.db;

/**
 * represent a SQL Statement
 */
public interface SQL {

	/**
	 * @return Returns the items.
	 */
	public abstract SQLItem[] getItems();

	/**
	 * @return Returns the position.
	 */
	public abstract int getPosition();

	/**
	 * @param position The position to set.
	 */
	public abstract void setPosition(int position);

	/**
	 * @return returns the pure SQL String
	 */
	public abstract String getSQLString();

	/**
	 * @param strSQL sets the SQL String
	 */
	public abstract void setSQLString(String strSQL);

	/**
	 * @return returns Unique String for Hash
	 */
	public abstract String toHashString();

}