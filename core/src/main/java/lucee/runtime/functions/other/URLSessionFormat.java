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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.type.util.ArrayUtil;

public final class URLSessionFormat implements Function {

	private static final long serialVersionUID = 1486918425114400713L;

	public static String call(PageContext pc, String strUrl) {
		Cookie[] cookies = ReqRspUtil.getCookies(pc.getHttpServletRequest(), pc.getWebCharset());
		if (!pc.getApplicationContext().isSetClientCookies() || ArrayUtil.isEmpty(cookies)) {
			HttpSession s;
			if (pc.getSessionType() == Config.SESSION_TYPE_APPLICATION) {
				int indexQ = strUrl.indexOf('?');
				int indexA = strUrl.indexOf('&');
				int len = strUrl.length();
				if (indexQ == len - 1 || indexA == len - 1) strUrl += getURLToken(pc);
				else if (indexQ != -1) strUrl += "&" + getURLToken(pc);
				else strUrl += "?" + getURLToken(pc);
			}
			else if ((s = pc.getSession()) != null) {
				if (s != null) {
					int indexQ = strUrl.indexOf('?');

					if (indexQ != -1) strUrl = strUrl.substring(0, indexQ) + getJSession(s) + strUrl.substring(indexQ);
					else strUrl += getJSession(s);
				}
			}

		}
		return strUrl;
	}

	private static String getURLToken(PageContext pc) {
		return "CFID=" + pc.getCFID() + "&CFTOKEN=" + pc.getCFToken();
	}

	private static String getJSession(HttpSession s) {
		return ";jsessionid=" + s.getId();
	}
}