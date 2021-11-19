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
package lucee.runtime.interpreter.ref;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

/**
 * Define a Reference to an Object
 */
public interface Ref {

	/**
	 * return the value for that the reference is for
	 * 
	 * @return value to reference
	 * @throws PageException
	 */
	public Object touchValue(PageContext pc) throws PageException;

	/**
	 * return the value for that the reference is for
	 * 
	 * @return value to reference
	 * @throws PageException
	 */
	public Object getValue(PageContext pc) throws PageException;

	/**
	 * return the value for that the reference is for
	 * 
	 * @return value to reference
	 * @throws PageException
	 */
	public Object getCollection(PageContext pc) throws PageException;

	/**
	 * return the name name of a reference
	 * 
	 * @return type as string
	 */
	public String getTypeName();

	public boolean eeq(PageContext pc, Ref other) throws PageException;
}