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

import static lucee.runtime.db.DatasourceManagerImpl.QOQ_DATASOURCE_NAME;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import lucee.Info;
import lucee.commons.collection.MapFactory;
import lucee.commons.date.TimeZoneConstants;
import lucee.commons.date.TimeZoneUtil;
import lucee.commons.digest.HashUtil;
import lucee.commons.digest.MD5;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.LoggerAndSourceData;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.type.cfml.CFMLResourceProvider;
import lucee.commons.io.res.type.s3.DummyS3ResourceProvider;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.retirement.RetireOutputStream;
import lucee.commons.lang.ByteSizeParser;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.URLDecoder;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Component;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheConnectionImpl;
import lucee.runtime.cache.ServerCacheConnection;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.request.RequestCacheHandler;
import lucee.runtime.cache.tag.timespan.TimespanCacheHandler;
import lucee.runtime.cfx.customtag.CFXTagClass;
import lucee.runtime.cfx.customtag.JavaCFXTagClass;
import lucee.runtime.component.ImportDefintion;
//import lucee.runtime.config.ajax.AjaxFactory;
import lucee.runtime.config.component.ComponentFactory;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceImpl;
import lucee.runtime.db.JDBCDriver;
import lucee.runtime.db.ParamSyntax;
import lucee.runtime.dump.ClassicHTMLDumpWriter;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.dump.DumpWriterEntry;
import lucee.runtime.dump.HTMLDumpWriter;
import lucee.runtime.dump.SimpleHTMLDumpWriter;
import lucee.runtime.dump.TextDumpWriter;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ConsoleExecutionLog;
import lucee.runtime.engine.ExecutionLog;
import lucee.runtime.engine.ExecutionLogFactory;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.engine.ThreadQueueImpl;
import lucee.runtime.engine.ThreadQueueNone;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.extension.Extension;
import lucee.runtime.extension.ExtensionImpl;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.gateway.GatewayEntryImpl;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.listener.MixedAppListener;
import lucee.runtime.listener.ModernAppListener;
import lucee.runtime.monitor.ActionMonitor;
import lucee.runtime.monitor.ActionMonitorCollector;
import lucee.runtime.monitor.ActionMonitorFatory;
import lucee.runtime.monitor.ActionMonitorWrap;
import lucee.runtime.monitor.AsyncRequestMonitor;
import lucee.runtime.monitor.IntervallMonitor;
import lucee.runtime.monitor.IntervallMonitorWrap;
import lucee.runtime.monitor.Monitor;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.monitor.RequestMonitorPro;
import lucee.runtime.monitor.RequestMonitorProImpl;
import lucee.runtime.monitor.RequestMonitorWrap;
import lucee.runtime.net.amf.AMFEngine;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.mail.ServerImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.orm.DummyORMEngine;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMConfigurationImpl;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.ConstructorInstance;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.search.DummySearchEngine;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.ScriptProtect;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.spooler.SpoolerEngineImpl;
import lucee.runtime.tag.TagUtil;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.type.scope.ClusterRemote;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.video.VideoExecuter;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibException;

/**
 * 
 */
public final class XMLConfigWebFactory extends XMLConfigFactory {

	private static final String TEMPLATE_EXTENSION = "cfm";
	private static final String COMPONENT_EXTENSION = "cfc";
	private static final String COMPONENT_EXTENSION_LUCEE = "lucee";
	private static final long GB1 = 1024 * 1024 * 1024;
	public static final boolean LOG = true;
	private static final int DEFAULT_MAX_CONNECTION = 100;

	/**
	 * creates a new ServletConfig Impl Object
	 * 
	 * @param configServer
	 * @param configDir
	 * @param servletConfig
	 * @return new Instance
	 * @throws SAXException
	 * @throws ClassNotFoundException
	 * @throws PageException
	 * @throws IOException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 * @throws NoSuchAlgorithmException
	 * @throws BundleException
	 */

	public static ConfigWebImpl newInstance(CFMLEngine engine, CFMLFactoryImpl factory, ConfigServerImpl configServer, Resource configDir, boolean isConfigDirACustomSetting,
			ServletConfig servletConfig)
			throws SAXException, ClassException, PageException, IOException, TagLibException, FunctionLibException, NoSuchAlgorithmException, BundleException {

		String hash = SystemUtil.hash(servletConfig.getServletContext());
		Map<String, String> labels = configServer.getLabels();
		String label = null;
		if (labels != null) {
			label = labels.get(hash);
		}
		if (label == null) label = hash;

		// make sure the web context does not point to the same directory as the server context
		if (configDir.equals(configServer.getConfigDir()))
			throw new ApplicationException("the web context [" + label + "] has defined the same configuration directory [" + configDir + "] as the server context");

		ConfigWeb[] webs = configServer.getConfigWebs();
		if (!ArrayUtil.isEmpty(webs)) {
			for (int i = 0; i < webs.length; i++) {
				// not sure this is necessary if(hash.equals(((ConfigWebImpl)webs[i]).getHash())) continue;
				if (configDir.equals(webs[i].getConfigDir())) throw new ApplicationException(
						"the web context [" + label + "] has defined the same configuration directory [" + configDir + "] as the web context [" + webs[i].getLabel() + "]");
			}
		}

		LogUtil.logGlobal(configServer, Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(),
				"===================================================================\n" + "WEB CONTEXT (" + label + ")\n"
						+ "-------------------------------------------------------------------\n" + "- config:" + configDir + (isConfigDirACustomSetting ? " (custom setting)" : "")
						+ "\n" + "- webroot:" + ReqRspUtil.getRootPath(servletConfig.getServletContext()) + "\n" + "- hash:" + hash + "\n" + "- label:" + label + "\n"
						+ "===================================================================\n"

		);

		int iDoNew = getNew(engine, configDir, false, UpdateInfo.NEW_NONE).updateType;
		boolean doNew = iDoNew != NEW_NONE;

		Resource configFile = configDir.getRealResource("lucee-web.xml." + TEMPLATE_EXTENSION);

		String strPath = servletConfig.getServletContext().getRealPath("/WEB-INF");
		Resource path = ResourcesImpl.getFileResourceProvider().getResource(strPath);

		// get config file
		if (!configFile.exists() || configFile.length() == 0) {
			createConfigFile("web", configFile);
		}
		Document doc = null;

		Resource bugFile;
		int count = 1;

		doc = loadDocumentCreateIfFails(configFile, "web");

		// htaccess
		if (path.exists()) createHtAccess(path.getRealResource(".htaccess"));
		if (configDir.exists()) createHtAccess(configDir.getRealResource(".htaccess"));

		createContextFiles(configDir, servletConfig, doNew);
		ConfigWebImpl configWeb = new ConfigWebImpl(factory, configServer, servletConfig, configDir, configFile);

		load(configServer, configWeb, doc, false, doNew);
		createContextFilesPost(configDir, configWeb, servletConfig, false, doNew);

		// call web.cfc for this context
		((CFMLEngineImpl) ConfigWebUtil.getEngine(configWeb)).onStart(configWeb, false);

		return configWeb;
	}

	private static void createHtAccess(Resource htAccess) {
		if (!htAccess.exists()) {
			htAccess.createNewFile();

			String content = "AuthName \"WebInf Folder\"\n" + "AuthType Basic\n" + "<Limit GET POST>\n" + "order deny,allow\n" + "deny from all\n" + "</Limit>";
			try {
				IOUtil.copy(new ByteArrayInputStream(content.getBytes()), htAccess, true);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
	}

	/**
	 * reloads the Config Object
	 * 
	 * @param cs
	 * @param force
	 * @throws SAXException
	 * @throws ClassNotFoundException
	 * @throws PageException
	 * @throws IOException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 * @throws BundleException
	 * @throws NoSuchAlgorithmException
	 */
	public static void reloadInstance(CFMLEngine engine, ConfigServerImpl cs, ConfigWebImpl cw, boolean force)
			throws SAXException, ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException {
		Resource configFile = cw.getConfigFile();
		Resource configDir = cw.getConfigDir();

		int iDoNew = getNew(engine, configDir, false, UpdateInfo.NEW_NONE).updateType;
		boolean doNew = iDoNew != NEW_NONE;

		if (configFile == null) return;

		if (second(cw.getLoadTime()) > second(configFile.lastModified()) && !force) return;

		Document doc = loadDocument(configFile);
		createContextFiles(configDir, null, doNew);
		cw.reset();
		load(cs, cw, doc, true, doNew);
		createContextFilesPost(configDir, cw, null, false, doNew);

		((CFMLEngineImpl) ConfigWebUtil.getEngine(cw)).onStart(cw, true);
	}

	private static long second(long ms) {
		return ms / 1000;
	}

	/**
	 * @param cs
	 * @param config
	 * @param doc
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws FunctionLibException
	 * @throws TagLibException
	 * @throws PageException
	 * @throws BundleException
	 */
	synchronized static void load(ConfigServerImpl cs, ConfigImpl config, Document doc, boolean isReload, boolean doNew) throws IOException {
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "start reading config");
		ThreadLocalConfig.register(config);
		boolean reload = false;

		try {
			// fix stuff from older config files
			if (XMLConfigAdmin.fixLFI(doc)) {
				String xml = XMLCaster.toString(doc);
				// TODO 4.5->5.0
				xml = StringUtil.replace(xml, "<lucee-configuration", "<cfLuceeConfiguration", false);
				xml = StringUtil.replace(xml, "</lucee-configuration", "</cfLuceeConfiguration", false);
				IOUtil.write(config.getConfigFile(), xml, CharsetUtil.UTF8, false);
				try {
					doc = XMLConfigWebFactory.loadDocument(config.getConfigFile());
				}
				catch (SAXException e) {
				}
			}

			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "fixed LFI");

			if (XMLConfigAdmin.fixSaltAndPW(doc, config)) reload = true;
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "fixed salt");

			if (XMLConfigAdmin.fixS3(doc)) reload = true;
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "fixed S3");

