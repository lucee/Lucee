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
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.tagext.BodyContent;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngineWrapper;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.CIPage;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageServletException;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.CreationImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.servlet.http.HTTPServletImpl;

public final class PageContextUtil {

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
			throws PageServletException {
		boolean callOnStart = ThreadLocalPageContext.callOnStart.get();
		try {
			ThreadLocalPageContext.callOnStart.set(false);
			if (config == null) config = ThreadLocalPageContext.getConfig();

			if (contextRoot == null) {
				if (config instanceof ConfigWeb) {
					try {
						contextRoot = ResourceUtil.toFile(config.getRootDirectory());
					}
					catch (IOException e) {
						LogUtil.log("pagecontext-loading", e);
					}
				}
				if (contextRoot == null) contextRoot = new File(".");
			}
			// Engine
			CFMLEngine engine = null;
			try {
				engine = CFMLEngineFactory.getInstance();
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			if (engine == null) throw new PageServletException(new ApplicationException("there is no ServletContext"));

			if (headers == null) headers = new HashMap<String, Object>();
			if (parameters == null) parameters = new HashMap<String, String>();
			if (attributes == null) attributes = new HashMap<String, Object>();

			// Request
			HttpServletRequest req = CreationImpl.getInstance(engine).createHttpServletRequest(contextRoot, host, scriptName, queryString, cookies, headers, parameters, attributes,
					null);

			// Response

			HttpServletResponse rsp = CreationImpl.getInstance(engine).createHttpServletResponse(os == null ? DevNullOutputStream.DEV_NULL_OUTPUT_STREAM : os);

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
						if (lucee.commons.io.SystemUtil.arePathsSame(rootDir, ReqRspUtil.getRootPath(conf.getServletContext()))) {
							servletConfig = conf;
							break;
						}
					}

					if (servletConfig == null) servletConfig = configs[0];
				}
				CFMLEngine e = engine;
				if (engine instanceof CFMLEngineWrapper) {
					e = ((CFMLEngineWrapper) engine).getEngine();
				}
				if (e instanceof CFMLEngineImpl && config instanceof ConfigServerImpl) factory = ((CFMLEngineImpl) e).getCFMLFactory((ConfigServerImpl) config, servletConfig, req);
				else {
					try {
						factory = e.getCFMLFactory(servletConfig, req);
					}
					catch (Exception se) {
						throw Caster.toPageServletException(se);
					}
				}

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
		// Account for debugger suspend time when calculating remaining timeout
		long suspendedMillis = (pc instanceof PageContextImpl) ? ((PageContextImpl) pc).getDebuggerTotalSuspendedMillis() : 0;
		long ms = pc.getRequestTimeout() - (System.currentTimeMillis() - pc.getStartTime() - suspendedMillis);
		if (ms > 0) {
			if (ms < 5) {}
			else if (ms < 10) ms = ms - 1;
			else if (ms < 50) ms = ms - 5;
			else if (ms < 200) ms = ms - 10;
			else if (ms < 1000) ms = ms - 50;
			else ms = ms - 100;

			return TimeSpanImpl.fromMillis(ms);
		}

		if (throwWhenAlreadyTimeout && allowRequestTimeout(pc) && ((PageContextImpl) pc).getTimeoutStackTrace() == null) throw CFMLFactoryImpl.createRequestTimeoutException(pc);

		return TimeSpanImpl.fromMillis(0);
	}

	public static void checkRequestTimeout(PageContext pc) throws RequestTimeoutException {
		// Account for debugger suspend time when checking timeout
		long suspendedMillis = (pc instanceof PageContextImpl) ? ((PageContextImpl) pc).getDebuggerTotalSuspendedMillis() : 0;
		if ((pc.getRequestTimeout() - (System.currentTimeMillis() - pc.getStartTime() - suspendedMillis) > 0) || ((PageContextImpl) pc).getTimeoutStackTrace() != null) return;
		if (allowRequestTimeout(pc)) throw CFMLFactoryImpl.createRequestTimeoutException(pc);
	}

