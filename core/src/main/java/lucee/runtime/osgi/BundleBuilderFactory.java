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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

public class BundleBuilderFactory {

	// Indicates the OSGi specification to use for reading this bundle.
	public static final int MANIFEST_VERSION = 2;

	private static final Set<String> INDIVIDUAL_FILTER = new HashSet<String>();
	private static final Set<String> MAIN_FILTER = new HashSet<String>();
	static {
		MAIN_FILTER.add("SHA1-Digest-Manifest");
		MAIN_FILTER.add("MD5-Digest-Manifest");
		// MAIN_FILTER.add("Sealed");

		INDIVIDUAL_FILTER.add("SHA1-Digest");
		INDIVIDUAL_FILTER.add("MD5-Digest");
		// INDIVIDUAL_FILTER.add("Sealed");
	}

	private String name;
	private final String symbolicName;
	private String description;
	private Manifest manifest;
	private Set<String> existingPackages = new HashSet<String>();
	private boolean ignoreExistingManifest = false;

	private String activator;

	// private List<Resource> jars=new ArrayList<Resource>();

	private List<String> exportPackage;
	private List<String> fragmentHost;
	private List<String> importPackage;
	private List<String> requireBundle;
	private List<String> requireBundleFragment;
	private List<String> dynImportPackage;
	private List<String> classPath;

	// private BundleFile bf;

	private Resource jar;

	private Version version;

	private String bundleActivationPolicy;

	/**
	 * 
	 * @param symbolicName this entry specifies a unique identifier for a bundle, based on the reverse
	 *            domain name convention (used also by the java packages).
	 * @param name Defines a human-readable name for this bundle, Simply assigns a short name to the
	 *            bundle.
	 * @param description A description of the bundle's functionality.
	 * @param version Designates a version number to the bundle.
	 * @param activator Indicates the class name to be invoked once a bundle is activated.
	 * @param name
	 * @throws IOException
	 * @throws BundleException
	 * @throws BundleBuilderFactoryException
	 */
	public BundleBuilderFactory(Resource jar, String symbolicName) throws IOException, BundleException {
		if (!jar.isFile()) throw new IOException("[" + jar + "] is not a file");
		this.jar = jar;
		// bf = new BundleFile(jar);

		if (StringUtil.isEmpty(symbolicName)) {
			// if(StringUtil.isEmpty(name))
			throw new BundleException("symbolic name is reqired");

		}
		this.symbolicName = toSymbolicName(symbolicName);
	}

	public BundleBuilderFactory(Resource jar) throws ApplicationException {
		if (!jar.isFile()) throw new ApplicationException("[" + jar + "] is not a file");
		this.jar = jar;

		this.symbolicName = createSymbolicName(jar);
	}

