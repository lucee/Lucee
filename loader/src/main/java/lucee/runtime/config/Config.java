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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.Extension;
import lucee.runtime.extension.ExtensionProvider;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.monitor.ActionMonitor;
import lucee.runtime.monitor.IntervallMonitor;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.rest.RestSettings;
import lucee.runtime.schedule.Scheduler;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.spooler.SpoolerEngine;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.video.VideoExecuter;

/**
 * interface for Config Object
 */
public interface Config {

	/**
	 * Define a strict scope cascading
	 */
	public static final short SCOPE_STRICT = 0;

	/**
	 * Define a small scope cascading
	 */
	public static final short SCOPE_SMALL = 1;

	/**
	 * Define a standart scope cascading (like other cf versions)
	 */
	public static final short SCOPE_STANDARD = 2;

	/**
	 * Field <code>CLIENT_SCOPE_TYPE_COOKIE</code>
	 */
	public static final short CLIENT_SCOPE_TYPE_COOKIE = 0;

	/**
	 * Field <code>CLIENT_SCOPE_TYPE_FILE</code>
	 */
	public static final short CLIENT_SCOPE_TYPE_FILE = 1;

	/**
	 * Field <code>CLIENT_SCOPE_TYPE_DB</code>
	 */
	public static final short CLIENT_SCOPE_TYPE_DB = 2;

	/**
	 * Field <code>SESSION_TYPE_APPLICATION</code>
	 */
	public static final short SESSION_TYPE_APPLICATION = 0;

	/**
	 * Field <code>SESSION_TYPE_J2EE</code>
	 */
	public static final short SESSION_TYPE_JEE = 1;

	/**
	 * Field <code>RECOMPILE_NEVER</code>
	 */
	public static final short RECOMPILE_NEVER = 0;
	/**
	 * Field <code>RECOMPILE_AT_STARTUP</code>
	 */
	public static final short RECOMPILE_AFTER_STARTUP = 1;
	/**
	 * Field <code>RECOMPILE_ALWAYS</code>
	 */
	public static final short RECOMPILE_ALWAYS = 2;

	public static final short INSPECT_ALWAYS = 0;
	public static final short INSPECT_ONCE = 1;
	public static final short INSPECT_NEVER = 2;
	// Hibernate Extension has hardcoded this 4, do not change!!!!
	public static final short INSPECT_UNDEFINED = 4;

	/*
	 * public static final int CUSTOM_TAG_MODE_NONE = 0; public static final int CUSTOM_TAG_MODE_CLASSIC
	 * = 1; public static final int CUSTOM_TAG_MODE_MODERN = 2; public static final int
	 * CUSTOM_TAG_MODE_CLASSIC_MODERN = 4; public static final int CUSTOM_TAG_MODE_MODERN_CLASSIC = 8;
	 */

	public static final int CACHE_TYPE_NONE = 0;
	public static final int CACHE_TYPE_OBJECT = 1;
	public static final int CACHE_TYPE_TEMPLATE = 2;
	public static final int CACHE_TYPE_QUERY = 4;
	public static final int CACHE_TYPE_RESOURCE = 8;
	public static final int CACHE_TYPE_FUNCTION = 16;
	public static final int CACHE_TYPE_INCLUDE = 32;
	public static final int CACHE_TYPE_HTTP = 64;
	public static final int CACHE_TYPE_FILE = 128;
	public static final int CACHE_TYPE_WEBSERVICE = 256;

	public static final int CACHEDWITHIN_QUERY = CACHE_TYPE_QUERY;
	public static final int CACHEDWITHIN_RESOURCE = CACHE_TYPE_RESOURCE;
	public static final int CACHEDWITHIN_FUNCTION = CACHE_TYPE_FUNCTION;
	public static final int CACHEDWITHIN_INCLUDE = CACHE_TYPE_INCLUDE;
	public static final int CACHEDWITHIN_HTTP = CACHE_TYPE_HTTP;
	public static final int CACHEDWITHIN_FILE = CACHE_TYPE_FILE;
	public static final int CACHEDWITHIN_WEBSERVICE = CACHE_TYPE_WEBSERVICE;

	public short getInspectTemplate();

	public String getDefaultDataSource();

