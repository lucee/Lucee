package lucee.commons.lang;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class PhysicalClassLoaderFactory {

	private static final AtomicLong counter = new AtomicLong(0);
	private static long _start = 0L;
	private static String start = Long.toString(_start, Character.MAX_RADIX);
	private static Object countToken = new Object();

	private static final long IDLE_TIMEOUT_MS = Math.max(60_000, Caster.toLongValue(SystemUtil.getSystemPropOrEnvVar("lucee.classloader.idle.timeout", null), 300_000L));
	private static final int IDLE_MINSIZE = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.classloader.idle.minsize", null), 0);

	private static RC rc = new RC();

	private static Map<String, CachedLoader> classLoaders = new ConcurrentHashMap<>();

	static String uid() {
		long currentCounter = counter.incrementAndGet(); // Increment and get atomically
		if (currentCounter < 0) {
			synchronized (countToken) {
				currentCounter = counter.incrementAndGet();
				if (currentCounter < 0) {
					counter.set(0L);
					currentCounter = 0L;
					start = Long.toString(++_start, Character.MAX_RADIX);
				}
			}
		}
		if (_start == 0L) return Long.toString(currentCounter, Character.MAX_RADIX);
		return start + "_" + Long.toString(currentCounter, Character.MAX_RADIX);
	}

	static URL[] doURLs(Collection<Resource> reses) throws IOException {
		List<URL> list = new ArrayList<URL>();
		for (Resource r: reses) {
			if ("jar".equalsIgnoreCase(ResourceUtil.getExtension(r, null)) || r.isDirectory()) list.add(doURL(r));
		}
		return list.toArray(new URL[list.size()]);
	}

	static URL doURL(Resource res) throws IOException {
		if (!(res instanceof FileResource)) {
			return ResourceUtil.toFile(res).toURL();
		}
		return ((FileResource) res).toURL();
	}

	public static PhysicalClassLoader getPhysicalClassLoader(Config c, Resource directory, boolean reload) throws IOException {
		boolean doesTrace = LogUtil.does(Log.LEVEL_TRACE);
		String key = HashUtil.create64BitHashAsString(directory.getAbsolutePath());

		CachedLoader cached = reload ? null : classLoaders.get(key);
		if (cached == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader", key)) {
				cached = reload ? null : classLoaders.get(key);
				if (cached == null) {
					// if we have a reload, clear the existing before set a new one
					if (reload) {
						CachedLoader existing = classLoaders.get(key);
						if (existing != null) PhysicalClassLoader.flush(existing.get(), c, false);
					}
					PhysicalClassLoader pcl = new PhysicalClassLoader(key, c, new ArrayList<Resource>(), directory, SystemUtil.getCoreClassLoader(), null, false);
					classLoaders.put(key, cached = new CachedLoader(pcl));
					if (doesTrace) LogUtil.log(Log.LEVEL_TRACE, "physical-classloader",
							"set new PhysicalClassLoader with key [" + key + "], there are now [" + classLoaders.size() + "] PhysicalClassLoaders loaded.");
					return pcl;
				}
			}
		}

		// at this point we know we had an existing one
		PhysicalClassLoader flushed = PhysicalClassLoader.flushIfNecessary(cached.get(), c);
		if (flushed != null) {
			classLoaders.put(key, cached = new CachedLoader(flushed));
			if (doesTrace) LogUtil.log(Log.LEVEL_TRACE, "physical-classloader",
					"set new PhysicalClassLoader with key [" + key + "], there are now [" + classLoaders.size() + "] PhysicalClassLoaders loaded.");
		}
		return cached.get();
	}

	public static boolean flush(Config c) {
		String key = key(c, ((ConfigPro) c).getJavaSettings(), null, SystemUtil.getCoreClassLoader());
		return classLoaders.remove(key) != null;
	}

	public static PhysicalClassLoader getRPCClassLoader(Config c, BundleClassLoader bcl, boolean reload) throws IOException {
		return getRPCClassLoader(c, null, bcl, SystemUtil.getCoreClassLoader(), reload);
	}

	public static PhysicalClassLoader getRPCClassLoader(Config c, JavaSettings js, boolean reload) throws IOException {
		return getRPCClassLoader(c, js, null, SystemUtil.getCoreClassLoader(), reload);
	}

	private static String key(Config c, JavaSettings js, BundleClassLoader bcl, ClassLoader parent) {
		String key = js == null ? "orphan" : ((JavaSettingsImpl) js).id();

		if (parent == null) parent = SystemUtil.getCoreClassLoader();
		if (parent instanceof PhysicalClassLoader) {
			key += ":" + ((PhysicalClassLoader) parent).id;
		}
		else if (parent instanceof BundleClassLoader) {
			key += ":" + OSGiUtil.createId((BundleClassLoader) parent);
		}
		else {
			key += ":" + parent.getClass().getName() + parent.hashCode();
		}

		if (bcl != null) {
			key += ":" + OSGiUtil.createId(bcl);
		}

		return HashUtil.create64BitHashAsString(key);
	}

	private static PhysicalClassLoader getRPCClassLoader(Config c, JavaSettings js, BundleClassLoader bcl, ClassLoader parent, boolean reload) throws IOException {
		boolean doesTrace = LogUtil.does(Log.LEVEL_TRACE);
		String key = key(c, js, bcl, parent);

		CachedLoader cached = reload ? null : classLoaders.get(key);
		if (cached == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader", key)) {
				cached = reload ? null : classLoaders.get(key);
				if (cached == null) {
					// if we have a reload, clear the existing before set a new one
					if (reload) {
						CachedLoader existing = classLoaders.get(key);
						if (existing != null) PhysicalClassLoader.flush(existing.get(), c, false);
					}
					List<Resource> resources;
					if (js == null) {
						resources = new ArrayList<Resource>();
					}
					else {
						resources = toSortedList(((JavaSettingsImpl) js).getAllResources());
					}
					Resource dir = storeResourceMeta(c, key, js, resources);
					PhysicalClassLoader pcl = new PhysicalClassLoader(key, c, resources, dir, parent, bcl, true);
					classLoaders.put(key, cached = new CachedLoader(pcl));
					if (doesTrace) LogUtil.log(Log.LEVEL_TRACE, "physical-classloader",
							"set new PhysicalClassLoader with key [" + key + "], there are now [" + classLoaders.size() + "] PhysicalClassLoaders loaded.");
					return pcl;
				}
			}
		}

		// at this point we know we had an existing one
		PhysicalClassLoader flushed = PhysicalClassLoader.flushIfNecessary(cached.get(), c);
		if (flushed != null) {
			classLoaders.put(key, cached = new CachedLoader(flushed));
			if (doesTrace) LogUtil.log(Log.LEVEL_TRACE, "physical-classloader",
					"set new PhysicalClassLoader with key [" + key + "], there are now [" + classLoaders.size() + "] PhysicalClassLoaders loaded.");
		}
		return cached.get();
	}

	/**
	 * Evicts classloaders that have not been accessed within the idle timeout window. Intended to be
	 * called periodically by the Lucee Controller thread.
	 */
	public static void clean(Config config) {
		boolean doesTrace = LogUtil.does(Log.LEVEL_TRACE);
		int sizeBefore = classLoaders.size();
		if (doesTrace) LogUtil.log(Log.LEVEL_TRACE, "physical-classloader",
				"clean called, checking " + sizeBefore + " PhysicalClassLoaders for idle timeout (>" + (IDLE_TIMEOUT_MS / 1000) + "s), min size threshold: " + IDLE_MINSIZE);

		if (sizeBefore <= IDLE_MINSIZE) {
			if (doesTrace) LogUtil.log(Log.LEVEL_TRACE, "physical-classloader", "clean skipped, size " + sizeBefore + " is within min size threshold " + IDLE_MINSIZE);
			return;
		}

		int evicted = 0;
		for (Map.Entry<String, CachedLoader> entry: classLoaders.entrySet()) {
			CachedLoader cached = entry.getValue();
			if (cached.isIdle() && !cached.loader.isRPC()) {
				// atomic remove guards against a race where the entry was just refreshed
				if (classLoaders.remove(entry.getKey(), cached)) {
					PhysicalClassLoader.flush(cached.loader, config, false);
					evicted++;
				}
			}
		}

		if (doesTrace) LogUtil.log(Log.LEVEL_TRACE, "physical-classloader",
				"clean finished, evicted " + evicted + " of " + sizeBefore + " PhysicalClassLoaders, remaining: " + classLoaders.size());
	}

	static Resource storeResourceMeta(Config config, String key, JavaSettings js, Collection<Resource> _resources) throws IOException {
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

	static List<Resource> toSortedList(Collection<Resource> resources) {
		List<Resource> list = new ArrayList<Resource>();
		if (resources != null) {
			for (Resource r: resources) {
				if (r != null) list.add(r);
			}
		}
		java.util.Collections.sort(list, rc);
		return list;
	}

	static List<Resource> toSortedList(Resource[] resources) {
		List<Resource> list = new ArrayList<Resource>();
		if (resources != null) {
			for (Resource r: resources) {
				if (r != null) list.add(r);
			}
		}
		java.util.Collections.sort(list, rc);
		return list;
	}

	private static class RC implements Comparator<Resource> {

		@Override
		public int compare(Resource l, Resource r) {
			return l.getAbsolutePath().compareTo(r.getAbsolutePath());
		}
	}

	private static class CachedLoader {
		final PhysicalClassLoader loader;
		volatile long lastAccess;

		CachedLoader(PhysicalClassLoader loader) {
			this.loader = loader;
			this.lastAccess = System.currentTimeMillis();
		}

		PhysicalClassLoader get() {
			this.lastAccess = System.currentTimeMillis();
			return this.loader;
		}

		boolean isIdle() {
			return (System.currentTimeMillis() - lastAccess) > IDLE_TIMEOUT_MS;
		}
	}
}