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
import java.io.Serializable;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.w3c.dom.Node;

import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.displayFormatting.DateFormat;
import lucee.runtime.functions.displayFormatting.TimeFormat;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.op.Caster;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;

/**
 * class to serialize and desirilize WDDX Packes
 */
public final class ScriptConverter extends ConverterSupport {
	private static final Collection.Key REMOTING_FETCH = KeyImpl.intern("remotingFetch");
	private static final char QUOTE_CHR = '"';
	private static final String QUOTE_STR = String.valueOf(QUOTE_CHR);

	private int deep = 1;
	private boolean ignoreRemotingFetch = true;

	/**
	 * constructor of the class
	 */
	public ScriptConverter() {}

	public ScriptConverter(boolean ignoreRemotingFetch) {
		this.ignoreRemotingFetch = ignoreRemotingFetch;
	}

	/**
	 * serialize Serializable class
	 * 
	 * @param serializable
	 * @param sb
	 * @throws ConverterException
	 */
	private void _serializeSerializable(Serializable serializable, StringBuilder sb) throws ConverterException {

		sb.append(goIn());
		sb.append("evaluateJava(").append(QUOTE_CHR);
		try {
			sb.append(JavaConverter.serialize(serializable));
		}
		catch (IOException e) {
			throw toConverterException(e);
		}
		sb.append(QUOTE_CHR).append(')');
	}

	/**
	 * serialize a Date
	 * 
	 * @param date Date to serialize
	 * @param sb
	 * @throws ConverterException
	 */
	private void _serializeDate(Date date, StringBuilder sb) throws ConverterException {
		_serializeDateTime(new DateTimeImpl(date), sb);
	}

	/**
	 * serialize a DateTime
	 * 
	 * @param dateTime DateTime to serialize
	 * @param sb
	 * @throws ConverterException
	 */
	private void _serializeDateTime(DateTime dateTime, StringBuilder sb) throws ConverterException {

		try {
			TimeZone tz = ThreadLocalPageContext.getTimeZone();
			sb.append(goIn());
			sb.append("createDateTime(");
			sb.append(DateFormat.call(null, dateTime, "yyyy,m,d", tz));
			sb.append(',');
			sb.append(TimeFormat.call(null, dateTime, "H,m,s,l,", tz));
			sb.append('"').append(tz.getID()).append('"');
			sb.append(')');
		}
		catch (PageException e) {
			throw toConverterException(e);
		}
	}

	/**
	 * serialize an Array
	 * 
	 * @param array Array to serialize
	 * @param sb
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeArray(Array array, StringBuilder sb, Set<Object> done) throws ConverterException {
		_serializeList(array.toList(), sb, done);
	}

	/**
	 * serialize a List (as Array)
	 * 
	 * @param list List to serialize
	 * @param sb
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeList(List list, StringBuilder sb, Set<Object> done) throws ConverterException {

		sb.append(goIn());
		sb.append("[");
		boolean doIt = false;
		ListIterator it = list.listIterator();
		while (it.hasNext()) {
			if (doIt) sb.append(',');
			doIt = true;
			_serialize(it.next(), sb, done);
		}

		sb.append(']');
	}

	/**
	 * serialize a Struct
	 * 
	 * @param struct Struct to serialize
	 * @param sb
	 * @param done
	 * @throws ConverterException
	 */
	public void _serializeStruct(Struct struct, StringBuilder sb, Set<Object> done) throws ConverterException {
		sb.append(goIn());
		boolean ordered = struct instanceof StructImpl && ((StructImpl) struct).getType() == Struct.TYPE_LINKED;

		if (ordered) sb.append('[');
		else sb.append('{');
		Iterator<Entry<Key, Object>> it = struct.entryIterator();
		Entry<Key, Object> e;
		boolean doIt = false;
		deep++;
		while (it.hasNext()) {
			e = it.next();
			String key = e.getKey().getString();
			if (doIt) sb.append(',');
			doIt = true;
			sb.append(QUOTE_CHR);
			sb.append(escape(key));
			sb.append(QUOTE_CHR);
			sb.append(':');
			_serialize(e.getValue(), sb, done);
		}
		deep--;

		if (ordered) sb.append(']');
		else sb.append('}');
	}

	public String serializeStruct(Struct struct, Set<Collection.Key> ignoreSet) throws ConverterException {
		StringBuilder sb = new StringBuilder();
		sb.append(goIn());
		sb.append("{");
		boolean hasIgnores = ignoreSet != null;
		Iterator<Key> it = struct.keyIterator();
		boolean doIt = false;
		deep++;
		Key key;
		while (it.hasNext()) {
			key = it.next();
			if (hasIgnores && ignoreSet.contains(key)) continue;
			if (doIt) sb.append(',');
			doIt = true;
			sb.append(QUOTE_CHR);
			sb.append(escape(key.getString()));
			sb.append(QUOTE_CHR);
			sb.append(':');
			_serialize(struct.get(key, null), sb, new HashSet<Object>());
		}
		deep--;

		return sb.append('}').toString();
	}

