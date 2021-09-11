/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.commons.lang;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.DirectoryResourceFilter;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.SourceNameClassVisitor.SourceInfo;

public class MappingUtil {

	public static PageSource searchMappingRecursive(Mapping mapping, String name, boolean onlyCFC) {
		if (name.indexOf('/') == -1) { // TODO handle this as well?
			Config config = mapping.getConfig();
			ExtensionResourceFilter ext = null;
			if (onlyCFC) ext = new ExtensionResourceFilter(Constants.getComponentExtensions(), true, true);
			else {
				ext = new ExtensionResourceFilter(Constants.getExtensions(), true, true);
				// ext.addExtension(config.getComponentExtension());
			}

			if (mapping.isPhysicalFirst()) {
				PageSource ps = searchPhysical(mapping, name, ext);
				if (ps != null) return ps;
				ps = searchArchive(mapping, name, onlyCFC);
				if (ps != null) return ps;
			}
			else {
				PageSource ps = searchArchive(mapping, name, onlyCFC);
				if (ps != null) return ps;
				ps = searchPhysical(mapping, name, ext);
				if (ps != null) return ps;
			}
		}
		return null;
	}

	private static PageSource searchArchive(Mapping mapping, String name, boolean onlyCFC) {
		Resource archive = mapping.getArchive();
		if (archive != null && archive.isFile()) {
			ZipInputStream zis = null;
			try {
				zis = new ZipInputStream(archive.getInputStream());
				ZipEntry entry;
				Class<?> clazz;
				while ((entry = zis.getNextEntry()) != null) {
					if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
					clazz = mapping.getArchiveClass(toClassName(entry.getName()), null);

					if (clazz == null) continue;
					SourceInfo srcInf = ASMUtil.getSourceInfo(mapping.getConfig(), clazz, onlyCFC);
					if (name.equalsIgnoreCase(srcInf.name)) {
						PageSource ps = mapping.getPageSource(srcInf.relativePath);
						return ps;
					}
				}
			}
			catch (IOException ioe) {
				LogUtil.log(mapping.getConfig(), "mapping", ioe);
			}
			finally {
				try {
					IOUtil.close(zis);
				}
				catch (IOException ioe) {
					LogUtil.log(mapping.getConfig(), "mapping", ioe);
				}
			}

		}
		// TODO Auto-generated method stub
		return null;
	}

	private static String toClassName(String name) {
		return name.replace('/', '.').substring(0, name.length() - 6);
	}

	private static PageSource searchPhysical(Mapping mapping, String name, ResourceFilter filter) {
		Resource physical = mapping.getPhysical();
		if (physical != null) {
			String _path = searchPhysical(mapping.getPhysical(), null, name, filter, true);

			if (_path != null) {
				return mapping.getPageSource(_path);
			}
		}
		return null;
	}

	private static String searchPhysical(Resource res, String dir, String name, ResourceFilter filter, boolean top) {
		if (res.isFile()) {
			if (res.getName().equalsIgnoreCase(name)) {
				return dir + res.getName();
			}
		}
		else if (res.isDirectory()) {
			Resource[] _dir = res.listResources(top ? DirectoryResourceFilter.FILTER : filter);
			if (_dir != null) {
				if (dir == null) dir = "/";
				else dir = dir + res.getName() + "/";
				String path;
				for (int i = 0; i < _dir.length; i++) {
					path = searchPhysical(_dir[i], dir, name, filter, false);
					if (path != null) return path;
				}
			}
		}

		return null;
	}

	public static SourceInfo getMatch(PageContext pc, StackTraceElement trace) {
		return getMatch(pc, null, trace);

	}

	public static SourceInfo getMatch(Config config, StackTraceElement trace) {
		return getMatch(null, config, trace);
	}

	public static SourceInfo getMatch(PageContext pc, Config config, StackTraceElement trace) {
		if (trace.getFileName() == null) return null;

		if (pc == null && config == null) config = ThreadLocalPageContext.getConfig();

		// PageContext pc = ThreadLocalPageContext.get();
		Mapping[] mappings = pc != null ? ConfigWebUtil.getAllMappings(pc) : ConfigWebUtil.getAllMappings(config);
		if (pc != null) config = pc.getConfig();

		Mapping mapping;
		Class clazz;
		for (int i = 0; i < mappings.length; i++) {
			mapping = mappings[i];
			// print.e("virtual:"+mapping.getVirtual()+"+"+trace.getClassName());
			// look for the class in that mapping
			clazz = ((MappingImpl) mapping).loadClass(trace.getClassName());
			if (clazz == null) continue;

			// classname is not distinct, because of that we must check class content
			try {
				SourceInfo si = ASMUtil.getSourceInfo(config, clazz, false);
				if (si != null && trace.getFileName() != null && trace.getFileName().equals(si.absolutePath(pc))) return si;
			}
			catch (IOException e) {
			}

		}
		return null;
	}
}