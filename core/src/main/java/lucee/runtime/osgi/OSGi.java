package lucee.runtime.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Access to the OSGiService that is provided by the OSGi extension. Without the extension a service
 * is returned that has no bundles.
 */
public final class OSGi {

	private static volatile OSGiService service;
	private static volatile File libDir;
	private static long checkedStamp = -1;
	private static Throwable failure;

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

	/**
	 * @return true when an OSGiService is provided (the OSGi extension is installed and loaded)
	 */
	public static boolean isAvailable() {
		return !(get() instanceof NoOSGiService);
	}

	/**
	 * @return a description of a bundle (and the class) for error messages
	 */
	public static String what(String className, String bundleName, String bundleVersion) {
		String bundle = "the OSGi bundle [" + bundleName + (bundleVersion == null || bundleVersion.trim().isEmpty() ? "" : ":" + bundleVersion.trim()) + "]";
		return className == null || className.trim().isEmpty() ? bundle : "the class [" + className.trim() + "] from " + bundle;
	}

	public static String notInstalledMessage(String what) {
		return "Cannot load " + what + ": OSGi is required for this, but OSGi is not installed. Install the OSGi extension (org.lucee:osgi-extension), "
				+ "or use a Maven based definition (class and maven coordinates) instead of an OSGi bundle.";
	}

	public static OSGiService get() {
		OSGiService s = service;
		if (s != null && !(s instanceof NoOSGiService && libChanged())) return s;
		synchronized (OSGi.class) {
			if (service == null || (service instanceof NoOSGiService && libChanged())) {
				failure = null;
				OSGiService found = discover();
				checkedStamp = stamp();
				service = found != null ? found : new NoOSGiService(failure);
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
		URLClassLoader ucl = null;
		try {
			List<URL> urls = new ArrayList<>();
			File dir = libDir;
			File[] jars = dir == null ? null : dir.listFiles((d, n) -> n.endsWith(".jar"));
			if (jars != null) {
				for (File jar: jars) {
					urls.add(jar.toURI().toURL());
				}
			}
			ClassLoader cl = OSGi.class.getClassLoader();
			if (!urls.isEmpty()) cl = ucl = new URLClassLoader(urls.toArray(new URL[0]), cl);
			for (OSGiService os: ServiceLoader.load(OSGiService.class, cl)) {
				// the provider needs the class loader (and its jars) from now on
				ucl = null;
				return os;
			}
		}
		catch (ServiceConfigurationError | LinkageError | IOException e) {
			// a provider exists but is broken, this is not the same as no provider
			failure = e;
		}
		finally {
			// no provider found, do not keep the jar files open
			if (ucl != null) {
				try {
					ucl.close();
				}
				catch (IOException ioe) {
				}
			}
		}
		return null;
	}

	private static final class NoOSGiService implements OSGiService {

		private final Throwable cause;

		private NoOSGiService(Throwable cause) {
			this.cause = cause;
		}

		private IOException missing(String name, String version) {
			return missing(what(null, name, version));
		}

		private IOException missing(String name, String version, String className) {
			return missing(what(className, name, version));
		}

		private IOException missing(String what) {
			if (cause != null) return new IOException("Cannot load " + what + ": the OSGi extension is installed but failed to load: " + cause, cause);
			return new OSGiNotAvailableException(notInstalledMessage(what));
		}

		@Override
		public Class<?> loadClass(String name, String version, String className) throws IOException {
			throw missing(name, version, className);
		}

		@Override
		public Class<?> loadClass(String name, String version, String className, File[] dirs, boolean versionOnlyMattersForDownload) throws IOException {
			throw missing(name, version, className);
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
