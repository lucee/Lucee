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
package lucee.runtime.converter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
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

import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.PageContext;
import lucee.runtime.coder.Base64Coder;
import lucee.runtime.component.Property;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.Controler;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.java.JavaObject;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.reflection.Reflector;
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
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.ComponentUtil;

/**
 * class to serialize and desirilize WDDX Packes
 */
public final class JSONConverter extends ConverterSupport {

	private static final Collection.Key REMOTING_FETCH = KeyImpl.getInstance("remotingFetch");

	private static final Key TO_JSON = KeyImpl.getInstance("_toJson");
	private static final String NULL_STRING = "";

	private boolean ignoreRemotingFetch;

	private CharsetEncoder charsetEncoder;

	private String pattern;

	/**
	 * @param ignoreRemotingFetch
	 * @param charset if set, characters not supported by the charset are escaped.
	 * @param patternCf
	 */
	public JSONConverter(boolean ignoreRemotingFetch, Charset charset) {
		this(ignoreRemotingFetch, charset, JSONDateFormat.PATTERN_CF);
	}

	public JSONConverter(boolean ignoreRemotingFetch, Charset charset, String pattern) {
		this.ignoreRemotingFetch = ignoreRemotingFetch;
		charsetEncoder = charset != null ? charset.newEncoder() : null;// .canEncode("string");
		this.pattern = pattern;
	}

	/**
	 * serialize Serializable class
	 * 
	 * @param serializable
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 */

	private void _serializeClass(PageContext pc, Set test, Class clazz, Object obj, StringBuilder sb, int queryFormat, Set<Object> done) throws ConverterException {

		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		if (test == null) test = new HashSet();

		// Fields
		Field[] fields = clazz.getFields();
		Field field;
		for (int i = 0; i < fields.length; i++) {
			field = fields[i];
			if (obj != null || (field.getModifiers() & Modifier.STATIC) > 0) try {
				sct.setEL(field.getName(), testRecusrion(test, field.get(obj)));
			}
			catch (Exception e) {
				LogUtil.log(ThreadLocalPageContext.getConfig(pc), Controler.class.getName(), e);
			}
		}
		if (obj != null) {
			// setters
			Method[] setters = Reflector.getSetters(clazz);
			for (int i = 0; i < setters.length; i++) {
				sct.setEL(setters[i].getName().substring(3), CollectionUtil.NULL);
			}
			// getters
			Method[] getters = Reflector.getGetters(clazz);
			for (int i = 0; i < getters.length; i++) {
				try {
					sct.setEL(getters[i].getName().substring(3), testRecusrion(test, getters[i].invoke(obj, ArrayUtil.OBJECT_EMPTY)));

				}
				catch (Exception e) {
				}
			}
		}

		test.add(clazz);

		_serializeStruct(pc, test, sct, sb, queryFormat, true, done);
	}

	private Object testRecusrion(Set test, Object obj) {
		if (test.contains(obj.getClass())) return obj.getClass().getName();
		return obj;
	}

	/**
	 * serialize a Date
	 * 
	 * @param date Date to serialize
	 * @param sb
	 * @throws ConverterException
	 */
	private void _serializeDate(Date date, StringBuilder sb) {
		_serializeDateTime(new DateTimeImpl(date), sb);
	}

	/**
	 * serialize a DateTime
	 * 
	 * @param dateTime DateTime to serialize
	 * @param sb
	 * @throws ConverterException
	 */
	private void _serializeDateTime(DateTime dateTime, StringBuilder sb) {

		sb.append(StringUtil.escapeJS(JSONDateFormat.format(dateTime, null, pattern), '"', charsetEncoder));

		/*
		 * try { sb.append(goIn()); sb.append("createDateTime(");
		 * sb.append(DateFormat.call(null,dateTime,"yyyy,m,d")); sb.append(' ');
		 * sb.append(TimeFormat.call(null,dateTime,"HH:mm:ss")); sb.append(')'); } catch (PageException e) {
		 * throw new ConverterException(e); }
		 */
		// Januar, 01 2000 01:01:01
	}

