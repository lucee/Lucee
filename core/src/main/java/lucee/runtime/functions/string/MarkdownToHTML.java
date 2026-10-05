/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
 */
package lucee.runtime.functions.string;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

/**
 * Lucee 8 renders markdown via the markdown extension. The extension build bundled for tests
 * (markdown-extension 1.0.0.0-RC) honors safeMode for HTML escaping but does not sanitize URLs.
 * This wrapper keeps that renderer and applies the same URL policy as commonmark's
 * DefaultUrlSanitizer (http, https, mailto, and relative URLs) when safeMode is true.
 * LDEV-3027 / CVE-2026-29519.
 */
public final class MarkdownToHTML extends BIF {

	private static final long serialVersionUID = 3775127934350736736L;

	private static final Set<String> ALLOWED_PROTOCOLS = new HashSet<String>();
	static {
		ALLOWED_PROTOCOLS.add("http");
		ALLOWED_PROTOCOLS.add("https");
		ALLOWED_PROTOCOLS.add("mailto");
	}

	private static volatile ClassDefinition delegateCD;
	private static volatile BIF delegate;

	public static void bindDelegate(ClassDefinition cd) {
		delegateCD = cd;
		delegate = null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		BIF impl = delegate();
		Object html = impl.invoke(pc, args);
		if (args.length >= 2 && Caster.toBooleanValue(args[1])) return sanitizeUrls(Caster.toString(html));
		return html;
	}

	private static BIF delegate() throws PageException {
		BIF impl = delegate;
		if (impl != null) return impl;
		ClassDefinition cd = delegateCD;
		if (cd == null) throw new ApplicationException("MarkdownToHTML is not available", "the markdown extension is not installed");
		synchronized (MarkdownToHTML.class) {
			impl = delegate;
			if (impl != null) return impl;
			try {
				impl = (BIF) ClassUtil.newInstance(cd.getClazz());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw Caster.toPageException(t);
			}
			delegate = impl;
			return impl;
		}
	}

	static String sanitizeUrls(String html) {
		if (html == null || html.length() == 0) return html;
		StringBuilder out = new StringBuilder(html.length());
		int i = 0;
		int n = html.length();
		while (i < n) {
			int href = indexOfIgnoreCase(html, "href", i);
			int src = indexOfIgnoreCase(html, "src", i);
			int pos;
			if (href < 0 && src < 0) {
				out.append(html, i, n);
				break;
			}
			if (href < 0 || (src >= 0 && src < href)) pos = src;
			else pos = href;
			if (pos > 0 && isNameChar(html.charAt(pos - 1))) {
				out.append(html, i, pos + 1);
				i = pos + 1;
				continue;
			}
			int nameLen = (pos == href) ? 4 : 3;
			int j = pos + nameLen;
			while (j < n && isHtmlSpace(html.charAt(j)))
				j++;
			if (j >= n || html.charAt(j) != '=') {
				out.append(html, i, pos + 1);
				i = pos + 1;
				continue;
			}
			j++;
			while (j < n && isHtmlSpace(html.charAt(j)))
				j++;
			if (j >= n || (html.charAt(j) != '"' && html.charAt(j) != '\'')) {
				out.append(html, i, pos + 1);
				i = pos + 1;
				continue;
			}
			char quote = html.charAt(j);
			int start = j + 1;
			int end = html.indexOf(quote, start);
			if (end < 0) {
				out.append(html, i, n);
				break;
			}
			out.append(html, i, start);
			out.append(sanitizeUrl(html.substring(start, end)));
			out.append(quote);
			i = end + 1;
		}
		return out.toString();
	}

	/**
	 * Same rules as org.commonmark.renderer.html.DefaultUrlSanitizer: allow http, https, mailto and
	 * URLs with no protocol (relative, protocol-relative, query or fragment). Any other scheme is
	 * replaced with an empty string.
	 */
	static String sanitizeUrl(String url) {
		url = stripHtmlSpaces(url);
		int n = url.length();
		for (int i = 0; i < n; i++) {
			char c = url.charAt(i);
			if (c == '/' || c == '#' || c == '?') return url;
			if (c == ':') {
				String protocol = url.substring(0, i).toLowerCase(Locale.ROOT);
				if (!ALLOWED_PROTOCOLS.contains(protocol)) return "";
				return url;
			}
		}
		return url;
	}

	private static String stripHtmlSpaces(String s) {
		int i = 0;
		int n = s.length();
		for (; n > i; n--) {
			if (!isHtmlSpace(s.charAt(n - 1))) break;
		}
		for (; i < n; i++) {
			if (!isHtmlSpace(s.charAt(i))) break;
		}
		if (i == 0 && n == s.length()) return s;
		return s.substring(i, n);
	}

	private static boolean isHtmlSpace(char ch) {
		switch (ch) {
		case ' ':
		case '\t':
		case '\n':
		case '\u000c':
		case '\r':
			return true;
		default:
			return false;
		}
	}

	private static boolean isNameChar(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '-' || ch == ':';
	}

	private static int indexOfIgnoreCase(String html, String needle, int from) {
		int n = html.length();
		int m = needle.length();
		for (int i = from; i <= n - m; i++) {
			int k = 0;
			for (; k < m; k++) {
				char c = html.charAt(i + k);
				char d = needle.charAt(k);
				if (c != d && Character.toLowerCase(c) != d) break;
			}
			if (k == m) return i;
		}
		return -1;
	}
}
