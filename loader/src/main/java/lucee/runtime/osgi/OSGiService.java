package lucee.runtime.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import lucee.StableInMajor;

/**
 * Access to OSGi. Lucee itself does not know anything about OSGi, this service is provided by the
 * OSGi extension and Lucee only uses it to load classes and resources that are defined with a bundle
 * (symbolic name and version). What the service runs behind the scenes is up to the provider. Only
 * strings and types of the JDK are used, so no OSGi type is part of the API. Get the instance with
 * {@code CFMLEngine.getOSGiService()}.
 * <p>
 * A bundle is identified by its symbolic name and its version. A version is a string, see
 * {@link lucee.runtime.config.maven.Version}; a null or empty version matches any version.
 */
@StableInMajor
public interface OSGiService {

	/**
	 * Load a class from a bundle, the bundle is installed, downloaded and started when necessary.
	 *
	 * @throws IOException when the bundle cannot be loaded or started
	 * @throws ClassNotFoundException when the bundle does not have the class
	 */
	public Class<?> loadClass(String bundleName, String bundleVersion, String className) throws IOException, ClassNotFoundException;

	/**
	 * Same as {@link #loadClass(String, String, String)} with additional options.
	 *
	 * @param additionalBundleDirectories directories with bundle jars to look into besides the
	 *            default location, can be null
	 * @param versionOnlyMattersForDownload when true, a locally available bundle with the same name is
	 *            used even when the version differs, the version is only used when the bundle has to be
	 *            downloaded
	 */
	public Class<?> loadClass(String bundleName, String bundleVersion, String className, File[] additionalBundleDirectories, boolean versionOnlyMattersForDownload)
			throws IOException, ClassNotFoundException;

	/**
	 * Search a class in all available bundles.
	 *
	 * @return the class or null when no bundle has it
	 */
	public Class<?> findClass(String className);

	/**
	 * Search a class in the given bundles only, in any version.
	 *
	 * @return the class or null when none of the bundles has it
	 */
	public Class<?> findClass(String className, String... bundleNames);

	/**
	 * @return the resource from a bundle, null when the bundle or the resource does not exist
	 */
	public URL getResource(String bundleName, String bundleVersion, String path);

	/**
	 * @return the resource from a bundle as a stream, null when the bundle or the resource does not
	 *         exist
	 */
	public InputStream getResourceAsStream(String bundleName, String bundleVersion, String path) throws IOException;

	/**
	 * @return the class loader of a bundle
	 * @throws IOException when there is no such bundle or it cannot be loaded
	 */
	public ClassLoader getClassLoader(String bundleName, String bundleVersion) throws IOException;

	/**
	 * Same as {@link #getClassLoader(String, String)}, the given directories are searched for the
	 * bundle as well.
	 *
	 * @param additionalBundleDirectories can be null
	 */
	public ClassLoader getClassLoader(String bundleName, String bundleVersion, File[] additionalBundleDirectories) throws IOException;

	/**
	 * Hand over a jar that comes with an extension or an archive. When the jar is a bundle it is made
	 * available, otherwise nothing happens.
	 *
	 * @param start start the bundle right away (it's activator runs), otherwise it is started when it
	 *            is used for the first time
	 * @return true when the jar is a bundle and is now available
	 * @throws IOException when the jar is a bundle but cannot be installed
	 */
	public boolean install(File jar, boolean start) throws IOException;

	/**
	 * Remove what {@link #install(File, boolean)} has made available for this jar, does nothing when there is
	 * nothing to remove.
	 */
	public void uninstall(File jar) throws IOException;

	/**
	 * @return an identifier for the bundle the class was loaded from that is stable as long as the
	 *         bundle is not replaced, null when the class is not loaded from a bundle or the bundle
	 *         was uninstalled in the meantime. The value is opaque, it can only be compared and used as
	 *         part of a key.
	 */
	public String getId(Class<?> clazz);

	/**
	 * @return the same for the class loader of a bundle, null when the class loader does not belong to
	 *         a bundle
	 */
	public String getId(ClassLoader classLoader);

	/**
	 * @return the same identifier for a jar that is (or can be) installed with
	 *         {@link #install(File, boolean)}, null when the jar is not a bundle. Two jars with the same
	 *         id are the same bundle, whatever the file name is.
	 */
	public String getId(File jar);

	/**
	 * General information about the bundle the class was loaded from, meant to be displayed (dump,
	 * diagnostics) and not to be interpreted. The content is up to the provider.
	 *
	 * @return the information or null when the class is not loaded from a bundle
	 */
	public Map<String, Object> getInfo(Class<?> clazz);

	/**
	 * Stop everything this service has started.
	 */
	public void shutdown();
}
