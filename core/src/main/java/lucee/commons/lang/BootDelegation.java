package lucee.commons.lang;

import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.SystemUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

/**
 * The packages the class loader of Lucee hands over to the parent. The system property / environment variable org.osgi.framework.bootdelegation adds packages.
 */
public final class BootDelegation {

	// the packages that are always handed over to the parent: the JDK, the servlet container and the Lucee core
	private static final String[] DEFAULT_BOOT_DELEGATION = new String[] {
			"coldfusion.xml.rpc",
			"com.apple.laf", "com.apple.laf.*", "com.integral.fusiondebug.server", "com.sun.jndi", "com.sun.jndi.*", "com.sun.msv.datatype", "com.sun.msv.datatype.*", "com.sun.org.apache.xerces.internal.*", "com.sun.org.glassfish", "com.sun.org.glassfish.*", "com.sun.proxy", "com.sun.proxy.*", "com.sun.xml.internal.ws.api.*",
			"jakarta.annotation", "jakarta.annotation.processing", "jakarta.el", "jakarta.enterprise.context", "jakarta.enterprise.inject", "jakarta.inject", "jakarta.security.*", "jakarta.security.auth", "jakarta.security.auth.callback", "jakarta.security.auth.login", "jakarta.security.auth.x500", "jakarta.security.cert", "jakarta.security.cert.*", "jakarta.security.sasl", "jakarta.servlet", "jakarta.servlet.http", "jakarta.servlet.jsp", "jakarta.servlet.jsp.tagext", "jakarta.servlet.jsp.el", "jakarta.transaction", "jakarta.validation", "jakarta.websocket", "jakarta.websocket.*", "jakarta.xml.*",
			"java.*",
			"javax.accessibility", "javax.annotation", "javax.annotation.processing", "javax.crypto", "javax.crypto.interfaces", "javax.crypto.spec", "javax.enterprise.context", "javax.enterprise.inject", "javax.imageio", "javax.imageio.event", "javax.imageio.metadata", "javax.imageio.plugins.bmp", "javax.imageio.plugins.jpeg", "javax.imageio.plugins.tiff", "javax.imageio.spi", "javax.imageio.stream", "javax.inject", "javax.jmdns", "javax.management", "javax.management.*", "javax.naming", "javax.naming.*", "javax.net", "javax.net.ssl", "javax.print", "javax.print.attribute", "javax.security.*", "javax.security.auth", "javax.security.auth.callback", "javax.security.auth.kerberos", "javax.security.auth.login", "javax.security.auth.x500", "javax.security.cert", "javax.security.cert.*", "javax.security.sasl", "javax.servlet", "javax.servlet.http", "javax.servlet.jsp", "javax.servlet.jsp.tagext", "javax.servlet.jsp.el", "javax.sql", "javax.swing", "javax.swing.border", "javax.swing.event", "javax.swing.filechooser", "javax.swing.plaf", "javax.swing.plaf.*", "javax.swing.table", "javax.swing.table.*", "javax.swing.text", "javax.swing.tree", "javax.tools", "javax.transaction", "javax.validation", "javax.websocket", "javax.websocket.*", "javax.xml.*",
			"jdk.*", "jdk.internal.reflect", "jdk.internal.reflect.*", "jdk.jfr.internal.event",
			"lucee", "lucee.*",
			"org.ietf.jgss", "org.osgi.framework", "org.omg.CORBA", "org.omg.CORBA.portable", "org.osgi.*", "org.w3c.dom", "org.w3c.dom.*", "org.xml.sax", "org.xml.sax.*",
			"sun.*", "sun.misc" };

	private static String[] bootDelegation;

	public static String[] getBootdelegation() {
		if (bootDelegation == null) {
			// the system property / environment variable adds packages to the list
			String additional = Caster.toString(SystemUtil.getSystemPropOrEnvVar("org.osgi.framework.bootdelegation", null), null);
			if (StringUtil.isEmpty(additional, true)) bootDelegation = DEFAULT_BOOT_DELEGATION;
			else {
				String[] add = ListUtil.trimItems(ListUtil.listToStringArray(StringUtil.unwrap(additional.trim()), ','));
				String[] result = new String[add.length + DEFAULT_BOOT_DELEGATION.length];
				System.arraycopy(add, 0, result, 0, add.length);
				System.arraycopy(DEFAULT_BOOT_DELEGATION, 0, result, add.length, DEFAULT_BOOT_DELEGATION.length);
				bootDelegation = result;
			}
		}
		return bootDelegation;
	}

	// the result only depends on the package and the boot delegation list (loaded once), so we cache by
	// package name; the number of packages is small, but we still cap the cache to avoid slow growth
	private static final int BOOTELEGATION_CACHE_MAX = 10_000;
	private static final ConcurrentHashMap<String, Boolean> BOOTELEGATION_CACHE = new ConcurrentHashMap<>();

	public static boolean isClassInBootelegation(String className) {
		int index = className.lastIndexOf('.');
		if (index == -1) return false;
		return isPackageInBootelegation(className.substring(0, index));
	}

	public static boolean isPackageInBootelegation(String packageName) {
		Boolean cached = BOOTELEGATION_CACHE.get(packageName);
		if (cached != null) return cached.booleanValue();
		boolean result = isInBootelegation(packageName, true);
		// only cache once the boot delegation list is available
		if (getBootdelegation().length == 0) return result;
		if (BOOTELEGATION_CACHE.size() >= BOOTELEGATION_CACHE_MAX) BOOTELEGATION_CACHE.clear();
		BOOTELEGATION_CACHE.put(packageName, result ? Boolean.TRUE : Boolean.FALSE);
		return result;
	}

	private static boolean isInBootelegation(String name, boolean isPackage) {
		// extract package
		String pack;
		if (isPackage) pack = name;
		else {
			int index = name.lastIndexOf('.');
			if (index == -1) return false;
			pack = name.substring(0, index);
		}

		String[] arr = getBootdelegation();
		for (String bd: arr) {
			// with wildcard
			if (bd.endsWith(".*")) {
				bd = bd.substring(0, bd.length() - 1);
				if (pack.startsWith(bd)) return true;
			}
			// no wildcard
			else {
				if (bd.equals(pack)) return true;
			}
		}
		return false;
	}
}
