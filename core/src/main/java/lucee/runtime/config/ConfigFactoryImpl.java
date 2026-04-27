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
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.osgi.framework.BundleException;
import org.xml.sax.SAXException;

import jakarta.servlet.ServletConfig;
import lucee.commons.date.TimeZoneUtil;
import lucee.commons.digest.HashUtil;
import lucee.commons.digest.MD5;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.retirement.RetireOutputStream;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
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
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.config.ConfigServerImpl.ConfigFile;
import lucee.runtime.config.component.ComponentFactory;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceImpl;
import lucee.runtime.db.ParamSyntax;
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
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.monitor.ActionMonitor;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibException;

public final class ConfigFactoryImpl extends ConfigFactory {

	private static final String TEMPLATE_EXTENSION = "cfm";
	private static final String COMPONENT_EXTENSION = "cfc";
	public static final boolean LOG = true;
	public static final int DEFAULT_MAX_CONNECTION = 100;
	public static final String DEFAULT_LOCATION = Constants.DEFAULT_UPDATE_URL.toExternalForm();

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

			// config file
			Resource configFile = getConfigFile(configDir, true, false);
			boolean hasConfig = configFile.exists() && configFile.length() > 0;

			if (!hasConfig) {
				LogUtil.logGlobal(Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "has no json server context config [" + configFile + "]");
			}
			ConfigServerImpl config = existing != null ? existing : new ConfigServerImpl(engine, initContextes, contextes, configDir, configFile, ui, essentialOnly, doNew);
			ThreadLocalConfigServer.register(config);
			// translate to new
			if (!hasConfig) {
				LogUtil.logGlobal(Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "create new server context json config file [" + configFile + "]");
				ConfigFile.createConfigFile(configFile);
				hasConfig = true;
			}
			LogUtil.logGlobal(Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "load config file");
			config.load(configFile);

			// admin mode
			load(config, essentialOnly);

