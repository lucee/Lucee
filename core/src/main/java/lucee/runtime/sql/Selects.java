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
import java.util.Iterator;
import java.util.List;

import lucee.runtime.exp.DatabaseException;
import lucee.runtime.sql.exp.Column;
import lucee.runtime.sql.exp.Expression;
import lucee.runtime.sql.exp.op.Operation;
import lucee.runtime.sql.exp.value.ValueNumber;

public class Selects {

	private List<Expression> orderbys = new ArrayList<Expression>();
	private List<Select> selects = new ArrayList<Select>();

	public void addOrderByExpression(Expression exp) {
		this.orderbys.add(exp);
	}

	public void calcOrderByExpressions() {
		if (getSelects().length == 1) {
			// Check if this order by is already present in the select
			for (Expression exp: getOrderbys()) {
				// For each expression in the select column list
				for (Expression col: getSelects()[0].getSelects()) {
					// If this same expression is present, regardless of alias...
					if (col.toString(true).equals(exp.toString(true)) || col.getAlias().equals(exp.getAlias())) {
						// Then set our order by's index to point to the index
						// of the column that has that data
						exp.setIndex(col.getIndex());
						break;
					}
				}
				// Didn't find it? It means we're ordering on a column we're not selecting like
				// SELECT col1 FROM table ORDER BY col2
				if (exp.getIndex() == 0) {
					// We need to add a phantom column into our result so
					// we can track the value and order on it
					exp.setAlias("__order_by_expression__" + getSelects()[0].getSelects().length);
					getSelects()[0].addSelectExpression(exp);
				}
			}
		}
	}

	/**
	 * @return the orderbys
	 */
	public Expression[] getOrderbys() {
		if (orderbys == null) return new Expression[0];
		return orderbys.toArray(new Expression[orderbys.size()]);
	}

	public void addSelect(Select select) {
		selects.add(select);
	}

	public Select[] getSelects() {
		if (selects == null) return new Select[0];
		return selects.toArray(new Select[selects.size()]);
	}

	@Override

	public String toString() {
		return _toString(this);
	}

	public static String _toString(Selects __selects) {
		Select[] _selects = __selects.getSelects();
		Select s;
		StringBuffer sb = new StringBuffer();

		for (int y = 0; y < _selects.length; y++) {
			s = _selects[y];

			if (y > 0) {
				if (s.isUnionDistinct()) sb.append("union distinct\n\n");
				else sb.append("union\n\n");
			}

			sb.append("select\n\t");

			if (s.isDistinct()) sb.append("distinct\n\t");
			ValueNumber top = s.getTop();
			if (top != null) sb.append("top " + top.getString() + "\n\t");
			// select
			Expression[] sels = s.getSelects();
			Expression exp;
			boolean first = true;
			for (int i = 0; i < sels.length; i++) {
				if (!first) sb.append("\t,");
				exp = sels[i];
				sb.append(exp.toString(false) + "\n");
				first = false;
			}

			// from
			sb.append("from\n\t");
			Column[] forms = s.getFroms();
			first = true;
			for (int i = 0; i < forms.length; i++) {
				if (!first) sb.append("\t,");
				exp = forms[i];
				sb.append(exp.toString(false) + "\n");
				first = false;
			}

			// where
			if (s.getWhere() != null) {
				sb.append("where \n\t");
				sb.append(s.getWhere().toString(true));
				sb.append("\n");
			}

			// group by
			Expression[] gbs = s.getGroupbys();
			if (gbs.length > 0) {
				sb.append("group by\n\t");
				first = true;
				for (int i = 0; i < gbs.length; i++) {
					if (!first) sb.append("\t,");
					exp = gbs[i];
					sb.append(exp.toString(false) + "\n");
					first = false;
				}
			}

			// having
			Operation having = s.getHaving();
			if (having != null) {
				sb.append("having \n\t");
				sb.append(having.toString(true));
				sb.append("\n");
			}

		}

		// order by
		if (__selects.orderbys != null && __selects.orderbys.size() > 0) {
			sb.append("order by\n\t");
			Iterator<Expression> it = __selects.orderbys.iterator();
			Expression exp;
			boolean first = true;
			while (it.hasNext()) {
				if (!first) sb.append("\t,");
				exp = it.next();
				sb.append(exp.toString(false) + " " + (exp.isDirectionBackward() ? "DESC" : "ASC") + "\n");
				first = false;
			}
		}
		return sb.toString();
	}

	public Column[] getTables() {
		Iterator<Select> it = selects.iterator();
		Select s;
		ArrayList<Column> rtn = new ArrayList<Column>();
		Column[] froms;
		while (it.hasNext()) {
			s = it.next();
			froms = s.getFroms();
			for (int i = 0; i < froms.length; i++) {
				rtn.add(froms[i]);
			}
		}
		return rtn.toArray(new Column[rtn.size()]);
	}

	public boolean isDistinct() {
		Select s;
		int len = selects.size();
		if (len == 1) {
			s = selects.get(0);
			return s.isDistinct();
		}
		for (int i = 1; i < len; i++) {
			s = selects.get(i);
			if (!s.isUnionDistinct()) return false;
		}
		return true;
	}

	public Column[] getDistincts() throws DatabaseException {
		List<Column> columns = new ArrayList<Column>();
		Select s;
		int len = selects.size();
		if (len == 1) {
			s = selects.get(0);
			Expression[] _selects = s.getSelects();
			for (int i = 0; i < _selects.length; i++) {
				if (_selects[i] instanceof Column) {
					columns.add((Column) _selects[i]);
				}
			}
			return columns.toArray(new Column[columns.size()]);
		}
		throw new DatabaseException("not supported for Union distinct", null, null, null);
	}
}