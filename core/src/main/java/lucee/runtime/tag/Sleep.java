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

import lucee.commons.io.SystemUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;

/**
 * Pauses the execution of the page for a given interval
 *
 *
 *
 **/
public final class Sleep extends TagImpl {

	/** Expressed in milli seconds. */
	private long time;

	@Override
	public void release() {
		super.release();
		time = 0;
	}

	/**
	 * set the value interval Expressed in milli seconds.
	 * 
	 * @param time value to set
	 **/
	public void setTime(double time) {
		this.time = (long) time;
	}

	@Override
	public int doStartTag() throws PageException {
		if (time >= 0) {
			SystemUtil.sleep(time);
		}
		else throw new ExpressionException("attribute interval must be greater or equal to 0, now [" + (time) + "]");
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}