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
package lucee.runtime.component;

import lucee.runtime.ComponentImpl;
import lucee.runtime.Interface;
import lucee.runtime.InterfaceImpl;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.exp.MissingIncludeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.util.ArrayUtil;

public class MetadataUtil {

	public static Page getPageWhenMetaDataStillValid(PageContext pc, ComponentImpl comp, boolean ignoreCache) throws PageException {
		Page page = getPage(pc, comp._getPageSource());
		if (ignoreCache) return page;

		if (page != null && page.metaData != null && page.metaData.get() != null) {
			if (hasChanged(pc, ((MetaDataSoftReference) page.metaData).creationTime, comp)) {
				page.metaData = null;
			}
		}
		return page;
	}

	public static Page getPageWhenMetaDataStillValid(PageContext pc, InterfaceImpl interf, boolean ignoreCache) throws PageException {
		Page page = getPage(pc, interf.getPageSource());
		if (ignoreCache) return page;

		if (page != null && page.metaData != null && page.metaData.get() != null) {
			if (hasChanged(pc, ((MetaDataSoftReference) page.metaData).creationTime, interf)) page.metaData = null;
		}
		return page;
	}

	private static boolean hasChanged(PageContext pc, long lastMetaCreation, ComponentImpl component) throws PageException {
		if (component == null) return false;

		// check the component
		Page p = getPage(pc, component._getPageSource());
		if (p == null || hasChanged(p.getCompileTime(), lastMetaCreation)) return true;

		// check interfaces
		Interface[] interfaces = component.getInterfaces();
		if (!ArrayUtil.isEmpty(interfaces)) {
			if (hasChanged(pc, lastMetaCreation, interfaces)) return true;
		}

		// check base
		return hasChanged(pc, lastMetaCreation, (ComponentImpl) component.getBaseComponent());
	}

	private static boolean hasChanged(PageContext pc, long lastMetaCreation, Interface[] interfaces) throws PageException {

		if (!ArrayUtil.isEmpty(interfaces)) {
			for (int i = 0; i < interfaces.length; i++) {
				if (hasChanged(pc, lastMetaCreation, interfaces[i])) return true;
			}
		}
		return false;
	}

	private static boolean hasChanged(PageContext pc, long lastMetaCreation, Interface inter) throws PageException {
		Page p = getPage(pc, inter.getPageSource());
		if (p == null || hasChanged(p.getCompileTime(), lastMetaCreation)) return true;
		return hasChanged(pc, lastMetaCreation, inter.getExtends());
	}

	private static boolean hasChanged(long compileTime, long lastMetaCreation) {
		return compileTime > lastMetaCreation;
	}

	private static Page getPage(PageContext pc, PageSource ps) throws PageException {
		try {
			return ps.loadPage(pc, false);
		}
		catch (MissingIncludeException mie) {
			return null;
		}
	}
}