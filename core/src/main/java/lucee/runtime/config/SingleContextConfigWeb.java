package lucee.runtime.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.SAXException;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogEngine;
import lucee.commons.io.log.LoggerAndSourceData;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl.ResourceProviderFactory;
import lucee.commons.io.res.type.compress.Compress;
import lucee.commons.io.res.util.ResourceClassLoader;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lock.KeyLock;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CIPage;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollection;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.compiler.CFMLCompilerImpl;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.customtag.InitFile;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnectionPool;
import lucee.runtime.db.JDBCDriver;
import lucee.runtime.debug.DebuggerPool;
import lucee.runtime.dump.DumpWriter;
import lucee.runtime.engine.ExecutionLogFactory;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.DeprecatedException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.extension.Extension;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.ExtensionProvider;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.gateway.GatewayEngine;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.lock.LockManager;
import lucee.runtime.monitor.ActionMonitor;
import lucee.runtime.monitor.ActionMonitorCollector;
import lucee.runtime.monitor.IntervallMonitor;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.net.amf.AMFEngine;
import lucee.runtime.net.mail.Server;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.regex.Regex;
import lucee.runtime.rest.RestSettings;
import lucee.runtime.schedule.Scheduler;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.spooler.SpoolerEngine;
import lucee.runtime.tag.TagHandlerPool;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.writer.CFMLWriter;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;

public class SingleContextConfigWeb extends ConfigBase implements ConfigWebPro {

	private ConfigServerImpl cs;
	protected Password password;
	private final ConfigWebHelper helper;

	public SingleContextConfigWeb(ConfigServerImpl cs) {
		this.cs = cs;
		helper = new ConfigWebHelper(cs, this);
	}

	@Override
	public boolean isAllowURLRequestTimeout() {
		return cs.isAllowURLRequestTimeout();
	}

	@Override
	public short getCompileType() {
		return cs.getCompileType();
	}

	@Override
	public void reloadTimeServerOffset() {
		cs.reloadTimeServerOffset();
	}

	@Override
	public long lastModified() {
		return cs.lastModified();
	}

	@Override
	public short getScopeCascadingType() {
		return cs.getScopeCascadingType();
	}

	@Override
	public FunctionLib[] getFLDs(int dialect) {
		return cs.getFLDs(dialect);
	}

	@Override
	public FunctionLib getCombinedFLDs(int dialect) {
		return cs.getCombinedFLDs(dialect);
	}

	@Override
	public TagLib[] getTLDs(int dialect) {
		return cs.getTLDs(dialect);
	}

	@Override
	public boolean allowImplicidQueryCall() {
		return cs.allowImplicidQueryCall();
	}

	@Override
	public boolean mergeFormAndURL() {
		return cs.mergeFormAndURL();
	}

	@Override
	public TimeSpan getApplicationTimeout() {
		return cs.getApplicationTimeout();
	}

	@Override
	public TimeSpan getSessionTimeout() {
		return cs.getSessionTimeout();
	}

	@Override
	public TimeSpan getClientTimeout() {
		return cs.getClientTimeout();
	}

	@Override
	public TimeSpan getRequestTimeout() {
		return cs.getRequestTimeout();
	}

	@Override
	public boolean isClientCookies() {
		return cs.isClientCookies();
	}

	@Override
	public boolean isDevelopMode() {
		return cs.isDevelopMode();
	}

	@Override
	public boolean isClientManagement() {
		return cs.isClientManagement();
	}

	@Override
	public boolean isDomainCookies() {
		return cs.isDomainCookies();
	}

	@Override
	public boolean isSessionManagement() {
		return cs.isSessionManagement();
	}

	@Override
	public boolean isMailSpoolEnable() {
		return cs.isMailSpoolEnable();
	}

	@Override
	public boolean isMailSendPartial() {
		return cs.isMailSendPartial();
	}

	@Override
	public boolean isUserset() {
		return cs.isUserset();
	}

	@Override
	public Server[] getMailServers() {
		return cs.getMailServers();
	}

	@Override
	public int getMailTimeout() {
		return cs.getMailTimeout();
	}

	@Override
	public boolean getPSQL() {
		return cs.getPSQL();
	}

	@Override
	public int getQueryVarUsage() {
		return cs.getQueryVarUsage();
	}

	@Override
	public ClassLoader getClassLoader() {
		return cs.getClassLoader();
	}

