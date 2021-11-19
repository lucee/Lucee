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
package lucee.runtime.sql.exp.op;

import lucee.runtime.sql.exp.Expression;
import lucee.runtime.sql.exp.ExpressionSupport;

public class Operation3 extends ExpressionSupport implements Operation {

	private Expression exp;
	private Expression left;
	private Expression right;
	private int operator;

	public Operation3(Expression exp, Expression left, Expression right, int operator) {
		this.exp = exp;
		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	@Override
	public String toString(boolean noAlias) {
		// like escape
		if (Operation.OPERATION3_LIKE == operator) {
			if (!hasAlias() || noAlias) {
				return exp.toString(true) + " like " + left.toString(true) + " escape " + right.toString(true);
			}
			return toString(true) + " as " + getAlias();
		}
		// between
		if (!hasAlias() || noAlias) {
			return exp.toString(true) + " between " + left.toString(true) + " and " + right.toString(true);
		}
		return toString(true) + " as " + getAlias();
	}

	/**
	 * @return the exp
	 */
	public Expression getExp() {
		return exp;
	}

	/**
	 * @return the left
	 */
	public Expression getLeft() {
		return left;
	}

	/**
	 * @return the operator
	 */
	public int getOperator() {
		return operator;
	}

	/**
	 * @return the right
	 */
	public Expression getRight() {
		return right;
	}

	@Override
	public void reset() {
		if (left != null) {
			left.reset();
		}
		if (right != null) {
			right.reset();
		}
		if (exp != null) {
			exp.reset();
		}
	}

	@Override
	public boolean hasAggregate() {
		if (left instanceof OperationAggregate) {
			return true;
		}
		if (left instanceof Operation && ((Operation) left).hasAggregate()) {
			return true;
		}
		if (right instanceof OperationAggregate) {
			return true;
		}
		if (right instanceof Operation && ((Operation) right).hasAggregate()) {
			return true;
		}
		if (exp instanceof OperationAggregate) {
			return true;
		}
		if (exp instanceof Operation && ((Operation) exp).hasAggregate()) {
			return true;
		}
		return false;
	}
}