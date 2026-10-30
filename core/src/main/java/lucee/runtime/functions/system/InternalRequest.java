package lucee.runtime.functions.system;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CharsetX;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.ContentType;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.URLDecoder;
import lucee.commons.net.URLEncoder;
import lucee.commons.net.URLItem;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.RequestTimeoutException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.other.CreatePageContext;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.SessionCookieData;
import lucee.runtime.listener.SessionCookieDataImpl;
import lucee.runtime.net.http.HTTPServletRequestWrap;
import lucee.runtime.net.http.HttpServletRequestDummy;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.HttpSessionDummy;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.tag.Http;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.ArgumentImpl;
import lucee.runtime.type.scope.CookieImpl;
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.scope.FormImpl;
import lucee.runtime.type.scope.UrlFormImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.PageContextUtil;

public class InternalRequest implements Function {

	private static final long serialVersionUID = -8163856691035353577L;

	public static boolean cookieAsQuery = false;

	private static final Key CONTENT_TYPE = KeyImpl.getInstance("content-type");
	private static final Key CONTENT_LENGTH = KeyImpl.getInstance("content-length");
	private static final List<String> methods = Arrays.asList(new String[] { "GET", "POST", "HEAD", "PUT", "DELETE", "OPTIONS", "TRACE", "PATCH", "QUERY" });

