package lucee.runtime.cache.tag.query;

import lucee.commons.digest.HashUtil;
import lucee.runtime.cache.tag.udf.UDFArgConverter;
import lucee.runtime.type.Query;
import lucee.runtime.type.query.QueryArray;
import lucee.runtime.type.query.QueryResult;

public class QueryArrayItem extends QueryResultCacheItem {

	private static final long serialVersionUID = 7327671003736543783L;

	public final QueryArray queryArray;

	public QueryArrayItem(QueryArray queryArray){
		super(queryArray);
		this.queryArray=queryArray;
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
		return new QueryArrayItem((QueryArray)queryArray.duplicate(true));
	}

}