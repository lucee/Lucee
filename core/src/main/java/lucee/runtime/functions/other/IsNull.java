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
 * Implements the CFML Function isnotmap
 */
package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.QueryColumn;

public final class IsNull implements Function {
	public static boolean call(PageContext pc, Object object) {
		if (object == null) return true;
		if (object instanceof QueryColumn && NullSupportHelper.full(pc)) {
			return ((QueryColumn) object).get(pc, null) == null;
		}
		return false;
	}

	// called by modifed call from translation time evaluator
	public static boolean call(PageContext pc, String str) {
		try {
			return pc.evaluate(str) == null;
		}
		catch (PageException e) {
			return true;
		}
	}
}