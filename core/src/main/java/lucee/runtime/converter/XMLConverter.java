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
package lucee.runtime.converter;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.lang.NumberUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;

/**
 * class to serialize and desirilize WDDX Packes
 */
public final class XMLConverter extends ConverterSupport {
	private static final Collection.Key REMOTING_FETCH = KeyImpl.intern("remotingFetch");

	private int deep = 1;
	private char del = '"';
	private TimeZone timeZone;
	private boolean ignoreRemotingFetch = true;
	// private PageContext pcx;

	private String type;

	private int id = 0;

	/**
	 * constructor of the class
	 * 
	 * @param timeZone
	 * @param xmlConform define if generated xml conform output or wddx conform output (wddx is not xml
	 *            conform)
	 */
	public XMLConverter(TimeZone timeZone, boolean ignoreRemotingFetch) {
		this.timeZone = timeZone;
		this.ignoreRemotingFetch = ignoreRemotingFetch;
	}

	/**
	 * defines timezone info will
	 * 
	 * @param timeZone
	 */
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * serialize a Date
	 * 
	 * @param date Date to serialize
	 * @return serialized date
	 * @throws ConverterException
	 */
	private String _serializeDate(Date date) {
		return _serializeDateTime(new DateTimeImpl(date));
	}

	/**
	 * serialize a DateTime
	 * 
	 * @param dateTime DateTime to serialize
	 * @return serialized dateTime
	 * @throws ConverterException
	 */
	private String _serializeDateTime(DateTime dateTime) {
		/*
		 * ACF FORMAT String strDate = new
		 * lucee.runtime.format.DateFormat(Locale.US).format(dateTime,"mmmm, dd yyyy"); String strTime = new
		 * lucee.runtime.format.TimeFormat(Locale.US).format(dateTime,"HH:mm:ss"); return
		 * goIn()+strDate+" "+strTime;
		 */
		return goIn() + JSONDateFormat.format(dateTime, null, JSONDateFormat.PATTERN_CF);
	}

	/**
	 * serialize an Array
	 * 
	 * @param array Array to serialize
	 * @param done
	 * @return serialized array
	 * @throws ConverterException
	 */
	private String _serializeArray(Array array, Map<Object, String> done, String id) throws ConverterException {
		return _serializeList(array.toList(), done, id);
	}

	/**
	 * serialize a List (as Array)
	 * 
	 * @param list List to serialize
	 * @param done
	 * @return serialized list
	 * @throws ConverterException
	 */
	private String _serializeList(List list, Map<Object, String> done, String id) throws ConverterException {
		// <ARRAY ID="1" SIZE="1"><ITEM INDEX="1" TYPE="STRING">hello world</ITEM></ARRAY>
		StringBuilder sb = new StringBuilder(goIn() + "<ARRAY ID=\"" + id + "\" SIZE=" + del + list.size() + del + ">");
		int index;
		ListIterator it = list.listIterator();
		while (it.hasNext()) {
			// <ITEM INDEX="1" TYPE="STRING">hello world</ITEM>
			index = it.nextIndex();
			String value = _serialize(it.next(), done);
			sb.append(goIn() + "<ITEM INDEX=\"" + (index + 1) + "\" TYPE=\"" + type + "\">");
			sb.append(value);
			sb.append(goIn() + "</ITEM>");
		}

		sb.append(goIn() + "</ARRAY>");
		type = "ARRAY";
		return sb.toString();
	}

