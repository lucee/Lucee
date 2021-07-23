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
package lucee.commons.lang;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.FileWrapper;

// TODO umbauen auf ZipInputStream oder ein wrapper schreiben fuer resorces der das file interface einhaelt

public final class ArchiveClassLoader extends ClassLoader implements Closeable {

	private final ZipFile zip;
	private final ClassLoader pcl;

	/**
	 * constructor of the class
	 * 
	 * @param file
	 * @param parent
	 * @throws IOException
	 */
	public ArchiveClassLoader(Resource file, ClassLoader parent) throws IOException {
		super(parent);
		this.pcl = parent;
		// this.file=file;

		// print.ln("archive:"+file.getPath());
		if (!file.exists()) throw new FileNotFoundException("file " + file.getAbsolutePath() + " doesn't exist");
		if (!file.isFile()) throw new IOException(file.getAbsolutePath() + " is not a file");
		if (!file.isReadable()) throw new IOException("no access to " + file.getAbsolutePath() + " file");

		this.zip = new ZipFile(FileWrapper.toFile(file));
	}

	/**
	 * Loads the class with the specified name. This method searches for classes in the same manner as
	 * the {@link #loadClass(String, boolean)} method. It is called by the Java virtual machine to
	 * resolve class references. Calling this method is equivalent to calling
	 * <code>loadClass(name, false)</code>.
	 *
	 * @param name the name of the class
	 * @return the resulting <code>Class</code> object
	 * @exception ClassNotFoundException if the class was not found
	 */
	@Override
	public Class loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	/**
	 * Loads the class with the specified name. The default implementation of this method searches for
	 * classes in the following order:
	 * <p>
	 *
	 * <ol>
	 * <li>Call {@link #findLoadedClass(String)} to check if the class has already been loaded.
	 * <p>
	 * <li>Call the <code>loadClass</code> method on the parent class loader. If the parent is
	 * <code>null</code> the class loader built-in to the virtual machine is used, instead.
	 * <p>
	 * <li>Call the {@link #findClass(String)} method to find the class.
	 * <p>
	 * </ol>
	 *
	 * If the class was found using the above steps, and the <code>resolve</code> flag is true, this
	 * method will then call the {@link #resolveClass(Class)} method on the resulting class object.
	 * <p>
	 * From the Java 2 SDK, v1.2, subclasses of ClassLoader are encouraged to override
	 * {@link #findClass(String)}, rather than this method.
	 * <p>
	 *
	 * @param name the name of the class
	 * @param resolve if <code>true</code> then resolve the class
	 * @return the resulting <code>Class</code> object
	 * @exception ClassNotFoundException if the class could not be found
	 */
	@Override
	protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class c = findLoadedClass(name);
		if (c == null) {
			c = findClassEL(name);
			if (c == null) {
				c = pcl.loadClass(name);
			}
		}
		if (resolve) {
			resolveClass(c);
		}
		return c;
	}

	@Override
	protected Class findClass(String name) throws ClassNotFoundException {
		Class clazz = findClassEL(name);
		if (clazz != null) return clazz;
		throw new ClassNotFoundException("class " + name + " not found");
	}

	private Class findClassEL(String name) {
		byte[] barr = getBytes(name.replace('.', '/').concat(".class"));
		if (barr == null) return null;
		try {
			int start = ClassUtil.hasCF33Prefix(barr) ? 10 : 0;
			return defineClass(name, barr, start, barr.length - start);
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream is = super.getResourceAsStream(name);
		if (is != null) return is;

		byte[] barr = getBytes(name);
		int start = ClassUtil.hasCF33Prefix(barr) ? 10 : 0;
		if (barr != null) return new ByteArrayInputStream(barr, start, barr.length - start);

		return null;
	}

	@Override
	public URL getResource(String name) {
		return null;
	}

	private byte[] getBytes(String name) {

		ZipEntry entry = zip.getEntry(name);

		if (entry == null) return null;

		int size = (int) entry.getSize();
		InputStream is = null;
		try {
			is = zip.getInputStream(entry);
			byte[] data = new byte[size];
			int pos = 0;
			while (pos < size) {
				int n = is.read(data, pos, data.length - pos);
				pos += n;
			}
			return data;
		}
		catch (IOException ioe) {
		}
		finally {
			IOUtil.closeEL(is);
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		zip.close();
	}
}