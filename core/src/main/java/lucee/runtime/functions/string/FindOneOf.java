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
 * Implements the CFML Function findoneof
 */
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public final class FindOneOf extends BIF {

	private static final long serialVersionUID = -7521748254181624968L;

	public static double call(PageContext pc, String set, String str) {
		return call(pc, set, str, 1);
	}

	public static double call(PageContext pc, String strSet, String strData, double number) {
		// strData
		char[] data = strData.toCharArray();
		// set
		char[] set = strSet.toCharArray();
		// start
		int start = (int) number - 1;
		if (start < 0) start = 0;

		if (start >= data.length || set.length == 0) return 0;
		// else {
		for (int i = start; i < data.length; i++) {
			for (int y = 0; y < set.length; y++) {
				if (data[i] == set[y]) return i + 1;
			}
		}
		// }
		return 0;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toDoubleValue(args[2]));
		throw new FunctionException(pc, "FindOneOf", 2, 3, args.length);
	}

}