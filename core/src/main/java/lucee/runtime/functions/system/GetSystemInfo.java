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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;

import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.ScopeContext;

public final class GetSystemInfo implements Function {

	private static final long serialVersionUID = 1L;

	public static Struct call(PageContext pc) {
		Struct sct = new StructImpl();
		Struct dsPoolInfo = new StructImpl();
		ConfigWebPro config = (ConfigWebPro) pc.getConfig();
		CFMLFactoryImpl factory = (CFMLFactoryImpl) config.getFactory();
		ScopeContext sc = factory.getScopeContext();
		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

		// threads/requests
		sct.put("activeRequests", factory.getActiveRequests());
		sct.put("activeThreads", factory.getActiveThreads());
		sct.put("queueRequests", config.getThreadQueue().size());

		// Datasource connections
		{
			// TODO provide more data
			int idle = 0, active = 0, waiters = 0;
			for (DatasourceConnPool pool: config.getDatasourceConnectionPools()) {
				idle += pool.getNumIdle();
				active += pool.getNumActive();
				waiters += pool.getNumWaiters();
				if ((pool.getNumActive() + pool.getNumIdle() + pool.getNumWaiters()) == 0) {
					continue;
				}
				String dsName = StringUtil.emptyIfNull(pool.getFactory().getDatasource().getName());
				String dsUser = StringUtil.emptyIfNull(pool.getFactory().getUsername());
				String dsHash = Integer.toHexString(pool.getFactory().getDatasource().hashCode());
				Struct dc = new StructImpl();
				dc.put("activeDatasourceConnections", pool.getNumActive());
				dc.put("idleDatasourceConnections", pool.getNumIdle());
				dc.put("waitingForConn", pool.getNumWaiters());
				dc.put("jdbcDriverClass", pool.getFactory().getDatasource().getClassDefinition().getName());
				dc.put("name", dsName);
				dc.put("username", dsUser);
				dc.put("connectionString", pool.getFactory().getDatasource().getConnectionStringTranslated());
				dsPoolInfo.put(dsName + ":" + dsUser + ":" + dsHash, dc);
			}
			sct.put("activeDatasourceConnections", active);
			sct.put("idleDatasourceConnections", idle);
			sct.put("waitingForConn", waiters);
			sct.put("datasourceConnections", dsPoolInfo);
		}

		// HTTP connection pool
		{
			Struct httpPoolInfo = new StructImpl();
			int httpLeased = 0, httpAvailable = 0, httpPending = 0, httpMax = 0;
			for (PoolingHttpClientConnectionManager cm: HTTPEngine4Impl.getConnectionManagers().values()) {
				PoolStats totals = cm.getTotalStats();
				httpLeased += totals.getLeased();
				httpAvailable += totals.getAvailable();
				httpPending += totals.getPending();
				httpMax += totals.getMax();

				for (HttpRoute route: cm.getRoutes()) {
					PoolStats stats = cm.getStats(route);
					if ((stats.getLeased() + stats.getAvailable() + stats.getPending()) == 0) continue;

					Struct hc = new StructImpl();
					hc.put("activeHttpConnections", stats.getLeased());
					hc.put("idleHttpConnections", stats.getAvailable());
					hc.put("waitingForHttpConn", stats.getPending());
					hc.put("max", stats.getMax());
					hc.put("route", route.getTargetHost().toHostString());
					httpPoolInfo.put(route.getTargetHost().toHostString(), hc);
				}
			}
			sct.put("activeHttpConnections", httpLeased);
			sct.put("idleHttpConnections", httpAvailable);
			sct.put("waitingForHttpConn", httpPending);
			sct.put("maxHttpConnections", httpMax);
			sct.put("httpConnections", httpPoolInfo);
		}

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

	public static void getCPU(Struct data) {
		Object process = Double.valueOf(0);
		Object system = Double.valueOf(0);
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			AttributeList list = mbs.getAttributes(name, new String[] { "ProcessCpuLoad", "SystemCpuLoad" });
			// Process
			if (list.size() >= 1) {
				Attribute attr = (Attribute) list.get(0);
				Object obj = attr.getValue();
				if (obj instanceof Double && !Double.isNaN(((Double) obj).doubleValue())) process = obj;
			}

			// System
			if (list.size() >= 2) {
				Attribute attr = (Attribute) list.get(1);
				Object obj = attr.getValue();
				if (obj instanceof Double && !Double.isNaN(((Double) obj).doubleValue())) system = obj;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			data.setEL("cpuProcess", process);
			data.setEL("cpuSystem", system);
		}
	}

}