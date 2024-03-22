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
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lucee.commons.lang.Pair;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.transformer.direct.DirectCallEngine;
import lucee.transformer.direct.PageContextDummy;

/**
 * class holds a Method and the parameter to call it
 */
public final class MethodInstance {

	private Class clazz;
	private Key methodName;
	private Object[] args;
	private Pair<Method, Object> result;

	/**
	 * constructor of the class
	 * 
	 * @param method
	 * @param args
	 * 
	 *            public MethodInstance(Method method, Object[] args) { this.method = method; this.args
	 *            = args; }
	 * @throws UnmodifiableClassException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */

	public MethodInstance(Class clazz, Key methodName, Object[] args) {
		this.clazz = clazz;
		this.methodName = methodName;
		this.args = args;
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
	 * @throws UnmodifiableClassException
	 * @throws IOException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */

	public Object invoke(Object o)
			throws PageException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		/*
		 * if (o == null && "identityHashCode".equals(methodName.getString()) && args.length == 1) {
		 * print.ds("------ identityHashCode --------"); return System.identityHashCode(args[0]); }
		 */

		if (o != null) {
			if ("toString".equals(methodName.getString()) && args.length == 0) {
				return o.toString();
			}
			else if ("equals".equals(methodName.getString()) && args.length == 1) {
				return o.equals(args[0]);
			}
		}
		PageContextDummy dummy = null;
		BIF instance = (BIF) getResult().getValue();
		try {

			if (o == null) {
				return instance.invoke(null, args);
			}
			else {
				dummy = PageContextDummy.getDummy(o);
				return instance.invoke(dummy, args);
			}

		}
		/*
		 * catch (ClassCastException e) { print.e("---------"); if (o != null)
		 * print.e(o.getClass().getName()); print.e(clazz.getName()); print.e(methodName); print.e(args);
		 * 
		 * List<String> listArgs = new ArrayList<>(); for (Object arg: args) {
		 * listArgs.add(Caster.toTypeName(arg)); } String msg; if (o == null) msg =
		 * "exception while invoking the static method [" + methodName + "] of the class [" +
		 * clazz.getName() + "] with the arguments [" + ListUtil.listToList(listArgs, ", ") + "]"; else {
		 * msg = "exception while invoking the instance method [" + methodName + "] of the class [" +
		 * clazz.getName() + "|" + o.getClass().getName() + "] with the arguments [" +
		 * ListUtil.listToList(listArgs, ", ") + "]"; }
		 * 
		 * ApplicationException ae = new ApplicationException(msg); ae.initCause(e);
		 * 
		 * throw ae; }
		 */
		finally {
			if (dummy != null) PageContextDummy.returnDummy(dummy);
		}

	}

	/**
	 * @return Returns the args.
	 */
	public Object[] getArgs() {
		return args;
	}

	public Method getMethod() throws PageException {
		return getResult().getName();
	}

	private Pair<Method, Object> getResult() throws PageException {
		if (result == null) {
			try {
				result = DirectCallEngine.getInstance(null).createInstance(clazz, methodName, args);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return result;
	}

	/**
	 * @return Returns the method.
	 * 
	 *         public Method getMethod() { return method; }
	 * 
	 *         public void setAccessible(boolean b) { method.setAccessible(b); }
	 */
}