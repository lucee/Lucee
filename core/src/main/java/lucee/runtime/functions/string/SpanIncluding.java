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
 * Implements the CFML Function spanincluding
 */
package lucee.runtime.functions.string;

import java.util.StringTokenizer;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class SpanIncluding extends BIF {

	private static final long serialVersionUID = 3274154577208880461L;

	public static String call(PageContext pc, String str, String set) {
		StringTokenizer stringtokenizer = new StringTokenizer(str, set);
		if (stringtokenizer.hasMoreTokens()) {
			String rtn = stringtokenizer.nextToken();
			int i = str.indexOf(rtn);
			if (i == 0) return "";
			return str.substring(0, i);
		}
		return str;

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));

		throw new FunctionException(pc, "SpanIncluding", 2, 2, args.length);
	}

}