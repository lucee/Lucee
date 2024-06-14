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
package lucee.loader.servlet.jakarta;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lucee.loader.engine.CFMLEngineFactory;

public class CFMLServlet extends AbsServlet {

	private static final long serialVersionUID = -1878214660283329587L;
	private HttpServletJavax myself;

	@Override
	public void init(final ServletConfig sg) throws ServletException {
		super.init(sg);
		myself = new HttpServletJavax(this);
		try {
			engine = CFMLEngineFactory.getInstance(ServletConfigJavax.getInstance(sg), this);
		}
		catch (javax.servlet.ServletException e) {
			throw (ServletException) ((ServletExceptionJavax) e).getJakartaInstance();
		}
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		try {
			engine.serviceCFML(myself, new HttpServletRequestJavax(req), new HttpServletResponseJavax(rsp));
		}
		catch (javax.servlet.ServletException e) {
			throw (ServletException) ((ServletExceptionJavax) e).getJakartaInstance();
		}
	}
}