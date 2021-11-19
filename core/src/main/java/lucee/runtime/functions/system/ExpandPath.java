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
/**
 * Implements the CFML Function expandpath
 */
package lucee.runtime.functions.system;

import java.io.IOException;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl.ResourceProviderFactory;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.type.util.ArrayUtil;

public final class ExpandPath implements Function {

	private static final long serialVersionUID = 6192659914120397912L;

	public static String call(PageContext pc, String relPath) throws PageException {
		ConfigWeb config = pc.getConfig();
		relPath = prettifyPath(pc, relPath);

		String contextPath = pc.getHttpServletRequest().getContextPath();
		if (!StringUtil.isEmpty(contextPath) && relPath.startsWith(contextPath + "/")) {
			boolean sws = StringUtil.startsWith(relPath, '/');
			relPath = relPath.substring(contextPath.length());
			if (sws && !StringUtil.startsWith(relPath, '/')) relPath = "/" + relPath;
		}

		Resource res;

		if (StringUtil.startsWith(relPath, '/')) {

			PageContextImpl pci = (PageContextImpl) pc;
			ConfigWebPro cwi = (ConfigWebPro) config;
			PageSource[] sources = cwi.getPageSources(pci, mergeMappings(pc.getApplicationContext().getMappings(), pc.getApplicationContext().getComponentMappings()), relPath,
					false, pci.useSpecialMappings(), true);

			if (!ArrayUtil.isEmpty(sources)) {
				// first check for existing
				for (int i = 0; i < sources.length; i++) {
					if (sources[i].exists()) {
						return toReturnValue(relPath, sources[i].getResource());
					}
				}

				// no expand needed
				if (!SystemUtil.isWindows() && !sources[0].exists()) {
					res = pc.getConfig().getResource(relPath);
					if (res.exists()) {
						return toReturnValue(relPath, res);
					}
				}
				for (int i = 0; i < sources.length; i++) {
					res = sources[i].getResource();
					if (res != null) {
						return toReturnValue(relPath, res);
					}
				}
			}

			// no expand needed
			else if (!SystemUtil.isWindows()) {
				res = pc.getConfig().getResource(relPath);
				if (res.exists()) {
					return toReturnValue(relPath, res);
				}
			}

			// Resource[] reses =
			// cwi.getPhysicalResources(pc,pc.getApplicationContext().getMappings(),realPath,false,pci.useSpecialMappings(),true);

		}
		relPath = ConfigWebUtil.replacePlaceholder(relPath, config);
		res = pc.getConfig().getResource(relPath);
		if (res.isAbsolute()) return toReturnValue(relPath, res);

		PageSource ps = pc.getBasePageSource();
		res = ps == null ? ResourceUtil.getCanonicalResourceEL(ResourceUtil.toResourceExisting(pc.getConfig(), ReqRspUtil.getRootPath(pc.getServletContext())))
				: ResourceUtil.getResource(pc, ps);

		if (!res.isDirectory()) res = res.getParentResource();
		res = res.getRealResource(relPath);
		return toReturnValue(relPath, res);

	}

	public static Mapping[] mergeMappings(Mapping[] l, Mapping[] r) {
		Mapping[] arr = new Mapping[(l == null ? 0 : l.length) + (r == null ? 0 : r.length)];
		int index = 0;
		if (l != null) {
			for (Mapping m: l) {
				arr[index++] = m;
			}
		}
		if (r != null) {
			for (Mapping m: r) {
				arr[index++] = m;
			}
		}
		return arr;
	}

	private static String toReturnValue(String realPath, Resource res) {
		String path;
		char pathChar = '/';
		try {
			path = res.getCanonicalPath();
			pathChar = ResourceUtil.FILE_SEPERATOR;
		}
		catch (IOException e) {
			path = res.getAbsolutePath();
		}
		boolean pathEndsWithSep = StringUtil.endsWith(path, pathChar);
		boolean realEndsWithSep = StringUtil.endsWith(realPath, '/');

		if (realEndsWithSep) {
			if (!pathEndsWithSep) path = path + pathChar;
		}
		else if (pathEndsWithSep) {
			path = path.substring(0, path.length() - 1);
		}

		return path;
	}

	private static String prettifyPath(PageContext pc, String path) {
		if (path == null) return null;

		// UNC Path
		if (path.startsWith("\\\\") && SystemUtil.isWindows()) {
			path = path.substring(2);
			path = path.replace('\\', '/');
			return "//" + StringUtil.replace(path, "//", "/", false);
		}

		path = path.replace('\\', '/');

		// virtual file system path
		int index = path.indexOf("://");
		if (index != -1) {
			ResourceProviderFactory[] factories = ((ConfigPro) pc.getConfig()).getResourceProviderFactories();
			String scheme = path.substring(0, index).toLowerCase().trim();
			for (int i = 0; i < factories.length; i++) {
				if (scheme.equalsIgnoreCase(factories[i].getScheme())) return scheme + "://" + StringUtil.replace(path.substring(index + 3), "//", "/", false);
			}
		}

		return StringUtil.replace(path, "//", "/", false);
		// TODO /aaa/../bbb/
	}
}