	/**
	 * return how lucee cascade scopes
	 * 
	 * @return type of cascading
	 */
	public abstract short getScopeCascadingType();

	// public abstract String[] getCFMLExtensions();

	// public abstract String getCFCExtension();

	// public abstract String getComponentExtension();

	// public abstract String[] getTemplateExtensions();

	// public abstract String[] getAllExtensions();

	/**
	 * return the mapping to custom tag directory
	 * 
	 * @return custom tag directory
	 */
	public abstract Mapping[] getCustomTagMappings();

	/**
	 * return if it is allowed to implizid query call, call a query member witot define name of the
	 * query.
	 * 
	 * @return is allowed
	 */
	public abstract boolean allowImplicidQueryCall();

	/**
	 * e merged return if url and form scope will b
	 * 
	 * @return merge or not
	 */
	public abstract boolean mergeFormAndURL();

	/**
	 * @return Returns the application Timeout.
	 */
	public abstract TimeSpan getApplicationTimeout();

	/**
	 * @return Returns the session Timeout.
	 */
	public abstract TimeSpan getSessionTimeout();

	/**
	 * @return Returns the client Timeout.
	 */
	public TimeSpan getClientTimeout();

	/**
	 * @return Returns the request Timeout.
	 */
	public abstract TimeSpan getRequestTimeout();

	/**
	 * @return Returns the clientCookies.
	 */
	public abstract boolean isClientCookies();

	/**
	 * @return Returns the clientManagement.
	 */
	public abstract boolean isClientManagement();

	/**
	 * @return Returns the domainCookies.
	 */
	public abstract boolean isDomainCookies();

	/**
	 * @return Returns the sessionManagement.
	 */
	public abstract boolean isSessionManagement();

	/**
	 * @return Returns the spoolEnable.
	 */
	public abstract boolean isMailSpoolEnable();

	/**
	 * @return Returns the mailTimeout.
	 */
	public abstract int getMailTimeout();

	/**
	 * @return preserve single quotes in cfquery tag or not
	 */
	public abstract boolean getPSQL();

	/**
	 * @return Returns the locale.
	 */
	public abstract Locale getLocale();

	/**
	 * return if debug output will be generated
	 * 
	 * @return debug or not
	 */
	public abstract boolean debug();

	/**
	 * return the temp directory
	 * 
	 * @return temp directory
	 */
	public abstract Resource getTempDirectory();

	/**
	 * @return Returns the spoolInterval.
	 */
	public abstract int getMailSpoolInterval();

	/**
	 * @return returns the time zone for this
	 */
	public abstract TimeZone getTimeZone();

	/**
	 * @return returns the offset from the timeserver to local time
	 */
	public abstract long getTimeServerOffset();

	/**
	 * @return return if a password is set
	 */
	public abstract boolean hasPassword();

	/**
	 * @param password password
	 * @return return if a password is set
	 */
	public abstract boolean passwordEqual(Password password);

	/**
	 * @return return if a password is set
	 */
	public abstract boolean hasServerPassword();

	/**
	 * @return Returns the mappings.
	 */
	public abstract Mapping[] getMappings();

	/**
	 * @return Returns the configDir.
	 */
	public abstract Resource getConfigDir();

	/**
	 * @return Returns the configFile.
	 */
	public abstract Resource getConfigFile();

	/**
	 * @return Returns the loadTime.
	 */
	public abstract long getLoadTime();

	/**
	 * @param dialect dialect
	 * @return Returns the baseComponent.
	 */
	public abstract String getBaseComponentTemplate(int dialect);

	/**
	 * @return returns the client type
	 */
	public abstract short getClientType();

	/**
	 * @return Returns the componentDataMemberDefaultAccess.
	 */
	public abstract int getComponentDataMemberDefaultAccess();

	/**
	 * @return Returns the timeServer.
	 */
	public abstract String getTimeServer();

	/**
	 * @return Returns the componentDump.
	 */
	public abstract String getComponentDumpTemplate();

	/**
	 * @return Returns the debug Template.
	 * @deprecated use instead <code>getDebugEntry(ip, defaultValue)</code>
	 */
	@Deprecated
	public abstract String getDebugTemplate();

	/**
	 * @param statusCode status code
	 * @return Returns the error Template for given status code.
	 */
	public abstract String getErrorTemplate(int statusCode);

