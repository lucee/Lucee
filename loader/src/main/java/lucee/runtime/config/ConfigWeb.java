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
package lucee.runtime.config;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import lucee.commons.io.res.Resource;
import lucee.runtime.CFMLFactory;
import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheHandlerCollection;
import lucee.runtime.exp.PageException;
import lucee.runtime.lock.LockManager;
import lucee.runtime.net.amf.AMFEngine;
import lucee.runtime.search.SearchEngine;

/**
 * Web Context
 */
public interface ConfigWeb extends Config, ServletConfig {

	/**
	 * @return lockmanager
	 */
	public LockManager getLockManager();

	/**
	 * @return return if is allowed to define request timeout via URL
	 */
	public boolean isAllowURLRequestTimeout();

	public String getLabel();

	public Resource getConfigServerDir();

	public CFMLFactory getFactory();

	/**
	 * 
	 * @param type Config.CACHE_TYPE_***
	 * @param defaultValue default value
	 * @return Returns a Cache Handler Collection.
	 */
	public CacheHandlerCollection getCacheHandlerCollection(int type, CacheHandlerCollection defaultValue);

	@Override
	public IdentificationWeb getIdentification();

	public ConfigServer getConfigServer(Password password) throws PageException;

	public SearchEngine getSearchEngine(PageContext pc) throws PageException;

	public boolean getSuppressWSBeforeArg();

	public JspWriter getWriter(PageContext pc, HttpServletRequest req, HttpServletResponse rsp);

	public AMFEngine getAMFEngine();
}