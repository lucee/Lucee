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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.xml.sax.SAXException;

import jakarta.servlet.ServletConfig;
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
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.type.cache.CacheResourceProvider;
import lucee.commons.io.res.type.cfml.CFMLResourceProvider;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.type.http.HTTPResourceProvider;
import lucee.commons.io.res.type.http.HTTPSResourceProvider;
import lucee.commons.io.res.type.s3.DummyS3ResourceProvider;
import lucee.commons.io.res.type.zip.ZipResourceProvider;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.retirement.RetireOutputStream;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.URLDecoder;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIEngineFactory;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheConnectionImpl;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.request.RequestCacheHandler;
import lucee.runtime.cache.tag.timespan.TimespanCacheHandler;
import lucee.runtime.cfx.customtag.CFXTagClass;
import lucee.runtime.cfx.customtag.JavaCFXTagClass;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.config.ConfigBase.Startup;
import lucee.runtime.config.component.ComponentFactory;
import lucee.runtime.config.gateway.GatewayMap;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceImpl;
import lucee.runtime.db.JDBCDriver;
import lucee.runtime.db.ParamSyntax;
import lucee.runtime.db.ParamSyntaxImpl;
import lucee.runtime.dump.ClassicHTMLDumpWriter;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.dump.DumpWriterEntry;
import lucee.runtime.dump.HTMLDumpWriter;
import lucee.runtime.dump.SimpleHTMLDumpWriter;
import lucee.runtime.dump.TextDumpWriter;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ConsoleExecutionLog;
import lucee.runtime.engine.DebugExecutionLog;
import lucee.runtime.engine.DebuggerExecutionLog;
import lucee.runtime.engine.ExecutionLog;
import lucee.runtime.engine.ExecutionLogFactory;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalConfig;
import lucee.runtime.engine.ThreadLocalConfigServer;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.engine.ThreadQueueImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.functions.other.CreateUUID;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.gateway.GatewayEntryImpl;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.listener.JavaSettingsImpl;
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
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.http.SSLUtil;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.mail.ServerImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.DummyORMEngine;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMConfigurationImpl;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.reflection.pairs.ConstructorInstance;
import lucee.runtime.regex.Regex;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.search.DummySearchEngine;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecretProvider;
import lucee.runtime.security.SecretProviderFactory;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.dynamic.meta.Constructor;
import lucee.transformer.dynamic.meta.Method;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibException;

public final class ConfigFactoryImpl extends ConfigFactory {

	private static final String TEMPLATE_EXTENSION = "cfm";
	private static final String COMPONENT_EXTENSION = "cfc";
	public static final boolean LOG = true;
	private static final int DEFAULT_MAX_CONNECTION = 100;
	public static final String DEFAULT_LOCATION = Constants.DEFAULT_UPDATE_URL.toExternalForm();
	public static final ClassDefinition<DummyORMEngine> DUMMY_ORM_ENGINE = new ClassDefinitionImpl<DummyORMEngine>(DummyORMEngine.class);

	private static String forceLogAppender = SystemUtil.getSystemPropOrEnvVar("lucee.logging.force.appender", null);
	private static String forceLogLevel = SystemUtil.getSystemPropOrEnvVar("lucee.logging.force.level", null);

