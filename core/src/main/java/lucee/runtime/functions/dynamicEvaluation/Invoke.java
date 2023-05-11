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
package lucee.runtime.functions.dynamicEvaluation;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class Invoke implements Function {

	private static final long serialVersionUID = 3451409617437302246L;
	private static final Struct EMPTY = new StructImpl();

	public static Object call(PageContext pc, Object obj, String name) throws PageException {
		return call(pc, obj, name, null);
	}

	public static Object call(PageContext pc, Object obj, String name, Object arguments) throws PageException {
		if (arguments == null) arguments = EMPTY;

		if (obj instanceof String) {
			if (StringUtil.isEmpty((String) obj)) {
				if (pc.getActiveComponent() != null) obj = pc.getActiveComponent();
				else obj = pc.variablesScope();
			}
			else obj = pc.loadComponent(Caster.toString(obj));
		}

		if (Decision.isStruct(arguments)) {
			Struct args = Caster.toStruct(arguments);
			if (args == arguments && args != null) args = (Struct) args.duplicate(false);
			return pc.getVariableUtil().callFunctionWithNamedValues(pc, obj, KeyImpl.init(name), args);
		}
		Object[] args = Caster.toNativeArray(arguments);
		if (args == arguments && args != null) {
			Object[] tmp = new Object[args.length];
			for (int i = 0; i < args.length; i++) {
				tmp[i] = args[i];
			}
			args = tmp;
		}
		return pc.getVariableUtil().callFunctionWithoutNamedValues(pc, obj, KeyImpl.init(name), args);

	}

}