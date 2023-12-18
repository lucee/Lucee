package lucee.runtime.config;

import java.io.IOException;

import javax.servlet.ServletConfig;

import org.osgi.framework.BundleException;
import org.xml.sax.SAXException;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl.ResourceProviderFactory;
import lucee.runtime.config.gateway.GatewayMap;
import lucee.runtime.exp.PageException;

public class ConfigWebImpl implements ConfigWebPro {
	private ConfigWebInner instance;

	public ConfigWebImpl(ConfigWebInner instance) {
		this.instance = instance;
	}

	@Override
	public lucee.commons.io.res.Resource getExtensionDirectory() {
		return instance.getExtensionDirectory();
	}

	@Override
	public lucee.commons.io.res.Resource getSecurityDirectory() {
		return instance.getSecurityDirectory();
	}

	@Override
	public java.nio.charset.Charset getMailDefaultCharset() {
		return instance.getMailDefaultCharset();
	}

	@Override
	public java.lang.String getDebugTemplate() {
		return instance.getDebugTemplate();
	}

	@Override
	public lucee.runtime.gateway.GatewayEngine getGatewayEngine() throws PageException {
		return instance.getGatewayEngine();
	}

	@Override
	public boolean isProxyEnableFor(java.lang.String arg0) {
		return instance.isProxyEnableFor(arg0);
	}

	@Override
	public lucee.commons.io.res.Resource getPhysical(lucee.runtime.Mapping[] arg0, java.lang.String arg1, boolean arg2) {
		return instance.getPhysical(arg0, arg1, arg2);
	}

