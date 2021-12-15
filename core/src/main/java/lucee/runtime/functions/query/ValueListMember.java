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
package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.runtime.type.util.ListUtil;

public class ValueListMember extends BIF {

	public static String call(PageContext pc, Query query, String columnName) throws PageException {
		return call(pc, query, columnName, ",");
	}

	public static String call(PageContext pc, Query query, String columnName, String delimiter) throws PageException {
		if (delimiter == null) delimiter = ",";
		return ListUtil.arrayToList(QueryColumnData.call(pc, query, columnName, null), delimiter);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]));
		return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
	}
}