	public static String createSymbolicName(Resource jar) {
		String name = jar.getName();
		int index = name.lastIndexOf('.');
		if (index != -1) {
			name = name.substring(0, index);
		}
		return toSymbolicName(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIgnoreExistingManifest(boolean ignoreExistingManifest) {
		this.ignoreExistingManifest = ignoreExistingManifest;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(String version) throws BundleException {
		if (StringUtil.isEmpty(version, true)) return;
		this.version = OSGiUtil.toVersion(version);
	}

	public void setVersion(Version version) {
		if (version == null) return;
		this.version = version;
	}

	private static String toSymbolicName(String name) {
		name = name.replace(' ', '.');
		name = name.replace('_', '.');
		name = name.replace('-', '.');
		return name;
	}

	public List<String> getExportPackage() {
		return exportPackage;
	}

	public void addExportPackage(String strExportPackage) {
		if (StringUtil.isEmpty(strExportPackage)) return;
		if (exportPackage == null) exportPackage = new ArrayList<String>();
		addPackages(exportPackage, strExportPackage);

	}

	public List<String> getRequireBundle() {
		return requireBundle;
	}

	public void addRequireBundle(String strRequireBundle) {
		if (StringUtil.isEmpty(strRequireBundle)) return;
		if (requireBundle == null) requireBundle = new ArrayList<String>();
		addPackages(requireBundle, strRequireBundle);

	}

	public List<String> getRequireBundleFragment() {
		return requireBundleFragment;
	}

	public void addRequireBundleFragment(String strRequireBundleFragment) {
		if (StringUtil.isEmpty(strRequireBundleFragment)) return;
		if (requireBundleFragment == null) requireBundleFragment = new ArrayList<String>();
		addPackages(requireBundleFragment, strRequireBundleFragment);

	}

	public List<String> getFragmentHost() {
		return fragmentHost;
	}

	public void addFragmentHost(String strExportPackage) {
		if (StringUtil.isEmpty(strExportPackage)) return;
		if (fragmentHost == null) fragmentHost = new ArrayList<String>();
		addPackages(fragmentHost, strExportPackage);

	}

	public void setBundleActivationPolicy(String bundleActivationPolicy) {
		this.bundleActivationPolicy = bundleActivationPolicy;
	}

	private static void addPackages(Collection<String> packages, String str) {
		StringTokenizer st = new StringTokenizer(str, ",");
		while (st.hasMoreTokens()) {
			packages.add(st.nextToken().trim());
		}
	}

	public List<String> getImportPackage() {
		return importPackage;
	}

	public List<String> getDynamicImportPackage() {
		return dynImportPackage;
	}

	public void addImportPackage(String strImportPackage) {
		if (StringUtil.isEmpty(strImportPackage)) return;
		if (importPackage == null) importPackage = new ArrayList<String>();
		addPackages(importPackage, strImportPackage);
	}

	public void addDynamicImportPackage(String strDynImportPackage) {
		if (StringUtil.isEmpty(strDynImportPackage)) return;
		if (dynImportPackage == null) dynImportPackage = new ArrayList<String>();
		addPackages(dynImportPackage, strDynImportPackage);
	}

	public List<String> getClassPath() {
		return classPath;
	}

	public void addClassPath(String str) {
		if (classPath == null) classPath = new ArrayList<String>();
		addPackages(classPath, str);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getActivator() {
		return activator;
	}

	public void setActivator(String activator) {
		this.activator = activator;
	}

	private void extendManifest(Manifest mf) {
		Attributes attrs = mf.getMainAttributes();
		attrs.putValue("Bundle-ManifestVersion", "" + MANIFEST_VERSION);
		if (!StringUtil.isEmpty(name)) attrs.putValue("Bundle-Name", name);
		attrs.putValue("Bundle-SymbolicName", symbolicName);
		if (!StringUtil.isEmpty(description)) attrs.putValue("Bundle-Description", description);
		if (!StringUtil.isEmpty(bundleActivationPolicy)) attrs.putValue("Bundle-ActivationPolicy", bundleActivationPolicy);
		if (version != null) attrs.putValue("Bundle-Version", version.toString());

		if (!StringUtil.isEmpty(activator)) {
			if (!activator.equalsIgnoreCase("none")) {
				attrs.putValue("Bundle-Activator", activator);
				addImportPackage("org.osgi.framework");
			}
			else {
				// attrs.remove("Bundle-Activator");
				attrs.putValue("Bundle-Activator", "");
			}
		}

		// Export-Package
		String str = ignoreExistingManifest ? null : attrs.getValue("Export-Package");
		// no existing Export-Package
		Set<String> set;
		if (Util.isEmpty(str, true)) {
			set = existingPackages;
		}
		else {
			set = new HashSet<String>();
			addPackages(set, str);
		}

		if (!ArrayUtil.isEmpty(exportPackage) && !isAsterix(exportPackage)) {
			Iterator<String> it = exportPackage.iterator();
			while (it.hasNext()) {
				set.add(it.next());
			}
		}
		exportPackage = ListUtil.toList(set);

		addList(attrs, "Export-Package", exportPackage);

		// Require-Bundle
		str = attrs.getValue("Require-Bundle");
		if (Util.isEmpty(str, true)) addList(attrs, "Require-Bundle", requireBundle);

		// Require-Bundle
		str = attrs.getValue("Require-Bundle-Fragment");
		if (Util.isEmpty(str, true)) addList(attrs, "Require-Bundle-Fragment", requireBundleFragment);

		// str = attrs.getValue("Fragment-Host");
		// if(Util.isEmpty(str,true))
		attrs.remove("Fragment-Host");
		addList(attrs, "Fragment-Host", fragmentHost);

		str = attrs.getValue("Import-Package");
		if (Util.isEmpty(str, true)) addList(attrs, "Import-Package", importPackage);

		str = attrs.getValue("DynamicImport-Package");
		if (Util.isEmpty(str, true)) addList(attrs, "DynamicImport-Package", dynImportPackage);

		str = attrs.getValue("Bundle-ClassPath");
		if (Util.isEmpty(str, true)) addList(attrs, "Bundle-ClassPath", classPath);
	}

	/*
	 * private static List<String> createExportPackageFromResource(Resource jar) { // get all
	 * directories List<Resource> dirs = ResourceUtil.listRecursive(jar,DirectoryResourceFilter.FILTER);
	 * List<String> rtn=new ArrayList<String>(); // remove directories with no files (of any kind)
	 * Iterator<Resource> it = dirs.iterator(); Resource[] children; int count; while(it.hasNext()) {
	 * Resource r = it.next(); children = r.listResources(); count=0; if(children!=null)for(int
	 * i=0;i<children.length;i++){ if(children[i].isFile())count++; } // has files if(count>0) {
	 * 
	 * } }
	 * 
	 * return null; }
	 */

	private boolean isAsterix(List<String> list) {
		if (list == null) return false;
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			if ("*".equals(it.next())) return true;
		}
		return false;
	}

	private void addList(Attributes attrs, String name, List<String> values) {
		if (values == null || values.isEmpty()) return;

		StringBuilder sb = new StringBuilder();
		Iterator<String> it = values.iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (!first) {
				sb.append(',');
			}
			sb.append(it.next());
			first = false;
		}
		attrs.putValue(name, sb.toString());
	}

	public void build() throws IOException {
		Resource res = SystemUtil.getTempFile(".jar", false);
		try {
			build(res);
			IOUtil.copy(res, jar);
		}
		finally {
			res.delete();
		}
	}

	public void build(Resource target) throws IOException {
		OutputStream os = target.getOutputStream();
		try {
			build(os);
		}
		finally {
			IOUtil.close(os);
		}
	}

	public void build(OutputStream os) throws IOException {
		ZipOutputStream zos = new MyZipOutputStream(os, CharsetUtil.UTF8);
		try {

			// jar
			handleEntry(zos, jar, new JarEntryListener(zos));

			// Manifest (do a blank one when method above has not loaded one)
			if (manifest == null) manifest = new Manifest();
			extendManifest(manifest);

			String mf = ManifestUtil.toString(manifest, 128, MAIN_FILTER, INDIVIDUAL_FILTER);
			InputStream is = new ByteArrayInputStream(mf.getBytes(CharsetUtil.UTF8));
			ZipEntry ze = new ZipEntry("META-INF/MANIFEST.MF");
			zos.putNextEntry(ze);
			try {
				copy(is, zos);
			}
			finally {
				IOUtil.close(is);
				zos.closeEntry();
			}
		}
		finally {
			IOUtil.close(zos);
		}
	}

	private void handleEntry(ZipOutputStream target, Resource file, EntryListener listener) throws IOException {
		ZipInputStream zis = new ZipInputStream(file.getInputStream());
		try {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				listener.handleEntry(file, zis, entry);
				zis.closeEntry();
			}
		}
		finally {
			IOUtil.close(zis);
		}
	}

	class JarEntryListener implements EntryListener {

		private ZipOutputStream zos;

		public JarEntryListener(ZipOutputStream zos) {
			this.zos = zos;
		}

		@Override
		public void handleEntry(Resource zipFile, ZipInputStream source, ZipEntry entry) throws IOException {

			// log for export-package
			if (!entry.isDirectory()) {
				String name = entry.getName();
				int index = name.lastIndexOf('/');
				if (index != -1 && !name.startsWith("META-INF")) {
					name = name.substring(0, index);
					if (name.length() > 0) existingPackages.add(ListUtil.trim(name.replace('/', '.'), "."));
				}
			}

			// security
			if ("META-INF/IDRSIG.DSA".equalsIgnoreCase(entry.getName()) || "META-INF/IDRSIG.SF".equalsIgnoreCase(entry.getName())
					|| "META-INF/INDEX.LIST".equalsIgnoreCase(entry.getName())) {
				return;
			}

			// manifest
			if ("META-INF/MANIFEST.MF".equalsIgnoreCase(entry.getName())) {
				if (!ignoreExistingManifest) {
					manifest = new Manifest(source);
					Attributes attrs = manifest.getMainAttributes();

					// they are in bootdelegation
					// ManifestUtil.removeFromList(attrs,"Import-Package","javax.*");
					ManifestUtil.removeOptional(attrs, "Import-Package");

					// ManifestUtil.removeFromList(attrs,"Import-Package","org.osgi.*");
				}
				return;
			}

			// ignore the following stuff
			if (entry.getName().endsWith(".DS_Store") || entry.getName().startsWith("__MACOSX")) {
				return;
			}

			MyZipEntry ze = new MyZipEntry(entry.getName());
			ze.setComment(entry.getComment());
			ze.setTime(entry.getTime());
			ze.setFile(zipFile);

			try {
				zos.putNextEntry(ze);
			}
			catch (NameAlreadyExistsException naee) {
				if (entry.isDirectory()) {
					return;
				}
				log("--------------------------------");
				log(ze.getName());
				log("before:" + naee.getFile());
				log("curren:" + zipFile);
				log("size:" + naee.getSize() + "==" + entry.getSize());
				return; // TODO throw naee;
			}
			try {
				copy(source, zos);
			}
			finally {
				zos.closeEntry();
			}
		}
	}

	public interface EntryListener {

		public void handleEntry(Resource zipFile, ZipInputStream source, ZipEntry entry) throws IOException;

	}

	public class MyZipOutputStream extends ZipOutputStream {

		private Map<String, Resource> names = new HashMap<String, Resource>();

		public MyZipOutputStream(OutputStream out, Charset charset) {
			super(out);
		}

		@Override
		public void putNextEntry(ZipEntry e) throws IOException {
			Resource file = names.get(e.getName());
			if (names.containsKey(e.getName())) throw new NameAlreadyExistsException(e.getName(), file, e.getSize());

			if (e instanceof MyZipEntry) names.put(e.getName(), ((MyZipEntry) e).getFile());
			super.putNextEntry(e);
		}

	}

	public class MyZipEntry extends ZipEntry {

		private Resource file;

		public MyZipEntry(String name) {
			super(name);
		}

		public void setFile(Resource file) {
			this.file = file;
		}

		public MyZipEntry(ZipEntry e) {
			super(e);
		}

		public Resource getFile() {
			return file;
		}
	}

	private final static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[0xffff];
		int len;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);
	}

	public void log(String str) {
		LogUtil.log((PageContext) null, Log.LEVEL_INFO, BundleBuilderFactory.class.getName(), str);
	}
}