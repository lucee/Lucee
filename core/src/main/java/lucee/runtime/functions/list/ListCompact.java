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
 * Implements the CFML Function listlast
 */
package lucee.runtime.functions.list;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class ListCompact extends BIF {

	private static final long serialVersionUID = 533751863889168299L;

	public static String call(PageContext pc, String list) {
		return call(pc, list, ",", false);
	}

	public static String call(PageContext pc, String list, String delimiter) {
		return call(pc, list, delimiter, false);
	}

	public static String call(PageContext pc, String list, String delimiter, boolean multiCharacterDelimiter) {
		return ListUtil.trim(list, delimiter, multiCharacterDelimiter);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]), ",", false);
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), false);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2]));

		throw new FunctionException(pc, "ListCompact", 1, 3, args.length);
	}
}