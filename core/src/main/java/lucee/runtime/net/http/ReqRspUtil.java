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
package lucee.runtime.net.http;

import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.SOFT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.map.ReferenceMap;
import org.xml.sax.InputSource;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.MimeType;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.URLDecoder;
import lucee.commons.net.URLEncoder;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.converter.JavaConverter;
import lucee.runtime.converter.WDDXConverter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.decision.IsLocalHost;
import lucee.runtime.interpreter.CFMLExpressionInterpreter;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.security.ScriptProtect;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.CollectionUtil;

public final class ReqRspUtil {

	private static final Cookie[] EMPTY = new Cookie[0];
	private static Map<String, String> rootPathes = new ReferenceMap<String, String>(HARD, SOFT);

	public static String get(Pair<String, Object>[] items, String name) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].getName().equalsIgnoreCase(name)) return Caster.toString(items[i].getValue(), null);
		}
		return null;
	}

	public static Pair<String, Object>[] add(Pair<String, Object>[] items, String name, Object value) {
		Pair<String, Object>[] tmp = new Pair[items.length + 1];
		for (int i = 0; i < items.length; i++) {
			tmp[i] = items[i];
		}
		tmp[items.length] = new Pair<String, Object>(name, value);
		return tmp;
	}

	public static Pair<String, Object>[] set(Pair<String, Object>[] items, String name, Object value) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].getName().equalsIgnoreCase(name)) {
				items[i] = new Pair<String, Object>(name, value);
				return items;
			}
		}
		return add(items, name, value);
	}

	/**
	 * return path to itself
	 * 
	 * @param req
	 */
	public static String self(HttpServletRequest req) {
		StringBuffer sb = new StringBuffer(req.getServletPath());
		String qs = req.getQueryString();
		if (!StringUtil.isEmpty(qs)) sb.append('?').append(qs);
		return sb.toString();
	}

	public static void setContentLength(HttpServletResponse rsp, int length) {
		rsp.setContentLength(length);
	}

	public static void setContentLength(HttpServletResponse rsp, long length) {
		if (length <= Integer.MAX_VALUE) {
			setContentLength(rsp, (int) length);
		}
		else {
			rsp.addHeader("Content-Length", Caster.toString(length));
		}
	}

	public static void setContentType(HttpServletResponse rsp, String contentType) {
		rsp.setContentType(contentType);
	}

	public static Cookie[] getCookies(HttpServletRequest req, Charset charset) {
		Cookie[] cookies = req.getCookies();

		if (cookies != null) {
			Cookie cookie;
			String tmp;
			for (int i = 0; i < cookies.length; i++) {
				cookie = cookies[i];
				// value (is decoded by the servlet engine with iso-8859-1)
				if (!StringUtil.isAscii(cookie.getValue())) {
					tmp = encode(cookie.getValue(), "iso-8859-1");
					cookie.setValue(decode(tmp, charset.name(), false));
				}
			}
		}
		else {

			String str = req.getHeader("Cookie");
			if (str != null) {
				try {
					String[] arr = lucee.runtime.type.util.ListUtil.listToStringArray(str, ';'), tmp;
					java.util.List<Cookie> list = new ArrayList<Cookie>();
					Cookie c;
					for (int i = 0; i < arr.length; i++) {
						tmp = lucee.runtime.type.util.ListUtil.listToStringArray(arr[i], '=');
						if (tmp.length > 0) {
							c = ReqRspUtil.toCookie(dec(tmp[0], charset.name(), false), tmp.length > 1 ? dec(tmp[1], charset.name(), false) : "", null);
							if (c != null) list.add(c);
						}
					}

					cookies = list.toArray(new Cookie[list.size()]);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
		}

		if (cookies == null) return EMPTY;

		return cookies;
	}

	public static void setCharacterEncoding(HttpServletResponse rsp, String charset) {
		try {
			Method setCharacterEncoding = rsp.getClass().getMethod("setCharacterEncoding", new Class[0]);
			setCharacterEncoding.invoke(rsp, new Object[0]);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw ExceptionUtil.toRuntimeException(t);
		}
	}

	public static String getQueryString(HttpServletRequest req) {
		// String qs = req.getAttribute("javax.servlet.include.query_string");
		return req.getQueryString();
	}

	public static String getHeader(HttpServletRequest request, String name, String defaultValue) {
		try {
			return request.getHeader(name);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	public static String getHeaderIgnoreCase(PageContext pc, String name, String defaultValue) {
		String charset = pc.getWebCharset().name();
		HttpServletRequest req = pc.getHttpServletRequest();
		Enumeration e = req.getHeaderNames();
		String keyDecoded, key;
		while (e.hasMoreElements()) {
			key = e.nextElement().toString();
			keyDecoded = ReqRspUtil.decode(key, charset, false);
			if (name.equalsIgnoreCase(key) || name.equalsIgnoreCase(keyDecoded)) return ReqRspUtil.decode(req.getHeader(key), charset, false);
		}
		return defaultValue;
	}

	public static List<String> getHeadersIgnoreCase(PageContext pc, String name) {

		String charset = pc.getWebCharset().name();
		HttpServletRequest req = pc.getHttpServletRequest();
		Enumeration e = req.getHeaderNames();
		List<String> rtn = new ArrayList<String>();
		String keyDecoded, key;

		while (e.hasMoreElements()) {
			key = e.nextElement().toString();
			keyDecoded = ReqRspUtil.decode(key, charset, false);
			if (name.equalsIgnoreCase(key) || name.equalsIgnoreCase(keyDecoded)) rtn.add(ReqRspUtil.decode(req.getHeader(key), charset, false));
		}

		return rtn;
	}

	public static String getScriptName(PageContext pc, HttpServletRequest req) {

		String sn = StringUtil.emptyIfNull(req.getContextPath()) + StringUtil.emptyIfNull(req.getServletPath());

		if (pc == null) pc = ThreadLocalPageContext.get();

		if (pc != null && ((pc.getApplicationContext().getScriptProtect() & ApplicationContext.SCRIPT_PROTECT_URL) > 0
				|| (pc.getApplicationContext().getScriptProtect() & ApplicationContext.SCRIPT_PROTECT_CGI) > 0)) {
			sn = ScriptProtect.translate(sn);
		}

		return sn;
	}

	private static boolean isHex(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	private static String dec(String str, String charset, boolean force) {

		str = str.trim();

		if (StringUtil.startsWith(str, '"') && StringUtil.endsWith(str, '"') && str.length() > 1) str = str.substring(1, str.length() - 1);

		return decode(str, charset, force);// java.net.URLDecoder.decode(str.trim(), charset);
	}

	public static String decode(String str, String charset, boolean force) {

		try {
			if (str == null) return null;

			return URLDecoder.decode(str, charset, force);
		}
		catch (UnsupportedEncodingException e) {
			return str;
		}
	}

	public static String encode(String str, String charset) {
		try {
			return URLEncoder.encode(str, charset);
		}
		catch (UnsupportedEncodingException e) {
			return str;
		}
	}

	public static String encode(String str, Charset charset) {
		try {
			return URLEncoder.encode(str, charset);
		}
		catch (UnsupportedEncodingException e) {
			return str;
		}
	}

	public static boolean needEncoding(String str, boolean allowPlus) {
		if (StringUtil.isEmpty(str, false)) return false;

		int len = str.length();
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (c >= '0' && c <= '9') continue;
			if (c >= 'a' && c <= 'z') continue;
			if (c >= 'A' && c <= 'Z') continue;

			// _-.*
			if (c == '-') continue;
			if (c == '_') continue;
			if (c == '.') continue;
			if (c == '*') continue;
			if (c == '/') continue;
			if (allowPlus && c == '+') continue;

			if (c == '%') {
				if (i + 2 >= len) return true;
				try {
					char c1 = str.charAt(i + 1);
					char c2 = str.charAt(i + 2);
					if (!isHex(c1) || !isHex(c2)) return true;
					Integer.parseInt(c1 + "" + c2, 16);
				}
				catch (NumberFormatException nfe) {
					return true;
				}
				i += 3;
				continue;
			}
			return true;
		}
		return false;
	}

	public static boolean needDecoding(String str) {
		if (StringUtil.isEmpty(str, false)) return false;

		boolean need = false;
		int len = str.length();
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (c >= '0' && c <= '9') continue;
			if (c >= 'a' && c <= 'z') continue;
			if (c >= 'A' && c <= 'Z') continue;

			// _-.*
			if (c == '-') continue;
			if (c == '_') continue;
			if (c == '.') continue;
			if (c == '*') continue;
			if (c == '+') {
				need = true;
				continue;
			}

			if (c == '%') {
				if (i + 2 >= len) return false;
				try {
					Integer.parseInt(str.substring(i + 1, i + 3), 16);
				}
				catch (NumberFormatException nfe) {
					return false;
				}
				i += 3;
				need = true;
				continue;
			}
			return false;
		}
		return need;
	}

	public static boolean isThis(HttpServletRequest req, String url) {
		try {
			return isThis(req, HTTPUtil.toURL(url, HTTPUtil.ENCODED_AUTO));
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return false;
		}
	}

	public static boolean isThis(HttpServletRequest req, URL url) {
		try {
			// Port
			int reqPort = req.getServerPort();
			int urlPort = url.getPort();
			if (urlPort <= 0) urlPort = HTTPUtil.isSecure(url) ? 443 : 80;
			if (reqPort <= 0) reqPort = req.isSecure() ? 443 : 80;
			if (reqPort != urlPort) return false;

			// host
			String reqHost = req.getServerName();
			String urlHost = url.getHost();
			if (reqHost.equalsIgnoreCase(urlHost)) return true;
			if (IsLocalHost.invoke(reqHost) && IsLocalHost.invoke(reqHost)) return true;

			InetAddress urlAddr = InetAddress.getByName(urlHost);

			InetAddress reqAddr = InetAddress.getByName(reqHost);
			if (reqAddr.getHostName().equalsIgnoreCase(urlAddr.getHostName())) return true;
			if (reqAddr.getHostAddress().equalsIgnoreCase(urlAddr.getHostAddress())) return true;

			reqAddr = InetAddress.getByName(req.getRemoteAddr());
			if (reqAddr.getHostName().equalsIgnoreCase(urlAddr.getHostName())) return true;
			if (reqAddr.getHostAddress().equalsIgnoreCase(urlAddr.getHostAddress())) return true;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return false;
	}

	public static LinkedList<MimeType> getAccept(PageContext pc) {
		LinkedList<MimeType> accept = new LinkedList<MimeType>();
		java.util.Iterator<String> it = ReqRspUtil.getHeadersIgnoreCase(pc, "accept").iterator();
		String value;
		while (it.hasNext()) {
			value = it.next();
			MimeType[] mtes = MimeType.getInstances(value, ',');
			if (mtes != null) for (int i = 0; i < mtes.length; i++) {
				accept.add(mtes[i]);
			}
		}
		return accept;
	}

	public static MimeType getContentType(PageContext pc) {
		java.util.Iterator<String> it = ReqRspUtil.getHeadersIgnoreCase(pc, "content-type").iterator();
		String value;
		MimeType rtn = null;
		while (it.hasNext()) {
			value = it.next();
			MimeType[] mtes = MimeType.getInstances(value, ',');
			if (mtes != null) for (int i = 0; i < mtes.length; i++) {
				rtn = mtes[i];
			}
		}
		if (rtn == null) return MimeType.ALL;
		return rtn;
	}

	public static String getContentTypeAsString(PageContext pc, String defaultValue) {
		MimeType mt = getContentType(pc);
		if (mt == MimeType.ALL) return defaultValue;
		return mt.toString();
	}

	/**
	 * returns the body of the request
	 * 
	 * @param pc
	 * @param deserialized if true lucee tries to deserialize the body based on the content-type, for
	 *            example when the content type is "application/json"
	 * @param defaultValue value returned if there is no body
	 * @return
	 */
	public static Object getRequestBody(PageContext pc, boolean deserialized, Object defaultValue) {

		HttpServletRequest req = pc.getHttpServletRequest();

		MimeType contentType = getContentType(pc);
		String strContentType = contentType == MimeType.ALL ? null : contentType.toString();
		Charset cs = getCharacterEncoding(pc, req);

		boolean isBinary = !(strContentType == null || HTTPUtil.isTextMimeType(contentType) == Boolean.TRUE
				|| strContentType.toLowerCase().startsWith("application/x-www-form-urlencoded"));

		if (req.getContentLength() > -1) {
			ServletInputStream is = null;
			try {
				byte[] data = IOUtil.toBytes(is = req.getInputStream());// new byte[req.getContentLength()];
				Object obj = CollectionUtil.NULL;

				if (deserialized) {
					int format = MimeType.toFormat(contentType, -1);
					obj = toObject(pc, data, format, cs, obj);
				}
				if (obj == CollectionUtil.NULL) {
					if (isBinary) obj = data;
					else obj = toString(data, cs);
				}

				return obj;
			}
			catch (Exception e) {
				pc.getConfig().getLog("application").error("request", e);
				return defaultValue;
			}
			finally {
				try {
					IOUtil.close(is);
				}
				catch (IOException e) {
					pc.getConfig().getLog("application").error("request", e);
				}
			}
		}

		return defaultValue;
	}

	private static String toString(byte[] data, Charset cs) {
		if (cs != null) return new String(data, cs).trim();
		return new String(data).trim();
	}

	/**
	 * returns the full request URL
	 *
	 * @param req - the HttpServletRequest
	 * @param includeQueryString - if true, the QueryString will be appended if one exists
	 */
	public static String getRequestURL(HttpServletRequest req, boolean includeQueryString) {

		StringBuffer sb = req.getRequestURL();
		int maxpos = sb.indexOf("/", 8);

		if (maxpos > -1) {

			if (req.isSecure()) {
				if (sb.substring(maxpos - 4, maxpos).equals(":443")) sb.delete(maxpos - 4, maxpos);
			}
			else {
				if (sb.substring(maxpos - 3, maxpos).equals(":80")) sb.delete(maxpos - 3, maxpos);
			}

			if (includeQueryString && !StringUtil.isEmpty(req.getQueryString())) sb.append('?').append(req.getQueryString());
		}

		return sb.toString();
	}

	public static String getRootPath(ServletContext sc) {

		if (sc == null) throw new RuntimeException("cannot determinate webcontext root, because the ServletContext is null");
		String id = new StringBuilder().append(sc.getContextPath()).append(':').append(sc.hashCode()).toString();
		String root = rootPathes.get(id);
		if (!StringUtil.isEmpty(root, true)) return root;

		root = sc.getRealPath("/");
		if (root == null) throw new RuntimeException("cannot determinate webcontext root, the ServletContext from class [" + sc.getClass().getName()
				+ "] is returning null for the method call sc.getRealPath(\"/\"), possibly due to configuration problem.");

		try {
			root = new File(root).getCanonicalPath();
		}
		catch (IOException e) {
		}
		rootPathes.put(id, root);
		return root;
	}

	public static Object toObject(PageContext pc, byte[] data, int format, Charset charset, Object defaultValue) {

		switch (format) {
		case UDF.RETURN_FORMAT_JSON:
			try {
				return new JSONExpressionInterpreter().interpret(pc, toString(data, charset));
			}
			catch (PageException pe) {
			}
			break;
		case UDF.RETURN_FORMAT_SERIALIZE:
			try {
				return new CFMLExpressionInterpreter().interpret(pc, toString(data, charset));
			}
			catch (PageException pe) {
			}
			break;
		case UDF.RETURN_FORMAT_WDDX:
			try {
				WDDXConverter converter = new WDDXConverter(pc.getTimeZone(), false, true);
				converter.setTimeZone(pc.getTimeZone());
				return converter.deserialize(toString(data, charset), false);
			}
			catch (Exception pe) {
			}
			break;
		case UDF.RETURN_FORMAT_XML:
			try {
				InputSource xml = XMLUtil.toInputSource(pc, toString(data, charset));
				InputSource validator = null;
				return XMLCaster.toXMLStruct(XMLUtil.parse(xml, validator, false), true);
			}
			catch (Exception pe) {
			}
			break;
		case UDF.RETURN_FORMAT_JAVA:
			try {
				return JavaConverter.deserialize(new ByteArrayInputStream(data));
			}
			catch (Exception pe) {
			}
			break;
		}

		return defaultValue;
	}

	public static boolean identical(HttpServletRequest left, HttpServletRequest right) {

		if (left == right) return true;
		if (left instanceof HTTPServletRequestWrap) left = ((HTTPServletRequestWrap) left).getOriginalRequest();
		if (right instanceof HTTPServletRequestWrap) right = ((HTTPServletRequestWrap) right).getOriginalRequest();
		if (left == right) return true;
		return false;
	}

	public static Charset getCharacterEncoding(PageContext pc, ServletRequest req) {
		return _getCharacterEncoding(pc, req.getCharacterEncoding());
	}

	public static Charset getCharacterEncoding(PageContext pc, ServletResponse rsp) {
		return _getCharacterEncoding(pc, rsp.getCharacterEncoding());
	}

	private static Charset _getCharacterEncoding(PageContext pc, String ce) {

		if (!StringUtil.isEmpty(ce, true)) {
			Charset c = CharsetUtil.toCharset(ce, null);
			if (c != null) return c;
		}

		pc = ThreadLocalPageContext.get(pc);
		if (pc != null) return pc.getWebCharset();
		Config config = ThreadLocalPageContext.getConfig(pc);
		return config.getWebCharset();
	}

	public static void removeCookie(HttpServletResponse rsp, String name) {
		javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(name, "");
		cookie.setMaxAge(0);
		cookie.setSecure(false);
		cookie.setPath("/");
		rsp.addCookie(cookie);
	}

	/**
	 * if encodings fails the given url is returned
	 * 
	 * @param rsp
	 * @param url
	 * @return
	 */
	public static String encodeRedirectURLEL(HttpServletResponse rsp, String url) {
		try {
			return rsp.encodeRedirectURL(url);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return url;
		}
	}

	public static String getDomain(HttpServletRequest req) { // DIFF 23
		StringBuilder sb = new StringBuilder();
		sb.append(req.isSecure() ? "https://" : "http://");
		sb.append(req.getServerName());
		sb.append(':');
		sb.append(req.getServerPort());
		if (!StringUtil.isEmpty(req.getContextPath())) sb.append(req.getContextPath());
		return sb.toString();
	}

	public static Cookie toCookie(String name, String value, Cookie defaultValue) {
		try {
			return new Cookie(name, value);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}
}