			if (!essentialOnly) {
				createContextFiles(configDir, config, doNew);
				try {
					((CFMLEngineImpl) ConfigUtil.getEngine(config)).onStart(config, false);
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
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

		// we only update if the config file has changed outside ConfigAdmin
		// lastModified() == last modified by the
		if (!ConfigAdmin.wasConfigFileChangedOutside(configServer)) {
			// if we have a password.txt we only reload that
			if (configServer.getConfigDir().getRealResource("password.txt").isFile()) {
				configServer.createSaltAndPW();
			}
			return;
		}

		// we reload in case the config file was changed from outside

		load(configServer, quick);
		try {
			((CFMLEngineImpl) ConfigUtil.getEngine(configServer)).onStart(configServer, true);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
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
	synchronized static void load(ConfigServerImpl config, boolean essentialOnly) throws IOException {
		if (LOG) LogUtil.logGlobal(ThreadLocalPageContext.getConfigServer(config), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "start reading config");
		ThreadLocalConfig.register(config);

		if (LOG) log(config, Log.LEVEL_INFO, "loaded filesystem");
		if (!essentialOnly) {
			config.getRHExtensions();
			if (LOG) log(config, Log.LEVEL_INFO, "loaded extension");
		}

		if (!essentialOnly) {
			((CFMLEngineImpl) config.getEngine()).touchMonitor(config);
		}
		// Trigger startup hooks (lazy-loaded)
		config.getStartups();

		ConfigServerImpl.instance = config;
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

	public static ResourceProvider toDefaultResourceProvider(Class defaultProviderClass, Map arguments) throws ClassException {
		Object o = ClassUtil.loadInstance(defaultProviderClass);
		if (o instanceof ResourceProvider) {
			ResourceProvider rp = (ResourceProvider) o;
			rp.init(null, arguments);
			return rp;
		}
		else throw new ClassException("object [" + Caster.toClassName(o) + "] must implement the interface " + ResourceProvider.class.getName());
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

	public static Map<String, AIEngine> loadAI(ConfigServerImpl config, Struct root, Map<String, AIEngine> defaultValue) {
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
					data = (Struct) ConfigUtil.replacePlaceHolders(config, data);
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

	public static DumpWriterEntry[] loadDumpWriter(ConfigServerImpl config, Struct root, DumpWriterEntry[] defaultValue) {
		try {
			Array writers = ConfigUtil.getAsArray("dumpWriters", root);

			Struct sct = new StructImpl();

			final boolean hasPlain = false;
			final boolean hasRich = false;

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

	public static Map<String, String> toArguments(Struct coll, String name, boolean decode, boolean lowerKeys) throws PageException {
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
			LogUtil.logGlobal(Log.LEVEL_ERROR, ConfigFactoryImpl.class.getName(), resource);
			LogUtil.logGlobal(Log.LEVEL_ERROR, ConfigFactoryImpl.class.getName(), file + "");
			LogUtil.logGlobal(ConfigFactoryImpl.class.getName(), t);
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

			LogUtil.logGlobal(Log.LEVEL_DEBUG, ConfigFactoryImpl.class.getName(), "update file:" + file);
			LogUtil.logGlobal(Log.LEVEL_DEBUG, ConfigFactoryImpl.class.getName(), " - source:" + srcSize);
			LogUtil.logGlobal(Log.LEVEL_DEBUG, ConfigFactoryImpl.class.getName(), " - target:" + trgSize);

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

	public static ExecutionLogFactory loadExeLog(ConfigServerImpl config, Struct root) {
		try {
			Struct el = ConfigUtil.getAsStruct("executionLog", root);

			// Determine the execution log class first
			String strClass = getAttr(config, el, "class");
			Class<? extends ExecutionLog> clazz = null;
			Map<String, String> args = null;

			// If debugger breakpoint support enabled and no explicit class configured, use DebuggerExecutionLog
			if (StringUtil.isEmpty(strClass) && config.getExecutionLogEnabled()) {
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
					LogUtil.logGlobal(ThreadLocalPageContext.getConfigServer(config), ConfigFactoryImpl.class.getName(), t);
					clazz = ConsoleExecutionLog.class;
				}
				if (clazz != null) LogUtil.logGlobal(ThreadLocalPageContext.getConfigServer(config), Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(),
						"loaded ExecutionLog class " + clazz.getName());

				// arguments
				args = toArguments(el, "arguments", true, false);
				if (args == null) args = toArguments(el, "classArguments", true, false);
			}

			if (clazz == null) {
				clazz = ConsoleExecutionLog.class;
				args = new HashMap<String, String>();
			}

			ExecutionLogFactory factory = new ExecutionLogFactory(clazz, args);

			// Track ExecutionLog mode in marker file to detect config changes.
			// Format: "enabled:lineBased" (e.g. "true:true" for debugger, "true:false" for console)
			// If mode changes, purge cfclasses to force recompile with correct bytecode.
			String val = config.getExecutionLogEnabled() + ":" + factory.isLineBased();
			boolean hasChanged = false;

			try {
				Resource contextDir = config.getConfigDir();
				Resource exeLog = contextDir.getRealResource("exeLog");

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
				log(config, e);
			}

			if (hasChanged) {
				try {
					if (config.getClassDirectory().exists()) config.getClassDirectory().remove(true);
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

	private static void setDatasource(ConfigServerImpl config, Map<String, DataSource> datasources, String datasourceName, ClassDefinition cd, String server, String databasename,
			int port, String dsn, String bundleName, String bundleVersion, String user, String pass, TagListener listener, int connectionLimit, int idleTimeout, int liveTimeout,
			int minIdle, int maxIdle, int maxTotal, long metaCacheTimeout, boolean blob, boolean clob, int allow, boolean validate, boolean storage, String timezone, Struct custom,
			String dbdriver, ParamSyntax ps, boolean literalTimestampWithTSOffset, boolean alwaysSetTimeout, boolean requestExclusive, boolean alwaysResetConnections)
			throws BundleException, ClassException, SQLException {

		datasources.put(datasourceName.toLowerCase(),
				new DataSourceImpl(datasourceName, cd, server, dsn, bundleName, bundleVersion, databasename, port, user, pass, listener, connectionLimit, idleTimeout, liveTimeout,
						minIdle, maxIdle, maxTotal, metaCacheTimeout, blob, clob, allow, custom, false, validate, storage,
						StringUtil.isEmpty(timezone, true) ? null : TimeZoneUtil.toTimeZone(timezone, null), dbdriver, ps, literalTimestampWithTSOffset, alwaysSetTimeout,
						requestExclusive, alwaysResetConnections, ThreadLocalPageContext.getLog(config, "application")));

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
	public static void loadTag(ConfigServerImpl config, Struct root, boolean doNew) {
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

	public static void loadFunctions(ConfigServerImpl config, Struct rootMayNull, boolean doNew) {
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
					LogUtil.logGlobal(ConfigFactoryImpl.class.getName(), e);
				}
			}

		}
	}

	public static PrintStream toPrintStream(Config config, String streamtype, boolean iserror) {
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

	/**
	 * cast a string value to a boolean
	 * 
	 * @param value String value represent a booolean ("yes", "no","true" aso.)
	 * @param defaultValue if can't cast to a boolean is value will be returned
	 * @return boolean value
	 */
	public static boolean toBoolean(String value, boolean defaultValue) {

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
		return ((ConfigPro) config).replacePlaceHolder(v);
	}

	public static String getAttr(Config config, Struct data, String name, String alias) {
		String v = ConfigUtil.getAsString(name, data, null);
		if (v == null) v = ConfigUtil.getAsString(alias, data, null);
		if (v == null) return null;
		if (StringUtil.isEmpty(v)) return "";
		return ((ConfigPro) config).replacePlaceHolder(v);
	}

	public static String getAttr(Config config, Struct data, String[] names) {
		String v;
		for (String name: names) {
			v = ConfigUtil.getAsString(name, data, null);
			if (!StringUtil.isEmpty(v)) return ((ConfigPro) config).replacePlaceHolder(v);
		}
		return null;
	}

	public static String getAttr(Config config, Struct data, lucee.runtime.type.Collection.Key... names) {
		String v;
		for (lucee.runtime.type.Collection.Key name: names) {
			v = ConfigUtil.getAsString(name, data, null);
			if (!StringUtil.isEmpty(v)) return ((ConfigPro) config).replacePlaceHolder(v);
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
			LogUtil.logGlobal(ThreadLocalPageContext.getConfigServer(config), ConfigFactoryImpl.class.getName(), e);
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

	public static class MonitorTemp implements ActionMonitor {

		public final ActionMonitor am;
		public final String name;
		public final boolean log;

		public MonitorTemp(ActionMonitor am, String name, boolean log) {
			this.am = am;
			this.name = name;
			this.log = log;
		}

		@Override
		public void init(ConfigServer configServer, String name, boolean logEnabled) {
			am.init(configServer, name, logEnabled);
		}

		@Override
		public short getType() {
			return am.getType();
		}

		@Override
		public String getName() {
			return am.getName();
		}

		@Override
		public Class getClazz() {
			return am.getClazz();
		}

		@Override
		public boolean isLogEnabled() {
			return am.isLogEnabled();
		}

		@Override
		public void log(PageContext pc, String type, String label, long executionTime, Object data) throws IOException {
			am.log(pc, type, label, executionTime, data);
		}

		@Override
		public void log(ConfigWeb config, String type, String label, long executionTime, Object data) throws IOException {
			am.log(config, type, label, executionTime, data);
		}

		@Override
		public Query getData(Map<String, Object> arguments) throws PageException {
			return am.getData(arguments);
		}

	}

}