/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.loader.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.EngineChangeListener;

public abstract class AbsServlet extends HttpServlet implements EngineChangeListener {

	private static final long serialVersionUID = 3911001884655921666L;

	protected CFMLEngine engine;

	@Override
	public void onUpdate() {
		try {
			// make sure that config is registered
			engine = CFMLEngineFactory.getInstance(getServletConfig(), this);
		}
		catch (final ServletException e) {

		}
	}
}