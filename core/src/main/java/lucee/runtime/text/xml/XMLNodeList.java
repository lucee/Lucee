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
package lucee.runtime.text.xml;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.struct.XMLObject;
import lucee.runtime.text.xml.struct.XMLStruct;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.ArraySupport;
import lucee.runtime.util.ArrayIterator;

/**
 * 
 */
public final class XMLNodeList extends ArraySupport implements NodeList, XMLObject {

	private boolean caseSensitive;
	private Document doc;
	private Node parent;
	private String filter;
	private final short type;

	/**
	 * @param parent Parent Node
	 * @param caseSensitive
	 */
	public XMLNodeList(Node parent, boolean caseSensitive, short type) {
		this(parent, caseSensitive, type, null);
	}

	public XMLNodeList(Node parent, boolean caseSensitive, short type, String filter) {
		this.filter = filter;
		this.type = type;
		if (parent instanceof XMLStruct) {
			XMLStruct xmlNode = ((XMLStruct) parent);
			this.parent = xmlNode.toNode();
			this.caseSensitive = xmlNode.getCaseSensitive();
		}
		else {
			this.parent = parent;
			this.caseSensitive = caseSensitive;
		}
		this.doc = XMLUtil.getDocument(this.parent);
	}

	@Override
	public int getLength() {
		return XMLUtil.childNodesLength(parent, type, caseSensitive, filter);
	}

	@Override
	public Node item(int index) {
		return XMLCaster.toXMLStruct(getChildNode(index), caseSensitive);
	}

	@Override
	public int size() {
		return getLength();
	}

	@Override
	public Collection.Key[] keys() {
		Collection.Key[] keys = new Collection.Key[getLength()];
		for (int i = 1; i <= keys.length; i++) {
			keys[i - 1] = KeyImpl.init(i + "");
		}
		return keys;
	}

	@Override
	public int[] intKeys() {
		int[] keys = new int[getLength()];
		for (int i = 1; i <= keys.length; i++) {
			keys[i - 1] = i;
		}
		return keys;
	}

	@Override
	public Object removeEL(Collection.Key key) {
		return removeEL(Caster.toIntValue(key.getString(), -1));
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		return removeE(Caster.toIntValue(key.getString()));
	}

