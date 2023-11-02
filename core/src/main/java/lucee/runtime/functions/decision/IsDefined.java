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
/**
 * Implements the CFML Function isdefined
 */
package lucee.runtime.functions.decision;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.ext.function.Function;
import lucee.runtime.interpreter.VariableInterpreter;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Null;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.util.VariableUtilImpl;

public final class IsDefined implements Function {

	private static final long serialVersionUID = -6477602189364145523L;

	public static boolean call(PageContext pc, String varName) {
		return VariableInterpreter.isDefined(pc, varName);
		// return pc.isDefined(varName);
	}

	public static boolean call(PageContext pc, double scope, Collection.Key key) {
		try {
			Object coll = VariableInterpreter.scope(pc, (int) scope, false);
			if (coll == null) return false;
			Object _null = NullSupportHelper.NULL(pc);
			coll = ((VariableUtilImpl) pc.getVariableUtil()).get(pc, coll, key, _null);
			if (coll == _null) return false;
			// return pc.scope((int)scope).get(key,null)!=null;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return false;
		}
		return true;
	}

	public static boolean call(PageContext pc, double scope, Collection.Key[] varNames) {
		Object defVal = NullSupportHelper.NULL(pc);
		try {
			Object coll = VariableInterpreter.scope(pc, (int) scope, false);
			// Object coll =pc.scope((int)scope);
			VariableUtilImpl vu = ((VariableUtilImpl) pc.getVariableUtil());
			for (int i = 0; i < varNames.length; i++) {
				coll = vu.getCollection(pc, coll, varNames[i], defVal);
				if (coll == defVal) return false;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return false;
		}
		return true;
	}

	// used for older compiled code in ra files
	public static boolean invoke(PageContext pc, String[] varNames, boolean allowNull) {
		int scope = VariableInterpreter.scopeString2Int(pc.ignoreScopes(), varNames[0]);

		Object defVal = allowNull ? Null.NULL : null;
		try {
			Object coll = VariableInterpreter.scope(pc, scope, false);
			// Object coll =pc.scope((int)scope);
			for (int i = scope == Scope.SCOPE_UNDEFINED ? 0 : 1; i < varNames.length; i++) {
				coll = pc.getVariableUtil().getCollection(pc, coll, varNames[i], defVal);
				if (coll == defVal) return false;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return false;
		}
		return true;
	}

	// used for older compiled code in ra files
	public static boolean call(PageContext pc, double scope, String key) {
		return call(pc, scope, KeyImpl.getInstance(key));
	}

	// used for older compiled code in ra files
	public static boolean call(PageContext pc, double scope, String[] varNames) {
		return call(pc, scope, KeyImpl.toKeyArray(varNames));
	}
}