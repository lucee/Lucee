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
package lucee.runtime;

import java.io.Serializable;
import java.util.Map;

import lucee.runtime.component.Property;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

/**
 * Shared across all duplicates of a prototype (LDEV-6298). Treat as immutable post-init.
 * Any new post-init mutation will silently leak across every duplicate of the same prototype.
 */
public final class ComponentProperties implements Serializable {

	private static final Collection.Key WSDL_FILE = KeyConstants._wsdlfile;

	// Reference fields (8 bytes each) - group together to minimize padding
	final String dspName;
	final String extend;
	final String hint;
	final String callPath;
	final String implement;
	final String subName;
	final String name;
	final Boolean output;
	Class javaAccessClass;
	Map<String, Property> properties;
	Struct meta;

	// int field (4 bytes)
	final int modifier;

	// Boolean fields (1 byte each) - group at end to minimize padding
	final boolean realPath;
	final boolean _synchronized;
	final boolean persistent;
	final boolean accessors;
	public boolean inline;

	public ComponentProperties(String name, String dspName, String extend, String implement, String hint, Boolean output, String callPath, boolean realPath, String subName,
			boolean _synchronized, Class javaAccessClass, boolean persistent, boolean accessors, int modifier, Struct meta) {
		this.name = name;
		this.dspName = dspName;
		this.extend = extend;
		this.implement = implement;
		this.hint = hint;
		this.output = output;
		this.callPath = callPath;
		this.realPath = realPath;
		this.subName = subName;
		this._synchronized = _synchronized;
		this.javaAccessClass = javaAccessClass;
		this.meta = meta;
		this.persistent = persistent;
		this.accessors = accessors;
		this.modifier = modifier;
	}

	public ComponentProperties duplicate() {
		ComponentProperties cp = new ComponentProperties(name, dspName, extend, implement, hint, output, callPath, realPath, subName, _synchronized, javaAccessClass, persistent,
				accessors, modifier, meta);
		cp.properties = properties;
		cp.inline = inline;
		return cp;
	}

	/**
	 * returns null if there is no wsdlFile defined
	 * 
	 * @return the wsdlFile
	 */
	public String getWsdlFile() {
		if (meta == null) return null;
		return (String) meta.get(WSDL_FILE, null);
	}
}