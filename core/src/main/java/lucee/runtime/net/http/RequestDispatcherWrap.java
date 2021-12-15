/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.net.http;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lucee.commons.net.HTTPUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;

public class RequestDispatcherWrap implements RequestDispatcher {

	private String realPath;
	private HTTPServletRequestWrap req;

	public RequestDispatcherWrap(HTTPServletRequestWrap req, String realPath) {
		this.realPath = realPath;
		this.req = req;
	}

	@Override
	public void forward(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
		PageContext pc = ThreadLocalPageContext.get();
		req = HTTPUtil.removeWrap(req);
		if (pc == null) {
			this.req.getOriginalRequestDispatcher(realPath).forward(req, rsp);
			return;
		}

		realPath = HTTPUtil.optimizeRealPath(pc, realPath);

		try {
			RequestDispatcher disp = this.req.getOriginalRequestDispatcher(realPath);
			disp.forward(req, rsp);
		}
		finally {
			ThreadLocalPageContext.register(pc);
		}
	}

	@Override
	public void include(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
		PageContext pc = ThreadLocalPageContext.get();
		if (pc == null) {
			this.req.getOriginalRequestDispatcher(realPath).include(req, rsp);
			return;
		}
		HTTPUtil.include(pc, req, rsp, realPath);
	}
}