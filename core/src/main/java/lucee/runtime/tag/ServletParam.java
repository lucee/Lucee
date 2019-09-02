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

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.tag.TagImpl;

/**
 * A child of cfservlet. It passes data to the servlet. Each cfservletparam tag within the cfservlet
 * block passes a separate piece of data to the servlet.
 *
 *
 *
 **/
public final class ServletParam extends TagImpl {
	private String value;
	private String type;
	private String variable;
	private String name;

	/**
	 * constructor for the tag class
	 * 
	 * @throws ExpressionException
	 **/
	public ServletParam() throws ExpressionException {
		throw new ExpressionException("tag cfservletparam is deprecated");
	}

	/**
	 * set the value value Value of a name-value pair passed to the servlet as a parameter.
	 * 
	 * @param value value to set
	 **/
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * set the value type
	 * 
	 * @param type value to set
	 **/
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * set the value variable
	 * 
	 * @param variable value to set
	 **/
	public void setVariable(String variable) {
		this.variable = variable;
	}

	/**
	 * set the value name If used with the value attribute, it is the name of the servlet parameter. If
	 * used with the variable attribute, it is the name of the servlet attribute
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int doStartTag() {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		value = "";
		type = "";
		variable = "";
		name = "";
	}
}