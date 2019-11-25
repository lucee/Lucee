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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.date.TimeZoneUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.cache.exp.CacheException;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.ftp.FTPConnectionData;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.SystemOut;
import lucee.commons.lang.types.RefBoolean;
import lucee.runtime.Component;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheConnectionImpl;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.component.Member;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.s3.Properties;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMConfigurationImpl;
import lucee.runtime.rest.RestSettingImpl;
import lucee.runtime.rest.RestSettings;
import lucee.runtime.security.Credential;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.CustomType;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFCustomType;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.library.ClassDefinitionImpl;

import org.osgi.framework.BundleException;

public class ModernApplicationContext extends ApplicationContextSupport {

	private static final long serialVersionUID = -8230105685329758613L;

	private static final Collection.Key APPLICATION_TIMEOUT = KeyConstants._applicationTimeout;
	private static final Collection.Key CLIENT_MANAGEMENT = KeyConstants._clientManagement;
	private static final Collection.Key CLIENT_STORAGE = KeyImpl.intern("clientStorage");
	private static final Collection.Key SESSION_STORAGE = KeyImpl.intern("sessionStorage");
	private static final Collection.Key LOGIN_STORAGE = KeyImpl.intern("loginStorage");
	private static final Collection.Key SESSION_TYPE = KeyImpl.intern("sessionType");
	private static final Collection.Key WS_SETTINGS = KeyImpl.intern("wssettings");
	private static final Collection.Key WS_SETTING = KeyImpl.intern("wssetting");
	private static final Collection.Key TRIGGER_DATA_MEMBER = KeyImpl.intern("triggerDataMember");
	private static final Collection.Key INVOKE_IMPLICIT_ACCESSOR = KeyImpl.intern("InvokeImplicitAccessor");
	private static final Collection.Key SESSION_MANAGEMENT = KeyImpl.intern("sessionManagement");
	private static final Collection.Key SESSION_TIMEOUT = KeyImpl.intern("sessionTimeout");
	private static final Collection.Key CLIENT_TIMEOUT = KeyImpl.intern("clientTimeout");
	private static final Collection.Key REQUEST_TIMEOUT = KeyImpl.intern("requestTimeout");
	private static final Collection.Key SET_CLIENT_COOKIES = KeyImpl.intern("setClientCookies");
	private static final Collection.Key SET_DOMAIN_COOKIES = KeyImpl.intern("setDomainCookies");
	private static final Collection.Key SCRIPT_PROTECT = KeyImpl.intern("scriptProtect");
	private static final Collection.Key CUSTOM_TAG_PATHS = KeyImpl.intern("customtagpaths");
	private static final Collection.Key COMPONENT_PATHS = KeyImpl.intern("componentpaths");
	private static final Collection.Key SECURE_JSON_PREFIX = KeyImpl.intern("secureJsonPrefix");
	private static final Collection.Key SECURE_JSON = KeyImpl.intern("secureJson");
	private static final Collection.Key LOCAL_MODE = KeyImpl.intern("localMode");
	private static final Collection.Key BUFFER_OUTPUT = KeyImpl.intern("bufferOutput");
	private static final Collection.Key SESSION_CLUSTER = KeyImpl.intern("sessionCluster");
	private static final Collection.Key CLIENT_CLUSTER = KeyImpl.intern("clientCluster");

	private static final Collection.Key DEFAULT_DATA_SOURCE = KeyImpl.intern("defaultdatasource");
	private static final Collection.Key DEFAULT_CACHE = KeyImpl.intern("defaultcache");

	private static final Collection.Key ORM_ENABLED = KeyImpl.intern("ormenabled");
	private static final Collection.Key ORM_SETTINGS = KeyImpl.intern("ormsettings");
	private static final Collection.Key IN_MEMORY_FILESYSTEM = KeyImpl.intern("inmemoryfilesystem");
	private static final Collection.Key REST_SETTING = KeyImpl.intern("restsettings");
	private static final Collection.Key JAVA_SETTING = KeyImpl.intern("javasettings");
	private static final Collection.Key SCOPE_CASCADING = KeyImpl.intern("scopeCascading");
	private static final Collection.Key SEARCH_IMPLICIT_SCOPES = KeyImpl.intern("searchImplicitScopes");
	private static final Collection.Key TYPE_CHECKING = KeyImpl.intern("typeChecking");
	private static final Collection.Key CGI_READONLY = KeyImpl.intern("CGIReadOnly");;
	private static final Collection.Key SUPPRESS_CONTENT = KeyImpl.intern("suppressRemoteComponentContent");
	private static final Collection.Key LOGS = KeyImpl.intern("logs");
	private static final Collection.Key LOG = KeyImpl.intern("log");

	private static final Collection.Key SESSION_COOKIE = KeyImpl.intern("sessioncookie");
	private static final Collection.Key AUTH_COOKIE = KeyImpl.intern("authcookie");
    private static final Collection.Key XML_FEATURES = KeyImpl.intern("xmlFeatures");


    private static Map<String, CacheConnection> initCacheConnections = new ConcurrentHashMap<String, CacheConnection>();

	private Component component;

	private String name = null;

	private boolean setClientCookies;
	private boolean setDomainCookies;
	private boolean setSessionManagement;
	private boolean setClientManagement;
	private TimeSpan applicationTimeout;
	private TimeSpan sessionTimeout;
	private TimeSpan clientTimeout;
	private TimeSpan requestTimeout;
	private int loginStorage = Scope.SCOPE_COOKIE;
	private int scriptProtect;
	private boolean typeChecking;
	private boolean allowCompression;
	private Object defaultDataSource;
	private boolean bufferOutput;
	private boolean suppressContent;
	private short sessionType;
	private short wstype;
	private boolean wsMaintainSession = false;
	private boolean sessionCluster;
	private boolean clientCluster;

	private String clientStorage;
	private String sessionStorage;
	private String secureJsonPrefix = "//";
	private boolean secureJson;
	private Mapping[] ctmappings;
	private Mapping[] cmappings;
	private DataSource[] dataSources;

	private lucee.runtime.net.s3.Properties s3;
	private FTPConnectionData ftp;
	private boolean triggerComponentDataMember;
	private Map<Integer, String> defaultCaches;
	private Map<Collection.Key, CacheConnection> cacheConnections;
	private boolean sameFormFieldAsArray;
	private boolean sameURLFieldAsArray;
	private Map<String, CustomType> customTypes;
	private boolean cgiScopeReadonly;
	private SessionCookieData sessionCookie;
	private AuthCookieData authCookie;
	private Object mailListener;

	private Mapping[] mappings;
	private boolean initMappings;
	private boolean initCustomTypes;
	private boolean initMailListener;
	private boolean initCachedWithins;

