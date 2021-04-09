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
package lucee.runtime;

import java.io.IOException;
import java.io.Serializable;

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;

/**
 * extends the source file with class features
 */
public interface PageSource extends Serializable {

	/**
	 * loads a page
	 * 
	 * @param pc page context
	 * @param forceReload force reload
	 * @return page source
	 * @throws PageException throws an exception when compilation fails or page does not exist
	 */
	public Page loadPage(PageContext pc, boolean forceReload) throws PageException;

	/**
	 * loads a page
	 * 
	 * @param pc page context
	 * @param forceReload force reload
	 * @param defaultValue default value
	 * @return page source
	 * @throws PageException throws an exception when compilation fails
	 */
	public Page loadPageThrowTemplateException(PageContext pc, boolean forceReload, Page defaultValue) throws PageException;

	/**
	 * loads a page
	 * 
	 * @param pc page context
	 * @param forceReload force reload
	 * @param defaultValue default value
	 * @return page source
	 */
	public Page loadPage(PageContext pc, boolean forceReload, Page defaultValue);

	/**
	 * returns the realpath without the mapping
	 * 
	 * @return Returns the realpath.
	 */
	public abstract String getRealpath();

	/**
	 * Returns the full name (mapping/realpath).
	 * 
	 * @return mapping/realpath
	 */
	public abstract String getRealpathWithVirtual();

	/**
	 * @return return the file name of the source file (test.cfm)
	 */
	public abstract String getFileName();

	/**
	 * if the pageSource is based on an archive, Lucee returns the ra:// path if the mapping physical
	 * path and archive is invalid or not defined, it is possible this method returns null
	 * 
	 * @return return the Resource matching this PageSource
	 */
	public abstract Resource getResource();

	/**
	 * if the pageSource is based on an archive, translate the source to a zip:// Resource
	 * 
	 * @return return the Resource matching this PageSource
	 * @param pc the Page Context Object
	 * @throws PageException Page Exception
	 */
	public abstract Resource getResourceTranslated(PageContext pc) throws PageException;

	/**
	 * @return returns the full classname (package and name) matching to filename (Example:
	 *         my.package.test_cfm)
	 */
	public String getClassName();

	public String getJavaName();

	/**
	 * @return returns the a package matching to file (Example: lucee.web)
	 */
	public abstract String getComponentName();

	/**
	 * @return returns mapping where PageSource based on
	 */
	public abstract Mapping getMapping();

	/**
	 * @return returns if page source exists or not
	 */
	public abstract boolean exists();

	/**
	 * @return returns if the physical part of the source file exists
	 */
	public abstract boolean physcalExists();

	/**
	 * @return return the source of the file as String array
	 * @throws IOException IO Exception
	 */
	public abstract String[] getSource() throws IOException;

	/**
	 * get an new Pagesource from realpath
	 * 
	 * @param realPath path
	 * @return new Pagesource
	 */
	public abstract PageSource getRealPage(String realPath);

	/**
	 * sets time last accessed page
	 * 
	 * @param lastAccess time ast accessed
	 */
	public abstract void setLastAccessTime(long lastAccess);

	/**
	 * 
	 * @return returns time last accessed page
	 */
	public abstract long getLastAccessTime();

	/**
	 * set time last accessed (now)
	 */
	public abstract void setLastAccessTime();

	/**
	 * @return returns how many this page is accessed since server is in use.
	 */
	public abstract int getAccessCount();

	/**
	 * return file object, based on physical path and realpath
	 * 
	 * @return file Object
	 */
	public Resource getPhyscalFile();

	/**
	 * @return return source path as String
	 */
	public String getDisplayPath();

	public int getDialect();

	/**
	 * returns true if the page source can be executed, means the source exists or is trusted and loaded
	 * 
	 * @return is the page source can be executed
	 */
	public boolean executable();

}