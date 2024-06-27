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
package lucee.runtime.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.script.ScriptEngineFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.framework.Felix;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import lucee.Info;
import lucee.cli.servlet.ServletContextImpl;
import lucee.commons.collection.MapFactory;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.compress.CompressUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.ResourceUtilImpl;
import lucee.commons.io.retirement.RetireOutputStreamFactory;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.intergral.fusiondebug.server.FDControllerImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngineFactorySupport;
import lucee.loader.engine.CFMLEngineWrapper;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.util.Util;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.ComponentPageImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.cache.ram.RamCache;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigAdmin;
import lucee.runtime.config.ConfigFactory;
import lucee.runtime.config.ConfigFactory.UpdateInfo;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigServerFactory;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebFactory;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.config.Identification;
import lucee.runtime.config.Password;
import lucee.runtime.engine.listener.CFMLServletContextListener;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.MissingIncludeException;
import lucee.runtime.exp.NativeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageServletException;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.instrumentation.InstrumentationFactory;
import lucee.runtime.jsr223.ScriptEngineFactoryImpl;
import lucee.runtime.net.http.HTTPServletRequestWrap;
import lucee.runtime.net.http.HttpServletRequestDummy;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.CastImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.CreationImpl;
import lucee.runtime.op.DecisionImpl;
import lucee.runtime.op.ExceptonImpl;
import lucee.runtime.op.IOImpl;
import lucee.runtime.op.JavaProxyUtilImpl;
import lucee.runtime.op.OperationImpl;
import lucee.runtime.op.StringsImpl;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.schedule.SchedulerImpl;
import lucee.runtime.thread.SerializableCookie;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.util.Cast;
import lucee.runtime.util.ClassUtil;
import lucee.runtime.util.ClassUtilImpl;
import lucee.runtime.util.Creation;
import lucee.runtime.util.DBUtil;
import lucee.runtime.util.DBUtilImpl;
import lucee.runtime.util.Decision;
import lucee.runtime.util.Excepton;
import lucee.runtime.util.HTMLUtil;
import lucee.runtime.util.HTMLUtilImpl;
import lucee.runtime.util.HTTPUtilImpl;
import lucee.runtime.util.IO;
import lucee.runtime.util.ListUtil;
import lucee.runtime.util.ListUtilImpl;
import lucee.runtime.util.ORMUtil;
import lucee.runtime.util.ORMUtilImpl;
import lucee.runtime.util.Operation;
import lucee.runtime.util.PageContextUtil;
import lucee.runtime.util.Strings;
import lucee.runtime.util.SystemUtilImpl;
import lucee.runtime.util.TemplateUtil;
import lucee.runtime.util.TemplateUtilImpl;
import lucee.runtime.util.ZipUtil;
import lucee.runtime.util.ZipUtilImpl;
import lucee.runtime.video.VideoUtil;
import lucee.runtime.video.VideoUtilImpl;
import lucee.servlet.http.HTTPServletImpl;
import lucee.transformer.dynamic.DynamicInvoker;

/**
 * The CFMl Engine
 */
public final class CFMLEngineImpl implements CFMLEngine {

	static {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		System.setProperty("javax.xml.bind.context.factory", "com.sun.xml.bind.v2.ContextFactory");

	}

	public static final PrintStream CONSOLE_ERR = System.err;
	public static final PrintStream CONSOLE_OUT = System.out;

	private static Map<String, CFMLFactory> initContextes = MapFactory.<String, CFMLFactory>getConcurrentMap();
	private static Map<String, CFMLFactory> contextes = MapFactory.<String, CFMLFactory>getConcurrentMap();
	private ConfigServerImpl configServer = null;
	private static CFMLEngineImpl engine = null;
	private CFMLEngineFactory factory;
	private final ControllerStateImpl controlerState = new ControllerStateImpl(true);
	private boolean allowRequestTimeout = true;
	private Monitor monitor;
	private List<ServletConfig> servletConfigs = new ArrayList<ServletConfig>();
	private long uptime;
	private InfoImpl info;

	private BundleCollection bundleCollection;

	private ScriptEngineFactory scriptEngine;
	private ScriptEngineFactory tagEngine;
	private Controler controler;
	private CFMLServletContextListener scl;
	private Boolean asyncReqHandle;
	private String envExt;
	private boolean quick;

	// private static CFMLEngineImpl engine=new CFMLEngineImpl();

	private CFMLEngineImpl(CFMLEngineFactory factory, BundleCollection bc) {
		this.factory = factory;
		this.bundleCollection = bc;

		this.allowRequestTimeout = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.requesttimeout", null), true);
		// log the startup process
		String logDir = SystemUtil.getSystemPropOrEnvVar("startlogdirectory", null);// "/Users/mic/Tmp/");
		if (logDir != null) {
			File f = new File(logDir);
			if (f.isDirectory()) {
				String logName = SystemUtil.getSystemPropOrEnvVar("logName", "stacktrace");
				int timeRange = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("timeRange", "stacktrace"), 1);
				LogST._do(f, logName, timeRange);
			}
		}

