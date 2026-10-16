/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.commons.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lucee.commons.digest.Hash;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringList;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.ContentType;
import lucee.commons.lang.mimetype.MimeType;
import lucee.commons.net.header.HeadersCollection;
import lucee.commons.net.header.HeadersHTTPResponse;
import lucee.commons.net.header.HeadersHttpResponseApache;
import lucee.commons.net.header.HeadersHttpURLConnection;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageServletException;
import lucee.runtime.net.http.HTTPServletRequestWrap;
import lucee.runtime.net.http.HttpServletResponseWrap;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.tag.Http;
import lucee.runtime.type.util.ListUtil;

public final class HTTPUtil {

	public static final short ENCODED_AUTO = 1;
	public static final short ENCODED_YES = 2;
	public static final short ENCODED_NO = 3;

	/**
	 * Field <code>ACTION_POST</code>
	 */
	public static final short ACTION_POST = 0;

	/**
	 * Field <code>ACTION_GET</code>
	 */
	public static final short ACTION_GET = 1;

	/**
	 * Field <code>STATUS_OK</code>
	 */
	public static final int STATUS_OK = 200;
	// private static final String NO_MIMETYPE="Unable to determine MIME type of file.";

	public static final int MAX_REDIRECT = 15;

	private static String[] VALIDATION_HEADERS = new String[] { "Digest" };

	private static Map<String, String[]> headerNames = new ConcurrentHashMap<>();
	static {
		headerNames.put("md5", new String[] { "X-Checksum-MD5", "Content-MD5" });
		headerNames.put("sha1", new String[] { "X-Checksum-SHA1" });
		headerNames.put("sha256", new String[] { "X-Checksum-SHA256", "X-Checksum-SHA-256" });
		headerNames.put("sha512", new String[] { "X-Checksum-SHA512", "X-Checksum-SHA-512" });
	}

	/**
	 * cast a string to a url
	 * 
	 * @param strUrl string represent a url
	 * @return url from string
	 * @throws MalformedURLException
	 */
	public static URL toURL(String strUrl, short encodeOption) throws MalformedURLException {
		return toURL(strUrl, -1, encodeOption);
	}

	public static URL toURL(String strUrl, short encodeOption, URL defaultValue) {
		if (StringUtil.isEmpty(strUrl, true)) return defaultValue;
		try {
			return toURL(strUrl, -1, encodeOption);
		}
		catch (MalformedURLException e) {
			return defaultValue;
		}
	}

	public static String validateURL(String strUrl, String defaultValue) {
		try {
			return toURL(strUrl, -1, Http.ENCODED_AUTO).toExternalForm();
		}
		catch (MalformedURLException e) {
			return defaultValue;
		}
	}

	/**
	 * cast a string to a url
	 * 
	 * @param strUrl string represent a url
	 * @return url from string
	 * @throws MalformedURLException
	 */

	public static URL toURL(String strUrl, int port, short encodeOption) throws MalformedURLException {
		URL url;
		try {
			url = new URL(strUrl);
		}
		catch (MalformedURLException mue) {
			url = new URL("http://" + strUrl);
		}
		if (encodeOption == Http.ENCODED_NO) return url;
		return encodeURL(url, port, encodeOption == Http.ENCODED_AUTO);
	}

	public static URL encodeURL(URL url, boolean encodeWhenNecessary) throws MalformedURLException {
		return encodeURL(url, -1, encodeWhenNecessary);

	}

