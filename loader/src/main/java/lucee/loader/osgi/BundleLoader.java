/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngineFactorySupport;
import lucee.loader.util.Util;

public class BundleLoader {

	public static BundleCollection loadBundles(final CFMLEngineFactory engFac, final File resourceRootDir, final File jarDirectory, final File rc, final BundleCollection old)
			throws IOException, BundleException {
		return loadBundles(engFac, resourceRootDir, jarDirectory, rc, old, true);
	}

	private static BundleCollection loadBundles(final CFMLEngineFactory engFac, final File resourceRootDir, final File jarDirectory, final File rc, final BundleCollection old,
			boolean retry) throws IOException, BundleException {
		long start = System.currentTimeMillis();
		final JarFile jf = new JarFile(rc);// TODO this should work in any case, but we should still improve this code
		final Map<String, Object> config = new HashMap<>();
		try {
			// default properties
			final Properties defProp = loadDefaultProperties(jf);
			// read the config from default.properties
			{
				final Iterator<Entry<Object, Object>> it = defProp.entrySet().iterator();
				Entry<Object, Object> e;
				String k;
				while (it.hasNext()) {
					e = it.next();
					k = (String) e.getKey();
					if (!k.startsWith("org.") && !k.startsWith("felix.")) continue;
					config.put(k, CFMLEngineFactorySupport.removeQuotes((String) e.getValue(), true));
				}
			}
			long tmp = System.currentTimeMillis();
			engFac.log(LoggerImpl.LOG_DEBUG, "loaded default properties in " + (tmp - start) + "ms");
			start = tmp;

			// close all bundles
			Felix felix;
			if (old != null) {
				engFac.log(LoggerImpl.LOG_DEBUG, "onload/uninstall existig bundle context");
				removeBundlesEL(old);
				felix = old.felix;
				// stops felix (wait for it)
				BundleUtil.stop(felix, false);
				felix = engFac.getFelix(resourceRootDir, config);
				tmp = System.currentTimeMillis();
				engFac.log(LoggerImpl.LOG_DEBUG, "reloaded Felix in " + (tmp - start) + "ms");
				start = tmp;
			}
			else {
				felix = engFac.getFelix(resourceRootDir, config);
				tmp = System.currentTimeMillis();
				engFac.log(LoggerImpl.LOG_DEBUG, "loaded Felix in " + (tmp - start) + "ms");
				start = tmp;
			}

			final BundleContext bc = felix.getBundleContext();

			// Manifest
			final Manifest mani = jf.getManifest();
			if (mani == null) throw new IOException("lucee core [" + rc + "] is invalid, there is no META-INF/MANIFEST.MF File");
			final Attributes attrs = mani.getMainAttributes();

			// get bundle needed for that core
			final String rb = attrs.getValue("Require-Bundle");
			if (Util.isEmpty(rb)) throw new IOException("lucee core [" + rc + "] is invalid, no Require-Bundle definition found in the META-INF/MANIFEST.MF File");

			// get fragments needed for that core (Lucee specific Key)
			final String rbf = attrs.getValue("Require-Bundle-Fragment");

			// load Required/Available Bundles
			final Map<String, String> requiredBundles = readRequireBundle(rb); // Require-Bundle

			final Map<String, String> requiredBundleFragments = readRequireBundle(rbf); // Require-Bundle-Fragment

			final Map<String, File> availableBundles = loadAvailableBundles(jarDirectory, requiredBundles, requiredBundleFragments);

			// deploys bundled bundles to bundle directory
			// deployBundledBundles(jarDirectory, availableBundles);

			String doDownload = Util.getSystemPropOrEnvVar("lucee.enable.bundle.download", null);
			boolean always = "always".equalsIgnoreCase(doDownload);

			// Add Required Bundles
			final List<Bundle> bundles = addRequiredBundles(requiredBundles, availableBundles, jf, always, engFac, bc);

			// Add Required Bundle Fragments
			addRequiredBundles(requiredBundleFragments, availableBundles, jf, always, engFac, bc);

			// Add Lucee core Bundle
			Bundle bundle = BundleUtil.addBundle(engFac, bc, rc, null);

			tmp = System.currentTimeMillis();
			engFac.log(LoggerImpl.LOG_DEBUG, "loaded bundles in " + (tmp - start) + "ms");
			start = tmp;

			// Start the bundles
			BundleUtil.start(engFac, bundles);
			BundleUtil.start(engFac, bundle);
			tmp = System.currentTimeMillis();
			engFac.log(LoggerImpl.LOG_DEBUG, "started bundles in " + (tmp - start) + "ms");
			start = tmp;

			return new BundleCollection(felix, bundle, bundles);
		}
		catch (BundleException be) {
			// PATCH for LDEV-6144: stale felix cache (bundle registered at old path after rename, e.g. from
			// LDEV-6145).
			// Clear the cache and retry once with a fresh Felix instance.
			File felixCacheDir;
			if (be.getMessage() != null && be.getMessage().contains("not unique") && (felixCacheDir = new File(extractFelixCacheRootdir(config), "felix-cache")).isDirectory()) {
				Util.deleteContent(felixCacheDir, null);
				return loadBundles(engFac, resourceRootDir, jarDirectory, rc, old, false);
			}
			else throw be;
		}
		finally {
			if (jf != null) try {
				jf.close();
			}
			catch (final IOException ioe) {}
		}
	}

