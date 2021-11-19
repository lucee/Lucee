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
package lucee.runtime.crypt;

public final class BinConverter {

	/**
	 * gets bytes from an array into a long
	 * 
	 * @param buffer where to get the bytes
	 * @param nStartIndex index from where to read the data
	 * @return the 64bit integer
	 */
	public static long byteArrayToLong(byte[] buffer, int nStartIndex) {

		return (((long) buffer[nStartIndex]) << 56) | ((buffer[nStartIndex + 1] & 0x0ffL) << 48) | ((buffer[nStartIndex + 2] & 0x0ffL) << 40)
				| ((buffer[nStartIndex + 3] & 0x0ffL) << 32) | ((buffer[nStartIndex + 4] & 0x0ffL) << 24) | ((buffer[nStartIndex + 5] & 0x0ffL) << 16)
				| ((buffer[nStartIndex + 6] & 0x0ffL) << 8) | ((long) buffer[nStartIndex + 7] & 0x0ff);

	}

	/**
	 * converts a long o bytes which are put into a given array
	 * 
	 * @param lValue the 64bit integer to convert
	 * @param buffer the target buffer
	 * @param nStartIndex where to place the bytes in the buffer
	 */
	public static void longToByteArray(long lValue, byte[] buffer, int nStartIndex) {
		buffer[nStartIndex] = (byte) (lValue >>> 56);
		buffer[nStartIndex + 1] = (byte) ((lValue >>> 48) & 0x0ff);
		buffer[nStartIndex + 2] = (byte) ((lValue >>> 40) & 0x0ff);
		buffer[nStartIndex + 3] = (byte) ((lValue >>> 32) & 0x0ff);
		buffer[nStartIndex + 4] = (byte) ((lValue >>> 24) & 0x0ff);
		buffer[nStartIndex + 5] = (byte) ((lValue >>> 16) & 0x0ff);
		buffer[nStartIndex + 6] = (byte) ((lValue >>> 8) & 0x0ff);
		buffer[nStartIndex + 7] = (byte) lValue;
	}

	/**
	 * converts values from an integer array to a long
	 * 
	 * @param buffer where to get the bytes
	 * @param nStartIndex index from where to read the data
	 * @return the 64bit integer
	 */
	public static long intArrayToLong(int[] buffer, int nStartIndex) {
		return (((long) buffer[nStartIndex]) << 32) | ((buffer[nStartIndex + 1]) & 0x0ffffffffL);
	}

	/**
	 * converts a long to integers which are put into a given array
	 * 
	 * @param lValue the 64bit integer to convert
	 * @param buffer the target buffer
	 * @param nStartIndex where to place the bytes in the buffer
	 */
	public static void longToIntArray(long lValue, int[] buffer, int nStartIndex) {
		buffer[nStartIndex] = (int) (lValue >>> 32);
		buffer[nStartIndex + 1] = (int) lValue;
	}

	/**
	 * makes a long from two integers (treated unsigned)
	 * 
	 * @param nLo lower 32bits
	 * @param nHi higher 32bits
	 * @return the built long
	 */
	public static long makeLong(int nLo, int nHi) {
		return (((long) nHi << 32) | (nLo & 0x00000000ffffffffL));
	}

	/**
	 * gets the lower 32 bits of a long
	 * 
	 * @param lVal the long integer
	 * @return lower 32 bits
	 */
	public static int longLo32(long lVal) {
		return (int) lVal;
	}

	/**
	 * gets the higher 32 bits of a long
	 * 
	 * @param lVal the long integer
	 * @return higher 32 bits
	 */
	public static int longHi32(long lVal) {
		return (int) ((lVal >>> 32));
	}

	final static char[] HEXTAB = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * converts a byte array to a binhex string
	 * 
	 * @param data the byte array
	 * @return the binhex string
	 */
	public static String bytesToBinHex(byte[] data) {
		return bytesToBinHex(data, 0, data.length);
	}

	/**
	 * converts a byte array to a binhex string
	 * 
	 * @param data the byte array
	 * @param nStartPos start index where to get the bytes
	 * @param nNumOfBytes number of bytes to convert
	 * @return the binhex string
	 */
	public static String bytesToBinHex(byte[] data, int nStartPos, int nNumOfBytes) {
		StringBuilder sbuf = new StringBuilder();
		sbuf.setLength(nNumOfBytes << 1);
		int nPos = 0;
		for (int nI = 0; nI < nNumOfBytes; nI++) {
			sbuf.setCharAt(nPos++, HEXTAB[(data[nI + nStartPos] >> 4) & 0x0f]);
			sbuf.setCharAt(nPos++, HEXTAB[data[nI + nStartPos] & 0x0f]);
		}
		return sbuf.toString();
	}

	/**
	 * converts a binhex string back into a byte array (invalid codes will be skipped)
	 * 
	 * @param sBinHex binhex string
	 * @param data the target array
	 * @param nSrcPos from which character in the string the conversion should begin, remember that
	 *            (nSrcPos modulo 2) should equals 0 normally
	 * @param nDstPos to store the bytes from which position in the array
	 * @param nNumOfBytes number of bytes to extract
	 * @return number of extracted bytes
	 */
	public static int binHexToBytes(String sBinHex, byte[] data, int nSrcPos, int nDstPos, int nNumOfBytes) {
		// check for correct ranges
		int nStrLen = sBinHex.length();
		int nAvailBytes = (nStrLen - nSrcPos) >> 1;
		if (nAvailBytes < nNumOfBytes) nNumOfBytes = nAvailBytes;
		int nOutputCapacity = data.length - nDstPos;
		if (nNumOfBytes > nOutputCapacity) nNumOfBytes = nOutputCapacity;
		// convert now
		int nResult = 0;
		for (int nI = 0; nI < nNumOfBytes; nI++) {
			byte bActByte = 0;
			boolean blConvertOK = true;
			for (int nJ = 0; nJ < 2; nJ++) {
				bActByte <<= 4;
				char cActChar = sBinHex.charAt(nSrcPos++);
				if ((cActChar >= 'a') && (cActChar <= 'f')) bActByte |= (byte) (cActChar - 'a') + 10;
				else if ((cActChar >= '0') && (cActChar <= '9')) bActByte |= (byte) (cActChar - '0');
				else blConvertOK = false;
			}
			if (blConvertOK) {
				data[nDstPos++] = bActByte;
				nResult++;
			}
		}
		return nResult;
	}

	/**
	 * converts a byte array into an UNICODE string
	 * 
	 * @param data the byte array
	 * @param nStartPos where to begin the conversion
	 * @param nNumOfBytes number of bytes to handle
	 * @return the string
	 */
	public static String byteArrayToUNCString(byte[] data, int nStartPos, int nNumOfBytes) {
		// we need two bytes for every character
		nNumOfBytes &= ~1;
		// enough bytes in the buffer?
		int nAvailCapacity = data.length - nStartPos;
		if (nAvailCapacity < nNumOfBytes) nNumOfBytes = nAvailCapacity;
		StringBuilder sbuf = new StringBuilder();
		sbuf.setLength(nNumOfBytes >> 1);
		int nSBufPos = 0;
		while (nNumOfBytes > 0) {
			sbuf.setCharAt(nSBufPos++, (char) ((data[nStartPos] << 8) | (data[nStartPos + 1] & 0x0ff)));
			nStartPos += 2;
			nNumOfBytes -= 2;
		}
		return sbuf.toString();
	}
}