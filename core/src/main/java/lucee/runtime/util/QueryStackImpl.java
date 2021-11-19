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
package lucee.runtime.util;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;

/**
 * Stack for Query Objects
 */
public final class QueryStackImpl implements QueryStack {
	Query[] queries = new Query[20];
	int start = queries.length;

	@Override
	public QueryStack duplicate(boolean deepCopy) {
		QueryStackImpl qs = new QueryStackImpl();
		if (deepCopy) {
			qs.queries = new Query[queries.length];
			for (int i = 0; i < queries.length; i++) {
				qs.queries[i] = (Query) Duplicator.duplicate(queries[i], deepCopy);
			}
		}
		else qs.queries = queries;

		qs.start = start;
		return qs;
	}

	@Override
	public void addQuery(Query query) {
		if (start < 1) grow();
		queries[--start] = query;
	}

	@Override
	public void removeQuery() {
		// print.ln("queries["+start+"]=null;");
		queries[start++] = null;
	}

	@Override
	public boolean isEmpty() {
		return start == queries.length;
	}

	@Override
	public Object getDataFromACollection(PageContext pc, Key key, Object defaultValue) {
		// Object rtn;
		QueryColumn col;
		// get data from queries
		for (int i = start; i < queries.length; i++) {
			col = queries[i].getColumn(key, null);
			if (col != null) return col.get(queries[i].getCurrentrow(pc.getId()), NullSupportHelper.empty(pc));
			// rtn=((Objects)queries[i]).get(pc,key,Null.NULL);
			// if(rtn!=Null.NULL) return rtn;
		}
		return defaultValue;
	}

	@Override
	public QueryColumn getColumnFromACollection(Key key) {
		QueryColumn rtn = null;

		// get data from queries
		for (int i = start; i < queries.length; i++) {
			rtn = queries[i].getColumn(key, null);
			if (rtn != null) {
				return rtn;
			}
		}
		return null;
	}

	@Override
	public void clear() {
		for (int i = start; i < queries.length; i++) {
			queries[i] = null;
		}
		start = queries.length;
	}

	private void grow() {
		Query[] tmp = new Query[queries.length + 20];
		for (int i = 0; i < queries.length; i++) {
			tmp[i + 20] = queries[i];
		}
		queries = tmp;
		start += 20;
	}

	@Override
	public Query[] getQueries() {
		Query[] tmp = new Query[queries.length - start];
		int count = 0;
		for (int i = start; i < queries.length; i++) {
			tmp[count++] = queries[i];
		}
		return tmp;
	}
}