package lucee.runtime.osgi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.text.xml.XMLUtil;

public class EnvClassLoader extends URLClassLoader {

	private static float FROM_SYSTEM = 1;
	private static float FROM_BOOTDELEGATION = 2;
	private static float FROM_CALLER = 3;

	private static SoftReference<String> EMPTY = new SoftReference<String>(null);
	private static Map<SoftReference<String>, SoftReference<String>> notFound = new java.util.concurrent.ConcurrentHashMap<>();

	private Config config;
	private Map<String, SoftReference<Object[]>> callerCache = new ConcurrentHashMap<String, SoftReference<Object[]>>();
	private Log trace;

	private static final short CLASS = 1;
	private static final short URL = 2;
	private static final short STREAM = 3;

	private static ThreadLocal<Set<String>> checking = new ThreadLocal<Set<String>>() {
		@Override
		protected Set<String> initialValue() {
			return new HashSet<>();
		}
	};

	private static ThreadLocal<Boolean> inside = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	private static final EnvClassLoader NULL_INSTANCE = new EnvClassLoader(null);

	public static EnvClassLoader getInstance(ConfigPro config) {
		config = (ConfigPro) ThreadLocalPageContext.get(config);
		if (config != null) return (EnvClassLoader) config.getClassLoaderEnv();

		return NULL_INSTANCE;
	}

