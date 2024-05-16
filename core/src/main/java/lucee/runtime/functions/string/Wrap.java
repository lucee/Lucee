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
 * Implements the CFML Function refind
 */
package lucee.runtime.functions.string;

import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

public final class Wrap extends BIF {

	public static String call(PageContext pc, String string, double limit) throws PageException {
		return call(pc, string, limit, false);
	}

	public static String call(PageContext pc, String string, double limit, boolean strip) throws PageException {
		if (strip) {
			string = REReplace.call(pc, string, "[[:space:]]", " ", "all");
		}
		int _limit = (int) limit;
		if (limit < 1) throw new FunctionException(pc, "Wrap", 2, "limit", "value mus be a positive number");
		return wrap(string, _limit);
	}

	/**
	 * wraps a String to specified length
	 * 
	 * @param str string to erap
	 * @param wrapTextLength
	 * @return wraped String
	 */
	public static String wrap(String str, int wrapTextLength) {
		if (wrapTextLength <= 0) return str;

		StringBuilder rtn = new StringBuilder();
		String ls = SystemUtil.getOSSpecificLineSeparator();
		Array arr = ListUtil.listToArray(str, ls);
		int len = arr.size();

		for (int i = 1; i <= len; i++) {
			rtn.append(wrapLine(Caster.toString(arr.get(i, ""), ""), wrapTextLength));
			if (i + 1 < len) rtn.append(ls);
		}
		return rtn.toString();
	}

	/**
	 * wrap a single line
	 * 
	 * @param str
	 * @param wrapTextLength
	 * @return
	 */
	private static String wrapLine(String str, int wrapTextLength) {
		int wtl = wrapTextLength;

		if (str.length() <= wtl) return str;

		String sub = str.substring(0, wtl);
		String rest = str.substring(wtl);
		char firstR = rest.charAt(0);
		String ls = SystemUtil.getOSSpecificLineSeparator();

		if (firstR == ' ' || firstR == '\t') return sub + ls + wrapLine(rest.length() > 1 ? rest.substring(1) : "", wrapTextLength);

		int indexSpace = sub.lastIndexOf(' ');
		int indexTab = sub.lastIndexOf('\t');
		int index = indexSpace <= indexTab ? indexTab : indexSpace;

		if (index == -1) return sub + ls + wrapLine(rest, wrapTextLength);
		return sub.substring(0, index) + ls + wrapLine(sub.substring(index + 1) + rest, wrapTextLength);

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toDoubleValue(args[1]), Caster.toBooleanValue(args[2]));

		throw new FunctionException(pc, "Wrap", 2, 3, args.length);
	}
}