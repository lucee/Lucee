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
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.SAXException;

import lucee.commons.collection.MapFactory;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.XMLException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Operator;
import lucee.runtime.text.xml.XMLAttributes;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLNodeList;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.StructSupport;

/**
 * 
 */
public class XMLNodeStruct extends StructSupport implements XMLStruct {

	private Node node;
	protected boolean caseSensitive;

	/**
	 * constructor of the class
	 * 
	 * @param node Node
	 * @param caseSensitive
	 */
	protected XMLNodeStruct(Node node, boolean caseSensitive) {
		if (node instanceof XMLStruct) node = ((XMLStruct) node).toNode();
		this.node = node;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public Object remove(Key key) throws PageException {
		Object o = XMLUtil.removeProperty(node, key, caseSensitive);
		if (o != null) return o;
		throw new ExpressionException("node has no child with name [" + key + "]");
	}

	@Override
	public Object removeEL(Key key) {
		return XMLUtil.removeProperty(node, key, caseSensitive);
	}

	@Override
	public Object get(Collection.Key key) throws PageException {
		return get((PageContext) null, key);
	}

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		try {
			return XMLUtil.getProperty(node, key, caseSensitive);
		}
		catch (SAXException e) {
			throw new XMLException(e);
		}
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		return XMLUtil.setProperty(node, key, value, caseSensitive);
	}

