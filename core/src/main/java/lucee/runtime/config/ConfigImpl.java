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

import static lucee.runtime.db.DatasourceManagerImpl.QOQ_DATASOURCE_NAME;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogEngine;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.LoggerAndSourceData;
import lucee.commons.io.log.log4j.layout.ClassicLayout;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.ResourcesImpl.ResourceProviderFactory;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.type.compress.Compress;
import lucee.commons.io.res.type.compress.CompressResource;
import lucee.commons.io.res.type.compress.CompressResourceProvider;
import lucee.commons.io.res.util.ResourceClassLoader;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.IPRange;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CIPage;
import lucee.runtime.Component;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.ram.RamCache;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.cfx.customtag.CFXTagPoolImpl;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.component.ImportDefintionImpl;
import lucee.runtime.customtag.InitFile;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnectionPool;
import lucee.runtime.db.JDBCDriver;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.dump.DumpWriterEntry;
import lucee.runtime.dump.HTMLDumpWriter;
import lucee.runtime.engine.ExecutionLogFactory;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.extension.Extension;
import lucee.runtime.extension.ExtensionProvider;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.functions.system.ContractPath;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.ntp.NtpClient;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.EnvClassLoader;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.regex.Regex;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.rest.RestSettingImpl;
import lucee.runtime.rest.RestSettings;
import lucee.runtime.schedule.Scheduler;
import lucee.runtime.schedule.SchedulerImpl;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.spooler.SpoolerEngine;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.ClusterNotSupported;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.video.VideoExecuterNotSupported;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.function.FunctionLibFactory;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibException;
import lucee.transformer.library.tag.TagLibFactory;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagAttr;
import lucee.transformer.library.tag.TagLibTagScript;

/**
 * Hold the definitions of the Lucee configuration.
 */
public abstract class ConfigImpl extends ConfigBase implements ConfigPro {

	private static final Extension[] EXTENSIONS_EMPTY = new Extension[0];
	private static final RHExtension[] RHEXTENSIONS_EMPTY = new RHExtension[0];

	private int mode = MODE_CUSTOM;

	private PhysicalClassLoader rpcClassLoader;
	private Map<String, DataSource> datasources = new HashMap<String, DataSource>();
	private Map<String, CacheConnection> caches = new HashMap<String, CacheConnection>();

	private CacheConnection defaultCacheFunction = null;
	private CacheConnection defaultCacheObject = null;
	private CacheConnection defaultCacheTemplate = null;
	private CacheConnection defaultCacheQuery = null;
	private CacheConnection defaultCacheResource = null;
	private CacheConnection defaultCacheInclude = null;
	private CacheConnection defaultCacheHTTP = null;
	private CacheConnection defaultCacheFile = null;
	private CacheConnection defaultCacheWebservice = null;

	private String cacheDefaultConnectionNameFunction = null;
	private String cacheDefaultConnectionNameObject = null;
	private String cacheDefaultConnectionNameTemplate = null;
	private String cacheDefaultConnectionNameQuery = null;
	private String cacheDefaultConnectionNameResource = null;
	private String cacheDefaultConnectionNameInclude = null;
	private String cacheDefaultConnectionNameHTTP = null;
	private String cacheDefaultConnectionNameFile = null;
	private String cacheDefaultConnectionNameWebservice = null;

	private TagLib[] cfmlTlds = new TagLib[0];
	private TagLib[] luceeTlds = new TagLib[0];

	private FunctionLib[] cfmlFlds = new FunctionLib[0];
	private FunctionLib[] luceeFlds = new FunctionLib[0];

	private FunctionLib combinedCFMLFLDs;
	private FunctionLib combinedLuceeFLDs;

	private short type = SCOPE_STANDARD;
	private boolean _allowImplicidQueryCall = true;
	private boolean _mergeFormAndURL = false;

	private Map<String, LoggerAndSourceData> loggers = new HashMap<String, LoggerAndSourceData>();

	private int _debug;
	private int debugLogOutput = SERVER_BOOLEAN_FALSE;
	private int debugOptions = 0;

	private boolean suppresswhitespace = false;
	private boolean suppressContent = false;
	private boolean showVersion = false;

	private Resource tempDirectory;
	private TimeSpan clientTimeout = new TimeSpanImpl(90, 0, 0, 0);
	private TimeSpan sessionTimeout = new TimeSpanImpl(0, 0, 30, 0);
	private TimeSpan applicationTimeout = new TimeSpanImpl(1, 0, 0, 0);
	private TimeSpan requestTimeout = new TimeSpanImpl(0, 0, 0, 30);

	private boolean sessionManagement = true;
	private boolean clientManagement = false;
	private boolean clientCookies = true;
	private boolean developMode = false;
	private boolean domainCookies = false;

	private Resource configFile;
	private Resource configDir;
	private String sessionStorage = DEFAULT_STORAGE_SESSION;
	private String clientStorage = DEFAULT_STORAGE_CLIENT;

	private long loadTime;

	private int spoolInterval = 30;
	private boolean spoolEnable = true;
	private boolean sendPartial = false;
	private boolean userSet = true;

	private Server[] mailServers;

	private int mailTimeout = 30;

	private TimeZone timeZone;

	private String timeServer = "";
	private boolean useTimeServer = true;

	private long timeOffset;

	private ClassDefinition<SearchEngine> searchEngineClassDef;
	private String searchEngineDirectory;

	private Locale locale;

	private boolean psq = false;
	private boolean debugShowUsage;

	private Map<String, String> errorTemplates = new HashMap<String, String>();

	protected Password password;
	private String salt;

	private Mapping[] mappings = new Mapping[0];
	private Mapping[] customTagMappings = new Mapping[0];
	private Mapping[] componentMappings = new Mapping[0];

	private SchedulerImpl scheduler;

	private CFXTagPool cfxTagPool;

	private PageSource baseComponentPageSourceCFML;
	private String baseComponentTemplateCFML;
	private PageSource baseComponentPageSourceLucee;
	private String baseComponentTemplateLucee;
	private boolean restList = false;

	private short clientType = CLIENT_SCOPE_TYPE_COOKIE;

	private String componentDumpTemplate;
	private int componentDataMemberDefaultAccess = Component.ACCESS_PRIVATE;
	private boolean triggerComponentDataMember = false;

	private short sessionType = SESSION_TYPE_APPLICATION;

	private Resource deployDirectory;

	private short compileType = RECOMPILE_NEVER;

	private CharSet resourceCharset = SystemUtil.getCharSet();
	private CharSet templateCharset = SystemUtil.getCharSet();
	private CharSet webCharset = CharSet.UTF8;

	private CharSet mailDefaultCharset = CharSet.UTF8;

	private Resource tldFile;
	private Resource fldFile;

	private Resources resources = new ResourcesImpl();
	private Map<String, Class<CacheHandler>> cacheHandlerClasses = new HashMap<String, Class<CacheHandler>>();

	private ApplicationListener applicationListener;

	private int scriptProtect = ApplicationContext.SCRIPT_PROTECT_ALL;

	private ProxyData proxy = null;

	private Resource clientScopeDir;
	private Resource sessionScopeDir;
	private long clientScopeDirSize = 1024 * 1024 * 10;
	private long sessionScopeDirSize = 1024 * 1024 * 10;

	private Resource cacheDir;
	private long cacheDirSize = 1024 * 1024 * 10;

	private boolean useComponentShadow = true;

	private PrintWriter out = SystemUtil.getPrintWriter(SystemUtil.OUT);
	private PrintWriter err = SystemUtil.getPrintWriter(SystemUtil.ERR);

	private DatasourceConnectionPool pool = new DatasourceConnectionPool();

	private boolean doCustomTagDeepSearch = false;
	private boolean doComponentTagDeepSearch = false;

	private double version = 1.0D;

	private boolean closeConnection = false;
	private boolean contentLength = true;
	private boolean allowCompression = false;

	private boolean doLocalCustomTag = true;

	private Struct constants = null;

	private RemoteClient[] remoteClients;

	private SpoolerEngine remoteClientSpoolerEngine;

	private Resource remoteClientDirectory;

	private boolean allowURLRequestTimeout = false;
	private boolean errorStatusCode = true;
	private int localMode = Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS;

	private RHExtensionProvider[] rhextensionProviders = Constants.RH_EXTENSION_PROVIDERS;

	private Extension[] extensions = EXTENSIONS_EMPTY;
	private RHExtension[] rhextensions = RHEXTENSIONS_EMPTY;
	private boolean extensionEnabled;
	private boolean allowRealPath = true;

	private DumpWriterEntry[] dmpWriterEntries;
	private Class clusterClass = ClusterNotSupported.class;// ClusterRemoteNotSupported.class;//
	private Struct remoteClientUsage;
	private Class adminSyncClass = AdminSyncNotSupported.class;
	private AdminSync adminSync;
	private String[] customTagExtensions = Constants.getExtensions();
	private Class videoExecuterClass = VideoExecuterNotSupported.class;

	protected MappingImpl scriptMapping;

	// private Resource tagDirectory;
	protected Mapping defaultFunctionMapping;
	protected Map<String, Mapping> functionMappings = new ConcurrentHashMap<String, Mapping>();

	protected Mapping defaultTagMapping;
	protected Map<String, Mapping> tagMappings = new ConcurrentHashMap<String, Mapping>();

	private short inspectTemplate = INSPECT_ONCE;
	private boolean typeChecking = true;
	private String cacheMD5;
	private boolean executionLogEnabled;
	private ExecutionLogFactory executionLogFactory;
	private Map<String, ORMEngine> ormengines = new HashMap<String, ORMEngine>();
	private ClassDefinition<? extends ORMEngine> cdORMEngine;
	private ORMConfiguration ormConfig;
	private ResourceClassLoader resourceCL;

	private ImportDefintion componentDefaultImport = new ImportDefintionImpl(Constants.DEFAULT_PACKAGE, "*");
	private boolean componentLocalSearch = true;
	private boolean componentRootSearch = true;
	private boolean useComponentPathCache = true;
	private boolean useCTPathCache = true;
	private lucee.runtime.rest.Mapping[] restMappings;

	protected int writerType = CFML_WRITER_REFULAR;
	private long configFileLastModified;
	private boolean checkForChangesInConfigFile;
	// protected String apiKey=null;

	private List consoleLayouts = new ArrayList();
	private List resourceLayouts = new ArrayList();

	private Map<Key, Map<Key, Object>> tagDefaultAttributeValues;
	private boolean handleUnQuotedAttrValueAsString = true;

	private Map<Integer, Object> cachedWithins = new HashMap<Integer, Object>();

	private int queueMax = 100;
	private long queueTimeout = 0;
	private boolean queueEnable = false;
	private int varUsage;

	private TimeSpan cachedAfterTimeRange;

	private static Map<String, Startup> startups;

	private Regex regex; // TODO add possibility to configure

	/**
	 * @return the allowURLRequestTimeout
	 */
	@Override
	public boolean isAllowURLRequestTimeout() {
		return allowURLRequestTimeout;
	}

	/**
	 * @param allowURLRequestTimeout the allowURLRequestTimeout to set
	 */
	public void setAllowURLRequestTimeout(boolean allowURLRequestTimeout) {
		this.allowURLRequestTimeout = allowURLRequestTimeout;
	}

	@Override
	public short getCompileType() {
		return compileType;
	}

	@Override
	public void reset() {
		timeServer = "";
		componentDumpTemplate = "";
		// resources.reset();
		ormengines.clear();
		compressResources.clear();
		clearFunctionCache();
		clearCTCache();
		clearComponentCache();
		// clearComponentMetadata();
	}

	@Override
	public void reloadTimeServerOffset() {
		timeOffset = 0;
		if (useTimeServer && !StringUtil.isEmpty(timeServer, true)) {
			NtpClient ntp = new NtpClient(timeServer);
			timeOffset = ntp.getOffset(0);
		}
	}

	/**
	 * private constructor called by factory method
	 * 
	 * @param configDir - config directory
	 * @param configFile - config file
	 */
	protected ConfigImpl(Resource configDir, Resource configFile) {
		this.configDir = configDir;
		this.configFile = configFile;
	}

	@Override
	public long lastModified() {
		return configFileLastModified;
	}

	protected void setLastModified() {
		this.configFileLastModified = configFile.lastModified();
	}

	@Override
	public short getScopeCascadingType() {
		return type;
	}
	/*
	 * @Override public String[] getCFMLExtensions() { return getAllExtensions(); }
	 * 
	 * @Override public String getCFCExtension() { return getComponentExtension(); }
	 * 
	 * @Override public String[] getAllExtensions() { return Constants.ALL_EXTENSION; }
	 * 
	 * @Override public String getComponentExtension() { return Constants.COMPONENT_EXTENSION; }
	 * 
	 * @Override public String[] getTemplateExtensions() { return Constants.TEMPLATE_EXTENSIONS; }
	 */

	protected void setFLDs(FunctionLib[] flds, int dialect) {
		if (dialect == CFMLEngine.DIALECT_CFML) {
			cfmlFlds = flds;
			combinedCFMLFLDs = null; // TODO improve check (hash)
		}
		else {
			luceeFlds = flds;
			combinedLuceeFLDs = null; // TODO improve check (hash)
		}
	}

	/**
	 * return all Function Library Deskriptors
	 * 
	 * @return Array of Function Library Deskriptors
	 */
	@Override
	public FunctionLib[] getFLDs(int dialect) {
		return dialect == CFMLEngine.DIALECT_CFML ? cfmlFlds : luceeFlds;
	}

	@Override
	public FunctionLib getCombinedFLDs(int dialect) {
		if (dialect == CFMLEngine.DIALECT_CFML) {
			if (combinedCFMLFLDs == null) combinedCFMLFLDs = FunctionLibFactory.combineFLDs(cfmlFlds);
			return combinedCFMLFLDs;
		}

		if (combinedLuceeFLDs == null) combinedLuceeFLDs = FunctionLibFactory.combineFLDs(luceeFlds);
		return combinedLuceeFLDs;
	}

