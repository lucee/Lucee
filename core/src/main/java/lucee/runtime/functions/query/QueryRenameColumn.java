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
 * Implements the CFML Function queryaddcolumn
 */
package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;

public final class QueryRenameColumn extends BIF {

	private static final long serialVersionUID = -242783888553490683L;

	public static boolean call(PageContext pc, Query query, String columnName, String newColumnName) throws PageException {
		query.rename(KeyImpl.init(columnName), KeyImpl.init(newColumnName));
		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		throw new FunctionException(pc, "QueryRenameColumn", 2, 2, args.length);
	}
}