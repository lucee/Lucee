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

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.osgi.framework.BundleException;
import org.xml.sax.SAXException;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lock.KeyLock;
import lucee.commons.lock.KeyLockImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.CIPage;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheHandlerCollection;
import lucee.runtime.cache.tag.CacheHandlerCollections;
import lucee.runtime.cfx.CFXTagPool;
import lucee.runtime.compiler.CFMLCompilerImpl;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.debug.DebuggerPool;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.lock.LockManager;
import lucee.runtime.lock.LockManagerImpl;
import lucee.runtime.monitor.ActionMonitor;
import lucee.runtime.monitor.ActionMonitorCollector;
import lucee.runtime.monitor.IntervallMonitor;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.net.amf.AMFEngine;
import lucee.runtime.net.amf.AMFEngineDummy;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.rpc.DummyWSHandler;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.net.rpc.ref.WSHandlerReflector;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.tag.TagHandlerPool;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.writer.CFMLWriter;
import lucee.runtime.writer.CFMLWriterImpl;
import lucee.runtime.writer.CFMLWriterWS;
import lucee.runtime.writer.CFMLWriterWSPref;

/**
 * Web Context
 */
public final class ConfigWebImpl extends ConfigImpl implements ServletConfig, ConfigWeb {

	private final ServletConfig config;
	private final ConfigServerImpl configServer;
	private SecurityManager securityManager;
	private static final LockManager lockManager = LockManagerImpl.getInstance(false);
	public static final short PASSWORD_ORIGIN_DEFAULT = 1;
	public static final short PASSWORD_ORIGIN_SERVER = 2;
	public static final short PASSWORD_ORIGIN_WEB = 3;
	private Resource rootDir;
	private final CFMLCompilerImpl compiler = new CFMLCompilerImpl();
	private CIPage baseComponentPageCFML;
	private CIPage baseComponentPageLucee;

	private Map<String, Mapping> serverTagMappings;
	private Map<String, Mapping> serverFunctionMappings;

	private KeyLock<String> contextLock = new KeyLockImpl<String>();
	private GatewayEngineImpl gatewayEngine;
	private DebuggerPool debuggerPool;
	private final CFMLFactoryImpl factory;
	private CacheHandlerCollections cacheHandlerCollections;

