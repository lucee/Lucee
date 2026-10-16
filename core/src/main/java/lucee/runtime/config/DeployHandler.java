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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
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
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.runtime.config.ConfigAdmin.AlreadyInstalledExtension;
import lucee.runtime.config.maven.ExtensionProvider;
import lucee.runtime.config.maven.Version;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.functions.conversion.DeserializeJSON;
import lucee.runtime.functions.system.IsZipFile;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public final class DeployHandler {

	private static final ResourceFilter ALL_EXT = new ExtensionResourceFilter(new String[] { ".lex", ".lar", ".lco", ".json" });
	private static final boolean USE_MAVEN_EXTENSION_PROVIDER = true;

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
								ConfigAdmin._updateRHExtension((ConfigPro) config, child, filter, true, force, RHExtension.ACTION_MOVE);
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
										CFConfigImport ci = new CFConfigImport(config, child, config.getResourceCharset(), null, "server", null, false, false, false);
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

	public static void moveToFailedFolder(Resource deployDirectory, Resource res) {
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

		// TODO Auto-generated method stub

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

		if (log != null) log.info("extension", "Installing extension [" + ed + "]");

		ConfigPro ci = (ConfigPro) config;
		String coreVersion = ConfigUtil.getCFMLEngine(config).getInfo().getVersion().toString();
		// is the extension already installed
		try {
			RHExtension installed = ConfigAdmin.hasRHExtensionInstalled(ci, ed);
			if (installed != null) {
				installDone.setValue(false);
				return installed;
			}
		}
		catch (Exception e) {
			if (throwOnError) throw Caster.toPageException(e);
			if (log != null) log.error("extension", e);
			else LogUtil.log("extension", e);
		}

		// we do have a local copy
		Resource local = ed.getSource(null);
		if (local != null) {
			if (log != null) log.info("extension", "Installing extension [" + ed + "] from provided file");

			RHExtension _ext = ConfigAdmin._updateRHExtension((ConfigPro) config, local, filter, reload, force, RHExtension.ACTION_COPY);
			installDone.setValue(true);
			if (log != null) log.info("extension", "Installed extension [" + ed + "] from provided file");
			return _ext;
		}

		// check if a local extension is matching our id
		Iterator<ExtensionDefintion> it = getLocalExtensions(config, false).iterator();
		ExtensionDefintion ext = null, tmp;

		if (log != null) {
			log.info("extension", "installing the extension " + ed);
		}
		while (it.hasNext()) {
			tmp = it.next();
			if (ed.equals(tmp)) {
				ext = tmp;
				break;
			}
		}

		// if we have one and also the defined version matches, there is no need to check online
		do {
			if (ext != null && ed.getVersion() != null) {
				Resource res = null;
				try {
					if (log != null) log.info("extension", "Installing extension [" + ed + "] from local provider");
					res = SystemUtil.getTempDirectory().getRealResource(ed.getId() + "-" + ed.getVersion() + ".lex");
					ResourceUtil.touch(res);
					IOUtil.copy(ext.getSource(), res);
					RHExtension _ext = ConfigAdmin._updateRHExtension((ConfigPro) config, res, filter, reload, force, RHExtension.ACTION_MOVE);
					installDone.setValue(true);
					if (log != null) log.info("extension", "Installed extension [" + ed + "] from local provider");
					return _ext;
				}
				catch (Exception e) {
					if (log != null) log.error("extension", e);
					// check if the zip is valid
					if (res instanceof File) {
						if (!IsZipFile.invoke((File) res)) {
							CFMLEngineImpl engine = CFMLEngineImpl.toCFMLEngineImpl(ConfigUtil.getEngine(config));
							engine.deployBundledExtension(true);
							if (IsZipFile.invoke((File) res)) {
								continue; // we start over that part
							}
						}
					}

					ext = null;
					LogUtil.log((config), DeployHandler.class.getName(), e);
				}
			}
			break;
		}
		while (true);
		Identification id = config.getIdentification();
		String apiKey = id == null ? null : id.getApiKey();
		RHExtensionProvider[] providers = ci.getRHExtensionProviders();
		URL url;
		// if we have a local version, we look if there is a newer remote version
		if (ext != null) {
			String content;
			for (int i = 0; i < providers.length; i++) {
				HTTPResponse rsp = null;
				try {
					url = providers[i].getURL();
					StringBuilder qs = new StringBuilder();
					qs.append("?withLogo=false");
					if (ed.getVersion() != null) qs.append("&version=").append(ed.getVersion());
					else qs.append("&coreVersion=").append(coreVersion);
					if (apiKey != null) qs.append("&ioid=").append(apiKey);

					url = new URL(url, "/rest/extension/provider/info/" + ed.getId() + qs);
					if (log != null) log.info("extension", "Check for a newer version at [" + url + "]");
					rsp = HTTPEngine4Impl.get(url, null, null, 5000, false, "UTF-8", "", null, new Header[] { new HeaderImpl("accept", "application/json") });

					if (rsp.getStatusCode() != 200) continue;

					content = rsp.getContentAsString();
					Struct sct = Caster.toStruct(DeserializeJSON.call(null, content));
					String remoteVersion = Caster.toString(sct.get(KeyConstants._version));

					// the local version is as good as the remote
					if (remoteVersion != null && remoteVersion.compareTo(ext.getVersion()) <= 0) {
						if (log != null) log.info("extension", "Installing extension [" + ed + "] from local provider");

						// avoid that the exzension from provider get removed
						Resource res = SystemUtil.getTempDirectory().getRealResource(ed.getId() + "-" + ed.getVersion() + ".lex");
						ResourceUtil.touch(res);

						IOUtil.copy(ext.getSource(), res);
						RHExtension _ext = ConfigAdmin._updateRHExtension((ConfigPro) config, res, filter, reload, force, RHExtension.ACTION_MOVE);
						installDone.setValue(true);
						return _ext;
					}
				}
				catch (Exception e) {
					if (log != null) log.error("extension", e);
				}
				finally {
					HTTPEngine.closeEL(rsp);
				}
			}
		}

		// if we have an ext at this stage this mean the remote providers was not acessible or have not this
		// extension
		if (ext != null) {
			try {
				if (log != null) log.info("extension", "Installing extension [" + ed + "] from local provider");
				Resource res = SystemUtil.getTempDirectory().getRealResource(ext.getSource().getName());
				ResourceUtil.touch(res);

				IOUtil.copy(ext.getSource(), res);
				RHExtension _ext = ConfigAdmin._updateRHExtension((ConfigPro) config, res, filter, reload, force, RHExtension.ACTION_MOVE);
				installDone.setValue(true);
				if (log != null) log.info("extension", "Installed extension [" + ed + "] from local provider");
				return _ext;
			}
			catch (Exception e) {
				if (log != null) log.error("extension", e);
			}
		}

		// if not we try to download it
		if (log != null) log.info("extension", "Installing extension [" + ed + "] from remote extension provider");
		Resource res = downloadExtension(ci, ed, log, throwOnError);
		if (res != null) {
			try {
				RHExtension _ext = ConfigAdmin._updateRHExtension((ConfigPro) config, res, filter, reload, force, RHExtension.ACTION_MOVE);
				installDone.setValue(true);
				if (log != null) log.info("extension", "Installed extension [" + ed + "] from remote extension provider");
				return _ext;
			}
			catch (Exception e) {
				if (log != null) log.error("extension", e);
				else throw Caster.toPageException(e);
			}
		}
		return null;
	}

	private static Resource downloadExtensionFromMaven(Config config, ExtensionDefintion ed, boolean investigate, boolean throwOnError, Log log) throws PageException {
		try {
			// get extension from Maven
			ExtensionProvider ep = new ExtensionProvider();

			String artifact = ed.getArtifactId();

			// translate UUID to artifact
			if (StringUtil.isEmpty(artifact, true)) {
				artifact = ep.toArtifact(ed.getId(), investigate);
				if (LogUtil.doesDebug(log) && artifact != null) {
					log.debug("main", "resolved extension id [" + ed.getId() + "] to artifact [" + artifact + "]");
				}
			}
			if (artifact == null) return null;

			Version version;
			// get latest version when no version is defined
			if (StringUtil.isEmpty(ed.getVersion(), true)) {
				// find out the last version still needs some investigated effort, maybe we will improve on that in
				// the future.
				version = ep.last(artifact);
				if (LogUtil.doesDebug(log) && version != null) {
					log.debug("main", "get latest version[" + version + "] for artifact [" + artifact + "]");
				}
			}
			else {
				version = Version.parseVersion(ed.getVersion());
				if (LogUtil.doesDebug(log) && version != null) {
					log.debug("main", "use defined [" + version + "] for artifact [" + artifact + "]");
				}
			}
			if (version == null) return null;

			return ep.getResource((ConfigPro) config, artifact, version);
			// Resource res = SystemUtil.getTempDirectory().getRealResource(ep.getGroup() + "-" + artifact + "-"
			// + version + ".lex");
			// ResourceUtil.touch(res);
			// IOUtil.copy(ep.get(artifact, version), res, true);
			// return res;
		}
		catch (Exception e) {
			if (throwOnError) throw Caster.toPageException(e);
			else if (LogUtil.doesWarn(log)) {
				log.log(Log.LEVEL_WARN, "main", e);
			}
			return null;
		}
	}

	public static Resource downloadExtension(Config config, ExtensionDefintion ed, Log log, boolean throwOnError) throws ApplicationException {
		// get extension from Maven
		// TODO set investigate to true and log as warning if it fallback to old behaviour
		if (USE_MAVEN_EXTENSION_PROVIDER) {
			try {
				Resource res = downloadExtensionFromMaven(config, ed, false, false, log);
				if (res != null) return res;
			}
			catch (PageException e) {
				// should not happen
				log.error("main", e);
			}
		}

		// classic extension provider via Rest service
		String coreVersion = ConfigUtil.getCFMLEngine(config).getInfo().getVersion().toString();
		Identification id = config.getIdentification();
		String apiKey = id == null ? null : id.getApiKey();
		URL url;
		RHExtensionProvider[] providers = ((ConfigPro) config).getRHExtensionProviders();
		ApplicationException exp = null;
		for (int i = 0; i < providers.length; i++) {
			HTTPResponse rsp = null;
			try {
				url = providers[i].getURL();
				StringBuilder qs = new StringBuilder();
				if (apiKey != null) addQueryParam(qs, "ioid", apiKey);
				if (ed.getVersion() != null) addQueryParam(qs, "version", ed.getVersion());
				else addQueryParam(qs, "coreVersion", coreVersion);

				url = new URL(url, "/rest/extension/provider/full/" + ed.getId() + qs);
				if (log != null) log.info("main", "Check for extension at [" + url + "]");
				rsp = HTTPEngine4Impl.get(url, null, null, 5000, true, "UTF-8", "", null, new Header[] { new HeaderImpl("accept", "application/cfml") });

				// If status code indicates success
				if (rsp.getStatusCode() >= 200 && rsp.getStatusCode() < 300) {

					// copy it locally
					Resource res = SystemUtil.getTempDirectory().getRealResource(ed.getId() + "-" + ed.getVersion() + ".lex");
					ResourceUtil.touch(res);
					IOUtil.copy(rsp.getContentAsStream(), res, true);

					HTTPUtil.validateDownload(url, rsp, res, null, true, null);

					if (log != null) log.info("main", "Downloaded extension [" + ed + "] to [" + res + "]");
					return res;

				}
				// we want the first
				if (throwOnError && exp == null) exp = new ApplicationException("Failed (" + rsp.getStatusCode() + ") to load extension: [" + ed + "] from [" + url + "]");
				if (log != null) log.warn("main", "Failed (" + rsp.getStatusCode() + ") to load extension: [" + ed + "] from [" + url + "]");
			}
			catch (Exception e) {
				if (log != null) log.error("extension", e);
			}
			finally {
				HTTPEngine.closeEL(rsp);
			}
		}
		if (exp != null) {
			throw exp;
		}

		return null;
	}

	private static void addQueryParam(StringBuilder qs, String name, String value) {
		if (StringUtil.isEmpty(value)) return;
		qs.append(qs.length() == 0 ? "?" : "&").append(name).append("=").append(value);
	}

	public static Resource getExtension(Config config, ExtensionDefintion ed, Log log) {
		// local
		ExtensionDefintion ext = getLocalExtension(config, ed, null);
		if (ext != null) {
			try {
				Resource src = ext.getSource();
				if (src.exists()) {
					Resource res = SystemUtil.getTempDirectory().getRealResource(ed.getId() + "-" + ed.getVersion() + ".lex");
					ResourceUtil.touch(res);
					IOUtil.copy(ext.getSource(), res);
					return res;
				}
			}
			catch (Exception e) {}
		}
		// remote
		try {
			return downloadExtension(config, ed, log, false);
		}
		catch (ApplicationException e) {
			return null;
		}
	}

	public static ExtensionDefintion getLocalExtension(Config config, ExtensionDefintion ed, ExtensionDefintion defaultValue) {
		Iterator<ExtensionDefintion> it = getLocalExtensions(config, false).iterator();
		ExtensionDefintion ext;
		while (it.hasNext()) {
			ext = it.next();
			if (ed.equals(ext)) {
				return ext;
			}
		}
		return defaultValue;
	}

	public static List<ExtensionDefintion> getLocalExtensions(Config config, boolean validate) {
		return ((ConfigPro) config).loadLocalExtensions(validate);
	}

	public static RHExtension deployExtension(ConfigPro config, Resource ext, boolean reload, boolean force, short action) throws PageException {
		ResetFilter filter = new ResetFilter();
		try {
			return ConfigAdmin._updateRHExtension(config, ext, filter, reload, force, action);
		}
		finally {
			filter.resetThrowPageException(config);
		}
	}

	public static void deployExtension(ConfigPro config, RHExtension rhext, ResetFilter filter, boolean reload, boolean force) throws PageException {
		ConfigAdmin._updateRHExtension(config, rhext, filter, reload, force);
	}
}