	/**
	 * @return Returns the sessionType.
	 */
	public abstract short getSessionType();

	/**
	 * @return returns the charset for the response and request
	 */
	public abstract Charset getWebCharset();

	/**
	 * @return returns the charset used to read cfml files
	 */
	public abstract Charset getTemplateCharset();

	/**
	 * @return returns the charset used to read and write resources
	 */
	public abstract Charset getResourceCharset();

	/**
	 * @return returns the default charset for mail
	 */
	public Charset getMailDefaultCharset();

	/**
	 * @return returns update type (auto or manual)
	 */
	public abstract String getUpdateType();

	/**
	 * @return returns URL for update
	 */
	public abstract URL getUpdateLocation();

	/**
	 * return directory, where lucee deploy translated cfml classes (java and class files)
	 * 
	 * @return deploy directory
	 */
	public abstract Resource getClassDirectory();

	/**
	 * @return Returns the rootDir.
	 */
	public abstract Resource getRootDirectory();

	/**
	 * @return Returns the accessor.
	 */
	public abstract SecurityManager getSecurityManager();

	/**
	 * @return Returns the cfxTagPool.
	 * @throws PageException Page Exception
	 */
	public abstract CFXTagPool getCFXTagPool() throws PageException;

	/**
	 * @param password password
	 * @return ConfigServer
	 * @throws PageException Page Exception
	 * @deprecated use instead ConfigWeb.getConfigServer(Password password)
	 */
	@Deprecated
	public ConfigServer getConfigServer(String password) throws PageException;

	public ConfigServer getConfigServer(String key, long timeNonce) throws PageException;

	/**
	 * reload the time offset to a time server
	 */
	public void reloadTimeServerOffset();

	/**
	 * reset config
	 */
	public void reset();

	/**
	 * @return return the search Storage
	 */
	public ClassDefinition<SearchEngine> getSearchEngineClassDefinition();

	public String getSearchEngineDirectory();

	/**
	 * @return return the Scheduler
	 */
	public Scheduler getScheduler();

	/**
	 * @return return all defined Mail Servers
	 */
	public Server[] getMailServers();

	/**
	 * return the compile type of this context
	 * @return compile type
	 */
	public short getCompileType();

	/**
	 * return the all datasources
	 * @return all datasources
	 */
	public DataSource[] getDataSources();

	/**
	 * @param path get a resource that match this path
	 * @return resource matching path
	 */
	public Resource getResource(String path);

	/**
	 * return current application listener
	 * 
	 * @return application listener
	 */
	public ApplicationListener getApplicationListener();

	/**
	 * @return the scriptProtect
	 */
	public int getScriptProtect();

	/**
	 * return default proxy setting password
	 * 
	 * @return the password for proxy
	 */
	public ProxyData getProxyData();

	/**
	 * return if proxy is enabled or not
	 * 
	 * @param host Host
	 * @return is proxy enabled
	 */
	public boolean isProxyEnableFor(String host);

	/**
	 * @return the triggerComponentDataMember
	 */
	public boolean getTriggerComponentDataMember();

	public RestSettings getRestSetting();

	public abstract Resource getClientScopeDir();

	public abstract long getClientScopeDirSize();

	public abstract ClassLoader getRPCClassLoader(boolean reload) throws IOException;

	public Resource getCacheDir();

	public long getCacheDirSize();

	public Map<String, CacheConnection> getCacheConnections();

	/**
	 * get default cache connection for a specific type
	 * 
	 * @param type default type, one of the following (CACHE_DEFAULT_NONE, CACHE_DEFAULT_OBJECT,
	 *            CACHE_DEFAULT_TEMPLATE, CACHE_DEFAULT_QUERY, CACHE_DEFAULT_RESOURCE)
	 * @return matching Cache Connection
	 */
	public CacheConnection getCacheDefaultConnection(int type);

	/**
	 * get name of a default cache connection for a specific type
	 * 
	 * @param type default type, one of the following (CACHE_DEFAULT_NONE, CACHE_DEFAULT_OBJECT,
	 *            CACHE_DEFAULT_TEMPLATE, CACHE_DEFAULT_QUERY, CACHE_DEFAULT_RESOURCE)
	 * @return name of matching Cache Connection
	 */
	public String getCacheDefaultConnectionName(int type);