	@Override
	public ClassLoader getClassLoaderEnv() {
		return cs.getClassLoaderEnv();
	}

	@Override
	public ClassLoader getClassLoaderCore() {
		return cs.getClassLoaderCore();
	}

	@Override
	public ResourceClassLoader getResourceClassLoader() {
		return cs.getResourceClassLoader();
	}

	@Override
	public ResourceClassLoader getResourceClassLoader(ResourceClassLoader defaultValue) {
		return cs.getResourceClassLoader(defaultValue);
	}

	@Override
	public Locale getLocale() {
		return cs.getLocale();
	}

	@Override
	public boolean debug() {
		return cs.debug();
	}

	@Override
	public boolean debugLogOutput() {
		return cs.debugLogOutput();
	}

	@Override
	public Resource getTempDirectory() {
		return cs.getTempDirectory();
	}

	@Override
	public int getMailSpoolInterval() {
		return cs.getMailSpoolInterval();
	}

	@Override
	public TimeZone getTimeZone() {
		return cs.getTimeZone();
	}

	@Override
	public long getTimeServerOffset() {
		return cs.getTimeServerOffset();
	}

	@Override
	public Scheduler getScheduler() {
		return cs.getScheduler();
	}

	@Override
	public Password isPasswordEqual(String password) {
		return cs.isPasswordEqual(password);
	}

	@Override
	public boolean hasPassword() {
		return cs.hasPassword();
	}

	@Override
	public boolean passwordEqual(Password password) {
		return cs.passwordEqual(password);
	}

	@Override
	public Mapping[] getMappings() {
		return cs.getMappings();
	}

	@Override
	public lucee.runtime.rest.Mapping[] getRestMappings() {
		return cs.getRestMappings();
	}

	@Override
	public PageSource getPageSource(Mapping[] mappings, String realPath, boolean onlyTopLevel) {
		return cs.getPageSource(mappings, realPath, onlyTopLevel);
	}

	@Override
	public PageSource getPageSourceExisting(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean onlyPhysicalExisting) {
		return cs.getPageSourceExisting(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, onlyPhysicalExisting);
	}

