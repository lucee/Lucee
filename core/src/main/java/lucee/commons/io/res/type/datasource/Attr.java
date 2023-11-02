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
package lucee.commons.io.res.type.datasource;

import lucee.commons.io.res.type.datasource.core.CoreSupport;

public class Attr {

	public static final int TYPE_DIRECTORY = 0;
	public static final int TYPE_FILE = 1;
	public static final int TYPE_LINK = 2;
	public static final int TYPE_UNDEFINED = 3;

	private boolean exists = true;
	private int size = 0;
	private short mode;
	private short attributes;
	private String name;
	private String parent;
	private int id;
	private long lastModified;
	private int type;
	private int data;
	private boolean isFile;
	private boolean isDirectory;

	private long created = System.currentTimeMillis();

	public Attr(int id, String name, String parent, boolean exists, int type, int size, long lastModified, short mode, short attributes, int data) {
		// if(mode==0)print.dumpStack();
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.exists = exists;
		this.type = type;
		this.size = size;
		this.lastModified = lastModified;
		this.mode = mode;
		this.attributes = attributes;
		this.data = data;

		this.isDirectory = CoreSupport.isDirectory(type);
		this.isFile = CoreSupport.isFile(type);
	}

	/**
	 * @return the data
	 */
	public int getData() {
		return data;
	}

	public static Attr notExists(String name, String parent) {
		return new Attr(0, name, parent, false, Attr.TYPE_UNDEFINED, 0, 0, (short) 0, (short) 0, 0);
	}

	/**
	 * @return the lastModified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the parent
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * @return the mode
	 */
	public short getMode() {
		return mode;
	}

	/**
	 * @return the attributes
	 */
	public short getAttributes() {
		return attributes;
	}

	public boolean exists() {
		return exists;
	}

	public boolean isFile() {
		return isFile;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public int size() {
		return size;
	}

	public long timestamp() {
		return created;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

}