	/**
	 * serialize a Component
	 * 
	 * @param component Component to serialize
	 * @param done
	 * @return serialized component
	 * @throws ConverterException
	 */
	private String _serializeComponent(Component component, Map<Object, String> done) throws ConverterException {
		StringBuilder sb = new StringBuilder();
		Component ca;
		component = new ComponentSpecificAccess(Component.ACCESS_PRIVATE, ca = component);
		boolean isPeristent = ca.isPersistent();

		deep++;
		Object member;
		Iterator<Key> it = component.keyIterator();
		Collection.Key key;
		while (it.hasNext()) {
			key = it.next();
			member = component.get(key, null);
			if (member instanceof UDF) continue;
			sb.append(goIn() + "<var scope=\"this\" name=" + del + key.toString() + del + ">");
			sb.append(_serialize(member, done));
			sb.append(goIn() + "</var>");
		}

		Property p;
		Boolean remotingFetch;
		Struct props = ignoreRemotingFetch ? null : ComponentUtil.getPropertiesAsStruct(ca, false);
		ComponentScope scope = ca.getComponentScope();
		it = scope.keyIterator();
		while (it.hasNext()) {
			key = Caster.toKey(it.next(), null);
			if (!ignoreRemotingFetch) {
				p = (Property) props.get(key, null);
				if (p != null) {
					remotingFetch = Caster.toBoolean(p.getDynamicAttributes().get(REMOTING_FETCH, null), null);
					if (remotingFetch == null) {
						if (isPeristent && ORMUtil.isRelated(p)) continue;
					}
					else if (!remotingFetch.booleanValue()) continue;
				}
			}

			member = scope.get(key, null);
			if (member instanceof UDF || key.equals(KeyConstants._this)) continue;
			sb.append(goIn() + "<var scope=\"variables\" name=" + del + key.toString() + del + ">");
			sb.append(_serialize(member, done));
			sb.append(goIn() + "</var>");
		}

		deep--;
		try {
			// return goIn()+"<struct>"+sb+"</struct>";
			return goIn() + "<component md5=\"" + ComponentUtil.md5(component) + "\" name=\"" + component.getAbsName() + "\">" + sb + "</component>";
		}
		catch (Exception e) {
			throw toConverterException(e);
		}
	}

	/**
	 * serialize a Struct
	 * 
	 * @param struct Struct to serialize
	 * @param done
	 * @return serialized struct
	 * @throws ConverterException
	 */
	private String _serializeStruct(Struct struct, Map<Object, String> done, String id) throws ConverterException {
		StringBuilder sb = new StringBuilder(goIn() + "<STRUCT ID=\"" + id + "\">");

		Iterator<Key> it = struct.keyIterator();

		deep++;
		while (it.hasNext()) {
			Key key = it.next();
			// <ENTRY NAME="STRING" TYPE="STRING">hello</ENTRY>
			String value = _serialize(struct.get(key, null), done);
			sb.append(goIn() + "<ENTRY NAME=\"" + key.toString() + "\" TYPE=\"" + type + "\">");
			sb.append(value);
			sb.append(goIn() + "</ENTRY>");
		}
		deep--;

		sb.append(goIn() + "</STRUCT>");
		type = "STRUCT";
		return sb.toString();
	}

	/**
	 * serialize a Map (as Struct)
	 * 
	 * @param map Map to serialize
	 * @param done
	 * @return serialized map
	 * @throws ConverterException
	 */
	private String _serializeMap(Map map, Map<Object, String> done) throws ConverterException {
		StringBuilder sb = new StringBuilder(goIn() + "<struct>");

		Iterator it = map.keySet().iterator();

		deep++;
		while (it.hasNext()) {
			Object key = it.next();
			sb.append(goIn() + "<var name=" + del + key.toString() + del + ">");
			sb.append(_serialize(map.get(key), done));
			sb.append(goIn() + "</var>");
		}
		deep--;

		sb.append(goIn() + "</struct>");
		return sb.toString();
	}

