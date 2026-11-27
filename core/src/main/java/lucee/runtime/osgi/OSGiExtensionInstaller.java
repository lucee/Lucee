package lucee.runtime.osgi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.loader.util.VersionImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.config.maven.Version;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.ExtensionMetadata;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionCollection;
import lucee.runtime.op.Caster;

/**
 * LDEV-7012 makes sure the OSGi extension (org.lucee:osgi-extension) is installed when something needs
 * OSGi and no OSGi implementation is available, for example an old extension that ships bundles, or a
 * class definition with a bundle name, on a server without the OSGi extension (lucee-light, config
 * only extensions, the extension was removed).
 *
 * The version is not "the latest": it is resolved offline first (installed, extension folders), then
 * the version the core itself requires (header Require-Extension) is used, a newer local copy wins.
 * The extension is always installed in server scope. It is only tried when OSGi is needed and not
 * available, one thread installs, the others wait, and a failure is remembered for a while, so the
 * network is not hit on every request.
 */
public final class OSGiExtensionInstaller {

	public static final String SETTING_AUTO_INSTALL = "lucee.osgi.autoinstall";
	public static final String ENV_AUTO_INSTALL = "LUCEE_OSGI_AUTOINSTALL";

	public static final String GROUP_ID = "org.lucee";
	public static final String ARTIFACT_ID = "osgi-extension";
	public static final String COORDINATE = GROUP_ID + ":" + ARTIFACT_ID;

	// after a failure the installation is not tried again for this time (ms)
	private static final long RETRY_AFTER = 10 * 60 * 1000;