	@Override
	public Object removeEL(int index) {
		int len = size();
		if (index < 1 || index > len) return null;
		try {
			return XMLCaster.toXMLStruct(parent.removeChild(XMLCaster.toRawNode(item(index - 1))), caseSensitive);
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public Object removeE(int index) throws PageException {
		int len = size();
		if (index < 1 || index > len) throw new ExpressionException("can't remove value form XML Node List at index " + index + ", valid indexes goes from 1 to " + len);
		return XMLCaster.toXMLStruct(parent.removeChild(XMLCaster.toRawNode(item(index - 1))), caseSensitive);
	}

	@Override
	public void clear() {
		Node[] nodes = getChildNodesAsArray();
		for (int i = 0; i < nodes.length; i++) {
			parent.removeChild(XMLCaster.toRawNode(nodes[i]));
		}
	}

	@Override
	public Object get(String key) throws ExpressionException {
		return getE(Caster.toIntValue(key));
	}

	@Override
	public final Object get(Collection.Key key) throws ExpressionException {
		return get(key.getString());
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key) throws ExpressionException {
		return get(key.getString());
	}

	@Override
	public Object getE(int key) throws ExpressionException {
		return getE(null, key);
	}

	public Object getE(PageContext pc, int key) throws ExpressionException {
		Object rtn = item(key - 1);
		if (rtn == null) throw new ExpressionException("invalid index [" + key + "] for XML Node List , indexes goes from [0-" + size() + "]");
		return rtn;
	}

	@Override
	public Object get(String key, Object defaultValue) {
		int index = Caster.toIntValue(key, Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return defaultValue;
		return get(index, defaultValue);
	}

	@Override
	public final Object get(Collection.Key key, Object defaultValue) {
		return get(key.getString(), defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return get(key.getString(), defaultValue);
	}

	@Override
	public Object get(int key, Object defaultValue) {
		Object rtn = item(key - 1);
		if (rtn == null) return defaultValue;
		return rtn;
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		return setE(Caster.toIntValue(key), value);
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		return set(key.getString(), value);
	}

	@Override
	public Object setE(int index, Object value) throws PageException {
		// check min Index
		if (index < 1) throw new ExpressionException("invalid index [" + index + "] to set a child node, valid indexes start at 1");

		Node[] nodes = getChildNodesAsArray();

		// if index Greater len append
		if (index > nodes.length) return append(value);

		// remove all children
		clear();

		// set all children before new Element
		for (int i = 1; i < index; i++) {
			append(nodes[i - 1]);
		}

		// set new Element
		append(XMLCaster.toNode(doc, value, true));

		// set all after new Element
		for (int i = index; i < nodes.length; i++) {
			append(nodes[i]);
		}

		return value;
	}

	@Override
	public Object setEL(String key, Object value) {
		int index = Caster.toIntValue(key, Integer.MIN_VALUE);
		if (index == Integer.MIN_VALUE) return null;
		return setEL(index, value);
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		return setEL(key.getString(), value);
	}

	@Override
	public Object setEL(int index, Object value) {
		try {
			return setE(index, value);
		}
		catch (PageException e) {
			return null;
		}
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
		Object[] values = new Object[getLength()];
		for (int i = 0; i < values.length; i++) {
			values[i] = item(i);
		}
		return new ArrayIterator(values);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		maxlevel--;
		DumpTable table = new DumpTable("xml", "#cc9999", "#ffffff", "#000000");
		table.setTitle("Array (XML Node List)");
		int len = size();

		for (int i = 1; i <= len; i++) {
			table.appendRow(1, new SimpleDumpData(i), DumpUtil.toDumpData(item(i - 1), pageContext, maxlevel, dp));
		}
		return table;
	}

	@Override
	public Object append(Object o) throws PageException {
		return parent.appendChild(XMLCaster.toNode(doc, o, true));
	}

	@Override
	public Object appendEL(Object o) {
		try {
			return append(o);
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public Object clone() {
		return duplicate(true);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLNodeList(parent.cloneNode(deepCopy), caseSensitive, type);
	}

	@Override
	public int getDimension() {
		return 1;
	}

	@Override
	public boolean insert(int index, Object value) throws PageException {
		// check min Index
		if (index < 1) throw new ExpressionException("invalid index [" + index + "] to insert a child node, valid indexes start at 1");

		Node[] nodes = getChildNodesAsArray();

		// if index Greater len append
		if (index > nodes.length) {
			append(value);
			return true;
		}

		// remove all children
		clear();

		// set all children before new Element
		for (int i = 1; i < index; i++) {
			append(nodes[i - 1]);
		}

		// set new Element
		append(XMLCaster.toNode(doc, value, true));

		// set all after new Element
		for (int i = index; i <= nodes.length; i++) {
			append(nodes[i - 1]);
		}

		return true;
	}

	@Override
	public Object prepend(Object o) throws PageException {

		Node[] nodes = getChildNodesAsArray();

		// remove all children
		clear();

		// set new Element
		append(XMLCaster.toNode(doc, o, true));

		// set all after new Element
		for (int i = 0; i < nodes.length; i++) {
			append(nodes[i]);
		}
		return o;
	}

	@Override
	public void resize(int to) throws ExpressionException {
		if (to > size()) throw new ExpressionException("can't resize a XML Node List Array with empty Elements");
	}

	@Override
	public void sort(String sortType, String sortOrder) throws ExpressionException {
		throw new ExpressionException("can't sort a XML Node List Array", "sorttype:" + sortType + ";sortorder:" + sortOrder);
	}

	@Override
	public void sortIt(Comparator comp) {
		throw new PageRuntimeException("can't sort a XML Node List Array");
	}

	@Override
	public Object[] toArray() {
		return getChildNodesAsArray();
	}

	public ArrayList toArrayList() {
		Object[] arr = toArray();
		ArrayList list = new ArrayList();
		for (int i = 0; i > arr.length; i++) {
			list.add(arr[i]);
		}
		return list;
	}

	/**
	 * @return returns an output from the content as plain Text
	 */
	public String toPlain() {
		StringBuffer sb = new StringBuffer();
		int length = size();
		for (int i = 1; i <= length; i++) {
			sb.append(i);
			sb.append(": ");
			sb.append(get(i, null));
			sb.append("\n");
		}
		return sb.toString();
	}

	private Node getChildNode(int index) {
		return XMLUtil.getChildNode(parent, type, caseSensitive, filter, index);
	}

	private Node[] getChildNodesAsArray() {
		return XMLUtil.getChildNodesAsArray(parent, type, caseSensitive, filter);
	}

	@Override
	public boolean containsKey(String key) {
		return get(key, null) != null;
	}

	@Override
	public boolean containsKey(Collection.Key key) {
		return get(key, null) != null;
	}

	@Override
	public boolean containsKey(int key) {
		return get(key, null) != null;
	}

	@Override
	public boolean getCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public String castToString() throws ExpressionException {
		throw new ExpressionException("Can't cast XML NodeList to String");
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		throw new ExpressionException("Can't cast XML NodeList to a boolean value");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws ExpressionException {
		throw new ExpressionException("Can't cast XML NodeList to a number value");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws ExpressionException {
		throw new ExpressionException("Can't cast XML NodeList to a Date");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public int compareTo(boolean b) throws ExpressionException {
		throw new ExpressionException("can't compare XML NodeList with a boolean value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare XML NodeList with a DateTime Object");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare XML NodeList with a numeric value");
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare XML NodeList with a String");
	}
}