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
package lucee.runtime.osgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.osgi.OSGiUtil.PackageDefinition;
import lucee.runtime.osgi.OSGiUtil.PackageQuery;
import lucee.runtime.osgi.OSGiUtil.VersionDefinition;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class BundleInfo implements Serializable {

	private static final long serialVersionUID = 6361333066318375689L;
	private transient Version _version;
	private String strVersion;
	private String name;
	private String symbolicName;
	private String exportPackage;
	private String importPackage;
	private String activator;
	private int manifestVersion;
	private String description;
	private String dynamicImportPackage;
	private String classPath;
	private String requireBundle;
	private String fragementHost;
	private Map<String, Object> headers;
	private boolean valid;

	private Map<String, PackageDefinition> exportPackageAsMap;
	private static Map<String, BundleInfo> bundles = new HashMap<String, BundleInfo>();

	public static BundleInfo getInstance(Config config, String id, InputStream is, boolean closeStream) throws IOException, BundleException {

		// cached ?
		BundleInfo bi = bundles.get(id);
		if (bi != null) {
			return bi;
		}

		// load file from bundles dir
		try {
			File bundleFile = new File(ConfigUtil.getCFMLEngine(config).getCFMLEngineFactory().getBundleDirectory(), id);
			if (bundleFile.isFile()) {
				bundles.put(id, bi = new BundleInfo(bundleFile));
				return bi;
			}
		}
		catch (Exception e) {
		}

		// create a temp file to read data from it (using the stream directly did not work properly)
		File tmp = File.createTempFile("temp-extension-" + id + "-", ".lex");
		try {
			FileOutputStream os = new FileOutputStream(tmp);
			IOUtil.copy(is, os, closeStream, true);
			bundles.put(id, bi = new BundleInfo(tmp));
			return bi;
		}
		finally {
			tmp.delete();
		}
	}

	public BundleInfo(Resource file) throws IOException, BundleException {
		this(toFileResource(file));
	}

	public BundleInfo(File file) throws IOException, BundleException {
		JarFile jar = new JarFile(file);
		try {
			Manifest manifest = jar.getManifest();
			if (manifest == null) return;

			Attributes attrs = manifest.getMainAttributes();
			if (attrs == null) return;

			manifestVersion = Caster.toIntValue(attrs.getValue("Bundle-ManifestVersion"), 1);
			name = attrs.getValue("Bundle-Name");
			symbolicName = attrs.getValue("Bundle-SymbolicName");
			String tmp = attrs.getValue("Bundle-Version");
			if (!StringUtil.isEmpty(tmp, true)) {
				strVersion = tmp.trim();
				_version = OSGiUtil.toVersion(strVersion);
			}
			exportPackage = attrs.getValue("Export-Package");
			importPackage = attrs.getValue("Import-Package");
			dynamicImportPackage = attrs.getValue("DynamicImport-Package");
			activator = attrs.getValue("Bundle-Activator");
			description = attrs.getValue("Bundle-Description");
			classPath = attrs.getValue("Bundle-ClassPath");
			requireBundle = attrs.getValue("Require-Bundle");
			fragementHost = attrs.getValue("Fragment-Host");

			headers = createHeaders(attrs);

			// is valid bundle?
			boolean valid;
			try {
				// no name or version = not valid
				if (getSymbolicName() == null || getVersion() == null) {
					valid = false;
				}
				// has exportPackage or is a fragment = valid
				else if (!StringUtil.isEmpty(exportPackage, true) || !StringUtil.isEmpty(fragementHost, true)) {
					valid = true;
				}
				// LAR archive (Lucee mapping archive) — always valid, not a real OSGi bundle
				else if (!StringUtil.isEmpty(attrs.getValue("mapping-type"), true)) {
					valid = true;
				}
				// has no exportPackage, fine when it has other files than just class files or has no class files
				// at all
				else {
					valid = true;
					if (containsOnlyClassFiles(jar)) {
						IOException ioe = new IOException("Invalid OSGi bundle structure in [" + file.getName() + "]: "
								+ "This bundle contains only Java class files but does not declare any 'Export-Package' entries in its manifest. "
								+ "According to OSGi specifications, bundles that contain classes intended for use by other bundles must explicitly export their packages. "
								+ "Please add appropriate 'Export-Package' declarations to the MANIFEST.MF file.");
						LogUtil.log("bundle", ioe);
						throw ioe;

					}

				}

			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				valid = false;
			}
			this.valid = valid;
		}
		finally {
			IOUtil.closeEL(jar);
		}
	}

	public boolean isBundle() {
		return valid;
	}

	/**
	 * Checks if the JAR contains only class files and no other resources. If true, the JAR likely needs
	 * to export packages but doesn't.
	 * 
	 * @return true if the JAR only contains class files, false otherwise
	 */
	private static boolean containsOnlyClassFiles(JarFile jar) {
		try {

			// Iterate through JAR entries
			java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();

			boolean hasClassFiles = false;

			while (entries.hasMoreElements()) {
				java.util.jar.JarEntry entry = entries.nextElement();
				String name = entry.getName();

				// Skip directories and META-INF
				if (entry.isDirectory() || name.indexOf("META-INF/MANIFEST.MF") != -1) {
					continue;
				}

				if (name.endsWith(".class")) {
					hasClassFiles = true;
				}
				else {
					return false;
				}
			}

			// If we have class files but no other files, return true
			return hasClassFiles;

		}
		catch (Exception e) {
			// Error accessing JAR, log it if needed
			return false; // Assume valid in case of errors
		}
	}

	public String getRequireBundle() {
		return requireBundle;
	}

	public Version getVersion() {
		if (_version == null && !StringUtil.isEmpty(strVersion)) {
			_version = OSGiUtil.toVersion(strVersion, null);
		}
		return _version;
	}

	public String getVersionAsString() {
		return strVersion;
	}

	private String getBundleName() {
		return name;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public String getExportPackage() {
		return exportPackage;
	}

	public Collection<PackageDefinition> getExportPackageAsCollection() {
		if (exportPackageAsMap == null) {
			synchronized (this) {
				if (exportPackageAsMap == null) {
					if (StringUtil.isEmpty(exportPackage, true)) {
						return (exportPackageAsMap = new HashMap<>()).values();
					}

					exportPackageAsMap = new HashMap<>();
					int len = exportPackage.length();
					char c;
					boolean inline = false;
					StringBuilder sb = new StringBuilder();
					PackageDefinition pd;
					for (int i = 0; i < len; i++) {
						c = exportPackage.charAt(i);
						if (c == '"') {
							sb.append('"');
							inline = !inline;
						}
						else if (!inline && c == ',') {
							pd = toPackageDefinition(sb.toString());
							exportPackageAsMap.put(pd.getName(), pd);

							sb = new StringBuilder();
						}
						else sb.append(c);
					}
					pd = toPackageDefinition(sb.toString());
					exportPackageAsMap.put(pd.getName(), pd);
				}
			}
		}
		return exportPackageAsMap.values();
	}

	public boolean hasMatchingExportPackage(PackageQuery pq) {
		getExportPackageAsCollection();
		PackageDefinition pd = exportPackageAsMap.get(pq.getName());
		if (pd != null) {
			if (VersionDefinition.matches(pq.getVersionDefinitons(), pd.getVersion())) return true;
		}

		return false;
	}

	private static PackageDefinition toPackageDefinition(String raw) {
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

	public String getImportPackage() {
		return importPackage;
	}

	public String getActivator() {
		return activator;
	}

	public int getManifestVersion() {
		return manifestVersion;
	}

	public String getDescription() {
		return description;
	}

	public String getDynamicImportPackage() {
		return dynamicImportPackage;
	}

	public String getFragementHost() {
		return fragementHost;
	}

	public String getClassPath() {
		return classPath;
	}

	public Object info() {
		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._Name, getBundleName());

		sct.setEL("Fragment-Host", getFragementHost());
		sct.setEL("Activator", getActivator());
		sct.setEL("ClassPath", getClassPath());
		sct.setEL("Description", getDescription());
		sct.setEL("DynamicImportPackage", getDynamicImportPackage());
		sct.setEL("ExportPackage", getExportPackage());
		sct.setEL("ImportPackage", getImportPackage());
		sct.setEL("SymbolicName", getSymbolicName());
		sct.setEL(KeyConstants._Version, getVersionAsString());
		sct.setEL("ManifestVersion", getManifestVersion());
		sct.setEL("RequireBundle", getRequireBundle());
		return sct;
	}

	/**
	 * Value can be a string (for a Single entry or a List&lt;String&gt; for multiple entries)
	 * 
	 * @return
	 */
	public Map<String, Object> getHeaders() {
		return headers;
	}

	private Map<String, Object> createHeaders(Attributes attrs) {
		Map<String, Object> headers = new HashMap<String, Object>();
		Iterator<Entry<Object, Object>> it = attrs.entrySet().iterator();
		Entry<Object, Object> e;
		String key, value;
		Object existing;
		List<String> list;
		while (it.hasNext()) {
			e = it.next();
			key = e.getKey().toString();
			value = StringUtil.unwrap(e.getValue().toString());
			existing = headers.get(key);
			if (existing != null) {
				if (existing instanceof String) {
					list = new ArrayList<>();
					list.add((String) existing);
					headers.put(key, list);
				}
				else list = (List<String>) existing;
				list.add(value);
			}
			else headers.put(key, value);
		}

		return headers;
	}

	public BundleDefinition toBundleDefinition() {
		return new BundleDefinition(getSymbolicName(), getVersion());
	}

	protected static File toFileResource(Resource file) throws IOException {
		if (file instanceof FileResource) return (File) file;
		throw new IOException("only file resources (local file system) are supported");
	}

	@Override
	public String toString() {
		return "name:" + name + ";version:" + getVersionAsString() + ";symbolicName:" + symbolicName + ";exportPackage:" + exportPackage + ";importPackage:" + importPackage
				+ ";activator:" + activator + ";manifestVersion:" + manifestVersion + ";description:" + description + ";dynamicImportPackage:" + dynamicImportPackage
				+ ";classPath:" + classPath + ";requireBundle:" + requireBundle + ";fragmentHost:" + fragementHost;
	}
}