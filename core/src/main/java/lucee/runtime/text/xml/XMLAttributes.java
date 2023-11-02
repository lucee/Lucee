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

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.XMLException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.StructSupport;

/**
 * represent a Struct and a NamedNodeMap
 */
public final class XMLAttributes extends StructSupport implements Struct, NamedNodeMap {

	private final NamedNodeMap nodeMap;
	private final Document owner;
	private final Node parent;
	private final boolean caseSensitive;

	/**
	 * constructor of the class (readonly)
	 * 
	 * @param nodeMap
	 */
	public XMLAttributes(Node parent, boolean caseSensitive) {
		this.owner = XMLUtil.getDocument(parent);
		this.parent = parent;
		this.nodeMap = parent.getAttributes();
		this.caseSensitive = caseSensitive;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		Collection.Key[] keys = keys();
		maxlevel--;
		DumpTable table = new DumpTable("xml", "#999966", "#cccc99", "#000000");
		table.setTitle("Struct (XML Attributes)");

		int maxkeys = dp.getMaxKeys();
		int index = 0;
		Collection.Key k;
		for (int i = 0; i < keys.length; i++) {
			k = keys[i];

			if (DumpUtil.keyValid(dp, maxlevel, k)) {
				if (maxkeys <= index++) break;
				table.appendRow(1, new SimpleDumpData(k.getString()), DumpUtil.toDumpData(get(k.getString(), null), pageContext, maxlevel, dp));
			}
		}
		return table;
	}

	@Override
	public int size() {
		return nodeMap.getLength();
	}

	@Override
	public Collection.Key[] keys() {
		int len = nodeMap.getLength();
		ArrayList<Collection.Key> list = new ArrayList<Collection.Key>();
		for (int i = 0; i < len; i++) {
			Node item = nodeMap.item(i);
			if (item instanceof Attr) list.add(KeyImpl.init(((Attr) item).getName()));
		}
		return list.toArray(new Collection.Key[list.size()]);
	}

	@Override
	public Object remove(Collection.Key k) throws PageException {
		String key = k.getString();
		Node rtn = null;
		if (!caseSensitive) {
			int len = nodeMap.getLength();
			String nn;
			for (int i = len - 1; i >= 0; i--) {
				nn = nodeMap.item(i).getNodeName();
				if (key.equalsIgnoreCase(nn)) rtn = nodeMap.removeNamedItem(nn);
			}
		}
		else rtn = nodeMap.removeNamedItem(toName(key));

		if (rtn != null) return rtn.getNodeValue();
		throw new ExpressionException("can't remove element with name [" + key + "], element doesn't exist");
	}

	@Override
	public Object removeEL(Collection.Key k) {
		String key = k.getString();
		Node rtn = null;
		if (!caseSensitive) {
			int len = nodeMap.getLength();
			String nn;
			for (int i = len - 1; i >= 0; i--) {
				nn = nodeMap.item(i).getNodeName();
				if (key.equalsIgnoreCase(nn)) rtn = nodeMap.removeNamedItem(nn);
			}
		}
		else rtn = nodeMap.removeNamedItem(toName(key));

		if (rtn != null) return rtn.getNodeValue();
		return null;
	}

	@Override
	public void clear() {
		Collection.Key[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			nodeMap.removeNamedItem(keys[i].getString());
		}
	}

	@Override
	public final Object get(Collection.Key key) throws ExpressionException {
		return get((PageContext) null, key);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key) throws ExpressionException {
		Node rtn = nodeMap.getNamedItem(key.getString());
		if (rtn != null) return rtn.getNodeValue();

		Collection.Key[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			if (key.equalsIgnoreCase(keys[i])) return nodeMap.getNamedItem(keys[i].getString()).getNodeValue();
		}
		throw new ExpressionException("No Attribute " + key.getString() + " defined for tag", "attributes are [" + ListUtil.arrayToList(keys, ", ") + "]");
	}

	@Override
	public final Object get(Collection.Key key, Object defaultValue) {
		return get((PageContext) null, key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		Node rtn = nodeMap.getNamedItem(key.getString());
		if (rtn != null) return rtn.getNodeValue();

		Collection.Key[] keys = keys();
		for (int i = 0; i < keys.length; i++) {
			if (key.equalsIgnoreCase(keys[i])) return nodeMap.getNamedItem(keys[i].getString()).getNodeValue();
		}
		return defaultValue;
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		if (owner == null) return value;

		try {
			Attr attr = owner.createAttribute(toName(key.getString()));
			attr.setValue(Caster.toString(value));
			nodeMap.setNamedItem(attr);

		}
		catch (DOMException de) {
			throw new XMLException(de);
		}

		return value;
	}

	private String toName(String name) {
		return toName(name, name);
	}

	private String toName(String name, String defaultValue) {
		if (caseSensitive) return name;

		Node n = nodeMap.getNamedItem(name);
		if (n != null) return n.getNodeName();

		int len = nodeMap.getLength();
		String nn;
		for (int i = 0; i < len; i++) {
			nn = nodeMap.item(i).getNodeName();
			if (name.equalsIgnoreCase(nn)) return nn;
		}
		return defaultValue;
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		if (owner == null) return value;
		try {
			Attr attr = owner.createAttribute(toName(key.getString()));
			attr.setValue(Caster.toString(value));
			nodeMap.setNamedItem(attr);
		}
		catch (Exception e) {
			return null;
		}
		return value;
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
	public int getLength() {
		return nodeMap.getLength();
	}

	@Override
	public Node item(int index) {
		return nodeMap.item(index);
	}

	@Override
	public Node getNamedItem(String name) {
		return nodeMap.getNamedItem(name);
	}

	@Override
	public Node removeNamedItem(String name) throws DOMException {
		return nodeMap.removeNamedItem(name);
	}

	@Override
	public Node setNamedItem(Node arg) throws DOMException {
		return nodeMap.setNamedItem(arg);
	}

	@Override
	public Node setNamedItemNS(Node arg) throws DOMException {
		return nodeMap.setNamedItemNS(arg);
	}

	@Override
	public Node getNamedItemNS(String namespaceURI, String localName) {
		return nodeMap.getNamedItemNS(namespaceURI, localName);
	}

	@Override
	public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
		return nodeMap.removeNamedItemNS(namespaceURI, localName);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLAttributes(parent.cloneNode(deepCopy), caseSensitive);
	}

	/**
	 * @return returns named Node map
	 */
	public NamedNodeMap toNamedNodeMap() {
		return nodeMap;
	}

	@Override
	public final boolean containsKey(Collection.Key key) {
		return get(key, null) != null;
	}

	@Override
	public final boolean containsKey(PageContext pc, Collection.Key key) {
		return get(pc, key, null) != null;
	}

	@Override
	public String castToString() throws ExpressionException {
		throw new ExpressionException("Can't cast XML NamedNodeMap to String");
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		throw new ExpressionException("Can't cast XML NamedNodeMap to a boolean value");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws ExpressionException {
		throw new ExpressionException("Can't cast XML NamedNodeMap to a number value");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws ExpressionException {
		throw new ExpressionException("Can't cast XML NamedNodeMap to a date value");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public int compareTo(boolean b) throws ExpressionException {
		throw new ExpressionException("can't compare XML NamedNodeMap with a boolean value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare XML NamedNodeMap with a DateTime Object");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare XML NamedNodeMap with a numeric value");
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare XML NamedNodeMap with a String");
	}

	@Override
	public int getType() {
		return Struct.TYPE_LINKED;
	}
}