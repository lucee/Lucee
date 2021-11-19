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
package lucee.runtime.util;

import lucee.commons.lang.types.RefBoolean;
import lucee.runtime.Component;
import lucee.runtime.Interface;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.util.ComponentUtil;

public class TemplateUtilImpl implements TemplateUtil {

	@Override
	public Class<?> getComponentJavaAccess(PageContext pc, Component component, RefBoolean isNew, boolean create, boolean writeLog, boolean suppressWSbeforeArg, boolean output,
			boolean returnvalue) throws PageException {
		return ComponentUtil.getComponentJavaAccess(pc, component, isNew, create, writeLog, suppressWSbeforeArg, output, returnvalue);
	}

	@Override
	public Class<?> getComponentPropertiesClass(PageContext pc, Component component) throws PageException {
		return ComponentUtil.getComponentPropertiesClass(pc, component);
	}

	@Override
	public long getCompileTime(PageContext pc, PageSource ps, long defaultValue) {
		return ComponentUtil.getCompileTime(pc, ps, defaultValue);
	}

	@Override
	public long getCompileTime(PageContext pc, PageSource ps) throws PageException {
		return ComponentUtil.getCompileTime(pc, ps);
	}

	@Override
	public Component searchComponent(PageContext pc, PageSource loadingLocation, String rawPath, Boolean searchLocal, Boolean searchRoot, boolean isExtendedComponent,
			boolean executeConstr) throws PageException {
		return ComponentLoader.searchComponent(pc, loadingLocation, rawPath, searchLocal, searchRoot, isExtendedComponent, executeConstr);
	}

	@Override
	public Interface searchInterface(PageContext pc, PageSource loadingLocation, String rawPath, boolean executeConstr) throws PageException {
		return ComponentLoader.searchInterface(pc, loadingLocation, rawPath, executeConstr);
	}

	@Override
	public Page searchPage(PageContext pc, PageSource child, String rawPath, Boolean searchLocal, Boolean searchRoot) throws PageException {
		return ComponentLoader.searchPage(pc, child, rawPath, searchLocal, searchRoot);
	}

	@Override
	public Component loadComponent(PageContext pc, PageSource ps, String callPath, boolean isRealPath, boolean silent, boolean executeConstr) throws PageException {
		return ComponentLoader.loadComponent(pc, ps, callPath, isRealPath, silent, executeConstr);
	}

	@Override
	public Page loadPage(PageContext pc, PageSource ps, boolean forceReload) throws PageException {
		return ComponentLoader.loadPage(pc, ps, forceReload);
	}

	@Override
	public Interface loadInterface(PageContext pc, Page page, PageSource ps, String callPath, boolean isRealPath) throws PageException {
		return ComponentLoader.loadInterface(pc, page, ps, callPath, isRealPath);
	}

	@Override
	public Component loadComponent(PageContext pc, Page page, String callPath, boolean isRealPath, boolean silent, boolean isExtendedComponent, boolean executeConstr)
			throws PageException {
		return ComponentLoader.loadComponent(pc, page, callPath, isRealPath, silent, isExtendedComponent, executeConstr);
	}
}