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
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.commons.collection.LinkedHashMapMaxSize;
import lucee.commons.collection.MapFactory;
import lucee.commons.date.TimeZoneConstants;
import lucee.commons.digest.Hash;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogEngine;
import lucee.commons.io.log.LogFactory;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.log.LoggerAndSourceData;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderDef;
import lucee.commons.io.res.ResourceProviderDefFactory;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.ResourcesImpl.InnerResourceProviderFactory;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.type.cache.CacheResourceProvider;
import lucee.commons.io.res.type.http.HTTPResourceProvider;
import lucee.commons.io.res.type.http.HTTPSResourceProvider;
import lucee.commons.io.res.type.s3.DummyS3ResourceProvider;
import lucee.commons.io.res.type.zip.ZipResourceProvider;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ByteSizeParser;
import lucee.commons.lang.CharsetX;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.PhysicalClassLoaderFactory;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.IPRange;
import lucee.commons.net.URLDecoder;
import lucee.loader.TP;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.ExtensionFilter;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Component;
import lucee.runtime.Mapping;
import lucee.runtime.MappingFactory;
import lucee.runtime.MappingImpl;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.ai.AIEngine;
import lucee.runtime.ai.AIEngineFactory;
import lucee.runtime.ai.AIEnginePool;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheConnectionFactory;
import lucee.runtime.cache.ram.RamCache;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.request.RequestCacheHandler;
import lucee.runtime.cache.tag.timespan.TimespanCacheHandler;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.cfx.customtag.CFXTagClass;
import lucee.runtime.cfx.customtag.CFXTagPoolImpl;
import lucee.runtime.cfx.customtag.JavaCFXTagClassFactory;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.component.ImportDefintionImpl;
import lucee.runtime.config.ConfigFactory.UpdateInfo;
import lucee.runtime.config.ConfigFactoryImpl.MonitorTemp;
import lucee.runtime.config.ConfigFactoryImpl.Path;
import lucee.runtime.config.ConfigUtil.CacheElement;
import lucee.runtime.config.LabelFactory.Label;
import lucee.runtime.config.Prop.Choice;
import lucee.runtime.config.gateway.GatewayMap;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.config.maven.MavenUpdateProvider.RepositoryFactory;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.customtag.InitFile;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceFactory;
import lucee.runtime.db.DataSourcePro;
import lucee.runtime.db.DatasourceConnectionFactory;
import lucee.runtime.db.JDBCDriver;
import lucee.runtime.db.JDBCDriverFactory;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.dump.DumpWriterEntry;
import lucee.runtime.dump.HTMLDumpWriter;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ExecutionLogFactory;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.engine.ThreadQueueImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.extension.Extension;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.ExtensionDefintionFactory;
import lucee.runtime.extension.ExtensionProvider;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.functions.other.CreateUUID;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.gateway.GatewayEntryFactory;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.listener.ModernAppListener;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.monitor.ActionMonitor;
import lucee.runtime.monitor.ActionMonitorCollector;
import lucee.runtime.monitor.ActionMonitorFatory;
import lucee.runtime.monitor.IntervallMonitor;
import lucee.runtime.monitor.Monitor;
import lucee.runtime.monitor.MonitorFactory;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.mail.ServerFactory;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.DummyORMEngine;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMConfigurationImpl;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.osgi.EnvClassLoader;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.regex.Regex;
import lucee.runtime.regex.RegexFactory;
import lucee.runtime.rest.RestSettingImpl;
import lucee.runtime.rest.RestSettings;
import lucee.runtime.schedule.Scheduler;
import lucee.runtime.schedule.SchedulerImpl;
import lucee.runtime.search.DummySearchEngine;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecretProvider;
import lucee.runtime.security.SecretProviderFactory;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.spooler.SpoolerEngine;
import lucee.runtime.spooler.SpoolerEngineImpl;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.type.scope.ClusterNotSupported;
import lucee.runtime.type.scope.ClusterRemote;
import lucee.runtime.type.scope.ClusterWrap;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.video.VideoExecuterNotSupported;
import lucee.transformer.dynamic.meta.Method;
import lucee.transformer.library.ClassDefinitionFactory;
import lucee.transformer.library.ClassDefinitionImpl;
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
public final class ConfigServerImpl implements ConfigServerPro {

	private static final long POOL_MAX_IDLE = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.datasource.pool.maxIdle", null), 60) * 1000L;
	public static final ClassDefinition<DummyORMEngine> DEFAULT_ORM_ENGINE = new ClassDefinitionImpl<DummyORMEngine>(DummyORMEngine.class);
	private static final long FIVE_SECONDS = 5000;

	public static ConfigServerImpl instance;

	//////////////////////////
	// no need to expose // TODO still use Prop
	//////////////////////////
	private TagLib[] cfmlTlds;
	private Resource tldFile;
	private FunctionLib cfmlFlds;
	private Resource fldFile;
	protected Mapping defaultFunctionMapping;
	protected final Map<String, Mapping> functionMappings = new ConcurrentHashMap<String, Mapping>();
	protected Mapping defaultTagMapping;
	protected final Map<String, Mapping> tagMappings = new ConcurrentHashMap<String, Mapping>();
	private RHExtensionProvider[] rhextensionProviders;
	private Map<String, ComponentMetaData> componentMetaData;
	private DumpWriterEntry[] dumpWriters;

	//////////////////////////
	// not read from config //
	//////////////////////////
	private Resource configFile;
	private Resource configDir;
	protected Struct root;
	private Integer mode;
	private static final double DEFAULT_VERSION = 5.0d;
	private static final ClassDefinition DEFAULT_SEARCH_ENGINE = new ClassDefinitionImpl(DummySearchEngine.class);
	private long loadTime;
	private final Map<String, PhysicalClassLoader> rpcClassLoaders = new ConcurrentHashMap<String, PhysicalClassLoader>();
	private PhysicalClassLoader directClassLoader;
	private boolean suppresswhitespace = false;
	private long timeOffset;
	private final String baseComponentTemplate = "Component.cfc";
	private PageSource baseComponentPageSource;
	private long sessionScopeDirSize = 1024 * 1024 * 100;
	private Resource sessionScopeDir;
	private Resource deployDir;
	private boolean newVersion;
	private AtomicBoolean insideLoggers = new AtomicBoolean(false);
	private boolean componentRootSearch = true;
	private long configFileLastModified;
	private final Map<String, DatasourceConnPool> pools = new ConcurrentHashMap<>();
	protected MappingImpl scriptMapping;
	private Class clusterClass = ClusterNotSupported.class;
	private Class videoExecuterClass = VideoExecuterNotSupported.class;
	private Map<Integer, CacheConnection> cacheDefaultConnection = null;
	private ClassLoader envClassLoader;
	private static Object token = new Object();
	private SpoolerEngine remoteClientSpoolerEngine;
	private Resource antiSamyPolicy;
	private Resource extAvailable;
	private Resource extInstalled;
	private SchedulerImpl scheduler;
	private List<Object> consoleLayouts = new ArrayList<>();
	private List<Object> resourceLayouts = new ArrayList<>();
	private Resource logDir;
	private Map<String, BundleDefinition> extensionBundles;
	private Map<String, SoftReference<ConfigUtil.CacheElement>> applicationPathCache = null;// new ArrayList<Page>();
	private Map<String, SoftReference<InitFile>> ctPatchCache = null;// new ArrayList<Page>();
	private Map<String, SoftReference<UDF>> udfCache = new ConcurrentHashMap<String, SoftReference<UDF>>();
	private final CFMLEngineImpl engine;
	private String libHash;
	private long localExtHash;
	private int localExtSize = -1;
	private Resource rootDir;
	private final UpdateInfo updateInfo;
	private IdentificationServer id;
	private SecurityManager defaultSecurityManager;
	private Map<String, SecurityManager> securityManagers = MapFactory.<String, SecurityManager>getConcurrentMap();
	private Map<String, SecurityManager> managers = MapFactory.<String, SecurityManager>getConcurrentMap();
	private Map<String, CFMLFactory> initContextes;
	private TagLib coreTLDs;
	private FunctionLib coreFLDs;
	private LinkedHashMapMaxSize<Long, String> previousNonces = new LinkedHashMapMaxSize<Long, String>(100);
	private Map<Key, String> placeHolderdata;
	private AtomicBoolean insidePlaceHolder = new AtomicBoolean(false);
	//////////////////////////
	//////////////////////////

	private static Prop<Boolean> metaOnlyFirstMatch = Prop.bool().keys("onlyFirstMatch").systemPropEnvVar("lucee.mapping.first").defaultValue(true).hidden();
	private final ConfigValue<Boolean> onlyFirstMatch = new ConfigValue<>(metaOnlyFirstMatch);

	private static Prop<CacheConnection> metaCacheConnection = Prop.custom(CacheConnectionFactory.getInstance(), Prop.TYPE_MAP).keys("caches").lowerCaseKeys().lowerCaseKeys()
			.access(SecurityManager.TYPE_DATASOURCE).description("Defines cache connections for data storage, sessions, and distributed locks."
					+ " Supports OSGi/Maven driver loading and specific role assignments like 'storage' or 'default' query caching.");
	private Map<String, CacheConnection> caches;

	public final static Prop<String> metaCacheDefaultConnectionNamesResource = Prop.str().keys("defaultResource", "cacheDefaultResource", "resource").parent("cache").deprecated()
			.description("default resource cache used");
	private final ConfigValue<String> cacheDefaultResource = new ConfigValue<>(metaCacheDefaultConnectionNamesResource);

	public final static Prop<String> metaCacheDefaultConnectionNamesFunction = Prop.str().keys("defaultFunction", "cacheDefaultFunction", "function").parent("cache").deprecated()
			.description("default function cache used");
	private final ConfigValue<String> cacheDefaultFunction = new ConfigValue<>(metaCacheDefaultConnectionNamesFunction);

	public final static Prop<String> metaCacheDefaultConnectionNamesInclude = Prop.str().keys("defaultInclude", "cacheDefaultInclude", "include").parent("cache").deprecated()
			.description("default include cache used");
	private final ConfigValue<String> cacheDefaultInclude = new ConfigValue<>(metaCacheDefaultConnectionNamesInclude);

	public final static Prop<String> metaCacheDefaultConnectionNamesQuery = Prop.str().keys("defaultQuery", "cacheDefaultQuery", "query").parent("cache").deprecated()
			.description("default query cache used");
	private final ConfigValue<String> cacheDefaultQuery = new ConfigValue<>(metaCacheDefaultConnectionNamesQuery);

	public final static Prop<String> metaCacheDefaultConnectionNamesTemplate = Prop.str().keys("defaultTemplate", "cacheDefaultTemplate", "template").parent("cache").deprecated()
			.description("default template cache used");
	private final ConfigValue<String> cacheDefaultTemplate = new ConfigValue<>(metaCacheDefaultConnectionNamesTemplate);

	public final static Prop<String> metaCacheDefaultConnectionNamesObject = Prop.str().keys("defaultObject", "cacheDefaultObject", "object").parent("cache").deprecated()
			.description("default object cache used");
	private final ConfigValue<String> cacheDefaultObject = new ConfigValue<>(metaCacheDefaultConnectionNamesObject);

	public final static Prop<String> metaCacheDefaultConnectionNamesFile = Prop.str().keys("defaultFile", "cacheDefaultFile", "file").parent("cache").deprecated()
			.description("default file cache used");
	private final ConfigValue<String> cacheDefaultFile = new ConfigValue<>(metaCacheDefaultConnectionNamesFile);

	public final static Prop<String> metaCacheDefaultConnectionNamesHTTP = Prop.str().keys("defaultHTTP", "cacheDefaultHTTP", "http").parent("cache").deprecated()
			.description("default http cache used");
	private final ConfigValue<String> cacheDefaultHTTP = new ConfigValue<>(metaCacheDefaultConnectionNamesHTTP);

	public final static Prop<String> metaCacheDefaultConnectionNamesWebservice = Prop.str().keys("defaultWebservice", "cacheDefaultWebservice", "webservice").parent("cache")
			.deprecated().description("default webservice cache used");
	private final ConfigValue<String> cacheDefaultWebservice = new ConfigValue<>(metaCacheDefaultConnectionNamesWebservice);

	private Map<Integer, String> cacheDefaultConnectionNames = null;

	private static Prop<DataSource> metaDatasourcesAll = Prop.custom(DataSourceFactory.getInstance(), Prop.TYPE_MAP).keys("dataSources").access(SecurityManager.TYPE_DATASOURCE)
			.lowerCaseKeys().description("Defines database connections. Supports loading drivers via OSGi bundles or Maven coordinates,"
					+ " cloud-native credential resolution, and advanced pooling controls like connection limits and live timeouts.");
	private Map<String, DataSource> dataSources;
	private Map<String, DataSource> datasourcesNoQoQ;

	@SuppressWarnings("unchecked")
	public final static Prop<Short> metaScopeType = Prop.shor().keys("scopeCascading", "scopeCascadingType").defaultValue(SCOPE_STANDARD)
			.choices(
					new Choice<Short>(SCOPE_STRICT, "strict").description(
							"High Performance: Scans 'Arguments', 'Local' (within functions), and 'Variables' scopes only. External scopes like URL/Form are ignored."),

					new Choice<Short>(SCOPE_SMALL, "small")
							.description("Balanced: Scans 'Arguments', 'Local', 'Variables', 'URL', and 'Form'. Excludes more expensive scopes like CGI and Cookie."),

					new Choice<Short>(SCOPE_STANDARD, "standard")
							.description("Standard CFML: Scans 'Arguments', 'Local', 'Variables', 'CGI', 'URL', 'Form', and 'Cookie'. Matches traditional CFML engine behavior."))
			.description("Defines the search strategy for unscoped variables. 'Strict' is recommended for modern, secure applications to prevent unintended scope injection.");
	private final ConfigValue<Short> scopeCascading = new ConfigValue<>(metaScopeType);

	public final static Prop<Boolean> metaAllowImplicidQueryCall = Prop.bool().keys("cascadeToResultset", "searchResults").systemPropEnvVar("lucee.cascade.to.resultset")
			.defaultValue(true).description(
					"When a variable has no scope defined (Example: #myVar# instead of #variables.myVar#), Lucee will also search available resultsets (CFML Standard) or not");
	private final ConfigValue<Boolean> cascadeToResultset = new ConfigValue<>(metaAllowImplicidQueryCall);

	private static Prop<Boolean> metaLimitEvaluation = Prop.bool().keys("limitEvaluation").systemPropEnvVar("lucee.security.isdefined", "lucee.isdefined.limit").defaultValue(false)
			.parent("security").description(
					"If enable you cannot use expression within \"[ ]\" like this susi[getVariableName()] . This affects the following functions [IsDefined, structGet, empty] and the following tags [savecontent attribute \"variable\"].");
	private final ConfigValue<Boolean> securityLimitEvaluation = new ConfigValue<>(metaLimitEvaluation);

	private static Prop<LoggerAndSourceData> metaLoggers = Prop.custom(LogFactory.getInstance(), Prop.TYPE_MAP).keys("loggers").logGlobal().lowerCaseKeys()
			.description("definition of all logs for Lucee");
	private volatile Map<String, LoggerAndSourceData> loggers;
	private static LogEngine logEngine;

	private static Prop<Boolean> metaDebugLogOutput = Prop.bool().keys("debuggingLogOutput").defaultValue(false).description(
			"When enabled, Lucee intercepts all response output and records each text fragment together with the source template and line number that produced it. The data is accessible via the debugger as a query (columns: text, template, line). Has no effect when debugging is disabled.");
	private final ConfigValue<Boolean> debuggingLogOutput = new ConfigValue<>(metaDebugLogOutput);

	// debug options
	public final static Prop<Boolean> metaDebugOptionsDatabase = Prop.bool().parent("monitoring").keys("debuggingDatabase", "debuggingShowDatabase")
			.systemPropEnvVar("lucee.monitoring.debuggingDatabase").defaultValue(false).access(SecurityManager.TYPE_DEBUGGING)
			.description("Select this option to log the database activity for the SQL Query events and Stored Procedure events.");
	private final ConfigValue<Boolean> debuggingDatabase = new ConfigValue<>(metaDebugOptionsDatabase);

	public final static Prop<Boolean> metaDebugOptionsException = Prop.bool().parent("monitoring").keys("debuggingException", "debuggingShowException")
			.systemPropEnvVar("lucee.monitoring.debuggingException").defaultValue(false).access(SecurityManager.TYPE_DEBUGGING)
			.description("Select this option to log all exceptions raised for the request.");
	private final ConfigValue<Boolean> debuggingException = new ConfigValue<>(metaDebugOptionsException);

	public final static Prop<Boolean> metaDebugOptionsTemplate = Prop.bool().parent("monitoring").keys("debuggingTemplate", "debuggingShowTemplate")
			.systemPropEnvVar("lucee.monitoring.debuggingTemplate").defaultValue(false).access(SecurityManager.TYPE_DEBUGGING)
			.description("Select this option log template activity for all cfm and cfc templates.");
	private final ConfigValue<Boolean> debuggingTemplate = new ConfigValue<>(metaDebugOptionsTemplate);

	public final static Prop<Boolean> metaDebugOptionsDump = Prop.bool().parent("monitoring").keys("debuggingDump", "debuggingShowDump")
			.systemPropEnvVar("lucee.monitoring.debuggingDump").defaultValue(false).access(SecurityManager.TYPE_DEBUGGING)
			.description("Select this option to enable output produced with help of the tag cfdump and send to debugging.");
	private final ConfigValue<Boolean> debuggingDump = new ConfigValue<>(metaDebugOptionsDump);

	public final static Prop<Boolean> metaDebugOptionsTracing = Prop.bool().parent("monitoring").keys("debuggingTracing", "debuggingShowTracing", "debuggingShowTrace")
			.systemPropEnvVar("lucee.monitoring.debuggingTracing").defaultValue(false).access(SecurityManager.TYPE_DEBUGGING)
			.description("Select this option to log trace event information. Tracing lets a developer track program flow and efficiency through the use of the CFTRACE tag.");
	private final ConfigValue<Boolean> debuggingTracing = new ConfigValue<>(metaDebugOptionsTracing);

	public final static Prop<Boolean> metaDebugOptionsTimer = Prop.bool().parent("monitoring").keys("debuggingTimer", "debuggingShowTimer")
			.systemPropEnvVar("lucee.monitoring.debuggingTimer").defaultValue(false).access(SecurityManager.TYPE_DEBUGGING).description(
					"Select this option to show timer event information. Timers let a developer track the execution time of the code between the start and end tags of the CFTIMER tag.");
	private final ConfigValue<Boolean> debuggingTimer = new ConfigValue<>(metaDebugOptionsTimer);

	public final static Prop<Boolean> metaDebugOptionsImplicitAccess = Prop.bool().parent("monitoring")
			.keys("debuggingImplicitAccess", "debuggingImplicitVariableAccess", "debuggingShowImplicitAccess").systemPropEnvVar("lucee.monitoring.debuggingImplicitAccess")
			.defaultValue(false).access(SecurityManager.TYPE_DEBUGGING)
			.description("Select this option to log all accesses to scopes, queries and threads that happens implicit (cascaded).");
	private final ConfigValue<Boolean> debuggingImplicitAccess = new ConfigValue<>(metaDebugOptionsImplicitAccess);

	public final static Prop<Boolean> metaDebugOptionsQueryUsage = Prop.bool().parent("monitoring").keys("debuggingQueryUsage", "debuggingShowQueryUsage")
			.systemPropEnvVar("lucee.monitoring.debuggingQueryUsage").defaultValue(false).access(SecurityManager.TYPE_DEBUGGING)
			.description("Select this option to also log query usage.");
	private final ConfigValue<Boolean> debuggingQueryUsage = new ConfigValue<>(metaDebugOptionsQueryUsage);

	public final static Prop<Boolean> metaDebugOptionsThread = Prop.bool().parent("monitoring").keys("debuggingThread", "debuggingShowThread")
			.systemPropEnvVar("lucee.monitoring.debuggingThread").defaultValue(false).access(SecurityManager.TYPE_DEBUGGING).description("Select this option to also log threads.");
	private final ConfigValue<Boolean> debuggingThread = new ConfigValue<>(metaDebugOptionsThread);

	public final static Prop<Boolean> metaSuppressContent = Prop.bool().keys("suppressContent").defaultValue(false)
			.description("Suppress content written to response stream when a Component is invoked remotely. Only works if the content was not flushed before.");
	private final ConfigValue<Boolean> suppressContent = new ConfigValue<>(metaSuppressContent);

	private static Prop<Boolean> metaShowVersion = Prop.bool().keys("showVersion").defaultValue(false)
			.description("deprected: expose Lucee version information in response header.");
	private final ConfigValue<Boolean> showVersion = new ConfigValue<>(metaShowVersion);

	private static Prop<String> metaTempDirectory = Prop.str().keys("tempDirectory");
	private Resource tempDirectory;
	private boolean tempDirectoryReload;

	public final static Prop<TimeSpan> metaClientTimeout = Prop.timespan().keys("clientTimeout").systemPropEnvVar("lucee.client.timeout")
			.defaultValue(new TimeSpanImpl(0, 0, 90, 0)).description("Sets the amount of time Lucee will keep the client scope alive.");
	private final ConfigValue<TimeSpan> clientTimeout = new ConfigValue<>(metaClientTimeout);

	public final static Prop<TimeSpan> metaSessionTimeout = Prop.timespan().keys("sessionTimeout").systemPropEnvVar("lucee.session.timeout")
			.defaultValue(new TimeSpanImpl(0, 0, 30, 0))
			.description("Sets the amount of time Lucee will keep the session scope alive. This behaviour can be overridden by the tag cfapplication.");
	private final ConfigValue<TimeSpan> sessionTimeout = new ConfigValue<>(metaSessionTimeout);

	public final static Prop<TimeSpan> metaApplicationTimeout = Prop.timespan().keys("applicationTimeout").systemPropEnvVar("lucee.application.timeout")
			.defaultValue(new TimeSpanImpl(1, 0, 0, 0))
			.description("Sets the amount of time Lucee will keep the application scope alive. This behaviour can be overridden by the tag cfapplication.");
	private final ConfigValue<TimeSpan> applicationTimeout = new ConfigValue<>(metaApplicationTimeout);

	public final static Prop<TimeSpan> metaRequestTimeoutOld = Prop.timespan().keys("requestTimeout").systemPropEnvVar("lucee.request.timeout").hidden()
			.defaultValue(new TimeSpanImpl(0, 0, 0, 0)).description("Defines how Lucee handles long running requests.");

	public final static Prop<TimeSpan> metaRequestTimeoutNew = Prop.timespan().parent("requestTimeout").keys("span", "value", "requestTimeout")
			.defaultValue(new TimeSpanImpl(0, 0, 0, 50)).description("Defines how Lucee handles long running requests.");
	private TimeSpan requestTimeout;

	public final static Prop<Float> metaRequestTimeoutMemorythreshold = Prop.procentage().parent("requestTimeout").keys("memorythreshold", "requestTimeoutMemoryThreshold")
			.defaultValue(0f).description("	\n"
					+ "This setting permits the establishment of a memory usage threshold, guiding Lucee on when to begin enforcing request timeouts based on the system's current memory consumption. The threshold value is a float from 0.0 (indicating 0% memory usage) to 1.0 (representing 100% memory usage). By monitoring memory usage against this threshold, Lucee intelligently decides whether to enforce or relax request timeouts, offering a dynamic approach to resource management. This is particularly advantageous for preventing system overloads and ensuring stable performance by not strictly applying timeouts when memory usage is below the defined threshold. The default threshold is set to 0.0, meaning Lucee will apply request timeouts without considering memory usage. Adjusting this threshold allows for more nuanced control over how your applications respond under various memory load conditions, optimizing both performance and reliability. This setting is only possible via System Property / Enviroment Variable.");
	private final ConfigValue<Float> requestTimeoutMemoryThreshold = new ConfigValue<>(metaRequestTimeoutMemorythreshold);

	public final static Prop<Float> metaRequestTimeoutCPUThreshold = Prop.procentage().parent("requestTimeout").keys("cputhreshold", "requestTimeoutCPUThreshold").defaultValue(0f)
			.description(
					"This configuration option allows you to set a CPU usage threshold that Lucee monitors before enforcing request timeouts. The threshold value is a float ranging from 0.0 (representing 0% CPU usage) to 1.0 (indicating 100% CPU usage). When the system's CPU usage is below this threshold, Lucee processes requests without applying the request timeout rule, enabling smoother handling under varying system loads. This mechanism is particularly useful for managing resource allocation and maintaining optimal application responsiveness, especially during periods of high demand or limited system resources. The default setting is 0.0, which means request timeouts are applied regardless of CPU usage. Adjusting this threshold provides a strategic lever to balance between system performance and request responsiveness, tailoring Lucee's behavior to your specific operational needs. This setting is only possible via System Property / Enviroment Variable.");
	private final ConfigValue<Float> requestTimeoutCPUThreshold = new ConfigValue<>(metaRequestTimeoutCPUThreshold);

	public final static Prop<Integer> metaRequestTimeoutConcurrentRequestThreshold = Prop.integer().parent("requestTimeout")
			.keys("concurrentrequestthreshold", "requestTimeoutConcurrentRequestThreshold").defaultValue(0).description(
					"This setting enables you to specify a threshold for the number of concurrent requests that Lucee can handle before beginning to enforce request timeouts. By adjusting this threshold, you can fine-tune how Lucee manages request timeouts under varying loads. A higher threshold allows more concurrent requests to be processed without enforcing timeouts, potentially improving performance under heavy load at the risk of longer request times. The default threshold is set to 0, meaning request timeouts are enforced immediately for all requests. This setting is only possible via System Property / Enviroment Variable.");
	private final ConfigValue<Integer> requestTimeoutConcurrentRequestThreshold = new ConfigValue<>(metaRequestTimeoutConcurrentRequestThreshold);

	public final static Prop<Boolean> metaSessionManagement = Prop.bool().keys("sessionManagement").systemPropEnvVar("lucee.session.management").defaultValue(true)
			.description("By default session management can be enabled. This behaviour can be overridden by the tag cfapplication.");
	private final ConfigValue<Boolean> sessionManagement = new ConfigValue<>(metaSessionManagement);

	public final static Prop<Boolean> metaClientManagement = Prop.bool().keys("clientManagement").systemPropEnvVar("lucee.client.management").defaultValue(false)
			.description("By default client management can be enabled. This behaviour can be overridden by the tag cfapplication.");
	private final ConfigValue<Boolean> clientManagement = new ConfigValue<>(metaClientManagement);

	public final static Prop<Boolean> metaClientCookies = Prop.bool().keys("clientCookies").defaultValue(true)
			.description("Enable or disable client cookies. This behaviour can be overridden by the tag cfapplication.");
	private final ConfigValue<Boolean> clientCookies = new ConfigValue<>(metaClientCookies);

	public final static Prop<Boolean> metaDevelopMode = Prop.bool().keys("developMode").defaultValue(DEFAULT_DEVELOP_MODE);
	private final ConfigValue<Boolean> developMode = new ConfigValue<>(metaDevelopMode);

	public final static Prop<Boolean> metaDomainCookies = Prop.bool().keys("domainCookies").defaultValue(false)
			.description("Enable or disable domain cookies. This behaviour can be overridden by the tag cfapplication.");
	private final ConfigValue<Boolean> domainCookies = new ConfigValue<>(metaDomainCookies);

	public final static Prop<String> metaSessionStorage = Prop.str().keys("sessionStorage").defaultValue(DEFAULT_STORAGE_SESSION)
			.description("The default storage for sessions can be set to \"memory\" for non-persistent in-memory data, \"file\" to store data on the local filesystem, "
					+ "or the specific name of a cache or datasource instance provided that \"Storage\" has been enabled for that instance.");
	private String sessionStorage;

	public final static Prop<String> metaClientStorage = Prop.str().keys("clientStorage").defaultValue(DEFAULT_STORAGE_CLIENT)
			.description("The default storage for client can be set to \"memory\" for non-persistent in-memory data, \"file\" to store data on the local filesystem, "
					+ "or the specific name of a cache or datasource instance provided that \"Storage\" has been enabled for that instance.");
	private String clientStorage;

	private static Prop<Integer> metaSpoolInterval = Prop.integer().keys("mailSpoolInterval").defaultValue(30).access(SecurityManager.TYPE_MAIL)
			.description("interval in seconds Lucee checks for new mails to send");
	private final ConfigValue<Integer> mailSpoolInterval = new ConfigValue<>(metaSpoolInterval);

	private static Prop<Boolean> metaSpoolEnable = Prop.bool().keys("mailSpoolEnable").defaultValue(true).access(SecurityManager.TYPE_MAIL)
			.description("if true, the mails are sent in a background thread and the main request does not have to wait until the mails are sent.");
	private final ConfigValue<Boolean> mailSpoolEnable = new ConfigValue<>(metaSpoolEnable);

	private static Prop<Boolean> metaSendPartial = Prop.bool().keys("mailSendPartial").defaultValue(false).access(SecurityManager.TYPE_MAIL).description(
			"This setting determines whether the SMTP protocol should deliver a message to all valid recipients when some addresses are invalid, rather than failing the entire delivery attempt if a single recipient is rejected.");
	private final ConfigValue<Boolean> mailSendPartial = new ConfigValue<>(metaSendPartial);//

	private static Prop<Boolean> metaUserSet = Prop.bool().keys("mailUserSet").defaultValue(true).access(SecurityManager.TYPE_MAIL).description(
			"This setting determines whether the SMTP protocol should explicitly use the sender's identity for the \"From\" address during the mail handshake, rather than relying on the default server identity or an automatically generated system address.");
	private final ConfigValue<Boolean> mailUserSet = new ConfigValue<>(metaUserSet);//

