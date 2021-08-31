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
package lucee.commons.net;

import java.io.UnsupportedEncodingException;

import lucee.commons.io.SystemUtil;
import lucee.runtime.net.http.ReqRspUtil;

public class URLDecoder {

	private URLDecoder() {
	}

	/**
	 * @param string
	 * @return
	 */
	public static String decode(String str, boolean force) {
		try {
			return decode(str, SystemUtil.getCharset().name(), force);
		}
		catch (UnsupportedEncodingException e) {
			return str;
		}
	}

	public static String decode(String s, String enc, boolean force) throws UnsupportedEncodingException {
		if (!force && !ReqRspUtil.needDecoding(s)) return s;
		// if(true) return java.net.URLDecoder.decode(s, enc);

		boolean needToChange = false;
		StringBuilder sb = new StringBuilder();
		int numChars = s.length();
		int i = 0;

		while (i < numChars) {
			char c = s.charAt(i);
			switch (c) {
			case '+':
				sb.append(' ');
				i++;
				needToChange = true;
				break;
			case '%':

				try {
					byte[] bytes = new byte[(numChars - i) / 3];
					int pos = 0;

					while (((i + 2) < numChars) && (c == '%')) {
						bytes[pos++] = (byte) Integer.parseInt(s.substring(i + 1, i + 3), 16);
						i += 3;
						if (i < numChars) c = s.charAt(i);
					}

					if ((i < numChars) && (c == '%')) {
						needToChange = true;
						sb.append(c);
						i++;
						continue;
						// throw new IOException("Incomplete trailing escape (%) pattern");
					}
					sb.append(new String(bytes, 0, pos, enc));
				}
				catch (NumberFormatException e) {
					needToChange = true;
					sb.append(c);
					i++;
					// throw new IOException("Illegal hex characters in escape (%) pattern - " + e.getMessage());
				}
				needToChange = true;
				break;
			default:
				sb.append(c);
				i++;
				break;
			}
		}

		return (needToChange ? sb.toString() : s);
	}
}