	private static File extractFelixCacheRootdir(Map<String, Object> config) {
		return new File((String) config.get("felix.cache.rootdir"));
	}

	public static List<Bundle> addRequiredBundles(final Map<String, String> requiredBundles, final Map<String, File> availableBundles, final JarFile luceeCore, boolean always,
			CFMLEngineFactory engFac, BundleContext bc) {
		final List<Bundle> bundles = new ArrayList<>();
		final List<Bundle> bundlesSync = Collections.synchronizedList(bundles);
		Iterator<Entry<String, String>> it = requiredBundles.entrySet().iterator();
		// Use regular threads
		List<CompletableFuture<?>> futures = new ArrayList<>();
		while (it.hasNext()) {
			Entry<String, String> e = it.next();
			futures.add(CompletableFuture.runAsync(() -> {
				try {
					String id = e.getKey() + "|" + e.getValue();
					File f = always ? null : availableBundles.get(id);
					if (f == null) {
						f = engFac.downloadBundle(e.getKey(), e.getValue(), null, luceeCore);
					}
					bundlesSync.add(BundleUtil.addBundle(engFac, bc, f, null));
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}));
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		return bundles;
	}

	private static Map<String, File> loadAvailableBundles(final File jarDirectory, final Map<String, String> requiredBundles, final Map<String, String> requiredBundleFragments) {
		final Map<String, File> rtn = new ConcurrentHashMap<>();
		final File[] jars = jarDirectory.listFiles();

		if (jars != null && jars.length > 0) {
			// Create a thread pool with a fixed number of threads
			// ExecutorService executor = Executors.newFixedThreadPool(Math.min(jars.length,
			// Runtime.getRuntime().availableProcessors()));

			// we first try to make a match based on the required bundles and bundle fragments
			List<File> remainings = null;
			boolean has;
			for (File jar: jars) {
				if (!jar.isFile() || !jar.getName().endsWith(".jar")) continue;
				String fullname = null;
				has = false;
				try {
					fullname = jar.getName();
					fullname = jar.getName().substring(0, fullname.length() - 4);
					int lastDot = fullname.lastIndexOf('.');
					if (lastDot != -1) {
						int lastSlash = fullname.substring(0, lastDot).lastIndexOf('-');
						if (lastSlash != -1) {
							String name = fullname.substring(0, lastSlash);
							String version = fullname.substring(lastSlash + 1);
							String val;
							if ((val = requiredBundles.get(name)) != null && val.equals(version)) {
								rtn.put(name + "|" + version, jar);
								has = true;
							}
							else if ((val = requiredBundleFragments.get(name)) != null && val.equals(version)) {
								rtn.put(name + "|" + version, jar);
								has = true;
							}
						}
					}

					if (!has) {
						if (remainings == null) remainings = new ArrayList<>();
						remainings.add(jar);
					}
				}
				catch (Throwable t) {
					Util.rethrowIfNecessary(t);
				}
			}
			if (remainings != null) {

				if (remainings.size() > 1) {
					List<CompletableFuture<?>> futures = new ArrayList<>();
					for (File jar: remainings) {
						// Submit tasks for processing each jar file
						futures.add(CompletableFuture.runAsync(() -> {
							try {
								rtn.put(loadBundleInfo(jar), jar);
							}
							catch (IOException ioe) { // Log the exception
								new Exception("Error loading bundle info for [" + jar.toString() + "]", ioe).printStackTrace();
							}
						}));
					}
					// Wait for all tasks to complete
					CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
				}
				else {
					for (File jar: remainings) {
						try {
							rtn.put(loadBundleInfo(jar), jar);

						}
						catch (IOException ioe) {
							// Log the exception
							new Exception("Error loading bundle info for [" + jar.toString() + "]", ioe).printStackTrace();
						}
					}
				}
			}
		}
		return rtn;

	}

	private static Map<String, File> loadAvailableBundlesSerial(final File jarDirectory) {
		final Map<String, File> rtn = new HashMap<>();
		final File[] jars = jarDirectory.listFiles();
		if (jars != null) {
			for (File jar: jars) {
				if (!jar.isFile() || !jar.getName().endsWith(".jar")) continue;
				try {
					rtn.put(loadBundleInfo(jar), jar);
				}
				catch (final IOException ioe) {
					new Exception("Error loading bundle info for [" + jar.toString() + "]", ioe).printStackTrace();
				}
			}
		}
		return rtn;
	}

	public static ExecutorService createExecutorService(int maxThreads) {
		if (maxThreads <= 0) {
			throw new IllegalArgumentException("Invalid value for maxThreads: " + maxThreads + ". The value must be greater than 0.");
		}
		if (parseJavaVersion(System.getProperty("java.version")) >= 19) {
			// FUTURE use newVirtualThreadPerTaskExecutor natively
			try {
				MethodHandles.Lookup lookup = MethodHandles.lookup();
				MethodType methodType = MethodType.methodType(ExecutorService.class);
				MethodHandle methodHandle = lookup.findStatic(Executors.class, "newVirtualThreadPerTaskExecutor", methodType);
				return (ExecutorService) methodHandle.invoke();
			}
			catch (Throwable e) {
				Util.rethrowIfNecessary(e);
			}
		}

		return Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), maxThreads));
	}

	private static int parseJavaVersion(String version) {
		if (version.startsWith("1.")) {
			// For older Java versions like 1.8
			version = version.substring(2);
		}
		int dotIndex = version.indexOf(".");
		int dashIndex = version.indexOf("-");
		// Get the version number before the first dot or dash
		try {
			if (dotIndex > 0) {
				return Integer.parseInt(version.substring(0, dotIndex));
			}
			else if (dashIndex > 0) {
				return Integer.parseInt(version.substring(0, dashIndex));
			}
			else {
				return Integer.parseInt(version);
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid Java version: " + version, e);
		}
	}

	public static String loadBundleInfo(final File jar) throws IOException {
		JarFile jf = new JarFile(jar);
		try {
			Attributes attrs = jf.getManifest().getMainAttributes();
			String symbolicName = attrs.getValue("Bundle-SymbolicName");
			String version = attrs.getValue("Bundle-Version");
			if (Util.isEmpty(symbolicName))
				throw new IOException("OSGi bundle [" + jar + "] is invalid, {Lucee-Core}META-INF/MANIFEST.MF does not contain a \"Bundle-SymbolicName\"");
			if (Util.isEmpty(version)) throw new IOException("OSGi bundle [" + jar + "] is invalid, {Lucee-Core}META-INF/MANIFEST.MF does not contain a \"Bundle-Version\"");

			return symbolicName + "|" + version;
		}
		finally {
			Util.closeEL(jf);
		}
	}

	private static Map<String, String> readRequireBundle(final String rb) throws IOException {
		final HashMap<String, String> rtn = new HashMap<>();
		if (Util.isEmpty(rb)) return rtn;

		final StringTokenizer st = new StringTokenizer(rb, ",");
		StringTokenizer stl;
		String line, jarName, jarVersion = null, token;
		int index;
		while (st.hasMoreTokens()) {
			line = st.nextToken().trim();
			if (Util.isEmpty(line)) continue;

			stl = new StringTokenizer(line, ";");

			// first is the name
			jarName = stl.nextToken().trim();

			while (stl.hasMoreTokens()) {
				token = stl.nextToken().trim();
				if (token.startsWith("bundle-version") && (index = token.indexOf('=')) != -1) jarVersion = token.substring(index + 1).trim();
			}
			if (jarVersion == null) throw new IOException("missing \"bundle-version\" info in the following \"Require-Bundle\" record: \"" + jarName + "\"");
			rtn.put(jarName, jarVersion);
		}
		return rtn;
	}

	/*
	 * private static String unwrap(String str) { return str == null ? null :
	 * CFMLEngineFactory.removeQuotes(str, true); }
	 */

	public static Properties loadDefaultProperties(final JarFile jf) throws IOException {
		final ZipEntry ze = jf.getEntry("default.properties");
		if (ze == null) throw new IOException("the Lucee core has no default.properties file!");

		final Properties prop = new Properties();
		InputStream is = null;
		try {
			is = jf.getInputStream(ze);
			prop.load(is);
		}
		finally {
			CFMLEngineFactorySupport.closeEL(is);
		}
		return prop;
	}

	public static void removeBundles(final BundleContext bc) throws BundleException {
		final Bundle[] bundles = bc.getBundles();
		for (final Bundle bundle: bundles)
			removeBundle(bundle);
	}

	public static void removeBundles(final BundleCollection bc, boolean uninstall) throws BundleException {
		BundleContext bcc = bc.getBundleContext();
		final Bundle[] bundles = bcc == null ? new Bundle[0] : bcc.getBundles();

		// stop
		for (final Bundle bundle: bundles) {
			if (!BundleUtil.isSystemBundle(bundle)) {
				stopBundle(bundle);
			}
		}
		// uninstall
		if (uninstall) {
			for (final Bundle bundle: bundles) {
				if (!BundleUtil.isSystemBundle(bundle)) {
					uninstallBundle(bundle);
				}
			}
		}
	}

	public static void removeBundlesEL(final BundleCollection bc) {
		BundleContext bcc = bc.getBundleContext();
		final Bundle[] bundles = bcc == null ? new Bundle[0] : bcc.getBundles();

		for (final Bundle bundle: bundles) {
			if (!BundleUtil.isSystemBundle(bundle)) {
				try {
					stopBundle(bundle);
				}
				catch (final BundleException e) {
					e.printStackTrace();
				}
			}
		}
		for (final Bundle bundle: bundles) {
			if (!BundleUtil.isSystemBundle(bundle)) {
				try {
					uninstallBundle(bundle);
				}
				catch (final BundleException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void removeBundle(final Bundle bundle) throws BundleException {
		stopBundle(bundle);
		uninstallBundle(bundle);
	}

	public static void uninstallBundle(final Bundle bundle) throws BundleException {
		if (bundle == null) return;

		if (bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STARTING || bundle.getState() == Bundle.STOPPING) stopBundle(bundle);

		if (bundle.getState() != Bundle.UNINSTALLED) {
			bundle.uninstall();
		}
	}

	public static void stopBundle(final Bundle bundle) throws BundleException {
		if (bundle == null) return;

		// wait for starting/stopping
		int sleept = 0;
		while (bundle.getState() == Bundle.STOPPING || bundle.getState() == Bundle.STARTING) {
			try {
				Thread.sleep(10);
			}
			catch (final InterruptedException e) {
				break;
			}
			sleept += 10;
			if (sleept > 5000) break; // only wait for 5 seconds
		}

		// force stopping (even when still starting)
		if (bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STARTING) BundleUtil.stop(bundle, false);

	}
}