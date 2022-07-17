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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.w3c.dom.DOMException;

import com.allaire.cfx.CustomTag;

import lucee.commons.digest.MD5;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.compress.ZipUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.util.FileWrapper;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.IPRange;
import lucee.commons.net.URLEncoder;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.security.Credentials;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.util.ExtensionFilter;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cfx.CFXTagException;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.converter.WDDXConverter;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.ParamSyntax;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.HTTPException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.extension.Extension;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.functions.other.CreateObject;
import lucee.runtime.functions.other.URLEncodedFormat;
import lucee.runtime.functions.string.Hash;
import lucee.runtime.functions.system.IsZipFile;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.gateway.GatewayEntryImpl;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.monitor.Monitor;
import lucee.runtime.net.ntp.NtpClient;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMConfigurationImpl;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.osgi.BundleBuilderFactory;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.schedule.ScheduleTask;
import lucee.runtime.schedule.ScheduleTaskImpl;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.security.SerialNumber;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.video.VideoExecuter;
import lucee.runtime.video.VideoExecuterNotSupported;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.tag.TagLibException;

/**
 * 
 */
public final class ConfigAdmin {

	private static final BundleInfo[] EMPTY = new BundleInfo[0];
	private ConfigPro config;
	private final Struct root;
	private Password password;

	/**
	 * 
	 * @param config
	 * @param password
	 * @return returns a new instance of the class
	 * @throws SAXException
	 * @throws IOException
	 * @throws PageException
	 */
	public static ConfigAdmin newInstance(Config config, Password password) throws IOException, PageException {
		return new ConfigAdmin((ConfigPro) config, password);
	}

	private void checkWriteAccess() throws SecurityException {
		ConfigWebUtil.checkGeneralWriteAccess(config, password);
	}

	private void checkReadAccess() throws SecurityException {
		ConfigWebUtil.checkGeneralReadAccess(config, password);
	}

	/**
	 * @param password
	 * @throws IOException
	 * @throws DOMException
	 * @throws ExpressionException
	 */
	public void setPassword(Password password) throws SecurityException, IOException {
		checkWriteAccess();
		PasswordImpl.writeToStruct(root, password, false);
	}

	/*
	 * public void setVersion(double version) { setVersion(doc,version);
	 * 
	 * }
	 */

	public static void setVersion(Struct root, Version version) {
		root.setEL("version", version.getMajor() + "." + version.getMinor());

	}
	/*
	 * public void setId(String id) {
	 * 
	 * Element root=doc.getDocumentElement(); if(!StringUtil.isEmpty(root.get("id"))) return;
	 * root.setEL("id",id); try { store(config); } catch (Exception e) {} }
	 */

	/**
	 * @param contextPath
	 * @param password
	 * @throws FunctionLibException
	 * @throws TagLibException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SAXException
	 * @throws PageException
	 * @throws BundleException
	 */
	public void removePassword(String contextPath) throws PageException, ClassException, IOException, TagLibException, FunctionLibException, BundleException {
		checkWriteAccess();
		if (contextPath == null || contextPath.length() == 0 || !(config instanceof ConfigServerImpl)) {
			// config.setPassword(password); do nothing!
		}
		else {
			ConfigServerImpl cs = (ConfigServerImpl) config;
			ConfigWebImpl cw = (ConfigWebImpl) cs.getConfigWeb(contextPath);
			if (cw != null) cw.updatePassword(false, cw.getPassword(), null);
		}
	}

	private ConfigAdmin(ConfigPro config, Password password) throws IOException, PageException {
		this.config = config;
		this.password = password;
		root = ConfigWebFactory.loadDocument(config.getConfigFile());
	}

	public static void checkForChangesInConfigFile(Config config) {
		ConfigPro ci = (ConfigPro) config;
		if (!ci.checkForChangesInConfigFile()) return;

		Resource file = config.getConfigFile();
		long diff = file.lastModified() - ci.lastModified();
		if (diff < 10 && diff > -10) return;
		// reload
		try {
			ConfigAdmin admin = ConfigAdmin.newInstance(ci, null);
			admin._reload();
			LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigAdmin.class.getName(), "reloaded the configuration [" + file + "] automatically");
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	private void addResourceProvider(String scheme, ClassDefinition cd, String arguments) throws PageException {
		checkWriteAccess();

		Array rpElements = ConfigWebUtil.getAsArray("resourceProviders", root);
		// Element[] rpElements = ConfigWebFactory.getChildren(resources, "resource-provider");
		String s;
		// update
		if (rpElements != null) {
			Struct rpElement;
			for (int i = 1; i <= rpElements.size(); i++) {
				rpElement = Caster.toStruct(rpElements.getE(i));
				s = Caster.toString(rpElement.get("scheme"));
				if (!StringUtil.isEmpty(s) && s.equalsIgnoreCase(scheme)) {
					setClass(rpElement, null, "", cd);
					rpElement.setEL("scheme", scheme);
					rpElement.setEL("arguments", arguments);
					return;
				}
			}
		}
		// Insert
		Struct el = new StructImpl(Struct.TYPE_LINKED);
		setClass(el, null, "", cd);
		el.setEL("scheme", scheme);
		el.setEL("arguments", arguments);
		rpElements.appendEL(el);
	}

	public static synchronized void _storeAndReload(ConfigPro config)
			throws PageException, ClassException, IOException, TagLibException, FunctionLibException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._store();
		admin._reload();
	}

	private synchronized void _storeAndReload() throws PageException, ClassException, IOException, TagLibException, FunctionLibException, BundleException, ConverterException {
		_store();
		_reload();
	}

	public synchronized void storeAndReload() throws PageException, ClassException, IOException, TagLibException, FunctionLibException, BundleException, ConverterException {
		checkWriteAccess();
		_store();
		_reload();
	}

