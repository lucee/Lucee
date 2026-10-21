package lucee.commons.io.log.slf4j;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.engine.ThreadLocalPageContext;

/**
 * LDEV-6664 Lucee side of the SLF4J bridge, free of SLF4J types. Messages go to the log named by
 * "lucee.slf4j.log" (system property or env var LUCEE_SLF4J_LOG), "application" by default.
 */
public final class SLF4JLog {

	private static final String LOG_NAME = SystemUtil.getSystemPropOrEnvVar("lucee.slf4j.log", "application");

	private SLF4JLog() {
	}

	public static boolean isEnabled(int level) {
		Log log = ThreadLocalPageContext.getLog(LOG_NAME);
		if (log == null) return level >= Log.LEVEL_WARN;
		switch (level) {
		case Log.LEVEL_TRACE:
			return LogUtil.doesTrace(log);
		case Log.LEVEL_DEBUG:
			return LogUtil.doesDebug(log);
		case Log.LEVEL_INFO:
			return LogUtil.doesInfo(log);
		default:
			return LogUtil.does(log, level);
		}
	}

	public static void log(int level, String loggerName, String msg, Throwable t) {
		Log log = ThreadLocalPageContext.getLog(LOG_NAME);
		if (log != null) {
			if (t != null) log.log(level, loggerName, msg, t);
			else log.log(level, loggerName, msg);
		}
		else {
			if (t != null) LogUtil.logGlobal(level, LOG_NAME + ":" + loggerName, msg + ";" + lucee.commons.lang.ExceptionUtil.getStacktrace(t, true));
			else LogUtil.logGlobal(level, LOG_NAME + ":" + loggerName, msg);
		}
	}
}
