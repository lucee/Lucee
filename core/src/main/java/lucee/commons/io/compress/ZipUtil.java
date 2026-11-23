/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.commons.io.compress;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lucee.commons.cli.Command;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;

/**
 * Helpers for reading archive entry names and resolving them under a target directory.
 */
public final class ZipUtil {

	private static final Charset IBM437 = loadIbm437();

	private static Charset loadIbm437() {
		for (String name: new String[] { "IBM437", "Cp437", "IBM-437" }) {
			try {
				return Charset.forName(name);
			}
			catch (Exception e) {
			}
		}
		return StandardCharsets.ISO_8859_1;
	}

	public static void unzip(Resource zip, Resource dir) throws IOException {
		if (zip != null && zip.length() > 0 && (dir.exists() || dir.mkdirs())) {
			if ("Mac OS X".equalsIgnoreCase(System.getProperty("os.name"))) {
				try {
					// Command.execute("unzip "+zip+" -d "+dir);
					Command.execute("unzip", new String[] { "-o", zip.getAbsolutePath(), "-d", dir.getAbsolutePath() });
				}
				catch (InterruptedException e) {
					LogUtil.warn("zip", e);
				}
				return;
			}
			CompressUtil.extract(CompressUtil.FORMAT_ZIP, zip, dir);
		}
	}

	public static void close(ZipOutputStream zos) {
		if (zos == null) return;
		try {
			zos.close();
		}
		catch (IOException e) {
		}
	}

	public static void close(ZipFile file) {
		if (file == null) return;
		try {
			file.close();
		}
		catch (IOException e) {
		}
	}

	public static Resource toResource(Resource targetDir, ZipEntry entry) throws IOException {
		return toResource(targetDir, entry.getName());
	}

	public static Resource toResource(Resource targetDir, String fileName) throws IOException {
		return toResource(targetDir, canonicalDirPrefix(targetDir), fileName);
	}

	/**
	 * Canonical path of the target directory, with a trailing separator. Compute this once per
	 * extraction and pass it to {@link #toResource(Resource, String, String)}.
	 */
	public static String canonicalDirPrefix(Resource dir) throws IOException {
		if (dir == null) throw new IOException("target directory is required");
		String path = dir.getCanonicalPath();
		if (path == null || path.isEmpty()) throw new IOException("invalid target directory");
		char last = path.charAt(path.length() - 1);
		if (last == '/' || last == '\\') return path;
		char sep = (path.indexOf('\\') >= 0 && path.indexOf('/') < 0) ? '\\' : '/';
		return path + sep;
	}

	/**
	 * Resolve an archive entry name under the target directory.
	 * <p>
	 * Inspired by {@link lucee.commons.io.res.util.ResourceUtil#isChildOf}: same scheme check and
	 * path prefix comparison, but the directory path is compared with a trailing separator so a
	 * sibling of the target (for example {@code target2} next to {@code target}) is not treated as
	 * being inside it.
	 *
	 * @param targetDir target directory
	 * @param targetPrefix value of {@link #canonicalDirPrefix(Resource)} for {@code targetDir}
	 * @param fileName entry name from the archive
	 * @return the resolved resource inside {@code targetDir}
	 * @throws IOException if the name is empty, contains a NUL, is absolute, or does not resolve
	 *             inside {@code targetDir}
	 */
	public static Resource toResource(Resource targetDir, String targetPrefix, String fileName) throws IOException {
		if (fileName == null || fileName.isEmpty() || fileName.indexOf('\0') != -1) {
			throw new IOException("invalid archive entry [" + fileName + "]");
		}

		String name = fileName.replace('\\', '/');
		if (isAbsoluteName(name)) {
			throw new IOException("archive entry [" + fileName + "] is outside the target directory");
		}

		Resource target = targetDir.getRealResource(name);
		if (!isInsideTarget(targetDir, targetPrefix, target)) {
			throw new IOException("archive entry [" + fileName + "] is outside the target directory");
		}
		return target;
	}

