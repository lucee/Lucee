package lucee.commons.io.log.slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.runtime.op.Caster;

/**
 * LDEV-6664 Makes an SLF4J 2.x provider available to classloaders that load slf4j-api 2.x from
 * Maven jars (javaSettings.maven / mavenLoad). Without it, SLF4J falls back to its NOP logger and
 * all log output of these libraries is lost.
 * 
 * This class does not reference any SLF4J type, so it is safe to load in core. The provider classes
 * in this package are compiled against slf4j-api, but are never loaded by the core classloader:
 * their bytecode is defined in the Maven classloader itself, so they link against the slf4j-api
 * that classloader provides.
 */
public final class SLF4JBridge {

	public static final String PACKAGE = "lucee.commons.io.log.slf4j.";
	public static final String PROVIDER_CLASS = PACKAGE + "LuceeSLF4JServiceProvider";
	public static final String SERVICE_FILE = "META-INF/services/org.slf4j.spi.SLF4JServiceProvider";

	public static final String SLF4J2_MARKER = "org/slf4j/spi/SLF4JServiceProvider.class";

	private static final boolean ENABLED = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.slf4j.bridge", null), true);

	private SLF4JBridge() {
	}

	/**
	 * the bridge applies to a Maven classloader when its jars contain slf4j-api 2.x and none of them
	 * brings its own SLF4J provider, the caller does the lookups on its own jars only
	 */
	public static boolean applies(URLClassLoader cl, URL slf4j2Marker, Enumeration<URL> serviceFiles) {
		if (!ENABLED || cl.getURLs().length == 0) return false;
		if (slf4j2Marker == null) return false;
		return serviceFiles == null || !serviceFiles.hasMoreElements();
	}

	/**
	 * true for the provider classes this bridge defines in the Maven classloader (not this class)
	 */
	public static boolean isBridgeClass(String name) {
		return name.startsWith(PACKAGE) && !name.equals(SLF4JBridge.class.getName()) && !name.equals(SLF4JLog.class.getName());
	}

	public static byte[] getBytecode(String name) throws ClassNotFoundException {
		InputStream is = SLF4JBridge.class.getClassLoader().getResourceAsStream(name.replace('.', '/').concat(".class"));
		if (is == null) throw new ClassNotFoundException(name);
		try {
			return IOUtil.toBytes(is, true);
		}
		catch (IOException e) {
			throw new ClassNotFoundException(name, e);
		}
	}

	/**
	 * a URL pointing to an in memory service file that registers the Lucee provider
	 */
	public static URL getServiceFileURL() throws IOException {
		final byte[] content = PROVIDER_CLASS.concat("\n").getBytes(StandardCharsets.UTF_8);
		return new URL(null, "lucee-slf4j:/" + SERVICE_FILE, new URLStreamHandler() {
			@Override
			protected URLConnection openConnection(URL u) {
				return new URLConnection(u) {
					@Override
					public void connect() {
					}

					@Override
					public InputStream getInputStream() {
						return new ByteArrayInputStream(content);
					}
				};
			}
		});
	}
}
