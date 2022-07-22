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
package lucee.runtime.type.scope;

import java.util.Set;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;

/**
 * interface for Argument scope
 */
public interface Argument extends Scope, Array, BindScope {

	/**
	 * sets if scope is bound to another variable for using outside of a udf
	 * 
	 * @param bind bind
	 */
	@Override
	public abstract void setBind(boolean bind);

	/**
	 * @return returns if scope is bound to another variable for using outside of a udf
	 */
	@Override
	public abstract boolean isBind();

	/**
	 * insert a key in argument scope at defined position
	 * 
	 * @param index index
	 * @param key key
	 * @param value value
	 * @return boolean
	 * @throws PageException Page Exception
	 */
	public abstract boolean insert(int index, String key, Object value) throws PageException;

	public Object setArgument(Object obj) throws PageException;

	public static final Object NULL = null;

	public Object getFunctionArgument(String key, Object defaultValue);

	public Object getFunctionArgument(Collection.Key key, Object defaultValue);

	public void setFunctionArgumentNames(Set<Collection.Key> functionArgumentNames);

	public boolean containsFunctionArgumentKey(Key key);

}