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
package lucee.runtime.security;

import java.util.Random;

import lucee.runtime.crypt.BinConverter;
import lucee.runtime.crypt.BlowfishCBC;
import lucee.runtime.crypt.BlowfishECB;
import lucee.runtime.crypt.SHA1;

/**
 * support class for easy string encryption with the Blowfish algorithm, now in CBC mode with a
 * SHA-1 key setup and correct padding
 */
public final class SerialDecoder {

	BlowfishCBC m_bfish;

	// one random generator for all simple callers...

	static Random m_rndGen;

	// ...and created early

	static {

		m_rndGen = new Random();

	}

	/**
	 * 
	 * constructor to set up a string as the key (oversized password will be cut)
	 * 
	 * @param sPassword the password (treated as a real unicode array)
	 * 
	 */

	public SerialDecoder(String sPassword) {

		// hash down the password to a 160bit key

		SHA1 hasher = new SHA1();

		hasher.update(sPassword);

		hasher.finalize();

		// setup the encryptor (use a dummy IV)

		m_bfish = new BlowfishCBC(hasher.getDigest(), 0);

		hasher.clear();

	}

	/**
	 * 
	 * decrypts a hexbin string (handling is case sensitive)
	 * 
	 * @param sCipherText hexbin string to decrypt
	 * 
	 * @return decrypted string (null equals an error)
	 * 
	 */

	public String decrypt(String sCipherText) {

		// get the number of estimated bytes in the string (cut off broken blocks)

		int nLen = (sCipherText.length() >> 1) & ~7;

		// does the given stuff make sense (at least the CBC IV)?

		if (nLen < BlowfishECB.BLOCKSIZE)

			return null;

		// get the CBC IV

		byte[] cbciv = new byte[BlowfishECB.BLOCKSIZE];

		int nNumOfBytes = BinConverter.binHexToBytes(sCipherText,

				cbciv,

				0,

				0,

				BlowfishECB.BLOCKSIZE);

		if (nNumOfBytes < BlowfishECB.BLOCKSIZE)

			return null;

		// (got it)

		m_bfish.setCBCIV(cbciv);

		// something left to decrypt?

		nLen -= BlowfishECB.BLOCKSIZE;

		if (nLen == 0)

			return "";

		// get all data bytes now

		byte[] buf = new byte[nLen];

		nNumOfBytes = BinConverter.binHexToBytes(sCipherText,

				buf,

				BlowfishECB.BLOCKSIZE * 2,

				0,

				nLen);

		// we cannot accept broken binhex sequences due to padding

		// and decryption

		if (nNumOfBytes < nLen)

			return null;

		// decrypt the buffer

		m_bfish.decrypt(buf);

		// get the last padding byte

		int nPadByte = buf[buf.length - 1] & 0x0ff;

		// ( try to get all information if the padding doesn't seem to be correct)

		if ((nPadByte > 8) || (nPadByte < 0))

			nPadByte = 0;

		// calculate the real size of this message

		nNumOfBytes -= nPadByte;

		if (nNumOfBytes < 0)

			return "";

		// success

		return BinConverter.byteArrayToUNCString(buf, 0, nNumOfBytes);

	}

	/**
	 * 
	 * destroys (clears) the encryption engine,
	 * 
	 * after that the instance is not valid anymore
	 * 
	 */

	public void destroy() {

		m_bfish.cleanUp();

	}

}