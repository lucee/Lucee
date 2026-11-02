package lucee.runtime.net.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import lucee.commons.lang.StringUtil;

/**
 * Adds Set-Cookie response headers, making sure a response carries only one Set-Cookie header per
 * cookie. A cookie is identified by name, domain and path (RFC 6265, 5.3). Setting the same cookie
 * again in the same response replaces the earlier header, at the position of the earlier header, so
 * the order of different cookies is kept and only the final value is sent. As the replacement
 * happens in the header list of the response, a cookie set before the response gets committed (for
 * example by a flush) is still sent.
 */
public final class SetCookieHeaders {

	public static final String SET_COOKIE = "Set-Cookie";

	private SetCookieHeaders() {
	}

	/**
	 * adds the given Set-Cookie header value to the response, replacing an earlier Set-Cookie header
	 * for the same cookie (name, domain and path)
	 * 
	 * @param rsp response to add the header to
	 * @param value value of the Set-Cookie header
	 */
	public static void add(HttpServletResponse rsp, String value) {
		String id = identity(value);
		Collection<String> existing = id == null ? null : rsp.getHeaders(SET_COOKIE);
		if (existing != null && !existing.isEmpty()) {
			List<String> headers = new ArrayList<String>(existing.size() + 1);
			boolean replaced = false;
			boolean changed = false;
			for (String header: existing) {
				if (id.equals(identity(header))) {
					changed = true;
					if (!replaced) {
						replaced = true;
						headers.add(value);
					}
				}
				else headers.add(header);
			}
			if (changed) {
				rsp.setHeader(SET_COOKIE, headers.get(0));
				for (int i = 1; i < headers.size(); i++) {
					rsp.addHeader(SET_COOKIE, headers.get(i));
				}
				return;
			}
		}
		rsp.addHeader(SET_COOKIE, value);
	}

	/**
	 * the identity of a cookie, made of the name, the domain and the path
	 * 
	 * @param header value of a Set-Cookie header
	 * @return the identity or null if the value is not a valid Set-Cookie value
	 */
	static String identity(String header) {
		if (header == null) return null;
		int index = header.indexOf(';');
		String nameValue = index == -1 ? header : header.substring(0, index);
		int eq = nameValue.indexOf('=');
		if (eq == -1) return null;
		String name = nameValue.substring(0, eq).trim();
		if (name.isEmpty()) return null;

		String domain = "";
		String path = "";
		while (index != -1) {
			int start = index + 1;
			index = header.indexOf(';', start);
			String attr = (index == -1 ? header.substring(start) : header.substring(start, index)).trim();
			eq = attr.indexOf('=');
			if (eq == -1) continue;
			String attrName = attr.substring(0, eq).trim();
			if (attrName.equalsIgnoreCase("domain")) {
				// a leading dot is ignored, domains are case insensitive
				domain = StringUtil.toLowerCase(attr.substring(eq + 1).trim());
				if (domain.startsWith(".")) domain = domain.substring(1);
			}
			else if (attrName.equalsIgnoreCase("path")) {
				path = attr.substring(eq + 1).trim();
			}
		}
		// ; can not be part of a name, a domain or a path, so it can be used as separator
		return name + ';' + domain + ';' + path;
	}
}
