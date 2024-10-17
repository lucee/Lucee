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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiFunction;

import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.FunctionMember;
import lucee.transformer.dynamic.meta.LegacyMethod;
import lucee.transformer.dynamic.meta.Method;

/**
 * class holds a Method and the parameter to call it
 */
public final class MethodInstance {

	private Class clazz;
	private Key methodName;
	private Object[] args;
	private Pair<FunctionMember, Object> result;

	public MethodInstance(Class clazz, Key methodName, Object[] args) {
		this.clazz = clazz;
		this.methodName = methodName;
		this.args = args;
	}

	public Object invoke(Object o) throws PageException, NoSuchMethodException, IOException {

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
		catch (LinkageError | ClassCastException e) { // java.lang.ClassCastException
			if (!Clazz.allowReflection()) throw e;
			LogUtil.log("dynamic", e);
			DynamicInvoker di = DynamicInvoker.getExistingInstance();
			lucee.transformer.dynamic.meta.Method method = Clazz.getMethodMatch(di.getClazz(clazz, true), methodName, args, true);
			try {
				return ((LegacyMethod) method).getMethod().invoke(o, args);
			}
			catch (Exception e1) {
				if (e1 instanceof InvocationTargetException) {
					Throwable t = ((InvocationTargetException) e1).getTargetException();
					ExceptionUtil.initCauseEL(e, t);
					throw e;
				}
				ExceptionUtil.initCauseEL(e, e1);
				throw e;
			}
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

	public boolean hasMethod() {
		try {
			FunctionMember fm = getResult().getName();
			return fm != null;
		}
		catch (PageException e) {
			return false;
		}
	}

	private Pair<FunctionMember, Object> getResult() throws PageException {
		if (result == null) {
			try {
				result = DynamicInvoker.getExistingInstance().createInstance(clazz, methodName, args);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw Caster.toPageException(t);
			}
		}
		return result;
	}
}