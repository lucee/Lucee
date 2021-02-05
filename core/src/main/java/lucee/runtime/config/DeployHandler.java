/**
 * Copyright (c) 2014, the Railo Company Ltd.
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
 */
package lucee.runtime.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

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
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
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

public class DeployHandler {

	private static final ResourceFilter ALL_EXT = new ExtensionResourceFilter(new String[] { ".lex", ".lar", ".lco" });

	/**
	 * deploys all files found
	 * 
	 * @param config
	 */
	public static void deploy(Config config) {
		if (!contextIsValid(config)) return;

		synchronized (config) {
			Resource dir = config.getDeployDirectory();
			if (!dir.exists()) dir.mkdirs();

			// check deploy directory
			Resource[] children = dir.listResources(ALL_EXT);
			Resource child;
			String ext;
			for (int i = 0; i < children.length; i++) {
				child = children[i];
				try {
					// Lucee archives
					ext = ResourceUtil.getExtension(child, null);
					if ("lar".equalsIgnoreCase(ext)) {
						// deployArchive(config,child,true);
						ConfigAdmin.updateArchive((ConfigPro) config, child, true);
					}

					// Lucee Extensions
					else if ("lex".equalsIgnoreCase(ext)) ConfigAdmin._updateRHExtension((ConfigPro) config, child, true);

					// Lucee core
					else if (config instanceof ConfigServer && "lco".equalsIgnoreCase(ext)) ConfigAdmin.updateCore((ConfigServerImpl) config, child, true);
				}
				catch (Exception e) {
					Log log = config.getLog("deploy");
					log.error("Extension", e);
				}
			}

			// check env var for change
			if (config instanceof ConfigServer) {
				String extensionIds = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("lucee-extensions", null)); // old no longer used
				if (StringUtil.isEmpty(extensionIds, true)) extensionIds = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("lucee.extensions", null));
				CFMLEngineImpl engine = (CFMLEngineImpl) ConfigWebUtil.getEngine(config);
				if (engine != null && !StringUtil.isEmpty(extensionIds, true) && !extensionIds.equals(engine.getEnvExt())) {
					try {
						engine.setEnvExt(extensionIds);
						List<ExtensionDefintion> extensions = RHExtension.toExtensionDefinitions(extensionIds);
						Resource configDir = CFMLEngineImpl.getSeverContextConfigDirectory(engine.getCFMLEngineFactory());
						Log log = config != null ? config.getLog("deploy") : null;
						boolean sucess = DeployHandler.deployExtensions(config, extensions.toArray(new ExtensionDefintion[extensions.size()]), log);
						if (sucess && configDir != null) ConfigFactory.updateRequiredExtension(engine, configDir, log);
						LogUtil.log(config, Log.LEVEL_INFO, "deploy", "controller",
								(sucess ? "sucessfully" : "unsucessfully") + " installed extensions:" + ListUtil.listToList(extensions, ", "));
					}
					catch (Exception e) {
						Log log = config.getLog("deploy");
						log.error("Extension", e);
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

	public static boolean deployExtensions(Config config, ExtensionDefintion[] eds, final Log log) throws PageException {
		boolean allSucessfull = true;
		if (!ArrayUtil.isEmpty(eds)) {
			ExtensionDefintion ed;
			boolean sucess;
			for (int i = 0; i < eds.length; i++) {
				ed = eds[i];
				if (StringUtil.isEmpty(ed.getId(), true)) continue;
				try {
					sucess = deployExtension(config, ed, log, i + 1 == eds.length);
				}
				catch (PageException e) {
					if (log != null) log.error("deploy-extension", e);
					else throw e;
					sucess = false;
				}
				if (!sucess) allSucessfull = false;
			}
		}
		return allSucessfull;
	}

	public static boolean deployExtensions(Config config, List<ExtensionDefintion> eds, Log log) throws PageException {
		boolean allSucessfull = true;
		if (eds != null && eds.size() > 0) {
			ExtensionDefintion ed;
			Iterator<ExtensionDefintion> it = eds.iterator();
			boolean sucess;
			int count = 0;
			while (it.hasNext()) {
				count++;
				ed = it.next();
				if (StringUtil.isEmpty(ed.getId(), true)) continue;
				try {
					sucess = deployExtension(config, ed, log, count == eds.size());
				}
				catch (PageException e) {
					if (log != null) log.error("deploy-extension", e);
					else throw e;
					sucess = false;
				}
				if (!sucess) allSucessfull = false;

			}
		}
		return allSucessfull;
	}

	/**
	 * install an extension based on the given id and version
	 * 
	 * @param config
	 * @param id the id of the extension
	 * @param version pass null if you don't need a specific version
	 * @return
	 * @throws IOException
	 * @throws PageException
	 */
	public static boolean deployExtension(Config config, ExtensionDefintion ed, Log log, boolean reload) throws PageException {
		ConfigPro ci = (ConfigPro) config;

		// is the extension already installed
		try {
			if (ConfigAdmin.hasRHExtensions(ci, ed) != null) return false;
		}
		catch (Exception e) {
			if (log != null) log.error("extension", e);
			else throw Caster.toPageException(e);
		}

		// check if a local extension is matching our id
		Iterator<ExtensionDefintion> it = getLocalExtensions(config, false).iterator();
		ExtensionDefintion ext = null, tmp;

		if (log != null) log.info("extension", "installing the extension " + ed);
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
					if (log != null) log.info("extension", "installing the extension " + ed + " from local provider");
					res = SystemUtil.getTempDirectory().getRealResource(ed.getId() + "-" + ed.getVersion() + ".lex");
					ResourceUtil.touch(res);
					IOUtil.copy(ext.getSource(), res);
					ConfigAdmin._updateRHExtension((ConfigPro) config, res, reload);
					return true;
				}
				catch (Exception e) {
					// check if the zip is valid
					if (res instanceof File) {
						if (!IsZipFile.invoke((File) res)) {
							CFMLEngineImpl engine = CFMLEngineImpl.toCFMLEngineImpl(ConfigWebUtil.getEngine(config));
							engine.deployBundledExtension(true);
							if (IsZipFile.invoke((File) res)) {
								continue; // we start over that part
							}
						}
					}

					ext = null;
					LogUtil.log(ThreadLocalPageContext.getConfig(config), DeployHandler.class.getName(), e);
				}
			}
			break;
		}
		while (true);

		String apiKey = config.getIdentification().getApiKey();
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
					if (apiKey != null) qs.append("&ioid=").append(apiKey);

					url = new URL(url, "/rest/extension/provider/info/" + ed.getId() + qs);
					if (log != null) log.info("extension", "check for a newer version at " + url);
					rsp = HTTPEngine.get(url, null, null, -1, false, "UTF-8", "", null, new Header[] { new HeaderImpl("accept", "application/json") });

					if (rsp.getStatusCode() != 200) continue;

					content = rsp.getContentAsString();
					Struct sct = Caster.toStruct(DeserializeJSON.call(null, content));
					String remoteVersion = Caster.toString(sct.get(KeyConstants._version));

					// the local version is as good as the remote
					if (remoteVersion != null && remoteVersion.compareTo(ext.getVersion()) <= 0) {
						if (log != null) log.info("extension", "installing the extension " + ed + " from local provider");

						// avoid that the exzension from provider get removed
						Resource res = SystemUtil.getTempDirectory().getRealResource(ed.getId() + "-" + ed.getVersion() + ".lex");
						ResourceUtil.touch(res);

						IOUtil.copy(ext.getSource(), res);
						ConfigAdmin._updateRHExtension((ConfigPro) config, res, reload);
						return true;
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
				if (log != null) log.info("extension", "installing the extension " + ed + " from local provider");
				Resource res = SystemUtil.getTempDirectory().getRealResource(ext.getSource().getName());
				ResourceUtil.touch(res);

				IOUtil.copy(ext.getSource(), res);
				ConfigAdmin._updateRHExtension((ConfigPro) config, res, reload);
				return true;
			}
			catch (Exception e) {
				if (log != null) log.error("extension", e);
			}
		}

