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

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.QueryColumn;

public class ValueArray extends BIF {

	private static final long serialVersionUID = -1810991362001086246L;

	public static Array call(PageContext pc, QueryColumn column) throws PageException {
		Array arr = new ArrayImpl();
		int size = column.size();
		Object obj;
		short type = SQLCaster.toCFType(column.getType(), lucee.commons.lang.CFTypes.TYPE_UNDEFINED);

		for (int i = 1; i <= size; i++) {
			obj = column.get(i, null);
			try {
				obj = Caster.castTo(pc, type, column.getTypeAsString(), obj);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
			arr.append(obj);
		}
		return arr;
	}

	public static Array call(PageContext pc, String strQueryColumn) throws PageException {
		return call(pc, ValueList.toColumn(pc, strQueryColumn));
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args[0] instanceof QueryColumn) return call(pc, (QueryColumn) args[0]);
		return call(pc, Caster.toString(args[0]));
	}
}