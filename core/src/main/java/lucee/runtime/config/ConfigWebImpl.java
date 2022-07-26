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

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.lock.KeyLock;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.CIPage;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheHandlerCollection;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.compiler.CFMLCompilerImpl;
import lucee.runtime.debug.DebuggerPool;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.gateway.GatewayEngine;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.lock.LockManager;
import lucee.runtime.monitor.ActionMonitor;
import lucee.runtime.monitor.ActionMonitorCollector;
import lucee.runtime.monitor.IntervallMonitor;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.net.amf.AMFEngine;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.tag.TagHandlerPool;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.writer.CFMLWriter;

/**
 * Web Context
 */
public class ConfigWebImpl extends ConfigImpl implements ServletConfig, ConfigWebPro {

	private final ServletConfig config;
	private final ConfigServerImpl configServer;
	private SecurityManager securityManager;

	private Resource rootDir;

	private final CFMLFactoryImpl factory;

	private final ConfigWebHelper helper;
	private short passwordSource;

	// private File deployDirectory;

	/**
	 * constructor of the class
	 * 
	 * @param configServer
	 * @param config
	 * @param configDir
	 * @param configFile
	 * @param cloneServer
	 */
	ConfigWebImpl(CFMLFactoryImpl factory, ConfigServerImpl configServer, ServletConfig config, Resource configDir, Resource configFile) {
		super(configDir, configFile);
		this.configServer = configServer;
		this.config = config;
		this.factory = factory;
		factory.setConfig(this);
		ResourceProvider frp = ResourcesImpl.getFileResourceProvider();

		this.rootDir = frp.getResource(ReqRspUtil.getRootPath(config.getServletContext()));

		// Fix for tomcat
		if (this.rootDir.getName().equals(".") || this.rootDir.getName().equals("..")) this.rootDir = this.rootDir.getParentResource();
		helper = new ConfigWebHelper(configServer, this);
	}

	@Override
	public void reset() {
		super.reset();
		factory.resetPageContext();
		helper.reset();
	}

	@Override
	public String getServletName() {
		return config.getServletName();
	}

	@Override
	public ServletContext getServletContext() {
		return config.getServletContext();
	}