	/**
	 * serialize a Query
	 * 
	 * @param query Query to serialize
	 * @param done
	 * @return serialized query
	 * @throws ConverterException
	 */
	private String _serializeQuery(Query query, Map<Object, String> done, String id) throws ConverterException {

		/*
		 * <QUERY ID="1"> <COLUMNNAMES> <COLUMN NAME="a"></COLUMN> <COLUMN NAME="b"></COLUMN> </COLUMNNAMES>
		 * 
		 * <ROWS> <ROW> <COLUMN TYPE="STRING">a1</COLUMN> <COLUMN TYPE="STRING">b1</COLUMN> </ROW> <ROW>
		 * <COLUMN TYPE="STRING">a2</COLUMN> <COLUMN TYPE="STRING">b2</COLUMN> </ROW> </ROWS> </QUERY>
		 */
		Collection.Key[] keys = CollectionUtil.keys(query);
		StringBuilder sb = new StringBuilder(goIn() + "<QUERY ID=\"" + id + "\">");

		// columns
		sb.append(goIn() + "<COLUMNNAMES>");
		for (int i = 0; i < keys.length; i++) {
			sb.append(goIn() + "<COLUMN NAME=\"" + keys[i].getString() + "\"></COLUMN>");
		}
		sb.append(goIn() + "</COLUMNNAMES>");

		String value;
		deep++;
		sb.append(goIn() + "<ROWS>");
		int len = query.getRecordcount();
		for (int row = 1; row <= len; row++) {
			sb.append(goIn() + "<ROW>");
			for (int col = 0; col < keys.length; col++) {
				try {
					value = _serialize(query.getAt(keys[col], row), done);
				}
				catch (PageException e) {
					value = _serialize(e.getMessage(), done);
				}
				sb.append("<COLUMN TYPE=\"" + type + "\">" + value + "</COLUMN>");
			}
			sb.append(goIn() + "</ROW>");

		}
		sb.append(goIn() + "</ROWS>");
		deep--;

		sb.append(goIn() + "</QUERY>");
		type = "QUERY";
		return sb.toString();
	}

	/**
	 * serialize an Object to it's xml Format represenation
	 * 
	 * @param object Object to serialize
	 * @param done
	 * @return serialized Object
	 * @throws ConverterException
	 */
	private String _serialize(Object object, Map<Object, String> done) throws ConverterException {

		type = "OBJECT";

		String rtn;
		deep++;
		// NULL
		if (object == null) {
			rtn = goIn() + "";
			deep--;
			type = "NULL";
			return rtn;
		}
		// String
		if (object instanceof String) {
			rtn = goIn() + XMLUtil.escapeXMLString(object.toString());
			deep--;
			type = "STRING";
			return rtn;
		}
		// Number
		if (object instanceof Number) {
			rtn = goIn() + ((Number) object).doubleValue();
			deep--;
			type = "NUMBER";
			return rtn;
		}
		// Boolean
		if (object instanceof Boolean) {
			rtn = goIn() + ((Boolean) object).booleanValue();
			deep--;
			type = "BOOLEAN";
			return rtn;
		}
		// DateTime
		if (object instanceof DateTime) {
			rtn = _serializeDateTime((DateTime) object);
			deep--;
			type = "DATE";
			return rtn;
		}
		// Date
		if (object instanceof Date) {
			rtn = _serializeDate((Date) object);
			deep--;
			type = "DATE";
			return rtn;
		}

		Object raw = LazyConverter.toRaw(object);
		String strId = done.get(raw);
		if (strId != null) {
			rtn = goIn() + "<REF id=\"" + strId + "\"\\>";
			deep--;
			type = "NULL";
			return rtn;
		}
		strId = Caster.toString(++this.id);
		done.put(raw, strId);
		try {
			// Component
			if (object instanceof Component) {
				rtn = _serializeComponent((Component) object, done);
				deep--;
				return rtn;
			}
			// Struct
			if (object instanceof Struct) {
				rtn = _serializeStruct((Struct) object, done, strId);
				deep--;
				return rtn;
			}
			// Map
			if (object instanceof Map) {
				rtn = _serializeMap((Map) object, done);
				deep--;
				return rtn;
			}
			// Array
			if (object instanceof Array) {
				rtn = _serializeArray((Array) object, done, strId);
				deep--;
				return rtn;
			}
			// List
			if (object instanceof List) {
				rtn = _serializeList((List) object, done, strId);
				deep--;
				return rtn;
			}
			// Query
			if (object instanceof Query) {
				rtn = _serializeQuery((Query) object, done, strId);
				deep--;
				return rtn;
			}
		}
		finally {
			done.remove(raw);
		}
		// Others
		rtn = "<STRUCT ID=\"" + strId + "\" TYPE=\"" + Caster.toTypeName(object) + "\"></STRUCT>";
		deep--;
		return rtn;
	}

