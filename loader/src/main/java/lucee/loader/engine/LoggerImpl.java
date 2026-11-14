/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
 */
package lucee.loader.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * Writes the log of the loader (the startup of Lucee) to a file.
 */
public class LoggerImpl {

	public static final int LOG_ERROR = 1;
	public static final int LOG_WARNING = 2;
	public static final int LOG_INFO = 3;
	public static final int LOG_DEBUG = 4;

	private final File logFile;
	private int logLevel;

	public LoggerImpl(final File logFile, int logLevel) {
		this.logFile = logFile;
		this.logLevel = logLevel;
		if (!logFile.exists()) try {
			logFile.createNewFile();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public int getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	public void log(final int level, final String msg) {
		log(level, msg, null);
	}

	public void log(final int level, final String msg, final Throwable throwable) {
		if (level > logLevel) return;
		String s = msg == null ? "" : msg;
		if (throwable != null) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			s += "\n" + sw.getBuffer();
		}
		write(level, s);
	}

	private void write(final int level, final String msg) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
			bw.write(toLevel(level, "ERROR") + " [" + new Date() + "]:\n" + msg + "\n");
			bw.flush();
		}
		catch (final IOException ioe) {
		}
		finally {
			if (bw != null) try {
				bw.close();
			}
			catch (final IOException e) {
			}
		}
	}

	public static String toLevel(int level, String defaultValue) {
		switch (level) {
		case LOG_DEBUG:
			return "DEBUG";
		case LOG_ERROR:
			return "ERROR";
		case LOG_INFO:
			return "INFO";
		case LOG_WARNING:
			return "WARNING";
		default:
			return defaultValue;
		}
	}

	public static int toLevel(String level, int defaultValue) {
		if (level != null) {
			// the numeric levels of the former Felix log level setting
			if ("1".equals(level.trim())) return LOG_ERROR;
			if ("2".equals(level.trim())) return LOG_WARNING;
			if ("3".equals(level.trim())) return LOG_INFO;
			if ("4".equals(level.trim())) return LOG_DEBUG;
			if ("DEBUG".equalsIgnoreCase(level)) return LOG_DEBUG;
			if ("ERROR".equalsIgnoreCase(level)) return LOG_ERROR;
			if ("INFO".equalsIgnoreCase(level)) return LOG_INFO;
			if ("WARNING".equalsIgnoreCase(level)) return LOG_WARNING;
			if ("WARN".equalsIgnoreCase(level)) return LOG_WARNING;
		}
		return defaultValue;
	}

}
