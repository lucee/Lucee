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

		byte bytes[] = new byte[s.length()];
		int pos = 0;
		int numChars = s.length();
		boolean needToChange = false;
		int i = 0;
		while (i < numChars) {
			char c = s.charAt(i);
			switch (c) {
			case '+':
				bytes[pos++] = (byte) ' ';
				i++;
				needToChange = true;
				break;
			case '%':
				try {
					if ((i + 2) < numChars) {
						/* next line may raise an exception */
						bytes[pos] = (byte) Integer.parseInt(s.substring(i + 1, i + 3), 16);
						pos++;
						i += 3;
						needToChange = true;
					}
					else {
						bytes[pos++] = (byte) c;
						i++;
					}
				}
				catch (NumberFormatException e) {
					bytes[pos++] = (byte) c;
					i++;
				}
				break;
			default:
				bytes[pos++] = (byte) c;
				i++;
				break;
			}
		}

		return (needToChange ? new String(bytes, 0, pos, enc) : s);
	}
}
