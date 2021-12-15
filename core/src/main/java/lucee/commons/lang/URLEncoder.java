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
package lucee.commons.lang;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import org.apache.commons.codec.net.URLCodec;

import lucee.commons.io.CharsetUtil;

/**
 * @deprecated use instead lucee.commons.net.URLEncoder
 * 
 */
@Deprecated
public class URLEncoder {

	private static final BitSet WWW_FORM_URL = new BitSet(256);

	static {
		// alpha characters
		for (int i = 'a'; i <= 'z'; i++) {
			WWW_FORM_URL.set(i);
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			WWW_FORM_URL.set(i);
		}
		// numeric characters
		for (int i = '0'; i <= '9'; i++) {
			WWW_FORM_URL.set(i);
		}
	}

	public static String encode(String str, java.nio.charset.Charset charset) throws UnsupportedEncodingException {
		return new String(URLCodec.encodeUrl(WWW_FORM_URL, str.getBytes(charset)), "us-ascii");
	}

	public static String encode(String str, String encoding) throws UnsupportedEncodingException {
		return new String(URLCodec.encodeUrl(WWW_FORM_URL, str.getBytes(encoding)), "us-ascii");
	}

	public static String encode(String str) throws UnsupportedEncodingException {
		return encode(str, CharsetUtil.UTF8);
	}
}