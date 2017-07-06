package lucee.runtime.cache.tag.request;

import java.util.HashMap;
import java.util.Map;

import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.MapCacheHandler;
import lucee.runtime.op.Caster;

public class RequestCacheHandler extends MapCacheHandler {
	
	private static ThreadLocal<Map<String,CacheItem>> data=new ThreadLocal<Map<String,CacheItem>>() {
		@Override 
		protected Map<String,CacheItem> initialValue() {
			return new HashMap<String, CacheItem>();
		}
	};
	
	@Override
	protected Map<String, CacheItem> map() {
		return data.get();
	}
	

	
	@Override
	public boolean acceptCachedWithin(Object cachedWithin) {
		String str=Caster.toString(cachedWithin,"").trim();
		return str.equalsIgnoreCase("request");
	}

	@Override
	public String pattern() {
		return "request";
	}
	
}
