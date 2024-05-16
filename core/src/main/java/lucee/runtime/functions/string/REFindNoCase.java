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

package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.regex.Regex;

/**
 * Implements the CFML Function refindnocase
 */
public final class REFindNoCase extends BIF {

	private static final long serialVersionUID = 1562665117076202965L;

	public static Object call(PageContext pc, String regExpr, String str) throws PageException {
		return call(pc, regExpr, str, 1, false, null, false);
	}

	public static Object call(PageContext pc, String regExpr, String str, double start) throws PageException {
		return call(pc, regExpr, str, start, false, null, false);
	}

	public static Object call(PageContext pc, String regExpr, String str, double start, boolean returnsubexpressions) throws PageException {
		return call(pc, regExpr, str, start, returnsubexpressions, null, false);
	}

	public static Object call(PageContext pc, String regExpr, String str, double start, boolean returnsubexpressions, String scope) throws PageException {
		return call(pc, regExpr, str, start, returnsubexpressions, scope, false);
	}

	public static Object call(PageContext pc, String regExpr, String str, double start, boolean returnsubexpressions, String scope, boolean multiline) throws PageException {
		Regex regex = ((PageContextImpl) pc).getRegex();
		boolean isMatchAll = scope == null ? false : scope.equalsIgnoreCase("all");
		if (returnsubexpressions) {
			if (isMatchAll) return regex.findAll(regExpr, str, (int) start, false, multiline);
			return regex.find(regExpr, str, (int) start, false, multiline);
		}
		if (isMatchAll) return regex.indexOfAll(regExpr, str, (int) start, false, multiline);
		return regex.indexOf(regExpr, str, (int) start, false, multiline);

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]));
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]), Caster.toBooleanValue(args[3]));
		if (args.length == 5)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]), Caster.toBooleanValue(args[3]), Caster.toString(args[4]));
		if (args.length == 6) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]), Caster.toBooleanValue(args[3]),
				Caster.toString(args[4]), Caster.toBooleanValue(args[5]));

		throw new FunctionException(pc, "REFindNoCase", 2, 6, args.length);
	}
}
