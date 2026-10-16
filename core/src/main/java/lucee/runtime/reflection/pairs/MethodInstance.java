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

import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection.Key;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Method;

/**
 * class holds a Method and the parameter to call it
 */
public final class MethodInstance {

	private Class clazz;
	private Key methodName;
	private Object[] args;
	private Method method;

	public MethodInstance(Class clazz, Key methodName, Object[] args, boolean nameCaseSensitive, boolean convertComparsion) {
		this.clazz = clazz;
		this.methodName = methodName;
		this.args = Reflector.cleanArgs(args);
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		Clazz clazzz = di.toClazz(clazz);
		this.method = clazzz.getMethod(methodName.getString(), args, nameCaseSensitive, true, convertComparsion, null);
	}

	public Object invoke(Object o) throws Exception {
		return getMethod().invoke(o, args);
	}

	public static Object invoke(Object obj, Key methodName, Object[] args, boolean nameCaseSensitive, boolean convertComparsion) throws PageException {
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		Clazz clazzz = di.toClazz(obj.getClass());
		try {
			Method method = clazzz.getMethod(methodName.getString(), args, nameCaseSensitive, true, convertComparsion);
			return method.invoke(obj, args);
		}
		catch (Exception ex) {
			throw Caster.toPageException(ex);
		}
	}

	public boolean hasMethod() {
		if (args.length == 0 && "toString".equals(methodName.getString())) {
			return true;
		}
		else if (args.length == 1 && "equals".equals(methodName.getString())) {
			return true;
		}

		return method != null;
	}

	public Method getMethod() throws PageException {
		if (method == null) {
			throw Caster.toPageException(new NoSuchMethodException(
					"No matching method for " + clazz.getName() + "." + methodName.getString()
							+ "(" + Reflector.getDspMethods(Reflector.getClasses(args)) + ") found."));
		}
		return method;
	}

	public Method getMethod(Method defaultValue) {
		return method == null ? defaultValue : method;
	}
}