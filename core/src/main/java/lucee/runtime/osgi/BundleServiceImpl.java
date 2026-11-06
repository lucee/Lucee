package lucee.runtime.osgi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Properties;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleWiring;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngineFactorySupport;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.Identification;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil.PackageQuery;
import lucee.runtime.osgi.OSGiUtil.VersionDefinition;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * The BundleService based on the OSGi framework (Felix) that is part of the core.
 */
public final class BundleServiceImpl implements BundleService {

	private static final BundleServiceImpl INSTANCE = new BundleServiceImpl();

	public static BundleServiceImpl getInstance() {
		return INSTANCE;
	}

	private volatile BundleCollection bundleCollection;
	private volatile CFMLEngineFactory bootFactory;

	/**
	 * Starts the OSGi framework when Lucee is loaded directly (no bundle collection handed over by the
	 * loader) and keeps the bundle collection.
	 */
	public synchronized void boot(CFMLEngineFactory factory, BundleCollection bc) throws Exception {
		this.bootFactory = factory;
		if (bc == null) {
			Properties prop = InfoImpl.getDefaultProperties();

			// read the config from default.properties
			Map<String, Object> config = new HashMap<String, Object>();
			for (Map.Entry<Object, Object> e: prop.entrySet()) {
				String k = (String) e.getKey();
				if (!k.startsWith("org.") && !k.startsWith("felix.")) continue;

				String val = CFMLEngineFactorySupport.removeQuotes((String) e.getValue(), true);
				String addional = Caster.toString(SystemUtil.getSystemPropOrEnvVar(k, null), null);

				if (!StringUtil.isEmpty(addional, true)) {
					if ("org.osgi.framework.bootdelegation".equals(k) || "org.osgi.framework.system.packages".equals(k)) {
						val = addional.trim() + "," + val;
					}
					else {
						val = addional.trim();
					}
				}
				config.put(k, val);
			}

			config.put(Constants.FRAMEWORK_BOOTDELEGATION, "lucee.*");
			Framework felix = factory.getFelix(factory.getResourceRoot(), config);
			bc = new BundleCollection(felix, felix, null);
		}
		this.bundleCollection = bc;
	}

	public BundleCollection getBundleCollection() {
		return bundleCollection;
	}

	public lucee.runtime.config.maven.Version getCoreVersion() {
		return OSGiUtil.toLucee(bundleCollection.getCoreVersion());
	}

	public Dictionary<String, String> getCoreHeaders() {
		return bundleCollection.getCoreHeaders();
	}

	/**
	 * removes older or newer lucee-core bundles to avoid conflicts
	 */
	public List<String> uninstallStaleCoreBundles(String keepVersion) {
		return OSGiUtil.uninstallOtherVersions(bundleCollection.getBundleContext(), "lucee.core", keepVersion);
	}

	public void shutdown() throws BundleException {
		CFMLEngineFactory factory = bootFactory;
		if (factory != null) factory.shutdownFelix();
	}

	private BundleServiceImpl() {
	}

	@Override
	public boolean isAvailable() {
		try {
			return OSGiUtil.getBundleContext() != null;
		}
		catch (Exception e) {
			return false;
		}
	}

	@Override
	public Class<?> loadClass(String bundleName, String bundleVersion, String className) throws IOException, ClassNotFoundException {
		Bundle bundle = load(bundleName, bundleVersion);
		return bundle.loadClass(className);
	}