	/**
	 * returns the default DumpWriter
	 * 
	 * @param defaultType default type
	 * @return default DumpWriter
	 */
	public abstract DumpWriter getDefaultDumpWriter(int defaultType);

	/**
	 * returns the DumpWriter matching key
	 * 
	 * @param key key for DumpWriter
	 * @param defaultType default type
	 * @return matching DumpWriter
	 * @throws PageException if there is no DumpWriter for this key
	 */
	public abstract DumpWriter getDumpWriter(String key, int defaultType) throws PageException;

	/**
	 * returns the DumpWriter matching key
	 * 
	 * @param key key for DumpWriter
	 * @return matching DumpWriter
	 * @deprecated use instead <code>getDumpWriter(String key,int defaultType)</code>
	 * @throws PageException if there is no DumpWriter for this key
	 */
	@Deprecated
	public abstract DumpWriter getDumpWriter(String key) throws PageException;

	/**
	 * define if components has a "shadow" in the component variables scope or not.
	 * 
	 * @return if the component has a shadow scope.
	 */
	public abstract boolean useComponentShadow();

	/*
	 * * return a database connection hold inside by a datasource definition
	 * 
	 * @param datasource definiti0on of the datasource
	 * 
	 * @param user username to connect
	 * 
	 * @param pass password to connect
	 * 
	 * @return datasource connnection
	 * 
	 * @throws PageException
	 */
	// public DatasourceConnection getConnection(String datasource, String user, String pass) throws
	// PageException;

	/*
	 * *
	 * 
	 * @return returns the ConnectionPool
	 */

	public Mapping[] getComponentMappings();

	public abstract boolean doCustomTagDeepSearch();

	/**
	 * @return returns the error print writer stream
	 */
	public abstract PrintWriter getErrWriter();

	/**
	 * @return returns the out print writer stream
	 */
	public abstract PrintWriter getOutWriter();

	/**
	 * define if lucee search in local directory for custom tags or not
	 * 
	 * @return search in local dir?
	 */
	public abstract boolean doLocalCustomTag();

	public String[] getCustomTagExtensions();

	/**
	 * @return if error status code will be returned or not
	 */
	public boolean getErrorStatusCode();

	public abstract int getLocalMode();

	/**
	 * @return return the class defined for the cluster scope
	 * 
	 */
	@Deprecated
	public Class<?> getClusterClass();

	/**
	 * @return classloader of ths context
	 */
	public ClassLoader getClassLoader(); // FUTURE deprecated, use instead getClassLoaderCore
	// public ClassLoader getClassLoaderCore();
	// public ClassLoader getClassLoaderLoader();

	public Resource getExtensionDirectory();

	public ExtensionProvider[] getExtensionProviders();

	public Extension[] getExtensions();

	public PageSource getBaseComponentPageSource(int dialect);

	public boolean allowRealPath();

	public Struct getConstants();

	public DataSource getDataSource(String datasource) throws PageException;

	public DataSource getDataSource(String datasource, DataSource defaultValue);

	public Map<String, DataSource> getDataSourcesAsMap();

	public String getDefaultEncoding();

	public ResourceProvider getDefaultResourceProvider();

	public boolean isExtensionEnabled();

	public Resource getFldFile();

	/**
	 * @return the tldFile
	 */
	public Resource getTldFile();

	/**
	 * get PageSource of the first Mapping that match the given criteria
	 * 
	 * @param mappings per application mappings
	 * @param realPath path to get PageSource for
	 * @param onlyTopLevel checks only toplevel mappings
	 * @return Page Source
	 * @deprecated use instead getPageSources or getPageSourceExisting
	 */
	@Deprecated
	public PageSource getPageSource(Mapping[] mappings, String realPath, boolean onlyTopLevel);

	/**
	 * return existing PageSource that match the given criteria, if there is no PageSource null is
	 * returned.
	 * 
	 * @param pc current PageContext
	 * @param mappings per application mappings
	 * @param realPath path to get PageSource for
	 * @param onlyTopLevel checks only toplevel mappings
	 * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
	 * @param useDefaultMapping also invoke the always existing default mapping "/"
	 * @param onlyPhysicalExisting only Physical existing
	 * @return Page Source
	 */
	public PageSource getPageSourceExisting(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean onlyPhysicalExisting);