	@Override
	public PageSource[] getPageSources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping) {
		return cs.getPageSources(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping);
	}

	@Override
	public PageSource[] getPageSources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping,
			boolean useComponentMappings) {
		return cs.getPageSources(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping, useComponentMappings);
	}

	@Override
	public Resource getPhysical(Mapping[] mappings, String realPath, boolean alsoDefaultMapping) {
		return cs.getPhysical(mappings, realPath, alsoDefaultMapping);
	}

	@Override
	public Resource[] getPhysicalResources(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping) {
		return cs.getPhysicalResources(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping);
	}

	@Override
	public Resource getPhysicalResourceExisting(PageContext pc, Mapping[] mappings, String realPath, boolean onlyTopLevel, boolean useSpecialMappings, boolean useDefaultMapping) {
		return cs.getPhysicalResourceExisting(pc, mappings, realPath, onlyTopLevel, useSpecialMappings, useDefaultMapping);
	}

	@Override
	public PageSource toPageSource(Mapping[] mappings, Resource res, PageSource defaultValue) {
		return cs.toPageSource(mappings, res, defaultValue);
	}

	@Override
	public Resource getConfigDir() {
		return cs.getConfigDir();
	}

	@Override
	public Resource getConfigFile() {
		return cs.getConfigFile();
	}

	@Override
	public TagLib getCoreTagLib(int dialect) {
		return cs.getCoreTagLib(dialect);
	}

	@Override
	public Mapping[] getCustomTagMappings() {
		return cs.getCustomTagMappings();
	}

	@Override
	public long getLoadTime() {
		return cs.getLoadTime();
	}

	@Override
	public CFXTagPool getCFXTagPool() throws SecurityException {
		return cs.getCFXTagPool();
	}

	@Override
	public String getBaseComponentTemplate(int dialect) {
		return cs.getBaseComponentTemplate(dialect);
	}

	@Override
	public PageSource getBaseComponentPageSource(int dialect) {
		return cs.getBaseComponentPageSource(dialect);
	}

	@Override
	public PageSource getBaseComponentPageSource(int dialect, PageContext pc) {
		return cs.getBaseComponentPageSource(dialect, pc);
	}

	@Override
	public boolean getRestList() {
		return cs.getRestList();
	}

	@Override
	public short getClientType() {
		return cs.getClientType();
	}

	@Override
	public ClassDefinition<SearchEngine> getSearchEngineClassDefinition() {
		return cs.getSearchEngineClassDefinition();
	}

	@Override
	public String getSearchEngineDirectory() {
		return cs.getSearchEngineDirectory();
	}

	@Override
	public int getComponentDataMemberDefaultAccess() {
		return cs.getComponentDataMemberDefaultAccess();
	}

	@Override
	public String getTimeServer() {
		return cs.getTimeServer();
	}

	@Override
	public String getComponentDumpTemplate() {
		return cs.getComponentDumpTemplate();
	}

	@Override
	public String getDebugTemplate() {
		return cs.getDebugTemplate();
	}

	@Override
	public String getErrorTemplate(int statusCode) {
		return cs.getErrorTemplate(statusCode);
	}

	@Override
	public short getSessionType() {
		return cs.getSessionType();
	}

	@Override
	public String getUpdateType() {
		return null;
	}

	@Override
	public URL getUpdateLocation() {
		return null;
	}

	@Override
	public Resource getClassDirectory() {
		return cs.getClassDirectory();
	}

	@Override
	public Resource getLibraryDirectory() {
		return cs.getLibraryDirectory();
	}

	@Override
	public Resource getEventGatewayDirectory() {
		return cs.getEventGatewayDirectory();
	}

	@Override
	public Resource getClassesDirectory() {
		return cs.getClassesDirectory();
	}

	@Override
	public Resource getRootDirectory() {
		return null;
	}

	@Override
	public boolean isSuppressWhitespace() {
		return cs.isSuppressWhitespace();
	}

	@Override
	public boolean isSuppressContent() {
		return cs.isSuppressContent();
	}

	@Override
	public String getDefaultEncoding() {
		return cs.getDefaultEncoding();
	}

	@Override
	public Charset getTemplateCharset() {
		return cs.getTemplateCharset();
	}

	@Override
	public Charset getWebCharset() {
		return cs.getWebCharset();
	}

	@Override
	public CharSet getWebCharSet() {
		return cs.getWebCharSet();
	}

	@Override
	public Charset getResourceCharset() {
		return cs.getResourceCharset();
	}

	@Override
	public CharSet getResourceCharSet() {
		return cs.getResourceCharSet();
	}

	@Override
	public SecurityManager getSecurityManager() {
		return cs.getSecurityManager();
	}

	@Override
	public Resource getFldFile() {
		return cs.getFldFile();
	}

	@Override
	public Resource getTldFile() {
		return cs.getTldFile();
	}

	@Override
	public DataSource[] getDataSources() {
		return cs.getDataSources();
	}

	@Override
	public Map<String, DataSource> getDataSourcesAsMap() {
		return cs.getDataSourcesAsMap();
	}

	@Override
	public Charset getMailDefaultCharset() {
		return cs.getMailDefaultCharset();
	}

	@Override
	public ResourceProvider getDefaultResourceProvider() {
		return cs.getDefaultResourceProvider();
	}

	@Override
	public Iterator<Entry<String, Class<CacheHandler>>> getCacheHandlers() {
		return cs.getCacheHandlers();
	}

	@Override
	public ResourceProvider[] getResourceProviders() {
		return cs.getResourceProviders();
	}

	@Override
	public ResourceProviderFactory[] getResourceProviderFactories() {
		return cs.getResourceProviderFactories();
	}

	@Override
	public boolean hasResourceProvider(String scheme) {
		return cs.hasResourceProvider(scheme);
	}

	@Override
	public Resource getResource(String path) {
		return cs.getResource(path);
	}

	@Override
	public ApplicationListener getApplicationListener() {
		return cs.getApplicationListener();
	}

	@Override
	public int getScriptProtect() {
		return cs.getScriptProtect();
	}

    @Override
    public ArrayList<Pattern> getScriptProtectRegexList() {
        return cs.getScriptProtectRegexList();
    }

    @Override
	public ProxyData getProxyData() {
		return cs.getProxyData();
	}

	@Override
	public boolean isProxyEnableFor(String host) {
		return cs.isProxyEnableFor(host);
	}

	@Override
	public boolean getTriggerComponentDataMember() {
		return cs.getTriggerComponentDataMember();
	}

	@Override
	public Resource getClientScopeDir() {
		return cs.getClientScopeDir();
	}

	@Override
	public Resource getSessionScopeDir() {
		return cs.getSessionScopeDir();
	}

	@Override
	public long getClientScopeDirSize() {
		return cs.getClientScopeDirSize();
	}

	@Override
	public ClassLoader getRPCClassLoader(boolean reload) throws IOException {
		return cs.getRPCClassLoader(reload);
	}

	@Override
	public ClassLoader getRPCClassLoader(boolean reload, ClassLoader[] parents) throws IOException {
		return cs.getRPCClassLoader(reload, parents);
	}

	@Override
	public Resource getCacheDir() {
		return cs.getCacheDir();
	}

	@Override
	public long getCacheDirSize() {
		return cs.getCacheDirSize();
	}

	@Override
	public DumpWriter getDefaultDumpWriter(int defaultType) {
		return cs.getDefaultDumpWriter(defaultType);
	}

	@Override
	public DumpWriter getDumpWriter(String name) throws DeprecatedException {
		return cs.getDumpWriter(name);
	}

	@Override
	public DumpWriter getDumpWriter(String name, int defaultType) throws ExpressionException {
		return cs.getDumpWriter(name, defaultType);
	}

	@Override
	public boolean useComponentShadow() {
		return cs.useComponentShadow();
	}

	@Override
	public boolean useComponentPathCache() {
		return cs.useComponentPathCache();
	}

	@Override
	public boolean useCTPathCache() {
		return cs.useCTPathCache();
	}

	@Override
	public DataSource getDataSource(String datasource) throws DatabaseException {
		return cs.getDataSource(datasource);
	}

	@Override
	public DataSource getDataSource(String datasource, DataSource defaultValue) {
		return cs.getDataSource(datasource, defaultValue);
	}

	@Override
	public PrintWriter getErrWriter() {
		return cs.getErrWriter();
	}

	@Override
	public PrintWriter getOutWriter() {
		return cs.getOutWriter();
	}

	@Override
	public DatasourceConnectionPool getDatasourceConnectionPool() {
		return cs.getDatasourceConnectionPool();
	}

	@Override
	public boolean doLocalCustomTag() {
		return cs.doLocalCustomTag();
	}

	@Override
	public String[] getCustomTagExtensions() {
		return cs.getCustomTagExtensions();
	}

	@Override
	public boolean doComponentDeepSearch() {
		return cs.doComponentDeepSearch();
	}

	@Override
	public boolean doCustomTagDeepSearch() {
		return cs.doCustomTagDeepSearch();
	}

	@Override
	public double getVersion() {
		return cs.getVersion();
	}

	@Override
	public boolean contentLength() {
		return cs.contentLength();
	}

	@Override
	public boolean allowCompression() {
		return cs.allowCompression();
	}

	@Override
	public Struct getConstants() {
		return cs.getConstants();
	}

	@Override
	public boolean isShowVersion() {
		return cs.isShowVersion();
	}

	@Override
	public RemoteClient[] getRemoteClients() {
		return cs.getRemoteClients();
	}

	@Override
	public SpoolerEngine getSpoolerEngine() {
		return cs.getSpoolerEngine();
	}

	@Override
	public Resource getRemoteClientDirectory() {
		return cs.getRemoteClientDirectory();
	}

	@Override
	public boolean getErrorStatusCode() {
		return cs.getErrorStatusCode();
	}

	@Override
	public int getLocalMode() {
		return cs.getLocalMode();
	}

	@Override
	public Resource getVideoDirectory() {
		return cs.getVideoDirectory();
	}

	@Override
	public Resource getExtensionDirectory() {
		return cs.getExtensionDirectory();
	}

	@Override
	public ExtensionProvider[] getExtensionProviders() {
		return cs.getExtensionProviders();
	}

	@Override
	public RHExtensionProvider[] getRHExtensionProviders() {
		return cs.getRHExtensionProviders();
	}

	@Override
	public Extension[] getExtensions() {
		return cs.getExtensions();
	}

	@Override
	public RHExtension[] getRHExtensions() {
		return cs.getRHExtensions();
	}

	@Override
	public boolean isExtensionEnabled() {
		return cs.isExtensionEnabled();
	}

	@Override
	public boolean allowRealPath() {
		return cs.allowRealPath();
	}

	@Override
	public Class getClusterClass() {
		return cs.getClusterClass();
	}

	@Override
	public Struct getRemoteClientUsage() {
		return cs.getRemoteClientUsage();
	}

	@Override
	public Class<AdminSync> getAdminSyncClass() {
		return cs.getAdminSyncClass();
	}

	@Override
	public AdminSync getAdminSync() throws ClassException {
		return cs.getAdminSync();
	}

	@Override
	public Class getVideoExecuterClass() {
		return cs.getVideoExecuterClass();
	}

	@Override
	public boolean getUseTimeServer() {
		return cs.getUseTimeServer();
	}

	@Override
	public Collection<Mapping> getTagMappings() {
		return cs.getTagMappings();
	}

	@Override
	public Mapping getTagMapping(String mappingName) {
		return cs.getTagMapping(mappingName);
	}

	@Override
	public Mapping getDefaultTagMapping() {
		return cs.getDefaultTagMapping();
	}

	@Override
	public Mapping getFunctionMapping(String mappingName) {
		return cs.getFunctionMapping(mappingName);
	}

	@Override
	public Mapping getDefaultFunctionMapping() {
		return cs.getDefaultFunctionMapping();
	}

	@Override
	public Collection<Mapping> getFunctionMappings() {
		return cs.getFunctionMappings();
	}

	@Override
	public String getDefaultDataSource() {
		return cs.getDefaultDataSource();
	}

	@Override
	public short getInspectTemplate() {
		return cs.getInspectTemplate();
	}

	@Override
	public boolean getTypeChecking() {
		return cs.getTypeChecking();
	}

	@Override
	public String getSerialNumber() {
		return cs.getSerialNumber();
	}

	@Override
	public Map<String, CacheConnection> getCacheConnections() {
		return cs.getCacheConnections();
	}

	@Override
	public CacheConnection getCacheDefaultConnection(int type) {
		return cs.getCacheDefaultConnection(type);
	}

	@Override
	public String getCacheDefaultConnectionName(int type) {
		return cs.getCacheDefaultConnectionName(type);
	}

	@Override
	public boolean getExecutionLogEnabled() {
		return cs.getExecutionLogEnabled();
	}

	@Override
	public ExecutionLogFactory getExecutionLogFactory() {
		return cs.getExecutionLogFactory();
	}

	@Override
	public ORMEngine resetORMEngine(PageContext pc, boolean force) throws PageException {
		return cs.resetORMEngine(pc, force);
	}

	@Override
	public ORMEngine getORMEngine(PageContext pc) throws PageException {
		return cs.getORMEngine(pc);
	}

	@Override
	public ClassDefinition<? extends ORMEngine> getORMEngineClassDefintion() {
		return cs.getORMEngineClassDefintion();
	}

	@Override
	public Mapping[] getComponentMappings() {
		return cs.getComponentMappings();
	}

	@Override
	public ORMConfiguration getORMConfig() {
		return cs.getORMConfig();
	}

	@Override
	public CIPage getCachedPage(PageContext pc, String pathWithCFC) throws TemplateException {
		return cs.getCachedPage(pc, pathWithCFC);
	}

	@Override
	public void putCachedPageSource(String pathWithCFC, PageSource ps) {
		cs.putCachedPageSource(pathWithCFC, ps);
	}

	@Override
	public InitFile getCTInitFile(PageContext pc, String key) {
		return cs.getCTInitFile(pc, key);
	}

	@Override
	public void putCTInitFile(String key, InitFile initFile) {
		cs.putCTInitFile(key, initFile);
	}

	@Override
	public Struct listCTCache() {
		return cs.listCTCache();
	}

	@Override
	public void clearCTCache() {
		cs.clearCTCache();
	}

	@Override
	public void clearFunctionCache() {
		cs.clearFunctionCache();
	}

	@Override
	public UDF getFromFunctionCache(String key) {
		return cs.getFromFunctionCache(key);
	}

	@Override
	public void putToFunctionCache(String key, UDF udf) {
		cs.putToFunctionCache(key, udf);
	}

	@Override
	public Struct listComponentCache() {
		return cs.listComponentCache();
	}

	@Override
	public void clearComponentCache() {
		cs.clearComponentCache();
	}

	@Override
	public void clearApplicationCache() {
		cs.clearApplicationCache();
	}

	@Override
	public ImportDefintion getComponentDefaultImport() {
		return cs.getComponentDefaultImport();
	}

	@Override
	public boolean getComponentLocalSearch() {
		return cs.getComponentLocalSearch();
	}

	@Override
	public boolean getComponentRootSearch() {
		return cs.getComponentRootSearch();
	}

	@Override
	public Compress getCompressInstance(Resource zipFile, int format, boolean caseSensitive) throws IOException {
		return cs.getCompressInstance(zipFile, format, caseSensitive);
	}

	@Override
	public boolean getSessionCluster() {
		return cs.getSessionCluster();
	}

	@Override
	public boolean getClientCluster() {
		return cs.getClientCluster();
	}

	@Override
	public String getClientStorage() {
		return cs.getClientStorage();
	}

	@Override
	public String getSessionStorage() {
		return cs.getSessionStorage();
	}

	@Override
	public DebugEntry[] getDebugEntries() {
		return cs.getDebugEntries();
	}

	@Override
	public DebugEntry getDebugEntry(String ip, DebugEntry defaultValue) {
		return cs.getDebugEntry(ip, defaultValue);
	}

	@Override
	public int getDebugMaxRecordsLogged() {
		return cs.getDebugMaxRecordsLogged();
	}

	@Override
	public boolean getDotNotationUpperCase() {
		return cs.getDotNotationUpperCase();
	}

	@Override
	public boolean preserveCase() {
		return cs.preserveCase();
	}

	@Override
	public boolean getDefaultFunctionOutput() {
		return cs.getDefaultFunctionOutput();
	}

	@Override
	public boolean getSuppressWSBeforeArg() {
		return cs.getSuppressWSBeforeArg();
	}

	@Override
	public RestSettings getRestSetting() {
		return cs.getRestSetting();
	}

	@Override
	public int getCFMLWriterType() {
		return cs.getCFMLWriterType();
	}

	@Override
	public boolean getBufferOutput() {
		return cs.getBufferOutput();
	}

	@Override
	public boolean hasDebugOptions(int debugOption) {
		return cs.hasDebugOptions(debugOption);
	}

	@Override
	public boolean checkForChangesInConfigFile() {
		return cs.checkForChangesInConfigFile();
	}

	@Override
	public int getExternalizeStringGTE() {
		return cs.getExternalizeStringGTE();
	}

	@Override
	public Map<String, LoggerAndSourceData> getLoggers() {
		return cs.getLoggers();
	}

	@Override
	public Log getLog(String name) {
		return cs.getLog(name);
	}

	@Override
	public Log getLog(String name, boolean createIfNecessary) throws PageException {
		return cs.getLog(name, createIfNecessary);
	}

	@Override
	public Map<Key, Map<Key, Object>> getTagDefaultAttributeValues() {
		return cs.getTagDefaultAttributeValues();
	}

	@Override
	public Boolean getHandleUnQuotedAttrValueAsString() {
		return cs.getHandleUnQuotedAttrValueAsString();
	}

	@Override
	public Object getCachedWithin(int type) {
		return cs.getCachedWithin(type);
	}

	@Override
	public Resource getPluginDirectory() {
		return cs.getPluginDirectory();
	}

	@Override
	public Resource getLogDirectory() {
		return cs.getLogDirectory();
	}

	@Override
	public String getSalt() {
		return cs.getSalt();
	}

	@Override
	public int getPasswordType() {
		return cs.getPasswordType();
	}

	@Override
	public String getPasswordSalt() {
		return cs.getPasswordSalt();
	}

	@Override
	public int getPasswordOrigin() {
		return cs.getPasswordOrigin();
	}

	@Override
	public Collection<BundleDefinition> getExtensionBundleDefintions() {
		return cs.getExtensionBundleDefintions();
	}

	@Override
	public JDBCDriver[] getJDBCDrivers() {
		return cs.getJDBCDrivers();
	}

	@Override
	public JDBCDriver getJDBCDriverByClassName(String className, JDBCDriver defaultValue) {
		return cs.getJDBCDriverByClassName(className, defaultValue);
	}

	@Override
	public JDBCDriver getJDBCDriverById(String id, JDBCDriver defaultValue) {
		return cs.getJDBCDriverById(id, defaultValue);
	}

	@Override
	public JDBCDriver getJDBCDriverByBundle(String bundleName, Version version, JDBCDriver defaultValue) {
		return cs.getJDBCDriverByBundle(bundleName, version, defaultValue);
	}

	@Override
	public JDBCDriver getJDBCDriverByCD(ClassDefinition cd, JDBCDriver defaultValue) {
		return cs.getJDBCDriverByCD(cd, defaultValue);
	}

	@Override
	public int getQueueMax() {
		return cs.getQueueMax();
	}

	@Override
	public long getQueueTimeout() {
		return cs.getQueueTimeout();
	}

	@Override
	public boolean getQueueEnable() {
		return cs.getQueueEnable();
	}

	@Override
	public boolean getCGIScopeReadonly() {
		return cs.getCGIScopeReadonly();
	}

	@Override
	public Resource getDeployDirectory() {
		return cs.getDeployDirectory();
	}

	@Override
	public boolean allowLuceeDialect() {
		return cs.allowLuceeDialect();
	}

	@Override
	public Map<String, ClassDefinition> getCacheDefinitions() {
		return cs.getCacheDefinitions();
	}

	@Override
	public ClassDefinition getCacheDefinition(String className) {
		return cs.getCacheDefinition(className);
	}

	@Override
	public Resource getAntiSamyPolicy() {
		return cs.getAntiSamyPolicy();
	}

	@Override
	public LogEngine getLogEngine() {
		return cs.getLogEngine();
	}

	@Override
	public TimeSpan getCachedAfterTimeRange() {
		return cs.getCachedAfterTimeRange();
	}

	@Override
	public Map<String, Startup> getStartups() {
		return cs.getStartups();
	}

	@Override
	public Regex getRegex() {
		return cs.getRegex();
	}

	@Override
	public RHExtension[] getServerRHExtensions() {
		return cs.getServerRHExtensions();
	}

	@Override
	public Cluster createClusterScope() throws PageException {
		return cs.createClusterScope();
	}

	@Override
	public Collection<BundleDefinition> getAllExtensionBundleDefintions() {
		return cs.getAllExtensionBundleDefintions();
	}

	@Override
	public void checkPassword() throws PageException {
		cs.checkPassword();
	}

	@Override
	public List<ExtensionDefintion> loadLocalExtensions(boolean validate) {
		return cs.loadLocalExtensions(validate);
	}

	@Override
	public Collection<RHExtension> getAllRHExtensions() {
		return cs.getAllRHExtensions();
	}

	@Override
	public boolean allowRequestTimeout() {
		return cs.allowRequestTimeout();
	}

	@Override
	public boolean closeConnection() {
		return cs.closeConnection();
	}

	@Override
	public void checkPermGenSpace(boolean check) {
		cs.checkPermGenSpace(check);
	}

	@Override
	public ActionMonitor getActionMonitor(String arg0) throws PageException {
		return cs.getActionMonitor(arg0);
	}

	@Override
	public ConfigServer getConfigServer(String arg0) throws PageException {
		return getConfigServer(arg0);
	}

	@Override
	public ConfigServer getConfigServer(String arg0, long arg1) throws PageException {
		return getConfigServer(arg0, arg1);
	}

	@Override
	public boolean getFullNullSupport() {
		return cs.getFullNullSupport();
	}

	@Override
	public IdentificationWeb getIdentification() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntervallMonitor getIntervallMonitor(String arg0) throws PageException {
		return cs.getIntervallMonitor(arg0);
	}

	@Override
	public IntervallMonitor[] getIntervallMonitors() {
		return cs.getIntervallMonitors();
	}

	@Override
	public Resource getLocalExtensionProviderDirectory() {
		return cs.getLocalExtensionProviderDirectory();
	}

	@Override
	public boolean getLoginCaptcha() {
		return cs.getLoginCaptcha();
	}

	@Override
	public int getLoginDelay() {
		return cs.getLoginDelay();
	}

	@Override
	public boolean getRememberMe() {
		return cs.getRememberMe();
	}

	@Override
	public RequestMonitor getRequestMonitor(String arg0) throws PageException {
		return cs.getRequestMonitor(arg0);
	}

	@Override
	public RequestMonitor[] getRequestMonitors() {
		return cs.getRequestMonitors();
	}

	@Override
	public Resource getSecurityDirectory() {
		return cs.getSecurityDirectory();
	}

	@Override
	public ThreadQueue getThreadQueue() {
		return cs.getThreadQueue();
	}

	@Override
	public boolean hasServerPassword() {
		return cs.hasServerPassword();
	}

	@Override
	public boolean isMonitoringEnabled() {
		return cs.isMonitoringEnabled();
	}

	@Override
	public AMFEngine getAMFEngine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConfigServer getConfigServer(Password arg0) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource getConfigServerDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CFMLFactory getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockManager getLockManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchEngine getSearchEngine(PageContext arg0) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JspWriter getWriter(PageContext arg0, HttpServletRequest arg1, HttpServletResponse arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mapping getDefaultServerTagMapping() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mapping getApplicationMapping(String type, String virtual, String physical, String archive, boolean physicalFirst, boolean ignoreVirtual) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Mapping> getServerFunctionMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mapping getServerFunctionMapping(String mappingName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Mapping> getServerTagMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mapping getServerTagMapping(String mappingName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getAllLabels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDefaultPassword() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getServerPasswordType() {
		return cs.getPasswordType();
	}

	@Override
	public String getServerPasswordSalt() {
		return cs.getPasswordSalt();
	}

	@Override
	public int getServerPasswordOrigin() {
		return cs.getPasswordOrigin();
	}

	@Override
	public GatewayEngine getGatewayEngine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WSHandler getWSHandler() throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CFMLCompilerImpl getCompiler() {
		return helper.getCompiler();
	}

	@Override
	public Mapping getApplicationMapping(String type, String virtual, String physical, String archive, boolean physicalFirst, boolean ignoreVirtual,
			boolean checkPhysicalFromWebroot, boolean checkArchiveFromWebroot) {
		return helper.getApplicationMapping(type, virtual, physical, archive, physicalFirst, ignoreVirtual, checkPhysicalFromWebroot, checkArchiveFromWebroot);
	}

	@Override
	public Mapping[] getApplicationMappings() {
		return helper.getApplicationMappings();
	}

	@Override
	public boolean isApplicationMapping(Mapping mapping) {
		return helper.isApplicationMapping(mapping);
	}

	@Override
	public CIPage getBaseComponentPage(int dialect, PageContext pc) throws PageException {
		return helper.getBaseComponentPage(dialect, pc);
	}

	@Override
	public void resetBaseComponentPage() {
		helper.resetBaseComponentPage();
	}

	@Override
	public ActionMonitorCollector getActionMonitorCollector() {
		return cs.getActionMonitorCollector();
	}

	@Override
	public KeyLock<String> getContextLock() {
		return helper.getContextLock();
	}

	@Override
	public CacheHandlerCollection getCacheHandlerCollection(int type, CacheHandlerCollection defaultValue) {
		return helper.getCacheHandlerCollection(type, defaultValue);

	}

	@Override
	public void releaseCacheHandlers(PageContext pc) {
		helper.releaseCacheHandlers(pc);
	}

	@Override
	public DebuggerPool getDebuggerPool() {
		return helper.getDebuggerPool();
	}

	@Override
	public CFMLWriter getCFMLWriter(PageContext pc, HttpServletRequest req, HttpServletResponse rsp) {
		return helper.getCFMLWriter(pc, req, rsp);
	}

	@Override
	public TagHandlerPool getTagHandlerPool() {
		return helper.getTagHandlerPool();
	}

	@Override
	public String getHash() {
		return SystemUtil.hash(getServletContext());
	}

	@Override
	public void updatePassword(boolean server, String passwordOld, String passwordNew) throws PageException, IOException, SAXException, BundleException {
		PasswordImpl.updatePassword(server ? cs : this, passwordOld, passwordNew);
	}

	@Override
	public Password updatePasswordIfNecessary(boolean server, String passwordRaw) {
		if (server) {
			return PasswordImpl.updatePasswordIfNecessary(cs, cs.password, passwordRaw);
		}
		return PasswordImpl.updatePasswordIfNecessary(this, password, passwordRaw);
	}

	@Override
	public Password isServerPasswordEqual(String password) {
		return cs.isPasswordEqual(password);
	}

	@Override
	public boolean hasIndividualSecurityManager() {
		return helper.hasIndividualSecurityManager(this);
	}

	@Override
	public short getPasswordSource() {
		return helper.getPasswordSource();
	}

	@Override
	public void reset() {
		helper.reset();
	}

	@Override
	public PageSource getApplicationPageSource(PageContext pc, String path, String filename, int mode, RefBoolean isCFC) {
		return cs.getApplicationPageSource(pc, path, filename, mode, isCFC);
	}

	@Override
	public void putApplicationPageSource(String path, PageSource ps, String filename, int mode, boolean isCFC) {
		cs.putApplicationPageSource(path, ps, filename, mode, isCFC);
	}

	@Override
	public TimeSpan getApplicationPathhCacheTimeout() {
		return cs.getApplicationPathhCacheTimeout();
	}
}
