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
package lucee.commons.net;

/**
 * Name Value Pair
 */
public final class URLItem {

	private String name;
	private String value;
	private boolean urlEncoded;

	/**
	 * @param name
	 * @param value
	 * @param isURLEncoded
	 */
	public URLItem(String name, String value, boolean urlEncoded) {
		this.name = name;
		this.value = value;
		this.urlEncoded = urlEncoded;

	}
	/*
	 * public URLItem(String name, byte[] value, boolean urlEncoded) { this.name = name; //this.value =
	 * value; this.urlEncoded=urlEncoded; }
	 */

	/**
	 * @return the urlEncoded
	 */
	public boolean isUrlEncoded() {
		return urlEncoded;
	}

	/**
	 * @param urlEncoded the urlEncoded to set
	 */
	public void setUrlEncoded(boolean urlEncoded) {
		this.urlEncoded = urlEncoded;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}

}