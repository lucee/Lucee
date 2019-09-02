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

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;

/**
 * Tests for a parameter's existence, tests its data type, and provides a default value if one is
 * not assigned.
 *
 *
 *
 **/
public final class Param extends TagImpl {

	/** The type of parameter that is required. The default is 'any'. */
	private String type = "any";

	/** Default value to set the parameter to if it does not exist. */
	private Object _default;

	/**
	 * The name of the parameter to test, such as Client.Email or Cookie.BackgroundColor. If you omit
	 * the DEFAULT attribute, an error occurs if the specified parameter does not exist
	 */
	private String name;

	private double min;
	private double max;
	private String pattern;

	@Override
	public void release() {
		super.release();
		type = "any";
		_default = null;
		name = null;

		min = -1;
		max = -1;
		pattern = null;
	}

	public Param() throws ApplicationException {
		throw new ApplicationException("this Tag Implementation is deprecated and replaced with a Translation Time Transformer");
	}

	/**
	 * set the value type The type of parameter that is required. The default is 'any'.
	 * 
	 * @param type value to set
	 **/
	public void setType(String type) {
		this.type = type.trim().toLowerCase();
	}

	/**
	 * set the value default Default value to set the parameter to if it does not exist.
	 * 
	 * @param _default value to set
	 **/
	public void setDefault(Object _default) {
		this._default = _default;
	}

	/**
	 * @param max the max to set
	 */
	public void setMax(double max) {
		this.max = max;
	}

	/**
	 * @param min the min to set
	 */
	public void setMin(double min) {
		this.min = min;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * set the value name The name of the parameter to test, such as Client.Email or
	 * Cookie.BackgroundColor. If you omit the DEFAULT attribute, an error occurs if the specified
	 * parameter does not exist
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int doStartTag() throws PageException {
		if ("range".equals(type)) pageContext.param(type, name, _default, min, max);
		else if ("regex".equals(type) || "regular_expression".equals(type)) pageContext.param(type, name, _default, pattern);
		else pageContext.param(type, name, _default);
		return SKIP_BODY;
	}

}