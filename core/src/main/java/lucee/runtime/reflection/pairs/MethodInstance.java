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
	private boolean convertComparsion;
	private boolean nameCaseSensitive;
	private Method method;
	private boolean initMethod = true;

	public MethodInstance(Class clazz, Key methodName, Object[] args, boolean nameCaseSensitive, boolean convertComparsion) {
		this.clazz = clazz;
		this.methodName = methodName;
		this.args = Reflector.cleanArgs(args);
		this.convertComparsion = convertComparsion;
		this.nameCaseSensitive = nameCaseSensitive;
		DynamicInvoker di = DynamicInvoker.getExistingInstance();
		Clazz clazzz = di.toClazz(clazz);
		try {
			this.method = clazzz.getMethod(methodName.getString(), args, nameCaseSensitive, true, convertComparsion);
			initMethod = false;
		}
		catch (NoSuchMethodException e) {}

	}

	public Object invoke(Object o) throws Exception {
		getMethod(null);
		return method.invoke(o, args);

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

		return getMethod(null) != null;
	}

	public Method getMethod() throws PageException {
		if (method == null && initMethod) {
			DynamicInvoker di = DynamicInvoker.getExistingInstance();
			Clazz clazzz = di.toClazz(clazz);
			try {
				method = clazzz.getMethod(methodName.getString(), args, nameCaseSensitive, true, convertComparsion);
			}
			catch (Exception ex) {
				throw Caster.toPageException(ex);
			}
			finally {
				initMethod = false;
			}
		}
		return method;
	}

	public Method getMethod(Method defaultValue) {
		if (method == null && initMethod) {
			DynamicInvoker di = DynamicInvoker.getExistingInstance();
			Clazz clazzz = di.toClazz(clazz);
			try {
				method = clazzz.getMethod(methodName.getString(), args, nameCaseSensitive, true, convertComparsion);
			}
			catch (Exception ex) {
				return defaultValue;
			}
			finally {
				initMethod = false;
			}
		}
		return method == null ? defaultValue : method;
	}
}
