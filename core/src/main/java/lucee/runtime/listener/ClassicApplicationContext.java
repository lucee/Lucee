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
package lucee.runtime.listener;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.ftp.FTPConnectionData;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.db.DataSource;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.s3.Properties;
import lucee.runtime.net.s3.PropertiesImpl;
import lucee.runtime.op.Duplicator;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.rest.RestSettings;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.CustomType;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.ArrayUtil;

/**
 * This class resolves the Application settings that are defined in cfapplication tag attributes,
 * e.g. sessionManagement, localMode, etc.
 */
public class ClassicApplicationContext extends ApplicationContextSupport {

	private static final long serialVersionUID = 940663152793150953L;

	private String name;
	private boolean setClientCookies;
	private boolean setDomainCookies;
	private boolean setSessionManagement;
	private boolean setClientManagement;
	private TimeSpan sessionTimeout = null;
	private TimeSpan requestTimeout = null;
	private TimeSpan clientTimeout;
	private TimeSpan applicationTimeout = null;
	private int loginStorage = -1;
	private String clientstorage;
	private String sessionstorage;
	private int scriptProtect;
	private boolean typeChecking;
	private Mapping[] mappings;
	private Mapping[] ctmappings;
	private Mapping[] cmappings;
	private List<Resource> funcDirs;
	private boolean bufferOutput;
	private boolean secureJson;
	private String secureJsonPrefix = "//";
	private boolean isDefault;
	private Object defaultDataSource;
	private boolean ormEnabled;
	private Object ormdatasource;
	private ORMConfiguration ormConfig;
	private Properties s3;
	private FTPConnectionData ftp;

	private int localMode;
	private Locale locale;
	private TimeZone timeZone;
	private CharSet webCharset;
	private CharSet resourceCharset;
	private short sessionType;
	private boolean sessionCluster;
	private boolean clientCluster;
	private Resource source;
	private boolean triggerComponentDataMember;
	private Map<Integer, String> defaultCaches = new ConcurrentHashMap<Integer, String>();
	private Map<Collection.Key, CacheConnection> cacheConnections = new ConcurrentHashMap<Collection.Key, CacheConnection>();
	private Server[] mailServers;
	private Map<Integer, Boolean> sameFieldAsArrays = new ConcurrentHashMap<Integer, Boolean>();
	private RestSettings restSettings;
	private Resource[] restCFCLocations;
	private Resource antiSamyPolicy;
	private JavaSettings javaSettings;
	private DataSource[] dataSources;
	private UDF onMissingTemplate;

	private short scopeCascading;
	private boolean allowCompression;
	private boolean suppressRemoteComponentContent;

	private short wstype;
	private boolean cgiScopeReadonly;

	private SessionCookieData sessionCookie;

	private AuthCookieData authCookie;

	private Map<Key, Pair<Log, Struct>> logs;

	private Object mailListener;
	private TagListener queryListener;

	private boolean wsMaintainSession;

	private boolean fullNullSupport;
	private SerializationSettings serializationSettings = SerializationSettings.DEFAULT;

	private boolean queryPSQ;
	private int queryVarUsage;

	private ProxyData proxyData;

	private TimeSpan queryCachedAfter;
	private String blockedExtForFileUpload;
	private Struct xmlFeatures;

	private Map<Key, Object> customAttrs;

