package lucee.debug;

import java.io.File;
import java.lang.reflect.Method;

public class Main {

    public static final String ARG_HOST = "LUCEE_DEBUG_HOST";
    public static final String ARG_PORT = "LUCEE_DEBUG_PORT";
    public static final String ARG_BASE = "LUCEE_DEBUG_BASE";
    public static final String ARG_WEBXML = "LUCEE_DEBUG_BASE";

    public static final String DEF_HOST = "localhost";
    public static final String DEF_PORT = "48080";
    public static final String DEF_BASE = "/workspace/test/LuceeDebugWebapp";

    public static void main(String[] args) throws Exception {

        String s;

        String webxml = getSystemPropOrEnvVar(ARG_WEBXML, "");
        if (webxml.isEmpty())
            webxml = Main.class.getResource("/debug/web.xml").getPath();

        s = getSystemPropOrEnvVar(ARG_BASE, DEF_BASE);

        String appBase = (new File(s)).getCanonicalPath().replace('\\', '/');
        String docBase = appBase + "/webroot";

        System.out.println("Setting appBase: " + appBase);
        System.out.println("Setting docBase: " + docBase);

        Class clsTomcat = Class.forName("org.apache.catalina.startup.Tomcat");
        Method tAddWebApp    = clsTomcat.getMethod("addWebapp", String.class, String.class);
        Method tGetConnector = clsTomcat.getMethod("getConnector");
        Method tGetServer    = clsTomcat.getMethod("getServer");
        Method tSetAddDefaultWebXmlToWebapp = clsTomcat.getMethod("setAddDefaultWebXmlToWebapp", boolean.class);
        Method tSetBaseDir   = clsTomcat.getMethod("setBaseDir", String.class);
        Method tSetHostname  = clsTomcat.getMethod("setHostname", String.class);
        Method tSetPort      = clsTomcat.getMethod("setPort", int.class);
        Method tStart        = clsTomcat.getMethod("start");

        Class clsContext = Class.forName("org.apache.catalina.Context");
        Method cSetAltDDName            = clsContext.getMethod("setAltDDName", String.class);
        Method cSetLogEffectiveWebXml   = clsContext.getMethod("setLogEffectiveWebXml", boolean.class);
        Method cSetResourceOnlyServlets = clsContext.getMethod("setResourceOnlyServlets", String.class);

        Class clsServer = Class.forName("org.apache.catalina.Server");
        Method sAwait = clsServer.getMethod("await");

        Object oTomcat = clsTomcat.newInstance();

        tSetBaseDir.invoke(oTomcat, appBase);

        s = getSystemPropOrEnvVar(ARG_HOST, DEF_HOST);
        tSetHostname.invoke(oTomcat, s);

        s = getSystemPropOrEnvVar(ARG_PORT, DEF_PORT);
        tSetPort.invoke(oTomcat, Integer.parseInt(s));
        tSetAddDefaultWebXmlToWebapp.invoke(oTomcat, false);

        Object oContext = tAddWebApp.invoke(oTomcat, "", docBase);

        cSetAltDDName.invoke(oContext, webxml);
        cSetLogEffectiveWebXml.invoke(oContext, true);
        cSetResourceOnlyServlets.invoke(oContext, "CFMLServlet");

        System.out.println(
            tGetConnector.invoke(oTomcat)
        );

        // tomcat.start()
        tStart.invoke(oTomcat);

        // tomcat.getServer()
        Object oServer = tGetServer.invoke(oTomcat);

        // server.await();
        sAwait.invoke(oServer);
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
