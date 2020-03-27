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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import lucee.commons.collection.LinkedHashMapMaxSize;
import lucee.commons.collection.MapFactory;
import lucee.commons.digest.Hash;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.ExtensionFilter;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.functions.system.IsZipFile;
import lucee.runtime.gateway.GatewayEntry;
import lucee.runtime.monitor.ActionMonitor;
import lucee.runtime.monitor.ActionMonitorCollector;
import lucee.runtime.monitor.IntervallMonitor;
import lucee.runtime.monitor.RequestMonitor;
import lucee.runtime.net.amf.AMFEngine;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.security.SecurityManagerImpl;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.type.scope.ClusterRemote;
import lucee.runtime.type.scope.ClusterWrap;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.function.FunctionLibFactory;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibException;
import lucee.transformer.library.tag.TagLibFactory;

/**
 * config server impl
 */
public final class ConfigServerImpl extends ConfigImpl implements ConfigServer {

	private static final long FIVE_SECONDS = 5000;

	private final CFMLEngineImpl engine;
	private Map<String, CFMLFactory> initContextes;
	// private Map contextes;
	private SecurityManager defaultSecurityManager;
	private Map<String, SecurityManager> managers = MapFactory.<String, SecurityManager>getConcurrentMap();
	Password defaultPassword;
	private Resource rootDir;
	private URL updateLocation;
	private String updateType = "";
	private ConfigListener configListener;
	private Map<String, String> labels;
	private RequestMonitor[] requestMonitors;
	private IntervallMonitor[] intervallMonitors;
	private ActionMonitorCollector actionMonitorCollector;

	private boolean monitoringEnabled = false;
	private int delay = 1;
	private boolean captcha = false;
	private boolean rememberMe = true;
	// private static ConfigServerImpl instance;

	private String[] authKeys;
	private String idPro;

	private LinkedHashMapMaxSize<Long, String> previousNonces = new LinkedHashMapMaxSize<Long, String>(100);

	private int permGenCleanUpThreshold = 60;

	final TagLib cfmlCoreTLDs;
	final TagLib luceeCoreTLDs;
	final FunctionLib cfmlCoreFLDs;
	final FunctionLib luceeCoreFLDs;

	private ServletConfig srvConfig;

	/**
	 * @param engine
	 * @param srvConfig
	 * @param initContextes
	 * @param contextes
	 * @param configDir
	 * @param configFile
	 * @throws TagLibException
	 * @throws FunctionLibException
	 */
	protected ConfigServerImpl(CFMLEngineImpl engine, Map<String, CFMLFactory> initContextes, Map<String, CFMLFactory> contextes, Resource configDir, Resource configFile)
			throws TagLibException, FunctionLibException {
		super(configDir, configFile);
		this.cfmlCoreTLDs = TagLibFactory.loadFromSystem(CFMLEngine.DIALECT_CFML, id);
		this.luceeCoreTLDs = TagLibFactory.loadFromSystem(CFMLEngine.DIALECT_LUCEE, id);
		this.cfmlCoreFLDs = FunctionLibFactory.loadFromSystem(CFMLEngine.DIALECT_CFML, id);
		this.luceeCoreFLDs = FunctionLibFactory.loadFromSystem(CFMLEngine.DIALECT_LUCEE, id);

		this.engine = engine;
		engine.setConfigServerImpl(this);
		this.initContextes = initContextes;
		// this.contextes=contextes;
		this.rootDir = configDir;
		// instance=this;
	}

	/**
	 * @return the configListener
	 */
	@Override
	public ConfigListener getConfigListener() {
		return configListener;
	}

	/**
	 * @param configListener the configListener to set
	 */
	@Override
	public void setConfigListener(ConfigListener configListener) {
		this.configListener = configListener;
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
		return getConfigWebImpl(realpath);
	}

	/**
	 * returns CongigWeb Implementtion
	 * 
	 * @param realpath
	 * @return ConfigWebImpl
	 */
	protected ConfigWebImpl getConfigWebImpl(String realpath) {
		Iterator<String> it = initContextes.keySet().iterator();
		while (it.hasNext()) {
			ConfigWebImpl cw = ((CFMLFactoryImpl) initContextes.get(it.next())).getConfigWebImpl();
			if (ReqRspUtil.getRootPath(cw.getServletContext()).equals(realpath)) return cw;
		}
		return null;
	}

