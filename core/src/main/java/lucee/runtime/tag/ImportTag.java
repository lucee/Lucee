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
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.tag.TagImpl;

/**
 * this tag is not used, it will ranslation over an evaluator
 * 
 * 
 * Imports a jsp Tag Library or a Custom Tag Directory
 *
 *
 *
 **/
public final class ImportTag extends TagImpl {

	private String path;

	@Override
	public void release() {
		path = null;
		super.release();
	}

	/**
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
	}

	public void setPath(String path) {
		this.path = path;

	}

	/**
	 * @param taglib
	 */
	public void setTaglib(String taglib) {
	}

	@Override
	public int doStartTag() throws ExpressionException, ApplicationException {
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}