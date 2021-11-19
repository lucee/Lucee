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
import java.io.IOException;
import java.util.Date;

import lucee.aprint;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SystemOut;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;
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
			for (StackTraceElement ste: stes) {
				if (ste.getClassName().indexOf("org.apache.log4j.") == 0) return true;
			}
		}
		return false;
	}

	public static void log(Config config, int level, String type, String msg) {
		log(config, level, "application", type, msg);
	}

	public static void log(Config config, String type, Throwable t) {
		log(config, "application", type, t);
	}

	public static void log(Config config, String logName, String type, Throwable t) {
		log(config, logName, type, t, Log.LEVEL_ERROR);
	}

	public static void log(Config config, String logName, String type, Throwable t, int logLevel) {
		config = ThreadLocalPageContext.getConfig(config);
		Log log = null;
		if (config != null) {
			log = config.getLog(logName);
		}

		if (log != null) {
			if (Log.LEVEL_ERROR == logLevel) log.error(type, t);
			else log.log(logLevel, type, t);
		}
		else logGlobal(config, logLevel, type, ExceptionUtil.getStacktrace(t, true));
	}

	public static void log(Config config, int level, String logName, String type, String msg) {
		config = ThreadLocalPageContext.getConfig(config);
		Log log = null;
		if (config != null) {
			log = config.getLog(logName);
		}

		if (log != null) log.log(level, type, msg);
		else {
			logGlobal(config, level, logName + ":" + type, msg);
			// if (config == null) SystemOut.printDate(msg);
			// else if (level == Log.LEVEL_ERROR || level == Log.LEVEL_FATAL)
			// SystemOut.printDate(config.getErrWriter(), msg);
			// else SystemOut.printDate(config.getOutWriter(), msg);
		}
	}

	public static void logGlobal(Config config, int level, String type, String msg) {
		try {
			CFMLEngine engine = ConfigWebUtil.getEngine(config);
			File root = engine.getCFMLEngineFactory().getResourceRoot();
			File flog = new File(root, "context/logs/" + (level > Log.LEVEL_DEBUG ? "err" : "out") + ".log");
			Resource log = ResourceUtil.toResource(flog);
			if (!log.isFile()) {
				log.getParentResource().mkdirs();
				log.createNewFile();
			}
			IOUtil.write(log, SystemOut.FORMAT.format(new Date(System.currentTimeMillis())) + " " + type + " " + msg + "\n", CharsetUtil.UTF8, true);
		}
		catch (IOException ioe) {
			aprint.e(ioe);
		}
	}

	public static void logGlobal(Config config, String type, Throwable t) {
		logGlobal(config, Log.LEVEL_ERROR, type, ExceptionUtil.getStacktrace(t, true));
	}
}