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
package lucee.runtime.tag;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletConfig;
import javax.servlet.jsp.tagext.Tag;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.VersionInfo;
import lucee.commons.collection.MapFactory;
import lucee.commons.digest.Base64Encoder;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CachePro;
import lucee.commons.io.compress.CompressUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.LoggerAndSourceData;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.DirectoryResourceFilter;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.NotResourceFilter;
import lucee.commons.io.res.filter.OrResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.IDGenerator;
import lucee.commons.lang.StringUtil;
import lucee.commons.surveillance.HeapDumper;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.osgi.BundleCollection;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cfx.customtag.CFXTagClass;
import lucee.runtime.cfx.customtag.JavaCFXTagClass;
import lucee.runtime.config.AdminSync;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.config.DatasourceConnPool;
import lucee.runtime.config.DebugEntry;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.config.Password;
import lucee.runtime.config.PasswordImpl;
import lucee.runtime.config.RemoteClient;
import lucee.runtime.config.RemoteClientImpl;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceImpl;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.db.DataSourcePro;
import lucee.runtime.db.DatasourceConnectionImpl;
import lucee.runtime.db.JDBCDriver;
import lucee.runtime.db.ParamSyntax;
import lucee.runtime.debug.Debugger;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ExecutionLogFactory;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionImpl;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.ext.tag.DynamicAttributes;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.functions.query.QuerySort;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.gateway.GatewayEntryImpl;
import lucee.runtime.gateway.GatewayUtil;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.monitor.IntervallMonitor;
import lucee.runtime.monitor.Monitor;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.net.http.CertificateInstaller;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.mail.SMTPException;
import lucee.runtime.net.mail.SMTPVerifier;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.mail.ServerImpl;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.Operator;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMConfigurationImpl;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.osgi.BundleBuilderFactory;
import lucee.runtime.osgi.BundleFile;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.JarUtil;
import lucee.runtime.osgi.ManifestUtil;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.rest.RestUtil;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.SpoolerEngine;
import lucee.runtime.spooler.SpoolerEngineImpl;
import lucee.runtime.spooler.SpoolerTask;
import lucee.runtime.spooler.remote.RemoteClientTask;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.type.scope.ClusterEntryImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;

/**
 * 
 */
public final class Admin extends TagImpl implements DynamicAttributes {

	private static final short TYPE_WEB = 0;
	private static final short TYPE_SERVER = 1;

	private static final short ACCESS_FREE = 0;
	private static final short ACCESS_NOT_WHEN_WEB = 1;
	private static final short ACCESS_NOT_WHEN_SERVER = 2;
	private static final short ACCESS_NEVER = 3;

	private static final short ACCESS_READ = 10;
	private static final short ACCESS_WRITE = 11;

	private static final Collection.Key DEBUG = KeyConstants._debug;
	// private static final Collection.Key DEBUG_TEMPLATE = KeyImpl.intern("debugTemplate");
	private static final Collection.Key DEBUG_SHOW_QUERY_USAGE = KeyImpl.getInstance("debugShowQueryUsage");
	// private static final Collection.Key STR_DEBUG_TEMPLATE = KeyImpl.intern("strdebugTemplate");
	private static final Collection.Key TEMPLATES = KeyConstants._templates;
	private static final Collection.Key STR = KeyConstants._str;
	private static final Collection.Key DO_STATUS_CODE = KeyImpl.getInstance("doStatusCode");
	private static final Collection.Key LABEL = KeyConstants._label;
	private static final Collection.Key FILE_ACCESS = KeyImpl.getInstance("file_access");
	private static final Collection.Key IP_RANGE = KeyImpl.getInstance("ipRange");
	private static final Collection.Key CUSTOM = KeyConstants._custom;
	private static final Collection.Key READONLY = KeyConstants._readOnly;
	private static final Collection.Key LOG_ENABLED = KeyImpl.getInstance("logEnabled");
	private static final Collection.Key CLASS = KeyConstants._class;

	private static final Key HAS_OWN_SEC_CONTEXT = KeyImpl.getInstance("hasOwnSecContext");
	private static final Key CONFIG_FILE = KeyImpl.getInstance("config_file");
	private static final Key PROCEDURE = KeyImpl.getInstance("procedure");
	private static final Key SERVER_LIBRARY = KeyImpl.getInstance("serverlibrary");
	private static final Key KEEP_ALIVE = KeyImpl.getInstance("keepalive");
	private static final Key CLIENT_SIZE = KeyImpl.getInstance("clientSize");
	private static final Key SESSION_SIZE = KeyImpl.getInstance("sessionSize");
	private static final Key CLIENT_ELEMENTS = KeyImpl.getInstance("clientElements");
	private static final Key SESSION_ELEMENTS = KeyImpl.getInstance("sessionElements");

	private static final short MAPPING_REGULAR = 1;
	private static final short MAPPING_CT = 2;
	private static final short MAPPING_CFC = 4;

	private Struct attributes = new StructImpl();
	private String action = null;
	private short type;
	private boolean singleMode;
	private Password password;
	private ConfigAdmin admin;
	private ConfigPro config;

	private static final ResourceFilter FILTER_CFML_TEMPLATES = new OrResourceFilter(
			new ResourceFilter[] { new DirectoryResourceFilter(), new ExtensionResourceFilter(Constants.getExtensions()) });
	private static final Key FRAGMENT = KeyImpl.getInstance("fragment");
	private static final Key HEADERS = KeyConstants._headers;
	private static final Key SYMBOLIC_NAME = KeyImpl.getInstance("symbolicName");
	private static final Key VENDOR = KeyImpl.getInstance("vendor");
	private static final Key USED_BY = KeyImpl.getInstance("usedBy");
	private static final Key PATH = KeyConstants._path;
	private AdminSync adminSync;

	@Override
	public void release() {
		super.release();
		attributes.clear();
	}

	@Override
	public void setDynamicAttribute(String uri, String localName, Object value) {
		attributes.setEL(KeyImpl.getInstance(localName), value);
	}

	@Override
	public void setDynamicAttribute(String uri, Collection.Key localName, Object value) {
		attributes.setEL(localName, value);
	}

