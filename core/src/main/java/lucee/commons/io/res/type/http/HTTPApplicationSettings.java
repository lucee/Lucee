package lucee.commons.io.res.type.http;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.HeaderImpl;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ModernApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.KeyImpl;

/**
 * LDEV-3376: reads the authentication for the http/https virtual filesystem from the Application.cfc
 * 
 * <pre>
 * this.vfs.http["api.example.com"] = {username: "susi", password: "sorglos"};
 * this.vfs.http["api.example.com:8443"] = {headers: {"Authorization": "Bearer abc"}};
 * </pre>
 * 
 * An entry is selected by the host of the url, "host:port" is looked up before "host".
 */
final class HTTPApplicationSettings {

	private static final Key HEADERS = KeyConstants._headers;

	private HTTPApplicationSettings() {
	}

	/**
	 * @return the headers the application defines for the host of the given connection data or null if
	 *         there are none. Credentials in the path replace the authentication of the application, all
	 *         other headers still apply.
	 */
	static Header[] getHeaders(HTTPConnectionData data) {
		if (StringUtil.isEmpty(data.host)) return null;

		PageContext pc = ThreadLocalPageContext.get();
		if (pc == null) return null;
		ApplicationContext ac = pc.getApplicationContext();
		if (!(ac instanceof ModernApplicationContext)) return null;

		Struct vfs = Caster.toStruct(((ModernApplicationContext) ac).getCustom(KeyConstants._vfs), null);
		if (vfs == null) return null;
		Struct http = Caster.toStruct(vfs.get(KeyConstants._http, null), null);
		if (http == null) return null;

		String name = data.host;
		Object entry = null;
		if (data.port > 0) {
			name = data.host + ":" + data.port;
			entry = http.get(KeyImpl.init(name), null);
		}
		if (entry == null) {
			name = data.host;
			entry = http.get(KeyImpl.init(name), null);
		}
		if (entry == null) return null;

		Struct sct = Caster.toStruct(entry, null);
		if (sct == null) throw invalid("this.vfs.http[\"" + name + "\"] needs to be a struct");

		boolean pathCredentials = !StringUtil.isEmpty(data.username);
		List<Header> result = new ArrayList<>();

		String username = Caster.toString(sct.get(KeyConstants._username, null), null);
		if (!pathCredentials && !StringUtil.isEmpty(username)) {
			String password = Caster.toString(sct.get(KeyConstants._password, null), "");
			result.add(new HeaderImpl("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8))));
		}

		Object oHeaders = sct.get(HEADERS, null);
		if (oHeaders != null) {
			Struct headers = Caster.toStruct(oHeaders, null);
			if (headers == null) throw invalid("this.vfs.http[\"" + name + "\"].headers needs to be a struct");
			for (Key key: headers.keys()) {
				String value = Caster.toString(headers.get(key, null), null);
				if (value == null) throw invalid("the header [" + key.getString() + "] in this.vfs.http[\"" + name + "\"].headers needs to be a simple value");
				if (key.getString().equalsIgnoreCase("authorization")) {
					if (pathCredentials) continue;
					// an explicit authorization header wins over username/password
					result.removeIf(h -> h.getName().equalsIgnoreCase("authorization"));
				}
				result.add(new HeaderImpl(key.getString(), value));
			}
		}
		return result.isEmpty() ? null : result.toArray(new Header[result.size()]);
	}

	private static PageRuntimeException invalid(String msg) {
		return new PageRuntimeException(new ApplicationException(msg));
	}
}