	public static URL encodeURL(URL url, int port, boolean encodeOnlyWhenNecessary) throws MalformedURLException {

		// file
		String path = url.getPath();
		// String file=url.getFile();
		String query = url.getQuery();
		String ref = url.getRef();
		String user = url.getUserInfo();
		if (port <= 0) port = url.getPort();

		// decode path
		if (!StringUtil.isEmpty(path)) {
			int sqIndex = path.indexOf(';');
			String q = null;
			if (sqIndex != -1) {
				q = path.substring(sqIndex + 1);
				path = path.substring(0, sqIndex);
			}

			StringBuilder res = new StringBuilder();

			StringList list = ListUtil.toListTrim(path, '/');
			String str;

			while (list.hasNext()) {
				str = list.next();
				// str=URLDecoder.decode(str);

				if (StringUtil.isEmpty(str)) continue;
				res.append("/");
				res.append(escapeQSValue(str, encodeOnlyWhenNecessary));
			}
			if (StringUtil.endsWith(path, '/')) res.append('/');
			path = res.toString();

			if (sqIndex != -1) {
				path += decodeQuery(q, ';');
			}
		}

		// decode query
		query = decodeQuery(query, '?');

		String file = path + query;

		// decode ref/anchor
		if (ref != null) {
			file += "#" + escapeQSValue(ref, encodeOnlyWhenNecessary);
		}

		// user/pasword
		if (!StringUtil.isEmpty(user)) {
			int index = user.indexOf(':');
			if (index != -1) {
				user = escapeQSValue(user.substring(0, index), encodeOnlyWhenNecessary) + ":" + escapeQSValue(user.substring(index + 1), encodeOnlyWhenNecessary);
			}
			else user = escapeQSValue(user, encodeOnlyWhenNecessary);

			String strUrl = getProtocol(url) + "://" + user + "@" + url.getHost();
			if (port > 0) strUrl += ":" + port;
			strUrl += file;
			return new URL(strUrl);
		}

		// port
		if (port <= 0) return new URL(url.getProtocol(), url.getHost(), file);
		return new URL(url.getProtocol(), url.getHost(), port, file);

	}

	private static String decodeQuery(String query, char startDelimiter) {
		if (!StringUtil.isEmpty(query)) {
			StringBuilder res = new StringBuilder();

			StringList list = ListUtil.toList(query, '&');
			String str;
			int index;
			char del = startDelimiter;
			while (list.hasNext()) {
				res.append(del);
				del = '&';
				str = list.next();
				index = str.indexOf('=');
				if (index == -1) res.append(escapeQSValue(str, true));
				else {
					res.append(escapeQSValue(str.substring(0, index), true));
					res.append('=');
					res.append(escapeQSValue(str.substring(index + 1), true));
				}
			}
			query = res.toString();
		}
		else query = "";
		return query;
	}

	public static URI toURI(String strUrl) throws URISyntaxException {
		return toURI(strUrl, -1);
	}

	public static URI toURI(String strUrl, int port) throws URISyntaxException {

		// print.o((strUrl));
		URI uri = new URI(strUrl);

		String host = uri.getHost();
		String fragment = uri.getRawFragment();
		String path = uri.getRawPath();
		String query = uri.getRawQuery();

		String scheme = uri.getScheme();
		String userInfo = uri.getRawUserInfo();
		if (port <= 0) port = uri.getPort();

		// decode path
		if (!StringUtil.isEmpty(path)) {

			int sqIndex = path.indexOf(';');
			String q = null;
			if (sqIndex != -1) {
				q = path.substring(sqIndex + 1);
				path = path.substring(0, sqIndex);
			}

			StringBuilder res = new StringBuilder();

			StringList list = ListUtil.toListTrim(path, '/');
			String str;

			while (list.hasNext()) {
				str = list.next();
				// str=URLDecoder.decode(str);

				if (StringUtil.isEmpty(str)) continue;
				res.append("/");
				res.append(escapeQSValue(str, true));
			}
			if (StringUtil.endsWith(path, '/')) res.append('/');
			path = res.toString();

			if (sqIndex != -1) {
				path += decodeQuery(q, ';');
			}
		}

		// decode query
		query = decodeQuery(query, '?');

		// decode ref/anchor
		if (!StringUtil.isEmpty(fragment)) {
			fragment = escapeQSValue(fragment, true);
		}

		// user/pasword
		if (!StringUtil.isEmpty(userInfo)) {
			int index = userInfo.indexOf(':');
			if (index != -1) {
				userInfo = escapeQSValue(userInfo.substring(0, index), true) + ":" + escapeQSValue(userInfo.substring(index + 1), true);
			}
			else userInfo = escapeQSValue(userInfo, true);
		}

		/*
		 * print.o("- fragment:"+fragment); print.o("- host:"+host); print.o("- path:"+path);
		 * print.o("- query:"+query); print.o("- scheme:"+scheme); print.o("- userInfo:"+userInfo);
		 * print.o("- port:"+port); print.o("- absolute:"+uri.isAbsolute());
		 * print.o("- opaque:"+uri.isOpaque());
		 */

		StringBuilder rtn = new StringBuilder();
		if (scheme != null) {
			rtn.append(scheme);
			rtn.append("://");
		}
		if (userInfo != null) {
			rtn.append(userInfo);
			rtn.append("@");
		}
		if (host != null) {
			rtn.append(host);
		}
		if (port > 0) {
			rtn.append(":");
			rtn.append(port);
		}
		if (path != null) {
			rtn.append(path);
		}
		if (query != null) {
			// rtn.append("?");
			rtn.append(query);
		}
		if (fragment != null) {
			rtn.append("#");
			rtn.append(fragment);
		}

		return new URI(rtn.toString());
	}

