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
package lucee.runtime.type.comparator;

import java.sql.Types;
import java.util.Comparator;

import lucee.runtime.PageContext;
import lucee.runtime.db.SQL;
import lucee.runtime.exp.IllegalQoQException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.sql.exp.Column;
import lucee.runtime.sql.exp.Expression;
import lucee.runtime.sql.exp.Literal;
import lucee.runtime.sql.exp.value.ValueNumber;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;

/**
 * Implementation of a Comparator that will sort multiple rows of a query all at the same time
 */
public final class QueryComparator implements Comparator<Integer> {

	private Comparator[] sorts;
	private Key[] cols;
	private Query target;
	private Collection.Key paramKey = new KeyImpl("?");
	private int numSorts = 0;

	/**
	 * constructor of the class
	 *
	 */
	public QueryComparator(PageContext pc, QueryImpl target, Expression[] sortExpressions, boolean isUnion, SQL sql) throws PageException {
		this.sorts = new Comparator[sortExpressions.length];
		this.cols = new Key[sortExpressions.length];
		this.target = target;

		// Build up an array of comparators based on the valid sort expressions, in order
		for (int i = 0; i < sortExpressions.length; i++) {
			Expression sortExpression = sortExpressions[i];

			Key columnKey;

			if (!isUnion) {
				Integer ordinalIndex;
				if (sortExpression instanceof Literal) {
					if (sortExpression instanceof ValueNumber && (ordinalIndex = Caster.toInteger(((Literal) sortExpression).getValue(), null)) != null && ordinalIndex > 0
							&& ordinalIndex <= target.getColumnNames().length) {
						// Sort the column referenced by the ordinal position
						addSOrt(target.getColumnNames()[ordinalIndex - 1], !sortExpression.isDirectionBackward());
					}
					else {
						// All other non-integer literals are invalid.
						throw new IllegalQoQException("ORDER BY item [" + sortExpression.toString(true) + "] in position " + (i + 1)
								+ " cannot be a literal value unless it is an integer matching a select column's ordinal position.", null, sql, null);
					}
				}
				else {
					// order by ? -- ignore this as well
					if (sortExpression instanceof Column && ((Column) sortExpression).getColumn().equals(paramKey)) continue;

					// Lookup column in query based on the index stored in the order by expression
					addSOrt(target.getColumnNames()[sortExpression.getIndex() - 1], !sortExpression.isDirectionBackward());
				}
			}
			else if (sortExpression instanceof Column) {
				Column c = (Column) sortExpression;
				// Lookup column in query based on name of column. unions don't allow operations in
				// the order by
				addSOrt(c.getColumn(), !sortExpression.isDirectionBackward());
			}
			else {
				throw new IllegalQoQException("ORDER BY items must be a column name/alias from the first select list if the statement contains a UNION operator", null, sql, null);
			}
		}
	}

	private void addSOrt(Key columnKey, boolean isAsc) throws PageException {
		cols[numSorts] = columnKey;

		int type = target.getColumn(columnKey).getType();
		// These types use a numeric sort
		if (type == Types.BIGINT || type == Types.BIT || type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT || type == Types.DECIMAL || type == Types.DOUBLE
				|| type == Types.NUMERIC || type == Types.REAL) {
			sorts[numSorts] = new NumberComparator(isAsc, true);
			// Everything else is a case-sensitive text sort
		}
		else {
			sorts[numSorts] = new TextComparator(isAsc, false);
		}
		numSorts++;
	}

	@Override
	public int compare(Integer oLeft, Integer oRight) {
		int currentResult = 0;
		try {
			// Loop over all our sorts. We'll keep checking until we find a column that sorts above or below,
			// or until we run out of sorts to check
			for (int i = 0; i < numSorts; i++) {
				currentResult = sorts[i].compare(target.getAt(cols[i], oLeft), target.getAt(cols[i], oRight));
				// Short circuit if one row is already sorted above or below another
				if (currentResult != 0) {
					return currentResult;
				}
				// If the current sorts were the same for both rows, we continue to the next sort
			}
			// If we made it all the way through the sorts, return the last value
			return currentResult;
		}
		catch (PageException e) {
			// throw new RuntimeException(e);
			return 0;
		}
	}

}