	/**
	 * constructor of the class
	 * 
	 * @param config
	 */
	public ClassicApplicationContext(ConfigWeb config, String name, boolean isDefault, Resource source) {
		super(config);
		this.name = name;
		setClientCookies = config.isClientCookies();
		setDomainCookies = config.isDomainCookies();
		setSessionManagement = config.isSessionManagement();
		setClientManagement = config.isClientManagement();
		sessionTimeout = config.getSessionTimeout();
		requestTimeout = config.getRequestTimeout();
		clientTimeout = config.getClientTimeout();
		applicationTimeout = config.getApplicationTimeout();
		loginStorage = Scope.SCOPE_COOKIE;
		scriptProtect = config.getScriptProtect();
		typeChecking = ((ConfigImpl) config).getTypeChecking();
		allowCompression = ((ConfigImpl) config).allowCompression();
		this.isDefault = isDefault;
		this.defaultDataSource = config.getDefaultDataSource();
		this.localMode = config.getLocalMode();
		this.queryPSQ = config.getPSQL();
		this.queryVarUsage = ((ConfigImpl) config).getQueryVarUsage();
		this.queryCachedAfter = ((ConfigImpl) config).getCachedAfterTimeRange();

		this.locale = config.getLocale();
		this.timeZone = config.getTimeZone();
		this.fullNullSupport = config.getFullNullSupport();
		this.scopeCascading = config.getScopeCascadingType();

		this.webCharset = ((ConfigImpl) config).getWebCharSet();
		this.resourceCharset = ((ConfigImpl) config).getResourceCharSet();
		this.bufferOutput = ((ConfigImpl) config).getBufferOutput();
		suppressRemoteComponentContent = ((ConfigImpl) config).isSuppressContent();
		this.sessionType = config.getSessionType();
		this.sessionCluster = config.getSessionCluster();
		this.clientCluster = config.getClientCluster();
		this.clientstorage = ((ConfigImpl) config).getClientStorage();
		this.sessionstorage = ((ConfigImpl) config).getSessionStorage();

		this.source = source;
		this.triggerComponentDataMember = config.getTriggerComponentDataMember();
		this.restSettings = config.getRestSetting();
		this.javaSettings = new JavaSettingsImpl();
		this.wstype = WS_TYPE_AXIS1;
		cgiScopeReadonly = ((ConfigImpl) config).getCGIScopeReadonly();
		this.antiSamyPolicy = ((ConfigImpl) config).getAntiSamyPolicy();

	}

	/**
	 * Constructor of the class, only used by duplicate method
	 */
	private ClassicApplicationContext(ConfigWeb config) {
		super(config);
	}

	public ApplicationContext duplicate() {
		ClassicApplicationContext dbl = new ClassicApplicationContext(config);
		dbl._duplicate(this);

		dbl.name = name;
		dbl.setClientCookies = setClientCookies;
		dbl.setDomainCookies = setDomainCookies;
		dbl.setSessionManagement = setSessionManagement;
		dbl.setClientManagement = setClientManagement;
		dbl.sessionTimeout = sessionTimeout;
		dbl.requestTimeout = requestTimeout;
		dbl.clientTimeout = clientTimeout;
		dbl.applicationTimeout = applicationTimeout;
		dbl.loginStorage = loginStorage;
		dbl.clientstorage = clientstorage;
		dbl.sessionstorage = sessionstorage;
		dbl.scriptProtect = scriptProtect;
		dbl.typeChecking = typeChecking;
		dbl.mappings = mappings;
		dbl.dataSources = dataSources;
		dbl.ctmappings = ctmappings;
		dbl.cmappings = cmappings;
		dbl.funcDirs = funcDirs;
		dbl.bufferOutput = bufferOutput;
		dbl.allowCompression = allowCompression;
		dbl.suppressRemoteComponentContent = suppressRemoteComponentContent;
		dbl.wstype = wstype;
		dbl.secureJson = secureJson;
		dbl.secureJsonPrefix = secureJsonPrefix;
		dbl.isDefault = isDefault;
		dbl.defaultDataSource = defaultDataSource;
		dbl.applicationtoken = applicationtoken;
		dbl.cookiedomain = cookiedomain;
		dbl.idletimeout = idletimeout;
		dbl.localMode = localMode;
		dbl.queryPSQ = queryPSQ;
		dbl.queryVarUsage = queryVarUsage;
		dbl.queryCachedAfter = queryCachedAfter;
		dbl.locale = locale;
		dbl.timeZone = timeZone;
		dbl.fullNullSupport = fullNullSupport;
		dbl.scopeCascading = scopeCascading;
		dbl.webCharset = webCharset;
		dbl.resourceCharset = resourceCharset;
		dbl.sessionType = sessionType;
		dbl.triggerComponentDataMember = triggerComponentDataMember;
		dbl.restSettings = restSettings;
		dbl.defaultCaches = Duplicator.duplicateMap(defaultCaches, new ConcurrentHashMap<Integer, String>(), false);
		dbl.cacheConnections = Duplicator.duplicateMap(cacheConnections, new ConcurrentHashMap<Integer, String>(), false);
		dbl.mailServers = mailServers;
		dbl.cachedWithinFile = Duplicator.duplicate(cachedWithinFile, false);
		dbl.cachedWithinFunction = Duplicator.duplicate(cachedWithinFunction, false);
		dbl.cachedWithinHTTP = Duplicator.duplicate(cachedWithinHTTP, false);
		dbl.cachedWithinInclude = Duplicator.duplicate(cachedWithinInclude, false);
		dbl.cachedWithinQuery = Duplicator.duplicate(cachedWithinQuery, false);
		dbl.cachedWithinResource = Duplicator.duplicate(cachedWithinResource, false);
		dbl.cachedWithinWS = Duplicator.duplicate(cachedWithinWS, false);

		dbl.sameFieldAsArrays = Duplicator.duplicateMap(sameFieldAsArrays, new ConcurrentHashMap<Integer, Boolean>(), false);

		dbl.ormEnabled = ormEnabled;
		dbl.ormConfig = ormConfig;
		dbl.ormdatasource = ormdatasource;
		dbl.sessionCluster = sessionCluster;
		dbl.clientCluster = clientCluster;
		dbl.source = source;
		dbl.cgiScopeReadonly = cgiScopeReadonly;
		dbl.antiSamyPolicy = antiSamyPolicy;
		dbl.sessionCookie = sessionCookie;
		dbl.authCookie = authCookie;
		return dbl;
	}