		// happen when Lucee is loaded directly
		if (bundleCollection == null) {
			try {
				Properties prop = InfoImpl.getDefaultProperties(null);

				// read the config from default.properties
				Map<String, Object> config = new HashMap<String, Object>();
				Iterator<Entry<Object, Object>> it = prop.entrySet().iterator();
				Entry<Object, Object> e;
				String k;
				while (it.hasNext()) {
					e = it.next();
					k = (String) e.getKey();
					if (!k.startsWith("org.") && !k.startsWith("felix.")) continue;
					config.put(k, CFMLEngineFactorySupport.removeQuotes((String) e.getValue(), true));
				}

				config.put(Constants.FRAMEWORK_BOOTDELEGATION, "lucee.*");

				Felix felix = factory.getFelix(factory.getResourceRoot(), config);

				bundleCollection = new BundleCollection(felix, felix, null);
				// bundleContext=bundleCollection.getBundleContext();
			}
			catch (Exception e) {
				throw Caster.toPageRuntimeException(e);
			}
		}
		this.info = new InfoImpl(bundleCollection == null ? null : bundleCollection.core);
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); // MUST better location for this

		UpdateInfo updateInfo;
		Resource configDir = null;
		try {
			configDir = getSeverContextConfigDirectory(factory);
			updateInfo = ConfigFactory.getNew(this, configDir, true);
		}
		catch (Exception e) {
			throw Caster.toPageRuntimeException(e);
		}
		CFMLEngineFactory.registerInstance((this));// patch, not really good but it works
		ConfigServerImpl cs = getConfigServerImpl(null, quick = true, false);

		boolean isRe = configDir == null ? false : ConfigFactory.isRequiredExtension(this, configDir, null);
		boolean installExtensions = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.extensions.install", null), true);

		// copy bundled extension to local extension directory (if never done before)
		if (installExtensions && updateInfo.updateType != ConfigFactory.NEW_NONE) {
			int count = deployBundledExtension(cs, false);
			LogUtil.log(Log.LEVEL_INFO, "deploy", "controller",
					count == 0 ? "No new extension available to add to local extension directory" : "Copied [" + count + "] bundled extension(s) to local extension directory");
		}
		// required extensions

		// if we have a "fresh" install
		Set<ExtensionDefintion> extensions;
		Set<String> extensionsToRemove = null;

		if (installExtensions && (updateInfo.updateType == ConfigFactory.NEW_FRESH || updateInfo.updateType == ConfigFactory.NEW_FROM4)) {
			List<ExtensionDefintion> ext = info.getRequiredExtension();
			extensions = toSet(null, ext);
			LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Found Extensions to install (new;" + updateInfo.getUpdateTypeAsString() + "):" + toList(extensions));
		}
		// if we have an update we update the extension that re installed and we have an older version as
		// defined in the manifest
		else if (installExtensions && (updateInfo.updateType == ConfigFactory.NEW_MINOR || !isRe)) {
			extensions = new HashSet<ExtensionDefintion>();
			extensionsToRemove = new HashSet<String>();

			checkInvalidExtensions(this, cs, extensions, extensionsToRemove);

			Iterator<ExtensionDefintion> it = info.getRequiredExtension().iterator();
			ExtensionDefintion ed;
			RHExtension rhe;
			Version edVersion, rheVersion;
			while (it.hasNext()) {
				ed = it.next();
				edVersion = OSGiUtil.toVersion(ed.getVersion(), null);
				if (ed.getVersion() == null) {
					continue; // no version definition no update
				}
				try {
					rhe = ConfigAdmin.hasRHExtensionInstalled(cs, new ExtensionDefintion(ed.getId()));
					if (rhe == null) {
						rheVersion = null;
						Version since = ed.getSince();
						if (since == null || updateInfo.oldVersion == null || !OSGiUtil.isNewerThan(since, updateInfo.oldVersion)) continue; // not installed we do not update

						LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Detected newer [" + since + ":" + updateInfo.oldVersion + "] Extension version [" + ed + "]");
						extensions.add(ed);
					}
					else rheVersion = OSGiUtil.toVersion(rhe.getVersion(), null);
					// if the installed is older than the one defined in the manifest we update (if possible)
					if (rheVersion != null && OSGiUtil.isNewerThan(edVersion, rheVersion)) { // TODO do none OSGi version number comparsion
						LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Detected newer [" + edVersion + ":" + rheVersion + "] Extension version [" + ed + "]");
						extensions.add(ed);
					}
				}
				catch (Exception e) {
					LogUtil.log("deploy", "controller", e);
					extensions.add(ed);
				}
			}
			if (!extensions.isEmpty()) {
				LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Detected Extensions to install (minor;" + updateInfo.getUpdateTypeAsString() + "):" + toList(extensions));
			}
		}
		else {
			LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "No extension(s) found to add/install");

			extensions = new HashSet<ExtensionDefintion>();
		}

		// install extension defined
		String extensionIds = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("lucee-extensions", null)); // old no longer used
		if (StringUtil.isEmpty(extensionIds, true)) extensionIds = StringUtil.unwrap(SystemUtil.getSystemPropOrEnvVar("lucee.extensions", null));

		this.envExt = null;
		if (!StringUtil.isEmpty(extensionIds, true)) {
			this.envExt = extensionIds;
			LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Extensions to install defined in env variable or system property:" + extensionIds);
			List<ExtensionDefintion> _extensions = RHExtension.toExtensionDefinitions(extensionIds);
			extensions = toSet(extensions, _extensions);

		}

		if (extensions.size() > 0) {
			Map<ExtensionDefintion, Boolean> results = null;
			StringBuilder successSB = new StringBuilder();
			StringBuilder failedSB = new StringBuilder();
			boolean sucess = true;
			try {
				results = DeployHandler.deployExtensions(cs, extensions.toArray(new ExtensionDefintion[extensions.size()]), null, false, false);
				for (Entry<ExtensionDefintion, Boolean> e: results.entrySet()) {
					// failed
					if (!Boolean.TRUE.equals(e.getValue())) {
						sucess = false;
						if (failedSB.length() > 0) failedSB.append(", ");
						failedSB.append(e.getKey().toString());
					}
					// success
					else {
						if (successSB.length() > 0) successSB.append(", ");
						successSB.append(e.getKey().toString());
					}
				}

			}
			catch (PageException e) {
				LogUtil.log("deploy", "controller", e);
				sucess = false;
			}
			if (sucess && configDir != null) ConfigFactory.updateRequiredExtension(this, configDir, null);
			if (successSB.length() > 0) LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Successfully installed the following extensions: " + successSB);
			if (failedSB.length() > 0) LogUtil.log(Log.LEVEL_INFO, "deploy", "controller", "Failed to install the following extensions: " + failedSB);
		}
		else if (configDir != null) ConfigFactory.updateRequiredExtension(this, configDir, null);

		// extension to remove (we only have to remove in case we did not install an other version)

		if (extensionsToRemove != null) {
			for (ExtensionDefintion ed: extensions) {
				extensionsToRemove.remove(ed.getId());
			}
			if (!extensionsToRemove.isEmpty()) {
				// remove extension that are not valid (to new for current version)
				LogUtil.log(Log.LEVEL_ERROR, "deploy", ConfigWebFactory.class.getName(), "Uninstall extension(s) ["
						+ lucee.runtime.type.util.ListUtil.toList(extensionsToRemove, ", ") + "] because they are not supported for the current Lucee version.");
				try {
					ConfigAdmin.removeRHExtensions(null, null, lucee.runtime.type.util.ListUtil.toStringArray(extensionsToRemove), false);
					if (configDir != null) ConfigFactory.updateRequiredExtension(this, configDir, null);
				}
				catch (Exception e) {
					LogUtil.log("debug", ConfigWebFactory.class.getName(), e);
				}
			}
		}

		cs = getConfigServerImpl(cs, quick = false, false);
		Log log = null;
		if (cs != null) {
			try {
				log = cs.getLog("deploy", true);
			}
			catch (PageException e) {
			}
		}

		touchMonitor(cs);
		LogUtil.log(cs, Log.LEVEL_INFO, "startup", "Touched monitors");
		this.uptime = System.currentTimeMillis();

		// check deploy folder
		try {
			DeployHandler.deploy(cs, log, false);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		controler = new Controler(cs, initContextes, 5 * 1000, controlerState);
		controler.setDaemon(true);
		controler.setPriority(Thread.MIN_PRIORITY);

		boolean disabled = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar(SystemUtil.SETTING_CONTROLLER_DISABLED, null), false);
		if (!disabled) {
			// start the controller
			LogUtil.log(cs, Log.LEVEL_INFO, "startup", "Start CFML Controller");
			controler.start();
		}

		// remove old log4j bundles FUTURE remove
		try {
			OSGiUtil.removeLocalBundle("log4j", OSGiUtil.toVersion("1.2.16"), null, true, true);
			OSGiUtil.removeLocalBundle("log4j", OSGiUtil.toVersion("1.2.17"), null, true, true);
		}
		catch (Exception e) {
			LogUtil.log(cs, "startup", e);
		}
	}

	private static void checkInvalidExtensions(CFMLEngineImpl eng, ConfigPro config, Set<ExtensionDefintion> extensionsToInstall, Set<String> extensionsToRemove) {
		RHExtension[] extensions = config.getRHExtensions();
		if (extensions != null) {
			InfoImpl info = (InfoImpl) eng.getInfo();
			boolean valid;
			for (RHExtension ext: extensions) {

				try {
					ext.validate(info);
					valid = true;
				}
				catch (ApplicationException ae) {
					valid = false;
					LogUtil.log("debug", "check-invalid-extension", ae);
				}
				if (!valid) {
					try {
						ExtensionDefintion ed = getRequiredExtension(info, ext.getId());

						if (ed != null) {
							extensionsToInstall.add(ed);
							LogUtil.log(Log.LEVEL_INFO, "debug", "check-invalid-extension",
									"Installed extension [" + ext + "] is invalid and get removed and replaced by [" + ed + "]");
						}
						else {
							extensionsToRemove.add(ext.toExtensionDefinition().getId());
							LogUtil.log(Log.LEVEL_INFO, "debug", "check-invalid-extension", "Installed extension [" + ext + "] is invalid and was removed.");

						}
					}
					catch (Exception e) {
						LogUtil.log("debug", ConfigWebFactory.class.getName(), e);
					}
				}
			}
		}
	}

	private static ExtensionDefintion getRequiredExtension(InfoImpl info, String id) {
		List<ExtensionDefintion> reqExt = info.getRequiredExtension();
		if (reqExt != null) {
			for (ExtensionDefintion ed: reqExt) {
				if (ed.getId().equals(id)) return ed;
			}
		}
		return null;
	}

	public static Set<ExtensionDefintion> toSet(Set<ExtensionDefintion> set, List<ExtensionDefintion> list) {
		LinkedHashMap<String, ExtensionDefintion> map = new LinkedHashMap<String, ExtensionDefintion>();
		ExtensionDefintion ed;

		// set > map
		if (set != null) {
			Iterator<ExtensionDefintion> it = set.iterator();
			while (it.hasNext()) {
				ed = it.next();
				map.put(ed.toString(), ed);
			}
		}

		// list > map
		if (list != null) {
			Iterator<ExtensionDefintion> it = list.iterator();
			while (it.hasNext()) {
				ed = it.next();
				map.put(ed.toString(), ed);
			}
		}

		// to Set
		LinkedHashSet<ExtensionDefintion> rtn = new LinkedHashSet<ExtensionDefintion>();
		Iterator<ExtensionDefintion> it = map.values().iterator();
		while (it.hasNext()) {
			ed = it.next();
			rtn.add(ed);
		}
		return rtn;
	}

	public static String toList(Collection<ExtensionDefintion> coll) {
		StringBuilder sb = new StringBuilder();
		Iterator<ExtensionDefintion> it = coll.iterator();
		ExtensionDefintion ed;
		while (it.hasNext()) {
			ed = it.next();
			if (sb.length() > 0) sb.append(", ");
			sb.append(ed.toString());
		}
		return sb.toString();
	}

	public int deployBundledExtension(boolean validate) {
		return deployBundledExtension(getConfigServerImpl(null, false, true), validate);
	}

	private int deployBundledExtension(ConfigServerImpl cs, boolean validate) {
		int count = 0;
		Resource dir = cs.getLocalExtensionProviderDirectory();
		List<ExtensionDefintion> existing = DeployHandler.getLocalExtensions(cs, validate);
		Map<String, ExtensionDefintion> existingMap = new HashMap<String, ExtensionDefintion>();

		{
			Iterator<ExtensionDefintion> it = existing.iterator();
			ExtensionDefintion ed;
			while (it.hasNext()) {
				ed = it.next();
				try {
					existingMap.put(ed.getSource().getName(), ed);
				}
				catch (ApplicationException e) {
				}
			}
		}

		Log log = cs.getLog("deploy");

		// get the index
		ClassLoader cl = CFMLEngineFactory.getInstance().getCFMLEngineFactory().getClass().getClassLoader();
		InputStream is = cl.getResourceAsStream("extensions/.index");
		if (is == null) is = cl.getResourceAsStream("/extensions/.index");
		if (is == null) is = SystemUtil.getResourceAsStream(null, "/extensions/.index");

		if (is == null) {
			log.error("extract-extension", "Could not find [/extensions/.index] defined in the index of the lucee.jar");
			return count;
		}

		try {

			String index = IOUtil.toString(is, CharsetUtil.UTF8);
			// log.info("extract-extension", "the following extensions are bundled with the lucee.jar [" + index
			// + "]");

			String[] names = lucee.runtime.type.util.ListUtil.listToStringArray(index, ';');
			String name;
			Resource temp = null;
			RHExtension rhe;
			ExtensionDefintion exist;
			Iterator<ExtensionDefintion> it;

			for (int i = 0; i < names.length; i++) {
				name = names[i];
				if (StringUtil.isEmpty(name, true)) continue;
				name = name.trim();

				// does it already exist?
				if (existingMap.containsKey(name)) {
					continue;
				}
				log.info("extract-extension", "Extract the extension [" + name + "] from the lucee.jar to the local extension folder [" + dir + "]");

				is = cl.getResourceAsStream("extensions/" + name);
				if (is == null) is = cl.getResourceAsStream("/extensions/" + name);
				if (is == null) {
					log.error("extract-extension", "Could not find extension [" + name + "] defined in the index in the lucee.jar");
					continue;
				}

				try {
					temp = SystemUtil.getTempDirectory().getRealResource(name);
					log.info("extract-extension", "Copy extension [" + name + "] to temp directory [" + temp + "]");
					ResourceUtil.touch(temp);
					Util.copy(is, temp.getOutputStream(), false, true);
					rhe = new RHExtension(cs, temp);
					rhe.validate();
					ExtensionDefintion alreadyExists = null;
					it = existing.iterator();
					while (it.hasNext()) {
						exist = it.next();
						if (exist.equals(rhe)) {
							alreadyExists = exist;
							break;
						}
					}

					String trgName = rhe.getId() + "-" + rhe.getVersion() + ".lex";
					if (alreadyExists == null) {
						temp.moveTo(dir.getRealResource(trgName));
						count++;
						log.debug("extract-extension", "Added [" + name + "] to [" + dir + "]");
					}
					else if (!alreadyExists.getSource().getName().equals(trgName)) {
						log.debug("extract-extension", "Rename [" + alreadyExists.getSource() + "] to [" + trgName + "]");
						alreadyExists.getSource().moveTo(alreadyExists.getSource().getParentResource().getRealResource(trgName));
					}
					else {
						log.info("extract-extension", "Extension  [" + name + "] already exists in local extension directory");
					}

					// now we check all extension name (for extension no longer delivered by lucee)
					it = existing.iterator();
					while (it.hasNext()) {
						exist = it.next();
						trgName = exist.getId() + "-" + exist.getVersion() + ".lex";
						if (!trgName.equals(exist.getSource().getName())) {
							exist.getSource().moveTo(exist.getSource().getParentResource().getRealResource(trgName));
							log.debug("extract-extension", "Rename [" + exist.getSource() + "] to [" + trgName + "]");

						}
					}
				}
				catch (Exception e) {
					log.error("extract-extension", e);
				}
				finally {
					if (temp != null && temp.exists()) temp.delete();
				}
			}
		}
		catch (Exception e) {
			log.error("extract-extension", e);
		}
		return count;
	}

	public void touchMonitor(ConfigServerImpl cs) {
		if (monitor != null && monitor.isAlive()) return;
		monitor = new Monitor(cs, controlerState);
		monitor.setDaemon(true);
		monitor.setPriority(Thread.MIN_PRIORITY);
		monitor.start();
	}

	/**
	 * get singelton instance of the CFML Engine
	 * 
	 * @param factory
	 * @return CFMLEngine
	 */
	public static synchronized CFMLEngine getInstance(CFMLEngineFactory factory, BundleCollection bc) {
		if (engine == null) {
			if (SystemUtil.getLoaderVersion() < 6.0D) {
				// windows needs 6.0 because restart is not working with older versions
				if (SystemUtil.isWindows())
					throw new RuntimeException("You need to update a newer lucee.jar to run this version, you can download the latest jar from https://download.lucee.org.");
				else if (SystemUtil.getLoaderVersion() < 5.8D)
					throw new RuntimeException("You need to update your lucee.jar to run this version, you can download the latest jar from https://download.lucee.org.");
				else if (SystemUtil.getLoaderVersion() < 5.9D) LogUtil.log(Log.LEVEL_INFO, "startup",
						"To use all features Lucee provides, you need to update your lucee.jar, you can download the latest jar from https://download.lucee.org.");
			}
			engine = new CFMLEngineImpl(factory, bc);

		}
		return engine;
	}

	/**
	 * get singelton instance of the CFML Engine, throwsexception when not already init
	 * 
	 * @param factory
	 * @return CFMLEngine
	 */
	public static synchronized CFMLEngine getInstance() throws PageServletException {
		if (engine != null) return engine;
		throw new PageServletException(new ApplicationException("CFML Engine is not loaded"));
	}

	@Override
	public void addServletConfig(ServletConfig config) throws PageServletException {
		if (PageSourceImpl.logAccessDirectory == null) {
			String str = config.getInitParameter("lucee-log-access-directory");
			if (!StringUtil.isEmpty(str)) {
				File file = new File(str.trim());
				file.mkdirs();
				if (file.isDirectory()) {
					PageSourceImpl.logAccessDirectory = file;
				}
			}
		}

		// FUTURE remove and add a new method for it (search:FUTURE add exeServletContextEvent)
		if ("LuceeServletContextListener".equals(config.getServletName())) {
			try {
				String status = config.getInitParameter("status");
				if ("release".equalsIgnoreCase(status)) reset();
			}
			catch (Exception e) {
				LogUtil.log(configServer, "startup", e);
			}
			return;
		}

		// add EventListener
		if (scl == null) {
			addEventListener(config.getServletContext());
		}

		servletConfigs.add(config);
		String real = ReqRspUtil.getRootPath(config.getServletContext());
		if (!initContextes.containsKey(real)) {
			CFMLFactory jspFactory = loadJSPFactory(getConfigServerImpl(null, false, false), config, initContextes.size());
			initContextes.put(real, jspFactory);
		}
	}

	private void addEventListener(ServletContext sc) {
		// TOMCAT
		if ("org.apache.catalina.core.ApplicationContextFacade".equals(sc.getClass().getName())) {
			Object obj = extractServletContext(sc);
			obj = extractServletContext(obj);
			if ("org.apache.catalina.core.StandardContext".equals(obj.getClass().getName())) {
				Method m = null;
				try {
					// TODO check if we already have a listener (lucee.loader.servlet.LuceeServletContextListener), if
					// so we do nothing
					// sc.getApplicationLifecycleListeners();
					m = obj.getClass().getMethod("addApplicationLifecycleListener", new Class[] { Object.class });
					CFMLServletContextListener tmp;
					m.invoke(obj, new Object[] { tmp = new CFMLServletContextListener(this) });
					scl = tmp;
					return;
				}
				catch (Exception e) {
					// because this is optional and not all servlet engine do support this, we keep the log level on
					// info
					LogUtil.log(configServer, "application", "add-event-listener", e, Log.LEVEL_INFO);
				}

			}
		}

		// GENERAL try add Event method directly (does not work with tomcat)
		if (!ServletContextImpl.class.getName().equals(sc.getClass().getName())) { // ServletContextImpl does not support addListener
			try {
				CFMLServletContextListener tmp = new CFMLServletContextListener(this);
				sc.addListener(tmp);
				scl = tmp;
				return;
			}
			catch (Exception e) {
				// because this is optional and not all servlet engine do support this, we keep the log level on
				// info
				LogUtil.log(configServer, "application", "add-event-listener", e, Log.LEVEL_INFO);
			}
		}

		LogUtil.log(configServer, Log.LEVEL_INFO, "startup", "Lucee was not able to register an event listener with " + (sc == null ? "null" : sc.getClass().getName()));
	}

	private Object extractServletContext(Object sc) {
		Class<?> clazz = sc.getClass();
		Field f = null;
		try {
			f = clazz.getDeclaredField("context");
		}
		catch (Exception e) {
			LogUtil.log(configServer, "extract-servlet-context", e);
		}
		if (f != null) {
			f.setAccessible(true);
			Object obj = null;
			try {
				obj = f.get(sc);
			}
			catch (Exception e) {
				LogUtil.log(configServer, "extract-servlet-context", e);
			}
			return obj;
		}
		return null;
	}

	@Override
	public ConfigServer getConfigServer(Password password) throws PageException {
		getConfigServerImpl(null, false, false).checkAccess(password);
		return configServer;
	}

	@Override
	public ConfigServer getConfigServer(String key, long timeNonce) throws PageException {
		getConfigServerImpl(null, false, false).checkAccess(key, timeNonce);
		return configServer;
	}

	public void setConfigServerImpl(ConfigServerImpl cs) {
		this.configServer = cs;
	}

	private ConfigServerImpl getConfigServerImpl(ConfigServerImpl existing, boolean essentialOnly, boolean allowGrapingThreadConfig) {
		if (configServer == null) {
			// if in process to be build, this may only exists with the thread yet
			if (allowGrapingThreadConfig) {
				Config config = ThreadLocalPageContext.getConfig();
				if (config instanceof ConfigServerImpl) {
					return (ConfigServerImpl) config;
				}
				if (config instanceof ConfigWebImpl) {
					return ((ConfigWebImpl) config).getConfigServerImpl();
				}
			}
			try {
				Resource context = getSeverContextConfigDirectory(factory);
				DynamicInvoker.getInstance(context);
				ConfigServerImpl tmp = ConfigServerFactory.newInstance(this, initContextes, contextes, context, existing, essentialOnly);
				if (essentialOnly) {
					return tmp;
				}
				configServer = tmp;
			}
			catch (Exception e) {
				e.printStackTrace();
				LogUtil.log(configServer, "startup", e);
			}
		}
		return configServer;
	}

	private ConfigServerImpl getExistingConfigServerImpl() {
		if (configServer != null) return configServer;

		// if in process to be build, this may only exists with the thread yet
		Config config = ThreadLocalPageContext.getConfig();
		if (config instanceof ConfigServerImpl) return (ConfigServerImpl) config;
		if (config instanceof ConfigWebImpl) {
			return ((ConfigWebImpl) config).getConfigServerImpl();
		}

		return null;
	}

	public static Resource getSeverContextConfigDirectory(CFMLEngineFactory factory) throws IOException {
		ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
		return frp.getResource(factory.getResourceRoot().getAbsolutePath()).getRealResource("context");
	}

	private CFMLFactoryImpl loadJSPFactory(ConfigServerImpl configServer, ServletConfig sg, int countExistingContextes) throws PageServletException {
		try {
			CFMLFactoryImpl factory = new CFMLFactoryImpl(this, sg);
			if (ConfigWebFactory.LOG) LogUtil.log(configServer, Log.LEVEL_INFO, "startup", "Init factory");

			boolean multi = configServer.getAdminMode() == ConfigImpl.ADMINMODE_MULTI;
			ConfigWebPro config;
			RefBoolean isCustomSetting = new RefBooleanImpl();
			Resource configDir = getConfigDirectory(sg, configServer, countExistingContextes, isCustomSetting);
			if (multi) {
				config = ConfigWebFactory.newInstanceMulti(this, factory, configServer, configDir, sg, null);
			}
			else {
				config = ConfigWebFactory.newInstanceSingle(this, factory, configServer, configDir, sg, null);
			}

			if (ConfigWebFactory.LOG) LogUtil.log(configServer, Log.LEVEL_INFO, "startup", "Loaded config");
			factory.setConfig(configServer, config);
			return factory;
		}
		catch (Exception e) {
			throw Caster.toPageServletException(e);
		}
	}

	/**
	 * loads Configuration File from System, from init Parameter from web.xml
	 * 
	 * @param sg
	 * @param configServer
	 * @param countExistingContextes
	 * @return return path to directory
	 */
	private Resource getConfigDirectory(ServletConfig sg, ConfigServerImpl configServer, int countExistingContextes, RefBoolean isCustomSetting) throws PageServletException {
		isCustomSetting.setValue(true);
		ServletContext sc = sg.getServletContext();
		String strConfig = sg.getInitParameter("configuration");
		if (StringUtil.isEmpty(strConfig)) strConfig = sg.getInitParameter("lucee-web-directory");
		if (StringUtil.isEmpty(strConfig)) strConfig = System.getProperty("lucee.web.dir");

		if (StringUtil.isEmpty(strConfig)) {
			isCustomSetting.setValue(false);
			strConfig = "{web-root-directory}/WEB-INF/lucee/";
		}
		// only for backward compatibility
		else if (strConfig.startsWith("/WEB-INF/lucee/")) strConfig = "{web-root-directory}" + strConfig;

		strConfig = StringUtil.removeQuotes(strConfig, true);

		// static path is not allowed
		if (countExistingContextes > 1 && strConfig != null && strConfig.indexOf('{') == -1) {
			String text = "Static path [" + strConfig + "] for servlet init param [lucee-web-directory] is not allowed, path must use a web-context specific placeholder.";
			LogUtil.log(configServer, Log.LEVEL_ERROR, CFMLEngineImpl.class.getName(), text);
			throw new PageServletException(new ApplicationException(text));
		}
		strConfig = SystemUtil.parsePlaceHolder(strConfig, sc, configServer.getLabels());

		ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
		Resource root = frp.getResource(ReqRspUtil.getRootPath(sc));
		Resource res;
		Resource configDir = ResourceUtil.createResource(res = root.getRealResource(strConfig), FileUtil.LEVEL_PARENT_FILE, FileUtil.TYPE_DIR);

		if (configDir == null) {
			configDir = ResourceUtil.createResource(res = frp.getResource(strConfig), FileUtil.LEVEL_GRAND_PARENT_FILE, FileUtil.TYPE_DIR);
		}

		if (configDir == null && !isCustomSetting.toBooleanValue()) {
			try {
				res.createDirectory(true);
				configDir = res;
			}
			catch (IOException e) {
				throw new PageServletException(Caster.toPageException(e));
			}
		}
		if (configDir == null) {
			throw new PageServletException(new ApplicationException("path [" + strConfig + "] is invalid"));
		}

		if (!configDir.exists() || ResourceUtil.isEmptyDirectory(configDir, null)) {
			Resource railoRoot;
			// there is a railo directory
			if (configDir.getName().equals("lucee") && (railoRoot = configDir.getParentResource().getRealResource("railo")).isDirectory()) {
				try {
					copyRecursiveAndRename(railoRoot, configDir);
				}
				catch (IOException e) {
					try {
						if (!configDir.isDirectory()) configDir.createDirectory(true);
					}
					catch (IOException ioe) {
						LogUtil.log(configServer, "config-directory", ioe);
					}
					return configDir;
				}
				// zip the railo-server di and delete it (optional)
				try {
					Resource p = railoRoot.getParentResource();
					CompressUtil.compress(CompressUtil.FORMAT_ZIP, railoRoot, p.getRealResource("railo-web-context-old.zip"), false, -1);
					ResourceUtil.removeEL(railoRoot, true);
				}
				catch (Exception e) {
					LogUtil.log(configServer, "controller", e);
				}
			}
			else if (!configDir.exists()) {
				try {
					configDir.createDirectory(true);
				}
				catch (IOException e) {
					LogUtil.log(configServer, "controller", e);
				}
			}
		}
		return configDir;
	}

	private static void copyRecursiveAndRename(Resource src, Resource trg) throws IOException {
		if (!src.exists()) return;
		if (src.isDirectory()) {
			if (!trg.exists()) trg.mkdirs();

			Resource[] files = src.listResources();
			for (int i = 0; i < files.length; i++) {
				copyRecursiveAndRename(files[i], trg.getRealResource(files[i].getName()));
			}
		}
		else if (src.isFile()) {
			if (trg.getName().endsWith(".rc") || trg.getName().startsWith(".")) {
				return;
			}

			if (trg.getName().equals("railo-web.xml.cfm")) {
				trg = trg.getParentResource().getRealResource("lucee-web.xml.cfm");
				// cfLuceeConfiguration
				InputStream is = src.getInputStream();
				OutputStream os = trg.getOutputStream();
				try {
					String str = Util.toString(is);
					str = str.replace("<cfRailoConfiguration", "<!-- copy from Railo context --><cfLuceeConfiguration");
					str = str.replace("</cfRailoConfiguration", "</cfLuceeConfiguration");
					str = str.replace("<railo-configuration", "<lucee-configuration");
					str = str.replace("</railo-configuration", "</lucee-configuration");
					str = str.replace("{railo-config}", "{lucee-config}");
					str = str.replace("{railo-server}", "{lucee-server}");
					str = str.replace("{railo-web}", "{lucee-web}");
					str = str.replace("\"railo.commons.", "\"lucee.commons.");
					str = str.replace("\"railo.runtime.", "\"lucee.runtime.");
					str = str.replace("\"railo.cfx.", "\"lucee.cfx.");
					str = str.replace("/railo-context.ra", "/lucee-context.lar");
					str = str.replace("/railo-context", "/lucee");
					str = str.replace("railo-server-context", "lucee-server");
					str = str.replace("http://www.getrailo.org", "https://update.lucee.org");
					str = str.replace("http://www.getrailo.com", "https://update.lucee.org");

					ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());

					try {
						Util.copy(bais, os);
						bais.close();
					}
					finally {
						Util.closeEL(is, os);
					}
				}
				finally {
					Util.closeEL(is, os);
				}
				return;
			}

			InputStream is = src.getInputStream();
			OutputStream os = trg.getOutputStream();
			try {
				Util.copy(is, os);
			}
			finally {
				Util.closeEL(is, os);
			}
		}
	}

	@Override
	public CFMLFactory getCFMLFactory(ServletConfig srvConfig, HttpServletRequest req) throws PageServletException {
		return getCFMLFactory(null, srvConfig, req);
	}

	public CFMLFactory getCFMLFactory(ConfigServerImpl cs, ServletConfig srvConfig, HttpServletRequest req) throws PageServletException {
		ServletContext srvContext = srvConfig.getServletContext();

		String real = ReqRspUtil.getRootPath(srvContext);
		if (cs == null) cs = getConfigServerImpl(null, false, true);

		// Load JspFactory

		CFMLFactory factory = contextes.get(real);
		if (factory == null) {
			factory = initContextes.get(real);
			if (factory == null) {
				factory = loadJSPFactory(cs, srvConfig, initContextes.size());
				initContextes.put(real, factory);
			}

			contextes.put(real, factory);

			try {
				String cp = req.getContextPath();
				if (cp == null) cp = "";
				((CFMLFactoryImpl) factory).setURL(new URL(req.getScheme(), req.getServerName(), req.getServerPort(), cp));
			}
			catch (MalformedURLException e) {
				LogUtil.log(cs, "startup", e);
			}
		}
		return factory;
	}

	@Override
	public void service(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws PageServletException, IOException {
		_service(servlet, req, rsp, Request.TYPE_CFML);
	}

	@Override
	public void serviceCFML(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws PageServletException, IOException {
		_service(servlet, req, rsp, Request.TYPE_CFML);
	}

	@Override
	public void serviceRest(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws PageServletException, IOException {
		_service(servlet, new HTTPServletRequestWrap(req), rsp, Request.TYPE_REST);
	}

	private void _service(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp, short type) throws PageServletException {
		CFMLFactoryImpl factory = (CFMLFactoryImpl) getCFMLFactory(servlet.getServletConfig(), req);
		boolean exeReqAsync = exeRequestAsync();
		PageContextImpl pc = factory.getPageContextImpl(servlet, req, rsp, null, false, -1, false, !exeReqAsync, false, -1, true, false, false, null);
		try {
			Request r = new Request(pc, type);
			if (exeReqAsync) {
				r.start();
				long ended = -1;
				do {
					SystemUtil.wait(Thread.currentThread(), 1000);
					// done?
					if (r.isDone()) {
						// print.e("mas-done:"+System.currentTimeMillis());
						break;
					}
					// reach request timeout
					else if (ended == -1 && (pc.getStartTime() + pc.getRequestTimeout()) < System.currentTimeMillis()) {
						// print.e("req-time:"+System.currentTimeMillis());
						CFMLFactoryImpl.terminate(pc, false);
						ended = System.currentTimeMillis();
						// break; we do not break here, we give the thread itself the chance to end we need the exception
						// output
					}
					// the thread itself seem blocked, so we release this thread
					else if (ended > -1 && ended + 10000 <= System.currentTimeMillis()) {
						// print.e("give-up:"+System.currentTimeMillis());
						break;
					}
				}
				while (true);
			}
			// run in thread coming from servlet engine
			else {
				try {
					Request.exe(pc, type, true, false);
				}
				catch (RequestTimeoutException rte) {
					if (rte.getThreadDeath() != null) throw rte.getThreadDeath();
				}
				catch (NativeException ne) {
					ExceptionUtil.rethrowIfNecessary(ne.getCause());
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					if (t instanceof Exception && !Abort.isSilentAbort(t))
						LogUtil.log(configServer, "application", "controller", t, t instanceof MissingIncludeException ? Log.LEVEL_WARN : Log.LEVEL_ERROR);
				}
			}
		}
		finally {
			factory.releaseLuceePageContext(pc, !exeReqAsync);
		}
	}

	@Override
	public void serviceFile(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws PageServletException, IOException {
		req = new HTTPServletRequestWrap(req);
		CFMLFactory factory = getCFMLFactory(servlet.getServletConfig(), req);
		ConfigWeb config = factory.getConfig();
		PageSource ps = config.getPageSourceExisting(null, null, req.getServletPath(), false, true, true, false);

		if (ps == null) {
			rsp.sendError(404);
		}
		else {
			Resource res = ps.getResource();
			if (res == null) {
				rsp.sendError(404);
			}
			else {
				ReqRspUtil.setContentLength(rsp, res.length());
				String mt = servlet.getServletContext().getMimeType(req.getServletPath());
				if (!StringUtil.isEmpty(mt)) ReqRspUtil.setContentType(rsp, mt);
				IOUtil.copy(res, rsp.getOutputStream(), true);
			}
		}
	}

	/*
	 * private String getContextList() { return
	 * List.arrayToList((String[])contextes.keySet().toArray(new String[contextes.size()]),", "); }
	 */

	@Override
	public String getVersion() {
		return info.getVersion().toString();
	}

	@Override
	public Info getInfo() {
		return info;
	}

	@Override
	public String getUpdateType() {
		ConfigServerImpl cs = getExistingConfigServerImpl();
		if (cs != null) return cs.getUpdateType();
		return lucee.runtime.config.Constants.DEFAULT_UPDATE_TYPE;
	}

	@Override
	public URL getUpdateLocation() {
		ConfigServerImpl cs = getExistingConfigServerImpl();
		if (cs != null) return cs.getUpdateLocation();
		return lucee.runtime.config.Constants.DEFAULT_UPDATE_URL;
	}

	@Override
	public Identification getIdentification() {
		return getConfigServerImpl(null, false, true).getIdentification();
	}

	@Override
	public boolean can(int type, Password password) {
		return getConfigServerImpl(null, false, true).passwordEqual(password);
	}

	@Override
	public CFMLEngineFactory getCFMLEngineFactory() {
		return factory;
	}

	@Override
	public void serviceAMF(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws PageServletException, IOException {
		throw new PageServletException(new ApplicationException("AMFServlet is no longer supported, use BrokerServlet instead."));
		// req=new HTTPServletRequestWrap(req);
		// getCFMLFactory(servlet.getServletConfig(), req).getConfig().getAMFEngine().service(servlet,new
		// HTTPServletRequestWrap(req),rsp);
	}

	@Override
	public void reset() {
		reset(null);
	}

	@Override
	public void reset(String configId) {
		if (!controlerState.active()) return;
		controlerState.setActive(false);
		try {
			LogUtil.log(configServer, Log.LEVEL_INFO, "startup", "Reset CFML Engine");

			RetireOutputStreamFactory.close();

			Controler cntr = getControler();
			if (cntr != null) cntr.close();

			// release HTTP Pool
			HTTPEngine4Impl.releaseConnectionManager();

			releaseCache(getConfigServerImpl(null, false, true));

			CFMLFactoryImpl cfmlFactory;
			// ScopeContext scopeContext;

			Iterator<Entry<String, CFMLFactory>> it = initContextes.entrySet().iterator();
			Entry<String, CFMLFactory> e;
			ConfigWeb config;
			while (it.hasNext()) {
				e = it.next();
				try {
					cfmlFactory = (CFMLFactoryImpl) e.getValue();
					config = cfmlFactory.getConfig();

					if (config != null && config.getIdentification() != null && configId != null && !configId.equals(config.getIdentification().getId())) continue;

					// RAM cache
					RamCache.doNotifyAll(this);

					// scheduled tasks
					SchedulerImpl scheduler = ((SchedulerImpl) config.getScheduler());
					if (scheduler != null) scheduler.stop();

					// scopes
					try {
						cfmlFactory.getScopeContext().clear();
					}
					catch (Exception ee) {
						LogUtil.log(configServer, "controller", ee);
					}

					// PageContext
					try {
						cfmlFactory.resetPageContext();
					}
					catch (Exception ee) {
						LogUtil.log(configServer, "controller", ee);
					}

					// Query Cache
					try {
						PageContext pc = ThreadLocalPageContext.get();
						if (pc != null) {
							pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).clear(pc);
							pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_FUNCTION, null).clear(pc);
							pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_INCLUDE, null).clear(pc);
						}
						// cfmlFactory.getDefaultQueryCache().clear(null);
					}
					catch (Exception ee) {
						LogUtil.log(configServer, "controller", ee);
					}

					// Gateway
					try {
						((GatewayEngineImpl) ((ConfigWebPro) cfmlFactory.getConfig()).getGatewayEngine()).reset(false);
					}
					catch (Exception ee) {
						LogUtil.log(configServer, "controller", ee);
					}

					// Cache
					releaseCache(cfmlFactory.getConfig());

				}
				catch (Exception ex) {
					LogUtil.log(configServer, "controller", ex);
				}
			}

			// release felix itself
			shutdownFelix();

		}
		catch (Exception ee) {
			LogUtil.logGlobal(configServer, "reset-engine", ee);
		}
		finally {
			// Controller
			controlerState.setActive(false);
		}
	}

	/*
	 * private void dump() { Iterator<Entry<Thread, StackTraceElement[]>> it =
	 * Thread.getAllStackTraces().entrySet().iterator(); while (it.hasNext()) { Entry<Thread,
	 * StackTraceElement[]> e = it.next(); print.e(e.getKey().getContextClassLoader());
	 * print.e(e.getValue()); }
	 * 
	 * }
	 */

	private void shutdownFelix() {
		CFMLEngineFactory f = getCFMLEngineFactory();
		try {
			Method m = f.getClass().getMethod("shutdownFelix", new Class[0]);
			m.invoke(f, new Object[0]);
		}
		// FUTURE do not use reflection
		// this will for sure fail if CFMLEngineFactory does not have this method
		catch (Exception e) {
			LogUtil.log(configServer, "controller", e);
		}
	}

	public static void releaseCache(Config config) {
		CacheUtil.releaseAll(config);
		if (config instanceof ConfigServer) CacheUtil.releaseAllApplication();
	}

	@Override
	public Cast getCastUtil() {
		return CastImpl.getInstance();
	}

	@Override
	public Operation getOperatonUtil() { // FUTURE rename to getOperationUtil()
		return OperationImpl.getInstance();
	}

	@Override
	public Decision getDecisionUtil() {
		return DecisionImpl.getInstance();
	}

	@Override
	public Excepton getExceptionUtil() {
		return ExceptonImpl.getInstance();
	}

	@Override
	public Object getJavaProxyUtil() { // FUTURE return JavaProxyUtil
		return new JavaProxyUtilImpl();
	}

	@Override
	public Creation getCreationUtil() {
		return CreationImpl.getInstance(this);
	}

	@Override
	public IO getIOUtil() {
		return IOImpl.getInstance();
	}

	@Override
	public Strings getStringUtil() {
		return StringsImpl.getInstance();
	}

	@Override
	public Object getFDController() {
		engine.allowRequestTimeout(false);

		return new FDControllerImpl(engine, engine.getConfigServerImpl(null, false, true).getSerialNumber());
	}

	public Map<String, CFMLFactory> getCFMLFactories() {
		return initContextes;
	}

	@Override
	public lucee.runtime.util.ResourceUtil getResourceUtil() {
		return ResourceUtilImpl.getInstance();
	}

	@Override
	public lucee.runtime.util.HTTPUtil getHTTPUtil() {
		return HTTPUtilImpl.getInstance();
	}

	@Override
	public PageContext getThreadPageContext() {
		return ThreadLocalPageContext.get();
	}

	@Override
	public Config getThreadConfig() {
		return ThreadLocalPageContext.getConfig();
	}

	@Override
	public void registerThreadPageContext(PageContext pc) {
		ThreadLocalPageContext.register(pc);
	}

	@Override
	public VideoUtil getVideoUtil() {
		return VideoUtilImpl.getInstance();
	}

	@Override
	public ZipUtil getZipUtil() {
		return ZipUtilImpl.getInstance();
	}

	/*
	 * public String getState() { return info.getStateAsString(); }
	 */

	public void allowRequestTimeout(boolean allowRequestTimeout) {
		this.allowRequestTimeout = allowRequestTimeout;
	}

	public boolean allowRequestTimeout() {
		return allowRequestTimeout;
	}

	public boolean isRunning() {
		try {
			CFMLEngine other = CFMLEngineFactory.getInstance();
			// FUTURE patch, do better impl when changing loader
			if (other != this && controlerState.active() && !(other instanceof CFMLEngineWrapper)) {
				LogUtil.log(configServer, Log.LEVEL_INFO, "startup",
						"CFMLEngine is still set to true but no longer valid, " + lucee.runtime.config.Constants.NAME + " disable this CFMLEngine.");
				controlerState.setActive(false);
				reset();
				return false;
			}
		}
		catch (Exception e) {
			LogUtil.log(configServer, "controller", e);
		}
		return controlerState.active();
	}

	public boolean active() {
		return controlerState.active();
	}

	public ControllerState getControllerState() {
		return controlerState;
	}

	@Override
	public void cli(Map<String, String> config, ServletConfig servletConfig) throws IOException, PageServletException, PageException {
		ServletContext servletContext = servletConfig.getServletContext();
		HTTPServletImpl servlet = new HTTPServletImpl(servletConfig, servletContext, servletConfig.getServletName());

		// webroot
		String strWebroot = config.get("webroot");
		if (StringUtil.isEmpty(strWebroot, true)) throw new IOException("Missing webroot configuration");
		Resource root = ResourcesImpl.getFileResourceProvider().getResource(strWebroot);
		root.mkdirs();

		// serverName
		String serverName = config.get("server-name");
		if (StringUtil.isEmpty(serverName, true)) serverName = "localhost";

		// uri
		String strUri = config.get("uri");
		if (StringUtil.isEmpty(strUri, true)) throw new IOException("Missing uri configuration");
		URI uri;
		try {
			uri = lucee.commons.net.HTTPUtil.toURI(strUri);
		}
		catch (URISyntaxException e) {
			throw Caster.toPageException(e);
		}

		// cookie
		Cookie[] cookies;
		String strCookie = config.get("cookie");
		if (StringUtil.isEmpty(strCookie, true)) cookies = SerializableCookie.COOKIES0;
		else {
			Map<String, String> mapCookies = HTTPUtil.parseParameterList(strCookie, false, null);
			int index = 0;
			cookies = new Cookie[mapCookies.size()];
			Entry<String, String> entry;
			Iterator<Entry<String, String>> it = mapCookies.entrySet().iterator();
			Cookie c;
			while (it.hasNext()) {
				entry = it.next();
				c = ReqRspUtil.toCookie(entry.getKey(), entry.getValue(), null);
				if (c != null) cookies[index++] = c;
				else throw new IOException("Cookie name [" + entry.getKey() + "] is invalid");
			}
		}

		// header
		Pair[] headers = new Pair[0];

		// parameters
		Pair[] parameters = new Pair[0];

		// attributes
		StructImpl attributes = new StructImpl();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		HttpServletRequestDummy req = new HttpServletRequestDummy(root, serverName, uri.getPath(), uri.getQuery(), cookies, headers, parameters, attributes, null, null);
		req.setProtocol("CLI/1.0");
		HttpServletResponse rsp = new HttpServletResponseDummy(os);

		serviceCFML(servlet, req, rsp);
		String res = os.toString(ReqRspUtil.getCharacterEncoding(null, rsp).name());
		// System. out.println(res);
	}

	@Override
	public ServletConfig[] getServletConfigs() {
		return servletConfigs.toArray(new ServletConfig[servletConfigs.size()]);
	}

	@Override
	public long uptime() {
		return uptime;
	}

	/*
	 * public Bundle getCoreBundle() { return bundle; }
	 */

	@Override
	public BundleCollection getBundleCollection() {
		return bundleCollection;
	}

	@Override
	public BundleContext getBundleContext() {
		return bundleCollection.getBundleContext();
	}

	@Override
	public ClassUtil getClassUtil() {
		return new ClassUtilImpl();
	}

	@Override
	public ListUtil getListUtil() {
		return new ListUtilImpl();
	}

	@Override
	public DBUtil getDBUtil() {
		return new DBUtilImpl();
	}

	@Override
	public ORMUtil getORMUtil() {
		return new ORMUtilImpl();
	}

	@Override
	public TemplateUtil getTemplateUtil() {
		return new TemplateUtilImpl();
	}

	@Override
	public HTMLUtil getHTMLUtil() {
		return new HTMLUtilImpl();
	}

	@Override
	public ScriptEngineFactory getScriptEngineFactory(int dialect) {// FUTURE remove
		if (scriptEngine == null) scriptEngine = new ScriptEngineFactoryImpl(this, false);
		return scriptEngine;
	}

	@Override
	public ScriptEngineFactory getTagEngineFactory(int dialect) {// FUTURE remove
		if (tagEngine == null) tagEngine = new ScriptEngineFactoryImpl(this, true);
		return tagEngine;
	}

	@Override
	public PageContext createPageContext(File contextRoot, String host, String scriptName, String queryString, Cookie[] cookies, Map<String, Object> headers,
			Map<String, String> parameters, Map<String, Object> attributes, OutputStream os, long timeout, boolean register) throws PageServletException {
		// FUTURE add first 2 arguments to interface
		return PageContextUtil.getPageContext(null, null, contextRoot, host, scriptName, queryString, cookies, headers, parameters, attributes, os, register, timeout, false);
	}

	@Override
	public ConfigWeb createConfig(File contextRoot, String host, String scriptName) throws PageServletException {
		// TODO do a mored rect approach
		PageContext pc = null;
		try {
			// FUTURE add first 2 arguments to interface
			pc = PageContextUtil.getPageContext(null, null, contextRoot, host, scriptName, null, null, null, null, null, null, false, -1, false);
			return pc.getConfig();
		}
		finally {
			pc.getConfig().getFactory().releaseLuceePageContext(pc, false);
		}

	}

	@Override
	public void releasePageContext(PageContext pc, boolean unregister) {
		PageContextUtil.releasePageContext(pc, unregister);
	}

	@Override
	public lucee.runtime.util.SystemUtil getSystemUtil() {
		return new SystemUtilImpl();
	}

	@Override
	public TimeZone getThreadTimeZone() {
		return ThreadLocalPageContext.getTimeZone();
	}

	@Override
	public Instrumentation getInstrumentation() {
		return InstrumentationFactory.getInstrumentation(ThreadLocalPageContext.getConfig());
	}

	public Controler getControler() {
		return controler;
	}

	public void onStart(ConfigPro config, boolean reload) {
		boolean isWeb = config instanceof ConfigWeb;
		String context = isWeb ? "Web" : "Server";

		if (!ThreadLocalPageContext.callOnStart.get()) return;

		Resource listenerTemplateCFML = config.getConfigDir().getRealResource("context/" + context + "." + lucee.runtime.config.Constants.getCFMLComponentExtension());
		Resource listenerTemplateCFMLWebRoot = null;
		if (isWeb) {
			try {
				Resource rootdir = config.getRootDirectory();
				listenerTemplateCFMLWebRoot = rootdir.getRealResource(context + "." + lucee.runtime.config.Constants.getCFMLComponentExtension());
			}
			catch (Exception e) {
			}
		}

		// dialect
		boolean inWebRoot;
		if (listenerTemplateCFMLWebRoot != null && listenerTemplateCFMLWebRoot.isFile()) {
			inWebRoot = true;
		}
		else if (listenerTemplateCFML.isFile()) {
			inWebRoot = false;
		}
		else return;

		boolean prewarm = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.enable.warmup", ""), false);

		// call web or server startup listener
		if (!StringUtil.emptyIfNull(Thread.currentThread().getName()).startsWith("on-start-") && (!prewarm || !isWeb)) {
			long timeout = config.getRequestTimeout().getMillis();
			if (timeout <= 0) timeout = 50000L;
			OnStart thread = new OnStart(config, context, reload, inWebRoot, prewarm ? "onBuild" : null);
			thread.setName("on-start-" + CreateUniqueId.invoke());
			long start = System.currentTimeMillis();
			thread.start();
			try {
				thread.join(timeout);
			}
			catch (Exception e) {
				LogUtil.log(config, "on-start", e);
			}
			if (thread.isAlive()) {
				LogUtil.log(config, Log.LEVEL_ERROR, "on-start", "killing on-start");
				SystemUtil.stop(thread);
			}
			LogUtil.log(config, Log.LEVEL_INFO, "on-start", "on-start executed in " + (System.currentTimeMillis() - start) + "ms");
		}

		if (prewarm && (isWeb || config.getAdminMode() == ConfigImpl.ADMINMODE_SINGLE)) {
			String msg = "Lucee warmup completed. Shutting down.";
			CONSOLE_ERR.println(msg);
			LogUtil.log(config, Log.LEVEL_ERROR, "application", msg);
			shutdownFelix();
			System.exit(0);
		}

	}

	/**
	 * process Startup Listeners, i.e. Server.cfc and Web.cfc
	 */
	private class OnStart extends Thread {

		private final ConfigPro config;
		private final boolean reload;
		private final String context;
		private final boolean inWebRoot;
		private final String functionName;

		public OnStart(ConfigPro config, String context, boolean reload, boolean inWebRoot, String functionName) {
			this.config = config;
			this.context = context;
			this.reload = reload;
			this.inWebRoot = inWebRoot;
			this.functionName = StringUtil.isEmpty(functionName) ? "on" + context + "Start" : functionName;
		}

		@Override
		public void run() {
			boolean isWeb = config instanceof ConfigWeb;

			String id = CreateUniqueId.invoke();
			final String requestURI = (inWebRoot ? "" : ("/" + (isWeb ? "lucee" : "lucee-server"))) + "/" + context + "."
					+ (lucee.runtime.config.Constants.getCFMLComponentExtension());

			// PageContext oldPC = ThreadLocalPageContext.get();
			PageContext pc = null;
			try {
				String remotePersisId;
				try {
					remotePersisId = Md5.getDigestAsString(requestURI + id);
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
				String queryString = "method=" + functionName + "&reload=" + reload + "&" + ComponentPageImpl.REMOTE_PERSISTENT_ID + "=" + remotePersisId;
				if (config instanceof ConfigWeb) {
					Pair[] headers = new Pair[] { new Pair<String, Object>("AMF-Forward", "true") };
					Struct attrs = new StructImpl();
					attrs.setEL(KeyConstants._client, "lucee-listener-1-0");

					pc = ThreadUtil.createPageContext((ConfigWeb) config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", requestURI, queryString,
							SerializableCookie.COOKIES0, headers, null, new Pair[0], attrs, true, Long.MAX_VALUE);
				}
				else {
					Map<String, Object> headers = new HashMap<String, Object>();
					headers.put("AMF-Forward", "true");
					Map<String, Object> attrs = new HashMap<String, Object>();
					attrs.put("client", "lucee-listener-1-0");

					File root = new File(config.getRootDirectory().getAbsolutePath());
					CreationImpl cr = (CreationImpl) CreationImpl.getInstance(engine);
					ServletConfig sc = cr.createServletConfig(root, null, null);
					pc = PageContextUtil.getPageContext(config, sc, root, "localhost", requestURI, queryString, SerializableCookie.COOKIES0, headers, null, attrs,
							DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, true, Long.MAX_VALUE,
							Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.ignore.scopes", null), false));
				}
				((PageContextImpl) pc).setListenerContext(true);
				pc.executeCFML(requestURI, true, false);
				((PageContextImpl) pc).setListenerContext(false);

			}
			catch (Exception e) {
				// we simply ignore exceptions, if the template itself throws an error it will be handled by the
				// error listener
			}
			finally {
				CFMLFactory f = pc.getConfig().getFactory();
				f.releaseLuceePageContext(pc, true);
				// ThreadLocalPageContext.register(oldPC);
			}
		}
	}

	/*
	 * execute request coming from the servlet engine in a separate thread or not
	 */
	public boolean exeRequestAsync() {
		if (asyncReqHandle == null) asyncReqHandle = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.async.request.handle", null), Boolean.FALSE);
		return asyncReqHandle;
	}

	public static CFMLEngineImpl toCFMLEngineImpl(CFMLEngine e) throws CasterException {
		if (e instanceof CFMLEngineImpl) return (CFMLEngineImpl) e;
		if (e instanceof CFMLEngineWrapper) return toCFMLEngineImpl(((CFMLEngineWrapper) e).getEngine());
		throw new CasterException(e, CFMLEngineImpl.class);
	}

	public static CFMLEngineImpl toCFMLEngineImpl(CFMLEngine e, CFMLEngineImpl defaultValue) {
		if (e instanceof CFMLEngineImpl) return (CFMLEngineImpl) e;
		if (e instanceof CFMLEngineWrapper) return toCFMLEngineImpl(((CFMLEngineWrapper) e).getEngine(), defaultValue);
		return defaultValue;
	}

	public Object getEnvExt() {
		return envExt;
	}

	public void setEnvExt(String envExt) {
		this.envExt = envExt;
	}

	public static boolean quick(CFMLEngine engine) {
		while (engine instanceof CFMLEngineWrapper) {
			engine = ((CFMLEngineWrapper) engine).getEngine();
		}
		if (engine instanceof CFMLEngineImpl) return ((CFMLEngineImpl) engine).quick;
		return false;
	}
}