	private static Prop<CharsetX> metaMailDefaultCharset = Prop.charSet().keys("mailDefaultEncoding", "mailDefaultCharset").access(SecurityManager.TYPE_MAIL)
			.defaultValue(CharsetX.UTF8).description("default charset used for sending mails");
	private final ConfigValue<CharsetX> mailDefaultEncoding = new ConfigValue<>(metaMailDefaultCharset);

	private static Prop<Integer> metaMailTimeout = Prop.integer().keys("mailConnectionTimeout", "mailTimeout").access(SecurityManager.TYPE_MAIL).defaultValue(30)
			.description("default mail connection timeout in seconds");
	private final ConfigValue<Integer> mailConnectionTimeout = new ConfigValue<>(metaMailTimeout);

	private static Prop<Server> metaMailServers = Prop.custom(ServerFactory.getInstance(), Prop.TYPE_LIST).keys("mailServers").access(SecurityManager.TYPE_MAIL)
			.description("mailserver to use for sending mails.");
	private Server[] mailServers;

	private static Prop<String> metaExtensionProviders = Prop.str(Prop.TYPE_LIST).keys("extensionProviders")
			.description("Maven groupIds used to discover Lucee extensions. "
					+ "Lucee scans each groupId for artifacts whose artifactId ends with '-extension' (e.g. 'yaml-extension'). " + "Defaults to 'org.lucee'. "
					+ "Example: [\"org.lucee\", \"com.rasia\"]");
	private List<String> extensionProviders;

	@SuppressWarnings("unchecked")
	public final static Prop<Integer> metaReturnFormat = Prop.integer().keys("returnFormat").defaultValue(UDF.RETURN_FORMAT_WDDX).choices(
			new Choice<Integer>(UDF.RETURN_FORMAT_WDDX, "wddx").description("Web Distributed Data eXchange (WDDX) format. This is the legacy default for CFML remote calls."),
			new Choice<Integer>(UDF.RETURN_FORMAT_JSON, "json").description("Javascript Object Notation (JSON). The modern standard for web APIs and AJAX requests."),
			new Choice<Integer>(UDF.RETURN_FORMAT_PLAIN, "text", "plain").description("Returns the raw string output of the function without any additional serialization."),
			new Choice<Integer>(UDF.RETURN_FORMAT_JAVA, "java").description("Binary-encoded Java objects. Used for high-performance communication between Java-based systems."),
			new Choice<Integer>(UDF.RETURN_FORMAT_SERIALIZE, "cfm", "cfml", "serialize")
					.description("Lucee's internal object serialization format, ideal for passing complex objects between Lucee instances."))
			.description(
					"Defines the serialization format for remote function calls. While 'wddx' is the default for backward compatibility, 'json' is recommended for modern web applications.");
	private final ConfigValue<Integer> returnFormat = new ConfigValue<>(metaReturnFormat);

	public final static Prop<TimeZone> metaTimeZone = Prop.timezone().keys("timezone", "thisTimezone")
			.defaultValue(TimeZone.getDefault() != null ? TimeZone.getDefault() : TimeZoneConstants.UTC)
			.description("Define the desired time zone for Lucee. This will also change the time for the context of the web.");
	private final ConfigValue<TimeZone> timeZone = new ConfigValue<>(metaTimeZone);

	private static Prop<String> metaSearchEngineDirectory = Prop.str().keys("directory").parent("search").defaultValue("{lucee-web}/search/")
			.description("search engine directory");
	private String searchDirectory;

	public static final Prop<Locale> metaLocale = Prop.locale().keys("locale", "thisLocale").defaultValue(Locale.US).access(SecurityManager.TYPE_SETTING)
			.description("Define the desired time locale for Lucee, this will change the default locale for the context of the web.");
	private final ConfigValue<Locale> locale = new ConfigValue<>(metaLocale);

	public static final Prop<Boolean> metaPsq = Prop.bool().keys("preserveSingleQuote", "datasourcePreserveSingleQuotes", "psq").defaultValue(false)
			.description("Preserve single quotes (') in SQL defined with the cfquery tag.");
	private final ConfigValue<Boolean> preserveSingleQuote = new ConfigValue<>(metaPsq);

	public final static Prop<String> metaErrorTemplate500 = Prop.str().keys("errorGeneralTemplate", "generalErrorTemplate", "template500").access(SecurityManager.TYPE_DEBUGGING)
			.defaultValue("/lucee/templates/error/error." + (Constants.getCFMLTemplateExtensions()[0])).description(
					"This setting specifies the custom file path for the template rendered during all uncaught internal server exceptions, providing a tailored response for unexpected application failures.");
	private final ConfigValue<String> errorGeneralTemplate = new ConfigValue<>(metaErrorTemplate500);

	public final static Prop<String> metaErrorTemplate404 = Prop.str().keys("errorMissingTemplate", "missingErrorTemplate", "template404").access(SecurityManager.TYPE_DEBUGGING)
			.defaultValue("/lucee/templates/error/error." + (Constants.getCFMLTemplateExtensions()[0])).description(
					"This setting specifies the custom file path for the template rendered whenever a requested resource is not found on the server, ensuring a controlled and helpful response for status 404 missing page exceptions.");
	private final ConfigValue<String> errorMissingTemplate = new ConfigValue<>(metaErrorTemplate404);

	private static Prop<Password> metaPassword = Prop.custom(PasswordFactory.getInstance()).keys("hspw", "adminhspw", "adminpw", "pw", "adminpassword", "password")
			.systemPropEnvVar("lucee.admin.password").description("password used by Lucee to access the configuration, can be clear text or encrypted");
	protected Password hspw;
	private boolean initPassword = true;

	private static Prop<String> metaSalt = Prop.str().keys("salt", "adminSalt").systemPropEnvVar("lucee.admin.salt").description("salt used for password encryption");
	private String salt;

	private static Prop<Mapping> metaMappings = Prop.custom(MappingFactory.getInstance(MappingFactory.TYPE_REGULAR), Prop.TYPE_MAP).keys("mappings", "CFMappings").lowerCaseKeys()
			.description("Maps logical paths to storage locations, supporting local filesystems and virtual providers like S3. "
					+ "Includes controls for 'physical' vs 'archive' priority, 'toplevel' browser visibility, 'inspectTemplate' change-detection rules, "
					+ "and 'listener' configurations for Application.cfc discovery.");
	private Mapping[] uncheckedMappings;
	private Mapping[] mappings;

	private static Prop<Mapping> metaCustomTagMappings = Prop.custom(MappingFactory.getInstance(MappingFactory.TYPE_CUSTOM_TAG), Prop.TYPE_LIST)
			.keys("customTagMappings", "customTagPaths")
			.description("Acts as a component classpath. Maps virtual handles to local or cloud-based directories (S3, etc.) and .lar archives. "
					+ "Controls 'primary' source priority, 'inspectTemplate' caching, and 'toplevel' remote access for component resolution.");
	private Mapping[] uncheckedCustomTagMappings;
	private Mapping[] customTagMappings;

	private static Prop<Mapping> metaComponentMappings = Prop.custom(MappingFactory.getInstance(MappingFactory.TYPE_COMPONENT), Prop.TYPE_LIST)
			.keys("componentMappings", "componentPaths")
			.description("Acts as a component classpath. Maps virtual handles to local or cloud-based directories (S3, etc.) and .lar archives. "
					+ "Controls 'primary' source priority, 'inspectTemplate' caching, and 'toplevel' remote access for component resolution.");
	private Mapping[] uncheckedComponentMappings;
	private Mapping[] componentMappings;

	private static Prop<CFXTagClass> metaCfxTagPool = Prop.custom(JavaCFXTagClassFactory.getInstance(), Prop.TYPE_MAP).keys("cfx").access(SecurityManager.TYPE_CFX_SETTING)
			.deprecated().lowerCaseKeys().hidden();
	private CFXTagPool cfx;

	public final static Prop<Boolean> metaRestList = Prop.bool().keys("list").parent("rest").defaultValue(false).description("List Services when \"/rest/\" is called");
	private final ConfigValue<Boolean> restList = new ConfigValue<>(metaRestList);

	private static Prop<Boolean> metaRestSkipCFCWithError = Prop.bool().keys("skipCFCWithError").parent("rest").defaultValue(false)
			.description("Determines how the REST engine handles components that fail to compile or initialize during service registration. "
					+ "If enabled (true), Lucee will ignore problematic CFCs and continue registering the rest of the application. "
					+ "If disabled (false), a single error in one CFC will prevent the entire REST service from being registered.");
	private final ConfigValue<Boolean> restSkipCFCWithError = new ConfigValue<>(metaRestSkipCFCWithError);

	private static Prop<Integer> metaRestReturnFormat = Prop.integer().keys("returnFormat").parent("rest").defaultValue(UDF.RETURN_FORMAT_JSON).choices(
			new Choice<Integer>(UDF.RETURN_FORMAT_WDDX, "wddx").description("Legacy CFML serialization format. Not recommended for modern web applications."),

			new Choice<Integer>(UDF.RETURN_FORMAT_JSON, "json").description("Modern standard for web APIs. The default and most efficient format for client-side consumption."),

			new Choice<Integer>(UDF.RETURN_FORMAT_PLAIN, "text", "plain").description("Returns the raw string output of the function without any automated serialization logic."),

			new Choice<Integer>(UDF.RETURN_FORMAT_JAVA, "java").description("Binary-encoded Java objects. Only suitable for communication between compatible Java environments."),

			new Choice<Integer>(UDF.RETURN_FORMAT_SERIALIZE, "cfm", "cfml", "serialize")
					.description("Lucee's native serialization format. Best for transferring complex data between Lucee servers."),

			new Choice<Integer>(UDF.RETURN_FORMAT_XML, "xml").description("Structured XML output. Used primarily for legacy integrations or specific document-based protocols."))

			.description("Sets the default serialization format for REST responses when a specific format is not requested by the client or defined in the component.");
	private final ConfigValue<Integer> restReturnFormat = new ConfigValue<>(metaRestReturnFormat);
	private RestSettings restSetting;

	@SuppressWarnings("unchecked")
	private static Prop<Short> metaClientType = Prop.shor().keys("clientType").defaultValue(Config.CLIENT_SCOPE_TYPE_COOKIE)
			.choices(new Choice<Short>(Config.CLIENT_SCOPE_TYPE_FILE, "file"), new Choice<Short>(Config.CLIENT_SCOPE_TYPE_DB, "db", "database"),
					new Choice<Short>(Config.CLIENT_SCOPE_TYPE_COOKIE, "cookie"))
			.deprecated().description("defines how Lucee stores the client scope, default is cookie, possible values are: cookie,file,db");
	private Short clientType;

	public final static Prop<String> metaComponentDumpTemplate = Prop.str().keys("componentDumpTemplate").defaultValue("/lucee/component-dump.cfm")

			.description("If you call a component directly this template will be invoked to dump the component. (Example: http://localhost:8888/lucee/Admin.cfc)");
	private final ConfigValue<String> componentDumpTemplate = new ConfigValue<>(metaComponentDumpTemplate);

	@SuppressWarnings("unchecked")
	public final static Prop<Integer> metaComponentDataMemberDefaultAccess = Prop.integer().keys("componentDataMemberAccess", "componentDataMemberDefaultAccess")
			.defaultValue(Component.ACCESS_PUBLIC)
			.choices(new Choice<Integer>(Component.ACCESS_REMOTE, "remote").description("External/API: Allows data members to be accessed via remote protocols."),

					new Choice<Integer>(Component.ACCESS_PUBLIC, "public").description("Open: Data members are accessible from any other component or script (Default)."),

					new Choice<Integer>(Component.ACCESS_PACKAGE, "package")
							.description("Restricted: Data members are only accessible by components within the same directory/package."),

					new Choice<Integer>(Component.ACCESS_PRIVATE, "private")
							.description("Strict: Data members are only accessible within the component itself or by components that extend it."))
			.description(
					"Determines the default visibility for data members in the 'this' scope of a component. This allows you to control how internal state is exposed to the rest of the application or external consumers.");
	private final ConfigValue<Integer> componentDataMemberAccess = new ConfigValue<>(metaComponentDataMemberDefaultAccess);

	public final static Prop<Boolean> metaTriggerComponentDataMember = Prop.bool().keys("componentImplicitNotation", "triggerComponentDataMember", "triggerDataMember")
			.defaultValue(false).description(
					"If there is no accessible data member (property, element of the this scope) inside a component, Lucee searches for available matching \"getters\" or \"setters\" for the requested property. The following example should clarify this behaviour. \"somevar = myComponent.properyName\". If \"myComponent\" has no accessible data member named \"propertyName\", Lucee searches for a function member (method) named \"getPropertyName\".");
	private final ConfigValue<Boolean> componentImplicitNotation = new ConfigValue<>(metaTriggerComponentDataMember);

	@SuppressWarnings("unchecked")
	public final static Prop<Short> metaSessionType = Prop.shor().keys("sessionType").defaultValue(SESSION_TYPE_APPLICATION).choices(
			new Choice<Short>(Config.SESSION_TYPE_APPLICATION, "cfml", "cfm", "c", "application")
					.description("Lucee Native: Managed entirely by the engine. Uses 'CFID' and 'CFTOKEN' cookies. Does not require a restart to modify settings."),

			new Choice<Short>(Config.SESSION_TYPE_JEE, "j2ee", "jee", "j").description(
					"Servlet Container: Managed by the underlying server (e.g., Tomcat/Jetty). Uses the 'JSESSIONID' cookie. Better for integration with external Java filters or load balancers."))
			.description(
					"Specifies the session management engine. 'cfml' is the native Lucee implementation, while 'j2ee' delegates session tracking to the underlying servlet container.");
	private final ConfigValue<Short> sessionType = new ConfigValue<>(metaSessionType);

	private static Prop<String> metaDeployDirectory = Prop.str().keys("deployDirectory").parent("fileSystem").description("folder where Lucee stores template classes");
	private Resource fileSystemDeployDirectory;

	private static Prop<CharsetX> metaResourceCharset = Prop.charSet().keys("resourceCharset").systemPropEnvVar("lucee.resource.charset", "lucee.charset.resource")
			.defaultValue(SystemUtil.getCharsetX()).description("Default character set for reading from/writing to various resources");
	private final ConfigValue<CharsetX> resourceCharset = new ConfigValue<>(metaResourceCharset);

	private static Prop<CharsetX> metaTemplateCharset = Prop.charSet().keys("templateCharset").systemPropEnvVar("lucee.template.charset", "lucee.charset.template")
			.defaultValue(SystemUtil.getCharsetX()).description("Default character used to read templates (*.cfm and *.cfc files)");
	private final ConfigValue<CharsetX> templateCharset = new ConfigValue<>(metaTemplateCharset);

	private static Prop<CharsetX> metaWebCharset = Prop.charSet().keys("webCharset").systemPropEnvVar("lucee.web.charset", "lucee.charset.web").defaultValue(CharsetX.UTF8)
			.description("Default character set for output streams, form-, url-, and cgi scope variables and reading/writing the header");
	private final ConfigValue<CharsetX> webCharset = new ConfigValue<>(metaWebCharset);

	@SuppressWarnings("unchecked")
	private static Prop<Integer> metaApplicationListenerType = Prop.integer().keys("listenerType", "applicationListener").systemPropEnvVar("lucee.listener.type")
			.defaultValue(ApplicationListener.TYPE_MIXED)
			.choices(
					new Choice<Integer>(ApplicationListener.TYPE_NONE, "none")
							.description("Disabled: No application files are processed. Useful for high-performance microservices with no global state."),

					new Choice<Integer>(ApplicationListener.TYPE_CLASSIC, "classic").description("Legacy: Searches only for Application.cfm/OnRequestEnd.cfm files."),

					new Choice<Integer>(ApplicationListener.TYPE_MODERN, "modern").description("Modern: Searches only for Application.cfc components."),

					new Choice<Integer>(ApplicationListener.TYPE_MIXED, "mixed").description("Compatibility: Searches for both Application.cfc and Application.cfm. (Default)"))
			.description("Determines which types of application initialization files Lucee should detect and execute.");
	private final ConfigValue<Integer> listenerType = new ConfigValue<>(metaApplicationListenerType);

	@SuppressWarnings("unchecked")
	private static Prop<Integer> metaApplicationListenerMode = Prop.integer().keys("listenerMode", "applicationMode").systemPropEnvVar("lucee.listener.mode")
			.defaultValue(ApplicationListener.MODE_CURRENT2ROOT)
			.choices(
					new Choice<Integer>(ApplicationListener.MODE_CURRENT, "current", "curr")
							.description("Local Only: Checks only the directory containing the requested template."),

					new Choice<Integer>(ApplicationListener.MODE_CURRENT2ROOT, "currentToRoot", "currToRoot", "current2root", "curr2root")
							.description("Full Recursive: Searches the current directory and every parent directory until it reaches the webroot. (Standard CFML behavior)"),

					new Choice<Integer>(ApplicationListener.MODE_CURRENT_OR_ROOT, "currentOrRoot", "currOrRoot")
							.description("Local & Root: Checks the current directory; if not found, it checks the webroot, skipping intermediate parent directories."),

					new Choice<Integer>(ApplicationListener.MODE_ROOT, "root").description("Root Only: Checks only the webroot for application files."))
			.description(
					"Specifies the file-system search strategy for locating application files. Recursive searching (currentToRoot) is the most flexible but can add overhead on deep directory structures.");
	private final ConfigValue<Integer> listenerMode = new ConfigValue<>(metaApplicationListenerMode);

	private static Prop<Boolean> metaApplicationListenerSingleton = Prop.bool().keys("listenerSingleton", "applicationSingleton")
			.systemPropEnvVar("lucee.listener.singleton", "lucee.application.singleton").defaultValue(false).description(
					"When enabled, Lucee caches a single instance of Application.cfc for the life of the application, reducing overhead compared to the default behavior of reinstantiating it for every request.");
	private final ConfigValue<Boolean> listenerSingleton = new ConfigValue<>(metaApplicationListenerSingleton);
	private ApplicationListener applicationListener;

	public final static Prop<String> metaScriptProtect = Prop.str().keys("scriptProtect").systemPropEnvVar("lucee.script.protect").defaultValue("all")
			.description("Determines which scopes are sanitized for XSS protection, accepting 'all', 'none', or a comma-separated list of scopes like 'url,form,cookie,cgi'.");
	private Integer scriptProtect;

	private static Prop<Boolean> metaProxyEnabled = Prop.bool().keys("enabled").access(SecurityManager.TYPE_SETTING).parent("proxy").defaultValue(true).description("enable proxy");
	private final ConfigValue<Boolean> proxyEnabled = new ConfigValue<>(metaProxyEnabled);

	private static Prop<String> metaProxyServer = Prop.str().keys("server", "host", "updateProxyHost").access(SecurityManager.TYPE_SETTING).parent("proxy")
			.description("proxy host");
	private final ConfigValue<String> proxyServer = new ConfigValue<>(metaProxyServer);

	private static Prop<String> metaProxyUsername = Prop.str().keys("username", "user", "updateProxyUsername").access(SecurityManager.TYPE_SETTING).parent("proxy")
			.description("proxy username");
	private final ConfigValue<String> proxyUsername = new ConfigValue<>(metaProxyUsername);

	private static Prop<String> metaProxyPassword = Prop.str().keys("password", "pass", "updateProxyPassword").access(SecurityManager.TYPE_SETTING).parent("proxy")
			.description("proxy password");
	private final ConfigValue<String> proxyPassword = new ConfigValue<>(metaProxyPassword);

	private static Prop<Integer> metaProxyPort = Prop.integer().keys("port", "updateProxyPort").access(SecurityManager.TYPE_SETTING).parent("proxy").description("proxy port");
	private final ConfigValue<Integer> proxyPort = new ConfigValue<>(metaProxyPort);

	private static Prop<String> metaProxyIncludes = Prop.str().keys("includes").access(SecurityManager.TYPE_SETTING).parent("proxy").description("proxy includes");
	private final ConfigValue<String> proxyIncludes = new ConfigValue<>(metaProxyIncludes);

	private static Prop<String> metaProxyExcludes = Prop.str().keys("excludes").access(SecurityManager.TYPE_SETTING).parent("proxy").description("proxy excludes");
	private final ConfigValue<String> proxyExcludes = new ConfigValue<>(metaProxyExcludes);
	private ProxyData proxy = null;

	private static Prop<String> metaClientScopeDir = Prop.str().keys("clientDirectory").description("client scope directory");
	private Resource clientDirectory;

	private static Prop<String> metaClientScopeDirSize = Prop.str().keys("clientDirectoryMaxSize").systemPropEnvVar("lucee.client.directory.max.size").defaultValue("100mb")

			.description("Defines the maximum allowable disk space for the client scope storage directory, accepting values with unit suffixes like kb, mb, gb, or tb.");
	private Long clientDirectoryMaxSize;

	private static Prop<String> metaCacheDir = Prop.str().keys("cacheDirectory").defaultValue("100mb").description("Defines the directory used by Lucee for file-based caching.");
	private Resource cacheDirectory;

	private static Prop<String> metaCacheDirSize = Prop.str().keys("cacheDirectoryMaxSize").systemPropEnvVar("lucee.cache.directory.max.size").defaultValue("100mb")
			.description("Defines the maximum allowable disk space for the cache directory, accepting values with unit suffixes like kb, mb, gb, or tb.");
	private Long cacheDirectoryMaxSize;

	public final static Prop<Boolean> metaUseComponentShadow = Prop.bool().keys("componentUseVariablesScope", "useShadow").defaultValue(true)
			.description("Defines whether a component has an independent variables scope parallel to the \"this\" scope (CFML standard) or not.");
	private final ConfigValue<Boolean> componentUseVariablesScope = new ConfigValue<>(metaUseComponentShadow);

	private static Prop<String> metaOut = Prop.str().keys("systemOut").systemPropEnvVar("lucee.system.out").defaultValue("system").access(SecurityManager.TYPE_SETTING).description(
			"Specifies the destination for standard system output, supporting 'system', 'log', 'null', file paths via 'file:', or custom PrintWriter implementations via 'class:'");
	private PrintWriter systemOut;

	private static Prop<String> metaErr = Prop.str().keys("systemErr").systemPropEnvVar("lucee.system.err").defaultValue("system").access(SecurityManager.TYPE_SETTING).description(
			"Defines the destination for standard error output, supporting values like 'system', 'log', 'null', or specific paths using 'file:' and custom implementations via 'class:'.");
	private PrintWriter systemErr;

	public final static Prop<Boolean> metaDoCustomTagDeepSearch = Prop.bool().keys("customTagDeepSearch", "customTagSearchSubdirectories").access(SecurityManager.TYPE_CUSTOM_TAG)
			.defaultValue(false).description("Search for custom tags in subdirectories.");
	private final ConfigValue<Boolean> customTagDeepSearch = new ConfigValue<>(metaDoCustomTagDeepSearch);

	public final static Prop<Boolean> metaDoComponentTagDeepSearch = Prop.bool().keys("componentDeepSearch", "componentSearchSubdirectories").defaultValue(false)
			.description("Search for CFCs in the subdirectories.");
	private final ConfigValue<Boolean> componentDeepSearch = new ConfigValue<>(metaDoComponentTagDeepSearch);

	private static Prop<Double> metaVersion = Prop.dbl().keys("version").defaultValue(DEFAULT_VERSION).hidden().noEnvVar();
	private final ConfigValue<Double> version = new ConfigValue<>(metaVersion);

	private static Prop<Boolean> metaCloseConnection = Prop.bool().keys("closeConnection").defaultValue(false).description(
			"This setting specifies whether every HTTP response should instruct the client and any intermediate proxies to terminate the network connection immediately after the request is fulfilled, preventing the connection from being reused for additional traffic.");
	private final ConfigValue<Boolean> closeConnection = new ConfigValue<>(metaCloseConnection);

	public final static Prop<Boolean> metaContentLength = Prop.bool().keys("contentLength").defaultValue(true).deprecated();
	private final ConfigValue<Boolean> contentLength = new ConfigValue<>(metaContentLength);

	public final static Prop<Boolean> metaAllowCompression = Prop.bool().keys("allowCompression").systemPropEnvVar("lucee.allow.compression")
			.defaultValue(ConfigPro.DEFAULT_ALLOW_COMPRESSION).description(
					"This setting determines whether the response stream is compressed using GZIP; when enabled, Lucee inspects the client's request headers and, if the client supports it, automatically compresses the output to reduce bandwidth usage and improve page load times.");
	private final ConfigValue<Boolean> allowCompression = new ConfigValue<>(metaAllowCompression);

	public final static Prop<Boolean> metaDoLocalCustomTag = Prop.bool().keys("customTagLocalSearch", "customTagSearchLocal").access(SecurityManager.TYPE_CUSTOM_TAG)
			.defaultValue(true).description("look for custom tags locally.");
	private final ConfigValue<Boolean> customTagLocalSearch = new ConfigValue<>(metaDoLocalCustomTag);

	private static Prop<Struct> metaConstants = Prop.sct().keys("constants").defaultValue(new StructImpl()).hidden();
	private Struct constants = null;

	public final static Prop<Boolean> metaAllowURLRequestTimeout = Prop.bool().keys("requestTimeoutInURL", "allowUrlRequesttimeout").defaultValue(false)
			.description("Defines if it is possible to overwrite the request timeout with the query string [requestTimeout] in the URL.");
	private Boolean requestTimeoutInURL;

	public final static Prop<Boolean> metaErrorStatusCode = Prop.bool().keys("errorStatusCode", "statuscode").systemPropEnvVar("lucee.status.code").defaultValue(true)
			.access(SecurityManager.TYPE_DEBUGGING).description("In case of an exception, should individual status codes be returned? Untick to always return 200 status code.");
	private final ConfigValue<Boolean> errorStatusCode = new ConfigValue<>(metaErrorStatusCode);

	@SuppressWarnings("unchecked")
	public final static Prop<Integer> metaLocalMode = Prop.integer().keys("localScopeMode").defaultValue(Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS).choices(
			new Choice<Integer>(Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS, "always", "modern", Boolean.TRUE).description(
					"Modern: Unscoped assignments inside a function are automatically placed in the 'local' scope. Encourages cleaner code and prevents variable leaks to the 'variables' scope."),

			new Choice<Integer>(Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS, "update", "classic", Boolean.FALSE).description(
					"Classic: Unscoped assignments only update the 'local' scope if the key already exists there; otherwise, they are created in the 'variables' scope. (Legacy default)"))
			.description(
					"Controls how unscoped variable assignments behave inside functions. The 'always' mode is recommended for modern applications to ensure better encapsulation and thread safety.");
	private final ConfigValue<Integer> localScopeMode = new ConfigValue<>(metaLocalMode);

	private static Prop<Boolean> metaAllowRealPath = Prop.bool().keys("allowRealpath").parent("fileSystem").defaultValue(true)
			.description("If true, Lucee resolves file paths used in code against the current context.");
	private final ConfigValue<Boolean> fileSystemAllowRealpath = new ConfigValue<>(metaAllowRealPath);

	public final static Prop<String> metaCustomTagExtensions = Prop.str().keys("customTagExtensions").defaultValue(ListUtil.arrayToList(Constants.getExtensions(), ","))
			.access(SecurityManager.TYPE_CUSTOM_TAG).description("this are the file extensions Lucee allows for custom tags.");
	private String[] customTagExtensions = null;

	public final static Prop<Boolean> metaTypeChecking = Prop.bool().keys("typeChecking", "UDFTypeChecking").systemPropEnvVar("lucee.type.checking", "lucee.udf.type.checking")
			.defaultValue(true).description("check the types defined with function arguments and return type");
	private final ConfigValue<Boolean> typeChecking = new ConfigValue<>(metaTypeChecking);

	private static Prop<String> metaDapSecret = Prop.str().keys("secret").parent("dap")
			.description("Security token used to authenticate the connection between the IDE and Lucee's Debug Adapter Protocol (DAP) server. "
					+ "This ensures that only authorized clients can attach to the process for step debugging,access variable scopes, or trigger programmatic breakpoints.");
	private final ConfigValue<String> dapSecret = new ConfigValue<>(metaDapSecret);

	private static Prop<Boolean> metaDapBreakpoint = Prop.bool().keys("breakpoint").parent("dap").defaultValue(false)
			.description("Enables zero-overhead instrumentation for step debugging and breakpoints. When enabled, Lucee leverages its internal execution hooks to monitor for "
					+ "registered breakpoints and the programmatic breakpoint() BIF. " + "Unlike traditional JDWP debugging, this event-driven approach incurs "
					+ "virtually no performance penalty when no breakpoints are hit, " + "eliminating the need for slow bytecode rewriting.");
	private final ConfigValue<Boolean> dapBreakpoint = new ConfigValue<>(metaDapBreakpoint);

	private static Prop<Boolean> metaExecutionLogEnabled = Prop.bool().keys("enabled").parent("executionLog").defaultValue(false)
			.description("Enables execution time logging. When enabled without an explicit class, Lucee uses DebuggerExecutionLog for breakpoint support.");
	private final ConfigValue<Boolean> executionLogEnabled = new ConfigValue<>(metaExecutionLogEnabled);

