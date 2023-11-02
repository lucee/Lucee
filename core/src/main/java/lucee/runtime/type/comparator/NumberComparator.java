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

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

/**
 * comparator implementation, compare to numbers
 */
public final class NumberComparator implements Comparator {

	private boolean isAsc;

	/**
	 * constructor of the class
	 * 
	 * @param isAsc is ascendinf or descending
	 */
	public NumberComparator(boolean isAsc) {
		this.isAsc = isAsc;
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
		double left = Caster.toDoubleValue(oLeft);
		double right = Caster.toDoubleValue(oRight);
		if (left < right) return -1;
		return left > right ? 1 : 0;

	}
}