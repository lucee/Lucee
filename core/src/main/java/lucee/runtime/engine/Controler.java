/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.runtime.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.aprint;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.ParentThreasRefThread;
import lucee.commons.lang.PhysicalClassLoaderFactory;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.lock.LockManagerImpl;
import lucee.runtime.net.smtp.SMTPConnectionPool;
import lucee.runtime.op.Caster;
import lucee.runtime.schedule.Scheduler;
import lucee.runtime.schedule.SchedulerImpl;
import lucee.runtime.timer.Stopwatch;
import lucee.runtime.type.scope.storage.StorageScopeFile;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.dynamic.DynamicInvoker;

/**
 * own thread how check the main thread and his data
 */
public final class Controler extends ParentThreasRefThread {

	private static final long TIMEOUT = 50 * 1000;
	private static final long STEP_REPORT_THRESHOLD_MS = 1000 * Caster.toLongValue(SystemUtil.getSystemPropOrEnvVar("lucee.controller.log.threshold", ""), 20); // threshold for
																																								// reporting out how
																																								// long controller
																																								// steps took

	private static final ControllerState INACTIVE = new ControllerStateImpl(false);

	private int interval;
	private long lastMinuteInterval = System.currentTimeMillis() - (1000 * 59); // first after a second
	private long last5MinuteInterval = System.currentTimeMillis() - (1000 * 299); // first after a second
	private long last10SecondsInterval = System.currentTimeMillis() - (1000 * 9); // first after a second
	private long lastHourInterval = System.currentTimeMillis();

	private final Map contextes;
	// private ScheduleThread scheduleThread;
	private final ConfigServer configServer;
	// private final ShutdownHook shutdownHook;
	private ControllerState state;

	// private boolean poolValidate;
	private boolean enableGC;

	/**
	 * @param configServer
	 * @param contextes
	 * @param interval
	 * @param state
	 */
	public Controler(ConfigServer configServer, Map contextes, int interval, ControllerState state) {
		this.contextes = contextes;
		this.interval = interval;
		this.state = state;
		this.configServer = configServer;
		if (configServer == null) throw new RuntimeException("configServer cannot be null");
		this.enableGC = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.controller.gc", null), false);
	}

	private static class ControlerThread extends ParentThreasRefThread {
		private Controler controler;
		private CFMLFactoryImpl[] factories;
		private boolean firstRun;
		private long done = -1;
		private Throwable t;
		private Log log;
		private long start;

		public ControlerThread(Controler controler, CFMLFactoryImpl[] factories, boolean firstRun, Log log) {
			this.start = System.currentTimeMillis();
			this.controler = controler;
			this.factories = factories;
			this.firstRun = firstRun;
			this.log = log;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			try {
				controler.control(factories, firstRun, log);
				done = System.currentTimeMillis() - start;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				this.t = t;
			}
			// long time=System.currentTimeMillis()-start;
			// if(time>10000) {
			// log.info("controller", "["+hashCode()+"] controller was running for "+time+"ms");
			// }
		}
	}

