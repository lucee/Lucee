package lucee.runtime.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Version;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogEngine;
import lucee.commons.io.log.LoggerAndSourceData;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl.ResourceProviderFactory;
import lucee.commons.io.res.type.compress.Compress;
import lucee.commons.io.res.util.ResourceClassLoader;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.types.RefBoolean;
import lucee.runtime.CIPage;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.customtag.InitFile;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DatasourceConnectionPool;
import lucee.runtime.db.JDBCDriver;
import lucee.runtime.engine.ExecutionLogFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.regex.Regex;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Cluster;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;

// FUTURE add to Config not necessary all of them!
public interface ConfigPro extends Config {

	public static final int CLIENT_BOOLEAN_TRUE = 0;
	public static final int CLIENT_BOOLEAN_FALSE = 1;
	public static final int SERVER_BOOLEAN_TRUE = 2;
	public static final int SERVER_BOOLEAN_FALSE = 3;

	public static final int DEBUG_DATABASE = 1;
	public static final int DEBUG_EXCEPTION = 2;
	public static final int DEBUG_TRACING = 4;
	public static final int DEBUG_TIMER = 8;
	public static final int DEBUG_IMPLICIT_ACCESS = 16;
	public static final int DEBUG_QUERY_USAGE = 32;
	public static final int DEBUG_DUMP = 64;
	public static final int DEBUG_TEMPLATE = 128;

	public static final int MODE_CUSTOM = 1;
	public static final int MODE_STRICT = 2;

	public static final int CFML_WRITER_REFULAR = 1;
	public static final int CFML_WRITER_WS = 2;
	public static final int CFML_WRITER_WS_PREF = 3;

	public static final String DEFAULT_STORAGE_SESSION = "memory";
	public static final String DEFAULT_STORAGE_CLIENT = "cookie";

	public static final int QUERY_VAR_USAGE_IGNORE = 1;
	public static final int QUERY_VAR_USAGE_WARN = 2;
	public static final int QUERY_VAR_USAGE_ERROR = 4;

	public Iterator<Entry<String, Class<CacheHandler>>> getCacheHandlers();

	public boolean getDotNotationUpperCase();

	public boolean getExecutionLogEnabled();

	public boolean getSuppressWSBeforeArg();

	public boolean getDefaultFunctionOutput();

	public TagLib getCoreTagLib(int dialect);

	public TagLib[] getTLDs(int dialect);

	public FunctionLib[] getFLDs(int dialect);

	public Collection<Mapping> getFunctionMappings();

	public Mapping getFunctionMapping(String mappingName);

	public Collection<Mapping> getTagMappings();

	public Mapping getDefaultTagMapping();

	public Mapping getTagMapping(String mappingName);

	public RHExtension[] getRHExtensions();

	public int getPasswordType();

	public String getPasswordSalt();

	public boolean preserveCase();

	public boolean hasDebugOptions(int debugOption);

	public int getDebugMaxRecordsLogged();

	public boolean getComponentLocalSearch();

	public boolean getComponentRootSearch();

	public lucee.runtime.rest.Mapping[] getRestMappings();

	public UDF getFromFunctionCache(String key);

	public void putToFunctionCache(String key, UDF udf);

	public RHExtension[] getServerRHExtensions();

	public DatasourceConnectionPool getDatasourceConnectionPool();

	public void clearCTCache();

	public void clearFunctionCache();

	public Resource getSessionScopeDir();

	public Regex getRegex();

	public boolean closeConnection();

	public PageSource getBaseComponentPageSource(int dialect, PageContext pc);

	public TimeSpan getCachedAfterTimeRange();

	public Log getLog(String name, boolean createIfNecessary);

	public Map<String, LoggerAndSourceData> getLoggers();

	public boolean isSuppressContent();

	public boolean allowCompression();

	public boolean getTypeChecking();

	public ResourceClassLoader getResourceClassLoader();

	public ClassLoader getRPCClassLoader(boolean reload, ClassLoader[] parents) throws IOException;

	public PageSource toPageSource(Mapping[] mappings, Resource res, PageSource defaultValue);

	public boolean getRestList();

	public ExecutionLogFactory getExecutionLogFactory();

	public boolean debugLogOutput();

	public int getExternalizeStringGTE();

	public boolean allowLuceeDialect();

	public FunctionLib getCombinedFLDs(int dialect);

