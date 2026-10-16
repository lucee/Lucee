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
package lucee.commons.io.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.OrResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;

/**
 * Util to manipulate zip files
 */
public final class CompressUtil {

	/**
	 * Field <code>FORMAT_ZIP</code>
	 */
	public static final int FORMAT_ZIP = 0;

	/**
	 * Field <code>FORMAT_GZIP</code>
	 */
	public static final int FORMAT_GZIP = 3;

	/**
	 * Constructor of the class
	 */
	private CompressUtil() {
	}

	/**
	 * extract a zip file to a directory
	 * 
	 * @param format
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public static void extract(int format, Resource source, Resource target) throws IOException {
		if (format == FORMAT_ZIP) extractZip(source, target);
		else if (format == FORMAT_GZIP) extractGZip(source, target);
		else throw new IOException("Can't extract in given format");
	}

	private static void extractGZip(Resource source, Resource target) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new GZIPInputStream(IOUtil.toBufferedInputStream(source.getInputStream()));
			os = IOUtil.toBufferedOutputStream(target.getOutputStream());
			IOUtil.copy(is, os, false, false);
		}
		finally {
			IOUtil.close(is, os);
		}
	}

	/**
	 * extract a zip file to a directory
	 * 
	 * @param format
	 * @param sources
	 * @param target
	 * @throws IOException
	 */
	public static void extract(int format, Resource[] sources, Resource target) throws IOException {
		for (int i = 0; i < sources.length; i++) {
			extract(format, sources[i], target);
		}
	}

	private static void extractZip(Resource zipFile, Resource targetDir) throws IOException {
		if (!targetDir.exists() || !targetDir.isDirectory()) throw new IOException("[" + targetDir + "] is not an existing directory");

		if (!zipFile.exists()) throw new IOException("[" + zipFile + "] is not an existing file");

		if (zipFile.isDirectory()) {
			Resource[] files = zipFile.listResources(new OrResourceFilter(new ResourceFilter[] { new ExtensionResourceFilter("zip"), new ExtensionResourceFilter("jar"),
					new ExtensionResourceFilter("war"), new ExtensionResourceFilter("tar"), new ExtensionResourceFilter("ear") }));
			if (files == null) throw new IOException("directory [" + zipFile + "] is empty");
			extract(FORMAT_ZIP, files, targetDir);
			return;
		}

		// read the zip file and build a query from its contents
		unzip(zipFile, targetDir);
		/*
		 * ZipInputStream zis=null; try { zis = new ZipInputStream(
		 * IOUtil.toBufferedInputStream(zipFile.getInputStream()) ) ; ZipEntry entry; while ( ( entry =
		 * zis.getNextEntry()) != null ) { Resource target=targetDir.getRealResource(entry.getName());
		 * if(entry.isDirectory()) { target.mkdirs(); } else { Resource parent=target.getParentResource();
		 * if(!parent.exists())parent.mkdirs();
		 * 
		 * IOUtil.copy(zis,target,false); } target.setLastModified(entry.getTime()); zis.closeEntry() ; } }
		 * finally { IOUtil.closeEL(zis); }
		 */
	}