	/**
	 * serialize an Array
	 * 
	 * @param array Array to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeArray(PageContext pc, Set test, Array array, StringBuilder sb, int queryFormat, Set<Object> done) throws ConverterException {
		_serializeList(pc, test, array.toList(), sb, queryFormat, done);
	}

	/**
	 * serialize a List (as Array)
	 * 
	 * @param list List to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeList(PageContext pc, Set test, List list, StringBuilder sb, int queryFormat, Set<Object> done) throws ConverterException {

		sb.append(goIn());
		sb.append("[");
		boolean doIt = false;
		ListIterator it = list.listIterator();
		while (it.hasNext()) {
			if (doIt) sb.append(',');
			doIt = true;
			_serialize(pc, test, it.next(), sb, queryFormat, done);
		}

		sb.append(']');
	}

	private void _serializeArray(PageContext pc, Set test, Object[] arr, StringBuilder sb, int queryFormat, Set<Object> done) throws ConverterException {

		sb.append(goIn());
		sb.append("[");
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) sb.append(',');
			_serialize(pc, test, arr[i], sb, queryFormat, done);
		}
		sb.append(']');
	}

	/**
	 * serialize a Struct
	 * 
	 * @param struct Struct to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param addUDFs
	 * @param done
	 * @throws ConverterException
	 */
	public void _serializeStruct(PageContext pc, Set test, Struct struct, StringBuilder sb, int queryFormat, boolean addUDFs, Set<Object> done) throws ConverterException {

		ApplicationContextSupport acs = (ApplicationContextSupport) pc.getApplicationContext();
		boolean preserveCase = acs.getSerializationSettings().getPreserveCaseForStructKey(); // preserve case by default for Struct

		// Component
		if (struct instanceof Component) {
			String res = castToJson(pc, (Component) struct, NULL_STRING);
			if (res != NULL_STRING) {
				sb.append(res);
				return;
			}
		}

		sb.append(goIn());
		sb.append("{");

		Iterator<Entry<Key, Object>> it = struct.entryIterator();
		Entry<Key, Object> e;
		String k;
		Object value;
		boolean doIt = false;
		while (it.hasNext()) {

			e = it.next();
			k = e.getKey().getString();
			if (!preserveCase) k = k.toUpperCase();
			value = e.getValue();

			if (!addUDFs && (value instanceof UDF || value == null)) continue;

			if (doIt) sb.append(',');

			doIt = true;
			sb.append(StringUtil.escapeJS(k, '"', charsetEncoder));
			sb.append(':');
			_serialize(pc, test, value, sb, queryFormat, done);
		}

		if (struct instanceof Component) {
			Boolean remotingFetch;
			Component comp = (Component) struct;
			boolean isPeristent = false;
			isPeristent = comp.isPersistent();

			Property[] props = comp.getProperties(false, true, false, false);
			ComponentScope scope = comp.getComponentScope();
			for (int i = 0; i < props.length; i++) {
				if (!ignoreRemotingFetch) {
					remotingFetch = Caster.toBoolean(props[i].getDynamicAttributes().get(REMOTING_FETCH, null), null);
					if (remotingFetch == null) {
						if (isPeristent && ORMUtil.isRelated(props[i])) continue;
					}
					else if (!remotingFetch.booleanValue()) continue;

				}
				Key key = KeyImpl.getInstance(props[i].getName());
				value = scope.get(key, null);
				if (!addUDFs && (value instanceof UDF || value == null)) continue;
				if (doIt) sb.append(',');
				doIt = true;
				sb.append(StringUtil.escapeJS(key.getString(), '"', charsetEncoder));
				sb.append(':');
				_serialize(pc, test, value, sb, queryFormat, done);
			}
		}

		sb.append('}');
	}

	private static String castToJson(PageContext pc, Component c, String defaultValue) throws ConverterException {
		Object o = c.get(TO_JSON, null);
		if (!(o instanceof UDF)) return defaultValue;
		UDF udf = (UDF) o;
		if (udf.getReturnType() != CFTypes.TYPE_VOID && udf.getFunctionArguments().length == 0) {
			try {
				return Caster.toString(c.call(pc, TO_JSON, new Object[0]));
			}
			catch (PageException e) {
				throw toConverterException(e);
			}
		}
		return defaultValue;
	}

