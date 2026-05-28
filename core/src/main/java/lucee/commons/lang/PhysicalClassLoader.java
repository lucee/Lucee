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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.PageSourcePool;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ClassRenamer;
import lucee.transformer.dynamic.DynamicInvoker;
import lucee.transformer.dynamic.meta.dynamic.ClazzDynamic;

/**
 * Directory ClassLoader
 */
public final class PhysicalClassLoader extends URLClassLoader implements ExtendableClassLoader, ClassLoaderDefault {

	private static final double CLASSLOADER_INSPECTION_SIZE = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.template.classloader.inspection.size", null), 100);
	private static final double CLASSLOADER_INSPECTION_SIZEBYTES = CLASSLOADER_INSPECTION_SIZE * 1024 * 1024;

	private static final double CLASSLOADER_INSPECTION_COUNT = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.template.classloader.inspection.count", null), 1000);
	private static final double CLASSLOADER_INSPECTION_RATIO = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.template.classloader.inspection.ratio", null), 3);

	// Track last flush stats for testing/debugging
	private static volatile int lastFlushPagesCleared = 0;

	public static int getLastFlushPagesCleared() {
		return lastFlushPagesCleared;
	}

	public static void resetLastFlushPagesCleared() {
		lastFlushPagesCleared = 0;
	}

	static {
		boolean res = registerAsParallelCapable();
	}

	private Resource directory;
	private ConfigPro config;
	private final ClassLoader addionalClassLoader;
	private final List<Resource> resources;

	private Map<String, Integer> loadedClasses = new ConcurrentHashMap<>();
	private Map<String, Integer> allLoadedClasses = new ConcurrentHashMap<>(); // this includes all renames
	private Map<String, String> unavaiClasses = new ConcurrentHashMap<>();

	private boolean rpc;

	private String birthplace;

	public final String id;

	PhysicalClassLoader(Config c, List<Resource> resources, Resource directory, ClassLoader parentClassLoader, ClassLoader addionalClassLoader, boolean rpc) throws IOException {

		this(c, PhysicalClassLoaderFactory.doURLs(resources), resources, directory,
				parentClassLoader == null ? (parentClassLoader = SystemUtil.getCombinedClassLoader()) : parentClassLoader, addionalClassLoader, rpc);

		// check directory
		if (!directory.exists()) directory.mkdirs();
		if (!directory.isDirectory()) throw new IOException("Resource [" + directory + "] is not a directory");
		if (!directory.canRead()) throw new IOException("Access denied to [" + directory + "] directory");
	}

	private PhysicalClassLoader(Config c, URL[] urls, List<Resource> resources, Resource directory, ClassLoader parentClassLoader, ClassLoader addionalClassLoader, boolean rpc) {
		super(urls, parentClassLoader == null ? (parentClassLoader = SystemUtil.getCombinedClassLoader()) : parentClassLoader);
		this.resources = resources;
		config = (ConfigPro) c;

		if (c != null && LogUtil.doesTrace(c.getLog("application"))) {
			c.getLog("application").trace("page-source", "PhysicalClassLoader<init>:" + ExceptionUtil.getStacktrace(new Throwable(), false));
		}

		this.addionalClassLoader = addionalClassLoader;
		this.birthplace = ExceptionUtil.getStacktrace(new Throwable(), false);

		this.directory = directory;
		this.rpc = rpc;

		StringBuilder sb = new StringBuilder();
		sb.append(directory);
		if (resources != null) {
			for (Resource r: resources) {
				sb.append(';').append(r);
			}
		}
		id = HashUtil.create64BitHashAsString(sb.toString());
	}

	static PhysicalClassLoader flush(PhysicalClassLoader existing, Config config, boolean doClone) {
		PhysicalClassLoader clone = doClone
				? new PhysicalClassLoader(config, existing.getURLs(), existing.resources, existing.directory, existing.getParent(), existing.addionalClassLoader, existing.rpc)
				: null;

		// flush PageSourcePools
		int pagesCleared = PageSourcePool.clearPages(config, existing, false);
		lastFlushPagesCleared = pagesCleared;

		// flush DynamicInvoker
		DynamicInvoker instance = DynamicInvoker.getExistingInstance();
		int count = 0;
		if (instance != null) count += instance.remove(existing);
		count += ClazzDynamic.remove(existing);
		if (LogUtil.does(Log.LEVEL_TRACE)) {
			int all = existing.allLoadedClasses.size();
			int unique = existing.loadedClasses.size();
			int allClassesBytes = 0;
			for (Integer i: existing.allLoadedClasses.values()) {
				allClassesBytes += i.intValue();
			}
			LogUtil.log(Log.LEVEL_TRACE, "physical-classloader", "flush physical classloader [" + existing.getDirectory() + "] (classes: " + all + "/" + unique + ", "
					+ StringUtil.byteFormat(allClassesBytes) + ", pages cleared: " + pagesCleared + ", dynamic invoker: " + count + ")");
		}

		ClazzDynamic.flush(existing);

		return clone;
	}

