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

import javax.servlet.jsp.tagext.Tag;

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.AuthCookieData;
import lucee.runtime.listener.AuthCookieDataImpl;
import lucee.runtime.security.CredentialImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.CookieImpl;
import lucee.runtime.type.scope.Scope;

/**
 * 
 */
public final class Loginuser extends TagImpl {
	private String name;
	private String password;
	private String[] roles;

	@Override
	public void release() {
		super.release();
		name = null;
		password = null;
		roles = null;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param oRoles The roles to set.
	 * @throws PageException
	 */
	public void setRoles(Object oRoles) throws PageException {
		roles = CredentialImpl.toRole(oRoles);
	}

	@Override
	public int doStartTag() throws PageException {
		Resource rolesDir = pageContext.getConfig().getConfigDir().getRealResource("roles");
		CredentialImpl login = new CredentialImpl(name, password, roles, rolesDir);
		pageContext.setRemoteUser(login);

		Tag parent = getParent();
		while (parent != null && !(parent instanceof Login)) {
			parent = parent.getParent();
		}
		ApplicationContext appContext = pageContext.getApplicationContext();
		if (parent != null) {
			int loginStorage = appContext.getLoginStorage();
			String name = Login.getApplicationName(appContext);

			if (loginStorage == Scope.SCOPE_SESSION && pageContext.getApplicationContext().isSetSessionManagement())
				pageContext.sessionScope().set(KeyImpl.init(name), login.encode());
			else {
				ApplicationContext ac = pageContext.getApplicationContext();
				TimeSpan tsExpires = AuthCookieDataImpl.DEFAULT.getTimeout();
				if (ac instanceof ApplicationContextSupport) {
					ApplicationContextSupport acs = (ApplicationContextSupport) ac;
					AuthCookieData data = acs.getAuthCookie();
					if (data != null) {
						TimeSpan tmp = data.getTimeout();
						if (tmp != null) tsExpires = tmp;
					}
				}
				int expires;
				long tmp = tsExpires.getSeconds();
				if (Integer.MAX_VALUE < tmp) expires = Integer.MAX_VALUE;
				else expires = (int) tmp;

				((CookieImpl) pageContext.cookieScope()).setCookie(KeyImpl.init(name), login.encode(), expires, false, "/", Login.getCookieDomain(appContext), null);
			}
		}

		return SKIP_BODY;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}