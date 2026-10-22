package lucee.commons.io.log;

import lucee.commons.lang.StringUtil;

/**
 * Recognizes the names a log appender or layout was known by over time: the simple name, the class
 * names of the former log4j based implementations in the core, the class names of the Log4j
 * extension and the class names of the built-in JUL engine. A configuration can contain any of them,
 * no matter which log engine is active.
 */
public final class LogLegacyHandler {

	private LogLegacyHandler() {
	}

	public static boolean isConsoleAppender(String name) {
		return "console".equalsIgnoreCase(name) || JULLogEngine.ConsoleAppenderType.class.getName().equals(name) || "lucee.commons.io.log.log4j.appender.ConsoleAppender".equals(name)
				|| "lucee.commons.io.log.log4j2.appender.ConsoleAppender".equals(name) || "org.lucee.extension.log4j.appender.ConsoleAppender".equals(name);
	}

	public static boolean isResourceAppender(String name) {
		return "resource".equalsIgnoreCase(name) || JULLogEngine.ResourceAppenderType.class.getName().equals(name)
				|| "lucee.commons.io.log.log4j.appender.RollingResourceAppender".equals(name) || "lucee.commons.io.log.log4j2.appender.ResourceAppender".equals(name)
				|| "org.lucee.extension.log4j.appender.ResourceAppender".equals(name);
	}

	public static boolean isClassicLayout(String name) {
		return StringUtil.isEmpty(name, true) || "classic".equalsIgnoreCase(name) || "datasource".equalsIgnoreCase(name) || JULLogEngine.ClassicLayoutType.class.getName().equals(name)
				|| "lucee.commons.io.log.log4j.layout.ClassicLayout".equals(name) || "lucee.commons.io.log.log4j2.layout.ClassicLayout".equals(name)
				|| "lucee.commons.io.log.log4j.layout.DatasourceLayout".equals(name) || "org.lucee.extension.log4j.layout.ClassicLayout".equals(name);
	}

	public static boolean isPatternLayout(String name) {
		return "pattern".equalsIgnoreCase(name) || JULLogEngine.PatternLayoutType.class.getName().equals(name) || "org.apache.log4j.PatternLayout".equals(name)
				|| "org.apache.logging.log4j.core.layout.PatternLayout".equals(name);
	}
}
