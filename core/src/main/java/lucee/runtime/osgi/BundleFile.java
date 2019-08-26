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

import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.SOFT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.commons.collections4.map.ReferenceMap;
import org.osgi.framework.BundleException;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;

public class BundleFile extends BundleInfo {

	private static final long serialVersionUID = -7094382262249367193L;
	private File file;
	private static Map<String, BundleFile> files = Collections.synchronizedMap(new ReferenceMap<String, BundleFile>(SOFT, SOFT));
	private Map<String, Boolean> classes = Collections.synchronizedMap(new ReferenceMap<String, Boolean>(SOFT, SOFT));

	public static BundleFile getInstance(Resource file, boolean onlyValidBundles) throws IOException, BundleException {
		BundleFile bi = getInstance(toFileResource(file));
		if (onlyValidBundles && !bi.isBundle()) return null;
		return bi;
	}

	public static BundleFile getInstance(Resource file) throws IOException, BundleException {
		return getInstance(toFileResource(file));
	}

	public static BundleFile getInstance(File file) throws IOException, BundleException {
		BundleFile bi = files.get(file.getAbsolutePath());
		if (bi == null) {
			bi = new BundleFile(file);
			files.put(file.getAbsolutePath(), bi);
		}
		return bi;
	}

	private BundleFile(File file) throws IOException, BundleException {
		super(file);
		this.file = file;
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	public File getFile() {
		return file;
	}

	public boolean hasClass(String className) throws IOException {
		className = className.replace('.', '/') + ".class";
		Boolean b = classes.get(className);
		if (b != null) return b.booleanValue();
		JarFile jar = new JarFile(file);
		try {
			b = jar.getEntry(className) != null;
			classes.put(className, b);
			return b.booleanValue();
		}
		finally {
			IOUtil.closeEL(jar);
		}
	}

	/**
	 * only return an instance if the Resource is a valid bundle, otherwise it returns null
	 * 
	 * @param res
	 * @return
	 * 
	 * 		public static BundleFile newInstance(Resource res) {
	 * 
	 *         try { BundleFile bf = new BundleFile(res); if (bf.isBundle()) return bf; } catch
	 *         (Throwable t) { ExceptionUtil.rethrowIfNecessary(t); }
	 * 
	 *         return null; }
	 */
}