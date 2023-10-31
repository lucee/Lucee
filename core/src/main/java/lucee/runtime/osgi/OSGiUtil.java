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
package lucee.runtime.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.resource.Requirement;

import lucee.print;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ResourceNameFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.osgi.BundleUtil;
import lucee.loader.util.Util;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWebFactory;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Identification;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalConfigServer;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.BundleRange.VersionRange;
import lucee.runtime.type.util.ListUtil;

public class OSGiUtil {
	private static boolean[] checkBundleRanges = new boolean[] { true, false };
	private static final int QUALIFIER_APPENDIX_SNAPSHOT = 1;
	private static final int QUALIFIER_APPENDIX_BETA = 2;
	private static final int QUALIFIER_APPENDIX_RC = 3;
	private static final int QUALIFIER_APPENDIX_OTHER = 4;
	private static final int QUALIFIER_APPENDIX_STABLE = 5;

	private static final int MAX_REDIRECTS = 5;

	private static ThreadLocal<Set<String>> bundlesThreadLocal = new ThreadLocal<Set<String>>() {
		@Override
		protected Set<String> initialValue() {
			return new HashSet<String>();
		}
	};

	private static class Filter implements FilenameFilter, ResourceNameFilter {

		private BundleRange bundleRange;

		public Filter(BundleRange bundleRange) {
			this.bundleRange = bundleRange;
		}

		@Override
		public boolean accept(File dir, String name) {
			return accept(name);
		}

		@Override
		public boolean accept(Resource dir, String name) {
			return accept(name);
		}

		private boolean accept(String name) {
			if (!name.endsWith(".jar")) return false;
			if (bundleRange == null) return true;
			Version v;
			String[] patterns = new String[] { bundleRange.getName() + "-", bundleRange.getName().replace('.', '-') + "-", bundleRange.getName().replace('-', '.') + "-" };
			for (String pattern: patterns) {
				if (name.startsWith(pattern)) {
					v = toVersion(name.substring(pattern.length(), name.length() - 4), null);
					if (v != null) return true;
				}
			}

			return false;
		}
	}

	private static final Filter JAR_EXT_FILTER = new Filter(null);

	private static String[] bootDelegation;
	private static Map<String, String> packageBundleMapping = new LinkedHashMap<String, String>();
	private static Map<String, SoftReference<Map<String, BundleFile>>> packageBundleMappingDyn = null;
	private static long packageBundleMappingDynLastMod;