	public static Struct call(final PageContext pc, String template, String method, Object oUrls, Object oForms, Struct cookies, Struct headers, Object body, String strCharset,
			boolean addToken, boolean throwonerror) throws PageException {
		method = method.toUpperCase().trim();
		template = resolveTemplate(pc, template);
		if (methods.indexOf(method) < 0) throw new FunctionException(pc, "_InternalRequest", 2, "method",
				"invalid method type [" + method + "], valid types are [" + ListUtil.arrayToList(methods.toArray(new String[0]), ",") + "]");
		Struct urls = toStruct(oUrls);
		Struct forms = toStruct(oForms);

		// add token
		if (addToken) {
			// if(true) throw new ApplicationException("addtoken==true");
			if (cookies == null) cookies = new StructImpl(4);

			cookies.set(KeyConstants._cfid, pc.getCFID());
			cookies.set(KeyConstants._cftoken, pc.getCFToken());
			String jsessionid = pc.getJSessionId();
			if (jsessionid != null) cookies.set(KeyConstants._jsessionid, jsessionid);
		}

		// charset
		Charset reqCharset = StringUtil.isEmpty(strCharset) ? pc.getWebCharset() : CharsetUtil.toCharset(strCharset);

		String ext = ResourceUtil.getExtension(template, null);
		// welcome files
		if (StringUtil.isEmpty(ext)) {
			throw new FunctionException(pc, "InternalRequest", 1, "template", "template path is invalid");
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] _barr = null;

		if (Decision.isBinary(body)) _barr = Caster.toBinary(body);
		else if (body != null) {
			Charset cs = null;
			// get charset
			if (headers != null) {
				String strCT = Caster.toString(headers.get(CONTENT_TYPE, null), null);
				if (strCT != null) {
					ContentType ct = HTTPUtil.toContentType(strCT, null);
					if (ct != null) {
						String strCS = ct.getCharset();
						if (!StringUtil.isEmpty(strCS)) cs = CharsetUtil.toCharsetX(strCS, CharsetX.UTF8).toCharset();
					}
				}
			}
			if (cs == null) cs = StandardCharsets.UTF_8;

			String str = Caster.toString(body);
			_barr = str.getBytes(cs);
		}

		PageContextImpl _pc = createPageContext(pc, template, urls, cookies, headers, _barr, reqCharset, baos, method);
		fillForm(_pc, forms, reqCharset);
		Collection request, session = null;
		int status;
		long exeTime;
		boolean isText = false;
		Charset _charset = null;
		PageException pe = null;
		Object rspCookies = cookieAsQuery
				? new QueryImpl(new String[] { "name", "value", "path", "domain", "expires", "secure", "httpOnly", "samesite", "partitioned" }, 0, "cookies")
				: new StructImpl(Struct.TYPE_LINKED);

		try {

			_pc.executeCFML(template, true, false);
			HttpSession s;
			if (_pc.getSessionType() == Config.SESSION_TYPE_JEE && (s = _pc.getHttpServletRequest().getSession(false)) != null) setSessionIdCookie(_pc, s.getId());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (!Abort.isSilentAbort(t)) {
				if (throwonerror) {
					// wrap RequestTimeoutException so it doesn't kill the parent request
					if (t instanceof RequestTimeoutException) {
						ApplicationException ae = new ApplicationException(t.getMessage());
						ae.initCause(t);
						throw ae;
					}
					throw Caster.toPageException(t);
				}
				pe = Caster.toPageException(t);
			}
		}
		finally {
			_pc.flush();
			// cookie = _pc.cookieScope().duplicate(false);
			request = _pc.requestScope().duplicate(false);
			session = _pc.hasCFSession() ? _pc.sessionScope().duplicate(false) : null;
			exeTime = System.currentTimeMillis() - pc.getStartTime();
			// debugging=_pc.getDebugger().getDebuggingData(_pc).duplicate(false);

			HttpServletResponseDummy rsp = (HttpServletResponseDummy) _pc.getHttpServletResponse();

			// headers
			Collection.Key name;
			headers = new StructImpl();
			Iterator<String> it = rsp.getHeaderNames().iterator();
			java.util.Collection<String> values;
			while (it.hasNext()) {
				name = KeyImpl.init(it.next());
				values = rsp.getHeaders(name.getString());
				if (values == null || values.size() == 0) continue;
				if (name.equals(KeyImpl.getInstance("Set-Cookie"))) {
					String cs = _pc.getWebCharset().name();
					for (String v: values) {

						if (cookieAsQuery) Http.parseCookie((Query) rspCookies, v, cs);
						else Http.parseCookie((Struct) rspCookies, v, cs);
					}

				}

				if (values.size() > 1) headers.set(name, Caster.toArray(values));
				else headers.set(name, values.iterator().next());
			}

			// content type and length
			headers.set(CONTENT_TYPE, rsp.getContentType());
			if (rsp.getContentLength() != -1) headers.set(CONTENT_LENGTH, rsp.getContentLength());

			// status
			status = rsp.getStatus();
			ContentType ct = HTTPUtil.toContentType(rsp.getContentType(), null);
			if (ct != null) {
				isText = HTTPUtil.isTextMimeType(ct.getMimeType()) == Boolean.TRUE;
				if (ct.getCharset() != null) _charset = CharsetUtil.toCharset(ct.getCharset(), null);
			}
			// a session we created only for this request, but the application does not use jee sessions
			HttpSession hs = _pc.getHttpServletRequest().getSession(false);
			if (hs instanceof HttpSessionDummy && _pc.getSessionType() != Config.SESSION_TYPE_JEE) {
				try {
					if (hs.isNew()) hs.invalidate();
				}
				catch (IllegalStateException ise) {
					// already invalidated
				}
			}
			releasePageContext(_pc, pc);

		}
		Struct rst = new StructImpl();

		byte[] barr = baos.toByteArray();
		if (isText) rst.set(KeyConstants._filecontent, new String(barr, _charset == null ? reqCharset : _charset));
		else rst.set(KeyConstants._filecontent_binary, barr);
		rst.set(KeyConstants._cookies, rspCookies);
		rst.set(KeyConstants._request, request);
		if (session != null) rst.set(KeyConstants._session, session);
		rst.set(KeyConstants._headers, headers);
		// rst.put(KeyConstants._debugging, debugging);
		rst.set(KeyConstants._executionTime, Double.valueOf(exeTime));
		rst.set(KeyConstants._status, Double.valueOf(status));
		rst.set(KeyConstants._status_code, Double.valueOf(status));
		if (pe != null) rst.set(KeyConstants._error, pe.getCatchBlock(pc.getConfig()));
		return rst;
	}

