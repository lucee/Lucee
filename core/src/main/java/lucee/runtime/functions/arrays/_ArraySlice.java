/**
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
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
package lucee.runtime.functions.arrays;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

/**
 * Internal function behind the array slice syntax <code>arr[from:to:step]</code> (LDEV-1813). The
 * semantics follow ACF 2018: indexes are 1-based, the end is inclusive, a negative index counts
 * from the end (-1 is the last element), omitted bounds default to the whole array and a negative
 * step walks the array backwards.
 */
public final class _ArraySlice implements Function {

	private static final long serialVersionUID = 4812590337421133127L;

	/** bit set in the mask when the "from" part was omitted in the source code */
	public static final int OMITTED_FROM = 1;
	/** bit set in the mask when the "to" part was omitted in the source code */
	public static final int OMITTED_TO = 2;
	/** bit set in the mask when the "step" part was omitted in the source code */
	public static final int OMITTED_STEP = 4;

	public static Array call(PageContext pc, Object obj, Object from, Object to, Object step, Object omitted) throws PageException {
		Array arr = obj instanceof Array ? (Array) obj : Caster.toArray(obj, null);
		if (arr == null) {
			throw new ExpressionException("the slice syntax [from:to:step] can only be used with arrays, the value is of type [" + Caster.toTypeName(obj) + "]");
		}

		int mask = Caster.toIntValue(omitted);
		int len = arr.size();

		int stp = (mask & OMITTED_STEP) != 0 ? 1 : Caster.toIntValue(step);
		if (stp == 0) throw new ExpressionException("the step of the slice syntax [from:to:step] cannot be 0");

		int f, t;
		if ((mask & OMITTED_FROM) != 0) f = stp > 0 ? 1 : len;
		else f = resolve(Caster.toIntValue(from), len);
		if ((mask & OMITTED_TO) != 0) t = stp > 0 ? len : 1;
		else t = resolve(Caster.toIntValue(to), len);

		ArrayImpl rtn = new ArrayImpl();
		// nothing to slice, this only happens for an empty array, because explicit bounds are validated
		if (len == 0) return rtn;

		if (stp > 0) {
			for (int i = f; i <= t; i += stp) {
				rtn.append(arr.getE(i));
			}
		}
		else {
			for (int i = f; i >= t; i += stp) {
				rtn.append(arr.getE(i));
			}
		}
		return rtn;
	}

	/**
	 * a negative index counts from the end of the array, the result must be a valid index of the array
	 */
	private static int resolve(int index, int len) throws PageException {
		int i = index < 0 ? len + index + 1 : index;
		if (i < 1 || i > len) {
			throw new ExpressionException("array index [" + index + "] used in the slice syntax [from:to:step] is out of range, the array has [" + len + "] elements");
		}
		return i;
	}
}
