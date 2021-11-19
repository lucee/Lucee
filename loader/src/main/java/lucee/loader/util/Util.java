/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.loader.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipFile;

import org.osgi.framework.Version;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

/**
 * Util class for different little jobs
 */
public class Util {

	private static File tempFile;
	// private static File homeFile;

	private static final int QUALIFIER_APPENDIX_SNAPSHOT = 1;
	private static final int QUALIFIER_APPENDIX_BETA = 2;
	private static final int QUALIFIER_APPENDIX_RC = 3;
	private static final int QUALIFIER_APPENDIX_OTHER = 4;
	private static final int QUALIFIER_APPENDIX_STABLE = 5;

	private final static SimpleDateFormat HTTP_TIME_STRING_FORMAT;
	static {
		HTTP_TIME_STRING_FORMAT = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH);
		HTTP_TIME_STRING_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	@Deprecated
	public final static void copy(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buffer = new byte[0xffff];
		int len;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);

		closeEL(in);
		closeEL(out);
	}

	public final static void copy(final InputStream in, final OutputStream out, final boolean closeIS, final boolean closeOS) throws IOException {
		final byte[] buffer = new byte[0xffff];
		int len;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);

		if (closeIS) closeEL(in);
		if (closeOS) closeEL(out);
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance.getIOUtil().toString (InputStream is,
	 *             Charset cs) read String data from an InputStream and returns it as String Object
	 * @param is InputStream to read data from.
	 * @return readed data from InputStream
	 * @throws IOException in case it is not possible to convert to a string
	 */
	@Deprecated
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

	@Deprecated
	public static boolean toBooleanValue(String str) throws IOException {
		str = str == null ? null : str.trim().toLowerCase();

		if ("true".equals(str)) return true;
		if ("false".equals(str)) return false;
		if ("yes".equals(str)) return true;
		if ("no".equals(str)) return false;
		throw new IOException("can't cast string to a boolean value");
	}

	@Deprecated
	public static void closeEL(final InputStream is, final OutputStream os) {
		closeEL(is);
		closeEL(os);
	}

	@Deprecated
	public static void closeEL(final ZipFile zf) {
		try {
			if (zf != null) zf.close();
		}
		catch (final Throwable e) {}
	}

	@Deprecated
	public static void closeEL(final InputStream is) {
		try {
			if (is != null) is.close();
		}
		catch (final Throwable e) {}
	}

	@Deprecated
	public static void closeEL(final Reader r) {
		try {
			if (r != null) r.close();
		}
		catch (final Throwable e) {}
	}

	@Deprecated
	public static void closeEL(final Writer w) {
		try {
			if (w != null) w.close();
		}
		catch (final Throwable e) {}
	}

	@Deprecated
	public static void closeEL(final OutputStream os) {
		try {
			if (os != null) os.close();
		}
		catch (final Throwable e) {}
	}

	@Deprecated
	public static String getContentAsString(final InputStream is, final String charset) throws IOException, PageException {
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

	/**
	 * check if string is empty (null or "")
	 *
	 * @param str input string
	 * @return is empty or not
	 */
	public static boolean isEmpty(final String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * check if string is empty (null or "")
	 * 
	 * @param str input string
	 * @param trim trim it
	 * @return is empty or not
	 */
	public static boolean isEmpty(final String str, final boolean trim) {
		if (!trim) return isEmpty(str);
		return str == null || str.trim().length() == 0;
	}

	/**
	 * @deprecated no replacement
	 * @param str input string
	 */
	@Deprecated
	public static int length(final String str) {
		if (str == null) return 0;
		return str.length();
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance().getStringUtil().replace(...)
	 * @param str String to work with
	 * @param sub1 value to replace
	 * @param sub2 replacement
	 * @param onlyFirst replace only first or all
	 * @return new String
	 */
	@Deprecated
	public static String replace(final String str, final String sub1, final String sub2, final boolean onlyFirst) {
		if (sub1.equals(sub2)) return str;

		if (!onlyFirst && sub1.length() == 1 && sub2.length() == 1) return str.replace(sub1.charAt(0), sub2.charAt(0));

		final StringBuffer sb = new StringBuffer();
		int start = 0;
		int pos;
		final int sub1Length = sub1.length();

		while ((pos = str.indexOf(sub1, start)) != -1) {
			sb.append(str.substring(start, pos));
			sb.append(sub2);
			start = pos + sub1Length;
			if (onlyFirst) break;
		}
		sb.append(str.substring(start));

		return sb.toString();
	}

	/**
	 * @param path path to parse
	 * @return updated path
	 */
	@Deprecated
	public static String parsePlaceHolder(final String path) {
		return CFMLEngineFactory.getInstance().getResourceUtil().parsePlaceHolder(path);
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance().getResourceUtil(). getTempDirectory()
	 *             returns the Temp Directory of the System
	 * @return temp directory
	 */
	@Deprecated
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
	 * @deprecated use instead CFMLEngineFactory.getInstance().getResourceUtil(). getHomeDirectory()
	 *             returns the Home Directory of the System
	 * @return home directory
	 */
	@Deprecated
	public static File getHomeDirectory() {
		return (File) CFMLEngineFactory.getInstance().getResourceUtil().getHomeDirectory();
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance().getResourceUtil(). getSystemDirectory()
	 * @return return System directory
	 */
	@Deprecated
	public static File getSystemDirectory() {
		return (File) CFMLEngineFactory.getInstance().getResourceUtil().getSystemDirectory();
	}

	/**
	 * 
	 * @param file file to get canonical form from it
	 *
	 * @return The canonical pathname string denoting the same file or directory as this abstract
	 *         pathname
	 */
	@Deprecated
	public static File getCanonicalFileEL(final File file) {
		try {
			return file.getCanonicalFile();
		}
		catch (final IOException e) {
			return file;
		}
	}

	@Deprecated
	public static String toHTTPTimeString(final Date date) {
		return replace(HTTP_TIME_STRING_FORMAT.format(date), "+00:00", "", true);
	}

	@Deprecated
	public static String toHTTPTimeString() {
		return replace(HTTP_TIME_STRING_FORMAT.format(new Date()), "+00:00", "", true);
	}

	@Deprecated
	public static boolean hasUpperCase(final String str) {
		if (isEmpty(str)) return false;
		return !str.equals(str.toLowerCase());
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance().getIOUtil(). toBufferedInputStream (...)
	 * @param is input stream
	 * @return buffered output stream
	 */
	@Deprecated
	public static BufferedInputStream toBufferedInputStream(final InputStream is) {
		if (is instanceof BufferedInputStream) return (BufferedInputStream) is;
		return new BufferedInputStream(is);
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance().getIOUtil(). toBufferedOutputStream (...)
	 * @param os output steam to buffer
	 * @return buffered output stream
	 */
	@Deprecated
	public static BufferedOutputStream toBufferedOutputStream(final OutputStream os) {
		if (os instanceof BufferedOutputStream) return (BufferedOutputStream) os;
		return new BufferedOutputStream(os);
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance.getIOUtil().copy(...)
	 * @param in input resource
	 * @param out output resource
	 * @throws IOException exception thrown in case copy fails
	 */
	@Deprecated
	public static void copy(final Resource in, final Resource out) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = toBufferedInputStream(in.getInputStream());
			os = toBufferedOutputStream(out.getOutputStream());
		}
		catch (final IOException ioe) {
			closeEL(os);
			closeEL(is);
			throw ioe;
		}
		copy(is, os);
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance().getStringUtil(). toVariableName (...)
	 * @param str input string
	 * @param addIdentityNumber add identity number or not
	 */
	@Deprecated
	public static String toVariableName(final String str, final boolean addIdentityNumber) {
		return CFMLEngineFactory.getInstance().getStringUtil().toVariableName(str, addIdentityNumber, false);
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance().getStringUtil().first(...);
	 * @param str input string
	 * @param delimiter delimiter to split
	 */
	@Deprecated
	public static String first(final String str, final String delimiter) {
		return CFMLEngineFactory.getInstance().getStringUtil().first(str, delimiter, true);
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance().getStringUtil().last(...);
	 * @param str input string
	 * @param delimiter delimiter to split
	 */
	@Deprecated
	public static String last(final String str, final String delimiter) {
		return CFMLEngineFactory.getInstance().getStringUtil().last(str, delimiter, true);
	}

	/**
	 * @deprecated use instead CFMLEngineFactory.getInstance().getStringUtil().removeQuotes (...);
	 * @param str string to removes quotes from
	 * @param trim trim it or not
	 */
	@Deprecated
	public static String removeQuotes(final String str, final boolean trim) {
		return CFMLEngineFactory.getInstance().getStringUtil().removeQuotes(str, trim);
	}

	public static void delete(final File f) {
		if (f.isDirectory()) for (final File c: f.listFiles())
			delete(c);
		f.delete();
	}

	/**
	 * check left value against right value
	 * 
	 * @param left left operand
	 * @param right right operand
	 * @return returns if right is newer than left
	 */
	public static boolean isNewerThan(final Version left, final Version right) {

		// major
		if (left.getMajor() > right.getMajor()) return true;
		if (left.getMajor() < right.getMajor()) return false;

		// minor
		if (left.getMinor() > right.getMinor()) return true;
		if (left.getMinor() < right.getMinor()) return false;

		// micro
		if (left.getMicro() > right.getMicro()) return true;
		if (left.getMicro() < right.getMicro()) return false;

		// qualifier
		// left
		String q = left.getQualifier();
		int index = q.indexOf('-');
		String qla = index == -1 ? "" : q.substring(index + 1).trim();
		String qln = index == -1 ? q : q.substring(0, index);
		int ql = isEmpty(qln) ? Integer.MIN_VALUE : Integer.parseInt(qln);

		// right
		q = right.getQualifier();
		index = q.indexOf('-');
		String qra = index == -1 ? "" : q.substring(index + 1).trim();
		String qrn = index == -1 ? q : q.substring(0, index);
		int qr = isEmpty(qln) ? Integer.MIN_VALUE : Integer.parseInt(qrn);

		if (ql > qr) return true;
		if (ql < qr) return false;

		int qlan = qualifierAppendix2Number(qla);
		int qran = qualifierAppendix2Number(qra);

		if (qlan > qran) return true;
		if (qlan < qran) return false;

		if (qlan == QUALIFIER_APPENDIX_OTHER && qran == QUALIFIER_APPENDIX_OTHER) return left.compareTo(right) > 0;

		return false;
	}

	private static int qualifierAppendix2Number(String str) {
		if (Util.isEmpty(str, true)) return QUALIFIER_APPENDIX_STABLE;
		if ("SNAPSHOT".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_SNAPSHOT;
		if ("BETA".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_BETA;
		if ("RC".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_RC;
		return QUALIFIER_APPENDIX_OTHER;
	}

	public static void deleteContent(Resource src, ResourceFilter filter) {
		_deleteContent(src, filter, false);
	}

	public static void _deleteContent(Resource src, ResourceFilter filter, boolean deleteDirectories) {
		if (src.isDirectory()) {
			Resource[] files = filter == null ? src.listResources() : src.listResources(filter);
			for (int i = 0; i < files.length; i++) {
				_deleteContent(files[i], filter, true);
				if (deleteDirectories) {
					try {
						src.remove(false);
					}
					catch (IOException e) {}
				}
			}

		}
		else if (src.isFile()) {
			src.delete();
		}
	}

	public static void deleteContent(File src, FileFilter filter) {
		_deleteContent(src, filter, false);
	}

	public static void _deleteContent(File src, FileFilter filter, boolean deleteDirectories) {
		if (src.isDirectory()) {
			File[] files = filter == null ? src.listFiles() : src.listFiles(filter);
			for (int i = 0; i < files.length; i++) {
				_deleteContent(files[i], filter, true);
				if (deleteDirectories) {
					src.delete();
				}
			}
		}
		else if (src.isFile()) {
			src.delete();
		}
	}

	/**
	 * returns a system setting by either a Java property name or a System environment variable
	 * 
	 * @param name - either a lowercased Java property name (e.g. lucee.controller.disabled) or an
	 *            UPPERCASED Environment variable name ((e.g. LUCEE_CONTROLLER_DISABLED))
	 * @param defaultValue - value to return if the neither the property nor the environment setting was
	 *            found
	 * @return - the value of the property referenced by propOrEnv or the defaultValue if not found
	 */
	public static String _getSystemPropOrEnvVar(String name, String defaultValue) { // FUTURE remove _ in front of the name
		// env
		String value = System.getenv(name);
		if (!Util.isEmpty(value)) return value;

		// prop
		value = System.getProperty(name);
		if (!Util.isEmpty(value)) return value;

		// env 2
		name = name.replace('.', '_').toUpperCase();
		value = System.getenv(name);
		if (!Util.isEmpty(value)) return value;

		return defaultValue;
	}
}