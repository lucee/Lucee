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
package lucee.runtime.listener;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.ftp.FTPConnectionData;
import lucee.commons.io.res.type.ftp.IFTPConnectionData;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.RuntimeProfile;
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
import lucee.runtime.regex.Regex;
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
public final class ClassicApplicationContext extends ApplicationContextSupport {

	private static final long serialVersionUID = 940663152793150953L;

	private String name;
	private Boolean setClientCookies;
	private Boolean setDomainCookies;
	private Boolean setSessionManagement;
	private Boolean setClientManagement;
	private TimeSpan sessionTimeout;
	private TimeSpan requestTimeout;
	private TimeSpan clientTimeout;
	private TimeSpan applicationTimeout;
	private int loginStorage = -1;
	private String clientstorage;
	private String sessionstorage;
	private Integer scriptProtect;
	private Boolean typeChecking;
	private Mapping[] mappings;
	private Mapping[] ctmappings;
	private Mapping[] cmappings;
	private List<Resource> funcDirs;
	private Boolean bufferOutput;
	private boolean secureJson;
	private String secureJsonPrefix = "//";
	private boolean isDefault;
	private Object defaultDataSource;
	private boolean ormEnabled;
	private Object ormdatasource;
	private ORMConfiguration ormConfig;
	private Properties s3;
	private IFTPConnectionData ftp;

	private Integer localMode;
	private Locale locale;
	private TimeZone timeZone;
	private CharSet webCharset;
	private CharSet resourceCharset;
	private Short sessionType;
	private Boolean sessionCluster;
	private Boolean clientCluster;
	private Resource source;
	private Boolean triggerComponentDataMember;
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

	private Short scopeCascading;
	private Boolean allowCompression;
	private Boolean suppressRemoteComponentContent;

	private short wstype;
	private Boolean cgiScopeReadonly;

	private SessionCookieData sessionCookie;

	private AuthCookieData authCookie;

	private Map<Key, Pair<Log, Struct>> logs;

	private Object mailListener;
	private TagListener queryListener;

	private boolean wsMaintainSession;

	private Boolean fullNullSupport;
	private ISerializationSettings serializationSettings = SerializationSettings.DEFAULT;

	private Boolean queryPSQ;
	private Integer queryVarUsage;

	private ProxyData proxyData;

	private TimeSpan queryCachedAfter;
	private String blockedExtForFileUpload;
	private Struct xmlFeatures;

	private Map<Key, Object> customAttrs;

	private Boolean allowImplicidQueryCall;
	private Boolean limitEvaluation;
	private Regex regex;

	private Boolean preciseMath;
	private Boolean formUrlAsStruct;

	private Integer returnFormat;

	private Boolean showDebug;

	private Boolean showDoc;

	private Boolean showMetric;

	private Boolean showTest;
	private Integer debugging;

	private ConfigPro cp;

	/**
	 * constructor of the class
	 * 
	 * @param config
	 */
	public ClassicApplicationContext(ConfigWeb config, String name, boolean isDefault, Resource source) {
		super(config);
		cp = (ConfigPro) config;
		this.name = name;

		loginStorage = Scope.SCOPE_SESSION;
		this.isDefault = isDefault;
		this.source = source;
		this.wstype = WS_TYPE_AXIS1;

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
		dbl.allowImplicidQueryCall = allowImplicidQueryCall;
		dbl.limitEvaluation = limitEvaluation;
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
		dbl.preciseMath = preciseMath;
		dbl.source = source;
		dbl.cgiScopeReadonly = cgiScopeReadonly;
		dbl.antiSamyPolicy = antiSamyPolicy;
		dbl.sessionCookie = sessionCookie;
		dbl.authCookie = authCookie;
		dbl.formUrlAsStruct = formUrlAsStruct;
		return dbl;
	}

	@Override
	public TimeSpan getApplicationTimeout() {
		if (applicationTimeout == null) return config.getApplicationTimeout();
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
		if (sessionTimeout == null) return config.getSessionTimeout();
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
		if (clientTimeout == null) return config.getClientTimeout();
		return clientTimeout;
	}

	/**
	 * @param clientTimeout The clientTimeout to set.
	 */
	@Override
	public void setClientTimeout(TimeSpan clientTimeout) {
		this.clientTimeout = clientTimeout;
	}

	@Override
	public boolean isSetClientCookies() {
		if (setClientCookies == null) return config.isClientCookies();
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
		if (setClientManagement == null) return config.isClientManagement();
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
		if (setDomainCookies == null) return config.isDomainCookies();
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
		if (setSessionManagement == null) return config.isSessionManagement();
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
		if (clientstorage == null) return ((ConfigPro) config).getClientStorage();
		return clientstorage;
	}

	@Override
	public String getSessionstorage() {
		if (sessionstorage == null) return ((ConfigPro) config).getSessionStorage();
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
		if (scriptProtect == null) return config.getScriptProtect();
		return scriptProtect;
	}

