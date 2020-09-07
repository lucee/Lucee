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

public class Operation1 extends ExpressionSupport implements Operation {

	private Expression exp;
	private int operator;

	/**
	 * @return the exp
	 */
	public Expression getExp() {
		return exp;
	}

	/**
	 * @return the operator
	 */
	public int getOperator() {
		return operator;
	}

	public Operation1(Expression exp, int operator) {
		this.exp = exp;
		this.operator = operator;
	}

	@Override
	public String toString(boolean noAlias) {
		if (!hasAlias() || noAlias) {
			if (operator == OPERATION1_IS_NULL || operator == OPERATION1_IS_NOT_NULL) {
				return exp.toString(true) + " " + Operation2.toString(operator);
			}
			return Operation2.toString(operator) + " " + exp.toString(true);
		}
		return toString(true) + " as " + getAlias();
	}

	@Override
	public void reset() {
		if (exp != null) {
			exp.reset();
		}
	}

}