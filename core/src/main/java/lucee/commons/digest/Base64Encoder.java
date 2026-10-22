/**
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
package lucee.commons.digest;

import java.nio.charset.StandardCharsets;

import java.util.Base64;

import lucee.commons.lang.StringUtil;
import lucee.runtime.coder.CoderException;

public final class Base64Encoder {

	private static final byte[] LENIENT_ALPHABET = new byte[128];

	static {
		java.util.Arrays.fill(LENIENT_ALPHABET, (byte) -1);
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		for (int i = 0; i < chars.length(); i++) {
			LENIENT_ALPHABET[chars.charAt(i)] = (byte) i;
		}
		LENIENT_ALPHABET['-'] = 62;
		LENIENT_ALPHABET['_'] = 63;
	}

	public static String encodeFromString(String data) {
		return encode(data.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Translates the specified byte array into Base64 string.
	 *
	 * @param data the byte array (not null)
	 * @return the translated Base64 string (not null)
	 */
	public static String encode(byte[] data) {
		if (data == null) return "";
		return Base64.getEncoder().encodeToString(data);
	}

	public static String decodeAsString(String data, boolean precise) throws CoderException {
		return new String(decode(data, precise), StandardCharsets.UTF_8);
	}

	/**
	 * Translates the specified Base64 string into a byte array.
	 *
	 * @param data the Base64 string (not null)
	 * @return the byte array (not null)
	 * @throws CoderException
	 */
	public static byte[] decode(String data, boolean precise) throws CoderException {
		if (StringUtil.isEmpty(data)) return new byte[0];
		if (precise) {
			int l = data.length();
			if (((l / 4) * 4) != l) {
				throw new CoderException("cannot convert the input to a binary, invalid length (" + l + ") of the string");
			}

			// A–Z, a–z, 0–9, +, / and =
			char c;
			int i = data.length() - 1;
			int count = 0;
			for (; i >= 0; i--) {
				c = data.charAt(i);
				if (c != '=') break;
				count++;
			}
			if (count > 3) throw new CoderException("invalid padding length [" + count + "], maximal length is [3]");

			// the JDK decoder rejects every char outside the alphabet itself, so the per-char
			// walk only runs on the failure path to produce the positioned message
			try {
				return Base64.getDecoder().decode(data);
			}
			catch (IllegalArgumentException iae) {
				for (; i >= 0; i--) {
					c = data.charAt(i);
					if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '+' || c == '/') continue;

					throw new CoderException("invalid character [" + c + "] in base64 string at position [" + (i + 1) + "]");
				}
				throw new CoderException("cannot convert the input to a binary: " + iae.getMessage());
			}
		}
		// the JDK decoder rejects malformed padding and characters outside the alphabet, here both are
		// skipped, the URL safe alphabet is accepted as well and decoding ends at the first padding
		byte[] res = decodeLenient(data);
		if (res.length == 0) throw new CoderException("cannot convert the input to a binary");
		return res;
	}

	private static byte[] decodeLenient(String data) {
		int len = data.length();
		byte[] out = new byte[(len / 4 + 1) * 3];
		int pos = 0;
		int bits = 0;
		int count = 0;
		for (int i = 0; i < len; i++) {
			char c = data.charAt(i);
			if (c == '=') break;
			int v = c < 128 ? LENIENT_ALPHABET[c] : -1;
			if (v < 0) continue;
			bits = (bits << 6) | v;
			if (++count == 4) {
				out[pos++] = (byte) (bits >> 16);
				out[pos++] = (byte) (bits >> 8);
				out[pos++] = (byte) bits;
				bits = 0;
				count = 0;
			}
		}
		// 6 bits of a single left over character are not enough for a byte
		if (count == 2) out[pos++] = (byte) (bits >> 4);
		else if (count == 3) {
			out[pos++] = (byte) (bits >> 10);
			out[pos++] = (byte) (bits >> 2);
		}
		return java.util.Arrays.copyOf(out, pos);
	}
}