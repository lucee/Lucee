/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.commons.io.res.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.runtime.exp.PageException;

/**
 * Classloader that load classes from resources
 */
public class ResourceClassLoader extends URLClassLoader implements Closeable {
	private static Map<String, ResourceClassLoader> classloaders = new ConcurrentHashMap<>();

	private final Collection<Resource> resources;

	private final String id;
	private static RC rc = new RC();
	static {
		boolean res = registerAsParallelCapable();
	}

	public static ResourceClassLoader getInstance(Resource[] resources, ClassLoader parent) throws IOException {
		List<Resource> list = new ArrayList<Resource>();
		for (Resource r: resources) {
			if (r != null) list.add(r);
		}
		java.util.Collections.sort(list, rc);
		return getInstance(list, parent);
	}

	public static ResourceClassLoader getInstance(Collection<Resource> resources, ClassLoader parent) throws IOException {
		List<Resource> list = new ArrayList<Resource>();
		for (Resource r: resources) {
			if (r != null) list.add(r);
		}
		java.util.Collections.sort(list, rc);
		return getInstance(list, parent);
	}

	private static ResourceClassLoader getInstance(List<Resource> resourcesSorted, ClassLoader parent) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (Resource r: resourcesSorted) {
			if (r != null) sb.append(r.getAbsolutePath()).append(';');
		}
		String id = HashUtil.create64BitHashAsString(sb);
		ResourceClassLoader rcl = classloaders.get(id);
		if (rcl == null) {
			rcl = new ResourceClassLoader(resourcesSorted, parent, id);
			classloaders.put(id, rcl);
		}
		return rcl;
	}

	private ResourceClassLoader(List<Resource> resources, ClassLoader parent, String id) throws IOException {
		super(doURLs(resources), parent);
		this.resources = resources;
		this.id = id;
	}

	private ResourceClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
		this.resources = new ArrayList<Resource>();
		this.id = "orphan";
	}

	/**
	 * @return the resources
	 */
	public Resource[] getResources() {
		return resources.toArray(new Resource[resources.size()]);
	}

	public boolean isEmpty() {
		return resources.isEmpty();
	}

	public static URL[] doURLs(Collection<Resource> reses) throws IOException {
		List<URL> list = new ArrayList<URL>();
		for (Resource r: reses) {
			if (r.isDirectory() || "jar".equalsIgnoreCase(ResourceUtil.getExtension(r, null))) list.add(doURL(r));
		}
		return list.toArray(new URL[list.size()]);
	}

	/**
	 * translate resources to url Objects
	 * 
	 * @param reses
	 * @return
	 * @throws PageException
	 */
	public static URL[] doURLs(Resource[] reses) throws IOException {
		List<URL> list = new ArrayList<URL>();
		for (int i = 0; i < reses.length; i++) {
			if (reses[i].isDirectory() || "jar".equalsIgnoreCase(ResourceUtil.getExtension(reses[i], null))) list.add(doURL(reses[i]));
		}
		return list.toArray(new URL[list.size()]);

	}

	private static URL doURL(Resource res) throws IOException {
		if (!(res instanceof FileResource)) {
			return ResourceUtil.toFile(res).toURL();
		}
		return ((FileResource) res).toURL();
	}

	@Override
	public void close() {
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public String hash() {
		return id;
	}

	private static class RC implements Comparator<Resource> {

		@Override
		public int compare(Resource l, Resource r) {
			return l.getAbsolutePath().compareTo(r.getAbsolutePath());
		}
	}
}