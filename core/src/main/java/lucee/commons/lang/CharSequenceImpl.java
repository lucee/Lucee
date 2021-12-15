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

public class CharSequenceImpl implements CharSequence {

	private final char[] chars;
	private final String str;
	private final String lcStr;

	/**
	 * Constructor of the class
	 * 
	 * @param chars
	 */
	public CharSequenceImpl(char[] chars) {
		this.str = new String(chars);
		this.chars = chars;

		char c;
		for (int i = 0; i < chars.length; i++) {
			c = chars[i];
			if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))) {
				lcStr = str.toLowerCase();
				return;
			}
		}
		lcStr = str;
	}

	/**
	 * Constructor of the class
	 * 
	 * @param str
	 */
	public CharSequenceImpl(String str) {
		this(str.toCharArray());
	}

	@Override
	public char charAt(int index) {
		return chars[index];
	}

	@Override
	public int length() {
		return chars.length;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		char[] dest = new char[end - start];
		System.arraycopy(chars, start, dest, 0, end - start);
		return new CharSequenceImpl(dest);
	}

	@Override
	public String toString() {
		return str;
	}

	public String toLowerCaseString() {
		return lcStr;
	}
}