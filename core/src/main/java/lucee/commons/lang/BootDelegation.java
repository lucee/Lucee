package lucee.commons.lang;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

/**
 * The packages the class loader of Lucee hands over to the parent (org.osgi.framework.bootdelegation).
 */
public final class BootDelegation {

	private static String[] bootDelegation;

	public static String[] getBootdelegation() {
		if (bootDelegation == null) {
			InputStream is = null;
			try {
				Properties prop = new Properties();
				// Lucee is one jar, the default.properties in the root is the one of Felix
				is = BootDelegation.class.getClassLoader().getResourceAsStream("lucee/default.properties");
				if (is == null) is = BootDelegation.class.getClassLoader().getResourceAsStream("default.properties");
				prop.load(is);
				String bd = prop.getProperty("org.osgi.framework.bootdelegation");
				String addional = Caster.toString(SystemUtil.getSystemPropOrEnvVar("org.osgi.framework.bootdelegation", null), null);

				if (!StringUtil.isEmpty(addional, true)) {
					if (!StringUtil.isEmpty(bd)) bd = addional.trim() + "," + bd;
					else bd = addional.trim();
				}

				if (!StringUtil.isEmpty(bd)) {
					bootDelegation = ListUtil.trimItems(ListUtil.listToStringArray(StringUtil.unwrap(bd), ','));
				}
			}
			catch (IOException ioe) {}
			finally {
				IOUtil.closeEL(is);
			}
		}
		if (bootDelegation == null) return new String[0];
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
