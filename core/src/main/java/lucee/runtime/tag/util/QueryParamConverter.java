/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.tag.util;

import java.nio.charset.Charset;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.db.SQL;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.db.SQLImpl;
import lucee.runtime.db.SQLItem;
import lucee.runtime.db.SQLItemImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class QueryParamConverter {

	public static SQL convert(String sql, Argument params) throws PageException {
		// All items of arguments will be key-based or position-based so proxy appropriate arrays
		Iterator<Entry<Key, Object>> it = params.entryIterator();
		if (it.hasNext()) {
			Entry<Key, Object> e = it.next();
			if (e.getKey().getString() == new String("1")) {
				// This indicates the first item has key == 1 therefore treat as array
				return convert(sql, Caster.toArray(params));
			}
		}
		return convert(sql, Caster.toStruct(params));
	}

	public static SQL convert(String sql, Struct params) throws PageException {
		Iterator<Entry<Key, Object>> it = params.entryIterator();
		List<SQLItems<NamedSQLItem>> namedItems = new ArrayList<SQLItems<NamedSQLItem>>();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			SQLItems<NamedSQLItem> namedSqlItem = toNamedSQLItem(e.getKey().getString(), e.getValue());
			namedItems.add(namedSqlItem);
		}
		return convert(sql, new ArrayList<SQLItems<SQLItem>>(), namedItems);
	}

	public static SQL convert(String sql, Array params) throws PageException {
		Iterator<Object> it = params.valueIterator();
		List<SQLItems<NamedSQLItem>> namedItems = new ArrayList<SQLItems<NamedSQLItem>>();
		List<SQLItems<SQLItem>> items = new ArrayList<SQLItems<SQLItem>>();
		Object value, paramValue;
		while (it.hasNext()) {
			value = it.next();

			if (Decision.isStruct(value)) {
				Struct sct = (Struct) value;
				// name (optional)
				String name = null;
				Object oName = sct.get(KeyConstants._name, null);
				if (oName != null) name = Caster.toString(oName);

				// value (required)
				paramValue = sct.get(KeyConstants._value);

				Charset charset = CharsetUtil.toCharset(Caster.toString(sct.get(KeyConstants._charset, null), null), null);
				int maxlength = Caster.toIntValue(sct.get("maxlength", null), -1);

				if (StringUtil.isEmpty(name)) {
					items.add(new SQLItems<SQLItem>(new SQLItemImpl(paramValue, Types.VARCHAR, maxlength, charset), sct));
				}
				else {
					namedItems.add(new SQLItems<NamedSQLItem>(new NamedSQLItem(name, paramValue, Types.VARCHAR, maxlength, charset), sct));
				}
			}
			else {
				items.add(new SQLItems<SQLItem>(new SQLItemImpl(value)));
			}
		}
		return convert(sql, items, namedItems);
	}

	public static Struct toStruct(SQLItem item) {
		Struct sct = new StructImpl();
		if (item instanceof NamedSQLItem) {
			NamedSQLItem nsi = (NamedSQLItem) item;
			sct.setEL(KeyConstants._name, nsi.getName());
		}
		sct.setEL(KeyConstants._value, item.getValue());
		sct.setEL(KeyConstants._type, SQLCaster.toStringType(item.getType(), null));
		sct.setEL(KeyConstants._scale, item.getScale());
		return sct;
	}

	private static SQLItems<NamedSQLItem> toNamedSQLItem(String name, Object value) throws PageException {
		if (Decision.isStruct(value)) {
			Struct sct = (Struct) value;
			// value (required if not null)
			value = isParamNull(sct) ? "" : sct.get(KeyConstants._value);
			Charset charset = isParamNull(sct) ? null : CharsetUtil.toCharset(Caster.toString(sct.get(KeyConstants._charset, null), null), null);
			int maxlength = isParamNull(sct) ? -1 : Caster.toIntValue(sct.get("maxlength", null), -1);
			return new SQLItems<NamedSQLItem>(new NamedSQLItem(name, value, Types.VARCHAR, maxlength, charset), sct); // extracting the type is not necessary, that will happen
																														// inside SQLItems
		}
		return new SQLItems<NamedSQLItem>(new NamedSQLItem(name, value, Types.VARCHAR, -1, null));
	}

	private static SQL convert(String sql, List<SQLItems<SQLItem>> items, List<SQLItems<NamedSQLItem>> namedItems) throws ApplicationException, PageException {
		// if(namedParams.size()==0) return new Pair<String, List<Param>>(sql,params);

		StringBuilder sb = new StringBuilder();
		int sqlLen = sql.length(), initialParamSize = items.size();
		char c, quoteType = 0, p = 0, pp = 0;
		boolean inQuotes = false;
		int qm = 0, _qm = 0;

		for (int i = 0; i < sqlLen; i++) {
			c = sql.charAt(i);

			if (c == '"' || c == '\'') {
				if (inQuotes) {
					if (c == quoteType) {
						if ('\\' != p) {
							inQuotes = false;
						}

					}
				}
				else {
					quoteType = c;
					inQuotes = true;
				}
			}
			else if (!inQuotes) {

				if (c == '?') {

					if (i < (sqlLen - 1) && sql.charAt(i + 1) == '?') {
						sb.append(c).append(c); // '?' is escaped, add both characters so that it's handled later
						i++;
						continue;
					}

					if (++_qm > initialParamSize) throw new ApplicationException("there are more question marks in the SQL than params defined");
				}
				else if (c == ':') {

					if (i < (sqlLen - 1) && sql.charAt(i + 1) == ':') {
						sb.append(c); // ':' is escaped, append it and skip parameter resolution
						i++;
						continue;
					}

					StringBuilder name = new StringBuilder();
					char cc;
					int y = i + 1;
					for (; y < sqlLen; y++) {
						cc = sql.charAt(y);
						if (!isVariableName(cc, true)) break;
						name.append(cc);
					}
					if (name.length() > 0) {
						i = y - 1;
						c = '?';
						items.add(qm, get(name.toString(), namedItems));
					}
				}
			}

			if (c == '?' && !inQuotes) {
				int len = items.get(qm).size();
				for (int j = 1; j <= len; j++) {
					if (j > 1) sb.append(',');
					sb.append('?');
				}
				qm++;
			}
			else {
				sb.append(c);
			}
			pp = p;
			p = c;
		}
		SQLItems<SQLItem> finalItems = flattenItems(items);
		return new SQLImpl(sb.toString(), finalItems.toArray(new SQLItem[finalItems.size()]));
	}

	private static SQLItems<SQLItem> flattenItems(List<SQLItems<SQLItem>> items) {
		SQLItems<SQLItem> finalItems = new SQLItems<SQLItem>();
		Iterator<SQLItems<SQLItem>> listsToFlatten = items.iterator();
		while (listsToFlatten.hasNext()) {
			List<SQLItem> sqlItems = listsToFlatten.next();
			finalItems.addAll(sqlItems);
		}
		return finalItems;
	}

	public static boolean isVariableName(char c, boolean alsoNumber) {
		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_')) return true;
		if (alsoNumber && (c >= '0' && c <= '9')) return true;
		return false;
	}

	private static SQLItems<SQLItem> get(String name, List<SQLItems<NamedSQLItem>> items) throws ApplicationException {
		Iterator<SQLItems<NamedSQLItem>> it = items.iterator();
		SQLItems<NamedSQLItem> item;
		while (it.hasNext()) {
			item = it.next();
			if (item.isEmpty()) {
				throw new ApplicationException("param [" + name + "] may not be empty");
			}
			if (item.get(0).name.equalsIgnoreCase(name)) {
				return item.convertToSQLItems();
			}
		}
		throw new ApplicationException("param [" + name + "] not found");
	}

	private static boolean isParamNull(Struct param) throws PageException {

		Object oNulls = param.get(KeyConstants._null, null);

		// "nulls" seems to be a typo that is currently left for backward compatibility; deprecate?
		if (oNulls == null) oNulls = param.get(KeyConstants._nulls, null);

		if (oNulls != null) return Caster.toBooleanValue(oNulls);

		return false;
	}

	private static class NamedSQLItem extends SQLItemImpl {
		public final String name;

		public NamedSQLItem(String name, Object value, int type, int maxlength, Charset charset) {
			super(value, type, maxlength, charset);
			this.name = name;
		}

		@Override
		public String toString() {
			return "{name:" + name + ";" + super.toString() + "}";
		}

		public String getName() {
			return name;
		}

		@Override
		public NamedSQLItem clone(Object object) {
			NamedSQLItem item = new NamedSQLItem(name, object, getType(), getMaxlength(), getCharset());
			item.setNulls(isNulls());
			item.setScale(getScale());
			return item;
		}
	}

	private static class SQLItems<T extends SQLItem> extends ArrayList<T> {

		public SQLItems() {
		}

		public SQLItems(T item) {
			add(item);
		}

		public SQLItems(T item, Struct sct) throws PageException {

			T filledItem = fillSQLItem(item, sct);
			Object oList = sct.get(KeyConstants._list, null);
			Object value = filledItem.getValue();
			boolean isList = (Decision.isArray(value) && !(value instanceof byte[])) || (oList != null && Caster.toBooleanValue(oList));

			if (isList) {
				Array values;

				if (Decision.isArray(value)) {
					values = Caster.toArray(value);
				}
				else {
					Object oSeparator = sct.get(KeyConstants._separator, null);
					String separator = ",";

					if (oSeparator != null) separator = Caster.toString(oSeparator);

					String v = Caster.toString(filledItem.getValue());
					values = ListUtil.listToArrayRemoveEmpty(v, separator);
				}

				int len = values.size();
				for (int i = 1; i <= len; i++) {
					T clonedItem = (T) filledItem.clone(values.getE(i));
					add(clonedItem);
				}
			}
			else {
				add(filledItem);
			}
		}

		private SQLItems<SQLItem> convertToSQLItems() {
			Iterator<T> it = iterator();
			SQLItems<SQLItem> p = new SQLItems<SQLItem>();
			while (it.hasNext()) {
				p.add(it.next());
			}
			return p;
		}

		private T fillSQLItem(T item, Struct sct) throws PageException, DatabaseException {

			// type (optional)
			Object oType = sct.get(KeyConstants._cfsqltype, null);
			if (oType == null) oType = sct.get(KeyConstants._sqltype, null);
			if (oType == null) oType = sct.get(KeyConstants._type, null);
			if (oType != null) {
				item.setType(SQLCaster.toSQLType(Caster.toString(oType)));
			}

			item.setNulls(isParamNull(sct));

			// scale (optional)
			Object oScale = sct.get(KeyConstants._scale, null);
			if (oScale != null) {
				item.setScale(Caster.toIntValue(oScale));
			}

			return item;
		}
	}

	/*
	 * 
	 * public static void main(String[] args) throws PageException { List<SQLItem> one=new
	 * ArrayList<SQLItem>(); one.add(new SQLItemImpl("aaa",1)); one.add(new SQLItemImpl("bbb",1));
	 * 
	 * List<NamedSQLItem> two=new ArrayList<NamedSQLItem>(); two.add(new
	 * NamedSQLItem("susi","sorglos",1)); two.add(new NamedSQLItem("peter","Petrus",1));
	 * 
	 * SQL sql = convert(
	 * "select ? as x, 'aa:a' as x from test where a=:susi and b=:peter and c=? and d=:susi", one, two);
	 * 
	 * print.e(sql);
	 * 
	 * // array with simple values Array arr=new ArrayImpl(); arr.appendEL("aaa"); arr.appendEL("bbb");
	 * sql = convert( "select * from test where a=? and b=?", arr); print.e(sql);
	 * 
	 * // array with complex values arr=new ArrayImpl(); Struct val1=new StructImpl(); val1.set("value",
	 * "Susi Sorglos"); Struct val2=new StructImpl(); val2.set("value", "123"); val2.set("type",
	 * "integer"); arr.append(val1); arr.append(val2); sql = convert(
	 * "select * from test where a=? and b=?", arr); print.e(sql);
	 * 
	 * // array with mixed values arr.appendEL("ccc"); arr.appendEL("ddd"); sql = convert(
	 * "select * from test where a=? and b=? and c=? and d=?", arr); print.e(sql);
	 * 
	 * // array mixed with named values Struct val3=new StructImpl(); val3.set("value", "456");
	 * val3.set("type", "integer"); val3.set("name", "susi"); arr.append(val3); sql = convert(
	 * "select :susi as name from test where a=? and b=? and c=? and d=?", arr); print.e(sql);
	 * 
	 * 
	 * // struct with simple values Struct sct=new StructImpl(); sct.set("abc", "Sorglos"); sql =
	 * convert( "select * from test where a=:abc", sct); print.e(sql);
	 * 
	 * // struct with mixed values sct.set("peter", val1); sct.set("susi", val3); sql = convert(
	 * "select :peter as p, :susi as s from test where a=:abc", sct); print.e(sql);
	 * 
	 * 
	 * }
	 */

}