	private boolean initApplicationTimeout;
	private boolean initSessionTimeout;
	private boolean initClientTimeout;
	private boolean initRequestTimeout;
	private boolean initSetClientCookies;
	private boolean initSetClientManagement;
	private boolean initSetDomainCookies;
	private boolean initSetSessionManagement;
	private boolean initScriptProtect;
	private boolean initTypeChecking;
	private boolean initAllowCompression;
	private boolean initDefaultAttributeValues;
	private boolean initClientStorage;
	private boolean initSecureJsonPrefix;
	private boolean initSecureJson;
	private boolean initSessionStorage;
	private boolean initSessionCluster;
	private boolean initClientCluster;
	private boolean initLoginStorage;
	private boolean initSessionType;
	private boolean initWS;
	private boolean initTriggerComponentDataMember;
	private boolean initDataSources;
	private boolean initCache;
	private boolean initCTMappings;
	private boolean initCMappings;
	private int localMode;
	private boolean initLocalMode;
	private boolean initBufferOutput;
	private boolean initSuppressContent;
	private boolean initS3;
	private boolean initFTP;
	private boolean ormEnabled;
	private ORMConfiguration ormConfig;
	private boolean initRestSetting;
	private RestSettings restSetting;
	private boolean initJavaSettings;
	private JavaSettings javaSettings;
	private Object ormDatasource;
	private Locale locale;
	private boolean initLocale;
	private TimeZone timeZone;
	private boolean initTimeZone;
	private CharSet webCharset;
	private boolean initWebCharset;
	private CharSet resourceCharset;
	private boolean initResourceCharset;
	private boolean initCGIScopeReadonly;
	private boolean initSessionCookie;
	private boolean initAuthCookie;
    private boolean initXmlFeatures;
    private Struct xmlFeatures;

	private Resource antiSamyPolicyResource;

	private Resource[] restCFCLocations;

	private short scopeCascading = -1;

	private Server[] mailServers;
	private boolean initMailServer;

	private boolean initLog;

	private Map<Collection.Key, Pair<Log, Struct>> logs;

	public ModernApplicationContext(PageContext pc, Component cfc, RefBoolean throwsErrorWhileInit) {
		super(pc.getConfig());
		ConfigImpl ci = ((ConfigImpl)config);
		setClientCookies = config.isClientCookies();
		setDomainCookies = config.isDomainCookies();
		setSessionManagement = config.isSessionManagement();
		setClientManagement = config.isClientManagement();
		sessionTimeout = config.getSessionTimeout();
		clientTimeout = config.getClientTimeout();
		requestTimeout = config.getRequestTimeout();
		applicationTimeout = config.getApplicationTimeout();
		scriptProtect = config.getScriptProtect();
		typeChecking = ci.getTypeChecking();
		allowCompression = ci.allowCompression();
		this.defaultDataSource = config.getDefaultDataSource();
		this.localMode = config.getLocalMode();
		this.locale = config.getLocale();
		this.timeZone = config.getTimeZone();
		this.webCharset = ci.getWebCharSet();
		this.resourceCharset = ci.getResourceCharSet();
		this.bufferOutput = ci.getBufferOutput();
		suppressContent = ci.isSuppressContent();
		this.sessionType = config.getSessionType();
		this.wstype = WS_TYPE_AXIS1;
		this.cgiScopeReadonly = ci.getCGIScopeReadonly();

		this.sessionCluster = config.getSessionCluster();
		this.clientCluster = config.getClientCluster();
		this.sessionStorage = ci.getSessionStorage();
		this.clientStorage = ci.getClientStorage();

		this.triggerComponentDataMember = config.getTriggerComponentDataMember();
		this.restSetting = config.getRestSetting();
		this.javaSettings = new JavaSettingsImpl();
		this.component = cfc;

		initAntiSamyPolicyResource(pc);
		if(antiSamyPolicyResource == null)
			this.antiSamyPolicyResource = ((ConfigImpl)config).getAntiSamyPolicy();
		// read scope cascading
		initScopeCascading();
		initSameFieldAsArray(pc);
		initWebCharset(pc);

		pc.addPageSource(component.getPageSource(), true);
		try {

			/////////// ORM /////////////////////////////////
			reinitORM(pc);

			throwsErrorWhileInit.setValue(false);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throwsErrorWhileInit.setValue(true);
			pc.removeLastPageSource(true);
		}
	}

	public void initScopeCascading() {
		Object o = get(component, SCOPE_CASCADING, null);
		if(o != null) {
			scopeCascading = ConfigWebUtil.toScopeCascading(Caster.toString(o, null), (short)-1);
		}
		else {
			Boolean b = Caster.toBoolean(get(component, SEARCH_IMPLICIT_SCOPES, null), null);
			if(b != null)
				scopeCascading = ConfigWebUtil.toScopeCascading(b);
		}

	}

	@Override
	public short getScopeCascading() {
		if(scopeCascading == -1)
			return config.getScopeCascadingType();
		return scopeCascading;
	}

	@Override
	public void setScopeCascading(short scopeCascading) {
		this.scopeCascading = scopeCascading;
	}

	@Override
	public void reinitORM(PageContext pc) throws PageException {

		// datasource
		Object o = get(component, KeyConstants._datasource, null);
		if(o != null) {
			this.ormDatasource = this.defaultDataSource = AppListenerUtil.toDefaultDatasource(pc.getConfig(), o, pc.getConfig().getLog("application"));
		}

		// default datasource
		o = get(component, DEFAULT_DATA_SOURCE, null);
		if(o != null) {
			this.defaultDataSource = AppListenerUtil.toDefaultDatasource(pc.getConfig(), o, pc.getConfig().getLog("application"));
		}

		// ormenabled
		o = get(component, ORM_ENABLED, null);
		if(o != null && Caster.toBooleanValue(o, false)) {
			this.ormEnabled = true;

			// settings
			o = get(component, ORM_SETTINGS, null);
			Struct settings;
			if(o instanceof Struct)
				settings = (Struct)o;
			else
				settings = new StructImpl();
			AppListenerUtil.setORMConfiguration(pc, this, settings);
		}
	}

	@Override
	public boolean hasName() {
		return true;// !StringUtil.isEmpty(getName());
	}

	@Override
	public String getName() {
		if(this.name == null) {
			this.name = Caster.toString(get(component, KeyConstants._name, ""), "");
		}
		return name;
	}

	@Override
	public int getLoginStorage() {
		if(!initLoginStorage) {
			String str = null;
			Object o = get(component, LOGIN_STORAGE, null);
			if(o != null) {
				str = Caster.toString(o, null);
				if(str != null)
					loginStorage = AppListenerUtil.translateLoginStorage(str, loginStorage);
			}
			initLoginStorage = true;
		}
		return loginStorage;
	}

	@Override
	public TimeSpan getApplicationTimeout() {
		if(!initApplicationTimeout) {
			Object o = get(component, APPLICATION_TIMEOUT, null);
			if(o != null)
				applicationTimeout = Caster.toTimespan(o, applicationTimeout);
			initApplicationTimeout = true;
		}
		return applicationTimeout;
	}

	@Override
	public TimeSpan getSessionTimeout() {
		if(!initSessionTimeout) {
			Object o = get(component, SESSION_TIMEOUT, null);
			if(o != null)
				sessionTimeout = Caster.toTimespan(o, sessionTimeout);
			initSessionTimeout = true;
		}
		return sessionTimeout;
	}

