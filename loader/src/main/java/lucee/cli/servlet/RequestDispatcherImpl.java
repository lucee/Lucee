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
package lucee.cli.servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

/**
 * Request dispatcher for the CLI (no servlet container). A forward discards what the current
 * request has written so far and runs the target template in the current request.
 */
final class RequestDispatcherImpl implements RequestDispatcher {

	private static final String ABORT_EXCEPTION = "lucee.runtime.exp.AbortException";

	private final String path;

	RequestDispatcherImpl(final String path) {
		this.path = path;
	}

	@Override
	public void forward(final ServletRequest req, final ServletResponse rsp) throws ServletException, IOException {
		final PageContext pc = CFMLEngineFactory.getInstance().getThreadPageContext();
		if (pc == null) throw new ServletException("no page context available to forward to [" + path + "]");
		pc.clear();
		try {
			pc.doInclude(path, false);
		}
		catch (final PageException pe) {
			// an abort in the forwarded template ends the forwarded request, which is what a forward waits for
			if (isAbort(pe)) return;
			throw new ServletException(pe);
		}
	}

	/**
	 * the loader has no compile time dependency to the core, so the abort exception of the core is
	 * detected by name in the class hierarchy of the exception
	 */
	private static boolean isAbort(final Throwable t) {
		for (Class<?> clazz = t.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			if (ABORT_EXCEPTION.equals(clazz.getName())) return true;
		}
		return false;
	}

	@Override
	public void include(final ServletRequest req, final ServletResponse rsp) throws ServletException, IOException {
		throw new ServletException("method include(ServletRequest, ServletResponse) not supported");
	}
}
