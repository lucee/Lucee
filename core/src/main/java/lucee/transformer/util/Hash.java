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
package lucee.transformer.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lucee.commons.io.log.LogUtil;

/**
 * Class Hash produces a MessageDigest hash for a given string.
 */
public final class Hash {
	private String plainText;
	private String algorithm;

	/**
	 * Method Hash.
	 * 
	 * @param plainText
	 * @param algorithm The algorithm to use like MD2, MD5, SHA-1, etc.
	 */
	public Hash(String plainText, String algorithm) {
		super();
		setPlainText(plainText);
		setAlgorithm(algorithm);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String hashText = null;

		try {
			hashText = Hash.getHashText(this.plainText, this.algorithm);
		}
		catch (NoSuchAlgorithmException nsae) {
			LogUtil.log(null, Hash.class.getName(), nsae);
		}

		return hashText;
	}

	/**
	 * Method getHashText.
	 * 
	 * @param plainText
	 * @param algorithm The algorithm to use like MD2, MD5, SHA-1, etc.
	 * @return String
	 * @throws NoSuchAlgorithmException
	 */
	public static String getHashText(String plainText, String algorithm) throws NoSuchAlgorithmException {
		MessageDigest mdAlgorithm = MessageDigest.getInstance(algorithm);

		mdAlgorithm.update(plainText.getBytes());

		byte[] digest = mdAlgorithm.digest();
		StringBuffer hexString = new StringBuffer();

		for (int i = 0; i < digest.length; i++) {
			plainText = Integer.toHexString(0xFF & digest[i]);

			if (plainText.length() < 2) {
				plainText = "0" + plainText;
			}

			hexString.append(plainText);
		}

		return hexString.toString();
	}

	/**
	 * Returns the algorithm.
	 * 
	 * @return String
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * Returns the plainText.
	 * 
	 * @return String
	 */
	public String getPlainText() {
		return plainText;
	}

	/**
	 * Sets the algorithm.
	 * 
	 * @param algorithm The algorithm to set
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Sets the plainText.
	 * 
	 * @param plainText The plainText to set
	 */
	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

}