	private static boolean allowRequestTimeout(PageContext pc) {
		if (!ThreadLocalPageContext.getConfig(pc).allowRequestTimeout()) return false;
		CFMLFactoryImpl factory = (CFMLFactoryImpl) pc.getConfig().getFactory();
		return factory.reachedConcurrentReqThreshold() && factory.reachedCPUThreshold() && factory.reachedMemoryThreshold();
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

	public static boolean hasDebugOptions(PageContext pc, int option) {
		pc = ThreadLocalPageContext.get(pc);
		if (pc instanceof PageContextImpl) return ((PageContextImpl) pc).hasDebugOptions(option);
		Config c = ThreadLocalPageContext.getConfig();
		if (c instanceof ConfigPro) return ((ConfigPro) c).hasDebugOptions(option);
		return false;
	}

	public static boolean debug(PageContext pc) {
		if (pc != null) return ((PageContextImpl) pc).getDebugOptions() > 0;
		return false;
	}

	public static boolean show(PageContext pc) {
		if (pc != null) return ((PageContextImpl) pc).show();
		return false;
	}

	public static lucee.runtime.Component loadInline(PageContext pc, String realPath, String inlineName) throws PageException {
		PageSource ps = PageSourceImpl.best(((PageContextImpl) pc).getRelativePageSources(realPath));
		String className = ps.getClassName();
		if (className.endsWith(Constants.CFML_CLASS_SUFFIX)) {
			className = className.substring(0, className.length() - Constants.CFML_CLASS_SUFFIX.length()) + "$" + inlineName + Constants.CFML_CLASS_SUFFIX;
			try {
				return ComponentLoader.loadInline((CIPage) ClassUtil.loadInstance(ps.loadPage(pc, false).getClass().getClassLoader(), className, new Object[] { ps }), pc);
				// return ComponentLoader.loadInline((CIPage)
				// ClassUtil.loadInstance(ps.getMapping().getPhysicalClass(className), new Object[] { ps }), this);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		throw new ApplicationException("unable to load inline component [" + inlineName + "] from [" + realPath + "]");
	}

	/**
	 * Decodes a base64-encoded bitmap of executable line numbers back to an int array. Used by
	 * generated Page classes to return executable lines without hitting bytecode limits.
	 *
	 * @param encoded base64-encoded bitmap where each bit represents whether that line is executable
	 * @param maxLine the highest line number in the bitmap
	 * @return array of executable line numbers
	 */
	public static int[] decodeExecutableLines(String encoded, int maxLine) {
		byte[] bitmap = Base64.getDecoder().decode(encoded);
		List<Integer> lines = new ArrayList<>();
		for (int line = 1; line <= maxLine; line++) {
			int byteIndex = (line - 1) / 8;
			int bitIndex = (line - 1) % 8;
			if (byteIndex < bitmap.length && (bitmap[byteIndex] & (1 << bitIndex)) != 0) {
				lines.add(line);
			}
		}
		int[] result = new int[lines.size()];
		for (int i = 0; i < lines.size(); i++) {
			result[i] = lines.get(i);
		}
		return result;
	}

	/**
	 * Encodes an array of executable line numbers as a base64 bitmap. Used at compile time to generate
	 * compact representation of executable lines.
	 *
	 * @param lines array of executable line numbers
	 * @return base64-encoded bitmap, or null if lines is empty
	 */
	public static String encodeExecutableLines(int[] lines) {
		if (lines == null || lines.length == 0) {
			return null;
		}
		int maxLine = 0;
		for (int line: lines) {
			if (line > maxLine) maxLine = line;
		}
		byte[] bitmap = new byte[(maxLine + 7) / 8];
		for (int line: lines) {
			int byteIndex = (line - 1) / 8;
			int bitIndex = (line - 1) % 8;
			bitmap[byteIndex] |= (1 << bitIndex);
		}
		return Base64.getEncoder().encodeToString(bitmap);
	}

	/**
	 * Gets the maximum line number from an array of line numbers. Used at compile time alongside
	 * encodeExecutableLines.
	 *
	 * @param lines array of line numbers
	 * @return the maximum line number, or 0 if empty
	 */
	public static int getMaxLine(int[] lines) {
		if (lines == null || lines.length == 0) return 0;
		int max = 0;
		for (int line: lines) {
			if (line > max) max = line;
		}
		return max;
	}

	public static PageContext createPageContext() throws PageException {
		try {
			return CFMLEngineFactory.getInstance().createPageContext(null, "getThreadPageContext:boolean", null, null, null, null, null, null, null, -1L, true);
		}
		catch (ServletException e) {
			throw Caster.toPageException(e);
		}
	}

	public static void popBody(PageContext pc) {
		pc.popBody();
	}
}