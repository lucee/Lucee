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
package lucee.runtime.rest;

import lucee.runtime.PageSource;
import lucee.runtime.rest.path.Path;

public class Source {

	private Mapping mapping;
	private Path[] path;
	private String rawPath;
	private PageSource pageSource;

	public Source(Mapping mapping, PageSource pageSource, String path) {
		this.mapping = mapping;
		this.pageSource = pageSource;
		this.path = Path.init(path);
		this.rawPath = path;
	}

	/**
	 * @return the pageSource
	 */
	public PageSource getPageSource() {
		return pageSource;
	}

	/**
	 * @return the mapping
	 */
	public Mapping getMapping() {
		return mapping;
	}

	/**
	 * @return the path
	 */
	public Path[] getPath() {
		return path;
	}

	public String getRawPath() {
		return rawPath;
	}

}