/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.commons.io.log;

import java.nio.charset.StandardCharsets;

import java.io.File;
import java.util.Date;

import lucee.aprint;
import lucee.commons.i18n.FormatUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.SystemOut;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.engine.ThreadLocalPageContext;

/**
 * Helper class for the logs
 */
public final class LogUtil {

	public static Resource ERR, OUT;
	private static int MIN_LEVEL;

	static {
		MIN_LEVEL = LogUtil.toLevel(SystemUtil.getSystemPropOrEnvVar("lucee.system.log.level", null), Log.LEVEL_WARN);
	}

	public static int toLevel(String strLevel, int defaultValue) {
		if (strLevel == null) return defaultValue;
		strLevel = strLevel.toLowerCase().trim();
		if (strLevel.startsWith("info")) return Log.LEVEL_INFO;
		if (strLevel.startsWith("debug")) return Log.LEVEL_DEBUG;
		if (strLevel.startsWith("warn")) return Log.LEVEL_WARN;
		if (strLevel.startsWith("error")) return Log.LEVEL_ERROR;
		if (strLevel.startsWith("fatal")) return Log.LEVEL_FATAL;
		if (strLevel.startsWith("trace")) return Log.LEVEL_TRACE;
		return defaultValue;
	}

	public static String levelToString(int level, String defaultValue) {
		if (Log.LEVEL_INFO == level) return "info";
		if (Log.LEVEL_DEBUG == level) return "debug";
		if (Log.LEVEL_WARN == level) return "warn";
		if (Log.LEVEL_ERROR == level) return "error";
		if (Log.LEVEL_FATAL == level) return "fatal";
		if (Log.LEVEL_TRACE == level) return "trace";

		return defaultValue;
	}

	public static boolean isAlreadyInLog() {
		StackTraceElement[] stes = Thread.currentThread().getStackTrace();
		if (stes != null) {
			String str;
			for (StackTraceElement ste: stes) {
				str = ste.getClassName();
				if (str.indexOf("org.apache.log4j.") == 0 || str.indexOf("org.apache.logging.log4j.") == 0 || str.indexOf("lucee.commons.io.log.log4j") == 0) return true;
			}
		}
		return false;
	}

	//////////
	public static void log(int level, String type, String msg) {
		log(level, "application", type, msg);
	}

	public static void log(PageContext pc, int level, String type, String msg) {
		log(pc, level, "application", type, msg);
	}

	public static void log(Config config, int level, String type, String msg) {
		log(config, level, "application", type, msg);
	}

	public static void warn(String type, Throwable t) {
		log((Config) null, type, t, Log.LEVEL_WARN, "application");
	}

	public static void info(String type, Throwable t) {
		log((Config) null, type, t, Log.LEVEL_INFO, "application");
	}

	public static void trace(String type, Throwable t) {
		log((Config) null, type, t, Log.LEVEL_TRACE, "application");
	}

	//////////
	public static void log(String type, Throwable t) {
		log((Config) null, type, t, Log.LEVEL_ERROR, "application");
	}

	public static void log(int level, String type, Throwable t) {
		log((Config) null, type, t, level, "application");
	}

	public static void log(PageContext pc, String type, Throwable t) {
		log(pc, "application", type, t);
	}

	public static void log(Config config, String type, Throwable t) {
		log(config, type, t, Log.LEVEL_ERROR, "application");
	}

	//////////
	public static void log(String logName, String type, Throwable t) {
		log((Config) null, type, t, Log.LEVEL_ERROR, logName);
	}

	public static void log(String type, Throwable t, String... logNames) {
		log((Config) null, type, t, Log.LEVEL_ERROR, logNames);
	}

	public static void log(PageContext pc, String logName, String type, Throwable t) {
		log(pc, logName, type, t, Log.LEVEL_ERROR);
	}

	//////////
	@Deprecated
	public static void log(String logName, String type, Throwable t, int logLevel) {
		Log log = ThreadLocalPageContext.getLog(logName);
		if (log != null) {
			if (Log.LEVEL_ERROR == logLevel) log.error(type, t);
			else log.log(logLevel, type, t);
		}
		else logGlobal(logLevel, type, ExceptionUtil.getStacktrace(t, true));
	}

