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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

import lucee.commons.io.SystemUtil;

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
		try {
			if (! force) {
				if (s.indexOf('%') == -1) {
					// no chance of URL encoding in this string.
					return s;
				}
			}
			URLCodec codec = new URLCodec(enc);
			String str;
			str = codec.decode(s);
			return str;	
		}
		catch (DecoderException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		}
	}
}
