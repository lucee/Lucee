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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;

/**
 * Directory ClassLoader
 */
public final class PClassLoader extends ClassLoader {

	private Resource directory;
	private ConfigPro config;
	private final ClassLoader[] parents;

	// Set<String> loadedClasses = new HashSet<>();
	Set<String> unavaiClasses = new HashSet<>();

	private Map<String, PhysicalClassLoader> customCLs;
	private Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();

	/**
	 * Constructor of the class
	 * 
	 * @param directory
	 * @param parent
	 * @throws IOException
	 */
	public PClassLoader(Config c, Resource directory) throws IOException {
		this(c, directory, (ClassLoader[]) null, true);
	}

	public PClassLoader(Config c, Resource directory, ClassLoader[] parentClassLoaders, boolean includeCoreCL) throws IOException {
		parents = parentClassLoaders == null || parentClassLoaders.length == 0 ? new ClassLoader[] { c.getClassLoader() } : parentClassLoaders;
		config = (ConfigPro) c;

		// check directory
		if (!directory.exists()) directory.mkdirs();
		if (!directory.isDirectory()) throw new IOException("resource " + directory + " is not a directory");
		if (!directory.canRead()) throw new IOException("no access to " + directory + " directory");
		this.directory = directory;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = classes.get(name);
		if (clazz != null) return clazz;

		// if(unavaiClasses.contains(name)) return defaultValue;
		clazz = findClass(name, (Class) null);
		if (clazz != null) return clazz;
		return super.loadClass(name, resolve);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> clazz = findClass(name, (Class) null);
		if (clazz != null) return clazz;
		return super.findClass(name);
	}

	@Override
	public URL getResource(String name) {
		return null;
	}

	@Override
	protected URL findResource(String name) {
		// TODO Auto-generated method stub
		return super.findResource(name);
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		// TODO
		return super.findResources(name);
	}

	private Class<?> findClass(String name, Class<?> defaultValue) {
		Resource res = directory.getRealResource(name.replace('.', '/').concat(".class"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			IOUtil.copy(res, baos, false);
		}
		catch (IOException e) {
			this.unavaiClasses.add(name);
			return defaultValue;
		}

		byte[] barr = baos.toByteArray();
		IOUtil.closeEL(baos);
		return loadClass(name, barr);
	}

	public synchronized Class<?> loadClass(String name, byte[] barr) {
		Class<?> clazz = new TestClassLoader().loadClass(name, barr);
		classes.put(name, clazz);
		return clazz;
	}

	static class TestClassLoader extends ClassLoader {
		public Class<?> loadClass(String name, byte[] barr) {
			return defineClass(name, barr, 0, barr.length);

		}
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		Resource f = _getResource(name);
		if (f != null) {
			try {
				return IOUtil.toBufferedInputStream(f.getInputStream());
			}
			catch (IOException e) {}
		}
		return null;
	}

	/**
	 * returns matching File Object or null if file not exust
	 * 
	 * @param name
	 * @return matching file
	 */
	public Resource _getResource(String name) {
		Resource f = directory.getRealResource(name);
		if (f != null && f.exists() && f.isFile()) return f;
		return null;
	}

	public boolean hasClass(String className) {
		return hasResource(className.replace('.', '/').concat(".class"));
	}

	public boolean hasResource(String name) {
		return _getResource(name) != null;
	}

	/**
	 * @return the directory
	 */
	public Resource getDirectory() {
		return directory;
	}
}