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
import java.lang.instrument.UnmodifiableClassException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceClassLoader;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.bytecode.util.ClassRenamer;

/**
 * Directory ClassLoader
 */
public final class PhysicalClassLoader extends ExtendableClassLoader {

	static {
		boolean res = registerAsParallelCapable();
	}

	private Resource directory;
	private ConfigPro config;
	private final ClassLoader[] parents;

	private Map<String, String> loadedClasses = new ConcurrentHashMap<String, String>();
	private Map<String, String> unavaiClasses = new ConcurrentHashMap<String, String>();

	private Map<String, SoftReference<PhysicalClassLoader>> customCLs;

	private static long counter = 0L;
	private static long _start = 0L;
	private static String start = Long.toString(_start, Character.MAX_RADIX);
	private static Object countToken = new Object();

	public static String uid() {
		synchronized (countToken) {
			counter++;
			if (counter < 0) {
				counter = 1;
				start = Long.toString(++_start, Character.MAX_RADIX);
			}
			if (_start == 0L) return Long.toString(counter, Character.MAX_RADIX);
			return start + "_" + Long.toString(counter, Character.MAX_RADIX);
		}
	}

	/**
	 * Constructor of the class
	 * 
	 * @param directory
	 * @param parent
	 * @throws IOException
	 */
	public PhysicalClassLoader(Config c, Resource directory) throws IOException {
		this(c, directory, (ClassLoader[]) null, true);
	}

	public PhysicalClassLoader(Config c, Resource directory, ClassLoader[] parentClassLoaders, boolean includeCoreCL) throws IOException {
		super(parentClassLoaders == null || parentClassLoaders.length == 0 ? c.getClassLoader() : parentClassLoaders[0]);
		config = (ConfigPro) c;

		// ClassLoader resCL = parent!=null?parent:config.getResourceClassLoader(null);

		List<ClassLoader> tmp = new ArrayList<ClassLoader>();
		if (parentClassLoaders == null || parentClassLoaders.length == 0) {
			ResourceClassLoader _cl = config.getResourceClassLoader(null);
			if (_cl != null) tmp.add(_cl);
		}
		else {
			for (ClassLoader p: parentClassLoaders) {
				tmp.add(p);
			}
		}

		if (includeCoreCL) tmp.add(config.getClassLoaderCore());
		parents = tmp.toArray(new ClassLoader[tmp.size()]);

		// check directory
		if (!directory.exists()) directory.mkdirs();
		if (!directory.isDirectory()) throw new IOException("Resource [" + directory + "] is not a directory");
		if (!directory.canRead()) throw new IOException("Access denied to [" + directory + "] directory");
		this.directory = directory;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {
			return loadClass(name, resolve, true);
		}
	}

	private Class<?> loadClass(String name, boolean resolve, boolean loadFromFS) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			for (ClassLoader p: parents) {
				try {
					c = p.loadClass(name);
					break;
				}
				catch (Exception e) {}
			}
			if (c == null) {
				if (loadFromFS) c = findClass(name);
				else throw new ClassNotFoundException(name);
			}
		}
		if (resolve) resolveClass(c);
		return c;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {// if(name.indexOf("sub")!=-1)print.ds(name);
		synchronized (getClassLoadingLock(name)) {
			Resource res = directory.getRealResource(name.replace('.', '/').concat(".class"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				IOUtil.copy(res, baos, false);
			}
			catch (IOException e) {
				this.unavaiClasses.put(name, "");
				throw new ClassNotFoundException("Class [" + name + "] is invalid or doesn't exist", e);
			}

			byte[] barr = baos.toByteArray();
			IOUtil.closeEL(baos);
			return _loadClass(name, barr, false);
		}
	}

	@Override
	public Class<?> loadClass(String name, byte[] barr) throws UnmodifiableClassException {
		Class<?> clazz = null;

		synchronized (getClassLoadingLock(name)) {

			// new class , not in memory yet
			try {
				clazz = loadClass(name, false, false); // we do not load existing class from disk
			}
			catch (ClassNotFoundException cnf) {}
			if (clazz == null) return _loadClass(name, barr, false);

			// first we try to update the class what needs instrumentation object
			/*
			 * try { InstrumentationFactory.getInstrumentation(config).redefineClasses(new
			 * ClassDefinition(clazz, barr)); return clazz; } catch (Exception e) { LogUtil.log(null,
			 * "compilation", e); }
			 */
			// in case instrumentation fails, we rename it
			return rename(clazz, barr);
		}
	}

	private Class<?> rename(Class<?> clazz, byte[] barr) {
		String newName = clazz.getName() + "$" + uid();
		return _loadClass(newName, ClassRenamer.rename(barr, newName), true);
	}

	private Class<?> _loadClass(String name, byte[] barr, boolean rename) {
		Class<?> clazz = defineClass(name, barr, 0, barr.length);
		if (clazz != null) {
			if (!rename) loadedClasses.put(name, "");
			resolveClass(clazz);
		}
		return clazz;
	}

	@Override
	public URL getResource(String name) {
		return null;
	}

	public int getSize() {
		return loadedClasses.size();
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream is = super.getResourceAsStream(name);
		if (is != null) return is;

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

	public boolean isClassLoaded(String className) {
		return findLoadedClass(className) != null;
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

	public PhysicalClassLoader getCustomClassLoader(Resource[] resources, boolean reload) throws IOException {
		if (ArrayUtil.isEmpty(resources)) return this;
		String key = hash(resources);

		if (reload && customCLs != null) customCLs.remove(key);

		SoftReference<PhysicalClassLoader> tmp = customCLs == null ? null : customCLs.get(key);
		PhysicalClassLoader pcl = tmp == null ? null : tmp.get();
		if (pcl != null) return pcl;
		pcl = new PhysicalClassLoader(config, getDirectory(), new ClassLoader[] { new ResourceClassLoader(resources, getParent()) }, true);
		if (customCLs == null) customCLs = new ConcurrentHashMap<String, SoftReference<PhysicalClassLoader>>();
		customCLs.put(key, new SoftReference<PhysicalClassLoader>(pcl));
		return pcl;
	}

	private String hash(Resource[] resources) {
		Arrays.sort(resources);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < resources.length; i++) {
			sb.append(ResourceUtil.getCanonicalPathEL(resources[i]));
			sb.append(';');
		}
		return HashUtil.create64BitHashAsString(sb.toString(), Character.MAX_RADIX);
	}

	public void clear() {
		this.loadedClasses.clear();
		this.unavaiClasses.clear();
	}
}