	@Override
	public TimeSpan getClientTimeout() {
		if(!initClientTimeout) {
			Object o = get(component, CLIENT_TIMEOUT, null);
			if(o != null)
				clientTimeout = Caster.toTimespan(o, clientTimeout);
			initClientTimeout = true;
		}
		return clientTimeout;
	}

	@Override
	public TimeSpan getRequestTimeout() {
		if(!initRequestTimeout) {
			Object o = get(component, REQUEST_TIMEOUT, null);
			if(o == null)
				o = get(component, KeyConstants._timeout, null);
			if(o != null)
				requestTimeout = Caster.toTimespan(o, requestTimeout);
			initRequestTimeout = true;
		}
		return requestTimeout;
	}

	@Override
	public void setRequestTimeout(TimeSpan requestTimeout) {
		this.requestTimeout = requestTimeout;
		initRequestTimeout = true;
	}

	@Override
	public boolean isSetClientCookies() {
		if(!initSetClientCookies) {
			Object o = get(component, SET_CLIENT_COOKIES, null);
			if(o != null)
				setClientCookies = Caster.toBooleanValue(o, setClientCookies);
			initSetClientCookies = true;
		}
		return setClientCookies;
	}

	@Override
	public boolean isSetClientManagement() {
		if(!initSetClientManagement) {
			Object o = get(component, CLIENT_MANAGEMENT, null);
			if(o != null)
				setClientManagement = Caster.toBooleanValue(o, setClientManagement);
			initSetClientManagement = true;
		}
		return setClientManagement;
	}

	@Override
	public boolean isSetDomainCookies() {
		if(!initSetDomainCookies) {
			Object o = get(component, SET_DOMAIN_COOKIES, null);
			if(o != null)
				setDomainCookies = Caster.toBooleanValue(o, setDomainCookies);
			initSetDomainCookies = true;
		}
		return setDomainCookies;
	}

	@Override
	public boolean isSetSessionManagement() {
		if(!initSetSessionManagement) {
			Object o = get(component, SESSION_MANAGEMENT, null);
			if(o != null)
				setSessionManagement = Caster.toBooleanValue(o, setSessionManagement);
			initSetSessionManagement = true;
		}
		return setSessionManagement;
	}

	@Override
	public String getClientstorage() {
		if(!initClientStorage) {
			String str = Caster.toString(get(component, CLIENT_STORAGE, null), null);
			if(!StringUtil.isEmpty(str))
				clientStorage = str;
			initClientStorage = true;
		}
		return clientStorage;
	}

	@Override
	public int getScriptProtect() {
		if(!initScriptProtect) {
			String str = null;
			Object o = get(component, SCRIPT_PROTECT, null);
			if(o != null) {
				str = Caster.toString(o, null);
				if(str != null)
					scriptProtect = AppListenerUtil.translateScriptProtect(str);
			}
			initScriptProtect = true;
		}
		return scriptProtect;
	}

	@Override
	public boolean getTypeChecking() {
		if(!initTypeChecking) {
			Boolean b = Caster.toBoolean(get(component, TYPE_CHECKING, null), null);
			if(b != null)
				typeChecking = b.booleanValue();
			initTypeChecking = true;
		}
		return typeChecking;
	}

	@Override
	public boolean getAllowCompression() {
		if(!initAllowCompression) {
			Boolean b = Caster.toBoolean(get(component, KeyConstants._compression, null), null);
			if(b != null)
				allowCompression = b.booleanValue();
			initAllowCompression = true;
		}
		return allowCompression;
	}

	@Override
	public void setAllowCompression(boolean allowCompression) {
		this.allowCompression = allowCompression;
		initAllowCompression = true;
	}

	@Override
	public String getSecureJsonPrefix() {
		if(!initSecureJsonPrefix) {
			Object o = get(component, SECURE_JSON_PREFIX, null);
			if(o != null)
				secureJsonPrefix = Caster.toString(o, secureJsonPrefix);
			initSecureJsonPrefix = true;
		}
		return secureJsonPrefix;
	}

	@Override
	public boolean getSecureJson() {
		if(!initSecureJson) {
			Object o = get(component, SECURE_JSON, null);
			if(o != null)
				secureJson = Caster.toBooleanValue(o, secureJson);
			initSecureJson = true;
		}
		return secureJson;
	}

	@Override
	public String getSessionstorage() {
		if(!initSessionStorage) {
			String str = Caster.toString(get(component, SESSION_STORAGE, null), null);
			if(!StringUtil.isEmpty(str))
				sessionStorage = str;
			initSessionStorage = true;
		}
		return sessionStorage;
	}

	@Override
	public boolean getSessionCluster() {
		if(!initSessionCluster) {
			Object o = get(component, SESSION_CLUSTER, null);
			if(o != null)
				sessionCluster = Caster.toBooleanValue(o, sessionCluster);
			initSessionCluster = true;
		}
		return sessionCluster;
	}

	@Override
	public boolean getClientCluster() {
		if(!initClientCluster) {
			Object o = get(component, CLIENT_CLUSTER, null);
			if(o != null)
				clientCluster = Caster.toBooleanValue(o, clientCluster);
			initClientCluster = true;
		}
		return clientCluster;
	}

	@Override
	public short getSessionType() {
		if(!initSessionType) {
			String str = null;
			Object o = get(component, SESSION_TYPE, null);
			if(o != null) {
				str = Caster.toString(o, null);
				if(str != null)
					sessionType = AppListenerUtil.toSessionType(str, sessionType);
			}
			initSessionType = true;
		}
		return sessionType;
	}

	@Override
	public short getWSType() {
		initWS();
		return wstype;
	}

	@Override
	public boolean getWSMaintainSession() {
		initWS();
		return wsMaintainSession;
	}

	@Override
	public void setWSMaintainSession(boolean wsMaintainSession) {
		initWS = true;
		this.wsMaintainSession = wsMaintainSession;
	}

	public void initWS() {
		if(!initWS) {
			Object o = get(component, WS_SETTINGS, null);
			if(o == null)
				o = get(component, WS_SETTING, null);
			if(o instanceof Struct) {
				Struct sct = (Struct)o;

				// type
				o = sct.get(KeyConstants._type, null);
				if(o instanceof String) {
					wstype = AppListenerUtil.toWSType(Caster.toString(o, null), WS_TYPE_AXIS1);
				}

				// MaintainSession
				o = sct.get("MaintainSession", null);
				if(o != null) {
					wsMaintainSession = Caster.toBooleanValue(o, false);
				}
			}
			initWS = true;
		}
	}

	@Override
	public void setWSType(short wstype) {
		initWS = true;
		this.wstype = wstype;
	}

	@Override
	public boolean getTriggerComponentDataMember() {
		if(!initTriggerComponentDataMember) {
			Boolean b = null;
			Object o = get(component, INVOKE_IMPLICIT_ACCESSOR, null);
			if(o == null)
				o = get(component, TRIGGER_DATA_MEMBER, null);
			if(o != null) {
				b = Caster.toBoolean(o, null);
				if(b != null)
					triggerComponentDataMember = b.booleanValue();
			}
			initTriggerComponentDataMember = true;
		}
		return triggerComponentDataMember;
	}

