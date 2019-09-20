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
package lucee.cli;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;

import lucee.loader.util.Util;

public class CLI {

	/*
	 * Config
	 * 
	 * webroot - webroot directory servlet-name - name of the servlet (default:CFMLServlet) server-name
	 * - server name (default:localhost) uri - host/scriptname/query cookie - cookies (same pattern as
	 * query string) form - form (same pattern as query string)
	 */

	public static void main(final String[] args) throws ServletException, IOException, JspException {

		final Map<String, String> config = toMap(args);

		System.setProperty("lucee.cli.call", "true");

		final boolean useRMI = "true".equalsIgnoreCase(config.get("rmi"));

		File root;
		final String param = config.get("webroot");
		if (Util.isEmpty(param, true)) {

			root = new File("."); // working directory that the java command was called from
			config.put("webroot", root.getAbsolutePath());
		}
		else {

			root = new File(param);
			root.mkdirs();
		}

		String servletName = config.get("servlet-name");

		if (Util.isEmpty(servletName, true)) servletName = "CFMLServlet";

		if (useRMI) {

			final CLIFactory factory = new CLIFactory(root, servletName, config);
			factory.setDaemon(false);
			factory.start();
		}
		else {

			final CLIInvokerImpl invoker = new CLIInvokerImpl(root, servletName);
			invoker.invoke(config);
		}
	}

	private static Map<String, String> toMap(final String[] args) {

		int index;
		String raw, key, value;

		final Map<String, String> config = new HashMap<String, String>();

		if (args != null && args.length > 0) for (final String arg: args) {

			raw = arg.trim();
			if (raw.startsWith("-")) raw = raw.substring(1);

			if (!raw.isEmpty()) {

				index = raw.indexOf('=');
				if (index == -1) {
					key = raw;
					value = "";
				}
				else {
					key = raw.substring(0, index).trim();
					value = raw.substring(index + 1).trim();
				}

				config.put(key.toLowerCase(), value);
			}
		}

		return config;
	}
}