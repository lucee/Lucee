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


import lucee.commons.io.IOUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
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

		sct.put("startTime", new DateTimeImpl(config.getEngine().uptime()));

		// threads/requests
		sct.put("activeRequests", factory.getActiveRequests());
		sct.put("activeThreads", factory.getActiveThreads());
		sct.put("queueRequests", config.getThreadQueue().size());

		// PageContext pool depth (running = activeRequests, childRunning = activeThreads)
		{
			Struct pcPool = new StructImpl();
			pcPool.put("idle", factory.getIdlePCCount());
			pcPool.put("max", CFMLFactoryImpl.PC_POOL_MAX_SIZE);
			sct.put("pageContextPool", pcPool);
		}

		// Throttle state (only when active)
		if (CFMLFactoryImpl.THROTTLE_ENABLED) {
			Struct throttle = new StructImpl();
			throttle.put("maxNoSleep", CFMLFactoryImpl.MAX_NO_SLEEP);
			throttle.put("sleepTime", CFMLFactoryImpl.SLEEP_TIME);
			throttle.put("maxNormalPriority", CFMLFactoryImpl.MAX_NORMAL_PRIORITY);
			throttle.put("firedOnce", CFMLFactoryImpl.THROTTLE_FIRED_ONCE.get());
			throttle.put("firedCount", CFMLFactoryImpl.THROTTLE_FIRED_COUNT.get());
			sct.put("throttle", throttle);
		}

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
			Struct pool = HTTPEngine.getEngine().getPoolStats();
			Struct routes = Caster.toStruct(pool.get("routes", null), null);
			Struct httpPoolInfo = new StructImpl();
			if (routes != null) {
				for (Key k: routes.keys()) {
					Struct r = Caster.toStruct(routes.get(k, null), null);
					if (r == null) continue;
					Struct hc = new StructImpl();
					hc.put("activeHttpConnections", r.get("leased", 0));
					hc.put("idleHttpConnections", r.get("available", 0));
					hc.put("waitingForHttpConn", r.get("pending", 0));
					hc.put("max", r.get("max", 0));
					hc.put("route", r.get("route", ""));
					httpPoolInfo.put(k.getString(), hc);
				}
			}
			sct.put("activeHttpConnections", pool.get("leased", 0));
			sct.put("idleHttpConnections", pool.get("available", 0));
			sct.put("waitingForHttpConn", pool.get("pending", 0));
			sct.put("maxHttpConnections", pool.get("max", 0));
			sct.put("httpConnections", httpPoolInfo);
		}

		// IOUtil buffer pools (LDEV-6410)
		{
			Struct ioBufferPools = new StructImpl();
			int blockSize = IOUtil.getDefaultBlockSize();
			int max = IOUtil.getBufferPoolMax();
			int byteBufSize = blockSize;
			int charBufSize = blockSize * 2;

			int byteIdle = IOUtil.byteBufferPoolSize();
			int byteActive = IOUtil.byteBufferActiveCount();
			Struct bytePool = new StructImpl();
			bytePool.put("max", max);
			bytePool.put("bufferSize", byteBufSize);
			bytePool.put("idle", byteIdle);
			bytePool.put("active", byteActive);
			bytePool.put("bytes", (long) Math.min(byteIdle + byteActive, max) * byteBufSize);
			ioBufferPools.put("byte", bytePool);

			int charIdle = IOUtil.charBufferPoolSize();
			int charActive = IOUtil.charBufferActiveCount();
			Struct charPool = new StructImpl();
			charPool.put("max", max);
			charPool.put("bufferSize", charBufSize);
			charPool.put("idle", charIdle);
			charPool.put("active", charActive);
			charPool.put("bytes", (long) Math.min(charIdle + charActive, max) * charBufSize);
			ioBufferPools.put("char", charPool);

			sct.put("ioBufferPools", ioBufferPools);
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