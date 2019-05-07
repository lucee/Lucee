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
package lucee.runtime.registry;

/**
 * represent a single Registry Entry
 */
public final class RegistryEntry {
	/**
	 * Field <code>TYPE_STRING</code>
	 */
	public static final short TYPE_STRING = 0;
	/**
	 * Field <code>TYPE_DWORD</code>
	 */
	public static final short TYPE_DWORD = 1;
	/**
	 * Field <code>TYPE_ANY</code>
	 */
	public static final short TYPE_ANY = 2;
	/**
	 * Field <code>TYPE_KEY</code>
	 */
	public static final short TYPE_KEY = 3;

	/**
	 * Field <code>REGSTR_TOKEN</code>
	 */
	public static final String REGSTR_TOKEN = "REG_SZ";
	/**
	 * Field <code>REGKEY_TOKEN</code>
	 */
	public static final String REGKEY_TOKEN = "REG_KEY";
	/**
	 * Field <code>REGDWORD_TOKEN</code>
	 */
	public static final String REGDWORD_TOKEN = "REG_DWORD";

	private short type;
	private String key;
	private Object value;

	/**
	 * constructor of the class
	 * 
	 * @param type (RegistryEntry.TYPE_DWORD, RegistryEntry.TYPE_STRING)
	 * @param key
	 * @param value
	 * @throws RegistryException
	 */
	public RegistryEntry(short type, String key, Object value) throws RegistryException {
		if (type != TYPE_DWORD && type != TYPE_STRING && type != TYPE_KEY) throw new RegistryException("invalid Registry Type definition");

		this.type = type;
		this.key = key;
		this.value = value;
	}

	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return Returns the type.
	 */
	public short getType() {
		return type;
	}

	/**
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		try {
			return "Registry Entry: [" + key + " " + toStringType(type) + " " + value + "]";
		}
		catch (RegistryException e) {
			return "Registry Entry: [" + key + " " + value + "]";
		}
	}

	/**
	 * cast a String type to a short Type
	 * 
	 * @param strType
	 * @return
	 * @throws RegistryException
	 */
	public static short toType(String strType) throws RegistryException {
		if (strType.equals(REGDWORD_TOKEN)) return RegistryEntry.TYPE_DWORD;
		else if (strType.equals(REGSTR_TOKEN)) return RegistryEntry.TYPE_STRING;
		else if (strType.equals(REGKEY_TOKEN)) return RegistryEntry.TYPE_KEY;
		throw new RegistryException(strType + " is not a valid Registry Type");
	}

	/**
	 * cast a short type to a String Type
	 * 
	 * @param type
	 * @return Registry String Type Definition
	 * @throws RegistryException
	 */
	public static String toStringType(short type) throws RegistryException {
		if (type == TYPE_DWORD) return REGDWORD_TOKEN;
		else if (type == TYPE_STRING) return REGSTR_TOKEN;
		else if (type == TYPE_KEY) return REGKEY_TOKEN;
		throw new RegistryException("invalid Registry Type definition");
	}

	/**
	 * cast a short type to a String Type
	 * 
	 * @param type
	 * @return Registry String Type Definition
	 * @throws RegistryException
	 */
	public static String toCFStringType(short type) throws RegistryException {
		if (type == TYPE_DWORD) return "DWORD";
		else if (type == TYPE_STRING) return "STRING";
		else if (type == TYPE_KEY) return "KEY";
		throw new RegistryException("invalid Registry Type definition");
	}
}