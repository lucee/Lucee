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
package lucee.runtime.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

// FUTURE all this needs to come from core

/**
 * 
 */
public final class XMLUtilImpl implements XMLUtil {

	public final static String NON_VALIDATING_DTD_GRAMMAR = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar";
	public final static String NON_VALIDATING_DTD_EXTERNAL = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

	public final static String VALIDATION_SCHEMA = "http://apache.org/xml/features/validation/schema";
	public final static String VALIDATION_SCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";

	public static final short UNDEFINED_NODE = -1;

	/*
	 * public static final Collection.Key XMLCOMMENT = KeyImpl.intern("xmlcomment"); public static final
	 * Collection.Key XMLTEXT = KeyImpl.intern("xmltext"); public static final Collection.Key XMLCDATA =
	 * KeyImpl.intern("xmlcdata"); public static final Collection.Key XMLCHILDREN =
	 * KeyImpl.intern("xmlchildren"); public static final Collection.Key XMLNODES =
	 * KeyImpl.intern("xmlnodes"); public static final Collection.Key XMLNSURI =
	 * KeyImpl.intern("xmlnsuri"); public static final Collection.Key XMLNSPREFIX =
	 * KeyImpl.intern("xmlnsprefix"); public static final Collection.Key XMLROOT =
	 * KeyImpl.intern("xmlroot"); public static final Collection.Key XMLPARENT =
	 * KeyImpl.intern("xmlparent"); public static final Collection.Key XMLNAME =
	 * KeyImpl.intern("xmlname"); public static final Collection.Key XMLTYPE =
	 * KeyImpl.intern("xmltype"); public static final Collection.Key XMLVALUE =
	 * KeyImpl.intern("xmlvalue"); public static final Collection.Key XMLATTRIBUTES =
	 * KeyImpl.intern("xmlattributes");
	 */

	// static DOMParser parser = new DOMParser();
	private static DocumentBuilder docBuilder;
	// private static DocumentBuilderFactory factory;
	private static TransformerFactory transformerFactory;

	@Override
	public String unescapeXMLString(String str) {

		StringBuilder rtn = new StringBuilder();
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

	/*
	 * public String unescapeXMLString2(String str) {
	 * 
	 * StringBuffer sb=new StringBuffer(); int index,last=0,indexSemi;
	 * while((index=str.indexOf('&',last))!=-1) { sb.append(str.substring(last,index));
	 * indexSemi=str.indexOf(';',index+1);
	 * 
	 * if(indexSemi==-1) { sb.append('&'); last=index+1; } else if(index+1==indexSemi) {
	 * sb.append("&;"); last=index+2; } else {
	 * sb.append(unescapeXMLEntity(str.substring(index+1,indexSemi))); last=indexSemi+1; } }
	 * sb.append(str.substring(last)); return sb.toString(); }
	 */

	private static String unescapeXMLEntity(String str) {
		if ("lt".equals(str)) return "<";
		if ("gt".equals(str)) return ">";
		if ("amp".equals(str)) return "&";
		if ("apos".equals(str)) return "'";
		if ("quot".equals(str)) return "\"";
		return "&" + str + ";";
	}

	@Override
	public String escapeXMLString(String xmlStr) {
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

	@Override
	public TransformerFactory getTransformerFactory() {
		return transformerFactory();
	}

	public static TransformerFactory transformerFactory() {
		if (transformerFactory == null) {
			try {
				Class<?> clazz = CFMLEngineFactory.getInstance().getClassUtil().loadClass("lucee.runtime.text.xml.XMLUtil");
				transformerFactory = (TransformerFactory) clazz.getMethod("getTransformerFactory", new Class[0]).invoke(null, new Object[0]);
			}
			catch (Exception e) {
				e.printStackTrace();
				transformerFactory = TransformerFactory.newInstance();
			}
		}
		return transformerFactory;
	}

	/**
	 * parse XML/HTML String to a XML DOM representation
	 * 
	 * @param xml XML InputSource
	 * @param isHtml is a HTML or XML Object
	 * @return parsed Document
	 * @throws SAXException
	 * @throws IOException
	 */
	@Override
	public final Document parse(InputSource xml, InputSource validator, boolean isHtml) throws SAXException, IOException {

		if (!isHtml) {
			DocumentBuilderFactory factory = newDocumentBuilderFactory();

			// print.o(factory);
			if (validator == null) {
				setAttributeEL(factory, NON_VALIDATING_DTD_EXTERNAL, Boolean.FALSE);
				setAttributeEL(factory, NON_VALIDATING_DTD_GRAMMAR, Boolean.FALSE);
			}
			else {
				setAttributeEL(factory, VALIDATION_SCHEMA, Boolean.TRUE);
				setAttributeEL(factory, VALIDATION_SCHEMA_FULL_CHECKING, Boolean.TRUE);

			}

			factory.setNamespaceAware(true);
			factory.setValidating(validator != null);

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				builder.setEntityResolver(new XMLEntityResolverDefaultHandler(validator));
				builder.setErrorHandler(new ThrowingErrorHandler(true, true, false));
				return builder.parse(xml);
			}
			catch (ParserConfigurationException e) {
				throw new SAXException(e);
			}

			/*
			 * DOMParser parser = new DOMParser(); print.out("parse"); parser.setEntityResolver(new
			 * XMLEntityResolverDefaultHandler(validator)); parser.parse(xml); return parser.getDocument();
			 */
		}

		XMLReader reader = new Parser();
		reader.setFeature(Parser.namespacesFeature, true);
		reader.setFeature(Parser.namespacePrefixesFeature, true);

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();

			DOMResult result = new DOMResult();
			transformer.transform(new SAXSource(reader, xml), result);
			return getDocument(result.getNode());
		}
		catch (Exception e) {
			throw new SAXException(e);
		}
	}