	@Override
	public void writeOut(PageContext pc, Object source, Writer writer) throws ConverterException, IOException {
		writer.write(serialize(source));
		writer.flush();
	}

	/**
	 * serialize an Object to his xml Format represenation and create a valid wddx representation
	 * 
	 * @param object Object to serialize
	 * @return serialized wddx package
	 * @throws ConverterException
	 */
	public String serialize(Object object) throws ConverterException {
		deep = 0;

		StringBuilder sb = new StringBuilder();
		// if(xmlConform)sb.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
		deep++;
		sb.append(_serialize(object, new HashMap<Object, String>()));
		deep--;
		return sb.toString();
	}

	/**
	 * deserialize a WDDX Package (XML Element) to a runtime object
	 * 
	 * @param element
	 * @return deserialized Element
	 * @throws ConverterException
	 */
	private Object _deserialize(Element element) throws ConverterException {
		String nodeName = element.getNodeName().toLowerCase();

		// NULL
		if (nodeName.equals("null")) {
			return null;
		}
		// String
		else if (nodeName.equals("string")) {
			return _deserializeString(element);
		}
		// Number
		else if (nodeName.equals("number")) {
			try {
				Node data = element.getFirstChild();
				if (data == null) return new Double(0);
				return Caster.toDouble(data.getNodeValue());
			}
			catch (Exception e) {
				throw toConverterException(e);
			}
		}
		// Boolean
		else if (nodeName.equals("boolean")) {
			try {
				return Caster.toBoolean(element.getAttribute("value"));
			}
			catch (PageException e) {
				throw toConverterException(e);

			}
		}
		// Array
		else if (nodeName.equals("array")) {
			return _deserializeArray(element);
		}
		// Component
		else if (nodeName.equals("component") || nodeName.equals("class")) {
			return _deserializeComponent(element);
		}
		// Struct
		else if (nodeName.equals("struct")) {
			return _deserializeStruct(element);
		}
		// Query
		else if (nodeName.equals("recordset")) {
			return _deserializeQuery(element);
		}
		// DateTime
		else if (nodeName.equalsIgnoreCase("dateTime")) {
			try {
				return DateCaster.toDateAdvanced(element.getFirstChild().getNodeValue(), timeZone);
			}
			catch (Exception e) {
				throw toConverterException(e);
			}
		}
		else throw new ConverterException("can't deserialize Element of type [" + nodeName + "] to an Object representation");

	}

	private Object _deserializeString(Element element) {
		NodeList childList = element.getChildNodes();
		int len = childList.getLength();
		StringBuilder sb = new StringBuilder();
		Node data;
		String str;
		for (int i = 0; i < len; i++) {
			data = childList.item(i);
			if (data == null) continue;

			// <char code="0a"/>
			if ("char".equals(data.getNodeName())) {
				str = ((Element) data).getAttribute("code");
				sb.append((char) NumberUtil.hexToInt(str, 10));
			}
			else {
				sb.append(str = data.getNodeValue());
			}
		}
		return sb.toString();
		// return XMLUtil.unescapeXMLString(sb.toString());
	}

	/**
	 * Desirialize a Query Object
	 * 
	 * @param recordset Query Object as XML Element
	 * @return Query Object
	 * @throws ConverterException
	 */
	private Object _deserializeQuery(Element recordset) throws ConverterException {
		try {
			// create Query Object
			Query query = new QueryImpl(lucee.runtime.type.util.ListUtil.listToArray(recordset.getAttribute("fieldNames"), ','),
					Caster.toIntValue(recordset.getAttribute("rowCount")), "query");

			NodeList list = recordset.getChildNodes();
			int len = list.getLength();
			for (int i = 0; i < len; i++) {
				Node node = list.item(i);
				if (node instanceof Element) {
					_deserializeQueryField(query, (Element) node);
				}
			}
			return query;
		}
		catch (PageException e) {
			throw toConverterException(e);
		}

	}

