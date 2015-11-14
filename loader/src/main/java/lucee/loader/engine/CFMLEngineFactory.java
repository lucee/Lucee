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
package lucee.loader.engine;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lucee.VersionInfo;
import lucee.loader.TP;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.osgi.BundleLoader;
import lucee.loader.osgi.BundleUtil;
import lucee.loader.osgi.LoggerImpl;
import lucee.loader.util.ExtensionFilter;
import lucee.loader.util.Util;
import lucee.loader.util.ZipUtil;
import lucee.runtime.config.Identification;
import lucee.runtime.config.Password;
import lucee.runtime.util.Pack200Util;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.intergral.fusiondebug.server.FDControllerFactory;

/**
 * Factory to load CFML Engine
 */
public class CFMLEngineFactory extends CFMLEngineFactorySupport {

	// set to false to disable patch loading, for example in major alpha releases
	private static final boolean PATCH_ENABLED = true;
	public static final Version VERSION_ZERO = new Version(0, 0, 0, "0");
	private static final String UPDATE_LOCATION = "http://release.lucee.org"; // MUST from server.xml


	private static CFMLEngineFactory factory;
	//private static  CFMLEngineWrapper engineListener;
	private static CFMLEngineWrapper singelton;

	private static File luceeServerRoot;

	private Felix felix;
	private BundleCollection bundleCollection;
	//private CFMLEngineWrapper engine;

	private final ClassLoader mainClassLoader = new TP().getClass()
			.getClassLoader();
	private Version version;
	private final List<EngineChangeListener> listeners = new ArrayList<EngineChangeListener>();
	private File resourceRoot;

	//private PrintWriter out;

	private final LoggerImpl logger;

	protected ServletConfig config;

	/**
	 * Constructor of the class
	 */
	protected CFMLEngineFactory(final ServletConfig config) {
		File logFile = null;
		this.config = config;
		try {
			logFile = new File(getResourceRoot(), "context/logs/felix.log");
		} catch (final IOException e) {
			e.printStackTrace();
		}
		logFile.getParentFile().mkdirs();
		logger = new LoggerImpl(logFile);
	}

	/**
	 * returns instance of this factory (singelton = always the same instance)
	 * do auto update when changes occur
	 * 
	 * @param config
	 * @return Singelton Instance of the Factory
	 * @throws ServletException
	 */
	public static CFMLEngine getInstance(final ServletConfig config)
			throws ServletException {

		if (singelton != null) {
			if (factory == null)
				factory = singelton.getCFMLEngineFactory(); // not sure if this ever is done, but it does not hurt
			return singelton;
		}

		if (factory == null)
			factory = new CFMLEngineFactory(config);

		// read init param from config
		factory.readInitParam(config);

		factory.initEngineIfNecessary();
		singelton.addServletConfig(config);

		// add listener for update
		//factory.addListener(singelton);
		return singelton;
	}

	/**
	 * returns instance of this factory (singelton = always the same instance)
	 * do auto update when changes occur
	 * 
	 * @return Singelton Instance of the Factory
	 * @throws RuntimeException
	 */
	public static CFMLEngine getInstance() throws RuntimeException {
		if (singelton != null)
			return singelton;
		throw new RuntimeException(
				"engine is not initalized, you must first call getInstance(ServletConfig)");
	}

	public static void registerInstance(final CFMLEngine engine) {
		if (engine instanceof CFMLEngineWrapper)
			throw new RuntimeException("that should not happen!");
		setEngine(engine);
	}

	/**
	 * returns instance of this factory (singelton always the same instance)
	 * 
	 * @param config
	 * @param listener
	 * @return Singelton Instance of the Factory
	 * @throws ServletException
	 */
	public static CFMLEngine getInstance(final ServletConfig config,
			final EngineChangeListener listener) throws ServletException {
		getInstance(config);

		// add listener for update
		factory.addListener(listener);

		// read init param from config
		factory.readInitParam(config);

		factory.initEngineIfNecessary();
		singelton.addServletConfig(config);

		// make the FDController visible for the FDClient
		FDControllerFactory.makeVisible();

		return singelton;
	}

