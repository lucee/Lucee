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
package lucee.runtime.monitor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;

public class ActionMonitorWrap extends MonitorWrap implements ActionMonitor {

	private static final Class[] PARAMS_LOG1 = new Class[] { PageContext.class, String.class, String.class, long.class, Object.class };
	private static final Class[] PARAMS_LOG2 = new Class[] { ConfigWeb.class, String.class, String.class, long.class, Object.class };

	private Method log;
	private Method getData;

	public ActionMonitorWrap(Object monitor) {
		super(monitor, TYPE_ACTION);
	}

	@Override
	public void log(PageContext pc, String type, String label, long executionTime, Object data) throws IOException {
		try {
			if (log == null) {
				log = monitor.getClass().getMethod("log", PARAMS_LOG1);
			}
			log.invoke(monitor, new Object[] { pc, type, label, Caster.toLong(executionTime), data });
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public void log(ConfigWeb config, String type, String label, long executionTime, Object data) throws IOException {
		try {
			if (log == null) {
				log = monitor.getClass().getMethod("log", PARAMS_LOG2);
			}
			log.invoke(monitor, new Object[] { config, type, label, Caster.toLong(executionTime), data });
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public Query getData(Map<String, Object> arguments) throws PageException {
		try {
			if (getData == null) {
				getData = monitor.getClass().getMethod("getData", new Class[] { Map.class });
			}
			return (Query) getData.invoke(monitor, new Object[] { arguments });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

}