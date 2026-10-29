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
import lucee.runtime.exp.AbortException;
import lucee.runtime.exp.PageException;

/**
 * Request dispatcher for the CLI (no servlet container). A forward discards what the current
 * request has written so far and runs the target template in the current request.
 */
final class RequestDispatcherImpl implements RequestDispatcher {

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
		catch (final AbortException ae) {
			// an abort in the forwarded template ends the forwarded request, which is what a forward waits for
			return;
		}
		catch (final PageException pe) {
			throw new ServletException(pe);
		}
	}

	@Override
	public void include(final ServletRequest req, final ServletResponse rsp) throws ServletException, IOException {
		throw new ServletException("method include(ServletRequest, ServletResponse) not supported");
	}
}
