package lucee.runtime.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Access to the OSGiService that is provided by the OSGi extension. Without the extension a service
 * is returned that has no bundles.
 */
public final class OSGi {

	private static volatile OSGiService service;
	private static volatile File libDir;
	private static long checkedStamp = -1;

	/**
	 * Sets the lib directory of the config, an extension providing the OSGiService is installed there.
	 */
	public static void setLibraryDirectory(File dir) {
		if (dir != null && !dir.equals(libDir)) {
			libDir = dir;
			reset();
		}
	}

	/**
	 * Forget a service that was not found, so the next call looks again (a jar was added to lib).
	 */
	public static void reset() {
		synchronized (OSGi.class) {
			if (service instanceof NoOSGiService) {
				service = null;
				checkedStamp = -1;
			}
		}
	}

	public static OSGiService get() {
		OSGiService s = service;
		if (s != null && !(s instanceof NoOSGiService && libChanged())) return s;
		synchronized (OSGi.class) {
			if (service == null || (service instanceof NoOSGiService && libChanged())) {
				OSGiService found = discover();
				checkedStamp = stamp();
				service = found != null ? found : new NoOSGiService();
			}
			return service;
		}
	}

	private static long stamp() {
		File dir = libDir;
		if (dir == null) return -1;
		long stamp = dir.lastModified();
		File[] jars = dir.listFiles();
		if (jars != null) stamp += jars.length;
		return stamp;
	}

	private static boolean libChanged() {
		return libDir != null && stamp() != checkedStamp;
	}

	private static OSGiService discover() {
		try {
			List<URL> urls = new ArrayList<>();
			File dir = libDir;
			File[] jars = dir == null ? null : dir.listFiles((d, n) -> n.endsWith(".jar"));
			if (jars != null) {
				for (File jar: jars) {
					urls.add(jar.toURI().toURL());
				}
			}
			ClassLoader cl = urls.isEmpty() ? OSGi.class.getClassLoader() : new URLClassLoader(urls.toArray(new URL[0]), OSGi.class.getClassLoader());
			for (OSGiService os: ServiceLoader.load(OSGiService.class, cl)) {
				return os;
			}
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
		}
		return null;
	}

	private static final class NoOSGiService implements OSGiService {

		private static IOException missing(String name, String version) {
			return new IOException("the OSGi extension is not installed, cannot load the bundle [" + name + ":" + version + "]");
		}

		@Override
		public Class<?> loadClass(String name, String version, String className) throws IOException {
			throw missing(name, version);
		}

		@Override
		public Class<?> loadClass(String name, String version, String className, File[] dirs, boolean versionOnlyMattersForDownload) throws IOException {
			throw missing(name, version);
		}

		@Override
		public Class<?> findClass(String className) {
			return null;
		}

		@Override
		public Class<?> findClass(String className, String... bundleNames) {
			return null;
		}

		@Override
		public URL getResource(String name, String version, String path) {
			return null;
		}

		@Override
		public InputStream getResourceAsStream(String name, String version, String path) {
			return null;
		}

		@Override
		public ClassLoader getClassLoader(String name, String version) throws IOException {
			throw missing(name, version);
		}

		@Override
		public ClassLoader getClassLoader(String name, String version, File[] dirs) throws IOException {
			throw missing(name, version);
		}

		@Override
		public boolean install(File jar, boolean start) {
			return false;
		}

		@Override
		public void uninstall(File jar) {
		}

		@Override
		public String getId(Class<?> clazz) {
			return null;
		}

		@Override
		public String getId(ClassLoader classLoader) {
			return null;
		}

		@Override
		public String getId(File jar) {
			return null;
		}

		@Override
		public Map<String, Object> getInfo(Class<?> clazz) {
			return null;
		}

		@Override
		public void shutdown() {
		}
	}
}
