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

import java.io.IOException;

import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;

/**
 * Flushes currently available data to the client.
 *
 *
 *
 **/
public final class Flush extends TagImpl {

	/**
	 * Flush the output each time at least the specified number of bytes become available. HTML headers,
	 * and any data that is already available when you make this call, are not included in the count.
	 */
	private double interval = -1;

	@Override
	public void release() {
		super.release();
		interval = -1;
	}

	/**
	 * set the value interval Flush the output each time at least the specified number of bytes become
	 * available. HTML headers, and any data that is already available when you make this call, are not
	 * included in the count.
	 * 
	 * @param interval value to set
	 **/
	public void setInterval(double interval) {
		this.interval = interval;
	}

	@Override
	public int doStartTag() throws PageException {
		try {
			if (interval == -1) ((PageContextImpl) pageContext).getRootOut().flush();
			else((PageContextImpl) pageContext).getRootOut().setBufferConfig((int) interval, true);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}