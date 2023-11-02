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

import java.lang.reflect.Method;

/**
 * Hold a pair of method and parameter to invoke
 */
public final class MethodParameterPair {

	private Method method;
	private Object[] parameters;

	/**
	 * constructor of the pair Object
	 * 
	 * @param method
	 * @param parameters
	 */
	public MethodParameterPair(Method method, Object[] parameters) {
		this.method = method;
		this.parameters = parameters;
		method.setAccessible(true);
	}

	/**
	 * returns the Method
	 * 
	 * @return returns the Method
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * returns the Parameters
	 * 
	 * @return returns the Parameters
	 */
	public Object[] getParameters() {
		return parameters;
	}

}