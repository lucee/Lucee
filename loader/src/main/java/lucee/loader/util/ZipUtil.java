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
package lucee.loader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	public static void zip(final File src, final File trgZipFile) throws IOException {
		if (trgZipFile.isDirectory()) throw new IllegalArgumentException("argument trgZipFile is the name of an existing directory");

		final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(trgZipFile));
		try {
			if (src.isFile()) addEntries(zos, src.getParentFile(), src);
			else if (src.isDirectory()) addEntries(zos, src, src.listFiles());
		}
		finally {
			Util.closeEL(zos);
		}
	}

	private static void addEntries(final ZipOutputStream zos, final File root, final File... files) throws IOException {
		if (files != null) for (final File file: files) {

			// directory
			if (file.isDirectory()) {
				addEntries(zos, root, file.listFiles());
				continue;
			}
			if (!file.isFile()) continue;

			// file
			InputStream is = null;
			final ZipEntry ze = generateZipEntry(root, file);

			try {
				zos.putNextEntry(ze);
				copy(is = new FileInputStream(file), zos);
			}
			finally {
				closeEL(is);
				zos.closeEntry();
			}
		}
	}

	private static ZipEntry generateZipEntry(final File root, final File file) {
		final String strRoot = root.getAbsolutePath();
		final String strFile = file.getAbsolutePath();
		return new ZipEntry(strFile.substring(strRoot.length() + 1, strFile.length()));

	}

	private final static void copy(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buffer = new byte[0xffff];
		int len;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);
	}

	private static void closeEL(final InputStream is) {
		if (is == null) return;
		try {
			is.close();
		}
		catch (final Throwable t) {}
	}
}