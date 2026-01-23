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
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.config.Config;
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

public class PhysicalClassLoaderFactory {
	private static final AtomicLong counter = new AtomicLong(0);
	private static long _start = 0L;
	private static String start = Long.toString(_start, Character.MAX_RADIX);
	private static Object countToken = new Object();

	private static RC rc = new RC();

	private static Map<String, PhysicalClassLoader> classLoaders = new ConcurrentHashMap<>();

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
		String key = HashUtil.create64BitHashAsString(directory.getAbsolutePath());

		PhysicalClassLoader rpccl = reload ? null : classLoaders.get(key);
		if (rpccl == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader", key)) {
				rpccl = reload ? null : classLoaders.get(key);
				if (rpccl == null) {
					// if we have a reload, clear the existing before set a new one
					if (reload) {
						PhysicalClassLoader existing = classLoaders.get(key);
						if (existing != null) PhysicalClassLoader.flush(existing, c);
					}
					classLoaders.put(key, rpccl = new PhysicalClassLoader(key, c, new ArrayList<Resource>(), directory, SystemUtil.getCoreClassLoader(), null, false));
					return rpccl;
				}
			}
		}

		// at this point we know we had an existing one
		PhysicalClassLoader flushed = PhysicalClassLoader.flushIfNecessary(rpccl, c);
		if (flushed != null) {
			classLoaders.put(key, rpccl = flushed);
		}
		return rpccl;
	}

	public static PhysicalClassLoader getRPCClassLoader(Config c, BundleClassLoader bcl, boolean reload) throws IOException {
		return getRPCClassLoader(c, null, bcl, SystemUtil.getCoreClassLoader(), reload);
	}

	public static PhysicalClassLoader getRPCClassLoader(Config c, JavaSettings js, boolean reload) throws IOException {
		return getRPCClassLoader(c, js, null, SystemUtil.getCoreClassLoader(), reload);
	}

	private static PhysicalClassLoader getRPCClassLoader(Config c, JavaSettings js, BundleClassLoader bcl, ClassLoader parent, boolean reload) throws IOException {
		String key = js == null ? "orphan" : ((JavaSettingsImpl) js).id();

		if (parent == null) parent = SystemUtil.getCoreClassLoader();
		if (parent instanceof PhysicalClassLoader) {
			key += ":" + ((PhysicalClassLoader) parent).id;
		}
		else {
			key += ":" + parent.getClass().getName() + parent.hashCode();
		}

		if (bcl != null) {
			key += ":" + bcl;
		}
		key = HashUtil.create64BitHashAsString(key);

		PhysicalClassLoader rpccl = reload ? null : classLoaders.get(key);
		if (rpccl == null) {
			synchronized (SystemUtil.createToken("PhysicalClassLoader", key)) {
				rpccl = reload ? null : classLoaders.get(key);
				if (rpccl == null) {
					// if we have a reload, clear the existing before set a new one
					if (reload) {
						PhysicalClassLoader existing = classLoaders.get(key);
						if (existing != null) PhysicalClassLoader.flush(existing, c);
					}
					List<Resource> resources;
					if (js == null) {
						resources = new ArrayList<Resource>();
					}
					else {
						resources = toSortedList(((JavaSettingsImpl) js).getAllResources());
					}
					Resource dir = storeResourceMeta(c, key, js, resources);
					classLoaders.put(key, rpccl = new PhysicalClassLoader(key, c, resources, dir, parent, bcl, true));
					return rpccl;
				}
			}
		}

		// at this point we know we had an existing one
		PhysicalClassLoader flushed = PhysicalClassLoader.flushIfNecessary(rpccl, c);
		if (flushed != null) {
			classLoaders.put(key, rpccl = flushed);
		}
		return rpccl;
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
}
