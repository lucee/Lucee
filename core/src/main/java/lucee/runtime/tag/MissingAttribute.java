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
package lucee.runtime.tag;

import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.util.ListUtil;

public class MissingAttribute {

	private final Key name;
	private final String type;
	private final String[] alias;

	public MissingAttribute(Key name, String type, String[] alias) {
		this.name = name;
		this.type = type;
		this.alias = alias;
	}

	public static MissingAttribute newInstance(Key name, String type) {
		return new MissingAttribute(name, type, null);
	}

	public static MissingAttribute newInstance(String name, String type) {
		return newInstance(KeyImpl.init(name), type, null);
	}

	public static MissingAttribute newInstance(Key name, String type, String[] alias) {
		return new MissingAttribute(name, type, alias);
	}

	public static MissingAttribute newInstance(String name, String type, String[] alias) {
		return newInstance(KeyImpl.init(name), type);
	}

	public String[] getAlias() {
		return alias;
	}

	/**
	 * @return the name
	 */
	public Key getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "name:" + name + ";type:" + type + ";alias:" + (alias == null ? "null" : ListUtil.arrayToList(alias, ",")) + ";";
	}
}