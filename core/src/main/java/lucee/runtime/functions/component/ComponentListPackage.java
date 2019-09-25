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
package lucee.runtime.functions.component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.bytecode.util.ASMUtil;

public class ComponentListPackage implements Function {

	private static final long serialVersionUID = 6502632300879457687L;

	private static final ExtensionResourceFilter FILTER_CFC = new ExtensionResourceFilter(Constants.getComponentExtensions());
	private static final ExtensionResourceFilter FILTER_CLASS = new ExtensionResourceFilter(".class");
	private static final String[] EMPTY = new String[0];

	public static Array call(PageContext pc, String packageName) throws PageException {
		Set<String> names;
		try {
			names = _call(pc, packageName);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}

		Array arr = new ArrayImpl();
		String name;
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			name = it.next();
			if (Constants.isComponentExtension(ResourceUtil.getExtension(name, ""))) {
				name = ResourceUtil.removeExtension(name, name);
			}
			arr.appendEL(name);
		}
		return arr;
	}

	private static Set<String> _call(PageContext pc, String packageName) throws IOException, ApplicationException {
		PageContextImpl pci = (PageContextImpl) pc;
		ConfigWebImpl config = (ConfigWebImpl) pc.getConfig();
		Set<String> rtn = null;
		// var SEP=server.separator.file;

		// get environment configuration
		boolean searchLocal = packageName.indexOf('.') == -1 ? true : config.getComponentLocalSearch();
		boolean searchRoot = config.getComponentRootSearch();

		String path = StringUtil.replace(packageName, ".", File.separator, false);

		// search local
		if (searchLocal) {
			PageSource ps = pci.getRelativePageSourceExisting(path);
			if (ps != null) {
				Mapping mapping = ps.getMapping();
				String _path = ps.getRealpath();
				_path = ListUtil.trim(_path, "\\/");
				String[] list = _listMapping(pc, mapping, _path);
				if (!ArrayUtil.isEmpty(list)) rtn = add(rtn, list);
			}
		}

		// check mappings (this includes the webroot)
		if (searchRoot) {
			String virtual = "/" + StringUtil.replace(packageName, ".", "/", false);
			Mapping[] mappings = config.getMappings();
			Mapping mapping;
			String _path;
			String[] list;
			for (int i = 0; i < mappings.length; i++) {
				mapping = mappings[i];
				if (StringUtil.startsWithIgnoreCase(virtual, mapping.getVirtual())) {
					_path = ListUtil.trim(virtual.substring(mapping.getVirtual().length()), "\\/").trim();
					_path = StringUtil.replace(_path, "/", File.separator, false);
					list = _listMapping(pc, mapping, _path);
					if (!ArrayUtil.isEmpty(list)) rtn = add(rtn, list);
				}
			}
		}

		// check component mappings
		Mapping[] mappings = config.getComponentMappings();
		Mapping mapping;
		String[] list;
		if (mappings != null) {
			for (int i = 0; i < mappings.length; i++) {
				mapping = mappings[i];
				list = _listMapping(pc, mapping, path);
				if (!ArrayUtil.isEmpty(list)) rtn = add(rtn, list);
			}
		}

		// check application component mappings
		ApplicationContext ac = pc.getApplicationContext();
		if (ac != null) {
			mappings = ac.getComponentMappings();
			if (mappings != null) {
				for (int i = 0; i < mappings.length; i++) {
					mapping = mappings[i];
					list = _listMapping(pc, mapping, path);
					if (!ArrayUtil.isEmpty(list)) rtn = add(rtn, list);
				}
			}
		}

		if (rtn == null) throw new ApplicationException("no package with name [" + packageName + "] found");
		return rtn;
	}

	private static Set<String> add(Set<String> set, String[] arr) {
		if (set == null) set = new HashSet<String>();
		for (int i = 0; i < arr.length; i++) {
			set.add(arr[i]);
		}
		return set;
	}

	private static String[] _listMapping(PageContext pc, Mapping mapping, String path) throws IOException {
		if (mapping.isPhysicalFirst()) {
			// check physical
			String[] list = _listPhysical(path, mapping);
			if (!ArrayUtil.isEmpty(list)) return list;

			// check archive
			list = _listArchive(pc, path, mapping);
			if (!ArrayUtil.isEmpty(list)) return list;
		}
		else {
			// check archive
			String[] list = _listArchive(pc, path, mapping);
			if (!ArrayUtil.isEmpty(list)) return list;
			// check physical
			list = _listPhysical(path, mapping);
			if (!ArrayUtil.isEmpty(list)) return list;
		}
		return null;
	}

	private static String[] _listPhysical(String path, Mapping mapping) {
		Resource physical = mapping.getPhysical();
		if (physical != null) {
			Resource dir = physical.getRealResource(path);
			if (dir.isDirectory()) {
				return dir.list(FILTER_CFC);
			}
		}
		return EMPTY;
	}

	private static String[] _listArchive(PageContext pc, String path, Mapping mapping) throws IOException {
		String packageName = StringUtil.replace(path, File.separator, ".", false);
		Resource archive = mapping.getArchive();
		if (archive != null) {
			// TODO nor working with pathes with none ascci characters, eith none ascci characters, the java
			// class path is renamed, so make sure you rename the path as well
			String strDir = "zip://" + archive + "!" + File.separator + path;
			Resource dir = ResourceUtil.toResourceNotExisting(pc, strDir, true, false);

			if (dir.isDirectory()) {
				java.util.List<String> list = new ArrayList<String>();
				// we use the class files here to get the info, the source files are optional and perhaps not
				// present.
				Resource[] children = dir.listResources(FILTER_CLASS);
				String className, c, sourceName = null;
				for (int i = 0; i < children.length; i++) {
					className = children[i].getName();
					className = className.substring(0, className.length() - 6);
					className = packageName + "." + className;

					try {
						Class<?> clazz = mapping.getArchiveClass(className);
						sourceName = ASMUtil.getSourceInfo(pc.getConfig(), clazz, true).name;
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
					}

					if (StringUtil.isEmpty(sourceName)) {
						c = IOUtil.toString(children[i], (Charset) null);
						int loc = c.indexOf("<clinit>");
						if (loc != -1) {
							c = c.substring(0, loc);
							c = ListUtil.last(c, "/\\", true).trim();
							if (Constants.isComponentExtension(ResourceUtil.getExtension(c, ""))) list.add(c);
						}
					}
					else list.add(sourceName);

				}
				if (list.size() > 0) return list.toArray(new String[list.size()]);
			}
		}
		return null;
	}
}