/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import lucee.commons.io.res.Resource;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.XMLException;
import lucee.runtime.text.xml.ArrayNodeList;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XMLUtilImpl implements XMLUtil {
	
	public static void main(String[] args) {
		//lucee.runtime.text.xml.XMLUtil.
	}

	@Override
    public String unescapeXMLString(String str) {
    	return lucee.runtime.text.xml.XMLUtil.unescapeXMLString(str);
    }

	@Override
    public String escapeXMLString(String xmlStr) {
		return lucee.runtime.text.xml.XMLUtil.escapeXMLString(xmlStr);
    }

	@Override
    public TransformerFactory getTransformerFactory() {
    	return lucee.runtime.text.xml.XMLUtil.getTransformerFactory();
    }
    
    @Override
    public final Document parse(InputSource xml,InputSource validator, boolean isHtml) throws SAXException, IOException {
    	return lucee.runtime.text.xml.XMLUtil.parse(xml, validator, isHtml);
    }
	
    @Override
    public Object setProperty(Node node, Collection.Key key, Object value,boolean caseSensitive, Object defaultValue) {
		return lucee.runtime.text.xml.XMLUtil.setProperty(node, key, value, caseSensitive, defaultValue);
	}
	
    @Override
    public Object setProperty(Node node, Collection.Key key, Object value,boolean caseSensitive) throws PageException {
		return lucee.runtime.text.xml.XMLUtil.setProperty(node, key, value, caseSensitive);
	}


    @Override
    public void replaceChild(Node newChild, Node oldChild) {
    	lucee.runtime.text.xml.XMLUtil.replaceChild(newChild, oldChild);
	}

	@Override
	public Object getProperty(Node node, Collection.Key k,boolean caseSensitive, Object defaultValue) {
		return lucee.runtime.text.xml.XMLUtil.getProperty(node, k, caseSensitive, defaultValue);
	}
	
	@Override
	public Object getProperty(Node node, Collection.Key k,boolean caseSensitive) throws SAXException {
		return lucee.runtime.text.xml.XMLUtil.getProperty(node, k, caseSensitive);
	}

	@Override
    public Object removeProperty(Node node, Collection.Key k,boolean caseSensitive) {
    	return lucee.runtime.text.xml.XMLUtil.removeProperty(node, k, caseSensitive);
    }

	@Override
    public boolean nameEqual(Node node, String name, boolean caseSensitive) {
    	return lucee.runtime.text.xml.XMLUtil.nameEqual(node, name, caseSensitive);
	}

	@Override
    public boolean isCaseSensitve(Node node) {
		return lucee.runtime.text.xml.XMLUtil.isCaseSensitve(node);
	}
	
	@Override
    public Element getRootElement(Node node, boolean caseSensitive) {
		return lucee.runtime.text.xml.XMLUtil.getRootElement(node, caseSensitive);
	}
	

	@Override
    public Node getParentNode(Node node, boolean caseSensitive) {
		return lucee.runtime.text.xml.XMLUtil.getParentNode(node, caseSensitive);
	}

	@Override
    public Document newDocument() throws ParserConfigurationException, FactoryConfigurationError {
		return lucee.runtime.text.xml.XMLUtil.newDocument();
	}
	
	@Override
    public Document getDocument(NodeList nodeList) throws XMLException {
		return lucee.runtime.text.xml.XMLUtil.getDocument(nodeList);
	}
	
	@Override
    public Document getDocument(Node node) {
		return lucee.runtime.text.xml.XMLUtil.getDocument(node);
	}
	
	@Override
    public ArrayNodeList getChildNodes(Node node, short type, boolean caseSensitive, String filter) {
		return lucee.runtime.text.xml.XMLUtil.getChildNodes(node, type, caseSensitive, filter);
	}
	

	@Override
    public int childNodesLength(Node node, short type, boolean caseSensitive, String filter) {
		return lucee.runtime.text.xml.XMLUtil.childNodesLength(node, type, caseSensitive, filter);
	}

	@Override
    public Node getChildNode(Node node, short type, boolean caseSensitive, String filter, int index) {
		return lucee.runtime.text.xml.XMLUtil.getChildNode(node, type, caseSensitive, filter, index);
	}
    
    @Override
    public String transform(InputSource xml, InputSource xsl, Map<String,Object> parameters) throws TransformerException, SAXException, IOException {
    	return lucee.runtime.text.xml.XMLUtil.transform(xml, xsl, parameters);
    }

    @Override
    public String transform(Document doc, InputSource xsl, Map<String,Object> parameters) throws TransformerException {
    	return lucee.runtime.text.xml.XMLUtil.transform(doc, xsl, parameters);
	}

    @Override
    public String getTypeAsString(Node node, boolean cftype) {
		return lucee.runtime.text.xml.XMLUtil.getTypeAsString(node, cftype);
    }

    @Override
    public Element getChildWithName(String name, Element el) {
		return lucee.runtime.text.xml.XMLUtil.getChildWithName(name, el);
	}
	
    @Override
    public InputSource toInputSource(Resource res, Charset cs) throws IOException {
		return lucee.runtime.text.xml.XMLUtil.toInputSource(res, cs);
    }

    @Override
    public InputSource toInputSource(Object value) throws IOException, ExpressionException {
		return lucee.runtime.text.xml.XMLUtil.toInputSource(ThreadLocalPageContext.get(), value);
	}
	
	@Override
    public Struct validate(InputSource xml, InputSource schema, String strSchema) throws XMLException {
		return lucee.runtime.text.xml.XMLUtil.validate(xml, schema, strSchema);
    }

	@Override
    public void prependChild(Element parent, Element child) {
		lucee.runtime.text.xml.XMLUtil.prependChild(parent, child);
	}

	@Override
    public void setFirst(Node parent, Node node) {
		lucee.runtime.text.xml.XMLUtil.setFirst(parent, node);
	}

	@Override
    public XMLReader createXMLReader(String oprionalDefaultSaxParser) throws SAXException {
		return lucee.runtime.text.xml.XMLUtil.createXMLReader(oprionalDefaultSaxParser);
	}

	@Override
	public Text toText(Document doc, Object o) throws PageException {
		return XMLCaster.toText(doc, o);
	}

	@Override
	public Text toCDATASection(Document doc, Object o) throws PageException {
		return XMLCaster.toCDATASection(doc, o);
	}

	@Override
	public Text[] toTextArray(Document doc, Object o) throws PageException {
		return XMLCaster.toTextArray(doc, o);
	}

	@Override
	public Attr toAttr(Document doc, Object o) throws PageException {
		return XMLCaster.toAttr(doc, o);
	}

	@Override
	public Attr[] toAttrArray(Document doc, Object o) throws PageException {
		return XMLCaster.toAttrArray(doc, o);
	}

	@Override
	public Comment toComment(Document doc, Object o) throws PageException {
		return XMLCaster.toComment(doc, o);
	}

	@Override
	public Comment[] toCommentArray(Document doc, Object o) throws PageException {
		return XMLCaster.toCommentArray(doc, o);
	}

	@Override
	public Element toElement(Document doc, Object o) throws PageException {
		return XMLCaster.toElement(doc, o);
	}

	@Override
	public Element[] toElementArray(Document doc, Object o) throws PageException {
		return XMLCaster.toElementArray(doc, o);
	}

	@Override
	public Node toRawNode(Node node) {
		return XMLCaster.toRawNode(node);
	}

	@Override
	public Node toNode(Document doc, Object o, boolean clone) throws PageException {
		return XMLCaster.toNode(doc, o, clone);
	}

	@Override
	public Node[] toNodeArray(Document doc, Object o) throws PageException {
		return XMLCaster.toNodeArray(doc, o);
	}

	@Override
	public Node toNode(Document doc, Object o, short type) throws PageException {
		return XMLCaster.toNode(doc, o, type);
	}

	@Override
	public Node[] toNodeArray(Document doc, Object o, short type) throws PageException {
		return XMLCaster.toNodeArray(doc, o, type);
	}

	@Override
	public String toHTML(Node node) throws PageException {
		return XMLCaster.toHTML(node);
	}

	@Override
	public void writeTo(Node node, Resource file) throws PageException {
		XMLCaster.writeTo(node, file);
	}

	@Override
	public String toString(Node node) throws PageException {
		return XMLCaster.toString(node);
	}

	@Override
	public String toString(Node node, boolean omitXMLDecl, boolean indent) throws PageException {
		return XMLCaster.toString(node, omitXMLDecl, indent);
	}

	@Override
	public String toString(Node node, boolean omitXMLDecl, boolean indent,
			String publicId, String systemId, String encoding) throws PageException {
		return XMLCaster.toString(node, omitXMLDecl, indent, publicId, systemId, encoding);
	}

	@Override
	public String toString(NodeList nodes, boolean omitXMLDecl, boolean indent) throws PageException {
		return XMLCaster.toString(nodes, omitXMLDecl, indent);
	}

	@Override
	public String toString(Node node, String defaultValue) {
		return XMLCaster.toString(node, defaultValue);
	}

	@Override
	public void writeTo(Node node, Result res, boolean omitXMLDecl,
			boolean indent, String publicId, String systemId, String encoding) throws PageException {
		XMLCaster.writeTo(node, res, omitXMLDecl, indent, publicId, systemId, encoding);
	}

	@Override
	public Element toRawElement(Object value, Element defaultValue) {
		return XMLCaster.toRawElement(value, defaultValue);
	}
}