	/**
	 * serialize a Map (as Struct)
	 * 
	 * @param map Map to serialize
	 * @param sb
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeMap(Map map, StringBuilder sb, Set<Object> done) throws ConverterException {
		if (map instanceof Serializable) {
			_serializeSerializable((Serializable) map, sb);
			return;
		}
		sb.append(goIn());
		sb.append("{");

		Iterator it = map.keySet().iterator();
		boolean doIt = false;
		deep++;
		while (it.hasNext()) {
			Object key = it.next();
			if (doIt) sb.append(',');
			doIt = true;
			sb.append(QUOTE_CHR);
			sb.append(escape(key.toString()));
			sb.append(QUOTE_CHR);
			sb.append(':');
			_serialize(map.get(key), sb, done);
		}
		deep--;

		sb.append('}');
	}

	/**
	 * serialize a Component
	 * 
	 * @param c Component to serialize
	 * @param sb
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeComponent(Component c, StringBuilder sb, Set<Object> done) throws ConverterException {

		ComponentSpecificAccess cw = new ComponentSpecificAccess(Component.ACCESS_PRIVATE, c);

		sb.append(goIn());
		try {
			sb.append("evaluateComponent(").append(QUOTE_CHR).append(c.getAbsName()).append(QUOTE_CHR).append(',').append(QUOTE_CHR).append(ComponentUtil.md5(c)).append(QUOTE_CHR)
					.append(",{");
		}
		catch (Exception e) {
			throw toConverterException(e);
		}

		boolean doIt = false;
		Object member;
		{

			Iterator<Entry<Key, Object>> it = cw.entryIterator();
			deep++;
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				member = e.getValue();
				if (member instanceof UDF) continue;
				if (doIt) sb.append(',');
				doIt = true;
				sb.append(QUOTE_CHR);
				sb.append(escape(e.getKey().getString()));
				sb.append(QUOTE_CHR);
				sb.append(':');
				_serialize(member, sb, done);
			}
			sb.append("}");
			deep--;
		}
		{
			boolean isPeristent = c.isPersistent();

			ComponentScope scope = c.getComponentScope();
			Iterator<Entry<Key, Object>> it = scope.entryIterator();
			sb.append(",{");
			deep++;
			doIt = false;
			Property p;
			Boolean remotingFetch;
			Struct props = ignoreRemotingFetch ? null : ComponentUtil.getPropertiesAsStruct(c, false);
			Entry<Key, Object> e;
			Key k;
			while (it.hasNext()) {
				e = it.next();
				k = e.getKey();
				// String key=Caster.toString(it.next(),"");
				if (KeyConstants._THIS.equalsIgnoreCase(k)) continue;
				if (!ignoreRemotingFetch) {
					p = (Property) props.get(k, null);
					if (p != null) {
						remotingFetch = Caster.toBoolean(p.getDynamicAttributes().get(REMOTING_FETCH, null), null);
						if (remotingFetch == null) {
							if (isPeristent && ORMUtil.isRelated(p)) continue;
						}
						else if (!remotingFetch.booleanValue()) continue;
					}
				}

				member = e.getValue();
				if (member instanceof UDF) continue;
				if (doIt) sb.append(',');
				doIt = true;
				sb.append(QUOTE_CHR);
				sb.append(escape(k.getString()));
				sb.append(QUOTE_CHR);
				sb.append(':');
				_serialize(member, sb, done);
			}
			sb.append("}");
			deep--;
		}

		sb.append(")");
		// sb.append("");
		// throw new ConverterException("can't serialize a component "+component.getDisplayName());
	}

	/**
	 * serialize a Query
	 * 
	 * @param query Query to serialize
	 * @param sb
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeQuery(Query query, StringBuilder sb, Set<Object> done) throws ConverterException {

		// Collection.Key[] keys = query.keys();
		Iterator<Key> it = query.keyIterator();
		Key k;
		sb.append(goIn());
		sb.append("query(");

		deep++;
		boolean oDoIt = false;
		int len = query.getRecordcount();
		while (it.hasNext()) {
			k = it.next();
			if (oDoIt) sb.append(',');
			oDoIt = true;
			sb.append(goIn());
			sb.append(QUOTE_CHR);
			sb.append(escape(k.getString()));
			sb.append(QUOTE_CHR);
			sb.append(":[");
			boolean doIt = false;
			for (int y = 1; y <= len; y++) {
				if (doIt) sb.append(',');
				doIt = true;
				try {
					_serialize(query.getAt(k, y), sb, done);
				}
				catch (PageException e) {
					_serialize(e.getMessage(), sb, done);
				}
			}
			sb.append(']');
		}
		deep--;

		sb.append(')');

	}

	/**
	 * serialize an Object to his xml Format represenation
	 * 
	 * @param object Object to serialize
	 * @param sb StringBuilder to write data
	 * @param done
	 * @throws ConverterException
	 */
	private void _serialize(Object object, StringBuilder sb, Set<Object> done) throws ConverterException {
		// try {
		deep++;
		// NULL
		if (object == null) {
			sb.append(goIn());
			sb.append("nullValue()");
			deep--;
			return;
		}
		// String
		if (object instanceof String) {
			sb.append(goIn());
			sb.append(QUOTE_CHR);
			sb.append(escape(object.toString()));
			sb.append(QUOTE_CHR);
			deep--;
			return;
		}
		if (object instanceof TimeZone) {
			sb.append(goIn());
			sb.append(QUOTE_CHR);
			sb.append(escape(((TimeZone) object).getID()));
			sb.append(QUOTE_CHR);
			deep--;
			return;
		}
		if (object instanceof Locale) {
			sb.append(goIn());
			sb.append(QUOTE_CHR);
			sb.append(LocaleFactory.toString((Locale) object));
			sb.append(QUOTE_CHR);
			deep--;
			return;
		}
		// Number
		if (object instanceof Number) {
			sb.append(goIn());
			sb.append(Caster.toString(((Number) object)));
			deep--;
			return;
		}
		// Boolean
		if (object instanceof Boolean) {
			sb.append(goIn());
			sb.append(Caster.toString(((Boolean) object).booleanValue()));
			deep--;
			return;
		}
		// DateTime
		if (object instanceof DateTime) {
			_serializeDateTime((DateTime) object, sb);
			deep--;
			return;
		}
		// Date
		if (object instanceof Date) {
			_serializeDate((Date) object, sb);
			deep--;
			return;
		}
		// XML
		if (object instanceof Node) {
			_serializeXML((Node) object, sb);
			deep--;
			return;
		}
		if (object instanceof ObjectWrap) {
			try {
				_serialize(((ObjectWrap) object).getEmbededObject(), sb, done);
			}
			catch (PageException e) {
				throw toConverterException(e);
			}
			deep--;
			return;
		}
		// Timespan
		if (object instanceof TimeSpan) {
			_serializeTimeSpan((TimeSpan) object, sb);
			deep--;
			return;
		}
		Object raw = LazyConverter.toRaw(object);
		if (done.contains(raw)) {
			sb.append(goIn());
			sb.append("nullValue()");
			deep--;
			return;
		}

		done.add(raw);
		try {
			// Component
			if (object instanceof Component) {
				_serializeComponent((Component) object, sb, done);
				deep--;
				return;
			}

			// Struct
			if (object instanceof Struct) {
				_serializeStruct((Struct) object, sb, done);
				deep--;
				return;
			}
			// Map
			if (object instanceof Map) {
				_serializeMap((Map) object, sb, done);
				deep--;
				return;
			}
			// Array
			if (object instanceof Array) {
				_serializeArray((Array) object, sb, done);
				deep--;
				return;
			}
			// List
			if (object instanceof List) {
				_serializeList((List) object, sb, done);
				deep--;
				return;
			}
			// Query
			if (object instanceof Query) {
				_serializeQuery((Query) object, sb, done);
				deep--;
				return;
			}
			// String Converter
			if (object instanceof ScriptConvertable) {
				sb.append(((ScriptConvertable) object).serialize());
				deep--;
				return;
			}
			if (object instanceof Serializable) {
				_serializeSerializable((Serializable) object, sb);
				deep--;
				return;
			}
		}
		finally {
			done.remove(raw);
		}
		throw new ConverterException("can't serialize Object of type [ " + Caster.toClassName(object) + " ]");
		// deep--;
		/*
		 * } catch(StackOverflowError soe){ throw soe; }
		 */
	}

