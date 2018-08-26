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
		if (!logFile.exists())
			try {
				logFile.createNewFile();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void doLog(final Bundle bundle, final ServiceReference sr,
			final int level, final String msg, Throwable throwable) {
		String s = "";
		if (sr != null)
			s = s + "SvcRef " + sr + " ";
		else if (bundle != null)
			s = s + "Bundle " + bundle.toString() + " ";
		s = s + msg;

		// level
		String strLevel;
		switch (level) {
		case LOG_DEBUG:
			strLevel = "DEBUG";
			break;
		case LOG_ERROR:
			strLevel = "ERROR";
			break;
		case LOG_INFO:
			strLevel = "INFO";
			break;
		case LOG_WARNING:
			strLevel = "WARNING";
			break;
		default:
			strLevel = "UNKNOWNN[" + level + "]";
		}

		// throwable
		if (throwable != null) {
			if ((throwable instanceof BundleException)
					&& (((BundleException) throwable).getNestedException() != null))
				throwable = ((BundleException) throwable).getNestedException();
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);

			throwable.printStackTrace(pw);
			s += "\n" + sw.getBuffer();
		}

		_log(strLevel, s);
	}

	private void _log(final String level, final String msg) {
		// TODO better impl 
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(logFile, true)));
			bw.write(level + " [" + new Date() + "]:\n" + msg + "\n");
		}
		catch (final IOException ioe) {
			// System.out.println(level + " [" + new Date() + "]:\n" + msg + "\n");
		}
		finally {
			if (bw != null)
				try {
					bw.close();
				} catch (final IOException e) {
				}
		}

	}

}