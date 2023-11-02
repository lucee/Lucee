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
 * Implements the CFML Function querysetcell
 */
package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;

public final class QuerySetCell extends BIF {

	private static final long serialVersionUID = -5234853923691806118L;

	public static boolean call(PageContext pc, Query query, String columnName, Object value) throws PageException {
		return call(pc, query, columnName, value, query.getRecordcount());
	}

	public static boolean call(PageContext pc, Query query, String columnName, Object value, double rowNumber) throws PageException {
		if (rowNumber == -9999) rowNumber = query.getRecordcount();// used for named arguments
		query.setAt(KeyImpl.init(columnName), (int) rowNumber, value);
		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), args[2]);
		return call(pc, Caster.toQuery(args[0]), Caster.toString(args[1]), args[2], Caster.toDoubleValue(args[3]));
	}
}