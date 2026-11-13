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
package lucee.runtime.functions.query;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.QueryUtil;

/**
 * Implements the CFML Function querynew
 */
public final class QueryNew extends BIF {

	private static final long serialVersionUID = -4313766961671090938L;

	public static lucee.runtime.type.Query call(PageContext pc, Object columnNames) throws PageException {
		Array arr = toArray(pc, columnNames, 1);
		if (arr.size() > 0 && Decision.isStruct(arr.getE(1))) {
			QueryImpl qry = new QueryImpl(new Key[0], arr.size(), "");
			Iterator<Object> it = arr.valueIterator();
			Iterator<Entry<Key, Object>> rit;
			int row = 0;
			Entry<Key, Object> e;
			Set<Key> containsCache = new HashSet<>();
			while (it.hasNext()) {
				rit = Caster.toStruct(it.next()).entryIterator();
				row++;
				while (rit.hasNext()) {
					e = rit.next();
					// add column
					if (!containsCache.contains(e.getKey())) {
						qry.addColumn(e.getKey(), new ArrayImpl());
						containsCache.add(e.getKey());
					}
					qry.setAt(e.getKey(), row, e.getValue());
				}
			}
			return qry;
		}

		return new QueryImpl(toArray(pc, columnNames, 1), 0, "query");
	}

	public static lucee.runtime.type.Query call(PageContext pc, Object columnNames, Object columnTypes) throws PageException {
		if (StringUtil.isEmpty(columnTypes)) return call(pc, columnNames);
		return new QueryImpl(toArray(pc, columnNames, 1), toArray(pc, columnTypes, 2), 0, "query");
	}

	public static lucee.runtime.type.Query call(PageContext pc, Object columnNames, Object columnTypes, Object data) throws PageException {

		Array cn = toArray(pc, columnNames, 1);
		lucee.runtime.type.Query qry;
		if (StringUtil.isEmpty(columnTypes)) qry = new QueryImpl(cn, 0, "query");
		else qry = new QueryImpl(cn, toArray(pc, columnTypes, 2), 0, "query");

		if (data == null) return qry;
		return populate(pc, qry, data, true);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), args[2]);
	}

	public static Query populate(PageContext pc, Query qry, Object data, boolean arrayAsMultiRow) throws PageException {
		if (Decision.isArray(data)) return _populate(pc, qry, Caster.toArray(data));
		else if (Decision.isStruct(data)) return _populate(pc, qry, Caster.toStruct(data), arrayAsMultiRow);
		else throw new FunctionException(pc, "QueryNew", 3, "data", "the date must be defined as array of structs , array of arrays or struct of arrays");
	}

	private static Query _populate(PageContext pc, Query qry, Struct data, boolean arrayAsMultiRow) throws PageException {
		Iterator<Entry<Key, Object>> it = data.entryIterator();
		Entry<Key, Object> e;
		Object v;
		Array arr;
		int rows = qry.getRecordcount();
		Key column;
		while (it.hasNext()) {
			e = it.next();
			column = e.getKey();
			// LDEV-3774: a key with leading/trailing whitespace matches the column (ACF compatibility)
			if (qry.getColumn(column, null) == null) {
				column = trimmed(column);
				if (column == null || data.containsKey(column)) continue;
			}
			if (qry.getColumn(column, null) != null) {
				v = e.getValue();
				if (arrayAsMultiRow) arr = Caster.toArray(v, null);
				else arr = null;

				if (arr == null) arr = new ArrayImpl(new Object[] { v });
				populateColumn(qry, column, arr, rows);
			}
		}
		return qry;
	}

	private static void populateColumn(Query qry, Key column, Array data, int rows) throws PageException {
		Iterator<?> it = data.valueIterator();
		int row = rows;
		while (it.hasNext()) {
			row++;
			if (row > qry.getRecordcount()) qry.addRow();
			qry.setAt(column, row, it.next());
		}
	}

	private static Query _populate(PageContext pc, Query qry, Array data) throws PageException {
		// check if the array only contains simple values or mixed
		Iterator<?> it = data.valueIterator();
		Object o;
		boolean hasSimpleValues = false;
		while (it.hasNext()) {
			o = it.next();
			if (!Decision.isStruct(o) && !Decision.isArray(o)) hasSimpleValues = true;
		}

		if (hasSimpleValues) {
			qry.addRow();
			populateRow(qry, data);
		}
		else {
			it = data.valueIterator();
			while (it.hasNext()) {
				o = it.next();
				qry.addRow();
				if (Decision.isStruct(o)) populateRow(qry, Caster.toStruct(o));
				else if (Decision.isArray(o)) populateRow(qry, Caster.toArray(o));
				else {
					populateRow(qry, new ArrayImpl(new Object[] { o }));
				}
			}
		}
		return qry;
	}

	private static void populateRow(Query qry, Struct data) throws PageException {
		Key[] columns = QueryUtil.getColumnNames(qry);
		int row = qry.getRecordcount();
		Object value;
		for (int i = 0; i < columns.length; i++) {
			value = data.get(columns[i], null);
			if (value == null) value = getByTrimmedKey(data, columns[i]);
			if (value != null) qry.setAt(columns[i], row, value);
		}

	}

	/**
	 * LDEV-3774: returns the value of a key that only differs from the column name by leading/trailing
	 * whitespace (ACF compatibility), or null
	 */
	private static Object getByTrimmedKey(Struct data, Key column) {
		Iterator<Entry<Key, Object>> it = data.entryIterator();
		Entry<Key, Object> e;
		Key k;
		while (it.hasNext()) {
			e = it.next();
			k = trimmed(e.getKey());
			if (k != null && k.equals(column)) return e.getValue();
		}
		return null;
	}

	/**
	 * returns the key without leading/trailing whitespace, or null if the key has none
	 */
	private static Key trimmed(Key key) {
		String str = key.getString();
		if (str.isEmpty() || (!Character.isWhitespace(str.charAt(0)) && !Character.isWhitespace(str.charAt(str.length() - 1)))) return null;
		str = str.trim();
		if (str.isEmpty()) return null;
		return KeyImpl.init(str);
	}

	private static void populateRow(Query qry, Array data) throws PageException {
		Iterator<?> it = data.valueIterator();
		Key[] columns = QueryUtil.getColumnNames(qry);
		int row = qry.getRecordcount();
		int index = -1;
		while (it.hasNext()) {
			index++;
			if (index >= columns.length) break;
			qry.setAt(columns[index], row, it.next());
		}
	}

	private static Array toArray(PageContext pc, Object columnNames, int index) throws PageException {
		if (Decision.isArray(columnNames)) return Caster.toArray(columnNames);
		String str = Caster.toString(columnNames, null);
		if (str == null) throw new FunctionException(pc, "QueryNew", index, index == 1 ? "columnNames" : "columnTypes", "Cannot cast columnlist to an Array or a String list");
		return ListUtil.listToArrayTrim(str, ",");
	}
}