		// if not we try to download it
		if (log != null) log.info("extension", "installing the extension " + ed + " from remote extension provider");
		Resource res = downloadExtension(ci, ed, log);
		if (res != null) {
			try {
				ConfigAdmin._updateRHExtension((ConfigPro) config, res, reload);
				return true;
			}
			catch (Exception e) {
				if (log != null) log.error("extension", e);
				else throw Caster.toPageException(e);
			}
		}
		throw new ApplicationException("Failed to install extension [" + ed.getId() + "]");
	}

	public static Resource downloadExtension(Config config, ExtensionDefintion ed, Log log) {

		String apiKey = config.getIdentification().getApiKey();
		URL url;
		RHExtensionProvider[] providers = ((ConfigPro) config).getRHExtensionProviders();

		for (int i = 0; i < providers.length; i++) {
			HTTPResponse rsp = null;
			try {
				url = providers[i].getURL();
				StringBuilder qs = new StringBuilder();
				addQueryParam(qs, "ioid", apiKey);
				addQueryParam(qs, "version", ed.getVersion());

				url = new URL(url, "/rest/extension/provider/full/" + ed.getId() + qs);
				if (log != null) log.info("main", "check for extension at : " + url);

				rsp = HTTPEngine.get(url, null, null, -1, true, "UTF-8", "", null, new Header[] { new HeaderImpl("accept", "application/cfml") });

				// If status code indicates success
				if (rsp.getStatusCode() >= 200 && rsp.getStatusCode() < 300) {

					// copy it locally
					Resource res = SystemUtil.getTempDirectory().getRealResource(ed.getId() + "-" + ed.getVersion() + ".lex");
					ResourceUtil.touch(res);
					IOUtil.copy(rsp.getContentAsStream(), res, true);
					if (log != null) log.info("main", "downloaded extension [" + ed + "] to [" + res + "]");
					return res;

				}
				else {
					if (log != null) log.warn("main", "failed (" + rsp.getStatusCode() + ") to load extension: " + ed + " from " + url);
				}
			}
			catch (Exception e) {
				if (log != null) log.error("extension", e);
			}
			finally {
				HTTPEngine.closeEL(rsp);
			}
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
		return downloadExtension(config, ed, log);
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

	public static void deployExtension(ConfigPro config, Resource ext) throws PageException {
		ConfigAdmin._updateRHExtension(config, ext, true);
	}
}
