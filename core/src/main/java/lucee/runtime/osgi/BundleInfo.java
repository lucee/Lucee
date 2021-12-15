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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
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
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class BundleInfo implements Serializable {

	private static final long serialVersionUID = -8723070772449992030L;

	private Version version;
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
	private static Map<String, BundleInfo> bundles = new HashMap<String, BundleInfo>();

	public static BundleInfo getInstance(String id, InputStream is, boolean closeStream) throws IOException, BundleException {
		BundleInfo bi = bundles.get(id);
		if (bi != null) return bi;

		File tmp = File.createTempFile("temp-extension", "lex");

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
			version = StringUtil.isEmpty(tmp, true) ? null : OSGiUtil.toVersion(tmp);
			exportPackage = attrs.getValue("Export-Package");
			importPackage = attrs.getValue("Import-Package");
			dynamicImportPackage = attrs.getValue("DynamicImport-Package");
			activator = attrs.getValue("Bundle-Activator");
			description = attrs.getValue("Bundle-Description");
			classPath = attrs.getValue("Bundle-ClassPath");
			requireBundle = attrs.getValue("Require-Bundle");
			fragementHost = attrs.getValue("Fragment-Host");

			headers = createHeaders(attrs);
		}
		finally {
			IOUtil.closeEL(jar);
		}
	}

	public boolean isBundle() {
		try {
			return getSymbolicName() != null && getVersion() != null;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return false;
		}
	}

	public String getRequireBundle() {
		return requireBundle;
	}

	public Version getVersion() {
		return version;
	}

	public String getVersionAsString() {
		return version == null ? null : version.toString();
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
	 * Value can be a string (for a Single entry or a List<String> for multiple entries)
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
}