	/**
	 * deserilize a single Field of a query WDDX Object
	 * 
	 * @param query
	 * @param field
	 * @throws ConverterException
	 * @throws PageException
	 */
	private void _deserializeQueryField(Query query, Element field) throws PageException, ConverterException {
		String name = field.getAttribute("name");
		NodeList list = field.getChildNodes();
		int len = list.getLength();
		int count = 0;
		for (int i = 0; i < len; i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				query.setAt(name, ++count, _deserialize((Element) node));
			}
		}

	}

	/**
	 * Desirialize a Component Object
	 * 
	 * @param elComp Component Object as XML Element
	 * @return Component Object
	 * @throws ConverterException
	 * @throws ConverterException
	 */
	private Object _deserializeComponent(Element elComp) throws ConverterException {
		// String type=elStruct.getAttribute("type");
		String name = elComp.getAttribute("name");
		String md5 = elComp.getAttribute("md5");

		// TLPC
		PageContext pc = ThreadLocalPageContext.get();

		// Load comp
		Component comp = null;
		try {
			comp = pc.loadComponent(name);
			if (!ComponentUtil.md5(comp).equals(md5)) {
				throw new ConverterException("component [" + name
						+ "] in this environment has not the same interface as the component to load, it is possible that one off the components has Functions added dynamically.");
			}
		}
		catch (ConverterException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ConverterException(e.getMessage());
		}

		NodeList list = elComp.getChildNodes();
		ComponentScope scope = comp.getComponentScope();
		int len = list.getLength();
		String scopeName;
		Element var, value;
		Collection.Key key;
		for (int i = 0; i < len; i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				var = (Element) node;
				value = getChildElement((Element) node);
				scopeName = var.getAttribute("scope");
				if (value != null) {
					key = Caster.toKey(var.getAttribute("name"), null);
					if (key == null) continue;
					if ("variables".equalsIgnoreCase(scopeName)) scope.setEL(key, _deserialize(value));
					else comp.setEL(key, _deserialize(value));
				}
			}
		}
		return comp;
	}

	/**
	 * Desirialize a Struct Object
	 * 
	 * @param elStruct Struct Object as XML Element
	 * @return Struct Object
	 * @throws ConverterException
	 */
	private Object _deserializeStruct(Element elStruct) throws ConverterException {
		String type = elStruct.getAttribute("type");
		Struct struct = new StructImpl();

		NodeList list = elStruct.getChildNodes();
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			// print.ln(i);

			Node node = list.item(i);
			if (node instanceof Element) {
				Element var = (Element) node;
				Element value = getChildElement((Element) node);
				if (value != null) {
					struct.setEL(var.getAttribute("name"), _deserialize(value));

				}
			}
		}
		if (struct.size() == 0 && type != null && type.length() > 0) {
			return "";
		}
		return struct;
	}

	/**
	 * Desirialize a Struct Object
	 * 
	 * @param el Struct Object as XML Element
	 * @return Struct Object
	 * @throws ConverterException
	 */
	private Array _deserializeArray(Element el) throws ConverterException {
		Array array = new ArrayImpl();

		NodeList list = el.getChildNodes();
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Node node = list.item(i);
			if (node instanceof Element) try {
				array.append(_deserialize((Element) node));
			}
			catch (PageException e) {
				throw toConverterException(e);
			}

		}
		return array;
	}

	/**
	 * return fitst child Element of an Element, if there are no child Elements return null
	 * 
	 * @param parent parent node
	 * @return child Element
	 */
	private Element getChildElement(Element parent) {
		NodeList list = parent.getChildNodes();
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * @return return current blockquote
	 */
	private String goIn() {
		// StringBuilder rtn=new StringBuilder(deep);
		// for(int i=0;i<deep;i++) rtn.append('\t');
		// return rtn.toString();
		return "";
	}

	@Override
	public boolean equals(Object obj) {
		return timeZone.equals(obj);
	}
}