	private static void unzip(Resource zipFile, Resource targetDir) throws IOException {
		/*
		 * if(zipFile instanceof File){ unzip((File)zipFile, targetDir); return; }
		 */

		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(IOUtil.toBufferedInputStream(zipFile.getInputStream()));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				Resource target = ZipUtil.toResource(targetDir, entry);
				if (entry.isDirectory()) {
					target.mkdirs();
				}
				else {
					Resource parent = target.getParentResource();
					if (!parent.exists()) parent.mkdirs();
					if (!target.exists()) IOUtil.copy(zis, target, false);
				}
				target.setLastModified(entry.getTime());
				zis.closeEntry();
			}
		}
		finally {
			IOUtil.close(zis);
		}
	}

	/**
	 * compress data to a zip file
	 * 
	 * @param format format it that should by compressed usually is CompressUtil.FORMAT_XYZ
	 * @param source
	 * @param target
	 * @param includeBaseFolder
	 * @param mode
	 * @throws IOException
	 */
	public static void compress(int format, Resource source, Resource target, boolean includeBaseFolder, int mode) throws IOException {
		if (format == FORMAT_GZIP) compressGZip(source, target);
		else {
			Resource[] sources = (!includeBaseFolder && source.isDirectory()) ? source.listResources() : new Resource[] { source };
			compress(format, sources, target, mode);
		}
	}

	/**
	 * compress data to a zip file
	 * 
	 * @param format format it that should by compressed usually is CompressUtil.FORMAT_XYZ
	 * @param sources
	 * @param target
	 * @param mode
	 * @throws IOException
	 */
	public static void compress(int format, Resource[] sources, Resource target, int mode) throws IOException {

		if (format == FORMAT_ZIP) compressZip(sources, target, null);

		else throw new IOException("Can't compress in given format");
	}

	/**
	 * compress a source file to a gzip file
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 * @throws IOException
	 */
	private static void compressGZip(Resource source, Resource target) throws IOException {
		if (source.isDirectory()) {
			throw new IOException("You can only create a GZIP File from a single source file, use TGZ (TAR-GZIP) to first TAR multiple files");
		}
		InputStream is = null;
		OutputStream os = null;
		try {
			is = source.getInputStream();
			os = target.getOutputStream();
		}
		catch (IOException ioe) {
			IOUtil.close(is, os);
			throw ioe;
		}
		compressGZip(is, os);

	}

	public static void compressGZip(InputStream source, OutputStream target) throws IOException {
		InputStream is = IOUtil.toBufferedInputStream(source);
		if (!(target instanceof GZIPOutputStream)) target = new GZIPOutputStream(IOUtil.toBufferedOutputStream(target));
		IOUtil.copy(is, target, true, true);
	}

	/**
	 * compress a source file/directory to a zip file
	 * 
	 * @param sources
	 * @param target
	 * @param filter
	 * @throws IOException
	 */
	public static void compressZip(Resource[] sources, Resource target, ResourceFilter filter) throws IOException {
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(IOUtil.toBufferedOutputStream(target.getOutputStream()));
			compressZip("", sources, zos, filter);
		}
		finally {
			IOUtil.close(zos);
		}
	}

	public static void compressZip(Resource[] sources, ZipOutputStream zos, ResourceFilter filter) throws IOException {
		compressZip("", sources, zos, filter);
	}

	private static void compressZip(String parent, Resource[] sources, ZipOutputStream zos, ResourceFilter filter) throws IOException {
		if (!StringUtil.isEmpty(parent)) parent += "/";
		if (sources != null) {
			for (int i = 0; i < sources.length; i++) {
				compressZip(parent + sources[i].getName(), sources[i], zos, filter);
			}
		}
	}

	private static void compressZip(String parent, Resource source, ZipOutputStream zos, ResourceFilter filter) throws IOException {
		if (source.isFile()) {
			// if(filter.accept(source)) {
			ZipEntry ze = new ZipEntry(parent);
			ze.setTime(source.lastModified());
			zos.putNextEntry(ze);
			try {
				IOUtil.copy(source, zos, false);
			}
			finally {
				zos.closeEntry();
			}
			// }
		}
		else if (source.isDirectory()) {
			if (!StringUtil.isEmpty(parent)) {
				ZipEntry ze = new ZipEntry(parent + "/");
				ze.setTime(source.lastModified());
				try {
					zos.putNextEntry(ze);
				}
				catch (IOException ioe) {
					if (Caster.toString(ioe.getMessage()).indexOf("duplicate") == -1) throw ioe;
				}
				zos.closeEntry();
			}
			compressZip(parent, filter == null ? source.listResources() : source.listResources(filter), zos, filter);
		}
	}

	public static void merge(Resource[] sources, Resource target) throws IOException {
		ZipEntry entry;
		ZipInputStream zis = null;
		ZipOutputStream zos = null;
		Set<String> done = new HashSet<>();
		try {
			zos = new ZipOutputStream(IOUtil.toBufferedOutputStream(target.getOutputStream()));

			for (Resource r: sources) {

				try {
					zis = new ZipInputStream(IOUtil.toBufferedInputStream(r.getInputStream()));
					while ((entry = zis.getNextEntry()) != null) {
						if (!done.contains(entry.getName())) {
							zos.putNextEntry(entry);
							IOUtil.copy(zis, zos, false, false);
							done.add(entry.getName());
						}
						zos.closeEntry();
					}
				}
				finally {
					IOUtil.close(zis);
				}
			}
		}
		finally {
			IOUtil.close(zos);
		}
	}

	public static void main(String[] args) throws IOException {
		ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
		Resource[] sources = new Resource[] { frp.getResource("/Users/mic/Downloads/aws-java-sdk-core-1.12.153.jar"),
				frp.getResource("/Users/mic/Downloads/aws-java-sdk-kms-1.12.153.jar"), frp.getResource("/Users/mic/Downloads/aws-java-sdk-s3-1.12.153.jar"),
				frp.getResource("/Users/mic/Downloads/jmespath-java-1.12.153.jar") };
		merge(sources, frp.getResource("/Users/mic/Downloads/aws-java-sdk-s3-all-1.12.153.jar"));

		/*
		 * 
		 * Resource src = frp.getResource("/Users/mic/temp/a");
		 * 
		 * Resource tgz = frp.getResource("/Users/mic/temp/b/a.tgz"); tgz.getParentResource().mkdirs();
		 * Resource tar = frp.getResource("/Users/mic/temp/b/a.tar"); tar.getParentResource().mkdirs();
		 * Resource zip = frp.getResource("/Users/mic/temp/b/a.zip"); zip.getParentResource().mkdirs();
		 * 
		 * Resource tgz1 = frp.getResource("/Users/mic/temp/b/tgz"); tgz1.mkdirs(); Resource tar1 =
		 * frp.getResource("/Users/mic/temp/b/tar"); tar1.mkdirs(); Resource zip1 =
		 * frp.getResource("/Users/mic/temp/b/zip"); zip1.mkdirs();
		 * 
		 * compressTGZ(new Resource[] { src }, tgz, -1); compressTar(new Resource[] { src }, tar, -1);
		 * compressZip(new Resource[] { src }, zip, null);
		 * 
		 * extractTGZ(tgz, tgz1); extractTar(tar, tar1); extractZip(src, zip1);
		 */

	}
}