	/**
	 * @param typeChecking The typeChecking to set.
	 */
	@Override
	public void setTypeChecking(boolean typeChecking) {
		this.typeChecking = typeChecking;
	}

	@Override
	public boolean getTypeChecking() {
		if (typeChecking == null) return ((ConfigPro) config).getTypeChecking();
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
		if (bufferOutput == null) return ((ConfigPro) config).getBufferOutput();
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
		if (defaultDataSource == null) return config.getDefaultDataSource();
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
	public IFTPConnectionData getFTP() {
		if (ftp == null) ftp = new FTPConnectionData();
		return ftp;
	}

	@Override
	public int getLocalMode() {
		if (localMode == null) return config.getLocalMode();
		return localMode;
	}

	@Override
	public Locale getLocale() {
		if (locale == null) return config.getLocale();
		return locale;
	}

	@Override
	public TimeZone getTimeZone() {
		if (timeZone == null) return config.getTimeZone();
		return timeZone;
	}

	@Override
	public boolean getFullNullSupport() {
		if (!RuntimeProfile.ALLOW_FULL_NULL_SUPPORT) return false;
		if (fullNullSupport == null) return config.getFullNullSupport();
		return fullNullSupport;
	}

	@Override
	public Charset getWebCharset() {
		return CharsetUtil.toCharset(getWebCharSet());
	}

	public CharSet getWebCharSet() {
		if (webCharset == null) return ((ConfigPro) config).getWebCharSet();
		return webCharset;
	}

	@Override
	public Charset getResourceCharset() {
		return CharsetUtil.toCharset(getResourceCharSet());
	}

	public CharSet getResourceCharSet() {
		if (resourceCharset == null) return ((ConfigPro) config).getResourceCharSet();
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
		// Check if full null support has been globally disallowed
		if (!RuntimeProfile.ALLOW_FULL_NULL_SUPPORT && fullNullSupport) {
			throw new RuntimeException( "Full null support has been disallowed via lucee.allow.full.null.support=false. Cannot enable it per-application." );
		}
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
		if (sessionType == null) return config.getSessionType();
		return sessionType;
	}

	/**
	 * @param sessionType the sessionType
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
		if (sessionCluster == null) return config.getSessionCluster();
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
		if (clientCluster == null) return config.getClientCluster();
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
	public void setFTP(IFTPConnectionData ftp) {
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
		if (triggerComponentDataMember == null) return config.getTriggerComponentDataMember();
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

	public void setFormUrlAsStruct(PageContext pc, boolean formUrlAsStruct) {
		boolean changed = this.formUrlAsStruct != formUrlAsStruct;
		this.formUrlAsStruct = formUrlAsStruct;
		if (changed) {
			pc.urlScope().reinitialize(this);
			pc.formScope().reinitialize(this);
		}
	}

	@Override
	public boolean getFormUrlAsStruct() {
		if (formUrlAsStruct == null) return cp.getFormUrlAsStruct();
		return formUrlAsStruct;
	}

	@Override
	public RestSettings getRestSettings() {
		if (restSettings == null) return config.getRestSetting();
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
	public ClassLoader getRPCClassLoader() throws IOException {
		throw new RuntimeException("the method [getRPCClassLoader()] is no longer supported");
	}

	@Override
	public JavaSettings getJavaSettings() {
		if (javaSettings == null) return ((ConfigPro) config).getJavaSettings();
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
		if (scopeCascading == null) return config.getScopeCascadingType();
		return scopeCascading;
	}

	@Override
	public void setScopeCascading(short scopeCascading) {
		this.scopeCascading = scopeCascading;
	}

	@Override
	public boolean getAllowImplicidQueryCall() {
		if (allowImplicidQueryCall == null) return config.allowImplicidQueryCall();
		return allowImplicidQueryCall;
	}

	@Override
	public void setAllowImplicidQueryCall(boolean allowImplicidQueryCall) {
		this.allowImplicidQueryCall = allowImplicidQueryCall;
	}

	@Override
	public boolean getLimitEvaluation() {
		if (limitEvaluation == null) return ((ConfigPro) config).limitEvaluation();
		return limitEvaluation;
	}

	@Override
	public void setLimitEvaluation(boolean limitEvaluation) {
		this.limitEvaluation = limitEvaluation;
	}

	@Override
	public boolean getAllowCompression() {
		if (allowCompression == null) return ((ConfigPro) config).allowCompression();
		return allowCompression;
	}

	@Override
	public void setAllowCompression(boolean allowCompression) {
		this.allowCompression = allowCompression;
	}

	@Override
	public TimeSpan getRequestTimeout() {
		if (requestTimeout == null) return config.getRequestTimeout();
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
		if (suppressRemoteComponentContent == null) return ((ConfigPro) config).isSuppressContent();
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
		if (cgiScopeReadonly == null) return ((ConfigPro) config).getCGIScopeReadonly();
		return cgiScopeReadonly;
	}

	@Override
	public void setCGIScopeReadonly(boolean cgiScopeReadonly) {
		this.cgiScopeReadonly = cgiScopeReadonly;
	}

	@Override
	public Resource getAntiSamyPolicyResource(PageContext pc) {
		if (antiSamyPolicy == null) return cp.getAntiSamyPolicy();
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
	public ISerializationSettings getSerializationSettings() {
		return serializationSettings;
	}

	@Override
	public void setSerializationSettings(ISerializationSettings settings) {
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
		if (queryPSQ == null) return config.getPSQL();
		return queryPSQ;
	}

	@Override
	public void setQueryPSQ(boolean psq) {
		this.queryPSQ = psq;
	}

	@Override
	public int getQueryVarUsage() {
		if (queryVarUsage == null) return ((ConfigPro) config).getQueryVarUsage();
		return queryVarUsage;
	}

	@Override
	public void setQueryVarUsage(int varUsage) {
		this.queryVarUsage = varUsage;
	}

	@Override
	public TimeSpan getQueryCachedAfter() {
		if (queryCachedAfter == null) return ((ConfigPro) config).getCachedAfterTimeRange();
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

	@Override
	public Regex getRegex() {
		if (regex == null) return cp.getRegex();
		return regex;
	}

	@Override
	public void setRegex(Regex regex) {
		this.regex = regex;
	}

	@Override
	public boolean getPreciseMath() {
		if (!RuntimeProfile.ALLOW_PRECISE_MATH) return false;
		if (preciseMath == null) return cp.getPreciseMath();
		return preciseMath;
	}

	@Override
	public void setPreciseMath(boolean preciseMath) {
		// Check if precise math has been globally disallowed
		if (!RuntimeProfile.ALLOW_PRECISE_MATH && preciseMath) {
			throw new RuntimeException( "Precise math has been disallowed via lucee.allow.precise.math=false. Cannot enable it per-application." );
		}
		this.preciseMath = preciseMath;
	}

	@Override
	public int getReturnFormat() {
		if (returnFormat == null) return cp.getReturnFormat();
		return returnFormat;
	}

	@Override
	public void setReturnFormat(int returnFormat) {
		this.returnFormat = returnFormat;
	}

	@Override
	public boolean getShowDebug() {
		if (showDebug == null) return cp.getShowDebug();
		return this.showDebug;
	}

	@Override
	public boolean getShowDoc() {
		if (showDoc == null) return cp.getShowDoc();
		return this.showDoc;
	}

	@Override
	public boolean getShowMetric() {
		if (showMetric == null) return cp.getShowMetric();
		return this.showMetric;
	}

	@Override
	public boolean getShowTest() {
		if (showTest == null) return cp.getShowTest();
		return this.showTest;
	}

	@Override
	public void setShowDebug(boolean b) {
		this.showDebug = b;
	}

	@Override
	public void setShowDoc(boolean b) {
		this.showDoc = b;
	}

	@Override
	public void setShowMetric(boolean b) {
		this.showMetric = b;
	}

	@Override
	public void setShowTest(boolean b) {
		this.showTest = b;
	}

	@Override
	public boolean hasDebugOptions(int option) {
		return (getDebugOptions() & option) > 0;
	}

	@Override
	public int getDebugOptions() {
		if (debugging == null) {
			synchronized (SystemUtil.createToken("ClassicApplicationContext", "getDebugOptions")) {
				if (debugging == null) {
					int d = 0;
					if (cp.hasDebugOptions(ConfigPro.DEBUG_DATABASE)) d += ConfigPro.DEBUG_DATABASE;
					if (cp.hasDebugOptions(ConfigPro.DEBUG_DUMP)) d += ConfigPro.DEBUG_DUMP;
					if (cp.hasDebugOptions(ConfigPro.DEBUG_EXCEPTION)) d += ConfigPro.DEBUG_EXCEPTION;
					if (cp.hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS)) d += ConfigPro.DEBUG_IMPLICIT_ACCESS;
					if (cp.hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)) d += ConfigPro.DEBUG_QUERY_USAGE;
					if (cp.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) d += ConfigPro.DEBUG_TEMPLATE;
					if (cp.hasDebugOptions(ConfigPro.DEBUG_THREAD)) d += ConfigPro.DEBUG_THREAD;
					if (cp.hasDebugOptions(ConfigPro.DEBUG_TIMER)) d += ConfigPro.DEBUG_TIMER;
					if (cp.hasDebugOptions(ConfigPro.DEBUG_TRACING)) d += ConfigPro.DEBUG_TRACING;
					debugging = d;
				}
			}
		}

		return debugging;
	}

	@Override
	public void setDebugOptions(int option) {
		if (!hasDebugOptions(option)) debugging += option;
	}

	@Override
	public void remDebugOptions(int option) {
		if (hasDebugOptions(option)) debugging -= option;
	}

	@Override
	public AIEngine getAIEngine(String name) throws PageException {
		return ((ConfigPro) config).getAIEnginePool().getEngine(config, name);
	}

	@Override
	public String getAIEngineNameForDefault(String defaultName) {
		return null;
	}
}
