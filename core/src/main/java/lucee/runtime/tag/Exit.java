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

import lucee.runtime.exp.TemplateException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.type.scope.Scope;

/**
 * Used to: Abort the processing of the currently executing CFML custom tag, exit the template
 * within the currently executing CFML custom tag and reexecute a section of code within the
 * currently executing CFML custom tag
 *
 *
 *
 **/
public final class Exit extends TagImpl {

	private static final short MODE_LOOP = 0;
	private static final short MODE_EXIT_TAG = 1;
	private static final short MODE_EXIT_TEMPLATE = 2;
	/**  */
	private short method = MODE_EXIT_TAG;

	@Override
	public void release() {
		super.release();
		method = MODE_EXIT_TAG;
	}

	/**
	 * set the value method
	 * 
	 * @param method value to set
	 **/
	public void setMethod(String method) {
		method = method.toLowerCase();
		if (method.equals("loop")) this.method = MODE_LOOP;
		else if (method.equals("exittag")) this.method = MODE_EXIT_TAG;
		else if (method.equals("exittemplate")) this.method = MODE_EXIT_TEMPLATE;
	}

	@Override
	public int doStartTag() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws TemplateException {
		Scope variables = pageContext.variablesScope();
		Object thistagObj = variables.get("thistag", null);
		boolean insideCT = (thistagObj != null) && (thistagObj instanceof lucee.runtime.type.Collection);
		// executebody

		// Inside Custom Tag
		if (insideCT) {
			lucee.runtime.type.Collection thistag = (lucee.runtime.type.Collection) thistagObj;
			// executionmode
			Object exeModeObj = thistag.get("executionmode", null);
			boolean isEndMode = (exeModeObj != null) && (exeModeObj instanceof String) && exeModeObj.toString().equalsIgnoreCase("end");

			// Start
			if (!isEndMode) {
				if (method == MODE_LOOP) {
					throw new TemplateException("invalid context for the tag exit, method loop can only be used in the end tag of a custom tag");
				}
				else if (method == MODE_EXIT_TAG) {
					thistag.setEL("executebody", Boolean.FALSE);
					return SKIP_PAGE;
				}
			}
			// End
			else if (method == MODE_LOOP) {
				thistag.setEL("executebody", Boolean.TRUE);
				return SKIP_PAGE;
			}
			return SKIP_PAGE;
		}

		// OUTside Custom Tag
		if (method == MODE_LOOP) throw new TemplateException("invalid context for the tag exit, method loop can only be used inside a custom tag");
		return SKIP_PAGE;

	}
}