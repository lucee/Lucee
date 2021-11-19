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
package lucee.runtime.err;

import java.util.ArrayList;

import lucee.runtime.exp.PageException;

/**
 * Handle Page Errors
 */
public final class ErrorPagePool {

	private ArrayList<ErrorPage> pages = new ArrayList<ErrorPage>();
	private boolean hasChanged = false;

	/**
	 * sets the page error
	 * 
	 * @param errorPage
	 */
	public void setErrorPage(ErrorPage errorPage) {
		pages.add(errorPage);
		hasChanged = true;
	}

	/**
	 * returns the error page
	 * 
	 * @param pe Page Exception
	 * @return
	 */
	public ErrorPage getErrorPage(PageException pe, short type) {
		for (int i = pages.size() - 1; i >= 0; i--) {
			ErrorPageImpl ep = (ErrorPageImpl) pages.get(i);
			if (ep.getType() == type) {
				if (type == ErrorPage.TYPE_EXCEPTION) {
					if (pe.typeEqual(ep.getTypeAsString())) return ep;
				}
				else return ep;

			}
		}
		return null;
	}

	/**
	 * clear the error page pool
	 */
	public void clear() {
		if (hasChanged) {
			pages.clear();
		}
		hasChanged = false;
	}

	/**
	 * remove this error page
	 * 
	 * @param type
	 */
	public void removeErrorPage(PageException pe) {
		// exception
		ErrorPage ep = getErrorPage(pe, ErrorPage.TYPE_EXCEPTION);
		if (ep != null) {
			pages.remove(ep);
			hasChanged = true;
		}
		// request
		ep = getErrorPage(pe, ErrorPage.TYPE_REQUEST);
		if (ep != null) {
			pages.remove(ep);
			hasChanged = true;
		}
		// validation
		ep = getErrorPage(pe, ErrorPage.TYPE_VALIDATION);
		if (ep != null) {
			pages.remove(ep);
			hasChanged = true;
		}

	}

}