	public static ConfigWebPro newInstanceWeb(CFMLEngine engine, CFMLFactoryImpl factory, ConfigServerImpl configServer, ServletConfig servletConfig,
			ConfigWebImpl existingToUpdate) throws PageException {

		Resource configDir = configServer.getConfigDir();
		double start = SystemUtil.millis();
		ConfigWebPro configWeb = existingToUpdate != null ? existingToUpdate.setInstance(factory, configServer, servletConfig, true)
				: new ConfigWebImpl(factory, configServer, servletConfig);
		factory.setConfig(configServer, configWeb);

		((ThreadQueueImpl) configWeb.getThreadQueue()).setMode(configWeb.getQueueEnable() ? ThreadQueue.MODE_ENABLED : ThreadQueue.MODE_DISABLED);

		// call web.cfc for this context
		((CFMLEngineImpl) ConfigUtil.getEngine(configWeb)).onStart(configWeb, false);

		((GatewayEngineImpl) configWeb.getGatewayEngine()).autoStart();

		// invoke config listener if set
		try {
			ConfigListener listener = configServer.getConfigListener();
			if (listener != null) listener.onLoadWebContext(configServer, configWeb);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(configServer, t);
		}

		log(configServer, Log.LEVEL_INFO,
				"\n===================================================================\n" + "WEB CONTEXT (" + createLabel(configServer, servletConfig) + ")\n"
						+ "-------------------------------------------------------------------\n" + "- config:" + configDir + "\n" + "- webroot:"
						+ ReqRspUtil.getRootPath(servletConfig.getServletContext()) + "\n" + "- label:" + createLabel(configServer, servletConfig) + "\n" + "- start-time:"
						+ Caster.toString(Math.round(SystemUtil.millis() - start)) + " ms\n" + "===================================================================\n"

		);
		return configWeb;
	}

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
	public static ConfigServerImpl newInstanceServer(CFMLEngineImpl engine, Map<String, CFMLFactory> initContextes, Map<String, CFMLFactory> contextes, Resource configDir,
			ConfigServerImpl existing, boolean essentialOnly)
			throws SAXException, ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException, ConverterException {
		if (ThreadLocalPageContext.insideServerNewInstance()) throw new ApplicationException("already inside server.newInstance");
		try {
			double start = SystemUtil.millis();
			ThreadLocalPageContext.insideServerNewInstance(true);
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

			UpdateInfo ui = getNew(engine, configDir, essentialOnly, UpdateInfo.NEW_NONE);
			boolean doNew = ui.updateType != NEW_NONE;

			Resource configFileOld = configDir.getRealResource("lucee-server.xml");

			// config file
			Resource configFileNew = getConfigFile(configDir, true, false);

			boolean hasConfigOld = false;
			boolean hasConfigNew = configFileNew.exists() && configFileNew.length() > 0;

			if (!hasConfigNew) {
				LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(),
						"has no json server context config [" + configFileNew + "]");
				hasConfigOld = configFileOld.exists() && configFileOld.length() > 0;
				LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(),
						"has " + (hasConfigOld ? "" : "no ") + "xml server context config [" + configFileOld + "]");
			}
			ConfigServerImpl config = existing != null ? existing : new ConfigServerImpl(engine, initContextes, contextes, configDir, configFileNew, ui, essentialOnly, doNew);
			ThreadLocalConfigServer.register(config);
			// translate to new
			if (!hasConfigNew) {
				if (hasConfigOld) {
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "convert server context xml config to json");
					try {
						translateConfigFile(config, configFileOld, configFileNew, "multi", true);
					}
					catch (IOException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigFactoryImpl.class.getName(), e);
						throw e;
					}
					catch (ConverterException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigFactoryImpl.class.getName(), e);
						throw e;
					}
					catch (SAXException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigFactoryImpl.class.getName(), e);
						throw e;
					}
				}
				// create config file
				else {
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(),
							"create new server context json config file [" + configFileNew + "]");
					ConfigFile.createConfigFile("server", configFileNew);
					hasConfigNew = true;
				}
			}
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "load config file");
			Struct root = loadDocumentCreateIfFails(config, configFileNew, "server");
			config.setRoot(root);
			config.resetScheduledTasks();
			// admin mode
			load(config, root, false, doNew, essentialOnly);
			SSLUtil.init( Paths.get( config.getConfigDir().getAbsolutePath(), "security" ) );

			if (!essentialOnly) {
				createContextFiles(configDir, config, doNew);
				((CFMLEngineImpl) ConfigUtil.getEngine(config)).onStart(config, false);
			}
			log(config, Log.LEVEL_INFO,
					"\n===================================================================\n" + "SERVER CONTEXT\n"
							+ "-------------------------------------------------------------------\n" + "- config:" + configDir + "\n" + "- loader-version:"
							+ SystemUtil.getLoaderVersion() + "\n" + "- core-version:" + engine.getInfo().getVersion() + "\n" + "- start-time:"
							+ Caster.toString(Math.round(SystemUtil.millis() - start)) + " ms\n" + "===================================================================\n"

			);

			return config;
		}
		finally {
			ThreadLocalPageContext.insideServerNewInstance(false);
			ThreadLocalConfigServer.release();
		}
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
		boolean quick = CFMLEngineImpl.quick(engine);
		Resource configFile = configServer.getConfigFile();
		if (configFile == null) return;
		if (second(configServer.getLoadTime()) > second(configFile.lastModified())) {
			if (!configServer.getConfigDir().getRealResource("password.txt").isFile()) return;
		}
		int iDoNew = getNew(engine, configServer.getConfigDir(), quick, UpdateInfo.NEW_NONE).updateType;
		boolean doNew = iDoNew != NEW_NONE;
		Struct root = loadDocumentCreateIfFails(null, configFile, "server");
		configServer.setRoot(root);
		configServer.resetScheduledTasks();
		load(configServer, root, true, doNew, quick);
		((CFMLEngineImpl) ConfigUtil.getEngine(configServer)).onStart(configServer, true);
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
	synchronized static void load(ConfigServerImpl config, Struct root, boolean isReload, boolean doNew, boolean essentialOnly) throws IOException {
		ConfigBase.onlyFirstMatch = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.mapping.first", null), true); // changed behaviour in 6.0
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "start reading config");
		ThreadLocalConfig.register(config);
		boolean reload = false;
		// load PW
		try {
			if (createSaltAndPW(root, config, essentialOnly)) reload = true;
			if (LOG) log(config, Log.LEVEL_INFO, "set salt");

			// reload when an old version of xml got updated
			if (reload) {
				root = reload(root, config, null);
				reload = false;
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}

		config.setLastModified();

		if (LOG) log(config, Log.LEVEL_INFO, "loaded filesystem");
		if (!essentialOnly) {
			_loadExtensionBundles(config, root);
			if (LOG) log(config, Log.LEVEL_INFO, "loaded extension");
		}

		if (!essentialOnly) {
			((CFMLEngineImpl) config.getEngine()).touchMonitor(config);
		}
		// Trigger startup hooks (lazy-loaded)
		config.getStartups();

		// Eagerly initialise the ExecutionLogFactory so its marker check + cfclasses
		// purge (loadExeLog) runs BEFORE any request can reach PageSourceImpl.loadPhysical.
		// Without this, loadExeLog runs lazily inside the first compile, by which point
		// path 2 has already defined stale-version classes into the fresh PCL —
		// purging cfclasses on disk then can't unload them and the rename storm runs.
		config.getExecutionLogFactory();

		config.setLoadTime(System.currentTimeMillis());
	}

	private static String createLabel(ConfigServerImpl configServer, ServletConfig servletConfig) {
		String hash = SystemUtil.hash(servletConfig.getServletContext());
		Map<String, String> labels = configServer.getLabels();
		String label = null;
		if (labels != null) {
			label = labels.get(hash);
		}
		if (label == null) label = hash;
		return label;
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
	 */ // MUST
	public static void reloadInstance(CFMLEngine engine, ConfigServerImpl cs, ConfigWebImpl cwi, boolean force)
			throws PageException, IOException, TagLibException, FunctionLibException, BundleException {

		cwi.reload();
		return;
	}

	private static boolean createSaltAndPW(Struct root, ConfigServerImpl config, boolean essentialOnly) {
		if (root == null) return false;

		// salt
		String salt = getAttr(null, root, "adminSalt");
		if (StringUtil.isEmpty(salt, true)) salt = getAttr(config, root, "salt");
		boolean rtn = false;
		if (StringUtil.isEmpty(salt, true) || !Decision.isUUId(salt)) {
			// create salt
			root.setEL("salt", salt = CreateUUID.invoke());
			rtn = true;
		}

		// no password yet
		if (!essentialOnly && StringUtil.isEmpty(root.get("hspw", ""), true) && StringUtil.isEmpty(root.get("adminhspw", ""), true) && StringUtil.isEmpty(root.get("pw", ""), true)
				&& StringUtil.isEmpty(root.get("adminpw", ""), true) && StringUtil.isEmpty(root.get("password", ""), true)
				&& StringUtil.isEmpty(root.get("adminpassword", ""), true)) {
			Resource pwFile = config.getConfigDir().getRealResource("password.txt");
			if (pwFile.isFile()) {
				try {
					String pw = IOUtil.toString(pwFile, (Charset) null);
					if (!StringUtil.isEmpty(pw, true)) {
						pw = pw.trim();
						String hspw = new PasswordImpl(Password.ORIGIN_UNKNOW, pw, salt).getPassword();
						root.setEL("hspw", hspw);
						pwFile.delete();
						rtn = true;
					}
				}
				catch (IOException e) {
					LogUtil.logGlobal(config, "application", e);
				}
			}
			else {
				LogUtil.log(config, Log.LEVEL_DEBUG, "application", "no password set and no password file found at [" + pwFile + "]");
			}
		}
		return rtn;
	}

	private static Struct reload(Struct root, ConfigImpl config, ConfigServerImpl cs) throws IOException, ConverterException {
		// store as json

		root = ConfigFile.reload(config.getConfigFile(), root);
		return root;
	}

	public static ResourceProvider loadDefaultResourceProvider(ConfigImpl config, Struct root) {
		try {
			Array defaultProviders = ConfigUtil.getAsArray("defaultResourceProvider", root);

			// Default Resource Provider
			if (defaultProviders != null && defaultProviders.size() > 0) {
				Struct defaultProvider = Caster.toStruct(defaultProviders.getE(defaultProviders.size()));
				ClassDefinition defProv = getClassDefinition(config, defaultProvider, "", config.getIdentification());

				String strDefaultProviderComponent = getAttr(config, defaultProvider, "component");
				if (StringUtil.isEmpty(strDefaultProviderComponent)) strDefaultProviderComponent = getAttr(config, defaultProvider, "class");

				// class
				if (defProv.hasClass()) {
					return toDefaultResourceProvider(defProv.getClazz(), toArguments(defaultProvider, "arguments", true, false));
				}

				// component
				else if (!StringUtil.isEmpty(strDefaultProviderComponent)) {
					strDefaultProviderComponent = strDefaultProviderComponent.trim();
					Map<String, String> args = toArguments(defaultProvider, "arguments", true, false);
					args.put("component", strDefaultProviderComponent);
					return toDefaultResourceProvider(CFMLResourceProvider.class, args);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return null;
	}

	private static ResourceProvider toDefaultResourceProvider(Class defaultProviderClass, Map arguments) throws ClassException {
		Object o = ClassUtil.loadInstance(defaultProviderClass);
		if (o instanceof ResourceProvider) {
			ResourceProvider rp = (ResourceProvider) o;
			rp.init(null, arguments);
			return rp;
		}
		else throw new ClassException("object [" + Caster.toClassName(o) + "] must implement the interface " + ResourceProvider.class.getName());
	}

	public static void loadResourceProvider(ConfigImpl config, Struct root) {
		try {
			Array providers = ConfigUtil.getAsArray("resourceProviders", root);
			// Resource Provider
			boolean hasHTTP = false;
			boolean hasHTTPs = false;
			boolean hasRAM = false;
			boolean hasZip = false;
			boolean hasS3 = false;

			if (providers != null && providers.size() > 0) {
				ClassDefinition prov;
				String strProviderCFC;
				String strProviderScheme;
				Iterator<?> pit = providers.getIterator();
				Struct provider;
				String className;
				while (pit.hasNext()) {
					provider = Caster.toStruct(pit.next(), null);
					if (provider == null) continue;
					try {

						// ignore FTP (no longer supported)
						className = getAttr(config, provider, "class");
						if ("lucee.commons.io.res.type.ftp.FTPResourceProvider".equals(className)) continue;
						//

						prov = getClassDefinition(config, provider, "", config.getIdentification());
						strProviderCFC = getAttr(config, provider, "component");
						if (StringUtil.isEmpty(strProviderCFC)) strProviderCFC = getAttr(config, provider, "class");

						strProviderScheme = getAttr(config, provider, "scheme");
						// class
						if (prov.hasClass() && !StringUtil.isEmpty(strProviderScheme)) {
							strProviderScheme = strProviderScheme.trim().toLowerCase();
							config.addResourceProvider(strProviderScheme, prov, toArguments(provider, "arguments", true, false));

							// patch for user not having
							if ("http".equalsIgnoreCase(strProviderScheme)) hasHTTP = true;
							else if ("https".equalsIgnoreCase(strProviderScheme)) hasHTTPs = true;
							else if ("ram".equalsIgnoreCase(strProviderScheme)) hasRAM = true;
							else if ("s3".equalsIgnoreCase(strProviderScheme)) hasS3 = true;
							else if ("zip".equalsIgnoreCase(strProviderScheme)) hasZip = true;

						}

						// cfc
						else if (!StringUtil.isEmpty(strProviderCFC) && !StringUtil.isEmpty(strProviderScheme)) {
							strProviderCFC = strProviderCFC.trim();
							strProviderScheme = strProviderScheme.trim().toLowerCase();
							Map<String, String> args = toArguments(provider, "arguments", true, false);
							args.put("component", strProviderCFC);
							config.addResourceProvider(strProviderScheme, new ClassDefinitionImpl(CFMLResourceProvider.class), args);
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}
			// adding zip when not exist
			if (!hasHTTP) {
				Map<String, String> args = new HashMap<>();
				args.put("lock-timeout", "10000");
				args.put("case-sensitive", "false");
				config.addResourceProvider("http", new ClassDefinitionImpl<>(HTTPResourceProvider.class), args);
			}
			if (!hasHTTPs) {
				Map<String, String> args = new HashMap<>();
				args.put("lock-timeout", "10000");
				args.put("case-sensitive", "false");
				config.addResourceProvider("https", new ClassDefinitionImpl<>(HTTPSResourceProvider.class), args);
			}
			if (!hasRAM) {
				Map<String, String> args = new HashMap<>();
				args.put("lock-timeout", "1000");
				args.put("case-sensitive", "true");
				config.addResourceProvider("tar", new ClassDefinitionImpl<>(CacheResourceProvider.class), args);
			}
			if (!hasS3) {
				ClassDefinition s3Class = new ClassDefinitionImpl(DummyS3ResourceProvider.class);
				Map<String, String> args = new HashMap<>();
				args.put("lock-timeout", "10000");
				config.addResourceProvider("s3", s3Class, args);
			}
			if (!hasZip) {
				Map<String, String> args = new HashMap<>();
				args.put("lock-timeout", "1000");
				args.put("case-sensitive", "1000");
				config.addResourceProvider("zip", new ClassDefinitionImpl<>(ZipResourceProvider.class), args);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
	}

	public static <T> ClassDefinition<T> getClassDefinition(Config config, Struct data, String prefix, Identification id) throws PageException {
		String attrName;
		String cn;

		if (StringUtil.isEmpty(prefix)) {
			cn = getAttr(config, data, "class");
			attrName = "class";
		}
		else {
			if (prefix.endsWith("-")) prefix = prefix.substring(0, prefix.length() - 1);
			cn = getAttr(config, data, prefix + "Class");
			attrName = prefix + "Class";
		}

		// proxy jar library no longer provided, so if still this class name is used ....
		if (cn != null && "com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cn)) {
			data.set(attrName, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		}

		ClassDefinition<T> cd = ClassDefinitionImpl.toClassDefinitionImpl(data, prefix, true, id);
		return cd;
	}

	public static HashMap<String, Class<CacheHandler>> loadCacheHandler(ConfigImpl config, Struct root) {
		HashMap<String, Class<CacheHandler>> cacheHandlers = new HashMap<String, Class<CacheHandler>>();

		try {

			// first of all we make sure we have a request and timespan cachehandler
			addCacheHandler(cacheHandlers, "request", new ClassDefinitionImpl(RequestCacheHandler.class));
			addCacheHandler(cacheHandlers, "timespan", new ClassDefinitionImpl(TimespanCacheHandler.class));

			Struct handlers = ConfigUtil.getAsStruct("cacheHandlers", root);
			if (handlers != null) {
				ClassDefinition cd;
				String strId;
				Iterator<Entry<Key, Object>> it = handlers.entryIterator();
				Entry<Key, Object> entry;
				Struct handler;
				while (it.hasNext()) {
					try {
						entry = it.next();

						handler = Caster.toStruct(entry.getValue(), null);
						if (handler == null) continue;

						cd = getClassDefinition(config, handler, "", config.getIdentification());
						strId = entry.getKey().getString();
						if (cd.hasClass() && !StringUtil.isEmpty(strId)) {
							strId = strId.trim().toLowerCase();
							try {
								addCacheHandler(cacheHandlers, strId, cd);
							}
							catch (Throwable t) {
								ExceptionUtil.rethrowIfNecessary(t);
								log(config, t);
							}
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}
		}
		catch (Throwable th) {
			ExceptionUtil.rethrowIfNecessary(th);
			log(config, th);
		}
		return cacheHandlers;
	}

	private static void addCacheHandler(HashMap<String, Class<CacheHandler>> cacheHandlers, String id, ClassDefinition<CacheHandler> cd) throws ClassException, BundleException {
		Class<CacheHandler> clazz = cd.getClazz();
		Object o = ClassUtil.loadInstance(clazz); // just try to load and forget afterwards
		if (o instanceof CacheHandler) {
			cacheHandlers.put(id, clazz);
		}
		else throw new ClassException("object [" + Caster.toClassName(o) + "] must implement the interface " + CacheHandler.class.getName());
	}

	public static Map<String, AIEngine> loadAI(ConfigImpl config, Struct root, Map<String, AIEngine> defaultValue) {
		try {
			// we only load this for the server context
			Struct ai = ConfigUtil.getAsStruct(config, root, false, "ai");
			if (ai != null) {
				return _loadAI(config, ai);
			}
		}
		catch (Exception ex) {
			log(config, ex);
		}
		return defaultValue;
	}

	public static Map<String, AIEngine> _loadAI(Config config, Struct ai) {
		String strId;
		Iterator<Entry<Key, Object>> it = ai.entryIterator();
		Entry<Key, Object> entry;
		Struct data;
		Map<String, AIEngine> engines = new HashMap<>();

		while (it.hasNext()) {
			try {
				entry = it.next();
				data = Caster.toStruct(entry.getValue(), null);
				if (data == null) continue;
				strId = entry.getKey().getString();
				if (!StringUtil.isEmpty(strId)) {
					data = (Struct) ConfigUtil.replaceConfigPlaceHolders(config, data);
					strId = strId.trim().toLowerCase();
					engines.put(strId, AIEngineFactory.getInstance(config, strId, data));
				}
			}
			catch (Exception e) {
				log(config, e);
			}
		}
		return engines;
	}

	public static Map<String, SecretProvider> loadSecretProviders(ConfigImpl config, Struct root, Map<String, SecretProvider> defaultValue) {
		try {
			// we only load this for the server context
			// we do not give config here as first argument, to prevent a infiniti loop
			Struct secretProvider = ConfigUtil.getAsStruct(null, root, false, "secretProvider");
			if (secretProvider != null) {
				return _loadSecretProviders(config, secretProvider);
			}
		}
		catch (Exception ex) {
			log(config, ex);
		}
		return defaultValue;
	}

	public static Map<String, SecretProvider> _loadSecretProviders(Config config, Struct secretProvider) {
		String strId;
		Iterator<Entry<Key, Object>> it = secretProvider.entryIterator();
		Entry<Key, Object> entry;
		Struct data;
		Map<String, SecretProvider> providers = new HashMap<>();

		while (it.hasNext()) {
			try {
				entry = it.next();
				data = Caster.toStruct(entry.getValue(), null);
				if (data == null) continue;
				strId = entry.getKey().getString();
				if (!StringUtil.isEmpty(strId)) {
					strId = strId.trim().toLowerCase();
					providers.put(strId.toLowerCase(), SecretProviderFactory.getInstance(config, strId, data));
				}
			}
			catch (Exception e) {
				log(config, e);
			}
		}
		return providers;
	}

	public static DumpWriterEntry[] loadDumpWriter(ConfigImpl config, Struct root, DumpWriterEntry[] defaultValue) {
		try {
			Array writers = ConfigUtil.getAsArray("dumpWriters", root);

			Struct sct = new StructImpl();

			boolean hasPlain = false;
			boolean hasRich = false;

			if (writers != null && writers.size() > 0) {
				ClassDefinition cd;
				String strName;
				String strDefault;
				Class clazz;
				int def = HTMLDumpWriter.DEFAULT_NONE;
				Iterator<?> it = writers.getIterator();
				Struct writer;
				while (it.hasNext()) {
					try {
						writer = Caster.toStruct(it.next(), null);
						if (writer == null) continue;

						cd = getClassDefinition(config, writer, "", config.getIdentification());
						strName = getAttr(config, writer, "name");
						strDefault = getAttr(config, writer, "default");
						clazz = cd.getClazz(null);
						if (clazz != null && !StringUtil.isEmpty(strName)) {
							if (StringUtil.isEmpty(strDefault)) def = HTMLDumpWriter.DEFAULT_NONE;
							else if ("browser".equalsIgnoreCase(strDefault)) def = HTMLDumpWriter.DEFAULT_RICH;
							else if ("console".equalsIgnoreCase(strDefault)) def = HTMLDumpWriter.DEFAULT_PLAIN;
							sct.put(strName, new DumpWriterEntry(def, strName, (DumpWriter) ClassUtil.loadInstance(clazz)));
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
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
			return entries.toArray(new DumpWriterEntry[entries.size()]);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
	}

	static Map<String, String> toArguments(Struct coll, String name, boolean decode, boolean lowerKeys) throws PageException {
		Map<String, String> map = new HashMap<>();
		Object obj = coll.get(name, null);
		if (obj == null) return map;

		if (Decision.isStruct(obj)) {
			Iterator<Entry<Key, Object>> it = Caster.toStruct(obj).entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				map.put(lowerKeys ? e.getKey().getLowerString() : e.getKey().getString(), Caster.toString(e.getValue())); // TODO remove need to cast to string
			}
		}
		if (Decision.isString(obj)) {
			String[] arr = ListUtil.toStringArray(ListUtil.listToArray(Caster.toString(obj), ';'), null);

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
		return map;
	}

	@Deprecated
	public static Struct cssStringToStruct(String attributes, boolean decode, boolean lowerKeys) {
		Struct sct = new StructImpl();
		if (StringUtil.isEmpty(attributes, true)) return sct;
		String[] arr = ListUtil.toStringArray(ListUtil.listToArray(attributes, ';'), null);

		int index;
		String str;
		for (int i = 0; i < arr.length; i++) {
			str = arr[i].trim();
			if (StringUtil.isEmpty(str)) continue;
			index = str.indexOf(':');
			if (index == -1) sct.setEL(lowerKeys ? str.toLowerCase() : str, "");
			else {
				String k = dec(str.substring(0, index).trim(), decode);
				if (lowerKeys) k = k.toLowerCase();
				sct.setEL(k, dec(str.substring(index + 1).trim(), decode));
			}
		}
		return sct;
	}

	private static String dec(String str, boolean decode) {
		if (!decode) return str;
		return URLDecoder.decode(str, false);
	}

	public static ConfigListener loadListener(ConfigServerImpl config, Struct root, ConfigListener defaultValue) {
		try {
			Struct listener = ConfigUtil.getAsStruct("listener", root);
			ClassDefinition cd = listener != null ? getClassDefinition(config, listener, "", config.getIdentification()) : null;
			String strArguments = getAttr(config, listener, "arguments");
			if (strArguments == null) strArguments = "";

			if (cd != null && cd.hasClass()) {
				try {
					Object obj = ClassUtil.loadInstance(cd.getClazz(), new Object[] { strArguments }, null);
					if (obj instanceof ConfigListener) {
						ConfigListener cl = (ConfigListener) obj;
						return cl;
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
	}

	public static IdentificationServerImpl loadId(ConfigServerImpl config, Struct root, Log log, IdentificationServerImpl defaultValue) {
		try {

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
				LogUtil.logGlobal(config, ConfigFactoryImpl.class.getName(), ioe);
			}
			if (StringUtil.isEmpty(securityKey)) securityKey = UUID.randomUUID().toString();

			// API Key
			String apiKey = null;
			String str = root != null ? getAttr(config, root, "apiKey") : null;
			if (!StringUtil.isEmpty(str, true)) apiKey = str.trim();
			return new IdentificationServerImpl(config, securityKey, apiKey);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			LogUtil.logGlobal(config, ConfigFactoryImpl.class.getName(), t);
		}
		return defaultValue;
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 */
	public static int loadSecurity(ConfigImpl config, Struct root) {
		try {
			Struct security = ConfigUtil.getAsStruct("security", root);
			int vu = ConfigPro.QUERY_VAR_USAGE_UNDEFINED;
			if (security != null) {
				vu = AppListenerUtil.toVariableUsage(getAttr(config, security, "variableUsage"), ConfigPro.QUERY_VAR_USAGE_UNDEFINED);
				if (vu == ConfigPro.QUERY_VAR_USAGE_UNDEFINED) vu = AppListenerUtil.toVariableUsage(getAttr(config, security, "varUsage"), ConfigPro.QUERY_VAR_USAGE_UNDEFINED);
			}
			if (vu == ConfigPro.QUERY_VAR_USAGE_UNDEFINED) {
				vu = ConfigPro.QUERY_VAR_USAGE_IGNORE;
			}
			return vu;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			t.printStackTrace();
			log(config, t);
		}
		return ConfigPro.QUERY_VAR_USAGE_IGNORE;
	}

	private static Resource[] _loadFileAccess(Config config, Array fileAccesses) {
		if (fileAccesses.size() == 0) return new Resource[0];
		java.util.List<Resource> reses = new ArrayList<Resource>();
		String path;
		Resource res;
		Iterator<?> it = fileAccesses.getIterator();
		Struct fa;
		while (it.hasNext()) {
			try {
				fa = Caster.toStruct(it.next(), null);
				if (fa == null) continue;

				path = getAttr(config, fa, "path");
				if (!StringUtil.isEmpty(path)) {
					res = config.getResource(path);
					if (res.isDirectory()) reses.add(res);
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log(config, t);
			}
		}
		// temp directory should be always accessible, even when access is local
		Resource tempDir = config.getTempDirectory();
		if (!reses.contains(tempDir)) reses.add(tempDir);
		return reses.toArray(new Resource[reses.size()]);
	}

	private static SecurityManagerImpl _toSecurityManager(Config config, Struct el) {
		SecurityManagerImpl sm = new SecurityManagerImpl(_attr(config, el, "setting", SecurityManager.VALUE_YES), _attr(config, el, "file", SecurityManager.VALUE_ALL),
				_attr(config, el, "direct_java_access", SecurityManager.VALUE_YES), _attr(config, el, "mail", SecurityManager.VALUE_YES),
				_attr(config, el, "datasource", SecurityManager.VALUE_YES), _attr(config, el, "mapping", SecurityManager.VALUE_YES),
				_attr(config, el, "remote", SecurityManager.VALUE_YES), _attr(config, el, "custom_tag", SecurityManager.VALUE_YES),
				_attr(config, el, "cfx_setting", SecurityManager.VALUE_YES), _attr(config, el, "cfx_usage", SecurityManager.VALUE_YES),
				_attr(config, el, "debugging", SecurityManager.VALUE_YES), _attr(config, el, "search", SecurityManager.VALUE_YES),
				_attr(config, el, "scheduled_task", SecurityManager.VALUE_YES), _attr(config, el, "tag_execute", SecurityManager.VALUE_YES),
				_attr(config, el, "tag_import", SecurityManager.VALUE_YES), _attr(config, el, "tag_object", SecurityManager.VALUE_YES),
				_attr(config, el, "tag_registry", SecurityManager.VALUE_YES), _attr(config, el, "cache", SecurityManager.VALUE_YES),
				_attr(config, el, "gateway", SecurityManager.VALUE_YES), _attr(config, el, "orm", SecurityManager.VALUE_YES),
				_attr2(config, el, "access_read", SecurityManager.ACCESS_PROTECTED), _attr2(config, el, "access_write", SecurityManager.ACCESS_PROTECTED));
		Array fileAccess = ConfigUtil.getAsArray("fileAccess", el);
		if (fileAccess.size() > 0) sm.setCustomFileAccess(_loadFileAccess(config, fileAccess));
		return sm;
	}

	public static SecurityManagerImpl _toSecurityManagerSingle(Config config, Struct el) {
		SecurityManagerImpl sm = (SecurityManagerImpl) SecurityManagerImpl.getOpenSecurityManager();
		sm.setAccess(SecurityManager.TYPE_ACCESS_READ, _attr2(config, el, "access_read", SecurityManager.ACCESS_PROTECTED));
		sm.setAccess(SecurityManager.TYPE_ACCESS_WRITE, _attr2(config, el, "access_write", SecurityManager.ACCESS_PROTECTED));
		sm.setAccess(SecurityManager.TYPE_REMOTE, _attr(config, el, "remote", SecurityManager.VALUE_YES));
		sm.setAccess(SecurityManager.TYPE_FILE, _attr(config, el, "file", SecurityManager.VALUE_ALL));
		sm.setAccess(SecurityManager.TYPE_TAG_EXECUTE, _attr(config, el, "tag_execute", SecurityManager.VALUE_YES));
		sm.setAccess(SecurityManager.TYPE_TAG_IMPORT, _attr(config, el, "tag_import", SecurityManager.VALUE_YES));
		sm.setAccess(SecurityManager.TYPE_TAG_OBJECT, _attr(config, el, "tag_object", SecurityManager.VALUE_YES));
		sm.setAccess(SecurityManager.TYPE_TAG_REGISTRY, _attr(config, el, "tag_registry", SecurityManager.VALUE_YES));
		sm.setAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS, _attr(config, el, "direct_java_access", SecurityManager.VALUE_YES));
		sm.setAccess(SecurityManager.TYPE_CFX_USAGE, _attr(config, el, "cfx_usage", SecurityManager.VALUE_YES));
		Array fileAccess = ConfigUtil.getAsArray("fileAccess", el);
		if (fileAccess.size() > 0) sm.setCustomFileAccess(_loadFileAccess(config, fileAccess));
		return sm;
	}

	private static short _attr(Config config, Struct el, String attr, short _default) {
		return SecurityManagerImpl.toShortAccessValue(getAttr(config, el, attr), _default);
	}

	private static short _attr2(Config config, Struct el, String attr, short _default) {
		String strAccess = getAttr(config, el, attr);
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
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, ConfigFactoryImpl.class.getName(), resource);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, ConfigFactoryImpl.class.getName(), file + "");
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigFactoryImpl.class.getName(), t);
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

			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigFactoryImpl.class.getName(), "update file:" + file);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigFactoryImpl.class.getName(), " - source:" + srcSize);
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigFactoryImpl.class.getName(), " - target:" + trgSize);

		}
		else file.createNewFile();
		IOUtil.copy(new ByteArrayInputStream(barr), file, true);
	}

	public static String doCheckChangesInLibraries(ConfigServerImpl config) {
		// create current hash from libs
		TagLib[] tlds = config.getTLDs();
		FunctionLib flds = config.getFLDs();

		StringBuilder sb = new StringBuilder();

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

		// preserve Case
		sb.append(config.preserveCase());
		sb.append(';');

		// full null support
		// sb.append(config.getFull Null Support()); // no longer a compiler switch
		// sb.append(';');

		// fusiondebug or not (FD uses full path name)
		sb.append(config.allowRequestTimeout());
		sb.append(';');

		// tld
		for (int i = 0; i < tlds.length; i++) {
			sb.append(tlds[i].getHash());
		}
		// fld
		sb.append(flds.getHash());

		return HashUtil.create64BitHashAsString(sb.toString());
	}

	public static void flushPageSourcePool(Mapping... mappings) {
		if (mappings != null) for (int i = 0; i < mappings.length; i++) {
			mappings[i].flush();
		}
	}

	public static void flushPageSourcePool(Collection<Mapping> mappings) {
		Iterator<Mapping> it = mappings.iterator();
		Mapping m;
		while (it.hasNext()) {
			m = it.next();
			m.flush();
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
	 * @return
	 * @throws IOException
	 */
	public static Mapping[] loadMappings(ConfigImpl config, Struct root) {
		Map<String, Mapping> mappings = MapFactory.<String, Mapping>getConcurrentMap();
		try {
			Struct _mappings = Caster.toStruct(root.get("mappings", null), null);
			if (_mappings == null) _mappings = Caster.toStruct(root.get("CFMappings", null), null);
			if (_mappings == null) _mappings = ConfigUtil.getAsStruct("mappings", root);
			else {
				root.setEL("mappings", _mappings);
			}

			// alias CFMappings

			Mapping tmp;

			boolean finished = true;
			boolean hasServerContext = false;
			boolean hasWebContext = false;
			if (_mappings != null) {
				Iterator<Entry<Key, Object>> it = _mappings.entryIterator();
				Entry<Key, Object> e;
				Struct el;
				while (it.hasNext()) {
					try {
						e = it.next();
						el = Caster.toStruct(e.getValue(), null);
						if (el == null) continue;

						String virtual = e.getKey().getString();
						String physical = getAttr(config, el, "physical");
						String archive = getAttr(config, el, "archive");
						String strListType = getAttr(config, el, "listenerType");
						if (StringUtil.isEmpty(strListType)) strListType = getAttr(config, el, "listener-type");
						if (StringUtil.isEmpty(strListType)) strListType = getAttr(config, el, "listenertype");

						String strListMode = getAttr(config, el, "listenerMode");
						if (StringUtil.isEmpty(strListMode)) strListMode = getAttr(config, el, "listener-mode");
						if (StringUtil.isEmpty(strListMode)) strListMode = getAttr(config, el, "listenermode");

						boolean readonly = toBoolean(getAttr(config, el, "readonly"), false);
						boolean hidden = toBoolean(getAttr(config, el, "hidden"), false);
						boolean toplevel = toBoolean(getAttr(config, el, "toplevel"), true);

						{
							if ("/lucee-server/".equalsIgnoreCase(virtual) || "/lucee-server-context/".equalsIgnoreCase(virtual)) {
								hasServerContext = true;
							}
							else if ("/lucee/".equalsIgnoreCase(virtual)) {
								hasWebContext = true;
							}
						}

						// lucee
						if ("/lucee/".equalsIgnoreCase(virtual)) {
							if (StringUtil.isEmpty(strListType, true)) strListType = "modern";
							if (StringUtil.isEmpty(strListMode, true)) strListMode = "curr2root";
							toplevel = true;
						}

						int listenerMode = ConfigUtil.toListenerMode(strListMode, -1);
						int listenerType = ConfigUtil.toListenerType(strListType, -1);
						ApplicationListener listener = ConfigUtil.loadListener(listenerType, null);
						if (listener != null || listenerMode != -1) {
							// type
							if (listener == null) listener = ConfigUtil.loadListener(ConfigUtil.toListenerType(config.getApplicationListener().getType(), -1), null);
							if (listener == null) listener = new ModernAppListener();

							// mode
							if (listenerMode == -1) {
								listenerMode = config.getApplicationListener().getMode();
							}
							listener.setMode(listenerMode);

						}

						// physical!=null &&
						if ((physical != null || archive != null)) {

							short insTemp = inspectTemplate(config, el);

							int insTempSlow = Caster.toIntValue(getAttr(config, el, "inspectTemplateIntervalSlow"), ConfigPro.INSPECT_INTERVAL_UNDEFINED);
							int insTempFast = Caster.toIntValue(getAttr(config, el, "inspectTemplateIntervalFast"), ConfigPro.INSPECT_INTERVAL_UNDEFINED);

							if ("/lucee/".equalsIgnoreCase(virtual) || "/lucee".equalsIgnoreCase(virtual) || "/lucee-server/".equalsIgnoreCase(virtual)
									|| "/lucee-server-context".equalsIgnoreCase(virtual))
								insTemp = ConfigPro.INSPECT_AUTO;

							String primary = getAttr(config, el, "primary");
							boolean physicalFirst = primary == null || !"archive".equalsIgnoreCase(primary);

							tmp = new MappingImpl(config, virtual, physical, archive, insTemp, insTempSlow, insTempFast, physicalFirst, hidden, readonly, toplevel, false, false,
									listener, listenerMode, listenerType);
							mappings.put(tmp.getVirtualLowerCase(), tmp);
							if (virtual.equals("/")) {
								finished = true;
								// break;
							}
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}

			// set default lucee-server-context
			{
				if (!hasServerContext) {
					ApplicationListener listener = ConfigUtil.loadListener(ApplicationListener.TYPE_MODERN, null);
					listener.setMode(ApplicationListener.MODE_CURRENT2ROOT);

					tmp = new MappingImpl(config, "/lucee-server", "{lucee-server}/context/", null, ConfigPro.INSPECT_AUTO, ConfigPro.INSPECT_INTERVAL_UNDEFINED,
							ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, false, true, true, false, false, listener, ApplicationListener.MODE_CURRENT2ROOT,
							ApplicationListener.TYPE_MODERN);
					mappings.put(tmp.getVirtualLowerCase(), tmp);
				}
				if (!hasWebContext) {
					ApplicationListener listener = ConfigUtil.loadListener(ApplicationListener.TYPE_MODERN, null);
					listener.setMode(ApplicationListener.MODE_CURRENT2ROOT);

					tmp = new MappingImpl(config, "/lucee", "{lucee-config}/context/", "{lucee-config}/context/lucee-context.lar", ConfigPro.INSPECT_AUTO,
							ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, false, true, true, false, false, listener,
							ApplicationListener.MODE_CURRENT2ROOT, ApplicationListener.TYPE_MODERN);
					mappings.put(tmp.getVirtualLowerCase(), tmp);
				}
			}

			if (!finished) {
				tmp = new MappingImpl(config, "/", "/", null, ConfigPro.INSPECT_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, true,
						true, true, false, false, null, -1, -1);
				mappings.put("/", tmp);
			}
			// config.setMappings((Mapping[]) mappings.toArray(new
			// Mapping[mappings.size()]));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return mappings.values().toArray(new Mapping[mappings.size()]);
	}

	private static short inspectTemplate(Config config, Struct data) {
		String strInsTemp = SystemUtil.getSystemPropOrEnvVar("lucee.inspect.template", null);
		if (StringUtil.isEmpty(strInsTemp, true)) strInsTemp = getAttr(config, data, "inspectTemplate");
		if (StringUtil.isEmpty(strInsTemp, true)) strInsTemp = getAttr(config, data, "inspect");
		if (StringUtil.isEmpty(strInsTemp, true)) {
			Boolean trusted = Caster.toBoolean(getAttr(config, data, "trusted"), null);
			if (trusted != null) {
				if (trusted.booleanValue()) return ConfigPro.INSPECT_AUTO;
				return ConfigPro.INSPECT_ALWAYS;
			}
			return ConfigPro.INSPECT_UNDEFINED;
		}

		return ConfigUtil.inspectTemplate(strInsTemp, ConfigPro.INSPECT_UNDEFINED);
	}

	public static lucee.runtime.rest.Mapping[] loadRestMappings(ConfigImpl config, Struct root) {
		Map<String, lucee.runtime.rest.Mapping> mappings = new HashMap<String, lucee.runtime.rest.Mapping>();
		try {
			boolean hasAccess = true;// MUST
			Struct el = ConfigUtil.getAsStruct("rest", root);

			Array _mappings = ConfigUtil.getAsArray("mapping", el);

			// first get mapping defined in server admin (read-only)
			boolean hasDefault = false;
			lucee.runtime.rest.Mapping tmp;

			// get current mappings
			if (hasAccess && _mappings != null) {
				Iterator<?> it = _mappings.getIterator();
				while (it.hasNext()) {
					try {
						el = Caster.toStruct(it.next());
						if (el == null) continue;

						String physical = getAttr(config, el, "physical");
						String virtual = getAttr(config, el, "virtual");
						boolean readonly = toBoolean(getAttr(config, el, "readonly"), false);
						boolean hidden = toBoolean(getAttr(config, el, "hidden"), false);
						boolean _default = toBoolean(getAttr(config, el, "default"), false);
						if (physical != null) {
							tmp = new lucee.runtime.rest.Mapping(config, virtual, physical, hidden, readonly, _default);
							if (_default) hasDefault = true;
							mappings.put(tmp.getVirtual(), tmp);
						}

					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}

			// set default if not exist
			if (!hasDefault) {
				Resource rest = config.getConfigDir().getRealResource("rest");
				rest.mkdirs();
				tmp = new lucee.runtime.rest.Mapping(config, "/default-set-by-lucee", rest.getAbsolutePath(), true, true, true);
				mappings.put(tmp.getVirtual(), tmp);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		lucee.runtime.rest.Mapping[] arr = mappings.values().toArray(new lucee.runtime.rest.Mapping[mappings.size()]);

		// make sure only one is default
		boolean hasDefault = false;
		lucee.runtime.rest.Mapping m;
		for (int i = 0; i < arr.length; i++) {
			m = arr[i];
			if (m.isDefault()) {
				if (hasDefault) m.setDefault(false);
				hasDefault = true;
			}
		}
		return arr;
	}

	public static Map<String, LoggerAndSourceData> loadLoggers(ConfigImpl config, Struct root) {
		config.clearLoggers(Boolean.FALSE);
		Set<String> existing = new HashSet<>();

		Map<String, LoggerAndSourceData> loggerMap = new HashMap<String, LoggerAndSourceData>();
		try {
			// loggers
			Struct loggers = ConfigUtil.getAsStruct("loggers", root);
			String name, tmp;
			Map<String, String> appenderArgs, layoutArgs;
			ClassDefinition cdAppender, cdLayout;
			int level = Log.LEVEL_ERROR;
			boolean readOnly = false;
			Iterator<Entry<Key, Object>> itt = loggers.entryIterator();
			Entry<Key, Object> entry;
			Struct child;
			while (itt.hasNext()) {
				try {
					entry = itt.next();
					child = Caster.toStruct(entry.getValue(), null);
					if (child == null) continue;

					name = entry.getKey().getString();

					// appender
					if (forceLogAppender != null) cdAppender = config.getLogEngine().appenderClassDefintion(forceLogAppender);
					else cdAppender = getClassDefinition(config, child, "appender", config.getIdentification());
					if (!cdAppender.hasClass()) {
						tmp = StringUtil.trim(getAttr(config, child, "appender"), "");
						cdAppender = config.getLogEngine().appenderClassDefintion(tmp);
					}
					else if (!cdAppender.isBundle()) {
						cdAppender = config.getLogEngine().appenderClassDefintion(cdAppender.getClassName());
					}
					appenderArgs = toArguments(child, "appenderArguments", true, false);

					// layout
					cdLayout = getClassDefinition(config, child, "layout", config.getIdentification());
					if (!cdLayout.hasClass()) {
						tmp = StringUtil.trim(getAttr(config, child, "layout"), "");
						cdLayout = config.getLogEngine().layoutClassDefintion(tmp);
					}
					else if (!cdLayout.isBundle()) {
						cdLayout = config.getLogEngine().layoutClassDefintion(cdLayout.getClassName());
					}
					layoutArgs = toArguments(child, "layoutArguments", true, false);

					String strLevel = getAttr(config, child, "level");
					if (forceLogLevel != null) strLevel = forceLogLevel;
					if (StringUtil.isEmpty(strLevel, true)) strLevel = getAttr(config, child, "logLevel");
					level = LogUtil.toLevel(StringUtil.trim(strLevel, ""), Log.LEVEL_ERROR);
					readOnly = Caster.toBooleanValue(getAttr(config, child, "readOnly"), false);
					// ignore when no appender/name is defined
					if (cdAppender.hasClass() && !StringUtil.isEmpty(name)) {
						existing.add(name.toLowerCase());
						if (cdLayout.hasClass()) {
							addLogger(config, loggerMap, name, level, cdAppender, appenderArgs, cdLayout, layoutArgs, readOnly, false);
						}
						else addLogger(config, loggerMap, name, level, cdAppender, appenderArgs, null, null, readOnly, false);
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					LogUtil.logGlobal(config, ConfigFactoryImpl.class.getName(), t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			LogUtil.logGlobal(config, ConfigFactoryImpl.class.getName(), t);
		}
		return loggerMap;
	}

	public static LoggerAndSourceData addLogger(Config config, Map<String, LoggerAndSourceData> loggers, String name, int level, ClassDefinition appender,
			Map<String, String> appenderArgs, ClassDefinition layout, Map<String, String> layoutArgs, boolean readOnly, boolean dyn) throws PageException {
		LoggerAndSourceData existing = loggers.get(name.toLowerCase());
		String id = LoggerAndSourceData.id(name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly);

		if (existing != null) {
			if (existing.id().equals(id)) {
				return existing;
			}
			existing.close();
		}

		LoggerAndSourceData las = new LoggerAndSourceData(config, id, name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly, dyn);
		loggers.put(name.toLowerCase(), las);
		return las;
	}

	/**
	 * Human-readable description of what changed in the exeLog marker between boots.
	 * The marker val format is "executionLogEnabled:lineBased:engineVersion" — emit a
	 * specific reason for each component that actually differs.
	 */
	private static String describeMarkerChange(String previousVal, String newVal) {
		if (previousVal == null) {
			return "purging cfclasses: marker file not present (fresh install or first boot of this Lucee version) [" + newVal + "]";
		}
		String[] oldParts = previousVal.split(":", -1);
		String[] newParts = newVal.split(":", -1);
		StringBuilder reasons = new StringBuilder();
		if (oldParts.length >= 1 && newParts.length >= 1 && !oldParts[0].equals(newParts[0])) {
			reasons.append("execution log enabled: ").append(oldParts[0]).append(" -> ").append(newParts[0]);
		}
		if (oldParts.length >= 2 && newParts.length >= 2 && !oldParts[1].equals(newParts[1])) {
			if (reasons.length() > 0) reasons.append("; ");
			reasons.append("execution log lineBased: ").append(oldParts[1]).append(" -> ").append(newParts[1]);
		}
		if (oldParts.length >= 3 && newParts.length >= 3 && !oldParts[2].equals(newParts[2])) {
			if (reasons.length() > 0) reasons.append("; ");
			reasons.append("Lucee engine version: ").append(oldParts[2]).append(" -> ").append(newParts[2]);
		}
		if (reasons.length() == 0) {
			reasons.append("marker mismatch with no specific delta detected [previous=").append(previousVal).append(", current=").append(newVal).append("]");
		}
		return "purging cfclasses, templates will recompile under the current engine (" + reasons + ")";
	}

	public static ExecutionLogFactory loadExeLog(ConfigImpl config, Struct root) {
		try {
			Struct el = ConfigUtil.getAsStruct("executionLog", root);

			// Determine the execution log class first
			String strClass = getAttr(config, el, "class");
			Class<? extends ExecutionLog> clazz = null;
			Map<String, String> args = null;

			// If debugger breakpoint support enabled and no explicit class configured, use DebuggerExecutionLog
			if (StringUtil.isEmpty(strClass) && ConfigImpl.DEBUGGER) {
				LogUtil.log(config, Log.LEVEL_INFO, "application", "Debugger breakpoint support enabled");
				clazz = DebuggerExecutionLog.class;
				args = new HashMap<String, String>();
			}
			else if (!StringUtil.isEmpty(strClass)) {
				try {
					if ("console".equalsIgnoreCase(strClass)) clazz = ConsoleExecutionLog.class;
					else if ("debug".equalsIgnoreCase(strClass)) clazz = DebugExecutionLog.class;
					else {
						ClassDefinition cd = el != null ? getClassDefinition(config, el, "", config.getIdentification()) : null;

						Class<?> c = cd != null ? cd.getClazz() : null;
						if (c != null && ExecutionLog.class.isAssignableFrom(c)) {
							clazz = c.asSubclass(ExecutionLog.class);
						}
						else {
							clazz = ConsoleExecutionLog.class;
							LogUtil.logGlobal(config, Log.LEVEL_ERROR, ConfigFactoryImpl.class.getName(),
									"class [" + strClass + "] must implement the interface " + ExecutionLog.class.getName());
						}
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), ConfigFactoryImpl.class.getName(), t);
					clazz = ConsoleExecutionLog.class;
				}
				if (clazz != null)
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "loaded ExecutionLog class " + clazz.getName());

				// arguments
				args = toArguments(el, "arguments", true, false);
				if (args == null) args = toArguments(el, "classArguments", true, false);
			}

			if (clazz == null) {
				clazz = ConsoleExecutionLog.class;
				args = new HashMap<String, String>();
			}

			ExecutionLogFactory factory = new ExecutionLogFactory(clazz, args);

			// Track ExecutionLog mode + engine version in marker file to detect
			// restart-triggered cache invariants. Format:
			//   "enabled:lineBased:engineVersion"
			// If any component changes, purge cfclasses so we recompile under the new
			// engine. Engine-version inclusion catches Lucee upgrades; without it,
			// .class files compiled by the previous engine survive on disk and get
			// loaded by the new engine via path 2 in PageSourceImpl.loadPhysical,
			// then trigger phantom-rename storms (every page-reload recompiles).
			String engineVersion = ConfigUtil.getCFMLEngine(config).getInfo().getVersion().toString();
			String val = config.getExecutionLogEnabled() + ":" + factory.isLineBased() + ":" + engineVersion;

			// Default to "cfclasses is untrusted; purge". Only skip the purge if the
			// marker file exists AND matches the current val exactly. Any read
			// failure also falls through to purge — when we can't prove the cache is
			// valid, we don't trust it.
			boolean cacheValid = false;
			String previousVal = null;
			try {
				Resource exeLog = config.getConfigDir().getRealResource("exeLog");
				if (exeLog.exists()) {
					previousVal = IOUtil.toString(exeLog, SystemUtil.getCharset());
					cacheValid = val.equals(previousVal);
				}
			}
			catch (IOException e) {
				log(config, e);
			}

			if (!cacheValid) {
				// Only log when there's actually something to purge. Fresh installs with
				// an empty cfclasses dir don't need noise. Logged at DEBUG so it routes
				// to out.log (informational) rather than err.log (problems).
				try {
					if (config.getClassDirectory().exists()) {
						log(config, Log.LEVEL_DEBUG, describeMarkerChange(previousVal, val));
						config.getClassDirectory().remove(true);
					}
				}
				catch (IOException e) {
					log(config, e);
				}
				// Write the marker AFTER the purge so a crash between purge and write
				// leaves the marker stale/missing — next boot detects and purges again
				// rather than trusting a half-purged cfclasses.
				try {
					Resource exeLog = config.getConfigDir().getRealResource("exeLog");
					if (!exeLog.exists()) exeLog.createNewFile();
					IOUtil.write(exeLog, val, SystemUtil.getCharset(), false);
				}
				catch (IOException e) {
					log(config, e);
				}
			}

			return factory;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return new ExecutionLogFactory(ConsoleExecutionLog.class, new HashMap<String, String>());
	}

	/**
	 * loads datasource settings from XMl DOM
	 * 
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 * @throws BundleException
	 * @throws ClassNotFoundException
	 */
	public static Map<String, DataSource> loadDataSources(ConfigImpl config, Struct root) {
		Map<String, DataSource> datasources = new HashMap<String, DataSource>();
		try {

			// When set to true, makes JDBC use a representation for DATE data that
			// is compatible with the Oracle8i database.
			System.setProperty("oracle.jdbc.V8Compatible", "true");

			SecurityManager sm = config.getSecurityManager();
			short access = sm.getAccess(SecurityManager.TYPE_DATASOURCE);
			int accessCount = -1;
			if (access == SecurityManager.VALUE_YES) accessCount = -1;
			else if (access == SecurityManager.VALUE_NO) accessCount = 0;
			else if (access >= SecurityManager.VALUE_1 && access <= SecurityManager.VALUE_10) {
				accessCount = access - SecurityManager.NUMBER_OFFSET;
			}

			// Databases

			// Data Sources
			Struct dataSources = ConfigUtil.getAsStruct(config, root, false, "dataSources");
			if (accessCount == -1) accessCount = dataSources.size();
			if (dataSources.size() < accessCount) accessCount = dataSources.size();

			// if(hasAccess) {
			JDBCDriver jdbc;
			ClassDefinition cd;
			String id;
			Iterator<Entry<Key, Object>> it = dataSources.entryIterator();
			Entry<Key, Object> e;
			Struct dataSource;
			while (it.hasNext()) {
				e = it.next();
				dataSource = Caster.toStruct(e.getValue(), null);
				if (dataSource == null) continue;

				if (dataSource.containsKey(KeyConstants._database)) {
					try {
						// do we have an id?
						jdbc = config.getJDBCDriverById(getAttr(config, dataSource, "id"), null);
						if (jdbc != null && jdbc.cd != null) {
							cd = jdbc.cd;
						}
						else {
							cd = getClassDefinition(config, dataSource, "", config.getIdentification());
						}

						// we have no class
						if (!cd.hasClass()) {
							jdbc = config.getJDBCDriverById(getAttr(config, dataSource, "type"), null);
							if (jdbc != null && jdbc.cd != null) {
								cd = jdbc.cd;
							}
						}
						// we only have a class
						else if (!cd.isBundle()) {
							jdbc = config.getJDBCDriverByClassName(cd.getClassName(), null);
							if (jdbc != null && jdbc.cd != null && jdbc.cd.isBundle()) cd = jdbc.cd;
						}

						// still no bundle!
						if (!cd.isBundle()) cd = patchJDBCClass(config, cd);
						int idle = Caster.toIntValue(getAttr(config, dataSource, "idleTimeout"), -1);
						if (idle == -1) idle = Caster.toIntValue(getAttr(config, dataSource, "connectionTimeout"), -1);
						int defLive = 15;
						if (idle > 0) defLive = idle * 5;// for backward compatibility

						String dsn = getAttr(config, dataSource, "connectionString");
						if (StringUtil.isEmpty(dsn, true)) dsn = getAttr(config, dataSource, "dsn");
						if (StringUtil.isEmpty(dsn, true)) dsn = getAttr(config, dataSource, "connStr");
						if (StringUtil.isEmpty(dsn, true)) dsn = getAttr(config, dataSource, "url");
						if (StringUtil.isEmpty(dsn, true)) {
							if (jdbc == null && cd.hasClass()) {
								jdbc = config.getJDBCDriverByClassName(cd.getClassName(), null);
							}
							if (jdbc != null) {
								dsn = jdbc.connStr;
							}

						}
						String bundleName = getAttr(config, dataSource, "bundleName");
						String bundleVersion = getAttr(config, dataSource, "bundleVersion");

						setDatasource(config, datasources, e.getKey().getString(), cd, getAttr(config, dataSource, "host"), getAttr(config, dataSource, "database"),
								Caster.toIntValue(getAttr(config, dataSource, "port"), -1), dsn, bundleName, bundleVersion, getAttr(config, dataSource, "username"),
								ConfigUtil.decrypt(getAttr(config, dataSource, "password")), null,
								Caster.toIntValue(getAttr(config, dataSource, "connectionLimit"), DEFAULT_MAX_CONNECTION), idle,
								Caster.toIntValue(getAttr(config, dataSource, "liveTimeout"), defLive), Caster.toIntValue(getAttr(config, dataSource, "minIdle"), 0),
								Caster.toIntValue(getAttr(config, dataSource, "maxIdle"), 0), Caster.toIntValue(getAttr(config, dataSource, "maxTotal"), 0),
								Caster.toLongValue(getAttr(config, dataSource, "metaCacheTimeout"), 60000), toBoolean(getAttr(config, dataSource, "blob"), true),
								toBoolean(getAttr(config, dataSource, "clob"), true), Caster.toIntValue(getAttr(config, dataSource, "allow"), DataSource.ALLOW_ALL),
								toBoolean(getAttr(config, dataSource, "validate"), false), toBoolean(getAttr(config, dataSource, "storage"), false),
								getAttr(config, dataSource, "timezone"), ConfigUtil.getAsStruct(config, dataSource, true, "custom"), getAttr(config, dataSource, "dbdriver"),
								ParamSyntaxImpl.toParamSyntax(dataSource, ParamSyntaxImpl.DEFAULT), toBoolean(getAttr(config, dataSource, "literalTimestampWithTSOffset"), false),
								toBoolean(getAttr(config, dataSource, "alwaysSetTimeout"), false), toBoolean(getAttr(config, dataSource, "requestExclusive"), false),
								toBoolean(getAttr(config, dataSource, "alwaysResetConnections"), false)

						);
					}
					catch (Throwable th) {
						ExceptionUtil.rethrowIfNecessary(th);
						log(config, th);
					}
				}
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return datasources;
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

	public static JDBCDriver[] loadJDBCDrivers(ConfigImpl config, Struct root) {
		Map<String, JDBCDriver> map = new HashMap<String, JDBCDriver>();
		try {
			// first add the server drivers, so they can be overwritten

			// jdbcDrivers
			Struct jdbcDrivers = ConfigUtil.getAsStruct("jdbcDrivers", root);
			Iterator<Entry<Key, Object>> it = jdbcDrivers.entryIterator();
			Entry<Key, Object> e;
			ClassDefinition cd;
			String label, id, connStr;
			while (it.hasNext()) {
				try {
					e = it.next();
					Struct driver = Caster.toStruct(e.getValue(), null);
					if (driver == null) continue;

					// class definition
					driver.setEL(KeyConstants._class, e.getKey().getString());
					cd = getClassDefinition(config, driver, "", config.getIdentification());
					if (StringUtil.isEmpty(cd.getClassName()) && !StringUtil.isEmpty(cd.getName())) {
						try {
							Bundle bundle = OSGiUtil.loadBundle(cd.getName(), cd.getVersion(), config.getIdentification(), null, false);
							String cn = JDBCDriver.extractClassName(bundle);
							cd = new ClassDefinitionImpl(cn, cd.getName(), cd.getVersionAsString(), config.getIdentification());
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
						}
					}

					label = getAttr(config, driver, "label");
					id = getAttr(config, driver, "id");
					connStr = getAttr(config, driver, "connectionString");
					// check if label exists
					if (StringUtil.isEmpty(label)) {
						log(config, Log.LEVEL_INFO, "missing label for jdbc driver [" + cd.getClassName() + "]");
						continue;
					}

					// check if it is a bundle or maven coordinates
					if ( !cd.isBundle() && !( cd instanceof ClassDefinitionImpl && ( (ClassDefinitionImpl) cd ).isMaven() ) ) {
						log(config, Log.LEVEL_INFO, "jdbc driver [" + label + "] does not describe a bundle or maven coordinates");
						continue;
					}
					map.put(cd.toString(), new JDBCDriver(label, id, connStr, cd));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return map.values().toArray(new JDBCDriver[map.size()]);
	}

	public static Map<String, ClassDefinition> loadCacheDefintions(ConfigImpl config, Struct root) {
		Map<String, ClassDefinition> map = new HashMap<String, ClassDefinition>();
		try {

			// first add the server drivers, so they can be overwritten
			ClassDefinition cd;

			Array caches = ConfigUtil.getAsArray("cacheClasses", root);
			if (caches != null) {
				Iterator<?> it = caches.getIterator();
				Struct cache;
				while (it.hasNext()) {
					try {
						cache = Caster.toStruct(it.next());
						if (cache == null) continue;
						cd = getClassDefinition(config, cache, "", config.getIdentification());

						if (!cd.isBundle() && !(cd instanceof ClassDefinitionImpl && ((ClassDefinitionImpl) cd).isMaven())) {
							log(config, Log.LEVEL_WARN, "skipping cache definition [" + cd + "], no bundle or maven coordinates");
							continue;
						}
						map.put(cd.getClassName(), cd);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return map;
	}

	public static Map<Integer, String> loadCacheDefaultConnectionNames(ConfigImpl config, Struct root) {
		Map<Integer, String> names = new HashMap<>();
		try {
			Struct defaultCache = ConfigUtil.getAsStruct("cache", root);

			// default cache
			for (int i = 0; i < ConfigPro.CACHE_TYPES_MAX.length; i++) {
				try {
					String def = getAttr(config, defaultCache, "default" + StringUtil.ucFirst(ConfigPro.STRING_CACHE_TYPES_MAX[i]));
					if (StringUtil.isEmpty(def, true)) def = getAttr(config, root, "cacheDefault" + StringUtil.ucFirst(ConfigPro.STRING_CACHE_TYPES_MAX[i]));

					if (!StringUtil.isEmpty(def, true)) {
						names.put(ConfigPro.CACHE_TYPES_MAX[i], def.trim());
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return names;
	}

	public static Map<String, CacheConnection> loadCacheCacheConnections(ConfigImpl config, Struct root) {
		Map<String, CacheConnection> caches = new HashMap<String, CacheConnection>();
		try {

			// cache connections
			Struct conns = ConfigUtil.getAsStruct("caches", root);

			// if(hasAccess) {
			ClassDefinition cd;
			Key name;
			CacheConnection cc;
			// Class cacheClazz;
			// caches
			{
				Iterator<Entry<Key, Object>> it = conns.entryIterator();
				Entry<Key, Object> entry;
				Struct data;
				while (it.hasNext()) {
					try {
						entry = it.next();
						name = entry.getKey();
						data = Caster.toStruct(entry.getValue(), null);
						cd = getClassDefinition(config, data, "", config.getIdentification());
						if (!cd.isBundle()) {
							ClassDefinition _cd = config.getCacheDefinition(cd.getClassName());
							if (_cd != null) cd = _cd;
						}

						{
							Struct custom = ConfigUtil.getAsStruct(config, data, true, "custom");
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
							cc = new CacheConnectionImpl(config, name.getString(), cd, custom, Caster.toBooleanValue(getAttr(config, data, "readOnly"), false),
									Caster.toBooleanValue(getAttr(config, data, "storage"), false));
							if (!StringUtil.isEmpty(name)) {
								caches.put(name.getLowerString(), cc);
							}
							else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, ConfigFactoryImpl.class.getName(), "missing cache name");

						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}

			// call static init once per driver
			{
				// group by classes
				final Map<ClassDefinition, List<CacheConnection>> _caches = new HashMap<ClassDefinition, List<CacheConnection>>();
				{
					Iterator<Entry<String, CacheConnection>> it = caches.entrySet().iterator();
					Entry<String, CacheConnection> entry;
					List<CacheConnection> list;
					while (it.hasNext()) {
						try {
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
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
							log(config, t);
						}
					}
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return caches;
	}

	private static String getMD5(Struct data, String cacheDef, String parentMD5) {
		try {
			return MD5.getDigestAsString(new StringBuilder().append(data.toString()).append(':').append(cacheDef).append(':').append(parentMD5).toString());
		}
		catch (IOException e) {
			return "";
		}
	}

	public static GatewayMap loadGatewayEL(ConfigImpl config, Struct root) {
		try {
			return loadGateway(config, root);
		}
		catch (Exception e) {
			log(config, e);
			return new GatewayMap();
		}
	}

	public static GatewayMap loadGateway(final ConfigImpl config, Struct root) {
		GatewayMap mapGateways = new GatewayMap();
		boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY);
		GatewayEntry ge;
		// cache connections
		Struct gateways = ConfigUtil.getAsStruct("gateways", root);

		String id;
		// caches
		if (hasAccess) {
			try {
				Iterator<Entry<Key, Object>> it = gateways.entryIterator();
				Entry<Key, Object> e;
				Struct eConnection;
				while (it.hasNext()) {
					try {
						e = it.next();
						eConnection = Caster.toStruct(e.getValue(), null);
						if (eConnection == null) continue;
						id = e.getKey().getLowerString();

						ge = new GatewayEntryImpl(id, getClassDefinition(config, eConnection, "", config.getIdentification()), getAttr(config, eConnection, "cfcPath"),
								getAttr(config, eConnection, "listenerCFCPath"), getAttr(config, eConnection, "startupMode"),
								ConfigUtil.getAsStruct(config, eConnection, true, "custom"), Caster.toBooleanValue(getAttr(config, eConnection, "readOnly"), false));

						if (!StringUtil.isEmpty(id)) {
							mapGateways.put(id.toLowerCase(), ge);
						}
						else {
							LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), Log.LEVEL_ERROR, ConfigFactoryImpl.class.getName(), "missing id");

						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log(config, t);
			}
		}
		return mapGateways;
	}

	private static void setDatasource(ConfigImpl config, Map<String, DataSource> datasources, String datasourceName, ClassDefinition cd, String server, String databasename,
			int port, String dsn, String bundleName, String bundleVersion, String user, String pass, TagListener listener, int connectionLimit, int idleTimeout, int liveTimeout,
			int minIdle, int maxIdle, int maxTotal, long metaCacheTimeout, boolean blob, boolean clob, int allow, boolean validate, boolean storage, String timezone, Struct custom,
			String dbdriver, ParamSyntax ps, boolean literalTimestampWithTSOffset, boolean alwaysSetTimeout, boolean requestExclusive, boolean alwaysResetConnections)
			throws BundleException, ClassException, SQLException {

		datasources.put(datasourceName.toLowerCase(),
				new DataSourceImpl(config, datasourceName, cd, server, dsn, bundleName, bundleVersion, databasename, port, user, pass, listener, connectionLimit, idleTimeout,
						liveTimeout, minIdle, maxIdle, maxTotal, metaCacheTimeout, blob, clob, allow, custom, false, validate, storage,
						StringUtil.isEmpty(timezone, true) ? null : TimeZoneUtil.toTimeZone(timezone, null), dbdriver, ps, literalTimestampWithTSOffset, alwaysSetTimeout,
						requestExclusive, alwaysResetConnections, ThreadLocalPageContext.getLog(config, "application")));

	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 * @throws IOException
	 */
	public static Mapping[] loadCustomTagsMappings(ConfigImpl config, Struct root) {
		Mapping[] mappings = null;
		try {
			Array ctMappings = ConfigUtil.getAsArray(config, root, true, KeyConstants._virtual, KeyConstants._physical, true, "customTagMappings", "customTagPaths");

			boolean hasDefault = false;

			// Web Mapping
			if (ctMappings.size() > 0) {
				Iterator<Object> it = ctMappings.valueIterator();
				List<Mapping> list = new ArrayList<>();
				Struct ctMapping;
				while (it.hasNext()) {
					try {
						ctMapping = Caster.toStruct(it.next(), null);
						if (ctMapping == null) continue;

						String virtual = createVirtual(config, ctMapping);
						String physical = getAttr(config, ctMapping, "physical");
						String archive = getAttr(config, ctMapping, "archive");
						boolean readonly = toBoolean(getAttr(config, ctMapping, "readonly"), false);
						boolean hidden = toBoolean(getAttr(config, ctMapping, "hidden"), false);
						if ("{lucee-web}/customtags/".equals(physical) || "{lucee-server}/customtags/".equals(physical)) continue;
						if ("{lucee-config}/customtags/".equals(physical)) hasDefault = true;
						short inspTemp = inspectTemplate(config, ctMapping);
						int insTempSlow = Caster.toIntValue(getAttr(config, ctMapping, "inspectTemplateIntervalSlow"), -1);
						int insTempFast = Caster.toIntValue(getAttr(config, ctMapping, "inspectTemplateIntervalFast"), -1);

						String primary = getAttr(config, ctMapping, "primary");

						boolean physicalFirst = StringUtil.isEmpty(archive, true) || !"archive".equalsIgnoreCase(primary);
						list.add(new MappingImpl(config, virtual, physical, archive, inspTemp, insTempSlow, insTempFast, physicalFirst, hidden, readonly, true, false, true, null,
								-1, -1));
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
				if (!hasDefault) {
					list.add(new MappingImpl(config, "/default", "{lucee-config}/customtags/", null, ConfigPro.INSPECT_NEVER, -1, -1, true, false, true, true, false, true, null,
							-1, -1));
				}
				mappings = list.toArray(new Mapping[list.size()]);
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		if (mappings == null) {
			mappings = new Mapping[] { new MappingImpl(config, "/default-customtags", "{lucee-config}/customtags/", null, ConfigPro.INSPECT_UNDEFINED,
					ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, true, true, true, false, true, null, -1, -1) };
		}
		return mappings;
	}

	private static Object toKey(Mapping m) {
		if (!StringUtil.isEmpty(m.getStrPhysical(), true)) return m.getVirtual() + ":" + m.getStrPhysical().toLowerCase().trim();
		return (m.getVirtual() + ":" + m.getStrPhysical() + ":" + m.getStrArchive()).toLowerCase();
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws ExpressionException
	 * @throws TagLibException
	 * @throws FunctionLibException
	 */
	public static void loadTag(ConfigImpl config, Struct root, boolean doNew) {
		try {
			Resource configDir = config.getConfigDir();
			String strDefaultTLDDirectory = null;
			String strDefaultTagDirectory = null;
			String strTagDirectory = null;

			// only read in server context
			strDefaultTLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.tld", null);
			strDefaultTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.tag", null);
			if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.tld", null);
			if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.tag", null);
			strTagDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.additional.tag", null);

			Struct fileSystem = ConfigUtil.getAsStruct("fileSystem", root);

			// get library directories
			if (fileSystem != null) {
				if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "tldDirectory"));
				if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "tagDirectory"));
				if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "tldDefaultDirectory"));
				if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "tagDefaultDirectory"));
				if (StringUtil.isEmpty(strTagDirectory)) strTagDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "tagAddionalDirectory"));
			}

			// set default directories if necessary
			if (StringUtil.isEmpty(strDefaultTLDDirectory)) strDefaultTLDDirectory = "{lucee-config}/library/tld/";
			if (StringUtil.isEmpty(strDefaultTagDirectory)) strDefaultTagDirectory = "{lucee-config}/library/tag/";

			// init TLDS
			{
				config.setTLDs(ConfigUtil.duplicate(new TagLib[] { ConfigUtil.getConfigServerImpl(config).getCoreTLDs() }, false)); // MUST duplicate needed?
			}

			// TLD Dir
			if (!StringUtil.isEmpty(strDefaultTLDDirectory)) {
				Resource tld = ConfigUtil.getFile(config, configDir, strDefaultTLDDirectory, FileUtil.TYPE_DIR);
				if (tld != null) config.setTldFile(tld);
			}

			// Tag Directory
			List<Path> listTags = new ArrayList<Path>();
			if (!StringUtil.isEmpty(strDefaultTagDirectory)) {
				Resource dir = ConfigUtil.getFile(config, configDir, strDefaultTagDirectory, FileUtil.TYPE_DIR);
				createTagFiles(config, configDir, dir, doNew);
				listTags.add(new Path(strDefaultTagDirectory, dir));
			}
			// addional tags
			Map<String, String> mapTags = new LinkedHashMap<String, String>();
			if (!StringUtil.isEmpty(strTagDirectory) || !mapTags.isEmpty()) {
				String[] arr = ListUtil.listToStringArray(strTagDirectory, ',');
				for (String str: arr) {
					mapTags.put(str, "");
				}
				for (String str: mapTags.keySet()) {
					try {
						str = str.trim();
						if (StringUtil.isEmpty(str)) continue;
						Resource dir = ConfigUtil.getFile(config, configDir, str, FileUtil.TYPE_DIR);
						listTags.add(new Path(str, dir));
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}
			config.setTagDirectory(listTags);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
	}

	public static void loadFunctions(ConfigImpl config, Struct rootMayNull, boolean doNew) {
		try {
			Resource configDir = config.getConfigDir();

			String strDefaultFLDDirectory = null;
			String strDefaultFuncDirectory = null;
			String strFuncDirectory = null;

			// only read in server context
			strDefaultFLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.fld", null);
			strDefaultFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.function", null);
			if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.fld", null);
			if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.default.function", null);
			strFuncDirectory = SystemUtil.getSystemPropOrEnvVar("lucee.library.additional.function", null);

			Struct fileSystem = rootMayNull == null ? null : ConfigUtil.getAsStruct("fileSystem", rootMayNull);

			// get library directories
			if (fileSystem != null) {
				if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "flddirectory"));
				if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "functionDirectory"));
				if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "fldDefaultDirectory"));
				if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "functionDefaultDirectory"));
				if (StringUtil.isEmpty(strFuncDirectory)) strFuncDirectory = ConfigUtil.translateOldPath(getAttr(config, fileSystem, "functionAddionalDirectory"));
			}

			// set default directories if necessary
			if (StringUtil.isEmpty(strDefaultFLDDirectory)) strDefaultFLDDirectory = "{lucee-config}/library/fld/";
			if (StringUtil.isEmpty(strDefaultFuncDirectory)) strDefaultFuncDirectory = "{lucee-config}/library/function/";

			// Init flds
			{
				config.setFLDs(ConfigUtil.getConfigServerImpl(config).getCoreFLDs().duplicate(false)); // MUST duplicate needed?

			}

			// FLDs
			if (!StringUtil.isEmpty(strDefaultFLDDirectory)) {
				Resource fld = ConfigUtil.getFile(config, configDir, strDefaultFLDDirectory, FileUtil.TYPE_DIR);
				if (fld != null) config.setFldFile(fld);
			}

			// Function files (CFML)
			List<Path> listFuncs = new ArrayList<Path>();
			if (!StringUtil.isEmpty(strDefaultFuncDirectory)) {
				Resource dir = ConfigUtil.getFile(config, configDir, strDefaultFuncDirectory, FileUtil.TYPE_DIR);
				createFunctionFiles(config, configDir, dir, doNew);
				listFuncs.add(new Path(strDefaultFuncDirectory, dir));
				// if (dir != null) config.setFunctionDirectory(dir);
			}
			// function additonal
			Map<String, String> mapFunctions = new LinkedHashMap<String, String>();
			if (!StringUtil.isEmpty(strFuncDirectory) || !mapFunctions.isEmpty()) {
				String[] arr = ListUtil.listToStringArray(strFuncDirectory, ',');
				for (String str: arr) {
					mapFunctions.put(str, "");
				}
				for (String str: mapFunctions.keySet()) {
					try {
						str = str.trim();
						if (StringUtil.isEmpty(str)) continue;
						Resource dir = ConfigUtil.getFile(config, configDir, str, FileUtil.TYPE_DIR);
						listFuncs.add(new Path(str, dir));
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}
			config.setFunctionDirectory(listFuncs);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
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
			Resource f;
			Resource build = dir.getRealResource("build");
			// /resource/library/tag/build/jquery
			Resource jquery = build.getRealResource("jquery");
			if (!jquery.isDirectory()) jquery.mkdirs();
			String[] names = new String[] { "jquery-1.12.4.min.js" };
			for (int i = 0; i < names.length; i++) {
				try {
					f = jquery.getRealResource(names[i]);
					if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/tag/build/jquery/" + names[i], f);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, t);
				}
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

			f = dir.getRealResource("threadInterrupt." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/threadInterrupt." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("interruptThread." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/interruptThread." + TEMPLATE_EXTENSION, f);

			// LDEV-6282: throw() is now a Java BIF; remove any stale CFML wrapper.
			f = dir.getRealResource("throw." + TEMPLATE_EXTENSION);
			if (f.exists()) delete(dir, "throw." + TEMPLATE_EXTENSION);

			f = dir.getRealResource("trace." + TEMPLATE_EXTENSION);
			if (!f.exists() || doNew) createFileFromResourceEL("/resource/library/function/trace." + TEMPLATE_EXTENSION, f);

			f = dir.getRealResource("queryExecute." + TEMPLATE_EXTENSION);
			if (f.exists()) delete(dir, "queryExecute." + TEMPLATE_EXTENSION);

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
					LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigFactoryImpl.class.getName(), e);
				}
			}

		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 */
	public static URL loadUpdate(ConfigImpl config, Struct root) {
		try {
			// Server
			if (root != null) {
				ConfigServerImpl cs = (ConfigServerImpl) config;

				String location = getAttr(config, root, "updateLocation");
				if (StringUtil.isEmpty(location, true)) location = getAttr(config, root, "updateSiteURL");
				if (!StringUtil.isEmpty(location, true)) {
					location = location.trim();
					if ("http://update.lucee.org".equals(location)) location = DEFAULT_LOCATION;
					if ("http://snapshot.lucee.org".equals(location) || "https://snapshot.lucee.org".equals(location)) location = DEFAULT_LOCATION;
					if ("http://release.lucee.org".equals(location) || "https://release.lucee.org".equals(location)) location = DEFAULT_LOCATION;
					return HTTPUtil.toURL(location, HTTPUtil.ENCODED_AUTO);
				}

			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return Constants.DEFAULT_UPDATE_URL;
	}

	public static RemoteClient[] loadRemoteClients(ConfigImpl config, Struct root) {
		java.util.List<RemoteClient> list = new ArrayList<RemoteClient>();
		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE);

			Struct _clients = ConfigUtil.getAsStruct("remoteClients", root);

			Array clients = null;
			Struct client;

			if (hasAccess && _clients != null) clients = ConfigUtil.getAsArray("remoteClient", _clients);

			if (clients != null) {
				Iterator<?> it = clients.getIterator();
				while (it.hasNext()) {
					try {
						client = Caster.toStruct(it.next(), null);
						if (client == null) continue;

						// type
						String type = getAttr(config, client, "type");
						if (StringUtil.isEmpty(type)) type = "web";
						// url
						String url = getAttr(config, client, "url");
						String label = getAttr(config, client, "label");
						if (StringUtil.isEmpty(label)) label = url;
						String sUser = getAttr(config, client, "serverUsername");
						String sPass = ConfigUtil.decrypt(getAttr(config, client, "serverPassword"));
						String aPass = ConfigUtil.decrypt(getAttr(config, client, "adminPassword"));
						String aCode = ConfigUtil.decrypt(getAttr(config, client, "securityKey"));
						// if(aCode!=null && aCode.indexOf('-')!=-1)continue;
						String usage = getAttr(config, client, "usage");
						if (usage == null) usage = "";

						String pUrl = getAttr(config, client, "proxyServer");
						int pPort = Caster.toIntValue(getAttr(config, client, "proxyPort"), -1);
						String pUser = getAttr(config, client, "proxyUsername");
						String pPass = ConfigUtil.decrypt(getAttr(config, client, "proxyPassword"));
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
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}

				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}

		return list.toArray(new RemoteClient[list.size()]);
	}

	public static PrintStream loadErr(ConfigImpl config, Struct root) {
		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			String err = null;
			err = SystemUtil.getSystemPropOrEnvVar("lucee.system.err", null);
			if (StringUtil.isEmpty(err)) err = getAttr(config, root, "systemErr");
			return toPrintStream(config, err, true);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return null;
	}

	public static PrintStream loadOut(ConfigImpl config, Struct root) {
		try {

			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			String out = null;
			// sys prop or env var
			out = SystemUtil.getSystemPropOrEnvVar("lucee.system.out", null);
			if (StringUtil.isEmpty(out)) out = getAttr(config, root, "systemOut");
			return toPrintStream(config, out, false);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return null;
	}

	private static PrintStream toPrintStream(Config config, String streamtype, boolean iserror) {
		if (!StringUtil.isEmpty(streamtype)) {
			streamtype = streamtype.trim();
			// null
			if ("null".equalsIgnoreCase(streamtype)) {
				return new PrintStream(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM);
			}
			// class
			else if (StringUtil.startsWithIgnoreCase(streamtype, "class:")) {
				String classname = streamtype.substring(6);
				try {

					return (PrintStream) ClassUtil.loadInstance((PageContext) null, classname);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
			// file
			else if (StringUtil.startsWithIgnoreCase(streamtype, "file:")) {
				String strRes = streamtype.substring(5);
				try {
					strRes = ConfigUtil.translateOldPath(strRes);
					Resource res = ConfigUtil.getFile(config, config.getConfigDir(), strRes, ResourceUtil.TYPE_FILE);
					if (res != null) return new PrintStream(res.getOutputStream(), true);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
			else if (StringUtil.startsWithIgnoreCase(streamtype, "log")) {
				try {
					CFMLEngineFactory factory = ConfigUtil.getCFMLEngineFactory(config);
					Resource root = ResourceUtil.toResource(factory.getResourceRoot());
					Resource log = root.getRealResource("context/logs/" + (iserror ? "err" : "out") + ".log");
					if (!log.isFile()) {
						log.getParentResource().mkdirs();
						log.createNewFile();
					}
					return new PrintStream(new RetireOutputStream(log, true, 5, null));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
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
	public static TimeZone loadTimezone(ConfigImpl config, Struct root, TimeZone defaultValue) {
		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			// timeZone
			String strTimeZone = null;
			strTimeZone = getAttr(config, root, new String[] { "timezone", "thisTimezone" });

			if (!StringUtil.isEmpty(strTimeZone)) return TimeZone.getTimeZone(strTimeZone);
			else {
				TimeZone def = TimeZone.getDefault();
				if (def == null) {
					def = TimeZoneConstants.EUROPE_LONDON;
				}
				return def;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
	}

	public static Locale loadLocale(ConfigImpl config, Struct root, Locale defaultValue) {
		if (ConfigUtil.hasAccess(config, SecurityManager.TYPE_SETTING)) {
			try {
				// locale
				String strLocale = getAttr(config, root, new String[] { "locale", "thisLocale" });
				if (!StringUtil.isEmpty(strLocale)) return Caster.toLocale(strLocale, defaultValue);

			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log(config, t);
			}
		}
		return defaultValue;
	}

	public static ClassDefinition loadWS(ConfigImpl config, Struct root, ClassDefinition defaultValue) {
		try {
			Struct ws = ConfigUtil.getAsStruct("webservice", root);
			ClassDefinition cd = ws != null ? getClassDefinition(config, ws, "", config.getIdentification()) : null;
			if (cd != null && !StringUtil.isEmpty(cd.getClassName())) {
				return cd;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
	}

	public static ClassDefinition loadORMClass(ConfigImpl config, Struct root) {

		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM);
			Struct orm = ConfigUtil.getAsStruct("orm", root);

			// engine

			ClassDefinition cd = null;
			if (orm != null) {
				cd = getClassDefinition(config, orm, "engine", config.getIdentification());
				if (cd == null || cd.isClassNameEqualTo(DummyORMEngine.class.getName()) || cd.isClassNameEqualTo("lucee.runtime.orm.hibernate.HibernateORMEngine"))
					cd = getClassDefinition(config, orm, "", config.getIdentification());

				if (cd != null && (cd.isClassNameEqualTo(DummyORMEngine.class.getName()) || cd.isClassNameEqualTo("lucee.runtime.orm.hibernate.HibernateORMEngine"))) cd = null;
			}

			if (cd == null || !cd.hasClass()) {
				cd = DUMMY_ORM_ENGINE;
			}
			return cd;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return DUMMY_ORM_ENGINE;
	}

	public static ORMConfiguration loadORMConfig(ConfigImpl config, Struct root, ORMConfiguration defaultValue) {
		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM);
			Struct orm = ConfigUtil.getAsStruct("orm", root);

			// config
			ORMConfiguration def = null;
			ORMConfiguration ormConfig = root == null ? def : ORMConfigurationImpl.load(config, null, orm, config.getRootDirectory(), def);
			return ormConfig;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
	}

	public static short loadJava(ConfigImpl config, Struct root, short defaultValue) {
		try {

			String strCompileType = getAttr(config, root, "compileType");
			if (!StringUtil.isEmpty(strCompileType)) {
				strCompileType = strCompileType.trim().toLowerCase();
				if (strCompileType.equals("after-startup")) {
					return Config.RECOMPILE_AFTER_STARTUP;
				}
				else if (strCompileType.equals("always")) {
					return Config.RECOMPILE_ALWAYS;
				}
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
	}

	public static JavaSettings loadJavaSettings(ConfigImpl config, Struct root, JavaSettings defaultValue) {
		try {

			Resource lib = config.getLibraryDirectory();
			Resource[] libs = lib.listResources(ExtensionResourceFilter.EXTENSION_JAR_NO_DIR);

			Struct javasettings = ConfigUtil.getAsStruct(config, root, false, "javasettings");

			JavaSettings js = JavaSettingsImpl.getInstance(config, javasettings, libs);
			return js;

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
	}

	public static Struct loadConstants(ConfigImpl config, Struct root, Struct defaultValue) {
		try {
			Struct constants = ConfigUtil.getAsStruct("constants", root);

			// Constants
			Struct sct = null;
			if (sct == null) sct = new StructImpl();
			Key name;
			if (constants != null) {
				Iterator<Entry<Key, Object>> it = constants.entryIterator();
				Struct con;
				Entry<Key, Object> e;
				while (it.hasNext()) {
					try {
						e = it.next();
						con = Caster.toStruct(it.next(), null);
						if (con == null) continue;

						name = e.getKey();
						if (StringUtil.isEmpty(name)) continue;
						sct.setEL(name, e.getValue());

					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}

				}
			}
			return sct;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
	}

	public static void log(Config config, Throwable e) {
		try {
			// Log log = ((ConfigPro) config).getLog("application", false);
			// if (log != null) log.error("configuration", e);
			LogUtil.logGlobal(config, ConfigFactoryImpl.class.getName(), e);
		}
		catch (Throwable th) {
			ExceptionUtil.rethrowIfNecessary(th);
			th.printStackTrace();
		}
	}

	public static void log(Config config, int level, String message) {
		try {
			// Log log = ((ConfigPro) config).getLog("application", false);
			// if (log != null) log.error("configuration", message);
			LogUtil.logGlobal(config, level, ConfigFactoryImpl.class.getName(), message);
		}
		catch (Throwable th) {
			ExceptionUtil.rethrowIfNecessary(th);
			th.printStackTrace();
		}
	}

	public static Map<String, Startup> loadStartupHook(ConfigImpl config, Struct root) {
		Map<String, Startup> startups = new HashMap<>();
		try {
			Array children = ConfigUtil.getAsArray("startupHooks", root);

			if (children == null || children.size() == 0) return startups;

			Iterator<?> it = children.getIterator();
			Struct child;
			while (it.hasNext()) {
				try {
					child = Caster.toStruct(it.next());
					if (child == null) continue;

					// class
					ClassDefinition cd = getClassDefinition(config, child, "", config.getIdentification());
					ConfigBase.Startup existing = startups.get(cd.getClassName());

					if (existing != null) {
						if (existing.cd.equals(cd)) continue;
						try {
							Method fin = Reflector.getMethod(existing.instance.getClass(), "finalize", new Class[0], true, null);
							if (fin != null) {
								fin.invoke(existing.instance, new Object[0]);
							}
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
						}
					}
					Class clazz = cd.getClazz();

					Constructor constr = Reflector.getConstructor(clazz, new Class[] { Config.class }, null);
					if (constr != null) startups.put(cd.getClassName(), new ConfigBase.Startup(cd, constr.newInstance(new Object[] { config })));
					else startups.put(cd.getClassName(), new ConfigBase.Startup(cd, ClassUtil.loadInstance(clazz)));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, t);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return startups;
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @throws IOException
	 */
	public static void loadMail(ConfigImpl config, Struct root) { // does no init values

		boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManager.TYPE_MAIL);

		// Send partial
		try {
			String strSendPartial = getAttr(config, root, "mailSendPartial");
			if (!StringUtil.isEmpty(strSendPartial) && hasAccess) {
				config.setMailSendPartial(toBoolean(strSendPartial, false));
			}
			else {
				config.setMailSendPartial(false);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
			config.setMailSendPartial(false);
		}

		// User set
		try {
			String strUserSet = getAttr(config, root, "mailUserSet");
			if (!StringUtil.isEmpty(strUserSet) && hasAccess) {
				config.setUserSet(toBoolean(strUserSet, true));
			}
			else {
				config.setUserSet(true);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
			config.setUserSet(true);
		}

		// Spool Interval
		try {
			String strSpoolInterval = getAttr(config, root, "mailSpoolInterval");
			if (!StringUtil.isEmpty(strSpoolInterval) && hasAccess) {
				config.setMailSpoolInterval(Caster.toIntValue(strSpoolInterval, 30));
			}
			else {
				config.setMailSpoolInterval(30);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
			config.setMailSpoolInterval(30);
		}

		// Encoding
		try {
			String strEncoding = getAttr(config, root, "mailDefaultEncoding");
			if (!StringUtil.isEmpty(strEncoding, true) && hasAccess) {
				config.setMailDefaultEncoding(strEncoding);
			}
			else {
				config.setMailDefaultEncoding(CharsetUtil.UTF8);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
			config.setMailDefaultEncoding(CharsetUtil.UTF8);
		}

		// Spool Enable
		try {
			String strSpoolEnable = getAttr(config, root, "mailSpoolEnable");
			if (!StringUtil.isEmpty(strSpoolEnable) && hasAccess) {
				config.setMailSpoolEnable(toBoolean(strSpoolEnable, true));
			}
			else {
				config.setMailSpoolEnable(true);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
			config.setMailSpoolEnable(true);
		}

		// Timeout
		try {
			String strTimeout = getAttr(config, root, "mailConnectionTimeout");
			if (!StringUtil.isEmpty(strTimeout) && hasAccess) {
				config.setMailTimeout(Caster.toIntValue(strTimeout, 30));
			}
			else {
				config.setMailTimeout(30);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
			config.setMailTimeout(30);
		}

		// Servers
		List<Server> servers = new ArrayList<Server>();
		try {
			int index = 0;
			// Server[] servers = null;
			Array elServers = ConfigUtil.getAsArray("mailServers", root);

			// TODO get mail servers from env var
			if (hasAccess) {
				Iterator<?> it = elServers.getIterator();
				Struct el;
				int i = -1;
				while (it.hasNext()) {
					try {
						el = Caster.toStruct(it.next(), null);
						if (el == null) continue;
						i++;
						servers.add(i,
								new ServerImpl(Caster.toIntValue(getAttr(config, el, "id"), i + 1), getAttr(config, el, "smtp"), Caster.toIntValue(getAttr(config, el, "port"), 25),
										getAttr(config, el, "username"), ConfigUtil.decrypt(getAttr(config, el, "password")), toLong(getAttr(config, el, "life"), 1000 * 60 * 5),
										toLong(getAttr(config, el, "idle"), 1000 * 60 * 1), toBoolean(getAttr(config, el, "tls"), false),
										toBoolean(getAttr(config, el, "ssl"), false), toBoolean(getAttr(config, el, "reuseConnection"), true), ServerImpl.TYPE_GLOBAL));

					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}
			config.setMailServers(servers.toArray(new Server[servers.size()]));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
			config.setMailServers(servers.toArray(new Server[servers.size()]));
		}
	}

	public static void loadMonitors(ConfigImpl config, Struct root) {
		try {
			// only load in server context
			ConfigServerImpl configServer = (ConfigServerImpl) config;
			Struct parent = ConfigUtil.getAsStruct("monitoring", root);
			Array children = ConfigUtil.getAsArray("monitor", parent);

			java.util.List<IntervallMonitor> intervalls = new ArrayList<IntervallMonitor>();
			java.util.List<RequestMonitor> requests = new ArrayList<RequestMonitor>();
			java.util.List<MonitorTemp> actions = new ArrayList<MonitorTemp>();
			String strType, name;
			ClassDefinition cd;
			boolean _log, async;
			short type;
			Iterator<?> it = children.getIterator();
			Struct el;
			while (it.hasNext()) {
				try {
					el = Caster.toStruct(it.next(), null);
					if (el == null) continue;

					cd = getClassDefinition(config, el, "", config.getIdentification());
					strType = getAttr(config, el, "type");
					name = getAttr(config, el, "name");
					async = Caster.toBooleanValue(getAttr(config, el, "async"), false);
					_log = Caster.toBooleanValue(getAttr(config, el, "log"), true);

					if ("request".equalsIgnoreCase(strType)) type = IntervallMonitor.TYPE_REQUEST;
					else if ("action".equalsIgnoreCase(strType)) type = Monitor.TYPE_ACTION;
					else type = IntervallMonitor.TYPE_INTERVAL;

					if (cd.hasClass() && !StringUtil.isEmpty(name)) {
						name = name.trim();
						try {
							Class clazz = cd.getClazz();
							Object obj;
							ConstructorInstance constr = Reflector.getConstructorInstance(clazz, new Object[] { configServer }, false);
							if (constr.getConstructor(null) != null) obj = constr.invoke();
							else obj = ClassUtil.newInstance(clazz);
							LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(),
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
								LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(),
										"initialize " + (strType) + " monitor [" + clazz.getName() + "]");
								requests.add(m);
							}
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
							LogUtil.logGlobal(ThreadLocalPageContext.getConfig(configServer == null ? config : configServer), ConfigFactoryImpl.class.getName(), t);
						}
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					log(config, t);
				}
			}
			configServer.setRequestMonitors(requests.toArray(new RequestMonitor[requests.size()]));
			configServer.setIntervallMonitors(intervalls.toArray(new IntervallMonitor[intervalls.size()]));
			ActionMonitorCollector actionMonitorCollector = ActionMonitorFatory.getActionMonitorCollector(configServer, actions.toArray(new MonitorTemp[actions.size()]));
			configServer.setActionMonitorCollector(actionMonitorCollector);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 * @throws PageException
	 */
	public static ClassDefinition<SearchEngine> loadSearchClass(ConfigImpl config, Struct root) {
		try {
			Struct search = ConfigUtil.getAsStruct("search", root);

			// class
			ClassDefinition<SearchEngine> cd = search != null ? getClassDefinition(config, search, "engine", config.getIdentification()) : null;
			if (cd == null || !cd.hasClass() || "lucee.runtime.search.lucene.LuceneSearchEngine".equals(cd.getClassName())) {
				cd = new ClassDefinitionImpl(DummySearchEngine.class);
			}

			return cd;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return new ClassDefinitionImpl(DummySearchEngine.class);
	}

	public static String loadSearchDir(ConfigImpl config, Struct root) {
		try {
			Struct search = ConfigUtil.getAsStruct("search", root);

			// directory
			String dir = search != null ? getAttr(config, search, "directory") : null;
			if (StringUtil.isEmpty(dir)) {
				dir = "{lucee-web}/search/";
			}

			return dir;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return "{lucee-web}/search/";
	}

	public static int loadDebugOptions(ConfigImpl config, Struct root) {
		int options = 0;
		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);

			// debug options
			String strDebugOption = SystemUtil.getSystemPropOrEnvVar("lucee.debugging.options", null);
			String[] debugOptions = StringUtil.isEmpty(strDebugOption) ? null : ListUtil.listToStringArray(strDebugOption, ',');

			String str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingDatabase", null);
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingShowDatabase");
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingDatabase");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_DATABASE;
			}
			else if (debugOptions != null && extractDebugOption("database", debugOptions)) options += ConfigPro.DEBUG_DATABASE;

			str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingException", null);
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingShowException");
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingException");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_EXCEPTION;
			}
			else if (debugOptions != null && extractDebugOption("exception", debugOptions)) options += ConfigPro.DEBUG_EXCEPTION;

			str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingTemplate", null);
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingShowTemplate");
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingTemplate");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_TEMPLATE;
			}
			else if (debugOptions != null && extractDebugOption("template", debugOptions)) options += ConfigPro.DEBUG_TEMPLATE;

			str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingDump", null);
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingShowDump");
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingDump");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_DUMP;
			}
			else if (debugOptions != null && extractDebugOption("dump", debugOptions)) options += ConfigPro.DEBUG_DUMP;

			str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingTracing", null);
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingShowTracing");
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingShowTrace");
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingTracing");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_TRACING;
			}
			else if (debugOptions != null && extractDebugOption("tracing", debugOptions)) options += ConfigPro.DEBUG_TRACING;

			str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingTimer", null);
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingShowTimer");
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingTimer");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_TIMER;
			}
			else if (debugOptions != null && extractDebugOption("timer", debugOptions)) options += ConfigPro.DEBUG_TIMER;

			str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingImplicitAccess", null);
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingShowImplicitAccess");
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingImplicitAccess");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS;
			}
			else if (debugOptions != null && extractDebugOption("implicit-access", debugOptions)) options += ConfigPro.DEBUG_IMPLICIT_ACCESS;

			str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingQueryUsage", null);
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingShowQueryUsage");
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingQueryUsage");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_QUERY_USAGE;
			}
			else if (debugOptions != null && extractDebugOption("queryUsage", debugOptions)) options += ConfigPro.DEBUG_QUERY_USAGE;

			str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.debuggingThread", null);
			if (StringUtil.isEmpty(str)) str = getAttr(config, root, "debuggingThread");
			if (hasAccess && !StringUtil.isEmpty(str)) {
				if (toBoolean(str, false)) options += ConfigPro.DEBUG_THREAD;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return options;
	}

	private static boolean extractDebugOption(String name, String[] values) {
		for (String val: values) {
			if (StringUtil.emptyIfNull(val).trim().equalsIgnoreCase(name)) return true;
		}
		return false;
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 */
	public static Map<String, CFXTagClass> loadCFX(ConfigImpl config, Struct root) {
		Map<String, CFXTagClass> map = MapFactory.<String, CFXTagClass>getConcurrentMap();
		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManager.TYPE_CFX_SETTING);

			if (hasAccess) {
				System.setProperty("cfx.bin.path", config.getConfigDir().getRealResource("bin").getAbsolutePath());

				// Java CFX Tags
				Struct cfxs = ConfigUtil.getAsStruct("cfx", root);
				Iterator<Entry<Key, Object>> it = cfxs.entryIterator();
				Struct cfxTag;
				Entry<Key, Object> entry;
				while (it.hasNext()) {
					try {
						entry = it.next();
						cfxTag = Caster.toStruct(entry.getValue(), null);
						if (cfxTag == null) continue;

						String type = getAttr(config, cfxTag, "type");
						if (type != null) {
							// Java CFX Tags
							if ("java".equalsIgnoreCase(type)) {
								String name = entry.getKey().getString();
								ClassDefinition cd = getClassDefinition(config, cfxTag, "", config.getIdentification());
								if (!StringUtil.isEmpty(name) && cd.hasClass()) {
									map.put(name.toLowerCase(), new JavaCFXTagClass(name, cd));
								}
							}
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}

			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return map;
	}

	/**
	 * loads the bundles defined in the extensions
	 * 
	 * @param cs
	 * @param config
	 * @param doc
	 * @param log
	 */
	private static void _loadExtensionBundles(ConfigServerImpl config, Struct root) {
		Log deployLog = config.getLog("deploy");

		try {
			Array children = ConfigUtil.getAsArray("extensions", root);
			String md5 = CollectionUtil.md5(children);
			if (md5.equals(config.getExtensionsMD5())) {
				return;
			}
			// config.getExtensionDefinitions();
			boolean firstLoad = config.getExtensionsMD5() == null;

			try {
				RHExtension.removeDuplicates(children);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				log(config, t);
			}

			Map<String, RHExtension> extensionsConfig = new ConcurrentHashMap<>();

			{
				String strBundles;
				RHExtension rhe;
				Iterator<Object> it = children.valueIterator();
				Entry<Key, Object> e;
				Struct child;
				String id;
				// load and install extension if necessary
				while (it.hasNext()) {
					child = Caster.toStruct(it.next(), null);
					if (child == null) continue;
					id = getAttr(config, child, KeyConstants._id);
					BundleInfo[] bfsq;
					try {
						String strRes = getAttr(config, child, KeyConstants._resource, KeyConstants._path, KeyConstants._url);
						if (StringUtil.isEmpty(id) && StringUtil.isEmpty(strRes)) continue;

						Resource res = null;
						if (!StringUtil.isEmpty(strRes, true)) {
							res = ResourceUtil.toResourceExisting(config, strRes, null);
							if (res == null) {
								if (!StringUtil.isEmpty(id, true)) {
									log(config, Log.LEVEL_ERROR, "the resource [" + strRes + "] from the extension [" + id + "] cannot be resolved");
								}
								else {
									log(config, Log.LEVEL_ERROR, "the extension resource [" + strRes + "] cannot be resolved");
								}
							}
							else {
								if (!StringUtil.isEmpty(id, true)) {
									log(config, Log.LEVEL_INFO, "the resource [" + strRes + "] from the extension [" + id + "] is valid");
								}
								else {
									log(config, Log.LEVEL_INFO, "the extension resource [" + strRes + "] is valid");
								}
							}
						}

						rhe = RHExtension.installExtension(config, id, getAttr(config, child, KeyConstants._version), res, false);
						// startBundles(config, rhe, firstLoad);
						extensionsConfig.put(rhe.getExtensionInstalledName(), rhe);
						// installedFiles.add(rhe.getExtensionFile());
						// installedIds.add(rhe.getId());
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
						continue;
					}
				}
			}

			// start bundles in parallel but wait for them to finish
			CountDownLatch latch = new CountDownLatch(extensionsConfig.size());
			ExecutorService executor = ThreadUtil.createExecutorService();
			try {

				for (RHExtension ext: extensionsConfig.values()) {
					executor.submit(() -> {
						try {
							// Call the startBundles method for each extension
							startBundles(config, ext, firstLoad);
						}
						catch (Exception e) {
							if (deployLog != null) deployLog.error("start-bundles", e);
						}
						finally {
							// Count down the latch regardless of success or failure
							latch.countDown();
						}
					});
				}

				// Wait for all virtual threads to complete
				try {
					latch.await();
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Interrupted while waiting for extension processing", e);
				}
			}
			finally {
				ThreadUtil.close(executor);
			}

			/*
			 * for (RHExtension ext: extensions) { installedFiles.add(ext.getExtensionFile());
			 * installedIds.add(ext.getId()); startBundles(config, ext, firstLoad); }
			 */

			// uninstall extensions no longer used
			Boolean cleanupExtension = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.cleanup.extension", null), true);
			if (cleanupExtension) {
				Map<String, Resource> installed = RHExtension.loadExtensionInstalledFiles(config);
				if (installed != null) {
					ResetFilter filter = new ResetFilter();
					try {

						for (Resource r: installed.values()) {

							// is this extension file not in the config
							if (!extensionsConfig.containsKey(r.getName())) {
								RHExtension ext = RHExtension.getInstance(config, r);

								RHExtension match = null;
								for (RHExtension e: extensionsConfig.values()) {
									if (e.getId().equals(ext.getId())) {
										match = e;
										break;
									}
								}

								// maybe it got updated and the extension file was not removed
								if (match != null) {

									if (deployLog != null) deployLog.info("extension", "Found the extension [" + ext
											+ "] in the installed folder that is in a different version in the configuraton [" + match + "], so we delete that extension file.");
									RHExtension.removeExtensionInstalledFile(config, r.getName());
								}
								// the extension no longer configured, sowe remove it
								else {
									if (deployLog != null) deployLog.info("extension", "Found the extension [" + ext
											+ "] in the installed folder that is not present in the configuration in any version, so we will uninstall it");
									ConfigAdmin._removeRHExtension(config, ext, null, filter, true);
									if (deployLog != null) deployLog.info("extension", "removed extension [" + ext + "]");
								}

							}

						}
					}
					finally {
						filter.reset(config);
					}
				}
			}
			// set
			config.setExtensions(extensionsConfig.values().toArray(new RHExtension[extensionsConfig.size()]), md5);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
	}

	private static void startBundles(ConfigServerImpl config, RHExtension rhe, boolean firstLoad) throws IOException, BundleException {
		if (rhe.getMetadata().isStartBundles()) {
			if (!firstLoad) {
				rhe.deployBundles(config, true);
			}
			else {
				try {
					BundleInfo[] bundles = rhe.getMetadata().getBundles();
					if (bundles != null) {
						for (BundleInfo bi: bundles) {
							OSGiUtil.loadBundleFromLocal(bi.getSymbolicName(), bi.getVersion(), null, false, null);
						}
					}
				}
				catch (Exception ex) {
					rhe.deployBundles(config, true);
				}
			}
		}

	}

	public static List<ExtensionDefintion> loadExtensionDefinition(ConfigImpl config, Struct root) {
		List<ExtensionDefintion> extensions = new ArrayList<>();

		try {
			Log deployLog = config.getLog("deploy");
			Array children = ConfigUtil.getAsArray("extensions", root);
			RHExtension.removeDuplicates(children);
			RHExtension.removeDisabled(children);
			if (children.size() > 0) {
				// Use a thread pool for processing
				ExecutorService executor = ThreadUtil.createExecutorService(children.size(), true);
				List<Future<ExtensionDefintion>> futures = new ArrayList<>();

				Iterator<Object> it = children.valueIterator();
				while (it.hasNext()) {
					Struct childSct = Caster.toStruct(it.next(), null);
					if (childSct == null) continue;

					// Submit each task to the thread pool
					futures.add(executor.submit(() -> {
						Map<String, String> child = Caster.toStringMap(childSct, null);
						if (child == null) return null;

						String id = getAttr(config, childSct, KeyConstants._id);
						try {
							return RHExtension.toExtensionDefinition(config, id, child);
						}
						catch (Exception e) {
							log(config, e);
							return null;
						}
					}));
				}

				// Collect results
				for (Future<ExtensionDefintion> future: futures) {
					try {
						ExtensionDefintion extDef = future.get();
						if (extDef != null) {
							extensions.add(extDef);
						}
					}
					catch (Exception ex) {
						log(config, ex);
					}
				}
				executor.shutdown();
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return extensions;
	}

	public static List<ExtensionDefintion> loadExtensionDefinitionSerial(ConfigImpl config, Struct root) {
		List<ExtensionDefintion> extensions = new ArrayList<>();
		try {
			Log deployLog = config.getLog("deploy");
			Array children = ConfigUtil.getAsArray("extensions", root);
			// set
			Map<String, String> child;
			Struct childSct;
			String id;
			Iterator<Object> it = children.valueIterator();
			while (it.hasNext()) {
				childSct = Caster.toStruct(it.next(), null);
				if (childSct == null) continue;
				child = Caster.toStringMap(childSct, null);

				if (child == null) continue;
				id = getAttr(config, childSct, KeyConstants._id);

				try {
					extensions.add(RHExtension.toExtensionDefinition(config, id, child));
				}
				catch (Exception e) {
					log(config, e);
				}
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return extensions;
	}

	public static RHExtensionProvider[] loadExtensionProviders(ConfigImpl config, Struct root) {
		Map<RHExtensionProvider, String> providers = new LinkedHashMap<RHExtensionProvider, String>();
		try {
			// providers
			Array xmlProviders = ConfigUtil.getAsArray("extensionProviders", root);
			String strProvider;

			for (int i = 0; i < Constants.RH_EXTENSION_PROVIDERS.length; i++) {
				providers.put(Constants.RH_EXTENSION_PROVIDERS[i], "");
			}
			if (xmlProviders != null) {
				Iterator<?> it = xmlProviders.valueIterator();
				String url;
				while (it.hasNext()) {
					url = Caster.toString(it.next(), null);
					if (StringUtil.isEmpty(url, true)) continue;

					try {
						providers.put(new RHExtensionProvider(url.trim(), false), "");
					}
					catch (MalformedURLException e) {
						LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), ConfigFactoryImpl.class.getName(), e);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return providers.keySet().toArray(new RHExtensionProvider[providers.size()]);
	}

	/**
	 * @param configServer
	 * @param config
	 * @param doc
	 * @return
	 * @throws IOException
	 */
	public static Mapping[] loadComponentMappings(ConfigImpl config, Struct root) {
		Mapping[] mappings = null;
		try {
			boolean hasSet = false;

			// Web Mapping
			Array compMappings = ConfigUtil.getAsArray(config, root, true, KeyConstants._virtual, KeyConstants._physical, false, "componentMappings", "componentPaths");
			hasSet = false;
			boolean hasDefault = false;
			if (compMappings.size() > 0) {
				Iterator<Object> it = compMappings.valueIterator();
				List<Mapping> list = new ArrayList<>();
				Struct cMapping;
				while (it.hasNext()) {
					try {
						cMapping = Caster.toStruct(it.next(), null);
						if (cMapping == null) continue;

						String virtual = createVirtual(config, cMapping);
						String physical = getAttr(config, cMapping, "physical");
						String archive = getAttr(config, cMapping, "archive");
						boolean readonly = toBoolean(getAttr(config, cMapping, "readonly"), false);
						boolean hidden = toBoolean(getAttr(config, cMapping, "hidden"), false);
						if ("{lucee-web}/components/".equals(physical) || "{lucee-server}/components/".equals(physical)) continue;
						if ("{lucee-config}/components/".equals(physical)) hasDefault = true;

						String strListMode = getAttr(config, cMapping, "listenerMode");
						if (StringUtil.isEmpty(strListMode)) strListMode = getAttr(config, cMapping, "listener-mode");
						if (StringUtil.isEmpty(strListMode)) strListMode = getAttr(config, cMapping, "listenermode");
						int listMode = ConfigUtil.toListenerMode(strListMode, -1);

						String strListType = getAttr(config, cMapping, "listenerType");
						if (StringUtil.isEmpty(strListType)) strListMode = getAttr(config, cMapping, "listener-type");
						if (StringUtil.isEmpty(strListType)) strListMode = getAttr(config, cMapping, "listenertype");
						int listType = ConfigUtil.toListenerType(strListType, -1);

						short inspTemp = inspectTemplate(config, cMapping);
						int insTempSlow = Caster.toIntValue(getAttr(config, cMapping, "inspectTemplateIntervalSlow"), -1);
						int insTempFast = Caster.toIntValue(getAttr(config, cMapping, "inspectTemplateIntervalFast"), -1);

						String primary = getAttr(config, cMapping, "primary");

						boolean physicalFirst = archive == null || !"archive".equalsIgnoreCase(primary);
						hasSet = true;
						list.add(new MappingImpl(config, virtual, physical, archive, inspTemp, insTempSlow, insTempFast, physicalFirst, hidden, readonly, true, false, true, null,
								listMode, listType));
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						log(config, t);
					}
				}
				if (!hasDefault) {
					list.add(new MappingImpl(config, "/default", "{lucee-config}/components/", null, ConfigPro.INSPECT_NEVER, -1, -1, true, false, true, true, false, true, null,
							-1, -1));
				}
				mappings = list.toArray(new Mapping[list.size()]);
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}

		if (mappings == null) {
			mappings = new Mapping[] { new MappingImpl(config, "/default-component", "{lucee-config}/components/", null, ConfigPro.INSPECT_UNDEFINED,
					ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, true, true, true, false, true, null, -1, -1) };
		}
		return mappings;

	}

	public static void loadProxy(ConfigServerImpl config, Struct root) {
		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
			Struct proxy = ConfigUtil.getAsStruct("proxy", root);

			boolean enabled = false;
			String server = null, username = null, password = null;
			int port = -1;
			if (proxy != null && proxy.size() > 0) {
				enabled = Caster.toBooleanValue(getAttr(config, proxy, "enabled"), true);
				server = getAttr(config, proxy, "server");
				username = getAttr(config, proxy, "username");
				password = getAttr(config, proxy, "password");
				port = Caster.toIntValue(getAttr(config, proxy, "port"), -1);
			}
			if (StringUtil.isEmpty(server, true)) {
				server = getAttr(config, root, "updateProxyHost");
				username = getAttr(config, root, "updateProxyUsername");
				password = getAttr(config, root, "updateProxyPassword");
				port = Caster.toIntValue(getAttr(config, root, "updateProxyPort"), -1);
				enabled = !StringUtil.isEmpty(server, true);

			}

			// includes/excludes
			Set<String> includes = proxy != null ? ProxyDataImpl.toStringSet(getAttr(config, proxy, "includes")) : null;
			Set<String> excludes = proxy != null ? ProxyDataImpl.toStringSet(getAttr(config, proxy, "excludes")) : null;

			if (enabled && hasAccess && !StringUtil.isEmpty(server)) {
				ProxyDataImpl pd = (ProxyDataImpl) ProxyDataImpl.getInstance(server, port, username, password);
				pd.setExcludes(excludes);
				pd.setIncludes(includes);
				config.setProxyData(pd);

			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
	}

	public static boolean loadError(ConfigImpl config, Struct root, boolean defaultValue) {
		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);

			// status code
			Boolean bStausCode = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.status.code", null), null);
			if (bStausCode == null) bStausCode = Caster.toBoolean(getAttr(config, root, "errorStatusCode"), null);

			if (bStausCode != null && hasAccess) {
				return bStausCode.booleanValue();
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
	}

	public static Regex loadRegex(ConfigImpl config, Struct root, Regex defaultValue) {
		try {
			boolean hasAccess = ConfigUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

			String strType = getAttr(config, root, "regexType");
			int type = StringUtil.isEmpty(strType) ? RegexFactory.TYPE_UNDEFINED : RegexFactory.toType(strType, RegexFactory.TYPE_UNDEFINED);

			if (hasAccess && type != RegexFactory.TYPE_UNDEFINED) {
				return RegexFactory.toRegex(type, null);
			}
			else return RegexFactory.toRegex(RegexFactory.TYPE_PERL, null);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			log(config, t);
		}
		return defaultValue;
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

	public static String getAttr(Config config, Struct data, String name) {
		String v = ConfigUtil.getAsString(name, data, null);
		if (v == null) {
			return null;
		}
		if (StringUtil.isEmpty(v)) return "";
		return ConfigUtil.replaceConfigPlaceHolder(config, v);
	}

	public static String getAttr(Config config, Struct data, String name, String alias) {
		String v = ConfigUtil.getAsString(name, data, null);
		if (v == null) v = ConfigUtil.getAsString(alias, data, null);
		if (v == null) return null;
		if (StringUtil.isEmpty(v)) return "";
		return ConfigUtil.replaceConfigPlaceHolder(config, v);
	}

	public static String getAttr(Config config, Struct data, String[] names) {
		String v;
		for (String name: names) {
			v = ConfigUtil.getAsString(name, data, null);
			if (!StringUtil.isEmpty(v)) return ConfigUtil.replaceConfigPlaceHolder(config, v);
		}
		return null;
	}

	public static String getAttr(Config config, Struct data, lucee.runtime.type.Collection.Key... names) {
		String v;
		for (lucee.runtime.type.Collection.Key name: names) {
			v = ConfigUtil.getAsString(name, data, null);
			if (!StringUtil.isEmpty(v)) return ConfigUtil.replaceConfigPlaceHolder(config, v);
		}
		return null;
	}

	public static Resource getConfigFile(Resource configDir, boolean server, boolean returnOnlyWhenExist) throws IOException {
		if (server) {
			// lucee.base.config
			String customCFConfig = SystemUtil.getSystemPropOrEnvVar("lucee.base.config", null);
			Resource configFile = null;
			if (!StringUtil.isEmpty(customCFConfig, true)) {

				configFile = ResourcesImpl.getFileResourceProvider().getResource(customCFConfig.trim());

				if (configFile.isFile()) {
					LogUtil.log(Log.LEVEL_INFO, "deploy", "config", "using config File : " + configFile);
					return configFile;
				}
				throw new IOException(
						"the config file [" + configFile + "] defined with the environment variable [LUCEE_BASE_CONFIG] or system property [-Dlucee.base.config] does not exist.");
			}
		}
		Resource res;
		for (String cf: ConfigFactoryImpl.CONFIG_FILE_NAMES) {
			res = configDir.getRealResource(cf);
			if (res.isFile()) return res;
		}

		if (returnOnlyWhenExist) {
			return null;
		}
		// default location
		return configDir.getRealResource(ConfigFactoryImpl.CONFIG_FILE_NAMES[0]);
	}

	public static boolean isConfigFileName(String fileName) {
		for (String fn: ConfigFactoryImpl.CONFIG_FILE_NAMES) {
			if (fn.equalsIgnoreCase(fileName)) return true;
		}
		return false;
	}

	public static Map<String, String> loadLabel(ConfigImpl configServer, Struct root) {
		Array children = ConfigUtil.getAsArray("labels", "label", root);

		Map<String, String> labels = new HashMap<String, String>();
		if (children != null) {
			Iterator<?> it = children.getIterator();
			Struct data;
			while (it.hasNext()) {
				data = Caster.toStruct(it.next(), null);
				if (data == null) continue;
				String id = ConfigUtil.getAsString("id", data, null);
				String name = ConfigUtil.getAsString("name", data, null);
				if (id != null && name != null) {
					labels.put(id, name);
				}
			}
		}
		return labels;
	}

	private static void createContextFiles(Resource configDir, ConfigServer config, boolean doNew) {
		// context
		{
			Resource contextDir = configDir.getRealResource("context");
			// lucee-admin (only deploy if enabled)
			if (Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.admin.enabled", "true"), true)) {
				Resource f = contextDir.getRealResource("lucee-admin.lar");
				if (!f.exists() || doNew) createFileFromResourceEL("/resource/context/lucee-admin.lar", f);
				else ConfigFactoryImpl.createFileFromResourceCheckSizeDiffEL("/resource/context/lucee-admin.lar", f);
			}

			create("/resource/context/", new String[] { "lucee-context.lar", "lucee-doc.lar", "component-dump.cfm", "Application.cfc", "form.cfm", "graph.cfm", "wddx.cfm",
					"admin.cfm", "formtag-form.cfm" }, contextDir, doNew);
		}

		// customtags
		if (doNew) {
			Resource ctDir = configDir.getRealResource("customtags");
			if (!ctDir.exists()) ctDir.mkdirs();
		}

		// gateway
		if (doNew) {
			Resource gwDir = configDir.getRealResource("components/lucee/extension/gateway/");
			create("/resource/context/gateway/", new String[] { "TaskGateway.cfc", "DummyGateway.cfc", "DirectoryWatcher.cfc", "DirectoryWatcherListener.cfc", "WatchService.cfc",
					"MailWatcher.cfc", "MailWatcherListener.cfc", "AsynchronousEvents.cfc", "AsynchronousEventsListener.cfc" }, gwDir, doNew);
		}

		// error
		if (doNew) {
			Resource errorDir = configDir.getRealResource("context/templates/error");
			create("/resource/context/templates/error/", new String[] { "error.cfm", "error-neo.cfm", "error-public.cfm" }, errorDir, doNew);
		}

		// display
		if (doNew) {
			Resource displayDir = configDir.getRealResource("context/templates/display");
			if (!displayDir.exists()) displayDir.mkdirs();
		}

		// Debug
		if (doNew) {
			Resource debug = configDir.getRealResource("context/admin/debug");
			create("/resource/context/admin/debug/", new String[] { "Debug.cfc", "Field.cfc", "Group.cfc", "Classic.cfc", "Simple.cfc", "Modern.cfc", "Comment.cfc" }, debug,
					doNew);
		}

		// Info
		if (doNew) {
			Resource info = configDir.getRealResource("context/admin/info");
			create("/resource/context/admin/info/", new String[] { "Info.cfc" }, info, doNew);
		}

		Resource wcdDir = configDir.getRealResource("web-context-deployment/admin");
		try {
			ResourceUtil.deleteEmptyFolders(wcdDir);
		}
		catch (IOException e) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(config), ConfigFactoryImpl.class.getName(), e);
		}

		// Security / SSL
		Resource secDir = configDir.getRealResource("security");
		Resource res = create("/resource/security/", "cacerts", secDir, false);
		if (SystemUtil.getSystemPropOrEnvVar("lucee.use.lucee.SSL.TrustStore", "").equalsIgnoreCase("true"))
			System.setProperty("javax.net.ssl.trustStore", res.toString());/* JAVJAK */
		// Allow using system proxies
		if (!SystemUtil.getSystemPropOrEnvVar("lucee.disable.systemProxies", "").equalsIgnoreCase("true")) System.setProperty("java.net.useSystemProxies", "true"); // it defaults
																																									// to false

		// deploy org.lucee.cfml components
		if (doNew) {
			ImportDefintion _import = ((ConfigPro) config).getComponentDefaultImport();
			String path = _import.getPackageAsPath();
			Resource components = config.getConfigDir().getRealResource("components");
			Resource dir = components.getRealResource(path);
			ComponentFactory.deploy(dir, doNew);
		}

		createContextFilesAdmin(configDir, config, doNew);
	}

	private static void createContextFilesAdmin(Resource configDir, ConfigServer config, boolean doNew) {

		// Plugin
		if (doNew) {
			Resource pluginDir = configDir.getRealResource("context/admin/plugin");
			create("/resource/context/admin/plugin/", new String[] { "Plugin.cfc" }, pluginDir, doNew);
		}
		// Plugin Note
		if (doNew) {
			Resource note = configDir.getRealResource("context/admin/plugin/Note");
			create("/resource/context/admin/plugin/Note/", new String[] { "language.xml", "overview.cfm", "Action.cfc" }, note, doNew);
		}

		// DB Drivers types
		if (doNew) {
			Resource typesDir = configDir.getRealResource("context/admin/dbdriver/types");
			create("/resource/context/admin/dbdriver/types/", new String[] { "IDriver.cfc", "Driver.cfc", "IDatasource.cfc", "IDriverSelector.cfc", "Field.cfc" }, typesDir, doNew);
		}

		if (doNew) {
			Resource dbDir = configDir.getRealResource("context/admin/dbdriver");
			create("/resource/context/admin/dbdriver/", new String[] { "Other.cfc" }, dbDir, doNew);
		}

		// Cache Drivers
		if (doNew) {
			Resource cDir = configDir.getRealResource("context/admin/cdriver");
			create("/resource/context/admin/cdriver/", new String[] { "Cache.cfc", "RamCache.cfc", "Field.cfc", "Group.cfc" }, cDir, doNew);
		}

		// AI Drivers
		if (doNew) {
			Resource aiDir = configDir.getRealResource("context/admin/aidriver");
			create("/resource/context/admin/aidriver/", new String[] { "AI.cfc", "Claude.cfc", "Gemini.cfc", "OpenAI.cfc", "Field.cfc", "Group.cfc" }, aiDir, doNew);
		}

		// Mail Server Drivers
		if (doNew) {
			Resource msDir = configDir.getRealResource("context/admin/mailservers");
			create("/resource/context/admin/mailservers/",
					new String[] { "Other.cfc", "GMail.cfc", "GMX.cfc", "iCloud.cfc", "Yahoo.cfc", "Outlook.cfc", "MailCom.cfc", "MailServer.cfc" }, msDir, doNew);
		}
		// Gateway Drivers
		if (doNew) {
			Resource gDir = configDir.getRealResource("context/admin/gdriver");
			create("/resource/context/admin/gdriver/",
					new String[] { "TaskGatewayDriver.cfc", "AsynchronousEvents.cfc", "DirectoryWatcher.cfc", "MailWatcher.cfc", "Gateway.cfc", "Field.cfc", "Group.cfc" }, gDir,
					doNew);
		}
		// Logging/appender
		if (doNew) {
			Resource app = configDir.getRealResource("context/admin/logging/appender");
			create("/resource/context/admin/logging/appender/",
					new String[] { "DatasourceAppender.cfc", "ConsoleAppender.cfc", "ResourceAppender.cfc", "Appender.cfc", "Field.cfc", "Group.cfc" }, app, doNew);
		}
		// Logging/layout
		if (doNew) {
			Resource lay = configDir.getRealResource("context/admin/logging/layout");
			create("/resource/context/admin/logging/layout/", new String[] { "DatadogLayout.cfc", "ClassicLayout.cfc", "HTMLLayout.cfc", "PatternLayout.cfc", "XMLLayout.cfc",
					"JsonLayout.cfc", "Layout.cfc", "Field.cfc", "Group.cfc" }, lay, doNew);
		}
	}

	public static class Path {
		public final String str;
		public final Resource res;

		public Path(String str, Resource res) {
			this.str = str;
			this.res = res;
		}

		public boolean isValidDirectory() {
			return res.isDirectory();
		}
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