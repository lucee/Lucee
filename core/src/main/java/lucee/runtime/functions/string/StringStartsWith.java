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
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

/**
 * implements the member method String.startWith(prefix, ignoreCase)
 */
public class StringStartsWith extends BIF {

	public static boolean call(PageContext pc, String input, String subs, boolean ignoreCase) {

		if (ignoreCase) return input.regionMatches(true, 0, subs, 0, subs.length());

		return input.startsWith(subs);
	}

	public static boolean call(PageContext pc, String input, String subs) {

		return call(pc, input, subs, false);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {

		if (args.length < 2 || args.length > 3) throw new FunctionException(pc, "startsWith", 2, 3, args.length);

		return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), args.length == 3 ? Caster.toBoolean(args[2]) : false);
	}
}