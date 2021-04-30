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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.jsp.tagext.DynamicAttributes;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.PageSource;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.AuthCookieData;
import lucee.runtime.listener.ClassicApplicationContext;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.listener.ModernApplicationContext;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.listener.SessionCookieData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.regex.Regex;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.tag.listener.TagListener;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.UndefinedImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Defines scoping for a CFML application, enables or disables storing client variables, and
 * specifies a client variable storage mechanism. By default, client variables are disabled. Also,
 * enables session variables and sets timeouts for session and application variables. Session and
 * application variables are stored in memory.
 *
 *
 *
 **/
public final class Application extends TagImpl implements DynamicAttributes {

	private static final int ACTION_CREATE = 0;
	private static final int ACTION_UPDATE = 1;

	private Boolean setClientCookies;
	private Boolean setDomainCookies;
	private Boolean setSessionManagement;
	private String clientstorage;
	private String sessionstorage;
	private Boolean setClientManagement;
	private String cfidStorage;
	private TimeSpan applicationTimeout;
	private TimeSpan sessionTimeout;
	private TimeSpan clientTimeout;
	private TimeSpan requestTimeout;
	private Mapping[] mappings;
	private Mapping[] customTagMappings;
	private Mapping[] componentMappings;
	private String secureJsonPrefix;
	private Boolean bufferOutput;
	private Boolean secureJson;
	private String scriptrotect;
	private Boolean typeChecking;
	private Object datasource;
	private Object defaultdatasource;
	private int loginstorage = Scope.SCOPE_UNDEFINED;

	// ApplicationContextImpl appContext;
	private String name = "";
	private int action = ACTION_CREATE;
	private int localMode = -1;
	private Object mailListener = null;
	private TagListener queryListener = null;
	private SerializationSettings serializationSettings;
	private Locale locale;
	private TimeZone timeZone;
	private Boolean nullSupport;
	private Boolean enableNULLSupport;
	private Boolean queryPSQ;
	private int queryVarUsage;
	private TimeSpan queryCachedAfter;

	private CharSet webCharset;
	private CharSet resourceCharset;
	private short sessionType = -1;
	private short wsType = -1;
	private Boolean sessionCluster;
	private Boolean clientCluster;
	private Boolean compression;

	private Boolean ormenabled;
	private Struct ormsettings;
	private Struct tag;
	private Struct s3;
	private Struct ftp;

	private Boolean triggerDataMember = null;
	private String cacheFunction;
	private String cacheQuery;
	private String cacheTemplate;
	private String cacheInclude;
	private String cacheObject;
	private String cacheResource;
	private String cacheHTTP;
	private String cacheFile;
	private String cacheWebservice;
	private Resource antiSamyPolicyResource;
	private Struct datasources;
	private Struct logs;
	private Array mails;
	private Struct caches;
	private UDF onmissingtemplate;
	private short scopeCascading = -1;
	private Boolean searchQueries = null;
	private Boolean suppress;
	private Boolean cgiReadOnly = null;
	private SessionCookieData sessionCookie;
	private AuthCookieData authCookie;
	private Object functionpaths;
	private Struct proxy;
	private String blockedExtForFileUpload;
	private Struct javaSettings;
	private Struct xmlFeatures;
	private Map<Key, Object> dynAttrs;
	private Regex regex;

