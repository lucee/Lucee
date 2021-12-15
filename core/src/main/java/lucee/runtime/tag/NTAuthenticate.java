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

import lucee.commons.io.SystemUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TagNotSupported;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class NTAuthenticate extends TagImpl {

	private String username;
	private String password;
	private String domain;
	private String result = "cfntauthenticate";
	// private String _action="auth";
	private boolean listGroups;
	private boolean throwOnError;

	@Override
	public void release() {
		super.release();
		username = null;
		password = null;
		domain = null;
		result = "cfntauthenticate";
		listGroups = false;
		throwOnError = false;

		// _action = "auth";

	}

	/*
	 * public void setListGroups(boolean b) { if(b) { listGroups = true; _action = "authAndGroups"; }
	 * else { listGroups = false; _action = "auth"; } }
	 */

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @param listGroups the listGroups to set
	 */
	public void setListgroups(boolean listGroups) {
		this.listGroups = listGroups;
	}

	/**
	 * @param throwOnError the throwOnError to set
	 */
	public void setThrowonerror(boolean throwOnError) {
		this.throwOnError = throwOnError;
	}

	@Override
	public int doStartTag() throws PageException {
		if (true) throw new TagNotSupported("ntauthenticate");
		String os = System.getProperty("os.name");
		Struct resultSt = new StructImpl();
		pageContext.setVariable(result, resultSt);

		if (SystemUtil.isWindows()) {
			/*
			 * 
			 * NTAuthentication ntauth = new NTAuthentication(domain); if(username != null)
			 * resultSt.set("username", username); try { boolean isAuth = false;
			 * 
			 * if(ntauth.IsUserInDirectory(username) && password != null && !StringUtil.isEmpty(domain)) isAuth
			 * = ntauth.AuthenticateUser(username, password);
			 * 
			 * resultSt.set(AUTH, Caster.toBoolean(isAuth)); resultSt.set(STATUS,
			 * isAuth?"success":"AuthenticationFailure");
			 * 
			 * if(listGroups && isAuth) { String groups =
			 * lucee.runtime.type.List.arrayToList(ntauth.GetUserGroups(username), ","); resultSt.set(GROUPS,
			 * groups); } } catch(Exception e) { resultSt.set(AUTH, Boolean.FALSE); if(e instanceof
			 * UserNotInDirException) resultSt.set(STATUS, "UserNotInDirFailure"); else if(e instanceof
			 * AuthenticationFailureException) resultSt.set(STATUS, "AuthenticationFailure");
			 * 
			 * if(throwOnError) throw new JspException(e); }
			 */
		}

		return 0;
	}

}