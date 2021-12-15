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
package lucee.commons.io.res.util;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;

public class WildcardPatternFilter implements ResourceAndResourceNameFilter {

	private final WildcardPattern matcher;

	public WildcardPatternFilter(String patt, boolean ignoreCase, String patternDelimiters) {

		matcher = new WildcardPattern(patt, !ignoreCase, patternDelimiters);
	}

	public WildcardPatternFilter(String pattern, String patternDelimiters) {

		this(pattern, SystemUtil.isWindows(), patternDelimiters);
	}

	@Override
	public boolean accept(Resource res) {

		return matcher.isMatch(res.getName());
	}

	@Override
	public boolean accept(Resource res, String name) {

		return matcher.isMatch(name);
	}

	public boolean accept(String name) {

		return matcher.isMatch(name);
	}

	@Override
	public String toString() {

		return matcher.toString();
	}

}