	public EnvClassLoader(ConfigPro config) {
		super(new URL[0], config != null ? config.getClassLoaderCore() : new lucee.commons.lang.ClassLoaderHelper().getClass().getClassLoader());
		this.config = config;
		this.trace = log(Log.LEVEL_TRACE);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	@Override
	public URL getResource(String name) {
		if (trace == null) {
			return (java.net.URL) load(name, URL, true, null, true);
		}
		double start = SystemUtil.millis();
		try {
			return (java.net.URL) load(name, URL, true, null, true);
		}
		finally {
			trace.trace("EnvClassLoader", "EnvClassLoader.getResource(" + name + "):" + (SystemUtil.millis() - start));
		}

	}

	@Override
	public InputStream getResourceAsStream(String name) {
		if (trace == null) {
			return (InputStream) load(name, STREAM, true, null, true);
		}
		double start = SystemUtil.millis();
		try {
			return (InputStream) load(name, STREAM, true, null, true);
		}
		finally {
			trace.trace("EnvClassLoader", "EnvClassLoader.getResourceAsStream(" + name + "):" + (SystemUtil.millis() - start));
		}
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {

		if (trace == null) {
			List<URL> list = new ArrayList<URL>();
			URL url = (URL) load(name, URL, false, null, true);
			if (url != null) list.add(url);
			return new E<URL>(list.iterator());
		}
		double start = SystemUtil.millis();
		try {
			List<URL> list = new ArrayList<URL>();
			URL url = (URL) load(name, URL, false, null, true);
			if (url != null) list.add(url);
			return new E<URL>(list.iterator());
		}
		finally {
			trace.trace("EnvClassLoader", "EnvClassLoader.getResources(" + name + "):" + (SystemUtil.millis() - start));
		}
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (trace == null) {
			Class<?> c = findLoadedClass(name);
			if (c == null) c = (Class<?>) load(name, CLASS, true, null, true);
			if (c == null) c = findClass(name);
			if (resolve) resolveClass(c);
			return c;
		}
		double start = SystemUtil.millis();
		try {
			Class<?> c = findLoadedClass(name);
			if (c == null) c = (Class<?>) load(name, CLASS, true, null, true);
			if (c == null) c = findClass(name);
			if (resolve) resolveClass(c);
			return c;
		}
		finally {
			trace.trace("EnvClassLoader", "EnvClassLoader.loadClass(" + name + "):" + (SystemUtil.millis() - start));
		}

	}

	private synchronized Object load(String name, short type, boolean doLog, List<ClassLoader> listContext, boolean useCache) {
		double start = SystemUtil.millis();

		StringBuilder id = new StringBuilder(name).append(';').append(type).append(';');
		String _id = id.toString();
		Set<String> cache = checking.get();
		if (useCache && cache.contains(_id)) {
			callerCache.put(id.toString(), new SoftReference<Object[]>(new Object[] { null }));
			return null;
		}
		try {
			cache.add(_id);

			if (listContext == null) {
				listContext = SystemUtil.getClassLoaderContext(true, id);
			}

			// PATCH XML
			if ((name + "").startsWith("META-INF/services") && !inside.get()) {
				inside.set(Boolean.TRUE);
				try {
					if (name.equalsIgnoreCase("META-INF/services/javax.xml.parsers.DocumentBuilderFactory")) {
						if (patchNeeded(name, doLog, listContext)) {
							if (type == URL) return XMLUtil.getDocumentBuilderFactoryResource();
							else if (type == STREAM) return new ByteArrayInputStream(XMLUtil.getDocumentBuilderFactoryName().getBytes());
						}
					}
					else if (name.equalsIgnoreCase("META-INF/services/javax.xml.parsers.SAXParserFactory")) {
						if (patchNeeded(name, doLog, listContext)) {
							if (type == URL) return XMLUtil.getSAXParserFactoryResource();
							else if (type == STREAM) return new ByteArrayInputStream(XMLUtil.getSAXParserFactoryName().getBytes());
						}
					}
					else if (name.equalsIgnoreCase("META-INF/services/javax.xml.transform.TransformerFactory")) {
						if (patchNeeded(name, doLog, listContext)) {
							if (type == URL) return XMLUtil.getTransformerFactoryResource();
							else if (type == STREAM) return new ByteArrayInputStream(XMLUtil.getTransformerFactoryName().getBytes());
						}
					}
					else if (name.equalsIgnoreCase("META-INF/services/org.apache.xerces.xni.parser.XMLParserConfiguration")) {
						if (patchNeeded(name, doLog, listContext)) {
							if (type == STREAM) return new ByteArrayInputStream(XMLUtil.getXMLParserConfigurationName().getBytes());
						}
					}
				}
				catch (IOException e) {
				}
				finally {
					inside.set(Boolean.FALSE);
				}
			}

			// PATCH for com.sun
			if ((name + "").startsWith("com.sun.")) {
				Object obj;
				ClassLoader loader = CFMLEngineFactory.class.getClassLoader();
				obj = _load(loader, name, type);
				if (obj != null) {
					if (trace != null) trace.trace("EnvClassLoader", "found [" + name + "] in loader ClassLoader");
					return obj;
				}
			}

			SoftReference<Object[]> sr = callerCache.get(id.toString());
			if (sr != null && sr.get() != null) {
				// print.e(name + " - from cache " + callerCache.size());
				return sr.get()[0];
			}
			// callers classloader context
			Object obj;
			for (ClassLoader cl: listContext) {
				obj = _load(cl, name, type);
				if (obj != null) {
					if (cl instanceof BundleReference) {
						if (trace != null) trace.trace("EnvClassLoader", "found [" + name + "] in bundle [" + (((BundleReference) cl).getBundle().getSymbolicName()) + ":"
								+ (((BundleReference) cl).getBundle().getVersion()) + "]");
					}
					else {
						if (trace != null) trace.trace("EnvClassLoader", "found [" + name + "] in System ClassLoader " + cl);
					}
					if (useCache) callerCache.put(id.toString(), new SoftReference<Object[]>(new Object[] { obj }));
					return obj;
				}
				else {
					if (cl instanceof BundleReference) {
						if (trace != null) trace.trace("EnvClassLoader", "not found [" + name + "] in bundle [" + (((BundleReference) cl).getBundle().getSymbolicName()) + ":"
								+ (((BundleReference) cl).getBundle().getVersion()) + "]");
					}
					else {
						if (trace != null) trace.trace("EnvClassLoader", "not found [" + name + "] in System ClassLoader " + cl);
					}

				}
			}
			// print.ds("4:" + (SystemUtil.millis() - start) + ":" + name);
			if (trace != null) trace.trace("EnvClassLoader", "not found [" + name + "] ");
			if (useCache) callerCache.put(id.toString(), new SoftReference<Object[]>(new Object[] { null }));
			return null;
		}
		finally {
			cache.remove(_id);
		}
	}

	private boolean patchNeeded(String name, boolean doLog, List<ClassLoader> listContext) throws IOException {
		Object o = load(name, STREAM, doLog, listContext, false);
		boolean patchIt = true;
		if (o instanceof InputStream) {
			String className = IOUtil.toString((InputStream) o, (Charset) null);
			o = StringUtil.isEmpty(className) ? null : load(className.trim(), CLASS, doLog, listContext, false);
			if (o != null) patchIt = false;
		}
		return patchIt;
	}

	private Object _load(ClassLoader cl, String name, short type) {
		Object obj = null;
		Bundle b = null;
		if (cl != null) {
			try {
				if (type == CLASS) {
					if (cl instanceof BundleReference) {
						b = ((BundleReference) cl).getBundle();
						if (notFound.containsKey(
								new SoftReference<String>(new StringBuilder(b.getSymbolicName()).append(':').append(b.getVersion()).append(':').append(name).toString())))
							return null;
						else obj = cl.loadClass(name);
					}
					else obj = cl.loadClass(name);
				}
				else if (type == URL) obj = cl.getResource(name);
				else obj = cl.getResourceAsStream(name);
			}
			catch (ClassNotFoundException cnfe) {
				if (b != null)
					notFound.put(new SoftReference<String>(new StringBuilder(b.getSymbolicName()).append(':').append(b.getVersion()).append(':').append(name).toString()), EMPTY);
			}
			catch (Exception e) {
			}

		}
		return obj;
	}

	private String toType(short type) {
		if (CLASS == type) return "class";
		if (STREAM == type) return "stream";
		return "url";
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		throw new ClassNotFoundException("class " + name + " not found in the core, the loader and all the extension bundles");
	}

	private static class E<T> implements Enumeration<T> {

		private Iterator<T> it;

		private E(Iterator<T> it) {
			this.it = it;
		}

		@Override
		public boolean hasMoreElements() {
			return it.hasNext();
		}

		@Override
		public T nextElement() {
			return it.next();
		}

	}

	//////////////////////////////////////////////////
	// URLClassloader methods, need to be supressed //
	//////////////////////////////////////////////////
	@Override
	public URL findResource(String name) {
		return getResource(name);
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		return getResources(name);
	}

	private Log log(int logLevel) {
		if (config == null) return null;
		Log log = config.getLog("application");
		if (log == null || log.getLogLevel() > logLevel) return null;
		return log;
	}
}