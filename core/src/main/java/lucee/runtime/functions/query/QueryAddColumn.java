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

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;

public final class QueryAddColumn extends BIF {

	private static final long serialVersionUID = -242783888553490683L;

	public static double call(PageContext pc, Query query, String string) throws PageException {
		return call(pc, query, string, null, new ArrayImpl());
	}

	public static double call(PageContext pc, Query query, String string, Object arrayOrDataType) throws PageException {
		if (!Decision.isArray(arrayOrDataType)) return call(pc, query, string, Caster.toString(arrayOrDataType), new ArrayImpl());
		return call(pc, query, string, null, Caster.toArray(arrayOrDataType));
	}

	public static double call(PageContext pc, Query query, String string, Object datatype, Object array) throws PageException {
		if (StringUtil.isEmpty(datatype)) query.addColumn(KeyImpl.init(string), Caster.toArray(array));
		else query.addColumn(KeyImpl.init(string), Caster.toArray(array), SQLCaster.toSQLType(Caster.toString(datatype)));
		return query.size();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), args[2]);
		return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), args[2], args[3]);
	}
}