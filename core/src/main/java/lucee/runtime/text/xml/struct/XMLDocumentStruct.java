/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
 */
package lucee.runtime.text.xml.struct;


import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import lucee.commons.lang.StringUtil;
import lucee.runtime.type.Collection;

public final class XMLDocumentStruct extends XMLNodeStruct implements Document {

	private Document doc;

	/**
	 * @param doc
	 * @param caseSensitive
	 */
	protected XMLDocumentStruct(Document doc, boolean caseSensitive) {
		super(doc, caseSensitive);
		this.doc = doc;

	}

	@Override
	public DOMImplementation getImplementation() {
		return doc.getImplementation();
	}

	@Override
	public DocumentFragment createDocumentFragment() {
		return doc.createDocumentFragment();
	}

	@Override
	public DocumentType getDoctype() {
		return doc.getDoctype();
	}

	@Override
	public Element getDocumentElement() {
		return doc.getDocumentElement();
	}

	@Override
	public Attr createAttribute(String name) throws DOMException {
		return doc.createAttribute(name);
	}

	@Override
	public CDATASection createCDATASection(String data) throws DOMException {
		return doc.createCDATASection(data);
	}

	@Override
	public Comment createComment(String data) {
		return doc.createComment(data);
	}

	@Override
	public Element createElement(String tagName) throws DOMException {
		return doc.createElement(tagName);
	}

	@Override
	public Element getElementById(String elementId) {
		return doc.getElementById(elementId);
	}

	@Override
	public EntityReference createEntityReference(String name) throws DOMException {
		return doc.createEntityReference(name);
	}

	@Override
	public Node importNode(Node importedNode, boolean deep) throws DOMException {
		return doc.importNode(importedNode, deep);
	}

	@Override
	public NodeList getElementsByTagName(String tagname) {
		return doc.getElementsByTagName(tagname);
	}

	@Override
	public Text createTextNode(String data) {
		return doc.createTextNode(data);
	}

	@Override
	public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
		return doc.createAttributeNS(namespaceURI, qualifiedName);
	}

	@Override
	public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		return doc.createElementNS(namespaceURI, qualifiedName);
	}

	@Override
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		return doc.getElementsByTagNameNS(namespaceURI, localName);
	}

	@Override
	public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
		if (StringUtil.isEmpty(target)) throw new RuntimeException("target is empty/null");
		if (StringUtil.isEmpty(target)) throw new RuntimeException("data is empty/null");
		return doc.createProcessingInstruction(target, data);
	}

	@Override
	public Node adoptNode(Node arg0) throws DOMException {
		return doc.adoptNode(arg0);
	}

	@Override
	public String getDocumentURI() {
		return doc.getDocumentURI();
	}

	@Override
	public DOMConfiguration getDomConfig() {
		return doc.getDomConfig();
	}

	@Override
	public String getInputEncoding() {
		return doc.getInputEncoding();
	}

	@Override
	public boolean getStrictErrorChecking() {
		return doc.getStrictErrorChecking();
	}

	@Override
	public String getXmlEncoding() {
		return doc.getXmlEncoding();
	}

	@Override
	public boolean getXmlStandalone() {
		return doc.getXmlStandalone();
	}

	@Override
	public String getXmlVersion() {
		return doc.getXmlVersion();
	}

	@Override
	public void normalizeDocument() {
		doc.normalizeDocument();
	}

	@Override
	public Node renameNode(Node arg0, String arg1, String arg2) throws DOMException {
		return doc.renameNode(arg0, arg1, arg2);
	}

	@Override
	public void setDocumentURI(String arg0) {
		doc.setDocumentURI(arg0);
	}

	@Override
	public void setStrictErrorChecking(boolean arg0) {
		doc.setStrictErrorChecking(arg0);
	}

	@Override
	public void setXmlStandalone(boolean arg0) throws DOMException {
		doc.setXmlStandalone(arg0);
	}

	@Override
	public void setXmlVersion(String arg0) throws DOMException {
		doc.setXmlVersion(arg0);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLDocumentStruct((Document) doc.cloneNode(deepCopy), caseSensitive);
	}

	@Override
	public Node cloneNode(boolean deep) {
		return new XMLDocumentStruct((Document) doc.cloneNode(deep), caseSensitive);
	}
}