			if (XMLConfigAdmin.fixPSQ(doc)) reload = true;
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "fixed PSQ");

			if (XMLConfigAdmin.fixLogging(cs, config, doc)) reload = true;
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "fixed logging");

			if (XMLConfigAdmin.fixExtension(config, doc)) reload = true;
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "fixed Extension");

			if (XMLConfigAdmin.fixComponentMappings(config, doc)) reload = true;
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "fixed component mappings");

			// delete to big felix.log (there is also code in the loader to do this, but if the loader is not
			// updated ...)
			if (config instanceof ConfigServerImpl) {
				try {
					ConfigServerImpl _cs = (ConfigServerImpl) config;
					File root = _cs.getCFMLEngine().getCFMLEngineFactory().getResourceRoot();
					File log = new File(root, "context/logs/felix.log");
					if (log.isFile() && log.length() > GB1) {
						if (log.delete()) ResourceUtil.touch(log);

					}
				}
				catch (Exception e) {
					log(config, null, e);
				}
			}
			// reload when an old version of xml got updated
			if (reload) {
				doc = reload(doc, config, cs);
				reload = false;
			}

		}
		catch (Exception e) {
			log(config, null, e);
		}

		config.setLastModified();
		if (config instanceof ConfigWeb) ConfigWebUtil.deployWebContext(cs, (ConfigWeb) config, false);
		if (config instanceof ConfigWeb) ConfigWebUtil.deployWeb(cs, (ConfigWeb) config, false);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "deploy web context");
		_loadConfig(cs, config, doc);
		int mode = config.getMode();
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded config");
		_loadConstants(cs, config, doc);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded constants");
		_loadLoggers(cs, config, doc, isReload);
		Log log = config.getLog("application");
		// loadServerLibDesc(cs, config, doc,log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded loggers");
		_loadTempDirectory(cs, config, doc, isReload, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded temp dir");
		_loadId(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded id");
		_loadVersion(config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded version");
		_loadSecurity(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded security");
		try {
			ConfigWebUtil.loadLib(cs, config);
		}
		catch (Exception e) {
			log(config, log, e);
		}
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded lib");
		_loadSystem(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded system");
		_loadResourceProvider(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded resource providers");
		_loadFilesystem(cs, config, doc, doNew, log); // load this before execute any code, what for example loadxtension does (json)
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded filesystem");
		_loadExtensionBundles(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded extension bundles");
		_loadWS(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded webservice");
		_loadORM(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded orm");
		_loadCacheHandler(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded cache handlers");
        _loadScriptProtect(cs, config, doc, log);
        if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded scriptprotect regex");
		_loadCharset(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded charset");
		_loadApplication(cs, config, doc, mode, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded application");
		_loadMappings(cs, config, doc, mode, log); // it is important this runs after
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded mappings");
		// loadApplication
		_loadRest(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded rest");
		_loadExtensions(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded extensions");
		_loadPagePool(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded page pool");
		_loadDataSources(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded datasources");
		_loadCache(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded cache");
		_loadCustomTagsMappings(cs, config, doc, mode, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded custom tag mappings");
		// loadFilesystem(cs, config, doc, doNew); // load tlds
		_loadTag(cs, config, doc, log); // load tlds
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded tags");
		_loadRegional(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded regional");
		_loadCompiler(cs, config, doc, mode, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded compiler");
		_loadScope(cs, config, doc, mode, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded scope");
		_loadMail(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded mail");
		_loadSearch(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded search");
		_loadScheduler(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded scheduled tasks");
		_loadDebug(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded debug");
		_loadError(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded error");
		_loadRegex(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded regex");
		_loadCFX(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded cfx");
		_loadComponent(cs, config, doc, mode, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded component");
		_loadUpdate(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded update");
		_loadJava(cs, config, doc, log); // define compile type
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded java");
		_loadSetting(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded setting");
		_loadProxy(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded proxy");
		_loadRemoteClient(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded remote clients");
		_loadVideo(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded video");
		_loadFlex(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded flex");
		settings(config, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded settings2");
		_loadListener(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded listeners");
		_loadDumpWriter(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded dump writers");
		_loadGatewayEL(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded gateways");
		_loadExeLog(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded exe log");
		_loadQueue(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded queue");
		_loadMonitors(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded monitors");
		_loadLogin(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded login");
		_loadStartupHook(cs, config, doc, log);
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "loaded startup hook");

		config.setLoadTime(System.currentTimeMillis());

		if (config instanceof ConfigWebImpl) {
			TagUtil.addTagMetaData((ConfigWebImpl) config, log);
			if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "added tag meta data");
		}
	}

	private static Document reload(Document doc, ConfigImpl config, ConfigServerImpl cs) throws PageException, IOException {
		XMLCaster.writeTo(doc, config.getConfigFile());
		try {
			doc = XMLConfigWebFactory.loadDocument(config.getConfigFile());
		}
		catch (SAXException e) {
		}
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(cs == null ? config : cs), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "reload xml");
		return doc;
	}

	private static void _loadResourceProvider(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasCS = configServer != null;
			config.clearResourceProviders();
			Element resources = doc != null ? getChildByName(doc.getDocumentElement(), "resources") : null;
			Element[] providers = resources != null ? getChildren(resources, "resource-provider") : new Element[0];
			Element[] defaultProviders = resources != null ? getChildren(resources, "default-resource-provider") : new Element[0];

			// Default Resource Provider
			if (hasCS) config.setDefaultResourceProvider(configServer.getDefaultResourceProvider());
			if (defaultProviders != null && defaultProviders.length > 0) {
				Element defaultProvider = defaultProviders[defaultProviders.length - 1];
				ClassDefinition defProv = getClassDefinition(defaultProvider, "", config.getIdentification());

				String strDefaultProviderComponent = getAttr(defaultProvider, "component");
				if (StringUtil.isEmpty(strDefaultProviderComponent)) strDefaultProviderComponent = getAttr(defaultProvider, "class");

				// class
				if (defProv.hasClass()) {
					config.setDefaultResourceProvider(defProv.getClazz(), toArguments(getAttr(defaultProvider, "arguments"), true));
				}

				// component
				else if (!StringUtil.isEmpty(strDefaultProviderComponent)) {
					strDefaultProviderComponent = strDefaultProviderComponent.trim();
					Map<String, String> args = toArguments(getAttr(defaultProvider, "arguments"), true);
					args.put("component", strDefaultProviderComponent);
					config.setDefaultResourceProvider(CFMLResourceProvider.class, args);
				}
			}
			// Resource Provider
			if (hasCS) config.setResourceProviderFactories(configServer.getResourceProviderFactories());
			if (providers != null && providers.length > 0) {
				ClassDefinition prov;
				String strProviderCFC;
				String strProviderScheme;
				ClassDefinition httpClass = null;
				Map httpArgs = null;
				boolean hasHTTPs = false;
				for (int i = 0; i < providers.length; i++) {
					try {
						prov = getClassDefinition(providers[i], "", config.getIdentification());
						strProviderCFC = getAttr(providers[i], "component");
						if (StringUtil.isEmpty(strProviderCFC)) strProviderCFC = getAttr(providers[i], "class");

						// ignore OLD S3 extension from 4.0
						// lucee.commons.io.res.type.s3.S3ResourceProvider
						if ("lucee.extension.io.resource.type.s3.S3ResourceProvider".equals(prov.getClassName())
								|| "lucee.commons.io.res.type.s3.S3ResourceProvider".equals(prov.getClassName()))
							continue;
						// prov=new ClassDefinitionImpl(S3ResourceProvider.class);

						strProviderScheme = getAttr(providers[i], "scheme");
						// class
						if (prov.hasClass() && !StringUtil.isEmpty(strProviderScheme)) {
							strProviderScheme = strProviderScheme.trim().toLowerCase();
							config.addResourceProvider(strProviderScheme, prov, toArguments(getAttr(providers[i], "arguments"), true));

							// patch for user not having
							if (strProviderScheme.equalsIgnoreCase("http")) {
								httpClass = prov;
								httpArgs = toArguments(getAttr(providers[i], "arguments"), true);
							}
							else if (strProviderScheme.equalsIgnoreCase("https")) hasHTTPs = true;
						}

						// cfc
						else if (!StringUtil.isEmpty(strProviderCFC) && !StringUtil.isEmpty(strProviderScheme)) {
							strProviderCFC = strProviderCFC.trim();
							strProviderScheme = strProviderScheme.trim().toLowerCase();
							Map<String, String> args = toArguments(getAttr(providers[i], "arguments"), true);
							args.put("component", strProviderCFC);
							config.addResourceProvider(strProviderScheme, new ClassDefinitionImpl(CFMLResourceProvider.class), args);
						}
					}
					catch (Throwable t) { // TODO log the exception
						ExceptionUtil.rethrowIfNecessary(t);
					}
				}

				// adding https when not exist
				if (!hasHTTPs && httpClass != null) {
					config.addResourceProvider("https", httpClass, httpArgs);
				}
				// adding s3 when not exist

				// we make sure we have the default on server level
				if (!hasCS && !config.hasResourceProvider("s3")) {
					ClassDefinition s3Class = new ClassDefinitionImpl(DummyS3ResourceProvider.class);
					config.addResourceProvider("s3", s3Class, toArguments("lock-timeout:10000;", false));
				}
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static ClassDefinition getClassDefinition(Element el, String prefix, Identification id) {
		String cn = getAttr(el, prefix + "class");
		String bn = getAttr(el, prefix + "bundle-name");
		String bv = getAttr(el, prefix + "bundle-version");

		// proxy jar libary no longer provided, so if still this class name is used ....
		if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cn)) {
			cn = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		}

		ClassDefinition cd = new ClassDefinitionImpl(cn, bn, bv, id);
		// if(!StringUtil.isEmpty(cd.className,true))cd.getClazz();
		return cd;
	}

	private static void _loadCacheHandler(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasCS = configServer != null;
			// !!!! config.clearResourceProviders();

			// first of all we make sure we have a request and timespan cachehandler
			if (!hasCS) {
				config.addCacheHandler("request", new ClassDefinitionImpl(RequestCacheHandler.class));
				config.addCacheHandler("timespan", new ClassDefinitionImpl(TimespanCacheHandler.class));
			}

			// add CacheHandlers from server context to web context
			if (hasCS) {
				Iterator<Entry<String, Class<CacheHandler>>> it = configServer.getCacheHandlers();
				if (it != null) {
					Entry<String, Class<CacheHandler>> entry;
					while (it.hasNext()) {
						entry = it.next();
						config.addCacheHandler(entry.getKey(), entry.getValue());
					}
				}
			}

			Element root = doc == null ? null : getChildByName(doc.getDocumentElement(), "cache-handlers");
			Element[] handlers = root == null ? null : getChildren(root, "cache-handler");
			if (!ArrayUtil.isEmpty(handlers)) {
				ClassDefinition cd;
				String strId;
				for (int i = 0; i < handlers.length; i++) {
					cd = getClassDefinition(handlers[i], "", config.getIdentification());
					strId = getAttr(handlers[i], "id");

					if (cd.hasClass() && !StringUtil.isEmpty(strId)) {
						strId = strId.trim().toLowerCase();
						try {
							config.addCacheHandler(strId, cd);
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
							log.error("Cache-Handler", t);
						}
					}
				}
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

    private static void _loadScriptProtect(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
        try {
            boolean hasCS = configServer != null;

            // add scriptprotect-regex from server context to web context
            if (hasCS) {
                config.setScriptProtectRegexList(configServer.getScriptProtectRegexList());
            }

            Element root = doc == null ? null : getChildByName(doc.getDocumentElement(), "scriptprotect");
            Element[] regexFilters = root == null ? null : getChildren(root, "filter-regex");
            if (!ArrayUtil.isEmpty(regexFilters)) {
                for (Element regexFilter : regexFilters) {
                    String regexValue = getAttr(regexFilter, "value");
                    Pattern regexPattern = Pattern.compile(regexValue, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    config.addScriptProtectRegex(regexPattern);
                }
            }
            else {
                //Loading default scriptprotect settings
                Pattern invalidTagRegex = Pattern.compile(ScriptProtect.INVALID_TAG_REGEX_DEFAULT, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                config.addScriptProtectRegex(invalidTagRegex);
            }
        }
        catch (Exception e) {
            log(config, log, e);
        }
    }

	private static void _loadDumpWriter(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasCS = configServer != null;

			Element coll = doc != null ? getChildByName(doc.getDocumentElement(), "dump-writers") : null;
			Element[] writers = coll != null ? getChildren(coll, "dump-writer") : new Element[0];

			Struct sct = new StructImpl();

			boolean hasPlain = false;
			boolean hasRich = false;
			if (hasCS) {
				DumpWriterEntry[] entries = configServer.getDumpWritersEntries();
				if (entries != null) for (int i = 0; i < entries.length; i++) {
					if (entries[i].getDefaultType() == HTMLDumpWriter.DEFAULT_PLAIN) hasPlain = true;
					if (entries[i].getDefaultType() == HTMLDumpWriter.DEFAULT_RICH) hasRich = true;
					sct.put(entries[i].getName(), entries[i]);
				}
			}

			if (writers != null && writers.length > 0) {
				ClassDefinition cd;
				String strName;
				String strDefault;
				Class clazz;
				int def = HTMLDumpWriter.DEFAULT_NONE;
				for (int i = 0; i < writers.length; i++) {
					cd = getClassDefinition(writers[i], "", config.getIdentification());
					strName = getAttr(writers[i], "name");
					strDefault = getAttr(writers[i], "default");
					clazz = cd.getClazz(null);
					if (clazz != null && !StringUtil.isEmpty(strName)) {
						if (StringUtil.isEmpty(strDefault)) def = HTMLDumpWriter.DEFAULT_NONE;
						else if ("browser".equalsIgnoreCase(strDefault)) def = HTMLDumpWriter.DEFAULT_RICH;
						else if ("console".equalsIgnoreCase(strDefault)) def = HTMLDumpWriter.DEFAULT_PLAIN;
						sct.put(strName, new DumpWriterEntry(def, strName, (DumpWriter) ClassUtil.loadInstance(clazz)));
					}
				}
			}
			else {
				// print.err("yep");
				if (!hasRich) sct.setEL(KeyConstants._html, new DumpWriterEntry(HTMLDumpWriter.DEFAULT_RICH, "html", new HTMLDumpWriter()));
				if (!hasPlain) sct.setEL(KeyConstants._text, new DumpWriterEntry(HTMLDumpWriter.DEFAULT_PLAIN, "text", new TextDumpWriter()));

				sct.setEL(KeyConstants._classic, new DumpWriterEntry(HTMLDumpWriter.DEFAULT_NONE, "classic", new ClassicHTMLDumpWriter()));
				sct.setEL(KeyConstants._simple, new DumpWriterEntry(HTMLDumpWriter.DEFAULT_NONE, "simple", new SimpleHTMLDumpWriter()));

			}
			Iterator<Object> it = sct.valueIterator();
			java.util.List<DumpWriterEntry> entries = new ArrayList<DumpWriterEntry>();
			while (it.hasNext()) {
				entries.add((DumpWriterEntry) it.next());
			}
			config.setDumpWritersEntries(entries.toArray(new DumpWriterEntry[entries.size()]));
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	static Map<String, String> toArguments(String attributes, boolean decode) {
		return cssStringToMap(attributes, decode, false);

	}

	public static Map<String, String> cssStringToMap(String attributes, boolean decode, boolean lowerKeys) {
		Map<String, String> map = new HashMap<String, String>();
		if (StringUtil.isEmpty(attributes, true)) return map;
		String[] arr = ListUtil.toStringArray(ListUtil.listToArray(attributes, ';'), null);

		int index;
		String str;
		for (int i = 0; i < arr.length; i++) {
			str = arr[i].trim();
			if (StringUtil.isEmpty(str)) continue;
			index = str.indexOf(':');
			if (index == -1) map.put(lowerKeys ? str.toLowerCase() : str, "");
			else {
				String k = dec(str.substring(0, index).trim(), decode);
				if (lowerKeys) k = k.toLowerCase();
				map.put(k, dec(str.substring(index + 1).trim(), decode));
			}
		}
		return map;
	}

	private static String dec(String str, boolean decode) {
		if (!decode) return str;
		return URLDecoder.decode(str, false);
	}

	private static void _loadListener(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			if (config instanceof ConfigServer) {
				ConfigServer cs = (ConfigServer) config;
				Element listener = doc != null ? getChildByName(doc.getDocumentElement(), "listener") : null;
				ClassDefinition cd = listener != null ? getClassDefinition(listener, "", config.getIdentification()) : null;
				String strArguments = getAttr(listener, "arguments");
				if (strArguments == null) strArguments = "";

				if (cd != null && cd.hasClass()) {
					try {

						Object obj = ClassUtil.loadInstance(cd.getClazz(), new Object[] { strArguments }, null);
						if (obj instanceof ConfigListener) {
							ConfigListener cl = (ConfigListener) obj;
							cs.setConfigListener(cl);
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						t.printStackTrace(config.getErrWriter());

					}

				}
			}
			else if (configServer != null) {
				ConfigListener listener = configServer.getConfigListener();
				if (listener != null) listener.onLoadWebContext(configServer, (ConfigWeb) config);
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void settings(ConfigImpl config, Log log) {
		try {
			doCheckChangesInLibraries(config);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadVersion(ConfigImpl config, Document doc, Log log) {
		try {
			Element luceeConfiguration = doc != null ? doc.getDocumentElement() : null;
			String strVersion = getAttr(luceeConfiguration, "version");
			config.setVersion(Caster.toDoubleValue(strVersion, 1.0d));
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadId(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {

			if (doc == null && configServer != null) {
				Identification id = configServer.getIdentification();
				((ConfigWebImpl) config).setIdentification(new IdentificationWebImpl((ConfigWebImpl) config, id.getSecurityKey(), id.getApiKey()));
				return;
			}

			// Security key
			Resource res = config.getConfigDir().getRealResource("id");
			String securityKey = null;
			try {
				if (!res.exists()) {
					res.createNewFile();
					IOUtil.write(res, securityKey = UUID.randomUUID().toString(), SystemUtil.getCharset(), false);
				}
				else {
					securityKey = IOUtil.toString(res, SystemUtil.getCharset());
				}
			}
			catch (Exception ioe) {
				log(config, log, ioe);
			}
			if (StringUtil.isEmpty(securityKey)) securityKey = UUID.randomUUID().toString();

			// API Key
			String apiKey = null;
			String str = doc != null ? getAttr(doc.getDocumentElement(), "api-key") : null;
			if (!StringUtil.isEmpty(str, true)) apiKey = str.trim();
			else if (configServer != null) apiKey = configServer.getIdentification().getApiKey(); // if there is no web api key the server api key is used

			if (config instanceof ConfigWebImpl) ((ConfigWebImpl) config).setIdentification(new IdentificationWebImpl((ConfigWebImpl) config, securityKey, apiKey));
			else((ConfigServerImpl) config).setIdentification(new IdentificationServerImpl((ConfigServerImpl) config, securityKey, apiKey));
			config.getIdentification().getId();
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static boolean equal(Resource[] srcs, Resource[] trgs) {
		if (srcs.length != trgs.length) return false;
		Resource src;
		outer: for (int i = 0; i < srcs.length; i++) {
			src = srcs[i];
			for (int y = 0; y < trgs.length; y++) {
				if (src.equals(trgs[y])) continue outer;
			}
			return false;
		}
		return true;
	}

	private static Resource[] getNewResources(Resource[] srcs, Resource[] trgs) {
		Resource trg;
		java.util.List<Resource> list = new ArrayList<Resource>();
		outer: for (int i = 0; i < trgs.length; i++) {
			trg = trgs[i];
			for (int y = 0; y < srcs.length; y++) {
				if (trg.equals(srcs[y])) continue outer;
			}
			list.add(trg);
		}
		return list.toArray(new Resource[list.size()]);
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 */
	private static void _loadSecurity(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			// Serial Number
			if (config instanceof ConfigServer) {
				Element luceeConfiguration = doc.getDocumentElement();
				String serial = getAttr(luceeConfiguration, "serial-number");
				if (!StringUtil.isEmpty(serial)) config.setSerialNumber(serial);
			}
			else if (configServer != null) {
				config.setSerialNumber(configServer.getSerialNumber());
			}

			// Security Manger
			SecurityManager securityManager = null;
			if (config instanceof ConfigServerImpl) {
				ConfigServerImpl cs = (ConfigServerImpl) config;
				Element security = doc != null ? getChildByName(doc.getDocumentElement(), "security") : null;

				// Default SecurityManager
				SecurityManagerImpl sm = _toSecurityManager(security);

				// additional file access directories
				Element[] elFileAccesses = security != null ? getChildren(security, "file-access") : new Element[0];
				sm.setCustomFileAccess(_loadFileAccess(config, elFileAccesses));

				cs.setDefaultSecurityManager(sm);

				// Web SecurityManager
				Element[] accessors = security != null ? getChildren(security, "accessor") : new Element[0];
				for (int i = 0; i < accessors.length; i++) {
					String id = getAttr(accessors[i], "id");
					if (id != null) {
						sm = _toSecurityManager(accessors[i]);
						elFileAccesses = getChildren(accessors[i], "file-access");
						sm.setCustomFileAccess(_loadFileAccess(config, elFileAccesses));
						cs.setSecurityManager(id, sm);
					}
				}

			}

			else if (configServer != null) {
				securityManager = configServer.getSecurityManager(config.getIdentification().getId());
			}
			if (config instanceof ConfigWebImpl) {
				if (securityManager == null) securityManager = SecurityManagerImpl.getOpenSecurityManager();
				((ConfigWebImpl) config).setSecurityManager(securityManager);
			}

			Element security = doc != null ? getChildByName(doc.getDocumentElement(), "security") : null;
			if (security != null) {
				int vu = AppListenerUtil.toVariableUsage(security.getAttribute("variable-usage"), ConfigImpl.QUERY_VAR_USAGE_IGNORE);
				config.setQueryVarUsage(vu);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			log(config, log, e);
		}
	}

	private static Resource[] _loadFileAccess(Config config, Element[] fileAccesses) {
		if (ArrayUtil.isEmpty(fileAccesses)) return new Resource[0];

		java.util.List<Resource> reses = new ArrayList<Resource>();
		String path;
		Resource res;
		for (int i = 0; i < fileAccesses.length; i++) {
			path = getAttr(fileAccesses[i], "path");
			if (!StringUtil.isEmpty(path)) {
				res = config.getResource(path);
				if (res.isDirectory()) reses.add(res);
			}
		}
		return reses.toArray(new Resource[reses.size()]);
	}

	private static SecurityManagerImpl _toSecurityManager(Element el) {
		SecurityManagerImpl sm = new SecurityManagerImpl(_attr(el, "setting", SecurityManager.VALUE_YES), _attr(el, "file", SecurityManager.VALUE_ALL),
				_attr(el, "direct_java_access", SecurityManager.VALUE_YES), _attr(el, "mail", SecurityManager.VALUE_YES), _attr(el, "datasource", SecurityManager.VALUE_YES),
				_attr(el, "mapping", SecurityManager.VALUE_YES), _attr(el, "remote", SecurityManager.VALUE_YES), _attr(el, "custom_tag", SecurityManager.VALUE_YES),
				_attr(el, "cfx_setting", SecurityManager.VALUE_YES), _attr(el, "cfx_usage", SecurityManager.VALUE_YES), _attr(el, "debugging", SecurityManager.VALUE_YES),
				_attr(el, "search", SecurityManager.VALUE_YES), _attr(el, "scheduled_task", SecurityManager.VALUE_YES), _attr(el, "tag_execute", SecurityManager.VALUE_YES),
				_attr(el, "tag_import", SecurityManager.VALUE_YES), _attr(el, "tag_object", SecurityManager.VALUE_YES), _attr(el, "tag_registry", SecurityManager.VALUE_YES),
				_attr(el, "cache", SecurityManager.VALUE_YES), _attr(el, "gateway", SecurityManager.VALUE_YES), _attr(el, "orm", SecurityManager.VALUE_YES),
				_attr2(el, "access_read", SecurityManager.ACCESS_PROTECTED), _attr2(el, "access_write", SecurityManager.ACCESS_PROTECTED));
		return sm;
	}

	private static short _attr(Element el, String attr, short _default) {
		return SecurityManagerImpl.toShortAccessValue(getAttr(el, attr), _default);
	}

	private static short _attr2(Element el, String attr, short _default) {
		String strAccess = getAttr(el, attr);
		if (StringUtil.isEmpty(strAccess)) return _default;
		strAccess = strAccess.trim().toLowerCase();
		if ("open".equals(strAccess)) return SecurityManager.ACCESS_OPEN;
		if ("protected".equals(strAccess)) return SecurityManager.ACCESS_PROTECTED;
		if ("close".equals(strAccess)) return SecurityManager.ACCESS_CLOSE;
		return _default;
	}

	static String createMD5FromResource(String resource) throws IOException {
		InputStream is = null;
		try {
			is = InfoImpl.class.getResourceAsStream(resource);
			byte[] barr = IOUtil.toBytes(is);
			return MD5.getDigestAsString(barr);
		}
		finally {
			IOUtil.close(is);
		}
	}

	static String createContentFromResource(Resource resource) throws IOException {
		return IOUtil.toString(resource, (Charset) null);
	}

	static void createFileFromResourceCheckSizeDiffEL(String resource, Resource file) {
		try {
			createFileFromResourceCheckSizeDiff(resource, file);
		}
		catch (Exception e) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, XMLConfigWebFactory.class.getName(), resource);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, XMLConfigWebFactory.class.getName(), file + "");
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), XMLConfigWebFactory.class.getName(), e);
		}
	}

	/**
	 * creates a File and his content froma a resurce
	 * 
	 * @param resource
	 * @param file
	 * @throws IOException
	 */
	static void createFileFromResourceCheckSizeDiff(String resource, Resource file) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtil.copy(InfoImpl.class.getResourceAsStream(resource), baos, true, false);
		byte[] barr = baos.toByteArray();

		if (file.exists()) {
			long trgSize = file.length();
			long srcSize = barr.length;
			if (srcSize == trgSize) return;

			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), "update file:" + file);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), " - source:" + srcSize);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(), " - target:" + trgSize);

		}
		else file.createNewFile();
		IOUtil.copy(new ByteArrayInputStream(barr), file, true);
	}

	/**
	 * Creates all files for Lucee Context
	 * 
	 * @param configDir
	 * @throws IOException
	 * @throws IOException
	 */
	private static void createContextFiles(Resource configDir, ServletConfig servletConfig, boolean doNew) throws IOException {
		// NICE dies muss dynamisch erstellt werden, da hier der admin hinkommt
		// und dieser sehr viele files haben wird
		Resource contextDir = configDir.getRealResource("context");
		if (!contextDir.exists()) contextDir.mkdirs();

		// custom locale files
		{
			Resource dir = configDir.getRealResource("locales");
			if (!dir.exists()) dir.mkdirs();
			Resource file = dir.getRealResource("pt-PT-date.df");
			if (!file.exists()) createFileFromResourceEL("/resource/locales/pt-PT-date.df", file);
		}

		// video
		Resource videoDir = configDir.getRealResource("video");
		if (!videoDir.exists()) videoDir.mkdirs();

		Resource video = videoDir.getRealResource("video.xml");
		if (!video.exists()) createFileFromResourceEL("/resource/video/video.xml", video);

		// bin
		Resource binDir = configDir.getRealResource("bin");
		if (!binDir.exists()) binDir.mkdirs();

		Resource ctDir = configDir.getRealResource("customtags");
		if (!ctDir.exists()) ctDir.mkdirs();

		// Jacob
		if (SystemUtil.isWindows()) {
			String name = (SystemUtil.getJREArch() == SystemUtil.ARCH_64) ? "jacob-x64.dll" : "jacob-i586.dll";
			Resource jacob = binDir.getRealResource(name);
			if (!jacob.exists()) {
				createFileFromResourceEL("/resource/bin/windows" + ((SystemUtil.getJREArch() == SystemUtil.ARCH_64) ? "64" : "32") + "/" + name, jacob);
			}
		}

		Resource storDir = configDir.getRealResource("storage");
		if (!storDir.exists()) storDir.mkdirs();

		Resource compDir = configDir.getRealResource("components");
		if (!compDir.exists()) compDir.mkdirs();

		// remove old cacerts files, they are now only in the server context
		Resource secDir = configDir.getRealResource("security");
		Resource f = null;
		if (secDir.exists()) {
			f = secDir.getRealResource("cacerts");
			if (f.exists()) f.delete();

		}
		else secDir.mkdirs();
		f = secDir.getRealResource("antisamy-basic.xml");
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/security/antisamy-basic.xml", f);

		// lucee-context
		f = contextDir.getRealResource("lucee-context.lar");
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/lucee-context.lar", f);
		else createFileFromResourceCheckSizeDiffEL("/resource/context/lucee-context.lar", f);

		// lucee-admin
		f = contextDir.getRealResource("lucee-admin.lar");
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/lucee-admin.lar", f);
		else createFileFromResourceCheckSizeDiffEL("/resource/context/lucee-admin.lar", f);

		// lucee-doc
		f = contextDir.getRealResource("lucee-doc.lar");
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/lucee-doc.lar", f);
		else createFileFromResourceCheckSizeDiffEL("/resource/context/lucee-doc.lar", f);

		f = contextDir.getRealResource("component-dump." + TEMPLATE_EXTENSION);
		if (!f.exists()) createFileFromResourceEL("/resource/context/component-dump." + TEMPLATE_EXTENSION, f);

		// Base Component
		String badContent = "<cfcomponent displayname=\"Component\" hint=\"This is the Base Component\">\n</cfcomponent>";
		String badVersion = "704b5bd8597be0743b0c99a644b65896";
		f = contextDir.getRealResource("Component." + COMPONENT_EXTENSION);

		if (!f.exists()) createFileFromResourceEL("/resource/context/Component." + COMPONENT_EXTENSION, f);
		else if (doNew && badVersion.equals(ConfigWebUtil.createMD5FromResource(f))) {
			createFileFromResourceEL("/resource/context/Component." + COMPONENT_EXTENSION, f);
		}
		else if (doNew && badContent.equals(createContentFromResource(f).trim())) {
			createFileFromResourceEL("/resource/context/Component." + COMPONENT_EXTENSION, f);
		}

		// Component.lucee
		f = contextDir.getRealResource("Component." + COMPONENT_EXTENSION_LUCEE);
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/Component." + COMPONENT_EXTENSION_LUCEE, f);

		f = contextDir.getRealResource(Constants.CFML_APPLICATION_EVENT_HANDLER);
		if (!f.exists()) createFileFromResourceEL("/resource/context/Application." + COMPONENT_EXTENSION, f);

		f = contextDir.getRealResource("form." + TEMPLATE_EXTENSION);
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/form." + TEMPLATE_EXTENSION, f);

		f = contextDir.getRealResource("graph." + TEMPLATE_EXTENSION);
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/graph." + TEMPLATE_EXTENSION, f);

		f = contextDir.getRealResource("wddx." + TEMPLATE_EXTENSION);
		if (!f.exists()) createFileFromResourceEL("/resource/context/wddx." + TEMPLATE_EXTENSION, f);

		f = contextDir.getRealResource("lucee-applet." + TEMPLATE_EXTENSION);
		if (!f.exists()) createFileFromResourceEL("/resource/context/lucee-applet." + TEMPLATE_EXTENSION, f);

		f = contextDir.getRealResource("lucee-applet.jar");
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/lucee-applet.jar", f);

		// f=new BinaryFile(contextDir,"lucee_context.ra");
		// if(!f.exists())createFileFromResource("/resource/context/lucee_context.ra",f);

		f = contextDir.getRealResource("admin." + TEMPLATE_EXTENSION);
		if (!f.exists()) createFileFromResourceEL("/resource/context/admin." + TEMPLATE_EXTENSION, f);

		// Video
		f = contextDir.getRealResource("swfobject.js");
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/video/swfobject.js", f);
		f = contextDir.getRealResource("swfobject.js." + TEMPLATE_EXTENSION);
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/video/swfobject.js." + TEMPLATE_EXTENSION, f);

		f = contextDir.getRealResource("mediaplayer.swf");
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/video/mediaplayer.swf", f);
		f = contextDir.getRealResource("mediaplayer.swf." + TEMPLATE_EXTENSION);
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/video/mediaplayer.swf." + TEMPLATE_EXTENSION, f);

		Resource adminDir = contextDir.getRealResource("admin");
		if (!adminDir.exists()) adminDir.mkdirs();

		// Plugin
		Resource pluginDir = adminDir.getRealResource("plugin");
		if (!pluginDir.exists()) pluginDir.mkdirs();

		f = pluginDir.getRealResource("Plugin." + COMPONENT_EXTENSION);
		if (!f.exists()) createFileFromResourceEL("/resource/context/admin/plugin/Plugin." + COMPONENT_EXTENSION, f);

		// Plugin Note
		Resource note = pluginDir.getRealResource("Note");
		if (!note.exists()) note.mkdirs();

		f = note.getRealResource("language.xml");
		if (!f.exists()) createFileFromResourceEL("/resource/context/admin/plugin/Note/language.xml", f);

		f = note.getRealResource("overview." + TEMPLATE_EXTENSION);
		if (!f.exists()) createFileFromResourceEL("/resource/context/admin/plugin/Note/overview." + TEMPLATE_EXTENSION, f);

		f = note.getRealResource("Action." + COMPONENT_EXTENSION);
		if (!f.exists()) createFileFromResourceEL("/resource/context/admin/plugin/Note/Action." + COMPONENT_EXTENSION, f);

		// gateway
		Resource componentsDir = configDir.getRealResource("components");
		if (!componentsDir.exists()) componentsDir.mkdirs();

		Resource gwDir = componentsDir.getRealResource("lucee/extension/gateway/");
		create("/resource/context/gateway/",
				new String[] { "TaskGateway." + COMPONENT_EXTENSION, "DummyGateway." + COMPONENT_EXTENSION, "DirectoryWatcher." + COMPONENT_EXTENSION,
						"DirectoryWatcherListener." + COMPONENT_EXTENSION, "MailWatcher." + COMPONENT_EXTENSION, "MailWatcherListener." + COMPONENT_EXTENSION,
						"AsynchronousEvents." + COMPONENT_EXTENSION, "AsynchronousEventsListener." + COMPONENT_EXTENSION },
				gwDir, doNew);

		// resources/language
		Resource langDir = adminDir.getRealResource("resources/language");
		create("/resource/context/admin/resources/language/", new String[] { "en.xml", "de.xml" }, langDir, doNew);

		// add Debug
		Resource debug = adminDir.getRealResource("debug");
		create("/resource/context/admin/debug/", new String[] { "Debug." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION }, debug, doNew);

		// add Cache Drivers
		Resource cDir = adminDir.getRealResource("cdriver");
		create("/resource/context/admin/cdriver/", new String[] { "Cache." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION }, cDir, doNew);

		// add DB Drivers types
		Resource dbDir = adminDir.getRealResource("dbdriver");
		Resource typesDir = dbDir.getRealResource("types");
		create("/resource/context/admin/dbdriver/types/", new String[] { "IDriver." + COMPONENT_EXTENSION, "Driver." + COMPONENT_EXTENSION, "IDatasource." + COMPONENT_EXTENSION,
				"IDriverSelector." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION }, typesDir, doNew);

		// add Gateway Drivers
		Resource gDir = adminDir.getRealResource("gdriver");
		create("/resource/context/admin/gdriver/", new String[] { "Gateway." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION }, gDir, doNew);

		// add Logging/appender
		Resource app = adminDir.getRealResource("logging/appender");
		create("/resource/context/admin/logging/appender/", new String[] { "Appender." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION }, app,
				doNew);

		// Logging/layout
		Resource lay = adminDir.getRealResource("logging/layout");
		create("/resource/context/admin/logging/layout/", new String[] { "Layout." + COMPONENT_EXTENSION, "Field." + COMPONENT_EXTENSION, "Group." + COMPONENT_EXTENSION }, lay,
				doNew);

		Resource templatesDir = contextDir.getRealResource("templates");
		if (!templatesDir.exists()) templatesDir.mkdirs();

		Resource errorDir = templatesDir.getRealResource("error");
		if (!errorDir.exists()) errorDir.mkdirs();

		f = errorDir.getRealResource("error." + TEMPLATE_EXTENSION);
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/templates/error/error." + TEMPLATE_EXTENSION, f);

		f = errorDir.getRealResource("error-neo." + TEMPLATE_EXTENSION);
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/templates/error/error-neo." + TEMPLATE_EXTENSION, f);

		f = errorDir.getRealResource("error-public." + TEMPLATE_EXTENSION);
		if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/templates/error/error-public." + TEMPLATE_EXTENSION, f);

		Resource displayDir = templatesDir.getRealResource("display");
		if (!displayDir.exists()) displayDir.mkdirs();

	}

	private static void createContextFilesPost(Resource configDir, ConfigImpl config, ServletConfig servletConfig, boolean isEventGatewayContext, boolean doNew) {
		Resource contextDir = configDir.getRealResource("context");
		if (!contextDir.exists()) contextDir.mkdirs();

		Resource adminDir = contextDir.getRealResource("admin");
		if (!adminDir.exists()) adminDir.mkdirs();

		// Plugin
		Resource pluginDir = adminDir.getRealResource("plugin");
		if (!pluginDir.exists()) pluginDir.mkdirs();

		// deploy org.lucee.cfml components
		if (config instanceof ConfigWeb) {
			ImportDefintion _import = config.getComponentDefaultImport();
			String path = _import.getPackageAsPath();
			Resource components = config.getConfigDir().getRealResource("components");
			Resource dir = components.getRealResource(path);
			dir.mkdirs();
			// print.o(dir);
			ComponentFactory.deploy(dir, doNew);
		}
	}

	private static void doCheckChangesInLibraries(ConfigImpl config) {
		// create current hash from libs
		TagLib[] ctlds = config.getTLDs(CFMLEngine.DIALECT_CFML);
		TagLib[] ltlds = config.getTLDs(CFMLEngine.DIALECT_LUCEE);
		FunctionLib[] cflds = config.getFLDs(CFMLEngine.DIALECT_CFML);
		FunctionLib[] lflds = config.getFLDs(CFMLEngine.DIALECT_LUCEE);

		StringBuilder sb = new StringBuilder();

		// version
		if (config instanceof ConfigWebImpl) {
			Info info = ((ConfigWebImpl) config).getFactory().getEngine().getInfo();
			sb.append(info.getVersion().toString()).append(';');
		}

		// charset
		sb.append(config.getTemplateCharset().name()).append(';');

		// dot notation upper case
		_getDotNotationUpperCase(sb, config.getMappings());
		_getDotNotationUpperCase(sb, config.getCustomTagMappings());
		_getDotNotationUpperCase(sb, config.getComponentMappings());
		_getDotNotationUpperCase(sb, config.getFunctionMappings());
		_getDotNotationUpperCase(sb, config.getTagMappings());
		// _getDotNotationUpperCase(sb,config.getServerTagMapping());
		// _getDotNotationUpperCase(sb,config.getServerFunctionMapping());

		// suppress ws before arg
		sb.append(config.getSuppressWSBeforeArg());
		sb.append(';');

		// externalize strings
		sb.append(config.getExternalizeStringGTE());
		sb.append(';');

		// function output
		sb.append(config.getDefaultFunctionOutput());
		sb.append(';');

		// full null support
		// sb.append(config.getFull Null Support()); // no longer a compiler switch
		// sb.append(';');

		// fusiondebug or not (FD uses full path name)
		sb.append(config.allowRequestTimeout());
		sb.append(';');

		// tld
		for (int i = 0; i < ctlds.length; i++) {
			sb.append(ctlds[i].getHash());
		}
		for (int i = 0; i < ltlds.length; i++) {
			sb.append(ltlds[i].getHash());
		}
		// fld
		for (int i = 0; i < cflds.length; i++) {
			sb.append(cflds[i].getHash());
		}
		for (int i = 0; i < lflds.length; i++) {
			sb.append(lflds[i].getHash());
		}

		if (config instanceof ConfigWeb) {
			boolean hasChanged = false;

			sb.append(";").append(((ConfigWebImpl) config).getConfigServerImpl().getLibHash());
			try {
				String hashValue = HashUtil.create64BitHashAsString(sb.toString());
				// check and compare lib version file
				Resource libHash = config.getConfigDir().getRealResource("lib-hash");

				if (!libHash.exists()) {
					libHash.createNewFile();
					IOUtil.write(libHash, hashValue, SystemUtil.getCharset(), false);
					hasChanged = true;
				}
				else if (!IOUtil.toString(libHash, SystemUtil.getCharset()).equals(hashValue)) {
					IOUtil.write(libHash, hashValue, SystemUtil.getCharset(), false);
					hasChanged = true;
				}
			}
			catch (IOException e) {
			}

			// change Compile type
			if (hasChanged) {
				try {
					// first we delete the physical classes
					config.getClassDirectory().remove(true);

					// now we force the pagepools to flush
					flushPageSourcePool(config.getMappings());
					flushPageSourcePool(config.getCustomTagMappings());
					flushPageSourcePool(config.getComponentMappings());
					flushPageSourcePool(config.getFunctionMappings());
					flushPageSourcePool(config.getTagMappings());

					if (config instanceof ConfigWeb) {
						flushPageSourcePool(((ConfigWebImpl) config).getApplicationMappings());
					}

				}
				catch (IOException e) {
					e.printStackTrace(config.getErrWriter());
				}
			}
		}
		else {
			((ConfigServerImpl) config).setLibHash(HashUtil.create64BitHashAsString(sb.toString()));
		}

	}

	private static void flushPageSourcePool(Mapping... mappings) {
		for (int i = 0; i < mappings.length; i++) {
			if (mappings[i] instanceof MappingImpl) ((MappingImpl) mappings[i]).flush(); // FUTURE make "flush" part of the interface
		}
	}

	private static void flushPageSourcePool(Collection<Mapping> mappings) {
		Iterator<Mapping> it = mappings.iterator();
		Mapping m;
		while (it.hasNext()) {
			m = it.next();
			if (m instanceof MappingImpl) ((MappingImpl) m).flush(); // FUTURE make "flush" part of the interface
		}
	}

	private static void _getDotNotationUpperCase(StringBuilder sb, Mapping... mappings) {
		for (int i = 0; i < mappings.length; i++) {
			sb.append(((MappingImpl) mappings[i]).getDotNotationUpperCase()).append(';');
		}
	}

	private static void _getDotNotationUpperCase(StringBuilder sb, Collection<Mapping> mappings) {
		Iterator<Mapping> it = mappings.iterator();
		Mapping m;
		while (it.hasNext()) {
			m = it.next();
			sb.append(((MappingImpl) m).getDotNotationUpperCase()).append(';');
		}
	}

	/**
	 * load mappings from XML Document
	 * 
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws IOException
	 */
	private static void _loadMappings(ConfigServerImpl configServer, ConfigImpl config, Document doc, int mode, Log log) throws IOException {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAPPING);
			Element el = doc != null ? getChildByName(doc.getDocumentElement(), "mappings") : null;
			Element[] _mappings = el != null ? getChildren(el, "mapping") : null;

			Map<String, Mapping> mappings = MapFactory.<String, Mapping>getConcurrentMap();
			Mapping tmp;

			boolean finished = false;

			if (configServer != null && config instanceof ConfigWeb) {
				Mapping[] sm = configServer.getMappings();
				if (sm != null) {
					for (int i = 0; i < sm.length; i++) {
						if (!sm[i].isHidden()) {
							if ("/".equals(sm[i].getVirtual())) finished = true;
							if (sm[i] instanceof MappingImpl) {
								tmp = ((MappingImpl) sm[i]).cloneReadOnly(config);
								mappings.put(tmp.getVirtualLowerCase(), tmp);

							}
							else {
								tmp = sm[i];
								mappings.put(tmp.getVirtualLowerCase(), tmp);
							}
						}
					}
				}
			}

			if (hasAccess) {
				boolean hasServerContext = false;
				if (_mappings != null) {
					for (int i = 0; i < _mappings.length; i++) {
						el = _mappings[i];

						String physical = el.getAttribute("physical");
						String archive = el.getAttribute("archive");
						String virtual = getAttr(el, "virtual");
						String listType = getAttr(el, "listener-type");
						String listMode = getAttr(el, "listener-mode");

						boolean readonly = toBoolean(getAttr(el, "readonly"), false);
						boolean hidden = toBoolean(getAttr(el, "hidden"), false);
						boolean toplevel = toBoolean(getAttr(el, "toplevel"), true);

						if (config instanceof ConfigServer && (virtual.equalsIgnoreCase("/lucee-server/") || virtual.equalsIgnoreCase("/lucee-server-context/"))) {
							hasServerContext = true;
						}

						// lucee
						if (virtual.equalsIgnoreCase("/lucee/")) {
							if (StringUtil.isEmpty(listType, true)) listType = "modern";
							if (StringUtil.isEmpty(listMode, true)) listMode = "curr2root";
							toplevel = true;
						}

						int listenerMode = ConfigWebUtil.toListenerMode(listMode, -1);
						int listenerType = ConfigWebUtil.toListenerType(listType, -1);
						ApplicationListener listener = ConfigWebUtil.loadListener(listenerType, null);
						if (listener != null || listenerMode != -1) {
							// type
							if (mode == ConfigPro.MODE_STRICT) listener = new ModernAppListener();
							else if (listener == null) listener = ConfigWebUtil.loadListener(ConfigWebUtil.toListenerType(config.getApplicationListener().getType(), -1), null);
							if (listener == null)// this should never be true
								listener = new ModernAppListener();

							// mode
							if (listenerMode == -1) {
								listenerMode = config.getApplicationListener().getMode();
							}
							listener.setMode(listenerMode);

						}

						// physical!=null &&
						if ((physical != null || archive != null)) {

							short insTemp = inspectTemplate(el);
							if ("/lucee/".equalsIgnoreCase(virtual) || "/lucee".equalsIgnoreCase(virtual) || "/lucee-server/".equalsIgnoreCase(virtual)
									|| "/lucee-server-context".equalsIgnoreCase(virtual))
								insTemp = ConfigPro.INSPECT_ONCE;

							String primary = getAttr(el, "primary");
							boolean physicalFirst = primary == null || !primary.equalsIgnoreCase("archive");

							tmp = new MappingImpl(config, virtual, physical, archive, insTemp, physicalFirst, hidden, readonly, toplevel, false, false, listener, listenerMode,
									listenerType);
							mappings.put(tmp.getVirtualLowerCase(), tmp);
							if (virtual.equals("/")) {
								finished = true;
								// break;
							}
						}
					}
				}

				// set default lucee-server-context
				if (config instanceof ConfigServer && !hasServerContext) {
					ApplicationListener listener = ConfigWebUtil.loadListener(ApplicationListener.TYPE_MODERN, null);
					listener.setMode(ApplicationListener.MODE_CURRENT2ROOT);

					tmp = new MappingImpl(config, "/lucee-server", "{lucee-server}/context/", null, ConfigPro.INSPECT_ONCE, true, false, true, true, false, false, listener,
							ApplicationListener.MODE_CURRENT2ROOT, ApplicationListener.TYPE_MODERN);
					mappings.put(tmp.getVirtualLowerCase(), tmp);
				}
			}

			if (!finished) {

				if ((config instanceof ConfigWebImpl) && ResourceUtil.isUNCPath(config.getRootDirectory().getPath())) {

					tmp = new MappingImpl(config, "/", config.getRootDirectory().getPath(), null, ConfigPro.INSPECT_UNDEFINED, true, true, true, true, false, false, null, -1, -1);
				}
				else {

					tmp = new MappingImpl(config, "/", "/", null, ConfigPro.INSPECT_UNDEFINED, true, true, true, true, false, false, null, -1, -1);
				}

				mappings.put("/", tmp);
			}

			Mapping[] arrMapping = new Mapping[mappings.size()];
			int index = 0;
			Iterator it = mappings.keySet().iterator();
			while (it.hasNext()) {
				arrMapping[index++] = mappings.get(it.next());
			}
			config.setMappings(arrMapping);
			// config.setMappings((Mapping[]) mappings.toArray(new
			// Mapping[mappings.size()]));
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static short inspectTemplate(Element el) {
		String strInsTemp = getAttr(el, "inspect-template");
		if (StringUtil.isEmpty(strInsTemp)) strInsTemp = getAttr(el, "inspect");
		if (StringUtil.isEmpty(strInsTemp)) {
			Boolean trusted = Caster.toBoolean(getAttr(el, "trusted"), null);
			if (trusted != null) {
				if (trusted.booleanValue()) return ConfigPro.INSPECT_NEVER;
				return ConfigPro.INSPECT_ALWAYS;
			}
			return ConfigPro.INSPECT_UNDEFINED;
		}
		return ConfigWebUtil.inspectTemplate(strInsTemp, ConfigPro.INSPECT_UNDEFINED);
	}

	private static void _loadRest(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasAccess = true;// MUST
			// ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST);
			boolean hasCS = configServer != null;
			Element el = doc != null ? getChildByName(doc.getDocumentElement(), "rest") : null;

			// list
			Boolean list = el != null ? Caster.toBoolean(getAttr(el, "list"), null) : null;
			if (list != null) {
				config.setRestList(list.booleanValue());
			}
			else if (hasCS) {
				config.setRestList(configServer.getRestList());
			}

			Element[] _mappings = el != null ? getChildren(el, "mapping") : null;

			// first get mapping defined in server admin (read-only)
			Map<String, lucee.runtime.rest.Mapping> mappings = new HashMap<String, lucee.runtime.rest.Mapping>();
			lucee.runtime.rest.Mapping tmp;
			if (configServer != null && config instanceof ConfigWeb) {
				lucee.runtime.rest.Mapping[] sm = configServer.getRestMappings();
				if (sm != null) {
					for (int i = 0; i < sm.length; i++) {

						if (!sm[i].isHidden()) {
							tmp = sm[i].duplicate(config, Boolean.TRUE);
							mappings.put(tmp.getVirtual(), tmp);
						}
					}
				}
			}

			// get current mappings
			if (hasAccess && _mappings != null) {
				for (int i = 0; i < _mappings.length; i++) {
					el = _mappings[i];
					String physical = el.getAttribute("physical");
					String virtual = getAttr(el, "virtual");
					boolean readonly = toBoolean(getAttr(el, "readonly"), false);
					boolean hidden = toBoolean(getAttr(el, "hidden"), false);
					boolean _default = toBoolean(getAttr(el, "default"), false);
					if (physical != null) {
						tmp = new lucee.runtime.rest.Mapping(config, virtual, physical, hidden, readonly, _default);
						mappings.put(tmp.getVirtual(), tmp);
					}
				}
			}

			config.setRestMappings(mappings.values().toArray(new lucee.runtime.rest.Mapping[mappings.size()]));
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadFlex(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element el = doc != null ? getChildByName(doc.getDocumentElement(), "flex") : null;

			// engine - we init an engine for every context, but only the server context defines the engine
			// class
			if (config instanceof ConfigServerImpl && doc != null) { // only server context

				// arguments
				Map<String, String> args = new HashMap<String, String>();
				String _caster = getAttr(el, "caster");
				if (_caster != null) args.put("caster", _caster);
				String _config = getAttr(el, "configuration");
				if (_config != null) args.put("configuration", _config);

				ClassDefinition<AMFEngine> cd = getClassDefinition(el, "", config.getIdentification());
				if (cd.hasClass()) ((ConfigServerImpl) config).setAMFEngine(cd, args);
			}
			else if (configServer != null && configServer.getAMFEngineClassDefinition() != null && configServer.getAMFEngineClassDefinition().hasClass()) { // only web contexts
				AMFEngine engine = toAMFEngine(config, configServer.getAMFEngineClassDefinition(), null);
				if (engine != null) {
					engine.init((ConfigWeb) config, configServer.getAMFEngineArgs());
					((ConfigWebImpl) config).setAMFEngine(engine);
				}
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static AMFEngine toAMFEngine(Config config, ClassDefinition<AMFEngine> cd, AMFEngine defaultValue) {
		Log log = config.getLog("application");
		try {
			Class<AMFEngine> clazz = cd.getClazz(null);
			if (clazz != null) {
				Object obj = ClassUtil.newInstance(clazz);
				if ((obj instanceof AMFEngine)) return (AMFEngine) obj;
				log.error("Flex", "object [" + Caster.toClassName(obj) + "] must implement the interface " + AMFEngine.class.getName());
			}
		}
		catch (Exception e) {
			log.error("Flex", e);
		}
		return defaultValue;
	}

	private static void _loadLoggers(ConfigServerImpl configServer, ConfigImpl config, Document doc, boolean isReload) {
		try {
			config.clearLoggers(Boolean.FALSE);
			Element parent = doc != null ? getChildByName(doc.getDocumentElement(), "logging") : null;
			Element[] children = parent != null ? getChildren(parent, "logger") : new Element[0];
			Element child;
			String name, appenderArgs, tmp, layoutArgs;
			ClassDefinition cdAppender, cdLayout;
			int level = Log.LEVEL_ERROR;
			boolean readOnly = false;
			for (int i = 0; i < children.length; i++) {
				child = children[i];
				name = StringUtil.trim(getAttr(child, "name"), "");

				// appender
				cdAppender = getClassDefinition(child, "appender-", config.getIdentification());
				if (!cdAppender.hasClass()) {
					tmp = StringUtil.trim(getAttr(child, "appender"), "");
					cdAppender = config.getLogEngine().appenderClassDefintion(tmp);
				}
				appenderArgs = StringUtil.trim(getAttr(child, "appender-arguments"), "");

				// layout
				cdLayout = getClassDefinition(child, "layout-", config.getIdentification());
				if (!cdLayout.hasClass()) {
					tmp = StringUtil.trim(getAttr(child, "layout"), "");
					cdLayout = config.getLogEngine().layoutClassDefintion(tmp);
				}
				layoutArgs = StringUtil.trim(getAttr(child, "layout-arguments"), "");

				String strLevel = getAttr(child, "level");
				if (StringUtil.isEmpty(strLevel, true)) strLevel = getAttr(child, "log-level");
				level = LogUtil.toLevel(StringUtil.trim(strLevel, ""), Log.LEVEL_ERROR);
				readOnly = Caster.toBooleanValue(getAttr(child, "read-only"), false);
				// ignore when no appender/name is defined
				if (cdAppender.hasClass() && !StringUtil.isEmpty(name)) {
					Map<String, String> appArgs = cssStringToMap(appenderArgs, true, true);
					if (cdLayout.hasClass()) {
						Map<String, String> layArgs = cssStringToMap(layoutArgs, true, true);
						config.addLogger(name, level, cdAppender, appArgs, cdLayout, layArgs, readOnly, false);
					}
					else config.addLogger(name, level, cdAppender, appArgs, null, null, readOnly, false);
				}
			}

			if (configServer != null) {
				Iterator<Entry<String, LoggerAndSourceData>> it = configServer.getLoggers().entrySet().iterator();
				Entry<String, LoggerAndSourceData> e;
				LoggerAndSourceData data;
				while (it.hasNext()) {
					e = it.next();

					// logger only exists in server context
					if (config.getLog(e.getKey(), false) == null) {
						data = e.getValue();
						config.addLogger(e.getKey(), data.getLevel(), data.getAppenderClassDefinition(), data.getAppenderArgs(), data.getLayoutClassDefinition(),
								data.getLayoutArgs(), true, false);
					}
				}
			}
		}
		catch (Exception e) {
			log(config, null, e);
		}
	}

	private static void _loadExeLog(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasServer = configServer != null;

			Element el = doc != null ? getChildByName(doc.getDocumentElement(), "execution-log") : null;

			// enabled
			Boolean bEnabled = Caster.toBoolean(getAttr(el, "enabled"), null);
			if (bEnabled == null) {
				if (hasServer) config.setExecutionLogEnabled(configServer.getExecutionLogEnabled());
			}
			else config.setExecutionLogEnabled(bEnabled.booleanValue());

			boolean hasChanged = false;
			String val = Caster.toString(config.getExecutionLogEnabled());
			try {
				Resource contextDir = config.getConfigDir();
				Resource exeLog = contextDir.getRealResource("exe-log");

				if (!exeLog.exists()) {
					exeLog.createNewFile();
					IOUtil.write(exeLog, val, SystemUtil.getCharset(), false);
					hasChanged = true;
				}
				else if (!IOUtil.toString(exeLog, SystemUtil.getCharset()).equals(val)) {
					IOUtil.write(exeLog, val, SystemUtil.getCharset(), false);
					hasChanged = true;
				}
			}
			catch (IOException e) {
				e.printStackTrace(config.getErrWriter());
			}

			if (hasChanged) {
				try {
					if (config.getClassDirectory().exists()) config.getClassDirectory().remove(true);
				}
				catch (IOException e) {
					e.printStackTrace(config.getErrWriter());
				}
			}

			// class
			String strClass = getAttr(el, "class");
			Class clazz;
			if (!StringUtil.isEmpty(strClass)) {
				try {
					if ("console".equalsIgnoreCase(strClass)) clazz = ConsoleExecutionLog.class;
					else {
						ClassDefinition cd = el != null ? getClassDefinition(el, "", config.getIdentification()) : null;

						Class c = cd != null ? cd.getClazz() : null;
						if (c != null && (ClassUtil.newInstance(c) instanceof ExecutionLog)) {
							clazz = c;
						}
						else {
							clazz = ConsoleExecutionLog.class;
							LogUtil.logGlobal(configServer == null ? config : configServer, Log.LEVEL_ERROR, XMLConfigWebFactory.class.getName(),
									"class [" + strClass + "] must implement the interface " + ExecutionLog.class.getName());
						}
					}
				}
				catch (Exception e) {
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), XMLConfigWebFactory.class.getName(), e);
					clazz = ConsoleExecutionLog.class;
				}
				if (clazz != null) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_INFO,
						XMLConfigWebFactory.class.getName(), "loaded ExecutionLog class " + clazz.getName());

				// arguments
				String strArgs = getAttr(el, "arguments");
				if (StringUtil.isEmpty(strArgs)) strArgs = getAttr(el, "class-arguments");
				Map<String, String> args = toArguments(strArgs, true);

				config.setExecutionLogFactory(new ExecutionLogFactory(clazz, args));
			}
			else {
				if (hasServer) config.setExecutionLogFactory(configServer.getExecutionLogFactory());
				else config.setExecutionLogFactory(new ExecutionLogFactory(ConsoleExecutionLog.class, new HashMap<String, String>()));
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * loads and sets the Page Pool
	 * 
	 * @param configServer
	 * @param config
	 * @param doc
	 */
	private static void _loadPagePool(ConfigServer configServer, Config config, Document doc, Log log) {
		// TODO xml configuration fuer das erstellen
		// config.setPagePool( new PagePool(10000,1000));
	}

	/**
	 * loads datasource settings from XMl DOM
	 * 
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws BundleException
	 * @throws ClassNotFoundException
	 */
	private static void _loadDataSources(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			// load JDBC Driver definition
			config.setJDBCDrivers(_loadJDBCDrivers(configServer, config, doc, log));

			// When set to true, makes JDBC use a representation for DATE data that
			// is compatible with the Oracle8i database.
			System.setProperty("oracle.jdbc.V8Compatible", "true");

			boolean hasCS = configServer != null;
			Map<String, DataSource> datasources = new HashMap<String, DataSource>();

			// Copy Parent datasources as readOnly
			if (hasCS) {
				Map<String, DataSource> ds = configServer.getDataSourcesAsMap();
				Iterator<Entry<String, DataSource>> it = ds.entrySet().iterator();
				Entry<String, DataSource> entry;
				while (it.hasNext()) {
					entry = it.next();
					if (!entry.getKey().equals(QOQ_DATASOURCE_NAME)) datasources.put(entry.getKey(), entry.getValue().cloneReadOnly());
				}
			}

			// TODO support H2
			// Default query of query DB
			/*
			 * setDatasource(datasources, QOQ_DATASOURCE_NAME, "org.h2.Driver" ,"" ,"" ,-1
			 * ,"jdbc:h2:.;MODE=HSQLDB" ,"sa" ,"" ,-1 ,-1 ,true ,true ,DataSource.ALLOW_ALL, new StructImpl() );
			 */
			// Default query of query DB
			try {
				setDatasource(config, datasources, QOQ_DATASOURCE_NAME, new ClassDefinitionImpl("org.hsqldb.jdbcDriver", "hsqldb", "1.8.0", config.getIdentification()),
						"hypersonic-hsqldb", "", -1, "jdbc:hsqldb:.", "sa", "", null, DEFAULT_MAX_CONNECTION, -1, -1, 60000, true, true, DataSource.ALLOW_ALL, false, false, null,
						new StructImpl(), "", ParamSyntax.DEFAULT, false, false, false, false);
			}
			catch (Exception e) {
				log.error("Datasource", e);
			}

			SecurityManager sm = config.getSecurityManager();
			short access = sm.getAccess(SecurityManager.TYPE_DATASOURCE);
			int accessCount = -1;
			if (access == SecurityManager.VALUE_YES) accessCount = -1;
			else if (access == SecurityManager.VALUE_NO) accessCount = 0;
			else if (access >= SecurityManager.VALUE_1 && access <= SecurityManager.VALUE_10) {
				accessCount = access - SecurityManager.NUMBER_OFFSET;
			}

			// Databases
			Element databases = doc != null ? getChildByName(doc.getDocumentElement(), "data-sources") : null;
			// if(databases==null)databases=doc.createElement("data-sources");

			// PSQ
			String strPSQ = databases != null ? getAttr(databases, "psq") : null;
			if (databases != null && StringUtil.isEmpty(strPSQ)) {
				// prior version was buggy, was the opposite
				strPSQ = getAttr(databases, "preserve-single-quote");
				if (!StringUtil.isEmpty(strPSQ)) {
					Boolean b = Caster.toBoolean(strPSQ, null);
					if (b != null) strPSQ = b.booleanValue() ? "false" : "true";
				}
			}
			if (access != SecurityManager.VALUE_NO && !StringUtil.isEmpty(strPSQ)) {
				config.setPSQL(toBoolean(strPSQ, true));
			}
			else if (hasCS) config.setPSQL(configServer.getPSQL());

			// Data Sources
			Element[] dataSources = databases != null ? getChildren(databases, "data-source") : new Element[0];
			if (accessCount == -1) accessCount = dataSources.length;
			if (dataSources.length < accessCount) accessCount = dataSources.length;

			// if(hasAccess) {
			JDBCDriver jdbc;
			ClassDefinition cd;
			String id;
			for (int i = 0; i < accessCount; i++) {
				Element dataSource = dataSources[i];
				if (dataSource.hasAttribute("database")) {
					try {
						// do we have an id?
						jdbc = config.getJDBCDriverById(getAttr(dataSource, "id"), null);
						if (jdbc != null && jdbc.cd != null) {
							cd = jdbc.cd;
						}
						else cd = getClassDefinition(dataSource, "", config.getIdentification());

						// we only have a class
						if (!cd.isBundle()) {
							jdbc = config.getJDBCDriverByClassName(cd.getClassName(), null);
							if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) cd = jdbc.cd;
						}

						// still no bundle!
						if (!cd.isBundle()) cd = patchJDBCClass(config, cd);
						int idle = Caster.toIntValue(getAttr(dataSource, "idleTimeout"), -1);
						if (idle == -1) idle = Caster.toIntValue(getAttr(dataSource, "connectionTimeout"), -1);
						int defLive = 60;
						if (idle > 0) defLive = idle * 5;// for backward compatibility

						setDatasource(config, datasources, getAttr(dataSource, "name"), cd, getAttr(dataSource, "host"), getAttr(dataSource, "database"),
								Caster.toIntValue(getAttr(dataSource, "port"), -1), getAttr(dataSource, "dsn"), getAttr(dataSource, "username"),
								ConfigWebUtil.decrypt(getAttr(dataSource, "password")), null, Caster.toIntValue(getAttr(dataSource, "connectionLimit"), DEFAULT_MAX_CONNECTION),
								idle, Caster.toIntValue(getAttr(dataSource, "liveTimeout"), defLive), Caster.toLongValue(getAttr(dataSource, "metaCacheTimeout"), 60000),
								toBoolean(getAttr(dataSource, "blob"), true), toBoolean(getAttr(dataSource, "clob"), true),
								Caster.toIntValue(getAttr(dataSource, "allow"), DataSource.ALLOW_ALL), toBoolean(getAttr(dataSource, "validate"), false),
								toBoolean(getAttr(dataSource, "storage"), false), getAttr(dataSource, "timezone"), toStruct(getAttr(dataSource, "custom")),
								getAttr(dataSource, "dbdriver"), ParamSyntax.toParamSyntax(dataSource, ParamSyntax.DEFAULT),
								toBoolean(getAttr(dataSource, "literal-timestamp-with-tsoffset"), false), toBoolean(getAttr(dataSource, "always-set-timeout"), false),
								toBoolean(getAttr(dataSource, "request-exclusive"), false), toBoolean(getAttr(dataSource, "always-reset-connections"), false)

						);
					}
					catch (Exception e) {
						log.error("Datasource", e);
					}
				}
			}
			// }
			config.setDataSources(datasources);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static ClassDefinition patchJDBCClass(ConfigImpl config, ClassDefinition cd) {
		// PATCH for MySQL driver that did change the className within the same extension, JDBC extension
		// expect that the className does not change.
		if ("org.gjt.mm.mysql.Driver".equals(cd.getClassName()) || "com.mysql.jdbc.Driver".equals(cd.getClassName()) || "com.mysql.cj.jdbc.Driver".equals(cd.getClassName())) {
			JDBCDriver jdbc = config.getJDBCDriverById("mysql", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("com.mysql.cj.jdbc.Driver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("com.mysql.jdbc.Driver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("org.gjt.mm.mysql.Driver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			ClassDefinitionImpl tmp = new ClassDefinitionImpl("com.mysql.cj.jdbc.Driver", "com.mysql.cj", null, config.getIdentification());
			if (tmp.getClazz(null) != null) return tmp;

			tmp = new ClassDefinitionImpl("com.mysql.jdbc.Driver", "com.mysql.jdbc", null, config.getIdentification());
			if (tmp.getClazz(null) != null) return tmp;
		}
		if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cd.getClassName())) {
			JDBCDriver jdbc = config.getJDBCDriverById("mssql", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			jdbc = config.getJDBCDriverByClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver", null);
			if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) return jdbc.cd;

			ClassDefinitionImpl tmp = new ClassDefinitionImpl("com.microsoft.sqlserver.jdbc.SQLServerDriver", cd.getName(), cd.getVersionAsString(), config.getIdentification());
			if (tmp.getClazz(null) != null) return tmp;
		}

		return cd;
	}

	public static JDBCDriver[] _loadJDBCDrivers(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		Map<String, JDBCDriver> map = new HashMap<String, JDBCDriver>();

		// first add the server drivers, so they can be overwritten
		if (configServer != null) {
			JDBCDriver[] sds = configServer.getJDBCDrivers();
			if (sds != null) {
				for (JDBCDriver sd: sds) {
					map.put(sd.cd.toString(), sd);
				}
			}
		}

		Element jdbc = doc != null ? getChildByName(doc.getDocumentElement(), "jdbc") : null;
		Element[] drivers = jdbc != null ? getChildren(jdbc, "driver") : new Element[0];

		ClassDefinition cd;
		String label, id, connStr;
		for (Element driver: drivers) {
			cd = getClassDefinition(driver, "", config.getIdentification());
			if (StringUtil.isEmpty(cd.getClassName()) && !StringUtil.isEmpty(cd.getName())) {
				try {
					Bundle bundle = OSGiUtil.loadBundle(cd.getName(), cd.getVersion(), config.getIdentification(), null, false);
					String cn = JDBCDriver.extractClassName(bundle);
					cd = new ClassDefinitionImpl(config.getIdentification(), cn, cd.getName(), cd.getVersion());
				}
				catch (Exception e) {
				}
			}

			label = getAttr(driver, "label");
			id = getAttr(driver, "id");
			connStr = getAttr(driver, "connectionString");
			// check if label exists
			if (StringUtil.isEmpty(label)) {
				if (log != null) log.error("Datasource", "missing label for jdbc driver [" + cd.getClassName() + "]");
				continue;
			}

			// check if it is a bundle
			if (!cd.isBundle()) {
				if (log != null) log.error("Datasource", "jdbc driver [" + label + "] does not describe a bundle");
				continue;
			}
			map.put(cd.toString(), new JDBCDriver(label, id, connStr, cd));
		}
		return map.values().toArray(new JDBCDriver[map.size()]);
	}

	/*
	 * private static ClassDefinition matchJDBCBundle(Config config, ClassDefinition cd) {
	 * if(!cd.isBundle()) { if("org.hsqldb.jdbcDriver".equals(cd.getClassName())) return new
	 * ClassDefinitionImpl<>(cd.getClassName(), "hypersonic.hsqldb", null, config.getIdentification());
	 * if("org.gjt.mm.mysql.Driver".equals(cd.getClassName())) return new
	 * ClassDefinitionImpl<>(cd.getClassName(), "com.mysql.jdbc", null, config.getIdentification());
	 * if("org.h2.Driver".equals(cd.getClassName())) return new ClassDefinitionImpl<>(cd.getClassName(),
	 * "org.h2", null, config.getIdentification());
	 * if("net.sourceforge.jtds.jdbc.Driver".equals(cd.getClassName())) return new
	 * ClassDefinitionImpl<>(cd.getClassName(), "jtds", null, config.getIdentification());
	 * if("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cd.getClassName())) return new
	 * ClassDefinitionImpl<>(cd.getClassName(), "microsoft.sqljdbc", null, config.getIdentification());
	 * 
	 * } return cd; }
	 */

	private static void _loadCache(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasCS = configServer != null;

			// load cache defintions
			{
				Element parent = doc != null ? getChildByName(doc.getDocumentElement(), "caches") : null;
				Element[] children = parent != null ? getChildren(parent, "cache") : new Element[0];
				Map<String, ClassDefinition> map = new HashMap<String, ClassDefinition>();

				// first add the server drivers, so they can be overwritten
				if (configServer != null) {
					Iterator<ClassDefinition> it = configServer.getCacheDefinitions().values().iterator();
					ClassDefinition cd;
					while (it.hasNext()) {
						cd = it.next();
						map.put(cd.getClassName(), cd);
					}
				}

				ClassDefinition cd;
				for (Element child: children) {
					cd = getClassDefinition(child, "", config.getIdentification());

					// check if it is a bundle
					if (!cd.isBundle()) {
						log.error("Cache", "[" + cd + "] does not have bundle info");
						continue;
					}
					map.put(cd.getClassName(), cd);
				}
				config.setCacheDefinitions(map);
			}

			Map<String, CacheConnection> caches = new HashMap<String, CacheConnection>();

			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE);
			Element eCache = doc != null ? getChildByName(doc.getDocumentElement(), "cache") : null;

			// check if we have an update or not
			StringBuilder sb = new StringBuilder();
			for (Entry<String, ClassDefinition> e: config.getCacheDefinitions().entrySet()) {
				sb.append(e.getKey()).append(':').append(e.getValue().toString()).append(';');
			}
			String md5 = eCache != null ? getMD5(eCache, sb.toString(), hasCS ? configServer.getCacheMD5() : "") : "";
			if (md5.equals(config.getCacheMD5())) {
				return;
			}
			config.setCacheMD5(md5);

			String[] typeNames = new String[] { "resource", "function", "include", "query", "template", "object", "file", "http", "webservice" };
			int[] types = new int[] { ConfigPro.CACHE_TYPE_RESOURCE, ConfigPro.CACHE_TYPE_FUNCTION, ConfigPro.CACHE_TYPE_INCLUDE, ConfigPro.CACHE_TYPE_QUERY,
					ConfigPro.CACHE_TYPE_TEMPLATE, ConfigPro.CACHE_TYPE_OBJECT, ConfigPro.CACHE_TYPE_FILE, ConfigPro.CACHE_TYPE_HTTP, ConfigPro.CACHE_TYPE_WEBSERVICE };

			// default cache
			for (int i = 0; i < types.length; i++) {
				String def = eCache != null ? getAttr(eCache, "default-" + typeNames[i]) : null;
				if (hasAccess && !StringUtil.isEmpty(def)) {
					config.setCacheDefaultConnectionName(types[i], def);
				}
				else if (hasCS) {
					if (eCache != null && eCache.hasAttribute("default-" + typeNames[i])) config.setCacheDefaultConnectionName(types[i], "");
					else config.setCacheDefaultConnectionName(types[i], configServer.getCacheDefaultConnectionName(types[i]));
				}
				else config.setCacheDefaultConnectionName(+types[i], "");
			}

			// cache connections
			Element[] eConnections = eCache != null ? getChildren(eCache, "connection") : new Element[0];

			// if(hasAccess) {
			ClassDefinition cd;
			String name;
			CacheConnection cc;
			// Class cacheClazz;
			// caches
			if (hasAccess) for (int i = 0; i < eConnections.length; i++) {
				Element eConnection = eConnections[i];
				name = getAttr(eConnection, "name");
				cd = getClassDefinition(eConnection, "", config.getIdentification());
				if (!cd.isBundle()) {
					ClassDefinition _cd = config.getCacheDefinition(cd.getClassName());
					if (_cd != null) cd = _cd;
				}

				{
					Struct custom = toStruct(getAttr(eConnection, "custom"));

					// Workaround for old EHCache class definitions
					if (cd.getClassName() != null && cd.getClassName().endsWith(".EHCacheLite")) {
						cd = new ClassDefinitionImpl("org.lucee.extension.cache.eh.EHCache");
						if (!custom.containsKey("distributed")) custom.setEL("distributed", "off");
						if (!custom.containsKey("asynchronousReplicationIntervalMillis")) custom.setEL("asynchronousReplicationIntervalMillis", "1000");
						if (!custom.containsKey("maximumChunkSizeBytes")) custom.setEL("maximumChunkSizeBytes", "5000000");

					} //
					else if (cd.getClassName() != null
							&& (cd.getClassName().endsWith(".extension.io.cache.eh.EHCache") || cd.getClassName().endsWith("lucee.runtime.cache.eh.EHCache"))) {
								cd = new ClassDefinitionImpl("org.lucee.extension.cache.eh.EHCache");
							}
					cc = new CacheConnectionImpl(config, name, cd, custom, Caster.toBooleanValue(getAttr(eConnection, "read-only"), false),
							Caster.toBooleanValue(getAttr(eConnection, "storage"), false));
					if (!StringUtil.isEmpty(name)) {
						caches.put(name.toLowerCase(), cc);
					}
					else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_ERROR, XMLConfigWebFactory.class.getName(),
							"missing cache name");

				}

			}
			// }

			// call static init once per driver
			{
				// group by classes
				final Map<ClassDefinition, List<CacheConnection>> _caches = new HashMap<ClassDefinition, List<CacheConnection>>();
				{
					Iterator<Entry<String, CacheConnection>> it = caches.entrySet().iterator();
					Entry<String, CacheConnection> entry;
					List<CacheConnection> list;
					while (it.hasNext()) {
						entry = it.next();
						cc = entry.getValue();
						if (cc == null) continue;// Jira 3196 ?!
						list = _caches.get(cc.getClassDefinition());
						if (list == null) {
							list = new ArrayList<CacheConnection>();
							_caches.put(cc.getClassDefinition(), list);
						}
						list.add(cc);
					}
				}
				// call
				Iterator<Entry<ClassDefinition, List<CacheConnection>>> it = _caches.entrySet().iterator();
				Entry<ClassDefinition, List<CacheConnection>> entry;
				List<CacheConnection> list;
				ClassDefinition _cd;
				while (it.hasNext()) {
					entry = it.next();
					list = entry.getValue();
					_cd = entry.getKey();
					try {
						Method m = _cd.getClazz().getMethod("init", new Class[] { Config.class, String[].class, Struct[].class });
						if (Modifier.isStatic(m.getModifiers())) m.invoke(null, new Object[] { config, _toCacheNames(list), _toArguments(list) });
						else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_ERROR, XMLConfigWebFactory.class.getName(),
								"method [init(Config,String[],Struct[]):void] for class [" + _cd.toString() + "] is not static");

					}
					catch (InvocationTargetException e) {
						log.error("Cache", e.getTargetException());
					}
					catch (RuntimeException e) {
						log.error("Cache", e);
					}
					catch (NoSuchMethodException e) {
						log.error("Cache", "missing method [public static init(Config,String[],Struct[]):void] for class [" + _cd.toString() + "] ");
					}
					catch (Throwable e) {
						ExceptionUtil.rethrowIfNecessary(e);
						log.error("Cache", e);
					}
				}
			}

			// Copy Parent caches as readOnly
			if (hasCS) {
				Map<String, CacheConnection> ds = configServer.getCacheConnections();
				Iterator<Entry<String, CacheConnection>> it = ds.entrySet().iterator();
				Entry<String, CacheConnection> entry;
				while (it.hasNext()) {
					entry = it.next();
					cc = entry.getValue();
					if (!caches.containsKey(entry.getKey())) caches.put(entry.getKey(), new ServerCacheConnection(configServer, cc));
				}
			}
			config.setCaches(caches);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static String getMD5(Node node, String cacheDef, String parentMD5) {

		try {
			return MD5.getDigestAsString(new StringBuilder().append(XMLCaster.toString(node, "")).append(':').append(cacheDef).append(':').append(parentMD5).toString());
		}
		catch (IOException e) {
			return "";
		}
	}

	private static void _loadGatewayEL(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			_loadGateway(configServer, config, doc);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadGateway(ConfigServerImpl configServer, ConfigImpl config, Document doc) {
		boolean hasCS = configServer != null;

		GatewayEngineImpl engine = hasCS ? ((GatewayEngineImpl) ((ConfigWebPro) config).getGatewayEngine()) : null;
		Map<String, GatewayEntry> mapGateways = new HashMap<String, GatewayEntry>();

		// get from server context
		if (hasCS) {
			Map<String, GatewayEntry> entries = configServer.getGatewayEntries();
			if (entries != null && !entries.isEmpty()) {
				Iterator<Entry<String, GatewayEntry>> it = entries.entrySet().iterator();
				Entry<String, GatewayEntry> e;
				while (it.hasNext()) {
					e = it.next();
					mapGateways.put(e.getKey(), ((GatewayEntryImpl) e.getValue()).duplicateReadOnly(engine));
				}
			}
		}

		Element eGateWay = doc != null ? getChildByName(doc.getDocumentElement(), "gateways") : null;
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY);
		GatewayEntry ge;
		// cache connections
		Element[] gateways = eGateWay != null ? getChildren(eGateWay, "gateway") : new Element[0];

		// if(hasAccess) {
		String id;
		// engine.reset();

		// caches
		if (hasAccess) {
			for (int i = 0; i < gateways.length; i++) {
				Element eConnection = gateways[i];
				id = getAttr(eConnection, "id").trim().toLowerCase();

				ge = new GatewayEntryImpl(engine, id, getClassDefinition(eConnection, "", config.getIdentification()), eConnection.getAttribute("cfc-path"),
						eConnection.getAttribute("listener-cfc-path"), getAttr(eConnection, "startup-mode"), toStruct(getAttr(eConnection, "custom")),
						Caster.toBooleanValue(getAttr(eConnection, "read-only"), false));

				if (!StringUtil.isEmpty(id)) {
					mapGateways.put(id.toLowerCase(), ge);
				}
				else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_ERROR, XMLConfigWebFactory.class.getName(),
						"missing id");
			}
			config.setGatewayEntries(mapGateways);
		}
		else if (hasCS) {
			((GatewayEngineImpl) ((ConfigWebPro) config).getGatewayEngine()).clear();
		}
	}

	private static Struct[] _toArguments(List<CacheConnection> list) {
		Iterator<CacheConnection> it = list.iterator();
		Struct[] args = new Struct[list.size()];
		int index = 0;
		while (it.hasNext()) {
			args[index++] = it.next().getCustom();
		}
		return args;
	}

	private static String[] _toCacheNames(List<CacheConnection> list) {
		Iterator<CacheConnection> it = list.iterator();
		String[] names = new String[list.size()];
		int index = 0;
		while (it.hasNext()) {
			names[index++] = it.next().getName();
		}
		return names;
	}

	private static Struct toStruct(String str) {

		Struct sct = new StructImpl();
		try {
			String[] arr = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(str, '&'));

			String[] item;
			for (int i = 0; i < arr.length; i++) {
				item = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(arr[i], '='));
				if (item.length == 2) sct.setEL(KeyImpl.init(URLDecoder.decode(item[0], true).trim()), URLDecoder.decode(item[1], true));
				else if (item.length == 1) sct.setEL(KeyImpl.init(URLDecoder.decode(item[0], true).trim()), "");
			}
		}
		catch (PageException ee) {
		}

		return sct;
	}

	private static void setDatasource(ConfigImpl config, Map<String, DataSource> datasources, String datasourceName, ClassDefinition cd, String server, String databasename,
			int port, String dsn, String user, String pass, TagListener listener, int connectionLimit, int idleTimeout, int liveTimeout, long metaCacheTimeout, boolean blob,
			boolean clob, int allow, boolean validate, boolean storage, String timezone, Struct custom, String dbdriver, ParamSyntax ps, boolean literalTimestampWithTSOffset,
			boolean alwaysSetTimeout, boolean requestExclusive, boolean alwaysResetConnections) throws BundleException, ClassException, SQLException {

		datasources.put(datasourceName.toLowerCase(),
				new DataSourceImpl(config, datasourceName, cd, server, dsn, databasename, port, user, pass, listener, connectionLimit, idleTimeout, liveTimeout, metaCacheTimeout,
						blob, clob, allow, custom, false, validate, storage, StringUtil.isEmpty(timezone, true) ? null : TimeZoneUtil.toTimeZone(timezone, null), dbdriver, ps,
						literalTimestampWithTSOffset, alwaysSetTimeout, requestExclusive, alwaysResetConnections, config.getLog("application")));

	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws IOException
	 */
	private static void _loadCustomTagsMappings(ConfigServerImpl configServer, ConfigImpl config, Document doc, int mode, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG);
			boolean hasCS = configServer != null;

			Element customTag = doc != null ? getChildByName(doc.getDocumentElement(), "custom-tag") : null;
			Element[] ctMappings = customTag != null ? getChildren(customTag, "mapping") : new Element[0];
			// String virtualx="/custom-tag/";

			// do patch cache
			String strDoPathcache = customTag != null ? getAttr(customTag, "use-cache-path") : null;
			if (hasAccess && !StringUtil.isEmpty(strDoPathcache, true)) {
				config.setUseCTPathCache(Caster.toBooleanValue(strDoPathcache.trim(), true));
			}
			else if (hasCS) {
				config.setUseCTPathCache(configServer.useCTPathCache());
			}

			// do custom tag local search
			if (mode == ConfigPro.MODE_STRICT) {
				config.setDoLocalCustomTag(false);
			}
			else {
				String strDoCTLocalSearch = customTag != null ? getAttr(customTag, "custom-tag-local-search") : null;
				if (hasAccess && !StringUtil.isEmpty(strDoCTLocalSearch)) {
					config.setDoLocalCustomTag(Caster.toBooleanValue(strDoCTLocalSearch.trim(), true));
				}
				else if (hasCS) {
					config.setDoLocalCustomTag(configServer.doLocalCustomTag());
				}
			}

			// do custom tag deep search
			if (mode == ConfigPro.MODE_STRICT) {
				config.setDoCustomTagDeepSearch(false);
			}
			else {
				String strDoCTDeepSearch = customTag != null ? getAttr(customTag, "custom-tag-deep-search") : null;
				if (hasAccess && !StringUtil.isEmpty(strDoCTDeepSearch)) {
					config.setDoCustomTagDeepSearch(Caster.toBooleanValue(strDoCTDeepSearch.trim(), false));
				}
				else if (hasCS) {
					config.setDoCustomTagDeepSearch(configServer.doCustomTagDeepSearch());
				}
			}

			// extensions
			if (mode == ConfigPro.MODE_STRICT) {
				config.setCustomTagExtensions(Constants.getComponentExtensions());
			}
			else {
				String strExtensions = customTag != null ? getAttr(customTag, "extensions") : null;
				if (hasAccess && !StringUtil.isEmpty(strExtensions)) {
					try {
						String[] arr = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(strExtensions, ","));
						config.setCustomTagExtensions(ListUtil.trimItems(arr));
					}
					catch (PageException e) {
					}
				}
				else if (hasCS) {
					config.setCustomTagExtensions(configServer.getCustomTagExtensions());
				}
			}

			// Web Mapping
			boolean hasSet = false;
			Mapping[] mappings = null;
			if (hasAccess && ctMappings.length > 0) {
				mappings = new Mapping[ctMappings.length];
				for (int i = 0; i < ctMappings.length; i++) {
					Element ctMapping = ctMappings[i];
					String physical = ctMapping.getAttribute("physical");
					String archive = ctMapping.getAttribute("archive");
					boolean readonly = toBoolean(getAttr(ctMapping, "readonly"), false);
					boolean hidden = toBoolean(getAttr(ctMapping, "hidden"), false);
					// boolean trusted = toBoolean(getAttr(ctMapping,"trusted"), false);
					short inspTemp = inspectTemplate(ctMapping);
					// int clMaxEl = toInt(getAttr(ctMapping,"classloader-max-elements"), 100);

					String primary = getAttr(ctMapping, "primary");

					boolean physicalFirst = archive == null || !primary.equalsIgnoreCase("archive");
					hasSet = true;
					mappings[i] = new MappingImpl(config, XMLConfigAdmin.createVirtual(ctMapping), physical, archive, inspTemp, physicalFirst, hidden, readonly, true, false, true,
							null, -1, -1);
					// print.out(mappings[i].isPhysicalFirst());
				}

				config.setCustomTagMappings(mappings);

			}

			// Server Mapping
			if (hasCS) {
				Mapping[] originals = configServer.getCustomTagMappings();
				if (originals == null) originals = new Mapping[0];
				Mapping[] clones = new Mapping[originals.length];
				LinkedHashMap map = new LinkedHashMap();
				Mapping m;
				for (int i = 0; i < clones.length; i++) {
					m = ((MappingImpl) originals[i]).cloneReadOnly(config);
					map.put(toKey(m), m);
					// clones[i]=((MappingImpl)m[i]).cloneReadOnly(config);
				}

				if (mappings != null) {
					for (int i = 0; i < mappings.length; i++) {
						m = mappings[i];
						map.put(toKey(m), m);
					}
				}
				if (originals.length > 0) {
					clones = new Mapping[map.size()];
					Iterator it = map.entrySet().iterator();
					Map.Entry entry;
					int index = 0;
					while (it.hasNext()) {
						entry = (Entry) it.next();
						clones[index++] = (Mapping) entry.getValue();
						// print.out("c:"+clones[index-1]);
					}
					hasSet = true;
					// print.err("set:"+clones.length);

					config.setCustomTagMappings(clones);
				}
			}

			if (!hasSet) {
				// MappingImpl m=new
				// MappingImpl(config,"/default-customtags/","{lucee-web}/customtags/",null,false,true,false,false,true,false,true);
				// config.setCustomTagMappings(new
				// Mapping[]{m.cloneReadOnly(config)});
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}

	}

	private static Object toKey(Mapping m) {
		if (!StringUtil.isEmpty(m.getStrPhysical(), true)) return m.getStrPhysical().toLowerCase().trim();
		return (m.getStrPhysical() + ":" + m.getStrArchive()).toLowerCase();
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @param serverPW
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private static void _loadConfig(ConfigServerImpl configServer, ConfigImpl config, Document doc) {
		Element luceeConfiguration = doc != null ? doc.getDocumentElement() : null;
		String salt = null;
		Password pw = null;
		if (doc == null && configServer != null) {
			config.setPassword(configServer.getPassword());
			config.setSalt(configServer.getSalt());
		}
		else {
			// salt (every context need to have a salt)
			salt = getAttr(luceeConfiguration, "salt");
			if (StringUtil.isEmpty(salt, true)) throw new RuntimeException("context is invalid, there is no salt!");
			config.setSalt(salt = salt.trim());

			// password
			pw = PasswordImpl.readFromXML(luceeConfiguration, salt, false);
			if (pw != null) {
				config.setPassword(pw);
				if (config instanceof ConfigWebImpl) ((ConfigWebImpl) config).setPasswordSource(ConfigWebImpl.PASSWORD_ORIGIN_WEB);
			}
			else if (configServer != null) {
				((ConfigWebImpl) config).setPasswordSource(configServer.hasCustomDefaultPassword() ? ConfigWebImpl.PASSWORD_ORIGIN_DEFAULT : ConfigWebImpl.PASSWORD_ORIGIN_SERVER);
				config.setPassword(configServer.getDefaultPassword());
			}
		}

		if (config instanceof ConfigServerImpl) {
			ConfigServerImpl csi = (ConfigServerImpl) config;
			String keyList = getAttr(luceeConfiguration, "auth-keys");
			if (!StringUtil.isEmpty(keyList)) {
				String[] keys = ListUtil.trimItems(ListUtil.toStringArray(ListUtil.toListRemoveEmpty(keyList, ',')));
				for (int i = 0; i < keys.length; i++) {
					try {
						keys[i] = URLDecoder.decode(keys[i], "UTF-8", true);
					}
					catch (UnsupportedEncodingException e) {
					}
				}

				csi.setAuthenticationKeys(keys);
			}
		}

		// default password
		if (config instanceof ConfigServerImpl) {
			pw = PasswordImpl.readFromXML(luceeConfiguration, salt, true);
			if (pw != null) ((ConfigServerImpl) config).setDefaultPassword(pw);
		}

		// mode
		String mode = getAttr(luceeConfiguration, "mode");
		if (!StringUtil.isEmpty(mode, true)) {
			mode = mode.trim();
			if ("custom".equalsIgnoreCase(mode)) config.setMode(ConfigPro.MODE_CUSTOM);
			if ("strict".equalsIgnoreCase(mode)) config.setMode(ConfigPro.MODE_STRICT);
		}
		else if (configServer != null) {
			config.setMode(configServer.getMode());
		}

		// check config file for changes
		String cFc = getAttr(luceeConfiguration, "check-for-changes");
		if (!StringUtil.isEmpty(cFc, true)) {
			config.setCheckForChangesInConfigFile(Caster.toBooleanValue(cFc.trim(), false));
		}
		else if (configServer != null) {
			config.setCheckForChangesInConfigFile(configServer.checkForChangesInConfigFile());
		}
	}

	private static void _loadTag(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element parent = doc != null ? getChildByName(doc.getDocumentElement(), "tags") : null;
			{
				Element[] tags = parent != null ? getChildren(parent, "tag") : new Element[0];
				Element tag;
				ClassDefinition cd;
				String nss, ns, n;
				if (tags != null) {
					for (int i = 0; i < tags.length; i++) {
						tag = tags[i];
						ns = getAttr(tag, "namespace");
						nss = getAttr(tag, "namespace-seperator");
						n = getAttr(tag, "name");
						cd = getClassDefinition(tag, "", config.getIdentification());
						config.addTag(ns, nss, n, CFMLEngine.DIALECT_BOTH, cd);
					}
				}
			}

			// set tag default values
			Element[] defaults = parent != null ? getChildren(parent, "default") : new Element[0];
			if (!ArrayUtil.isEmpty(defaults)) {
				Element def;
				String tagName, attrName, attrValue;
				Struct tags = new StructImpl(), tag;
				Map<Key, Map<Key, Object>> trg = new HashMap<Key, Map<Key, Object>>();
				for (int i = 0; i < defaults.length; i++) {
					def = defaults[i];
					tagName = getAttr(def, "tag");
					attrName = getAttr(def, "attribute-name");
					attrValue = getAttr(def, "attribute-value");
					if (StringUtil.isEmpty(tagName) || StringUtil.isEmpty(attrName) || StringUtil.isEmpty(attrValue)) continue;

					tag = (Struct) tags.get(tagName, null);
					if (tag == null) {
						tag = new StructImpl();
						tags.setEL(tagName, tag);
					}
					tag.setEL(attrName, attrValue);
					ApplicationContextSupport.initTagDefaultAttributeValues(config, trg, tags, CFMLEngine.DIALECT_CFML);
					ApplicationContextSupport.initTagDefaultAttributeValues(config, trg, tags, CFMLEngine.DIALECT_LUCEE);
					config.setTagDefaultAttributeValues(trg);
				}

				// initTagDefaultAttributeValues

			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadTempDirectory(ConfigServerImpl configServer, ConfigImpl config, Document doc, boolean isReload, Log log) {
		try {
			if (configServer != null && doc == null) {
				config.setTempDirectory(configServer.getTempDirectory(), !isReload);
				return;
			}

			Resource configDir = config.getConfigDir();
			boolean hasCS = configServer != null;

			Element fileSystem = getChildByName(doc.getDocumentElement(), "file-system");
			if (fileSystem == null) fileSystem = getChildByName(doc.getDocumentElement(), "filesystem");

			String strTempDirectory = null;
			if (fileSystem != null) strTempDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("temp-directory"));

			Resource cst = null;
			// Temp Dir
			if (!StringUtil.isEmpty(strTempDirectory)) cst = ConfigWebUtil.getFile(configDir, strTempDirectory, null, configDir, FileUtil.TYPE_DIR, config);

			if (cst == null && hasCS) cst = configServer.getTempDirectory();

			if (cst == null) cst = ConfigWebUtil.getFile(configDir, "temp", null, configDir, FileUtil.TYPE_DIR, config);

			config.setTempDirectory(cst, !isReload);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws ExpressionException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 */
	private static void _loadFilesystem(ConfigServerImpl configServer, ConfigImpl config, Document doc, boolean doNew, Log log) {
		try {
			if (configServer != null) {
				Resource src = configServer.getConfigDir().getRealResource("distribution");
				Resource trg = config.getConfigDir().getRealResource("context/");
				copyContextFiles(src, trg);
			}
			Resource configDir = config.getConfigDir();

			boolean hasCS = configServer != null;

			String strAllowRealPath = null;
			String strDeployDirectory = null;
			// String strTempDirectory=null;

			// system.property or env var
			String strDefaultFLDDirectory = null;
			String strDefaultTLDDirectory = null;
			String strDefaultFuncDirectory = null;
			String strDefaultTagDirectory = null;
			String strFuncDirectory = null;
			String strTagDirectory = null;

			// only read in server context
			if (!hasCS) {
				strDefaultFLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.fld", null);
				strDefaultTLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.tld", null);
				strDefaultFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.function", null);
				strDefaultTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.tag", null);
				if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.fld", null);
				if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.tld", null);
				if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.function", null);
				if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.tag", null);
				strFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.additional.function", null);
				strTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.additional.tag", null);

			}

			Element fileSystem = doc != null ? getChildByName(doc.getDocumentElement(), "file-system") : null;
			if (fileSystem == null) fileSystem = doc != null ? getChildByName(doc.getDocumentElement(), "filesystem") : null;

			// get library directories
			if (fileSystem != null) {
				strAllowRealPath = getAttr(fileSystem, "allow-realpath");
				strDeployDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("deploy-directory"));
				if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("tld-directory"));
				if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("fld-directory"));
				if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("tag-directory"));
				if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("function-directory"));
				if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("tld-default-directory"));
				if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("fld-default-directory"));
				if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("tag-default-directory"));
				if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("function-default-directory"));
				if (StringUtil.isEmpty(strTagDirectory)) strTagDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("tag-addional-directory"));
				if (StringUtil.isEmpty(strFuncDirectory)) strFuncDirectory = ConfigWebUtil.translateOldPath(fileSystem.getAttribute("function-addional-directory"));
			}

			// set default directories if necessary
			if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = "{lucee-config}/library/fld/";
			if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = "{lucee-config}/library/tld/";
			if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = "{lucee-config}/library/function/";
			if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = "{lucee-config}/library/tag/";

			// Deploy Dir
			Resource dd = ConfigWebUtil.getFile(configDir, strDeployDirectory, "cfclasses", configDir, FileUtil.TYPE_DIR, config);
			config.setDeployDirectory(dd);

			// TAG

			// init TLDS
			if (hasCS) {
				config.setTLDs(ConfigWebUtil.duplicate(configServer.getTLDs(CFMLEngine.DIALECT_CFML), false), CFMLEngine.DIALECT_CFML);
				config.setTLDs(ConfigWebUtil.duplicate(configServer.getTLDs(CFMLEngine.DIALECT_LUCEE), false), CFMLEngine.DIALECT_LUCEE);
			}
			else {
				ConfigServerImpl cs = (ConfigServerImpl) config;
				config.setTLDs(ConfigWebUtil.duplicate(new TagLib[] { cs.cfmlCoreTLDs }, false), CFMLEngine.DIALECT_CFML);
				config.setTLDs(ConfigWebUtil.duplicate(new TagLib[] { cs.luceeCoreTLDs }, false), CFMLEngine.DIALECT_LUCEE);
			}

			// TLD Dir
			if (!StringUtil.isEmpty(strDefaultTLDDirectory)) {
				Resource tld = ConfigWebUtil.getFile(config, configDir, strDefaultTLDDirectory, FileUtil.TYPE_DIR);
				if (tld != null) config.setTldFile(tld, CFMLEngine.DIALECT_BOTH);
			}

			// Tag Directory
			List<Resource> listTags = new ArrayList<Resource>();
			if (!StringUtil.isEmpty(strDefaultTagDirectory)) {
				Resource dir = ConfigWebUtil.getFile(config, configDir, strDefaultTagDirectory, FileUtil.TYPE_DIR);
				createTagFiles(config, configDir, dir, doNew);
				if (dir != null) listTags.add(dir);
			}
			if (!StringUtil.isEmpty(strTagDirectory)) {
				String[] arr = ListUtil.listToStringArray(strTagDirectory, ',');
				for (String str: arr) {
					str = str.trim();
					if (StringUtil.isEmpty(str)) continue;
					Resource dir = ConfigWebUtil.getFile(config, configDir, str, FileUtil.TYPE_DIR);
					if (dir != null) listTags.add(dir);
				}
			}
			config.setTagDirectory(listTags);

			// allow realpath
			if (hasCS) {
				config.setAllowRealPath(configServer.allowRealPath());
			}
			if (!StringUtil.isEmpty(strAllowRealPath, true)) {
				config.setAllowRealPath(Caster.toBooleanValue(strAllowRealPath, true));
			}

			// FUNCTIONS

			// Init flds
			if (hasCS) {
				config.setFLDs(ConfigWebUtil.duplicate(configServer.getFLDs(CFMLEngine.DIALECT_CFML), false), CFMLEngine.DIALECT_CFML);
				config.setFLDs(ConfigWebUtil.duplicate(configServer.getFLDs(CFMLEngine.DIALECT_LUCEE), false), CFMLEngine.DIALECT_LUCEE);
			}
			else {
				ConfigServerImpl cs = (ConfigServerImpl) config;
				config.setFLDs(ConfigWebUtil.duplicate(new FunctionLib[] { cs.cfmlCoreFLDs }, false), CFMLEngine.DIALECT_CFML);
				config.setFLDs(ConfigWebUtil.duplicate(new FunctionLib[] { cs.luceeCoreFLDs }, false), CFMLEngine.DIALECT_LUCEE);
			}

			// FLDs
			if (!StringUtil.isEmpty(strDefaultFLDDirectory)) {
				Resource fld = ConfigWebUtil.getFile(config, configDir, strDefaultFLDDirectory, FileUtil.TYPE_DIR);
				if (fld != null) config.setFldFile(fld, CFMLEngine.DIALECT_BOTH);
			}

			// Function files (CFML)
			List<Resource> listFuncs = new ArrayList<Resource>();
			if (!StringUtil.isEmpty(strDefaultFuncDirectory)) {
				Resource dir = ConfigWebUtil.getFile(config, configDir, strDefaultFuncDirectory, FileUtil.TYPE_DIR);
				createFunctionFiles(config, configDir, dir, doNew);
				if (dir != null) listFuncs.add(dir);
				// if (dir != null) config.setFunctionDirectory(dir);
			}
			if (!StringUtil.isEmpty(strFuncDirectory)) {
				String[] arr = ListUtil.listToStringArray(strFuncDirectory, ',');
				for (String str: arr) {
					str = str.trim();
					if (StringUtil.isEmpty(str)) continue;
					Resource dir = ConfigWebUtil.getFile(config, configDir, str, FileUtil.TYPE_DIR);
					if (dir != null) listFuncs.add(dir);
					// if (dir != null) config.setFunctionDirectory(dir);
				}
			}
			config.setFunctionDirectory(listFuncs);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void createTagFiles(Config config, Resource configDir, Resource dir, boolean doNew) {
		if (config instanceof ConfigServer) {

			// Dump
			create("/resource/library/tag/", new String[] { "Dump." + COMPONENT_EXTENSION }, dir, doNew);

			/*
			 * Resource sub = dir.getRealResource("lucee/dump/skins/");
			 * create("/resource/library/tag/lucee/dump/skins/",new String[]{
			 * "text."+CFML_TEMPLATE_MAIN_EXTENSION ,"simple."+CFML_TEMPLATE_MAIN_EXTENSION
			 * ,"modern."+CFML_TEMPLATE_MAIN_EXTENSION ,"classic."+CFML_TEMPLATE_MAIN_EXTENSION
			 * ,"pastel."+CFML_TEMPLATE_MAIN_EXTENSION },sub,doNew);
			 */

			// MediaPlayer
			Resource f = dir.getRealResource("MediaPlayer." + COMPONENT_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/tag/MediaPlayer." + COMPONENT_EXTENSION, f);

			// /resource/library/tag/build
			Resource build = dir.getRealResource("build");
			if (!build.exists()) build.mkdirs();
			String[] names = new String[] { "_background.png", "_bigplay.png", "_controls.png", "_loading.gif", "_player.swf", "_player.xap",
					"background_png." + TEMPLATE_EXTENSION, "bigplay_png." + TEMPLATE_EXTENSION, "controls_png." + TEMPLATE_EXTENSION, "jquery.js." + TEMPLATE_EXTENSION,
					"loading_gif." + TEMPLATE_EXTENSION, "mediaelement-and-player.min.js." + TEMPLATE_EXTENSION, "mediaelementplayer.min.css." + TEMPLATE_EXTENSION,
					"player.swf." + TEMPLATE_EXTENSION, "player.xap." + TEMPLATE_EXTENSION };
			for (int i = 0; i < names.length; i++) {
				f = build.getRealResource(names[i]);
				if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/tag/build/" + names[i], f);

			}

			// /resource/library/tag/build/jquery
			Resource jquery = build.getRealResource("jquery");
			if (!jquery.isDirectory()) jquery.mkdirs();
			names = new String[] { "jquery-1.12.4.min.js" };
			for (int i = 0; i < names.length; i++) {
				f = jquery.getRealResource(names[i]);
				if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/tag/build/jquery/" + names[i], f);
			}

			// AJAX
			// AjaxFactory.deployTags(dir, doNew);

		}
	}

	private static void createFunctionFiles(Config config, Resource configDir, Resource dir, boolean doNew) {

		if (config instanceof ConfigServer) {
			Resource f = dir.getRealResource("writeDump." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/writeDump." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("dump." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/dump." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("location." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/location." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("threadJoin." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/threadJoin." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("threadTerminate." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/threadTerminate." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("throw." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/throw." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("trace." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/trace." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("queryExecute." + TEMPLATE_EXTENSION);
			// if (!f.exists() || doNew)
			// createFileFromResourceEL("/resource/library/function/queryExecute."+TEMPLATE_EXTENSION, f);
			if (f.exists())// FUTURE add this instead if(updateType=NEW_FRESH || updateType=NEW_FROM4)
				delete(dir, "queryExecute." + TEMPLATE_EXTENSION);

			f = dir.getRealResource("transactionCommit." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/transactionCommit." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("transactionRollback." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/transactionRollback." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("transactionSetsavepoint." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/transactionSetsavepoint." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("writeLog." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/writeLog." + TEMPLATE_EXTENSION, f);

			// AjaxFactory.deployFunctions(dir, doNew);

		}
	}

	private static void copyContextFiles(Resource src, Resource trg) {
		// directory
		if (src.isDirectory()) {
			if (trg.exists()) trg.mkdirs();
			Resource[] children = src.listResources();
			for (int i = 0; i < children.length; i++) {
				copyContextFiles(children[i], trg.getRealResource(children[i].getName()));
			}
		}
		// file
		else if (src.isFile()) {
			if (src.lastModified() > trg.lastModified()) {
				try {
					if (trg.exists()) trg.remove(true);
					trg.createFile(true);
					src.copyTo(trg, false);
				}
				catch (IOException e) {
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), XMLConfigWebFactory.class.getName(), e);
				}
			}

		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 */
	private static void _loadUpdate(ConfigServer configServer, Config config, Document doc, Log log) {
		try {
			// Server
			if (config instanceof ConfigServer && doc != null) {
				ConfigServer cs = (ConfigServer) config;
				Element update = getChildByName(doc.getDocumentElement(), "update");

				if (update != null) {
					cs.setUpdateType(getAttr(update, "type"));

					String location = getAttr(update, "location");
					if (location != null) {
						location = location.trim();
						if ("http://snapshot.lucee.org".equals(location) || "https://snapshot.lucee.org".equals(location)) location = "https://update.lucee.org";
						if ("http://release.lucee.org".equals(location) || "https://release.lucee.org".equals(location)) location = "https://update.lucee.org";
					}
				}
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadVideo(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element video = config instanceof ConfigServerImpl && doc != null ? getChildByName(doc.getDocumentElement(), "video") : null;
			boolean hasCS = configServer != null;
			ClassDefinition cd = null;
			// video-executer
			if (video != null) {
				cd = getClassDefinition(video, "video-executer-", config.getIdentification());
			}

			if (cd != null && cd.hasClass()) {

				try {
					Class clazz = cd.getClazz();
					if (!Reflector.isInstaneOf(clazz, VideoExecuter.class, false))
						throw new ApplicationException("class [" + cd + "] does not implement interface [" + VideoExecuter.class.getName() + "]");
					config.setVideoExecuterClass(clazz);

				}
				catch (Exception e) {
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), XMLConfigWebFactory.class.getName(), e);
				}
			}
			else if (hasCS) config.setVideoExecuterClass(configServer.getVideoExecuterClass());
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 */
	private static void _loadSetting(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			Element setting = hasAccess && doc != null ? getChildByName(doc.getDocumentElement(), "setting") : null;
			boolean hasCS = configServer != null;
			String str = null;

			// suppress whitespace
			str = null;
			if (setting != null) {
				str = getAttr(setting, "suppress-content");
			}
			if (!StringUtil.isEmpty(str) && hasAccess) {
				config.setSuppressContent(toBoolean(str, false));
			}
			else if (hasCS) config.setSuppressContent(configServer.isSuppressContent());

			// CFML Writer
			str = SystemUtil.getSystemPropOrEnvVar("lucee.cfml.writer", null);
			if (StringUtil.isEmpty(str) && setting != null) {
				str = getAttr(setting, "cfml-writer");
			}
			if (!StringUtil.isEmpty(str) && hasAccess) {
				if ("white-space".equalsIgnoreCase(str)) config.setCFMLWriterType(ConfigPro.CFML_WRITER_WS);
				else if ("white-space-pref".equalsIgnoreCase(str)) config.setCFMLWriterType(ConfigPro.CFML_WRITER_WS_PREF);
				else if ("regular".equalsIgnoreCase(str)) config.setCFMLWriterType(ConfigPro.CFML_WRITER_REFULAR);
				// FUTURE add support for classes implementing CFMLWriter interface
			}
			else if (hasCS) config.setCFMLWriterType(configServer.getCFMLWriterType());

			// show version
			str = null;
			if (setting != null) {
				str = getAttr(setting, "show-version");
				if (StringUtil.isEmpty(str)) str = getAttr(setting, "showversion");
			}
			if (!StringUtil.isEmpty(str) && hasAccess) {
				config.setShowVersion(toBoolean(str, false));
			}
			else if (hasCS) config.setShowVersion(configServer.isShowVersion());

			// close connection
			str = null;
			if (setting != null) {
				str = getAttr(setting, "close-connection");
				if (StringUtil.isEmpty(str)) str = getAttr(setting, "closeconnection");
			}
			if (!StringUtil.isEmpty(str) && hasAccess) {
				config.setCloseConnection(toBoolean(str, false));
			}
			else if (hasCS) config.setCloseConnection(configServer.closeConnection());

			// content-length
			str = null;
			if (setting != null) {
				str = getAttr(setting, "content-length");
				if (StringUtil.isEmpty(str)) str = getAttr(setting, "contentlength");
			}
			if (!StringUtil.isEmpty(str) && hasAccess) {
				config.setContentLength(toBoolean(str, true));
			}
			else if (hasCS) config.setContentLength(configServer.contentLength());

			// buffer-output
			str = null;
			if (setting != null) {
				str = getAttr(setting, "buffering-output");
				if (StringUtil.isEmpty(str)) str = getAttr(setting, "buffer-output");
			}
			Boolean b = Caster.toBoolean(str, null);
			if (b != null && hasAccess) {
				config.setBufferOutput(b.booleanValue());
			}
			else if (hasCS) config.setBufferOutput(configServer.getBufferOutput());

			// allow-compression
			str = SystemUtil.getSystemPropOrEnvVar("lucee.allow.compression", null);
			if (StringUtil.isEmpty(str) && setting != null) {
				str = getAttr(setting, "allow-compression");
				if (StringUtil.isEmpty(str)) str = getAttr(setting, "allowcompression");
			}
			if (!StringUtil.isEmpty(str) && hasAccess) {
				config.setAllowCompression(toBoolean(str, true));
			}
			else if (hasCS) config.setAllowCompression(configServer.allowCompression());
			Element mode = doc != null ? getChildByName(doc.getDocumentElement(), "mode") : null;
			// mode
			String developMode = getAttr(mode, "develop");
			if (!StringUtil.isEmpty(developMode) && hasAccess) {
				config.setDevelopMode(toBoolean(developMode, false));
			}
			else if (hasCS) config.setDevelopMode(configServer.isDevelopMode());
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadRemoteClient(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE);

			// SNSN
			// RemoteClientUsage

			// boolean hasCS=configServer!=null;
			Element _clients = doc != null ? getChildByName(doc.getDocumentElement(), "remote-clients") : null;

			// usage
			String strUsage = getAttr(_clients, "usage");
			Struct sct;
			if (!StringUtil.isEmpty(strUsage)) sct = toStruct(strUsage);// config.setRemoteClientUsage(toStruct(strUsage));
			else sct = new StructImpl();
			// TODO make this generic
			if (configServer != null) {
				String sync = Caster.toString(configServer.getRemoteClientUsage().get("synchronisation", ""), "");
				if (!StringUtil.isEmpty(sync)) {
					sct.setEL("synchronisation", sync);
				}
			}
			config.setRemoteClientUsage(sct);

			// max-threads
			int maxThreads = Caster.toIntValue(getAttr(_clients, "max-threads"), -1);
			if (maxThreads < 1 && configServer != null) {
				SpoolerEngineImpl engine = (SpoolerEngineImpl) configServer.getSpoolerEngine();
				if (engine != null) maxThreads = engine.getMaxThreads();
			}
			if (maxThreads < 1) maxThreads = 20;

			// directory
			String strDir = SystemUtil.getSystemPropOrEnvVar("lucee.task.directory", null);
			if (StringUtil.isEmpty(strDir)) strDir = _clients != null ? _clients.getAttribute("directory") : null;
			Resource file = ConfigWebUtil.getFile(config.getRootDirectory(), strDir, "client-task", config.getConfigDir(), FileUtil.TYPE_DIR, config);
			config.setRemoteClientDirectory(file);

			Element[] clients;
			Element client;

			if (!hasAccess) clients = new Element[0];
			else clients = _clients != null ? getChildren(_clients, "remote-client") : new Element[0];
			java.util.List<RemoteClient> list = new ArrayList<RemoteClient>();
			for (int i = 0; i < clients.length; i++) {
				client = clients[i];
				// type
				String type = getAttr(client, "type");
				if (StringUtil.isEmpty(type)) type = "web";
				// url
				String url = getAttr(client, "url");
				String label = getAttr(client, "label");
				if (StringUtil.isEmpty(label)) label = url;
				String sUser = getAttr(client, "server-username");
				String sPass = ConfigWebUtil.decrypt(getAttr(client, "server-password"));
				String aPass = ConfigWebUtil.decrypt(getAttr(client, "admin-password"));
				String aCode = ConfigWebUtil.decrypt(getAttr(client, "security-key"));
				// if(aCode!=null && aCode.indexOf('-')!=-1)continue;
				String usage = getAttr(client, "usage");
				if (usage == null) usage = "";

				String pUrl = getAttr(client, "proxy-server");
				int pPort = Caster.toIntValue(getAttr(client, "proxy-port"), -1);
				String pUser = getAttr(client, "proxy-username");
				String pPass = ConfigWebUtil.decrypt(getAttr(client, "proxy-password"));

				ProxyData pd = null;
				if (!StringUtil.isEmpty(pUrl, true)) {
					pd = new ProxyDataImpl();
					pd.setServer(pUrl);
					if (!StringUtil.isEmpty(pUser)) {
						pd.setUsername(pUser);
						pd.setPassword(pPass);
					}
					if (pPort > 0) pd.setPort(pPort);
				}
				list.add(new RemoteClientImpl(label, type, url, sUser, sPass, aPass, pd, aCode, usage));
			}
			if (list.size() > 0) config.setRemoteClients(list.toArray(new RemoteClient[list.size()]));
			else config.setRemoteClients(new RemoteClient[0]);

			// init spooler engine
			Resource dir = config.getRemoteClientDirectory();
			if (dir != null && !dir.exists()) dir.mkdirs();
			if (config.getSpoolerEngine() == null) {
				config.setSpoolerEngine(new SpoolerEngineImpl(config, dir, "Remote Client Spooler", config.getLog("remoteclient"), maxThreads));
			}
			else {
				SpoolerEngineImpl engine = (SpoolerEngineImpl) config.getSpoolerEngine();
				engine.setConfig(config);
				engine.setLog(config.getLog("remoteclient"));
				engine.setPersisDirectory(dir);
				engine.setMaxThreads(maxThreads);

			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadSystem(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {

			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
			Element sys = hasAccess && doc != null ? getChildByName(doc.getDocumentElement(), "system") : null;

			boolean hasCS = configServer != null;

			// web context
			if (hasCS) {
				config.setOut(config.getOutWriter());
				config.setErr(config.getErrWriter());
				return;
			}

			String out = null, err = null;
			// sys prop or env var
			out = SystemUtil.getSystemPropOrEnvVar("lucee.system.out", null);
			err = SystemUtil.getSystemPropOrEnvVar("lucee.system.err", null);

			if (sys != null) {
				if (StringUtil.isEmpty(out)) out = getAttr(sys, "out");
				if (StringUtil.isEmpty(err)) err = getAttr(sys, "err");
			}

			// OUT
			PrintStream ps = toPrintStream(config, out, false);
			config.setOut(new PrintWriter(ps));
			System.setOut(ps);

			// ERR
			ps = toPrintStream(config, err, true);
			config.setErr(new PrintWriter(ps));
			System.setErr(ps);

		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static PrintStream toPrintStream(Config config, String streamtype, boolean iserror) {
		if (!StringUtil.isEmpty(streamtype)) {
			streamtype = streamtype.trim();
			// null
			if (streamtype.equalsIgnoreCase("null")) {
				return new PrintStream(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM);
			}
			// class
			else if (StringUtil.startsWithIgnoreCase(streamtype, "class:")) {
				String classname = streamtype.substring(6);
				try {

					return (PrintStream) ClassUtil.loadInstance(classname);
				}
				catch (Exception e) {
				}
			}
			// file
			else if (StringUtil.startsWithIgnoreCase(streamtype, "file:")) {
				String strRes = streamtype.substring(5);
				try {
					strRes = ConfigWebUtil.translateOldPath(strRes);
					Resource res = ConfigWebUtil.getFile(config, config.getConfigDir(), strRes, ResourceUtil.TYPE_FILE);
					if (res != null) return new PrintStream(res.getOutputStream(), true);
				}
				catch (Exception e) {
				}
			}
			else if (StringUtil.startsWithIgnoreCase(streamtype, "log")) {
				try {
					CFMLEngine engine = ConfigWebUtil.getEngine(config);
					Resource root = ResourceUtil.toResource(engine.getCFMLEngineFactory().getResourceRoot());
					Resource log = root.getRealResource("context/logs/" + (iserror ? "err" : "out") + ".log");
					if (!log.isFile()) {
						log.getParentResource().mkdirs();
						log.createNewFile();
					}
					return new PrintStream(new RetireOutputStream(log, true, 5, null));
				}
				catch (Exception e) {
				}
			}
		}
		return iserror ? CFMLEngineImpl.CONSOLE_ERR : CFMLEngineImpl.CONSOLE_OUT;

	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 */
	private static void _loadCharset(ConfigServer configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			Element charset = hasAccess && doc != null ? getChildByName(doc.getDocumentElement(), "charset") : null;
			Element regional = hasAccess && doc != null ? getChildByName(doc.getDocumentElement(), "regional") : null;
			Element fileSystem = hasAccess && doc != null ? getChildByName(doc.getDocumentElement(), "file-system") : null;

			boolean hasCS = configServer != null;

			// template
			String template = null, fsCharset = null, fsEncoding = null;
			template = SystemUtil.getSystemPropOrEnvVar("lucee.template.charset", null);
			if (charset != null && StringUtil.isEmpty(template)) template = getAttr(charset, "template-charset");
			if (fileSystem != null) fsCharset = getAttr(fileSystem, "charset"); // deprecated but still supported
			if (fileSystem != null) fsEncoding = getAttr(fileSystem, "encoding"); // deprecated but still supported

			if (!StringUtil.isEmpty(template)) config.setTemplateCharset(template);
			else if (!StringUtil.isEmpty(fsCharset)) config.setTemplateCharset(fsCharset);
			else if (!StringUtil.isEmpty(fsEncoding)) config.setTemplateCharset(fsEncoding);
			else if (hasCS) config.setTemplateCharset(configServer.getTemplateCharset());

			// web
			String web = null, defaultEncoding = null;
			web = SystemUtil.getSystemPropOrEnvVar("lucee.web.charset", null);
			if (charset != null && StringUtil.isEmpty(web)) web = getAttr(charset, "web-charset");
			if (regional != null) defaultEncoding = getAttr(regional, "default-encoding"); // deprecated
			// supported
			if (!StringUtil.isEmpty(web)) config.setWebCharset(web);
			else if (!StringUtil.isEmpty(defaultEncoding)) config.setWebCharset(defaultEncoding);
			else if (hasCS) config.setWebCharset(configServer.getWebCharset());

			// resource
			String resource = null;
			resource = SystemUtil.getSystemPropOrEnvVar("lucee.resource.charset", null);
			if (charset != null && StringUtil.isEmpty(resource)) resource = getAttr(charset, "resource-charset");
			if (!StringUtil.isEmpty(resource)) config.setResourceCharset(resource);
			else if (hasCS) config.setResourceCharset(configServer.getResourceCharset());
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadQueue(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element queue = doc != null ? getChildByName(doc.getDocumentElement(), "queue") : null;

			// Server
			if (config instanceof ConfigServerImpl) {

				// max
				Integer max = Caster.toInteger(SystemUtil.getSystemPropOrEnvVar("lucee.queue.max", null), null);
				if (max == null) max = Caster.toInteger(getAttr(queue, "max"), null);
				config.setQueueMax(Caster.toIntValue(max, 100));

				// timeout
				Long timeout = Caster.toLong(SystemUtil.getSystemPropOrEnvVar("lucee.queue.timeout", null), null);
				if (timeout == null) timeout = Caster.toLong(getAttr(queue, "timeout"), null);
				config.setQueueTimeout(Caster.toLongValue(timeout, 0L));

				// enable
				Boolean enable = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.queue.enable", null), null);
				if (enable == null) enable = Caster.toBoolean(getAttr(queue, "enable"), null);
				config.setQueueEnable(Caster.toBooleanValue(enable, false));

				((ConfigServerImpl) config).setThreadQueue(config.getQueueEnable() ? new ThreadQueueImpl() : new ThreadQueueNone());

			}
			// Web
			else {
				config.setQueueMax(configServer.getQueueMax());
				config.setQueueTimeout(configServer.getQueueTimeout());
				config.setQueueEnable(configServer.getQueueEnable());
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 */
	private static void _loadRegional(ConfigServer configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			Element regional = hasAccess && doc != null ? getChildByName(doc.getDocumentElement(), "regional") : null;
			boolean hasCS = configServer != null;

			// timeZone
			String strTimeZone = null;
			if (regional != null) strTimeZone = getAttr(regional, "timezone");

			if (!StringUtil.isEmpty(strTimeZone)) config.setTimeZone(TimeZone.getTimeZone(strTimeZone));
			else if (hasCS) config.setTimeZone(configServer.getTimeZone());
			else {
				TimeZone def = TimeZone.getDefault();
				if (def == null) {
					def = TimeZoneConstants.EUROPE_LONDON;
				}
				config.setTimeZone(def);
			}

			// this is necessary, otherwise travis has no default
			if (TimeZone.getDefault() == null) TimeZone.setDefault(config.getTimeZone());

			// timeserver
			String strTimeServer = hasCS ? null : SystemUtil.getSystemPropOrEnvVar("lucee.timeserver", null);
			Boolean useTimeServer = null;
			if (!StringUtil.isEmpty(strTimeServer)) useTimeServer = Boolean.TRUE;

			if (regional != null) {
				if (StringUtil.isEmpty(strTimeServer)) strTimeServer = getAttr(regional, "timeserver");
				if (useTimeServer == null) useTimeServer = Caster.toBoolean(getAttr(regional, "use-timeserver"), null);
			}

			if (!StringUtil.isEmpty(strTimeServer)) config.setTimeServer(strTimeServer);
			else if (hasCS) config.setTimeServer(configServer.getTimeServer());

			if (useTimeServer != null) config.setUseTimeServer(useTimeServer.booleanValue());
			else if (hasCS) config.setUseTimeServer(((ConfigPro) configServer).getUseTimeServer());

			// locale
			String strLocale = null;
			if (regional != null) strLocale = getAttr(regional, "locale");

			if (!StringUtil.isEmpty(strLocale)) config.setLocale(strLocale);
			else if (hasCS) config.setLocale(configServer.getLocale());
			else config.setLocale(Locale.US);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadWS(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element el = doc != null ? getChildByName(doc.getDocumentElement(), "webservice") : null;
			ClassDefinition cd = el != null ? getClassDefinition(el, "", config.getIdentification()) : null;
			if (cd != null && !StringUtil.isEmpty(cd.getClassName())) {
				config.setWSHandlerClassDefinition(cd);
			}
			else if (configServer != null) {
				config.setWSHandlerClassDefinition(configServer.getWSHandlerClassDefinition());
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadORM(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM);

			Element orm = hasAccess && doc != null ? getChildByName(doc.getDocumentElement(), "orm") : null;
			boolean hasCS = configServer != null;

			// engine
			ClassDefinition cdDefault = new ClassDefinitionImpl(DummyORMEngine.class);

			ClassDefinition cd = null;
			if (orm != null) {
				cd = getClassDefinition(orm, "engine-", config.getIdentification());
				if (cd == null || cd.isClassNameEqualTo(DummyORMEngine.class.getName()) || cd.isClassNameEqualTo("lucee.runtime.orm.hibernate.HibernateORMEngine"))
					cd = getClassDefinition(orm, "", config.getIdentification());

				if (cd != null && (cd.isClassNameEqualTo(DummyORMEngine.class.getName()) || cd.isClassNameEqualTo("lucee.runtime.orm.hibernate.HibernateORMEngine"))) cd = null;
			}

			if (cd == null || !cd.hasClass()) {
				if (configServer != null) cd = configServer.getORMEngineClass();
				else cd = cdDefault;
			}

			// load class (removed because this unnecessary loads the orm engine)
			/*
			 * try { cd.getClazz(); // TODO check interface as well } catch (Exception e) { log.error("ORM", e);
			 * cd=cdDefault; }
			 */

			config.setORMEngineClass(cd);

			// config
			if (orm == null && doc != null) orm = doc.createElement("orm"); // this is just a dummy
			ORMConfiguration def = hasCS ? configServer.getORMConfig() : null;
			ORMConfiguration ormConfig = doc == null ? def : ORMConfigurationImpl.load(config, null, orm, config.getRootDirectory(), def);
			config.setORMConfig(ormConfig);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws PageException
	 * @throws IOException
	 */
	private static void _loadScope(ConfigServerImpl configServer, ConfigImpl config, Document doc, int mode, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			Element scope = doc != null ? getChildByName(doc.getDocumentElement(), "scope") : null;
			boolean hasCS = configServer != null;

			// Cluster Scope
			if (!hasCS) {
				ClassDefinition cd = scope != null ? getClassDefinition(scope, "cluster-", config.getIdentification()) : null;
				if (hasAccess && cd != null && cd.hasClass()) {
					try {
						Class clazz = cd.getClazz();
						if (!Reflector.isInstaneOf(clazz, Cluster.class, false) && !Reflector.isInstaneOf(clazz, ClusterRemote.class, false)) throw new ApplicationException(
								"class [" + clazz.getName() + "] does not implement interface [" + Cluster.class.getName() + "] or [" + ClusterRemote.class.getName() + "]");

						config.setClusterClass(clazz);

					}
					catch (Exception e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), XMLConfigWebFactory.class.getName(), e);
					}

				}
			}
			// else if(hasCS)
			// config.setClassClusterScope(configServer.getClassClusterScope());

			// Local Mode
			if (mode == ConfigPro.MODE_STRICT) {
				config.setLocalMode(Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS);
			}
			else {
				String strLocalMode = scope != null ? getAttr(scope, "local-mode") : null;
				if (hasAccess && !StringUtil.isEmpty(strLocalMode)) {
					config.setLocalMode(strLocalMode);
				}
				else if (hasCS) config.setLocalMode(configServer.getLocalMode());
			}

			// CGI readonly
			String strCGIReadonly = scope != null ? getAttr(scope, "cgi-readonly") : null;
			if (hasAccess && !StringUtil.isEmpty(strCGIReadonly)) {
				config.setCGIScopeReadonly(Caster.toBooleanValue(strCGIReadonly, true));
			}
			else if (hasCS) config.setCGIScopeReadonly(configServer.getCGIScopeReadonly());

			// Session-Type
			String strSessionType = scope != null ? getAttr(scope, "session-type") : null;
			if (hasAccess && !StringUtil.isEmpty(strSessionType)) {
				config.setSessionType(AppListenerUtil.toSessionType(strSessionType, hasCS ? configServer.getSessionType() : Config.SESSION_TYPE_APPLICATION));
			}
			else if (hasCS) config.setSessionType(configServer.getSessionType());

			// Cascading
			if (mode == ConfigPro.MODE_STRICT) {
				config.setScopeCascadingType(Config.SCOPE_STRICT);
			}
			else {
				String strScopeCascadingType = scope != null ? getAttr(scope, "cascading") : null;
				if (hasAccess && !StringUtil.isEmpty(strScopeCascadingType)) {
					config.setScopeCascadingType(ConfigWebUtil.toScopeCascading(strScopeCascadingType, Config.SCOPE_STANDARD));
				}
				else if (hasCS) config.setScopeCascadingType(configServer.getScopeCascadingType());
			}

			// cascade-to-resultset
			if (mode == ConfigPro.MODE_STRICT) {
				config.setAllowImplicidQueryCall(false);
			}
			else {
				Boolean allowImplicidQueryCall = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.cascade.to.resultset", null), null);
				if (allowImplicidQueryCall == null) allowImplicidQueryCall = Caster.toBoolean(getAttr(scope, "cascade-to-resultset"), null);
				if (hasAccess && allowImplicidQueryCall != null) {
					config.setAllowImplicidQueryCall(allowImplicidQueryCall.booleanValue());
				}
				else if (hasCS) config.setAllowImplicidQueryCall(configServer.allowImplicidQueryCall());
			}

			// Merge url and Form
			String strMergeFormAndURL = scope != null ? getAttr(scope, "merge-url-form") : null;
			if (hasAccess && !StringUtil.isEmpty(strMergeFormAndURL)) {
				config.setMergeFormAndURL(toBoolean(strMergeFormAndURL, false));
			}
			else if (hasCS) config.setMergeFormAndURL(configServer.mergeFormAndURL());

			// Client-Storage
			{
				String clientStorage = scope != null ? getAttr(scope, "clientstorage") : null;
				if (StringUtil.isEmpty(clientStorage, true)) clientStorage = scope != null ? getAttr(scope, "client-storage") : null;

				if (hasAccess && !StringUtil.isEmpty(clientStorage)) {
					config.setClientStorage(clientStorage);
				}
				else if (hasCS) config.setClientStorage(configServer.getClientStorage());
			}

			// Session-Storage
			{
				String sessionStorage = scope != null ? getAttr(scope, "sessionstorage") : null;
				if (StringUtil.isEmpty(sessionStorage, true)) sessionStorage = scope != null ? getAttr(scope, "session-storage") : null;

				if (hasAccess && !StringUtil.isEmpty(sessionStorage)) {
					config.setSessionStorage(sessionStorage);
				}
				else if (hasCS) config.setSessionStorage(configServer.getSessionStorage());
			}

			// Client Timeout
			String clientTimeout = scope != null ? getAttr(scope, "clienttimeout") : null;
			if (StringUtil.isEmpty(clientTimeout, true)) clientTimeout = scope != null ? getAttr(scope, "client-timeout") : null;
			if (StringUtil.isEmpty(clientTimeout, true)) {
				// deprecated
				clientTimeout = scope != null ? getAttr(scope, "client-max-age") : null;
				int days = Caster.toIntValue(clientTimeout, -1);
				if (days > 0) clientTimeout = days + ",0,0,0";
				else clientTimeout = "";
			}
			if (hasAccess && !StringUtil.isEmpty(clientTimeout)) {
				config.setClientTimeout(clientTimeout);
			}
			else if (hasCS) config.setClientTimeout(configServer.getClientTimeout());

			// Session Timeout
			String sessionTimeout = scope != null ? getAttr(scope, "sessiontimeout") : null;
			if (hasAccess && !StringUtil.isEmpty(sessionTimeout)) {
				config.setSessionTimeout(sessionTimeout);
			}
			else if (hasCS) config.setSessionTimeout(configServer.getSessionTimeout());

			// App Timeout

			String appTimeout = scope != null ? getAttr(scope, "applicationtimeout") : null;
			if (hasAccess && !StringUtil.isEmpty(appTimeout)) {
				config.setApplicationTimeout(appTimeout);
			}
			else if (hasCS) config.setApplicationTimeout(configServer.getApplicationTimeout());

			// Client Type
			String strClientType = scope != null ? getAttr(scope, "clienttype") : null;
			if (hasAccess && !StringUtil.isEmpty(strClientType)) {
				config.setClientType(strClientType);
			}
			else if (hasCS) config.setClientType(configServer.getClientType());

			// Client
			Resource configDir = config.getConfigDir();
			String strClientDirectory = scope != null ? getAttr(scope, "client-directory") : null;
			if (hasAccess && !StringUtil.isEmpty(strClientDirectory)) {
				strClientDirectory = ConfigWebUtil.translateOldPath(strClientDirectory);
				Resource res = ConfigWebUtil.getFile(configDir, strClientDirectory, "client-scope", configDir, FileUtil.TYPE_DIR, config);
				config.setClientScopeDir(res);
			}
			else {
				config.setClientScopeDir(configDir.getRealResource("client-scope"));
			}

			String strMax = scope != null ? getAttr(scope, "client-directory-max-size") : null;
			if (hasAccess && !StringUtil.isEmpty(strMax)) {
				config.setClientScopeDirSize(ByteSizeParser.parseByteSizeDefinition(strMax, config.getClientScopeDirSize()));
			}
			else if (hasCS) config.setClientScopeDirSize(configServer.getClientScopeDirSize());

			// Session Management
			String strSessionManagement = scope != null ? getAttr(scope, "sessionmanagement") : null;
			if (hasAccess && !StringUtil.isEmpty(strSessionManagement)) {
				config.setSessionManagement(toBoolean(strSessionManagement, true));
			}
			else if (hasCS) config.setSessionManagement(configServer.isSessionManagement());

			// Client Management
			String strClientManagement = scope != null ? getAttr(scope, "clientmanagement") : null;
			if (hasAccess && !StringUtil.isEmpty(strClientManagement)) {
				config.setClientManagement(toBoolean(strClientManagement, false));
			}
			else if (hasCS) config.setClientManagement(configServer.isClientManagement());

			// Client Cookies
			String strClientCookies = scope != null ? getAttr(scope, "setclientcookies") : null;
			if (hasAccess && !StringUtil.isEmpty(strClientCookies)) {
				config.setClientCookies(toBoolean(strClientCookies, true));
			}
			else if (hasCS) config.setClientCookies(configServer.isClientCookies());

			// Domain Cookies
			String strDomainCookies = scope != null ? getAttr(scope, "setdomaincookies") : null;
			if (hasAccess && !StringUtil.isEmpty(strDomainCookies)) {
				config.setDomainCookies(toBoolean(strDomainCookies, false));
			}
			else if (hasCS) config.setDomainCookies(configServer.isDomainCookies());
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadJava(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasCS = configServer != null;
			Element java = doc != null ? getChildByName(doc.getDocumentElement(), "java") : null;

			//
			String strInspectTemplate = getAttr(java, "inspect-template");
			if (!StringUtil.isEmpty(strInspectTemplate, true)) {
				config.setInspectTemplate(ConfigWebUtil.inspectTemplate(strInspectTemplate, ConfigPro.INSPECT_ONCE));
			}
			else if (hasCS) {
				config.setInspectTemplate(configServer.getInspectTemplate());
			}

			//
			String strCompileType = getAttr(java, "compile-type");
			if (!StringUtil.isEmpty(strCompileType)) {
				strCompileType = strCompileType.trim().toLowerCase();
				if (strCompileType.equals("after-startup")) {
					config.setCompileType(Config.RECOMPILE_AFTER_STARTUP);
				}
				else if (strCompileType.equals("always")) {
					config.setCompileType(Config.RECOMPILE_ALWAYS);
				}
			}
			else if (hasCS) {
				config.setCompileType(configServer.getCompileType());
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadConstants(ConfigServerImpl configServer, ConfigImpl config, Document doc) {
		try {
			boolean hasCS = configServer != null;
			Element constant = doc != null ? getChildByName(doc.getDocumentElement(), "constants") : null;

			// Constants
			Element[] elConstants = constant != null ? getChildren(constant, "constant") : new Element[0];
			Struct sct = null;
			if (hasCS) {
				sct = configServer.getConstants();
				if (sct != null) sct = (Struct) sct.duplicate(false);
			}
			if (sct == null) sct = new StructImpl();
			String name;
			for (int i = 0; i < elConstants.length; i++) {
				name = getAttr(elConstants[i], "name");
				if (StringUtil.isEmpty(name)) continue;
				sct.setEL(KeyImpl.getInstance(name.trim()), getAttr(elConstants[i], "value"));
			}
			config.setConstants(sct);
		}
		catch (Exception e) {
			log(config, null, e);
		}
	}

	public static void log(Config config, Log log, Exception e) {
		try {
			if (log != null) log.error("configuration", e);
			else {
				LogUtil.logGlobal(config, XMLConfigWebFactory.class.getName(), e);
			}
		}
		catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	private static void _loadLogin(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			// server context
			if (config instanceof ConfigServer) {
				Element login = doc != null ? getChildByName(doc.getDocumentElement(), "login") : null;
				boolean captcha = Caster.toBooleanValue(getAttr(login, "captcha"), false);
				boolean rememberme = Caster.toBooleanValue(getAttr(login, "rememberme"), true);

				int delay = Caster.toIntValue(getAttr(login, "delay"), 1);
				ConfigServerImpl cs = (ConfigServerImpl) config;
				cs.setLoginDelay(delay);
				cs.setLoginCaptcha(captcha);
				cs.setRememberMe(rememberme);
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadStartupHook(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element parent = doc != null ? getChildByName(doc.getDocumentElement(), "startup") : null;
			Element[] children = parent != null ? getChildren(parent, "hook") : null;

			if (children == null || children.length == 0) return;

			for (Element child: children) {
				ClassDefinition cd = getClassDefinition(child, "", config.getIdentification());
				ConfigBase.Startup existing = config.getStartups().get(cd.getClassName());

				if (existing != null) {
					if (existing.cd.equals(cd)) continue;
					try {
						Method fin = Reflector.getMethod(existing.instance.getClass(), "finalize", new Class[0], null);
						if (fin != null) {
							fin.invoke(existing.instance, new Object[0]);
						}
					}
					catch (Exception e) {
					}
				}
				Class clazz = cd.getClazz();

				Constructor constr = Reflector.getConstructor(clazz, new Class[] { Config.class }, null);
				if (constr != null) config.getStartups().put(cd.getClassName(), new ConfigBase.Startup(cd, constr.newInstance(new Object[] { config })));
				else config.getStartups().put(cd.getClassName(), new ConfigBase.Startup(cd, ClassUtil.loadInstance(clazz)));

			}
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws IOException
	 */
	private static void _loadMail(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) { // does no init values
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);

			boolean hasCS = configServer != null;
			Element mail = doc != null ? getChildByName(doc.getDocumentElement(), "mail") : null;

			// Send partial
			{
				String strSendPartial = getAttr(mail, "send-partial");
				if (!StringUtil.isEmpty(strSendPartial) && hasAccess) {
					config.setMailSendPartial(toBoolean(strSendPartial, false));
				}
				else if (hasCS) config.setMailSendPartial(configServer.isMailSendPartial());
			}
			// User set
			{
				String strUserSet = getAttr(mail, "user-set");
				if (!StringUtil.isEmpty(strUserSet) && hasAccess) {
					config.setUserSet(toBoolean(strUserSet, false));
				}
				else if (hasCS) config.setUserSet(configServer.isUserset());
			}

			// Spool Interval
			String strSpoolInterval = getAttr(mail, "spool-interval");
			if (!StringUtil.isEmpty(strSpoolInterval) && hasAccess) {
				config.setMailSpoolInterval(Caster.toIntValue(strSpoolInterval, 30));
			}
			else if (hasCS) config.setMailSpoolInterval(configServer.getMailSpoolInterval());

			String strEncoding = getAttr(mail, "default-encoding");
			if (!StringUtil.isEmpty(strEncoding) && hasAccess) config.setMailDefaultEncoding(strEncoding);
			else if (hasCS) config.setMailDefaultEncoding(configServer.getMailDefaultCharset());

			// Spool Enable
			String strSpoolEnable = getAttr(mail, "spool-enable");
			if (!StringUtil.isEmpty(strSpoolEnable) && hasAccess) {
				config.setMailSpoolEnable(toBoolean(strSpoolEnable, false));
			}
			else if (hasCS) config.setMailSpoolEnable(configServer.isMailSpoolEnable());

			// Timeout
			String strTimeout = getAttr(mail, "timeout");
			if (!StringUtil.isEmpty(strTimeout) && hasAccess) {
				config.setMailTimeout(Caster.toIntValue(strTimeout, 60));
			}
			else if (hasCS) config.setMailTimeout(configServer.getMailTimeout());

			// Servers
			int index = 0;
			// Server[] servers = null;
			Element[] elServers = mail != null ? getChildren(mail, "server") : new Element[0];
			List<Server> servers = new ArrayList<Server>();
			if (hasCS) {
				Server[] readOnlyServers = configServer.getMailServers();
				if (readOnlyServers != null) {
					for (int i = 0; i < readOnlyServers.length; i++) {
						servers.add(readOnlyServers[index++].cloneReadOnly());
					}
				}
			}
			// TODO get mail servers from env var
			if (hasAccess) {
				for (int i = 0; i < elServers.length; i++) {
					Element el = elServers[i];
					if (el.getNodeName().equals("server")) servers.add(i,
							new ServerImpl(Caster.toIntValue(getAttr(el, "id"), i + 1), getAttr(el, "smtp"), Caster.toIntValue(getAttr(el, "port"), 25), getAttr(el, "username"),
									ConfigWebUtil.decrypt(getAttr(el, "password")), toLong(el.getAttribute("life"), 1000 * 60 * 5), toLong(el.getAttribute("idle"), 1000 * 60 * 1),
									toBoolean(getAttr(el, "tls"), false), toBoolean(getAttr(el, "ssl"), false), toBoolean(getAttr(el, "reuse-connection"), true),
									hasCS ? ServerImpl.TYPE_LOCAL : ServerImpl.TYPE_GLOBAL));
				}
			}
			config.setMailServers(servers.toArray(new Server[servers.size()]));
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadMonitors(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			// only load in server context
			if (configServer != null) return;

			configServer = (ConfigServerImpl) config;

			Element parent = doc != null ? getChildByName(doc.getDocumentElement(), "monitoring") : null;
			Boolean enabled = Caster.toBoolean(getAttr(parent, "enabled"), null);
			if (enabled != null) configServer.setMonitoringEnabled(enabled.booleanValue());

			Element[] children = parent != null ? getChildren(parent, "monitor") : new Element[0];

			java.util.List<IntervallMonitor> intervalls = new ArrayList<IntervallMonitor>();
			java.util.List<RequestMonitor> requests = new ArrayList<RequestMonitor>();
			java.util.List<MonitorTemp> actions = new ArrayList<MonitorTemp>();
			String strType, name;
			ClassDefinition cd;
			boolean _log, async;
			short type;
			for (int i = 0; i < children.length; i++) {
				Element el = children[i];
				cd = getClassDefinition(el, "", config.getIdentification());
				strType = getAttr(el, "type");
				name = getAttr(el, "name");
				async = Caster.toBooleanValue(getAttr(el, "async"), false);
				_log = Caster.toBooleanValue(getAttr(el, "log"), true);

				if ("request".equalsIgnoreCase(strType)) type = IntervallMonitor.TYPE_REQUEST;
				else if ("action".equalsIgnoreCase(strType)) type = Monitor.TYPE_ACTION;
				else type = IntervallMonitor.TYPE_INTERVAL;

				if (cd.hasClass() && !StringUtil.isEmpty(name)) {
					name = name.trim();
					try {
						Class clazz = cd.getClazz();
						Object obj;
						ConstructorInstance constr = Reflector.getConstructorInstance(clazz, new Object[] { configServer }, null);
						if (constr != null) obj = constr.invoke();
						else obj = ClassUtil.newInstance(clazz);
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(),
								"loaded " + (strType) + " monitor [" + clazz.getName() + "]");
						if (type == IntervallMonitor.TYPE_INTERVAL) {
							IntervallMonitor m = obj instanceof IntervallMonitor ? (IntervallMonitor) obj : new IntervallMonitorWrap(obj);
							m.init(configServer, name, _log);
							intervalls.add(m);
						}
						else if (type == Monitor.TYPE_ACTION) {
							ActionMonitor am = obj instanceof ActionMonitor ? (ActionMonitor) obj : new ActionMonitorWrap(obj);
							actions.add(new MonitorTemp(am, name, _log));
						}
						else {
							RequestMonitorPro m = new RequestMonitorProImpl(obj instanceof RequestMonitor ? (RequestMonitor) obj : new RequestMonitorWrap(obj));
							if (async) m = new AsyncRequestMonitor(m);
							m.init(configServer, name, _log);
							LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_INFO, XMLConfigWebFactory.class.getName(),
									"initialize " + (strType) + " monitor [" + clazz.getName() + "]");

							requests.add(m);
						}
					}
					catch (Exception e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), XMLConfigWebFactory.class.getName(), e);
					}
				}

			}
			configServer.setRequestMonitors(requests.toArray(new RequestMonitor[requests.size()]));
			configServer.setIntervallMonitors(intervalls.toArray(new IntervallMonitor[intervalls.size()]));
			ActionMonitorCollector actionMonitorCollector = ActionMonitorFatory.getActionMonitorCollector(configServer, actions.toArray(new MonitorTemp[actions.size()]));
			configServer.setActionMonitorCollector(actionMonitorCollector);

			((CFMLEngineImpl) configServer.getCFMLEngine()).touchMonitor(configServer);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws PageException
	 */
	private static void _loadSearch(ConfigServer configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element search = doc != null ? getChildByName(doc.getDocumentElement(), "search") : null;

			// class
			ClassDefinition<SearchEngine> cd = search != null ? getClassDefinition(search, "engine-", config.getIdentification()) : null;
			if (cd == null || !cd.hasClass() || "lucee.runtime.search.lucene.LuceneSearchEngine".equals(cd.getClassName())) {
				if (configServer != null) cd = ((ConfigPro) configServer).getSearchEngineClassDefinition();
				else cd = new ClassDefinitionImpl(DummySearchEngine.class);
			}

			// directory
			String dir = search != null ? search.getAttribute("directory") : null;
			if (StringUtil.isEmpty(dir)) {
				if (configServer != null) dir = ((ConfigPro) configServer).getSearchEngineDirectory();
				else dir = "{lucee-web}/search/";
			}

			config.setSearchEngine(cd, dir);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @param isEventGatewayContext
	 * @throws IOException
	 * @throws PageException
	 */
	private static void _loadScheduler(ConfigServer configServer, ConfigImpl config, Document doc, Log log) {
		try {
			if (config instanceof ConfigServer) return;

			Resource configDir = config.getConfigDir();
			Element scheduler = doc == null ? null : getChildByName(doc.getDocumentElement(), "scheduler");

			// set scheduler
			Resource file = ConfigWebUtil.getFile(config.getRootDirectory(), getAttr(scheduler, "directory"), "scheduler", configDir, FileUtil.TYPE_DIR, config);
			config.setScheduler(configServer.getCFMLEngine(), file);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 */
	private static void _loadDebug(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasCS = configServer != null;
			Element debugging = doc != null ? getChildByName(doc.getDocumentElement(), "debugging") : null;
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);

			// Entries
			Element[] entries = debugging != null ? getChildren(debugging, "debug-entry") : new Element[0];
			Map<String, DebugEntry> list = new HashMap<String, DebugEntry>();
			if (hasCS) {
				DebugEntry[] _entries = ((ConfigPro) configServer).getDebugEntries();
				for (int i = 0; i < _entries.length; i++) {
					list.put(_entries[i].getId(), _entries[i].duplicate(true));
				}
			}
			Element e;
			String id;
			for (int i = 0; i < entries.length; i++) {
				e = entries[i];
				id = getAttr(e, "id");
				try {
					list.put(id, new DebugEntry(id, getAttr(e, "type"), getAttr(e, "iprange"), getAttr(e, "label"), e.getAttribute("path"), getAttr(e, "fullname"),
							toStruct(getAttr(e, "custom"))));
				}
				catch (IOException ioe) {
				}
			}
			config.setDebugEntries(list.values().toArray(new DebugEntry[list.size()]));

			// debug
			String strDebug = getAttr(debugging, "debug");
			if (hasAccess && !StringUtil.isEmpty(strDebug)) {
				config.setDebug(toBoolean(strDebug, false) ? ConfigImpl.CLIENT_BOOLEAN_TRUE : ConfigImpl.CLIENT_BOOLEAN_FALSE);
			}
			else if (hasCS) config.setDebug(configServer.debug() ? ConfigImpl.SERVER_BOOLEAN_TRUE : ConfigImpl.SERVER_BOOLEAN_FALSE);

			// debug-log-output
			String strDLO = getAttr(debugging, "debug-log-output");
			if (hasAccess && !StringUtil.isEmpty(strDLO)) {
				config.setDebugLogOutput(toBoolean(strDLO, false) ? ConfigImpl.CLIENT_BOOLEAN_TRUE : ConfigImpl.CLIENT_BOOLEAN_FALSE);
			}
			else if (hasCS) config.setDebugLogOutput(configServer.debugLogOutput() ? ConfigImpl.SERVER_BOOLEAN_TRUE : ConfigImpl.SERVER_BOOLEAN_FALSE);

			// debug options
			String strDebugOption = hasCS ? null : SystemUtil.getSystemPropOrEnvVar("lucee.debugging.options", null);
			String[] debugOptions = StringUtil.isEmpty(strDebugOption) ? null : ListUtil.listToStringArray(strDebugOption, ',');

			int options = 0;
			String str = getAttr(debugging, "database");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_DATABASE;
			}
			else if (debugOptions != null && extractDebugOption("database", debugOptions)) options += ConfigPro.DEBUG_DATABASE;
			else if (hasCS && configServer.hasDebugOptions(ConfigPro.DEBUG_DATABASE)) options += ConfigPro.DEBUG_DATABASE;

			str = getAttr(debugging, "exception");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_EXCEPTION;
			}
			else if (debugOptions != null && extractDebugOption("exception", debugOptions)) options += ConfigPro.DEBUG_EXCEPTION;
			else if (hasCS && configServer.hasDebugOptions(ConfigPro.DEBUG_EXCEPTION)) options += ConfigPro.DEBUG_EXCEPTION;

			str = getAttr(debugging, "templenabled");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_TEMPLATE;
			}
			else if (debugOptions != null && extractDebugOption("template", debugOptions)) options += ConfigPro.DEBUG_TEMPLATE;
			else if (hasCS && configServer.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) options += ConfigPro.DEBUG_TEMPLATE;
			// default is true
			else options += ConfigPro.DEBUG_TEMPLATE;

			str = getAttr(debugging, "dump");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_DUMP;
			}
			else if (debugOptions != null && extractDebugOption("dump", debugOptions)) options += ConfigPro.DEBUG_DUMP;
			else if (hasCS && configServer.hasDebugOptions(ConfigPro.DEBUG_DUMP)) options += ConfigPro.DEBUG_DUMP;

			str = getAttr(debugging, "tracing");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_TRACING;
			}
			else if (debugOptions != null && extractDebugOption("tracing", debugOptions)) options += ConfigPro.DEBUG_TRACING;
			else if (hasCS && configServer.hasDebugOptions(ConfigPro.DEBUG_TRACING)) options += ConfigPro.DEBUG_TRACING;

			str = getAttr(debugging, "timer");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_TIMER;
			}
			else if (debugOptions != null && extractDebugOption("timer", debugOptions)) options += ConfigPro.DEBUG_TIMER;
			else if (hasCS && configServer.hasDebugOptions(ConfigPro.DEBUG_TIMER)) options += ConfigPro.DEBUG_TIMER;

			str = getAttr(debugging, "implicit-access");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS;
			}
			else if (debugOptions != null && extractDebugOption("implicit-access", debugOptions)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS;
			else if (hasCS && configServer.hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS;

			str = getAttr(debugging, "query-usage");
			if (StringUtil.isEmpty(str)) str = getAttr(debugging, "show-query-usage");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_QUERY_USAGE;
			}
			else if (debugOptions != null && extractDebugOption("query-usage", debugOptions)) options += ConfigPro.DEBUG_QUERY_USAGE;
			else if (hasCS && configServer.hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)) options += ConfigPro.DEBUG_QUERY_USAGE;

			// max records logged
			String strMax = getAttr(debugging, "max-records-logged");
			if (hasAccess && !StringUtil.isEmpty(strMax)) {
				config.setDebugMaxRecordsLogged(Caster.toIntValue(strMax, 10));
			}
			else if (hasCS) config.setDebugMaxRecordsLogged(configServer.getDebugMaxRecordsLogged());

			config.setDebugOptions(options);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static boolean extractDebugOption(String name, String[] values) {
		for (String val: values) {
			if (val.trim().equalsIgnoreCase(name)) return true;
		}
		return false;
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 */
	private static void _loadCFX(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CFX_SETTING);

			Map<String, CFXTagClass> map = MapFactory.<String, CFXTagClass>getConcurrentMap();
			if (configServer != null) {
				try {
					if (configServer.getCFXTagPool() != null) {
						Map<String, CFXTagClass> classes = configServer.getCFXTagPool().getClasses();
						Iterator<Entry<String, CFXTagClass>> it = classes.entrySet().iterator();
						Entry<String, CFXTagClass> e;
						while (it.hasNext()) {
							e = it.next();
							map.put(e.getKey(), e.getValue().cloneReadOnly());
						}
					}
				}
				catch (SecurityException e) {
				}
			}

			if (hasAccess) {
				if (configServer == null) {
					System.setProperty("cfx.bin.path", config.getConfigDir().getRealResource("bin").getAbsolutePath());
				}

				// Java CFX Tags
				Element cfxTagsParent = doc != null ? getChildByName(doc.getDocumentElement(), "ext-tags", false, true) : null;
				if (cfxTagsParent == null) cfxTagsParent = doc != null ? getChildByName(doc.getDocumentElement(), "cfx-tags", false, true) : null;
				if (cfxTagsParent == null) cfxTagsParent = doc != null ? getChildByName(doc.getDocumentElement(), "ext-tags") : null;

				boolean oldStyle = cfxTagsParent != null ? cfxTagsParent.getNodeName().equals("cfx-tags") : false;

				Element[] cfxTags = cfxTagsParent != null ? (oldStyle ? getChildren(cfxTagsParent, "cfx-tag") : getChildren(cfxTagsParent, "ext-tag")) : new Element[0];
				for (int i = 0; i < cfxTags.length; i++) {
					String type = getAttr(cfxTags[i], "type");
					if (type != null) {
						// Java CFX Tags
						if (type.equalsIgnoreCase("java")) {
							String name = getAttr(cfxTags[i], "name");
							ClassDefinition cd = getClassDefinition(cfxTags[i], "", config.getIdentification());
							if (!StringUtil.isEmpty(name) && cd.hasClass()) {
								map.put(name.toLowerCase(), new JavaCFXTagClass(name, cd));
							}
						}
					}
				}

			}
			config.setCFXTagPool(map);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * loads the bundles defined in the extensions
	 * 
	 * @param cs
	 * @param config
	 * @param doc
	 * @param log
	 */
	private static void _loadExtensionBundles(ConfigServerImpl cs, ConfigImpl config, Document doc, Log log) {
		try {
			Element parent = doc != null ? getChildByName(doc.getDocumentElement(), "extensions") : null;
			Element[] children = parent != null ? getChildren(parent, "rhextension") : new Element[0];
			String strBundles;
			List<RHExtension> extensions = new ArrayList<RHExtension>();

			RHExtension rhe;
			for (Element child: children) {
				BundleInfo[] bfsq;
				try {
					rhe = new RHExtension(config, child);
					if (rhe.getStartBundles()) rhe.deployBundles(config);
					extensions.add(rhe);
				}
				catch (Exception e) {
					log.error("load-extension", e);
					continue;
				}
			}
			config.setExtensions(extensions.toArray(new RHExtension[extensions.size()]));
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadExtensions(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element xmlExtParent = doc != null ? getChildByName(doc.getDocumentElement(), "extensions") : null;

			String strEnabled = xmlExtParent != null ? getAttr(xmlExtParent, "enabled") : null;
			if (!StringUtil.isEmpty(strEnabled)) {
				config.setExtensionEnabled(Caster.toBooleanValue(strEnabled, false));
			}

			// RH Providers
			{
				// providers
				Element[] xmlProviders = xmlExtParent != null ? getChildren(xmlExtParent, "rhprovider") : null;
				String strProvider;
				Map<RHExtensionProvider, String> providers = new LinkedHashMap<RHExtensionProvider, String>();

				for (int i = 0; i < Constants.RH_EXTENSION_PROVIDERS.length; i++) {
					providers.put(Constants.RH_EXTENSION_PROVIDERS[i], "");
				}
				if (xmlProviders != null) {
					for (int i = 0; i < xmlProviders.length; i++) {
						strProvider = getAttr(xmlProviders[i], "url");
						if (!StringUtil.isEmpty(strProvider, true)) {
							try {
								providers.put(new RHExtensionProvider(strProvider.trim(), false), "");
							}
							catch (MalformedURLException e) {
								LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), XMLConfigWebFactory.class.getName(), e);
							}
						}
					}
				}
				config.setRHExtensionProviders(providers.keySet().toArray(new RHExtensionProvider[providers.size()]));
			}

			// extensions
			Element[] xmlExtensions = xmlExtParent != null ? getChildren(xmlExtParent, "extension") : null;
			Extension[] extensions = xmlExtensions != null ? new Extension[xmlExtensions.length] : new Extension[0];
			if (xmlExtensions != null) {
				Element xmlExtension;
				for (int i = 0; i < xmlExtensions.length; i++) {
					xmlExtension = xmlExtensions[i];
					extensions[i] = new ExtensionImpl(getAttr(xmlExtension, "config"), getAttr(xmlExtension, "id"), getAttr(xmlExtension, "provider"),
							getAttr(xmlExtension, "version"), getAttr(xmlExtension, "name"), getAttr(xmlExtension, "label"), getAttr(xmlExtension, "description"),
							getAttr(xmlExtension, "category"), getAttr(xmlExtension, "image"), getAttr(xmlExtension, "author"), getAttr(xmlExtension, "codename"),
							getAttr(xmlExtension, "video"), getAttr(xmlExtension, "support"), getAttr(xmlExtension, "documentation"), getAttr(xmlExtension, "forum"),
							getAttr(xmlExtension, "mailinglist"), getAttr(xmlExtension, "network"), DateCaster.toDateAdvanced(getAttr(xmlExtension, "created"), null, null),
							getAttr(xmlExtension, "type"));
				}
			}
			config.setExtensions(extensions);
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws IOException
	 */
	private static void _loadComponent(ConfigServer configServer, ConfigImpl config, Document doc, int mode, Log log) {
		try {
			Element component = doc != null ? getChildByName(doc.getDocumentElement(), "component") : null;
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
			boolean hasSet = false;
			boolean hasCS = configServer != null;

			// String virtual="/component/";

			if (component != null && hasAccess) {

				// component-default-import
				String strCDI = getAttr(component, "component-default-import");
				if (StringUtil.isEmpty(strCDI, true) && configServer != null) {
					strCDI = ((ConfigServerImpl) configServer).getComponentDefaultImport().toString();
				}
				if (!StringUtil.isEmpty(strCDI, true)) config.setComponentDefaultImport(strCDI);

				// Base CFML
				String strBase = getAttr(component, "base-cfml");
				if (StringUtil.isEmpty(strBase, true)) strBase = getAttr(component, "base");
				if (StringUtil.isEmpty(strBase, true) && configServer != null) {
					strBase = configServer.getBaseComponentTemplate(CFMLEngine.DIALECT_CFML);
				}
				config.setBaseComponentTemplate(CFMLEngine.DIALECT_CFML, strBase);

				// Base Lucee
				strBase = getAttr(component, "base-lucee");
				if (StringUtil.isEmpty(strBase, true)) {
					if (configServer != null) strBase = configServer.getBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE);
					else strBase = "/lucee/Component.lucee";

				}
				config.setBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE, strBase);

				// deep search
				if (mode == ConfigPro.MODE_STRICT) {
					config.setDoComponentDeepSearch(false);
				}
				else {
					String strDeepSearch = getAttr(component, "deep-search");
					if (!StringUtil.isEmpty(strDeepSearch)) {
						config.setDoComponentDeepSearch(Caster.toBooleanValue(strDeepSearch.trim(), false));
					}
					else if (hasCS) {
						config.setDoComponentDeepSearch(((ConfigServerImpl) configServer).doComponentDeepSearch());
					}
				}

				// Dump-Template
				String strDumpRemplate = component.getAttribute("dump-template");
				if ((strDumpRemplate == null || strDumpRemplate.trim().length() == 0) && configServer != null) {
					strDumpRemplate = configServer.getComponentDumpTemplate();
				}
				config.setComponentDumpTemplate(strDumpRemplate);

				// data-member-default-access
				if (mode == ConfigPro.MODE_STRICT) {
					config.setComponentDataMemberDefaultAccess(Component.ACCESS_PRIVATE);
				}
				else {
					String strDmda = getAttr(component, "data-member-default-access");
					if (strDmda != null && strDmda.trim().length() > 0) {
						strDmda = strDmda.toLowerCase().trim();
						if (strDmda.equals("remote")) config.setComponentDataMemberDefaultAccess(Component.ACCESS_REMOTE);
						else if (strDmda.equals("public")) config.setComponentDataMemberDefaultAccess(Component.ACCESS_PUBLIC);
						else if (strDmda.equals("package")) config.setComponentDataMemberDefaultAccess(Component.ACCESS_PACKAGE);
						else if (strDmda.equals("private")) config.setComponentDataMemberDefaultAccess(Component.ACCESS_PRIVATE);
					}
					else if (configServer != null) {
						config.setComponentDataMemberDefaultAccess(configServer.getComponentDataMemberDefaultAccess());
					}
				}

				// trigger-properties
				if (mode == ConfigPro.MODE_STRICT) {
					config.setTriggerComponentDataMember(true);
				}
				else {
					Boolean tp = Caster.toBoolean(getAttr(component, "trigger-data-member"), null);
					if (tp != null) config.setTriggerComponentDataMember(tp.booleanValue());
					else if (configServer != null) {
						config.setTriggerComponentDataMember(configServer.getTriggerComponentDataMember());
					}
				}

				// local search
				if (mode == ConfigPro.MODE_STRICT) {
					config.setComponentLocalSearch(false);
				}
				else {
					Boolean ls = Caster.toBoolean(getAttr(component, "local-search"), null);
					if (ls != null) config.setComponentLocalSearch(ls.booleanValue());
					else if (configServer != null) {
						config.setComponentLocalSearch(((ConfigServerImpl) configServer).getComponentLocalSearch());
					}
				}

				// use cache path
				Boolean ucp = Caster.toBoolean(getAttr(component, "use-cache-path"), null);
				if (ucp != null) config.setUseComponentPathCache(ucp.booleanValue());
				else if (configServer != null) {
					config.setUseComponentPathCache(((ConfigServerImpl) configServer).useComponentPathCache());
				}

				// use component shadow
				if (mode == ConfigPro.MODE_STRICT) {
					config.setUseComponentShadow(false);
				}
				else {
					Boolean ucs = Caster.toBoolean(getAttr(component, "use-shadow"), null);
					if (ucs != null) config.setUseComponentShadow(ucs.booleanValue());
					else if (configServer != null) {
						config.setUseComponentShadow(configServer.useComponentShadow());
					}
				}

			}
			else if (configServer != null) {
				config.setBaseComponentTemplate(CFMLEngine.DIALECT_CFML, configServer.getBaseComponentTemplate(CFMLEngine.DIALECT_CFML));
				config.setBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE, configServer.getBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE));
				config.setComponentDumpTemplate(configServer.getComponentDumpTemplate());
				if (mode == ConfigPro.MODE_STRICT) {
					config.setComponentDataMemberDefaultAccess(Component.ACCESS_PRIVATE);
					config.setTriggerComponentDataMember(true);
				}
				else {
					config.setComponentDataMemberDefaultAccess(configServer.getComponentDataMemberDefaultAccess());
					config.setTriggerComponentDataMember(configServer.getTriggerComponentDataMember());
				}
			}

			if (mode == ConfigPro.MODE_STRICT) {
				config.setDoComponentDeepSearch(false);
				config.setComponentDataMemberDefaultAccess(Component.ACCESS_PRIVATE);
				config.setTriggerComponentDataMember(true);
				config.setComponentLocalSearch(false);
				config.setUseComponentShadow(false);

			}

			// Web Mapping

			Element[] cMappings = component != null ? getChildren(component, "mapping") : new Element[0];
			hasSet = false;
			Mapping[] mappings = null;
			if (hasAccess && cMappings.length > 0) {
				mappings = new Mapping[cMappings.length];
				for (int i = 0; i < cMappings.length; i++) {
					Element cMapping = cMappings[i];
					String physical = cMapping.getAttribute("physical");
					String archive = cMapping.getAttribute("archive");
					boolean readonly = toBoolean(getAttr(cMapping, "readonly"), false);
					boolean hidden = toBoolean(getAttr(cMapping, "hidden"), false);

					int listMode = ConfigWebUtil.toListenerMode(getAttr(cMapping, "listener-mode"), -1);
					int listType = ConfigWebUtil.toListenerType(getAttr(cMapping, "listener-type"), -1);
					short inspTemp = inspectTemplate(cMapping);
					String virtual = XMLConfigAdmin.createVirtual(cMapping);

					String primary = getAttr(cMapping, "primary");

					boolean physicalFirst = archive == null || !primary.equalsIgnoreCase("archive");
					hasSet = true;
					mappings[i] = new MappingImpl(config, virtual, physical, archive, inspTemp, physicalFirst, hidden, readonly, true, false, true, null, listMode, listType);
				}

				config.setComponentMappings(mappings);

			}

			// Server Mapping
			if (hasCS) {
				Mapping[] originals = ((ConfigServerImpl) configServer).getComponentMappings();
				Mapping[] clones = new Mapping[originals.length];
				LinkedHashMap map = new LinkedHashMap();
				Mapping m;
				for (int i = 0; i < clones.length; i++) {
					m = ((MappingImpl) originals[i]).cloneReadOnly(config);
					map.put(toKey(m), m);
					// clones[i]=((MappingImpl)m[i]).cloneReadOnly(config);
				}

				if (mappings != null) {
					for (int i = 0; i < mappings.length; i++) {
						m = mappings[i];
						map.put(toKey(m), m);
					}
				}
				if (originals.length > 0) {
					clones = new Mapping[map.size()];
					Iterator it = map.entrySet().iterator();
					Map.Entry entry;
					int index = 0;
					while (it.hasNext()) {
						entry = (Entry) it.next();
						clones[index++] = (Mapping) entry.getValue();
						// print.out("c:"+clones[index-1]);
					}
					hasSet = true;
					// print.err("set:"+clones.length);

					config.setComponentMappings(clones);
				}
			}

			if (!hasSet) {
				MappingImpl m = new MappingImpl(config, "/default", "{lucee-web}/components/", null, ConfigPro.INSPECT_UNDEFINED, true, false, false, true, false, true, null, -1,
						-1);
				config.setComponentMappings(new Mapping[] { m.cloneReadOnly(config) });
			}

		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadProxy(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			boolean hasCS = configServer != null;
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
			Element proxy = doc != null ? getChildByName(doc.getDocumentElement(), "proxy") : null;

			// proxy server
			String server = getAttr(proxy, "server");
			String username = getAttr(proxy, "username");
			String password = getAttr(proxy, "password");
			int port = Caster.toIntValue(getAttr(proxy, "port"), -1);

			// includes/excludes
			Set<String> includes = proxy != null ? ProxyDataImpl.toStringSet(getAttr(proxy, "includes")) : null;
			Set<String> excludes = proxy != null ? ProxyDataImpl.toStringSet(getAttr(proxy, "excludes")) : null;

			if (hasAccess && !StringUtil.isEmpty(server)) {
				ProxyDataImpl pd = (ProxyDataImpl) ProxyDataImpl.getInstance(server, port, username, password);
				pd.setExcludes(excludes);
				pd.setIncludes(includes);
				config.setProxyData(pd);

			}
			else if (hasCS) config.setProxyData(configServer.getProxyData());
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	private static void _loadError(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element error = doc != null ? getChildByName(doc.getDocumentElement(), "error") : null;
			boolean hasCS = configServer != null;
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);

			// error template
			String template = getAttr(error, "template");

			// 500
			String template500 = getAttr(error, "template-500");
			if (StringUtil.isEmpty(template500)) template500 = getAttr(error, "template500");
			if (StringUtil.isEmpty(template500)) template500 = getAttr(error, "500");
			if (StringUtil.isEmpty(template500)) template500 = template;
			if (hasAccess && !StringUtil.isEmpty(template500)) {
				config.setErrorTemplate(500, template500);
			}
			else if (hasCS) config.setErrorTemplate(500, configServer.getErrorTemplate(500));
			else config.setErrorTemplate(500, "/lucee/templates/error/error." + TEMPLATE_EXTENSION);

			// 404
			String template404 = getAttr(error, "template-404");
			if (StringUtil.isEmpty(template404)) template404 = getAttr(error, "template404");
			if (StringUtil.isEmpty(template404)) template404 = getAttr(error, "404");
			if (StringUtil.isEmpty(template404)) template404 = template;
			if (hasAccess && !StringUtil.isEmpty(template404)) {
				config.setErrorTemplate(404, template404);
			}
			else if (hasCS) config.setErrorTemplate(404, configServer.getErrorTemplate(404));
			else config.setErrorTemplate(404, "/lucee/templates/error/error." + TEMPLATE_EXTENSION);

			// status code
			Boolean bStausCode = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.status.code", null), null);
			if (bStausCode == null) bStausCode = Caster.toBoolean(getAttr(error, "status-code"), null);
			if (bStausCode == null) bStausCode = Caster.toBoolean(getAttr(error, "statusCode"), null);
			if (bStausCode == null) bStausCode = Caster.toBoolean(getAttr(error, "status"), null);

			if (bStausCode != null && hasAccess) {
				config.setErrorStatusCode(bStausCode.booleanValue());
			}
			else if (hasCS) config.setErrorStatusCode(configServer.getErrorStatusCode());
		}
		catch (Exception e) {
			log(config, log, e);
		}

	}

	private static void _loadRegex(ConfigServerImpl configServer, ConfigImpl config, Document doc, Log log) {
		try {
			Element regex = doc != null ? getChildByName(doc.getDocumentElement(), "regex") : null;
			boolean hasCS = configServer != null;
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			String strType = getAttr(regex, "type");
			int type = StringUtil.isEmpty(strType) ? RegexFactory.TYPE_UNDEFINED : RegexFactory.toType(strType, RegexFactory.TYPE_UNDEFINED);

			if (hasAccess && type != RegexFactory.TYPE_UNDEFINED) {
				config.setRegex(RegexFactory.toRegex(type, null));
			}
			else if (hasCS) config.setRegex(configServer.getRegex());
			else config.setRegex(RegexFactory.toRegex(RegexFactory.TYPE_PERL, null));

		}
		catch (Exception e) {
			log(config, log, e);
		}

	}

	private static void _loadCompiler(ConfigServerImpl configServer, ConfigImpl config, Document doc, int mode, Log log) {
		try {
			boolean hasCS = configServer != null;

			Element compiler = doc != null ? getChildByName(doc.getDocumentElement(), "compiler") : null;

			// suppress WS between cffunction and cfargument
			if (mode == ConfigPro.MODE_STRICT) {
				config.setSuppressWSBeforeArg(true);
			}
			else {
				String suppress = SystemUtil.getSystemPropOrEnvVar("lucee.suppress.ws.before.arg", null);
				if (StringUtil.isEmpty(suppress, true)) suppress = compiler != null ? getAttr(compiler, "suppress-ws-before-arg") : null;
				if (StringUtil.isEmpty(suppress, true)) suppress = compiler != null ? getAttr(compiler, "supress-ws-before-arg") : null;
				if (!StringUtil.isEmpty(suppress, true)) {
					config.setSuppressWSBeforeArg(Caster.toBooleanValue(suppress, true));
				}
				else if (hasCS) {
					config.setSuppressWSBeforeArg(configServer.getSuppressWSBeforeArg());
				}
			}

			// do dot notation keys upper case
			if (mode == ConfigPro.MODE_STRICT) {
				config.setDotNotationUpperCase(false);
			}
			else {

				// Env Var
				if (!hasCS) {
					Boolean tmp = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.preserve.case", null), null);
					if (tmp != null) {
						config.setDotNotationUpperCase(!tmp.booleanValue());
					}
				}
				String _case = compiler != null ? getAttr(compiler, "dot-notation-upper-case") : null;
				if (!StringUtil.isEmpty(_case, true)) {
					config.setDotNotationUpperCase(Caster.toBooleanValue(_case, true));
				}
				else if (hasCS) {
					config.setDotNotationUpperCase(configServer.getDotNotationUpperCase());
				}
			}

			// full null support
			// if (!hasCS) {
			boolean fns = hasCS ? configServer.getFullNullSupport() : false;
			if (mode == ConfigPro.MODE_STRICT) {
				fns = true;
			}
			else {
				String str = compiler != null ? getAttr(compiler, "full-null-support") : null; // TODO move to an other place, no longer a compiler setting
				if (StringUtil.isEmpty(str, true)) str = SystemUtil.getSystemPropOrEnvVar("lucee.full.null.support", null);

				if (!StringUtil.isEmpty(str, true)) {
					fns = Caster.toBooleanValue(str, hasCS ? configServer.getFullNullSupport() : false);
				}
			}

			// when FNS is true or the lucee dialect is disabled we have no flip flop within a request. FNS is
			// always the same
			/*
			 * if(fns ||
			 * !Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.enable.dialect",null),false)) {
			 * NullSupportHelper.fullNullSupport = fns; NullSupportHelper.simpleMode = true; }
			 */
			config.setFullNullSupport(fns);
			// }

			// default output setting
			String output = compiler != null ? getAttr(compiler, "default-function-output") : null;
			if (!StringUtil.isEmpty(output, true)) {
				config.setDefaultFunctionOutput(Caster.toBooleanValue(output, true));
			}
			else if (hasCS) {
				config.setDefaultFunctionOutput(configServer.getDefaultFunctionOutput());
			}

			// suppress WS between cffunction and cfargument
			String str = compiler != null ? getAttr(compiler, "externalize-string-gte") : null;
			if (Decision.isNumber(str)) {
				config.setExternalizeStringGTE(Caster.toIntValue(str, -1));
			}
			else if (hasCS) {
				config.setExternalizeStringGTE(configServer.getExternalizeStringGTE());
			}

			// allow-lucee-dialect
			if (!hasCS) {
				str = compiler != null ? getAttr(compiler, "allow-lucee-dialect") : null;
				if (str == null || !Decision.isBoolean(str)) str = SystemUtil.getSystemPropOrEnvVar("lucee.enable.dialect", null);
				if (str != null && Decision.isBoolean(str)) {
					config.setAllowLuceeDialect(Caster.toBooleanValue(str, false));
				}
			}
			else {
				config.setAllowLuceeDialect(configServer.allowLuceeDialect());
			}

			// Handle Unquoted Attribute Values As String
			if (mode == ConfigPro.MODE_STRICT) {
				config.setHandleUnQuotedAttrValueAsString(false);
			}
			else {
				str = compiler != null ? getAttr(compiler, "handle-unquoted-attribute-value-as-string") : null;
				if (str != null && Decision.isBoolean(str)) {
					config.setHandleUnQuotedAttrValueAsString(Caster.toBooleanValue(str, true));
				}
				else if (hasCS) {
					config.setHandleUnQuotedAttrValueAsString(configServer.getHandleUnQuotedAttrValueAsString());
				}
			}
		}
		catch (Exception e) {
			log(config, log, e);
		}

	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws IOException
	 * @throws PageException
	 */
	private static void _loadApplication(ConfigServerImpl configServer, ConfigImpl config, Document doc, int mode, Log log) {
		try {
			boolean hasCS = configServer != null;
			boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			Element application = doc != null ? getChildByName(doc.getDocumentElement(), "application") : null;
			Element scope = doc != null ? getChildByName(doc.getDocumentElement(), "scope") : null;

			// Listener type
			ApplicationListener listener;
			if (mode == ConfigPro.MODE_STRICT) {
				listener = new ModernAppListener();
			}
			else {
				String strLT = SystemUtil.getSystemPropOrEnvVar("lucee.listener.type", null);
				if (StringUtil.isEmpty(strLT)) strLT = application == null ? null : getAttr(application, "listener-type");
				listener = ConfigWebUtil.loadListener(strLT, null);

				if (listener == null) {
					if (hasCS && configServer.getApplicationListener() != null) listener = ConfigWebUtil.loadListener(configServer.getApplicationListener().getType(), null);
					if (listener == null) listener = new MixedAppListener();
				}
			}

			String[] strTypes = new String[] { "function", "include", "query", "resource", "http", "file", "webservice" };
			int[] types = new int[] { Config.CACHEDWITHIN_FUNCTION, Config.CACHEDWITHIN_INCLUDE, Config.CACHEDWITHIN_QUERY, Config.CACHEDWITHIN_RESOURCE, Config.CACHEDWITHIN_HTTP,
					Config.CACHEDWITHIN_FILE, Config.CACHEDWITHIN_WEBSERVICE };

			// cachedwithin
			for (int i = 0; i < types.length; i++) {
				String cw = getAttr(application, "cached-within-" + strTypes[i]);
				if (!StringUtil.isEmpty(cw, true)) config.setCachedWithin(types[i], cw);
				else if (hasCS) config.setCachedWithin(types[i], configServer.getCachedWithin(types[i]));
			}

			// Type Checking
			Boolean typeChecking = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.type.checking", null), null);
			if (typeChecking == null) typeChecking = Caster.toBoolean(getAttr(application, "type-checking"), null);
			if (typeChecking != null) config.setTypeChecking(typeChecking.booleanValue());
			else if (hasCS) config.setTypeChecking(configServer.getTypeChecking());

			// cached after
			TimeSpan ts = null;
			if (hasAccess) {
				String ca = getAttr(application, "cached-after");
				if (!StringUtil.isEmpty(ca)) ts = Caster.toTimespan(ca);
			}
			if (ts != null) config.setCachedAfterTimeRange(ts);
			else if (hasCS) config.setCachedAfterTimeRange(configServer.getCachedAfterTimeRange());
			else config.setCachedAfterTimeRange(null);

			// Listener Mode
			String strLM = SystemUtil.getSystemPropOrEnvVar("lucee.listener.mode", null);
			if (StringUtil.isEmpty(strLM)) strLM = getAttr(application, "listener-mode");
			int listenerMode = ConfigWebUtil.toListenerMode(strLM, -1);
			if (listenerMode == -1) {
				if (hasCS) listenerMode = configServer.getApplicationListener() == null ? ApplicationListener.MODE_CURRENT2ROOT : configServer.getApplicationListener().getMode();
				else listenerMode = ApplicationListener.MODE_CURRENT2ROOT;
			}

			listener.setMode(listenerMode);
			config.setApplicationListener(listener);

			// Req Timeout URL
			if (mode == ConfigPro.MODE_STRICT) {
				config.setAllowURLRequestTimeout(false);
			}
			else {
				String allowURLReqTimeout = getAttr(application, "allow-url-requesttimeout");
				if (hasAccess && !StringUtil.isEmpty(allowURLReqTimeout)) {
					config.setAllowURLRequestTimeout(Caster.toBooleanValue(allowURLReqTimeout, false));
				}
				else if (hasCS) config.setAllowURLRequestTimeout(configServer.isAllowURLRequestTimeout());
			}

			// Req Timeout
			ts = null;
			if (hasAccess) {
				String reqTimeoutApplication = getAttr(application, "requesttimeout");
				String reqTimeoutScope = scope == null ? null : getAttr(scope, "requesttimeout"); // deprecated

				if (!StringUtil.isEmpty(reqTimeoutApplication)) ts = Caster.toTimespan(reqTimeoutApplication);
				if (ts == null && !StringUtil.isEmpty(reqTimeoutScope)) ts = Caster.toTimespan(reqTimeoutScope);
			}

			if (ts != null && ts.getMillis() > 0) config.setRequestTimeout(ts);
			else if (hasCS) config.setRequestTimeout(configServer.getRequestTimeout());

			// script-protect
			String strScriptProtect = SystemUtil.getSystemPropOrEnvVar("lucee.script.protect", null);
			if (StringUtil.isEmpty(strScriptProtect)) strScriptProtect = getAttr(application, "script-protect");
			if (hasAccess && !StringUtil.isEmpty(strScriptProtect)) {
				config.setScriptProtect(AppListenerUtil.translateScriptProtect(strScriptProtect));
			}
			else if (hasCS) config.setScriptProtect(configServer.getScriptProtect());

			// classic-date-parsing
			if (config instanceof ConfigServer) {
				if (mode == ConfigPro.MODE_STRICT) {
					DateCaster.classicStyle = true;
				}
				else {
					String strClassicDateParsing = getAttr(application, "classic-date-parsing");

					if (!StringUtil.isEmpty(strClassicDateParsing)) {
						DateCaster.classicStyle = Caster.toBooleanValue(strClassicDateParsing, false);
					}
				}
			}

			// Cache
			Resource configDir = config.getConfigDir();
			String strCacheDirectory = getAttr(application, "cache-directory");
			if (hasAccess && !StringUtil.isEmpty(strCacheDirectory)) {
				strCacheDirectory = ConfigWebUtil.translateOldPath(strCacheDirectory);
				Resource res = ConfigWebUtil.getFile(configDir, strCacheDirectory, "cache", configDir, FileUtil.TYPE_DIR, config);
				config.setCacheDir(res);
			}
			else {
				config.setCacheDir(configDir.getRealResource("cache"));
			}

			String strMax = getAttr(application, "cache-directory-max-size");
			if (hasAccess && !StringUtil.isEmpty(strMax)) {
				config.setCacheDirSize(ByteSizeParser.parseByteSizeDefinition(strMax, config.getCacheDirSize()));
			}
			else if (hasCS) config.setCacheDirSize(configServer.getCacheDirSize());

			// admin sync
			ClassDefinition asc = getClassDefinition(application, "admin-sync-", config.getIdentification());
			if (!asc.hasClass()) asc = getClassDefinition(application, "admin-synchronisation-", config.getIdentification());

			if (hasAccess && asc.hasClass()) {
				try {
					Class clazz = asc.getClazz();
					if (!Reflector.isInstaneOf(clazz, AdminSync.class, false))
						throw new ApplicationException("class [" + clazz.getName() + "] does not implement interface [" + AdminSync.class.getName() + "]");
					config.setAdminSyncClass(clazz);

				}
				catch (Exception e) {
					LogUtil.logGlobal(configServer == null ? config : configServer, XMLConfigWebFactory.class.getName(), e);
				}
			}
			else if (hasCS) config.setAdminSyncClass(configServer.getAdminSyncClass());
		}
		catch (Exception e) {
			log(config, log, e);
		}
	}

	/**
	 * cast a string value to a boolean
	 * 
	 * @param value String value represent a booolean ("yes", "no","true" aso.)
	 * @param defaultValue if can't cast to a boolean is value will be returned
	 * @return boolean value
	 */
	private static boolean toBoolean(String value, boolean defaultValue) {

		if (value == null || value.trim().length() == 0) return defaultValue;

		try {
			return Caster.toBooleanValue(value.trim());
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	public static long toLong(String value, long defaultValue) {

		if (value == null || value.trim().length() == 0) return defaultValue;
		long longValue = Caster.toLongValue(value.trim(), Long.MIN_VALUE);
		if (longValue == Long.MIN_VALUE) return defaultValue;
		return longValue;
	}

	/**
	 * reads an attribute from a xml Element and parses placeholders
	 * 
	 * @param el
	 * @param name
	 * @return
	 */
	public static String getAttr(Element el, String name) {
		if (el == null) return null;
		String v = el.getAttribute(name);
		return replaceConfigPlaceHolder(v);
	}

	public static String replaceConfigPlaceHolder(String v) {
		if (StringUtil.isEmpty(v) || v.indexOf('{') == -1) return v;

		int s = -1, e = -1, d = -1;
		int prefixLen, start = -1, end;
		String _name, _prop;
		while ((s = v.indexOf("{system:", start)) != -1 | /* don't change */
				(e = v.indexOf("{env:", start)) != -1 | /* don't change */
				(d = v.indexOf("${", start)) != -1) {
			boolean isSystem = false, isDollar = false;
			// system
			if (s > -1 && (e == -1 || e > s)) {
				start = s;
				prefixLen = 8;
				isSystem = true;
			}
			// env
			else if (e > -1) {
				start = e;
				prefixLen = 5;
			}
			// dollar
			else {
				start = d;
				prefixLen = 2;
				isDollar = true;
			}

			end = v.indexOf('}', start);
			/*
			 * print.e("----------------"); print.e(s+"-"+e); print.e(v); print.e(start); print.e(end);
			 */
			if (end > prefixLen) {
				_name = v.substring(start + prefixLen, end);
				// print.e(_name);
				if (isDollar) {
					String[] _parts = _name.split(":");
					_prop = SystemUtil.getSystemPropOrEnvVar(_parts[0], (_parts.length > 1) ? _parts[1] : null);
				}
				else {
					_prop = isSystem ? System.getProperty(_name) : System.getenv(_name);
				}

				if (_prop != null) {
					v = new StringBuilder().append(v.substring(0, start)).append(_prop).append(v.substring(end + 1)).toString();
					start += _prop.length();
				}
				else start = end;
			}
			else start = end; // set start to end for the next round
			s = -1;
			e = -1; // reset index
			d = -1; // I don't think we need this?
		}
		return v;
	}

	public static class MonitorTemp {

		public final ActionMonitor am;
		public final String name;
		public final boolean log;

		public MonitorTemp(ActionMonitor am, String name, boolean log) {
			this.am = am;
			this.name = name;
			this.log = log;
		}

	}
}
