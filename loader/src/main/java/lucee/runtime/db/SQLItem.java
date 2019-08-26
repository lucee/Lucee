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

import lucee.runtime.exp.PageException;

/**
 * an Item of a SQL Statement
 */
public interface SQLItem {

	/**
	 * @return Returns the nulls.
	 */
	public abstract boolean isNulls();

	/**
	 * @param nulls The nulls to set.
	 */
	public abstract void setNulls(boolean nulls);

	/**
	 * @return Returns the scale.
	 */
	public abstract int getScale();

	/**
	 * @param scale The scale to set.
	 */
	public abstract void setScale(int scale);

	/**
	 * @return Returns the value.
	 */
	public abstract Object getValue();

	/**
	 * @param value The value to set.
	 */
	public abstract void setValue(Object value);

	/**
	 * @return Returns the cfsqltype.
	 */
	public abstract int getType();

	/**
	 * @param type The cfsqltype to set.
	 */
	public abstract void setType(int type);

	/**
	 * @param object
	 * @return cloned SQL Item
	 */
	public abstract SQLItem clone(Object object);

	/**
	 * @return CF compatible Type
	 * @throws PageException
	 */
	public abstract Object getValueForCF() throws PageException;

	/**
	 * @return Returns the isValueSet.
	 */
	public abstract boolean isValueSet();

}