	void readInitParam(final ServletConfig config) {
		if (luceeServerRoot != null)
			return;

		String initParam = config.getInitParameter("lucee-server-directory");
		if (Util.isEmpty(initParam))
			initParam = config.getInitParameter("lucee-server-root");
		if (Util.isEmpty(initParam))
			initParam = config.getInitParameter("lucee-server-dir");
		if (Util.isEmpty(initParam))
			initParam = config.getInitParameter("lucee-server");
		if (Util.isEmpty(initParam))
			initParam = System.getProperty("lucee.server.dir");

		initParam = parsePlaceHolder(removeQuotes(initParam, true));
		try {
			if (!Util.isEmpty(initParam)) {
				final File root = new File(initParam);
				if (!root.exists()) {
					if (root.mkdirs()) {
						luceeServerRoot = root.getCanonicalFile();
						return;
					}
				} else if (root.canWrite()) {
					luceeServerRoot = root.getCanonicalFile();
					return;
				}
			}
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * adds a listener to the factory that will be informed when a new engine
	 * will be loaded.
	 * 
	 * @param listener
	 */
	private void addListener(final EngineChangeListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * @throws ServletException
	 */
	private void initEngineIfNecessary() throws ServletException {
		if (singelton == null)
			initEngine();
	}

	private void initEngine() throws ServletException {
		final Version coreVersion = VersionInfo.getIntVersion();
		final long coreCreated = VersionInfo.getCreateTime();

		// get newest lucee version as file
		File patcheDir = null;
		try {
			patcheDir = getPatchDirectory();
			log(Logger.LOG_DEBUG, "lucee-server-root:" + patcheDir.getParent());
		} catch (final IOException e) {
			throw new ServletException(e);
		}

		final File[] patches = PATCH_ENABLED ? patcheDir
				.listFiles(new ExtensionFilter(new String[] { ".lco" })) : null;
		File lucee = null;
		if (patches != null)
			for (final File patche : patches)
				if (patche.getName().startsWith("tmp.lco"))
					patche.delete();
				else if (patche.lastModified() < coreCreated)
					patche.delete();
				else if (lucee == null
						|| Util.isNewerThan(
								toVersion(patche.getName(), VERSION_ZERO),
								toVersion(lucee.getName(), VERSION_ZERO)))
					lucee = patche;
		if (lucee != null
				&& Util.isNewerThan(coreVersion,
						toVersion(lucee.getName(), VERSION_ZERO)))
			lucee = null;

		// Load Lucee
		//URL url=null;
		try {
			// Load core version when no patch available
			if (lucee == null) {
				log(Logger.LOG_DEBUG, "Load Build in Core");
				// 
				final String coreExt = "lco";
				setEngine(getCore());

				lucee = new File(patcheDir, singelton.getInfo().getVersion()
						.toString()
						+ "." + coreExt);
				if (PATCH_ENABLED) {
					final InputStream bis = new TP().getClass()
							.getResourceAsStream("/core/core." + coreExt);
					final OutputStream bos = new BufferedOutputStream(
							new FileOutputStream(lucee));
					copy(bis, bos);
					closeEL(bis);
					closeEL(bos);
				}
			} else {

				bundleCollection = BundleLoader.loadBundles(this,
						getFelixCacheDirectory(), getBundleDirectory(), lucee,
						bundleCollection);
				//bundle=loadBundle(lucee);
				log(Logger.LOG_DEBUG,
						"loaded bundle:"
								+ bundleCollection.core.getSymbolicName());
				setEngine(getEngine(bundleCollection));
				log(Logger.LOG_DEBUG, "loaded engine:" + singelton);
			}
			version = singelton.getInfo().getVersion();

			log(Logger.LOG_DEBUG, "Loaded Lucee Version "
					+ singelton.getInfo().getVersion());
		} catch (final InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			throw new ServletException(e.getTargetException());
		} catch (final Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

		//check updates
		String updateType = singelton.getUpdateType();
		if (updateType == null || updateType.length() == 0)
			updateType = "manuell";

		if (updateType.equalsIgnoreCase("auto"))
			new UpdateChecker(this, null).start();

	}

	private static CFMLEngineWrapper setEngine(final CFMLEngine engine) {
		//new RuntimeException("setEngine").printStackTrace();
		if (singelton == null)
			singelton = new CFMLEngineWrapper(engine);
		else if (!singelton.isIdentical(engine)) {
			singelton.setEngine(engine); // reset of the old is made before
		} else {
			//new RuntimeException("useless call").printStackTrace();
		}

		return singelton;
	}

	public Felix getFelix(final File cacheRootDir, Map<String, Object> config)
			throws BundleException {

		if (config == null)
			config = new HashMap<String, Object>();

		// Log Level
		int logLevel = 1; // 1 = error, 2 = warning, 3 = information, and 4 = debug
		final String strLogLevel = (String) config.get("felix.log.level");
		if (!Util.isEmpty(strLogLevel))
			if ("warn".equalsIgnoreCase(strLogLevel)
					|| "warning".equalsIgnoreCase(strLogLevel)
					|| "2".equalsIgnoreCase(strLogLevel))
				logLevel = 2;
			else if ("info".equalsIgnoreCase(strLogLevel)
					|| "information".equalsIgnoreCase(strLogLevel)
					|| "3".equalsIgnoreCase(strLogLevel))
				logLevel = 3;
			else if ("debug".equalsIgnoreCase(strLogLevel)
					|| "4".equalsIgnoreCase(strLogLevel))
				logLevel = 4;
		config.put("felix.log.level", "" + logLevel);

		// storage clean
		final String storageClean = (String) config
				.get(Constants.FRAMEWORK_STORAGE_CLEAN);
		if (Util.isEmpty(storageClean))
			config.put(Constants.FRAMEWORK_STORAGE_CLEAN,
					Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		// parent classLoader
		final String parentClassLoader = (String) config
				.get(Constants.FRAMEWORK_BUNDLE_PARENT);
		if (Util.isEmpty(parentClassLoader))
			config.put(Constants.FRAMEWORK_BUNDLE_PARENT,
					Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
		else
			config.put(Constants.FRAMEWORK_BUNDLE_PARENT,
					BundleUtil.toFrameworkBundleParent(parentClassLoader));

		// felix.cache.rootdir
		if (!cacheRootDir.exists())
			cacheRootDir.mkdirs();
		if (cacheRootDir.isDirectory())
			config.put("felix.cache.rootdir", cacheRootDir.getAbsolutePath());

		if (logger != null)
			config.put("felix.log.logger", logger);
		// TODO felix.log.logger 

		// remove any empty record, this can produce trouble
		{
			final Iterator<Entry<String, Object>> it = config.entrySet()
					.iterator();
			Entry<String, Object> e;
			Object v;
			while (it.hasNext()) {
				e = it.next();
				v = e.getValue();
				if (v == null || v.toString().isEmpty())
					it.remove();
			}
		}

		final StringBuilder sb = new StringBuilder("loading felix with config:");
		final Iterator<Entry<String, Object>> it = config.entrySet().iterator();
		Entry<String, Object> e;
		while (it.hasNext()) {
			e = it.next();
			sb.append("\n- ").append(e.getKey()).append(':')
					.append(e.getValue());
		}
		log(Logger.LOG_INFO, sb.toString());

		felix = new Felix(config);
		felix.start();

		return felix;
	}

	public void log(final Throwable t) {
		if (logger != null)
			logger.log(Logger.LOG_ERROR, "", t);
	}

	public void log(final int level, final String msg) {
		if (logger != null)
			logger.log(level, msg);
	}

	private CFMLEngine getCore() throws IOException, BundleException,
			ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		final File rc = new File(getTempDirectory(), "tmp_"
				+ System.currentTimeMillis() + ".lco");
		try {
			InputStream is = null;
			OutputStream os = null;
			try {
				is = new TP().getClass().getResourceAsStream("/core/core.lco");
				os = new FileOutputStream(rc);
				copy(is, os);

			} finally {
				closeEL(is);
				closeEL(os);
			}
			bundleCollection = BundleLoader.loadBundles(this,
					getFelixCacheDirectory(), getBundleDirectory(), rc,
					bundleCollection);
			return getEngine(bundleCollection);
		} finally {
			rc.delete();
		}
	}

	/**
	 * method to initalize a update of the CFML Engine.
	 * checks if there is a new Version and update it whwn a new version is
	 * available
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean update(final Password password, final Identification id)
			throws IOException, ServletException {
		if (!singelton.can(CFMLEngine.CAN_UPDATE, password))
			throw new IOException("access denied to update CFMLEngine");
		//new RunUpdate(this).start();
		return _update(id);
	}

	/**
	 * restart the cfml engine
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean restart(final Password password) throws IOException,
			ServletException {
		if (!singelton.can(CFMLEngine.CAN_RESTART_ALL, password))
			throw new IOException("access denied to restart CFMLEngine");

		return _restart();
	}

	/**
	 * restart the cfml engine
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean restart(final String configId, final Password password)
			throws IOException, ServletException {
		if (!singelton.can(CFMLEngine.CAN_RESTART_CONTEXT, password))// TODO restart single context
			throw new IOException(
					"access denied to restart CFML Context (configId:"
							+ configId + ")");

		return _restart();
	}

	/**
	 * restart the cfml engine
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	private synchronized boolean _restart() throws ServletException {
		if(singelton!=null)
			singelton.reset();
		
		initEngine();
		System.gc();
		return true;
	}

	/**
	 * updates the engine when a update is available
	 * 
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	private boolean _update(final Identification id) throws IOException,
			ServletException {
		
		if(singelton!=null)
			singelton.reset();
		
		final File newLucee = downloadCore(id);
		if (newLucee == null)
			return false;


		final Version v = null;
		try {

			bundleCollection = BundleLoader.loadBundles(this,
					getFelixCacheDirectory(), getBundleDirectory(), newLucee,
					bundleCollection);
			final CFMLEngine e = getEngine(bundleCollection);
			if (e == null)
				throw new IOException("can't load engine");
			version = e.getInfo().getVersion();
			//engine = e;
			setEngine(e);
			//e.reset();
			callListeners(e);
		} catch (final Exception e) {
			System.gc();
			try {
				newLucee.delete();
			} catch (final Exception ee) {
			}
			log(e);
			e.printStackTrace();
			return false;
		}

		log(Logger.LOG_DEBUG, "Version (" + v + ")installed");
		return true;
	}

	public File downloadBundle(final String symbolicName,
			final String symbolicVersion, Identification id) throws IOException {
		
		final File jarDir = getBundleDirectory();
		
		// before we download we check if we have it bundled
		File jar = deployBundledBundle(jarDir,symbolicName,symbolicVersion);
		if(jar!=null && jar.isFile()) return jar;
		
		
		jar = new File(jarDir, symbolicName.replace('.', '-') + "-"
				+ symbolicVersion.replace('.', '-') + (".jar"));

		final URL updateProvider = getUpdateLocation();
		if (id == null && singelton != null)
			id = singelton.getIdentification();

		final URL updateUrl = new URL(updateProvider,
				"/rest/update/provider/download/" + symbolicName + "/"
						+ symbolicVersion + "/"
						+ (id != null ? id.toQueryString() : "")
						+ (id == null ? "?" : "&")+"allowRedirect=true"
						
				);
		System.out.println("download " + symbolicName + ":" + symbolicVersion +" from "+updateUrl+" and copy to "+jar); // MUST remove
		log(Logger.LOG_DEBUG, "download bundle [" + symbolicName + ":"+ symbolicVersion + "] from " + updateUrl+" and copy to "+jar);

		int code;
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) updateUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			code = conn.getResponseCode();
		} catch (final UnknownHostException e) {
			log(e);
			throw e;
		}
		//System.out.println("SC:" + code+"->"+conn.getFollowRedirects());
		// the update provider is not providing a download for this
		if (code != 200) {
			
			// the update provider can also provide a different (final) location for this
			if(code==302) {
				String location = conn.getHeaderField("Location");
				// just in case we check invalid names
				if(location==null)location = conn.getHeaderField("location");
				if(location==null)location = conn.getHeaderField("LOCATION");
				System.out.println("download redirected:" + location); // MUST remove
				
				conn.disconnect();
				URL url = new URL(location);
				try {
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.connect();
					code = conn.getResponseCode();
				} catch (final UnknownHostException e) {
					
					log(e);
					throw e;
				}
				
			}
			
			// no download available!
			if(code != 200){
				final String msg = "Lucee is not able do download the bundle for ["
					+ symbolicName + "] in version [" + symbolicVersion
					+ "] from " + updateUrl
					+ ", please donwload manually and copy to [" + jarDir + "]";
				log(Logger.LOG_ERROR, msg);
				conn.disconnect();
				throw new IOException(msg);
			}
			
		}

		//if(jar.createNewFile()) {	
		copy((InputStream) conn.getContent(), new FileOutputStream(jar));
		conn.disconnect();
		return jar;
		/*}
		else {
			throw new IOException("File ["+jar.getName()+"] already exists, won't copy new one");
		}*/
	}
	
	private File deployBundledBundle(File bundleDirectory, String symbolicName, String symbolicVersion) {
		String sub="bundles/";
		String nameAndVersion=symbolicName+"|"+symbolicVersion;
		
		ZipEntry entry;
		ZipInputStream zis = null;
		try {
			CodeSource src = CFMLEngineFactory.class.getProtectionDomain().getCodeSource();
			if (src == null) return null;
			URL loc = src.getLocation();
			
			zis=new ZipInputStream(loc.openStream());
			String path,name,bundleInfo;
			int index;
			File temp;
			boolean isPack200;
			while ((entry = zis.getNextEntry())!= null) {
				temp=null;
				path = entry.getName().replace('\\', '/');
				if(path.startsWith("/")) path=path.substring(1); // some zip path start with "/" some not
				isPack200=false;
				if(path.startsWith(sub) && (path.endsWith(".jar") || (isPack200=path.endsWith(".jar.pack.gz")))) { // ignore non jar files or file from elsewhere
					index=path.lastIndexOf('/')+1;
					if(index==sub.length()) { // ignore sub directories
						name=path.substring(index);
						temp=null;
						try {
							temp=File.createTempFile("bundle", ".tmp");
							Util.copy(zis, new FileOutputStream(temp),false,true);
							
							if(isPack200) {
								File temp2 = File.createTempFile("bundle", ".tmp2");
								Pack200Util.pack2Jar(temp, temp2);
								temp.delete();
								temp=temp2;
								name=name.substring(0,name.length()-".pack.gz".length());
							}
							
							bundleInfo=BundleLoader.loadBundleInfo(temp);
							if(bundleInfo!=null && nameAndVersion.equals(bundleInfo)) {
								File trg=new File(bundleDirectory,name);
								temp.renameTo(trg);
								System.out.println("adding bundle ["+symbolicName+"] in version ["+symbolicVersion+"] to ["+trg+"]");
								log(Logger.LOG_DEBUG, "adding bundle ["+symbolicName+"] in version ["+symbolicVersion+"] to ["+trg+"]");

								return trg;
							}
						}
						finally {
							if(temp!=null && temp.exists())temp.delete();
						}
						
					}
				}
				zis.closeEntry();
			} 
		}
		catch(Throwable t){
			t.printStackTrace();// TODO log this
		}
		finally {
			Util.closeEL(zis);
		}
		return null;
	}

	private File downloadCore(Identification id) throws IOException {
		final URL updateProvider = getUpdateLocation();

		if (id == null && singelton != null)
			id = singelton.getIdentification();
		
		// only happens when the code runs from the debug project
		if(version==null) version=getInstance().getInfo().getVersion();
		
		final URL infoUrl = new URL(updateProvider,
				"/rest/update/provider/update-for/" + version.toString()
						+ (id != null ? id.toQueryString() : ""));

		log(Logger.LOG_DEBUG, "Check for update at " + updateProvider);

		String strAvailableVersion = toString(
				(InputStream) infoUrl.getContent()).trim();
		log(Logger.LOG_DEBUG,
				"receive available update version from update provider ("
						+ strAvailableVersion + ") ");

		strAvailableVersion = CFMLEngineFactorySupport.removeQuotes(strAvailableVersion, true);
							  
		if (strAvailableVersion.length() == 0
				|| !Util.isNewerThan(toVersion(strAvailableVersion, VERSION_ZERO),
						version)) {
			log(Logger.LOG_DEBUG, "There is no newer Version available");
			return null;
		}

		log(Logger.LOG_DEBUG,
				"Found a newer Version \n - current Version "
						+ version.toString()
						+ "\n - available Version "
						+ strAvailableVersion);

		final URL updateUrl = new URL(updateProvider,
				"/rest/update/provider/download/" + strAvailableVersion
						+ (id != null ? id.toQueryString() : "")
						+ (id == null ? "?" : "&")+"allowRedirect=true"
				);
		log(Logger.LOG_DEBUG, "download update from " + updateUrl);
		System.out.println(updateUrl);
		
		
		// local resource
		final File patchDir = getPatchDirectory();
		final File newLucee = new File(patchDir, strAvailableVersion + (".lco"));
		////
		
		
		
		int code;
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) updateUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			code = conn.getResponseCode();
		} catch (final UnknownHostException e) {
			log(e);
			throw e;
		}
		
		// the update provider is not providing a download for this
		if (code != 200) {
			
			// the update provider can also provide a different (final) location for this
			if(code==302) {
				String location = conn.getHeaderField("Location");
				// just in case we check invalid names
				if(location==null)location = conn.getHeaderField("location");
				if(location==null)location = conn.getHeaderField("LOCATION");
				System.out.println("download redirected:" + location); // MUST remove
				log(Logger.LOG_DEBUG, "download redirected to " + updateUrl);
				
				
				conn.disconnect();
				URL url = new URL(location);
				try {
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.connect();
					code = conn.getResponseCode();
				} catch (final UnknownHostException e) {
					log(e);
					throw e;
				}
			}
			
			// no download available!
			if(code != 200){
				final String msg = "Lucee is not able do download the core for version ["
					+ version.toString() + "] from " + updateUrl
					+ ", please donwload it manually and copy to [" + patchDir + "]";
				log(Logger.LOG_ERROR, msg);
				conn.disconnect();
				throw new IOException(msg);
			}
		}
		
		// copy it to local directory
		if (newLucee.createNewFile()) {
			copy((InputStream) conn.getContent(), new FileOutputStream(newLucee));
			conn.disconnect();
			
			// when it is a loader extract the core from it
			File tmp = extractCoreIfLoader(newLucee);
			if(tmp!=null) {
				System.out.println("extract core from loader"); // MUST remove
				log(Logger.LOG_DEBUG, "extract core from loader");
				
				newLucee.delete();
				tmp.renameTo(newLucee);
				tmp.delete();
				System.out.println("exist?"+newLucee.exists()); // MUST remove
				
			}
		}
		else {
			conn.disconnect();
			log(Logger.LOG_DEBUG,
					"File for new Version already exists, won't copy new one");
			return null;
		}
		return newLucee;
	}

	public static File extractCoreIfLoader(File file) {
		try {
			return _extractCoreIfLoader(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static File _extractCoreIfLoader(File file) throws IOException {
		JarFile jf = new JarFile(file);
		try{
			// is it a lucee loader ?
			String value = jf.getManifest().getMainAttributes().getValue("Main-Class");
			if(Util.isEmpty(value) || !value.equals("lucee.runtime.script.Main")) return null;
			
			// get the core file;
			JarEntry je = jf.getJarEntry("core/core.lco");
			if(je==null) return null;
			
			InputStream is = jf.getInputStream(je);
			File trg = File.createTempFile("lucee", ".lco");
			OutputStream os=new FileOutputStream(trg);
			try{
				Util.copy(is, os);
			}
			finally {
				Util.closeEL(is);
				Util.closeEL(os);
			}
			
			return trg;
		}
		finally {
			jf.close();
		}
	}

	public URL getUpdateLocation() throws MalformedURLException {
		URL location = singelton == null ? null : singelton.getUpdateLocation();

		// read location directly from xml
		if (location == null) {
			final InputStream is = null;

			try {
				final File xml = new File(getResourceRoot(),
						"context/lucee-server.xml");
				if (xml.exists() || xml.length() > 0) {
					final DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();
					final DocumentBuilder dBuilder = dbFactory
							.newDocumentBuilder();
					final Document doc = dBuilder.parse(xml);
					final Element root = doc.getDocumentElement();

					final NodeList children = root.getChildNodes();

					for (int i = children.getLength() - 1; i >= 0; i--) {
						final Node node = children.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE
								&& node.getNodeName().equals("update")) {
							final String loc = ((Element) node)
									.getAttribute("location");
							if (!Util.isEmpty(loc))
								location = new URL(loc);
						}
					}
				}

			} catch (final Throwable t) {
				t.printStackTrace();
			} finally {
				CFMLEngineFactorySupport.closeEL(is);
			}
		}

		// if there is no lucee-server.xml
		if (location == null)
			location = new URL(UPDATE_LOCATION);

		return location;
	}

	/**
	 * method to initalize a update of the CFML Engine.
	 * checks if there is a new Version and update it whwn a new version is
	 * available
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean removeUpdate(final Password password) throws IOException,
			ServletException {
		if (!singelton.can(CFMLEngine.CAN_UPDATE, password))
			throw new IOException("access denied to update CFMLEngine");
		return removeUpdate();
	}

	/**
	 * method to initalize a update of the CFML Engine.
	 * checks if there is a new Version and update it whwn a new version is
	 * available
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean removeLatestUpdate(final Password password)
			throws IOException, ServletException {
		if (!singelton.can(CFMLEngine.CAN_UPDATE, password))
			throw new IOException("access denied to update CFMLEngine");
		return removeLatestUpdate();
	}

	/**
	 * updates the engine when a update is available
	 * 
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	private boolean removeUpdate() throws IOException, ServletException {
		final File patchDir = getPatchDirectory();
		final File[] patches = patchDir.listFiles(new ExtensionFilter(
				new String[] { "rc", "rcs" }));

		for (int i = 0; i < patches.length; i++)
			if (!patches[i].delete())
				patches[i].deleteOnExit();
		_restart();
		return true;
	}

	private boolean removeLatestUpdate() throws IOException, ServletException {
		final File patchDir = getPatchDirectory();
		final File[] patches = patchDir.listFiles(new ExtensionFilter(
				new String[] { ".lco" }));
		File patch = null;
		for (final File patche : patches)
			if (patch == null
					|| Util.isNewerThan(toVersion(patche.getName(), VERSION_ZERO),
							toVersion(patch.getName(), VERSION_ZERO)))
				patch = patche;
		if (patch != null && !patch.delete())
			patch.deleteOnExit();

		_restart();
		return true;
	}

	public String[] getInstalledPatches() throws ServletException, IOException {
		final File patchDir = getPatchDirectory();
		final File[] patches = patchDir.listFiles(new ExtensionFilter(
				new String[] { ".lco" }));

		final List<String> list = new ArrayList<String>();
		String name;
		final int extLen = "rc".length() + 1;
		for (final File patche : patches) {
			name = patche.getName();
			name = name.substring(0, name.length() - extLen);
			list.add(name);
		}
		final String[] arr = list.toArray(new String[list.size()]);
		Arrays.sort(arr);
		return arr;
	}

	/**
	 * call all registred listener for update of the engine
	 * 
	 * @param engine
	 */
	private void callListeners(final CFMLEngine engine) {
		final Iterator<EngineChangeListener> it = listeners.iterator();
		while (it.hasNext())
			it.next().onUpdate();
	}

	public File getPatchDirectory() throws IOException {
		File pd = getDirectoryByProp("lucee.patches.dir");
		if (pd != null)
			return pd;

		pd = new File(getResourceRoot(), "patches");
		if (!pd.exists())
			pd.mkdirs();
		return pd;
	}

	public File getBundleDirectory() throws IOException {
		File bd = getDirectoryByProp("lucee.bundles.dir");
		if (bd != null)
			return bd;

		bd = new File(getResourceRoot(), "bundles");
		if (!bd.exists())
			bd.mkdirs();
		return bd;
	}

	public File getFelixCacheDirectory() throws IOException {
		return getResourceRoot();
		//File bd = new File(getResourceRoot(),"felix-cache");
		//if(!bd.exists())bd.mkdirs();
		//return bd;
	}

	/**
	 * return directory to lucee resource root
	 * 
	 * @return lucee root directory
	 * @throws IOException
	 */
	public File getResourceRoot() throws IOException {
		if (resourceRoot == null) {
			resourceRoot = new File(_getResourceRoot(), "lucee-server");
			if (!resourceRoot.exists())
				resourceRoot.mkdirs();
		}
		return resourceRoot;
	}

	/**
	 * @return return running context root
	 * @throws IOException
	 * @throws IOException
	 */
	private File _getResourceRoot() throws IOException {

		// custom configuration
		if (luceeServerRoot == null)
			readInitParam(config);
		if (luceeServerRoot != null)
			return luceeServerRoot;

		File lbd = getDirectoryByProp("lucee.base.dir"); // directory defined by the caller
		if(lbd==null) lbd = getDirectoryByEnv("lucee.base.dir"); // directory defined by the caller
		
		File root=lbd;
		// get the root directory
		if (root == null)
			root = getDirectoryByProp("jboss.server.home.dir"); // Jboss/Jetty|Tomcat 
		if (root == null)
			root = getDirectoryByProp("jonas.base"); // Jonas
		if (root == null)
			root = getDirectoryByProp("catalina.base"); // Tomcat
		if (root == null)
			root = getDirectoryByProp("jetty.home"); // Jetty
		if (root == null)
			root = getDirectoryByProp("org.apache.geronimo.base.dir"); // Geronimo
		if (root == null)
			root = getDirectoryByProp("com.sun.aas.instanceRoot"); // Glassfish
		if (root == null)
			root = getDirectoryByProp("env.DOMAIN_HOME"); // weblogic
		if (root == null)
			root = getClassLoaderRoot(mainClassLoader).getParentFile()
					.getParentFile();

		
		final File classicRoot = getClassLoaderRoot(mainClassLoader);
		
		// in case of a war file the server root need to be with the context
		if(lbd==null) {
			File webInf=getWebInfFolder(classicRoot);
			if(webInf!=null) {
				root=webInf;
				if(!root.exists())root.mkdir();
				System.out.println("war-root-directory:" + root);
			}
		}
		
		System.out.println("root-directory:" + root);
		
		if (root == null)
			throw new IOException(
				 "can't locate the root of the servlet container, please define a location (physical path) for the server configuration"
				+" with help of the servlet init param [lucee-server-directory] in the web.xml where the Lucee Servlet is defined"
				+" or the system property [lucee.base.dir].");

		final File modernDir = new File(root, "lucee-server");
		if (true) {
			// there is a server context in the old lucee location, move that one
			File classicDir;
			System.out.println("classic-root-directory:" + classicRoot);
			boolean had = false;
			if (classicRoot.isDirectory()
					&& (classicDir = new File(classicRoot, "lucee-server"))
							.isDirectory()) {
				System.out.println("had lucee-server classic" + classicDir);
				moveContent(classicDir, modernDir);
				had = true;
			}
			// there is a railo context
			if (!had
					&& classicRoot.isDirectory()
					&& (classicDir = new File(classicRoot, "railo-server"))
							.isDirectory()) {
				System.out.println("had railo-server classic" + classicDir);
				// check if there is a Railo context
				copyRecursiveAndRename(classicDir, modernDir);
				// zip the railo-server di and delete it (optional)
				try {
					ZipUtil.zip(classicDir, new File(root,
							"railo-server-context-old.zip"));
					Util.delete(classicDir);
				} catch (final Throwable t) {
					t.printStackTrace();
				}
				//moveContent(classicDir,new File(root,"lucee-server"));
			}
		}

		return root;
	}

	private static File getWebInfFolder(File file) {
		File parent;
		while(file!=null && !file.getName().equals("WEB-INF")) {
			parent=file.getParentFile();
			if(file.equals(parent)) return null; // this should not happen, simply to be sure
			file=parent;
		}
		return file;
	}
	
	private static void copyRecursiveAndRename(final File src, File trg)
			throws IOException {
		if (!src.exists())
			return;

		if (src.isDirectory()) {
			if (!trg.exists())
				trg.mkdirs();

			final File[] files = src.listFiles();
			for (final File file : files)
				copyRecursiveAndRename(file, new File(trg, file.getName()));
		} else if (src.isFile()) {
			if (trg.getName().endsWith(".rc") || trg.getName().startsWith("."))
				return;

			if (trg.getName().equals("railo-server.xml")) {
				trg = new File(trg.getParentFile(), "lucee-server.xml");
				// cfLuceeConfiguration
				final FileInputStream is = new FileInputStream(src);
				final FileOutputStream os = new FileOutputStream(trg);
				try {
					String str = Util.toString(is);
					str = str
							.replace("<cfRailoConfiguration",
									"<!-- copy from Railo context --><cfLuceeConfiguration");
					str = str.replace("</cfRailoConfiguration",
							"</cfLuceeConfiguration");

					str = str
							.replace("<railo-configuration",
									"<!-- copy from Railo context --><cfLuceeConfiguration");
					str = str.replace("</railo-configuration",
							"</cfLuceeConfiguration");

					str = str.replace("{railo-config}", "{lucee-config}");
					str = str.replace("{railo-server}", "{lucee-server}");
					str = str.replace("{railo-web}", "{lucee-web}");
					str = str.replace("\"railo.commons.", "\"lucee.commons.");
					str = str.replace("\"railo.runtime.", "\"lucee.runtime.");
					str = str.replace("\"railo.cfx.", "\"lucee.cfx.");
					str = str
							.replace("/railo-context.ra", "/lucee-context.lar");
					str = str.replace("/railo-context", "/lucee");
					str = str.replace("railo-server-context", "lucee-server");
					str = str.replace("http://www.getrailo.org",
							"http://release.lucee.org");
					str = str.replace("http://www.getrailo.com",
							"http://release.lucee.org");

					final ByteArrayInputStream bais = new ByteArrayInputStream(
							str.getBytes());

					try {
						Util.copy(bais, os);
						bais.close();
					} finally {
						Util.closeEL(is, os);
					}
				} finally {
					Util.closeEL(is, os);
				}
				return;
			}

			final FileInputStream is = new FileInputStream(src);
			final FileOutputStream os = new FileOutputStream(trg);
			try {
				Util.copy(is, os);
			} finally {
				Util.closeEL(is, os);
			}
		}
	}

	private void moveContent(final File src, final File trg) throws IOException {
		if (src.isDirectory()) {
			final File[] children = src.listFiles();
			if (children != null)
				for (final File element : children)
					moveContent(element, new File(trg, element.getName()));
			src.delete();
		} else if (src.isFile()) {
			trg.getParentFile().mkdirs();
			src.renameTo(trg);
		}
	}

	private File getDirectoryByProp(final String name) {
		return _getDirectoryBy(System.getProperty(name));
	}


	private File getDirectoryByEnv(final String name) {
		return _getDirectoryBy(System.getenv(name));
	}
	

	private File _getDirectoryBy(final String value) {
		if (Util.isEmpty(value, true))
			return null;

		final File dir = new File(value);
		dir.mkdirs();
		if (dir.isDirectory())
			return dir;

		return null;
	}

	/**
	 * returns the path where the classloader is located
	 * 
	 * @param cl ClassLoader
	 * @return file of the classloader root
	 */
	public static File getClassLoaderRoot(final ClassLoader cl) {
		final String path = "lucee/loader/engine/CFMLEngine.class";
		final URL res = cl.getResource(path);
		if (res == null)
			return null;
		// get file and remove all after !
		String strFile = null;
		try {
			strFile = URLDecoder.decode(res.getFile().trim(), "iso-8859-1");
		} catch (final UnsupportedEncodingException e) {

		}
		int index = strFile.indexOf('!');
		if (index != -1)
			strFile = strFile.substring(0, index);

		// remove path at the end
		index = strFile.lastIndexOf(path);
		if (index != -1)
			strFile = strFile.substring(0, index);

		// remove "file:" at start and lucee.jar at the end
		if (strFile.startsWith("file:"))
			strFile = strFile.substring(5);
		if (strFile.endsWith("lucee.jar"))
			strFile = strFile.substring(0, strFile.length() - 9);

		File file = new File(strFile);
		if (file.isFile())
			file = file.getParentFile();

		return file;
	}

	/**
	 * Load CFMl Engine Implementation (lucee.runtime.engine.CFMLEngineImpl)
	 * from a Classloader
	 * 
	 * @param bundle
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private CFMLEngine getEngine(final BundleCollection bc)
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		log(Logger.LOG_DEBUG,
				"state:" + BundleUtil.bundleState(bc.core.getState(), ""));
		//bundle.getBundleContext().getServiceReference(CFMLEngine.class.getName());
		log(Logger.LOG_DEBUG,
				Constants.FRAMEWORK_BOOTDELEGATION
						+ ":"
						+ bc.getBundleContext().getProperty(
								Constants.FRAMEWORK_BOOTDELEGATION));
		log(Logger.LOG_DEBUG, "felix.cache.rootdir:"
				+ bc.getBundleContext().getProperty("felix.cache.rootdir"));

		//log(Logger.LOG_DEBUG,bc.master.loadClass(TP.class.getName()).getClassLoader().toString());
		final Class<?> clazz = bc.core
				.loadClass("lucee.runtime.engine.CFMLEngineImpl");
		log(Logger.LOG_DEBUG, "class:" + clazz.getName());
		final Method m = clazz.getMethod("getInstance", new Class[] {
				CFMLEngineFactory.class, BundleCollection.class });
		return (CFMLEngine) m.invoke(null, new Object[] { this, bc });

	}

	private class UpdateChecker extends Thread {
		private final CFMLEngineFactory factory;
		private final Identification id;

		private UpdateChecker(final CFMLEngineFactory factory,
				final Identification id) {
			this.factory = factory;
			this.id = id;
		}

		@Override
		public void run() {
			long time = 10000;
			while (true)
				try {
					sleep(time);
					time = 1000 * 60 * 60 * 24;
					factory._update(id);

				} catch (final Exception e) {

				}
		}
	}

}