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
package lucee.commons.io.res.filter;

import lucee.commons.io.res.Resource;

/**
 * A FileFilter providing conditional OR logic across a list of file filters. This filter returns
 * true if any filters in the list return true. Otherwise, it returns false. Checking of the file
 * filter list stops when the first filter returns true.
 */
public final class AndResourceFilter implements ResourceFilter {

	private final ResourceFilter[] filters;

	/**
	 * @param filters
	 */
	public AndResourceFilter(ResourceFilter[] filters) {
		this.filters = filters;
	}

	@Override
	public boolean accept(Resource f) {
		for (int i = 0; i < filters.length; i++) {
			if (!filters[i].accept(f)) return false;
		}
		return true;
	}
}