	@Override
	public void release() {
		super.release();
		setClientCookies = null;
		setDomainCookies = null;
		setSessionManagement = null;
		clientstorage = null;
		sessionstorage = null;
		cfidStorage = null;
		setClientManagement = null;
		sessionTimeout = null;
		clientTimeout = null;
		requestTimeout = null;
		applicationTimeout = null;
		mappings = null;
		customTagMappings = null;
		componentMappings = null;
		bufferOutput = null;
		secureJson = null;
		secureJsonPrefix = null;
		typeChecking = null;
		suppress = null;
		loginstorage = Scope.SCOPE_UNDEFINED;
		scriptrotect = null;
		functionpaths = null;
		proxy = null;
		datasource = null;
		defaultdatasource = null;
		datasources = null;
		logs = null;
		mails = null;
		caches = null;
		this.name = "";
		action = ACTION_CREATE;
		localMode = -1;
		mailListener = null;
		queryListener = null;
		serializationSettings = null;
		locale = null;
		timeZone = null;
		nullSupport = null;
		enableNULLSupport = null;
		queryPSQ = null;
		queryVarUsage = 0;
		queryCachedAfter = null;
		webCharset = null;
		resourceCharset = null;
		sessionType = -1;
		wsType = -1;
		sessionCluster = null;
		clientCluster = null;
		compression = null;

		ormenabled = null;
		ormsettings = null;
		tag = null;
		s3 = null;
		ftp = null;
		// appContext=null;

		triggerDataMember = null;
		cgiReadOnly = null;

		cacheFunction = null;
		cacheQuery = null;
		cacheTemplate = null;
		cacheObject = null;
		cacheResource = null;
		cacheInclude = null;
		cacheHTTP = null;
		cacheFile = null;
		cacheWebservice = null;
		antiSamyPolicyResource = null;
		onmissingtemplate = null;
		scopeCascading = -1;
		searchQueries = null;
		authCookie = null;
		sessionCookie = null;
		blockedExtForFileUpload = null;
		javaSettings = null;
		xmlFeatures = null;
		dynAttrs = null;
		regex = null;
	}

	@Override
	public void setDynamicAttribute(String uri, String localName, Object value) {
		setDynamicAttribute(uri, KeyImpl.init(localName), value);
	}

	public void setDynamicAttribute(String uri, Key localName, Object value) {
		if (dynAttrs == null) dynAttrs = new HashMap<Key, Object>();
		dynAttrs.put(localName, value);
	}

	/**
	 * set the value setclientcookies Yes or No. Yes enables client cookies. Default is Yes. If you set
	 * this attribute to "No", CFML does not automatically send the CFID and CFTOKEN cookies to the
	 * client browser; you must manually code CFID and CFTOKEN on the URL for every page that uses
	 * Session or Client variables.
	 * 
	 * @param setClientCookies value to set
	 **/
	public void setSetclientcookies(boolean setClientCookies) {
		this.setClientCookies = setClientCookies ? Boolean.TRUE : Boolean.FALSE;
		// getAppContext().setSetClientCookies(setClientCookies);
	}

	/**
	 * set the value setdomaincookies Yes or No. Sets the CFID and CFTOKEN cookies for a domain, not
	 * just a single host. Applications that are running on clusters must set this value to Yes. The
	 * default is No.
	 * 
	 * @param setDomainCookies value to set
	 **/
	public void setSetdomaincookies(boolean setDomainCookies) {
		this.setDomainCookies = setDomainCookies ? Boolean.TRUE : Boolean.FALSE;
		// getAppContext().setSetDomainCookies(setDomainCookies);
	}

	/**
	 * set the value sessionmanagement Yes or No. Yes enables session variables. Default is No.
	 * 
	 * @param setSessionManagement value to set
	 **/
	public void setSessionmanagement(boolean setSessionManagement) {
		this.setSessionManagement = setSessionManagement ? Boolean.TRUE : Boolean.FALSE;
		// getAppContext().setSetSessionManagement(setSessionManagement);
	}

	public void setSessioncookie(Struct data) {
		this.sessionCookie = AppListenerUtil.toSessionCookie(pageContext.getConfig(), data);
	}

	public void setAuthcookie(Struct data) {
		this.authCookie = AppListenerUtil.toAuthCookie(pageContext.getConfig(), data);
	}

	public void setBlockedextforfileupload(String blockedExt) {
		this.blockedExtForFileUpload = blockedExt;
	}

	public void setSearchresults(boolean searchQueries) {
		this.searchQueries = searchQueries;
	}

	/**
	 * @param datasource the datasource to set
	 * @throws PageException
	 */
	public void setDatasource(Object datasource) throws PageException {
		this.datasource = AppListenerUtil.toDefaultDatasource(pageContext.getConfig(), datasource, pageContext.getConfig().getLog("application"));
	}

