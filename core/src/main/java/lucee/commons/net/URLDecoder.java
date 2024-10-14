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
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
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
		try {
			URLCodec codec = new URLCodec(enc);
			return codec.decode(preprocessInput(codec, s, enc));
		}
		catch (DecoderException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		}
	}

	public static String preprocessInput(URLCodec codec, String input, String encoding) {
		StringBuilder result = null;
		boolean hasEncoding;
		if (StringUtil.isEmpty(encoding, true)) {
			encoding = encoding.trim();
			hasEncoding = true;
		}
		else {
			hasEncoding = false;
		}
		try {
			int length = input.length();

			for (int i = 0; i < length; i++) {
				char c = input.charAt(i);

				if (c > 127) {
					if (result == null) {
						result = new StringBuilder();
						if (i > 0) {
							result.append(input.subSequence(0, i));
						}
					}
					if (hasEncoding) result.append(codec.encode(String.valueOf(c), encoding));
					else result.append(codec.encode(String.valueOf(c)));
				}
				else if (result != null) result.append(c);
			}

		}
		catch (UnsupportedEncodingException | EncoderException uee) {
		}
		return result == null ? input : result.toString();
	}
}