	/**
	 * get all PageSources that match the given criteria
	 * 
	 * @param pc current PageContext
	 * @param mappings per application mappings
	 * @param realPath path to get PageSource for
	 * @param onlyTopLevel checks only toplevel mappings
	 * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
	 * @param useDefaultMapping also invoke the always existing default mapping "/"
	 * @return All Page Sources
	 * @deprecated use instead
	 */
	@Deprecated
	public PageSource[] getPageSources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping);

	/**
	 * get all PageSources that match the given criteria
	 * 
	 * @param pc current PageContext
	 * @param mappings per application mappings
	 * @param realPath path to get PageSource for
	 * @param onlyTopLevel checks only toplevel mappings
	 * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
	 * @param useDefaultMapping also invoke the always existing default mapping "/"
	 * @param useComponentMappings also invoke component mappings
	 * @return All Page Sources
	 */
	public PageSource[] getPageSources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean useComponentMappings);

	/**
	 * get Resource of the first Mapping that match the given criteria
	 * 
	 * @param mappings per application mappings
	 * @param relPath path to get PageSource for
	 * @param alsoDefaultMapping also default mapping
	 * @return Resource
	 * @deprecated use instead getPhysicalResources or getPhysicalResourceExisting
	 */
	@Deprecated
	public Resource getPhysical(Mapping[] mappings, String relPath, boolean alsoDefaultMapping);

	/**
	 * get all Resources that match the given criteria
	 * 
	 * @param pc current PageContext
	 * @param mappings per application mappings
	 * @param realPath path to get PageSource for
	 * @param onlyTopLevel checks only toplevel mappings
	 * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
	 * @param useDefaultMapping also invoke the always existing default mapping "/"
	 * @return Resource
	 */
	public Resource[] getPhysicalResources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping);

	/**
	 * return existing Resource that match the given criteria, if there is no Resource null is returned.
	 * 
	 * @param pc current PageContext
	 * @param mappings per application mappings
	 * @param realPath path to get Resource for
	 * @param onlyTopLevel checks only toplevel mappings
	 * @param useSpecialMappings invoke special mappings like "mapping-tag" or "mapping-customtag"
	 * @param useDefaultMapping also invoke the always existing default mapping "/"
	 * @return Resource
	 */
	public Resource getPhysicalResourceExisting(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping);

	public Resource getRemoteClientDirectory();

	public RemoteClient[] getRemoteClients();

	public SpoolerEngine getSpoolerEngine();

	public ResourceProvider[] getResourceProviders();

	public double getVersion();

	public Resource getVideoDirectory();

	// public String getVideoProviderLocation();

	public boolean isShowVersion();

	public boolean isSuppressWhitespace();

	// public boolean isVideoAgreementAccepted();

	public Struct getRemoteClientUsage();

	public Class<AdminSync> getAdminSyncClass();

	public Class<VideoExecuter> getVideoExecuterClass();

	public ThreadQueue getThreadQueue();

	public boolean getSessionCluster();

	public boolean getClientCluster();

	public Resource getSecurityDirectory();

	public boolean isMonitoringEnabled();

	public RequestMonitor[] getRequestMonitors();

	public RequestMonitor getRequestMonitor(String name) throws PageException;

	public IntervallMonitor[] getIntervallMonitors();

	public IntervallMonitor getIntervallMonitor(String name) throws PageException;

	public ActionMonitor getActionMonitor(String name) throws PageException;

	/**
	 * if free permspace gen is lower than 10000000 bytes, lucee shrinks all classloaders
	 * 
	 * @param check check
	 */
	public void checkPermGenSpace(boolean check);

	public boolean allowRequestTimeout();

	public Log getLog(String name);

	public Boolean getHandleUnQuotedAttrValueAsString();

	public Object getCachedWithin(int type);

	public Identification getIdentification();

	public int getLoginDelay();

	public boolean getLoginCaptcha();

	public boolean getRememberMe();

	public boolean getFullNullSupport();

	public ORMEngine getORMEngine(PageContext pc) throws PageException;

	public Resource getLocalExtensionProviderDirectory();

	public Resource getDeployDirectory();

}