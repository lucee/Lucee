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

import lucee.commons.io.CharsetUtil;

public final class Base64Util {

	private static byte base64Alphabet[];
	private static byte lookUpBase64Alphabet[];

	/**
	 * @param arrayOctect byte array to check
	 * @return true if base64
	 */
	public static boolean isBase64(byte arrayOctect[]) {
		int length = arrayOctect.length;
		if (length == 0) return true;
		for (int i = 0; i < length; i++) {
			if (!isBase64(arrayOctect[i])) return false;
		}
		return true;
	}

	/**
	 * @param octect byte to check
	 * @return true if base64
	 */
	public static boolean isBase64(byte octect) {
		return octect == 61 || base64Alphabet[octect] != -1;
	}

	/**
	 * @param isValidString string to check
	 * @return true if base64
	 */
	public static boolean isBase64(String isValidString) {
		return isBase64(isValidString.getBytes(CharsetUtil.UTF8));
	}

	/** Initializations */
	static {
		base64Alphabet = new byte[255];
		lookUpBase64Alphabet = new byte[64];
		for (int i = 0; i < 255; i++)
			base64Alphabet[i] = -1;
		for (int i = 90; i >= 65; i--)
			base64Alphabet[i] = (byte) (i - 65);
		for (int i = 122; i >= 97; i--)
			base64Alphabet[i] = (byte) ((i - 97) + 26);
		for (int i = 57; i >= 48; i--)
			base64Alphabet[i] = (byte) ((i - 48) + 52);
		base64Alphabet[43] = 62;
		base64Alphabet[47] = 63;
		for (int i = 0; i <= 25; i++)
			lookUpBase64Alphabet[i] = (byte) (65 + i);
		int i = 26;
		for (int j = 0; i <= 51; j++) {
			lookUpBase64Alphabet[i] = (byte) (97 + j);
			i++;
		}
		i = 52;
		for (int j = 0; i <= 61; j++) {
			lookUpBase64Alphabet[i] = (byte) (48 + j);
			i++;
		}
		lookUpBase64Alphabet[62] = 43;
		lookUpBase64Alphabet[63] = 47;
	}
}