package lucee.runtime.cache.tag.request;

import java.util.HashMap;
import java.util.Map;

import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheHandlerPro;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.MapCacheHandler;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class RequestCacheHandler extends MapCacheHandler implements CacheHandlerPro {

	private static ThreadLocal<Map<String, CacheItem>> data = new ThreadLocal<Map<String, CacheItem>>() {
		@Override
		protected Map<String, CacheItem> initialValue() {
			return new HashMap<String, CacheItem>();
		}
	};

	@Override
	protected Map<String, CacheItem> map() {
		return data.get();
	}

	@Override
	public boolean acceptCachedWithin(Object cachedWithin) {
		return Caster.toString(cachedWithin, "").equalsIgnoreCase("request");
	}

	@Override
	public String pattern() {
		return "request";
	}

	@Override
	public CacheItem get(PageContext pc, String cacheId, Object cachePolicy) throws PageException {
		return get(pc, cacheId);
	}

}
