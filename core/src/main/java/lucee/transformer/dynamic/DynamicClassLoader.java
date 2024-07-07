package lucee.transformer.dynamic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExtendableClassLoader;

/**
 * Directory ClassLoader
 */
public final class DynamicClassLoader extends ExtendableClassLoader {

	static {
		boolean res = registerAsParallelCapable();
	}
	private Resource directory;

	private final Map<String, String> loadedClasses = new ConcurrentHashMap<>();
	private final Map<String, String> allLoadedClasses = new ConcurrentHashMap<>(); // this includes all renames
	private final Map<String, String> unavaiClasses = new ConcurrentHashMap<>();

	private final Map<String, SoftReference<Object>> instances = new ConcurrentHashMap<>();

	private static long counter = 0L;
	private static long _start = 0L;
	private static String start = Long.toString(_start, Character.MAX_RADIX);
	private static final Object countToken = new Object();

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
	 * @param log
	 * @throws IOException
	 */
	public DynamicClassLoader(Resource directory, Log log) throws IOException {
		this(directory, (ClassLoader[]) null, true, log);
	}

	public DynamicClassLoader(ClassLoader parent, Resource directory, Log log) {
		super(parent);

		try {
			if (!directory.exists()) directory.mkdirs();
			if (!directory.isDirectory()) throw new IOException("Resource [" + directory + "] is not a directory");
			if (!directory.canRead()) throw new IOException("Access denied to [" + directory + "] directory");
			this.directory = directory; // we only store it when okay
		}
		catch (Exception e) {
			if (log != null) log.error("dynamic", e);
		}
	}

	public DynamicClassLoader(Resource directory, ClassLoader[] parentClassLoaders, boolean includeCoreCL, Log log) {
		super(parentClassLoaders == null || parentClassLoaders.length == 0 ? directory.getClass().getClassLoader() : parentClassLoaders[0]);

		// parents.add(new TP().getClass().getClassLoader());
		// if (includeCoreCL) parents.add(CFMLEngineImpl.class.getClassLoader());

		// check directory
		try {
			if (!directory.exists()) directory.mkdirs();
			if (!directory.isDirectory()) throw new IOException("Resource [" + directory + "] is not a directory");
			if (!directory.canRead()) throw new IOException("Access denied to [" + directory + "] directory");
			this.directory = directory; // we only store it when okay
		}
		catch (Exception e) {
			if (log != null) log.error("dynamic", e);
		}
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	public Object loadInstance(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		SoftReference<Object> ref = instances.get(name);
		Object value;
		if (ref != null && (value = ref.get()) != null) {
			return value;
		}
		Class<?> clazz = loadClass(name, false);
		value = clazz.getConstructor().newInstance();
		instances.put(name, new SoftReference<Object>(value));
		return value;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		synchronized (SystemUtil.createToken("dcl", name)) {
			return loadClass(name, resolve, true);
		}
	}

	private Class<?> loadClass(String name, boolean resolve, boolean loadFromFS) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			try {
				c = getParent().loadClass(name);
			}
			catch (Exception e) {
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
		synchronized (SystemUtil.createToken("dcl", name)) {
			if (directory == null) throw new ClassNotFoundException("Class [" + name + "] not found (memory mode)");
			Resource res = directory.getRealResource(name.replace('.', '/').concat(".class"));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				IOUtil.copy(res, baos, false);
			}
			catch (IOException e) {
				this.unavaiClasses.put(name, "");
				throw new ClassNotFoundException("Class [" + name + "] is invalid or doesn't exist [parent:" + getParent() + "]", e);
			}

			byte[] barr = baos.toByteArray();
			IOUtil.closeEL(baos);
			return _loadClass(name, barr);
		}
	}

	public Object loadInstance(String name, byte[] barr) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, UnmodifiableClassException {
		SoftReference<Object> ref = instances.get(name);
		Object value;
		if (ref != null && (value = ref.get()) != null) {
			return value;
		}
		Class<?> clazz = loadClass(name, barr);
		value = clazz.getConstructor().newInstance();
		instances.put(name, new SoftReference<Object>(value));
		return value;
	}

	@Override
	public Class<?> loadClass(String className, byte[] barr) throws UnmodifiableClassException {
		Class<?> clazz = null;

		// store file
		if (directory != null) {

			Resource classFile = directory.getRealResource(className.replace('.', '/') + ".class");
			classFile.getParentResource().mkdirs();
			try {
				IOUtil.write(classFile, barr);
			}
			catch (IOException e) {
				// TODO Log
				e.printStackTrace();
			}
		}
		synchronized (SystemUtil.createToken("dcl", className)) {
			try {
				return _loadClass(className, barr);
			}
			catch (Exception | LinkageError e) {
			}

			// new class , not in memory yet
			try {
				return loadClass(className, false, true);
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Class<?> _loadClass(String name, byte[] barr) {
		// print.e(">>>>" + name);
		Class<?> clazz = defineClass(name, barr, 0, barr.length);
		if (clazz != null) {
			loadedClasses.put(name, "");
			allLoadedClasses.put(name, "");

			resolveClass(clazz);
		}
		return clazz;
	}

	@Override
	public URL getResource(String name) {
		return null;
	}

	public int getSize(boolean includeAllRenames) {
		return includeAllRenames ? allLoadedClasses.size() : loadedClasses.size();
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
		Resource f = directory == null ? null : directory.getRealResource(name);
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
}
