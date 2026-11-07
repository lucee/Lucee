package lucee.runtime.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Access to the OSGiService that is provided by the OSGi extension. Without the extension a service
 * is returned that has no bundles.
 */
public final class OSGi {

	private static volatile OSGiService service;

	public static OSGiService get() {
		OSGiService s = service;
		if (s != null) return s;
		synchronized (OSGi.class) {
			if (service == null) {
				OSGiService found = null;
				try {
					for (OSGiService os: ServiceLoader.load(OSGiService.class, OSGi.class.getClassLoader())) {
						found = os;
						break;
					}
				}
				catch (Throwable t) {
					found = null;
				}
				service = found != null ? found : new NoOSGiService();
			}
			return service;
		}
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
