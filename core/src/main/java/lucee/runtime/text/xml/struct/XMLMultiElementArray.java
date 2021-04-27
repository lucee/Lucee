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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.util.ArraySupport;
import lucee.runtime.type.wrap.ArrayAsArrayList;

public class XMLMultiElementArray extends ArraySupport {

	private static final long serialVersionUID = -2673749147723742450L;
	private XMLMultiElementStruct struct;

	public XMLMultiElementArray(XMLMultiElementStruct struct) {
		this.struct = struct;
	}

	@Override
	public Object append(Object o) throws PageException {
		return setE(size() + 1, o);
	}

	@Override
	public Object appendEL(Object o) {
		return setEL(size() + 1, o);
	}

	@Override
	public boolean containsKey(int key) {
		return get(key, null) != null;
	}

	@Override
	public Object get(int key, Object defaultValue) {
		return struct.get(key, defaultValue);
	}

	@Override
	public Object getE(int key) throws PageException {
		return struct.get(key);
	}

	@Override
	public int getDimension() {
		return struct.getInnerArray().getDimension();
	}

	@Override
	public boolean insert(int index, Object value) throws PageException {
		Element element = XMLCaster.toElement(struct.getOwnerDocument(), value);
		boolean rtn = struct.getInnerArray().insert(index, element);
		Object obj = struct.getInnerArray().get(index, null);

		if (obj instanceof Element) {
			Element el = ((Element) obj);
			el.getParentNode().insertBefore(XMLCaster.toRawNode(element), el);
		}
		else {
			struct.getParentNode().appendChild(XMLCaster.toRawNode(element));
		}
		return rtn;
	}

	@Override
	public int[] intKeys() {
		return struct.getInnerArray().intKeys();
	}

	@Override
	public Object prepend(Object value) throws PageException {
		Element element = XMLCaster.toElement(struct.getOwnerDocument(), value);
		Object obj = struct.getInnerArray().get(1, null);

		if (obj instanceof Element) {
			Element el = ((Element) obj);
			el.getParentNode().insertBefore(XMLCaster.toRawNode(element), el);
		}
		else {
			struct.getParentNode().appendChild(XMLCaster.toRawNode(element));
		}
		return struct.getInnerArray().prepend(element);
	}

	@Override
	public Object removeE(int key) throws PageException {
		return struct.remove(key);
	}

	@Override
	public Object removeEL(int key) {
		return struct.removeEL(key);
	}

	@Override
	public Object pop() throws PageException {
		return removeE(size());
	}

	@Override
	public Object pop(Object defaultValue) {
		try {
			return removeE(size());
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public Object shift() throws PageException {
		return removeE(1);
	}

	@Override
	public Object shift(Object defaultValue) {
		try {
			return removeE(1);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public void resize(int to) throws PageException {
		throw new PageRuntimeException("resizing of xml nodelist not allowed");
	}

	@Override
	public Object setE(int key, Object value) throws PageException {
		return struct.set(key, value);
	}

	@Override
	public Object setEL(int key, Object value) {
		return struct.setEL(key, value);
	}

	@Override
	public void sort(String sortType, String sortOrder) throws PageException {
		if (size() <= 1) return;

		struct.getInnerArray().sort(sortType, sortOrder);

		Object[] nodes = struct.getInnerArray().toArray();
		Node last = (Node) nodes[nodes.length - 1], current;
		Node parent = last.getParentNode();
		for (int i = nodes.length - 2; i >= 0; i--) {
			current = (Node) nodes[i];
			parent.insertBefore(current, last);
			last = current;
		} // MUST testen
	}

	@Override
	public void sortIt(Comparator comp) {
		if (size() <= 1) return;

		struct.getInnerArray().sortIt(comp);

		Object[] nodes = struct.getInnerArray().toArray();
		Node last = (Node) nodes[nodes.length - 1], current;
		Node parent = last.getParentNode();
		for (int i = nodes.length - 2; i >= 0; i--) {
			current = (Node) nodes[i];
			parent.insertBefore(current, last);
			last = current;
		} // MUST testen
	}

	@Override
	public Object[] toArray() {
		return struct.getInnerArray().toArray();
	}

	public ArrayList toArrayList() {
		return ArrayAsArrayList.toArrayList(this);
	}

	@Override
	public void clear() {// MUST
	}

	@Override
	public boolean containsKey(String key) {
		return struct.containsKey(key);
	}

	@Override
	public boolean containsKey(Key key) {
		return struct.containsKey(key);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLMultiElementArray((XMLMultiElementStruct) Duplicator.duplicate(struct, deepCopy));
	}

	@Override
	public Object get(String key) throws PageException {
		return struct.get(key);
	}

	@Override
	public final Object get(Key key) throws PageException {
		return struct.get(key);
	}

	@Override
	public final Object get(PageContext pc, Key key) throws PageException {
		return struct.get(key);
	}

	@Override
	public Object get(String key, Object defaultValue) {
		return struct.get(key, defaultValue);
	}

	@Override
	public final Object get(Key key, Object defaultValue) {
		return struct.get(key, defaultValue);
	}

	@Override
	public final Object get(PageContext pc, Key key, Object defaultValue) {
		return struct.get(pc, key, defaultValue);
	}

	@Override
	public Key[] keys() {
		return struct.getInnerArray().keys();
	}

	@Override
	public Object remove(Key key) throws PageException {
		return struct.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		return struct.removeEL(key);
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		return struct.set(key, value);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return struct.set(key, value);
	}

	@Override
	public Object setEL(String key, Object value) {
		return struct.setEL(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return struct.setEL(key, value);
	}

	@Override
	public int size() {
		return struct.getInnerArray().size();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return struct.toDumpData(pageContext, maxlevel, dp);
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
	public boolean castToBooleanValue() throws PageException {
		return struct.castToBooleanValue();
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return struct.castToBoolean(defaultValue);
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return struct.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return struct.castToDateTime(defaultValue);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return struct.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return struct.castToDoubleValue(defaultValue);
	}

	@Override
	public String castToString() throws PageException {
		return struct.castToString();
	}

	@Override
	public String castToString(String defaultValue) {
		return struct.castToString(defaultValue);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return struct.compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return struct.compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return struct.compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return struct.compareTo(dt);
	}

	@Override
	public Object clone() {
		return duplicate(true);
	}

	@Override
	public boolean add(Object o) {

		return false;
	}
}