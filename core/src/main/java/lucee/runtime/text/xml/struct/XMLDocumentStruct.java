/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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

import java.lang.reflect.Method;

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
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.util.ArrayUtil;

/**
 * 
 */
public final class XMLDocumentStruct extends XMLNodeStruct implements Document {

	private Document doc;

	/**
	 * @param doc
	 * @param caseSensitive
	 */
	protected XMLDocumentStruct(Document doc, boolean caseSensitive) {
		super(doc, caseSensitive);
		this.doc=doc;
		
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
		return doc.importNode(importedNode,deep);
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
		return doc.createAttributeNS(namespaceURI,qualifiedName);
	}

	@Override
	public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		return doc.createElementNS(namespaceURI,qualifiedName);
	}

	@Override
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		return doc.getElementsByTagNameNS(namespaceURI,localName);
	}

	@Override
	public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
		if (StringUtil.isEmpty(target)) throw new RuntimeException("target is empty/null");
		if (StringUtil.isEmpty(target)) throw new RuntimeException("data is empty/null");
		return doc.createProcessingInstruction(target,data);
	}

	// used only with java 7, do not set @Override
	@Override
	public Node adoptNode(Node arg0) throws DOMException {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("adoptNode", new Class[]{arg0.getClass()});
			return Caster.toNode(m.invoke(doc, new Object[]{arg0}));
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public String getDocumentURI() {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("getDocumentURI", new Class[]{});
			return Caster.toString(m.invoke(doc, ArrayUtil.OBJECT_EMPTY));
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public DOMConfiguration getDomConfig() {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("getDomConfig", new Class[]{});
			return (DOMConfiguration) m.invoke(doc, ArrayUtil.OBJECT_EMPTY);
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public String getInputEncoding() {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("getInputEncoding", new Class[]{});
			return Caster.toString(m.invoke(doc, ArrayUtil.OBJECT_EMPTY));
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public boolean getStrictErrorChecking() {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("getStrictErrorChecking", new Class[]{});
			return Caster.toBooleanValue(m.invoke(doc, ArrayUtil.OBJECT_EMPTY));
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public String getXmlEncoding() {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("getXmlEncoding", new Class[]{});
			return Caster.toString(m.invoke(doc, ArrayUtil.OBJECT_EMPTY));
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public boolean getXmlStandalone() {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("getXmlStandalone", new Class[]{});
			return Caster.toBooleanValue(m.invoke(doc, ArrayUtil.OBJECT_EMPTY));
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public String getXmlVersion() {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("getXmlVersion", new Class[]{});
			return Caster.toString(m.invoke(doc, ArrayUtil.OBJECT_EMPTY));
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public void normalizeDocument() {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("normalizeDocument", new Class[]{});
			m.invoke(doc, ArrayUtil.OBJECT_EMPTY);
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public Node renameNode(Node arg0, String arg1, String arg2) throws DOMException {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("renameNode", new Class[]{arg0.getClass(),arg1.getClass(),arg2.getClass()});
			return Caster.toNode(m.invoke(doc, new Object[]{arg0,arg1,arg2}));
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public void setDocumentURI(String arg0) {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("setDocumentURI", new Class[]{arg0.getClass()});
			m.invoke(doc, new Object[]{arg0});
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
		
	}

	// used only with java 7, do not set @Override
	@Override
	public void setStrictErrorChecking(boolean arg0) {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("setStrictErrorChecking", new Class[]{boolean.class});
			m.invoke(doc, new Object[]{Caster.toBoolean(arg0)});
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
		
	}

	// used only with java 7, do not set @Override
	@Override
	public void setXmlStandalone(boolean arg0) throws DOMException {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("setXmlStandalone", new Class[]{boolean.class});
			m.invoke(doc, new Object[]{Caster.toBoolean(arg0)});
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
		
	}

	// used only with java 7, do not set @Override
	@Override
	public void setXmlVersion(String arg0) throws DOMException {
    	// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = doc.getClass().getMethod("setXmlVersion", new Class[]{arg0.getClass()});
			m.invoke(doc, new Object[]{arg0});
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}
	
	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLDocumentStruct((Document)doc.cloneNode(deepCopy),caseSensitive);
	}
	

	@Override
	public Node cloneNode(boolean deep) {
		return new XMLDocumentStruct((Document)doc.cloneNode(deep),caseSensitive);
	}
}