	/**
	 * Choose a charset for reading zip entry names. Names are first decoded as UTF-8 (the
	 * language-encoding flag is always honoured). If a name is not valid UTF-8, IBM437/Cp437 is used
	 * instead, which is the default encoding in the ZIP specification.
	 */
	public static Charset charsetForZip(Resource zip) throws IOException {
		if (zip == null) return StandardCharsets.UTF_8;
		File file = toLocalFile(zip);
		if (file != null) return charsetForZip(file);
		return charsetForZipStream(zip);
	}

	public static Charset charsetForZip(File zip) throws IOException {
		if (zip == null || !zip.isFile()) return StandardCharsets.UTF_8;
		ZipFile zf = null;
		try {
			zf = new ZipFile(zip, StandardCharsets.UTF_8);
			Enumeration<? extends ZipEntry> en = zf.entries();
			while (en.hasMoreElements()) {
				en.nextElement();
			}
			return StandardCharsets.UTF_8;
		}
		catch (IllegalArgumentException e) {
			if (isMalformedName(e)) return IBM437;
			return StandardCharsets.UTF_8;
		}
		catch (java.util.zip.ZipException e) {
			if (isMalformedName(e)) return IBM437;
			return StandardCharsets.UTF_8;
		}
		finally {
			if (zf != null) {
				try {
					zf.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

	public static boolean isMalformedName(Throwable e) {
		while (e != null) {
			if (e instanceof MalformedInputException) return true;
			String msg = e.getMessage();
			if (msg != null) {
				String u = msg.toUpperCase();
				// ZipInputStream: "MALFORMED" / "malformed input off : ..."
				// ZipFile: "invalid CEN header (bad entry name or comment)"
				if (u.indexOf("MALFORMED") != -1 || u.indexOf("BAD ENTRY NAME") != -1) return true;
			}
			e = e.getCause();
		}
		return false;
	}

	private static Charset charsetForZipStream(Resource zip) throws IOException {
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(IOUtil.toBufferedInputStream(zip.getInputStream()), StandardCharsets.UTF_8);
			while (zis.getNextEntry() != null) {
				zis.closeEntry();
			}
			return StandardCharsets.UTF_8;
		}
		catch (IllegalArgumentException e) {
			if (isMalformedName(e)) return IBM437;
			return StandardCharsets.UTF_8;
		}
		catch (java.util.zip.ZipException e) {
			if (isMalformedName(e)) return IBM437;
			return StandardCharsets.UTF_8;
		}
		finally {
			IOUtil.close(zis);
		}
	}

	private static boolean isInsideTarget(Resource targetDir, String targetPrefix, Resource candidate) throws IOException {
		if (targetDir != null && candidate.getResourceProvider() != null && targetDir.getResourceProvider() != null
				&& !candidate.getResourceProvider().getScheme().equals(targetDir.getResourceProvider().getScheme())) {
			return false;
		}
		String canon = candidate.getCanonicalPath();
		if (canon == null) return false;
		String dirCanon = targetPrefix;
		char last = dirCanon.charAt(dirCanon.length() - 1);
		if (last == '/' || last == '\\') dirCanon = dirCanon.substring(0, dirCanon.length() - 1);
		return canon.equals(dirCanon) || canon.startsWith(targetPrefix);
	}

	private static boolean isAbsoluteName(String name) {
		if (name.startsWith("/")) return true;
		return name.length() >= 2 && name.charAt(1) == ':' && Character.isLetter(name.charAt(0));
	}

	private static File toLocalFile(Resource zip) {
		try {
			if (zip.getResourceProvider() != null && !"file".equalsIgnoreCase(zip.getResourceProvider().getScheme())) return null;
			File f = new File(zip.getCanonicalPath());
			return f.isFile() ? f : null;
		}
		catch (Exception e) {
			return null;
		}
	}
}
