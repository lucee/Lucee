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
package lucee.runtime.tag;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;

public class ZipParamSource implements ZipParamAbstr {

	private Resource source;
	private String entryPath;
	private ResourceFilter filter;
	private String prefix;
	private boolean recurse;

	public ZipParamSource(Resource source, String entryPath, ResourceFilter filter, String prefix, boolean recurse) {

		this.source=source;
		this.entryPath=entryPath;
		this.filter=filter;
		this.prefix=prefix;
		this.recurse=recurse;
	}

	/**
	 * @return the source
	 */
	public Resource getSource() {
		return source;
	}

	/**
	 * @return the entryPath
	 */
	public String getEntryPath() {
		return entryPath;
	}

	/**
	 * @return the filter
	 */
	public ResourceFilter getFilter(){
		return filter;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @return the recurse
	 */
	public boolean isRecurse() {
		return recurse;
	}

}