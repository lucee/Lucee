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
 * Implements the CFML Function replace
 */
package lucee.runtime.functions.string;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public final class Replace extends BIF {

	private static final long serialVersionUID = -313884944032266348L;

	public static String call(PageContext pc, String str, String sub1, String sub2) throws PageException {// old
		return _call(pc, str, sub1, sub2, true);
	}

	public static String call(PageContext pc, String str, String sub1, Object sub2) throws PageException {
		return _call(pc, str, sub1, sub2, true);
	}

	public static String call(PageContext pc, String str, String sub1, String sub2, String scope) throws PageException {// old
		return _call(pc, str, sub1, sub2, !scope.equalsIgnoreCase("all"));
	}

	public static String call(PageContext pc, String str, String sub1, Object sub2, String scope) throws PageException {
		return _call(pc, str, sub1, sub2, !scope.equalsIgnoreCase("all"));
	}

	public static String call(PageContext pc, String input, Object find, String repl, String scope) throws PageException {
		return _call(pc, input, find, repl, !scope.equalsIgnoreCase("all"));
	}

	public static String call(PageContext pc, String input, Object find, Object repl, String scope) throws PageException {
		return _call(pc, input, find, repl, !scope.equalsIgnoreCase("all"));
	}

	public static String call(PageContext pc, String input, Object find, String repl) throws PageException {
		return _call(pc, input, find, repl, true);
	}

	public static String call(PageContext pc, String input, Object find, Object repl) throws PageException {
		return _call(pc, input, find, repl, true);
	}

	public static String call(PageContext pc, String input, Object struct) throws PageException {
		if (!Decision.isStruct(struct)) throw new FunctionException(pc, "replace", 2, "sub1", "When passing only two parameters, the second parameter must be a Struct.");
		return StringUtil.replaceStruct(input, Caster.toStruct(struct), false);
	}

	private static String _call(PageContext pc, String str, String sub1, Object sub2, boolean firstOnly) throws PageException {
		if (StringUtil.isEmpty(sub1)) return str;
		if (Decision.isSimpleValue(sub2)) return StringUtil.replace(str, sub1, Caster.toString(sub2), firstOnly, false);

		return StringUtil.replace(pc, str, sub1, Caster.toFunction(sub2), firstOnly);
	}

	private static String _call(PageContext pc, String input, Object find, Object repl, boolean onlyFirst) throws PageException {
		if (!Decision.isSimpleValue(find))
			throw new FunctionException(pc, "replace", 2, "sub1", "When passing three parameters or more, the second parameter must be a simple value.");
		return _call(pc, input, Caster.toString(find), repl, onlyFirst);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), args[1]);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), args[1], args[2]);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), args[1], args[2], Caster.toString(args[3]));

		throw new FunctionException(pc, "Replace", 2, 4, args.length);
	}

}