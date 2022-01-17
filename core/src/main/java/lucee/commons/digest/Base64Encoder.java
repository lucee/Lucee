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

import java.util.HashMap;
import java.util.Map;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.coder.CoderException;

public class Base64Encoder {

	private static final char[] ALPHABET = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
			'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', '+', '/' };
	private static final char PAD = '=';

	private static final Map<Character, Integer> REVERSE = new HashMap<Character, Integer>();
	static {
		for (int i = 0; i < 64; i++) {
			REVERSE.put(ALPHABET[i], i);
		}
		REVERSE.put('-', 62);
		REVERSE.put('_', 63);
		REVERSE.put(PAD, 0);
	}

	public static String encodeFromString(String data) {
		return encode(data.getBytes(CharsetUtil.UTF8));
	}

	/**
	 * Translates the specified byte array into Base64 string.
	 *
	 * @param data the byte array (not null)
	 * @return the translated Base64 string (not null)
	 */
	public static String encode(byte[] data) {
		StringBuilder builder = new StringBuilder();
		for (int position = 0; position < data.length; position += 3) {
			builder.append(encodeGroup(data, position));
		}
		return builder.toString();
	}

	//// Helper methods

	/**
	 * Encode three bytes of data into four characters.
	 */
	private static char[] encodeGroup(byte[] data, int position) {
		final char[] c = new char[] { '=', '=', '=', '=' };
		int b1 = 0, b2 = 0, b3 = 0;
		int length = data.length - position;

		if (length == 0) return c;

		if (length >= 1) {
			b1 = (data[position]) & 0xFF;
		}
		if (length >= 2) {
			b2 = (data[position + 1]) & 0xFF;
		}
		if (length >= 3) {
			b3 = (data[position + 2]) & 0xFF;
		}

		c[0] = ALPHABET[b1 >> 2];
		c[1] = ALPHABET[(b1 & 3) << 4 | (b2 >> 4)];
		if (length == 1) return c;
		c[2] = ALPHABET[(b2 & 15) << 2 | (b3 >> 6)];
		if (length == 2) return c;
		c[3] = ALPHABET[b3 & 0x3f];
		return c;
	}

	public static String decodeAsString(String data, boolean precise) throws CoderException {
		return new String(decode(data, precise), CharsetUtil.UTF8);
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
		}

		byte[] res = org.apache.commons.codec.binary.Base64.decodeBase64(data);
		if (res == null || res.length == 0) throw new CoderException("cannot convert the input to a binary");
		return res;
	}

}