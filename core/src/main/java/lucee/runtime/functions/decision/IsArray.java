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
 * Implements the CFML Function isarray
 */
package lucee.runtime.functions.decision;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;

public final class IsArray implements Function {
	public static boolean call(PageContext pc, Object object) {
		return Decision.isArray(object);
	}

	public static boolean call(PageContext pc, Object object, double dimension) {
		if (dimension == -999) return Decision.isArray(object); // -999 == default value for named argument

		if ((object instanceof Array)) {
			return ((Array) object).getDimension() == (int) dimension;
		}
		else if (dimension == 1) {
			return Decision.isArray(object);
		}
		return false;
	}
}