	/*
	 * private static String getProtocol(URI uri) { String p=uri.getRawSchemeSpecificPart(); if(p==null)
	 * return null; if(p.indexOf('/')==-1) return p; if(p.indexOf("https")!=-1) return "https";
	 * if(p.indexOf("http")!=-1) return "http"; return p; }
	 */

	private static String getProtocol(URL url) {
		String p = url.getProtocol().toLowerCase();
		if (p.indexOf('/') == -1) return p;
		if (p.indexOf("https") != -1) return "https";
		if (p.indexOf("http") != -1) return "http";
		return p;
	}

	public static String escapeQSValue(String str, boolean encodeOnlyWhenNecessary) {
		if (encodeOnlyWhenNecessary && !ReqRspUtil.needEncoding(str)) return str;
		PageContextImpl pc = (PageContextImpl) ThreadLocalPageContext.get();
		if (pc != null) {
			try {
				return URLEncoder.encode(str, pc.getWebCharset());
			}
			catch (UnsupportedEncodingException e) {
				LogUtil.warn("http-util", e);
			}
		}
		return URLEncoder.encode(str);
	}

	public static URL removeUnecessaryPort(URL url) {
		int port = url.getPort();
		if (port == 80 && url.getProtocol().equalsIgnoreCase("http")) port = -1;
		else if (port == 443 && url.getProtocol().equalsIgnoreCase("https")) port = -1;

		try {
			return new URL(url.getProtocol(), url.getHost(), port, url.getFile());
		}
		catch (MalformedURLException e) {
			return url;// this should never happen
		}
	}

	public static String removeUnecessaryPort(String url) throws MalformedURLException {
		return removeUnecessaryPort(new URL(url)).toExternalForm();
	}

	/*
	 * public static URL toURL(HttpMethod httpMethod) { HostConfiguration config =
	 * httpMethod.getHostConfiguration();
	 * 
	 * try { String qs = httpMethod.getQueryString(); if(StringUtil.isEmpty(qs)) return new
	 * URL(config.getProtocol().getScheme(),config.getHost(),config.getPort(),httpMethod.getPath());
	 * return new
	 * URL(config.getProtocol().getScheme(),config.getHost(),config.getPort(),httpMethod.getPath()+"?"+
	 * qs); } catch (MalformedURLException e) { return null; } }
	 */

	public static String optimizeRealPath(PageContext pc, String realPath) {
		int index;
		String requestURI = realPath, queryString = null;
		if ((index = realPath.indexOf('?')) != -1) {
			requestURI = realPath.substring(0, index);
			queryString = realPath.substring(index + 1);
		}
		PageSource ps = PageSourceImpl.best(((PageContextImpl) pc).getRelativePageSources(requestURI));
		requestURI = ps.getRealpathWithVirtual();
		if (queryString != null) return requestURI + "?" + queryString;
		return requestURI;
	}

	public static void forward(PageContext pc, String realPath) throws PageServletException, IOException {
		ServletContext context = pc.getServletContext();
		realPath = HTTPUtil.optimizeRealPath(pc, realPath);

		try {
			pc.getHttpServletRequest().setAttribute("lucee.forward.request_uri", realPath);

			RequestDispatcher disp = context.getRequestDispatcher(realPath);
			if (disp == null) throw new PageServletException(new ApplicationException("Page [" + realPath + "] not found"));

			// populateRequestAttributes();
			disp.forward(removeWrap(pc.getHttpServletRequest()), pc.getHttpServletResponse());
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw Caster.toPageServletException(e);
		}
		finally {
			ThreadLocalPageContext.register(pc);
		}
	}

	public static ServletRequest removeWrap(ServletRequest req) {
		while (req instanceof HTTPServletRequestWrap)
			return ((HTTPServletRequestWrap) req).getOriginalRequest();
		return req;
	}

	public static void include(PageContext pc, String realPath) throws PageServletException, IOException {
		include(pc, pc.getHttpServletRequest(), pc.getHttpServletResponse(), realPath);
	}

