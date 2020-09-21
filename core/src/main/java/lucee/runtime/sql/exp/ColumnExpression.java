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
package lucee.runtime.sql.exp;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.util.QueryUtil;

public class ColumnExpression extends ExpressionSupport implements Column {

	private String table;
	private String column;
	private Collection.Key columnKey;
	private Collection.Key columnAliasKey;
	private boolean hasBracked;
	private boolean isParam;
	private int columnIndex;
	private QueryColumn col;

	@Override
	public String toString() {
		return "table:" + table + ";column:" + column + ";hasBracked:" + hasBracked + ";columnIndex:" + columnIndex;

	}

	public ColumnExpression(String value, int columnIndex) {
		this.column = value;
		this.columnIndex = columnIndex;
		if (value.equals("?")) {
			this.isParam = true;
		}
	}

	public void setSub(String sub) {
		if (table == null) {
			table = column;
			column = sub;
		}
		else column = (column + "." + sub);
	}

	@Override
	public String toString(boolean noAlias) {
		if (hasAlias() && !noAlias) return getFullName() + " as " + getAlias();
		return getFullName();
	}

	@Override
	public String getFullName() {
		if (table == null) return column;
		return table + "." + column;
	}

	@Override
	public boolean isParam() {
		return this.isParam;
	}

	@Override
	public String getAlias() {
		if (!hasAlias()) return getColumn().getString();
		return super.getAlias();
	}

	@Override
	public Collection.Key getColumn() {
		if (columnKey == null) columnKey = KeyImpl.init(column);
		return columnKey;
	}

	@Override
	public Collection.Key getColumnAlias() {
		if (columnAliasKey == null) columnAliasKey = KeyImpl.init(getAlias());
		return columnAliasKey;
	}

	@Override
	public String getTable() {
		return table;
	}

	@Override
	public boolean hasBracked() {
		return hasBracked;
	}

	@Override
	public void hasBracked(boolean b) {
		this.hasBracked = b;
	}

	public String getColumnName() {

		return column;
	}

	/**
	 * @return the columnIndex
	 */
	@Override
	public int getColumnIndex() {
		return columnIndex;
	}

	// MUST handle null correctly
	@Override
	public Object getValue(PageContext pc, Query qr, int row) throws PageException {
		if (col == null) col = qr.getColumn(getColumn());
		return QueryUtil.getValue(pc, col, row);
	}

	@Override
	public Object getValue(PageContext pc, Query qr, int row, Object defaultValue) {
		if (col == null) {
			col = qr.getColumn(getColumn(), null);
			if (col == null) return defaultValue;
		}
		return col.get(row, defaultValue);
	}

	@Override
	public void reset() {
		col = null;
	}

}