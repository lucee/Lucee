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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.net.http.ReqRspUtil;

public final class URLDecoder {

	private static final char[] HEX = "0123456789ABCDEF".toCharArray();

	private URLDecoder() {
	}

	/**
	 * used by ESAPI extension
	 * 
	 * @param str
	 * @param force
	 * @return
	 */
	public static String decode(String str, boolean force) {
		try {
			return decode(str, SystemUtil.getCharset().name(), force);
		}
		catch (UnsupportedEncodingException | ApplicationException e) {
			return str;
		}
	}

	public static String decode(String s, String enc, boolean force) throws UnsupportedEncodingException, ApplicationException {
		try {
			if (!force && !ReqRspUtil.needDecoding(s)) return s;
			byte[] bytes = decodeEscapes(preprocessInput(s, enc).getBytes(StandardCharsets.US_ASCII));
			return new String(bytes, enc);
		}
		catch (IllegalArgumentException e) {
			ApplicationException ae = new ApplicationException("Invalid URL encoding in string [" + s + "]");
			ExceptionUtil.initCauseEL(ae, e);
			throw ae;
		}
	}

	private static byte[] decodeEscapes(byte[] bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i];
			if (b == '+') baos.write(' ');
			else if (b == '%') {
				if (i + 2 >= bytes.length) throw new IllegalArgumentException("Invalid URL encoding: truncated escape");
				int u = Character.digit((char) bytes[++i], 16);
				int l = Character.digit((char) bytes[++i], 16);
				if (u == -1 || l == -1) throw new IllegalArgumentException("Invalid URL encoding: not a valid digit (radix 16)");
				baos.write((u << 4) + l);
			}
			else baos.write(b);
		}
		return baos.toByteArray();
	}

	// old implementation, simply ignores invalid encoding and strip it out and continues
	public static String decodeLax(String s, String enc, boolean force) throws UnsupportedEncodingException {
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

	public static String preprocessInput(String input, String encoding) {
		StringBuilder result = null;
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
					for (byte b: String.valueOf(c).getBytes(encoding)) {
						result.append('%').append(HEX[(b >> 4) & 0xF]).append(HEX[b & 0xF]);
					}
				}
				else if (result != null) result.append(c);
			}

		}
		catch (UnsupportedEncodingException uee) {
			LogUtil.warn("physical-classloader", uee);
		}
		return result == null ? input : result.toString();
	}
}