	private static ImportDefintion DEFAULT_IMPORT_DEFINITION = new ImportDefintionImpl(Constants.DEFAULT_PACKAGE, "*");
	public final static Prop<String> metaComponentDefaultImport = Prop.str().keys("componentAutoImport", "componentDefaultImport")
			.defaultValue(DEFAULT_IMPORT_DEFINITION.toString())
			.description("Defines a package that is automatically imported for all components. Defaults to 'org.lucee.cfml.*' and requires the wildcard suffix.");
	private ImportDefintion componentAutoImport;

	public final static Prop<Boolean> metaComponentLocalSearch = Prop.bool().keys("componentLocalSearch").defaultValue(true)
			.description("If enabled, Lucee looks for component relative to the local position.");
	private final ConfigValue<Boolean> componentLocalSearch = new ConfigValue<>(metaComponentLocalSearch);

	public final static Prop<Boolean> metaUseComponentPathCache = Prop.bool().keys("componentUseCachePath", "componentPathCache").defaultValue(true)
			.description("If enabled, Lucee caches the resolved location of components, speeding up subsequent access.");
	private final ConfigValue<Boolean> componentUseCachePath = new ConfigValue<>(metaUseComponentPathCache);

	public final static Prop<Boolean> metaUseCTPathCache = Prop.bool().keys("customTagUseCachePath", "customTagCachePaths").access(SecurityManager.TYPE_CUSTOM_TAG)
			.defaultValue(true).description("If enabled, Lucee caches the resolved location of custom tags, speeding up subsequent access.");
	private final ConfigValue<Boolean> customTagUseCachePath = new ConfigValue<>(metaUseCTPathCache);

	@SuppressWarnings("unchecked")
	public final static Prop<Integer> metaCfmlWriter = Prop.integer().keys("cfmlWriter", "whitespaceManagement").systemPropEnvVar("lucee.cfml.writer")
			.defaultValue(ConfigPro.CFML_WRITER_REGULAR)
			.choices(
					new Choice<Integer>(ConfigPro.CFML_WRITER_REGULAR, "regular", "normal")
							.description("Standard: Outputs the generated content exactly as written in the source files, including all indentation and line breaks."),

					new Choice<Integer>(ConfigPro.CFML_WRITER_WS, "white-space", "simple").description(
							"Basic Compression: Aggressively removes most whitespace and line breaks. Fast execution, but can occasionally impact the layout of pre-formatted text."),

					new Choice<Integer>(ConfigPro.CFML_WRITER_WS_PREF, "white-space-pref", "smart").description(
							"Advanced Optimization: Uses an intelligent algorithm to remove unnecessary whitespace while preserving critical spacing (e.g., inside 'pre' or 'textarea' tags)."))
			.description(
					"Specifies the white-space management strategy for the output stream. Using 'smart' optimization can significantly reduce page weight without breaking HTML layout.");
	protected final ConfigValue<Integer> cfmlWriter = new ConfigValue<>(metaCfmlWriter);

	// "lucee.mvn.repo.snapshots"

	@SuppressWarnings("unchecked")
	private static Prop<Repository> metaMavenSnapshotRepository = Prop.custom(RepositoryFactory.getInstance(MavenUpdateProvider.TYPE_SNAPSHOT), Prop.TYPE_LIST)
			.keys("snapshotRepository").parent("maven").systemPropEnvVar("lucee.mvn.repo.snapshots").description(
					"Specifies the remote repositories used for 'Snapshot' versions of Lucee artifacts. " + "This allows the engine to pull pre-release core updates (.lco) or "
							+ "experimental extension versions (.lex) for testing and development. Defaults to the Sonatype Snapshot repository.");
	private Repository[] mavenSnapshotRepository;
	@SuppressWarnings("unchecked")
	private static Prop<Repository> metaMavenRepository = Prop.custom(RepositoryFactory.getInstance(MavenUpdateProvider.TYPE_RELEASE), Prop.TYPE_LIST)
			.keys("repository", "releaseRepository").parent("maven").systemPropEnvVar("lucee.mvn.repo.releases")
			.description("Specifies the remote repositories used to resolve stable Lucee artifacts. " + "This includes the Lucee Core (.lco files), the Lucee Loader (.jar), "
					+ "Lucee Extensions (.lex), and standard third-party Java libraries. By default, this points to Maven Central, ensuring the engine can "
					+ "autonomously download necessary components to maintain its modular core.");
	private Repository[] mavenRepository;

	@SuppressWarnings("unchecked")
	private static Prop<Integer> metaMavenDownloadPolicyStartup = Prop.integer().keys("downloadPolicyStartup").parent("maven")
			.systemPropEnvVar("lucee.maven.download.policy.startup").defaultValue(ConfigPro.MAVEN_DOWNLOAD_POLICY_IGNORE)
			.choices(new Choice<Integer>(ConfigPro.MAVEN_DOWNLOAD_POLICY_IGNORE, "ignore").description("Download Maven artifacts silently during startup."),
					new Choice<Integer>(ConfigPro.MAVEN_DOWNLOAD_POLICY_WARN, "warn", "warning").description("Download Maven artifacts during startup but log each download."),
					new Choice<Integer>(ConfigPro.MAVEN_DOWNLOAD_POLICY_ERROR, "error")
							.description("Block all Maven artifact downloads during startup. May prevent Lucee from starting if required artifacts are missing."))
			.description("Controls whether Lucee is allowed to download Maven artifacts during startup.");
	protected final ConfigValue<Integer> mavenDownloadPolicyStartup = new ConfigValue<>(metaMavenDownloadPolicyStartup);

	@SuppressWarnings("unchecked")
	private static Prop<Integer> metaMavenDownloadPolicyRuntime = Prop.integer().keys("downloadPolicyRuntime").parent("maven")
			.systemPropEnvVar("lucee.maven.download.policy.runtime").defaultValue(ConfigPro.MAVEN_DOWNLOAD_POLICY_IGNORE).choices(metaMavenDownloadPolicyStartup.getChoices())
			.description("Controls whether Lucee is allowed to download Maven artifacts at runtime, after startup is complete.");
	protected final ConfigValue<Integer> mavenDownloadPolicyRuntime = new ConfigValue<>(metaMavenDownloadPolicyRuntime);

	@SuppressWarnings("unchecked")
	private static Prop<Integer> metaMavenDownloadPolicyLogLevel = Prop.integer().keys("downloadPolicyLogLevel").parent("maven")
			.systemPropEnvVar("lucee.maven.download.policy.log.level").defaultValue(Log.LEVEL_WARN)
			.choices(new Choice<Integer>(Log.LEVEL_TRACE, "trace").description("Log at trace level."),
					new Choice<Integer>(Log.LEVEL_DEBUG, "debug").description("Log at debug level."), new Choice<Integer>(Log.LEVEL_INFO, "info").description("Log at info level."),
					new Choice<Integer>(Log.LEVEL_WARN, "warn").description("Log at warn level."), new Choice<Integer>(Log.LEVEL_ERROR, "error").description("Log at error level."),
					new Choice<Integer>(Log.LEVEL_FATAL, "fatal").description("Log at fatal level."))
			.description("Log level used when the Maven download policy is set to 'warn'. Has no effect when the policy is 'ignore' or 'error'.");
	protected final ConfigValue<Integer> mavenDownloadPolicyLogLevel = new ConfigValue<>(metaMavenDownloadPolicyLogLevel);

	private static Prop<Boolean> metaHandleUnquotedAttributeValueAsString = Prop.bool().keys("handleUnquotedAttributeValueAsString").defaultValue(true)
			.description("Controls if unquoted tag attributes are treated as literal strings (true) or as variable references (false) for evaluation.");
	private final ConfigValue<Boolean> handleUnquotedAttributeValueAsString = new ConfigValue<>(metaHandleUnquotedAttributeValueAsString);

	public final static Prop<Integer> metaQueueMax = Prop.integer().keys("requestQueueMax").systemPropEnvVar("lucee.queue.max").defaultValue(100)
			.description("Maximum number of requests allowed in the queue.");
	private final ConfigValue<Integer> requestQueueMax = new ConfigValue<>(metaQueueMax);

	public final static Prop<Long> metaQueueTimeout = Prop.loong().keys("requestQueueTimeout").systemPropEnvVar("lucee.queue.timeout").defaultValue(0L)
			.description("timeout for an element in the queue in milliseconds");
	private final ConfigValue<Long> requestQueueTimeout = new ConfigValue<>(metaQueueTimeout);

	public final static Prop<Boolean> metaQueueEnable = Prop.bool().keys("requestQueueEnable").systemPropEnvVar("lucee.queue.enable").defaultValue(false)
			.description("defines if Lucee uses a queue for incoming request or not.");
	private final ConfigValue<Boolean> requestQueueEnable = new ConfigValue<>(metaQueueEnable);

	@SuppressWarnings("unchecked")
	private static Prop<Integer> metaVarUsage = Prop.integer().keys("variableUsage").parent("security").defaultValue(ConfigPro.QUERY_VAR_USAGE_IGNORE).choices(
			new Choice<Integer>(ConfigPro.QUERY_VAR_USAGE_IGNORE, "ignore", Boolean.FALSE).description(
					"Permissive: Allows unscoped variables inside 'cfquery' without 'cfqueryparam'. This is the legacy default but poses a high risk of SQL injection."),

			new Choice<Integer>(ConfigPro.QUERY_VAR_USAGE_WARN, "warn", "warning").description(
					"Audit Mode: Allows the query to execute but logs a security warning. Useful for identifying vulnerable code in existing applications without breaking them."),

			new Choice<Integer>(ConfigPro.QUERY_VAR_USAGE_ERROR, "error", Boolean.TRUE)
					.description("Strict/Secure: Blocks any 'cfquery' that contains variables not wrapped in 'cfqueryparam'. Highly recommended for modern, secure environments."))
			.description(
					"Controls how Lucee handles raw variables used inside 'cfquery' tags. Enabling 'error' mode effectively prevents SQL injection by mandating parameterized queries.");
	private final ConfigValue<Integer> securityVariableUsage = new ConfigValue<>(metaVarUsage);

	public final static Prop<TimeSpan> metaCachedAfterTimeRange = Prop.timespan().keys("cachedAfter")
			.description("In case the attribute \"cacheAfter\" is set without the attribute \"cachedwithin\" in the tag \"query\" this time span is used for the element cached.");
	private TimeSpan cachedAfter;
	private boolean initCachedAfter = true;

	public final static Prop<Regex> metaRegex = Prop.custom(RegexFactory.getInstance()).keys("regexType").systemPropEnvVar("lucee.regex.type")
			.defaultValue(RegexFactory.toRegex(RegexFactory.TYPE_PERL, null))
			.description("Which regular expression dialect should be used. Modern (Java dialect) or Classic (Perl5 dialect).");
	private final ConfigValue<Regex> regexType = new ConfigValue<>(metaRegex);

	private static Prop<TimeSpan> metaApplicationPathCacheTimeout = Prop.timespan().keys("applicationPathTimeout").systemPropEnvVar("lucee.application.path.cache.timeout")
			.defaultValue(TimeSpanImpl.fromMillis(20000L)) // 20 seconds
			.description("Specifies how long Lucee caches the resolved location of Application.cfc or Application.cfm files. "
					+ "Caching this path prevents repetitive, expensive file-system lookups on every request. "
					+ "A shorter timeout detects new application files faster, while a longer timeout improves performance in high-traffic environments.");
	private Long applicationPathTimeout;

	private static Prop<Boolean> metaPreciseMath = Prop.bool().keys("preciseMath").systemPropEnvVar("lucee.precise.math").defaultValue(false)
			.description("If enabled, this improves the accuracy of floating point calculations but makes them slightly slower.");
	private final ConfigValue<Boolean> preciseMath = new ConfigValue<>(metaPreciseMath);

	public final static Prop<String> metaMainLoggerName = Prop.str().keys("mainLogger").systemPropEnvVar("lucee.logging.main").defaultValue("application")
			.description("defines the main logger used by Lucee, this logger is used when no log name was provided or the provided name does not exist.");
	private String mainLogger;

	@SuppressWarnings("unchecked")
	private static Prop<Short> metaCompileType = Prop.shor().keys("compileType").defaultValue(RECOMPILE_NEVER).choices(new Choice<Short>(RECOMPILE_NEVER, "never").description(
			"Performance Optimized: Retains all existing compiled templates across restarts. Fastest startup time, but requires manual clearing if source files were changed while the engine was offline."),

			new Choice<Short>(RECOMPILE_ALWAYS, "always").description(
					"Safe/Clean: Forces a full recompile of all templates immediately upon restart. Ensures no stale code exists, but significantly increases CPU load and startup time."),

			new Choice<Short>(RECOMPILE_AFTER_STARTUP, "after-startup").description(
					"Lazy Reload: Retains existing templates initially, but background tasks gradually re-verify and recompile templates as they are accessed after the system is online."))
			.description(
					"Determines how Lucee handles previously compiled templates (bytecode) after an engine restart. Balancing startup speed against the risk of executing stale code.");
	private final ConfigValue<Short> compileType = new ConfigValue<>(metaCompileType);

	@SuppressWarnings("unchecked")
	public final static Prop<Short> metaInspectTemplate = Prop.shor().keys("inspectTemplate").defaultValue(INSPECT_AUTO)
			.choices(
					new Choice<Short>(INSPECT_AUTO, "auto").description("Optimized Performance: Lucee performs background checks intermittently to detect template modifications "
							+ "without blocking requests. When a change is detected, Lucee temporarily increases check frequency, "
							+ "then gradually returns to the standard interval. Configure the standard and accelerated check intervals "
							+ "(in milliseconds) to balance update responsiveness with request throughput."),

					new Choice<Short>(INSPECT_ALWAYS, "always").description("Not Recommended: All files in the template cache are checked for updates on every request. "
							+ "Guarantees the latest template version is always used, but increases file system overhead and may impact performance."),

					new Choice<Short>(INSPECT_NEVER, "never").description("Best Performance: Cached templates are never re-inspected for changes at runtime. "
							+ "Ideal for production environments where templates are static during the server's uptime."),

					new Choice<Short>(INSPECT_ONCE, "once").description("Good: Each template is inspected for changes exactly once after it enters the cache. "
							+ "Balances file system overhead with the ability to pick up updates made earlier in the server's uptime."))

			.description("Controls how Lucee checks whether cached templates (CFM/CFC) have been modified. "
					+ "'auto' is recommended for most environments as it minimizes request overhead while still detecting changes. "
					+ "'never' gives maximum performance for static deployments. 'always' is available but not recommended for production.");

	private final ConfigValue<Short> inspectTemplate = new ConfigValue<>(metaInspectTemplate);

	public final static Prop<Integer> metaInspectTemplateAutoIntervalSlow = Prop.integer().keys("inspectTemplateIntervalSlow").access(SecurityManager.TYPE_SETTING)
			.defaultValue(ConfigPro.INSPECT_INTERVAL_SLOW)
			.description("The standard background check interval (in milliseconds) used in 'auto' mode to detect template modifications. "
					+ "When no recent changes have been detected, Lucee polls at this frequency. "
					+ "After a period of heightened monitoring, Lucee gradually transitions back to this interval.");
	private int inspectTemplateAutoIntervalSlow = ConfigPro.INSPECT_INTERVAL_UNDEFINED;

	public final static Prop<Integer> metaInspectTemplateAutoIntervalFast = Prop.integer().keys("inspectTemplateIntervalFast").access(SecurityManager.TYPE_SETTING)
			.defaultValue(ConfigPro.INSPECT_INTERVAL_FAST)
			.description("The accelerated background check interval (in milliseconds) used in 'auto' mode after a template change is detected. "
					+ "Lucee temporarily switches to this shorter interval to remain responsive to any further modifications, "
					+ "before gradually transitioning back to the standard interval over time.");
	private int inspectTemplateAutoIntervalFast = ConfigPro.INSPECT_INTERVAL_UNDEFINED;

