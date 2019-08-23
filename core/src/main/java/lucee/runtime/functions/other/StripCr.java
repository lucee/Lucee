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
 * Implements the CFML Function stripcr
 */
package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class StripCr extends BIF {

	private static final long serialVersionUID = 1101162964675776635L;

	public static String call(PageContext pc, String string) {
		StringBuilder sb = new StringBuilder(string.length());
		int start = 0;
		int pos = 0;

		while ((pos = string.indexOf('\r', start)) != -1) {
			sb.append(string.substring(start, pos));
			start = pos + 1;
		}
		if (start < string.length()) sb.append(string.substring(start, string.length()));

		return sb.toString();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));

		throw new FunctionException(pc, "StripCr", 1, 1, args.length);
	}
}