	private void _serializeXML(Node node, StringBuilder sb) {
		node = XMLCaster.toRawNode(node);
		sb.append(goIn());
		sb.append("xmlParse(").append(QUOTE_CHR);
		sb.append(escape(XMLCaster.toString(node, "")));
		sb.append(QUOTE_CHR).append(")");

	}

	private void _serializeTimeSpan(TimeSpan span, StringBuilder sb) {
		sb.append(goIn());
		sb.append("createTimeSpan(");
		sb.append(span.getDay());
		sb.append(',');
		sb.append(span.getHour());
		sb.append(',');
		sb.append(span.getMinute());
		sb.append(',');
		sb.append(span.getSecond());
		sb.append(')');
	}

	private String escape(String str) {
		return StringUtil.replace(StringUtil.replace(str, QUOTE_STR, QUOTE_STR + QUOTE_STR, false), "#", "##", false);
	}

	@Override
	public void writeOut(PageContext pc, Object source, Writer writer) throws ConverterException, IOException {
		writer.write(serialize(source));
		writer.flush();
	}

	/**
	 * serialize an Object to his literal Format
	 * 
	 * @param object Object to serialize
	 * @return serialized wddx package
	 * @throws ConverterException
	 */
	public String serialize(Object object) throws ConverterException {
		deep = 0;
		StringBuilder sb = new StringBuilder();
		_serialize(object, sb, new HashSet<Object>());
		return sb.toString();
	}

	/**
	 * @return return current blockquote
	 */
	private String goIn() {
		/*
		 * StringBuilder rtn=new StringBuilder('\n'); for(int i=0;i<deep;i++) rtn.append('\t'); return
		 * rtn.toString(); /
		 */

		return "";
	}

}