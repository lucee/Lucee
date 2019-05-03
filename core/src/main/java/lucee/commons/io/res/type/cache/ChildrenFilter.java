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
package lucee.commons.io.res.type.cache;

import lucee.commons.io.cache.CacheKeyFilter;
import lucee.commons.lang.StringUtil;

public class ChildrenFilter implements CacheKeyFilter {

	private String path;

	public ChildrenFilter(String path) {
		this.path = (StringUtil.endsWith(path, '/')) ? path + ":" : path + "/:";
	}

	@Override
	public boolean accept(String key) {
		return key.startsWith(path);
	}

	@Override
	public String toPattern() {
		return path + "*";
	}

}