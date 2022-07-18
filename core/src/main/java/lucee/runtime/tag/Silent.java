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

import javax.servlet.jsp.JspException;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.ext.tag.BodyTagTryCatchFinallyImpl;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.writer.BodyContentImpl;

public final class Silent extends BodyTagTryCatchFinallyImpl {

	private Boolean bufferOutput = true;
	private BodyContentImpl bc;
	private boolean wasSilent;

	/**
	 * @param bufferoutput the bufferoutput to set
	 */
	public void setBufferoutput(boolean bufferOutput) {
		this.bufferOutput = bufferOutput ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public int doStartTag() throws JspException {
		if (bufferOutput == null) bufferOutput = ((ApplicationContextSupport) pageContext.getApplicationContext()).getBufferOutput() ? Boolean.TRUE : Boolean.FALSE;

		if (bufferOutput.booleanValue()) bc = (BodyContentImpl) pageContext.pushBody();
		else wasSilent = pageContext.setSilent();

		return EVAL_BODY_INCLUDE;
	}

	@Override
	public void doCatch(Throwable t) throws Throwable {
		ExceptionUtil.rethrowIfNecessary(t);
		if (bufferOutput.booleanValue()) {
			try {
				bc.flush();
			}
			catch (IOException e) {
			}
			pageContext.popBody();
			bc = null;
		}
		else if (!wasSilent) pageContext.unsetSilent();
		super.doCatch(t);
	}

	@Override
	public void doFinally() {
		if (bufferOutput.booleanValue()) {
			if (bc != null) {
				bc.clearBody();
				pageContext.popBody();
			}
		}
		else if (!wasSilent) pageContext.unsetSilent();
	}

	@Override
	public void release() {
		super.release();
		bc = null;
		this.bufferOutput = true;
	}

}