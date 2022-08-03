package lucee.runtime.functions.system;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.ContentType;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.URLDecoder;
import lucee.commons.net.URLEncoder;
import lucee.commons.net.URLItem;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.Abort;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.other.CreatePageContext;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.net.http.HttpServletResponseDummy;
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
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.scope.FormImpl;
import lucee.runtime.type.scope.UrlFormImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class InternalRequest implements Function {

	private static final long serialVersionUID = -8163856691035353577L;

	public static boolean cookieAsQuery = false;
	public static final Key FILECONTENT_BYNARY = KeyImpl.getInstance("filecontent_binary");
	public static final Key STATUS_CODE = KeyImpl.getInstance("status_code");

	private static final Key CONTENT_TYPE = KeyImpl.getInstance("content-type");

	public static Struct call(final PageContext pc, String template, String method, Object oUrls, Object oForms, Struct cookies, Struct headers, Object body, String strCharset,
			boolean addToken, boolean throwonerror) throws PageException {
		Struct urls = toStruct(oUrls);
		Struct forms = toStruct(oForms);

		// add token
		if (addToken) {
			// if(true) throw new ApplicationException("addtoken==true");
			if (cookies == null) cookies = new StructImpl();

			cookies.set(KeyConstants._cfid, pc.getCFID());
			cookies.set(KeyConstants._cftoken, pc.getCFToken());
			String jsessionid = pc.getJSessionId();
			if (jsessionid != null) cookies.set("jsessionid", jsessionid);
		}

		// charset
		Charset reqCharset = StringUtil.isEmpty(strCharset) ? pc.getWebCharset() : CharsetUtil.toCharset(strCharset);

		String ext = ResourceUtil.getExtension(template, null);
		// welcome files
		if (StringUtil.isEmpty(ext)) {
			throw new FunctionException(pc, "Invoke", 1, "url", "welcome file listing not supported, please define the template name.");
		}

		// dialect
		int dialect = ((CFMLFactoryImpl) pc.getConfig().getFactory()).toDialect(ext, -1);
		if (dialect == -1) dialect = pc.getCurrentTemplateDialect();
		// CFMLEngine.DIALECT_LUCEE

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] _barr = null;

		if (Decision.isBinary(body)) _barr = Caster.toBinary(body);
		else if (body != null) {
			Charset cs = null;
			// get charset
			if (headers != null) {
				String strCT = Caster.toString(headers.get(CONTENT_TYPE), null);
				if (strCT != null) {
					ContentType ct = HTTPUtil.toContentType(strCT, null);
					if (ct != null) {
						String strCS = ct.getCharset();
						if (!StringUtil.isEmpty(strCS)) cs = CharsetUtil.toCharSet(strCS, CharSet.UTF8).toCharset();
					}
				}
			}
			if (cs == null) cs = CharsetUtil.UTF8;

			String str = Caster.toString(body);
			_barr = str.getBytes(cs);
		}

		PageContextImpl _pc = createPageContext(pc, template, urls, cookies, headers, _barr, reqCharset, baos);
		fillForm(_pc, forms, reqCharset);
		Collection request, session = null;
		int status;
		long exeTime;
		boolean isText = false;
		Charset _charset = null;
		PageException pe = null;
		Object rspCookies = cookieAsQuery ? new QueryImpl(new String[] { "name", "value", "path", "domain", "expires", "secure", "httpOnly", "samesite" }, 0, "cookies")
				: new StructImpl(Struct.TYPE_LINKED);

		try {

			if (CFMLEngine.DIALECT_LUCEE == dialect) _pc.execute(template, true, false);
			else _pc.executeCFML(template, true, false);
			HttpSession s;
			if (_pc.getSessionType() == Config.SESSION_TYPE_JEE && (s = _pc.getSession()) != null) _pc.cookieScope().set(KeyConstants._JSESSIONID, s.getId());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			if (!(t instanceof Abort)) {
				if (throwonerror) throw Caster.toPageException(t);
				pe = Caster.toPageException(t);
			}
		}
		finally {
			_pc.flush();
			// cookie = _pc.cookieScope().duplicate(false);
			request = _pc.requestScope().duplicate(false);
			session = sessionEnabled(_pc) ? _pc.sessionScope().duplicate(false) : null;
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
				if (name.equals("Set-Cookie")) {
					String cs = _pc.getWebCharset().name();
					for (String v: values) {

						if (cookieAsQuery) Http.parseCookie((Query) rspCookies, v, cs);
						else Http.parseCookie((Struct) rspCookies, v, cs);
					}

				}

				if (values.size() > 1) headers.set(name, Caster.toArray(values));
				else headers.set(name, values.iterator().next());
			}

			// status
			status = rsp.getStatus();
			ContentType ct = HTTPUtil.toContentType(rsp.getContentType(), null);
			if (ct != null) {
				isText = HTTPUtil.isTextMimeType(ct.getMimeType()) == Boolean.TRUE;
				if (ct.getCharset() != null) _charset = CharsetUtil.toCharset(ct.getCharset(), null);
			}
			releasePageContext(_pc, pc);

		}
		Struct rst = new StructImpl();

		byte[] barr = baos.toByteArray();
		if (isText) rst.set(KeyConstants._filecontent, new String(barr, _charset == null ? reqCharset : _charset));
		else rst.set(FILECONTENT_BYNARY, barr);
		rst.set(KeyConstants._cookies, rspCookies);
		rst.set(KeyConstants._request, request);
		if (session != null) rst.set(KeyConstants._session, session);
		rst.set(KeyConstants._headers, headers);
		// rst.put(KeyConstants._debugging, debugging);
		rst.set(KeyConstants._executionTime, new Double(exeTime));
		rst.set(KeyConstants._status, new Double(status));
		rst.set(STATUS_CODE, new Double(status));
		if (pe != null) rst.set(KeyConstants._error, pe.getCatchBlock(pc.getConfig()));
		return rst;
	}

	private static Struct toStruct(Object obj) throws PageException {
		if (Decision.isCastableToStruct(obj)) return Caster.toStruct(obj);
		String str = Caster.toString(obj);
		int index;
		Struct data = new StructImpl(Struct.TYPE_LINKED);
		// boolean asArray = pc.getApplicationContext().getSameFieldAsArray(scope);
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

	private static boolean sessionEnabled(PageContextImpl pc) {
		ApplicationContext ac = pc.getApplicationContext();
		if (ac == null) return false;// this test properly is not necessary
		return ac.hasName() && ac.isSetSessionManagement();
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

	private static PageContextImpl createPageContext(PageContext pc, String template, Struct urls, Struct cookies, Struct headers, byte[] body, Charset charset, OutputStream os)
			throws PageException {

		HttpSession session = pc.getSessionType() == Config.SESSION_TYPE_JEE ? pc.getSession() : null;

		return ThreadUtil.createPageContext(pc.getConfig(), os, pc.getHttpServletRequest().getServerName(), template, toQueryString(urls, charset),
				CreatePageContext.toCookies(cookies), CreatePageContext.toPair(headers, true), body, CreatePageContext.toPair(new StructImpl(), true),
				CreatePageContext.castValuesToString(new StructImpl()), true, -1, session);
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
			if (!ReqRspUtil.needEncoding(str, false)) return str;
			return URLEncoder.encode(str, charset);
		}
		catch (UnsupportedEncodingException uee) {
			throw Caster.toPageException(uee);
		}
	}

}