	@Override
	public void setIdentification(lucee.runtime.config.IdentificationWeb arg0) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setIdentification(arg0);
		else((SingleContextConfigWeb) instance).setIdentification(arg0);
		// ignored for Single, should not be called anyway
	}

	@Override
	public boolean equals(java.lang.Object arg0) {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).equals(arg0);
		else return ((SingleContextConfigWeb) instance).equals(arg0);
	}

	@Override
	public boolean hasResourceProvider(java.lang.String arg0) {
		return instance.hasResourceProvider(arg0);
	}

	@Override
	public java.lang.String getPasswordSalt() {
		return instance.getPasswordSalt();
	}

	public java.lang.Object[] getConsoleLayouts() throws lucee.runtime.exp.PageException {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getConsoleLayouts();
		else return ((SingleContextConfigWeb) instance).getConsoleLayouts();
	}

	public java.lang.String getServerSalt() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getServerSalt();
		else return ((SingleContextConfigWeb) instance).getServerSalt();
	}

	@Override
	public lucee.runtime.PageSource getApplicationPageSource(lucee.runtime.PageContext arg0, java.lang.String arg1, java.lang.String arg2, int arg3,
			lucee.commons.lang.types.RefBoolean arg4) {
		return instance.getApplicationPageSource(arg0, arg1, arg2, arg3, arg4);
	}

	@Override
	public short getSessionType() {
		return instance.getSessionType();
	}

	@Override
	public short getClientType() {
		return instance.getClientType();
	}

	@Override
	public java.lang.String getSearchEngineDirectory() {
		return instance.getSearchEngineDirectory();
	}

	@Override
	public lucee.runtime.PageSource getBaseComponentPageSource(lucee.runtime.PageContext arg1, boolean force) {
		return instance.getBaseComponentPageSource(arg1, force);
	}

	@Override
	public lucee.commons.io.res.ResourceProvider getDefaultResourceProvider() {
		return instance.getDefaultResourceProvider();
	}

	public lucee.commons.lang.CharSet getMailDefaultCharSet() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getMailDefaultCharSet();
		else return ((SingleContextConfigWeb) instance).getMailDefaultCharSet();
	}

	@Override
	public lucee.commons.io.res.Resource getFldFile() {
		return instance.getFldFile();
	}

	@Override
	public lucee.commons.io.res.Resource getTldFile() {
		return instance.getTldFile();
	}

	@Override
	public lucee.commons.io.res.Resource[] getPhysicalResources(lucee.runtime.PageContext arg0, lucee.runtime.Mapping[] arg1, java.lang.String arg2, boolean arg3, boolean arg4,
			boolean arg5) {
		return instance.getPhysicalResources(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public double getVersion() {
		return instance.getVersion();
	}

	@Override
	public lucee.transformer.library.function.FunctionLib getFLDs() {
		return instance.getFLDs();
	}

	@Override
	public lucee.transformer.library.function.FunctionLib[] getFLDs(int dialect) {
		return instance.getFLDs(dialect);
	}

	@Override
	public java.io.PrintWriter getOutWriter() {
		return instance.getOutWriter();
	}

	@Override
	public lucee.runtime.listener.ApplicationListener getApplicationListener() {
		return instance.getApplicationListener();
	}

	@Override
	public int getComponentDataMemberDefaultAccess() {
		return instance.getComponentDataMemberDefaultAccess();
	}

	@Override
	public long getCacheDirSize() {
		return instance.getCacheDirSize();
	}

	@Override
	public lucee.runtime.engine.ThreadQueue getThreadQueue() {
		return instance.getThreadQueue();
	}

	@Override
	public lucee.runtime.config.DebugEntry[] getDebugEntries() {
		return instance.getDebugEntries();
	}

	public int getDebugOptions() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getDebugOptions();
		else return ((SingleContextConfigWeb) instance).getDebugOptions();
	}

	@Override
	public lucee.commons.io.res.Resource getDeployDirectory() {
		return instance.getDeployDirectory();
	}

	@Override
	public short getAdminMode() {
		return instance.getAdminMode();
	}

	@Override
	public long getApplicationPathCacheTimeout() {
		return instance.getApplicationPathCacheTimeout();
	}

	public java.util.Map getGatewayEntries() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getGatewayEntries();
		else return ((SingleContextConfigWeb) instance).getGatewayEntries();
	}

	@Override
	public int getScriptProtect() {
		return instance.getScriptProtect();
	}

	public lucee.runtime.Mapping getScriptMapping() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getScriptMapping();
		else return ((SingleContextConfigWeb) instance).getScriptMapping();
	}

	@Override
	public lucee.runtime.monitor.IntervallMonitor[] getIntervallMonitors() {
		return instance.getIntervallMonitors();
	}

	public void resetRPCClassLoader() {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).resetRPCClassLoader();
		else((SingleContextConfigWeb) instance).resetRPCClassLoader();
	}

	@Override
	public lucee.runtime.dump.DumpWriter getDumpWriter(java.lang.String arg0) throws PageException {
		return instance.getDumpWriter(arg0);
	}

	@Override
	public boolean useCTPathCache() {
		return instance.useCTPathCache();
	}

	@Override
	public boolean getFullNullSupport() {
		return instance.getFullNullSupport();
	}

	@Override
	public boolean isSessionManagement() {
		return instance.isSessionManagement();
	}

	@Override
	public boolean allowRealPath() {
		return instance.allowRealPath();
	}

	@Override
	public lucee.commons.io.res.Resource getClientScopeDir() {
		return instance.getClientScopeDir();
	}

	@Override
	public lucee.runtime.schedule.Scheduler getScheduler() {
		return instance.getScheduler();
	}

	@Override
	public short getCompileType() {
		return instance.getCompileType();
	}

	@Override
	public lucee.commons.io.res.Resource getPhysicalResourceExisting(lucee.runtime.PageContext arg0, lucee.runtime.Mapping[] arg1, java.lang.String arg2, boolean arg3,
			boolean arg4, boolean arg5) {
		return instance.getPhysicalResourceExisting(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public boolean allowLuceeDialect() {
		return instance.allowLuceeDialect();
	}

	@Override
	public java.util.Enumeration getInitParameterNames() {
		return instance.getInitParameterNames();
	}

	@Override
	public boolean getRestList() {
		return instance.getRestList();
	}

	@Override
	public int getCFMLWriterType() {
		return instance.getCFMLWriterType();
	}

	@Override
	public java.lang.ClassLoader getClassLoaderCore() {
		return instance.getClassLoaderCore();
	}

	@Override
	public javax.servlet.ServletContext getServletContext() {
		return instance.getServletContext();
	}

	@Override
	public java.lang.String getSalt() {
		return instance.getSalt();
	}

	public lucee.runtime.PageSource[] getPageSources(lucee.runtime.PageContext arg0, lucee.runtime.Mapping[] arg1, java.lang.String arg2, boolean arg3, boolean arg4, boolean arg5,
			boolean arg6, boolean arg7) {

		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getPageSources(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
		else return ((SingleContextConfigWeb) instance).getPageSources(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	@Override
	public lucee.runtime.Mapping getServerTagMapping(java.lang.String arg0) {
		return instance.getServerTagMapping(arg0);
	}

	@Override
	public lucee.runtime.db.JDBCDriver getJDBCDriverByCD(lucee.runtime.db.ClassDefinition arg0, lucee.runtime.db.JDBCDriver arg1) {
		return instance.getJDBCDriverByCD(arg0, arg1);
	}

	@Override
	public void removeDatasourceConnectionPool(lucee.runtime.db.DataSource arg0) {
		instance.removeDatasourceConnectionPool(arg0);
	}

	@Override
	public lucee.runtime.cache.tag.CacheHandlerCollection getCacheHandlerCollection(int arg0, lucee.runtime.cache.tag.CacheHandlerCollection arg1) {
		return instance.getCacheHandlerCollection(arg0, arg1);
	}

	@Override
	public void setPassword(lucee.runtime.config.Password arg0) {
		instance.setPassword(arg0);
	}

	@Override
	public boolean isUserset() {
		return instance.isUserset();
	}

	@Override
	public lucee.runtime.tag.TagHandlerPool getTagHandlerPool() {
		return instance.getTagHandlerPool();
	}

	@Override
	public java.nio.charset.Charset getWebCharset() {
		return instance.getWebCharset();
	}

	@Override
	public lucee.runtime.type.UDF getFromFunctionCache(java.lang.String arg0) {
		return instance.getFromFunctionCache(arg0);
	}

	@Override
	public lucee.runtime.db.JDBCDriver getJDBCDriverByBundle(java.lang.String arg0, org.osgi.framework.Version arg1, lucee.runtime.db.JDBCDriver arg2) {
		return instance.getJDBCDriverByBundle(arg0, arg1, arg2);
	}

	@Override
	public void checkPermGenSpace(boolean arg0) {
		instance.checkPermGenSpace(arg0);
	}

	@Override
	public java.lang.String getDefaultEncoding() {
		return instance.getDefaultEncoding();
	}

	@Override
	public lucee.runtime.CIPage getBaseComponentPage(lucee.runtime.PageContext arg1) throws lucee.runtime.exp.PageException {
		return instance.getBaseComponentPage(arg1);
	}

	@Override
	public lucee.commons.io.log.Log getLog(java.lang.String arg0, boolean arg1) throws lucee.runtime.exp.PageException {
		return instance.getLog(arg0, arg1);
	}

	@Override
	public boolean isApplicationMapping(lucee.runtime.Mapping arg0) {
		return instance.isApplicationMapping(arg0);
	}

	@Override
	public boolean getSuppressWSBeforeArg() {
		return instance.getSuppressWSBeforeArg();
	}

	@Override
	public java.util.Collection getServerFunctionMappings() {
		return instance.getServerFunctionMappings();
	}

	public void clearComponentMetadata() {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).clearComponentMetadata();
		else((SingleContextConfigWeb) instance).clearComponentMetadata();
	}

	@Override
	public boolean getPSQL() {
		return instance.getPSQL();
	}

	public java.lang.Object[] getResourceLayouts() throws lucee.runtime.exp.PageException {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getResourceLayouts();
		else return ((SingleContextConfigWeb) instance).getResourceLayouts();
	}

	@Override
	public void updatePassword(boolean arg0, java.lang.String arg1, java.lang.String arg2) throws lucee.runtime.exp.PageException, IOException, SAXException, BundleException {
		instance.updatePassword(this, arg0, arg1, arg2);
	}

	@Override
	public lucee.runtime.dump.DumpWriter getDumpWriter(java.lang.String arg0, int arg1) throws PageException {
		return instance.getDumpWriter(arg0, arg1);
	}

	@Override
	public int getLocalMode() {
		return instance.getLocalMode();
	}

	@Override
	public long getQueueTimeout() {
		return instance.getQueueTimeout();
	}

	@Override
	public java.util.Collection getExtensionBundleDefintions() {
		return instance.getExtensionBundleDefintions();
	}

	@Override
	public lucee.runtime.Mapping getApplicationMapping(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2, java.lang.String arg3, boolean arg4, boolean arg5,
			boolean arg6, boolean arg7) {
		return instance.getApplicationMapping(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	@Override
	public boolean getCGIScopeReadonly() {
		return instance.getCGIScopeReadonly();
	}

	@Override
	public boolean getComponentRootSearch() {
		return instance.getComponentRootSearch();
	}

	@Override
	public java.lang.String getCacheDefaultConnectionName(int arg0) {
		return instance.getCacheDefaultConnectionName(arg0);
	}

	@Override
	public java.util.Collection getDatasourceConnectionPools() {
		return instance.getDatasourceConnectionPools();
	}

	@Override
	public lucee.runtime.extension.ExtensionProvider[] getExtensionProviders() {
		return instance.getExtensionProviders();
	}

	public void flushComponentPathCache() {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).flushComponentPathCache();
		else((SingleContextConfigWeb) instance).flushComponentPathCache();
	}

	@Override
	public boolean isMailSpoolEnable() {
		return instance.isMailSpoolEnable();
	}

	@Override
	public java.lang.Boolean getHandleUnQuotedAttrValueAsString() {
		return instance.getHandleUnQuotedAttrValueAsString();
	}

	@Override
	public java.lang.String getBaseComponentTemplate(int arg0) {
		return instance.getBaseComponentTemplate(arg0);
	}

	@Override
	public java.lang.Class getClusterClass() {
		return instance.getClusterClass();
	}

	public java.lang.String createSecurityToken() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).createSecurityToken();
		else return ((SingleContextConfigWeb) instance).createSecurityToken();
	}

	@Override
	public void reset() {
		instance.reset();
	}

	@Override
	public lucee.runtime.dump.DumpWriter getDefaultDumpWriter(int arg0) {
		return instance.getDefaultDumpWriter(arg0);
	}

	@Override
	public lucee.runtime.cfx.CFXTagPool getCFXTagPool() throws PageException {
		return instance.getCFXTagPool();
	}

	public lucee.commons.io.res.Resource getServerConfigDir() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getServerConfigDir();
		else return ((SingleContextConfigWeb) instance).getServerConfigDir();
	}

	@Override
	public void clearFunctionCache() {
		instance.clearFunctionCache();
	}

	@Override
	public lucee.runtime.PageSource[] getPageSources(lucee.runtime.PageContext arg0, lucee.runtime.Mapping[] arg1, java.lang.String arg2, boolean arg3, boolean arg4, boolean arg5,
			boolean arg6) {
		return instance.getPageSources(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	@Override
	public IdentificationWeb getIdentification() {
		return instance.getIdentification();
	}

	@Override
	public java.util.Locale getLocale() {
		return instance.getLocale();
	}

	@Override
	public java.util.Map getCacheConnections() {
		return instance.getCacheConnections();
	}

	public java.lang.String getCacheMD5() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getCacheMD5();
		else return ((SingleContextConfigWeb) instance).getCacheMD5();
	}

	@Override
	public boolean hasIndividualSecurityManager() {
		return instance.hasIndividualSecurityManager(this);
	}

	@Override
	public lucee.commons.io.res.Resource getLibraryDirectory() {
		return instance.getLibraryDirectory();
	}

	@Override
	public boolean isDevelopMode() {
		return instance.isDevelopMode();
	}

	@Override
	public lucee.runtime.db.JDBCDriver[] getJDBCDrivers() {
		return instance.getJDBCDrivers();
	}

	@Override
	public lucee.runtime.rest.RestSettings getRestSetting() {
		return instance.getRestSetting();
	}

	@Override
	public lucee.runtime.db.ClassDefinition getSearchEngineClassDefinition() {
		return instance.getSearchEngineClassDefinition();
	}

	@Override
	public java.lang.Class getVideoExecuterClass() {
		return instance.getVideoExecuterClass();
	}

	@Override
	public boolean closeConnection() {
		return instance.closeConnection();
	}

	@Override
	public lucee.runtime.Mapping getDefaultFunctionMapping() {
		return instance.getDefaultFunctionMapping();
	}

	@Override
	public boolean debugLogOutput() {
		return instance.debugLogOutput();
	}

	@Override
	public void releaseCacheHandlers(lucee.runtime.PageContext arg0) {
		instance.releaseCacheHandlers(arg0);
	}

	@Override
	public boolean allowImplicidQueryCall() {
		return instance.allowImplicidQueryCall();
	}

	@Override
	public boolean limitEvaluation() {
		return instance.limitEvaluation();
	}

	@Override
	public lucee.runtime.customtag.InitFile getCTInitFile(lucee.runtime.PageContext arg0, java.lang.String arg1) {
		return instance.getCTInitFile(arg0, arg1);
	}

	@Override
	public java.lang.ClassLoader getClassLoader() {
		return instance.getClassLoader();
	}

	@Override
	public lucee.runtime.config.DatasourceConnPool getDatasourceConnectionPool(lucee.runtime.db.DataSource arg0, java.lang.String arg1, java.lang.String arg2) {
		return instance.getDatasourceConnectionPool(arg0, arg1, arg2);
	}

	@Override
	public lucee.runtime.db.DataSource getDataSource(java.lang.String arg0, lucee.runtime.db.DataSource arg1) {
		return instance.getDataSource(arg0, arg1);
	}

	@Override
	public lucee.runtime.rest.Mapping[] getRestMappings() {
		return instance.getRestMappings();
	}

	@Override
	public boolean useComponentPathCache() {
		return instance.useComponentPathCache();
	}

	@Override
	public lucee.runtime.orm.ORMEngine resetORMEngine(lucee.runtime.PageContext arg0, boolean arg1) throws lucee.runtime.exp.PageException {
		return instance.resetORMEngine(arg0, arg1);
	}

	@Override
	public lucee.runtime.config.Password isServerPasswordEqual(java.lang.String arg0) {
		return instance.isServerPasswordEqual(arg0);
	}

	@Override
	public lucee.runtime.monitor.IntervallMonitor getIntervallMonitor(java.lang.String arg0) throws lucee.runtime.exp.PageException {
		return instance.getIntervallMonitor(arg0);
	}

	@Override
	public boolean isSuppressContent() {
		return instance.isSuppressContent();
	}

	public ComponentMetaData getComponentMetadata(java.lang.String arg0) {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getComponentMetadata(arg0);
		else return ((SingleContextConfigWeb) instance).getComponentMetadata(arg0);
	}

	@Override
	public void resetBaseComponentPage() {
		instance.resetBaseComponentPage();
	}

	@Override
	public lucee.commons.io.res.Resource getConfigFile() {
		return instance.getConfigFile();
	}

	@Override
	public java.nio.charset.Charset getResourceCharset() {
		return instance.getResourceCharset();
	}

	@Override
	public boolean doLocalCustomTag() {
		return instance.doLocalCustomTag();
	}

	@Override
	public lucee.runtime.PageSource getPageSource(lucee.runtime.Mapping[] arg0, java.lang.String arg1, boolean arg2) {
		return instance.getPageSource(arg0, arg1, arg2);
	}

	public lucee.commons.io.cache.Cache createRAMCache(lucee.runtime.type.Struct arg0) throws java.io.IOException {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).createRAMCache(arg0);
		else return ((SingleContextConfigWeb) instance).createRAMCache(arg0);
	}

	@Override
	public boolean doCustomTagDeepSearch() {
		return instance.doCustomTagDeepSearch();
	}

	@Override
	public lucee.commons.io.res.Resource getConfigServerDir() {
		return instance.getConfigServerDir();
	}

	@Override
	public short getScopeCascadingType() {
		return instance.getScopeCascadingType();
	}

	@Override
	public java.lang.String toString() {
		return instance.toString();
	}

	@Override
	public boolean preserveCase() {
		return instance.preserveCase();
	}

	@Override
	public java.lang.String getUpdateType() {
		return instance.getUpdateType();
	}

	@Override
	public boolean getQueueEnable() {
		return instance.getQueueEnable();
	}

	@Override
	public lucee.runtime.Mapping[] getApplicationMappings() {
		return instance.getApplicationMappings();
	}

	@Override
	public java.lang.String getComponentDumpTemplate() {
		return instance.getComponentDumpTemplate();
	}

	@Override
	public lucee.commons.io.res.Resource getAntiSamyPolicy() {
		return instance.getAntiSamyPolicy();
	}

	@Override
	public lucee.runtime.component.ImportDefintion getComponentDefaultImport() {
		return instance.getComponentDefaultImport();
	}

	@Override
	public lucee.commons.io.res.ResourceProvider[] getResourceProviders() {
		return instance.getResourceProviders();
	}

	@Override
	public lucee.runtime.config.ConfigServer getConfigServer(java.lang.String arg0, long arg1) throws lucee.runtime.exp.PageException {
		return instance.getConfigServer(arg0, arg1);
	}

	@Override
	public java.lang.ClassLoader getRPCClassLoader(boolean arg0, java.lang.ClassLoader[] arg1) throws java.io.IOException {
		return instance.getRPCClassLoader(arg0, arg1);
	}

	@Override
	public lucee.runtime.config.MockPool getDatasourceConnectionPool() {
		return instance.getDatasourceConnectionPool();
	}

	@Override
	public int getServerPasswordType() {
		return instance.getServerPasswordType();
	}

	@Override
	public lucee.runtime.PageSource getBaseComponentPageSource(int arg0) {
		return instance.getBaseComponentPageSource(arg0);
	}

	@Override
	public boolean checkForChangesInConfigFile() {
		return instance.checkForChangesInConfigFile();
	}

	@Override
	public lucee.runtime.PageSource toPageSource(lucee.runtime.Mapping[] arg0, lucee.commons.io.res.Resource arg1, lucee.runtime.PageSource arg2) {
		return instance.toPageSource(arg0, arg1, arg2);
	}

	@Override
	public lucee.runtime.monitor.RequestMonitor[] getRequestMonitors() {
		return instance.getRequestMonitors();
	}

	public void setAllowURLRequestTimeout(boolean arg0) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setAllowURLRequestTimeout(arg0);
		else((SingleContextConfigWeb) instance).setAllowURLRequestTimeout(arg0);
	}

	@Override
	public java.lang.String getHash() {
		return instance.getHash();
	}

	public void updatePassword(boolean arg0, lucee.runtime.config.Password arg1, lucee.runtime.config.Password arg2) throws lucee.runtime.exp.PageException {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).updatePassword(this, arg0, arg1, arg2);
		// TODO what do do here?
	}

	@Override
	public short getInspectTemplate() {
		return instance.getInspectTemplate();
	}

	@Override
	public lucee.runtime.monitor.ActionMonitor getActionMonitor(java.lang.String arg0) throws lucee.runtime.exp.PageException {
		return instance.getActionMonitor(arg0);
	}

	@Override
	public boolean getTypeChecking() {
		return instance.getTypeChecking();
	}

	@Override
	public lucee.runtime.config.RemoteClient[] getRemoteClients() {
		return instance.getRemoteClients();
	}

	@Override
	public boolean getComponentLocalSearch() {
		return instance.getComponentLocalSearch();
	}

	@Override
	public lucee.runtime.regex.Regex getRegex() {
		return instance.getRegex();
	}

	@Override
	public lucee.runtime.type.scope.Cluster createClusterScope() throws lucee.runtime.exp.PageException {
		return instance.createClusterScope();
	}

	@Override
	public lucee.runtime.type.dt.TimeSpan getRequestTimeout() {
		return instance.getRequestTimeout();
	}

	@Override
	public java.lang.String getSerialNumber() {
		return instance.getSerialNumber();
	}

	@Override
	public java.lang.String getInitParameter(java.lang.String arg0) {
		return instance.getInitParameter(arg0);
	}

	protected void setPasswordSource(short arg0) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setPasswordSource(arg0);

	}

	@Override
	public lucee.runtime.config.Password isPasswordEqual(java.lang.String arg0) {
		return instance.isPasswordEqual(arg0);
	}

	@Override
	public lucee.commons.lang.CharSet getWebCharSet() {
		return instance.getWebCharSet();
	}

	@Override
	public lucee.runtime.Mapping getTagMapping(java.lang.String arg0) {
		return instance.getTagMapping(arg0);
	}

	@Override
	public java.lang.String getDefaultDataSource() {
		return instance.getDefaultDataSource();
	}

	@Override
	public java.io.PrintWriter getErrWriter() {
		return instance.getErrWriter();
	}

	@Override
	public boolean isClientCookies() {
		return instance.isClientCookies();
	}

	@Override
	public java.util.Collection getServerTagMappings() {
		return instance.getServerTagMappings();
	}

	@Override
	public int getMailTimeout() {
		return instance.getMailTimeout();
	}

	@Override
	public java.util.Map getAllLabels() {
		return instance.getAllLabels();
	}

	@Override
	public void putCTInitFile(java.lang.String arg0, lucee.runtime.customtag.InitFile arg1) {
		instance.putCTInitFile(arg0, arg1);
	}

	@Override
	public void resetServerFunctionMappings() {
		instance.resetServerFunctionMappings();
	}

	@Override
	public lucee.runtime.Mapping getDefaultTagMapping() {
		return instance.getDefaultTagMapping();
	}

	@Override
	public lucee.runtime.Mapping getDefaultServerTagMapping() {
		return instance.getDefaultServerTagMapping();
	}

	@Override
	public lucee.runtime.lock.LockManager getLockManager() {
		return instance.getLockManager();
	}

	@Override
	public boolean getDefaultFunctionOutput() {
		return instance.getDefaultFunctionOutput();
	}

	@Override
	public boolean isClientManagement() {
		return instance.isClientManagement();
	}

	@Override
	public lucee.runtime.type.dt.TimeSpan getClientTimeout() {
		return instance.getClientTimeout();
	}

	@Override
	public java.util.TimeZone getTimeZone() {
		return instance.getTimeZone();
	}

	@Override
	public boolean getPreciseMath() {
		return instance.getPreciseMath();
	}

	@Override
	public lucee.runtime.type.Struct listCTCache() {
		return instance.listCTCache();
	}

	@Override
	public boolean passwordEqual(lucee.runtime.config.Password arg0) {
		return instance.passwordEqual(arg0);
	}

	@Override
	public boolean doComponentDeepSearch() {
		return instance.doComponentDeepSearch();
	}

	@Override
	public lucee.commons.io.res.Resource getLocalExtensionProviderDirectory() {
		return instance.getLocalExtensionProviderDirectory();
	}

	@Override
	public java.lang.Class getAdminSyncClass() {
		return instance.getAdminSyncClass();
	}

	@Override
	public boolean hasPassword() {
		return instance.hasPassword();
	}

	@Override
	public boolean getDotNotationUpperCase() {
		return instance.getDotNotationUpperCase();
	}

	@Override
	public int getDebugMaxRecordsLogged() {
		return instance.getDebugMaxRecordsLogged();
	}

	@Override
	public java.util.Collection getFunctionMappings() {
		return instance.getFunctionMappings();
	}

	@Override
	public lucee.runtime.net.amf.AMFEngine getAMFEngine() {
		return instance.getAMFEngine();
	}

	@Override
	public lucee.runtime.type.dt.TimeSpan getCachedAfterTimeRange() {
		return instance.getCachedAfterTimeRange();
	}

	@Override
	public lucee.runtime.db.ClassDefinition getCacheDefinition(java.lang.String arg0) {
		return instance.getCacheDefinition(arg0);
	}

	@Override
	public java.util.Iterator getCacheHandlers() {
		return instance.getCacheHandlers();
	}

	@Override
	public java.lang.ClassLoader getClassLoaderEnv() {
		return instance.getClassLoaderEnv();
	}

	@Override
	public boolean getSessionCluster() {
		return instance.getSessionCluster();
	}

	@Override
	public boolean getUseTimeServer() {
		return instance.getUseTimeServer();
	}

	@Override
	public lucee.commons.io.res.Resource getConfigDir() {
		return instance.getConfigDir();
	}

	@Override
	public lucee.runtime.db.DataSource getDataSource(java.lang.String arg0) throws PageException {
		return instance.getDataSource(arg0);
	}

	@Override
	public lucee.runtime.net.rpc.WSHandler getWSHandler() throws lucee.runtime.exp.PageException {
		return instance.getWSHandler();
	}

	@Override
	public lucee.runtime.Mapping getServerFunctionMapping(java.lang.String arg0) {
		return instance.getServerFunctionMapping(arg0);
	}

	@Override
	public lucee.runtime.debug.DebuggerPool getDebuggerPool() {
		return instance.getDebuggerPool();
	}

	@Override
	public boolean mergeFormAndURL() {
		return instance.mergeFormAndURL();
	}

	@Override
	public int getMailSpoolInterval() {
		return instance.getMailSpoolInterval();
	}

	@Override
	public long getLoadTime() {
		return instance.getLoadTime();
	}

	@Override
	public lucee.runtime.net.mail.Server[] getMailServers() {
		return instance.getMailServers();
	}

	@Override
	public lucee.commons.io.res.Resource getPluginDirectory() {
		return instance.getPluginDirectory();
	}

	@Override
	public int getQueueMax() {
		return instance.getQueueMax();
	}

	@Override
	public lucee.runtime.Mapping[] getComponentMappings() {
		return instance.getComponentMappings();
	}

	public lucee.runtime.Mapping getDefaultServerFunctionMapping() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getDefaultServerFunctionMapping();
		else return ((SingleContextConfigWeb) instance).getDefaultServerFunctionMapping();
	}

	@Override
	public java.lang.String getErrorTemplate(int arg0) {
		return instance.getErrorTemplate(arg0);
	}

	@Override
	public lucee.runtime.PageSource[] getPageSources(lucee.runtime.PageContext arg0, lucee.runtime.Mapping[] arg1, java.lang.String arg2, boolean arg3, boolean arg4,
			boolean arg5) {
		return instance.getPageSources(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public Resource[] getResources(lucee.runtime.PageContext arg0, lucee.runtime.Mapping[] arg1, java.lang.String arg2, boolean arg3, boolean arg4, boolean arg5, boolean arg6,
			boolean arg7) {
		return instance.getResources(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
	}

	@Override
	public lucee.runtime.engine.ExecutionLogFactory getExecutionLogFactory() {
		return instance.getExecutionLogFactory();
	}

	@Override
	public boolean contentLength() {
		return instance.contentLength();
	}

	public void flushApplicationPathCache() {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).flushApplicationPathCache();
		else((SingleContextConfigWeb) instance).flushApplicationPathCache();
	}

	@Override
	public boolean isMailSendPartial() {
		return instance.isMailSendPartial();
	}

	@Override
	public lucee.commons.io.res.util.ResourceClassLoader getResourceClassLoader(lucee.commons.io.res.util.ResourceClassLoader arg0) {
		return instance.getResourceClassLoader(arg0);
	}

	public lucee.runtime.config.ConfigServerImpl getConfigServerImpl() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getConfigServerImpl();
		return ((SingleContextConfigWeb) instance).getConfigServerImpl();
	}

	@Override
	public lucee.runtime.net.proxy.ProxyData getProxyData() {
		return instance.getProxyData();
	}

	@Override
	public lucee.commons.io.log.Log getLog(java.lang.String arg0) {
		return instance.getLog(arg0);
	}

	@Override
	public java.lang.String getTimeServer() {
		return instance.getTimeServer();
	}

	@Override
	public boolean allowRequestTimeout() {
		return instance.allowRequestTimeout();
	}

	public void createTag(lucee.transformer.library.tag.TagLib arg0, java.lang.String arg1, java.lang.String arg2) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).createTag(arg0, arg1, arg2);
		else((SingleContextConfigWeb) instance).createTag(arg0, arg1, arg2);
	}

	public lucee.commons.lang.CharSet getTemplateCharSet() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getTemplateCharSet();
		else return ((SingleContextConfigWeb) instance).getTemplateCharSet();
	}

	@Override
	public lucee.commons.lock.KeyLock getContextLock() {
		return instance.getContextLock();
	}

	@Override
	public lucee.runtime.type.dt.TimeSpan getApplicationTimeout() {
		return instance.getApplicationTimeout();
	}

	@Override
	public java.lang.String getSessionStorage() {
		return instance.getSessionStorage();
	}

	@Override
	public lucee.runtime.config.ConfigServer getConfigServer(String password) throws lucee.runtime.exp.PageException {
		return instance.getConfigServer(this, password);
	}

	public void setCacheMD5(java.lang.String arg0) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setCacheMD5(arg0);

	}

	@Override
	public lucee.runtime.spooler.SpoolerEngine getSpoolerEngine() {
		return instance.getSpoolerEngine();
	}

	@Override
	public lucee.commons.io.res.Resource getEventGatewayDirectory() {
		return instance.getEventGatewayDirectory();
	}

	@Override
	public boolean debug() {
		return instance.debug();
	}

	@Override
	public lucee.runtime.Mapping getFunctionMapping(java.lang.String arg0) {
		return instance.getFunctionMapping(arg0);
	}

	@Override
	public java.util.Collection getTagMappings() {
		return instance.getTagMappings();
	}

	@Override
	public lucee.commons.io.res.type.compress.Compress getCompressInstance(lucee.commons.io.res.Resource arg0, int arg1, boolean arg2) throws java.io.IOException {
		return instance.getCompressInstance(arg0, arg1, arg2);
	}

	@Override
	public lucee.commons.io.res.Resource getTempDirectory() {
		return instance.getTempDirectory();
	}

	@Override
	public lucee.runtime.type.Struct listComponentCache() {
		return instance.listComponentCache();
	}

	@Override
	public void putToFunctionCache(java.lang.String arg0, lucee.runtime.type.UDF arg1) {
		instance.putToFunctionCache(arg0, arg1);
	}

	@Override
	public int getLoginDelay() {
		return instance.getLoginDelay();
	}

	@Override
	public java.util.Map getDataSourcesAsMap() {
		return instance.getDataSourcesAsMap();
	}

	public void flushCTPathCache() {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).flushCTPathCache();
		else((SingleContextConfigWeb) instance).flushCTPathCache();
	}

	@Override
	public lucee.runtime.CIPage getCachedPage(lucee.runtime.PageContext arg0, java.lang.String arg1) throws lucee.runtime.exp.TemplateException {
		return instance.getCachedPage(arg0, arg1);
	}

	@Override
	public java.util.Map getTagDefaultAttributeValues() {
		return instance.getTagDefaultAttributeValues();
	}

	@Override
	public lucee.runtime.config.ConfigServer getConfigServer(lucee.runtime.config.Password arg0) throws lucee.runtime.exp.PageException {
		return instance.getConfigServer(arg0);
	}

	@Override
	public void clearApplicationCache() {
		instance.clearApplicationCache();
	}

	@Override
	public java.util.Map getCacheDefinitions() {
		return instance.getCacheDefinitions();
	}

	@Override
	public boolean isExtensionEnabled() {
		return instance.isExtensionEnabled();
	}

	@Override
	public short getPasswordSource() {
		return instance.getPasswordSource();
	}

	@Override
	public lucee.runtime.db.JDBCDriver getJDBCDriverByClassName(java.lang.String arg0, lucee.runtime.db.JDBCDriver arg1) {
		return instance.getJDBCDriverByClassName(arg0, arg1);
	}

	@Override
	public void clearCTCache() {
		instance.clearCTCache();
	}

	public void putComponentMetadata(java.lang.String arg0, ComponentMetaData arg1) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).putComponentMetadata(arg0, arg1);
		else((SingleContextConfigWeb) instance).putComponentMetadata(arg0, arg1);
	}

	@Override
	public boolean isAllowURLRequestTimeout() {
		return instance.isAllowURLRequestTimeout();
	}

	@Override
	public java.util.Map getStartups() {
		return instance.getStartups();
	}

	@Override
	public java.lang.String getLabel() {
		return instance.getLabel();
	}

	public java.lang.String[] getLogNames() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getLogNames();
		else return ((SingleContextConfigWeb) instance).getLogNames();
	}

	@Override
	public lucee.commons.io.res.Resource getClassesDirectory() {
		return instance.getClassesDirectory();
	}

	@Override
	public lucee.runtime.extension.Extension[] getExtensions() {
		return instance.getExtensions();
	}

	@Override
	public long getTimeServerOffset() {
		return instance.getTimeServerOffset();
	}

	@Override
	public int getQueryVarUsage() {
		return instance.getQueryVarUsage();
	}

	@Override
	public lucee.commons.io.res.util.ResourceClassLoader getResourceClassLoader() {
		return instance.getResourceClassLoader();
	}

	@Override
	public ResourceProviderFactory[] getResourceProviderFactories() {
		return instance.getResourceProviderFactories();
	}

	@Override
	public long lastModified() {
		return instance.lastModified();
	}

	@Override
	public lucee.runtime.compiler.CFMLCompilerImpl getCompiler() {
		return instance.getCompiler();
	}

	@Override
	public boolean isMonitoringEnabled() {
		return instance.isMonitoringEnabled();
	}

	@Override
	public boolean hasDebugOptions(int arg0) {
		return instance.hasDebugOptions(arg0);
	}

	@Override
	public java.lang.String getClientStorage() {
		return instance.getClientStorage();
	}

	@Override
	public lucee.runtime.cache.CacheConnection getCacheDefaultConnection(int arg0) {
		return instance.getCacheDefaultConnection(arg0);
	}

	@Override
	public void putApplicationPageSource(java.lang.String arg0, lucee.runtime.PageSource arg1, java.lang.String arg2, int arg3, boolean arg4) {
		instance.putApplicationPageSource(arg0, arg1, arg2, arg3, arg4);
	}

	protected void setAMFEngine(lucee.runtime.net.amf.AMFEngine arg0) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setAMFEngine(arg0);

	}

	@Override
	public boolean getBufferOutput() {
		return instance.getBufferOutput();
	}

	@Override
	public void reloadTimeServerOffset() {
		instance.reloadTimeServerOffset();
	}

	@Override
	public boolean isSuppressWhitespace() {
		return instance.isSuppressWhitespace();
	}

	@Override
	public lucee.runtime.search.SearchEngine getSearchEngine(lucee.runtime.PageContext arg0) throws lucee.runtime.exp.PageException {
		return instance.getSearchEngine(arg0);
	}

	@Override
	public lucee.runtime.Mapping[] getCustomTagMappings() {
		return instance.getCustomTagMappings();
	}

	@Override
	public lucee.runtime.config.Password updatePasswordIfNecessary(boolean arg0, java.lang.String arg1) {
		return instance.updatePasswordIfNecessary(arg0, arg1);
	}

	@Override
	public java.util.List loadLocalExtensions(boolean arg0) {
		return instance.loadLocalExtensions(arg0);
	}

	@Override
	public boolean isShowVersion() {
		return instance.isShowVersion();
	}

	@Override
	public lucee.commons.lang.CharSet getResourceCharSet() {
		return instance.getResourceCharSet();
	}

	@Override
	public java.net.URL getUpdateLocation() {
		return instance.getUpdateLocation();
	}

	@Override
	public lucee.runtime.extension.RHExtension[] getServerRHExtensions() {
		return instance.getServerRHExtensions();
	}

	@Override
	public java.lang.String getServerPasswordSalt() {
		return instance.getServerPasswordSalt();
	}

	protected void setGatewayEntries(GatewayMap entries) {
		// TODO i think that method is never used
		if (instance instanceof ConfigImpl) ((ConfigImpl) instance).setGatewayEntries(entries);
	}

	@Override
	public lucee.runtime.orm.ORMEngine getORMEngine(lucee.runtime.PageContext arg0) throws lucee.runtime.exp.PageException {
		return instance.getORMEngine(arg0);
	}

	public long getSessionScopeDirSize() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getSessionScopeDirSize();
		else return ((SingleContextConfigWeb) instance).getSessionScopeDirSize();
	}

	@Override
	public int getPasswordOrigin() {
		return instance.getPasswordOrigin();
	}

	public void clearResourceProviders() {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).clearResourceProviders();

	}

	@Override
	public lucee.runtime.db.ClassDefinition getORMEngineClassDefintion() {
		return instance.getORMEngineClassDefintion();
	}

	@Override
	public lucee.commons.io.res.Resource getSessionScopeDir() {
		return instance.getSessionScopeDir();
	}

	@Override
	public lucee.commons.io.res.Resource getRemoteClientDirectory() {
		return instance.getRemoteClientDirectory();
	}

	@Override
	public java.lang.String getServletName() {
		return instance.getServletName();
	}

	@Override
	public lucee.commons.io.res.Resource getResource(java.lang.String arg0) {
		return instance.getResource(arg0);
	}

	@Override
	public boolean getClientCluster() {
		return instance.getClientCluster();
	}

	@Override
	public lucee.transformer.library.tag.TagLib[] getTLDs() {
		return instance.getTLDs();
	}

	public lucee.runtime.Mapping getApplicationMapping(java.lang.String arg0, java.lang.String arg1) {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getApplicationMapping(arg0, arg1);
		else return ((SingleContextConfigWeb) instance).getApplicationMapping(arg0, arg1);
	}

	@Override
	public lucee.commons.io.res.Resource getRootDirectory() {
		return instance.getRootDirectory();
	}

	@Override
	public void clearComponentCache() {
		instance.clearComponentCache();
	}

	@Override
	public java.nio.charset.Charset getTemplateCharset() {
		return instance.getTemplateCharset();
	}

	public int getMode() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getMode();
		else return ((SingleContextConfigWeb) instance).getMode();
	}

	@Override
	public lucee.runtime.Mapping[] getMappings() {
		return instance.getMappings();
	}

	@Override
	public lucee.commons.io.res.Resource getVideoDirectory() {
		return instance.getVideoDirectory();
	}

	@Override
	public lucee.commons.io.log.LogEngine getLogEngine() {
		return instance.getLogEngine();
	}

	@Override
	public java.lang.String[] getCustomTagExtensions() {
		return instance.getCustomTagExtensions();
	}

	@Override
	public lucee.runtime.extension.RHExtensionProvider[] getRHExtensionProviders() {
		return instance.getRHExtensionProviders();
	}

	public void setCacheDefinitions(java.util.Map arg0) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setCacheDefinitions(arg0);
	}

	@Override
	public boolean isDefaultPassword() {
		return instance.isDefaultPassword();
	}

	@Override
	public lucee.runtime.orm.ORMConfiguration getORMConfig() {
		return instance.getORMConfig();
	}

	@Override
	public lucee.runtime.db.JDBCDriver getJDBCDriverById(java.lang.String arg0, lucee.runtime.db.JDBCDriver arg1) {
		return instance.getJDBCDriverById(arg0, arg1);
	}

	@Override
	public java.lang.Object getCachedWithin(int arg0) {
		return instance.getCachedWithin(arg0);
	}

	@Override
	public lucee.runtime.Mapping getApplicationMapping(java.lang.String arg0, java.lang.String arg1, java.lang.String arg2, java.lang.String arg3, boolean arg4, boolean arg5) {
		return instance.getApplicationMapping(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public int getPasswordType() {
		return instance.getPasswordType();
	}

	public lucee.runtime.db.ClassDefinition getORMEngineClass() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getORMEngineClass();
		else return ((SingleContextConfigWeb) instance).getORMEngineClass();
	}

	@Override
	public lucee.runtime.monitor.RequestMonitor getRequestMonitor(java.lang.String arg0) throws lucee.runtime.exp.PageException {
		return instance.getRequestMonitor(arg0);
	}

	@Override
	public boolean getExecutionLogEnabled() {
		return instance.getExecutionLogEnabled();
	}

	@Override
	public javax.servlet.jsp.JspWriter getWriter(lucee.runtime.PageContext arg0, javax.servlet.http.HttpServletRequest arg1, javax.servlet.http.HttpServletResponse arg2) {
		return instance.getWriter(arg0, arg1, arg2);
	}

	@Override
	public boolean allowCompression() {
		return instance.allowCompression();
	}

	@Override
	public lucee.runtime.config.DebugEntry getDebugEntry(java.lang.String arg0, lucee.runtime.config.DebugEntry arg1) {
		return instance.getDebugEntry(arg0, arg1);
	}

	public void createFunction(lucee.transformer.library.function.FunctionLib arg0, java.lang.String arg1, java.lang.String arg2) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).createFunction(arg0, arg1, arg2);

	}

	@Override
	public lucee.runtime.security.SecurityManager getSecurityManager() {
		return instance.getSecurityManager();
	}

	@Override
	public lucee.runtime.writer.CFMLWriter getCFMLWriter(lucee.runtime.PageContext arg0, javax.servlet.http.HttpServletRequest arg1, javax.servlet.http.HttpServletResponse arg2) {
		return instance.getCFMLWriter(arg0, arg1, arg2);
	}

	@Override
	public lucee.runtime.CFMLFactory getFactory() {
		return instance.getFactory();
	}

	@Override
	public lucee.commons.io.res.Resource getClassDirectory() {
		return instance.getClassDirectory();
	}

	public lucee.runtime.dump.DumpWriterEntry[] getDumpWritersEntries() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getDumpWritersEntries();
		else return ((SingleContextConfigWeb) instance).getDumpWritersEntries();
	}

	protected void setSecurityManager(lucee.runtime.security.SecurityManager arg0) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setSecurityManager(arg0);
	}

	@Override
	public java.util.Collection getAllExtensionBundleDefintions() {
		return instance.getAllExtensionBundleDefintions();
	}

	@Override
	public lucee.runtime.db.DataSource[] getDataSources() {
		return instance.getDataSources();
	}

	@Override
	public lucee.commons.io.res.Resource getLogDirectory() {
		return instance.getLogDirectory();
	}

	@Override
	public boolean hasServerPassword() {
		return instance.hasServerPassword();
	}

	@Override
	public lucee.runtime.type.Struct getRemoteClientUsage() {
		return instance.getRemoteClientUsage();
	}

	@Override
	public java.lang.ClassLoader getRPCClassLoader(boolean arg0) throws java.io.IOException {
		return instance.getRPCClassLoader(arg0);
	}

	@Override
	public java.util.Collection getAllRHExtensions() {
		return instance.getAllRHExtensions();
	}

	public void setAllowLuceeDialect(boolean arg0) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setAllowLuceeDialect(arg0);
	}

	@Override
	public long getClientScopeDirSize() {
		return instance.getClientScopeDirSize();
	}

	@Override
	public boolean useComponentShadow() {
		return instance.useComponentShadow();
	}

	@Override
	public lucee.runtime.PageSource getPageSourceExisting(lucee.runtime.PageContext arg0, lucee.runtime.Mapping[] arg1, java.lang.String arg2, boolean arg3, boolean arg4,
			boolean arg5, boolean arg6) {
		return instance.getPageSourceExisting(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	@Override
	public boolean getRememberMe() {
		return instance.getRememberMe();
	}

	@Override
	public int getExternalizeStringGTE() {
		return instance.getExternalizeStringGTE();
	}

	@Override
	public lucee.runtime.config.AdminSync getAdminSync() throws lucee.commons.lang.ClassException {
		return instance.getAdminSync();
	}

	@Override
	public lucee.runtime.monitor.ActionMonitorCollector getActionMonitorCollector() {
		return instance.getActionMonitorCollector();
	}

	@Override
	public void putCachedPageSource(java.lang.String arg0, lucee.runtime.PageSource arg1) {
		instance.putCachedPageSource(arg0, arg1);
	}

	@Override
	public boolean getLoginCaptcha() {
		return instance.getLoginCaptcha();
	}

	@Override
	public boolean getErrorStatusCode() {
		return instance.getErrorStatusCode();
	}

	@Override
	public lucee.runtime.type.dt.TimeSpan getSessionTimeout() {
		return instance.getSessionTimeout();
	}

	@Override
	public lucee.commons.io.res.Resource getCacheDir() {
		return instance.getCacheDir();
	}

	@Override
	public void checkPassword() throws lucee.runtime.exp.PageException {
		instance.checkPassword();
	}

	@Override
	public int getServerPasswordOrigin() {
		return instance.getServerPasswordOrigin();
	}

	@Override
	public lucee.runtime.type.Struct getConstants() {
		return instance.getConstants();
	}

	@Override
	public boolean getTriggerComponentDataMember() {
		return instance.getTriggerComponentDataMember();
	}

	@Override
	public lucee.runtime.extension.RHExtension[] getRHExtensions() {
		return instance.getRHExtensions();
	}

	@Override
	public java.util.Map getLoggers() {
		return instance.getLoggers();
	}

	@Override
	public boolean isDomainCookies() {
		return instance.isDomainCookies();
	}

	@Override
	public lucee.transformer.library.tag.TagLib getCoreTagLib() {
		return instance.getCoreTagLib();
	}

	protected void setMode(int mode) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setMode(mode);
	}

	protected void setSalt(String salt) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setSalt(salt);
	}

	protected void setCheckForChangesInConfigFile(boolean checkForChangesInConfigFile) {
		if (instance instanceof MultiContextConfigWeb) ((MultiContextConfigWeb) instance).setCheckForChangesInConfigFile(checkForChangesInConfigFile);
	}

	public ConfigWebPro getInstance() {
		return instance;
	}

	public ConfigWebImpl setInstance(ConfigWebInner instance) {
		if (this.instance != null) {
			this.instance.reset();
		}
		this.instance = instance;
		return this;
	}

	@Override
	public boolean isSingle() {
		return instance.isSingle();
	}

	@Override
	public Resource getWebConfigDir() {
		return instance.getWebConfigDir();
	}

	public Password getPassword() {
		if (instance instanceof MultiContextConfigWeb) return ((MultiContextConfigWeb) instance).getPassword();
		else return ((SingleContextConfigWeb) instance).getPassword();
	}

	@Override
	public ServletConfig getServletConfig() {
		return instance.getServletConfig();
	}

	@Override
	public void setLastModified() {
		instance.setLastModified();
	}

	@Override
	public void checkMappings() {
		instance.checkMappings();
	}

	@Override
	public String getMainLogger() {
		return instance.getMainLogger();
	}

	@Override
	public int getInspectTemplateAutoInterval(boolean slow) {
		return instance.getInspectTemplateAutoInterval(slow);
	}
}
