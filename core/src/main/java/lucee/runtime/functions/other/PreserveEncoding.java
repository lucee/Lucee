/**
 * Copyright (c) 2026, Lucee Association Switzerland
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
 */
package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;

public final class PreserveEncoding implements Function {

	private static final long serialVersionUID = 4132895470591232133L;

	public static Object call(PageContext pc, Object value) {
		return value;
	}
}
