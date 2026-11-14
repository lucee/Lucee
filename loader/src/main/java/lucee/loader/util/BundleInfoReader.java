package lucee.loader.util;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * Reads the name and version of a jar that is an OSGi bundle from its manifest, no OSGi involved.
 */
public final class BundleInfoReader {

	private BundleInfoReader() {
	}

	/**
	 * @return symbolic name and version of the bundle as "name|version"
	 */
	public static String loadBundleInfo(final File jar) throws IOException {
		JarFile jf = new JarFile(jar);
		try {
			Attributes attrs = jf.getManifest().getMainAttributes();
			String symbolicName = attrs.getValue("Bundle-SymbolicName");
			String version = attrs.getValue("Bundle-Version");
			if (Util.isEmpty(symbolicName)) throw new IOException("OSGi bundle [" + jar + "] is invalid, META-INF/MANIFEST.MF does not contain a \"Bundle-SymbolicName\"");
			if (Util.isEmpty(version)) throw new IOException("OSGi bundle [" + jar + "] is invalid, META-INF/MANIFEST.MF does not contain a \"Bundle-Version\"");
			return symbolicName + "|" + version;
		}
		finally {
			Util.closeEL(jf);
		}
	}
}