	public ServletContext getServletContext() {
		Iterator<String> it = initContextes.keySet().iterator();
		while (it.hasNext()) {
			ConfigWebImpl cw = ((CFMLFactoryImpl) initContextes.get(it.next())).getConfigWebImpl();
			return cw.getServletContext();
		}
		return null;
	}

	public ConfigWebImpl getConfigWebById(String id) {
		Iterator<String> it = initContextes.keySet().iterator();

		while (it.hasNext()) {
			ConfigWebImpl cw = ((CFMLFactoryImpl) initContextes.get(it.next())).getConfigWebImpl();
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
		if (defaultSecurityManager == null) {
			defaultSecurityManager = SecurityManagerImpl.getOpenSecurityManager();
		}
		return defaultSecurityManager.cloneSecurityManager();
	}

	@Override
	public boolean hasIndividualSecurityManager(String id) {
		return managers.containsKey(id);
	}

	/**
	 * @param defaultSecurityManager
	 */
	protected void setDefaultSecurityManager(SecurityManager defaultSecurityManager) {
		this.defaultSecurityManager = defaultSecurityManager;
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
		return defaultSecurityManager;
	}

	/**
	 * @return Returns the defaultPassword.
	 */
	protected Password getDefaultPassword() {
		if (defaultPassword == null) return password;
		return defaultPassword;
	}

	protected boolean hasCustomDefaultPassword() {
		return defaultPassword != null;
	}

	/**
	 * @param defaultPassword The defaultPassword to set.
	 */
	protected void setDefaultPassword(Password defaultPassword) {
		this.defaultPassword = defaultPassword;
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
	public String getUpdateType() {
		return updateType;
	}

	@Override
	public void setUpdateType(String updateType) {
		if (!StringUtil.isEmpty(updateType)) this.updateType = updateType;
	}

	@Override
	public URL getUpdateLocation() {
		return updateLocation;
	}

	@Override
	public void setUpdateLocation(URL updateLocation) {
		this.updateLocation = updateLocation;
	}

	@Override
	public void setUpdateLocation(String strUpdateLocation) throws MalformedURLException {
		setUpdateLocation(new URL(strUpdateLocation));
	}

	@Override
	public void setUpdateLocation(String strUpdateLocation, URL defaultValue) {
		try {
			setUpdateLocation(strUpdateLocation);
		}
		catch (MalformedURLException e) {
			setUpdateLocation(defaultValue);
		}
	}

	@Override
	public SecurityManager getSecurityManager() {
		SecurityManagerImpl sm = (SecurityManagerImpl) getDefaultSecurityManager();// .cloneSecurityManager();
		// sm.setAccess(SecurityManager.TYPE_ACCESS_READ,SecurityManager.ACCESS_PROTECTED);
		// sm.setAccess(SecurityManager.TYPE_ACCESS_WRITE,SecurityManager.ACCESS_PROTECTED);
		return sm;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	public Map<String, String> getLabels() {
		if (labels == null) labels = new HashMap<String, String>();
		return labels;
	}

	private ThreadQueue threadQueue;

	public ThreadQueue setThreadQueue(ThreadQueue threadQueue) {
		return this.threadQueue = threadQueue;
	}

	@Override
	public ThreadQueue getThreadQueue() {
		return threadQueue;
	}

	@Override
	public RequestMonitor[] getRequestMonitors() {
		return requestMonitors;
	}

	@Override
	public RequestMonitor getRequestMonitor(String name) throws ApplicationException {
		if (requestMonitors != null) for (int i = 0; i < requestMonitors.length; i++) {
			if (requestMonitors[i].getName().equalsIgnoreCase(name)) return requestMonitors[i];
		}
		throw new ApplicationException("there is no request monitor registered with name [" + name + "]");
	}

	protected void setRequestMonitors(RequestMonitor[] monitors) {
		this.requestMonitors = monitors;
	}

	@Override
	public IntervallMonitor[] getIntervallMonitors() {
		return intervallMonitors;
	}

	@Override
	public IntervallMonitor getIntervallMonitor(String name) throws ApplicationException {
		if (intervallMonitors != null) for (int i = 0; i < intervallMonitors.length; i++) {
			if (intervallMonitors[i].getName().equalsIgnoreCase(name)) return intervallMonitors[i];
		}
		throw new ApplicationException("there is no intervall monitor registered with name [" + name + "]");
	}

	protected void setIntervallMonitors(IntervallMonitor[] monitors) {
		this.intervallMonitors = monitors;
	}

	public void setActionMonitorCollector(ActionMonitorCollector actionMonitorCollector) {
		this.actionMonitorCollector = actionMonitorCollector;
	}

	public ActionMonitorCollector getActionMonitorCollector() {
		return actionMonitorCollector;
	}

	@Override
	public ActionMonitor getActionMonitor(String name) {
		return actionMonitorCollector == null ? null : actionMonitorCollector.getActionMonitor(name);
	}

	@Override
	public boolean isMonitoringEnabled() {
		return monitoringEnabled;
	}

	protected void setMonitoringEnabled(boolean monitoringEnabled) {
		this.monitoringEnabled = monitoringEnabled;
	}

	protected void setLoginDelay(int delay) {
		this.delay = delay;
	}

	protected void setLoginCaptcha(boolean captcha) {
		this.captcha = captcha;
	}

	protected void setRememberMe(boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

	@Override
	public int getLoginDelay() {
		return delay;
	}

	@Override
	public boolean getLoginCaptcha() {
		return captcha;
	}

	@Override
	public boolean getRememberMe() {
		return rememberMe;
	}

	@Override
	public void reset() {
		super.reset();
		getThreadQueue().clear();
	}

	@Override
	public Resource getSecurityDirectory() {
		Resource cacerts = null;
		// javax.net.ssl.trustStore
		String trustStore = SystemUtil.getPropertyEL("javax.net.ssl.trustStore");
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
		int promille = SystemUtil.getFreePermGenSpacePromille();

		long kbFreePermSpace = SystemUtil.getFreePermGenSpaceSize() / 1024;
		int percentageAvailable = SystemUtil.getPermGenFreeSpaceAsAPercentageOfAvailable();

		// Pen Gen Space info not available indicated by a return of -1
		if (check && kbFreePermSpace < 0) {
			if (countLoadedPages() > 2000) shrink();
		}
		else if (check && percentageAvailable < permGenCleanUpThreshold) {
			shrink();
			if (permGenCleanUpThreshold >= 5) {
				// adjust the threshold allowed down so the amount of permgen can slowly grow to its allocated space
				// up to 100%
				setPermGenCleanUpThreshold(permGenCleanUpThreshold - 5);
			}
			else {
				LogUtil.log(ThreadLocalPageContext.getConfig(this), Log.LEVEL_WARN, ConfigServerImpl.class.getName(),
						" Free Perm Gen Space is less than 5% free: shrinking all template classloaders : consider increasing allocated Perm Gen Space");
			}
		}
		else if (check && kbFreePermSpace < 2048) {
			LogUtil.log(ThreadLocalPageContext.getConfig(this), Log.LEVEL_WARN, ConfigServerImpl.class.getName(),

					" Free Perm Gen Space is less than 2Mb (free:" + ((SystemUtil.getFreePermGenSpaceSize() / 1024)) + "kb), shrinking all template classloaders");
			// first request a GC and then check if it helps
			System.gc();
			if ((SystemUtil.getFreePermGenSpaceSize() / 1024) < 2048) {
				shrink();
			}
		}
	}

	private void shrink() {
		ConfigWeb[] webs = getConfigWebs();
		int count = 0;
		for (int i = 0; i < webs.length; i++) {
			count += shrink((ConfigWebImpl) webs[i], false);
		}
		if (count == 0) {
			for (int i = 0; i < webs.length; i++) {
				shrink((ConfigWebImpl) webs[i], true);
			}
		}
	}

	private static int shrink(ConfigWebImpl config, boolean force) {
		int count = 0;
		count += shrink(config.getMappings(), force);
		count += shrink(config.getCustomTagMappings(), force);
		count += shrink(config.getComponentMappings(), force);
		count += shrink(config.getFunctionMappings(), force);
		count += shrink(config.getServerFunctionMappings(), force);
		count += shrink(config.getTagMappings(), force);
		count += shrink(config.getServerTagMappings(), force);
		// count+=shrink(config.getServerTagMapping(),force);
		return count;
	}

	private static int shrink(Collection<Mapping> mappings, boolean force) {
		int count = 0;
		Iterator<Mapping> it = mappings.iterator();
		while (it.hasNext()) {
			count += shrink(it.next(), force);
		}
		return count;
	}

	private static int shrink(Mapping[] mappings, boolean force) {
		int count = 0;
		for (int i = 0; i < mappings.length; i++) {
			count += shrink(mappings[i], force);
		}
		return count;
	}

	private static int shrink(Mapping mapping, boolean force) {
		try {
			// PCLCollection pcl = ((MappingImpl)mapping).getPCLCollection();
			// if(pcl!=null)return pcl.shrink(force);
			((MappingImpl) mapping).shrink();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return 0;
	}

	public int getPermGenCleanUpThreshold() {
		return permGenCleanUpThreshold;
	}

	public void setPermGenCleanUpThreshold(int permGenCleanUpThreshold) {
		this.permGenCleanUpThreshold = permGenCleanUpThreshold;
	}

	public long countLoadedPages() {
		/*
		 * long count=0; ConfigWeb[] webs = getConfigWebs(); for(int i=0;i<webs.length;i++){
		 * count+=_count((ConfigWebImpl) webs[i]); } return count;
		 */
		return -1;
		// MUST implement
	}
	/*
	 * private static long _countx(ConfigWebImpl config) { long count=0;
	 * count+=_count(config.getMappings()); count+=_count(config.getCustomTagMappings());
	 * count+=_count(config.getComponentMappings()); count+=_count(config.getFunctionMapping());
	 * count+=_count(config.getServerFunctionMapping()); count+=_count(config.getTagMapping());
	 * count+=_count(config.getServerTagMapping());
	 * //count+=_count(((ConfigWebImpl)config).getServerTagMapping()); return count; }
	 */

	/*
	 * private static long _count(Mapping[] mappings) { long count=0; for(int
	 * i=0;i<mappings.length;i++){ count+=_count(mappings[i]); } return count; }
	 */

	/*
	 * private static long _countx(Mapping mapping) { PCLCollection pcl =
	 * ((MappingImpl)mapping).getPCLCollection(); return pcl==null?0:pcl.count(); }
	 */

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
		CFMLEngineFactory factory = getCFMLEngine().getCFMLEngineFactory();

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

	private IdentificationServer id;

	private String libHash;

	private ClassDefinition<AMFEngine> amfEngineCD;

	private Map<String, String> amfEngineArgs;

	private List<ExtensionDefintion> localExtensions;

	private long localExtHash;
	private int localExtSize = -1;

	private Map<String, GatewayEntry> gatewayEntries;

	public String[] getAuthenticationKeys() {
		return authKeys == null ? new String[0] : authKeys;
	}

	protected void setAuthenticationKeys(String[] authKeys) {
		this.authKeys = authKeys;
	}

	public ConfigServer getConfigServer(String key, String nonce) {
		return this;
	}

	public void checkAccess(Password password) throws ExpressionException {
		if (!hasPassword()) throw new ExpressionException("Cannot access, no password is defined");
		if (!passwordEqual(password)) throw new ExpressionException("No access, password is invalid");
	}

	public void checkAccess(String key, long timeNonce) throws PageException {

		if (previousNonces.containsKey(timeNonce)) {
			long now = System.currentTimeMillis();
			long diff = timeNonce > now ? timeNonce - now : now - timeNonce;
			if (diff > 10) throw new ApplicationException("nonce was already used, same nonce can only be used once");

		}
		long now = System.currentTimeMillis() + getTimeServerOffset();
		if (timeNonce > (now + FIVE_SECONDS) || timeNonce < (now - FIVE_SECONDS))
			throw new ApplicationException("nonce is outdated (timserver offset:" + getTimeServerOffset() + ")");
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
		return id;
	}

	protected void setIdentification(IdentificationServer id) {
		this.id = id;
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
			itt = ((ConfigImpl) cw).getExtensionBundleDefintions().iterator();
			while (itt.hasNext()) {
				bd = itt.next();
				rtn.put(bd.getName() + "|" + bd.getVersionAsString(), bd);
			}
		}

		return rtn.values();
	}

	@Override
	public Collection<RHExtension> getAllRHExtensions() {
		Map<String, RHExtension> rtn = new HashMap<>();

		// server (this)
		RHExtension[] arr = getRHExtensions();
		for (RHExtension rhe: arr) {
			rtn.put(rhe.getId(), rhe);
		}

		// webs
		ConfigWeb[] cws = getConfigWebs();
		for (ConfigWeb cw: cws) {
			arr = ((ConfigWebImpl) cw).getRHExtensions();
			for (RHExtension rhe: arr) {
				rtn.put(rhe.getId(), rhe);
			}
		}

		return rtn.values();
	}

	protected void setLibHash(String libHash) {
		this.libHash = libHash;
	}

	protected String getLibHash() {
		return libHash;
	}

	@Override
	public Resource getLocalExtensionProviderDirectory() {
		Resource dir = getConfigDir().getRealResource("extensions/available");
		if (!dir.exists()) dir.mkdirs();
		return dir;
	}

	protected void setAMFEngine(ClassDefinition<AMFEngine> cd, Map<String, String> args) {
		amfEngineCD = cd;
		amfEngineArgs = args;
	}

	public ClassDefinition<AMFEngine> getAMFEngineClassDefinition() {
		return amfEngineCD;
	}

	public Map<String, String> getAMFEngineArgs() {
		return amfEngineArgs;
	}

	@Override
	public RHExtension[] getServerRHExtensions() {
		return getRHExtensions();
	}

	@Override
	public List<ExtensionDefintion> loadLocalExtensions(boolean validate) {
		Resource[] locReses = getLocalExtensionProviderDirectory().listResources(new ExtensionResourceFilter(".lex"));
		if (validate || localExtensions == null || localExtSize != locReses.length || extHash(locReses) != localExtHash) {
			localExtensions = new ArrayList<ExtensionDefintion>();
			Map<String, String> map = new HashMap<String, String>();
			RHExtension ext;
			String v, fileName, uuid, version;
			ExtensionDefintion ed;
			for (int i = 0; i < locReses.length; i++) {
				ed = null;
				// we stay happy with the file name when it has the right pattern (uuid-version.lex)
				fileName = locReses[i].getName();
				if (!validate && fileName.length() > 39) {
					uuid = fileName.substring(0, 35);
					version = fileName.substring(36, fileName.length() - 4);
					if (Decision.isUUId(uuid)) {
						ed = new ExtensionDefintion(uuid, version);
						ed.setSource(this, locReses[i]);
					}
				}
				if (ed == null) {
					try {
						ext = new RHExtension(this, locReses[i], false);
						ed = new ExtensionDefintion(ext.getId(), ext.getVersion());
						ed.setSource(ext);

					}
					catch (Exception e) {
						ed = null;
						LogUtil.log(ThreadLocalPageContext.getConfig(this), ConfigServerImpl.class.getName(), e);
						try {
							if (!IsZipFile.invoke(locReses[i])) locReses[i].remove(true);
						}
						catch (Exception ee) {
							LogUtil.log(ThreadLocalPageContext.getConfig(this), ConfigServerImpl.class.getName(), ee);
						}
					}
				}

				if (ed != null) {
					// check if we already have an extension with the same id to avoid having more than once
					v = map.get(ed.getId());
					if (v != null && v.compareToIgnoreCase(ed.getId()) > 0) continue;

					map.put(ed.getId(), ed.getVersion());
					localExtensions.add(ed);
				}

			}
			localExtHash = extHash(locReses);
			localExtSize = locReses.length; // we store the size because localExtensions size could be smaller because of duplicates
		}
		return localExtensions;
	}

	private long extHash(Resource[] locReses) {
		StringBuilder sb = new StringBuilder();
		if (locReses != null) {
			for (Resource locRes: locReses) {
				sb.append(locRes.getAbsolutePath()).append(';');
			}
		}
		return HashUtil.create64BitHash(sb);
	}

	@Override
	protected void setGatewayEntries(Map<String, GatewayEntry> gatewayEntries) {
		this.gatewayEntries = gatewayEntries;
	}

	@Override
	public Map<String, GatewayEntry> getGatewayEntries() {
		return gatewayEntries;
	}

	/*
	 * private WSHandler wsHandler;
	 * 
	 * @Override // that method normally should not be used, maybe in rthe future public WSHandler
	 * getWSHandler() throws PageException { if (wsHandler == null) { ClassDefinition cd =
	 * getWSHandlerClassDefinition(); try { if (isEmpty(cd)) return new DummyWSHandler(); Object obj =
	 * cd.getClazz().newInstance(); if (obj instanceof WSHandler) wsHandler = (WSHandler) obj; else
	 * wsHandler = new WSHandlerReflector(obj); } catch (Exception e) { throw Caster.toPageException(e);
	 * } } return wsHandler; }
	 */

	@Override
	public void checkPassword() throws PageException {
		CFMLEngine engine = ConfigWebUtil.getEngine(this);
		ConfigWeb[] webs = getConfigWebs();
		try {
			XMLConfigServerFactory.reloadInstance(engine, this);
			for (int i = 0; i < webs.length; i++) {
				XMLConfigWebFactory.reloadInstance(engine, this, (ConfigWebImpl) webs[i], true);
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
}