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
/**
 * Implements the CFML Function createobject
 * FUTURE neue attr unterstuestzen
 */
package lucee.runtime.functions.other;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.HTTPClient;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.type.Struct;

public final class WebserviceProxy implements Function {

	private static final long serialVersionUID = -5702516737227809987L;
	private static final Data EMPTY = new Data(null, null, null);

	public static Object call(PageContext pc, String wsdlUrl) throws PageException {
		return call(pc, wsdlUrl, null);
	}

	public static Object call(PageContext pc, String wsdlUrl, Struct args) throws PageException {
		checkAccess(pc);
		// MUST terminate webservice type smarter
		Data data = readArgs(args);

		// Soap/WSDL
		if (StringUtil.indexOfIgnoreCase(wsdlUrl, "?wsdl") != -1) {
			return doWebService(pc, wsdlUrl, data.user, data.pass, data.proxy);
		}
		// HTTP
		return doHTTP(pc, wsdlUrl, data.user, data.pass, data.proxy);

	}

	private static void checkAccess(PageContext pc) throws SecurityException {
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_OBJECT) == SecurityManager.VALUE_NO)
			throw new SecurityException("Can't access function [webserviceProxy]", "Access is denied by the Security Manager");
	}

	public static Object doWebService(PageContext pc, String wsdlUrl) throws PageException {
		// TODO CF8 impl. all new attributes for wsdl
		return ((ConfigWebPro) ThreadLocalPageContext.getConfig(pc)).getWSHandler().getWSClient(wsdlUrl, null, null, null);
	}

	public static Object doWebService(PageContext pc, String wsdlUrl, String username, String password, ProxyData proxy) throws PageException {
		// TODO CF8 impl. all new attributes for wsdl
		return ((ConfigWebPro) ThreadLocalPageContext.getConfig(pc)).getWSHandler().getWSClient(wsdlUrl, username, password, proxy);
	}

	public static Object doHTTP(PageContext pc, String httpUrl) throws PageException {
		return new HTTPClient(httpUrl, null, null, null);
	}

	public static Object doHTTP(PageContext pc, String httpUrl, String username, String password, ProxyData proxy) throws PageException {
		return new HTTPClient(httpUrl, username, password, proxy);
	}

	private static Data readArgs(Struct args) throws PageException {
		if (args != null) {
			// basic security
			ProxyDataImpl proxy = null;
			String user = Caster.toString(args.get("username", null));
			String pass = Caster.toString(args.get("password", null));

			// proxy
			String proxyServer = Caster.toString(args.get("proxyServer", null));
			String proxyPort = Caster.toString(args.get("proxyPort", null));
			String proxyUser = Caster.toString(args.get("proxyUser", null));
			if (StringUtil.isEmpty(proxyUser)) proxyUser = Caster.toString(args.get("proxyUsername", null));
			String proxyPassword = Caster.toString(args.get("proxyPassword", null));

			if (!StringUtil.isEmpty(proxyServer)) {
				proxy = new ProxyDataImpl(proxyServer, Caster.toIntValue(proxyPort, -1), proxyUser, proxyPassword);
			}
			return new Data(user, pass, proxy);
		}
		return EMPTY;
	}

	static class Data {
		String user = null;
		String pass = null;

		public Data(String user, String pass, ProxyDataImpl proxy) {
			super();
			this.user = user;
			this.pass = pass;
			this.proxy = proxy;
		}

		ProxyDataImpl proxy = null;

	}
}