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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * class to handle characters, similar to StringBuffer, but dont copy big blocks of char arrays.
 */
public class CharBuffer {

	private final static int BLOCK_LENGTH = 1024;
	private char buffer[];
	private int pos = 0;
	private int length = 0;
	private final Entity root = new Entity(null);
	private Entity curr = root;

	/**
	 * default constructor
	 */
	public CharBuffer() {
		this(BLOCK_LENGTH);
	}

	/**
	 * constructor with size of the buffer
	 * 
	 * @param size
	 */
	public CharBuffer(int size) {
		buffer = new char[size];
	}

	public void append(char c) {
		append(new char[] { c });
	}

	/**
	 * method to append a char array to the buffer
	 * 
	 * @param c char array to append
	 */
	public void append(char c[]) {
		int maxlength = buffer.length - pos;
		if (c.length < maxlength) {
			System.arraycopy(c, 0, buffer, pos, c.length);
			pos += c.length;
		}
		else {
			System.arraycopy(c, 0, buffer, pos, maxlength);
			curr.next = new Entity(buffer);
			curr = curr.next;
			length += buffer.length;
			buffer = new char[(buffer.length > c.length - maxlength) ? buffer.length : c.length - maxlength];
			if (c.length > maxlength) {
				System.arraycopy(c, maxlength, buffer, 0, c.length - maxlength);
				pos = c.length - maxlength;
			}
			else {
				pos = 0;
			}
		}
	}

	/**
	 * method to append a part of a char array
	 * 
	 * @param c char array to get part from
	 * @param off start index on the char array
	 * @param len length of the sequenz to get from array
	 */
	public void append(char c[], int off, int len) {
		int restLength = buffer.length - pos;
		if (len < restLength) {
			System.arraycopy(c, off, buffer, pos, len);
			pos += len;
		}
		else {
			System.arraycopy(c, off, buffer, pos, restLength);
			curr.next = new Entity(buffer);
			curr = curr.next;
			length += buffer.length;
			buffer = new char[(buffer.length > len - restLength) ? buffer.length : len - restLength];

			System.arraycopy(c, off + restLength, buffer, 0, len - restLength);
			pos = len - restLength;

		}
	}

	/**
	 * Method to append a string to char buffer
	 * 
	 * @param str String to append
	 */
	public void append(String str) {
		if (str == null) return;
		int restLength = buffer.length - pos;
		if (str.length() < restLength) {
			str.getChars(0, str.length(), buffer, pos);
			pos += str.length();
		}
		else {
			str.getChars(0, restLength, buffer, pos);
			curr.next = new Entity(buffer);
			curr = curr.next;
			length += buffer.length;
			buffer = new char[(buffer.length > str.length() - restLength) ? buffer.length : str.length() - restLength];

			str.getChars(restLength, str.length(), buffer, 0);
			pos = str.length() - restLength;

		}
	}

	/**
	 * method to append a part of a String
	 * 
	 * @param str string to get part from
	 * @param off start index on the string
	 * @param len length of the sequenz to get from string
	 */
	public void append(String str, int off, int len) {
		int restLength = buffer.length - pos;
		if (len < restLength) {
			str.getChars(off, off + len, buffer, pos);
			pos += len;
		}
		else {
			str.getChars(off, off + restLength, buffer, pos);
			curr.next = new Entity(buffer);
			curr = curr.next;
			length += buffer.length;
			buffer = new char[(buffer.length > len - restLength) ? buffer.length : len - restLength];

			str.getChars(off + restLength, off + len, buffer, 0);
			pos = len - restLength;

		}
	}

	/**
	 * method to writeout content of the char buffer in a writer, this is faster than get char array
	 * with (toCharArray()) and write this in writer.
	 * 
	 * @param writer writer to write inside
	 * @throws IOException
	 */
	public void writeOut(Writer writer) throws IOException {

		Entity e = root;
		while (e.next != null) {
			e = e.next;
			writer.write(e.data);
		}
		writer.write(buffer, 0, pos);
	}

	public void writeOut(OutputStream os, String charset) throws IOException {
		Entity e = root;
		while (e.next != null) {
			e = e.next;
			os.write(new String(e.data).getBytes(charset));
		}
		os.write(new String(buffer, 0, pos).getBytes(charset));
	}

	/**
	 * return content of the Char Buffer as char array
	 * 
	 * @return char array
	 */
	public char[] toCharArray() {
		Entity e = root;
		char[] chrs = new char[size()];
		int off = 0;
		while (e.next != null) {
			e = e.next;
			System.arraycopy(e.data, 0, chrs, off, e.data.length);
			off += e.data.length;
		}
		System.arraycopy(buffer, 0, chrs, off, pos);
		return chrs;
	}

	@Override
	public String toString() {
		return new String(toCharArray());
	}

	/**
	 * clear the content of the buffer
	 */
	public void clear() {
		if (size() == 0) return;
		buffer = new char[buffer.length];
		root.next = null;
		pos = 0;
		length = 0;
		curr = root;
	}

	/**
	 * @return returns the size of the content of the buffer
	 */
	public int size() {
		return length + pos;
	}

	private class Entity {
		private char[] data;
		private Entity next;

		private Entity(char[] data) {
			this.data = data;
		}
	}

	public byte[] getBytes(String characterEncoding) throws UnsupportedEncodingException {

		return toString().getBytes(characterEncoding);

	}

}