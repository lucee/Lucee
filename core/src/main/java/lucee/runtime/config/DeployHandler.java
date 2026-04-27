/**
 * Copyright (c) 2014, the Railo Company Ltd.
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
 */
package lucee.runtime.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.runtime.config.ConfigAdmin.AlreadyInstalledExtension;
import lucee.runtime.config.maven.ExtensionProvider;
import lucee.runtime.config.maven.Version;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.mvn.MavenUtil.GAVSO;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public final class DeployHandler {

	private static final ResourceFilter ALL_EXT = new ExtensionResourceFilter(new String[] { ".lex", ".lar", ".lco", ".json" });

	/**
	 * deploys all files found
	 * 
	 * @param config
	 */
	public static void deploy(Config config, Log log, boolean force) {
		if (!contextIsValid(config)) return;

		synchronized (config) {
			Resource dir = config.getDeployDirectory();
			if (!dir.exists()) dir.mkdirs();

			// check deploy directory
			Resource[] children = dir.listResources(ALL_EXT);
			String ext;
			if (children.length > 0) {
				ThreadQueue queue = config.getThreadQueue();
				short prevMode = ThreadQueue.MODE_UNDEFINED;
				if (queue != null) prevMode = queue.setMode(ThreadQueue.MODE_BLOCKING);
				try {
					for (Resource child: children) {
						if (LogUtil.doesInfo(log)) {
							log.log(Log.LEVEL_INFO, "deploy handler", "found [" + child.getAbsolutePath() + "] in deploy folder");
						}

						try {
							// Lucee archives
							ext = ResourceUtil.getExtension(child, null);
							if ("lar".equalsIgnoreCase(ext)) {
								// deployArchive(config,child,true);
								ConfigAdmin.updateArchive((ConfigPro) config, child, true);
							}

							// Lucee Extensions
							else if ("lex".equalsIgnoreCase(ext)) {
								ResetFilter filter = new ResetFilter();
								ConfigAdmin._updateRHExtension((ConfigPro) config, RHExtension.getInstance(config, child, log), filter, true, force, RHExtension.ACTION_MOVE, log);
								filter.reset(config);
							}

							// Lucee core
							else if (config instanceof ConfigServer && "lco".equalsIgnoreCase(ext)) {
								ConfigAdmin.updateCore((ConfigServerImpl) config, child, true);
							}
							// CFConfig
							else if ("json".equalsIgnoreCase(ext)) {
								try {
									if (ConfigFactoryImpl.isConfigFileName(child.getName())) {
										log.log(Log.LEVEL_INFO, "deploy handler", "Importing config file [" + child.getName() + "]");
										CFConfigImport ci = new CFConfigImport(config, child, config.getResourceCharset(), null, null, false, false, false);
										ci.execute(true);
										child.delete();
									}
								}
								catch (Exception e) {
									DeployHandler.moveToFailedFolder(config.getDeployDirectory(), child);
									throw Caster.toPageException(e);
								}
							}
							else if (!child.isDirectory()) {
								DeployHandler.moveToFailedFolder(config.getDeployDirectory(), child);
								throw new IOException("Deploy, unsupported file [" + child.getName() + "]");
							}

						}
						catch (Exception e) {
							if (e instanceof AlreadyInstalledExtension) {
								log.log(Log.LEVEL_INFO, "deploy handler", e);
							}
							else {
								log.log(Log.LEVEL_ERROR, "deploy handler", e);
							}
						}
					}
				}
				finally {
					queue.setMode(prevMode);
				}
			}

			// check env var for change
			if (config instanceof ConfigServer) {
				String extensionIds = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("lucee-extensions", null)); // old no longer used
				if (StringUtil.isEmpty(extensionIds, true)) extensionIds = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("lucee.extensions", null));
				CFMLEngineImpl engine = (CFMLEngineImpl) ConfigUtil.getEngine(config);
				if (engine != null && !StringUtil.isEmpty(extensionIds, true) && !extensionIds.equals(engine.getEnvExt())) {
					try {
						engine.setEnvExt(extensionIds);
						List<ExtensionDefintion> extensions = RHExtension.toExtensionDefinitions(extensionIds);
						Resource configDir = CFMLEngineImpl.getSeverContextConfigDirectory(engine.getCFMLEngineFactory());
						Map<ExtensionDefintion, Boolean> results = DeployHandler.deployExtensions(config, extensions.toArray(new ExtensionDefintion[extensions.size()]), log, force,
								false);
						boolean sucess = true;
						for (Boolean b: results.values()) {
							if (!Boolean.TRUE.equals(b)) sucess = false;
						}

						if (sucess && configDir != null) ConfigFactory.updateRequiredExtension(engine, configDir, log);
						log.log(Log.LEVEL_INFO, "deploy handler",
								(sucess ? "Successfully installed" : "Failed to install") + " extensions: [" + ListUtil.listToList(extensions, ", ") + "]");
					}
					catch (Exception e) {
						log.log(Log.LEVEL_ERROR, "deploy handler", e);
					}
				}
			}
		}
	}

	private static boolean contextIsValid(Config config) {
		// this test is not very good but it works
		ConfigWeb[] webs;
		if (config instanceof ConfigWeb) webs = new ConfigWeb[] { ((ConfigWeb) config) };
		else webs = ((ConfigServer) config).getConfigWebs();

		for (int i = 0; i < webs.length; i++) {
			try {
				ReqRspUtil.getRootPath(webs[i].getServletContext());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				return false;
			}
		}
		return true;
	}

	public static Resource moveToFailedFolder(Resource deployDirectory, Resource res) {
		Resource dir = deployDirectory.getRealResource("failed-to-deploy");
		Resource dst = dir.getRealResource(res.getName());
		dir.mkdirs();

		try {
			if (dst.exists()) dst.remove(true);
			ResourceUtil.moveTo(res, dst, true);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return dst;
	}

	public static Map<ExtensionDefintion, Boolean> deployExtensions(Config config, ExtensionDefintion[] eds, final Log log, boolean force, boolean throwOnError)
			throws PageException {
		Map<ExtensionDefintion, Boolean> results = throwOnError ? null : new HashMap<>();
		if (!ArrayUtil.isEmpty(eds)) {

			ExtensionDefintion ed;
			RefBoolean sucess = new RefBooleanImpl();
			ResetFilter filter = new ResetFilter();
			try {
				for (int i = 0; i < eds.length; i++) {
					ed = eds[i];
					if (StringUtil.isEmpty(ed.getId(), true)) {
						if (!throwOnError) results.put(ed, Boolean.FALSE);
						continue;
					}
					try {
						RHExtension ext = deployExtension(config, ed, filter, log, i + 1 == eds.length, force, throwOnError, sucess);
						if (!throwOnError) results.put(ed, ext != null ? Boolean.TRUE : Boolean.FALSE);
					}
					catch (PageException e) {
						if (throwOnError) throw e;
						results.put(ed, Boolean.FALSE);

						if (log != null) log.error("deploy-extension", e);
						else LogUtil.log("deploy-extension", e);
					}
				}
			}
			finally {
				try {
					ConfigUtil.getConfigServerImpl(config).resetExtensionDefinitions().resetRHExtensions();
					filter.add("resetExtensionDefinitions", "resetRHExtensions");
					filter.reset(config);
				}
				catch (Exception e) {
					if (throwOnError) throw Caster.toPageException(e);

					if (log != null) log.error("deploy-extension", e);
					else LogUtil.log("deploy-extension", e);
				}
			}

		}
		return results;
	}

	public static boolean deployExtensions(Config config, List<ExtensionDefintion> eds, Log log, boolean force, boolean throwOnError) throws PageException {
		boolean allSucessfull = true;
		if (eds != null && eds.size() > 0) {
			ExtensionDefintion ed;
			Iterator<ExtensionDefintion> it = eds.iterator();
			RefBoolean sucess = new RefBooleanImpl();
			ResetFilter filter = new ResetFilter();
			try {
				int count = 0;
				while (it.hasNext()) {
					count++;
					ed = it.next();
					if (StringUtil.isEmpty(ed.getId(), true)) continue;
					try {
						deployExtension(config, ed, filter, log, count == eds.size(), force, throwOnError, sucess);
					}
					catch (PageException e) {
						if (throwOnError) throw e;
						if (log != null) log.error("deploy-extension", e);
						else LogUtil.log("deploy-extension", e);
						sucess.setValue(false);
					}
					if (!sucess.toBooleanValue()) allSucessfull = false;

				}
			}
			finally {
				try {
					filter.reset(config);
				}
				catch (Exception e) {
					if (throwOnError) throw Caster.toPageException(e);

					if (log != null) log.error("deploy-extension", e);
					else LogUtil.log("deploy-extension", e);
				}
			}
		}
		return allSucessfull;
	}

	/**
	 * install an extension based on the given id and version
	 * 
	 * @param config
	 * @param ed the id of the extension
	 * @param log
	 * @param reload
	 * @param force
	 * @param throwOnError
	 * @param installDone
	 * @return
	 * @throws PageException
	 */
	public static RHExtension deployExtension(Config config, ExtensionDefintion ed, ResetFilter filter, Log log, boolean reload, boolean force, boolean throwOnError,
			RefBoolean installDone) throws PageException {

		RHExtension rhe = ed.toRHExtension(config);
		if (rhe.installed()) {
			if (force) {
				// MUST uninstall
			}
			else {
				if (installDone != null) installDone.setValue(false);
				return rhe;
			}
		}
		return ConfigAdmin._updateRHExtension((ConfigPro) config, rhe, filter, reload, force, RHExtension.ACTION_COPY, log);
	}

	private static Version getLatestVersionFor(ConfigPro config, ExtensionDefintion ed, boolean investigate, boolean throwOnError, Log log) throws PageException {
		try {
			// get extension from Maven
			GAVSO gav = getGavById(config, ed, investigate, log);
			ExtensionProvider ep = ExtensionProvider.getInstance(config, gav.g);

			Version version = StringUtil.isEmpty(ed.getVersion(), true) ? null : Version.parseVersion(ed.getVersion(), null);
			// get latest version when no version is defined
			if (version == null) {
				// find out the last version still needs some investigated effort, maybe we will improve on that in
				// the future.
				version = ep.last(gav.a);
				if (LogUtil.doesDebug(log) && version != null) {
					log.debug("main", "got latest version[" + version + "] for artifact [" + gav.a + "]");
				}
				return version;
			}

			if (LogUtil.doesDebug(log)) {
				log.debug("main", "use defined [" + version + "] for artifact [" + gav.a + "]");
			}
			return version;
		}
		catch (Exception e) {
			if (throwOnError) throw Caster.toPageException(e);
			else if (LogUtil.doesWarn(log)) {
				log.log(Log.LEVEL_WARN, "main", e);
			}
			return null;
		}
	}

	public static Version getLatestVersionFor(ConfigPro config, ExtensionDefintion ed, Log log, boolean throwOnError) throws PageException {
		return getLatestVersionFor(config, ed, false, false, log);

	}

	private static GAVSO getGavById(ConfigPro config, ExtensionDefintion ed, boolean investigate, Log log) throws ApplicationException {
		String groupId = ed.getGroupId();
		String artifactId = ed.getArtifactId();

		if (!StringUtil.isEmpty(groupId, true) && !StringUtil.isEmpty(artifactId, true)) {
			return new GAVSO(groupId, artifactId, null);
		}

		// translate UUID to artifact
		GAVSO gav = ExtensionProvider.toGAVSO(config, ed.getId(), investigate, null);
		if (gav != null) {
			if (LogUtil.doesDebug(log) && artifactId != null) {
				log.debug("main", "resolved extension id [" + ed.getId() + "] to maven endpoint [" + gav.g + ":" + gav.a + "]");
			}
			return gav;
		}

		throw new ApplicationException("could not link the exension with the id [" + ed.getId() + "] to any maven endpoint.");
	}

	public static Resource downloadExtensionFromMaven(ConfigPro config, ExtensionDefintion ed, boolean investigate, boolean throwOnError, Log log) throws PageException {
		try {
			GAVSO gav = getGavById(config, ed, investigate, log);
			ExtensionProvider ep = ExtensionProvider.getInstance(config, gav.g);
			Version version;
			// get latest version when no version is defined
			if (StringUtil.isEmpty(ed.getVersion(), true)) {
				// find out the last version still needs some investigated effort, maybe we will improve on that in
				// the future.
				version = ep.last(gav.a);
				if (LogUtil.doesDebug(log) && version != null) {
					log.debug("main", "get latest version[" + version + "] for artifact [" + gav.a + "]");
				}
			}
			else {
				version = Version.parseVersion(ed.getVersion());
				if (LogUtil.doesDebug(log) && version != null) {
					log.debug("main", "use defined [" + version + "] for artifact [" + gav.a + "]");
				}
			}
			if (version == null) return null;

			return ep.getLEXResource(config, gav.a, version);
		}
		catch (Exception e) {
			if (throwOnError) throw Caster.toPageException(e);
			else if (LogUtil.doesWarn(log)) {
				log.log(Log.LEVEL_WARN, "main", e);
			}
			return null;
		}
	}

	public static Resource downloadExtension(ConfigPro config, ExtensionDefintion ed, Log log, boolean throwOnError) throws PageException {
		Resource res = downloadExtensionFromMaven(config, ed, false, false, log);
		if (res != null) return res;

		if (throwOnError) throw new ApplicationException("Failed  to load extension: [" + ed + "]");
		if (log != null) log.warn("main", "Failed to load extension: [" + ed + "] ");
		return null;
	}

	private static void addQueryParam(StringBuilder qs, String name, String value) {
		if (StringUtil.isEmpty(value)) return;
		qs.append(qs.length() == 0 ? "?" : "&").append(name).append("=").append(value);
	}

	public static List<ExtensionDefintion> getLocalExtensions(Config config, boolean validate) {
		return ((ConfigPro) config).loadLocalExtensions(validate);
	}

	public static RHExtension deployExtension(ConfigPro config, Resource ext, boolean reload, boolean force, short action, Log log) throws PageException {
		ResetFilter filter = new ResetFilter();
		try {
			return ConfigAdmin._updateRHExtension(config, RHExtension.getInstance(config, ext, config.getLog("deploy")), filter, reload, force, action, log);
		}
		finally {
			filter.resetThrowPageException(config);
		}
	}

	public static void deployExtension(ConfigPro config, RHExtension rhext, boolean reload, boolean force, Log log) throws PageException {
		ResetFilter filter = new ResetFilter();
		try {
			ConfigAdmin._updateRHExtension(config, rhext, filter, reload, force, RHExtension.ACTION_COPY, log);
		}
		finally {
			filter.resetThrowPageException(config);
		}
	}
}