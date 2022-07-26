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
package lucee.runtime.type.scope;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.security.ScriptProtect;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.ReadOnlyStruct;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructUtil;

/**
 *
 *
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
public final class CGIImplReadOnly extends ReadOnlyStruct implements CGI, ScriptProtected, Externalizable {

	private static final long serialVersionUID = 5219795840777155232L;

	private static final Collection.Key[] keys = { KeyConstants._auth_password, KeyConstants._auth_type, KeyConstants._auth_user, KeyConstants._cert_cookie,
			KeyConstants._cert_flags, KeyConstants._cert_issuer, KeyConstants._cert_keysize, KeyConstants._cert_secretkeysize, KeyConstants._cert_serialnumber,
			KeyConstants._cert_server_issuer, KeyConstants._cert_server_subject, KeyConstants._cert_subject, KeyConstants._cf_template_path, KeyConstants._content_length,
			KeyConstants._content_type, KeyConstants._gateway_interface, KeyConstants._http_accept, KeyConstants._http_accept_encoding, KeyConstants._http_accept_language,
			KeyConstants._http_connection, KeyConstants._http_cookie, KeyConstants._http_host, KeyConstants._http_user_agent, KeyConstants._http_referer, KeyConstants._https,
			KeyConstants._https_keysize, KeyConstants._https_secretkeysize, KeyConstants._https_server_issuer, KeyConstants._https_server_subject, KeyConstants._path_info,
			KeyConstants._path_translated, KeyConstants._query_string, KeyConstants._remote_addr, KeyConstants._remote_host, KeyConstants._remote_user,
			KeyConstants._request_method, KeyConstants._request_url, KeyConstants._script_name, KeyConstants._server_name, KeyConstants._server_port,
			KeyConstants._server_port_secure, KeyConstants._server_protocol, KeyConstants._server_software, KeyConstants._web_server_api, KeyConstants._context_path,
			KeyConstants._local_addr, KeyConstants._local_host };
	private static Struct staticKeys = new StructImpl();
	static {
		for (int i = 0; i < keys.length; i++) {
			staticKeys.setEL(keys[i], "");
		}
	}

	private static String localAddress = "";
	private static String localHost = "";

	static {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			localAddress = addr.getHostAddress();
			localHost = addr.getHostName();
		}
		catch (UnknownHostException uhe) {
		}
	}

	private transient HttpServletRequest req;
	private boolean isInit;
	private transient Struct https;
	private transient Struct headers;
	private int scriptProtected;

	private boolean disconnected;
	private Map<Key, Object> disconnectedData;

	public CGIImplReadOnly() {
		this.setReadOnly(true);
	}

	public void disconnect() {
		if (disconnected) return;

		_disconnect();
		disconnected = true;
		req = null;
	}

	private void _disconnect() {
		disconnectedData = new HashMap<Key, Object>();
		for (int i = 0; i < keys.length; i++) {
			disconnectedData.put(keys[i], get(keys[i], ""));
		}
	}

	@Override
	public final boolean containsKey(Key key) {
		return staticKeys.containsKey(key);
	}

	@Override
	public final boolean containsKey(PageContext pc, Key key) {
		return staticKeys.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return super.containsValue(value);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		Struct sct = new StructImpl();
		copy(this, sct, deepCopy);
		return sct;
	}

	@Override
	public int size() {
		return keys.length;
	}

	@Override
	public Collection.Key[] keys() {
		return keys;
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {

		if (disconnected) {
			return disconnectedData.getOrDefault(key, defaultValue);
		}

		if (https == null) {
			https = new StructImpl();
			headers = new StructImpl();
			String k, v;
			try {
				Enumeration e = req.getHeaderNames();

				while (e.hasMoreElements()) {
					k = (String) e.nextElement();
					v = req.getHeader(k);
					// print.err(k.length()+":"+k);
					headers.setEL(KeyImpl.init(k), v);
					headers.setEL(KeyImpl.init(k = k.replace('-', '_')), v);
					https.setEL(KeyImpl.init("http_" + k), v);
				}
			}
			catch (Exception e) {
			}
		}

		String lkey = key.getLowerString();
		char first = lkey.charAt(0);
		try {
			if (first == 'a') {
				if (key.equals(KeyConstants._auth_type)) return toString(req.getAuthType());
			}
			else if (first == 'c') {
				if (key.equals(KeyConstants._context_path)) return toString(req.getContextPath());
				if (key.equals(KeyConstants._cf_template_path)) return getPathTranslated();
			}
			else if (first == 'h') {

				if (lkey.startsWith("http_")) {

					Object _null = NullSupportHelper.NULL();
					Object o = https.get(key, _null);
					if (o == _null && key.equals(KeyConstants._http_if_modified_since)) o = https.get(KeyConstants._last_modified, _null);
					if (o != _null) return doScriptProtect((String) o);
				}
				else if (key.equals(KeyConstants._https)) return (req.isSecure() ? "on" : "off");
			}
			else if (first == 'r') {
				if (key.equals(KeyConstants._remote_user)) return toString(req.getRemoteUser());
				if (key.equals(KeyConstants._remote_addr)) {
					return toString(req.getRemoteAddr());
				}
				if (key.equals(KeyConstants._remote_host)) return toString(req.getRemoteHost());
				if (key.equals(KeyConstants._request_method)) return req.getMethod();
				if (key.equals(KeyConstants._request_url)) {
					try {
						return ReqRspUtil.getRequestURL(req, true);
					}
					catch (Exception e) {
					}
				}
				if (key.equals(KeyConstants._request_uri)) return toString(req.getAttribute("javax.servlet.include.request_uri"));
				if (key.getUpperString().startsWith("REDIRECT_")) {
					// from attributes (key sensitive)
					Object value = req.getAttribute(key.getString());
					if (!StringUtil.isEmpty(value)) return toString(value);

					// from attributes (key insensitive)
					Enumeration<String> names = req.getAttributeNames();
					String k;
					while (names.hasMoreElements()) {
						k = names.nextElement();
						if (k.equalsIgnoreCase(key.getString())) {
							return toString(req.getAttribute(k));
						}
					}
				}
			}
			else if (first == 'l') {
				if (key.equals(KeyConstants._local_addr)) return toString(localAddress);
				if (key.equals(KeyConstants._local_host)) return toString(localHost);
			}
			else if (first == 's') {
				if (key.equals(KeyConstants._script_name)) return ReqRspUtil.getScriptName(null, req);
				// return StringUtil.emptyIfNull(req.getContextPath())+StringUtil.emptyIfNull(req.getServletPath());
				if (key.equals(KeyConstants._server_name)) return toString(req.getServerName());
				if (key.equals(KeyConstants._server_protocol)) return toString(req.getProtocol());
				if (key.equals(KeyConstants._server_port)) return Caster.toString(req.getServerPort());
				if (key.equals(KeyConstants._server_port_secure)) return (req.isSecure() ? "1" : "0");
			}
			else if (first == 'p') {
				if (key.equals(KeyConstants._path_info)) {
					String pathInfo = Caster.toString(req.getAttribute("javax.servlet.include.path_info"), null);
					if (StringUtil.isEmpty(pathInfo)) pathInfo = Caster.toString(req.getHeader("xajp-path-info"), null);
					if (StringUtil.isEmpty(pathInfo)) pathInfo = req.getPathInfo();
					if (StringUtil.isEmpty(pathInfo)) {
						pathInfo = Caster.toString(req.getAttribute("requestedPath"), null);
						if (!StringUtil.isEmpty(pathInfo, true)) {
							String scriptName = ReqRspUtil.getScriptName(null, req);
							if (pathInfo.startsWith(scriptName)) pathInfo = pathInfo.substring(scriptName.length());
						}
					}

					if (!StringUtil.isEmpty(pathInfo, true)) return pathInfo;
					return "";
				}
				if (key.equals(KeyConstants._path_translated)) return getPathTranslated();
			}
			else if (first == 'q') {
				if (key.equals(KeyConstants._query_string)) return doScriptProtect(toString(ReqRspUtil.getQueryString(req)));
			}
		}
		catch (Exception e) {
			return other(key, defaultValue);
		}

		// check header
		String headerValue = (String) headers.get(key, null);// req.getHeader(key.getString());
		if (headerValue != null) return doScriptProtect(headerValue);

		return other(key, defaultValue);
	}

	private Object getPathTranslated() {
		try {
			PageContext pc = ThreadLocalPageContext.get();
			return pc.getBasePageSource().getResourceTranslated(pc).toString();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return "";
	}

	private Object other(Collection.Key key, Object defaultValue) {
		if (staticKeys.containsKey(key)) return "";
		return defaultValue;
	}

	private String doScriptProtect(String value) {
		if (isScriptProtected()) return ScriptProtect.translate(value);
		return value;
	}

	private String toString(Object str) {
		return StringUtil.toStringEmptyIfNull(str);
	}

	@Override
	public Object get(Collection.Key key) {
		Object value = get(key, "");
		if (value == null) value = "";
		return value;
	}

	@Override
	public Object get(PageContext pc, Collection.Key key) {
		Object value = get(pc, key, "");
		if (value == null) value = "";
		return value;
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new KeyIterator(keys());
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new StringIterator(keys());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	@Override
	public boolean isInitalized() {
		return isInit;
	}

	@Override
	public void initialize(PageContext pc) {
		isInit = true;
		req = pc.getHttpServletRequest();

		if (scriptProtected == ScriptProtected.UNDEFINED) {
			scriptProtected = ((pc.getApplicationContext().getScriptProtect() & ApplicationContext.SCRIPT_PROTECT_CGI) > 0) ? ScriptProtected.YES : ScriptProtected.NO;
		}
	}

	@Override
	public void release(PageContext pc) {
		isInit = false;
		scriptProtected = ScriptProtected.UNDEFINED;
		req = null;
		https = null;
		headers = null;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return StructUtil.toDumpTable(this, "CGI Scope", pageContext, maxlevel, dp);
	}

	@Override
	public int getType() {
		return SCOPE_CGI;
	}

	@Override
	public String getTypeAsString() {
		return "cgi";
	}

	@Override
	public boolean isScriptProtected() {
		return scriptProtected == ScriptProtected.YES;
	}

	@Override
	public void setScriptProtecting(ApplicationContext ac, boolean scriptProtecting) {
		scriptProtected = scriptProtecting ? ScriptProtected.YES : ScriptProtected.NO;
	}

	public static String getDomain(HttpServletRequest req) { // DIFF 23
		StringBuffer sb = new StringBuffer();
		sb.append(req.isSecure() ? "https://" : "http://");
		sb.append(req.getServerName());
		sb.append(':');
		sb.append(req.getServerPort());
		if (!StringUtil.isEmpty(req.getContextPath())) sb.append(req.getContextPath());
		return sb.toString();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		_disconnect();

		out.writeBoolean(isInit);
		out.writeObject(disconnectedData);
		out.writeInt(scriptProtected);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		isInit = in.readBoolean();
		disconnectedData = (Map<Key, Object>) in.readObject();
		scriptProtected = in.readInt();
		disconnected = true;
	}

}
