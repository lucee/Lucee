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
/**
 * Implements the CFML Function querysetcell
 */
package lucee.runtime.functions.query;

import java.util.Arrays;
import java.util.Comparator;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ListUtil;

public final class QuerySort extends BIF {

	private static final long serialVersionUID = -6566120440638749819L;

	public static boolean call(PageContext pc, Query query, Object columnNameOrSortFunc) throws PageException {
		if (Decision.isSimpleValue(columnNameOrSortFunc)) return _call(pc, query, Caster.toString(columnNameOrSortFunc), null);

		return _call(pc, query, Caster.toFunction(columnNameOrSortFunc));
	}

	public static boolean call(PageContext pc, Query query, Object columnNameOrSortFunc, String directions) throws PageException {
		if (Decision.isSimpleValue(columnNameOrSortFunc)) return _call(pc, query, Caster.toString(columnNameOrSortFunc), directions);
		return _call(pc, query, Caster.toFunction(columnNameOrSortFunc));
	}

	public static boolean _call(PageContext pc, Query query, UDF udf) throws PageException {
		int recordcount = query.getRecordcount();
		Key[] columns = query.getColumnNames();
		QueryRow[] rows = new QueryRow[recordcount];
		Struct sct;
		Object empty = NullSupportHelper.full(pc) ? null : "";
		for (int row = 1; row <= recordcount; row++) {
			sct = new StructImpl();
			for (int col = 0; col < columns.length; col++) {
				sct.setEL(columns[col], query.getAt(columns[col], row, empty));
			}
			rows[row - 1] = new QueryRow(query, row, sct);
		}

		Arrays.sort(rows, new QueryRowComparator(pc, udf));
		((QueryImpl) query).sort(toInt(rows));
		return true;
	}

	private static int[] toInt(QueryRow[] rows) {
		int[] ints = new int[rows.length];
		for (int i = 0; i < rows.length; i++) {
			ints[i] = rows[i].rowNbr;
		}
		return ints;
	}

	private static boolean _call(PageContext pc, Query query, String columnNames, String directions) throws PageException {
		// column names
		String[] arrColumnNames = ListUtil.trimItems(ListUtil.listToStringArray(columnNames, ','));
		int[] dirs = new int[arrColumnNames.length];

		// directions
		if (!StringUtil.isEmpty(directions)) {
			String[] arrDirections = ListUtil.trimItems(ListUtil.listToStringArray(directions, ','));
			if (arrColumnNames.length != arrDirections.length) throw new DatabaseException("column names and directions has not the same count", null, null, null);

			String direction;
			for (int i = 0; i < dirs.length; i++) {
				direction = arrDirections[i].toLowerCase();
				dirs[i] = 0;
				if (direction.equals("asc")) dirs[i] = Query.ORDER_ASC;
				else if (direction.equals("desc")) dirs[i] = Query.ORDER_DESC;
				else {
					throw new DatabaseException("argument direction of function querySort must be \"asc\" or \"desc\", now \"" + direction + "\"", null, null, null);
				}
			}
		}
		else {
			for (int i = 0; i < dirs.length; i++) {
				dirs[i] = Query.ORDER_ASC;
			}
		}

		for (int i = arrColumnNames.length - 1; i >= 0; i--)
			query.sort(KeyImpl.init(arrColumnNames[i]), dirs[i]);

		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), args[1]);
		return call(pc, Caster.toQuery(args[0]), args[1], Caster.toString(args[2]));
	}

	public static class QueryRow {

		public final Query query;
		public final int rowNbr;
		public final Struct row;

		public QueryRow(Query query, int rowNbr, Struct row) {
			this.query = query;
			this.rowNbr = rowNbr;
			this.row = row;
		}

	}

	public static class QueryRowComparator implements Comparator<QueryRow> {

		private PageContext pc;
		private final UDF udf;

		public QueryRowComparator(PageContext pc, UDF udf) {
			this.pc = pc;
			this.udf = udf;
		}

		@Override
		public int compare(QueryRow left, QueryRow right) {
			try {
				return Caster.toIntValue(udf.call(pc, new Object[] { left.row, right.row }, true));
			}
			catch (PageException pe) {
				throw new PageRuntimeException(pe);
			}
		}

	}
}