	public Cluster createClusterScope() throws PageException;

	public ClassLoader getClassLoaderCore();

	public Resource getLogDirectory();

	public LogEngine getLogEngine();

	public boolean getUseTimeServer();

	public boolean useComponentPathCache();

	public ImportDefintion getComponentDefaultImport();

	public boolean doComponentDeepSearch();

	public boolean isDevelopMode();

	public boolean getCGIScopeReadonly();

	public String getSessionStorage();

	public String getClientStorage();

	public boolean getBufferOutput();

	public int getCFMLWriterType();

	public boolean contentLength();

	public boolean getQueueEnable();

	public long getQueueTimeout();

	public int getQueueMax();

	public boolean isAllowURLRequestTimeout();

	public Resource getPluginDirectory();

	public Mapping getDefaultFunctionMapping();

	public Resource getEventGatewayDirectory();

	public void clearComponentCache();

	public void clearApplicationCache();

	public Map<String, ConfigBase.Startup> getStartups();

	public AdminSync getAdminSync() throws ClassException;

	public Password isPasswordEqual(String password);

	public JDBCDriver[] getJDBCDrivers();

	/**
	 * get the extension bundle definition not only from this context, get it from all contexts,
	 * including the server context
	 * 
	 * @return
	 */
	public Collection<BundleDefinition> getAllExtensionBundleDefintions();

	public boolean useCTPathCache();

	public ORMConfiguration getORMConfig();

	public ClassDefinition<? extends ORMEngine> getORMEngineClassDefintion();

	public Resource getLibraryDirectory();

	public Resource getClassesDirectory();

	public RHExtensionProvider[] getRHExtensionProviders();

	public DebugEntry[] getDebugEntries();

	public DebugEntry getDebugEntry(String ip, DebugEntry defaultValue);

	public int getQueryVarUsage();

	public void checkPassword() throws PageException;

	public String getSerialNumber();

	public ORMEngine resetORMEngine(PageContext pc, boolean force) throws PageException;

	// FUTURE add to interface
	public boolean isMailSendPartial();

	// FUTURE add to interface and impl
	public boolean isUserset();

	public CharSet getResourceCharSet();

	public CharSet getWebCharSet();

	public Map<String, ClassDefinition> getCacheDefinitions();

	public ClassDefinition getCacheDefinition(String className);

	public Resource getAntiSamyPolicy();

	public Map<Key, Map<Key, Object>> getTagDefaultAttributeValues();

	public ResourceProviderFactory[] getResourceProviderFactories();

	public boolean hasResourceProvider(String scheme);

	public Struct listCTCache();

	public Struct listComponentCache();

	public ClassLoader getClassLoaderEnv();

	public JDBCDriver getJDBCDriverById(String id, JDBCDriver defaultValue);

	public JDBCDriver getJDBCDriverByBundle(String bundleName, Version version, JDBCDriver defaultValue);

	public JDBCDriver getJDBCDriverByCD(ClassDefinition cd, JDBCDriver defaultValue);

	public JDBCDriver getJDBCDriverByClassName(String className, JDBCDriver defaultValue);

	public InitFile getCTInitFile(PageContext pc, String key);

	public void putCTInitFile(String key, InitFile initFile);

	public CIPage getCachedPage(PageContext pc, String pathWithCFC) throws TemplateException;

	public void putCachedPageSource(String pathWithCFC, PageSource ps);

	/**
	 * 
	 * @param validate if true Lucee checks if the file is a valid zip file
	 * @return
	 */
	public List<ExtensionDefintion> loadLocalExtensions(boolean validate);

	public ResourceClassLoader getResourceClassLoader(ResourceClassLoader defaultValue);

	public Compress getCompressInstance(Resource zipFile, int format, boolean caseSensitive) throws IOException;

	public int getPasswordOrigin();

	public String getSalt();

	public Collection<BundleDefinition> getExtensionBundleDefintions();

	public boolean checkForChangesInConfigFile();

	public long lastModified();

	public Collection<RHExtension> getAllRHExtensions();

	public PageSource getApplicationPageSource(PageContext pc, String path, String filename, int mode, RefBoolean isCFC);

	public void putApplicationPageSource(String path, PageSource ps, String filename, int mode, boolean isCFC);

	public TimeSpan getApplicationPathhCacheTimeout();
}
