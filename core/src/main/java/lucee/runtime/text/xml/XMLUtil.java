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
package lucee.runtime.text.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.XMLException;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.EnvClassLoader;
import lucee.runtime.text.xml.struct.XMLMultiElementStruct;
import lucee.runtime.text.xml.struct.XMLStruct;
import lucee.runtime.text.xml.struct.XMLStructFactory;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;

/**
 *
 */
public final class XMLUtil {

	public static final short UNDEFINED_NODE = -1;

	public static final Collection.Key XMLCOMMENT = KeyImpl.intern("xmlcomment");
	public static final Collection.Key XMLTEXT = KeyImpl.intern("xmltext");
	public static final Collection.Key XMLCDATA = KeyImpl.intern("xmlcdata");
	public static final Collection.Key XMLCHILDREN = KeyImpl.intern("xmlchildren");
	public static final Collection.Key XMLNODES = KeyImpl.intern("xmlnodes");
	public static final Collection.Key XMLNSURI = KeyImpl.intern("xmlnsuri");
	public static final Collection.Key XMLNSPREFIX = KeyImpl.intern("xmlnsprefix");
	public static final Collection.Key XMLROOT = KeyImpl.intern("xmlroot");
	public static final Collection.Key XMLPARENT = KeyImpl.intern("xmlparent");
	public static final Collection.Key XMLNAME = KeyImpl.intern("xmlname");
	public static final Collection.Key XMLTYPE = KeyImpl.intern("xmltype");
	public static final Collection.Key XMLVALUE = KeyImpl.intern("xmlvalue");
	public static final Collection.Key XMLATTRIBUTES = KeyImpl.intern("xmlattributes");
	public static final Collection.Key KEY_FEATURE_SECURE = KeyImpl.intern("secure");
	public static final Collection.Key KEY_FEATURE_DISALLOW_DOCTYPE_DECL = KeyImpl.intern("disallowDoctypeDecl");
	public static final Collection.Key KEY_FEATURE_EXTERNAL_GENERAL_ENTITIES = KeyImpl.intern("externalGeneralEntities");

	// public final static String
	// DEFAULT_SAX_PARSER="org.apache.xerces.parsers.SAXParser";

	/*
	 * private static final Collection.Key = KeyImpl.getInstance(); private static final Collection.Key
	 * = KeyImpl.getInstance(); private static final Collection.Key = KeyImpl.getInstance(); private
	 * static final Collection.Key = KeyImpl.getInstance(); private static final Collection.Key =
	 * KeyImpl.getInstance(); private static final Collection.Key = KeyImpl.getInstance();
	 */

	// static DOMParser parser = new DOMParser();
	private static DocumentBuilder docBuilder;
	// private static DocumentBuilderFactory factory;
	private static TransformerFactory transformerFactory;
	// private static DocumentBuilderFactory documentBuilderFactory;

	private static SAXParserFactory saxParserFactory;

	private static URL transformerFactoryResource;

	public static String unescapeXMLString(String str) {

		StringBuffer rtn = new StringBuffer();
		int posStart = -1;
		int posFinish = -1;
		while ((posStart = str.indexOf('&', posStart)) != -1) {
			int last = posFinish + 1;

			posFinish = str.indexOf(';', posStart);
			if (posFinish == -1) break;
			rtn.append(str.substring(last, posStart));
			if (posStart + 1 < posFinish) {
				rtn.append(unescapeXMLEntity(str.substring(posStart + 1, posFinish)));
			}
			else {
				rtn.append("&;");
			}

			posStart = posFinish + 1;
		}
		rtn.append(str.substring(posFinish + 1));
		return rtn.toString();
	}

	public static String unescapeXMLString2(String str) {

		StringBuffer sb = new StringBuffer();
		int index, last = 0, indexSemi;
		while ((index = str.indexOf('&', last)) != -1) {
			sb.append(str.substring(last, index));
			indexSemi = str.indexOf(';', index + 1);

			if (indexSemi == -1) {
				sb.append('&');
				last = index + 1;
			}
			else if (index + 1 == indexSemi) {
				sb.append("&;");
				last = index + 2;
			}
			else {
				sb.append(unescapeXMLEntity(str.substring(index + 1, indexSemi)));
				last = indexSemi + 1;
			}
		}
		sb.append(str.substring(last));
		return sb.toString();
	}

	private static String unescapeXMLEntity(String str) {
		if ("lt".equals(str)) return "<";
		if ("gt".equals(str)) return ">";
		if ("amp".equals(str)) return "&";
		if ("apos".equals(str)) return "'";
		if ("quot".equals(str)) return "\"";
		return "&" + str + ";";
	}

