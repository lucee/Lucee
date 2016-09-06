package lucee.runtime.cache.tag.query;

import java.io.Serializable;
import java.util.Date;

import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.type.Duplicable;
import lucee.runtime.type.Query;
import lucee.runtime.type.query.QueryArray;
import lucee.runtime.type.query.QueryResult;
import lucee.runtime.type.query.QueryStruct;

public abstract class QueryResultCacheItem  implements CacheItem, Dumpable, Serializable,Duplicable {

	private static final long serialVersionUID = -2322582053856364084L;

	private QueryResult queryResult;
	private final long creationDate;

	protected QueryResultCacheItem(QueryResult qr) {
		this.queryResult=qr;
		this.creationDate=System.currentTimeMillis();
	}
	

	public static CacheItem newInstance(QueryResult qr, CacheItem defaultValue) {
		if(qr instanceof Query)
			return new QueryCacheItem((Query) qr);
		else if(qr instanceof QueryArray)
			return new QueryArrayItem((QueryArray) qr);
		else if(qr instanceof QueryStruct)
			return new QueryStructItem((QueryStruct) qr);
		return defaultValue;
	}
	

	public final QueryResult getQueryResult() {
		return queryResult;
	}

	@Override
	public final String getName() {
		return queryResult.getName();
	}
	

	@Override
	public final long getPayload() {
		return queryResult.getRecordcount();
	}
	
	@Override
	public final String getMeta() {
		return queryResult.getSql().getSQLString();
	}

	@Override
	public final long getExecutionTime() {
		return queryResult.getExecutionTime();
	}
	

	@Override
	public final DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		return queryResult.toDumpData(pageContext, maxlevel, properties);
	}
	
	public boolean isCachedAfter(Date cacheAfter) {
    	if(cacheAfter==null) return true;
    	if(creationDate>=cacheAfter.getTime()){
        	return true;
        }
        return false;
    }

}
