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
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;

public class RequestMonitorWrap extends MonitorWrap implements RequestMonitor {
	private static final Class[] PARAMS_LOG = new Class[] { PageContext.class, boolean.class };

	private Method log;
	private Method getData;

	private Method getDataRaw;

	public RequestMonitorWrap(Object monitor) {
		super(monitor, TYPE_REQUEST);
	}

	@Override
	public void log(PageContext pc, boolean error) throws IOException {

		try {
			if (log == null) {
				log = monitor.getClass().getMethod("log", PARAMS_LOG);
			}
			log.invoke(monitor, new Object[] { pc, Caster.toBoolean(error) });
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public Query getData(ConfigWeb config, Map<String, Object> arguments) throws PageException {
		try {
			if (getData == null) {
				getData = monitor.getClass().getMethod("getData", new Class[] { ConfigWeb.class, Map.class });
			}
			return (Query) getData.invoke(monitor, new Object[] { config, arguments });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/*
	 * public Query getData(ConfigWeb config,long minAge, long maxAge, int maxrows) throws IOException{
	 * try { if(getData==null) { getData=monitor.getClass().getMethod("getData", new
	 * Class[]{long.class,long.class,int.class}); } return (Query) getData.invoke(monitor, new
	 * Object[]{new Long(minAge),new Long(maxAge),new Integer(maxrows)}); } catch (Exception e) { throw
	 * ExceptionUtil.toIOException(e); } }
	 * 
	 * public Query getDataRaw(ConfigWeb config, long minAge, long maxAge) throws IOException { try {
	 * if(getDataRaw==null) { getDataRaw=monitor.getClass().getMethod("getDataRaw", new
	 * Class[]{ConfigWeb.class,long.class,long.class}); } return (Query) getDataRaw.invoke(monitor, new
	 * Object[]{config,new Long(minAge),new Long(maxAge)}); } catch (Exception e) { throw
	 * ExceptionUtil.toIOException(e); } }
	 */
}