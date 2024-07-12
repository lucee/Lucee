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

import java.io.File;
import java.util.Date;

import lucee.aprint;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
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
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.engine.ThreadLocalPageContext;

/**
 * Helper class for the logs
 */
public final class LogUtil {

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

	//////////
	public static void log(String type, Throwable t) {
		log("application", type, t);
	}

	public static void log(PageContext pc, String type, Throwable t) {
		log(pc, "application", type, t);
	}

	public static void log(Config config, String type, Throwable t) {
		log(config, "application", type, t, Log.LEVEL_ERROR);
	}

	//////////
	public static void log(String logName, String type, Throwable t) {
		log(logName, type, t, Log.LEVEL_ERROR);
	}

	public static void log(PageContext pc, String logName, String type, Throwable t) {
		log(pc, logName, type, t, Log.LEVEL_ERROR);
	}

	//////////
	public static void log(String logName, String type, Throwable t, int logLevel) {
		Log log = ThreadLocalPageContext.getLog(logName);
		if (log != null) {
			if (Log.LEVEL_ERROR == logLevel) log.error(type, t);
			else log.log(logLevel, type, t);
		}
		else logGlobal(ThreadLocalPageContext.getConfig(), logLevel, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void log(Config config, String logName, String type, Throwable t, int logLevel) {
		Log log = ThreadLocalPageContext.getLog(config, logName);
		if (log != null) {
			if (Log.LEVEL_ERROR == logLevel) log.error(type, t);
			else log.log(logLevel, type, t);
		}
		else logGlobal(config, logLevel, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void log(PageContext pc, String logName, String type, Throwable t, int logLevel) {
		Log log = ThreadLocalPageContext.getLog(pc, logName);
		if (log != null) {
			if (Log.LEVEL_ERROR == logLevel) log.error(type, t);
			else log.log(logLevel, type, t);
		}
		else logGlobal(ThreadLocalPageContext.getConfig(pc), logLevel, type, ExceptionUtil.getStacktrace(t, true));
	}

	//////////
	public static void log(int level, String logName, String type, String msg) {
		Log log = ThreadLocalPageContext.getLog(logName);
		if (log != null) log.log(level, type, msg);
		else {
			logGlobal(ThreadLocalPageContext.getConfig(), level, logName + ":" + type, msg);
		}
	}

	public static void log(Config config, int level, String logName, String type, String msg) {
		Log log = ThreadLocalPageContext.getLog(config, logName);
		if (log != null) log.log(level, type, msg);
		else {
			logGlobal(ThreadLocalPageContext.getConfig(config), level, logName + ":" + type, msg);
		}
	}

	public static void log(PageContext pc, int level, String logName, String type, String msg) {
		Log log = ThreadLocalPageContext.getLog(pc, logName);
		if (log != null) log.log(level, type, msg);
		else {
			logGlobal(ThreadLocalPageContext.getConfig(pc), level, logName + ":" + type, msg);
		}
	}

	public static void logGlobal(Config config, int level, String type, String msg) {
		try {
			CFMLEngineFactory factory = ConfigWebUtil.getCFMLEngineFactory(config);
			File root = factory.getResourceRoot();
			File flog = new File(root, "context/logs/" + (level > Log.LEVEL_DEBUG ? "err" : "out") + ".log");
			Resource log = ResourceUtil.toResource(flog);
			if (!log.isFile()) {
				log.getParentResource().mkdirs();
				log.createNewFile();
			}
			IOUtil.write(log, SystemOut.FORMAT.format(new Date(System.currentTimeMillis())) + " " + type + " " + msg + "\n", CharsetUtil.UTF8, true);
		}
		catch (Exception e) {
			aprint.e(type + ":" + msg);
			aprint.e(e);
		}
	}

	public static void logGlobal(Config config, String type, Throwable t) {
		logGlobal(config, Log.LEVEL_ERROR, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void logGlobal(Config config, String type, String msg, Throwable t) {
		logGlobal(config, Log.LEVEL_ERROR, type, msg + ";" + ExceptionUtil.getStacktrace(t, true));
	}

	public static boolean doesInfo(Log log) {
		return (log != null && log.getLogLevel() >= Log.LEVEL_INFO);
	}

	public static boolean doesDebug(Log log) {
		return (log != null && log.getLogLevel() >= Log.LEVEL_DEBUG);
	}

	public static boolean doesWarn(Log log) {
		return (log != null && log.getLogLevel() >= Log.LEVEL_WARN);
	}

	public static boolean doesError(Log log) {
		return (log != null && log.getLogLevel() >= Log.LEVEL_ERROR);
	}

	public static boolean doesFatal(Log log) {
		return (log != null && log.getLogLevel() >= Log.LEVEL_FATAL);
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
			ConfigWeb config = pc.getConfig();
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
		catch (Exception e) {
		}
		return template;
	}
}