	@Override
	public void setTriggerComponentDataMember(boolean triggerComponentDataMember) {
		initTriggerComponentDataMember = true;
		this.triggerComponentDataMember = triggerComponentDataMember;
	}

	@Override
	public boolean getSameFieldAsArray(int scope) {
		return Scope.SCOPE_URL == scope ? sameURLFieldAsArray : sameFormFieldAsArray;
	}

	public void initSameFieldAsArray(PageContext pc) {
		boolean oldForm = pc.getApplicationContext().getSameFieldAsArray(Scope.SCOPE_FORM);
		boolean oldURL = pc.getApplicationContext().getSameFieldAsArray(Scope.SCOPE_URL);

		// Form
		Object o = get(component, KeyImpl.init("sameformfieldsasarray"), null);
		if(o != null && Decision.isBoolean(o))
			sameFormFieldAsArray = Caster.toBooleanValue(o, false);

		// URL
		o = get(component, KeyImpl.init("sameurlfieldsasarray"), null);
		if(o != null && Decision.isBoolean(o))
			sameURLFieldAsArray = Caster.toBooleanValue(o, false);

		if(oldForm != sameFormFieldAsArray)
			pc.formScope().reinitialize(this);
		if(oldURL != sameURLFieldAsArray)
			pc.urlScope().reinitialize(this);
	}

	public void initWebCharset(PageContext pc) {
		initCharset();
		Charset cs = getWebCharset();
		// has defined a web charset
		if(cs != null) {
			if(!cs.equals(config.getWebCharset())) {
				ReqRspUtil.setContentType(pc.getHttpServletResponse(), "text/html; charset=" + cs.name());
			}
		}

	}

	@Override
	public String getDefaultCacheName(int type) {
		initCache();
		return defaultCaches.get(type);
	}

	@Override
	public Server[] getMailServers() {
		initMailServers();
		return mailServers;
	}

	private void initMailServers() {

		if(!initMailServer) {

			Key key;
			Object oMail = get(component, key = KeyConstants._mail, null);

			if(oMail == null)
				oMail = get(component, key = KeyConstants._mails, null);

			if(oMail == null)
				oMail = get(component, key = KeyConstants._mailServer, null);

			if(oMail == null)
				oMail = get(component, key = KeyConstants._mailServers, null);

			if(oMail == null)
				oMail = get(component, key = KeyConstants._smtpServerSettings, null);

			Array arrMail = Caster.toArray(oMail, null);
			// we also support a single struct instead of an array of structs
			if(arrMail == null) {
				Struct sctMail = Caster.toStruct(get(component, key, null), null);
				if(sctMail != null) {
					arrMail = new ArrayImpl();
					arrMail.appendEL(sctMail);
				}
			}

			if(arrMail != null) {
				mailServers = AppListenerUtil.toMailServers(config, arrMail, null);
			}

			initMailServer = true;
		}
	}

	public void setMailServers(Server[] servers) {
		this.mailServers = servers;
		this.initMailServer = true;
	}

	@Override
	public CacheConnection getCacheConnection(String cacheName, CacheConnection defaultValue) {
		initCache();
		return cacheConnections.get(KeyImpl.init(cacheName));
	}

	public Key[] getCacheConnectionNames() {
		initCache();
		Set<Key> set = cacheConnections.keySet();
		return set.toArray(new Key[set.size()]);
	}

