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
package lucee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import lucee.loader.TP;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngineFactorySupport;

/**
 * returns th current built in version
 */
public class VersionInfo {

	private static org.osgi.framework.Version version = null;
	private static long created = -1;

	/**
	 * @return returns the current version
	 */
	public static org.osgi.framework.Version getIntVersion() {
		init();
		return version;
	}

	/**
	 * return creation time of this version
	 * 
	 * @return creation time
	 */
	public static long getCreateTime() {
		init();
		return created;
	}

	private static void init() {
		if (version != null) return;
		String content = "9000000:" + System.currentTimeMillis();
		try {
			InputStream is = new TP().getClass().getClassLoader().getResourceAsStream("lucee/version");
			if (is != null) {
				content = getContentAsString(is, "UTF-8");
			}
			else {
				System.err.println("lucee/version not found");
			}
		}
		catch (final IOException e) {}

		final int index = content.indexOf(':');
		version = CFMLEngineFactorySupport.toVersion(content.substring(0, index), CFMLEngineFactory.VERSION_ZERO);
		final String d = content.substring(index + 1);
		try {
			created = Long.parseLong(d);
		}
		catch (final NumberFormatException nfe) {
			try {
				created = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").parse(d).getTime();
			}
			catch (final ParseException pe) {
				pe.printStackTrace();
				created = 0;
			}
		}

	}

	private static String getContentAsString(final InputStream is, final String charset) throws IOException {

		final BufferedReader br = (charset == null) ? new BufferedReader(new InputStreamReader(is)) : new BufferedReader(new InputStreamReader(is, charset));
		final StringBuffer content = new StringBuffer();

		String line = br.readLine();
		if (line != null) {
			content.append(line);
			while ((line = br.readLine()) != null)
				content.append("\n" + line);
		}
		br.close();
		return content.toString();
	}
}