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

import java.util.Iterator;
import java.util.List;

import lucee.runtime.sql.exp.Expression;
import lucee.runtime.sql.exp.ExpressionSupport;

public class OperationN extends ExpressionSupport implements Operation {

	private String operator;
	private List operants;

	public OperationN(String operator, List operants) {
		this.operator = operator;
		this.operants = operants;
	}

	@Override
	public String toString(boolean noAlias) {
		if (!hasIndex() || noAlias) {
			StringBuffer sb = new StringBuffer();
			sb.append(operator);
			sb.append('(');
			Iterator it = operants.iterator();
			boolean isFirst = true;
			while (it.hasNext()) {
				if (!isFirst) sb.append(',');
				Expression exp = (Expression) it.next();
				sb.append(exp.toString(!operator.equalsIgnoreCase("cast")));
				isFirst = false;
			}
			sb.append(')');
			return sb.toString();
		}
		return toString(true) + " as " + getAlias();
	}

	/**
	 * @return the operants
	 */
	public Expression[] getOperants() {
		if (operants == null) return new Expression[0];
		return (Expression[]) operants.toArray(new Expression[operants.size()]);
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	@Override
	public void reset() {
		Iterator it = operants.iterator();
		while (it.hasNext()) {
			Expression exp = (Expression) it.next();
			exp.reset();
		}
	}
}