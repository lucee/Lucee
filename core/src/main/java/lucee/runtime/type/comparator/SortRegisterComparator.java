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

import lucee.commons.lang.ComparatorUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

/**
 * Implementation of a Comparator, compares to Softregister Objects
 */
public final class SortRegisterComparator implements ExceptionComparator {

	private boolean isAsc;
	private PageException pageException = null;
	private boolean ignoreCase;
	private final Comparator comparator;

	/**
	 * constructor of the class
	 * 
	 * @param isAsc is ascending or descending
	 * @param ignoreCase do ignore case
	 */
	public SortRegisterComparator(PageContext pc, boolean isAsc, boolean ignoreCase, boolean localeSensitive) {
		this.isAsc = isAsc;
		this.ignoreCase = ignoreCase;

		comparator = ComparatorUtil.toComparator(ignoreCase ? ComparatorUtil.SORT_TYPE_TEXT_NO_CASE : ComparatorUtil.SORT_TYPE_TEXT, isAsc,
				localeSensitive ? ThreadLocalPageContext.getLocale(pc) : null, null);

	}

	/**
	 * @return Returns the expressionException.
	 */
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
		String strLeft = Caster.toString(((SortRegister) oLeft).getValue());
		String strRight = Caster.toString(((SortRegister) oRight).getValue());
		return strLeft.compareTo(strRight);
		// return comparator.compare(strLeft, strRight);
	}

}