	public static void include(PageContext pc, ServletRequest req, ServletResponse rsp, String realPath) throws PageServletException, IOException {
		realPath = optimizeRealPath(pc, realPath);
		boolean inline = HttpServletResponseWrap.get();
		// print.out(rsp+":"+pc.getResponse());
		RequestDispatcher disp = getRequestDispatcher(pc, realPath);

		if (inline) {
			// RequestDispatcher disp = getRequestDispatcher(pc,realPath);
			try {
				disp.include(req, rsp);
			}
			catch (IOException e) {
				throw e;
			}
			catch (Exception e) {
				throw Caster.toPageServletException(e);
			}
			return;
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			HttpServletResponseWrap hsrw = new HttpServletResponseWrap(pc.getHttpServletResponse(), baos);
			HttpServletResponseWrap.set(true);

			// RequestDispatcher disp = getRequestDispatcher(pc,realPath);

			disp.include(req, hsrw);
			if (!hsrw.isCommitted()) hsrw.flushBuffer();
			pc.write(IOUtil.toString(baos.toByteArray(), ReqRspUtil.getCharacterEncoding(pc, hsrw)));
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw Caster.toPageServletException(e);
		}
		finally {
			HttpServletResponseWrap.release();
			ThreadLocalPageContext.register(pc);
		}
	}

	private static RequestDispatcher getRequestDispatcher(PageContext pc, String realPath) throws PageServletException {
		RequestDispatcher disp = pc.getServletContext().getRequestDispatcher(realPath);
		if (disp == null) throw new PageServletException(new ApplicationException("Page [" + realPath + "] not found"));
		return disp;
	}

	// MUST create a copy from toURL and rename toURI and rewrite for URI, perhaps it is possible to
	// merge them somehow
	public static String encode(String realpath) {

		int qIndex = realpath.indexOf('?');

		if (qIndex == -1) return realpath;

		String file = realpath.substring(0, qIndex);
		String query = realpath.substring(qIndex + 1);
		int sIndex = query.indexOf('#');

		String anker = null;
		if (sIndex != -1) {
			// print.o(sIndex);
			anker = query.substring(sIndex + 1);
			query = query.substring(0, sIndex);
		}

		StringBuilder res = new StringBuilder(file);

		// query
		if (!StringUtil.isEmpty(query)) {

			StringList list = ListUtil.toList(query, '&');
			String str;
			int index;
			char del = '?';
			while (list.hasNext()) {
				res.append(del);
				del = '&';
				str = list.next();
				index = str.indexOf('=');
				if (index == -1) res.append(escapeQSValue(str, true));
				else {
					res.append(escapeQSValue(str.substring(0, index), true));
					res.append('=');
					res.append(escapeQSValue(str.substring(index + 1), true));
				}
			}
		}

		// anker
		if (anker != null) {
			res.append('#');
			res.append(escapeQSValue(anker, true));
		}

		return res.toString();
	}

	public static int getPort(URL url) {
		if (url.getPort() != -1) return url.getPort();
		if ("https".equalsIgnoreCase(url.getProtocol())) return 443;
		return 80;
	}

	/**
	 * return the length of a file defined by a url.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static long length(URL url) throws IOException, GeneralSecurityException {
		return HTTPEngine.head(url).getContentLength();

	}

	/*
	 * public static ContentType getContentType(HttpMethod http) { Header[] headers =
	 * http.getResponseHeaders(); for(int i=0;i<headers.length;i++){
	 * if("Content-Type".equalsIgnoreCase(headers[i].getName())){ String[] mimeCharset =
	 * splitMimeTypeAndCharset(headers[i].getValue()); String[] typeSub =
	 * splitTypeAndSubType(mimeCharset[0]); return new
	 * ContentTypeImpl(typeSub[0],typeSub[1],mimeCharset[1]); } } return null; }
	 */

	public static Map<String, String> parseParameterList(String _str, boolean decode, String charset) {
		// return lucee.commons.net.HTTPUtil.toURI(strUrl,port);
		Map<String, String> data = new HashMap<String, String>();
		StringList list = ListUtil.toList(_str, '&');
		String str;
		int index;
		while (list.hasNext()) {
			str = list.next();
			index = str.indexOf('=');
			if (index == -1) {
				data.put(decode(str, decode), "");
			}
			else {
				data.put(decode(str.substring(0, index), decode), decode(str.substring(index + 1), decode));
			}
		}
		return data;
	}

	private static String decode(String str, boolean encode) {
		// TODO Auto-generated method stub
		return str;
	}

