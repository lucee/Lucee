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
package lucee.runtime.text.xml.struct;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import lucee.runtime.type.Collection;

/**
 * 
 */
public final class XMLAttrStruct extends XMLNodeStruct implements Attr {

	private Attr attr;

	/**
	 * constructor of the class
	 * 
	 * @param section
	 * @param caseSensitive
	 */
	public XMLAttrStruct(Attr attr, boolean caseSensitive) {
		super(attr, caseSensitive);
		this.attr = attr;
	}

	@Override
	public String getName() {
		return attr.getName();
	}

	@Override
	public Element getOwnerElement() {
		return new XMLElementStruct(attr.getOwnerElement(), caseSensitive);
	}

	@Override
	public boolean getSpecified() {
		return attr.getSpecified();
	}

	@Override
	public String getValue() {
		return attr.getValue();
	}

	@Override
	public void setValue(String arg0) throws DOMException {
		attr.setValue(arg0);
	}

	// used only with java 7, do not set @Override
	public TypeInfo getSchemaTypeInfo() {
		return null;
	}

	// used only with java 7, do not set @Override
	public boolean isId() {
		return false;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLAttrStruct((Attr) attr.cloneNode(deepCopy), caseSensitive);
	}

	@Override
	public Node cloneNode(boolean deep) {
		return new XMLAttrStruct((Attr) attr.cloneNode(deep), caseSensitive);
	}
}