	static {
		// this is needed in case old version of extensions are used, because lucee no longer bundles this
		packageBundleMapping.put("org.bouncycastle.asn1", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.crypto", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.i18n", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.jce", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.math", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.mozilla", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.ocsp", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.openssl", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.voms", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.x509", "bouncycastle.prov");
		packageBundleMapping.put("org.bouncycastle.tsp", "bouncycastle.tsp");
		packageBundleMapping.put("org.bouncycastle.cms", "bouncycastle.mail");
		packageBundleMapping.put("org.bouncycastle.mail", "bouncycastle.mail");
		packageBundleMapping.put("org.bouncycastle.sasn1", "bouncycastle.mail");
		packageBundleMapping.put("org.bouncycastle", "bcprov");
		// packageBundleMapping.put("org.apache.log4j", "log4j");
		packageBundleMapping.put("com.fasterxml.jackson.annotation", "com.fasterxml.jackson.core.jackson-annotations");
		packageBundleMapping.put("org.apache.lucene.analysis", "apache.lucene");
	}

	/**
	 * only installs a bundle, if the bundle does not already exist, if the bundle exists the existing
	 * bundle is unloaded first.
	 *
	 * @param factory
	 * @param context
	 * @param bundle
	 * @return
	 * @throws IOException
	 * @throws BundleException
	 */
	public static Bundle installBundle(BundleContext context, Resource bundle, boolean checkExistence) throws IOException, BundleException {
		if (checkExistence) {
			BundleFile bf = BundleFile.getInstance(bundle);
			if (!bf.isBundle()) throw new BundleException(bundle + " is not a valid bundle!");

			Bundle existing = loadBundleFromLocal(context, bf.getSymbolicName(), bf.getVersion(), null, false, null);
			if (existing != null) return existing;
		}

		return _loadBundle(context, bundle.getAbsolutePath(), bundle.getInputStream(), true);
	}

	/**
	 * does not check if the bundle already exists!
	 *
	 * @param context
	 * @param path
	 * @param is
	 * @param closeStream
	 * @return
	 * @throws BundleException
	 */
	private static Bundle _loadBundle(BundleContext context, String path, InputStream is, boolean closeStream) throws BundleException {
		log(Log.LEVEL_DEBUG, "add bundle:" + path);

		try {
			// we make this very simply so an old loader that is calling this still works
			return context.installBundle(path, is);
		}
		finally {
			// we make this very simply so an old loader that is calling this still works
			if (closeStream && is != null) {
				try {
					is.close();
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
		}
	}

	/**
	 * only installs a bundle, if the bundle does not already exist, if the bundle exists the existing
	 * bundle is unloaded first. the bundle is not stored physically on the system.
	 *
	 * @param factory
	 * @param context
	 * @param bundle
	 * @return
	 * @throws IOException
	 * @throws BundleException
	 */
	public static Bundle installBundle(BundleContext context, InputStream bundleIS, boolean closeStream, boolean checkExistence) throws IOException, BundleException {
		// store locally to test the bundle
		String name = System.currentTimeMillis() + ".tmp";
		Resource dir = SystemUtil.getTempDirectory();
		Resource tmp = dir.getRealResource(name);
		int count = 0;
		while (tmp.exists())
			tmp = dir.getRealResource((count++) + "_" + name);
		IOUtil.copy(bundleIS, tmp, closeStream);

		try {
			return installBundle(context, tmp, checkExistence);
		}
		finally {
			tmp.delete();
		}
	}

	public static Version toVersion(String version, Version defaultValue) {
		if (StringUtil.isEmpty(version)) return defaultValue;
		// String[] arr = ListUtil.listToStringArray(version, '.');
		String[] arr;
		try {
			arr = ListUtil.toStringArrayTrim(ListUtil.listToArray(version.trim(), '.'));
		}
		catch (PageException e) {
			return defaultValue; // should not happen
		}

		Integer major, minor, micro;
		String qualifier;

		if (arr.length == 1) {
			major = Caster.toInteger(arr[0], null);
			minor = 0;
			micro = 0;
			qualifier = null;
		}
		else if (arr.length == 2) {
			major = Caster.toInteger(arr[0], null);
			minor = Caster.toInteger(arr[1], null);
			micro = 0;
			qualifier = null;
		}
		else if (arr.length == 3) {
			major = Caster.toInteger(arr[0], null);
			minor = Caster.toInteger(arr[1], null);
			micro = Caster.toInteger(arr[2], null);
			qualifier = null;
		}
		else {
			major = Caster.toInteger(arr[0], null);
			minor = Caster.toInteger(arr[1], null);
			micro = Caster.toInteger(arr[2], null);
			qualifier = arr[3];
		}

		if (major == null || minor == null || micro == null) return defaultValue;

		if (qualifier == null) return new Version(major, minor, micro);
		return new Version(major, minor, micro, qualifier);
	}

	public static Version toVersion(String version) throws BundleException {
		Version v = toVersion(version, null);
		if (v != null) return v;
		throw new BundleException(
				"Given version [" + version + "] is invalid, a valid version is following this pattern <major-number>.<minor-number>.<micro-number>[.<qualifier>]");
	}

	/**
	 * tries to load a class with ni bundle definition
	 *
	 * @param name
	 * @param version
	 * @param id
	 * @param startIfNecessary
	 * @return
	 * @throws BundleException
	 */
	public static Class loadClass(String className, Class defaultValue) {

		// a class necessary need a package info, otherwise it is useless to search for it
		if (className.indexOf('.') == -1 && className.indexOf('/') == -1 && className.indexOf('\\') == -1) return defaultValue;

		className = className.trim();

		String classPath = className.replace('.', '/') + ".class";

		CFMLEngine engine = CFMLEngineFactory.getInstance();
		BundleCollection bc = engine.getBundleCollection();
		// first we try to load the class from the Lucee core
		try {
			// load from core
			if (bc.core.getEntry(classPath) != null) {
				return bc.core.loadClass(className);
			}
		}
		catch (Exception e) {
		} // class is not visible to the Lucee core

		// now we check all started bundled (not only bundles used by core)
		Bundle[] bundles = bc.getBundleContext().getBundles();
		for (Bundle b: bundles) {
			if (b != bc.core && b.getEntry(classPath) != null) {
				try {
					return b.loadClass(className);
				}
				catch (Exception e) {
				} // class is not visible to that bundle
			}
		}

		// now we check lucee loader (SystemClassLoader?)
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();
		{
			ClassLoader cl = factory.getClass().getClassLoader();
			if (cl.getResource(classPath) != null) {
				try {
					// print.e("loader:");
					return cl.loadClass(className);
				}
				catch (Exception e) {
				}
			}
		}

		// now we check bundles not loaded
		Set<String> loaded = new HashSet<String>();
		for (Bundle b: bundles) {
			loaded.add(b.getSymbolicName() + "|" + b.getVersion());
		}

		try {
			File dir = factory.getBundleDirectory();
			File[] children = dir.listFiles(JAR_EXT_FILTER);
			BundleFile bf;
			String[] bi;
			for (int i = 0; i < children.length; i++) {
				try {
					bi = getBundleInfoFromFileName(children[i].getName());
					if (bi != null && loaded.contains(bi[0] + "|" + bi[1])) continue;
					bf = BundleFile.getInstance(children[i]);
					if (bf.isBundle() && !loaded.contains(bf.getSymbolicName() + "|" + bf.getVersion()) && bf.hasClass(className)) {
						Bundle b = null;
						try {
							b = _loadBundle(bc.getBundleContext(), bf);
						}
						catch (IOException e) {
						}

						if (b != null) {
							startIfNecessary(b);
							if (b.getEntry(classPath) != null) {
								try {
									return b.loadClass(className);
								}
								catch (Exception e) {
								} // class is not visible to that bundle
							}
						}
					}
				}
				catch (Throwable t2) {
					ExceptionUtil.rethrowIfNecessary(t2);
				}
			}
		}
		catch (Throwable t1) {
			ExceptionUtil.rethrowIfNecessary(t1);
		}

		return defaultValue;
	}

	public static String[] getBundleInfoFromFileName(String name) {
		name = ResourceUtil.removeExtension(name, name);
		int index = name.indexOf('-');
		if (index == -1) return null;
		return new String[] { name.substring(0, index), name.substring(index + 1) };
	}

	public static Bundle loadBundle(BundleContext bc, BundleFile bf, Bundle defaultValue) {
		if (!bf.isBundle()) return defaultValue;
		try {
			return loadBundle(bc, bf);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static Bundle loadBundle(BundleFile bf) throws IOException, BundleException {
		return loadBundle(CFMLEngineFactory.getInstance().getBundleContext(), bf);
	}

	private static Bundle loadBundle(BundleContext bc, BundleFile bf) throws IOException, BundleException {

		// check in loaded bundles
		if (bc == null) bc = CFMLEngineFactory.getInstance().getBundleContext();
		Bundle[] bundles = bc.getBundles();
		for (Bundle b: bundles) {
			if (bf.getSymbolicName().equals(b.getSymbolicName())) {
				if (b.getVersion().equals(bf.getVersion())) return b;
			}
		}
		return _loadBundle(bc, bf);
	}

	private static Bundle loadBundle(BundleContext bc, BundleFile bf, List<VersionDefinition> versionsDefinitions) throws IOException, BundleException {
		if (bc == null) bc = CFMLEngineFactory.getInstance().getBundleContext();

		// check in loaded bundles
		Bundle[] bundles = bc.getBundles();
		for (Bundle b: bundles) {
			if (bf.getSymbolicName().equals(b.getSymbolicName())) {
				if (VersionDefinition.matches(versionsDefinitions, b.getVersion())) return b;
			}
		}
		return _loadBundle(bc, bf);
	}

	private static long BUNDLE_JARS_DIR_MAX_AGE_IN_MS = 1000;
	private static File[] bundleDirectoryJars;
	private static long bundleDirectoryJarsLastCheck = 0;
	private static Object bundleDirectoryJarsToken = new Object();

	private static Bundle loadBundleByPackage(BundleContext bc, PackageQuery pq, Set<Bundle> loadedBundles, boolean startIfNecessary, Set<String> parents)
			throws BundleException, IOException {
		// check hardcoded mappings
		try {
			{

				// init dyn mapping
				if (packageBundleMappingDyn == null) {
					initPackageMapping();
				}
				boolean second = false;
				while (true) {
					// look for match in mapping
					SoftReference<Map<String, BundleFile>> sr = packageBundleMappingDyn.get(pq.getName());
					Map<String, BundleFile> map;
					if (sr != null && (map = sr.get()) != null) {

						for (BundleFile bf: map.values()) {
							if (!parents.contains(toString(bf))) continue;

							if (bf != null && bf.hasMatchingExportPackage(pq)) {
								return null;
							}
						}

						BundleFile match = null;
						for (BundleFile bf: map.values()) {
							if (bf != null && bf.hasMatchingExportPackage(pq)) {
								if (match == null || isNewerThan(bf.getVersion(), match.getVersion())) match = bf;
							}
						}
						if (map.size() > 1 && match != null) {
							print.e("yyyyyyyyyyyyyyyy");
							print.e(pq);
							print.e("+ " + match.getSymbolicName() + ":" + match.getVersionAsString() + " -> " + match.getFile());
							for (BundleFile _bf: map.values()) {
								print.e("- " + _bf.getSymbolicName() + ":" + _bf.getVersionAsString() + " -> " + _bf.getFile());
							}
						}

						if (match != null) {
							// load existing
							Bundle b = exists(loadedBundles, match.getSymbolicName(), pq.getVersionDefinitons());
							if (b != null) {
								if (startIfNecessary) _startIfNecessary(b, parents);
								if ("javax.xml.bind.helpers".equals(pq.getName())) {
									print.e(parents);
									print.ds("pppppp " + pq.getName() + " " + match.getSymbolicName() + ":" + match.getVersionAsString());
								}
								return b;
							}
							// load new
							b = loadBundle(bc, match, pq.getVersionDefinitons());
							if (b != null) {
								loadedBundles.add(b);
								if (startIfNecessary) _startIfNecessary(b, parents);
								if ("javax.xml.bind.helpers".equals(pq.getName())) {
									print.e(parents);
									print.ds("qqqqqqq " + pq.getName() + " " + match.getSymbolicName() + ":" + match.getVersionAsString());
								}
								return b;
							}
						}

					}
					if (second) break;

					if (packageBundleMappingDynLastMod < CFMLEngineFactory.getInstance().getCFMLEngineFactory().getBundleDirectory().lastModified()) {
						initPackageMapping();
					}
					else break;

					second = true;

				}
			}

			// if part of bootdelegation we ignore
			if (OSGiUtil.isPackageInBootelegation(pq.getName())) {
				return null;
			}

			String bn = packageBundleMapping.get(pq.getName());
			if (!StringUtil.isEmpty(bn)) {
				try {
					return loadBundle(bc, bn, null, null, null, startIfNecessary, false, pq.isRequired(), pq.isRequired() ? null : Boolean.FALSE);
				}
				catch (BundleException be) {
					if (pq.isRequired()) throw be;
					return null;
				}
			}

			// hardcoded mappings
			for (Entry<String, String> e: packageBundleMapping.entrySet()) {
				if (pq.getName().startsWith(e.getKey() + ".")) {
					try {
						return loadBundle(bc, e.getValue(), null, null, null, startIfNecessary, false, pq.isRequired(), pq.isRequired() ? null : Boolean.FALSE);
					}
					catch (BundleException be) {
						if (pq.isRequired()) throw be;
					}
				}
			}
		}
		catch (BundleException e) {
			log(e);
			if (pq.getResolution() == PackageQuery.RESOLUTION_NONE) throw e;
		}
		catch (Exception e) {
			log(e);
			if (pq.getResolution() == PackageQuery.RESOLUTION_NONE) throw ExceptionUtil.toIOException(e);
		}
		return null;
	}

	private static void initPackageMapping() throws IOException, BundleException {
		long now = System.currentTimeMillis();
		packageBundleMappingDyn = new LinkedHashMap<>();
		File[] children = getJarsFromBundleDirectory(CFMLEngineFactory.getInstance().getCFMLEngineFactory());
		BundleFile bf;
		for (File child: children) {
			bf = BundleFile.getInstance(child);
			if (bf.isBundle()) {
				for (PackageDefinition pd: bf.getExportPackageAsCollection()) {
					SoftReference<Map<String, BundleFile>> sr = packageBundleMappingDyn.get(pd.getName());
					Map<String, BundleFile> map;
					if (sr != null && (map = sr.get()) != null) {
						map.put(bf.getAbsolutePath(), bf);
						/*
						 * print.e("xxxxxxxxxxxxxxxxxxxx"); print.e(pd.getName()); for (BundleFile _bf: map.values()) {
						 * print.e(_bf.getSymbolicName() + ":" + _bf.getVersionAsString() + " -> " + bf.getFile()); }
						 */

					}
					else {
						map = new LinkedHashMap<>();
						map.put(bf.getAbsolutePath(), bf);
						packageBundleMappingDyn.put(pd.getName(), new SoftReference<Map<String, BundleFile>>(map));
					}
				}
			}
		}
		packageBundleMappingDynLastMod = now;
	}

	private static File[] getJarsFromBundleDirectory(CFMLEngineFactory factory) throws IOException {
		File[] tmp = bundleDirectoryJars;
		long now = System.currentTimeMillis();
		if (bundleDirectoryJars == null || (bundleDirectoryJarsLastCheck + BUNDLE_JARS_DIR_MAX_AGE_IN_MS) < now) {
			synchronized (bundleDirectoryJarsToken) {
				if (bundleDirectoryJars == null || (bundleDirectoryJarsLastCheck + BUNDLE_JARS_DIR_MAX_AGE_IN_MS) < now) {
					tmp = bundleDirectoryJars = factory.getBundleDirectory().listFiles(JAR_EXT_FILTER);
					bundleDirectoryJarsLastCheck = now;
				}
			}
		}
		return tmp;
	}

	private static void resetJarsFromBundleDirectory(CFMLEngineFactory factory) throws IOException {
		if (bundleDirectoryJars != null) {
			synchronized (bundleDirectoryJarsToken) {
				if (bundleDirectoryJars != null) {
					bundleDirectoryJars = null;
				}
			}
		}
	}

	private static Bundle exists(Set<Bundle> loadedBundles, String symbolicName, List<VersionDefinition> versionDefinitions) {
		if (loadedBundles != null) {
			for (Bundle b: loadedBundles) {
				if (b.getSymbolicName().equals(symbolicName)) {
					if (VersionDefinition.matches(versionDefinitions, b.getVersion())) {
						return b;
					}
				}
			}
		}
		return null;
	}

	private static Bundle exists(Set<Bundle> loadedBundles, BundleRange br) {
		if (loadedBundles != null) {
			Bundle b, match = null;
			Iterator<Bundle> it = loadedBundles.iterator();
			while (it.hasNext()) {
				b = it.next();
				if (br.matches(b)) {
					if (match == null || OSGiUtil.isNewerThan(b.getVersion(), match.getVersion())) match = b;
				}
			}
			return match;
		}
		return null;
	}

	public static Bundle loadBundle(BundleContext bc, final BundleRange bundleRange, Identification id, List<Resource> addional, boolean startIfNecessary,
			boolean versionOnlyMattersForDownload, boolean downloadIfNecessary, Boolean printExceptions) throws BundleException {
		try {
			return _loadBundle(bc == null ? CFMLEngineFactory.getInstance().getBundleContext() : bc, bundleRange, id, addional, startIfNecessary, null,
					versionOnlyMattersForDownload, downloadIfNecessary, printExceptions);
		}
		catch (StartFailedException sfe) {
			throw sfe.bundleException;
		}
	}

	public static Bundle loadBundle(String name, Version version, Identification id, List<Resource> addional, boolean startIfNecessary) throws BundleException {
		try {
			return _loadBundle(CFMLEngineFactory.getInstance().getBundleContext(),
					new BundleRange(name.trim()).setVersionRange(new BundleRange.VersionRange().add(version, VersionDefinition.EQ)), id, addional, startIfNecessary, null, false,
					true, null);
		}
		catch (StartFailedException sfe) {
			throw sfe.bundleException;
		}
	}

	public static Bundle loadBundle(BundleContext bc, String name, Version version, Identification id, List<Resource> addional, boolean startIfNecessary,
			boolean versionOnlyMattersForDownload, boolean downloadIfNecessary, Boolean printExceptions) throws BundleException {
		try {
			return _loadBundle(bc == null ? CFMLEngineFactory.getInstance().getBundleContext() : bc,
					new BundleRange(name.trim()).setVersionRange(new BundleRange.VersionRange().add(version, VersionDefinition.EQ)), id, addional, startIfNecessary, null,
					versionOnlyMattersForDownload, downloadIfNecessary, printExceptions);
		}
		catch (StartFailedException sfe) {
			throw sfe.bundleException;
		}
	}

	public static Bundle _loadBundle(BundleContext bc, final BundleRange bundleRange, Identification id, List<Resource> addional, boolean startIfNecessary, Set<String> parents,
			boolean versionOnlyMattersForDownload, boolean downloadIfNecessary, Boolean printExceptions) throws BundleException, StartFailedException {
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();
		boolean[] arrVersionMatters = versionOnlyMattersForDownload && bundleRange.getVersionRange() != null && !bundleRange.getVersionRange().isEmpty()
				? new boolean[] { true, false }
				: new boolean[] { true };

		// check in loaded bundles
		if (bc == null) bc = engine.getBundleContext();
		Bundle[] bundles = bc.getBundles();
		StringBuilder versionsFound = new StringBuilder();
		Bundle match = null;
		for (boolean versionMatters: arrVersionMatters) {
			for (Bundle b: bundles) {
				if (bundleRange.getName().equalsIgnoreCase(b.getSymbolicName())) {
					if (bundleRange.getVersionRange() == null || bundleRange.getVersionRange().isEmpty() || !versionMatters
							|| bundleRange.getVersionRange().isWithin(b.getVersion())) {
						if (match == null || OSGiUtil.isNewerThan(b.getVersion(), match.getVersion())) match = b;
					}
					else {
						if (versionsFound.length() > 0) versionsFound.append(", ");
						versionsFound.append(b.getVersion().toString());
					}
				}
			}
		}
		if (match != null) {
			if (startIfNecessary) {
				try {
					_startIfNecessary(match, parents);
				}
				catch (BundleException be) {
					throw new StartFailedException(be, match);
				}
			}
			return match;
		}

		// is it in jar directory but not loaded
		BundleFile bf = _getBundleFile(factory, bundleRange, addional, versionsFound);
		if (versionOnlyMattersForDownload && (bf == null || !bf.isBundle())) bf = _getBundleFile(factory, bundleRange.getName(), null, addional, versionsFound);
		if (bf != null && bf.isBundle()) {
			Bundle b = null;
			try {
				b = _loadBundle(bc, bf);
			}
			catch (IOException e) {
				LogUtil.log(ThreadLocalPageContext.get(), OSGiUtil.class.getName(), e);
			}
			if (b != null) {
				if (startIfNecessary) {
					try {
						startIfNecessary(b);
					}
					catch (BundleException be) {
						throw new StartFailedException(be, b);
					}
				}
				return b;
			}
		}

		// if not found try to download
		if (downloadIfNecessary) {
			try {
				Bundle b;
				if (bundleRange.getVersionRange() != null && !bundleRange.getVersionRange().isEmpty()) {
					// TODO not only check for from version, request a range, but that needs an adjustment with the
					// provider
					BundleFile _bf = improveFileName(
							BundleFile.getInstance(factory.downloadBundle(bundleRange.getName(), bundleRange.getVersionRange().getFrom().getVersion().toString(), id)));
					resetJarsFromBundleDirectory(factory);
					b = _loadBundle(bc, _bf);
				}
				else {
					// MUST find out why this breaks at startup with commandbox if version exists
					Resource r = downloadBundle(factory, bundleRange.getName(), null, id);
					BundleFile src = BundleFile.getInstance(r);
					BundleFile trg = improveFileName(src);
					if (src != trg) r = ResourceUtil.toResource(trg.getFile());

					resetJarsFromBundleDirectory(factory);
					b = _loadBundle(bc, r);
				}

				if (startIfNecessary) {
					try {
						_start(b, parents);
					}
					catch (BundleException be) {
						throw new StartFailedException(be, b);
					}
				}
				return b;
			}
			catch (Exception e) {
				log(e);
			}
		}

		String localDir = "";
		try {
			localDir = " (" + factory.getBundleDirectory() + ")";
		}
		catch (IOException e) {
		}
		String upLoc = "";
		if (!ThreadLocalPageContext.insideServerNewInstance()) {
			try {
				upLoc = " (" + factory.getUpdateLocation() + ")";
			}
			catch (IOException e) {
			}
		}
		else {
			upLoc = " (" + ConfigWebFactory.DEFAULT_LOCATION + ")";
		}
		String bundleError = "";
		String parentBundle = parents == null ? " " : String.join(",", parents);
		String downloadText = downloadIfNecessary ? " or from the update provider [" + upLoc + "]" : "";
		if (versionsFound.length() > 0) {
			bundleError = "The OSGi Bundle with name [" + bundleRange.getName() + "] for [" + parentBundle + "] is not available in version [" + bundleRange.getVersionRange()
					+ "] locally [" + localDir + "]" + downloadText + ", the following versions are available locally [" + versionsFound + "].";
		}
		else if (bundleRange.getVersionRange() != null) {
			bundleError = "The OSGi Bundle with name [" + bundleRange.getName() + "] in version [" + bundleRange.getVersionRange() + "] for [" + parentBundle
					+ "] is not available locally [" + localDir + "]" + downloadText + ".";
		}
		else {
			bundleError = "The OSGi Bundle with name [" + bundleRange.getName() + "] for [" + parentBundle + "] is not available locally [" + localDir + "]" + downloadText + ".";
		}

		if (printExceptions == null) printExceptions = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.cli.printExceptions", null), false);
		try {
			throw new BundleException(bundleError);
		}
		catch (BundleException be) {
			if (printExceptions) be.printStackTrace();
			throw be;
		}
	}

	private static Resource downloadBundle(CFMLEngineFactory factory, final String symbolicName, String symbolicVersion, Identification id) throws IOException, BundleException {
		resetJarsFromBundleDirectory(factory);
		if (!Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.enable.bundle.download", null), true)) {
			boolean printExceptions = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.cli.printExceptions", null), false);
			String bundleError = "Lucee is missing the Bundle jar [" + (symbolicVersion != null ? symbolicName + ":" + symbolicVersion : symbolicName)
					+ "], and has been prevented from downloading it. If this jar is not a core jar,"
					+ " it will need to be manually downloaded and placed in the {{lucee-server}}/context/bundles directory.";
			try {
				throw new RuntimeException(bundleError);
			}
			catch (RuntimeException re) {
				if (printExceptions) re.printStackTrace();
				throw re;
			}
		}
		final Resource jarDir = ResourceUtil.toResource(factory.getBundleDirectory());
		final URL updateProvider;
		if (ThreadLocalPageContext.insideServerNewInstance()) {
			ConfigServer cs = ThreadLocalConfigServer.get();
			updateProvider = cs != null ? cs.getUpdateLocation() : factory.getUpdateLocation();
		}
		else {
			updateProvider = factory.getUpdateLocation();
		}

		if (symbolicVersion == null) symbolicVersion = "latest";
		final URL updateUrl = new URL(updateProvider, "/rest/update/provider/download/" + symbolicName + "/" + symbolicVersion + "/" + (id != null ? id.toQueryString() : "")
				+ (id == null ? "?" : "&") + "allowRedirect=true"

		);
		log(Logger.LOG_INFO, "Downloading bundle [" + symbolicName + ":" + symbolicVersion + "] from [" + updateUrl + "]");

		int code;
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) updateUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.connect();
			code = conn.getResponseCode();
		}
		catch (UnknownHostException e) {
			throw new IOException("Downloading the bundle  [" + symbolicName + ":" + symbolicVersion + "] from [" + updateUrl + "] failed", e);
		}
		// the update provider is not providing a download for this
		if (code != 200) {
			int count = 1;
			// the update provider can also provide a different (final) location for this
			while ((code == 301 || code == 302) && count++ <= MAX_REDIRECTS) {
				String location = conn.getHeaderField("Location");
				// just in case we check invalid names
				if (location == null) location = conn.getHeaderField("location");
				if (location == null) location = conn.getHeaderField("LOCATION");
				LogUtil.log(Log.LEVEL_INFO, OSGiUtil.class.getName(), "Download redirected: " + location); // MUST remove

				conn.disconnect();
				URL url = new URL(location);
				try {
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(10000);
					conn.connect();
					code = conn.getResponseCode();
				}
				catch (final UnknownHostException e) {
					log(e);
					throw new IOException("Failed to download the bundle  [" + symbolicName + ":" + symbolicVersion + "] from [" + location + "]", e);
				}
			}

			// no download available!
			if (code != 200) {
				final String msg = "Download bundle failed for [" + symbolicName + "] in version [" + symbolicVersion + "] from [" + updateUrl
						+ "], please download manually and copy to [" + jarDir + "]";
				log(Logger.LOG_ERROR, msg);
				conn.disconnect();
				throw new IOException(msg);
			}

		}

		// extract version if necessary
		if ("latest".equals(symbolicVersion)) {
			// copy to temp file
			Resource temp = SystemUtil.getTempFile("jar", false);
			IOUtil.copy((InputStream) conn.getContent(), temp, true);
			try {
				conn.disconnect();

				// extract version and create file with correct name
				BundleFile bf = BundleFile.getInstance(temp);
				Resource jar = jarDir.getRealResource(symbolicName + "-" + bf.getVersionAsString() + ".jar");
				IOUtil.copy(temp, jar);
				return jar;
			}
			finally {
				temp.delete();
			}
		}
		else {
			Resource jar = jarDir.getRealResource(symbolicName + "-" + symbolicVersion + ".jar");
			IOUtil.copy((InputStream) conn.getContent(), jar, true);
			conn.disconnect();
			return jar;
		}
	}

	/**
	 * this should be used when you not want to load a Bundle to the system
	 *
	 * @param name
	 * @param version
	 * @param id only necessary if downloadIfNecessary is set to true
	 * @param downloadIfNecessary
	 * @return
	 * @throws BundleException
	 */
	public static BundleFile getBundleFile(String name, Version version, Identification id, List<Resource> addional, boolean downloadIfNecessary) throws BundleException {
		name = name.trim();

		CFMLEngine engine = CFMLEngineFactory.getInstance();
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();

		StringBuilder versionsFound = new StringBuilder();

		// is it in jar directory but not loaded
		BundleFile bf = _getBundleFile(factory, name, version, addional, versionsFound);
		if (bf != null) return bf;

		// if not found try to download
		if (downloadIfNecessary && version != null) {
			try {
				resetJarsFromBundleDirectory(factory);
				bf = BundleFile.getInstance(factory.downloadBundle(name, version.toString(), id));
				if (bf.isBundle()) return bf;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		if (versionsFound.length() > 0) throw new BundleException("The OSGi Bundle with name [" + name + "] is not available in version [" + version
				+ "] locally or from the update provider, the following versions are available locally [" + versionsFound + "].");
		if (version != null)
			throw new BundleException("The OSGi Bundle with name [" + name + "] in version [" + version + "] is not available locally or from the update provider.");
		throw new BundleException("The OSGi Bundle with name [" + name + "] is not available locally or from the update provider.");
	}

	/**
	 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal
	 *         to, or greater than the second.
	 */
	public static int compare(final Version left, final Version right) {

		// major
		if (left.getMajor() > right.getMajor()) return 100;
		if (left.getMajor() < right.getMajor()) return -100;

		// minor
		if (left.getMinor() > right.getMinor()) return 50;
		if (left.getMinor() < right.getMinor()) return -50;

		// micro
		if (left.getMicro() > right.getMicro()) return 10;
		if (left.getMicro() < right.getMicro()) return -10;

		// qualifier
		// left
		String q = left.getQualifier();
		int index = q.indexOf('-');
		String qla = index == -1 ? "" : q.substring(index + 1).trim();
		String qln = index == -1 ? q : q.substring(0, index);
		int ql = StringUtil.isEmpty(qln) ? Integer.MIN_VALUE : Caster.toIntValue(qln, Integer.MAX_VALUE);

		// right
		q = right.getQualifier();
		index = q.indexOf('-');
		String qra = index == -1 ? "" : q.substring(index + 1).trim();
		String qrn = index == -1 ? q : q.substring(0, index);
		int qr = StringUtil.isEmpty(qln) ? Integer.MIN_VALUE : Caster.toIntValue(qrn, Integer.MAX_VALUE);

		if (ql > qr) return 5;
		if (ql < qr) return -5;

		int qlan = qualifierAppendix2Number(qla);
		int qran = qualifierAppendix2Number(qra);

		if (qlan > qran) return 2;
		if (qlan < qran) return -2;

		if (qlan == QUALIFIER_APPENDIX_OTHER && qran == QUALIFIER_APPENDIX_OTHER) return left.compareTo(right) > 0 ? 1 : -1;

		return 0;
	}

	/**
	 * check left value against right value
	 *
	 * @param left
	 * @param right
	 * @return returns if right is newer than left
	 * @deprecated use instead "compare"
	 */
	@Deprecated
	public static boolean isNewerThan(final Version left, final Version right) {
		return compare(left, right) > 0;
	}

	private static int qualifierAppendix2Number(String str) {
		if (Util.isEmpty(str, true)) return QUALIFIER_APPENDIX_STABLE;
		if ("SNAPSHOT".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_SNAPSHOT;
		if ("BETA".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_BETA;
		if ("RC".equalsIgnoreCase(str)) return QUALIFIER_APPENDIX_RC;
		return QUALIFIER_APPENDIX_OTHER;
	}

	public static BundleFile getBundleFile(String name, Version version, Identification id, List<Resource> addional, boolean downloadIfNecessary, BundleFile defaultValue) {
		name = name.trim();

		CFMLEngine engine = CFMLEngineFactory.getInstance();
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();

		StringBuilder versionsFound = new StringBuilder();

		// is it in jar directory but not loaded
		BundleFile bf = _getBundleFile(factory, name, version, addional, versionsFound);
		if (bf != null) return bf;

		// if not found try to download
		if (downloadIfNecessary && version != null) {
			try {
				resetJarsFromBundleDirectory(factory);
				bf = BundleFile.getInstance(factory.downloadBundle(name, version.toString(), id));
				if (bf.isBundle()) return bf;
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		return defaultValue;
	}

	private static BundleFile _getBundleFile(CFMLEngineFactory factory, String name, Version version, List<Resource> addional, StringBuilder versionsFound) {
		return _getBundleFile(factory, new BundleRange(name).setVersionRange(new BundleRange.VersionRange().add(version, VersionDefinition.EQ)), addional, versionsFound);
	}

	private static BundleFile _getBundleFile(CFMLEngineFactory factory, BundleRange bundleRange, List<Resource> addional, StringBuilder versionsFound) {

		Resource match = null;
		try {

			BundleFile mbf = null;
			Resource dir = ResourceUtil.toResource(factory.getBundleDirectory());
			// first we check if there is a file match (fastest solution)
			List<Resource> jars = createPossibleNameMatches(dir, addional, bundleRange);
			for (Resource jar: jars) {
				if (jar.isFile()) {
					match = jar;
					BundleFile bf = BundleFile.getInstance(jar);
					if (bf.isBundle() && bundleRange.matches(bf)) {
						if (mbf == null || OSGiUtil.isNewerThan(bf.getVersion(), mbf.getVersion())) mbf = bf;
					}
				}
			}
			if (mbf != null) {
				return improveFileName(mbf);
			}
			List<Resource> children = listFiles(dir, addional, JAR_EXT_FILTER);

			// now we check all jar files
			{

				// now we check by Manifest comparsion, name not necessary reflect the correct bundle info
				BundleFile bf;
				for (boolean checkBundleRange: checkBundleRanges) {
					mbf = null;
					for (Resource child: children) {
						if (checkBundleRange && !new Filter(bundleRange).accept(child.getName())) continue;
						match = child;
						bf = BundleFile.getInstance(child);
						if (bf.isBundle()) {
							if (bf.getSymbolicName().equals(bundleRange.getName())) {
								if (bundleRange.matches(bf)) {
									if (mbf == null || OSGiUtil.isNewerThan(bf.getVersion(), mbf.getVersion())) mbf = bf;
								}
								else {
									if (versionsFound != null) {
										if (versionsFound.length() > 0) versionsFound.append(", ");
										versionsFound.append(bf.getVersionAsString());
									}
								}
							}
						}
					}
					if (mbf != null) {
						return improveFileName(mbf);
					}
				}
			}

		}
		catch (Exception e) {
			log(e);
			if (match != null) {
				if (FileUtil.isLocked(match)) {
					log(Log.LEVEL_ERROR, "cannot load the bundle [" + match + "], bundle seem to have a windows lock");

					// in case the file exists, but is locked we create a copy of if and use that copy
					BundleFile bf;
					try {
						bf = BundleFile.getInstance(FileUtil.createTempResourceFromLockedResource(match, false));
						if (bf.isBundle() && bundleRange.matches(bf)) {
							return bf;
						}
					}
					catch (Exception e1) {
						log(e1);
					}
				}
			}
		}
		return null;
	}

	/**
	 * rename file to match the Manifest information
	 * 
	 * @param bf
	 */
	private static BundleFile improveFileName(BundleFile bf) {
		File f = bf.getFile();
		String preferedName = bf.getSymbolicName() + "-" + bf.getVersionAsString() + ".jar";
		if (!preferedName.equals(f.getName())) {
			try {
				File nf = new File(f.getParentFile(), preferedName);

				if (f.renameTo(nf)) {
					return BundleFile.getInstance(nf);
				}
				else {
					IOUtil.copy(new FileInputStream(f), new FileOutputStream(nf), true, true);
					if (!f.delete()) {
						f.deleteOnExit();
					}
					else {
						return BundleFile.getInstance(nf);
					}
				}
			}
			catch (Exception e) {
				LogUtil.log("OSGi", e);
			}
		}
		return bf;
	}

	private static List<Resource> createPossibleNameMatches(Resource dir, List<Resource> addional, BundleRange bundleRange) {
		List<Resource> resources = new ArrayList<Resource>();

		// do we have a from match
		VersionRange vr = bundleRange.getVersionRange();
		if (vr != null) {
			VersionDefinition from = vr.getFrom();
			if (from != null && from.version != null && (from.op == VersionDefinition.EQ || from.op == VersionDefinition.GTE)) {
				String name = bundleRange.getName();
				String regular = name + "-" + from.version + ".jar";
				String dash = name.replace('.', '-') + "-" + (from.version.toString().replace('.', '-')) + ".jar";
				String dot = name.replace('-', '.') + "-" + from.version + ".jar";

				String[] patterns = regular.equals(dot) ? new String[] { regular, dash } : new String[] { regular, dot, dash };

				for (String pattern: patterns) {
					resources.add(dir.getRealResource(pattern));
				}
			}
		}

		if (addional != null && !addional.isEmpty()) {
			String name = bundleRange.getName();
			String[] patterns = new String[] { name + "-", name.replace('.', '-'), name.replace('-', '.') };

			Iterator<Resource> it = addional.iterator();
			Resource res;
			while (it.hasNext()) {
				res = it.next();
				if (res.isDirectory()) {
					for (Resource child: res.listResources()) {
						if (child.isFile()) {
							if (!child.getName().endsWith(".jar")) continue;
							for (String pattern: patterns) {
								if (child.getName().startsWith(pattern)) resources.add(child);
							}
						}
					}
				}
				else if (res.isFile()) {
					if (!res.getName().endsWith(".jar")) continue;
					for (String pattern: patterns) {
						if (res.getName().startsWith(pattern)) resources.add(res);
					}
				}
			}
		}
		return resources;
	}

	private static List<Resource> listFiles(Resource dir, List<Resource> addional, Filter filter) {
		List<Resource> children = new ArrayList<Resource>();
		_add(children, dir.listResources(filter));
		if (addional != null && !addional.isEmpty()) {
			Iterator<Resource> it = addional.iterator();
			Resource res;
			while (it.hasNext()) {
				res = it.next();
				if (res.isDirectory()) {
					_add(children, res.listResources(filter));
				}
				else if (res.isFile()) {
					if (filter.accept(res, res.getName())) children.add(res);
				}
			}
		}
		return children;
	}

	private static void _add(List<Resource> children, Resource[] reses) {
		if (reses == null || reses.length == 0) return;
		for (Resource res: reses) {
			children.add(res);
		}
	}

	/**
	 * get all local bundles (even bundles not loaded/installed)
	 *
	 * @param name
	 * @param version
	 * @return
	 */
	public static List<BundleDefinition> getBundleDefinitions() {
		CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		return getBundleDefinitions(engine.getBundleContext());
	}

	public static List<BundleDefinition> getBundleDefinitions(BundleContext bc) {
		Set<String> set = new HashSet<>();
		List<BundleDefinition> list = new ArrayList<>();
		Bundle[] bundles = bc.getBundles();
		for (Bundle b: bundles) {
			list.add(new BundleDefinition(b));
			set.add(b.getSymbolicName() + ":" + b.getVersion());
		}
		// is it in jar directory but not loaded
		CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();
		try {
			File[] children = factory.getBundleDirectory().listFiles(JAR_EXT_FILTER);
			BundleFile bf;
			for (int i = 0; i < children.length; i++) {
				try {
					bf = BundleFile.getInstance(children[i]);
					if (bf.isBundle() && !set.contains(bf.getSymbolicName() + ":" + bf.getVersion())) list.add(new BundleDefinition(bf.getSymbolicName(), bf.getVersion()));
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
		}
		catch (IOException ioe) {
		}

		return list;
	}

	public static Bundle getBundleLoaded(String name, Version version, Bundle defaultValue) {
		CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		return getBundleLoaded(engine.getBundleContext(), name, version, defaultValue);
	}

	public static Bundle getBundleLoaded(BundleContext bc, String name, Version version, Bundle defaultValue) {
		name = name.trim();

		Bundle[] bundles = bc.getBundles();
		for (Bundle b: bundles) {
			if (name.equalsIgnoreCase(b.getSymbolicName())) {
				if (version == null || version.equals(b.getVersion())) {
					return b;
				}
			}
		}
		return defaultValue;
	}

	public static Bundle loadBundleFromLocal(String name, Version version, List<Resource> addional, boolean loadIfNecessary, Bundle defaultValue) {
		CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		return loadBundleFromLocal(engine.getBundleContext(), name, version, addional, loadIfNecessary, defaultValue);
	}

	public static Bundle loadBundleFromLocal(BundleContext bc, String name, Version version, List<Resource> addional, boolean loadIfNecessary, Bundle defaultValue) {
		name = name.trim();
		Bundle[] bundles = bc.getBundles();
		for (Bundle b: bundles) {
			if (name.equalsIgnoreCase(b.getSymbolicName())) {
				if (version == null || version.equals(b.getVersion())) {
					return b;
				}
			}
		}
		if (!loadIfNecessary) return defaultValue;

		// is it in jar directory but not loaded

		CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();
		BundleFile bf = _getBundleFile(factory, name, version, addional, null);
		if (bf != null) {
			try {
				return _loadBundle(bc, bf);
			}
			catch (Exception e) {
			}
		}

		return defaultValue;
	}

	/**
	 * get local bundle, but does not download from update provider!
	 *
	 * @param name
	 * @param version
	 * @return
	 * @throws BundleException
	 */
	public static void removeLocalBundle(String name, Version version, List<Resource> addional, boolean removePhysical, boolean doubleTap) throws BundleException {
		name = name.trim();
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();

		// first we look for an active bundle and do stop it
		Bundle b = getBundleLoaded(name, version, null);
		if (b != null) {
			stopIfNecessary(b);
			b.uninstall();
		}

		if (!removePhysical) return;

		// now we remove the file
		BundleFile bf = _getBundleFile(factory, name, version, null, null);
		if (bf != null) {
			if (!bf.delete() && doubleTap) bf.deleteOnExit();
		}
	}

	public static void removeLocalBundleSilently(String name, Version version, List<Resource> addional, boolean removePhysical) {
		try {
			removeLocalBundle(name, version, addional, removePhysical, true);
		}
		catch (Exception e) {
		}
	}

	// bundle stuff
	public static void startIfNecessary(Bundle[] bundles) throws BundleException {
		for (Bundle b: bundles) {
			startIfNecessary(b);
		}
	}

	public static Bundle startIfNecessary(Bundle bundle) throws BundleException {
		return _startIfNecessary(bundle, null);
	}

	private static Bundle _startIfNecessary(Bundle bundle, Set<String> parents) throws BundleException {
		if (bundle.getState() == Bundle.ACTIVE) return bundle;
		return _start(bundle, parents);
	}

	public static Bundle start(Bundle bundle) throws BundleException {
		try {
			return _start(bundle, null);
		}
		finally {
			bundlesThreadLocal.get().clear();
		}
	}

	private static Bundle _start(Bundle bundle, Set<String> parents) throws BundleException {
		if (bundle == null) return bundle;
		String bn = toString(bundle);
		if (bundlesThreadLocal.get().contains(bn)) return bundle;
		bundlesThreadLocal.get().add(bn);
		String fh = bundle.getHeaders().get("Fragment-Host");
		// Fragment cannot be started
		if (!Util.isEmpty(fh)) {
			log(Log.LEVEL_DEBUG, "Do not start [" + bundle.getSymbolicName() + "], because this is a fragment bundle for [" + fh + "]");
			return bundle;
		}

		log(Log.LEVEL_DEBUG, "Start bundle: [" + bundle.getSymbolicName() + ":" + bundle.getVersion().toString() + "]");

		// check if required related bundles are missing and load them if necessary
		final List<BundleDefinition> failedBD = new ArrayList<OSGiUtil.BundleDefinition>();
		if (parents == null) parents = new HashSet<String>();
		Set<Bundle> loadedBundles = loadBundles(parents, bundle, null, failedBD);
		try {
			// startIfNecessary(loadedBundles.toArray(new Bundle[loadedBundles.size()]));
			BundleUtil.start(bundle);
		}
		catch (BundleException be2) {
			// print.e(be);
			List<PackageQuery> listPackages = getRequiredPackages(bundle);
			List<PackageQuery> failedPD = new ArrayList<PackageQuery>();
			try {
				loadPackages(bundle.getBundleContext(), parents, loadedBundles, listPackages, bundle, failedPD);
				BundleUtil.start(bundle);
			}
			catch (BundleException be3) {
				try {
					if (resolveBundleLoadingIssues(bundle.getBundleContext(), ThreadLocalPageContext.getConfig(), be3)) BundleUtil.start(bundle);
					else throw be3;
				}
				catch (BundleException be4) {
					if (failedBD.size() > 0) {
						Iterator<BundleDefinition> itt = failedBD.iterator();
						BundleDefinition _bd;
						StringBuilder sb = new StringBuilder("Lucee was not able to download/load the following bundles [");
						while (itt.hasNext()) {
							_bd = itt.next();
							sb.append(_bd.name + ":" + _bd.getVersionAsString()).append(';');
						}
						sb.append("]");
						throw new BundleException(be2.getMessage() + sb, be2.getCause());
					}
					throw be4;
				}
			}
		}

		return bundle;
	}

	private static void loadPackages(BundleContext bc, final Set<String> parents, final Set<Bundle> loadedBundles, List<PackageQuery> listPackages, final Bundle bundle,
			final List<PackageQuery> failedPD) {
		PackageQuery pq;
		Iterator<PackageQuery> it = listPackages.iterator();
		parents.add(toString(bundle));
		while (it.hasNext()) {
			pq = it.next();
			try {
				loadBundleByPackage(bc, pq, loadedBundles, true, parents);
			}
			catch (Exception _be) {
				failedPD.add(pq);
				// log(_be);
			}
		}
	}

	private static Set<Bundle> loadBundles(final Set<String> parents, final Bundle bundle, List<Resource> addional, final List<BundleDefinition> failedBD) throws BundleException {
		Set<Bundle> loadedBundles = new HashSet<Bundle>();
		loadedBundles.add(bundle);
		parents.add(toString(bundle));

		List<BundleRange> listBundles = getRequiredBundles(bundle);
		Bundle b;
		BundleRange br;
		Iterator<BundleRange> it = listBundles.iterator();
		List<StartFailedException> secondChance = null;
		while (it.hasNext()) {
			br = it.next();
			b = exists(loadedBundles, br);
			if (b != null) {
				_startIfNecessary(b, parents);
				continue;
			}
			try {
				// if(parents==null) parents=new HashSet<Bundle>();

				b = _loadBundle(bundle.getBundleContext(), br, ThreadLocalPageContext.getConfig().getIdentification(), addional, true, parents, false, true, null);

				loadedBundles.add(b);
			}
			catch (StartFailedException sfe) {

				sfe.setBundleDefinition(br.toBundleDefintion());
				if (secondChance == null) secondChance = new ArrayList<StartFailedException>();
				secondChance.add(sfe);
			}
			catch (BundleException _be) {
				failedBD.add(br.toBundleDefintion()); // TODO better solution for this
				log(_be);
			}
		}
		// we do this because it maybe was relaying on other bundles now loaded
		// TODO rewrite the complete impl so didd is not necessary
		if (secondChance != null) {
			Iterator<StartFailedException> _it = secondChance.iterator();
			StartFailedException sfe;
			while (_it.hasNext()) {
				sfe = _it.next();
				try {
					_startIfNecessary(sfe.bundle, parents);
					loadedBundles.add(sfe.bundle);
				}
				catch (BundleException _be) {
					failedBD.add(sfe.getBundleDefinition());
					log(_be);
				}
			}
		}
		return loadedBundles;
	}

	private static String toString(Bundle b) {
		return b.getSymbolicName() + ":" + b.getVersion().toString();
	}

	private static String toString(BundleFile bf) {
		return bf.getSymbolicName() + ":" + bf.getVersion().toString();
	}

	public static void stopIfNecessary(Bundle bundle) throws BundleException {
		if (isFragment(bundle) || bundle.getState() != Bundle.ACTIVE) return;
		stop(bundle);
	}

	public static void stop(Bundle b) throws BundleException {
		b.stop();
	}

	public static void uninstall(Bundle b) throws BundleException {
		b.uninstall();
	}

	public static boolean isFragment(Bundle bundle) {
		return (bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0;
	}

	public static boolean isFragment(BundleFile bf) {
		return !StringUtil.isEmpty(bf.getFragementHost(), true);
	}

	public static List<BundleRange> getRequiredBundles(Bundle bundle) throws BundleException {
		List<BundleRange> rtn = new ArrayList<BundleRange>();
		BundleRevision rev = bundle.adapt(BundleRevision.class);
		List<Requirement> requirements = rev.getRequirements(null);
		Iterator<Requirement> it = requirements.iterator();
		Requirement r;
		Entry<String, String> e;
		String value, name;
		int index, start, end;
		BundleRange br;
		while (it.hasNext()) {
			r = it.next();
			Iterator<Entry<String, String>> iit = r.getDirectives().entrySet().iterator();
			while (iit.hasNext()) {
				e = iit.next();
				if (!"filter".equals(e.getKey())) continue;
				value = e.getValue();
				// name
				index = value.indexOf("(osgi.wiring.bundle");
				if (index == -1) continue;
				start = value.indexOf('=', index);
				end = value.indexOf(')', index);
				if (start == -1 || end == -1 || end < start) continue;
				name = value.substring(start + 1, end).trim();
				rtn.add(br = new BundleRange(name));
				br.setVersionRange(extractVersionRange(value));
			}
		}
		return rtn;
	}

	private static BundleRange.VersionRange extractVersionRange(String value) throws BundleException {
		String strBV = "(bundle-version";
		int last = 0;
		int index, op, start, end;
		boolean isNegated;
		BundleRange.VersionRange vr = new BundleRange.VersionRange();
		while ((index = value.indexOf(strBV, last)) != -1) {
			last = index + strBV.length();

			isNegated = index > 0 && value.charAt(index - 1) == '!';
			end = value.indexOf(')', index);

			start = value.indexOf("<=", index);
			op = -1;

			// Version Defintion
			if (start != -1 && start < end) {
				op = VersionDefinition.LTE;
				start += 2;
			}
			else {
				start = value.indexOf(">=", index);
				if (start != -1 && start < end) {
					op = VersionDefinition.GTE;
					start += 2;
				}
				else {
					start = value.indexOf("=", index);
					if (start != -1 && start < end) {
						op = VersionDefinition.EQ;
						start++;
					}
				}
			}
			if (isNegated) op = VersionDefinition.negate(op, -1);

			if (op == -1 || start == -1 || end == -1 || end < start) continue;

			vr.add(value.substring(start, end).trim(), op);
		}

		return vr;

	}

	public static List<PackageQuery> getRequiredPackages(Bundle bundle) throws BundleException {
		List<PackageQuery> rtn = new ArrayList<PackageQuery>();
		BundleRevision br = bundle.adapt(BundleRevision.class);
		List<Requirement> requirements = br.getRequirements(null);
		Iterator<Requirement> it = requirements.iterator();
		Requirement r;
		Entry<String, String> e;
		PackageQuery pd;
		int res = PackageQuery.RESOLUTION_NONE;
		while (it.hasNext()) {
			r = it.next();

			Iterator<Entry<String, String>> iit = r.getDirectives().entrySet().iterator();
			pd = null;
			inner: while (iit.hasNext()) {
				e = iit.next();
				if ("filter".equals(e.getKey())) {
					pd = toPackageQuery(e.getValue());
				}
				if ("resolution".equals(e.getKey())) {
					res = PackageQuery.toResolution(e.getValue(), PackageQuery.RESOLUTION_NONE);
				}
			}
			if (pd != null) {
				if (res != PackageQuery.RESOLUTION_NONE) {
					pd.setResolution(res);
				}
				rtn.add(pd);
			}
		}
		return rtn;
	}

	private static PackageQuery toPackageQuery(String value) throws BundleException {
		// name(&(osgi.wiring.package=org.jboss.logging)(version>=3.3.0)(!(version>=4.0.0)))
		int index = value.indexOf("(osgi.wiring.package");
		if (index == -1) {
			return null;
		}
		int start = value.indexOf('=', index);
		int end = value.indexOf(')', index);
		if (start == -1 || end == -1 || end < start) {
			return null;
		}
		String name = value.substring(start + 1, end).trim();
		PackageQuery pd = new PackageQuery(name);
		int last = end, op;
		boolean not;
		// version
		while ((index = value.indexOf("(version", last)) != -1) {
			op = -1;

			end = value.indexOf(')', index);

			start = value.indexOf("<=", index);
			if (start != -1 && start < end) {
				op = VersionDefinition.LTE;
				start += 2;
			}
			else {
				start = value.indexOf(">=", index);
				if (start != -1 && start < end) {
					op = VersionDefinition.GTE;
					start += 2;
				}
				else {
					start = value.indexOf("==", index);
					if (start != -1 && start < end) {
						op = VersionDefinition.EQ;
						start += 2;
					}
					else {
						start = value.indexOf("!=", index);
						if (start != -1 && start < end) {
							op = VersionDefinition.NEQ;
							start += 2;
						}
						else {
							start = value.indexOf("=", index);
							if (start != -1 && start < end) {
								op = VersionDefinition.EQ;
								start += 1;
							}
							else {
								start = value.indexOf("<", index);
								if (start != -1 && start < end) {
									op = VersionDefinition.LT;
									start += 1;
								}
								else {
									start = value.indexOf(">", index);
									if (start != -1 && start < end) {
										op = VersionDefinition.GT;
										start += 1;
									}
								}
							}
						}
					}
				}
			}
			not = value.charAt(index - 1) == '!';
			last = end;
			if (op == -1 || start == -1 || end == -1 || end < start) continue;
			pd.addVersion(op, value.substring(start, end).trim(), not);
		}

		return pd;
	}

	private static Bundle _loadBundle(BundleContext context, BundleFile bundle) throws IOException, BundleException {
		return _loadBundle(context, bundle.getAbsolutePath(), bundle.getInputStream(), true);
	}

	private static Bundle _loadBundle(BundleContext context, Resource bundle) throws IOException, BundleException {
		return _loadBundle(context, bundle.getAbsolutePath(), bundle.getInputStream(), true);
	}

	public static class VersionDefinition implements Serializable {

		private static final long serialVersionUID = 4915024473510761950L;

		public static final int LTE = 1;
		public static final int GTE = 2;
		public static final int EQ = 4;
		public static final int LT = 8;
		public static final int GT = 16;
		public static final int NEQ = 32;

		private Version version;
		private int op;

		public VersionDefinition(Version version, int op, boolean not) {
			this.version = version;

			if (not) {
				if (op == LTE) {
					op = GT;
					not = false;
				}
				else if (op == LT) {
					op = GTE;
					not = false;
				}
				else if (op == GTE) {
					op = LT;
					not = false;
				}
				else if (op == GT) {
					op = LTE;
					not = false;
				}
				else if (op == EQ) {
					op = NEQ;
					not = false;
				}
				else if (op == NEQ) {
					op = EQ;
					not = false;
				}
			}
			this.op = op;

		}

		public static String toOperator(int op, String defaultValue) {
			if (EQ == op) return "EQ";
			if (LTE == op) return "LTE";
			if (LT == op) return "LT";
			if (GTE == op) return "GTE";
			if (GT == op) return "GT";
			if (NEQ == op) return "NEQ";
			return defaultValue;
		}

		public static int negate(int op, int defaultValue) {
			if (EQ == op) return NEQ;
			if (LTE == op) return GT;
			if (LT == op) return GTE;
			if (GTE == op) return LT;
			if (GT == op) return LTE;
			if (NEQ == op) return EQ;
			return defaultValue;
		}

		public boolean matches(Version v) {
			if (EQ == op) return v.compareTo(version) == 0;
			if (LTE == op) return v.compareTo(version) <= 0;
			if (LT == op) return v.compareTo(version) < 0;
			if (GTE == op) return v.compareTo(version) >= 0;
			if (GT == op) return v.compareTo(version) > 0;
			if (NEQ == op) return v.compareTo(version) != 0;
			return false;
		}

		public static boolean matches(List<VersionDefinition> versionDefintions, Version version) {
			for (VersionDefinition vd: versionDefintions) {
				if (!vd.matches(version)) return false;
			}
			return true;
		}

		public Version getVersion() {
			return version;
		}

		public int getOp() {
			return op;
		}

		public String getVersionAsString() {
			return version == null ? null : version.toString();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("version ");
			sb.append(getOpAsString()).append(' ').append(version);

			return sb.toString();
		}

		public String getOpAsString() {
			switch (getOp()) {
			case EQ:
				return "EQ";
			case LTE:
				return "LTE";
			case GTE:
				return "GTE";
			case NEQ:
				return "NEQ";
			case LT:
				return "LT";
			case GT:
				return "GT";
			}
			return null;
		}
	}

	public static class PackageQuery {

		public final static int RESOLUTION_NONE = 0;
		public final static int RESOLUTION_DYNAMIC = 1;
		public final static int RESOLUTION_OPTIONAL = 2;

		private final String name;
		private List<VersionDefinition> versions = new ArrayList<OSGiUtil.VersionDefinition>();
		private int resolution = RESOLUTION_NONE;

		public PackageQuery(String name) {
			this.name = name;
		}

		public boolean matches(Version version) {
			for (VersionDefinition vd: versions) {
				if (!vd.matches(version)) return false;
			}
			return true;
		}

		public boolean isRequired() {
			return resolution == RESOLUTION_NONE;
		}

		public void setResolution(int resolution) {
			this.resolution = resolution;
		}

		public int getResolution() {
			return resolution;
		}

		public void addVersion(int op, String version, boolean not) throws BundleException {
			versions.add(new VersionDefinition(OSGiUtil.toVersion(version), op, not));
		}

		public String getName() {
			return name;
		}

		public List<VersionDefinition> getVersionDefinitons() {
			return versions;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("name:").append(name).append(';');
			sb.append("resolution:").append(toResolution(resolution, "")).append(';');
			Iterator<VersionDefinition> it = versions.iterator();
			sb.append("versions:");
			while (it.hasNext()) {
				sb.append(',').append(it.next());
			}

			return sb.toString();
		}

		public static int toResolution(String value, int defaultValue) {
			if (!StringUtil.isEmpty(value, true)) {
				value = value.trim().toLowerCase();
				if ("dynamic".equals(value)) return RESOLUTION_DYNAMIC;
				if ("optional".equals(value)) return RESOLUTION_OPTIONAL;
			}
			return defaultValue;
		}

		public static String toResolution(int value, String defaultValue) {
			if (RESOLUTION_DYNAMIC == value) return "dynamic";
			if (RESOLUTION_NONE == value) return "none";
			if (RESOLUTION_OPTIONAL == value) return "optional";
			return defaultValue;
		}
	}

	public static class PackageDefinition {
		private final String name;
		private Version version;

		public PackageDefinition(String name) {
			this.name = name;
		}

		public void setVersion(String version) throws BundleException {
			this.version = OSGiUtil.toVersion(version);
		}

		public void setVersion(Version version) {
			this.version = version;
		}

		public String getName() {
			return name;
		}

		public Version getVersion() {
			return version;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("name:").append(name).append(';').append("version:").append(version);
			return sb.toString();
		}
	}

	public static class BundleDefinition implements Serializable {

		private static final long serialVersionUID = -144133130941294618L;
		private final String name;
		private Bundle bundle;
		private VersionDefinition versionDef;

		public BundleDefinition(String name) {
			this.name = name;
		}

		public BundleDefinition(String name, String version) throws BundleException {
			this.name = name;
			if (name == null) throw new IllegalArgumentException("Name cannot be null");
			if (!StringUtil.isEmpty(version, true)) setVersion(VersionDefinition.EQ, version);
		}

		public BundleDefinition(String name, Version version) {
			this.name = name;
			if (name == null) throw new IllegalArgumentException("Name cannot be null");
			if (version != null) setVersion(VersionDefinition.EQ, version);
		}

		public BundleDefinition(Bundle bundle) {
			this.name = bundle.getSymbolicName();
			if (name == null) throw new IllegalArgumentException("Name cannot be null");

			if (bundle.getVersion() != null) setVersion(VersionDefinition.EQ, bundle.getVersion());
			this.bundle = bundle;
		}

		public String getName() {
			return name;
		}

		/**
		 * only return a bundle if already loaded, does not load the bundle
		 *
		 * @return
		 */
		public Bundle getLoadedBundle() {
			return bundle;
		}

		public Bundle getBundle(Identification id, List<Resource> addional, boolean startIfNecessary, boolean versionOnlyMattersForDownload) throws BundleException {
			if (bundle == null) {
				bundle = OSGiUtil.loadBundle(null, name, getVersion(), id, addional, startIfNecessary, versionOnlyMattersForDownload, true, null);
			}
			return bundle;
		}

		/**
		 * get Bundle, also load if necessary from local or remote
		 *
		 * @return
		 * @throws BundleException
		 * @throws StartFailedException
		 */
		public Bundle getBundle(Config config, List<Resource> addional) throws BundleException {
			return getBundle(config, addional, false);
		}

		public Bundle getBundle(Config config, List<Resource> addional, boolean versionOnlyMattersForDownload) throws BundleException {
			if (bundle == null) {
				config = ThreadLocalPageContext.getConfig(config);
				bundle = OSGiUtil.loadBundle(CFMLEngineFactory.getInstance().getBundleContext(), name, getVersion(), config == null ? null : config.getIdentification(), addional,
						false, versionOnlyMattersForDownload, true, null);
			}
			return bundle;
		}

		public Bundle getLocalBundle(List<Resource> addional) {
			if (bundle == null) {
				bundle = OSGiUtil.loadBundleFromLocal(name, getVersion(), addional, true, null);
			}
			return bundle;
		}

		public BundleFile getBundleFile(boolean downloadIfNecessary, List<Resource> addional) throws BundleException {
			Config config = ThreadLocalPageContext.getConfig();
			return OSGiUtil.getBundleFile(name, getVersion(), config == null ? null : config.getIdentification(), addional, downloadIfNecessary);

		}

		public int getOp() {
			return versionDef == null ? VersionDefinition.EQ : versionDef.getOp();
		}

		public Version getVersion() {
			return versionDef == null ? null : versionDef.getVersion();
		}

		public VersionDefinition getVersionDefiniton() {
			return versionDef;
		}

		public String getVersionAsString() {
			return versionDef == null ? null : versionDef.getVersionAsString();
		}

		public void setVersion(int op, String version) throws BundleException {
			setVersion(op, OSGiUtil.toVersion(version));
		}

		public void setVersion(int op, Version version) {
			this.versionDef = new VersionDefinition(version, op, false);
		}

		@Override
		public String toString() {
			return "name:" + name + ";version:" + versionDef + ";";
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof BundleDefinition)) return false;

			return toString().equals(obj.toString());

		}

	}

	private static void log(int level, String msg) {
		try {
			Log log = ThreadLocalPageContext.getLog("application");
			if (log != null) log.log(level, "OSGi", msg);
		}
		catch (Exception t) {
			LogUtil.log(level, BundleBuilderFactory.class.getName(), msg);
		}
	}

	private static void log(Throwable t) {
		try {
			Log log = ThreadLocalPageContext.getLog("application");
			if (log != null) log.log(Log.LEVEL_ERROR, "OSGi", t);
		}
		catch (Exception _t) {
			/* this can fail when called from an old loader */
			LogUtil.log(OSGiUtil.class.getName(), _t);
		}
	}

	public static String toState(int state, String defaultValue) {
		switch (state) {
		case Bundle.ACTIVE:
			return "active";
		case Bundle.INSTALLED:
			return "installed";
		case Bundle.UNINSTALLED:
			return "uninstalled";
		case Bundle.RESOLVED:
			return "resolved";
		case Bundle.STARTING:
			return "starting";
		case Bundle.STOPPING:
			return "stopping";
		}
		return defaultValue;

	}

	/**
	 * value can be a String (for a single entry) or a List<String> for multiple entries
	 *
	 * @param b
	 * @return
	 */
	public static Map<String, Object> getHeaders(Bundle b) {
		Dictionary<String, String> headers = b.getHeaders();
		Enumeration<String> keys = headers.keys();
		Enumeration<String> values = headers.elements();

		String key, value;
		Object existing;
		List<String> list;
		Map<String, Object> _headers = new LinkedHashMap<String, Object>();
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			value = StringUtil.unwrap(values.nextElement());
			existing = _headers.get(key);
			if (existing != null) {
				if (existing instanceof String) {
					list = new ArrayList<>();
					list.add((String) existing);
					_headers.put(key, list);
				}
				else list = (List<String>) existing;
				list.add(value);
			}
			else _headers.put(key, value);
		}

		return _headers;
	}

	public static String[] getBootdelegation() {
		if (bootDelegation == null) {
			InputStream is = null;
			try {
				Properties prop = new Properties();
				is = OSGiUtil.class.getClassLoader().getResourceAsStream("default.properties");
				prop.load(is);
				String bd = prop.getProperty("org.osgi.framework.bootdelegation");
				if (!StringUtil.isEmpty(bd)) {
					bd += ",java.lang,java.lang.*";
					bootDelegation = ListUtil.trimItems(ListUtil.listToStringArray(StringUtil.unwrap(bd), ','));
				}
			}
			catch (IOException ioe) {
			}
			finally {
				IOUtil.closeEL(is);
			}
		}
		if (bootDelegation == null) return new String[0];
		return bootDelegation;
	}

	public static boolean isClassInBootelegation(String className) {
		return isInBootelegation(className, false);
	}

	public static boolean isPackageInBootelegation(String className) {
		return isInBootelegation(className, true);
	}

	private static boolean isInBootelegation(String name, boolean isPackage) {
		// extract package
		String pack;
		if (isPackage) pack = name;
		else {
			int index = name.lastIndexOf('.');
			if (index == -1) return false;
			pack = name.substring(0, index);
		}

		String[] arr = OSGiUtil.getBootdelegation();
		for (String bd: arr) {
			// with wildcard
			if (bd.endsWith(".*")) {
				bd = bd.substring(0, bd.length() - 1);
				if (pack.startsWith(bd)) return true;
			}
			// no wildcard
			else {
				if (bd.equals(pack)) return true;
			}
		}
		return false;
	}

	public static BundleDefinition[] toBundleDefinitions(BundleInfo[] bundles) {
		if (bundles == null) return new BundleDefinition[0];

		BundleDefinition[] rtn = new BundleDefinition[bundles.length];
		for (int i = 0; i < bundles.length; i++) {
			rtn[i] = bundles[i].toBundleDefinition();
		}
		return rtn;
	}

	public static Bundle getFrameworkBundle(Config config, Bundle defaultValue) {
		Bundle[] bundles = ConfigWebUtil.getEngine(config).getBundleContext().getBundles();
		Bundle b = null;
		for (int i = 0; i < bundles.length; i++) {
			b = bundles[i];
			if (b != null && isFrameworkBundle(b)) return b;
		}
		return defaultValue;
	}

	public static boolean isFrameworkBundle(Bundle b) {// FELIX specific

		return "org.apache.felix.framework".equalsIgnoreCase(b.getSymbolicName()); // TODO move to cire util class tha does not exist yet
	}

	public static Bundle getBundleFromClass(Class clazz, Bundle defaultValue) {
		ClassLoader cl = clazz.getClassLoader();
		if (cl instanceof BundleClassLoader) {
			return ((BundleClassLoader) cl).getBundle();
		}
		return defaultValue;
	}

	public static String getClassPath() {
		List<File> list = getClassPathAsList();
		StringBuilder sb = new StringBuilder();
		for (File f: list) {
			if (sb.length() > 0) sb.append(File.pathSeparator);
			sb.append(f.getAbsolutePath());
		}
		return sb.toString();
	}

	public static List<File> getClassPathAsList() {
		ClassLoader cl = OSGiUtil.class.getClassLoader();
		BundleClassLoader bcl = cl instanceof BundleClassLoader ? (BundleClassLoader) cl : null;
		BundleContext bc = null;
		if (bcl != null) {
			Bundle bundle = bcl.getBundle();
			bc = bundle.getBundleContext();
		}

		Set<String> set = new HashSet<>();
		set.add(ClassUtil.getSourcePathForClass(CFMLEngineFactory.class, null));
		set.add(ClassUtil.getSourcePathForClass(javax.servlet.jsp.JspException.class, null));
		set.add(ClassUtil.getSourcePathForClass(javax.servlet.Servlet.class, null));

		List<File> list = new ArrayList<>();
		for (String path: set) {
			list.add(new File(path));
		}

		// core
		/// list.add(new File(bc.getBundle().getLocation()));

		// all other bundles
		if (bc != null) {
			for (Bundle b: bc.getBundles()) {
				if ("System Bundle".equalsIgnoreCase(b.getLocation())) continue;
				list.add(new File(b.getLocation()));
			}
		}
		return list;
	}

	public static List<File> getClassPathAsListWithJarExtension() throws IOException {
		List<File> list = getClassPathAsList();
		int len = list.size();
		File f;
		Resource trg, tmpDir = SystemUtil.getTempDirectory().getRealResource("jars");
		if (!tmpDir.isDirectory()) tmpDir.createDirectory(true);
		for (int i = 0; i < len; i++) {
			f = list.get(i);
			if (!"jar".equalsIgnoreCase(ResourceUtil.getExtension(f.getName(), "jar"))) {
				trg = tmpDir.getRealResource(HashUtil.create64BitHashAsString(f.getAbsolutePath(), Character.MAX_RADIX) + ".jar");
				if (!trg.isFile()) {
					IOUtil.copy(new FileInputStream(f), trg, true);
				}
				list.set(i, new File(trg.getAbsolutePath()));
			}
		}
		return list;
	}

	public static void stop(Class clazz) throws BundleException {
		if (clazz == null) return;
		Bundle bundleCore = OSGiUtil.getBundleFromClass(CFMLEngineImpl.class, null);
		Bundle bundleFromClass = OSGiUtil.getBundleFromClass(clazz, null);
		if (bundleFromClass != null && !bundleFromClass.equals(bundleCore)) {
			OSGiUtil.stopIfNecessary(bundleFromClass);
		}
		// TODO Auto-generated method stub

	}

	public static boolean isValid(Object obj) {
		if (obj != null) {
			ClassLoader cl = obj.getClass().getClassLoader();
			if (cl instanceof BundleClassLoader) {
				if (((Bundle) ((BundleClassLoader) cl).getBundle()).getState() != Bundle.ACTIVE) return false;
			}
		}
		return true;
	}

	private static List<PackageDefinition> getExportPackages(Bundle b) {
		Dictionary<String, String> headers = b.getHeaders();
		String raw = headers.get("Export-Package");
		List<PackageDefinition> records = new ArrayList<>();
		int len = raw.length();
		char c;
		boolean inline = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			c = raw.charAt(i);
			if (c == '"') {
				sb.append('"');
				inline = !inline;
			}
			else if (!inline && c == ',') {
				records.add(_toPackageQuery(sb.toString()));
				sb = new StringBuilder();
			}
			else sb.append(c);
		}
		records.add(_toPackageQuery(sb.toString()));

		return records;
	}

	private static PackageDefinition _toPackageQuery(String raw) {
		String[] arr = ListUtil.listToStringArray(raw, ';');
		PackageDefinition pd = new PackageDefinition(arr[0].trim());

		for (int i = 1; i < arr.length; i++) {
			if (arr[i].startsWith("version=")) {
				Version v = OSGiUtil.toVersion(StringUtil.unwrap(arr[i].substring(8)), null);
				if (v != null) pd.setVersion(v);
				break;
			}
		}

		return pd;
	}

	public static boolean resolveBundleLoadingIssues(BundleContext bc, Config config, BundleException be) {
		try {
			loadBundlesAndPackagesFromMessage(bc, config, be.getMessage());
			return true;
		}
		catch (Exception e) {
			LogUtil.log(config, "OSGi", e);
		}
		return false;
	}

	public static boolean resolveBundleLoadingIssues(BundleContext bc, Config config, ClassNotFoundException cnfe) {
		Throwable cause = cnfe.getCause();
		if (!(cause instanceof BundleException)) return false;
		BundleException be = (BundleException) cause;

		return resolveBundleLoadingIssues(bc, config, be);
	}

	// (bundle-version>=30.1.0)
	// (!(bundle-version>=31.0.0))
	private static VersionDefinition toVersionDefinition(String value, boolean bundle) throws BundleException {
		String strBV = bundle ? "(bundle-version" : "(version";
		int last = 0;
		int index, op, start, end;
		boolean isNegated;
		if ((index = value.indexOf(strBV, last)) != -1) {
			last = index + strBV.length();

			isNegated = index > 0 && value.charAt(index - 1) == '!';
			end = value.indexOf(')', index);

			start = value.indexOf("<=", index);
			op = -1;

			// Version Defintion
			if (start != -1 && start < end) {
				op = VersionDefinition.LTE;
				start += 2;
			}
			else {
				start = value.indexOf(">=", index);
				if (start != -1 && start < end) {
					op = VersionDefinition.GTE;
					start += 2;
				}
				else {
					start = value.indexOf("=", index);
					if (start != -1 && start < end) {
						op = VersionDefinition.EQ;
						start++;
					}
				}
			}
			// if (isNegated) op = VersionDefinition.negate(op, -1);

			if (op != -1 && start != -1 && end != -1 && end > start) return new VersionDefinition(toVersion(value.substring(start, end).trim()), op, isNegated);

		}

		return null;

	}

	// a string like:
	// &(osgi.wiring.bundle=com.googlecode.owasp-java-html-sanitizer)(bundle-version>=20211018.2.0))
	private static BundleRange toBundleRange(String raw) throws BundleException, IOException {
		// remove the wrap
		if (raw.startsWith("(")) {
			raw = raw.substring(1);
			raw = raw.substring(0, raw.length() - 1);
		}

		// extract bundle name
		// &(osgi.wiring.bundle=com.googlecode.owasp-java-html-sanitizer)
		if (!raw.startsWith("&(osgi.wiring.bundle="))
			throw new IOException("string does not look as expected [" + raw + "], expecting it starts like this [&(osgi.wiring.bundle=com.googlecode.owasp-java-html-sanitizer)]");

		int start = 21;
		int end = findEnd(raw, start);
		String bundleName = raw.substring(start, end);

		// extract versions
		VersionDefinition vd;
		List<VersionDefinition> versions = new ArrayList<OSGiUtil.VersionDefinition>();
		while ((start = raw.indexOf('(', end)) != -1) {
			end = findEnd(raw, start + 1);
			vd = toVersionDefinition(raw.substring(start, end + 1), true);
			if (vd != null) versions.add(vd);

		}
		VersionRange vr = null;
		if (versions.size() == 1) {
			VersionDefinition from = versions.get(0);
			vr = new lucee.runtime.osgi.BundleRange.VersionRange(from.version, from.op, null, 0);
		}
		else if (versions.size() == 2) {
			VersionDefinition from = versions.get(0);
			VersionDefinition to = versions.get(1);
			vr = new lucee.runtime.osgi.BundleRange.VersionRange(from.version, from.op, to.version, to.op);
		}

		return new BundleRange(bundleName, vr);
	}

	private static void loadBundlesAndPackagesFromMessage(BundleContext bc, Config config, final String msg) throws BundleException, IOException {
		if (bc == null) bc = CFMLEngineFactory.getInstance().getBundleContext();

		int start = 0, end;
		int index;

		// loads the bundles defined in the exception message
		BundleRange br = null;
		while ((index = msg.indexOf("osgi.wiring.bundle;", start)) != -1) {

			start = index + 19;
			index = msg.indexOf('(', index + 19);
			if (index == -1) throw new IOException("no start point found");
			start = index + 1;
			end = findEnd(msg, start);
			if (end == -1) throw new IOException("no end point found");

			br = toBundleRange(msg.substring(start - 1, end + 1));
			if (br != null) loadBundle(bc, br, config.getIdentification(), null, true, false, true, null);
		}

		// load the bundles based on the packages defined in the exception message
		start = 0;
		PackageQuery pq = null;
		while ((index = msg.indexOf("osgi.wiring.package;", start)) != -1) {

			start = index + 19;
			index = msg.indexOf('(', index + 19);
			if (index == -1) throw new IOException("no start point found");
			start = index + 1;
			end = findEnd(msg, start);
			if (end == -1) throw new IOException("no end point found");
			pq = toPackageQuery(msg.substring(start - 1, end + 1));
			if (pq != null) loadBundleByPackage(bc, pq, new HashSet<Bundle>(), true, new HashSet<String>());
		}

	}

	private static int findEnd(String msg, int start) {
		int len = msg.length();
		char c;
		int deep = 0;
		for (int i = start; i < len; i++) {
			c = msg.charAt(i);
			if (c == '(') deep++;
			if (c == ')') {
				if (deep == 0) return i;
				deep--;

			}
		}
		return -1;
	}
}
