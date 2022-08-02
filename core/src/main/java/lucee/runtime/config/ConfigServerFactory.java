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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.BundleException;
import org.xml.sax.SAXException;

import com.jacob.com.LibraryLoader;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassException;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactory;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.tag.TagLibException;

/**
 * 
 */
public final class ConfigServerFactory extends ConfigFactory {

	/**
	 * creates a new ServletConfig Impl Object
	 * 
	 * @param engine
	 * @param initContextes
	 * @param contextes
	 * @param configDir
	 * @return new Instance
	 * @throws SAXException
	 * @throws ClassNotFoundException
	 * @throws PageException
	 * @throws IOException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 * @throws BundleException
	 * @throws ConverterException
	 */
	public static ConfigServerImpl newInstance(CFMLEngineImpl engine, Map<String, CFMLFactory> initContextes, Map<String, CFMLFactory> contextes, Resource configDir,
			ConfigServerImpl existing, boolean essentialOnly)
			throws SAXException, ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException, ConverterException {

		boolean isCLI = SystemUtil.isCLICall();
		if (isCLI) {
			Resource logs = configDir.getRealResource("logs");
			logs.mkdirs();
			Resource out = logs.getRealResource("out");
			Resource err = logs.getRealResource("err");
			ResourceUtil.touch(out);
			ResourceUtil.touch(err);
			if (logs instanceof FileResource) {
				SystemUtil.setPrintWriter(SystemUtil.OUT, new PrintWriter((FileResource) out));
				SystemUtil.setPrintWriter(SystemUtil.ERR, new PrintWriter((FileResource) err));
			}
			else {
				SystemUtil.setPrintWriter(SystemUtil.OUT, new PrintWriter(IOUtil.getWriter(out, "UTF-8")));
				SystemUtil.setPrintWriter(SystemUtil.ERR, new PrintWriter(IOUtil.getWriter(err, "UTF-8")));
			}
		}
		LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigServerFactory.class.getName(),
				"===================================================================\n" + "SERVER CONTEXT\n"
						+ "-------------------------------------------------------------------\n" + "- config:" + configDir + "\n" + "- loader-version:"
						+ SystemUtil.getLoaderVersion() + "\n" + "- core-version:" + engine.getInfo().getVersion() + "\n"
						+ "===================================================================\n"

		);
		UpdateInfo ui = getNew(engine, configDir, false, UpdateInfo.NEW_NONE);
		boolean doNew = ui.updateType != NEW_NONE;

		Resource configFileOld = configDir.getRealResource("lucee-server.xml");
		Resource configFileNew = configDir.getRealResource(".CFConfig.json");

		boolean hasConfigOld = false;
		boolean hasConfigNew = configFileNew.exists() && configFileNew.length() > 0;
		if (!hasConfigNew) {
			hasConfigOld = configFileOld.exists() && configFileOld.length() > 0;
		}
		ConfigServerImpl config = existing != null ? existing : new ConfigServerImpl(engine, initContextes, contextes, configDir, configFileNew, ui, essentialOnly);

		// translate to new
		if (!hasConfigNew) {
			if (hasConfigOld) {
				translateConfigFile(config, configFileOld, configFileNew, "multi", true);
			}
			// create config file
			else {
				createConfigFile("server", configFileNew);
				hasConfigNew = true;
			}
		}

		Struct root = loadDocumentCreateIfFails(configFileNew, "server");

		load(config, root, false, doNew, essentialOnly);

		if (!essentialOnly) {
			double version = ConfigWebUtil.getAsDouble("version", root, 1.0d);
			boolean cleanupDatasources = version < 5.0D;
			createContextFiles(configDir, config, doNew, cleanupDatasources);
			((CFMLEngineImpl) ConfigWebUtil.getEngine(config)).onStart(config, false);
		}