	public static ContentType toContentType(String str, ContentType defaultValue) {
		if (StringUtil.isEmpty(str, true)) return defaultValue;
		String[] types = str.split(";");
		ContentType ct = null;
		if (types.length > 0) {
			ct = new ContentType(types[0]);
			if (types.length > 1) {
				String tmp = types[types.length - 1].trim();
				int index = tmp.indexOf("charset=");
				if (index != -1) {
					ct.setCharset(StringUtil.removeQuotes(tmp.substring(index + 8), true));
				}
			}
		}
		return ct;
	}

	public static String[] splitMimeTypeAndCharset(String mimetype, String[] defaultValue) {
		if (StringUtil.isEmpty(mimetype, true)) return defaultValue;
		String[] types = mimetype.split(";");
		String[] rtn = new String[2];

		if (types.length > 0) {
			rtn[0] = types[0].trim();
			if (types.length > 1) {
				String tmp = types[types.length - 1].trim();
				int index = tmp.indexOf("charset=");
				if (index != -1) {
					rtn[1] = StringUtil.removeQuotes(tmp.substring(index + 8), true);
				}
			}
		}
		return rtn;
	}

	public static String[] splitTypeAndSubType(String mimetype) {
		String[] types = ListUtil.listToStringArray(mimetype, '/');
		String[] rtn = new String[2];

		if (types.length > 0) {
			rtn[0] = types[0].trim();
			if (types.length > 1) {
				rtn[1] = types[1].trim();
			}
		}
		return rtn;
	}

	public static Boolean isTextMimeType(String mimetype) {
		if (mimetype == null) mimetype = "";
		else mimetype = mimetype.trim().toLowerCase();

		if (StringUtil.startsWithIgnoreCase(mimetype, "audio/") || StringUtil.startsWithIgnoreCase(mimetype, "image/") || StringUtil.startsWithIgnoreCase(mimetype, "video/"))
			return false;

		if (StringUtil.startsWithIgnoreCase(mimetype, "text") || StringUtil.startsWithIgnoreCase(mimetype, "application/xml")
				|| StringUtil.startsWithIgnoreCase(mimetype, "application/atom+xml") || StringUtil.startsWithIgnoreCase(mimetype, "application/xhtml")
				|| StringUtil.startsWithIgnoreCase(mimetype, "application/json") || StringUtil.startsWithIgnoreCase(mimetype, "application/ld-json")
				|| StringUtil.startsWithIgnoreCase(mimetype, "application/cfml") || StringUtil.startsWithIgnoreCase(mimetype, "application/x-www-form-urlencoded")
				|| StringUtil.startsWithIgnoreCase(mimetype, "application/EDIFACT") || StringUtil.startsWithIgnoreCase(mimetype, "application/javascript")
				|| StringUtil.startsWithIgnoreCase(mimetype, "message") || StringUtil.indexOfIgnoreCase(mimetype, "xml") != -1
				|| StringUtil.indexOfIgnoreCase(mimetype, "json") != -1 || StringUtil.indexOfIgnoreCase(mimetype, "rss") != -1
				|| StringUtil.indexOfIgnoreCase(mimetype, "atom") != -1 || StringUtil.indexOfIgnoreCase(mimetype, "text") != -1)
			return true;

		return null;
	}

	public static Boolean isTextMimeType(MimeType mimetype) {
		if (mimetype == null) return false;
		if (MimeType.APPLICATION_JSON.same(mimetype)) return true;
		if (MimeType.APPLICATION_PLAIN.same(mimetype)) return true;
		if (MimeType.APPLICATION_CFML.same(mimetype)) return true;
		if (MimeType.APPLICATION_WDDX.same(mimetype)) return true;
		if (MimeType.APPLICATION_XML.same(mimetype)) return true;

		return isTextMimeType(mimetype.toString());
	}

	public static boolean isSecure(URL url) {
		return StringUtil.indexOfIgnoreCase(url.getProtocol(), "https") != -1;
	}

	public static void validateDownload(URL url, HttpURLConnection conn, Resource res, String checksum, boolean deleteFileWhenInvalid, Exception cause) throws IOException {
		_validateDownload(url, new HeadersHttpURLConnection(conn), res, checksum, deleteFileWhenInvalid, cause);
	}

	public static void validateDownload(URL url, HTTPResponse rsp, Resource res, String checksum, boolean deleteFileWhenInvalid, Exception cause) throws IOException {
		_validateDownload(url, new HeadersHTTPResponse(rsp), res, checksum, deleteFileWhenInvalid, cause);
	}

