package lucee.debug;

import org.apache.catalina.Context;
import org.apache.catalina.Server;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class Main {

	public static final String ARG_HOST = "LUCEE_DEBUG_HOST";
	public static final String ARG_PORT = "LUCEE_DEBUG_PORT";
	public static final String ARG_BASE = "LUCEE_DEBUG_BASE";
	public static final String ARG_WEBXML = "LUCEE_DEBUG_WEBXML";

	public static final String DEF_HOST = "localhost";
	public static final String DEF_PORT = "48080";
	public static final String DEF_BASE = "/workspace/test/LuceeDebugWebapp";

	public static void main(String[] args) throws Exception {

		String s;

		System.setProperty("lucee.controller.disabled", "true");

		s = getSystemPropOrEnvVar(ARG_BASE, DEF_BASE);

		String appBase = (new File(s)).getCanonicalPath().replace('\\', '/');
		String docBase = appBase + "/webroot";
		String webxml = getSystemPropOrEnvVar(ARG_WEBXML, docBase + "/WEB-INF/web.xml");

		System.out.println("Setting appBase: " + appBase);
		System.out.println("Setting docBase: " + docBase);
		System.out.println("Setting web.xml: " + webxml);

		File f = new File(webxml);
		if (!f.exists()) throw (new IllegalArgumentException("web.xml not found at " + webxml));

		Tomcat tomcat = new Tomcat();

		tomcat.setBaseDir(appBase);

		s = getSystemPropOrEnvVar(ARG_HOST, DEF_HOST);
		tomcat.setHostname(s);

		s = getSystemPropOrEnvVar(ARG_PORT, DEF_PORT);
		tomcat.setPort(Integer.parseInt(s));

		tomcat.setAddDefaultWebXmlToWebapp(false);

		Context context = tomcat.addWebapp("", docBase);

		context.setAltDDName(webxml);
		context.setLogEffectiveWebXml(true);
		context.setResourceOnlyServlets("CFMLServlet");

		Connector connector = tomcat.getConnector();

		System.out.println(connector);

		tomcat.start();

		Server server = tomcat.getServer();

		server.await();
	}

	/**
	 * converts a System property format to its equivalent Environment variable, e.g. an input of
	 * "lucee.conf.name" will return "LUCEE_CONF_NAME"
	 *
	 * @param name the System property name
	 * @return the equivalent Environment variable name
	 */
	private static String convertSystemPropToEnvVar(String name) {
		return name.replace('.', '_').toUpperCase();
	}

	/**
	 * returns a system setting by either a Java property name or a System environment variable
	 *
	 * @param name - either a lowercased Java property name (e.g. lucee.controller.disabled) or an
	 *            UPPERCASED Environment variable name ((e.g. LUCEE_CONTROLLER_DISABLED))
	 * @param defaultValue - value to return if the neither the property nor the environment setting was
	 *            found
	 * @return - the value of the property referenced by propOrEnv or the defaultValue if not found
	 */
	private static String getSystemPropOrEnvVar(String name, String defaultValue) {
		// env
		String value = System.getenv(name);
		if (!isEmpty(value)) return value;

		// prop
		value = System.getProperty(name);
		if (!isEmpty(value)) return value;

		// env 2
		name = convertSystemPropToEnvVar(name);
		value = System.getenv(name);
		if (!isEmpty(value)) return value;

		return defaultValue;
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