	private static Struct toStruct(Object obj) throws PageException {
		if (Decision.isCastableToStruct(obj)) return Caster.toStruct(obj);
		String str = Caster.toString(obj);
		int index;
		Struct data = new StructImpl(Struct.TYPE_LINKED);
		Key n;
		String v;
		Object existing;
		for (String el: ListUtil.listToList(str, '&', true)) {

			index = el.indexOf('=');
			if (index == -1) {
				n = KeyImpl.init(URLDecoder.decode(el, true));
				v = "";
			}
			else {
				n = KeyImpl.init(URLDecoder.decode(el.substring(0, index), true));
				v = URLDecoder.decode(el.substring(index + 1), true);
			}
			existing = data.get(n, null);
			if (existing != null) {
				if (existing instanceof ArgumentImpl) {
					((ArgumentImpl) existing).appendEL(v);
				}
				else {
					ArgumentImpl arr = new ArgumentImpl();
					arr.append(existing);
					arr.append(v);
					data.setEL(n, arr);
				}
			}
			else data.setEL(n, v);
		}
		return data;
	}

	private static void fillForm(PageContextImpl _pc, Struct src, Charset charset) throws PageException {
		if (src == null) return;

		Iterator<Entry<Key, Object>> it = src.entryIterator();
		Form tmp = _pc.formScope();
		FormImpl trg = tmp instanceof UrlFormImpl ? ((UrlFormImpl) tmp).getForm() : (FormImpl) tmp;

		Entry<Key, Object> e;
		Key n;
		Object v;

		Object vv;
		java.util.List<URLItem> list = new ArrayList<>();
		while (it.hasNext()) {
			e = it.next();
			n = e.getKey();
			v = e.getValue();

			if (v instanceof Array) {
				Iterator<Object> itt = ((Array) v).valueIterator();
				while (itt.hasNext()) {
					vv = itt.next();
					list.add(new URLItem(n.getString(), Caster.toString(vv), false));
				}

			}
			else if (v instanceof Struct) {
				Iterator<Entry<Key, Object>> itt = ((Struct) v).entryIterator();
				Entry<Key, Object> ee;
				while (itt.hasNext()) {
					ee = itt.next();
					list.add(new URLItem(n.getString() + "." + ee.getKey(), Caster.toString(ee.getValue()), false));
				}

			}
			else list.add(new URLItem(n.getString(), Caster.toString(v), false));
		}
		trg.addRaw(null, list.toArray(new URLItem[list.size()]));
	}

	/**
	 * LDEV-4038: a template path that does not start with a slash is relative to the directory of the
	 * calling template, a path with a leading slash (or a windows path) is used as it is.
	 */
	private static String resolveTemplate(PageContext pc, String template) throws PageException {
		if (StringUtil.isEmpty(template)) return template;
		char first = template.charAt(0);
		if (first == '/' || first == '\\' || ResourceUtil.isWindowsPath(template)) return template;

		PageSource ps = pc.getCurrentPageSource(null);
		if (ps == null) return template;

		String caller = ps.getRealpathWithVirtual().replace('\\', '/');
		String dir = caller.substring(0, caller.lastIndexOf('/') + 1);

		// merge and normalize ./ and ../
		List<String> parts = new ArrayList<String>();
		for (String part: (dir + template.replace('\\', '/')).split("/")) {
			if (part.isEmpty() || part.equals(".")) continue;
			if (part.equals("..")) {
				if (parts.isEmpty()) throw new FunctionException(pc, "_InternalRequest", 1, "template",
						"template path [" + template + "] is relative to the calling template [" + caller + "] and points outside of the root");
				parts.remove(parts.size() - 1);
			}
			else parts.add(part);
		}
		return "/" + String.join("/", parts);
	}

