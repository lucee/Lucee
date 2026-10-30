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
package lucee.runtime.functions.conversion;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.interpreter.StrictJSONValidator;
import lucee.runtime.op.Caster;

public final class IsJSON {

	/**
	 * default of the argument "lenient"; true accepts everything the (lenient) interpreter accepts,
	 * false only accepts JSON as defined by RFC 8259
	 */
	public static final boolean DEFAULT_LENIENT = true;

	public static boolean call(PageContext pc, Object obj) {
		try {
			return call(pc, obj, null, DEFAULT_LENIENT);
		}
		catch (ApplicationException e) {
			// not possible, only an invalid format throws
			return false;
		}
	}

	public static boolean call(PageContext pc, Object obj, String strFormat) throws ApplicationException {
		return call(pc, obj, strFormat, DEFAULT_LENIENT);
	}

	/**
	 * @param strFormat "json", "json5" or empty/null; when empty, "json5" is used if lenient, otherwise
	 *            the strict JSON of RFC 8259
	 * @param lenient when false, only JSON as defined by RFC 8259 is valid (format "json5" stays JSON5,
	 *            as this is a superset by definition)
	 */
	public static boolean call(PageContext pc, Object obj, String strFormat, boolean lenient) throws ApplicationException {
		int format = StringUtil.isEmpty(strFormat, true) ? 0 : JSONExpressionInterpreter.toFormat(strFormat);

		String str = Caster.toString(obj, null);
		if (StringUtil.isEmpty(str, true)) return false;

		if (!lenient && format != JSONExpressionInterpreter.FORMAT_JSON5) {
			return StrictJSONValidator.isValid(str);
		}
		if (format == 0) format = JSONExpressionInterpreter.FORMAT_JSON5;

		try {
			new JSONExpressionInterpreter(false, format).interpret(pc, str);
			return true;
		}
		catch (PageException e) {
			return false;
		}
	}

}
