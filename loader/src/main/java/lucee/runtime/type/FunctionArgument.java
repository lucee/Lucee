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

/**
 * a function argument definition
 */
public interface FunctionArgument extends Serializable {

	public static final int DEFAULT_TYPE_NULL = 0;
	public static final int DEFAULT_TYPE_LITERAL = 1;
	public static final int DEFAULT_TYPE_RUNTIME_EXPRESSION = 2;

	/**
	 * @return Returns the name of the argument.
	 */
	public abstract Collection.Key getName();

	/**
	 * @return Returns if argument is required or not.
	 */
	public abstract boolean isRequired();

	/**
	 * @return Returns the type of the argument.
	 */
	public abstract short getType();

	/**
	 * @return Returns the type of the argument.
	 */
	public abstract String getTypeAsString();

	/**
	 * @return Returns the Hint of the argument.
	 */
	public abstract String getHint();

	/**
	 * @return Returns the Display name of the argument.
	 */
	public abstract String getDisplayName();

	/**
	 * @return the default type of the argument
	 */
	public int getDefaultType();

	/**
	 * @return the meta data defined
	 */
	public Struct getMetaData();

	public boolean isPassByReference();
}