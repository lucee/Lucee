package lucee.runtime.cache.tag.query;

import lucee.commons.digest.HashUtil;
import lucee.runtime.cache.tag.udf.UDFArgConverter;
import lucee.runtime.type.query.QueryArray;

public class QueryArrayItem extends QueryResultCacheItem {

	private static final long serialVersionUID = 7327671003736543783L;

	public final QueryArray queryArray;

	public QueryArrayItem(QueryArray queryArray, String[] tags, String datasourceName) {
		super(queryArray, tags, datasourceName, System.currentTimeMillis());
		this.queryArray = queryArray;
	}

	@Override
	public String getHashFromValue() {
		return Long.toString(HashUtil.create64BitHash(UDFArgConverter.serialize(queryArray)));
	}

	public QueryArray getQueryArray() {
		return queryArray;
	}

	@Override
	public Object duplicate(boolean deepCopy) {
		return new QueryArrayItem((QueryArray) queryArray.duplicate(true), getTags(), getDatasourceName());
	}

}