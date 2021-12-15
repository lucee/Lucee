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

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.TagImpl;

public class AjaxImport extends TagImpl {
	// private String csssrc;
	// private String scriptsrc;
	// private String tags;

	@Override
	public void release() {
		super.release();
		// csssrc=null;
		// scriptsrc=null;
		// tags=null;
	}

	/**
	 * @param csssrc the csssrc to set
	 */
	public void setCsssrc(String csssrc) {
		// this.csssrc = csssrc;
	}

	/**
	 * @param scriptsrc the scriptsrc to set
	 */
	public void setScriptsrc(String scriptsrc) {
		// this.scriptsrc = scriptsrc;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(String tags) {
		// this.tags = tags;
	}

	@Override
	public int doStartTag() throws PageException {
		throw new TagNotSupported("AjaxImport");
		// return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}