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
package lucee.runtime.functions.decision;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

/**
 * 
 */
public final class IsValid implements Function {

	private static final long serialVersionUID = -1383105304624662986L;

	/**
	 * check for many diff types
	 * 
	 * @param pc
	 * @param type
	 * @param value
	 * @return
	 * @throws ExpressionException
	 */
	public static boolean call(PageContext pc, String type, Object value) throws ExpressionException {
		type = type.trim();

		if ("range".equalsIgnoreCase(type)) throw new FunctionException(pc, "isValid", 1, "type", "for [range] you have to define a min and max value");

		if ("regex".equalsIgnoreCase(type) || "regular_expression".equalsIgnoreCase(type))
			throw new FunctionException(pc, "isValid", 1, "type", "for [regex] you have to define a pattern");

		return Decision.isValid(type, value);
	}

	/**
	 * regex check
	 * 
	 * @param pc
	 * @param type
	 * @param value
	 * @param objPattern
	 * @return
	 * @throws PageException
	 */
	public static boolean call(PageContext pc, String type, Object value, Object objPattern) throws PageException {
		type = type.trim();

		if (!"regex".equalsIgnoreCase(type) && !"regular_expression".equalsIgnoreCase(type))
			throw new FunctionException(pc, "isValid", 1, "type", "wrong attribute count for type [" + type + "]");

		return regex(pc, Caster.toString(value, null), Caster.toString(objPattern));
	}

	public static boolean regex(PageContext pc, String value, String strPattern) {
		if (value == null) return false;
		return ((PageContextImpl) pc).getRegex().matches(strPattern, value, false);
	}

	public static boolean call(PageContext pc, String type, Object value, Object objMin, Object objMax) throws PageException {

		// for named argument calls
		if (objMax == null) {
			if (objMin == null) return call(pc, type, value);
			return call(pc, type, value, objMin);
		}

		type = type.trim().toLowerCase();

		// numeric
		if ("range".equals(type) || "integer".equals(type) || "float".equals(type) || "numeric".equals(type) || "number".equals(type)) {

			double number = Caster.toDoubleValue(value, true, Double.NaN);
			if (!Decision.isValid(number)) return false;

			double min = toRangeNumber(pc, objMin, 3, "min");
			double max = toRangeNumber(pc, objMax, 4, "max");

			return number >= min && number <= max;
		}
		else if ("string".equals(type)) {
			String str = Caster.toString(value, null);
			if (str == null) return false;

			double min = toRangeNumber(pc, objMin, 3, "min");
			double max = toRangeNumber(pc, objMax, 4, "max");

			return str.length() >= min && str.length() <= max;
		}

		else throw new FunctionException(pc, "isValid", 1, "type", "wrong attribute count for type [" + type + "]");

	}

	private static double toRangeNumber(PageContext pc, Object objMin, int index, String name) throws FunctionException {
		double d = Caster.toDoubleValue(objMin, false, Double.NaN);
		if (!Decision.isValid(d)) throw new FunctionException(pc, "isValid", index, name, "value must be numeric");
		return d;
	}
}