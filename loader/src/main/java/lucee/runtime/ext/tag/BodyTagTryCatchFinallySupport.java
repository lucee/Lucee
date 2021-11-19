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
package lucee.runtime.ext.tag;

import javax.servlet.jsp.tagext.TryCatchFinally;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageServletException;
import lucee.runtime.util.Excepton;

/**
 * extends Body Support Tag with TryCatchFinally Functionality
 */
public abstract class BodyTagTryCatchFinallySupport extends BodyTagSupport implements TryCatchFinally {

	/**
	 * @see javax.servlet.jsp.tagext.TryCatchFinally#doCatch(java.lang.Throwable)
	 */
	@Override
	public void doCatch(Throwable t) throws Throwable {
		if (t instanceof PageServletException) {
			final PageServletException pse = (PageServletException) t;
			t = pse.getPageException();
		}
		if (bodyContent != null) {
			final Excepton util = CFMLEngineFactory.getInstance().getExceptionUtil();
			if (util.isOfType(Excepton.TYPE_ABORT, t)) bodyContent.writeOut(bodyContent.getEnclosingWriter());
			bodyContent.clearBuffer();
		}
		throw t;
	}

	/**
	 * @see javax.servlet.jsp.tagext.TryCatchFinally#doFinally()
	 */
	@Override
	public void doFinally() {

	}

}