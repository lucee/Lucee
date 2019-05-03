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

import lucee.runtime.exp.AbortException;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;

/**
 * Stops processing of a page at the tag location. the engine returns everything that was processed
 * before the cfabort tag. The cfabort tag is often used with conditional logic to stop processing a
 * page when a condition occurs.
 *
 *
 *
 **/
public final class Abort extends TagImpl {

	/**
	 * The error to display when cfabort executes. The error message displays in the standard CFML error
	 * page.
	 */
	private String showerror;
	private int type = lucee.runtime.exp.Abort.SCOPE_REQUEST;

	/**
	 * set the value showerror The error to display when cfabort executes. The error message displays in
	 * the standard CFML error page.
	 * 
	 * @param showerror value to set
	 **/
	public void setShowerror(String showerror) {
		this.showerror = showerror;
	}

	/**
	 * sets the type of the abort (page,request)
	 * 
	 * @param type
	 * @throws ApplicationException
	 */
	public void setType(String type) throws ApplicationException {
		type = type.toLowerCase().trim();
		if (type.equals("page")) this.type = lucee.runtime.exp.Abort.SCOPE_PAGE;
		else if (type.equals("request")) this.type = lucee.runtime.exp.Abort.SCOPE_REQUEST;
		else throw new ApplicationException("attribute type has an invalid value [" + type + "], valid values are [page,request]");
	}

	@Override
	public int doStartTag() throws PageException {
		if (showerror != null) throw new AbortException(showerror);
		throw new lucee.runtime.exp.Abort(type);
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		showerror = null;
		this.type = lucee.runtime.exp.Abort.SCOPE_REQUEST;
	}
}