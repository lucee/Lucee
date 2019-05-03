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
package lucee.runtime.video;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.type.util.ListUtil;

public class VideoInputImpl implements VideoInput {

	private Resource resource;
	private String args = "";
	private String path;

	/**
	 * Constructor of the class
	 * 
	 * @param resource
	 */
	public VideoInputImpl(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @see lucee.runtime.video.VideoInput#getResource()
	 */
	@Override
	public Resource getResource() {
		return resource;
	}

	/**
	 * @see lucee.runtime.video.VideoInput#setCommand(java.lang.String, java.util.List)
	 */
	@Override
	public void setCommand(String path, java.util.List args) {
		this.path = path;
		try {
			addArgs(ListUtil.listToList(args, " "));
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public void setCommand(String path, String[] args) {
		this.path = path;
		addArgs(ListUtil.arrayToList(args, " "));
	}

	/**
	 * @see lucee.runtime.video.VideoInput#getCommandAsString()
	 */
	@Override
	public String getCommandAsString() {
		return path + " " + args;
	}

	private void addArgs(String args) {
		if (StringUtil.isEmpty(this.args, true)) this.args = args;
		else this.args += "; " + args;

	}
}