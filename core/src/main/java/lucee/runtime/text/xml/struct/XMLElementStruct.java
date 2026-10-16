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
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

import lucee.runtime.type.Collection;

public class XMLElementStruct extends XMLNodeStruct implements Element {

	private Element element;

	/**
	 * constructor of the class
	 * 
	 * @param element
	 * @param caseSensitive
	 */
	protected XMLElementStruct(Element element, boolean caseSensitive) {
		super(element instanceof XMLElementStruct ? element = ((XMLElementStruct) element).getElement() : element, caseSensitive);
		this.element = element;
	}

	@Override
	public String getTagName() {
		return element.getTagName();
	}

	@Override
	public void removeAttribute(String name) throws DOMException {
		element.removeAttribute(name);
	}

	@Override
	public boolean hasAttribute(String name) {
		return element.hasAttribute(name);
	}

	@Override
	public String getAttribute(String name) {
		return element.getAttribute(name);
	}

	@Override
	public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
		element.removeAttributeNS(namespaceURI, localName);
	}

	@Override
	public void setAttribute(String name, String value) throws DOMException {
		element.setAttribute(name, value);
	}

	@Override
	public boolean hasAttributeNS(String namespaceURI, String localName) {
		return element.hasAttributeNS(namespaceURI, localName);
	}

	@Override
	public Attr getAttributeNode(String name) {
		return element.getAttributeNode(name);
	}

	@Override
	public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
		return element.removeAttributeNode(oldAttr);
	}

	@Override
	public Attr setAttributeNode(Attr newAttr) throws DOMException {
		return element.setAttributeNode(newAttr);
	}

	@Override
	public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
		return element.setAttributeNodeNS(newAttr);
	}

	@Override
	public NodeList getElementsByTagName(String name) {
		return element.getElementsByTagName(name);
	}

	@Override
	public String getAttributeNS(String namespaceURI, String localName) {
		return element.getAttributeNS(namespaceURI, localName);
	}

	@Override
	public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
		element.setAttributeNS(namespaceURI, qualifiedName, value);
	}

	@Override
	public Attr getAttributeNodeNS(String namespaceURI, String localName) {
		return element.getAttributeNodeNS(namespaceURI, localName);
	}

	@Override
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		return element.getElementsByTagNameNS(namespaceURI, localName);
	}

	@Override
	public void setIdAttribute(String name, boolean isId) throws DOMException {
		element.setIdAttribute(name, isId);
	}

	@Override
	public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
		element.setIdAttributeNS(namespaceURI, localName, isId);
	}

	@Override
	public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
		element.setIdAttributeNode(idAttr, isId);
	}

	@Override
	public TypeInfo getSchemaTypeInfo() {
		return element.getSchemaTypeInfo();
	}

	/**
	 * @return the element
	 */
	public Element getElement() {
		return element;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLElementStruct((Element) element.cloneNode(deepCopy), caseSensitive);
	}

	@Override
	public Node cloneNode(boolean deep) {
		return new XMLElementStruct((Element) element.cloneNode(deep), caseSensitive);
	}
}