	// LDEV: background ticker that periodically re-inspects "auto" inspectTemplate mappings, replacing
	// the
	// old PageSourcePoolWatcher polling thread. Started lazily on first page load, sped up after a
	// change.
	private final ScheduledExecutorService inspectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "InspectAutoRefresh");
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		return t;
	});
	private volatile ScheduledFuture<?> nextTick;
	private final Object tickToken = new Object();
	private final AtomicBoolean fastRequested = new AtomicBoolean(false);

	public final static Prop<Boolean> metaFormUrlAsStruct = Prop.bool().keys("formUrlAsStruct").defaultValue(true)
			.description("When enabled, Lucee parses dot-notation in URL/Form keys into nested structures. " + "For example, 'index.cfm?person.name=John' becomes URL.person.name. "
					+ "If disabled, it remains a flat key: URL['person.name'].");
	private final ConfigValue<Boolean> formUrlAsStruct = new ConfigValue<>(metaFormUrlAsStruct);

	public final static Prop<Boolean> metaMergeFormAndURL = Prop.bool().keys("mergeUrlForm").defaultValue(false).description(
			"This setting defines if the scopes URL and Form will be merged together (CFML Default is false). If a key already exists in Form and URL Scopes, the value from the Form Scope is used.");
	private final ConfigValue<Boolean> mergeUrlForm = new ConfigValue<>(metaMergeFormAndURL);

	public final static Prop<Boolean> metaShowDebug = Prop.bool().parent("monitoring").keys("showDebug").systemPropEnvVar("lucee.monitoring.showDebug").defaultValue(false)
			.description("show debug output in the monitoring at the bottom of the page");
	private final ConfigValue<Boolean> showDebug = new ConfigValue<>(metaShowDebug);

	public final static Prop<Boolean> metaShowDoc = Prop.bool().parent("monitoring").keys("showDoc", "doc", "documentation", "showReference", "reference")
			.systemPropEnvVar("lucee.monitoring.showDoc").defaultValue(false).description("show documentation output in the monitoring at the bottom of the page");
	private final ConfigValue<Boolean> showDoc = new ConfigValue<>(metaShowDoc);

	public final static Prop<Boolean> metaShowMetric = Prop.bool().parent("monitoring").keys("showMetric", "showMetrics", "metric", "metrics")
			.systemPropEnvVar("lucee.monitoring.showMetric").description("show metrics output in the monitoring at the bottom of the page").defaultValue(false);
	private final ConfigValue<Boolean> showMetric = new ConfigValue<>(metaShowMetric);

	public final static Prop<Boolean> metaShowTest = Prop.bool().parent("monitoring").keys("showTest", "showTests", "test").systemPropEnvVar("lucee.monitoring.showTest")
			.defaultValue(false).description("Show test output in the monitoring at the bottom of the page (not supported yet)");
	private final ConfigValue<Boolean> showTest = new ConfigValue<>(metaShowTest);

	private static Prop<Boolean> metafullNullSupport = Prop.bool().keys("nullSupport", "fullNullSupport").systemPropEnvVar("lucee.full.null.support").defaultValue(false)
			.description("having full null support enabled or not");
	private final ConfigValue<Boolean> nullSupport = new ConfigValue<>(metafullNullSupport);

	private static Prop<SecretProvider> metaSecretProviders = Prop.custom(SecretProviderFactory.getInstance(), Prop.TYPE_MAP).keys("secretProvider", "secretProviders")
			.lowerCaseKeys();
	protected Map<String, SecretProvider> secretProvider;

	private static Prop<ClassDefinition> metacCacheDefinitions = Prop.custom(ClassDefinitionFactory.getInstance(), Prop.TYPE_LIST).keys("cacheClasses").deprecated().hidden();
	private Map<String, ClassDefinition> cacheClasses;

	private static Prop<GatewayEntry> metaGatewayEntries = Prop.custom(GatewayEntryFactory.getInstance(), Prop.TYPE_MAP).keys("gateways").access(SecurityManagerImpl.TYPE_GATEWAY)
			.lowerCaseKeys().description(
					"Defines Event Gateways for asynchronous communication (SMS, XMPP, File Watcher, etc.). Configures the driver class, listener CFC, and custom protocol parameters.");
	private GatewayMap gateways;

	public final static Prop<Boolean> metaCgiScopeReadonly = Prop.bool().keys("cgiScopeReadOnly").defaultValue(true).description("make the cgi scope read only or not");
	private final ConfigValue<Boolean> cgiScopeReadonly = new ConfigValue<>(metaCgiScopeReadonly);

	public final static Prop<Integer> metaDebugMaxRecordsLogged = Prop.integer().keys("debuggingMaxRecordsLogged", "debuggingShowMaxRecordsLogged").defaultValue(10)
			.access(SecurityManager.TYPE_DEBUGGING)
			.description("defines the size of the debbugging stack that holds the debugging results of the last requests, by default this is 10");
	private final ConfigValue<Integer> debuggingMaxRecordsLogged = new ConfigValue<>(metaDebugMaxRecordsLogged);

	private static Prop<Boolean> metaCheckForChangesInConfigFile = Prop.bool().keys("checkForChanges").systemPropEnvVar("lucee.check.for.changes").defaultValue(false)
			.description("Enables automatic 'Hot-Reload' for the Lucee configuration file. "
					+ "When set to 'true', the engine polls the 'lucee-config.json' file (approximately every 60 seconds) "
					+ "and automatically applies any detected changes to the running server without requiring a restart.");
	private final ConfigValue<Boolean> checkForChanges = new ConfigValue<>(metaCheckForChangesInConfigFile);

	private static Prop<String> metaRemoteClientDirectory = Prop.str().keys("directory").parent("remoteClients").systemPropEnvVar("lucee.task.directory")
			.description("Specifies the file-system directory where Lucee stores persistent background tasks (e.g., cfmail, cfthread tasks). "
					+ "By storing tasks as physical files, Lucee prevents memory exhaustion during high-volume mail bursts and ensures that "
					+ "failed or pending tasks are preserved across server restarts.");
	private Resource remoteClientsDirectory;

	private static Prop<Integer> metaRemoteClientMaxThreads = Prop.integer().keys("maxThreads").parent("remoteClients").defaultValue(20)
			.description("Sets the maximum number of concurrent worker threads for the Lucee Spooler. "
					+ "The spooler handles asynchronous tasks such as background mail delivery (cfmail) and thread tasks (cfthread type='task'). "
					+ "Increasing this value allows for faster parallel processing of background queues, but consumes more system resources.");
	private final ConfigValue<Integer> remoteClientsMaxThreads = new ConfigValue<>(metaRemoteClientMaxThreads);

	private static Prop<AIEngine> metaAiEngines = Prop.custom(AIEngineFactory.getInstance(), Prop.TYPE_MAP).keys("ai", "aiEngines").lowerCaseKeys()
			.description("A map of named AI Engine configurations. This allows you to define and manage multiple "
					+ "connections to AI providers such as OpenAI (ChatGPT), Google (Gemini), Anthropic (Claude), "
					+ "or local instances like Ollama. Each engine can be configured with specific models, "
					+ "system instructions, and security keys, enabling specialized AI behavior across your applications.");
	protected Map<String, AIEngine> ai;

	private static Prop<Startup> metaStartups = Prop.custom(StartupFactory.getInstance(), Prop.TYPE_LIST).keys("startupHooks")
			.description("A list of custom Java classes to be instantiated upon Lucee startup. "
					+ "Lucee follows a specific instantiation priority: it first looks for a constructor "
					+ "that accepts the 'lucee.runtime.config.Config' interface to pass the current " + "configuration; if not found, it falls back to a no-argument constructor. "
					+ "These hooks are ideal for initializing third-party libraries, listeners, or " + "application-specific global state.");
	private static Map<String, Startup> startupHooks;

	private static Prop<RemoteClient> metaRemoteClients = Prop.custom(RemoteClientFactory.getInstance(), Prop.TYPE_LIST).keys("remoteClient").parent("remoteClients")
			.access(SecurityManagerImpl.TYPE_REMOTE).deprecated();
	private RemoteClient[] remoteClientsRemoteClient;

	private static Prop<lucee.runtime.rest.Mapping> metaRestMappings = Prop.custom(lucee.runtime.rest.MappingFactory.getInstance(), Prop.TYPE_LIST).keys("mapping").parent("rest")
			.description("Defines a list of REST mappings for the engine. Each mapping connects a virtual URI (accessed via '/rest/virtual-path') to a physical directory. "
					+ "If no mapping is explicitly marked as 'default', Lucee automatically creates a fallback mapping " + "pointing to the '{lucee-config}/rest' directory. "
					+ "The engine strictly enforces a single default mapping; if multiple mappings are marked as default, "
					+ "only the first one encountered is honored, and others are downgraded to non-default.");
	private lucee.runtime.rest.Mapping[] restMapping;

	public final static Prop<Struct> metaRemoteClientsUsage = Prop.sct().keys("usage").parent("remoteClients").defaultValue(new StructImpl()).deprecated();
	private final ConfigValue<Struct> remoteClientsUsage = new ConfigValue<>(metaRemoteClientsUsage);

	private static Prop<String> metaCachedWithinFunction = Prop.str().keys("cachedWithinFunction").description("Enables and defines the default caching for function calls. "
			+ "If set, all functions that support caching will be cached for this duration unless overridden in the code.").deprecated();
	private final ConfigValue<String> cachedWithinFunction = new ConfigValue<>(metaCachedWithinFunction);

	private static Prop<String> metaCachedWithinInclude = Prop.str().keys("cachedWithinInclude")
			.description("Enables and defines default caching for 'cfinclude'. " + "Setting this automatically caches included template output for the specified timespan.")
			.deprecated();;
	private final ConfigValue<String> cachedWithinInclude = new ConfigValue<>(metaCachedWithinInclude);

	private static Prop<String> metaCachedWithinQuery = Prop.str().keys("cachedWithinQuery")
			.description("Enables and defines the default caching for database queries. " + "When set, all 'cfquery' operations are cached by default using this timespan.")
			.deprecated();;
	private final ConfigValue<String> cachedWithinQuery = new ConfigValue<>(metaCachedWithinQuery);

	private static Prop<String> metaCachedWithinResource = Prop.str().keys("cachedWithinResource")
			.description("Enables and defines default caching for Lucee resources and virtual file system lookups.").deprecated();;
	private final ConfigValue<String> cachedWithinResource = new ConfigValue<>(metaCachedWithinResource);

	private static Prop<String> metaCachedWithinHTTP = Prop.str().keys("cachedWithinHTTP")
			.description(
					"Enables and defines default caching for 'cfhttp' requests. " + "Setting this ensures all outgoing HTTP calls are cached for the defined period by default.")
			.deprecated();;
	private final ConfigValue<String> cachedWithinHTTP = new ConfigValue<>(metaCachedWithinHTTP);

	private static Prop<String> metaCachedWithinFile = Prop.str().keys("cachedWithinFile").description("Enables and defines default caching for file-system read operations.")
			.deprecated();;
	private final ConfigValue<String> cachedWithinFile = new ConfigValue<>(metaCachedWithinFile);

	private static Prop<String> metaCachedWithinWebservice = Prop.str().keys("cachedWithinWebservice")
			.description("Enables and defines default caching for SOAP webservice calls. " + "Automatically caches remote responses for the specified duration.").deprecated();;
	private final ConfigValue<String> cachedWithinWebservice = new ConfigValue<>(metaCachedWithinWebservice);

	private static Prop<Boolean> metaSuppressWhitespaceBeforeArgument = Prop.bool().keys("suppressWhitespaceBeforeArgument", "suppressWhitespaceBeforecfargument")
			.systemPropEnvVar("lucee.suppress.ws.before.arg").defaultValue(true)
			.description("When enabled, Lucee automatically removes all white space (spaces, tabs, and newlines) "
					+ "located between a <cffunction> start tag and its first <cfargument>, as well as "
					+ "between consecutive <cfargument> tags. This ensures that source code formatting "
					+ "inside a function definition does not produce unintended output in the response stream.");
	private final ConfigValue<Boolean> suppressWhitespaceBeforeArgument = new ConfigValue<>(metaSuppressWhitespaceBeforeArgument);
	public final static Prop<Boolean> metaBufferTagBodyOutput = Prop.bool().keys("bufferTagBodyOutput", "bufferOutput").defaultValue(DEFAULT_BUFFER_TAG_BODY_OUTPUT)
			.description("Determines how Lucee handles content generated within a tag's body when an exception occurs. "
					+ "If enabled (true), the body output is buffered and displayed even if the tag fails. "
					+ "If disabled (false), any content generated within the body prior to a failure is ignored and not sent to the response stream.");
	private final ConfigValue<Boolean> bufferTagBodyOutput = new ConfigValue<>(metaBufferTagBodyOutput);

	public final static Prop<Boolean> metaPreserveCase = Prop.bool().keys("preserveCase").systemPropEnvVar("lucee.preserve.case").defaultValue(false)
			.description("When enabled, struct keys defined via dot notation preserve their original casing (e.g. sct.myKey → \"myKey\"). "
					+ "When disabled (CFML default), dot-notation keys are converted to uppercase (e.g. sct.myKey → \"MYKEY\"). "
					+ "Keys defined via bracket notation always preserve their original case regardless of this setting.");
	private Boolean preserveCase;

	private static Prop<Boolean> metaDefaultFunctionOutput = Prop.bool().keys("defaultFunctionOutput").defaultValue(true)
			.description("Specifies the default value for the 'output' attribute of the <cffunction> tag. "
					+ "If set to true, functions will allow white space and content within their body to be "
					+ "rendered to the response stream by default. If false, output is suppressed unless " + "the function explicitly sets output='true'.");
	private final ConfigValue<Boolean> defaultFunctionOutput = new ConfigValue<>(metaDefaultFunctionOutput);

	private static Prop<ClassDefinition> metacWsHandlerCD = Prop.custom(ClassDefinitionFactory.getInstance()).keys("webservice").deprecated();
	private final ConfigValue<ClassDefinition> wsHandlerCD = new ConfigValue<>(metacWsHandlerCD);

	private static Prop<JDBCDriver> metaJdbcDrivers = Prop.custom(JDBCDriverFactory.getInstance(), Prop.TYPE_MAP).keys("jdbcDrivers")
			.description("A map of registered JDBC drivers, primarily managed by Lucee extensions. "
					+ "Each entry maps a driver class name to its metadata (label, bundle, and connection string template). "
					+ "DataSources reference these drivers by their 'id' or 'class', allowing Lucee to automatically "
					+ "update the underlying driver version for all associated data sources when a driver extension is updated.");
	private JDBCDriver[] jdbcDrivers;

	private static Prop<ClassDefinition> metaSearchEngineClassDef = Prop.custom(ClassDefinitionFactory.getInstance("engine")).keys("search").defaultValue(null)
			.description("Defines the search engine implementation used by Lucee for features like <cfsearch> and <cfindex>. "
					+ "By default, no search engine is defined. This property uses a standard ClassDefinition, "
					+ "allowing the engine to be loaded via OSGi bundles or Maven coordinates (as seen with the " + "modern Maven-based Lucene extension).");
	private ClassDefinition<SearchEngine> search;

	private static Prop<Integer> metaExternalizeStringGTE = Prop.integer().keys("externalizeStringGte").defaultValue(-1)
			.description("Determines the character length threshold at which strings are moved from generated Java class files into separate external files. "
					+ "Externalizing strings drastically reduces the memory footprint of loaded templates but can negatively impact execution time. "
					+ "A lower breakpoint (smaller strings) results in more externalization and slower execution, while -1 (default) disables this feature entirely.");
	private final ConfigValue<Integer> externalizeStringGTE = new ConfigValue<>(metaExternalizeStringGTE);

	private static Prop<DebugEntry> metaDebugTemplates = Prop.custom(DebugEntryFactory.getInstance(), Prop.TYPE_LIST).keys("debugTemplates").description(
			"A list of registered debugging templates available to the engine. " + "Each entry defines a specific debugging 'skin' (e.g., Classic, Modern, or Comment) "
					+ "along with its physical path and access restrictions. While multiple templates can be "
					+ "registered, only the templates matching the current request's IP range and security "
					+ "settings will be executed to profile and display request execution details.");
	private DebugEntry[] debugTemplates;

	// TODO more detailed defintion for Javasettings
	private static Prop<Struct> metaJavaSettings = Prop.sct().keys("javasettings")
			.description("Configures the dynamic Java class loading behavior for the Lucee engine. "
					+ "The 'loadPaths' array specifies local directories or JAR files to be added to the classpath. "
					+ "The 'maven' array allows for the declaration of remote artifacts using standard coordinates "
					+ "(groupId:artifactId:version), which Lucee will automatically resolve and load. "
					+ "These settings enable the use of external Java libraries within CFML without " + "requiring manual placement in the server's lib directory.");
	private JavaSettings javaSettings;
	private final Map<String, JavaSettings> javaSettingsInstances = new ConcurrentHashMap<>();

	private final Map<String, ORMEngine> ormengines = new ConcurrentHashMap<String, ORMEngine>();
	// TODO make a ORM specific type for this that loads the orm config and the class defintion
	private static Prop<Struct> metaOrm = Prop.sct().keys("orm").access(SecurityManagerImpl.TYPE_ORM).defaultValue(new StructImpl())
			.description("Configures the Object-Relational Mapping (ORM) subsystem. " + "This property serves a dual purpose: it contains the 'engineClass', 'engineBundleName', "
					+ "and 'engineBundleVersion' required to load the ORM implementation (e.g., Hibernate) via "
					+ "OSGi or Maven, and it stores the ORM configuration settings. These settings include "
					+ "database dialects, CFC locations, caching providers, and 'dbcreate' behaviors, " + "which can be defined globally or per-datasource.");
	private final ConfigValue<Struct> orm = new ConfigValue<>(metaOrm);
	private ClassDefinition<? extends ORMEngine> ormCD;
	private ORMConfiguration ormConfig;
	private boolean initOrmConfig = true;

	private static Prop<ExtensionDefintion> metaExtensions = Prop.custom(ExtensionDefintionFactory.getInstance(), Prop.TYPE_LIST).keys("extensions")
			.description("Defines the Lucee extensions (LEX) to be managed by the engine. "
					+ "Extensions can be specified by ID and version, or via a path to a .lex file using Lucee's virtual file system "
					+ "(supporting local paths, 'https', 's3', etc.).  "
					+ "On startup, Lucee synchronizes the environment: it installs missing extensions and removes any existing " + "extensions not present in this list. ");
	private List<ExtensionDefintion> extensions;
	private RHExtension[] extensionsX;
	private int extensionsLoadCount = 0;

	private static Prop<ResourceProviderDef> metaDefaultResourceProviderDef = Prop.custom(ResourceProviderDefFactory.getInstance(false)).keys("defaultResourceProvider")
			.description("Defines the primary Resource Provider for the engine, responsible for handling standard file system operations. "
					+ "If not explicitly configured, Lucee defaults to the standard local file system provider. "
					+ "Changing this allows for advanced setups where the 'default' file operations are redirected to a different storage backend.");

	private ResourceProviderDef defaultResourceProvider;
	private ResourceProvider defaultResourceProviderInstance;
	private static Prop<ResourceProviderDef> metaResourceProviderDef = Prop.custom(ResourceProviderDefFactory.getInstance(true), Prop.TYPE_LIST).keys("resourceProviders")
			.description("A list of secondary Resource Providers that register specific URI schemes (e.g., s3://, ftp://, ram://) into Lucee's Virtual File System. "
					+ "Lucee automatically ensures that core providers for 'http', 'https', 'ram', 's3', and 'zip' are available with default settings "
					+ "if they are not explicitly defined here. This property allows you to override those defaults or add entirely new custom storage providers.");
	private List<ResourceProviderDef> resourceProviders;
	private final ResourcesImpl resources = new ResourcesImpl();

	private static Prop<ClassDefinition> metaCacheHandlers = Prop.custom(ClassDefinitionFactory.getInstance(), Prop.TYPE_MAP).keys("cacheHandlers").lowerCaseKeys()
			.description("Registers custom Cache Handlers used to process the 'cachedWithin' attribute in tags like <cfquery>, <cfhttp>, and <cffunction>. "
					+ "Lucee provides built-in handlers for 'request' (storing data for the duration of the current request) and 'timespan' "
					+ "(standard duration-based caching). This property allows developers to extend this behavior with custom logic, "
					+ "mapping unique keywords to specific Java-based cache handling implementations.");
	private Map<String, ClassDefinition> cacheHandlers;
	private Map<String, Class<CacheHandler>> cacheHandlersInstances;

	private static Prop<Array> metaScheduledTasks = Prop.arr().keys("scheduledTasks").defaultValue(new ArrayImpl()).deprecated()
			.description("Defines the legacy 'classic' scheduled tasks for the engine. "
					+ "In modern versions of Lucee, these have been largely superseded by the Quartz-based scheduler. "
					+ "While this property remains for backward compatibility.");
	private final ConfigValue<Array> scheduledTasks = new ConfigValue<>(metaScheduledTasks);

	private static Prop<Boolean> metaMonitoringEnabled = Prop.bool().keys("enabled").parent("monitoring").defaultValue(false).description(
			"Enables the background monitoring service in Lucee. When active, the engine collects real-time performance data and health metrics at regular intervals.");
	private final ConfigValue<Boolean> monitoringEnabled = new ConfigValue<>(metaMonitoringEnabled);

	public final static Prop<Boolean> metaLoginCaptcha = Prop.bool().keys("loginCaptcha").defaultValue(false).description("is a captcha used for the Lucee admin to login.");
	private final ConfigValue<Boolean> loginCaptcha = new ConfigValue<>(metaLoginCaptcha);

	private static Prop<Boolean> metaClassicDateParsing = Prop.bool().keys("classicDateParsing").defaultValue(false).deprecated();
	private final ConfigValue<Boolean> classicDateParsing = new ConfigValue<>(metaClassicDateParsing);

	public final static Prop<Boolean> metaRememberMe = Prop.bool().keys("loginRememberme").defaultValue(true);
	private final ConfigValue<Boolean> rememberMe = new ConfigValue<>(metaRememberMe);

	private static Prop<String> metaUpdateLocation = Prop.str().keys("updateLocation", "updateSiteURL").defaultValue(Constants.DEFAULT_UPDATE_URL.toExternalForm()).deprecated();
	private String updateLocation;
	private URL updateLocationURL;

	private static Prop<String> metaUpdateType = Prop.str().keys("updateType").defaultValue("manual").deprecated();
	private final ConfigValue<String> updateType = new ConfigValue<>(metaUpdateType);

	public final static Prop<String> metaAuthKeys = Prop.str().keys("authKeys").defaultValue(null).deprecated();
	private String[] authKeys;

	public final static Prop<Integer> metaLoginDelay = Prop.integer().keys("loginDelay").defaultValue(1);
	private final ConfigValue<Integer> loginDelay = new ConfigValue<>(metaLoginDelay);

	private static Prop<String> metaMavenDirectory = Prop.str().keys("mavenDirectory").systemPropEnvVar("lucee.maven.local.repository")
			.description("Specifies the local directory where Lucee's internal Maven provider stores and caches " + "downloaded artifacts. "
					+ "By default, this is managed within the Lucee server directory, but centralizing it "
					+ "allows multiple Lucee instances to share the same cache, reducing redundant downloads "
					+ "and improving startup times in containerized or clustered environments.");
	private Resource mavenDirectory;

	private static Prop<LabelFactory.Label> metaLabelsLabel = Prop.custom(LabelFactory.getInstance(), Prop.TYPE_LIST).keys("label").parent("labels")
			.description("Defines a mapping of Web Context identifiers to human-readable names (labels). "
					+ "The 'id' is a hash of the physical webroot path, and the 'label' is the descriptive name. "
					+ "These labels are used in the Lucee Administrator and monitoring output. They also function "
					+ "as the dynamic placeholder '{web-context-label}' in file paths. " + "The unique ID for any context can be retrieved in CFML using: "
					+ "lucee.commons.io.SystemUtil::hash(getPageContext().getConfig().getServletContext())");
	private List<LabelFactory.Label> labelsLabel;
	private Map<String, String> labels;

	private static Prop<Monitor> metaMonitors = Prop.custom(MonitorFactory.getInstance(), Prop.TYPE_LIST).keys("monitor").parent("monitoring")
			.description("Registers a list of active monitors to observe engine behavior and performance. Lucee supports three distinct monitoring strategies: "
					+ "1. 'request': Hooks into every single request for high-fidelity profiling (can be run 'async' to reduce overhead). "
					+ "2. 'action': Responds to specific internal engine events or actions. "
					+ "3. 'interval': Runs background tasks at fixed periods to capture system snapshots. "
					+ "Monitors are loaded as standard ClassDefinitions, allowing for custom implementations " + "via local classes, OSGi bundles, or Maven artifacts.");
	private List<Monitor> monitors;
	private RequestMonitor[] requestMonitors;
	private IntervallMonitor[] intervallMonitors;
	private ActionMonitorCollector actionMonitorCollector;

	private ExecutionLogFactory executionLogFactory;

	protected ConfigServerImpl(CFMLEngineImpl engine, Map<String, CFMLFactory> initContextes, Map<String, CFMLFactory> contextes, Resource configDir, Resource configFile,
			UpdateInfo updateInfo, boolean essentialOnly, boolean newVersion) {

		this.configDir = configDir;
		this.configFile = configFile;
		this.newVersion = newVersion;

		this.engine = engine;
		if (!essentialOnly) engine.setConfigServerImpl(this);
		this.initContextes = initContextes;
		// this.contextes=contextes;
		this.rootDir = configDir;
		// instance=this;
		this.updateInfo = updateInfo;

	}

	Map<Key, String> getPlaceHolderData() {
		if (this.placeHolderdata == null) {
			if (insidePlaceHolder.get()) return new HashMap<>();
			synchronized (SystemUtil.createToken("configweb", "placeHolderdata")) {
				if (this.placeHolderdata == null) {
					if (insidePlaceHolder.get()) return new HashMap<>();
					insidePlaceHolder.set(true);
					try {
						Map<Key, String> data = new HashMap<>();
						data.put(KeyImpl.init("lucee-config"), getConfigDir().getAbsolutePath());
						data.put(KeyImpl.init("lucee-config-dir"), getConfigDir().getAbsolutePath());
						data.put(KeyImpl.init("lucee-config-directory"), getConfigDir().getAbsolutePath());
						data.put(KeyImpl.init("lucee-web"), getConfigDir().getAbsolutePath());
						data.put(KeyImpl.init("lucee-web-dir"), getConfigDir().getAbsolutePath());
						data.put(KeyImpl.init("lucee-web-directory"), getConfigDir().getAbsolutePath());

						data.put(KeyConstants._temp, getTempDirectory().getAbsolutePath());
						data.put(KeyImpl.init("temp-dir"), getTempDirectory().getAbsolutePath());
						data.put(KeyImpl.init("temp-directory"), getTempDirectory().getAbsolutePath());

						// add constants
						Struct constants = getConstants();
						if (constants != null) {
							Iterator<Entry<Key, Object>> it = constants.entryIterator();
							while (it.hasNext()) {
								Entry<Key, Object> e = it.next();
								data.put(e.getKey(), Caster.toString(e.getValue(), ""));
							}
						}
						this.placeHolderdata = Collections.unmodifiableMap(data);
					}
					finally {
						insidePlaceHolder.set(false);
					}
				}
			}
		}
		return this.placeHolderdata;
	}

	public Struct raw() {
		return root;
	}

	@Override
	public Struct getRawData() {
		return (Struct) root.duplicate(true);
	}

	@Override
	public String replacePlaceHolder(String str) {
		return ConfigUtil.replacePlaceHolder(this, str, getPlaceHolderData());
	}

	@Override
	public String replacePlaceHolder(String str, Map<Key, String> customPlaceHolderData) {
		return ConfigUtil.replacePlaceHolder(this, str, customPlaceHolderData != null ? ConfigUtil.merge(getPlaceHolderData(), customPlaceHolderData) : getPlaceHolderData());
	}

	public List<Monitor> getMonitors() {
		if (monitors == null) {
			synchronized (SystemUtil.createToken("config", "monitors")) {
				if (monitors == null) {
					monitors = metaMonitors.list(this, root);
				}
			}
		}
		return monitors;
	}

	public ConfigServerImpl resetMonitors() {
		if (monitors != null) {
			synchronized (SystemUtil.createToken("config", "monitors")) {
				if (monitors != null) {
					monitors = null;
					requestMonitors = null;
					intervallMonitors = null;
					actionMonitorCollector = null;
				}
			}
		}
		return this;
	}

	public boolean getOnlyFirstMatch() {
		return onlyFirstMatch.get(this, root);
	}

	public ConfigServerImpl resetOnlyFirstMatch() {
		onlyFirstMatch.reset();
		return this;
	}

	@Override
	public RequestMonitor[] getRequestMonitors() {
		if (requestMonitors == null) {
			synchronized (SystemUtil.createToken("config", "getRequestMonitors")) {
				if (requestMonitors == null) {
					java.util.List<RequestMonitor> list = new ArrayList<RequestMonitor>();
					for (Monitor m: getMonitors()) {
						if (Monitor.TYPE_REQUEST == m.getType() && m instanceof RequestMonitor) {
							list.add((RequestMonitor) m);
						}
					}
					requestMonitors = list.toArray(new RequestMonitor[list.size()]);
				}
			}
		}
		return requestMonitors;
	}

	@Override
	public IntervallMonitor[] getIntervallMonitors() {
		if (intervallMonitors == null) {
			synchronized (SystemUtil.createToken("config", "monitors")) {
				if (intervallMonitors == null) {
					java.util.List<IntervallMonitor> list = new ArrayList<IntervallMonitor>();
					for (Monitor m: getMonitors()) {
						if (Monitor.TYPE_INTERVAL == m.getType() && m instanceof IntervallMonitor) {
							list.add((IntervallMonitor) m);
						}
					}
					intervallMonitors = list.toArray(new IntervallMonitor[list.size()]);
				}
			}
		}
		return intervallMonitors;
	}

	public ActionMonitorCollector getActionMonitorCollector() {
		if (actionMonitorCollector == null) {
			synchronized (SystemUtil.createToken("config", "monitors")) {
				if (actionMonitorCollector == null) {
					java.util.List<MonitorTemp> list = new ArrayList<MonitorTemp>();
					for (Monitor m: getMonitors()) {
						if (Monitor.TYPE_ACTION == m.getType() && m instanceof MonitorTemp) {
							list.add((MonitorTemp) m);
						}
					}
					actionMonitorCollector = ActionMonitorFatory.getActionMonitorCollector(this, list.toArray(new MonitorTemp[list.size()]));
				}
			}
		}
		return actionMonitorCollector;
	}

	@Override
	public RequestMonitor getRequestMonitor(String name) throws ApplicationException {
		for (RequestMonitor rm: getRequestMonitors()) {
			if (rm.getName().equalsIgnoreCase(name)) return rm;
		}
		throw new ApplicationException("there is no request monitor registered with name [" + name + "]");
	}

	@Override
	public IntervallMonitor getIntervallMonitor(String name) throws ApplicationException {
		for (IntervallMonitor im: getIntervallMonitors()) {
			if (im.getName().equalsIgnoreCase(name)) return im;
		}
		throw new ApplicationException("there is no intervall monitor registered with name [" + name + "]");
	}

	@Override
	public ActionMonitor getActionMonitor(String name) {
		ActionMonitorCollector am = getActionMonitorCollector();
		return am == null ? null : am.getActionMonitor(name);
	}

	public Map<String, String> getLabels() {
		if (labels == null) {
			synchronized (SystemUtil.createToken("config", "getLabels")) {
				if (labels == null) {
					labelsLabel = metaLabelsLabel.list(this, root);
					Map<String, String> map = new HashMap<String, String>();
					for (Label label: labelsLabel) {
						map.put(label.id, label.name);
					}
					labels = map;
				}
			}
		}
		return labels;
	}

	public ConfigServerImpl resetLabels() {
		if (labels != null) {
			synchronized (SystemUtil.createToken("config", "getLabels")) {
				if (labels != null) {
					labels = null;
					labelsLabel = null;
				}
			}
		}
		return this;
	}

	@Override
	public Resource getMavenDir() {
		if (mavenDirectory == null) {
			synchronized (this) {
				if (mavenDirectory == null) {

					String repoDir = metaMavenDirectory.get(this, root);

					Resource tmp = null;
					if (!StringUtil.isEmpty(repoDir, true)) {
						tmp = getResource(repoDir);
						// at least the grand parent need to exist
						if (ResourceUtil.doesGrandParentExists(tmp)) {
							try {
								tmp.createDirectory(true);
							}
							catch (IOException e) {
								tmp = null;
								LogUtil.log(this, "maven", e);
							}
						}
						else {
							tmp = null;
							LogUtil.log(this, Log.LEVEL_ERROR, "maven",
									"Cannot use directory [" + repoDir + "] because the directory structure two levels above it does not exist");
						}
					}
					if (tmp == null) {
						tmp = ResourceUtil.getCanonicalResourceEL(getConfigDir().getRealResource("../mvn/"));
						tmp.mkdirs();

					}
					mavenDirectory = tmp;
				}
			}
		}
		return mavenDirectory;
	}

	public ConfigServerImpl resetMavenDir() {
		if (mavenDirectory != null) {
			synchronized (SystemUtil.createToken("config", "getLoginDelay")) {
				if (mavenDirectory != null) {
					mavenDirectory = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getLoginDelay() {
		return loginDelay.get(this, root);
	}

	public ConfigServerImpl resetLoginDelay() {
		loginDelay.reset();
		return this;
	}

	public String[] getAuthenticationKeys() throws PageException {
		if (authKeys == null) {
			synchronized (SystemUtil.createToken("config", "getAuthenticationKeys")) {
				if (authKeys == null) {
					String keyList = metaAuthKeys.get(this, root);

					if (!StringUtil.isEmpty(keyList)) {
						String[] keys = ListUtil.trimItems(ListUtil.toStringArray(ListUtil.toListRemoveEmpty(keyList, ',')));
						for (int i = 0; i < keys.length; i++) {
							try {
								keys[i] = URLDecoder.decode(keys[i], "UTF-8", true);
							}
							catch (Exception e) {
								throw Caster.toPageException(e);
							}
						}
						authKeys = keys;
					}
					else authKeys = new String[0];
				}
			}
		}
		return authKeys;
	}

	public ConfigServerImpl resetAuthenticationKeys() {
		if (authKeys != null) {
			synchronized (SystemUtil.createToken("config", "getAuthenticationKeys")) {
				if (authKeys != null) {
					authKeys = null;
				}
			}
		}
		return this;
	}

	@Override
	public String getUpdateType() {
		return updateType.get(this, root);
	}

	public ConfigServerImpl resetUpdateType() {
		updateType.reset();
		return this;
	}

	@Override
	public URL getUpdateLocation() {
		if (updateLocationURL == null) {
			synchronized (SystemUtil.createToken("config", "getUpdateLocation")) {
				if (updateLocationURL == null) {

					updateLocation = metaUpdateLocation.get(this, root);
					try {
						updateLocationURL = HTTPUtil.toURL(updateLocation, HTTPUtil.ENCODED_AUTO);
					}
					catch (MalformedURLException e) {
						throw new PageRuntimeException(e);
					}
				}
			}
		}
		return updateLocationURL;
	}

	public ConfigServerImpl resetUpdateLocation() {
		if (updateLocationURL != null) {
			synchronized (SystemUtil.createToken("config", "getUpdateLocation")) {
				if (updateLocationURL != null) {
					updateLocation = null;
					updateLocationURL = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getRememberMe() {
		return rememberMe.get(this, root);
	}

	public ConfigServerImpl resetRememberMe() {
		rememberMe.reset();
		return this;
	}

	@Override
	public boolean getDateCasterClassicStyle() {
		return classicDateParsing.get(this, root);
	}

	public ConfigServerImpl resetDateCasterClassicStyle() {
		classicDateParsing.reset();
		return this;
	}

	@Override
	public boolean getLoginCaptcha() {
		return loginCaptcha.get(this, root);
	}

	public ConfigServerImpl resetLoginCaptcha() {
		loginCaptcha.reset();
		return this;
	}

	@Override
	public final boolean isMonitoringEnabled() {
		return monitoringEnabled.get(this, root);
	}

	public ConfigServerImpl resetMonitoringEnabled() {
		monitoringEnabled.reset();
		return this;
	}

	/**
	 * @return the allowURLRequestTimeout
	 */
	@Override
	public boolean isAllowURLRequestTimeout() {
		if (requestTimeoutInURL == null) {
			synchronized (SystemUtil.createToken("config", "isAllowURLRequestTimeout")) {
				if (requestTimeoutInURL == null) {
					String allowURLReqTimeout = ConfigFactoryImpl.getAttr(this, root, new String[] { "requestTimeoutInURL", "allowUrlRequesttimeout" });
					if (!StringUtil.isEmpty(allowURLReqTimeout)) {
						requestTimeoutInURL = Caster.toBooleanValue(allowURLReqTimeout, metaAllowURLRequestTimeout.defaultValue);
					}
					else requestTimeoutInURL = metaAllowURLRequestTimeout.defaultValue;
				}
			}
		}
		return requestTimeoutInURL;
	}

	public ConfigServerImpl resetAllowURLRequestTimeout() {
		if (requestTimeoutInURL != null) {
			synchronized (SystemUtil.createToken("config", "isAllowURLRequestTimeout")) {
				if (requestTimeoutInURL != null) {
					requestTimeoutInURL = null;
				}
			}
		}
		return this;
	}

	@Override
	public short getCompileType() {
		return compileType.get(this, root);
	}

	public ConfigServerImpl resetCompileType() {
		compileType.reset();
		return this;
	}

	@Override
	@Deprecated
	public void reloadTimeServerOffset() {
		// FUTURE remove method
	}

	@Override
	public long configLastModified() {
		return configFileLastModified;
	}

	private void setConfigLastModified() {
		this.configFileLastModified = configFile.lastModified();
	}

	@Override
	public short getScopeCascadingType() {
		return scopeCascading.get(this, root);
	}

	public ConfigServerImpl resetScopeCascadingType() {
		scopeCascading.reset();
		return this;
	}

	/**
	 * return all Tag Library Deskriptors
	 * 
	 * @return Array of Tag Library Deskriptors
	 */
	@Override
	public TagLib[] getTLDs() {
		// TODO
		if (cfmlTlds == null) {
			synchronized (SystemUtil.createToken("config", "getTLDs")) {
				if (cfmlTlds == null) {
					ConfigFactoryImpl.loadTag(this, root, newVersion());
				}
			}
		}
		return cfmlTlds;
	}

	public ConfigServerImpl resetTLDs() {
		if (cfmlTlds != null) {
			synchronized (SystemUtil.createToken("config", "getTLDs")) {
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
		return cascadeToResultset.get(this, root);
	}

	public ConfigServerImpl resetAllowImplicidQueryCall() {
		cascadeToResultset.reset();
		return this;
	}

	@Override
	public boolean limitEvaluation() {
		return securityLimitEvaluation.get(this, root);
	}

	public ConfigServerImpl resetLimitEvaluation() {
		securityLimitEvaluation.reset();
		return this;
	}

	@Override
	public boolean mergeFormAndURL() {
		return mergeUrlForm.get(this, root);
	}

	public ConfigServerImpl resetMergeFormAndURL() {
		mergeUrlForm.reset();
		return this;
	}

	@Override
	public TimeSpan getApplicationTimeout() {
		return applicationTimeout.get(this, root);
	}

	public ConfigServerImpl resetApplicationTimeout() {
		applicationTimeout.reset();
		return this;
	}

	@Override
	public TimeSpan getSessionTimeout() {
		return sessionTimeout.get(this, root);
	}

	public ConfigServerImpl resetSessionTimeout() {
		sessionTimeout.reset();
		return this;
	}

	@Override
	public TimeSpan getClientTimeout() {
		return clientTimeout.get(this, root);
	}

	public ConfigServerImpl resetClientTimeout() {
		clientTimeout.reset();
		return this;
	}

	@Override
	public TimeSpan getRequestTimeout() {
		if (requestTimeout == null) {
			synchronized (SystemUtil.createToken("config", "getRequestTimeout")) {
				if (requestTimeout == null) {
					// we first check if we have a struct requesttimeout
					Object rt = root.get(KeyConstants._requestTimeout, null);

					TimeSpan tmp = rt instanceof Struct ? null : metaRequestTimeoutOld.get(this, root);
					if (tmp == null || tmp.getMillis() == 0) {
						tmp = metaRequestTimeoutNew.get(this, root);
					}
					requestTimeout = tmp;
				}
			}
		}
		return requestTimeout;
	}

	public ConfigServerImpl resetRequestTimeout() {
		if (requestTimeout != null) {
			synchronized (SystemUtil.createToken("config", "getRequestTimeout")) {
				if (requestTimeout != null) {
					requestTimeout = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getRequestTimeoutConcurrentRequestThreshold() {
		return requestTimeoutConcurrentRequestThreshold.get(this, root);
	}

	public ConfigServerImpl resetRequestTimeoutConcurrentRequestThreshold() {
		requestTimeoutConcurrentRequestThreshold.reset();
		return this;
	}

	@Override
	public float getRequestTimeoutCPUThreshold() {
		return requestTimeoutCPUThreshold.get(this, root);
	}

	public ConfigServerImpl resetRequestTimeoutCPUThreshold() {
		requestTimeoutCPUThreshold.reset();
		return this;
	}

	@Override
	public float getRequestTimeoutMemoryThreshold() {
		return requestTimeoutMemoryThreshold.get(this, root);
	}

	public ConfigServerImpl resetRequestTimeoutMemoryThreshold() {
		requestTimeoutMemoryThreshold.reset();
		return this;
	}

	@Override
	public boolean isClientCookies() {
		return clientCookies.get(this, root);
	}

	public ConfigServerImpl resetClientCookies() {
		clientCookies.reset();
		return this;
	}

	@Override
	public boolean isDevelopMode() {
		return developMode.get(this, root);
	}

	public ConfigServerImpl resetDevelopMode() {
		developMode.reset();
		return this;
	}

	@Override
	public boolean isClientManagement() {
		return clientManagement.get(this, root);
	}

	public ConfigServerImpl resetClientManagement() {
		clientManagement.reset();
		return this;
	}

	@Override
	public boolean isDomainCookies() {
		return domainCookies.get(this, root);
	}

	public ConfigServerImpl resetDomainCookies() {
		domainCookies.reset();
		return this;
	}

	@Override
	public boolean isSessionManagement() {
		return sessionManagement.get(this, root);
	}

	public ConfigServerImpl resetSessionManagement() {
		sessionManagement.reset();
		return this;
	}

	@Override
	public boolean isMailSpoolEnable() {
		return mailSpoolEnable.get(this, root);
	}

	public ConfigServerImpl resetMailSpoolEnable() {
		mailSpoolEnable.reset();
		return this;
	}

	@Override
	public boolean isMailSendPartial() {
		return mailSendPartial.get(this, root);
	}

	public ConfigServerImpl resetMailSendPartial() {
		mailSendPartial.reset();
		return this;
	}

	@Override
	public boolean isUserset() {
		return mailUserSet.get(this, root);
	}

	public ConfigServerImpl resetUserset() {
		mailUserSet.reset();
		return this;
	}

	@Override
	public Server[] getMailServers() {
		// TODO
		if (mailServers == null) {
			synchronized (SystemUtil.createToken("config", "mail")) {
				if (mailServers == null) {
					List<Server> list = metaMailServers.list(this, root);
					mailServers = list.toArray(new Server[list.size()]);
				}
			}
		}
		return mailServers;
	}

	public ConfigServerImpl resetMailServers() {
		if (mailServers != null) {
			synchronized (SystemUtil.createToken("config", "mail")) {
				if (mailServers != null) {
					mailServers = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getMailTimeout() {
		return mailConnectionTimeout.get(this, root);
	}

	public ConfigServerImpl resetMailTimeout() {
		mailConnectionTimeout.reset();
		return this;
	}

	@Override
	public int getQueryVarUsage() {
		return securityVariableUsage.get(this, root);
	}

	public ConfigServerImpl resetQueryVarUsage() {
		securityVariableUsage.reset();
		return this;
	}

	@Override
	public boolean getPSQL() {
		return preserveSingleQuote.get(this, root);
	}

	public ConfigServerImpl resetPSQL() {
		preserveSingleQuote.reset();
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
		return locale.get(this, root);
	}

	public ConfigServerImpl resetLocale() {
		locale.reset();
		return this;
	}

	@Override
	public boolean debug() {
		return true;
	}

	@Override
	public boolean getShowDebug() {
		return showDebug.get(this, root);
	}

	public ConfigServerImpl resetShowDebug() {
		showDebug.reset();
		return this;
	}

	@Override
	public boolean getShowDoc() {
		return showDoc.get(this, root);
	}

	public ConfigServerImpl resetShowDoc() {
		showDoc.reset();
		return this;
	}

	@Override
	public boolean getShowMetric() {
		return showMetric.get(this, root);
	}

	public ConfigServerImpl resetShowMetric() {
		showMetric.reset();
		return this;
	}

	@Override
	public boolean getShowTest() {
		return showTest.get(this, root);
	}

	public ConfigServerImpl resetShowTest() {
		showTest.reset();
		return this;
	}

	@Override
	public boolean debugLogOutput() {
		return debuggingLogOutput.get(this, root);
	}

	public ConfigServerImpl resetLogOutput() {
		debuggingLogOutput.reset();
		return this;
	}

	// = SERVER_BOOLEAN_FALSE

	@Override
	public int getMailSpoolInterval() {
		return mailSpoolInterval.get(this, root);
	}

	public ConfigServerImpl resetMailSpoolInterval() {
		mailSpoolInterval.reset();
		return this;
	}

	@Override
	public TimeZone getTimeZone() {
		return timeZone.get(this, root);
	}

	public ConfigServerImpl resetTimeZone() {
		timeZone.reset();
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
		// TODO
		// MUST reset scheduler
		if (scheduler == null) {
			synchronized (SystemUtil.createToken("config", "getScheduler")) {
				if (scheduler == null) {
					try {
						scheduler = new SchedulerImpl(ConfigUtil.getEngine(this), this, getScheduledTasks());
					}
					catch (PageException e) {
						try {
							scheduler = new SchedulerImpl(ConfigUtil.getEngine(this), this, new ArrayImpl());
						}
						catch (PageException e1) {
							throw Caster.toPageRuntimeException(e1);
						}
					}
				}
			}
		}
		return scheduler;
	}

	public ConfigServerImpl resetScheduler() {
		if (scheduler != null) {
			synchronized (SystemUtil.createToken("config", "getScheduler")) {
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

	@Override
	public void setPassword(Password password) {
		this.hspw = password;
	}

	/**
	 * @return gets the password as hash
	 */
	protected Password getPassword() {
		// TODO
		if (initPassword) {
			synchronized (SystemUtil.createToken("config", "getPassword")) {
				if (initPassword) {
					hspw = metaPassword.get(this, root);
					initPassword = false;
				}
			}
		}
		return hspw;
	}

	protected ConfigServerImpl resetPassword() {
		if (!initPassword) {
			synchronized (SystemUtil.createToken("config", "getPassword")) {
				if (!initPassword) {
					initPassword = true;
					hspw = null;
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
		// TODO
		if (mappings == null) {
			synchronized (SystemUtil.createToken("config", "getMappings")) {
				if (mappings == null) {
					close(this.uncheckedMappings);

					// check for specific mappings to exist
					Map<String, Mapping> tmpMappings = metaMappings.map(this, root, new ConcurrentHashMap<>(), true, KeyConstants._virtual);
					boolean finished = true;
					boolean hasServerContext = false; // TODO still needed?
					boolean hasWebContext = false;
					String virtual;
					for (Entry<String, Mapping> entry: tmpMappings.entrySet()) {
						virtual = entry.getKey();
						if ("/lucee-server/".equalsIgnoreCase(virtual) || "/lucee-server-context/".equalsIgnoreCase(virtual)) {
							hasServerContext = true;
						}
						else if ("/lucee/".equalsIgnoreCase(virtual)) {
							hasWebContext = true;
						}
						else if ("/".equals(virtual)) {
							finished = true;
						}
					}

					// set default lucee-server context if needed TODO still neded?
					if (!hasServerContext) {
						ApplicationListener listener = ConfigUtil.loadListener(ApplicationListener.TYPE_MODERN, null);
						listener.setMode(ApplicationListener.MODE_CURRENT2ROOT);

						MappingImpl tmp = new MappingImpl(this, "/lucee-server", "{lucee-server}/context/", null, ConfigPro.INSPECT_AUTO, ConfigPro.INSPECT_INTERVAL_UNDEFINED,
								ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, false, true, true, false, false, listener, ApplicationListener.MODE_CURRENT2ROOT,
								ApplicationListener.TYPE_MODERN);
						tmpMappings.put(tmp.getVirtualLowerCase(), tmp);
					}
					// set default lucee context if needed
					if (!hasWebContext) {
						ApplicationListener listener = ConfigUtil.loadListener(ApplicationListener.TYPE_MODERN, null);
						listener.setMode(ApplicationListener.MODE_CURRENT2ROOT);

						MappingImpl tmp = new MappingImpl(this, "/lucee", "{lucee-config}/context/", "{lucee-config}/context/lucee-context.lar", ConfigPro.INSPECT_AUTO,
								ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, false, true, true, false, false, listener,
								ApplicationListener.MODE_CURRENT2ROOT, ApplicationListener.TYPE_MODERN);
						tmpMappings.put(tmp.getVirtualLowerCase(), tmp);
					}
					// seet root mapping (always needed)
					if (!finished) {
						MappingImpl tmp = new MappingImpl(this, "/", "/", null, ConfigPro.INSPECT_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED,
								ConfigPro.INSPECT_INTERVAL_UNDEFINED, true, true, true, true, false, false, null, -1, -1);
						tmpMappings.put("/", tmp);
					}

					this.mappings = initMappings(this.uncheckedMappings = tmpMappings.values().toArray(new Mapping[tmpMappings.size()]));
				}
			}
		}
		return mappings;
	}

	@Override
	public ConfigServerImpl resetMappings() {
		if (mappings != null) {
			synchronized (SystemUtil.createToken("config", "getMappings")) {
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
			synchronized (SystemUtil.createToken("config", "getCustomTagMappings")) {
				if (customTagMappings == null) {
					close(this.uncheckedCustomTagMappings);

					List<Mapping> list = metaCustomTagMappings.list(this, root);

					boolean hasDefault = false;
					for (Mapping m: list) {
						if ("{lucee-config}/customtags/".equals(m.getStrPhysical())) {
							hasDefault = true;
							break;
						}
					}
					if (!hasDefault) {
						list.add(new MappingImpl(this, "/default", "{lucee-config}/customtags/", null, ConfigPro.INSPECT_NEVER, -1, -1, true, false, true, true, false, true, null,
								-1, -1));
					}
					this.customTagMappings = initMappings(this.uncheckedCustomTagMappings = list.toArray(new Mapping[list.size()]));
				}
			}
		}
		return customTagMappings;
	}

	public ConfigServerImpl resetCustomTagMappings() {
		if (customTagMappings != null) {
			synchronized (SystemUtil.createToken("config", "getCustomTagMappings")) {
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
			synchronized (SystemUtil.createToken("config", "getComponentMappings")) {
				if (componentMappings == null) {
					close(this.uncheckedComponentMappings);

					List<Mapping> list = metaComponentMappings.list(this, root);

					boolean hasDefault = false;
					for (Mapping m: list) {
						if ("{lucee-config}/components/".equals(m.getStrPhysical())) {
							hasDefault = true;
							break;
						}
					}

					if (!hasDefault) {
						list.add(new MappingImpl(this, "/default", "{lucee-config}/components/", null, ConfigPro.INSPECT_NEVER, -1, -1, true, false, true, true, false, true, null,
								-1, -1));
					}
					this.componentMappings = initMappings(this.uncheckedComponentMappings = list.toArray(new Mapping[list.size()]));
				}
			}
		}
		return componentMappings;
	}

	public ConfigServerImpl resetComponentMappings() {
		if (componentMappings != null) {
			synchronized (SystemUtil.createToken("config", "getComponentMappings")) {
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
		return scheduledTasks.get(this, root);
	}

	public ConfigServerImpl resetScheduledTasks() {
		scheduledTasks.reset();
		return this;
	}

	public void checkMappings() {
		mappings = initMappings(uncheckedMappings);
		customTagMappings = initMappings(uncheckedCustomTagMappings);
		componentMappings = initMappings(uncheckedComponentMappings);
	}

	private Mapping[] initMappings(Mapping[] mappings) {
		if (mappings == null) return null;
		Map<String, Mapping> map = new HashMap<>();

		for (Mapping m: mappings) {

			try {
				m.check();
				if (!map.containsKey(m.getVirtualLowerCaseWithSlash())) {
					map.put(m.getVirtualLowerCaseWithSlash(), m);
				}
			}
			catch (Exception e) {
				LogUtil.log(this, "mappings", e);
			}
		}
		return map.values().toArray(new Mapping[map.size()]);
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
		if (restMapping == null) {
			synchronized (SystemUtil.createToken("config", "getRestMappings")) {
				if (restMapping == null) {

					List<lucee.runtime.rest.Mapping> list = metaRestMappings.list(this, root);
					Map<String, lucee.runtime.rest.Mapping> map = new HashMap<>();

					// has default and put in a map
					boolean hasDefault = false;
					for (lucee.runtime.rest.Mapping m: list) {
						map.put(m.getVirtual(), m);
						if (m.isDefault()) {
							hasDefault = true;
						}
					}

					// set default if not exist
					if (!hasDefault) {
						Resource rest = this.getConfigDir().getRealResource("rest");
						rest.mkdirs();
						lucee.runtime.rest.Mapping tmp = new lucee.runtime.rest.Mapping(this, "/default-set-by-lucee", rest.getAbsolutePath(), true, true, true);
						map.put(tmp.getVirtual(), tmp);
						restMapping = map.values().toArray(new lucee.runtime.rest.Mapping[map.size()]);
					}
					else {
						// make sure only one is default
						hasDefault = false;
						for (lucee.runtime.rest.Mapping m: map.values()) {
							if (m.isDefault()) {
								if (hasDefault) m.setDefault(false);
								hasDefault = true;
							}
						}
						restMapping = map.values().toArray(new lucee.runtime.rest.Mapping[map.size()]);
					}
				}
			}
		}
		return restMapping;
	}

	@Override
	public ConfigServerImpl resetRestMappings() {
		if (restMapping != null) {
			synchronized (SystemUtil.createToken("config", "getRestMappings")) {
				if (restMapping != null) {
					restMapping = null;
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
		return getPageSources(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, false, getOnlyFirstMatch());
	}

	@Override
	public PageSource[] getPageSources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean useComponentMappings) {
		return getPageSources(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, useComponentMappings, getOnlyFirstMatch());
	}

	public PageSource[] getPageSources(PageContext pc, Mapping[] appMappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean useComponentMappings, boolean onlyFirstMatch) {
		return ConfigUtil.getPageSources(pc, this, appMappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, useComponentMappings, onlyFirstMatch);
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
		tlta.setDefaultValue("false");
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
		arg.setDefaultValue("false");
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
			synchronized (SystemUtil.createToken("config", "getFLDs")) {
				if (cfmlFlds == null) {
					// TODO make some kind of pre state in case root is empty
					ConfigFactoryImpl.loadFunctions(this, root, newVersion());
				}
			}
		}
		return cfmlFlds;
	}

	public ConfigServerImpl resetFLDs() {
		if (cfmlFlds != null) {
			synchronized (SystemUtil.createToken("config", "getFLDs")) {
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

	@Override
	public Resource getTempDirectory() {
		if (tempDirectory == null) {
			synchronized (SystemUtil.createToken("config", "getTempDirectory")) {
				if (tempDirectory == null) {
					try {

						String strTempDirectory = ConfigUtil.translateOldPath(metaTempDirectory.get(this, root));

						Resource configDir = getConfigDir();

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
					catch (Exception ex) {
						ConfigFactoryImpl.log(this, ex);
						throw Caster.toPageRuntimeException(ex);
					}
				}
			}
		}
		return tempDirectory;
	}

	public ConfigServerImpl resetTempDirectory() {
		if (tempDirectory != null) {
			synchronized (SystemUtil.createToken("config", "getTempDirectory")) {
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

	@Override
	public Collection<String> getAIEngineNames() {
		return getAIEngines().keySet();
	}

	@Override
	public AIEngine getAIEngine(String name) {
		return getAIEngines().get(name);
	}

	private Map<String, AIEngine> getAIEngines() {
		if (ai == null) {
			synchronized (SystemUtil.createToken("config", "getAIEngineFactories")) {
				if (ai == null) {
					ai = metaAiEngines.map(this, root);
				}
			}
		}
		return ai;
	}

	public ConfigServerImpl resetAIEngineFactories() {
		if (ai != null) {
			synchronized (SystemUtil.createToken("config", "getAIEngineFactories")) {
				if (ai != null) {
					ai = null;
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
		if (secretProvider == null) {
			synchronized (SystemUtil.createToken("config", "getSecretProviders")) {
				if (secretProvider == null) {
					secretProvider = metaSecretProviders.map(this, root);
					// secretProviders = ConfigFactoryImpl.loadSecretProviders(this, root, null);
				}
			}
		}
		return secretProvider;
	}

	public ConfigServerImpl resetSecretProviders() {
		if (secretProvider != null) {
			synchronized (SystemUtil.createToken("config", "getSecretProviders")) {
				if (secretProvider != null) {
					secretProvider = null;
				}
			}
		}
		return this;
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

	/**
	 * @return Returns the configLogger. / public Log getConfigLogger() { return configLogger; }
	 */

	@Override
	public CFXTagPool getCFXTagPool() throws SecurityException {
		if (cfx == null) {
			synchronized (SystemUtil.createToken("config", "getCFXTagPool")) {
				if (cfx == null) {
					cfx = new CFXTagPoolImpl(metaCfxTagPool.map(this, root));
				}
			}
		}
		return cfx;
	}

	public ConfigServerImpl resetCFXTagPool() {
		if (cfx != null) {
			synchronized (SystemUtil.createToken("config", "getCFXTagPool")) {
				if (cfx != null) {
					cfx = null;
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
								if (a != null) {
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
		return restList.get(this, root);
	}

	public ConfigServerImpl resetRestList() {
		restList.reset();
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
			synchronized (SystemUtil.createToken("config", "getClientType")) {
				if (clientType == null) {
					clientType = metaClientType.get(this, root);
				}
			}
		}
		return this.clientType;
	}

	public ConfigServerImpl resetClientType() {
		if (clientType != null) {
			synchronized (SystemUtil.createToken("config", "getClientType")) {
				if (clientType != null) {
					clientType = null;
				}
			}
		}
		return this;
	}

	@Override
	public ClassDefinition<SearchEngine> getSearchEngineClassDefinition() {
		if (search == null) {
			synchronized (SystemUtil.createToken("config", "getSearchEngineClassDefinition")) {
				if (search == null) {

					ClassDefinition cd = metaSearchEngineClassDef.get(this, root);
					if (cd == null || !cd.hasClass() || "lucee.runtime.search.lucene.LuceneSearchEngine".equals(cd.getClassName())) {
						cd = DEFAULT_SEARCH_ENGINE;
					}
					search = cd;
				}
			}
		}
		return this.search;
	}

	public ConfigServerImpl resetSearchEngineClassDefinition() {
		if (search != null) {
			synchronized (SystemUtil.createToken("config", "getSearchEngineClassDefinition")) {
				if (search != null) {
					search = null;
				}
			}
		}
		return this;
	}

	@Override
	public String getSearchEngineDirectory() {
		if (searchDirectory == null) {
			synchronized (SystemUtil.createToken("config", "getSearchEngineDirectory")) {
				if (searchDirectory == null) {
					searchDirectory = metaSearchEngineDirectory.get(this, root);
				}
			}
		}
		return this.searchDirectory;
	}

	public ConfigServerImpl resetSearchEngineDirectory() {
		if (searchDirectory != null) {
			synchronized (SystemUtil.createToken("config", "getSearchEngineDirectory")) {
				if (searchDirectory != null) {
					searchDirectory = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getComponentDataMemberDefaultAccess() {
		return componentDataMemberAccess.get(this, root);
	}

	public ConfigServerImpl resetComponentDataMemberDefaultAccess() {
		componentDataMemberAccess.reset();
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
		return componentDumpTemplate.get(this, root);
	}

	public ConfigServerImpl resetComponentDumpTemplate() {
		componentDumpTemplate.reset();
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
		if (statusCode == 404) return errorMissingTemplate.get(this, root);
		return errorGeneralTemplate.get(this, root);
	}

	public ConfigServerImpl resetErrorTemplates() {
		errorMissingTemplate.reset();
		errorGeneralTemplate.reset();
		return this;
	}

	@Override
	public short getSessionType() {
		return sessionType.get(this, root);
	}

	public ConfigServerImpl resetSessionType() {
		sessionType.reset();
		return this;
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

	@Override
	public Resource getClassDirectory() {
		if (fileSystemDeployDirectory == null) {
			synchronized (SystemUtil.createToken("config", "getClassDirectory")) {
				if (fileSystemDeployDirectory == null) {

					String strDeployDirectory = metaDeployDirectory.get(this, root);
					if (!StringUtil.isEmpty(strDeployDirectory, true)) {
						strDeployDirectory = ConfigUtil.translateOldPath(strDeployDirectory);
					}
					fileSystemDeployDirectory = ConfigUtil.getFile(configDir, strDeployDirectory, "cfclasses", configDir, FileUtil.TYPE_DIR, ResourceUtil.LEVEL_GRAND_PARENT_FILE,
							this);
				}
			}
		}
		return fileSystemDeployDirectory;
	}

	public ConfigServerImpl resetClassDirectory() {
		if (fileSystemDeployDirectory != null) {
			synchronized (SystemUtil.createToken("config", "getClassDirectory")) {
				if (fileSystemDeployDirectory != null) {
					fileSystemDeployDirectory = null;
				}
			}
		}
		return this;
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
		return suppressContent.get(this, root);
	}

	public ConfigServerImpl resetSuppressContent() {
		suppressContent.reset();
		return this;
	}

	@Override
	public String getDefaultEncoding() {
		return getWebCharset().name();
	}

	@Override
	public Charset getTemplateCharset() {
		return CharsetUtil.toCharset(getTemplateCharsetX());
	}

	public CharsetX getTemplateCharsetX() {
		return templateCharset.get(this, root);
	}

	public ConfigServerImpl resetTemplateCharsetX() {
		templateCharset.reset();
		return this;
	}

	@Override
	public Charset getWebCharset() {
		return CharsetUtil.toCharset(getWebCharsetX());
	}

	@Override
	public CharsetX getWebCharsetX() {
		return webCharset.get(this, root);
	}

	public ConfigServerImpl resetWebCharsetX() {
		webCharset.reset();
		return this;
	}

	@Override
	public Charset getResourceCharset() {
		return CharsetUtil.toCharset(getResourceCharsetX());
	}

	@Override
	public CharsetX getResourceCharsetX() {
		return resourceCharset.get(this, root);
	}

	public ConfigServerImpl resetResourceCharsetX() {
		resourceCharset.reset();
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
			synchronized (SystemUtil.createToken("config", "getDataSources")) {
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
		if (dataSources == null) {
			synchronized (SystemUtil.createToken("config", "getDataSources")) {
				if (dataSources == null) {
					// patch
					Struct raw = ConfigUtil.getAsStruct("dataSources", root);
					raw.removeEL(KeyImpl.init("preserveSingleQuote"));

					dataSources = metaDatasourcesAll.map(this, root);
				}
			}
		}
		return dataSources;
	}

	public ConfigServerImpl resetDataSources() {
		if (dataSources != null || datasourcesNoQoQ != null) {
			synchronized (SystemUtil.createToken("config", "getDataSources")) {
				if (dataSources != null || datasourcesNoQoQ != null) {
					dataSources = null;
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
		return getMailDefaultCharsetX().toCharset();
	}

	public CharsetX getMailDefaultCharsetX() {
		return mailDefaultEncoding.get(this, root);
	}

	public ConfigServerImpl resetMailDefaultCharsetX() {
		mailDefaultEncoding.reset();
		return this;
	}

	@Override
	public ResourceProvider getDefaultResourceProvider() {
		if (defaultResourceProviderInstance == null) {
			synchronized (SystemUtil.createToken("config", "getDefaultResourceProvider")) {
				if (defaultResourceProviderInstance == null) {

					ResourceProviderDef def = metaDefaultResourceProviderDef.get(this, root);
					if (def != null) {
						try {
							defaultResourceProviderInstance = ConfigFactoryImpl.toDefaultResourceProvider(def.getClassDefinition().getClazz(), def.getArgs());
							defaultResourceProvider = def;
							Resources resources = getResources();
							resources.registerDefaultResourceProvider(defaultResourceProviderInstance);
						}
						catch (Exception e) {
							ConfigFactoryImpl.log(this, e);
							defaultResourceProviderInstance = ResourcesImpl.getFileResourceProvider();
							defaultResourceProvider = null;
						}

					}
					else {
						defaultResourceProviderInstance = ResourcesImpl.getFileResourceProvider();
						defaultResourceProvider = null;
					}

				}
			}
		}
		return defaultResourceProviderInstance;
	}

	public ConfigServerImpl resetDefaultResourceProvider() {
		if (defaultResourceProvider != null) {
			synchronized (SystemUtil.createToken("config", "getDefaultResourceProvider")) {
				if (defaultResourceProvider != null) {
					ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
					if (defaultResourceProvider != frp) {
						getResources().registerDefaultResourceProvider(frp);
					}
					defaultResourceProvider = null;
					defaultResourceProviderInstance = null;

				}
			}
		}
		return this;
	}

	@Override
	public Iterator<Entry<String, Class<CacheHandler>>> getCacheHandlers() {
		if (cacheHandlersInstances == null) {
			synchronized (SystemUtil.createToken("config", "getCacheHandlers")) {
				if (cacheHandlersInstances == null) {
					Map<String, Class<CacheHandler>> map = new HashMap<>();

					try {
						addCacheHandler(map, "request", new ClassDefinitionImpl(RequestCacheHandler.class));
						addCacheHandler(map, "timespan", new ClassDefinitionImpl(TimespanCacheHandler.class));
					}
					catch (Exception ex) {
						ConfigFactoryImpl.log(this, ex);
					}
					String strId;
					ClassDefinition cd;
					Map<String, ClassDefinition> handlersMap = metaCacheHandlers.map(this, root);
					for (Entry<String, ClassDefinition> entry: handlersMap.entrySet()) {

						strId = entry.getKey();
						cd = entry.getValue();
						if (cd.hasClass() && !StringUtil.isEmpty(strId)) {
							strId = strId.trim().toLowerCase();
							try {
								addCacheHandler(map, strId, cd);
							}
							catch (Exception ex) {
								ConfigFactoryImpl.log(this, ex);
							}
						}

					}
					cacheHandlers = handlersMap;
					cacheHandlersInstances = map;
				}
			}
		}
		return cacheHandlersInstances.entrySet().iterator();
	}

	public ConfigServerImpl resetCacheHandlers() {
		if (cacheHandlersInstances != null) {
			synchronized (SystemUtil.createToken("config", "getCacheHandlers")) {
				if (cacheHandlersInstances != null) {
					cacheHandlersInstances = null;
					cacheHandlers = null;
				}
			}
		}
		return this;
	}

	private static void addCacheHandler(Map<String, Class<CacheHandler>> cacheHandlers, String id, ClassDefinition<CacheHandler> cd) throws ClassException, BundleException {
		Class<CacheHandler> clazz = cd.getClazz();
		Object o = ClassUtil.loadInstance(clazz); // just try to load and forget afterwards
		if (o instanceof CacheHandler) {
			cacheHandlers.put(id, clazz);
		}
		else throw new ClassException("object [" + Caster.toClassName(o) + "] must implement the interface " + CacheHandler.class.getName());
	}

	private Resources getResources() {
		if (resourceProviders == null) {
			synchronized (SystemUtil.createToken("config", "getResources")) {
				if (resourceProviders == null) {

					List<ResourceProviderDef> list = metaResourceProviderDef.list(this, root);

					boolean hasHTTP = false;
					boolean hasHTTPs = false;
					boolean hasRAM = false;
					boolean hasZip = false;
					boolean hasS3 = false;
					String scheme;
					for (ResourceProviderDef def: list) {
						scheme = def.getScheme();
						if ("http".equalsIgnoreCase(scheme)) hasHTTP = true;
						else if ("https".equalsIgnoreCase(scheme)) hasHTTPs = true;
						else if ("ram".equalsIgnoreCase(scheme)) hasRAM = true;
						else if ("s3".equalsIgnoreCase(scheme)) hasS3 = true;
						else if ("zip".equalsIgnoreCase(scheme)) hasZip = true;
					}

					// adding missing providers
					if (!hasHTTP) {
						Map<String, String> args = new HashMap<>();
						args.put("lock-timeout", "10000");
						args.put("case-sensitive", "false");
						list.add(new ResourceProviderDef("http", new ClassDefinitionImpl<>(HTTPResourceProvider.class), args));
					}
					if (!hasHTTPs) {
						Map<String, String> args = new HashMap<>();
						args.put("lock-timeout", "10000");
						args.put("case-sensitive", "false");
						list.add(new ResourceProviderDef("https", new ClassDefinitionImpl<>(HTTPSResourceProvider.class), args));
					}
					if (!hasRAM) {
						Map<String, String> args = new HashMap<>();
						args.put("lock-timeout", "1000");
						args.put("case-sensitive", "true");
						list.add(new ResourceProviderDef("tar", new ClassDefinitionImpl<>(CacheResourceProvider.class), args));
					}
					if (!hasS3) {
						ClassDefinition s3Class = new ClassDefinitionImpl(DummyS3ResourceProvider.class);
						Map<String, String> args = new HashMap<>();
						args.put("lock-timeout", "10000");
						list.add(new ResourceProviderDef("s3", new ClassDefinitionImpl<>(DummyS3ResourceProvider.class), args));
					}
					if (!hasZip) {
						Map<String, String> args = new HashMap<>();
						args.put("lock-timeout", "1000");
						args.put("case-sensitive", "1000");
						list.add(new ResourceProviderDef("zip", new ClassDefinitionImpl<>(ZipResourceProvider.class), args));
					}

					for (ResourceProviderDef def: list) {
						resources.registerResourceProvider(def.getScheme(), def.getClassDefinition(), def.getArgs());
					}

					resourceProviders = list;

				}
			}
		}
		return resources;
	}

	public ConfigServerImpl resetResources() {
		if (resourceProviders != null) {
			synchronized (SystemUtil.createToken("config", "getResources")) {
				if (resourceProviders != null) {
					resourceProviders = null;
					resources.reset();
				}
			}
		}
		return this;
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
	public InnerResourceProviderFactory[] getResourceProviderFactories() {
		return ((ResourcesImpl) getResources()).getResourceProviderFactories();
	}

	@Override
	public boolean hasResourceProvider(String scheme) {
		InnerResourceProviderFactory[] factories = ((ResourcesImpl) getResources()).getResourceProviderFactories();
		for (int i = 0; i < factories.length; i++) {
			if (factories[i].getScheme().equalsIgnoreCase(scheme)) return true;
		}
		return false;
	}

	@Override
	public Resource getResource(String path) {
		return getResources().getResource(path);
	}

	public int getListenerType() {
		return listenerType.get(this, root);
	}

	public ConfigServerImpl resetListenerType() {
		listenerType.reset();
		return this;
	}

	public int getListenerMode() {
		return listenerMode.get(this, root);
	}

	public ConfigServerImpl resetListenerMode() {
		listenerMode.reset();
		return this;
	}

	public boolean getListenerSingleton() {
		return listenerSingleton.get(this, root);
	}

	public ConfigServerImpl resetListenerSingleton() {
		listenerSingleton.reset();
		return this;
	}

	@Override
	public ApplicationListener getApplicationListener() {
		if (applicationListener == null) {
			synchronized (SystemUtil.createToken("config", "getApplicationListener")) {
				if (applicationListener == null) {

					// type
					ApplicationListener listener = ConfigUtil.loadListener(getListenerType(), null);

					// mode
					listener.setMode(getListenerMode());

					// singleton
					if (listener instanceof ModernAppListener) {// FYI Mixed does extend Modern so it is included
						listener.setSingelton(getListenerSingleton());
					}
					applicationListener = listener;

				}
			}
		}
		return applicationListener;
	}

	public ConfigServerImpl resetApplicationListener() {
		if (applicationListener != null) {
			synchronized (SystemUtil.createToken("config", "getApplicationListener")) {
				if (applicationListener != null) {
					applicationListener = null;
					resetListenerType();
					resetListenerMode();
					resetListenerSingleton();
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
			synchronized (SystemUtil.createToken("config", "getScriptProtect")) {
				if (scriptProtect == null) {
					scriptProtect = AppListenerUtil.translateScriptProtect(metaScriptProtect.get(this, root), ApplicationContext.SCRIPT_PROTECT_ALL);
				}
			}
		}
		return scriptProtect;
	}

	public ConfigServerImpl resetScriptProtect() {
		if (scriptProtect != null) {
			synchronized (SystemUtil.createToken("config", "getScriptProtect")) {
				if (scriptProtect != null) {
					scriptProtect = null;
				}
			}
		}
		return this;
	}

	public boolean getProxyEnabled() {
		return proxyEnabled.get(this, root);
	}

	public ConfigServerImpl resetProxyEnabled() {
		proxyEnabled.reset();
		return this;
	}

	public String getProxyServer() {
		return proxyServer.get(this, root);
	}

	public ConfigServerImpl resetProxyServer() {
		proxyServer.reset();
		return this;
	}

	public String getProxyUsername() {
		return proxyUsername.get(this, root);
	}

	public ConfigServerImpl resetProxyUsername() {
		proxyUsername.reset();
		return this;
	}

	public String getProxyPassword() {
		return proxyPassword.get(this, root);
	}

	public ConfigServerImpl resetProxyPassword() {
		proxyPassword.reset();
		return this;
	}

	public Integer getProxyPort() {
		return proxyPort.get(this, root);
	}

	public ConfigServerImpl resetProxyPort() {
		proxyPort.reset();
		return this;
	}

	public String getProxyIncludes() {
		return proxyIncludes.get(this, root);
	}

	public ConfigServerImpl resetProxyIncludes() {
		proxyIncludes.reset();
		return this;
	}

	public String getProxyExcludes() {
		return proxyExcludes.get(this, root);
	}

	public ConfigServerImpl resetProxyExcludes() {
		proxyExcludes.reset();
		return this;
	}

	/**
	 * @return the proxyPassword
	 */
	@Override
	public ProxyData getProxyData() {
		if (proxy == null) {
			synchronized (SystemUtil.createToken("config", "getProxyData")) {
				if (proxy == null) {
					boolean enabled = getProxyEnabled();
					String server = getProxyServer();

					if (enabled && !StringUtil.isEmpty(server)) {

						String user = getProxyUsername();
						String pass = getProxyPassword();
						Integer port = getProxyPort();

						ProxyDataImpl pd = (ProxyDataImpl) ProxyDataImpl.getInstance(server, port, user, pass);

						String strIncludes = getProxyIncludes();
						Set<String> includes = proxy != null ? ProxyDataImpl.toStringSet(strIncludes) : null;
						if (includes != null) pd.setIncludes(includes);

						String strExcludes = getProxyExcludes();
						Set<String> excludes = proxy != null ? ProxyDataImpl.toStringSet(strExcludes) : null;
						if (excludes != null) pd.setExcludes(excludes);
						proxy = pd;
					}
				}
			}
		}
		return proxy;
	}

	public ConfigServerImpl resetProxyData() {
		if (proxy != null) {
			synchronized (SystemUtil.createToken("config", "getProxyData")) {
				if (proxy != null) {
					proxy = null;
					resetProxyEnabled();
					resetProxyServer();
					resetProxyUsername();
					resetProxyPassword();
					resetProxyPort();
					resetProxyIncludes();
					resetProxyExcludes();
				}
			}
		}
		return this;
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
		return componentImplicitNotation.get(this, root);
	}

	public ConfigServerImpl resetTriggerComponentDataMember() {
		componentImplicitNotation.reset();
		return this;
	}

	@Override
	public Resource getClientScopeDir() {
		if (clientDirectory == null) {
			synchronized (SystemUtil.createToken("config", "getClientScopeDir")) {
				if (clientDirectory == null) {
					Resource configDir = getConfigDir();

					String strClientDirectory = metaClientScopeDir.get(this, root);
					if (!StringUtil.isEmpty(strClientDirectory, true)) {
						strClientDirectory = ConfigUtil.translateOldPath(strClientDirectory.trim());
						clientDirectory = ConfigUtil.getFile(configDir, strClientDirectory, "client-scope", configDir, FileUtil.TYPE_DIR, ResourceUtil.LEVEL_PARENT_FILE, this);
					}
					else {
						clientDirectory = configDir.getRealResource("client-scope");
					}
				}
			}
		}
		return clientDirectory;
	}

	public ConfigServerImpl resetClientScopeDir() {
		if (clientDirectory != null) {
			synchronized (SystemUtil.createToken("config", "getClientScopeDir")) {
				if (clientDirectory != null) {
					clientDirectory = null;
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
		if (clientDirectoryMaxSize == null) {
			synchronized (SystemUtil.createToken("config", "getClientScopeDirSize")) {
				if (clientDirectoryMaxSize == null) {
					clientDirectoryMaxSize = ByteSizeParser.parseByteSizeDefinition(metaClientScopeDirSize.get(this, root).trim(), 1024L * 1024L * 100L);
				}
			}
		}
		return clientDirectoryMaxSize;
	}

	public ConfigServerImpl resetClientScopeDirSize() {
		if (clientDirectoryMaxSize != null) {
			synchronized (SystemUtil.createToken("config", "getClientScopeDirSize")) {
				if (clientDirectoryMaxSize != null) {
					clientDirectoryMaxSize = null;
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

	@Override
	public PhysicalClassLoader getDirectClassLoader(boolean reload) throws IOException {
		if (directClassLoader == null || reload) {
			synchronized (SystemUtil.createToken("config", "getDirectClassLoader")) {
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
		if (cacheDirectory == null) {
			synchronized (SystemUtil.createToken("config", "getCacheDir")) {
				if (cacheDirectory == null) {
					Resource configDir = getConfigDir();

					String strCacheDirectory = metaCacheDir.get(this, root);
					if (!StringUtil.isEmpty(strCacheDirectory)) {
						strCacheDirectory = ConfigUtil.translateOldPath(strCacheDirectory);
						cacheDirectory = ConfigUtil.getFile(configDir, strCacheDirectory, "cache", configDir, FileUtil.TYPE_DIR, ResourceUtil.LEVEL_GRAND_PARENT_FILE, this);
					}
					else {
						cacheDirectory = configDir.getRealResource("cache");
					}
				}
			}
		}
		return cacheDirectory;
	}

	public ConfigServerImpl resetCacheDir() {
		if (cacheDirectory != null) {
			synchronized (SystemUtil.createToken("config", "getCacheDir")) {
				if (cacheDirectory != null) {
					cacheDirectory = null;
				}
			}
		}
		return this;
	}

	@Override
	public long getCacheDirSize() {
		if (cacheDirectoryMaxSize == null) {
			synchronized (SystemUtil.createToken("config", "getCacheDirSize")) {
				if (cacheDirectoryMaxSize == null) {
					cacheDirectoryMaxSize = ByteSizeParser.parseByteSizeDefinition(metaCacheDirSize.get(this, root), 1024L * 1024L * 100L);
					;
				}
			}
		}
		return cacheDirectoryMaxSize;
	}

	public ConfigServerImpl resetCacheDirSize() {
		if (cacheDirectoryMaxSize != null) {
			synchronized (SystemUtil.createToken("config", "getCacheDirSize")) {
				if (cacheDirectoryMaxSize != null) {
					cacheDirectoryMaxSize = null;
				}
			}
		}
		return this;
	}

	public DumpWriterEntry[] getDumpWritersEntries() {
		if (dumpWriters == null) {
			synchronized (SystemUtil.createToken("config", "getDumpWritersEntries")) {
				if (dumpWriters == null) {
					dumpWriters = ConfigFactoryImpl.loadDumpWriter(this, root, null);
					// MUST handle default value was returned
				}
			}
		}
		return dumpWriters;
	}

	public ConfigServerImpl resetDumpWritersEntries() {
		if (dumpWriters != null) {
			synchronized (SystemUtil.createToken("config", "getDumpWritersEntries")) {
				if (dumpWriters != null) {
					dumpWriters = null;
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
		return componentUseVariablesScope.get(this, root);
	}

	public ConfigServerImpl resetComponentShadow() {
		componentUseVariablesScope.reset();
		return this;
	}

	@Override
	public boolean useComponentPathCache() {
		return componentUseCachePath.get(this, root);
	}

	public ConfigServerImpl resetComponentPathCache() {
		componentUseCachePath.reset();
		return this;
	}

	@Override
	public boolean useCTPathCache() {
		return customTagUseCachePath.get(this, root);
	}

	public ConfigServerImpl resetUseCTPathCache() {
		customTagUseCachePath.reset();
		return this;
	}

	public void flushApplicationPathCache() {
		if (applicationPathCache != null) applicationPathCache.clear();
	}

	public void flushCTPathCache() {
		if (ctPatchCache != null) ctPatchCache.clear();
	}

	@Override
	public PrintWriter getErrWriter() {
		if (systemErr == null) {
			synchronized (SystemUtil.createToken("config", "getErrWriter")) {
				if (systemErr == null) {
					String strErr = metaErr.get(this, root);
					PrintStream ps = ConfigFactoryImpl.toPrintStream(this, strErr, true);

					if (ps == null) {
						systemErr = SystemUtil.getPrintWriter(SystemUtil.ERR);
					}
					else {
						systemErr = new PrintWriter(ps);
						System.setErr(ps);
					}
				}
			}
		}
		return systemErr;
	}

	public ConfigServerImpl resetErrWriter() {
		if (systemErr != null) {
			synchronized (SystemUtil.createToken("config", "getErrWriter")) {
				if (systemErr != null) {
					systemErr = null;
				}
			}
		}
		return this;
	}

	@Override
	public PrintWriter getOutWriter() {
		if (systemOut == null) {
			synchronized (SystemUtil.createToken("config", "getOutWriter")) {
				if (systemOut == null) {
					String strOut = metaOut.get(this, root);
					PrintStream ps = ConfigFactoryImpl.toPrintStream(this, strOut, false);

					if (ps == null) {
						systemOut = SystemUtil.getPrintWriter(SystemUtil.OUT);
					}
					else {
						systemOut = new PrintWriter(ps);
						System.setOut(ps);
					}
				}
			}
		}
		return systemOut;
	}

	public ConfigServerImpl resetOutWriter() {
		if (systemOut != null) {
			synchronized (SystemUtil.createToken("config", "getOutWriter")) {
				if (systemOut != null) {
					systemOut = null;
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
	@Override
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

	public boolean getLocalCustomTag() {
		return doLocalCustomTag();
	}

	@Override
	public boolean doLocalCustomTag() {
		return customTagLocalSearch.get(this, root);
	}

	public ConfigServerImpl resetLocalCustomTag() {
		customTagLocalSearch.reset();
		return this;
	}

	@Override
	public String[] getCustomTagExtensions() {
		if (customTagExtensions == null) {
			synchronized (SystemUtil.createToken("config", "getCustomTagExtensions")) {
				if (customTagExtensions == null) {
					customTagExtensions = ListUtil.trimItems(ListUtil.listToStringArray(metaCustomTagExtensions.get(this, root), ','));
				}
			}
		}
		return customTagExtensions;
	}

	public ConfigServerImpl resetCustomTagExtensions() {
		if (customTagExtensions != null) {
			synchronized (SystemUtil.createToken("config", "getCustomTagExtensions")) {
				if (customTagExtensions != null) {
					customTagExtensions = null;
				}
			}
		}
		return this;
	}

	public boolean getComponentDeepSearch() {
		return doComponentDeepSearch();
	}

	@Override
	public boolean doComponentDeepSearch() {
		return componentDeepSearch.get(this, root);
	}

	public ConfigServerImpl resetComponentDeepSearch() {
		componentDeepSearch.reset();
		return this;
	}

	public boolean getCustomTagDeepSearch() {
		return doCustomTagDeepSearch();
	}

	@Override
	public boolean doCustomTagDeepSearch() {
		return customTagDeepSearch.get(this, root);
	}

	public ConfigServerImpl resetCustomTagDeepSearch() {
		customTagDeepSearch.reset();
		return this;
	}

	/**
	 * @return the version
	 */
	@Override
	public double getVersion() {
		return version.get(this, root);
	}

	public ConfigServerImpl resetVersion() {
		version.reset();
		return this;
	}

	@Override
	public boolean closeConnection() {
		return closeConnection.get(this, root);
	}

	public ConfigServerImpl resetConnection() {
		closeConnection.reset();
		return this;
	}

	@Override
	public boolean contentLength() {
		return contentLength.get(this, root);
	}

	public ConfigServerImpl resetContentLength() {
		contentLength.reset();
		return this;
	}

	@Override
	public boolean allowCompression() {
		return allowCompression.get(this, root);
	}

	public ConfigServerImpl resetAllowCompression() {
		allowCompression.reset();
		return this;
	}

	/**
	 * @return the constants
	 */
	@Override
	public Struct getConstants() {
		if (constants == null) {
			synchronized (SystemUtil.createToken("config", "getConstants")) {
				if (constants == null) {
					constants = metaConstants.get(this, root);
				}
			}
		}
		return constants;
	}

	public ConfigServerImpl resetConstants() {
		if (constants != null) {
			synchronized (SystemUtil.createToken("config", "getConstants")) {
				if (constants != null) {
					constants = null;
					placeHolderdata = null;

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
		return showVersion.get(this, root);
	}

	public ConfigServerImpl resetShowVersion() {
		showVersion.reset();
		return this;
	}

	@Override
	public RemoteClient[] getRemoteClients() {
		if (remoteClientsRemoteClient == null) {
			synchronized (SystemUtil.createToken("config", "getRemoteClients")) {
				if (remoteClientsRemoteClient == null) {
					List<RemoteClient> list = metaRemoteClients.list(this, root);

					remoteClientsRemoteClient = list.toArray(new RemoteClient[list.size()]);
				}
			}
		}
		return remoteClientsRemoteClient;
	}

	public ConfigServerImpl resetRemoteClients() {
		if (remoteClientsRemoteClient != null) {
			synchronized (SystemUtil.createToken("config", "getRemoteClients")) {
				if (remoteClientsRemoteClient != null) {
					remoteClientsRemoteClient = null;
				}
			}
		}
		return this;
	}

	@Override
	public SpoolerEngine getSpoolerEngine() {
		if (remoteClientSpoolerEngine == null) {
			synchronized (SystemUtil.createToken("config", "getSpoolerEngine")) {
				if (remoteClientSpoolerEngine == null) {
					remoteClientSpoolerEngine = new SpoolerEngineImpl(this, "Remote Client Spooler");
				}
			}
		}
		return remoteClientSpoolerEngine;
	}

	public int getRemoteClientMaxThreads() {
		return remoteClientsMaxThreads.get(this, root);
	}

	public ConfigServerImpl resetRemoteClientMaxThreads() {
		remoteClientsMaxThreads.reset();
		return this;
	}

	//
	@Override
	public Resource getRemoteClientDirectory() {
		if (remoteClientsDirectory == null) {
			synchronized (SystemUtil.createToken("config", "getRemoteClientDirectory")) {
				if (remoteClientsDirectory == null) {
					String strDir = metaRemoteClientDirectory.get(this, root);
					remoteClientsDirectory = ConfigUtil.getFile(getRootDirectory(), strDir, "client-task", getConfigDir(), FileUtil.TYPE_DIR, ResourceUtil.LEVEL_GRAND_PARENT_FILE,
							this);

					if (!remoteClientsDirectory.exists()) remoteClientsDirectory.mkdirs();
				}
			}
		}
		return remoteClientsDirectory;
	}

	public ConfigServerImpl resetRemoteClientDirectory() {
		if (remoteClientsDirectory != null) {
			synchronized (SystemUtil.createToken("config", "getRemoteClientDirectory")) {
				if (remoteClientsDirectory != null) {
					remoteClientsDirectory = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getErrorStatusCode() {
		return errorStatusCode.get(this, root);
	}

	public ConfigServerImpl resetErrorStatusCode() {
		errorStatusCode.reset();
		return this;
	}

	@Override
	public int getLocalMode() {
		return localScopeMode.get(this, root);
	}

	public ConfigServerImpl resetLocalMode() {
		localScopeMode.reset();
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
	public List<String> getExtensionProvidersGroupIds() {
		if (extensionProviders == null) {
			synchronized (SystemUtil.createToken("config", "extensionProviders")) {
				if (extensionProviders == null) {
					extensionProviders = metaExtensionProviders.list(this, root);

					// Remove legacy URL-based providers (pre-7.2 REST endpoints)
					extensionProviders.removeIf(p -> p.startsWith("http://") || p.startsWith("https://"));

					// "org.lucee" always needs to exist
					if (extensionProviders.size() == 0 || !extensionProviders.contains("org.lucee")) {
						extensionProviders.add("org.lucee");
					}
				}
			}
		}
		return extensionProviders;
	}

	public ConfigServerImpl resetExtensionProviderGroupIds() {
		if (extensionProviders != null) {
			synchronized (SystemUtil.createToken("config", "extensionProviders")) {
				if (extensionProviders != null) {
					extensionProviders = null;
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

		if (extensionsX == null) {
			synchronized (SystemUtil.createToken("config", "extensions")) {
				if (extensionsX == null) {
					boolean firstLoad = extensionsLoadCount == 0;
					Log log = getLog("deploy");
					extensionsLoadCount++;
					List<ExtensionDefintion> definitions = getExtensionDefinitions();
					if (LogUtil.doesInfo(log)) log.info("extensions", "Loading " + definitions.size() + " extension definitions from config");
					// print.e(extensions);
					Map<String, RHExtension> exts = new HashMap<>();
					{
						RHExtension ext;
						for (ExtensionDefintion ed: definitions) {
							try {
								if (LogUtil.doesDebug(log)) log.debug("extensions", "Converting extension definition: " + ed);
								ext = ed.toRHExtension(this);
								if (!ext.installed()) {
									if (LogUtil.doesInfo(log)) log.info("extensions", "Deploying extension: " + ext.getId() + " v" + ext.getVersion());
									DeployHandler.deployExtension(this, ext, false, false, log);
								}
								if (LogUtil.doesDebug(log)) log.debug("extensions", "Added extension: " + ext.getStorageName());
								exts.put(ext.getStorageName(), ext);
							}
							catch (Exception ex) {
								if (LogUtil.doesError(log)) {
									log.error("start-bundles", ex);
								}
							}

						}
					}

					// start bundles in parallel but wait for them to finish
					CountDownLatch latch = new CountDownLatch(exts.size());
					try (ExecutorService executor = ThreadUtil.createExecutorService()) {

						for (RHExtension ext: exts.values()) {
							executor.submit(() -> {
								try {
									// Call the startBundles method for each extension
									startBundles(this, ext, firstLoad);
								}
								catch (Exception ex) {
									if (LogUtil.doesError(log)) {
										log.error("start-bundles", ex);
									}
								}
								finally {
									// Count down the latch regardless of success or failure
									latch.countDown();
								}
							});
						}

						// Wait for all virtual threads to complete
						try {
							latch.await();
						}
						catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							throw new RuntimeException("Interrupted while waiting for extension processing", e);
						}
					}

					// uninstall extensions no longer used
					Boolean cleanupExtension = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.cleanup.extension", null), true);
					if (cleanupExtension) {

						List<lucee.runtime.extension.RHExtensionCollection.Entry> installedExtensions = RHExtension.getInstalledExtensions(this);
						if (!installedExtensions.isEmpty()) {
							ResetFilter filter = null;
							try {

								for (lucee.runtime.extension.RHExtensionCollection.Entry entry: installedExtensions) {
									if (!exts.containsKey(entry.getFilename())) {
										// is it installed in a different version what should not happen
										RHExtension other = RHExtension.getInstalledDifferentVersion(this, entry.getRHExtension().toExtensionDefinition(), log);
										if (other != null) {

											if (LogUtil.doesError(log)) {
												log.error("start-bundles",
														"Found the extension [" + entry.getFilename() + "] in the installed folder what is installed in a different version");
											}
											try {
												entry.getRHExtension().delete(this, log);
											}
											catch (Exception ex) {
												if (LogUtil.doesError(log)) {
													log.error("start-bundles", ex);
												}
											}
										}
										else {

											if (LogUtil.doesInfo(log)) {
												log.info("start-bundles", "Found the extension [" + entry.getRHExtension().toExtensionDefinition()
														+ "] in the installed folder that is not present in the configuration in any version, so we will uninstall it");
											}

											try {
												if (filter == null) filter = new ResetFilter();
												ConfigAdmin._removeRHExtension(this, entry.getRHExtension(), null, filter, true, log);
												if (LogUtil.doesInfo(log)) {
													log.info("start-bundles", "removed extension [" + entry.getRHExtension().toExtensionDefinition() + "]");
												}
											}
											catch (PageException ex) {
												LogUtil.log("deploy", "start-bundles", ex);
											}
										}
									}
								}
							}
							finally {
								try {
									if (filter != null) filter.reset(this);
								}
								catch (Exception ex) {
									if (LogUtil.doesError(log)) {
										log.error("start-bundles", ex);
									}
								}
							}
						}
					}
					extensionsX = exts.values().toArray(new RHExtension[exts.size()]);
				}
			}
		}
		return extensionsX;
	}

	private static void startBundles(ConfigServerImpl config, RHExtension rhe, boolean firstLoad) throws IOException, BundleException {
		Log log = config.getLog("deploy");
		if (LogUtil.doesInfo(log)) log.info("extensions", "Starting bundles for extension: " + rhe.getId() + " v" + rhe.getVersion());
		if (rhe.getMetadata().isStartBundles()) {
			if (!firstLoad) {
				if (LogUtil.doesDebug(log)) log.debug("extensions", "Deploying bundles (not first load): " + rhe.getId());
				rhe.deployBundles(config, true);
			}
			else {
				try {
					BundleInfo[] bundles = rhe.getMetadata().getBundles();
					if (bundles != null) {
						if (LogUtil.doesInfo(log)) log.info("extensions", "Loading " + bundles.length + " bundles for: " + rhe.getId());
						for (BundleInfo bi: bundles) {
							if (LogUtil.doesDebug(log)) log.debug("extensions", "  Loading bundle: " + bi.getSymbolicName() + " v" + bi.getVersion());
							OSGiUtil.loadBundleFromLocal(bi.getSymbolicName(), bi.getVersion(), null, false, null);
						}
					}
				}
				catch (Exception ex) {
					if (LogUtil.doesWarn(log)) log.error("extensions", "Exception loading bundles for " + rhe.getId() + ", deploying instead", ex);
					rhe.deployBundles(config, true);
				}
			}
		}
		if (LogUtil.doesInfo(log)) log.info("extensions", "Finished bundles for extension: " + rhe.getId());

	}

	public ConfigServerImpl resetRHExtensions() {
		if (extensionsX != null) {
			synchronized (SystemUtil.createToken("config", "extensions")) {
				if (extensionsX != null) {
					extensionsX = null;
				}
			}
		}

		return this;
	}

	public List<ExtensionDefintion> getExtensionDefinitions() {
		if (extensions == null) {
			synchronized (SystemUtil.createToken("config", "getExtensionDefinitions")) {
				if (extensions == null) {

					// remove duplicates
					Array raw = ConfigUtil.getAsArray("extensions", root);
					try {
						RHExtension.removeDuplicates(raw);
						// LDEV-6329: skip extensions explicitly disabled via the "enabled" flag
						RHExtension.removeDisabled(raw);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						ConfigFactoryImpl.log(this, t);
					}
					extensions = metaExtensions.list(this, root);
				}
			}
		}
		return extensions;
	}

	public ConfigServerImpl resetExtensionDefinitions() {
		if (extensions != null) {
			synchronized (SystemUtil.createToken("config", "getExtensionDefinitions")) {
				if (extensions != null) {
					extensions = null;
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
		return fileSystemAllowRealpath.get(this, root);
	}

	public ConfigServerImpl resetAllowRealPath() {
		fileSystemAllowRealpath.reset();
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
		return remoteClientsUsage.get(this, root);
	}

	public ConfigServerImpl resetRemoteClientUsage() {
		remoteClientsUsage.reset();
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
		// allow overriding the configured inspectTemplate via system property / environment variable
		String strInspectTemplate = SystemUtil.getSystemPropOrEnvVar("lucee.inspect.template", null);
		if (!StringUtil.isEmpty(strInspectTemplate, true)) return ConfigUtil.inspectTemplate(strInspectTemplate, ConfigPro.INSPECT_AUTO);
		return inspectTemplate.get(this, root);
	}

	public ConfigServerImpl resetInspectTemplate() {
		inspectTemplate.reset();
		// Admin inspect-mode change drops resolution caches on all web configs — entries
		// populated under the previous mode contract shouldn't carry across the boundary.
		for (ConfigWeb cw: getConfigWebs()) {
			if (cw instanceof ConfigWebPro) ((ConfigWebPro) cw).clearResolvedMappingPaths();
		}
		return this;
	}

	@Override
	public boolean getTypeChecking() {
		return typeChecking.get(this, root);
	}

	public ConfigServerImpl resetTypeChecking() {
		typeChecking.reset();
		return this;
	}

	@Override
	public int getInspectTemplateAutoInterval(boolean slow) {
		if (inspectTemplateAutoIntervalSlow == ConfigPro.INSPECT_INTERVAL_UNDEFINED) {
			synchronized (SystemUtil.createToken("config", "getInspectTemplateAutoInterval")) {
				if (inspectTemplateAutoIntervalSlow == ConfigPro.INSPECT_INTERVAL_UNDEFINED) {
					inspectTemplateAutoIntervalFast = metaInspectTemplateAutoIntervalFast.get(this, root);
					inspectTemplateAutoIntervalSlow = metaInspectTemplateAutoIntervalSlow.get(this, root);
				}
			}
		}
		return slow ? inspectTemplateAutoIntervalSlow : inspectTemplateAutoIntervalFast;
	}

	public ConfigServerImpl resetInspectTemplateAutoInterval() {
		if (inspectTemplateAutoIntervalSlow != ConfigPro.INSPECT_INTERVAL_UNDEFINED) {
			synchronized (SystemUtil.createToken("config", "getInspectTemplateAutoInterval")) {
				if (inspectTemplateAutoIntervalSlow != ConfigPro.INSPECT_INTERVAL_UNDEFINED) {
					inspectTemplateAutoIntervalSlow = ConfigPro.INSPECT_INTERVAL_UNDEFINED;
					inspectTemplateAutoIntervalFast = ConfigPro.INSPECT_INTERVAL_UNDEFINED;
				}
			}
		}
		return this;
	}

	public void shutdown() {
		inspectScheduler.shutdownNow();
	}

	public void ensureInspectTickerStarted() {
		ScheduledFuture<?> f = nextTick;
		if (f != null && !f.isDone()) return;
		synchronized (tickToken) {
			f = nextTick;
			if (f != null && !f.isDone()) return;
			scheduleNextTick(getInspectTemplateAutoInterval(true));
		}
	}

	public void requestFastTick() {
		fastRequested.set(true);

		long fast = getInspectTemplateAutoInterval(false);
		ScheduledFuture<?> existing = nextTick;
		if (existing == null || existing.isDone()) {
			synchronized (tickToken) {
				existing = nextTick;
				if (existing == null || existing.isDone()) {
					scheduleNextTick(fast);
					return;
				}
			}
		}
		if (existing.getDelay(TimeUnit.MILLISECONDS) > fast) {
			synchronized (tickToken) {
				ScheduledFuture<?> cur = nextTick;
				if (cur != null && cur.getDelay(TimeUnit.MILLISECONDS) > fast) {
					scheduleNextTick(fast);
				}
			}
		}
	}

	private void scheduleNextTick(long delayMs) {
		ScheduledFuture<?> old = nextTick;
		if (old != null) old.cancel(false);
		try {
			nextTick = inspectScheduler.schedule(this::runInspectTick, delayMs, TimeUnit.MILLISECONDS);
		}
		catch (java.util.concurrent.RejectedExecutionException ree) {
			nextTick = null;
		}
	}

	private void runInspectTick() {
		try {
			boolean anyAuto = inspectAllAutoMappings();
			synchronized (tickToken) {
				boolean fast = fastRequested.getAndSet(false);
				if (anyAuto) {
					long delay = fast ? getInspectTemplateAutoInterval(false) : getInspectTemplateAutoInterval(true);
					scheduleNextTick(delay);
				}
				else nextTick = null;
			}
		}
		catch (Throwable t) {
			if (inspectScheduler.isShutdown()) return;
			LogUtil.log(this, "inspect-ticker", t);
			scheduleNextTick(getInspectTemplateAutoInterval(true));
		}
	}

	private boolean inspectAllAutoMappings() {
		boolean anyAuto = false;
		anyAuto |= resetMatching(getMappings());
		anyAuto |= resetMatching(getCustomTagMappings());
		anyAuto |= resetMatching(getComponentMappings());
		anyAuto |= resetMatching(getFunctionMappings());
		anyAuto |= resetMatching(getTagMappings());
		for (ConfigWeb cw: getConfigWebs()) {
			if (!(cw instanceof ConfigWebPro)) continue;
			ConfigWebPro cwp = (ConfigWebPro) cw;
			anyAuto |= resetMatching(cwp.getMappings());
			anyAuto |= resetMatching(cwp.getCustomTagMappings());
			anyAuto |= resetMatching(cwp.getComponentMappings());
			anyAuto |= resetMatching(cwp.getFunctionMappings());
			anyAuto |= resetMatching(cwp.getTagMappings());
			anyAuto |= resetMatching(cwp.getApplicationMappings());
			// Re-check negative entries in the resolution cache for AUTO ConfigWebs only.
			// On a healthy config (all paths exist) this is a zero-syscall walk.
			if (cwp.getInspectTemplate() == ConfigPro.INSPECT_AUTO) cwp.revalidateNegativeMappingPaths();
		}
		return anyAuto;
	}

	private static boolean resetMatching(Mapping[] mappings) {
		if (mappings == null) return false;
		boolean any = false;
		for (Mapping m: mappings) {
			if (matchesAutoPhysical(m)) {
				any = true;
				((MappingImpl) m).resetPages(null);
			}
		}
		return any;
	}

	private static boolean resetMatching(Collection<Mapping> mappings) {
		if (mappings == null) return false;
		boolean any = false;
		for (Mapping m: mappings) {
			if (matchesAutoPhysical(m)) {
				any = true;
				((MappingImpl) m).resetPages(null);
			}
		}
		return any;
	}

	private static boolean matchesAutoPhysical(Mapping m) {
		if (m == null) return false;
		if (m.getInspectTemplate() != ConfigPro.INSPECT_AUTO) return false;
		if (m.getPhysical() == null) return false;
		return true;
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

	public String getCacheDefaultResource() {
		return cacheDefaultResource.get(this, root);
	}

	public ConfigServerImpl resetCacheDefaultResource() {
		cacheDefaultResource.reset();
		return this;
	}

	public String getCacheDefaultFunction() {
		return cacheDefaultFunction.get(this, root);
	}

	public ConfigServerImpl resetCacheDefaultFunction() {
		cacheDefaultFunction.reset();
		return this;
	}

	public String getCacheDefaultInclude() {
		return cacheDefaultInclude.get(this, root);
	}

	public ConfigServerImpl resetCacheDefaultInclude() {
		cacheDefaultInclude.reset();
		return this;
	}

	public String getCacheDefaultQuery() {
		return cacheDefaultQuery.get(this, root);
	}

	public ConfigServerImpl resetCacheDefaultQuery() {
		cacheDefaultQuery.reset();
		return this;
	}

	public String getCacheDefaultTemplate() {
		return cacheDefaultTemplate.get(this, root);
	}

	public ConfigServerImpl resetCacheDefaultTemplate() {
		cacheDefaultTemplate.reset();
		return this;
	}

	public String getCacheDefaultObject() {
		return cacheDefaultObject.get(this, root);
	}

	public ConfigServerImpl resetCacheDefaultObject() {
		cacheDefaultObject.reset();
		return this;
	}

	public String getCacheDefaultFile() {
		return cacheDefaultFile.get(this, root);
	}

	public ConfigServerImpl resetCacheDefaultFile() {
		cacheDefaultFile.reset();
		return this;
	}

	public String getCacheDefaultHTTP() {
		return cacheDefaultHTTP.get(this, root);
	}

	public ConfigServerImpl resetCacheDefaultHTTP() {
		cacheDefaultHTTP.reset();
		return this;
	}

	public String getCacheDefaultWebservice() {
		return cacheDefaultWebservice.get(this, root);
	}

	public ConfigServerImpl resetCacheDefaultWebservice() {
		cacheDefaultWebservice.reset();
		return this;
	}

	public Map<Integer, String> getCacheDefaultConnectionNames() {
		if (cacheDefaultConnectionNames == null) {
			synchronized (SystemUtil.createToken("config", "getCacheDefaultConnectionName")) {
				if (cacheDefaultConnectionNames == null) {
					Map<Integer, String> names = new HashMap<>();

					// resource
					String str = getCacheDefaultResource();
					if (!StringUtil.isEmpty(str, true)) names.put(ConfigPro.CACHE_TYPE_RESOURCE, str);

					// function
					str = getCacheDefaultFunction();
					if (!StringUtil.isEmpty(str, true)) names.put(ConfigPro.CACHE_TYPE_FUNCTION, str);

					// include
					str = getCacheDefaultInclude();
					if (!StringUtil.isEmpty(str, true)) names.put(ConfigPro.CACHE_TYPE_INCLUDE, str);

					// query
					str = getCacheDefaultQuery();
					if (!StringUtil.isEmpty(str, true)) names.put(ConfigPro.CACHE_TYPE_QUERY, str);

					// template
					str = getCacheDefaultTemplate();
					if (!StringUtil.isEmpty(str, true)) names.put(ConfigPro.CACHE_TYPE_TEMPLATE, str);

					// object
					str = getCacheDefaultObject();
					if (!StringUtil.isEmpty(str, true)) names.put(ConfigPro.CACHE_TYPE_OBJECT, str);

					// file
					str = getCacheDefaultFile();
					if (!StringUtil.isEmpty(str, true)) names.put(ConfigPro.CACHE_TYPE_FILE, str);

					// HTTP
					str = getCacheDefaultHTTP();
					if (!StringUtil.isEmpty(str, true)) names.put(ConfigPro.CACHE_TYPE_HTTP, str);

					// Webservice
					str = getCacheDefaultWebservice();
					if (!StringUtil.isEmpty(str, true)) names.put(ConfigPro.CACHE_TYPE_WEBSERVICE, str);

					cacheDefaultConnectionNames = names;
				}
			}
		}
		return cacheDefaultConnectionNames;
	}

	public ConfigServerImpl resetCacheDefaultConnectionNames() {
		if (cacheDefaultConnectionNames != null) {
			synchronized (SystemUtil.createToken("config", "getCacheDefaultConnectionName")) {
				if (cacheDefaultConnectionNames != null) {
					cacheDefaultConnectionNames = null;

					resetCacheDefaultFile();
					resetCacheDefaultFunction();
					resetCacheDefaultHTTP();
					resetCacheDefaultInclude();
					resetCacheDefaultObject();
					resetCacheDefaultQuery();
					resetCacheDefaultResource();
					resetCacheDefaultTemplate();
					resetCacheDefaultWebservice();
				}
			}
		}
		return this;
	}

	@Override
	public String getCacheDefaultConnectionName(int type) {
		String res = getCacheDefaultConnectionNames().get(type);
		if (StringUtil.isEmpty(res, true)) return "";
		return res.trim();
	}

	public Map<Integer, CacheConnection> getCacheDefaultConnections() {
		if (cacheDefaultConnection == null) {
			synchronized (SystemUtil.createToken("config", "getCacheDefaultConnection")) {
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

	public ConfigServerImpl resetCacheDefaultConnections() {
		if (cacheDefaultConnection != null) {
			synchronized (SystemUtil.createToken("config", "getCacheDefaultConnection")) {
				if (cacheDefaultConnection != null) {
					cacheDefaultConnection = null;
				}
			}
		}
		return this;
	}

	@Override
	public Map<String, CacheConnection> getCacheConnections() {// = new HashMap<String, CacheConnection>()
		if (caches == null) {
			synchronized (SystemUtil.createToken("config", "getCacheConnections")) {
				if (caches == null) {
					caches = metaCacheConnection.map(this, root);
				}
			}
		}
		return caches;
	}

	public ConfigServerImpl resetCacheConnections() {// = new HashMap<String, CacheConnection>()
		if (caches != null) {
			synchronized (SystemUtil.createToken("config", "getCacheConnections")) {
				if (caches != null) {
					caches = null;
				}
			}
		}
		return this;
	}

	public ConfigServerImpl resetCacheAll() {
		resetCacheDefaultConnectionNames();
		resetCacheDefaultConnections();
		resetCacheConnections();
		resetCacheDefinitions();
		return this;
	}

	@Override
	public String getDapSecret() {
		return dapSecret.get(this, root);
	}

	public ConfigServerImpl resetDapSecret() {
		dapSecret.reset();
		return this;
	}

	@Override
	public boolean getDapBreakpoint() {
		return dapBreakpoint.get(this, root);
	}

	public ConfigServerImpl resetDapBreakpoint() {
		dapBreakpoint.reset();
		return this;
	}

	@Override
	public boolean getExecutionLogEnabled() {
		if (getDapBreakpoint()) return true;
		return executionLogEnabled.get(this, root);
	}

	public ConfigServerImpl resetExecutionLogEnabled() {
		executionLogEnabled.reset();
		return this;
	}

	@Override
	public ExecutionLogFactory getExecutionLogFactory() {
		if (executionLogFactory == null) {
			synchronized (SystemUtil.createToken("config", "getExecutionLogFactory")) {
				if (executionLogFactory == null) {
					executionLogFactory = ConfigFactoryImpl.loadExeLog(this, root);
				}
			}
		}
		return executionLogFactory;
	}

	public ConfigServerImpl resetExecutionLogFactory() {
		if (executionLogFactory != null) {
			synchronized (SystemUtil.createToken("config", "getExecutionLogFactory")) {
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
			synchronized (SystemUtil.createToken("ConfigImpl", "getORMEngine:" + name)) {
				engine = ormengines.get(name);
				if (engine == null) {
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
				}
			}
		}
		return engine;
	}

	@Override
	public boolean hasORMEngine() {
		return getORMEngineClassDefintion().equals(DEFAULT_ORM_ENGINE);
	}

	public Struct getORM() {
		return orm.get(this, root);
	}

	public ConfigServerImpl resetORM() {
		orm.reset();
		return this;
	}

	@Override
	public ClassDefinition<? extends ORMEngine> getORMEngineClassDefintion() {
		if (ormCD == null) {
			synchronized (SystemUtil.createToken("config", "ormCD")) {
				if (ormCD == null) {

					// class
					ClassDefinition cd = null;
					try {
						cd = ConfigFactoryImpl.getClassDefinition(this, getORM(), "engine", getIdentification());
					}
					catch (Exception ex) {
						ConfigFactoryImpl.log(this, ex);

					}
					ormCD = cd == null || !cd.hasClass() ? DEFAULT_ORM_ENGINE : cd;
				}
			}
		}
		return ormCD;
	}

	public ConfigServerImpl resetORMEngineClassDefintion() {
		if (ormCD != null) {
			synchronized (SystemUtil.createToken("config", "ormCD")) {
				if (ormCD != null) {
					resetORM();
					ormCD = null;
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
		if (initOrmConfig) {
			synchronized (SystemUtil.createToken("config", "ormConfig")) {
				if (initOrmConfig) {

					Struct data = getORM();
					// class

					ormConfig = data == null ? null : ORMConfigurationImpl.load(this, null, data, this.getRootDirectory(), null);
					initOrmConfig = false;
				}
			}
		}
		return ormConfig;
	}

	public ConfigServerImpl resetORMConfig() {
		if (!initOrmConfig) {
			synchronized (SystemUtil.createToken("config", "ormCD")) {
				if (!initOrmConfig) {
					resetORM();
					ormConfig = null;
					initOrmConfig = true;
				}
			}
		}
		return this;
	}

	@Override
	public PageSource getApplicationPageSource(PageContext pc, String path, String filename, int mode, RefBoolean isCFC) {
		if (applicationPathCache == null) return null;
		String id = path + ":" + filename + ":" + mode;

		SoftReference<CacheElement> tmp = getApplicationPathCacheTimeout() <= 0 ? null : applicationPathCache.get(id);
		if (tmp != null) {
			CacheElement ce = tmp.get();
			if (ce != null && (ce.created + getApplicationPathCacheTimeout()) >= System.currentTimeMillis()) {
				if (ce.pageSource.loadPage(pc, false, (Page) null) != null) {
					if (isCFC != null) isCFC.setValue(ce.isCFC);
					return ce.pageSource;
				}
				applicationPathCache.remove(id);
			}
		}
		return null;
	}

	@Override
	public void putApplicationPageSource(String path, PageSource ps, String filename, int mode, boolean isCFC) {
		if (getApplicationPathCacheTimeout() <= 0) return;
		if (applicationPathCache == null) applicationPathCache = new ConcurrentHashMap<String, SoftReference<CacheElement>>();// MUSTMUST new
		String id = path + ":" + filename + ":" + mode;
		applicationPathCache.put(id, new SoftReference<CacheElement>(new CacheElement(ps, isCFC)));
	}

	@Override
	public long getApplicationPathCacheTimeout() {
		if (applicationPathTimeout == null) {
			synchronized (SystemUtil.createToken("config", "getApplicationPathCacheTimeout")) {
				if (applicationPathTimeout == null) {
					applicationPathTimeout = metaApplicationPathCacheTimeout.get(this, root).getMillis();
				}
			}
		}
		return applicationPathTimeout;
	}

	public ConfigServerImpl resetApplicationPathCacheTimeout() {
		if (applicationPathTimeout != null) {
			synchronized (SystemUtil.createToken("config", "getApplicationPathCacheTimeout")) {
				if (applicationPathTimeout != null) {
					applicationPathTimeout = null;
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
	public void clearApplicationCache() {
		if (applicationPathCache == null) return;
		applicationPathCache.clear();
	}

	@Override
	public ImportDefintion getComponentDefaultImport() {
		if (componentAutoImport == null) {
			synchronized (SystemUtil.createToken("config", "getComponentDefaultImport")) {
				if (componentAutoImport == null) {
					componentAutoImport = ImportDefintionImpl.getInstance(metaComponentDefaultImport.get(this, root), DEFAULT_IMPORT_DEFINITION);
				}
			}
		}
		return componentAutoImport;
	}

	public ConfigServerImpl resetComponentDefaultImport() {
		if (componentAutoImport != null) {
			synchronized (SystemUtil.createToken("config", "getComponentDefaultImport")) {
				if (componentAutoImport != null) {
					this.componentAutoImport = null;
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
		return componentLocalSearch.get(this, root);
	}

	public ConfigServerImpl resetComponentLocalSearch() {
		componentLocalSearch.reset();
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
			synchronized (SystemUtil.createToken("config", "")) {
				if (clientStorage == null) {
					clientStorage = validateStorage(metaClientStorage.get(this, root));
				}
			}
		}
		return clientStorage;
	}

	public ConfigServerImpl resetClientStorage() {
		if (clientStorage != null) {
			synchronized (SystemUtil.createToken("config", "")) {
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
			synchronized (SystemUtil.createToken("config", "getSessionStorage")) {
				if (sessionStorage == null) {
					sessionStorage = validateStorage(metaSessionStorage.get(this, root));
				}
			}
		}
		return sessionStorage;
	}

	public ConfigServerImpl resetSessionStorage() {
		if (sessionStorage != null) {
			synchronized (SystemUtil.createToken("config", "getSessionStorage")) {
				if (sessionStorage != null) {
					sessionStorage = null;
				}
			}
		}
		return this;
	}

	// TODO make this a PropFactory
	private String validateStorage(String storage) {
		storage = storage.trim().toLowerCase();

		// empty
		if (StringUtil.isEmpty(storage, true)) return "";

		// standard storages
		if ("cookie".equals(storage) || "memory".equals(storage) || "file".equals(storage)) return storage;

		// aliases
		if ("ram".equals(storage)) return "memory";
		if ("registry".equals(storage)) return "file";

		// datasource
		DataSource ds = getDataSource(storage, null);
		if (ds != null) {
			if (ds.isStorage()) return storage;
			throw new PageRuntimeException(new ApplicationException("datasource [" + storage + "] is not enabled to be used as session/client storage"));
		}

		// cache
		CacheConnection cc = getCacheConnections().get(storage);
		if (cc != null) {
			if (cc.isStorage()) return storage;
			throw new PageRuntimeException(new ApplicationException("cache [" + storage + "] is not enabled to be used as session/client storage"));
		}

		String sdx = StringUtil.soundex(storage);

		// check if a datasource has a similar name
		DataSource[] sources = getDataSources();
		for (int i = 0; i < sources.length; i++) {
			if (StringUtil.soundex(sources[i].getName()).equals(sdx))
				throw new PageRuntimeException(new ApplicationException("no matching storage for [" + storage + "] found, did you mean [" + sources[i].getName() + "]"));
		}

		// check if a cache has a similar name
		Iterator<String> it = getCacheConnections().keySet().iterator();
		String name;
		while (it.hasNext()) {
			name = it.next();
			if (StringUtil.soundex(name).equals(sdx))
				throw new PageRuntimeException(new ApplicationException("no matching storage for [" + storage + "] found, did you mean [" + name + "]"));
		}
		throw new PageRuntimeException(new ApplicationException("no matching storage for [" + storage + "] found"));
	}

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

	@Override
	public DebugEntry[] getDebugEntries() {
		if (debugTemplates == null) {
			synchronized (SystemUtil.createToken("config", "getDebugEntries")) {
				if (debugTemplates == null) {
					List<DebugEntry> list = metaDebugTemplates.list(this, root);
					// remove duplicates
					Map<String, DebugEntry> map = new HashMap<String, DebugEntry>();
					for (DebugEntry de: list) {
						map.put(de.getId(), de);
					}
					debugTemplates = map.values().toArray(new DebugEntry[map.size()]);
				}
			}
		}
		return debugTemplates;
	}

	public ConfigServerImpl resetDebugEntries() {
		if (debugTemplates != null) {
			synchronized (SystemUtil.createToken("config", "getDebugEntries")) {
				if (debugTemplates != null) {
					debugTemplates = null;
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
		return debuggingMaxRecordsLogged.get(this, root);
	}

	public ConfigServerImpl resetDebugMaxRecordsLogged() {
		debuggingMaxRecordsLogged.reset();
		return this;
	}

	@Override
	public boolean getDotNotationUpperCase() {
		if (preserveCase == null) {
			synchronized (SystemUtil.createToken("config", "getDotNotationUpperCase")) {
				if (preserveCase == null) {
					preserveCase = metaPreserveCase.get(this, root);
				}
			}
		}
		return !preserveCase;// invert: lucee.preserve.case=true means dotNotationUpperCase=false
	}

	public ConfigServerImpl resetDotNotationUpperCase() {
		if (preserveCase != null) {
			synchronized (SystemUtil.createToken("config", "getDotNotationUpperCase")) {
				if (preserveCase != null) {
					preserveCase = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean preserveCase() {
		return !getDotNotationUpperCase();
	}

	@Override
	public boolean getDefaultFunctionOutput() {
		return defaultFunctionOutput.get(this, root);
	}

	public ConfigServerImpl restDefaultFunctionOutput() {
		defaultFunctionOutput.reset();
		return this;
	}

	@Override
	public boolean getSuppressWSBeforeArg() {
		return suppressWhitespaceBeforeArgument.get(this, root);
	}

	public ConfigServerImpl resetSuppressWSBeforeArg() {
		suppressWhitespaceBeforeArgument.reset();
		return this;
	}

	@Override
	public RestSettings getRestSetting() {
		if (restSetting == null) {
			synchronized (SystemUtil.createToken("config", "restSkipCFCWithError")) {
				if (restSetting == null) {
					restSetting = new RestSettingImpl(getRestSkipCFCWithError(), getRestReturnFormat());
				}
			}
		}
		return restSetting;
	}

	public ConfigServerImpl resetRestSetting() {
		if (restSetting != null) {
			synchronized (SystemUtil.createToken("config", "getSuppressWSBeforeArg")) {
				if (restSetting != null) {
					resetRestSkipCFCWithError();
					resetRestReturnFormat();
					restSetting = null;
				}
			}
		}
		return this;
	}

	public boolean getRestSkipCFCWithError() {
		return restSkipCFCWithError.get(this, root);
	}

	public ConfigServerImpl resetRestSkipCFCWithError() {
		restSkipCFCWithError.reset();
		return this;
	}

	public int getRestReturnFormat() {
		return restReturnFormat.get(this, root);
	}

	public ConfigServerImpl resetRestReturnFormat() {
		restReturnFormat.reset();
		return this;
	}

	public int getMode() {
		if (mode == null) {
			synchronized (SystemUtil.createToken("config", "getMode")) {
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

	public ConfigServerImpl resetMode() {
		if (mode != null) {
			synchronized (SystemUtil.createToken("config", "getMode")) {
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
		return cfmlWriter.get(this, root);
	}

	public ConfigServerImpl resetCFMLWriterType() {
		cfmlWriter.reset();
		return this;
	}

	@Override
	public boolean getBufferOutput() {
		return bufferTagBodyOutput.get(this, root);
	}

	public ConfigServerImpl resetBufferOutput() {
		bufferTagBodyOutput.reset();
		return this;
	}

	public boolean getDebuggingDatabase() {
		return debuggingDatabase.get(this, root);
	}

	public ConfigServerImpl resetDebuggingDatabase() {
		debuggingDatabase.reset();
		return this;
	}

	public boolean getDebuggingException() {
		return debuggingException.get(this, root);
	}

	public ConfigServerImpl resetDebuggingException() {
		debuggingException.reset();
		return this;
	}

	public boolean getDebuggingTemplate() {
		return debuggingTemplate.get(this, root);
	}

	public ConfigServerImpl resetDebuggingTemplate() {
		debuggingTemplate.reset();
		return this;
	}

	public boolean getDebuggingDump() {
		return debuggingDump.get(this, root);
	}

	public ConfigServerImpl resetDebuggingDump() {
		debuggingDump.reset();
		return this;
	}

	public boolean getDebuggingTracing() {
		return debuggingTracing.get(this, root);
	}

	public ConfigServerImpl resetDebuggingTracing() {
		debuggingTracing.reset();
		return this;
	}

	public boolean getDebuggingTimer() {
		return debuggingTimer.get(this, root);
	}

	public ConfigServerImpl resetDebuggingTimer() {
		debuggingTimer.reset();
		return this;
	}

	public boolean getDebuggingImplicitAccess() {
		return debuggingImplicitAccess.get(this, root);
	}

	public ConfigServerImpl resetDebuggingImplicitAccess() {
		debuggingImplicitAccess.reset();
		return this;
	}

	public boolean getDebuggingQueryUsage() {
		return debuggingQueryUsage.get(this, root);
	}

	public ConfigServerImpl resetDebuggingQueryUsage() {
		debuggingQueryUsage.reset();
		return this;
	}

	public boolean getDebuggingThread() {
		return debuggingThread.get(this, root);
	}

	public ConfigServerImpl resetDebuggingThread() {
		debuggingThread.reset();
		return this;
	}

	public int getDebugOptions() {
		int options = 0;
		if (getDebuggingDatabase()) options += ConfigPro.DEBUG_DATABASE;
		if (getDebuggingException()) options += ConfigPro.DEBUG_EXCEPTION;
		if (getDebuggingTemplate()) options += ConfigPro.DEBUG_TEMPLATE;
		if (getDebuggingDump()) options += ConfigPro.DEBUG_DUMP;
		if (getDebuggingTracing()) options += ConfigPro.DEBUG_TRACING;
		if (getDebuggingTimer()) options += ConfigPro.DEBUG_TIMER;
		if (getDebuggingImplicitAccess()) options += ConfigPro.DEBUG_IMPLICIT_ACCESS;
		if (getDebuggingQueryUsage()) options += ConfigPro.DEBUG_QUERY_USAGE;
		if (getDebuggingThread()) options += ConfigPro.DEBUG_THREAD;
		return options;
	}

	public ConfigServerImpl resetDebugOptions() {
		resetDebuggingDatabase();
		resetDebuggingException();
		resetDebuggingTemplate();
		resetDebuggingDump();
		resetDebuggingTracing();
		resetDebuggingTimer();
		resetDebuggingImplicitAccess();
		resetDebuggingQueryUsage();
		resetDebuggingThread();
		return this;
	}
	// = 0

	@Override
	public boolean hasDebugOptions(int debugOption) {
		return (getDebugOptions() & debugOption) > 0;
	}

	@Override
	public boolean checkForChangesInConfigFile() {
		return checkForChanges.get(this, root);
	}

	public ConfigServerImpl resetCheckForChangesInConfigFile() {
		checkForChanges.reset();
		return this;
	}

	@Override
	public int getExternalizeStringGTE() {
		return externalizeStringGTE.get(this, root);
	}

	public ConfigServerImpl resetExternalizeStringGTE() {
		externalizeStringGTE.reset();
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
		synchronized (SystemUtil.createToken("config", "loggers")) {
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

			if (insideLoggers.get()) {
				return new HashMap<String, LoggerAndSourceData>(); // avoid cycle loop
			}

			synchronized (SystemUtil.createToken("config", "loggers")) {
				if (loggers == null) {
					if (root == null || insideLoggers.get()) {
						return new HashMap<String, LoggerAndSourceData>(); // avoid cycle loop
					}
					insideLoggers.set(true);
					try {
						loggers = metaLoggers.map(this, root);
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
			synchronized (SystemUtil.createToken("config", "loggers")) {
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

			ClassDefinition appender = getLogEngine().appenderClassDefintion("console");
			ClassDefinition layout = getLogEngine().layoutClassDefintion("pattern");
			las = LogFactory.createLogger(this, name, Log.LEVEL_ERROR, appender, null, layout, null, true, true, Prop.SOURCE_INTERNAL);

			String id = LoggerAndSourceData.id(name.toLowerCase(), appender, null, layout, null, Log.LEVEL_ERROR, true);
			LoggerAndSourceData existing = loggers != null ? loggers.get(name.toLowerCase()) : null;
			if (existing != null) {
				if (existing.id().equals(id)) {
					return existing;
				}
				existing.close();
			}

		}
		return las.init();
	}

	@Override
	public Map<Key, Map<Key, Object>> getTagDefaultAttributeValues() {
		return null;
		// return tagDefaultAttributeValues == null ? null :
		// Duplicator.duplicateMap(tagDefaultAttributeValues, new ConcurrentHashMap<Key, Map<Key,
		// Object>>(), true);
	}

	@Override
	public Boolean getHandleUnQuotedAttrValueAsString() {
		return handleUnquotedAttributeValueAsString.get(this, root);
	}

	public ConfigServerImpl resetHandleUnQuotedAttrValueAsString() {
		handleUnquotedAttributeValueAsString.reset();
		return this;
	}

	public String getCachedWithinFile() {
		return cachedWithinFile.get(this, root);
	}

	public ConfigServerImpl resetCachedWithinFile() {
		cachedWithinFile.reset();
		return this;
	}

	public String getCachedWithinFunction() {
		return cachedWithinFunction.get(this, root);
	}

	public ConfigServerImpl resetCachedWithinFunction() {
		cachedWithinFunction.reset();
		return this;
	}

	public String getCachedWithinHTTP() {
		return cachedWithinHTTP.get(this, root);
	}

	public ConfigServerImpl resetCachedWithinHTTP() {
		cachedWithinHTTP.reset();
		return this;
	}

	public String getCachedWithinInclude() {
		return cachedWithinInclude.get(this, root);
	}

	public ConfigServerImpl resetCachedWithinInclude() {
		cachedWithinInclude.reset();
		return this;
	}

	public String getCachedWithinQuery() {
		return cachedWithinQuery.get(this, root);
	}

	public ConfigServerImpl resetCachedWithinQuery() {
		cachedWithinQuery.reset();
		return this;
	}

	public String getCachedWithinResource() {
		return cachedWithinResource.get(this, root);
	}

	public ConfigServerImpl resetCachedWithinResource() {
		cachedWithinResource.reset();
		return this;
	}

	public String getCachedWithinWebservice() {
		return cachedWithinWebservice.get(this, root);
	}

	public ConfigServerImpl resetCachedWithinWebservice() {
		cachedWithinWebservice.reset();
		return this;
	}

	@Override
	public Object getCachedWithin(int type) {

		switch (type) {
		case Config.CACHEDWITHIN_FUNCTION:
			return getCachedWithinFunction();
		case Config.CACHEDWITHIN_INCLUDE:
			return getCachedWithinInclude();
		case Config.CACHEDWITHIN_QUERY:
			return getCachedWithinQuery();
		case Config.CACHEDWITHIN_RESOURCE:
			return getCachedWithinResource();
		case Config.CACHEDWITHIN_HTTP:
			return getCachedWithinHTTP();
		case Config.CACHEDWITHIN_FILE:
			return getCachedWithinFile();
		case Config.CACHEDWITHIN_WEBSERVICE:
			return getCachedWithinWebservice();
		}
		return null;
	}

	public ConfigServerImpl resetCachedWithin() {
		resetCachedWithinFile();
		resetCachedWithinFunction();
		resetCachedWithinHTTP();
		resetCachedWithinInclude();
		resetCachedWithinQuery();
		resetCachedWithinResource();
		resetCachedWithinWebservice();
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
		if (salt == null) {
			synchronized (SystemUtil.createToken("config", "getSalt")) {
				if (salt == null) {
					this.salt = metaSalt.get(this, root);
					if (StringUtil.isEmpty(this.salt, true)) {
						throw new RuntimeException("context is invalid, there is no salt!");
					}

				}
			}
		}
		return salt;
	}

	public ConfigServerImpl resetSalt() {
		if (salt != null) {
			synchronized (SystemUtil.createToken("config", "getSalt")) {
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
		if (jdbcDrivers == null) {
			synchronized (SystemUtil.createToken("config", "getJDBCDrivers")) {
				if (jdbcDrivers == null) {
					Map<String, JDBCDriver> map = metaJdbcDrivers.map(this, root);
					jdbcDrivers = map.values().toArray(new JDBCDriver[map.size()]);
				}
			}
		}
		return jdbcDrivers;
	}

	public ConfigServerImpl resetJDBCDrivers() {
		if (jdbcDrivers != null) {
			synchronized (SystemUtil.createToken("config", "getJDBCDrivers")) {
				if (jdbcDrivers != null) {
					jdbcDrivers = null;
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
		return requestQueueMax.get(this, root);
	}

	public ConfigServerImpl resetQueueMax() {
		requestQueueMax.reset();
		return this;
	}

	@Override
	public long getQueueTimeout() {
		return requestQueueTimeout.get(this, root);
	}

	public ConfigServerImpl resetQueueTimeout() {
		requestQueueTimeout.reset();
		return this;
	}

	@Override
	public boolean getQueueEnable() {
		return requestQueueEnable.get(this, root);
	}

	public ConfigServerImpl resetQueueEnable() {
		requestQueueEnable.reset();
		return this;
	}

	@Override
	public boolean getCGIScopeReadonly() {
		return cgiScopeReadonly.get(this, root);
	}

	public ConfigServerImpl resetCGIScopeReadonly() {
		cgiScopeReadonly.reset();
		return this;
	}

	@Override
	public Resource getDeployDirectory() {
		if (deployDir == null) {

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
		return deployDir;
	}

	@Override
	@Deprecated
	public Map<String, ClassDefinition> getCacheDefinitions() {
		if (cacheClasses == null) {
			synchronized (SystemUtil.createToken("config", "getCacheDefinitions")) {
				if (cacheClasses == null) {
					List<ClassDefinition> list = metacCacheDefinitions.list(this, root);
					Map<String, ClassDefinition> map = new HashMap<String, ClassDefinition>();
					for (ClassDefinition cd: list) {
						map.put(cd.getClassName(), cd);
					}
					cacheClasses = map;
				}
			}
		}
		return cacheClasses;
	}

	public ConfigServerImpl resetCacheDefinitions() {
		if (cacheClasses != null) {
			synchronized (SystemUtil.createToken("config", "getCacheDefinitions")) {
				if (cacheClasses != null) {
					cacheClasses = null;
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
			synchronized (SystemUtil.createToken("config", "getAntiSamyPolicy")) {
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
		if (gateways == null) {
			synchronized (SystemUtil.createToken("config", "getGatewayEntries")) {
				if (gateways == null) {
					gateways = (GatewayMap) metaGatewayEntries.map(this, root, new GatewayMap());
				}
			}
		}
		return gateways;
	}

	public ConfigServerImpl resetGatewayEntries() {
		if (gateways != null) {
			synchronized (SystemUtil.createToken("config", "getGatewayEntries")) {
				if (gateways != null) {
					gateways = null;
				}
			}
		}
		return this;
	}

	protected ClassDefinition getWSHandlerClassDefinition() {
		return wsHandlerCD.get(this, root);
	}

	protected ConfigServerImpl resetWSHandlerClassDefinition() {
		wsHandlerCD.reset();
		return this;
	}

	boolean isEmpty(ClassDefinition cd) {
		return cd == null || StringUtil.isEmpty(cd.getClassName());
	}

	@Override
	public final boolean getFullNullSupport() {
		return nullSupport.get(this, root);
	}

	public final ConfigServerImpl resetFullNullSupport() {
		nullSupport.reset();
		return this;
	}

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
		if (initCachedAfter) {
			synchronized (SystemUtil.createToken("config", "getCachedAfterTimeRange")) {
				if (initCachedAfter) {

					TimeSpan ts = metaCachedAfterTimeRange.get(this, root);
					if (ts != null && ts.getMillis() > 0) cachedAfter = ts;
					initCachedAfter = false;
				}
			}
		}
		return this.cachedAfter;
	}

	public ConfigServerImpl resetCachedAfterTimeRange() {
		if (!initCachedAfter) {
			synchronized (SystemUtil.createToken("config", "getCachedAfterTimeRange")) {
				if (!initCachedAfter) {
					cachedAfter = null;
					initCachedAfter = true;
				}
			}
		}
		return this;
	}

	@Override
	public Map<String, Startup> getStartups() {
		if (startupHooks == null) {
			synchronized (SystemUtil.createToken("config", "getStartups")) {
				if (startupHooks == null) {

					List<Startup> list = metaStartups.list(this, root);
					Map<String, Startup> map = new ConcurrentHashMap<>(list.size());
					for (Startup startup: list) {
						map.put(startup.cd.getClassName(), startup);
					}
					startupHooks = map;
				}
			}
		}
		return startupHooks;
	}

	public ConfigServerImpl resetStartups() {
		if (startupHooks != null) {
			synchronized (SystemUtil.createToken("config", "getStartups")) {
				if (startupHooks != null) {
					// Call finalize() on existing startup hook instances before clearing
					for (Startup startup: startupHooks.values()) {
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
					startupHooks = null;
				}
			}
		}
		return this;
	}

	@Override
	public Regex getRegex() {
		return regexType.get(this, root);
	}

	public ConfigServerImpl resetRegex() {
		regexType.reset();
		return this;
	}

	@Override
	public boolean getPreciseMath() {
		return preciseMath.get(this, root);
	}

	public ConfigServerImpl resetPreciseMath() {
		preciseMath.reset();
		return this;
	}

	protected void setMainLogger(String mainLoggerName) {
		if (!StringUtil.isEmpty(mainLoggerName, true)) this.mainLogger = mainLoggerName.trim();
	}

	@Override
	public String getMainLogger() {
		if (mainLogger == null) {
			synchronized (SystemUtil.createToken("config", "getMainLogger")) {
				if (mainLogger == null) {
					mainLogger = metaMainLoggerName.get(this, root);
				}

			}
		}

		return this.mainLogger;
	}

	public ConfigServerImpl resetMainLogger() {
		if (mainLogger != null) {
			synchronized (SystemUtil.createToken("config", "getMainLogger")) {
				if (mainLogger != null) {
					mainLogger = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getFormUrlAsStruct() {
		return formUrlAsStruct.get(this, root);
	}

	// = true
	public ConfigServerImpl resetFormUrlAsStruct() {
		formUrlAsStruct.reset();
		return this;
	}

	@Override
	public int getReturnFormat() {
		return returnFormat.get(this, root);
	}

	public ConfigServerImpl resetReturnFormat() {
		returnFormat.reset();
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
			synchronized (SystemUtil.createToken("extensions", "javaSettings")) {
				if (javaSettings == null) {

					Resource lib = getLibraryDirectory();
					Resource[] libs = lib.listResources(ExtensionResourceFilter.EXTENSION_JAR_NO_DIR);

					Struct javasettings = metaJavaSettings.get(this, root);

					javaSettings = JavaSettingsImpl.getInstance(this, javasettings, libs);
					if (javaSettings == null) javaSettings = JavaSettingsImpl.getInstance(this, new StructImpl(), null);
				}
			}
		}
		return javaSettings;
	}

	public ConfigServerImpl resetJavaSettings() {
		if (javaSettings != null) {
			synchronized (SystemUtil.createToken("extensions", "javaSettings")) {
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

	public ConfigServerImpl resetExtensionInstalledDir() {
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

	public ConfigServerImpl resetExtensionAvailableDir() {
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
		clearApplicationCache();
		clearLoggers(null);
		clearComponentMetadata();
		baseComponentPageSource = null;
		getThreadQueue().clear();
	}

	public void resetAll() throws Exception {
		resetAll(null);
	}

	public void resetAll(ResetFilter filter) throws Exception {

		List<Method> methods = Reflector.getMethods(this.getClass());
		if (filter == null) {
			LogUtil.log(Log.LEVEL_DEBUG, "config", "reset all");

			for (Method method: methods) {
				if (!method.getName().startsWith("reset") || method.getName().equals("reset") || method.getName().equals("resetAll") || method.getArgumentCount() != 0) continue;
				method.invoke(this);
			}
		}
		else {
			ExceptionUtil.initCauseEL(null, null);
			LogUtil.log(Log.LEVEL_DEBUG, "config", "reset the following: " + filter);

			for (Method method: methods) {
				if (method.getArgumentCount() == 0 && filter.allow(method.getName())) {
					method.invoke(this);
				}
			}
		}
	}

	public void touchAll(ResetFilter filter) throws Exception {
		List<Method> methods = Reflector.getMethods(this.getClass());

		Set<String> ignores = new HashSet<>();
		ignores.add("getDebugTemplate");
		ignores.add("getExtensionProviders");
		ignores.add("getExtensions");
		ignores.add("getConfigListener");
		ignores.add("getAdminSyncClass");

		if (filter == null) {
			for (Method method: methods) {
				if (!method.getName().startsWith("get") || ignores.contains(method.getName()) || method.getArgumentCount() != 0) continue;
				method.invoke(this);
			}
		}
		else {
			for (Method method: methods) {
				if (!ignores.contains(method.getName()) && method.getArgumentCount() == 0 && filter.allow(method.getName())) method.invoke(this);
			}
		}
	}

	FunctionLib getCoreFLDs() throws FunctionLibException {
		if (coreFLDs == null) {
			synchronized (SystemUtil.createToken("config", "getCoreFLDs")) {
				if (coreFLDs == null) {
					this.coreFLDs = FunctionLibFactory.loadFromSystem(id);
				}
			}
		}
		return coreFLDs;
	}

	TagLib getCoreTLDs() throws TagLibException {
		if (coreTLDs == null) {
			synchronized (SystemUtil.createToken("config", "getCoreTLDs")) {
				if (coreTLDs == null) {
					this.coreTLDs = TagLibFactory.loadFromSystem(id);
				}
			}
		}
		return coreTLDs;
	}

	public void update() throws IOException, ConverterException {
		ConfigFile.write(getConfigFile(), root, null);
		setConfigLastModified();
	}

	public void load(Resource configFile) throws IOException, PageException {

		// we have an update
		if (this.loadTime != 0) {
			try {
				resetAll();
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}

		try {
			root = ConfigFile.read(configFile, null);
		}
		catch (Exception e) {
			// rename buggy config files
			if (configFile.exists()) {
				Resource bugFile;
				int count = 1;
				Resource configDir = configFile.getParentResource();
				while ((bugFile = configDir.getRealResource("corrupt-" + (count++) + "-" + ConfigFactory.CONFIG_FILE_NAMES[0] + ".")).exists()) {}

				LogUtil.log(Log.LEVEL_ERROR, ConfigFactory.class.getName(),
						"The configuration file [" + configFile
								+ "] contained syntax errors and could not be read. A new configuration file has been created, and the invalid file has been renamed to [" + bugFile
								+ "].");
				LogUtil.log(ThreadLocalPageContext.get(), ConfigFactory.class.getName(), e);

				IOUtil.copy(configFile, bugFile);
				configFile.delete();
			}
			ConfigFile.createConfigFile(configFile);
			root = ConfigFile.read(configFile, null);
		}
		this.setConfigLastModified();
		this.loadTime = System.currentTimeMillis();

		createSaltAndPW(root);
	}

	public void createSaltAndPW() throws IOException {
		createSaltAndPW(root);
	}

	private void createSaltAndPW(Struct root) throws IOException {
		if (root == null) return;

		boolean update = false;
		String salt = metaSalt.get(this, root);
		// not existing?
		if (StringUtil.isEmpty(salt, true) || !Decision.isUUId(salt)) {
			// create salt
			root.setEL("salt", salt = CreateUUID.invoke());
			update = true;
		}

		Password pw = metaPassword.get(this, root);

		// no password yet
		if (pw == null) {
			Resource pwFile = getConfigDir().getRealResource("password.txt");
			if (pwFile.isFile()) {
				try {
					String strPW = IOUtil.toString(pwFile, (Charset) null);
					if (!StringUtil.isEmpty(strPW, true)) {
						PasswordImpl.writeToStruct(root, salt, strPW.trim());
						pwFile.delete();
						update = true;
					}
				}
				catch (IOException e) {
					LogUtil.logGlobal(this, "application", e);
				}
			}
			else {
				LogUtil.log(this, Log.LEVEL_DEBUG, "application", "no password set and no password file found at [" + pwFile + "]");
			}
		}

		resetSalt().resetPassword();
		if (update) {
			try {
				ConfigFile.write(getConfigFile(), root, null);
			}
			catch (ConverterException e) {
				throw ExceptionUtil.toIOException(e);
			}
		}
	}

	public UpdateInfo getUpdateInfo() {
		return updateInfo;
	}

	@Override
	public ConfigListener getConfigListener() {
		throw new RuntimeException("no longer supported");
	}

	@Override
	public void setConfigListener(ConfigListener configListener) {

	}

	@Override
	public ConfigServer getConfigServer(String password) {
		return this;
	}

	@Override
	public ConfigServer getConfigServer(String key, long timeNonce) {
		return this;
	}

	@Override
	public ConfigWeb[] getConfigWebs() {

		Iterator<String> it = initContextes.keySet().iterator();
		ConfigWeb[] webs = new ConfigWeb[initContextes.size()];
		int index = 0;
		while (it.hasNext()) {
			webs[index++] = ((CFMLFactoryImpl) initContextes.get(it.next())).getConfig();
		}
		return webs;
	}

	@Override
	public ConfigWeb getConfigWeb(String realpath) {
		return getConfigWebPro(realpath);
	}

	/**
	 * returns CongigWeb Implementtion
	 * 
	 * @param realpath
	 * @return ConfigWebPro
	 */
	protected ConfigWebPro getConfigWebPro(String realpath) {
		Iterator<String> it = initContextes.keySet().iterator();
		while (it.hasNext()) {
			ConfigWeb cw = ((CFMLFactoryImpl) initContextes.get(it.next())).getConfig();
			if (ReqRspUtil.getRootPath(cw.getServletContext()).equals(realpath)) return (ConfigWebPro) cw;
		}
		return null;
	}

	public ConfigWeb getConfigWebById(String id) {
		Iterator<String> it = initContextes.keySet().iterator();

		while (it.hasNext()) {
			ConfigWeb cw = ((CFMLFactoryImpl) initContextes.get(it.next())).getConfig();
			if (cw.getIdentification().getId().equals(id)) return cw;
		}
		return null;
	}

	/**
	 * @return JspFactoryImpl array
	 */
	public CFMLFactoryImpl[] getJSPFactories() {
		Iterator<String> it = initContextes.keySet().iterator();
		CFMLFactoryImpl[] factories = new CFMLFactoryImpl[initContextes.size()];
		int index = 0;
		while (it.hasNext()) {
			factories[index++] = (CFMLFactoryImpl) initContextes.get(it.next());
		}
		return factories;
	}

	@Override
	public Map<String, CFMLFactory> getJSPFactoriesAsMap() {
		return initContextes;
	}

	@Override
	public SecurityManager getSecurityManager(String id) {
		Object o = managers.get(id);
		if (o != null) return (SecurityManager) o;
		return getDefaultSecurityManager().cloneSecurityManager();
	}

	@Override
	public boolean hasIndividualSecurityManager(String id) {
		return managers.containsKey(id);
	}

	/**
	 * @param id
	 * @param securityManager
	 */
	protected void setSecurityManager(String id, SecurityManager securityManager) {
		managers.put(id, securityManager);
	}

	/**
	 * @param id
	 */
	protected void removeSecurityManager(String id) {
		managers.remove(id);
	}

	@Override
	public SecurityManager getDefaultSecurityManager() {
		if (defaultSecurityManager == null) {
			synchronized (SystemUtil.createToken("config", "getDefaultSecurityManager")) {
				if (defaultSecurityManager == null) {
					Struct security = ConfigUtil.getAsStruct("security", root);
					if (security != null) {
						defaultSecurityManager = ConfigFactoryImpl._toSecurityManagerSingle(this, security);
					}
					else defaultSecurityManager = SecurityManagerImpl.getOpenSecurityManager();
				}
			}
		}

		return defaultSecurityManager;
	}

	public ConfigServerImpl resetDefaultSecurityManager() {
		if (defaultSecurityManager != null) {
			synchronized (SystemUtil.createToken("config", "getDefaultSecurityManager")) {
				if (defaultSecurityManager != null) {
					defaultSecurityManager = null;
					securityManagers.clear();
				}
			}
		}

		return this;
	}

	@Override
	public CFMLEngine getCFMLEngine() {
		return getEngine();
	}

	@Override
	public CFMLEngine getEngine() {
		return engine;
	}

	/**
	 * @return Returns the rootDir.
	 */
	@Override
	public Resource getRootDirectory() {
		return rootDir;
	}

	@Override
	public void setUpdateType(String updateType) {
		throw new RuntimeException("this action is no longer allowed");
	}

	@Override
	public void setUpdateLocation(URL updateLocation) {
		throw new RuntimeException("this action is no longer allowed");
	}

	@Override
	public void setUpdateLocation(String strUpdateLocation) throws MalformedURLException {
		throw new RuntimeException("this action is no longer allowed");
	}

	@Override
	public void setUpdateLocation(String strUpdateLocation, URL defaultValue) {
		throw new RuntimeException("this action is no longer allowed");
	}

	@Override
	public SecurityManager getSecurityManager() {
		SecurityManagerImpl sm = (SecurityManagerImpl) getDefaultSecurityManager();// .cloneSecurityManager();
		// sm.setAccess(SecurityManager.TYPE_ACCESS_READ,SecurityManager.ACCESS_PROTECTED);
		// sm.setAccess(SecurityManager.TYPE_ACCESS_WRITE,SecurityManager.ACCESS_PROTECTED);
		return sm;
	}

	public SecurityManager getSecurityManager(Resource rootDir) {
		SecurityManager sm = securityManagers.get(rootDir.getAbsolutePath());
		if (sm == null) {
			synchronized (SystemUtil.createToken("config", "getSecurityManager")) {
				sm = securityManagers.get(rootDir.getAbsolutePath());
				if (sm == null) {
					SecurityManagerImpl dsm = (SecurityManagerImpl) getDefaultSecurityManager();
					sm = dsm.duplicate().setRootDirectory(rootDir);
					securityManagers.put(rootDir.getAbsolutePath(), sm);
				}
			}
		}
		return sm;
	}

	private ThreadQueue threadQueue = new ThreadQueueImpl(ThreadQueue.MODE_BLOCKING, null); // before the queue is loaded we block all requests

	private AIEnginePool aiEnginePool;

	private String _id;

	public ThreadQueue setThreadQueue(ThreadQueue threadQueue) {
		return this.threadQueue = threadQueue;
	}

	@Override
	public ThreadQueue getThreadQueue() {
		return threadQueue;
	}

	@Override
	public Resource getSecurityDirectory() {
		Resource cacerts = null;
		String trustStore = SystemUtil.getPropertyEL("javax.net.ssl.trustStore");/* JAVJAK */
		if (trustStore != null) {
			cacerts = ResourcesImpl.getFileResourceProvider().getResource(trustStore);
		}

		// security/cacerts
		if (cacerts == null || !cacerts.exists()) {
			cacerts = getConfigDir().getRealResource("security/cacerts");
			if (!cacerts.exists()) cacerts.mkdirs();
		}
		return cacerts;
	}

	@Override
	public void checkPermGenSpace(boolean check) {

	}

	@Override
	public Cluster createClusterScope() throws PageException {
		Cluster cluster = null;
		try {
			if (Reflector.isInstaneOf(getClusterClass(), Cluster.class, false)) {
				cluster = (Cluster) ClassUtil.loadInstance(getClusterClass(), ArrayUtil.OBJECT_EMPTY);
				cluster.init(this);
			}
			else if (Reflector.isInstaneOf(getClusterClass(), ClusterRemote.class, false)) {
				ClusterRemote cb = (ClusterRemote) ClassUtil.loadInstance(getClusterClass(), ArrayUtil.OBJECT_EMPTY);

				cluster = new ClusterWrap(this, cb);
				// cluster.init(cs);
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		return cluster;
	}

	@Override
	public boolean hasServerPassword() {
		return hasPassword();
	}

	public String[] getInstalledPatches() throws PageException {
		CFMLEngineFactory factory = getEngine().getCFMLEngineFactory();

		try {
			return factory.getInstalledPatches();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			try {
				return getInstalledPatchesOld(factory);
			}
			catch (Exception e1) {
				throw Caster.toPageException(e1);
			}
		}
	}

	private String[] getInstalledPatchesOld(CFMLEngineFactory factory) throws IOException {
		File patchDir = new File(factory.getResourceRoot(), "patches");
		if (!patchDir.exists()) patchDir.mkdirs();

		File[] patches = patchDir.listFiles(new ExtensionFilter(new String[] { "." + getCoreExtension() }));

		List<String> list = new ArrayList<String>();
		String name;
		int extLen = getCoreExtension().length() + 1;
		for (int i = 0; i < patches.length; i++) {
			name = patches[i].getName();
			name = name.substring(0, name.length() - extLen);
			list.add(name);
		}
		String[] arr = list.toArray(new String[list.size()]);
		Arrays.sort(arr);
		return arr;
	}

	private String getCoreExtension() {
		return "lco";
	}

	@Override
	public boolean allowRequestTimeout() {
		return engine.allowRequestTimeout();
	}

	public ConfigServer getConfigServer(String key, String nonce) {
		return this;
	}

	public void checkAccess(Password password) throws ExpressionException {
		if (hasPassword() && !passwordEqual(password)) throw new ExpressionException("No access, password is invalid");
	}

	public void checkAccess(String key, long timeNonce) throws PageException {

		if (previousNonces.containsKey(timeNonce)) {
			long now = System.currentTimeMillis();
			long diff = timeNonce > now ? timeNonce - now : now - timeNonce;
			if (diff > 10) throw new ApplicationException("nonce was already used, same nonce can only be used once");

		}
		long now = System.currentTimeMillis();
		if (timeNonce > (now + FIVE_SECONDS) || timeNonce < (now - FIVE_SECONDS)) throw new ApplicationException("nonce is outdated");
		previousNonces.put(timeNonce, "");

		String[] keys = getAuthenticationKeys();
		// check if one of the keys matching
		String hash;
		for (int i = 0; i < keys.length; i++) {
			try {
				hash = Hash.hash(keys[i], Caster.toString(timeNonce), Hash.ALGORITHM_SHA_256, Hash.ENCODING_HEX);
				if (hash.equals(key)) return;
			}
			catch (NoSuchAlgorithmException e) {
				throw Caster.toPageException(e);
			}
		}
		throw new ApplicationException("No access, no matching authentication key found");
	}

	@Override
	public IdentificationServer getIdentification() {
		if (id == null) {
			synchronized (SystemUtil.createToken("config", "id")) {
				if (id == null) {
					id = ConfigFactoryImpl.loadId(this, root, null, null);
					id.getId();
				}
			}
		}
		return id;
	}

	public void resetIdentification() {
		if (id != null) {
			synchronized (SystemUtil.createToken("config", "id")) {
				if (id != null) {
					id = null;
				}
			}
		}
	}

	@Override
	public Repository[] getMavenRepository() {
		if (mavenRepository == null) {
			synchronized (SystemUtil.createToken("config", "mavenRepository")) {
				if (mavenRepository == null) {
					List<Repository> tmp = metaMavenRepository.list(this, root);
					if (tmp != null && !tmp.isEmpty()) {
						mavenRepository = tmp.toArray(new Repository[tmp.size()]);
					}
					else {
						mavenRepository = MavenUpdateProvider.DEFAULT_REPOSITORIES_RELEASES;
					}
				}
			}
		}
		return mavenRepository;
	}

	public void resetMavenRepository() {
		if (mavenRepository != null) {
			synchronized (SystemUtil.createToken("config", "mavenRepository")) {
				if (mavenRepository != null) {
					mavenRepository = null;
				}
			}
		}
	}

	@Override
	public Repository[] getMavenSnapshotRepository() {
		if (mavenSnapshotRepository == null) {
			synchronized (SystemUtil.createToken("config", "mavenSnapshotRepository")) {
				if (mavenSnapshotRepository == null) {
					List<Repository> tmp = metaMavenSnapshotRepository.list(this, root);
					if (tmp != null && !tmp.isEmpty()) {
						mavenSnapshotRepository = tmp.toArray(new Repository[tmp.size()]);
					}
					else {
						mavenSnapshotRepository = MavenUpdateProvider.DEFAULT_REPOSITORIES_SNAPSHOTS;
					}
				}
			}
		}
		return mavenSnapshotRepository;
	}

	public void resetMavenSnapshotRepository() {
		if (mavenSnapshotRepository != null) {
			synchronized (SystemUtil.createToken("config", "mavenSnapshotRepository")) {
				if (mavenSnapshotRepository != null) {
					mavenSnapshotRepository = null;
				}
			}
		}
	}

	public int getMavenDownloadPolicyStartup() {
		return mavenDownloadPolicyStartup.get(this, root);
	}

	public void resetMavenDownloadPolicyStartup() {
		mavenDownloadPolicyStartup.reset();
	}

	public int getMavenDownloadPolicyRuntime() {
		return mavenDownloadPolicyRuntime.get(this, root);
	}

	public void resetMavenDownloadPolicyRuntime() {
		mavenDownloadPolicyRuntime.reset();
	}

	public int getMavenDownloadPolicyLogLevel() {
		return mavenDownloadPolicyLogLevel.get(this, root);
	}

	public void resetMavenDownloadPolicyLogLevel() {
		mavenDownloadPolicyLogLevel.reset();
	}

	@Override
	public Collection<BundleDefinition> getAllExtensionBundleDefintions() {
		Map<String, BundleDefinition> rtn = new HashMap<>();

		// server (this)
		Iterator<BundleDefinition> itt = getExtensionBundleDefintions().iterator();
		BundleDefinition bd;
		while (itt.hasNext()) {
			bd = itt.next();
			rtn.put(bd.getName() + "|" + bd.getVersionAsString(), bd);
		}

		// webs
		ConfigWeb[] cws = getConfigWebs();
		for (ConfigWeb cw: cws) {
			itt = ((ConfigPro) cw).getExtensionBundleDefintions().iterator();
			while (itt.hasNext()) {
				bd = itt.next();
				rtn.put(bd.getName() + "|" + bd.getVersionAsString(), bd);
			}
		}

		return rtn.values();
	}

	@Override
	public Collection<RHExtension> getAllRHExtensions() {
		List<RHExtension> rtn = new ArrayList<>();
		RHExtension[] arr = getRHExtensions();
		for (RHExtension rhe: arr) {
			rtn.add(rhe);
		}
		return rtn;
	}

	protected String getLibHash() {
		if (libHash == null) {
			synchronized (SystemUtil.createToken("config", "getLibHash")) {
				if (libHash == null) {
					libHash = ConfigFactoryImpl.doCheckChangesInLibraries(this);
				}
			}
		}

		return libHash;
	}

	@Override
	public Resource getLocalExtensionProviderDirectory() {
		Resource dir = getConfigDir().getRealResource("extensions/available");
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}

	@Override
	public List<ExtensionDefintion> loadLocalExtensions(boolean validate) {

		List<lucee.runtime.extension.RHExtensionCollection.Entry> entries = RHExtension.getExtensions(this);
		List<ExtensionDefintion> defintions = new ArrayList<>();
		for (lucee.runtime.extension.RHExtensionCollection.Entry e: entries) {
			defintions.add(e.getRHExtension().toExtensionDefinition());
		}
		return defintions;
	}

	@Override
	public void checkPassword() throws PageException {
		CFMLEngine engine = ConfigUtil.getEngine(this);
		ConfigWeb[] webs = getConfigWebs();
		try {
			ConfigFactoryImpl.reloadInstance(engine, this);
			for (ConfigWeb web: webs) {
				ConfigFactoryImpl.reloadInstance(engine, this, (ConfigWebImpl) web, true);
			}

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

	}

	@Override
	public AIEnginePool getAIEnginePool() {
		if (aiEnginePool == null) {
			synchronized (this) {
				if (aiEnginePool == null) {
					aiEnginePool = new AIEnginePool();
				}
			}
		}
		return aiEnginePool;
	}

	@Override
	public String getId() {
		if (_id == null) {
			_id = HashUtil.create64BitHashAsString(Caster.toString(getRootDirectory().getAbsolutePath()), Character.MAX_RADIX);
		}
		return _id;
	}

	public static class ConfigFile {

		public static void write(Resource configFile, Struct root, Charset charset) throws IOException, ConverterException {
			if (charset == null) charset = StandardCharsets.UTF_8;
			LogUtil.logGlobal((Config) null, Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "writing the config file [" + configFile + "]");

			JSONConverter json = new JSONConverter(true, StandardCharsets.UTF_8, JSONDateFormat.PATTERN_CF, false);
			String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_ROW, true);
			synchronized (SystemUtil.createToken("ConfigFile", ResourceUtil.getNormalizedPathEL(configFile))) {
				IOUtil.write(configFile, str, StandardCharsets.UTF_8, false);
			}
		}

		public static Struct read(Resource configFile, Charset charset) throws PageException, IOException {
			if (charset == null) charset = StandardCharsets.UTF_8;
			LogUtil.logGlobal((Config) null, Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "read the config file [" + configFile + "]");

			String raw;
			synchronized (SystemUtil.createToken("ConfigFile", ResourceUtil.getNormalizedPathEL(configFile))) {
				raw = IOUtil.toString(configFile, StandardCharsets.UTF_8);
			}
			return Caster.toStruct(new JSONExpressionInterpreter().interpret(null, raw));
		}

		public static void createConfigFile(Resource configFile) throws IOException {
			String resource = "/resource/config/server.json";
			InputStream is = InfoImpl.class.getResourceAsStream(resource);
			if (is == null) is = SystemUtil.getResourceAsStream(null, resource);
			if (is == null) throw new IOException("File [" + resource + "] does not exist.");

			configFile = configFile.getAbsoluteResource();

			synchronized (SystemUtil.createToken("ConfigFile", ResourceUtil.getNormalizedPathEL(configFile))) {
				if (configFile.exists()) configFile.delete();
				configFile.createNewFile();
				IOUtil.copy(is, configFile, true);
			}
			LogUtil.logGlobal(Log.LEVEL_DEBUG, ConfigFactory.class.getName(), "Written file: [" + configFile + "]");
		}

	}

	@Override
	public Class<AdminSync> getAdminSyncClass() {
		throw new RuntimeException("no longer supported");
	}
}