	public void setDefaultdatasource(Object defaultdatasource) throws PageException {
		this.defaultdatasource = AppListenerUtil.toDefaultDatasource(pageContext.getConfig(), defaultdatasource, pageContext.getConfig().getLog("application"));
	}

	public void setDatasources(Struct datasources) {
		this.datasources = datasources;
	}

	public void setLogs(Struct logs) {
		this.logs = logs;
	}

	public void setMails(Array mails) {
		this.mails = mails;
	}

	public void setCaches(Struct caches) {
		this.caches = caches;
	}

	public void setLocalmode(String strLocalMode) throws ApplicationException {
		this.localMode = AppListenerUtil.toLocalMode(strLocalMode);

	}

	public void setMaillistener(Object mailListener) throws ApplicationException {
		this.mailListener = mailListener;
	}

	public void setQuerylistener(Object listener) throws ApplicationException {
		this.queryListener = Query.toTagListener(listener);
	}

	public void setSerializationsettings(Struct sct) throws ApplicationException {
		if (sct == null) return;
		this.serializationSettings = SerializationSettings.toSerializationSettings(sct);
	}

	public void setTimezone(TimeZone tz) {
		if (tz == null) return;
		this.timeZone = tz;

	}

	public void setNullsupport(boolean nullSupport) {
		this.nullSupport = nullSupport;
	}

	public void setEnablenullsupport(boolean enableNULLSupport) {
		this.enableNULLSupport = enableNULLSupport;
	}

	public void setVariableusage(String varUsage) throws ApplicationException {
		this.queryVarUsage = AppListenerUtil.toVariableUsage(varUsage);
	}

	public void setCachedafter(TimeSpan ts) throws ApplicationException {
		this.queryCachedAfter = ts;
	}

	public void setPsq(boolean psq) {
		this.queryPSQ = psq;
	}

	public void setScopecascading(String scopeCascading) throws ApplicationException {
		if (StringUtil.isEmpty(scopeCascading)) return;
		short NULL = -1;
		short tmp = ConfigWebUtil.toScopeCascading(scopeCascading, NULL);
		if (tmp == NULL) throw new ApplicationException("invalid value (" + scopeCascading + ") for attribute [ScopeCascading], valid values are [strict,small,standard]");
		this.scopeCascading = tmp;
	}

	public void setSearchQueries(boolean searchQueries) throws ApplicationException {
		this.searchQueries = searchQueries;
	}

	public void setSearchimplicitscopes(boolean searchImplicitScopes) throws ApplicationException {
		short tmp = ConfigWebUtil.toScopeCascading(searchImplicitScopes);
		this.scopeCascading = tmp;
	}

	public void setWebcharset(String charset) {
		if (StringUtil.isEmpty(charset)) return;
		webCharset = CharsetUtil.toCharSet(charset);

	}

	public void setResourcecharset(String charset) {
		if (StringUtil.isEmpty(charset)) return;
		resourceCharset = CharsetUtil.toCharSet(charset);

	}

	public void setLocale(Locale locale) {
		if (locale == null) return;
		this.locale = locale;

	}

	/**
	 * set the value clientstorage Specifies how the engine stores client variables
	 * 
	 * @param clientstorage value to set
	 **/
	public void setClientstorage(String clientstorage) {
		this.clientstorage = clientstorage;
	}

	public void setSessionstorage(String sessionstorage) {
		System.out.println("sessionStorage----->>>" + sessionstorage);
		this.sessionstorage = sessionstorage;
	}

	public void setCfidStorage(String cfidStorage) {
		this.cfidStorage = cfidStorage;
	}

	/**
	 * set the value clientmanagement Yes or No. Enables client variables. Default is No.
	 * 
	 * @param setClientManagement value to set
	 **/
	public void setClientmanagement(boolean setClientManagement) {
		this.setClientManagement = setClientManagement ? Boolean.TRUE : Boolean.FALSE;
		// getAppContext().setSetClientManagement(setClientManagement);
	}

