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
package lucee.runtime.functions.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.db.DatasourceConnectionPool;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.ScopeContext;

public final class GetSystemInfo implements Function {

	private static final long serialVersionUID = 1L;

	public static Struct call(PageContext pc) throws PageException {
		Struct sct = new StructImpl();
		ConfigWebImpl config = (ConfigWebImpl) pc.getConfig();
		CFMLFactoryImpl factory = (CFMLFactoryImpl) config.getFactory();
		ScopeContext sc = factory.getScopeContext();
		OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

		// threads/requests
		sct.put("activeRequests", factory.getActiveRequests());
		sct.put("activeThreads", factory.getActiveThreads());
		sct.put("queueRequests", config.getThreadQueue().size());

		// Datasource connections
		sct.put("activeDatasourceConnections", getConnections(config));

		// tasks
		sct.put("tasksOpen", config.getSpoolerEngine().getOpenTaskCount());
		sct.put("tasksClosed", config.getSpoolerEngine().getClosedTaskCount());

		// scopes
		sct.put("sessionCount", sc.getSessionCount());
		sct.put("clientCount", sc.getClientCount());
		sct.put("applicationContextCount", sc.getAppContextCount());
		
		// cpu
		getCPU(sct);
		
		return sct;
	}
	
	
	private static void getCPU(Struct data) {
		OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
		
		// need to use reflection as the impl class is not visible
		for (Method method : mxBean.getClass().getDeclaredMethods()) {
			
			if(!Modifier.isPublic(method.getModifiers())) {
				method.setAccessible(true);
			}
			
			String methodName = method.getName();
			if(
					methodName.startsWith("get") && 
					methodName.contains("Cpu") && 
					methodName.contains("Load") && 
					Modifier.isPublic(method.getModifiers())) {
				
				Double value=1d;
				try {
					value = (Double)method.invoke(mxBean);
				}
				catch (Exception e) {}
				if(value>0) { //cpuSystem
					if("getSystemCpuLoad".equals(methodName)) {
						data.setEL("cpuSystem", value);
					}
					if("getProcessCpuLoad".equals(methodName)) {
						data.setEL("cpuProcess", value);
					}
				}
			}
		}
	}

	public static int getConnections(ConfigWebImpl config) {
		int count = 0;
		DatasourceConnectionPool pool = config.getDatasourceConnectionPool();
		Iterator<Integer> it = pool.openConnections().values().iterator();
		Integer i;
		while (it.hasNext()) {
			i = it.next();
			if (i != null)
				count += i.intValue();
		}
		return count;
	}
}