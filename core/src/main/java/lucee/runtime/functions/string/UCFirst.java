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
 * Implements the CFML Function asc
 */
package lucee.runtime.functions.string;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class UCFirst extends BIF {

	private static final long serialVersionUID = 6476775359884698477L;

	public static String call(PageContext pc, String string) {
		return call(pc, string, false);
	}

	public static String call(PageContext pc, String string, boolean doAll) {
		if (!doAll) return StringUtil.ucFirst(string);
		return StringUtil.capitalize(string, null);
	}

	public static String call(PageContext pc, String string, boolean doAll, boolean doLowerIfAllUppercase) {
		if (doLowerIfAllUppercase && StringUtil.isAllUpperCase(string)) string = string.toLowerCase();

		if (!doAll) return StringUtil.ucFirst(string);

		return StringUtil.capitalize(string, null);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toBooleanValue(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toBooleanValue(args[1]), Caster.toBooleanValue(args[2]));

		throw new FunctionException(pc, "UCFirst", 1, 3, args.length);
	}
}