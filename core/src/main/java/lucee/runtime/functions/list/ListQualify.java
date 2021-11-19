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

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

/**
 * Implements the CFML Function listqualify
 */
public final class ListQualify extends BIF {

	private static final long serialVersionUID = -7450079285934992224L;

	public static String call(PageContext pc, String list, String qualifier) {
		return call(pc, list, qualifier, ",", "all", false, false);
	}

	public static String call(PageContext pc, String list, String qualifier, String delimiter) {
		return call(pc, list, qualifier, delimiter, "all", false, false);
	}

	public static String call(PageContext pc, String list, String qualifier, String delimiter, String elements) {
		return call(pc, list, qualifier, delimiter, elements, false, false);
	}

	public static String call(PageContext pc, String list, String qualifier, String delimiter, String elements, boolean includeEmptyFields) {
		return call(pc, list, qualifier, delimiter, elements, includeEmptyFields, false);
	}

	public static String call(PageContext pc, String list, String qualifier, String delimiter, String elements, boolean includeEmptyFields, boolean psq // this is used only
	// internally by lucee,
	// search for "PSQ-BIF" in
	// code
	) {

		if (list.length() == 0) return "";
		if (psq) list = StringUtil.replace(list, "'", "''", false);

		Array arr = includeEmptyFields ? ListUtil.listToArray(list, delimiter) : ListUtil.listToArrayRemoveEmpty(list, delimiter);

		boolean isQChar = qualifier.length() == 1;
		boolean isDChar = delimiter.length() == 1;

		if (isQChar && isDChar) return doIt(arr, qualifier.charAt(0), delimiter.charAt(0), elements);
		else if (isQChar && !isDChar) return doIt(arr, qualifier.charAt(0), delimiter, elements);
		else if (!isQChar && isDChar) return doIt(arr, qualifier, delimiter.charAt(0), elements);
		else return doIt(arr, qualifier, delimiter, elements);

	}

	private static String doIt(Array arr, char qualifier, char delimiter, String elements) {
		StringBuilder rtn = new StringBuilder();
		int len = arr.size();

		if (StringUtil.toLowerCase(elements).equals("all")) {
			rtn.append(qualifier);
			rtn.append(arr.get(1, ""));
			rtn.append(qualifier);
			for (int i = 2; i <= len; i++) {
				rtn.append(delimiter);
				rtn.append(qualifier);
				rtn.append(arr.get(i, ""));
				rtn.append(qualifier);
			}
		}
		else {
			qualifyString(rtn, arr.get(1, "").toString(), qualifier);
			for (int i = 2; i <= len; i++) {
				rtn.append(delimiter);
				qualifyString(rtn, arr.get(i, "").toString(), qualifier);
			}
		}
		return rtn.toString();
	}

	private static String doIt(Array arr, char qualifier, String delimiter, String scope) {
		StringBuilder rtn = new StringBuilder();
		int len = arr.size();

		if (StringUtil.toLowerCase(scope).equals("all")) {
			rtn.append(qualifier);
			rtn.append(arr.get(1, ""));
			rtn.append(qualifier);
			for (int i = 2; i <= len; i++) {
				rtn.append(delimiter);
				rtn.append(qualifier);
				rtn.append(arr.get(i, ""));
				rtn.append(qualifier);
			}
		}
		else {
			qualifyString(rtn, arr.get(1, "").toString(), qualifier);
			for (int i = 2; i <= len; i++) {
				rtn.append(delimiter);
				qualifyString(rtn, arr.get(i, "").toString(), qualifier);
			}
		}
		return rtn.toString();
	}

	private static String doIt(Array arr, String qualifier, char delimiter, String scope) {
		StringBuilder rtn = new StringBuilder();
		int len = arr.size();

		if (StringUtil.toLowerCase(scope).equals("all")) {
			rtn.append(qualifier);
			rtn.append(arr.get(1, ""));
			rtn.append(qualifier);
			for (int i = 2; i <= len; i++) {
				rtn.append(delimiter);
				rtn.append(qualifier);
				rtn.append(arr.get(i, ""));
				rtn.append(qualifier);
			}
		}
		else {
			qualifyString(rtn, arr.get(1, "").toString(), qualifier);
			for (int i = 2; i <= len; i++) {
				rtn.append(delimiter);
				qualifyString(rtn, arr.get(i, "").toString(), qualifier);
			}
		}
		return rtn.toString();
	}

	private static String doIt(Array arr, String qualifier, String delimiter, String scope) {
		StringBuilder rtn = new StringBuilder();
		int len = arr.size();

		if (StringUtil.toLowerCase(scope).equals("all")) {
			rtn.append(qualifier);
			rtn.append(arr.get(1, ""));
			rtn.append(qualifier);
			for (int i = 2; i <= len; i++) {
				rtn.append(delimiter);
				rtn.append(qualifier);
				rtn.append(arr.get(i, ""));
				rtn.append(qualifier);
			}
		}
		else {
			qualifyString(rtn, arr.get(1, "").toString(), qualifier);
			for (int i = 2; i <= len; i++) {
				rtn.append(delimiter);
				qualifyString(rtn, arr.get(i, "").toString(), qualifier);
			}
		}
		return rtn.toString();
	}

	private static void qualifyString(StringBuilder rtn, String value, String qualifier) {
		if (Decision.isNumber(value)) rtn.append(value);
		else {
			rtn.append(qualifier);
			rtn.append(value);
			rtn.append(qualifier);
		}
	}

	private static void qualifyString(StringBuilder rtn, String value, char qualifier) {
		if (Decision.isNumber(value)) rtn.append(value);
		else {
			rtn.append(qualifier);
			rtn.append(value);
			rtn.append(qualifier);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		if (args.length == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]));
		if (args.length == 5)
			return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toBooleanValue(args[4]));

		throw new FunctionException(pc, "ListQualify", 2, 5, args.length);
	}

}