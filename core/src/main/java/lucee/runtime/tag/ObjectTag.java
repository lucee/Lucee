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

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.functions.other.CreateObject;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.security.SecurityManager;

/**
 * Lets you call methods in COM, CORBA, and JAVA objects.
 *
 *
 *
 **/
public final class ObjectTag extends TagImpl {

	/*
	 * Component - name - component TODO support full functionality Component ------------- name
	 * component
	 * 
	 * Com ---------- type action class name context server
	 * 
	 * Corba -------------------- type context class name locale
	 * 
	 * Java -------------------- type action class name
	 * 
	 * Webservice --------------------------- webservice name
	 * 
	 * all ------------- name component type action class context server locale webservice
	 */

	private String name;
	private String component;
	private String type = "";
	private String action;
	private String clazz;
	private String context;
	private String server;
	private String locale;
	private String webservice;
	private String delimiters = ",";

	private String username;
	private String password;
	private String proxyServer;
	private int proxyPort;
	private String proxyUser;
	private String proxyPassword;

	@Override
	public void release() {
		super.release();
		name = null;
		component = null;
		type = "";
		action = null;
		clazz = null;
		context = null;
		server = null;
		locale = null;
		webservice = null;
		delimiters = ",";

		username = null;
		password = null;
		proxyServer = null;
		proxyPort = -1;
		proxyUser = null;
		proxyPassword = null;
	}

	@Override
	public int doStartTag() throws PageException {

		if (component != null) {
			pageContext.setVariable(name, CreateObject.doComponent(pageContext, component));
		}
		else if (type.equals("java")) {
			checkAccess(pageContext, type);
			checkClass();
			pageContext.setVariable(name, CreateObject.doJava(pageContext, clazz, context, delimiters));
		}
		else if (type.equals("com")) {
			checkAccess(pageContext, type);
			checkClass();
			pageContext.setVariable(name, CreateObject.doCOM(pageContext, clazz));
		}
		else if (type.equals("webservice")) {
			checkAccess(pageContext, type);
			checkWebservice();
			ProxyData proxy = null;
			if (proxyServer != null) {
				proxy = new ProxyDataImpl(proxyServer, proxyPort, proxyUser, proxyPassword);
			}
			pageContext.setVariable(name, CreateObject.doWebService(pageContext, webservice, username, password, proxy));
		}
		else {
			if (type == null) throw new ApplicationException("Too few attributes defined for tag [object]");
			throw new ApplicationException("Wrong value for attribute [type]",
					"types are [com,java,webservice,corba] and the only supported type (at the moment) are [webservice,com,component,java]");
		}

		return SKIP_BODY;
	}

	private static void checkAccess(PageContext pc, String type) throws SecurityException {
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_OBJECT) == SecurityManager.VALUE_NO)
			throw new SecurityException("Cannot access tag [object] with type [" + type + "]", "access is prohibited by security manager");

	}

	/**
	 * check if attribute class is defined
	 * 
	 * @throws ApplicationException
	 */
	private void checkClass() throws ApplicationException {
		if (clazz == null) throw new ApplicationException("Attribute [class] is required");
	}

	/**
	 * check if attribute webservice is defined
	 * 
	 * @throws ApplicationException
	 */
	private void checkWebservice() throws ApplicationException {
		if (webservice == null) throw new ApplicationException("Attribute [webservice] is required");
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	/**
	 * @param locale The locale to set.
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param server The server to set.
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type.toLowerCase().trim();
	}

	/**
	 * @param webservice The webservice to set.
	 */
	public void setWebservice(String webservice) {
		this.type = "webservice";
		this.webservice = webservice;
	}

	/**
	 * @param action The action to set.
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @param clazz The clazz to set.
	 */
	public void setClass(String clazz) {
		this.clazz = clazz;
	}

	/**
	 * @param component The component to set.
	 */
	public void setComponent(String component) {
		this.component = component;
	}

	/**
	 * @param context The context to set.
	 */
	public void setContext(String context) {
		this.context = context;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param proxyServer the proxyServer to set
	 */
	public void setProxyServer(String proxyServer) {
		this.proxyServer = proxyServer;
	}

	/**
	 * @param proxyPort the proxyPort to set
	 */
	public void setProxyPort(double proxyPort) {
		this.proxyPort = (int) proxyPort;
	}

	/**
	 * @param proxyUser the proxyUser to set
	 */
	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	/**
	 * @param proxyPassword the proxyPassword to set
	 */
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	/**
	 * @param delimiters the delimiters to set
	 */
	public void setDelimiters(String delimiters) {
		this.delimiters = delimiters;
	}

}
