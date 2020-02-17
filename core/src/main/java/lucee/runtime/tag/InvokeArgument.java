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

import javax.servlet.jsp.tagext.Tag;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;

// TODO tag invokeargument
// attr omit

/**
 * Required for cfhttp POST operations, cfhttpparam is used to specify the parameters necessary to
 * build a cfhttp POST.
 *
 *
 *
 **/
public final class InvokeArgument extends TagImpl {

	/** A variable name for the data being passed. */
	private String name;

	/** Specifies the value of the variable being passed. */
	private Object value;
	private boolean omit;

	/**
	 * set the value value
	 * 
	 * @param value value to set
	 **/
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * set the value name
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param omit the omit to set
	 */
	public void setOmit(boolean omit) {
		this.omit = omit;
	}

	@Override
	public int doStartTag() throws PageException {
		Tag parent = getParent();
		while (parent != null && !(parent instanceof Invoke)) {
			parent = parent.getParent();
		}

		if (parent instanceof Invoke) {
			Invoke invoke = (Invoke) parent;
			invoke.setArgument(name, value);
		}
		else {
			throw new ApplicationException("Wrong Context, tag InvokeArgument must be inside an Invoke tag");
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		value = null;
		name = null;
		omit = false;
	}
}