	/**
	 * serialize a Map (as Struct)
	 * 
	 * @param map Map to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeMap(PageContext pc, Set test, Map map, StringBuilder sb, int queryFormat, Set<Object> done) throws ConverterException {
		sb.append(goIn());
		sb.append("{");

		Iterator it = map.keySet().iterator();
		boolean doIt = false;
		while (it.hasNext()) {
			Object key = it.next();
			if (doIt) sb.append(',');
			doIt = true;
			sb.append(StringUtil.escapeJS(key.toString(), '"', charsetEncoder));
			sb.append(':');
			_serialize(pc, test, map.get(key), sb, queryFormat, done);
		}

		sb.append('}');
	}

	/**
	 * serialize a Component
	 * 
	 * @param component Component to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeComponent(PageContext pc, Set test, Component component, StringBuilder sb, int queryFormat, Set<Object> done) throws ConverterException {
		ComponentSpecificAccess cw = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, component);
		_serializeStruct(pc, test, cw, sb, queryFormat, false, done);
	}

	private void _serializeUDF(PageContext pc, Set test, UDF udf, StringBuilder sb, int queryFormat, Set<Object> done) throws ConverterException {
		Struct sct = new StructImpl();
		try {
			// Meta
			Struct meta = udf.getMetaData(pc);
			sct.setEL("Metadata", meta);

			// Parameters
			sct.setEL("MethodAttributes", meta.get("PARAMETERS"));
		}
		catch (PageException e) {
			throw toConverterException(e);
		}

		sct.setEL("Access", ComponentUtil.toStringAccess(udf.getAccess(), "public"));
		sct.setEL("Output", Caster.toBoolean(udf.getOutput()));
		sct.setEL("ReturnType", udf.getReturnTypeAsString());
		try {
			sct.setEL("PagePath", udf.getSource());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		_serializeStruct(pc, test, sct, sb, queryFormat, true, done);
		// TODO key SuperScope and next?
	}

	/**
	 * serialize a Query
	 * 
	 * @param query Query to serialize
	 * @param sb
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 */
	private void _serializeQuery(PageContext pc, Set test, Query query, StringBuilder sb, int queryFormat, Set<Object> done) throws ConverterException {

		ApplicationContextSupport acs = (ApplicationContextSupport) pc.getApplicationContext();
		boolean preserveCase = acs.getSerializationSettings().getPreserveCaseForQueryColumn(); // UPPERCASE column keys by default for Query

		Collection.Key[] _keys = CollectionUtil.keys(query);

		if (queryFormat == SerializationSettings.SERIALIZE_AS_STRUCT) {
			sb.append(goIn());
			sb.append("[");
			int rc = query.getRecordcount();
			for (int row = 1; row <= rc; row++) {
				if (row > 1) sb.append(',');
				sb.append("{");
				for (int col = 0; col < _keys.length; col++) {
					if (col > 0) sb.append(',');
					sb.append(StringUtil.escapeJS(preserveCase ? _keys[col].getString() : _keys[col].getUpperString(), '"', charsetEncoder));
					sb.append(':');
					try {
						_serialize(pc, test, query.getAt(_keys[col], row), sb, queryFormat, done);
					}
					catch (PageException e) {
						_serialize(pc, test, e.getMessage(), sb, queryFormat, done);
					}
				}

				sb.append("}");
			}
			sb.append("]");

			return;
		}

		sb.append(goIn());
		sb.append("{");

		/*
		 * 
		 * {"DATA":[["a","b"],["c","d"]]} {"DATA":{"aaa":["a","c"],"bbb":["b","d"]}}
		 */
		// Rowcount
		if (queryFormat == SerializationSettings.SERIALIZE_AS_COLUMN) {
			sb.append("\"ROWCOUNT\":");
			sb.append(Caster.toString(query.getRecordcount()));
			sb.append(',');
		}

		// Columns
		sb.append("\"COLUMNS\":[");
		String[] cols = query.getColumns();
		for (int i = 0; i < cols.length; i++) {
			if (i > 0) sb.append(",");
			sb.append(StringUtil.escapeJS(preserveCase ? cols[i] : cols[i].toUpperCase(), '"', charsetEncoder));
		}
		sb.append("],");

		// Data
		sb.append("\"DATA\":");
		if (queryFormat == SerializationSettings.SERIALIZE_AS_COLUMN) {
			sb.append('{');
			boolean oDoIt = false;
			int len = query.getRecordcount();
			pc = ThreadLocalPageContext.get(pc);
			boolean upperCase = false;
			if (pc != null) upperCase = pc.getCurrentTemplateDialect() == CFMLEngine.DIALECT_CFML && ((ConfigWebPro) pc.getConfig()).getDotNotationUpperCase();

			for (int i = 0; i < _keys.length; i++) {
				if (oDoIt) sb.append(',');
				oDoIt = true;
				sb.append(goIn());

				sb.append(StringUtil.escapeJS(upperCase ? _keys[i].getUpperString() : _keys[i].getString(), '"', charsetEncoder));
				sb.append(":[");
				boolean doIt = false;
				for (int y = 1; y <= len; y++) {
					if (doIt) sb.append(',');
					doIt = true;
					try {
						_serialize(pc, test, query.getAt(_keys[i], y), sb, queryFormat, done);
					}
					catch (PageException e) {
						_serialize(pc, test, e.getMessage(), sb, queryFormat, done);
					}
				}

				sb.append(']');
			}

			sb.append('}');
		}
		else {
			sb.append('[');
			boolean oDoIt = false;
			int len = query.getRecordcount();
			for (int row = 1; row <= len; row++) {
				if (oDoIt) sb.append(',');
				oDoIt = true;

				sb.append("[");
				boolean doIt = false;
				for (int col = 0; col < _keys.length; col++) {
					if (doIt) sb.append(',');
					doIt = true;
					try {
						_serialize(pc, test, query.getAt(_keys[col], row), sb, queryFormat, done);
					}
					catch (PageException e) {
						_serialize(pc, test, e.getMessage(), sb, queryFormat, done);
					}
				}
				sb.append(']');
			}
			sb.append(']');
		}
		sb.append('}');
	}

