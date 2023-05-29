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
 * Implements the CFML Function arraymin
 */
package lucee.runtime.functions.arrays;

import java.util.List;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.util.ArrayUtil;

public final class ArraySlice extends BIF {

	private static final long serialVersionUID = 7309769117464009924L;

	public static Array call(PageContext pc, Array arr, double offset) throws PageException {
		return call(pc, arr, offset, 0);
	}

	public static Array call(PageContext pc, Array arr, double offset, double length) throws PageException {

		int len = arr.size();
		if (len == 0) throw new FunctionException(pc, "arraySlice", 1, "array", "Array cannot be empty");
		
		if (offset > 0) {
			if (len < offset) throw new FunctionException(pc, "arraySlice", 2, "offset", "Offset cannot be greater than size of the array");

			int to = 0;
			if (length > 0) to = (int) (offset + length - 1);
			else if (length < 0) to = (int) (len + length);
			if (len < to) throw new FunctionException(pc, "arraySlice", 3, "length", "Offset+length cannot be greater than size of the array");

			return get(arr, (int) offset, to);
		}
		return call(pc, arr, len + offset, length);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]));
		else if (args.length == 3) return call(pc, Caster.toArray(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]));
		else throw new FunctionException(pc, "ArraySlice", 2, 3, args.length);
	}

	public static Array get(Array arr, int from, int to) throws PageException {
		int dimension = arr.getDimension();
		Array rtn = ArrayUtil.getInstance(dimension);

		if (dimension < 2) {
			if (to < 1) to = arr.size();
			if (from > to) return rtn;
			List subList = ((List) arr).subList(from - 1, to);
			rtn = new ArrayImpl(subList.toArray());
		}
		else { // two and three-dimensional arrays need this because the above one losses dimension
			int[] keys = arr.intKeys();
			for (int i = 0; i < keys.length; i++) {
				int key = keys[i];
				if (key < from) continue;
				if (to > 0 && key > to) break;
				rtn.append(arr.getE(key));
			}
		}

		return rtn;
	}

}