		return config;
	}

	/**
	 * reloads the Config Object
	 * 
	 * @param configServer
	 * @throws SAXException
	 * @throws ClassNotFoundException
	 * @throws PageException
	 * @throws IOException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 * @throws BundleException
	 */
	public static void reloadInstance(CFMLEngine engine, ConfigServerImpl configServer)
			throws ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException {
		Resource configFile = configServer.getConfigFile();
		if (configFile == null) return;
		if (second(configServer.getLoadTime()) > second(configFile.lastModified())) {
			if (!configServer.getConfigDir().getRealResource("password.txt").isFile()) return;
		}
		int iDoNew = getNew(engine, configServer.getConfigDir(), false, UpdateInfo.NEW_NONE).updateType;
		boolean doNew = iDoNew != NEW_NONE;
		load(configServer, loadDocument(configFile), true, doNew, false);
		((CFMLEngineImpl) ConfigWebUtil.getEngine(configServer)).onStart(configServer, true);
	}

	private static long second(long ms) {
		return ms / 1000;
	}

	/**
	 * @param configServer
	 * @param doc
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws FunctionLibException
	 * @throws TagLibException
	 * @throws PageException
	 * @throws BundleException
	 */
	static void load(ConfigServerImpl configServer, Struct root, boolean isReload, boolean doNew, boolean essentialOnly)
			throws ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException {
		ConfigBase.onlyFirstMatch = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.mapping.first", null), false);
		ConfigWebFactory.load(null, configServer, root, isReload, doNew, essentialOnly);
		loadLabel(configServer, root);
	}

	private static void loadLabel(ConfigServerImpl configServer, Struct root) {
		Array children = ConfigWebUtil.getAsArray("labels", "label", root);

		Map<String, String> labels = new HashMap<String, String>();
		if (children != null) {
			Iterator<?> it = children.getIterator();
			Struct data;
			while (it.hasNext()) {
				data = Caster.toStruct(it.next(), null);
				if (data == null) continue;
				String id = ConfigWebUtil.getAsString("id", data, null);
				String name = ConfigWebUtil.getAsString("name", data, null);
				if (id != null && name != null) {
					labels.put(id, name);
				}
			}
		}
		configServer.setLabels(labels);
	}

	private static void createContextFiles(Resource configDir, ConfigServer config, boolean doNew, boolean cleanupDatasources) {

		Resource contextDir = configDir.getRealResource("context");
		Resource adminDir = contextDir.getRealResource("admin");

		// Debug
		Resource debug = adminDir.getRealResource("debug");
		create("/resource/context/admin/debug/", new String[] { "Debug.cfc", "Field.cfc", "Group.cfc", "Classic.cfc", "Simple.cfc", "Modern.cfc", "Comment.cfc" }, debug, doNew);

		// DB Drivers types
		Resource dbDir = adminDir.getRealResource("dbdriver");
		Resource typesDir = dbDir.getRealResource("types");
		create("/resource/context/admin/dbdriver/types/", new String[] { "IDriver.cfc", "Driver.cfc", "IDatasource.cfc", "IDriverSelector.cfc", "Field.cfc" }, typesDir, doNew);

		create("/resource/context/admin/dbdriver/", new String[] { "Other.cfc" }, dbDir, doNew);

		// Cache Drivers
		Resource cDir = adminDir.getRealResource("cdriver");
		create("/resource/context/admin/cdriver/", new String[] { "Cache.cfc", "RamCache.cfc"
				// ,"EHCache.cfc"
				, "Field.cfc", "Group.cfc" }, cDir, doNew);

		Resource wcdDir = configDir.getRealResource("web-context-deployment/admin");
		Resource cdDir = wcdDir.getRealResource("cdriver");

		try {
			ResourceUtil.deleteEmptyFolders(wcdDir);
		}
		catch (IOException e) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), ConfigServerFactory.class.getName(), e);
		}

		// Mail Server Drivers
		Resource msDir = adminDir.getRealResource("mailservers");
		create("/resource/context/admin/mailservers/",
				new String[] { "Other.cfc", "GMail.cfc", "GMX.cfc", "iCloud.cfc", "Yahoo.cfc", "Outlook.cfc", "MailCom.cfc", "MailServer.cfc" }, msDir, doNew);

		// Gateway Drivers
		Resource gDir = adminDir.getRealResource("gdriver");
		create("/resource/context/admin/gdriver/",
				new String[] { "TaskGatewayDriver.cfc", "AsynchronousEvents.cfc", "DirectoryWatcher.cfc", "MailWatcher.cfc", "Gateway.cfc", "Field.cfc", "Group.cfc" }, gDir,
				doNew);

		// Logging/appender
		Resource app = adminDir.getRealResource("logging/appender");
		create("/resource/context/admin/logging/appender/",
				new String[] { "DatasourceAppender.cfc", "ConsoleAppender.cfc", "ResourceAppender.cfc", "Appender.cfc", "Field.cfc", "Group.cfc" }, app, doNew);

		// Logging/layout
		Resource lay = adminDir.getRealResource("logging/layout");
		create("/resource/context/admin/logging/layout/",
				new String[] { "ClassicLayout.cfc", "HTMLLayout.cfc", "PatternLayout.cfc", "XMLLayout.cfc", "Layout.cfc", "Field.cfc", "Group.cfc" }, lay, doNew);

		// Security / SSL
		Resource secDir = configDir.getRealResource("security");
		if (!secDir.exists()) secDir.mkdirs();
		Resource res = create("/resource/security/", "cacerts", secDir, false);
		if (SystemUtil.getSystemPropOrEnvVar("lucee.use.lucee.SSL.TrustStore", "").equalsIgnoreCase("true")) System.setProperty("javax.net.ssl.trustStore", res.toString());
		// Allow using system proxies
		if (!SystemUtil.getSystemPropOrEnvVar("lucee.disable.systemProxies", "").equalsIgnoreCase("true")) System.setProperty("java.net.useSystemProxies", "true"); // it defaults
																																									// to false

		// Jacob
		if (SystemUtil.isWindows()) {

			Resource binDir = configDir.getRealResource("bin");
			if (binDir != null) {

				if (!binDir.exists()) binDir.mkdirs();

				String name = (SystemUtil.getJREArch() == SystemUtil.ARCH_64) ? "jacob-x64.dll" : "jacob-i586.dll";

				Resource jacob = binDir.getRealResource(name);
				if (!jacob.exists()) {
					createFileFromResourceEL("/resource/bin/windows" + ((SystemUtil.getJREArch() == SystemUtil.ARCH_64) ? "64" : "32") + "/" + name, jacob);
				}
				System.setProperty(LibraryLoader.JACOB_DLL_PATH, jacob.getAbsolutePath());
				System.setProperty(LibraryLoader.JACOB_DLL_NAME, name);
			}
		}
	}

}