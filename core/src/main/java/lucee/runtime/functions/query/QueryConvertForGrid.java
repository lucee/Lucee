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
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class QueryConvertForGrid extends BIF {

	private static final long serialVersionUID = 871091293736619034L;

	public static Struct call(PageContext pc, Query src, double dpage, double dpageSize) throws PageException {
		int page = (int) dpage;
		int pageSize = (int) dpageSize;
		if (page < 1) {
			throw new FunctionException(pc, "QueryConvertForGrid", 2, "page", "page must be a positive number now (" + page + ")");
		}

		int start = ((page - 1) * pageSize) + 1;
		int end = start + pageSize;

		Collection.Key[] srcColumns = src.getColumnNames();
		int srcRows = src.getRowCount();

		int trgRows = srcRows - start + 1;
		if (trgRows > pageSize) trgRows = pageSize;
		if (trgRows < 0) trgRows = 0;

		Query trg = new QueryImpl(srcColumns, trgRows, src.getName());
		int trgRow = 0;
		for (int srcRow = start; (srcRow <= end) && (srcRow <= srcRows); srcRow++) {
			trgRow++;
			for (int col = 0; col < srcColumns.length; col++) {
				trg.setAtEL(srcColumns[col], trgRow, src.getAt(srcColumns[col], srcRow, null));
			}
		}

		Struct sct = new StructImpl();
		sct.setEL(KeyConstants._QUERY, trg);
		sct.setEL("TOTALROWCOUNT", new Integer(srcRows));
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		return call(pc, Caster.toQuery(args[0]), Caster.toDoubleValue(args[1]), Caster.toDoubleValue(args[2]));
	}
}