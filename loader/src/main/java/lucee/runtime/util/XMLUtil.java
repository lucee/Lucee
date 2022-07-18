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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lucee.commons.io.res.Resource;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public interface XMLUtil {

	public String unescapeXMLString(String str);

	public String escapeXMLString(String xmlStr);

	public TransformerFactory getTransformerFactory();

	/**
	 * parse XML/HTML String to a XML DOM representation
	 * 
	 * @param xml XML InputSource
	 * @param validator validator
	 * @param isHtml is a HTML or XML Object
	 * @return parsed Document
	 * @throws SAXException SAX Exception
	 * @throws IOException IO Exception
	 */
	public Document parse(InputSource xml, InputSource validator, boolean isHtml) throws SAXException, IOException;

	public void replaceChild(Node newChild, Node oldChild);

	/**
	 * check if given name is equal to name of the element (with and without namespace)
	 * 
	 * @param node node to compare the name
	 * @param name name to compare
	 * @return is name of the given Node equal to the given name
	 */
	public boolean nameEqual(Node node, String name);

	/**
	 * return the root Element from a node
	 * 
	 * @param node node to get root element from
	 * @return Root Element
	 */
	public Element getRootElement(Node node);

	/**
	 * returns a new Empty XMl Document
	 * 
	 * @return new Document
	 * @throws ParserConfigurationException Parser Configuration Exception
	 * @throws FactoryConfigurationError Factory Configuration Error
	 */
	public Document newDocument() throws ParserConfigurationException, FactoryConfigurationError;

	/**
	 * return the Owner Document of a Node List
	 * 
	 * @param nodeList node list
	 * @return XML Document
	 * @throws PageException Page Exception
	 */
	public Document getDocument(NodeList nodeList) throws PageException;

	public Document getDocument(Node node);

	/**
	 * return all Children of a node by a defined type as Node List
	 * 
	 * @param node node to get children from
	 * @param type type of returned node
	 * @param filter filter to use
	 * @return all matching child node
	 */
	public ArrayList<Node> getChildNodes(Node node, short type, String filter);

	public Node getChildNode(Node node, short type, String filter, int index);

	/**
	 * transform a XML Object to another format, with help of a XSL Stylesheet
	 * 
	 * @param xml xml to convert
	 * @param xsl xsl used to convert
	 * @param parameters parameters used to convert
	 * @return resulting string
	 * @throws TransformerException Transformer Exception
	 * @throws SAXException SAX Exception
	 * @throws IOException IO Exception
	 */
	public String transform(InputSource xml, InputSource xsl, Map<String, Object> parameters) throws TransformerException, SAXException, IOException;

	/**
	 * transform a XML Document to another format, with help of a XSL Stylesheet
	 * 
	 * @param doc xml to convert
	 * @param xsl xsl used to convert
	 * @param parameters parameters used to convert
	 * @return resulting string
	 * @throws TransformerException Transformer Exception
	 */
	public String transform(Document doc, InputSource xsl, Map<String, Object> parameters) throws TransformerException;

	public Element getChildWithName(String name, Element el);

	public InputSource toInputSource(Resource res, Charset cs) throws IOException;

	public InputSource toInputSource(Object value) throws IOException, PageException;

	public Struct validate(InputSource xml, InputSource schema, String strSchema) throws PageException;

	public void prependChild(Element parent, Element child);

	public void setFirst(Node parent, Node node);

	/**
	 * write a xml Dom to a file
	 * 
	 * @param node node
	 * @param file Resource
	 * @throws PageException Page Exception
	 */
	public void writeTo(Node node, Resource file) throws PageException;

	public String toString(Node node, boolean omitXMLDecl, boolean indent, String publicId, String systemId, String encoding) throws PageException;

	public String toString(NodeList nodes, boolean omitXMLDecl, boolean indent) throws PageException;

	public String toString(Node node, String defaultValue);

	public void writeTo(Node node, Result res, boolean omitXMLDecl, boolean indent, String publicId, String systemId, String encoding) throws PageException;

	public Node toNode(Object obj) throws PageException;

	/**
	 * creates and returns a xml Document instance
	 * 
	 * @param file Resource
	 * @param isHtml Is html
	 * @return struct
	 * @throws PageException Page Exception
	 */
	public abstract Document createDocument(Resource file, boolean isHtml) throws PageException;

	/**
	 * creates and returns a xml Document instance
	 * 
	 * @param xml XML
	 * @param isHtml Is html
	 * @return struct
	 * @throws PageException Page Exception
	 */
	public abstract Document createDocument(String xml, boolean isHtml) throws PageException;

	/**
	 * creates and returns a xml Document instance
	 * 
	 * @param is Input Stream
	 * @param isHtml Is html
	 * @return struct
	 * @throws PageException Page Exception
	 */
	public abstract Document createDocument(InputStream is, boolean isHtml) throws PageException;

}