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
 * Implements the CFML Function querysetrow
 */
package lucee.runtime.functions.query;

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.Array;

public final class QuerySetRow extends BIF {

	private static final long serialVersionUID = -5234853923691806118L;	

	public static boolean call(PageContext pc, Query query, double rowNumber, Object rowData) throws PageException {
		if (rowNumber == -9999) {
			query.addRow(1);
			rowNumber = query.getRecordcount();
		}
		Collection.Key[] colNames = query.getColumnNames();

		if (Decision.isStruct(rowData)){
			Struct data = Caster.toStruct(rowData);
			Iterator<Key> it = data.keyIterator();
			Key key;
			while (it.hasNext()) {
				key = KeyImpl.toKey(it.next());
				query.setAt(key, (int) rowNumber, data.get(key, null));
			}			
		} else if (Decision.isArray(rowData)){
			Array data = Caster.toArray(rowData);
			for (int col = 0; col < colNames.length; col++){
				if (col > data.length) // TODO doesn't work
					break;
				// TODO could use/support trustType for better performance
				query.setAtIndex(col, (int) rowNumber, data[col], false);  // TODO doesn't work, data[col]
			}
		} else {
			throw new FunctionException(pc, QuerySetRow.class.getSimpleName(), 2, "rowData",
				"The argument [rowData] must be either a Struct or Array");
		};		
		return true;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		return call(pc, Caster.toQuery(args[0]), Caster.toDouble(args[1]), args[2]);
	}
}