	private DocumentBuilderFactory newDocumentBuilderFactory() {
		return DocumentBuilderFactory.newInstance();
	}

	private static void setAttributeEL(DocumentBuilderFactory factory, String name, Object value) {
		try {
			factory.setAttribute(name, value);
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			// SystemOut.printDate("attribute ["+name+"] is not allowed for
			// ["+factory.getClass().getName()+"]");
		}
	}

	@Override
	public void replaceChild(Node newChild, Node oldChild) {
		Node nc = newChild;
		Node oc = oldChild;
		Node p = oc.getParentNode();
		if (nc != oc) p.replaceChild(nc, oc);
	}

	@Override
	public boolean nameEqual(Node node, String name) {
		if (name == null) return false;
		return name.equals(node.getNodeName()) || name.equals(node.getLocalName());
	}

	@Override
	public Element getRootElement(Node node) {
		Document doc = null;
		if (node instanceof Document) doc = (Document) node;
		else doc = node.getOwnerDocument();
		return doc.getDocumentElement();
	}

	@Override
	public Document newDocument() throws ParserConfigurationException, FactoryConfigurationError {
		if (docBuilder == null) {
			docBuilder = newDocumentBuilderFactory().newDocumentBuilder();
		}
		return docBuilder.newDocument();
	}

	@Override
	public Document getDocument(NodeList nodeList) throws PageException {
		if (nodeList instanceof Document) return (Document) nodeList;
		int len = nodeList.getLength();
		for (int i = 0; i < len; i++) {
			Node node = nodeList.item(i);
			if (node != null) return node.getOwnerDocument();
		}
		throw CFMLEngineFactory.getInstance().getExceptionUtil().createXMLException("can't get Document from NodeList, in NoteList are no Nodes");
	}

	@Override
	public Document getDocument(Node node) {
		if (node instanceof Document) return (Document) node;
		return node.getOwnerDocument();
	}

	@Override
	public synchronized ArrayList<Node> getChildNodes(Node node, short type, String filter) {
		ArrayList<Node> rtn = new ArrayList<Node>();
		NodeList nodes = node.getChildNodes();
		int len = nodes.getLength();
		Node n;
		for (int i = 0; i < len; i++) {
			try {
				n = nodes.item(i);
				if (n != null && (type == UNDEFINED_NODE || n.getNodeType() == type)) {
					if (filter == null || filter.equals(n.getLocalName())) rtn.add(n);
				}
			}
			catch (Throwable t) {
				if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			}
		}
		return rtn;
	}

	public synchronized List<Node> getChildNodesAsList(Node node, short type, String filter) {
		List<Node> rtn = new ArrayList<Node>();
		NodeList nodes = node.getChildNodes();
		int len = nodes.getLength();
		Node n;
		for (int i = 0; i < len; i++) {
			try {
				n = nodes.item(i);
				if (n != null && (n.getNodeType() == type || type == UNDEFINED_NODE)) {
					if (filter == null || filter.equals(n.getLocalName())) rtn.add(n);
				}
			}
			catch (Throwable t) {
				if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			}
		}
		return rtn;
	}

