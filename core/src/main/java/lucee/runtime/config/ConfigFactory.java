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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lucee.commons.digest.MD5;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.XMLConfigReader.NameRule;
import lucee.runtime.config.XMLConfigReader.ReadRule;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public abstract class ConfigFactory {

	public static final int NEW_NONE = 0;
	public static final int NEW_MINOR = 1;
	public static final int NEW_FRESH = 2;
	public static final int NEW_FROM4 = 3;

	public static UpdateInfo getNew(CFMLEngine engine, Resource contextDir, final boolean readOnly, UpdateInfo defaultValue) {
		try {
			return getNew(engine, contextDir, readOnly);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static UpdateInfo getNew(CFMLEngine engine, Resource contextDir, final boolean readOnly) throws IOException, BundleException {
		lucee.Info info = engine.getInfo();

		String strOldVersion;
		final Resource resOldVersion = contextDir.getRealResource("version");

		String strNewVersion = info.getVersion() + "-" + info.getRealeaseTime();

		// if the config got deleted, we need to make sure the required extension get installed again
		boolean deleted = false;
		if (readOnly && !ConfigServerFactory.getConfigFile(contextDir, true).exists()) {
			deleted = resOldVersion.delete();
		}

		// new version
		if (deleted || !resOldVersion.exists()) {
			if (!readOnly) {
				resOldVersion.createNewFile();
				IOUtil.write(resOldVersion, strNewVersion, SystemUtil.getCharset(), false);
			}
			return UpdateInfo.NEW_FRESH;
		}
		// changed version
		else if (!(strOldVersion = IOUtil.toString(resOldVersion, SystemUtil.getCharset())).equals(strNewVersion)) {
			if (!readOnly) {
				IOUtil.write(resOldVersion, strNewVersion, SystemUtil.getCharset(), false);
			}
			Version oldVersion = OSGiUtil.toVersion(strOldVersion);

			return new UpdateInfo(oldVersion, oldVersion.getMajor() < 5 ? NEW_FROM4 : NEW_MINOR);
		}
		return UpdateInfo.NEW_NONE;
	}

	public static boolean modeChange(Resource configDir, final String mode, final boolean readOnly) throws IOException {
		String strOldVersion;
		final Resource resOldVersion = configDir.getRealResource("mode");
		// fresh install
		if (!resOldVersion.exists()) {
			if (!readOnly) {
				resOldVersion.createNewFile();
				IOUtil.write(resOldVersion, mode, SystemUtil.getCharset(), false);
			}
			return false;
		}
		// changed
		else if (!(strOldVersion = IOUtil.toString(resOldVersion, SystemUtil.getCharset())).equals(mode)) {
			if (!readOnly) {
				IOUtil.write(resOldVersion, mode, SystemUtil.getCharset(), false);
			}
			return true;
		}
		return false;
	}

	public static class UpdateInfo {

		public static final UpdateInfo NEW_NONE = new UpdateInfo(ConfigWebFactory.NEW_NONE);
		public static final UpdateInfo NEW_FRESH = new UpdateInfo(ConfigWebFactory.NEW_FRESH);

		public final Version oldVersion;
		public final int updateType;

		public UpdateInfo(int updateType) {
			this.oldVersion = null;
			this.updateType = updateType;
		}

		public UpdateInfo(Version oldVersion, int updateType) {
			this.oldVersion = oldVersion;
			this.updateType = updateType;
		}

		public String getUpdateTypeAsString() {
			if (updateType == ConfigWebFactory.NEW_NONE) return "new-none";
			if (updateType == ConfigWebFactory.NEW_FRESH) return "new-fresh";
			if (updateType == ConfigWebFactory.NEW_FROM4) return "new-from4";
			if (updateType == ConfigWebFactory.NEW_MINOR) return "new-minor";
			return "unkown:" + updateType;
		}

	}

	public static void updateRequiredExtension(CFMLEngine engine, Resource contextDir, Log log) {
		lucee.Info info = engine.getInfo();
		try {
			Resource res = contextDir.getRealResource("required-extension");
			String str = info.getVersion() + "-" + info.getRealeaseTime();
			if (!res.exists()) res.createNewFile();
			IOUtil.write(res, str, SystemUtil.getCharset(), false);

		}
		catch (Exception e) {
			if (log != null) log.error("required-extension", e);
		}
	}

	public static boolean isRequiredExtension(CFMLEngine engine, Resource contextDir, Log log) {
		lucee.Info info = engine.getInfo();
		try {
			Resource res = contextDir.getRealResource("required-extension");
			if (!res.exists()) return false;

			String writtenVersion = IOUtil.toString(res, SystemUtil.getCharset());
			String currVersion = info.getVersion() + "-" + info.getRealeaseTime();
			return writtenVersion.equals(currVersion);
		}
		catch (Exception e) {
			if (log != null) log.error("required-extension", e);
		}
		return false;
	}

	/**
	 * load XML Document from XML File
	 * 
	 * @param xmlFile XML File to read
	 * @return returns the Document
	 * @throws SAXException
	 * @throws IOException
	 * @throws PageException
	 * @throws NoSuchAlgorithmException
	 */
	static Struct loadDocument(Resource file) throws IOException, PageException {
		InputStream is = null;
		try {
			return _loadDocument(file);
		}
		finally {
			IOUtil.close(is);
		}
	}

	static Struct loadDocumentCreateIfFails(Resource configFile, String type) throws IOException, PageException {
		try {
			return _loadDocument(configFile);
		}
		catch (Exception e) {
			// rename buggy config files
			if (configFile.exists()) {
				Resource bugFile;
				int count = 1;
				Resource configDir = configFile.getParentResource();
				while ((bugFile = configDir.getRealResource("lucee-" + type + "." + (count++) + ".buggy")).exists()) {
				}

				LogUtil.log(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigFactory.class.getName(),
						"The configuration file [" + configFile
								+ "] contained syntax errors and could not be read. A new configuration file has been created, and the invalid file has been renamed to [" + bugFile
								+ "].");
				LogUtil.log(ThreadLocalPageContext.get(), ConfigFactory.class.getName(), e);

				IOUtil.copy(configFile, bugFile);
				configFile.delete();
			}
			createConfigFile(type, configFile);
			return loadDocument(configFile);
		}
	}

	public static Struct translateConfigFile(ConfigPro config, Object old, Resource configFileNew, String defaultMode, Boolean isServer)
			throws ConverterException, IOException, SAXException {
		// read the old config (XML)

		if (isServer == null) isServer = config instanceof ConfigServer;
		else {
			if (isServer && config instanceof ConfigWeb) {
				config = ((ConfigWebImpl) config).getConfigServerImpl();
			}
		}

		XMLConfigReader reader = null;
		if (old instanceof Resource) {
			reader = new XMLConfigReader((Resource) old, true, new ReadRule(), new NameRule());
		}
		else if (old instanceof InputSource) {
			reader = new XMLConfigReader((InputSource) old, true, new ReadRule(), new NameRule());
		}
		else {
			new ConverterException("inputing data is invalid, cannot cast [" + old.getClass().getName() + "] to a Resource or an InputSource");
		}
		Struct root = ConfigUtil.getAsStruct(reader.getData(), false, "cfLuceeConfiguration", "luceeConfiguration", "lucee-configuration");

		//////////////////// charset ////////////////////
		{
			Struct charset = ConfigUtil.getAsStruct("charset", root);
			Struct regional = ConfigUtil.getAsStruct("regional", root);
			Struct fileSystem = ConfigUtil.getAsStruct("fileSystem", root);
			copy("charset", "templateCharset", fileSystem, root);// deprecated but still supported
			copy("encoding", "templateCharset", fileSystem, root);// deprecated but still supported
			move("templateCharset", charset, root);

			move("charset", "webCharset", charset, root);// deprecated but still supported
			copy("encoding", "webCharset", fileSystem, root);// deprecated but still supported
			copy("defaultEncoding", "webCharset", regional, root);// deprecated but still supported
			move("webCharset", charset, root);

			copy("charset", "resourceCharset", fileSystem, root);// deprecated but still supported
			copy("encoding", "resourceCharset", fileSystem, root);// deprecated but still supported
			move("resourceCharset", charset, root);

			rem("charset", root);
		}
		//////////////////// regional ////////////////////
		{
			Struct regional = ConfigUtil.getAsStruct("regional", root);
			move("timezone", regional, root);
			move("locale", regional, root);
			rem("regional", root);
		}
		//////////////////// application ////////////////////
		{
			Struct application = ConfigUtil.getAsStruct("application", root);
			Struct scope = ConfigUtil.getAsStruct("scope", root);
			move("listenerType", application, root);
			move("listenerMode", application, root);
			move("typeChecking", application, root);
			move("cachedAfter", application, root);
			for (String type: ConfigPro.STRING_CACHE_TYPES) {
				move("cachedWithin" + StringUtil.ucFirst(type), application, root);
			}
			moveAsBool("allowUrlRequesttimeout", "requestTimeoutInURL", application, root);
			move("requesttimeout", "requestTimeout", scope, root);// deprecated but still supported
			move("requesttimeout", "requestTimeout", application, root);
			move("scriptProtect", application, root);
			move("classicDateParsing", application, root);
			move("cacheDirectory", application, root);
			move("cacheDirectoryMaxSize", application, root);
			move("adminSynchronisation", "adminSync", application, root);
			move("adminSync", application, root);

			rem("application", root);
		}

		//////////////////// caches ////////////////////
		{
			Struct cache = ConfigUtil.getAsStruct("cache", root);
			Struct caches = ConfigUtil.getAsStruct("caches", root);
			Array conns = ConfigUtil.getAsArray("connection", cache);

			// classes
			move("cache", "cacheClasses", caches, root);

			// defaults
			for (String type: ConfigPro.STRING_CACHE_TYPES_MAX) {
				move("default" + StringUtil.ucFirst(type), cache, root);
			}
			// connections
			Iterator<?> it = conns.getIterator();
			while (it.hasNext()) {
				Struct conn = Caster.toStruct(it.next(), null);
				if (conn == null) continue;
				add(conn, Caster.toString(conn.remove(KeyConstants._name, null), null), caches);
			}
			rem("cache", root);
		}

		//////////////////// cache handlers ////////////////////
		{
			Struct handlers = ConfigUtil.getAsStruct("cacheHandlers", root);
			Array handler = ConfigUtil.getAsArray("cacheHandler", handlers);

			Key[] keys = handler.keys();
			for (int i = keys.length - 1; i >= 0; i--) {
				Key k = keys[i];
				Struct data = Caster.toStruct(handler.get(k, null), null);
				if (data == null) continue;
				add(data, Caster.toString(data.remove(KeyConstants._id, null), null), handlers);
				handler.remove(k, null);
			}
		}

		//////////////////// CFX ////////////////////
		{
			Struct extTags = ConfigUtil.getAsStruct("extTags", root);
			Array extTag = ConfigUtil.getAsArray("extTag", extTags);
			Struct cfx = ConfigUtil.getAsStruct("cfx", root);

			Iterator<?> it = extTag.getIterator();
			while (it.hasNext()) {
				Struct conn = Caster.toStruct(it.next(), null);
				if (conn == null) continue;
				add(conn, Caster.toString(conn.remove(KeyConstants._name, null), null), cfx);
			}
			rem("extTags", root);
		}

		//////////////////// Compiler ////////////////////
		{
			Struct compiler = ConfigUtil.getAsStruct("compiler", root);
			moveAsBool("supressWsBeforeArg", "suppressWhitespaceBeforeArgument", compiler, root);// deprecated but still supported
			moveAsBool("suppressWsBeforeArg", "suppressWhitespaceBeforeArgument", compiler, root);
			moveAsBool("dotNotationUpperCase", "dotNotationUpperCase", compiler, root);
			moveAsBool("fullNullSupport", "nullSupport", compiler, root);
			move("defaultFunctionOutput", compiler, root);
			move("externalizeStringGte", compiler, root);
			moveAsBool("handleUnquotedAttributeValueAsString", "handleUnquotedAttributeValueAsString", compiler, root);
			rem("compiler", root);
		}

		//////////////////// Component ////////////////////
		{
			Struct component = ConfigUtil.getAsStruct("component", root);
			move("componentDefaultImport", "componentAutoImport", component, root);
			move("base", "componentBase", component, root);// deprecated but still supported
			move("baseCfml", "componentBase", component, root);
			move("baseLucee", "componentBaseLuceeDialect", component, root);
			rem("componentBase", root);
			rem("componentBaseLuceeDialect", root);
			moveAsBool("deepSearch", "componentDeepSearch", component, root);
			move("dumpTemplate", "componentDumpTemplate", component, root);
			move("dataMemberDefaultAccess", "componentDataMemberAccess", component, root);
			moveAsBool("triggerDataMember", "componentImplicitNotation", component, root);
			moveAsBool("localSearch", "componentLocalSearch", component, root);
			moveAsBool("useCachePath", "componentUseCachePath", component, root);
			moveAsBool("useShadow", "componentUseVariablesScope", component, root);

			// mappings
			Array ctMappings = ConfigUtil.getAsArray("mapping", component);
			add(ctMappings, "componentMappings", root);
			rem("mapping", component);
		}

		//////////////////// Custom tags ////////////////////
		{
			Struct ct = ConfigUtil.getAsStruct("customTag", root);
			moveAsBool("customTagUseCachePath", "customTagUseCachePath", ct, root);
			moveAsBool("useCachePath", "customTagUseCachePath", ct, root);
			moveAsBool("customTagLocalSearch", "customTagLocalSearch", ct, root);
			moveAsBool("localSearch", "customTagLocalSearch", ct, root);
			moveAsBool("deepSearch", "customTagDeepSearch", ct, root);
			moveAsBool("customTagDeepSearch", "customTagDeepSearch", ct, root);
			move("extensions", "customTagExtensions", ct, root);
			move("customTagExtensions", "customTagExtensions", ct, root);
			Array ctMappings = ConfigUtil.getAsArray("mapping", ct);
			add(ctMappings, "customTagMappings", root);
			rem("mapping", ct);
		}

		//////////////////// Constants ////////////////////
		{
			Struct constants = ConfigUtil.getAsStruct("constants", root);
			Array constant = ConfigUtil.getAsArray("constant", constants);
			rem("constant", constants);

			Key[] keys = constant.keys();
			for (int i = keys.length - 1; i >= 0; i--) {
				Key k = keys[i];
				Struct data = Caster.toStruct(constant.get(k, null), null);

				if (data == null) continue;
				constants.setEL(KeyImpl.init(Caster.toString(data.get(KeyConstants._name, null), null)), data.get(KeyConstants._value, null));
			}
		}

		//////////////////// JDBC ////////////////////
		{
			Struct jdbc = ConfigUtil.getAsStruct("jdbc", root);
			Array driver = ConfigUtil.getAsArray("driver", jdbc);
			Struct jdbcDrivers = ConfigUtil.getAsStruct("jdbcDrivers", root);

			Key[] keys = driver.keys();
			for (int i = keys.length - 1; i >= 0; i--) {
				Key k = keys[i];
				Struct data = Caster.toStruct(driver.get(k, null), null);
				if (data == null) continue;

				add(data, Caster.toString(data.remove(KeyConstants._class, null), null), jdbcDrivers);
				driver.remove(k, null);
			}
		}

		//////////////////// Datasource ////////////////////
		{
			Struct dataSources = ConfigUtil.getAsStruct("dataSources", root);
			// preserveSingleQuote
			Boolean b = Caster.toBoolean(dataSources.get("psq", null), null);
			if (b == null) {
				b = Caster.toBoolean(dataSources.get("preserveSingleQuote", null), null);
				if (b != null) b = b.booleanValue() ? Boolean.FALSE : Boolean.TRUE;
			}
			if (b != null) root.setEL("preserveSingleQuote", b.booleanValue());

			Array dataSource = ConfigUtil.getAsArray("dataSource", dataSources);

			Key[] keys = dataSource.keys();
			for (int i = keys.length - 1; i >= 0; i--) {
				Key k = keys[i];
				Struct data = Caster.toStruct(dataSource.get(k, null), null);
				if (data == null) continue;
				add(data, Caster.toString(data.remove(KeyConstants._name, null), null), dataSources);
				dataSource.remove(k, null);
			}
		}

		//////////////////// Debugging ////////////////////
		{
			Struct debugging = ConfigUtil.getAsStruct("debugging", root);
			moveAsBool("debug", "debuggingEnabled", debugging, root);
			moveAsBool("debugLogOutput", "debuggingLogOutput", debugging, root);
			moveAsBool("database", "debuggingDatabase", debugging, root);
			moveAsBool("exception", "debuggingException", debugging, root);
			moveAsBool("templenabled", "debuggingTemplate", debugging, root);
			moveAsBool("dump", "debuggingDump", debugging, root);
			moveAsBool("tracing", "debuggingTracing", debugging, root);
			moveAsBool("timer", "debuggingTimer", debugging, root);
			moveAsBool("implicitAccess", "debuggingImplicitAccess", debugging, root);
			moveAsBool("queryUsage", "debuggingQueryUsage", debugging, root);
			moveAsBool("showQueryUsage", "debuggingQueryUsage", debugging, root);
			moveAsBool("thread", "debuggingThread", debugging, root);
			moveAsInt("maxRecordsLogged", "debuggingMaxRecordsLogged", debugging, root);

			Array entries = ConfigUtil.getAsArray("debugEntry", debugging);
			add(entries, "debugTemplates", root);
			rem("debugEntry", debugging);
		}

		//////////////////// Dump Writer ////////////////////
		{
			Struct dumpWriters = ConfigUtil.getAsStruct("dumpWriters", root);
			Array dumpWriter = ConfigUtil.getAsArray("dumpWriter", dumpWriters);
			add(dumpWriter, "dumpWriters", root);
			rem("dumpWriter", dumpWriters);
		}

		//////////////////// Error ////////////////////
		{
			Struct error = ConfigUtil.getAsStruct("error", root);
			String tmpl = Caster.toString(error.get("template", null), null);
			String tmpl500 = Caster.toString(error.get("template500", null), null);
			String tmpl404 = Caster.toString(error.get("template404", null), null);

			// generalErrorTemplate
			if (!StringUtil.isEmpty(tmpl500)) root.setEL("errorGeneralTemplate", tmpl500);
			else if (!StringUtil.isEmpty(tmpl)) root.setEL("errorGeneralTemplate", tmpl);

			// missingErrorTemplate
			if (!StringUtil.isEmpty(tmpl404)) root.setEL("errorMissingTemplate", tmpl404);
			else if (!StringUtil.isEmpty(tmpl)) root.setEL("errorMissingTemplate", tmpl);

			moveAsBool("status", "errorStatusCode", error, root);
			moveAsBool("statusCode", "errorStatusCode", error, root);
		}

		//////////////////// Extensions ////////////////////
		{
			Struct extensions = ConfigUtil.getAsStruct("extensions", root);
			Array rhextension = ConfigUtil.getAsArray("rhextension", extensions);
			Array newExtensions = new ArrayImpl();
			rem("enabled", extensions);
			rem("extension", extensions);

			// extensions
			if (config != null) {
				Key[] keys = rhextension.keys();
				for (int i = keys.length - 1; i >= 0; i--) {
					Key k = keys[i];
					Struct data = Caster.toStruct(rhextension.get(k, null), null);
					if (data == null) continue;
					String id = Caster.toString(data.get(KeyConstants._id, null), null);
					String version = Caster.toString(data.get(KeyConstants._version, null), null);
					String name = Caster.toString(data.get(KeyConstants._name, null), null);
					RHExtension.storeMetaData(config, id, version, data);
					Struct sct = new StructImpl(Struct.TYPE_LINKED);
					sct.setEL(KeyConstants._id, id);
					sct.setEL(KeyConstants._version, version);
					if (name != null) sct.setEL(KeyConstants._name, name);
					// add(sct, Caster.toString(data.remove(KeyConstants._id, null), null), extensions);
					newExtensions.appendEL(sct);
					rhextension.remove(k, null);
				}
				root.setEL("extensions", newExtensions);
			}

			// providers
			Array rhprovider = ConfigUtil.getAsArray("rhprovider", extensions);
			Array extensionProviders = ConfigUtil.getAsArray("extensionProviders", root);
			Iterator<Object> it = rhprovider.valueIterator();
			while (it.hasNext()) {
				Struct data = Caster.toStruct(it.next(), null);
				if (data == null) continue;

				String url = Caster.toString(data.get(KeyConstants._url, null), null);
				if (!StringUtil.isEmpty(url)) extensionProviders.appendEL(url);
			}
			rem("rhprovider", extensions);
		}

		//////////////////// Gateway ////////////////////
		{
			Struct gateways = ConfigUtil.getAsStruct("gateways", root);
			Array gateway = ConfigUtil.getAsArray("gateway", gateways);

			Key[] keys = gateway.keys();
			for (int i = keys.length - 1; i >= 0; i--) {
				Key k = keys[i];
				Struct data = Caster.toStruct(gateway.get(k, null), null);
				if (data == null) continue;

				add(data, Caster.toString(data.remove(KeyConstants._id, null), null), gateways);
				gateway.remove(k, null);
			}
		}

		//////////////////// Java ////////////////////
		{
			Struct java = ConfigUtil.getAsStruct("java", root);
			move("inspectTemplate", java, root);
			move("compileType", java, root);
		}

		//////////////////// Loggers ////////////////////
		{
			Struct logging = ConfigUtil.getAsStruct("logging", root);
			Array logger = ConfigUtil.getAsArray("logger", logging);
			Struct loggers = ConfigUtil.getAsStruct("loggers", root);

			Key[] keys = logger.keys();
			for (int i = keys.length - 1; i >= 0; i--) {
				Key k = keys[i];
				Struct data = Caster.toStruct(logger.get(k, null), null);
				if (data == null) continue;
				add(data, Caster.toString(data.remove(KeyConstants._name, null), null), loggers);
				logger.remove(k, null);
			}
		}

		//////////////////// Login ////////////////////
		{
			Struct login = ConfigUtil.getAsStruct("login", root);
			moveAsBool("captcha", "loginCaptcha", login, root);
			moveAsBool("rememberme", "loginRememberme", login, root);
			moveAsInt("delay", "loginDelay", login, root);
		}

		//////////////////// Mail ////////////////////
		{
			Struct mail = ConfigUtil.getAsStruct("mail", root);
			moveAsBool("sendPartial", "mailSendPartial", mail, root);
			moveAsBool("userSet", "mailUserSet", mail, root);
			moveAsInt("spoolInterval", "mailSpoolInterval", mail, root);
			move("defaultEncoding", "mailDefaultEncoding", mail, root);
			moveAsBool("spoolEnable", "mailSpoolEnable", mail, root);
			moveAsInt("timeout", "mailConnectionTimeout", mail, root);

			Array server = ConfigUtil.getAsArray("server", mail);
			add(server, "mailServers", root);
			rem("mail", root);
		}
		// Array _mappings = ConfigWebUtil.getAsArray("mappings", "mapping", root);

		//////////////////// Mappings ////////////////////
		{
			Struct mappings = ConfigUtil.getAsStruct("mappings", root);
			Array mapping = ConfigUtil.getAsArray("mapping", mappings);

			Key[] keys = mapping.keys();
			for (int i = keys.length - 1; i >= 0; i--) {
				Key k = keys[i];
				Struct data = Caster.toStruct(mapping.get(k, null), null);
				if (data == null) continue;
				add(data, Caster.toString(data.remove(KeyConstants._virtual, null), null), mappings);
				mapping.remove(k, null);
			}
		}

		//////////////////// Monitor ////////////////////
		{
			Struct monitoring = ConfigUtil.getAsStruct("monitoring", root);
			Array monitor = ConfigUtil.getAsArray("monitor", monitoring);
			moveAsBool("enabled", "monitorEnable", monitoring, root);

			Struct monitors = ConfigUtil.getAsStruct("monitors", root);
			Key[] keys = monitor.keys();
			for (int i = keys.length - 1; i >= 0; i--) {
				Key k = keys[i];
				Struct data = Caster.toStruct(monitor.get(k, null), null);
				if (data == null) continue;
				add(data, Caster.toString(data.remove(KeyConstants._name, null), null), monitors);
				monitor.remove(k, null);
			}
		}

		//////////////////// queue ////////////////////
		{
			Struct queue = ConfigUtil.getAsStruct("queue", root);
			moveAsInt("enable", "requestQueueEnable", queue, root);
			moveAsInt("max", "requestQueueMax", queue, root);
			moveAsInt("timeout", "requestQueueTimeout", queue, root);
		}

		//////////////////// regex ////////////////////
		{
			Struct regex = ConfigUtil.getAsStruct("regex", root);
			move("type", "regexType", regex, root);
		}

		//////////////////// version ////////////////////
		{
			root.setEL(KeyConstants._version, "5.0");
		}

		//////////////////// scheduler ////////////////////
		if (config != null) {
			Resource configDir = config.getConfigDir();
			Struct scheduler = ConfigUtil.getAsStruct("scheduler", root);

			// set scheduler
			Resource schedulerDir = ConfigUtil.getFile(config.getRootDirectory(), ConfigWebFactory.getAttr(scheduler, "directory"), "scheduler", configDir, FileUtil.TYPE_DIR,
					ResourceUtil.LEVEL_GRAND_PARENT_FILE, config);
			Resource schedulerFile = schedulerDir.getRealResource("scheduler.xml");
			if (schedulerFile.isFile()) {
				Struct schedulerRoot = new XMLConfigReader(schedulerFile, true, new ReadRule(), new NameRule()).getData();
				Array task = ConfigUtil.getAsArray("schedule", "task", schedulerRoot);
				add(task, "scheduledTasks", root);
			}
			rem("scheduler", root);
		}

		//////////////////// Scope ////////////////////
		{
			Struct scope = ConfigUtil.getAsStruct("scope", root);
			move("localMode", "localScopeMode", scope, root);
			moveAsBool("cgiReadonly", "cgiScopeReadonly", scope, root);
			move("sessionType", "sessionType", scope, root);
			move("cascading", "scopeCascading", scope, root);
			moveAsBool("cascadeToResultset", "cascadeToResultset", scope, root);
			moveAsBool("mergeUrlForm", "mergeUrlForm", scope, root);
			move("clientStorage", "clientStorage", scope, root);
			move("sessionStorage", "sessionStorage", scope, root);
			move("clientTimeout", "clientTimeout", scope, root);
			move("sessionTimeout", "sessionTimeout", scope, root);
			move("applicationTimeout", "applicationTimeout", scope, root);
			move("clientType", "clientType", scope, root);
			move("clientDirectory", "clientDirectory", scope, root);
			move("clientDirectoryMaxSize", "clientDirectoryMaxSize", scope, root);
			moveAsBool("sessionManagement", "sessionManagement", scope, root);
			moveAsBool("setclientcookies", "clientCookies", scope, root);
			moveAsBool("setdomaincookies", "domainCookies", scope, root);
			moveAsBool("clientManagement", "clientManagement", scope, root);
			if (!root.containsKey("clientTimeout")) {
				int clientMaxAge = Caster.toIntValue(scope.get(KeyConstants._clientMaxAge, null), -1);
				if (clientMaxAge >= 0) root.setEL("clientTimeout", "0,0," + clientMaxAge + ",0");
			}
			scope.removeEL(KeyConstants._clientMaxAge);
			rem("scope", root);

		}

		//////////////////// Setting ////////////////////
		{
			Struct setting = ConfigUtil.getAsStruct("setting", root);
			moveAsBool("suppressContent", "suppressContent", setting, root);
			move("cfmlWriter", "cfmlWriter", setting, root);
			moveAsBool("showVersion", "showVersion", setting, root);
			moveAsBool("closeConnection", "closeConnection", setting, root);
			moveAsBool("contentLength", "showContentLength", setting, root);
			moveAsBool("bufferOutput", "bufferTagBodyOutput", setting, root);
			moveAsBool("bufferingOutput", "bufferTagBodyOutput", setting, root);
			moveAsBool("allowCompression", "allowCompression", setting, root);

			Struct _mode = ConfigUtil.getAsStruct("mode", root);
			moveAsBool("develop", "developMode", _mode, root);

			// now that mode is free we can use it for the admin mode
			Boolean b = Caster.toBoolean(setting.remove(KeyImpl.init("singlemode"), null), null);
			if (b != null) root.setEL(KeyConstants._mode, b.booleanValue() ? "single" : "multi");
			else if (!StringUtil.isEmpty(defaultMode)) root.setEL(KeyConstants._mode, defaultMode);
		}

		//////////////////// startup Hooks ////////////////////
		{
			Struct startup = ConfigUtil.getAsStruct("startup", root);
			Array hook = ConfigUtil.getAsArray("hook", startup);
			add(hook, "startupHooks", root);
			rem("startup", root);
		}

		//////////////////// System ////////////////////
		{
			Struct system = ConfigUtil.getAsStruct("system", root);
			move("out", "systemOut", system, root);
			move("err", "systemErr", system, root);
		}

		//////////////////// Tags ////////////////////
		{
			Struct tags = ConfigUtil.getAsStruct("tags", root);
			Array _default = ConfigUtil.getAsArray("default", tags);
			Array tag = ConfigUtil.getAsArray("tag", tags);

			add(_default, "tagDefaults", root);
			add(tag, "tags", root);
		}

		//////////////////// System ////////////////////
		{
			Struct fs = ConfigUtil.getAsStruct("fileSystem", root);
			move("tempDirectory", "tempDirectory", fs, root);
		}

		//////////////////// Update ////////////////////
		{
			Struct update = ConfigUtil.getAsStruct("update", root);
			move("location", "updateLocation", update, root);
			move("type", "updateType", update, root);
		}

		//////////////////// Resources ////////////////////
		{
			Struct resources = ConfigUtil.getAsStruct("resources", root);
			Array providers = ConfigUtil.getAsArray("resourceProvider", resources);

			// Ram -> Cache (Ram is no longer supported)
			Iterator<Object> it = providers.valueIterator();
			Struct data;
			boolean hasRam = false;
			while (it.hasNext()) {
				data = Caster.toStruct(it.next(), null);
				if (Caster.toString(data.get(KeyConstants._class, ""), "").equals("lucee.commons.io.res.type.ram.RamResourceProvider")) {
					hasRam = true;
					data.setEL(KeyConstants._class, "lucee.commons.io.res.type.cache.CacheResourceProvider");
				}
				if (Caster.toString(data.get(KeyConstants._class, ""), "").equals("lucee.commons.io.res.type.cache.CacheResourceProvider")) {
					hasRam = true;
				}
			}
			// we need the ram cache set in server, so we can go to single mode without harm
			if (isServer && !hasRam) {
				Struct sct = new StructImpl(Struct.TYPE_LINKED);
				sct.setEL("scheme", "ram");
				sct.setEL(KeyConstants._class, "lucee.commons.io.res.type.cache.CacheResourceProvider");
				sct.setEL(KeyConstants._arguments, "case-sensitive:true;lock-timeout:1000");
				providers.appendEL(sct);
			}

			Array defaultProviders = ConfigUtil.getAsArray("defaultResourceProvider", resources);

			add(providers, "resourceProviders", root);
			add(defaultProviders, "defaultResourceProvider", root);
			rem("resources", root);
		}

		// startupHooks
		remIfEmpty(root);

		// TODO scope?
		//////////////////// translate ////////////////////
		// allowLuceeDialect,cacheDirectory,cacheDirectoryMaxSize,componentAutoImport,componentBase,componentBaseLuceeDialect,componentDeepSearch
		// ,componentDumpTemplate, componentDataMemberDefaultAccess,componentUseVariablesScope,
		// componentLocalSearch,componentUseCachePath,componentMappings
		// classicDateParsing,cacheClasses,cacheHandlers,cfx,defaultFunctionOutput,externalizeStringGte,handleUnquotedAttributeValueAsString,
		// constants, customTagUseCachePath, customTagLocalSearch, customTagDeepSearch, customTagExtensions,
		// customTagMappings, debugTemplates,debuggingShowDump, debuggingImplicitAccess,
		// debuggingQueryUsage, debuggingMaxRecordsLogged
		// preserveSingleQuote,extensions,fileSystem, gateways,jdbcDrivers, loginCaptcha, loginRememberme,
		// loginDelay, mailSendPartial, mailUserSet, requestQueueEnable, requestQueueMax, regexType,
		// scheduledTasks<array>, localMode,
		// cgiReadonly->cgiScopeReadonly,cascadeToResultset,mergeUrlForm,clientType,clientDirectory,clientDirectoryMaxSize,
		// search,suppressContent,cfmlWriter,showVersion,showContentLength,allowCompression,startupHooks,systemErr,systemOut,tags,
		// tagDefaults,tempDirectory,updateLocation,updateType

		root = sort(root);

		if (configFileNew != null) {
			// store it as Json
			JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
			String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_ROW, true);
			IOUtil.write(configFileNew, str, CharsetUtil.UTF8, false);
		}
		return root;
	}

	private static Struct sort(Struct root) {
		Key[] keys = root.keys();
		Arrays.sort(keys);
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		Object val;
		// simple values first
		for (Key key: keys) {
			val = root.get(key, null);
			if (Decision.isSimpleValue(val)) sct.setEL(key, val);
		}
		// simple values first
		for (Key key: keys) {
			val = root.get(key, null);
			if (!Decision.isSimpleValue(val)) sct.setEL(key, val);
		}

		return sct;
	}

	private static void remIfEmpty(Collection coll) {
		Key[] keys = coll.keys();
		Object v;
		Collection sub;
		for (Key k: keys) {
			v = coll.get(k, null);
			if (v instanceof Collection) {
				sub = (Collection) v;
				if (sub.size() > 0) remIfEmpty(sub);
				if (sub.size() == 0) coll.remove(k, null);
			}
		}
	}

	private static void rem(String key, Struct sct) {
		sct.remove(KeyImpl.init(key), null);
	}

	private static void move(String key, Struct from, Struct to) {
		Key k = KeyImpl.init(key);
		Object val = from.remove(k, null);
		if (val != null) to.setEL(k, val);
	}

	private static void move(String fromKey, String toKey, Struct from, Struct to) {
		Object val = from.remove(KeyImpl.init(fromKey), null);
		if (val != null) to.setEL(KeyImpl.init(toKey), val);
	}

	private static void moveAsBool(String fromKey, String toKey, Struct from, Struct to) {
		Object val = from.remove(KeyImpl.init(fromKey), null);
		if (val != null && Decision.isCastableToBoolean(val)) to.setEL(KeyImpl.init(toKey), Caster.toBooleanValue(val, false));
	}

	private static void moveAsInt(String fromKey, String toKey, Struct from, Struct to) {
		Object val = from.remove(KeyImpl.init(fromKey), null);
		if (val != null && Decision.isCastableToNumeric(val)) to.setEL(KeyImpl.init(toKey), Caster.toIntValue(val, 0));
	}

	private static void add(Object fromData, String toKey, Struct to) {
		if (fromData == null) return;
		to.setEL(KeyImpl.init(toKey), fromData);
	}

	private static void copy(String fromKey, String toKey, Struct from, Struct to) {
		Object val = from.get(KeyImpl.init(fromKey), null);
		if (val != null) to.setEL(KeyImpl.init(toKey), val);
	}

	static String createVirtual(Struct data) {
		String str = ConfigWebFactory.getAttr(data, "virtual");
		if (!StringUtil.isEmpty(str)) return str;
		return createVirtual(ConfigWebFactory.getAttr(data, "physical"), ConfigWebFactory.getAttr(data, "archive"));
	}

	private static String createVirtual(String physical, String archive) {
		return "/" + MD5.getDigestAsString(physical + ":" + archive, "");
	}

	/**
	 * creates the Config File, if File not exist
	 * 
	 * @param xmlName
	 * @param configFile
	 * @throws IOException
	 */
	static void createConfigFile(String name, Resource configFile) throws IOException {
		createFileFromResource("/resource/config/" + name + ".json", configFile.getAbsoluteResource());
	}

	private static Struct _loadDocument(Resource res) throws IOException, PageException {
		String name = res.getName();
		// That step is not necessary anymore TODO remove
		if (StringUtil.endsWithIgnoreCase(name, ".xml.cfm") || StringUtil.endsWithIgnoreCase(name, ".xml")) {
			try {
				return ConfigUtil.getAsStruct(new XMLConfigReader(res, true, new ReadRule(), new NameRule()).getData(), false, "cfLuceeConfiguration", "luceeConfiguration",
						"lucee-configuration");
			}
			catch (SAXException e) {
				throw Caster.toPageException(e);
			}
		}
		try {
			return Caster.toStruct(new JSONExpressionInterpreter().interpret(null, IOUtil.toString(res, CharsetUtil.UTF8)));
			// data.set(KeyConstants._md5, Hash.md5(content));
		}
		catch (FileNotFoundException fnfe) {
			Resource dir = res.getParentResource();
			Resource ls = dir.getRealResource("lucee-server.xml");
			Resource lw = dir.getRealResource("lucee-web.xml.cfm");
			if (ls.isFile()) return _loadDocument(ls);
			else if (lw.isFile()) return _loadDocument(lw);
			else throw fnfe;
		}
		/*
		 * catch (NoSuchAlgorithmException e) { throw ExceptionUtil.toIOException(e); }
		 */
	}

	/**
	 * creates a File and his content from a resource, ignores if it is no able to create the resource,
	 * it logs if the file exists, but cannot be copy to the target, no logging when not exist as a
	 * source.
	 * 
	 * @param resource
	 * @param file
	 * @param password
	 * @throws IOException
	 */
	public static void createFileFromResourceEL(String resource, Resource file) {
		if (file.exists()) file.delete();

		InputStream is = InfoImpl.class.getResourceAsStream(resource);
		if (is == null) is = SystemUtil.getResourceAsStream(null, resource);
		if (is != null) {
			try {
				file.createNewFile();
				IOUtil.copy(is, file, true);
				LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigFactory.class.getName(), "Written file: [" + file + "]");
			}
			catch (Exception e) {
				LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigFactory.class.getName(), e);
			}
		}
	}

	/**
	 * creates a File and his content from a resource
	 * 
	 * @param resource
	 * @param file
	 * @param password
	 * @throws IOException
	 */
	static void createFileFromResource(String resource, Resource file, String password) throws IOException {
		if (file.exists()) file.delete();

		InputStream is = InfoImpl.class.getResourceAsStream(resource);
		if (is == null) is = SystemUtil.getResourceAsStream(null, resource);
		if (is == null) throw new IOException("File [" + resource + "] does not exist.");
		file.createNewFile();
		IOUtil.copy(is, file, true);
		LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigFactory.class.getName(), "Written file: [" + file + "]");

	}

	/**
	 * creates a File and his content froma a resurce
	 * 
	 * @param resource
	 * @param file
	 * @throws IOException
	 */
	static void createFileFromResource(String resource, Resource file) throws IOException {
		createFileFromResource(resource, file, null);
	}

	static void create(String srcPath, String[] names, Resource dir, boolean doNew) {
		for (int i = 0; i < names.length; i++) {
			create(srcPath, names[i], dir, doNew);
		}
	}

	static Resource create(String srcPath, String name, Resource dir, boolean doNew) {
		boolean update = doNew;
		// directory does not exist (so file do also not exist)
		if (!dir.exists()) {
			update = true;
			dir.mkdirs();
		}

		Resource f = dir.getRealResource(name);
		if (update || f.length() == 0L) {
			ConfigFactory.createFileFromResourceEL(srcPath + name, f);
		}
		return f;
	}

	static void delete(Resource dbDir, String[] names) {
		for (int i = 0; i < names.length; i++) {
			delete(dbDir, names[i]);
		}
	}

	static void delete(Resource dbDir, String name) {
		Resource f = dbDir.getRealResource(name);
		if (f.exists()) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigFactory.class.getName(), "Delete file: [" + f + "]");

			f.delete();
		}

	}

}
