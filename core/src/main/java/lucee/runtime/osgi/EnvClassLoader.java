package lucee.runtime.osgi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.SystemUtil.Caller;
import lucee.commons.io.log.Log;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.text.xml.XMLUtil;

public class EnvClassLoader extends URLClassLoader {

	private static float FROM_SYSTEM = 1;
	private static float FROM_BOOTDELEGATION = 2;
	private static float FROM_CALLER = 3;

	private ConfigImpl config;
	// private Map<String, SoftReference<Coll>> callerCache=new ConcurrentHashMap<String,
	// SoftReference<Coll>>();

	private static final short CLASS = 1;
	private static final short URL = 2;
	private static final short STREAM = 3;

	public EnvClassLoader(ConfigImpl config) {
		super(new URL[0], config != null ? config.getClassLoaderCore() : new lucee.commons.lang.ClassLoaderHelper().getClass().getClassLoader());
		this.config = config;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	@Override
	public URL getResource(String name) {
		return (java.net.URL) load(name, URL, true);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream is = (InputStream) load(name, STREAM, true);
		if (is != null) return is;

		// PATCH
		if (name.equalsIgnoreCase("META-INF/services/org.apache.xerces.xni.parser.XMLParserConfiguration")) {
			return new ByteArrayInputStream(XMLUtil.getXMLParserConfigurationName().getBytes());
		}
		else if (name.equalsIgnoreCase("META-INF/services/javax.xml.parsers.DocumentBuilderFactory")) {
			return new ByteArrayInputStream(XMLUtil.getDocumentBuilderFactoryName().getBytes());
		}
		return null;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		List<URL> list = new ArrayList<URL>();
		URL url = (URL) load(name, URL, false);
		if (url != null) list.add(url);
		return new E<URL>(list.iterator());
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		log("check for class/resource [" + name + "]," + (resolve ? " " : " not ") + "resolve it", 0);

		Class<?> c = findLoadedClass(name);
		if (c == null) c = (Class<?>) load(name, CLASS, true);
		if (c == null) c = findClass(name);
		if (resolve) resolveClass(c);
		return c;
	}

	private synchronized Object load(String name, short type, boolean doLog) {
		double start = SystemUtil.millis();
		Object obj = null;
		// cache.get(name);

		// PATCH XML Meta data move to getResource*
		/*
		 * if (name.equalsIgnoreCase("META-INF/services/javax.xml.parsers.DocumentBuilderFactory")) { try {
		 * return XMLUtil.getDocumentBuilderFactoryResource(); } catch (IOException e) {
		 * e.printStackTrace(); } }
		 */

		// PATCH for com.sun
		if ((name + "").startsWith("com.sun.")) {
			// com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl
			// com.sun.org.apache.xerces.internal.parsers.SAXParser
			ClassLoader loader = CFMLEngineFactory.class.getClassLoader();
			obj = _load(loader, name, type);
			if (obj != null) {
				log("found [" + name + "] in loader ClassLoader", start);
				return obj;
			}
		}

		// first we check the callers classpath

		Caller caller = SystemUtil.getCallerClass();
		/// print.e("caller:" + (SystemUtil.millis() - start) + ":" + name);
		if (!caller.isEmpty()) {
			float from = 0;
			// if the request comes from classpath
			Class clazz = caller.fromClasspath();
			if (clazz != null) {
				if (clazz.getClassLoader() != null) {
					obj = _load(clazz.getClassLoader(), name, type);
					if (obj != null) from = FROM_SYSTEM;
					if (obj == null && clazz.getClassLoader() instanceof PhysicalClassLoader && clazz != caller.fromBootDelegation
							&& caller.fromBootDelegation != null & caller.fromBootDelegation.getClassLoader() != null) {
						obj = _load(caller.fromBootDelegation.getClassLoader(), name, type);
						if (obj != null) from = FROM_BOOTDELEGATION;
					}
				}
			}
			if (obj == null && caller.fromBundle != null) {
				if (caller.fromBundle.getClassLoader() != null) {
					obj = _load(caller.fromBundle.getClassLoader(), name, type);
					if (obj != null) {
						org.apache.felix.framework.BundleWiringImpl.BundleClassLoader bcl = null;
						/// print.e(caller.fromBundle.getClassLoader().getClass().getName());
						/// print.e(caller.fromBundle.getClassLoader().hashCode());
						from = FROM_CALLER;
					}
				}
			}
			if (obj != null) {
				/// print.e("1.1:" + (SystemUtil.millis() - start) + ":" + name);
				if (from == FROM_SYSTEM) log("found [" + name + "] in system classpath", start);
				else if (from == FROM_BOOTDELEGATION) log("found [" + name + "] in boot delegation", start);
				else if (from == FROM_CALLER) log("found [" + name + "] in callers bundle", start);

				return obj;
			}
		}
		/// print.e("1:" + (SystemUtil.millis() - start) + ":" + name);

		// now we check in the core for the class (this includes all jars loaded by the core)
		if ((caller.isEmpty() || caller.fromBundle != null) && caller.fromBundle.getClassLoader() != getParent()) {
			obj = _load(getParent(), name, type);
			if (obj != null) {
				/// print.e("2.1:" + (SystemUtil.millis() - start) + ":" + name);

				log("found [" + name + "] in core bundle (included all related bundles)", start);
				return obj;
			}
		}
		/// print.e("2:" + (SystemUtil.millis() - start) + ":" + name);

		// now we check extension bundles
		if (caller.isEmpty() || /* PATCH LDEV-1312 */(ThreadLocalPageContext.get() == null)/* if we are in a child threads */ || caller.fromBundle != null) {
			Bundle b = null;
			CFMLEngine engine = ConfigWebUtil.getEngine(config);
			if (engine != null) {
				BundleContext bc = engine.getBundleContext();
				if (bc != null) {
					Bundle[] bundles = bc.getBundles();
					for (int i = 0; i < bundles.length; i++) {
						b = bundles[i];
						if (b != null && !OSGiUtil.isFrameworkBundle(b)) {
							try {
								if (type == CLASS) obj = b.loadClass(name);
								else if (type == URL) obj = b.getResource(name);
								else {
									java.net.URL url = b.getResource(name);
									if (url != null) obj = url.openStream();
								}
								if (obj != null) break;
							}
							catch (Exception e) {
								obj = null;
								b = null;
							}
						}
					}
				}
			}
			if (obj != null) {
				/// print.e("3.1:" + (SystemUtil.millis() - start) + ":" + name);

				log("found [" + name + "] in bundle [" + (b != null ? (b.getSymbolicName() + ":" + b.getVersion().toString()) : "") + "]", start);
				// return obj;
			}
		}
		/// print.e("3:" + (SystemUtil.millis() - start) + ":" + name);

		if (caller.fromClasspath() != null) {
			ClassLoader loader = CFMLEngineFactory.class.getClassLoader();
			obj = _load(loader, name, type);
			if (obj != null) {
				log("found [" + name + "] in loader ClassLoader", start);
				return obj;
			}
		}
		/// print.e("4:" + (SystemUtil.millis() - start) + ":" + name);

		/*
		 * if(obj==null) { ClassLoader loader = CFMLEngineFactory.class.getClassLoader(); Object obj2 =
		 * _load(loader, name, type); if(obj2!=null) {
		 * //print.e("found in classpath but not used:"+name+"->"); } }
		 */
		log("not found [" + name + "] " + (obj != null), start);
		return obj;
	}

	private Object _load(ClassLoader cl, String name, short type) {
		Object obj = null;
		if (cl != null) {
			try {
				if (type == CLASS) obj = cl.loadClass(name);
				else if (type == URL) obj = cl.getResource(name);
				else obj = cl.getResourceAsStream(name);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
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

	private void log(String msg, double start) {
		Config c = ThreadLocalPageContext.getConfig(config);
		if (c == null) return;
		Log log = c.getLog("application");
		if (log == null) return;
		// System.err.println("--------- " + (start == 0 ? "" : "" + (SystemUtil.millis() - start)) + "
		// -------->" + msg);
		log.log(Log.LEVEL_TRACE, EnvClassLoader.class.getName(), msg);
	}
}