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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public interface XMLUtil {

	public String unescapeXMLString(String str);

	public String escapeXMLString(String xmlStr);

	public TransformerFactory getTransformerFactory();

	/**
	 * parse XML/HTML String to a XML DOM representation
	 * 
	 * @param xml XML InputSource
	 * @param isHtml is a HTML or XML Object
	 * @return parsed Document
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document parse(InputSource xml, InputSource validator, boolean isHtml) throws SAXException, IOException;

	// public Object setProperty(Node node, Key key, Object value, boolean caseSensitive, Object defaultValue);

	// public Object setProperty(Node node, Key key, Object value, boolean caseSensitive) throws PageException;

	public void replaceChild(Node newChild, Node oldChild);

	/* *
	 * returns a property from a XMl Node (Expression Less)
	 * 
	 * @param node
	 * @param key
	 * @param caseSensitive
	 * @return Object matching key
	 */
	//public Object getProperty(Node node, Key key, boolean caseSensitive,Object defaultValue);

	/*
	 * returns a property from a XMl Node
	 * 
	 * @param node
	 * @param key
	 * @param caseSensitive
	 * @return Object matching key
	 * @throws SAXException
	 */
	//public Object getPropertyX(Node node, Key key, boolean caseSensitive) throws SAXException;

	/**
	 * check if given name is equal to name of the element (with and without
	 * namespace)
	 * 
	 * @param node
	 * @param name
	 * @param caseSensitive
	 * @return
	 */
	public boolean nameEqual(Node node, String name);

	//public boolean isCaseSensitve(Node node);

	/* *
	 * removes child from a node
	 * 
	 * @param node
	 * @param key
	 * @param caseSensitive
	 * @return removed property
	 */
	//public Object removeProperty(Node node, Key key, boolean caseSensitive);

	/**
	 * return the root Element from a node
	 * 
	 * @param node node to get root element from
	 * @param caseSensitive
	 * @return Root Element
	 */
	public Element getRootElement(Node node);

	//public Node getParentNode(Node node);

	/**
	 * returns a new Empty XMl Document
	 * 
	 * @return new Document
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 */
	public Document newDocument() throws ParserConfigurationException, FactoryConfigurationError;

	/**
	 * return the Owner Document of a Node List
	 * 
	 * @param nodeList
	 * @return XML Document
	 * @throws PageException
	 */
	public Document getDocument(NodeList nodeList) throws PageException;

	public Document getDocument(Node node);

	/**
	 * return all Children of a node by a defined type as Node List
	 * 
	 * @param node node to get children from
	 * @param type type of returned node
	 * @param filter
	 * @param caseSensitive
	 * @return all matching child node
	 */
	public ArrayList<Node> getChildNodes(Node node, short type, String filter);

	//public int childNodesLength(Node node, short type, boolean caseSensitive,String filter);

	public Node getChildNode(Node node, short type, String filter, int index);

	/**
	 * transform a XML Object to a other format, with help of a XSL Stylesheet
	 * 
	 * @param xml xml to convert
	 * @param xsl xsl used to convert
	 * @param parameters parameters used to convert
	 * @return resulting string
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 */
	public String transform(InputSource xml, InputSource xsl, Map<String, Object> parameters) throws TransformerException, SAXException, IOException;

	/**
	 * transform a XML Document to a other format, with help of a XSL Stylesheet
	 * 
	 * @param doc xml to convert
	 * @param xsl xsl used to convert
	 * @param parameters parameters used to convert
	 * @return resulting string
	 * @throws TransformerException
	 */
	public String transform(Document doc, InputSource xsl,
			Map<String, Object> parameters) throws TransformerException;

	/* *
	 * returns the Node Type As String
	 * 
	 * @param node
	 * @param cftype
	 * @return
	 */
	//public String getTypeAsString(Node node, boolean cftype);

	public Element getChildWithName(String name, Element el);

	public InputSource toInputSource(Resource res, Charset cs) throws IOException;

	public InputSource toInputSource(Object value) throws IOException, PageException;

	public Struct validate(InputSource xml, InputSource schema, String strSchema) throws PageException;

	public void prependChild(Element parent, Element child);

	public void setFirst(Node parent, Node node);

	//public XMLReader createXMLReader(String oprionalDefaultSaxParser) throws SAXException;

	/* *
	 * casts a value to a XML Text
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @return XML Text Object
	 * @throws PageException
	 */
	//public Text toText(Document doc, Object o) throws PageException;

	//public Text toCDATASection(Document doc, Object o) throws PageException;

	/* *
	 * casts a value to a XML Text Array
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @return XML Text Array
	 * @throws PageException
	 */
	//public Text[] toTextArray(Document doc, Object o) throws PageException;

	/* *
	 * casts a value to a XML Attribute Object
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @return XML Comment Object
	 * @throws PageException
	 */
	//public Attr toAttr(Document doc, Object o) throws PageException;

	/* *
	 * casts a value to a XML Attr Array
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @return XML Attr Array
	 * @throws PageException
	 */
	//public Attr[] toAttrArray(Document doc, Object o) throws PageException;

	/* *
	 * casts a value to a XML Comment Object
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @return XML Comment Object
	 * @throws PageException
	 */
	//public Comment toComment(Document doc, Object o) throws PageException;

	/* *
	 * casts a value to a XML Comment Array
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @return XML Comment Array
	 * @throws PageException
	 */
	//public Comment[] toCommentArray(Document doc, Object o) throws PageException;

	/* *
	 * casts a value to a XML Element
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @return XML Element Object
	 * @throws PageException
	 */
	//public Element toElement(Document doc, Object o) throws PageException;

	/* *
	 * casts a value to a XML Element Array
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @return XML Comment Array
	 * @throws PageException
	 */
	//public Element[] toElementArray(Document doc, Object o) throws PageException;

	/* *
	 * remove lucee node wraps (XMLStruct) from node
	 * 
	 * @param node
	 * @return raw node (without wrap)
	 */
	//public Node toRawNode(Node node);

	//public Node toNode(Document doc, Object o, boolean clone) throws PageException;

	/* *
	 * casts a value to a XML Element Array
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @return XML Comment Array
	 * @throws PageException
	 */
	//public Node[] toNodeArray(Document doc, Object o) throws PageException;

	/* *
	 * casts a value to a XML Object defined by type parameter
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @param type type to cast to
	 * @return XML Text Object
	 * @throws PageException
	 */
	//public Node toNode(Document doc, Object o, short type) throws PageException;

	/* *
	 * casts a value to a XML Object Array defined by type parameter
	 * 
	 * @param doc XML Document
	 * @param o Object to cast
	 * @param type type to cast to
	 * @return XML Node Array Object
	 * @throws PageException
	 */
	//public Node[] toNodeArray(Document doc, Object o, short type) throws PageException;

	// public String toHTML(Node node) throws PageException;

	/**
	 * write a xml Dom to a file
	 * 
	 * @param node
	 * @param file
	 * @throws PageException
	 */
	public void writeTo(Node node, Resource file) throws PageException;
	
	public String toString(Node node, boolean omitXMLDecl, boolean indent, String publicId, String systemId, String encoding) throws PageException;

	public String toString(NodeList nodes, boolean omitXMLDecl, boolean indent) throws PageException;

	public String toString(Node node, String defaultValue);

	public void writeTo(Node node, Result res, boolean omitXMLDecl, boolean indent, String publicId, String systemId, String encoding) throws PageException;

	public Node toNode(Object obj) throws PageException;

	//public Element toRawElement(Object value, Element defaultValue);
	
	/**
	 * creates and returns a xml Document instance
	 * 
	 * @param file
	 * @param isHtml
	 * @return struct
	 * @throws PageException
	 */
	public abstract Document createDocument(Resource file, boolean isHtml) throws PageException;

	/**
	 * creates and returns a xml Document instance
	 * 
	 * @param xml
	 * @param isHtml
	 * @return struct
	 * @throws PageException
	 */
	public abstract Document createDocument(String xml, boolean isHtml) throws PageException;

	/**
	 * creates and returns a xml Document instance
	 * 
	 * @param is
	 * @param isHtml
	 * @return struct
	 * @throws PageException
	 */
	public abstract Document createDocument(InputStream is, boolean isHtml)
			throws PageException;

}