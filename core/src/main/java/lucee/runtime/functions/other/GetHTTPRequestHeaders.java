/**
 * Copyright (c) 2021, Lucee Assosication Switzerland. All rights reserved.
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

package lucee.runtime.functions.other;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Returns a Struct with the HTTP Request Headers
 */
public class GetHTTPRequestHeaders extends BIF {

	public static Struct call(PageContext pc) throws PageException {

		HttpServletRequest req = pc.getHttpServletRequest();
		String charset = pc.getWebCharset().name();

		Struct result = new StructImpl();
		Enumeration e = req.getHeaderNames();

		while (e.hasMoreElements()) {
			String key = e.nextElement().toString();
			result.set(KeyImpl.init(ReqRspUtil.decode(key, charset, false)), ReqRspUtil.decode(req.getHeader(key), charset, false));
		}

		return result;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {

		return call(pc);
	}
}