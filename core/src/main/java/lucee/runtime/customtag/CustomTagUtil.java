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
package lucee.runtime.customtag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.util.ListUtil;

public class CustomTagUtil {

	public static InitFile loadInitFile(PageContext pc, String name) throws PageException {

		InitFile initFile = loadInitFile(pc, name, null);
		if (initFile != null) {
			return initFile;
		}
		// EXCEPTION
		ConfigWeb config = pc.getConfig();
		// message
		StringBuilder msg = new StringBuilder("Custom tag \"").append(getDisplayName(config, name)).append("\" was not found.");

		List<String> dirs = new ArrayList();

		if (config.doLocalCustomTag()) {

			dirs.add(ResourceUtil.getResource(pc, pc.getCurrentPageSource()).getParent());
		}

		Mapping[] actms = pc.getApplicationContext().getCustomTagMappings();
		Mapping[] cctms = config.getCustomTagMappings();

		Resource r;

		if (actms != null) {
			for (Mapping m: actms) {

				r = m.getPhysical();
				if (r != null) dirs.add(r.toString());
			}
		}

		if (cctms != null) {
			for (Mapping m: cctms) {

				r = m.getPhysical();
				if (r != null) dirs.add(r.toString());
			}
		}

		if (!dirs.isEmpty()) {

			msg.append(" Directories searched: ");

			Iterator<String> it = dirs.iterator();
			while (it.hasNext()) {

				msg.append('"').append(it.next()).append('"');
				if (it.hasNext()) msg.append(", ");
			}
		}

		throw new ExpressionException(msg.toString());
	}

	public static InitFile loadInitFile(PageContext pc, String name, InitFile defaultValue) throws PageException {
		ConfigPro config = (ConfigPro) pc.getConfig();
		String[] filenames = getFileNames(config, name);
		boolean doCache = config.useCTPathCache();

		boolean doCustomTagDeepSearch = config.doCustomTagDeepSearch();
		PageSource ps = null;
		InitFile initFile;

		// CACHE
		// check local
		String localCacheName = null;
		Mapping[] actms = pc.getApplicationContext().getCustomTagMappings();
		Mapping[] cctms = config.getCustomTagMappings();

		if (doCache) {
			if (pc.getConfig().doLocalCustomTag()) {
				localCacheName = pc.getCurrentPageSource().getDisplayPath().replace('\\', '/');
				localCacheName = "local:" + localCacheName.substring(0, localCacheName.lastIndexOf('/') + 1).concat(name);
				initFile = config.getCTInitFile(pc, localCacheName);
				if (initFile != null) return initFile;
			}

			// cache application mapping
			if (actms != null) for (int i = 0; i < actms.length; i++) {
				initFile = config.getCTInitFile(pc, "application:" + actms[i].hashCode() + "/" + name);
				if (initFile != null) return initFile;
			}

			// cache config mapping
			if (cctms != null) for (int i = 0; i < cctms.length; i++) {
				initFile = config.getCTInitFile(pc, "config:" + cctms[i].hashCode() + "/" + name);
				if (initFile != null) return initFile;
			}
		}

		// SEARCH
		// search local
		if (pc.getConfig().doLocalCustomTag()) {
			for (int i = 0; i < filenames.length; i++) {
				PageSource[] arr = ((PageContextImpl) pc).getRelativePageSources(filenames[i]);
				// ps=pc.getRelativePageSource(filenames[i]);
				ps = MappingImpl.isOK(arr);
				if (ps != null) {
					initFile = new InitFile(pc, ps, filenames[i]);
					if (doCache) config.putCTInitFile(localCacheName, initFile);
					return initFile;
				}
			}
		}

		// search application custom tag mapping
		if (actms != null) {
			for (int i = 0; i < filenames.length; i++) {
				ps = getMapping(actms, filenames[i], doCustomTagDeepSearch);
				if (ps != null) {
					initFile = new InitFile(pc, ps, filenames[i]);
					if (doCache) config.putCTInitFile("application:" + ps.getMapping().hashCode() + "/" + name, initFile);
					return initFile;
				}
			}
		}

		// search custom tag mappings
		for (int i = 0; i < filenames.length; i++) {
			ps = getMapping(cctms, filenames[i], doCustomTagDeepSearch);
			if (ps != null) {
				initFile = new InitFile(pc, ps, filenames[i]);
				if (doCache) config.putCTInitFile("config:" + ps.getMapping().hashCode() + "/" + name, initFile);
				return initFile;
			}
		}

		return defaultValue;
	}

	public static String getComponentExtension(PageContext pc, PageSource ps) {
		return ps.getDialect() == CFMLEngine.DIALECT_CFML ? Constants.getCFMLComponentExtension() : Constants.getLuceeComponentExtension();
	}

	public static String[] getFileNames(Config config, String name) throws ExpressionException {
		String[] extensions = config.getCustomTagExtensions();
		if (extensions.length == 0) throw new ExpressionException("Custom Tags are disabled");
		String[] fileNames = new String[extensions.length];

		for (int i = 0; i < fileNames.length; i++) {
			fileNames[i] = name + '.' + extensions[i];
		}
		return fileNames;
	}

	private static PageSource getMapping(Mapping[] ctms, String filename, boolean doCustomTagDeepSearch) {
		PageSource ps;
		for (int i = 0; i < ctms.length; i++) {
			ps = ((MappingImpl) ctms[i]).getCustomTagPath(filename, doCustomTagDeepSearch);
			if (ps != null) return ps;
		}
		return null;
	}

	public static String getDisplayName(Config config, String name) {
		String[] extensions = config.getCustomTagExtensions();
		if (extensions.length == 0) return name;

		return name + ".[" + ListUtil.arrayToList(extensions, "|") + "]";
	}

	public static String toString(Mapping[] ctms) {
		if (ctms == null) return "";
		StringBuilder sb = new StringBuilder();
		Resource p;
		for (int i = 0; i < ctms.length; i++) {
			if (sb.length() != 0) sb.append(", ");
			p = ctms[i].getPhysical();
			if (p != null) sb.append(p.toString());
		}
		return sb.toString();
	}
}