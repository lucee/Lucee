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
package lucee.runtime.tag;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.CookieData;
import lucee.runtime.listener.SessionCookieData;
import lucee.runtime.listener.SessionCookieDataImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.scope.CookieImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Defines cookie variables, including expiration and security options.
 *
 *
 *
 **/
public final class Cookie extends TagImpl {

	/**
	 * Yes or No. Specifies that the variable must transmit securely. If the browser does not support
	 ** Secure Socket Layer (SSL) security, the cookie is not sent.
	 */
	private boolean secure = false;

	/** The value assigned to the cookie variable. */
	private String value = "";

	/**  */
	private String domain = null;

	/**  */
	private String path = "/";

	/**
	 * Schedules the expiration of a cookie variable. Can be specified as a date (as in, 10/09/97),
	 ** number of days (as in, 10, 100), "Now", or "Never". Using Now effectively deletes the cookie from
	 ** the client browser.
	 */
	private Object expires = null;

	/** The name of the cookie variable. */
	private String name;

	private boolean httponly;
	private boolean preservecase;
	private Boolean _encode = null;

	private short samesite = SessionCookieData.SAMESITE_EMPTY;

	@Override
	public void release() {
		super.release();
		secure = false;
		value = "";
		domain = null;
		path = "/";
		expires = null;
		name = null;
		httponly = false;
		preservecase = false;
		_encode = null;
		samesite = SessionCookieData.SAMESITE_EMPTY;
	}

	/**
	 * set the value secure Yes or No. Specifies that the variable must transmit securely. If the
	 * browser does not support Secure Socket Layer (SSL) security, the cookie is not sent.
	 * 
	 * @param secure value to set
	 **/
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	/**
	 * set the value value The value assigned to the cookie variable.
	 * 
	 * @param value value to set
	 **/
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * set the value domain
	 * 
	 * @param domain value to set
	 **/
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * set the value path
	 * 
	 * @param path value to set
	 **/
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * set the value expires Schedules the expiration of a cookie variable. Can be specified as a date
	 * (as in, 10/09/97), number of days (as in, 10, 100), "Now", or "Never". Using Now effectively
	 * deletes the cookie from the client browser.
	 * 
	 * @param expires value to set
	 **/
	public void setExpires(Object expires) {
		this.expires = expires;
	}

	/**
	 * set the value expires Schedules the expiration of a cookie variable. Can be specified as a date
	 * (as in, 10/09/97), number of days (as in, 10, 100), "Now", or "Never". Using Now effectively
	 * deletes the cookie from the client browser.
	 * 
	 * @param expires value to set
	 * @deprecated replaced with setExpires(Object expires):void
	 **/
	@Deprecated
	public void setExpires(String expires) {
		this.expires = expires;
	}

	/**
	 * set the value name The name of the cookie variable.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	public void setHttponly(boolean httponly) {
		this.httponly = httponly;
	}

	public void setPreservecase(boolean preservecase) {
		this.preservecase = preservecase;
	}

	public void setEncodevalue(boolean encode) {
		this._encode = encode;
	}

	public void setEncode(boolean encode) {
		this._encode = encode;
	}

	public void setSamesite(String samesite) throws ApplicationException {
		this.samesite = SessionCookieDataImpl.toSamesite(samesite);
	}

	@Override
	public int doStartTag() throws PageException {
		Key key = KeyImpl.getInstance(name);
		String appName = Login.getApplicationName(pageContext.getApplicationContext());
		boolean isAppName = false;
		if (KeyConstants._CFID.equalsIgnoreCase(key) || KeyConstants._CFTOKEN.equalsIgnoreCase(key) || (isAppName = key.equals(appName))) {
			ApplicationContext ac = pageContext.getApplicationContext();
			if (ac instanceof ApplicationContextSupport) {
				ApplicationContextSupport acs = (ApplicationContextSupport) ac;
				CookieData data = isAppName ? acs.getAuthCookie() : acs.getSessionCookie();
				if (data != null && data.isDisableUpdate()) throw new ExpressionException("customize " + key + " is disabled!");

			}
		}
		((CookieImpl) pageContext.cookieScope()).setCookie(key, value, expires, secure, path, domain, httponly, preservecase, _encode, samesite);
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}