	@Override
	public TimeSpan getApplicationTimeout() {
		return applicationTimeout;
	}

	/**
	 * @param applicationTimeout The applicationTimeout to set.
	 */
	@Override
	public void setApplicationTimeout(TimeSpan applicationTimeout) {
		this.applicationTimeout = applicationTimeout;
	}

	@Override
	public int getLoginStorage() {
		return loginStorage;
	}

	/**
	 * @param loginStorage The loginStorage to set.
	 */
	@Override
	public void setLoginStorage(int loginStorage) {
		this.loginStorage = loginStorage;
	}

	public void setLoginStorage(String strLoginStorage) throws ApplicationException {
		setLoginStorage(AppListenerUtil.translateLoginStorage(strLoginStorage));
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public TimeSpan getSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 * @param sessionTimeout The sessionTimeout to set.
	 */
	@Override
	public void setSessionTimeout(TimeSpan sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	@Override
	public TimeSpan getClientTimeout() {
		return clientTimeout;
	}

	/**
	 * @param sessionTimeout The sessionTimeout to set.
	 */
	@Override
	public void setClientTimeout(TimeSpan clientTimeout) {
		this.clientTimeout = clientTimeout;
	}

	@Override
	public boolean isSetClientCookies() {
		return setClientCookies;
	}

	/**
	 * @param setClientCookies The setClientCookies to set.
	 */
	@Override
	public void setSetClientCookies(boolean setClientCookies) {
		this.setClientCookies = setClientCookies;
	}

	@Override
	public boolean isSetClientManagement() {
		return setClientManagement;
	}

	/**
	 * @param setClientManagement The setClientManagement to set.
	 */
	@Override
	public void setSetClientManagement(boolean setClientManagement) {
		this.setClientManagement = setClientManagement;
	}

	@Override
	public boolean isSetDomainCookies() {
		return setDomainCookies;
	}

	/**
	 * @param setDomainCookies The setDomainCookies to set.
	 */
	@Override
	public void setSetDomainCookies(boolean setDomainCookies) {
		this.setDomainCookies = setDomainCookies;
	}

	@Override
	public boolean isSetSessionManagement() {
		return setSessionManagement;
	}

	/**
	 * @param setSessionManagement The setSessionManagement to set.
	 */
	@Override
	public void setSetSessionManagement(boolean setSessionManagement) {
		this.setSessionManagement = setSessionManagement;
	}

	@Override
	public String getClientstorage() {
		return clientstorage;
	}

	@Override
	public String getSessionstorage() {
		return sessionstorage;
	}

	/**
	 * @param clientstorage The clientstorage to set.
	 */
	@Override
	public void setClientstorage(String clientstorage) {
		if (StringUtil.isEmpty(clientstorage, true)) return;
		this.clientstorage = clientstorage;
	}

	@Override
	public void setSessionstorage(String sessionstorage) {
		if (StringUtil.isEmpty(sessionstorage, true)) return;
		this.sessionstorage = sessionstorage;
	}

	@Override
	public boolean hasName() {
		return name != null;
	}

	/**
	 * @param scriptProtect The scriptProtect to set.
	 */
	@Override
	public void setScriptProtect(int scriptProtect) {
		this.scriptProtect = scriptProtect;
	}

	@Override
	public int getScriptProtect() {
		// if(isDefault)print.err("get:"+scriptProtect);
		return scriptProtect;
	}

	/**
	 * @param scriptProtect The scriptProtect to set.
	 */
	@Override
	public void setTypeChecking(boolean typeChecking) {
		this.typeChecking = typeChecking;
	}

	@Override
	public boolean getTypeChecking() {
		return typeChecking;
	}

	@Override
	public void setMappings(Mapping[] mappings) {
		if (mappings.length > 0) this.mappings = mappings;
	}

	/**
	 * @return the mappings
	 */
	@Override
	public Mapping[] getMappings() {
		return mappings;
	}

	@Override
	public void setCustomTagMappings(Mapping[] ctmappings) {
		this.ctmappings = ctmappings;
	}

	@Override
	public Mapping[] getCustomTagMappings() {
		return ctmappings;
	}

	@Override
	public void setComponentMappings(Mapping[] cmappings) {
		this.cmappings = cmappings;
	}

	@Override
	public Mapping[] getComponentMappings() {
		return cmappings;
	}

	@Override
	public void setSecureJson(boolean secureJson) {
		this.secureJson = secureJson;
	}

	/**
	 * @return the secureJson
	 */
	@Override
	public boolean getSecureJson() {
		return secureJson;
	}

	@Override
	public boolean getBufferOutput() {
		return bufferOutput;
	}

	@Override
	public void setBufferOutput(boolean bufferOutput) {
		this.bufferOutput = bufferOutput;
	}

	@Override
	public void setSecureJsonPrefix(String secureJsonPrefix) {
		this.secureJsonPrefix = secureJsonPrefix;
	}

	/**
	 * @return the secureJsonPrefix
	 */
	@Override
	public String getSecureJsonPrefix() {
		return secureJsonPrefix;
	}

	@Override
	public String getDefaultDataSource() {
		throw new PageRuntimeException(new DeprecatedException("this method is no longer supported!"));
	}

	@Override
	public Object getDefDataSource() {
		return defaultDataSource;
	}

	@Override
	public void setDefaultDataSource(String defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
	}

	@Override
	public void setDefDataSource(Object defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
	}

	@Override
	public boolean isORMEnabled() {
		return ormEnabled;
	}

	@Override
	public String getORMDatasource() {
		throw new PageRuntimeException(new DeprecatedException("this method is no longer supported!"));
	}

	@Override
	public Object getORMDataSource() {
		return ormdatasource;
	}

	@Override
	public ORMConfiguration getORMConfiguration() {
		return ormConfig;
	}

	@Override
	public void setORMConfiguration(ORMConfiguration config) {
		this.ormConfig = config;
	}

	@Override
	public void setORMEnabled(boolean ormEnabled) {
		this.ormEnabled = ormEnabled;
	}

	@Override
	public Properties getS3() {
		if (s3 == null) s3 = new PropertiesImpl();
		return s3;
	}

	@Override
	public FTPConnectionData getFTP() {
		if (ftp == null) ftp = new FTPConnectionData();
		return ftp;
	}

	@Override
	public int getLocalMode() {
		return localMode;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public TimeZone getTimeZone() {
		return timeZone;
	}

	@Override
	public boolean getFullNullSupport() {
		return fullNullSupport;
	}

	@Override
	public Charset getWebCharset() {
		return CharsetUtil.toCharset(webCharset);
	}

	public CharSet getWebCharSet() {
		return webCharset;
	}

	@Override
	public Charset getResourceCharset() {
		return CharsetUtil.toCharset(resourceCharset);
	}

	public CharSet getResourceCharSet() {
		return resourceCharset;
	}

	/**
	 * @param localMode the localMode to set
	 */
	@Override
	public void setLocalMode(int localMode) {
		this.localMode = localMode;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	@Override
	public void setFullNullSupport(boolean fullNullSupport) {
		this.fullNullSupport = fullNullSupport;
	}

	@Override
	public void setWebCharset(Charset webCharset) {
		this.webCharset = CharsetUtil.toCharSet(webCharset);
	}

	@Override
	public void setResourceCharset(Charset resourceCharset) {
		this.resourceCharset = CharsetUtil.toCharSet(resourceCharset);
	}

	/**
	 * @return the sessionType
	 */
	@Override
	public short getSessionType() {
		return sessionType;
	}

	/**
	 * @return the sessionType
	 */
	@Override
	public void setSessionType(short sessionType) {
		this.sessionType = sessionType;
	}

	/**
	 * @return the sessionCluster
	 */
	@Override
	public boolean getSessionCluster() {
		return sessionCluster;
	}

	/**
	 * @param sessionCluster the sessionCluster to set
	 */
	@Override
	public void setSessionCluster(boolean sessionCluster) {
		this.sessionCluster = sessionCluster;
	}

	/**
	 * @return the clientCluster
	 */
	@Override
	public boolean getClientCluster() {
		return clientCluster;
	}

	/**
	 * @param clientCluster the clientCluster to set
	 */
	@Override
	public void setClientCluster(boolean clientCluster) {
		this.clientCluster = clientCluster;
	}

	@Override
	public void setS3(Properties s3) {
		this.s3 = s3;
	}

	@Override
	public void setFTP(FTPConnectionData ftp) {
		this.ftp = ftp;
	}

	@Override
	public void setORMDatasource(String ormdatasource) {
		this.ormdatasource = ormdatasource;
	}

	@Override
	public void setORMDataSource(Object ormdatasource) {
		this.ormdatasource = ormdatasource;
	}

	@Override
	public void reinitORM(PageContext pc) throws PageException {
		// do nothing
	}

	@Override
	public Resource getSource() {
		return source;
	}

	@Override
	public boolean getTriggerComponentDataMember() {
		return triggerComponentDataMember;
	}

	@Override
	public void setTriggerComponentDataMember(boolean triggerComponentDataMember) {
		this.triggerComponentDataMember = triggerComponentDataMember;
	}

	@Override
	public void setDefaultCacheName(int type, String name) {
		if (StringUtil.isEmpty(name, true)) return;
		defaultCaches.put(type, name.trim());
	}

	@Override
	public String getDefaultCacheName(int type) {
		return defaultCaches.get(type);
	}

	@Override
	public void setCacheConnection(String cacheName, CacheConnection value) {
		if (StringUtil.isEmpty(cacheName, true)) return;
		cacheConnections.put(KeyImpl.init(cacheName), value);
	}

	@Override
	public CacheConnection getCacheConnection(String cacheName, CacheConnection defaultValue) {
		return cacheConnections.get(KeyImpl.init(cacheName));
	}

	@Override
	public Key[] getCacheConnectionNames() {
		return cacheConnections == null ? new Key[0] : cacheConnections.keySet().toArray(new Key[cacheConnections.size()]);
	}

	@Override
	public void setMailServers(Server[] servers) {
		this.mailServers = servers;
	}

	@Override
	public Server[] getMailServers() {
		return this.mailServers;
	}

	public void setSameFieldAsArray(PageContext pc, int scope, boolean sameFieldAsArray) {
		sameFieldAsArrays.put(scope, sameFieldAsArray);
		if (Scope.SCOPE_URL == scope) pc.urlScope().reinitialize(this);
		else pc.formScope().reinitialize(this);
	}

	@Override
	public boolean getSameFieldAsArray(int scope) {
		Boolean b = sameFieldAsArrays.get(scope);
		if (b == null) return false;
		return b.booleanValue();
	}

	@Override
	public RestSettings getRestSettings() {
		return restSettings;
	}

	public void setRestSettings(RestSettings restSettings) {
		this.restSettings = restSettings;
	}

	public void setRestCFCLocations(Resource[] restCFCLocations) {
		this.restCFCLocations = restCFCLocations;
	}

	@Override
	public Resource[] getRestCFCLocations() {
		return restCFCLocations;
	}

	@Override
	public JavaSettings getJavaSettings() {
		return javaSettings;
	}

	@Override
	public void setJavaSettings(JavaSettings javaSettings) {
		this.javaSettings = javaSettings;
	}

	@Override
	public DataSource[] getDataSources() {
		return dataSources;
	}

	@Override
	public void setDataSources(DataSource[] dataSources) {
		if (!ArrayUtil.isEmpty(dataSources)) this.dataSources = dataSources;
	}

	public void setOnMissingTemplate(UDF onMissingTemplate) {
		this.onMissingTemplate = onMissingTemplate;
	}

	public UDF getOnMissingTemplate() {
		return onMissingTemplate;
	}

	@Override
	public short getScopeCascading() {
		return scopeCascading;
	}

	@Override
	public void setScopeCascading(short scopeCascading) {
		this.scopeCascading = scopeCascading;
	}

	@Override
	public boolean getAllowCompression() {
		return allowCompression;
	}

	@Override
	public void setAllowCompression(boolean allowCompression) {
		this.allowCompression = allowCompression;
	}

	@Override
	public TimeSpan getRequestTimeout() {
		return requestTimeout;
	}

	@Override
	public void setRequestTimeout(TimeSpan requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@Override
	public CustomType getCustomType(String strType) {
		// not supported
		return null;
	}

	@Override
	public boolean getSuppressContent() {
		return suppressRemoteComponentContent;
	}

	@Override
	public void setSuppressContent(boolean suppressContent) {
		this.suppressRemoteComponentContent = suppressContent;
	}

	@Override
	public short getWSType() {
		return wstype;
	}

	@Override
	public void setWSType(short wstype) {
		this.wstype = wstype;
	}

	@Override
	public boolean getCGIScopeReadonly() {
		return cgiScopeReadonly;
	}

	@Override
	public void setCGIScopeReadonly(boolean cgiScopeReadonly) {
		this.cgiScopeReadonly = cgiScopeReadonly;
	}

	@Override
	public Resource getAntiSamyPolicyResource() {
		return antiSamyPolicy;
	}

	@Override
	public void setAntiSamyPolicyResource(Resource antiSamyPolicy) {
		this.antiSamyPolicy = antiSamyPolicy;
	}

	@Override
	public SessionCookieData getSessionCookie() {
		return sessionCookie;
	}

	@Override
	public void setSessionCookie(SessionCookieData data) {
		sessionCookie = data;
	}

	@Override
	public AuthCookieData getAuthCookie() {
		return authCookie;
	}

	@Override
	public void setAuthCookie(AuthCookieData data) {
		authCookie = data;
	}

	@Override
	public java.util.Collection<Key> getLogNames() {
		if (logs == null) return new HashSet<Collection.Key>();
		return logs.keySet();
	}

	@Override
	public void setLoggers(Map<Key, Pair<Log, Struct>> logs) {
		this.logs = logs;
	}

	@Override
	public Log getLog(String name) {
		if (logs == null) return null;
		Pair<Log, Struct> pair = logs.get(KeyImpl.init(StringUtil.emptyIfNull(name)));
		if (pair == null) return null;
		return pair.getName();
	}

	@Override
	public Struct getLogMetaData(String name) {
		if (logs == null) return null;
		Pair<Log, Struct> pair = logs.get(KeyImpl.init(StringUtil.emptyIfNull(name)));
		if (pair == null) return null;
		return (Struct) pair.getValue().duplicate(false);
	}

	@Override
	public Object getMailListener() {
		return mailListener;
	}

	@Override
	public void setMailListener(Object listener) {
		this.mailListener = listener;
	}

	@Override
	public TagListener getQueryListener() {
		return queryListener;
	}

	@Override
	public void setQueryListener(TagListener listener) {
		this.queryListener = listener;
	}

	@Override
	public SerializationSettings getSerializationSettings() {
		return serializationSettings;
	}

	@Override
	public void setSerializationSettings(SerializationSettings settings) {
		this.serializationSettings = settings;
	}

	@Override
	public boolean getWSMaintainSession() {
		return wsMaintainSession;
	}

	@Override
	public void setWSMaintainSession(boolean wsMaintainSession) {
		this.wsMaintainSession = wsMaintainSession;
	}

	@Override
	public List<Resource> getFunctionDirectories() {
		return funcDirs;
	}

	@Override
	public void setFunctionDirectories(List<Resource> resources) {
		this.funcDirs = resources;
	}

	@Override
	public boolean getQueryPSQ() {
		return queryPSQ;
	}

	@Override
	public void setQueryPSQ(boolean psq) {
		this.queryPSQ = psq;
	}

	@Override
	public int getQueryVarUsage() {
		return queryVarUsage;
	}

	@Override
	public void setQueryVarUsage(int varUsage) {
		this.queryVarUsage = varUsage;
	}

	@Override
	public TimeSpan getQueryCachedAfter() {
		return queryCachedAfter;
	}

	@Override
	public void setQueryCachedAfter(TimeSpan ts) {
		this.queryCachedAfter = ts;
	}

	@Override
	public ProxyData getProxyData() {
		return proxyData;
	}

	@Override
	public void setProxyData(ProxyData data) {
		this.proxyData = data;
	}

	public void setBlockedextforfileupload(String blockedExtForFileUpload) {
		this.blockedExtForFileUpload = blockedExtForFileUpload;
	}

	@Override
	public String getBlockedExtForFileUpload() {
		return blockedExtForFileUpload;
	}

	@Override
	public Struct getXmlFeatures() {
		return xmlFeatures;
	}

	@Override
	public void setXmlFeatures(Struct xmlFeatures) {
		this.xmlFeatures = xmlFeatures;
	}

	public void setCustomAttributes(Map<Key, Object> customAttrs) {
		this.customAttrs = customAttrs;
	}

	public Map<Key, Object> getCustomAttributes() {
		return customAttrs;
	}
}
