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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.URLDecoder;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.ExtensionFilter;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.ai.AIEnginePool;
import lucee.runtime.config.ConfigFactory.UpdateInfo;
import lucee.runtime.config.gateway.GatewayMap;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.engine.ThreadQueue;
import lucee.runtime.engine.ThreadQueueImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.functions.system.IsZipFile;
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
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Cluster;
import lucee.runtime.type.scope.ClusterRemote;
import lucee.runtime.type.scope.ClusterWrap;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;
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

	public static Config instance;

	private final CFMLEngineImpl engine;
	private Map<String, CFMLFactory> initContextes;
	// private Map contextes;
	private SecurityManager defaultSecurityManager;
	private Map<String, SecurityManager> managers = MapFactory.<String, SecurityManager>getConcurrentMap();
	Password defaultPassword;
	private Resource rootDir;
	private URL updateLocation;
	private String updateType;
	private ConfigListener configListener;
	private boolean initConfigListener;
	private Map<String, String> labels;
	private RequestMonitor[] requestMonitors;
	private IntervallMonitor[] intervallMonitors;
	private ActionMonitorCollector actionMonitorCollector;

	private Boolean monitoringEnabled;
	private int delay = -1;
	private Boolean captcha;
	private Boolean rememberMe;
	private Boolean classicStyle;
	// private static ConfigServerImpl instance;

	private String[] authKeys;
	private String idPro;

	private LinkedHashMapMaxSize<Long, String> previousNonces = new LinkedHashMapMaxSize<Long, String>(100);

	private int permGenCleanUpThreshold = 60;

	private TagLib coreTLDs;
	private FunctionLib coreFLDs;

	private final UpdateInfo updateInfo;

	private IdentificationServer id;

	private String libHash;

	private ClassDefinition<AMFEngine> amfEngineCD;

	private Map<String, String> amfEngineArgs;

	private List<ExtensionDefintion> localExtensions;

	private long localExtHash;
	private int localExtSize = -1;

	private GatewayMap gatewayEntries;

	private Resource mvnDir;

	private final ScheduledExecutorService inspectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "InspectAutoRefresh");
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		return t;
	});
	private volatile ScheduledFuture<?> nextTick;
	private final Object tickToken = new Object();
	private final AtomicBoolean fastRequested = new AtomicBoolean(false);

	/**
	 * @param engine
	 * @param initContextes
	 * @param contextes
	 * @param configDir
	 * @param configFile
	 * @param updateInfo
	 * @param essentialOnly
	 * @throws TagLibException
	 * @throws FunctionLibException
	 */
	protected ConfigServerImpl(CFMLEngineImpl engine, Map<String, CFMLFactory> initContextes, Map<String, CFMLFactory> contextes, Resource configDir, Resource configFile,
			UpdateInfo updateInfo, boolean essentialOnly, boolean newVersion) throws TagLibException, FunctionLibException {
		super(configDir, configFile, newVersion);

		this.engine = engine;
		if (!essentialOnly) engine.setConfigServerImpl(this);
		this.initContextes = initContextes;
		// this.contextes=contextes;
		this.rootDir = configDir;
		// instance=this;
		this.updateInfo = updateInfo;
		instance = this;
	}

	FunctionLib getCoreFLDs() throws FunctionLibException {
		if (coreFLDs == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getCoreFLDs")) {
				if (coreFLDs == null) {
					this.coreFLDs = FunctionLibFactory.loadFromSystem(id);
				}
			}
		}
		return coreFLDs;
	}

	TagLib getCoreTLDs() throws TagLibException {
		if (coreTLDs == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getCoreTLDs")) {
				if (coreTLDs == null) {
					this.coreTLDs = TagLibFactory.loadFromSystem(id);
				}
			}
		}
		return coreTLDs;
	}

	public void setRoot(Struct root) {
		this.root = root;
	}

	public UpdateInfo getUpdateInfo() {
		return updateInfo;
	}

	@Override
	public ConfigListener getConfigListener() {
		if (initConfigListener) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getConfigListener")) {
				if (initConfigListener) {
					configListener = ConfigFactoryImpl.loadListener(this, root, null);
					initConfigListener = false;
				}
			}
		}
		return configListener;
	}

	public ConfigServerImpl resetConfigListener() {
		if (!initConfigListener) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getConfigListener")) {
				if (!initConfigListener) {
					configListener = null;
					initConfigListener = true;
				}
			}
		}
		return this;
	}

	@Override
	public void setConfigListener(ConfigListener configListener) {
		this.configListener = configListener;
		this.initConfigListener = false;
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
			synchronized (SystemUtil.createToken("ConfigImpl", "getDefaultSecurityManager")) {
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
			synchronized (SystemUtil.createToken("ConfigImpl", "getDefaultSecurityManager")) {
				if (defaultSecurityManager != null) {
					defaultSecurityManager = null;
				}
			}
		}

		return this;
	}

	/**
	 * @return Returns the defaultPassword.
	 */
	protected Password getDefaultPassword() {
		defaultPassword = null;// TEST PW
		if (defaultPassword == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDefaultPassword")) {
				if (defaultPassword == null) {
					Password pw = PasswordImpl.readFromStruct(this, root, getSalt(), true, true);
					if (pw != null) defaultPassword = pw;
					else defaultPassword = getPassword();
				}
			}
		}
		return defaultPassword;
	}

	protected ConfigImpl resetDefaultPassword() {
		if (defaultPassword != null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getDefaultPassword")) {
				if (defaultPassword != null) {
					defaultPassword = null;
				}
			}
		}
		return this;
	}

	protected void setDefaultPassword(Password defaultPassword) {
		this.defaultPassword = defaultPassword;
	}

	protected boolean hasCustomDefaultPassword() {
		return getDefaultPassword() != null;
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
		if (updateType == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getUpdateType")) {
				if (updateType == null) {
					String ut = ConfigFactoryImpl.getAttr(this, root, "updateType");
					if (StringUtil.isEmpty(ut, true)) updateType = "manual";
					else updateType = ut.trim();
				}
			}
		}
		return updateType;
	}

	public ConfigServerImpl resetUpdateType() {
		if (updateType != null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getUpdateType")) {
				if (updateType != null) {
					updateType = null;
				}
			}
		}
		return this;
	}

	@Override
	public void setUpdateType(String updateType) {
		throw new RuntimeException("this action is no longer allowed");
	}

	@Override
	public URL getUpdateLocation() {
		if (updateLocation == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getUpdateLocation")) {
				if (updateLocation == null) {
					updateLocation = ConfigFactoryImpl.loadUpdate(this, root);
				}
			}
		}
		return updateLocation;
	}

	public ConfigServerImpl resetUpdateLocation() {
		if (updateLocation != null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getUpdateLocation")) {
				if (updateLocation != null) {
					updateLocation = null;
				}
			}
		}
		return this;
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

	public Map<String, String> getLabels() {
		if (labels == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getLabels")) {
				if (labels == null) {
					labels = ConfigFactoryImpl.loadLabel(null, root);
					// Only create empty map if loadLabel returns null
					if (labels == null) {
						labels = new HashMap<String, String>();
					}
				}
			}
		}
		return labels;
	}

	public ConfigServerImpl resetLabels() {
		if (labels != null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getLabels")) {
				if (labels != null) {
					labels = null;
				}
			}
		}
		return this;
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

	protected void setRequestMonitors(RequestMonitor[] monitors) {
		this.requestMonitors = monitors;
	}

	protected void setIntervallMonitors(IntervallMonitor[] monitors) {
		this.intervallMonitors = monitors;
	}

	protected void setActionMonitorCollector(ActionMonitorCollector actionMonitorCollector) {
		this.actionMonitorCollector = actionMonitorCollector;
	}

	@Override
	public RequestMonitor[] getRequestMonitors() {
		if (requestMonitors == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "monitors")) {
				if (requestMonitors == null) {
					ConfigFactoryImpl.loadMonitors(this, root);
				}
			}
		}
		return requestMonitors;
	}

	@Override
	public RequestMonitor getRequestMonitor(String name) throws ApplicationException {
		for (RequestMonitor rm: getRequestMonitors()) {
			if (rm.getName().equalsIgnoreCase(name)) return rm;
		}
		throw new ApplicationException("there is no request monitor registered with name [" + name + "]");
	}

	@Override
	public IntervallMonitor[] getIntervallMonitors() {
		if (intervallMonitors == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "monitors")) {
				if (intervallMonitors == null) {
					ConfigFactoryImpl.loadMonitors(this, root);
				}
			}
		}
		return intervallMonitors;
	}

	@Override
	public IntervallMonitor getIntervallMonitor(String name) throws ApplicationException {
		for (IntervallMonitor im: getIntervallMonitors()) {
			if (im.getName().equalsIgnoreCase(name)) return im;
		}
		throw new ApplicationException("there is no intervall monitor registered with name [" + name + "]");
	}

	public ActionMonitorCollector getActionMonitorCollector() {
		if (actionMonitorCollector == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "monitors")) {
				if (actionMonitorCollector == null) {
					ConfigFactoryImpl.loadMonitors(this, root);
				}
			}
		}
		return actionMonitorCollector;
	}

	@Override
	public ActionMonitor getActionMonitor(String name) {
		ActionMonitorCollector am = getActionMonitorCollector();
		return am == null ? null : am.getActionMonitor(name);
	}

	public ConfigServerImpl resetMonitors() {
		if (actionMonitorCollector != null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "monitors")) {
				if (actionMonitorCollector != null) {
					requestMonitors = null;
					intervallMonitors = null;
					actionMonitorCollector = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean isMonitoringEnabled() {
		if (monitoringEnabled == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "isMonitoringEnabled")) {
				if (monitoringEnabled == null) {
					Struct parent = ConfigUtil.getAsStruct("monitoring", root);
					monitoringEnabled = Caster.toBoolean(ConfigFactoryImpl.getAttr(this, parent, "enabled"), Boolean.FALSE);
				}
			}
		}
		return monitoringEnabled;
	}

	public ConfigServerImpl resetMonitoringEnabled() {
		if (monitoringEnabled != null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "isMonitoringEnabled")) {
				if (monitoringEnabled != null) {
					monitoringEnabled = null;
				}
			}
		}
		return this;
	}

	@Override
	public int getLoginDelay() {
		if (delay == -1) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getLoginDelay")) {
				if (delay == -1) {
					delay = Caster.toIntValue(ConfigFactoryImpl.getAttr(this, root, "loginDelay"), 1);
				}
			}
		}
		return delay;
	}

	public ConfigServerImpl resetLoginDelay() {
		if (delay != -1) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getLoginDelay")) {
				if (delay != -1) {
					delay = -1;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getLoginCaptcha() {
		if (captcha == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getLoginCaptcha")) {
				if (captcha == null) {
					captcha = Caster.toBooleanValue(ConfigFactoryImpl.getAttr(this, root, "loginCaptcha"), false);
				}
			}
		}
		return captcha;
	}

	public ConfigServerImpl resetLoginCaptcha() {
		if (captcha != null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getLoginCaptcha")) {
				if (captcha != null) {
					captcha = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getRememberMe() {
		if (rememberMe == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getRememberMe")) {
				if (rememberMe == null) {
					rememberMe = Caster.toBooleanValue(ConfigFactoryImpl.getAttr(this, root, "loginRememberme"), true);
				}
			}
		}
		return rememberMe;
	}

	public ConfigServerImpl resetRememberMe() {
		if (rememberMe != null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getRememberMe")) {
				if (rememberMe != null) {
					rememberMe = null;
				}
			}
		}
		return this;
	}

	@Override
	public boolean getDateCasterClassicStyle() {
		if (classicStyle == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getDateCasterClassicStyle")) {
				if (classicStyle == null) {
					String strClassicDateParsing = ConfigFactoryImpl.getAttr(this, root, "classicDateParsing");
					classicStyle = Caster.toBoolean(strClassicDateParsing, Boolean.FALSE);
				}
			}
		}
		return classicStyle;
	}

	public ConfigImpl resetDateCasterClassicStyle() {
		if (classicStyle != null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getDateCasterClassicStyle")) {
				if (classicStyle != null) {
					classicStyle = null;
				}
			}
		}
		return this;
	}

	@Override
	public ConfigImpl resetInspectTemplate() {
		super.resetInspectTemplate();
		// Admin inspect-mode change drops resolution caches on all web configs — entries
		// populated under the previous mode contract shouldn't carry across the boundary.
		for (ConfigWeb cw: getConfigWebs()) {
			if (cw instanceof ConfigWebPro) ((ConfigWebPro) cw).clearResolvedMappingPaths();
		}
		return this;
	}

	@Override
	public void reset() {
		super.reset();
		getThreadQueue().clear();
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

	private void shrink() {
		ConfigWeb[] webs = getConfigWebs();
		int count = 0;
		for (int i = 0; i < webs.length; i++) {
			count += shrink((ConfigWebPro) webs[i], false);
		}
		if (count == 0) {
			for (int i = 0; i < webs.length; i++) {
				shrink((ConfigWebPro) webs[i], true);
			}
		}
	}

	private static int shrink(ConfigWebPro config, boolean force) {
		int count = 0;
		count += shrink(config.getMappings(), force);
		count += shrink(config.getCustomTagMappings(), force);
		count += shrink(config.getComponentMappings(), force);
		count += shrink(config.getFunctionMappings(), force);
		count += shrink(config.getTagMappings(), force);
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
		return -1;
		// MUST implement
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

	public String[] getAuthenticationKeys() {
		if (authKeys == null) {
			synchronized (SystemUtil.createToken("ConfigImpl", "getAuthenticationKeys")) {
				if (authKeys == null) {
					String keyList = ConfigFactoryImpl.getAttr(this, root, "authKeys");
					if (!StringUtil.isEmpty(keyList)) {
						String[] keys = ListUtil.trimItems(ListUtil.toStringArray(ListUtil.toListRemoveEmpty(keyList, ',')));
						for (int i = 0; i < keys.length; i++) {
							try {
								keys[i] = URLDecoder.decode(keys[i], "UTF-8", true);
							}
							catch (Exception e) {
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
			synchronized (SystemUtil.createToken("ConfigServerImpl", "id")) {
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
			synchronized (SystemUtil.createToken("ConfigServerImpl", "id")) {
				if (id != null) {
					id = null;
				}
			}
		}
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
		Map<String, RHExtension> rtn = new HashMap<>();

		// server (this)
		RHExtension[] arr = getRHExtensions();
		for (RHExtension rhe: arr) {
			rtn.put(rhe.getId(), rhe);
		}

		// webs
		ConfigWeb[] cws = getConfigWebs();
		for (ConfigWeb cw: cws) {
			arr = ((ConfigWebPro) cw).getRHExtensions();
			for (RHExtension rhe: arr) {
				rtn.put(rhe.getId(), rhe);
			}
		}

		return rtn.values();
	}

	protected String getLibHash() {
		if (libHash == null) {
			synchronized (SystemUtil.createToken("ConfigServerImpl", "getLibHash")) {
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
						ext = RHExtension.getInstance(this, locReses[i]);
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
	public Resource getMavenDir() {
		if (mvnDir == null) {
			synchronized (this) {
				if (mvnDir == null) {
					String repoDir = SystemUtil.getSystemPropOrEnvVar("lucee.maven.local.repository", null);
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
					mvnDir = tmp;
				}
			}
		}
		return mvnDir;
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

}