	static PhysicalClassLoader flushIfNecessary(PhysicalClassLoader existing, Config config) {
		// RPC classloaders have no rename pressure, skip threshold checks
		if (existing.rpc) return null;

		double all;

		if (existing.allLoadedClasses.size() == 0) {
			return null;
		}

		if (LogUtil.does(Log.LEVEL_TRACE)) {
			int allClasses = existing.allLoadedClasses.size();
			int allClassesBytes = 0;
			int uniqueClasses = existing.loadedClasses.size();
			double ratio = uniqueClasses > 0 ? (double) allClasses / uniqueClasses : 0;

			for (Integer i: existing.allLoadedClasses.values()) {
				allClassesBytes += i.intValue();
			}

			boolean willFlush = allClasses > CLASSLOADER_INSPECTION_SIZE && ratio > CLASSLOADER_INSPECTION_RATIO;
			int level = willFlush ? Log.LEVEL_DEBUG : Log.LEVEL_TRACE;

			LogUtil.log(level, "physical-classloader",
					"checking if flush necessary for physical classloader [" + existing.getDirectory() + "]: " + "all loaded classes: " + allClasses + " ("
							+ StringUtil.byteFormat(allClassesBytes) + "), " + "unique loaded classes: " + uniqueClasses + ", " + "ratio: " + String.format("%.2f", ratio) + ", "
							+ "inspection size threshold: " + Caster.toString(CLASSLOADER_INSPECTION_COUNT) + "/" + Caster.toString(CLASSLOADER_INSPECTION_SIZE) + ", "
							+ "inspection ratio threshold: " + CLASSLOADER_INSPECTION_RATIO + " - "
							+ (allClasses > CLASSLOADER_INSPECTION_SIZE
									? (ratio > CLASSLOADER_INSPECTION_RATIO ? "FLUSHING (size and ratio thresholds exceeded)"
											: "NOT flushing (ratio " + String.format("%.2f", ratio) + " below threshold " + CLASSLOADER_INSPECTION_RATIO + ")")
									: "NOT flushing (size " + allClasses + " below threshold " + CLASSLOADER_INSPECTION_SIZE + ")"));
		}

		// check size
		if ((all = existing.allLoadedClasses.size()) > CLASSLOADER_INSPECTION_COUNT) {
			if ((all / existing.loadedClasses.size()) > CLASSLOADER_INSPECTION_RATIO) {
				return flush(existing, config, true);
			}

			int allClassesBytes = 0;
			for (Integer i: existing.allLoadedClasses.values()) {
				allClassesBytes += i.intValue();
			}
			if (allClassesBytes > CLASSLOADER_INSPECTION_SIZEBYTES) {
				return flush(existing, config, true);
			}
		}
		return null;
	}

	public String getBirthplace() {
		return birthplace;
	}

