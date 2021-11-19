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
 * Implements the CFML Function gettoken
 */
package lucee.runtime.functions.string;

import java.util.StringTokenizer;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class GetToken extends BIF {

	private static final long serialVersionUID = 4114410822911429954L;

	public static String call(PageContext pc, String str, double index) throws ExpressionException {
		return call(pc, str, index, null);
	}

	public static String call(PageContext pc, String str, double index, String delimiters) throws ExpressionException {
		if (delimiters == null) delimiters = "\r\n\t ";

		if (index < 1) throw new FunctionException(pc, "getToken", 2, "index", "index must be a positive number now (" + ((int) index) + ")");

		StringTokenizer tokens = new StringTokenizer(str, delimiters);
		int count = 0;
		while (tokens.hasMoreTokens()) {
			if (++count == index) return tokens.nextToken();
			tokens.nextToken();
		}
		return "";
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toString(args[2]));

		throw new FunctionException(pc, "GetToken", 2, 3, args.length);
	}
}