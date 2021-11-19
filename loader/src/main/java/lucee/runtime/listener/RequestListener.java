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
package lucee.runtime.listener;

import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;

/**
 * this listener is executed after the application.cfc/application.cfm was invoked, but before
 * onApplicationStart, this class can change the PageSource executed
 */
public interface RequestListener {

	/**
	 * execute by the Application Listener
	 * 
	 * @param pc page context of the current request
	 * @param requestedPage original requested pagesource
	 * @return pagesource that should be use by the ApplicationListener
	 * @throws PageException Page Exception
	 */
	public PageSource execute(PageContext pc, PageSource requestedPage) throws PageException;

}