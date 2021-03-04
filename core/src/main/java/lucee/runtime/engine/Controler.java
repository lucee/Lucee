/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.functions.system.PagePoolClear;
import lucee.runtime.lock.LockManagerImpl;
import lucee.runtime.net.smtp.SMTPConnectionPool;
import lucee.runtime.op.Caster;
import lucee.runtime.schedule.SchedulerImpl;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.scope.storage.StorageScopeFile;
import lucee.runtime.type.util.ArrayUtil;

/**
 * own thread how check the main thread and his data
 */
public final class Controler extends Thread {

	private static final long TIMEOUT = 50 * 1000;

	private static final ControllerState INACTIVE = new ControllerStateImpl(false);

	private int interval;
	private long lastMinuteInterval = System.currentTimeMillis() - (1000 * 59); // first after a second
	private long last10SecondsInterval = System.currentTimeMillis() - (1000 * 9); // first after a second
	private long lastHourInterval = System.currentTimeMillis();

	private final Map contextes;
	// private ScheduleThread scheduleThread;
	private final ConfigServer configServer;
	// private final ShutdownHook shutdownHook;
	private ControllerState state;

	private boolean poolValidate;

	/**
	 * @param contextes
	 * @param interval
	 * @param run
	 */
	public Controler(ConfigServer configServer, Map contextes, int interval, ControllerState state) {
		this.contextes = contextes;
		this.interval = interval;
		this.state = state;
		this.configServer = configServer;
		this.poolValidate = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.datasource.pool.validate", null), true);
		// shutdownHook=new ShutdownHook(configServer);
		// Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	private static class ControlerThread extends Thread {
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
				controler.control(factories, firstRun);
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
		List<ControlerThread> threads = new ArrayList<ControlerThread>();
		CFMLFactoryImpl factories[] = null;
		while (state.active()) {

			// sleep
			SystemUtil.wait(this, interval);
			if (!state.active()) break;

			factories = toFactories(factories, contextes);
			// start the thread that calls control
			ControlerThread ct = new ControlerThread(this, factories, firstRun, configServer.getLog("application"));
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

	private void control(CFMLFactoryImpl[] factories, boolean firstRun) {
		long now = System.currentTimeMillis();
		boolean do10Seconds = last10SecondsInterval + 10000 < now;
		if (do10Seconds) last10SecondsInterval = now;

		boolean doMinute = lastMinuteInterval + 60000 < now;
		if (doMinute) lastMinuteInterval = now;

		boolean doHour = (lastHourInterval + (1000 * 60 * 60)) < now;
		if (doHour) lastHourInterval = now;

		// broadcast cluster scope
		try {
			ScopeContext.getClusterScope(configServer, true).broadcast();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		if (firstRun) {
			try {
				RHExtension.correctExtensions(configServer);
			}
			catch (Exception e) {
			}
		}

		// every 10 seconds
		if (do10Seconds) {
			// deploy extensions, archives ...
			// try{DeployHandler.deploy(configServer);}catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}
		}
		// every minute
		if (doMinute) {
			// deploy extensions, archives ...
			try {
				DeployHandler.deploy(configServer);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			try {
				ConfigAdmin.checkForChangesInConfigFile(configServer);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		// every hour
		if (doHour) {
			try {
				configServer.checkPermGenSpace(true);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		for (int i = 0; i < factories.length; i++) {
			control(factories[i], do10Seconds, doMinute, doHour, firstRun);
		}
	}

	private void control(CFMLFactoryImpl cfmlFactory, boolean do10Seconds, boolean doMinute, boolean doHour, boolean firstRun) {
		try {
			boolean isRunning = cfmlFactory.getUsedPageContextLength() > 0;
			if (isRunning) {
				cfmlFactory.checkTimeout();
			}
			ConfigWeb config = null;

			if (firstRun) {
				config = cfmlFactory.getConfig();
				ThreadLocalConfig.register(config);

				config.reloadTimeServerOffset();
				checkOldClientFile(config);

				try {
					RHExtension.correctExtensions(config);
				}
				catch (Exception e) {
				}

				// try{checkStorageScopeFile(config,Session.SCOPE_CLIENT);}catch(Throwable t)
				// {ExceptionUtil.rethrowIfNecessary(t);}
				// try{checkStorageScopeFile(config,Session.SCOPE_SESSION);}catch(Throwable t)
				// {ExceptionUtil.rethrowIfNecessary(t);}
				try {
					config.reloadTimeServerOffset();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
				try {
					checkTempDirectorySize(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
				try {
					checkCacheFileSize(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
				try {
					cfmlFactory.getScopeContext().clearUnused();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}

			if (config == null) {
				config = cfmlFactory.getConfig();
			}
			ThreadLocalConfig.register(config);
			if (do10Seconds) {
				// try{DeployHandler.deploy(config);}catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}
			}

			// every Minute
			if (doMinute) {
				if (config == null) {
					config = cfmlFactory.getConfig();
				}
				ThreadLocalConfig.register(config);

				try {
					((SchedulerImpl) config.getScheduler()).startIfNecessary();
				}
				catch (Exception e) {
					LogUtil.log(ThreadLocalPageContext.getConfig(configServer), Controler.class.getName(), e);
				}

				// double check templates
				try {
					((ConfigWebPro) config).getCompiler().checkWatched();
				}
				catch (Exception e) {
					LogUtil.log(ThreadLocalPageContext.getConfig(configServer), Controler.class.getName(), e);
				}

				// deploy extensions, archives ...
				try {
					DeployHandler.deploy(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}

				// clear unused DB Connections
				try {
					for (DatasourceConnPool pool: ((ConfigPro) config).getDatasourceConnectionPools()) {
						try {
							pool.evict();
						}
						catch (Exception ex) {
						}
					}
				}
				catch (Exception e) {
				}

				// clear all unused scopes
				try {
					cfmlFactory.getScopeContext().clearUnused();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
				// Memory usage
				// clear Query Cache
				/*
				 * try{ ConfigWebUtil.getCacheHandlerFactories(config).query.clean(null);
				 * ConfigWebUtil.getCacheHandlerFactories(config).include.clean(null);
				 * ConfigWebUtil.getCacheHandlerFactories(config).function.clean(null);
				 * //cfmlFactory.getDefaultQueryCache().clearUnused(null); }catch(Throwable
				 * t){ExceptionUtil.rethrowIfNecessary(t);}
				 */
				// contract Page Pool
				try {
					doClearPagePools(config);
				}
				catch (Exception e) {
				}
				// try{checkPermGenSpace((ConfigWebPro) config);}catch(Throwable t)
				// {ExceptionUtil.rethrowIfNecessary(t);}
				try {
					doCheckMappings(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
				try {
					doClearMailConnections();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
				// clean LockManager
				if (cfmlFactory.getUsedPageContextLength() == 0) try {
					((LockManagerImpl) config.getLockManager()).clean();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}

				try {
					ConfigAdmin.checkForChangesInConfigFile(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}

			}
			// every hour
			if (doHour) {
				if (config == null) {
					config = cfmlFactory.getConfig();
				}
				ThreadLocalConfig.register(config);

				// time server offset
				try {
					config.reloadTimeServerOffset();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
				// check file based client/session scope
				// try{checkStorageScopeFile(config,Session.SCOPE_CLIENT);}catch(Throwable t)
				// {ExceptionUtil.rethrowIfNecessary(t);}
				// try{checkStorageScopeFile(config,Session.SCOPE_SESSION);}catch(Throwable t)
				// {ExceptionUtil.rethrowIfNecessary(t);}
				// check temp directory
				try {
					checkTempDirectorySize(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
				// check cache directory
				try {
					checkCacheFileSize(config);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}

			try {
				configServer.checkPermGenSpace(true);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		finally {
			ThreadLocalConfig.release();
		}
	}

	private void doClearPagePools(ConfigWeb config) {
		PagePoolClear.clear(null, config, true);
	}

	private CFMLFactoryImpl[] toFactories(CFMLFactoryImpl[] factories, Map contextes) {
		if (factories == null || factories.length != contextes.size()) factories = (CFMLFactoryImpl[]) contextes.values().toArray(new CFMLFactoryImpl[contextes.size()]);

		return factories;
	}

	private void doClearMailConnections() {
		SMTPConnectionPool.closeSessions();
	}

	private void checkOldClientFile(ConfigWeb config) {
		ExtensionResourceFilter filter = new ExtensionResourceFilter(".script", false);

		// move old structured file in new structure
		try {
			Resource dir = config.getClientScopeDir(), trgres;
			Resource[] children = dir.listResources(filter);
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
		}
	}

	private void checkCacheFileSize(ConfigWeb config) {
		checkSize(config, config.getCacheDir(), config.getCacheDirSize(), new ExtensionResourceFilter(".cache"));
	}

	private void checkTempDirectorySize(ConfigWeb config) {
		checkSize(config, config.getTempDirectory(), 1024 * 1024 * 1024, null);
	}

	private void checkSize(ConfigWeb config, Resource dir, long maxSize, ResourceFilter filter) {
		if (!dir.exists()) return;
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
				catch (IOException e) {
					LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, Controler.class.getName(), "cannot remove resource [" + res.getAbsolutePath() + "]");
					break;
				}
			}
			res = null;
		}

	}

	private void doCheckMappings(ConfigWeb config) {
		Mapping[] mappings = config.getMappings();
		for (int i = 0; i < mappings.length; i++) {
			Mapping mapping = mappings[i];
			mapping.check();
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
