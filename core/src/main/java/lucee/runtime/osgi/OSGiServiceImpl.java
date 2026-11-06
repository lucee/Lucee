package lucee.runtime.osgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
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
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.ConfigServer;
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
 * The OSGiService based on the OSGi framework (Felix) that is part of the core.
 */
public final class OSGiServiceImpl implements OSGiService {

	private static final OSGiServiceImpl INSTANCE = new OSGiServiceImpl();

	public static OSGiServiceImpl getInstance() {
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
	 * @return the file the running core was loaded from, or null when core is not loaded as a bundle
	 */
	public File getCoreFile() {
		try {
			for (Bundle b: context().getBundles()) {
				if ("lucee.core".equals(b.getSymbolicName()) && b.getState() != Bundle.UNINSTALLED) return new File(b.getLocation());
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return null;
	}

	/**
	 * removes older or newer lucee-core bundles to avoid conflicts
	 */
	public List<String> uninstallStaleCoreBundles(String keepVersion) {
		return OSGiUtil.uninstallOtherVersions(bundleCollection.getBundleContext(), "lucee.core", keepVersion);
	}

	@Override
	public void shutdown() {
		CFMLEngineFactory factory = bootFactory;
		if (factory == null) return;
		try {
			factory.shutdownFelix();
		}
		catch (BundleException be) {
			LogUtil.log(Log.LEVEL_ERROR, "osgi", be);
		}
	}

	private OSGiServiceImpl() {
	}

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


	private void start(String bundleName, String bundleVersion) throws IOException {
		Bundle b = get(bundleName, bundleVersion);
		if (b == null) throw new IOException("there is no bundle [" + bundleName + ":" + bundleVersion + "]");
		try {
			OSGiUtil.startIfNecessary(b);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	private void stop(String bundleName, String bundleVersion) throws IOException {
		Bundle b = get(bundleName, bundleVersion);
		if (b == null) return;
		try {
			OSGiUtil.stopIfNecessary(b);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	private void uninstall(String bundleName, String bundleVersion) throws IOException {
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

	private Map<String, Object> getBundleInfo(String bundleName, String bundleVersion) {
		Bundle b = get(bundleName, bundleVersion);
		return b == null ? null : toInfo(b);
	}

	private Map<String, Object> getBundleInfo(Class<?> clazz) {
		Bundle b = OSGiUtil.getBundleFromClass(clazz, null);
		return b == null ? null : toInfo(b);
	}

	private Map<String, Object> getBundleInfo(ClassLoader classLoader) {
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

	private String getBundleState(Class<?> clazz) {
		Bundle b = OSGiUtil.getBundleFromClass(clazz, null);
		return b == null ? null : OSGiUtil.toState(b.getState(), "unknown");
	}

	private Map<String, Object> getRequirements(String bundleName, String bundleVersion) throws IOException {
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


	@Override
	public Class<?> findClass(String className, String... bundleNames) {
		if (bundleNames == null || bundleNames.length == 0) return findClass(className);
		for (Bundle b: context().getBundles()) {
			for (String name: bundleNames) {
				if (name == null || !name.trim().equals(b.getSymbolicName())) continue;
				try {
					return b.loadClass(className);
				}
				catch (ClassNotFoundException e) {
				}
			}
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String bundleName, String bundleVersion, String path) throws IOException {
		URL url = getResource(bundleName, bundleVersion, path);
		return url == null ? null : url.openStream();
	}

	@Override
	public boolean install(File jar) throws IOException {
		BundleFile bf = BundleFile.getInstance(jar);
		if (!bf.isBundle()) return false;

		// copy to the bundle directory, when not already there
		BundleFile existing = getBundleFile(bf.getSymbolicName(), bf.getBundleVersion());
		File target;
		if (existing != null) target = existing.getFile();
		else {
			CFMLEngineFactory factory = CFMLEngineFactory.getInstance().getCFMLEngineFactory();
			target = new File(factory.getBundleDirectory(), bf.getSymbolicName() + "-" + bf.getBundleVersion().toString() + ".jar");
			InputStream is = bf.getInputStream();
			OutputStream os = new FileOutputStream(target);
			try {
				IOUtil.copy(is, os, false, false);
			}
			finally {
				IOUtil.close(is, os);
			}
		}
		try {
			OSGiUtil.installBundle(context(), ResourceUtil.toResource(target), true);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
		return true;
	}

	@Override
	public void uninstall(File jar) throws IOException {
		BundleFile bf = BundleFile.getInstance(jar);
		if (!bf.isBundle()) return;
		uninstall(bf.getSymbolicName(), bf.getVersionAsString());
		removeLocal(bf.getSymbolicName(), bf.getVersionAsString(), true);
	}

	@Override
	public String getId(Class<?> clazz) {
		if (clazz == null) return null;
		return toId(OSGiUtil.getBundleFromClass(clazz, null));
	}

	@Override
	public String getId(ClassLoader classLoader) {
		return classLoader instanceof BundleReference ? toId(((BundleReference) classLoader).getBundle()) : null;
	}

	private static String toId(Bundle b) {
		// the system bundle and the core bundle are Lucee itself
		if (b == null || b.getBundleId() == 0 || b.getState() == Bundle.UNINSTALLED || "lucee.core".equals(b.getSymbolicName())) return null;
		return b.getSymbolicName() + ":" + OSGiUtil.toLucee(b.getVersion());
	}

	@Override
	public Map<String, Object> getInfo(Class<?> clazz) {
		if (clazz == null) return null;
		Bundle b = OSGiUtil.getBundleFromClass(clazz, null);
		if (b == null) return null;
		Map<String, Object> info = toInfo(b);
		try {
			Map<String, Object> req = getRequirements(b.getSymbolicName(), OSGiUtil.toLucee(b.getVersion()).toString());
			if (req != null) info.putAll(req);
		}
		catch (IOException e) {
			LogUtil.warn("osgi", e);
		}
		return info;
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

	private List<Map<String, Object>> listCoreBundles() {
		List<Map<String, Object>> list = new ArrayList<>();
		BundleCollection bc = OSGiUtil.getBundleCollection();
		list.add(toInfo(bc.core));
		Iterator<Bundle> it = bc.getSlaves();
		while (it.hasNext()) {
			list.add(toInfo(it.next()));
		}
		return list;
	}

	private List<Map<String, Object>> listBundles() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Bundle b: context().getBundles()) {
			list.add(toInfo(b));
		}
		return list;
	}

	@Override
	public URL getResource(String bundleName, String bundleVersion, String path) {
		Bundle b = get(bundleName, bundleVersion);
		if (b == null) {
			try {
				b = load(bundleName, bundleVersion);
			}
			catch (IOException e) {
				return null;
			}
		}
		return b.getResource(path);
	}

	@Override
	public ClassLoader getClassLoader(String bundleName, String bundleVersion) throws IOException {
		Bundle b = load(bundleName, bundleVersion);
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

	private Map<String, Object> loadBundle(String bundleName, String bundleVersion) throws IOException {
		return toInfo(load(bundleName, bundleVersion));
	}

	private Map<String, Object> loadFromLocal(String bundleName, String bundleVersion, boolean loadIfNecessary) {
		Bundle b = OSGiUtil.loadBundleFromLocal(context(), bundleName, toVersion(bundleVersion), null, loadIfNecessary, null);
		return b == null ? null : toInfo(b);
	}

	private void removeLocal(String bundleName, String bundleVersion, boolean removeFile) throws IOException {
		try {
			OSGiUtil.removeLocalBundle(bundleName, toVersion(bundleVersion), null, removeFile, false);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}

	private ClassLoader getLoaderClassLoader() throws IOException {
		try {
			return OSGiUtil.getEmptyBundleClassLoader(null);
		}
		catch (BundleException be) {
			throw new IOException(be);
		}
	}

	private String getClassPath() {
		return OSGiUtil.getClassPath();
	}

	/**
	 * @return the jar of the bundle in the local bundle directory, or null when it is not there
	 */
	public BundleFile getBundleFile(String bundleName, lucee.runtime.config.maven.Version version) {
		return OSGiUtil.getBundleFile(bundleName, OSGiUtil.toOSGi(version), null, null, false, null);
	}

	@Override
	public Class<?> findClass(String className) {
		return OSGiUtil.loadClass(className, null);
	}

	private Map<String, Object> getLocalBundleInfo(String bundleName, String bundleVersion) {
		BundleFile bf = OSGiUtil.getBundleFile(bundleName, toVersion(bundleVersion), null, null, false, null);
		return bf == null ? null : toInfo(bf);
	}

	private List<Map<String, Object>> listLocalBundles() {
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

	@Override
	public Class<?> loadClass(String bundleName, String bundleVersion, String className, File[] additionalBundleDirectories, boolean versionOnlyMattersForDownload)
			throws IOException, ClassNotFoundException {
		List<Resource> addional = null;
		if (additionalBundleDirectories != null && additionalBundleDirectories.length > 0) {
			addional = new ArrayList<Resource>();
			for (File f: additionalBundleDirectories) {
				addional.add(ResourceUtil.toResource(f));
			}
		}
		Identification id = null;
		ConfigServer cs = ThreadLocalPageContext.getConfigServer();
		if (cs != null) id = cs.getIdentification();
		try {
			return _loadClass(bundleName, bundleVersion, className, id, addional, versionOnlyMattersForDownload);
		}
		catch (ClassNotFoundException outer) {
			// a bundle may be missing, try to fix it and load again
			if (OSGiUtil.resolveBundleLoadingIssues(null, cs, outer)) return _loadClass(bundleName, bundleVersion, className, id, addional, versionOnlyMattersForDownload);
			throw outer;
		}
	}

	private Class<?> _loadClass(String bundleName, String bundleVersion, String className, Identification id, List<Resource> addional, boolean versionOnlyMattersForDownload)
			throws IOException, ClassNotFoundException {
		try {
			return OSGiUtil.loadBundle(null, bundleName, toVersion(bundleVersion), id, addional, true, versionOnlyMattersForDownload, true, null).loadClass(className);
		}
		catch (BundleException be) {
			throw new IOException(be.getMessage(), be);
		}
	}
}
