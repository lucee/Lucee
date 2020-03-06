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
package lucee.runtime.util;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.tagext.BodyContent;

import lucee.cli.servlet.HTTPServletImpl;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.op.Caster;
import lucee.runtime.op.CreationImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class PageContextUtil {

	public static PageSource getPageSource(Mapping[] mappings, String realPath) {
		PageSource ps;
		for (int i = 0; i < mappings.length; i++) {
			ps = mappings[i].getPageSource(realPath);
			if (ps.exists()) return ps;
		}
		return null;
	}

	public static Mapping[] merge(Mapping[] mappings1, Mapping[] mappings2) {
		Mapping[] mappings = new Mapping[mappings1.length + mappings2.length];
		for (int i = 0; i < mappings1.length; i++) {
			mappings[i] = mappings1[i];
		}
		for (int i = 0; i < mappings2.length; i++) {
			mappings[mappings1.length + i] = mappings2[i];
		}
		return mappings;
	}

	public static ApplicationListener getApplicationListener(PageContext pc) {
		PageSource ps = pc.getBasePageSource();
		if (ps != null) {
			MappingImpl mapp = (MappingImpl) ps.getMapping();
			if (mapp != null) return mapp.getApplicationListener();
		}
		return pc.getConfig().getApplicationListener();
	}

	public static String getCookieDomain(PageContext pc) {
		if (!pc.getApplicationContext().isSetDomainCookies()) return null;

		String result = Caster.toString(pc.cgiScope().get(KeyConstants._server_name, null), null);

		if (!StringUtil.isEmpty(result)) {

			String listLast = ListUtil.last(result, '.');
			if (!lucee.runtime.op.Decision.isNumber(listLast)) { // if it's numeric then must be IP address
				int numparts = 2;
				int listLen = ListUtil.len(result, '.', true);

				if (listLen > 2) {
					if (listLast.length() == 2 || !StringUtil.isAscii(listLast)) { // country TLD

						int tldMinus1 = ListUtil.getAt(result, '.', listLen - 1, true, "").length();

						if (tldMinus1 == 2 || tldMinus1 == 3) // domain is in country like, example.co.uk or example.org.il
							numparts++;
					}
				}

				if (listLen > numparts) result = result.substring(result.indexOf('.'));
				else if (listLen == numparts) result = "." + result;
			}
		}

		return result;
	}

	public static PageContext getPageContext(Config config, ServletConfig servletConfig, File contextRoot, String host, String scriptName, String queryString, Cookie[] cookies,
			Map<String, Object> headers, Map<String, String> parameters, Map<String, Object> attributes, OutputStream os, boolean register, long timeout, boolean ignoreScopes)
			throws ServletException {
		boolean callOnStart = ThreadLocalPageContext.callOnStart.get();
		try {
			ThreadLocalPageContext.callOnStart.set(false);

			if (contextRoot == null) contextRoot = new File(".");
			// Engine
			CFMLEngine engine = null;
			try {
				engine = CFMLEngineFactory.getInstance();
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			if (engine == null) throw new ServletException("there is no ServletContext");

			if (headers == null) headers = new HashMap<String, Object>();
			if (parameters == null) parameters = new HashMap<String, String>();
			if (attributes == null) attributes = new HashMap<String, Object>();

			// Request
			HttpServletRequest req = CreationImpl.getInstance(engine).createHttpServletRequest(contextRoot, host, scriptName, queryString, cookies, headers, parameters, attributes,
					null);

			// Response
			HttpServletResponse rsp = CreationImpl.getInstance(engine).createHttpServletResponse(os);

			if (config == null) config = ThreadLocalPageContext.getConfig();

			CFMLFactory factory = null;
			HttpServlet servlet;
			if (config instanceof ConfigWeb) {
				ConfigWeb cw = (ConfigWeb) config;
				factory = cw.getFactory();
				servlet = factory.getServlet();
			}
			else {
				if (servletConfig == null) {

					ServletConfig[] configs = engine.getServletConfigs();
					String rootDir = contextRoot.getAbsolutePath();

					for (ServletConfig conf: configs) {
						if (lucee.commons.io.SystemUtil.arePathsSame(rootDir, conf.getServletContext().getRealPath("/"))) {
							servletConfig = conf;
							break;
						}
					}

					if (servletConfig == null) servletConfig = configs[0];
				}

				factory = engine.getCFMLFactory(servletConfig, req);
				servlet = new HTTPServletImpl(servletConfig, servletConfig.getServletContext(), servletConfig.getServletName());
			}

			return factory.getLuceePageContext(servlet, req, rsp, null, false, -1, false, register, timeout, false, ignoreScopes);
		}
		finally {
			ThreadLocalPageContext.callOnStart.set(callOnStart);
		}
	}

	public static void releasePageContext(PageContext pc, boolean register) {
		if (pc != null) pc.getConfig().getFactory().releaseLuceePageContext(pc, register);
		ThreadLocalPageContext.register(null);
	}

	public static TimeSpan remainingTime(PageContext pc, boolean throwWhenAlreadyTimeout) throws RequestTimeoutException {
		long ms = pc.getRequestTimeout() - (System.currentTimeMillis() - pc.getStartTime());
		if (ms > 0) {
			if (ms < 5) {}
			else if (ms < 10) ms = ms - 1;
			else if (ms < 50) ms = ms - 5;
			else if (ms < 200) ms = ms - 10;
			else if (ms < 1000) ms = ms - 50;
			else ms = ms - 100;

			return TimeSpanImpl.fromMillis(ms);
		}

		if (throwWhenAlreadyTimeout) throw CFMLFactoryImpl.createRequestTimeoutException(pc);

		return TimeSpanImpl.fromMillis(0);
	}

	public static void checkRequestTimeout(PageContext pc) throws RequestTimeoutException {
		if (pc.getRequestTimeout() - (System.currentTimeMillis() - pc.getStartTime()) > 0) return;
		throw CFMLFactoryImpl.createRequestTimeoutException(pc);
	}

	public static String getHandlePageException(PageContextImpl pc, PageException pe) throws PageException {
		BodyContent bc = null;
		String str = null;
		try {
			bc = pc.pushBody();
			pc.handlePageException(pe, false);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		finally {
			if (bc != null) str = bc.getString();
			pc.popBody();
		}
		return str;
	}

	public static Object getFunction(PageContext pc, Object coll, Object[] args) throws PageException {
		return Caster.toFunction(coll).call(pc, args, true);
	}

	public static Object getFunctionWithNamedValues(PageContext pc, Object coll, Object[] args) throws PageException {
		return Caster.toFunction(coll).callWithNamedValues(pc, Caster.toFunctionValues(args), true);
	}
}