	public static void validateDownload(URL url, HttpResponse rsp, Resource res, String checksum, boolean deleteFileWhenInvalid, Exception cause) throws IOException {
		_validateDownload(url, new HeadersHttpResponseApache(rsp), res, checksum, deleteFileWhenInvalid, cause);
	}

	private static void _validateDownload(URL url, HeadersCollection headersCollection, Resource res, String checksum, boolean deleteFileWhenInvalid, Exception cause)
			throws IOException {
		// in case of an exception, the file may was not created
		if (cause != null && !res.exists()) return;
		Header[] headers;
		Header h;
		String label, name;
		// validate alogirtm specific headers
		for (Entry<String, String[]> e: headerNames.entrySet()) {
			name = e.getKey();
			label = name.toUpperCase();
			for (String hn: e.getValue()) {
				headers = headersCollection.getHeaders(hn);
				if (headers != null && headers.length > 0) {
					h = headers[0];
					if (!StringUtil.isEmpty(h.getValue(), true)) {

						String fileHash;
						if ("md5".equalsIgnoreCase(name)) fileHash = Hash.md5(res);
						else if ("sha1".equalsIgnoreCase(name)) fileHash = Hash.sha1(res);
						else if ("sha256".equalsIgnoreCase(name)) fileHash = Hash.sha256(res);
						else if ("sha512".equalsIgnoreCase(name)) fileHash = Hash.sha512(res);
						else continue;

						if (!fileHash.equalsIgnoreCase(h.getValue())) {
							if (deleteFileWhenInvalid) {
								res.remove(true);
							}
							IOException ioe = new IOException(String
									.format(label + " validation failed: The " + label + " hash in the response header [" + hn + " = " + h.getValue() + "] from the download ["
											+ url + "] does not match the computed " + label + " hash [" + fileHash + "] based on the downloaded file at [" + res
											+ "]. File integrity compromised" + (deleteFileWhenInvalid ? " and file got deleted" : "") + "."));
							if (cause != null) ExceptionUtil.initCauseEL(ioe, cause);
							throw ioe;
						}
						if (!StringUtil.isEmpty(checksum, true) && StringUtil.startsWithIgnoreCase(checksum, name + ":")
								&& StringUtil.endsWithIgnoreCase(url.toExternalForm(), ".jar") && !(name + "-" + fileHash).equalsIgnoreCase(checksum)) {

							IOException ioe = new IOException(String.format(label + " validation failed: The checksum defined in the dependency [" + checksum
									+ "] does not match the computed checksum [" + name + "-" + fileHash + "] based on the downloaded file [" + res
									+ "]. File integrity could get compromised" + (deleteFileWhenInvalid ? " and file got deleted" : "") + "."));
							if (cause != null) ExceptionUtil.initCauseEL(ioe, cause);
							throw ioe;

						}

						if (cause != null) throw ExceptionUtil.toIOException(cause);
						return;
					}
				}
			}
			if (cause != null) throw ExceptionUtil.toIOException(cause);
		}

		// Digest validation
		for (String hn: VALIDATION_HEADERS) {
			headers = headersCollection.getHeaders(hn);
			if (headers != null && headers.length > 0) {
				h = headers[0];
				String raw = h.getValue();
				if (!StringUtil.isEmpty(raw, true)) {
					// Split by commas for multiple algorithm/hash pairs
					String[] digestEntries = ListUtil.listToStringArray(raw, ',');
					for (String digestEntry: digestEntries) {
						String[] pair = ListUtil.listToStringArray(digestEntry, '=');
						if (pair.length == 2) {
							name = pair[0].trim().toLowerCase();
							label = name.toUpperCase();
							String expectedHash = pair[1].trim();
							String fileHash;

							switch (name) {
							case "md5":
							case "md-5":
								fileHash = Hash.md5(res);
								break;
							case "sha1":
							case "sha-1":
								fileHash = Hash.sha1(res);
								break;
							case "sha256":
							case "sha-256":
								fileHash = Hash.sha256(res);
								break;
							case "sha512":
							case "sha-512":
								fileHash = Hash.sha512(res);
								break;
							default:
								continue; // Unknown algorithm, skip it
							}

							if (!fileHash.equalsIgnoreCase(expectedHash)) {
								if (deleteFileWhenInvalid) {
									res.remove(true);
								}
								throw new IOException(String.format(
										"%s validation failed: The %s hash in the response header '%s' [%s] does not match the computed %s hash [%s] based on the downloaded file. File integrity might be compromised.",
										label, label, hn, expectedHash, label, fileHash));
							}
						}
					}
				}
			}
		}
	}
}
