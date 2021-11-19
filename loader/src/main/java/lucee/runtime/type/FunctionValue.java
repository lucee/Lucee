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
package lucee.runtime.type;

import java.io.Serializable;

import lucee.runtime.dump.Dumpable;
import lucee.runtime.op.Castable;

/**
 * represent a named function value for a functions
 */
public interface FunctionValue extends Castable, Serializable, Dumpable {

	/**
	 * @return Returns the name.
	 * @deprecated use instead <code>getNameAsString();</code>
	 */
	@Deprecated
	public abstract String getName();

	/**
	 * @return Returns the name as string
	 */
	public String getNameAsString();

	/**
	 * @return Returns the name as key
	 */
	public Collection.Key getNameAsKey();

	/**
	 * @return Returns the value.
	 */
	public abstract Object getValue();

}