	/**
	 * @return retun the inner map
	 */
	public Map<String, Node> getMap() {
		NodeList elements = XMLUtil.getChildNodes(node, Node.ELEMENT_NODE, false, null);// TODO ist das false hier ok?
		Map<String, Node> map = MapFactory.<String, Node>getConcurrentMap();
		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			Node node = elements.item(i);
			map.put(node.getNodeName(), node);
		}
		return map;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLNodeStruct(node.cloneNode(deepCopy), caseSensitive);
	}

	@Override
	public Node cloneNode(boolean deep) {
		return new XMLNodeStruct(node.cloneNode(deep), caseSensitive);
	}

	@Override
	public short getNodeType() {
		return node.getNodeType();
	}

	@Override
	public void normalize() {
		node.normalize();
	}

	@Override
	public boolean hasAttributes() {
		return node.hasAttributes();
	}

	@Override
	public boolean hasChildNodes() {
		return node.hasChildNodes();
	}

	@Override
	public String getLocalName() {
		return node.getLocalName();
	}

	@Override
	public String getNamespaceURI() {
		return node.getNamespaceURI();
	}

	@Override
	public String getNodeName() {
		return node.getNodeName();
	}

	@Override
	public String getNodeValue() throws DOMException {
		return node.getNodeValue();
	}

	@Override
	public String getPrefix() {
		return node.getPrefix();
	}

	@Override
	public void setNodeValue(String nodeValue) throws DOMException {
		node.setNodeValue(nodeValue);
	}

	@Override
	public void setPrefix(String prefix) throws DOMException {
		node.setPrefix(prefix);
	}

	@Override
	public Document getOwnerDocument() {
		if (node instanceof Document) return (Document) node;
		return node.getOwnerDocument();
	}

	@Override
	public NamedNodeMap getAttributes() {
		return new XMLAttributes(node, caseSensitive);
	}

	@Override
	public Node getFirstChild() {
		return node.getFirstChild();
	}

	@Override
	public Node getLastChild() {
		return node.getLastChild();
	}

	@Override
	public Node getNextSibling() {
		return node.getNextSibling();
	}

	@Override
	public Node getParentNode() {
		return node.getParentNode();
	}

	@Override
	public Node getPreviousSibling() {
		return node.getPreviousSibling();
	}

	@Override
	public NodeList getChildNodes() {
		return node.getChildNodes();
	}

	@Override
	public boolean isSupported(String feature, String version) {
		return node.isSupported(feature, version);
	}

	@Override
	public Node appendChild(Node newChild) throws DOMException {
		return node.appendChild(newChild);
	}

	@Override
	public Node removeChild(Node oldChild) throws DOMException {
		return node.removeChild(XMLCaster.toRawNode(oldChild));
	}

	@Override
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		return node.insertBefore(newChild, refChild);
	}

	@Override
	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		return node.replaceChild(XMLCaster.toRawNode(newChild), XMLCaster.toRawNode(oldChild));
	}

	@Override
	public int size() {
		NodeList list = node.getChildNodes();
		int len = list.getLength();
		int count = 0;
		for (int i = 0; i < len; i++) {
			if (list.item(i) instanceof Element) count++;
		}
		return count;
	}

	@Override
	public Collection.Key[] keys() {
		NodeList elements = XMLUtil.getChildNodes(node, Node.ELEMENT_NODE, false, null);// TODO ist das false hie ok
		Collection.Key[] arr = new Collection.Key[elements.getLength()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = KeyImpl.init(elements.item(i).getNodeName());
		}
		return arr;
	}

	@Override
	public void clear() {
		/*
		 * NodeList elements=XMLUtil.getChildNodes(node,Node.ELEMENT_NODE); int len=elements.getLength();
		 * for(int i=0;i<len;i++) { node.removeChild(elements.item(i)); }
		 */
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		return XMLUtil.getProperty(node, key, caseSensitive, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return XMLUtil.getProperty(node, key, caseSensitive, defaultValue);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return XMLUtil.setProperty(node, key, value, caseSensitive, null);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return new KeyIterator(keys());
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return new StringIterator(keys());
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new ValueIterator(this, keys());
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return DumpUtil.toDumpData(node, pageContext, maxlevel, dp);
	}

	@Override
	public final Node toNode() {
		return node;
	}

	/**
	 * @return Returns the caseSensitive.
	 */
	@Override
	public boolean getCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean containsKey(Collection.Key key) {
		return get(key, null) != null;
	}

	@Override
	public boolean containsKey(PageContext pc, Collection.Key key) {
		return get(key, null) != null;
	}

	@Override
	public XMLNodeList getXMLNodeList() {
		return new XMLNodeList(node, getCaseSensitive(), Node.ELEMENT_NODE);
	}

	@Override
	public String castToString() throws PageException {
		return XMLCaster.toString(this.node);
	}

	@Override
	public String castToString(String defaultValue) {
		return XMLCaster.toString(this.node, defaultValue);
	}

	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		throw new ExpressionException("Can't cast XML Node to a boolean value");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws ExpressionException {
		throw new ExpressionException("Can't cast XML Node to a number value");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws ExpressionException {
		throw new ExpressionException("Can't cast XML Node to a Date");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return Operator.compare(castToString(), b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare(castToString(), (Date) dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(castToString(), d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(castToString(), str);
	}

	// used only with java 7, do not set @Override
	@Override
	public String getBaseURI() {
		// not supported
		return null;
	}

	// used only with java 7, do not set @Override
	@Override
	public short compareDocumentPosition(Node other) throws DOMException {
		// not supported
		return -1;
	}

	// used only with java 7, do not set @Override
	@Override
	public void setTextContent(String textContent) throws DOMException {
		// TODO not supported
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "this method is not supported");
	}

	// used only with java 7, do not set @Override
	@Override
	public boolean isSameNode(Node other) {
		return this == other;
	}

	// used only with java 7, do not set @Override
	@Override
	public String lookupPrefix(String namespaceURI) {
		// TODO not supported
		return null;
	}

	// used only with java 7, do not set @Override
	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
		// TODO not supported
		return false;
	}

	// used only with java 7, do not set @Override
	@Override
	public String lookupNamespaceURI(String prefix) {
		// TODO not supported
		return null;
	}

	// used only with java 7, do not set @Override
	@Override
	public boolean isEqualNode(Node node) {
		// TODO not supported
		return this == node;
	}

	// used only with java 7, do not set @Override
	@Override
	public Object getFeature(String feature, String version) {
		// TODO not supported
		return null;
	}

	// used only with java 7, do not set @Override
	@Override
	public Object getUserData(String key) {
		// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = node.getClass().getMethod("getUserData", new Class[] { key.getClass() });
			return m.invoke(node, new Object[] { key });
		}
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public String getTextContent() throws DOMException {
		// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = node.getClass().getMethod("getTextContent", new Class[] {});
			return Caster.toString(m.invoke(node, ArrayUtil.OBJECT_EMPTY));
		}
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = node.getClass().getMethod("setUserData", new Class[] { key.getClass(), data.getClass(), handler.getClass() });
			return m.invoke(node, new Object[] { key, data, handler });
		}
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	@Override
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof XMLNodeStruct)) return super.equals(obj);
		XMLNodeStruct other = ((XMLNodeStruct) obj);
		return other.caseSensitive = caseSensitive && other.node.equals(node);
	}

	@Override
	public int getType() {
		return Struct.TYPE_LINKED;
	}
}