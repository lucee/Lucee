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
import lucee.runtime.exp.PageException;

public interface TemplateUtil {

	/**
	 * generate a ComponentJavaAccess (CJA) class from a component a CJA is a dynamic generated java
	 * class that has all method defined inside a component as java methods.
	 * 
	 * This is used to generated server side Webservices.
	 * 
	 * @param component
	 * @param isNew
	 * @param create
	 * @param writeLog
	 * @param suppressWSbeforeArg
	 * @param output
	 * @param returnValue if true the method returns the value of the last expression executed inside
	 *            when you call the method "call"
	 * @return
	 * @throws PageException
	 */
	public Class<?> getComponentJavaAccess(PageContext pc, Component component, RefBoolean isNew, boolean create, boolean writeLog, boolean suppressWSbeforeArg, boolean output,
			boolean returnValue) throws PageException;

	public Class<?> getComponentPropertiesClass(PageContext pc, Component component) throws PageException;

	public long getCompileTime(PageContext pc, PageSource ps, long defaultValue);

	public long getCompileTime(PageContext pc, PageSource ps) throws PageException;

	public Component searchComponent(PageContext pc, PageSource loadingLocation, String rawPath, Boolean searchLocal, Boolean searchRoot, boolean isExtendedComponent,
			boolean executeConstr) throws PageException;

	public Interface searchInterface(PageContext pc, PageSource loadingLocation, String rawPath, boolean executeConstr) throws PageException;

	public Page searchPage(PageContext pc, PageSource child, String rawPath, Boolean searchLocal, Boolean searchRoot) throws PageException;

	public Component loadComponent(PageContext pc, Page page, String callPath, boolean isRealPath, boolean silent, boolean isExtendedComponent, boolean executeConstr)
			throws PageException;

	public Component loadComponent(PageContext pc, PageSource ps, String callPath, boolean isRealPath, boolean silent, boolean executeConstr) throws PageException;

	public Page loadPage(PageContext pc, PageSource ps, boolean forceReload) throws PageException;

	public Interface loadInterface(PageContext pc, Page page, PageSource ps, String callPath, boolean isRealPath) throws PageException;

}