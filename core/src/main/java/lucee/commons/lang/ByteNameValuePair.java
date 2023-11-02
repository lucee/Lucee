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

import java.io.UnsupportedEncodingException;

/**
 * Name Value Pair
 */
public final class ByteNameValuePair {

	private byte[] name;
	private byte[] value;
	private boolean urlEncoded;

	/**
	 * constructor of the class
	 * 
	 * @param name
	 * @param value
	 */
	public ByteNameValuePair(byte[] name, byte[] value, boolean urlEncoded) {
		this.name = name;
		this.value = value;
		this.urlEncoded = urlEncoded;
	}

	/**
	 * @return Returns the name.
	 */
	public byte[] getName() {
		return name;
	}

	/**
	 * @param encoding
	 * @return Returns the name.
	 * @throws UnsupportedEncodingException
	 */
	public String getName(String encoding) throws UnsupportedEncodingException {
		return new String(name, encoding);
	}

	/**
	 * @param encoding
	 * @param defaultValue
	 * @return Returns the name.
	 */
	public String getName(String encoding, String defaultValue) {
		try {
			return new String(name, encoding);
		}
		catch (UnsupportedEncodingException e) {
			return defaultValue;
		}
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(byte[] name) {
		this.name = name;
	}

	/**
	 * @return Returns the value.
	 */
	public byte[] getValue() {
		return value;
	}

	/**
	 * @param encoding
	 * @return Returns the name.
	 * @throws UnsupportedEncodingException
	 */
	public String getValue(String encoding) throws UnsupportedEncodingException {
		return new String(value, encoding);
	}

	/**
	 * @param encoding
	 * @param defaultValue
	 * @return Returns the name.
	 */
	public String getValue(String encoding, String defaultValue) {
		try {
			return new String(value, encoding);
		}
		catch (UnsupportedEncodingException e) {
			return defaultValue;
		}
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}

	/**
	 * @return the urlEncoded
	 */
	public boolean isUrlEncoded() {
		return urlEncoded;
	}

}