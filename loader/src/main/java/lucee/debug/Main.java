package lucee.debug;

import lucee.loader.util.Util;
import org.apache.catalina.Context;
import org.apache.catalina.Server;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.nio.file.Paths;

import static lucee.loader.engine.CFMLEngineFactory.ARG_PROJECT_DIR;
import static lucee.loader.engine.CFMLEngineFactory.ARG_CLASSES_DIR;

public class Main {

	public static final String ARG_HOST = "LUCEE_DEBUG_HOST";
	public static final String ARG_PORT = "LUCEE_DEBUG_PORT";
	public static final String ARG_BASE = "LUCEE_DEBUG_BASE";
	public static final String ARG_WEBXML = "LUCEE_DEBUG_WEBXML";

	public static final String DEF_HOST = "localhost";
	public static final String DEF_PORT = "48888";
	public static final String DEF_BASE = "/workspace/test/LuceeDebugWebapp";

	public static void main(String[] args) throws Exception {

		String s;

		System.setProperty("lucee.controller.disabled", "true");

		s = Util._getSystemPropOrEnvVar(ARG_PROJECT_DIR, "");
		if (s.isEmpty()) {
			s = Paths.get("").toAbsolutePath().toString();
			System.out.println(ARG_PROJECT_DIR + " is not set, using " + s);
			System.setProperty(convertEnvVarToSysProp(ARG_PROJECT_DIR), s);
		}
		else {
			System.out.println(ARG_PROJECT_DIR + " is set to " + s);
		}

		s = Util._getSystemPropOrEnvVar(ARG_CLASSES_DIR, "");
		if (s.isEmpty()) {
			s = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			s = Paths.get(s).getParent().toString();
			System.out.println(ARG_CLASSES_DIR + " is not set, using " + s);
			System.setProperty(convertEnvVarToSysProp(ARG_CLASSES_DIR), s);
		}
		else {
			System.out.println(ARG_CLASSES_DIR + " is set to " + s);
		}

		s = Util._getSystemPropOrEnvVar(ARG_BASE, DEF_BASE);

		String appBase = (new File(s)).getCanonicalPath().replace('\\', '/');
		String docBase = appBase + "/webroot";
		String webxml = Util._getSystemPropOrEnvVar(ARG_WEBXML, docBase + "/WEB-INF/web.xml");

		System.out.println("Setting appBase: " + appBase);
		System.out.println("Setting docBase: " + docBase);
		System.out.println("Setting web.xml: " + webxml);

		File f = new File(webxml);
		if (!f.exists())
			throw(new IllegalArgumentException("web.xml not found at " + webxml));

		Tomcat tomcat = new Tomcat();

		tomcat.setBaseDir(appBase);

		s = Util._getSystemPropOrEnvVar(ARG_HOST, DEF_HOST);
		tomcat.setHostname(s);

		s = Util._getSystemPropOrEnvVar(ARG_PORT, DEF_PORT);
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
	private static String convertSysPropToEnvVar(String name) {
		return name.replace('.', '_').toUpperCase();
	}

	/**
	 * converts an Environment variable format to its equivalent System property, e.g. an input of
	 * "LUCEE_CONF_NAME" will return "lucee.conf.name"
	 *
	 * @param name the System property name
	 * @return the equivalent Environment variable name
	 */
	private static String convertEnvVarToSysProp(String name) {
		return name.replace('_', '.').toLowerCase();
	}

}
