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

import java.util.Iterator;
import java.util.Map.Entry;

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

	/** @deprecated used by old lucee archives */
	@Deprecated
	public static lucee.runtime.type.Query call(PageContext pc, String columnNames) throws PageException {
		return call(pc, (Object) columnNames);
	}

	/** @deprecated used by old lucee archives */
	@Deprecated
	public static lucee.runtime.type.Query call(PageContext pc, String columnNames, String columnTypes) throws PageException {
		return call(pc, (Object) columnNames, (Object) columnTypes);
	}

	/** @deprecated used by old lucee archives */
	@Deprecated
	public static lucee.runtime.type.Query call(PageContext pc, String columnNames, String columnTypes, Object data) throws PageException {
		return call(pc, (Object) columnNames, (Object) columnTypes, data);
	}

	public static lucee.runtime.type.Query call(PageContext pc, Object columnNames) throws PageException {
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
		return populate(pc, qry, data);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), args[2]);
	}

	public static Query populate(PageContext pc, Query qry, Object data) throws PageException {
		if (Decision.isArray(data)) return _populate(pc, qry, Caster.toArray(data));
		else if (Decision.isStruct(data)) return _populate(pc, qry, Caster.toStruct(data));
		else throw new FunctionException(pc, "QueryNew", 3, "data", "the date must be defined as array of structs , array of arrays or struct of arrays");
	}

	private static Query _populate(PageContext pc, Query qry, Struct data) throws PageException {
		Iterator<Entry<Key, Object>> it = data.entryIterator();
		Entry<Key, Object> e;
		Object v;
		Array arr;
		int rows = qry.getRecordcount();
		while (it.hasNext()) {
			e = it.next();
			if (qry.getColumn(e.getKey(), null) != null) {
				v = e.getValue();
				arr = Caster.toArray(v, null);
				arr = new ArrayImpl(new Object[] { v });
				populateColumn(qry, e.getKey(), arr, rows);
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
			if (value != null) qry.setAt(columns[i], row, value);
		}

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
		if (str == null) throw new FunctionException(pc, "QueryNew", index, index == 1 ? "columnNames" : "columnTypes", "cannot cast to an array or a string list");
		return ListUtil.listToArrayTrim(str, ",");
	}
}