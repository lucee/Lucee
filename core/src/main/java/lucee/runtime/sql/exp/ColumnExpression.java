/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
	private boolean cacheColumn;

	@Override
	public String toString() {
		return "table:" + table + ";column:" + column + ";hasBracked:" + hasBracked + ";columnIndex:" + columnIndex;

	}

	public ColumnExpression(String value, int columnIndex, boolean cacheColumn) {
		this.column = value;
		this.cacheColumn = cacheColumn;
		this.columnIndex = columnIndex;
		if (value.equals("?")) {
			this.isParam = true;
		}
	}

	public ColumnExpression(String value, int columnIndex) {
		this(value, columnIndex, true);
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
		return QueryUtil.getValue(pc, getCol( qr ), row);
	}

	@Override
	public Object getValue(PageContext pc, Query qr, int row, Object defaultValue) {
		try {
			return getCol( qr ).get(row, defaultValue);
		// Per the interface, methods accepting a default value cannot throw an exception,
		// so we must return the default value if any exceptions happen.
		} catch( PageException e ) {
			return defaultValue;
		}
	}

	/**
		Tells this column expression to not cache the column reference back to the original query
	*/
	public void setCacheColumn(boolean cacheColumn) {
		this.cacheColumn = cacheColumn;
	}

	/**
		Acquire the actual query column reference, taking caching into account
		We cache the lookup of the column for basic selects because we run the same thing
		over and over on the same query object.  But for partitioned selects, we have multiple query
		objects we run this on, so we can't cache the column reference
	 */
	private QueryColumn getCol(Query qr) throws PageException {
		// If we're not caching the query column, get it fresh
		if( !cacheColumn ) {
			return qr.getColumn(getColumn());
		// If we are caching and we have no reference, create it and return it
		} else if (col == null) {
			// This behavior needs to be thread safe.
			synchronized( this ) {
				// Double check lock pattern in case another thread beat us
				if (col != null) {
					return col;
				}
				return col = qr.getColumn(getColumn());
			}
		// If we are caching and we have the reference already, just return it!
		} else {
			return col;
		}

	}


	@Override
	public void reset() {
		col = null;
	}

}