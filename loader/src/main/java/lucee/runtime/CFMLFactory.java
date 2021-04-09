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
package lucee.runtime;

import java.net.URL;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;

import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.ConfigWeb;

/**
 * implements a JSP Factory, this class procduce JSP Compatible PageContext Object this object holds
 * also the must interfaces to coldfusion specified functionality
 */
public abstract class CFMLFactory extends JspFactory {

	/**
	 * reset the PageContexes
	 */
	public abstract void resetPageContext();

	/**
	 * similar to getPageContext Method but return the concrete implementation of the Lucee PageContext
	 * and take the HTTP Version of the Servlet Objects
	 * 
	 * @param servlet servlet
	 * @param req http request
	 * @param rsp http response
	 * @param errorPageURL error page URL
	 * @param needsSession need session
	 * @param bufferSize buffer size
	 * @param autoflush auto flush
	 * @return page context created
	 */
	@Deprecated
	public abstract PageContext getLuceePageContext(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp, String errorPageURL, boolean needsSession, int bufferSize,
			boolean autoflush);

	/**
	 * similar to getPageContext Method but return the concrete implementation of the Lucee PageCOntext
	 * and take the HTTP Version of the Servlet Objects
	 * 
	 * @param servlet servlet
	 * @param req http request
	 * @param rsp http response
	 * @param errorPageURL error page URL
	 * @param needsSession need session
	 * @param bufferSize buffer size
	 * @param autoflush auto flush
	 * @param register register the PageContext to the current thread
	 * @param timeout timeout in ms, if the value is smaller than 1 it is ignored and the value comming
	 *            from the context is used
	 * @param register2RunningThreads register to running threads
	 * @param ignoreScopes ignore scopes
	 * @return return the PageContext
	 */
	public abstract PageContext getLuceePageContext(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp, String errorPageURL, boolean needsSession, int bufferSize,
			boolean autoflush, boolean register, long timeout, boolean register2RunningThreads, boolean ignoreScopes);

	/**
	 * Similar to the releasePageContext Method, but take lucee PageContext as entry
	 * 
	 * @param pc page context
	 * @deprecated use instead <code>releaseLuceePageContext(PageContext pc, boolean unregister)</code>
	 */
	@Deprecated
	public abstract void releaseLuceePageContext(PageContext pc);

	/**
	 * Similar to the releasePageContext Method, but take lucee PageContext as entry
	 * 
	 * @param pc page context
	 * @param unregister unregister from current thread
	 */
	public abstract void releaseLuceePageContext(PageContext pc, boolean unregister);

	/**
	 * check timeout of all running threads, downgrade also priority from all thread run longer than 10
	 * seconds
	 */
	public abstract void checkTimeout();

	/**
	 * @return returns count of pagecontext in use
	 */
	public abstract int getUsedPageContextLength();

	/**
	 * @return Returns the config.
	 */
	public abstract ConfigWeb getConfig();

	/**
	 * @return label of the factory
	 */
	public abstract Object getLabel();

	public abstract URL getURL();

	/**
	 * @deprecated no replacement
	 * @param label a label
	 */
	@Deprecated
	public abstract void setLabel(String label);

	/**
	 * @return the servlet
	 */
	public abstract HttpServlet getServlet();

	public abstract CFMLEngine getEngine();

	public abstract int toDialect(String ext); // FUTURE deprecate
	// public abstract int toDialect(String ext, int defaultValue);// FUTURE

	public abstract Iterator<String> getCFMLExtensions();

	public abstract Iterator<String> getLuceeExtensions();

}