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
package lucee.runtime.type.scope;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;

/**
 * interface for the cookie scope
 */
public interface Cookie extends Scope, UserScope {

	/**
	 * set a cookie value
	 * 
	 * @param name name of the cookie
	 * @param value value of the cookie
	 * @param expires expirs of the cookie (Date, number in seconds or keyword as string )
	 * @param secure set secure or not
	 * @param path path of the cookie
	 * @param domain domain of the cookie
	 * @throws PageException Page Exception
	 * @deprecated
	 */
	@Deprecated
	public abstract void setCookie(Collection.Key name, Object value, Object expires, boolean secure, String path, String domain) throws PageException;

	/**
	 * set a cookie value
	 * 
	 * @param name Name of the cookie
	 * @param value value of the cookie
	 * @param expires expires in seconds
	 * @param secure secute or not
	 * @param path path of the cookie
	 * @param domain domain of the cookie
	 * @throws PageException Page Exception
	 * @deprecated
	 */
	@Deprecated
	public abstract void setCookie(Collection.Key name, Object value, int expires, boolean secure, String path, String domain) throws PageException;

	/**
	 * set a cookie value
	 * 
	 * @param name Name of the cookie
	 * @param value value of the cookie
	 * @param expires expires in seconds
	 * @param secure secute or not
	 * @param path path of the cookie
	 * @param domain domain of the cookie
	 * @deprecated
	 */
	@Deprecated
	public abstract void setCookieEL(Collection.Key name, Object value, int expires, boolean secure, String path, String domain);

	/**
	 * set a cookie value
	 * 
	 * @param name name of the cookie
	 * @param value value of the cookie
	 * @param expires expirs of the cookie (Date, number in seconds or keyword as string )
	 * @param secure set secure or not
	 * @param path path of the cookie
	 * @param domain domain of the cookie
	 * @param httpOnly if true, sets cookie as httponly so that it cannot be accessed using JavaScripts.
	 *            Note that the browser must have httponly compatibility.
	 * @param preserveCase if true, keep the case of the name as it is
	 * @param encode if true, url encode the name and the value
	 * @throws PageException Page Exception
	 */
	public abstract void setCookie(Collection.Key name, Object value, Object expires, boolean secure, String path, String domain, boolean httpOnly, boolean preserveCase,
			boolean encode) throws PageException;

	/**
	 * set a cookie value
	 * 
	 * @param name Name of the cookie
	 * @param value value of the cookie
	 * @param expires expires in seconds
	 * @param secure secute or not
	 * @param path path of the cookie
	 * @param domain domain of the cookie
	 * @param httpOnly if true, sets cookie as httponly so that it cannot be accessed using JavaScripts.
	 *            Note that the browser must have httponly compatibility.
	 * @param preserveCase if true, keep the case of the name as it is
	 * @param encode if true, url encode the name and the value
	 * @throws PageException Page Exception
	 */
	public abstract void setCookie(Collection.Key name, Object value, int expires, boolean secure, String path, String domain, boolean httpOnly, boolean preserveCase,
			boolean encode) throws PageException;

	/**
	 * set a cookie value
	 * 
	 * @param name Name of the cookie
	 * @param value value of the cookie
	 * @param expires expires in seconds
	 * @param secure secute or not
	 * @param path path of the cookie
	 * @param domain domain of the cookie
	 * @param httpOnly if true, sets cookie as httponly so that it cannot be accessed using JavaScripts.
	 *            Note that the browser must have httponly compatibility.
	 * @param preserveCase if true, keep the case of the name as it is
	 * @param encode if true, url encode the name and the value
	 */
	public abstract void setCookieEL(Collection.Key name, Object value, int expires, boolean secure, String path, String domain, boolean httpOnly, boolean preserveCase,
			boolean encode);

}