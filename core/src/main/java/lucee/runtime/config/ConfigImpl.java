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

import static lucee.runtime.db.DatasourceManagerImpl.QOQ_DATASOURCE_NAME;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.commons.date.TimeZoneConstants;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogEngine;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.LoggerAndSourceData;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.ResourcesImpl.ResourceProviderFactory;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.type.compress.Compress;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ByteSizeParser;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.PhysicalClassLoaderFactory;
import lucee.commons.lang.SerializableObject;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.net.IPRange;
import lucee.loader.TP;
import lucee.runtime.CIPage;
import lucee.runtime.Component;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.ram.RamCache;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.cfx.customtag.CFXTagPoolImpl;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.component.ImportDefintionImpl;
import lucee.runtime.config.ConfigFactoryImpl.Path;
import lucee.runtime.config.ConfigUtil.CacheElement;
import lucee.runtime.config.gateway.GatewayMap;
import lucee.runtime.customtag.InitFile;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourcePro;
import lucee.runtime.db.DatasourceConnectionFactory;
import lucee.runtime.db.JDBCDriver;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.dump.DumpWriterEntry;
import lucee.runtime.dump.HTMLDumpWriter;
import lucee.runtime.engine.ExecutionLogFactory;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.extension.Extension;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.ExtensionProvider;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.listener.MixedAppListener;
import lucee.runtime.listener.ModernAppListener;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.Duplicator;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.EnvClassLoader;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.regex.Regex;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.rest.RestSettingImpl;
import lucee.runtime.rest.RestSettings;
import lucee.runtime.schedule.Scheduler;
import lucee.runtime.schedule.SchedulerImpl;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecretProvider;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.spooler.SpoolerEngine;
import lucee.runtime.spooler.SpoolerEngineImpl;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.ClusterNotSupported;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.UDFUtil;
import lucee.runtime.video.VideoExecuterNotSupported;
import lucee.transformer.dynamic.meta.Method;
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

	private static final RHExtension[] RHEXTENSIONS_EMPTY = new RHExtension[0];

	private Integer mode;
	private static final double DEFAULT_VERSION = 5.0d;
	private static final long CACHE_DIR_SIZE_DEFAULT = 1024L * 1024L * 100L;

	private final Map<String, PhysicalClassLoader> rpcClassLoaders = new ConcurrentHashMap<String, PhysicalClassLoader>();
	private PhysicalClassLoader directClassLoader;
	private Map<String, DataSource> datasourcesAll;
	private Map<String, DataSource> datasourcesNoQoQ;

	private Map<String, CacheConnection> cacheConnection;

	private Map<Integer, String> cacheDefaultConnectionNames = null;
	private Map<Integer, CacheConnection> cacheDefaultConnection = null;

	private TagLib[] cfmlTlds;
	private FunctionLib cfmlFlds;

	private Resource tldFile;
	private Resource fldFile;

	private Short scopeType;
	private Boolean allowImplicidQueryCall;
	private Boolean limitEvaluation;

	private Boolean mergeFormAndURL;

	private Map<String, LoggerAndSourceData> loggers;

	private Integer debugLogOutput;
	private Integer debugOptions;

	private boolean suppresswhitespace = false;
	private Boolean suppressContent;
	private Boolean showVersion;

	private Resource tempDirectory;
	private boolean tempDirectoryReload;
	private TimeSpan clientTimeout;
	private TimeSpan sessionTimeout;
	private TimeSpan applicationTimeout;
	private TimeSpan requestTimeout;

	private Boolean sessionManagement;
	private Boolean clientManagement;
	private Boolean clientCookies;
	private Boolean developMode;
	private Boolean domainCookies;

	private Resource configFile;
	private Resource configDir;
	private String sessionStorage;
	private String clientStorage;

	private long loadTime;

	private int spoolInterval = -1;
	private Boolean spoolEnable;
	private Boolean sendPartial;
	private Boolean userSet;

	private Server[] mailServers;

	private int mailTimeout = -1;

	private Integer returnFormat;

	private TimeZone timeZone;

	private long timeOffset;

	private ClassDefinition<SearchEngine> searchEngineClassDef;
	private String searchEngineDirectory;

	private Locale locale;

	private Boolean psq;
	private boolean debugShowUsage;

	private Map<String, String> errorTemplates;

	protected Password password;
	private boolean initPassword = true;
	private String salt;

	private Mapping[] uncheckedMappings;
	private Mapping[] mappings;
	private Mapping[] uncheckedCustomTagMappings;
	private Mapping[] customTagMappings;
	private Mapping[] uncheckedComponentMappings;
	private Mapping[] componentMappings;

	private SchedulerImpl scheduler;

	private CFXTagPool cfxTagPool;

	private PageSource baseComponentPageSource;
	private final String baseComponentTemplate = "Component.cfc";
	private Boolean restList;

	private Short clientType;

	private String componentDumpTemplate;
	private Integer componentDataMemberDefaultAccess;
	private Boolean triggerComponentDataMember;

	private Short sessionType;

	private Resource deployDirectory;

	private CharSet resourceCharset;
	private CharSet templateCharset;
	private CharSet webCharset;

	private CharSet mailDefaultCharset;
	private final Resources resources = new ResourcesImpl();
	private boolean initResource = true;
	private Map<String, Class<CacheHandler>> cacheHandlerClasses;

	private ApplicationListener applicationListener;

	private Integer scriptProtect;

	private ResourceProvider defaultResourceProvider;

	private ProxyData proxy = null;

	private Resource clientScopeDir;
	private Resource sessionScopeDir;
	private Long clientScopeDirSize;
	private long sessionScopeDirSize = 1024 * 1024 * 100;

	private Resource cacheDir;
	private Long cacheDirSize;

	private Boolean useComponentShadow;

	private PrintWriter out;
	private PrintWriter err;

	private final Map<String, DatasourceConnPool> pools = new ConcurrentHashMap<>();

	private Boolean doCustomTagDeepSearch = null;
	private Boolean doComponentTagDeepSearch;

	private Double version = null;

	private Boolean closeConnection;
	private Boolean contentLength;
	private Boolean allowCompression;

	private Boolean doLocalCustomTag;

	private Struct constants = null;

	private RemoteClient[] remoteClients;

	private SpoolerEngine remoteClientSpoolerEngine;

	private Resource remoteClientDirectory;
	private Integer remoteClientMaxThreads;

	private Boolean allowURLRequestTimeout;
	private Boolean errorStatusCode;
	private Integer localMode;

	private RHExtensionProvider[] rhextensionProviders;

	private List<ExtensionDefintion> extensionsDefs;
	private RHExtension[] rhextensions = RHEXTENSIONS_EMPTY;
	private String extensionsMD5;
	private Boolean allowRealPath;

	private DumpWriterEntry[] dmpWriterEntries;
	private Class clusterClass = ClusterNotSupported.class;// ClusterRemoteNotSupported.class;//
	private Struct remoteClientUsage;
	private Class adminSyncClass;
	private AdminSync adminSync;
	private String[] customTagExtensions = null;
	private Class videoExecuterClass = VideoExecuterNotSupported.class;

	protected MappingImpl scriptMapping;

	// private Resource tagDirectory;
	protected Mapping defaultFunctionMapping;
	protected final Map<String, Mapping> functionMappings = new ConcurrentHashMap<String, Mapping>();

	protected Mapping defaultTagMapping;
	protected final Map<String, Mapping> tagMappings = new ConcurrentHashMap<String, Mapping>();

	private Boolean typeChecking;

	private Boolean executionLogEnabled;
	private ExecutionLogFactory executionLogFactory;
	private final Map<String, ORMEngine> ormengines = new HashMap<String, ORMEngine>();
	private ClassDefinition<? extends ORMEngine> cdORMEngine;
	private ORMConfiguration ormConfig;

	private ImportDefintion componentDefaultImport;
	private Boolean componentLocalSearch;
	private boolean componentRootSearch = true;
	private Boolean useComponentPathCache;
	private Boolean useCTPathCache;
	private lucee.runtime.rest.Mapping[] restMappings;

	protected Integer writerType;
	private long configFileLastModified;
	private Boolean checkForChangesInConfigFile;
	// protected String apiKey=null;

	private List<Object> consoleLayouts = new ArrayList<>();
	private List<Object> resourceLayouts = new ArrayList<>();

	private Map<Key, Map<Key, Object>> tagDefaultAttributeValues;
	private Boolean handleUnQuotedAttrValueAsString;

	private Map<Integer, Object> cachedWithins;

	private int queueMax = -1;
	private long queueTimeout = -1;
	private Boolean queueEnable;
	private Integer varUsage;

	private TimeSpan cachedAfterTimeRange;
	private boolean initCachedAfterTimeRange = true;

	private static Map<String, Startup> startups;

	private Regex regex; // TODO add possibility to configure

	private Long applicationPathCacheTimeout;
	private ClassLoader envClassLoader;

	private Boolean preciseMath;
	private static Object token = new Object();
	private String mainLoggerName;

	private short compileType = -1;
	private short inspectTemplate = -1;
	private int inspectTemplateAutoIntervalSlow = ConfigPro.INSPECT_INTERVAL_UNDEFINED;
	private int inspectTemplateAutoIntervalFast = ConfigPro.INSPECT_INTERVAL_UNDEFINED;

	private Boolean formUrlAsStruct;

	private Boolean showDebug;
	private Boolean showDoc;
	private Boolean showMetric;
	private Boolean showTest;

	private JavaSettings javaSettings;
	private final Map<String, JavaSettings> javaSettingsInstances = new ConcurrentHashMap<>();

	private Boolean fullNullSupport;

	private Resource extInstalled;
	private Resource extAvailable;

	protected Map<String, AIEngine> aiEngines;

	protected Map<String, SecretProvider> secretProviders;

	private Map<String, ClassDefinition> cacheDefinitions;

	private GatewayMap gatewayEntries;

	private AtomicBoolean insideLoggers = new AtomicBoolean(false);

	private Resource deployDir;

	private Resource antiSamyPolicy;
	private Boolean cgiScopeReadonly;

	private boolean newVersion;
	private Integer debugMaxRecordsLogged;
	private Array scheduledTasks;
	protected Struct root;
	private ComponentPathCache componentPathCache = new ComponentPathCache();

	/**
	 * @return the allowURLRequestTimeout
	 */
	@Override
	public boolean isAllowURLRequestTimeout() {
		if (allowURLRequestTimeout == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isAllowURLRequestTimeout")) {
				if (allowURLRequestTimeout == null) {
					String allowURLReqTimeout = ConfigFactoryImpl.getAttr(this, root, new String[] { "requestTimeoutInURL", "allowUrlRequesttimeout" });
					if (!StringUtil.isEmpty(allowURLReqTimeout)) {
						allowURLRequestTimeout = Caster.toBooleanValue(allowURLReqTimeout, false);
					}
					else allowURLRequestTimeout = false;
				}
			}
		}
		return allowURLRequestTimeout;
	}

	public ConfigImpl resetAllowURLRequestTimeout() {
		if (allowURLRequestTimeout != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isAllowURLRequestTimeout")) {
				if (allowURLRequestTimeout != null) {
					allowURLRequestTimeout = null;
				}
			}
		}
		return this;
	}

	@Override
	public short getCompileType() {
		if (compileType == -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCompileType")) {
				if (compileType == -1) {
					compileType = ConfigFactoryImpl.loadJava(this, root, RECOMPILE_NEVER);
				}
			}
		}
		return compileType;
	}

	public ConfigImpl resetCompileType() {
		if (compileType != -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCompileType")) {
				if (compileType != -1) {
					compileType = -1;
				}
			}
		}
		return this;
	}

	@Override
	@Deprecated
	public void reloadTimeServerOffset() {
		// FUTURE remove method
	}

	/**
	 * private constructor called by factory method
	 * 
	 * @param configDir - config directory
	 * @param configFile - config file
	 */
	protected ConfigImpl(Resource configDir, Resource configFile, boolean newVersion) {
		this.configDir = configDir;
		this.configFile = configFile;
		this.newVersion = newVersion;
	}

	@Override
	public long lastModified() {
		return configFileLastModified;
	}

	@Override
	public void setLastModified() {
		this.configFileLastModified = configFile.lastModified();
	}

	@Override
	public short getScopeCascadingType() {
		if (scopeType == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getScopeCascadingType")) {
				if (scopeType == null) {
					// Cascading
					String strScopeCascadingType = ConfigFactoryImpl.getAttr(this, root, "scopeCascading");
					if (!StringUtil.isEmpty(strScopeCascadingType)) {
						scopeType = ConfigUtil.toScopeCascading(strScopeCascadingType, Config.SCOPE_STANDARD);
					}
					else scopeType = SCOPE_STANDARD;
				}
			}
		}
		return scopeType;
	}

	public ConfigImpl resetScopeCascadingType() {
		if (scopeType != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getScopeCascadingType")) {
				if (scopeType != null) {
					scopeType = null;
				}
			}
		}

		return this;
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

	/**
	 * return all Tag Library Deskriptors
	 * 
	 * @return Array of Tag Library Deskriptors
	 */
	@Override
	public TagLib[] getTLDs() {
		if (cfmlTlds == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTLDs")) {
				if (cfmlTlds == null) {
					ConfigFactoryImpl.loadTag(this, root, newVersion());
				}
			}
		}
		return cfmlTlds;
	}

	public ConfigImpl resetTLDs() {
		if (cfmlTlds != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTLDs")) {
				if (cfmlTlds != null) {
					cfmlTlds = null;
					tldFile = null;
				}
			}
		}
		return this;
	}

	@Override
	public TagLib getCoreTagLib() {
		TagLib[] tlds = getTLDs();
		for (int i = 0; i < tlds.length; i++) {
			if (tlds[i].isCore()) return tlds[i];
		}
		throw new RuntimeException("no core taglib found"); // this should never happen
	}

	protected void setTLDs(TagLib[] tlds) {
		cfmlTlds = tlds;
	}

	@Override
	public boolean allowImplicidQueryCall() {
		if (allowImplicidQueryCall == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "allowImplicidQueryCall")) {
				if (allowImplicidQueryCall == null) {
					Boolean b = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.cascade.to.resultset", null), null);
					if (b == null) b = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, root, "cascadeToResultset"), Boolean.TRUE);
					allowImplicidQueryCall = b;
				}
			}
		}
		return allowImplicidQueryCall;
	}

	public ConfigImpl resetAllowImplicidQueryCall() {
		if (allowImplicidQueryCall != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "allowImplicidQueryCall")) {
				if (allowImplicidQueryCall != null) {
					allowImplicidQueryCall = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean limitEvaluation() {
		if (limitEvaluation == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "limitEvaluation")) {
				if (limitEvaluation == null) {

					Boolean b = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.security.limitEvaluation", null), null);
					if (b == null) b = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.security.isdefined", null), null);
					if (b == null) b = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.isdefined.limit", null), null);
					if (b == null) {
						Struct security = ConfigUtil.getAsStruct("security", root);
						if (security != null) {
							b = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, security, "limitEvaluation"), null);
						}
					}
					if (b != null) limitEvaluation = b;
					else limitEvaluation = Boolean.FALSE;

				}
			}
		}
		return limitEvaluation;
	}

	public ConfigImpl resetLimitEvaluation() {
		if (limitEvaluation != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "limitEvaluation")) {
				if (limitEvaluation != null) {
					limitEvaluation = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean mergeFormAndURL() {
		if (mergeFormAndURL == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mergeFormAndURL")) {
				if (mergeFormAndURL == null) {
					String strMergeFormAndURL = ConfigFactoryImpl.getAttr(this, root, "mergeUrlForm");
					if (!StringUtil.isEmpty(strMergeFormAndURL, true)) {
						mergeFormAndURL = Caster.toBoolean(strMergeFormAndURL, Boolean.FALSE);
					}
					else mergeFormAndURL = Boolean.FALSE;
				}
			}
		}
		return mergeFormAndURL;
	}

	public ConfigImpl resetMergeFormAndURL() {
		if (mergeFormAndURL != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mergeFormAndURL")) {
				if (mergeFormAndURL != null) {
					mergeFormAndURL = null;
				}
			}
		}
		return this;
	}

	@Override
	public TimeSpan getApplicationTimeout() {
		if (applicationTimeout == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getApplicationTimeout")) {
				if (applicationTimeout == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "applicationTimeout");
					if (!StringUtil.isEmpty(str, true)) {
						applicationTimeout = Caster.toTimespan(str.trim(), null);
					}
					if (applicationTimeout == null) applicationTimeout = new TimeSpanImpl(1, 0, 0, 0);
				}
			}
		}
		return applicationTimeout;
	}

	public ConfigImpl resetApplicationTimeout() {
		if (applicationTimeout != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getApplicationTimeout")) {
				if (applicationTimeout != null) {
					applicationTimeout = null;
				}
			}
		}
		return this;
	}

	@Override
	public TimeSpan getSessionTimeout() {
		if (sessionTimeout == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSessionTimeout")) {
				if (sessionTimeout == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "sessionTimeout");
					if (!StringUtil.isEmpty(str, true)) {
						sessionTimeout = Caster.toTimespan(str.trim(), null);
					}
					if (sessionTimeout == null) sessionTimeout = new TimeSpanImpl(0, 0, 30, 0);
				}
			}
		}
		return sessionTimeout;
	}

	public ConfigImpl resetSessionTimeout() {
		if (sessionTimeout != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSessionTimeout")) {
				if (sessionTimeout != null) {
					sessionTimeout = null;
				}
			}
		}
		return this;
	}

	@Override
	public TimeSpan getClientTimeout() {
		if (clientTimeout == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClientTimeout")) {
				if (clientTimeout == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "clientTimeout");
					if (!StringUtil.isEmpty(str, true)) {
						clientTimeout = Caster.toTimespan(str.trim(), null);
					}
					if (clientTimeout == null) clientTimeout = new TimeSpanImpl(0, 0, 90, 0);
				}
			}
		}
		return clientTimeout;
	}

	public ConfigImpl resetClientTimeout() {
		if (clientTimeout != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClientTimeout")) {
				if (clientTimeout != null) {
					clientTimeout = null;
				}
			}
		}
		return this;
	}

	@Override
	public TimeSpan getRequestTimeout() {
		if (requestTimeout == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRequestTimeout")) {
				if (requestTimeout == null) {
					TimeSpan ts = null;
					String reqTimeout = SystemUtil.getSystemPropOrEnvVar("lucee.requesttimeout", null);
					if (StringUtil.isEmpty(reqTimeout)) reqTimeout = ConfigFactoryImpl.getAttr(this, root, "requesttimeout");
					if (!StringUtil.isEmpty(reqTimeout)) ts = Caster.toTimespan(reqTimeout, null);
					if (ts != null && ts.getMillis() > 0) requestTimeout = ts;
					else requestTimeout = new TimeSpanImpl(0, 0, 0, 50);
				}
			}
		}
		return requestTimeout;
	}

	public ConfigImpl resetRequestTimeout() {
		if (requestTimeout != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRequestTimeout")) {
				if (requestTimeout != null) {
					requestTimeout = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isClientCookies() {
		if (clientCookies == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isClientCookies")) {
				if (clientCookies == null) {
					String strClientCookies = ConfigFactoryImpl.getAttr(this, root, "clientCookies");
					if (!StringUtil.isEmpty(strClientCookies, true)) {
						clientCookies = Caster.toBoolean(strClientCookies, Boolean.TRUE);
					}
					else clientCookies = Boolean.TRUE;
				}
			}
		}
		return clientCookies;
	}

	public ConfigImpl resetClientCookies() {
		if (clientCookies != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isClientCookies")) {
				if (clientCookies != null) {
					clientCookies = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isDevelopMode() {
		if (developMode == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isDevelopMode")) {
				if (developMode == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "developMode");
					if (!StringUtil.isEmpty(str, true)) {
						developMode = Caster.toBoolean(str.trim(), ConfigPro.DEFAULT_DEVELOP_MODE);
					}
					else developMode = ConfigPro.DEFAULT_DEVELOP_MODE;
				}
			}
		}
		return developMode;
	}

	public ConfigImpl resetDevelopMode() {
		if (developMode != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isDevelopMode")) {
				if (developMode != null) {
					developMode = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isClientManagement() {
		if (clientManagement == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isClientManagement")) {
				if (clientManagement == null) {
					String strClientManagement = ConfigFactoryImpl.getAttr(this, root, "clientManagement");
					if (!StringUtil.isEmpty(strClientManagement)) {
						clientManagement = Caster.toBoolean(strClientManagement, Boolean.FALSE);
					}
					else clientManagement = Boolean.FALSE;
				}
			}
		}
		return clientManagement;
	}

	public ConfigImpl resetClientManagement() {
		if (clientManagement != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isClientManagement")) {
				if (clientManagement != null) {
					clientManagement = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isDomainCookies() {
		if (domainCookies == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isDomainCookies")) {
				if (domainCookies == null) {
					String strDomainCookies = ConfigFactoryImpl.getAttr(this, root, "domainCookies");
					if (!StringUtil.isEmpty(strDomainCookies, true)) {
						domainCookies = Caster.toBoolean(strDomainCookies.trim(), Boolean.FALSE);
					}
					else domainCookies = Boolean.FALSE;
				}
			}
		}
		return domainCookies;
	}

	public ConfigImpl resetDomainCookies() {
		if (domainCookies != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isDomainCookies")) {
				if (domainCookies != null) {
					domainCookies = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isSessionManagement() {
		if (sessionManagement == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isSessionManagement")) {
				if (sessionManagement == null) {
					String strSessionManagement = ConfigFactoryImpl.getAttr(this, root, "sessionManagement");
					if (!StringUtil.isEmpty(strSessionManagement, true)) {
						sessionManagement = Caster.toBoolean(strSessionManagement, Boolean.TRUE);
					}
					else sessionManagement = Boolean.TRUE;
				}
			}
		}
		return sessionManagement;
	}

	public ConfigImpl resetSessionManagement() {
		if (sessionManagement != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isSessionManagement")) {
				if (sessionManagement != null) {
					sessionManagement = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isMailSpoolEnable() {
		if (spoolEnable == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (spoolEnable == null) {
					ConfigFactoryImpl.loadMail(this, root);
				}
			}
		}
		return spoolEnable;
	}

	public ConfigImpl resetMailSpoolEnable() {
		if (spoolEnable != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (spoolEnable != null) {
					spoolEnable = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isMailSendPartial() {
		if (sendPartial == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (sendPartial == null) {
					ConfigFactoryImpl.loadMail(this, root);
				}
			}
		}
		return sendPartial;
	}

	public ConfigImpl resetMailSendPartial() {
		if (sendPartial != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (sendPartial != null) {
					sendPartial = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isUserset() {
		if (userSet == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (userSet == null) {
					ConfigFactoryImpl.loadMail(this, root);
				}
			}
		}
		return userSet;
	}

	public ConfigImpl resetUserset() {
		if (userSet != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (userSet != null) {
					userSet = null;
				}
			}
		}
		return this;
	}

	@Override
	public Server[] getMailServers() {
		if (mailServers == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (mailServers == null) {
					ConfigFactoryImpl.loadMail(this, root);
				}
			}
		}
		return mailServers;
	}

	public ConfigImpl resetMailServers() {
		if (mailServers != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (mailServers != null) {
					mailServers = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getMailTimeout() {
		if (mailTimeout == -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (mailTimeout == -1) {
					ConfigFactoryImpl.loadMail(this, root);
				}
			}
		}
		return mailTimeout;
	}

	public ConfigImpl resetMailTimeout() {
		if (mailTimeout != -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (mailTimeout != -1) {
					mailTimeout = -1;
				}
			}
		}
		return this;
	}

	@Override
	public int getQueryVarUsage() {
		if (varUsage == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getQueryVarUsage")) {
				if (varUsage == null) {
					varUsage = ConfigFactoryImpl.loadSecurity(this, root);
				}
			}
		}
		return varUsage;
	}

	public ConfigImpl resetQueryVarUsage() {
		if (varUsage != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getQueryVarUsage")) {
				if (varUsage != null) {
					varUsage = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getPSQL() {
		if (psq == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getPSQL")) {
				if (psq == null) {
					// PSQ
					String strPSQ = ConfigFactoryImpl.getAttr(this, root, "preserveSingleQuote");
					if (StringUtil.isEmpty(strPSQ)) strPSQ = ConfigFactoryImpl.getAttr(this, root, "datasourcePreserveSingleQuotes");
					if (!StringUtil.isEmpty(strPSQ)) {
						psq = Caster.toBoolean(strPSQ, Boolean.FALSE);
					}
					else psq = Boolean.FALSE;
				}
			}
		}
		return psq;
	}

	public ConfigImpl resetPSQL() {
		if (psq != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getPSQL")) {
				if (psq != null) {
					psq = null;
				}
			}
		}
		return this;
	}

	@Override
	public ClassLoader getClassLoader() {
		ClassLoader cl = null;
		try {
			cl = getRPCClassLoader(false);
		}
		catch (IOException e) {}
		if (cl != null) return cl;
		return SystemUtil.getCoreClassLoader();

	}

	// do not remove, ised in Hibernate extension
	@Override
	public ClassLoader getClassLoaderEnv() {
		if (envClassLoader == null) envClassLoader = new EnvClassLoader(this);
		return envClassLoader;
	}

	@Override
	public ClassLoader getClassLoaderCore() {
		return new lucee.commons.lang.ClassLoaderHelper().getClass().getClassLoader();
	}

	@Override
	public ClassLoader getClassLoaderLoader() {
		return new TP().getClass().getClassLoader();
	}

	@Override
	public Locale getLocale() {
		if (locale == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getLocale")) {
				if (locale == null) {
					locale = ConfigFactoryImpl.loadLocale(this, root, Locale.US);
				}
			}
		}
		return locale;
	}

	public ConfigImpl resetLocale() {
		if (locale != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getLocale")) {
				if (locale != null) {
					locale = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean debug() {
		return true;
	}

	@Override
	public boolean getShowDebug() {
		if (showDebug == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getShowDebug")) {
				if (showDebug == null) {
					// monitoring debug
					String str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.showDebug", null);
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "showDebug");

					if (!StringUtil.isEmpty(str)) {
						showDebug = Caster.toBoolean(str, Boolean.FALSE);
					}
					else showDebug = Boolean.FALSE;
				}
			}
		}
		return showDebug;
	}

	public ConfigImpl resetShowDebug() {
		if (showDebug != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getShowDebug")) {
				if (showDebug != null) {
					showDebug = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getShowDoc() {
		if (showDoc == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getShowDoc")) {
				if (showDoc == null) {
					String str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.showDoc", null);
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "showReference");
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "doc");
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "documentation");
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "reference");
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "showDoc");
					if (!StringUtil.isEmpty(str)) {
						showDoc = Caster.toBoolean(str, Boolean.FALSE);
					}
					else showDoc = Boolean.FALSE;
				}
			}
		}
		return showDoc;
	}

	public ConfigImpl resetShowDoc() {
		if (showDoc != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getShowDoc")) {
				if (showDoc != null) {
					showDoc = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getShowMetric() {
		if (showMetric == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getShowMetric")) {
				if (showMetric == null) {
					String str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.showMetric", null);
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "showMetrics");
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "metric");
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "metrics");
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "showMetric");
					if (!StringUtil.isEmpty(str)) {
						showMetric = Caster.toBoolean(str, Boolean.FALSE);
					}
					else showMetric = Boolean.FALSE;
				}
			}
		}
		return this.showMetric;
	}

	public ConfigImpl resetShowMetric() {
		if (showMetric != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getShowMetric")) {
				if (showMetric != null) {
					showMetric = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getShowTest() {
		if (showTest == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getShowTest")) {
				if (showTest == null) {
					String str = SystemUtil.getSystemPropOrEnvVar("lucee.monitoring.showTest", null);
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "showTests");
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "test");
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "showTest");
					if (!StringUtil.isEmpty(str)) {
						showTest = Caster.toBoolean(str, false);
					}
					else showTest = Boolean.FALSE;
				}
			}
		}
		return this.showTest;
	}

	public ConfigImpl resetShowTest() {
		if (showTest != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getShowTest")) {
				if (showTest != null) {
					showTest = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean debugLogOutput() {
		if (debugLogOutput == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "debugLogOutput")) {
				if (debugLogOutput == null) {
					String strDLO = ConfigFactoryImpl.getAttr(this, root, "debuggingLogOutput");
					if (!StringUtil.isEmpty(strDLO)) {
						debugLogOutput = Caster.toBooleanValue(strDLO, false) ? ConfigPro.CLIENT_BOOLEAN_TRUE : ConfigPro.CLIENT_BOOLEAN_FALSE;
					}
					else debugLogOutput = ConfigPro.CLIENT_BOOLEAN_FALSE;
				}
			}
		}
		return debugLogOutput == CLIENT_BOOLEAN_TRUE || debugLogOutput == SERVER_BOOLEAN_TRUE;
	}

	public ConfigImpl resetLogOutput() {
		if (debugLogOutput != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "debugLogOutput")) {
				if (debugLogOutput != null) {
					debugLogOutput = null;
				}
			}
		}

		return this;
	}

	// = SERVER_BOOLEAN_FALSE

	@Override
	public int getMailSpoolInterval() {
		if (spoolInterval == -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (spoolInterval == -1) {
					ConfigFactoryImpl.loadMail(this, root);
				}
			}
		}
		return spoolInterval;
	}

	public ConfigImpl resetMailSpoolInterval() {
		if (spoolInterval != -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (spoolInterval != -1) {
					spoolInterval = -1;
				}
			}
		}
		return this;
	}

	@Override
	public TimeZone getTimeZone() {
		if (timeZone == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTimeZone")) {
				if (timeZone == null) {
					timeZone = ConfigFactoryImpl.loadTimezone(this, root, null);
					if (timeZone == null) timeZone = TimeZone.getDefault();
					// there was no system default, so we use UTC
					if (timeZone == null) {
						timeZone = TimeZoneConstants.UTC;
						TimeZone.setDefault(timeZone);
					}
				}
			}
		}
		return timeZone;
	}

	public ConfigImpl resetTimeZone() {
		if (timeZone != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTimeZone")) {
				if (timeZone != null) {
					timeZone = null;
				}
			}
		}
		return this;
	}

	@Override
	@Deprecated
	public long getTimeServerOffset() {
		return timeOffset;
	}

	/**
	 * @return return the Scheduler
	 */
	@Override
	public Scheduler getScheduler() {
		// MUST reset scheduler
		if (scheduler == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getScheduler")) {
				if (scheduler == null) {
					try {
						scheduler = new SchedulerImpl(ConfigUtil.getEngine(this), this, getScheduledTasks());
					}
					catch (PageException e) {
						try {
							scheduler = new SchedulerImpl(ConfigUtil.getEngine(this), this, new ArrayImpl());
						}
						catch (PageException e1) {}
					}
				}
			}
		}
		return scheduler;
	}

	public ConfigImpl resetScheduler() {
		if (scheduler != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getScheduler")) {
				if (scheduler != null) {
					try {
						scheduler.refresh(getScheduledTasks());
					}
					catch (Exception e) {
						SchedulerImpl tmp = scheduler;
						scheduler = null;
						tmp.stop();
					}

				}
			}
		}
		return this;
	}

	/**
	 * @return gets the password as hash
	 */
	protected Password getPassword() {
		initPassword = true; // TEST PW
		if (initPassword) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getPassword")) {
				if (initPassword) {
					Password pw = PasswordImpl.readFromStruct(this, root, getSalt(), false, true);
					if (pw != null) {
						password = pw;
					}
					initPassword = false;
				}
			}
		}
		return password;
	}

	protected ConfigImpl resetPassword() {
		if (!initPassword) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getPassword")) {
				if (!initPassword) {
					initPassword = true;
					password = null;
				}
			}
		}
		return this;
	}

	@Override
	public Password isPasswordEqual(String password) {
		if (getPassword() == null) return null;
		return ((PasswordImpl) getPassword()).isEqual(this, password);
	}

	@Override
	public boolean hasPassword() {
		return getPassword() != null;
	}

	@Override
	public boolean passwordEqual(Password password) {
		if (getPassword() == null) return false;
		return getPassword().equals(password);
	}

	@Override
	public Mapping[] getMappings() {
		if (mappings == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getMappings")) {
				if (mappings == null) {
					close(this.uncheckedMappings);
					this.mappings = initMappings(this.uncheckedMappings = ConfigFactoryImpl.loadMappings(this, root));
				}
			}
		}
		return mappings;
	}

	@Override
	public ConfigImpl resetMappings() {
		if (mappings != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getMappings")) {
				if (mappings != null) {
					ConfigFactoryImpl.flushPageSourcePool(mappings);
					close(this.uncheckedMappings);
					this.mappings = null;
					this.uncheckedMappings = null;
				}
			}
		}
		return this;
	}

	@Override
	public Mapping[] getCustomTagMappings() {
		if (customTagMappings == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCustomTagMappings")) {
				if (customTagMappings == null) {
					close(this.uncheckedCustomTagMappings);
					this.customTagMappings = initMappings(this.uncheckedCustomTagMappings = ConfigFactoryImpl.loadCustomTagsMappings(this, root));
				}
			}
		}
		return customTagMappings;
	}

	public ConfigImpl resetCustomTagMappings() {
		if (customTagMappings != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCustomTagMappings")) {
				if (customTagMappings != null) {
					ConfigFactoryImpl.flushPageSourcePool(customTagMappings);

					close(this.uncheckedCustomTagMappings);
					this.customTagMappings = null;
					this.uncheckedCustomTagMappings = null;
				}
			}
		}
		return this;
	}

	@Override
	public Mapping[] getComponentMappings() {
		if (componentMappings == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentMappings")) {
				if (componentMappings == null) {
					close(this.uncheckedComponentMappings);
					this.componentMappings = initMappings(this.uncheckedComponentMappings = ConfigFactoryImpl.loadComponentMappings(this, root));
				}
			}
		}
		return componentMappings;
	}

	public ConfigImpl resetComponentMappings() {
		if (componentMappings != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentMappings")) {
				if (componentMappings != null) {
					ConfigFactoryImpl.flushPageSourcePool(componentMappings);
					close(this.uncheckedComponentMappings);
					this.componentMappings = null;
					this.uncheckedComponentMappings = null;
				}
			}
		}
		return this;
	}

	public Array getScheduledTasks() {
		if (scheduledTasks == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getScheduledTasks")) {
				if (scheduledTasks == null) {
					this.scheduledTasks = ConfigUtil.getAsArray("scheduledTasks", root);
					if (this.scheduledTasks == null) this.scheduledTasks = new ArrayImpl();
				}
			}
		}
		return scheduledTasks;
	}

	public ConfigImpl resetScheduledTasks() {
		if (scheduledTasks != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getScheduledTasks")) {
				if (scheduledTasks != null) {
					this.scheduledTasks = null;
				}
			}
		}
		return this;
	}

	public void checkMappings() {
		mappings = initMappings(uncheckedMappings);
		customTagMappings = initMappings(uncheckedCustomTagMappings);
		componentMappings = initMappings(uncheckedComponentMappings);
	}

	private Mapping[] initMappings(Mapping[] mappings) {
		if (mappings == null) return null;
		List<Mapping> list = new ArrayList<Mapping>();
		for (Mapping m: mappings) {
			try {
				m.check();
				list.add(m);
			}
			catch (Exception e) {
				LogUtil.log(this, "mappings", e);
			}
		}
		return list.toArray(new Mapping[list.size()]);
	}

	protected void close(Mapping[] mappings) {
		if (mappings != null) {
			for (Mapping m: mappings) {
				if (m instanceof MappingImpl) ((MappingImpl) m).close();
			}
		}
	}

	@Override
	public lucee.runtime.rest.Mapping[] getRestMappings() {
		if (restMappings == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRestMappings")) {
				if (restMappings == null) {
					restMappings = ConfigFactoryImpl.loadRestMappings(this, root);
				}
			}
		}
		return restMappings;
	}

	@Override
	public ConfigImpl resetRestMappings() {
		if (restMappings != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRestMappings")) {
				if (restMappings != null) {
					restMappings = null;
				}
			}
		}
		return this;
	}

	@Override
	public PageSource getPageSource(Mapping[] mappings, String realPath, boolean onlyTopLevel) {
		throw new PageRuntimeException(new DeprecatedException("method not supported"));
	}

	@Override
	public PageSource getPageSourceExisting(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean onlyPhysicalExisting) {
		return ConfigUtil.getPageSourceExisting(pc, this, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, onlyPhysicalExisting);
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

	public PageSource[] getPageSources(PageContext pc, Mapping[] appMappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean useComponentMappings, boolean onlyFirstMatch) {
		return ConfigUtil.getPageSources(pc, this, appMappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, useComponentMappings, onlyFirstMatch);
	}

	@Override
	public Resource[] getResources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean useComponentMappings, boolean onlyFirstMatch) {
		return ConfigUtil.getResources(pc, this, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, useComponentMappings, onlyFirstMatch);
	}

	@Override
	public PageSource toPageSource(Mapping[] mappings, Resource res, PageSource defaultValue) {
		return ConfigUtil.toPageSource(this, mappings, res, defaultValue);
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
	@Override
	public void setPassword(Password password) {
		this.password = password;
	}

	/**
	 * set the optional directory of the tag library deskriptors
	 * 
	 * @param fileTld directory of the tag libray deskriptors
	 * @throws TagLibException
	 */
	protected void setTldFile(Resource fileTld) throws TagLibException {
		TagLib[] tlds = cfmlTlds;

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
		cfmlTlds = tlds;

		int index = 0;
		Iterator<TagLib> it = map.values().iterator();
		while (it.hasNext()) {
			tlds[index++] = it.next();
		}
	}

	protected void setTagDirectory(List<Path> listTagDirectory) {
		Iterator<Path> it = listTagDirectory.iterator();
		int index = -1;
		String mappingName;
		Path path;
		Mapping m;
		boolean isDefault;
		while (it.hasNext()) {
			path = it.next();
			index++;
			isDefault = index == 0;
			mappingName = "/mapping-tag" + (isDefault ? "" : index) + "";

			m = new MappingImpl(this, mappingName, path.isValidDirectory() ? path.res.getAbsolutePath() : path.str, null, ConfigPro.INSPECT_AUTO, 60000, 1000, true, true, true,
					true, false, true, null, -1, -1);
			if (isDefault) defaultTagMapping = m;
			tagMappings.put(mappingName, m);

			TagLib tlc = getCoreTagLib();

			// now overwrite with new data
			if (path.res.isDirectory()) {
				String[] files = path.res.list(new ExtensionResourceFilter(getMode() == ConfigPro.MODE_STRICT ? Constants.getComponentExtensions() : Constants.getExtensions()));
				for (int i = 0; i < files.length; i++) {
					if (tlc != null) createTag(tlc, files[i], mappingName);
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

	protected void setFunctionDirectory(List<Path> listFunctionDirectory) {
		Iterator<Path> it = listFunctionDirectory.iterator();
		int index = -1;
		String mappingName;
		Path path;
		boolean isDefault;
		while (it.hasNext()) {
			path = it.next();
			index++;
			isDefault = index == 0;
			mappingName = "/mapping-function" + (isDefault ? "" : index) + "";
			MappingImpl mapping = new MappingImpl(this, mappingName, (path.isValidDirectory() ? path.res.getAbsolutePath() : path.str), null, ConfigPro.INSPECT_AUTO, 60000, 1000,
					true, true, true, true, false, true, null, -1, -1);
			if (isDefault) defaultFunctionMapping = mapping;
			this.functionMappings.put(mappingName, mapping);

			// now overwrite with new data
			if (path.res != null && path.res.isDirectory()) {
				String[] files = path.res.list(new ExtensionResourceFilter(Constants.getTemplateExtensions()));

				for (String file: files) {
					if (cfmlFlds != null) createFunction(cfmlFlds, file, mappingName);

				}
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

	protected void setFLDs(FunctionLib flds) {
		cfmlFlds = flds;
	}

	/**
	 * return all Function Library Deskriptors
	 * 
	 * @return Array of Function Library Deskriptors
	 */
	@Override
	public FunctionLib getFLDs() {
		if (cfmlFlds == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getFLDs")) {
				if (cfmlFlds == null) {
					// TODO make some kind of pre state in case root is empty
					ConfigFactoryImpl.loadFunctions(this, root, newVersion());
				}
			}
		}
		return cfmlFlds;
	}

	public ConfigImpl resetFLDs() {
		if (cfmlFlds != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getFLDs")) {
				if (cfmlFlds != null) {
					cfmlFlds = null;
					fldFile = null;
				}
			}
		}
		return this;
	}

	@Override
	@Deprecated
	public FunctionLib[] getFLDs(int dialect) { // used in the image extension
		return new FunctionLib[] { getFLDs() };
	}

	protected void setFldFile(Resource fileFld) throws FunctionLibException {
		if (fileFld == null) return;
		this.fldFile = fileFld;

		// overwrite with additional functions
		FunctionLib fl;
		if (fileFld.isDirectory()) {
			Resource[] files = fileFld.listResources(new ExtensionResourceFilter(new String[] { "fld", "fldx" }));
			for (int i = 0; i < files.length; i++) {
				try {
					fl = FunctionLibFactory.loadFromFile(files[i], getIdentification());

					overwrite(cfmlFlds, fl);

				}
				catch (FunctionLibException fle) {
					LogUtil.log(this, Log.LEVEL_ERROR, "loading", "can't load fld " + files[i]);
					fle.printStackTrace(getErrWriter());
				}
			}
		}
		else {
			fl = FunctionLibFactory.loadFromFile(fileFld, getIdentification());
			overwrite(cfmlFlds, fl);
		}
	}

	@Override
	public Resource getFldFile() {
		getFLDs(); // will trigger the load of this variable

		return fldFile;
	}

	private void overwrite(FunctionLib existingFL, FunctionLib newFL) {
		Iterator<FunctionLibFunction> it = newFL.getFunctions().values().iterator();
		while (it.hasNext()) {
			existingFL.setFunction(it.next());
		}
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

	@Override
	public Resource getTempDirectory() {
		if (tempDirectory == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTempDirectory")) {
				if (tempDirectory == null) {
					try {
						Resource configDir = getConfigDir();
						String strTempDirectory = ConfigUtil.translateOldPath(ConfigFactoryImpl.getAttr(this, root, "tempDirectory"));

						Resource cst = null;
						// Temp Dir
						if (!StringUtil.isEmpty(strTempDirectory)) {
							cst = ConfigUtil.getFile(configDir, strTempDirectory, null, configDir, FileUtil.TYPE_DIR, ResourceUtil.LEVEL_GRAND_PARENT_FILE, this);
						}
						if (cst == null) {
							cst = ConfigUtil.getFile(configDir, "temp", null, configDir, FileUtil.TYPE_DIR, ResourceUtil.LEVEL_GRAND_PARENT_FILE, this);
						}

						if (!isDirectory(cst) || !cst.isWriteable()) {
							LogUtil.log(this, Log.LEVEL_ERROR, "loading",
									"temp directory [" + cst + "] is not writable or can not be created, using directory [" + SystemUtil.getTempDirectory() + "] instead");

							cst = SystemUtil.getTempDirectory();
							if (!cst.isWriteable()) {
								LogUtil.log(this, Log.LEVEL_ERROR, "loading", "temp directory [" + cst + "] is not writable");
							}
							if (!cst.exists()) cst.mkdirs();

						}
						if (!tempDirectoryReload) ResourceUtil.removeChildrenEL(cst, false);// start with an empty temp directory
						this.tempDirectory = cst;

					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						ConfigFactoryImpl.log(this, t);
					}
				}
			}
		}
		return tempDirectory;
	}

	public ConfigImpl resetTempDirectory() {
		if (tempDirectory != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTempDirectory")) {
				if (tempDirectory != null) {
					tempDirectory = null;
					tempDirectoryReload = true;
				}
			}
		}
		return this;
	}

	/**
	 * sets the Schedule Directory
	 * 
	 * @param scheduleDirectory sets the schedule Directory
	 * @param logger
	 * @throws PageException
	 */

	/**
	 * @param spoolInterval The spoolInterval to set.
	 */
	protected void setMailSpoolInterval(int spoolInterval) {
		this.spoolInterval = spoolInterval;
	}

	@Override
	public Collection<String> getAIEngineNames() {
		return getAIEngines().keySet();
	}

	@Override
	public AIEngine getAIEngine(String name) {
		return getAIEngines().get(name);
	}

	private Map<String, AIEngine> getAIEngines() {
		if (aiEngines == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getAIEngineFactories")) {
				if (aiEngines == null) {
					aiEngines = ConfigFactoryImpl.loadAI(this, root, null);
					if (aiEngines == null) aiEngines = new HashMap<>();
				}
			}
		}
		return aiEngines;
	}

	public ConfigImpl resetAIEngineFactories() {
		if (aiEngines != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getAIEngineFactories")) {
				if (aiEngines != null) {
					aiEngines = null;
				}
			}
		}
		return this;
	}

	@Override
	public SecretProvider getSecretProvider(String name) throws ApplicationException {
		SecretProvider sp = getSecretProviders().get(name.toLowerCase().trim());
		if (sp != null) return sp;
		throw new ApplicationException("there is no secret provider for the name [" + name + "]");
	}

	@Override
	public Map<String, SecretProvider> getSecretProviders() {
		if (secretProviders == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSecretProviders")) {
				if (secretProviders == null) {
					secretProviders = ConfigFactoryImpl.loadSecretProviders(this, root, null);
				}
			}
		}
		return secretProviders;
	}

	public ConfigImpl resetSecretProviders() {
		if (secretProviders != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSecretProviders")) {
				if (secretProviders != null) {
					secretProviders = null;
				}
			}
		}
		return this;
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
		if (cfxTagPool == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCFXTagPool")) {
				if (cfxTagPool == null) {
					cfxTagPool = new CFXTagPoolImpl(ConfigFactoryImpl.loadCFX(this, root));
				}
			}
		}
		return cfxTagPool;
	}

	public ConfigImpl resetCFXTagPool() {
		if (cfxTagPool != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCFXTagPool")) {
				if (cfxTagPool != null) {
					cfxTagPool = null;
				}
			}
		}
		return this;
	}

	@Override
	public PageSource getBaseComponentPageSource(PageContext pc, boolean force) {
		PageSource base = force ? null : baseComponentPageSource;

		if (base == null) {
			synchronized (SystemUtil.createToken("dialect", "")) {
				base = force ? null : baseComponentPageSource;
				if (base == null) {

					// package
					ImportDefintion di = getComponentDefaultImport();
					String pack = di == null ? null : di.getPackageAsPath();
					if (StringUtil.isEmpty(pack, true)) pack = "";
					else if (!pack.endsWith("/")) pack += "";
					// name
					String componentName = getBaseComponentTemplate();

					Mapping[] mappigs = getComponentMappings();
					if (!ArrayUtil.isEmpty(mappigs)) {
						PageSource ps;
						outer: do {
							for (Mapping m: mappigs) {
								ps = m.getPageSource(pack + componentName);
								if (ps.exists()) {
									base = ps;
									break outer;
								}
							}
							for (Mapping m: mappigs) {
								ps = m.getPageSource(componentName);
								if (ps.exists()) {
									base = ps;
									break outer;
								}
							}
							for (Mapping m: mappigs) {
								ps = m.getPageSource("org/lucee/cfml/" + componentName);
								if (ps.exists()) {
									base = ps;
									break outer;
								}
							}
						}
						while (false);
					}
					if (base == null) {
						StringBuilder detail;
						if (ArrayUtil.isEmpty(mappigs)) {
							detail = new StringBuilder("There are no components mappings available!");
						}
						else {
							detail = new StringBuilder();
							for (Mapping m: mappigs) {
								if (detail.length() > 0) detail.append(", ");
								else detail.append("The following component mappings are available [");

								Resource p = m.getPhysical();
								String physical = m.getStrPhysical();
								if (p != null) {
									try {
										physical = p.getCanonicalPath() + " (" + m.getStrPhysical() + ")";
									}
									catch (IOException e) {}
								}

								Resource a = m.getArchive();
								String archive = m.getStrArchive();
								if (p != null) {
									try {
										archive = a.getCanonicalPath() + " (" + m.getStrArchive() + ")";
									}
									catch (IOException e) {}
								}

								detail.append(physical).append(':').append(archive);
							}
							detail.append("]");
						}
						LogUtil.log(Log.LEVEL_ERROR, "component",
								"could not load the base component Component, it was not found in any of the component mappings." + detail.toString());

					}
					else {
						this.baseComponentPageSource = base;
					}
				}
			}
		}
		return base;
	}

	@Override
	public String getBaseComponentTemplate() {
		return baseComponentTemplate;
	}

	@Override
	public boolean getRestList() {
		if (restList == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRestList")) {
				if (restList == null) {
					Struct rest = ConfigUtil.getAsStruct("rest", root);
					restList = rest != null ? Caster.toBoolean(ConfigFactoryImpl.getAttr(this, rest, "list"), Boolean.FALSE) : Boolean.FALSE;
				}
			}
		}
		return restList;
	}

	public ConfigImpl resetRestList() {
		if (restList != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRestList")) {
				if (restList != null) {
					restList = null;
				}
			}
		}
		return this;
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
		if (clientType == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClientType")) {
				if (clientType == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "clientType");
					if (!StringUtil.isEmpty(str, true)) {
						str = str.trim().toLowerCase();
						if (str.equals("file")) clientType = Config.CLIENT_SCOPE_TYPE_FILE;
						else if (str.equals("db")) clientType = Config.CLIENT_SCOPE_TYPE_DB;
						else if (str.equals("database")) clientType = Config.CLIENT_SCOPE_TYPE_DB;
						else clientType = Config.CLIENT_SCOPE_TYPE_COOKIE;
					}
					else clientType = Config.CLIENT_SCOPE_TYPE_COOKIE;
				}
			}
		}
		return this.clientType;
	}

	public ConfigImpl resetClientType() {
		if (clientType != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClientType")) {
				if (clientType != null) {
					clientType = null;
				}

			}
		}
		return this;
	}

	@Override
	public ClassDefinition<SearchEngine> getSearchEngineClassDefinition() {
		if (searchEngineClassDef == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSearchEngineClassDefinition")) {
				if (searchEngineClassDef == null) {
					searchEngineClassDef = ConfigFactoryImpl.loadSearchClass(this, root);
				}
			}
		}
		return this.searchEngineClassDef;
	}

	public ConfigImpl resetSearchEngineClassDefinition() {
		if (searchEngineClassDef != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSearchEngineClassDefinition")) {
				if (searchEngineClassDef != null) {
					searchEngineClassDef = null;
				}
			}
		}
		return this;
	}

	@Override
	public String getSearchEngineDirectory() {
		if (searchEngineDirectory == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSearchEngineDirectory")) {
				if (searchEngineDirectory == null) {
					searchEngineDirectory = ConfigFactoryImpl.loadSearchDir(this, root);
				}
			}
		}
		return this.searchEngineDirectory;
	}

	public ConfigImpl resetSearchEngineDirectory() {
		if (searchEngineDirectory != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSearchEngineDirectory")) {
				if (searchEngineDirectory != null) {
					searchEngineDirectory = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getComponentDataMemberDefaultAccess() {
		if (componentDataMemberDefaultAccess == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentDataMemberDefaultAccess")) {
				if (componentDataMemberDefaultAccess == null) {

					String strDmda = ConfigFactoryImpl.getAttr(this, root, "componentDataMemberAccess");
					if (!StringUtil.isEmpty(strDmda, true)) {
						strDmda = strDmda.toLowerCase().trim();
						if (strDmda.equals("remote")) componentDataMemberDefaultAccess = Component.ACCESS_REMOTE;
						else if (strDmda.equals("public")) componentDataMemberDefaultAccess = Component.ACCESS_PUBLIC;
						else if (strDmda.equals("package")) componentDataMemberDefaultAccess = Component.ACCESS_PACKAGE;
						else if (strDmda.equals("private")) componentDataMemberDefaultAccess = Component.ACCESS_PRIVATE;
						else componentDataMemberDefaultAccess = Component.ACCESS_PUBLIC;
					}
					else {
						componentDataMemberDefaultAccess = Component.ACCESS_PUBLIC;
					}
				}
			}
		}
		return componentDataMemberDefaultAccess;
	}

	public ConfigImpl resetComponentDataMemberDefaultAccess() {
		if (componentDataMemberDefaultAccess != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentDataMemberDefaultAccess")) {
				if (componentDataMemberDefaultAccess != null) {
					componentDataMemberDefaultAccess = null;
				}
			}
		}
		return this;
	}
	// = Component.ACCESS_PUBLIC

	@Override
	@Deprecated
	public String getTimeServer() {
		return "";
	}

	@Override
	public String getComponentDumpTemplate() {
		if (componentDumpTemplate == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentDumpTemplate")) {
				if (componentDumpTemplate == null) {
					String strDumpRemplate = ConfigFactoryImpl.getAttr(this, root, "componentDumpTemplate");
					if (StringUtil.isEmpty(strDumpRemplate, true)) {
						componentDumpTemplate = "/lucee/component-dump.cfm";
					}
					else componentDumpTemplate = strDumpRemplate.trim();

				}
			}
		}
		return componentDumpTemplate;
	}

	public ConfigImpl resetComponentDumpTemplate() {
		if (componentDumpTemplate != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentDumpTemplate")) {
				if (componentDumpTemplate != null) {
					componentDumpTemplate = null;
				}
			}
		}
		return this;
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

		if (errorTemplates == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getErrorTemplate")) {
				if (errorTemplates == null) {
					Map<String, String> tmp = new HashMap<String, String>();
					boolean hasAccess = ConfigUtil.hasAccess(this, SecurityManager.TYPE_DEBUGGING);

					// 500
					String template500 = ConfigFactoryImpl.getAttr(this, root, "errorGeneralTemplate");
					if (StringUtil.isEmpty(template500)) template500 = ConfigFactoryImpl.getAttr(this, root, "generalErrorTemplate");
					if (hasAccess && !StringUtil.isEmpty(template500)) tmp.put("500", template500);
					else tmp.put("500", "/lucee/templates/error/error." + (Constants.getCFMLTemplateExtensions()[0]));

					// 404
					String template404 = ConfigFactoryImpl.getAttr(this, root, "errorMissingTemplate");
					if (StringUtil.isEmpty(template404)) template404 = ConfigFactoryImpl.getAttr(this, root, "missingErrorTemplate");
					if (hasAccess && !StringUtil.isEmpty(template404)) tmp.put("404", template404);
					else tmp.put("404", "/lucee/templates/error/error." + (Constants.getCFMLTemplateExtensions()[0]));

					errorTemplates = tmp;
				}
			}
		}
		return errorTemplates.get(Caster.toString(statusCode));
	}

	public ConfigImpl resetErrorTemplates() {
		if (errorTemplates != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getErrorTemplate")) {
				if (errorTemplates != null) {
					errorTemplates = null;
				}
			}
		}
		return this;
	}

	@Override
	public short getSessionType() {
		if (sessionType == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSessionType")) {
				if (sessionType == null) {
					// Session-Type
					String strSessionType = ConfigFactoryImpl.getAttr(this, root, "sessionType");
					if (!StringUtil.isEmpty(strSessionType, true)) {
						sessionType = AppListenerUtil.toSessionType(strSessionType.trim(), Config.SESSION_TYPE_APPLICATION);
					}
					else sessionType = SESSION_TYPE_APPLICATION;
				}
			}
		}
		return sessionType;
	}

	public ConfigImpl resetSessionType() {
		if (sessionType != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSessionType")) {
				if (sessionType != null) {
					sessionType = null;
				}
			}
		}
		return this;
	}

	@Override
	public abstract String getUpdateType();

	@Override
	public abstract URL getUpdateLocation();

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

	@Override
	public Resource getClassDirectory() {
		if (deployDirectory == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClassDirectory")) {
				if (deployDirectory == null) {
					String strDeployDirectory = null;
					Struct fileSystem = ConfigUtil.getAsStruct("fileSystem", root);
					if (fileSystem != null) {
						strDeployDirectory = ConfigUtil.translateOldPath(ConfigFactoryImpl.getAttr(this, fileSystem, "deployDirectory"));
					}
					deployDirectory = ConfigUtil.getFile(configDir, strDeployDirectory, "cfclasses", configDir, FileUtil.TYPE_DIR, ResourceUtil.LEVEL_GRAND_PARENT_FILE, this);
				}
			}
		}
		return deployDirectory;
	}

	public ConfigImpl resetClassDirectory() {
		if (deployDirectory != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClassDirectory")) {
				if (deployDirectory != null) {
					deployDirectory = null;
				}
			}
		}
		return this;
	}

	@Override
	public abstract Resource getRootDirectory();

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
		if (suppressContent == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isSuppressContent")) {
				if (suppressContent == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "suppressContent");
					if (!StringUtil.isEmpty(str, true)) {
						suppressContent = Caster.toBoolean(str, Boolean.FALSE);
					}
					else suppressContent = Boolean.FALSE;
				}
			}
		}
		return suppressContent;
	}

	public ConfigImpl resetSuppressContent() {
		if (suppressContent != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isSuppressContent")) {
				if (suppressContent != null) {
					suppressContent = null;
				}
			}
		}
		return this;
	}

	@Override
	public String getDefaultEncoding() {
		return getWebCharset().name();
	}

	@Override
	public Charset getTemplateCharset() {
		return CharsetUtil.toCharset(getTemplateCharSet());
	}

	public CharSet getTemplateCharSet() {
		if (templateCharset == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTemplateCharSet")) {
				if (templateCharset == null) {
					String template = SystemUtil.getSystemPropOrEnvVar("lucee.template.charset", null);
					if (StringUtil.isEmpty(template)) template = ConfigFactoryImpl.getAttr(this, root, "templateCharset");
					if (!StringUtil.isEmpty(template)) templateCharset = CharsetUtil.toCharSet(template, null);

					if (templateCharset == null) templateCharset = SystemUtil.getCharSet();
				}
			}
		}
		return templateCharset;
	}

	public ConfigImpl resetTemplateCharSet() {
		if (templateCharset != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTemplateCharSet")) {
				if (templateCharset != null) {
					templateCharset = null;
				}
			}
		}
		return this;
	}

	@Override
	public Charset getWebCharset() {
		return CharsetUtil.toCharset(getWebCharSet());
	}

	@Override
	public CharSet getWebCharSet() {
		if (webCharset == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getWebCharSet")) {
				if (webCharset == null) {
					// web
					String web = SystemUtil.getSystemPropOrEnvVar("lucee.web.charset", null);
					if (StringUtil.isEmpty(web)) web = ConfigFactoryImpl.getAttr(this, root, "webCharset");
					if (!StringUtil.isEmpty(web)) webCharset = CharsetUtil.toCharSet(web, null);

					if (webCharset == null) webCharset = CharSet.UTF8;
				}
			}
		}
		return webCharset;
	}

	public ConfigImpl resetWebCharSet() {
		if (webCharset != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getWebCharSet")) {
				if (webCharset != null) {
					webCharset = null;
				}
			}
		}
		return this;
	}

	@Override
	public Charset getResourceCharset() {
		return CharsetUtil.toCharset(getResourceCharSet());
	}

	@Override
	public CharSet getResourceCharSet() {// = SystemUtil.getCharSet()
		if (resourceCharset == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getResourceCharSet")) {
				if (resourceCharset == null) {
					String resource = null;
					resource = SystemUtil.getSystemPropOrEnvVar("lucee.resource.charset", null);
					if (StringUtil.isEmpty(resource)) resource = ConfigFactoryImpl.getAttr(this, root, "resourceCharset");
					if (!StringUtil.isEmpty(resource)) resourceCharset = CharsetUtil.toCharSet(resource, null);
					if (resourceCharset == null) resourceCharset = SystemUtil.getCharSet();
				}
			}
		}
		return resourceCharset;
	}

	public ConfigImpl resetResourceCharSet() {// = SystemUtil.getCharSet()
		if (resourceCharset != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getResourceCharSet")) {
				if (resourceCharset != null) {
					resourceCharset = null;
				}
			}
		}
		return this;
	}

	@Override
	public Resource getTldFile() {
		getTLDs();
		return tldFile;
	}

	@Override
	public Map<String, DataSource> getDataSourcesAsMap() {
		if (datasourcesNoQoQ == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDataSources")) {
				if (datasourcesNoQoQ == null) {
					Map<String, DataSource> map = new HashMap<String, DataSource>();
					Iterator<Entry<String, DataSource>> it = getDataSourcesAll().entrySet().iterator();
					Entry<String, DataSource> entry;
					while (it.hasNext()) {
						entry = it.next();
						if (!entry.getKey().equals(QOQ_DATASOURCE_NAME)) map.put(entry.getKey(), entry.getValue());
					}
					datasourcesNoQoQ = map;
				}
			}
		}
		return datasourcesNoQoQ;
	}

	private Map<String, DataSource> getDataSourcesAll() {
		if (datasourcesAll == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDataSources")) {
				if (datasourcesAll == null) {
					datasourcesAll = ConfigFactoryImpl.loadDataSources(this, root);
				}
			}
		}
		return datasourcesAll;
	}

	public ConfigImpl resetDataSources() {
		if (datasourcesAll != null || datasourcesNoQoQ != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDataSources")) {
				if (datasourcesAll != null || datasourcesNoQoQ != null) {
					datasourcesAll = null;
					datasourcesNoQoQ = null;
				}
			}
		}
		return this;
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
	public DataSource getDataSource(String datasource) throws DatabaseException {
		DataSource ds = (datasource == null) ? null : (DataSource) getDataSourcesAll().get(datasource.toLowerCase());
		if (ds != null) return ds;

		// create error detail
		DatabaseException de = new DatabaseException("datasource [" + datasource + "] doesn't exist", null, null, null);
		de.setDetail(ExceptionUtil.createSoundexDetail(datasource, getDataSourcesAll().keySet().iterator(), "datasource names"));
		de.setAdditional(KeyConstants._Datasource, datasource);
		throw de;
	}

	@Override
	public DataSource getDataSource(String datasource, DataSource defaultValue) {
		DataSource ds = (datasource == null) ? null : (DataSource) getDataSourcesAll().get(datasource.toLowerCase());
		if (ds != null) return ds;
		return defaultValue;
	}

	/**
	 * @return the mailDefaultCharset
	 */
	@Override
	public Charset getMailDefaultCharset() {
		return getMailDefaultCharSet().toCharset();
	}

	public CharSet getMailDefaultCharSet() {
		if (mailDefaultCharset == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (mailDefaultCharset == null) {
					ConfigFactoryImpl.loadMail(this, root);
				}
			}
		}
		return mailDefaultCharset;
	}

	public ConfigImpl resetMailDefaultCharSet() {
		if (mailDefaultCharset != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "mail")) {
				if (mailDefaultCharset != null) {
					mailDefaultCharset = null;
				}
			}
		}
		return this;
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

	/**
	 * @param defaultResourceProvider the defaultResourceProvider to set
	 */
	protected void setDefaultResourceProvider(ResourceProvider defaultResourceProvider) {
		if (defaultResourceProvider == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheHandlers")) {
				if (cacheHandlerClasses == null) {
					cacheHandlerClasses = ConfigFactoryImpl.loadCacheHandler(this, root);
				}
			}
		}
		getResources().registerDefaultResourceProvider(defaultResourceProvider);
	}

	/**
	 * @return the defaultResourceProvider
	 */
	@Override
	public ResourceProvider getDefaultResourceProvider() {
		Resources resources = getResources();
		if (defaultResourceProvider == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDefaultResourceProvider")) {
				if (defaultResourceProvider == null) {
					defaultResourceProvider = ConfigFactoryImpl.loadDefaultResourceProvider(this, root);
					if (defaultResourceProvider == null) defaultResourceProvider = ResourcesImpl.getFileResourceProvider();
					if (defaultResourceProvider != resources.getDefaultResourceProvider()) resources.registerDefaultResourceProvider(defaultResourceProvider);
				}
			}
		}
		return resources.getDefaultResourceProvider();
	}

	public ConfigImpl resetDefaultResourceProvider() {
		if (defaultResourceProvider != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDefaultResourceProvider")) {
				if (defaultResourceProvider != null) {
					defaultResourceProvider = null;
					getResources().registerDefaultResourceProvider(ResourcesImpl.getFileResourceProvider());
				}
			}
		}
		return this;
	}

	@Override
	public Iterator<Entry<String, Class<CacheHandler>>> getCacheHandlers() {
		if (cacheHandlerClasses == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheHandlers")) {
				if (cacheHandlerClasses == null) {
					cacheHandlerClasses = ConfigFactoryImpl.loadCacheHandler(this, root);
				}
			}
		}
		return cacheHandlerClasses.entrySet().iterator();
	}

	public ConfigImpl resetCacheHandlers() {
		if (cacheHandlerClasses != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheHandlers")) {
				if (cacheHandlerClasses != null) {
					cacheHandlerClasses = null;
				}
			}
		}
		return this;
	}

	private Resources getResources() {
		if (initResource) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getResources")) {
				if (initResource) {
					ConfigFactoryImpl.loadResourceProvider(this, root);
					initResource = false;
					cacheHandlerClasses = null;
				}
			}
		}
		return resources;
	}

	public ConfigImpl resetResources() {
		if (!initResource) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getResources")) {
				if (!initResource) {
					initResource = true;
					resources.reset();
				}
			}
		}
		return this;
	}

	protected void addResourceProvider(String strProviderScheme, ClassDefinition cd, Map arguments) {
		((ResourcesImpl) resources).registerResourceProvider(strProviderScheme, cd, arguments);
	}

	/**
	 * @return return the resource providers
	 */
	@Override
	public ResourceProvider[] getResourceProviders() {
		return getResources().getResourceProviders();
	}

	/**
	 * @return return the resource providers
	 */
	@Override
	public ResourceProviderFactory[] getResourceProviderFactories() {
		return ((ResourcesImpl) getResources()).getResourceProviderFactories();
	}

	@Override
	public boolean hasResourceProvider(String scheme) {
		ResourceProviderFactory[] factories = ((ResourcesImpl) getResources()).getResourceProviderFactories();
		for (int i = 0; i < factories.length; i++) {
			if (factories[i].getScheme().equalsIgnoreCase(scheme)) return true;
		}
		return false;
	}

	@Override
	public Resource getResource(String path) {
		return getResources().getResource(path);
	}

	@Override
	public ApplicationListener getApplicationListener() {
		if (applicationListener == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getApplicationListener")) {
				if (applicationListener == null) {

					// type
					ApplicationListener listener;
					String strLT = SystemUtil.getSystemPropOrEnvVar("lucee.listener.type", null);
					if (StringUtil.isEmpty(strLT)) strLT = SystemUtil.getSystemPropOrEnvVar("lucee.application.listener", null);
					if (StringUtil.isEmpty(strLT)) strLT = ConfigFactoryImpl.getAttr(this, root, new String[] { "listenerType", "applicationListener" });
					listener = ConfigUtil.loadListener(strLT, null);
					if (listener == null) listener = new MixedAppListener();

					// mode
					String strLM = SystemUtil.getSystemPropOrEnvVar("lucee.listener.mode", null);
					if (StringUtil.isEmpty(strLM)) strLM = SystemUtil.getSystemPropOrEnvVar("lucee.application.mode", null);
					if (StringUtil.isEmpty(strLM)) strLM = ConfigFactoryImpl.getAttr(this, root, new String[] { "listenerMode", "applicationMode" });
					int listenerMode = ConfigUtil.toListenerMode(strLM, -1);
					if (listenerMode == -1) listenerMode = ApplicationListener.MODE_CURRENT2ROOT;
					listener.setMode(listenerMode);

					// singleton
					if (listener instanceof ModernAppListener) {
						String strSi = SystemUtil.getSystemPropOrEnvVar("lucee.listener.singleton", null);
						if (StringUtil.isEmpty(strSi)) strSi = SystemUtil.getSystemPropOrEnvVar("lucee.application.singleton", null);
						if (StringUtil.isEmpty(strSi)) strSi = ConfigFactoryImpl.getAttr(this, root, new String[] { "listenerSingleton", "applicationSingleton" });
						listener.setSingelton(Caster.toBooleanValue(strSi, false));
					}
					applicationListener = listener;

				}
			}
		}
		return applicationListener;
	}

	public ConfigImpl resetApplicationListener() {
		if (applicationListener != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getApplicationListener")) {
				if (applicationListener != null) {
					applicationListener = null;
				}
			}
		}
		return this;
	}

	/**
	 * @return the scriptProtect
	 */
	@Override
	public int getScriptProtect() {
		if (scriptProtect == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getScriptProtect")) {
				if (scriptProtect == null) {
					String strScriptProtect = SystemUtil.getSystemPropOrEnvVar("lucee.script.protect", null);
					if (StringUtil.isEmpty(strScriptProtect)) strScriptProtect = ConfigFactoryImpl.getAttr(this, root, "scriptProtect");
					if (!StringUtil.isEmpty(strScriptProtect)) {
						scriptProtect = AppListenerUtil.translateScriptProtect(strScriptProtect, 0);
					}
					else scriptProtect = ApplicationContext.SCRIPT_PROTECT_ALL;
				}
			}
		}
		return scriptProtect;
	}

	public ConfigImpl resetScriptProtect() {
		if (scriptProtect != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getScriptProtect")) {
				if (scriptProtect != null) {
					scriptProtect = null;
				}
			}
		}
		return this;
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
	@Deprecated
	public boolean isProxyEnableFor(String host) {
		return ProxyDataImpl.isProxyEnableFor(getProxyData(), host);
	}

	/**
	 * @return the triggerComponentDataMember
	 */
	@Override
	public boolean getTriggerComponentDataMember() {
		if (triggerComponentDataMember == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTriggerComponentDataMember")) {
				if (triggerComponentDataMember == null) {
					triggerComponentDataMember = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, root, "componentImplicitNotation"), Boolean.FALSE);
				}
			}
		}
		return triggerComponentDataMember;
	}

	public ConfigImpl resetTriggerComponentDataMember() {
		if (triggerComponentDataMember != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTriggerComponentDataMember")) {
				if (triggerComponentDataMember != null) {
					triggerComponentDataMember = null;
				}
			}
		}
		return this;
	}

	@Override
	public Resource getClientScopeDir() {
		if (clientScopeDir == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClientScopeDir")) {
				if (clientScopeDir == null) {
					Resource configDir = getConfigDir();
					String strClientDirectory = ConfigFactoryImpl.getAttr(this, root, "clientDirectory");
					if (!StringUtil.isEmpty(strClientDirectory, true)) {
						strClientDirectory = ConfigUtil.translateOldPath(strClientDirectory.trim());
						clientScopeDir = ConfigUtil.getFile(configDir, strClientDirectory, "client-scope", configDir, FileUtil.TYPE_DIR, ResourceUtil.LEVEL_PARENT_FILE, this);
					}
					else {
						clientScopeDir = configDir.getRealResource("client-scope");
					}
				}
			}
		}
		return clientScopeDir;
	}

	public ConfigImpl resetClientScopeDir() {
		if (clientScopeDir != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClientScopeDir")) {
				if (clientScopeDir != null) {
					clientScopeDir = null;
				}
			}
		}
		return this;
	}

	@Override
	public Resource getSessionScopeDir() {
		if (sessionScopeDir == null) sessionScopeDir = getConfigDir().getRealResource("session-scope");
		return sessionScopeDir;
	}

	@Override
	public long getClientScopeDirSize() {
		if (clientScopeDirSize == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClientScopeDirSize")) {
				if (clientScopeDirSize == null) {
					String strMax = ConfigFactoryImpl.getAttr(this, root, "clientDirectoryMaxSize");
					if (!StringUtil.isEmpty(strMax, true)) {
						clientScopeDirSize = ByteSizeParser.parseByteSizeDefinition(strMax.trim(), 1024L * 1024L * 100L);
					}
					else clientScopeDirSize = 1024L * 1024L * 100L;
				}
			}
		}
		return clientScopeDirSize;
	}

	public ConfigImpl resetClientScopeDirSize() {
		if (clientScopeDirSize != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getClientScopeDirSize")) {
				if (clientScopeDirSize != null) {
					clientScopeDirSize = null;
				}
			}
		}
		return this;
	}

	// =

	public long getSessionScopeDirSize() {
		return sessionScopeDirSize;
	}

	protected void setSessionScopeDir(Resource sessionScopeDir) {
		this.sessionScopeDir = sessionScopeDir;
	}

	@Override
	public ClassLoader getRPCClassLoader(boolean reload) throws IOException {
		return PhysicalClassLoaderFactory.getRPCClassLoader(this, getJavaSettings(), reload);
	}

	@Override
	public ClassLoader getRPCClassLoader(boolean reload, JavaSettings js) throws IOException {
		return PhysicalClassLoaderFactory.getRPCClassLoader(this, js != null ? js : getJavaSettings(), reload);
	}

	private static final Object dclt = new SerializableObject();
	private static final long POOL_MAX_IDLE = 60000;

	@Override
	public PhysicalClassLoader getDirectClassLoader(boolean reload) throws IOException {
		if (directClassLoader == null || reload) {
			synchronized (dclt) {
				if (directClassLoader == null || reload) {
					Resource dir = getClassDirectory().getRealResource("direct/");
					if (!dir.exists()) {
						ResourceUtil.createDirectoryEL(dir, true);
					}
					directClassLoader = PhysicalClassLoaderFactory.getPhysicalClassLoader(this, dir, reload);
				}
			}
		}
		return directClassLoader;
	}

	public void clearRPCClassLoader() {
		rpcClassLoaders.clear();
	}

	@Override
	public Resource getCacheDir() {
		if (cacheDir == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDir")) {
				if (cacheDir == null) {
					Resource configDir = getConfigDir();
					String strCacheDirectory = ConfigFactoryImpl.getAttr(this, root, "cacheDirectory");
					if (!StringUtil.isEmpty(strCacheDirectory)) {
						strCacheDirectory = ConfigUtil.translateOldPath(strCacheDirectory);
						Resource res = ConfigUtil.getFile(configDir, strCacheDirectory, "cache", configDir, FileUtil.TYPE_DIR, ResourceUtil.LEVEL_GRAND_PARENT_FILE, this);
						cacheDir = res;
					}
					else {
						cacheDir = configDir.getRealResource("cache");
					}
				}
			}
		}
		return cacheDir;
	}

	public ConfigImpl resetCacheDir() {
		if (cacheDir != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDir")) {
				if (cacheDir != null) {
					cacheDir = null;
				}
			}
		}
		return this;
	}

	@Override
	public long getCacheDirSize() {
		if (cacheDirSize == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDirSize")) {
				if (cacheDirSize == null) {
					String strMax = ConfigFactoryImpl.getAttr(this, root, "cacheDirectoryMaxSize");
					if (!StringUtil.isEmpty(strMax)) {
						cacheDirSize = ByteSizeParser.parseByteSizeDefinition(strMax, CACHE_DIR_SIZE_DEFAULT);
					}
					else {
						cacheDirSize = CACHE_DIR_SIZE_DEFAULT;
					}
				}
			}
		}
		return cacheDirSize;
	}

	public ConfigImpl resetCacheDirSize() {
		if (cacheDirSize != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDirSize")) {
				if (cacheDirSize != null) {
					cacheDirSize = null;
				}
			}
		}
		return this;
	}

	public DumpWriterEntry[] getDumpWritersEntries() {
		if (dmpWriterEntries == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDumpWritersEntries")) {
				if (dmpWriterEntries == null) {
					dmpWriterEntries = ConfigFactoryImpl.loadDumpWriter(this, root, null);
					// MUST handle default value was returned
				}
			}
		}
		return dmpWriterEntries;
	}

	public ConfigImpl resetDumpWritersEntries() {
		if (dmpWriterEntries != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDumpWritersEntries")) {
				if (dmpWriterEntries != null) {
					dmpWriterEntries = null;
				}
			}
		}
		return this;
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
		if (useComponentShadow == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "useComponentShadow")) {
				if (useComponentShadow == null) {
					useComponentShadow = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, root, "componentUseVariablesScope"), Boolean.TRUE);
				}
			}
		}
		return useComponentShadow;
	}

	public ConfigImpl resetComponentShadow() {
		if (useComponentShadow != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "useComponentShadow")) {
				if (useComponentShadow != null) {
					useComponentShadow = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean useComponentPathCache() {
		if (useComponentPathCache == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "useComponentPathCache")) {
				if (useComponentPathCache == null) {
					useComponentPathCache = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, root, "componentUseCachePath"), Boolean.TRUE);
				}
			}
		}
		return useComponentPathCache;
	}

	public ConfigImpl resetComponentPathCache() {
		if (useComponentPathCache != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "useComponentPathCache")) {
				if (useComponentPathCache != null) {
					useComponentPathCache = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean useCTPathCache() {
		if (useCTPathCache == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "useCTPathCache")) {
				if (useCTPathCache == null) {
					if (ConfigUtil.hasAccess(this, SecurityManager.TYPE_CUSTOM_TAG)) {
						String strDoPathcache = ConfigFactoryImpl.getAttr(this, root, "customTagUseCachePath");
						if (StringUtil.isEmpty(strDoPathcache, true)) strDoPathcache = ConfigFactoryImpl.getAttr(this, root, "customTagCachePaths");
						if (!StringUtil.isEmpty(strDoPathcache, true)) {
							useCTPathCache = Caster.toBooleanValue(strDoPathcache.trim(), true);
						}
					}
					if (useCTPathCache == null) useCTPathCache = Boolean.TRUE;
				}
			}
		}
		return useCTPathCache;
	}

	public ConfigImpl resetUseCTPathCache() {
		if (useCTPathCache != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "useCTPathCache")) {
				if (useCTPathCache != null) {
					useCTPathCache = null;
				}
			}
		}
		return this;
	}

	public void flushComponentPathCache() {
		componentPathCache.flush();
	}

	public void flushApplicationPathCache() {
		if (applicationPathCache != null) applicationPathCache.clear();
	}

	public void flushCTPathCache() {
		if (ctPatchCache != null) ctPatchCache.clear();
	}

	@Override
	public PrintWriter getErrWriter() {
		if (err == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getErrWriter")) {
				if (err == null) {
					PrintStream tmp = ConfigFactoryImpl.loadErr(this, root);
					if (tmp == null) {
						err = SystemUtil.getPrintWriter(SystemUtil.ERR);
					}
					else {
						err = new PrintWriter(tmp);
						System.setOut(tmp);

					}
				}
			}
		}
		return err;
	}

	public ConfigImpl resetErrWriter() {
		if (err != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getErrWriter")) {
				if (err != null) {
					err = null;
				}
			}
		}
		return this;
	}

	@Override
	public PrintWriter getOutWriter() {
		if (out == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getOutWriter")) {
				if (out == null) {
					PrintStream tmp = ConfigFactoryImpl.loadOut(this, root);
					if (tmp == null) {
						out = SystemUtil.getPrintWriter(SystemUtil.OUT);
					}
					else {
						out = new PrintWriter(tmp);
						System.setOut(tmp);

					}
				}
			}
		}
		return out;
	}

	public ConfigImpl resetOutWriter() {
		if (out != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getOutWriter")) {
				if (out != null) {
					out = null;
				}
			}
		}
		return this;
	}

	@Override
	public DatasourceConnPool getDatasourceConnectionPool(DataSource ds, String user, String pass) {
		String id = DatasourceConnectionFactory.createId(ds, user, pass);
		DatasourceConnPool pool = pools.get(id);
		if (pool == null) {
			synchronized (id) {
				pool = pools.get(id);
				if (pool == null) {// TODO add config but from where?
					DataSourcePro dsp = (DataSourcePro) ds;
					// MUST merge ConnectionLimit and MaxTotal
					int mt = 0;
					if (dsp.getMaxTotal() > 0) mt = dsp.getMaxTotal();
					else {
						mt = dsp.getConnectionLimit();
						if (mt <= 0) mt = Integer.MAX_VALUE;
					}

					// maxWaitMillis: how long to wait for a connection when pool is exhausted (30 seconds)
					long maxWaitMillis = 30000L;
					// minEvictableIdleTimeMillis: use idleTimeout (in minutes) for how long connection can be idle
					// before eviction
					// -1 = not set (use default 10 minutes), 0 = infinite (no eviction), >0 = use that value
					int idleTimeout = dsp.getIdleTimeout();
					long minEvictableIdleTimeMillis;
					if (idleTimeout > 0) {
						minEvictableIdleTimeMillis = idleTimeout * 60000L;
					}
					else if (idleTimeout == 0) {
						minEvictableIdleTimeMillis = -1; // infinite - disable eviction
					}
					else {
						minEvictableIdleTimeMillis = 10 * 60000L; // default: 10 minutes
					}

					pool = new DatasourceConnPool(this, ds, user, pass, "datasource", DatasourceConnPool.createPoolConfig(null, null, null, dsp.getMinIdle(), dsp.getMaxIdle(), mt,
							maxWaitMillis, minEvictableIdleTimeMillis, 0, 0, 0, null));
					pools.put(id, pool);
				}
			}
		}
		return pool;
	}

	// mark-then-sweep across two ticks: any borrow between mark and sweep clears the mark
	public void cleanDatasourceConnectionPools() {
		long now = System.currentTimeMillis();
		for (Entry<String, DatasourceConnPool> e: pools.entrySet()) {
			DatasourceConnPool pool = e.getValue();
			if ((pool.getNumActive() + pool.getNumIdle() + pool.getNumWaiters()) == 0 && (pool.getLastBorrowed() + POOL_MAX_IDLE) < now) {
				if (pool.isEvictionCandidate()) {
					pool.close();
					pools.remove(e.getKey(), pool);
				}
				else {
					pool.setEvictionCandidate(true);
				}
			}
		}
	}

	@Override
	public MockPool getDatasourceConnectionPool() {
		return new MockPool();
	}

	@Override
	public Collection<DatasourceConnPool> getDatasourceConnectionPools() {
		return pools.values();
	}

	@Override
	public void removeDatasourceConnectionPool(DataSource ds) {
		removeDatasourceConnectionPool(ds.getName());
	}

	@Override
	public void removeDatasourceConnectionPool(String name) {
		List<Entry<String, DatasourceConnPool>> matches = null;
		for (Entry<String, DatasourceConnPool> e: pools.entrySet()) {
			if (e.getValue().getFactory().getDatasource().getName().equalsIgnoreCase(name)) {
				if (matches == null) matches = new ArrayList<>();
				matches.add(e);
			}
		}
		if (matches == null) return;
		// close first, then CAS-remove: if close() throws, entry stays in the map and bg sweep retries
		for (Entry<String, DatasourceConnPool> e: matches) {
			e.getValue().close();
			pools.remove(e.getKey(), e.getValue()); // CAS — only remove if value unchanged
		}
	}

	@Override
	public boolean doLocalCustomTag() {
		if (doLocalCustomTag == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "doLocalCustomTag")) {
				if (doLocalCustomTag == null) {
					if (getMode() == ConfigPro.MODE_STRICT) {
						doLocalCustomTag = Boolean.FALSE;
					}
					else {
						if (ConfigUtil.hasAccess(this, SecurityManager.TYPE_CUSTOM_TAG)) {
							String strDoCTLocalSearch = ConfigFactoryImpl.getAttr(this, root, "customTagLocalSearch");
							if (StringUtil.isEmpty(strDoCTLocalSearch, true)) strDoCTLocalSearch = ConfigFactoryImpl.getAttr(this, root, "customTagSearchLocal");
							if (!StringUtil.isEmpty(strDoCTLocalSearch)) {
								doLocalCustomTag = Caster.toBooleanValue(strDoCTLocalSearch.trim(), true);
							}
						}
					}
					if (doLocalCustomTag == null) doLocalCustomTag = Boolean.TRUE;
				}
			}
		}
		return doLocalCustomTag;
	}

	public ConfigImpl resetLocalCustomTag() {
		if (doLocalCustomTag != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "doLocalCustomTag")) {
				if (doLocalCustomTag != null) {
					doLocalCustomTag = null;
				}
			}
		}
		return this;
	}

	@Override
	public String[] getCustomTagExtensions() {
		if (customTagExtensions == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCustomTagExtensions")) {
				if (customTagExtensions == null) {
					// extensions
					if (getMode() == ConfigPro.MODE_STRICT) {
						customTagExtensions = Constants.getExtensions();
					}
					else {
						if (ConfigUtil.hasAccess(this, SecurityManager.TYPE_CUSTOM_TAG)) {
							String strExtensions = ConfigFactoryImpl.getAttr(this, root, "customTagExtensions");
							if (!StringUtil.isEmpty(strExtensions)) {
								try {
									String[] arr = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(strExtensions, ","));
									customTagExtensions = ListUtil.trimItems(arr);
								}
								catch (PageException e) {
									// MUST log
									LogUtil.log("config", e);
								}
							}
						}
					}
					customTagExtensions = Constants.getExtensions();
				}
			}
		}
		return customTagExtensions;
	}

	public ConfigImpl resetCustomTagExtensions() {
		if (customTagExtensions != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCustomTagExtensions")) {
				if (customTagExtensions != null) {
					customTagExtensions = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean doComponentDeepSearch() {
		if (doComponentTagDeepSearch == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "doComponentDeepSearch")) {
				if (doComponentTagDeepSearch == null) {
					String strDeepSearch = ConfigFactoryImpl.getAttr(this, root, "componentDeepSearch");
					if (!StringUtil.isEmpty(strDeepSearch)) {
						doComponentTagDeepSearch = Caster.toBoolean(strDeepSearch.trim(), Boolean.FALSE);
					}
					else doComponentTagDeepSearch = Boolean.FALSE;
				}
			}
		}
		return doComponentTagDeepSearch;
	}

	public ConfigImpl resetComponentDeepSearch() {
		if (doComponentTagDeepSearch != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "doComponentDeepSearch")) {
				if (doComponentTagDeepSearch != null) {
					doCustomTagDeepSearch = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean doCustomTagDeepSearch() {
		if (doCustomTagDeepSearch == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "doCustomTagDeepSearch")) {
				if (doCustomTagDeepSearch == null) {
					// do custom tag deep search
					if (getMode() == ConfigPro.MODE_STRICT) {
						doCustomTagDeepSearch = Boolean.FALSE;
					}
					else {
						if (ConfigUtil.hasAccess(this, SecurityManager.TYPE_CUSTOM_TAG)) {
							String strDoCTDeepSearch = ConfigFactoryImpl.getAttr(this, root, "customTagDeepSearch");
							if (StringUtil.isEmpty(strDoCTDeepSearch, true)) strDoCTDeepSearch = ConfigFactoryImpl.getAttr(this, root, "customTagSearchSubdirectories");
							if (!StringUtil.isEmpty(strDoCTDeepSearch)) {
								doCustomTagDeepSearch = Caster.toBooleanValue(strDoCTDeepSearch.trim(), false);
							}
						}
					}
					if (doCustomTagDeepSearch == null) doCustomTagDeepSearch = Boolean.FALSE;
				}
			}
		}
		return doCustomTagDeepSearch;
	}

	public ConfigImpl resetCustomTagDeepSearch() {
		if (doCustomTagDeepSearch != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "doCustomTagDeepSearch")) {
				if (doCustomTagDeepSearch != null) {
					doCustomTagDeepSearch = null;
				}
			}
		}
		return this;
	}

	/**
	 * @return the version
	 */
	@Override
	public double getVersion() {
		if (version == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getVersion")) {
				if (version == null) {
					try {
						String strVersion = ConfigFactoryImpl.getAttr(this, root, "version");
						version = Caster.toDoubleValue(strVersion, DEFAULT_VERSION);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						ConfigFactoryImpl.log(this, t);
					}
					if (version == null) version = DEFAULT_VERSION;
				}
			}

		}
		return version;
	}

	public ConfigImpl resetVersion() {
		if (version != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getVersion")) {
				if (version != null) {
					version = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean closeConnection() {
		if (closeConnection == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "closeConnection")) {
				if (closeConnection == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "closeConnection");
					if (!StringUtil.isEmpty(str)) {
						closeConnection = Caster.toBoolean(str, Boolean.FALSE);
					}
					else closeConnection = Boolean.FALSE;
				}
			}
		}
		return closeConnection;
	}

	public ConfigImpl resetConnection() {
		if (closeConnection != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "closeConnection")) {
				if (closeConnection != null) {
					closeConnection = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean contentLength() {
		if (contentLength == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "contentLength")) {
				if (contentLength == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "contentLength");
					if (!StringUtil.isEmpty(str)) {
						contentLength = Caster.toBoolean(str, Boolean.TRUE);
					}
					else contentLength = Boolean.TRUE;
				}
			}
		}
		return contentLength;
	}

	public ConfigImpl resetContentLength() {
		if (contentLength != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "contentLength")) {
				if (contentLength != null) {
					contentLength = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean allowCompression() {
		if (allowCompression == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "allowCompression")) {
				if (allowCompression == null) {
					String str = SystemUtil.getSystemPropOrEnvVar("lucee.allow.compression", null);
					if (StringUtil.isEmpty(str)) {
						str = ConfigFactoryImpl.getAttr(this, root, "allowCompression");
					}
					if (!StringUtil.isEmpty(str)) {
						allowCompression = Caster.toBoolean(str, ConfigImpl.DEFAULT_ALLOW_COMPRESSION);
					}
					else allowCompression = ConfigImpl.DEFAULT_ALLOW_COMPRESSION;
				}
			}
		}
		return allowCompression;
	}

	public ConfigImpl resetAllowCompression() {
		if (allowCompression != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "allowCompression")) {
				if (allowCompression != null) {
					allowCompression = null;
				}
			}
		}
		return this;
	}

	/**
	 * @return the constants
	 */
	@Override
	public Struct getConstants() {
		if (constants == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getConstants")) {
				if (constants == null) {
					constants = ConfigFactoryImpl.loadConstants(this, root, null);
					if (constants == null) constants = new StructImpl();
				}
			}
		}
		return constants;
	}

	public ConfigImpl resetConstants() {
		if (constants != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getConstants")) {
				if (constants != null) {
					constants = null;
				}
			}
		}
		return this;
	}

	/**
	 * @return the showVersion
	 */
	@Override
	public boolean isShowVersion() {
		if (showVersion == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isShowVersion")) {
				if (showVersion == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "showVersion");
					if (!StringUtil.isEmpty(str)) {
						showVersion = Caster.toBoolean(str, Boolean.FALSE);
					}
					else showVersion = Boolean.FALSE;
				}
			}
		}
		return showVersion;
	}

	public ConfigImpl resetShowVersion() {
		if (showVersion != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "isShowVersion")) {
				if (showVersion != null) {
					showVersion = null;
				}
			}
		}
		return this;
	}

	@Override
	public RemoteClient[] getRemoteClients() {
		if (remoteClients == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRemoteClients")) {
				if (remoteClients == null) {
					remoteClients = ConfigFactoryImpl.loadRemoteClients(this, root);
				}
			}
		}
		return remoteClients;
	}

	public ConfigImpl resetRemoteClients() {
		if (remoteClients != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRemoteClients")) {
				if (remoteClients != null) {
					remoteClients = null;
				}
			}
		}
		return this;
	}

	@Override
	public SpoolerEngine getSpoolerEngine() {
		if (remoteClientSpoolerEngine == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSpoolerEngine")) {
				if (remoteClientSpoolerEngine == null) {
					remoteClientSpoolerEngine = new SpoolerEngineImpl(this, "Remote Client Spooler");
				}
			}
		}
		return remoteClientSpoolerEngine;
	}

	public int getRemoteClientMaxThreads() {
		if (remoteClientMaxThreads == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRemoteClientMaxThreads")) {
				if (remoteClientMaxThreads == null) {
					Struct _clients = ConfigUtil.getAsStruct("remoteClients", root);
					remoteClientMaxThreads = Caster.toInteger(ConfigFactoryImpl.getAttr(this, _clients, "maxThreads"), 20);
				}
			}
		}
		return remoteClientMaxThreads;
	}

	public ConfigImpl resetRemoteClientMaxThreads() {
		if (remoteClientMaxThreads != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRemoteClientMaxThreads")) {
				if (remoteClientMaxThreads != null) {
					remoteClientMaxThreads = null;
				}
			}
		}
		return this;
	}

	//
	@Override
	public Resource getRemoteClientDirectory() {
		if (remoteClientDirectory == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRemoteClientDirectory")) {
				if (remoteClientDirectory == null) {
					String strDir = SystemUtil.getSystemPropOrEnvVar("lucee.task.directory", null);
					if (StringUtil.isEmpty(strDir)) {
						Struct _clients = ConfigUtil.getAsStruct("remoteClients", root);
						strDir = _clients != null ? ConfigFactoryImpl.getAttr(this, _clients, "directory") : null;
					}
					remoteClientDirectory = ConfigUtil.getFile(getRootDirectory(), strDir, "client-task", getConfigDir(), FileUtil.TYPE_DIR, ResourceUtil.LEVEL_GRAND_PARENT_FILE,
							this);

					if (!remoteClientDirectory.exists()) remoteClientDirectory.mkdirs();
				}
			}
		}
		return remoteClientDirectory;
	}

	public ConfigImpl resetRemoteClientDirectory() {
		if (remoteClientDirectory != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRemoteClientDirectory")) {
				if (remoteClientDirectory != null) {
					remoteClientDirectory = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getErrorStatusCode() {
		if (errorStatusCode == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getErrorStatusCode")) {
				if (errorStatusCode == null) {
					errorStatusCode = ConfigFactoryImpl.loadError(this, root, true);
				}
			}
		}
		return errorStatusCode;
	}

	public ConfigImpl resetErrorStatusCode() {
		if (errorStatusCode != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getErrorStatusCode")) {
				if (errorStatusCode != null) {
					errorStatusCode = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getLocalMode() {
		if (localMode == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getLocalMode")) {
				if (localMode == null) {
					String strLocalMode = ConfigFactoryImpl.getAttr(this, root, "localMode");
					if (StringUtil.isEmpty(strLocalMode)) strLocalMode = ConfigFactoryImpl.getAttr(this, root, "localScopeMode");
					if (!StringUtil.isEmpty(strLocalMode, true)) {
						localMode = AppListenerUtil.toLocalMode(strLocalMode, Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS);
					}
					else {
						localMode = Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS;
					}
				}
			}
		}
		return localMode;
	}

	public ConfigImpl resetLocalMode() {
		if (localMode != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getLocalMode")) {
				if (localMode != null) {
					localMode = null;
				}
			}
		}
		return this;
	}

	@Override
	public Resource getVideoDirectory() {
		// TODO take from tag <video>
		Resource dir = getConfigDir().getRealResource("video");
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}

	@Override
	public ExtensionProvider[] getExtensionProviders() {
		throw new RuntimeException("no longer supported, use getRHExtensionProviders() instead.");
	}

	@Override
	public RHExtensionProvider[] getRHExtensionProviders() {
		if (rhextensionProviders == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRHExtensionProviders")) {
				if (rhextensionProviders == null) {
					rhextensionProviders = ConfigFactoryImpl.loadExtensionProviders(this, root);
				}
			}
		}
		return rhextensionProviders;
	}

	public ConfigImpl resetRHExtensionProviders() {
		if (rhextensionProviders != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRHExtensionProviders")) {
				if (rhextensionProviders != null) {
					rhextensionProviders = null;
				}
			}
		}
		return this;
	}

	// = Constants.RH_EXTENSION_PROVIDERS;

	@Override
	public Extension[] getExtensions() {
		throw new PageRuntimeException("no longer supported");
	}

	@Override
	public RHExtension[] getRHExtensions() {
		return rhextensions;
	}

	public ConfigImpl resetRHExtensions() {
		return this;
	}

	public String getExtensionsMD5() {
		return extensionsMD5;
	}

	protected void setExtensions(RHExtension[] extensions, String md5) {
		this.rhextensions = extensions;
		this.extensionsMD5 = md5;
	}

	public List<ExtensionDefintion> getExtensionDefinitions() {
		if (extensionsDefs == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getExtensionDefinitions")) {
				if (extensionsDefs == null) {
					extensionsDefs = ConfigFactoryImpl.loadExtensionDefinition(this, root);
				}
			}
		}
		return this.extensionsDefs;
	}

	public ConfigImpl resetExtensionDefinitions() {
		if (extensionsDefs != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getExtensionDefinitions")) {
				if (extensionsDefs != null) {
					extensionsDefs = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isExtensionEnabled() {
		throw new PageRuntimeException("no longer supported");
	}

	@Override
	public boolean allowRealPath() {
		if (allowRealPath == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "allowRealPath")) {
				if (allowRealPath == null) {
					Struct fileSystem = ConfigUtil.getAsStruct("fileSystem", root);
					if (fileSystem != null) {
						String strAllowRealPath = ConfigFactoryImpl.getAttr(this, fileSystem, "allowRealpath");
						if (!StringUtil.isEmpty(strAllowRealPath, true)) {
							allowRealPath = Caster.toBoolean(strAllowRealPath.trim(), Boolean.TRUE);
						}
						else allowRealPath = Boolean.TRUE;
					}
					else allowRealPath = Boolean.TRUE;
				}
			}
		}
		return allowRealPath;
	}

	public ConfigImpl resetAllowRealPath() {
		if (allowRealPath != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "allowRealPath")) {
				if (allowRealPath != null) {
					allowRealPath = null;

				}
			}
		}
		return this;
	}
	// = true

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
		if (remoteClientUsage == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRemoteClientUsage")) {
				if (remoteClientUsage == null) {
					Struct _clients = ConfigUtil.getAsStruct("remoteClients", root);
					Struct sct = ConfigUtil.getAsStruct(null, _clients, true, "usage");// config.setRemoteClientUsage(toStruct(strUsage));
					if (sct == null) remoteClientUsage = new StructImpl();
					else remoteClientUsage = sct;

				}
			}
		}
		return remoteClientUsage;
	}

	public ConfigImpl resetRemoteClientUsage() {
		if (remoteClientUsage != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRemoteClientUsage")) {
				if (remoteClientUsage != null) {
					remoteClientUsage = null;

				}
			}
		}
		return this;
	}

	@Override
	public Class<AdminSync> getAdminSyncClass() {
		if (adminSyncClass == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getAdminSyncClass")) {
				if (adminSyncClass == null) {
					try {
						ClassDefinition asc = ConfigFactoryImpl.getClassDefinition(this, root, "adminSync", getIdentification());
						if (!asc.hasClass()) asc = ConfigFactoryImpl.getClassDefinition(this, root, "adminSynchronisation", getIdentification());

						if (asc.hasClass()) {

							Class clazz = asc.getClazz();
							if (!Reflector.isInstaneOf(clazz, AdminSync.class, false))
								throw new ApplicationException("class [" + clazz.getName() + "] does not implement interface [" + AdminSync.class.getName() + "]");
							adminSyncClass = clazz;

						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						LogUtil.logGlobal(this, ConfigFactoryImpl.class.getName(), t);

					}
					if (adminSyncClass == null) adminSyncClass = AdminSyncNotSupported.class;
				}
			}
		}
		return adminSyncClass;
	}

	@Override
	public AdminSync getAdminSync() throws ClassException {
		if (adminSync == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getAdminSyncClass")) {
				if (adminSync == null) {
					adminSync = (AdminSync) ClassUtil.loadInstance(getAdminSyncClass());
				}
			}

		}
		return this.adminSync;
	}

	public ConfigImpl resetAdminSyncClass() {
		if (adminSyncClass != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getAdminSyncClass")) {
				if (adminSyncClass != null) {
					adminSyncClass = null;
					adminSync = null;
				}
			}
		}
		return this;
	}

	@Override
	public Class getVideoExecuterClass() {
		return videoExecuterClass;
	}

	protected void setVideoExecuterClass(Class videoExecuterClass) {
		this.videoExecuterClass = videoExecuterClass;
	}

	/**
	 * @return the tagMappings
	 */
	@Override
	public Collection<Mapping> getTagMappings() {
		getTLDs();
		return tagMappings.values();

		// MUST 7 flushPageSourcePool(config.getTagMappings());

	}

	@Override
	public Mapping getTagMapping(String mappingName) {
		getTLDs();
		return tagMappings.get(mappingName);
	}

	@Override
	public Mapping getDefaultTagMapping() {
		getTLDs();
		return defaultTagMapping;
	}

	@Override
	public Mapping getFunctionMapping(String mappingName) {
		getFLDs();
		return functionMappings.get(mappingName);
	}

	@Override
	public Mapping getDefaultFunctionMapping() {
		getFLDs();
		return defaultFunctionMapping;
	}

	@Override
	public Collection<Mapping> getFunctionMappings() {
		getFLDs();
		return functionMappings.values();

		// MUST 7 ConfigWebFactory.flushPageSourcePool(config.getFunctionMappings());
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

			this.scriptMapping = new MappingImpl(this, "/mapping-script/", physical.getAbsolutePath(), null, ConfigPro.INSPECT_AUTO, 60000, 1000, true, true, true, true, false,
					true, null, -1, -1);
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
		if (inspectTemplate == -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getInspectTemplate")) {
				if (inspectTemplate == -1) {
					String strInspectTemplate = SystemUtil.getSystemPropOrEnvVar("lucee.inspect.template", null);
					if (StringUtil.isEmpty(strInspectTemplate, true)) strInspectTemplate = ConfigFactoryImpl.getAttr(this, root, "inspectTemplate");
					if (!StringUtil.isEmpty(strInspectTemplate, true)) {
						inspectTemplate = ConfigUtil.inspectTemplate(strInspectTemplate, ConfigPro.INSPECT_AUTO);
					}
					if (inspectTemplate == -1) inspectTemplate = INSPECT_AUTO;
				}
			}
		}
		return inspectTemplate;
	}

	public ConfigImpl resetInspectTemplate() {
		if (inspectTemplate != -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getInspectTemplate")) {
				if (inspectTemplate != -1) {
					inspectTemplate = -1;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getTypeChecking() {
		if (typeChecking == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTypeChecking")) {
				if (typeChecking == null) {
					Boolean b = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.type.checking", null), null);
					if (b == null) b = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.udf.type.checking", null), null);
					if (b == null) b = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, root, new String[] { "typeChecking", "UDFTypeChecking" }), Boolean.TRUE);
					typeChecking = b;
				}
			}
		}
		return typeChecking;
	}

	public ConfigImpl resetTypeChecking() {
		if (typeChecking != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getTypeChecking")) {
				if (typeChecking != null) {
					typeChecking = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getInspectTemplateAutoInterval(boolean slow) {
		if (inspectTemplateAutoIntervalSlow == ConfigPro.INSPECT_INTERVAL_UNDEFINED) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getInspectTemplateAutoInterval")) {
				if (inspectTemplateAutoIntervalSlow == ConfigPro.INSPECT_INTERVAL_UNDEFINED) {
					inspectTemplateAutoIntervalFast = Caster.toIntValue(ConfigFactoryImpl.getAttr(this, root, "inspectTemplateIntervalFast"), ConfigPro.INSPECT_INTERVAL_FAST);
					if (inspectTemplateAutoIntervalFast <= 0) inspectTemplateAutoIntervalFast = ConfigPro.INSPECT_INTERVAL_FAST;
					inspectTemplateAutoIntervalSlow = Caster.toIntValue(ConfigFactoryImpl.getAttr(this, root, "inspectTemplateIntervalSlow"), ConfigPro.INSPECT_INTERVAL_SLOW);
					if (inspectTemplateAutoIntervalSlow <= 0) inspectTemplateAutoIntervalSlow = ConfigPro.INSPECT_INTERVAL_SLOW;
				}
			}
		}
		return slow ? inspectTemplateAutoIntervalSlow : inspectTemplateAutoIntervalFast;
	}

	public ConfigImpl resetInspectTemplateAutoInterval() {
		if (inspectTemplateAutoIntervalSlow != ConfigPro.INSPECT_INTERVAL_UNDEFINED) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getInspectTemplateAutoInterval")) {
				if (inspectTemplateAutoIntervalSlow != ConfigPro.INSPECT_INTERVAL_UNDEFINED) {
					inspectTemplateAutoIntervalSlow = ConfigPro.INSPECT_INTERVAL_UNDEFINED;
					inspectTemplateAutoIntervalFast = ConfigPro.INSPECT_INTERVAL_UNDEFINED;
				}
			}
		}
		return this;
	}

	@Override
	public String getSerialNumber() {
		return "";
	}

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

	public Map<Integer, String> getCacheDefaultConnectionNames() {
		if (cacheDefaultConnectionNames == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDefaultConnectionName")) {
				if (cacheDefaultConnectionNames == null) {
					cacheDefaultConnectionNames = ConfigFactoryImpl.loadCacheDefaultConnectionNames(this, root);
				}
			}
		}
		return cacheDefaultConnectionNames;
	}

	@Override
	public String getCacheDefaultConnectionName(int type) {
		String res = getCacheDefaultConnectionNames().get(type);
		if (StringUtil.isEmpty(res, true)) return "";
		return res.trim();
	}

	public ConfigImpl resetCacheDefaultConnectionNames() {
		if (cacheDefaultConnectionNames != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDefaultConnectionName")) {
				if (cacheDefaultConnectionNames != null) {
					cacheDefaultConnectionNames = null;
				}
			}
		}
		return this;
	}

	public Map<Integer, CacheConnection> getCacheDefaultConnections() {
		if (cacheDefaultConnection == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDefaultConnection")) {
				if (cacheDefaultConnection == null) {

					Map<Integer, String> names = getCacheDefaultConnectionNames();
					Map<Integer, CacheConnection> tmp = new HashMap<>();

					CacheConnection cc;
					for (Entry<String, CacheConnection> entry: getCacheConnections().entrySet()) {
						cc = entry.getValue();

						for (Entry<Integer, String> e: names.entrySet()) {
							if (cc.getName().equalsIgnoreCase(e.getValue())) {
								tmp.put(e.getKey(), cc);
							}
						}
					}

					// when default was set to null
					/*
					 * for (Entry<Integer, String> e: names.entrySet()) { if (StringUtil.isEmpty(e.getValue()) &&
					 * tmp.get(e.getKey()) != null) { tmp.remove(e.getKey()); } }
					 */
					cacheDefaultConnection = tmp;
				}
			}
		}
		return cacheDefaultConnection;
	}

	@Override
	public CacheConnection getCacheDefaultConnection(int type) {
		return getCacheDefaultConnections().get(type);
	}

	public ConfigImpl resetCacheDefaultConnections() {
		if (cacheDefaultConnection != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDefaultConnection")) {
				if (cacheDefaultConnection != null) {
					cacheDefaultConnection = null;
				}
			}
		}
		return this;
	}

	@Override
	public Map<String, CacheConnection> getCacheConnections() {// = new HashMap<String, CacheConnection>()
		if (cacheConnection == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheConnections")) {
				if (cacheConnection == null) {
					cacheConnection = ConfigFactoryImpl.loadCacheCacheConnections(this, root);
				}
			}
		}
		return cacheConnection;
	}

	public ConfigImpl resetCacheConnections() {// = new HashMap<String, CacheConnection>()
		if (cacheConnection != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheConnections")) {
				if (cacheConnection != null) {
					cacheConnection = null;
				}
			}
		}
		return this;
	}

	public ConfigImpl resetCacheAll() {
		resetCacheDefaultConnectionNames();
		resetCacheDefaultConnections();
		resetCacheConnections();
		resetCacheDefinitions();
		return this;
	}

	@Override
	public boolean getExecutionLogEnabled() {
		if (executionLogEnabled == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getExecutionLogEnabled")) {
				if (executionLogEnabled == null) {
					Struct sct = ConfigUtil.getAsStruct("executionLog", root);
					executionLogEnabled = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, sct, "enabled"), Boolean.FALSE);
				}
			}
		}
		return executionLogEnabled;
	}

	public ConfigImpl resetExecutionLogEnabled() {
		if (executionLogEnabled != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getExecutionLogEnabled")) {
				if (executionLogEnabled != null) {
					executionLogEnabled = null;
				}
			}
		}
		return this;
	}

	@Override
	public ExecutionLogFactory getExecutionLogFactory() {
		if (executionLogFactory == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getExecutionLogFactory")) {
				if (executionLogFactory == null) {
					executionLogFactory = ConfigFactoryImpl.loadExeLog(this, root);
				}
			}
		}
		return executionLogFactory;
	}

	public ConfigImpl resetExecutionLogFactory() {
		if (executionLogFactory != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getExecutionLogFactory")) {
				if (executionLogFactory != null) {
					executionLogFactory = null;
				}
			}
		}
		return this;
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
				engine = (ORMEngine) ClassUtil.loadInstance(getORMEngineClassDefintion().getClazz());
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
				ApplicationException ae = new ApplicationException(
						"cannot initialize ORM Engine [" + getORMEngineClassDefintion() + "], make sure you have added all the required jar files");
				ExceptionUtil.initCauseEL(ae, t);
				throw ae;

			}
			ormengines.put(name, engine);
			/*
			 * } catch (PageException pe) { throw pe; }
			 */
		}

		return engine;
	}

	@Override
	public boolean hasORMEngine() {
		return getORMEngineClassDefintion().equals(ConfigFactoryImpl.DUMMY_ORM_ENGINE);
	}

	@Override
	public ClassDefinition<? extends ORMEngine> getORMEngineClassDefintion() {
		if (cdORMEngine == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getORMEngineClassDefintion")) {
				if (cdORMEngine == null) {
					cdORMEngine = ConfigFactoryImpl.loadORMClass(this, root);
				}
			}
		}
		return cdORMEngine;
	}

	public ConfigImpl resetORMEngineClassDefintion() {
		if (cdORMEngine != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getORMEngineClassDefintion")) {
				if (cdORMEngine != null) {
					cdORMEngine = null;
				}
			}
		}
		return this;
	}

	public ClassDefinition<? extends ORMEngine> getORMEngineClass() {
		return getORMEngineClassDefintion();
	}

	@Override
	public ORMConfiguration getORMConfig() {
		if (ormConfig == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getORMConfig")) {
				if (ormConfig == null) {
					ormConfig = ConfigFactoryImpl.loadORMConfig(this, root, null);
				}
			}
		}
		return ormConfig;
	}

	public ConfigImpl resetORMConfig() {
		if (ormConfig != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getORMConfig")) {
				if (ormConfig != null) {
					ormConfig = null;
				}
			}
		}
		return this;
	}

	private Map<String, SoftReference<ConfigUtil.CacheElement>> applicationPathCache = null;// new ArrayList<Page>();
	private Map<String, SoftReference<InitFile>> ctPatchCache = null;// new ArrayList<Page>();
	private Map<String, SoftReference<UDF>> udfCache = new ConcurrentHashMap<String, SoftReference<UDF>>();

	@Override
	public CIPage getCachedPage(PageContext pc, String pathWithCFC) throws PageException {
		return componentPathCache.getPage(pc, pathWithCFC);
	}

	@Override
	public void putCachedPageSource(String pathWithCFC, PageSource ps) {
		componentPathCache.put(pathWithCFC, ps);
	}

	@Override
	public PageSource getApplicationPageSource(PageContext pc, String path, String filename, int mode, RefBoolean isCFC) {
		if (applicationPathCache == null) return null;
		String id = (path + ":" + filename + ":" + mode).toLowerCase();

		SoftReference<CacheElement> tmp = getApplicationPathCacheTimeout() <= 0 ? null : applicationPathCache.get(id);
		if (tmp != null) {
			CacheElement ce = tmp.get();
			if (ce != null && (ce.created + getApplicationPathCacheTimeout()) >= System.currentTimeMillis()) {
				if (ce.pageSource.loadPage(pc, false, (Page) null) != null) {
					if (isCFC != null) isCFC.setValue(ce.isCFC);
					return ce.pageSource;
				}
			}
		}
		return null;
	}

	@Override
	public void putApplicationPageSource(String path, PageSource ps, String filename, int mode, boolean isCFC) {
		if (getApplicationPathCacheTimeout() <= 0) return;
		if (applicationPathCache == null) applicationPathCache = new ConcurrentHashMap<String, SoftReference<CacheElement>>();// MUSTMUST new
		String id = (path + ":" + filename + ":" + mode).toLowerCase();
		applicationPathCache.put(id, new SoftReference<CacheElement>(new CacheElement(ps, isCFC)));
	}

	@Override
	public long getApplicationPathCacheTimeout() {
		if (applicationPathCacheTimeout == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getApplicationPathCacheTimeout")) {
				if (applicationPathCacheTimeout == null) {
					TimeSpan ts = null;
					String str = SystemUtil.getSystemPropOrEnvVar("lucee.application.path.cache.timeout", null);
					if (StringUtil.isEmpty(str)) str = ConfigFactoryImpl.getAttr(this, root, "applicationPathTimeout");
					if (!StringUtil.isEmpty(str)) ts = Caster.toTimespan(str, null);
					if (ts != null && ts.getMillis() > 0) applicationPathCacheTimeout = ts.getMillis();
					else applicationPathCacheTimeout = 20000L;
				}
			}
		}
		return applicationPathCacheTimeout;
	}

	public ConfigImpl resetApplicationPathCacheTimeout() {
		if (applicationPathCacheTimeout != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getApplicationPathCacheTimeout")) {
				if (applicationPathCacheTimeout != null) {
					applicationPathCacheTimeout = null;
				}
			}
		}
		return this;
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
		InitFile initFile;
		while (it.hasNext()) {
			entry = it.next();
			v = entry.getValue();
			if (v != null) {
				initFile = v.get();
				if (initFile != null) sct.setEL(entry.getKey(), initFile.getPageSource().getDisplayPath());
			}
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
		return componentPathCache.list();
	}

	@Override
	public void clearComponentCache() {
		componentPathCache.clear();
	}

	@Override
	public void clearApplicationCache() {
		if (applicationPathCache == null) return;
		applicationPathCache.clear();
	}

	@Override
	public ImportDefintion getComponentDefaultImport() {
		if (componentDefaultImport == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentDefaultImport")) {
				if (componentDefaultImport == null) {
					String strCDI = ConfigFactoryImpl.getAttr(this, root, "componentAutoImport");
					if (!StringUtil.isEmpty(strCDI, true)) {
						this.componentDefaultImport = ImportDefintionImpl.getInstance(strCDI.trim(), null);
					}
					if (this.componentDefaultImport == null) this.componentDefaultImport = new ImportDefintionImpl(Constants.DEFAULT_PACKAGE, "*");
				}
			}
		}
		return componentDefaultImport;
	}

	public ConfigImpl resetComponentDefaultImport() {
		if (componentDefaultImport != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentDefaultImport")) {
				if (componentDefaultImport != null) {
					this.componentDefaultImport = null;
				}
			}
		}
		return this;
	}

	protected void setComponentDefaultImport(String str) {

	}

	/**
	 * @return the componentLocalSearch
	 */
	@Override
	public boolean getComponentLocalSearch() {
		if (componentLocalSearch == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentLocalSearch")) {
				if (componentLocalSearch == null) {
					componentLocalSearch = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, root, "componentLocalSearch"), Boolean.TRUE);
				}
			}
		}
		return componentLocalSearch;
	}

	public ConfigImpl resetComponentLocalSearch() {
		if (componentLocalSearch != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getComponentLocalSearch")) {
				if (componentLocalSearch != null) {
					componentLocalSearch = null;
				}
			}
		}
		return this;
	}
	// = true

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

	@Override
	public Compress getCompressInstance(Resource zipFile, int format, boolean caseSensitive) throws IOException {
		return Compress.getInstance(zipFile, format, caseSensitive);
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
		if (clientStorage == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "")) {
				if (clientStorage == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "clientStorage");
					if (!StringUtil.isEmpty(str, true)) {
						clientStorage = str.trim();
					}
					else clientStorage = DEFAULT_STORAGE_CLIENT;
				}
			}
		}
		return clientStorage;
	}

	public ConfigImpl resetClientStorage() {
		if (clientStorage != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "")) {
				if (clientStorage != null) {
					clientStorage = null;
				}
			}
		}
		return this;
	}

	@Override
	public String getSessionStorage() {
		if (sessionStorage == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSessionStorage")) {
				if (sessionStorage == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "sessionStorage");
					if (!StringUtil.isEmpty(str, true)) {
						sessionStorage = str.trim();
					}
					else sessionStorage = DEFAULT_STORAGE_SESSION;
				}
			}
		}
		return sessionStorage;
	}

	public ConfigImpl resetSessionStorage() {
		if (sessionStorage != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSessionStorage")) {
				if (sessionStorage != null) {
					sessionStorage = null;
				}
			}
		}
		return this;
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

	private DebugEntry[] debugEntries;

	@Override
	public DebugEntry[] getDebugEntries() {
		if (debugEntries == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDebugEntries")) {
				if (debugEntries == null) {
					Array entries = ConfigUtil.getAsArray("debugTemplates", root);
					Map<String, DebugEntry> list = new HashMap<String, DebugEntry>();

					String id;
					if (entries != null) {
						Iterator<?> it = entries.getIterator();
						Struct e;
						while (it.hasNext()) {
							try {
								e = Caster.toStruct(it.next(), null);
								if (e == null) continue;
								id = ConfigFactoryImpl.getAttr(this, e, "id");
								list.put(id,
										new DebugEntry(id, ConfigFactoryImpl.getAttr(this, e, "type"), ConfigFactoryImpl.getAttr(this, e, "iprange"),
												ConfigFactoryImpl.getAttr(this, e, "label"), ConfigFactoryImpl.getAttr(this, e, "path"),
												ConfigFactoryImpl.getAttr(this, e, "fullname"), ConfigUtil.getAsStruct(this, e, true, "custom")));
							}
							catch (Throwable t) {
								ExceptionUtil.rethrowIfNecessary(t);
								ConfigFactoryImpl.log(this, t);
							}
						}
					}
					debugEntries = list.values().toArray(new DebugEntry[list.size()]);
				}
			}
		}
		return debugEntries;
	}

	public ConfigImpl resetDebugEntries() {
		if (debugEntries != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDebugEntries")) {
				if (debugEntries != null) {
					debugEntries = null;
				}
			}
		}
		return this;
	}

	@Override
	public DebugEntry getDebugEntry(String ip, DebugEntry defaultValue) {
		DebugEntry[] debugEntries = getDebugEntries();
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

	// debugMaxRecordsLogged = 10

	@Override
	public int getDebugMaxRecordsLogged() {
		if (debugMaxRecordsLogged == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDebugMaxRecordsLogged")) {
				if (debugMaxRecordsLogged == null) {
					String strMax = ConfigFactoryImpl.getAttr(this, root, "debuggingMaxRecordsLogged");
					if (StringUtil.isEmpty(strMax)) strMax = ConfigFactoryImpl.getAttr(this, root, "debuggingShowMaxRecordsLogged");
					if (!StringUtil.isEmpty(strMax)) {
						debugMaxRecordsLogged = Caster.toIntValue(strMax, 10);
					}
					else debugMaxRecordsLogged = 10;
				}
			}
		}
		return debugMaxRecordsLogged;
	}

	public ConfigImpl resetDebugMaxRecordsLogged() {
		if (debugMaxRecordsLogged != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDebugMaxRecordsLogged")) {
				if (debugMaxRecordsLogged != null) {
					debugMaxRecordsLogged = null;
				}
			}
		}
		return this;
	}

	private Boolean dotNotationUpperCase;

	@Override
	public boolean getDotNotationUpperCase() {
		if (dotNotationUpperCase == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDotNotationUpperCase")) {
				if (dotNotationUpperCase == null) {
					Boolean tmp = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.preserve.case", null), null);
					if (tmp != null) tmp = !tmp; // invert: lucee.preserve.case=true means dotNotationUpperCase=false
					if (tmp == null) tmp = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, root, "dotNotationUpperCase"), null);
					if (tmp == null) {
						tmp = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, root, "preserveCase"), null);
						if (tmp != null) tmp = !tmp;
					}
					if (tmp == null) dotNotationUpperCase = Boolean.TRUE;
					else dotNotationUpperCase = tmp;
				}
			}
		}
		return dotNotationUpperCase;
	}

	public ConfigImpl resetDotNotationUpperCase() {
		if (dotNotationUpperCase != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDotNotationUpperCase")) {
				if (dotNotationUpperCase != null) {
					dotNotationUpperCase = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean preserveCase() {
		return !getDotNotationUpperCase();
	}

	private Boolean defaultFunctionOutput;

	@Override
	public boolean getDefaultFunctionOutput() {
		if (defaultFunctionOutput == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDefaultFunctionOutput")) {
				if (defaultFunctionOutput == null) {
					String output = ConfigFactoryImpl.getAttr(this, root, "defaultFunctionOutput");
					defaultFunctionOutput = Caster.toBooleanValue(output, true);
				}
			}
		}
		return defaultFunctionOutput;
	}

	public ConfigImpl restDefaultFunctionOutput() {
		if (defaultFunctionOutput != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDefaultFunctionOutput")) {
				if (defaultFunctionOutput != null) {
					defaultFunctionOutput = null;
				}
			}
		}
		return this;
	}

	private Boolean getSuppressWSBeforeArg;

	@Override
	public boolean getSuppressWSBeforeArg() {
		if (getSuppressWSBeforeArg == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSuppressWSBeforeArg")) {
				if (getSuppressWSBeforeArg == null) {
					String suppress = SystemUtil.getSystemPropOrEnvVar("lucee.suppress.ws.before.arg", null);
					if (StringUtil.isEmpty(suppress, true)) {
						suppress = ConfigFactoryImpl.getAttr(this, root, new String[] { "suppressWhitespaceBeforeArgument", "suppressWhitespaceBeforecfargument" });
					}
					getSuppressWSBeforeArg = Caster.toBooleanValue(suppress, true);
				}
			}
		}
		return getSuppressWSBeforeArg;
	}

	public ConfigImpl restSuppressWSBeforeArg() {
		if (getSuppressWSBeforeArg != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSuppressWSBeforeArg")) {
				if (getSuppressWSBeforeArg != null) {
					getSuppressWSBeforeArg = null;
				}
			}
		}
		return this;
	}

	private RestSettings restSetting = new RestSettingImpl(false, UDF.RETURN_FORMAT_JSON);

	protected void setRestSetting(RestSettings restSetting) {
		this.restSetting = restSetting;
	}

	@Override
	public RestSettings getRestSetting() {
		return restSetting;
	}

	public int getMode() {
		if (mode == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getMode")) {
				if (mode == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "mode");
					if (!StringUtil.isEmpty(str, true)) {
						str = str.trim();
						if ("custom".equalsIgnoreCase(str)) mode = ConfigPro.MODE_CUSTOM;
						else if ("strict".equalsIgnoreCase(str)) mode = ConfigPro.MODE_STRICT;
						else mode = ConfigPro.MODE_CUSTOM;
					}
					else mode = ConfigPro.MODE_CUSTOM;
				}
			}
		}
		return mode;
	}

	public ConfigImpl resetMode() {
		if (mode != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getMode")) {
				if (mode != null) {
					mode = null;
				}
			}
		}
		return this;
	}

	// do not move to Config interface, do instead setCFMLWriterClass
	@Override
	public int getCFMLWriterType() {
		if (writerType == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCFMLWriterType")) {
				if (writerType == null) {
					String str = SystemUtil.getSystemPropOrEnvVar("lucee.cfml.writer", null);
					if (StringUtil.isEmpty(str)) {
						str = ConfigFactoryImpl.getAttr(this, root, "cfmlWriter");
					}

					// CB compatibility
					if (StringUtil.isEmpty(str, true)) {
						str = ConfigFactoryImpl.getAttr(this, root, "whitespaceManagement");
					}

					if (!StringUtil.isEmpty(str, true)) {
						str = str.trim();
						if ("white-space".equalsIgnoreCase(str)) writerType = ConfigPro.CFML_WRITER_WS;
						else if ("simple".equalsIgnoreCase(str)) writerType = ConfigPro.CFML_WRITER_WS;
						else if ("white-space-pref".equalsIgnoreCase(str)) writerType = ConfigPro.CFML_WRITER_WS_PREF;
						else if ("snart".equalsIgnoreCase(str)) writerType = ConfigPro.CFML_WRITER_WS_PREF;
						else if ("regular".equalsIgnoreCase(str)) writerType = ConfigPro.CFML_WRITER_REFULAR;
						else writerType = ConfigPro.CFML_WRITER_REFULAR;
					}
					else writerType = ConfigPro.CFML_WRITER_REFULAR;
				}
			}
		}
		return writerType;
	}

	public ConfigImpl resetCFMLWriterType() {
		if (writerType != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCFMLWriterType")) {
				if (writerType != null) {
					writerType = null;
				}
			}
		}
		return this;
	}

	private Boolean bufferOutput;

	private Integer externalizeStringGTE;
	private Map<String, BundleDefinition> extensionBundles;
	private JDBCDriver[] drivers;
	private Resource logDir;

	@Override
	public boolean getBufferOutput() {
		if (bufferOutput == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getBufferOutput")) {
				if (bufferOutput == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "bufferTagBodyOutput");
					if (!StringUtil.isEmpty(str, true)) {
						bufferOutput = Caster.toBoolean(str.trim(), DEFAULT_BUFFER_TAG_BODY_OUTPUT);
					}
					else bufferOutput = DEFAULT_BUFFER_TAG_BODY_OUTPUT;
				}
			}
		}
		return bufferOutput;
	}

	public ConfigImpl resetBufferOutput() {
		if (bufferOutput != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getBufferOutput")) {
				if (bufferOutput != null) {
					bufferOutput = null;
				}
			}
		}
		return this;
	}

	public int getDebugOptions() {
		if (debugOptions == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDebugOptions")) {
				if (debugOptions == null) {
					debugOptions = ConfigFactoryImpl.loadDebugOptions(this, root);
				}
			}
		}
		return debugOptions;
	}

	public ConfigImpl resetDebugOptions() {
		if (debugOptions != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDebugOptions")) {
				if (debugOptions != null) {
					debugOptions = null;
				}
			}
		}
		return this;
	}
	// = 0

	@Override
	public boolean hasDebugOptions(int debugOption) {
		return (getDebugOptions() & debugOption) > 0;
	}

	@Override
	public boolean checkForChangesInConfigFile() {
		if (checkForChangesInConfigFile == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "checkForChangesInConfigFile")) {
				if (checkForChangesInConfigFile == null) {
					String cFc = ConfigFactoryImpl.getAttr(this, root, "checkForChanges");
					if (!StringUtil.isEmpty(cFc, true)) {
						checkForChangesInConfigFile = Caster.toBoolean(cFc.trim(), Boolean.FALSE);
					}
					else checkForChangesInConfigFile = Boolean.FALSE;
				}
			}
		}
		return checkForChangesInConfigFile;
	}

	public ConfigImpl resetCheckForChangesInConfigFile() {
		if (checkForChangesInConfigFile != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "checkForChangesInConfigFile")) {
				if (checkForChangesInConfigFile != null) {
					checkForChangesInConfigFile = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getExternalizeStringGTE() {
		if (externalizeStringGTE == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getExternalizeStringGTE")) {
				if (externalizeStringGTE == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "externalizeStringGte");
					if (Decision.isNumber(str)) {
						externalizeStringGTE = Caster.toIntValue(str, -1);
					}
					else externalizeStringGTE = -1;
				}
			}
		}
		return externalizeStringGTE;
	}

	public ConfigImpl resetExternalizeStringGTE() {
		if (externalizeStringGTE != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getExternalizeStringGTE")) {
				if (externalizeStringGTE != null) {
					externalizeStringGTE = null;
				}
			}
		}
		return this;
	}

	protected void addConsoleLayout(Object layout) {
		consoleLayouts.add(layout);

	}

	protected void addResourceLayout(Object layout) {
		resourceLayouts.add(layout);
	}

	public Object[] getConsoleLayouts() throws PageException {
		if (consoleLayouts.isEmpty()) consoleLayouts.add(getLogEngine().getDefaultLayout());
		return consoleLayouts.toArray(new Object[consoleLayouts.size()]);

	}

	public Object[] getResourceLayouts() throws PageException {
		if (resourceLayouts.isEmpty()) resourceLayouts.add(getLogEngine().getClassicLayout());
		return resourceLayouts.toArray(new Object[resourceLayouts.size()]);
	}

	protected void clearLoggers(Boolean dyn) {
		if (loggers == null || loggers.size() == 0) return;
		synchronized (SystemUtil.createToken("ConfigImpl", "loggers")) {
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
			loggers = null;
		}
	}

	public Map<String, LoggerAndSourceData> getLoggers() {
		if (loggers == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "loggers")) {
				if (loggers == null) {
					if (root == null || insideLoggers.get()) {
						return new HashMap<String, LoggerAndSourceData>(); // avoid cycle loop
					}
					insideLoggers.set(true);
					try {
						loggers = ConfigFactoryImpl.loadLoggers(this, root);
					}
					finally {
						insideLoggers.set(false);
					}
				}
			}
		}
		return loggers;
	}

	public Map<String, LoggerAndSourceData> resetLoggers() {
		if (loggers != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "loggers")) {
				if (loggers != null) {
					loggers = null;
				}
			}
		}
		return loggers;
	}

	@Override
	public String[] getLogNames() {
		Set<String> keys = getLoggers().keySet();
		return keys.toArray(new String[keys.size()]);
	}

	@Override
	public Log getLog(String name) {
		try {
			return getLog(name, true);
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public Log getLog(String name, boolean createIfNecessary) throws PageException {
		if (root == null) return null;
		LoggerAndSourceData lsd = _getLoggerAndSourceData(name, createIfNecessary);
		if (lsd == null) return null;
		return lsd.getLog(false);
	}

	@Override
	public boolean isLoggingLoaded() {
		return loggers != null;
	}

	private LoggerAndSourceData _getLoggerAndSourceData(String name, boolean createIfNecessary) throws PageException {
		LoggerAndSourceData las = getLoggers().get(name.toLowerCase());
		if (las == null) {
			if (!createIfNecessary) return null;
			return ConfigFactoryImpl.addLogger(this, loggers, name, Log.LEVEL_ERROR, getLogEngine().appenderClassDefintion("console"), null,
					getLogEngine().layoutClassDefintion("pattern"), null, true, true);
		}
		return las;
	}

	@Override
	public Map<Key, Map<Key, Object>> getTagDefaultAttributeValues() {
		return tagDefaultAttributeValues == null ? null : Duplicator.duplicateMap(tagDefaultAttributeValues, new ConcurrentHashMap<Key, Map<Key, Object>>(), true);
	}

	protected void setTagDefaultAttributeValues(Map<Key, Map<Key, Object>> values) {
		this.tagDefaultAttributeValues = values;
	}

	@Override
	public Boolean getHandleUnQuotedAttrValueAsString() {
		if (handleUnQuotedAttrValueAsString == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getHandleUnQuotedAttrValueAsString")) {
				if (handleUnQuotedAttrValueAsString == null) {
					// Handle Unquoted Attribute Values As String
					String str = ConfigFactoryImpl.getAttr(this, root, "handleUnquotedAttributeValueAsString");
					if (str != null && Decision.isBoolean(str)) {
						handleUnQuotedAttrValueAsString = Caster.toBooleanValue(str, true);
					}
					else handleUnQuotedAttrValueAsString = true;
				}
			}
		}
		return handleUnQuotedAttrValueAsString;
	}

	public ConfigImpl resetHandleUnQuotedAttrValueAsString() {
		if (handleUnQuotedAttrValueAsString != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getHandleUnQuotedAttrValueAsString")) {
				if (handleUnQuotedAttrValueAsString != null) {
					handleUnQuotedAttrValueAsString = null;
				}
			}
		}
		return this;
	}

	@Override
	public Object getCachedWithin(int type) {
		if (cachedWithins == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCachedWithin")) {
				if (cachedWithins == null) {
					HashMap<Integer, Object> map = new HashMap<Integer, Object>();
					for (int i = 0; i < ConfigPro.CACHE_TYPES.length; i++) {
						try {
							String cw = ConfigFactoryImpl.getAttr(this, root, "cachedWithin" + StringUtil.ucFirst(ConfigPro.STRING_CACHE_TYPES[i]));
							if (!StringUtil.isEmpty(cw, true)) map.put(ConfigPro.CACHE_TYPES[i], cw.trim());
						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
							ConfigFactoryImpl.log(this, t);
						}
					}
					cachedWithins = map;
				}
			}
		}
		return cachedWithins.get(type);
		// = new HashMap<Integer, Object>()
	}

	public ConfigImpl resetCachedWithin() {
		if (cachedWithins != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCachedWithin")) {
				if (cachedWithins != null) {
					cachedWithins = null;
				}
			}
		}
		return this;
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

	@Override
	public String getSalt() {
		salt = null;// TEST PW
		if (salt == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSalt")) {
				if (salt == null) {
					String salt = ConfigFactoryImpl.getAttr(this, root, "salt");
					if (StringUtil.isEmpty(salt, true)) salt = ConfigFactoryImpl.getAttr(this, root, "adminSalt");
					// salt (every context need to have a salt)
					if (StringUtil.isEmpty(salt, true)) throw new RuntimeException("context is invalid, there is no salt!");
					this.salt = salt.trim();
				}
			}
		}
		return salt;
	}

	public ConfigImpl resetSalt() {
		if (salt != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getSalt")) {
				if (salt != null) {
					salt = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getPasswordType() {
		if (getPassword() == null) return Password.HASHED_SALTED;// when there is no password, we will have a HS password
		return getPassword().getType();
	}

	@Override
	public String getPasswordSalt() {
		if (getPassword() == null || getPassword().getSalt() == null) return getSalt();
		return getPassword().getSalt();
	}

	@Override
	public int getPasswordOrigin() {
		if (getPassword() == null) return Password.ORIGIN_UNKNOW;
		return getPassword().getOrigin();
	}

	@Override
	public Collection<BundleDefinition> getExtensionBundleDefintions() {
		if (this.extensionBundles == null) {
			RHExtension[] rhes = getRHExtensions();
			Map<String, BundleDefinition> extensionBundles = new HashMap<String, BundleDefinition>();

			for (RHExtension rhe: rhes) {
				BundleInfo[] bis;
				try {
					bis = rhe.getMetadata().getBundles();
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

	@Override
	public JDBCDriver[] getJDBCDrivers() {
		if (drivers == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getJDBCDrivers")) {
				if (drivers == null) {
					drivers = ConfigFactoryImpl.loadJDBCDrivers(this, root);
				}
			}
		}
		return drivers;
	}

	public ConfigImpl resetJDBCDrivers() {
		if (drivers != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getJDBCDrivers")) {
				if (drivers != null) {
					drivers = null;
				}
			}
		}
		return this;
	}

	@Override
	public JDBCDriver getJDBCDriverByClassName(String className, JDBCDriver defaultValue) {
		for (JDBCDriver d: getJDBCDrivers()) {
			if (d.cd.getClassName().equals(className)) return d;
		}
		return defaultValue;
	}

	@Override
	public JDBCDriver getJDBCDriverById(String id, JDBCDriver defaultValue) {
		if (!StringUtil.isEmpty(id)) {
			for (JDBCDriver d: getJDBCDrivers()) {
				if (d.id != null && d.id.equalsIgnoreCase(id)) return d;
			}
		}
		return defaultValue;
	}

	@Override
	public JDBCDriver getJDBCDriverByBundle(String bundleName, Version version, JDBCDriver defaultValue) {
		for (JDBCDriver d: getJDBCDrivers()) {
			if (d.cd.getName().equals(bundleName) && (version == null || version.equals(d.cd.getVersion()))) return d;
		}
		return defaultValue;
	}

	@Override
	public JDBCDriver getJDBCDriverByCD(ClassDefinition cd, JDBCDriver defaultValue) {
		for (JDBCDriver d: getJDBCDrivers()) {
			if (d.cd.getId().equals(cd.getId())) return d; // TODO comparing cd objects directly?
		}
		return defaultValue;
	}

	@Override
	public int getQueueMax() {
		if (queueMax == -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getQueueMax")) {
				if (queueMax == -1) {
					Integer max = Caster.toInteger(SystemUtil.getSystemPropOrEnvVar("lucee.queue.max", null), null);
					if (max == null) max = Caster.toInteger(ConfigFactoryImpl.getAttr(this, root, "requestQueueMax"), null);
					queueMax = Caster.toIntValue(max, 100);
				}
			}
		}
		return queueMax;
	}

	public ConfigImpl resetQueueMax() {
		if (queueMax != -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getQueueMax")) {
				if (queueMax != -1) {
					queueMax = -1;
				}
			}
		}
		return this;
	}

	@Override
	public long getQueueTimeout() {
		if (queueTimeout == -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getQueueTimeout")) {
				if (queueTimeout == -1) {
					Long timeout = Caster.toLong(SystemUtil.getSystemPropOrEnvVar("lucee.queue.timeout", null), null);
					if (timeout == null) timeout = Caster.toLong(ConfigFactoryImpl.getAttr(this, root, "requestQueueTimeout"), null);
					queueTimeout = Caster.toLongValue(timeout, 0L);
				}
			}
		}
		return queueTimeout;
	}

	public ConfigImpl resetQueueTimeout() {
		if (queueTimeout != -1) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getQueueTimeout")) {
				if (queueTimeout != -1) {
					queueTimeout = -1;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getQueueEnable() {
		if (queueEnable == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getQueueEnable")) {
				if (queueEnable == null) {
					Boolean enable = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.queue.enable", null), null);
					if (enable == null) enable = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, root, "requestQueueEnable"), null);
					queueEnable = Caster.toBooleanValue(enable, false);
				}
			}
		}
		return queueEnable;
	}

	public ConfigImpl resetQueueEnable() {
		if (queueEnable != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getQueueEnable")) {
				if (queueEnable != null) {
					queueEnable = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getCGIScopeReadonly() {
		if (cgiScopeReadonly == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCGIScopeReadonly")) {
				if (cgiScopeReadonly == null) {
					String strCGIReadonly = ConfigFactoryImpl.getAttr(this, root, "cgiScopeReadOnly");
					if (!StringUtil.isEmpty(strCGIReadonly, true)) {
						cgiScopeReadonly = Caster.toBooleanValue(strCGIReadonly.trim(), true);
					}
					else cgiScopeReadonly = Boolean.TRUE;
				}
			}
		}
		return cgiScopeReadonly;
	}

	public ConfigImpl resetCGIScopeReadonly() {
		if (cgiScopeReadonly != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCGIScopeReadonly")) {
				if (cgiScopeReadonly != null) {
					cgiScopeReadonly = null;
				}
			}
		}
		return this;
	}

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
					File file = new File(ConfigUtil.getCFMLEngineFactory(this).getResourceRoot(), "deploy");
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

	@Override
	public Map<String, ClassDefinition> getCacheDefinitions() {
		if (cacheDefinitions == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDefinitions")) {
				if (cacheDefinitions == null) {
					cacheDefinitions = ConfigFactoryImpl.loadCacheDefintions(this, root);
				}
			}
		}
		return cacheDefinitions;
	}

	public ConfigImpl resetCacheDefinitions() {
		if (cacheDefinitions != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCacheDefinitions")) {
				if (cacheDefinitions != null) {
					cacheDefinitions = null;
				}
			}
		}
		return this;
	}

	@Override
	public ClassDefinition getCacheDefinition(String className) {
		return getCacheDefinitions().get(className);
	}

	@Override
	public Resource getAntiSamyPolicy() {
		if (antiSamyPolicy == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getAntiSamyPolicy")) {
				if (antiSamyPolicy == null) {

					Resource secDir = getConfigDir().getRealResource("security");
					antiSamyPolicy = getConfigDir().getRealResource("antisamy-basic.xml");
					if (!antiSamyPolicy.exists() || newVersion) {
						if (!secDir.exists()) secDir.mkdirs();
						ConfigFactoryImpl.createFileFromResourceEL("/resource/security/antisamy-basic.xml", antiSamyPolicy);
					}

				}
			}
		}
		return antiSamyPolicy;
	}

	public GatewayMap getGatewayEntries() {
		if (gatewayEntries == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getGatewayEntries")) {
				if (gatewayEntries == null) {
					gatewayEntries = ConfigFactoryImpl.loadGatewayEL(this, root);
				}
			}
		}
		return gatewayEntries;
	}

	public ConfigImpl resetGatewayEntries() {
		if (gatewayEntries != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getGatewayEntries")) {
				if (gatewayEntries != null) {
					gatewayEntries = null;
				}
			}
		}
		return this;
	}

	private ClassDefinition wsHandlerCD;
	private boolean initWsHandlerCD = true;

	protected ClassDefinition getWSHandlerClassDefinition() {
		if (initWsHandlerCD) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getWSHandlerClassDefinition")) {
				if (initWsHandlerCD) {
					wsHandlerCD = ConfigFactoryImpl.loadWS(this, root, null);
					initWsHandlerCD = false;
				}
			}
		}
		return wsHandlerCD;
	}

	protected ConfigImpl resetWSHandlerClassDefinition() {
		if (!initWsHandlerCD) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getWSHandlerClassDefinition")) {
				if (!initWsHandlerCD) {
					wsHandlerCD = null;
					initWsHandlerCD = true;
				}
			}
		}
		return this;
	}

	boolean isEmpty(ClassDefinition cd) {
		return cd == null || StringUtil.isEmpty(cd.getClassName());
	}

	@Override
	public final boolean getFullNullSupport() {
		if (fullNullSupport == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getFullNullSupport")) {
				if (fullNullSupport == null) {
					boolean fns = false;
					String str = SystemUtil.getSystemPropOrEnvVar("lucee.full.null.support", null);
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, new String[] { "nullSupport", "fullNullSupport" });

					if (!StringUtil.isEmpty(str, true)) {
						fns = Caster.toBooleanValue(str, false);
					}
					fullNullSupport = fns;
				}
			}
		}
		return fullNullSupport;
	}

	public final ConfigImpl resetFullNullSupport() {
		if (fullNullSupport != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getFullNullSupport")) {
				if (fullNullSupport != null) {
					fullNullSupport = null;
				}
			}
		}
		return this;
	}

	private static LogEngine logEngine;

	@Override
	public LogEngine getLogEngine() {
		if (logEngine == null) {
			synchronized (token) {
				if (logEngine == null) {
					logEngine = LogEngine.newInstance(this);
				}
			}

		}
		return logEngine;
	}

	@Override
	public TimeSpan getCachedAfterTimeRange() {
		if (initCachedAfterTimeRange) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCachedAfterTimeRange")) {
				if (initCachedAfterTimeRange) {
					TimeSpan ts = null;
					String ca = ConfigFactoryImpl.getAttr(this, root, "cachedAfter");
					if (!StringUtil.isEmpty(ca)) ts = Caster.toTimespan(ca, null);
					if (ts != null && ts.getMillis() > 0) cachedAfterTimeRange = ts;
					initCachedAfterTimeRange = false;
				}
			}
		}
		return this.cachedAfterTimeRange;
	}

	public ConfigImpl resetCachedAfterTimeRange() {
		if (!initCachedAfterTimeRange) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getCachedAfterTimeRange")) {
				if (!initCachedAfterTimeRange) {
					cachedAfterTimeRange = null;
					initCachedAfterTimeRange = true;
				}
			}
		}
		return this;
	}

	@Override
	public Map<String, Startup> getStartups() {
		if (startups == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getStartups")) {
				if (startups == null) {
					startups = ConfigFactoryImpl.loadStartupHook(this, root);
				}
			}
		}
		return startups;
	}

	public ConfigImpl resetStartups() {
		if (startups != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getStartups")) {
				if (startups != null) {
					// Call finalize() on existing startup hook instances before clearing
					for (Startup startup: startups.values()) {
						try {
							Method fin = Reflector.getMethod(startup.instance.getClass(), "finalize", new Class[0], true, null);
							if (fin != null) {
								fin.invoke(startup.instance, new Object[0]);
							}
						}
						catch (Exception e) {
							// ignore - best effort cleanup
						}
					}
					startups = null;
				}
			}
		}
		return this;
	}

	@Override
	public Regex getRegex() {
		if (regex == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRegex")) {
				if (regex == null) regex = ConfigFactoryImpl.loadRegex(this, root, null);
				if (regex == null) regex = RegexFactory.toRegex(RegexFactory.TYPE_PERL, null);
			}
		}
		return regex;
	}

	public ConfigImpl resetRegex() {
		if (regex != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getRegex")) {
				if (regex != null) regex = null;
			}
		}
		return this;
	}

	@Override
	public boolean getPreciseMath() {
		if (preciseMath == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getPreciseMath")) {
				if (preciseMath == null) {
					boolean pm = false;
					String str = SystemUtil.getSystemPropOrEnvVar("lucee.precise.math", null);
					if (StringUtil.isEmpty(str, true)) str = ConfigFactoryImpl.getAttr(this, root, "preciseMath");

					if (!StringUtil.isEmpty(str, true)) {
						pm = Caster.toBooleanValue(str, false);
					}
					preciseMath = pm;
				}
			}
		}
		return preciseMath;
	}

	public ConfigImpl resetPreciseMath() {
		if (preciseMath != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getPreciseMath")) {
				if (preciseMath != null) {
					preciseMath = null;
				}
			}
		}
		return this;
	}

	protected void setMainLogger(String mainLoggerName) {
		if (!StringUtil.isEmpty(mainLoggerName, true)) this.mainLoggerName = mainLoggerName.trim();
	}

	@Override
	public String getMainLogger() {
		if (mainLoggerName == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getMainLogger")) {
				if (mainLoggerName == null) {
					try {

						// main logger
						String mainLogger = SystemUtil.getSystemPropOrEnvVar("lucee.logging.main", null);
						if (!StringUtil.isEmpty(mainLogger, true)) {
							mainLoggerName = mainLogger.trim();
						}
						else {
							mainLogger = ConfigFactoryImpl.getAttr(this, root, "mainLogger");
							if (!StringUtil.isEmpty(mainLogger, true)) {
								mainLoggerName = mainLogger.trim();
							}
						}
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						ConfigFactoryImpl.log(this, t);
					}
				}
				if (mainLoggerName == null) mainLoggerName = "application";
			}
		}

		return this.mainLoggerName;
	}

	public ConfigImpl resetMainLogger() {
		if (mainLoggerName != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getMainLogger")) {
				if (mainLoggerName != null) {
					mainLoggerName = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getFormUrlAsStruct() {
		if (formUrlAsStruct == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getFormUrlAsStruct")) {
				if (formUrlAsStruct == null) {
					String str = ConfigFactoryImpl.getAttr(this, root, "formUrlAsStruct");
					if (!StringUtil.isEmpty(str, true)) {
						formUrlAsStruct = Caster.toBoolean(str.trim(), Boolean.TRUE);
					}
					else formUrlAsStruct = Boolean.TRUE;
				}
			}
		}
		return formUrlAsStruct;
	}

	// = true
	public ConfigImpl resetFormUrlAsStruct() {
		if (formUrlAsStruct != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getFormUrlAsStruct")) {
				if (formUrlAsStruct != null) {
					formUrlAsStruct = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getReturnFormat() {
		if (returnFormat == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getReturnFormat")) {
				if (returnFormat == null) {
					String strRF = ConfigFactoryImpl.getAttr(this, root, "returnFormat");
					if (!StringUtil.isEmpty(strRF, true)) returnFormat = UDFUtil.toReturnFormat(strRF, UDF.RETURN_FORMAT_WDDX);
					else returnFormat = UDF.RETURN_FORMAT_WDDX;
				}
			}
		}
		return returnFormat;
	}

	public ConfigImpl resetReturnFormat() {
		if (returnFormat != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getReturnFormat")) {
				if (returnFormat != null) {
					returnFormat = null;
				}
			}
		}
		return this;
	}

	@Override
	public JavaSettings getJavaSettings(String id) {
		return javaSettingsInstances.get(id);
	}

	@Override
	public void setJavaSettings(String id, JavaSettings js) {
		javaSettingsInstances.put(id, js);
	}

	@Override
	public JavaSettings getJavaSettings() {
		if (javaSettings == null) {
			synchronized (javaSettingsInstances) {
				if (javaSettings == null) {
					javaSettings = ConfigFactoryImpl.loadJavaSettings(this, root, null);
					if (javaSettings == null) javaSettings = JavaSettingsImpl.getInstance(this, new StructImpl(), null);
				}
			}
		}
		return javaSettings;
	}

	public ConfigImpl resetJavaSettings() {
		if (javaSettings != null) {
			synchronized (javaSettingsInstances) {
				if (javaSettings != null) {
					javaSettings = null;
				}
			}
		}
		return this;
	}

	@Override
	public Resource getExtensionDirectory() {
		return getExtensionInstalledDir();
	}

	@Override
	public Resource getExtensionInstalledDir() {
		if (extInstalled == null) {
			synchronized (SystemUtil.createToken("extensions", "installed")) {
				if (extInstalled == null) {
					extInstalled = getConfigDir().getRealResource("extensions/installed");
					if (!extInstalled.exists()) extInstalled.mkdirs();
				}
			}
		}
		return extInstalled;
	}

	public ConfigImpl resetExtensionInstalledDir() {
		if (extInstalled != null) {
			synchronized (SystemUtil.createToken("extensions", "installed")) {
				if (extInstalled != null) {
					extInstalled = null;
				}
			}
		}
		return this;
	}

	@Override
	public Resource getExtensionAvailableDir() {
		if (extAvailable == null) {
			synchronized (SystemUtil.createToken("extensions", "available")) {
				if (extAvailable == null) {
					extAvailable = getConfigDir().getRealResource("extensions/available");
					if (!extAvailable.exists()) extAvailable.mkdirs();
				}
			}
		}
		return extAvailable;
	}

	public ConfigImpl resetExtensionAvailableDir() {
		if (extAvailable != null) {
			synchronized (SystemUtil.createToken("extensions", "available")) {
				if (extAvailable != null) {
					extAvailable = null;
				}
			}
		}
		return this;
	}

	public boolean newVersion() {
		return newVersion;
	}

	@Override
	public void reset() {
		// resources.reset();
		ormengines.clear();
		clearFunctionCache();
		clearCTCache();
		clearComponentCache();
		clearApplicationCache();
		clearLoggers(null);
		clearComponentMetadata();
		baseComponentPageSource = null;
	}

	public void resetAll() throws Exception {
		resetAll(null);
	}

	public void resetAll(ResetFilter filter) throws Exception {
		List<Method> methods = Reflector.getMethods(this.getClass());
		if (filter == null) {
			for (Method method: methods) {
				if (!method.getName().startsWith("reset") || method.getName().equals("reset") || method.getName().equals("resetAll") || method.getArgumentCount() != 0) continue;
				method.invoke(this);
			}
		}
		else {
			for (Method method: methods) {
				if (method.getArgumentCount() == 0 && filter.allow(method.getName())) method.invoke(this);
			}
		}
	}

	/*
	 * public static void main(String[] args) throws IOException { List<Method> methods; Resource res =
	 * ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Tmp3/wwwwww");
	 * ResourceUtil.deleteContent(res, null); DynamicInvoker.getInstance(res);
	 * 
	 * methods = Reflector.getMethods(lucee.runtime.config.ConfigWebImpl.class); methods =
	 * Reflector.getMethods(lucee.runtime.config.ConfigServerImpl.class);
	 * 
	 * int max = 5; for (Method method: methods) { if (!method.getName().startsWith("reset") ||
	 * method.getName().equals("reset") || method.getName().equals("resetAll") ||
	 * method.getArgumentCount() != 0) continue; print.e("->" +
	 * method.getDeclaringProviderClassNameWithSameAccess() + ":" +
	 * method.getDeclaringProviderClassName() + ":" + method.getName()); // if (--max == 0) break; } }
	 */
}