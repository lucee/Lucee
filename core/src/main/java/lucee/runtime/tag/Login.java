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

import java.io.IOException;

import lucee.runtime.coder.Base64Coder;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.security.Credential;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * 
 */
public final class Login extends BodyTagImpl {

	private static final Key CFLOGIN = KeyImpl.getInstance("cflogin");
	private int idletimeout = 1800;
	private String applicationtoken;
	private String cookiedomain;

	@Override
	public void release() {
		super.release();
		idletimeout = 1800;
		applicationtoken = null;
		cookiedomain = null;
	}

	/**
	 * @param applicationtoken The applicationtoken to set.
	 */
	public void setApplicationtoken(String applicationtoken) {
		this.applicationtoken = applicationtoken;
	}

	/**
	 * @param cookiedomain The cookiedomain to set.
	 */
	public void setCookiedomain(String cookiedomain) {
		this.cookiedomain = cookiedomain;
	}

	/**
	 * @param idletimeout The idletimout to set.
	 */
	public void setIdletimeout(double idletimeout) {
		this.idletimeout = (int) idletimeout;
	}

	@Override
	public int doStartTag() throws PageException {

		ApplicationContext ac = pageContext.getApplicationContext();
		ac.setSecuritySettings(applicationtoken, cookiedomain, idletimeout);

		Credential remoteUser = pageContext.getRemoteUser();
		if (remoteUser == null) {

			// Form
			Object name = pageContext.formScope().get("j_username", null);
			Object password = pageContext.formScope().get("j_password", null);
			if (name != null) {
				setCFLogin(name, password);
				return EVAL_BODY_INCLUDE;
			}
			// Header
			String strAuth = pageContext.getHttpServletRequest().getHeader("authorization");
			if (strAuth != null) {
				int pos = strAuth.indexOf(' ');
				if (pos != -1) {
					String format = strAuth.substring(0, pos).toLowerCase();
					if (format.equals("basic")) {
						String encoded = strAuth.substring(pos + 1);
						String dec;
						try {
							dec = Base64Coder.decodeToString(encoded, "UTF-8");
						}
						catch (IOException e) {
							throw Caster.toPageException(e);
						}

						// print.ln("encoded:"+encoded);
						// print.ln("decoded:"+Base64Util.decodeBase64(encoded));
						Array arr = ListUtil.listToArray(dec, ":");
						if (arr.size() < 3) {
							if (arr.size() == 1) setCFLogin(arr.get(1, null), "");
							else setCFLogin(arr.get(1, null), arr.get(2, null));
						}
					}

				}
			}
			return EVAL_BODY_INCLUDE;
		}
		return SKIP_BODY;
	}

	/**
	 * @param username
	 * @param password
	 */
	private void setCFLogin(Object username, Object password) {
		if (username == null) return;
		if (password == null) password = "";

		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._name, username);
		sct.setEL(KeyConstants._password, password);
		pageContext.undefinedScope().setEL(CFLOGIN, sct);
	}

	@Override
	public int doEndTag() {
		pageContext.undefinedScope().removeEL(CFLOGIN);
		return EVAL_PAGE;
	}

	public static String getApplicationName(ApplicationContext appContext) {
		return "cfauthorization_" + appContext.getSecurityApplicationToken();
	}

	public static String getCookieDomain(ApplicationContext appContext) {
		return appContext.getSecurityCookieDomain();
	}

	public static int getIdleTimeout(ApplicationContext appContext) {
		return appContext.getSecurityIdleTimeout();
	}
}