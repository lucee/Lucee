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
package lucee.commons.io.res.type.ram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Core of a Ram Resource, holds the concrete data for an existing resource
 */
public final class RamResourceCore implements Serializable {

	/**
	 * Directory Resource
	 */
	public static final int TYPE_DIRECTORY = 1;

	/**
	 * Directory Resource
	 */
	public static final int TYPE_FILE = 2;

	private static final String[] EMPTY_NAMES = new String[0];

	private int type;
	private String name;
	private byte[] data;
	private List children;
	private long lastModified = System.currentTimeMillis();

	private int mode = 0777;
	private int attributes = 0;

	private RamResourceCore parent;

	/**
	 * Konstruktor
	 * 
	 * @param parent
	 * @param type
	 * @param name
	 * @param caseSensitive
	 */
	public RamResourceCore(RamResourceCore parent, int type, String name) {
		if (parent != null) {
			parent.addChild(this);
		}
		this.parent = parent;
		this.type = type;
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
	 * Gibt den Feldnamen children zurueck.
	 * 
	 * @return children
	 */
	public String[] getChildNames() {
		if (children == null || children.size() == 0) return EMPTY_NAMES;
		String[] arr = new String[children.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = ((RamResourceCore) children.get(i)).getName();
		}
		return arr;
	}

	/**
	 * Setzt den Feldnamen children.
	 * 
	 * @param children children
	 */
	public void setChildren(List children) {
		this.children = children;
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
		if (this.data != null) this.type = RamResourceCore.TYPE_FILE;

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

	public void addChild(RamResourceCore child) {
		if (children == null) children = new ArrayList();
		children.add(child);
	}

	/**
	 * returns a child that match given name
	 * 
	 * @param name
	 * @return matching child
	 */
	public RamResourceCore getChild(String name, boolean caseSensitive) {
		if (children == null) return null;

		RamResourceCore child;
		for (int i = children.size() - 1; i >= 0; i--) {
			child = (RamResourceCore) children.get(i);
			if (child != null && (caseSensitive ? child.getName().equals(name) : child.getName().equalsIgnoreCase(name))) return child;
		}
		return null;
	}

	/**
	 * returns the parent if this core
	 * 
	 * @return parent core or null if no parent available
	 */
	public RamResourceCore getParent() {
		return parent;
	}

	/**
	 * remove given child from this core
	 * 
	 * @param core
	 */
	public void removeChild(RamResourceCore core) {

		if (children == null) return;

		RamResourceCore child;
		for (int i = children.size() - 1; i >= 0; i--) {
			child = (RamResourceCore) children.get(i);
			if (child == core) {
				children.remove(i);
				break;
			}
		}
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
		setChildren(null);
		RamResourceCore p = getParent();
		if (p != null) p.removeChild(this);
	}

}