	protected IdentificationWeb id;

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
	}

	@Override
	public void reset() {
		super.reset();
		factory.resetPageContext();
		tagHandlerPool.reset();
		contextLock = new KeyLockImpl<String>();
		baseComponentPageCFML = null;
		baseComponentPageLucee = null;
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
		return lockManager;
	}

	/**
	 * @return the compiler
	 */
	public CFMLCompilerImpl getCompiler() {
		return compiler;
	}

	public CIPage getBaseComponentPage(int dialect, PageContext pc) throws PageException {
		// CFML
		if (dialect == CFMLEngine.DIALECT_CFML) {
			if (baseComponentPageCFML == null) {
				baseComponentPageCFML = (CIPage) getBaseComponentPageSource(dialect, pc).loadPage(pc, false);
			}
			return baseComponentPageCFML;
		}
		// Lucee
		if (baseComponentPageLucee == null) {
			baseComponentPageLucee = (CIPage) getBaseComponentPageSource(dialect, pc).loadPage(pc, false);
		}
		return baseComponentPageLucee;
	}

	public void resetBaseComponentPage() {
		baseComponentPageCFML = null;
		baseComponentPageLucee = null;
	}

	public Collection<Mapping> getServerTagMappings() {
		if (serverTagMappings == null) {
			Iterator<Entry<String, Mapping>> it = getConfigServerImpl().tagMappings.entrySet().iterator();// .cloneReadOnly(this);
			Entry<String, Mapping> e;
			serverTagMappings = new ConcurrentHashMap<String, Mapping>();
			while (it.hasNext()) {
				e = it.next();
				serverTagMappings.put(e.getKey(), ((MappingImpl) e.getValue()).cloneReadOnly(this));
			}
		}
		return serverTagMappings.values();
	}

	public Mapping getDefaultServerTagMapping() {
		return getConfigServerImpl().defaultTagMapping;
	}

	public Mapping getServerTagMapping(String mappingName) {
		getServerTagMappings(); // necessary to make sure it exists
		return serverTagMappings.get(mappingName);
	}

	public Collection<Mapping> getServerFunctionMappings() {
		if (serverFunctionMappings == null) {
			Iterator<Entry<String, Mapping>> it = getConfigServerImpl().functionMappings.entrySet().iterator();
			Entry<String, Mapping> e;
			serverFunctionMappings = new ConcurrentHashMap<String, Mapping>();
			while (it.hasNext()) {
				e = it.next();
				serverFunctionMappings.put(e.getKey(), ((MappingImpl) e.getValue()).cloneReadOnly(this));
			}
		}
		return serverFunctionMappings.values();
	}

	public Mapping getServerFunctionMapping(String mappingName) {
		getServerFunctionMappings();// call this to make sure it exists
		return serverFunctionMappings.get(mappingName);
	}

	public Mapping getDefaultServerFunctionMapping() {
		return getConfigServerImpl().defaultFunctionMapping;
	}

	private Map<String, SoftReference<Mapping>> applicationMappings = new ConcurrentHashMap<String, SoftReference<Mapping>>();

	private TagHandlerPool tagHandlerPool = new TagHandlerPool(this);
	private SearchEngine searchEngine;
	private AMFEngine amfEngine;

	// FYI used by Extensions, do not remove
	public Mapping getApplicationMapping(String virtual, String physical) {
		return getApplicationMapping("application", virtual, physical, null, true, false, true, true);
	}

	public boolean isApplicationMapping(Mapping mapping) {
		Iterator<SoftReference<Mapping>> it = applicationMappings.values().iterator();
		SoftReference<Mapping> sr;
		while (it.hasNext()) {
			sr = it.next();
			if (sr != null && mapping.equals(sr.get())) return true;
		}
		return false;
	}

	public Mapping getApplicationMapping(String type, String virtual, String physical, String archive, boolean physicalFirst, boolean ignoreVirtual,
			boolean checkPhysicalFromWebroot, boolean checkArchiveFromWebroot) {
		String key = type + ":" + virtual.toLowerCase() + ":" + (physical == null ? "" : physical.toLowerCase()) + ":" + (archive == null ? "" : archive.toLowerCase()) + ":"
				+ physicalFirst;
		key = Long.toString(HashUtil.create64BitHash(key), Character.MAX_RADIX);

		SoftReference<Mapping> t = applicationMappings.get(key);
		Mapping m = t == null ? null : t.get();

		if (m == null) {
			m = new MappingImpl(this, virtual, physical, archive, Config.INSPECT_UNDEFINED, physicalFirst, false, false, false, true, ignoreVirtual, null, -1, -1,
					checkPhysicalFromWebroot, checkArchiveFromWebroot);
			applicationMappings.put(key, new SoftReference<Mapping>(m));
		}

		return m;
	}

	public Mapping[] getApplicationMapping() {
		List<Mapping> list = new ArrayList<>();
		Iterator<SoftReference<Mapping>> it = applicationMappings.values().iterator();
		SoftReference<Mapping> sr;
		while (it.hasNext()) {
			sr = it.next();
			if (sr != null) list.add(sr.get());
		}
		return list.toArray(new Mapping[list.size()]);
	}

	@Override
	public String getLabel() {
		String hash = getHash();
		String label = hash;
		Map<String, String> labels = configServer.getLabels();
		if (labels != null) {
			String l = labels.get(hash);
			if (!StringUtil.isEmpty(l)) {
				label = l;
			}
		}
		return label;
	}

	public String getHash() {
		return SystemUtil.hash(getServletContext());
	}

	public KeyLock<String> getContextLock() {
		return contextLock;
	}

	@Override
	public Map<String, GatewayEntry> getGatewayEntries() {
		return getGatewayEngine().getEntries();
	}

	@Override
	protected void setGatewayEntries(Map<String, GatewayEntry> gatewayEntries) {
		try {
			getGatewayEngine().addEntries(this, gatewayEntries);
		}
		catch (Exception e) {
			LogUtil.log(ThreadLocalPageContext.getConfig(this), ConfigWebImpl.class.getName(), e);
		}
	}

	public GatewayEngineImpl getGatewayEngine() {
		if (gatewayEngine == null) {
			gatewayEngine = new GatewayEngineImpl(this);
		}
		return gatewayEngine;
	}

	public void setGatewayEngine(GatewayEngineImpl gatewayEngine) {
		this.gatewayEngine = gatewayEngine;
	}

	public TagHandlerPool getTagHandlerPool() {
		return tagHandlerPool;
	}

	public DebuggerPool getDebuggerPool() {
		if (debuggerPool == null) {
			Resource dir = getConfigDir().getRealResource("debugger");
			dir.mkdirs();
			debuggerPool = new DebuggerPool(dir);
		}
		return debuggerPool;
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

	public void updatePassword(boolean server, String passwordOld, String passwordNew) throws PageException, IOException, SAXException, BundleException {
		PasswordImpl.updatePassword(server ? configServer : this, passwordOld, passwordNew);
	}

	public void updatePassword(boolean server, Password passwordOld, Password passwordNew) throws PageException, IOException, SAXException, BundleException {
		PasswordImpl.updatePassword(server ? configServer : this, passwordOld, passwordNew);
	}

	public Password updatePasswordIfNecessary(boolean server, String passwordRaw) {
		ConfigImpl config = server ? configServer : this;
		return PasswordImpl.updatePasswordIfNecessary(config, config.password, passwordRaw);
	}

	@Override
	public Resource getConfigServerDir() {
		return configServer.getConfigDir();
	}

	public Map<String, String> getAllLabels() {
		return configServer.getLabels();
	}

	@Override
	public boolean allowRequestTimeout() {
		return configServer.allowRequestTimeout();
	}

	public CFMLWriter getCFMLWriter(PageContext pc, HttpServletRequest req, HttpServletResponse rsp) {
		if (writerType == CFML_WRITER_WS) return new CFMLWriterWS(pc, req, rsp, -1, false, closeConnection(), isShowVersion(), contentLength());
		else if (writerType == CFML_WRITER_REFULAR) return new CFMLWriterImpl(pc, req, rsp, -1, false, closeConnection(), isShowVersion(), contentLength());
		else return new CFMLWriterWSPref(pc, req, rsp, -1, false, closeConnection(), isShowVersion(), contentLength());
	}

	@Override
	public JspWriter getWriter(PageContext pc, HttpServletRequest req, HttpServletResponse rsp) {
		return getCFMLWriter(pc, req, rsp);
	}

	public ActionMonitorCollector getActionMonitorCollector() {
		return configServer.getActionMonitorCollector();
	}

	public boolean hasIndividualSecurityManager() {
		return configServer.hasIndividualSecurityManager(getIdentification().getId());
	}

	@Override
	public CFMLFactory getFactory() {
		return factory;
	}

	@Override
	public CacheHandlerCollection getCacheHandlerCollection(int type, CacheHandlerCollection defaultValue) {
		if (cacheHandlerCollections == null) cacheHandlerCollections = new CacheHandlerCollections(this);
		switch (type) {
		case Config.CACHE_TYPE_FILE:
			return cacheHandlerCollections.file;
		case Config.CACHE_TYPE_FUNCTION:
			return cacheHandlerCollections.function;
		case Config.CACHE_TYPE_HTTP:
			return cacheHandlerCollections.http;
		case Config.CACHE_TYPE_INCLUDE:
			return cacheHandlerCollections.include;
		case Config.CACHE_TYPE_QUERY:
			return cacheHandlerCollections.query;
		case Config.CACHE_TYPE_RESOURCE:
			return cacheHandlerCollections.resource;
		case Config.CACHE_TYPE_WEBSERVICE:
			return cacheHandlerCollections.webservice;
		// case Config.CACHE_TYPE_OBJECT: return cacheHandlerCollections.object;
		// case Config.CACHE_TYPE_TEMPLATE: return cacheHandlerCollections.template;
		}

		return defaultValue;
	}

	public void releaseCacheHandlers(PageContext pc) {
		if (cacheHandlerCollections == null) return;
		cacheHandlerCollections.releaseCacheHandlers(pc);
	}

	protected void setIdentification(IdentificationWeb id) {
		this.id = id;
	}

	@Override
	public IdentificationWeb getIdentification() {
		return id;
	}

	public int getServerPasswordType() {
		return configServer.getPasswordType();
	}

	public String getServerPasswordSalt() {
		return configServer.getPasswordSalt();
	}

	public int getServerPasswordOrigin() {
		return configServer.getPasswordOrigin();
	}

	public String getServerSalt() {
		return configServer.getSalt();
	}

	public Password isServerPasswordEqual(String password) {
		return configServer.isPasswordEqual(password);
	}

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
		if (searchEngine == null) {
			try {
				Object o = ClassUtil.loadInstance(getSearchEngineClassDefinition().getClazz());
				if (o instanceof SearchEngine) searchEngine = (SearchEngine) o;
				else throw new ApplicationException("class [" + o.getClass().getName() + "] does not implement the interface SearchEngine");

				searchEngine.init(this,
						ConfigWebUtil.getFile(getConfigDir(), ConfigWebUtil.translateOldPath(getSearchEngineDirectory()), "search", getConfigDir(), FileUtil.TYPE_DIR, this));
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return searchEngine;
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
		amfEngine = engine;
	}

	@Override
	public AMFEngine getAMFEngine() {
		if (amfEngine == null) return AMFEngineDummy.getInstance();
		return amfEngine;
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

	private short passwordSource;

	public WSHandler getWSHandler() throws PageException {
		if (wsHandler == null) {
			ClassDefinition cd = getWSHandlerClassDefinition();
			if (isEmpty(cd)) cd = configServer.getWSHandlerClassDefinition();
			try {
				if (isEmpty(cd)) return new DummyWSHandler();
				Object obj = cd.getClazz().newInstance();
				if (obj instanceof WSHandler) wsHandler = (WSHandler) obj;
				else wsHandler = new WSHandlerReflector(obj);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return wsHandler;
	}

	protected void setPasswordSource(short passwordSource) {
		this.passwordSource = passwordSource;
	}

	public short getPasswordSource() {
		return passwordSource;
	}

	@Override
	public void checkPassword() throws PageException {
		configServer.checkPassword();
	}
}