	/**
	 * return all Tag Library Deskriptors
	 * 
	 * @return Array of Tag Library Deskriptors
	 */
	@Override
	public TagLib[] getTLDs(int dialect) {
		return dialect == CFMLEngine.DIALECT_CFML ? cfmlTlds : luceeTlds;
	}

	protected void setTLDs(TagLib[] tlds, int dialect) {
		if (dialect == CFMLEngine.DIALECT_CFML) cfmlTlds = tlds;
		else luceeTlds = tlds;
	}

	@Override
	public boolean allowImplicidQueryCall() {
		return _allowImplicidQueryCall;
	}

	@Override
	public boolean mergeFormAndURL() {
		return _mergeFormAndURL;
	}

	@Override
	public TimeSpan getApplicationTimeout() {
		return applicationTimeout;
	}

	@Override
	public TimeSpan getSessionTimeout() {
		return sessionTimeout;
	}

	@Override
	public TimeSpan getClientTimeout() {
		return clientTimeout;
	}

	@Override
	public TimeSpan getRequestTimeout() {
		return requestTimeout;
	}

	@Override
	public boolean isClientCookies() {
		return clientCookies;
	}

	@Override
	public boolean isDevelopMode() {
		return developMode;
	}

	@Override
	public boolean isClientManagement() {
		return clientManagement;
	}

	@Override
	public boolean isDomainCookies() {
		return domainCookies;
	}

	@Override
	public boolean isSessionManagement() {
		return sessionManagement;
	}

	@Override
	public boolean isMailSpoolEnable() {
		return spoolEnable;
	}

	// FUTURE add to interface
	@Override
	public boolean isMailSendPartial() {
		return sendPartial;
	}

	// FUTURE add to interface and impl
	@Override
	public boolean isUserset() {
		return userSet;
	}

	@Override
	public Server[] getMailServers() {
		if (mailServers == null) mailServers = new Server[0];
		return mailServers;
	}

	@Override
	public int getMailTimeout() {
		return mailTimeout;
	}

	@Override
	public boolean getPSQL() {
		return psq;
	}

	protected void setQueryVarUsage(int varUsage) {
		this.varUsage = varUsage;
	}

	@Override
	public int getQueryVarUsage() {
		return varUsage;
	}

	@Override
	public ClassLoader getClassLoader() {
		ResourceClassLoader rcl = getResourceClassLoader(null);
		if (rcl != null) return rcl;
		return new lucee.commons.lang.ClassLoaderHelper().getClass().getClassLoader();

	}

	// do not remove, ised in Hibernate extension
	@Override
	public ClassLoader getClassLoaderEnv() {
		return new EnvClassLoader(this);
	}

	@Override
	public ClassLoader getClassLoaderCore() {
		return new lucee.commons.lang.ClassLoaderHelper().getClass().getClassLoader();
	}
	/*
	 * public ClassLoader getClassLoaderLoader() { return new TP().getClass().getClassLoader(); }
	 */

	@Override
	public ResourceClassLoader getResourceClassLoader() {
		if (resourceCL == null) throw new RuntimeException("no RCL defined yet!");
		return resourceCL;
	}

	@Override
	public ResourceClassLoader getResourceClassLoader(ResourceClassLoader defaultValue) {
		if (resourceCL == null) return defaultValue;
		return resourceCL;
	}

	protected void setResourceClassLoader(ResourceClassLoader resourceCL) {
		this.resourceCL = resourceCL;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public boolean debug() {
		if (!(_debug == CLIENT_BOOLEAN_TRUE || _debug == SERVER_BOOLEAN_TRUE)) return false;
		return true;
	}

	@Override
	public boolean debugLogOutput() {
		return debug() && debugLogOutput == CLIENT_BOOLEAN_TRUE || debugLogOutput == SERVER_BOOLEAN_TRUE;
	}

	@Override
	public Resource getTempDirectory() {
		if (tempDirectory == null) {
			Resource tmp = SystemUtil.getTempDirectory();
			if (!tmp.exists()) tmp.mkdirs();
			return tmp;
		}
		if (!tempDirectory.exists()) tempDirectory.mkdirs();
		return tempDirectory;
	}

	@Override
	public int getMailSpoolInterval() {
		return spoolInterval;
	}

	@Override
	public TimeZone getTimeZone() {
		return timeZone;
	}

	@Override
	public long getTimeServerOffset() {
		return timeOffset;
	}

	/**
	 * @return return the Scheduler
	 */
	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * @return gets the password as hash
	 */
	protected Password getPassword() {
		return password;
	}

	@Override
	public Password isPasswordEqual(String password) {
		if (this.password == null) return null;
		return ((PasswordImpl) this.password).isEqual(this, password);
	}

	@Override
	public boolean hasPassword() {
		return password != null;
	}

	@Override
	public boolean passwordEqual(Password password) {
		if (this.password == null) return false;
		return this.password.equals(password);
	}

	@Override
	public Mapping[] getMappings() {
		return mappings;
	}

	@Override
	public lucee.runtime.rest.Mapping[] getRestMappings() {
		if (restMappings == null) restMappings = new lucee.runtime.rest.Mapping[0];
		return restMappings;
	}

	protected void setRestMappings(lucee.runtime.rest.Mapping[] restMappings) {

		// make sure only one is default
		boolean hasDefault = false;
		lucee.runtime.rest.Mapping m;
		for (int i = 0; i < restMappings.length; i++) {
			m = restMappings[i];
			if (m.isDefault()) {
				if (hasDefault) m.setDefault(false);
				hasDefault = true;
			}
		}

		this.restMappings = restMappings;
	}

	@Override
	public PageSource getPageSource(Mapping[] mappings, String realPath, boolean onlyTopLevel) {
		throw new PageRuntimeException(new DeprecatedException("method not supported"));
	}

	@Override
	public PageSource getPageSourceExisting(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean onlyPhysicalExisting) {
		realPath = realPath.replace('\\', '/');
		String lcRealPath = StringUtil.toLowerCase(realPath) + '/';
		Mapping mapping;
		PageSource ps;
		Mapping rootApp = null;
		if (mappings != null) {
			for (int i = 0; i < mappings.length; i++) {
				mapping = mappings[i];
				// we keep this for later
				if ("/".equals(mapping.getVirtual())) {
					rootApp = mapping;
					continue;
				}
				if (lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(), 0)) {
					ps = mapping.getPageSource(realPath.substring(mapping.getVirtual().length()));
					if (onlyPhysicalExisting) {
						if (ps.physcalExists()) return ps;
					}
					else if (ps.exists()) return ps;
				}
			}
		}

		/// special mappings
		if (useSpecialMappings && lcRealPath.startsWith("/mapping-", 0)) {
			String virtual = "/mapping-tag";
			// tag mappings
			Mapping[] tagMappings = (this instanceof ConfigWebPro) ? new Mapping[] { ((ConfigWebPro) this).getDefaultServerTagMapping(), getDefaultTagMapping() }
					: new Mapping[] { getDefaultTagMapping() };
			if (lcRealPath.startsWith(virtual, 0)) {
				for (int i = 0; i < tagMappings.length; i++) {
					mapping = tagMappings[i];
					// if(lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(),0)) {
					ps = mapping.getPageSource(realPath.substring(virtual.length()));
					if (onlyPhysicalExisting) {
						if (ps.physcalExists()) return ps;
					}
					else if (ps.exists()) return ps;
					// }
				}
			}

			// customtag mappings
			tagMappings = getCustomTagMappings();
			virtual = "/mapping-customtag";
			if (lcRealPath.startsWith(virtual, 0)) {
				for (int i = 0; i < tagMappings.length; i++) {
					mapping = tagMappings[i];
					// if(lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(),0)) {
					ps = mapping.getPageSource(realPath.substring(virtual.length()));
					if (onlyPhysicalExisting) {
						if (ps.physcalExists()) return ps;
					}
					else if (ps.exists()) return ps;
					// }
				}
			}
		}

		// component mappings (only used for gateway)
		if (pc != null && ((PageContextImpl) pc).isGatewayContext()) {
			boolean isCFC = Constants.isComponentExtension(ResourceUtil.getExtension(realPath, null));
			if (isCFC) {
				Mapping[] cmappings = getComponentMappings();
				for (int i = 0; i < cmappings.length; i++) {
					ps = cmappings[i].getPageSource(realPath);
					if (onlyPhysicalExisting) {
						if (ps.physcalExists()) return ps;
					}
					else if (ps.exists()) return ps;
				}
			}
		}

		// config mappings
		for (int i = 0; i < this.mappings.length - 1; i++) {
			mapping = this.mappings[i];
			if ((!onlyTopLevel || mapping.isTopLevel()) && lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(), 0)) {
				ps = mapping.getPageSource(realPath.substring(mapping.getVirtual().length()));
				if (onlyPhysicalExisting) {
					if (ps.physcalExists()) return ps;
				}
				else if (ps.exists()) return ps;
			}
		}

