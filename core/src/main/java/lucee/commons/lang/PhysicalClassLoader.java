/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageSourcePool;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.bytecode.util.ClassRenamer;

/**
 * Directory ClassLoader
 */
public final class PhysicalClassLoader extends URLClassLoader implements ExtendableClassLoader {

	static {
		boolean res = registerAsParallelCapable();
	}

	private static RC rc = new RC();

	private static Map<String, PhysicalClassLoader> classLoaders = new ConcurrentHashMap<>();

	private Resource directory;
	private ConfigPro config;
	private final ClassLoader addionalClassLoader;
	private final Collection<Resource> resources;

	private Map<String, String> loadedClasses = new ConcurrentHashMap<String, String>();
	private Map<String, String> allLoadedClasses = new ConcurrentHashMap<String, String>(); // this includes all renames
	private Map<String, String> unavaiClasses = new ConcurrentHashMap<String, String>();

	private PageSourcePool pageSourcePool;

	private boolean rpc;

	private String birthplace;

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

	public static PhysicalClassLoader getPhysicalClassLoader(Config c, Resource directory, boolean reload) throws IOException {

		String key = HashUtil.create64BitHashAsString(directory.getAbsolutePath());

		PhysicalClassLoader rpccl = reload ? null : classLoaders.get(key);
		if (rpccl == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader", key)) {
				rpccl = reload ? null : classLoaders.get(key);
				if (rpccl == null) {
					classLoaders.put(key, rpccl = new PhysicalClassLoader(c, new ArrayList<Resource>(), directory, SystemUtil.getCombinedClassLoader(), null, null, false));
				}
			}
		}
		return rpccl;
	}

	public static PhysicalClassLoader getRPCClassLoader(Config c, JavaSettings js, boolean reload, ClassLoader parent) throws IOException {

		String key = js == null ? "orphan" : ((JavaSettingsImpl) js).id();

		PhysicalClassLoader rpccl = reload ? null : classLoaders.get(key);
		if (rpccl == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader", key)) {
				rpccl = reload ? null : classLoaders.get(key);
				if (rpccl == null) {
					List<Resource> resources;
					if (js == null) {
						resources = new ArrayList<Resource>();
					}
					else {
						resources = toSortedList(((JavaSettingsImpl) js).getAllResources());
					}
					Resource dir = storeResourceMeta(c, key, js, resources);
					// (Config config, String key, JavaSettings js, Collection<Resource> _resources)
					classLoaders.put(key, rpccl = new PhysicalClassLoader(c, resources, dir, parent != null ? parent : SystemUtil.getCombinedClassLoader(), null, null, true));
				}
			}
		}
		return rpccl;
	}

	public static PhysicalClassLoader getRPCClassLoader(Config c, BundleClassLoader bcl, boolean reload) throws IOException {
		String key = HashUtil.create64BitHashAsString(bcl + "");
		PhysicalClassLoader rpccl = reload ? null : classLoaders.get(key);
		if (rpccl == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader", key)) {
				rpccl = reload ? null : classLoaders.get(key);
				if (rpccl == null) {
					Resource dir = c.getClassDirectory().getRealResource("RPC/" + key);
					if (!dir.exists()) ResourceUtil.createDirectoryEL(dir, true);
					// (Config config, String key, JavaSettings js, Collection<Resource> _resources)
					classLoaders.put(key, rpccl = new PhysicalClassLoader(c, new ArrayList<Resource>(), dir, SystemUtil.getCombinedClassLoader(), bcl, null, true));
				}
			}
		}
		return rpccl;
	}

	private PhysicalClassLoader(Config c, List<Resource> resources, Resource directory, ClassLoader parentClassLoader, ClassLoader addionalClassLoader,
			PageSourcePool pageSourcePool, boolean rpc) throws IOException {
		super(doURLs(resources), parentClassLoader == null ? (parentClassLoader = SystemUtil.getCombinedClassLoader()) : parentClassLoader);
		this.resources = resources;
		config = (ConfigPro) c;
		this.addionalClassLoader = addionalClassLoader;
		this.birthplace = ExceptionUtil.getStacktrace(new Throwable(), false);
		this.pageSourcePool = pageSourcePool;
		// ClassLoader resCL = parent!=null?parent:config.getResourceClassLoader(null);

		// check directory
		if (!directory.exists()) directory.mkdirs();
		if (!directory.isDirectory()) throw new IOException("Resource [" + directory + "] is not a directory");
		if (!directory.canRead()) throw new IOException("Access denied to [" + directory + "] directory");
		this.directory = directory;
		this.rpc = rpc;
	}

	public String getBirthplace() {
		return birthplace;
	}

	public boolean isRPC() {
		return rpc;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized (SystemUtil.createToken("pcl", name)) {
			return loadClass(name, resolve, true);
		}
	}

	private Class<?> loadClass(String name, boolean resolve, boolean loadFromFS) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			try {
				c = super.loadClass(name, resolve);
			}
			catch (Exception e) {
			}

			if (addionalClassLoader != null) {
				try {
					c = addionalClassLoader.loadClass(name);
				}
				catch (Exception e) {
				}
			}

			// }
			if (c == null) {
				if (loadFromFS) c = findClass(name);
				else throw new ClassNotFoundException(name);
			}
		}
		if (resolve) resolveClass(c);
		return c;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {

		try {
			return super.findClass(name);
		}
		catch (ClassNotFoundException cnfe) {
		}

		if (addionalClassLoader != null) {
			try {
				return addionalClassLoader.loadClass(name);
			}
			catch (ClassNotFoundException e) {
			}
		}

		synchronized (SystemUtil.createToken("pcl", name)) {
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

		synchronized (SystemUtil.createToken("pcl", name)) {

			// new class , not in memory yet
			try {
				clazz = loadClass(name, false, false); // we do not load existing class from disk
			}
			catch (ClassNotFoundException cnf) {
			}
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
			allLoadedClasses.put(name, "");

			resolveClass(clazz);
		}
		return clazz;
	}

	public Resource[] getJarResources() {
		return resources.toArray(new Resource[resources.size()]);
	}

	public boolean hasJarResources() {
		return resources.isEmpty();
	}

	public int getSize(boolean includeAllRenames) {
		return includeAllRenames ? allLoadedClasses.size() : loadedClasses.size();
	}

	/*
	 * @Override public URL getResource(String name) { URL r = super.getResource(name); if (r != null)
	 * return r; print.e("xx ====>" + name);
	 * 
	 * Resource f = _getResource(name);
	 * 
	 * if (f != null) { return ResourceUtil.toURL(f, null); } return null; }
	 */

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream is = super.getResourceAsStream(name);
		if (is != null) return is;

		URL url = super.getResource(name);
		if (url != null) {
			try {
				return IOUtil.toBufferedInputStream(url.openStream());
			}
			catch (IOException e) {
			}
		}

		Resource f = _getResource(name);
		if (f != null) {
			try {
				return IOUtil.toBufferedInputStream(f.getInputStream());
			}
			catch (IOException e) {
			}
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
		if (f != null && f.isFile()) return f;
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

	public void clear() {
		clear(true);
	}

	public void clear(boolean clearPagePool) {
		if (clearPagePool && pageSourcePool != null) pageSourcePool.clearPages(this);
		this.loadedClasses.clear();
		this.allLoadedClasses.clear();
		this.unavaiClasses.clear();
	}

	private static Resource storeResourceMeta(Config config, String key, JavaSettings js, Collection<Resource> _resources) throws IOException {
		Resource dir = config.getClassDirectory().getRealResource("RPC/" + key);
		if (!dir.exists()) {
			ResourceUtil.createDirectoryEL(dir, true);
			Resource file = dir.getRealResource("classloader-resources.json");
			Struct root = new StructImpl();
			root.setEL(KeyConstants._resources, _resources);
			JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
			try {
				String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_COLUMN, null);
				IOUtil.write(file, str, CharsetUtil.UTF8, false);
			}
			catch (ConverterException e) {
				throw ExceptionUtil.toIOException(e);
			}

		}
		return dir;
	}

	/**
	 * removes memory based appendix from class name, for example it translates
	 * [test.test_cfc$sub2$cf$5] to [test.test_cfc$sub2$cf]
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static String substractAppendix(String name) throws ApplicationException {
		if (name.endsWith("$cf")) return name;
		int index = name.lastIndexOf('$');
		if (index != -1) {
			name = name.substring(0, index);
		}
		if (name.endsWith("$cf")) return name;
		throw new ApplicationException("could not remove appendix from [" + name + "]");
	}

	@Override
	public void finalize() throws Throwable {
		try {
			clear();
		}
		catch (Exception e) {
			LogUtil.log(config, "classloader", e);
		}
		super.finalize();
	}

	public static List<Resource> toSortedList(Collection<Resource> resources) {
		List<Resource> list = new ArrayList<Resource>();
		if (resources != null) {
			for (Resource r: resources) {
				if (r != null) list.add(r);
			}
		}
		java.util.Collections.sort(list, rc);
		return list;
	}

	public static List<Resource> toSortedList(Resource[] resources) {
		List<Resource> list = new ArrayList<Resource>();
		if (resources != null) {
			for (Resource r: resources) {
				if (r != null) list.add(r);
			}
		}
		java.util.Collections.sort(list, rc);
		return list;
	}

	private static URL[] doURLs(Collection<Resource> reses) throws IOException {
		List<URL> list = new ArrayList<URL>();
		for (Resource r: reses) {
			if ("jar".equalsIgnoreCase(ResourceUtil.getExtension(r, null)) || r.isDirectory()) list.add(doURL(r));
		}
		return list.toArray(new URL[list.size()]);
	}

	private static URL doURL(Resource res) throws IOException {
		if (!(res instanceof FileResource)) {
			return ResourceUtil.toFile(res).toURL();
		}
		return ((FileResource) res).toURL();
	}

	private static class RC implements Comparator<Resource> {

		@Override
		public int compare(Resource l, Resource r) {
			return l.getAbsolutePath().compareTo(r.getAbsolutePath());
		}
	}

}