	public boolean isRPC() {
		return rpc;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false, true);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name, resolve, true);
	}

	public Boolean isClassAvailable(ClassLoader loader, String className) {
		if (allLoadedClasses.containsKey(className)) return true;
		if (unavaiClasses.containsKey(className)) return false;

		return null;
		// String resourcePath = className.replace('.', '/').concat(".class");
		// return loader.getResource(resourcePath) != null;
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve, Class<?> defaultValue) {
		return loadClass(name, resolve, true, defaultValue);
	}

	private Class<?> loadClass(String name, boolean resolve, boolean loadFromFS, Class<?> defaultValue) {
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);

		if (c == null) {
			synchronized (getClassLoadingLock(name)) {
				c = findLoadedClass(name);
				if (c == null) {
					ClassLoader pcl = getParent();
					if (pcl instanceof ClassLoaderDefault) {
						c = ((ClassLoaderDefault) pcl).loadClass(name, resolve, null);
					}
					else {
						try {
							c = super.loadClass(name, resolve);
						}
						catch (Exception e) {}
					}

					if (c == null && addionalClassLoader != null) {
						try {
							c = addionalClassLoader.loadClass(name);
						}
						catch (Exception e) {}
					}

					if (c == null) {
						if (loadFromFS) {
							try {
								c = findClass(name);
							}
							catch (ClassNotFoundException e) {
								return defaultValue;
							}
						}
						else return defaultValue;
					}
				}
			}
		}
		if (resolve) resolveClass(c);
		return c;
	}

	private Class<?> loadClass(String name, boolean resolve, boolean loadFromFS) throws ClassNotFoundException {
		Class<?> c = loadClass(name, resolve, loadFromFS, null);
		if (c == null) {
			throw new ClassNotFoundException(name);
		}
		return c;
	}

	@Override
	public Class<?> loadClass(String name, byte[] barr) throws UnmodifiableClassException {
		synchronized (getClassLoadingLock(name)) {
			Class<?> clazz = findLoadedClass(name);
			if (clazz == null) return _loadClass(name, barr, false);
			/*
			 * Instrumentation instr = InstrumentationFactory.getInstrumentation(config); if (instr != null) {
			 * try { instr.redefineClasses(new ClassDefinition(clazz, barr)); return clazz; } catch (Exception
			 * e) { LogUtil.log(InstrumentationFactory.class.getName(), e); } }
			 */

			return rename(clazz, barr);
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		/*
		 * ClassNotFoundException cnfe = null; try { return super.findClass(name); } catch
		 * (ClassNotFoundException e) { cnfe = e; }
		 */

		if (super.findResource(name.replace('.', '/').concat(".class")) != null) {
			return super.findClass(name);
		}

		if (addionalClassLoader != null) {
			// boolean true in case it returns TRUE or null
			if (!Boolean.FALSE.equals(isClassAvailable(addionalClassLoader, name))) {
				try {
					return addionalClassLoader.loadClass(name);
				}
				catch (ClassNotFoundException e) {
					LogUtil.trace("physical-classloader", e);
				}
			}
		}

		synchronized (getClassLoadingLock(name)) {
			Resource res = directory.getRealResource(name.replace('.', '/').concat(".class"));
			if (!res.isFile()) {
				// if (cnfe != null) throw cnfe;
				throw new ClassNotFoundException("Class [" + name + "] is invalid or doesn't exist");
			}

			return _loadClass(name, read(name), false);
		}
	}

	private byte[] read(String name) throws ClassNotFoundException {
		Resource res = directory.getRealResource(name.replace('.', '/').concat(".class"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			IOUtil.copy(res, baos, false);
		}
		catch (IOException e) {
			this.unavaiClasses.put(name, "");
			throw new ClassNotFoundException("Class [" + name + "] is invalid or doesn't exist", e);
		}
		finally {
			IOUtil.closeEL(baos);
		}
		return baos.toByteArray();
	}

	private Class<?> rename(Class<?> clazz, byte[] barr) {
		String newName = clazz.getName() + "$" + PhysicalClassLoaderFactory.uid();
		return _loadClass(newName, ClassRenamer.rename(barr, newName), true);
	}

	private Class<?> _loadClass(String name, byte[] barr, boolean rename) {
		try {
			Class<?> clazz = defineClass(name, barr, 0, barr.length);

			if (clazz != null) {
				if (!rename) loadedClasses.put(name, barr.length);
				allLoadedClasses.put(name, barr.length);

				resolveClass(clazz);
			}
			return clazz;
		}
		catch (ClassFormatError cfe) {
			if (!ASMUtil.isValidBytecode(barr)) throw new RuntimeException("given bytcode for [" + name + "] is not valid");
			throw cfe;
		}
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

		/*
		 * if (name.endsWith(".class")) { print.ds("getResourceAsStream: " + name + " !!!!!"); // MUST store
		 * the barr in a less memory intensive way String className = name.substring(0, name.length() -
		 * 6).replace('/', '.').replace('\\', '.'); byte[] barr = allLoadedClasses.get(className); if (barr
		 * != null) return new ByteArrayInputStream(barr); }
		 */

		Resource f = _getResource(name);
		if (f != null) {
			try {
				return IOUtil.toBufferedInputStream(f.getInputStream());
			}
			catch (IOException e) {
				LogUtil.trace("physical-classloader", e);
			}
		}

		URL url = super.getResource(name);
		if (url != null) {
			try {
				return IOUtil.toBufferedInputStream(url.openStream());
			}
			catch (IOException e) {
				LogUtil.trace("physical-classloader", e);
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

	private void clear() {
		PageSourcePool.clearPages(config, this, false);

		this.loadedClasses.clear();
		this.allLoadedClasses.clear();
		this.unavaiClasses.clear();
	}

	/**
	 * removes memory based appendix from class name, for example it translates
	 * [test.test_cfc$sub2$cf$5] to [test.test_cfc$sub2$cf]
	 * 
	 * @param name
	 * @return
	 * @throws ApplicationException
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

	public void populate(Query query) {
		/*
		 * int row; ClassInfo ci; for (Entry<String, ClassInfo> e: loadedClasses.entrySet()) { row =
		 * query.addRow(); query.setAtEL("class", row, e.getKey()); ci = e.getValue();
		 * query.setAtEL("iterations", row, ci.count()); query.setAtEL("size", row, ci.size());
		 * query.setAtEL("totaSize", row, ci.totaSize());
		 * 
		 * }
		 */
	}

}