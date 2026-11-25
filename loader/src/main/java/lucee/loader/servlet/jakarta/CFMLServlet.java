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

	@Override
	public void init(final ServletConfig sg) throws ServletException {
		super.init(sg);
		engine = CFMLEngineFactory.getInstance(sg, this);
	}

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse rsp) throws ServletException, IOException {
		engine.serviceCFML(this, req, rsp);
	}

	/**
	 * LDEV-6990: shut down the engine (and with it the OSGi framework) when the servlet is taken out
	 * of service (context stop, undeploy, JVM shutdown). This is needed when the
	 * LuceeServletContextListener is not registered (for example with a WAR whose web.xml is
	 * metadata-complete), otherwise Felix and Lucee threads keep running and the JVM does not exit
	 * (same fix as LDEV-5334 on 6.2, lost with the Jakarta migration). reset() does nothing when the
	 * engine was already reset by the listener, so calling it from both places is safe.
	 */
	@Override
	public void destroy() {
		if (engine != null) {
			engine.reset();
		}
		super.destroy();
	}
}