	private static PageContextImpl createPageContext(PageContext pc, String template, Struct urls, Struct cookies, Struct headers, byte[] body, Charset charset, OutputStream os,
			String method) throws PageException {

		HttpSession session = pc.getSessionType() == Config.SESSION_TYPE_JEE ? pc.getSession() : null;
		// no container session to share, use our own (LDEV-5288); a jsessionid cookie picks up a session from a former internal request
		boolean ownSession = session == null;
		if (ownSession && cookies != null) session = HttpSessionDummy.get(Caster.toString(cookies.get(KeyConstants._jsessionid, null), null));

		PageContextImpl _pc = ThreadUtil.createPageContext(pc.getConfig(), os, pc.getHttpServletRequest().getServerName(), template, toQueryString(urls, charset),
				CreatePageContext.toCookies(cookies), CreatePageContext.toPair(headers, true), body, CreatePageContext.toPair(new StructImpl(), true),
				CreatePageContext.castValuesToString(pc, new StructImpl()), true, -1, session, method);
		if (ownSession) {
			HttpServletRequest req = HTTPServletRequestWrap.pure(_pc.getHttpServletRequest());
			if (req instanceof HttpServletRequestDummy) ((HttpServletRequestDummy) req).setCreateSession(true);
		}
		return _pc;
	}

	/**
	 * sets the JSESSIONID as a session cookie, with the path, domain, secure, httpOnly, samesite and
	 * partitioned settings of the application's sessionCookie, like the cfid/cftoken cookies
	 */
	private static void setSessionIdCookie(PageContextImpl pc, String id) {
		String domain = PageContextUtil.getCookieDomain(pc);
		boolean httpOnly = SessionCookieDataImpl.DEFAULT.isHttpOnly();
		boolean secure = SessionCookieDataImpl.DEFAULT.isSecure();
		short samesite = SessionCookieDataImpl.DEFAULT.getSamesite();
		String path = SessionCookieDataImpl.DEFAULT.getPath();
		boolean partitioned = SessionCookieDataImpl.DEFAULT.isPartitioned();

		ApplicationContext ac = pc.getApplicationContext();
		if (ac instanceof ApplicationContextSupport) {
			SessionCookieData data = ((ApplicationContextSupport) ac).getSessionCookie();
			if (data != null) {
				httpOnly = data.isHttpOnly();
				secure = data.isSecure();
				samesite = data.getSamesite();
				partitioned = data.isPartitioned();
				if (!StringUtil.isEmpty(data.getDomain(), true)) domain = data.getDomain().trim();
				if (!StringUtil.isEmpty(data.getPath(), true)) path = data.getPath().trim();
			}
		}
		// -1: no expires, the cookie lives as long as the browser session
		((CookieImpl) pc.cookieScope()).setCookieEL(KeyConstants._JSESSIONID, id, -1, secure, path, domain, httpOnly, true, false, samesite, partitioned);
	}

	private static String toQueryString(Struct urls, Charset charset) throws PageException {
		// query string | URL
		Entry<Key, Object> e;
		StringBuilder sbQS = new StringBuilder();
		if (urls != null) {
			Iterator<Entry<Key, Object>> it = urls.entryIterator();
			Object v;
			Key n;
			while (it.hasNext()) {
				e = it.next();
				n = e.getKey();
				v = e.getValue();

				if (v instanceof Argument) {
					Iterator<Entry<Key, Object>> itt = ((Argument) v).entryIterator();
					Entry<Key, Object> ee;
					while (itt.hasNext()) {
						ee = itt.next();
						if (sbQS.length() > 0) sbQS.append('&');
						sbQS.append(urlenc(n.getString(), charset));
						sbQS.append('=');
						sbQS.append(urlenc(Caster.toString(ee.getValue()), charset));
					}
				}
				else {
					if (sbQS.length() > 0) sbQS.append('&');
					sbQS.append(urlenc(e.getKey().getString(), charset));
					sbQS.append('=');
					sbQS.append(urlenc(Caster.toString(v), charset));
				}
			}
		}
		return sbQS.toString();
	}

	private static void releasePageContext(PageContext pc, PageContext oldPC) {
		pc.flush();
		oldPC.getConfig().getFactory().releaseLuceePageContext(pc, false);
		ThreadLocalPageContext.release();
		if (oldPC != null) ThreadLocalPageContext.register(oldPC);
	}

	private static String urlenc(String str, Charset charset) throws PageException {
		try {
			if (!ReqRspUtil.needEncoding(str)) return str;
			return URLEncoder.encode(str, charset);
		}
		catch (UnsupportedEncodingException uee) {
			throw Caster.toPageException(uee);
		}
	}

}
