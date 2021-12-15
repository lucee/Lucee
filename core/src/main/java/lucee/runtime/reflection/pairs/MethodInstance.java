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
package lucee.runtime.reflection.pairs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * class holds a Method and the parameter to call it
 */
public final class MethodInstance {

	private Method method;
	private Object[] args;

	/**
	 * constructor of the class
	 * 
	 * @param method
	 * @param args
	 */
	public MethodInstance(Method method, Object[] args) {
		this.method = method;
		this.args = args;
		method.setAccessible(true);
	}

	/**
	 * Invokes the method
	 * 
	 * @param o Object to invoke Method on it
	 * @return return value of the Method
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InvocationTargetException
	 */
	public Object invoke(Object o) throws IllegalAccessException, InvocationTargetException {
		return method.invoke(o, args);
	}

	/**
	 * @return Returns the args.
	 */
	public Object[] getArgs() {
		return args;
	}

	/**
	 * @return Returns the method.
	 */
	public Method getMethod() {
		return method;
	}

	public void setAccessible(boolean b) {
		method.setAccessible(b);
	}
}