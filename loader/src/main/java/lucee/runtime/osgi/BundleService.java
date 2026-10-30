package lucee.runtime.osgi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import lucee.StableInMajor;

/**
 * Access to the OSGi bundles of Lucee. Only strings and types of the JDK are used, so no OSGi type
 * is part of the API. Get the instance with {@code CFMLEngine.getBundleService()}.
 * <p>
 * A bundle is identified by its symbolic name and its version. A version is a string, see
 * {@link lucee.runtime.config.maven.Version}; a null or empty version matches any version.
 * <p>
 * A bundle info is a map with the keys {@code name}, {@code version}, {@code id}, {@code state}
 * (installed, resolved, starting, active, stopping, uninstalled), {@code location} and
 * {@code fragment} (Boolean).
 */
@StableInMajor
public interface BundleService {

	/**
	 * @return true when OSGi is available
	 */
	public boolean isAvailable();

	/**
	 * Load a class from a bundle, the bundle is downloaded and started when necessary.
	 *
	 * @throws IOException when the bundle cannot be loaded or started
	 * @throws ClassNotFoundException when the bundle does not have the class
	 */
	public Class<?> loadClass(String bundleName, String bundleVersion, String className) throws IOException, ClassNotFoundException;

	/**
	 * Install a bundle (jar) and optionally start it.
	 *
	 * @return the info of the installed bundle
	 * @throws IOException when the file is not a bundle or the bundle cannot be installed or started
	 */
	public Map<String, Object> install(File bundle, boolean start) throws IOException;

	/**
	 * Start a bundle that is installed.
	 *
	 * @throws IOException when the bundle does not exist or cannot be started
	 */
	public void start(String bundleName, String bundleVersion) throws IOException;

	/**
	 * Stop a bundle, does nothing when the bundle does not exist or is not active.
	 */
	public void stop(String bundleName, String bundleVersion) throws IOException;

	/**
	 * Uninstall a bundle, does nothing when the bundle does not exist.
	 */
	public void uninstall(String bundleName, String bundleVersion) throws IOException;

	/**
	 * @return the info of the installed bundle, null when there is no such bundle
	 */
	public Map<String, Object> getBundleInfo(String bundleName, String bundleVersion);

	/**
	 * @return the info of the bundle the class was loaded from, null when the class is not loaded
	 *         from a bundle
	 */
	public Map<String, Object> getBundleInfo(Class<?> clazz);

	/**
	 * @return the infos of all installed bundles
	 */
	public List<Map<String, Object>> listBundles();

	/**
	 * @return the resource from a bundle, null when the bundle or the resource does not exist
	 */
	public URL getResource(String bundleName, String bundleVersion, String path);

	/**
	 * @return the classloader of an installed bundle
	 * @throws IOException when there is no such bundle
	 */
	public ClassLoader getClassLoader(String bundleName, String bundleVersion) throws IOException;
}
