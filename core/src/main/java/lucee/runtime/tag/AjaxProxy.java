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

public class AjaxProxy extends TagImpl {
	private String bind;
	private String cfc;
	private String jsClassName;
	private String onError;
	private String onSuccess;

	@Override
	public void release() {
		super.release();
		bind = null;
		cfc = null;
		jsClassName = null;
		onError = null;
		onSuccess = null;
	}

	/**
	 * @param bind the bind to set
	 */
	public void setBind(String bind) {
		this.bind = bind;
	}

	/**
	 * @param cfc the cfc to set
	 */
	public void setCfc(String cfc) {
		this.cfc = cfc;
	}

	/**
	 * @param jsClassName the jsClassName to set
	 */
	public void setJsclassname(String jsClassName) {
		this.jsClassName = jsClassName;
	}

	/**
	 * @param onError the onError to set
	 */
	public void setOnerror(String onError) {
		this.onError = onError;
	}

	/**
	 * @param onSuccess the onSuccess to set
	 */
	public void setOnsuccess(String onSuccess) {
		this.onSuccess = onSuccess;
	}

	@Override
	public int doStartTag() throws PageException {
		throw new TagNotSupported("AjaxProxy");
		// return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}