		if (useDefaultMapping) {
			if (rootApp != null) mapping = rootApp;
			else mapping = this.mappings[this.mappings.length - 1];

			ps = mapping.getPageSource(realPath);
			if (onlyPhysicalExisting) {
				if (ps.physcalExists()) return ps;
			}
			else if (ps.exists()) return ps;
		}
		return null;
	}

	@Override
	public PageSource[] getPageSources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping) {
		return getPageSources(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, false, onlyFirstMatch);
	}

	@Override
	public PageSource[] getPageSources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean useComponentMappings) {
		return getPageSources(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, useComponentMappings, onlyFirstMatch);
	}

	public PageSource[] getPageSources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean useComponentMappings, boolean onlyFirstMatch) {
		realPath = realPath.replace('\\', '/');
		String lcRealPath = StringUtil.toLowerCase(realPath) + '/';
		Mapping mapping;
		Mapping rootApp = null;
		PageSource ps;
		List<PageSource> list = new ArrayList<PageSource>();

		if (mappings != null) {
			for (int i = 0; i < mappings.length; i++) {
				mapping = mappings[i];
				// we keep this for later
				if ("/".equals(mapping.getVirtual())) {
					rootApp = mapping;
					continue;
				}
				// print.err(lcRealPath+".startsWith"+(mapping.getStrPhysical()));
				if (lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(), 0)) {
					ps = mapping.getPageSource(realPath.substring(mapping.getVirtual().length()));
					if (onlyFirstMatch) return new PageSource[] { ps };
					else list.add(ps);
				}
			}
		}

		/// special mappings
		if (useSpecialMappings && lcRealPath.startsWith("/mapping-", 0)) {
			String virtual = "/mapping-tag";
			// tag mappings
			Mapping[] tagMappings = (this instanceof ConfigWebPro) ? new Mapping[] { ((ConfigWebPro) this).getDefaultServerTagMapping(), getDefaultTagMapping() }
					: new Mapping[] { getDefaultTagMapping() };
			if (lcRealPath.startsWith(virtual, 0)) {
				for (int i = 0; i < tagMappings.length; i++) {
					ps = tagMappings[i].getPageSource(realPath.substring(virtual.length()));
					if (ps.exists()) {
						if (onlyFirstMatch) return new PageSource[] { ps };
						else list.add(ps);
					}
				}
			}

			// customtag mappings
			tagMappings = getCustomTagMappings();
			virtual = "/mapping-customtag";
			if (lcRealPath.startsWith(virtual, 0)) {
				for (int i = 0; i < tagMappings.length; i++) {
					ps = tagMappings[i].getPageSource(realPath.substring(virtual.length()));
					if (ps.exists()) {
						if (onlyFirstMatch) return new PageSource[] { ps };
						else list.add(ps);
					}
				}
			}
		}

		// component mappings (only used for gateway)
		if (useComponentMappings || (pc != null && ((PageContextImpl) pc).isGatewayContext())) {
			boolean isCFC = Constants.isComponentExtension(ResourceUtil.getExtension(realPath, null));
			if (isCFC) {
				Mapping[] cmappings = getComponentMappings();
				for (int i = 0; i < cmappings.length; i++) {
					ps = cmappings[i].getPageSource(realPath);
					if (ps.exists()) {
						if (onlyFirstMatch) return new PageSource[] { ps };
						else list.add(ps);
					}
				}
			}
		}

		// config mappings
		for (int i = 0; i < this.mappings.length - 1; i++) {
			mapping = this.mappings[i];
			if ((!onlyTopLevel || mapping.isTopLevel()) && lcRealPath.startsWith(mapping.getVirtualLowerCaseWithSlash(), 0)) {
				ps = mapping.getPageSource(realPath.substring(mapping.getVirtual().length()));
				if (onlyFirstMatch) return new PageSource[] { ps };
				else list.add(ps);
			}
		}

		if (useDefaultMapping) {
			if (rootApp != null) mapping = rootApp;
			else mapping = this.mappings[this.mappings.length - 1];
			ps = mapping.getPageSource(realPath);
			if (onlyFirstMatch) return new PageSource[] { ps };
			else list.add(ps);
		}
		return list.toArray(new PageSource[list.size()]);
	}

	/**
	 * @param mappings
	 * @param realPath
	 * @param alsoDefaultMapping ignore default mapping (/) or not
	 * @return physical path from mapping
	 */
	@Override
	public Resource getPhysical(Mapping[] mappings, String realPath, boolean alsoDefaultMapping) {
		throw new PageRuntimeException(new DeprecatedException("method not supported"));
	}

	@Override
	public Resource[] getPhysicalResources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping) {
		// now that archives can be used the same way as physical resources, there is no need anymore to
		// limit to that
		throw new PageRuntimeException(new DeprecatedException("method not supported"));
	}

	@Override
	public Resource getPhysicalResourceExisting(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping) {
		// now that archives can be used the same way as physical resources, there is no need anymore to
		// limit to that
		throw new PageRuntimeException(new DeprecatedException("method not supported"));
	}

	@Override
	public PageSource toPageSource(Mapping[] mappings, Resource res, PageSource defaultValue) {
		Mapping mapping;
		String path;

		// app mappings
		if (mappings != null) {
			for (int i = 0; i < mappings.length; i++) {
				mapping = mappings[i];

				// Physical
				if (mapping.hasPhysical()) {
					path = ResourceUtil.getPathToChild(res, mapping.getPhysical());
					if (path != null) {
						return mapping.getPageSource(path);
					}
				}
				// Archive
				if (mapping.hasArchive() && res.getResourceProvider() instanceof CompressResourceProvider) {
					Resource archive = mapping.getArchive();
					CompressResource cr = ((CompressResource) res);
					if (archive.equals(cr.getCompressResource())) {
						return mapping.getPageSource(cr.getCompressPath());
					}
				}
			}
		}

		// config mappings
		for (int i = 0; i < this.mappings.length; i++) {
			mapping = this.mappings[i];

			// Physical
			if (mapping.hasPhysical()) {
				path = ResourceUtil.getPathToChild(res, mapping.getPhysical());
				if (path != null) {
					return mapping.getPageSource(path);
				}
			}
			// Archive
			if (mapping.hasArchive() && res.getResourceProvider() instanceof CompressResourceProvider) {
				Resource archive = mapping.getArchive();
				CompressResource cr = ((CompressResource) res);
				if (archive.equals(cr.getCompressResource())) {
					return mapping.getPageSource(cr.getCompressPath());
				}
			}
		}

		// map resource to root mapping when same filesystem
		Mapping rootMapping = this.mappings[this.mappings.length - 1];
		Resource root;
		if (rootMapping.hasPhysical() && res.getResourceProvider().getScheme().equals((root = rootMapping.getPhysical()).getResourceProvider().getScheme())) {

			String realpath = "";
			while (root != null && !ResourceUtil.isChildOf(res, root)) {
				root = root.getParentResource();
				realpath += "../";
			}
			String p2c = ResourceUtil.getPathToChild(res, root);
			if (StringUtil.startsWith(p2c, '/') || StringUtil.startsWith(p2c, '\\')) p2c = p2c.substring(1);
			realpath += p2c;

			return rootMapping.getPageSource(realpath);

		}
		// MUST better impl than this
		if (this instanceof ConfigWebPro) {
			Resource parent = res.getParentResource();
			if (parent != null && !parent.equals(res)) {
				Mapping m = ((ConfigWebPro) this).getApplicationMapping("application", "/", parent.getAbsolutePath(), null, true, false);
				return m.getPageSource(res.getName());
			}
		}

		// Archive
		// MUST check archive
		return defaultValue;
	}

	@Override
	public Resource getConfigDir() {
		return configDir;
	}

	@Override
	public Resource getConfigFile() {
		return configFile;
	}

	/**
	 * sets the password
	 * 
	 * @param password
	 */
	protected void setPassword(Password password) {
		this.password = password;
	}

	/**
	 * set how lucee cascade scopes
	 * 
	 * @param type cascading type
	 */
	protected void setScopeCascadingType(short type) {
		this.type = type;
	}

	protected void addTag(String nameSpace, String nameSpaceSeperator, String name, int dialect, ClassDefinition cd) {
		if (dialect == CFMLEngine.DIALECT_BOTH) {
			addTag(nameSpace, nameSpaceSeperator, name, CFMLEngine.DIALECT_CFML, cd);
			addTag(nameSpace, nameSpaceSeperator, name, CFMLEngine.DIALECT_LUCEE, cd);
			return;
		}

		TagLib[] tlds = dialect == CFMLEngine.DIALECT_CFML ? cfmlTlds : luceeTlds;

		for (int i = 0; i < tlds.length; i++) {
			if (tlds[i].getNameSpaceAndSeparator().equalsIgnoreCase(nameSpace + nameSpaceSeperator)) {
				TagLibTag tlt = new TagLibTag(tlds[i]);
				tlt.setAttributeType(TagLibTag.ATTRIBUTE_TYPE_DYNAMIC);
				tlt.setBodyContent("free");
				tlt.setTagClassDefinition(cd);
				tlt.setName(name);
				tlds[i].setTag(tlt);
			}
		}
	}

	/**
	 * set the optional directory of the tag library deskriptors
	 * 
	 * @param fileTld directory of the tag libray deskriptors
	 * @throws TagLibException
	 */
	protected void setTldFile(Resource fileTld, int dialect) throws TagLibException {
		if (dialect == CFMLEngine.DIALECT_BOTH) {
			setTldFile(fileTld, CFMLEngine.DIALECT_CFML);
			setTldFile(fileTld, CFMLEngine.DIALECT_LUCEE);
			return;
		}

		TagLib[] tlds = dialect == CFMLEngine.DIALECT_CFML ? cfmlTlds : luceeTlds;

		if (fileTld == null) return;
		this.tldFile = fileTld;
		String key;
		Map<String, TagLib> map = new HashMap<String, TagLib>();
		// First fill existing to set
		for (int i = 0; i < tlds.length; i++) {
			key = getKey(tlds[i]);
			map.put(key, tlds[i]);
		}

		TagLib tl;

		// now overwrite with new data
		if (fileTld.isDirectory()) {
			Resource[] files = fileTld.listResources(new ExtensionResourceFilter(new String[] { "tld", "tldx" }));
			for (int i = 0; i < files.length; i++) {
				try {
					tl = TagLibFactory.loadFromFile(files[i], getIdentification());
					key = getKey(tl);
					if (!map.containsKey(key)) map.put(key, tl);
					else overwrite(map.get(key), tl);
				}
				catch (TagLibException tle) {
					LogUtil.log(this, Log.LEVEL_ERROR, "loading", "can't load tld " + files[i]);
					tle.printStackTrace(getErrWriter());
				}

			}
		}
		else if (fileTld.isFile()) {
			tl = TagLibFactory.loadFromFile(fileTld, getIdentification());
			key = getKey(tl);
			if (!map.containsKey(key)) map.put(key, tl);
			else overwrite(map.get(key), tl);
		}

		// now fill back to array
		tlds = new TagLib[map.size()];
		if (dialect == CFMLEngine.DIALECT_CFML) cfmlTlds = tlds;
		else luceeTlds = tlds;

		int index = 0;
		Iterator<TagLib> it = map.values().iterator();
		while (it.hasNext()) {
			tlds[index++] = it.next();
		}
	}

	@Override
	public TagLib getCoreTagLib(int dialect) {
		TagLib[] tlds = dialect == CFMLEngine.DIALECT_CFML ? cfmlTlds : luceeTlds;

		for (int i = 0; i < tlds.length; i++) {
			if (tlds[i].isCore()) return tlds[i];
		}
		throw new RuntimeException("no core taglib found"); // this should never happen
	}

	protected void setTagDirectory(List<Resource> listTagDirectory) {
		Iterator<Resource> it = listTagDirectory.iterator();
		int index = -1;
		String mappingName;
		Resource tagDirectory;
		Mapping m;
		boolean isDefault;
		while (it.hasNext()) {
			tagDirectory = it.next();
			index++;
			isDefault = index == 0;
			mappingName = "/mapping-tag" + (isDefault ? "" : index) + "";

			m = new MappingImpl(this, mappingName, tagDirectory.getAbsolutePath(), null, ConfigPro.INSPECT_NEVER, true, true, true, true, false, true, null, -1, -1);
			if (isDefault) defaultTagMapping = m;
			tagMappings.put(mappingName, m);

			TagLib tlc = getCoreTagLib(CFMLEngine.DIALECT_CFML);
			TagLib tll = getCoreTagLib(CFMLEngine.DIALECT_LUCEE);

			// now overwrite with new data
			if (tagDirectory.isDirectory()) {
				String[] files = tagDirectory
						.list(new ExtensionResourceFilter(getMode() == ConfigPro.MODE_STRICT ? Constants.getComponentExtensions() : Constants.getExtensions()));
				for (int i = 0; i < files.length; i++) {
					if (tlc != null) createTag(tlc, files[i], mappingName);
					if (tll != null) createTag(tll, files[i], mappingName);
				}
			}
		}
	}

	public void createTag(TagLib tl, String filename, String mappingName) {// Jira 1298
		String name = toName(filename);// filename.substring(0,filename.length()-(getCFCExtension().length()+1));

		TagLibTag tlt = new TagLibTag(tl);
		tlt.setName(name);
		tlt.setTagClassDefinition("lucee.runtime.tag.CFTagCore", getIdentification(), null);
		tlt.setHandleExceptions(true);
		tlt.setBodyContent("free");
		tlt.setParseBody(false);
		tlt.setDescription("");
		tlt.setAttributeType(TagLibTag.ATTRIBUTE_TYPE_MIXED);

		// read component and read setting from that component
		TagLibTagScript tlts = new TagLibTagScript(tlt);
		tlts.setType(TagLibTagScript.TYPE_MULTIPLE);
		tlt.setScript(tlts);

		TagLibTagAttr tlta = new TagLibTagAttr(tlt);
		tlta.setName("__filename");
		tlta.setRequired(true);
		tlta.setRtexpr(true);
		tlta.setType("string");
		tlta.setHidden(true);
		tlta.setDefaultValue(filename);
		tlt.setAttribute(tlta);

		tlta = new TagLibTagAttr(tlt);
		tlta.setName("__name");
		tlta.setRequired(true);
		tlta.setRtexpr(true);
		tlta.setHidden(true);
		tlta.setType("string");
		tlta.setDefaultValue(name);
		tlt.setAttribute(tlta);

		tlta = new TagLibTagAttr(tlt);
		tlta.setName("__isweb");
		tlta.setRequired(true);
		tlta.setRtexpr(true);
		tlta.setHidden(true);
		tlta.setType("boolean");
		tlta.setDefaultValue(this instanceof ConfigWeb ? "true" : "false");
		tlt.setAttribute(tlta);

		tlta = new TagLibTagAttr(tlt);
		tlta.setName("__mapping");
		tlta.setRequired(true);
		tlta.setRtexpr(true);
		tlta.setHidden(true);
		tlta.setType("string");
		tlta.setDefaultValue(mappingName);
		tlt.setAttribute(tlta);

		tl.setTag(tlt);
	}

	protected void setFunctionDirectory(List<Resource> listFunctionDirectory) {
		Iterator<Resource> it = listFunctionDirectory.iterator();
		int index = -1;
		String mappingName;
		Resource functionDirectory;
		boolean isDefault;
		while (it.hasNext()) {
			functionDirectory = it.next();
			index++;
			isDefault = index == 0;
			mappingName = "/mapping-function" + (isDefault ? "" : index) + "";
			MappingImpl mapping = new MappingImpl(this, mappingName, functionDirectory.getAbsolutePath(), null, ConfigPro.INSPECT_NEVER, true, true, true, true, false, true, null,
					-1, -1);
			if (isDefault) defaultFunctionMapping = mapping;
			this.functionMappings.put(mappingName, mapping);

			FunctionLib flc = cfmlFlds[cfmlFlds.length - 1];
			FunctionLib fll = luceeFlds[luceeFlds.length - 1];

			// now overwrite with new data
			if (functionDirectory.isDirectory()) {
				String[] files = functionDirectory.list(new ExtensionResourceFilter(Constants.getTemplateExtensions()));

				for (String file: files) {
					if (flc != null) createFunction(flc, file, mappingName);
					if (fll != null) createFunction(fll, file, mappingName);
				}
				combinedCFMLFLDs = null;
				combinedLuceeFLDs = null;
			}
		}
	}

	public void createFunction(FunctionLib fl, String filename, String mapping) {
		String name = toName(filename);// filename.substring(0,filename.length()-(getCFMLExtensions().length()+1));
		FunctionLibFunction flf = new FunctionLibFunction(fl, true);
		flf.setArgType(FunctionLibFunction.ARG_DYNAMIC);
		flf.setFunctionClass("lucee.runtime.functions.system.CFFunction", null, null);
		flf.setName(name);
		flf.setReturn("object");

		FunctionLibFunctionArg arg = new FunctionLibFunctionArg(flf);
		arg.setName("__filename");
		arg.setRequired(true);
		arg.setType("string");
		arg.setHidden(true);
		arg.setDefaultValue(filename);
		flf.setArg(arg);

		arg = new FunctionLibFunctionArg(flf);
		arg.setName("__name");
		arg.setRequired(true);
		arg.setHidden(true);
		arg.setType("string");
		arg.setDefaultValue(name);
		flf.setArg(arg);

		arg = new FunctionLibFunctionArg(flf);
		arg.setName("__isweb");
		arg.setRequired(true);
		arg.setHidden(true);
		arg.setType("boolean");
		arg.setDefaultValue(this instanceof ConfigWeb ? "true" : "false");
		flf.setArg(arg);

		arg = new FunctionLibFunctionArg(flf);
		arg.setName("__mapping");
		arg.setRequired(true);
		arg.setHidden(true);
		arg.setType("string");
		arg.setDefaultValue(mapping);
		flf.setArg(arg);

		fl.setFunction(flf);
	}

	private static String toName(String filename) {
		int pos = filename.lastIndexOf('.');
		if (pos == -1) return filename;
		return filename.substring(0, pos);
	}

	private void overwrite(TagLib existingTL, TagLib newTL) {
		Iterator<TagLibTag> it = newTL.getTags().values().iterator();
		while (it.hasNext()) {
			existingTL.setTag(it.next());
		}
	}

	private String getKey(TagLib tl) {
		return tl.getNameSpaceAndSeparator().toLowerCase();
	}

	protected void setFldFile(Resource fileFld, int dialect) throws FunctionLibException {
		if (dialect == CFMLEngine.DIALECT_BOTH) {
			setFldFile(fileFld, CFMLEngine.DIALECT_CFML);
			setFldFile(fileFld, CFMLEngine.DIALECT_LUCEE);
			return;
		}

		FunctionLib[] flds = dialect == CFMLEngine.DIALECT_CFML ? cfmlFlds : luceeFlds;

		// merge all together (backward compatibility)
		if (flds.length > 1) for (int i = 1; i < flds.length; i++) {
			overwrite(flds[0], flds[i]);
		}
		flds = new FunctionLib[] { flds[0] };
		if (dialect == CFMLEngine.DIALECT_CFML) {
			cfmlFlds = flds;
			if (cfmlFlds != flds) combinedCFMLFLDs = null;// TODO improve check
		}
		else {
			luceeFlds = flds;
			if (luceeFlds != flds) combinedLuceeFLDs = null;// TODO improve check
		}

		if (fileFld == null) return;
		this.fldFile = fileFld;

		// overwrite with additional functions
		FunctionLib fl;
		if (fileFld.isDirectory()) {
			Resource[] files = fileFld.listResources(new ExtensionResourceFilter(new String[] { "fld", "fldx" }));
			for (int i = 0; i < files.length; i++) {
				try {
					fl = FunctionLibFactory.loadFromFile(files[i], getIdentification());

					overwrite(flds[0], fl);

				}
				catch (FunctionLibException fle) {
					LogUtil.log(this, Log.LEVEL_ERROR, "loading", "can't load fld " + files[i]);
					fle.printStackTrace(getErrWriter());
				}
			}
		}
		else {
			fl = FunctionLibFactory.loadFromFile(fileFld, getIdentification());
			overwrite(flds[0], fl);
		}
	}

	private void overwrite(FunctionLib existingFL, FunctionLib newFL) {
		Iterator<FunctionLibFunction> it = newFL.getFunctions().values().iterator();
		while (it.hasNext()) {
			existingFL.setFunction(it.next());
		}
	}

	private String getKey(FunctionLib functionLib) {
		return functionLib.getDisplayName().toLowerCase();
	}

	/**
	 * sets if it is allowed to implict query call, call a query member without define name of the
	 * query.
	 * 
	 * @param _allowImplicidQueryCall is allowed
	 */
	protected void setAllowImplicidQueryCall(boolean _allowImplicidQueryCall) {
		this._allowImplicidQueryCall = _allowImplicidQueryCall;
	}

	/**
	 * sets if url and form scope will be merged
	 * 
	 * @param _mergeFormAndURL merge yes or no
	 */
	protected void setMergeFormAndURL(boolean _mergeFormAndURL) {
		this._mergeFormAndURL = _mergeFormAndURL;
	}

	/**
	 * @param strApplicationTimeout The applicationTimeout to set.
	 * @throws PageException
	 */
	void setApplicationTimeout(String strApplicationTimeout) throws PageException {
		setApplicationTimeout(Caster.toTimespan(strApplicationTimeout));
	}

	/**
	 * @param applicationTimeout The applicationTimeout to set.
	 */
	protected void setApplicationTimeout(TimeSpan applicationTimeout) {
		this.applicationTimeout = applicationTimeout;
	}

	/**
	 * @param strSessionTimeout The sessionTimeout to set.
	 * @throws PageException
	 */
	protected void setSessionTimeout(String strSessionTimeout) throws PageException {
		setSessionTimeout(Caster.toTimespan(strSessionTimeout));
	}

	/**
	 * @param sessionTimeout The sessionTimeout to set.
	 */
	protected void setSessionTimeout(TimeSpan sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	protected void setClientTimeout(String strClientTimeout) throws PageException {
		setClientTimeout(Caster.toTimespan(strClientTimeout));
	}

	/**
	 * @param clientTimeout The sessionTimeout to set.
	 */
	protected void setClientTimeout(TimeSpan clientTimeout) {
		this.clientTimeout = clientTimeout;
	}

	/**
	 * @param strRequestTimeout The requestTimeout to set.
	 * @throws PageException
	 */
	protected void setRequestTimeout(String strRequestTimeout) throws PageException {
		setRequestTimeout(Caster.toTimespan(strRequestTimeout));
	}

	/**
	 * @param requestTimeout The requestTimeout to set.
	 */
	protected void setRequestTimeout(TimeSpan requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	/**
	 * @param clientCookies The clientCookies to set.
	 */
	protected void setClientCookies(boolean clientCookies) {
		this.clientCookies = clientCookies;
	}

	/**
	 * @param developMode
	 */
	protected void setDevelopMode(boolean developMode) {
		this.developMode = developMode;
	}

	/**
	 * @param clientManagement The clientManagement to set.
	 */
	protected void setClientManagement(boolean clientManagement) {
		this.clientManagement = clientManagement;
	}

	/**
	 * @param domainCookies The domainCookies to set.
	 */
	protected void setDomainCookies(boolean domainCookies) {
		this.domainCookies = domainCookies;
	}

	/**
	 * @param sessionManagement The sessionManagement to set.
	 */
	protected void setSessionManagement(boolean sessionManagement) {
		this.sessionManagement = sessionManagement;
	}

	/**
	 * @param spoolEnable The spoolEnable to set.
	 */
	protected void setMailSpoolEnable(boolean spoolEnable) {
		this.spoolEnable = spoolEnable;
	}

	protected void setMailSendPartial(boolean sendPartial) {
		this.sendPartial = sendPartial;
	}

	protected void setUserSet(boolean userSet) {
		this.userSet = userSet;
	}

	/**
	 * @param mailTimeout The mailTimeout to set.
	 */
	protected void setMailTimeout(int mailTimeout) {
		this.mailTimeout = mailTimeout;
	}

	/**
	 * @param psq (preserve single quote) sets if sql string inside a cfquery will be preserved for
	 *            Single Quotes
	 */
	protected void setPSQL(boolean psq) {
		this.psq = psq;
	}

	/**
	 * set if lucee make debug output or not
	 * 
	 * @param _debug debug or not
	 */
	protected void setDebug(int _debug) {
		this._debug = _debug;
	}

	protected void setDebugLogOutput(int debugLogOutput) {
		this.debugLogOutput = debugLogOutput;
	}

	/**
	 * sets the temp directory
	 * 
	 * @param strTempDirectory temp directory
	 * @throws ExpressionException
	 */
	protected void setTempDirectory(String strTempDirectory, boolean flush) throws ExpressionException {
		setTempDirectory(resources.getResource(strTempDirectory), flush);
	}

	/**
	 * sets the temp directory
	 * 
	 * @param tempDirectory temp directory
	 * @throws ExpressionException
	 */
	protected void setTempDirectory(Resource tempDirectory, boolean flush) throws ExpressionException {
		if (!isDirectory(tempDirectory) || !tempDirectory.isWriteable()) {
			LogUtil.log(this, Log.LEVEL_ERROR, "loading",
					"temp directory [" + tempDirectory + "] is not writable or can not be created, using directory [" + SystemUtil.getTempDirectory() + "] instead");

			tempDirectory = SystemUtil.getTempDirectory();
			if (!tempDirectory.isWriteable()) {
				LogUtil.log(this, Log.LEVEL_ERROR, "loading", "temp directory [" + tempDirectory + "] is not writable");
			}
		}
		if (flush) ResourceUtil.removeChildrenEL(tempDirectory);// start with an empty temp directory
		this.tempDirectory = tempDirectory;
	}

	/**
	 * sets the Schedule Directory
	 * 
	 * @param scheduleDirectory sets the schedule Directory
	 * @param logger
	 * @throws PageException
	 */
	protected void setScheduler(CFMLEngine engine, Array scheduledTasks) throws PageException {
		if (scheduledTasks == null) {
			if (this.scheduler == null) this.scheduler = new SchedulerImpl(engine, this, new ArrayImpl());
			return;
		}

		try {
			if (this.scheduler == null) this.scheduler = new SchedulerImpl(engine, this, scheduledTasks);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * @param spoolInterval The spoolInterval to set.
	 */
	protected void setMailSpoolInterval(int spoolInterval) {
		this.spoolInterval = spoolInterval;
	}

	/**
	 * sets the timezone
	 * 
	 * @param timeZone
	 */
	protected void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * sets the time server
	 * 
	 * @param timeServer
	 */
	protected void setTimeServer(String timeServer) {
		this.timeServer = timeServer;
	}

	/**
	 * sets the locale
	 * 
	 * @param strLocale
	 */
	protected void setLocale(String strLocale) {
		if (strLocale == null) {
			this.locale = Locale.US;
		}
		else {
			try {
				this.locale = Caster.toLocale(strLocale);
				if (this.locale == null) this.locale = Locale.US;
			}
			catch (ExpressionException e) {
				this.locale = Locale.US;
			}
		}
	}

	/**
	 * sets the locale
	 * 
	 * @param locale
	 */
	protected void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @param mappings The mappings to set.
	 */
	protected void setMappings(Mapping[] mappings) {
		this.mappings = ConfigWebUtil.sort(mappings);
	}

	/**
	 * @param datasources The datasources to set
	 */
	protected void setDataSources(Map<String, DataSource> datasources) {
		this.datasources = datasources;
	}

	/**
	 * @param customTagMappings The customTagMapping to set.
	 */
	protected void setCustomTagMappings(Mapping[] customTagMappings) {
		this.customTagMappings = customTagMappings;
	}

	@Override
	public Mapping[] getCustomTagMappings() {
		return customTagMappings;
	}

	/**
	 * @param mailServers The mailsServers to set.
	 */
	protected void setMailServers(Server[] mailServers) {
		this.mailServers = mailServers;
	}

	/**
	 * is file a directory or not, touch if not exist
	 * 
	 * @param directory
	 * @return true if existing directory or has created new one
	 */
	protected boolean isDirectory(Resource directory) {
		if (directory.exists()) return directory.isDirectory();
		try {
			directory.createDirectory(true);
			return true;
		}
		catch (IOException e) {
			e.printStackTrace(getErrWriter());
		}
		return false;
	}

	@Override
	public long getLoadTime() {
		return loadTime;
	}

	/**
	 * @param loadTime The loadTime to set.
	 */
	protected void setLoadTime(long loadTime) {
		this.loadTime = loadTime;
	}

	/**
	 * @return Returns the configLogger. / public Log getConfigLogger() { return configLogger; }
	 */

	@Override
	public CFXTagPool getCFXTagPool() throws SecurityException {
		return cfxTagPool;
	}

	/**
	 * @param cfxTagPool The customTagPool to set.
	 */
	protected void setCFXTagPool(CFXTagPool cfxTagPool) {
		this.cfxTagPool = cfxTagPool;
	}

	/**
	 * @param cfxTagPool The customTagPool to set.
	 */
	protected void setCFXTagPool(Map cfxTagPool) {
		this.cfxTagPool = new CFXTagPoolImpl(cfxTagPool);
	}

	@Override
	public String getBaseComponentTemplate(int dialect) {
		if (dialect == CFMLEngine.DIALECT_CFML) return baseComponentTemplateCFML;
		return baseComponentTemplateLucee;
	}

	/**
	 * @return pagesource of the base component
	 */
	@Override
	public PageSource getBaseComponentPageSource(int dialect) {
		return getBaseComponentPageSource(dialect, ThreadLocalPageContext.get());
	}

	@Override
	public PageSource getBaseComponentPageSource(int dialect, PageContext pc) {
		PageSource base = dialect == CFMLEngine.DIALECT_CFML ? baseComponentPageSourceCFML : baseComponentPageSourceLucee;

		if (base == null) {
			base = PageSourceImpl.best(getPageSources(pc, null, getBaseComponentTemplate(dialect), false, false, true));
			if (!base.exists()) {
				String baseTemplate = getBaseComponentTemplate(dialect);
				String mod = ContractPath.call(pc, baseTemplate, false);
				if (!mod.equals(baseTemplate)) {
					base = PageSourceImpl.best(getPageSources(pc, null, mod, false, false, true));

				}
			}
			if (dialect == CFMLEngine.DIALECT_CFML) this.baseComponentPageSourceCFML = base;
			else this.baseComponentPageSourceLucee = base;
		}
		return base;
	}

	/**
	 * @param template The baseComponent template to set.
	 */
	protected void setBaseComponentTemplate(int dialect, String template) {
		if (dialect == CFMLEngine.DIALECT_CFML) {
			this.baseComponentPageSourceCFML = null;
			this.baseComponentTemplateCFML = template;

		}
		else {
			this.baseComponentPageSourceLucee = null;
			this.baseComponentTemplateLucee = template;
		}

	}

	protected void setRestList(boolean restList) {
		this.restList = restList;
	}

	@Override
	public boolean getRestList() {
		return restList;
	}

	/**
	 * @param clientType
	 */
	protected void setClientType(short clientType) {
		this.clientType = clientType;
	}

	/**
	 * @param strClientType
	 */
	protected void setClientType(String strClientType) {
		strClientType = strClientType.trim().toLowerCase();
		if (strClientType.equals("file")) clientType = Config.CLIENT_SCOPE_TYPE_FILE;
		else if (strClientType.equals("db")) clientType = Config.CLIENT_SCOPE_TYPE_DB;
		else if (strClientType.equals("database")) clientType = Config.CLIENT_SCOPE_TYPE_DB;
		else clientType = Config.CLIENT_SCOPE_TYPE_COOKIE;
	}

	@Override
	public short getClientType() {
		return this.clientType;
	}

	/**
	 * @param searchEngine The searchEngine to set.
	 */
	protected void setSearchEngine(ClassDefinition cd, String directory) {
		this.searchEngineClassDef = cd;
		this.searchEngineDirectory = directory;
	}

	@Override
	public ClassDefinition<SearchEngine> getSearchEngineClassDefinition() {
		return this.searchEngineClassDef;
	}

	@Override
	public String getSearchEngineDirectory() {
		return this.searchEngineDirectory;
	}

	@Override
	public int getComponentDataMemberDefaultAccess() {
		return componentDataMemberDefaultAccess;
	}

	/**
	 * @param componentDataMemberDefaultAccess The componentDataMemberDefaultAccess to set.
	 */
	protected void setComponentDataMemberDefaultAccess(int componentDataMemberDefaultAccess) {
		this.componentDataMemberDefaultAccess = componentDataMemberDefaultAccess;
	}

	@Override
	public String getTimeServer() {
		return timeServer;
	}

	@Override
	public String getComponentDumpTemplate() {
		return componentDumpTemplate;
	}

	/**
	 * @param template The componentDump template to set.
	 */
	protected void setComponentDumpTemplate(String template) {
		this.componentDumpTemplate = template;
	}

	public String createSecurityToken() {
		try {
			return Md5.getDigestAsString(getConfigDir().getAbsolutePath());
		}
		catch (IOException e) {
			return null;
		}

	}

	@Override
	public String getDebugTemplate() {
		throw new PageRuntimeException(new DeprecatedException("no longer supported, use instead getDebugEntry(ip, defaultValue)"));
	}

	@Override
	public String getErrorTemplate(int statusCode) {
		return errorTemplates.get(Caster.toString(statusCode));
	}

	/**
	 * @param errorTemplate the errorTemplate to set
	 */
	protected void setErrorTemplate(int statusCode, String errorTemplate) {
		this.errorTemplates.put(Caster.toString(statusCode), errorTemplate);
	}

	@Override
	public short getSessionType() {
		return sessionType;
	}

	/**
	 * @param sessionType The sessionType to set.
	 */
	protected void setSessionType(short sessionType) {
		this.sessionType = sessionType;
	}

	@Override
	public abstract String getUpdateType();

	@Override
	public abstract URL getUpdateLocation();

	@Override
	public Resource getClassDirectory() {
		return deployDirectory;
	}

	@Override
	public Resource getLibraryDirectory() {
		Resource dir = getConfigDir().getRealResource("lib");
		if (!dir.exists()) dir.mkdir();
		return dir;
	}

	@Override
	public Resource getEventGatewayDirectory() {
		Resource dir = getConfigDir().getRealResource("context/admin/gdriver");
		if (!dir.exists()) dir.mkdir();
		return dir;
	}

	@Override
	public Resource getClassesDirectory() {
		Resource dir = getConfigDir().getRealResource("classes");
		if (!dir.exists()) dir.mkdir();
		return dir;
	}

	/**
	 * set the deploy directory, directory where lucee deploy transalted cfml classes (java and class
	 * files)
	 * 
	 * @param strDeployDirectory deploy directory
	 * @throws ExpressionException
	 */
	protected void setDeployDirectory(String strDeployDirectory) throws ExpressionException {
		setDeployDirectory(resources.getResource(strDeployDirectory));
	}

	/**
	 * set the deploy directory, directory where lucee deploy transalted cfml classes (java and class
	 * files)
	 * 
	 * @param deployDirectory deploy directory
	 * @throws ExpressionException
	 * @throws ExpressionException
	 */
	protected void setDeployDirectory(Resource deployDirectory) throws ExpressionException {
		if (!isDirectory(deployDirectory)) {
			throw new ExpressionException("deploy directory " + deployDirectory + " doesn't exist or is not a directory");
		}
		this.deployDirectory = deployDirectory;
	}

	@Override
	public abstract Resource getRootDirectory();

	/**
	 * sets the compileType value.
	 * 
	 * @param compileType The compileType to set.
	 */
	protected void setCompileType(short compileType) {
		this.compileType = compileType;
	}

	/**
	 * FUTHER Returns the value of suppresswhitespace.
	 * 
	 * @return value suppresswhitespace
	 */
	@Override
	public boolean isSuppressWhitespace() {
		return suppresswhitespace;
	}

	/**
	 * FUTHER sets the suppresswhitespace value.
	 * 
	 * @param suppresswhitespace The suppresswhitespace to set.
	 */
	protected void setSuppressWhitespace(boolean suppresswhitespace) {
		this.suppresswhitespace = suppresswhitespace;
	}

	@Override
	public boolean isSuppressContent() {
		return suppressContent;
	}

	protected void setSuppressContent(boolean suppressContent) {
		this.suppressContent = suppressContent;
	}

	@Override
	public String getDefaultEncoding() {
		return webCharset.name();
	}

	@Override
	public Charset getTemplateCharset() {
		return CharsetUtil.toCharset(templateCharset);
	}

	public CharSet getTemplateCharSet() {
		return templateCharset;
	}

	/**
	 * sets the charset to read the files
	 * 
	 * @param templateCharset
	 */
	protected void setTemplateCharset(String templateCharset) {
		this.templateCharset = CharsetUtil.toCharSet(templateCharset, this.templateCharset);
	}

	protected void setTemplateCharset(Charset templateCharset) {
		this.templateCharset = CharsetUtil.toCharSet(templateCharset);
	}

	@Override
	public Charset getWebCharset() {
		return CharsetUtil.toCharset(webCharset);
	}

	@Override
	public CharSet getWebCharSet() {
		return webCharset;
	}

	/**
	 * sets the charset to read and write resources
	 * 
	 * @param resourceCharset
	 */
	protected void setResourceCharset(String resourceCharset) {
		this.resourceCharset = CharsetUtil.toCharSet(resourceCharset, this.resourceCharset);
	}

	protected void setResourceCharset(Charset resourceCharset) {
		this.resourceCharset = CharsetUtil.toCharSet(resourceCharset);
	}

	@Override
	public Charset getResourceCharset() {
		return CharsetUtil.toCharset(resourceCharset);
	}

	@Override
	public CharSet getResourceCharSet() {
		return resourceCharset;
	}

	/**
	 * sets the charset for the response stream
	 * 
	 * @param webCharset
	 */
	protected void setWebCharset(String webCharset) {
		this.webCharset = CharsetUtil.toCharSet(webCharset, this.webCharset);
	}

	protected void setWebCharset(Charset webCharset) {
		this.webCharset = CharsetUtil.toCharSet(webCharset);
	}

	@Override
	public SecurityManager getSecurityManager() {
		return null;
	}

	@Override
	public Resource getFldFile() {
		return fldFile;
	}

	@Override
	public Resource getTldFile() {
		return tldFile;
	}

	@Override
	public DataSource[] getDataSources() {
		Map<String, DataSource> map = getDataSourcesAsMap();
		Iterator<DataSource> it = map.values().iterator();
		DataSource[] ds = new DataSource[map.size()];
		int count = 0;

		while (it.hasNext()) {
			ds[count++] = it.next();
		}
		return ds;
	}

	@Override
	public Map<String, DataSource> getDataSourcesAsMap() {
		Map<String, DataSource> map = new HashMap<String, DataSource>();
		Iterator<Entry<String, DataSource>> it = datasources.entrySet().iterator();
		Entry<String, DataSource> entry;
		while (it.hasNext()) {
			entry = it.next();
			if (!entry.getKey().equals(QOQ_DATASOURCE_NAME)) map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	/**
	 * @return the mailDefaultCharset
	 */
	@Override
	public Charset getMailDefaultCharset() {
		return mailDefaultCharset.toCharset();
	}

	public CharSet getMailDefaultCharSet() {
		return mailDefaultCharset;
	}

	/**
	 * @param mailDefaultEncoding the mailDefaultCharset to set
	 */
	protected void setMailDefaultEncoding(String mailDefaultCharset) {
		this.mailDefaultCharset = CharsetUtil.toCharSet(mailDefaultCharset, this.mailDefaultCharset);
	}

	protected void setMailDefaultEncoding(Charset mailDefaultCharset) {
		this.mailDefaultCharset = CharsetUtil.toCharSet(mailDefaultCharset);
	}

	protected void setDefaultResourceProvider(Class defaultProviderClass, Map arguments) throws ClassException {
		Object o = ClassUtil.loadInstance(defaultProviderClass);
		if (o instanceof ResourceProvider) {
			ResourceProvider rp = (ResourceProvider) o;
			rp.init(null, arguments);
			setDefaultResourceProvider(rp);
		}
		else throw new ClassException("object [" + Caster.toClassName(o) + "] must implement the interface " + ResourceProvider.class.getName());
	}

	/**
	 * @param defaultResourceProvider the defaultResourceProvider to set
	 */
	protected void setDefaultResourceProvider(ResourceProvider defaultResourceProvider) {
		resources.registerDefaultResourceProvider(defaultResourceProvider);
	}

	/**
	 * @return the defaultResourceProvider
	 */
	@Override
	public ResourceProvider getDefaultResourceProvider() {
		return resources.getDefaultResourceProvider();
	}

	protected void addCacheHandler(String id, ClassDefinition<CacheHandler> cd) throws ClassException, BundleException {
		Class<CacheHandler> clazz = cd.getClazz();
		Object o = ClassUtil.loadInstance(clazz); // just try to load and forget afterwards
		if (o instanceof CacheHandler) {
			addCacheHandler(id, clazz);
		}
		else throw new ClassException("object [" + Caster.toClassName(o) + "] must implement the interface " + CacheHandler.class.getName());
	}

	protected void addCacheHandler(String id, Class<CacheHandler> chc) {
		cacheHandlerClasses.put(id, chc);
	}

	@Override
	public Iterator<Entry<String, Class<CacheHandler>>> getCacheHandlers() {
		return cacheHandlerClasses.entrySet().iterator();
	}

	protected void addResourceProvider(String strProviderScheme, ClassDefinition cd, Map arguments) throws ClassException, BundleException {
		((ResourcesImpl) resources).registerResourceProvider(strProviderScheme, cd, arguments);
	}

	/*
	 * protected void addResourceProvider(ResourceProvider provider) {
	 * ((ResourcesImpl)resources).registerResourceProvider(provider); }
	 */

	public void clearResourceProviders() {
		resources.reset();
	}

	/**
	 * @return return the resource providers
	 */
	@Override
	public ResourceProvider[] getResourceProviders() {
		return resources.getResourceProviders();
	}

	/**
	 * @return return the resource providers
	 */
	@Override
	public ResourceProviderFactory[] getResourceProviderFactories() {
		return ((ResourcesImpl) resources).getResourceProviderFactories();
	}

	@Override
	public boolean hasResourceProvider(String scheme) {
		ResourceProviderFactory[] factories = ((ResourcesImpl) resources).getResourceProviderFactories();
		for (int i = 0; i < factories.length; i++) {
			if (factories[i].getScheme().equalsIgnoreCase(scheme)) return true;
		}
		return false;
	}

	protected void setResourceProviderFactories(ResourceProviderFactory[] resourceProviderFactories) {
		for (int i = 0; i < resourceProviderFactories.length; i++) {
			((ResourcesImpl) resources).registerResourceProvider(resourceProviderFactories[i]);
		}
	}

	@Override
	public Resource getResource(String path) {
		return resources.getResource(path);
	}

	@Override
	public ApplicationListener getApplicationListener() {
		return applicationListener;
	}

	/**
	 * @param applicationListener the applicationListener to set
	 */
	protected void setApplicationListener(ApplicationListener applicationListener) {
		this.applicationListener = applicationListener;
	}

	/**
	 * @return the scriptProtect
	 */
	@Override
	public int getScriptProtect() {
		return scriptProtect;
	}

	/**
	 * @param scriptProtect the scriptProtect to set
	 */
	protected void setScriptProtect(int scriptProtect) {
		this.scriptProtect = scriptProtect;
	}

	/**
	 * @return the proxyPassword
	 */
	@Override
	public ProxyData getProxyData() {
		return proxy;
	}

	/**
	 * @param proxy the proxyPassword to set
	 */
	protected void setProxyData(ProxyData proxy) {
		this.proxy = proxy;
	}

	@Override
	public boolean isProxyEnableFor(String host) { // FUTURE remove
		return ProxyDataImpl.isProxyEnableFor(getProxyData(), host);
	}

	/**
	 * @return the triggerComponentDataMember
	 */
	@Override
	public boolean getTriggerComponentDataMember() {
		return triggerComponentDataMember;
	}

	/**
	 * @param triggerComponentDataMember the triggerComponentDataMember to set
	 */
	protected void setTriggerComponentDataMember(boolean triggerComponentDataMember) {
		this.triggerComponentDataMember = triggerComponentDataMember;
	}

	@Override
	public Resource getClientScopeDir() {
		if (clientScopeDir == null) clientScopeDir = getConfigDir().getRealResource("client-scope");
		return clientScopeDir;
	}

	@Override
	public Resource getSessionScopeDir() {
		if (sessionScopeDir == null) sessionScopeDir = getConfigDir().getRealResource("session-scope");
		return sessionScopeDir;
	}

	@Override
	public long getClientScopeDirSize() {
		return clientScopeDirSize;
	}

	public long getSessionScopeDirSize() {
		return sessionScopeDirSize;
	}

	/**
	 * @param clientScopeDir the clientScopeDir to set
	 */
	protected void setClientScopeDir(Resource clientScopeDir) {
		this.clientScopeDir = clientScopeDir;
	}

	protected void setSessionScopeDir(Resource sessionScopeDir) {
		this.sessionScopeDir = sessionScopeDir;
	}

	/**
	 * @param clientScopeDirSize the clientScopeDirSize to set
	 */
	protected void setClientScopeDirSize(long clientScopeDirSize) {
		this.clientScopeDirSize = clientScopeDirSize;
	}

	@Override
	public ClassLoader getRPCClassLoader(boolean reload) throws IOException {

		if (rpcClassLoader != null && !reload) return rpcClassLoader;

		Resource dir = getClassDirectory().getRealResource("RPC");
		if (!dir.exists()) dir.createDirectory(true);
		rpcClassLoader = new PhysicalClassLoader(this, dir, null, false);
		return rpcClassLoader;
	}

	@Override
	public ClassLoader getRPCClassLoader(boolean reload, ClassLoader[] parents) throws IOException {

		if (rpcClassLoader != null && !reload) return rpcClassLoader;

		Resource dir = getClassDirectory().getRealResource("RPC");
		if (!dir.exists()) dir.createDirectory(true);
		rpcClassLoader = new PhysicalClassLoader(this, dir, parents, false);
		return rpcClassLoader;
	}

	public void resetRPCClassLoader() {
		rpcClassLoader = null;
	}

	protected void setCacheDir(Resource cacheDir) {
		this.cacheDir = cacheDir;
	}

	@Override
	public Resource getCacheDir() {
		return this.cacheDir;
	}

	@Override
	public long getCacheDirSize() {
		return cacheDirSize;
	}

	protected void setCacheDirSize(long cacheDirSize) {
		this.cacheDirSize = cacheDirSize;
	}

	protected void setDumpWritersEntries(DumpWriterEntry[] dmpWriterEntries) {
		this.dmpWriterEntries = dmpWriterEntries;
	}

	public DumpWriterEntry[] getDumpWritersEntries() {
		return dmpWriterEntries;
	}

	@Override
	public DumpWriter getDefaultDumpWriter(int defaultType) {
		DumpWriterEntry[] entries = getDumpWritersEntries();
		if (entries != null) for (int i = 0; i < entries.length; i++) {
			if (entries[i].getDefaultType() == defaultType) {
				return entries[i].getWriter();
			}
		}
		return new HTMLDumpWriter();
	}

	@Override
	public DumpWriter getDumpWriter(String name) throws DeprecatedException {
		throw new DeprecatedException("this method is no longer supported");
	}

	@Override
	public DumpWriter getDumpWriter(String name, int defaultType) throws ExpressionException {
		if (StringUtil.isEmpty(name)) return getDefaultDumpWriter(defaultType);

		DumpWriterEntry[] entries = getDumpWritersEntries();
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getName().equals(name)) {
				return entries[i].getWriter();
			}
		}

		// error
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < entries.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(entries[i].getName());
		}
		throw new ExpressionException("invalid format definition [" + name + "], valid definitions are [" + sb + "]");
	}

	@Override
	public boolean useComponentShadow() {
		return useComponentShadow;
	}

	@Override
	public boolean useComponentPathCache() {
		return useComponentPathCache;
	}

	@Override
	public boolean useCTPathCache() {
		return useCTPathCache;
	}

	public void flushComponentPathCache() {
		if (componentPathCache != null) componentPathCache.clear();
	}

	public void flushCTPathCache() {
		if (ctPatchCache != null) ctPatchCache.clear();
	}

	protected void setUseCTPathCache(boolean useCTPathCache) {
		this.useCTPathCache = useCTPathCache;
	}

	protected void setUseComponentPathCache(boolean useComponentPathCache) {
		this.useComponentPathCache = useComponentPathCache;
	}

	/**
	 * @param useComponentShadow the useComponentShadow to set
	 */
	protected void setUseComponentShadow(boolean useComponentShadow) {
		this.useComponentShadow = useComponentShadow;
	}

	@Override
	public DataSource getDataSource(String datasource) throws DatabaseException {
		DataSource ds = (datasource == null) ? null : (DataSource) datasources.get(datasource.toLowerCase());
		if (ds != null) return ds;

		// create error detail
		DatabaseException de = new DatabaseException("datasource [" + datasource + "] doesn't exist", null, null, null);
		de.setDetail(ExceptionUtil.createSoundexDetail(datasource, datasources.keySet().iterator(), "datasource names"));
		de.setAdditional(KeyConstants._Datasource, datasource);
		throw de;
	}

	@Override
	public DataSource getDataSource(String datasource, DataSource defaultValue) {
		DataSource ds = (datasource == null) ? null : (DataSource) datasources.get(datasource.toLowerCase());
		if (ds != null) return ds;
		return defaultValue;
	}

	@Override
	public PrintWriter getErrWriter() {
		return err;
	}

	/**
	 * @param err the err to set
	 */
	protected void setErr(PrintWriter err) {
		this.err = err;
	}

	@Override
	public PrintWriter getOutWriter() {
		return out;
	}

	/**
	 * @param out the out to set
	 */
	protected void setOut(PrintWriter out) {
		this.out = out;
	}

	@Override
	public DatasourceConnectionPool getDatasourceConnectionPool() {
		return pool;
	}

	@Override
	public boolean doLocalCustomTag() {
		return doLocalCustomTag;
	}

	@Override
	public String[] getCustomTagExtensions() {
		return customTagExtensions;
	}

	protected void setCustomTagExtensions(String... customTagExtensions) {
		this.customTagExtensions = customTagExtensions;
	}

	protected void setDoLocalCustomTag(boolean doLocalCustomTag) {
		this.doLocalCustomTag = doLocalCustomTag;
	}

	@Override
	public boolean doComponentDeepSearch() {
		return doComponentTagDeepSearch;
	}

	protected void setDoComponentDeepSearch(boolean doComponentTagDeepSearch) {
		this.doComponentTagDeepSearch = doComponentTagDeepSearch;
	}

	@Override
	public boolean doCustomTagDeepSearch() {
		return doCustomTagDeepSearch;
	}

	/**
	 * @param doCustomTagDeepSearch the doCustomTagDeepSearch to set
	 */
	protected void setDoCustomTagDeepSearch(boolean doCustomTagDeepSearch) {
		this.doCustomTagDeepSearch = doCustomTagDeepSearch;
	}

	protected void setVersion(double version) {
		this.version = version;
	}

	/**
	 * @return the version
	 */
	@Override
	public double getVersion() {
		return version;
	}

	@Override
	public boolean closeConnection() {
		return closeConnection;
	}

	protected void setCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
	}

	@Override
	public boolean contentLength() {
		return contentLength;
	}

	@Override
	public boolean allowCompression() {
		return allowCompression;
	}

	protected void setAllowCompression(boolean allowCompression) {
		this.allowCompression = allowCompression;
	}

	protected void setContentLength(boolean contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * @return the constants
	 */
	@Override
	public Struct getConstants() {
		return constants;
	}

	/**
	 * @param constants the constants to set
	 */
	protected void setConstants(Struct constants) {
		this.constants = constants;
	}

	/**
	 * @return the showVersion
	 */
	@Override
	public boolean isShowVersion() {
		return showVersion;
	}

	/**
	 * @param showVersion the showVersion to set
	 */
	protected void setShowVersion(boolean showVersion) {
		this.showVersion = showVersion;
	}

	protected void setRemoteClients(RemoteClient[] remoteClients) {
		this.remoteClients = remoteClients;
	}

	@Override
	public RemoteClient[] getRemoteClients() {
		if (remoteClients == null) return new RemoteClient[0];
		return remoteClients;
	}

	@Override
	public SpoolerEngine getSpoolerEngine() {
		return remoteClientSpoolerEngine;
	}

	protected void setRemoteClientDirectory(Resource remoteClientDirectory) {
		this.remoteClientDirectory = remoteClientDirectory;
	}

	/**
	 * @return the remoteClientDirectory
	 */
	@Override
	public Resource getRemoteClientDirectory() {
		if (remoteClientDirectory == null) {
			return ConfigWebUtil.getFile(getRootDirectory(), "client-task", "client-task", getConfigDir(), FileUtil.TYPE_DIR, this);
		}

		return remoteClientDirectory;
	}

	protected void setSpoolerEngine(SpoolerEngine spoolerEngine) {
		this.remoteClientSpoolerEngine = spoolerEngine;
	}

	/*
	 * *
	 * 
	 * @return the structCase / public int getStructCase() { return structCase; }
	 */

	/*
	 * *
	 * 
	 * @param structCase the structCase to set / protected void setStructCase(int structCase) {
	 * this.structCase = structCase; }
	 */

	/**
	 * @return if error status code will be returned or not
	 */
	@Override
	public boolean getErrorStatusCode() {
		return errorStatusCode;
	}

	/**
	 * @param errorStatusCode the errorStatusCode to set
	 */
	protected void setErrorStatusCode(boolean errorStatusCode) {
		this.errorStatusCode = errorStatusCode;
	}

	@Override
	public int getLocalMode() {
		return localMode;
	}

	/**
	 * @param localMode the localMode to set
	 */
	protected void setLocalMode(int localMode) {
		this.localMode = localMode;
	}

	/**
	 * @param strLocalMode the localMode to set
	 */
	protected void setLocalMode(String strLocalMode) {
		this.localMode = AppListenerUtil.toLocalMode(strLocalMode, this.localMode);
	}

	@Override
	public Resource getVideoDirectory() {
		// TODO take from tag <video>
		Resource dir = getConfigDir().getRealResource("video");
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}

	@Override
	public Resource getExtensionDirectory() {
		// TODO take from tag <extensions>
		Resource dir = getConfigDir().getRealResource("extensions/installed");
		if (!dir.exists()) dir.mkdirs();

		return dir;
	}

	@Override
	public ExtensionProvider[] getExtensionProviders() {
		throw new RuntimeException("no longer supported, use getRHExtensionProviders() instead.");
	}

	protected void setRHExtensionProviders(RHExtensionProvider[] extensionProviders) {
		this.rhextensionProviders = extensionProviders;
	}

	@Override
	public RHExtensionProvider[] getRHExtensionProviders() {
		return rhextensionProviders;
	}

	@Override
	public Extension[] getExtensions() {
		return extensions;
	}

	@Override
	public RHExtension[] getRHExtensions() {
		return rhextensions;
	}

	protected void setExtensions(Extension[] extensions) {
		this.extensions = extensions;
	}

	protected void setExtensions(RHExtension[] extensions) {
		this.rhextensions = extensions;
	}

	protected void setExtensionEnabled(boolean extensionEnabled) {
		this.extensionEnabled = extensionEnabled;
	}

	@Override
	public boolean isExtensionEnabled() {
		return extensionEnabled;
	}

	@Override
	public boolean allowRealPath() {
		return allowRealPath;
	}

	protected void setAllowRealPath(boolean allowRealPath) {
		this.allowRealPath = allowRealPath;
	}

	/**
	 * @return the classClusterScope
	 */
	@Override
	public Class getClusterClass() {
		return clusterClass;
	}

	/**
	 * @param clusterClass the classClusterScope to set
	 */
	protected void setClusterClass(Class clusterClass) {
		this.clusterClass = clusterClass;
	}

	@Override
	public Struct getRemoteClientUsage() {
		if (remoteClientUsage == null) remoteClientUsage = new StructImpl();
		return remoteClientUsage;
	}

	protected void setRemoteClientUsage(Struct remoteClientUsage) {
		this.remoteClientUsage = remoteClientUsage;
	}

	@Override
	public Class<AdminSync> getAdminSyncClass() {
		return adminSyncClass;
	}

	protected void setAdminSyncClass(Class adminSyncClass) {
		this.adminSyncClass = adminSyncClass;
		this.adminSync = null;
	}

	@Override
	public AdminSync getAdminSync() throws ClassException {
		if (adminSync == null) {
			adminSync = (AdminSync) ClassUtil.loadInstance(getAdminSyncClass());

		}
		return this.adminSync;
	}

	@Override
	public Class getVideoExecuterClass() {
		return videoExecuterClass;
	}

	protected void setVideoExecuterClass(Class videoExecuterClass) {
		this.videoExecuterClass = videoExecuterClass;
	}

	protected void setUseTimeServer(boolean useTimeServer) {
		this.useTimeServer = useTimeServer;
	}

	@Override
	public boolean getUseTimeServer() {
		return useTimeServer;
	}

	/**
	 * @return the tagMappings
	 */
	@Override
	public Collection<Mapping> getTagMappings() {
		return tagMappings.values();
	}

	@Override
	public Mapping getTagMapping(String mappingName) {
		return tagMappings.get(mappingName);
	}

	@Override
	public Mapping getDefaultTagMapping() {
		return defaultTagMapping;
	}

	@Override
	public Mapping getFunctionMapping(String mappingName) {
		return functionMappings.get(mappingName);
	}

	@Override
	public Mapping getDefaultFunctionMapping() {
		return defaultFunctionMapping;
	}

	@Override
	public Collection<Mapping> getFunctionMappings() {
		return functionMappings.values();
	}

	/*
	 * *
	 * 
	 * @return the tagDirectory
	 * 
	 * public Resource getTagDirectory() { return tagDirectory; }
	 */

	/**
	 * mapping used for script (JSR 223)
	 * 
	 * @return
	 */
	public Mapping getScriptMapping() {
		if (scriptMapping == null) {
			// Physical resource TODO make in RAM
			Resource physical = getConfigDir().getRealResource("jsr223");
			if (!physical.exists()) physical.mkdirs();

			this.scriptMapping = new MappingImpl(this, "/mapping-script/", physical.getAbsolutePath(), null, ConfigPro.INSPECT_NEVER, true, true, true, true, false, true, null, -1,
					-1);
		}
		return scriptMapping;
	}

	@Override
	public String getDefaultDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	protected void setDefaultDataSource(String defaultDataSource) {
		// this.defaultDataSource=defaultDataSource;
	}

	/**
	 * @return the inspectTemplate
	 */
	@Override
	public short getInspectTemplate() {
		return inspectTemplate;
	}

	@Override
	public boolean getTypeChecking() {
		return typeChecking;
	}

	protected void setTypeChecking(boolean typeChecking) {
		this.typeChecking = typeChecking;
	}

	/**
	 * @param inspectTemplate the inspectTemplate to set
	 */
	protected void setInspectTemplate(short inspectTemplate) {
		this.inspectTemplate = inspectTemplate;
	}

	@Override
	public String getSerialNumber() {
		return "";
	}

	protected void setCaches(Map<String, CacheConnection> caches) {
		this.caches = caches;
		Iterator<Entry<String, CacheConnection>> it = caches.entrySet().iterator();
		Entry<String, CacheConnection> entry;
		CacheConnection cc;
		while (it.hasNext()) {
			entry = it.next();
			cc = entry.getValue();
			if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameTemplate)) {
				defaultCacheTemplate = cc;
			}
			else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameFunction)) {
				defaultCacheFunction = cc;
			}
			else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameQuery)) {
				defaultCacheQuery = cc;
			}
			else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameResource)) {
				defaultCacheResource = cc;
			}
			else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameObject)) {
				defaultCacheObject = cc;
			}
			else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameInclude)) {
				defaultCacheInclude = cc;
			}
			else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameHTTP)) {
				defaultCacheHTTP = cc;
			}
			else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameFile)) {
				defaultCacheFile = cc;
			}
			else if (cc.getName().equalsIgnoreCase(cacheDefaultConnectionNameWebservice)) {
				defaultCacheWebservice = cc;
			}
		}
	}

	@Override
	public Map<String, CacheConnection> getCacheConnections() {
		return caches;
	}

	// used by argus cache FUTURE add to interface
	/**
	 * creates a new RamCache, please make sure to finalize.
	 * 
	 * @param arguments possible arguments are "timeToLiveSeconds", "timeToIdleSeconds" and
	 *            "controlInterval"
	 * @throws IOException
	 */
	public Cache createRAMCache(Struct arguments) throws IOException {
		RamCache rc = new RamCache();
		if (arguments == null) arguments = new StructImpl();
		rc.init(this, "" + CreateUniqueId.invoke(), arguments);
		return rc;
	}

	@Override
	public CacheConnection getCacheDefaultConnection(int type) {
		if (type == CACHE_TYPE_FUNCTION) return defaultCacheFunction;
		if (type == CACHE_TYPE_OBJECT) return defaultCacheObject;
		if (type == CACHE_TYPE_TEMPLATE) return defaultCacheTemplate;
		if (type == CACHE_TYPE_QUERY) return defaultCacheQuery;
		if (type == CACHE_TYPE_RESOURCE) return defaultCacheResource;
		if (type == CACHE_TYPE_INCLUDE) return defaultCacheInclude;
		if (type == CACHE_TYPE_HTTP) return defaultCacheHTTP;
		if (type == CACHE_TYPE_FILE) return defaultCacheFile;
		if (type == CACHE_TYPE_WEBSERVICE) return defaultCacheWebservice;

		return null;
	}

	protected void setCacheDefaultConnectionName(int type, String cacheDefaultConnectionName) {
		if (type == CACHE_TYPE_FUNCTION) cacheDefaultConnectionNameFunction = cacheDefaultConnectionName;
		else if (type == CACHE_TYPE_OBJECT) cacheDefaultConnectionNameObject = cacheDefaultConnectionName;
		else if (type == CACHE_TYPE_TEMPLATE) cacheDefaultConnectionNameTemplate = cacheDefaultConnectionName;
		else if (type == CACHE_TYPE_QUERY) cacheDefaultConnectionNameQuery = cacheDefaultConnectionName;
		else if (type == CACHE_TYPE_RESOURCE) cacheDefaultConnectionNameResource = cacheDefaultConnectionName;
		else if (type == CACHE_TYPE_INCLUDE) cacheDefaultConnectionNameInclude = cacheDefaultConnectionName;
		else if (type == CACHE_TYPE_HTTP) cacheDefaultConnectionNameHTTP = cacheDefaultConnectionName;
		else if (type == CACHE_TYPE_FILE) cacheDefaultConnectionNameFile = cacheDefaultConnectionName;
		else if (type == CACHE_TYPE_WEBSERVICE) cacheDefaultConnectionNameWebservice = cacheDefaultConnectionName;
	}

	@Override
	public String getCacheDefaultConnectionName(int type) {
		if (type == CACHE_TYPE_FUNCTION) return cacheDefaultConnectionNameFunction;
		if (type == CACHE_TYPE_OBJECT) return cacheDefaultConnectionNameObject;
		if (type == CACHE_TYPE_TEMPLATE) return cacheDefaultConnectionNameTemplate;
		if (type == CACHE_TYPE_QUERY) return cacheDefaultConnectionNameQuery;
		if (type == CACHE_TYPE_RESOURCE) return cacheDefaultConnectionNameResource;
		if (type == CACHE_TYPE_INCLUDE) return cacheDefaultConnectionNameInclude;
		if (type == CACHE_TYPE_HTTP) return cacheDefaultConnectionNameHTTP;
		if (type == CACHE_TYPE_FILE) return cacheDefaultConnectionNameFile;
		if (type == CACHE_TYPE_WEBSERVICE) return cacheDefaultConnectionNameWebservice;
		return null;
	}

	public String getCacheMD5() {
		return cacheMD5;
	}

	public void setCacheMD5(String cacheMD5) {
		this.cacheMD5 = cacheMD5;
	}

	@Override
	public boolean getExecutionLogEnabled() {
		return executionLogEnabled;
	}

	protected void setExecutionLogEnabled(boolean executionLogEnabled) {
		this.executionLogEnabled = executionLogEnabled;
	}

	@Override
	public ExecutionLogFactory getExecutionLogFactory() {
		return executionLogFactory;
	}

	protected void setExecutionLogFactory(ExecutionLogFactory executionLogFactory) {
		this.executionLogFactory = executionLogFactory;
	}

	@Override
	public ORMEngine resetORMEngine(PageContext pc, boolean force) throws PageException {
		// String name = pc.getApplicationContext().getName();
		// ormengines.remove(name);
		ORMEngine e = getORMEngine(pc);
		e.reload(pc, force);
		return e;
	}

	@Override
	public ORMEngine getORMEngine(PageContext pc) throws PageException {
		String name = pc.getApplicationContext().getName();

		ORMEngine engine = ormengines.get(name);
		if (engine == null) {
			// try {
			Throwable t = null;

			try {
				engine = (ORMEngine) ClassUtil.loadInstance(cdORMEngine.getClazz());
				engine.init(pc);
			}
			catch (ClassException ce) {
				t = ce;
			}
			catch (BundleException be) {
				t = be;
			}
			catch (NoClassDefFoundError ncfe) {
				t = ncfe;
			}

			if (t != null) {
				ApplicationException ae = new ApplicationException("cannot initialize ORM Engine [" + cdORMEngine + "], make sure you have added all the required jar files");

				ae.setStackTrace(t.getStackTrace());
				ae.setDetail(t.getMessage());

			}
			ormengines.put(name, engine);
			/*
			 * } catch (PageException pe) { throw pe; }
			 */
		}

		return engine;
	}

	@Override
	public ClassDefinition<? extends ORMEngine> getORMEngineClassDefintion() {
		return cdORMEngine;
	}

	@Override
	public Mapping[] getComponentMappings() {
		return componentMappings;
	}

	/**
	 * @param componentMappings the componentMappings to set
	 */
	protected void setComponentMappings(Mapping[] componentMappings) {
		this.componentMappings = componentMappings;
	}

	protected void setORMEngineClass(ClassDefinition<? extends ORMEngine> cd) {
		this.cdORMEngine = cd;
	}

	public ClassDefinition<? extends ORMEngine> getORMEngineClass() {
		return this.cdORMEngine;
	}

	protected void setORMConfig(ORMConfiguration ormConfig) {
		this.ormConfig = ormConfig;
	}

	@Override
	public ORMConfiguration getORMConfig() {
		return ormConfig;
	}

	private Map<String, SoftReference<PageSource>> componentPathCache = null;// new ArrayList<Page>();
	private Map<String, SoftReference<InitFile>> ctPatchCache = null;// new ArrayList<Page>();
	private Map<String, SoftReference<UDF>> udfCache = new ConcurrentHashMap<String, SoftReference<UDF>>();

	@Override
	public CIPage getCachedPage(PageContext pc, String pathWithCFC) throws TemplateException {
		if (componentPathCache == null) return null;

		SoftReference<PageSource> tmp = componentPathCache.get(pathWithCFC.toLowerCase());
		PageSource ps = tmp == null ? null : tmp.get();
		if (ps == null) return null;

		try {
			return (CIPage) ps.loadPageThrowTemplateException(pc, false, (Page) null);
		}
		catch (PageException pe) {
			throw (TemplateException) pe;
		}
	}

	@Override
	public void putCachedPageSource(String pathWithCFC, PageSource ps) {
		if (componentPathCache == null) componentPathCache = new ConcurrentHashMap<String, SoftReference<PageSource>>();// MUSTMUST new
		// ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);
		componentPathCache.put(pathWithCFC.toLowerCase(), new SoftReference<PageSource>(ps));
	}

	@Override
	public InitFile getCTInitFile(PageContext pc, String key) {
		if (ctPatchCache == null) return null;

		SoftReference<InitFile> tmp = ctPatchCache.get(key.toLowerCase());
		InitFile initFile = tmp == null ? null : tmp.get();
		if (initFile != null) {
			if (MappingImpl.isOK(initFile.getPageSource())) return initFile;
			ctPatchCache.remove(key.toLowerCase());
		}
		return null;
	}

	@Override
	public void putCTInitFile(String key, InitFile initFile) {
		if (ctPatchCache == null) ctPatchCache = new ConcurrentHashMap<String, SoftReference<InitFile>>();// MUSTMUST new ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);
		ctPatchCache.put(key.toLowerCase(), new SoftReference<InitFile>(initFile));
	}

	@Override
	public Struct listCTCache() {
		Struct sct = new StructImpl();
		if (ctPatchCache == null) return sct;
		Iterator<Entry<String, SoftReference<InitFile>>> it = ctPatchCache.entrySet().iterator();

		Entry<String, SoftReference<InitFile>> entry;
		SoftReference<InitFile> v;
		while (it.hasNext()) {
			entry = it.next();
			v = entry.getValue();
			if (v != null) sct.setEL(entry.getKey(), v.get().getPageSource().getDisplayPath());
		}
		return sct;
	}

	@Override
	public void clearCTCache() {
		if (ctPatchCache == null) return;
		ctPatchCache.clear();
	}

	@Override
	public void clearFunctionCache() {
		udfCache.clear();
	}

	@Override
	public UDF getFromFunctionCache(String key) {
		SoftReference<UDF> tmp = udfCache.get(key);
		if (tmp == null) return null;
		return tmp.get();
	}

	@Override
	public void putToFunctionCache(String key, UDF udf) {
		udfCache.put(key, new SoftReference<UDF>(udf));
	}

	@Override
	public Struct listComponentCache() {
		Struct sct = new StructImpl();
		if (componentPathCache == null) return sct;
		Iterator<Entry<String, SoftReference<PageSource>>> it = componentPathCache.entrySet().iterator();

		Entry<String, SoftReference<PageSource>> entry;
		while (it.hasNext()) {
			entry = it.next();
			String k = entry.getKey();
			if (k == null) continue;
			SoftReference<PageSource> v = entry.getValue();
			if (v == null) continue;
			PageSource ps = v.get();
			if (ps == null) continue;
			sct.setEL(KeyImpl.init(k), ps.getDisplayPath());
		}
		return sct;
	}

	@Override
	public void clearComponentCache() {
		if (componentPathCache == null) return;
		componentPathCache.clear();
	}

	@Override
	public ImportDefintion getComponentDefaultImport() {
		return componentDefaultImport;
	}

	protected void setComponentDefaultImport(String str) {
		if (StringUtil.isEmpty(str)) return;
		if ("org.railo.cfml.*".equalsIgnoreCase(str)) str = "org.lucee.cfml.*";

		ImportDefintion cdi = ImportDefintionImpl.getInstance(str, null);
		if (cdi != null) this.componentDefaultImport = cdi;
	}

	/**
	 * @return the componentLocalSearch
	 */
	@Override
	public boolean getComponentLocalSearch() {
		return componentLocalSearch;
	}

	/**
	 * @param componentLocalSearch the componentLocalSearch to set
	 */
	protected void setComponentLocalSearch(boolean componentLocalSearch) {
		this.componentLocalSearch = componentLocalSearch;
	}

	/**
	 * @return the componentLocalSearch
	 */
	@Override
	public boolean getComponentRootSearch() {
		return componentRootSearch;
	}

	/**
	 * @param componentRootSearch the componentLocalSearch to set
	 */
	protected void setComponentRootSearch(boolean componentRootSearch) {
		this.componentRootSearch = componentRootSearch;
	}

	private final Map<String, SoftReference<Compress>> compressResources = new ConcurrentHashMap<String, SoftReference<Compress>>();

	@Override
	public Compress getCompressInstance(Resource zipFile, int format, boolean caseSensitive) throws IOException {
		SoftReference<Compress> tmp = compressResources.get(zipFile.getPath());
		Compress compress = tmp == null ? null : tmp.get();
		if (compress == null) {
			compress = new Compress(zipFile, format, caseSensitive);
			compressResources.put(zipFile.getPath(), new SoftReference<Compress>(compress));
		}
		return compress;
	}

	@Override
	public boolean getSessionCluster() {
		return false;
	}

	@Override
	public boolean getClientCluster() {
		return false;
	}

	@Override
	public String getClientStorage() {
		return clientStorage;
	}

	@Override
	public String getSessionStorage() {
		return sessionStorage;
	}

	protected void setClientStorage(String clientStorage) {
		this.clientStorage = clientStorage;
	}

	protected void setSessionStorage(String sessionStorage) {
		this.sessionStorage = sessionStorage;
	}

	private Map<String, ComponentMetaData> componentMetaData = null;

	public ComponentMetaData getComponentMetadata(String key) {
		if (componentMetaData == null) return null;
		return componentMetaData.get(key.toLowerCase());
	}

	public void putComponentMetadata(String key, ComponentMetaData data) {
		if (componentMetaData == null) componentMetaData = new HashMap<String, ComponentMetaData>();
		componentMetaData.put(key.toLowerCase(), data);
	}

	public void clearComponentMetadata() {
		if (componentMetaData == null) return;
		componentMetaData.clear();
	}

	public static class ComponentMetaData {

		public final Struct meta;
		public final long lastMod;

		public ComponentMetaData(Struct meta, long lastMod) {
			this.meta = meta;
			this.lastMod = lastMod;
		}
	}

	private DebugEntry[] debugEntries;

	protected void setDebugEntries(DebugEntry[] debugEntries) {
		this.debugEntries = debugEntries;
	}

	@Override
	public DebugEntry[] getDebugEntries() {
		if (debugEntries == null) debugEntries = new DebugEntry[0];
		return debugEntries;
	}

	@Override
	public DebugEntry getDebugEntry(String ip, DebugEntry defaultValue) {
		if (debugEntries.length == 0) return defaultValue;
		InetAddress ia;

		try {
			ia = IPRange.toInetAddress(ip);
		}
		catch (IOException e) {
			return defaultValue;
		}

		for (int i = 0; i < debugEntries.length; i++) {
			if (debugEntries[i].getIpRange().inRange(ia)) return debugEntries[i];
		}
		return defaultValue;
	}

	private int debugMaxRecordsLogged = 10;

	protected void setDebugMaxRecordsLogged(int debugMaxRecordsLogged) {
		this.debugMaxRecordsLogged = debugMaxRecordsLogged;
	}

	@Override
	public int getDebugMaxRecordsLogged() {
		return debugMaxRecordsLogged;
	}

	private boolean dotNotationUpperCase = true;

	protected void setDotNotationUpperCase(boolean dotNotationUpperCase) {
		this.dotNotationUpperCase = dotNotationUpperCase;
	}

	@Override
	public boolean getDotNotationUpperCase() {
		return dotNotationUpperCase;
	}

	@Override
	public boolean preserveCase() {
		return !dotNotationUpperCase;
	}

	private boolean defaultFunctionOutput = true;

	protected void setDefaultFunctionOutput(boolean defaultFunctionOutput) {
		this.defaultFunctionOutput = defaultFunctionOutput;
	}

	@Override
	public boolean getDefaultFunctionOutput() {
		return defaultFunctionOutput;
	}

	private boolean getSuppressWSBeforeArg = true;

	protected void setSuppressWSBeforeArg(boolean getSuppressWSBeforeArg) {
		this.getSuppressWSBeforeArg = getSuppressWSBeforeArg;
	}

	@Override
	public boolean getSuppressWSBeforeArg() {
		return getSuppressWSBeforeArg;
	}

	private RestSettings restSetting = new RestSettingImpl(false, UDF.RETURN_FORMAT_JSON);

	protected void setRestSetting(RestSettings restSetting) {
		this.restSetting = restSetting;
	}

	@Override
	public RestSettings getRestSetting() {
		return restSetting;
	}

	protected void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	// do not move to Config interface, do instead getCFMLWriterClass
	protected void setCFMLWriterType(int writerType) {
		this.writerType = writerType;
	}

	// do not move to Config interface, do instead setCFMLWriterClass
	@Override
	public int getCFMLWriterType() {
		return writerType;
	}

	private boolean bufferOutput = false;

	private int externalizeStringGTE = -1;
	private Map<String, BundleDefinition> extensionBundles;
	private JDBCDriver[] drivers;
	private Resource logDir;

	@Override
	public boolean getBufferOutput() {
		return bufferOutput;
	}

	protected void setBufferOutput(boolean bufferOutput) {
		this.bufferOutput = bufferOutput;
	}

	public int getDebugOptions() {
		return debugOptions;
	}

	@Override
	public boolean hasDebugOptions(int debugOption) {
		return (debugOptions & debugOption) > 0;
	}

	protected void setDebugOptions(int debugOptions) {
		this.debugOptions = debugOptions;
	}

	protected void setCheckForChangesInConfigFile(boolean checkForChangesInConfigFile) {
		this.checkForChangesInConfigFile = checkForChangesInConfigFile;
	}

	@Override
	public boolean checkForChangesInConfigFile() {
		return checkForChangesInConfigFile;
	}

	protected void setExternalizeStringGTE(int externalizeStringGTE) {
		this.externalizeStringGTE = externalizeStringGTE;
	}

	@Override
	public int getExternalizeStringGTE() {
		return externalizeStringGTE;
	}

	protected void addConsoleLayout(Object layout) {
		consoleLayouts.add(layout);

	}

	protected void addResourceLayout(Object layout) {
		resourceLayouts.add(layout);
	}

	public Object[] getConsoleLayouts() {
		if (consoleLayouts.isEmpty()) consoleLayouts.add(getLogEngine().getDefaultLayout());
		return consoleLayouts.toArray(new Object[consoleLayouts.size()]);

	}

	public Object[] getResourceLayouts() {
		if (resourceLayouts.isEmpty()) resourceLayouts.add(new ClassicLayout());
		return resourceLayouts.toArray(new Object[resourceLayouts.size()]);
	}

	protected void clearLoggers(Boolean dyn) {
		if (loggers.size() == 0) return;
		List<String> list = dyn != null ? new ArrayList<String>() : null;
		try {
			Iterator<Entry<String, LoggerAndSourceData>> it = loggers.entrySet().iterator();
			Entry<String, LoggerAndSourceData> e;
			while (it.hasNext()) {
				e = it.next();
				if (dyn == null || dyn.booleanValue() == e.getValue().getDyn()) {
					e.getValue().close();
					if (list != null) list.add(e.getKey());
				}

			}
		}
		catch (Exception e) {}

		if (list == null) loggers.clear();
		else {
			Iterator<String> it = list.iterator();
			while (it.hasNext()) {
				loggers.remove(it.next());
			}
		}
	}

	protected LoggerAndSourceData addLogger(String name, int level, ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout,
			Map<String, String> layoutArgs, boolean readOnly, boolean dyn) {
		LoggerAndSourceData existing = loggers.get(name.toLowerCase());
		String id = LoggerAndSourceData.id(name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly);

		if (existing != null) {
			if (existing.id().equals(id)) {
				return existing;
			}
			existing.close();
		}

		LoggerAndSourceData las = new LoggerAndSourceData(this, id, name.toLowerCase(), appender, appenderArgs, layout, layoutArgs, level, readOnly, dyn);
		loggers.put(name.toLowerCase(), las);
		return las;
	}

	@Override
	public Map<String, LoggerAndSourceData> getLoggers() {
		return loggers;
	}

	// FUTURE add to interface
	public String[] getLogNames() {
		return loggers.keySet().toArray(new String[loggers.size()]);
	}

	@Override
	public Log getLog(String name) {
		return getLog(name, true);
	}

	@Override
	public Log getLog(String name, boolean createIfNecessary) {
		LoggerAndSourceData lsd = _getLoggerAndSourceData(name, createIfNecessary);
		if (lsd == null) return null;
		return lsd.getLog();
	}

	private LoggerAndSourceData _getLoggerAndSourceData(String name, boolean createIfNecessary) {
		LoggerAndSourceData las = loggers.get(name.toLowerCase());
		if (las == null) {
			if (!createIfNecessary) return null;
			return addLogger(name, Log.LEVEL_ERROR, getLogEngine().appenderClassDefintion("console"), null, getLogEngine().layoutClassDefintion("pattern"), null, true, true);
		}
		return las;
	}

	@Override
	public Map<Key, Map<Key, Object>> getTagDefaultAttributeValues() {
		return tagDefaultAttributeValues == null ? null : Duplicator.duplicateMap(tagDefaultAttributeValues, new HashMap<Key, Map<Key, Object>>(), true);
	}

	protected void setTagDefaultAttributeValues(Map<Key, Map<Key, Object>> values) {
		this.tagDefaultAttributeValues = values;
	}

	@Override
	public Boolean getHandleUnQuotedAttrValueAsString() {
		return handleUnQuotedAttrValueAsString;
	}

	protected void setHandleUnQuotedAttrValueAsString(boolean handleUnQuotedAttrValueAsString) {
		this.handleUnQuotedAttrValueAsString = handleUnQuotedAttrValueAsString;
	}

	protected void setCachedWithin(int type, Object value) {
		cachedWithins.put(type, value);
	}

	@Override
	public Object getCachedWithin(int type) {
		return cachedWithins.get(type);
	}

	@Override
	public Resource getPluginDirectory() {
		return getConfigDir().getRealResource("context/admin/plugin");
	}

	@Override
	public Resource getLogDirectory() {
		if (logDir == null) {
			logDir = getConfigDir().getRealResource("logs");
			logDir.mkdir();
		}
		return logDir;
	}

	protected void setSalt(String salt) {
		this.salt = salt;
	}

	@Override
	public String getSalt() {
		return this.salt;
	}

	@Override
	public int getPasswordType() {
		if (password == null) return Password.HASHED_SALTED;// when there is no password, we will have a HS password
		return password.getType();
	}

	@Override
	public String getPasswordSalt() {
		if (password == null || password.getSalt() == null) return this.salt;
		return password.getSalt();
	}

	@Override
	public int getPasswordOrigin() {
		if (password == null) return Password.ORIGIN_UNKNOW;
		return password.getOrigin();
	}

	@Override
	public Collection<BundleDefinition> getExtensionBundleDefintions() {
		if (this.extensionBundles == null) {
			RHExtension[] rhes = getRHExtensions();
			Map<String, BundleDefinition> extensionBundles = new HashMap<String, BundleDefinition>();

			for (RHExtension rhe: rhes) {
				BundleInfo[] bis;
				try {
					bis = rhe.getBundles();
				}
				catch (Exception e) {
					continue;
				}
				if (bis != null) {
					for (BundleInfo bi: bis) {
						extensionBundles.put(bi.getSymbolicName() + "|" + bi.getVersionAsString(), bi.toBundleDefinition());
					}
				}
			}
			this.extensionBundles = extensionBundles;
		}
		return extensionBundles.values();
	}

	protected void setJDBCDrivers(JDBCDriver[] drivers) {
		this.drivers = drivers;
	}

	@Override
	public JDBCDriver[] getJDBCDrivers() {
		return drivers;
	}

	@Override
	public JDBCDriver getJDBCDriverByClassName(String className, JDBCDriver defaultValue) {
		for (JDBCDriver d: drivers) {
			if (d.cd.getClassName().equals(className)) return d;
		}
		return defaultValue;
	}

	@Override
	public JDBCDriver getJDBCDriverById(String id, JDBCDriver defaultValue) {
		if (!StringUtil.isEmpty(id)) {
			for (JDBCDriver d: drivers) {
				if (d.id != null && d.id.equalsIgnoreCase(id)) return d;
			}
		}
		return defaultValue;
	}

	@Override
	public JDBCDriver getJDBCDriverByBundle(String bundleName, Version version, JDBCDriver defaultValue) {
		for (JDBCDriver d: drivers) {
			if (d.cd.getName().equals(bundleName) && (version == null || version.equals(d.cd.getVersion()))) return d;
		}
		return defaultValue;
	}

	@Override
	public JDBCDriver getJDBCDriverByCD(ClassDefinition cd, JDBCDriver defaultValue) {
		for (JDBCDriver d: drivers) {

			if (d.cd.getId().equals(cd.getId())) return d; // TODO comparing cd objects directly?
		}
		return defaultValue;
	}

	@Override
	public int getQueueMax() {
		return queueMax;
	}

	protected void setQueueMax(int queueMax) {
		this.queueMax = queueMax;
	}

	@Override
	public long getQueueTimeout() {
		return queueTimeout;
	}

	protected void setQueueTimeout(long queueTimeout) {
		this.queueTimeout = queueTimeout;
	}

	@Override
	public boolean getQueueEnable() {
		return queueEnable;
	}

	protected void setQueueEnable(boolean queueEnable) {
		this.queueEnable = queueEnable;
	}

	private boolean cgiScopeReadonly = true;

	@Override
	public boolean getCGIScopeReadonly() {
		return cgiScopeReadonly;
	}

	protected void setCGIScopeReadonly(boolean cgiScopeReadonly) {
		this.cgiScopeReadonly = cgiScopeReadonly;
	}

	private Resource deployDir;

	@Override
	public Resource getDeployDirectory() {
		if (deployDir == null) {
			// config web
			if (this instanceof ConfigWeb) {
				deployDir = getConfigDir().getRealResource("deploy");
				if (!deployDir.exists()) deployDir.mkdirs();
			}
			// config server
			else {
				try {
					File file = new File(ConfigWebUtil.getEngine(this).getCFMLEngineFactory().getResourceRoot(), "deploy");
					if (!file.exists()) file.mkdirs();
					deployDir = ResourcesImpl.getFileResourceProvider().getResource(file.getAbsolutePath());
				}
				catch (IOException ioe) {
					deployDir = getConfigDir().getRealResource("deploy");
					if (!deployDir.exists()) deployDir.mkdirs();
				}
			}
		}
		return deployDir;
	}

	private boolean allowLuceeDialect = false;

	@Override
	public boolean allowLuceeDialect() {
		return allowLuceeDialect;
	}

	public void setAllowLuceeDialect(boolean allowLuceeDialect) {
		this.allowLuceeDialect = allowLuceeDialect;
	}

	/*
	 * public boolean installExtension(ExtensionDefintion ed) throws PageException { return
	 * DeployHandler.deployExtension(this, ed, getLog("deploy"),true); }
	 */

	private Map<String, ClassDefinition> cacheDefinitions;

	public void setCacheDefinitions(Map<String, ClassDefinition> caches) {
		this.cacheDefinitions = caches;
	}

	@Override
	public Map<String, ClassDefinition> getCacheDefinitions() {
		return this.cacheDefinitions;
	}

	@Override
	public ClassDefinition getCacheDefinition(String className) {
		return this.cacheDefinitions.get(className);
	}

	@Override
	public Resource getAntiSamyPolicy() {
		return getConfigDir().getRealResource("security/antisamy-basic.xml");
	}

	protected abstract void setGatewayEntries(Map<String, GatewayEntry> gatewayEntries);

	public abstract Map<String, GatewayEntry> getGatewayEntries();

	private ClassDefinition wsHandlerCD;
	protected WSHandler wsHandler = null;

	protected void setWSHandlerClassDefinition(ClassDefinition cd) {
		this.wsHandlerCD = cd;
		wsHandler = null;
	}

	// public abstract WSHandler getWSHandler() throws PageException;

	protected ClassDefinition getWSHandlerClassDefinition() {
		return wsHandlerCD;
	}

	boolean isEmpty(ClassDefinition cd) {
		return cd == null || StringUtil.isEmpty(cd.getClassName());
	}

	private boolean fullNullSupport = false;

	protected final void setFullNullSupport(boolean fullNullSupport) {
		this.fullNullSupport = fullNullSupport;
	}

	@Override
	public final boolean getFullNullSupport() {
		return fullNullSupport;
	}

	private LogEngine logEngine;

	@Override
	public LogEngine getLogEngine() {
		if (logEngine == null) logEngine = LogEngine.getInstance(this);
		return logEngine;
	}

	protected void setCachedAfterTimeRange(TimeSpan ts) {
		this.cachedAfterTimeRange = ts;
	}

	@Override
	public TimeSpan getCachedAfterTimeRange() {
		if (this.cachedAfterTimeRange != null && this.cachedAfterTimeRange.getMillis() <= 0) this.cachedAfterTimeRange = null;
		return this.cachedAfterTimeRange;
	}

	@Override
	public Map<String, Startup> getStartups() {
		if (startups == null) startups = new HashMap<>();
		return startups;
	}

	@Override
	public Regex getRegex() {
		if (regex == null) regex = RegexFactory.toRegex(RegexFactory.TYPE_PERL, null);
		return regex;
	}

	protected void setRegex(Regex regex) {
		this.regex = regex;
	}
}