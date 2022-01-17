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
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lucee.commons.date.JREDateTimeUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.CollectionUtil;

/**
 * class to serialize to Convert CFML Objects (query,array,struct usw) to a JavaScript
 * representation
 */
public final class JSConverter extends ConverterSupport {

	private static final String NULL = "null";
	private boolean useShortcuts = false;
	private boolean useWDDX = true;

	/**
	 * serialize a CFML object to a JavaScript Object
	 * 
	 * @param object object to serialize
	 * @param clientVariableName name of the variable to create
	 * @return vonverte Javascript Code as String
	 * @throws ConverterException
	 */
	public String serialize(Object object, String clientVariableName) throws ConverterException {
		StringBuilder sb = new StringBuilder();
		_serialize(clientVariableName, object, sb, new HashSet<Object>());
		String str = sb.toString().trim();
		return clientVariableName + "=" + str + (StringUtil.endsWith(str, ';') ? "" : ";");
		// return sb.toString();
	}

	@Override
	public void writeOut(PageContext pc, Object source, Writer writer) throws ConverterException, IOException {
		writer.write(_serialize(source));
		writer.flush();
	}

	private String _serialize(Object object) throws ConverterException {
		StringBuilder sb = new StringBuilder();
		_serialize("tmp", object, sb, new HashSet<Object>());
		String str = sb.toString().trim();
		return str + (StringUtil.endsWith(str, ';') ? "" : ";");
		// return sb.toString();
	}

	private void _serialize(String name, Object object, StringBuilder sb, Set<Object> done) throws ConverterException {
		// NULL
		if (object == null) {
			sb.append(goIn());
			sb.append(NULL + ";");
			return;
		}
		// CharSequence (String, StringBuilder ...)
		if (object instanceof CharSequence) {
			sb.append(goIn());
			sb.append(StringUtil.escapeJS(object.toString(), '"'));
			sb.append(";");
			return;
		}
		// Number
		if (object instanceof Number) {
			sb.append(goIn());
			sb.append(Caster.toStringPrecise(((Number) object)));
			sb.append(';');
			return;
		}
		// Date
		if (Decision.isDateSimple(object, false)) {
			_serializeDateTime(Caster.toDate(object, false, null, null), sb);
			return;
		}
		// Boolean
		if (object instanceof Boolean) {
			sb.append(goIn());
			sb.append("\"");
			sb.append((((Boolean) object).booleanValue() ? "true" : "false"));
			sb.append("\";");
			return;
		}

		Object raw = LazyConverter.toRaw(object);
		if (done.contains(raw)) {
			sb.append(NULL + ";");
			return;
		}
		done.add(raw);
		try {
			// Struct
			if (object instanceof Struct) {
				_serializeStruct(name, (Struct) object, sb, done);
				return;
			}
			// Map
			if (object instanceof Map) {
				_serializeMap(name, (Map) object, sb, done);
				return;
			}
			// List
			if (object instanceof List) {
				_serializeList(name, (List) object, sb, done);
				return;
			}
			// Array
			if (Decision.isArray(object)) {
				_serializeArray(name, Caster.toArray(object, null), sb, done);
				return;
			}
			// Query
			if (object instanceof Query) {
				_serializeQuery(name, (Query) object, sb, done);
				return;
			}
		}
		finally {
			done.remove(raw);
		}

		sb.append(goIn());
		sb.append(NULL + ";");
		return;
		// throw new ConverterException("can't serialize Object of type ["+Caster.toClassName(object)+"] to
		// a js representation");

	}

	/**
	 * serialize an Array
	 * 
	 * @param name
	 * @param array Array to serialize
	 * @param sb
	 * @param done
	 * @return serialized array
	 * @throws ConverterException
	 */
	private void _serializeArray(String name, Array array, StringBuilder sb, Set<Object> done) throws ConverterException {
		_serializeList(name, array.toList(), sb, done);
	}

	/**
	 * serialize a List (as Array)
	 * 
	 * @param name
	 * @param list List to serialize
	 * @param sb
	 * @param done
	 * @return serialized list
	 * @throws ConverterException
	 */
	private void _serializeList(String name, List list, StringBuilder sb, Set<Object> done) throws ConverterException {

		if (useShortcuts) sb.append("[];");
		else sb.append("new Array();");

		ListIterator it = list.listIterator();
		int index = -1;
		while (it.hasNext()) {
			// if(index!=-1)sb.append(",");
			index = it.nextIndex();
			sb.append(name + "[" + index + "]=");
			_serialize(name + "[" + index + "]", it.next(), sb, done);
			// sb.append(";");
		}
	}

