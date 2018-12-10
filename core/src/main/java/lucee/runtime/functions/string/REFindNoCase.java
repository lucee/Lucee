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

import org.apache.oro.text.regex.MalformedPatternException;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.regex.Perl5Util;

/**
 * Implements the CFML Function refindnocase
 */
public final class REFindNoCase extends BIF {

    private static final long serialVersionUID = 1562665117076202965L;

    public static Object call(PageContext pc, String regExpr, String str) throws ExpressionException {
	return call(pc, regExpr, str, 1, false);
    }

    public static Object call(PageContext pc, String regExpr, String str, double start) throws ExpressionException {
	return call(pc, regExpr, str, start, false);
    }

    public static Object call(PageContext pc, String regExpr, String str, double start, boolean returnsubexpressions) throws ExpressionException {
	return call(pc, regExpr, str, start, returnsubexpressions, "one");
    }

    public static Object call(PageContext pc, String regExpr, String str, double start, boolean returnsubexpressions, String scope) throws ExpressionException {
	try {
	    boolean isMatchAll = scope.equalsIgnoreCase("all");
	    if (returnsubexpressions) {
		return Perl5Util.find(regExpr, str, (int) start, false, isMatchAll);
	    }
	    return Perl5Util.indexOf(regExpr, str, (int) start, false, isMatchAll);
	}
	catch (MalformedPatternException e) {
	    throw new FunctionException(pc, "reFindNoCase", 1, "regularExpression", e.getMessage());
	}
    }

    @Override
    public Object invoke(PageContext pc, Object[] args) throws PageException {
	if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
	if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]));
	if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]), Caster.toBooleanValue(args[3]));

	throw new FunctionException(pc, "REFindNoCase", 2, 4, args.length);
    }
}