	@Override
	public Map<String, Object> install(File bundle, boolean start) throws IOException {
		try {
			Resource res = ResourceUtil.toResource(bundle);
			Bundle b = OSGiUtil.installBundle(context(), res, true);
			if (start) b = OSGiUtil.startIfNecessary(b);
			return toInfo(b);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	@Override
	public void start(String bundleName, String bundleVersion) throws IOException {
		Bundle b = get(bundleName, bundleVersion);
		if (b == null) throw new IOException("there is no bundle [" + bundleName + ":" + bundleVersion + "]");
		try {
			OSGiUtil.startIfNecessary(b);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	@Override
	public void stop(String bundleName, String bundleVersion) throws IOException {
		Bundle b = get(bundleName, bundleVersion);
		if (b == null) return;
		try {
			OSGiUtil.stopIfNecessary(b);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	@Override
	public void uninstall(String bundleName, String bundleVersion) throws IOException {
		Bundle b = get(bundleName, bundleVersion);
		if (b == null) return;
		try {
			OSGiUtil.stopIfNecessary(b);
			OSGiUtil.uninstall(b);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	@Override
	public Map<String, Object> getBundleInfo(String bundleName, String bundleVersion) {
		Bundle b = get(bundleName, bundleVersion);
		return b == null ? null : toInfo(b);
	}

	@Override
	public Map<String, Object> getBundleInfo(Class<?> clazz) {
		Bundle b = OSGiUtil.getBundleFromClass(clazz, null);
		return b == null ? null : toInfo(b);
	}

	@Override
	public Map<String, Object> getBundleInfo(ClassLoader classLoader) {
		if (classLoader instanceof BundleReference) {
			Bundle b = ((BundleReference) classLoader).getBundle();
			Map<String, Object> info = new StructImpl(Struct.TYPE_LINKED);
			info.put("name", b.getSymbolicName());
			info.put("version", OSGiUtil.toLucee(b.getVersion()).toString());
			info.put("id", Long.valueOf(b.getBundleId()));
			info.put("state", OSGiUtil.toState(b.getState(), "unknown"));
			return info;
		}
		return null;
	}

	@Override
	public String getBundleState(Class<?> clazz) {
		Bundle b = OSGiUtil.getBundleFromClass(clazz, null);
		return b == null ? null : OSGiUtil.toState(b.getState(), "unknown");
	}

	@Override
	public Map<String, Object> getRequirements(String bundleName, String bundleVersion) throws IOException {
		Bundle b = get(bundleName, bundleVersion);
		if (b == null) return null;
		try {
			Pair<List<BundleRange>, List<PackageQuery>> pair = OSGiUtil.getRequiredBundlesAndPackages(b);
			Map<String, Object> result = new StructImpl(Struct.TYPE_LINKED);
			result.put("requiredBundles", BundleRange.toArray(pair.getName()));
			result.put("requiredPackages", toPackageArray(pair.getValue()));
			return result;
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	private static Array toPackageArray(List<PackageQuery> list) {
		Array arr = new ArrayImpl();
		for (PackageQuery pd: list) {
			Struct sct = new StructImpl();
			Array versions = new ArrayImpl();
			sct.setEL(KeyConstants._package, pd.getName());
			sct.setEL("versions", versions);
			for (VersionDefinition vd: pd.getVersionDefinitons()) {
				Struct v = new StructImpl();
				v.setEL(KeyConstants._bundleVersion, vd.getVersion().toString());
				v.setEL("operator", vd.getOpAsString());
				versions.appendEL(v);
			}
			arr.appendEL(sct);
		}
		return arr;
	}

	@Override
	public List<Map<String, Object>> listCoreBundles() {
		List<Map<String, Object>> list = new ArrayList<>();
		BundleCollection bc = OSGiUtil.getBundleCollection();
		list.add(toInfo(bc.core));
		Iterator<Bundle> it = bc.getSlaves();
		while (it.hasNext()) {
			list.add(toInfo(it.next()));
		}
		return list;
	}

	@Override
	public List<Map<String, Object>> listBundles() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Bundle b: context().getBundles()) {
			list.add(toInfo(b));
		}
		return list;
	}

	@Override
	public URL getResource(String bundleName, String bundleVersion, String path) {
		Bundle b = get(bundleName, bundleVersion);
		return b == null ? null : b.getResource(path);
	}

	@Override
	public ClassLoader getClassLoader(String bundleName, String bundleVersion) throws IOException {
		Bundle b = get(bundleName, bundleVersion);
		if (b == null) throw new IOException("there is no bundle [" + bundleName + ":" + bundleVersion + "]");
		BundleWiring wiring = b.adapt(BundleWiring.class);
		if (wiring == null) throw new IOException("the bundle [" + bundleName + ":" + bundleVersion + "] is not resolved");
		return wiring.getClassLoader();
	}

	private static BundleContext context() {
		return OSGiUtil.getBundleContext();
	}

	private static Bundle get(String name, String version) {
		return OSGiUtil.getBundleLoaded(context(), name, toVersion(version), null);
	}

	private static Bundle load(String name, String version) throws IOException {
		try {
			CFMLEngine engine = ConfigUtil.getCFMLEngine();
			return OSGiUtil.loadBundle(name, toVersion(version), engine.getIdentification(), null, true);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	private static Version toVersion(String version) {
		if (StringUtil.isEmpty(version, true)) return null;
		return OSGiUtil.toVersion(version.trim(), null);
	}

	private static Map<String, Object> toInfo(Bundle b) {
		Map<String, Object> info = new StructImpl(Struct.TYPE_LINKED);
		info.put("name", b.getSymbolicName());
		info.put("version", OSGiUtil.toLucee(b.getVersion()).toString());
		info.put("id", Long.valueOf(b.getBundleId()));
		info.put("state", OSGiUtil.toState(b.getState(), "unknown"));
		try {
			info.put("location", b.getLocation());
		}
		catch (IllegalStateException ise) {
			info.put("location", null);
		}
		info.put("fragment", Boolean.valueOf(OSGiUtil.isFragment(b)));
		try {
			info.put("headers", OSGiUtil.getHeaders(b));
		}
		catch (IllegalStateException ise) {
			info.put("headers", new StructImpl(Struct.TYPE_LINKED));
		}
		return info;
	}

	@Override
	public Map<String, Object> loadBundle(String bundleName, String bundleVersion) throws IOException {
		return toInfo(load(bundleName, bundleVersion));
	}

	@Override
	public Map<String, Object> loadFromLocal(String bundleName, String bundleVersion, boolean loadIfNecessary) {
		Bundle b = OSGiUtil.loadBundleFromLocal(context(), bundleName, toVersion(bundleVersion), null, loadIfNecessary, null);
		return b == null ? null : toInfo(b);
	}

	@Override
	public void removeLocal(String bundleName, String bundleVersion, boolean removeFile) throws IOException {
		try {
			OSGiUtil.removeLocalBundle(bundleName, toVersion(bundleVersion), null, removeFile, false);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	@Override
	public ClassLoader getLoaderClassLoader() throws IOException {
		try {
			return OSGiUtil.getEmptyBundleClassLoader(null);
		}
		catch (BundleException be) {
			throw new IOException(be);
		}
	}

	@Override
	public String getClassPath() {
		return OSGiUtil.getClassPath();
	}

	@Override
	public Class<?> findClass(String className) {
		return OSGiUtil.loadClass(className, null);
	}

	@Override
	public Map<String, Object> getLocalBundleInfo(String bundleName, String bundleVersion) {
		BundleFile bf = OSGiUtil.getBundleFile(bundleName, toVersion(bundleVersion), null, null, false, null);
		return bf == null ? null : toInfo(bf);
	}

	@Override
	public List<Map<String, Object>> listLocalBundles() {
		List<Map<String, Object>> list = new ArrayList<>();
		Set<String> installed = new HashSet<>();
		for (Bundle b: context().getBundles()) {
			installed.add(b.getSymbolicName() + ":" + b.getVersion());
		}
		try {
			File[] children = ConfigUtil.getCFMLEngineFactory().getBundleDirectory().listFiles((dir, name) -> name.endsWith(".jar"));
			if (children == null) return list;
			for (File child: children) {
				try {
					BundleFile bf = BundleFile.getInstance(child);
					if (bf.isBundle() && !installed.contains(bf.getSymbolicName() + ":" + bf.getVersion())) list.add(toInfo(bf));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
		}
		catch (IOException ioe) {}
		return list;
	}

	private static Map<String, Object> toInfo(BundleFile bf) {
		Map<String, Object> info = new StructImpl(Struct.TYPE_LINKED);
		info.put("name", bf.getSymbolicName());
		info.put("version", OSGiUtil.toLucee(bf.getVersion()).toString());
		info.put("state", "notinstalled");
		info.put("location", bf.getFile().getAbsolutePath());
		info.put("fragment", Boolean.valueOf(OSGiUtil.isFragment(bf)));
		info.put("headers", bf.getHeaders());
		return info;
	}

	/**
	 * load (download if necessary) and start the bundle
	 */
	public void loadBundle(BundleDefinition bd, Identification id, List<Resource> addional, boolean versionOnlyMattersForDownload) throws BundleException {
		OSGiUtil.loadBundle(null, bd.getName(), OSGiUtil.toOSGi(bd.getVersion()), id, addional, true, versionOnlyMattersForDownload, true, null);
	}

	public Class<?> loadClass(BundleDefinition bd, Identification id, List<Resource> addional, boolean versionOnlyMattersForDownload, String className)
			throws BundleException, ClassNotFoundException {
		return OSGiUtil.loadBundle(null, bd.getName(), OSGiUtil.toOSGi(bd.getVersion()), id, addional, true, versionOnlyMattersForDownload, true, null).loadClass(className);
	}

	/**
	 * try to fix a class that could not be loaded because a bundle is missing
	 *
	 * @return true when something was fixed and it makes sense to try again
	 */
	public boolean resolveLoadingIssues(Config config, ClassNotFoundException cnfe) {
		return OSGiUtil.resolveBundleLoadingIssues(null, config, cnfe);
	}
}
