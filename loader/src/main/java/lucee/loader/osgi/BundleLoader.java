/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.loader.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import lucee.commons.io.log.Log;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngineFactorySupport;
import lucee.loader.util.Util;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class BundleLoader {

	/**
	 * build (if necessary) a bundle and load it
	 * 
	 * @param engFac
	 * @param cacheRootDir
	 * @param jarDirectory
	 * @param rc
	 * @param old
	 * @return
	 * @throws IOException
	 * @throws BundleException
	 */
	public static BundleCollection loadBundles(final CFMLEngineFactory engFac,
			final File cacheRootDir, final File jarDirectory, final File rc,
			final BundleCollection old) throws IOException, BundleException {
		final JarFile jf = new JarFile(rc);// TODO this should work in any case, but we should still improve this code
		try {
			// Manifest
			final Manifest mani = jf.getManifest();
			if (mani == null)
				throw new IOException("lucee core [" + rc
						+ "] is invalid, there is no META-INF/MANIFEST.MF File");
			final Attributes attrs = mani.getMainAttributes();

			// default properties
			final Properties defProp = loadDefaultProperties(jf);

			// Get data from Manifest and default.properties

			// Lucee Core Version
			//String rcv = unwrap(defProp.getProperty("lucee.core.version"));
			//if(Util.isEmpty(rcv)) throw new IOException("lucee core ["+rc+"] is invalid, no core version is defined in the {Lucee-Core}/default.properties File");
			//int version = CFMLEngineFactory.toInVersion(rcv);

			// read the config from default.properties
			final Map<String, Object> config = new HashMap<String, Object>();
			{
				final Iterator<Entry<Object, Object>> it = defProp.entrySet()
						.iterator();
				Entry<Object, Object> e;
				String k;
				while (it.hasNext()) {
					e = it.next();
					k = (String) e.getKey();
					if (!k.startsWith("org.") && !k.startsWith("felix."))
						continue;
					config.put(
							k,
							CFMLEngineFactorySupport.removeQuotes(
									(String) e.getValue(), true));
				}
			}

			/* / org.osgi.framework.storage.clean
			String storageClean = unwrap(defProp
					.getProperty("org.osgi.framework.storage.clean"));
			if (Util.isEmpty(storageClean))
				storageClean = Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT;
			engFac.log(Logger.LOG_INFO, "org.osgi.framework.storage.clean:"
					+ storageClean);*/

			/* / org.osgi.framework.bootdelegation
			String bootDelegation = unwrap(defProp.getProperty("org.osgi.framework.bootdelegation"));
			if (Util.isEmpty(bootDelegation))
				throw new IOException(
						"[org.osgi.framework.bootdelegation] setting is necessary in file {Lucee-Core}/default.properties");
			engFac.log(Logger.LOG_INFO, "org.osgi.framework.bootdelegation:"
					+ bootDelegation);*/

			/* / org.osgi.framework.system.packages
			String systemPackages = unwrap(defProp.getProperty("org.osgi.framework.system.packages"));
			engFac.log(Logger.LOG_INFO, "org.osgi.framework.system.packages:"
					+ systemPackages);*/

			/*/ org.osgi.framework.bundle.parent
			String parentClassLoader = unwrap(defProp
					.getProperty("org.osgi.framework.bundle.parent"));
			if (Util.isEmpty(parentClassLoader))
				parentClassLoader = Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK;
			else
				parentClassLoader = BundleUtil
						.toFrameworkBundleParent(parentClassLoader);
			engFac.log(Logger.LOG_INFO, "org.osgi.framework.bundle.parent:"
					+ parentClassLoader);
			*/

			/*/ felix.log.level
			int logLevel = 1; // 1 = error, 2 = warning, 3 = information, and 4 = debug
			String strLogLevel = unwrap(defProp.getProperty("felix.log.level"));
			if (!Util.isEmpty(strLogLevel)) {
				if ("warn".equalsIgnoreCase(strLogLevel)
						|| "warning".equalsIgnoreCase(strLogLevel))
					logLevel = 2;
				else if ("info".equalsIgnoreCase(strLogLevel)
						|| "information".equalsIgnoreCase(strLogLevel))
					logLevel = 3;
				else if ("debug".equalsIgnoreCase(strLogLevel))
					logLevel = 4;
			}
			engFac.log(Logger.LOG_INFO,
					"felix.log.level (1 = error, 2 = warning, 3 = information, and 4 = debug):"
							+ logLevel);
			*/
			// 
			/*
			 if (old != null) {
				bc = old.getBundleContext();
				removeBundles(old);
				//clearCacheDirectory(felix-cache);
			} else {
				bc = engFac.getFelix(cacheRootDir, storageClean, bootDelegation,
						parentClassLoader, logLevel, null).getBundleContext();
			}
			 */

			// close all bundles
			Felix felix;
			if (old != null) {
				removeBundlesEL(old);
				felix = old.felix;
				felix.stop();// stops all active bundles
				felix = engFac.getFelix(cacheRootDir, config);
				//felix.start();
			} else
				felix = engFac.getFelix(cacheRootDir, config);
			final BundleContext bc = felix.getBundleContext();

			// get bundle needed for that core
			final String rb = attrs.getValue("Require-Bundle");
			if (Util.isEmpty(rb))
				throw new IOException(
						"lucee core ["
								+ rc
								+ "] is invalid, no Require-Bundle defintion found in the META-INF/MANIFEST.MF File");

			// get fragments needed for that core (Lucee specific Key)
			final String rbf = attrs.getValue("Require-Bundle-Fragment");

			// load Required/Available Bundles
			final Map<String, String> requiredBundles = readRequireBundle(rb); // Require-Bundle
			final Map<String, String> requiredBundleFragments = readRequireBundle(rbf); // Require-Bundle-Fragment
			final Map<String, File> availableBundles = loadAvailableBundles(jarDirectory);

			// Add Required Bundles
			Entry<String, String> e;
			File f;
			String id;
			final List<Bundle> bundles = new ArrayList<Bundle>();
			Iterator<Entry<String, String>> it = requiredBundles.entrySet()
					.iterator();
			while (it.hasNext()) {
				e = it.next();
				id = e.getKey() + "|" + e.getValue();
				f = availableBundles.get(id);
				//StringBuilder sb=new StringBuilder();
				if (f == null) {
					/*sb.append(id+"\n");
					Iterator<String> _it = availableBundles.keySet().iterator();
					while(_it.hasNext()){
						sb.append("- "+_it.next()+"\n");
					}
					throw new RuntimeException(sb.toString());*/
				}
				if (f == null)
					f = engFac.downloadBundle(e.getKey(), e.getValue(), null);
				bundles.add(BundleUtil.addBundle(engFac, bc, f, null));
			}

			// Add Required Bundle Fragments
			final List<Bundle> fragments = new ArrayList<Bundle>();
			it = requiredBundleFragments.entrySet().iterator();
			while (it.hasNext()) {
				e = it.next();
				id = e.getKey() + "|" + e.getValue();
				f = availableBundles.get(id);

				if (f == null)
					f = engFac.downloadBundle(e.getKey(), e.getValue(), null); // if identification is not defined, it is loaded from the CFMLEngine
				fragments.add(BundleUtil.addBundle(engFac, bc, f, null));
			}

			// Add Lucee core Bundle
			Bundle bundle;
			//bundles.add(bundle = BundleUtil.addBundle(engFac, bc, rc,null));
			bundle = BundleUtil.addBundle(engFac, bc, rc, null);

			// Start the bundles
			BundleUtil.start(engFac, bundles);
			BundleUtil.start(engFac, bundle);

			return new BundleCollection(felix, bundle, bundles);
		} finally {
			if (jf != null)
				try {
					jf.close();
				} catch (final IOException ioe) {
				}
		}
	}

	private static Map<String, File> loadAvailableBundles(
			final File jarDirectory) {
		final Map<String, File> rtn = new HashMap<String, File>();
		final File[] jars = jarDirectory.listFiles();
		JarFile jf = null;
		String symbolicName, version;
		Attributes attrs;
		for (int i = 0; i < jars.length; i++) {
			if (!jars[i].isFile() || !jars[i].getName().endsWith(".jar"))
				continue;
			try {
				jf = new JarFile(jars[i]);
				attrs = jf.getManifest().getMainAttributes();
				symbolicName = attrs.getValue("Bundle-SymbolicName");
				version = attrs.getValue("Bundle-Version");
				if (Util.isEmpty(symbolicName))
					throw new IOException(
							"OSGi bundle ["
									+ jars[i]
									+ "] is invalid, {Lucee-Core}META-INF/MANIFEST.MF does not contain a \"Bundle-SymbolicName\"");
				if (Util.isEmpty(version))
					throw new IOException(
							"OSGi bundle ["
									+ jars[i]
									+ "] is invalid, {Lucee-Core}META-INF/MANIFEST.MF does not contain a \"Bundle-Version\"");

				rtn.put(symbolicName + "|" + version, jars[i]);
			} catch (final Throwable t) {
			} finally {
				if (jf != null)
					try {
						jf.close();
					} catch (final IOException e) {
					}
			}
		}
		return rtn;

	}

	private static Map<String, String> readRequireBundle(final String rb)
			throws IOException {
		final HashMap<String, String> rtn = new HashMap<String, String>();
		final StringTokenizer st = new StringTokenizer(rb, ",");
		StringTokenizer stl;
		String line, jarName, jarVersion = null, token;
		int index;
		while (st.hasMoreTokens()) {
			line = st.nextToken().trim();
			if (Util.isEmpty(line))
				continue;

			stl = new StringTokenizer(line, ";");

			// first is the name
			jarName = stl.nextToken().trim();

			while (stl.hasMoreTokens()) {
				token = stl.nextToken().trim();
				if (token.startsWith("bundle-version")
						&& (index = token.indexOf('=')) != -1)
					jarVersion = token.substring(index + 1).trim();
			}
			if (jarVersion == null)
				throw new IOException(
						"missing \"bundle-version\" info in the following \"Require-Bundle\" record: \""
								+ jarName + "\"");
			rtn.put(jarName, jarVersion);
		}
		return rtn;
	}

	/*private static String unwrap(String str) {
		return str == null ? null : CFMLEngineFactory.removeQuotes(str, true);
	}*/

	public static Properties loadDefaultProperties(final JarFile jf)
			throws IOException {
		final ZipEntry ze = jf.getEntry("default.properties");
		if (ze == null)
			throw new IOException(
					"the Lucee core has no default.properties file!");

		final Properties prop = new Properties();
		InputStream is = null;
		try {
			is = jf.getInputStream(ze);
			prop.load(is);
		} finally {
			CFMLEngineFactorySupport.closeEL(is);
		}
		return prop;
	}

	public static void removeBundles(final BundleContext bc)
			throws BundleException {
		final Bundle[] bundles = bc.getBundles();
		for (final Bundle bundle : bundles)
			removeBundle(bundle);
	}

	public static void removeBundles(final BundleCollection bc)
			throws BundleException {
		final Bundle[] bundles = bc.getBundleContext().getBundles();

		for (final Bundle bundle : bundles)
			if (!BundleUtil.isSystemBundle(bundle))
				removeBundle(bundle);
	}

	public static void removeBundlesEL(final BundleCollection bc) {
		final Bundle[] bundles = bc.getBundleContext().getBundles();

		for (final Bundle bundle : bundles)
			if (!BundleUtil.isSystemBundle(bundle))
				try {
					removeBundle(bundle);
				} catch (final BundleException e) {
					// TODO remove 
					e.printStackTrace();
				}
	}

	public static void removeBundle(final Bundle bundle) throws BundleException {
		if (bundle == null)
			return;

		log(Log.LEVEL_INFO, "remove bundle:" + bundle.getSymbolicName());

		// wait for starting
		int sleept = 0;
		while (bundle.getState() == Bundle.STARTING) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {
				break;
			}
			sleept += 10;
			if (sleept > 3000)
				break; // only wait for 3 seconds
		}

		// force stopping (even when still starting)
		if (bundle.getState() == Bundle.ACTIVE
				|| bundle.getState() == Bundle.STARTING)
			bundle.stop();

		// wait for stopping
		sleept = 0;
		while (bundle.getState() == Bundle.STOPPING) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {
				break;
			}
			sleept += 10;
			if (sleept > 3000)
				break; // only wait for 3 seconds
		}

		if (bundle.getState() != Bundle.UNINSTALLED)
			bundle.uninstall();
	}

	private static void log(final int level, final String msg) {
		System.out.println(msg);
	}
}