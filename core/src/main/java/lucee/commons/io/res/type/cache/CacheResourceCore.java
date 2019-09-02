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
package lucee.commons.io.res.type.cache;

import java.io.Serializable;

/**
 * Core of a Ram Resource, holds the concrete data for an existing resource
 */
public final class CacheResourceCore implements Serializable {

	/**
	 * Directory Resource
	 */
	public static final int TYPE_DIRECTORY = 1;

	/**
	 * Directory Resource
	 */
	public static final int TYPE_FILE = 2;

	private int type;
	private String name;
	private byte[] data;
	private long lastModified = System.currentTimeMillis();

	private int mode = 0777;
	private int attributes = 0;

	private String path;

	/**
	 * Konstruktor
	 * 
	 * @param parent
	 * @param type
	 * @param name
	 * @param caseSensitive
	 */
	public CacheResourceCore(int type, String path, String name) {
		this.type = type;
		this.path = path;
		this.name = name;
	}

	/**
	 * Gibt den Feldnamen lastModified zurueck.
	 * 
	 * @return lastModified
	 */
	public long getLastModified() {
		return this.lastModified;
	}

	/**
	 * Setzt den Feldnamen lastModified.
	 * 
	 * @param lastModified lastModified
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Gibt den Feldnamen data zurueck.
	 * 
	 * @return data
	 */
	public byte[] getData() {
		return this.data;
	}

	/**
	 * Setzt den Feldnamen data.
	 * 
	 * @param data data
	 * @param append
	 */
	public void setData(byte[] data, boolean append) {
		lastModified = System.currentTimeMillis();

		// set data
		if (append) {
			if (this.data != null && data != null) {
				byte[] newData = new byte[this.data.length + data.length];
				int i = 0;
				for (; i < this.data.length; i++) {
					newData[i] = this.data[i];
				}
				for (; i < this.data.length + data.length; i++) {
					newData[i] = data[i - this.data.length];
				}
				this.data = newData;
			}
			else if (data != null) {
				this.data = data;
			}
		}
		else {
			this.data = data;
		}

		// set type
		if (this.data != null) this.type = TYPE_FILE;

	}

	/**
	 * Gibt den Feldnamen name zurueck.
	 * 
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Setzt den Feldnamen name.
	 * 
	 * @param name name
	 */
	public void setName(String name) {
		lastModified = System.currentTimeMillis();
		this.name = name;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Gibt den Feldnamen type zurueck.
	 * 
	 * @return type
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Setzt den Feldnamen type.
	 * 
	 * @param type type
	 */
	public void setType(int type) {
		lastModified = System.currentTimeMillis();
		this.type = type;
	}

	/**
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getAttributes() {
		return attributes;
	}

	public void setAttributes(int attributes) {
		this.attributes = attributes;
	}

	public void remove() {
		setType(0);
		setData(null, false);
	}

}