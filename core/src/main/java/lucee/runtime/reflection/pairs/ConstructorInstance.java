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
import lucee.commons.lang.Pair;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.ConstructorReflection;
import lucee.transformer.dynamic.meta.FunctionMember;

/**
 * class holds a Constructor and the parameter to call it
 */
public final class ConstructorInstance {

	private Class clazz;
	private Object[] args;
	private Pair<FunctionMember, Object> result;

	/**
	 * constructor of the class
	 * 
	 * @param constructor
	 * @param args
	 */
	public ConstructorInstance(Class clazz, Object[] args) {
		this.clazz = clazz;
		this.args = args;
	}

	public Object invoke() throws PageException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException, IOException {

		try {
			return ((BiFunction<Object, Object, Object>) getResult().getValue()).apply(null, args);
		}
		catch (IncompatibleClassChangeError | IllegalStateException e) {
			LogUtil.log("direct", e);

			DynamicInvoker di = DynamicInvoker.getInstance(null);
			lucee.transformer.dynamic.meta.Constructor constr = Clazz.getConstructorMatch(di.getClazz(clazz, true), args, true);
			return ((ConstructorReflection) constr).getConstructor().newInstance(args);
		}
	}

	/**
	 * @return Returns the args.
	 */
	public Object[] getArgs() {
		return args;
	}

	public Constructor getConstructor() throws PageException {
		return (Constructor) getResult().getName();
	}

	public Constructor getConstructor(Constructor defaultValue) {
		try {
			return (Constructor) getResult().getName();
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	private Pair<FunctionMember, Object> getResult() throws PageException {
		if (result == null) {
			try {
				result = DynamicInvoker.getInstance(null).createInstance(clazz, null, args);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return result;
	}
}