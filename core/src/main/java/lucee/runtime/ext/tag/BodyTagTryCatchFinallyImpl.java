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

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.AbortException;
import lucee.runtime.exp.PageServletException;

/**
 * extends Body Support Tag eith TryCatchFinally Functionality
 */
public abstract class BodyTagTryCatchFinallyImpl extends BodyTagImpl implements TryCatchFinally {

	@Override
	public void doCatch(Throwable t) throws Throwable {
		ExceptionUtil.rethrowIfNecessary(t);
		if (t instanceof PageServletException) {
			PageServletException pse = (PageServletException) t;
			t = pse.getPageException();
		}
		if (bodyContent != null) {
			if (t instanceof AbortException) {
				bodyContent.writeOut(bodyContent.getEnclosingWriter());
			}
			bodyContent.clearBuffer();
		}
		throw t;
	}

	@Override
	public void doFinally() {

	}

}