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
import lucee.runtime.ext.tag.BodyTagImpl;

// MUST change behavior of multiple headers now is an array, it das so?

/**
 * Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard
 * GET operations and create a query object from a text file. POST operations lets you upload MIME
 * file types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a
 * specified server.
 *
 *
 *
 * 
 **/
public final class Div extends BodyTagImpl {

	private String bind;
	private boolean bindOnLoad;
	private String id;
	private String onBindError;
	private String tagName;

	@Override
	public void release() {
		super.release();
		this.bind = null;
		this.bindOnLoad = false;
		this.id = null;
		this.onBindError = null;
		this.tagName = null;
	}

	/**
	 * @param bind the bind to set
	 */
	public void setBind(String bind) {
		this.bind = bind;
	}

	/**
	 * @param bindOnLoad the bindOnLoad to set
	 */
	public void setBindonload(boolean bindOnLoad) {
		this.bindOnLoad = bindOnLoad;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param onBindError the onBindError to set
	 */
	public void setOnbinderror(String onBindError) {
		this.onBindError = onBindError;
	}

	/**
	 * @param tagName the tagName to set
	 */
	public void setTagname(String tagName) {
		this.tagName = tagName;
	}

	@Override
	public int doStartTag() throws TagNotSupported {
		throw new TagNotSupported("Div");
		// return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws PageException {
		return EVAL_PAGE;
	}

	@Override
	public void doInitBody() {
	}

	@Override
	public int doAfterBody() {
		return SKIP_BODY;
	}

	/**
	 * sets if has body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {
	}
}