	private synchronized void _store() throws PageException, ConverterException, IOException {
		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, true, true);
		String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_ROW);
		IOUtil.write(config.getConfigFile(), str, CharsetUtil.UTF8, false);
	}

	private synchronized void _reload() throws PageException, ClassException, IOException, TagLibException, FunctionLibException, BundleException {

		// if(storeInMemoryData)XMLCaster.writeTo(doc,config.getConfigFile());
		CFMLEngine engine = ConfigWebUtil.getEngine(config);
		if (config instanceof ConfigServerImpl) {

			ConfigServerImpl cs = (ConfigServerImpl) config;
			ConfigServerFactory.reloadInstance(engine, cs);
			ConfigWeb[] webs = cs.getConfigWebs();
			for (ConfigWeb web: webs) {
				if (web instanceof ConfigWebImpl) ConfigWebFactory.reloadInstance(engine, (ConfigServerImpl) config, (ConfigWebImpl) web, true);
				else if (web instanceof SingleContextConfigWeb) ((SingleContextConfigWeb) web).reload();
			}
		}
		else if (config instanceof ConfigWebImpl) {
			ConfigServerImpl cs = ((ConfigWebImpl) config).getConfigServerImpl();
			ConfigWebFactory.reloadInstance(engine, cs, (ConfigWebImpl) config, false);
		}
		else if (config instanceof SingleContextConfigWeb) {
			SingleContextConfigWeb sccw = (SingleContextConfigWeb) config;

			ConfigServerImpl cs = sccw.getConfigServerImpl();
			ConfigServerFactory.reloadInstance(engine, cs);
			sccw.reload();
			/*
			 * ConfigWeb[] webs = cs.getConfigWebs(); for (int i = 0; i < webs.length; i++) { if (webs[i]
			 * instanceof ConfigWebImpl) ConfigWebFactory.reloadInstance(engine, (ConfigServerImpl) config,
			 * (ConfigWebImpl) webs[i], true); }
			 */
		}
	}

	/*
	 * private void createAbort() { try {
	 * ConfigWebFactory.getChildByName(doc.getDocumentElement(),"cfabort",true); } catch(Throwable t)
	 * {ExceptionUtil.rethrowIfNecessary(t);} }
	 */

	public void setTaskMaxThreads(Integer maxThreads) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update task settings");
		Struct mail = _getRootElement("remoteClients");
		mail.setEL("maxThreads", Caster.toString(maxThreads, ""));
	}

	/**
	 * sets Mail Logger to Config
	 * 
	 * @param logFile
	 * @param level
	 * @throws PageException
	 */
	public void setMailLog(Config config, String logFile, String level) throws PageException {
		ConfigPro ci = (ConfigPro) config;
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);

		if (!hasAccess) throw new SecurityException("no access to update mail server settings");
		ConfigWebUtil.getFile(config, config.getRootDirectory(), logFile, FileUtil.TYPE_FILE);

		Struct loggers = ConfigWebUtil.getAsStruct("loggers", root);
		Struct logger = Caster.toStruct(loggers.get(KeyConstants._mail, null), null);

		if (logger == null) {
			logger = new StructImpl(Struct.TYPE_LINKED);
			loggers.setEL(KeyConstants._mail, logger);
		}
		if ("console".equalsIgnoreCase(logFile)) {
			setClass(logger, null, "appender", ci.getLogEngine().appenderClassDefintion("console"));
			setClass(logger, null, "layout", ci.getLogEngine().layoutClassDefintion("pattern"));
		}
		else {
			setClass(logger, null, "appender", ci.getLogEngine().appenderClassDefintion("resource"));
			setClass(logger, null, "layout", ci.getLogEngine().layoutClassDefintion("classic"));
			logger.setEL("appenderArguments", "path:" + logFile);
		}
		logger.setEL("logLevel", level);
	}

	/**
	 * sets if spool is enable or not
	 * 
	 * @param spoolEnable
	 * @throws SecurityException
	 */
	public void setMailSpoolEnable(Boolean spoolEnable) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);

		if (!hasAccess) throw new SecurityException("no access to update mail server settings");
		root.setEL("mailSpoolEnable", Caster.toString(spoolEnable, ""));
	}

	/**
	 * sets the timeout for the spooler for one job
	 * 
	 * @param timeout
	 * @throws SecurityException
	 */
	public void setMailTimeout(Integer timeout) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);
		if (!hasAccess) throw new SecurityException("no access to update mail server settings");
		root.setEL("mailConnectionTimeout", Caster.toString(timeout, ""));
	}

	/**
	 * sets the charset for the mail
	 * 
	 * @param charset
	 * @throws SecurityException
	 */
	public void setMailDefaultCharset(String charset) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);
		if (!hasAccess) throw new SecurityException("no access to update mail server settings");

		if (!StringUtil.isEmpty(charset)) {
			try {
				IOUtil.checkEncoding(charset);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}

		root.setEL("mailDefaultEncoding", charset);
	}

	/**
	 * insert or update a mailserver on system
	 * 
	 * @param hostName
	 * @param username
	 * @param password
	 * @param port
	 * @param ssl
	 * @param tls
	 * @throws PageException
	 */
	public void updateMailServer(int id, String hostName, String username, String password, int port, boolean tls, boolean ssl, long lifeTimeSpan, long idleTimeSpan,
			boolean reuseConnections) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL);
		if (!hasAccess) throw new SecurityException("no access to update mail server settings");

		if (port < 1) port = 21;

		if (hostName == null || hostName.trim().length() == 0) throw new ExpressionException("Host (SMTP) cannot be an empty value");
		hostName = hostName.trim();

		Array children = ConfigWebUtil.getAsArray("mailServers", root);

		boolean checkId = id > 0;

		// Update
		Struct server = null;
		String _hostName, _username;
		for (int i = 1; i <= children.size(); i++) {
			Struct el = Caster.toStruct(children.get(i, null), null);
			if (el == null) continue;

			if (checkId) {
				if (i == id) {
					server = el;
					break;
				}
			}
			else {
				_hostName = StringUtil.emptyIfNull(Caster.toString(el.get("smtp", null)));
				_username = StringUtil.emptyIfNull(Caster.toString(el.get("username", null)));
				if (_hostName.equalsIgnoreCase(hostName) && _username.equals(StringUtil.emptyIfNull(username))) {
					server = el;
					break;
				}
			}
		}

		// Insert
		if (server == null) {
			server = new StructImpl(Struct.TYPE_LINKED);
			children.appendEL(server);
		}
		server.setEL("smtp", hostName);
		server.setEL(KeyConstants._username, username);
		server.setEL(KeyConstants._password, ConfigWebUtil.encrypt(password));
		server.setEL(KeyConstants._port, (port));
		server.setEL("tls", (tls));
		server.setEL("ssl", (ssl));
		server.setEL("life", (lifeTimeSpan));
		server.setEL("idle", (idleTimeSpan));
		server.setEL("reuseConnection", (reuseConnections));
	}

	/**
	 * removes a mailserver from system
	 * 
	 * @param hostName
	 * @throws SecurityException
	 */
	public void removeMailServer(String hostName, String username) throws SecurityException {
		checkWriteAccess();
		Array children = ConfigWebUtil.getAsArray("mailServers", root);
		Key[] keys = children.keys();
		String _hostName, _username;
		if (children.size() > 0) {
			for (int i = keys.length - 1; i >= 0; i--) {
				Key key = keys[i];
				Struct el = Caster.toStruct(children.get(key, null), null);
				if (el == null) continue;
				_hostName = Caster.toString(el.get("smtp", null), null);
				_username = Caster.toString(el.get("username", null), null);
				if (StringUtil.emptyIfNull(_hostName).equalsIgnoreCase(StringUtil.emptyIfNull(hostName))
						&& StringUtil.emptyIfNull(_username).equalsIgnoreCase(StringUtil.emptyIfNull(username))) {
					children.removeEL(key);
				}
			}
		}
	}

	public void removeLogSetting(String name) throws SecurityException {
		checkWriteAccess();
		Struct children = ConfigWebUtil.getAsStruct("loggers", root);
		if (children.size() > 0) {
			String _name;
			Key[] keys = children.keys();
			for (Key key: keys) {
				_name = key.getString();
				if (_name != null && _name.equalsIgnoreCase(name)) {
					children.removeEL(key);
				}
			}
		}
	}

	static void updateMapping(ConfigPro config, String virtual, String physical, String archive, String primary, short inspect, boolean toplevel, int listenerMode,
			int listenerType, boolean readonly, boolean reload) throws IOException, PageException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._updateMapping(virtual, physical, archive, primary, inspect, toplevel, listenerMode, listenerType, readonly);
		admin._store();
		if (reload) admin._reload();
	}

	static void updateComponentMapping(ConfigPro config, String virtual, String physical, String archive, String primary, short inspect, boolean reload)
			throws IOException, PageException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._updateComponentMapping(virtual, physical, archive, primary, inspect);
		admin._store();
		if (reload) admin._reload();
	}

	static void updateCustomTagMapping(ConfigPro config, String virtual, String physical, String archive, String primary, short inspect, boolean reload)
			throws IOException, PageException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._updateCustomTag(virtual, physical, archive, primary, inspect);
		admin._store();
		if (reload) admin._reload();
	}

	public static Array updateScheduledTask(ConfigPro config, ScheduleTask task, boolean reload) throws IOException, PageException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._updateScheduledTask(task);
		admin._store();
		if (reload) admin._reload();
		return admin._getScheduledTasks();
	}

	private void _updateScheduledTask(ScheduleTask task) throws ExpressionException, SecurityException {
		Struct data = _getScheduledTask(task.getTask(), false);

		data.setEL(KeyConstants._name, task.getTask());
		if (task.getResource() != null) data.setEL(KeyConstants._file, task.getResource().getAbsolutePath());
		else if (data.containsKey(KeyConstants._file)) data.removeEL(KeyConstants._file);

		if (task.getStartDate() != null) data.setEL("startDate", task.getStartDate().castToString(null));
		if (task.getStartTime() != null) data.setEL("startTime", task.getStartTime().castToString(null));
		if (task.getEndDate() != null) data.setEL("endDate", task.getEndDate().castToString(null));
		else if (data.containsKey("endDate")) rem(data, "endDate");
		if (task.getEndTime() != null) data.setEL("endTime", task.getEndTime().castToString(null));
		else if (data.containsKey("endTime")) rem(data, "endTime");

		data.setEL(KeyConstants._url, task.getUrl().toExternalForm());
		data.setEL(KeyConstants._port, task.getUrl().getPort());
		data.setEL(KeyConstants._interval, task.getIntervalAsString());
		data.setEL("timeout", (int) task.getTimeout());
		Credentials c = task.getCredentials();
		if (c != null) {
			if (c.getUsername() != null) data.setEL("username", c.getUsername());
			if (c.getPassword() != null) data.setEL("password", c.getPassword());
		}
		else {
			if (data.containsKey("username")) rem(data, "username");
			if (data.containsKey("password")) rem(data, "password");
		}
		ProxyData pd = task.getProxyData();
		if (pd != null) {
			if (!StringUtil.isEmpty(pd.getServer(), true)) data.setEL("proxyHost", pd.getServer());
			else if (data.containsKey("proxyHost")) rem(data, "proxyHost");
			if (!StringUtil.isEmpty(pd.getUsername(), true)) data.setEL("proxyUser", pd.getUsername());
			else if (data.containsKey("proxyUser")) rem(data, "proxyUser");
			if (!StringUtil.isEmpty(pd.getPassword(), true)) data.setEL("proxyPassword", pd.getPassword());
			else if (data.containsKey("proxyPassword")) rem(data, "proxyPassword");
			if (pd.getPort() > 0) data.setEL("proxyPort", pd.getPort());
			else if (data.containsKey("proxyPort")) rem(data, "proxyPort");
		}
		else {
			if (data.containsKey("proxyHost")) rem(data, "proxyHost");
			if (data.containsKey("proxyUser")) rem(data, "proxyUser");
			if (data.containsKey("proxyPassword")) rem(data, "proxyPassword");
			if (data.containsKey("proxyPort")) rem(data, "proxyPort");
		}
		data.setEL("resolveUrl", task.isResolveURL());
		data.setEL("publish", task.isPublish());
		data.setEL("hidden", ((ScheduleTaskImpl) task).isHidden());
		data.setEL("readonly", ((ScheduleTaskImpl) task).isReadonly());
		data.setEL("autoDelete", ((ScheduleTaskImpl) task).isAutoDelete());
		data.setEL("unique", ((ScheduleTaskImpl) task).unique());
		if (((ScheduleTaskImpl) task).getUserAgent() != null) data.setEL("userAgent", ((ScheduleTaskImpl) task).getUserAgent());
		else if (data.containsKey("userAgent")) rem(data, "userAgent");
	}

	public static void pauseScheduledTask(ConfigPro config, String name, boolean pause, boolean throwWhenNotExist, boolean reload)
			throws PageException, IOException, ConverterException, BundleException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		Struct data = null;
		try {
			data = admin._getScheduledTask(name, true);
		}
		catch (ExpressionException ee) {
			if (throwWhenNotExist) throw ee;
			return;
		}
		data.setEL("paused", pause);

		admin._store();
		if (reload) admin._reload();
	}

	public static void removeScheduledTask(ConfigPro config, String name, boolean reload) throws PageException, IOException, ConverterException, BundleException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._removeScheduledTask(name);
		admin._store();
		if (reload) admin._reload();
	}

	/**
	 * insert or update a mapping on system
	 * 
	 * @param virtual
	 * @param physical
	 * @param archive
	 * @param primary
	 * @param trusted
	 * @param toplevel
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void updateMapping(String virtual, String physical, String archive, String primary, short inspect, boolean toplevel, int listenerMode, int listenerType,
			boolean readOnly) throws ExpressionException, SecurityException {
		checkWriteAccess();
		_updateMapping(virtual, physical, archive, primary, inspect, toplevel, listenerMode, listenerType, readOnly);
	}

	private void _updateMapping(String virtual, String physical, String archive, String primary, short inspect, boolean toplevel, int listenerMode, int listenerType,
			boolean readOnly) throws ExpressionException, SecurityException {

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAPPING);
		virtual = virtual.trim();
		if (physical == null) physical = "";
		else physical = physical.trim();
		if (archive == null) archive = "";
		else archive = archive.trim();
		primary = primary.trim();
		if (!hasAccess) throw new SecurityException("no access to update mappings");

		// check virtual
		if (virtual == null || virtual.length() == 0) throw new ExpressionException("virtual path cannot be an empty value");
		virtual = virtual.replace('\\', '/');

		if (!virtual.equals("/") && virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1);

		if (virtual.charAt(0) != '/') throw new ExpressionException("virtual path must start with [/]");
		boolean isArchive = primary.equalsIgnoreCase("archive");

		if ((physical.length() + archive.length()) == 0) throw new ExpressionException("physical or archive must have a value");

		if (isArchive && archive.length() == 0) isArchive = false;

		if (!isArchive && archive.length() > 0 && physical.length() == 0) isArchive = true;

		Struct children = ConfigWebUtil.getAsStruct("mappings", root);
		Key[] keys = children.keys();
		// Element mappings = _getRootElement("mappings");
		// Element[] children = ConfigWebFactory.getChildren(mappings, "mapping");

		Struct el = null;
		for (Key key: keys) {
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			String v = key.getString();
			if (!StringUtil.isEmpty(v)) {
				if (!v.equals("/") && v.endsWith("/")) v = v.substring(0, v.length() - 1);

				if (v.equals(virtual)) {
					el = tmp;
					el.remove("trusted");
					break;
				}
			}
		}

		// create element if necessary
		boolean update = el != null;
		if (el == null) {
			el = new StructImpl(Struct.TYPE_LINKED);
			children.setEL(virtual, el);
		}

		// physical
		if (physical.length() > 0) {
			el.setEL("physical", physical);
		}
		else if (el.containsKey("physical")) {
			el.remove("physical");
		}

		// archive
		if (archive.length() > 0) {
			el.setEL("archive", archive);
		}
		else if (el.containsKey("archive")) {
			el.remove("archive");
		}

		// primary
		el.setEL("primary", isArchive ? "archive" : "physical");

		// listener-type
		String type = ConfigWebUtil.toListenerType(listenerType, null);
		if (type != null) {
			el.setEL("listenerType", type);
		}
		else if (el.containsKey("listenerType")) {
			el.remove("listenerType");
		}

		// listener-mode
		String mode = ConfigWebUtil.toListenerMode(listenerMode, null);
		if (mode != null) {
			el.setEL("listenerMode", mode);
		}
		else if (el.containsKey("listenerMode")) {
			el.remove("listenerMode");
		}

		// others
		el.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""));
		el.setEL("topLevel", Caster.toString(toplevel));
		el.setEL("readOnly", Caster.toString(readOnly));

		// set / to the end
		if (!update) {
			children = ConfigWebUtil.getAsStruct("mappings", root);
			keys = children.keys();
			for (Key key: keys) {
				Struct tmp = Caster.toStruct(children.get(key, null), null);
				if (tmp == null) continue;

				String v = key.getString();

				if (v != null && v.equals("/")) {
					children.removeEL(key);
					children.setEL(v, tmp);
					return;
				}

			}
		}

	}

	public void updateRestMapping(String virtual, String physical, boolean _default) throws ExpressionException, SecurityException {
		checkWriteAccess();
		boolean hasAccess = true;// TODO ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST);
		virtual = virtual.trim();
		physical = physical.trim();
		if (!hasAccess) throw new SecurityException("no access to update REST mapping");

		// check virtual
		if (virtual == null || virtual.length() == 0) throw new ExpressionException("virtual path cannot be an empty value");
		virtual = virtual.replace('\\', '/');
		if (virtual.equals("/")) throw new ExpressionException("virtual path cannot be /");

		if (virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1);

		if (virtual.charAt(0) != '/') virtual = "/" + virtual;

		if ((physical.length()) == 0) throw new ExpressionException("physical path cannot be an empty value");

		Struct rest = _getRootElement("rest");
		Array children = ConfigWebUtil.getAsArray("mapping", rest);

		// remove existing default
		if (_default) {
			for (int i = 1; i <= children.size(); i++) {
				Struct tmp = Caster.toStruct(children.get(i, null), null);
				if (tmp == null) continue;

				if (Caster.toBooleanValue(tmp.get("default", null), false)) tmp.setEL("default", "false");
			}
		}

		// Update
		String v;
		Struct el = null;
		for (int i = 1; i <= children.size(); i++) {
			Struct tmp = Caster.toStruct(children.get(i, null), null);
			if (tmp == null) continue;

			v = ConfigWebUtil.getAsString("virtual", tmp, null);
			if (v != null && v.equals(virtual)) {
				el = tmp;
			}
		}

		// Insert
		if (el == null) {
			el = new StructImpl(Struct.TYPE_LINKED);
			children.appendEL(el);
		}

		el.setEL("virtual", virtual);
		el.setEL("physical", physical);
		el.setEL("default", Caster.toString(_default));
	}

	/**
	 * delete a mapping on system
	 * 
	 * @param virtual
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void removeMapping(String virtual) throws ExpressionException, SecurityException {
		checkWriteAccess();
		_removeMapping(virtual);
	}

	public void _removeMapping(String virtual) throws ExpressionException {
		// check parameters
		if (virtual == null || virtual.length() == 0) throw new ExpressionException("virtual path cannot be an empty value");
		virtual = virtual.replace('\\', '/');

		if (!virtual.equals("/") && virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1);
		if (virtual.charAt(0) != '/') throw new ExpressionException("virtual path must start with [/]");

		Struct children = ConfigWebUtil.getAsStruct("mappings", root);
		Key[] keys = children.keys();
		for (Key key: keys) {
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			String v = key.getString();
			if (v != null) {
				if (!v.equals("/") && v.endsWith("/")) v = v.substring(0, v.length() - 1);
				if (v != null && v.equals(virtual)) {
					children.removeEL(key);
				}
			}
		}
	}

	public void removeRestMapping(String virtual) throws ExpressionException, SecurityException {
		checkWriteAccess();
		// check parameters
		if (virtual == null || virtual.length() == 0) throw new ExpressionException("virtual path cannot be an empty value");
		virtual = virtual.replace('\\', '/');
		if (virtual.equals("/")) throw new ExpressionException("virtual path cannot be /");

		if (virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1);
		if (virtual.charAt(0) != '/') virtual = "/" + virtual;

		Array children = ConfigWebUtil.getAsArray("rest", "mapping", root);
		Key[] keys = children.keys();
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			String v = ConfigWebUtil.getAsString("virtual", tmp, null);
			if (v != null) {
				if (!v.equals("/") && v.endsWith("/")) v = v.substring(0, v.length() - 1);
				if (v != null && v.equals(virtual)) {
					children.removeEL(key);
				}
			}
		}
	}

	/**
	 * delete a customtagmapping on system
	 * 
	 * @param virtual
	 * @throws SecurityException
	 */
	public void removeCustomTag(String virtual) throws SecurityException {
		checkWriteAccess();

		Array mappings = ConfigWebUtil.getAsArray("customTagMappings", root);
		Key[] keys = mappings.keys();
		Struct data;
		String v;
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			data = Caster.toStruct(mappings.get(key, null), null);
			if (data == null) continue;
			v = createVirtual(data);

			if (virtual.equals(v)) {
				mappings.removeEL(key);
			}
		}
	}

	private void _removeScheduledTask(String name) throws SecurityException, ExpressionException {
		Array tasks = ConfigWebUtil.getAsArray("scheduledTasks", root);
		Key[] keys = tasks.keys();
		Struct data;
		String n;
		Boolean exist = false;
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			data = Caster.toStruct(tasks.get(key, null), null);
			if (data == null) continue;
			n = Caster.toString(data.get(KeyConstants._name, null), null);

			if (name.equals(n)) {
				exist = true;
				tasks.removeEL(key);
			}
		}
		if (!exist) throw new ExpressionException("can't delete schedule task [ " + name + " ], task doesn't exist");
	}

	public void removeComponentMapping(String virtual) throws SecurityException {
		checkWriteAccess();

		Array mappings = ConfigWebUtil.getAsArray("componentMappings", root);
		Key[] keys = mappings.keys();
		Struct data;
		String v;
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			data = Caster.toStruct(mappings.get(key, null), null);
			if (data == null) continue;
			v = createVirtual(data);

			if (virtual.equals(v)) {
				mappings.removeEL(key);
			}
		}
	}

	/**
	 * insert or update a mapping for Custom Tag
	 * 
	 * @param virtual
	 * @param physical
	 * @param archive
	 * @param primary
	 * @param trusted
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void updateCustomTag(String virtual, String physical, String archive, String primary, short inspect) throws ExpressionException, SecurityException {
		checkWriteAccess();
		_updateCustomTag(virtual, physical, archive, primary, inspect);
	}

	private void _updateCustomTag(String virtual, String physical, String archive, String primary, short inspect) throws ExpressionException, SecurityException {
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG);
		if (!hasAccess) throw new SecurityException("no access to change custom tag settings");
		if (physical == null) physical = "";
		if (archive == null) archive = "";

		// virtual="/custom-tag";
		if (StringUtil.isEmpty(virtual)) virtual = createVirtual(physical, archive);

		boolean isArchive = primary.equalsIgnoreCase("archive");
		if (isArchive && archive.length() == 0) {
			throw new ExpressionException("archive must have a value when primary has value archive");
		}
		if (!isArchive && physical.length() == 0) {
			throw new ExpressionException("physical must have a value when primary has value physical");
		}

		Array mappings = ConfigWebUtil.getAsArray("customTagMappings", root);
		Key[] keys = mappings.keys();
		// Update
		String v;
		// Element[] children = ConfigWebFactory.getChildren(mappings, "mapping");
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			Struct el = Caster.toStruct(mappings.get(key, null), null);
			if (el == null) continue;
			v = createVirtual(el);
			if (virtual.equals(v)) {
				el.setEL("virtual", v);
				el.setEL("physical", physical);
				el.setEL("archive", archive);
				el.setEL("primary", primary.equalsIgnoreCase("archive") ? "archive" : "physical");
				el.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""));
				el.removeEL(KeyImpl.init("trusted"));
				return;
			}
		}

		// Insert
		Struct el = new StructImpl(Struct.TYPE_LINKED);
		mappings.appendEL(el);
		if (physical.length() > 0) el.setEL("physical", physical);
		if (archive.length() > 0) el.setEL("archive", archive);
		el.setEL("primary", primary.equalsIgnoreCase("archive") ? "archive" : "physical");
		el.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""));
		el.setEL("virtual", StringUtil.isEmpty(virtual) ? createVirtual(el) : virtual);
	}

	private Struct _getScheduledTask(String name, boolean throwWhenNotExist) throws ExpressionException {
		Array scheduledTasks = ConfigWebUtil.getAsArray("scheduledTasks", root);
		Key[] keys = scheduledTasks.keys();
		// Update
		Struct data = null;
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			Struct tmp = Caster.toStruct(scheduledTasks.get(key, null), null);
			if (tmp == null) continue;

			String n = Caster.toString(tmp.get(KeyConstants._name, null), null);
			if (name.equalsIgnoreCase(n)) {
				data = tmp;
				break;
			}
		}

		// Insert
		if (data == null) {
			if (throwWhenNotExist) throw new ExpressionException("scheduled task [" + name + "] does not exist!");
			data = new StructImpl(Struct.TYPE_LINKED);
			scheduledTasks.appendEL(data);
		}
		return data;
	}

	private Array _getScheduledTasks() {
		return ConfigWebUtil.getAsArray("scheduledTasks", root);
	}

	public void updateComponentMapping(String virtual, String physical, String archive, String primary, short inspect) throws ExpressionException, SecurityException {
		checkWriteAccess();
		_updateComponentMapping(virtual, physical, archive, primary, inspect);
	}

	private void _updateComponentMapping(String virtual, String physical, String archive, String primary, short inspect) throws ExpressionException {
		primary = primary.equalsIgnoreCase("archive") ? "archive" : "physical";
		if (physical == null) physical = "";
		else physical = physical.trim();

		if (archive == null) archive = "";
		else archive = archive.trim();

		boolean isArchive = primary.equalsIgnoreCase("archive");
		if (isArchive && archive.length() == 0) {
			throw new ExpressionException("archive must have a value when primary has value archive");
		}
		if (!isArchive && physical.length() == 0) {
			throw new ExpressionException("physical must have a value when primary has value physical");
		}

		Array componentMappings = ConfigWebUtil.getAsArray("componentMappings", root);
		Key[] keys = componentMappings.keys();
		Struct el;

		// Update
		String v;
		Struct data;
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			data = Caster.toStruct(componentMappings.get(key, null), null);
			if (data == null) continue;

			v = createVirtual(data);

			if (virtual.equals(v)) {
				data.setEL("virtual", v);
				data.setEL("physical", physical);
				data.setEL("archive", archive);
				data.setEL("primary", primary.equalsIgnoreCase("archive") ? "archive" : "physical");
				data.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""));
				data.removeEL(KeyImpl.init("trusted"));
				return;
			}
		}

		// Insert
		el = new StructImpl(Struct.TYPE_LINKED);
		componentMappings.appendEL(el);
		if (physical.length() > 0) el.setEL("physical", physical);
		if (archive.length() > 0) el.setEL("archive", archive);
		el.setEL("primary", primary.equalsIgnoreCase("archive") ? "archive" : "physical");
		el.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""));
		el.setEL("virtual", StringUtil.isEmpty(virtual) ? createVirtual(el) : virtual);
	}

	public static String createVirtual(Struct data) {
		String str = ConfigWebFactory.getAttr(data, "virtual");
		if (!StringUtil.isEmpty(str)) return str;
		return createVirtual(ConfigWebFactory.getAttr(data, "physical"), ConfigWebFactory.getAttr(data, "archive"));
	}

	public static String createVirtual(String physical, String archive) {
		return "/" + MD5.getDigestAsString(physical + ":" + archive, "");
	}

	public void updateJar(Resource resJar) throws IOException, BundleException {
		updateJar(config, resJar, true);
	}

	public static void updateJar(Config config, Resource resJar, boolean reloadWhenClassicJar) throws IOException, BundleException {
		BundleFile bf = BundleFile.getInstance(resJar);

		// resJar is a bundle
		if (bf.isBundle()) {
			bf = installBundle(config, bf);
			OSGiUtil.loadBundle(bf);
			return;
		}

		Resource lib = ((ConfigPro) config).getLibraryDirectory();
		if (!lib.exists()) lib.mkdir();
		Resource fileLib = lib.getRealResource(resJar.getName());

		// if there is an existing, has the file changed?
		if (fileLib.length() != resJar.length()) {
			IOUtil.closeEL(config.getClassLoader());
			ResourceUtil.copy(resJar, fileLib);
			if (reloadWhenClassicJar) ConfigWebUtil.reloadLib(config);
		}
	}

	/*
	 * important! returns null when not a bundle!
	 */
	static BundleFile installBundle(Config config, Resource resJar, String extVersion, boolean convert2bundle) throws IOException, BundleException {

		BundleFile bf = BundleFile.getInstance(resJar);

		// resJar is a bundle
		if (bf.isBundle()) {
			return installBundle(config, bf);
		}

		if (!convert2bundle) return null;

		// name
		String name = bf.getSymbolicName();
		if (StringUtil.isEmpty(name)) name = BundleBuilderFactory.createSymbolicName(resJar);

		// version
		Version version = bf.getVersion();
		if (version == null) version = OSGiUtil.toVersion(extVersion);

		LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigAdmin.class.getName(), "failed to load [" + resJar + "] as OSGi Bundle");
		BundleBuilderFactory bbf = new BundleBuilderFactory(resJar, name);
		bbf.setVersion(version);
		bbf.setIgnoreExistingManifest(false);
		bbf.build();

		bf = BundleFile.getInstance(resJar);
		LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigAdmin.class.getName(), "converted  [" + resJar + "] to an OSGi Bundle");
		return installBundle(config, bf);
	}

	private static BundleFile installBundle(Config config, BundleFile bf) throws IOException, BundleException {

		// does this bundle already exists
		BundleFile _bf = OSGiUtil.getBundleFile(bf.getSymbolicName(), bf.getVersion(), null, null, false, null);
		if (_bf != null) return _bf;

		CFMLEngine engine = CFMLEngineFactory.getInstance();
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();

		// copy to jar directory
		File jar = new File(factory.getBundleDirectory(), bf.getSymbolicName() + "-" + bf.getVersion().toString() + (".jar"));
		InputStream is = bf.getInputStream();
		OutputStream os = new FileOutputStream(jar);
		try {
			IOUtil.copy(is, os, false, false);
		}
		finally {
			IOUtil.close(is, os);
		}
		return BundleFile.getInstance(jar);
	}

	/**
	 * 
	 * @param config
	 * @param is
	 * @param name
	 * @param extensionVersion if given jar is no bundle the extension version is used for the bundle
	 *            created
	 * @param closeStream
	 * @return
	 * @throws IOException
	 * @throws BundleException
	 */
	static Bundle updateBundle(Config config, InputStream is, String name, String extensionVersion, boolean closeStream) throws IOException, BundleException {
		Object obj = installBundle(config, is, name, extensionVersion, closeStream, false);
		if (!(obj instanceof BundleFile)) throw new BundleException("input is not an OSGi Bundle.");

		BundleFile bf = (BundleFile) obj;
		return OSGiUtil.loadBundle(bf);
	}

	/**
	 * @param config
	 * @param is
	 * @param name
	 * @param extensionVersion
	 * @param closeStream
	 * @param convert2bundle
	 * @return return the Bundle File or the file in case it is not a bundle.
	 * @throws IOException
	 * @throws BundleException
	 */
	public static Object installBundle(Config config, InputStream is, String name, String extensionVersion, boolean closeStream, boolean convert2bundle)
			throws IOException, BundleException {
		Resource tmp = SystemUtil.getTempDirectory().getRealResource(name);
		OutputStream os = tmp.getOutputStream();
		IOUtil.copy(is, os, closeStream, true);

		BundleFile bf = installBundle(config, tmp, extensionVersion, convert2bundle);
		if (bf != null) {
			tmp.delete();
			return bf;
		}
		return tmp;
	}

	static void updateJar(Config config, InputStream is, String name, boolean closeStream) throws IOException, BundleException {
		Resource tmp = SystemUtil.getTempDirectory().getRealResource(name);
		try {
			IOUtil.copy(is, tmp, closeStream);
			updateJar(config, tmp, true);
		}
		finally {
			tmp.delete();
		}
	}

	/**
	 * insert or update a Java CFX Tag
	 * 
	 * @param name
	 * @param strClass
	 * @throws PageException
	 */
	public void updateJavaCFX(String name, ClassDefinition cd) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CFX_SETTING);

		if (!hasAccess) throw new SecurityException("no access to change cfx settings");

		if (name == null || name.length() == 0) throw new ExpressionException("class name can't be an empty value");

		Struct cfxs = ConfigWebUtil.getAsStruct("cfx", root);
		Key[] keys = cfxs.keys();
		// Update
		for (Key key: keys) {
			String n = key.getString();

			if (n != null && n.equalsIgnoreCase(name)) {
				Struct data = Caster.toStruct(cfxs.get(key, null), null);
				if (data == null) continue;
				if (!"java".equalsIgnoreCase(ConfigWebUtil.getAsString("type", data, ""))) throw new ExpressionException("there is already a c++ cfx tag with this name");
				setClass(data, CustomTag.class, "", cd);
				data.setEL("type", "java");
				return;
			}
		}

		// Insert
		Struct el = new StructImpl(Struct.TYPE_LINKED);
		cfxs.setEL(name, el);
		setClass(el, CustomTag.class, "", cd);
		el.setEL("type", "java");
	}

	public void verifyCFX(String name) throws PageException {
		CFXTagPool pool = config.getCFXTagPool();
		CustomTag ct = null;
		try {
			ct = pool.getCustomTag(name);
		}
		catch (CFXTagException e) {
			throw Caster.toPageException(e);
		}
		finally {
			if (ct != null) pool.releaseCustomTag(ct);
		}

	}

	public void verifyJavaCFX(String name, ClassDefinition cd) throws PageException {
		try {
			Class clazz = cd.getClazz();
			if (!Reflector.isInstaneOf(clazz, CustomTag.class, false))
				throw new ExpressionException("class [" + cd + "] must implement interface [" + CustomTag.class.getName() + "]");
		}
		catch (ClassException e) {
			throw Caster.toPageException(e);
		}
		catch (BundleException e) {
			throw Caster.toPageException(e);
		}

		if (StringUtil.startsWithIgnoreCase(name, "cfx_")) name = name.substring(4);
		if (StringUtil.isEmpty(name)) throw new ExpressionException("class name can't be an empty value");
	}

	/**
	 * remove a CFX Tag
	 * 
	 * @param name
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void removeCFX(String name) throws ExpressionException, SecurityException {
		checkWriteAccess();
		// check parameters
		if (name == null || name.length() == 0) throw new ExpressionException("name for CFX Tag can be an empty value");

		Struct cfxs = ConfigWebUtil.getAsStruct("cfx", root);
		Key[] keys = cfxs.keys();
		for (Key key: keys) {
			String n = key.getString();
			if (n != null && n.equalsIgnoreCase(name)) {
				cfxs.removeEL(key);
			}
		}
	}

	/**
	 * update or insert new database connection
	 * 
	 * @param name
	 * @param clazzName
	 * @param dsn
	 * @param username
	 * @param password
	 * @param host
	 * @param database
	 * @param port
	 * @param connectionLimit
	 * @param connectionTimeout
	 * @param blob
	 * @param clob
	 * @param allow
	 * @param storage
	 * @param custom
	 * @throws PageException
	 */
	public void updateDataSource(String id, String name, String newName, ClassDefinition cd, String dsn, String username, String password, String host, String database, int port,
			int connectionLimit, int idleTimeout, int liveTimeout, long metaCacheTimeout, boolean blob, boolean clob, int allow, boolean validate, boolean storage, String timezone,
			Struct custom, String dbdriver, ParamSyntax paramSyntax, boolean literalTimestampWithTSOffset, boolean alwaysSetTimeout, boolean requestExclusive,
			boolean alwaysResetConnections) throws PageException {

		checkWriteAccess();
		SecurityManager sm = config.getSecurityManager();
		short access = sm.getAccess(SecurityManager.TYPE_DATASOURCE);
		boolean hasAccess = true;
		boolean hasInsertAccess = true;
		int maxLength = 0;

		if (access == SecurityManager.VALUE_YES) hasAccess = true;
		else if (access == SecurityManager.VALUE_NO) hasAccess = false;
		else if (access >= SecurityManager.VALUE_1 && access <= SecurityManager.VALUE_10) {
			int existingLength = getDatasourceLength(config);
			maxLength = access - SecurityManager.NUMBER_OFFSET;
			hasInsertAccess = maxLength > existingLength;
		}
		if (!hasAccess) throw new SecurityException("no access to update datasource connections");

		// check parameters
		if (name == null || name.length() == 0) throw new ExpressionException("name can't be an empty value");

		Struct children = ConfigWebUtil.getAsStruct("dataSources", root);
		Key[] keys = children.keys();
		for (Key key: keys) {

			if (key.getString().equalsIgnoreCase(name)) {
				Struct tmp = Caster.toStruct(children.get(key, null), null);
				if (tmp == null) continue;
				Struct el = tmp;
				if (password.equalsIgnoreCase("****************")) password = ConfigWebUtil.getAsString("password", el, null);

				if (!StringUtil.isEmpty(newName) && !newName.equals(name)) el.setEL("name", newName);
				setClass(el, null, "", cd);

				if (!StringUtil.isEmpty(id)) el.setEL(KeyConstants._id, id);
				else if (el.containsKey(KeyConstants._id)) el.removeEL(KeyConstants._id);

				el.setEL("dsn", dsn);
				el.setEL("username", username);
				el.setEL("password", ConfigWebUtil.encrypt(password));

				el.setEL("host", host);
				if (!StringUtil.isEmpty(timezone)) el.setEL(KeyConstants._timezone, timezone);
				else if (el.containsKey(KeyConstants._timezone)) el.removeEL(KeyConstants._timezone);
				el.setEL("database", database);
				el.setEL("port", Caster.toString(port));
				el.setEL("connectionLimit", Caster.toString(connectionLimit));
				el.setEL("connectionTimeout", Caster.toString(idleTimeout));
				el.setEL("liveTimeout", Caster.toString(liveTimeout));
				el.setEL("metaCacheTimeout", Caster.toString(metaCacheTimeout));
				el.setEL("blob", Caster.toString(blob));
				el.setEL("clob", Caster.toString(clob));
				el.setEL("allow", Caster.toString(allow));
				el.setEL("validate", Caster.toString(validate));
				el.setEL("storage", Caster.toString(storage));
				el.setEL("custom", toStringURLStyle(custom));

				if (!StringUtil.isEmpty(dbdriver)) el.setEL("dbdriver", Caster.toString(dbdriver));

				// Param Syntax
				el.setEL("paramDelimiter", (paramSyntax.delimiter));
				el.setEL("paramLeadingDelimiter", (paramSyntax.leadingDelimiter));
				el.setEL("paramSeparator", (paramSyntax.separator));

				if (literalTimestampWithTSOffset) el.setEL("literalTimestampWithTSOffset", "true");
				else if (el.containsKey("literalTimestampWithTSOffset")) el.removeEL(KeyImpl.init("literalTimestampWithTSOffset"));

				if (alwaysSetTimeout) el.setEL("alwaysSetTimeout", "true");
				else if (el.containsKey("alwaysSetTimeout")) el.removeEL(KeyImpl.init("alwaysSetTimeout"));

				if (requestExclusive) el.setEL("requestExclusive", "true");
				else if (el.containsKey("requestExclusive")) el.removeEL(KeyImpl.init("requestExclusive"));

				if (alwaysResetConnections) el.setEL("alwaysResetConnections", "true");
				else if (el.containsKey("alwaysResetConnections")) el.removeEL(KeyImpl.init("alwaysResetConnections"));

				return;
			}
		}

		if (!hasInsertAccess) throw new SecurityException("Unable to add a datasource connection, the maximum count of [" + maxLength + "] datasources has been reached. "
				+ " This can be configured in the Server Admin, under Security, Access");

		// Insert
		Struct el = new StructImpl(Struct.TYPE_LINKED);
		children.setEL(!StringUtil.isEmpty(newName) ? newName : name, el);
		setClass(el, null, "", cd);
		el.setEL("dsn", dsn);

		if (!StringUtil.isEmpty(id)) el.setEL(KeyConstants._id, id);
		else if (el.containsKey(KeyConstants._id)) el.removeEL(KeyConstants._id);

		if (username.length() > 0) el.setEL(KeyConstants._username, username);
		el.setEL(KeyConstants._password, ConfigWebUtil.encrypt(password));

		el.setEL("host", host);
		if (!StringUtil.isEmpty(timezone)) el.setEL("timezone", timezone);
		el.setEL("database", database);
		if (port > -1) el.setEL("port", Caster.toString(port));
		el.setEL("connectionLimit", Caster.toString(connectionLimit));
		if (idleTimeout > -1) el.setEL("connectionTimeout", Caster.toString(idleTimeout));
		if (liveTimeout > -1) el.setEL("liveTimeout", Caster.toString(liveTimeout));
		if (metaCacheTimeout > -1) el.setEL("metaCacheTimeout", Caster.toString(metaCacheTimeout));

		el.setEL("blob", Caster.toString(blob));
		el.setEL("clob", Caster.toString(clob));
		el.setEL("validate", Caster.toString(validate));
		el.setEL("storage", Caster.toString(storage));
		if (allow > -1) el.setEL("allow", Caster.toString(allow));
		el.setEL("custom", toStringURLStyle(custom));

		if (!StringUtil.isEmpty(dbdriver)) el.setEL("dbdriver", Caster.toString(dbdriver));

		// Param Syntax
		el.setEL("paramDelimiter", (paramSyntax.delimiter));
		el.setEL("paramLeadingDelimiter", (paramSyntax.leadingDelimiter));
		el.setEL("paramSeparator", (paramSyntax.separator));

		if (literalTimestampWithTSOffset) el.setEL("literalTimestampWithTSOffset", "true");
		if (alwaysSetTimeout) el.setEL("alwaysSetTimeout", "true");
		if (requestExclusive) el.setEL("requestExclusive", "true");
		if (alwaysResetConnections) el.setEL("alwaysResetConnections", "true");

	}

	static void removeJDBCDriver(ConfigPro config, ClassDefinition cd, boolean reload) throws IOException, PageException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._removeJDBCDriver(cd);
		admin._store(); // store is necessary, otherwise it get lost

		if (reload) admin._reload();
	}

	private void _removeJDBCDriver(ClassDefinition cd) throws PageException {

		if (!cd.isBundle()) throw new ApplicationException("missing bundle name");

		Struct children = ConfigWebUtil.getAsStruct("jdbcDrivers", root);
		Key[] keys = children.keys();
		// Remove
		for (Key key: keys) {
			if (key.getString().equalsIgnoreCase(cd.getClassName())) {
				children.removeEL(key);
				break;
			}
		}

		// now unload (maybe not necessary)
		if (cd.isBundle()) {
			Bundle bl = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null);
			if (bl != null) {
				try {
					OSGiUtil.uninstall(bl);
				}
				catch (BundleException e) {
				}
			}
		}
	}

	private void _removeStartupHook(ClassDefinition cd) throws PageException {

		if (!cd.isBundle()) throw new ApplicationException("missing bundle name");

		Array children = ConfigWebUtil.getAsArray("startupHooks", root);
		Key[] keys = children.keys();
		// Remove
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			String n = ConfigWebUtil.getAsString("class", tmp, "");
			if (n.equalsIgnoreCase(cd.getClassName())) {
				children.removeEL(key);
				break;
			}
		}

		// now unload (maybe not necessary)
		if (cd.isBundle()) {
			unloadStartupIfNecessary(config, cd, true);
			Bundle bl = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null);
			if (bl != null) {
				try {
					OSGiUtil.uninstall(bl);
				}
				catch (BundleException e) {
				}
			}
		}
	}

	private void unloadStartupIfNecessary(ConfigPro config, ClassDefinition<?> cd, boolean force) {
		ConfigBase.Startup startup = config.getStartups().get(cd.getClassName());
		if (startup == null) return;
		if (startup.cd.equals(cd) && !force) return;

		try {
			Method fin = Reflector.getMethod(startup.instance.getClass(), "finalize", new Class[0], null);
			if (fin != null) {
				fin.invoke(startup.instance, new Object[0]);
			}
			config.getStartups().remove(cd.getClassName());
		}
		catch (Exception e) {
		}
	}

	public void updateJDBCDriver(String label, String id, ClassDefinition cd) throws PageException {
		checkWriteAccess();
		_updateJDBCDriver(label, id, cd);
	}

	private void _updateJDBCDriver(String label, String id, ClassDefinition cd) throws PageException {

		// check if label exists
		if (StringUtil.isEmpty(label)) throw new ApplicationException("missing label for jdbc driver [" + cd.getClassName() + "]");
		// check if it is a bundle
		if (!cd.isBundle()) throw new ApplicationException("missing bundle name for [" + label + "]");

		Struct children = ConfigWebUtil.getAsStruct("jdbcDrivers", root);
		Key[] keys = children.keys();
		// Update
		Struct child = null;
		for (Key key: keys) {
			String n = key.getString();
			if (key.getString().equalsIgnoreCase(cd.getClassName())) {
				Struct tmp = Caster.toStruct(children.get(key, null), null);
				if (tmp == null) continue;
				child = tmp;
				break;
			}
		}

		// Insert
		if (child == null) {
			child = new StructImpl(Struct.TYPE_LINKED);
			children.setEL(cd.getClassName(), child);
		}

		child.setEL("label", label);
		if (!StringUtil.isEmpty(id)) child.setEL(KeyConstants._id, id);
		else child.removeEL(KeyConstants._id);
		// make sure the class exists
		setClass(child, null, "", cd);
		child.removeEL(KeyConstants._class);

		// now unload again, JDBC driver can be loaded when necessary
		if (cd.isBundle()) {
			Bundle bl = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null);
			if (bl != null) {
				try {
					OSGiUtil.uninstall(bl);
				}
				catch (BundleException e) {
				}
			}
		}
	}

	private void _updateStartupHook(ClassDefinition cd) throws PageException {
		unloadStartupIfNecessary(config, cd, false);
		// check if it is a bundle
		if (!cd.isBundle()) throw new ApplicationException("missing bundle info");

		Array children = ConfigWebUtil.getAsArray("startupHooks", root);

		// Update
		Struct child = null;
		for (int i = 1; i <= children.size(); i++) {
			Struct tmp = Caster.toStruct(children.get(i, null), null);
			if (tmp == null) continue;

			String n = ConfigWebUtil.getAsString("class", tmp, null);
			if (n.equalsIgnoreCase(cd.getClassName())) {
				child = tmp;
				break;
			}
		}

		// Insert
		if (child == null) {
			child = new StructImpl(Struct.TYPE_LINKED);
			children.appendEL(child);
		}

		// make sure the class exists
		setClass(child, null, "", cd);

		// now unload again, JDBC driver can be loaded when necessary
		if (cd.isBundle()) {
			Bundle bl = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null);
			if (bl != null) {
				try {
					OSGiUtil.uninstall(bl);
				}
				catch (BundleException e) {
				}
			}
		}
	}

	public void updateGatewayEntry(String id, ClassDefinition cd, String componentPath, String listenerCfcPath, int startupMode, Struct custom, boolean readOnly)
			throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY);
		if (!hasAccess) throw new SecurityException("no access to update gateway entry");

		_updateGatewayEntry(id, cd, componentPath, listenerCfcPath, startupMode, custom, readOnly);
	}

	void _updateGatewayEntry(String id, ClassDefinition cd, String componentPath, String listenerCfcPath, int startupMode, Struct custom, boolean readOnly) throws PageException {

		// check parameters
		id = id.trim();
		if (StringUtil.isEmpty(id)) throw new ExpressionException("id can't be an empty value");

		if ((cd == null || StringUtil.isEmpty(cd.getClassName())) && StringUtil.isEmpty(componentPath)) throw new ExpressionException("you must define className or componentPath");

		Struct children = ConfigWebUtil.getAsStruct("gateways", root);
		Key[] keys = children.keys();

		// Update
		for (Key key: keys) {
			Struct el = Caster.toStruct(children.get(key, null), null);
			if (el == null) continue;

			String n = key.getString();
			if (n.equalsIgnoreCase(id)) {
				setClass(el, null, "", cd);
				el.setEL("cfcPath", componentPath);
				el.setEL("listenerCFCPath", listenerCfcPath);
				el.setEL("startupMode", GatewayEntryImpl.toStartup(startupMode, "automatic"));
				el.setEL("custom", toStringURLStyle(custom));
				el.setEL("readOnly", Caster.toString(readOnly));
				return;
			}
		}

		// Insert
		Struct el = new StructImpl(Struct.TYPE_LINKED);
		children.setEL(id, el);
		el.setEL("cfcPath", componentPath);
		el.setEL("listenerCFCPath", listenerCfcPath);
		el.setEL("startupMode", GatewayEntryImpl.toStartup(startupMode, "automatic"));
		setClass(el, null, "", cd);
		el.setEL("custom", toStringURLStyle(custom));
		el.setEL("readOnly", Caster.toString(readOnly));
	}

	static void removeSearchEngine(ConfigPro config, boolean reload) throws IOException, PageException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._removeSearchEngine();
		admin._store();
		if (reload) admin._reload();
	}

	private void _removeSearchEngine() {
		Struct orm = _getRootElement("search");
		removeClass(orm, "engine");
	}

	public void updateSearchEngine(ClassDefinition cd) throws PageException {
		checkWriteAccess();
		_updateSearchEngine(cd);

	}

	private void _updateSearchEngine(ClassDefinition cd) throws PageException {
		Struct orm = _getRootElement("search");
		setClass(orm, SearchEngine.class, "engine", cd);
	}

	public void removeSearchEngine() throws SecurityException {
		checkWriteAccess();

		Struct orm = _getRootElement("search");
		removeClass(orm, "engine");

	}

	static void removeORMEngine(ConfigPro config, boolean reload) throws IOException, PageException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._removeORMEngine();
		admin._store();
		if (reload) admin._reload();
	}

	private void _removeORMEngine() {
		Struct orm = _getRootElement("orm");
		removeClass(orm, "engine");
		removeClass(orm, "");// in the beginning we had no prefix
	}

	private void _removeWebserviceHandler() {
		Struct orm = _getRootElement("webservice");
		removeClass(orm, "");
	}

	public void removeORMEngine() throws SecurityException {
		checkWriteAccess();
		_removeORMEngine();
	}

	public void updateORMEngine(ClassDefinition cd) throws PageException {
		checkWriteAccess();
		_updateORMEngine(cd);

	}

	private void _updateORMEngine(ClassDefinition cd) throws PageException {
		Struct orm = _getRootElement("orm");
		removeClass(orm, "");// in the beginning we had no prefix
		setClass(orm, ORMEngine.class, "engine", cd);
	}

	private void _updateWebserviceHandler(ClassDefinition cd) throws PageException {
		Struct orm = _getRootElement("webservice");
		setClass(orm, null, "", cd);
	}

	public void updateCacheConnection(String name, ClassDefinition cd, int _default, Struct custom, boolean readOnly, boolean storage) throws PageException {

		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE);
		if (!hasAccess) throw new SecurityException("no access to update cache connection");

		// check parameters
		name = name.trim();
		if (StringUtil.isEmpty(name)) throw new ExpressionException("name can't be an empty value");
		// else if(name.equals("template") || name.equals("object"))
		// throw new ExpressionException("name ["+name+"] is not allowed for a cache connection, the
		// following names are reserved words [object,template]");

		try {
			Class clazz;
			if (cd.getClassName() != null && cd.getClassName().endsWith(".EHCacheLite"))
				clazz = ClassUtil.loadClass(config.getClassLoader(), "org.lucee.extension.cache.eh.EHCache");
			else clazz = ClassUtil.loadClass(config.getClassLoader(), cd.getClassName());

			if (!Reflector.isInstaneOf(clazz, Cache.class, false)) throw new ExpressionException("class [" + clazz.getName() + "] is not of type [" + Cache.class.getName() + "]");
		}
		catch (ClassException e) {
			throw new ExpressionException(e.getMessage());
		}

		if (name.equalsIgnoreCase(Caster.toString(root.get("defaultTemplate", null), null))) rem(root, "defaultTemplate");
		if (name.equalsIgnoreCase(Caster.toString(root.get("defaultObject", null), null))) rem(root, "defaultObject");
		if (name.equalsIgnoreCase(Caster.toString(root.get("defaultQuery", null), null))) rem(root, "defaultQuery");
		if (name.equalsIgnoreCase(Caster.toString(root.get("defaultResource", null), null))) rem(root, "defaultResource");
		if (name.equalsIgnoreCase(Caster.toString(root.get("defaultFunction", null), null))) rem(root, "defaultFunction");
		if (name.equalsIgnoreCase(Caster.toString(root.get("defaultInclude", null), null))) rem(root, "defaultInclude");

		if (_default == ConfigPro.CACHE_TYPE_OBJECT) {
			root.setEL("defaultObject", name);
		}
		else if (_default == ConfigPro.CACHE_TYPE_TEMPLATE) {
			root.setEL("defaultTemplate", name);
		}
		else if (_default == ConfigPro.CACHE_TYPE_QUERY) {
			root.setEL("defaultQuery", name);
		}
		else if (_default == ConfigPro.CACHE_TYPE_RESOURCE) {
			root.setEL("defaultResource", name);
		}
		else if (_default == ConfigPro.CACHE_TYPE_FUNCTION) {
			root.setEL("defaultFunction", name);
		}
		else if (_default == ConfigPro.CACHE_TYPE_INCLUDE) {
			root.setEL("defaultInclude", name);
		}
		else if (_default == ConfigPro.CACHE_TYPE_HTTP) {
			root.setEL("defaultHttp", name);
		}
		else if (_default == ConfigPro.CACHE_TYPE_FILE) {
			root.setEL("defaultFile", name);
		}
		else if (_default == ConfigPro.CACHE_TYPE_WEBSERVICE) {
			root.setEL("defaultWebservice", name);
		}

		// Update
		// boolean isUpdate=false;
		Struct conns = ConfigWebUtil.getAsStruct("caches", root);
		Iterator<Key> it = conns.keyIterator();
		Key key;
		while (it.hasNext()) {
			key = it.next();
			if (key.getString().equalsIgnoreCase(name)) {
				Struct el = Caster.toStruct(conns.get(key, null), null);
				setClass(el, null, "", cd);
				el.setEL("custom", toStringURLStyle(custom));
				el.setEL("readOnly", Caster.toString(readOnly));
				el.setEL("storage", Caster.toString(storage));
				return;
			}
		}

		// Insert
		Struct data = new StructImpl(Struct.TYPE_LINKED);
		conns.setEL(name, data);
		setClass(data, null, "", cd);
		data.setEL("custom", toStringURLStyle(custom));
		data.setEL("readOnly", Caster.toString(readOnly));
		data.setEL("storage", Caster.toString(storage));

	}

	private void rem(Struct sct, String key) {
		sct.removeEL(KeyImpl.init(key));
	}

	public void removeCacheDefaultConnection(int type) throws PageException {
		checkWriteAccess();

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE);
		if (!hasAccess) throw new SecurityException("no access to update cache connections");

		Struct parent = _getRootElement("cache");
		if (type == ConfigPro.CACHE_TYPE_OBJECT) {
			rem(parent, "defaultObject");
		}
		else if (type == ConfigPro.CACHE_TYPE_TEMPLATE) {
			rem(parent, "defaultTemplate");
		}
		else if (type == ConfigPro.CACHE_TYPE_QUERY) {
			rem(parent, "defaultQuery");
		}
		else if (type == ConfigPro.CACHE_TYPE_RESOURCE) {
			rem(parent, "defaultResource");
		}
		else if (type == ConfigPro.CACHE_TYPE_FUNCTION) {
			rem(parent, "defaultFunction");
		}
		else if (type == ConfigPro.CACHE_TYPE_INCLUDE) {
			rem(parent, "defaultInclude");
		}
		else if (type == ConfigPro.CACHE_TYPE_HTTP) {
			rem(parent, "defaultHttp");
		}
		else if (type == ConfigPro.CACHE_TYPE_FILE) {
			rem(parent, "defaultFile");
		}
		else if (type == ConfigPro.CACHE_TYPE_WEBSERVICE) {
			rem(parent, "defaultWebservice");
		}
	}

	public void updateCacheDefaultConnection(int type, String name) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE);

		if (!hasAccess) throw new SecurityException("no access to update cache default connections");

		Struct parent = _getRootElement("cache");
		if (type == ConfigPro.CACHE_TYPE_OBJECT) {
			parent.setEL("defaultObject", name);
		}
		else if (type == ConfigPro.CACHE_TYPE_TEMPLATE) {
			parent.setEL("defaultTemplate", name);
		}
		else if (type == ConfigPro.CACHE_TYPE_QUERY) {
			parent.setEL("defaultQuery", name);
		}
		else if (type == ConfigPro.CACHE_TYPE_RESOURCE) {
			parent.setEL("defaultResource", name);
		}
		else if (type == ConfigPro.CACHE_TYPE_FUNCTION) {
			parent.setEL("defaultFunction", name);
		}
		else if (type == ConfigPro.CACHE_TYPE_INCLUDE) {
			parent.setEL("defaultInclude", name);
		}
		else if (type == ConfigPro.CACHE_TYPE_HTTP) {
			parent.setEL("defaultHttp", name);
		}
		else if (type == ConfigPro.CACHE_TYPE_FILE) {
			parent.setEL("defaultFile", name);
		}
		else if (type == ConfigPro.CACHE_TYPE_WEBSERVICE) {
			parent.setEL("defaultWebservice", name);
		}
	}

	public void removeResourceProvider(String scheme) throws PageException {
		checkWriteAccess();
		SecurityManager sm = config.getSecurityManager();
		short access = sm.getAccess(SecurityManager.TYPE_FILE);
		boolean hasAccess = access == SecurityManager.VALUE_YES;

		if (!hasAccess) throw new SecurityException("no access to remove resource provider");

		_removeResourceProvider(scheme);
	}

	public void _removeResourceProvider(String scheme) throws PageException {

		Array children = ConfigWebUtil.getAsArray("resourceProviders", root);
		Key[] keys = children.keys();

		// remove
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			String elScheme = ConfigWebUtil.getAsString("scheme", tmp, "");
			if (elScheme.equalsIgnoreCase(scheme)) {
				children.removeEL(key);
				break;
			}
		}
	}

	public void updateResourceProvider(String scheme, ClassDefinition cd, Struct arguments) throws PageException {
		updateResourceProvider(scheme, cd, toStringCSSStyle(arguments));
	}

	public void _updateResourceProvider(String scheme, ClassDefinition cd, Struct arguments) throws PageException {
		_updateResourceProvider(scheme, cd, toStringCSSStyle(arguments));
	}

	public void updateResourceProvider(String scheme, ClassDefinition cd, String arguments) throws PageException {
		checkWriteAccess();
		SecurityManager sm = config.getSecurityManager();
		short access = sm.getAccess(SecurityManager.TYPE_FILE);
		boolean hasAccess = access == SecurityManager.VALUE_YES;

		if (!hasAccess) throw new SecurityException("no access to update resources");
		_updateResourceProvider(scheme, cd, arguments);
	}

	public void _updateResourceProvider(String scheme, ClassDefinition cd, String arguments) throws PageException {

		// check parameters
		if (StringUtil.isEmpty(scheme)) throw new ExpressionException("scheme can't be an empty value");

		Array children = ConfigWebUtil.getAsArray("resourceProviders", root);

		// Update
		for (int i = 1; i <= children.size(); i++) {
			Struct el = Caster.toStruct(children.get(i, null), null);
			if (el == null) continue;

			String elScheme = ConfigWebUtil.getAsString("scheme", el, null);
			if (elScheme.equalsIgnoreCase(scheme)) {
				setClass(el, null, "", cd);
				el.setEL("scheme", scheme);
				el.setEL("arguments", arguments);
				return;
			}
		}

		// Insert
		Struct el = new StructImpl();
		children.appendEL(el);
		el.setEL("scheme", scheme);
		el.setEL("arguments", arguments);
		setClass(el, null, "", cd);
	}

	public void updateDefaultResourceProvider(ClassDefinition cd, String arguments) throws PageException {
		checkWriteAccess();
		SecurityManager sm = config.getSecurityManager();
		short access = sm.getAccess(SecurityManager.TYPE_FILE);
		boolean hasAccess = access == SecurityManager.VALUE_YES;

		if (!hasAccess) throw new SecurityException("no access to update resources");

		Array children = ConfigWebUtil.getAsArray("defaultResourceProviders", root);

		// Update
		for (int i = 1; i <= children.size(); i++) {
			Struct el = Caster.toStruct(children.get(i, null), null);
			if (el == null) continue;

			el.setEL("arguments", arguments);
			return;
		}

		// Insert
		Struct el = new StructImpl(Struct.TYPE_LINKED);
		children.appendEL(el);
		el.setEL("arguments", arguments);
		setClass(el, null, "", cd);
	}

	private int getDatasourceLength(ConfigPro config) {
		Map ds = config.getDataSourcesAsMap();
		Iterator it = ds.keySet().iterator();
		int len = 0;

		while (it.hasNext()) {
			if (!((DataSource) ds.get(it.next())).isReadOnly()) len++;
		}
		return len;
	}

	private static String toStringURLStyle(Struct sct) {
		if (sct == null) return "";
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		StringBuilder rtn = new StringBuilder();
		while (it.hasNext()) {
			e = it.next();
			if (rtn.length() > 0) rtn.append('&');
			rtn.append(URLEncoder.encode(e.getKey().getString()));
			rtn.append('=');
			rtn.append(URLEncoder.encode(Caster.toString(e.getValue(), "")));
		}
		return rtn.toString();
	}

	private static String toStringCSSStyle(Struct sct) {
		// Collection.Key[] keys = sct.keys();
		StringBuilder rtn = new StringBuilder();
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;

		while (it.hasNext()) {
			e = it.next();
			if (rtn.length() > 0) rtn.append(';');
			rtn.append(encode(e.getKey().getString()));
			rtn.append(':');
			rtn.append(encode(Caster.toString(e.getValue(), "")));
		}
		return rtn.toString();
	}

	private static String encode(String str) {
		try {
			return URLEncodedFormat.invoke(str, "UTF-8", false);
		}
		catch (PageException e) {
			return URLEncoder.encode(str);
		}
	}

	public Query getResourceProviders() throws PageException {
		checkReadAccess();
		// check parameters
		Array elProviders = ConfigWebUtil.getAsArray("resourceProviders", root);
		Array elDefaultProviders = ConfigWebUtil.getAsArray("defaultResourceProvider", root);

		ResourceProvider[] providers = config.getResourceProviders();
		ResourceProvider defaultProvider = config.getDefaultResourceProvider();

		Query qry = new QueryImpl(new String[] { "support", "scheme", "caseSensitive", "default", "class", "bundleName", "bundleVersion", "arguments" },
				elProviders.size() + elDefaultProviders.size(), "resourceproviders");
		int row = 1;
		for (int i = 1; i <= elDefaultProviders.size(); i++) {
			Struct tmp = Caster.toStruct(elDefaultProviders.get(i, null), null);
			if (tmp == null) continue;
			getResourceProviders(new ResourceProvider[] { defaultProvider }, qry, tmp, row++, Boolean.TRUE);
		}
		for (int i = 1; i <= elProviders.size(); i++) {
			Struct tmp = Caster.toStruct(elProviders.get(i, null), null);
			if (tmp == null) continue;
			getResourceProviders(providers, qry, tmp, row++, Boolean.FALSE);
		}
		return qry;
	}

	private void getResourceProviders(ResourceProvider[] providers, Query qry, Struct p, int row, Boolean def) throws PageException {
		Array support = new ArrayImpl();
		String cn = ConfigWebUtil.getAsString("class", p, null);
		String name = ConfigWebUtil.getAsString("bundleName", p, null);
		String version = ConfigWebUtil.getAsString("bundleVersion", p, null);
		ClassDefinition cd = new ClassDefinitionImpl(cn, name, version, ThreadLocalPageContext.getConfig().getIdentification());

		qry.setAt("scheme", row, p.get("scheme"));
		qry.setAt("arguments", row, p.get("arguments"));

		qry.setAt("class", row, cd.getClassName());
		qry.setAt("bundleName", row, cd.getName());
		qry.setAt("bundleVersion", row, cd.getVersionAsString());
		for (int i = 0; i < providers.length; i++) {
			if (providers[i].getClass().getName().equals(cd.getClassName())) {
				if (providers[i].isAttributesSupported()) support.append("attributes");
				if (providers[i].isModeSupported()) support.append("mode");
				qry.setAt("support", row, ListUtil.arrayToList(support, ","));
				qry.setAt("scheme", row, providers[i].getScheme());
				qry.setAt("caseSensitive", row, Caster.toBoolean(providers[i].isCaseSensitive()));
				qry.setAt("default", row, def);
				break;
			}
		}
	}

	public void removeJDBCDriver(String className) throws ExpressionException, SecurityException {
		checkWriteAccess();
		// check parameters
		if (StringUtil.isEmpty(className)) throw new ExpressionException("class name for jdbc driver cannot be empty");

		Struct children = ConfigWebUtil.getAsStruct("jdbcDrivers", root);
		Key[] keys = children.keys();
		for (Key key: keys) {
			if (key.getString().equalsIgnoreCase(className)) {
				children.removeEL(key);
			}
		}
	}

	/**
	 * remove a DataSource Connection
	 * 
	 * @param name
	 * @throws ExpressionException
	 * @throws SecurityException
	 */
	public void removeDataSource(String name) throws ExpressionException, SecurityException {
		checkWriteAccess();
		// check parameters
		if (name == null || name.length() == 0) throw new ExpressionException("name for Datasource Connection can be an empty value");

		Struct children = ConfigWebUtil.getAsStruct("dataSources", root);
		Key[] keys = children.keys();
		for (Key key: keys) {
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			if (key.getString().equalsIgnoreCase(name)) {
				children.removeEL(key);
			}
		}
	}

	public void removeCacheConnection(String name) throws ExpressionException, SecurityException {
		checkWriteAccess();

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE);
		if (!hasAccess) throw new SecurityException("no access to remove cache connection");

		// check parameters
		if (StringUtil.isEmpty(name)) throw new ExpressionException("name for Cache Connection can not be an empty value");

		Struct parent = _getRootElement("cache");

		// remove default flag
		if (name.equalsIgnoreCase(Caster.toString(parent.get("defaultObject", null), null))) rem(parent, "defaultObject");
		if (name.equalsIgnoreCase(Caster.toString(parent.get("defaultTemplate", null), null))) rem(parent, "defaultTemplate");
		if (name.equalsIgnoreCase(Caster.toString(parent.get("defaultQuery", null), null))) rem(parent, "defaultQuery");
		if (name.equalsIgnoreCase(Caster.toString(parent.get("defaultResource", null), null))) rem(parent, "defaultResource");

		// remove element
		Array children = ConfigWebUtil.getAsArray("connection", parent);
		Key[] keys = children.keys();
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			String n = ConfigWebUtil.getAsString("name", tmp, "");
			if (n != null && n.equalsIgnoreCase(name)) {
				Map<String, CacheConnection> conns = config.getCacheConnections();
				CacheConnection cc = conns.get(n.toLowerCase());
				if (cc != null) {
					CacheUtil.releaseEL(cc);
					// CacheUtil.removeEL( config instanceof ConfigWeb ? (ConfigWeb) config : null, cc );
				}

				children.removeEL(key);
			}
		}
	}

	public boolean cacheConnectionExists(String name) throws ExpressionException, SecurityException {
		checkReadAccess();
		if (!ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE)) throw new SecurityException("no access to check cache connection");
		if (name == null || name.isEmpty()) throw new ExpressionException("name for Cache Connection can not be an empty value");

		Array children = ConfigWebUtil.getAsArray("cache", "connection", root);
		for (int i = 1; i <= children.size(); i++) {
			Struct tmp = Caster.toStruct(children.get(i, null), null);
			if (tmp == null) continue;

			String n = ConfigWebUtil.getAsString("name", tmp, null);
			if (n != null && n.equalsIgnoreCase(name)) return true;
		}
		return false;
	}

	public void removeGatewayEntry(String name) throws PageException {
		checkWriteAccess();

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY);
		if (!hasAccess) throw new SecurityException("no access to remove gateway entry");

		_removeGatewayEntry(name);
	}

	protected void _removeGatewayEntry(String name) throws PageException {
		if (StringUtil.isEmpty(name)) throw new ExpressionException("name for Gateway Id can be an empty value");

		Struct children = ConfigWebUtil.getAsStruct("gateways", root);
		Key[] keys = children.keys();
		// remove element
		for (Key key: keys) {
			String n = key.getString();
			if (n != null && n.equalsIgnoreCase(name)) {

				if (config instanceof ConfigWeb) {
					_removeGatewayEntry((ConfigWebPro) config, n);
				}
				else {
					ConfigWeb[] cws = ((ConfigServerImpl) config).getConfigWebs();
					for (ConfigWeb cw: cws) {
						_removeGatewayEntry((ConfigWebPro) cw, name);
					}
				}
				children.removeEL(key);
			}
		}
	}

	private void _removeGatewayEntry(ConfigWebPro cw, String name) {
		GatewayEngineImpl engine = (GatewayEngineImpl) cw.getGatewayEngine();
		Map<String, GatewayEntry> conns = engine.getEntries();
		GatewayEntry ge = conns.get(name);
		if (ge != null) {
			engine.remove(ge);
		}
	}

	public void removeRemoteClient(String url) throws ExpressionException, SecurityException {
		checkWriteAccess();

		// SNSN

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE);
		if (!hasAccess) throw new SecurityException("no access to remove remote client settings");

		// check parameters
		if (StringUtil.isEmpty(url)) throw new ExpressionException("url for Remote Client can be an empty value");

		Array children = ConfigWebUtil.getAsArray("remoteClients", "remoteClient", root);
		Key[] keys = children.keys();
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			String n = ConfigWebUtil.getAsString("url", tmp, null);
			if (n != null && n.equalsIgnoreCase(url)) {
				children.removeEL(key);
			}
		}
	}

	/**
	 * update PSQ State
	 * 
	 * @param psq Preserver Single Quote
	 * @throws SecurityException
	 */
	public void updatePSQ(Boolean psq) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DATASOURCE);

		if (!hasAccess) throw new SecurityException("no access to update datasource connections");

		root.setEL("preserveSingleQuote", Caster.toBooleanValue(psq, true));
	}

	public void updateInspectTemplate(String str) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update");

		root.setEL("inspectTemplate", str);

	}

	public void updateTypeChecking(Boolean typeChecking) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update");

		if (typeChecking == null) rem(root, "typeChecking");
		else root.setEL("typeChecking", Caster.toString(typeChecking.booleanValue()));

	}

	public void updateCachedAfterTimeRange(TimeSpan ts) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update");

		if (ts == null) rem(root, "cachedAfter");
		else {
			if (ts.getMillis() < 0) throw new ApplicationException("value cannot be a negative number");
			root.setEL("cachedAfter", ts.getDay() + "," + ts.getHour() + "," + ts.getMinute() + "," + ts.getSecond());
		}
	}

	/**
	 * sets the scope cascading type
	 * 
	 * @param type (SCOPE_XYZ)
	 * @throws SecurityException
	 */
	public void updateScopeCascadingType(String type) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		if (type.equalsIgnoreCase("strict")) root.setEL("scopeCascading", "strict");
		else if (type.equalsIgnoreCase("small")) root.setEL("scopeCascading", "small");
		else if (type.equalsIgnoreCase("standard")) root.setEL("scopeCascading", "standard");
		else root.setEL("scopeCascading", "standard");

	}

	/**
	 * sets the scope cascading type
	 * 
	 * @param type (SCOPE_XYZ)
	 * @throws SecurityException
	 */
	public void updateScopeCascadingType(short type) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		if (type == ConfigWeb.SCOPE_STRICT) root.setEL("scopeCascading", "strict");
		else if (type == ConfigWeb.SCOPE_SMALL) root.setEL("scopeCascading", "small");
		else if (type == ConfigWeb.SCOPE_STANDARD) root.setEL("scopeCascading", "standard");

	}

	/**
	 * sets if allowed implicid query call
	 * 
	 * @param allow
	 * @throws SecurityException
	 */
	public void updateAllowImplicidQueryCall(Boolean allow) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("cascadeToResultset", allow);

	}

	public void updateMergeFormAndUrl(Boolean merge) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("mergeUrlForm", merge);

	}

	/**
	 * updates request timeout value
	 * 
	 * @param span
	 * @throws SecurityException
	 * @throws ApplicationException
	 */
	public void updateRequestTimeout(TimeSpan span) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		if (span != null) {
			if (span.getMillis() <= 0) throw new ApplicationException("value must be a positive number");
			root.setEL("requestTimeout", span.getDay() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond());
		}
		else rem(root, "requestTimeout");
	}

	public void updateApplicationPathTimeout(TimeSpan span) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		if (span != null) {
			if (span.getMillis() <= 0) throw new ApplicationException("value must be a positive number");
			root.setEL("applicationPathTimeout", span.getDay() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond());
		}
		else rem(root, "applicationPathTimeout");
	}

	/**
	 * updates session timeout value
	 * 
	 * @param span
	 * @throws SecurityException
	 */
	public void updateSessionTimeout(TimeSpan span) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		if (span != null) root.setEL("sessiontimeout", span.getDay() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond());
		else rem(root, "sessiontimeout");
	}

	public void updateClientStorage(String storage) throws SecurityException, ApplicationException {
		updateStorage("client", storage);
	}

	public void updateSessionStorage(String storage) throws SecurityException, ApplicationException {
		updateStorage("session", storage);
	}

	private void updateStorage(String storageName, String storage) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");
		storage = validateStorage(storage);

		if (!StringUtil.isEmpty(storage, true)) root.setEL(storageName + "Storage", storage);
		else rem(root, storageName + "Storage");
	}

	private String validateStorage(String storage) throws ApplicationException {
		storage = storage.trim().toLowerCase();

		// empty
		if (StringUtil.isEmpty(storage, true)) return "";

		// standard storages
		if ("cookie".equals(storage) || "memory".equals(storage) || "file".equals(storage)) return storage;

		// aliases
		if ("ram".equals(storage)) return "memory";
		if ("registry".equals(storage)) return "file";

		// datasource
		DataSource ds = config.getDataSource(storage, null);
		if (ds != null) {
			if (ds.isStorage()) return storage;
			throw new ApplicationException("datasource [" + storage + "] is not enabled to be used as session/client storage");
		}

		// cache
		CacheConnection cc = CacheUtil.getCacheConnection(ThreadLocalPageContext.get(config), storage, null);
		if (cc != null) {
			if (cc.isStorage()) return storage;
			throw new ApplicationException("cache [" + storage + "] is not enabled to be used as session/client storage");
		}

		String sdx = StringUtil.soundex(storage);

		// check if a datasource has a similar name
		DataSource[] sources = config.getDataSources();
		for (int i = 0; i < sources.length; i++) {
			if (StringUtil.soundex(sources[i].getName()).equals(sdx))
				throw new ApplicationException("no matching storage for [" + storage + "] found, did you mean [" + sources[i].getName() + "]");
		}

		// check if a cache has a similar name
		Iterator<String> it = config.getCacheConnections().keySet().iterator();
		String name;
		while (it.hasNext()) {
			name = it.next();
			if (StringUtil.soundex(name).equals(sdx)) throw new ApplicationException("no matching storage for [" + storage + "] found, did you mean [" + name + "]");
		}

		throw new ApplicationException("no matching storage for [" + storage + "] found");
	}

	/**
	 * updates session timeout value
	 * 
	 * @param span
	 * @throws SecurityException
	 */
	public void updateClientTimeout(TimeSpan span) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		if (span != null) root.setEL("clientTimeout", span.getDay() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond());
		else rem(root, "clientTimeout");
	}

	public void updateCFMLWriterType(String writerType) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		writerType = writerType.trim();

		// remove
		if (StringUtil.isEmpty(writerType)) {
			if (root.containsKey("cfmlWriter")) rem(root, "cfmlWriter");
			return;
		}

		// update
		if (!"white-space".equalsIgnoreCase(writerType) && !"white-space-pref".equalsIgnoreCase(writerType) && !"regular".equalsIgnoreCase(writerType))
			throw new ApplicationException("invalid writer type definition [" + writerType + "], valid types are [white-space, white-space-pref, regular]");

		root.setEL("cfmlWriter", writerType.toLowerCase());
	}

	public void updateSuppressContent(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("suppressContent", Caster.toString(value, ""));
	}

	public void updateShowVersion(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("showVersion", Caster.toString(value, ""));
	}

	public void updateAllowCompression(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("allowCompression", value);
	}

	public void updateContentLength(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("showContentLength", Caster.toString(value, ""));
	}

	public void updateBufferOutput(Boolean value) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("bufferTagBodyOutput", value);
	}

	/**
	 * updates request timeout value
	 * 
	 * @param span
	 * @throws SecurityException
	 */
	public void updateApplicationTimeout(TimeSpan span) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		if (span != null) root.setEL("applicationTimeout", span.getDay() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond());
		else rem(root, "applicationTimeout");
	}

	public void updateApplicationListener(String type, String mode) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update listener type");

		root.setEL("listenerType", type.toLowerCase().trim());
		root.setEL("listenerMode", mode.toLowerCase().trim());
	}

	public void updateCachedWithin(int type, Object value) throws SecurityException, ApplicationException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update cachedwithin setting");

		String t = AppListenerUtil.toCachedWithinType(type, "");
		if (t == null) throw new ApplicationException("invalid cachedwithin type definition");
		String v = Caster.toString(value, null);
		if (v != null) root.setEL("cachedWithin" + StringUtil.ucFirst(t), v);
		else rem(root, "cachedWithin" + StringUtil.ucFirst(t));
	}

	public void updateProxy(boolean enabled, String server, int port, String username, String password) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update listener type");

		Struct proxy = _getRootElement("proxy");
		proxy.setEL("enabled", Caster.toString(enabled));
		if (!StringUtil.isEmpty(server)) proxy.setEL("server", server);
		if (port > 0) proxy.setEL("port", Caster.toString(port));
		if (!StringUtil.isEmpty(username)) proxy.setEL("username", username);
		if (!StringUtil.isEmpty(password)) proxy.setEL("password", password);
	}

	/*
	 * public void removeProxy() throws SecurityException { boolean
	 * hasAccess=ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_SETTING); if(!hasAccess) throw new
	 * SecurityException("no access to remove proxy settings");
	 * 
	 * Element proxy=_getRootElement("proxy"); proxy.removeAttribute("server");
	 * proxy.removeAttribute("port"); proxy.removeAttribute("username");
	 * proxy.removeAttribute("password"); }
	 */

	/**
	 * enable or desable session management
	 * 
	 * @param sessionManagement
	 * @throws SecurityException
	 */
	public void updateSessionManagement(Boolean sessionManagement) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("sessionManagement", Caster.toString(sessionManagement, ""));
	}

	/**
	 * enable or desable client management
	 * 
	 * @param clientManagement
	 * @throws SecurityException
	 */
	public void updateClientManagement(Boolean clientManagement) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);

		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("clientManagement", Caster.toString(clientManagement, ""));
	}

	/**
	 * set if client cookies are enabled or not
	 * 
	 * @param clientCookies
	 * @throws SecurityException
	 */
	public void updateClientCookies(Boolean clientCookies) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("clientCookies", clientCookies);
	}

	/**
	 * set if it's develop mode or not
	 * 
	 * @param developmode
	 * @throws SecurityException
	 */
	public void updateMode(Boolean developmode) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("developMode", Caster.toString(developmode, ""));
	}

	/**
	 * set if domain cookies are enabled or not
	 * 
	 * @param domainCookies
	 * @throws SecurityException
	 */
	public void updateDomaincookies(Boolean domainCookies) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		root.setEL("domainCookies", Caster.toString(domainCookies, ""));
	}

	/**
	 * update the locale
	 * 
	 * @param locale
	 * @throws SecurityException
	 */
	public void updateLocale(String locale) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update regional setting");

		root.setEL("locale", locale.trim());
	}

	public void updateMonitorEnabled(boolean updateMonitorEnabled) throws SecurityException {
		checkWriteAccess();
		_updateMonitorEnabled(updateMonitorEnabled);
	}

	void _updateMonitorEnabled(boolean updateMonitorEnabled) {
		root.setEL("monitorEnable", Caster.toString(updateMonitorEnabled));
	}

	public void updateScriptProtect(String strScriptProtect) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update script protect");

		root.setEL("scriptProtect", strScriptProtect.trim());
	}

	public void updateAllowURLRequestTimeout(Boolean allowURLRequestTimeout) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update AllowURLRequestTimeout");

		root.setEL("requestTimeoutInURL", Caster.toString(allowURLRequestTimeout, ""));
	}

	public void updateScriptProtect(int scriptProtect) throws SecurityException {
		updateScriptProtect(AppListenerUtil.translateScriptProtect(scriptProtect));
	}

	/**
	 * update the timeZone
	 * 
	 * @param timeZone
	 * @throws SecurityException
	 */
	public void updateTimeZone(String timeZone) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update regional setting");

		root.setEL("timezone", timeZone.trim());

	}

	/**
	 * update the timeServer
	 * 
	 * @param timeServer
	 * @param useTimeServer
	 * @throws PageException
	 */
	public void updateTimeServer(String timeServer, Boolean useTimeServer) throws PageException {
		checkWriteAccess();
		if (useTimeServer != null && useTimeServer.booleanValue() && !StringUtil.isEmpty(timeServer, true)) {
			try {
				new NtpClient(timeServer).getOffset();
			}
			catch (IOException e) {
				try {
					new NtpClient(timeServer).getOffset();
				}
				catch (IOException ee) {
					throw Caster.toPageException(ee);
				}
			}
		}

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update regional setting");

		root.setEL("timeserver", timeServer.trim());
		if (useTimeServer != null) root.setEL("useTimeserver", Caster.toBooleanValue(useTimeServer));
		else rem(root, "useTimeserver");
	}

	/**
	 * update the baseComponent
	 * 
	 * @param baseComponent
	 * @throws SecurityException
	 */
	public void updateBaseComponent(String baseComponentCFML, String baseComponentLucee) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");
		root.setEL("componentBase", baseComponentCFML);
		root.setEL("componentBaseLuceeDialect", baseComponentLucee);
	}

	public void updateComponentDeepSearch(Boolean deepSearch) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");
		if (deepSearch != null) root.setEL("componentDeepSearch", Caster.toString(deepSearch.booleanValue()));
		else {
			if (root.containsKey("componentDeepSearch")) rem(root, "componentDeepSearch");
		}

	}

	public void updateComponentDefaultImport(String componentDefaultImport) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");
		root.setEL("componentAutoImport", componentDefaultImport);
	}

	/**
	 * update the Component Data Member default access type
	 * 
	 * @param strAccess
	 * @throws SecurityException
	 * @throws ExpressionException
	 */
	public void updateComponentDataMemberDefaultAccess(String strAccess) throws SecurityException, ApplicationException {
		checkWriteAccess();

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");

		if (StringUtil.isEmpty(strAccess)) {
			root.setEL("componentDataMemberAccess", "");
		}
		else {
			root.setEL("componentDataMemberAccess", ComponentUtil.toStringAccess(ComponentUtil.toIntAccess(strAccess)));
		}
	}

	/**
	 * update the Component Data Member default access type
	 * 
	 * @param triggerDataMember
	 * @throws SecurityException
	 */
	public void updateTriggerDataMember(Boolean triggerDataMember) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update trigger-data-member");

		root.setEL("componentImplicitNotation", Caster.toString(triggerDataMember, ""));
	}

	public void updateComponentUseShadow(Boolean useShadow) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update use-shadow");

		root.setEL("componentUseVariablesScope", Caster.toString(useShadow, ""));
	}

	public void updateComponentLocalSearch(Boolean componentLocalSearch) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component Local Search");

		root.setEL("componentLocalSearch", Caster.toString(componentLocalSearch, ""));
	}

	public void updateComponentPathCache(Boolean componentPathCache) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component Cache Path");

		if (!Caster.toBooleanValue(componentPathCache, false)) config.clearComponentCache();
		root.setEL("componentUseCachePath", Caster.toString(componentPathCache, ""));
	}

	public void updateCTPathCache(Boolean ctPathCache) throws SecurityException {
		checkWriteAccess();
		if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw new SecurityException("no access to update custom tag setting");

		if (!Caster.toBooleanValue(ctPathCache, false)) config.clearCTCache();
		root.setEL("customTagUseCachePath", Caster.toString(ctPathCache, ""));
	}

	public void updateSecurity(String varUsage) throws SecurityException {
		checkWriteAccess();
		Struct el = _getRootElement("security");

		if (el != null) {
			if (!StringUtil.isEmpty(varUsage)) el.setEL("variableUsage", Caster.toString(varUsage));
			else rem(el, "variableUsage");
		}

	}

	/**
	 * updates if debugging or not
	 * 
	 * @param debug if value is null server setting is used
	 * @throws SecurityException
	 */
	public void updateDebug(Boolean debug, Boolean template, Boolean database, Boolean exception, Boolean tracing, Boolean dump, Boolean timer, Boolean implicitAccess,
			Boolean queryUsage, Boolean thread) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);
		if (!hasAccess) throw new SecurityException("no access to change debugging settings");

		if (debug != null) root.setEL("debuggingEnabled", debug.booleanValue());
		else rem(root, "debuggingEnabled");

		if (database != null) root.setEL("debuggingDatabase", database.booleanValue());
		else rem(root, "debuggingDatabase");

		if (template != null) root.setEL("debuggingTemplate", template.booleanValue());
		else rem(root, "debuggingTemplate");

		if (exception != null) root.setEL("debuggingException", exception.booleanValue());
		else rem(root, "debuggingException");

		if (tracing != null) root.setEL("debuggingTracing", tracing.booleanValue());
		else rem(root, "debuggingTracing");

		if (dump != null) root.setEL("debuggingDump", dump.booleanValue());
		else rem(root, "debuggingDump");

		if (timer != null) root.setEL("debuggingTimer", timer.booleanValue());
		else rem(root, "debuggingTimer");

		if (implicitAccess != null) root.setEL("debuggingImplicitAccess", implicitAccess.booleanValue());
		else rem(root, "debuggingImplicitAccess");

		if (queryUsage != null) root.setEL("debuggingQueryUsage", queryUsage.booleanValue());
		else rem(root, "debuggingQueryUsage");

		if (queryUsage != null) root.setEL("debuggingThread", thread.booleanValue());
		else rem(root, "debuggingThread");
	}

	/**
	 * updates the ErrorTemplate
	 * 
	 * @param template
	 * @throws SecurityException
	 */
	public void updateErrorTemplate(int statusCode, String template) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to change error settings");

		if (statusCode == 404) root.setEL("errorMissingTemplate", template);
		else root.setEL("errorGeneralTemplate", template);
	}

	public void updateErrorStatusCode(Boolean doStatusCode) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to change error settings");

		root.setEL("errorStatusCode", Caster.toString(doStatusCode, ""));
	}

	public void updateRegexType(String type) throws PageException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to change regex settings");

		if (StringUtil.isEmpty(type)) rem(root, "regexType");
		else root.setEL("regexType", RegexFactory.toType(RegexFactory.toType(type), "perl"));
	}

	/**
	 * updates the DebugTemplate
	 * 
	 * @param template
	 * @throws SecurityException
	 */
	public void updateComponentDumpTemplate(String template) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update component setting");

		root.setEL("componentDumpTemplate", template);
	}

	private Struct _getRootElement(String name) {
		return ConfigWebUtil.getAsStruct(name, root);
	}

	/**
	 * @param setting
	 * @param file
	 * @param directJavaAccess
	 * @param mail
	 * @param datasource
	 * @param mapping
	 * @param customTag
	 * @param cfxSetting
	 * @param cfxUsage
	 * @param debugging
	 * @param search
	 * @param scheduledTasks
	 * @param tagExecute
	 * @param tagImport
	 * @param tagObject
	 * @param tagRegistry
	 * @throws SecurityException
	 */
	public void updateDefaultSecurity(short setting, short file, Resource[] fileAccess, short directJavaAccess, short mail, short datasource, short mapping, short remote,
			short customTag, short cfxSetting, short cfxUsage, short debugging, short search, short scheduledTasks, short tagExecute, short tagImport, short tagObject,
			short tagRegistry, short cache, short gateway, short orm, short accessRead, short accessWrite) throws SecurityException {
		checkWriteAccess();
		if (!(config instanceof ConfigServer)) throw new SecurityException("can't change security settings from this context");

		Struct security = _getRootElement("security");
		updateSecurityFileAccess(security, fileAccess, file);
		security.setEL("setting", SecurityManagerImpl.toStringAccessValue(setting));
		security.setEL("file", SecurityManagerImpl.toStringAccessValue(file));
		security.setEL("direct_java_access", SecurityManagerImpl.toStringAccessValue(directJavaAccess));
		security.setEL("mail", SecurityManagerImpl.toStringAccessValue(mail));
		security.setEL("datasource", SecurityManagerImpl.toStringAccessValue(datasource));
		security.setEL("mapping", SecurityManagerImpl.toStringAccessValue(mapping));
		security.setEL("remote", SecurityManagerImpl.toStringAccessValue(remote));
		security.setEL("custom_tag", SecurityManagerImpl.toStringAccessValue(customTag));
		security.setEL("cfx_setting", SecurityManagerImpl.toStringAccessValue(cfxSetting));
		security.setEL("cfx_usage", SecurityManagerImpl.toStringAccessValue(cfxUsage));
		security.setEL("debugging", SecurityManagerImpl.toStringAccessValue(debugging));
		security.setEL("search", SecurityManagerImpl.toStringAccessValue(search));
		security.setEL("scheduled_task", SecurityManagerImpl.toStringAccessValue(scheduledTasks));

		security.setEL("tag_execute", SecurityManagerImpl.toStringAccessValue(tagExecute));
		security.setEL("tag_import", SecurityManagerImpl.toStringAccessValue(tagImport));
		security.setEL("tag_object", SecurityManagerImpl.toStringAccessValue(tagObject));
		security.setEL("tag_registry", SecurityManagerImpl.toStringAccessValue(tagRegistry));
		security.setEL("cache", SecurityManagerImpl.toStringAccessValue(cache));
		security.setEL("gateway", SecurityManagerImpl.toStringAccessValue(gateway));
		security.setEL("orm", SecurityManagerImpl.toStringAccessValue(orm));

		security.setEL("access_read", SecurityManagerImpl.toStringAccessRWValue(accessRead));
		security.setEL("access_write", SecurityManagerImpl.toStringAccessRWValue(accessWrite));

	}

	private void removeSecurityFileAccess(Struct parent) {
		Array children = ConfigWebUtil.getAsArray("fileAccess", parent);
		Key[] keys = children.keys();
		// remove existing
		if (children.size() > 0) {
			for (int i = keys.length - 1; i >= 0; i--) {
				Key key = keys[i];
				children.removeEL(key);
			}
		}
	}

	private void updateSecurityFileAccess(Struct parent, Resource[] fileAccess, short file) {
		removeSecurityFileAccess(parent);

		// insert
		if (!ArrayUtil.isEmpty(fileAccess) && file != SecurityManager.VALUE_ALL) {
			Struct fa;
			Array children = ConfigWebUtil.getAsArray("fileAccess", parent);
			for (int i = 0; i < fileAccess.length; i++) {
				fa = new StructImpl();
				fa.setEL("path", fileAccess[i].getAbsolutePath());
				children.appendEL(fa);
			}
		}

	}

	/**
	 * update a security manager that match the given id
	 * 
	 * @param id
	 * @param setting
	 * @param file
	 * @param fileAccess
	 * @param directJavaAccess
	 * @param mail
	 * @param datasource
	 * @param mapping
	 * @param customTag
	 * @param cfxSetting
	 * @param cfxUsage
	 * @param debugging
	 * @param search
	 * @param scheduledTasks
	 * @param tagExecute
	 * @param tagImport
	 * @param tagObject
	 * @param tagRegistry
	 * @throws SecurityException
	 * @throws ApplicationException
	 */
	public void updateSecurity(String id, short setting, short file, Resource[] fileAccess, short directJavaAccess, short mail, short datasource, short mapping, short remote,
			short customTag, short cfxSetting, short cfxUsage, short debugging, short search, short scheduledTasks, short tagExecute, short tagImport, short tagObject,
			short tagRegistry, short cache, short gateway, short orm, short accessRead, short accessWrite) throws SecurityException, ApplicationException {
		checkWriteAccess();
		if (!(config instanceof ConfigServer)) throw new SecurityException("can't change security settings from this context");

		Struct security = _getRootElement("security");
		Array children = ConfigWebUtil.getAsArray("accessor", security);
		Struct accessor = null;
		for (int i = 1; i <= children.size(); i++) {
			Struct tmp = Caster.toStruct(children.get(i, null), null);
			if (tmp == null) continue;

			if (id.equals(tmp.get("id", ""))) {
				accessor = tmp;
			}
		}
		if (accessor == null) throw new ApplicationException("there is noc Security Manager for id [" + id + "]");
		updateSecurityFileAccess(accessor, fileAccess, file);

		accessor.setEL("setting", SecurityManagerImpl.toStringAccessValue(setting));
		accessor.setEL("file", SecurityManagerImpl.toStringAccessValue(file));
		accessor.setEL("direct_java_access", SecurityManagerImpl.toStringAccessValue(directJavaAccess));
		accessor.setEL("mail", SecurityManagerImpl.toStringAccessValue(mail));
		accessor.setEL("datasource", SecurityManagerImpl.toStringAccessValue(datasource));
		accessor.setEL("mapping", SecurityManagerImpl.toStringAccessValue(mapping));
		accessor.setEL("remote", SecurityManagerImpl.toStringAccessValue(remote));
		accessor.setEL("custom_tag", SecurityManagerImpl.toStringAccessValue(customTag));
		accessor.setEL("cfx_setting", SecurityManagerImpl.toStringAccessValue(cfxSetting));
		accessor.setEL("cfx_usage", SecurityManagerImpl.toStringAccessValue(cfxUsage));
		accessor.setEL("debugging", SecurityManagerImpl.toStringAccessValue(debugging));
		accessor.setEL("search", SecurityManagerImpl.toStringAccessValue(search));
		accessor.setEL("scheduled_task", SecurityManagerImpl.toStringAccessValue(scheduledTasks));
		accessor.setEL("cache", SecurityManagerImpl.toStringAccessValue(cache));
		accessor.setEL("gateway", SecurityManagerImpl.toStringAccessValue(gateway));
		accessor.setEL("orm", SecurityManagerImpl.toStringAccessValue(orm));

		accessor.setEL("tag_execute", SecurityManagerImpl.toStringAccessValue(tagExecute));
		accessor.setEL("tag_import", SecurityManagerImpl.toStringAccessValue(tagImport));
		accessor.setEL("tag_object", SecurityManagerImpl.toStringAccessValue(tagObject));
		accessor.setEL("tag_registry", SecurityManagerImpl.toStringAccessValue(tagRegistry));

		accessor.setEL("access_read", SecurityManagerImpl.toStringAccessRWValue(accessRead));
		accessor.setEL("access_write", SecurityManagerImpl.toStringAccessRWValue(accessWrite));
	}

	/**
	 * @return returns the default password
	 * @throws SecurityException
	 */
	public Password getDefaultPassword() throws SecurityException {
		checkReadAccess();
		if (config instanceof ConfigServerImpl) {
			return ((ConfigServerImpl) config).getDefaultPassword();
		}
		throw new SecurityException("can't access default password within this context");
	}

	/**
	 * @param password
	 * @throws SecurityException
	 * @throws IOException
	 * @throws DOMException
	 */
	public void updateDefaultPassword(String password) throws SecurityException, IOException {
		checkWriteAccess();
		((ConfigServerImpl) config).setDefaultPassword(PasswordImpl.writeToStruct(root, password, true));
	}

	public void removeDefaultPassword() throws SecurityException {
		checkWriteAccess();
		PasswordImpl.removeFromStruct(root, true);
		((ConfigServerImpl) config).setDefaultPassword(null);
	}

	/**
	 * session type update
	 * 
	 * @param type
	 * @throws SecurityException
	 */
	public void updateSessionType(String type) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		type = type.toLowerCase().trim();

		root.setEL("sessionType", type);
	}

	public void updateLocalMode(String mode) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("no access to update scope setting");

		mode = mode.toLowerCase().trim();
		root.setEL("localScopeMode", mode);
	}

	public void updateRestList(Boolean list) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = true;// TODO ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST);
		if (!hasAccess) throw new SecurityException("no access to update rest setting");

		Struct rest = _getRootElement("rest");
		if (list == null) {
			if (rest.containsKey("list")) rem(rest, "list");
		}
		else rest.setEL("list", Caster.toString(list.booleanValue()));
	}

	/**
	 * updates update settingd for Lucee
	 * 
	 * @param type
	 * @param location
	 * @throws SecurityException
	 */
	public void updateUpdate(String type, String location) throws SecurityException {
		checkWriteAccess();

		if (!(config instanceof ConfigServer)) {
			throw new SecurityException("can't change update setting from this context, access is denied");
		}
		root.setEL("updateType", type);
		try {
			location = HTTPUtil.toURL(location, HTTPUtil.ENCODED_AUTO).toString();
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
		}
		root.setEL("updateLocation", location);
	}

	/**
	 * creates an individual security manager based on the default security manager
	 * 
	 * @param id
	 * @throws DOMException
	 * @throws PageException
	 */
	public void createSecurityManager(Password password, String id) throws PageException {
		checkWriteAccess();
		ConfigServerImpl cs = (ConfigServerImpl) ConfigWebUtil.getConfigServer(config, password);
		SecurityManagerImpl dsm = (SecurityManagerImpl) cs.getDefaultSecurityManager().cloneSecurityManager();
		cs.setSecurityManager(id, dsm);

		Struct security = _getRootElement("security");
		Struct accessor = null;

		Array children = ConfigWebUtil.getAsArray("accessor", security);
		for (int i = 1; i <= children.size(); i++) {
			Struct tmp = Caster.toStruct(children.get(i, null), null);
			if (tmp == null) continue;

			if (id.equals(tmp.get("id"))) {
				accessor = tmp;
			}
		}
		if (accessor == null) {
			accessor = new StructImpl(Struct.TYPE_LINKED);
			children.appendEL(accessor);
		}

		updateSecurityFileAccess(accessor, dsm.getCustomFileAccess(), dsm.getAccess(SecurityManager.TYPE_FILE));

		accessor.setEL("id", id);
		accessor.setEL("setting", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_SETTING)));
		accessor.setEL("file", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_FILE)));
		accessor.setEL("direct_java_access", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS)));
		accessor.setEL("mail", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_MAIL)));
		accessor.setEL("datasource", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_DATASOURCE)));
		accessor.setEL("mapping", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_MAPPING)));
		accessor.setEL("custom_tag", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_CUSTOM_TAG)));
		accessor.setEL("cfx_setting", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_CFX_SETTING)));
		accessor.setEL("cfx_usage", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_CFX_USAGE)));
		accessor.setEL("debugging", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_DEBUGGING)));
		accessor.setEL("cache", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManagerImpl.TYPE_CACHE)));
		accessor.setEL("gateway", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManagerImpl.TYPE_GATEWAY)));
		accessor.setEL("orm", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManagerImpl.TYPE_ORM)));

		accessor.setEL("tag_execute", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_EXECUTE)));
		accessor.setEL("tag_import", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_IMPORT)));
		accessor.setEL("tag_object", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_OBJECT)));
		accessor.setEL("tag_registry", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_REGISTRY)));

	}

	/**
	 * remove security manager matching given id
	 * 
	 * @param id
	 * @throws PageException
	 */
	public void removeSecurityManager(Password password, String id) throws PageException {
		checkWriteAccess();
		((ConfigServerImpl) ConfigWebUtil.getConfigServer(config, password)).removeSecurityManager(id);

		Array children = ConfigWebUtil.getAsArray("security", "accessor", root);
		Key[] keys = children.keys();
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			String n = ConfigWebUtil.getAsString("id", tmp, "");
			if (id.equals(n)) {
				children.removeEL(key);
			}
		}
	}

	/**
	 * run update from cfml engine
	 * 
	 * @throws PageException
	 */
	public void runUpdate(Password password) throws PageException {
		checkWriteAccess();
		ConfigServerImpl cs = (ConfigServerImpl) ConfigWebUtil.getConfigServer(config, password);
		CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();

		synchronized (factory) {
			try {
				cleanUp(factory);
				factory.update(cs.getPassword(), cs.getIdentification());
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}

	}

	/**
	 * run update from cfml engine
	 * 
	 * @throws PageException
	 */
	public void removeLatestUpdate(Password password) throws PageException {
		_removeUpdate(password, true);
	}

	public void removeUpdate(Password password) throws PageException {
		_removeUpdate(password, false);
	}

	private void _removeUpdate(Password password, boolean onlyLatest) throws PageException {
		checkWriteAccess();

		ConfigServerImpl cs = (ConfigServerImpl) ConfigWebUtil.getConfigServer(config, password);

		try {
			CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();

			if (onlyLatest) {
				factory.removeLatestUpdate(cs.getPassword());
			}
			else factory.removeUpdate(cs.getPassword());

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public void changeVersionTo(Version version, Password password, IdentificationWeb id) throws PageException {
		checkWriteAccess();
		ConfigServerImpl cs = (ConfigServerImpl) ConfigWebUtil.getConfigServer(config, password);

		Log logger = cs.getLog("deploy");

		try {
			CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();
			cleanUp(factory);
			// do we have the core file?
			final File patchDir = factory.getPatchDirectory();
			File localPath = new File(version.toString() + ".lco");

			if (!localPath.isFile()) {
				localPath = null;
				Version v;
				final File[] patches = patchDir.listFiles(new ExtensionFilter(new String[] { ".lco" }));
				for (final File patch: patches) {
					v = CFMLEngineFactory.toVersion(patch.getName(), null);
					// not a valid file get deleted
					if (v == null) {
						patch.delete();
					}
					else {
						if (v.equals(version)) { // match!
							localPath = patch;
						}
						// delete newer files
						else if (OSGiUtil.isNewerThan(v, version)) {
							patch.delete();
						}
					}
				}
			}

			// download patch
			if (localPath == null) {

				downloadCore(factory, version, id);
			}

			logger.log(Log.LEVEL_INFO, "Update-Engine", "Installing Lucee version [" + version + "] (previous version was [" + cs.getEngine().getInfo().getVersion() + "])");

			factory.restart(password);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void cleanUp(CFMLEngineFactory factory) throws IOException {
		final File patchDir = factory.getPatchDirectory();
		final File[] patches = patchDir.listFiles(new ExtensionFilter(new String[] { ".lco" }));
		for (final File patch: patches) {
			if (!IsZipFile.invoke(patch)) patch.delete();
		}
	}

	private File downloadCore(CFMLEngineFactory factory, Version version, Identification id) throws IOException {
		final URL updateProvider = factory.getUpdateLocation();

		final URL updateUrl = new URL(updateProvider,
				"/rest/update/provider/download/" + version.toString() + (id != null ? id.toQueryString() : "") + (id == null ? "?" : "&") + "allowRedirect=true");
		// log.debug("Admin", "download "+version+" from " + updateUrl);
		// System. out.println(updateUrl);

		// local resource
		final File patchDir = factory.getPatchDirectory();
		final File newLucee = new File(patchDir, version + (".lco"));

		int code;
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) updateUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.connect();
			code = conn.getResponseCode();
		}
		catch (final UnknownHostException e) {
			// log.error("Admin", e);
			throw e;
		}

		// the update provider is not providing a download for this
		if (code != 200) {

			int count = 0;
			final int max = 5;
			// the update provider can also provide a different (final) location for this
			while ((code == 301 || code == 302) && (count++ < max)) {
				String location = conn.getHeaderField("Location");
				// just in case we check invalid names
				if (location == null) location = conn.getHeaderField("location");
				if (location == null) location = conn.getHeaderField("LOCATION");
				if (location == null) break;
				// System. out.println("download redirected:" + location); // MUST remove

				conn.disconnect();
				URL url = new URL(location);
				try {
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(10000);
					conn.connect();
					code = conn.getResponseCode();
				}
				catch (final UnknownHostException e) {
					// log.error("Admin", e);
					throw e;
				}
			}

			// no download available!
			if (code != 200) {
				final String msg = "Lucee Core download failed (response status:" + code + ") the core for version [" + version.toString() + "] from " + updateUrl
						+ ", please download it manually and copy to [" + patchDir + "]";
				// log.debug("Admin", msg);
				conn.disconnect();
				throw new IOException(msg);
			}
		}

		// copy it to local directory
		if (newLucee.createNewFile()) {
			IOUtil.copy((InputStream) conn.getContent(), new FileOutputStream(newLucee), false, true);
			conn.disconnect();

			// when it is a loader extract the core from it
			File tmp = CFMLEngineFactory.extractCoreIfLoader(newLucee);
			if (tmp != null) {
				// System .out.println("extract core from loader"); // MUST remove
				// log.debug("Admin", "extract core from loader");

				newLucee.delete();
				tmp.renameTo(newLucee);
				tmp.delete();
				// System. out.println("exist?" + newLucee.exists()); // MUST remove

			}
		}
		else {
			conn.disconnect();
			// log.debug("Admin","File for new Version already exists, won't copy new one");
			return null;
		}
		return newLucee;
	}

	private String getCoreExtension() {
		return "lco";
	}

	private boolean isNewerThan(int left, int right) {
		return left > right;
	}

	/*
	 * private Resource getPatchDirectory(CFMLEngine engine) throws IOException { //File
	 * f=engine.getCFMLEngineFactory().getResourceRoot(); Resource res =
	 * ResourcesImpl.getFileResourceProvider().getResource(engine.getCFMLEngineFactory().getResourceRoot
	 * ().getAbsolutePath()); Resource pd = res.getRealResource("patches"); if(!pd.exists())pd.mkdirs();
	 * return pd; }
	 */

	/**
	 * run update from cfml engine
	 * 
	 * @throws PageException
	 */
	public void restart(Password password) throws PageException {
		checkWriteAccess();
		ConfigServerImpl cs = (ConfigServerImpl) ConfigWebUtil.getConfigServer(config, password);
		CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();

		synchronized (factory) {
			try {
				cleanUp(factory);
				factory.restart(cs.getPassword());
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
	}

	public void restart(ConfigServerImpl cs) throws PageException {
		CFMLEngineFactory factory = cs.getCFMLEngine().getCFMLEngineFactory();

		synchronized (factory) {
			try {
				Method m = factory.getClass().getDeclaredMethod("_restart", new Class[0]);
				if (m == null) throw new ApplicationException("Cannot restart Lucee.");
				m.setAccessible(true);
				m.invoke(factory, new Object[0]);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
	}

	public void updateWebCharset(String charset) throws PageException {
		checkWriteAccess();

		if (StringUtil.isEmpty(charset)) {
			if (config instanceof ConfigWeb) rem(root, "webCharset");
			else root.setEL("webCharset", "UTF-8");
		}
		else {
			charset = checkCharset(charset);
			root.setEL("webCharset", charset);
		}
	}

	public void updateResourceCharset(String charset) throws PageException {
		checkWriteAccess();

		if (StringUtil.isEmpty(charset)) {
			rem(root, "resourceCharset");
		}
		else {
			charset = checkCharset(charset);
			root.setEL("resourceCharset", charset);
		}
	}

	public void updateTemplateCharset(String charset) throws PageException {

		checkWriteAccess();

		if (StringUtil.isEmpty(charset, true)) {
			rem(root, "templateCharset");
		}
		else {
			charset = checkCharset(charset);
			root.setEL("templateCharset", charset);
		}
	}

	private String checkCharset(String charset) throws PageException {
		charset = charset.trim();
		if ("system".equalsIgnoreCase(charset)) charset = SystemUtil.getCharset().name();
		else if ("jre".equalsIgnoreCase(charset)) charset = SystemUtil.getCharset().name();
		else if ("os".equalsIgnoreCase(charset)) charset = SystemUtil.getCharset().name();

		// check access
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) {
			throw new SecurityException("Access Denied to update regional setting");
		}

		// check encoding
		try {
			IOUtil.checkEncoding(charset);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return charset;
	}

	private Resource getStoragDir(Config config) {
		Resource storageDir = config.getConfigDir().getRealResource("storage");
		if (!storageDir.exists()) storageDir.mkdirs();
		return storageDir;
	}

	public void storageSet(Config config, String key, Object value) throws ConverterException, IOException, SecurityException {
		checkWriteAccess();
		Resource storageDir = getStoragDir(config);
		Resource storage = storageDir.getRealResource(key + ".wddx");

		WDDXConverter converter = new WDDXConverter(config.getTimeZone(), true, true);
		String wddx = converter.serialize(value);
		IOUtil.write(storage, wddx, "UTF-8", false);
	}

	public Object storageGet(Config config, String key) throws ConverterException, IOException, SecurityException {
		checkReadAccess();
		Resource storageDir = getStoragDir(config);
		Resource storage = storageDir.getRealResource(key + ".wddx");
		if (!storage.exists()) throw new IOException("There is no storage named [" + key + "]");
		WDDXConverter converter = new WDDXConverter(config.getTimeZone(), true, true);
		return converter.deserialize(IOUtil.toString(storage, "UTF-8"), true);
	}

	public void updateCustomTagDeepSearch(boolean customTagDeepSearch) throws SecurityException {
		checkWriteAccess();
		if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw new SecurityException("Access Denied to update custom tag setting");

		root.setEL("customTagDeepSearch", Caster.toString(customTagDeepSearch));
	}

	public void resetId() throws PageException {
		checkWriteAccess();
		Resource res = config.getConfigDir().getRealResource("id");
		try {
			if (res.exists()) res.remove(false);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}

	}

	public void updateCustomTagLocalSearch(boolean customTagLocalSearch) throws SecurityException {
		checkWriteAccess();
		if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw new SecurityException("Access Denied to update custom tag setting");
		root.setEL("customTagLocalSearch", Caster.toString(customTagLocalSearch));
	}

	public void updateCustomTagExtensions(String extensions) throws PageException {
		checkWriteAccess();
		if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw new SecurityException("Access Denied to update custom tag setting");

		// check
		Array arr = ListUtil.listToArrayRemoveEmpty(extensions, ',');
		ListUtil.trimItems(arr);
		// throw new ApplicationException("you must define at least one extension");

		// update charset
		root.setEL("customTagExtensions", ListUtil.arrayToList(arr, ","));
	}

	public void updateRemoteClient(String label, String url, String type, String securityKey, String usage, String adminPassword, String serverUsername, String serverPassword,
			String proxyServer, String proxyUsername, String proxyPassword, String proxyPort) throws PageException {
		checkWriteAccess();

		// SNSN

		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE);
		if (!hasAccess) throw new SecurityException("Access Denied to update remote client settings");

		Struct clients = _getRootElement("remoteClients");

		if (StringUtil.isEmpty(url)) throw new ExpressionException("[url] cannot be empty");
		if (StringUtil.isEmpty(securityKey)) throw new ExpressionException("[securityKey] cannot be empty");
		if (StringUtil.isEmpty(adminPassword)) throw new ExpressionException("[adminPassword] can not be empty");
		url = url.trim();
		securityKey = securityKey.trim();
		adminPassword = adminPassword.trim();

		Array children = ConfigWebUtil.getAsArray("remoteClient", clients);

		// Update
		for (int i = 1; i <= children.size(); i++) {
			Struct el = Caster.toStruct(children.get(i, null), null);
			if (el == null) continue;

			String _url = ConfigWebUtil.getAsString("url", el, "");
			if (_url != null && _url.equalsIgnoreCase(url)) {
				el.setEL("label", label);
				el.setEL("type", type);
				el.setEL("usage", usage);
				el.setEL("serverUsername", serverUsername);
				el.setEL("proxyServer", proxyServer);
				el.setEL("proxyUsername", proxyUsername);
				el.setEL("proxyPort", proxyPort);
				el.setEL("securityKey", ConfigWebUtil.encrypt(securityKey));
				el.setEL("adminPassword", ConfigWebUtil.encrypt(adminPassword));
				el.setEL("serverPassword", ConfigWebUtil.encrypt(serverPassword));
				el.setEL("proxyPassword", ConfigWebUtil.encrypt(proxyPassword));
				return;
			}
		}

		// Insert
		Struct el = new StructImpl(Struct.TYPE_LINKED);

		el.setEL("label", label);
		el.setEL("url", url);
		el.setEL("type", type);
		el.setEL("usage", usage);
		el.setEL("serverUsername", serverUsername);
		el.setEL("proxyServer", proxyServer);
		el.setEL("proxyUsername", proxyUsername);
		el.setEL("proxyPort", proxyPort);
		el.setEL("securityKey", ConfigWebUtil.encrypt(securityKey));
		el.setEL("adminPassword", ConfigWebUtil.encrypt(adminPassword));
		el.setEL("serverPassword", ConfigWebUtil.encrypt(serverPassword));
		el.setEL("proxyPassword", ConfigWebUtil.encrypt(proxyPassword));
		children.appendEL(el);
	}

	public void updateUpdateAdminMode(String mode, boolean merge, boolean keep) throws PageException {
		checkWriteAccess();

		if (config.getAdminMode() == ConfigImpl.ADMINMODE_MULTI) {
			// copy the content from all web cfconfig into the server cfconfig
			if (merge) {
				ConfigWeb[] webs = ((ConfigServer) config).getConfigWebs();
				for (ConfigWeb cw: webs) {
					try {
						merge(root, ConfigWebFactory.loadDocument(cw.getConfigFile()));
					}
					catch (IOException e) {
						throw Caster.toPageException(e);
					}
				}
			}

			// delete all the server configs
			if (!keep) {
				ConfigWeb[] webs = ((ConfigServer) config).getConfigWebs();
				for (ConfigWeb cw: webs) {
					cw.getConfigFile().delete();
				}
			}
		}

		if (StringUtil.isEmpty(mode, true)) return;
		mode = mode.trim();

		if (mode.equalsIgnoreCase("m") || mode.equalsIgnoreCase("multi") || mode.equalsIgnoreCase("multiple")) mode = "multi";
		else if (mode.equalsIgnoreCase("s") || mode.equalsIgnoreCase("single")) mode = "single";
		else throw new ApplicationException("invalid mode [" + mode + "], valid modes are [single,multi]");

		root.setEL(KeyConstants._mode, mode);
	}

	private void merge(Collection server, Collection web) {
		Key[] keys = web.keys();
		Object exServer, exWeb;
		for (Key key: keys) {
			exServer = server.get(key, null);

			if (exServer instanceof Collection) {
				exWeb = web.get(key, null);
				if (exWeb instanceof Collection) merge((Collection) exServer, (Collection) exWeb);
			}
			else {
				if (server instanceof Array) ((Array) server).appendEL(web.get(key, null)); // TODO can create a duplicate
				else server.setEL(key, web.get(key, null));
			}
		}
	}

	public void updateMonitor(ClassDefinition cd, String type, String name, boolean logEnabled) throws PageException {
		checkWriteAccess();
		_updateMonitor(cd, type, name, logEnabled);
	}

	void _updateMonitor(ClassDefinition cd, String type, String name, boolean logEnabled) throws PageException {
		stopMonitor(ConfigWebUtil.toMonitorType(type, Monitor.TYPE_INTERVAL), name);

		Struct children = ConfigWebUtil.getAsStruct("monitors", root);
		Key[] keys = children.keys();
		Struct monitor = null;
		// Update
		for (Key key: keys) {
			Struct el = Caster.toStruct(children.get(key, null), null);
			if (el == null) continue;

			String _name = key.getString();
			if (_name != null && _name.equalsIgnoreCase(name)) {
				monitor = el;
				break;
			}
		}

		// Insert
		if (monitor == null) {
			monitor = new StructImpl(Struct.TYPE_LINKED);
			children.setEL(name, monitor);
		}
		setClass(monitor, null, "", cd);
		monitor.setEL("type", type);
		monitor.setEL("name", name);
		monitor.setEL("log", Caster.toString(logEnabled));
	}

	private void stopMonitor(int type, String name) {
		Monitor monitor = null;
		try {
			if (Monitor.TYPE_ACTION == type) monitor = config.getActionMonitor(name);
			else if (Monitor.TYPE_REQUEST == type) monitor = config.getRequestMonitor(name);
			else if (Monitor.TYPE_REQUEST == type) monitor = config.getIntervallMonitor(name);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		IOUtil.closeEL(monitor);
	}

	static void removeCacheHandler(ConfigPro config, String id, boolean reload) throws IOException, PageException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		admin._removeCacheHandler(id);
		admin._store();
		if (reload) admin._reload();
	}

	private void _removeCache(ClassDefinition cd) {
		Array children = ConfigWebUtil.getAsArray("cacheClasses", root);
		Key[] keys = children.keys();
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			Struct el = Caster.toStruct(children.get(key, null), null);
			if (el == null) continue;

			String _class = ConfigWebUtil.getAsString("virtual", el, null);
			if (_class != null && _class.equalsIgnoreCase(cd.getClassName())) {
				children.removeEL(key);
				break;
			}
		}
	}

	private void _removeCacheHandler(String id) {
		Struct handlers = ConfigWebUtil.getAsStruct("cacheHandlers", root);
		Key[] keys = handlers.keys();
		for (Key key: keys) {
			String _id = key.getString();
			if (_id.equalsIgnoreCase(id)) {
				Struct el = Caster.toStruct(handlers.get(key, null), null);
				if (el == null) continue;
				handlers.removeEL(key);
				break;
			}
		}
	}

	public void updateCacheHandler(String id, ClassDefinition cd) throws PageException {
		checkWriteAccess();
		_updateCacheHandler(id, cd);
	}

	private void _updateCache(ClassDefinition cd) throws PageException {
		Array children = ConfigWebUtil.getAsArray("cacheClasses", root);
		Struct ch = null;
		// Update
		for (int i = 1; i <= children.size(); i++) {
			Struct el = Caster.toStruct(children.get(i, null), null);
			if (el == null) continue;

			String _class = ConfigWebUtil.getAsString("class", el, null);
			if (_class != null && _class.equalsIgnoreCase(cd.getClassName())) {
				ch = el;
				break;
			}
		}

		// Insert
		if (ch == null) {
			ch = new StructImpl(Struct.TYPE_LINKED);
			children.appendEL(ch);
		}
		setClass(ch, null, "", cd);
	}

	private void _updateCacheHandler(String id, ClassDefinition cd) throws PageException {
		Struct handlers = ConfigWebUtil.getAsStruct("cacheHandlers", root);
		Iterator<Entry<Key, Object>> it = handlers.entryIterator();
		Struct ch = null;
		// Update
		Entry<Key, Object> entry;
		while (it.hasNext()) {
			entry = it.next();
			String _id = entry.getKey().getString();
			if (_id != null && _id.equalsIgnoreCase(id)) {
				Struct el = Caster.toStruct(entry.getValue(), null);
				if (el == null) continue;
				ch = el;
				break;
			}
		}

		// Insert
		if (ch == null) {
			ch = new StructImpl(Struct.TYPE_LINKED);
			handlers.setEL(id, ch);
		}
		setClass(ch, null, "", cd);
	}

	public void updateExecutionLog(ClassDefinition cd, Struct args, boolean enabled) throws PageException {
		Struct el = _getRootElement("executionLog");
		setClass(el, null, "", cd);
		el.setEL("arguments", toStringCSSStyle(args));
		el.setEL("enabled", Caster.toString(enabled));
	}

	public void removeMonitor(String type, String name) throws SecurityException {
		checkWriteAccess();
		_removeMonitor(type, name);
	}

	void _removeMonitor(String type, String name) {

		stopMonitor(ConfigWebUtil.toMonitorType(type, Monitor.TYPE_INTERVAL), name);

		Array children = ConfigWebUtil.getAsArray("monitors", root);
		Key[] keys = children.keys();
		for (Key key: keys) {
			String _name = key.getString();
			if (_name != null && _name.equalsIgnoreCase(name)) {
				children.removeEL(key);
			}
		}
	}

	public void removeCacheHandler(String id) throws PageException {
		Struct handlers = ConfigWebUtil.getAsStruct("cacheHandlers", root);
		Key[] keys = handlers.keys();
		for (Key key: keys) {
			String _id = key.getString();
			if (_id.equalsIgnoreCase(id)) {
				Struct el = Caster.toStruct(handlers.get(key, null), null);
				if (el == null) continue;
				handlers.removeEL(key);
				break;
			}
		}
	}

	public void updateExtensionInfo(boolean enabled) {
		root.setEL("extensionEnabled", enabled);
	}

	public void updateRHExtensionProvider(String strUrl) throws MalformedURLException, PageException {
		updateExtensionProvider(strUrl);
	}

	public void updateExtensionProvider(String strUrl) throws MalformedURLException, PageException {
		Array children = ConfigWebUtil.getAsArray("extensionProviders", root);
		strUrl = strUrl.trim();

		URL _url = HTTPUtil.toURL(strUrl, HTTPUtil.ENCODED_NO);
		strUrl = _url.toExternalForm();

		// Update
		String url;
		for (int i = 1; i <= children.size(); i++) {
			url = Caster.toString(children.get(i, null), null);
			if (url == null) continue;

			if (url.trim().equalsIgnoreCase(strUrl)) {
				return;
			}
		}

		// Insert
		children.prepend(strUrl);
	}

	public void removeExtensionProvider(String strUrl) {
		Array children = ConfigWebUtil.getAsArray("extensionProviders", root);
		Key[] keys = children.keys();
		strUrl = strUrl.trim();
		String url;
		for (int i = keys.length - 1; i >= 0; i--) {
			Key key = keys[i];
			url = Caster.toString(children.get(key, null), null);
			if (url == null) continue;

			if (url.trim().equalsIgnoreCase(strUrl)) {
				children.removeEL(key);
				return;
			}
		}
	}

	public void removeRHExtensionProvider(String strUrl) {
		removeExtensionProvider(strUrl);
	}

	private String createUid(PageContext pc, String provider, String id) throws PageException {
		if (Decision.isUUId(id)) {
			return Hash.invoke(pc.getConfig(), id, null, null, 1);
		}
		return Hash.invoke(pc.getConfig(), provider + id, null, null, 1);
	}

	private void setExtensionAttrs(Struct el, Extension extension) {
		el.setEL("version", extension.getVersion());

		el.setEL("config", extension.getStrConfig());
		// el.setEL("config",new ScriptConverter().serialize(extension.getConfig()));

		el.setEL("category", extension.getCategory());
		el.setEL("description", extension.getDescription());
		el.setEL("image", extension.getImage());
		el.setEL("label", extension.getLabel());
		el.setEL("name", extension.getName());

		el.setEL("author", extension.getAuthor());
		el.setEL("type", extension.getType());
		el.setEL("codename", extension.getCodename());
		el.setEL("video", extension.getVideo());
		el.setEL("support", extension.getSupport());
		el.setEL("documentation", extension.getDocumentation());
		el.setEL("forum", extension.getForum());
		el.setEL("mailinglist", extension.getMailinglist());
		el.setEL("network", extension.getNetwork());
		el.setEL("created", Caster.toString(extension.getCreated(), null));

	}

	public void resetORMSetting() throws SecurityException {
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM);

		if (!hasAccess) throw new SecurityException("Access Denied to update ORM Settings");

		Struct orm = _getRootElement("orm");
		if (root.containsKey("orm")) rem(root, "orm");
	}

	public void updateORMSetting(ORMConfiguration oc) throws SecurityException {
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM);

		if (!hasAccess) throw new SecurityException("Access Denied to update ORM Settings");

		Struct orm = _getRootElement("orm");
		orm.setEL("autogenmap", Caster.toString(oc.autogenmap(), "true"));
		orm.setEL("eventHandler", Caster.toString(oc.eventHandler(), ""));
		orm.setEL("eventHandling", Caster.toString(oc.eventHandling(), "false"));
		orm.setEL("namingStrategy", Caster.toString(oc.namingStrategy(), ""));
		orm.setEL("flushAtRequestEnd", Caster.toString(oc.flushAtRequestEnd(), "true"));
		orm.setEL("cacheProvider", Caster.toString(oc.getCacheProvider(), ""));
		orm.setEL("cacheConfig", Caster.toString(oc.getCacheConfig(), "true"));
		orm.setEL("catalog", Caster.toString(oc.getCatalog(), ""));
		orm.setEL("dbCreate", ORMConfigurationImpl.dbCreateAsString(oc.getDbCreate()));
		orm.setEL("dialect", Caster.toString(oc.getDialect(), ""));
		orm.setEL("schema", Caster.toString(oc.getSchema(), ""));
		orm.setEL("logSql", Caster.toString(oc.logSQL(), "false"));
		orm.setEL("saveMapping", Caster.toString(oc.saveMapping(), "false"));
		orm.setEL("secondaryCacheEnable", Caster.toString(oc.secondaryCacheEnabled(), "false"));
		orm.setEL("useDbForMapping", Caster.toString(oc.useDBForMapping(), "true"));
		orm.setEL("ormConfig", Caster.toString(oc.getOrmConfig(), ""));
		orm.setEL("sqlCcript", Caster.toString(oc.getSqlScript(), "true"));

		if (oc.isDefaultCfcLocation()) {
			rem(orm, "cfcLocation");
		}
		else {
			Resource[] locations = oc.getCfcLocations();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < locations.length; i++) {
				if (i != 0) sb.append(",");
				sb.append(locations[i].getAbsolutePath());
			}
			orm.setEL("cfcLocation", sb.toString());
		}

		orm.setEL("sqlScript", Caster.toString(oc.getSqlScript(), "true"));

	}

	public void removeRHExtension(String id) throws PageException {
		checkWriteAccess();
		if (StringUtil.isEmpty(id, true)) return;

		Array children = ConfigWebUtil.getAsArray("extensions", root);
		int[] keys = children.intKeys();
		Struct child;
		RHExtension rhe;
		int key;
		for (int i = keys.length - 1; i >= 0; i--) {
			key = keys[i];
			child = Caster.toStruct(children.get(key, null), null);
			if (child == null) continue;

			try {
				rhe = new RHExtension(config, Caster.toString(child.get(KeyConstants._id), null), Caster.toString(child.get(KeyConstants._version), null), null, false);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				continue;
			}

			if (id.equalsIgnoreCase(rhe.getId()) || id.equalsIgnoreCase(rhe.getSymbolicName())) {
				removeRHExtension(config, rhe, null, true);
				children.removeEL(key);
			}
		}
	}

	public void removeExtension(String provider, String id) throws PageException {
		removeRHExtension(id);
	}

	public static void updateArchive(ConfigPro config, Resource arc, boolean reload) throws PageException {
		try {
			ConfigAdmin admin = new ConfigAdmin(config, null);
			admin.updateArchive(config, arc);
			admin._store();
			if (reload) admin._reload();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	public static void updateCore(ConfigServerImpl config, Resource core, boolean reload) throws PageException {
		try {
			// get patches directory
			CFMLEngine engine = ConfigWebUtil.getEngine(config);
			ConfigServerImpl cs = config;
			Version v;
			v = CFMLEngineFactory.toVersion(core.getName(), null);
			Log logger = cs.getLog("deploy");
			File f = engine.getCFMLEngineFactory().getResourceRoot();
			Resource res = ResourcesImpl.getFileResourceProvider().getResource(f.getAbsolutePath());
			Resource pd = res.getRealResource("patches");
			if (!pd.exists()) pd.mkdirs();
			Resource pf = pd.getRealResource(core.getName());

			// move to patches directory
			core.moveTo(pf);
			core = pf;
			logger.log(Log.LEVEL_INFO, "Update-Engine", "Installing Lucee [" + v + "] (previous version was [" + cs.getEngine().getInfo().getVersion() + "] )");
			//
			ConfigAdmin admin = new ConfigAdmin(config, null);
			admin.restart(config);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(config.getDeployDirectory(), core);
			throw Caster.toPageException(t);
		}
	}

	public void updateArchive(Config config, Resource archive) throws PageException {
		Log logger = config.getLog("deploy");
		String type = null, virtual = null, name = null;
		boolean readOnly, topLevel, hidden, physicalFirst;
		short inspect;
		int listMode, listType;
		InputStream is = null;
		ZipFile file = null;
		try {
			file = new ZipFile(FileWrapper.toFile(archive));
			ZipEntry entry = file.getEntry("META-INF/MANIFEST.MF");

			// no manifest
			if (entry == null) {
				DeployHandler.moveToFailedFolder(config.getDeployDirectory(), archive);
				throw new ApplicationException("Cannot deploy " + Constants.NAME + " Archive [" + archive + "], file is to old, the file does not have a MANIFEST.");
			}

			is = file.getInputStream(entry);
			Manifest manifest = new Manifest(is);
			Attributes attr = manifest.getMainAttributes();

			// id = unwrap(attr.getValue("mapping-id"));
			type = StringUtil.unwrap(attr.getValue("mapping-type"));
			virtual = StringUtil.unwrap(attr.getValue("mapping-virtual-path"));
			name = ListUtil.trim(virtual, "/");
			readOnly = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-readonly")), false);
			topLevel = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-top-level")), false);

			listMode = ConfigWebUtil.toListenerMode(StringUtil.unwrap(attr.getValue("mapping-listener-mode")), -1);
			listType = ConfigWebUtil.toListenerType(StringUtil.unwrap(attr.getValue("mapping-listener-type")), -1);

			inspect = ConfigWebUtil.inspectTemplate(StringUtil.unwrap(attr.getValue("mapping-inspect")), Config.INSPECT_UNDEFINED);
			if (inspect == Config.INSPECT_UNDEFINED) {
				Boolean trusted = Caster.toBoolean(StringUtil.unwrap(attr.getValue("mapping-trusted")), null);
				if (trusted != null) {
					if (trusted.booleanValue()) inspect = Config.INSPECT_NEVER;
					else inspect = Config.INSPECT_ALWAYS;
				}
			}

			hidden = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-hidden")), false);
			physicalFirst = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-physical-first")), false);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(config.getDeployDirectory(), archive);
			throw Caster.toPageException(t);
		}

		finally {
			try {
				IOUtil.close(is);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
			ZipUtil.close(file);
		}
		try {
			Resource trgDir = config.getConfigDir().getRealResource("archives").getRealResource(type).getRealResource(name);
			Resource trgFile = trgDir.getRealResource(archive.getName());
			trgDir.mkdirs();

			// delete existing files

			ResourceUtil.deleteContent(trgDir, null);
			ResourceUtil.moveTo(archive, trgFile, true);
			logger.log(Log.LEVEL_INFO, "archive", "Add " + type + " mapping [" + virtual + "] with archive [" + trgFile.getAbsolutePath() + "]");
			if ("regular".equalsIgnoreCase(type)) _updateMapping(virtual, null, trgFile.getAbsolutePath(), "archive", inspect, topLevel, listMode, listType, readOnly);
			else if ("cfc".equalsIgnoreCase(type)) _updateComponentMapping(virtual, null, trgFile.getAbsolutePath(), "archive", inspect);
			else if ("ct".equalsIgnoreCase(type)) _updateCustomTag(virtual, null, trgFile.getAbsolutePath(), "archive", inspect);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(config.getDeployDirectory(), archive);
			throw Caster.toPageException(t);
		}
	}

	public static void _updateRHExtension(ConfigPro config, Resource ext, boolean reload, boolean force) throws PageException {
		try {
			ConfigAdmin admin = new ConfigAdmin(config, null);
			admin.updateRHExtension(config, ext, reload, force);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public void updateRHExtension(Config config, Resource ext, boolean reload, boolean force) throws PageException {
		RHExtension rhext;
		try {
			rhext = new RHExtension(config, ext, true);
			rhext.validate();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(ext.getParentResource(), ext);
			throw Caster.toPageException(t);
		}
		updateRHExtension(config, rhext, reload, force);
	}

	public void updateRHExtension(Config config, RHExtension rhext, boolean reload, boolean force) throws PageException {

		try {
			if (!force && ConfigAdmin.hasRHExtensions((ConfigPro) config, rhext.toExtensionDefinition()) != null) {
				throw new ApplicationException("the extension " + rhext.getName() + " (id: " + rhext.getId() + ") in version " + rhext.getVersion() + " is already installed");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		ConfigPro ci = (ConfigPro) config;
		Log logger = ci.getLog("deploy");
		String type = ci instanceof ConfigWeb ? "web" : "server";
		// load already installed previous version and uninstall the parts no longer needed
		RHExtension existingRH = getRHExtension(ci, rhext.getId(), null);
		if (existingRH != null) {
			// same version
			if (existingRH.getVersion().compareTo(rhext.getVersion()) == 0) {
				removeRHExtension(config, existingRH, rhext, false);
			}
			else removeRHExtension(config, existingRH, rhext, true);

		}
		// INSTALL
		try {

			// boolean clearTags=false,clearFunction=false;
			boolean reloadNecessary = false;

			// store to xml
			BundleDefinition[] existing = _updateExtension(ci, rhext);
			// _storeAndReload();
			// this must happen after "store"
			cleanBundles(rhext, ci, existing);// clean after populating the new ones
			// ConfigWebAdmin.updateRHExtension(ci,rhext);

			ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(rhext.getExtensionFile().getInputStream()));
			ZipEntry entry;
			String path;
			String fileName;
			while ((entry = zis.getNextEntry()) != null) {
				path = entry.getName();
				fileName = fileName(entry);
				// jars
				if (!entry.isDirectory() && (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles")
						|| startsWith(path, type, "bundle") || startsWith(path, type, "lib") || startsWith(path, type, "libs")) && (StringUtil.endsWithIgnoreCase(path, ".jar"))) {

					Object obj = ConfigAdmin.installBundle(config, zis, fileName, rhext.getVersion(), false, false);
					// jar is not a bundle, only a regular jar
					if (!(obj instanceof BundleFile)) {
						Resource tmp = (Resource) obj;
						Resource tmpJar = tmp.getParentResource().getRealResource(ListUtil.last(path, "\\/"));
						tmp.moveTo(tmpJar);
						ConfigAdmin.updateJar(config, tmpJar, false);
					}
				}

				// flds
				if (!entry.isDirectory() && startsWith(path, type, "flds") && (StringUtil.endsWithIgnoreCase(path, ".fld") || StringUtil.endsWithIgnoreCase(path, ".fldx"))) {
					logger.log(Log.LEVEL_INFO, "extension", "Deploy fld [" + fileName + "]");
					updateFLD(zis, fileName, false);
					reloadNecessary = true;
				}
				// tlds
				if (!entry.isDirectory() && startsWith(path, type, "tlds") && (StringUtil.endsWithIgnoreCase(path, ".tld") || StringUtil.endsWithIgnoreCase(path, ".tldx"))) {
					logger.log(Log.LEVEL_INFO, "extension", "Deploy tld/tldx [" + fileName + "]");
					updateTLD(zis, fileName, false);
					reloadNecessary = true;
				}

				// tags
				if (!entry.isDirectory() && startsWith(path, type, "tags")) {
					String sub = subFolder(entry);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy tag [" + sub + "]");
					updateTag(zis, sub, false);
					// clearTags=true;
					reloadNecessary = true;
				}

				// functions
				if (!entry.isDirectory() && startsWith(path, type, "functions")) {
					String sub = subFolder(entry);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy function [" + sub + "]");
					updateFunction(zis, sub, false);
					// clearFunction=true;
					reloadNecessary = true;
				}

				// mappings
				if (!entry.isDirectory() && (startsWith(path, type, "archives") || startsWith(path, type, "mappings"))) {
					String sub = subFolder(entry);
					logger.log(Log.LEVEL_INFO, "extension", "deploy mapping " + sub);
					updateArchive(zis, sub, false);
					reloadNecessary = true;
					// clearFunction=true;
				}

				// event-gateway
				if (!entry.isDirectory() && (startsWith(path, type, "event-gateways") || startsWith(path, type, "eventGateways"))
						&& (StringUtil.endsWithIgnoreCase(path, "." + Constants.getCFMLComponentExtension())
								|| StringUtil.endsWithIgnoreCase(path, "." + Constants.getLuceeComponentExtension()))) {
					String sub = subFolder(entry);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy event-gateway [" + sub + "]");
					updateEventGateway(zis, sub, false);
				}

				// context
				String realpath;
				if (!entry.isDirectory() && startsWith(path, type, "context") && !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(8);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy context [" + realpath + "]");
					updateContext(zis, realpath, false, false);
				}
				// web contextS
				boolean first;
				if (!entry.isDirectory() && ((first = startsWith(path, type, "webcontexts")) || startsWith(path, type, "web.contexts"))
						&& !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(first ? 12 : 13);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy webcontext [" + realpath + "]");
					updateWebContexts(zis, realpath, false, false);
				}
				// applications
				if (!entry.isDirectory() && (startsWith(path, type, "applications") || startsWith(path, type, "web.applications") || startsWith(path, type, "web"))
						&& !StringUtil.startsWith(fileName(entry), '.')) {
					int index;
					if (startsWith(path, type, "applications")) index = 13;
					else if (startsWith(path, type, "web.applications")) index = 17;
					else index = 4; // web

					realpath = path.substring(index);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy application [" + realpath + "]");
					updateApplication(zis, realpath, false);
				}
				// configs
				if (!entry.isDirectory() && (startsWith(path, type, "config")) && !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(7);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy config [" + realpath + "]");
					updateConfigs(zis, realpath, false, false);
				}
				// components
				if (!entry.isDirectory() && (startsWith(path, type, "components")) && !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(11);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy component [" + realpath + "]");
					updateComponent(zis, realpath, false, false);
				}

				// plugins
				if (!entry.isDirectory() && (startsWith(path, type, "plugins")) && !StringUtil.startsWith(fileName(entry), '.')) {
					realpath = path.substring(8);
					logger.log(Log.LEVEL_INFO, "extension", "Deploy plugin [" + realpath + "]");
					updatePlugin(zis, realpath, false);
				}

				zis.closeEntry();
			}
			////////////////////////////////////////////

			// load the bundles
			if (rhext.getStartBundles()) {
				rhext.deployBundles(ci);
				BundleInfo[] bfs = rhext.getBundles();
				if (bfs != null) {
					for (BundleInfo bf: bfs) {
						OSGiUtil.loadBundleFromLocal(bf.getSymbolicName(), bf.getVersion(), null, false, null);
					}
				}
			}

			// update cache
			if (!ArrayUtil.isEmpty(rhext.getCaches())) {
				Iterator<Map<String, String>> itl = rhext.getCaches().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_updateCache(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update cache [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update cache handler
			if (!ArrayUtil.isEmpty(rhext.getCacheHandlers())) {
				Iterator<Map<String, String>> itl = rhext.getCacheHandlers().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String _id = map.get("id");
					if (!StringUtil.isEmpty(_id) && cd != null && cd.hasClass()) {
						_updateCacheHandler(_id, cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update cache handler [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update Search
			if (!ArrayUtil.isEmpty(rhext.getSearchs())) {
				Iterator<Map<String, String>> itl = rhext.getSearchs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.hasClass()) {
						_updateSearchEngine(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update search engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update Resource
			if (!ArrayUtil.isEmpty(rhext.getResources())) {
				Iterator<Map<String, String>> itl = rhext.getResources().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String scheme = map.get("scheme");
					if (cd != null && cd.hasClass() && !StringUtil.isEmpty(scheme)) {
						Struct args = new StructImpl(Struct.TYPE_LINKED);
						copyButIgnoreClassDef(map, args);
						args.remove("scheme");
						_updateResourceProvider(scheme, cd, args);
						reloadNecessary = true;
					}
					logger.info("extension", "Update resource provider [" + scheme + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update orm
			if (!ArrayUtil.isEmpty(rhext.getOrms())) {
				Iterator<Map<String, String>> itl = rhext.getOrms().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);

					if (cd != null && cd.hasClass()) {
						_updateORMEngine(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update orm engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update webservice
			if (!ArrayUtil.isEmpty(rhext.getWebservices())) {
				Iterator<Map<String, String>> itl = rhext.getWebservices().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);

					if (cd != null && cd.hasClass()) {
						_updateWebserviceHandler(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update webservice handler [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update monitor
			if (!ArrayUtil.isEmpty(rhext.getMonitors())) {
				Iterator<Map<String, String>> itl = rhext.getMonitors().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.hasClass()) {
						_updateMonitorEnabled(true);
						_updateMonitor(cd, map.get("type"), map.get("name"), true);
						reloadNecessary = true;
					}
					logger.info("extension", "Update monitor engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update jdbc
			if (!ArrayUtil.isEmpty(rhext.getJdbcs())) {
				Iterator<Map<String, String>> itl = rhext.getJdbcs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String _label = map.get("label");
					String _id = map.get("id");
					if (cd != null && cd.isBundle()) {
						_updateJDBCDriver(_label, _id, cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update JDBC Driver [" + _label + ":" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update startup hook
			if (!ArrayUtil.isEmpty(rhext.getStartupHooks())) {
				Iterator<Map<String, String>> itl = rhext.getStartupHooks().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_updateStartupHook(cd);
						reloadNecessary = true;
					}
					logger.info("extension", "Update Startup Hook [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// update mapping
			if (!ArrayUtil.isEmpty(rhext.getMappings())) {
				Iterator<Map<String, String>> itl = rhext.getMappings().iterator();
				Map<String, String> map;

				String virtual, physical, archive, primary;
				short inspect;
				int lmode, ltype;
				boolean toplevel, readonly;
				while (itl.hasNext()) {
					map = itl.next();
					virtual = map.get("virtual");
					physical = map.get("physical");
					archive = map.get("archive");
					primary = map.get("primary");

					inspect = ConfigWebUtil.inspectTemplate(map.get("inspect"), Config.INSPECT_UNDEFINED);
					lmode = ConfigWebUtil.toListenerMode(map.get("listener-mode"), -1);
					ltype = ConfigWebUtil.toListenerType(map.get("listener-type"), -1);

					toplevel = Caster.toBooleanValue(map.get("toplevel"), false);
					readonly = Caster.toBooleanValue(map.get("readonly"), false);

					_updateMapping(virtual, physical, archive, primary, inspect, toplevel, lmode, ltype, readonly);
					reloadNecessary = true;

					logger.info("extension", "Update Mapping [" + virtual + "]");
				}
			}

			// update event-gateway-instance

			if (!ArrayUtil.isEmpty(rhext.getEventGatewayInstances())) {
				Iterator<Map<String, Object>> itl = rhext.getEventGatewayInstances().iterator();
				Map<String, Object> map;
				while (itl.hasNext()) {
					map = itl.next();
					// id
					String id = Caster.toString(map.get("id"), null);
					// class
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					// component path
					String cfcPath = Caster.toString(map.get("cfcPath"), null);
					if (StringUtil.isEmpty(cfcPath)) cfcPath = Caster.toString(map.get("componentPath"), null);
					// listener component path
					String listenerCfcPath = Caster.toString(map.get("listenerCFCPath"), null);
					if (StringUtil.isEmpty(listenerCfcPath)) listenerCfcPath = Caster.toString(map.get("listenerComponentPath"), null);
					// startup mode
					String strStartupMode = Caster.toString(map.get("startupMode"), "automatic");
					int startupMode = GatewayEntryImpl.toStartup(strStartupMode, GatewayEntryImpl.STARTUP_MODE_AUTOMATIC);
					// read only
					boolean readOnly = Caster.toBooleanValue(map.get("readOnly"), false);
					// custom
					Struct custom = Caster.toStruct(map.get("custom"), null);
					/*
					 * print.e("::::::::::::::::::::::::::::::::::::::::::"); print.e("id:"+id); print.e("cd:"+cd);
					 * print.e("cfc:"+cfcPath); print.e("listener:"+listenerCfcPath);
					 * print.e("startupMode:"+startupMode); print.e(custom);
					 */

					if (!StringUtil.isEmpty(id) && (!StringUtil.isEmpty(cfcPath) || (cd != null && cd.hasClass()))) {
						_updateGatewayEntry(id, cd, cfcPath, listenerCfcPath, startupMode, custom, readOnly);
					}

					logger.info("extension", "Update event gateway entry [" + id + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]");
				}
			}

			// reload
			// if(reloadNecessary){
			reloadNecessary = true;
			if (reload && reloadNecessary) _storeAndReload();
			else _store();
			// }
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			DeployHandler.moveToFailedFolder(rhext.getExtensionFile().getParentResource(), rhext.getExtensionFile());
			try {
				ConfigAdmin.removeRHExtensions((ConfigPro) config, config.getLog("deploy"), new String[] { rhext.getId() }, false);
			}
			catch (Throwable t2) {
				ExceptionUtil.rethrowIfNecessary(t2);
			}
			throw Caster.toPageException(t);
		}
	}

	private void copyButIgnoreClassDef(Map<String, String> src, Struct trg) {
		Iterator<Entry<String, String>> it = src.entrySet().iterator();
		Entry<String, String> e;
		String name;
		while (it.hasNext()) {
			e = it.next();
			name = e.getKey();
			if ("class".equals(name) || "bundle-name".equals(name) || "bundlename".equals(name) || "bundleName".equals(name) || "bundle-version".equals(name)
					|| "bundleversion".equals(name) || "bundleVersion".equals(name))
				continue;
			trg.setEL(name, e.getValue());
		}
	}

	/**
	 * removes an installed extension from the system
	 * 
	 * @param config
	 * @param rhe extension to remove
	 * @param replacementRH the extension that will replace this extension, so do not remove parts
	 *            defined in this extension.
	 * @throws PageException
	 */
	private void removeRHExtension(Config config, RHExtension rhe, RHExtension replacementRH, boolean deleteExtension) throws PageException {
		ConfigPro ci = ((ConfigPro) config);
		Log logger = ci.getLog("deploy");

		// MUST check replacementRH everywhere

		try {
			// remove the bundles
			BundleDefinition[] candidatesToRemove = OSGiUtil.toBundleDefinitions(rhe.getBundles(EMPTY));
			if (replacementRH != null) {
				// spare bundles used in the new extension as well
				Map<String, BundleDefinition> notRemove = toMap(OSGiUtil.toBundleDefinitions(replacementRH.getBundles(EMPTY)));
				List<BundleDefinition> tmp = new ArrayList<OSGiUtil.BundleDefinition>();
				String key;
				for (int i = 0; i < candidatesToRemove.length; i++) {
					key = candidatesToRemove[i].getName() + "|" + candidatesToRemove[i].getVersionAsString();
					if (notRemove.containsKey(key)) continue;
					tmp.add(candidatesToRemove[i]);
				}
				candidatesToRemove = tmp.toArray(new BundleDefinition[tmp.size()]);
			}
			ConfigAdmin.cleanBundles(rhe, ci, candidatesToRemove);

			// FLD
			removeFLDs(logger, rhe.getFlds()); // MUST check if others use one of this fld

			// TLD
			removeTLDs(logger, rhe.getTlds()); // MUST check if others use one of this tld

			// Tag
			removeTags(logger, rhe.getTags());

			// Functions
			removeFunctions(logger, rhe.getFunctions());

			// Event Gateway
			removeEventGateways(logger, rhe.getEventGateways());

			// context
			removeContext(config, false, logger, rhe.getContexts()); // MUST check if others use one of this

			// web contextS
			removeWebContexts(config, false, logger, rhe.getWebContexts()); // MUST check if others use one of this

			// applications
			removeApplications(config, logger, rhe.getApplications()); // MUST check if others use one of this

			// plugins
			removePlugins(config, logger, rhe.getPlugins()); // MUST check if others use one of this

			// remove cache handler
			if (!ArrayUtil.isEmpty(rhe.getCacheHandlers())) {
				Iterator<Map<String, String>> itl = rhe.getCacheHandlers().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String _id = map.get("id");

					if (!StringUtil.isEmpty(_id) && cd != null && cd.hasClass()) {
						_removeCacheHandler(_id);
						// reload=true;
					}
					logger.info("extension", "Remove cache handler [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove cache
			if (!ArrayUtil.isEmpty(rhe.getCaches())) {
				Iterator<Map<String, String>> itl = rhe.getCaches().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_removeCache(cd);
						// reload=true;
					}
					logger.info("extension", "Remove cache handler [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove Search
			if (!ArrayUtil.isEmpty(rhe.getSearchs())) {
				Iterator<Map<String, String>> itl = rhe.getSearchs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.hasClass()) {
						_removeSearchEngine();
						// reload=true;
					}
					logger.info("extension", "Remove search engine [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove resource
			if (!ArrayUtil.isEmpty(rhe.getResources())) {
				Iterator<Map<String, String>> itl = rhe.getResources().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					String scheme = map.get("scheme");
					if (cd != null && cd.hasClass()) {
						_removeResourceProvider(scheme);
					}
					logger.info("extension", "Remove resource [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove orm
			if (!ArrayUtil.isEmpty(rhe.getOrms())) {
				Iterator<Map<String, String>> itl = rhe.getOrms().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);

					if (cd != null && cd.hasClass()) {
						_removeORMEngine();
						// reload=true;
					}
					logger.info("extension", "Remove orm engine [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove webservice
			if (!ArrayUtil.isEmpty(rhe.getWebservices())) {
				Iterator<Map<String, String>> itl = rhe.getWebservices().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);

					if (cd != null && cd.hasClass()) {
						_removeWebserviceHandler();
						// reload=true;
					}
					logger.info("extension", "Remove webservice handler [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove monitor
			if (!ArrayUtil.isEmpty(rhe.getMonitors())) {
				Iterator<Map<String, String>> itl = rhe.getMonitors().iterator();
				Map<String, String> map;
				String name;
				while (itl.hasNext()) {
					map = itl.next();

					// ClassDefinition cd = RHExtension.toClassDefinition(config,map);

					// if(cd.hasClass()) {
					_removeMonitor(map.get("type"), name = map.get("name"));
					// reload=true;
					// }
					logger.info("extension", "Remove monitor [" + name + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove jdbc
			if (!ArrayUtil.isEmpty(rhe.getJdbcs())) {
				Iterator<Map<String, String>> itl = rhe.getJdbcs().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_removeJDBCDriver(cd);
					}
					logger.info("extension", "Remove JDBC Driver [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove startup hook
			if (!ArrayUtil.isEmpty(rhe.getStartupHooks())) {
				Iterator<Map<String, String>> itl = rhe.getStartupHooks().iterator();
				Map<String, String> map;
				while (itl.hasNext()) {
					map = itl.next();
					ClassDefinition cd = RHExtension.toClassDefinition(config, map, null);
					if (cd != null && cd.isBundle()) {
						_removeStartupHook(cd);
					}
					logger.info("extension", "Remove Startup Hook [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]");
				}
			}

			// remove mapping
			if (!ArrayUtil.isEmpty(rhe.getMappings())) {
				Iterator<Map<String, String>> itl = rhe.getMappings().iterator();
				Map<String, String> map;
				String virtual;
				while (itl.hasNext()) {
					map = itl.next();
					virtual = map.get("virtual");
					_removeMapping(virtual);
					logger.info("extension", "remove Mapping [" + virtual + "]");
				}
			}

			// remove event-gateway-instance
			if (!ArrayUtil.isEmpty(rhe.getEventGatewayInstances())) {
				Iterator<Map<String, Object>> itl = rhe.getEventGatewayInstances().iterator();
				Map<String, Object> map;
				String id;
				while (itl.hasNext()) {
					map = itl.next();
					id = Caster.toString(map.get("id"), null);
					if (!StringUtil.isEmpty(id)) {
						_removeGatewayEntry(id);
						logger.info("extension", "remove event gateway entry [" + id + "]");
					}
				}
			}

			// Loop Files
			ZipInputStream zis = new ZipInputStream(IOUtil.toBufferedInputStream(rhe.getExtensionFile().getInputStream()));
			String type = ci instanceof ConfigWeb ? "web" : "server";
			try {
				ZipEntry entry;
				String path;
				String fileName;
				Resource tmp;
				while ((entry = zis.getNextEntry()) != null) {
					path = entry.getName();
					fileName = fileName(entry);

					// archives
					if (!entry.isDirectory() && (startsWith(path, type, "archives") || startsWith(path, type, "mappings"))) {
						String sub = subFolder(entry);
						logger.log(Log.LEVEL_INFO, "extension", "Remove archive [" + sub + "] registered as a mapping");
						tmp = SystemUtil.getTempFile(".lar", false);
						IOUtil.copy(zis, tmp, false);
						removeArchive(tmp);
					}
					zis.closeEntry();
				}
			}
			finally {
				IOUtil.close(zis);
			}

			// now we can delete the extension
			if (deleteExtension) rhe.getExtensionFile().delete();

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			// failed to uninstall, so we install it again
			try {
				updateRHExtension(config, rhe.getExtensionFile(), true, true);
				// RHExtension.install(config, rhe.getExtensionFile());
			}
			catch (Throwable t2) {
				ExceptionUtil.rethrowIfNecessary(t2);
			}
			throw Caster.toPageException(t);
		}

	}

	private Map<String, BundleDefinition> toMap(BundleDefinition[] bundleDefinitions) {
		Map<String, BundleDefinition> rtn = new HashMap<String, OSGiUtil.BundleDefinition>();
		for (int i = 0; i < bundleDefinitions.length; i++) {
			rtn.put(bundleDefinitions[i].getName() + "|" + bundleDefinitions[i].getVersionAsString(), bundleDefinitions[i]);
		}
		return rtn;
	}

	private static boolean startsWith(String path, String type, String name) {
		return StringUtil.startsWithIgnoreCase(path, name + "/") || StringUtil.startsWithIgnoreCase(path, type + "/" + name + "/");
	}

	private static String fileName(ZipEntry entry) {
		String name = entry.getName();
		int index = name.lastIndexOf('/');
		if (index == -1) return name;
		return name.substring(index + 1);
	}

	private static String subFolder(ZipEntry entry) {
		String name = entry.getName();
		int index = name.indexOf('/');
		if (index == -1) return name;
		return name.substring(index + 1);
	}

	public void verifyExtensionProvider(String strUrl) throws PageException {
		HTTPResponse method = null;
		try {
			URL url = HTTPUtil.toURL(strUrl + "?wsdl", HTTPUtil.ENCODED_AUTO);
			method = HTTPEngine.get(url, null, null, 2000, true, null, null, null, null);
		}
		catch (MalformedURLException e) {
			throw new ApplicationException("Url definition [" + strUrl + "] is invalid");
		}
		catch (IOException e) {
			throw new ApplicationException("Can't invoke [" + strUrl + "]", e.getMessage());
		}

		if (method.getStatusCode() != 200) {
			int code = method.getStatusCode();
			String text = method.getStatusText();
			String msg = code + " " + text;
			throw new HTTPException(msg, null, code, text, method.getURL());
		}
		// Object o =
		CreateObject.doWebService(null, strUrl + "?wsdl");
		HTTPEngine.closeEL(method);
	}

	public void updateTLD(Resource resTld) throws IOException {
		updateLD(config.getTldFile(), resTld);
	}

	public void updateFLD(Resource resFld) throws IOException {
		updateLD(config.getFldFile(), resFld);
	}

	private void updateLD(Resource dir, Resource res) throws IOException {
		if (!dir.exists()) dir.createDirectory(true);

		Resource file = dir.getRealResource(res.getName());
		if (file.length() != res.length()) {
			ResourceUtil.copy(res, file);
		}
	}

	void updateTLD(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getTldFile(), is, name, closeStream);
	}

	void updateFLD(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getFldFile(), is, name, closeStream);
	}

	void updateTag(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getDefaultTagMapping().getPhysical(), is, name, closeStream);
	}

	void updateFunction(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getDefaultFunctionMapping().getPhysical(), is, name, closeStream);
	}

	void updateEventGateway(InputStream is, String name, boolean closeStream) throws IOException {
		write(config.getEventGatewayDirectory(), is, name, closeStream);
	}

	void updateArchive(InputStream is, String name, boolean closeStream) throws IOException, PageException {
		Resource res = write(SystemUtil.getTempDirectory(), is, name, closeStream);
		// Resource res = write(DeployHandler.getDeployDirectory(config),is,name,closeStream);
		updateArchive(config, res);
	}

	private static Resource write(Resource dir, InputStream is, String name, boolean closeStream) throws IOException {
		if (!dir.exists()) dir.createDirectory(true);
		Resource file = dir.getRealResource(name);
		Resource p = file.getParentResource();
		if (!p.exists()) p.createDirectory(true);
		IOUtil.copy(is, file.getOutputStream(), closeStream, true);
		return file;
	}

	public void removeTLD(String name) throws IOException {
		removeFromDirectory(config.getTldFile(), name);
	}

	public void removeTLDs(Log logger, String[] names) throws IOException {
		if (ArrayUtil.isEmpty(names)) return;
		Resource file = config.getTldFile();
		for (int i = 0; i < names.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove TLD file " + names[i]);
			removeFromDirectory(file, names[i]);
		}
	}

	public void removeEventGateways(Log logger, String[] relpath) throws IOException {
		if (ArrayUtil.isEmpty(relpath)) return;
		Resource dir = config.getEventGatewayDirectory();// get Event gateway Directory
		for (int i = 0; i < relpath.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove Event Gateway " + relpath[i]);
			removeFromDirectory(dir, relpath[i]);
		}
	}

	public void removeFunctions(Log logger, String[] relpath) throws IOException {
		if (ArrayUtil.isEmpty(relpath)) return;
		Resource file = config.getDefaultFunctionMapping().getPhysical();
		for (int i = 0; i < relpath.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove Function " + relpath[i]);
			removeFromDirectory(file, relpath[i]);
		}
	}

	public void removeArchive(Resource archive) throws IOException, PageException {
		Log logger = config.getLog("deploy");
		String virtual = null, type = null;
		InputStream is = null;
		ZipFile file = null;
		try {
			file = new ZipFile(FileWrapper.toFile(archive));
			ZipEntry entry = file.getEntry("META-INF/MANIFEST.MF");

			// no manifest
			if (entry == null) throw new ApplicationException("Cannot remove " + Constants.NAME + " Archive [" + archive + "], file is to old, the file does not have a MANIFEST.");

			is = file.getInputStream(entry);
			Manifest manifest = new Manifest(is);
			Attributes attr = manifest.getMainAttributes();
			virtual = StringUtil.unwrap(attr.getValue("mapping-virtual-path"));
			type = StringUtil.unwrap(attr.getValue("mapping-type"));
			logger.info("archive", "Remove " + type + " mapping [" + virtual + "]");

			if ("regular".equalsIgnoreCase(type)) removeMapping(virtual);
			else if ("cfc".equalsIgnoreCase(type)) removeComponentMapping(virtual);
			else if ("ct".equalsIgnoreCase(type)) removeCustomTag(virtual);
			else throw new ApplicationException("Invalid type [" + type + "], valid types are [regular, cfc, ct]");
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		finally {
			IOUtil.close(is);
			ZipUtil.close(file);
		}
	}

	public void removeTags(Log logger, String[] relpath) throws IOException {
		if (ArrayUtil.isEmpty(relpath)) return;
		Resource file = config.getDefaultTagMapping().getPhysical();
		for (int i = 0; i < relpath.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove Tag [" + relpath[i] + "]");
			removeFromDirectory(file, relpath[i]);
		}
	}

	public void removeFLDs(Log logger, String[] names) throws IOException {
		if (ArrayUtil.isEmpty(names)) return;
		Resource file = config.getFldFile();
		for (int i = 0; i < names.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove FLD file [" + names[i] + "]");
			removeFromDirectory(file, names[i]);
		}
	}

	public void removeFLD(String name) throws IOException {
		removeFromDirectory(config.getFldFile(), name);
	}

	private void removeFromDirectory(Resource dir, String relpath) throws IOException {
		if (dir.isDirectory()) {
			Resource file = dir.getRealResource(relpath);
			if (file.isFile()) file.remove(false);
		}
	}

	public void updateRemoteClientUsage(String code, String displayname) {
		Struct usage = config.getRemoteClientUsage();
		usage.setEL(code, displayname);

		Struct extensions = _getRootElement("remoteClients");
		extensions.setEL("usage", toStringURLStyle(usage));

	}

	public void updateVideoExecuterClass(ClassDefinition cd) throws PageException {

		if (cd.getClassName() == null) cd = new ClassDefinitionImpl(VideoExecuterNotSupported.class.getName());

		Struct app = _getRootElement("video");
		setClass(app, VideoExecuter.class, "videoExecuter", cd);
	}

	public void updateAdminSyncClass(ClassDefinition cd) throws PageException {

		if (cd.getClassName() == null) cd = new ClassDefinitionImpl(AdminSyncNotSupported.class.getName());

		setClass(root, AdminSync.class, "adminSync", cd);
	}

	public void removeRemoteClientUsage(String code) {
		Struct usage = config.getRemoteClientUsage();
		usage.removeEL(KeyImpl.getInstance(code));

		Struct extensions = _getRootElement("remoteClients");
		extensions.setEL("usage", toStringURLStyle(usage));

	}

	class MyResourceNameFilter implements ResourceNameFilter {
		private String name;

		public MyResourceNameFilter(String name) {
			this.name = name;
		}

		@Override
		public boolean accept(Resource parent, String name) {
			return name.equals(this.name);
		}
	}

	public void updateSerial(String serial) throws PageException {

		checkWriteAccess();
		if (!(config instanceof ConfigServer)) {
			throw new SecurityException("Can't change serial number from this context, access is denied");
		}

		if (!StringUtil.isEmpty(serial)) {
			serial = serial.trim();
			if (!new SerialNumber(serial).isValid(serial)) throw new SecurityException("Serial number is invalid");
			root.setEL("serialNumber", serial);
		}
		else {
			try {
				rem(root, "serialNumber");
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		try {
			rem(root, "serial");
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public boolean updateLabel(String hash, String label) {
		// check
		if (StringUtil.isEmpty(hash, true)) return false;
		if (StringUtil.isEmpty(label, true)) return false;

		hash = hash.trim();
		label = label.trim();

		Array children = ConfigWebUtil.getAsArray("labels", "label", root);

		// Update
		for (int i = 1; i <= children.size(); i++) {
			Struct tmp = Caster.toStruct(children.get(i, null), null);
			if (tmp == null) continue;

			String h = ConfigWebUtil.getAsString("id", tmp, null);
			if (h != null) {
				if (h.equals(hash)) {
					if (label.equals(tmp.get("name", null))) return false;
					tmp.setEL("name", label);
					return true;
				}
			}
		}

		// Insert
		Struct el = new StructImpl(Struct.TYPE_LINKED);
		children.appendEL(el);
		el.setEL("id", hash);
		el.setEL("name", label);

		return true;
	}

	public void updateDebugSetting(int maxLogs) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);
		if (!hasAccess) throw new SecurityException("Access denied to change debugging settings");

		if (maxLogs == -1) rem(root, "debuggingMaxRecordsLogged");
		else root.setEL("debuggingMaxRecordsLogged", maxLogs);
	}

	public void updateDebugEntry(String type, String iprange, String label, String path, String fullname, Struct custom) throws SecurityException, IOException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);
		if (!hasAccess) throw new SecurityException("Access denied to change debugging settings");

		// leave this, this method throws an exception when ip range is not valid
		IPRange.getInstance(iprange);

		String id = MD5.getDigestAsString(label.trim().toLowerCase());
		type = type.trim();
		iprange = iprange.trim();
		label = label.trim();

		Array children = ConfigWebUtil.getAsArray("debugTemplates", root);

		// Update
		Struct el = null;
		for (int i = 1; i <= children.size(); i++) {
			Struct tmp = Caster.toStruct(children.get(i, null), null);
			if (tmp == null) continue;

			String _id = ConfigWebUtil.getAsString("id", tmp, null);
			if (_id != null) {
				if (_id.equals(id)) {
					el = tmp;
					break;
				}
			}
		}

		// Insert
		if (el == null) {
			el = new StructImpl(Struct.TYPE_LINKED);
			children.appendEL(el);
			el.setEL("id", id);
		}

		el.setEL("type", type);
		el.setEL("iprange", iprange);
		el.setEL("label", label);
		el.setEL("path", path);
		el.setEL("fullname", fullname);
		el.setEL("custom", toStringURLStyle(custom));
	}

	public void removeDebugEntry(String id) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING);
		if (!hasAccess) throw new SecurityException("Access denied to change debugging settings");

		Array children = ConfigWebUtil.getAsArray("debugTemplates", root);
		Key[] keys = children.keys();
		String _id;
		if (children.size() > 0) {
			for (int i = keys.length - 1; i >= 0; i--) {
				Key key = keys[i];
				Struct el = Caster.toStruct(children.get(key, null), null);
				if (el == null) continue;

				_id = ConfigWebUtil.getAsString("id", el, null);
				if (_id != null && _id.equalsIgnoreCase(id)) {
					children.removeEL(key);
				}
			}
		}
	}

	public void updateLoginSettings(boolean captcha, boolean rememberMe, int delay) {
		root.setEL("loginCaptcha", captcha);
		root.setEL("loginRememberme", rememberMe);
		root.setEL("loginDelay", delay);
	}

	public void updateLogSettings(String name, int level, ClassDefinition appenderCD, Struct appenderArgs, ClassDefinition layoutCD, Struct layoutArgs) throws PageException {
		checkWriteAccess();
		// TODO
		// boolean hasAccess=ConfigWebUtil.hasAccess(config,SecurityManagerImpl.TYPE_GATEWAY);
		// if(!hasAccess) throw new SecurityException("no access to update gateway entry");

		// check parameters
		name = name.trim();
		if (StringUtil.isEmpty(name)) throw new ApplicationException("Log file name cannot be empty");

		if (appenderCD == null || !appenderCD.hasClass()) throw new ExpressionException("Appender class is required");
		if (layoutCD == null || !layoutCD.hasClass()) throw new ExpressionException("Layout class is required");

		try {
			appenderCD.getClazz();
			layoutCD.getClazz();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		Struct children = ConfigWebUtil.getAsStruct("loggers", root);
		Key[] keys = children.keys();
		// Update
		Struct el = null;
		for (Key key: keys) {
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			String n = key.getString();
			if (name.equalsIgnoreCase(n)) {
				el = tmp;
				break;
			}
		}
		// Insert
		if (el == null) {
			el = new StructImpl(Struct.TYPE_LINKED);
			children.setEL(name, el);
		}

		el.setEL("level", LogUtil.levelToString(level, ""));
		setClass(el, null, "appender", appenderCD);
		el.setEL("appenderArguments", toStringCSSStyle(appenderArgs));
		setClass(el, null, "layout", layoutCD);
		el.setEL("layoutArguments", toStringCSSStyle(layoutArgs));

		if (el.containsKey("appender")) rem(el, "appender");
		if (el.containsKey("layout")) rem(el, "layout");
	}

	public void updateCompilerSettings(Boolean dotNotationUpperCase, Boolean suppressWSBeforeArg, Boolean nullSupport, Boolean handleUnQuotedAttrValueAsString,
			Integer externalizeStringGTE) throws PageException {

		// Struct element = _getRootElement("compiler");

		checkWriteAccess();
		if (dotNotationUpperCase == null) {
			if (root.containsKey("dotNotationUpperCase")) rem(root, "dotNotationUpperCase");
		}
		else {
			root.setEL("dotNotationUpperCase", dotNotationUpperCase);
		}

		if (suppressWSBeforeArg == null) {
			if (root.containsKey("suppressWhitespaceBeforeArgument")) rem(root, "suppressWhitespaceBeforeArgument");
		}
		else {
			root.setEL("suppressWhitespaceBeforeArgument", suppressWSBeforeArg);
		}

		// full null support
		if (nullSupport == null) {
			if (root.containsKey("nullSupport")) rem(root, "nullSupport");
		}
		else {
			root.setEL("nullSupport", Caster.toString(nullSupport));
		}

		// externalize-string-gte
		if (externalizeStringGTE == null) {
			if (root.containsKey("externalizeStringGte")) rem(root, "externalizeStringGte");
		}
		else {
			root.setEL("externalizeStringGte", Caster.toString(externalizeStringGTE));
		}

		// handle Unquoted Attribute Values As String
		if (handleUnQuotedAttrValueAsString == null) {
			if (root.containsKey("handleUnquotedAttributeValueAsString")) rem(root, "handleUnquotedAttributeValueAsString");
		}
		else {
			root.setEL("handleUnquotedAttributeValueAsString", Caster.toString(handleUnQuotedAttrValueAsString));
		}

	}

	Resource[] updateWebContexts(InputStream is, String realpath, boolean closeStream, boolean store) throws PageException, IOException, BundleException, ConverterException {
		List<Resource> filesDeployed = new ArrayList<Resource>();

		if (config instanceof ConfigWeb) {
			ConfigAdmin._updateContextClassic(config, is, realpath, closeStream, filesDeployed);
		}
		else ConfigAdmin._updateWebContexts(config, is, realpath, closeStream, filesDeployed, store);

		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void _updateWebContexts(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed, boolean store)
			throws PageException, IOException, BundleException, ConverterException {
		if (!(config instanceof ConfigServer)) throw new ApplicationException("Invalid context, you can only call this method from server context");
		ConfigServer cs = (ConfigServer) config;

		Resource wcd = cs.getConfigDir().getRealResource("web-context-deployment");
		Resource trg = wcd.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		if (store) _storeAndReload((ConfigPro) config);
	}

	Resource[] updateConfigs(InputStream is, String realpath, boolean closeStream, boolean store) throws PageException, IOException, BundleException, ConverterException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		_updateConfigs(config, is, realpath, closeStream, filesDeployed, store);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void _updateConfigs(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed, boolean store)
			throws PageException, IOException, BundleException, ConverterException {
		Resource configs = config.getConfigDir(); // MUST get that dynamically
		Resource trg = configs.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		if (store) _storeAndReload((ConfigPro) config);
	}

	Resource[] updateComponent(InputStream is, String realpath, boolean closeStream, boolean store) throws PageException, IOException, BundleException, ConverterException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		_updateComponent(config, is, realpath, closeStream, filesDeployed, store);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void _updateComponent(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed, boolean store)
			throws PageException, IOException, BundleException, ConverterException {
		Resource comps = config.getConfigDir().getRealResource("components"); // MUST get that dynamically
		Resource trg = comps.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		if (store) _storeAndReload((ConfigPro) config);
	}

	Resource[] updateContext(InputStream is, String realpath, boolean closeStream, boolean store) throws PageException, IOException, BundleException, ConverterException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		_updateContext(config, is, realpath, closeStream, filesDeployed, store);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void _updateContext(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed, boolean store)
			throws PageException, IOException, BundleException, ConverterException {
		Resource trg = config.getConfigDir().getRealResource("context").getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		if (store) _storeAndReload((ConfigPro) config);
	}

	@Deprecated
	static Resource[] updateContextClassic(ConfigPro config, InputStream is, String realpath, boolean closeStream)
			throws PageException, IOException, BundleException, ConverterException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		ConfigAdmin._updateContextClassic(config, is, realpath, closeStream, filesDeployed);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	@Deprecated
	private static void _updateContextClassic(Config config, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed)
			throws PageException, IOException, BundleException, ConverterException {
		if (config instanceof ConfigServer) {
			ConfigWeb[] webs = ((ConfigServer) config).getConfigWebs();
			if (webs.length == 0) return;
			if (webs.length == 1) {
				_updateContextClassic(webs[0], is, realpath, closeStream, filesDeployed);
				return;
			}
			try {
				byte[] barr = IOUtil.toBytes(is);
				for (int i = 0; i < webs.length; i++) {
					_updateContextClassic(webs[i], new ByteArrayInputStream(barr), realpath, true, filesDeployed);
				}
			}
			finally {
				if (closeStream) IOUtil.close(is);
			}
			return;
		}

		// ConfigWeb
		Resource trg = config.getConfigDir().getRealResource("context").getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
		_storeAndReload((ConfigPro) config);
	}

	public boolean removeConfigs(Config config, boolean store, String... realpathes) throws PageException, IOException, BundleException, ConverterException {
		if (ArrayUtil.isEmpty(realpathes)) return false;
		boolean force = false;
		for (int i = 0; i < realpathes.length; i++) {
			if (_removeConfigs(config, realpathes[i], store)) force = true;
		}
		return force;
	}

	private boolean _removeConfigs(Config config, String realpath, boolean _store) throws PageException, IOException, BundleException, ConverterException {

		Resource context = config.getConfigDir(); // MUST get dyn
		Resource trg = context.getRealResource(realpath);
		if (trg.exists()) {
			trg.remove(true);
			if (_store) ConfigAdmin._storeAndReload((ConfigPro) config);
			ResourceUtil.removeEmptyFolders(context, null);
			return true;
		}
		return false;
	}

	public boolean removeComponents(Config config, boolean store, String... realpathes) throws PageException, IOException, BundleException, ConverterException {
		if (ArrayUtil.isEmpty(realpathes)) return false;
		boolean force = false;
		for (int i = 0; i < realpathes.length; i++) {
			if (_removeComponent(config, realpathes[i], store)) force = true;
		}
		return force;
	}

	private boolean _removeComponent(Config config, String realpath, boolean _store) throws PageException, IOException, BundleException, ConverterException {

		Resource context = config.getConfigDir().getRealResource("components"); // MUST get dyn
		Resource trg = context.getRealResource(realpath);
		if (trg.exists()) {
			trg.remove(true);
			if (_store) ConfigAdmin._storeAndReload((ConfigPro) config);
			ResourceUtil.removeEmptyFolders(context, null);
			return true;
		}
		return false;
	}

	public boolean removeContext(Config config, boolean store, Log logger, String... realpathes) throws PageException, IOException, BundleException, ConverterException {
		if (ArrayUtil.isEmpty(realpathes)) return false;
		boolean force = false;
		for (int i = 0; i < realpathes.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "remove " + realpathes[i]);
			if (_removeContext(config, realpathes[i], store)) force = true;
		}
		return force;
	}

	private boolean _removeContext(Config config, String realpath, boolean _store) throws PageException, IOException, BundleException, ConverterException {

		Resource context = config.getConfigDir().getRealResource("context");
		Resource trg = context.getRealResource(realpath);
		if (trg.exists()) {
			trg.remove(true);
			if (_store) ConfigAdmin._storeAndReload((ConfigPro) config);
			ResourceUtil.removeEmptyFolders(context, null);
			return true;
		}
		return false;
	}

	public boolean removeWebContexts(Config config, boolean store, Log logger, String... realpathes) throws PageException, IOException, BundleException, ConverterException {
		if (ArrayUtil.isEmpty(realpathes)) return false;

		if (config instanceof ConfigWeb) {
			return removeContext(config, store, logger, realpathes);
		}

		boolean force = false;
		for (int i = 0; i < realpathes.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove Context [" + realpathes[i] + "]");
			if (_removeWebContexts(config, realpathes[i], store)) force = true;
		}
		return force;
	}

	private boolean _removeWebContexts(Config config, String realpath, boolean _store) throws PageException, IOException, BundleException, ConverterException {

		if (config instanceof ConfigServer) {
			ConfigServer cs = ((ConfigServer) config);

			// remove files from deploy folder
			Resource deploy = cs.getConfigDir().getRealResource("web-context-deployment");
			Resource trg = deploy.getRealResource(realpath);

			if (trg.exists()) {
				trg.remove(true);
				ResourceUtil.removeEmptyFolders(deploy, null);
			}

			// remove files from lucee web context
			boolean store = false;
			ConfigWeb[] webs = cs.getConfigWebs();
			for (int i = 0; i < webs.length; i++) {
				if (_removeContext(webs[i], realpath, _store)) {
					store = true;
				}
			}
			return store;
		}
		return false;
	}

	Resource[] updateApplication(InputStream is, String realpath, boolean closeStream) throws PageException, IOException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		Resource dir;
		// server context
		if (config instanceof ConfigServer) dir = config.getConfigDir().getRealResource("web-deployment");
		// if web context we simply deploy to that webcontext, that's all
		else dir = config.getRootDirectory();

		deployFilesFromStream(config, dir, is, realpath, closeStream, filesDeployed);

		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	private static void deployFilesFromStream(Config config, Resource root, InputStream is, String realpath, boolean closeStream, List<Resource> filesDeployed)
			throws PageException, IOException {
		// MUST this makes no sense at this point
		if (config instanceof ConfigServer) {
			ConfigWeb[] webs = ((ConfigServer) config).getConfigWebs();
			if (webs.length == 0) return;
			if (webs.length == 1) {
				deployFilesFromStream(webs[0], root, is, realpath, closeStream, filesDeployed);
				return;
			}
			try {
				byte[] barr = IOUtil.toBytes(is);
				for (int i = 0; i < webs.length; i++) {
					deployFilesFromStream(webs[i], root, new ByteArrayInputStream(barr), realpath, true, filesDeployed);
				}
			}
			finally {
				if (closeStream) IOUtil.close(is);
			}
			return;
		}

		// ConfigWeb
		Resource trg = root.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
		Resource p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		IOUtil.copy(is, trg.getOutputStream(false), closeStream, true);
		filesDeployed.add(trg);
	}

	private void removePlugins(Config config, Log logger, String[] realpathes) throws PageException, IOException {
		if (ArrayUtil.isEmpty(realpathes)) return;
		for (int i = 0; i < realpathes.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove plugin [" + realpathes[i] + "]");
			removeFiles(config, ((ConfigPro) config).getPluginDirectory(), realpathes[i]);
		}
	}

	private void removeApplications(Config config, Log logger, String[] realpathes) throws PageException, IOException {
		if (ArrayUtil.isEmpty(realpathes)) return;
		for (int i = 0; i < realpathes.length; i++) {
			logger.log(Log.LEVEL_INFO, "extension", "Remove application [" + realpathes[i] + "]");
			removeFiles(config, config.getRootDirectory(), realpathes[i]);
		}
	}

	private void removeFiles(Config config, Resource root, String realpath) throws PageException, IOException {
		if (config instanceof ConfigServer) {
			ConfigWeb[] webs = ((ConfigServer) config).getConfigWebs();
			for (int i = 0; i < webs.length; i++) {
				removeFiles(webs[i], root, realpath);
			}
			return;
		}

		// ConfigWeb
		Resource trg = root.getRealResource(realpath);
		if (trg.exists()) trg.remove(true);
	}

	public static void removeRHExtensions(ConfigPro config, Log log, String[] extensionIDs, boolean removePhysical)
			throws IOException, PageException, BundleException, ConverterException {
		ConfigAdmin admin = new ConfigAdmin(config, null);

		Map<String, BundleDefinition> oldMap = new HashMap<>();
		BundleDefinition[] bds;
		for (String extensionID: extensionIDs) {
			try {
				bds = admin._removeExtension(config, extensionID, removePhysical);
				if (bds != null) {
					for (BundleDefinition bd: bds) {
						if (bd == null) continue;// TODO why are they Null?
						oldMap.put(bd.toString(), bd);
					}
				}
			}
			catch (Exception e) {
				log.log(Log.LEVEL_ERROR, ConfigAdmin.class.getName(), e);
			}
		}

		admin._storeAndReload();

		if (!oldMap.isEmpty() && config instanceof ConfigServer) {
			ConfigServer cs = (ConfigServer) config;
			ConfigWeb[] webs = cs.getConfigWebs();
			for (int i = 0; i < webs.length; i++) {
				try {
					admin._storeAndReload((ConfigPro) webs[i]);
				}
				catch (Exception e) {
					log.log(Log.LEVEL_ERROR, ConfigAdmin.class.getName(), e);
				}
			}
		}
		cleanBundles(null, config, oldMap.values().toArray(new BundleDefinition[oldMap.size()])); // clean after populating the new ones

	}

	public BundleDefinition[] _removeExtension(ConfigPro config, String extensionID, boolean removePhysical)
			throws IOException, PageException, BundleException, ConverterException {
		if (!Decision.isUUId(extensionID)) throw new IOException("id [" + extensionID + "] is invalid, it has to be a UUID");

		Array children = ConfigWebUtil.getAsArray("extensions", root);
		int[] keys = children.intKeys();

		// Update
		Struct el;
		String id;
		String[] arr;
		boolean storeChildren = false;
		BundleDefinition[] bundles;
		Log log = config.getLog("deploy");
		int key;
		for (int i = keys.length - 1; i >= 0; i--) {
			key = keys[i];
			el = Caster.toStruct(children.get(key, null), null);
			if (el == null) continue;

			id = Caster.toString(el.get(KeyConstants._id), null);
			if (extensionID.equalsIgnoreCase(id)) {
				bundles = RHExtension.toBundleDefinitions(ConfigWebUtil.getAsString("bundles", el, null)); // get existing bundles before populate new ones

				// bundles
				arr = _removeExtensionCheckOtherUsage(children, el, "bundles");
				// removeBundles(arr,removePhysical);
				// flds
				arr = _removeExtensionCheckOtherUsage(children, el, "flds");
				removeFLDs(log, arr);
				// tlds
				arr = _removeExtensionCheckOtherUsage(children, el, "tlds");
				removeTLDs(log, arr);
				// contexts
				arr = _removeExtensionCheckOtherUsage(children, el, "contexts");
				storeChildren = removeContext(config, false, log, arr);

				// webcontexts
				arr = _removeExtensionCheckOtherUsage(children, el, "webcontexts");
				storeChildren = removeWebContexts(config, false, log, arr);

				// applications
				arr = _removeExtensionCheckOtherUsage(children, el, "applications");
				removeApplications(config, log, arr);

				// components
				arr = _removeExtensionCheckOtherUsage(children, el, "components");
				removeComponents(config, false, arr);

				// configs
				arr = _removeExtensionCheckOtherUsage(children, el, "config");
				removeConfigs(config, false, arr);

				// plugins
				arr = _removeExtensionCheckOtherUsage(children, el, "plugins");
				removePlugins(config, log, arr);
				children.removeEL(key);

				// remove files
				String version = Caster.toString(el.get(KeyConstants._version, null), null);
				Resource file = RHExtension.getMetaDataFile(config, id, version);
				if (file.isFile()) file.delete();
				file = RHExtension.getExtensionFile(config, id, version);
				if (file.isFile()) file.delete();

				return bundles;
			}
		}
		return null;
	}

	public static void cleanBundles(RHExtension rhe, ConfigPro config, BundleDefinition[] candiatesToRemove) throws BundleException, ApplicationException, IOException {
		if (ArrayUtil.isEmpty(candiatesToRemove)) return;

		BundleCollection coreBundles = ConfigWebUtil.getEngine(config).getBundleCollection();

		// core master
		_cleanBundles(candiatesToRemove, coreBundles.core.getSymbolicName(), coreBundles.core.getVersion());

		// core slaves
		Iterator<Bundle> it = coreBundles.getSlaves();
		Bundle b;
		while (it.hasNext()) {
			b = it.next();
			_cleanBundles(candiatesToRemove, b.getSymbolicName(), b.getVersion());
		}

		// all extension
		Iterator<RHExtension> itt = config.getAllRHExtensions().iterator();
		RHExtension _rhe;
		while (itt.hasNext()) {
			_rhe = itt.next();
			if (rhe != null && rhe.equals(_rhe)) continue;
			BundleInfo[] bundles = _rhe.getBundles(null);
			if (bundles != null) {
				for (BundleInfo bi: bundles) {
					_cleanBundles(candiatesToRemove, bi.getSymbolicName(), bi.getVersion());
				}
			}
		}

		// now we only have BundlesDefs in the array no longer used
		for (BundleDefinition ctr: candiatesToRemove) {
			if (ctr != null) OSGiUtil.removeLocalBundleSilently(ctr.getName(), ctr.getVersion(), null, true);
		}
	}

	private static void _cleanBundles(BundleDefinition[] candiatesToRemove, String name, Version version) {
		BundleDefinition bd;
		for (int i = 0; i < candiatesToRemove.length; i++) {
			bd = candiatesToRemove[i];

			if (bd != null && name.equalsIgnoreCase(bd.getName())) {
				if (version == null) {
					if (bd.getVersion() == null) candiatesToRemove[i] = null; // remove that from array
				}
				else if (bd.getVersion() != null && version.equals(bd.getVersion())) {
					candiatesToRemove[i] = null; // remove that from array
				}
			}
		}
	}

	private String[] _removeExtensionCheckOtherUsage(Array children, Struct curr, String type) {
		String currVal = ConfigWebUtil.getAsString(type, curr, null);
		if (StringUtil.isEmpty(currVal)) return null;
		Key[] keys = children.keys();
		String otherVal;
		Struct other;
		Set<String> currSet = ListUtil.toSet(ListUtil.trimItems(ListUtil.listToStringArray(currVal, ',')));
		String[] otherArr;
		Key key;
		for (int i = keys.length - 1; i >= 0; i--) {
			key = keys[i];
			Struct tmp = Caster.toStruct(children.get(key, null), null);
			if (tmp == null) continue;

			other = tmp;
			if (other == curr) continue;
			otherVal = ConfigWebUtil.getAsString(type, other, null);
			if (StringUtil.isEmpty(otherVal)) continue;
			otherArr = ListUtil.trimItems(ListUtil.listToStringArray(otherVal, ','));
			for (int y = 0; y < otherArr.length; y++) {
				currSet.remove(otherArr[y]);
			}
		}
		return currSet.toArray(new String[currSet.size()]);
	}

	/**
	 * 
	 * @param config
	 * @param ext
	 * @return the bundles used before when this was a update, if it is a new extension then null is
	 *         returned
	 * @throws IOException
	 * @throws BundleException
	 * @throws PageException
	 */
	public BundleDefinition[] _updateExtension(ConfigPro config, RHExtension ext) throws IOException, BundleException, PageException {
		if (!Decision.isUUId(ext.getId())) throw new IOException("id [" + ext.getId() + "] is invalid, it has to be a UUID");

		Array children = ConfigWebUtil.getAsArray("extensions", root);
		int[] keys = children.intKeys();
		int key;
		// Update
		Struct el;
		String id;
		BundleDefinition[] old;
		for (int i = keys.length - 1; i >= 0; i--) {
			key = keys[i];
			el = Caster.toStruct(children.get(key, null), null);
			if (el == null) continue;
			id = Caster.toString(el.get(KeyConstants._id), null);
			if (ext.getId().equalsIgnoreCase(id)) {
				old = RHExtension.toBundleDefinitions(ConfigWebUtil.getAsString("bundles", el, null)); // get existing bundles before populate new ones
				ext.populate(el, false);
				old = minus(old, OSGiUtil.toBundleDefinitions(ext.getBundles()));
				return old;
			}
		}

		// Insert
		el = new StructImpl(Struct.TYPE_LINKED);
		ext.populate(el, false);
		children.appendEL(el);
		return null;
	}

	private BundleDefinition[] minus(BundleDefinition[] oldBD, BundleDefinition[] newBD) {
		List<BundleDefinition> list = new ArrayList<>();
		boolean has;
		for (BundleDefinition o: oldBD) {
			has = false;
			for (BundleDefinition n: newBD) {
				if (o.equals(n)) {
					has = true;
					break;
				}
			}
			if (!has) list.add(o);
		}
		return list.toArray(new BundleDefinition[list.size()]);
	}

	private RHExtension getRHExtension(final ConfigPro config, final String id, final RHExtension defaultValue) {
		Array children = ConfigWebUtil.getAsArray("extensions", root);

		if (children != null) {
			int[] keys = children.intKeys();
			for (int i: keys) {

				Struct tmp = Caster.toStruct(children.get(i, null), null);
				if (tmp == null) continue;

				String _id = Caster.toString(tmp.get(KeyConstants._id, null), null);
				if (!id.equals(_id)) continue;

				try {
					return new RHExtension(config, _id, Caster.toString(tmp.get(KeyConstants._version), null), null, false);
				}
				catch (Exception e) {
					return defaultValue;
				}
			}
		}
		return defaultValue;
	}

	/**
	 * returns the version if the extension is available
	 * 
	 * @param config
	 * @param id
	 * @return
	 * @throws PageException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static RHExtension hasRHExtensions(ConfigPro config, ExtensionDefintion ed) throws PageException, IOException {
		ConfigAdmin admin = new ConfigAdmin(config, null);
		return admin._hasRHExtensions(config, ed);
	}

	private RHExtension _hasRHExtensions(ConfigPro config, ExtensionDefintion ed) throws PageException {

		Array children = ConfigWebUtil.getAsArray("extensions", root);
		int[] keys = children.intKeys();
		RHExtension tmp;
		try {
			String id, v;
			for (int key: keys) {
				Struct sct = Caster.toStruct(children.get(key, null), null);
				if (sct == null) continue;
				id = Caster.toString(sct.get(KeyConstants._id, null), null);
				v = Caster.toString(sct.get(KeyConstants._version, null), null);
				if (!RHExtension.isInstalled(config, id, v)) continue;

				if (ed.equals(new ExtensionDefintion(id, v))) return new RHExtension(config, id, v, null, false);
			}
			return null;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public void updateAuthKey(String key) throws PageException {
		checkWriteAccess();
		key = key.trim();

		// merge new key and existing
		ConfigServerImpl cs = (ConfigServerImpl) config;
		String[] keys = cs.getAuthenticationKeys();
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < keys.length; i++) {
			set.add(keys[i]);
		}
		set.add(key);

		root.setEL("authKeys", authKeysAsList(set));

	}

	public void removeAuthKeys(String key) throws PageException {
		checkWriteAccess();
		key = key.trim();

		// remove key
		ConfigServerImpl cs = (ConfigServerImpl) config;
		String[] keys = cs.getAuthenticationKeys();
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < keys.length; i++) {
			if (!key.equals(keys[i])) set.add(keys[i]);
		}

		root.setEL("authKeys", authKeysAsList(set));
	}

	public void updateAPIKey(String key) throws SecurityException, ApplicationException {
		checkWriteAccess();
		key = key.trim();
		if (!Decision.isGUId(key)) throw new ApplicationException("Passed API Key [" + key + "] is not valid");
		root.setEL("apiKey", key);

	}

	public void removeAPIKey() throws PageException {
		checkWriteAccess();
		if (root.containsKey("apiKey")) rem(root, "apiKey");
	}

	private String authKeysAsList(Set<String> set) throws PageException {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = set.iterator();
		String key;
		while (it.hasNext()) {
			key = it.next().trim();
			if (sb.length() > 0) sb.append(',');
			try {
				sb.append(URLEncoder.encode(key, "UTF-8"));
			}
			catch (UnsupportedEncodingException e) {
				throw Caster.toPageException(e);
			}
		}
		return sb.toString();
	}

	Resource[] updatePlugin(InputStream is, String realpath, boolean closeStream) throws PageException, IOException {
		List<Resource> filesDeployed = new ArrayList<Resource>();
		deployFilesFromStream(config, config.getPluginDirectory(), is, realpath, closeStream, filesDeployed);
		return filesDeployed.toArray(new Resource[filesDeployed.size()]);
	}

	public void updatePlugin(PageContext pc, Resource src) throws PageException, IOException {
		// convert to a directory when it is a zip
		if (!src.isDirectory()) {
			if (!IsZipFile.invoke(src))
				throw new ApplicationException("Path [" + src.getAbsolutePath() + "] is invalid, it has to be a path to an existing zip file or a directory containing a plugin");
			src = ResourceUtil.toResourceExisting(pc, "zip://" + src.getAbsolutePath());
		}
		String name = ResourceUtil.getName(src.getName());
		if (!PluginFilter.doAccept(src)) throw new ApplicationException("Plugin [" + src.getAbsolutePath() + "] is invalid, missing one of the following files [Action."
				+ Constants.getCFMLComponentExtension() + " or Action." + Constants.getLuceeComponentExtension() + ",language.xml] in root, existing files are ["
				+ lucee.runtime.type.util.ListUtil.arrayToList(src.list(), ", ") + "]");

		Resource dir = config.getPluginDirectory();
		Resource trgDir = dir.getRealResource(name);
		if (trgDir.exists()) {
			trgDir.remove(true);
		}

		ResourceUtil.copyRecursive(src, trgDir);
	}

	private static void setClass(Struct el, Class instanceOfClass, String prefix, ClassDefinition cd) throws PageException {
		if (cd == null || StringUtil.isEmpty(cd.getClassName())) return;

		if (prefix.endsWith("-")) prefix = prefix.substring(0, prefix.length() - 1);
		boolean hp = !prefix.isEmpty();
		// validate class
		try {
			Class clazz = cd.getClazz();

			if (instanceOfClass != null && !Reflector.isInstaneOf(clazz, instanceOfClass, false))
				throw new ApplicationException("Class [" + clazz.getName() + "] is not of type [" + instanceOfClass.getName() + "]");
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		el.setEL(hp ? prefix + "Class" : "class", cd.getClassName().trim());
		if (cd.isBundle()) {
			el.setEL(hp ? prefix + "BundleName" : "bundleName", cd.getName());
			if (cd.hasVersion()) el.setEL(hp ? prefix + "BundleVersion" : "bundleVersion", cd.getVersionAsString());
		}
		else {
			if (el.containsKey(hp ? prefix + "BundleName" : "bundleName")) el.remove(hp ? prefix + "BundleName" : "bundleName");
			if (el.containsKey(hp ? prefix + "BundleVersion" : "bundleVersion")) el.remove(hp ? prefix + "BundleVersion" : "bundleVersion");
		}
	}

	private void removeClass(Struct el, String prefix) {
		if (prefix.endsWith("-")) prefix = prefix.substring(0, prefix.length() - 1);
		boolean hp = !prefix.isEmpty();
		el.removeEL(KeyImpl.init(hp ? prefix + "Class" : "class"));
		el.removeEL(KeyImpl.init(hp ? prefix + "BundleName" : "bundleName"));
		el.removeEL(KeyImpl.init(hp ? prefix + "BundleVersion" : "bundleVersion"));
	}

	public final static class PluginFilter implements ResourceFilter {
		@Override
		public boolean accept(Resource res) {
			return doAccept(res);
		}

		public static boolean doAccept(Resource res) {
			return res.isDirectory() && (res.getRealResource("/Action." + Constants.getCFMLComponentExtension()).isFile()
					|| res.getRealResource("/Action." + Constants.getLuceeComponentExtension()).isFile()) && res.getRealResource("/language.xml").isFile();
		}

	}

	public void updateQueue(Integer max, Integer timeout, Boolean enable) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("Accces Denied to update queue settings");

		// max
		if (max == null) rem(root, "requestQueueMax");
		else root.setEL("requestQueueMax", max);
		// total
		if (timeout == null) rem(root, "requestQueueTimeout");
		else root.setEL("requestQueueTimeout", timeout);
		// enable
		if (enable == null) rem(root, "requestQueueEnable");
		else root.setEL("requestQueueEnable", enable);
	}

	public void updateCGIReadonly(Boolean cgiReadonly) throws SecurityException {
		checkWriteAccess();
		boolean hasAccess = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING);
		if (!hasAccess) throw new SecurityException("Accces Denied to update scope setting");

		root.setEL("cgiScopeReadOnly", Caster.toString(cgiReadonly, ""));
	}
}
