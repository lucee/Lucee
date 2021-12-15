/**
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
package lucee.runtime.net.amf;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.runtime.config.ConfigWeb;

/**
 * AMF Engine
 */
public interface AMFEngine {

	/**
	 * method to initialize the engine
	 * 
	 */
	public void init(ConfigWeb config, Map<String, String> arguments) throws IOException;

	/**
	 * Main entry point for the AMF (Flex) Engine
	 * 
	 * @param servlet
	 * @param req
	 * @param rsp
	 *
	 * @throws IOException
	 */
	public void service(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws IOException;
}