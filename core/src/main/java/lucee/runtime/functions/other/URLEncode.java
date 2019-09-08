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
package lucee.runtime.functions.other;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;

public class URLEncode {

	public static String call(PageContext pc, String str) throws PageException {

		return invoke(str, "UTF-8", true);
	}

	public static String call(PageContext pc, String str, String encoding) throws PageException {

		return invoke(str, encoding, true);
	}

	public static String call(PageContext pc, String str, String encoding, boolean force) throws PageException {

		return invoke(str, encoding, force);
	}

	public static String invoke(String str, String encoding, boolean force) throws PageException {

		if (!force && !ReqRspUtil.needEncoding(str, false)) return str;

		try {

			return URLEncoder.encode(str, encoding);
		}
		catch (UnsupportedEncodingException e) {

			throw Caster.toPageException(e);
		}
	}

}