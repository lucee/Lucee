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

import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.aprint;
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
import lucee.commons.net.http.HTTPEngine;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.lock.LockManagerImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.schedule.Scheduler;
import lucee.runtime.schedule.SchedulerImpl;
import lucee.runtime.timer.Stopwatch;
import lucee.runtime.type.scope.storage.StorageScopeFile;
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

	/**
	 * default delay in seconds before the periodic background tasks (every 10 seconds, every minute,
	 * every 5 minutes and the one-time startup cleanup) run for the first time, see LDEV-3368
	 */
	public static final long DEFAULT_STARTUP_DELAY_SECONDS = 120;

	private int interval;
	private final long startupDelay = getStartupDelay(); // in milliseconds
	private final long startupEnd; // point in time, before that the delayed tasks do not run
	private long lastMinuteInterval;
	private long last5MinuteInterval;
	private long last10SecondsInterval;
	private long lastConfigFileCheck; // the check for changes in the config file is not delayed
	private long lastHourInterval = System.currentTimeMillis();
	private volatile boolean startupCleanupPending = true;

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
		long now = System.currentTimeMillis();
		this.startupEnd = now + startupDelay;
		// the first run is due one second after the startup delay has passed (without delay that is the
		// next time the controller wakes up)
		this.last10SecondsInterval = getInitialLastRun(now, startupDelay, 10000);
		this.lastMinuteInterval = getInitialLastRun(now, startupDelay, 60000);
		this.last5MinuteInterval = getInitialLastRun(now, startupDelay, 300000);
		this.lastConfigFileCheck = getInitialLastRun(now, 0, 60000);
		this.enableGC = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.controller.gc", null), false);
	}

	/**
	 * returns the delay in milliseconds the background controller waits after it was created, before it
	 * executes the periodic tasks for the first time. Configured with the system property
	 * "lucee.controller.startup.delay" or the environment variable "LUCEE_CONTROLLER_STARTUP_DELAY",
	 * the value is in seconds.
	 */
	public static long getStartupDelay() {
		return parseStartupDelay(SystemUtil.getSystemPropOrEnvVar("lucee.controller.startup.delay", null));
	}

	/**
	 * @param value delay in seconds, an empty or invalid value results in the default, a negative value
	 *            in 0 (no delay)
	 * @return delay in milliseconds
	 */
	public static long parseStartupDelay(String value) {
		long seconds = StringUtil.isEmpty(value, true) ? DEFAULT_STARTUP_DELAY_SECONDS : Caster.toLongValue(value.trim(), DEFAULT_STARTUP_DELAY_SECONDS);
		if (seconds < 0) seconds = 0;
		return seconds * 1000;
	}

	/**
	 * value for the "last run" timestamp of a periodic task, so the task is executed for the first time
	 * one second after "start + delay"
	 */
	public static long getInitialLastRun(long start, long delay, long intervalLength) {
		return start + delay - (intervalLength - 1000);
	}

	private static class ControlerThread extends ParentThreasRefThread {
		private Controler controler;
		private CFMLFactoryImpl[] factories;
		private boolean firstRun;
		private boolean startupCleanup;
		private long done = -1;
		private Throwable t;
		private Log log;
		private long start;

		public ControlerThread(Controler controler, CFMLFactoryImpl[] factories, boolean firstRun, boolean startupCleanup, Log log) {
			this.start = System.currentTimeMillis();
			this.startupCleanup = startupCleanup;
			this.controler = controler;
			this.factories = factories;
			this.firstRun = firstRun;
			this.log = log;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			try {
				controler.control(factories, firstRun, startupCleanup, log);
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
			// the one-time cleanup of temp/cache/client files is disk intensive, so it waits until the startup
			// delay has passed (LDEV-3368)
			boolean startupCleanup = false;
			if (startupCleanupPending && factories.length > 0 && System.currentTimeMillis() >= startupEnd) {
				startupCleanup = true;
				startupCleanupPending = false;
			}
			ControlerThread ct = new ControlerThread(this, factories, firstRun, startupCleanup, configServer.getLog("application"));
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
			IOUtil.write(target, line, StandardCharsets.UTF_8, true);
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

	public static boolean isDeployCheckDue(boolean do10Seconds, ConfigServer cs) {
		return do10Seconds && ConfigUtil.getConfigServerImpl(cs).isDeployWatch();
	}

	private void control(CFMLFactoryImpl[] factories, boolean firstRun, boolean startupCleanup, Log log) {
		long now = System.currentTimeMillis();
		boolean do10Seconds = last10SecondsInterval + 10000 < now;
		if (do10Seconds) last10SecondsInterval = now;

		boolean doMinute = lastMinuteInterval + 60000 < now;
		if (doMinute) lastMinuteInterval = now;

		boolean do5Minute = last5MinuteInterval + 300000 < now;
		if (do5Minute) last5MinuteInterval = now;

		// checking the config file for changes is cheap and a change must be picked up quickly, so it is
		// not delayed (LDEV-3368)
		boolean doConfigFileCheck = lastConfigFileCheck + 60000 < now;
		if (doConfigFileCheck) lastConfigFileCheck = now;

		boolean doHour = (lastHourInterval + (1000 * 60 * 60)) < now;
		if (doHour) lastHourInterval = now;

		// every 10 seconds (the deploy folder is already processed synchronously at startup, so the
		// periodic check can wait for the startup delay), unless disabled with deployWatch (LDEV-5018)
		if (isDeployCheckDue(do10Seconds, configServer)) {
			// deploy extensions, archives ...
			try {
				DeployHandler.deploy(configServer, configServer.getLog("deploy"), false);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (log != null) log.error("controler", t);
			}
		}
		// every minute: check the config file for changes (not delayed)
		if (doConfigFileCheck) {

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
			control(factories[i], do10Seconds, doMinute, doHour, startupCleanup, i, log);
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

	private void control(CFMLFactoryImpl cfmlFactory, boolean do10Seconds, boolean doMinute, boolean doHour, boolean startupCleanup, int index, Log log) {

		try {
			boolean isRunning = cfmlFactory.getUsedPageContextLength() > 0;
			if (isRunning) {
				cfmlFactory.checkTimeout();
			}
			Stopwatch stopwatch = new Stopwatch(Stopwatch.UNIT_MILLI);
			final ConfigWeb config = cfmlFactory.getConfig();

			// when we are in single mode, some service in ConfigWeb point to the same ConfigServer, so we only
			// need to execute them once.
			final boolean doit = index == 0;

			ThreadLocalConfig.register(config);

			if (startupCleanup) {

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

				LogUtil.log(ThreadLocalPageContext.getConfigServer(config), Log.LEVEL_TRACE, Controler.class.getName(),
						"Running background Controller maintenance (every minute).");

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
						HTTPEngine.closeIdleConnections();
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

				LogUtil.log(ThreadLocalPageContext.getConfigServer(config), Log.LEVEL_TRACE, Controler.class.getName(), "Running background Controller maintenance (every hour).");

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

		// list once
		Resource[] files = filter == null ? dir.listResources() : dir.listResources(filter);
		if (files == null || files.length == 0) return;

		// calculate size (recursive, matching original getRealSize behaviour)
		int count = files.length;
		long size = 0;
		for (Resource f: files) {
			size += ResourceUtil.getRealSize(f);
		}

		LogUtil.log(ThreadLocalPageContext.getConfigServer(config), Log.LEVEL_DEBUG, Controler.class.getName(),
				"Checking size of directory [" + dir + "]. Current size [" + size + "]. Max size [" + maxSize + "].");

		if (count <= 100000 && size <= maxSize) return;

		LogUtil.log(ThreadLocalPageContext.getConfigServer(config), Log.LEVEL_WARN, Controler.class.getName(),
				"Removing files from directory [" + dir + "]. Current size [" + size + "]. Max size [" + maxSize + "]. Number of files [" + count + "]");

		// sort oldest first
		Arrays.sort(files, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));

		// keep deleting the oldest files first, until under both thresholds
		int deleted = 0;
		int failed = 0;
		for (Resource f: files) {
			if (count <= 100000 && size <= maxSize) break;
			long fSize = ResourceUtil.getRealSize(f);
			try {
				f.remove(true);
				count--;
				size -= fSize;
				deleted++;
				// periodically yield to avoid blocking filesystem
				if (deleted % 100 == 0) Thread.sleep(15);
			}
			catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				break;
			}
			catch (Exception e) {
				failed++;
			}
		}

		if (deleted > 0 || failed > 0) {
			LogUtil.log(ThreadLocalPageContext.getConfigServer(config), Log.LEVEL_WARN, Controler.class.getName(), "Cleanup of directory [" + dir + "]: removed [" + deleted
					+ "] files, failed [" + failed + "]. Remaining size [" + size + "], remaining files [" + count + "].");
		}
	}

	private void checkStopWatch(ConfigWeb config, Stopwatch stopwatch, String name) {
		long time = stopwatch.stop();
		stopwatch.reset();
		if (STEP_REPORT_THRESHOLD_MS > 0 && time > STEP_REPORT_THRESHOLD_MS)
			LogUtil.log(ThreadLocalPageContext.getConfigServer(config), Log.LEVEL_INFO, Controler.class.getName(), name + " took " + time + "ms");
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
