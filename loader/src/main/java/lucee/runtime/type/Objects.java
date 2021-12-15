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
package lucee.runtime.type;

import lucee.runtime.PageContext;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Castable;

/**
 * Hold a native or wild object, to use id inside lucee runtime
 */
public interface Objects extends Dumpable, Castable {

	/**
	 * return property
	 * 
	 * @param pc PageContext
	 * @param key Name of the Property
	 * @return return value of the Property
	 */
	public Object get(PageContext pc, Collection.Key key, Object defaultValue);

	/**
	 * return property or getter of the ContextCollection
	 * 
	 * @param pc PageContext
	 * @param key Name of the Property
	 * @return return value of the Property
	 * @throws PageException
	 */
	public Object get(PageContext pc, Collection.Key key) throws PageException;

	/**
	 * sets a property (Data Member) value of the object
	 * 
	 * @param pc
	 * @param propertyName property name to set
	 * @param value value to insert
	 * @return value set to property
	 * @throws PageException
	 */
	public Object set(PageContext pc, Collection.Key propertyName, Object value) throws PageException;

	/**
	 * sets a property (Data Member) value of the object
	 * 
	 * @param pc
	 * @param propertyName property name to set
	 * @param value value to insert
	 * @return value set to property
	 */
	public Object setEL(PageContext pc, Collection.Key propertyName, Object value);

	/**
	 * calls a method of the object
	 * 
	 * @param pc
	 * @param methodName name of the method to call
	 * @param arguments arguments to call method with
	 * @return return value of the method
	 * @throws PageException
	 */
	public Object call(PageContext pc, Collection.Key methodName, Object[] arguments) throws PageException;

	/**
	 * call a method of the Object with named arguments
	 * 
	 * @param pc PageContext
	 * @param methodName name of the method
	 * @param args Named Arguments for the method
	 * @return return result of the method
	 * @throws PageException
	 */
	public abstract Object callWithNamedValues(PageContext pc, Collection.Key methodName, Struct args) throws PageException;
}