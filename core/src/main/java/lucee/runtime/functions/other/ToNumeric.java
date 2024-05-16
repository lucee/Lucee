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
package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public class ToNumeric {
	public static double call(PageContext pc, Object value) throws PageException {
		return Caster.toDoubleValue(value);
	}

	public static double call(PageContext pc, Object value, Object oRadix) throws PageException {
		if (oRadix == null) return call(pc, value);
		int radix;
		if (Decision.isNumber(oRadix)) {
			radix = Caster.toIntValue(oRadix);
			if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) throw invalidRadix(pc, Caster.toString(radix));
		}
		else {
			String str = Caster.toString(oRadix).trim().toLowerCase();
			if ("bin".equals(str)) radix = 2;
			else if ("oct".equals(str)) radix = 8;
			else if ("dec".equals(str)) radix = 10;
			else if ("hex".equals(str)) radix = 16;
			else throw invalidRadix(pc, str);
		}
		return Long.parseLong(Caster.toString(value), radix);
	}

	private static FunctionException invalidRadix(PageContext pc, String radix) {
		return new FunctionException(pc, "ToNumeric", 2, "radix",
				"invalid value [" + radix + "], valid values are [" + Character.MIN_RADIX + "-" + Character.MAX_RADIX + ",bin,oct,dec,hex]");
	}
}