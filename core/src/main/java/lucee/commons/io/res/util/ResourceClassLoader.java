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
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.digest.MD5;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.util.ArrayUtil;

/**
 * Classloader that load classes from resources
 */
public final class ResourceClassLoader extends URLClassLoader implements Closeable {

	private List<Resource> resources = new ArrayList<Resource>();
	private Map<String, SoftReference<ResourceClassLoader>> customCLs;

	/**
	 * Constructor of the class
	 * 
	 * @param reses
	 * @param parent
	 * @throws PageException
	 */
	public ResourceClassLoader(Resource[] resources, ClassLoader parent) throws IOException {
		super(doURLs(resources), parent);
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] != null) this.resources.add(resources[i]);
		}
	}

	public ResourceClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
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
		if (!(res instanceof FileResource)) throw new IOException("resource [" + res.getPath() + "] must be a local file");
		return ((FileResource) res).toURL();
	}

	@Override
	public void close() {
	}

	/*
	 * public synchronized void addResources(Resource[] reses) throws IOException { for(int
	 * i=0;i<reses.length;i++){ if(!this.resources.contains(reses[i])){ this.resources.add(reses[i]);
	 * addURL(doURL(reses[i])); } } }
	 */

	public ResourceClassLoader getCustomResourceClassLoader(Resource[] resources) throws IOException {

		if (ArrayUtil.isEmpty(resources)) return this;

		String key = hash(resources);
		SoftReference<ResourceClassLoader> tmp = customCLs == null ? null : customCLs.get(key);
		ResourceClassLoader rcl = tmp == null ? null : tmp.get();

		if (rcl != null) return rcl;

		resources = ResourceUtil.merge(this.getResources(), resources);
		rcl = new ResourceClassLoader(resources, getParent());

		if (customCLs == null) customCLs = new ConcurrentHashMap<String, SoftReference<ResourceClassLoader>>();

		customCLs.put(key, new SoftReference<ResourceClassLoader>(rcl));
		return rcl;
	}

	public ResourceClassLoader getCustomResourceClassLoader2(Resource[] resources) throws IOException {
		if (ArrayUtil.isEmpty(resources)) return this;
		String key = hash(resources);
		SoftReference<ResourceClassLoader> tmp = customCLs == null ? null : customCLs.get(key);
		ResourceClassLoader rcl = tmp == null ? null : tmp.get();
		if (rcl != null) return rcl;

		rcl = new ResourceClassLoader(resources, this);
		if (customCLs == null) customCLs = new ConcurrentHashMap<String, SoftReference<ResourceClassLoader>>();
		customCLs.put(key, new SoftReference<ResourceClassLoader>(rcl));
		return rcl;
	}

	private String hash(Resource[] resources) {
		Arrays.sort(resources);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < resources.length; i++) {
			sb.append(ResourceUtil.getCanonicalPathEL(resources[i]));
			sb.append(';');
		}
		return MD5.getDigestAsString(sb.toString(), null);
	}

}