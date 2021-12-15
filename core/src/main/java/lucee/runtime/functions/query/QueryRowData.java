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

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * implements BIF QueryRowData
 */
public class QueryRowData extends BIF {

	// is this needed?
	private static final long serialVersionUID = -5234853923691806118L;

	public static Object call(PageContext pc, Query query, double rowNumber) throws PageException {
		return call(pc, query, rowNumber, "struct");
	}

	public static Object call(PageContext pc, Query query, double rowNumber, String returnFormat) throws PageException {

		int row = Caster.toInteger(rowNumber);

		if (row < 1 || row > query.getRecordcount()) throw new FunctionException(pc, QueryRowData.class.getSimpleName(), 2, "rowNumber",
				"The argument rowNumber [" + row + "] must be between 1 and the query's record count [" + query.getRecordcount() + "]");

		Collection.Key[] colNames = query.getColumnNames();

		if (!StringUtil.isEmpty(returnFormat, true)) {
			if ("array".equalsIgnoreCase(returnFormat.trim())) {
				Array resultArray = new ArrayImpl();
				for (int col = 0; col < colNames.length; col++) {
					resultArray.append(query.getAt(colNames[col], row, NullSupportHelper.empty(pc)));
				}
				return resultArray;
			}
		}
		Struct result = new StructImpl();

		for (int col = 0; col < colNames.length; col++)
			result.setEL(colNames[col], query.getAt(colNames[col], row, NullSupportHelper.empty(pc)));

		return result;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toQuery(args[0]), Caster.toDouble(args[1]));
		else return call(pc, Caster.toQuery(args[0]), Caster.toInteger(args[1]), Caster.toString(args[2]));
	}
}