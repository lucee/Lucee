/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.loader.osgi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

public class LoggerImpl extends Logger {

	private final File logFile;

	public LoggerImpl(final File logFile) {
		this.logFile = logFile;
		setLogLevel(LOG_ERROR);
		if (!logFile.exists()) try {
			logFile.createNewFile();
		}
		catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void doLog(final Bundle bundle, final ServiceReference sr, final int level, final String msg, Throwable throwable) {
		if (level > getLogLevel()) return;
		String s = "";
		if (sr != null) s = s + "SvcRef " + sr + " ";
		else if (bundle != null) s = s + "Bundle " + bundle.toString() + " ";
		s = s + msg;

		// throwable
		if (throwable != null) {
			if ((throwable instanceof BundleException) && (((BundleException) throwable).getNestedException() != null))
				throwable = ((BundleException) throwable).getNestedException();
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);

			throwable.printStackTrace(pw);
			s += "\n" + sw.getBuffer();
		}

		_log(level, s);
	}

	@Override
	protected void _log(Bundle bundle, ServiceReference sr, int level, String msg, Throwable throwable) {
		_log(level, msg);
	}

	private void _log(final int level, final String msg) {
		if (level > getLogLevel()) return;

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
			bw.write(toLevel(level) + " [" + new Date() + "]:\n" + msg + "\n");
			bw.flush();
		}
		catch (final IOException ioe) {}
		finally {
			if (bw != null) try {
				bw.close();
			}
			catch (final IOException e) {}
		}
	}

	private String toLevel(int level) {
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
			return "UNKNOWNN[" + level + "]";
		}
	}

	private int toLevel(String level) {
		if (level != null) {
			if ("DEBUG".equalsIgnoreCase(level)) return LOG_DEBUG;
			if ("ERROR".equalsIgnoreCase(level)) return LOG_ERROR;
			if ("INFO".equalsIgnoreCase(level)) return LOG_INFO;
			if ("WARNING".equalsIgnoreCase(level)) return LOG_WARNING;
		}
		return LOG_ERROR;
	}

}