	@Override
	public synchronized Node getChildNode(Node node, short type, String filter, int index) {
		NodeList nodes = node.getChildNodes();
		int len = nodes.getLength();
		Node n;
		int count = 0;
		for (int i = 0; i < len; i++) {
			try {
				n = nodes.item(i);
				if (n != null && (type == UNDEFINED_NODE || n.getNodeType() == type)) {
					if (filter == null || filter.equals(n.getLocalName())) {
						if (count == index) return n;
						count++;
					}
				}
			}
			catch (Throwable t) {
				if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			}
		}
		return null;
	}

	/**
	 * return all Children of a node by a defined type as Node Array
	 * 
	 * @param node node to get children from
	 * @param type type of returned node
	 * @return all matching child node
	 */
	public Node[] getChildNodesAsArray(Node node, short type) {
		ArrayList<Node> nodeList = getChildNodes(node, type, null);
		return nodeList.toArray(new Node[nodeList.size()]);
	}

	public Node[] getChildNodesAsArray(Node node, short type, String filter) {
		ArrayList<Node> nodeList = getChildNodes(node, type, filter);
		return nodeList.toArray(new Node[nodeList.size()]);
	}

	/**
	 * return all Element Children of a node
	 * 
	 * @param node node to get children from
	 * @return all matching child node
	 */
	public Element[] getChildElementsAsArray(Node node) {
		ArrayList<Node> nodeList = getChildNodes(node, Node.ELEMENT_NODE, null);
		return nodeList.toArray(new Element[nodeList.size()]);
	}

	@Override
	public String transform(InputSource xml, InputSource xsl, Map<String, Object> parameters) throws TransformerException, SAXException, IOException {
		return transform(parse(xml, null, false), xsl, parameters);
	}

