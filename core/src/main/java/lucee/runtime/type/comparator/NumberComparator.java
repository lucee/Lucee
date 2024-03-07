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
package lucee.runtime.type.comparator;

import java.util.Comparator;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

/**
 * comparator implementation, compare to numbers
 */
public final class NumberComparator implements Comparator {

	private boolean isAsc;
	private boolean allowEmpty;

	/**
	 * constructor of the class
	 *
	 * @param isAsc is ascendinf or descending
	 */
	public NumberComparator(boolean isAsc) {
		this(isAsc, false);
	}

	public NumberComparator(boolean isAsc, boolean allowEmpty) {
		this.isAsc = isAsc;
		this.allowEmpty = allowEmpty;
	}

	@Override
	public int compare(Object oLeft, Object oRight) {
		try {
			if (isAsc) return compareObjects(oLeft, oRight);
			return compareObjects(oRight, oLeft);
		}
		catch (PageException e) {
			throw new PageRuntimeException(new ExpressionException("can only sort arrays with simple values", e.getMessage()));
		}
	}

	private int compareObjects(Object oLeft, Object oRight) throws PageException {
		// If we're allowing empty/null values, then run this logic
		if (allowEmpty) {
			oLeft = v(oLeft);
			oRight = v(oRight);
			if (oLeft == null && oRight == null) {
				return 0;
			}
			else if (oLeft == null && oRight != null) {
				return -1;
			}
			else if (oLeft != null && oRight == null) {
				return 1;
			}
		}
		// This logic assumes we have two non-null values
		double left = Caster.toDoubleValue(oLeft);
		double right = Caster.toDoubleValue(oRight);
		if (left < right) return -1;
		return left > right ? 1 : 0;
	}

	private Object v(Object value) {
		if (value instanceof String && StringUtil.isEmpty(value.toString())) return null;
		return value;
	}
}