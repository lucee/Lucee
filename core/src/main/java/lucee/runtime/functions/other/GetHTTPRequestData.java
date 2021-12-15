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
/**
 * Implements the CFML Function gethttprequestdata
 */
package lucee.runtime.functions.other;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class GetHTTPRequestData implements Function {

	private static final long serialVersionUID = 1365182999286292317L;

	public static Struct call(PageContext pc) throws PageException {
		return call(pc, true);
	}

	public static Struct call(PageContext pc, boolean includeBody) throws PageException {

		Struct sct = new StructImpl();
		Struct headers = new StructImpl();
		HttpServletRequest req = pc.getHttpServletRequest();
		String charset = pc.getWebCharset().name();
		// headers
		Enumeration e = req.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement().toString();
			headers.set(KeyImpl.init(ReqRspUtil.decode(key, charset, false)), ReqRspUtil.decode(req.getHeader(key), charset, false));
		}

		sct.set(KeyConstants._headers, headers);
		sct.set(KeyConstants._protocol, req.getProtocol());
		sct.set(KeyConstants._method, req.getMethod());

		if (includeBody) sct.set(KeyConstants._content, ReqRspUtil.getRequestBody(pc, false, ""));

		return sct;
	}
}