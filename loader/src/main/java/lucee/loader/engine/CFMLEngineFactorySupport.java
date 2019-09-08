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
package lucee.loader.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.osgi.framework.Version;

import lucee.loader.TP;

public abstract class CFMLEngineFactorySupport {
	private static File tempFile;
	private static File homeFile;

	/**
	 * copy an inputstream to an outputstream
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public final static void copy(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buffer = new byte[0xffff];
		int len;
		try {
			while ((len = in.read(buffer)) != -1)
				out.write(buffer, 0, len);
		}
		finally {
			closeEL(in);
			closeEL(out);
		}
	}

	/**
	 * close inputstream without an Exception
	 * 
	 * @param is
	 */
	public final static void closeEL(final InputStream is) {
		try {
			if (is != null) is.close();
		}
		catch (final Throwable e) {}
	}

	/**
	 * close outputstream without an Exception
	 * 
	 * @param os
	 */
	public final static void closeEL(final OutputStream os) {
		try {
			if (os != null) os.close();
		}
		catch (final Throwable e) {}
	}

	/**
	 * read String data from an InputStream and returns it as String Object
	 * 
	 * @param is InputStream to read data from.
	 * @return readed data from InputStream
	 * @throws IOException
	 */
	public static String toString(final InputStream is) throws IOException {
		final BufferedReader br = new BufferedReader(new InputStreamReader(is));
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

	/**
	 * cast a lucee string version to an int version
	 * 
	 * @param version
	 * @return int version
	 */
	public static Version toVersion(String version, final Version defaultValue) {
		// remove extension if there is any
		final int rIndex = version.lastIndexOf(".lco");
		if (rIndex != -1) version = version.substring(0, rIndex);

		try {
			return Version.parseVersion(version);
		}
		catch (final IllegalArgumentException iae) {
			return defaultValue;
		}
	}

	public static String removeQuotes(String str, final boolean trim) {
		if (str == null) return str;
		if (trim) str = str.trim();
		if (str.length() < 2) return str;

		final char first = str.charAt(0);
		final char last = str.charAt(str.length() - 1);

		if ((first == '"' || first == '\'') && first == last) return str.substring(1, str.length() - 1);

		return str;
	}

	/**
	 * replace path placeholder with the real path, placeholders are
	 * [{temp-directory},{system-directory},{home-directory}]
	 * 
	 * @param path
	 * @return updated path
	 */
	public static String parsePlaceHolder(String path) {
		if (path == null) return path;
		// Temp
		if (path.startsWith("{temp")) {
			if (path.startsWith("}", 5)) path = new File(getTempDirectory(), path.substring(6)).toString();
			else if (path.startsWith("-dir}", 5)) path = new File(getTempDirectory(), path.substring(10)).toString();
			else if (path.startsWith("-directory}", 5)) path = new File(getTempDirectory(), path.substring(16)).toString();
		}
		// System
		else if (path.startsWith("{system")) {
			if (path.charAt(7) == ':') {
				// now we read the properties name
				int end = path.indexOf('}', 8);
				if (end > 8) {
					String name = path.substring(8, end);
					String prop = System.getProperty(name);
					if (prop != null) return new File(new File(prop), path.substring(end + 1)).getAbsolutePath();
				}
			}
			else if (path.startsWith("}", 7)) path = new File(getSystemDirectory(), path.substring(8)).toString();
			else if (path.startsWith("-dir}", 7)) path = new File(getSystemDirectory(), path.substring(12)).toString();
			else if (path.startsWith("-directory}", 7)) path = new File(getSystemDirectory(), path.substring(18)).toString();
		}

		// env
		else if (path.startsWith("{env:")) {
			// now we read the properties name
			int end = path.indexOf('}', 5);
			if (end > 5) {
				String name = path.substring(5, end);
				String env = System.getenv(name);
				if (env != null) return new File(new File(env), path.substring(end + 1)).getAbsolutePath();
			}
		}

		// Home
		else if (path.startsWith("{home")) {
			if (path.startsWith("}", 5)) path = new File(getHomeDirectory(), path.substring(6)).toString();
			else if (path.startsWith("-dir}", 5)) path = new File(getHomeDirectory(), path.substring(10)).toString();
			else if (path.startsWith("-directory}", 5)) path = new File(getHomeDirectory(), path.substring(16)).toString();
		}
		// ClassLoaderDir
		if (path.startsWith("{classloader")) {
			if (path.startsWith("}", 12)) path = new File(getClassLoaderDirectory(), path.substring(13)).toString();
			else if (path.startsWith("-dir}", 12)) path = new File(getClassLoaderDirectory(), path.substring(17)).toString();
			else if (path.startsWith("-directory}", 12)) path = new File(getClassLoaderDirectory(), path.substring(23)).toString();
		}

		return path;
	}

	public static File getHomeDirectory() {
		if (homeFile != null) return homeFile;

		final String homeStr = System.getProperty("user.home");
		if (homeStr != null) {
			homeFile = new File(homeStr);
			homeFile = getCanonicalFileEL(homeFile);
		}
		return homeFile;
	}

	public static File getClassLoaderDirectory() {
		return CFMLEngineFactory.getClassLoaderRoot(TP.class.getClassLoader());
	}

	/**
	 * returns the Temp Directory of the System
	 * 
	 * @return temp directory
	 */
	public static File getTempDirectory() {
		if (tempFile != null) return tempFile;

		final String tmpStr = System.getProperty("java.io.tmpdir");
		if (tmpStr != null) {
			tempFile = new File(tmpStr);
			if (tempFile.exists()) {
				tempFile = getCanonicalFileEL(tempFile);
				return tempFile;
			}
		}
		try {
			final File tmp = File.createTempFile("a", "a");
			tempFile = tmp.getParentFile();
			tempFile = getCanonicalFileEL(tempFile);
			tmp.delete();
		}
		catch (final IOException ioe) {}

		return tempFile;
	}

	/**
	 * @return return System directory
	 */
	public static File getSystemDirectory() {
		final String pathes = System.getProperty("java.library.path");
		if (pathes != null) {
			final String[] arr = pathes.split(File.pathSeparator);
			// String[] arr=List.toStringArrayEL(List.listToArray(pathes,File.pathSeparatorChar));
			for (final String element: arr)
				if (element.toLowerCase().indexOf("windows\\system") != -1) {
					final File file = new File(element);
					if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file);

				}
			for (final String element: arr)
				if (element.toLowerCase().indexOf("windows") != -1) {
					final File file = new File(element);
					if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file);

				}
			for (final String element: arr)
				if (element.toLowerCase().indexOf("winnt") != -1) {
					final File file = new File(element);
					if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file);

				}
			for (final String element: arr)
				if (element.toLowerCase().indexOf("win") != -1) {
					final File file = new File(element);
					if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file);

				}
			for (final String element: arr) {
				final File file = new File(element);
				if (file.exists() && file.isDirectory() && file.canWrite()) return getCanonicalFileEL(file);
			}
		}
		return null;
	}

	private static File getCanonicalFileEL(final File file) {
		try {
			return file.getCanonicalFile();
		}
		catch (final IOException e) {
			return file;
		}
	}
}