	@Override
	public String transform(Document doc, InputSource xsl, Map<String, Object> parameters) throws TransformerException {
		StringWriter sw = new StringWriter();
		TransformerFactory factory = getTransformerFactory();
		factory.setErrorListener(SimpleErrorListener.THROW_FATAL);
		Transformer transformer = factory.newTransformer(new StreamSource(xsl.getCharacterStream()));
		if (parameters != null) {
			Iterator it = parameters.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry e = (Map.Entry) it.next();
				transformer.setParameter(e.getKey().toString(), e.getValue());
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
	public String getTypeAsString(Node node, boolean cftype) {
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

	@Override
	public synchronized Element getChildWithName(String name, Element el) {
		Element[] children = getChildElementsAsArray(el);
		for (int i = 0; i < children.length; i++) {
			if (name.equalsIgnoreCase(children[i].getNodeName())) return children[i];
		}
		return null;
	}

	@Override
	public InputSource toInputSource(Resource res, Charset cs) throws IOException {
		String str = CFMLEngineFactory.getInstance().getIOUtil().toString((res), cs);
		return new InputSource(new StringReader(str));
	}

	public InputSource toInputSource(PageContext pc, Object value) throws IOException, PageException {
		if (value instanceof InputSource) {
			return (InputSource) value;
		}
		if (value instanceof String) {
			return toInputSource(pc, (String) value);
		}
		if (value instanceof StringBuffer) {
			return toInputSource(pc, value.toString());
		}
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		IO io = engine.getIOUtil();
		if (value instanceof Resource) {
			String str = io.toString(((Resource) value), (Charset) null);
			return new InputSource(new StringReader(str));
		}
		if (value instanceof File) {
			String str = io.toString(engine.getCastUtil().toResource((value)), (Charset) null);
			return new InputSource(new StringReader(str));
		}
		if (value instanceof InputStream) {
			InputStream is = (InputStream) value;
			try {
				String str = io.toString(is, (Charset) null);
				return new InputSource(new StringReader(str));
			}
			finally {
				io.closeSilent(is);
			}
		}
		if (value instanceof Reader) {
			Reader reader = (Reader) value;
			try {
				String str = io.toString(reader);
				return new InputSource(new StringReader(str));
			}
			finally {
				io.closeSilent(reader);
			}
		}
		if (value instanceof byte[]) {
			return new InputSource(new ByteArrayInputStream((byte[]) value));
		}
		throw engine.getExceptionUtil().createXMLException("can't cast object of type [" + value + "] to an Input for xml parser");
	}

	public InputSource toInputSource(PageContext pc, String xml) throws IOException, PageException {
		return toInputSource(pc, xml, true);
	}

	public InputSource toInputSource(PageContext pc, String xml, boolean canBePath) throws IOException, PageException {
		// xml text
		xml = xml.trim();
		if (!canBePath || xml.startsWith("<")) {
			return new InputSource(new StringReader(xml));
		}
		// xml link
		if (pc == null) pc = CFMLEngineFactory.getInstance().getThreadPageContext();
		Resource res = CFMLEngineFactory.getInstance().getResourceUtil().toResourceExisting(pc, xml);
		return toInputSource(pc, res);
	}

	@Override
	public Struct validate(InputSource xml, InputSource schema, String strSchema) throws PageException {
		return new XMLValidator(schema, strSchema).validate(xml);
	}

	@Override
	public void prependChild(Element parent, Element child) {
		Node first = parent.getFirstChild();
		if (first == null) parent.appendChild(child);
		else {
			parent.insertBefore(child, first);
		}
	}

	@Override
	public void setFirst(Node parent, Node node) {
		Node first = parent.getFirstChild();
		if (first != null) parent.insertBefore(node, first);
		else parent.appendChild(node);
	}

	public XMLReader createXMLReader(String oprionalDefaultSaxParser) throws SAXException {
		try {
			return XMLReaderFactory.createXMLReader(oprionalDefaultSaxParser);
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			return XMLReaderFactory.createXMLReader();
		}
	}

	@Override
	public InputSource toInputSource(Object value) throws IOException, PageException {
		if (value instanceof InputSource) {
			return (InputSource) value;
		}
		if (value instanceof String) {
			return toInputSource(CFMLEngineFactory.getInstance().getThreadPageContext(), (String) value, true);
		}
		if (value instanceof StringBuffer) {
			return toInputSource(CFMLEngineFactory.getInstance().getThreadPageContext(), value.toString(), true);
		}
		if (value instanceof Resource) {
			IO io = CFMLEngineFactory.getInstance().getIOUtil();
			String str = io.toString(((Resource) value), (Charset) null);
			return new InputSource(new StringReader(str));
		}
		if (value instanceof File) {
			CFMLEngine e = CFMLEngineFactory.getInstance();
			String str = e.getIOUtil().toString(e.getCastUtil().toResource(value), (Charset) null);
			return new InputSource(new StringReader(str));
		}
		if (value instanceof InputStream) {
			InputStream is = (InputStream) value;
			IO io = CFMLEngineFactory.getInstance().getIOUtil();
			try {
				String str = io.toString(is, (Charset) null);
				return new InputSource(new StringReader(str));
			}
			finally {
				io.closeSilent(is);
			}
		}
		if (value instanceof Reader) {
			Reader reader = (Reader) value;
			IO io = CFMLEngineFactory.getInstance().getIOUtil();
			try {
				String str = io.toString(reader);
				return new InputSource(new StringReader(str));
			}
			finally {
				io.closeSilent(reader);
			}
		}
		if (value instanceof byte[]) {
			return new InputSource(new ByteArrayInputStream((byte[]) value));
		}
		throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("can't cast object of type [" + value + "] to an Input for xml parser");
	}

	@Override
	public void writeTo(Node node, Resource file) throws PageException {
		OutputStream os = null;
		CFMLEngine e = CFMLEngineFactory.getInstance();
		IO io = e.getIOUtil();
		try {
			os = io.toBufferedOutputStream(file.getOutputStream());
			writeTo(node, new StreamResult(os), false, false, null, null, null);
		}
		catch (IOException ioe) {
			throw e.getCastUtil().toPageException(ioe);
		}
		finally {
			e.getIOUtil().closeSilent(os);
		}
	}

	@Override
	public void writeTo(Node node, Result res, boolean omitXMLDecl, boolean indent, String publicId, String systemId, String encoding) throws PageException {
		try {
			Transformer t = getTransformerFactory().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDecl ? "yes" : "no");
			// t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");

			// optional properties
			if (!Util.isEmpty(publicId, true)) t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId);
			if (!Util.isEmpty(systemId, true)) t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
			if (!Util.isEmpty(encoding, true)) t.setOutputProperty(OutputKeys.ENCODING, encoding);

			t.transform(new DOMSource(node), res);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	@Override
	public String toString(Node node, boolean omitXMLDecl, boolean indent, String publicId, String systemId, String encoding) throws PageException {
		StringWriter sw = new StringWriter();
		try {
			writeTo(node, new StreamResult(sw), omitXMLDecl, indent, publicId, systemId, encoding);
		}
		finally {
			CFMLEngineFactory.getInstance().getIOUtil().closeSilent(sw);
		}
		return sw.getBuffer().toString();
	}

	@Override
	public String toString(NodeList nodes, boolean omitXMLDecl, boolean indent) throws PageException {
		StringWriter sw = new StringWriter();
		try {
			int len = nodes.getLength();
			for (int i = 0; i < len; i++) {
				writeTo(nodes.item(i), new StreamResult(sw), omitXMLDecl, indent, null, null, null);
			}
		}
		finally {
			Util.closeEL(sw);
		}
		return sw.getBuffer().toString();
	}

	@Override
	public String toString(Node node, String defaultValue) {
		StringWriter sw = new StringWriter();
		try {
			writeTo(node, new StreamResult(sw), false, false, null, null, null);
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			return defaultValue;
		}
		finally {
			Util.closeEL(sw);
		}
		return sw.getBuffer().toString();
	}

	@Override
	public Node toNode(Object value) throws PageException {
		if (value instanceof Node) return (Node) value;
		try {
			return parse(toInputSource(CFMLEngineFactory.getInstance().getThreadPageContext(), value), null, false);
		}
		catch (Exception outer) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(outer);
		}
	}

	@Override
	public Document createDocument(Resource res, boolean isHTML) throws PageException {
		InputStream is = null;
		try {
			return parse(new InputSource(is = res.getInputStream()), null, isHTML);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
		finally {
			Util.closeEL(is);
		}
	}

	@Override
	public Document createDocument(String xml, boolean isHTML) throws PageException {
		try {
			return parse(toInputSource(null, xml), null, isHTML);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	@Override
	public Document createDocument(InputStream is, boolean isHTML) throws PageException {
		try {
			return parse(new InputSource(is), null, isHTML);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	static class SimpleErrorListener implements ErrorListener {

		public static final ErrorListener THROW_FATAL = new SimpleErrorListener(false, true, true);
		public static final ErrorListener THROW_ERROR = new SimpleErrorListener(false, false, true);
		public static final ErrorListener THROW_WARNING = new SimpleErrorListener(false, false, false);
		private boolean ignoreFatal;
		private boolean ignoreError;
		private boolean ignoreWarning;

		public SimpleErrorListener(boolean ignoreFatal, boolean ignoreError, boolean ignoreWarning) {
			this.ignoreFatal = ignoreFatal;
			this.ignoreError = ignoreError;
			this.ignoreWarning = ignoreWarning;
		}

		@Override
		public void error(TransformerException te) throws TransformerException {
			if (!ignoreError) throw te;
		}

		@Override
		public void fatalError(TransformerException te) throws TransformerException {
			if (!ignoreFatal) throw te;
		}

		@Override
		public void warning(TransformerException te) throws TransformerException {
			if (!ignoreWarning) throw te;
		}
	}

	static class XMLEntityResolverDefaultHandler extends DefaultHandler {

		private InputSource entityRes;

		public XMLEntityResolverDefaultHandler(InputSource entityRes) {
			this.entityRes = entityRes;
		}

		@Override
		public InputSource resolveEntity(String publicID, String systemID) throws SAXException {
			// if(entityRes!=null)print.out("resolveEntity("+(entityRes!=null)+"):"+publicID+":"+systemID);

			if (entityRes != null) return entityRes;
			try {
				CFMLEngine engine = CFMLEngineFactory.getInstance();
				return new InputSource(engine.getIOUtil().toBufferedInputStream(engine.getHTTPUtil().toURL(systemID).openStream()));
			}
			catch (Throwable t) {
				if (t instanceof ThreadDeath) throw (ThreadDeath) t;
				return null;
			}
		}
	}

	static class ThrowingErrorHandler implements ErrorHandler {

		private boolean throwFatalError;
		private boolean throwError;
		private boolean throwWarning;

		public ThrowingErrorHandler(boolean throwFatalError, boolean throwError, boolean throwWarning) {
			this.throwFatalError = throwFatalError;
			this.throwError = throwError;
			this.throwWarning = throwWarning;
		}

		@Override
		public void error(SAXParseException e) throws SAXException {
			if (throwError) throw new SAXException(e);
		}

		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			if (throwFatalError) throw new SAXException(e);
		}

		@Override
		public void warning(SAXParseException e) throws SAXException {
			if (throwWarning) throw new SAXException(e);
		}
	}
}