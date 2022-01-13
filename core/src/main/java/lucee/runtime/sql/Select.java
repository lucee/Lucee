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
package lucee.runtime.sql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lucee.runtime.sql.exp.Column;
import lucee.runtime.sql.exp.ColumnExpression;
import lucee.runtime.sql.exp.Expression;
import lucee.runtime.sql.exp.op.Operation;
import lucee.runtime.sql.exp.op.OperationAggregate;
import lucee.runtime.sql.exp.value.ValueNumber;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;

public class Select {
	private List selects = new ArrayList();
	private Set<String> additionalColumns = new HashSet();
	private List froms = new ArrayList();
	private Operation where;
	private List groupbys = new ArrayList();
	private Operation having;
	private ValueNumber top;
	private boolean distinct;
	private boolean unionDistinct;

	public void addSelectExpression(Expression select) {
		// Make sure there isn't already a column or alias of the same name. This will just cause issues down the road since our
		// column counts in the final query won't match the index in the expression
		for (Expression col: getSelects()) {
			if (col.getAlias().equalsIgnoreCase(select.getAlias())) {
				return;
			}
		}
		selects.add(select);
		select.setIndex(selects.size());
	}

	public void expandAsterisks(Query source) {
		Expression[] selectCols = getSelects();
		this.selects.clear();
		Iterator<Key> it;
		Key k;
		for (Expression col: selectCols) {
			if (col.getAlias().equals("*")) {
				it = source.keyIterator();
				while (it.hasNext()) {
					k = it.next();
					addSelectExpression(new ColumnExpression(k.getString(), 0));
				}
			}
			else {
				addSelectExpression(col);
			}
		}
		// We may not need all of these now.
		calcAdditionalColumns(getAdditionalColumns());
	}

	public void addFromExpression(Column exp) {
		froms.add(exp);
		exp.setIndex(froms.size());
	}

	public void setWhereExpression(Operation where) {
		this.where = where;
	}

	public void addGroupByExpression(Expression col) {
		this.groupbys.add(col);
	}

	public void setTop(ValueNumber top) {
		this.top = top;
	}

	public void calcAdditionalColumns(Set<String> allColumns) {
		// Remove any columns we are explicitly selecting
		for (Expression expSelect: getSelects()) {
			if (expSelect instanceof ColumnExpression) {
				ColumnExpression ce = (ColumnExpression) expSelect;
				allColumns.remove(ce.getColumnName());
			}
		}
		// What's left are columns used by functions and aggregates,
		// but not directly part of the final result
		this.additionalColumns = allColumns;
	}

	public Set<String> getAdditionalColumns() {
		return this.additionalColumns;
	}

	/**
	 * @return the froms
	 */
	public Column[] getFroms() {
		return (Column[]) froms.toArray(new Column[froms.size()]);
	}

	/**
	 * @return the groupbys
	 */
	public Expression[] getGroupbys() {
		if (groupbys == null) return new Column[0];
		return (Expression[]) groupbys.toArray(new Expression[groupbys.size()]);
	}

	/**
	 * @return the havings
	 */
	public Operation getHaving() {
		return having;
	}

	/**
	 * @return the selects
	 */
	public Expression[] getSelects() {
		return (Expression[]) selects.toArray(new Expression[selects.size()]);
	}

	/**
	 * @return whether at least one select is an aggregate
	 */
	public boolean hasAggregateSelect() {
		for (Expression col: getSelects()) {
			if (col instanceof OperationAggregate) {
				return true;
			}
			if (col instanceof Operation && ((Operation) col).hasAggregate()) {
				return true;
			}
		}
		return false;

	}

	/**
	 * @return the where
	 */
	public Operation getWhere() {
		return where;
	}

	public boolean isUnionDistinct() {
		return unionDistinct;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean b) {
		this.distinct = b;
	}

	public void setUnionDistinct(boolean b) {
		// print.out("-"+b);
		this.unionDistinct = b;
	}

	/**
	 * @param having the having to set
	 */
	public void setHaving(Operation having) {
		this.having = having;
	}

	/**
	 * @return the top
	 */
	public ValueNumber getTop() {
		return top;
	}

}