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
 * Implements the CFML Function valuelist
 */
package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.ref.VariableReference;
import lucee.runtime.type.scope.Scope;

public class ValueList implements Function {

	private static final long serialVersionUID = -6503473251723048160L;

	public static String call(PageContext pc, String strQueryColumn) throws PageException {
		return call(pc, toColumn(pc, strQueryColumn), ",");
	}

	public static String call(PageContext pc, String strQueryColumn, String delimiter) throws PageException {
		return call(pc, toColumn(pc, strQueryColumn), delimiter);
	}

	public static String call(PageContext pc, QueryColumn column) throws PageException {
		return call(pc, column, ",");
	}

	public static String call(PageContext pc, QueryColumn column, String delimiter) throws PageException {
		StringBuilder sb = new StringBuilder();
		int size = column.size();
		for (int i = 1; i <= size; i++) {
			if (i > 1) sb.append(delimiter);
			sb.append(Caster.toString(column.get(i, null)));
		}
		return sb.toString();
	}

	protected static QueryColumn toColumn(PageContext pc, String strQueryColumn) throws PageException {
		// if(strQueryColumn.indexOf('.')<1)
		// throw new ExpressionException("invalid query column definition ["+strQueryColumn+"]");
		VariableReference ref = ((PageContextImpl) pc).getVariableReference(strQueryColumn);
		if (ref.getParent() instanceof Scope) throw new ExpressionException("invalid query column definition [" + strQueryColumn + "]");

		Query query = Caster.toQuery(ref.getParent());
		return query.getColumn(ref.getKey());
	}
}