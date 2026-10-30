package lucee.runtime.osgi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleWiring;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * The BundleService based on the OSGi framework (Felix) that is part of the core.
 */
public final class BundleServiceImpl implements BundleService {

	private static final BundleServiceImpl INSTANCE = new BundleServiceImpl();

	public static BundleServiceImpl getInstance() {
		return INSTANCE;
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
		info.put("location", b.getLocation());
		info.put("fragment", Boolean.valueOf(OSGiUtil.isFragment(b)));
		return info;
	}
}