	@Deprecated
	public static void log(Config config, String logName, String type, Throwable t, int logLevel) {
		Log log = ThreadLocalPageContext.getLog(config, logName);
		if (log != null) {
			if (Log.LEVEL_ERROR == logLevel) log.error(type, t);
			else log.log(logLevel, type, t);
		}
		else logGlobal(config, logLevel, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void log(Config config, String type, Throwable t, int logLevel, String... logNames) {
		Log log = null;
		for (String ln: logNames) {
			log = ThreadLocalPageContext.getLog(config, ln, false);
			if (log != null) break;
		}

		if (log != null) {
			if (Log.LEVEL_ERROR == logLevel) log.error(type, t);
			else log.log(logLevel, type, t);
		}
		else logGlobal(ThreadLocalPageContext.getConfigServer(config), logLevel, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void log(PageContext pc, String logName, String type, Throwable t, int logLevel) {
		Log log = ThreadLocalPageContext.getLog(pc, logName);
		if (log != null) {
			if (Log.LEVEL_ERROR == logLevel) log.error(type, t);
			else log.log(logLevel, type, t);
		}
		else logGlobal(ThreadLocalPageContext.getConfigServer(pc), logLevel, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void log(PageContext pc, String type, Throwable t, int logLevel, String... logNames) {
		Log log = null;
		for (String ln: logNames) {
			log = ThreadLocalPageContext.getLog(pc, ln, false);
			if (log != null) break;
		}

		if (log != null) {
			if (Log.LEVEL_ERROR == logLevel) log.error(type, t);
			else log.log(logLevel, type, t);
		}
		else logGlobal(ThreadLocalPageContext.getConfigServer(pc), logLevel, type, ExceptionUtil.getStacktrace(t, true));
	}

	//////////
	public static void log(int level, String logName, String type, String msg) {
		Log log = ThreadLocalPageContext.getLog(logName);
		if (log != null) log.log(level, type, msg);
		else {
			logGlobal(level, logName + ":" + type, msg);
		}
	}

	public static void logx(Config config, int level, String type, String msg, String... logNames) {
		Log log = null;
		for (String ln: logNames) {
			log = ThreadLocalPageContext.getLog(config, ln, false);
			if (log != null) break;
		}
		if (log != null) log.log(level, type, msg);
		else {
			logGlobal(ThreadLocalPageContext.getConfigServer(config), level, logNames[0] + ":" + type, msg);
		}
	}

	public static void log(Config config, int level, String logName, String type, String msg) {
		Log log = ThreadLocalPageContext.getLog(config, logName);
		if (log != null) {
			log.log(level, type, msg);
			return;
		}
		// fallback to application
		if (!"application".equalsIgnoreCase(logName)) {
			log = ThreadLocalPageContext.getLog(config, "application");
			if (log != null) {
				log.log(level, type, msg);
				return;
			}
		}

		logGlobal(ThreadLocalPageContext.getConfigServer(config), level, logName + ":" + type, msg);

	}

	public static void log(PageContext pc, int level, String logName, String type, String msg) {
		Log log = ThreadLocalPageContext.getLog(pc, logName);
		if (log != null) log.log(level, type, msg);
		else {
			logGlobal(ThreadLocalPageContext.getConfigServer(pc), level, logName + ":" + type, msg);
		}
	}

	public static void logGlobal(int level, String type, String msg) {
		CFMLEngineFactory factory = null;
		try {
			factory = ConfigUtil.getCFMLEngineFactory();
		}
		catch (RuntimeException re) {
			aprint.e(levelToString(level, "unknown") + ":" + type + ":" + msg);
			return;
		}
		logGlobal(factory, level, type, msg);
	}

	public static void logGlobal(Config config, int level, String type, String msg) {
		CFMLEngineFactory factory = null;
		try {
			factory = ConfigUtil.getCFMLEngineFactory();
		}
		catch (RuntimeException re) {
			aprint.e(levelToString(level, "unknown") + ":" + type + ":" + msg);
			return;
		}
		logGlobal(factory, level, type, msg);
	}

	public static void logGlobal(CFMLEngineFactory factory, int level, String type, String msg) {

		if (level < MIN_LEVEL) return;

		try {
			Resource log;
			boolean check = false;
			if (level > Log.LEVEL_DEBUG) {
				if (ERR == null) {
					synchronized (factory) {
						if (ERR == null) {
							ERR = ResourceUtil.toResource(new File(factory.getResourceRoot(), "context/logs/err.log"));
							check = true;
						}
					}
				}
				log = ERR;
			}
			else {
				if (OUT == null) {
					synchronized (factory) {
						if (OUT == null) {
							OUT = ResourceUtil.toResource(new File(factory.getResourceRoot(), "context/logs/out.log"));
							check = true;
						}
					}
				}
				log = OUT;
			}
			if (check && !log.isFile()) {
				log.getParentResource().mkdirs();
				log.createNewFile();
			}
			IOUtil.write(log, FormatUtil.format(SystemOut.FORMAT, new Date(System.currentTimeMillis()), null) + " " + type + " " + msg + "\n", StandardCharsets.UTF_8, true);
		}
		catch (Exception e) {
			ERR = null;
			OUT = null;
			aprint.e(type + ":" + msg);
			if (factory != null) aprint.e(e); // in case there is no factory, we expect an error and are fine to simply write to the console
		}
	}

	public static void logGlobal(CFMLEngineFactory factory, String type, Throwable t) {
		logGlobal(factory, Log.LEVEL_ERROR, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void logGlobal(String type, Throwable t) {
		logGlobal(Log.LEVEL_ERROR, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void logGlobal(Config config, String type, Throwable t) {
		logGlobal(config, Log.LEVEL_ERROR, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void logGlobal(String type, String msg, Throwable t) {
		logGlobal(Log.LEVEL_ERROR, type, msg + ";" + ExceptionUtil.getStacktrace(t, true));
	}

	public static void logGlobal(Config config, String type, String msg, Throwable t) {
		logGlobal(config, Log.LEVEL_ERROR, type, msg + ";" + ExceptionUtil.getStacktrace(t, true));
	}

	public static boolean does(int logLevel) {
		return does(ThreadLocalPageContext.getLog("application"), logLevel);
	}

	public static boolean does(Log log, int logLevel) {
		return (log != null && log.getLogLevel() <= logLevel);
	}

	public static boolean doesTrace(Log log) {
		return (log != null && log.getLogLevel() <= Log.LEVEL_TRACE);
	}

	public static boolean doesInfo(Log log) {
		return (log != null && log.getLogLevel() <= Log.LEVEL_INFO);
	}

	public static boolean doesDebug(Log log) {
		return (log != null && log.getLogLevel() <= Log.LEVEL_DEBUG);
	}

	public static boolean doesWarn(Log log) {
		return (log != null && log.getLogLevel() <= Log.LEVEL_WARN);
	}

	public static boolean doesError(Log log) {
		return (log != null && log.getLogLevel() <= Log.LEVEL_ERROR);
	}

	public static boolean doesFatal(Log log) {
		return (log != null && log.getLogLevel() <= Log.LEVEL_FATAL);
	}

	public static String caller(PageContext pc, String defaultValue) {
		Exception t = new Exception("Stack trace");
		StackTraceElement[] traces = t.getStackTrace();

		String template;
		for (StackTraceElement trace: traces) {
			template = trace.getFileName();
			if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue;

			return abs(pc, template) + ":" + trace.getLineNumber();
		}
		return defaultValue;
	}

	private static String abs(PageContext pc, String template) {
		try {
			Config config = ThreadLocalPageContext.getConfig(pc);
			Resource res = config.getResource(template);
			if (res.exists()) return template;
			String tmp;
			PageSource ps = pc == null ? null : ((PageContextImpl) pc).getPageSource(template);
			res = ps == null ? null : ps.getPhyscalFile();
			if (res == null || !res.exists()) {
				tmp = ps.getDisplayPath();
				res = StringUtil.isEmpty(tmp) ? null : config.getResource(tmp);
				if (res != null && res.exists()) return res.getAbsolutePath();
			}
			else return res.getAbsolutePath();
		}
		catch (Exception e) {}
		return template;
	}

	public static Log getLog(Config c, String... logNames) {
		Log log = null;
		for (String ln: logNames) {
			log = ThreadLocalPageContext.getLog(c, ln, false);
			if (log != null) return log;
		}
		return null;
	}
}