	private void initCache() {
		if(!initCache) {
			boolean hasResource = false;
			if(defaultCaches == null)
				defaultCaches = new ConcurrentHashMap<Integer, String>();
			if(cacheConnections == null)
				cacheConnections = new ConcurrentHashMap<Collection.Key, CacheConnection>();
			Struct sctDefCache = Caster.toStruct(get(component, DEFAULT_CACHE, null), null);
			if(sctDefCache == null)
				sctDefCache = Caster.toStruct(get(component, KeyConstants._cache, null), null);

			// Default
			if(sctDefCache != null) {
				// Function
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_FUNCTION, KeyConstants._function);
				// Query
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_QUERY, KeyConstants._query);
				// Template
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_TEMPLATE, KeyConstants._template);
				// Object
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_OBJECT, KeyConstants._object);
				// INCLUDE
				initDefaultCache(sctDefCache, Config.CACHE_TYPE_INCLUDE, KeyConstants._include);
				// Resource
				if(initDefaultCache(sctDefCache, Config.CACHE_TYPE_RESOURCE, KeyConstants._resource))
					hasResource = true;
				// HTTP
				if(initDefaultCache(sctDefCache, Config.CACHE_TYPE_HTTP, KeyConstants._http))
					hasResource = true;
				// File
				if(initDefaultCache(sctDefCache, Config.CACHE_TYPE_FILE, KeyConstants._file))
					hasResource = true;
				// Webservice
				if(initDefaultCache(sctDefCache, Config.CACHE_TYPE_WEBSERVICE, KeyConstants._webservice))
					hasResource = true;
			}
			// check alias inmemoryfilesystem
			if(!hasResource) {
				String str = Caster.toString(get(component, IN_MEMORY_FILESYSTEM, null), null);
				if(!StringUtil.isEmpty(str, true)) {
					defaultCaches.put(Config.CACHE_TYPE_RESOURCE, str.trim());
				}
			}

			// cache definitions
			Struct sctCache = Caster.toStruct(get(component, KeyConstants._cache, null), null);
			if(sctCache != null) {
				Iterator<Entry<Key, Object>> it = sctCache.entryIterator();

				_initCache(cacheConnections, it, false);

			}
			initCache = true;
		}
	}

	private void _initCache(Map<Key, CacheConnection> cacheConnections, Iterator<Entry<Key, Object>> it, boolean sub) {
		Entry<Key, Object> e;
		Struct sct;
		CacheConnection cc;
		while(it.hasNext()) {
			e = it.next();

			if(!sub && KeyConstants._function.equals(e.getKey()) || KeyConstants._query.equals(e.getKey()) || KeyConstants._template.equals(e.getKey())
					|| KeyConstants._object.equals(e.getKey()) || KeyConstants._include.equals(e.getKey()) || KeyConstants._resource.equals(e.getKey())
					|| KeyConstants._http.equals(e.getKey()) || KeyConstants._file.equals(e.getKey()) || KeyConstants._webservice.equals(e.getKey()))
				continue;

			if(!sub && KeyConstants._connections.equals(e.getKey())) {
				Struct _sct = Caster.toStruct(e.getValue(), null);
				if(_sct != null)
					_initCache(cacheConnections, _sct.entryIterator(), true);
				continue;

			}

			sct = Caster.toStruct(e.getValue(), null);
			if(sct == null)
				continue;

			cc = toCacheConnection(config, e.getKey().getString(), sct, null);

			if(cc != null) {
				cacheConnections.put(e.getKey(), cc);
				Key def = Caster.toKey(sct.get(KeyConstants._default, null), null);
				if(def != null) {
					String n = e.getKey().getString().trim();
					if(KeyConstants._function.equals(def))
						defaultCaches.put(Config.CACHE_TYPE_FUNCTION, n);
					else if(KeyConstants._query.equals(def))
						defaultCaches.put(Config.CACHE_TYPE_QUERY, n);
					else if(KeyConstants._template.equals(def))
						defaultCaches.put(Config.CACHE_TYPE_TEMPLATE, n);
					else if(KeyConstants._object.equals(def))
						defaultCaches.put(Config.CACHE_TYPE_OBJECT, n);
					else if(KeyConstants._include.equals(def))
						defaultCaches.put(Config.CACHE_TYPE_INCLUDE, n);
					else if(KeyConstants._resource.equals(def))
						defaultCaches.put(Config.CACHE_TYPE_RESOURCE, n);
					else if(KeyConstants._http.equals(def))
						defaultCaches.put(Config.CACHE_TYPE_HTTP, n);
					else if(KeyConstants._file.equals(def))
						defaultCaches.put(Config.CACHE_TYPE_FILE, n);
					else if(KeyConstants._webservice.equals(def))
						defaultCaches.put(Config.CACHE_TYPE_WEBSERVICE, n);
				}
			}
		}
	}

	private boolean initDefaultCache(Struct data, int type, Key key) {
		Object o = data.get(key, null);
		boolean hasResource = false;
		if(o != null) {
			String name;
			Struct sct;
			CacheConnection cc;

			if(!StringUtil.isEmpty(name = Caster.toString(o, null), true)) {
				defaultCaches.put(type, name.trim());
				hasResource = true;
			}
			else if((sct = Caster.toStruct(o, null)) != null) {
				cc = toCacheConnection(config, key.getString(), sct, null);
				if(cc != null) {
					cacheConnections.put(key, cc);
					defaultCaches.put(type, key.getString());
					hasResource = true;
				}
			}
		}
		return hasResource;
	}

	public static CacheConnection toCacheConnection(Config config, String name, Struct data, CacheConnection defaultValue) {
		try {
			return toCacheConnection(config, name, data);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static CacheConnection toCacheConnection(Config config, String name, Struct data)
			throws ApplicationException, CacheException, ClassException, BundleException {
		// class definition
		String className = Caster.toString(data.get(KeyConstants._class, null), null);
		if(StringUtil.isEmpty(className))
			throw new ApplicationException("missing key class in struct the defines a cachec connection");
		ClassDefinition cd = new ClassDefinitionImpl(className, Caster.toString(data.get(KeyConstants._bundleName, null), null),
				Caster.toString(data.get(KeyConstants._bundleVersion, null), null), config.getIdentification());

		CacheConnectionImpl cc = new CacheConnectionImpl(config, name, cd, Caster.toStruct(data.get(KeyConstants._custom, null), null),
				Caster.toBooleanValue(data.get(KeyConstants._readonly, null), false), Caster.toBooleanValue(data.get(KeyConstants._storage, null), false));
		String id = cc.id();
		CacheConnection icc = initCacheConnections.get(id);
		if(icc != null)
			return icc;
		try {
			Method m = cd.getClazz().getMethod("init", new Class[] { Config.class, String[].class, Struct[].class });
			if(Modifier.isStatic(m.getModifiers()))
				m.invoke(null, new Object[] { config, new String[] { cc.getName() }, new Struct[] { cc.getCustom() } });
			else
				SystemOut.print(config.getErrWriter(), "method [init(Config,String[],Struct[]):void] for class [" + cd.toString() + "] is not static");

			initCacheConnections.put(id, cc);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return cc;

	}

	@Override
	public void setDefaultCacheName(int type, String cacheName) {
		if(StringUtil.isEmpty(cacheName, true))
			return;

		initCache();
		defaultCaches.put(type, cacheName.trim());
	}

	public void setCacheConnection(String cacheName, CacheConnection cc) {
		if(StringUtil.isEmpty(cacheName, true))
			return;
		initCache();
		cacheConnections.put(KeyImpl.init(cacheName), cc);
	}

	@Override
	public Object getMailListener() {
		if(!initMailListener) {
			Struct mail = Caster.toStruct(get(component, KeyConstants._mail, null), null);
			if(mail != null)
				mailListener = mail.get(KeyConstants._listener, null);

			initMailListener = true;
		}
		return mailListener;
	}

	@Override
	public Mapping[] getMappings() {
		if(!initMappings) {
			Object o = get(component, KeyConstants._mappings, null);
			if(o != null)
				mappings = AppListenerUtil.toMappings(config, o, mappings, getSource());
			initMappings = true;
		}
		return mappings;
	}

	@Override
	public Mapping[] getCustomTagMappings() {
		if(!initCTMappings) {
			Object o = get(component, CUSTOM_TAG_PATHS, null);
			if(o != null)
				ctmappings = AppListenerUtil.toCustomTagMappings(config, o, getSource(), ctmappings);
			initCTMappings = true;
		}
		return ctmappings;
	}

	@Override
	public Mapping[] getComponentMappings() {
		if(!initCMappings) {
			Object o = get(component, COMPONENT_PATHS, null);
			if(o != null)
				cmappings = AppListenerUtil.toComponentMappings(config, o, getSource(), cmappings);
			initCMappings = true;
		}
		return cmappings;
	}

	@Override
	public int getLocalMode() {
		if(!initLocalMode) {
			Object o = get(component, LOCAL_MODE, null);
			if(o != null)
				localMode = AppListenerUtil.toLocalMode(o, localMode);
			initLocalMode = true;
		}
		return localMode;
	}

	@Override
	public Locale getLocale() {
		if(!initLocale) {
			Object o = get(component, KeyConstants._locale, null);
			if(o != null) {
				String str = Caster.toString(o, null);
				if(!StringUtil.isEmpty(str))
					locale = LocaleFactory.getLocale(str, locale);
			}
			initLocale = true;
		}
		return locale;
	}

	@Override
	public TimeZone getTimeZone() {
		if(!initTimeZone) {
			Object o = get(component, KeyConstants._timezone, null);
			if(o != null) {
				String str = Caster.toString(o, null);
				if(!StringUtil.isEmpty(str))
					timeZone = TimeZoneUtil.toTimeZone(str, timeZone);
			}
			initTimeZone = true;
		}
		return timeZone;
	}

	@Override
	public Charset getWebCharset() {
		if(!initWebCharset)
			initCharset();
		return CharsetUtil.toCharset(webCharset);
	}

	public CharSet getWebCharSet() {
		if(!initWebCharset)
			initCharset();
		return webCharset;
	}

	@Override
	public Resource getAntiSamyPolicyResource() {
		return antiSamyPolicyResource;
	}

	@Override
	public void setAntiSamyPolicyResource(Resource res) {
		antiSamyPolicyResource = res;
	}

	public void initAntiSamyPolicyResource(PageContext pc) {
		Struct sct = Caster.toStruct(get(component, KeyConstants._security, null), null);
		if(sct != null) {
			Resource tmp = ResourceUtil.toResourceExisting(pc, Caster.toString(sct.get("antisamypolicy", null), null), true, null);
			if(tmp != null)
				antiSamyPolicyResource = tmp;
		}
	}

	@Override
	public Charset getResourceCharset() {
		if(!initResourceCharset)
			initCharset();
		return CharsetUtil.toCharset(resourceCharset);
	}

	public CharSet getResourceCharSet() {
		if(!initResourceCharset)
			initCharset();
		return resourceCharset;
	}

	/**
	 * @return webcharset if it was defined, otherwise null
	 */
	private CharSet initCharset() {
		Object o = get(component, KeyConstants._charset, null);
		if(o != null) {
			Struct sct = Caster.toStruct(o, null);
			if(sct != null) {
				CharSet web = CharsetUtil.toCharSet(Caster.toString(sct.get(KeyConstants._web, null), null), null);
				if(!initWebCharset && web != null)
					webCharset = web;
				CharSet res = CharsetUtil.toCharSet(Caster.toString(sct.get(KeyConstants._resource, null), null), null);
				if(!initResourceCharset && res != null)
					resourceCharset = res;

				initWebCharset = true;
				initResourceCharset = true;
				return web;
			}
		}
		initWebCharset = true;
		initResourceCharset = true;
		return null;
	}

	@Override
	public boolean getBufferOutput() {
		boolean bo = _getBufferOutput();
		return bo;
	}

	public boolean _getBufferOutput() {
		if(!initBufferOutput) {
			Object o = get(component, BUFFER_OUTPUT, null);
			if(o != null)
				bufferOutput = Caster.toBooleanValue(o, bufferOutput);
			initBufferOutput = true;
		}
		return bufferOutput;
	}

	@Override
	public boolean getSuppressContent() {
		if(!initSuppressContent) {
			Object o = get(component, SUPPRESS_CONTENT, null);
			if(o != null)
				suppressContent = Caster.toBooleanValue(o, suppressContent);
			initSuppressContent = true;
		}
		return suppressContent;
	}

	@Override
	public void setSuppressContent(boolean suppressContent) {
		this.suppressContent = suppressContent;
		initSuppressContent = true;
	}

	@Override
	public lucee.runtime.net.s3.Properties getS3() {
		if(!initS3) {
			Object o = get(component, KeyConstants._s3, null);
			if(o != null && Decision.isStruct(o))
				s3 = AppListenerUtil.toS3(Caster.toStruct(o, null));
			initS3 = true;
		}
		return s3;
	}

	public FTPConnectionData getFTP() {
		if(!initFTP) {
			Object o = get(component, KeyConstants._ftp, null);
			if(o != null && Decision.isStruct(o))
				ftp = AppListenerUtil.toFTP(Caster.toStruct(o, null));
			initFTP = true;
		}
		return ftp;
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
	public DataSource[] getDataSources() {
		if(!initDataSources) {
			Object o = get(component, KeyConstants._datasources, null);
			// if "this.datasources" does not exists, check if "this.datasource" exists and contains a struct
			/*
			 * if(o==null){ o = get(component,KeyConstants._datasource,null); if(!Decision.isStruct(o)) o=null; }
			 */

			if(o != null)
				dataSources = AppListenerUtil.toDataSources(config, o, dataSources, config.getLog("application"));

			initDataSources = true;
		}
		return dataSources;
	}

	@Override
	public boolean isORMEnabled() {
		return this.ormEnabled;
	}

	@Override
	public String getORMDatasource() {
		throw new PageRuntimeException(new DeprecatedException("this method is no longer supported!"));
	}

	@Override
	public Object getORMDataSource() {
		return ormDatasource;
	}

	@Override
	public ORMConfiguration getORMConfiguration() {
		return ormConfig;
	}

	public Component getComponent() {
		return component;
	}

	public Object getCustom(Key key) {
		try {
			ComponentSpecificAccess cw = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, component);
			return cw.get(key, null);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		return null;
	}

	private static Object get(Component app, Key name, String defaultValue) {
		Member mem = app.getMember(Component.ACCESS_PRIVATE, name, true, false);
		if(mem == null)
			return defaultValue;
		return mem.getValue();
	}

	//////////////////////// SETTERS /////////////////////////

	@Override
	public void setApplicationTimeout(TimeSpan applicationTimeout) {
		initApplicationTimeout = true;
		this.applicationTimeout = applicationTimeout;
	}

	@Override
	public void setSessionTimeout(TimeSpan sessionTimeout) {
		initSessionTimeout = true;
		this.sessionTimeout = sessionTimeout;
	}

	@Override
	public void setClientTimeout(TimeSpan clientTimeout) {
		initClientTimeout = true;
		this.clientTimeout = clientTimeout;
	}

	@Override
	public void setClientstorage(String clientstorage) {
		initClientStorage = true;
		this.clientStorage = clientstorage;
	}

	@Override
	public void setSessionstorage(String sessionstorage) {
		initSessionStorage = true;
		this.sessionStorage = sessionstorage;
	}

	@Override
	public void setCustomTagMappings(Mapping[] customTagMappings) {
		initCTMappings = true;
		this.ctmappings = customTagMappings;
	}

	@Override
	public void setComponentMappings(Mapping[] componentMappings) {
		initCMappings = true;
		this.cmappings = componentMappings;
	}

	@Override
	public void setMappings(Mapping[] mappings) {
		initMappings = true;
		this.mappings = mappings;
	}

	@Override
	public void setMailListener(Object mailListener) {
		initMailListener = true;
		this.mailListener = mailListener;
	}

	@Override
	public void setDataSources(DataSource[] dataSources) {
		initDataSources = true;
		this.dataSources = dataSources;
	}

	@Override
	public void setLoginStorage(int loginStorage) {
		initLoginStorage = true;
		this.loginStorage = loginStorage;
	}

	@Override
	public void setDefaultDataSource(String datasource) {
		this.defaultDataSource = datasource;
	}

	@Override
	public void setDefDataSource(Object datasource) {
		this.defaultDataSource = datasource;
	}

	@Override
	public void setScriptProtect(int scriptrotect) {
		initScriptProtect = true;
		this.scriptProtect = scriptrotect;
	}

	@Override
	public void setTypeChecking(boolean typeChecking) {
		initTypeChecking = true;
		this.typeChecking = typeChecking;
	}

	@Override
	public void setSecureJson(boolean secureJson) {
		initSecureJson = true;
		this.secureJson = secureJson;
	}

	@Override
	public void setSecureJsonPrefix(String secureJsonPrefix) {
		initSecureJsonPrefix = true;
		this.secureJsonPrefix = secureJsonPrefix;
	}

	@Override
	public void setSetClientCookies(boolean setClientCookies) {
		initSetClientCookies = true;
		this.setClientCookies = setClientCookies;
	}

	@Override
	public void setSetClientManagement(boolean setClientManagement) {
		initSetClientManagement = true;
		this.setClientManagement = setClientManagement;
	}

	@Override
	public void setSetDomainCookies(boolean setDomainCookies) {
		initSetDomainCookies = true;
		this.setDomainCookies = setDomainCookies;
	}

	@Override
	public void setSetSessionManagement(boolean setSessionManagement) {
		initSetSessionManagement = true;
		this.setSessionManagement = setSessionManagement;
	}

	@Override
	public void setLocalMode(int localMode) {
		initLocalMode = true;
		this.localMode = localMode;
	}

	@Override
	public void setLocale(Locale locale) {
		initLocale = true;
		this.locale = locale;
	}

	@Override
	public void setTimeZone(TimeZone timeZone) {
		initTimeZone = true;
		this.timeZone = timeZone;
	}

	@Override
	public void setWebCharset(Charset webCharset) {
		initWebCharset = true;
		this.webCharset = CharsetUtil.toCharSet(webCharset);
	}

	@Override
	public void setResourceCharset(Charset resourceCharset) {
		initResourceCharset = true;
		this.resourceCharset = CharsetUtil.toCharSet(resourceCharset);
	}

	@Override
	public void setBufferOutput(boolean bufferOutput) {
		initBufferOutput = true;
		this.bufferOutput = bufferOutput;
	}

	@Override
	public void setSessionType(short sessionType) {
		initSessionType = true;
		this.sessionType = sessionType;
	}

	@Override
	public void setClientCluster(boolean clientCluster) {
		initClientCluster = true;
		this.clientCluster = clientCluster;
	}

	@Override
	public void setSessionCluster(boolean sessionCluster) {
		initSessionCluster = true;
		this.sessionCluster = sessionCluster;
	}

	@Override
	public void setS3(Properties s3) {
		initS3 = true;
		this.s3 = s3;
	}

	public void setFTP(FTPConnectionData ftp) {
		initFTP = true;
		this.ftp = ftp;
	}

	@Override
	public void setORMEnabled(boolean ormEnabled) {
		this.ormEnabled = ormEnabled;
	}

	@Override
	public void setORMConfiguration(ORMConfiguration ormConfig) {
		this.ormConfig = ormConfig;
	}

	@Override
	public void setORMDatasource(String ormDatasource) {
		this.ormDatasource = ormDatasource;
	}

	@Override
	public void setORMDataSource(Object ormDatasource) {
		this.ormDatasource = ormDatasource;
	}

	@Override
	public Resource getSource() {
		return component.getPageSource().getResource();
	}

	@Override
	public RestSettings getRestSettings() {
		initRest();
		return restSetting;
	}

	@Override
	public Resource[] getRestCFCLocations() {
		initRest();
		return restCFCLocations;
	}

	private void initRest() {
		if(!initRestSetting) {
			Object o = get(component, REST_SETTING, null);
			if(o != null && Decision.isStruct(o)) {
				Struct sct = Caster.toStruct(o, null);

				// cfclocation
				Object obj = sct.get(KeyConstants._cfcLocation, null);
				if(obj == null)
					obj = sct.get(KeyConstants._cfcLocations, null);
				List<Resource> list = ORMConfigurationImpl.loadCFCLocation(config, null, obj, true);
				restCFCLocations = list == null ? null : list.toArray(new Resource[list.size()]);

				// skipCFCWithError
				boolean skipCFCWithError = Caster.toBooleanValue(sct.get(KeyConstants._skipCFCWithError, null), restSetting.getSkipCFCWithError());

				// returnFormat
				int returnFormat = Caster.toIntValue(sct.get(KeyConstants._returnFormat, null), restSetting.getReturnFormat());

				restSetting = new RestSettingImpl(skipCFCWithError, returnFormat);

			}
			initRestSetting = true;
		}
	}

	@Override
	public JavaSettings getJavaSettings() {
		initJava();
		return javaSettings;
	}

	private void initJava() {
		if(!initJavaSettings) {
			Object o = get(component, JAVA_SETTING, null);
			if(o != null && Decision.isStruct(o)) {
				Struct sct = Caster.toStruct(o, null);

				// loadPaths
				Object obj = sct.get(KeyImpl.init("loadPaths"), null);
				List<Resource> paths;
				if(obj != null) {
					paths = loadPaths(ThreadLocalPageContext.get(), obj);
				}
				else
					paths = new ArrayList<Resource>();

				// loadCFMLClassPath
				Boolean loadCFMLClassPath = Caster.toBoolean(sct.get(KeyImpl.init("loadCFMLClassPath"), null), null);
				if(loadCFMLClassPath == null)
					loadCFMLClassPath = Caster.toBoolean(sct.get(KeyImpl.init("loadColdFusionClassPath"), null), null);
				if(loadCFMLClassPath == null)
					loadCFMLClassPath = javaSettings.loadCFMLClassPath();

				// reloadOnChange
				boolean reloadOnChange = Caster.toBooleanValue(sct.get(KeyImpl.init("reloadOnChange"), null), javaSettings.reloadOnChange());

				// watchInterval
				int watchInterval = Caster.toIntValue(sct.get(KeyImpl.init("watchInterval"), null), javaSettings.watchInterval());

				// watchExtensions
				obj = sct.get(KeyImpl.init("watchExtensions"), null);
				List<String> extensions = new ArrayList<String>();
				if(obj != null) {
					Array arr;
					if(Decision.isArray(obj)) {
						try {
							arr = Caster.toArray(obj);
						}
						catch (PageException e) {
							arr = new ArrayImpl();
						}
					}
					else {
						arr = lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(Caster.toString(obj, ""), ',');
					}
					Iterator<Object> it = arr.valueIterator();
					String ext;
					while(it.hasNext()) {
						ext = Caster.toString(it.next(), null);
						if(StringUtil.isEmpty(ext))
							continue;
						ext = ext.trim();
						if(ext.startsWith("."))
							ext = ext.substring(1);
						if(ext.startsWith("*."))
							ext = ext.substring(2);
						extensions.add(ext);
					}

				}
				javaSettings = new JavaSettingsImpl(paths.toArray(new Resource[paths.size()]), loadCFMLClassPath, reloadOnChange, watchInterval,
						extensions.toArray(new String[extensions.size()]));

			}
			initJavaSettings = true;
		}
	}

	public static java.util.List<Resource> loadPaths(PageContext pc, Object obj) {

		Resource res;
		if(!Decision.isArray(obj)) {
			String list = Caster.toString(obj, null);
			if(!StringUtil.isEmpty(list)) {
				obj = ListUtil.listToArray(list, ',');
			}
		}

		if(Decision.isArray(obj)) {
			Array arr = Caster.toArray(obj, null);
			java.util.List<Resource> list = new ArrayList<Resource>();
			Iterator<Object> it = arr.valueIterator();
			while(it.hasNext()) {
				try {
					String path = Caster.toString(it.next(), null);
					if(path == null)
						continue;
					// print.e("--------------------------------------------------");
					// print.e(path);
					res = ORMConfigurationImpl.toResourceExisting(pc.getConfig(), pc.getApplicationContext(), path, false);

					// print.e(res+"->"+(res!=null && res.exists()));
					if(res == null || !res.exists())
						res = ResourceUtil.toResourceExisting(pc, path, true, null);

					// print.e(res+"->"+(res!=null && res.exists()));
					if(res != null)
						list.add(res);
				}
				catch (Exception e) {
					SystemOut.printDate(e);
				}
			}
			return list;
		}
		return null;
	}

	public Struct getSerializationSettings(){

		Object oSerialization = get(component, KeyConstants._serialization, null);

		if (oSerialization instanceof Struct)
			return (Struct)oSerialization;

		return new StructImpl();
	}

	@Override
	public Map<Collection.Key, Object> getTagAttributeDefaultValues(PageContext pc, String tagClassName) {
		if(!initDefaultAttributeValues) {
			// this.tag.<tagname>.<attribute-name>=<value>
			Struct sct = Caster.toStruct(get(component, KeyConstants._tag, null), null);
			if(sct != null) {
				setTagAttributeDefaultValues(pc, sct);
			}
			initDefaultAttributeValues = true;
		}
		return super.getTagAttributeDefaultValues(pc, tagClassName);
	}

	@Override
	public void setTagAttributeDefaultValues(PageContext pc, Struct sct) {
		initDefaultAttributeValues = true;
		super.setTagAttributeDefaultValues(pc, sct);
	}

	@Override
	public CustomType getCustomType(String strType) {
		if(!initCustomTypes) {
			if(customTypes == null)
				customTypes = new HashMap<String, CustomType>();

			// this.type.susi=function(any value){};
			Struct sct = Caster.toStruct(get(component, KeyConstants._type, null), null);
			if(sct != null) {
				Iterator<Entry<Key, Object>> it = sct.entryIterator();
				Entry<Key, Object> e;
				UDF udf;
				while(it.hasNext()) {
					e = it.next();
					udf = Caster.toFunction(e.getValue(), null);
					if(udf != null)
						customTypes.put(e.getKey().getLowerString(), new UDFCustomType(udf));
				}
			}
			initCustomTypes = true;
		}
		return customTypes.get(strType.trim().toLowerCase());
	}

	@Override
	public Object getCachedWithin(int type) {
		if(!initCachedWithins) {
			Struct sct = Caster.toStruct(get(component, KeyConstants._cachedWithin, null), null);
			if(sct != null) {
				Iterator<Entry<Key, Object>> it = sct.entryIterator();
				Entry<Key, Object> e;
				Object v;
				int k;
				while(it.hasNext()) {
					e = it.next();
					k = AppListenerUtil.toCachedWithinType(e.getKey().getString(), -1);
					v = e.getValue();
					if(k != -1 && !StringUtil.isEmpty(v))
						setCachedWithin(k, v);
				}
			}
			sct = null;
			// also support this.tag.include... as second chance
			if(super.getCachedWithin(Config.CACHEDWITHIN_INCLUDE) == null) {
				sct = Caster.toStruct(get(component, KeyConstants._tag, null), null);
				if(sct != null) {
					Object obj = sct.get(KeyConstants._include, null);
					if(Decision.isCastableToStruct(obj)) {
						Struct tmp = Caster.toStruct(obj, null);
						obj = tmp == null ? null : tmp.get("cachedWithin", null);
						if(!StringUtil.isEmpty(obj))
							setCachedWithin(Config.CACHEDWITHIN_INCLUDE, obj);
					}
				}
			}

			// also support this.tag.function... as second chance
			if(super.getCachedWithin(Config.CACHEDWITHIN_FUNCTION) == null) {
				if(sct == null)
					sct = Caster.toStruct(get(component, KeyConstants._tag, null), null);
				if(sct != null) {
					Object obj = sct.get(KeyConstants._function, null);
					if(Decision.isCastableToStruct(obj)) {
						Struct tmp = Caster.toStruct(obj, null);
						obj = tmp == null ? null : tmp.get("cachedWithin", null);
						if(!StringUtil.isEmpty(obj))
							setCachedWithin(Config.CACHEDWITHIN_FUNCTION, obj);
					}
				}
			}

			initCachedWithins = true;
		}
		return super.getCachedWithin(type);
	}

	@Override
	public boolean getCGIScopeReadonly() {
		if(!initCGIScopeReadonly) {
			Object o = get(component, CGI_READONLY, null);
			if(o != null)
				cgiScopeReadonly = Caster.toBooleanValue(o, cgiScopeReadonly);
			initCGIScopeReadonly = true;
		}
		return cgiScopeReadonly;
	}

	public void setCGIScopeReadonly(boolean cgiScopeReadonly) {
		initCGIScopeReadonly = true;
		this.cgiScopeReadonly = cgiScopeReadonly;
	}

	@Override
	public SessionCookieData getSessionCookie() {
		if(!initSessionCookie) {
			Struct sct = Caster.toStruct(get(component, SESSION_COOKIE, null), null);
			if(sct != null)
				sessionCookie = AppListenerUtil.toSessionCookie(config, sct);
			initSessionCookie = true;
		}
		return sessionCookie;
	}

	@Override
	public AuthCookieData getAuthCookie() {
		if(!initAuthCookie) {
			Struct sct = Caster.toStruct(get(component, AUTH_COOKIE, null), null);
			if(sct != null)
				authCookie = AppListenerUtil.toAuthCookie(config, sct);
			initAuthCookie = true;
		}
		return authCookie;
	}

	@Override
	public void setSessionCookie(SessionCookieData data) {
		sessionCookie = data;
		initSessionCookie = true;
	}

	@Override
	public void setAuthCookie(AuthCookieData data) {
		authCookie = data;
		initAuthCookie = true;
	}

	@Override
	public void setLoggers(Map<Key, Pair<Log, Struct>> logs) {
		this.logs = logs;
		initLog = true;
	}

	@Override
	public Log getLog(String name) {
		if(!initLog)
			initLog();
		Pair<Log, Struct> pair = logs.get(KeyImpl.init(StringUtil.emptyIfNull(name)));
		if(pair == null)
			return null;
		return pair.getName();
	}

	@Override
	public Struct getLogMetaData(String name) {
		if(!initLog)
			initLog();
		Pair<Log, Struct> pair = logs.get(KeyImpl.init(StringUtil.emptyIfNull(name)));
		if(pair == null)
			return null;
		return (Struct)pair.getValue().duplicate(false);
	}

	@Override
	public java.util.Collection<Collection.Key> getLogNames() {
		if(!initLog)
			initLog();
		return logs.keySet();
	}

	private void initLog() {
		// appender
		Object oLogs = get(component, LOGS, null);
		if(oLogs == null)
			oLogs = get(component, LOG, null);
		Struct sct = Caster.toStruct(oLogs, null);
		logs = initLog(sct);
		initLog = true;
	}

	public static void releaseInitCacheConnections() {
		if(initCacheConnections != null) {
			for (CacheConnection cc : initCacheConnections.values()) {
				CacheUtil.releaseEL(cc);
			}
		}
	}

    @Override
    public Struct getXmlFeatures() {
        if (!initXmlFeatures) {
            Struct sct = Caster.toStruct(get(component, XML_FEATURES, null), null);
            if (sct != null)
                xmlFeatures = sct;
            initXmlFeatures = true;
        }
        return xmlFeatures;
    }

    @Override
    public void setXmlFeatures(Struct xmlFeatures) {
        this.xmlFeatures = xmlFeatures;
    }

}