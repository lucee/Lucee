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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class Base64Util {

	private static final Base64.Encoder base64Encoder = Base64.getEncoder();
	private static final Base64.Decoder base64Decoder = Base64.getDecoder();
	private static final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder base64UrlDecoder = Base64.getUrlDecoder();

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

	/**
	 * creates a new random UUID and encodes it as a URL-safe Bas64 string
	 *
	 * @return a 22 character string
	 */
	public static String createUuidAsBase64() {
		return encodeUuidAsBase64(UUID.randomUUID());
	}

	/**
	 * encodes a 36 character long UUID string as a URL-safe Base64 string
	 *
	 * @param uuid a UUID in the format xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
	 * @return a 22 character string
	 */
	public static String encodeUuidAsBase64(String uuid) {
		return encodeUuidAsBase64(UUID.fromString(uuid));
	}

	/**
	 * encodes a UUID object as a URL-safe Base64 string
	 *
	 * @param uuid a java.util.UUID object
	 * @return a 22 character string
	 */
	public static String encodeUuidAsBase64(UUID uuid) {
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		byte[] barr = bb.array();
		byte[] benc = base64UrlEncoder.encode(barr);
		return new String(benc, 0, benc.length, StandardCharsets.US_ASCII);
	}

	/**
	 * decodes a 22 character long Base64 string to a UUID string
	 *
	 * @param base64 a 22 character long string
	 * @return a 36 character UUID string in the format xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
	 */
	public static String decodeBase64AsUuid(String base64) {
		byte[] barr = base64.getBytes(StandardCharsets.US_ASCII);
		byte[] bdec = base64UrlDecoder.decode(barr);
		ByteBuffer buffer = ByteBuffer.wrap(bdec);
		UUID uuid = new UUID(buffer.getLong(), buffer.getLong());
		return uuid.toString();
	}

	/**
	 * encodes a number as a Base64 string, e.g. 9876543210 => AkywFuo
	 *
	 * @param number a string that represents a whole number
	 * @return a URL-safe Base64 string
	 */
	public static String encodeNumberAsBase64(String number) {
		BigInteger bint = new BigInteger(number);
		byte[] barr = bint.toByteArray();
		byte[] benc = base64UrlEncoder.encode(barr);
		String result = new String(benc, 0, benc.length, StandardCharsets.US_ASCII);
		return result;
	}

	/**
	 * decodes a Base64 string to a string that represents a number, e.g. AkywFuo => 9876543210
	 *
	 * @param base64 the Base64 string
	 * @return a string representation of the decoded number
	 */
	public static String decodeBase64AsNumber(String base64) {
		byte[] barr = base64.getBytes(StandardCharsets.US_ASCII);
		byte[] bdec = base64UrlDecoder.decode(barr);
		ByteBuffer buffer = ByteBuffer.wrap(bdec);
		BigInteger bint = new BigInteger(buffer.array());
		return bint.toString();
	}

	public static byte[] base64Encode(byte[] barr, boolean urlSafe) {
		Base64.Encoder encoder = urlSafe ? base64UrlEncoder : base64Encoder;
		return encoder.encode(barr);
	}

	public static byte[] base64Decode(String b64, boolean urlSafe) {
		Base64.Decoder decoder = urlSafe ? base64UrlDecoder : base64Decoder;
		return decoder.decode(b64);
	}

}