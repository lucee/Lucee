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

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

import lucee.print;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Collection.Key;
import lucee.transformer.direct.DynamicMethodInvoker;

/**
 * class holds a Method and the parameter to call it
 */
public final class MethodInstance {

	private Class clazz;
	private Key methodName;
	private Object[] args;
	private Pair<Executable, Object> result;

	public MethodInstance(Class clazz, Key methodName, Object[] args) {
		this.clazz = clazz;
		this.methodName = methodName;
		this.args = args;
	}

	public Object invoke(Object o)
			throws PageException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		if (o != null) {
			if ("toString".equals(methodName.getString()) && args.length == 0) {
				return o.toString();
			}
			else if ("equals".equals(methodName.getString()) && args.length == 1) {
				return o.equals(args[0]);
			}
		}
		try {
			return ((BiFunction<Object, Object, Object>) getResult().getValue()).apply(o, args);
		}
		catch (IncompatibleClassChangeError | IllegalStateException e) {
			print.e(e);
			LogUtil.log("direct", e);
			Method method = Reflector.getMethod(clazz, methodName, args, true);
			return method.invoke(o, args);
		}
	}

	/**
	 * @return Returns the args.
	 */
	public Object[] getArgs() {
		return args;
	}

	public Method getMethod() throws PageException {
		return (Method) getResult().getName();
	}

	public Method getMethod(Method defaultValue) {
		try {
			return (Method) getResult().getName();
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	private Pair<Executable, Object> getResult() throws PageException {
		if (result == null) {
			try {
				result = DynamicMethodInvoker.getInstance(null).createInstance(clazz, methodName, args);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return result;
	}
}