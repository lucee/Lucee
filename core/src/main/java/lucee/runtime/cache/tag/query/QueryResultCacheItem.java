package lucee.runtime.cache.tag.query;

import java.io.Serializable;
import java.util.Date;

import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.db.DataSource;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.type.Duplicable;
import lucee.runtime.type.Query;
import lucee.runtime.type.query.QueryArray;
import lucee.runtime.type.query.QueryResult;
import lucee.runtime.type.query.QueryStruct;

public abstract class QueryResultCacheItem implements CacheItem, Dumpable, Serializable, Duplicable {

	private static final long serialVersionUID = -2322582053856364084L;
	private static final String[] EMPTY = new String[0];

	private final long creationDate;
	private QueryResult queryResult;
	private String[] tags;
	private final String dsn;

	protected QueryResultCacheItem(QueryResult qr, String[] tags, String datasourceName, long creationDate) {
		this.queryResult = qr;
		this.creationDate = creationDate;
		this.tags = (tags == null) ? EMPTY : tags;
		this.dsn = datasourceName;
	}

	public static CacheItem newInstance(QueryResult qr, String[] tags, DataSource ds, CacheItem defaultValue) {
		String dsn = ds == null ? null : ds.getName();
		if (qr instanceof Query) return new QueryCacheItem((Query) qr, tags, dsn);
		else if (qr instanceof QueryArray) return new QueryArrayItem((QueryArray) qr, tags, dsn);
		else if (qr instanceof QueryStruct) return new QueryStructItem((QueryStruct) qr, tags, dsn);
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

	public final String[] getTags() {
		return tags;
	}

	public final String getDatasourceName() {
		return dsn;
	}

	public final long getCreationDate() {
		return creationDate;
	}

	@Override
	public final DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		return queryResult.toDumpData(pageContext, maxlevel, properties);
	}

	public boolean isCachedAfter(Date cacheAfter) {
		if (cacheAfter == null) return true;
		if (creationDate >= cacheAfter.getTime()) {
			return true;
		}
		return false;
	}

}
