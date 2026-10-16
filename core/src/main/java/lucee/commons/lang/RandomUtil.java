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

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public final class RandomUtil {
	public static final char[] CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
			'w', 'x', 'y', 'z' };

	public static final char[] CHARS_LC = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
			'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	private static final byte[] BYTES = toBytes(CHARS);
	private static final byte[] BYTES_LC = toBytes(CHARS_LC);
	private static final SecureRandom SR = new SecureRandom();

	public static String createRandomString(int length) {
		return create(BYTES, length);
	}

	public static String createRandomStringLC(int length) {
		return create(BYTES_LC, length);
	}

	// one nextBytes() per string (each takes the provider's global lock), mapped by rejection sampling so every char is equally likely
	private static String create(byte[] chars, int length) {
		if (length < 1) return "";
		int n = chars.length;
		int limit = 256 - (256 % n);
		byte[] buf = new byte[length + (length >> 3) + 4];
		byte[] out = new byte[length];
		int i = 0;
		while (i < length) {
			SR.nextBytes(buf);
			for (int pos = 0; pos < buf.length && i < length; pos++) {
				int b = buf[pos] & 0xFF;
				if (b < limit) out[i++] = chars[b % n];
			}
		}
		return new String(out, StandardCharsets.ISO_8859_1);
	}

	private static byte[] toBytes(char[] chars) {
		byte[] b = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) b[i] = (byte) chars[i];
		return b;
	}
}