	/**
	 * serialize an Object to his xml Format represenation
	 * 
	 * @param object Object to serialize
	 * @param sb StringBuilder to write data
	 * @param serializeQueryByColumns
	 * @param done
	 * @throws ConverterException
	 */
	private void _serialize(PageContext pc, Set test, Object object, StringBuilder sb, int queryFormat, Set done) throws ConverterException {

		// NULL
		if (object == null || object == CollectionUtil.NULL) {
			sb.append(goIn());
			sb.append("null");
			return;
		}
		// String
		if (object instanceof String || object instanceof StringBuilder) {
			sb.append(goIn());
			sb.append(StringUtil.escapeJS(object.toString(), '"', charsetEncoder));
			return;
		}
		// TimeZone
		if (object instanceof TimeZone) {
			sb.append(goIn());
			sb.append(StringUtil.escapeJS(((TimeZone) object).getID(), '"', charsetEncoder));
			return;
		}
		// Locale
		if (object instanceof Locale) {
			sb.append(goIn());
			sb.append(StringUtil.escapeJS(LocaleFactory.toString((Locale) object), '"', charsetEncoder));
			return;
		}
		// Character
		if (object instanceof Character) {
			sb.append(goIn());
			sb.append(StringUtil.escapeJS(String.valueOf(((Character) object).charValue()), '"', charsetEncoder));
			return;
		}
		// Number
		if (object instanceof Number) {
			sb.append(Caster.toStringPrecise((Number) object));
			return;
		}
		// Boolean
		if (object instanceof Boolean) {
			sb.append(goIn());
			sb.append(Caster.toString(((Boolean) object).booleanValue()));
			return;
		}
		// DateTime
		if (object instanceof DateTime) {
			_serializeDateTime((DateTime) object, sb);
			return;
		}
		// Date
		if (object instanceof Date) {
			_serializeDate((Date) object, sb);
			return;
		}
		// XML
		if (object instanceof Node) {
			_serializeXML((Node) object, sb);
			return;
		}
		// Timespan
		if (object instanceof TimeSpan) {
			_serializeTimeSpan((TimeSpan) object, sb);
			return;
		}
		// File
		if (object instanceof File) {
			_serialize(pc, test, ((File) object).getAbsolutePath(), sb, queryFormat, done);
			return;
		}
		// String Converter
		if (object instanceof ScriptConvertable) {
			sb.append(((ScriptConvertable) object).serialize());
			return;
		}
		// byte[]
		if (object instanceof byte[]) {
			sb.append("\"" + Base64Coder.encode((byte[]) object) + "\"");
			return;
		}
		Object raw = LazyConverter.toRaw(object);
		if (done.contains(raw)) {
			sb.append(goIn());
			sb.append("null");
			return;
		}

		done.add(raw);

		try {
			// Component
			if (object instanceof Component) {
				_serializeComponent(pc, test, (Component) object, sb, queryFormat, done);
				return;
			}
			// UDF
			if (object instanceof UDF) {
				_serializeUDF(pc, test, (UDF) object, sb, queryFormat, done);
				return;
			}
			// Struct
			if (object instanceof Struct) {
				_serializeStruct(pc, test, (Struct) object, sb, queryFormat, true, done);
				return;
			}
			// Map
			if (object instanceof Map) {
				_serializeMap(pc, test, (Map) object, sb, queryFormat, done);
				return;
			}
			// Array
			if (object instanceof Array) {
				_serializeArray(pc, test, (Array) object, sb, queryFormat, done);
				return;
			}
			// List
			if (object instanceof List) {
				_serializeList(pc, test, (List) object, sb, queryFormat, done);
				return;
			}
			// Query
			if (object instanceof Query) {
				_serializeQuery(pc, test, (Query) object, sb, queryFormat, done);
				return;
			}
			// Native Array
			if (Decision.isNativeArray(object)) {
				if (object instanceof char[]) _serialize(pc, test, new String((char[]) object), sb, queryFormat, done);
				else {
					_serializeArray(pc, test, ArrayUtil.toReferenceType(object, ArrayUtil.OBJECT_EMPTY), sb, queryFormat, done);
				}
				return;
			}
			// ObjectWrap
			if (object instanceof ObjectWrap) {
				try {
					_serialize(pc, test, ((ObjectWrap) object).getEmbededObject(), sb, queryFormat, done);
				}
				catch (PageException e) {
					if (object instanceof JavaObject) {
						_serializeClass(pc, test, ((JavaObject) object).getClazz(), null, sb, queryFormat, done);
					}
					else throw new ConverterException("can't serialize Object of type [ " + Caster.toClassName(object) + " ]");
				}
				return;
			}

			_serializeClass(pc, test, object.getClass(), object, sb, queryFormat, done);
		}
		finally {
			done.remove(raw);
		}
	}

