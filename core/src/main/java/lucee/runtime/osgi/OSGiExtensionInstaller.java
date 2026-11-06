package lucee.runtime.osgi;

import java.util.List;
import java.util.Map;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.loader.util.VersionImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.config.maven.ExtensionProvider;
import lucee.runtime.config.maven.Version;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionMetadata;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionCollection;
import lucee.runtime.op.Caster;

/**
 * Makes sure the OSGi extension (org.lucee:osgi-extension) is installed when something needs OSGi
 * and no OSGi implementation is available (LDEV-6829).
 * 
 * The extension is resolved like the other extensions of the core, offline first: an installed
 * version, then a version in the extension "available" folder (that is where bundled .lex files
 * land), and only then Maven. It always is installed in server scope.
 */
public final class OSGiExtensionInstaller {

	public static final String SETTING_AUTO_INSTALL = "lucee.osgi.autoinstall";

	public static final String GROUP_ID = "org.lucee";
	public static final String ARTIFACT_ID = "osgi-extension";
	public static final String COORDINATE = GROUP_ID + ":" + ARTIFACT_ID;

	private static final String[] BUNDLE_NAME_KEYS = new String[] { "bundleName", "bundle-name", "bundle_name" };

	// the extension itself must never trigger its own installation
	private static final ThreadLocal<Boolean> installing = new ThreadLocal<Boolean>();
	// test seam, see setAvailabilityOverride
	private static final ThreadLocal<Boolean> availabilityOverride = new ThreadLocal<Boolean>();

	// only one thread installs the extension, the others wait for it
	private static final Object LOCK = new Object();

	private OSGiExtensionInstaller() {}

	/**
	 * @return false when the installation of the OSGi extension was disabled with the setting
	 *         lucee.osgi.autoinstall / LUCEE_OSGI_AUTOINSTALL
	 */
	public static boolean isAutoInstallEnabled() {
		// not cached on purpose, we only get here when OSGi is missing and that is a rare path
		return Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar(SETTING_AUTO_INSTALL, null), true);
	}

	public static boolean isOSGiAvailable() {
		Boolean override = availabilityOverride.get();
		if (override != null) return override.booleanValue();
		return OSGiServiceImpl.getInstance().isAvailable();
	}

	/**
	 * Pretends, for the current thread only, that OSGi is available (true) or not available (false),
	 * used to test the installation without a server that lacks OSGi. Pass null to remove the override.
	 */
	public static void setAvailabilityOverride(Boolean available) {
		if (available == null) availabilityOverride.remove();
		else availabilityOverride.set(available);
	}

	/**
	 * Makes sure OSGi is available, installs the OSGi extension when it is not.
	 * 
	 * @param config any config, the extension is installed in the server scope
	 * @param reason what needs OSGi, used in the error message
	 * @return true if the OSGi extension was installed by this call
	 * @throws ApplicationException when OSGi is not available and the OSGi extension could not (or
	 *             must not) be installed
	 */
	public static boolean ensureAvailable(Config config, String reason) throws ApplicationException {
		if (isOSGiAvailable()) return false;
		// we are already installing the extension in this thread
		if (installing.get() != null) return false;
		return install(config, reason);
	}

	/**
	 * Installs the OSGi extension in server scope if it is not installed yet. Nothing gets installed if
	 * the extension cannot be resolved.
	 * 
	 * @param config any config, the extension is installed in the server scope
	 * @param reason what needs OSGi, used in the error message
	 * @return true if the OSGi extension was installed by this call
	 * @throws ApplicationException when the extension could not (or must not) be installed
	 */
	public static boolean install(Config config, String reason) throws ApplicationException {
		if (!isAutoInstallEnabled()) {
			throw new ApplicationException(message(reason, "the automatic installation is disabled by the setting [" + SETTING_AUTO_INSTALL + "]"));
		}
		synchronized (LOCK) {
			return _install(config, reason);
		}
	}

	private static boolean _install(Config config, String reason) throws ApplicationException {
		installing.set(Boolean.TRUE);
		try {
			ConfigServerImpl cs = ConfigUtil.getConfigServerImpl(config);
			Log log = cs.getLog("deploy");

			if (findInstalled(cs) != null) return false;

			// resolve first, so a failure installs nothing
			RHExtension rhe;
			try {
				rhe = resolve(cs, log);
			}
			catch (Exception e) {
				ApplicationException ae = new ApplicationException(message(reason, e.getMessage()));
				ae.initCause(e);
				throw ae;
			}
			if (log != null) log.info("osgi", "install the OSGi extension [" + rhe.getArtifactId() + " : " + rhe.getVersion() + "] because [" + reason + "] needs OSGi");
			try {
				DeployHandler.deployExtension(cs, rhe, false, false, log);
			}
			catch (PageException e) {
				ApplicationException ae = new ApplicationException(message(reason, "failed to install the extension: " + e.getMessage()));
				ae.initCause(e);
				throw ae;
			}
			return true;
		}
		finally {
			installing.remove();
		}
	}

	/**
	 * Resolves the OSGi extension, offline first.
	 */
	static RHExtension resolve(ConfigServerImpl cs, Log log) throws Exception {
		// bundled .lex (or any other .lex) in the extension folders
		RHExtension local = findLocal(cs);
		if (local != null) return local;

		// Maven
		ExtensionProvider ep = ExtensionProvider.getInstance(cs, GROUP_ID);
		Version version = ep.last(ARTIFACT_ID);
		if (version == null) throw new ApplicationException("could not find [" + COORDINATE + "] in the Maven repositories");
		Resource lex = ep.getLEXResource(cs, ARTIFACT_ID, version);
		return RHExtension.getInstance(cs, lex, log);
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
		Version v;
		for (RHExtensionCollection.Entry e: RHExtension.getExtensions(cs)) {
			RHExtension rhe = e.getRHExtension();
			if (!isOSGiExtension(rhe)) continue;
			v = VersionImpl.parseVersion(rhe.getVersion(), null);
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

		BundleInfo[] bundles = md.getBundles(null);
		if (bundles != null && bundles.length > 0) return true;

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
	 * Makes sure OSGi is available before the given extension is installed.
	 */
	public static void ensureAvailableFor(Config config, RHExtension rhe) throws ApplicationException {
		if (isOSGiAvailable() || !needsOSGi(rhe)) return;
		ensureAvailable(config, "the extension [" + rhe.getMetadata().getName() + " : " + rhe.getVersion() + "]");
	}

	public static String message(String reason, String detail) {
		return "OSGi is needed for " + reason + " but is not available" + (StringUtil.isEmpty(detail, true) ? "" : " (" + detail + ")")
				+ ". Please install the OSGi extension (" + COORDINATE + ").";
	}
}
