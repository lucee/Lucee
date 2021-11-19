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
 * Implements the CFML Function iscustomfunction
 */
package lucee.runtime.functions.decision;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Decision;
import lucee.runtime.type.ObjectWrap;

public final class IsCustomFunction implements Function {

	private static final long serialVersionUID = 1578909692090122692L;

	public static boolean call(PageContext pc, Object object) throws FunctionException {
		return call(pc, object, null);
	}

	public static boolean call(PageContext pc, Object object, String type) throws FunctionException {
		if (object instanceof ObjectWrap) {
			return call(pc, ((ObjectWrap) object).getEmbededObject(null), type);
		}
		// no function at all
		if (!Decision.isUserDefinedFunction(object)) return false;

		// no type we are good
		if (StringUtil.isEmpty(type, true)) return true;

		// check type
		type = type.trim();
		if ("closure".equalsIgnoreCase(type)) return Decision.isClosure(object);
		if ("lambda".equalsIgnoreCase(type)) return Decision.isLambda(object);
		if ("udf".equalsIgnoreCase(type)) return !Decision.isLambda(object) && !Decision.isClosure(object);

		throw new FunctionException(pc, "IsCustomFunction", 2, "type", "function type [" + type + "] is invalid, only the following values are valid [closure,lambda,udf]");

	}
}