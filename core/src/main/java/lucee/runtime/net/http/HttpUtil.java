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
package lucee.runtime.net.http;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.Pair;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class HttpUtil {

	/**
	 * read all headers from request and return it
	 * 
	 * @param req
	 * @return
	 */
	public static Pair<String, String>[] cloneHeaders(HttpServletRequest req) {
		List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
		Enumeration<String> e = req.getHeaderNames(), ee;
		String name;
		while (e.hasMoreElements()) {
			name = e.nextElement();
			ee = req.getHeaders(name);
			while (ee.hasMoreElements()) {
				headers.add(new Pair<String, String>(name, ee.nextElement().toString()));
			}
		}
		return (Pair<String, String>[]) headers.toArray(new Pair[headers.size()]);
	}

	public static Struct getAttributesAsStruct(HttpServletRequest req) {
		Struct attributes = new StructImpl();
		Enumeration e = req.getAttributeNames();
		String name;
		while (e.hasMoreElements()) {
			name = (String) e.nextElement();// MUST (hhlhgiug) can throw ConcurrentModificationException
			if (name != null) attributes.setEL(name, req.getAttribute(name));
		}
		return attributes;
	}

	public static Pair<String, Object>[] getAttributes(HttpServletRequest req) {
		List<Pair<String, Object>> attributes = new ArrayList<Pair<String, Object>>();
		Enumeration e = req.getAttributeNames();
		String name;
		while (e.hasMoreElements()) {
			name = (String) e.nextElement();
			attributes.add(new Pair<String, Object>(name, req.getAttribute(name)));
		}
		return attributes.toArray(new Pair[attributes.size()]);
	}

	public static Pair<String, String>[] cloneParameters(HttpServletRequest req) {
		List<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
		Enumeration e = req.getParameterNames();
		String[] values;
		String name;

		while (e.hasMoreElements()) {
			name = (String) e.nextElement();
			values = req.getParameterValues(name);
			if (values == null && ReqRspUtil.needEncoding(name, false)) values = req.getParameterValues(ReqRspUtil.encode(name, ReqRspUtil.getCharacterEncoding(null, req)));
			if (values == null) {
				PageContext pc = ThreadLocalPageContext.get();
				if (pc != null && ReqRspUtil.identical(pc.getHttpServletRequest(), req)) {
					values = HTTPServletRequestWrap.getParameterValues(ThreadLocalPageContext.get(), name);
				}
			}
			if (values != null) for (int i = 0; i < values.length; i++) {
				parameters.add(new Pair<String, String>(name, values[i]));
			}
		}
		return parameters.toArray(new Pair[parameters.size()]);
	}

	public static Cookie[] cloneCookies(Config config, HttpServletRequest req) {
		Cookie[] src = ReqRspUtil.getCookies(req, CharsetUtil.getWebCharset());
		if (src == null) return new Cookie[0];

		Cookie[] dest = new Cookie[src.length];
		for (int i = 0; i < src.length; i++) {
			dest[i] = (Cookie) src[i].clone();
		}
		return dest;
	}

}