	public static String escapeXMLString(String xmlStr) {
		char c;
		StringBuffer sb = new StringBuffer();
		int len = xmlStr.length();
		for (int i = 0; i < len; i++) {
			c = xmlStr.charAt(i);
			if (c == '<') sb.append("&lt;");
			else if (c == '>') sb.append("&gt;");
			else if (c == '&') sb.append("&amp;");
			// else if(c=='\'') sb.append("&amp;");
			else if (c == '"') sb.append("&quot;");
			// else if(c>127) sb.append("&#"+((int)c)+";");
			else sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * @return returns a singelton TransformerFactory
	 */
	public static TransformerFactory getTransformerFactory() {
		if (transformerFactory == null) transformerFactory = _newTransformerFactory();
		return transformerFactory;
	}

	public static String getTransformerFactoryName() {
		return getTransformerFactory().getClass().getName();
	}

	public static URL getTransformerFactoryResource() throws IOException {
		if (transformerFactoryResource == null) {
			String name = getTransformerFactoryName();
			Resource localFile = SystemUtil.getTempDirectory().getRealResource(name.replace('\\', '_').replace('/', '_'));
			IOUtil.write(localFile, name.getBytes());
			transformerFactoryResource = ((File) localFile).toURI().toURL();
		}
		return transformerFactoryResource;
	}

	private static TransformerFactory _newTransformerFactory() {

		Thread.currentThread().setContextClassLoader(new EnvClassLoader((ConfigImpl) ThreadLocalPageContext.getConfig()));
		TransformerFactory factory = null;
		Class clazz = null;
		try {
			clazz = ClassUtil.loadClass("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		}
		catch (Exception e) {
			try {
				clazz = ClassUtil.loadClass("org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
			}
			catch (Exception ee) {}
		}
		if (clazz != null) {
			try {
				factory = (TransformerFactory) ClassUtil.loadInstance(clazz);
			}
			catch (Exception e) {}
		}
		if (factory == null) return factory = TransformerFactory.newInstance();
		LogUtil.log(null, Log.LEVEL_INFO, "application", "xml", factory.getClass().getName() + " is used as TransformerFactory");

		return factory;
	}

	public static final Document parse(InputSource xml, InputSource validator, boolean isHtml) throws SAXException, IOException {
		return parse(xml, validator, new XMLEntityResolverDefaultHandler(validator), isHtml);
	}

	/**
	 * parse XML/HTML String to a XML DOM representation
	 *
	 * @param xml XML InputSource
	 * @param isHtml is a HTML or XML Object
	 * @return parsed Document
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static final Document parse(InputSource xml, InputSource validator, EntityResolver entRes, boolean isHtml) throws SAXException, IOException {

		if (!isHtml) {
			DocumentBuilderFactory factory = newDocumentBuilderFactory(validator);

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				if (entRes != null) builder.setEntityResolver(entRes);
				builder.setErrorHandler(new ThrowingErrorHandler(true, true, false));
				return builder.parse(xml);
			}
			catch (ParserConfigurationException e) {
				throw new SAXException(e);
			}
		}

		XMLReader reader = new Parser();
		reader.setFeature(Parser.namespacesFeature, true);
		reader.setFeature(Parser.namespacePrefixesFeature, true);

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();

			DOMResult result = new DOMResult();
			transformer.transform(new SAXSource(reader, xml), result);
			return XMLUtil.getDocument(result.getNode());
		}
		catch (Exception e) {
			throw new SAXException(e);
		}
	}

	private static DocumentBuilderFactory newDocumentBuilderFactory(InputSource validator) {
		DocumentBuilderFactory factory;
		if (validator != null) {
			factory = _newDocumentBuilderFactory();// DocumentBuilderFactory.newInstance();
			XMLUtil.setAttributeEL(factory, XMLConstants.VALIDATION_SCHEMA, Boolean.TRUE);
			XMLUtil.setAttributeEL(factory, XMLConstants.VALIDATION_SCHEMA_FULL_CHECKING, Boolean.TRUE);
			factory.setNamespaceAware(true);
			factory.setValidating(true);
		}
		else {
			factory = _newDocumentBuilderFactory();// DocumentBuilderFactory.newInstance();
			XMLUtil.setAttributeEL(factory, XMLConstants.NON_VALIDATING_DTD_EXTERNAL, Boolean.FALSE);
			XMLUtil.setAttributeEL(factory, XMLConstants.NON_VALIDATING_DTD_GRAMMAR, Boolean.FALSE);
			factory.setNamespaceAware(true);
			factory.setValidating(false);
		}

		PageContext pc = ThreadLocalPageContext.get();
		if (pc != null) {
			ApplicationContextSupport ac = ((ApplicationContextSupport) pc.getApplicationContext());
			Struct features = ac == null ? null : ac.getXmlFeatures();
			if (features != null) {
				try { // handle feature aliases, e.g. secure
					Object obj;
					boolean featureValue;
					obj = features.get(KEY_FEATURE_SECURE, null);
					if (obj != null) {
						featureValue = Caster.toBoolean(obj);
						if (featureValue) {
							// set features per
							// https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
							factory.setFeature(XMLConstants.FEATURE_DISALLOW_DOCTYPE_DECL, true);
							factory.setFeature(XMLConstants.FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
							factory.setFeature(XMLConstants.FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
							factory.setFeature(XMLConstants.FEATURE_NONVALIDATING_LOAD_EXTERNAL_DTD, false);
							factory.setXIncludeAware(false);
							factory.setExpandEntityReferences(false);
							factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
							factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
						}
						features.remove(KEY_FEATURE_SECURE);
					}

					obj = features.get(KEY_FEATURE_DISALLOW_DOCTYPE_DECL, null);
					if (obj != null) {
						featureValue = Caster.toBoolean(obj);
						factory.setFeature(XMLConstants.FEATURE_DISALLOW_DOCTYPE_DECL, featureValue);
						features.remove(KEY_FEATURE_DISALLOW_DOCTYPE_DECL);
					}

					obj = features.get(KEY_FEATURE_EXTERNAL_GENERAL_ENTITIES, null);
					if (obj != null) {
						featureValue = Caster.toBoolean(obj);
						factory.setFeature(XMLConstants.FEATURE_EXTERNAL_GENERAL_ENTITIES, featureValue);
						features.remove(KEY_FEATURE_EXTERNAL_GENERAL_ENTITIES);
					}
				}
				catch (PageException | ParserConfigurationException ex) {
					throw new RuntimeException(ex);
				}

				features.forEach((k, v) -> {
					try {
						factory.setFeature(k.toString().toLowerCase(), Caster.toBoolean(v));
					}
					catch (PageException | ParserConfigurationException ex) {
						throw new RuntimeException(ex);
					}
				});
			}
		}

		return factory;
	}

	private static Class<DocumentBuilderFactory> dbf;

	private static URL documentBuilderFactoryResource;

	private static URL saxParserFactoryResource;

	private static Class<DocumentBuilderFactory> _newDocumentBuilderFactoryClass() {
		if (dbf == null) {
			Thread.currentThread().setContextClassLoader(new EnvClassLoader((ConfigImpl) ThreadLocalPageContext.getConfig()));
			Class<DocumentBuilderFactory> clazz = null;
			try {
				clazz = ClassUtil.loadClass("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
			}
			catch (Exception e) {
				try {
					clazz = ClassUtil.loadClass("org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
				}
				catch (Exception ee) {}
			}
			if (clazz != null) {
				dbf = clazz;
				LogUtil.log(null, Log.LEVEL_INFO, "application", "xml", clazz.getName() + " is used as DocumentBuilderFactory");
			}
		}
		return dbf;
	}

	public static String getXMLParserConfigurationName() {
		String value = "org.apache.xerces.parsers.XIncludeAwareParserConfiguration";
		System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration", value);
		return value; // TODO better impl, still used?
	}

	public static String getDocumentBuilderFactoryName() {
		Class<DocumentBuilderFactory> clazz = _newDocumentBuilderFactoryClass();
		if (clazz != null) return clazz.getName();
		return DocumentBuilderFactory.newInstance().getClass().getName();
	}

	public static URL getDocumentBuilderFactoryResource() throws IOException {
		if (documentBuilderFactoryResource == null) {
			String name = getDocumentBuilderFactoryName();
			Resource localFile = SystemUtil.getTempDirectory().getRealResource(name.replace('\\', '_').replace('/', '_'));
			IOUtil.write(localFile, name.getBytes());
			documentBuilderFactoryResource = ((File) localFile).toURI().toURL();
		}
		return documentBuilderFactoryResource;
	}

	private static DocumentBuilderFactory _newDocumentBuilderFactory() {
		Class<DocumentBuilderFactory> clazz = _newDocumentBuilderFactoryClass();
		DocumentBuilderFactory factory = null;
		if (clazz != null) {
			try {
				factory = (DocumentBuilderFactory) ClassUtil.loadInstance(clazz);
			}
			catch (Exception e) {}
		}
		if (factory == null) factory = DocumentBuilderFactory.newInstance();
		return factory;
	}

	private static SAXParserFactory newSAXParserFactory() {
		if (saxParserFactory == null) {
			Thread.currentThread().setContextClassLoader(new EnvClassLoader((ConfigImpl) ThreadLocalPageContext.getConfig()));
			saxParserFactory = SAXParserFactory.newInstance();
		}
		return saxParserFactory;
	}

	public static String getSAXParserFactoryName() {
		return newSAXParserFactory().getClass().getName();
	}

	public static URL getSAXParserFactoryResource() throws IOException {
		if (saxParserFactoryResource == null) {
			String name = getSAXParserFactoryName();
			Resource localFile = SystemUtil.getTempDirectory().getRealResource(name.replace('\\', '_').replace('/', '_'));
			IOUtil.write(localFile, name.getBytes());
			saxParserFactoryResource = ((File) localFile).toURI().toURL();
		}
		return saxParserFactoryResource;
	}

	public static XMLReader createXMLReader() throws SAXException {
		Thread.currentThread().setContextClassLoader(new EnvClassLoader((ConfigImpl) ThreadLocalPageContext.getConfig()));
		try {
			return XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		}
		catch (Exception e) {}

		try {
			return XMLReaderFactory.createXMLReader("org.apache.xerces.internal.parsers.SAXParser");
		}
		catch (Exception ee) {}

		try {
			return XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		}
		catch (Exception ee) {}

		try {
			return newSAXParserFactory().newSAXParser().getXMLReader();
		}
		catch (ParserConfigurationException pce) {
			throw new RuntimeException(pce);
		}

	}

	private static void setAttributeEL(DocumentBuilderFactory factory, String name, Object value) {
		try {
			factory.setAttribute(name, value);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	/**
	 * sets a node to a node (Expression Less)
	 *
	 * @param node
	 * @param key
	 * @param value
	 * @return Object set
	 */
	public static Object setPropertyEL(Node node, Collection.Key key, Object value) {
		try {
			return setProperty(node, key, value);
		}
		catch (PageException e) {
			return null;
		}
	}

	public static Object setProperty(Node node, Collection.Key key, Object value, boolean caseSensitive, Object defaultValue) {
		try {
			return setProperty(node, key, value, caseSensitive);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	/**
	 * sets a node to a node
	 *
	 * @param node
	 * @param k
	 * @param value
	 * @return Object set
	 * @throws PageException
	 */
	public static Object setProperty(Node node, Collection.Key k, Object value) throws PageException {
		return setProperty(node, k, value, isCaseSensitve(node));
	}

	public static Object setProperty(Node node, Collection.Key k, Object value, boolean caseSensitive) throws PageException {
		Document doc = getDocument(node);
		boolean isXMLChildren;
		// Comment
		if (k.equals(XMLCOMMENT)) {
			removeChildren(XMLCaster.toRawNode(node), Node.COMMENT_NODE, false);
			node.appendChild(XMLCaster.toRawNode(XMLCaster.toComment(doc, value)));
		}
		// NS URI
		else if (k.equals(XMLNSURI)) {
			// TODO impl
			throw new ExpressionException("XML NS URI can't be set", "not implemented");
		}
		// Prefix
		else if (k.equals(XMLNSPREFIX)) {
			// TODO impl
			throw new ExpressionException("XML NS Prefix can't be set", "not implemented");
			// node.setPrefix(Caster.toString(value));
		}
		// Root
		else if (k.equals(XMLROOT)) {
			doc.appendChild(XMLCaster.toNode(doc, value, false));
		}
		// Parent
		else if (k.equals(XMLPARENT)) {
			Node parent = getParentNode(node, caseSensitive);
			Key name = KeyImpl.init(parent.getNodeName());
			parent = getParentNode(parent, caseSensitive);

			if (parent == null) throw new ExpressionException("there is no parent element, you are already on the root element");

			return setProperty(parent, name, value, caseSensitive);
		}
		// Name
		else if (k.equals(XMLNAME)) {
			throw new XMLException("You can't assign a new value for the property [xmlname]");
		}
		// Type
		else if (k.equals(XMLTYPE)) {
			throw new XMLException("You can't change type of a xml node [xmltype]");
		}
		// value
		else if (k.equals(XMLVALUE)) {
			node.setNodeValue(Caster.toString(value));
		}
		// Attributes
		else if (k.equals(XMLATTRIBUTES)) {
			Element parent = XMLCaster.toElement(doc, node);
			Attr[] attres = XMLCaster.toAttrArray(doc, value);
			// print.ln("=>"+value);
			for (int i = 0; i < attres.length; i++) {
				if (attres[i] != null) {
					parent.setAttributeNode(attres[i]);
					// print.ln(attres[i].getName()+"=="+attres[i].getValue());
				}
			}
		}
		// Text
		else if (k.equals(XMLTEXT)) {
			removeChildCharacterData(XMLCaster.toRawNode(node), false);
			node.appendChild(XMLCaster.toRawNode(XMLCaster.toText(doc, value)));
		}
		// CData
		else if (k.equals(XMLCDATA)) {
			removeChildCharacterData(XMLCaster.toRawNode(node), false);
			node.appendChild(XMLCaster.toRawNode(XMLCaster.toCDATASection(doc, value)));
		}
		// Children
		else if ((isXMLChildren = k.equals(XMLCHILDREN)) || k.equals(XMLNODES)) {
			Node[] nodes = XMLCaster.toNodeArray(doc, value);
			removeChildren(XMLCaster.toRawNode(node), isXMLChildren ? Node.ELEMENT_NODE : XMLUtil.UNDEFINED_NODE, false);
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i] == node) throw new XMLException("can't assign a XML Node to himself");
				if (nodes[i] != null) node.appendChild(XMLCaster.toRawNode(nodes[i]));
			}
		}
		else {
			boolean isIndex = false;
			Node child = XMLCaster.toNode(doc, value, false);
			if (!k.getString().equalsIgnoreCase(child.getNodeName()) && !(isIndex = Decision.isInteger(k))) {
				throw new XMLException("if you assign a XML Element to a XMLStruct , assignment property must have same name like XML Node Name",
						"Property Name is " + k.getString() + " and XML Element Name is " + child.getNodeName());
			}
			Node n;

			// by index
			if (isIndex) {
				NodeList list = XMLUtil.getChildNodes(node.getParentNode(), Node.ELEMENT_NODE, true, node.getNodeName());
				int len = list.getLength();

				int index = Caster.toIntValue(k);
				if (index > len || index < 1) {
					String detail = len > 1 ? "your index is " + index + ", but there are only " + len + " child elements"
							: "your index is " + index + ", but there is only " + len + " child element";

					throw new XMLException("index is out of range", detail);
				}
				n = list.item(index - 1);
				XMLUtil.replaceChild(child, n);
				return value;
			}

			NodeList list = XMLUtil.getChildNodes(node, Node.ELEMENT_NODE);
			int len = list.getLength();

			// by name
			for (int i = 0; i < len; i++) {
				n = list.item(i);
				if (nameEqual(n, k.getString(), caseSensitive)) {
					XMLUtil.replaceChild(child, n);
					return value;
				}
			}
			node.appendChild(XMLCaster.toRawNode(child));
		}

		return value;
	}

	public static void replaceChild(Node newChild, Node oldChild) {

		Node nc = XMLCaster.toRawNode(newChild);
		Node oc = XMLCaster.toRawNode(oldChild);
		Node p = oc.getParentNode();

		if (nc != oc) p.replaceChild(nc, oc);
	}

	public static Object getProperty(Node node, Collection.Key key, Object defaultValue) {
		return getProperty(node, key, isCaseSensitve(node), defaultValue);
	}

	/**
	 * returns a property from a XMl Node (Expression Less)
	 *
	 * @param node
	 * @param k
	 * @param caseSensitive
	 * @return Object matching key
	 */
	public static Object getProperty(Node node, Collection.Key k, boolean caseSensitive, Object defaultValue) {
		try {
			return getProperty(node, k, caseSensitive);
		}
		catch (SAXException e) {
			return defaultValue;
		}
	}

	public static Object getProperty(Node node, Collection.Key key) throws SAXException {
		return getProperty(node, key, isCaseSensitve(node));
	}

	/**
	 * returns a property from a XMl Node
	 *
	 * @param node
	 * @param k
	 * @param caseSensitive
	 * @return Object matching key
	 * @throws SAXException
	 */
	public static Object getProperty(Node node, Collection.Key k, boolean caseSensitive) throws SAXException {
		// String lcKey=StringUtil.toLowerCase(key);
		if (k.getLowerString().startsWith("xml")) {
			// Comment
			if (k.equals(XMLCOMMENT)) {
				StringBuffer sb = new StringBuffer();
				NodeList list = node.getChildNodes();
				int len = list.getLength();
				for (int i = 0; i < len; i++) {
					Node n = list.item(i);
					if (n instanceof Comment) {
						sb.append(((Comment) n).getData());
					}
				}
				return sb.toString();
			}
			// NS URI
			if (k.equals(XMLNSURI)) {
				undefinedInRoot(k, node);
				return param(node.getNamespaceURI(), "");
			}
			// Prefix
			if (k.equals(XMLNSPREFIX)) {
				undefinedInRoot(k, node);
				return param(node.getPrefix(), "");
			}
			// Root
			else if (k.equals(XMLROOT)) {
				Element re = getRootElement(node, caseSensitive);
				if (re == null) throw new SAXException("Attribute [" + k.getString() + "] not found in XML, XML is empty");
				return param(re, "");
			}
			// Parent
			else if (k.equals(XMLPARENT)) {

				Node parent = getParentNode(node, caseSensitive);
				if (parent == null) {
					if (node.getNodeType() == Node.DOCUMENT_NODE)
						throw new SAXException("Attribute [" + k.getString() + "] not found in XML, there is no parent element, you are already at the root element");
					throw new SAXException("Attribute [" + k.getString() + "] not found in XML, there is no parent element");
				}
				return parent;
			}
			// Name
			else if (k.equals(XMLNAME)) {
				return node.getNodeName();
			}
			// Value
			else if (k.equals(XMLVALUE)) {
				return StringUtil.toStringEmptyIfNull(node.getNodeValue());
			}
			// Type
			else if (k.equals(XMLTYPE)) {
				return getTypeAsString(node, true);
			}
			// Attributes
			else if (k.equals(XMLATTRIBUTES)) {
				NamedNodeMap attr = node.getAttributes();

				if (attr == null) throw undefined(k, node);
				return new XMLAttributes(node, caseSensitive);
			}
			// Text
			else if (k.equals(XMLTEXT)) {
				undefinedInRoot(k, node);

				if (node instanceof Text || node instanceof CDATASection) return ((CharacterData) node).getData();

				StringBuilder sb = new StringBuilder();
				NodeList list = node.getChildNodes();
				int len = list.getLength();
				for (int i = 0; i < len; i++) {
					Node n = list.item(i);
					if (n instanceof Text || n instanceof CDATASection) {
						sb.append(((CharacterData) n).getData());
					}
				}
				return sb.toString();
			}
			// CData
			else if (k.equals(XMLCDATA)) {
				undefinedInRoot(k, node);
				StringBuffer sb = new StringBuffer();
				NodeList list = node.getChildNodes();
				int len = list.getLength();
				for (int i = 0; i < len; i++) {
					Node n = list.item(i);
					if (n instanceof Text || n instanceof CDATASection) {
						sb.append(((CharacterData) n).getData());
					}
				}
				return sb.toString();
			}
			// Children
			else if (k.equals(XMLCHILDREN)) {
				return new XMLNodeList(node, caseSensitive, Node.ELEMENT_NODE);
			}
			// Nodes
			else if (k.equals(XMLNODES)) {
				return new XMLNodeList(node, caseSensitive, XMLUtil.UNDEFINED_NODE);
			}
		}

		if (node instanceof Document) {
			node = ((Document) node).getDocumentElement();
			if (node == null) throw new SAXException("Attribute [" + k.getString() + "] not found in XML, XML is empty");

			// if((!caseSensitive && node.getNodeName().equalsIgnoreCase(k.getString())) ||
			// (caseSensitive &&
			// node.getNodeName().equals(k.getString()))) {
			if (nameEqual(node, k.getString(), caseSensitive)) {
				return XMLStructFactory.newInstance(node, caseSensitive);
			}
		}
		else if (node.getNodeType() == Node.ELEMENT_NODE && Decision.isInteger(k)) {
			int index = Caster.toIntValue(k, 0);
			int count = 0;
			Node parent = node.getParentNode();
			String nodeName = node.getNodeName();
			Element[] children = XMLUtil.getChildElementsAsArray(parent);

			for (int i = 0; i < children.length; i++) {
				if (XMLUtil.nameEqual(children[i], nodeName, caseSensitive)) count++;

				if (count == index) return XMLCaster.toXMLStruct(children[i], caseSensitive);
			}
			String detail;
			if (count == 0) detail = "there are no Elements with this name";
			else if (count == 1) detail = "there is only 1 Element with this name";
			else detail = "there are only " + count + " Elements with this name";
			throw new SAXException("invalid index [" + k.getString() + "] for Element with name [" + node.getNodeName() + "], " + detail);
		}
		else {
			List<Node> children = XMLUtil.getChildNodesAsList(node, Node.ELEMENT_NODE, caseSensitive, null);
			int len = children.size();
			Array array = null;// new ArrayImpl();
			Element el;
			XMLStruct sct = null, first = null;
			for (int i = 0; i < len; i++) {
				el = (Element) children.get(i);// XMLCaster.toXMLStruct(getChildNode(index),caseSensitive);
				if (XMLUtil.nameEqual(el, k.getString(), caseSensitive)) {
					sct = XMLCaster.toXMLStruct(el, caseSensitive);

					if (array != null) {
						array.appendEL(sct);
					}
					else if (first != null) {
						array = new ArrayImpl();
						array.appendEL(first);
						array.appendEL(sct);
					}
					else {
						first = sct;
					}
				}
			}

			if (array != null) {
				try {
					return new XMLMultiElementStruct(array, false);
				}
				catch (PageException e) {}
			}
			if (first != null) return first;
		}
		throw new SAXException("Attribute [" + k.getString() + "] not found");
	}

	private static SAXException undefined(Key key, Node node) {
		if (node.getNodeType() == Node.DOCUMENT_NODE) return new SAXException(
				"you cannot address [" + key + "] on the Document Object, to address [" + key + "]  from the root Node use [{variable-name}.xmlRoot." + key + "]");

		return new SAXException(key + " is undefined");
	}

	private static void undefinedInRoot(Key key, Node node) throws SAXException {
		if (node.getNodeType() == Node.DOCUMENT_NODE) throw undefined(key, node);
	}

	/**
	 * check if given name is equal to name of the element (with and without namespace)
	 *
	 * @param node
	 * @param name
	 * @param caseSensitive
	 * @return
	 */
	public static boolean nameEqual(Node node, String name, boolean caseSensitive) {
		if (name == null) return false;
		if (caseSensitive) {
			return name.equals(node.getNodeName()) || name.equals(node.getLocalName());
		}
		return name.equalsIgnoreCase(node.getNodeName()) || name.equalsIgnoreCase(node.getLocalName());
	}

	public static boolean isCaseSensitve(Node node) {
		if (node instanceof XMLStruct) return ((XMLStruct) node).isCaseSensitive();
		return true;
	}

	/**
	 * removes child from a node
	 *
	 * @param node
	 * @param k
	 * @param caseSensitive
	 * @return removed property
	 */
	public static Object removeProperty(Node node, Collection.Key k, boolean caseSensitive) {
		boolean isXMLChildren;
		// String lcKeyx=k.getLowerString();
		if (k.getLowerString().startsWith("xml")) {
			// Comment
			if (k.equals(XMLCOMMENT)) {
				StringBuffer sb = new StringBuffer();
				NodeList list = node.getChildNodes();
				int len = list.getLength();
				for (int i = 0; i < len; i++) {
					Node n = list.item(i);
					if (n instanceof Comment) {
						sb.append(((Comment) n).getData());
						node.removeChild(XMLCaster.toRawNode(n));
					}
				}
				return sb.toString();
			}
			// Text
			else if (k.equals(XMLTEXT)) {
				if (node instanceof Text || node instanceof CDATASection) return ((CharacterData) node).getData();

				StringBuilder sb = new StringBuilder();
				NodeList list = node.getChildNodes();
				int len = list.getLength();
				for (int i = 0; i < len; i++) {
					Node n = list.item(i);
					if (n instanceof Text || n instanceof CDATASection) {
						sb.append(((CharacterData) n).getData());
						node.removeChild(XMLCaster.toRawNode(n));
					}
				}
				return sb.toString();
			}
			// children
			else if ((isXMLChildren = k.equals(XMLCHILDREN)) || k.equals(XMLNODES)) {
				NodeList list = node.getChildNodes();
				Node child;
				for (int i = list.getLength() - 1; i >= 0; i--) {
					child = XMLCaster.toRawNode(list.item(i));
					if (isXMLChildren && child.getNodeType() != Node.ELEMENT_NODE) continue;
					node.removeChild(child);
				}
				return list;
			}
		}

		NodeList nodes = node.getChildNodes();
		Array array = new ArrayImpl();
		for (int i = nodes.getLength() - 1; i >= 0; i--) {
			Object o = nodes.item(i);
			if (o instanceof Element) {
				Element el = (Element) o;
				if (nameEqual(el, k.getString(), caseSensitive)) {
					array.appendEL(XMLCaster.toXMLStruct(el, caseSensitive));
					node.removeChild(XMLCaster.toRawNode(el));
				}
			}
		}

		if (array.size() > 0) {
			try {
				return new XMLMultiElementStruct(array, false);
			}
			catch (PageException e) {}
		}
		return null;
	}

	private static Object param(Object o1, Object o2) {
		if (o1 == null) return o2;
		return o1;
	}

	/**
	 * return the root Element from a node
	 *
	 * @param node node to get root element from
	 * @param caseSensitive
	 * @return Root Element
	 */
	public static Element getRootElement(Node node, boolean caseSensitive) {
		Document doc = XMLUtil.getDocument(node);
		Element el = doc.getDocumentElement();
		if (el == null) return null;
		return (Element) XMLStructFactory.newInstance(el, caseSensitive);
	}

	public static Node getParentNode(Node node, boolean caseSensitive) {
		Node parent = node.getParentNode();
		if (parent == null) return null;
		return XMLStructFactory.newInstance(parent, caseSensitive);
	}

	/**
	 * returns a new Empty XMl Document
	 *
	 * @return new Document
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 */
	public static Document newDocument() throws ParserConfigurationException, FactoryConfigurationError {
		if (docBuilder == null) {
			docBuilder = newDocumentBuilderFactory(null).newDocumentBuilder();
		}
		return docBuilder.newDocument();
	}

	/**
	 * return the Owner Document of a Node List
	 *
	 * @param nodeList
	 * @return XML Document
	 * @throws XMLException
	 */
	public static Document getDocument(NodeList nodeList) throws XMLException {
		if (nodeList instanceof Document) return (Document) nodeList;
		int len = nodeList.getLength();
		for (int i = 0; i < len; i++) {
			Node node = nodeList.item(i);
			if (node != null) return node.getOwnerDocument();
		}
		throw new XMLException("can't get Document from NodeList, in NoteList are no Nodes");
	}

	/**
	 * return the Owner Document of a Node
	 *
	 * @param node
	 * @return XML Document
	 */
	public static Document getDocument(Node node) {
		if (node instanceof Document) return (Document) node;
		return node.getOwnerDocument();
	}

	/**
	 * removes child elements from a specific type
	 *
	 * @param node node to remove elements from
	 * @param type Type Definition to remove (Constant value from class Node)
	 * @param deep remove also in sub nodes
	 */
	private static void removeChildren(Node node, short type, boolean deep) {
		synchronized (sync(node)) {
			NodeList list = node.getChildNodes();

			for (int i = list.getLength(); i >= 0; i--) {
				Node n = list.item(i);
				if (n == null) continue;

				if (n.getNodeType() == type || type == UNDEFINED_NODE) node.removeChild(XMLCaster.toRawNode(n));
				else if (deep) removeChildren(n, type, deep);
			}
		}
	}

	/**
	 * remove children from type CharacterData from a node, this includes Text,Comment and CDataSection
	 * nodes
	 *
	 * @param node
	 * @param deep
	 */
	private static void removeChildCharacterData(Node node, boolean deep) {
		synchronized (sync(node)) {
			NodeList list = node.getChildNodes();

			for (int i = list.getLength(); i >= 0; i--) {
				Node n = list.item(i);
				if (n == null) continue;

				if (n instanceof CharacterData) node.removeChild(XMLCaster.toRawNode(n));
				else if (deep) removeChildCharacterData(n, deep);
			}
		}
	}

	/**
	 * return all Children of a node by a defined type as Node List
	 *
	 * @param node node to get children from
	 * @param type type of returned node
	 * @return all matching child node
	 */
	public static ArrayNodeList getChildNodes(Node node, short type) {
		return getChildNodes(node, type, false, null);
	}

	public static int childNodesLength(Node node, short type, boolean caseSensitive, String filter) {
		synchronized (sync(node)) {
			NodeList nodes = node.getChildNodes();
			int len = nodes.getLength();
			Node n;
			int count = 0;
			for (int i = 0; i < len; i++) {
				try {
					n = nodes.item(i);
					if (n != null && (type == UNDEFINED_NODE || n.getNodeType() == type)) {
						if (filter == null || (caseSensitive ? filter.equals(n.getLocalName()) : filter.equalsIgnoreCase(n.getLocalName()))) count++;
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
			return count;
		}
	}

	public static Object sync(Node node) {
		Document d = getDocument(node);
		if (d != null) return d;
		return node;
	}

	public synchronized static ArrayNodeList getChildNodes(Node node, short type, boolean caseSensitive, String filter) {
		ArrayNodeList rtn = new ArrayNodeList();
		NodeList nodes = node == null ? null : node.getChildNodes();
		int len = nodes == null ? 0 : nodes.getLength();
		Node n;
		for (int i = 0; i < len; i++) {
			try {
				n = nodes.item(i);
				if (n != null && (type == UNDEFINED_NODE || n.getNodeType() == type)) {
					if (filter == null || (caseSensitive ? filter.equals(n.getLocalName()) : filter.equalsIgnoreCase(n.getLocalName()))) rtn.add(n);
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
		return rtn;
	}

	public static List<Node> getChildNodesAsList(Node node, short type, boolean caseSensitive, String filter) {
		synchronized (sync(node)) {
			List<Node> rtn = new ArrayList<Node>();
			NodeList nodes = node.getChildNodes();
			int len = nodes.getLength();
			Node n;
			for (int i = 0; i < len; i++) {
				try {
					n = nodes.item(i);
					if (n != null && (n.getNodeType() == type || type == UNDEFINED_NODE)) {
						if (filter == null || (caseSensitive ? filter.equals(n.getLocalName()) : filter.equalsIgnoreCase(n.getLocalName()))) rtn.add(n);
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
			return rtn;
		}
	}

	public static Node getChildNode(Node node, short type, boolean caseSensitive, String filter, int index) {
		synchronized (sync(node)) {
			NodeList nodes = node.getChildNodes();
			int len = nodes.getLength();
			Node n;
			int count = 0;
			for (int i = 0; i < len; i++) {
				try {
					n = nodes.item(i);
					if (n != null && (type == UNDEFINED_NODE || n.getNodeType() == type)) {
						if (filter == null || (caseSensitive ? filter.equals(n.getLocalName()) : filter.equalsIgnoreCase(n.getLocalName()))) {
							if (count == index) return n;
							count++;
						}
					}
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
				}
			}
			return null;
		}
	}

	/**
	 * return all Children of a node by a defined type as Node Array
	 *
	 * @param node node to get children from
	 * @param type type of returned node
	 * @return all matching child node
	 */
	public static Node[] getChildNodesAsArray(Node node, short type) {
		ArrayNodeList nodeList = getChildNodes(node, type);
		return nodeList.toArray(new Node[nodeList.getLength()]);
	}

	public static Node[] getChildNodesAsArray(Node node, short type, boolean caseSensitive, String filter) {
		ArrayNodeList nodeList = getChildNodes(node, type, caseSensitive, filter);
		return nodeList.toArray(new Node[nodeList.getLength()]);
	}

	/**
	 * return all Element Children of a node
	 *
	 * @param node node to get children from
	 * @return all matching child node
	 */
	public static Element[] getChildElementsAsArray(Node node) {
		ArrayNodeList nodeList = getChildNodes(node, Node.ELEMENT_NODE);
		return nodeList.toArray(new Element[nodeList.getLength()]);
	}

	/**
	 * transform a XML Object to another format, with help of a XSL Stylesheet
	 *
	 * @param xml xml to convert
	 * @param xsl xsl used to convert
	 * @return resulting string
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static String transform(InputSource xml, InputSource xsl) throws TransformerException, SAXException, IOException {
		return transform(parse(xml, null, false), xsl, null);
	}

	/**
	 * transform a XML Object to another format, with help of a XSL Stylesheet
	 *
	 * @param xml xml to convert
	 * @param xsl xsl used to convert
	 * @param parameters parameters used to convert
	 * @return resulting string
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static String transform(InputSource xml, InputSource xsl, Map<String, Object> parameters) throws TransformerException, SAXException, IOException {
		return transform(parse(xml, null, false), xsl, parameters);
	}

	/**
	 * transform a XML Document to another format, with help of a XSL Stylesheet
	 *
	 * @param doc xml to convert
	 * @param xsl xsl used to convert
	 * @return resulting string
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static String transform(Document doc, InputSource xsl) throws TransformerException {
		return transform(doc, xsl, null);
	}

	/**
	 * transform a XML Document to another format, with help of a XSL Stylesheet
	 *
	 * @param doc xml to convert
	 * @param xsl xsl used to convert
	 * @param parameters parameters used to convert
	 * @return resulting string
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static String transform(Document doc, InputSource xsl, Map<String, Object> parameters) throws TransformerException {
		StringWriter sw = new StringWriter();
		TransformerFactory factory = getTransformerFactory();
		factory.setErrorListener(SimpleErrorListener.THROW_FATAL);
		Transformer transformer = factory.newTransformer(new StreamSource(xsl.getCharacterStream()));
		if (parameters != null) {
			Iterator<Entry<String, Object>> it = parameters.entrySet().iterator();
			Entry<String, Object> e;
			while (it.hasNext()) {
				e = it.next();
				transformer.setParameter(e.getKey(), e.getValue());
			}
		}
		transformer.transform(new DOMSource(doc), new StreamResult(sw));
		return sw.toString();
	}

	/**
	 * returns the Node Type As String
	 *
	 * @param node
	 * @param cftype
	 * @return
	 */
	public static String getTypeAsString(Node node, boolean cftype) {
		String suffix = cftype ? "" : "_NODE";

		switch (node.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			return "ATTRIBUTE" + suffix;
		case Node.CDATA_SECTION_NODE:
			return "CDATA_SECTION" + suffix;
		case Node.COMMENT_NODE:
			return "COMMENT" + suffix;
		case Node.DOCUMENT_FRAGMENT_NODE:
			return "DOCUMENT_FRAGMENT" + suffix;
		case Node.DOCUMENT_NODE:
			return "DOCUMENT" + suffix;
		case Node.DOCUMENT_TYPE_NODE:
			return "DOCUMENT_TYPE" + suffix;
		case Node.ELEMENT_NODE:
			return "ELEMENT" + suffix;
		case Node.ENTITY_NODE:
			return "ENTITY" + suffix;
		case Node.ENTITY_REFERENCE_NODE:
			return "ENTITY_REFERENCE" + suffix;
		case Node.NOTATION_NODE:
			return "NOTATION" + suffix;
		case Node.PROCESSING_INSTRUCTION_NODE:
			return "PROCESSING_INSTRUCTION" + suffix;
		case Node.TEXT_NODE:
			return "TEXT" + suffix;
		default:
			return "UNKNOW" + suffix;
		}
	}

	public static Element getChildWithName(String name, Element el) {
		synchronized (sync(el)) {
			Element[] children = XMLUtil.getChildElementsAsArray(el);
			for (int i = 0; i < children.length; i++) {
				if (name.equalsIgnoreCase(children[i].getNodeName())) return children[i];
			}
		}
		return null;
	}

	public static InputSource toInputSource(Resource res, Charset cs) throws IOException {
		String str = IOUtil.toString((res), cs);
		return new InputSource(new StringReader(str));
	}

	public static InputSource toInputSource(PageContext pc, Object value) throws IOException, ExpressionException {
		if (value instanceof InputSource) {
			return (InputSource) value;
		}
		if (value instanceof String) {
			return toInputSource(pc, (String) value);
		}
		if (value instanceof StringBuffer) {
			return toInputSource(pc, value.toString());
		}
		if (value instanceof Resource) {
			String str = IOUtil.toString(((Resource) value), (Charset) null);
			return new InputSource(new StringReader(str));
		}
		if (value instanceof File) {
			String str = IOUtil.toString(ResourceUtil.toResource(((File) value)), (Charset) null);
			return new InputSource(new StringReader(str));
		}
		if (value instanceof InputStream) {
			InputStream is = (InputStream) value;
			try {
				String str = IOUtil.toString(is, (Charset) null);
				return new InputSource(new StringReader(str));
			}
			finally {
				IOUtil.closeEL(is);
			}
		}
		if (value instanceof Reader) {
			Reader reader = (Reader) value;
			try {
				String str = IOUtil.toString(reader);
				return new InputSource(new StringReader(str));
			}
			finally {
				IOUtil.closeEL(reader);
			}
		}
		if (value instanceof byte[]) {
			return new InputSource(new ByteArrayInputStream((byte[]) value));
		}
		throw new ExpressionException("can't cast object of type [" + Caster.toClassName(value) + "] to an Input for xml parser");

	}

	public static InputSource toInputSource(PageContext pc, String xml) throws IOException, ExpressionException {
		return toInputSource(pc, xml, true);
	}

	public static InputSource toInputSource(PageContext pc, String xml, boolean canBePath) throws IOException, ExpressionException {
		// xml text
		xml = xml.trim();
		if (!canBePath || xml.startsWith("<") || xml.length() > 2000 || StringUtil.isEmpty(xml, true)) {
			return new InputSource(new StringReader(xml));
		}
		// xml link
		pc = ThreadLocalPageContext.get(pc);
		Resource res = ResourceUtil.toResourceExisting(pc, xml);
		return toInputSource(pc, res);
	}

	/**
	 * adds a child at the first place
	 *
	 * @param parent
	 * @param child
	 */
	public static void prependChild(Element parent, Element child) {
		Node first = parent.getFirstChild();
		if (first == null) parent.appendChild(child);
		else {
			parent.insertBefore(child, first);
		}
	}

	public static void setFirst(Node parent, Node node) {
		Node first = parent.getFirstChild();
		if (first != null) parent.insertBefore(node, first);
		else parent.appendChild(node);
	}

	public static Document createDocument(Resource res, boolean isHTML) throws IOException, XMLException {
		InputStream is = null;
		try {
			return parse(toInputSource(res, null), null, isHTML);
		}
		catch (SAXException saxe) {
			final String msg = saxe.getMessage();
			if (msg != null || StringUtil.indexOfIgnoreCase(msg, "Premature end of file.") != -1) {

				String content = IOUtil.toString(res, CharsetUtil.UTF8);
				String str;
				if (content.isEmpty()) str = "XML File [" + res.getAbsolutePath() + "] is empty;" + saxe.getMessage();
				else if (content.length() > content.trim().length())
					str = "XML File [" + res.getAbsolutePath() + "] is invalid, it has whitespaces at start or end;" + saxe.getMessage();
				else str = "XML File [" + res.getAbsolutePath() + "] is invalid;" + saxe.getMessage();

				XMLException se = new XMLException(str);
				se.setAdditional(KeyImpl.init("path"), res.getAbsolutePath());
				se.setAdditional(KeyImpl.init("content"), content);
				se.setStackTrace(saxe.getStackTrace());

				throw se;
			}
			throw new XMLException(saxe);
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	public static Document createDocument(String xml, boolean isHTML) throws SAXException, IOException {
		return parse(toInputSource(xml), null, isHTML);
	}

	public static Document createDocument(InputStream is, boolean isHTML) throws SAXException, IOException {
		return parse(new InputSource(is), null, isHTML);
	}

	public static InputSource toInputSource(Object value) throws IOException {
		if (value instanceof InputSource) {
			return (InputSource) value;
		}
		if (value instanceof String) {
			return toInputSource((String) value);
		}
		if (value instanceof StringBuffer) {
			return toInputSource(value.toString());
		}

		if (value instanceof Resource) {
			String str = IOUtil.toString(((Resource) value), (Charset) null);
			return new InputSource(new StringReader(str));
		}
		if (value instanceof File) {
			FileInputStream fis = new FileInputStream((File) value);
			try {
				return toInputSource(fis);
			}
			finally {
				IOUtil.closeEL(fis);
			}
		}
		if (value instanceof InputStream) {
			InputStream is = (InputStream) value;
			try {
				String str = IOUtil.toString(is, (Charset) null);
				return new InputSource(new StringReader(str));
			}
			finally {
				IOUtil.closeEL(is);
			}
		}
		if (value instanceof Reader) {
			Reader reader = (Reader) value;
			try {
				String str = IOUtil.toString(reader);
				return new InputSource(new StringReader(str));
			}
			finally {
				IOUtil.closeEL(reader);
			}
		}
		if (value instanceof byte[]) {
			return new InputSource(new ByteArrayInputStream((byte[]) value));
		}
		throw new IOException("can't cast object of type [" + value + "] to an Input for xml parser");
	}

	public static InputSource toInputSource(String xml) throws IOException {
		return new InputSource(new StringReader(xml.trim()));
	}

	public static Struct validate(InputSource xml, InputSource schema, String strSchema) throws XMLException {
		return new XMLValidator(schema, strSchema).validate(xml);
	}

}
