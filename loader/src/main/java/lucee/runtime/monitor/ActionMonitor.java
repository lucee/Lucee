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
package lucee.runtime.monitor;

import java.io.IOException;
import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Query;

// added with Lucee 4.1
public interface ActionMonitor extends Monitor {

	/**
	 * logs certain action within a Request
	 * 
	 * @param pc
	 * @param type
	 * @param label
	 * @param executionTime
	 * @param data
	 * @throws IOException
	 */
	public void log(PageContext pc, String type, String label, long executionTime, Object data) throws IOException;

	/**
	 * logs certain action outside a Request, like sending mails
	 * 
	 * @param config
	 * @param type
	 * @param label
	 * @param executionTime
	 * @param data
	 * @throws IOException
	 */
	public void log(ConfigWeb config, String type, String label, long executionTime, Object data) throws IOException;

	public Query getData(Map<String, Object> arguments) throws PageException;
}