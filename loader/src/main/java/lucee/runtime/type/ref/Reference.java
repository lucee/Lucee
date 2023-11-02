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
package lucee.runtime.type.ref;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;

/**
 * 
 */
public interface Reference {

	/**
	 * @return returns the value of the Variable
	 * @throws PageException
	 * @deprecated use instead <code>{@link #getKey()}</code>
	 */
	@Deprecated
	public abstract String getKeyAsString() throws PageException;

	/**
	 * @return returns the value of the Variable
	 * @throws PageException
	 */
	public abstract Collection.Key getKey() throws PageException;

	/**
	 * @param pc PageContext of the current Request
	 * @return returns the value of the Variable
	 * @throws PageException
	 */
	public abstract Object get(PageContext pc) throws PageException;

	/**
	 * @param pc PageContext of the current Request
	 * @param defaultValue default value
	 * @return returns the value of the Variable
	 */
	public abstract Object get(PageContext pc, Object defaultValue);

	/**
	 * @param pc PageContext of the current Request
	 * @param value resets the value of the variable
	 * @return new Value set
	 * @throws PageException
	 */
	public abstract Object set(PageContext pc, Object value) throws PageException;

	/**
	 * @param pc PageContext of the current Request
	 * @param value resets the value of the variable
	 * @return new value set
	 */
	public abstract Object setEL(PageContext pc, Object value);

	/**
	 * clears the variable from collection
	 * 
	 * @param pc
	 * @return removed Object
	 * @throws PageException
	 */
	public abstract Object remove(PageContext pc) throws PageException;

	/**
	 * clears the variable from collection
	 * 
	 * @param pc
	 * @return removed Object
	 */
	public abstract Object removeEL(PageContext pc);

	/**
	 * create it when not exist
	 * 
	 * @param pc
	 * @return removed Object
	 * @throws PageException
	 */
	public abstract Object touch(PageContext pc) throws PageException;

	/**
	 * create it when not exist
	 * 
	 * @param pc
	 * @return removed Object
	 */
	public abstract Object touchEL(PageContext pc);

	/**
	 * @return returns the collection
	 */
	public abstract Object getParent();
}