	@Override
	public String getInitParameter(String name) {
		return config.getInitParameter(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return config.getInitParameterNames();
	}

	protected ConfigServerImpl getConfigServerImpl() {
		return configServer;
	}

	@Override
	public ConfigServer getConfigServer(String password) throws ExpressionException {
		Password pw = isServerPasswordEqual(password);
		if (pw == null) pw = PasswordImpl.passwordToCompare(this, true, password);
		return getConfigServer(pw);
	}

	@Override
	public ConfigServer getConfigServer(Password password) throws ExpressionException {
		configServer.checkAccess(password);
		return configServer;
	}

	@Override
	public ConfigServer getConfigServer(String key, long timeNonce) throws PageException {
		configServer.checkAccess(key, timeNonce);
		return configServer;
	}

	public Resource getServerConfigDir() {
		return configServer.getConfigDir();
	}

	/**
	 * @return Returns the accessor.
	 */
	@Override
	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	/**
	 * @param securityManager The accessor to set.
	 */
	protected void setSecurityManager(SecurityManager securityManager) {
		((SecurityManagerImpl) securityManager).setRootDirectory(getRootDirectory());
		this.securityManager = securityManager;
	}

	@Override
	public CFXTagPool getCFXTagPool() throws SecurityException {
		if (securityManager.getAccess(SecurityManager.TYPE_CFX_USAGE) == SecurityManager.VALUE_YES) return super.getCFXTagPool();
		throw new SecurityException("no access to cfx functionality", "disabled by security settings");
	}

	/**
	 * @return Returns the rootDir.
	 */
	@Override
	public Resource getRootDirectory() {
		return rootDir;
	}

	@Override
	public String getUpdateType() {
		return configServer.getUpdateType();
	}

	@Override
	public URL getUpdateLocation() {
		return configServer.getUpdateLocation();
	}

	@Override
	public LockManager getLockManager() {
		return helper.getLockManager();
	}

	/**
	 * @return the compiler
	 */
	@Override
	public CFMLCompilerImpl getCompiler() {
		return helper.getCompiler();
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
	public Collection<Mapping> getServerTagMappings() {
		return helper.getServerTagMappings();
	}

	@Override
	public Mapping getDefaultServerTagMapping() {
		return getConfigServerImpl().defaultTagMapping;
	}

	@Override
	public Mapping getServerTagMapping(String mappingName) {
		return helper.getServerTagMapping(mappingName);
	}

	@Override
	public Collection<Mapping> getServerFunctionMappings() {
		return helper.getServerFunctionMappings();
	}

	@Override
	public Mapping getServerFunctionMapping(String mappingName) {
		return helper.getServerFunctionMapping(mappingName);
	}

	public Mapping getDefaultServerFunctionMapping() {
		return getConfigServerImpl().defaultFunctionMapping;
	}

	// FYI used by Extensions, do not remove
	public Mapping getApplicationMapping(String virtual, String physical) {
		return getApplicationMapping("application", virtual, physical, null, true, false);
	}

	@Override
	public boolean isApplicationMapping(Mapping mapping) {
		return helper.isApplicationMapping(mapping);
	}

	@Override
	public Mapping getApplicationMapping(String type, String virtual, String physical, String archive, boolean physicalFirst, boolean ignoreVirtual) {
		return getApplicationMapping(type, virtual, physical, archive, physicalFirst, ignoreVirtual, true, true);
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
	public String getLabel() {
		return helper.getLabel();
	}

	@Override
	public String getHash() {
		return SystemUtil.hash(getServletContext());
	}

	@Override
	public KeyLock<String> getContextLock() {
		return helper.getContextLock();
	}

	@Override
	public Map<String, GatewayEntry> getGatewayEntries() {
		return helper.getGatewayEngineImpl().getEntries();
	}

	@Override
	protected void setGatewayEntries(Map<String, GatewayEntry> gatewayEntries) {
		try {
			helper.getGatewayEngineImpl().addEntries(this, gatewayEntries);
		}
		catch (Exception e) {
			LogUtil.log(ThreadLocalPageContext.getConfig(this), ConfigWebImpl.class.getName(), e);
		}
	}

	@Override
	public GatewayEngine getGatewayEngine() {
		return helper.getGatewayEngineImpl();
	}

	@Override
	public TagHandlerPool getTagHandlerPool() {
		return helper.getTagHandlerPool();
	}

	@Override
	public DebuggerPool getDebuggerPool() {
		return helper.getDebuggerPool();
	}

	@Override
	public ThreadQueue getThreadQueue() {
		return configServer.getThreadQueue();
	}

	@Override
	public int getLoginDelay() {
		return configServer.getLoginDelay();
	}

	@Override
	public boolean getLoginCaptcha() {
		return configServer.getLoginCaptcha();
	}

	@Override
	public boolean getRememberMe() {
		return configServer.getRememberMe();
	}

	@Override
	public Resource getSecurityDirectory() {
		return configServer.getSecurityDirectory();
	}

	@Override
	public boolean isMonitoringEnabled() {
		return configServer.isMonitoringEnabled();
	}

	@Override
	public RequestMonitor[] getRequestMonitors() {
		return configServer.getRequestMonitors();
	}

	@Override
	public RequestMonitor getRequestMonitor(String name) throws PageException {
		return configServer.getRequestMonitor(name);
	}

	@Override
	public IntervallMonitor[] getIntervallMonitors() {
		return configServer.getIntervallMonitors();
	}

	@Override
	public IntervallMonitor getIntervallMonitor(String name) throws PageException {
		return configServer.getIntervallMonitor(name);
	}

	@Override
	public void checkPermGenSpace(boolean check) {
		configServer.checkPermGenSpace(check);
	}

	@Override
	public Cluster createClusterScope() throws PageException {
		return configServer.createClusterScope();
	}

	@Override
	public boolean hasServerPassword() {
		return configServer.hasPassword();
	}

	@Override
	public void updatePassword(boolean server, String passwordOld, String passwordNew) throws PageException {
		try {
			PasswordImpl.updatePassword(server ? configServer : this, passwordOld, passwordNew);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public void updatePassword(boolean server, Password passwordOld, Password passwordNew) throws PageException {
		try {
			PasswordImpl.updatePassword(server ? configServer : this, passwordOld, passwordNew);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Password updatePasswordIfNecessary(boolean server, String passwordRaw) {
		ConfigPro config = server ? configServer : this;
		return PasswordImpl.updatePasswordIfNecessary(config, ((ConfigImpl) config).password, passwordRaw);
	}

	@Override
	public Resource getConfigServerDir() {
		return configServer.getConfigDir();
	}

	@Override
	public Map<String, String> getAllLabels() {
		return configServer.getLabels();
	}

	@Override
	public boolean allowRequestTimeout() {
		return configServer.allowRequestTimeout();
	}

	@Override
	public CFMLWriter getCFMLWriter(PageContext pc, HttpServletRequest req, HttpServletResponse rsp) {
		return helper.getCFMLWriter(pc, req, rsp);
	}

	@Override
	public JspWriter getWriter(PageContext pc, HttpServletRequest req, HttpServletResponse rsp) {
		return getCFMLWriter(pc, req, rsp);
	}

	@Override
	public ActionMonitorCollector getActionMonitorCollector() {
		return configServer.getActionMonitorCollector();
	}

	@Override
	public boolean hasIndividualSecurityManager() {
		return helper.hasIndividualSecurityManager(this);
	}

	@Override
	public CFMLFactory getFactory() {
		return factory;
	}

	@Override
	public CacheHandlerCollection getCacheHandlerCollection(int type, CacheHandlerCollection defaultValue) {
		return helper.getCacheHandlerCollection(type, defaultValue);
	}

	@Override
	public void releaseCacheHandlers(PageContext pc) {
		helper.releaseCacheHandlers(pc);
	}

	protected void setIdentification(IdentificationWeb id) {
		helper.setIdentification(id);
	}

	@Override
	public IdentificationWeb getIdentification() {
		return helper.getIdentification();
	}

	@Override
	public int getServerPasswordType() {
		return configServer.getPasswordType();
	}

	@Override
	public String getServerPasswordSalt() {
		return configServer.getPasswordSalt();
	}

	@Override
	public int getServerPasswordOrigin() {
		return configServer.getPasswordOrigin();
	}

	public String getServerSalt() {
		return configServer.getSalt();
	}

	@Override
	public Password isServerPasswordEqual(String password) {
		return configServer.isPasswordEqual(password);
	}

	@Override
	public boolean isDefaultPassword() {
		if (password == null) return false;
		return password == configServer.defaultPassword;
	}

	@Override
	public Collection<BundleDefinition> getAllExtensionBundleDefintions() {
		return configServer.getAllExtensionBundleDefintions();
	}

	@Override
	public Collection<RHExtension> getAllRHExtensions() {
		return configServer.getAllRHExtensions();
	}

	@Override
	public SearchEngine getSearchEngine(PageContext pc) throws PageException {
		return helper.getSearchEngine(pc);
	}

	@Override
	public ActionMonitor getActionMonitor(String name) {
		return configServer.getActionMonitor(name);
	}

	@Override
	public Resource getLocalExtensionProviderDirectory() {
		return configServer.getLocalExtensionProviderDirectory();
	}

	protected void setAMFEngine(AMFEngine engine) {
		helper.setAMFEngine(engine);
	}

	@Override
	public AMFEngine getAMFEngine() {
		return helper.getAMFEngine();
	}

	/*
	 * public boolean installServerExtension(ExtensionDefintion ed) throws PageException { return
	 * configServer.installExtension(ed); }
	 */

	@Override
	public RHExtension[] getServerRHExtensions() {
		return configServer.getRHExtensions();
	}

	@Override
	public List<ExtensionDefintion> loadLocalExtensions(boolean validate) {
		return configServer.loadLocalExtensions(validate);
	}

	@Override
	public WSHandler getWSHandler() throws PageException {
		return helper.getWSHandler();
	}

	protected void setPasswordSource(short passwordSource) {
		this.passwordSource = passwordSource;
	}

	@Override
	public short getPasswordSource() {
		return passwordSource;
	}

	@Override
	public void checkPassword() throws PageException {
		configServer.checkPassword();
	}

	@Override
	public short getAdminMode() {
		return configServer.getAdminMode();
	}
}