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
 * Implements the CFML Function arrayMerge
 * Merge 2 arrays
 */
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public final class ArrayMerge extends BIF {

	private static final long serialVersionUID = -391473381762154998L;

	public static Array call(PageContext pc, Array arr1, Array arr2) throws PageException {
		return call(pc, arr1, arr2, false);
	}

	public static Array call(PageContext pc, Array arr1, Array arr2, boolean leaveIndex) throws PageException {

		ArrayImpl arr = new ArrayImpl(arr1.size() + arr2.size());
		// arr.ensureCapacity(arr1.size() + arr2.size());

		if (leaveIndex) {
			set(arr, arr2);
			set(arr, arr1);
			return arr;
		}
		append(arr, arr1);
		append(arr, arr2);
		return arr;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), Caster.toArray(args[1]));
		else if (args.length == 3) return call(pc, Caster.toArray(args[0]), Caster.toArray(args[1]), Caster.toBooleanValue(args[2]));
		else throw new FunctionException(pc, "ArrayMerge", 2, 3, args.length);
	}

	public static void set(Array target, Array source) throws PageException {
		int[] srcKeys = source.intKeys();
		for (int i = 0; i < srcKeys.length; i++) {
			target.setE(srcKeys[i], source.getE(srcKeys[i]));
		}
	}

	public static void append(Array target, Array source) throws PageException {
		int[] srcKeys = source.intKeys();
		for (int i = 0; i < srcKeys.length; i++) {
			target.append(source.getE(srcKeys[i]));
		}
	}

}