	/**
	 * set the value sessiontimeout Enter the CreateTimeSpan function and values in days, hours,
	 * minutes, and seconds, separated by commas, to specify the lifespan of session variables.
	 * 
	 * @param sessionTimeout value to set
	 **/
	public void setSessiontimeout(TimeSpan sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public void setSessiontype(String sessionType) throws ApplicationException {
		this.sessionType = AppListenerUtil.toSessionType(sessionType);
	}

	public void setWstype(String wstype) throws ApplicationException {
		this.wsType = AppListenerUtil.toWSType(wstype);
	}

	public void setClientcluster(boolean clientCluster) {
		this.clientCluster = clientCluster;
	}

	public void setSessioncluster(boolean sessionCluster) {
		this.sessionCluster = sessionCluster;
	}

	public void setClienttimeout(TimeSpan clientTimeout) {
		this.clientTimeout = clientTimeout;
	}

	public void setRequesttimeout(TimeSpan requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public void setCachefunction(String cacheFunction) {
		if (StringUtil.isEmpty(cacheFunction, true)) return;
		this.cacheFunction = cacheFunction.trim();
	}

	public void setCachequery(String cacheQuery) {
		if (StringUtil.isEmpty(cacheQuery, true)) return;
		this.cacheQuery = cacheQuery.trim();
	}

	public void setCachetemplate(String cacheTemplate) {
		if (StringUtil.isEmpty(cacheTemplate, true)) return;
		this.cacheTemplate = cacheTemplate.trim();
	}

	public void setCacheinclude(String cacheInclude) {
		if (StringUtil.isEmpty(cacheInclude, true)) return;
		this.cacheInclude = cacheInclude.trim();
	}

	public void setCacheobject(String cacheObject) {
		if (StringUtil.isEmpty(cacheObject, true)) return;
		this.cacheObject = cacheObject.trim();
	}

	public void setCacheresource(String cacheResource) {
		if (StringUtil.isEmpty(cacheResource, true)) return;
		this.cacheResource = cacheResource.trim();
	}

	public void setCachehttp(String cacheHTTP) {
		if (StringUtil.isEmpty(cacheHTTP, true)) return;
		this.cacheHTTP = cacheHTTP.trim();
	}

	public void setCachefile(String cacheFile) {
		if (StringUtil.isEmpty(cacheFile, true)) return;
		this.cacheFile = cacheFile.trim();
	}

	public void setCachewebservice(String cacheWebservice) {
		if (StringUtil.isEmpty(cacheWebservice, true)) return;
		this.cacheWebservice = cacheWebservice.trim();
	}

	public void setCompression(boolean compress) {
		this.compression = compress;
	}

	public void setAntiSamyPolicyResource(String strAntiSamyPolicyResource) throws ExpressionException {
		this.antiSamyPolicyResource = ResourceUtil.toResourceExisting(pageContext, strAntiSamyPolicyResource);
	}

	public void setTriggerdatamember(boolean triggerDataMember) {
		this.triggerDataMember = triggerDataMember ? Boolean.TRUE : Boolean.FALSE;
	}

	public void setInvokeimplicitaccessor(boolean invokeimplicitaccessor) {
		setTriggerdatamember(invokeimplicitaccessor);
	}

	/**
	 * @param ormenabled the ormenabled to set
	 */
	public void setOrmenabled(boolean ormenabled) {
		this.ormenabled = ormenabled;
	}

	/**
	 * @param ormsettings the ormsettings to set
	 */
	public void setOrmsettings(Struct ormsettings) {
		this.ormsettings = ormsettings;
	}

	public void setTag(Struct tag) {
		this.tag = tag;
	}

	/**
	 * @param s3 the s3 to set
	 */
	public void setS3(Struct s3) {
		this.s3 = s3;
	}

	/**
	 * @param s3 the s3 to set
	 */
	public void setFtp(Struct ftp) {
		this.ftp = ftp;
	}

	/**
	 * set the value applicationtimeout Enter the CreateTimeSpan function and values in days, hours,
	 * minutes, and seconds, separated by commas, to specify the lifespan of application variables.
	 * 
	 * @param applicationTimeout value to set
	 **/
	public void setApplicationtimeout(TimeSpan applicationTimeout) {
		this.applicationTimeout = applicationTimeout;
		// getAppContext().setApplicationTimeout(applicationTimeout);
	}

	/**
	 * set the value name The name of your application. This name can be up to 64 characters long.
	 * Required for application and session variables, optional for client variables
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	public void setAction(String strAction) throws ApplicationException {
		strAction = strAction.toLowerCase();
		if (strAction.equals("create")) action = ACTION_CREATE;
		else if (strAction.equals("update")) action = ACTION_UPDATE;
		else throw new ApplicationException("invalid action definition [" + strAction + "] for tag application, valid values are [create,update]");

	}

	public void setMappings(Struct mappings) throws PageException {
		this.mappings = AppListenerUtil.toMappings(pageContext.getConfig(), mappings, getSource());
		// getAppContext().setMappings(AppListenerUtil.toMappings(pageContext, mappings));
	}

	public void setCustomtagpaths(Object mappings) throws PageException {
		this.customTagMappings = AppListenerUtil.toCustomTagMappings(pageContext.getConfig(), mappings, getSource());
	}

	public void setComponentpaths(Object mappings) throws PageException {
		this.componentMappings = AppListenerUtil.toComponentMappings(pageContext.getConfig(), mappings, getSource());
	}

	public void setFunctionpaths(Object functionpaths) {
		this.functionpaths = functionpaths;
	}

	public void setJavasettings(Struct javaSettings) {
		this.javaSettings = javaSettings;
	}

	public void setSecurejsonprefix(String secureJsonPrefix) {
		this.secureJsonPrefix = secureJsonPrefix;
		// getAppContext().setSecureJsonPrefix(secureJsonPrefix);
	}

	public void setSecurejson(boolean secureJson) {
		this.secureJson = secureJson ? Boolean.TRUE : Boolean.FALSE;
		// getAppContext().setSecureJson(secureJson);
	}

	public void setBufferoutput(boolean bufferOutput) {
		this.bufferOutput = bufferOutput ? Boolean.TRUE : Boolean.FALSE;
		// getAppContext().setSecureJson(secureJson);
	}

	/**
	 * @param loginstorage The loginstorage to set.
	 * @throws ApplicationException
	 */
	public void setLoginstorage(String loginstorage) throws ApplicationException {
		loginstorage = loginstorage.toLowerCase();
		if (loginstorage.equals("session")) this.loginstorage = Scope.SCOPE_SESSION;
		else if (loginstorage.equals("cookie")) this.loginstorage = Scope.SCOPE_COOKIE;
		else throw new ApplicationException("invalid loginStorage definition [" + loginstorage + "] for tag application, valid values are [session,cookie]");
	}

	/**
	 * @param scriptrotect the scriptrotect to set
	 */
	public void setScriptprotect(String strScriptrotect) {
		this.scriptrotect = strScriptrotect;
	}

	public void setProxy(Struct proxy) {
		this.proxy = proxy;
	}

	public void setTypechecking(boolean typeChecking) {
		this.typeChecking = typeChecking;
	}

	public void setSuppressremotecomponentcontent(boolean suppress) {
		this.suppress = suppress;
	}

	public void setOnmissingtemplate(Object oUDF) throws PageException {
		this.onmissingtemplate = Caster.toFunction(oUDF);
	}

	public void setCgireadonly(boolean cgiReadOnly) {
		this.cgiReadOnly = cgiReadOnly;
	}

	public void setXmlfeatures(Struct xmlFeatures) {
		this.xmlFeatures = xmlFeatures;
	}

	public void setRegex(Object data) throws PageException {
		if (Decision.isSimpleValue(data)) {
			regex = RegexFactory.toRegex(RegexFactory.toType(Caster.toString(data)), null);
		}
		else {
			Struct sct = Caster.toStruct(data);
			Object o = sct.get(KeyConstants._type, null);
			if (o == null) o = sct.get("engine", null);
			if (o == null) o = sct.get("dialect", null);
			if (o != null) {
				regex = RegexFactory.toRegex(RegexFactory.toType(Caster.toString(o)), null);
			}
		}
	}

	@Override
	public int doStartTag() throws PageException {

		ApplicationContext ac = null;
		boolean initORM = false;

		if (action == ACTION_UPDATE) {
			ac = pageContext.getApplicationContext();
			// no update because the current context has a different name
			if (!StringUtil.isEmpty(name) && !name.equalsIgnoreCase(ac.getName())) ac = null;
			else {
				initORM = set(ac, true);
				pageContext.setApplicationContext(ac); // we need to make this, so Lucee does not miss any change
			}
		}
		// if we do not update we have to create a new one
		if (ac == null) {
			PageSource ps = pageContext.getCurrentPageSource(null);
			ac = new ClassicApplicationContext(pageContext.getConfig(), name, false, ps == null ? null : ps.getResourceTranslated(pageContext));
			initORM = set(ac, false);
			pageContext.setApplicationContext(ac);
		}

		// scope cascading
		if (((UndefinedImpl) pageContext.undefinedScope()).getScopeCascadingType() != ac.getScopeCascading()) {
			pageContext.undefinedScope().initialize(pageContext);
		}

		// ORM
		if (initORM) ORMUtil.resetEngine(pageContext, false);

		return SKIP_BODY;
	}

	private Resource getSource() throws PageException {
		PageSource curr = pageContext.getCurrentPageSource();
		if (curr == null) return null;
		return ResourceUtil.getResource(pageContext, curr);
	}

	private boolean set(ApplicationContext ac, boolean update) throws PageException {
		if (dynAttrs != null && ac instanceof ClassicApplicationContext) {
			ClassicApplicationContext cac = (ClassicApplicationContext) ac;
			cac.setCustomAttributes(dynAttrs);
			dynAttrs = null;
		}

		if (applicationTimeout != null) ac.setApplicationTimeout(applicationTimeout);
		if (sessionTimeout != null) ac.setSessionTimeout(sessionTimeout);
		if (clientTimeout != null) ac.setClientTimeout(clientTimeout);
		if (requestTimeout != null) ac.setRequestTimeout(requestTimeout);
		if (cfidStorage != null) ac.setCfidstorage(cfidStorage);
		if (clientstorage != null) {
			ac.setClientstorage(clientstorage);
		}
		System.out.println("set, before if----->>>" + sessionstorage);
		if (sessionstorage != null) {
			System.out.println("set, inside if----->>>" + sessionstorage);
			ac.setSessionstorage(sessionstorage);
			System.out.println("set, inside if--- ac.getCfidstorage() -->>>" + ac.getCfidstorage());
		}
		if (customTagMappings != null) ac.setCustomTagMappings(customTagMappings);
		if (componentMappings != null) ac.setComponentMappings(componentMappings);
		if (mappings != null) ac.setMappings(mappings);
		if (loginstorage != Scope.SCOPE_UNDEFINED) ac.setLoginStorage(loginstorage);
		if (!StringUtil.isEmpty(datasource)) {
			ac.setDefDataSource(datasource);
			ac.setORMDataSource(datasource);
		}
		if (!StringUtil.isEmpty(defaultdatasource)) ac.setDefDataSource(defaultdatasource);
		if (datasources != null) {
			try {
				ac.setDataSources(AppListenerUtil.toDataSources(pageContext.getConfig(), datasources, pageContext.getConfig().getLog("application")));
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		if (logs != null) {
			try {
				ApplicationContextSupport acs = (ApplicationContextSupport) ac;
				acs.setLoggers(ApplicationContextSupport.initLog(pageContext.getConfig(), logs));
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		if (mails != null) {
			ApplicationContextSupport acs = (ApplicationContextSupport) ac;
			try {
				acs.setMailServers(AppListenerUtil.toMailServers(pageContext.getConfig(), mails, null));
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		if (caches != null) {
			try {
				ApplicationContextSupport acs = (ApplicationContextSupport) ac;
				Iterator<Entry<Key, Object>> it = caches.entryIterator();
				Entry<Key, Object> e;
				String name;
				Struct sct;
				while (it.hasNext()) {
					e = it.next();
					// default value by name
					if (!StringUtil.isEmpty(name = Caster.toString(e.getValue(), null))) {
						setDefault(ac, e.getKey(), name);
					}
					// cache definition
					else if ((sct = Caster.toStruct(e.getValue(), null)) != null) {
						CacheConnection cc = ModernApplicationContext.toCacheConnection(pageContext.getConfig(), e.getKey().getString(), sct);
						if (cc != null) {
							name = e.getKey().getString();
							acs.setCacheConnection(name, cc);

							// key is a cache type
							setDefault(ac, e.getKey(), name);

							// default key
							Key def = Caster.toKey(sct.get(KeyConstants._default, null), null);
							if (def != null) setDefault(ac, def, name);

						}
					}
				}
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}

		if (onmissingtemplate != null && ac instanceof ClassicApplicationContext) {
			((ClassicApplicationContext) ac).setOnMissingTemplate(onmissingtemplate);
		}

		ApplicationContextSupport acs = (ApplicationContextSupport) ac;

		if (scriptrotect != null) ac.setScriptProtect(AppListenerUtil.translateScriptProtect(scriptrotect));
		if (functionpaths != null) acs.setFunctionDirectories(AppListenerUtil.loadResources(pageContext.getConfig(), ac, functionpaths, true));
		if (proxy != null) acs.setProxyData(ProxyDataImpl.toProxyData(proxy));
		if (bufferOutput != null) ac.setBufferOutput(bufferOutput.booleanValue());
		if (secureJson != null) ac.setSecureJson(secureJson.booleanValue());
		if (typeChecking != null) ac.setTypeChecking(typeChecking.booleanValue());
		if (suppress != null) ac.setSuppressContent(suppress.booleanValue());
		if (secureJsonPrefix != null) ac.setSecureJsonPrefix(secureJsonPrefix);
		if (setClientCookies != null) ac.setSetClientCookies(setClientCookies.booleanValue());
		if (setClientManagement != null) ac.setSetClientManagement(setClientManagement.booleanValue());
		if (setDomainCookies != null) ac.setSetDomainCookies(setDomainCookies.booleanValue());
		if (setSessionManagement != null) ac.setSetSessionManagement(setSessionManagement.booleanValue());
		if (localMode != -1) ac.setLocalMode(localMode);
		if (mailListener != null) ((ApplicationContextSupport) ac).setMailListener(mailListener);
		if (queryListener != null) ((ApplicationContextSupport) ac).setQueryListener(queryListener);
		if (serializationSettings != null) ((ApplicationContextSupport) ac).setSerializationSettings(serializationSettings);
		if (locale != null) ac.setLocale(locale);
		if (timeZone != null) ac.setTimeZone(timeZone);
		if (nullSupport != null) ((ApplicationContextSupport) ac).setFullNullSupport(nullSupport);
		if (enableNULLSupport != null) ((ApplicationContextSupport) ac).setFullNullSupport(enableNULLSupport);
		if (queryPSQ != null) ((ApplicationContextSupport) ac).setQueryPSQ(queryPSQ);
		if (queryVarUsage != 0) ((ApplicationContextSupport) ac).setQueryVarUsage(queryVarUsage);
		if (queryCachedAfter != null) ((ApplicationContextSupport) ac).setQueryCachedAfter(queryCachedAfter);
		if (webCharset != null) ac.setWebCharset(webCharset.toCharset());
		if (resourceCharset != null) ac.setResourceCharset(resourceCharset.toCharset());
		if (sessionType != -1) ac.setSessionType(sessionType);
		if (wsType != -1) ac.setWSType(wsType);
		if (triggerDataMember != null) ac.setTriggerComponentDataMember(triggerDataMember.booleanValue());
		if (compression != null) ac.setAllowCompression(compression.booleanValue());
		if (cacheFunction != null) ac.setDefaultCacheName(Config.CACHE_TYPE_FUNCTION, cacheFunction);
		if (cacheObject != null) ac.setDefaultCacheName(Config.CACHE_TYPE_OBJECT, cacheObject);
		if (cacheQuery != null) ac.setDefaultCacheName(Config.CACHE_TYPE_QUERY, cacheQuery);
		if (cacheResource != null) ac.setDefaultCacheName(Config.CACHE_TYPE_RESOURCE, cacheResource);
		if (cacheTemplate != null) ac.setDefaultCacheName(Config.CACHE_TYPE_TEMPLATE, cacheTemplate);
		if (cacheInclude != null) ac.setDefaultCacheName(Config.CACHE_TYPE_INCLUDE, cacheInclude);
		if (cacheHTTP != null) ac.setDefaultCacheName(Config.CACHE_TYPE_HTTP, cacheHTTP);
		if (cacheFile != null) ac.setDefaultCacheName(Config.CACHE_TYPE_FILE, cacheFile);
		if (cacheWebservice != null) ac.setDefaultCacheName(Config.CACHE_TYPE_WEBSERVICE, cacheWebservice);
		if (antiSamyPolicyResource != null) ((ApplicationContextSupport) ac).setAntiSamyPolicyResource(antiSamyPolicyResource);
		if (sessionCookie != null) acs.setSessionCookie(sessionCookie);
		if (authCookie != null) acs.setAuthCookie(authCookie);
		if (tag != null) ac.setTagAttributeDefaultValues(pageContext, tag);
		if (clientCluster != null) ac.setClientCluster(clientCluster.booleanValue());
		if (sessionCluster != null) ac.setSessionCluster(sessionCluster.booleanValue());
		if (cgiReadOnly != null) ac.setCGIScopeReadonly(cgiReadOnly.booleanValue());
		if (s3 != null) ac.setS3(AppListenerUtil.toS3(s3));
		if (ftp != null) ((ApplicationContextSupport) ac).setFTP(AppListenerUtil.toFTP(ftp));

		// Scope cascading
		if (scopeCascading != -1) ac.setScopeCascading(scopeCascading);
		if (blockedExtForFileUpload != null) {
			if (ac instanceof ClassicApplicationContext) {
				((ClassicApplicationContext) ac).setBlockedextforfileupload(blockedExtForFileUpload);
			}
		}

		if (ac instanceof ApplicationContextSupport) {
			ApplicationContextSupport appContextSup = ((ApplicationContextSupport) ac);

			if (javaSettings != null) appContextSup.setJavaSettings(JavaSettingsImpl.newInstance(new JavaSettingsImpl(), javaSettings));
			if (xmlFeatures != null) appContextSup.setXmlFeatures(xmlFeatures);
			if (searchQueries != null) appContextSup.setAllowImplicidQueryCall(searchQueries.booleanValue());
			if (regex != null) appContextSup.setRegex(regex);
		}

		// ORM
		boolean initORM = false;
		if (!update) {
			if (ormenabled == null) ormenabled = false;
			if (ormsettings == null) ormsettings = new StructImpl();
		}
		if (ormenabled != null) ac.setORMEnabled(ormenabled);
		if (ac.isORMEnabled()) {
			initORM = true;
			if (ormsettings != null) AppListenerUtil.setORMConfiguration(pageContext, ac, ormsettings);
		}
		return initORM;
	}

	private static void setDefault(ApplicationContext ac, Key type, String cacheName) {
		if (KeyConstants._function.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_FUNCTION, cacheName);
		else if (KeyConstants._object.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_OBJECT, cacheName);
		else if (KeyConstants._query.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_QUERY, cacheName);
		else if (KeyConstants._resource.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_RESOURCE, cacheName);
		else if (KeyConstants._template.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_TEMPLATE, cacheName);
		else if (KeyConstants._include.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_INCLUDE, cacheName);
		else if (KeyConstants._http.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_HTTP, cacheName);
		else if (KeyConstants._file.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_FILE, cacheName);
		else if (KeyConstants._webservice.equals(type)) ac.setDefaultCacheName(Config.CACHE_TYPE_WEBSERVICE, cacheName);
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}