	@Override
	public void run() {
		// scheduleThread.start();
		boolean firstRun = true;
		long count = 0;
		List<ControlerThread> threads = new ArrayList<ControlerThread>();
		CFMLFactoryImpl factories[] = null;
		while (state.active()) {
			// sleep
			SystemUtil.wait(this, interval);
			if (!state.active()) break;

			factories = toFactories(factories, contextes);
			// start the thread that calls control
			ControlerThread ct = new ControlerThread(this, factories, firstRun, configServer.getLog("application"));
			ct.setName("ControllerThread:" + (++count));
			ct.start();
			threads.add(ct);

			if (threads.size() > 10 && lastMinuteInterval + 60000 < System.currentTimeMillis())
				configServer.getLog("application").info("controller", threads.size() + " active controller threads");

			// now we check all threads we have
			Iterator<ControlerThread> it = threads.iterator();
			long time;
			while (it.hasNext()) {
				ct = it.next();
				// print.e(ct.hashCode());
				time = System.currentTimeMillis() - ct.start;
				// done
				if (ct.done >= 0) {
					if (time > 10000) configServer.getLog("application").info("controller", "controller took " + ct.done + "ms to execute successfully.");
					it.remove();
				}
				// failed
				else if (ct.t != null) {
					addParentStacktrace(ct.t);
					configServer.getLog("application").log(Log.LEVEL_ERROR, "controler", ct.t);
					it.remove();
				}
				// stop it!
				else if (time > TIMEOUT) {
					SystemUtil.stop(ct);
					// print.e(ct.getStackTrace());
					if (!ct.isAlive()) {
						configServer.getLog("application").error("controller", "controller thread [" + ct.hashCode() + "] forced to stop after " + time + "ms");
						it.remove();
					}
					else {
						Throwable t = new Throwable();
						t.setStackTrace(ct.getStackTrace());

						configServer.getLog("application").log(Log.LEVEL_ERROR, "controler", "was not able to stop controller thread running for " + time + "ms", t);
					}
				}
			}
			if (factories.length > 0) firstRun = false;
		}
	}

	public static void dumpThreadPositions(Resource target) throws IOException {

		StackTraceElement[] stes;
		String line;
		List<StackTraceElement> elements;
		for (Entry<Thread, StackTraceElement[]> e: Thread.getAllStackTraces().entrySet()) {
			stes = e.getValue();
			if (stes == null || stes.length == 0) continue;
			elements = new ArrayList<>();
			for (int i = 0; i < stes.length; i++) {
				if (stes[i].getLineNumber() > 0) {
					elements.add(stes[i]);
				}
			}
			if (elements.size() == 0) continue;
			// print.e(stes);
			line = "{\"stack\":[";
			String del = "";
			for (StackTraceElement ste: elements) {
				line += (del + "\"" + ste.getClassName() + "." + (StringUtil.isEmpty(ste.getMethodName()) ? "<init>" : ste.getMethodName()) + "():" + ste.getLineNumber() + "\"");
				del = ",";
			}

			line += "],\"thread\":\"" + e.getKey().getName() + "\",\"id\":" + e.getKey().getId() + ",\"time\":" + System.currentTimeMillis() + "}\n";
			IOUtil.write(target, line, CharsetUtil.UTF8, true);
		}

	}

	private static void dumpThreads() {
		aprint.e("==================== THREAD DUMP " + new Date() + " ====================");
		for (Entry<Thread, StackTraceElement[]> e: Thread.getAllStackTraces().entrySet()) {
			aprint.e(e.getKey().getName() + ":" + e.getKey().getId() + " " + e.getKey().getState());
			aprint.e(ExceptionUtil.getStacktrace(e.getValue()));
			aprint.e("------------------------------------------------------------------");
		}
		aprint.e("==================================================================");

	}

	private void control(CFMLFactoryImpl[] factories, boolean firstRun, Log log) {
		long now = System.currentTimeMillis();
		boolean do10Seconds = last10SecondsInterval + 10000 < now;
		if (do10Seconds) last10SecondsInterval = now;

		boolean doMinute = lastMinuteInterval + 60000 < now;
		if (doMinute) lastMinuteInterval = now;

		boolean do5Minute = last5MinuteInterval + 300000 < now;
		if (do5Minute) last5MinuteInterval = now;

		boolean doHour = (lastHourInterval + (1000 * 60 * 60)) < now;
		if (doHour) lastHourInterval = now;

		// every 10 seconds
		if (do10Seconds) {
			// deploy extensions, archives ...
			try {
				DeployHandler.deploy(configServer, configServer.getLog("deploy"), false);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (log != null) log.error("controler", t);
			}
		}
		// every minute
		if (doMinute) {

			try {
				ConfigAdmin.checkForChangesInConfigFile(configServer);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (log != null) log.error("controler", t);
			}
		}
		Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_MILLI);
		for (int i = 0; i < factories.length; i++) {
			stopwatch.start();
			ConfigWeb config = factories[i].getConfig();
			control(factories[i], do10Seconds, doMinute, doHour, firstRun, i, log);
			checkStopWatch(config, stopwatch, "Web Context [" + config.getRootDirectory().getAbsolutePath() + "]");
		}

