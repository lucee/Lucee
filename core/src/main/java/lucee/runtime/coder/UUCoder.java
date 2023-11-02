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
package lucee.runtime.coder;

import java.text.StringCharacterIterator;

/**
 * Unix Coding for java
 */
public final class UUCoder {

	/**
	 * encodes a byte array to a String
	 * 
	 * @param barr
	 * @return encoded String
	 */
	public static String encode(byte barr[]) {
		StringBuilder rtn = new StringBuilder();
		int len = barr.length;
		int read = 0;
		boolean stop = false;
		byte b = 0;
		int offset = 0;

		do {
			int left = len - read;
			if (left == 0) stop = true;

			if (left <= 45) b = (byte) left;
			else b = 45;

			rtn.append(_enc(b));
			for (int i = 0; i < b; i += 3) {
				if (len - offset < 3) {
					byte padding[] = new byte[3];
					for (int z = 0; offset + z < len; z++)
						padding[z] = barr[offset + z];
					encodeBytes(padding, 0, rtn);
				}
				else {
					encodeBytes(barr, offset, rtn);
				}
				offset += 3;
			}

			rtn.append('\n');
			read += b;
			if (b < 45) stop = true;
		}
		while (!stop);
		return rtn.toString();
	}

	/**
	 * decodes back a String to a byte array
	 * 
	 * @param b
	 * @return decoded byte array
	 */
	public static byte[] decode(String str) throws CoderException {
		byte out[] = new byte[str.length()];
		int len = 0;
		int offset = 0;
		// int current = 0;
		byte b = 0;
		boolean stop = false;
		StringCharacterIterator it = new StringCharacterIterator(str);
		do {
			b = _dec(it.current());
			it.next();
			if (b > 45) throw new CoderException("can't decode string [" + str + "]");
			if (b < 45) stop = true;
			len += b;
			for (; b > 0; b -= 3) {
				decodeChars(it, out, offset);
				offset += 3;
			}
			it.next();
		}
		while (!stop);
		byte rtn[] = new byte[len];
		for (int i = 0; i < len; i++)
			rtn[i] = out[i];

		return rtn;
	}

	private static void encodeBytes(byte in[], int off, StringBuilder out) {
		out.append(_enc((byte) (in[off] >>> 2)));
		out.append(_enc((byte) (in[off] << 4 & 0x30 | in[off + 1] >>> 4 & 0xf)));
		out.append(_enc((byte) (in[off + 1] << 2 & 0x3c | in[off + 2] >>> 6 & 3)));
		out.append(_enc((byte) (in[off + 2] & 0x3f)));
	}

	private static void decodeChars(StringCharacterIterator it, byte out[], int off) {
		byte b1 = _dec(it.current());
		byte b2 = _dec(it.next());
		byte b3 = _dec(it.next());
		byte b4 = _dec(it.next());
		it.next();
		byte b5 = (byte) (b1 << 2 | b2 >> 4);
		byte b6 = (byte) (b2 << 4 | b3 >> 2);
		byte b7 = (byte) (b3 << 6 | b4);
		out[off] = b5;
		out[off + 1] = b6;
		out[off + 2] = b7;
	}

	private static char _enc(byte c) {
		return (char) ((c & 0x3f) + 32);
	}

	private static byte _dec(char c) {
		return (byte) (c - 32 & 0x3f);
	}
}