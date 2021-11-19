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
package lucee.runtime.net.http;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public final class RequestDispatcherDummy implements RequestDispatcher {

	public RequestDispatcherDummy(HttpServletRequestDummy dummy) {
	}

	@Override
	public void forward(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
		throw new ServletException("operation not supported");
		// TODO impl
	}

	@Override
	public void include(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
		throw new ServletException("operation not supported");
		// TODO impl
	}

}