	@Override
	public int doStartTag() throws PageException {
		config = (ConfigPro) pageContext.getConfig();

		// Action
		Object objAction = attributes.get(KeyConstants._action);
		if (objAction == null) throw new ApplicationException("Missing attribute [action] for tag [admin]");
		action = StringUtil.toLowerCase(Caster.toString(objAction)).trim();

		// Generals
		if (action.equals("buildbundle")) {
			doBuildBundle();
			return SKIP_BODY;
		}
		if (action.equals("readbundle")) {
			doReadBundle();
			return SKIP_BODY;
		}
		if (action.equals("getlocales")) {
			doGetLocales();
			return SKIP_BODY;
		}
		if (action.equals("gettimezones")) {
			doGetTimeZones();
			return SKIP_BODY;
		}
		if (action.equals("printdebug")) {
			throw new DeprecatedException("Action [printdebug] is no longer supported, use instead [getdebugdata]");
		}
		if (action.equals("getdebugdata")) {
			doGetDebugData();
			return SKIP_BODY;
		}
		if (action.equals("adddump")) {
			doAddDump();
			return SKIP_BODY;
		}
		if (action.equals("addgenericdata")) {
			doAddGenericData();
			return SKIP_BODY;
		}
		if (action.equals("getloginsettings")) {
			doGetLoginSettings();
			return SKIP_BODY;
		}

		// Type
		singleMode = config.getAdminMode() == ConfigImpl.ADMINMODE_SINGLE;
		type = singleMode ? TYPE_SERVER : toType(getString("type", "web"), true);

		// has Password
		if (action.equals("haspassword")) {
			boolean hasPassword = type == TYPE_WEB ? pageContext.getConfig().hasPassword() : pageContext.getConfig().hasServerPassword();

			pageContext.setVariable(getString("admin", action, "returnVariable", true), Caster.toBoolean(hasPassword));
			return SKIP_BODY;
		}

		// check Password
		else if (action.equals("checkpassword")) {
			try {
				config.checkPassword();
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
			return SKIP_BODY;
		}

		// update Password
		else if (action.equals("updatepassword")) {

			try {
				((ConfigWebPro) pageContext.getConfig()).updatePassword(type != TYPE_WEB, getString("oldPassword", null), getString("admin", action, "newPassword", true));
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
			return SKIP_BODY;
		}

		try {
			_doStartTag();
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}

		return Tag.SKIP_BODY;
	}

	private void doAddDump() throws ApplicationException {
		Debugger debugger = pageContext.getDebugger();
		PageSource ps = pageContext.getCurrentTemplatePageSource();
		if (ps != null) debugger.addDump(ps, getString("admin", action, "dump", true));
	}

	private void doAddGenericData() throws PageException {
		Debugger debugger = pageContext.getDebugger();
		debugger.addGenericData(getString("admin", action, "category", true), toMapStrStr(getStruct("admin", action, "data")));
	}

	private Map<String, String> toMapStrStr(Struct struct) throws PageException {
		Iterator<Entry<Key, Object>> it = struct.entryIterator();
		Map<String, String> map = new HashMap<String, String>();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			map.put(e.getKey().getString(), Caster.toString(e.getValue()));
		}
		return map;
	}

	private short toType(String strType, boolean throwError) throws ApplicationException {
		strType = StringUtil.toLowerCase(strType).trim();
		if ("web".equals(strType)) return TYPE_WEB;
		else if ("server".equals(strType)) return TYPE_SERVER;
		if (throwError) throw new ApplicationException("Invalid value for attribute type [" + strType + "] of tag admin", "valid values are [web, server]");
		return TYPE_WEB;
	}

	private void doTagSchedule() throws PageException {
		Schedule schedule = new Schedule();
		try {

			schedule.setPageContext(pageContext);
			schedule.setAction(getString("admin", action, "scheduleAction"));
			schedule.setTask(getString("task", null));
			schedule.setHidden(getBoolV("hidden", false));
			schedule.setReadonly(getBoolV("readonly", false));
			schedule.setOperation(getString("operation", null));
			schedule.setFile(getString("file", null));
			schedule.setPath(getString("path", null));
			schedule.setStartdate(getObject("startDate", null));
			schedule.setStarttime(getObject("startTime", null));
			schedule.setUrl(getString("url", null));
			schedule.setPublish(getBoolV("publish", false));
			schedule.setEnddate(getObject("endDate", null));
			schedule.setEndtime(getObject("endTime", null));
			schedule.setInterval(getString("interval", null));
			schedule.setRequesttimeout(new Double(getDouble("requestTimeOut", -1)));
			schedule.setUsername(getString("username", null));
			schedule.setPassword(getString("schedulePassword", null));
			schedule.setProxyserver(getString("proxyServer", null));
			schedule.setProxyuser(getString("proxyuser", null));
			schedule.setProxypassword(getString("proxyPassword", null));
			schedule.setResolveurl(getBoolV("resolveURL", false));
			schedule.setPort(new Double(getDouble("port", -1)));
			schedule.setProxyport(new Double(getDouble("proxyPort", 80)));
			schedule.setUnique(getBoolV("unique", false));

			String rtn = getString("returnvariable", null);
			if (StringUtil.isEmpty(rtn)) rtn = getString("result", "cfschedule");
			schedule.setResult(rtn);

			schedule.doStartTag();
		}
		finally {
			schedule.release();
			adminSync.broadcast(attributes, config);
			adminSync.broadcast(attributes, config);
		}
	}

	/*
	 * private void doTagSearch() throws PageException { Search search=new Search(); try {
	 * 
	 * search.setPageContext(pageContext);
	 * 
	 * search.setName(getString("admin",action,"name"));
	 * search.setCollection(getString("admin",action,"collection"));
	 * search.setType(getString("type",null)); search.setMaxrows(getDouble("maxRows",-1));
	 * search.setStartrow(getDouble("startRow",1)); search.setCategory(getString("category",null));
	 * search.setCategorytree(getString("categoryTree",null));
	 * search.setStatus(getString("status",null)); search.setSuggestions(getString("suggestions",null));
	 * 
	 * search.doStartTag(); } finally { search.release(); } }
	 */

	private void doTagIndex() throws PageException {
		Index index = new Index();
		try {

			index.setPageContext(pageContext);

			index.setCollection(getString("admin", action, "collection"));
			index.setAction(getString("admin", action, "indexAction"));
			index.setType(getString("indexType", null));
			index.setTitle(getString("title", null));
			index.setKey(getString("key", null));
			index.setBody(getString("body", null));
			index.setCustom1(getString("custom1", null));
			index.setCustom2(getString("custom2", null));
			index.setCustom3(getString("custom3", null));
			index.setCustom4(getString("custom4", null));
			index.setUrlpath(getString("URLpath", null));
			index.setExtensions(getString("extensions", null));
			index.setQuery(getString("query", null));
			index.setRecurse(getBoolV("recurse", false));
			index.setLanguage(getString("language", null));
			index.setCategory(getString("category", null));
			index.setCategorytree(getString("categoryTree", null));
			index.setStatus(getString("status", null));
			index.setPrefix(getString("prefix", null));

			index.doStartTag();
		}
		finally {
			index.release();
			adminSync.broadcast(attributes, config);
		}
	}

	private void doTagCollection() throws PageException {
		lucee.runtime.tag.Collection coll = new lucee.runtime.tag.Collection();
		try {

			coll.setPageContext(pageContext);

			// coll.setCollection(getString("admin",action,"collection"));
			coll.setAction(getString("collectionAction", null));
			coll.setCollection(getString("collection", null));
			coll.setPath(getString("path", null));
			coll.setLanguage(getString("language", null));
			coll.setName(getString("name", null));

			coll.doStartTag();
		}
		finally {
			coll.release();
			adminSync.broadcast(attributes, config);
		}
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void _doStartTag() throws PageException, IOException {
		config = (ConfigPro) pageContext.getConfig();

		// getToken
		if (action.equals("gettoken")) {
			doGetToken();
			return;
		}

		// schedule
		if (action.equals("schedule")) {
			doTagSchedule();
			return;
		}
		// search
		if (action.equals("collection")) {
			doTagCollection();
			return;
		}
		// index
		if (action.equals("index")) {
			doTagIndex();
			return;
		}
		// cluster
		if (action.equals("setcluster")) {
			doSetCluster();
			return;
		}
		if (action.equals("getcluster")) {
			doGetCluster();
			return;
		}

		if (action.equals("getextension")) {
			if (type == TYPE_SERVER) doGetRHServerExtension();
			else doGetRHExtension();
			return;
		}
		if (action.equals("getextensions") || action.equals("getrhextensions")) {
			if (type == TYPE_SERVER) doGetRHServerExtensions();
			else doGetRHExtensions();
			return;
		}
		if (action.equals("getserverextensions") || action.equals("getrhserverextensions")) {
			doGetRHServerExtensions();
			return;
		}

		if (check("hashpassword", ACCESS_FREE)) {
			String raw = getString("admin", action, "pw");
			Password pw = PasswordImpl.passwordToCompare(pageContext.getConfig(), type != TYPE_WEB, raw);

			Password changed = ((ConfigWebPro) pageContext.getConfig()).updatePasswordIfNecessary(type == TYPE_SERVER, raw);
			if (changed != null) pw = changed;

			pageContext.setVariable(getString("admin", action, "returnVariable"), pw.getPassword());
			return; // do not remove
		}

		try {
			// Password
			String strPW = getString("password", "");
			Password tmp = type == TYPE_SERVER ? ((ConfigWebPro) config).isServerPasswordEqual(strPW) : config.isPasswordEqual(strPW); // hash password if
			// necessary (for
			// backward
			// compatibility)
			if (tmp != null) password = tmp;
			else password = null;

			// Config
			if (type == TYPE_SERVER) config = (ConfigPro) pageContext.getConfig().getConfigServer(password);

			adminSync = config.getAdminSync();
			admin = ConfigAdmin.newInstance(config, password);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		if (check("connect", ACCESS_FREE)) {
			ConfigWebUtil.checkPassword(config, null, password);
			ConfigWebUtil.checkGeneralReadAccess(config, password);

			try {
				if (config instanceof ConfigServer) ((PageContextImpl) pageContext).setServerPassword(password);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		else if (check("getinfo", ACCESS_FREE) && check2(ACCESS_READ)) doGetInfo();
		else if (check("surveillance", ACCESS_FREE) && check2(ACCESS_READ)) doSurveillance();
		else if (check("getRegional", ACCESS_FREE) && check2(ACCESS_READ)) doGetRegional();
		else if (check("isMonitorEnabled", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doIsMonitorEnabled();
		else if (check("resetORMSetting", ACCESS_FREE) && check2(ACCESS_READ)) doResetORMSetting();
		else if (check("getORMSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetORMSetting();
		else if (check("getORMEngine", ACCESS_FREE) && check2(ACCESS_READ)) doGetORMEngine();
		else if (check("updateORMSetting", ACCESS_FREE) && check2(ACCESS_READ)) doUpdateORMSetting();
		else if (check("getApplicationListener", ACCESS_FREE) && check2(ACCESS_READ)) doGetApplicationListener();
		else if (check("getProxy", ACCESS_FREE) && check2(ACCESS_READ)) doGetProxy();
		else if (check("getCharset", ACCESS_FREE) && check2(ACCESS_READ)) doGetCharset();
		else if (check("getComponent", ACCESS_FREE) && check2(ACCESS_READ)) doGetComponent();
		else if (check("getScope", ACCESS_FREE) && check2(ACCESS_READ)) doGetScope();
		else if (check("getDevelopMode", ACCESS_FREE) && check2(ACCESS_READ)) doGetDevelopMode();
		else if (check("getApplicationSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetApplicationSetting();
		else if (check("getQueueSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetQueueSetting();
		else if (check("getOutputSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetOutputSetting();
		else if (check("getDatasourceSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetDatasourceSetting();
		else if (check("getCustomTagSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetCustomTagSetting();
		else if (check("getDatasource", ACCESS_FREE) && check2(ACCESS_READ)) doGetDatasource();
		else if (check("getDatasources", ACCESS_FREE) && check2(ACCESS_READ)) doGetDatasources();
		else if (check("getJDBCDrivers", ACCESS_FREE) && check2(ACCESS_READ)) doGetJDBCDrivers();
		else if (check("getCacheConnections", ACCESS_FREE) && check2(ACCESS_READ)) doGetCacheConnections();
		else if (check("getCacheConnection", ACCESS_FREE) && check2(ACCESS_READ)) doGetCacheConnection();
		else if (check("getCacheDefaultConnection", ACCESS_FREE) && check2(ACCESS_READ)) doGetCacheDefaultConnection();
		else if (check("getRemoteClients", ACCESS_FREE) && check2(ACCESS_READ)) doGetRemoteClients();
		else if (check("getRemoteClient", ACCESS_FREE) && check2(ACCESS_READ)) doGetRemoteClient();
		else if (check("hasRemoteClientUsage", ACCESS_FREE) && check2(ACCESS_READ)) doHasRemoteClientUsage();
		else if (check("getRemoteClientUsage", ACCESS_FREE) && check2(ACCESS_READ)) doGetRemoteClientUsage();
		else if (check("getSpoolerTasks", ACCESS_FREE) && check2(ACCESS_READ)) doGetSpoolerTasks();
		else if (check("getPerformanceSettings", ACCESS_FREE) && check2(ACCESS_READ)) doGetPerformanceSettings();
		else if (check("getLogSettings", ACCESS_FREE) && check2(ACCESS_READ)) doGetLogSettings();
		else if (check("getCompilerSettings", ACCESS_FREE) && check2(ACCESS_READ)) doGetCompilerSettings();
		else if (check("updatePerformanceSettings", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdatePerformanceSettings();
		else if (check("updateCompilerSettings", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCompilerSettings();
		else if (check("getGatewayentries", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_READ)) doGetGatewayEntries();
		else if (check("getGatewayentry", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_READ)) doGetGatewayEntry();
		else if (check("getRunningThreads", ACCESS_FREE) && check2(ACCESS_READ)) doGetRunningThreads();
		else if (check("getMonitors", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetMonitors();
		else if (check("getMonitor", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetMonitor();
		else if (check("getBundles", ACCESS_FREE) && check2(ACCESS_READ)) doGetBundles();
		else if (check("getBundle", ACCESS_FREE) && check2(ACCESS_READ)) doGetBundle();
		else if (check("getExecutionLog", ACCESS_FREE) && check2(ACCESS_READ)) doGetExecutionLog();
		else if (check("gateway", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_READ)) doGateway();

		// alias for getSpoolerTasks
		else if (check("getRemoteClientTasks", ACCESS_FREE) && check2(ACCESS_READ)) doGetSpoolerTasks();
		else if (check("getDatasourceDriverList", ACCESS_FREE) && check2(ACCESS_READ)) doGetDatasourceDriverList();
		else if (check("getDebuggingList", ACCESS_FREE) && check2(ACCESS_READ)) doGetDebuggingList();
		else if (check("getLoggedDebugData", ACCESS_FREE)) // no password necessary for this
			doGetLoggedDebugData();
		else if (check("PurgeDebugPool", ACCESS_FREE) && check2(ACCESS_WRITE)) doPurgeDebugPool();
		else if (check("getDebugSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetDebugSetting();
		else if (check("getSSLCertificate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetSSLCertificate();
		else if (check("getPluginDirectory", ACCESS_FREE) && check2(ACCESS_READ)) doGetPluginDirectory();
		else if (check("getPlugins", ACCESS_FREE) && check2(ACCESS_READ)) doGetPlugins();
		else if (check("updatePlugin", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdatePlugin();
		else if (check("removePlugin", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemovePlugin();

		else if (check("getContextDirectory", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) getContextDirectory();
		else if (check("updateContext", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateContext();
		else if (check("removeContext", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveContext();

		else if (check("getJars", ACCESS_FREE) && check2(ACCESS_READ)) doGetJars();
		else if (check("getFlds", ACCESS_FREE) && check2(ACCESS_READ)) doGetFLDs();
		else if (check("getTlds", ACCESS_FREE) && check2(ACCESS_READ)) doGetTLDs();
		else if (check("getLocalExtension", ACCESS_FREE) && check2(ACCESS_READ)) doGetLocalExtension();
		else if (check("getLocalExtensions", ACCESS_FREE) && check2(ACCESS_READ)) doGetLocalExtensions();
		else if (check("getMailSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetMailSetting();
		else if (check("getTaskSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetTaskSetting();
		else if (check("getMailServers", ACCESS_FREE) && check2(ACCESS_READ)) doGetMailServers();
		else if (check("getMapping", ACCESS_FREE) && check2(ACCESS_READ)) doGetMapping();
		else if (check("getMappings", ACCESS_FREE) && check2(ACCESS_READ)) doGetMappings();
		else if (check("getRestMappings", ACCESS_FREE) && check2(ACCESS_READ)) doGetRestMappings();
		else if (check("getRestSettings", ACCESS_FREE) && check2(ACCESS_READ)) doGetRestSettings();
		else if ((check("getRHExtensionProviders", ACCESS_FREE) || check("getExtensionProviders", ACCESS_FREE)) && check2(ACCESS_READ)) doGetRHExtensionProviders();
		else if (check("getExtensionInfo", ACCESS_FREE) && check2(ACCESS_READ)) doGetExtensionInfo();

		else if (check("getCustomTagMappings", ACCESS_FREE) && check2(ACCESS_READ)) doGetCustomTagMappings();
		else if (check("getComponentMappings", ACCESS_FREE) && check2(ACCESS_READ)) doGetComponentMappings();
		else if (check("getCfxTags", ACCESS_FREE) && check2(ACCESS_READ)) doGetCFXTags();
		else if (check("getJavaCfxTags", ACCESS_FREE) && check2(ACCESS_READ)) doGetJavaCFXTags();
		else if (check("getDebug", ACCESS_FREE) && check2(ACCESS_READ)) doGetDebug();
		else if (check("getSecurity", ACCESS_FREE) && check2(ACCESS_READ)) doGetSecurity();
		else if (check("getDebugEntry", ACCESS_FREE)) doGetDebugEntry();
		else if (check("getError", ACCESS_FREE) && check2(ACCESS_READ)) doGetError();
		else if (check("getRegex", ACCESS_FREE) && check2(ACCESS_READ)) doGetRegex();
		else if (check("verifyremoteclient", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyRemoteClient();
		else if (check("verifyDatasource", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyDatasource();
		else if (check("verifyCacheConnection", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyCacheConnection();
		else if (check("verifyMailServer", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyMailServer();
		else if (check("verifyExtensionProvider", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyExtensionProvider();
		else if (check("verifyJavaCFX", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyJavaCFX();
		else if (check("verifyCFX", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyCFX();

		else if (check("resetId", ACCESS_FREE) && check2(ACCESS_WRITE)) doResetId();
		else if (check("updateLoginSettings", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateLoginSettings();
		else if (check("updateLogSettings", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateLogSettings();
		else if (check("updateJar", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateJar();
		else if (check("updateSSLCertificate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateSSLCertificate();
		else if (check("updateMonitorEnabled", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateMonitorEnabled();
		else if (check("updateTLD", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateTLD();
		else if (check("updateFLD", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateFLD();
		else if (check("updateregional", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRegional();
		else if (check("updateApplicationListener", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateApplicationListener();
		else if (check("updateCachedWithin", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCachedWithin();
		else if (check("updateproxy", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateProxy();
		else if (check("updateCharset", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCharset();
		else if (check("updatecomponent", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateComponent();
		else if (check("updatescope", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateScope();
		else if (check("updateDevelopMode", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDevelopMode();
		else if (check("updateRestSettings", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRestSettings();
		else if (check("updateRestMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRestMapping();
		else if (check("removeRestMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveRestMapping();
		else if (check("updateApplicationSetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateApplicationSettings();
		else if (check("updateOutputSetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateOutputSettings();
		else if (check("updateQueueSetting", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateQueueSettings();
		else if (check("updatepsq", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdatePSQ();
		else if (check("updatedatasource", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDatasource();
		else if (check("updateJDBCDriver", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateJDBCDriver();
		else if (check("updateCacheDefaultConnection", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCacheDefaultConnection();
		else if (check("updateCacheConnection", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCacheConnection();
		else if (check("updateremoteclient", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRemoteClient();
		else if (check("updateRemoteClientUsage", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRemoteClientUsage();
		else if (check("updatemailsetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateMailSetting();
		else if (check("updatemailserver", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateMailServer();
		else if (check("updatetasksetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateTaskSetting();
		else if (check("updatemapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateMapping();
		else if (check("updatecustomtag", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCustomTag();
		else if (check("updateComponentMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateComponentMapping();
		else if (check("stopThread", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doStopThread();
		else if (check("updateAdminMode", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateAdminMode();

		else if (check("updatejavacfx", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateJavaCFX();
		else if (check("updatedebug", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDebug();
		else if (check("updatesecurity", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateSecurity();
		else if (check("updatedebugentry", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDebugEntry();
		else if (check("updatedebugsetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDebugSetting();

		else if (check("updateerror", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateError();
		else if (check("updateregex", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRegex();
		else if (check("updateCustomTagSetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCustomTagSetting();
		// else if(check("updateExtension", ACCESS_FREE) && check2(ACCESS_WRITE))
		// doUpdateExtension();
		else if ((check("updateRHExtension", ACCESS_FREE) || check("updateExtension", ACCESS_FREE)) && check2(ACCESS_WRITE)) doUpdateRHExtension();
		else if ((check("removeRHExtension", ACCESS_FREE) || check("removeExtension", ACCESS_FREE)) && check2(ACCESS_WRITE)) doRemoveRHExtension();
		else if (check("updateExtensionProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateExtensionProvider();
		else if ((check("updateRHExtensionProvider", ACCESS_FREE) || check("updateExtensionProvider", ACCESS_FREE)) && check2(ACCESS_WRITE)) doUpdateRHExtensionProvider();
		else if (check("updateExtensionInfo", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateExtensionInfo();
		else if (check("updateGatewayEntry", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_WRITE)) doUpdateGatewayEntry();
		// else if(check("updateLogSettings", ACCESS_FREE) && check2(ACCESS_WRITE ))
		// doUpdateUpdateLogSettings();
		else if (check("updateMonitor", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateMonitor();
		else if (check("updateCacheHandler", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateCacheHandler();
		else if (check("updateORMEngine", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateORMEngine();
		else if (check("updateExecutionLog", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateExecutionLog();

		// else if(check("removeproxy", ACCESS_NOT_WHEN_SERVER )) doRemoveProxy();
		else if (check("removeMonitor", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveMonitor();
		else if (check("removeCacheHandler", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveCacheHandler();
		else if (check("removeORMEngine", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveORMEngine();
		else if (check("removebundle", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveBundle();
		else if (check("removeTLD", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveTLD();
		else if (check("removeFLD", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveFLD();
		else if (check("removeJDBCDriver", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveJDBCDriver();
		else if (check("removedatasource", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveDatasource();
		else if (check("removeCacheConnection", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveCacheConnection();
		else if (check("removeremoteclient", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveRemoteClient();
		else if (check("removeRemoteClientUsage", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveRemoteClientUsage();
		else if (check("removeSpoolerTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveSpoolerTask();
		else if (check("removeAllSpoolerTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveAllSpoolerTask();
		// alias for executeSpoolerTask
		else if (check("removeRemoteClientTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveSpoolerTask();
		else if (check("executeSpoolerTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doExecuteSpoolerTask();
		// alias for executeSpoolerTask
		else if (check("executeRemoteClientTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doExecuteSpoolerTask();
		else if (check("removemailserver", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveMailServer();
		else if (check("removemapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveMapping();
		else if (check("removecustomtag", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveCustomTag();
		else if (check("removecomponentmapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveComponentMapping();
		else if (check("removecfx", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveCFX();
		else if (check("removeExtension", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveExtension();
		else if (check("removeExtensionProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveExtensionProvider();
		else if (check("removeRHExtensionProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveRHExtensionProvider();
		else if (check("removeDefaultPassword", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveDefaultPassword();
		else if (check("removeGatewayEntry", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_WRITE)) doRemoveGatewayEntry();
		else if (check("removeDebugEntry", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveDebugEntry();
		else if (check("removeCacheDefaultConnection", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveCacheDefaultConnection();
		else if (check("removeLogSetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveLogSetting();

		else if (check("storageGet", ACCESS_FREE) && check2(ACCESS_READ)) doStorageGet();
		else if (check("storageSet", ACCESS_FREE) && check2(ACCESS_WRITE)) doStorageSet();

		else if (check("getdefaultpassword", ACCESS_FREE) && check2(ACCESS_READ)) doGetDefaultPassword();
		else if (check("getContexts", ACCESS_FREE) && check2(ACCESS_READ)) doGetContexts();
		else if (check("updatedefaultpassword", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDefaultPassword();
		else if (check("hasindividualsecurity", ACCESS_FREE) && check2(ACCESS_READ)) doHasIndividualSecurity();
		else if (check("resetpassword", ACCESS_FREE) && check2(ACCESS_WRITE)) doResetPassword();
		else if (check("stopThread", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doStopThread();

		else if (check("updateAuthKey", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateAuthKey();
		else if (check("removeAuthKey", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveAuthKey();
		else if (check("listAuthKey", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doListAuthKey();

		else if (check("updateAPIKey", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateAPIKey();
		else if (check("removeAPIKey", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveAPIKey();
		else if (check("getAPIKey", ACCESS_FREE) && check2(ACCESS_READ)) doGetAPIKey();

		else if (check("createsecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doCreateSecurityManager();
		else if (check("getsecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetSecurityManager();
		else if (check("removesecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveSecurityManager();
		else if (check("getdefaultsecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetDefaultSecurityManager();
		else if (check("updatesecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateSecurityManager();
		else if (check("updatedefaultsecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateDefaultSecurityManager();
		else if (check("compileMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doCompileMapping();
		else if (check("compileComponentMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doCompileComponentMapping();
		else if (check("compileCTMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doCompileCTMapping();
		else if (check("createArchive", ACCESS_FREE) && check2(ACCESS_WRITE)) doCreateArchive(MAPPING_REGULAR);
		else if (check("createComponentArchive", ACCESS_FREE) && check2(ACCESS_WRITE)) doCreateArchive(MAPPING_CFC);
		else if (check("createCTArchive", ACCESS_FREE) && check2(ACCESS_WRITE)) doCreateArchive(MAPPING_CT);
		else if (check("reload", ACCESS_FREE) && check2(ACCESS_WRITE)) doReload();

		else if (check("getResourceProviders", ACCESS_FREE) && check2(ACCESS_READ)) doGetResourceProviders();
		else if (check("updateResourceProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateResourceProvider();
		else if (check("updateDefaultResourceProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDefaultResourceProvider();
		else if (check("removeResourceProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveResourceProvider();

		else if (check("getAdminSyncClass", ACCESS_FREE) && check2(ACCESS_READ)) doGetAdminSyncClass();
		else if (check("updateAdminSyncClass", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateAdminSyncClass();

		else if (check("getVideoExecuterClass", ACCESS_FREE) && check2(ACCESS_READ)) doGetVideoExecuterClass();
		else if (check("updateVideoExecuterClass", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateVideoExecuterClass();
		else if (check("terminateRunningThread", ACCESS_FREE) && check2(ACCESS_WRITE)) doTerminateRunningThread();

		else if (check("updateLabel", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateLabel();
		else if (check("restart", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRestart();
		else if (check("runUpdate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRunUpdate();
		else if (check("removeUpdate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveUpdate();
		else if (check("changeVersionTo", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doChangeVersionTo();
		else if (check("getUpdate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doGetUpdate();
		else if (check("getMinVersion", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) getMinVersion();
		else if (check("getLoaderInfo", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) getLoaderInfo();
		else if (check("listPatches", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) listPatches();
		else if (check("updateupdate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateUpdate();
		else if (check("getSerial", ACCESS_FREE) && check2(ACCESS_READ)) doGetSerial();
		else if (check("updateSerial", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateSerial();
		else if (check("heapDump", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doHeapDump();
		else if (check("securitymanager", ACCESS_FREE) && check2(ACCESS_READ)) doSecurityManager();

		else throw new ApplicationException("Invalid action [" + action + "] for tag admin");

	}

	private boolean check2(short accessRW) throws SecurityException {
		if (accessRW == ACCESS_READ) ConfigWebUtil.checkGeneralReadAccess(config, password);
		else if (accessRW == ACCESS_WRITE) ConfigWebUtil.checkGeneralWriteAccess(config, password);
		/*
		 * else if(accessRW==CHECK_PW) { ConfigWebUtil.checkGeneralReadAccess(config,password);
		 * ConfigWebUtil.checkPassword(config,null,password); }
		 */
		return true;
	}

	private boolean check(String action, short access) throws ApplicationException {
		if (this.action.equalsIgnoreCase(action)) {
			if (access == ACCESS_FREE) {
			}
			else if (access == ACCESS_NOT_WHEN_SERVER) {
				throwNoAccessWhenServer();
			}

			else if (access == ACCESS_NOT_WHEN_WEB) {
				throwNoAccessWhenWeb();
			}
			else if (access == ACCESS_NEVER) {
				throwNoAccessWhenServer();
				throwNoAccessWhenServer();
			}
			return true;
		}
		return false;
	}

	private void doRunUpdate() throws PageException {
		admin.runUpdate(password);
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveUpdate() throws PageException {
		boolean onlyLatest = getBoolV("onlyLatest", false);

		if (onlyLatest) admin.removeLatestUpdate(password);
		else admin.removeUpdate(password);
		adminSync.broadcast(attributes, config);
	}

	private void doChangeVersionTo() throws PageException {
		try {
			Version version = OSGiUtil.toVersion(getString("admin", "changeVersionTo", "version"));
			admin.changeVersionTo(version, password
			// ,pageContext.getConfig().getLog("Application")
					, pageContext.getConfig().getIdentification());
			adminSync.broadcast(attributes, config);
		}
		catch (BundleException e) {
			throw Caster.toPageException(e);
		}
	}

	private void doRestart() throws PageException {
		admin.restart(password);
		adminSync.broadcast(attributes, config);
	}

	private void doCreateArchive(short mappingType) throws PageException {
		String virtual = getString("admin", action, "virtual").toLowerCase();
		String strFile = getString("admin", action, "file");
		Resource file = ResourceUtil.toResourceNotExisting(pageContext, strFile);

		boolean addCFMLFiles = getBoolV("addCFMLFiles", true);
		boolean addNonCFMLFiles = getBoolV("addNonCFMLFiles", true);
		Boolean ignoreScopes = getBool("ignoreScopes", null);

		// compile
		MappingImpl mapping = (MappingImpl) doCompileMapping(mappingType, virtual, true, ignoreScopes);

		// class files
		if (mapping == null) throw new ApplicationException("There is no mapping for [" + virtual + "]");
		if (!mapping.hasPhysical()) throw new ApplicationException("Mapping [" + virtual + "] has no physical directory");

		Resource classRoot = mapping.getClassRootDirectory();
		Resource temp = SystemUtil.getTempDirectory().getRealResource("mani-" + IDGenerator.stringId());
		Resource mani = temp.getRealResource("META-INF/MANIFEST.MF");

		try {
			if (file.exists()) file.delete();
			if (!file.exists()) file.createFile(true);

			ResourceFilter filter;

			// include everything, no filter needed
			if (addCFMLFiles && addNonCFMLFiles) filter = null;
			// CFML Files but no other files
			else if (addCFMLFiles) {
				if (mappingType == MAPPING_CFC) filter = new ExtensionResourceFilter(ArrayUtil.toArray(Constants.getComponentExtensions(), "class", "MF"), true, true);
				else filter = new ExtensionResourceFilter(ArrayUtil.toArray(Constants.getExtensions(), "class", "MF"), true, true);
			}
			// No CFML Files, but all other files
			else if (addNonCFMLFiles) {
				filter = new NotResourceFilter(new ExtensionResourceFilter(Constants.getExtensions(), false, true));
			}
			// no files at all
			else {
				filter = new ExtensionResourceFilter(new String[] { "class", "MF" }, true, true);
			}
			String id = HashUtil.create64BitHashAsString(mapping.getStrPhysical(), Character.MAX_RADIX);
			// String id = MD5.getDigestAsString(mapping.getStrPhysical());

			String type;
			if (mappingType == MAPPING_CFC) type = "cfc";
			else if (mappingType == MAPPING_CT) type = "ct";
			else type = "regular";

			String token = HashUtil.create64BitHashAsString(System.currentTimeMillis() + "", Character.MAX_RADIX);

			// create manifest
			Manifest mf = new Manifest();
			// StringBuilder manifest=new StringBuilder();

			// Write OSGi specific stuff
			Attributes attrs = mf.getMainAttributes();
			attrs.putValue("Bundle-ManifestVersion", Caster.toString(BundleBuilderFactory.MANIFEST_VERSION));
			attrs.putValue("Bundle-SymbolicName", id);
			attrs.putValue("Bundle-Name", ListUtil.trim(mapping.getVirtual().replace('/', '.'), "."));
			attrs.putValue("Bundle-Description", "this is a " + type + " mapping generated by " + Constants.NAME + ".");
			attrs.putValue("Bundle-Version", "1.0.0." + token);
			// attrs.putValue("Import-Package","lucee.*");
			attrs.putValue("Require-Bundle", "lucee.core");

			// Mapping
			attrs.putValue("mapping-id", id);
			attrs.putValue("mapping-type", type);
			attrs.putValue("mapping-virtual-path", mapping.getVirtual());
			attrs.putValue("mapping-hidden", Caster.toString(mapping.isHidden()));
			attrs.putValue("mapping-physical-first", Caster.toString(mapping.isPhysicalFirst()));
			attrs.putValue("mapping-readonly", Caster.toString(mapping.isReadonly()));
			attrs.putValue("mapping-top-level", Caster.toString(mapping.isTopLevel()));
			attrs.putValue("mapping-inspect", ConfigWebUtil.inspectTemplate(mapping.getInspectTemplateRaw(), ""));
			attrs.putValue("mapping-listener-type", ConfigWebUtil.toListenerType(mapping.getListenerType(), ""));
			attrs.putValue("mapping-listener-mode", ConfigWebUtil.toListenerMode(mapping.getListenerMode(), ""));

			mani.createFile(true);
			IOUtil.write(mani, ManifestUtil.toString(mf, 100, null, null), "UTF-8", false);

			// source files
			Resource[] sources;
			if (!addCFMLFiles && !addNonCFMLFiles) sources = new Resource[] { temp, classRoot };
			else sources = new Resource[] { temp, mapping.getPhysical(), classRoot };

			CompressUtil.compressZip(ResourceUtil.listResources(sources, filter), file, filter);

			if (getBoolV("append", false)) {
				if (mappingType == MAPPING_CFC) {
					admin.updateComponentMapping(mapping.getVirtual(), mapping.getStrPhysical(), strFile, mapping.isPhysicalFirst() ? "physical" : "archive",
							mapping.getInspectTemplateRaw());
				}
				else if (mappingType == MAPPING_CT) {
					admin.updateCustomTag(mapping.getVirtual(), mapping.getStrPhysical(), strFile, mapping.isPhysicalFirst() ? "physical" : "archive",
							mapping.getInspectTemplateRaw());

				}

				else admin.updateMapping(mapping.getVirtual(), mapping.getStrPhysical(), strFile, mapping.isPhysicalFirst() ? "physical" : "archive",
						mapping.getInspectTemplateRaw(), mapping.isTopLevel(), mapping.getListenerMode(), mapping.getListenerType(), mapping.isReadonly());
				store();
			}

		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		finally {
			ResourceUtil.removeEL(temp, true);
		}
		adminSync.broadcast(attributes, config);
	}

	private void doCompileMapping() throws PageException {
		doCompileMapping(MAPPING_REGULAR, getString("admin", action, "virtual").toLowerCase(), getBoolV("stoponerror", true), getBool("ignoreScopes", null));
		adminSync.broadcast(attributes, config);
	}

	private void doCompileComponentMapping() throws PageException {
		doCompileMapping(MAPPING_CFC, getString("admin", action, "virtual").toLowerCase(), getBoolV("stoponerror", true), getBool("ignoreScopes", null));
		adminSync.broadcast(attributes, config);
	}

	private void doCompileCTMapping() throws PageException {
		doCompileMapping(MAPPING_CT, getString("admin", action, "virtual").toLowerCase(), getBoolV("stoponerror", true), getBool("ignoreScopes", null));
		adminSync.broadcast(attributes, config);
	}

	private Mapping doCompileMapping(short mappingType, String virtual, boolean stoponerror, Boolean ignoreScopes) throws PageException {

		if (StringUtil.isEmpty(virtual)) return null;

		if (!StringUtil.startsWith(virtual, '/')) virtual = '/' + virtual;
		if (!StringUtil.endsWith(virtual, '/')) virtual += '/';

		Mapping[] mappings = null;
		if (mappingType == MAPPING_CFC) mappings = config.getComponentMappings();
		else if (mappingType == MAPPING_CT) mappings = config.getCustomTagMappings();
		else mappings = config.getMappings();

		for (int i = 0; i < mappings.length; i++) {
			Mapping mapping = mappings[i];
			if (mapping.getVirtualLowerCaseWithSlash().equals(virtual)) {
				Map<String, String> errors = stoponerror ? null : MapFactory.<String, String>getConcurrentMap();
				doCompileFile(mapping, mapping.getPhysical(), "", errors, ignoreScopes);
				if (errors != null && errors.size() > 0) {
					StringBuilder sb = new StringBuilder();
					Iterator<String> it = errors.keySet().iterator();
					Object key;
					while (it.hasNext()) {
						key = it.next();
						if (sb.length() > 0) sb.append("\n\n");
						sb.append(errors.get(key));

					}
					throw new ApplicationException(sb.toString());
				}
				return mapping;
			}
		}
		return null;
	}

	private void doCompileFile(Mapping mapping, Resource file, String path, Map<String, String> errors, Boolean explicitIgnoreScope) throws PageException {
		if (ResourceUtil.exists(file)) {
			if (file.isDirectory()) {
				Resource[] files = file.listResources(FILTER_CFML_TEMPLATES);
				if (files != null) for (int i = 0; i < files.length; i++) {
					String p = path + '/' + files[i].getName();
					// print.ln(files[i]+" - "+p);
					doCompileFile(mapping, files[i], p, errors, explicitIgnoreScope);
				}
			}
			else if (file.isFile()) {
				PageSource ps = mapping.getPageSource(path);

				PageContextImpl pci = (PageContextImpl) pageContext;
				boolean envIgnoreScopes = pci.ignoreScopes();
				try {
					if (explicitIgnoreScope != null) pci.setIgnoreScopes(explicitIgnoreScope);
					((PageSourceImpl) ps).clear();
					((PageSourceImpl) ps).loadPage(pageContext, explicitIgnoreScope != null);
					// pageContext.compile(ps);
				}
				catch (PageException pe) {
					LogUtil.log(ThreadLocalPageContext.getConfig(pageContext), Admin.class.getName(), pe);
					String template = ps.getDisplayPath();
					StringBuilder msg = new StringBuilder(pe.getMessage());
					msg.append(", Error Occurred in File [");
					msg.append(template);
					if (pe instanceof PageExceptionImpl) {
						try {
							PageExceptionImpl pei = (PageExceptionImpl) pe;
							Array context = pei.getTagContext(config);
							if (context.size() > 0) {
								msg.append(":");
								msg.append(Caster.toString(((Struct) context.getE(1)).get("line")));
							}
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
						}

					}
					msg.append("]");
					if (errors != null) errors.put(template, msg.toString());
					else throw new ApplicationException(msg.toString());

				}
				finally {
					pci.setIgnoreScopes(envIgnoreScopes);
				}
			}
		}
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doResetPassword() throws PageException {
		try {
			admin.removePassword(getString("contextPath", null));
		}
		catch (Exception e) {
			LogUtil.log(ThreadLocalPageContext.getConfig(pageContext), Admin.class.getName(), e);
		}
		store();
	}

	private void doUpdateAPIKey() throws PageException {
		admin.updateAPIKey(getString("key", null));
		store();
	}

	private void doRemoveAPIKey() throws PageException {
		try {
			admin.removeAPIKey();
		}
		catch (Exception e) {
		}
		store();
	}

	private void doGetAPIKey() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), config.getIdentification().getApiKey());
	}

	private void doUpdateAuthKey() throws PageException {
		try {
			admin.updateAuthKey(getString("key", null));
		}
		catch (Exception e) {
		}
		store();
	}

	private void doRemoveAuthKey() throws PageException {
		try {
			admin.removeAuthKeys(getString("key", null));
		}
		catch (Exception e) {
		}
		store();
	}

	private void doListAuthKey() throws PageException {
		ConfigServerImpl cs = (ConfigServerImpl) config;
		pageContext.setVariable(getString("admin", action, "returnVariable"), Caster.toArray(cs.getAuthenticationKeys()));
	}

	/**
	 * @throws PageException
	 */
	private void doGetContexts() throws PageException {
		CFMLFactory[] factories;
		if (config instanceof ConfigServerImpl) {
			ConfigServerImpl cs = (ConfigServerImpl) config;
			factories = cs.getJSPFactories();
		}
		else {
			ConfigWebPro cw = (ConfigWebPro) config;
			factories = new CFMLFactory[] { cw.getFactory() };
		}

		lucee.runtime.type.Query qry = new QueryImpl(new Collection.Key[] { KeyConstants._path, KeyConstants._id, KeyConstants._hash, KeyConstants._label, HAS_OWN_SEC_CONTEXT,
				KeyConstants._url, CONFIG_FILE, CLIENT_SIZE, CLIENT_ELEMENTS, SESSION_SIZE, SESSION_ELEMENTS }, factories.length, getString("admin", action, "returnVariable"));
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
		ConfigWebPro cw;
		for (int i = 0; i < factories.length; i++) {
			int row = i + 1;
			CFMLFactoryImpl factory = (CFMLFactoryImpl) factories[i];
			cw = (ConfigWebPro) factory.getConfig();
			qry.setAtEL(KeyConstants._path, row, ReqRspUtil.getRootPath(factory.getConfig().getServletContext()));

			qry.setAtEL(CONFIG_FILE, row, factory.getConfig().getConfigFile().getAbsolutePath());
			if (factory.getURL() != null) qry.setAtEL(KeyConstants._url, row, factory.getURL().toExternalForm());

			qry.setAtEL(KeyConstants._id, row, factory.getConfig().getIdentification().getId());
			qry.setAtEL(KeyConstants._hash, row, SystemUtil.hash(factory.getConfig().getServletContext()));
			qry.setAtEL(KeyConstants._label, row, factory.getLabel());
			qry.setAtEL(HAS_OWN_SEC_CONTEXT, row, Caster.toBoolean(cw.hasIndividualSecurityManager()));

			setScopeDirInfo(qry, row, CLIENT_SIZE, CLIENT_ELEMENTS, cw.getClientScopeDir());
			setScopeDirInfo(qry, row, SESSION_SIZE, SESSION_ELEMENTS, cw.getSessionScopeDir());
		}
	}

	private void setScopeDirInfo(Query qry, int row, Key sizeName, Key elName, Resource dir) {
		qry.setAtEL(sizeName, row, Caster.toDouble(ResourceUtil.getRealSize(dir)));
		qry.setAtEL(elName, row, Caster.toDouble(ResourceUtil.getChildCount(dir)));
	}

	private void doHasIndividualSecurity() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"),
				Caster.toBoolean(pageContext.getConfig().getConfigServer(password).hasIndividualSecurityManager(getString("admin", action, "id")

				)));
	}

	private void doUpdateUpdate() throws PageException {
		admin.updateUpdate(getString("admin", action, "updatetype"), getString("admin", action, "updatelocation"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateSerial() throws PageException {
		admin.updateSerial(getString("admin", action, "serial"));
		store();
		pageContext.serverScope().reload();
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetSerial() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), config.getSerialNumber());
	}

	private Resource getContextDirectory() throws PageException {
		ConfigServerImpl cs = (ConfigServerImpl) ConfigWebUtil.getConfigServer(config, password);
		Resource dist = cs.getConfigDir().getRealResource("distribution");
		dist.mkdirs();
		return dist;
	}

	private void doGetPluginDirectory() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), config.getPluginDirectory().getAbsolutePath());
	}

	private void doUpdatePlugin() throws PageException, IOException {
		String strSrc = getString("admin", action, "source");
		Resource src = ResourceUtil.toResourceExisting(pageContext, strSrc);
		admin.updatePlugin(pageContext, src);
		store();
	}

	private void doUpdateLabel() throws PageException {
		if (config instanceof ConfigServer) {
			if (admin.updateLabel(getString("admin", action, "hash"), getString("admin", action, "label"))) {
				store();
				adminSync.broadcast(attributes, config);
			}
		}
	}

	private void doUpdateContext() throws PageException, IOException {
		String strSrc = getString("admin", action, "source");
		String strRealpath = getString("admin", action, "destination");
		Resource src = ResourceUtil.toResourceExisting(pageContext, strSrc);

		ConfigServerImpl server = (ConfigServerImpl) ConfigWebUtil.getConfigServer(config, password);
		Resource trg, p;
		Resource deploy = server.getConfigDir().getRealResource("web-context-deployment");
		deploy.mkdirs();

		// deploy it
		trg = deploy.getRealResource(strRealpath);
		if (trg.exists()) trg.remove(true);
		p = trg.getParentResource();
		if (!p.isDirectory()) p.createDirectory(true);
		src.copyTo(trg, false);
		store();

		ConfigWeb[] webs = server.getConfigWebs();
		for (int i = 0; i < webs.length; i++) {
			ConfigWebUtil.deployWebContext(server, webs[i], true);
		}
	}

	private void doRemoveContext() throws PageException {
		String strRealpath = getString("admin", action, "destination");
		ConfigServerImpl server = (ConfigServerImpl) config;

		try {
			admin.removeContext(server, true, config.getLog("deploy"), strRealpath);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}

		store();
	}

	private void doRemovePlugin() throws PageException, IOException {
		Resource dir = config.getPluginDirectory();
		String name = getString("admin", action, "name");
		Resource trgDir = dir.getRealResource(name);
		trgDir.remove(true);

		store();
	}

	private void doGetPlugins() throws PageException {
		Resource dir = config.getPluginDirectory();

		String[] list = dir.list(new ConfigAdmin.PluginFilter());
		lucee.runtime.type.Query qry = new QueryImpl(new Collection.Key[] { KeyConstants._name }, list.length, getString("admin", action, "returnVariable"));
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);

		for (int i = 0; i < list.length; i++) {
			int row = i + 1;
			qry.setAtEL(KeyConstants._name, row, list[i]);
		}
	}

	private void doStorageSet() throws PageException {
		try {
			admin.storageSet(config, getString("admin", action, "key"), getObject("admin", action, "value"));
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void doStorageGet() throws PageException {
		try {
			pageContext.setVariable(getString("admin", action, "returnVariable"), admin.storageGet(config, getString("admin", action, "key")));
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetDefaultPassword() throws PageException {
		Password password = admin.getDefaultPassword();

		pageContext.setVariable(getString("admin", action, "returnVariable"), password == null ? "" : password.getPassword());
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateDefaultPassword() throws PageException {
		try {
			admin.updateDefaultPassword(getString("admin", action, "newPassword"));
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		store();
	}

	private void doRemoveDefaultPassword() throws PageException {
		admin.removeDefaultPassword();
		store();
	}

	/*
	 * *
	 * 
	 * @throws PageException
	 * 
	 * / private void doUpdatePassword() throws PageException { try {
	 * ConfigWebAdmin.setPassword(config,password==null?null:Caster.toString(password),getString("admin"
	 * ,action,"newPassword")); } catch (Exception e) { throw Caster.toPageException(e); } //store(); }
	 */

	private void doGetSecurity() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

		sct.set("varUsage", AppListenerUtil.toVariableUsage(config.getQueryVarUsage(), "ignore"));
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetDebug() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

		sct.set(DEBUG, Caster.toBoolean(config.debug()));
		sct.set(KeyConstants._database, Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_DATABASE)));
		sct.set(KeyConstants._exception, Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_EXCEPTION)));
		sct.set(KeyConstants._template, Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)));
		sct.set("tracing", Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_TRACING)));
		sct.set(KeyConstants._dump, Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_DUMP)));
		sct.set("timer", Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_TIMER)));
		sct.set("implicitAccess", Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS)));
		sct.set("queryUsage", Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)));
	}

	private void doGetError() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		// sct.set("errorTemplate",config.getErrorTemplate());

		Struct templates = new StructImpl();
		Struct str = new StructImpl();
		sct.set(TEMPLATES, templates);
		sct.set(STR, str);
		sct.set(DO_STATUS_CODE, Caster.toBoolean(config.getErrorStatusCode()));

		// 500
		String template = config.getErrorTemplate(500);
		try {
			PageSource ps = ((PageContextImpl) pageContext).getPageSourceExisting(template);
			if (ps != null) templates.set("500", ps.getDisplayPath());
			else templates.set("500", "");
		}
		catch (PageException e) {
			templates.set("500", "");
		}
		str.set("500", template);

		// 404
		template = config.getErrorTemplate(404);
		try {
			PageSource ps = ((PageContextImpl) pageContext).getPageSourceExisting(template);
			if (ps != null) templates.set("404", ps.getDisplayPath());
			else templates.set("404", "");
		}
		catch (PageException e) {
			templates.set("404", "");
		}
		str.set("404", template);

	}

	private void doGetRegex() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set(KeyConstants._type, config.getRegex().getTypeName());
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetDebugData() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), pageContext.getConfig().debug() ? pageContext.getDebugger().getDebuggingData(pageContext) : null);
	}

	private void doGetLoggedDebugData() throws PageException {
		if (config instanceof ConfigServer) return;

		ConfigWebPro cw = (ConfigWebPro) config;
		String id = getString("id", null);
		Array data = cw.getDebuggerPool().getData(pageContext);

		if (StringUtil.isEmpty(id)) {
			pageContext.setVariable(getString("admin", action, "returnVariable"), data);
		}
		else {
			Iterator<Object> it = data.valueIterator();
			Struct sct;
			while (it.hasNext()) {
				sct = (Struct) it.next();
				if (Operator.equalsEL(id, sct.get(KeyConstants._id, ""), false, true)) {
					pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
					return;
				}
			}
			throw new ApplicationException("No debugging data with id [" + id + "] found.");
		}
	}

	private void doPurgeDebugPool() throws PageException {
		if (config instanceof ConfigServer) return;
		ConfigWebPro cw = (ConfigWebPro) config;
		cw.getDebuggerPool().purge();
	}

	private void doGetInfo() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

		if (config instanceof ConfigWebPro) {
			ConfigWebPro cw = (ConfigWebPro) config;
			sct.setEL(KeyConstants._id, cw.getIdentification().getId());
			sct.setEL(KeyConstants._label, cw.getLabel());
			sct.setEL(KeyConstants._hash, cw.getHash());

			sct.setEL(KeyConstants._root, cw.getRootDirectory().getAbsolutePath());
			sct.setEL("configServerDir", cw.getConfigServerDir().getAbsolutePath());
			sct.setEL("configWebDir", cw.getConfigDir().getAbsolutePath());
		}
		else {
			sct.setEL("configServerDir", config.getConfigDir().getAbsolutePath());
			sct.setEL("configWebDir", pageContext.getConfig().getConfigDir().getAbsolutePath());
		}

		sct.setEL(KeyConstants._config, config.getConfigFile().getAbsolutePath());

		// Servlets
		if (config instanceof ConfigServer) {
			ConfigServer cs = (ConfigServer) config;
			CFMLEngineImpl engine = (CFMLEngineImpl) cs.getCFMLEngine();
			Struct srv = new StructImpl(), params;

			ServletConfig[] configs = engine.getServletConfigs();
			ServletConfig sc;
			Enumeration e;
			String name, value;
			for (int i = 0; i < configs.length; i++) {
				sc = configs[i];
				e = sc.getInitParameterNames();
				params = new StructImpl();
				while (e.hasMoreElements()) {
					name = (String) e.nextElement();
					value = sc.getInitParameter(name);
					params.set(name, value);
				}
				srv.set(sc.getServletName(), params);
			}
			sct.set(KeyConstants._servlets, srv);
		}

		// sct.setEL("javaAgentSupported", Caster.toBoolean(InstrumentationUtil.isSupported()));
		sct.setEL("javaAgentSupported", Boolean.TRUE);
		// sct.setEL("javaAgentPath", ClassUtil.getSourcePathForClass("lucee.runtime.instrumentation.Agent",
		// ""));
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doCreateSecurityManager() throws PageException {
		admin.createSecurityManager(password, getString("admin", action, "id"));
		store();
	}

	private void doRemoveSecurityManager() throws PageException {
		admin.removeSecurityManager(password, getString("admin", action, "id"));
		store();
	}

	private short fb(String key) throws PageException {
		return getBool("admin", action, key) ? SecurityManager.VALUE_YES : SecurityManager.VALUE_NO;
	}

	private short fb2(String key) throws PageException {
		return SecurityManagerImpl.toShortAccessRWValue(getString("admin", action, key));
	}

	private void doUpdateDefaultSecurityManager() throws PageException {

		admin.updateDefaultSecurity(fb("setting"), SecurityManagerImpl.toShortAccessValue(getString("admin", action, "file")), getFileAcces(), fb("direct_java_access"), fb("mail"),
				SecurityManagerImpl.toShortAccessValue(getString("admin", action, "datasource")), fb("mapping"), fb("remote"), fb("custom_tag"), fb("cfx_setting"), fb("cfx_usage"),
				fb("debugging"), fb("search"), fb("scheduled_task"), fb("tag_execute"), fb("tag_import"), fb("tag_object"), fb("tag_registry"), fb("cache"), fb("gateway"),
				fb("orm"), fb2("access_read"), fb2("access_write"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private Resource[] getFileAcces() throws PageException {
		Object value = attributes.get(FILE_ACCESS, null);
		if (value == null) return null;
		Array arr = Caster.toArray(value);
		List<Resource> rtn = new ArrayList<Resource>();
		Iterator it = arr.valueIterator();
		String path;
		Resource res;
		while (it.hasNext()) {
			path = Caster.toString(it.next());
			if (StringUtil.isEmpty(path)) continue;

			res = config.getResource(path);
			if (!res.exists()) throw new ApplicationException("Path [" + path + "] does not exist");
			if (!res.isDirectory()) throw new ApplicationException("Path [" + path + "] is not a directory");
			rtn.add(res);
		}
		return rtn.toArray(new Resource[rtn.size()]);
	}

	private void doUpdateSecurityManager() throws PageException {
		admin.updateSecurity(getString("admin", action, "id"), fb("setting"), SecurityManagerImpl.toShortAccessValue(getString("admin", action, "file")), getFileAcces(),
				fb("direct_java_access"), fb("mail"), SecurityManagerImpl.toShortAccessValue(getString("admin", action, "datasource")), fb("mapping"), fb("remote"),
				fb("custom_tag"), fb("cfx_setting"), fb("cfx_usage"), fb("debugging"), fb("search"), fb("scheduled_task"), fb("tag_execute"), fb("tag_import"), fb("tag_object"),
				fb("tag_registry"), fb("cache"), fb("gateway"), fb("orm"), fb2("access_read"), fb2("access_write"));
		store();
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetDefaultSecurityManager() throws PageException {
		ConfigServer cs = ConfigWebUtil.getConfigServer(config, password);

		SecurityManager dsm = cs.getDefaultSecurityManager();
		_fillSecData(dsm);
	}

	private void doGetSecurityManager() throws PageException {
		ConfigServer cs = ConfigWebUtil.getConfigServer(config, password);
		SecurityManager sm = cs.getSecurityManager(getString("admin", action, "id"));
		_fillSecData(sm);
	}

	private void _fillSecData(SecurityManager sm) throws PageException {

		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("cfx_setting", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_CFX_SETTING) == SecurityManager.VALUE_YES));
		sct.set("cfx_usage", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_CFX_USAGE) == SecurityManager.VALUE_YES));
		sct.set("custom_tag", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_CUSTOM_TAG) == SecurityManager.VALUE_YES));
		sct.set(KeyConstants._datasource, _fillSecDataDS(sm.getAccess(SecurityManager.TYPE_DATASOURCE)));
		sct.set("debugging", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_DEBUGGING) == SecurityManager.VALUE_YES));
		sct.set("direct_java_access", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) == SecurityManager.VALUE_YES));
		sct.set("mail", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_MAIL) == SecurityManager.VALUE_YES));
		sct.set(KeyConstants._mapping, Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_MAPPING) == SecurityManager.VALUE_YES));
		sct.set("remote", Caster.toBoolean(sm.getAccess(SecurityManagerImpl.TYPE_REMOTE) == SecurityManager.VALUE_YES));
		sct.set("setting", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_SETTING) == SecurityManager.VALUE_YES));
		sct.set("search", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_SEARCH) == SecurityManager.VALUE_YES));
		sct.set("scheduled_task", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_SCHEDULED_TASK) == SecurityManager.VALUE_YES));
		sct.set(KeyConstants._cache, Caster.toBoolean(sm.getAccess(SecurityManagerImpl.TYPE_CACHE) == SecurityManager.VALUE_YES));
		sct.set("gateway", Caster.toBoolean(sm.getAccess(SecurityManagerImpl.TYPE_GATEWAY) == SecurityManager.VALUE_YES));
		sct.set(KeyConstants._orm, Caster.toBoolean(sm.getAccess(SecurityManagerImpl.TYPE_ORM) == SecurityManager.VALUE_YES));

		sct.set("tag_execute", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_TAG_EXECUTE) == SecurityManager.VALUE_YES));
		sct.set("tag_import", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_TAG_IMPORT) == SecurityManager.VALUE_YES));
		sct.set("tag_object", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_TAG_OBJECT) == SecurityManager.VALUE_YES));
		sct.set("tag_registry", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_TAG_REGISTRY) == SecurityManager.VALUE_YES));
		sct.set("access_read", SecurityManagerImpl.toStringAccessRWValue(sm.getAccess(SecurityManager.TYPE_ACCESS_READ)));
		sct.set("access_write", SecurityManagerImpl.toStringAccessRWValue(sm.getAccess(SecurityManager.TYPE_ACCESS_WRITE)));
		short accessFile = sm.getAccess(SecurityManager.TYPE_FILE);
		String str = SecurityManagerImpl.toStringAccessValue(accessFile);
		if (str.equals("yes")) str = "all";
		sct.set(KeyConstants._file, str);

		Array arr = new ArrayImpl();
		if (accessFile != SecurityManager.VALUE_ALL) {
			Resource[] reses = ((SecurityManagerImpl) sm).getCustomFileAccess();
			for (int i = 0; i < reses.length; i++) {
				arr.appendEL(reses[i].getAbsolutePath());
			}
		}
		sct.set("file_access", arr);

	}

	private Double _fillSecDataDS(short access) {
		switch (access) {
		case SecurityManager.VALUE_YES:
			return Caster.toDouble(-1);
		case SecurityManager.VALUE_NO:
			return Caster.toDouble(0);
		case SecurityManager.VALUE_1:
			return Caster.toDouble(1);
		case SecurityManager.VALUE_2:
			return Caster.toDouble(2);
		case SecurityManager.VALUE_3:
			return Caster.toDouble(3);
		case SecurityManager.VALUE_4:
			return Caster.toDouble(4);
		case SecurityManager.VALUE_5:
			return Caster.toDouble(5);
		case SecurityManager.VALUE_6:
			return Caster.toDouble(6);
		case SecurityManager.VALUE_7:
			return Caster.toDouble(7);
		case SecurityManager.VALUE_8:
			return Caster.toDouble(8);
		case SecurityManager.VALUE_9:
			return Caster.toDouble(9);
		case SecurityManager.VALUE_10:
			return Caster.toDouble(10);
		}
		return Caster.toDouble(-1);
	}

	private void doUpdateSecurity() throws PageException {

		admin.updateSecurity(getString("varUsage", ""));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateDebug() throws PageException {
		admin.updateDebug(Caster.toBoolean(getString("debug", ""), null), Caster.toBoolean(getString("template", ""), null), Caster.toBoolean(getString("database", ""), null),
				Caster.toBoolean(getString("exception", ""), null), Caster.toBoolean(getString("tracing", ""), null), Caster.toBoolean(getString("dump", ""), null),
				Caster.toBoolean(getString("timer", ""), null), Caster.toBoolean(getString("implicitAccess", ""), null), Caster.toBoolean(getString("queryUsage", ""), null));

		store();
		adminSync.broadcast(attributes, config);
		if (!Caster.toBoolean(getString("debug", ""))) doPurgeDebugPool(); // purge the debug log pool when disabling debug to free up memory
	}

	private void doGetDebugSetting() throws PageException {
		Struct sct = new StructImpl();
		sct.set("maxLogs", Caster.toDouble(config.getDebugMaxRecordsLogged()));
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

	}

	private void doUpdateDebugSetting() throws PageException {
		String str = getString("admin", action, "maxLogs");
		int maxLogs;
		if (StringUtil.isEmpty(str, true)) maxLogs = -1;
		else maxLogs = Caster.toIntValue(str);
		admin.updateDebugSetting(maxLogs);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateDebugEntry() throws PageException {
		try {
			admin.updateDebugEntry(getString("admin", "updateDebugEntry", "debugtype"), getString("admin", "updateDebugEntry", "iprange"),
					getString("admin", "updateDebugEntry", "label"), getString("admin", "updateDebugEntry", "path"), getString("admin", "updateDebugEntry", "fullname"),
					getStruct("admin", "updateDebugEntry", "custom"));
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}

		store();
		adminSync.broadcast(attributes, config);
	}

	private void doGetDebugEntry() throws PageException {
		DebugEntry[] entries = config.getDebugEntries();

		String rtn = getString("admin", action, "returnVariable");
		lucee.runtime.type.Query qry = new QueryImpl(new Collection.Key[] { KeyConstants._id, LABEL, IP_RANGE, READONLY, KeyConstants._type, CUSTOM }, entries.length, rtn);
		pageContext.setVariable(rtn, qry);
		DebugEntry de;
		for (int i = 0; i < entries.length; i++) {
			int row = i + 1;
			de = entries[i];
			qry.setAtEL(KeyConstants._id, row, de.getId());
			qry.setAtEL(LABEL, row, de.getLabel());
			qry.setAtEL(IP_RANGE, row, de.getIpRangeAsString());
			qry.setAtEL(KeyConstants._type, row, de.getType());
			qry.setAtEL(READONLY, row, Caster.toBoolean(de.isReadOnly()));
			qry.setAtEL(CUSTOM, row, de.getCustom());
		}
	}

	private void doUpdateError() throws PageException {

		admin.updateErrorTemplate(500, getString("admin", action, "template500"));
		admin.updateErrorTemplate(404, getString("admin", action, "template404"));
		admin.updateErrorStatusCode(getBoolObject("admin", action, "statuscode"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateRegex() throws PageException {

		admin.updateRegexType(getString("admin", action, "regextype"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateJavaCFX() throws PageException {
		String name = getString("admin", action, "name");
		if (StringUtil.startsWithIgnoreCase(name, "cfx_")) name = name.substring(4);
		lucee.runtime.db.ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());
		admin.updateJavaCFX(name, cd);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doVerifyJavaCFX() throws PageException {
		String name = getString("admin", action, "name");
		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());
		admin.verifyJavaCFX(name, cd);
	}

	private void doVerifyCFX() throws PageException {
		String name = getString("admin", action, "name");
		if (StringUtil.startsWithIgnoreCase(name, "cfx_")) name = name.substring(4);
		admin.verifyCFX(name);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doRemoveCFX() throws PageException {
		admin.removeCFX(getString("admin", action, "name"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveExtension() throws PageException {
		admin.removeExtension(getString("admin", action, "provider"), getString("admin", action, "id"));
		store();
		// adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetJavaCFXTags() throws PageException {
		Map map = config.getCFXTagPool().getClasses();
		lucee.runtime.type.Query qry = new QueryImpl(new Collection.Key[] { KeyConstants._displayname, KeyConstants._sourcename, KeyConstants._readonly, KeyConstants._name,
				KeyConstants._class, KeyConstants._bundleName, KeyConstants._bundleVersion, KeyConstants._isvalid }, 0, "query");
		Iterator it = map.keySet().iterator();

		int row = 0;
		while (it.hasNext()) {
			CFXTagClass tag = (CFXTagClass) map.get(it.next());
			if (tag instanceof JavaCFXTagClass) {
				row++;
				qry.addRow(1);
				JavaCFXTagClass jtag = (JavaCFXTagClass) tag;
				qry.setAt(KeyConstants._displayname, row, tag.getDisplayType());
				qry.setAt(KeyConstants._sourcename, row, tag.getSourceName());
				qry.setAt(KeyConstants._readonly, row, Caster.toBoolean(tag.isReadOnly()));
				qry.setAt(KeyConstants._isvalid, row, Caster.toBoolean(tag.isValid()));
				qry.setAt(KeyConstants._name, row, jtag.getName());
				qry.setAt(KeyConstants._class, row, jtag.getClassDefinition().getClassName());
				qry.setAt(KeyConstants._bundleName, row, jtag.getClassDefinition().getName());
				qry.setAt(KeyConstants._bundleVersion, row, jtag.getClassDefinition().getVersionAsString());
			}

		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetCFXTags() throws PageException {
		Map map = config.getCFXTagPool().getClasses();
		lucee.runtime.type.Query qry = new QueryImpl(
				new String[] { "displayname", "sourcename", "readonly", "isvalid", "name", "procedure_class", "procedure_bundleName", "procedure_bundleVersion", "keep_alive" },
				map.size(), "query");
		Iterator it = map.keySet().iterator();

		int row = 0;
		while (it.hasNext()) {
			row++;
			CFXTagClass tag = (CFXTagClass) map.get(it.next());

			qry.setAt("displayname", row, tag.getDisplayType());
			qry.setAt("sourcename", row, tag.getSourceName());
			qry.setAt("readonly", row, Caster.toBoolean(tag.isReadOnly()));
			qry.setAt("isvalid", row, Caster.toBoolean(tag.isValid()));

			if (tag instanceof JavaCFXTagClass) {
				JavaCFXTagClass jtag = (JavaCFXTagClass) tag;
				qry.setAt(KeyConstants._name, row, jtag.getName());
				qry.setAt("procedure_class", row, jtag.getClassDefinition().getClassName());
				qry.setAt("procedure_bundleName", row, jtag.getClassDefinition().getName());
				qry.setAt("procedure_bundleVersion", row, jtag.getClassDefinition().getVersionAsString());
			}

		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	/**
	 * @throws PageException
	 */
	private void doUpdateComponentMapping() throws PageException {
		admin.updateComponentMapping(getString("virtual", ""), getString("physical", ""), getString("archive", ""), getString("primary", "physical"),
				ConfigWebUtil.inspectTemplate(getString("inspect", ""), ConfigPro.INSPECT_UNDEFINED));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doRemoveComponentMapping() throws PageException {
		admin.removeComponentMapping(getString("admin", action, "virtual"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 */
	private void doUpdateCustomTag() throws PageException {
		admin.updateCustomTag(getString("admin", action, "virtual"), getString("admin", action, "physical"), getString("admin", action, "archive"),
				getString("admin", action, "primary"), ConfigWebUtil.inspectTemplate(getString("inspect", ""), ConfigPro.INSPECT_UNDEFINED));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doRemoveCustomTag() throws PageException {
		admin.removeCustomTag(getString("admin", action, "virtual"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetCustomTagMappings() throws PageException {
		Mapping[] mappings = config.getCustomTagMappings();
		lucee.runtime.type.Query qry = new QueryImpl(
				new String[] { "archive", "strarchive", "physical", "strphysical", "virtual", "hidden", "physicalFirst", "readonly", "inspect" }, mappings.length, "query");

		for (int i = 0; i < mappings.length; i++) {
			MappingImpl m = (MappingImpl) mappings[i];
			int row = i + 1;
			qry.setAt("archive", row, m.getArchive());
			qry.setAt("strarchive", row, m.getStrArchive());
			qry.setAt("physical", row, m.getPhysical());
			qry.setAt("strphysical", row, m.getStrPhysical());
			qry.setAt("virtual", row, m.getVirtual());
			qry.setAt("hidden", row, Caster.toBoolean(m.isHidden()));
			qry.setAt("physicalFirst", row, Caster.toBoolean(m.isPhysicalFirst()));
			qry.setAt("readonly", row, Caster.toBoolean(m.isReadonly()));
			qry.setAt("inspect", row, ConfigWebUtil.inspectTemplate(m.getInspectTemplateRaw(), ""));
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetComponentMappings() throws PageException {
		Mapping[] mappings = config.getComponentMappings();
		lucee.runtime.type.Query qry = new QueryImpl(
				new String[] { "archive", "strarchive", "physical", "strphysical", "virtual", "hidden", "physicalFirst", "readonly", "inspect" }, mappings.length, "query");

		for (int i = 0; i < mappings.length; i++) {
			MappingImpl m = (MappingImpl) mappings[i];
			int row = i + 1;
			qry.setAt("archive", row, m.getArchive());
			qry.setAt("strarchive", row, m.getStrArchive());
			qry.setAt("physical", row, m.getPhysical());
			qry.setAt("strphysical", row, m.getStrPhysical());
			qry.setAt("virtual", row, m.getVirtual());
			qry.setAt("hidden", row, Caster.toBoolean(m.isHidden()));
			qry.setAt("physicalFirst", row, Caster.toBoolean(m.isPhysicalFirst()));
			qry.setAt("readonly", row, Caster.toBoolean(m.isReadonly()));
			qry.setAt("inspect", row, ConfigWebUtil.inspectTemplate(m.getInspectTemplateRaw(), ""));
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doRemoveMapping() throws PageException {
		admin.removeMapping(getString("admin", action, "virtual"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateRestMapping() throws PageException {
		admin.updateRestMapping(getString("admin", action, "virtual"), getString("admin", action, "physical"), getBool("admin", action, "default"));
		store();
		adminSync.broadcast(attributes, config);

		RestUtil.release(config.getRestMappings());
	}

	private void doRemoveRestMapping() throws PageException {
		admin.removeRestMapping(getString("admin", action, "virtual"));
		store();
		adminSync.broadcast(attributes, config);
		RestUtil.release(config.getRestMappings());
	}

	private void doUpdateMapping() throws PageException {
		admin.updateMapping(getString("admin", action, "virtual"), getString("admin", action, "physical"), getString("admin", action, "archive"),
				getString("admin", action, "primary"), ConfigWebUtil.inspectTemplate(getString("inspect", ""), ConfigPro.INSPECT_UNDEFINED),
				Caster.toBooleanValue(getString("toplevel", "true")), ConfigWebUtil.toListenerMode(getString("listenerMode", ""), -1),
				ConfigWebUtil.toListenerType(getString("listenerType", ""), -1), Caster.toBooleanValue(getString("readonly", "false"))

		);
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetMapping() throws PageException {

		Mapping[] mappings = config.getMappings();
		Struct sct = new StructImpl();
		String virtual = getString("admin", action, "virtual");

		for (int i = 0; i < mappings.length; i++) {
			MappingImpl m = (MappingImpl) mappings[i];
			if (!m.getVirtual().equals(virtual)) continue;

			sct.set("archive", m.getArchive());
			sct.set("strarchive", m.getStrArchive());
			sct.set("physical", m.getPhysical());
			sct.set("strphysical", m.getStrPhysical());
			sct.set("virtual", m.getVirtual());
			sct.set(KeyConstants._hidden, Caster.toBoolean(m.isHidden()));
			sct.set("physicalFirst", Caster.toBoolean(m.isPhysicalFirst()));
			sct.set("readonly", Caster.toBoolean(m.isReadonly()));
			sct.set("inspect", ConfigWebUtil.inspectTemplate(m.getInspectTemplateRaw(), ""));
			sct.set("toplevel", Caster.toBoolean(m.isTopLevel()));

			pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
			return;
		}
		throw new ApplicationException("there is no mapping with virtual [" + virtual + "]");
	}

	private void doGetRHExtensionProviders() throws PageException {
		RHExtensionProvider[] providers = config.getRHExtensionProviders();
		lucee.runtime.type.Query qry = new QueryImpl(new Key[] { KeyConstants._url, KeyConstants._readonly }, providers.length, "query");

		RHExtensionProvider provider;
		for (int i = 0; i < providers.length; i++) {
			provider = providers[i];
			int row = i + 1;
			qry.setAt(KeyConstants._url, row, provider.getURL().toExternalForm());
			qry.setAt(KeyConstants._readonly, row, provider.isReadonly());
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetExtensionInfo() throws PageException {
		Resource ed = config.getExtensionDirectory();
		Struct sct = new StructImpl();
		sct.set(KeyConstants._directory, ed.getPath());
		sct.set(KeyConstants._enabled, Caster.toBoolean(config.isExtensionEnabled()));

		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
	}

	/*
	 * private void doGetExtensions() throws PageException { Extension[] extensions =
	 * config.getExtensions(); lucee.runtime.type.Query qry = new QueryImpl(new String[] { "type",
	 * "provider", "id", "config", "version", "category", "description", "image", "label", "name",
	 * "author", "codename", "video", "support", "documentation", "forum", "mailinglist", "network",
	 * "created" }, 0, "query");
	 * 
	 * String provider = getString("provider", null); String id = getString("id", null); Extension
	 * extension; String extProvider, extId; int row = 0; for (int i = 0; i < extensions.length; i++) {
	 * extension = extensions[i]; if(!extension.getType().equalsIgnoreCase("all") &&
	 * toType(extension.getType(), false) != type) continue;
	 * 
	 * extProvider = extension.getProvider(); extId = extension.getId(); if(provider != null &&
	 * !provider.equalsIgnoreCase(extProvider)) continue; if(id != null && !id.equalsIgnoreCase(extId))
	 * continue;
	 * 
	 * qry.addRow(); row++; qry.setAt("provider", row, extProvider); qry.setAt(KeyConstants._id, row,
	 * extId); qry.setAt(KeyConstants._config, row, extension.getConfig(pageContext));
	 * qry.setAt(KeyConstants._version, row, extension.getVersion());
	 * 
	 * qry.setAt("category", row, extension.getCategory()); qry.setAt(KeyConstants._description, row,
	 * extension.getDescription()); qry.setAt("image", row, extension.getImage());
	 * qry.setAt(KeyConstants._label, row, extension.getLabel()); qry.setAt(KeyConstants._name, row,
	 * extension.getName());
	 * 
	 * qry.setAt(KeyConstants._author, row, extension.getAuthor()); qry.setAt("codename", row,
	 * extension.getCodename()); qry.setAt("video", row, extension.getVideo()); qry.setAt("support",
	 * row, extension.getSupport()); qry.setAt("documentation", row, extension.getDocumentation());
	 * qry.setAt("forum", row, extension.getForum()); qry.setAt("mailinglist", row,
	 * extension.getMailinglist()); qry.setAt("network", row, extension.getNetwork());
	 * qry.setAt(KeyConstants._created, row, extension.getCreated()); qry.setAt(KeyConstants._type, row,
	 * extension.getType());
	 * 
	 * } pageContext.setVariable(getString("admin", action, "returnVariable"), qry); }
	 */

	private void doGetMappings() throws PageException {

		Mapping[] mappings = config.getMappings();
		lucee.runtime.type.Query qry = new QueryImpl(
				new String[] { "archive", "strarchive", "physical", "strphysical", "virtual", "hidden", "physicalFirst", "readonly", "inspect", "toplevel" }, mappings.length,
				"query");

		for (int i = 0; i < mappings.length; i++) {
			MappingImpl m = (MappingImpl) mappings[i];
			int row = i + 1;
			qry.setAt("archive", row, m.getArchive());
			qry.setAt("strarchive", row, m.getStrArchive());
			qry.setAt("physical", row, m.getPhysical());
			qry.setAt("strphysical", row, m.getStrPhysical());
			qry.setAt("virtual", row, m.getVirtual());
			qry.setAt("hidden", row, Caster.toBoolean(m.isHidden()));
			qry.setAt("physicalFirst", row, Caster.toBoolean(m.isPhysicalFirst()));
			qry.setAt("readonly", row, Caster.toBoolean(m.isReadonly()));
			qry.setAt("inspect", row, ConfigWebUtil.inspectTemplate(m.getInspectTemplateRaw(), ""));
			qry.setAt("toplevel", row, Caster.toBoolean(m.isTopLevel()));
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetRestMappings() throws PageException {

		lucee.runtime.rest.Mapping[] mappings = config.getRestMappings();
		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "physical", "strphysical", "virtual", "hidden", "readonly", "default" }, mappings.length, "query");

		lucee.runtime.rest.Mapping m;
		for (int i = 0; i < mappings.length; i++) {
			m = mappings[i];
			int row = i + 1;
			qry.setAt("physical", row, m.getPhysical());
			qry.setAt("strphysical", row, m.getStrPhysical());
			qry.setAt("virtual", row, m.getVirtual());
			qry.setAt("hidden", row, Caster.toBoolean(m.isHidden()));
			qry.setAt("readonly", row, Caster.toBoolean(m.isReadonly()));
			qry.setAt("default", row, Caster.toBoolean(m.isDefault()));
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetRestSettings() throws PageException {
		Struct sct = new StructImpl();
		sct.set(KeyConstants._list, Caster.toBoolean(config.getRestList()));
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

	}

	private void doGetResourceProviders() throws PageException {

		pageContext.setVariable(getString("admin", action, "returnVariable"), admin.getResourceProviders());
	}

	private void doUpdateAdminSyncClass() throws PageException {
		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());
		admin.updateAdminSyncClass(cd);
		store();
	}

	private void doGetAdminSyncClass() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), config.getAdminSyncClass().getName());
	}

	private void doUpdateVideoExecuterClass() throws PageException {
		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());
		admin.updateVideoExecuterClass(cd);
		store();
	}

	private void doGetVideoExecuterClass() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), config.getVideoExecuterClass().getName());
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doRemoveMailServer() throws PageException {
		admin.removeMailServer(getString("admin", action, "hostname"), getString("username", null));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateMailServer() throws PageException {

		admin.updateMailServer(getInt("id", -1), getString("admin", action, "hostname"), getString("admin", action, "dbusername"), getString("admin", action, "dbpassword"),
				Caster.toIntValue(getString("admin", action, "port")), getBoolV("tls", false), getBoolV("ssl", false), toTimeout(getObject("life", null), 1000 * 60 * 5),
				toTimeout(getObject("idle", null), 1000 * 60 * 5), getBoolV("reuseConnection", true)

		);
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateMailSetting() throws PageException {
		// admin.setMailLog(getString("admin",action,"logfile"),getString("loglevel","ERROR"));

		admin.setMailSpoolEnable(getBoolObject("admin", action, "spoolenable"));

		/*
		 * / spool interval String str=getString("admin",action,"maxThreads"); Integer i=null;
		 * if(!StringUtil.isEmpty(str))i=Caster.toInteger(maxThreads);
		 */

		// timeout
		String str = getString("admin", action, "timeout");
		Integer i = null;
		if (!StringUtil.isEmpty(str)) i = Caster.toInteger(str);
		admin.setMailTimeout(i);

		admin.setMailDefaultCharset(getString("admin", action, "defaultencoding"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateTaskSetting() throws PageException {

		// max Threads
		String str = getString("admin", action, "maxThreads");
		Integer i = null;
		if (!StringUtil.isEmpty(str)) {
			i = Caster.toInteger(str);
			if (i.intValue() < 10) throw new ApplicationException("We need at least 10 threads to run tasks properly");
		}
		admin.setTaskMaxThreads(i);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void listPatches() throws PageException {
		try {

			pageContext.setVariable(getString("admin", action, "returnVariable"), Caster.toArray(((ConfigServerImpl) config).getInstalledPatches()));
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void getMinVersion() throws PageException {
		try {

			pageContext.setVariable(getString("admin", action, "returnVariable"), VersionInfo.getIntVersion().toString());
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void getLoaderInfo() throws PageException {
		try {
			Struct sct = new StructImpl();
			sct.set("LoaderVersion", VersionInfo.getIntVersion().toString());
			sct.set("LuceeVersion", pageContext.getConfig().getFactory().getEngine().getInfo().getVersion().toString());
			sct.set("LoaderPath", ClassUtil.getSourcePathForClass("lucee.loader.servlet.CFMLServlet", ""));
			pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void doGetMailServers() throws PageException {

		Server[] servers = config.getMailServers();
		lucee.runtime.type.Query qry = new QueryImpl(
				new String[] { "id", "hostname", "password", "passwordEncrypted", "username", "port", "authentication", "readonly", "tls", "ssl", "life", "idle", "type" },
				servers.length, "query");

		for (int i = 0; i < servers.length; i++) {
			Server s = servers[i];
			int row = i + 1;
			qry.setAt("id", row, s instanceof ServerImpl ? ((ServerImpl) s).getId() : -1);
			qry.setAt("hostname", row, s.getHostName());
			qry.setAt("password", row, s.isReadOnly() ? "" : s.getPassword());
			qry.setAt("passwordEncrypted", row, s.isReadOnly() ? "" : ConfigWebUtil.encrypt(s.getPassword()));
			qry.setAt("username", row, s.isReadOnly() ? "" : s.getUsername());
			qry.setAt("port", row, Caster.toInteger(s.getPort()));
			qry.setAt("readonly", row, Caster.toBoolean(s.isReadOnly()));
			qry.setAt("authentication", row, Caster.toBoolean(s.hasAuthentication()));
			qry.setAt("ssl", row, Caster.toBoolean(s.isSSL()));
			qry.setAt("tls", row, Caster.toBoolean(s.isTLS()));
			if (s instanceof ServerImpl) {
				ServerImpl si = (ServerImpl) s;
				qry.setAt("type", row, si.getType() == ServerImpl.TYPE_GLOBAL ? "global" : "local");
				qry.setAt("life", row, (si.getLifeTimeSpan() / 1000));
				qry.setAt("idle", row, (si.getIdleTimeSpan() / 1000));
			}
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetRunningThreads() throws PageException {
		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "Id", "Start", "Timeout", "ThreadType", "StackTrace", "TagContext", "Label", "RootPath", "ConfigFile", "URL" },
				0, "query");

		if (type == TYPE_WEB) {
			fillGetRunningThreads(qry, pageContext.getConfig());
		}
		else {
			ConfigServer cs = pageContext.getConfig().getConfigServer(password);
			ConfigWeb[] webs = cs.getConfigWebs();
			for (int i = 0; i < webs.length; i++) {
				fillGetRunningThreads(qry, webs[i]);
			}
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private static void fillGetRunningThreads(lucee.runtime.type.Query qry, ConfigWeb configWeb) throws PageException {
		CFMLFactoryImpl factory = ((CFMLFactoryImpl) configWeb.getFactory());
		Map<Integer, PageContextImpl> pcs = factory.getActivePageContexts();
		Iterator<PageContextImpl> it = pcs.values().iterator();
		PageContextImpl pc;
		Collection.Key key;
		int row = 0;
		while (it.hasNext()) {
			pc = it.next();
			qry.addRow();
			row++;
			StackTraceElement[] st = pc.getThread().getStackTrace();
			configWeb.getConfigDir();
			configWeb.getIdentification().getId();
			configWeb.getConfigDir();
			qry.setAt("Id", row, new Double(pc.getId()));
			qry.setAt("Start", row, new DateTimeImpl(pc.getStartTime(), false));
			qry.setAt("Timeout", row, new Double(pc.getRequestTimeout() / 1000));

			PageContext root = pc.getRootPageContext();
			qry.setAt("ThreadType", row, (root != null && root != pc) ? "main" : "child");
			qry.setAt("StackTrace", row, toString(st));
			qry.setAt("TagContext", row, PageExceptionImpl.getTagContext(pc.getConfig(), st));

			qry.setAt("label", row, factory.getLabel());
			qry.setAt("RootPath", row, ReqRspUtil.getRootPath(configWeb.getServletContext()));
			qry.setAt("ConfigFile", row, configWeb.getConfigFile().getAbsolutePath());
			if (factory.getURL() != null) qry.setAt("url", row, factory.getURL().toExternalForm());

		}

	}

	private static String toString(StackTraceElement[] traces) {
		StackTraceElement trace;
		StringBuilder sb = new StringBuilder(traces.length * 32);
		for (int i = 0; i < traces.length; i++) {
			trace = traces[i];
			sb.append("\tat ");
			sb.append(trace.toString());
			sb.append(':');
			sb.append(trace.getLineNumber());
			sb.append(SystemUtil.getOSSpecificLineSeparator());
		}
		return sb.toString();
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetMailSetting() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

		int maxThreads = 20;
		SpoolerEngine engine = config.getSpoolerEngine();
		if (engine instanceof SpoolerEngineImpl) {
			maxThreads = ((SpoolerEngineImpl) engine).getMaxThreads();
		}

		sct.set("spoolEnable", Caster.toBoolean(config.isMailSpoolEnable()));
		sct.set("spoolInterval", Caster.toInteger(config.getMailSpoolInterval()));
		sct.set("maxThreads", Caster.toDouble(maxThreads));
		sct.set("timeout", Caster.toInteger(config.getMailTimeout()));
		sct.set("defaultencoding", config.getMailDefaultCharset().name());

	}

	private void doGetTaskSetting() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

		int maxThreads = 20;
		SpoolerEngine engine = config.getSpoolerEngine();
		if (engine instanceof SpoolerEngineImpl) {
			SpoolerEngineImpl ei = ((SpoolerEngineImpl) engine);
			maxThreads = ei.getMaxThreads();
		}
		sct.set("maxThreads", Caster.toDouble(maxThreads));

	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetTLDs() throws PageException {
		lucee.runtime.type.Query qry = new QueryImpl(
				new String[] { "displayname", "namespace", "namespaceseparator", "shortname", "type", "description", "uri", "elclass", "elBundleName", "elBundleVersion",
						"source" },
				new String[] { "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar" }, 0, "tlds");

		int dialect = "lucee".equalsIgnoreCase(getString("dialect", "cfml")) ? CFMLEngine.DIALECT_LUCEE : CFMLEngine.DIALECT_CFML;

		TagLib[] libs = config.getTLDs(dialect);
		for (int i = 0; i < libs.length; i++) {
			qry.addRow();
			qry.setAt("displayname", i + 1, libs[i].getDisplayName());
			qry.setAt("namespace", i + 1, libs[i].getNameSpace());
			qry.setAt("namespaceseparator", i + 1, libs[i].getNameSpaceSeparator());
			qry.setAt("shortname", i + 1, libs[i].getShortName());
			qry.setAt("type", i + 1, libs[i].getType());
			qry.setAt("description", i + 1, libs[i].getDescription());
			qry.setAt("uri", i + 1, Caster.toString(libs[i].getUri()));
			qry.setAt("elclass", i + 1, libs[i].getELClassDefinition().getClassName());
			qry.setAt("elBundleName", i + 1, libs[i].getELClassDefinition().getName());
			qry.setAt("elBundleVersion", i + 1, libs[i].getELClassDefinition().getVersionAsString());
			qry.setAt("source", i + 1, StringUtil.emptyIfNull(libs[i].getSource()));
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetRHServerExtension() throws PageException {
		_doGetRHExtension(config.getServerRHExtensions());
	}

	private void doGetRHExtension() throws PageException {
		_doGetRHExtension(config.getRHExtensions());
	}

	private void _doGetRHExtension(RHExtension[] extensions) throws PageException {
		String id = getString("admin", action, "id");
		if (StringUtil.isEmpty(id, true)) throw new ApplicationException("Extension ID cannot be empty");
		for (RHExtension ext: extensions) {
			if (id.equals(ext.getId()) || id.equals(ext.getSymbolicName())) {
				pageContext.setVariable(getString("admin", action, "returnVariable"), ext.toStruct());
				return;
			}
		}
		throw new ApplicationException("No Extension found with ID [" + id + "]");
	}

	private void doGetRHExtensions() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), RHExtension.toQuery(config, config.getRHExtensions(), null));
	}

	private void doGetRHServerExtensions() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), RHExtension.toQuery(config, config.getServerRHExtensions(), null));
	}

	private void doGetLocalExtension() throws PageException {
		String id = getString("admin", action, "id");
		boolean asBinary = getBoolV("asBinary", false);
		if (asBinary) {
			Iterator<ExtensionDefintion> it = DeployHandler.getLocalExtensions(config, false).iterator();
			ExtensionDefintion ext;
			while (it.hasNext()) {
				ext = it.next();
				if (id.equalsIgnoreCase(ext.getId())) {
					try {
						pageContext.setVariable(getString("admin", action, "returnVariable"), IOUtil.toBytes(ext.getSource()));
						return;
					}
					catch (IOException e) {
						throw Caster.toPageException(e);
					}
				}
			}
			throw new ApplicationException("No local Extension found with with id [" + id + "]");

		}
		else {
			List<RHExtension> locals = RHExtension.toRHExtensions(DeployHandler.getLocalExtensions(config, false));
			Query qry = RHExtension.toQuery(config, locals, null);
			int rows = qry.getRecordcount();
			String _id;
			int row = 0;
			for (int r = 1; r <= rows; r++) {
				_id = Caster.toString(qry.getAt(KeyConstants._id, r), null);
				if (id.equalsIgnoreCase(_id)) {
					row = r;
					break;
				}
			}
			if (row == 0) throw new ApplicationException("No local Extension found with id [" + id + "]");
			pageContext.setVariable(getString("admin", action, "returnVariable"), Caster.toStruct(qry, row));
		}
	}

	private void doGetLocalExtensions() throws PageException {
		List<RHExtension> locals = RHExtension.toRHExtensions(DeployHandler.getLocalExtensions(config, false));
		Query qry = RHExtension.toQuery(config, locals, null);
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetFLDs() throws PageException {
		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "displayname", "namespace", "namespaceseparator", "shortname", "description", "uri", "source" },
				new String[] { "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar" }, 0, "tlds");

		int dialect = "lucee".equalsIgnoreCase(getString("dialect", "cfml")) ? CFMLEngine.DIALECT_LUCEE : CFMLEngine.DIALECT_CFML;

		FunctionLib[] libs = config.getFLDs(dialect);
		for (int i = 0; i < libs.length; i++) {
			qry.addRow();
			qry.setAt("displayname", i + 1, libs[i].getDisplayName());
			qry.setAt("namespace", i + 1, "");// TODO support for namespace
			qry.setAt("namespaceseparator", i + 1, "");
			qry.setAt("shortname", i + 1, libs[i].getShortName());
			qry.setAt("description", i + 1, libs[i].getDescription());
			qry.setAt("uri", i + 1, Caster.toString(libs[i].getUri()));
			qry.setAt("source", i + 1, StringUtil.emptyIfNull(libs[i].getSource()));
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetRemoteClientUsage() throws PageException {
		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "code", "displayname" }, new String[] { "varchar", "varchar" }, 0, "usage");
		Struct usages = config.getRemoteClientUsage();
		// Key[] keys = usages.keys();
		Iterator<Entry<Key, Object>> it = usages.entryIterator();
		Entry<Key, Object> e;
		int i = -1;
		while (it.hasNext()) {
			i++;
			e = it.next();
			qry.addRow();
			qry.setAt(KeyConstants._code, i + 1, e.getKey().getString());
			qry.setAt(KeyConstants._displayname, i + 1, e.getValue());
			// qry.setAt("description", i+1, usages[i].getDescription());
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doHasRemoteClientUsage() throws PageException {

		Struct usages = config.getRemoteClientUsage();
		pageContext.setVariable(getString("admin", action, "returnVariable"), usages.isEmpty() ? Boolean.FALSE : Boolean.TRUE);
	}

	private void doGetJars() throws PageException {
		Resource lib = config.getLibraryDirectory();
		lucee.runtime.type.Query qry = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._source, KeyConstants._info }, new String[] { "varchar", "varchar", "varchar" },
				0, "jars");

		if (lib.isDirectory()) {
			Resource[] children = lib.listResources(new ExtensionResourceFilter(new String[] { ".jar", ".zip" }, false, true));

			for (int i = 0; i < children.length; i++) {
				qry.addRow();
				qry.setAt(KeyConstants._name, i + 1, children[i].getName());
				qry.setAt(KeyConstants._source, i + 1, children[i].getAbsolutePath());
				try {
					qry.setAt(KeyConstants._info, i + 1, BundleFile.getInstance(children[i]).info());
				}
				catch (Exception e) {
				}
			}
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doUpdateJDBCDriver() throws PageException {
		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "classname"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());

		String label = getString("admin", action, "label");
		String id = getString("id", null);

		admin.updateJDBCDriver(label, id, cd);
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateDatasource() throws PageException {
		int allow = (getBoolV("allowed_select", false) ? DataSource.ALLOW_SELECT : 0) + (getBoolV("allowed_insert", false) ? DataSource.ALLOW_INSERT : 0)
				+ (getBoolV("allowed_update", false) ? DataSource.ALLOW_UPDATE : 0) + (getBoolV("allowed_delete", false) ? DataSource.ALLOW_DELETE : 0)
				+ (getBoolV("allowed_alter", false) ? DataSource.ALLOW_ALTER : 0) + (getBoolV("allowed_drop", false) ? DataSource.ALLOW_DROP : 0)
				+ (getBoolV("allowed_revoke", false) ? DataSource.ALLOW_REVOKE : 0) + (getBoolV("allowed_grant", false) ? DataSource.ALLOW_GRANT : 0)
				+ (getBoolV("allowed_create", false) ? DataSource.ALLOW_CREATE : 0);
		if (allow == 0) allow = DataSource.ALLOW_ALL;

		String cn = getString("admin", action, "classname");
		if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cn)) {
			cn = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		}

		ClassDefinition cd = new ClassDefinitionImpl(cn, getString("bundleName", null), getString("bundleVersion", null), config.getIdentification());

		// customParameterSyntax
		Struct sct = getStruct("customParameterSyntax", null);
		ParamSyntax ps = (sct != null && sct.containsKey("delimiter") && sct.containsKey("separator")) ? ParamSyntax.toParamSyntax(sct) : ParamSyntax.DEFAULT;

		//
		boolean literalTimestampWithTSOffset = getBoolV("literalTimestampWithTSOffset", false);
		boolean alwaysSetTimeout = getBoolV("alwaysSetTimeout", false);
		boolean requestExclusive = getBoolV("requestExclusive", false);
		boolean alwaysResetConnections = getBoolV("alwaysResetConnections", false);

		String id = getString("id", null);
		String dsn = getString("admin", action, "dsn");
		String name = getString("admin", action, "name");
		String newName = getString("admin", action, "newName");
		String username = getString("admin", action, "dbusername");
		String password = getString("admin", action, "dbpassword");
		String host = getString("host", "");
		String timezone = getString("timezone", "");
		String database = getString("database", "");
		int port = getInt("port", -1);
		int connLimit = getInt("connectionLimit", -1);
		int idleTimeout = getInt("connectionTimeout", -1);
		if (idleTimeout == -1) idleTimeout = getInt("idleTimeout", -1);
		int liveTimeout = getInt("liveTimeout", -1);
		int minIdle = getInt("minIdle", -1);
		int maxIdle = getInt("maxIdle", -1);
		int maxTotal = getInt("maxTotal", -1);
		long metaCacheTimeout = getLong("metaCacheTimeout", 60000);
		boolean blob = getBoolV("blob", false);
		boolean clob = getBoolV("clob", false);
		boolean validate = getBoolV("validate", false);
		boolean storage = getBoolV("storage", false);
		boolean verify = getBoolV("verify", true);
		Struct custom = getStruct("custom", new StructImpl());
		String dbdriver = getString("dbdriver", "");

		// config.getDatasourceConnectionPool().remove(name);
		DataSourcePro ds = null;
		try {
			ds = new DataSourceImpl(config, name, cd, host, dsn, database, port, username, password, null, connLimit, idleTimeout, liveTimeout, minIdle, maxIdle, maxTotal,
					metaCacheTimeout, blob, clob, allow, custom, false, validate, storage, null, dbdriver, ps, literalTimestampWithTSOffset, alwaysSetTimeout, requestExclusive,
					alwaysResetConnections, config.getLog("application"));
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		if (verify) _doVerifyDatasource(ds, username, password);
		// print.out("limit:"+connLimit);
		admin.updateDataSource(id, name, newName, cd, dsn, username, password, host, database, port, connLimit, idleTimeout, liveTimeout, metaCacheTimeout, blob, clob, allow,
				validate, storage, timezone, custom, dbdriver, ps, literalTimestampWithTSOffset, alwaysSetTimeout, requestExclusive, alwaysResetConnections);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateCacheConnection() throws PageException {
		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());

		admin.updateCacheConnection(getString("admin", action, "name"), cd, toCacheConstant("default"), getStruct("admin", action, "custom"), getBoolV("readOnly", false),
				getBoolV("storage", false)

		);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateGatewayEntry() throws PageException {
		String strStartupMode = getString("admin", action, "startupMode");
		int startup = GatewayEntryImpl.toStartup(strStartupMode, -1);
		if (startup == -1) throw new ApplicationException("Invalid startup mode [" + strStartupMode + "], valid values are [automatic,manual,disabled]");

		// custom validation
		Struct custom = getStruct("admin", action, "custom");
		if (custom != null) {
			String path = Caster.toString(custom.get("directory", null), null);
			if (!StringUtil.isEmpty(path)) { //
				Resource dir = ResourceUtil.toResourceNotExisting(pageContext, path);
				if (!dir.isDirectory()) throw new ApplicationException("Directory [" + path + " ] not exists ");
			}
		}
		// listenerCfcPath validation
		/*
		 * String path = getString("admin", action, "listenerCfcPath"); if(!StringUtil.isEmpty(path,true)) {
		 * path=path.trim().replace('\\','/'); if(path.indexOf("./")==-1)path=path.replace('.','/'); String
		 * ext = "."+Constants.getCFMLComponentExtension(); if(!path.endsWith(ext)) path+=ext;
		 * 
		 * Resource listnerCFC = ResourceUtil.toResourceNotExisting(pageContext, path);
		 * if(!listnerCFC.exists()) throw new ApplicationException("invalid [" + listnerCFC
		 * +" ] listener CFC"); }
		 */

		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());
		admin.updateGatewayEntry(getString("admin", action, "id"), cd, getString("admin", action, "cfcPath"), getString("admin", action, "listenerCfcPath"), startup,
				getStruct("admin", action, "custom"), getBoolV("readOnly", false)

		);
		store();
		adminSync.broadcast(attributes, config);
	}

	private int toCacheConstant(String name) throws ApplicationException {
		String def = getString(name, null);
		if (StringUtil.isEmpty(def)) return Config.CACHE_TYPE_NONE;
		def = def.trim().toLowerCase();

		if (def.equals("object")) return ConfigPro.CACHE_TYPE_OBJECT;
		if (def.equals("template")) return ConfigPro.CACHE_TYPE_TEMPLATE;
		if (def.equals("query")) return ConfigPro.CACHE_TYPE_QUERY;
		if (def.equals("resource")) return ConfigPro.CACHE_TYPE_RESOURCE;
		if (def.equals("function")) return ConfigPro.CACHE_TYPE_FUNCTION;
		if (def.equals("include")) return ConfigPro.CACHE_TYPE_INCLUDE;
		if (def.equals("http")) return ConfigPro.CACHE_TYPE_HTTP;
		if (def.equals("file")) return ConfigPro.CACHE_TYPE_FILE;
		if (def.equals("webservice")) return ConfigPro.CACHE_TYPE_WEBSERVICE;

		throw new ApplicationException("Invalid default type [" + def + "], valid default types are [object,template,query,resource,function]");
	}

	private void doUpdateCacheDefaultConnection() throws PageException {
		admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_OBJECT, getString("admin", action, "object"));
		admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_TEMPLATE, getString("admin", action, "template"));
		admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_QUERY, getString("admin", action, "query"));
		admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_RESOURCE, getString("admin", action, "resource"));
		admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_FUNCTION, getString("admin", action, "function"));
		admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_INCLUDE, getString("admin", action, "include"));
		admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_HTTP, getString("admin", action, "http"));
		admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_FILE, getString("admin", action, "file"));
		admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_WEBSERVICE, getString("admin", action, "webservice"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveCacheDefaultConnection() throws PageException {
		admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_OBJECT);
		admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_TEMPLATE);
		admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_QUERY);
		admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_RESOURCE);
		admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_FUNCTION);
		admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_INCLUDE);
		admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_HTTP);
		admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_FILE);
		admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_WEBSERVICE);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveLogSetting() throws PageException {
		admin.removeLogSetting(getString("admin", "RemoveLogSettings", "name"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveResourceProvider() throws PageException {
		/*
		 * ClassDefinition cd = new ClassDefinitionImpl( getString("admin",action,"class") ,
		 * getString("bundleName",null) ,getString("bundleVersion",null), config.getIdentification());
		 */

		admin.removeResourceProvider(getString("admin", action, "scheme"));

		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateResourceProvider() throws PageException {

		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());

		String scheme = getString("admin", action, "scheme");

		Struct sctArguments = getStruct("arguments", null);
		if (sctArguments != null) {

			admin.updateResourceProvider(scheme, cd, sctArguments);
		}
		else {
			String strArguments = getString("admin", action, "arguments");
			admin.updateResourceProvider(scheme, cd, strArguments);
		}

		// admin.updateResourceProvider(scheme,clazz,arguments);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateDefaultResourceProvider() throws PageException {
		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());

		String arguments = getString("admin", action, "arguments");

		admin.updateDefaultResourceProvider(cd, arguments);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doVerifyMailServer() throws PageException {
		_doVerifyMailServer(getString("admin", action, "hostname"), getInt("admin", action, "port"), getString("admin", action, "mailusername"),
				getString("admin", action, "mailpassword"));
	}

	private void _doVerifyMailServer(String host, int port, String user, String pass) throws PageException {
		try {
			SMTPVerifier.verify(host, user, pass, port);
		}
		catch (SMTPException e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doVerifyDatasource() throws PageException {
		ClassDefinition cd = new ClassDefinitionImpl(Caster.toString(attributes.get("classname", null), null), Caster.toString(attributes.get("bundleName", null), null),
				Caster.toString(attributes.get("bundleVersion", null), null), config.getIdentification());

		String connStr = (String) attributes.get("connStr", null);
		if (StringUtil.isEmpty(connStr)) connStr = (String) attributes.get("dsn", null);
		if (cd.hasClass() && connStr != null) {
			_doVerifyDatasource(cd, connStr, getString("admin", action, "dbusername"), getString("admin", action, "dbpassword"));
		}
		else {
			_doVerifyDatasource(getString("admin", action, "name"), getString("admin", action, "dbusername"), getString("admin", action, "dbpassword"));
		}
	}

	private void doVerifyRemoteClient() throws PageException {
		// SNSN
		/*
		 * SerialNumber sn = config.getSerialNumber(); if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
		 * throw new
		 * SecurityException("can not verify remote client with "+sn.getStringVersion()+" version of Lucee"
		 * );
		 */
		ProxyData pd = null;
		String proxyServer = getString("proxyServer", null);
		if (!StringUtil.isEmpty(proxyServer)) {
			String proxyUsername = getString("proxyUsername", null);
			String proxyPassword = getString("proxyPassword", null);
			int proxyPort = getInt("proxyPort", -1);
			pd = new ProxyDataImpl();
			pd.setServer(proxyServer);
			if (!StringUtil.isEmpty(proxyUsername)) pd.setUsername(proxyUsername);
			if (!StringUtil.isEmpty(proxyPassword)) pd.setPassword(proxyPassword);
			if (proxyPort != -1) pd.setPort(proxyPort);
		}
		RemoteClient client = new RemoteClientImpl(getString("admin", action, "label"), type == TYPE_WEB ? "web" : "server", getString("admin", action, "url"),
				getString("serverUsername", null), getString("serverPassword", null), getString("admin", action, "adminPassword"), pd, getString("admin", action, "securityKey"),
				getString("admin", action, "usage")

		);

		Struct attrColl = new StructImpl();
		attrColl.setEL(KeyConstants._action, "connect");
		try {
			new RemoteClientTask(null, client, attrColl, getCallerId(), "synchronisation").execute(config);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	private void _doVerifyDatasource(DataSourcePro ds, String username, String password) throws PageException {
		try {
			DatasourceConnectionImpl dc = new DatasourceConnectionImpl(null, ds.getConnection(config, username, password), ds, username, password);
			dc.close();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void _doVerifyDatasource(String name, String username, String password) throws PageException {

		DataSourceManager manager = pageContext.getDataSourceManager();
		manager.releaseConnection(pageContext, manager.getConnection(pageContext, name, username, password));
	}

	private void _doVerifyDatasource(ClassDefinition cd, String connStrTranslated, String user, String pass) throws PageException {
		try {
			DataSourceImpl.verify(config, cd, connStrTranslated, user, pass);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdatePSQ() throws PageException {
		admin.updatePSQ(getBoolObject("admin", action, "psq"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doReload() throws PageException {
		store();
	}

	private void doRemoveJDBCDriver() throws PageException {
		admin.removeJDBCDriver(getString("admin", action, "class"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doRemoveDatasource() throws PageException {
		admin.removeDataSource(getString("admin", action, "name"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doTerminateRunningThread() throws PageException {
		int id = getInt("admin", "RemoveRunningThread", "id");

		if (type == TYPE_WEB) {
			terminateRunningThread(pageContext.getConfig(), id);
		}
		else {
			ConfigServer cs = pageContext.getConfig().getConfigServer(password);
			ConfigWeb[] webs = cs.getConfigWebs();
			for (int i = 0; i < webs.length; i++) {
				if (terminateRunningThread(webs[i], id)) break;
			}
		}
	}

	private static boolean terminateRunningThread(ConfigWeb configWeb, int id) {

		Map<Integer, PageContextImpl> pcs = ((CFMLFactoryImpl) configWeb.getFactory()).getActivePageContexts();
		Iterator<PageContextImpl> it = pcs.values().iterator();
		PageContextImpl pc;
		Collection.Key key;
		while (it.hasNext()) {
			pc = it.next();
			if (pc.getId() == id) {
				CFMLFactoryImpl.terminate(pc, true);
				return true;
			}
		}
		return false;
	}

	private void doRemoveRemoteClient() throws PageException {
		admin.removeRemoteClient(getString("admin", action, "url"));
		store();
	}

	private void doRemoveSpoolerTask() throws PageException {
		config.getSpoolerEngine().remove(getString("admin", action, "id"));
	}

	private void doRemoveAllSpoolerTask() {
		((SpoolerEngineImpl) config.getSpoolerEngine()).removeAll();
	}

	private void doExecuteSpoolerTask() throws PageException {
		PageException pe = config.getSpoolerEngine().execute(getString("admin", action, "id"));
		if (pe != null) throw pe;
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetDatasourceSetting() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("psq", Caster.toBoolean(config.getPSQL()));
	}

	private void doGetORMSetting() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), config.getORMConfig().toStruct());
	}

	private void doGetORMEngine() throws PageException {
		ClassDefinition<? extends ORMEngine> cd = config.getORMEngineClassDefintion();
		Struct sct = new StructImpl();
		sct.set(KeyConstants._class, cd.getClassName());
		sct.set(KeyConstants._bundleName, cd.getName());
		sct.set(KeyConstants._bundleVersion, cd.getVersionAsString());

		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
	}

	private void doUpdateORMSetting() throws SecurityException, PageException {
		ORMConfiguration oc = config.getORMConfig();
		Struct settings = new StructImpl();

		settings.set(ORMConfigurationImpl.AUTO_GEN_MAP, getBool("admin", action, "autogenmap"));
		settings.set(ORMConfigurationImpl.EVENT_HANDLING, getBool("admin", action, "eventHandling"));
		settings.set(ORMConfigurationImpl.FLUSH_AT_REQUEST_END, getBool("admin", action, "flushatrequestend"));
		settings.set(ORMConfigurationImpl.LOG_SQL, getBool("admin", action, "logSQL"));
		settings.set(ORMConfigurationImpl.SAVE_MAPPING, getBool("admin", action, "savemapping"));
		settings.set(ORMConfigurationImpl.USE_DB_FOR_MAPPING, getBool("admin", action, "useDBForMapping"));
		settings.set(ORMConfigurationImpl.SECONDARY_CACHE_ENABLED, getBool("admin", action, "secondarycacheenabled"));

		settings.set(ORMConfigurationImpl.CATALOG, getString("admin", action, "catalog"));
		settings.set(ORMConfigurationImpl.SCHEMA, getString("admin", action, "schema"));
		settings.set(ORMConfigurationImpl.SQL_SCRIPT, getString("admin", action, "sqlscript"));
		settings.set(ORMConfigurationImpl.CACHE_CONFIG, getString("admin", action, "cacheconfig"));
		settings.set(ORMConfigurationImpl.CACHE_PROVIDER, getString("admin", action, "cacheProvider"));
		settings.set(ORMConfigurationImpl.ORM_CONFIG, getString("admin", action, "ormConfig"));

		// dbcreate
		String strDbcreate = getString("admin", action, "dbcreate");
		String dbcreate = "none";
		if ("none".equals(strDbcreate)) dbcreate = "none";
		else if ("update".equals(strDbcreate)) dbcreate = "update";
		else if ("dropcreate".equals(strDbcreate)) dbcreate = "dropcreate";
		else throw new ApplicationException("Invalid dbcreate definition [" + strDbcreate + "], valid dbcreate definitions are [none,update,dropcreate]");
		settings.set(ORMConfigurationImpl.DB_CREATE, dbcreate);

		// cfclocation
		String strCfclocation = getString("admin", action, "cfclocation");
		Array arrCfclocation = lucee.runtime.type.util.ListUtil.listToArray(strCfclocation, ",\n");
		Iterator it = arrCfclocation.valueIterator();
		String path;
		while (it.hasNext()) {
			path = (String) it.next();
			ResourceUtil.toResourceExisting(config, path);
		}
		settings.set(KeyConstants._cfcLocation, arrCfclocation);

		admin.updateORMSetting(ORMConfigurationImpl.load(config, null, settings, null, oc));

		store();
		adminSync.broadcast(attributes, config);
	}

	private void doResetORMSetting() throws SecurityException, PageException {
		config.getORMConfig();
		admin.resetORMSetting();
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdatePerformanceSettings() throws SecurityException, PageException {
		admin.updateInspectTemplate(getString("admin", action, "inspectTemplate"));

		admin.updateTypeChecking(getBoolObject("admin", action, "typeChecking"));

		// cached after
		Object obj = getObject("cachedAfter", null);
		if (StringUtil.isEmpty(obj)) obj = null;

		if (obj != null) admin.updateCachedAfterTimeRange(Caster.toTimeSpan(obj));
		else admin.updateCachedAfterTimeRange(null);

		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateCompilerSettings() throws SecurityException, PageException {
		admin.updateCompilerSettings(getBoolObject("admin", "UpdateCompilerSettings", "dotNotationUpperCase"),
				getBoolObject("admin", "UpdateCompilerSettings", "suppressWSBeforeArg"), getBoolObject("admin", "UpdateCompilerSettings", "nullSupport"),
				getBoolObject("admin", "UpdateCompilerSettings", "handleUnquotedAttrValueAsString"), getInteger("admin", "UpdateCompilerSettings", "externalizeStringGTE"));
		admin.updateTemplateCharset(getString("admin", action, "templateCharset"));

		store();
		adminSync.broadcast(attributes, config);
	}

	/*
	 * private void doGetLogSetting() throws PageException { String name=getString("admin",
	 * "GetLogSetting", "name"); name=name.trim().toLowerCase(); Query qry=_doGetLogSettings();
	 * 
	 * int records = qry.getRecordcount(); for(int row=1;row<=records;row++){ String n =
	 * Caster.toString(qry.getAt("name", row, null),null); if(!StringUtil.isEmpty(n) &&
	 * n.trim().equalsIgnoreCase(name)) { Struct sct=new StructImpl(); String
	 * returnVariable=getString("admin",action,"returnVariable");
	 * pageContext.setVariable(returnVariable,sct);
	 * 
	 * sct.setEL(KeyConstants._name, qry.getAt(KeyConstants._name, row, ""));
	 * sct.setEL(KeyConstants._level, qry.getAt(KeyConstants._level, row, "")); sct.setEL("virtualpath",
	 * qry.getAt("virtualpath", row, "")); sct.setEL(KeyConstants._class, qry.getAt(KeyConstants._class,
	 * row, "")); sct.setEL("maxFile", qry.getAt("maxFile", row, "")); sct.setEL("maxFileSize",
	 * qry.getAt("maxFileSize", row, "")); sct.setEL(KeyConstants._path, qry.getAt(KeyConstants._path,
	 * row, ""));
	 * 
	 * return; } } throw new ApplicationException("invalid log name ["+name+"]");
	 * 
	 * }
	 */

	private void doGetCompilerSettings() throws PageException {
		String returnVariable = getString("admin", action, "returnVariable");
		Struct sct = new StructImpl();
		pageContext.setVariable(returnVariable, sct);

		sct.set("DotNotationUpperCase", config.getDotNotationUpperCase() ? Boolean.TRUE : Boolean.FALSE);
		sct.set("suppressWSBeforeArg", config.getSuppressWSBeforeArg() ? Boolean.TRUE : Boolean.FALSE);
		sct.set("nullSupport", config.getFullNullSupport() ? Boolean.TRUE : Boolean.FALSE);
		sct.set("handleUnquotedAttrValueAsString", config.getHandleUnQuotedAttrValueAsString() ? Boolean.TRUE : Boolean.FALSE);
		sct.set("templateCharset", config.getTemplateCharset());
		sct.set("externalizeStringGTE", Caster.toDouble(config.getExternalizeStringGTE()));

	}

	private void doGetLogSettings() throws PageException {
		String returnVariable = getString("admin", action, "returnVariable");
		pageContext.setVariable(returnVariable, _doGetLogSettings());
	}

	private Query _doGetLogSettings() {
		Map<String, LoggerAndSourceData> loggers = config.getLoggers();
		Query qry = new QueryImpl(new String[] { "name", "level", "appenderClass", "appenderBundleName", "appenderBundleVersion", "appenderArgs", "layoutClass", "layoutBundleName",
				"layoutBundleVersion", "layoutArgs", "readonly" }, 0, lucee.runtime.type.util.ListUtil.last("logs", '.'));
		int row = 0;
		Iterator<Entry<String, LoggerAndSourceData>> it = loggers.entrySet().iterator();
		Entry<String, LoggerAndSourceData> e;
		LoggerAndSourceData logger;
		while (it.hasNext()) {
			e = it.next();
			logger = e.getValue();
			if (logger.getDyn()) continue;
			row = qry.addRow();
			// row++;
			qry.setAtEL("name", row, e.getKey());
			qry.setAtEL("level", row, LogUtil.levelToString(logger.getLevel(), ""));
			qry.setAtEL("appenderClass", row, logger.getAppenderClassDefinition().getClassName());
			qry.setAtEL("appenderBundleName", row, logger.getAppenderClassDefinition().getName());
			qry.setAtEL("appenderBundleVersion", row, logger.getAppenderClassDefinition().getVersionAsString());

			qry.setAtEL("appenderArgs", row, toStruct(logger.getAppenderArgs()));
			qry.setAtEL("layoutClass", row, logger.getLayoutClassDefinition().getClassName());
			qry.setAtEL("layoutBundleName", row, logger.getLayoutClassDefinition().getName());
			qry.setAtEL("layoutBundleVersion", row, logger.getLayoutClassDefinition().getVersionAsString());

			qry.setAtEL("layoutArgs", row, toStruct(logger.getLayoutArgs()));
			qry.setAtEL("readonly", row, logger.getReadOnly());
		}
		return qry;
	}

	private Object toStruct(Map<String, String> map) {
		Struct sct = new StructImpl();
		if (map != null) {
			Iterator<Entry<String, String>> it = map.entrySet().iterator();
			Entry<String, String> e;
			while (it.hasNext()) {
				e = it.next();
				sct.setEL(e.getKey(), e.getValue());
			}
		}
		return sct;
	}

	private void doGetPerformanceSettings() throws ApplicationException, PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

		short it = config.getInspectTemplate();
		String str = "once";
		if (it == ConfigPro.INSPECT_ALWAYS) str = "always";
		else if (it == ConfigPro.INSPECT_NEVER) str = "never";
		sct.set("inspectTemplate", str);
		sct.set("typeChecking", config.getTypeChecking());

		// cached within
		TimeSpan cachedAfter = config.getCachedAfterTimeRange();
		if (cachedAfter == null) cachedAfter = new TimeSpanImpl(0, 0, 0, 0);

		sct.set("cachedAfter", cachedAfter);
		sct.set("cachedAfter_day", cachedAfter.getDay());
		sct.set("cachedAfter_hour", cachedAfter.getHour());
		sct.set("cachedAfter_minute", cachedAfter.getMinute());
		sct.set("cachedAfter_second", cachedAfter.getSecond());

	}

	private void doGetCustomTagSetting() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("customTagDeepSearch", Caster.toBoolean(config.doCustomTagDeepSearch()));// deprecated
		sct.set("customTagLocalSearch", Caster.toBoolean(config.doLocalCustomTag()));// deprecated

		sct.set("deepSearch", Caster.toBoolean(config.doCustomTagDeepSearch()));
		sct.set("localSearch", Caster.toBoolean(config.doLocalCustomTag()));
		sct.set("customTagPathCache", Caster.toBoolean(config.useCTPathCache()));
		sct.set("extensions", new ArrayImpl(config.getCustomTagExtensions()));
	}

	private void doGetDatasourceDriverList() throws PageException {
		Resource luceeContext = ResourceUtil.toResourceExisting(pageContext, "/lucee/admin/dbdriver/");
		Resource[] children = luceeContext.listResources(new ExtensionResourceFilter(Constants.getComponentExtensions()));
		String rtnVar = getString("admin", action, "returnVariable");

		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "name" }, children.length, rtnVar);

		for (int i = 0; i < children.length; i++) {
			qry.setAt("name", i + 1, children[i].getName());
		}
		pageContext.setVariable(rtnVar, qry);

	}

	private void doGetDebuggingList() throws PageException {
		Resource luceeContext = ResourceUtil.toResourceExisting(pageContext, "/lucee/templates/debugging/");
		Resource[] children = luceeContext.listResources(new ExtensionResourceFilter(Constants.getTemplateExtensions()));
		String rtnVar = getString("admin", action, "returnVariable");

		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "name" }, children.length, rtnVar);

		for (int i = 0; i < children.length; i++) {
			qry.setAt("name", i + 1, children[i].getName());
		}
		pageContext.setVariable(rtnVar, qry);

	}

	private void doGetGatewayEntries() throws PageException {
		Map entries = ((GatewayEngineImpl) ((ConfigWebPro) config).getGatewayEngine()).getEntries();
		Iterator it = entries.entrySet().iterator();
		lucee.runtime.type.Query qry = new QueryImpl(
				new String[] { "class", "bundleName", "bundleVersion", "id", "custom", "cfcPath", "listenerCfcPath", "startupMode", "state", "readOnly" }, 0, "entries");
		Map.Entry entry;
		GatewayEntry ge;
		// Gateway g;
		int row = 0;
		while (it.hasNext()) {
			row++;
			entry = (Entry) it.next();
			ge = (GatewayEntry) entry.getValue();
			// g=ge.getGateway();
			qry.addRow();
			qry.setAtEL("class", row, ge.getClassDefinition().getClassName());
			qry.setAtEL("bundleName", row, ge.getClassDefinition().getName());
			qry.setAtEL("bundleVersion", row, ge.getClassDefinition().getVersionAsString());
			qry.setAtEL("id", row, ge.getId());
			qry.setAtEL("listenerCfcPath", row, ge.getListenerCfcPath());
			qry.setAtEL("cfcPath", row, ge.getCfcPath());
			qry.setAtEL("startupMode", row, GatewayEntryImpl.toStartup(ge.getStartupMode(), "automatic"));
			qry.setAtEL("custom", row, ge.getCustom());
			qry.setAtEL("readOnly", row, Caster.toBoolean(ge.isReadOnly()));
			qry.setAtEL("state", row, GatewayEngineImpl.toStringState(GatewayUtil.getState(ge), "failed"));

		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetBundle() throws PageException {
		String symbolicName = getString("admin", "getBundle", "symbolicName", true);
		Version version = OSGiUtil.toVersion(getString("version", null), null);

		BundleDefinition bd;
		BundleFile bf = null;
		Bundle b = OSGiUtil.getBundleLoaded(symbolicName, version, null);
		if (b != null) {
			bd = new BundleDefinition(b);
		}
		else {
			try {
				bf = OSGiUtil.getBundleFile(symbolicName, version, null, null, false);
				bd = bf.toBundleDefinition();
				b = bd.getLoadedBundle();

			}
			catch (BundleException e) {
				throw Caster.toPageException(e);
			}
		}

		CFMLEngine engine = ConfigWebUtil.getEngine(config);
		BundleCollection coreBundles = engine.getBundleCollection();
		java.util.Collection<BundleDefinition> extBundles = config.getAllExtensionBundleDefintions();

		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

		sct.set(SYMBOLIC_NAME, bd.getName());
		sct.set(KeyConstants._title, bd.getName());
		sct.set(KeyConstants._version, bd.getVersionAsString());
		sct.set(USED_BY, _usedBy(bd.getName(), bd.getVersion(), coreBundles, extBundles));
		try {
			if (b != null) {
				sct.set(PATH, b.getLocation());
			}
			else {
				if (bf == null) bf = bd.getBundleFile(false, JavaSettingsImpl.getBundleDirectories(pageContext));
				sct.set(PATH, bf.getFile());
			}

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		Map<String, Object> headers = null;
		if (b != null) {
			sct.set(KeyConstants._version, bd.getVersion().toString());
			sct.set(KeyConstants._id, b.getBundleId());
			sct.set(KeyConstants._state, OSGiUtil.toState(b.getState(), null));
			sct.set(FRAGMENT, OSGiUtil.isFragment(b));

			headers = OSGiUtil.getHeaders(b);
		}
		else {
			sct.set(KeyConstants._state, "notinstalled");
			try {

				if (bf == null) bf = bd.getBundleFile(false, null);
				sct.set(KeyConstants._version, bf.getVersionAsString());
				sct.set(FRAGMENT, OSGiUtil.isFragment(bf));
				headers = bf.getHeaders();

			}
			catch (BundleException e) {
			}

		}

		if (headers != null) {
			Struct h = Caster.toStruct(headers, false);
			sct.set(HEADERS, h);

			// title
			String str = Caster.toString(h.get("Bundle-Title", null), null);
			if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Title", null), null);
			if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Title", null), null);
			if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Bundle-Name", null), null);
			if (!StringUtil.isEmpty(str)) sct.set(KeyConstants._title, str);

			// description
			str = Caster.toString(h.get("Bundle-Description", null), null);
			if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Description", null), null);
			if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Description", null), null);
			if (!StringUtil.isEmpty(str)) sct.set(KeyConstants._description, str);

			// Vendor
			str = Caster.toString(h.get("Bundle-Vendor", null), null);
			if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Vendor", null), null);
			if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Vendor", null), null);
			if (!StringUtil.isEmpty(str)) sct.set(VENDOR, str);

		}

	}

	private void doGetBundles() throws PageException {
		CFMLEngine engine = ConfigWebUtil.getEngine(config);
		BundleCollection coreBundles = engine.getBundleCollection();
		java.util.Collection<BundleDefinition> extBundles = config.getAllExtensionBundleDefintions();

		List<BundleDefinition> bds = OSGiUtil.getBundleDefinitions(engine.getBundleContext());
		Iterator<BundleDefinition> it = bds.iterator();
		BundleDefinition bd;
		Bundle b;
		String str;
		Query qry = new QueryImpl(new Key[] { SYMBOLIC_NAME, KeyConstants._title, KeyConstants._description, KeyConstants._version, VENDOR, KeyConstants._state, PATH, USED_BY,
				KeyConstants._id, FRAGMENT, HEADERS }, bds.size(), "bundles");
		int row = 0;
		while (it.hasNext()) {
			row++;
			bd = it.next();
			b = bd.getLoadedBundle();
			qry.setAt(SYMBOLIC_NAME, row, bd.getName());
			qry.setAt(KeyConstants._title, row, bd.getName());
			qry.setAt(KeyConstants._version, row, bd.getVersionAsString());
			qry.setAt(USED_BY, row, _usedBy(bd.getName(), bd.getVersion(), coreBundles, extBundles));
			BundleFile bf = null;
			try {
				if (b != null) {
					qry.setAt(PATH, row, b.getLocation());
				}
				else {
					bf = bd.getBundleFile(false, null);
					qry.setAt(PATH, row, bf.getFile());
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}

			Map<String, Object> headers = null;
			if (b != null) {
				qry.setAt(KeyConstants._version, row, bd.getVersion().toString());
				qry.setAt(KeyConstants._id, row, b.getBundleId());
				qry.setAt(KeyConstants._state, row, OSGiUtil.toState(b.getState(), null));
				qry.setAt(FRAGMENT, row, OSGiUtil.isFragment(b));

				headers = OSGiUtil.getHeaders(b);
			}
			else {
				qry.setAt(KeyConstants._state, row, "notinstalled");

				try {
					if (b != null) {
						qry.setAt(KeyConstants._version, row, b.getVersion().toString());
						qry.setAt(FRAGMENT, row, OSGiUtil.isFragment(b));
						Dictionary<String, String> dic = b.getHeaders();
						Enumeration<String> keys = dic.keys();
						headers = new HashMap<String, Object>();
						String key;
						while (keys.hasMoreElements()) {
							key = keys.nextElement();
							headers.put(key, dic.get(key));
						}
					}
					else {
						if (bf != null) bf = bd.getBundleFile(false, null);
						qry.setAt(KeyConstants._version, row, bf.getVersionAsString());
						// qry.setAt(KeyConstants._id, row, bf.getBundleId());
						qry.setAt(FRAGMENT, row, OSGiUtil.isFragment(bf));
						headers = bf.getHeaders();
					}

				}
				catch (BundleException e) {
				}

			}

			if (headers != null) {
				Struct h = Caster.toStruct(headers, false);
				qry.setAt(HEADERS, row, h);

				// title
				str = Caster.toString(h.get("Bundle-Title", null), null);
				if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Title", null), null);
				if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Title", null), null);
				if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Bundle-Name", null), null);
				if (!StringUtil.isEmpty(str)) qry.setAt(KeyConstants._title, row, str);

				// description
				str = Caster.toString(h.get("Bundle-Description", null), null);
				if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Description", null), null);
				if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Description", null), null);
				if (!StringUtil.isEmpty(str)) qry.setAt(KeyConstants._description, row, str);

				// Vendor
				str = Caster.toString(h.get("Bundle-Vendor", null), null);
				if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Vendor", null), null);
				if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Vendor", null), null);
				if (!StringUtil.isEmpty(str)) qry.setAt(VENDOR, row, str);

				// Specification-Vendor,Bundle-Vendor
			}

		}

		QuerySort.call(pageContext, qry, "title");
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private String _usedBy(String name, Version version, BundleCollection coreBundles, java.util.Collection<BundleDefinition> extBundles) {
		Set<String> extensions = new HashSet<String>();

		// core
		if (_eq(name, version, coreBundles.core.getSymbolicName(), coreBundles.core.getVersion())) {
			extensions.add("Lucee");
			// return "Lucee";
		}
		Iterator<Bundle> it = coreBundles.getSlaves();
		Bundle b;
		while (it.hasNext()) {
			b = it.next();
			if (_eq(name, version, b.getSymbolicName(), b.getVersion())) {
				extensions.add("Lucee");
				// return "Lucee";
				break;
			}
		}

		Iterator<BundleDefinition> itt = extBundles.iterator();
		BundleDefinition bd;
		while (itt.hasNext()) {
			bd = itt.next();
			if (_eq(name, version, bd.getName(), bd.getVersion())) {
				findExtension(extensions, bd);
			}
		}

		if (extensions.size() == 0) return "";
		if (extensions.size() == 1) return extensions.iterator().next();

		return ListUtil.arrayToList(extensions.toArray(new String[extensions.size()]), ", ");
	}

	private void findExtension(Set<String> extensions, BundleDefinition bd) {
		ConfigPro ci = config;
		_findExtension(ci.getRHExtensions(), bd, extensions);
		_findExtension(ci.getServerRHExtensions(), bd, extensions);
	}

	private void _findExtension(RHExtension[] extensions, BundleDefinition bd, Set set) {
		BundleInfo[] bundles;
		for (RHExtension e: extensions) {
			try {
				bundles = e.getBundles();
				if (bundles != null) {
					for (BundleInfo b: bundles) {
						if (_eq(bd.getName(), bd.getVersion(), b.getSymbolicName(), b.getVersion())) {
							set.add(e.getName());
						}
					}
				}
			}
			catch (Exception ex) {
			}
		}
	}

	private boolean _eq(String lName, Version lVersion, String rName, Version rVersion) {
		if (!lName.equals(rName)) return false;
		if (lVersion == null) return rVersion == null;
		return lVersion.equals(rVersion);
	}

	private void doGetMonitors() throws PageException {
		if (!(config instanceof ConfigServerImpl)) throw new ApplicationException("invalid context for this action");

		ConfigServerImpl cs = (ConfigServerImpl) config;
		IntervallMonitor[] intervalls = cs.getIntervallMonitors();
		RequestMonitor[] requests = cs.getRequestMonitors();

		lucee.runtime.type.Query qry = new QueryImpl(new Collection.Key[] { KeyConstants._name, KeyConstants._type, LOG_ENABLED, CLASS }, 0, "monitors");
		doGetMonitors(qry, intervalls);
		doGetMonitors(qry, requests);

		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetMonitor() throws PageException {
		if (!(config instanceof ConfigServerImpl)) throw new ApplicationException("invalid context for this action");
		ConfigServerImpl cs = (ConfigServerImpl) config;

		String type = getString("admin", action, "monitorType");
		String name = getString("admin", action, "name");
		type = type.trim();
		Monitor m;
		if ("request".equalsIgnoreCase(type)) m = cs.getRequestMonitor(name);
		else m = cs.getIntervallMonitor(name);

		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._name, m.getName());
		sct.setEL(KeyConstants._type, m.getType() == Monitor.TYPE_INTERVAL ? "intervall" : "request");
		sct.setEL(LOG_ENABLED, m.isLogEnabled());
		sct.setEL(CLASS, m.getClazz().getName());

		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
	}

	private void doGetExecutionLog() throws PageException {
		ExecutionLogFactory factory = config.getExecutionLogFactory();
		Struct sct = new StructImpl();

		sct.set(KeyConstants._enabled, Caster.toBoolean(config.getExecutionLogEnabled()));
		Class clazz = factory.getClazz();
		sct.set(KeyConstants._class, clazz != null ? clazz.getName() : "");
		sct.set(KeyConstants._arguments, factory.getArgumentsAsStruct());

		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
	}

	private void doGetMonitors(Query qry, Monitor[] monitors) {
		Monitor m;
		int row;
		for (int i = 0; i < monitors.length; i++) {
			m = monitors[i];
			row = qry.addRow();
			qry.setAtEL(KeyConstants._name, row, m.getName());
			qry.setAtEL(KeyConstants._type, row, m.getType() == Monitor.TYPE_INTERVAL ? "intervall" : "request");
			qry.setAtEL(LOG_ENABLED, row, m.isLogEnabled());
			qry.setAtEL(CLASS, row, m.getClazz().getName());
		}

	}

	private void doGetGatewayEntry() throws PageException {

		String id = getString("admin", action, "id");
		Map entries = ((GatewayEngineImpl) ((ConfigWebPro) config).getGatewayEngine()).getEntries();
		Iterator it = entries.keySet().iterator();
		GatewayEntry ge;
		// Gateway g;
		Struct sct;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (key.equalsIgnoreCase(id)) {
				ge = (GatewayEntry) entries.get(key);
				// g=ge.getGateway();
				sct = new StructImpl();
				sct.setEL(KeyConstants._id, ge.getId());
				sct.setEL(KeyConstants._class, ge.getClassDefinition().getClassName());
				sct.setEL(KeyConstants._bundleName, ge.getClassDefinition().getName());
				sct.setEL(KeyConstants._bundleVersion, ge.getClassDefinition().getVersionAsString());
				sct.setEL(KeyConstants._listenerCfcPath, ge.getListenerCfcPath());
				sct.setEL(KeyConstants._cfcPath, ge.getCfcPath());
				sct.setEL(KeyConstants._startupMode, GatewayEntryImpl.toStartup(ge.getStartupMode(), "automatic"));
				sct.setEL(KeyConstants._custom, ge.getCustom());
				sct.setEL(KeyConstants._readOnly, Caster.toBoolean(ge.isReadOnly()));
				sct.setEL(KeyConstants._state, GatewayEngineImpl.toStringState(GatewayUtil.getState(ge), "failed"));

				pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
				return;
			}
		}
		throw new ApplicationException("No Gateway entry found with id [" + id + "]");
	}

	private void doGateway() throws PageException {

		String id = getString("admin", action, "id");
		String act = getString("admin", action, "gatewayAction").trim().toLowerCase();
		GatewayEngineImpl eng = ((GatewayEngineImpl) ((ConfigWebPro) config).getGatewayEngine());
		if ("restart".equals(act)) eng.restart(id);
		else if ("start".equals(act)) eng.start(id);
		else if ("stop".equals(act)) eng.stop(id);
		else throw new ApplicationException("Invalid gateway action [" + act + "], valid actions are [start,stop,restart]");
	}

	private void doGetCacheConnections() throws PageException {
		Map conns = config.getCacheConnections();
		Iterator it = conns.entrySet().iterator();
		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "class", "bundleName", "bundleVersion", "name", "custom", "default", "readOnly", "storage" }, 0, "connections");
		Map.Entry entry;
		CacheConnection cc;
		CacheConnection defObj = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_OBJECT);
		CacheConnection defTmp = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_TEMPLATE);
		CacheConnection defQry = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_QUERY);
		CacheConnection defRes = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_RESOURCE);
		CacheConnection defUDF = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_FUNCTION);
		CacheConnection defInc = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_INCLUDE);
		CacheConnection defHTT = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_HTTP);
		CacheConnection defFil = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_FILE);
		CacheConnection defWSe = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_WEBSERVICE);

		int row = 0;
		String def;
		while (it.hasNext()) {
			row++;
			entry = (Entry) it.next();
			cc = (CacheConnection) entry.getValue();
			qry.addRow();
			def = "";
			if (cc == defObj) def = "object";
			if (cc == defTmp) def = "template";
			if (cc == defQry) def = "query";
			if (cc == defRes) def = "resource";
			if (cc == defUDF) def = "function";
			if (cc == defInc) def = "include";
			if (cc == defHTT) def = "http";
			if (cc == defFil) def = "file";
			if (cc == defWSe) def = "webservice";

			qry.setAtEL(KeyConstants._class, row, cc.getClassDefinition().getClassName());
			qry.setAtEL(KeyConstants._bundleName, row, cc.getClassDefinition().getName());
			qry.setAtEL(KeyConstants._bundleVersion, row, cc.getClassDefinition().getVersionAsString());
			qry.setAtEL(KeyConstants._name, row, cc.getName());
			qry.setAtEL(KeyConstants._custom, row, cc.getCustom());
			qry.setAtEL(KeyConstants._default, row, def);
			qry.setAtEL(KeyConstants._readonly, row, Caster.toBoolean(cc.isReadOnly()));
			qry.setAtEL(KeyConstants._storage, row, Caster.toBoolean(cc.isStorage()));

		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doGetCacheDefaultConnection() throws PageException {
		int type;
		String strType = getString("admin", "GetCacheDefaultConnection", "cacheType");
		strType = strType.toLowerCase().trim();
		if (strType.equals("object")) type = ConfigPro.CACHE_TYPE_OBJECT;
		else if (strType.equals("template")) type = ConfigPro.CACHE_TYPE_TEMPLATE;
		else if (strType.equals("query")) type = ConfigPro.CACHE_TYPE_QUERY;
		else if (strType.equals("resource")) type = ConfigPro.CACHE_TYPE_RESOURCE;
		else if (strType.equals("function")) type = ConfigPro.CACHE_TYPE_FUNCTION;
		else if (strType.equals("include")) type = ConfigPro.CACHE_TYPE_INCLUDE;
		else if (strType.equals("http")) type = ConfigPro.CACHE_TYPE_HTTP;
		else if (strType.equals("file")) type = ConfigPro.CACHE_TYPE_FILE;
		else if (strType.equals("webservice")) type = ConfigPro.CACHE_TYPE_WEBSERVICE;
		else throw new ApplicationException("inv,query,resource invalid type definition, valid values are [object,template,query,resource,function,include]");

		CacheConnection cc = config.getCacheDefaultConnection(type);
		if (cc != null) {
			Struct sct = new StructImpl();

			sct.setEL(KeyConstants._name, cc.getName());
			sct.setEL(KeyConstants._class, cc.getClassDefinition().getClassName());
			sct.setEL(KeyConstants._bundleName, cc.getClassDefinition().getName());
			sct.setEL(KeyConstants._bundleVersion, cc.getClassDefinition().getVersionAsString());
			sct.setEL(KeyConstants._custom, cc.getCustom());
			sct.setEL(KeyConstants._default, Caster.toBoolean(true));
			sct.setEL(KeyConstants._readonly, Caster.toBoolean(cc.isReadOnly()));

			pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		}
		else throw new ApplicationException("There is no cache default connection");
	}

	private void doGetCacheConnection() throws PageException {

		String name = getString("admin", action, "name");
		Map conns = config.getCacheConnections();
		Iterator it = conns.keySet().iterator();
		CacheConnection cc;
		CacheConnection dObj = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_OBJECT);
		CacheConnection dTmp = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_TEMPLATE);
		CacheConnection dQry = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_QUERY);
		CacheConnection dRes = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_RESOURCE);
		CacheConnection dUDF = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_FUNCTION);
		CacheConnection dInc = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_INCLUDE);
		CacheConnection dHTT = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_HTTP);
		CacheConnection dFil = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_FILE);
		CacheConnection dWSe = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_WEBSERVICE);

		Struct sct;
		String d;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (key.equalsIgnoreCase(name)) {
				cc = (CacheConnection) conns.get(key);
				sct = new StructImpl();
				d = "";
				if (cc == dObj) d = "object";
				else if (cc == dTmp) d = "template";
				else if (cc == dQry) d = "query";
				else if (cc == dRes) d = "resource";
				else if (cc == dUDF) d = "function";
				else if (cc == dInc) d = "include";
				else if (cc == dHTT) d = "http";
				else if (cc == dFil) d = "file";
				else if (cc == dWSe) d = "webservice";

				sct.setEL(KeyConstants._name, cc.getName());
				sct.setEL(KeyConstants._class, cc.getClassDefinition().getClassName());
				sct.setEL(KeyConstants._bundleName, cc.getClassDefinition().getName());
				sct.setEL(KeyConstants._bundleVersion, cc.getClassDefinition().getVersionAsString());
				sct.setEL(KeyConstants._custom, cc.getCustom());
				sct.setEL(KeyConstants._default, d);
				sct.setEL(KeyConstants._readOnly, Caster.toBoolean(cc.isReadOnly()));
				sct.setEL(KeyConstants._storage, Caster.toBoolean(cc.isStorage()));

				pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
				return;
			}
		}
		throw new ApplicationException("There is no cache connection with name [" + name + "]");
	}

	private void doRemoveCacheConnection() throws PageException {
		admin.removeCacheConnection(getString("admin", action, "name"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveGatewayEntry() throws PageException {
		admin.removeGatewayEntry(getString("admin", action, "id"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveDebugEntry() throws PageException {
		admin.removeDebugEntry(getString("admin", action, "id"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doVerifyCacheConnection() throws PageException {

		try {
			Cache cache = CacheUtil.getCache(pageContext, getString("admin", action, "name"));
			if (cache instanceof CachePro) ((CachePro) cache).verify();
			else cache.getCustomInfo();
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetDatasource() throws PageException {

		String name = getString("admin", action, "name");
		Map ds = config.getDataSourcesAsMap();
		Iterator it = ds.keySet().iterator();

		while (it.hasNext()) {
			String key = (String) it.next();
			if (key.equalsIgnoreCase(name)) {
				DataSource d = (DataSource) ds.get(key);
				Struct sct = new StructImpl();
				ClassDefinition cd = d.getClassDefinition();

				sct.setEL(KeyConstants._name, key);
				sct.setEL(KeyConstants._host, d.getHost());
				sct.setEL(KeyConstants._classname, cd.getClassName());
				sct.setEL(KeyConstants._class, cd.getClassName());
				sct.setEL(KeyConstants._bundleName, cd.getName());
				sct.setEL(KeyConstants._bundleVersion, cd.getVersionAsString());
				sct.setEL(KeyConstants._dsn, d.getDsnOriginal());
				sct.setEL(KeyConstants._database, d.getDatabase());
				sct.setEL(KeyConstants._port, d.getPort() < 1 ? "" : Caster.toString(d.getPort()));
				sct.setEL(KeyConstants._dsnTranslated, d.getDsnTranslated());
				sct.setEL(KeyConstants._timezone, toStringTimeZone(d.getTimeZone()));
				sct.setEL(KeyConstants._password, d.getPassword());
				sct.setEL(KeyConstants._passwordEncrypted, ConfigWebUtil.encrypt(d.getPassword()));
				sct.setEL(KeyConstants._username, d.getUsername());
				sct.setEL(KeyConstants._readonly, Caster.toBoolean(d.isReadOnly()));
				sct.setEL(KeyConstants._select, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_SELECT)));
				sct.setEL(KeyConstants._delete, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_DELETE)));
				sct.setEL(KeyConstants._update, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_UPDATE)));
				sct.setEL(KeyConstants._insert, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_INSERT)));
				sct.setEL(KeyConstants._create, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_CREATE)));
				sct.setEL(KeyConstants._insert, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_INSERT)));
				sct.setEL(KeyConstants._drop, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_DROP)));
				sct.setEL(KeyConstants._grant, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_GRANT)));
				sct.setEL(KeyConstants._revoke, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_REVOKE)));
				sct.setEL(KeyConstants._alter, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_ALTER)));

				sct.setEL("connectionLimit", d.getConnectionLimit() < 1 ? "-1" : Caster.toString(d.getConnectionLimit()));
				sct.setEL("connectionTimeout", d.getConnectionTimeout() < 1 ? "" : Caster.toString(d.getConnectionTimeout()));
				sct.setEL("metaCacheTimeout", Caster.toDouble(d.getMetaCacheTimeout()));
				sct.setEL("custom", d.getCustoms());
				sct.setEL("blob", Boolean.valueOf(d.isBlob()));
				sct.setEL("clob", Boolean.valueOf(d.isClob()));
				sct.setEL("validate", Boolean.valueOf(d.validate()));
				sct.setEL("storage", Boolean.valueOf(d.isStorage()));
				if (d instanceof DataSourcePro) {
					DataSourcePro dp = ((DataSourcePro) d);
					sct.setEL("requestExclusive", Boolean.valueOf(dp.isRequestExclusive()));
					sct.setEL("alwaysResetConnections", Boolean.valueOf(dp.isAlwaysResetConnections()));
					sct.setEL("liveTimeout", dp.getLiveTimeout() < 1 ? "" : Caster.toString(dp.getLiveTimeout()));
				}

				if (d instanceof DataSourceImpl) {
					DataSourceImpl di = ((DataSourceImpl) d);
					sct.setEL("literalTimestampWithTSOffset", Boolean.valueOf(di.getLiteralTimestampWithTSOffset()));
					sct.setEL("alwaysSetTimeout", Boolean.valueOf(di.getAlwaysSetTimeout()));
					sct.setEL("dbdriver", Caster.toString(di.getDbDriver(), ""));
				}
				pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
				return;
			}
		}
		throw new ApplicationException("There is no datasource with name [" + name + "]");
	}

	private Object toStringTimeZone(TimeZone timeZone) {
		if (timeZone == null) return "";
		return timeZone.getID();
	}

	private void doGetRemoteClient() throws PageException {

		String url = getString("admin", action, "url");
		RemoteClient[] clients = config.getRemoteClients();
		RemoteClient client;
		for (int i = 0; i < clients.length; i++) {
			client = clients[i];

			if (client.getUrl().equalsIgnoreCase(url)) {
				Struct sct = new StructImpl();
				ProxyData pd = client.getProxyData();
				sct.setEL("label", client.getLabel());
				sct.setEL("usage", client.getUsage());
				sct.setEL("securityKey", client.getSecurityKey());
				sct.setEL("adminPassword", client.getAdminPassword());
				sct.setEL("ServerUsername", client.getServerUsername());
				sct.setEL("ServerPassword", client.getServerPassword());
				sct.setEL("type", client.getType());
				sct.setEL("url", client.getUrl());
				sct.setEL("proxyServer", pd == null ? "" : StringUtil.emptyIfNull(pd.getServer()));
				sct.setEL("proxyUsername", pd == null ? "" : StringUtil.emptyIfNull(pd.getUsername()));
				sct.setEL("proxyPassword", pd == null ? "" : StringUtil.emptyIfNull(pd.getPassword()));
				sct.setEL("proxyPort", pd == null ? "" : (pd.getPort() == -1 ? "" : Caster.toString(pd.getPort())));

				pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
				return;
			}
		}
		throw new ApplicationException("No remote client found with url [" + url + "]");
	}

	private void doGetSpoolerTasks() throws PageException {
		int startrow = getInt("startrow", 1);
		if (startrow < 1) startrow = 1;
		int maxrow = getInt("maxrow", -1);
		String result = getString("result", null);
		SpoolerEngineImpl engine = (SpoolerEngineImpl) config.getSpoolerEngine();

		Query qry = engine.getAllTasksAsQuery(startrow, maxrow);
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
		if (!StringUtil.isEmpty(result)) {
			Struct sct = new StructImpl();
			pageContext.setVariable(result, sct);
			sct.setEL("open", engine.getOpenTaskCount());
			sct.setEL("closed", engine.getClosedTaskCount());
		}

	}

	private int doGetRemoteClientTasks(lucee.runtime.type.Query qry, SpoolerTask[] tasks, int row) {
		SpoolerTask task;
		for (int i = 0; i < tasks.length; i++) {
			row++;
			task = tasks[i];
			try {
				qry.setAt("type", row, task.getType());
				qry.setAt("name", row, task.subject());
				qry.setAt("detail", row, task.detail());
				qry.setAt("id", row, task.getId());

				qry.setAt("lastExecution", row, new DateTimeImpl(pageContext, task.lastExecution(), true));
				qry.setAt("nextExecution", row, new DateTimeImpl(pageContext, task.nextExecution(), true));
				qry.setAt("closed", row, Caster.toBoolean(task.closed()));
				qry.setAt("tries", row, Caster.toDouble(task.tries()));
				qry.setAt("triesmax", row, Caster.toDouble(task.tries()));
				qry.setAt("exceptions", row, translateTime(task.getExceptions()));

				int triesMax = 0;
				ExecutionPlan[] plans = task.getPlans();
				for (int y = 0; y < plans.length; y++) {
					triesMax += plans[y].getTries();
				}
				qry.setAt("triesmax", row, Caster.toDouble(triesMax));
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		return row;
	}

	private Array translateTime(Array exp) {
		exp = (Array) Duplicator.duplicate(exp, true);
		Iterator<Object> it = exp.valueIterator();
		Struct sct;
		while (it.hasNext()) {
			sct = (Struct) it.next();
			sct.setEL("time", new DateTimeImpl(pageContext, Caster.toLongValue(sct.get("time", null), 0), true));
		}
		return exp;
	}

	private void doGetRemoteClients() throws PageException {
		RemoteClient[] clients = config.getRemoteClients();
		RemoteClient client;
		ProxyData pd;
		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "label", "usage", "securityKey", "adminPassword", "serverUsername", "serverPassword", "type", "url",
				"proxyServer", "proxyUsername", "proxyPassword", "proxyPort" }, clients.length, "query");

		int row = 0;

		for (int i = 0; i < clients.length; i++) {
			client = clients[i];
			pd = client.getProxyData();
			row = i + 1;
			qry.setAt("label", row, client.getLabel());
			qry.setAt("usage", row, client.getUsage());
			qry.setAt("securityKey", row, client.getSecurityKey());
			qry.setAt("adminPassword", row, client.getAdminPassword());
			qry.setAt("ServerUsername", row, client.getServerUsername());
			qry.setAt("ServerPassword", row, client.getServerPassword());
			qry.setAt("type", row, client.getType());
			qry.setAt("url", row, client.getUrl());
			qry.setAt("proxyServer", row, pd == null ? "" : pd.getServer());
			qry.setAt("proxyUsername", row, pd == null ? "" : pd.getUsername());
			qry.setAt("proxyPassword", row, pd == null ? "" : pd.getPassword());
			qry.setAt("proxyPort", row, pd == null ? "" : Caster.toString(pd.getPort()));

		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	private void doSetCluster() {// MUST remove this
		try {
			_doSetCluster();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	private void _doSetCluster() throws PageException {

		Struct entries = Caster.toStruct(getObject("admin", action, "entries"));
		Struct entry;
		Iterator<Object> it = entries.valueIterator();
		Cluster cluster = pageContext.clusterScope();
		while (it.hasNext()) {
			entry = Caster.toStruct(it.next());
			cluster.setEntry(new ClusterEntryImpl(KeyImpl.getInstance(Caster.toString(entry.get(KeyConstants._key))),
					Caster.toSerializable(entry.get(KeyConstants._value, null), null), Caster.toLongValue(entry.get(KeyConstants._time))));
		}

		cluster.broadcast();
	}

	private void doGetCluster() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), ((PageContextImpl) pageContext).clusterScope(false));
	}

	private void doGetToken() throws PageException {
		pageContext.setVariable(getString("admin", action, "returnVariable"), config.getIdentification().getSecurityToken());
	}

	private void doGetJDBCDrivers() throws PageException {

		JDBCDriver[] drivers = config.getJDBCDrivers();
		lucee.runtime.type.Query qry = new QueryImpl(
				new Key[] { KeyConstants._id, KeyConstants._label, KeyConstants._class, KeyConstants._bundleName, KeyConstants._bundleVersion, KeyConstants._connectionString },
				drivers.length, "jdbc");

		JDBCDriver driver;
		int row;
		for (int i = 0; i < drivers.length; i++) {
			row = i + 1;
			driver = drivers[i];
			if (!StringUtil.isEmpty(driver.id)) qry.setAt(KeyConstants._id, row, driver.id);
			if (!StringUtil.isEmpty(driver.connStr)) qry.setAt(KeyConstants._connectionString, row, driver.connStr);
			qry.setAt(KeyConstants._label, row, driver.label);
			qry.setAt(KeyConstants._class, row, driver.cd.getClassName());
			qry.setAt(KeyConstants._bundleName, row, driver.cd.getName());
			qry.setAt(KeyConstants._bundleVersion, row, driver.cd.getVersion().toString());
		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetDatasources() throws PageException {

		Map ds = config.getDataSourcesAsMap();
		Iterator it = ds.keySet().iterator();
		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "name", "host", "classname", "bundleName", "bundleVersion", "dsn", "DsnTranslated", "database", "port",
				"timezone", "username", "password", "passwordEncrypted", "readonly", "grant", "drop", "create", "revoke", "alter", "select", "delete", "update", "insert",
				"connectionLimit", "openConnections", "idleConnections", "activeConnections", "waitingForConnection", "connectionTimeout", "clob", "blob", "validate", "storage",
				"customSettings", "metaCacheTimeout" }, ds.size(), "query");

		int row = 0;

		while (it.hasNext()) {
			Object key = it.next();
			DataSource d = (DataSource) ds.get(key);
			row++;

			qry.setAt(KeyConstants._name, row, key);
			qry.setAt(KeyConstants._host, row, d.getHost());
			qry.setAt("classname", row, d.getClassDefinition().getClassName());
			qry.setAt("bundleName", row, d.getClassDefinition().getName());
			qry.setAt("bundleVersion", row, d.getClassDefinition().getVersionAsString());
			qry.setAt("dsn", row, d.getDsnOriginal());
			qry.setAt("database", row, d.getDatabase());
			qry.setAt(KeyConstants._port, row, d.getPort() < 1 ? "" : Caster.toString(d.getPort()));
			qry.setAt("dsnTranslated", row, d.getDsnTranslated());
			qry.setAt("timezone", row, toStringTimeZone(d.getTimeZone()));
			qry.setAt(KeyConstants._password, row, d.getPassword());

			qry.setAt("passwordEncrypted", row, ConfigWebUtil.encrypt(d.getPassword()));
			qry.setAt(KeyConstants._username, row, d.getUsername());
			qry.setAt(KeyConstants._readonly, row, Caster.toBoolean(d.isReadOnly()));
			qry.setAt(KeyConstants._select, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_SELECT)));
			qry.setAt(KeyConstants._delete, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_DELETE)));
			qry.setAt(KeyConstants._update, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_UPDATE)));
			qry.setAt(KeyConstants._create, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_CREATE)));
			qry.setAt(KeyConstants._insert, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_INSERT)));
			qry.setAt(KeyConstants._drop, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_DROP)));
			qry.setAt(KeyConstants._grant, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_GRANT)));
			qry.setAt(KeyConstants._revoke, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_REVOKE)));
			qry.setAt(KeyConstants._alter, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_ALTER)));

			// open connections
			int idle = 0, active = 0, waiters = 0;
			for (DatasourceConnPool pool: config.getDatasourceConnectionPools()) {
				if (!d.getName().equalsIgnoreCase(pool.getFactory().getDatasource().getName())) continue;
				idle += pool.getNumIdle();
				active += pool.getNumActive();
				waiters += pool.getNumWaiters();
			}

			qry.setAt("openConnections", row, idle + active);
			qry.setAt("idleConnections", row, idle);
			qry.setAt("activeConnections", row, active);
			qry.setAt("waitingForConnection", row, waiters);
			qry.setAt("connectionLimit", row, d.getConnectionLimit() < 1 ? "" : Caster.toString(d.getConnectionLimit()));
			qry.setAt("connectionTimeout", row, d.getConnectionTimeout() < 1 ? "" : Caster.toString(d.getConnectionTimeout()));
			// MUST add live and idle timeout and everything else posible
			qry.setAt("customSettings", row, d.getCustoms());
			qry.setAt("blob", row, Boolean.valueOf(d.isBlob()));
			qry.setAt("clob", row, Boolean.valueOf(d.isClob()));
			qry.setAt("validate", row, Boolean.valueOf(d.validate()));
			qry.setAt("storage", row, Boolean.valueOf(d.isStorage()));
			qry.setAt("metaCacheTimeout", row, Caster.toDouble(d.getMetaCacheTimeout()));

		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateScope() throws PageException {

		admin.updateScopeCascadingType(getString("admin", action, "scopeCascadingType"));
		admin.updateAllowImplicidQueryCall(getBoolObject("admin", action, "allowImplicidQueryCall"));
		admin.updateMergeFormAndUrl(getBoolObject("admin", action, "mergeFormAndUrl"));
		admin.updateSessionManagement(getBoolObject("admin", action, "sessionManagement"));
		admin.updateClientManagement(getBoolObject("admin", action, "clientManagement"));
		admin.updateDomaincookies(getBoolObject("admin", action, "domainCookies"));
		admin.updateClientCookies(getBoolObject("admin", action, "clientCookies"));
		// admin.updateRequestTimeout(getTimespan("admin",action,"requestTimeout"));
		admin.updateClientTimeout(getTimespan("admin", action, "clientTimeout"));
		admin.updateSessionTimeout(getTimespan("admin", action, "sessionTimeout"));
		admin.updateClientStorage(getString("admin", action, "clientStorage"));
		admin.updateCfidStorage(getString("admin", action, "cfidStorage"));
		admin.updateSessionStorage(getString("admin", action, "sessionStorage"));
		admin.updateApplicationTimeout(getTimespan("admin", action, "applicationTimeout"));
		admin.updateSessionType(getString("admin", action, "sessionType"));
		admin.updateLocalMode(getString("admin", action, "localMode"));
		admin.updateCGIReadonly(getBoolObject("admin", action, "cgiReadonly"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateDevelopMode() throws PageException {

		admin.updateMode(getBoolObject("admin", action, "mode"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateRestSettings() throws PageException {

		admin.updateRestList(getBool("list", null));
		// admin.updateRestAllowChanges(getBool("allowChanges", null));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateApplicationSettings() throws PageException {
		admin.updateRequestTimeout(getTimespan("admin", action, "requestTimeout"));
		admin.updateScriptProtect(getString("admin", action, "scriptProtect"));
		admin.updateAllowURLRequestTimeout(getBoolObject("admin", action, "allowURLRequestTimeout")); // DIFF 23
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateQueueSettings() throws PageException {
		admin.updateQueue(getInteger("admin", action, "max"), getInteger("admin", action, "timeout"), getBoolObject("admin", action, "enable"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateOutputSettings() throws PageException {
		admin.updateCFMLWriterType(getString("admin", action, "cfmlWriter"));
		admin.updateSuppressContent(getBoolObject("admin", action, "suppressContent"));
		// admin.updateShowVersion(getBoolObject("admin",action, "showVersion"));
		admin.updateAllowCompression(getBoolObject("admin", action, "allowCompression"));
		admin.updateContentLength(getBoolObject("admin", action, "contentLength"));
		admin.updateBufferOutput(getBoolObject("admin", action, "bufferOutput"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateCustomTagSetting() throws PageException {
		admin.updateCustomTagDeepSearch(getBool("admin", action, "deepSearch"));
		admin.updateCustomTagLocalSearch(getBool("admin", action, "localSearch"));
		admin.updateCTPathCache(getBool("admin", action, "customTagPathCache"));
		admin.updateCustomTagExtensions(getString("admin", action, "extensions"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/*
	 * private void doUpdateUpdateLogSettings() throws PageException { int
	 * level=LogUtil.toIntType(getString("admin", "updateUpdateLogSettings", "level"), -1); String
	 * source=getString("admin", "updateUpdateLogSettings", "path"); if(source.indexOf("{")==-1){
	 * Resource res = ResourceUtil.toResourceNotExisting(pageContext, source, false); String
	 * tmp=SystemUtil.addPlaceHolder(res, config, null);
	 * 
	 * 
	 * if(tmp!=null) source=tmp; else source=ContractPath.call(pageContext, source); }
	 * 
	 * admin.updateLogSettings( getString("admin", "updateUpdateLogSettings", "name"), level, source,
	 * getInt("admin", "updateUpdateLogSettings", "maxfile"), getInt("admin", "updateUpdateLogSettings",
	 * "maxfilesize") ); store(); adminSync.broadcast(attributes, config); }
	 */

	private void doUpdateAdminMode() throws PageException {
		admin.updateUpdateAdminMode(getString("admin", "updateAdminMode", "mode"), getBool("admin", "updateAdminMode", "merge"), getBool("admin", "updateAdminMode", "keep"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateMonitor() throws PageException {
		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());

		admin.updateMonitor(cd, getString("admin", "updateMonitor", "monitorType"), getString("admin", "updateMonitor", "name"), getBool("admin", "updateMonitor", "logEnabled"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateORMEngine() throws PageException {
		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());
		admin.updateORMEngine(cd);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateCacheHandler() throws PageException {
		ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());
		admin.updateCacheHandler(getString("admin", "updateCacheHandler", "id"), cd);
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateExecutionLog() throws PageException {
		lucee.runtime.db.ClassDefinition cd = new ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
				config.getIdentification());
		admin.updateExecutionLog(cd, getStruct("admin", "updateExecutionLog", "arguments"), getBool("admin", "updateExecutionLog", "enabled"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveMonitor() throws PageException {
		admin.removeMonitor(getString("admin", "removeMonitor", "type"), getString("admin", "removeMonitor", "name"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveCacheHandler() throws PageException {
		admin.removeCacheHandler(getString("admin", "removeCacheHandler", "id"));
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doRemoveORMEngine() throws PageException {
		admin.removeORMEngine();
		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateRHExtension() throws PageException {

		// ID
		String id = getString("id", null);
		if (!StringUtil.isEmpty(id)) {
			ExtensionDefintion ed;
			String version = getString("version", null);
			if (!StringUtil.isEmpty(version, true) && !"latest".equalsIgnoreCase(version)) ed = new ExtensionDefintion(id, version);
			else ed = RHExtension.toExtensionDefinition(id);
			DeployHandler.deployExtension(config, ed, config == null ? null : config.getLog("application"), true);
			return;
		}

		// this can be a binary that represent the extension, a string that is a path to the extension or a
		// base64 base encoded string
		Object obj = getObject("admin", "UpdateRHExtensions", "source");

		if (obj instanceof String) {
			String str = (String) obj;
			// we assume that when the string is more than 5000 it is a base64 encoded binary
			if (str.length() > 5000) {
				obj = Base64Encoder.decode(str);
			}
		}

		// path
		if (obj instanceof String) {
			Resource src = ResourceUtil.toResourceExisting(config, (String) obj);
			ConfigAdmin._updateRHExtension(config, src, true);
		}
		else {
			try {
				Resource tmp = SystemUtil.getTempFile("lex", true);
				IOUtil.copy(new ByteArrayInputStream(Caster.toBinary(obj)), tmp, true);
				ConfigAdmin._updateRHExtension(config, tmp, true);
			}
			catch (IOException ioe) {
				throw Caster.toPageException(ioe);
			}
		}

	}

	private void doRemoveRHExtension() throws PageException {
		String id = getString("admin", "removeRHExtensions", "id");
		if (!Decision.isUUId(id)) throw new ApplicationException("Invalid id [" + id + "], id must be a UUID");
		try {
			admin.removeRHExtension(id);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		store();
	}

	/*
	 * private void doUpdateExtension() throws PageException {
	 * 
	 * admin.updateExtension(pageContext, new ExtensionImpl(getStruct("config", null),
	 * getString("admin", "UpdateExtensions", "id"), getString("admin", "UpdateExtensions", "provider"),
	 * getString("admin", "UpdateExtensions", "version"),
	 * 
	 * getString("admin", "UpdateExtensions", "name"), getString("label", ""), getString("description",
	 * ""), getString("category", ""), getString("image", ""), getString("author", ""),
	 * getString("codename", ""), getString("video", ""), getString("support", ""),
	 * getString("documentation", ""), getString("forum", ""), getString("mailinglist", ""),
	 * getString("network", ""), getDateTime("created", null), getString("admin", "UpdateExtensions",
	 * "_type")));
	 * 
	 * store(); // adminSync.broadcast(attributes, config); }
	 */

	private void doUpdateExtensionProvider() throws PageException {
		admin.updateExtensionProvider(getString("admin", "UpdateExtensionProvider", "url"));
		store();
	}

	private void doUpdateRHExtensionProvider() throws PageException {
		try {
			admin.updateRHExtensionProvider(getString("admin", "UpdateRHExtensionProvider", "url"));
		}
		catch (MalformedURLException e) {
			throw Caster.toPageException(e);
		}
		store();
	}

	private void doUpdateExtensionInfo() throws PageException {
		admin.updateExtensionInfo(getBool("admin", "UpdateExtensionInfo", "enabled"));
		store();
	}

	private void doVerifyExtensionProvider() throws PageException {
		admin.verifyExtensionProvider(getString("admin", "VerifyExtensionProvider", "url"));

	}

	private void doResetId() throws PageException {
		admin.resetId();
		store();

	}

	private void doRemoveExtensionProvider() throws PageException {
		admin.removeExtensionProvider(getString("admin", "RemoveExtensionProvider", "url"));
		store();
	}

	private void doRemoveRHExtensionProvider() throws PageException {
		admin.removeRHExtensionProvider(getString("admin", "RemoveRHExtensionProvider", "url"));
		store();
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetApplicationSetting() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("scriptProtect", AppListenerUtil.translateScriptProtect(config.getScriptProtect()));

		// request timeout
		TimeSpan ts = config.getRequestTimeout();
		sct.set("requestTimeout", ts);
		sct.set("requestTimeout_day", Caster.toInteger(ts.getDay()));
		sct.set("requestTimeout_hour", Caster.toInteger(ts.getHour()));
		sct.set("requestTimeout_minute", Caster.toInteger(ts.getMinute()));
		sct.set("requestTimeout_second", Caster.toInteger(ts.getSecond()));

		// application path timeout
		ts = TimeSpanImpl.fromMillis(config.getApplicationPathCacheTimeout());
		sct.set("applicationPathTimeout", ts);
		sct.set("applicationPathTimeout_day", Caster.toInteger(ts.getDay()));
		sct.set("applicationPathTimeout_hour", Caster.toInteger(ts.getHour()));
		sct.set("applicationPathTimeout_minute", Caster.toInteger(ts.getMinute()));
		sct.set("applicationPathTimeout_second", Caster.toInteger(ts.getSecond()));

		// AllowURLRequestTimeout
		sct.set("AllowURLRequestTimeout", Caster.toBoolean(config.isAllowURLRequestTimeout()));

	}

	private void doGetQueueSetting() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set(KeyConstants._max, Caster.toInteger(config.getQueueMax()));
		sct.set(KeyConstants._timeout, Caster.toInteger(config.getQueueTimeout()));
		sct.set("enable", Caster.toBoolean(config.getQueueEnable()));
	}

	private void doGetOutputSetting() throws PageException {

		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("suppressContent", Caster.toBoolean(config.isSuppressContent()));
		sct.set("contentLength", Caster.toBoolean(config.contentLength()));
		// sct.set("showVersion",Caster.toBoolean(config.isShowVersion()));
		sct.set("allowCompression", Caster.toBoolean(config.allowCompression()));
		int wt = config.getCFMLWriterType();
		String cfmlWriter = "regular";
		if (wt == ConfigPro.CFML_WRITER_WS) cfmlWriter = "white-space";
		else if (wt == ConfigPro.CFML_WRITER_WS_PREF) cfmlWriter = "white-space-pref";

		sct.set("cfmlWriter", cfmlWriter);
		sct.set("bufferOutput", Caster.toBoolean(config.getBufferOutput()));

	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetScope() throws PageException {
		String sessionType = AppListenerUtil.toSessionType(config.getSessionType(), "application");
		String localMode = AppListenerUtil.toLocalMode(config.getLocalMode(), "classic");

		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("allowImplicidQueryCall", Caster.toBoolean(config.allowImplicidQueryCall()));
		sct.set("mergeFormAndUrl", Caster.toBoolean(config.mergeFormAndURL()));

		sct.set("sessiontype", sessionType);
		sct.set("localmode", localMode);
		sct.set("sessionManagement", Caster.toBoolean(config.isSessionManagement()));
		sct.set("clientManagement", Caster.toBoolean(config.isClientManagement()));
		sct.set("cfidStorage", config.getCfidStorage());
		sct.set("domainCookies", Caster.toBoolean(config.isDomainCookies()));
		sct.set("clientCookies", Caster.toBoolean(config.isClientCookies()));
		sct.set("clientStorage", config.getClientStorage());
		sct.set("sessionStorage", config.getSessionStorage());
		sct.set("cgiReadonly", config.getCGIScopeReadonly());

		TimeSpan ts = config.getSessionTimeout();
		sct.set("sessionTimeout", ts);
		sct.set("sessionTimeout_day", Caster.toInteger(ts.getDay()));
		sct.set("sessionTimeout_hour", Caster.toInteger(ts.getHour()));
		sct.set("sessionTimeout_minute", Caster.toInteger(ts.getMinute()));
		sct.set("sessionTimeout_second", Caster.toInteger(ts.getSecond()));

		ts = config.getApplicationTimeout();
		sct.set("applicationTimeout", ts);
		sct.set("applicationTimeout_day", Caster.toInteger(ts.getDay()));
		sct.set("applicationTimeout_hour", Caster.toInteger(ts.getHour()));
		sct.set("applicationTimeout_minute", Caster.toInteger(ts.getMinute()));
		sct.set("applicationTimeout_second", Caster.toInteger(ts.getSecond()));

		ts = config.getClientTimeout();
		sct.set("clientTimeout", ts);
		sct.set("clientTimeout_day", Caster.toInteger(ts.getDay()));
		sct.set("clientTimeout_hour", Caster.toInteger(ts.getHour()));
		sct.set("clientTimeout_minute", Caster.toInteger(ts.getMinute()));
		sct.set("clientTimeout_second", Caster.toInteger(ts.getSecond()));

		// scope cascading type
		if (config.getScopeCascadingType() == Config.SCOPE_STRICT) sct.set("scopeCascadingType", "strict");
		else if (config.getScopeCascadingType() == Config.SCOPE_SMALL) sct.set("scopeCascadingType", "small");
		else if (config.getScopeCascadingType() == Config.SCOPE_STANDARD) sct.set("scopeCascadingType", "standard");
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetDevelopMode() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("developMode", Caster.toBoolean(config.isDevelopMode()));
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateComponent() throws PageException {

		admin.updateComponentDeepSearch(getBoolObject("admin", action, "deepSearch"));
		admin.updateBaseComponent(getString("admin", action, "baseComponentTemplateCFML"), getString("admin", action, "baseComponentTemplateLucee"));
		admin.updateComponentDumpTemplate(getString("admin", action, "componentDumpTemplate"));
		admin.updateComponentDataMemberDefaultAccess(getString("admin", action, "componentDataMemberDefaultAccess"));
		admin.updateTriggerDataMember(getBoolObject("admin", action, "triggerDataMember"));
		admin.updateComponentUseShadow(getBoolObject("admin", action, "useShadow"));
		admin.updateComponentDefaultImport(getString("admin", action, "componentDefaultImport"));
		admin.updateComponentLocalSearch(getBoolObject("admin", action, "componentLocalSearch"));
		admin.updateComponentPathCache(getBoolObject("admin", action, "componentPathCache"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * @throws PageException
	 * 
	 */
	private void doGetComponent() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		// Base Component
		try {
			PageSource psCFML = config.getBaseComponentPageSource(CFMLEngine.DIALECT_CFML);

			if (psCFML != null && psCFML.exists()) sct.set("baseComponentTemplateCFML", psCFML.getDisplayPath());
			else sct.set("baseComponentTemplateCFML", "");
		}
		catch (PageException e) {
			sct.set("baseComponentTemplateCFML", "");
		}
		try {
			PageSource psLucee = config.getBaseComponentPageSource(CFMLEngine.DIALECT_LUCEE);

			if (psLucee != null && psLucee.exists()) sct.set("baseComponentTemplateLucee", psLucee.getDisplayPath());
			else sct.set("baseComponentTemplateLucee", "");

		}
		catch (PageException e) {
			sct.set("baseComponentTemplateLucee", "");
		}
		sct.set("strBaseComponentTemplateCFML", config.getBaseComponentTemplate(CFMLEngine.DIALECT_CFML));
		sct.set("strBaseComponentTemplateLucee", config.getBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE));

		// dump template
		try {
			PageSource ps = ((PageContextImpl) pageContext).getPageSourceExisting(config.getComponentDumpTemplate());
			if (ps != null) sct.set("componentDumpTemplate", ps.getDisplayPath());
			else sct.set("componentDumpTemplate", "");
		}
		catch (PageException e) {
			sct.set("componentDumpTemplate", "");
		}
		sct.set("strComponentDumpTemplate", config.getComponentDumpTemplate());

		sct.set("deepSearch", Caster.toBoolean(config.doComponentDeepSearch()));
		sct.set("componentDataMemberDefaultAccess", ComponentUtil.toStringAccess(config.getComponentDataMemberDefaultAccess()));
		sct.set("triggerDataMember", Caster.toBoolean(config.getTriggerComponentDataMember()));
		sct.set("useShadow", Caster.toBoolean(config.useComponentShadow()));
		sct.set("ComponentDefaultImport", config.getComponentDefaultImport());
		sct.set("componentLocalSearch", config.getComponentLocalSearch());
		sct.set("componentPathCache", config.useComponentPathCache());

	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doUpdateRegional() throws PageException {
		Boolean useTimeServer = getBool("usetimeserver", null);

		try {
			admin.updateLocale(getString("admin", action, "locale"));
			admin.updateTimeZone(getString("admin", action, "timezone"));
			admin.updateTimeServer(getString("admin", action, "timeserver"), useTimeServer);
		}
		finally {
			store();
		}
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateMonitorEnabled() throws PageException {

		try {
			admin.updateMonitorEnabled(getBool("admin", "UpdateMonitorEnabled", "monitorEnabled"));
		}
		finally {
			store();
		}
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateTLD() throws PageException {
		try {
			String jar = getString("jar", null);
			if (!StringUtil.isEmpty(jar, true)) {
				Resource resJar = ResourceUtil.toResourceExisting(pageContext, jar);
				admin.updateJar(resJar);
			}
			Resource resTld = ResourceUtil.toResourceExisting(pageContext, getString("admin", action, "tld"));
			admin.updateTLD(resTld);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		store();
	}

	private void doUpdateFLD() throws PageException {
		try {
			String jar = getString("jar", null);
			if (!StringUtil.isEmpty(jar, true)) {
				Resource resJar = ResourceUtil.toResourceExisting(pageContext, jar);
				admin.updateJar(resJar);
			}
			Resource resFld = ResourceUtil.toResourceExisting(pageContext, getString("admin", action, "fld"));
			admin.updateFLD(resFld);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		store();
	}

	private void doUpdateJar() throws PageException {
		try {
			Resource resJar = ResourceUtil.toResourceExisting(pageContext, getString("admin", action, "jar"));
			admin.updateJar(resJar);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		store();
	}

	private void doUpdateLoginSettings() throws PageException {
		boolean rememberMe = getBool("admin", "UpdateLoginSettings", "rememberme");
		boolean captcha = getBool("admin", "UpdateLoginSettings", "captcha");
		int delay = getInt("admin", "UpdateLoginSettings", "delay");
		admin.updateLoginSettings(captcha, rememberMe, delay);
		store();
	}

	private void doUpdateLogSettings() throws PageException {
		String str = getString("admin", "UpdateLogSettings", "level", true);
		int l = LogUtil.toLevel(str, -1);
		if (l == -1) throw new ApplicationException("Invalid log level name [" + str + "], valid log level names are [INFO,DEBUG,WARN,ERROR,FATAL,TRACE]");

		ClassDefinition acd = new ClassDefinitionImpl(getString("admin", action, "appenderClass", true), getString("appenderBundleName", null),
				getString("appenderBundleVersion", null), config.getIdentification());
		ClassDefinition lcd = new ClassDefinitionImpl(getString("admin", action, "layoutClass", true), getString("layoutBundleName", null), getString("layoutBundleVersion", null),
				config.getIdentification());

		admin.updateLogSettings(getString("admin", "UpdateLogSettings", "name", true), l, acd, Caster.toStruct(getObject("admin", "UpdateLogSettings", "appenderArgs")), lcd,
				Caster.toStruct(getObject("admin", "UpdateLogSettings", "layoutArgs")));
		store();
	}

	private void doUpdateSSLCertificate() throws PageException {
		String host = getString("admin", "UpdateSSLCertificateInstall", "host");
		int port = getInt("port", 443);
		updateSSLCertificate(config, host, port);
	}

	public static void updateSSLCertificate(Config config, String host, int port) throws PageException {
		Resource cacerts = config.getSecurityDirectory();

		try {
			CertificateInstaller installer = new CertificateInstaller(cacerts, host, port);
			installer.installAll();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void doGetSSLCertificate() throws PageException {
		String host = getString("admin", "GetSSLCertificate", "host");
		int port = getInt("port", 443);
		pageContext.setVariable(getString("admin", action, "returnVariable"), getSSLCertificate(config, host, port));
	}

	public static Query getSSLCertificate(Config config, String host, int port) throws PageException {
		Resource cacerts = config.getSecurityDirectory();
		CertificateInstaller installer;
		try {
			installer = new CertificateInstaller(cacerts, host, port);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		X509Certificate[] certs = installer.getCertificates();
		X509Certificate cert;

		Query qry = new QueryImpl(new String[] { "subject", "issuer" }, certs.length, "certificates");
		for (int i = 0; i < certs.length; i++) {
			cert = certs[i];
			qry.setAtEL("subject", i + 1, cert.getSubjectDN().getName());
			qry.setAtEL("issuer", i + 1, cert.getIssuerDN().getName());
		}
		return qry;
	}

	private void doRemoveBundle() throws PageException {
		try {
			String name = getString("admin", action, "name");
			String version = getString("admin", action, "version");
			boolean removePhysical = getBoolV("removePhysical", true);

			OSGiUtil.removeLocalBundle(name.trim(), OSGiUtil.toVersion(version.trim()), null, removePhysical, false);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		store();
	}

	private void doRemoveTLD() throws PageException {
		try {
			String name = getString("tld", null);
			if (StringUtil.isEmpty(name)) name = getString("admin", action, "name");
			admin.removeTLD(name);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		store();
	}

	private void doRemoveFLD() throws PageException {
		try {
			String name = getString("fld", null);
			if (StringUtil.isEmpty(name)) name = getString("admin", action, "name");
			admin.removeFLD(name);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		store();
	}

	private void doUpdateRemoteClient() throws PageException {

		admin.updateRemoteClient(getString("admin", action, "label"), getString("admin", action, "url"), getString("admin", action, "remotetype"),
				getString("admin", action, "securityKey"), getString("admin", action, "usage"), getString("admin", action, "adminPassword"), getString("ServerUsername", ""),
				getString("ServerPassword", ""), getString("proxyServer", ""), getString("proxyUsername", ""), getString("proxyPassword", ""), getString("proxyPort", "")

		);

		store();
	}

	private void doReadBundle() throws PageException {
		String ret = getString("admin", action, "returnvariable");
		Resource res = ResourceUtil.toResourceExisting(pageContext, getString("admin", action, "bundle"));
		if (!res.isFile()) throw new ApplicationException("[" + res + "] is not a file");
		try {
			Struct sct = new StructImpl();
			pageContext.setVariable(ret, BundleFile.getInstance(res).info());
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private void doBuildBundle() throws PageException {
		String name = getString("admin", action, "name");
		String symName = getString("symbolicname", null);
		String existingRelation = getString("existingrelation", null);
		// boolean doDyn=StringUtil.isEmpty(existingRelation) ||
		// (existingRelation=existingRelation.trim()).equalsIgnoreCase("dynamic");
		// print.e("dynamic:"+existingRelation+"<>"+doDyn);

		boolean ignoreExistingManifest = getBoolV("ignoreExistingManifest", false);
		Resource dest = ResourceUtil.toResourceNotExisting(pageContext, getString("admin", action, "destination"));
		String strJar = getString("admin", action, "jar");
		if (StringUtil.isEmpty(strJar, true)) throw new ApplicationException("Missing valid jar path");
		Resource jar = ResourceUtil.toResourceExisting(pageContext, strJar.trim());

		Set<String> relatedPackages = null;
		try {
			relatedPackages = JarUtil.getExternalImports(jar, new String[0]);// OSGiUtil.getBootdelegation()
		}
		catch (IOException e1) {
			LogUtil.log(ThreadLocalPageContext.getConfig(pageContext), Admin.class.getName(), e1);
		}
		if (relatedPackages == null) relatedPackages = new HashSet<String>();

		// org.osgi.framework.bootdelegation

		BundleBuilderFactory factory;
		try {
			symName = StringUtil.isEmpty(symName, true) ? null : symName.trim();
			if (symName == null) symName = name;

			factory = new BundleBuilderFactory(jar, symName);
			factory.setName(name);
			factory.setIgnoreExistingManifest(ignoreExistingManifest);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		String activator = getString("bundleActivator", null);
		if (activator == null) activator = getString("activator", null);

		if (!StringUtil.isEmpty(activator, true)) factory.setActivator(activator.trim());

		String version = getString("version", null);
		if (!StringUtil.isEmpty(version, true)) factory.setVersion(OSGiUtil.toVersion(version, null));

		String description = getString("description", null);
		if (!StringUtil.isEmpty(description, true)) factory.setDescription(description.trim());

		String classPath = getString("classPath", null);
		if (!StringUtil.isEmpty(classPath, true)) factory.addClassPath(classPath.trim());

		// dynamic import packages
		String dynamicImportPackage = getString("dynamicimportpackage", null);
		if (!StringUtil.isEmpty(dynamicImportPackage, true)) factory.addDynamicImportPackage(dynamicImportPackage = dynamicImportPackage.trim());
		Set<String> dynamicImportPackageSet = ListUtil.listToSet(dynamicImportPackage, ",", true);
		/*
		 * String dynamicImportPackage=getString("dynamicimportpackage",null); if(doDyn) {
		 * if(relatedPackages.size()>0) { // add importPackage to set
		 * if(!StringUtil.isEmpty(dynamicImportPackage)) { String[] arr =
		 * ListUtil.trimItems(ListUtil.listToStringArray(dynamicImportPackage, ',')); for(int
		 * i=0;i<arr.length;i++){ relatedPackages.add(arr[i]); } }
		 * dynamicImportPackage=ListUtil.toList(relatedPackages, ","); } relatedPackages.clear(); }
		 * if(!StringUtil.isEmpty(dynamicImportPackage,true))factory.addDynamicImportPackage(
		 * dynamicImportPackage.trim());
		 */

		// Import Package
		// we remove all imports that are defined as dyn import
		Iterator<String> it = dynamicImportPackageSet.iterator();
		while (it.hasNext()) {
			relatedPackages.remove(it.next());
		}
		String importPackage = getString("importpackage", null);
		// add importPackage to set
		if (!StringUtil.isEmpty(importPackage)) {
			String[] arr = ListUtil.trimItems(ListUtil.listToStringArray(importPackage, ','));
			for (int i = 0; i < arr.length; i++) {
				relatedPackages.add(arr[i]);
			}
		}

		// remove all packages defined in dynamic imports
		if (!StringUtil.isEmpty(dynamicImportPackage)) {
			String[] arr = ListUtil.trimItems(ListUtil.listToStringArray(dynamicImportPackage, ','));
			List<String> newDynImport = new ArrayList<String>();
			for (int i = 0; i < arr.length; i++) {
				if (!relatedPackages.contains(arr[i])) newDynImport.add(arr[i]);
				// relatedPackages.remove(arr[i]);
			}
			if (arr.length != newDynImport.size()) dynamicImportPackage = ListUtil.listToListEL(newDynImport, ",");
		}

		List sortedList = new ArrayList(relatedPackages);
		Collections.sort(sortedList);
		importPackage = ListUtil.toList(sortedList, ",");
		if (!StringUtil.isEmpty(importPackage, true)) factory.addImportPackage(importPackage.trim());

		String bundleActivationPolicy = getString("bundleActivationPolicy", null);
		if (!StringUtil.isEmpty(bundleActivationPolicy, true)) factory.setBundleActivationPolicy(bundleActivationPolicy.trim());

		String exportPackage = getString("exportpackage", null);

		if (!StringUtil.isEmpty(exportPackage, true)) {
			exportPackage = ListUtil.sort(exportPackage.trim(), "text", "asc", ",");
			factory.addExportPackage(exportPackage);
		}

		String requireBundle = getString("requireBundle", null);
		if (!StringUtil.isEmpty(requireBundle, true)) {
			requireBundle = ListUtil.sort(requireBundle.trim(), "text", "asc", ",");
			factory.addRequireBundle(requireBundle);
		}

		String requireBundleFragment = getString("requireBundleFragment", null);
		if (!StringUtil.isEmpty(requireBundleFragment, true)) {
			requireBundleFragment = ListUtil.sort(requireBundleFragment.trim(), "text", "asc", ",");
			factory.addRequireBundleFragment(requireBundleFragment);
		}

		String fragmentHost = getString("fragmentHost", null);
		if (!StringUtil.isEmpty(fragmentHost, true)) factory.addFragmentHost(fragmentHost.trim());

		try {
			factory.build(dest);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}

	}

	private void doUpdateRemoteClientUsage() throws PageException {
		admin.updateRemoteClientUsage(getString("admin", action, "code"), getString("admin", action, "displayname")

		);
		store();
	}

	private void doRemoveRemoteClientUsage() throws PageException {
		admin.removeRemoteClientUsage(getString("admin", action, "code")

		);
		store();
	}

	private String getCallerId() throws IOException {
		if (type == TYPE_WEB) {
			return config.getIdentification().getId();
		}
		if (config instanceof ConfigWeb) {
			ConfigWeb cw = (ConfigWeb) config;
			return cw.getIdentification().getServerIdentification().getId();
		}
		if (config instanceof ConfigServer) {
			return config.getIdentification().getId();
		}
		throw new IOException("can not create id");
	}

	private void doUpdateApplicationListener() throws PageException {
		admin.updateApplicationListener(getString("admin", action, "listenerType"), getString("admin", action, "listenerMode"));
		admin.updateApplicationPathTimeout(getTimespan("admin", action, "applicationPathTimeout"));

		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateCachedWithin() throws PageException {
		String str = getString("admin", action, "cachedWithinType");
		int type = AppListenerUtil.toCachedWithinType(str, -1);
		if (type == -1) throw new ApplicationException("Cached within type [" + str + "] is invalid, valid types are [function,include,query,resource]");
		admin.updateCachedWithin(type, getString("admin", action, "cachedWithin"));

		store();
		adminSync.broadcast(attributes, config);
	}

	private void doUpdateProxy() throws PageException {
		admin.updateProxy(getBool("admin", action, "proxyenabled"), getString("admin", action, "proxyserver"), getInt("admin", action, "proxyport"),
				getString("admin", action, "proxyusername"), getString("admin", action, "proxypassword"));
		store();
	}

	private void doUpdateCharset() throws PageException {
		admin.updateResourceCharset(getString("admin", action, "resourceCharset"));
		admin.updateTemplateCharset(getString("admin", action, "templateCharset"));
		admin.updateWebCharset(getString("admin", action, "webCharset"));
		store();
		adminSync.broadcast(attributes, config);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doSecurityManager() throws PageException {
		String rtnVar = getString("admin", action, "returnVariable");
		String secType = getString("admin", action, "sectype");
		String secValue = getString("secvalue", null);
		boolean isServer = config instanceof ConfigServer;

		if (secValue == null) {
			if (isServer) {
				pageContext.setVariable(rtnVar, SecurityManagerImpl.toStringAccessValue(SecurityManager.VALUE_YES));
			}
			else {
				pageContext.setVariable(rtnVar, SecurityManagerImpl.toStringAccessValue(config.getSecurityManager().getAccess(secType)));
			}
			return;
		}
		pageContext.setVariable(rtnVar, Caster.toBoolean(isServer || config.getSecurityManager().getAccess(secType) == SecurityManagerImpl.toShortAccessValue(secValue)));
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetTimeZones() throws PageException {

		String strLocale = getString("locale", "english (united kingdom)");
		Locale locale = LocaleFactory.getLocale(strLocale);

		String[] timeZones = TimeZone.getAvailableIDs();
		lucee.runtime.type.Query qry = new QueryImpl(new String[] { "id", "display" }, new String[] { "varchar", "varchar" }, timeZones.length, "timezones");
		Arrays.sort(timeZones);
		TimeZone timeZone;
		for (int i = 0; i < timeZones.length; i++) {
			timeZone = TimeZone.getTimeZone(timeZones[i]);
			qry.setAt("id", i + 1, timeZones[i]);
			qry.setAt("display", i + 1, timeZone.getDisplayName(locale));

		}
		pageContext.setVariable(getString("admin", action, "returnVariable"), qry);
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetLocales() throws PageException {
		Struct sct = new StructImpl(StructImpl.TYPE_LINKED);
		// Array arr=new ArrayImpl();
		String strLocale = getString("locale", "english (united kingdom)");
		Locale locale = LocaleFactory.getLocale(strLocale);
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		Map locales = LocaleFactory.getLocales();
		Iterator it = locales.keySet().iterator();

		String key;
		Locale l;
		while (it.hasNext()) {
			key = (String) it.next();
			l = (Locale) locales.get(key);
			sct.setEL(l.toString(), l.getDisplayName(locale));
			// arr.append(locale.getDisplayName());
		}
		// arr.sort("textnocase","asc");
	}

	private void doGetApplicationListener() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		ApplicationListener appListener = config.getApplicationListener();
		sct.set("type", AppListenerUtil.toStringType(appListener));
		sct.set("mode", AppListenerUtil.toStringMode(appListener.getMode()));
		// replaced with encoding outputsct.set("defaultencoding", config.get DefaultEncoding());
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetRegional() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("locale", Caster.toString(config.getLocale()));
		sct.set("timezone", toStringTimeZone(config.getTimeZone()));
		sct.set("timeserver", config.getTimeServer());
		sct.set("usetimeserver", config.getUseTimeServer());
		// replaced with encoding outputsct.set("defaultencoding", config.get DefaultEncoding());
	}

	private void doIsMonitorEnabled() throws PageException {
		if (config instanceof ConfigServerImpl) {
			ConfigServerImpl cs = (ConfigServerImpl) config;
			pageContext.setVariable(getString("admin", action, "returnVariable"), Caster.toBoolean(cs.isMonitoringEnabled()));
		}
	}

	private void doSurveillance() throws PageException {
		// Server
		if (config instanceof ConfigServer) {
			ConfigServer cs = (ConfigServer) config;
			ConfigWeb[] webs = cs.getConfigWebs();
			Struct sct = new StructImpl();
			for (int i = 0; i < webs.length; i++) {
				ConfigWeb cw = webs[i];
				try {
					sct.setEL(cw.getIdentification().getId(), ((CFMLFactoryImpl) cw.getFactory()).getInfo());
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
			pageContext.setVariable(getString("admin", action, "returnVariable"), sct);

		}
		// Web
		else {
			CFMLFactoryImpl factory = (CFMLFactoryImpl) ((ConfigWeb) config).getFactory();
			pageContext.setVariable(getString("admin", action, "returnVariable"), factory.getInfo());
		}
	}

	private void doStopThread() throws PageException {
		String contextId = getString("admin", "stopThread", "contextId");
		String threadId = getString("admin", "stopThread", "threadId");
		String stopType = getString("stopType", "exception");

		if (!(config instanceof ConfigServer)) throw new ApplicationException("Invalid context for this action");

		ConfigServer cs = (ConfigServer) config;
		ConfigWeb[] webs = cs.getConfigWebs();
		boolean has = false;
		for (int i = 0; i < webs.length; i++) {
			ConfigWeb cw = webs[i];
			if (!cw.getIdentification().getId().equals(contextId)) continue;
			((CFMLFactoryImpl) cw.getFactory()).stopThread(threadId, stopType);
			has = true;
			break;
		}
		if (!has) {
			for (int i = 0; i < webs.length; i++) {
				ConfigWeb cw = webs[i];
				if (!contextId.equals(cw.getLabel())) continue;
				((CFMLFactoryImpl) cw.getFactory()).stopThread(threadId, stopType);
				has = true;
				break;
			}
		}

	}

	private void doHeapDump() throws PageException {
		String strDestination = getString("admin", action, "destination");
		boolean live = getBoolV("live", true);
		Resource destination = ResourceUtil.toResourceNotExisting(pageContext, strDestination);
		try {
			HeapDumper.dumpTo(destination, live);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private void doGetProxy() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		ProxyData pd = config.getProxyData();
		String port = pd == null || pd.getPort() <= 0 ? "" : Caster.toString(pd.getPort());

		// sct.set("enabled",Caster.toBoolean(config.isProxyEnable()));
		sct.set("port", port);
		sct.set("server", pd == null ? "" : emptyIfNull(pd.getServer()));
		sct.set("username", pd == null ? "" : emptyIfNull(pd.getUsername()));
		sct.set("password", pd == null ? "" : emptyIfNull(pd.getPassword()));
	}

	private void doGetLoginSettings() throws ApplicationException, PageException {
		Struct sct = new StructImpl();
		ConfigPro c = (ConfigPro) ThreadLocalPageContext.getConfig(config);
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("captcha", Caster.toBoolean(c.getLoginCaptcha()));
		sct.set("delay", Caster.toDouble(c.getLoginDelay()));
		sct.set("rememberme", Caster.toBoolean(c.getRememberMe()));
		if (c instanceof ConfigWebPro) {
			ConfigWebPro cw = (ConfigWebPro) c;
			short origin = cw.getPasswordSource();
			if (origin == ConfigWebPro.PASSWORD_ORIGIN_DEFAULT) sct.set("origin", "default");
			else if (origin == ConfigWebPro.PASSWORD_ORIGIN_WEB) sct.set("origin", "web");
			else if (origin == ConfigWebPro.PASSWORD_ORIGIN_SERVER) sct.set("origin", "server");
		}

	}

	private void doGetCharset() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		sct.set("resourceCharset", config.getResourceCharset().name());
		sct.set("templateCharset", config.getTemplateCharset().name());
		sct.set("webCharset", ((PageContextImpl) pageContext).getWebCharset().name());
		sct.set("jreCharset", SystemUtil.getCharset().name());
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetUpdate() throws PageException {
		Struct sct = new StructImpl();
		pageContext.setVariable(getString("admin", action, "returnVariable"), sct);
		URL location = config.getUpdateLocation();
		if (location == null) location = Constants.DEFAULT_UPDATE_URL;

		String type = config.getUpdateType();
		if (StringUtil.isEmpty(type)) type = "manual";

		sct.set("location", location.toExternalForm());
		sct.set("type", type);

	}

	/**
	 * @throws PageException
	 * 
	 */
	private void store() throws PageException {
		try {
			admin.storeAndReload();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private String getString(String tagName, String actionName, String attributeName) throws ApplicationException {
		return getString(tagName, actionName, attributeName, true);
	}

	private String getString(String tagName, String actionName, String attributeName, boolean trim) throws ApplicationException {
		String value = getString(attributeName, null);
		if (value == null)
			throw new ApplicationException("Attribute [" + attributeName + "] for tag [" + tagName + "] is required if attribute action has the value [" + actionName + "]");
		return trim ? value.trim() : value;
	}

	private double getDouble(String tagName, String actionName, String attributeName) throws ApplicationException {
		double value = getDouble(attributeName, Double.NaN);
		if (!Decision.isValid(value))
			throw new ApplicationException("Attribute [" + attributeName + "] for tag [" + tagName + "] is required if attribute action has the value [" + actionName + "]");
		return value;
	}

	private String getString(String attributeName, String defaultValue) {
		Object value = attributes.get(attributeName, null);
		if (value == null) return defaultValue;
		return Caster.toString(value, null);
	}

	private DateTime getDateTime(String attributeName, DateTime defaultValue) {
		Object value = attributes.get(attributeName, null);
		if (value == null) return defaultValue;
		return DateCaster.toDateAdvanced(value, null, defaultValue);
	}

	private Object getObject(String attributeName, Object defaultValue) {
		return attributes.get(attributeName, defaultValue);
	}

	private boolean getBool(String tagName, String actionName, String attributeName) throws PageException {
		Object value = attributes.get(attributeName, null);
		if (value == null)
			throw new ApplicationException("Attribute [" + attributeName + "] for tag [" + tagName + "] is required if attribute action has the value [" + actionName + "]");
		return Caster.toBooleanValue(value);
	}

	private Boolean getBoolObject(String tagName, String actionName, String attributeName) throws PageException {
		Object value = attributes.get(attributeName, null);
		if (value == null)
			throw new ApplicationException("Attribute [" + attributeName + "] for tag [" + tagName + "] is required if attribute action has the value [" + actionName + "]");
		if (StringUtil.isEmpty(value)) return null;
		return Caster.toBoolean(value);
	}

	private Object getObject(String tagName, String actionName, String attributeName) throws PageException {
		Object value = attributes.get(attributeName, null);
		if (value == null)
			throw new ApplicationException("Attribute [" + attributeName + "] for tag [" + tagName + "] is required if attribute action has the value [" + actionName + "]");
		return value;
	}

	private long toTimeout(Object timeout, long defaultValue) throws PageException {
		if (timeout instanceof TimeSpan) return ((TimeSpan) timeout).getMillis();
		// seconds
		int i = Caster.toIntValue(timeout);
		if (i < 0) throw new ApplicationException("Invalid value [" + i + "], value must be a positive integer greater or equal than 0");
		return i * 1000;
	}

	private boolean getBoolV(String attributeName, boolean defaultValue) {
		Object value = attributes.get(attributeName, null);
		if (value == null) return defaultValue;
		return Caster.toBooleanValue(value, defaultValue);
	}

	private Boolean getBool(String attributeName, Boolean defaultValue) {
		Object value = attributes.get(attributeName, null);
		if (value == null) return defaultValue;
		return Caster.toBoolean(value, defaultValue);
	}

	private Struct getStruct(String attributeName, Struct defaultValue) {
		Object value = attributes.get(attributeName, null);
		if (value == null) return defaultValue;
		try {
			return Caster.toStruct(value);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	private Struct getStruct(String tagName, String actionName, String attributeName) throws PageException {
		Object value = attributes.get(attributeName, null);
		if (value == null)
			throw new ApplicationException("Attribute [" + attributeName + "] for tag [" + tagName + "] is required if attribute action has the value [" + actionName + "]");
		return Caster.toStruct(value);
	}

	private Integer getInteger(String tagName, String actionName, String attributeName) throws PageException {
		Object value = attributes.get(attributeName, null);
		if (value == null)
			throw new ApplicationException("Attribute [" + attributeName + "] for tag [" + tagName + "] is required if attribute action has the value [" + actionName + "]");
		if (StringUtil.isEmpty(value)) return null;
		return Caster.toIntValue(value);
	}

	private int getInt(String tagName, String actionName, String attributeName) throws PageException {
		Object value = attributes.get(attributeName, null);
		if (value == null)
			throw new ApplicationException("Attribute [" + attributeName + "] for tag [" + tagName + "] is required if attribute action has the value [" + actionName + "]");
		return (int) Caster.toDoubleValue(value);
	}

	private int getInt(String attributeName, int defaultValue) {
		Object value = attributes.get(attributeName, null);
		if (value == null) return defaultValue;
		return Caster.toIntValue(value, defaultValue);
	}

	private long getLong(String attributeName, long defaultValue) {
		Object value = attributes.get(attributeName, null);
		if (value == null) return defaultValue;
		return Caster.toLongValue(value, defaultValue);
	}

	private double getDouble(String attributeName, double defaultValue) {
		Object value = attributes.get(attributeName, null);
		if (value == null) return defaultValue;
		return Caster.toDoubleValue(value, true, defaultValue);
	}

	private TimeSpan getTimespan(String tagName, String actionName, String attributeName) throws PageException {
		Object value = attributes.get(attributeName, null);
		if (value == null)
			throw new ApplicationException("Attribute [" + attributeName + "] for tag [" + tagName + "] is required if attribute action has the value [" + actionName + "]");
		if (StringUtil.isEmpty(value)) return null;
		return Caster.toTimespan(value);
	}

	private Object emptyIfNull(String str) {
		if (str == null) return "";
		return str;
	}

	private void throwNoAccessWhenWeb() throws ApplicationException {
		if (!singleMode && type == TYPE_WEB) throw new ApplicationException("Action [" + action + "] is not available for Web Admin ( Server Admin only )");
	}

	private void throwNoAccessWhenServer() throws ApplicationException {
		if (!singleMode && type == TYPE_SERVER) {
			throw new ApplicationException("Action [" + action + "] is not available for Server Admin ( Web Admin only )");
		}
	}
}
