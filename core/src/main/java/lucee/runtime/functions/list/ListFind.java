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
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

/**
 * Implements the CFML Function listfind
 */
public final class ListFind extends BIF {

	private static final long serialVersionUID = -8999117503012321225L;

	public static double call(PageContext pc, String list, String value) {
		return ListUtil.listFindIgnoreEmpty(list, value, ',') + 1;
	}

	public static double call(PageContext pc, String list, String value, String delimter) {
		return ListUtil.listFindIgnoreEmpty(list, value, delimter) + 1;
	}

	public static double call(PageContext pc, String list, String value, String delimter, boolean includeEmptyFields) {
		if (includeEmptyFields) return ListUtil.listFind(list, value, delimter) + 1;
		return ListUtil.listFindIgnoreEmpty(list, value, delimter) + 1;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), ",", false);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), false);
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]));

		throw new FunctionException(pc, "ListFind", 2, 4, args.length);
	}
}