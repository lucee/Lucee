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
package lucee.loader.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import lucee.loader.util.Util;

@Deprecated
public class CFMLServlet extends lucee.loader.servlet.javax.CFMLServlet {

	private static final long serialVersionUID = 1389465039826124601L;

	static final String ERROR_MESSAGE = "The Servlet [lucee.loader.servlet.CFMLServlet] is deprecated. "
			+ "Please use [lucee.loader.servlet.javax.CFMLServlet] for Java EE/javax environments (Tomcat 9, Jetty 9, Undertow 2.0, JBoss 7, WebSphere Liberty 19) "
			+ "or [lucee.loader.servlet.jakarta.CFMLServlet] for Jakarta EE environments (Tomcat 10, Jetty 11, Undertow 3.0, Payara 6, WildFly 30).";

	@Override
	@Deprecated
	public void init(final ServletConfig sg) throws ServletException {
		super.init(sg);
		if (!engine.getCastUtil().toBooleanValue(Util.getSystemPropOrEnvVar("lucee.suppress.servlet.warning", null), false)) {
			engine.getCFMLEngineFactory().log(lucee.loader.engine.LoggerImpl.LOG_WARNING, ERROR_MESSAGE);
			System.err.println(ERROR_MESSAGE);
		}
	}

}