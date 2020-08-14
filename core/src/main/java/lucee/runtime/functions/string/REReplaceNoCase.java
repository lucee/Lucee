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
 * Implements the CFML Function rereplacenocase
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.regex.Regex;

public final class REReplaceNoCase extends BIF implements Function {

	private static final long serialVersionUID = 3261493342788819694L;

	public static String call(PageContext pc, String string, String regExp, String replace) throws PageException {
		Regex regex = ((PageContextImpl) ThreadLocalPageContext.get()).getRegex();
		return regex.replace(string, regExp, replace, false, false);
	}

	public static String call(PageContext pc, String string, String regExp, String replace, String scope) throws PageException {
		Regex regex = ((PageContextImpl) ThreadLocalPageContext.get()).getRegex();
		if (scope.equalsIgnoreCase("all")) return regex.replaceAll(string, regExp, replace, false, false);
		return regex.replace(string, regExp, replace, false, false);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]));
		throw new FunctionException(pc, "REReplaceNoCase", 3, 4, args.length);
	}
}