	private void _serializeXML(Node node, StringBuilder sb) {
		node = XMLCaster.toRawNode(node);
		sb.append(goIn());
		sb.append(StringUtil.escapeJS(XMLCaster.toString(node, ""), '"', charsetEncoder));
	}

	private void _serializeTimeSpan(TimeSpan ts, StringBuilder sb) throws ConverterException {
		sb.append(goIn());
		try {
			sb.append(ts.castToDoubleValue());
		}
		catch (PageException e) {// should never happen because TimeSpanImpl does not throw an exception
			throw new ConverterException(e.getMessage());
		}
	}

	/**
	 * serialize an Object to his literal Format
	 * 
	 * @param object Object to serialize
	 * @param serializeQueryByColumns
	 * @return serialized wddx package
	 * @throws ConverterException
	 */
	public String serialize(PageContext pc, Object object, int queryFormat) throws ConverterException {
		StringBuilder sb = new StringBuilder(256);
		_serialize(pc, null, object, sb, queryFormat, new HashSet());
		return sb.toString();
	}

	@Override
	public void writeOut(PageContext pc, Object source, Writer writer) throws ConverterException, IOException {
		writer.write(serialize(pc, source, SerializationSettings.SERIALIZE_AS_ROW));
		writer.flush();
	}

	/**
	 * @return return current blockquote
	 */
	private String goIn() {
		return "";
	}

	public static String serialize(PageContext pc, Object o) throws ConverterException {
		JSONConverter converter = new JSONConverter(false, null);
		return converter.serialize(pc, o, SerializationSettings.SERIALIZE_AS_ROW);
	}

	public static int toQueryFormat(Object options, int defaultValue) {
		Boolean b = Caster.toBoolean(options, null);
		if (Boolean.TRUE.equals(b)) return SerializationSettings.SERIALIZE_AS_COLUMN;
		if (Boolean.FALSE.equals(b)) return SerializationSettings.SERIALIZE_AS_ROW;

		String str = Caster.toString(options, null);
		if ("row".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_ROW;
		if ("col".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_COLUMN;
		if ("column".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_COLUMN;
		if ("struct".equalsIgnoreCase(str)) return SerializationSettings.SERIALIZE_AS_STRUCT;

		return defaultValue;
	}

}