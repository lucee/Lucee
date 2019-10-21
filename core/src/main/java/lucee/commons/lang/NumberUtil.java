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

import lucee.runtime.exp.ExpressionException;
import java.io.*;

public class NumberUtil {
	public static int hexToInt(String s, int defaultValue) {
		try {
			return hexToInt(s);
		}
		catch (ExpressionException e) {
			return defaultValue;
		}
	}

	public static int hexToInt(String s) throws ExpressionException {
		s = s.toLowerCase();
		int[] n = new int[s.length()];
		char c;
		int sum = 0;
		int koef = 1;
		for (int i = n.length - 1; i >= 0; i--) {
			c = s.charAt(i);

			if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
				throw new ExpressionException("invalid hex constant [" + c + "], hex constants are [0-9,a-f]");
			}

			switch (c) {
			case 48:
				n[i] = 0;
				break;
			case 49:
				n[i] = 1;
				break;
			case 50:
				n[i] = 2;
				break;
			case 51:
				n[i] = 3;
				break;
			case 52:
				n[i] = 4;
				break;
			case 53:
				n[i] = 5;
				break;
			case 54:
				n[i] = 6;
				break;
			case 55:
				n[i] = 7;
				break;
			case 56:
				n[i] = 8;
				break;
			case 57:
				n[i] = 9;
				break;
			case 97:
				n[i] = 10;
				break;
			case 98:
				n[i] = 11;
				break;
			case 99:
				n[i] = 12;
				break;
			case 100:
				n[i] = 13;
				break;
			case 101:
				n[i] = 14;
				break;
			case 102:
				n[i] = 15;
				break;
			}

			sum = sum + n[i] * koef;
			koef = koef * 16;
		}
		return sum;
	}

	public static byte[] longToByteArray(long l) {
		byte[] ba = new byte[8];
		for (int i = 0; i < 64; i += 8) {
			ba[i >> 3] = new Long((l & (255L << i)) >> i).byteValue();
		}
		return ba;
	}

	public static long byteArrayToLong(byte[] ba) {
		long l = 0;
		for (int i = 0; (i < 8) && (i < 8); i++) {
			l |= (((long) ba[i]) << (i << 3)) & (255L << (i << 3));
		}
		return l;
	}

	public static int randomRange(int min, int max) {
		return min + (int) (Math.random() * ((max - min) + 1));
	}
}