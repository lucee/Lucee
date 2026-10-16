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
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.Clazz;
import lucee.transformer.dynamic.meta.Constructor;

/**
 * class holds a Constructor and the parameter to call it
 */
public final class ConstructorInstance {

	private Class clazz;
	private Object[] args;
	private Constructor constr;
	private boolean initConstr = true;

	private boolean convertComparsion;

	/**
	 * constructor of the class
	 * 
	 * @param clazz
	 * @param args
	 */
	public ConstructorInstance(Class clazz, Object[] args, boolean convertComparsion) {
		this.clazz = clazz;
		this.args = Reflector.cleanArgs(args);
		this.convertComparsion = convertComparsion;
	}

	public Object invoke() throws Exception {
		getConstructor();
		return constr.newInstance(args);

	}

	/**
	 * @return Returns the args.
	 */
	public Object[] getArgs() {
		return args;
	}

	public Constructor getConstructor(Constructor defaultValue) {
		if (constr == null && initConstr) {
			try {
				DynamicInvoker di = DynamicInvoker.getExistingInstance();
				Clazz clazzz = di.toClazz(clazz);
				constr = clazzz.getConstructor(args, true, convertComparsion);
			}
			catch (Exception ex) {
				return defaultValue;
			}
			finally {
				initConstr = false;
			}
		}
		return constr == null ? defaultValue : constr;
	}

	private Constructor getConstructor() throws PageException {
		if (constr == null && initConstr) {
			try {
				DynamicInvoker di = DynamicInvoker.getExistingInstance();
				Clazz clazzz = di.toClazz(clazz);
				constr = clazzz.getConstructor(args, true, convertComparsion);
			}
			catch (Exception ex) {
				throw Caster.toPageException(ex);
			}
			finally {
				initConstr = false;
			}
		}
		return constr;
	}

}