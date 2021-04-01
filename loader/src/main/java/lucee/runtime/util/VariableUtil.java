/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.util;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;

/**
 * Variable Util
 */
public interface VariableUtil {

	/**
	 * return a property from the given Object, when property doesn't exists return null
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @param defaultValue default value
	 * @return value or null
	 * @deprecated use instead
	 */
	@Deprecated
	public abstract Object getCollection(PageContext pc, Object coll, String key, Object defaultValue);

	/**
	 * return a property from the given Object, when property doesn't exists return null
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @param defaultValue default value
	 * @return value or null
	 */
	public Object getCollection(PageContext pc, Object coll, Collection.Key key, Object defaultValue);

	/**
	 * return a property from the given Object, when property doesn't exists return null
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @param defaultValue default value
	 * @return value or null
	 * @deprecated use instead
	 *             <code>get(PageContext pc, Object coll, Collection.Key key, Object defaultValue);</code>
	 */
	@Deprecated
	public abstract Object get(PageContext pc, Object coll, String key, Object defaultValue);

	/**
	 * return a property from the given Object, when property doesn't exists return null
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @param defaultValue default value
	 * @return value or null
	 */
	public abstract Object get(PageContext pc, Object coll, Collection.Key key, Object defaultValue);

	/**
	 * return a property from the given Object, when property doesn't exists return null
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @param defaultValue default value
	 * @return value or null
	 */
	public abstract Object getLight(PageContext pc, Object coll, String key, Object defaultValue);

	/**
	 * return a property from the given Object, when coll is a query return a Column,when property
	 * doesn't exists throw exception
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @return value value to get
	 * @throws PageException Page Context
	 */
	public abstract Object getCollection(PageContext pc, Object coll, String key) throws PageException;

	/**
	 * return a property from the given Object, when property doesn't exists throw exception
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @return value value to get
	 * @throws PageException Page Context
	 */
	public abstract Object get(PageContext pc, Object coll, String key) throws PageException;

	/**
	 * sets a value to the Object
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @param value Value to set
	 * @return value setted
	 * @throws PageException Page Context
	 * @deprecated use instead
	 *             <code>set(PageContext pc, Object coll, Collection.Key key,Object value)</code>
	 */
	@Deprecated
	public Object set(PageContext pc, Object coll, String key, Object value) throws PageException;

	public Object set(PageContext pc, Object coll, Collection.Key key, Object value) throws PageException;

	/**
	 * sets a value to the Object
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @param value Value to set
	 * @return value setted or null if can't set
	 * @deprecated use instead
	 *             <code>setEL(PageContext pc, Object coll, Collection.Key key,Object value);</code>
	 */
	@Deprecated
	public abstract Object setEL(PageContext pc, Object coll, String key, Object value);

	/**
	 * sets a value to the Object
	 * 
	 * @param pc Page Context
	 * @param coll Collection to check
	 * @param key to get from Collection
	 * @param value Value to set
	 * @return value setted or null if can't set
	 */
	public abstract Object setEL(PageContext pc, Object coll, Collection.Key key, Object value);

	/**
	 * remove value from Collection
	 * 
	 * @param coll Collection
	 * @param key key 
	 * @return has cleared or not
	 */
	@Deprecated
	public abstract Object removeEL(Object coll, String key);

	public abstract Object removeEL(Object coll, Collection.Key key);

	/**
	 * clear value from Collection
	 * 
	 * @param coll Collection
	 * @param key key 
	 * @return has cleared or not
	 * @throws PageException Page Context
	 */
	@Deprecated
	public abstract Object remove(Object coll, String key) throws PageException;

	public abstract Object remove(Object coll, Collection.Key key) throws PageException;

	/**
	 * call a Function (UDF, Method) with or witout named values
	 * 
	 * @param pc Page Context
	 * @param coll Collection of the UDF Function
	 * @param key name of the function
	 * @param args arguments to call the function
	 * @return return value of the function
	 * @throws PageException Page Context
	 */
	public abstract Object callFunction(PageContext pc, Object coll, String key, Object[] args) throws PageException;

	/**
	 * call a Function (UDF, Method) without Named Values
	 * 
	 * @param pc Page Context
	 * @param coll Collection of the UDF Function
	 * @param key name of the function
	 * @param args arguments to call the function
	 * @return return value of the function
	 * @throws PageException Page Context
	 * @deprecated use instead
	 *             <code>callFunctionWithoutNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args)</code>
	 */
	@Deprecated
	public abstract Object callFunctionWithoutNamedValues(PageContext pc, Object coll, String key, Object[] args) throws PageException;

	/**
	 * call a Function (UDF, Method) without Named Values
	 * 
	 * @param pc Page Context
	 * @param coll Collection of the UDF Function
	 * @param key name of the function
	 * @param args arguments to call the function
	 * @return return value of the function
	 * @throws PageException Page Context
	 */
	public Object callFunctionWithoutNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args) throws PageException;

	/**
	 * call a Function (UDF, Method) with Named Values
	 * 
	 * @param pc Page Context
	 * @param coll Collection of the UDF Function
	 * @param key name of the function
	 * @param args arguments to call the function
	 * @return return value of the function
	 * @throws PageException Page Context
	 * @deprecated use instead
	 *             <code>callFunctionWithNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args)</code>
	 */
	@Deprecated
	public abstract Object callFunctionWithNamedValues(PageContext pc, Object coll, String key, Object[] args) throws PageException;

	/**
	 * call a Function (UDF, Method) with Named Values
	 * 
	 * @param pc Page Context
	 * @param coll Collection of the UDF Function
	 * @param key name of the function
	 * @param args arguments to call the function
	 * @return return value of the function
	 * @throws PageException Page Context
	 */
	public Object callFunctionWithNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args) throws PageException;

	public Object callFunctionWithNamedValues(PageContext pc, Object coll, Collection.Key key, Struct args) throws PageException;

}