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
 * creates a CFML query Column
 */
package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Array;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.util.CollectionUtil;

public final class Query_ extends BIF {

	private static final long serialVersionUID = -3496695992298284984L;

	public static Query call(PageContext pc, Object[] arr) throws DatabaseException {
		String[] names = new String[arr.length];
		Array[] columns = new Array[arr.length];
		int count = 0;

		for (int i = 0; i < arr.length; i++) {
			if (arr[i] instanceof FunctionValue) {
				FunctionValue vf = (FunctionValue) arr[i];
				if (vf.getValue() instanceof Array) {
					names[count] = vf.getNameAsString();
					columns[count] = (Array) vf.getValue();
					count++;
				}
				else throw new DatabaseException("invalid argument for function query, only array as value are allowed", "example: query(column1:array(1,2,3))", null, null);
			}
			else throw new DatabaseException("invalid argument for function query, only named argument are allowed", "example: query(column1:array(1,2,3))", null, null);
		}
		Query query = new QueryImpl(CollectionUtil.toKeys(names, true), columns, "query");
		return query;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		return call(pc, (Object[]) args[0]);
	}
}