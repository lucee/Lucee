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
package lucee.runtime.gateway;

import java.util.Map;

public interface GatewayEngine {

	public static final int LOGLEVEL_INFO = 0;
	public static final int LOGLEVEL_DEBUG = 1;
	public static final int LOGLEVEL_WARN = 2;
	public static final int LOGLEVEL_ERROR = 3;
	public static final int LOGLEVEL_FATAL = 4;
	public static final int LOGLEVEL_TRACE = 5;

	/**
	 * invoke given method on cfc listener
	 * 
	 * @param gateway
	 * @param method method to invoke
	 * @param data arguments
	 * @return returns if invocation was successfull
	 */
	public boolean invokeListener(Gateway gateway, String method, Map<?, ?> data);

	/**
	 * logs message with defined logger for gateways
	 * 
	 * @param gateway
	 * @param level
	 * @param message
	 */
	public void log(Gateway gateway, int level, String message);

}