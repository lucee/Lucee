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

import java.util.Base64;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.coder.CoderException;

public final class Base64Encoder {

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
		if (data == null) return "";
		return Base64.getEncoder().encodeToString(data);
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

			for (; i >= 0; i--) {
				c = data.charAt(i);
				if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '+' || c == '/') continue;

				throw new CoderException("invalid character [" + c + "] in base64 string at position [" + (i + 1) + "]");
			}

		}

		if (precise) {
			return Base64.getDecoder().decode(data);
		}
		// Use Apache Commons for lenient decode - handles malformed padding that JDK rejects
		byte[] res = org.apache.commons.codec.binary.Base64.decodeBase64(data);
		if (res == null || res.length == 0) throw new CoderException("cannot convert the input to a binary");
		return res;
	}
}