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
/**
 * Implements the CFML Function createobject
 * FUTURE neue attr unterstuestzen
 */
package lucee.runtime.functions.other;

import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.com.COMObject;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionNotSupported;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.HTTPClient;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.type.Struct;

public final class CreateObject implements Function {
	public static Object call(PageContext pc, String cfcName) throws PageException {
		return call(pc, "component", cfcName, null, null);
	}

	public static Object call(PageContext pc, String type, String className) throws PageException {
		return call(pc, type, className, null, null);
	}

	public static Object call(PageContext pc, String type, String className, Object context) throws PageException {
		return call(pc, type, className, context, null);
	}

	public static Object call(PageContext pc, String type, String className, Object context, Object serverName) throws PageException {
		type = StringUtil.toLowerCase(type);

		// JAVA
		if (type.equals("java")) {
			checkAccess(pc, type);
			return doJava(pc, className, context, Caster.toString(serverName));
		}
		// COM
		if (type.equals("com")) {
			return doCOM(pc, className);
		}
		// Component
		if (type.equals("component") || type.equals("cfc")) {
			return doComponent(pc, className);
		}
		// Webservice
		if (type.equals("webservice") || type.equals("wsdl")) {
			String user = null;
			String pass = null;
			ProxyDataImpl proxy = null;
			if (context != null) {
				Struct args = (serverName != null) ? Caster.toStruct(serverName) : Caster.toStruct(context);
				// basic security
				user = Caster.toString(args.get("username", null));
				pass = Caster.toString(args.get("password", null));

				// proxy
				String proxyServer = Caster.toString(args.get("proxyServer", null));
				String proxyPort = Caster.toString(args.get("proxyPort", null));
				String proxyUser = Caster.toString(args.get("proxyUser", null));
				if (StringUtil.isEmpty(proxyUser)) proxyUser = Caster.toString(args.get("proxyUsername", null));
				String proxyPassword = Caster.toString(args.get("proxyPassword", null));

				if (!StringUtil.isEmpty(proxyServer)) {
					proxy = new ProxyDataImpl(proxyServer, Caster.toIntValue(proxyPort, -1), proxyUser, proxyPassword);
				}

			}
			return doWebService(pc, className, user, pass, proxy);
		}
		if (type.equals("http")) {
			String user = null;
			String pass = null;
			ProxyDataImpl proxy = null;
			if (context != null) {
				Struct args = (serverName != null) ? Caster.toStruct(serverName) : Caster.toStruct(context);
				// basic security
				user = Caster.toString(args.get("username", null));
				pass = Caster.toString(args.get("password", null));

				// proxy
				String proxyServer = Caster.toString(args.get("proxyServer", null));
				String proxyPort = Caster.toString(args.get("proxyPort", null));
				String proxyUser = Caster.toString(args.get("proxyUser", null));
				if (StringUtil.isEmpty(proxyUser)) proxyUser = Caster.toString(args.get("proxyUsername", null));
				String proxyPassword = Caster.toString(args.get("proxyPassword", null));

				if (!StringUtil.isEmpty(proxyServer)) {
					proxy = new ProxyDataImpl(proxyServer, Caster.toIntValue(proxyPort, -1), proxyUser, proxyPassword);
				}

			}
			return doHTTP(pc, className, user, pass, proxy);
		}
		// .net
		if (type.equals(".net") || type.equals("dotnet")) {
			return doDotNet(pc, className);
		}
		throw new ExpressionException(
				"Invalid argument for function createObject, first argument (type), " + "must be (com, java, webservice or component) other types are not supported");

	}

	private static Object doDotNet(PageContext pc, String className) throws FunctionNotSupported {
		throw new FunctionNotSupported("CreateObject", "type .net");
	}

	private static void checkAccess(PageContext pc, String type) throws SecurityException {
		if (pc.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_OBJECT) == SecurityManager.VALUE_NO)
			throw new SecurityException("Can't access function [createObject] with type [" + type + "]", "Access is denied by the Security Manager");
	}

	public static Object doJava(PageContext pc, String className, Object pathsOrBundleName, String delimiterOrBundleVersion) throws PageException {
		return JavaProxy.call(pc, className, pathsOrBundleName, delimiterOrBundleVersion);
	}

	public static Object doCOM(PageContext pc, String className) {
		return new COMObject(className);
	}

	public static Component doComponent(PageContext pc, String className) throws PageException {
		return pc.loadComponent(className);
	}

	public static Object doWebService(PageContext pc, String wsdlUrl) throws PageException {
		// TODO CF8 impl. all new attributes for wsdl
		return ((ConfigWebImpl) ThreadLocalPageContext.getConfig(pc)).getWSHandler().getWSClient(wsdlUrl, null, null, null);
	}

	public static Object doWebService(PageContext pc, String wsdlUrl, String username, String password, ProxyData proxy) throws PageException {
		// TODO CF8 impl. all new attributes for wsdl
		return ((ConfigWebImpl) ThreadLocalPageContext.getConfig(pc)).getWSHandler().getWSClient(wsdlUrl, username, password, proxy);
	}

	public static Object doHTTP(PageContext pc, String httpUrl) throws PageException {
		return new HTTPClient(httpUrl, null, null, null);
	}

	public static Object doHTTP(PageContext pc, String httpUrl, String username, String password, ProxyData proxy) throws PageException {
		return new HTTPClient(httpUrl, username, password, proxy);
	}
}