		if (firstRun) {

			try {
				RHExtension.correctExtensions(configServer);
			}
			catch (Exception e) {
				if (log != null) log.error("controler", e);
			}
		}

		// every 5 minutes
		if (this.enableGC && do5Minute) {
			try {
				System.gc();
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (log != null) log.error("controler", t);
			}
		}
	}

	private void control(CFMLFactoryImpl cfmlFactory, boolean do10Seconds, boolean doMinute, boolean doHour, boolean firstRun, int index, Log log) {

		try {
			boolean isRunning = cfmlFactory.getUsedPageContextLength() > 0;
			if (isRunning) {
				cfmlFactory.checkTimeout();
			}
			Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_MILLI);
			final ConfigWeb config = cfmlFactory.getConfig();
			final boolean isSingle = ((ConfigPro) config).getAdminMode() == ConfigImpl.ADMINMODE_SINGLE;

			// when we are in single mode, some service in ConfigWeb point to the same ConfigServer, so we only
			// need to execute them once.
			final boolean doit = !isSingle || index == 0;

			ThreadLocalConfig.register(config);

			if (firstRun) {

				checkOldClientFile(config, log);

				if (doit) {
					stopwatch.start();
					try {
						checkTempDirectorySize(config);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						if (log != null) log.error("controler", t);
					}
					checkStopWatch(config, stopwatch, "checkTempDirectorySize");
				}

				if (doit) {
					stopwatch.start();
					try {
						checkCacheFileSize(config);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						if (log != null) log.error("controler", t);
					}
					checkStopWatch(config, stopwatch, "checkCacheFileSize");
				}

				stopwatch.start();
				try {
					cfmlFactory.getScopeContext().clearUnused();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				checkStopWatch(config, stopwatch, "clearUnused");
			}

			ThreadLocalConfig.register(config);
			if (do10Seconds) {

			}

			// every Minute
			if (doMinute) {
				ThreadLocalConfig.register(config);

				LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_TRACE, Controler.class.getName(), "Running background Controller maintenance (every minute).");

				stopwatch.start();
				try {
					Scheduler scheduler = config.getScheduler();
					if (scheduler != null) ((SchedulerImpl) scheduler).startIfNecessary();
				}
				catch (Exception e) {
					if (log != null) log.error("controler", e);
				}
				checkStopWatch(config, stopwatch, "checkScheduler");

				// double check templates
				stopwatch.start();
				try {
					((ConfigWebPro) config).getCompiler().checkWatched();
				}
				catch (Exception e) {
					if (log != null) log.error("controler", e);
				}
				checkStopWatch(config, stopwatch, "checkTemplates");

				/*
				 * stopwatch.start(); // deploy extensions, archives ... try { DeployHandler.deploy(config,
				 * ThreadLocalPageContext.getLog(config, "deploy"), false); } catch (Throwable t) {
				 * ExceptionUtil.rethrowIfNecessary(t); if (log != null) log.error("controler", t); }
				 * checkStopWatch(config, stopwatch, "deploy");
				 */

				// clear unused DB Connections
				if (doit) {
					stopwatch.start();
					try {
						for (DatasourceConnPool pool: ((ConfigPro) config).getDatasourceConnectionPools()) {
							try {
								pool.evict();
							}
							catch (Exception ex) {
								if (log != null) log.error("controler", ex);
							}
						}
					}
					catch (Exception e) {
						if (log != null) log.error("controler", e);
					}
					checkStopWatch(config, stopwatch, "clearUnusedDBConnections");

					stopwatch.start();
					try {
						((ConfigPro) config).cleanDatasourceConnectionPools();
					}
					catch (Exception e) {
						if (log != null) log.error("controler", e);
					}
					checkStopWatch(config, stopwatch, "cleanDatasourceConnectionPools");
				}

				if (doit) {
					stopwatch.start();
					// Clear unused http connections
					try {
						HTTPEngine4Impl.closeIdleConnections();
					}
					catch (Exception e) {
						if (log != null) log.error("controler", e);
					}
					checkStopWatch(config, stopwatch, "clearUnusedHttpConnections");
				}

				// clear all unused scopes
				stopwatch.start();
				try {
					cfmlFactory.getScopeContext().clearUnused();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				checkStopWatch(config, stopwatch, "clearUnusedScopes");

				stopwatch.start();
				try {
					doCheckMappings(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				checkStopWatch(config, stopwatch, "checkMappings");

				if (doit) {
					stopwatch.start();
					try {
						doClearMailConnections();
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						if (log != null) log.error("controler", t);
					}
					checkStopWatch(config, stopwatch, "clearMailConnections");
				}

				// clean LockManager
				stopwatch.start();
				if (cfmlFactory.getUsedPageContextLength() == 0) try {
					((LockManagerImpl) config.getLockManager()).clean();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (log != null) log.error("controler", t);
				}
				checkStopWatch(config, stopwatch, "cleanLockManager");

				/*
				 * stopwatch.start(); try { ConfigAdmin.checkForChangesInConfigFile(config); } catch (Throwable t) {
				 * ExceptionUtil.rethrowIfNecessary(t); if (log != null) log.error("controler", t); }
				 * checkStopWatch(config, stopwatch, "checkForChangesInConfigFile");
				 */

			}
			// every hour
			if (doHour) {

				LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_TRACE, Controler.class.getName(), "Running background Controller maintenance (every hour).");

				ThreadLocalConfig.register(config);

				// check temp directory
				if (doit) {
					stopwatch.start();
					try {
						checkTempDirectorySize(config);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						if (log != null) log.error("controler", t);
					}
					checkStopWatch(config, stopwatch, "checkTempDirectorySize");
				}

				// check cache directory
				if (doit) {
					stopwatch.start();
					try {
						checkCacheFileSize(config);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						if (log != null) log.error("controler", t);
					}
					checkStopWatch(config, stopwatch, "checkCacheFileSize");
				}

				// clear RPC
				if (doit) {
					stopwatch.start();
					try {
						checkRPC(config);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						if (log != null) log.error("controler", t);
					}
					checkStopWatch(config, stopwatch, "checkRPC");
				}

				// clean up dynclasses
				if (doit) {
					stopwatch.start();
					try {
						DynamicInvoker di = DynamicInvoker.getExistingInstance();
						if (di != null) di.cleanup();
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						if (log != null) log.error("controler", t);
					}
					checkStopWatch(config, stopwatch, "cleanupDynamicInvoker");
				}

				// clean up PhysicalClassLoader
				if (doit) {
					stopwatch.start();
					try {
						PhysicalClassLoaderFactory.clean(config);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						if (log != null) log.error("controler", t);
					}
					checkStopWatch(config, stopwatch, "cleanupPhysicalClassLoaderFactory");
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (log != null) log.error("controler", t);
		}
		finally {
			ThreadLocalConfig.release();
		}
	}

	private CFMLFactoryImpl[] toFactories(CFMLFactoryImpl[] factories, Map contextes) {
		if (factories == null || factories.length != contextes.size()) factories = (CFMLFactoryImpl[]) contextes.values().toArray(new CFMLFactoryImpl[contextes.size()]);

		return factories;
	}

	private void doClearMailConnections() {
		SMTPConnectionPool.closeSessions();
	}

	private void checkOldClientFile(ConfigWeb config, Log log) {
		ExtensionResourceFilter filter = new ExtensionResourceFilter(".script");

		// move old structured file in new structure
		try {
			Resource dir = config.getClientScopeDir(), trgres;
			Resource[] children = dir.listResources(filter);
			if (children == null) return;
			String src, trg;
			int index;
			for (int i = 0; i < children.length; i++) {
				src = children[i].getName();
				index = src.indexOf('-');

				trg = StorageScopeFile.getFolderName(src.substring(0, index), src.substring(index + 1), false);
				trgres = dir.getRealResource(trg);
				if (!trgres.exists()) {
					trgres.createFile(true);
					ResourceUtil.copy(children[i], trgres);
				}
				// children[i].moveTo(trgres);
				children[i].delete();

			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (log != null) log.error("controler", t);
		}
	}

	private void checkCacheFileSize(ConfigWeb config) {
		checkSize(config, config.getCacheDir(), config.getCacheDirSize(), new ExtensionResourceFilter(".cache"));
	}

	private void checkRPC(ConfigWeb config) {
		checkSize(config, config.getClassDirectory().getRealResource("RPC/"), 1024 * 1024 * 1024, null);
	}

	private void checkTempDirectorySize(ConfigWeb config) {
		checkSize(config, config.getTempDirectory(), 1024 * 1024 * 1024, null);

	}

	private void checkSize(ConfigWeb config, Resource dir, long maxSize, ResourceFilter filter) {
		if (dir == null || !dir.exists()) return;
		Resource res = null;
		int count = ArrayUtil.size(filter == null ? dir.list() : dir.list(filter));
		long size = ResourceUtil.getRealSize(dir, filter);
		LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_DEBUG, Controler.class.getName(),
				"Checking size of directory [" + dir + "]. Current size [" + size + "]. Max size [" + maxSize + "].");

		int len = -1;

		if (count > 100000 || size > maxSize) {
			LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_WARN, Controler.class.getName(),
					"Removing files from directory [" + dir + "]. Current size [" + size + "]. Max size [" + maxSize + "]. Number of files [" + count + "]");
		}

		while (count > 100000 || size > maxSize) {
			Resource[] files = filter == null ? dir.listResources() : dir.listResources(filter);
			if (len == files.length) break;// protect from inifinti loop
			len = files.length;
			for (int i = 0; i < files.length; i++) {
				if (res == null || res.lastModified() > files[i].lastModified()) {
					res = files[i];
				}
			}
			if (res != null) {
				size -= res.length();
				try {
					res.remove(true);
					count--;
				}
				catch (Exception e) {
					LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, Controler.class.getName(), "cannot remove resource [" + res.getAbsolutePath() + "]");
					break;
				}
			}
			res = null;
		}

	}

	private void checkStopWatch(ConfigWeb config, Stopwatch stopwatch, String name) {
		long time = stopwatch.stop();
		stopwatch.reset();
		if (STEP_REPORT_THRESHOLD_MS > 0 && time > STEP_REPORT_THRESHOLD_MS)
			LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, Controler.class.getName(), name + " took " + time + "ms");
	}

	private void doCheckMappings(ConfigWeb config) {
		lucee.runtime.config.ConfigWebImpl d;
		if (config instanceof ConfigWebPro) {
			((ConfigWebPro) config).checkMappings();
		}
		else {
			for (Mapping mapping: config.getMappings()) {
				mapping.check();
			}
		}
	}

	public void close() {
		state = INACTIVE;
		SystemUtil.notify(this);
	}

	static class ExpiresFilter implements ResourceFilter {

		private long time;
		private boolean allowDir;

		public ExpiresFilter(long time, boolean allowDir) {
			this.allowDir = allowDir;
			this.time = time;
		}

		@Override
		public boolean accept(Resource res) {

			if (res.isDirectory()) return allowDir;

			// load content
			String str = null;
			try {
				str = IOUtil.toString(res, "UTF-8");
			}
			catch (IOException e) {
				return false;
			}

			int index = str.indexOf(':');
			if (index != -1) {
				long expires = Caster.toLongValue(str.substring(0, index), -1L);
				// check is for backward compatibility, old files have no expires date inside. they do ot expire
				if (expires != -1) {
					if (expires < System.currentTimeMillis()) {
						return true;
					}
					str = str.substring(index + 1);
					return false;
				}
			}
			// old files not having a timestamp inside
			else if (res.lastModified() <= time) {
				return true;

			}
			return false;
		}
	}
}
