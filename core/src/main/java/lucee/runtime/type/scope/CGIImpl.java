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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.security.ScriptProtect;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;

/**
 *
 *
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
public final class CGIImpl extends StructSupport implements CGI, ScriptProtected, Externalizable {

	private static final long serialVersionUID = 5219795840777155232L;

	private static final Collection.Key[] STATIC_KEYS = { KeyConstants._auth_password, KeyConstants._auth_type, KeyConstants._auth_user, KeyConstants._cert_cookie,
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
		for (int i = 0; i < STATIC_KEYS.length; i++) {
			staticKeys.setEL(STATIC_KEYS[i], "");
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
	private Struct internal;
	private Map<Collection.Key, Collection.Key> aliases;
	private int scriptProtected;

	public CGIImpl() {
		// this.setReadOnly(true);
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

		// if(internal==null) {
		internal = new StructImpl();
		aliases = new HashMap<Collection.Key, Collection.Key>();
		String k, v;
		Collection.Key key, alias, httpKey;
		try {
			Enumeration<String> e = req.getHeaderNames();
			while (e.hasMoreElements()) {
				// keys
				k = e.nextElement();
				key = KeyImpl.init(k);
				if (k.contains("-")) alias = KeyImpl.init(k.replace('-', '_'));
				else alias = null;

				httpKey = KeyImpl.init("http_" + (alias == null ? key : alias).getString().toLowerCase());

				// set value
				v = doScriptProtect(req.getHeader(k));
				internal.setEL(httpKey, v);

				// set alias keys
				aliases.put(key, httpKey);
				if (alias != null) aliases.put(alias, httpKey);
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		// }
	}

	@Override
	public void release(PageContext pc) {
		isInit = false;
		scriptProtected = ScriptProtected.UNDEFINED;
		req = null;
		internal = null;
		aliases = null;
	}

	@Override
	public boolean containsKey(Key key) {
		return internal.containsKey(key) || staticKeys.containsKey(key) || aliases.containsKey(key);
	}

	@Override
	public boolean containsKey(PageContext pc, Key key) {
		return internal.containsKey(key) || staticKeys.containsKey(key) || aliases.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		Iterator<Object> it = internal.valueIterator();
		while (it.hasNext()) {
			if (it.next().equals(value)) return true;
		}
		return false;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		Struct sct = new StructImpl();
		StructImpl.copy(this, sct, deepCopy);
		return sct;
	}

	@Override
	public int size() {
		return keys().length;
	}

	@Override
	public Collection.Key[] keys() {
		Set<Collection.Key> set = new HashSet<Collection.Key>();
		Iterator<Key> it = internal.keyIterator();
		while (it.hasNext())
			set.add(it.next());
		it = staticKeys.keyIterator();
		while (it.hasNext())
			set.add(it.next());
		return set.toArray(new Collection.Key[set.size()]);
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		return get(null, key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {

		// do we have internal?

		Object _null = NullSupportHelper.NULL(pc);
		Object res = internal.get(pc, key, _null);
		if (res != _null) return res;

		// do we have an alias
		{
			Key k = aliases.get(key);
			if (k != null) {
				res = internal.get(pc, k, _null);
				if (res != _null) return res;
			}
		}

		if (req != null) {

			String lkey = key.getLowerString();
			char first = lkey.charAt(0);

			try {
				if (first == 'a') {
					if (key.equals(KeyConstants._auth_type)) return store(key, toString(req.getAuthType()));
				}
				else if (first == 'c') {
					if (key.equals(KeyConstants._context_path)) return store(key, toString(req.getContextPath()));
					if (key.equals(KeyConstants._cf_template_path)) return store(key, getPathTranslated());
				}
				else if (first == 'h') {

					// _http_if_modified_since
					if (key.equals(KeyConstants._http_if_modified_since)) {
						Object o = internal.get(KeyConstants._last_modified, _null);
						if (o != _null) return store(key, (String) o);
					}
					else if (key.equals(KeyConstants._https)) return store(key, req.isSecure() ? "on" : "off");
				}
				else if (first == 'r') {
					if (key.equals(KeyConstants._remote_user)) return store(key, toString(req.getRemoteUser()));
					if (key.equals(KeyConstants._remote_addr)) return store(key, toString(req.getRemoteAddr()));
					if (key.equals(KeyConstants._remote_host)) return store(key, toString(req.getRemoteHost()));
					if (key.equals(KeyConstants._request_method)) return store(key, req.getMethod());
					if (key.equals(KeyConstants._request_url)) return store(key, ReqRspUtil.getRequestURL(req, true));
					if (key.equals(KeyConstants._request_uri)) return store(key, toString(req.getAttribute("javax.servlet.include.request_uri")));
					// we do not store this, to be as backward compatible as possible.
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
					if (key.equals(KeyConstants._local_addr)) return store(key, toString(localAddress));
					if (key.equals(KeyConstants._local_host)) return store(key, toString(localHost));
				}
				else if (first == 's') {
					if (key.equals(KeyConstants._script_name)) return store(key, ReqRspUtil.getScriptName(null, req));
					if (key.equals(KeyConstants._server_name)) return store(key, toString(req.getServerName()));
					if (key.equals(KeyConstants._server_protocol)) return store(key, toString(req.getProtocol()));
					if (key.equals(KeyConstants._server_port)) return store(key, Caster.toString(req.getServerPort()));
					if (key.equals(KeyConstants._server_port_secure)) return store(key, req.isSecure() ? "1" : "0");
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

						if (!StringUtil.isEmpty(pathInfo, true)) return store(key, pathInfo);
						return "";
					}
					if (key.equals(KeyConstants._path_translated)) return store(key, getPathTranslated());
				}
				else if (first == 'q') {
					if (key.equals(KeyConstants._query_string)) return store(key, doScriptProtect(toString(ReqRspUtil.getQueryString(req))));
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		return other(key, defaultValue);
	}

	private Object store(Key key, String value) {
		internal.setEL(key, value);
		return value;
	}

	private Object other(Collection.Key key, Object defaultValue) {
		if (staticKeys.containsKey(key)) return "";
		return defaultValue;
	}

	private String getPathTranslated() {
		try {
			PageContext pc = ThreadLocalPageContext.get();
			return pc.getBasePageSource().getResourceTranslated(pc).toString();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
		return "";
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
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return StructUtil.toDumpTable(this, "CGI Scope (writable)", pageContext, maxlevel, dp);
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

	@Override
	public Object remove(Key key) throws PageException {
		Key k = aliases.remove(key);
		if (k != null) key = k;

		Object rtn = internal.remove(key);
		if (staticKeys.containsKey(key)) internal.set(key, ""); // we do this to avoid to this get reinit again
		return rtn;
	}

	@Override
	public Object removeEL(Key key) {
		Key k = aliases.remove(key);
		if (k != null) key = k;

		Object rtn = internal.removeEL(key);
		if (staticKeys.containsKey(key)) internal.setEL(key, ""); // we do this to avoid to this get reinit again
		return rtn;
	}

	@Override
	public void clear() {
		Key[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			removeEL(keys[i]);
		}
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		Key k = aliases.get(key);
		if (k != null) key = k;
		return internal.set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		Key k = aliases.get(key);
		if (k != null) key = k;
		return internal.setEL(key, value);
	}

	@Override
	public Iterator<Object> valueIterator() {
		return internal.valueIterator();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		duplicate(false);// we make this to store everything into internal

		out.writeBoolean(isInit);
		out.writeObject(internal);
		out.writeObject(aliases);
		out.writeInt(scriptProtected);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		isInit = in.readBoolean();
		internal = (Struct) in.readObject();
		aliases = (Map<Key, Key>) in.readObject();
		scriptProtected = in.readInt();
	}
}