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
 * Implements the CFML Function urlencodedformat
 */
package lucee.runtime.functions.other;

import java.io.UnsupportedEncodingException;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.URLEncoder;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;

public final class URLEncodedFormat implements Function {

	private static final long serialVersionUID = 5640029138134769481L;

	public static String call(PageContext pc, String str) throws PageException {
		return call(pc, str, "UTF-8", true);
	}

	public static String call(PageContext pc, String str, String encoding) throws PageException {
		return call(pc, str, encoding, true);
	}

	public static String call(PageContext pc, String str, String encoding, boolean force) throws PageException {
		return invoke(str, encoding, force);
	}

	public static String invoke(String str, String encoding, boolean force) throws PageException {
		if (!force && !ReqRspUtil.needEncoding(str, false)) return str;

		try {
			String enc = lucee.commons.net.URLEncoder.encode(str, encoding);
			return StringUtil.replace(
					StringUtil.replace(StringUtil.replace(StringUtil.replace(StringUtil.replace(enc, "+", "%20", false), "*", "%2A", false), "-", "%2D", false), ".", "%2E", false),
					"_", "%5F", false);// TODO do better
			// return enc;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			try {
				return URLEncoder.encode(str, encoding);
			}
			catch (UnsupportedEncodingException e) {
				throw Caster.toPageException(e);
			}
		}
	}

}