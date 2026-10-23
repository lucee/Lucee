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
package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.ext.function.Function;

/**
 * metaphone() was removed in Lucee 9 together with commons-codec, the function still exists, so
 * existing code fails with a clear message instead of returning a different value silently
 */
public final class Metaphone implements Function {

	public static String call(PageContext pc, String str) throws ApplicationException {
		throw new ApplicationException("metaphone() was removed in Lucee 9 together with commons-codec; use soundex() or a Java/Maven metaphone implementation",
				"soundex() returns a different value (a fixed length Soundex code like [L200] instead of a Metaphone key like [LS]), so stored keys need to be recalculated if you switch.");
	}

}