	private static final boolean AUTO_INSTALL = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar(SETTING_AUTO_INSTALL, null), true);

	private static final String[] BUNDLE_NAME_KEYS = new String[] { "bundleName", "bundle-name", "bundle_name" };

	// the installation of the extension must never trigger itself
	private static final ThreadLocal<Boolean> installing = new ThreadLocal<Boolean>();

	// a ReentrantLock, not synchronized: this is also called from virtual threads (LDEV-6812)
	// set in the tasks that start the bundles of the extensions at startup
	private static final ThreadLocal<Boolean> startingBundles = new ThreadLocal<Boolean>();

	private static final ReentrantLock LOCK = new ReentrantLock();
	private static volatile String failure;
	private static volatile long failedAt;

	private OSGiExtensionInstaller() {
	}

	public static void setStartingBundles(boolean starting) {
		if (starting) startingBundles.set(Boolean.TRUE);
		else startingBundles.remove();
	}

	public static boolean isAutoInstallEnabled() {
		return AUTO_INSTALL;
	}

	/**
	 * Makes sure OSGi is available, installs the OSGi extension when it is not.
	 *
	 * @param reason what needs OSGi, used in the log and in the error message
	 * @return the OSGiService, if the OSGi extension was installed (or found) by this call, null if
	 *         there was nothing to do or it is not possible to do it here (no config yet, we are
	 *         installing the extension already)
	 * @throws OSGiNotAvailableException when OSGi is not available and the OSGi extension could not (or
	 *             must not) be installed
	 */
	public static OSGiService ensureAvailable(String reason) throws OSGiNotAvailableException {
		if (OSGi.isAvailable()) return OSGi.get();
		if (installing.get() != null) return null;
		Config config = ThreadLocalPageContext.getConfig();
		if (config == null) return null;

		if (!AUTO_INSTALL) {
			throw new OSGiNotAvailableException(message(reason, "the automatic installation is disabled by the system property [" + SETTING_AUTO_INSTALL
					+ "] (environment variable [" + ENV_AUTO_INSTALL + "]), set it to true to allow it"));
		}
		String failed = failure;
		if (failed != null && System.currentTimeMillis() - failedAt < RETRY_AFTER) throw new OSGiNotAvailableException(message(reason, failed));

		// the thread that loads the extensions waits for the tasks that start the bundles, they cannot wait for it
		if (startingBundles.get() != null) return null;

		LOCK.lock();
		try {
			// another thread did it while we were waiting
			if (OSGi.isAvailable()) return OSGi.get();
			failed = failure;
			if (failed != null && System.currentTimeMillis() - failedAt < RETRY_AFTER) throw new OSGiNotAvailableException(message(reason, failed));

			installing.set(Boolean.TRUE);
			try {
				install(config, reason);
				OSGi.reset();
				if (!OSGi.isAvailable()) throw new OSGiNotAvailableException(message(reason, "the OSGi extension was installed, but it provides no OSGi implementation"));
				failure = null;
				return OSGi.get();
			}
			catch (OSGiNotAvailableException e) {
				remember(e.getMessage());
				throw e;
			}
			catch (Exception e) {
				String detail = e.getMessage() == null ? e.getClass().getName() : e.getMessage();
				remember(detail);
				OSGiNotAvailableException ex = new OSGiNotAvailableException(message(reason, detail));
				ex.initCause(e);
				throw ex;
			}
			finally {
				installing.remove();
			}
		}
		finally {
			LOCK.unlock();
		}
	}

	private static void remember(String message) {
		failure = message;
		failedAt = System.currentTimeMillis();
	}

	private static void install(Config config, String reason) throws Exception {
		ConfigServerImpl cs = ConfigUtil.getConfigServerImpl(config);
		Log log = cs.getLog("deploy");

		// installed, but not loaded yet, nothing to install
		if (findInstalled(cs) != null) return;

		// resolve first, so a failure installs nothing
		RHExtension rhe = resolve(cs);
		if (log != null) {
			log.info("osgi", "install the OSGi extension [" + rhe.getArtifactId() + " : " + rhe.getVersion() + "] automatically, because [" + reason
					+ "] needs OSGi and OSGi is not installed (set the system property [" + SETTING_AUTO_INSTALL + "] to false to disable this)");
		}
		DeployHandler.deployExtension(cs, rhe, false, false, log);
	}

	/**
	 * The version the core requires (header Require-Extension), or a newer one that is already available
	 * locally (extension folders). Never the latest from a repository.
	 */
	static RHExtension resolve(ConfigServerImpl cs) throws Exception {
		ExtensionDefintion pinned = findRequired(cs);
		RHExtension local = findLocal(cs);

		if (pinned == null) {
			if (local != null) return local;
			throw new ApplicationException("the core does not define a version of [" + COORDINATE + "] (header Require-Extension) and no copy of it is available locally");
		}
		if (local != null) {
			Version localVersion = VersionImpl.parseVersion(local.getVersion(), null);
			Version pinnedVersion = StringUtil.isEmpty(pinned.getVersion(), true) ? null : VersionImpl.parseVersion(pinned.getVersion(), null);
			if (pinnedVersion == null || (localVersion != null && localVersion.compareTo(pinnedVersion) >= 0)) return local;
		}
		return pinned.toRHExtension(cs);
	}

	private static ExtensionDefintion findRequired(ConfigServerImpl cs) {
		List<ExtensionDefintion> required = ((InfoImpl) ConfigUtil.getEngine(cs).getInfo()).getRequiredExtension();
		if (required == null) return null;
		for (ExtensionDefintion ed: required) {
			if (ed.getGroupId() != null && ed.getArtifactId() != null && GROUP_ID.equalsIgnoreCase(ed.getGroupId()) && ARTIFACT_ID.equalsIgnoreCase(ed.getArtifactId())) return ed;
			if (ed.getCoordinate(cs, false) != null && GROUP_ID.equalsIgnoreCase(ed.getCoordinate(cs, false).groupId)
					&& ARTIFACT_ID.equalsIgnoreCase(ed.getCoordinate(cs, false).artifactId)) return ed;
		}
		return null;
	}

	private static RHExtension findInstalled(ConfigPro cs) {
		for (RHExtension rhe: cs.getRHExtensions()) {
			if (isOSGiExtension(rhe)) return rhe;
		}
		return null;
	}

	private static RHExtension findLocal(ConfigPro cs) {
		RHExtension best = null;
		Version bestVersion = null;
		for (RHExtensionCollection.Entry e: RHExtension.getExtensions(cs)) {
			RHExtension rhe = e.getRHExtension();
			if (!isOSGiExtension(rhe)) continue;
			Version v = VersionImpl.parseVersion(rhe.getVersion(), null);
			if (best == null || (v != null && (bestVersion == null || v.compareTo(bestVersion) > 0))) {
				best = rhe;
				bestVersion = v;
			}
		}
		return best;
	}

	/**
	 * @return true if the extension is the OSGi extension (org.lucee:osgi-extension)
	 */
	public static boolean isOSGiExtension(RHExtension rhe) {
		return rhe != null && GROUP_ID.equals(rhe.getGroupId()) && ARTIFACT_ID.equals(rhe.getArtifactId());
	}

	/**
	 * @return true if the extension needs OSGi: it ships bundles or archives, or defines a class (cache,
	 *         jdbc, search, ...) that is loaded from a bundle.
	 */
	public static boolean needsOSGi(RHExtension rhe) {
		if (rhe == null || isOSGiExtension(rhe)) return false;
		ExtensionMetadata md = rhe.getMetadata();

		if (md.getBundles(null) != null && md.getBundles(null).length > 0) return true;

		String[] archives = md.getArchives();
		if (archives != null && archives.length > 0) return true;

		return hasBundleClass(md.getMappings()) || hasBundleClass(md.getCaches()) || hasBundleClass(md.getCacheHandlers()) || hasBundleClass(md.getJdbcs())
				|| hasBundleClass(md.getSearchs()) || hasBundleClass(md.getOrms()) || hasBundleClass(md.getResources()) || hasBundleClass(md.getMonitors())
				|| hasBundleClass(md.getWebservices()) || hasBundleClass(md.getAMFs()) || hasBundleClass(md.getLogEngines()) || hasBundleClass(md.getHttpEngines())
				|| hasBundleClass(md.getRegexEngines()) || hasBundleClass(md.getStartupHooks()) || hasBundleClass(md.getMimeTypes());
	}

	private static boolean hasBundleClass(List<Map<String, String>> settings) {
		if (settings == null) return false;
		for (Map<String, String> setting: settings) {
			if (setting == null) continue;
			for (String key: BUNDLE_NAME_KEYS) {
				if (!StringUtil.isEmpty(setting.get(key), true)) return true;
			}
		}
		return false;
	}

	/**
	 * Makes sure OSGi is available before the given extension is installed, when the extension needs it.
	 */
	public static void ensureAvailableFor(RHExtension rhe) throws OSGiNotAvailableException {
		if (OSGi.isAvailable() || !needsOSGi(rhe)) return;
		ensureAvailable("the extension [" + rhe.getMetadata().getName() + " : " + rhe.getVersion() + "]");
	}

	public static String message(String reason, String detail) {
		return "OSGi is needed for " + reason + " but is not available" + (StringUtil.isEmpty(detail, true) ? "" : " (" + detail + ")") + ". Please install the OSGi extension ("
				+ COORDINATE + ").";
	}
}
