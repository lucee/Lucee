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
package lucee.runtime.cache.tag.query;

import lucee.commons.digest.HashUtil;
import lucee.runtime.cache.tag.udf.UDFArgConverter;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.query.QueryResult;

public final class QueryCacheItem extends QueryResultCacheItem {

	private static final long serialVersionUID = 7327671003736543783L;

	public final Query query;
	private String hash;

	public QueryCacheItem(Query query, String[] tags, String datasourceName, long cacheTime) {
		super((QueryResult) query, tags, datasourceName, cacheTime);
		this.query = query;
	}

	public QueryCacheItem(Query query, String[] tags, String datasourceName) {
		this(query, tags, datasourceName, System.currentTimeMillis());
	}

	@Override
	public String getHashFromValue() {
		// TODO faster impl
		if (hash == null) hash = Long.toString(HashUtil.create64BitHash(UDFArgConverter.serialize(query)));
		return hash;
	}

	public Query getQuery() {
		return query;
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		// LDEV-1406: the copy shares the column data with this item and copies it only when it gets written to
		Query qry = (query instanceof QueryImpl) ? ((QueryImpl) query).duplicateCopyOnWrite() : (Query) query.duplicate(true);
		return new QueryCacheItem(qry, getTags(), getDatasourceName(), getCreationDate());
	}

}