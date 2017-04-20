package lucee.runtime.cache.util;

import lucee.print;
import lucee.commons.io.cache.CacheEntry;
import lucee.commons.io.cache.CacheEntryFilter;
import lucee.commons.io.cache.CacheFilter;
import lucee.runtime.cache.tag.query.QueryResultCacheItem;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.ListUtil;

public class QueryTagFilter implements CacheEntryFilter {
	
	private Struct sct=new StructImpl();
	private String[] tags;

	public QueryTagFilter(String[] tags) {
		this.tags=tags;
		for(String tag:tags) {
			this.sct.put(tag, "");
		}
	}

	@Override
	public boolean accept(CacheEntry ce) {
		Object val = ce.getValue();
		if(val instanceof QueryResultCacheItem) {
			String[] _tags = ((QueryResultCacheItem)val).getTags();
			if(_tags!=null) {
				for(String tag:_tags) {
					if(sct.containsKey(tag)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toPattern() {
		return "tags:"+ListUtil.arrayToList(tags, ", ");
	}
}
