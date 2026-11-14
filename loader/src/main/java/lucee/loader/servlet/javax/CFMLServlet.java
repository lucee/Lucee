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
package lucee.loader.servlet.javax;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

public class CFMLServlet extends AbsServlet {

	private static final long serialVersionUID = -1878214660283329587L;
	private HttpServletJakarta myself;

	private static final String EMULATION_MESSAGE = "Lucee is running in Java EE/javax compatibility mode. "
			+ "For optimal performance, consider upgrading your server to a Jakarta EE environment (Tomcat 10, Jetty 11, Undertow 3.0, Payara 6, WildFly 30). "
			+ "While compatibility mode works, native Jakarta EE support offers better performance and future compatibility.";

	@Override
	public void init(final ServletConfig sg) throws ServletException {
		super.init(sg);
		myself = new HttpServletJakarta(this);
		try {
			engine = CFMLEngineFactory.getInstance(ServletConfigJakarta.getInstance(sg), this);
		}
		catch (ServletExceptionJakarta e) {
			throw (ServletException) e.getJavaxInstance();
		}
		catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e);
		}
		if (!engine.getCastUtil().toBooleanValue(Util.getSystemPropOrEnvVar("lucee.suppress.servlet.warning", null), false)) {
			engine.getCFMLEngineFactory().log(lucee.loader.engine.LoggerImpl.LOG_WARNING, EMULATION_MESSAGE);
			System.err.println(EMULATION_MESSAGE);
		}
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		try {
			engine.serviceCFML(myself, new HttpServletRequestJakarta(req), new HttpServletResponseJakarta(rsp));
		}
		catch (ServletExceptionJakarta e) {
			throw (ServletException) e.getJavaxInstance();
		}
		catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}
}