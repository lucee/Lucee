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
 * Implements the CFML Function arrayappend
 */
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;

/**
 * implementation of the Function arrayAppend
 */
public final class ArrayAppend extends BIF {

	private static final long serialVersionUID = 5989673419120862625L;

	public static boolean call(PageContext pc, Array array, Object object) throws PageException {
		return call(pc, array, object, false);
	}

	/**
	 * @param pc
	 * @param array
	 * @param object
	 * @return has appended
	 * @throws PageException
	 */
	public static boolean call(PageContext pc, Array array, Object object, boolean merge) throws PageException {
		if (merge && Decision.isCastableToArray(object)) {
			Object[] appends = Caster.toNativeArray(object);

			for (int i = 0; i < appends.length; i++) {
				array.append(appends[i]);
			}
		}
		else array.append(object);
		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), args[1]);
		else if (args.length == 3) return call(pc, Caster.toArray(args[0]), args[1], Caster.toBooleanValue(args[2]));
		else throw new FunctionException(pc, "ArrayAppend", 2, 2, args.length);
	}
}