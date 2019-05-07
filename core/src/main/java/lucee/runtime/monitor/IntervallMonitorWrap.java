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
import java.lang.reflect.Method;
import java.util.Map;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;

public class IntervallMonitorWrap extends MonitorWrap implements IntervallMonitor {
	private static final Object[] PARAMS_LOG = new Object[0];

	private Method log;
	private Method getData;

	public IntervallMonitorWrap(Object monitor) {
		super(monitor, TYPE_INTERVAL);
	}

	@Override
	public void log() throws IOException {

		try {
			if (log == null) {
				log = monitor.getClass().getMethod("log", new Class[0]);
			}
			log.invoke(monitor, PARAMS_LOG);
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