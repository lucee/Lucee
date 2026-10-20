package lucee.runtime.regex;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;
import lucee.runtime.listener.ApplicationContext;

/**
 * LDEV-6660: the Perl5 regex engine (Apache ORO) is deprecated as the default regex engine. Lucee 9
 * uses the Java engine by default and moves the Perl5 engine into the Perl Regex extension. Logs a
 * warning to the application log once per application (and context), not on every regex call.
 */
public final class RegexDeprecation {

	public static final String MESSAGE = "The regex engine [perl] (Apache ORO) is deprecated. Lucee 9 uses the regex engine [java] by default "
			+ "and the [perl] engine is only available with the Perl Regex extension. "
			+ "Test your application with this.regex.type=\"java\" (or \"compat\" to keep the Perl-style replacement syntax) in the Application.cfc, "
			+ "or with the regexType setting of the server (system property lucee.regex.type, environment variable LUCEE_REGEX_TYPE). "
			+ "To stay on [perl] with Lucee 9, set the regex type [perl] explicitly and install the Perl Regex extension.";

	private static final Set<String> warned = ConcurrentHashMap.newKeySet();

	private RegexDeprecation() {
	}

	public static boolean isPerl(Regex regex) {
		return regex != null && "perl".equals(regex.getTypeName());
	}

	/**
	 * logs the deprecation warning if the given engine is the perl engine and it was not yet logged for
	 * this application
	 * 
	 * @return true if the warning was logged by this call
	 */
	public static boolean warnIfPerl(PageContext pc, Regex regex) {
		if (!isPerl(regex) || pc == null) return false;
		String key = toKey(pc);
		if (!warned.add(key)) return false;
		ApplicationContext ac = pc.getApplicationContext();
		String appName = ac == null ? "" : ac.getName();
		LogUtil.log(pc, Log.LEVEL_WARN, "application", RegexDeprecation.class.getName(),
				"Application [" + (appName == null ? "" : appName) + "]: " + MESSAGE);
		return true;
	}

	/**
	 * true if the warning was already logged for the application of the given page context
	 */
	public static boolean hasWarned(PageContext pc) {
		return pc != null && warned.contains(toKey(pc));
	}

	private static String toKey(PageContext pc) {
		ApplicationContext ac = pc.getApplicationContext();
		String appName = ac == null ? null : ac.getName();
		return System.identityHashCode(pc.getConfig()) + ":" + (appName == null ? "" : appName.toLowerCase());
	}
}
