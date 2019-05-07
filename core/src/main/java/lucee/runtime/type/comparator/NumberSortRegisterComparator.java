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

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;

/**
 * Implementation of a Comparator, compares to Softregister Objects
 */
public final class NumberSortRegisterComparator implements ExceptionComparator {

	private boolean isAsc;
	private PageException pageException = null;

	/**
	 * constructor of the class
	 * 
	 * @param isAsc is ascendinf or descending
	 */
	public NumberSortRegisterComparator(boolean isAsc) {
		this.isAsc = isAsc;
	}

	/**
	 * @return Returns the expressionException.
	 */
	@Override
	public PageException getPageException() {
		return pageException;
	}

	@Override
	public int compare(Object oLeft, Object oRight) {
		try {
			if (pageException != null) return 0;
			else if (isAsc) return compareObjects(oLeft, oRight);
			else return compareObjects(oRight, oLeft);
		}
		catch (PageException e) {
			pageException = e;
			return 0;
		}
	}

	private int compareObjects(Object oLeft, Object oRight) throws PageException {
		/*
		 * return Operator.compare( ((SortRegister)oLeft).getValue(), ((SortRegister)oRight).getValue() );
		 */
		return Operator.compare(Caster.toDoubleValue(v(((SortRegister) oLeft).getValue())), Caster.toDoubleValue(v(((SortRegister) oRight).getValue())));

	}

	private Object v(Object value) {
		if (value instanceof String && StringUtil.isEmpty(value.toString())) return null;
		return value;
	}

}