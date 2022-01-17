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

import java.io.UnsupportedEncodingException;

import lucee.commons.digest.Base64Encoder;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.op.Caster;

/**
 * Util class to handle Base 64 Encoded Strings
 */
public final class Base64Coder {

	/**
	 * decodes a Base64 String to a Plain String
	 * 
	 * @param encoded
	 * @return
	 * @throws ExpressionException
	 */
	public static String decodeToString(String encoded, String charset, boolean percise) throws CoderException, UnsupportedEncodingException {
		byte[] dec = decode(Caster.toString(encoded, null), percise);
		return new String(dec, charset);
	}

	/**
	 * encodes a String to Base64 String
	 * 
	 * @param plain String to encode
	 * @return encoded String
	 * @throws CoderException
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeFromString(String plain, String charset) throws CoderException, UnsupportedEncodingException {
		return encode(plain.getBytes(charset));
	}

	/**
	 * encodes a byte array to Base64 String
	 * 
	 * @param barr byte array to encode
	 * @return encoded String
	 * @throws CoderException
	 */
	public static String encode(byte[] barr) {
		return Base64Encoder.encode(barr);
	}

	/**
	 * decodes a Base64 String to a Plain String
	 * 
	 * @param encoded
	 * @return decoded binary data
	 * @throws CoderException
	 */
	public static byte[] decode(String encoded, boolean percise) throws CoderException {
		return Base64Encoder.decode(encoded, percise);
	}
}