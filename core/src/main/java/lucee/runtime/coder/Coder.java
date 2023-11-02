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

/**
 * 
 */
public final class Coder {

	/**
	 * Field <code>ENCODING_UU</code>
	 */
	public static final short ENCODING_UU = 0;
	/**
	 * Field <code>ENCODING_HEX</code>
	 */
	public static final short ENCODING_HEX = 1;
	/**
	 * Field <code>ENCODING_BASE64</code>
	 */
	public static final short ENCODING_BASE64 = 2;

	/**
	 * @param type
	 * @param value
	 * @return
	 * @throws CoderException
	 */
	public static byte[] decode(String type, String value) throws CoderException {
		type = type.toLowerCase().trim();
		if (type.equals("hex")) return decode(ENCODING_HEX, value);
		if (type.equals("uu")) return decode(ENCODING_UU, value);
		if (type.equals("base64")) return decode(ENCODING_BASE64, value);
		throw new CoderException("Invalid encoding definition [" + type + "]. Valid encodings are [hex, uu, base64].");
	}

	/**
	 * @param type
	 * @param value
	 * @return
	 * @throws CoderException
	 */
	public static byte[] decode(short type, String value) throws CoderException {
		if (type == ENCODING_UU) return UUCoder.decode(value);
		else if (type == ENCODING_HEX) return HexCoder.decode(value);
		else if (type == ENCODING_BASE64) return Base64Coder.decode(value);
		throw new CoderException("Invalid encoding definition");
	}

	/**
	 * @param type
	 * @param value
	 * @return
	 * @throws CoderException
	 */
	public static String encode(String type, byte[] value) throws CoderException {
		type = type.toLowerCase().trim();
		if (type.equals("hex")) return encode(ENCODING_HEX, value);
		if (type.equals("uu")) return encode(ENCODING_UU, value);
		if (type.equals("base64")) return encode(ENCODING_BASE64, value);
		throw new CoderException("Invalid encoding definition [" + type + "]. Valid encodings are [hex, uu, base64].");
	}

	/**
	 * @param type
	 * @param value
	 * @return
	 * @throws CoderException
	 */
	public static String encode(short type, byte[] value) throws CoderException {
		if (type == ENCODING_UU) return UUCoder.encode(value);
		else if (type == ENCODING_HEX) return HexCoder.encode(value);
		else if (type == ENCODING_BASE64) return Base64Coder.encode(value);
		throw new CoderException("Invalid encoding definition");
	}
}