	/**
	 * serialize a Struct
	 * 
	 * @param name
	 * @param struct Struct to serialize
	 * @param done
	 * @param sb2
	 * @return serialized struct
	 * @throws ConverterException
	 */
	private String _serializeStruct(String name, Struct struct, StringBuilder sb, Set<Object> done) throws ConverterException {
		if (useShortcuts) sb.append("{};");
		else sb.append("new Object();");

		Iterator<Entry<Key, Object>> it = struct.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			// lower case ist ok!
			String key = StringUtil.escapeJS(Caster.toString(e.getKey().getLowerString(), ""), '"');
			sb.append(name + "[" + key + "]=");
			_serialize(name + "[" + key + "]", e.getValue(), sb, done);
		}
		return sb.toString();
	}

	/**
	 * serialize a Map (as Struct)
	 * 
	 * @param name
	 * @param map Map to serialize
	 * @param done
	 * @param sb2
	 * @return serialized map
	 * @throws ConverterException
	 */
	private String _serializeMap(String name, Map map, StringBuilder sb, Set<Object> done) throws ConverterException {

		if (useShortcuts) sb.append("{}");
		else sb.append("new Object();");
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			String skey = StringUtil.toLowerCase(StringUtil.escapeJS(key.toString(), '"'));
			sb.append(name + "[" + skey + "]=");
			_serialize(name + "[" + skey + "]", map.get(key), sb, done);
			// sb.append(";");
		}
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
	private void _serializeQuery(String name, Query query, StringBuilder sb, Set<Object> done) throws ConverterException {
		if (useWDDX) _serializeWDDXQuery(name, query, sb, done);
		else _serializeASQuery(name, query, sb, done);
	}

	private void _serializeWDDXQuery(String name, Query query, StringBuilder sb, Set<Object> done) throws ConverterException {
		Iterator<Key> it = query.keyIterator();
		Key k;
		sb.append("new WddxRecordset();");

		int recordcount = query.getRecordcount();
		int i = -1;
		while (it.hasNext()) {
			i++;
			k = it.next();
			if (useShortcuts) sb.append("col" + i + "=[];");
			else sb.append("col" + i + "=new Array();");
			// lower case ist ok!
			String skey = StringUtil.escapeJS(k.getLowerString(), '"');
			for (int y = 0; y < recordcount; y++) {

				sb.append("col" + i + "[" + y + "]=");

				_serialize("col" + i + "[" + y + "]", query.getAt(k, y + 1, null), sb, done);

			}
			sb.append(name + "[" + skey + "]=col" + i + ";col" + i + "=null;");
		}
	}

	private void _serializeASQuery(String name, Query query, StringBuilder sb, Set<Object> done) throws ConverterException {

		Collection.Key[] keys = CollectionUtil.keys(query);
		String[] strKeys = new String[keys.length];
		for (int i = 0; i < strKeys.length; i++) {
			strKeys[i] = StringUtil.escapeJS(keys[i].getString(), '"');
		}
		if (useShortcuts) sb.append("[];");
		else sb.append("new Array();");

		int recordcount = query.getRecordcount();
		for (int i = 0; i < recordcount; i++) {
			if (useShortcuts) sb.append(name + "[" + i + "]={};");
			else sb.append(name + "[" + i + "]=new Object();");

			for (int y = 0; y < strKeys.length; y++) {
				sb.append(name + "[" + i + "][" + strKeys[y] + "]=");
				_serialize(name + "[" + i + "][" + strKeys[y] + "]", query.getAt(keys[y], i + 1, null), sb, done);
			}
		}
	}

	/**
	 * serialize a DateTime
	 * 
	 * @param dateTime DateTime to serialize
	 * @param sb
	 * @param sb
	 * @throws ConverterException
	 */
	private void _serializeDateTime(DateTime dateTime, StringBuilder sb) {

		Calendar c = JREDateTimeUtil.getThreadCalendar(ThreadLocalPageContext.getTimeZone());
		c.setTime(dateTime);
		sb.append(goIn());
		sb.append("new Date(");
		sb.append(c.get(Calendar.YEAR));
		sb.append(",");
		sb.append(c.get(Calendar.MONTH));
		sb.append(",");
		sb.append(c.get(Calendar.DAY_OF_MONTH));
		sb.append(",");
		sb.append(c.get(Calendar.HOUR_OF_DAY));
		sb.append(",");
		sb.append(c.get(Calendar.MINUTE));
		sb.append(",");
		sb.append(c.get(Calendar.SECOND));
		sb.append(");");
	}

	private String goIn() {
		// StringBuilder rtn=new StringBuilder(deep);
		// for(int i=0;i<deep;i++) rtn.append('\t');
		return "";// rtn.toString();
	}

	public void useShortcuts(boolean useShortcuts) {
		this.useShortcuts = useShortcuts;

	}

	public void useWDDX(boolean useWDDX) {
		this.useWDDX = useWDDX;
	}

	/*
	 * @param args
	 * 
	 * @throws Exception
	 * 
	 * public static void main(String[] args) throws Exception { JSConverter js=new JSConverter(); Query
	 * query=QueryNew.call(null,"aaa,bbb,ccc"); QueryAddRow.call(null,query);
	 * QuerySetCell.call(null,query,"aaa","1.1"); QuerySetCell.call(null,query,"bbb","1.2");
	 * QuerySetCell.call(null,query,"ccc","1.3"); QueryAddRow.call(null,query);
	 * QuerySetCell.call(null,query,"aaa","2.1"); QuerySetCell.call(null,query,"bbb","2.2");
	 * QuerySetCell.call(null,query,"ccc","2.3"); QueryAddRow.call(null,query);
	 * QuerySetCell.call(null,query,"aaa","3.1"); QuerySetCell.call(null,query,"bbb","3.2");
	 * QuerySetCell.call(null,query,"ccc","3.3<hello>"); Array arr2=List ToArray.call(null,"111,222");
	 * Array arr=List ToArray.call(null,"aaaa,bbb,ccc,dddd,eee");
	 * 
	 * arr.set(10,arr2);
	 * 
	 * Struct sct= new Struct(); sct.set("aaa","val1"); sct.set("bbb","val2"); sct.set("ccc","val3");
	 * sct.set("ddd",arr2);
	 * 
	 * /* }
	 */
}