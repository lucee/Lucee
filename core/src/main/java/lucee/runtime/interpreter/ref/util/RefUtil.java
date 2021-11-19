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
package lucee.runtime.interpreter.ref.util;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.ref.Ref;

public final class RefUtil {

	/**
	 * transalte a Ref array to an Object array
	 * 
	 * @param refs
	 * @return objects
	 * @throws PageException
	 */
	public static Object[] getValue(PageContext pc, Ref[] refs) throws PageException {
		Object[] objs = new Object[refs.length];
		for (int i = 0; i < refs.length; i++) {
			objs[i] = refs[i].getValue(pc);
		}
		return objs;
	}

	public static boolean eeq(PageContext pc, Ref left, Ref right) throws PageException {
		// TODO Auto-generated method stub
		return left.getValue(pc) == right.getValue(pc);
	}

}